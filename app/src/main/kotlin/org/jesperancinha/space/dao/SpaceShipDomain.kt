package org.jesperancinha.space.dao

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object Messages : IntIdTable() {
    val purpose = varchar("purpose", 255)
    val message = varchar("message", 255)
    val packageId = reference("messagePackage", MessagePackages)
}

object MessagePackages : IntIdTable() {
    val timestamp = datetime("timestamp")
}

object Transmissions : IntIdTable() {
    val sender = varchar("sender", 255)
    val receiver = varchar("receiver", 255)
    val extraInfo = varchar("extraInfo", 255).nullable()
    val messagePackage = reference("messagePackage", MessagePackages)
    val timestamp = datetime("timestamp")
}

data class MessageEntity(val id: Int?, val purpose: String, val message: String)

@Serializable
data class TransmissionNgDtoEntity(
    val id: Int?,
    val sender: String,
    val receiver: String,
    val extraInfo: String?,
    val messagePackageId: Int,
    @Serializable(with = LocalDateTimeSerializer::class)
    val timestamp: LocalDateTime
)