package com.github.kusitms_bugi.global.validator

import com.github.kusitms_bugi.global.properties.FrontendProperties
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import org.springframework.stereotype.Component

@Component
class AllowedOriginValidator(
    private val frontendProperties: FrontendProperties
) : ConstraintValidator<AllowedOrigin, String> {

    override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {
        if (value.isNullOrBlank()) return false
        
        return frontendProperties.allowedOrigins.any { origin ->
            value.startsWith(origin)
        }
    }
}
