package com.github.kusitms_bugi.domain.session.infrastructure.jpa

import com.github.kusitms_bugi.domain.session.domain.SessionField
import com.github.kusitms_bugi.domain.session.domain.SessionMetricField
import com.github.kusitms_bugi.domain.session.domain.SessionStatus
import com.github.kusitms_bugi.domain.session.domain.SessionStatusHistoryField
import com.github.kusitms_bugi.domain.user.infrastructure.jpa.User
import com.github.kusitms_bugi.global.entity.BaseEntity
import jakarta.persistence.*
import org.hibernate.annotations.BatchSize
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Entity
@Table(name = "session")
class Session(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    override var user: User,

    @OrderBy("timestamp ASC")
    @OneToMany(mappedBy = "session", cascade = [CascadeType.ALL], orphanRemoval = true)
    @BatchSize(size = 25)
    override var statusHistory: MutableList<SessionStatusHistory> = mutableListOf(),

    @OrderBy("timestamp ASC")
    @OneToMany(mappedBy = "session", cascade = [CascadeType.ALL], orphanRemoval = true)
    @BatchSize(size = 25)
    override var metrics: MutableSet<SessionMetric> = mutableSetOf(),

    @Column(name = "score", nullable = true)
    override var score: Int? = null
) : BaseEntity(), SessionField<SessionStatusHistory, SessionMetric> {

    fun lastStatus(): SessionStatus? = statusHistory.lastOrNull()?.status

    private fun getActiveRanges(): List<Pair<LocalDateTime, LocalDateTime>> {
        val ranges = mutableListOf<Pair<LocalDateTime, LocalDateTime>>()
        var activeStartTime: LocalDateTime? = null

        statusHistory.forEach { history ->
            when (history.status) {
                SessionStatus.STARTED, SessionStatus.RESUMED -> activeStartTime = history.timestamp
                SessionStatus.PAUSED, SessionStatus.STOPPED -> {
                    activeStartTime?.let { start ->
                        ranges.add(start to history.timestamp)
                        activeStartTime = null
                    }
                }
            }
        }

        return ranges
    }

    fun calculateTotalActiveSeconds(): Long {
        return getActiveRanges().sumOf { (start, end) ->
            ChronoUnit.SECONDS.between(start, end)
        }
    }

    fun calculatePausedDuration(start: LocalDateTime, end: LocalDateTime): Duration {
        var pausedMillis = 0L
        var pauseStartTime: LocalDateTime? = null

        statusHistory
            .filter { it.timestamp in start..end }
            .forEach { history ->
                when (history.status) {
                    SessionStatus.PAUSED -> {
                        pauseStartTime = history.timestamp
                    }
                    SessionStatus.RESUMED -> {
                        pauseStartTime?.let { pauseStart ->
                            pausedMillis += ChronoUnit.MILLIS.between(pauseStart, history.timestamp)
                            pauseStartTime = null
                        }
                    }
                    else -> {}
                }
            }

        return Duration.ofMillis(pausedMillis)
    }

    fun getActiveMetrics(): List<SessionMetric> {
        val activeRanges = getActiveRanges()
        return metrics
            .filter { metric -> activeRanges.any { (start, end) -> metric.timestamp in start..end } }
            .sortedBy { it.timestamp }
    }

    fun getLevelDurations(): Map<Int, Long> {
        val activeMetrics = getActiveMetrics()
        val levelDurations = (1..6).associateWith { 0L }.toMutableMap()

        // Pause 구간들을 한 번만 미리 계산 (O(m) where m = statusHistory 수)
        val pauseRanges = getPauseRanges()

        activeMetrics.zipWithNext().forEach { (current, next) ->
            val level = current.score.coerceIn(1, 6)
            val duration = Duration.between(current.timestamp, next.timestamp)

            // 미리 계산된 pause 구간을 사용하여 pausedDuration 계산 (O(p) where p = pauseRanges 수)
            val pausedDuration = calculatePausedDurationFromRanges(current.timestamp, next.timestamp, pauseRanges)
            val activeDuration = duration.minus(pausedDuration).toMillis()

            if (activeDuration > 0) {
                levelDurations[level] = levelDurations.getValue(level) + activeDuration
            }
        }

        return levelDurations
    }

    private fun getPauseRanges(): List<Pair<LocalDateTime, LocalDateTime>> {
        val ranges = mutableListOf<Pair<LocalDateTime, LocalDateTime>>()
        var pauseStartTime: LocalDateTime? = null

        statusHistory.forEach { history ->
            when (history.status) {
                SessionStatus.PAUSED -> {
                    pauseStartTime = history.timestamp
                }
                SessionStatus.RESUMED -> {
                    pauseStartTime?.let { pauseStart ->
                        ranges.add(pauseStart to history.timestamp)
                        pauseStartTime = null
                    }
                }
                else -> {}
            }
        }

        return ranges
    }

    private fun calculatePausedDurationFromRanges(
        start: LocalDateTime,
        end: LocalDateTime,
        pauseRanges: List<Pair<LocalDateTime, LocalDateTime>>
    ): Duration {
        var pausedMillis = 0L

        pauseRanges.forEach { (pauseStart, pauseEnd) ->
            // pause 구간이 [start, end] 범위와 겹치는 부분 계산
            val overlapStart = maxOf(start, pauseStart)
            val overlapEnd = minOf(end, pauseEnd)

            if (overlapStart < overlapEnd) {
                pausedMillis += ChronoUnit.MILLIS.between(overlapStart, overlapEnd)
            }
        }

        return Duration.ofMillis(pausedMillis)
    }

    fun calculateGoodSeconds(): Long {
        // Pause 구간들을 한 번만 미리 계산하여 재사용
        val pauseRanges = getPauseRanges()

        return metrics.zipWithNext()
            .filter { (_, current) -> current.score >= 4 }
            .sumOf { (prev, current) ->
                val duration = ChronoUnit.MILLIS.between(prev.timestamp, current.timestamp)
                val pausedDuration = calculatePausedDurationFromRanges(prev.timestamp, current.timestamp, pauseRanges).toMillis()
                duration - pausedDuration
            } / 1_000
    }
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
    override var score: Int,

    @Column(nullable = false)
    override var timestamp: LocalDateTime
) : BaseEntity(), SessionMetricField<Session>