package org.jesperancinha.space.service

import arrow.fx.coroutines.CyclicBarrier
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlin.random.Random
import kotlin.random.Random.Default.nextLong

class FormationService {
    suspend fun spaceshipReady(barrier: CyclicBarrier, id: Int) {
        delay(nextLong(500, 2000)) // Random prep time
        println("🛸 Spaceship $id is ready for formation.")
        barrier.await()
        println("🛸 Spaceship $id enters formation.")
    }

   suspend fun startFormation() = coroutineScope {
        val totalShips = 3
        val barrier = CyclicBarrier(totalShips)

        val fleet = (1..totalShips).map { id ->
            async { spaceshipReady(barrier, id) }
        }
        fleet.awaitAll()
    }
}