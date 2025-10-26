package com.github.kusitms_bugi.global.exception

import com.github.kusitms_bugi.global.response.ApiResponse
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.web.HttpMediaTypeException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.resource.NoResourceFoundException

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ApiException::class)
    fun handleApiException(ex: ApiException): ApiResponse<Unit> {
        return ApiResponse.failure(ex)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleMessageNotReadable(ex: HttpMessageNotReadableException): ApiResponse<Unit> {
        val message = ex.message?.let {
            when {
                it.contains("JSON property") && it.contains("due to missing") -> {
                    val propertyName = it.substringAfter("JSON property ").substringBefore(" due to missing")
                    "$propertyName 필드가 누락되었습니다."
                }
                else -> "요청 본문을 읽을 수 없습니다."
            }
        } ?: "요청 본문을 읽을 수 없습니다."
        
        return ApiResponse.failure(ApiException(GlobalExceptionCode.BAD_REQUEST, message, ex))
    }

    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNotFound(ex: NoResourceFoundException): ApiResponse<Unit> {
        return ApiResponse.failure(ApiException(GlobalExceptionCode.NOT_FOUND, ex))
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleMethodNotAllowed(ex: HttpRequestMethodNotSupportedException): ApiResponse<Unit> {
        return ApiResponse.failure(ApiException(GlobalExceptionCode.METHOD_NOT_ALLOWED, ex))
    }

    @ExceptionHandler(HttpMediaTypeException::class)
    fun handleMediaTypeNotSupported(ex: HttpMediaTypeException): ApiResponse<Unit> {
        return ApiResponse.failure(ApiException(GlobalExceptionCode.UNSUPPORTED_MEDIA_TYPE, ex))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ApiResponse<Unit> {
        val errors = ex.bindingResult.fieldErrors
            .joinToString(", ") { "${it.field}: ${it.defaultMessage}" }
        
        return ApiResponse.failure(ApiException(GlobalExceptionCode.VALIDATION_FAILED, errors, ex))
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingParameter(ex: MissingServletRequestParameterException): ApiResponse<Unit> {
        return ApiResponse.failure(ApiException(GlobalExceptionCode.MISSING_PARAMETER, ex))
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(ex: MethodArgumentTypeMismatchException): ApiResponse<Unit> {
        return ApiResponse.failure(ApiException(GlobalExceptionCode.TYPE_MISMATCH, ex))
    }

    @ExceptionHandler(AuthorizationDeniedException::class)
    fun authorizationDenied(ex: AuthorizationDeniedException): ApiResponse<Unit> {
        return ApiResponse.failure(ApiException(GlobalExceptionCode.NOT_AUTHENTICATED, ex))
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ApiResponse<Unit> {
        return ApiResponse.failure(ApiException(GlobalExceptionCode.INTERNAL_SERVER_ERROR, ex))
    }
}