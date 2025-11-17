package com.github.kusitms_bugi.domain.dashboard.presentation.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import org.springdoc.core.annotations.ParameterObject

@ParameterObject
@Schema(description = "출석 현황 조회 요청")
data class GetPeriodRequest(
    @field:Schema(description = "조회 기간")
    @field:NotNull(message = "조회 기간은 필수입니다")
    var period: Period,

    @field:Schema(description = "조회 년도")
    @field:NotNull(message = "년도는 필수입니다")
    var year: Int,

    @field:Schema(description = "조회 월")
    var month: Int? = null
) {
    enum class Period {
        MONTHLY, YEARLY
    }
}
