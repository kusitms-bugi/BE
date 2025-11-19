package com.github.kusitms_bugi.domain.auth.presentation.dto.response

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String
)
