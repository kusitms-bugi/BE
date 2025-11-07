package com.github.kusitms_bugi.domain.user.presentation

import com.github.kusitms_bugi.domain.user.presentation.dto.request.*
import com.github.kusitms_bugi.domain.user.presentation.dto.response.CheckEmailDuplicateResponse
import com.github.kusitms_bugi.domain.user.presentation.dto.response.LoginResponse
import com.github.kusitms_bugi.domain.user.presentation.dto.response.RefreshTokenResponse
import com.github.kusitms_bugi.domain.user.presentation.dto.response.SignupResponse
import com.github.kusitms_bugi.global.response.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@Tag(name = "인증")
@RequestMapping("/auth")
interface UserApi {

    @Operation(summary = "이메일 중복 확인")
    @PostMapping("/check-email")
    fun checkEmailDuplicate(@Validated @RequestBody request: CheckEmailDuplicateRequest): ApiResponse<CheckEmailDuplicateResponse>

    @Operation(summary = "회원가입")
    @PostMapping("/sign-up")
    fun signup(@Validated @RequestBody request: SignupRequest): ApiResponse<SignupResponse>

    @Operation(summary = "이메일 인증")
    @PostMapping("/verify-email")
    fun verifyEmail(@Validated @RequestBody request: VerifyEmailRequest): ApiResponse<Unit>

    @Operation(summary = "인증 메일 재전송")
    @PostMapping("/resend-verification-email")
    fun resendVerificationEmail(@Validated @RequestBody request: ResendVerificationEmailRequest): ApiResponse<Unit>

    @Operation(summary = "로그인")
    @PostMapping("/login")
    fun login(@Validated @RequestBody request: LoginRequest): ApiResponse<LoginResponse>

    @Operation(summary = "토큰 갱신")
    @PostMapping("/refresh")
    fun refreshToken(@Validated @RequestBody request: RefreshTokenRequest): ApiResponse<RefreshTokenResponse>
}