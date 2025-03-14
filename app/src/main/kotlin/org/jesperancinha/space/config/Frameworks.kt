package org.jesperancinha.space.config

import arrow.core.Either
import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.fx.stm.TVar
import arrow.fx.stm.atomically
import arrow.resilience.Schedule
import arrow.resilience.saga
import arrow.resilience.transact
import io.ktor.server.application.*
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import org.jesperancinha.space.config.FleetUserService.AppError.DatabaseError
import org.jesperancinha.space.config.FleetUserService.AppError.NotFound
import org.jesperancinha.space.config.FleetUserService.FleetUser
import org.jesperancinha.space.service.TransmissionService
import org.jetbrains.exposed.sql.Database
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import kotlin.random.Random


val userDatabase = mutableMapOf<Int, FleetUser?>(
    1 to FleetUser(id = 1, name = "João", email = "jesperancinha@techexample.com")
)

class FleetUserService {
    @Serializable
    data class FleetUser(
        val id: Int,
        val name: String?,
        val email: String,
        val telephone: String? = null,
        val bankAccountNumber: String? = null,
        val department: String? = null,
        val chamber: String? = null,
    )

    sealed interface AppError {
        data object NotFound : AppError
        data object DatabaseError : AppError
    }

    suspend fun getUser(id: Int): Either<AppError, FleetUser> {
        return Schedule
            .doUntil<Either<AppError, FleetUser>> { either, _ -> either.isRight() }
            .repeat {
                either {
                    delay(300)
                    val user = runCatching { fetchUser(id) }
                        .onFailure { raise(DatabaseError) }
                        .getOrNull()
                    ensure(user != null) {
                        raise(NotFound)
                    }
                    user
                }
            }
    }

    suspend fun updateUserEmail(id: Int, newEmail: String): Either<AppError, FleetUser> {
        val (result, retries) = Schedule
            .doUntil<Either<AppError, FleetUser>> { either, _ ->
                either.isRight() || either.leftOrNull() is NotFound
            }
            .and(Schedule.recurs(2))
            .repeat {
                delay(300)
                val either = either {
                    val updatedUser = runCatching { fetchUser(id) }
                        .onFailure {
                            raise(DatabaseError)
                        }
                        .getOrNull()?.copy(email = newEmail)

                    ensure(updatedUser != null) {
                        raise(NotFound)
                    }
                    updatedUser.apply { userDatabase[id] = this }
                }
                either
            }
        result.fold(
            ifLeft = { println("Failed after $retries retries") },
            ifRight = { println("Tried $retries until successfully updated!") }
        )

        return result
    }


    fun updateUser(user: FleetUser) {
        userDatabase[user.id] = user
    }

    suspend fun registerUserById(id: Int, request: FleetUser) =
        either {
            saga {
                val originalUser = extracted(id, true)
                ensure(originalUser != null) {
                    raise(NotFound)
                }
                saga({
                    val user = extracted(id, true)!!
                    println("User with $id has clearance to proceed with telephone ${request.telephone}")
                    updateUser(user.copy(telephone = request.telephone))
                }) {
                    updateUser(originalUser)
                }
                saga({
                    val user = extracted(id, true)!!
                    println("User with $id has clearance to proceed with bank account ${request.bankAccountNumber}")
                    updateUser(user.copy(bankAccountNumber = request.bankAccountNumber))
                }) {
                    updateUser(originalUser)
                }
                saga({
                    val user = extracted(id, true)!!
                    println("User with $id has clearance to proceed with department ${request.department} and chamber ${user.chamber}")
                    updateUser(
                        user.copy(
                            department = request.department,
                            chamber = request.chamber
                        )
                    )

                }) {
                    updateUser(originalUser)
                }
            }.transact()
        }

    private fun Raise<AppError>.extracted(id: Int, dontFail: Boolean = false): FleetUser? {
        val user = runCatching { fetchUser(id, dontFail) }
            .onFailure { raise(DatabaseError) }
            .getOrNull()
        ensure(user != null) {
            raise(NotFound)
        }
        return user
    }


    private fun fetchUser(id: Int, dontFail: Boolean = false) =
        Random
            .nextBoolean()
            .run {
                if (this && !dontFail) throw RuntimeException("Database connection fail!")
                else
                    userDatabase[id]
            }
}


object ApplicationBankAccount {
    var total: Long = 0

    fun addValue(total: Long) {
        this.total += total
    }
}

class STMService {

    suspend fun addStimulus(stimulus: Long = 100) {
        val stimulusSTM = TVar.new(stimulus)
        val applicationAccountSTM = TVar.new(ApplicationBankAccount.total)
        val nTransfers = stimulus / 5
        (1..nTransfers).forEach {
            transfer(stimulusSTM, applicationAccountSTM, 5L)
        }

        val newStimulusSTM = atomically { stimulusSTM.read() }
        val newApplicationAccountSTM = atomically {
            applicationAccountSTM.read()
        }
        ApplicationBankAccount.total = newApplicationAccountSTM
        println("Stimulus virtual account balance: $newStimulusSTM")
        println("Application accountBalance: $newApplicationAccountSTM")
    }

    suspend fun transfer(from: TVar<Long>, to: TVar<Long>, amount: Long) {
        atomically {
            val fromBalance = from.read()
            if (fromBalance >= amount) {
                from.write(fromBalance - amount)
                to.write(to.read() + amount + (0.001 * ApplicationBankAccount.total).toLong())
            }
        }
    }

    fun currentBalance(): Long = ApplicationBankAccount.total

}


fun Application.configureFrameworks() {
    install(Koin) {
        slf4jLogger()
        modules(module {
            single<HelloService> {
                HelloService {
                    println(environment.log.info("Hello, World!"))
                }
            }
            val database = Database.connect(
                url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;",
                driver = "org.h2.Driver",
                user = "root",
                password = ""
            )
            val transmissionService = TransmissionService(database)
            single { transmissionService }
            single<FleetUserService> {
                FleetUserService()
            }
            single<STMService> {
                STMService()
            }
        })
    }
}
