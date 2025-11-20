package com.github.kusitms_bugi.domain.dashboard.presentation

import com.github.kusitms_bugi.domain.dashboard.application.*
import com.github.kusitms_bugi.domain.dashboard.presentation.dto.request.GetPeriodRequest
import com.github.kusitms_bugi.domain.dashboard.presentation.dto.response.*
import com.github.kusitms_bugi.global.response.ApiResponse
import com.github.kusitms_bugi.global.security.CustomUserDetails
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RestController

@RestController
class DashboardController(
    private val averageScoreService: AverageScoreService,
    private val attendanceService: AttendanceService,
    private val levelService: LevelService,
    private val postureGraphService: PostureGraphService,
    private val highlightService: HighlightService,
    private val posturePatternService: PosturePatternService
) : DashboardApi {

    override fun getAverageScore(
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ApiResponse<AverageScoreResponse> {
        return ApiResponse.success(averageScoreService.getAverageScore(userDetails.user))
    }

    override fun getAttendance(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Validated request: GetPeriodRequest
    ): ApiResponse<AttendanceResponse> {
        return ApiResponse.success(attendanceService.getAttendance(userDetails.user, request))
    }

    override fun getLevel(
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ApiResponse<LevelResponse> {
        return ApiResponse.success(levelService.getLevel(userDetails.user))
    }

    override fun getPostureGraph(
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ApiResponse<PostureGraphResponse> {
        return ApiResponse.success(postureGraphService.getPostureGraph(userDetails.user))
    }

    override fun getHighlight(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Validated request: GetPeriodRequest
    ): ApiResponse<HighlightResponse> {
        return ApiResponse.success(highlightService.getHighlight(userDetails.user, request))
    }

    override fun getPosturePattern(
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ApiResponse<PosturePatternResponse> {
        return ApiResponse.success(posturePatternService.getPosturePattern(userDetails.user))
    }
}
