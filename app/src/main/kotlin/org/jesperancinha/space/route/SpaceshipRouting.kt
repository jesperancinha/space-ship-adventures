package org.jesperancinha.space.route

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jesperancinha.space.config.FleetUserService
import org.jesperancinha.space.config.FleetUserService.FleetUser
import org.jesperancinha.space.config.STMService
import org.jesperancinha.space.dao.MessagePackages
import org.jesperancinha.space.dao.Messages
import org.jesperancinha.space.dao.Transmissions
import org.jesperancinha.space.dto.Message
import org.jesperancinha.space.dto.TransmissionNgDto
import org.jesperancinha.space.service.MessageService
import org.jesperancinha.space.service.TransmissionService
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.ktor.ext.inject

fun Application.configureSpecialRouting() {

    val userService: FleetUserService by inject()
    routing {
        get("/users/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            id?.let {
                userService.getUser(it).fold(
                    ifLeft = { call.respond(HttpStatusCode.NotFound, "User not found") },
                    ifRight = { user -> call.respond(user) }
                )
            } ?: call.respond(HttpStatusCode.BadRequest, "Invalid user ID")
        }

        put("/users/{id}/email") {
            val id = call.parameters["id"]?.toIntOrNull()
            val request = call.receive<FleetUser>()
            id?.let {
                userService.updateUserEmail(it, request.email).fold(
                    ifLeft = { error ->
                        when (error) {
                            is FleetUserService.AppError.DatabaseError -> call.respond(
                                HttpStatusCode.InternalServerError,
                                "Error"
                            )

                            is FleetUserService.AppError.NotFound -> call.respond(
                                HttpStatusCode.NotFound,
                                "Error"
                            )
                        }
                        call.respond(HttpStatusCode.NotFound, "User not found")
                    },
                    ifRight = { user -> call.respond(user) }
                )
            } ?: call.respond(HttpStatusCode.BadRequest, "Invalid user ID")
        }

        put("/users/{id}/register") {
            val id = call.parameters["id"]?.toIntOrNull()
            id?.let {
                val request = call.receive<FleetUser>()
                val registerUserById = userService.registerUserById(id, request)
                registerUserById.fold(
                    ifLeft = { call.respond(HttpStatusCode.InternalServerError, "Update not possible!") },
                    ifRight = { _ -> call.respond("Successfully registered user with id = $id") }
                )
            }
        }
    }
}


fun Application.configureSTMRouting() {

    val stmService: STMService by inject()

    routing {
        get("/account") {
            call.respond(HttpStatusCode.OK, stmService.currentBalance())
        }
        put("/account") {
            stmService.addStimulus()
            call.respond(HttpStatusCode.OK, stmService.currentBalance())

        }
    }
}

fun Application.configureSpaceRouting() {

    Database.connect(
        url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
        user = "root",
        driver = "org.h2.Driver",
        password = "",
    )
    transaction { SchemaUtils.create(Messages, MessagePackages, Transmissions) }

    val messageService = MessageService()
    val transmissionService = TransmissionService(messageService)


    routing {
        get("/users") {
            call.respond(HttpStatusCode.OK, "ok")
        }
        get("/message") {
            call.respond(HttpStatusCode.OK, "ok")
        }
        route("/messages") {
            get {
                val messages = messageService.getMessages()
                call.respond(messages)
            }

            post {
                val message = call.receive<Message>()
                val createdMessage = messageService.createMessage(message)
                call.respond(HttpStatusCode.Created, createdMessage)
            }
        }
        route("/transmissions") {
            get {
                val transmissions = transmissionService.getTransmissions()
                call.respond(transmissions)
            }

            post {
                val transmission = call.receive<TransmissionNgDto>()
                transmissionService.createTransmission(transmission)
                    .fold({
                        call.respond(HttpStatusCode.InternalServerError, it)
                    }) {
                        call.respond(HttpStatusCode.Created, it)
                    }
            }

            post("/ensemble") {
                val transmission = call.receive<TransmissionNgDto>()

            }
        }
    }
}