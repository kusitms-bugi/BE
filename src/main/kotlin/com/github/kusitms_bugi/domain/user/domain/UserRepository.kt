package com.github.kusitms_bugi.domain.user.domain

import java.util.*

interface UserRepository {
    fun save(user: User): User
    fun findById(id: UUID): User?
    fun findByEmail(email: String): User?
    fun findAll(): List<User>
}
