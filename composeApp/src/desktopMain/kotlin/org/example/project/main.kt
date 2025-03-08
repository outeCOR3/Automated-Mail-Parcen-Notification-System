package org.example.project

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.example.project.network.UserApi
import org.example.project.network.createHttpClient

fun main() = application {
    val client = createHttpClient() // ✅ Create HTTP Client
    val userApi = UserApi(client)   // ✅ Initialize UserApi properly

    Window(
        onCloseRequest = ::exitApplication,
        title = "itonaa",
    ) {
        App(client, userApi) // ✅ Pass the correct userApi
    }
}
