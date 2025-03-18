package org.jesperancinha.space.config

import arrow.fx.stm.atomically
import io.kotest.common.runBlocking
import io.kotest.matchers.shouldBe
import io.ktor.server.testing.*
import org.jesperancinha.space.module
import org.junit.Before
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.test.KoinTest
import org.koin.test.inject

class DockingServiceTest : KoinTest {
    private val dockingService: DockingService by inject()

    @Test
    fun requestDocking() = testApplication {
        application {
            module()
        }
        startApplication()
        dockingService.requestDocking("25th of April")
        dockingService.requestDocking("Vasco da Gama")
    }

    @Test
    fun refuel() = testApplication {
        application {
            module()
        }
        startApplication()
        dockingService.refuel("25th of April", 50)
        dockingService.refuel("Vasco da Gama", 100)

        dockingService.fuelStation.unsafeRead().fuel shouldBe 50
        atomically {
            dockingService.fuelStation.write(FuelStation(100))
        }
    }

    @Test
    fun refuelWithRollback() = testApplication {
        application {
            module()
        }
        startApplication()
        dockingService.refuelWithRollback("25th of April", 50)
        dockingService.refuelWithRollback("Vasco da Gama", 100)
        dockingService.refuelWithRollback("Vasco da Gama", 50)
        dockingService.fuelStation.unsafeRead().fuel shouldBe 0
        atomically {
            dockingService.fuelStation.write(FuelStation(100))
        }
    }
}