package org.jesperancinha.space.dto

import arrow.core.Option
import arrow.core.nonEmptyListOf
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.types.shouldBeTypeOf
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class TransmissionNgDtoTest {
    @Test
    fun `should invalidate transmission and accumulate errors`() {
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
        timestamp =LocalDateTime.now()
    )
        val validateTransmission = TransmissionNgDto.validateTransmission(
            transmissionNgDto.id,
            transmissionNgDto.sender,
            transmissionNgDto.receiver,
            transmissionNgDto.extraInfo,
            transmissionNgDto.messagePackage,
            transmissionNgDto.timestamp,
        )
        validateTransmission.isLeft().shouldBeTrue()
        validateTransmission.leftOrNull().shouldNotBeNull()
            .shouldBeTypeOf<String>()
    }
}