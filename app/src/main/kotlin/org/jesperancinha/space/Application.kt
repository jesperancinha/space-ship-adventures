package org.jesperancinha.space

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.websocket.*
import org.jesperancinha.space.config.configureFrameworks
import org.jesperancinha.space.route.*

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }
    install(WebSockets)
    configureSerialization()
    configureSecurity()
    configureAdministration()
    configureFrameworks()
//    configureMonitoring()
    configureHTTP()
    configureDatabases()
    configureRouting()
    configureSpecialRouting()
    configureSTMRouting()
    configureTransmissions()
}
