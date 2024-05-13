package de.rlang.plants

import org.springframework.context.annotation.Bean
import org.springframework.web.server.i18n.AcceptHeaderLocaleContextResolver

@Bean
fun localeResolver(): AcceptHeaderLocaleContextResolver = AcceptHeaderLocaleContextResolver()
