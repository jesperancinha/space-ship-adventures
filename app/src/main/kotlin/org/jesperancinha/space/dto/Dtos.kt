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
