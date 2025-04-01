package org.jesperancinha.space.service

import arrow.core.*

sealed interface Transmission

data class TextMessage(val sender: String, val content: String) : Transmission
data class Coordinates(val x: Double, val y: Double, val z: Double) : Transmission
data class DistressSignal(val level: Int) : Transmission
sealed interface TransmissionError
data object CorruptSignal : TransmissionError
data object UnknownFormat : TransmissionError

data class TransmissionData(val sender: String, val message: String)

sealed interface ValidationError
data object EmptySender : ValidationError
data object EmptyMessage : ValidationError

class MessageService {

    fun decodeTransmission(rawData: String): Option<Transmission> =
        when {
            rawData.startsWith("MSG:") -> TextMessage("Unknown", rawData.removePrefix("MSG:")).some()
            rawData.startsWith("COORD:") -> {
                val parts = rawData.removePrefix("COORD:").split(",").mapNotNull { it.toDoubleOrNull() }
                if (parts.size == 3) Coordinates(parts[0], parts[1], parts[2]).some() else None
            }
            rawData == "SOS" -> DistressSignal(10).some()
            else -> None
        }


    fun parseTransmission(rawData: String): Either<TransmissionError, Transmission> =
        when {
            rawData.contains("###") -> CorruptSignal.left()
            rawData.startsWith("MSG:") -> TextMessage("Unknown", rawData.removePrefix("MSG:")).right()
            else -> UnknownFormat.left()
        }

    data class TransmissionData(val sender: String, val message: String)

    sealed interface ValidationError
    data object EmptySender : ValidationError
    data object EmptyMessage : ValidationError

    fun processTransmission(rawData: String): Either<String, String> =
        parseTransmission(rawData)
            .map { "Processed: $it" }
            .mapLeft { "Error: $it" }
}