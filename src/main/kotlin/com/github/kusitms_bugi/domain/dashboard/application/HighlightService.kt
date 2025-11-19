package com.github.kusitms_bugi.domain.dashboard.application

import com.github.kusitms_bugi.domain.dashboard.presentation.dto.request.GetPeriodRequest
import com.github.kusitms_bugi.domain.dashboard.presentation.dto.response.HighlightResponse
import com.github.kusitms_bugi.domain.session.infrastructure.jpa.Session
import com.github.kusitms_bugi.domain.session.infrastructure.jpa.SessionJpaRepository
import com.github.kusitms_bugi.domain.user.infrastructure.jpa.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters

@Service
@Transactional(readOnly = true)
class HighlightService(
    private val sessionJpaRepository: SessionJpaRepository
) {

    fun getHighlight(user: User, request: GetPeriodRequest): HighlightResponse {
        return when (request.period) {
            GetPeriodRequest.Period.WEEKLY -> calculateWeeklyHighlight(user)
            GetPeriodRequest.Period.MONTHLY -> calculateMonthlyHighlight(user, request.year, request.month ?: 1)
            GetPeriodRequest.Period.YEARLY -> HighlightResponse(current = 0, previous = 0)
        }
    }

    private fun calculateWeeklyHighlight(user: User): HighlightResponse {
        val today = LocalDate.now()
        val thisWeekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val lastWeekStart = thisWeekStart.minusWeeks(1)
        val lastWeekEnd = thisWeekStart.minusDays(1)

        val thisWeekSessions = sessionJpaRepository.findByUserAndCreatedAtBetween(
            user,
            thisWeekStart.atStartOfDay(),
            today.atTime(23, 59, 59)
        )

        val lastWeekSessions = sessionJpaRepository.findByUserAndCreatedAtBetween(
            user,
            lastWeekStart.atStartOfDay(),
            lastWeekEnd.atTime(23, 59, 59)
        )

        return HighlightResponse(
            current = calculateAverageBadPostureMinutes(thisWeekSessions),
            previous = calculateAverageBadPostureMinutes(lastWeekSessions)
        )
    }

    private fun calculateMonthlyHighlight(user: User, year: Int, month: Int): HighlightResponse {
        val thisMonth = YearMonth.of(year, month)
        val lastMonth = thisMonth.minusMonths(1)

        val thisMonthSessions = sessionJpaRepository.findByUserAndCreatedAtBetween(
            user,
            thisMonth.atDay(1).atStartOfDay(),
            thisMonth.atEndOfMonth().atTime(23, 59, 59)
        )

        val lastMonthSessions = sessionJpaRepository.findByUserAndCreatedAtBetween(
            user,
            lastMonth.atDay(1).atStartOfDay(),
            lastMonth.atEndOfMonth().atTime(23, 59, 59)
        )

        return HighlightResponse(
            current = calculateAverageBadPostureMinutes(thisMonthSessions),
            previous = calculateAverageBadPostureMinutes(lastMonthSessions)
        )
    }

    private fun calculateAverageBadPostureMinutes(sessions: List<Session>): Int {
        if (sessions.isEmpty()) return 0

        val totalBadPostureMillis = sessions.sumOf { session ->
            val levelDurations = session.getLevelDurations()
            (levelDurations[4] ?: 0L) + (levelDurations[5] ?: 0L) + (levelDurations[6] ?: 0L)
        }

        val averageMillis = totalBadPostureMillis / sessions.size
        return (averageMillis / 1000 / 60).toInt()
    }
}
