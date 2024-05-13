package de.rlang.plants.infrastructure

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.File

@Configuration
class GoogleConfiguration {
    @Bean
    fun credentials(): GoogleCredentials =
        GoogleCredentials
            .fromStream(File("/Users/rlang/Downloads/plants-419814-3aaea5cb2da9.json").inputStream())
            .createScoped(DriveScopes.DRIVE_FILE, DriveScopes.DRIVE)

    @Bean
    fun drive(credentials: GoogleCredentials): Drive {
        val transport = GoogleNetHttpTransport.newTrustedTransport();
        val jsonFactory = GsonFactory.getDefaultInstance()
        return Drive.Builder(transport, jsonFactory, HttpCredentialsAdapter(credentials))
            .setApplicationName("plants")
            .build()
    }
}
