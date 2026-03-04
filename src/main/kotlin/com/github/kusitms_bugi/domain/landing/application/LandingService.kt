package com.github.kusitms_bugi.domain.landing.application

import com.github.kusitms_bugi.global.mail.EmailService
import com.github.kusitms_bugi.global.properties.LandingProperties
import org.springframework.stereotype.Service

@Service
class LandingService(
    private val emailService: EmailService,
    private val landingProperties: LandingProperties
) {

    fun sendDownloadLink(email: String) {
        emailService.sendDownloadEmail(email, landingProperties.downloadUrl)
    }
}
