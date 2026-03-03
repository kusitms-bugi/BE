package com.github.kusitms_bugi.domain.user.presentation

import com.github.kusitms_bugi.domain.user.presentation.dto.request.SendDownloadEmailRequest
import com.github.kusitms_bugi.domain.user.presentation.dto.response.MyProfileResponse
import com.github.kusitms_bugi.global.response.ApiResponse
import com.github.kusitms_bugi.global.security.CustomUserDetails
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@Tag(name = "유저")
@RequestMapping("/users")
interface UserApi {

    @Operation(summary = "내 정보 조회")
    @GetMapping("/me")
    fun getMyProfile(
        @Parameter(hidden = true) @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ApiResponse<MyProfileResponse>

    @Operation(summary = "회원 탈퇴")
    @DeleteMapping("/me")
    fun withdraw(
        @Parameter(hidden = true) @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ApiResponse<Unit>

    @Operation(summary = "다운로드 메일 전송")
    @PostMapping("/me/send-download-email")
    fun sendDownloadEmail(
        @Parameter(hidden = true) @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Validated @RequestBody request: SendDownloadEmailRequest
    ): ApiResponse<Unit>
}