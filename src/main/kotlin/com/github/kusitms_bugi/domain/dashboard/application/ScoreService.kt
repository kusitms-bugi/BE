package com.github.kusitms_bugi.domain.dashboard.application

import com.github.kusitms_bugi.domain.session.infrastructure.jpa.Session
import com.github.kusitms_bugi.domain.session.infrastructure.jpa.SessionMetric
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDateTime
import kotlin.math.*

@Service
class ScoreService {

    fun calculateScoreFromSession(session: Session): Int {
        val sessionLengthMinutes = calculateSessionLengthMinutes(session)
        if (sessionLengthMinutes < 5.0) return 50

        val activeMetrics = session.getActiveMetrics()
        val levelDurations = session.getLevelDurations()

        val raw = calculateRawScore(levelDurations)
        val goodStreakBonus = calculateGoodStreakBonus(session, activeMetrics, sessionLengthMinutes)
        val badStreakPenalty = calculateBadStreakPenalty(session, activeMetrics, sessionLengthMinutes)
        val transitionPenalty = calculateTransitionPenalty(activeMetrics, sessionLengthMinutes)
        val usageBoost = calculateUsageBoost(sessionLengthMinutes)
        val tilt = calculateTilt(levelDurations, sessionLengthMinutes)

        val preScore = raw + goodStreakBonus + badStreakPenalty + transitionPenalty + usageBoost + tilt
        val stabilized = stabilizeForShortSession(preScore, sessionLengthMinutes)
        val adaptiveFloor = calculateAdaptiveFloor(levelDurations, sessionLengthMinutes)
        val finalScore = max(adaptiveFloor, stabilized)

        return round(finalScore).toInt().coerceIn(1, 100)
    }

    private fun calculateSessionLengthMinutes(session: Session): Double {
        return session.calculateTotalActiveSeconds() / 60.0
    }

    private fun calculateFlips(activeMetrics: List<SessionMetric>): Int {
        if (activeMetrics.size < 2) return 0

        var flips = 0
        var previousSide = if (activeMetrics.first().score < 3.0) "good" else "bad"

        for (i in 1 until activeMetrics.size) {
            val currentSide = if (activeMetrics[i].score < 3.0) "good" else "bad"
            if (currentSide != previousSide) {
                flips++
                previousSide = currentSide
            }
        }

        return flips
    }

    private fun extractStreaks(
        activeMetrics: List<SessionMetric>,
        session: Session,
        condition: (Int) -> Boolean
    ): List<Double> {
        if (activeMetrics.isEmpty()) return emptyList()

        val streaks = mutableListOf<Double>()
        var streakStartTime: LocalDateTime? = null

        activeMetrics.forEach { metric ->
            if (condition(metric.score)) {
                if (streakStartTime == null) {
                    streakStartTime = metric.timestamp
                }
            } else {
                streakStartTime?.let { start ->
                    val activeDuration = calculateActiveDuration(session, start, metric.timestamp)
                    if (activeDuration > 0) {
                        streaks.add(activeDuration)
                    }
                    streakStartTime = null
                }
            }
        }

        streakStartTime?.let { start ->
            val lastMetric = activeMetrics.last()
            val activeDuration = calculateActiveDuration(session, start, lastMetric.timestamp)
            if (activeDuration > 0) {
                streaks.add(activeDuration)
            }
        }

        return streaks
    }

    private fun calculateActiveDuration(session: Session, start: LocalDateTime, end: LocalDateTime): Double {
        val duration = Duration.between(start, end)
        val pausedDuration = session.calculatePausedDuration(start, end)
        return duration.minus(pausedDuration).toMillis() / 1000.0 / 60.0
    }

    private fun calculateRawScore(levelDurations: Map<Int, Long>): Double {
        val totalDuration = levelDurations.values.sum().toDouble()
        if (totalDuration == 0.0) return 50.0

        val percentages = (1..6).map { level ->
            (levelDurations.getOrDefault(level, 0L) / totalDuration) * 100.0
        }

        val weights = listOf(30.0, 22.0, 12.0, -15.0, -40.0, -70.0)
        val weightedSum = percentages.zip(weights).sumOf { (percentage, weight) ->
            percentage * weight
        }

        return 50.0 + weightedSum / 100.0
    }

