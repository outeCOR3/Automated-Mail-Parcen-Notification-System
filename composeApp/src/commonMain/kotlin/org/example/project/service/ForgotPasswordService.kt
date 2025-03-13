package org.example.project.service

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import org.example.project.model.ResetPasswordRequest


class ForgotPasswordService(private val client: HttpClient) {
    var errorMessage: String? = null

    suspend fun resetPassword(email: String, newPassword: String, confirmNewPassword:String): Boolean {
        errorMessage = null

        return try {
            val resetRequest = ResetPasswordRequest(email, newPassword,confirmNewPassword)
            val response: HttpResponse = client.post("http://192.168.8.132:8080/auth/reset-password") {
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToString(ResetPasswordRequest.serializer(), resetRequest))
            }

            if (response.status == HttpStatusCode.OK) {
                true
            } else {
                errorMessage = "Failed to reset password: ${response.status}"
                false
            }
        } catch (e: Exception) {
            errorMessage = "Error during password reset: ${e.message}"
            false
        }
    }
}