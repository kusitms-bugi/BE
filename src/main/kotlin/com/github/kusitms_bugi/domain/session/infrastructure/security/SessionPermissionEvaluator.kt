package com.github.kusitms_bugi.domain.session.infrastructure.security

import com.github.kusitms_bugi.domain.session.infrastructure.jpa.Session
import com.github.kusitms_bugi.global.security.CustomUserDetails
import org.springframework.stereotype.Component

@Component
@Suppress("unused") // SpEL에서 사용
class SessionPermissionEvaluator {

    fun canAccessSession(principal: CustomUserDetails, session: Session) = session.user.id == principal.getId()
}
