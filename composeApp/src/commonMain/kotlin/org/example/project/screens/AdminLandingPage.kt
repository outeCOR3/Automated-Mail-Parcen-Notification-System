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

@Composable
fun AdminLandingPage(
    username: String,
    onNavigateToHome: () -> Unit = {},
    onNavigateToLock: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onCreateUser: () -> Unit = {},
    onBackToLogin: () -> Unit = {}
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showCreateUser by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf(0) }

    var isHomeIconClicked by remember { mutableStateOf(false) }
    var isLockIconClicked by remember { mutableStateOf(false) }
    var isNotificationIconClicked by remember { mutableStateOf(false) }

    val client = HttpClient()

    if (showCreateUser) {
        CreateUserScreen(
            onCreateUser = { username, password, role ->
                showCreateUser = false
                // Handle user creation logic with role
            },
            onCancel = { showCreateUser = false },
            client = client
        )
    } else {
        // Animation configurations
        val animationSpec = tween<Dp>(
            durationMillis = 400,
            easing = androidx.compose.animation.core.FastOutSlowInEasing
        )

        // Home icon animations
        val homeIconSize by animateDpAsState(
            targetValue = if (isHomeIconClicked) 90.dp else 80.dp,
            animationSpec = animationSpec
        )
        val homeIconOffset by animateDpAsState(
            targetValue = if (isHomeIconClicked) (-40).dp else (9).dp,
            animationSpec = animationSpec
        )

        // Lock icon animations
        val lockIconSize by animateDpAsState(
            targetValue = if (isLockIconClicked) 90.dp else 80.dp,
            animationSpec = animationSpec
        )
        val lockIconOffset by animateDpAsState(
            targetValue = if (isLockIconClicked) (-40).dp else (9).dp,
            animationSpec = animationSpec
        )

        // Notification icon animations
        val notificationIconSize by animateDpAsState(
            targetValue = if (isNotificationIconClicked) 90.dp else 80.dp,
            animationSpec = animationSpec
        )
        val notificationIconOffset by animateDpAsState(
            targetValue = if (isNotificationIconClicked) (-40).dp else (9).dp,
            animationSpec = animationSpec
        )

        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text(text = "LANDLORD", color = Color.White) },
                backgroundColor = Color(0xFF78C2D1),
                actions = {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = Color.White
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
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

            // Show UserListScreen directly
            UserListScreen(

                client = client
            )

            // Bottom navigation bar
            Box(modifier = Modifier.fillMaxWidth()) {
                // Home icon
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
                        Icon(
                            Icons.Filled.Home,
                            contentDescription = "Home",
                            tint = Color.Black,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }

                // Lock icon
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
                        Icon(
                            Icons.Filled.Lock,
                            contentDescription = "Lock",
                            tint = Color.Black,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }

                // Notification icon
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
                        Icon(
                            Icons.Filled.Notifications,
                            contentDescription = "Notifications",
                            tint = Color.Black,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }
            }

            // Bottom app bar
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
