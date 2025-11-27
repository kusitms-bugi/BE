package com.github.kusitms_bugi.domain.user.domain

import com.github.kusitms_bugi.global.entity.BaseField

interface UserField<SESSION> : BaseField {
    val name: String
    val email: String
    val password: String
    val avatar: String?
    val active: Boolean
    val sessions: MutableList<SESSION>
}