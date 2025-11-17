package com.github.kusitms_bugi.domain.session.application

import com.github.kusitms_bugi.domain.dashboard.application.ScoreService
import com.github.kusitms_bugi.domain.session.domain.SessionRepository
import com.github.kusitms_bugi.domain.session.domain.SessionStatus
import com.github.kusitms_bugi.domain.session.infrastructure.jpa.Session
import com.github.kusitms_bugi.domain.session.infrastructure.jpa.SessionMetric
import com.github.kusitms_bugi.domain.session.infrastructure.jpa.SessionStatusHistory
import com.github.kusitms_bugi.domain.session.presentation.dto.request.SaveMetricsRequest
import com.github.kusitms_bugi.domain.session.presentation.dto.response.CreateSessionResponse
import com.github.kusitms_bugi.domain.session.presentation.dto.response.GetSessionReportResponse
import com.github.kusitms_bugi.domain.user.infrastructure.jpa.User
import com.github.kusitms_bugi.global.exception.ApiException
import com.github.kusitms_bugi.global.exception.SessionExceptionCode
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SessionService(
    private val sessionRepository: SessionRepository,
    private val scoreService: ScoreService
) {

    @Transactional(readOnly = true)
    @PreAuthorize("@sessionPermissionEvaluator.canAccessSession(principal, #session)")
    fun getSession(session: Session): GetSessionReportResponse {
        session.lastStatus()
            .takeIf { it == SessionStatus.STOPPED }
            ?: throw ApiException(SessionExceptionCode.SESSION_NOT_STOPPED)

        return GetSessionReportResponse(
            totalSeconds = session.calculateTotalActiveSeconds(),
            goodSeconds = session.calculateGoodSeconds(),
            score = scoreService.calculateScoreFromSession(session)
        )
    }

    @Transactional
    fun createSession(user: User): CreateSessionResponse {
        val session = Session(user = user)

        session.statusHistory.add(
            SessionStatusHistory(
                session = session,
                status = SessionStatus.STARTED
            )
        )

        return CreateSessionResponse(sessionId = sessionRepository.save(session).id)
    }

    @Transactional
    @PreAuthorize("@sessionPermissionEvaluator.canAccessSession(principal, #session)")
    fun pauseSession(session: Session) {
        session.lastStatus()
            .takeIf { it == SessionStatus.STARTED || it == SessionStatus.RESUMED }
            ?: throw ApiException(SessionExceptionCode.SESSION_NOT_RESUMED)

        session.statusHistory.add(
            SessionStatusHistory(
                session = session,
                status = SessionStatus.PAUSED
            )
        )

        sessionRepository.save(session)
    }

    @Transactional
    @PreAuthorize("@sessionPermissionEvaluator.canAccessSession(principal, #session)")
    fun resumeSession(session: Session) {
        session.lastStatus()
            .takeIf { it == SessionStatus.PAUSED }
            ?: throw ApiException(SessionExceptionCode.SESSION_NOT_PAUSED)

        session.statusHistory.add(
            SessionStatusHistory(
                session = session,
                status = SessionStatus.RESUMED
            )
        )

        sessionRepository.save(session)
    }

    @Transactional
    @PreAuthorize("@sessionPermissionEvaluator.canAccessSession(principal, #session)")
    fun stopSession(session: Session) {
        session.lastStatus()
            .takeUnless { it == SessionStatus.STOPPED }
            ?: throw ApiException(SessionExceptionCode.SESSION_ALREADY_STOPPED)

        session.statusHistory.add(
            SessionStatusHistory(
                session = session,
                status = SessionStatus.STOPPED
            )
        )

        session.score = scoreService.calculateScoreFromSession(session)

        sessionRepository.save(session)
    }

    @Transactional
    @PreAuthorize("@sessionPermissionEvaluator.canAccessSession(principal, #session)")
    fun saveMetrics(session: Session, request: SaveMetricsRequest) {
        session.lastStatus()
            .takeIf { it == SessionStatus.STARTED || it == SessionStatus.RESUMED }
            ?: throw ApiException(SessionExceptionCode.SESSION_NOT_ACTIVE)

        request.metrics.forEach { metricData ->
            SessionMetric(
                session = session,
                score = metricData.score,
                timestamp = metricData.timestamp
            ).let { session.metrics.add(it) }
        }

        sessionRepository.save(session)
    }
}
