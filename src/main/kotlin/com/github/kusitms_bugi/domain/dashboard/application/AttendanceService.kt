package com.github.kusitms_bugi.domain.dashboard.application

import com.github.kusitms_bugi.domain.dashboard.presentation.dto.request.GetAttendanceRequest
import com.github.kusitms_bugi.domain.dashboard.presentation.dto.response.AttendanceResponse
import com.github.kusitms_bugi.domain.session.infrastructure.jpa.Session
import com.github.kusitms_bugi.domain.session.infrastructure.jpa.SessionJpaRepository
import com.github.kusitms_bugi.domain.user.infrastructure.jpa.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.round

@Service
@Transactional(readOnly = true)
class AttendanceService(
    private val sessionJpaRepository: SessionJpaRepository
) {

    fun getAttendance(user: User, request: GetAttendanceRequest): AttendanceResponse {
        val yearMonth = YearMonth.of(request.year, request.month)

        val sessions = sessionJpaRepository.findByUserAndCreatedAtBetween(
            user = user,
            startDate = yearMonth.atDay(1).atStartOfDay(),
            endDate = yearMonth.atEndOfMonth().atTime(23, 59, 59)
        )

        val sessionsByDate = sessions
            .groupBy { it.createdAt.toLocalDate() }
            .mapValues { (_, sessionsOnDate) ->
                sessionsOnDate.sumOf { session ->
                    (session.calculateTotalActiveSeconds() / 60.0).toInt()
                }
            }

        val today = LocalDate.now()
        val attendances = (1..yearMonth.lengthOfMonth()).associate { day ->
            val date = yearMonth.atDay(day)
            val value = when {
                date.isAfter(today) -> null
                else -> sessionsByDate[date] ?: 0
            }
            date to value
        }

        val recentSevenDaysStart = LocalDate.now().minusDays(6)
        val previousSevenDaysStart = LocalDate.now().minusDays(13)
        val previousSevenDaysEnd = LocalDate.now().minusDays(7)

        val recentSessions = sessionJpaRepository.findByUserAndCreatedAtBetween(
            user,
            recentSevenDaysStart.atStartOfDay(),
            LocalDate.now().atTime(23, 59, 59)
        )

        val previousSessions = sessionJpaRepository.findByUserAndCreatedAtBetween(
            user,
            previousSevenDaysStart.atStartOfDay(),
            previousSevenDaysEnd.atTime(23, 59, 59)
        )

        val title = calculateTitle(recentSessions, previousSessions)
        val content1 = calculateContent1(user, recentSessions)
        val content2 = calculateContent2(user, recentSessions)
        val subContent = calculateSubContent(recentSessions)

        return AttendanceResponse(
            attendances = attendances,
            title = title,
            content1 = content1,
            content2 = content2,
            subContent = subContent
        )
    }

    private fun calculateTitle(recentSessions: List<Session>, previousSessions: List<Session>): String {
        val recentBadTime = calculateTotalBadPostureTime(recentSessions)
        val previousBadTime = calculateTotalBadPostureTime(previousSessions)
        val recentGoodRatio = calculateGoodPostureRatio(recentSessions)
        val previousGoodRatio = calculateGoodPostureRatio(previousSessions)

        val badTimeIncreased = recentBadTime > previousBadTime
        val goodRatioDecreased = recentGoodRatio < previousGoodRatio

        return when {
            !badTimeIncreased && !goodRatioDecreased -> "잘하고 있어요!"
            badTimeIncreased && goodRatioDecreased -> "주의가 필요해요!"
            else -> "조금만 더 힘내봐요!"
        }
    }

    private fun calculateContent1(user: User, recentSessions: List<Session>): String {
        val recentSevenDaysStart = LocalDate.now().minusDays(6)
        val allUsersSessions = sessionJpaRepository.findByUserAndCreatedAtBetween(
            user,
            recentSevenDaysStart.atStartOfDay(),
            LocalDate.now().atTime(23, 59, 59)
        )

        val myAverageScore = recentSessions
            .mapNotNull { it.score?.toDouble() }
            .takeIf { it.isNotEmpty() }
            ?.average() ?: 50.0

        val allScores = allUsersSessions
            .mapNotNull { it.score?.toDouble() }
            .takeIf { it.isNotEmpty() } ?: listOf(50.0)

        val higherCount = allScores.count { score -> score < myAverageScore }
        val percentile = if (allScores.isNotEmpty()) {
            round((higherCount.toDouble() / allScores.size) * 100).toInt()
        } else {
            50
        }

        return "전체 사용자 중, 당신의 자세는 상위 ${percentile}%예요"
    }

    private fun calculateContent2(user: User, recentSessions: List<Session>): String {
        val recentSevenDaysStart = LocalDate.now().minusDays(6)
        val allUsersSessions = sessionJpaRepository.findByUserAndCreatedAtBetween(
            user,
            recentSevenDaysStart.atStartOfDay(),
            LocalDate.now().atTime(23, 59, 59)
        )

        val myGoodRatio = calculateGoodPostureRatio(recentSessions)
        val allUsersGoodRatio = calculateGoodPostureRatio(allUsersSessions)

        val difference = round(myGoodRatio - allUsersGoodRatio).toInt()

        return when {
            difference > 0 -> "다른 사용자보다 바른 자세 비율이 ${difference}% 높아요"
            difference < 0 -> "다른 사용자보다 거북목이 평균 ${-difference}% 더 적어요"
            else -> "다른 사용자와 비슷한 자세 비율이에요"
        }
    }

    private fun calculateSubContent(sessions: List<Session>): String {
        val averageLevel = sessions
            .flatMap { session ->
                val levelDurations = session.getLevelDurations()
                (1..6).flatMap { level ->
                    List((levelDurations[level] ?: 0L).toInt()) { level }
                }
            }
            .takeIf { it.isNotEmpty() }
            ?.average() ?: 3.0

        return when {
            averageLevel <= 1.5 -> "꼿꼿기린"
            averageLevel <= 2.5 -> "쑥쑥기린"
            averageLevel <= 3.5 -> "아기기린"
            averageLevel <= 4.5 -> "꾸부정거부기"
            else -> "뽀각거부기"
        }
    }

    private fun calculateTotalBadPostureTime(sessions: List<Session>): Long {
        return sessions.sumOf { session ->
            val levelDurations = session.getLevelDurations()
            (levelDurations[4] ?: 0L) + (levelDurations[5] ?: 0L) + (levelDurations[6] ?: 0L)
        }
    }

    private fun calculateGoodPostureRatio(sessions: List<Session>): Double {
        val totalDuration = sessions.sumOf { session ->
            session.getLevelDurations().values.sum()
        }.toDouble()

        if (totalDuration == 0.0) return 0.0

        val goodDuration = sessions.sumOf { session ->
            val levelDurations = session.getLevelDurations()
            (levelDurations[1] ?: 0L) + (levelDurations[2] ?: 0L) + (levelDurations[3] ?: 0L)
        }

        return (goodDuration / totalDuration) * 100.0
    }
}
