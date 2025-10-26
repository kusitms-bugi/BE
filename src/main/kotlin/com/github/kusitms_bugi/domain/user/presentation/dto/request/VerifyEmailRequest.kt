package com.github.kusitms_bugi.domain.user.presentation.dto.request

import jakarta.validation.constraints.NotBlank

data class VerifyEmailRequest(
    @field:NotBlank(message = "토큰은 필수입니다.")
    val token: String
)
