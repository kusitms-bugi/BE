package com.github.kusitms_bugi.domain.dashboard.application

import com.github.kusitms_bugi.domain.dashboard.presentation.dto.request.GetAttendanceRequest
import com.github.kusitms_bugi.domain.dashboard.presentation.dto.response.AttendanceResponse
import com.github.kusitms_bugi.domain.session.infrastructure.jpa.SessionJpaRepository
import com.github.kusitms_bugi.domain.user.infrastructure.jpa.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.round

data class DailyStats(
    val date: LocalDate,
    val activeMinutes: Int,
    val badDurationMillis: Long,
    val goodDurationMillis: Long,
    val totalDurationMillis: Long,
    val avgLevel: Double
)

@Service
@Transactional(readOnly = true)
class AttendanceService(
    private val sessionJpaRepository: SessionJpaRepository
) {

    fun getAttendance(user: User, request: GetAttendanceRequest): AttendanceResponse {
        val yearMonth = YearMonth.of(request.year, request.month)
        val today = LocalDate.now()

        val earliestDate = minOf(
            yearMonth.atDay(1),
            today.minusDays(13)
        )

        val allStats = sessionJpaRepository.getAttendanceStats(
            user.id,
            earliestDate.atStartOfDay(),
            yearMonth.atEndOfMonth().atTime(23, 59, 59)
        ).map { result ->
            DailyStats(
                date = LocalDate.parse(result[0] as String),
                activeMinutes = (result[1] as Number).toInt(),
                badDurationMillis = (result[2] as Number).toLong(),
                goodDurationMillis = (result[3] as Number).toLong(),
                totalDurationMillis = (result[4] as Number).toLong(),
                avgLevel = (result[5] as Number).toDouble()
            )
        }.associateBy { it.date }

        val attendances = (1..yearMonth.lengthOfMonth()).associate { day ->
            val date = yearMonth.atDay(day)
            val value = when {
                date.isAfter(today) -> null
                else -> allStats[date]?.activeMinutes ?: 0
            }
            date to value
        }

        val recentStats = (0..6).mapNotNull { offset ->
            allStats[today.minusDays(offset.toLong())]
        }

        val previousStats = (7..13).mapNotNull { offset ->
            allStats[today.minusDays(offset.toLong())]
        }

        val title = calculateTitle(recentStats, previousStats)
        val content1 = calculateContent1(recentStats, allStats.values.toList())
        val content2 = calculateContent2(recentStats, allStats.values.toList())
        val subContent = calculateSubContent(recentStats)

        return AttendanceResponse(
            attendances = attendances,
            title = title,
            content1 = content1,
            content2 = content2,
            subContent = subContent
        )
    }

    private fun calculateTitle(recentStats: List<DailyStats>, previousStats: List<DailyStats>): String {
        val recentBadTime = recentStats.sumOf { it.badDurationMillis }
        val previousBadTime = previousStats.sumOf { it.badDurationMillis }
        val recentGoodRatio = calculateGoodRatio(recentStats)
        val previousGoodRatio = calculateGoodRatio(previousStats)

        val badTimeIncreased = recentBadTime < previousBadTime
        val goodRatioDecreased = recentGoodRatio > previousGoodRatio

        return when {
            !badTimeIncreased && !goodRatioDecreased -> "잘하고 있어요!"
            badTimeIncreased && goodRatioDecreased -> "주의가 필요해요!"
            else -> "조금만 더 힘내봐요!"
        }
    }

    private fun calculateContent1(recentStats: List<DailyStats>, allStats: List<DailyStats>): String {
        // Use average level as a proxy for score comparison
        val myAverageLevel = recentStats
            .filter { it.avgLevel > 0 }
            .map { it.avgLevel }
            .takeIf { it.isNotEmpty() }
            ?.average() ?: 3.5

        val allLevels = allStats
            .filter { it.avgLevel > 0 }
            .map { it.avgLevel }
            .takeIf { it.isNotEmpty() } ?: listOf(3.5)

        val worseCount = allLevels.count { level -> level > myAverageLevel }
        val percentile = if (allLevels.isNotEmpty()) {
            round((worseCount.toDouble() / allLevels.size) * 100).toInt()
        } else {
            50
        }

        return "전체 사용자 중, 당신의 자세는 상위 ${percentile}%예요"
    }

    private fun calculateContent2(recentStats: List<DailyStats>, allStats: List<DailyStats>): String {
        val myGoodRatio = calculateGoodRatio(recentStats)
        val allUsersGoodRatio = calculateGoodRatio(allStats)

        val difference = round(myGoodRatio - allUsersGoodRatio).toInt()

        return when {
            difference > 0 -> "다른 사용자보다 바른 자세 비율이 ${difference}% 높아요"
            difference < 0 -> "다른 사용자보다 거북목이 평균 ${-difference}% 더 적어요"
            else -> "다른 사용자와 비슷한 자세 비율이에요"
        }
    }

    private fun calculateSubContent(stats: List<DailyStats>): String {
        val averageLevel = stats
            .filter { it.avgLevel > 0 }
            .map { it.avgLevel }
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

    private fun calculateGoodRatio(stats: List<DailyStats>): Double {
        val totalDuration = stats.sumOf { it.totalDurationMillis }.toDouble()
        if (totalDuration == 0.0) return 0.0

        val goodDuration = stats.sumOf { it.goodDurationMillis }
        return (goodDuration / totalDuration) * 100.0
    }
}
