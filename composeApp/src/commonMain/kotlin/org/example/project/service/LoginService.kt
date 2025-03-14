package org.example.project.service

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import org.example.project.model.LoginRequest
import org.example.project.model.LoginResponse

class LoginService(private val client: HttpClient) {
    var errorMessage: String? = null
    private var userRole: String? = null

    suspend fun login(email: String, password: String): Boolean {
        errorMessage = null

        // Validation: Ensure email and password are not empty
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Email and password cannot be empty."
            return false
        }

        return try {
            val response: HttpResponse = client.post("http://172.20.10.4:8080/auth/login") {
                contentType(io.ktor.http.ContentType.Application.Json) // Explicitly set JSON content type
                setBody(LoginRequest(email, password)) // Ensure serialization works
            }

            if (response.status == HttpStatusCode.OK) {
                val responseBody = response.body<LoginResponse>()
                saveToken(responseBody.token)
                userRole = responseBody.role
                true
            } else {
                errorMessage = "Login failed: Wrong email or password"
                false
            }
        } catch (e: Exception) {
            errorMessage = "Error during login: ${e.message}"
            false
        }
    }

    fun getUserRole(): String? {
        return userRole
    }

    private fun saveToken(token: String) {
        // Save the token securely (e.g., DataStore, SharedPreferences)
    }
}
