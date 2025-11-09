package com.github.kusitms_bugi.domain.user.presentation.dto.response

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String
)
