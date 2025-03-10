package org.jesperancinha.space.model

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.LocalDateTime
import org.jetbrains.exposed.sql.insertAndGetId



object Transmissions : IntIdTable() {
    val sender = varchar("sender", 50)
    val receiver = varchar("receiver", 50)
    val message = text("message")
    val timestamp = datetime("timestamp").clientDefault { LocalDateTime.now() }
}

fun initDatabase() {
    Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")

    transaction {
        SchemaUtils.create(Transmissions)
    }
}
data class TransmissionDTO(val id: Int, val sender: String, val receiver: String, val message: String, val timestamp: LocalDateTime)

object TransmissionRepository {
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
