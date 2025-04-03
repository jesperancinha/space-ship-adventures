package org.jesperancinha.space.route

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sse.*
import io.ktor.sse.*
import org.jesperancinha.space.config.STMService
import org.jesperancinha.space.service.HelloService
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    install(SSE)
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause" , status = HttpStatusCode.InternalServerError)
        }
    }

    val helloService by inject<HelloService>()
    routing {
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
    }
}