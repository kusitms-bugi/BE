package com.github.kusitms_bugi.domain.user.application

import com.github.kusitms_bugi.domain.user.domain.UserRepository
import com.github.kusitms_bugi.global.exception.ApiException
import com.github.kusitms_bugi.global.exception.UserExceptionCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class UserService(
    private val userRepository: UserRepository
) {

    @Transactional
    fun withdraw(userId: UUID) {
        val user = userRepository.findById(userId)
            ?: throw ApiException(UserExceptionCode.USER_NOT_FOUND)

        if (user.isDeleted()) {
            throw ApiException(UserExceptionCode.USER_ALREADY_WITHDRAWN)
        }

        user.delete()
    }
}
