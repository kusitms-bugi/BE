package com.github.kusitms_bugi.domain.user.infrastructure.jpa

import com.github.kusitms_bugi.domain.session.infrastructure.jpa.Session
import com.github.kusitms_bugi.domain.user.domain.UserField
import com.github.kusitms_bugi.global.entity.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "users")
class User(
    @Column(nullable = false)
    override var name: String,

    @Column(nullable = false, unique = true)
    override var email: String,

    @Column(nullable = false)
    override var password: String,

    @Column
    override var avatar: String? = null,

    @Column(nullable = false)
    override var active: Boolean = false,

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    override var sessions: MutableList<Session> = mutableListOf()
) : BaseEntity(), UserField<Session>

