package com.github.kusitms_bugi.domain.session.domain

import com.github.kusitms_bugi.global.entity.BaseEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "session_status")
class SessionStatusHistory(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    var session: Session,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    var status: SessionStatus,

    @Column(nullable = false)
    var timestamp: LocalDateTime = LocalDateTime.now()
) : BaseEntity()
