package org.jesperancinha.space.service

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import arrow.core.toNonEmptyListOrNull

data class Passenger(val name: String)
sealed interface SpaceshipValidationError
object EmptySpaceshipName : SpaceshipValidationError
object NoCrew : SpaceshipValidationError
data class Spaceship internal constructor(
    val title: String, val crew: NonEmptyList<Passenger>?
) {
    companion object {
        operator fun invoke(
            title: String, crew: Iterable<String>
        ): Either<SpaceshipValidationError, Spaceship> = either {
            ensure(title.isNotEmpty()) { EmptySpaceshipName }
            ensureNotNull(crew.toNonEmptyListOrNull()) { NoCrew }
            Spaceship(title, crew.map { Passenger(it) }.toNonEmptyListOrNull())
        }
    }
}

class ErrorHandlingService {


}