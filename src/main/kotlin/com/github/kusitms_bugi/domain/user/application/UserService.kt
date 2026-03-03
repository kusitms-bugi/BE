package com.github.kusitms_bugi.domain.user.application

import com.github.kusitms_bugi.domain.user.domain.UserRepository
import com.github.kusitms_bugi.domain.user.presentation.dto.request.SendDownloadEmailRequest
import com.github.kusitms_bugi.global.exception.ApiException
import com.github.kusitms_bugi.global.exception.UserExceptionCode
import com.github.kusitms_bugi.global.mail.EmailService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class UserService(
    private val userRepository: UserRepository,
    private val emailService: EmailService
) {

    @Transactional
    fun withdraw(userId: UUID) {
        val user = userRepository.findById(userId)
            ?: throw ApiException(UserExceptionCode.USER_NOT_FOUND)
        userRepository.deleteById(user.id)
    }

    fun sendDownloadEmail(request: SendDownloadEmailRequest) {
        emailService.sendDownloadEmail(request.email)
    }
}
