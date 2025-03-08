package org.example.project

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.example.project.network.createHttpClient

fun main() = application {
    val client = createHttpClient() // ✅ Create HTTP Client here

    Window(
        onCloseRequest = ::exitApplication,
        title = "itonaa",
    ) {
        App(client) // ✅ Now 'client' is passed correctly
    }
}
