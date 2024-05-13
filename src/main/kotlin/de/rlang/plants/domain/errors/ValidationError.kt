package de.rlang.plants.domain.errors

import arrow.core.Nel
import org.springframework.validation.FieldError

data class ValidationError(val field: String, val value: Any, val message: String) {
    fun toFieldError(): FieldError = FieldError(
        "", field, value, false, emptyArray(), emptyArray(), message
    )
}

data class ValidationErrors(val errors: Nel<ValidationError>) : PlantError {
    fun fieldErrors() = errors.map(ValidationError::toFieldError)
}