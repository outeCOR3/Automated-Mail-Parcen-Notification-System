package org.example.project.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import kotlinx.coroutines.launch
import org.example.project.model.LockingAction
import org.example.project.service.LockingActionService

@Composable
fun LockingAction(token: String,client: HttpClient) {
    // Initialize the HTTP client and service

    val lockingService = remember { LockingActionService(client) }

    // State management
    var isLocked by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Rotation animation
    val rotation by animateFloatAsState(
        targetValue = if (isLocked) 0f else 360f,
        animationSpec = tween(
            durationMillis = 300,
            easing = LinearEasing
        ),
        label = "Rotation"
    )

    // Floating effect (Y-axis movement)
    val offsetY by animateFloatAsState(
        targetValue = if (isLocked) 0f else -15f,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "Floating Effect"
    )

    // Glow effect (Opacity changes)
    val glow by animateFloatAsState(
        targetValue = if (isLocked) 0.2f else 0.8f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "Glow Effect"
    )

    // Background color shift
    val backgroundColor by animateColorAsState(
        targetValue = if (isLocked) Color.White else Color(0xFF2E2E2E),
        animationSpec = tween(600),
        label = "Background Shift"
    )

    // Lock color
    val lockColor by animateColorAsState(
        targetValue = if (isLocked) Color(0xFF90CAF9) else Color(0xFFFFC107),
        animationSpec = tween(600),
        label = "Lock Color"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Lock Icon
            Icon(
                imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                contentDescription = "Lock Icon",
                tint = lockColor,
                modifier = Modifier
                    .size(200.dp)
                    .graphicsLayer(
                        rotationZ = rotation,
                        translationY = offsetY
                    )
                    .clickable {
                        coroutineScope.launch {
                            // Call the lock/unlock service
                            val success = lockingService.toggleLockerState(token)
                            if (success) {
                                isLocked = !isLocked // Update state only on success
                            }
                            errorMessage = lockingService.getErrorMessage()
                        }
                    }
            )

            // Glowing Effect (Only When Unlocked)
            if (!isLocked) {
                Canvas(
                    modifier = Modifier.size(250.dp)
                ) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(lockColor.copy(alpha = glow), Color.Transparent),
                            center = Offset(size.width / 2, size.height / 2),
                            radius = size.width / 2
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lock Status Text
            Text(
                text = if (isLocked) "Locked" else "Unlocked",
                fontSize = 28.sp,
                color = lockColor
            )

            // Error Message Display
            errorMessage?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = it,
                    fontSize = 16.sp,
                    color = Color.Red
                )
            }
        }
    }
}