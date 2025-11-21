package com.github.kusitms_bugi.global.security

import com.github.kusitms_bugi.global.properties.JwtProperties
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class EmailVerificationTokenProvider(
    private val jwtProperties: JwtProperties
) {
    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray())
    }

    fun generateEmailVerificationToken(userId: UUID): String {
        val now = Date()

        return Jwts.builder()
            .subject(userId.toString())
            .issuer("bugi-core")
            .audience().add("bugi-client").and()
            .issuedAt(now)
            .notBefore(Date(now.time))
            .signWith(key)
            .compact()
    }

    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun getUserIdFromToken(token: String): UUID {
        return UUID.fromString(
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .payload
                .subject
        )
    }
}
