package org.jesperancinha.space.route

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.runBlocking
import org.jesperancinha.space.model.TransmissionRepository

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }

    routing {
        route("/transmissions") {
            get {
                val transmissions = runBlocking { TransmissionRepository.getTransmissions() }
                call.respond(transmissions)
            }

            post {
                val request = call.receive<Map<String, String>>()
                val sender = request["sender"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing sender")
                val receiver = request["receiver"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing receiver")
                val message = request["message"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing message")

                val transmission = runBlocking {
                    TransmissionRepository.sendTransmission(sender, receiver, message)
                }

                call.respond(transmission)
            }
        }
    }
}
