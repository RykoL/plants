package de.rlang.plants.domain

enum class Interval {
    DAYS,
    WEEKS,
}

data class WateringInterval(val number: Int, val interval: Interval) {
    fun asKey() = "label.${this.interval.toString().lowercase()}"
}
