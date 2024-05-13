package de.rlang.plants.domain.presentation

import de.rlang.plants.domain.Plant
import de.rlang.plants.domain.WateringInterval
import java.net.URL
import java.time.LocalDate

data class PlantPresentation(
    val id: String,
    val name: String,
    val location: String,
    val image: URL,
    val wateringInterval: WateringInterval,
    val nextWateringDate: LocalDate,
    val wateringState: String,
    val lastWatered: LocalDate?,
) {
    companion object {
        fun fromPlant(plant: Plant): PlantPresentation {
            return PlantPresentation(
                plant.id,
                plant.name.value,
                plant.location.name,
                plant.imageId.createURL(),
                plant.wateringInterval,
                plant.nextWateringDate(),
                plant.wateringState.toString(),
                plant.lastWatered.getOrNull(),
            )
        }
    }
}
