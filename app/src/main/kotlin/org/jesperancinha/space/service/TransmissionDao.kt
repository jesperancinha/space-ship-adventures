package org.jesperancinha.space.service

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind.STRING
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME


class TransmissionService(database: Database) {
    object Transmissions : IntIdTable() {
        val sender = varchar("sender", 50)
        val receiver = varchar("receiver", 50)
        val message = text("message")
        val timestamp = datetime("timestamp").clientDefault { LocalDateTime.now() }
    }

    init {
        transaction(database) {
            SchemaUtils.create(Transmissions)
        }

    }

    @Serializable
    data class TransmissionDTO(
        val id: Int,
        val sender: String,
        val receiver: String,
        val message: String,
        @Serializable(with = LocalDateTimeSerializer::class)
        val timestamp: LocalDateTime
    )


    suspend fun sendTransmission(sender: String, receiver: String, message: String): TransmissionDTO =
        newSuspendedTransaction {
            val id = Transmissions.insertAndGetId {
                it[Transmissions.sender] = sender
                it[Transmissions.receiver] = receiver
                it[Transmissions.message] = message
            }.value

            return@newSuspendedTransaction TransmissionDTO(id, sender, receiver, message, LocalDateTime.now())
        }

    suspend fun getTransmissions(): List<TransmissionDTO> =
        newSuspendedTransaction {
            Transmissions.selectAll().map {
                TransmissionDTO(
                    it[Transmissions.id].value,
                    it[Transmissions.sender],
                    it[Transmissions.receiver],
                    it[Transmissions.message],
                    it[Transmissions.timestamp]
                )
            }
        }
}

object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    private val formatter = ISO_LOCAL_DATE_TIME

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalDateTime", STRING)

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        encoder.encodeString(value.format(formatter))
    }

    override fun deserialize(decoder: Decoder): LocalDateTime {
        return LocalDateTime.parse(decoder.decodeString(), formatter)
    }
}