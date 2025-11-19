package com.github.kusitms_bugi.domain.dashboard.application

import com.github.kusitms_bugi.domain.dashboard.presentation.dto.response.AverageScoreResponse
import com.github.kusitms_bugi.domain.session.infrastructure.jpa.Session
import com.github.kusitms_bugi.domain.session.infrastructure.jpa.SessionJpaRepository
import com.github.kusitms_bugi.domain.user.infrastructure.jpa.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import kotlin.math.round

@Service
@Transactional(readOnly = true)
class AverageScoreService(
    private val sessionJpaRepository: SessionJpaRepository
) {

    fun getAverageScore(user: User): AverageScoreResponse {
        val thirtyDaysAgo = LocalDateTime.now().minusDays(ANALYSIS_PERIOD_DAYS)

        val averageScore = sessionJpaRepository.findByUserAndCreatedAtAfter(user, thirtyDaysAgo)
            .takeIf { it.isNotEmpty() }
            ?.mapNotNull { session -> calculateCompositeScore(session) }
            ?.takeIf { it.isNotEmpty() }
            ?.average()
            ?.let { round(it).toInt().coerceIn(MIN_SCORE, MAX_SCORE) }
            ?: DEFAULT_SCORE

        return AverageScoreResponse(score = averageScore)
    }

    private fun calculateCompositeScore(session: Session): Double? {
        val finalScore = session.score?.toDouble() ?: return null
        val levelDurations = session.getLevelDurations()
        val totalDuration = levelDurations.values.sum().toDouble()

        val pz = when {
            totalDuration == 0.0 -> DEFAULT_PZ
            else -> {
                val goodLevelDuration = levelDurations[LEVEL_1].orZero() + levelDurations[LEVEL_2].orZero()
                (goodLevelDuration / totalDuration) * 100.0
            }
        }

        return (finalScore * FINAL_SCORE_WEIGHT) + (pz * PZ_WEIGHT)
    }

    private fun Long?.orZero() = this ?: 0L

    companion object {
        private const val ANALYSIS_PERIOD_DAYS = 30L
        private const val FINAL_SCORE_WEIGHT = 0.65
        private const val PZ_WEIGHT = 0.35
        private const val DEFAULT_SCORE = 50
        private const val DEFAULT_PZ = 50.0
        private const val MIN_SCORE = 1
        private const val MAX_SCORE = 100
        private const val LEVEL_1 = 1
        private const val LEVEL_2 = 2
    }
}
