package org.example.project

import  android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import io.ktor.client.*
import io.ktor.client.engine.android.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val client = HttpClient(Android)  // Create HttpClient instance for Android

        setContent {
            App()  // Pass client to App()
        }
    }
}
