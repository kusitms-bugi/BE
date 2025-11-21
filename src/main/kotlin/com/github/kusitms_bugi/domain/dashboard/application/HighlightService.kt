package com.github.kusitms_bugi.domain.dashboard.application

import com.github.kusitms_bugi.domain.dashboard.presentation.dto.request.GetHighlightRequest
import com.github.kusitms_bugi.domain.dashboard.presentation.dto.response.HighlightResponse
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

    fun getHighlight(user: User, request: GetHighlightRequest): HighlightResponse {
        return when (request.period) {
            GetHighlightRequest.Period.WEEKLY -> calculateWeeklyHighlight(user)
            GetHighlightRequest.Period.MONTHLY -> calculateMonthlyHighlight(user)
        }
    }

    private fun calculateWeeklyHighlight(user: User): HighlightResponse {
        val today = LocalDate.now()
        val thisWeekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val lastWeekStart = thisWeekStart.minusWeeks(1)
        val lastWeekEnd = thisWeekStart.minusDays(1)

        val current = sessionJpaRepository.getHighlightStats(
            user.id,
            thisWeekStart.atStartOfDay(),
            today.atTime(23, 59, 59)
        ) ?: 0

        val previous = sessionJpaRepository.getHighlightStats(
            user.id,
            lastWeekStart.atStartOfDay(),
            lastWeekEnd.atTime(23, 59, 59)
        ) ?: 0

        return HighlightResponse(
            current = current,
            previous = previous
        )
    }

    private fun calculateMonthlyHighlight(user: User): HighlightResponse {
        val today = LocalDate.now()
        val thisMonth = YearMonth.from(today)
        val lastMonth = thisMonth.minusMonths(1)

        val current = sessionJpaRepository.getHighlightStats(
            user.id,
            thisMonth.atDay(1).atStartOfDay(),
            thisMonth.atEndOfMonth().atTime(23, 59, 59)
        ) ?: 0

        val previous = sessionJpaRepository.getHighlightStats(
            user.id,
            lastMonth.atDay(1).atStartOfDay(),
            lastMonth.atEndOfMonth().atTime(23, 59, 59)
        ) ?: 0

        return HighlightResponse(
            current = current,
            previous = previous
        )
    }
}
