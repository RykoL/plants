package de.rlang.plants.domain.dto

import com.fasterxml.jackson.annotation.JsonAlias

data class TrefleSearchResponse(
    val data: List<TrefleSearchItem>
)

data class TrefleSearchItem(
    @JsonAlias("image_url")
    val imageURL: String,
    val slug: String,
    @JsonAlias("common_name")
    val commonName: String
)