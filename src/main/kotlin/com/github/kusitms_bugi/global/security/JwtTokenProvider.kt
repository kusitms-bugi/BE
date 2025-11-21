package com.github.kusitms_bugi.global.security

import com.github.kusitms_bugi.global.exception.ApiException
import com.github.kusitms_bugi.global.exception.AuthExceptionCode
import com.github.kusitms_bugi.global.properties.JwtProperties
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.TimeUnit
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    private val jwtProperties: JwtProperties,
    private val redisTemplate: RedisTemplate<String, String>
) {
    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray())
    }

    fun generateAccessToken(userId: UUID): String {
        return createToken(userId, jwtProperties.accessTokenValidity.toMillis(), TokenType.ACCESS)
    }

    fun generateRefreshToken(userId: UUID): String {
        val token = createToken(userId, jwtProperties.refreshTokenValidity.toMillis(), TokenType.REFRESH)

        redisTemplate.opsForValue().set(
            "refresh_token:$userId",
            token,
            jwtProperties.refreshTokenValidity.toMillis(),
            TimeUnit.MILLISECONDS
        )

        return token
    }

    fun validateRefreshToken(refreshToken: String): Boolean {
        if (!validateToken(refreshToken)) {
            return false
        }
        val userId = getUserIdFromToken(refreshToken)
        val key = "refresh_token:$userId"
        val storedToken = redisTemplate.opsForValue().get(key)
        return storedToken == refreshToken
    }

    private fun createToken(userId: UUID, validity: Long, tokenType: TokenType): String {
        val now = Date()

        return Jwts.builder()
            .subject(userId.toString())
            .issuer("bugi-core")
            .audience().add("bugi-client").and()
            .issuedAt(now)
            .notBefore(Date(now.time))
            .expiration(Date(now.time + validity))
            .claim("type", tokenType.name)
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
        } catch (_: ExpiredJwtException) {
            throw ApiException(AuthExceptionCode.TOKEN_EXPIRED)
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

    fun getUserIdFromRefreshToken(refreshToken: String): UUID {
        return getUserIdFromToken(refreshToken)
    }
}
