package org.jesperancinha.space

import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSerialization()
    configureSecurity()
    configureAdministration()
    configureFrameworks()
    configureMonitoring()
    configureHTTP()
    configureDatabases()
    configureRouting()
}
