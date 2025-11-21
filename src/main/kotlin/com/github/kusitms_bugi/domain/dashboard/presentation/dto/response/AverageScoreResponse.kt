package com.github.kusitms_bugi.domain.dashboard.presentation.dto.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "평균 자세 점수 응답")
data class AverageScoreResponse(
    @field:Schema(description = "평균 자세 점수", example = "47")
    val score: Int
)
