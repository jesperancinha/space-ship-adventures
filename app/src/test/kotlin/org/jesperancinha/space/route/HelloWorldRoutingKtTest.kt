package org.jesperancinha.space.route

import arrow.core.Option
import arrow.core.none
import arrow.core.some
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

class HelloWorldRoutingKtTest {

    @Test
    fun `should compute`() {
        compute() shouldBe none()
    }

    @Test
    fun `should computeOk`() {
        computeOk() shouldBe 30.some()
    }
}