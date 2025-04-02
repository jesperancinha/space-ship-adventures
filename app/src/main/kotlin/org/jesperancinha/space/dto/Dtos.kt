package org.jesperancinha.space.dto

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

data class Message(
    val id: Int,
    val purpose: String,
    val message: String
)
data class MessagePackage(
    val messages: List<Message>,
    @Serializable(with = LocalDateTimeSerializer::class)
    val timestamp: LocalDateTime
)

data class TransmissionNgDto(
    val id: Int,
    val sender: String,
    val receiver: String,
    @Serializable(with = LocalDateTimeSerializer::class)
    val timestamp: LocalDateTime
)