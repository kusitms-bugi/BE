package com.github.kusitms_bugi.domain.session.presentation.dto.request

import java.time.LocalDateTime
import java.util.*

data class SaveMetricsRequest(
    val sessionId: UUID,
    val metrics: List<MetricData>
)

data class MetricData(
    val score: Double,
    val timestamp: LocalDateTime
)
