package de.rlang.plants

import arrow.core.flatMap
import arrow.core.getOrElse
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import de.rlang.plants.domain.Plant
import de.rlang.plants.domain.WateringInterval
import de.rlang.plants.domain.errors.DomainError
import de.rlang.plants.domain.errors.ValidationErrors
import de.rlang.plants.domain.presentation.PlantPresentation
import de.rlang.plants.domain.request.PlantDetailRequest
import de.rlang.plants.domain.request.NewPlantRequest
import de.rlang.plants.domain.request.SearchPlantName
import de.rlang.plants.domain.request.WaterPlant
import de.rlang.plants.infrastructure.PlantRepository
import de.rlang.plants.service.PlantService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactor.awaitSingle
import org.slf4j.LoggerFactory
import org.springframework.http.codec.multipart.FilePart
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.multipart.MultipartFile
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Controller
class PlantController(
    val plantService: PlantService,
) {
    private val logger = LoggerFactory.getLogger(PlantController::class.java)

    @PostMapping("")
    suspend fun registerNewPlant(
        response: ServerHttpResponse,
        model: Model,
        @ModelAttribute registerNewPlantRequest: NewPlantRequest,
        @RequestPart("image") image: FilePart,
    ): String {
        return plantService.addNewPlant(registerNewPlantRequest, image)
            .fold({
                when (it) {
                    is ValidationErrors -> logger.error("AAAAAAH")
                    is DomainError -> logger.error("Error occured please help {}", it.message)
                    else -> Unit
                }
                "fragments/addNewPlantForm"
            }, {
                model["plant"] = PlantPresentation.fromPlant(it)
                "fragments/newPlantFragment"
            })
    }

    @DeleteMapping("/{id}/delete")
    suspend fun removePlant(model: Model, @PathVariable id: String): String {
        plantService.removePlant(id)
        model["oobAttr"] = "delete:li[data-plant-list-id=\"$id\"]"
        return "oob/deletePlant"
    }

    @GetMapping("newPlantForm")
    fun newPlantForm(): String {
        return "fragments/addNewPlantForm"
    }

    @PostMapping("searchNames")
    suspend fun searchPlantName(model: Model, searchPlantName: SearchPlantName): String {
        val plantNames = plantService.searchPlantNames(searchPlantName.name).getOrElse { emptyList() }
        model["names"] = plantNames
        return "fragments/searchPlantNames"
    }

    @PostMapping("detail")
    suspend fun plantDetail(model: Model, plantDetailRequest: PlantDetailRequest): String {
        plantService
            .plantById(plantDetailRequest.plantId)
            .map(PlantPresentation::fromPlant)
            .onSome {
                model["plant"] = it
            }
            .onNone { logger.warn("Plant with id ${plantDetailRequest.plantId} does not exist") }
        return "fragments/drawer"
    }

    @PostMapping("water")
    suspend fun waterPlant(model: Model, waterPlant: WaterPlant): String {
        plantService.waterPlant(waterPlant.plantId)
        return "fragments/plantTable"
    }

    @GetMapping("")
    suspend fun index(model: Model): String {
        return "index"
    }

    @ModelAttribute("plants")
    suspend fun plants(): Flow<PlantPresentation> =
        plantService.plants().getOrElse { emptyFlow() }.map(PlantPresentation::fromPlant)

    @ModelAttribute("newPlant")
    fun emptyPlant() = NewPlantRequest.empty()
}
