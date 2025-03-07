package org.example.project

import androidx.compose.material3.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import kotlinx.coroutines.launch
import org.example.project.network.UserApi
import org.example.project.network.createHttpClient

@Composable
fun App() {
    MaterialTheme {
        val httpClient = remember { createHttpClient() }
        val userApi = remember { UserApi(httpClient) }

        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var passwordVisible by remember { mutableStateOf(false) }
        var rememberMe by remember { mutableStateOf(false) }
        var loginResponse by remember { mutableStateOf<String?>(null) }

        val scope = rememberCoroutineScope()

        val customColorScheme = lightColorScheme(
            primary = Color(0xFF6200EE),
            onPrimary = Color.White,
            background = Color(0xFFF5F5F5),
            onBackground = Color.Black
        )

        MaterialTheme(colorScheme = customColorScheme) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(min = 280.dp, max = 400.dp)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Welcome Back!", fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = "Toggle password visibility"
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = rememberMe, onCheckedChange = { rememberMe = it })
                            Text(text = "Remember me")
                        }

                        Text(
                            text = "Forgot Password?",
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { /* Handle forgot password */ }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                if (email.isBlank() || password.isBlank()) {
                                    loginResponse = "Please fill out all fields."
                                    return@launch
                                }

                                try {
                                    val user = userApi.getUserByEmail(email)
                                    if (user != null && user.password == password) {
                                        loginResponse = "Login successful"
                                    } else {
                                        loginResponse = "Invalid email or password"
                                    }
                                } catch (e: Exception) {
                                    loginResponse = "Error: ${e.message}"
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(text = "Log In", fontSize = 18.sp)
                    }

                    loginResponse?.let {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = it, color = if (it == "Login successful") Color.Green else Color.Red)
                    }
                }
            }
        }
    }
}
