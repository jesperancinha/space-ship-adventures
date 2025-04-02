package org.jesperancinha.space.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jesperancinha.space.dao.*
import org.jesperancinha.space.dto.Message
import org.jesperancinha.space.dto.TransmissionNgDto
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class MessageService {
    suspend fun createMessage(message: Message): MessageEntity {
        return withContext(Dispatchers.IO) {
            transaction {
                val messageId = Messages
                    .insertAndGetId {
                        it[purpose] = message.purpose
                        it[Messages.message] = message.message
                    }
                MessageEntity(messageId.value, message.purpose, message.message)
            }
        }
    }

    suspend fun getMessages(): List<MessageEntity> {
        return withContext(Dispatchers.IO) {
            transaction {
                Messages.selectAll().map {
                    MessageEntity(it[Messages.id].value, it[Messages.purpose], it[Messages.message])
                }
            }
        }
    }

    suspend fun getMessage(id: Int): MessageEntity? {
        return withContext(Dispatchers.IO) {
            transaction {
                Messages.selectAll().where { Messages.id eq id }.map {
                    MessageEntity(it[Messages.id].value, it[Messages.purpose], it[Messages.message])
                }.singleOrNull()
            }
        }
    }

    suspend fun updateMessage(id: Int, message: Message): MessageEntity? {
        return withContext(Dispatchers.IO) {
            transaction {
                val updatedRowCount = Messages.update({ Messages.id eq id }) {
                    it[purpose] = message.purpose
                    it[Messages.message] = message.message
                }
                if (updatedRowCount > 0) {
                    MessageEntity(id, message.purpose, message.message)
                } else {
                    null
                }
            }
        }
    }

    suspend fun deleteMessage(id: Int): Boolean {
        return withContext(Dispatchers.IO) {
            transaction {
                Messages.deleteWhere { Messages.id eq id } > 0
            }
        }
    }
}

class TransmissionService(private val messageService: MessageService) {

    suspend fun createTransmission(transmission: TransmissionNgDto): TransmissionNgDto {
        val messagePackage = transmission.messagePackage
        messagePackage.messages.map { message ->
            messageService.createMessage(message).id!!
        }

        return withContext(Dispatchers.IO) {
            transaction {
                val messagePackageId = MessagePackages
                    .insertAndGetId {
                        it[timestamp] = messagePackage.timestamp
                    }

                val transmissionId = Transmissions.insertAndGetId {
                    it[sender] = transmission.sender
                    it[receiver] = transmission.receiver
                    it[extraInfo] = transmission.extraInfo
                    it[Transmissions.messagePackage] = messagePackageId.value
                    it[timestamp] = transmission.timestamp
                }

                TransmissionNgDto(
                    transmissionId.value,
                    transmission.sender,
                    transmission.receiver,
                    transmission.extraInfo,
                    transmission.messagePackage,
                    transmission.timestamp
                )
            }
        }
    }

    suspend fun getTransmissions(): List<TransmissionNgDtoEntity> {
        return withContext(Dispatchers.IO) {
            transaction {
                Transmissions.selectAll().map {
                    val messagePackageId = it[Transmissions.messagePackage]
                    TransmissionNgDtoEntity(
                        it[Transmissions.id].value,
                        it[Transmissions.sender],
                        it[Transmissions.receiver],
                        it[Transmissions.extraInfo],
                        messagePackageId.value,
                        it[Transmissions.timestamp]
                    )
                }
            }
        }
    }

    suspend fun getTransmission(id: Int): TransmissionNgDtoEntity? {
        return withContext(Dispatchers.IO) {
            transaction {
                Transmissions.selectAll()
                    .where { Transmissions.id eq id }
                    .map {
                        val messagePackageId = it[Transmissions.messagePackage]
                        TransmissionNgDtoEntity(
                            it[Transmissions.id].value,
                            it[Transmissions.sender],
                            it[Transmissions.receiver],
                            it[Transmissions.extraInfo],
                            messagePackageId.value,
                            it[Transmissions.timestamp]
                        )
                    }.singleOrNull()
            }
        }
    }

    suspend fun updateTransmission(id: Int, transmission: TransmissionNgDto): TransmissionNgDtoEntity? {
        return withContext(Dispatchers.IO) {
            transaction {
                val updatedRowCount = Transmissions.update({ Transmissions.id eq id }) {
                    it[sender] = transmission.sender
                    it[receiver] = transmission.receiver
                    it[extraInfo] = transmission.extraInfo
                    it[messagePackage] = transmission.messagePackage.hashCode()
                    it[timestamp] = transmission.timestamp
                }
                if (updatedRowCount > 0) {
                    TransmissionNgDtoEntity(
                        id,
                        transmission.sender,
                        transmission.receiver,
                        transmission.extraInfo,
                        transmission.messagePackage.hashCode(),
                        transmission.timestamp
                    )
                } else {
                    null
                }
            }
        }
    }

    suspend fun deleteTransmission(id: Int): Boolean {
        return withContext(Dispatchers.IO) {
            transaction {
                Transmissions.deleteWhere { Transmissions.id eq id } > 0
            }
        }
    }
}
