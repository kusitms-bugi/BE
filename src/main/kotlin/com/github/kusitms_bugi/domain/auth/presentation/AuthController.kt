package com.github.kusitms_bugi.domain.auth.presentation

import com.github.kusitms_bugi.domain.auth.application.AuthService
import com.github.kusitms_bugi.domain.auth.presentation.dto.request.CheckEmailDuplicateRequest
import com.github.kusitms_bugi.domain.auth.presentation.dto.request.LoginRequest
import com.github.kusitms_bugi.domain.auth.presentation.dto.request.RefreshTokenRequest
import com.github.kusitms_bugi.domain.auth.presentation.dto.request.ResendVerificationEmailRequest
import com.github.kusitms_bugi.domain.auth.presentation.dto.request.SignupRequest
import com.github.kusitms_bugi.domain.auth.presentation.dto.request.VerifyEmailRequest
import com.github.kusitms_bugi.domain.auth.presentation.dto.response.CheckEmailDuplicateResponse
import com.github.kusitms_bugi.domain.auth.presentation.dto.response.LoginResponse
import com.github.kusitms_bugi.domain.auth.presentation.dto.response.RefreshTokenResponse
import com.github.kusitms_bugi.domain.auth.presentation.dto.response.SignupResponse
import com.github.kusitms_bugi.global.response.ApiResponse
import org.springframework.web.bind.annotation.RestController

@RestController
class AuthController(
    private val authService: AuthService
) : AuthApi {

    override fun checkEmailDuplicate(request: CheckEmailDuplicateRequest): ApiResponse<CheckEmailDuplicateResponse> {
        return ApiResponse.success(authService.checkEmailDuplicate(request))
    }

    override fun signup(request: SignupRequest): ApiResponse<SignupResponse> {
        return ApiResponse.success(authService.signup(request))
    }

    override fun verifyEmail(request: VerifyEmailRequest): ApiResponse<Unit> {
        authService.verifyEmail(request)
        return ApiResponse.success()
    }

    override fun resendVerificationEmail(request: ResendVerificationEmailRequest): ApiResponse<Unit> {
        authService.resendVerificationEmail(request)
        return ApiResponse.success()
    }

    override fun login(request: LoginRequest): ApiResponse<LoginResponse> {
        return ApiResponse.success(authService.login(request))
    }

    override fun refreshToken(request: RefreshTokenRequest): ApiResponse<RefreshTokenResponse> {
        return ApiResponse.success(authService.refreshToken(request))
    }
}
