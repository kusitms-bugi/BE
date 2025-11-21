package com.github.kusitms_bugi.domain.session.presentation

import com.github.kusitms_bugi.domain.session.presentation.dto.request.MetricData
import com.github.kusitms_bugi.domain.session.presentation.dto.response.CreateSessionResponse
import com.github.kusitms_bugi.domain.session.presentation.dto.response.GetSessionReportResponse
import com.github.kusitms_bugi.global.response.ApiResponse
import com.github.kusitms_bugi.global.security.CustomUserDetails
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.util.*

@Tag(name = "세션")
@RequestMapping("/sessions")
interface SessionApi {

    @Operation(summary = "세션 조회")
    @GetMapping("/{sessionId}/report")
    fun getSessionReport(
        @Parameter(description = "세션 ID", schema = Schema(type = "string", format = "uuid")) @PathVariable sessionId: UUID,
        @Parameter(hidden = true) @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ApiResponse<GetSessionReportResponse>

    @Operation(summary = "세션 생성")
    @PostMapping
    fun createSession(
        @Parameter(hidden = true) @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ApiResponse<CreateSessionResponse>

    @Operation(summary = "세션 일시정지")
    @PatchMapping("/{sessionId}/pause")
    fun pauseSession(
        @Parameter(description = "세션 ID", schema = Schema(type = "string", format = "uuid")) @PathVariable sessionId: UUID,
        @Parameter(hidden = true) @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ApiResponse<Unit>

    @Operation(summary = "세션 재개")
    @PatchMapping("/{sessionId}/resume")
    fun resumeSession(
        @Parameter(description = "세션 ID", schema = Schema(type = "string", format = "uuid")) @PathVariable sessionId: UUID,
        @Parameter(hidden = true) @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ApiResponse<Unit>

    @Operation(summary = "세션 중단")
    @PatchMapping("/{sessionId}/stop")
    fun stopSession(
        @Parameter(description = "세션 ID", schema = Schema(type = "string", format = "uuid")) @PathVariable sessionId: UUID,
        @Parameter(hidden = true) @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ApiResponse<Unit>

    @Operation(summary = "세션 메트릭 저장")
    @PostMapping("/{sessionId}/metrics")
    fun saveMetrics(
        @Parameter(description = "세션 ID", schema = Schema(type = "string", format = "uuid")) @PathVariable sessionId: UUID,
        @Parameter(hidden = true) @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Validated @RequestBody request: List<MetricData>
    ): ApiResponse<Unit>
}