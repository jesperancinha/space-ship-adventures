package org.jesperancinha.space.dto

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class MessageTest {

    @Test
    fun `should validate message to null if empty purpose`() {
        val message = Message(
            id = -1,
            purpose = "",
            message = "No purpose, no message"
        )
        message.getInitials().shouldBeNull()
    }

    @Test
    fun `should validate message to full message if purpose`() {
        val message = Message(
            id = -1,
            purpose = "purpose",
            message = "No purpose, no message"
        )
        message.getInitials().shouldNotBeNull() shouldBe "p - N"
    }
}