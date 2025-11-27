package com.github.kusitms_bugi.domain.session.presentation.dto.request

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import java.time.LocalDateTime

data class MetricData(
    @field:Min(value = 1, message = "점수는 최소 1이어야 합니다.")
    @field:Max(value = 6, message = "점수는 최대 6이어야 합니다.")
    val score: Int,
    val timestamp: LocalDateTime
)
