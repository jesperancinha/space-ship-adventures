package org.jesperancinha.space.dto

import arrow.core.nonEmptyListOf
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class TransmissionNgDtoTest {
    @Test
    fun `should invalidate transmission and show one error`() {
        val transmissionNgDto = TransmissionNgDto(
            id = 1,
            sender = "",
            receiver = "",
            messagePackage = MessagePackage(
                messages = nonEmptyListOf(
                    Message(
                        id = 1,
                        purpose = "purpose",
                        message = "message"
                    )
                ),
                timestamp = LocalDateTime.now()
            ),
            timestamp = LocalDateTime.now()
        )
        val validateTransmission = transmissionNgDto.validateTransmission()
        validateTransmission.isLeft().shouldBeTrue()
        validateTransmission.leftOrNull().shouldNotBeNull()
            .shouldBeTypeOf<String>()
    }

    @Test
    fun `should invalidate transmission and accumulate errors`() {
        val eitherTransmissionNgDto = TransmissionNgDto. invoke(
            id = 1,
            sender = "",
            receiver = "",
            messagePackage = MessagePackage(
                messages = nonEmptyListOf(
                    Message(
                        id = 1,
                        purpose = "purpose",
                        message = "message"
                    )
                ),
                timestamp = LocalDateTime.now()
            ),
            timestamp = LocalDateTime.now()
        )
        eitherTransmissionNgDto.leftOrNull()
            .shouldNotBeNull()
            .shouldHaveSize(2)
    }
    @Test
    fun `should validate transmission and not accumulate errors`() {
        val transmissionNgDto = TransmissionNgDto. invoke(
            id = 1,
            sender = "wow",
            receiver = "wow2",
            messagePackage = MessagePackage(
                messages = nonEmptyListOf(
                    Message(
                        id = 1,
                        purpose = "purpose",
                        message = "message"
                    )
                ),
                timestamp = LocalDateTime.now()
            ),
            timestamp = LocalDateTime.now()
        )
        transmissionNgDto.getOrNull()
            .shouldNotBeNull()
            .should {
                it.sender shouldBe "wow"
                it.receiver shouldBe "wow2"
            }
    }
}