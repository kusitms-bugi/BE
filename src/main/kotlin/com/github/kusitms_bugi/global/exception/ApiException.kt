package com.github.kusitms_bugi.global.exception

class ApiException(
    val code: String,
    override val message: String,
    override val cause: Throwable? = null
) : RuntimeException("[$code] $message", cause) {

    constructor(exceptionCode: ApiExceptionCode) : this(exceptionCode.code, exceptionCode.message, null)

    constructor(exceptionCode: ApiExceptionCode, cause: Throwable) : this(
        exceptionCode.code,
        exceptionCode.message,
        cause
    )

    constructor(exceptionCode: ApiExceptionCode, message: String, cause: Throwable) : this(
        exceptionCode.code,
        message,
        cause
    )
}