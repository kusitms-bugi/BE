package com.github.kusitms_bugi.domain.user.presentation

import com.github.kusitms_bugi.domain.user.application.UserService
import com.github.kusitms_bugi.domain.user.presentation.dto.request.*
import com.github.kusitms_bugi.domain.user.presentation.dto.response.CheckEmailDuplicateResponse
import com.github.kusitms_bugi.domain.user.presentation.dto.response.LoginResponse
import com.github.kusitms_bugi.domain.user.presentation.dto.response.RefreshTokenResponse
import com.github.kusitms_bugi.domain.user.presentation.dto.response.SignupResponse
import com.github.kusitms_bugi.global.response.ApiResponse
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(
    private val userService: UserService
) : UserApi {

    override fun checkEmailDuplicate(request: CheckEmailDuplicateRequest): ApiResponse<CheckEmailDuplicateResponse> {
        return ApiResponse.success(userService.checkEmailDuplicate(request))
    }

    override fun signup(request: SignupRequest): ApiResponse<SignupResponse> {
        return ApiResponse.success(userService.signup(request))
    }

    override fun verifyEmail(request: VerifyEmailRequest): ApiResponse<Unit> {
        userService.verifyEmail(request)
        return ApiResponse.success()
    }

    override fun resendVerificationEmail(request: ResendVerificationEmailRequest): ApiResponse<Unit> {
        userService.resendVerificationEmail(request)
        return ApiResponse.success()
    }

    override fun login(request: LoginRequest): ApiResponse<LoginResponse> {
        return ApiResponse.success(userService.login(request))
    }

    override fun refreshToken(request: RefreshTokenRequest): ApiResponse<RefreshTokenResponse> {
        return ApiResponse.success(userService.refreshToken(request))
    }
}
