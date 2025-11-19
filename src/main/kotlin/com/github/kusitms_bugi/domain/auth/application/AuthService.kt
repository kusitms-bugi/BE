package com.github.kusitms_bugi.domain.auth.application

import com.github.kusitms_bugi.domain.auth.presentation.dto.request.CheckEmailDuplicateRequest
import com.github.kusitms_bugi.domain.auth.presentation.dto.request.LoginRequest
import com.github.kusitms_bugi.domain.auth.presentation.dto.request.RefreshTokenRequest
import com.github.kusitms_bugi.domain.auth.presentation.dto.request.ResendVerificationEmailRequest
import com.github.kusitms_bugi.domain.auth.presentation.dto.request.SignupRequest
import com.github.kusitms_bugi.domain.auth.presentation.dto.request.VerifyEmailRequest
import com.github.kusitms_bugi.domain.auth.presentation.dto.request.toEntity
import com.github.kusitms_bugi.domain.auth.presentation.dto.response.CheckEmailDuplicateResponse
import com.github.kusitms_bugi.domain.auth.presentation.dto.response.LoginResponse
import com.github.kusitms_bugi.domain.auth.presentation.dto.response.RefreshTokenResponse
import com.github.kusitms_bugi.domain.auth.presentation.dto.response.SignupResponse
import com.github.kusitms_bugi.domain.auth.presentation.dto.response.toResponse
import com.github.kusitms_bugi.domain.user.domain.UserRepository
import com.github.kusitms_bugi.global.exception.ApiException
import com.github.kusitms_bugi.global.exception.UserExceptionCode
import com.github.kusitms_bugi.global.mail.EmailService
import com.github.kusitms_bugi.global.security.EmailVerificationTokenProvider
import com.github.kusitms_bugi.global.security.JwtTokenProvider
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val emailService: EmailService,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider,
    private val emailVerificationTokenProvider: EmailVerificationTokenProvider
) {

    @Transactional(readOnly = true)
    fun checkEmailDuplicate(request: CheckEmailDuplicateRequest) =
        CheckEmailDuplicateResponse(
            isDuplicate = userRepository.findByEmail(request.email) != null
        )

    @Transactional
    fun signup(request: SignupRequest): SignupResponse {
        userRepository.findByEmail(request.email)?.let {
            throw ApiException(UserExceptionCode.EMAIL_ALREADY_EXISTS)
        }

        return request
            .toEntity(password = passwordEncoder.encode(request.password))
            .let { userRepository.save(it) }
            .also { user ->
                emailVerificationTokenProvider.generateEmailVerificationToken(user.id)
                    .let { token -> emailService.sendVerificationEmail(user.email, token, request.callbackUrl) }
            }
            .toResponse()
    }

    @Transactional
    fun verifyEmail(request: VerifyEmailRequest) {
        request.token
            .takeIf { emailVerificationTokenProvider.validateToken(it) }
            ?: throw ApiException(UserExceptionCode.INVALID_TOKEN)

        emailVerificationTokenProvider.getUserIdFromToken(request.token)
            .let { userRepository.findById(it) ?: throw ApiException(UserExceptionCode.USER_NOT_FOUND) }
            .apply { active = true }
    }

    @Transactional
    fun resendVerificationEmail(request: ResendVerificationEmailRequest) =
        (userRepository.findByEmail(request.email) ?: throw ApiException(UserExceptionCode.USER_NOT_FOUND))
            .also { if (it.active) throw ApiException(UserExceptionCode.USER_ALREADY_ACTIVE) }
            .let { user ->
                emailVerificationTokenProvider.generateEmailVerificationToken(user.id)
                    .let { token -> emailService.sendVerificationEmail(user.email, token, request.callbackUrl) }
            }

    @Transactional(readOnly = true)
    fun login(request: LoginRequest): LoginResponse =
        (userRepository.findByEmail(request.email)
            ?.takeIf { passwordEncoder.matches(request.password, it.password) }
            ?: throw ApiException(UserExceptionCode.USER_NOT_FOUND))
            .also { if (!it.active) throw ApiException(UserExceptionCode.USER_NOT_ACTIVE) }
            .let {
                LoginResponse(
                    accessToken = jwtTokenProvider.generateAccessToken(it.id),
                    refreshToken = jwtTokenProvider.generateRefreshToken(it.id)
                )
            }

    @Transactional(readOnly = true)
    fun refreshToken(request: RefreshTokenRequest): RefreshTokenResponse =
        request.refreshToken
            .also { if (!jwtTokenProvider.validateRefreshToken(it)) throw ApiException(UserExceptionCode.INVALID_REFRESH_TOKEN) }
            .let { jwtTokenProvider.getUserIdFromRefreshToken(it) }
            .let { userRepository.findById(it) ?: throw ApiException(UserExceptionCode.USER_NOT_FOUND) }
            .also { if (!it.active) throw ApiException(UserExceptionCode.USER_NOT_ACTIVE) }
            .let {
                RefreshTokenResponse(
                    accessToken = jwtTokenProvider.generateAccessToken(it.id),
                    refreshToken = jwtTokenProvider.generateRefreshToken(it.id)
                )
            }
}
