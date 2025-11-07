package com.github.kusitms_bugi.domain.session.infrastructure.jpa

import com.github.kusitms_bugi.domain.session.domain.SessionField
import com.github.kusitms_bugi.domain.session.domain.SessionMetricField
import com.github.kusitms_bugi.domain.session.domain.SessionStatus
import com.github.kusitms_bugi.domain.session.domain.SessionStatusHistoryField
import com.github.kusitms_bugi.domain.user.infrastructure.jpa.User
import com.github.kusitms_bugi.global.entity.BaseEntity
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(name = "session")
@NamedEntityGraph(
    name = "Session.withAll",
    attributeNodes = [
        NamedAttributeNode("user"),
        NamedAttributeNode("statusHistory"),
        NamedAttributeNode("metrics")
    ]
)
class Session(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    override var user: User,

    @OneToMany(mappedBy = "session", cascade = [CascadeType.ALL], orphanRemoval = true)
    override var statusHistory: MutableList<SessionStatusHistory> = mutableListOf(),

    @OneToMany(mappedBy = "session", cascade = [CascadeType.ALL], orphanRemoval = true)
    override var metrics: MutableList<SessionMetric> = mutableListOf()
) : BaseEntity(), SessionField<SessionStatusHistory, SessionMetric> {

    fun lastStatus(): SessionStatus? = statusHistory.maxByOrNull { it.timestamp }?.status
}

@Entity
@Table(name = "session_status")
@EntityListeners(AuditingEntityListener::class)
class SessionStatusHistory(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    override var session: Session,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    override var status: SessionStatus,

    @CreatedDate
    @Column(nullable = false)
    override var timestamp: LocalDateTime = LocalDateTime.now()
) : BaseEntity(), SessionStatusHistoryField<Session>

@Entity
@Table(name = "session_metric")
@EntityListeners(AuditingEntityListener::class)
class SessionMetric(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    override var session: Session,

    @Column(nullable = false)
    override var score: Double,

    @CreatedDate
    @Column(nullable = false)
    override var timestamp: LocalDateTime = LocalDateTime.now()
) : BaseEntity(), SessionMetricField<Session>