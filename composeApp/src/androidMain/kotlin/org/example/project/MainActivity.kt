package org.example.project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import org.example.project.network.UserApi

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val client = HttpClient(Android)  // Create HttpClient instance for Android
        val userApi = UserApi(client)
        setContent {
            App(client,userApi)  // Pass client to App()
        }
    }
}
