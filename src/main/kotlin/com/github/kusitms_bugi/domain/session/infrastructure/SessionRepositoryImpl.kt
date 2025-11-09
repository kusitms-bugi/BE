package com.github.kusitms_bugi.domain.session.infrastructure

import com.github.kusitms_bugi.domain.session.domain.SessionRepository
import com.github.kusitms_bugi.domain.session.infrastructure.jpa.Session
import com.github.kusitms_bugi.domain.session.infrastructure.jpa.SessionJpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class SessionRepositoryImpl(
    private val sessionJpaRepository: SessionJpaRepository
) : SessionRepository {
    override fun findById(id: UUID): Session? {
        return sessionJpaRepository.findById(id).orElse(null)
    }

    override fun save(session: Session): Session {
        return sessionJpaRepository.save(session)
    }
}
