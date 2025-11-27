package com.github.kusitms_bugi.domain.session.presentation

import com.github.kusitms_bugi.domain.session.application.SessionService
import com.github.kusitms_bugi.domain.session.presentation.dto.request.MetricData
import com.github.kusitms_bugi.domain.session.presentation.dto.response.CreateSessionResponse
import com.github.kusitms_bugi.domain.session.presentation.dto.response.GetSessionReportResponse
import com.github.kusitms_bugi.global.response.ApiResponse
import com.github.kusitms_bugi.global.security.CustomUserDetails
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
class SessionController(
    private val sessionService: SessionService
) : SessionApi {

    override fun getSessionReport(sessionId: UUID, @AuthenticationPrincipal userDetails: CustomUserDetails): ApiResponse<GetSessionReportResponse> {
        return ApiResponse.success(sessionService.getSession(sessionId, userDetails))
    }

    override fun createSession(userDetails: CustomUserDetails): ApiResponse<CreateSessionResponse> {
        return ApiResponse.success(sessionService.createSession(userDetails.user))
    }

    override fun pauseSession(sessionId: UUID, @AuthenticationPrincipal userDetails: CustomUserDetails): ApiResponse<Unit> {
        sessionService.pauseSession(sessionId, userDetails)
        return ApiResponse.success()
    }

    override fun resumeSession(sessionId: UUID, @AuthenticationPrincipal userDetails: CustomUserDetails): ApiResponse<Unit> {
        sessionService.resumeSession(sessionId, userDetails)
        return ApiResponse.success()
    }

    override fun stopSession(sessionId: UUID, @AuthenticationPrincipal userDetails: CustomUserDetails): ApiResponse<Unit> {
        sessionService.stopSession(sessionId, userDetails)
        return ApiResponse.success()
    }

    override fun saveMetrics(sessionId: UUID, @AuthenticationPrincipal userDetails: CustomUserDetails, request: List<MetricData>): ApiResponse<Unit> {
        sessionService.saveMetrics(sessionId, userDetails, request)
        return ApiResponse.success()
    }
}
