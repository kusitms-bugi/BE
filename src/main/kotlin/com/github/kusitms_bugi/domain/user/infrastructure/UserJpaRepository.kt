package com.github.kusitms_bugi.domain.user.infrastructure

import com.github.kusitms_bugi.domain.user.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface UserJpaRepository : JpaRepository<User, UUID> {
    fun findByEmail(email: String): User?
}
