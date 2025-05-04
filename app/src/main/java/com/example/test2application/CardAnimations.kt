package com.example.test2application

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.abs

@Composable
fun FloatingCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable () -> Unit
) {
    val floatAnim = rememberInfiniteTransition()
    
    val floatY by floatAnim.animateFloat(
        initialValue = 0f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    val shadowAlpha by floatAnim.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Card(
        modifier = modifier
            .graphicsLayer {
                translationY = floatY
            }
            .shadow(
                elevation = 10.dp,
                spotColor = Color.Blue.copy(alpha = shadowAlpha),
                ambientColor = Color.Cyan.copy(alpha = shadowAlpha * 0.5f)
            ),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        content()
    }
}

@Composable
fun PulseCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    pulseColor: Color = Color.Blue,
    onClick: () -> Unit = {},
    content: @Composable () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scaleAnim = remember { Animatable(1f) }
    val pulseAnim = rememberInfiniteTransition()
    
    val pulse by pulseAnim.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    val glowAlpha by pulseAnim.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            scaleAnim.animateTo(
                targetValue = 0.95f,
                animationSpec = tween(100, easing = FastOutSlowInEasing)
            )
            scaleAnim.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            isPressed = false
        }
    }
    
    Card(
        modifier = modifier
            .graphicsLayer {
                scaleX = scaleAnim.value * pulse
                scaleY = scaleAnim.value * pulse
            }
            .shadow(
                elevation = 8.dp,
                spotColor = pulseColor.copy(alpha = glowAlpha),
                ambientColor = pulseColor.copy(alpha = glowAlpha * 0.5f)
            )
            .clickable {
                isPressed = true
                onClick()
            },
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        content()
    }
}

@Composable
fun TiltCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    
    Card(
        modifier = modifier
            .graphicsLayer {
                rotationX = offsetY / 20
                rotationY = -offsetX / 20
                cameraDistance = 12f * density
            }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val x = event.changes.first().position.x
                        val y = event.changes.first().position.y
                        
                        offsetX = (x - size.width / 2)
                        offsetY = (y - size.height / 2)
                    }
                }
            },
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        content()
    }
} 