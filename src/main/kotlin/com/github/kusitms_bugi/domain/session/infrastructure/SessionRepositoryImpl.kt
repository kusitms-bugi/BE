package com.github.kusitms_bugi.domain.session.infrastructure

import com.github.kusitms_bugi.domain.session.domain.Session
import com.github.kusitms_bugi.domain.session.domain.SessionRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class SessionRepositoryImpl(
    private val sessionJpaRepository: SessionJpaRepository
) : SessionRepository {
    override fun save(session: Session): Session {
        return sessionJpaRepository.save(session)
    }

    override fun findById(id: UUID): Session? {
        return sessionJpaRepository.findByIdOrNull(id)
    }

    override fun findByUserId(userId: UUID): List<Session> {
        return sessionJpaRepository.findByUserId(userId)
    }
}
