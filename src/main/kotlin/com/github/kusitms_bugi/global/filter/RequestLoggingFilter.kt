package com.github.kusitms_bugi.global.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper

@Configuration
@Profile("!deployment")
class RequestLoggingFilter : OncePerRequestFilter() {

    private val _logger = LoggerFactory.getLogger(RequestLoggingFilter::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val requestWrapper = ContentCachingRequestWrapper(request)
        val responseWrapper = ContentCachingResponseWrapper(response)

        val startTime = System.currentTimeMillis()

        try {
            filterChain.doFilter(requestWrapper, responseWrapper)
        } finally {
            val duration = System.currentTimeMillis() - startTime
            logRequestAndResponse(requestWrapper, responseWrapper, duration)
            responseWrapper.copyBodyToResponse()
        }
    }

    private fun logRequestAndResponse(
        request: ContentCachingRequestWrapper,
        response: ContentCachingResponseWrapper,
        duration: Long
    ) {
        val method = request.method
        val uri = request.requestURI
        val queryString = request.queryString?.let { "?$it" } ?: ""
        val status = response.status
        val clientIp = getClientIp(request)

        val requestBody = getRequestBody(request)
        val responseBody = getResponseBody(response)

        val logMessage = "HTTP Request/Response - $method $uri$queryString - Status: $status - Duration: ${duration}ms - IP: $clientIp - Request Body: $requestBody - Response Body: $responseBody"
        _logger.info(logMessage)
    }

    private fun getRequestBody(request: ContentCachingRequestWrapper): String {
        val content = request.contentAsByteArray
        return if (content.isNotEmpty()) {
            String(content, Charsets.UTF_8).take(1000)
        } else {
            "Empty"
        }
    }

    private fun getResponseBody(response: ContentCachingResponseWrapper): String {
        val content = response.contentAsByteArray
        return if (content.isNotEmpty()) {
            String(content, Charsets.UTF_8).take(1000)
        } else {
            "Empty"
        }
    }

    private fun getClientIp(request: HttpServletRequest): String {
        val xForwardedFor = request.getHeader("X-Forwarded-For")
        val xRealIp = request.getHeader("X-Real-IP")

        return when {
            !xForwardedFor.isNullOrBlank() -> xForwardedFor.split(",")[0].trim()
            !xRealIp.isNullOrBlank() -> xRealIp
            else -> request.remoteAddr
        }
    }
}