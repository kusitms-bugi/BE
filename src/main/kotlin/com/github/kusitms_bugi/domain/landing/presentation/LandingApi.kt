package com.github.kusitms_bugi.domain.landing.presentation

import com.github.kusitms_bugi.domain.landing.presentation.dto.request.DownloadRequest
import com.github.kusitms_bugi.global.response.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@Tag(name = "랜딩")
@RequestMapping("/landing")
interface LandingApi {

    @Operation(summary = "다운로드 링크 이메일 전송")
    @PostMapping("/download")
    fun sendDownloadLink(
        @Valid @RequestBody request: DownloadRequest
    ): ApiResponse<Unit>
}
