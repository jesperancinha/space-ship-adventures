package org.jesperancinha.space.dao

import kotlinx.serialization.Serializable

@Serializable
data class City(val name: String, val population: Int)
