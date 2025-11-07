package com.github.kusitms_bugi.global.security

import com.github.kusitms_bugi.domain.user.infrastructure.jpa.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.*

class CustomUserDetails(
    val user: User
) : UserDetails {
    fun getId(): UUID = user.id

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return listOf(SimpleGrantedAuthority("ROLE_USER"))
    }

    override fun getPassword(): String = user.password

    override fun getUsername(): String = user.email

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = user.deletedAt == null
}
