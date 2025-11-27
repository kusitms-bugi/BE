package com.github.kusitms_bugi.domain.dashboard.application

import com.github.kusitms_bugi.domain.dashboard.presentation.dto.response.LevelResponse
import com.github.kusitms_bugi.domain.session.infrastructure.jpa.SessionJpaRepository
import com.github.kusitms_bugi.domain.user.infrastructure.jpa.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import kotlin.math.pow

@Service
@Transactional(readOnly = true)
class LevelService(
    private val sessionJpaRepository: SessionJpaRepository
) {

    fun getLevel(user: User): LevelResponse {
        val totalDistance = calculateTotalDistanceFromDb(user.id)
        val currentLevel = calculateLevel(totalDistance)
        val levelStartDistance = calculateLevelStartDistance(currentLevel)
        val currentProgress = (totalDistance - levelStartDistance).toInt()
        val requiredDistance = calculateRequiredDistance(currentLevel)

        return LevelResponse(
            level = currentLevel,
            current = currentProgress,
            required = requiredDistance
        )
    }

    private fun calculateTotalDistanceFromDb(userId: UUID): Double {
        val levelDurations = sessionJpaRepository.calculateLevelDurationsForUser(userId)
            .associate { result ->
                (result[0] as Number).toInt() to (result[1] as Number).toLong()
            }

        return LEVEL_SPEEDS.entries.sumOf { (level, speed) ->
            val durationMillis = levelDurations[level] ?: 0L
            val durationHours = durationMillis / 1000.0 / 3600.0
            durationHours * speed
        }
    }

    private fun calculateLevel(totalDistance: Double): Int {
        var accumulatedDistance = 0.0
        var level = 1

        while (level <= MAX_LEVEL) {
            val requiredForNextLevel = BASE_DISTANCE * (GROWTH_RATE.pow(level - 1))
            if (totalDistance < accumulatedDistance + requiredForNextLevel) {
                return level
            }
            accumulatedDistance += requiredForNextLevel
            level++
        }

        return MAX_LEVEL
    }

    private fun calculateLevelStartDistance(level: Int): Double {
        if (level == 1) return 0.0

        return (1 until level).sumOf { l ->
            BASE_DISTANCE * (GROWTH_RATE.pow(l - 1))
        }
    }

    private fun calculateRequiredDistance(level: Int): Int {
        return (BASE_DISTANCE * (GROWTH_RATE.pow(level - 1))).toInt()
    }

    companion object {
        private val LEVEL_SPEEDS = mapOf(
            1 to 3000.0,
            2 to 1500.0,
            3 to 500.0,
            4 to 200.0,
            5 to 50.0,
            6 to 0.1
        )

        private const val BASE_DISTANCE = 1500.0
        private const val GROWTH_RATE = 1.2
        private const val MAX_LEVEL = 100
    }
}
