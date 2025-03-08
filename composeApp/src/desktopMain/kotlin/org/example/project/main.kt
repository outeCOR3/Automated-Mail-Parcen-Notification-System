package org.example.project

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.example.project.network.createHttpClient

fun main() = application {
    val client = createHttpClient() // ✅ Create HTTP Client
 // ✅ Initialize UserApi properly

    Window(
        onCloseRequest = ::exitApplication,
        title = "itonaa",
    ) {
        App(client) // ✅ Pass the correct userApi
    }
}
