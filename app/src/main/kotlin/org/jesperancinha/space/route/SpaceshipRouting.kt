package org.jesperancinha.space.route

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jesperancinha.space.config.FleetUserService
import org.jesperancinha.space.config.FleetUserService.FleetUser
import org.jesperancinha.space.config.STMService
import org.jesperancinha.space.dto.TransmissionNgDto
import org.jesperancinha.space.service.Transmission
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
    routing {
        get("/users") {
            call.respond(HttpStatusCode.OK, "ok")
        }
        get("/message") {
            call.respond(HttpStatusCode.OK, "ok")
        }
        post("/transmission") {
            val request = call.receive<TransmissionNgDto>()
            println(request)
            call.respond(HttpStatusCode.OK, "ok")
        }
    }

}