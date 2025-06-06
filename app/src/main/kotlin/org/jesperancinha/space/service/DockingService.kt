package org.jesperancinha.space.service

import arrow.fx.stm.TVar
import arrow.fx.stm.atomically
import arrow.fx.stm.stm

data class DockingBay(var occupied: Boolean = false)
data class FuelStation(var fuel: Int)

class DockingService {
    lateinit var dockingBay: TVar<DockingBay>
    lateinit var fuelStation: TVar<FuelStation>

    suspend fun initialize() {
        dockingBay = TVar.new(DockingBay())
        fuelStation = TVar.new(FuelStation(100))
    }

    suspend fun requestDocking(spaceship: String) {
        atomically {
            val bay = dockingBay.read()
            if (!bay.occupied) {
                println("$spaceship is docking...")
                dockingBay.write(DockingBay(occupied = true))
                println("$spaceship has successfully docked!")
            } else {
                println("$spaceship must wait. Docking bay is occupied.")
            }
        }
    }

    suspend fun refuel(spaceship: String, requestedFuel: Int) {
        atomically {
            stm {
                val station = fuelStation.read()
                if (station.fuel >= requestedFuel) {
                    println("$spaceship is refueling with $requestedFuel units...")
                    fuelStation.write(FuelStation(station.fuel - requestedFuel))
                    println("$spaceship successfully refueled! Remaining fuel: ${station.fuel - requestedFuel}")
                } else {
                    println("$spaceship cannot refuel, not enough fuel available!")
                }
            } orElse {}
        }
    }
    suspend fun refuelWithRollback(spaceship: String, requestedFuel: Int) {
        try {
            atomically {
                print("----- Start of atomic transaction\n")
                val station = fuelStation.read()
                if (station.fuel >= requestedFuel) {
                    println("$spaceship attempting to refuel with $requestedFuel units...")
                    fuelStation.write(FuelStation(station.fuel - requestedFuel))
                    if (requestedFuel == 20) {
                        throw IllegalStateException("$spaceship encountered a system failure!")
                    }
                    println("$spaceship successfully refueled! Remaining fuel: ${station.fuel - requestedFuel}")
                } else {
                    println("$spaceship cannot refuel, not enough fuel available!")
                }
            }
        } catch (e: IllegalStateException) {
            println("⚠️ Transaction failed for $spaceship: ${e.message}")
            println("$spaceship will retry later...")
        }
    }
}