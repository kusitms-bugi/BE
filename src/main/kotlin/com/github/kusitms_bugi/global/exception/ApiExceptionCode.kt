package com.github.kusitms_bugi.global.exception

interface ApiExceptionCode {
    val code: String
    val message: String
}

enum class GlobalExceptionCode(
    override val code: String,
    override val message: String
) : ApiExceptionCode {
    INTERNAL_SERVER_ERROR("GLOBAL-100", "서버 내부 오류가 발생했습니다."),

    BAD_REQUEST("GLOBAL-200", "잘못된 요청입니다."),
    NOT_FOUND("GLOBAL-201", "요청한 리소스를 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED("GLOBAL-202", "지원하지 않는 HTTP 메서드입니다."),
    UNSUPPORTED_MEDIA_TYPE("GLOBAL-203", "지원하지 않는 미디어 타입입니다."),

    VALIDATION_FAILED("GLOBAL-301", "입력값이 올바르지 않습니다"),
    MISSING_PARAMETER("GLOBAL-302", "필수 요청 파라미터가 누락되었습니다."),
    TYPE_MISMATCH("GLOBAL-303", "요청 파라미터 타입이 올바르지 않습니다."),

    NO_REDIRECT_URI("GLOBAL-401", "리다이렉트 URI가 없습니다."),
    UNAUTHORIZED_REDIRECT_URI("GLOBAL-402", "허용되지 않은 리다이렉트 URI입니다."),
    NOT_AUTHENTICATED("GLOBAL-403", "인증되지 않은 사용자입니다."),
}

enum class UserExceptionCode(
    override val code: String,
    override val message: String
) : ApiExceptionCode {
    USER_NOT_FOUND("USER-001", "사용자를 찾을 수 없습니다."),
    EMAIL_ALREADY_EXISTS("USER-002", "이미 존재하는 이메일입니다."),
    INVALID_TOKEN("USER-003", "유효하지 않은 토큰입니다."),
    USER_NOT_ACTIVE("USER-004", "이메일 인증이 완료되지 않은 사용자입니다."),
    USER_ALREADY_ACTIVE("USER-005", "이미 이메일 인증이 완료된 사용자입니다."),
    INVALID_REFRESH_TOKEN("USER-006", "유효하지 않은 리프레시 토큰입니다."),
}