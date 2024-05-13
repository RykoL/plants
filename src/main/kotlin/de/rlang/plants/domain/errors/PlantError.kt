package de.rlang.plants.domain.errors

sealed interface PlantError

data class PlantNotFoundError(val id: String) : PlantError
