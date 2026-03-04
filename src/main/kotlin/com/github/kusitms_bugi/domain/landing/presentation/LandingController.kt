package com.github.kusitms_bugi.domain.landing.presentation

import com.github.kusitms_bugi.domain.landing.application.LandingService
import com.github.kusitms_bugi.domain.landing.presentation.dto.request.DownloadRequest
import com.github.kusitms_bugi.global.response.ApiResponse
import org.springframework.web.bind.annotation.RestController

@RestController
class LandingController(
    private val landingService: LandingService
) : LandingApi {

    override fun sendDownloadLink(request: DownloadRequest): ApiResponse<Unit> {
        landingService.sendDownloadLink(request.email)
        return ApiResponse.success()
    }
}
