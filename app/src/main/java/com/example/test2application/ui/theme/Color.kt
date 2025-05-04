package com.example.test2application.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val RescueRed = Color(0xFFE53935)
val RescueBlue = Color(0xFF1E88E5)
val TechDark = Color(0xFF1A237E)
val TechLight = Color(0xFF42A5F5)
val SafetyGreen = Color(0xFF43A047)
val DarkGray = Color(0xFF121212)
val LightGray = Color(0xFFEEEEEE)
val AlertOrange = Color(0xFFFB8C00)

// New cool tech gradient colors
val DeepBlue = Color(0xFF0D47A1)
val ElectricBlue = Color(0xFF2979FF)
val NeonBlue = Color(0xFF00B0FF)
val CyberPurple = Color(0xFF6200EA)
val TechGreen = Color(0xFF00C853)

// Gradient brushes
val rescueGradient = Brush.verticalGradient(
    colors = listOf(DeepBlue, CyberPurple.copy(alpha = 0.7f))
)

val emergencyGradient = Brush.verticalGradient(
    colors = listOf(RescueRed.copy(alpha = 0.8f), DeepBlue)
)

val cardGlowGradient = Brush.radialGradient(
    colors = listOf(
        NeonBlue.copy(alpha = 0.7f),
        NeonBlue.copy(alpha = 0.0f)
    )
)