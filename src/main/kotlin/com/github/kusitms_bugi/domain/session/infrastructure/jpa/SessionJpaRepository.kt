package com.github.kusitms_bugi.domain.session.infrastructure.jpa

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface SessionJpaRepository : JpaRepository<Session, UUID> {
    
    @EntityGraph("Session.withAll")
    override fun findById(id: UUID): Optional<Session>
}