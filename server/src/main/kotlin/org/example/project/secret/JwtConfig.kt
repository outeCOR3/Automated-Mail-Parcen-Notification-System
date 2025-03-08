package org.example.project.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date

object JwtConfig {
    private const val secret = "my_secret_key"
    private const val issuer = "my_ktor_app"
    private const val validityInMs = 3_600_000 * 10 // 10 hours


    private val algorithm = Algorithm.HMAC256(secret)

    fun generateToken(email: String): String {
        return JWT.create()
            .withIssuer(issuer)
            .withSubject(email)
            .withExpiresAt(Date(System.currentTimeMillis() + validityInMs))
            .sign(algorithm)
    }
}
