package com.github.kusitms_bugi.domain.user.domain

import com.github.kusitms_bugi.global.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "users")
class User(
    @Column(nullable = false)
    var name: String,

    @Column(nullable = false, unique = true)
    var email: String,

    @Column(nullable = false)
    var password: String,

    @Column
    var avatar: String? = null,

    @Column(nullable = false)
    var active: Boolean = false
) : BaseEntity()
