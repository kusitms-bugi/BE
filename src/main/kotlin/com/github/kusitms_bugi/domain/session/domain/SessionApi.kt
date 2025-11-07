package com.github.kusitms_bugi.domain.session.domain

import com.github.kusitms_bugi.domain.session.presentation.dto.request.SaveMetricsRequest
import com.github.kusitms_bugi.domain.session.presentation.dto.response.CreateSessionResponse
import com.github.kusitms_bugi.domain.session.presentation.dto.response.GetSessionResponse
import com.github.kusitms_bugi.global.response.ApiResponse
import com.github.kusitms_bugi.global.security.CustomUserDetails
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.util.*

@Tag(name = "세션")
@RequestMapping("/sessions")
interface SessionApi {

    @Operation(summary = "세션 생성")
    @PostMapping
    fun createSession(@Parameter(hidden = true) @AuthenticationPrincipal userDetails: CustomUserDetails): ApiResponse<CreateSessionResponse>

    @Operation(summary = "세션 조회")
    @GetMapping("/{sessionId}")
    fun getSession(@PathVariable sessionId: UUID): ApiResponse<GetSessionResponse>

    @Operation(summary = "세션 일시정지")
    @PatchMapping("/{sessionId}/pause")
    fun pauseSession(@PathVariable sessionId: UUID): ApiResponse<Unit>

    @Operation(summary = "세션 재개")
    @PatchMapping("/{sessionId}/resume")
    fun resumeSession(@PathVariable sessionId: UUID): ApiResponse<Unit>

    @Operation(summary = "세션 중단")
    @PatchMapping("/{sessionId}/stop")
    fun stopSession(@PathVariable sessionId: UUID): ApiResponse<Unit>

    @Operation(summary = "세션 메트릭 저장")
    @PostMapping("/{sessionId}/metrics")
    fun saveMetrics(@PathVariable sessionId: UUID, @Validated @RequestBody request: SaveMetricsRequest): ApiResponse<Unit>
}
