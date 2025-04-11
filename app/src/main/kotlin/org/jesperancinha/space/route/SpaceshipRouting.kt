package org.jesperancinha.space.route

import arrow.core.raise.nullable
import arrow.core.toNonEmptyListOrNull
import arrow.fx.coroutines.parZip
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jesperancinha.space.config.FleetUserService
import org.jesperancinha.space.config.FleetUserService.FleetUser
import org.jesperancinha.space.dao.MessagePackages
import org.jesperancinha.space.dao.Messages
import org.jesperancinha.space.dao.Transmissions
import org.jesperancinha.space.dto.*
import org.jesperancinha.space.service.MessageService
import org.jesperancinha.space.service.TransmissionService
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.ktor.ext.inject
import org.slf4j.event.Level.DEBUG

fun Application.configureSpecialRouting() {

    val userService: FleetUserService by inject()
    routing {
        route("/simple") {
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
                nullable {
                    val request = call.receive<FleetUser>()
                    val registerUserById = userService.registerUserById(id.bind(), request)
                    registerUserById.fold(
                        ifLeft = { call.respond(HttpStatusCode.InternalServerError, "Update not possible!") },
                        ifRight = { _ -> call.respond("Successfully registered user with id = $id") }
                    )
                }
            }
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
    val messagesLens = TransmissionNgDto.messagePackage.messages
    val messagePackageLens = TransmissionNgDto.messagePackage
    install(CallLogging) {
        level = DEBUG
    }

    routing {

        route("/pieces") {
            post("/messages") {
                val transmission = call.receive<TransmissionNgDto>()
                call.respond(HttpStatusCode.OK, messagesLens.get(transmission))
            }
            post("/phantom") {
                val transmission = call.receive<TransmissionNgDto>()
                val modified = messagesLens.set(
                    transmission, listOf(
                        Message(
                            id = 0,
                            purpose = "phantom purpose",
                            message = "phantom message",
                            packageId = 0
                        )
                    ).toNonEmptyListOrNull() ?: throw RuntimeException("Invalid phantom")
                )
                call.respond(HttpStatusCode.OK, messagesLens.get(modified))
            }
            post("/package") {
                val transmission = call.receive<TransmissionNgDto>()
                call.respond(HttpStatusCode.OK, messagePackageLens.get(transmission))
            }
            post("/retransmissions") {
                val transmission = call.receive<TransmissionNgDto>()
                messagesLens.get(transmission)
                    .forEach {
                        nullable {
                            it.messageCC.bind()
                            it.messageBcc.bind()
                            println(it)
                        } ?: println(null)
                    }
                call.respond(HttpStatusCode.OK, messagePackageLens.get(transmission))
            }
        }
        route("/messages") {
            get {
                val messages = messageService.getMessages()
                call.respond(messages)
            }
            get("/purposes") {
                val messages = messageService.getMessages()
                val senderMessageDetails = messages.mapNotNull {
                    nullable {
                        parZip({
                            transmissionService.getTransmissionByPackageId(it.packageId.bind()).bind().sender
                        }, {
                            messageService.getMessagePackageById(it.packageId.bind()).timestamp
                        }) { sender, timestamp ->
                            SenderMessageDetail(sender, timestamp)
                        }
                    }
                }
                call.respond(senderMessageDetails)
            }

        }
        route("/transmissions") {
            get {
                val transmissions = transmissionService.getTransmissions()
                call.respond(transmissions)
            }

            get("/full") {
                val transmissions = transmissionService.getTransmissions()
                    .map {
                        TransmissionNgDto(
                            id = it.id,
                            sender = it.sender,
                            receiver = it.receiver,
                            extraInfo = it.extraInfo,
                            messagePackage = messageService.getMessagePackageById(it.messagePackageId),
                            timestamp = it.timestamp
                        )
                    }
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
        }
    }
}