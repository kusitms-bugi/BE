package com.github.kusitms_bugi.domain.user.presentation.dto.request

import com.github.kusitms_bugi.domain.user.domain.User
import com.github.kusitms_bugi.global.validator.AllowedOrigin
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class SignupRequest(
    @field:NotBlank(message = "이름은 필수입니다.")
    val name: String,

    @field:NotBlank(message = "이메일은 필수입니다.")
    @field:Email(message = "올바른 이메일 형식이 아닙니다.")
    val email: String,

    @field:NotBlank(message = "비밀번호는 필수입니다.")
    @field:Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
    val password: String,

    @field:NotBlank(message = "콜백 URL은 필수입니다.")
    @field:AllowedOrigin
    val callbackUrl: String,

    val avatar: String? = null
)

fun SignupRequest.toEntity(password: String) = User(
    name = this.name,
    email = this.email,
    password = password,
    avatar = this.avatar,
    active = false
)