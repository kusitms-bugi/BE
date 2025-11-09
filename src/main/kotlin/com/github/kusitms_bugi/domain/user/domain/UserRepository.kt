package com.github.kusitms_bugi.domain.user.domain

import com.github.kusitms_bugi.domain.user.infrastructure.jpa.User
import java.util.*

interface UserRepository {
    fun save(user: User): User
    fun findById(id: UUID): User?
    fun findByEmail(email: String): User?
}
