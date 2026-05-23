package com.example.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ArcReactorCyan
import com.example.ui.theme.HologramBlue
import com.example.ui.theme.JarvisAccentGold
import com.example.ui.theme.JarvisAccentRed
import com.example.voice.VoiceState
import kotlin.math.sin

@Composable
fun ArcReactorOrb(
    modifier: Modifier = Modifier,
    isThinking: Boolean = false,
    isListening: Boolean = false,
    isSpeaking: Boolean = false,
    colorSchemeIndex: Int = 0 // 0: Blue HUD, 1: Gold, 2: Red Alert
) {
    val infiniteTransition = rememberInfiniteTransition(label = "arc_reactor")
    
    // Core spin rotation
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isThinking) 1500 else 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Breathing pulse
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 0.93f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isListening) 900 else 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Determine color scheme based on parameters
    val baseColor = when (colorSchemeIndex) {
        1 -> JarvisAccentGold
        2 -> JarvisAccentRed
        else -> ArcReactorCyan
    }
    val secondaryColor = when (colorSchemeIndex) {
        1 -> Color(0xFFFF9100)
        2 -> Color(0xFFFF5252)
        else -> HologramBlue
    }

    Box(
        modifier = modifier.size(240.dp),
        contentAlignment = Alignment.Center
    ) {
        // Futuristic grid background lines inside the circular boundary
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = (size.minDimension / 2) * breathingScale

            // Pulsing inner bloom glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        baseColor.copy(alpha = if (isSpeaking) 0.5f else 0.25f),
                        baseColor.copy(alpha = 0.05f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius * 1.1f
                ),
                radius = radius * 1.1f
            )

            // Outer rings
            drawCircle(
                color = baseColor.copy(alpha = 0.35f),
                radius = radius,
                style = Stroke(width = 2.dp.toPx())
            )
            drawCircle(
                color = secondaryColor.copy(alpha = 0.2f),
                radius = radius * 0.88f,
                style = Stroke(width = 1.dp.toPx())
            )

            // Radial metric ticks (Outer dial)
            val tickCount = 40
            for (i in 0 until tickCount) {
                val angle = (i * (360f / tickCount))
                rotate(degrees = angle, pivot = center) {
                    val lineLength = if (i % 5 == 0) 12.dp.toPx() else 6.dp.toPx()
                    val lineAlpha = if (i % 5 == 0) 0.6f else 0.3f
                    drawLine(
                        color = baseColor.copy(alpha = lineAlpha),
                        start = Offset(center.x, center.y - radius),
                        end = Offset(center.x, center.y - radius + lineLength),
                        strokeWidth = (if (i % 5 == 0) 2.dp else 1.dp).toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        // Spinning internals of the reactor
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = (size.minDimension / 2) * breathingScale

            rotate(degrees = rotationAngle, pivot = center) {
                // Draw 3 arc blades
                val sweepAngle = 70f
                val strokeWidth = 8.dp.toPx()
                val insetDistance = radius * 0.72f
                val activeAlpha = if (isThinking || isSpeaking) 0.85f else 0.55f

                for (i in 0..2) {
                    val startAngle = i * 120f
                    drawArc(
                        color = baseColor.copy(alpha = activeAlpha),
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = Offset(center.x - insetDistance, center.y - insetDistance),
                        size = Size(insetDistance * 2, insetDistance * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                // Dotted ring
                val pointRadius = radius * 0.45f
                for (j in 0 until 12) {
                    val dotAngle = j * 30f
                    rotate(degrees = dotAngle, pivot = center) {
                        drawCircle(
                            color = baseColor.copy(alpha = 0.7f),
                            radius = 3.dp.toPx(),
                            center = Offset(center.x, center.y - pointRadius)
                        )
                    }
                }
            }

            // Central power core triangle/hexagon core
            val coreRadius = radius * 0.25f
            drawCircle(
                color = Color.White.copy(alpha = if (isSpeaking) 0.95f else 0.75f),
                radius = coreRadius,
                style = Stroke(width = 3.dp.toPx())
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, baseColor.copy(alpha = 0.8f), Color.Transparent),
                    center = center,
                    radius = coreRadius * 0.8f
                ),
                radius = coreRadius * 0.8f
            )
        }
    }
}

@Composable
fun HolographicWaveform(
    modifier: Modifier = Modifier,
    voiceState: VoiceState = VoiceState.IDLE,
    colorSchemeIndex: Int = 0
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_anim")
    
    // Wave animation offset phase shift
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    val color = when (colorSchemeIndex) {
        1 -> JarvisAccentGold
        2 -> JarvisAccentRed
        else -> ArcReactorCyan
    }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val midY = height / 2

        if (voiceState == VoiceState.IDLE) {
            // Drawing a flat, calm ambient micro-quantum pulse
            drawLine(
                color = color.copy(alpha = 0.25f),
                start = Offset(0f, midY),
                end = Offset(width, midY),
                strokeWidth = 1.dp.toPx()
            )
            return@Canvas
        }

        val frequency = 4.0 // cycles across width
        val amplitude = when (voiceState) {
            VoiceState.LISTENING -> height * 0.4f
            VoiceState.THINKING -> height * 0.15f
            VoiceState.SPEAKING -> height * 0.35f
            else -> height * 0.05f
        }

        // Draw multiple overlapping phase shifted sine lines to create depth
        val waveCount = if (voiceState == VoiceState.THINKING) 5 else 3
        for (w in 0 until waveCount) {
            val phaseShift = wavePhase + (w * (Math.PI / 4)).toFloat()
            val alphaFactor = 1f - (w * 0.18f)
            val strokeThick = (3.dp - (w * 0.5f).dp).toPx()

            var lastX = 0f
            var lastY = midY

            for (x in 0..width.toInt() step 4) {
                val xNorm = x.toFloat() / width
                // Taper the sine waves on the edges so it fits on screen nicely
                val taper = sin(xNorm * Math.PI).toFloat()
                
                // Add secondary frequency component for complex organic tech wave feel
                val secondarySine = if (voiceState == VoiceState.SPEAKING) {
                    sin(xNorm * Math.PI * 12 + wavePhase).toFloat() * 0.25f
                } else 0f

                val sinValue = sin((xNorm * Math.PI * 2 * frequency) + phaseShift).toFloat() + secondarySine
                val y = midY + (sinValue * amplitude * taper)

                if (x > 0) {
                    drawLine(
                        color = color.copy(alpha = 0.8f * alphaFactor),
                        start = Offset(lastX, lastY),
                        end = Offset(x.toFloat(), y),
                        strokeWidth = strokeThick,
                        cap = StrokeCap.Round
                    )
                }
                lastX = x.toFloat()
                lastY = y
            }
        }
    }
}
