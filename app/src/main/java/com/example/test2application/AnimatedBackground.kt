package com.example.test2application

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.ui.unit.dp
import kotlin.random.Random

@Composable
fun AnimatedTechBackground(modifier: Modifier = Modifier) {
    val particles = remember { List(30) { Particle.random() } }
    
    Box(modifier = modifier.fillMaxSize()) {
        particles.forEachIndexed { index, particle ->
            AnimatedParticle(particle, index)
        }
    }
}

@Composable
private fun AnimatedParticle(particle: Particle, index: Int) {
    val infiniteTransition = rememberInfiniteTransition()
    
    val xPosition by infiniteTransition.animateFloat(
        initialValue = particle.initialX,
        targetValue = particle.initialX + particle.rangeX,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = particle.duration,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    val yPosition by infiniteTransition.animateFloat(
        initialValue = particle.initialY,
        targetValue = particle.initialY + particle.rangeY,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = particle.duration,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = particle.minAlpha,
        targetValue = particle.maxAlpha,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1500 + (index * 100),
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    val size by infiniteTransition.animateFloat(
        initialValue = particle.minSize,
        targetValue = particle.maxSize,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2000 + (index * 100),
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .alpha(alpha)
    ) {
        when (particle.type) {
            ParticleType.CIRCLE -> drawCircle(
                color = particle.color,
                radius = size,
                center = Offset(xPosition, yPosition),
                style = Stroke(width = 2.dp.toPx())
            )
            ParticleType.DOT -> drawCircle(
                color = particle.color,
                radius = size / 5,
                center = Offset(xPosition, yPosition)
            )
            ParticleType.GRID -> drawGrid(
                color = particle.color,
                center = Offset(xPosition, yPosition),
                size = size
            )
        }
    }
}

private fun DrawScope.drawGrid(color: Color, center: Offset, size: Float) {
    val halfSize = size / 2
    // Horizontal line
    drawLine(
        color = color,
        start = Offset(center.x - halfSize, center.y),
        end = Offset(center.x + halfSize, center.y),
        strokeWidth = 1.dp.toPx()
    )
    // Vertical line
    drawLine(
        color = color,
        start = Offset(center.x, center.y - halfSize),
        end = Offset(center.x, center.y + halfSize),
        strokeWidth = 1.dp.toPx()
    )
}

data class Particle(
    val initialX: Float,
    val initialY: Float,
    val rangeX: Float,
    val rangeY: Float,
    val duration: Int,
    val color: Color,
    val minAlpha: Float,
    val maxAlpha: Float,
    val minSize: Float,
    val maxSize: Float,
    val type: ParticleType
) {
    companion object {
        fun random(): Particle {
            val techColors = listOf(
                Color(0xFF00B0FF),  // Blue
                Color(0xFF64FFDA),  // Teal
                Color(0xFF76FF03),  // Green
                Color(0xFFEEFF41),  // Yellow
                Color(0xFFFF4081)   // Pink
            )
            
            return Particle(
                initialX = Random.nextFloat() * 1000,
                initialY = Random.nextFloat() * 2000,
                rangeX = Random.nextFloat() * 200 - 100,
                rangeY = Random.nextFloat() * 200 - 100,
                duration = Random.nextInt(8000, 15000),
                color = techColors.random(),
                minAlpha = Random.nextFloat() * 0.2f,
                maxAlpha = Random.nextFloat() * 0.3f + 0.2f,
                minSize = Random.nextFloat() * 30 + 10,
                maxSize = Random.nextFloat() * 60 + 40,
                type = ParticleType.values().random()
            )
        }
    }
}

enum class ParticleType {
    CIRCLE, DOT, GRID
} 