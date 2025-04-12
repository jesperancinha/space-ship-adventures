package org.jesperancinha.space.service

import arrow.core.toNonEmptyListOrNull
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ErrorHandlingServiceTest {

    @Test
    fun `should create spaceship`() {
        val spaceship = Spaceship("Andromeda", listOf("Kirk"))
        spaceship.isRight().shouldBeTrue()
        spaceship.getOrNull()
            .shouldNotBeNull()
            .should {
                it.title.shouldBe("Andromeda")
                it.crew
                    .shouldNotBeNull()
                    .shouldHaveSize(1)
                    .first().shouldBe(Passenger("Kirk"))
            }
    }

    @Test
    fun `should fail create spaceship with empty name`() {
        val spaceship = Spaceship("", listOf("Kirk"))
        spaceship.isLeft().shouldBeTrue()
        spaceship.leftOrNull() shouldBe EmptySpaceshipName
    }

    @Test
    fun `should fail create spaceship with no crew`() {
        val spaceship = Spaceship("Andromeda", emptyList())
        spaceship.isLeft().shouldBeTrue()
        spaceship.leftOrNull() shouldBe NoCrew
    }

    @Test
    fun testEmptyList() {
        val list = emptyList<String>().toNonEmptyListOrNull()
        list.shouldBeNull()
    }

}