package com.github.kusitms_bugi.domain.dashboard.application

import com.github.kusitms_bugi.domain.dashboard.presentation.dto.response.PosturePatternResponse
import com.github.kusitms_bugi.domain.session.infrastructure.jpa.Session
import com.github.kusitms_bugi.domain.session.infrastructure.jpa.SessionJpaRepository
import com.github.kusitms_bugi.domain.user.infrastructure.jpa.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

@Service
@Transactional(readOnly = true)
class PosturePatternService(
    private val sessionJpaRepository: SessionJpaRepository
) {

    fun getPosturePattern(user: User): PosturePatternResponse {
        val sevenDaysAgo = LocalDate.now().minusDays(6)
        val sessions = sessionJpaRepository.findByUserAndCreatedAtBetween(
            user,
            sevenDaysAgo.atStartOfDay(),
            LocalDate.now().atTime(23, 59, 59)
        )

        val worstTime = calculateWorstTime(sessions)
        val worstDay = calculateWorstDay(sessions)
        val recovery = calculateRecovery(sessions)

        return PosturePatternResponse(
            worstTime = worstTime,
            worstDay = worstDay,
            recovery = recovery,
            stretching = DEFAULT_STRETCHING
        )
    }

    private fun calculateWorstTime(sessions: List<Session>): LocalTime {
        val badPostureDurationByHour = mutableMapOf<Int, Long>()

        sessions.forEach { session ->
            val metrics = session.getActiveMetrics()

            metrics.zipWithNext().forEach { (current, next) ->
                if (current.score >= 4.0) {
                    val hour = current.timestamp.hour
                    val durationMillis = java.time.Duration.between(current.timestamp, next.timestamp).toMillis()
                    val pausedDuration = session.calculatePausedDuration(current.timestamp, next.timestamp).toMillis()
                    val activeDuration = durationMillis - pausedDuration

                    if (activeDuration > 0) {
                        badPostureDurationByHour[hour] = badPostureDurationByHour.getOrDefault(hour, 0L) + activeDuration
                    }
                }
            }
        }

        val worstHour = badPostureDurationByHour.maxByOrNull { it.value }?.key ?: 12
        return LocalTime.of(worstHour, 0, 0)
    }

    private fun calculateWorstDay(sessions: List<Session>): DayOfWeek {
        val badPostureDurationByDay = mutableMapOf<DayOfWeek, Long>()

        sessions.forEach { session ->
            val dayOfWeek = session.createdAt.dayOfWeek
            val levelDurations = session.getLevelDurations()
            val badPostureDuration = (levelDurations[4] ?: 0L) +
                                     (levelDurations[5] ?: 0L) +
                                     (levelDurations[6] ?: 0L)

            badPostureDurationByDay[dayOfWeek] = badPostureDurationByDay.getOrDefault(dayOfWeek, 0L) + badPostureDuration
        }

        return badPostureDurationByDay.maxByOrNull { it.value }?.key ?: DayOfWeek.MONDAY
    }

    private fun calculateRecovery(sessions: List<Session>): Int {
        val recoveryTimes = mutableListOf<Long>()

        sessions.forEach { session ->
            val metrics = session.getActiveMetrics()
            var badPostureStartTime: java.time.LocalDateTime? = null

            metrics.forEach { metric ->
                when {
                    metric.score >= 4.0 && badPostureStartTime == null -> {
                        badPostureStartTime = metric.timestamp
                    }
                    metric.score < 4.0 && badPostureStartTime != null -> {
                        val startTime = badPostureStartTime!!
                        val durationMillis = java.time.Duration.between(startTime, metric.timestamp).toMillis()
                        val pausedDuration = session.calculatePausedDuration(startTime, metric.timestamp).toMillis()
                        val activeDuration = durationMillis - pausedDuration

                        if (activeDuration > 0) {
                            recoveryTimes.add(activeDuration)
                        }
                        badPostureStartTime = null
                    }
                }
            }
        }

        if (recoveryTimes.isEmpty()) return 0

        val averageRecoveryMillis = recoveryTimes.average()
        return (averageRecoveryMillis / 1000 / 60).toInt()
    }

    companion object {
        private const val DEFAULT_STRETCHING = "목돌리기"
    }
}
