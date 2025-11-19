package com.github.kusitms_bugi.domain.session.infrastructure.jpa

import com.github.kusitms_bugi.domain.user.infrastructure.jpa.User
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime
import java.util.*

interface SessionJpaRepository : JpaRepository<Session, UUID> {

    override fun findById(id: UUID): Optional<Session>

    fun findByUser(user: User): List<Session>

    fun findByUserAndCreatedAtAfter(user: User, createdAt: LocalDateTime): List<Session>

    fun findByUserAndCreatedAtBetween(user: User, startDate: LocalDateTime, endDate: LocalDateTime): List<Session>
}