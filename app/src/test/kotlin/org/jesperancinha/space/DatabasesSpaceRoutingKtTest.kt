package org.jesperancinha.space

import io.ktor.client.request.*
import io.ktor.server.testing.*
import org.jesperancinha.space.route.configureDatabases
import kotlin.test.Test

class DatabasesSpaceRoutingKtTest {

    @Test
    fun testPostCities() = testApplication {
        application {
            configureDatabases()
        }
        client.post("/cities").apply {
        }
    }
}