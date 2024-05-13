package de.rlang.plants.infrastructure

import arrow.core.Either
import de.rlang.plants.domain.dto.TrefleSearchResponse
import de.rlang.plants.domain.errors.TrefleError
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class TrefleClient(
    @Value("\${trefle.baseURL}")
    private val baseURL: String,
    @Value("\${trefle.token}")
    private val token: String,
) {
    private val webClient = WebClient.builder().baseUrl(baseURL).build()
    suspend fun searchPlant(name: String): Either<TrefleError, TrefleSearchResponse> {
        return Either.catch {
            webClient
                .get()
                .uri {
                    it
                        .path("/api/v1/plants/search")
                        .queryParam("token", token)
                        .queryParam("q", name)
                        .build()
                }
                .retrieve()
                .bodyToMono(TrefleSearchResponse::class.java)
                .awaitSingle()
        }.mapLeft {
            TrefleError(it.message!!, HttpStatusCode.valueOf(500))
        }
    }
}