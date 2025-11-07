package com.github.kusitms_bugi.domain.session.infrastructure

import com.github.kusitms_bugi.domain.session.domain.Session
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface SessionJpaRepository : JpaRepository<Session, UUID> {
    fun findByUserId(userId: UUID): List<Session>
}
