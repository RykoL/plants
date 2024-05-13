package de.rlang.plants.domain

import arrow.core.Either
import arrow.core.Nel
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.getOrElse
import arrow.core.raise.either
import arrow.core.raise.zipOrAccumulate
import arrow.optics.copy
import arrow.optics.optics
import de.rlang.plants.domain.errors.ValidationError
import de.rlang.plants.domain.errors.ValidationErrors
import java.time.LocalDate

@optics
data class Plant(
    val name: PlantName,
    val location: PlantLocation,
    val imageId: ImageId,
    val wateringInterval: WateringInterval,
    val wateringState: WateringState,
    val lastWatered: Option<LocalDate>,
) {
    lateinit var id: String

    fun water(): Plant {
        val plant = copy {
            Plant.lastWatered set Some(LocalDate.now())
            Plant.wateringState set WateringState.WATERED
        }
        plant.id = id
        return plant
    }

    fun nextWateringDate(): LocalDate =
        lastWatered.map {
            when (wateringInterval.interval) {
                Interval.DAYS -> it.plusDays(wateringInterval.number.toLong())
                Interval.WEEKS -> it.plusWeeks(wateringInterval.number.toLong())
            }
        }.getOrElse { LocalDate.now() }

    companion object {
        fun create(
            name: String,
            location: String,
            imageId: ImageId,
            wateringState: WateringState,
            wateringInterval: WateringInterval,
            lastWatered: Option<LocalDate>,
        ): Either<ValidationErrors, Plant> =
            either<Nel<ValidationError>, Plant> {
                zipOrAccumulate(
                    { PlantName.create(name).bind() },
                    { PlantLocation.create(location).bind() },
                ) { plantName, plantLocation ->
                    Plant(plantName, plantLocation, imageId, wateringInterval, wateringState, lastWatered)
                }
            }.mapLeft { ValidationErrors(it) }

        fun new(
            name: String,
            location: String,
            imageId: ImageId,
            wateringInterval: WateringInterval,
        ): Either<ValidationErrors, Plant> = create(name, location, imageId, WateringState.DRY, wateringInterval, None)
    }
}
