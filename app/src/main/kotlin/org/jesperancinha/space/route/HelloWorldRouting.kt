package org.jesperancinha.space.route

import arrow.core.Option
import arrow.core.none
import arrow.core.raise.option
import arrow.core.some
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sse.*
import io.ktor.sse.*
import io.netty.handler.codec.DefaultHeaders
import org.jesperancinha.space.config.STMService
import org.jesperancinha.space.service.HelloService
import org.koin.ktor.ext.inject
import io.ktor.server.application.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.response.*

fun Application.configureRouting() {
    install(SSE)
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }
    }
    install(DefaultHeaders) {
        header("X-Engine", "Ktor")
    }

    val helloService by inject<HelloService>()

    routing {
        route("/basic") {
            get("/hello") {
                call.respondText("Hello, Ktor!")
            }
            post("/submit") {
                val data = call.receive<String>()
                call.respondText("Received: $data")
            }
            get("/user") {
                val id = call.parameters["id"]!!
                call.response.headers.append("X-Version", "1.0")
                call.respondText("User ID is $id")
            }
            get("/test") {
                call.respond(HttpStatusCode.Accepted, "Processing")
            }
        }
        get("/") {
            call.respondText("Hello World!")
        }
        sse("/hello") {
            send(ServerSentEvent("world"))
        }
        get("/log") {
            helloService.sayHello()
            call.respondText("Hello World!")
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


        get("/nobinding") {
            call.respond(HttpStatusCode.OK, compute())
        }
        get("/binding") {
            call.respond(HttpStatusCode.OK, computeOk())
        }
    }
}


fun compute(): Option<Int> = option {
    val a = 10.some().bind()
    val b = none<Int>().bind()
    a + b
}

fun computeOk(): Option<Int> = option {
    val a = 10.some().bind()
    val b = 20.some().bind()
    a + b
}