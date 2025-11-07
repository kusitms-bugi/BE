package com.github.kusitms_bugi.domain.session.domain

import java.util.*

interface SessionRepository {
    fun save(session: Session): Session
    fun findById(id: UUID): Session?
    fun findByUserId(userId: UUID): List<Session>
}
