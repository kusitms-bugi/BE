package com.github.kusitms_bugi.domain.dashboard.presentation.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import org.springdoc.core.annotations.ParameterObject

@ParameterObject
@Schema(description = "하이라이트 조회 요청 DTO")
data class GetHighlightRequest(
    @field:Schema(description = "조회 기간")
    @field:NotNull(message = "조회 기간은 필수입니다")
    var period: Period
) {
    enum class Period {
        WEEKLY, MONTHLY
    }
}
