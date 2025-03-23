package org.example.project.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date

object JwtConfig {
    private const val secret = "my_secret_key"
    const val issuer = "my_ktor_app"
    private const val validityInMs = 3_600_000 * 10// 1 hour (add multiplication to add hours)

    val algorithm = Algorithm.HMAC256(secret)

    fun generateToken(email: String, role: String): String {
        return JWT.create()
            .withIssuer(issuer)
            .withSubject(email)
            .withClaim("role", role)
            .withExpiresAt(Date(System.currentTimeMillis() + validityInMs))
            .sign(algorithm)
    }

    fun verifyToken(token: String): Map<String, String>? {
        return try {
            val decodedJWT = JWT.require(algorithm)
                .withIssuer(issuer)
                .build()
                .verify(token)
            mapOf(
                "email" to decodedJWT.subject,
                "role" to decodedJWT.getClaim("role").asString()
            )
        } catch (e: Exception) {
            null
        }
    }
}
