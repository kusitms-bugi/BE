package com.github.kusitms_bugi.global.config

import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Configuration
import java.util.*

@Configuration
class TimeZoneConfig {

    @PostConstruct
    fun configureTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone(SEOUL_TIMEZONE))
        System.setProperty("user.timezone", SEOUL_TIMEZONE)
    }

    companion object {
        private const val SEOUL_TIMEZONE = "Asia/Seoul"
    }
}