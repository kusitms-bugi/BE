package com.github.kusitms_bugi.domain.dashboard.presentation

import com.github.kusitms_bugi.domain.dashboard.presentation.dto.request.GetAttendanceRequest
import com.github.kusitms_bugi.domain.dashboard.presentation.dto.request.GetHighlightRequest
import com.github.kusitms_bugi.domain.dashboard.presentation.dto.response.*
import com.github.kusitms_bugi.global.response.ApiResponse
import com.github.kusitms_bugi.global.security.CustomUserDetails
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Tag(name = "대시보드")
@RequestMapping("/dashboard")
interface DashboardApi {

    @Operation(summary = "평균 자세 점수 조회",)
    @GetMapping("/average-score")
    fun getAverageScore(
        @Parameter(hidden = true) @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ApiResponse<AverageScoreResponse>

    @Operation(summary = "출석 현황 조회")
    @GetMapping("/attendance")
    fun getAttendance(
        @Parameter(hidden = true) @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Validated request: GetAttendanceRequest
    ): ApiResponse<AttendanceResponse>

    @Operation(summary = "레벨 도달 현황 조회")
    @GetMapping("/level")
    fun getLevel(
        @Parameter(hidden = true) @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ApiResponse<LevelResponse>

    @Operation(summary = "바른 자세 점수 그래프 조회 (최근 31일)")
    @GetMapping("/posture-graph")
    fun getPostureGraph(
        @Parameter(hidden = true) @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ApiResponse<PostureGraphResponse>

    @Operation(summary = "하이라이트 조회")
    @GetMapping("/highlight")
    fun getHighlight(
        @Parameter(hidden = true) @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Validated request: GetHighlightRequest
    ): ApiResponse<HighlightResponse>

    @Operation(summary = "자세 패턴 분석 조회")
    @GetMapping("/posture-pattern")
    fun getPosturePattern(
        @Parameter(hidden = true) @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ApiResponse<PosturePatternResponse>
}
