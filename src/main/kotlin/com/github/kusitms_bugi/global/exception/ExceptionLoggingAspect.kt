package com.github.kusitms_bugi.global.exception

import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Aspect
@Component
@Order(0)
class ExceptionLoggingAspect {

    private val logger = LoggerFactory.getLogger(ExceptionLoggingAspect::class.java)

    @Before("execution(* com.github.kusitms_bugi.global.exception.GlobalExceptionHandler.*(..)) && args(ex,..)")
    fun logException(joinPoint: JoinPoint, ex: Exception) {
        logger.error("[${joinPoint.signature.name}] ${ex.javaClass.simpleName} - ${ex.message}", ex)
    }
}
