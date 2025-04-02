package org.jesperancinha.space.dto

import arrow.core.*
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.nullable
import arrow.core.raise.zipOrAccumulate
import arrow.optics.optics
import kotlinx.serialization.Serializable
import org.jesperancinha.space.dao.LocalDateTimeSerializer
import org.jesperancinha.space.dao.NonEmptyListSerializer
import java.time.LocalDateTime


@optics
@Serializable
data class Message(
    val id: Int? = null,
    val purpose: String,
    val message: String,
    val messageBcc:String? = null,
    val messageCC: String? = null
) {

    private fun getFirstChar(input: String): Option<Char> {
        return input.firstOrNull()?.some() ?: none()
    }

    fun getInitials() = nullable {
        "${getFirstChar(purpose).bind()} - ${getFirstChar(message).bind()}"
    }
    companion object
}

@optics
@Serializable
data class MessagePackage(
    @Serializable(with = NonEmptyListSerializer::class)
    val messages: NonEmptyList<Message>,
    @Serializable(with = LocalDateTimeSerializer::class)
    val timestamp: LocalDateTime = LocalDateTime.now()
) {
    companion object
}

@optics
@Serializable
data class TransmissionNgDto(
    val id: Int? = null,
    val sender: String,
    val receiver: String,
    val extraInfo: String? = null,
    @Serializable
    val messagePackage: MessagePackage,
    @Serializable(with = LocalDateTimeSerializer::class)
    val timestamp: LocalDateTime = LocalDateTime.now()
) {
    fun validateTransmission(): Either<String, TransmissionNgDto> = either {
        ensure(sender.isNotBlank()) { "Sender cannot be blank" }
        ensure(receiver.isNotBlank()) { "Receiver cannot be blank" }
        TransmissionNgDto(id, sender, receiver, extraInfo, messagePackage, timestamp)
    }

    companion object {
        operator fun invoke(
            id: Int? = null,
            sender: String,
            receiver: String,
            extraInfo: String? = null,
            messagePackage: MessagePackage,
            timestamp: LocalDateTime = LocalDateTime.now()
        ): Either<NonEmptyList<String>, Unit> = either {
            zipOrAccumulate(
                { ensure(sender.isNotBlank()) { "Sender cannot be blank" } },
                { ensure(receiver.isNotBlank()) { "Receiver cannot be blank" } }
            ) { _, _ -> TransmissionNgDto(id, sender, receiver, extraInfo, messagePackage, timestamp) }
        }
    }
}
