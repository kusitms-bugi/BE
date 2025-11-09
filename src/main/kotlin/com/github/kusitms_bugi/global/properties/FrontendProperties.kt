package com.github.kusitms_bugi.global.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties(prefix = "frontend")
data class FrontendProperties(
    val allowedOrigins: List<String>
)