package com.github.kusitms_bugi.domain.session.presentation

import com.github.kusitms_bugi.domain.session.application.SessionService
import com.github.kusitms_bugi.domain.session.infrastructure.jpa.Session
import com.github.kusitms_bugi.domain.session.presentation.dto.request.SaveMetricsRequest
import com.github.kusitms_bugi.domain.session.presentation.dto.response.CreateSessionResponse
import com.github.kusitms_bugi.domain.session.presentation.dto.response.GetSessionReportResponse
import com.github.kusitms_bugi.global.response.ApiResponse
import com.github.kusitms_bugi.global.security.CustomUserDetails
import org.springframework.web.bind.annotation.RestController

@RestController
class SessionController(
    private val sessionService: SessionService
) : SessionApi {

    override fun getSessionReport(session: Session): ApiResponse<GetSessionReportResponse> {
        return ApiResponse.success(sessionService.getSession(session))
    }

    override fun createSession(userDetails: CustomUserDetails): ApiResponse<CreateSessionResponse> {
        return ApiResponse.success(sessionService.createSession(userDetails.user))
    }

    override fun pauseSession(session: Session): ApiResponse<Unit> {
        sessionService.pauseSession(session)
        return ApiResponse.success()
    }

    override fun resumeSession(session: Session): ApiResponse<Unit> {
        sessionService.resumeSession(session)
        return ApiResponse.success()
    }

    override fun stopSession(session: Session): ApiResponse<Unit> {
        sessionService.stopSession(session)
        return ApiResponse.success()
    }

    override fun saveMetrics(session: Session, request: SaveMetricsRequest): ApiResponse<Unit> {
        sessionService.saveMetrics(session, request)
        return ApiResponse.success()
    }
}
