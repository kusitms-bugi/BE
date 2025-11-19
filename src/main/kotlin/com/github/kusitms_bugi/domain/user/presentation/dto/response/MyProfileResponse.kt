package com.github.kusitms_bugi.domain.user.presentation.dto.response

import com.github.kusitms_bugi.domain.user.infrastructure.jpa.User

data class MyProfileResponse(
    val name: String,
    val email: String,
    val avatar: String?
)

fun User.toMyProfileResponse() = MyProfileResponse(
    name = this.name,
    email = this.email,
    avatar = this.avatar
)