package org.example.project.screens

import UserListScreen
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material.BottomAppBar
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.json.Json
import org.example.project.model.UsersDTO

@Composable
fun AdminLandingPage(
    token: String, // Add token parameter
    client: HttpClient, // Pass client explicitly
    onNavigateToHome: () -> Unit = {},
    onNavigateToLock: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onCreateUser: () -> Unit = {},
    onBackToLogin: () -> Unit = {}
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showCreateUser by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf(0) }
    var adminUsername by remember { mutableStateOf<String?>(null) } // Fetch admin username

    var isHomeIconClicked by remember { mutableStateOf(false) }
    var isLockIconClicked by remember { mutableStateOf(false) }
    var isNotificationIconClicked by remember { mutableStateOf(false) }

    // Fetch admin username via /users/me
    LaunchedEffect(Unit) {
        try {
            val response: HttpResponse = client.get("http://192.168.68.138:8080/users/me") {
                header("Authorization", "Bearer $token")
            }
            println("Admin /me Status: ${response.status}")
            println("Admin /me Body: ${response.body<String>()}")
            if (response.status == io.ktor.http.HttpStatusCode.OK) {
                val user = Json.decodeFromString<UsersDTO>(response.body())
                adminUsername = user.username
            } else {
                adminUsername = "Error: ${response.status}"
            }
        } catch (e: Exception) {
            println("Error fetching admin username: ${e.message}")
            adminUsername = "Error: ${e.message}"
        }
    }

    if (showCreateUser) {
        CreateUserScreen(
            onCreateUser = { username, email, password ->
                showCreateUser = false
                // Handle user creation logic
            },
            onCancel = { showCreateUser = false },
            client = client
        )
    } else {
        val animationSpec = tween<Dp>(durationMillis = 400, easing = androidx.compose.animation.core.FastOutSlowInEasing)

        val homeIconSize by animateDpAsState(targetValue = if (isHomeIconClicked) 90.dp else 80.dp, animationSpec = animationSpec)
        val homeIconOffset by animateDpAsState(targetValue = if (isHomeIconClicked) (-40).dp else (9).dp, animationSpec = animationSpec)
        val lockIconSize by animateDpAsState(targetValue = if (isLockIconClicked) 90.dp else 80.dp, animationSpec = animationSpec)
        val lockIconOffset by animateDpAsState(targetValue = if (isLockIconClicked) (-40).dp else (9).dp, animationSpec = animationSpec)
        val notificationIconSize by animateDpAsState(targetValue = if (isNotificationIconClicked) 90.dp else 80.dp, animationSpec = animationSpec)
        val notificationIconOffset by animateDpAsState(targetValue = if (isNotificationIconClicked) (-40).dp else (9).dp, animationSpec = animationSpec)

        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text(text = "LANDLORD - ${adminUsername ?: "Loading..."}", color = Color.White) },
                backgroundColor = Color(0xFF78C2D1),
                actions = {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                        }
                        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                            DropdownMenuItem(onClick = {
                                menuExpanded = false
                                showCreateUser = true
                            }) {
                                Text("Create User")
                            }
                            DropdownMenuItem(onClick = {
                                menuExpanded = false
                                onBackToLogin()
                            }) {
                                Text("Logout")
                            }
                        }
                    }
                }
            )

            // Pass token to UserListScreen
            UserListScreen(client = client, token = token)

            Box(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .offset(y = homeIconOffset)
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(homeIconSize)
                            .background(Color(0xFF5DA8B8), shape = CircleShape)
                            .clickable {
                                isHomeIconClicked = !isHomeIconClicked
                                selectedItem = 0
                                onNavigateToHome()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Home, contentDescription = "Home", tint = Color.Black, modifier = Modifier.size(34.dp))
                    }
                }

                Box(
                    modifier = Modifier
                        .offset(y = lockIconOffset)
                        .align(Alignment.BottomCenter)
                ) {
                    Box(
                        modifier = Modifier
                            .size(lockIconSize)
                            .background(Color(0xFF5DA8B8), shape = CircleShape)
                            .clickable {
                                isLockIconClicked = !isLockIconClicked
                                selectedItem = 1
                                onNavigateToLock()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Lock, contentDescription = "Lock", tint = Color.Black, modifier = Modifier.size(34.dp))
                    }
                }

                Box(
                    modifier = Modifier
                        .offset(y = notificationIconOffset)
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(notificationIconSize)
                            .background(Color(0xFF5DA8B8), shape = CircleShape)
                            .clickable {
                                isNotificationIconClicked = !isNotificationIconClicked
                                selectedItem = 2
                                onNavigateToNotifications()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Notifications, contentDescription = "Notifications", tint = Color.Black, modifier = Modifier.size(34.dp))
                    }
                }
            }

            val cutoutRadius = 30.dp
            val cutoutOffsetY = 20.dp
            val density = LocalDensity.current
            val cutoutRadiusPx = with(density) { cutoutRadius.toPx() }
            val cutoutOffsetYPx = with(density) { cutoutOffsetY.toPx() }

            BottomAppBar(
                backgroundColor = Color(0xFF78C2D1),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .graphicsLayer {
                        shape = GenericShape { size: Size, layoutDirection: LayoutDirection ->
                            moveTo(0f, 0f)
                            lineTo(size.width / 2 - cutoutRadiusPx, 0f)
                            arcTo(
                                rect = Rect(
                                    left = size.width / 2 - cutoutRadiusPx * 2,
                                    top = -cutoutRadiusPx * 2,
                                    right = size.width / 2 + cutoutRadiusPx * 2,
                                    bottom = 0f
                                ),
                                startAngleDegrees = 0f,
                                sweepAngleDegrees = -180f,
                                forceMoveTo = false
                            )
                            lineTo(size.width, 0f)
                            lineTo(size.width, size.height)
                            lineTo(0f, size.height)
                            close()
                        }
                        clip = true
                    },
                elevation = 12.dp
            ) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}
