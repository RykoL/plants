package de.rlang.plants.domain.errors

import org.springframework.http.HttpStatusCode

class TrefleError(val message: String , statusCode: HttpStatusCode) : PlantError