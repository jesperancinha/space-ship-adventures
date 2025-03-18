package org.jesperancinha.space

import arrow.fx.stm.TVar
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.jesperancinha.space.config.DockingBay
import org.jesperancinha.space.config.configureFrameworks
import org.jesperancinha.space.route.*

fun main(args: Array<String>) {
    EngineMain.main(args)
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
    configureSpecialRouting()
    configureSTMRouting()
    configureTransmissions()
}
