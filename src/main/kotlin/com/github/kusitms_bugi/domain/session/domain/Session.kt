package com.github.kusitms_bugi.domain.session.domain

import com.github.kusitms_bugi.domain.user.infrastructure.jpa.User
import com.github.kusitms_bugi.global.entity.BaseField
import java.time.LocalDateTime

enum class SessionStatus {
    STARTED,
    PAUSED,
    RESUMED,
    STOPPED
}

interface SessionField<SESSION_STATUS_HISTORY, SESSION_METRIC> : BaseField {
    var user: User
    var statusHistory: MutableList<SESSION_STATUS_HISTORY>
    var metrics: MutableSet<SESSION_METRIC>
    var score: Int?
}

interface SessionStatusHistoryField<SESSION> : BaseField {
    var session: SESSION
    var status: SessionStatus
    var timestamp: LocalDateTime
}

interface SessionMetricField<SESSION> : BaseField {
    var session: SESSION
    var score: Double
    var timestamp: LocalDateTime
}