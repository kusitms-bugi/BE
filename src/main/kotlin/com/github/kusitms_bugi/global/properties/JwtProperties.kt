package com.github.kusitms_bugi.global.properties

import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated
import java.time.Duration

@Validated
@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    @field:NotBlank
    val secret: String,

    val accessTokenValidity: Duration,
    val refreshTokenValidity: Duration
)
