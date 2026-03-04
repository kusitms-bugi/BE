package com.github.kusitms_bugi.global.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties(prefix = "landing")
data class LandingProperties(
    val downloadUrl: String
)
