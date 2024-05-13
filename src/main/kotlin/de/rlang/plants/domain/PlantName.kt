package de.rlang.plants.domain

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import de.rlang.plants.domain.errors.ValidationError

@JvmInline
value class PlantName private constructor(val value: String) {
    companion object {
        fun create(name: String): Either<ValidationError, PlantName> =
            either {
                ensure(name.isNotBlank()) { ValidationError("name", name, "PlantName is empty") }
                PlantName(name)
            }
    }
}
