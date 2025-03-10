package org.jesperancinha.space.org.jesperancinha.space

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import org.jesperancinha.space.config.configureFrameworks

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }
    configureSerialization()
    configureSecurity()
    configureAdministration()
    configureFrameworks()
//    configureMonitoring()
    configureHTTP()
    configureDatabases()
    configureRouting()
}
