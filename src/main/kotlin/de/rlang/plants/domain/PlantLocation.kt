package de.rlang.plants.domain

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import de.rlang.plants.domain.errors.ValidationError

@JvmInline
value class PlantLocation private constructor(val name: String) {
    companion object {
        fun create(location: String): Either<ValidationError, PlantLocation> =
            either {
                ensure(location.isNotBlank()) { ValidationError("location",location, "Location cannot be blank") }
                ensure(location.length < 100) { ValidationError("location",location, "Location cannot be more than 20 characters") }

                PlantLocation(location)
            }
    }
}
