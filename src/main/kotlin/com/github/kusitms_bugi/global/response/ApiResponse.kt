package com.github.kusitms_bugi.global.response

import com.github.kusitms_bugi.global.exception.ApiException
import java.time.LocalDateTime

data class ApiResponse<T>(
    val timestamp: LocalDateTime,
    val success: Boolean,
    val data: T? = null,
    val code: String? = null,
    val message: String? = null
) {
    companion object {
        fun <T> success(data: T): ApiResponse<T> {
            return ApiResponse(
                timestamp = LocalDateTime.now(),
                success = true,
                data = data,
                code = "SUCCESS",
                message = null
            )
        }
        
        fun success(): ApiResponse<Unit> {
            return ApiResponse(
                timestamp = LocalDateTime.now(),
                success = true,
                data = Unit,
                code = "SUCCESS",
                message = null
            )
        }
        
        fun failure(exception: ApiException): ApiResponse<Unit> {
            return ApiResponse(
                timestamp = LocalDateTime.now(),
                success = false,
                data = null,
                code = exception.code,
                message = exception.message
            )
        }
    }
}

data class Page<T> (
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val last: Boolean
) {
    companion object {
        fun <T> of(content: List<T>, page: Int, size: Int, totalElements: Long): Page<T> {
            val totalPages = ((totalElements + size - 1) / size).toInt().coerceAtLeast(1)

            return Page(
                content = content,
                page = page,
                size = size,
                totalElements = totalElements,
                totalPages = totalPages,
                last = page >= totalPages - 1
            )
        }
    }
}