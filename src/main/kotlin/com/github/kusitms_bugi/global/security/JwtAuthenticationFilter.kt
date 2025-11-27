package com.github.kusitms_bugi.global.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.kusitms_bugi.global.exception.ApiException
import com.github.kusitms_bugi.global.response.ApiResponse
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider,
    private val userDetailsService: CustomUserDetailsService,
    private val objectMapper: ObjectMapper
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            extractTokenFromRequest(request)
                ?.takeIf { jwtTokenProvider.validateToken(it) }
                ?.let { jwtTokenProvider.getUserIdFromToken(it) }
                ?.let { userDetailsService.loadUserByUsername(it) }
                ?.let { UsernamePasswordAuthenticationToken(it, null, it.authorities) }
                ?.let { SecurityContextHolder.getContext().authentication = it }

            filterChain.doFilter(request, response)
        } catch (e: ApiException) {
            response.status = HttpServletResponse.SC_OK
            response.contentType = MediaType.APPLICATION_JSON_VALUE
            response.characterEncoding = "UTF-8"

            response.writer.write(objectMapper.writeValueAsString(ApiResponse.failure(e)))
        }
    }

    private fun extractTokenFromRequest(request: HttpServletRequest): String? {
        return request.getHeader("Authorization")
            ?.takeIf { it.startsWith("Bearer ") }
            ?.substring(7)
    }
}
