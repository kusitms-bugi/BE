package com.github.kusitms_bugi.domain.user.infrastructure

import com.github.kusitms_bugi.domain.user.domain.User
import com.github.kusitms_bugi.domain.user.domain.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class UserRepositoryImpl(
    private val userJpaRepository: UserJpaRepository
) : UserRepository {
    override fun save(user: User): User {
        return userJpaRepository.save(user)
    }

    override fun findById(id: UUID): User? {
        return userJpaRepository.findByIdOrNull(id)
    }

    override fun findByEmail(email: String): User? {
        return userJpaRepository.findByEmail(email)
    }

    override fun findAll(): List<User> {
        return userJpaRepository.findAll()
    }
}
