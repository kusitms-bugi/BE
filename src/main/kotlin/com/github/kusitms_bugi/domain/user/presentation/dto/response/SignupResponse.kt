package com.github.kusitms_bugi.domain.user.presentation.dto.response

import com.github.kusitms_bugi.domain.user.domain.User
import java.util.*

data class SignupResponse(
    val id: UUID,
    val name: String,
    val email: String,
    val avatar: String?
)

fun User.toResponse() = SignupResponse(
    id = this.id,
    name = this.name,
    email = this.email,
    avatar = this.avatar
)