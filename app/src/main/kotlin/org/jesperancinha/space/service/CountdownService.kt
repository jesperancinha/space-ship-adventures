package org.jesperancinha.space.service

import arrow.fx.coroutines.CountDownLatch
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CountdownService {

    suspend fun countdown(latch: CountDownLatch) {
        for (i in 5 downTo 1) {
            println("🚀 Launch in $i...")
            delay(1000)
            latch.countDown()
        }
    }

    suspend fun performCountdown() = coroutineScope {
        val latch = CountDownLatch(5)
        launch { countdown(latch) }
        latch.await()
        println("🚀 Liftoff!")
    }

}