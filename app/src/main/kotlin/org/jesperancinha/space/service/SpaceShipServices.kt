package org.jesperancinha.space.service

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.raise.either
import arrow.core.toNonEmptyListOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.jesperancinha.space.dao.MessageEntity
import org.jesperancinha.space.dao.MessagePackages
import org.jesperancinha.space.dao.Messages
import org.jesperancinha.space.dao.Transmissions
import org.jesperancinha.space.dto.Message
import org.jesperancinha.space.dto.MessagePackage
import org.jesperancinha.space.dto.TransmissionNgDto
import org.jesperancinha.space.dto.TransmissionNgDtoEntity
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

class MessageService {
    suspend fun createMessage(message: Message, packageIdValue: Int): Message {
        message.getInitials()
        return withContext(Dispatchers.IO) {
            transaction {
                val messageId = Messages
                    .insertAndGetId {
                        it[purpose] = message.purpose
                        it[Messages.message] = message.message
                        it[packageId] = packageIdValue
                    }
                Message(messageId.value, message.purpose, message.message, packageId = message.packageId)
            }
        }
    }

    suspend fun getMessages(): List<Message> {
        return withContext(Dispatchers.IO) {
            transaction {
                Messages.selectAll().map {
                    Message(it[Messages.id].value, it[Messages.purpose], it[Messages.message], packageId = it[Messages.packageId].value)
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

    suspend fun getMessagesByPackageId(id: Int): NonEmptyList<Message> {
        return withContext(Dispatchers.IO) {
            transaction {
                Messages.selectAll().where { Messages.packageId eq id }.map {
                    Message(it[Messages.id].value, it[Messages.purpose], it[Messages.message], packageId = it[Messages.packageId].value)
                }.toNonEmptyListOrNull() ?: error("List was unexpectedly empty!")
            }
        }
    }

    suspend fun getMessagePackageById(id: Int): MessagePackage {
        return withContext(Dispatchers.IO) {
            transaction {
                MessagePackages.selectAll().where { MessagePackages.id eq id }.map {
                    MessagePackage(
                        messages =
                        runBlocking { getMessagesByPackageId(id) }, timestamp = it[MessagePackages.timestamp].atZone(ZoneId.systemDefault()).toLocalDateTime()
                    )
                }.single()
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
    suspend fun createTransmission(transmission: TransmissionNgDto): Either<String, TransmissionNgDto> = either {
        transmission.validateTransmission()
            .fold({
                raise(it)
            })
            {

                withContext(Dispatchers.IO) {
                   val messagePackageId = transaction {
                        MessagePackages
                            .insertAndGetId {
                                it[timestamp] = Instant.now()
                            }
                    }
                    transaction {
                        val messagePackage = transmission.messagePackage
                        messagePackage.messages.map { message ->
                            runBlocking { messageService.createMessage(message, messagePackageId.value).id }
                        }

                        val transmissionId = Transmissions.insertAndGetId {
                            it[sender] = transmission.sender
                            it[receiver] = transmission.receiver
                            it[extraInfo] = transmission.extraInfo
                            it[Transmissions.messagePackage] = messagePackageId.value
                            it[timestamp] = transmission.timestamp.toInstant(ZoneOffset.UTC)
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
                        it[Transmissions.timestamp].atZone(ZoneId.systemDefault()).toLocalDateTime()
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
                            it[Transmissions.timestamp].atZone(ZoneId.systemDefault()).toLocalDateTime()
                        )
                    }.singleOrNull()
            }
        }
    }

    suspend fun getTransmissionByPackageId(id: Int): TransmissionNgDtoEntity? {
        return withContext(Dispatchers.IO) {
            transaction {
                Transmissions.selectAll()
                    .where { Transmissions.messagePackage eq id }
                    .map {
                        val messagePackageId = it[Transmissions.messagePackage]
                        TransmissionNgDtoEntity(
                            it[Transmissions.id].value,
                            it[Transmissions.sender],
                            it[Transmissions.receiver],
                            it[Transmissions.extraInfo],
                            messagePackageId.value,
                            it[Transmissions.timestamp].atZone(ZoneId.systemDefault()).toLocalDateTime()
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
                    it[timestamp] = transmission.timestamp.toInstant(ZoneOffset.UTC)
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
