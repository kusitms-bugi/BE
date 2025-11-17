package com.github.kusitms_bugi.domain.dashboard.presentation.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "출석 현황 응답")
data class AttendanceResponse(
    @field:Schema(
        description = "출석 데이터",
        example = "{\"2025-01-01\": 3, \"2025-01-02\": 5, \"2025-01-03\": 2}"
    )
    val attendances: Map<LocalDate, Int>
)
