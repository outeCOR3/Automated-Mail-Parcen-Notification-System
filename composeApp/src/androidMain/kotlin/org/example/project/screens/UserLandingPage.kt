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

@Composable
fun LockingAction(token: String) {
    var isLocked by remember { mutableStateOf(true) }

    // Rotation animation
    val rotation by animateFloatAsState(
        targetValue = if (isLocked) 0f else 360f,
        animationSpec = tween(
            durationMillis = 300, // 2 seconds for a slower spin
            easing = LinearEasing // Smooth constant speed
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
        targetValue = if (isLocked) Color.White else Color(0xFF2E2E2E), // Soft Dark Gray when Unlocked
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
            // Lock Icon (No Shadows, PNG-like)
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
                    .clickable { isLocked = !isLocked }
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
                text = if (isLocked) "Lockedsss" else "Unlocked",
                fontSize = 28.sp,
                color = lockColor
            )
        }
    }
