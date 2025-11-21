package com.github.kusitms_bugi.domain.dashboard.application

import com.github.kusitms_bugi.domain.dashboard.presentation.dto.response.AverageScoreResponse
import com.github.kusitms_bugi.domain.session.infrastructure.jpa.SessionJpaRepository
import com.github.kusitms_bugi.domain.user.infrastructure.jpa.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class AverageScoreService(
    private val sessionJpaRepository: SessionJpaRepository
) {

    fun getAverageScore(user: User): AverageScoreResponse {
        val thirtyDaysAgo = LocalDateTime.now().minusDays(ANALYSIS_PERIOD_DAYS)

        val averageScore = sessionJpaRepository.getAverageCompositeScore(
            user.id,
            thirtyDaysAgo
        ) ?: DEFAULT_SCORE

        return AverageScoreResponse(score = averageScore.coerceIn(MIN_SCORE, MAX_SCORE))
    }

    companion object {
        private const val ANALYSIS_PERIOD_DAYS = 30L
        private const val DEFAULT_SCORE = 50
        private const val MIN_SCORE = 1
        private const val MAX_SCORE = 100
    }
}
