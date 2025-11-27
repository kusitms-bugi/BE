package com.github.kusitms_bugi.domain.session.domain

import com.github.kusitms_bugi.domain.session.infrastructure.jpa.Session
import java.util.*

interface SessionRepository {
    fun findById(id: UUID): Session?
    fun save(session: Session): Session
}
