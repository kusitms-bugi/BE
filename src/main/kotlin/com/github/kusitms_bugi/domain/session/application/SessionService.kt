package com.github.kusitms_bugi.domain.session.application

import com.github.kusitms_bugi.domain.dashboard.application.ScoreService
import com.github.kusitms_bugi.domain.session.domain.SessionStatus
import com.github.kusitms_bugi.domain.session.infrastructure.SessionRepositoryImpl
import com.github.kusitms_bugi.domain.session.infrastructure.jpa.Session
import com.github.kusitms_bugi.domain.session.infrastructure.jpa.SessionMetric
import com.github.kusitms_bugi.domain.session.infrastructure.jpa.SessionStatusHistory
import com.github.kusitms_bugi.domain.session.presentation.dto.request.MetricData
import com.github.kusitms_bugi.domain.session.presentation.dto.response.CreateSessionResponse
import com.github.kusitms_bugi.domain.session.presentation.dto.response.GetSessionReportResponse
import com.github.kusitms_bugi.domain.user.infrastructure.jpa.User
import com.github.kusitms_bugi.global.exception.ApiException
import com.github.kusitms_bugi.global.exception.SessionExceptionCode
import com.github.kusitms_bugi.global.security.CustomUserDetails
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class SessionService(
    private val sessionRepository: SessionRepositoryImpl,
    private val scoreService: ScoreService
) {

    @Transactional(readOnly = true)
    fun getSession(sessionId: UUID, userDetails: CustomUserDetails): GetSessionReportResponse {
        val session = sessionRepository.findByIdWithDetails(sessionId)
            ?: throw ApiException(SessionExceptionCode.SESSION_NOT_FOUND)

        if (session.user.id != userDetails.getId()) {
            throw ApiException(SessionExceptionCode.SESSION_ACCESS_DENIED)
        }

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
    fun pauseSession(sessionId: UUID, userDetails: CustomUserDetails) {
        val session = sessionRepository.findByIdWithStatusHistory(sessionId)
            ?: throw ApiException(SessionExceptionCode.SESSION_NOT_FOUND)

        if (session.user.id != userDetails.getId()) {
            throw ApiException(SessionExceptionCode.SESSION_ACCESS_DENIED)
        }

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
    fun resumeSession(sessionId: UUID, userDetails: CustomUserDetails) {
        val session = sessionRepository.findByIdWithStatusHistory(sessionId)
            ?: throw ApiException(SessionExceptionCode.SESSION_NOT_FOUND)

        if (session.user.id != userDetails.getId()) {
            throw ApiException(SessionExceptionCode.SESSION_ACCESS_DENIED)
        }

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
    fun stopSession(sessionId: UUID, userDetails: CustomUserDetails) {
        val session = sessionRepository.findByIdWithDetails(sessionId)
            ?: throw ApiException(SessionExceptionCode.SESSION_NOT_FOUND)

        if (session.user.id != userDetails.getId()) {
            throw ApiException(SessionExceptionCode.SESSION_ACCESS_DENIED)
        }

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
    fun saveMetrics(sessionId: UUID, userDetails: CustomUserDetails, request: Collection<MetricData>) {
        // statusHistory와 metrics 모두 필요하므로 findByIdWithDetails 사용
        val session = sessionRepository.findByIdWithDetails(sessionId)
            ?: throw ApiException(SessionExceptionCode.SESSION_NOT_FOUND)

        if (session.user.id != userDetails.getId()) {
            throw ApiException(SessionExceptionCode.SESSION_ACCESS_DENIED)
        }

        session.lastStatus()
            .takeIf { it == SessionStatus.STARTED || it == SessionStatus.RESUMED }
            ?: throw ApiException(SessionExceptionCode.SESSION_NOT_ACTIVE)

        request.forEach {
            SessionMetric(
                session = session,
                score = it.score,
                timestamp = it.timestamp
            ).let { metric -> session.metrics.add(metric) }
        }

        sessionRepository.save(session)
    }
}
