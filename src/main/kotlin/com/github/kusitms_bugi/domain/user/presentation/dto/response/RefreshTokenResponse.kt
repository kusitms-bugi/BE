package com.github.kusitms_bugi.domain.user.presentation.dto.response

data class RefreshTokenResponse(
    val accessToken: String,
    val refreshToken: String
)
