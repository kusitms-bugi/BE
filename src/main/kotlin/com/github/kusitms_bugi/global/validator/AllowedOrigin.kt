package com.github.kusitms_bugi.global.validator

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [AllowedOriginValidator::class])
annotation class AllowedOrigin(
    val message: String = "허용되지 않은 콜백 URL입니다.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
