package org.example.project.service

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.headers
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import org.example.project.model.LockingAction


class LockingActionService(private val client: HttpClient) {
    private var errorMessage: String? = null
    private var lockerId: Int? = null

    suspend fun lockUnlockLocker(token: String, isLocked: Boolean): Boolean {
        errorMessage = null

        if (token.isBlank()) {
            errorMessage = "Token cannot be empty."
            return false
        }

        return try {
            // Step 1: Get user information to get lockerId
            val response: HttpResponse = client.get("http://192.168.8.132:8080/locker/me") {
                headers {
                    append("Authorization", "Bearer $token")
                }
            }

            if (response.status != HttpStatusCode.OK) {
                errorMessage = "Failed to retrieve user info."
                return false
            }

            val user = response.body<LockingAction>()
            lockerId = user.id // Extract lockerId from user info

            // Step 2: Send POST request to lock/unlock the locker
            val lockingAction = LockingAction(id = lockerId!!, isLocked = isLocked)
            val lockResponse: HttpResponse = client.post("http://192.168.8.132:8080/locker/lockers/lock") {
                headers {
                    append("Authorization", "Bearer $token")
                }
                setBody(lockingAction)
            }

            if (lockResponse.status == HttpStatusCode.OK) {
                true // Lock/unlock success
            } else {
                errorMessage = "Failed to lock/unlock the locker"
                false
            }
        } catch (e: Exception) {
            errorMessage = "Error during lock/unlock action: ${e.message}"
            false
        }
    }

    fun getErrorMessage(): String? = errorMessage
}
