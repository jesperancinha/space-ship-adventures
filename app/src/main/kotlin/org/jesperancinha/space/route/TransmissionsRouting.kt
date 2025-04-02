package org.jesperancinha.space.route

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import org.jesperancinha.space.dao.TransmissionService
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.scope

fun Application.configureTransmissions() {

    val transmissionService by inject<TransmissionService>()

    routing {

        route("/transmissions/old") {

            webSocket("/socket") {
                val transmissionService2 = call.scope.getOrNull<TransmissionService>()
                println(transmissionService2?.getTransmissions())
            }

            get {
                val transmissionService2 = call.scope.getOrNull<TransmissionService>()
                val transmissions = transmissionService.getTransmissions()
                println(transmissionService2?.getTransmissions())
                call.respond(transmissions)
            }

            post {
                val request = call.receive<Map<String, String>>()
                val sender = request["sender"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing sender")
                val receiver =
                    request["receiver"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing receiver")
                val message =
                    request["message"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing message")

                call.respond(transmissionService.sendTransmission(sender, receiver, message))
            }
        }
    }
}
