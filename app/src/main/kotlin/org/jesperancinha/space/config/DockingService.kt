package org.jesperancinha.space.config

import arrow.fx.stm.TVar
import arrow.fx.stm.atomically

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
            val station = fuelStation.read()
            if (station.fuel >= requestedFuel) {
                println("$spaceship is refueling with $requestedFuel units...")
                fuelStation.write(FuelStation(station.fuel - requestedFuel))
                println("$spaceship successfully refueled! Remaining fuel: ${station.fuel - requestedFuel}")
            } else {
                println("$spaceship cannot refuel, not enough fuel available!")
            }
        }
    }
    suspend fun refuelWithRollback(spaceship: String, requestedFuel: Int) {
        try {
            atomically {
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