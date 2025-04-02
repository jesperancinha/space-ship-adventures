package org.jesperancinha.space.dto

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.Option
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.optics.optics
import kotlinx.serialization.Serializable
import org.jesperancinha.space.dao.LocalDateTimeSerializer
import java.time.LocalDateTime

@Serializable
data class TransmissionDto(
    val id: Int,
    val sender: String,
    val receiver: String,
    val message: String,
    @Serializable(with = LocalDateTimeSerializer::class)
    val timestamp: LocalDateTime
)

@optics
data class Message(
    val id: Int,
    val purpose: String,
    val message: String
)

@optics
data class MessagePackage(
    val messages: NonEmptyList<Message>,
    @Serializable(with = LocalDateTimeSerializer::class)
    val timestamp: LocalDateTime
)

@optics
data class TransmissionNgDto(
    val id: Int,
    val sender: String,
    val receiver: String,
    val messagePackage: MessagePackage,
    @Serializable(with = LocalDateTimeSerializer::class)
    val timestamp: Option<LocalDateTime>
) {
    companion object {
        fun validateTransmission(
            id: Int,
            sender: String,
            receiver: String,
            timestamp: Option<LocalDateTime>,
            messagePackage: MessagePackage,
        ): Either<String, TransmissionNgDto> {
            return either {
                ensure(sender.isNotBlank()) { "Sender cannot be blank" }
                ensure(receiver.isNotBlank()) { "Receiver cannot be blank" }
                TransmissionNgDto(id, sender, receiver, messagePackage, timestamp)
            }
        }
    }
}