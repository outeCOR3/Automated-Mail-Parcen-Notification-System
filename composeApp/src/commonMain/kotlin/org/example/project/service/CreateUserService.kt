package org.example.project.service

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import org.example.project.model.RegisterUserRequest

class CreateUserService(private val client: HttpClient) {
    var errorMessage: String? = null

    suspend fun register(email: String, password: String, username: String): Boolean {
        errorMessage = null

        return try {
            val registerRequest = RegisterUserRequest(email, password,email )
            val response: HttpResponse = client.post("http://192.168.8.132:8080/auth/register") {
                contentType(io.ktor.http.ContentType.Application.Json)
                setBody(Json.encodeToString(registerRequest))  // Ensure serialization works
            }

            if (response.status == HttpStatusCode.Created) {
                true
            } else {
                errorMessage = "Registration failed: Account is Already Existing"
                false
            }
        } catch (e: Exception) {
            errorMessage = "Error during registration: ${e.message}"
            println(errorMessage)
            false
        }
    }
}
