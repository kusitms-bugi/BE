package com.github.kusitms_bugi.domain.auth.presentation.dto.request

import com.github.kusitms_bugi.global.validator.AllowedOrigin
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class ResendVerificationEmailRequest(
    @field:NotBlank(message = "이메일은 필수입니다.")
    @field:Email(message = "올바른 이메일 형식이 아닙니다.")
    val email: String,

    @field:NotBlank(message = "콜백 URL은 필수입니다.")
    @field:AllowedOrigin
    val callbackUrl: String
)
