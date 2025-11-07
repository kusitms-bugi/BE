package com.github.kusitms_bugi.domain.session.presentation

import com.github.kusitms_bugi.domain.session.application.SessionService
import com.github.kusitms_bugi.domain.session.domain.SessionApi
import com.github.kusitms_bugi.domain.session.presentation.dto.request.SaveMetricsRequest
import com.github.kusitms_bugi.domain.session.presentation.dto.response.CreateSessionResponse
import com.github.kusitms_bugi.domain.session.presentation.dto.response.GetSessionResponse
import com.github.kusitms_bugi.global.response.ApiResponse
import com.github.kusitms_bugi.global.security.CustomUserDetails
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
class SessionController(
    private val sessionService: SessionService
) : SessionApi {

    override fun createSession(userDetails: CustomUserDetails): ApiResponse<CreateSessionResponse> {
        return ApiResponse.success(sessionService.createSession(userDetails.user))
    }

    override fun getSession(sessionId: UUID): ApiResponse<GetSessionResponse> {
        return ApiResponse.success(sessionService.getSession(sessionId))
    }

    override fun pauseSession(sessionId: UUID): ApiResponse<Unit> {
        sessionService.pauseSession(sessionId)
        return ApiResponse.success()
    }

    override fun resumeSession(sessionId: UUID): ApiResponse<Unit> {
        sessionService.resumeSession(sessionId)
        return ApiResponse.success()
    }

    override fun stopSession(sessionId: UUID): ApiResponse<Unit> {
        sessionService.stopSession(sessionId)
        return ApiResponse.success()
    }

    override fun saveMetrics(sessionId: UUID, request: SaveMetricsRequest): ApiResponse<Unit> {
        sessionService.saveMetrics(request)
        return ApiResponse.success()
    }
}
