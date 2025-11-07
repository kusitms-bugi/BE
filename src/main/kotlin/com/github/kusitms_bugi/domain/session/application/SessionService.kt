package com.github.kusitms_bugi.domain.session.application

import com.github.kusitms_bugi.domain.session.domain.*
import com.github.kusitms_bugi.domain.session.presentation.dto.request.SaveMetricsRequest
import com.github.kusitms_bugi.domain.session.presentation.dto.response.CreateSessionResponse
import com.github.kusitms_bugi.domain.session.presentation.dto.response.GetSessionResponse
import com.github.kusitms_bugi.domain.user.domain.User
import com.github.kusitms_bugi.global.exception.ApiException
import com.github.kusitms_bugi.global.exception.UserExceptionCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

@Service
class SessionService(
    private val sessionRepository: SessionRepository
) {

    @Transactional
    fun createSession(user: User): CreateSessionResponse {
        val session = Session(user = user)
        session.statusHistory.add(
            SessionStatusHistory(
                session = session,
                status = SessionStatus.STARTED,
                timestamp = LocalDateTime.now()
            )
        )

        return sessionRepository.save(session)
            .let { CreateSessionResponse(sessionId = it.id) }
    }

    @Transactional(readOnly = true)
    fun getSession(sessionId: UUID): GetSessionResponse {
        val session = sessionRepository.findById(sessionId)
            ?: throw ApiException(UserExceptionCode.USER_NOT_FOUND)

        val totalActiveTimeSeconds = calculateActiveTime(session)

        return GetSessionResponse(
            sessionId = session.id,
            totalActiveTimeSeconds = totalActiveTimeSeconds
        )
    }

    private fun calculateActiveTime(session: Session): Long {
        val history = session.statusHistory.sortedBy { it.timestamp }
        var totalSeconds = 0L
        var activeStartTime: LocalDateTime? = null

        history.forEach { event ->
            when (event.status) {
                SessionStatus.STARTED, SessionStatus.RESUMED -> {
                    activeStartTime = event.timestamp
                }
                SessionStatus.PAUSED, SessionStatus.STOPPED -> {
                    activeStartTime?.let { start ->
                        totalSeconds += ChronoUnit.SECONDS.between(start, event.timestamp)
                        activeStartTime = null
                    }
                }
            }
        }

        return totalSeconds
    }

    @Transactional
    fun pauseSession(sessionId: UUID) {
        val session = sessionRepository.findById(sessionId)
            ?: throw ApiException(UserExceptionCode.USER_NOT_FOUND)

        session.statusHistory.add(
            SessionStatusHistory(
                session = session,
                status = SessionStatus.PAUSED,
                timestamp = LocalDateTime.now()
            )
        )
    }

    @Transactional
    fun resumeSession(sessionId: UUID) {
        val session = sessionRepository.findById(sessionId)
            ?: throw ApiException(UserExceptionCode.USER_NOT_FOUND)

        session.statusHistory.add(
            SessionStatusHistory(
                session = session,
                status = SessionStatus.RESUMED,
                timestamp = LocalDateTime.now()
            )
        )
    }

    @Transactional
    fun stopSession(sessionId: UUID) {
        val session = sessionRepository.findById(sessionId)
            ?: throw ApiException(UserExceptionCode.USER_NOT_FOUND)

        session.statusHistory.add(
            SessionStatusHistory(
                session = session,
                status = SessionStatus.STOPPED,
                timestamp = LocalDateTime.now()
            )
        )
    }

    @Transactional
    fun saveMetrics(request: SaveMetricsRequest) {
        val session = sessionRepository.findById(request.sessionId)
            ?: throw ApiException(UserExceptionCode.USER_NOT_FOUND)

        request.metrics.forEach { metricData ->
            SessionMetric(
                session = session,
                score = metricData.score,
                timestamp = metricData.timestamp
            ).let { session.metrics.add(it) }
        }
    }
}
