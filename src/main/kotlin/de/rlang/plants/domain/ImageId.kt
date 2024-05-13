package de.rlang.plants.domain

import java.net.URI
import java.net.URL

@JvmInline
value class ImageId(val value: String) {
    fun createURL(): URL = URI.create("https://lh3.googleusercontent.com/d/$value").toURL()
}