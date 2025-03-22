package org.example.project.service

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
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
    private var userToken: String? = null // Store the token

    suspend fun login(email: String, password: String): Boolean {
        errorMessage = null

        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Email and password cannot be empty."
            return false
        }

        return try {
            val response: HttpResponse = client.post("http://192.168.8.132:8080/auth/login") {
                contentType(io.ktor.http.ContentType.Application.Json)
                setBody(LoginRequest(email, password))
            }

            if (response.status == HttpStatusCode.OK) {
                val responseBody = response.body<LoginResponse>()
                userToken = responseBody.token // Save token
                userRole = responseBody.role
                saveToken(responseBody.token) // Optionally save to persistent storage
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

    fun getUserRole(): String? = userRole

    fun getUserToken(): String? = userToken // Expose the token

    private fun saveToken(token: String) {
        // Implement secure storage (e.g., DataStore, SharedPreferences)
    }
}
