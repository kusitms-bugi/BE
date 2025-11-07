package com.github.kusitms_bugi.domain.session.domain

import com.github.kusitms_bugi.domain.user.domain.User
import com.github.kusitms_bugi.global.entity.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "session")
class Session(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @OneToMany(mappedBy = "session", cascade = [CascadeType.ALL], orphanRemoval = true)
    var statusHistory: MutableList<SessionStatusHistory> = mutableListOf(),

    @OneToMany(mappedBy = "session", cascade = [CascadeType.ALL], orphanRemoval = true)
    var metrics: MutableList<SessionMetric> = mutableListOf()
) : BaseEntity()
