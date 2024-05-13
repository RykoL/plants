package de.rlang.plants.domain.request

import de.rlang.plants.domain.Interval

class NewPlantRequest(
    val name: String,
    val location: String,
    val wateringIntervalValue: Int,
    val wateringIntervalUnit: Interval,
) {
    companion object {
        fun empty() = NewPlantRequest("",  "", 1, Interval.WEEKS)
    }
}

