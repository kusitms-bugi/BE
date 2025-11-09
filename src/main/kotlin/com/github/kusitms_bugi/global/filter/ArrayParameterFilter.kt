package com.github.kusitms_bugi.global.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletRequestWrapper
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class ArrayParameterFilter : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val wrappedRequest = object : HttpServletRequestWrapper(request) {
            override fun getParameterMap(): Map<String, Array<String>> {
                val originalParams = super.getParameterMap()
                val modifiedParams = mutableMapOf<String, Array<String>>()
                
                originalParams.forEach { (key, values) ->
                    val cleanKey = key.replace("[]", "")
                    modifiedParams[cleanKey] = values
                }
                
                return modifiedParams
            }
            
            override fun getParameterValues(name: String): Array<String>? {
                return parameterMap[name]
            }
        }
        
        filterChain.doFilter(wrappedRequest, response)
    }
}
