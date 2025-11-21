package com.github.kusitms_bugi.domain.dashboard.presentation.dto.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "레벨 도달 현황 응답")
data class LevelResponse(
    @field:Schema(description = "레벨", example = "1")
    val level: Int,

    @field:Schema(description = "현재 이동 거리", example = "400")
    val current: Int,

    @field:Schema(description = "필요한 거리", example = "1200")
    val required: Int,
)
