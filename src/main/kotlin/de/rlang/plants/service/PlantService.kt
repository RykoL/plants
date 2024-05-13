package de.rlang.plants.service

import arrow.core.Either
import arrow.core.Option
import arrow.core.flatMap
import arrow.core.mapOrAccumulate
import arrow.core.recover
import de.rlang.plants.domain.Plant
import de.rlang.plants.domain.PlantName
import de.rlang.plants.domain.WateringInterval
import de.rlang.plants.domain.errors.DomainError
import de.rlang.plants.domain.errors.PlantError
import de.rlang.plants.domain.errors.PlantNotFoundError
import de.rlang.plants.domain.errors.ValidationErrors
import de.rlang.plants.domain.request.NewPlantRequest
import de.rlang.plants.infrastructure.DriveClient
import de.rlang.plants.infrastructure.PlantRepository
import de.rlang.plants.infrastructure.TrefleClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono


@Service
class PlantService(
    private val trefleClient: TrefleClient,
    private val plantRepository: PlantRepository,
    private val driveClient: DriveClient,
) {

    private val logger = LoggerFactory.getLogger("PlantService")

    suspend fun addNewPlant(newPlant: NewPlantRequest, image: FilePart): Either<PlantError, Plant> =
        driveClient.uploadPlantImage(image)
            .flatMap { driveClient.setImagePublic(it) }
            .mapLeft { DomainError(it.localizedMessage) }
            .flatMap { imageId ->
                Plant.new(
                    newPlant.name,
                    newPlant.location,
                    imageId,
                    WateringInterval(newPlant.wateringIntervalValue, newPlant.wateringIntervalUnit)
                )
            }
            .flatMap { savePlant(it) }

    private suspend fun savePlant(plant: Plant): Either<PlantError, Plant> =
        Either.catch { plantRepository.save(plant).awaitSingle() }.mapLeft { DomainError(it.localizedMessage) }

    suspend fun plants(): Either<PlantError, Flow<Plant>> = Either.catch {
        plantRepository
            .findAll(Sort.by(Sort.Direction.ASC, "lastWatered")).asFlow()
    }.recover { emptyFlow() }

    suspend fun waterPlant(plantId: String) {
        plantById(plantId)
            .toEither {
                logger.warn("Couldn't find plant with id $plantId")
                PlantNotFoundError(plantId) }
            .map { it.water() }
            .flatMap { Either.catch { plantRepository.save(it).awaitSingle() } }
            .onLeft { logger.error("Encountered error during saving the plant. Error was {}", it) }
    }

    suspend fun searchPlantNames(name: String): Either<PlantError, List<PlantName>> {
        return trefleClient
            .searchPlant(name)
            .onRight { logger.info("Found ${it.data.size} items") }
            .flatMap { response ->
                response.data.mapOrAccumulate { PlantName.create(it.commonName).bind() }
                    .mapLeft { ValidationErrors(it) }
            }
    }

    suspend fun plantById(plantId: String): Option<Plant> {
        return Either.catch { plantRepository.findById(plantId).awaitSingle() }
            .onLeft {
                logger.warn("Couldn't find plant with id $plantId")
                logger.error("Message was", it)
            }
            .getOrNone()
    }

    suspend fun removePlant(plantId: String): Unit {
        plantById(plantId).map {
            plantRepository.delete(it).awaitSingleOrNull()
            driveClient.deletePlantImage(it.imageId)
        }
    }
}
