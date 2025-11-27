package com.github.kusitms_bugi.domain.dashboard.presentation.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import java.time.DayOfWeek
import java.time.LocalTime

@Schema(description = "자세 패턴 분석 응답")
data class PosturePatternResponse(
    @field:Schema(description = "안 좋은 시간대", example = "14:00:00")
    val worstTime: LocalTime,

    @field:Schema(description = "안 좋은 요일", example = "FRIDAY")
    val worstDay: DayOfWeek,

    @field:Schema(description = "회복 탄력성 (분)", example = "3")
    val recovery: Int,

    @field:Schema(description = "추천 스트레칭", example = "목돌리기")
    val stretching: String,
)