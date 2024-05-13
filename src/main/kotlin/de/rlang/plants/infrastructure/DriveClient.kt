package de.rlang.plants.infrastructure

import arrow.core.Either
import com.google.api.client.http.InputStreamContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.Permission
import de.rlang.plants.domain.ImageId
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.io.InputStream
import java.util.UUID

@Component
class DriveClient(
    private val driveClient: Drive,
    @Value("\${google.drive.folderId}")
    private val folderId: String
) {
    suspend fun uploadPlantImage(file: FilePart): Either<Throwable, ImageId> =
        Either.catch {
            val metadata = File()
            metadata.parents = listOf(folderId)
            metadata.name = UUID.randomUUID().toString()
            val inputStream = getInputStreamFromFluxDataBuffer(file.content()).awaitSingle()
            val result = driveClient
                .files()
                .create(metadata, InputStreamContent(file.headers().contentType.toString(), inputStream))
                .setFields("id")
                .execute()

            ImageId(result.id)
        }

    suspend fun deletePlantImage(imageId: ImageId): Either<Throwable, Unit> = Either.catch {
        driveClient.files().delete(imageId.value).execute()
    }

    suspend fun setImagePublic(imageId: ImageId): Either<Throwable, ImageId> =
        Either.catch {
            val permission = Permission().apply {
                type = "anyone"
                role = "reader"
            }
            driveClient.Permissions().create(imageId.value, permission)
            imageId
        }

    private fun getInputStreamFromFluxDataBuffer(data: Flux<DataBuffer?>): Mono<InputStream> {
        return DataBufferUtils.join(data).map(DataBuffer::asInputStream)
    }
}