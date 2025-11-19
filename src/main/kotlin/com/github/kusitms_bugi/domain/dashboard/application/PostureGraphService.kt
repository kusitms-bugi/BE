package com.github.kusitms_bugi.domain.dashboard.application

import com.github.kusitms_bugi.domain.dashboard.presentation.dto.request.GetPeriodRequest
import com.github.kusitms_bugi.domain.dashboard.presentation.dto.response.PostureGraphResponse
import com.github.kusitms_bugi.domain.session.infrastructure.jpa.SessionJpaRepository
import com.github.kusitms_bugi.domain.user.infrastructure.jpa.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import kotlin.math.round

@Service
@Transactional(readOnly = true)
class PostureGraphService(
    private val sessionJpaRepository: SessionJpaRepository
) {

    fun getPostureGraph(user: User, request: GetPeriodRequest): PostureGraphResponse {
        val points = when (request.period) {
            GetPeriodRequest.Period.WEEKLY -> calculateWeeklyData(user)
            GetPeriodRequest.Period.MONTHLY -> {
                val month = request.month ?: LocalDate.now().monthValue
                calculateMonthlyData(user, request.year, month)
            }
            GetPeriodRequest.Period.YEARLY -> emptyMap()
        }

        return PostureGraphResponse(points)
    }

    private fun calculateWeeklyData(user: User): Map<LocalDate, Int> {
        val now = LocalDate.now()
        val weeks = (0 until WEEKS_COUNT).map { weekOffset ->
            val weekStart = now.minusWeeks(weekOffset.toLong())
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            weekStart
        }.reversed()

        val startDate = weeks.first().atStartOfDay()
        val endDate = now.atTime(23, 59, 59)
        val sessions = sessionJpaRepository.findByUserAndCreatedAtBetween(user, startDate, endDate)

        val sessionsByWeek = sessions.groupBy { session ->
            session.createdAt.toLocalDate()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        }

        return weeks.associateWith { weekStart ->
            sessionsByWeek[weekStart]
                ?.mapNotNull { it.score }
                ?.takeIf { it.isNotEmpty() }
                ?.average()
                ?.let { round(it).toInt() }
                ?: 0
        }
    }

    private fun calculateMonthlyData(user: User, year: Int, month: Int): Map<LocalDate, Int> {
        val yearMonth = java.time.YearMonth.of(year, month)
        val daysInMonth = yearMonth.lengthOfMonth()

        val days = (1..daysInMonth).map { day ->
            LocalDate.of(year, month, day)
        }

        val startDate = yearMonth.atDay(1).atStartOfDay()
        val endDate = yearMonth.atEndOfMonth().atTime(23, 59, 59)
        val sessions = sessionJpaRepository.findByUserAndCreatedAtBetween(user, startDate, endDate)

        val sessionsByDay = sessions.groupBy { session ->
            session.createdAt.toLocalDate()
        }

        return days.associateWith { day ->
            sessionsByDay[day]
                ?.mapNotNull { it.score }
                ?.takeIf { it.isNotEmpty() }
                ?.average()
                ?.let { round(it).toInt() }
                ?: 0
        }
    }

    companion object {
        private const val WEEKS_COUNT = 20
    }
}
