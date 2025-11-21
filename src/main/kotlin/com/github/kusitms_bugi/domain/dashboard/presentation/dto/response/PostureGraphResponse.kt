package com.github.kusitms_bugi.domain.dashboard.presentation.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "바른 자세 점수 그래프 응답")
data class PostureGraphResponse(
    @field:Schema(
        description = "시계열 데이터 포인트 (날짜: 점수)",
        example = "{\"2025-01-01\": 85, \"2025-01-02\": 92, \"2025-01-03\": 78}"
    )
    val points: Map<LocalDate, Int>
)