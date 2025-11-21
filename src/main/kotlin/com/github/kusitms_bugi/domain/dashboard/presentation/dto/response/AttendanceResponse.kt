package com.github.kusitms_bugi.domain.dashboard.presentation.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "출석 현황 응답")
data class AttendanceResponse(
    @field:Schema(
        description = "출석 데이터 (오늘 이전 날짜는 데이터 없으면 0, 오늘 이후 날짜는 null)",
        example = "{\"2025-01-01\": 3, \"2025-01-02\": 5, \"2025-01-03\": 2, \"2025-01-25\": null}"
    )
    val attendances: Map<LocalDate, Int?>,

    @field:Schema(
        description = "피드백 제목",
        example = "잘하고 있어요!"
    )
    val title: String,

    @field:Schema(
        description = "전체 사용자와의 점수 비교",
        example = "전체 사용자 중, 당신의 자세는 상위 25%예요"
    )
    val content1: String,

    @field:Schema(
        description = "전체 사용자와의 자세 비율 비교",
        example = "다른 사용자보다 바른 자세 비율이 15% 높아요"
    )
    val content2: String,

    @field:Schema(
        description = "목 하중 상태",
        example = "꾸부정거부기"
    )
    val subContent: String
)
