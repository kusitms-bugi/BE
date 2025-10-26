package com.github.kusitms_bugi.global.security

import com.github.kusitms_bugi.domain.user.domain.UserRepository
import com.github.kusitms_bugi.global.exception.ApiException
import com.github.kusitms_bugi.global.exception.UserExceptionCode
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service
import java.util.*

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {
    override fun loadUserByUsername(id: String): UserDetails {
        return CustomUserDetails(
            userRepository.findById(UUID.fromString(id))
                ?: throw ApiException(UserExceptionCode.USER_NOT_FOUND)
        )
    }

    fun loadUserByUsername(id: UUID) = loadUserByUsername(id.toString())
}
