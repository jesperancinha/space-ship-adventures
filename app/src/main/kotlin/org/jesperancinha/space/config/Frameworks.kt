package org.jesperancinha.space.config

import io.ktor.server.application.*
import org.jesperancinha.space.service.TransmissionService
import org.jetbrains.exposed.sql.Database
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureFrameworks() {
    install(Koin) {
        slf4jLogger()
        modules(module {
            single<HelloService> {
                HelloService {
                    println(environment.log.info("Hello, World!"))
                }
            }
            val database = Database.connect(
                url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;",
                driver = "org.h2.Driver",
                user = "root",
                password = ""
            )
            val transmissionService = TransmissionService(database)
            single { transmissionService }
        })
    }
}
