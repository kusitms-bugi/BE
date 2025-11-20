package com.github.kusitms_bugi.domain.session.infrastructure.jpa

import com.github.kusitms_bugi.domain.user.infrastructure.jpa.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime
import java.util.*

interface SessionJpaRepository : JpaRepository<Session, UUID> {

    @Query("SELECT DISTINCT s FROM Session s LEFT JOIN FETCH s.statusHistory LEFT JOIN FETCH s.metrics WHERE s.id = :id")
    override fun findById(@Param("id") id: UUID): Optional<Session>

    @Query("SELECT DISTINCT s FROM Session s LEFT JOIN FETCH s.statusHistory LEFT JOIN FETCH s.metrics WHERE s.user = :user")
    fun findByUser(@Param("user") user: User): List<Session>

    @Query("SELECT DISTINCT s FROM Session s LEFT JOIN FETCH s.statusHistory LEFT JOIN FETCH s.metrics WHERE s.user = :user AND s.createdAt > :createdAt")
    fun findByUserAndCreatedAtAfter(@Param("user") user: User, @Param("createdAt") createdAt: LocalDateTime): List<Session>

    @Query("SELECT DISTINCT s FROM Session s LEFT JOIN FETCH s.statusHistory LEFT JOIN FETCH s.metrics WHERE s.user = :user AND s.createdAt BETWEEN :startDate AND :endDate")
    fun findByUserAndCreatedAtBetween(@Param("user") user: User, @Param("startDate") startDate: LocalDateTime, @Param("endDate") endDate: LocalDateTime): List<Session>
}
