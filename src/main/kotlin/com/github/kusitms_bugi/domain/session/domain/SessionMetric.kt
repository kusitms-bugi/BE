package com.github.kusitms_bugi.domain.session.domain

import com.github.kusitms_bugi.global.entity.BaseEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "session_metric")
class SessionMetric(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    var session: Session,

    @Column(nullable = false)
    var score: Double,

    @Column(nullable = false)
    var timestamp: LocalDateTime
) : BaseEntity()
