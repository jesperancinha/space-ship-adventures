package org.jesperancinha.space.org.jesperancinha.space

import io.ktor.server.application.*
import io.ktor.server.engine.*

fun Application.configureAdministration() {
    install(ShutDownUrl.ApplicationCallPlugin) {
        shutDownUrl = "/ktor/application/shutdown"
        exitCodeSupplier = { 0 }
    }
}
