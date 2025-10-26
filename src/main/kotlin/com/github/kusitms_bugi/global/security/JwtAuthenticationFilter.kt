package com.github.kusitms_bugi.global.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider,
    private val userDetailsService: CustomUserDetailsService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        extractTokenFromRequest(request)
            ?.takeIf { jwtTokenProvider.validateToken(it) }
            ?.let { jwtTokenProvider.getUserIdFromToken(it) }
            ?.let { userDetailsService.loadUserByUsername(it) }
            ?.let { UsernamePasswordAuthenticationToken(it, null, it.authorities) }
            ?.let { SecurityContextHolder.getContext().authentication = it }

        filterChain.doFilter(request, response)
    }

    private fun extractTokenFromRequest(request: HttpServletRequest): String? {
        return request.getHeader("Authorization")
            ?.takeIf { it.startsWith("Bearer ") }
            ?.substring(7)
    }
}
