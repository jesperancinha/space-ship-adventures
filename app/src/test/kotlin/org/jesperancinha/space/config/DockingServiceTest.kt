package org.jesperancinha.space.config

import arrow.fx.stm.atomically
import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.shouldBe
import io.ktor.server.testing.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jesperancinha.space.module
import org.junit.jupiter.api.Test
import org.koin.test.KoinTest
import org.koin.test.inject

class DockingServiceTest : KoinTest {
    private val dockingService: DockingService by inject()

    @Test
    fun `should succeed in requesting docking`() = testApplication {
        application {
            module()
        }
        startApplication()
        CoroutineScope(Dispatchers.IO)
            .launch {
                val job1 = launch {
                    dockingService.requestDocking("25th of April")
                }
                val job2 = launch {
                    dockingService.requestDocking("Vasco da Gama")
                }
                job1.join()
                job2.join()
            }.join()
    }

    @Test
    fun `should succeed in refuelling while rejecting another spaceship`() = testApplication {
        application {
            module()
        }
        startApplication()
        CoroutineScope(Dispatchers.IO)
            .launch {
                val job1 = launch {
                    dockingService.refuel("25th of April", 50)
                }
                val job2 = launch {
                    dockingService.refuel("Vasco da Gama", 100)
                }
                job1.join()
                job2.join()
            }.join()

        dockingService.fuelStation.unsafeRead().fuel.shouldBeIn(0,50)
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
        CoroutineScope(Dispatchers.IO)
            .launch {
                val job1 = launch {
                    dockingService.refuelWithRollback("25th of April", 50)
                }
                val job2 = launch {
                    dockingService.refuelWithRollback("Vasco da Gama", 100)
                }
                val job3 = launch {
                    dockingService.refuelWithRollback("Vasco da Gama", 50)
                }
                job1.join()
                job2.join()
                job3.join()
            }.join()
        dockingService.fuelStation.unsafeRead().fuel shouldBe 0
        atomically {
            dockingService.fuelStation.write(FuelStation(100))
        }
    }

    @Test
    fun refuelWithRollback2() = testApplication {
        application {
            module()
        }
        startApplication()
        CoroutineScope(Dispatchers.IO)
            .launch {
                val job1 = launch {
                    dockingService.refuelWithRollback("25th of April", 30)
                }
                val job2 = launch {
                    dockingService.refuelWithRollback("Vasco da Gama", 20)
                }
                job1.join()
                job2.join()
            }.join()
        dockingService.fuelStation.unsafeRead().fuel shouldBe 70
        atomically {
            dockingService.fuelStation.write(FuelStation(100))
        }
    }
}