    private fun calculateGoodStreakBonus(
        session: Session,
        activeMetrics: List<SessionMetric>,
        sessionLength: Double
    ): Double {
        val goodStreaks = extractStreaks(activeMetrics, session) { score -> score >= 4 }
        if (goodStreaks.isEmpty()) return 0.0

        val baseGood = goodStreaks.sumOf { streak ->
            when {
                streak >= 20.0 -> 10.0
                streak >= 10.0 -> 5.0
                streak >= 5.0 -> 2.0
                else -> 0.0
            }
        }

        val maxStreak = goodStreaks.max()
        val goodFactor = 1 + 0.4 * min(1.0, maxStreak / 60.0)
        val capGood = 18.0 + 10.0 * min(1.0, sessionLength / 480.0)

        return min(capGood, baseGood * goodFactor)
    }

    private fun calculateBadStreakPenalty(
        session: Session,
        activeMetrics: List<SessionMetric>,
        sessionLength: Double
    ): Double {
        val badStreaks = extractStreaks(activeMetrics, session) { score -> score <= 3 }
        if (badStreaks.isEmpty()) return 0.0

        val baseBad = badStreaks.sumOf { streak ->
            when {
                streak >= 20.0 -> -20.0
                streak >= 10.0 -> -10.0
                streak >= 3.0 -> -4.0
                else -> 0.0
            }
        }

        val maxStreak = badStreaks.max()
        val badFactorBase = 1 + 0.8 * min(1.0, maxStreak / 30.0)
        val badFactorAdj = badFactorBase * min(1.0, sessionLength / 240.0)
        val capBad = 12.0 + 23.0 * min(1.0, sessionLength / 240.0)

        return -min(capBad, abs(baseBad) * badFactorAdj)
    }

    private fun calculateTransitionPenalty(activeMetrics: List<SessionMetric>, sessionLength: Double): Double {
        val flips = calculateFlips(activeMetrics)
        val threshold = 8.0 * (sessionLength / 60.0)
        return -max(0.0, flips - threshold)
    }

    private fun calculateUsageBoost(sessionLength: Double): Double {
        return 12.0 * (1 - exp(-sessionLength / 120.0))
    }

    private fun calculateTilt(levelDurations: Map<Int, Long>, sessionLength: Double): Double {
        val totalDuration = levelDurations.values.sum().toDouble()
        if (totalDuration == 0.0) return 0.0

        val p1 = (levelDurations.getOrDefault(1, 0L) / totalDuration) * 100.0
        val p2 = (levelDurations.getOrDefault(2, 0L) / totalDuration) * 100.0

        val sGood = (p1 + p2) / 100.0
        val lengthFactor = min(1.0, sessionLength / 480.0)

        return lengthFactor * (18.0 * (sGood - 0.5) - 5.0 * max(0.0, sGood - 0.8))
    }

    private fun stabilizeForShortSession(preScore: Double, sessionLength: Double): Double {
        val stabilizationFactor = min(1.0, sessionLength / 20.0)
        return 50.0 + stabilizationFactor * (preScore - 50.0)
    }

    private fun calculateAdaptiveFloor(levelDurations: Map<Int, Long>, sessionLength: Double): Double {
        val totalDuration = levelDurations.values.sum().toDouble()
        if (totalDuration == 0.0) return 10.0

        val p1 = (levelDurations.getOrDefault(1, 0L) / totalDuration) * 100.0
        val p2 = (levelDurations.getOrDefault(2, 0L) / totalDuration) * 100.0
        val p3 = (levelDurations.getOrDefault(3, 0L) / totalDuration) * 100.0

        val mixFloor = 10.0 + 0.2 * (p1 + p2) + 0.1 * p3
        val timeScaler = 1.0 - exp(-sessionLength / 60.0)

        return round(timeScaler * mixFloor)
    }
}