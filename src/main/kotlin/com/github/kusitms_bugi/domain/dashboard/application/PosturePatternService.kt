package com.github.kusitms_bugi.domain.dashboard.application

import com.github.kusitms_bugi.domain.dashboard.presentation.dto.response.PosturePatternResponse
import com.github.kusitms_bugi.domain.session.infrastructure.jpa.SessionJpaRepository
import com.github.kusitms_bugi.domain.user.infrastructure.jpa.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

data class PosturePatternStats(
    val hourOfDay: Int,
    val dayOfWeek: Int,
    val badPostureDurationMillis: Long,
    val goodPostureDurationMillis: Long,
    val recoveryCount: Int,
    val avgRecoveryTimeMillis: Long
)

@Service
@Transactional(readOnly = true)
class PosturePatternService(
    private val sessionJpaRepository: SessionJpaRepository
) {

    fun getPosturePattern(user: User): PosturePatternResponse {
        val sevenDaysAgo = LocalDate.now().minusDays(6)

        val stats = sessionJpaRepository.getPosturePatternStats(
            user.id,
            sevenDaysAgo.atStartOfDay(),
            LocalDate.now().atTime(23, 59, 59)
        ).map { result ->
            PosturePatternStats(
                hourOfDay = (result[0] as Number).toInt(),
                dayOfWeek = (result[1] as Number).toInt(),
                badPostureDurationMillis = (result[2] as Number).toLong(),
                goodPostureDurationMillis = (result[3] as Number).toLong(),
                recoveryCount = (result[4] as Number).toInt(),
                avgRecoveryTimeMillis = (result[5] as Number).toLong()
            )
        }

        val worstTime = calculateWorstTimeFromStats(stats)
        val worstDay = calculateWorstDayFromStats(stats)
        val recovery = calculateRecoveryFromStats(stats)

        return PosturePatternResponse(
            worstTime = worstTime,
            worstDay = worstDay,
            recovery = recovery,
            stretching = getRandomStretching(user)
        )
    }

    private fun calculateWorstTimeFromStats(stats: List<PosturePatternStats>): LocalTime {
        val goodPostureDurationByHour = stats
            .groupBy { it.hourOfDay }
            .mapValues { (_, hourStats) -> hourStats.sumOf { it.goodPostureDurationMillis } }

        val worstHour = goodPostureDurationByHour.maxByOrNull { it.value }?.key ?: 12
        return LocalTime.of(worstHour, 0, 0)
    }

    private fun calculateWorstDayFromStats(stats: List<PosturePatternStats>): DayOfWeek {
        val goodPostureDurationByDay = stats
            .groupBy { it.dayOfWeek }
            .mapValues { (_, dayStats) -> dayStats.sumOf { it.goodPostureDurationMillis } }

        val worstDayNum = goodPostureDurationByDay.maxByOrNull { it.value }?.key ?: 1
        return when (worstDayNum) {
            0 -> DayOfWeek.SUNDAY
            else -> DayOfWeek.of(worstDayNum)
        }
    }

    private fun calculateRecoveryFromStats(stats: List<PosturePatternStats>): Int {
        val totalRecoveryTime = stats.sumOf { it.avgRecoveryTimeMillis * it.recoveryCount }
        val totalRecoveryCount = stats.sumOf { it.recoveryCount }

        if (totalRecoveryCount == 0) return 0

        val averageRecoveryMillis = totalRecoveryTime.toDouble() / totalRecoveryCount
        return (averageRecoveryMillis / 1000).toInt()
    }

    private fun getRandomStretching(user: User): String {
        val today = LocalDate.now()
        val seed = "${user.id}-${today}".hashCode()
        val index = (seed and 0x7fffffff) % STRETCHING_LIST.size
        return STRETCHING_LIST[index]
    }

    companion object {
        private val STRETCHING_LIST = listOf(
            "턱 당기기",
            "목 측면 스트레칭",
            "목 전방 굴곡 스트레칭",
            "목 후방 신전",
            "날개뼈 모으기",
            "벽 밀기",
            "가슴 펴기",
            "손깍지 끼고 가슴 열기",
            "흉추 신전",
            "고양이-낙타 자세"
        )
    }
}
