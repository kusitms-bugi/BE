package com.github.kusitms_bugi.domain.dashboard.presentation

import com.github.kusitms_bugi.domain.dashboard.presentation.dto.request.GetPeriodRequest
import com.github.kusitms_bugi.domain.dashboard.presentation.dto.response.*
import com.github.kusitms_bugi.global.response.ApiResponse
import com.github.kusitms_bugi.global.security.CustomUserDetails
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RestController

@RestController
class DashboardController : DashboardApi {

    override fun getAverageScore(
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ApiResponse<AverageScoreResponse> {
        TODO("Not yet implemented")
    }

    override fun getAttendance(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Validated request: GetPeriodRequest
    ): ApiResponse<AttendanceResponse> {
        TODO("Not yet implemented")
    }

    override fun getLevel(
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ApiResponse<LevelResponse> {
        TODO("Not yet implemented")
    }

    override fun getPostureGraph(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Validated request: GetPeriodRequest
    ): ApiResponse<PostureGraphResponse> {
        TODO("Not yet implemented")
    }

    override fun getHighlight(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Validated request: GetPeriodRequest
    ): ApiResponse<HighlightResponse> {
        TODO("Not yet implemented")
    }

    override fun getPosturePattern(
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ApiResponse<PosturePatternResponse> {
        TODO("Not yet implemented")
    }
}
