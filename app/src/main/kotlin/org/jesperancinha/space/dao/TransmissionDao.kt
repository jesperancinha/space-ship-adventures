package org.jesperancinha.space.dao

import arrow.core.NonEmptyList
import arrow.core.toNonEmptyListOrNull
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.PrimitiveKind.STRING
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.jesperancinha.space.dto.TransmissionDto
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
    object TransmissionsOld : IntIdTable() {
        val sender = varchar("sender", 50)
        val receiver = varchar("receiver", 50)
        val message = text("message")
        val timestamp = datetime("timestamp").clientDefault { LocalDateTime.now() }
    }

    init {
        transaction(database) {
            SchemaUtils.create(TransmissionsOld)
        }

    }

    suspend fun sendTransmission(sender: String, receiver: String, message: String): TransmissionDto =
        newSuspendedTransaction {
            val id = TransmissionsOld.insertAndGetId {
                it[TransmissionsOld.sender] = sender
                it[TransmissionsOld.receiver] = receiver
                it[TransmissionsOld.message] = message
            }.value

            return@newSuspendedTransaction TransmissionDto(id, sender, receiver, message, LocalDateTime.now())
        }

    suspend fun getTransmissions(): List<TransmissionDto> =
        newSuspendedTransaction {
            TransmissionsOld.selectAll().map {
                TransmissionDto(
                    it[TransmissionsOld.id].value,
                    it[TransmissionsOld.sender],
                    it[TransmissionsOld.receiver],
                    it[TransmissionsOld.message],
                    it[TransmissionsOld.timestamp]
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

class NonEmptyListSerializer<T>(private val dataSerializer: KSerializer<T>) : KSerializer<NonEmptyList<T>> {
    override val descriptor: SerialDescriptor =
        ListSerializer(dataSerializer).descriptor

    override fun serialize(encoder: Encoder, value: NonEmptyList<T>) {
        ListSerializer(dataSerializer).serialize(encoder, value.toList())
    }

    override fun deserialize(decoder: Decoder): NonEmptyList<T> {
        val list = ListSerializer(dataSerializer).deserialize(decoder)
        return list.toNonEmptyListOrNull() ?: throw IllegalArgumentException("List cannot be empty")
    }
}