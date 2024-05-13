package de.rlang.plants.infrastructure

import de.rlang.plants.domain.Plant
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository

@Repository
interface PlantRepository : ReactiveMongoRepository<Plant, String> {
}
