package com.github.kusitms_bugi.domain.dashboard.presentation.dto.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "하이라이트 응답")
data class HighlightResponse(
    @field:Schema(description = "이번 주/월 평균 사용 시간 (분)", example = "321")
    val current: Int,

    @field:Schema(description = "저번 주/월 평균 사용 시간 (분)", example = "257")
    val previous: Int,
)
