package com.example

import android.os.Bundle
import android.content.Intent
import android.provider.Settings
import android.app.Activity
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.MyApplicationTheme
import androidx.compose.foundation.BorderStroke
import com.example.ui.JarvisViewModel
import com.example.ui.ArcReactorOrb
import com.example.ui.HolographicWaveform
import com.example.voice.VoiceState
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    JarvisAppCore(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun JarvisAppCore(
    modifier: Modifier = Modifier,
    viewModel: JarvisViewModel = viewModel()
) {
    val isBooted by viewModel.isBooted.collectAsStateWithLifecycle()
    val isAuthenticated by viewModel.isAuthenticated.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisDarkBackground)
    ) {
        // Holographic visual space grid lines
        CyberGridPattern(modifier = Modifier.fillMaxSize())

        when {
            !isBooted -> {
                StartupSequencePanel(viewModel = viewModel)
            }
            !isAuthenticated -> {
                BiometricScannerPanel(viewModel = viewModel)
            }
            else -> {
                MainAssistantHUD(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun CyberGridPattern(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val step = 80f // grid space lines

        // Horizontal Grid projection
        for (y in 0..height.toInt() step step.toInt()) {
            drawLine(
                color = ArcReactorCyan.copy(alpha = 0.04f),
                start = Offset(0f, y.toFloat()),
                end = Offset(width, y.toFloat()),
                strokeWidth = 1f
            )
        }

        // Vertical lines
        for (x in 0..width.toInt() step step.toInt()) {
            drawLine(
                color = ArcReactorCyan.copy(alpha = 0.04f),
                start = Offset(x.toFloat(), 0f),
                end = Offset(x.toFloat(), height),
                strokeWidth = 1f
            )
        }
    }
}

@Composable
fun StartupSequencePanel(viewModel: JarvisViewModel) {
    val progress by viewModel.bootProgress.collectAsStateWithLifecycle()
    val statusText by viewModel.bootStatusText.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Glowing reactor symbol rotating slightly
        ArcReactorOrb(
            modifier = Modifier.size(180.dp),
            isThinking = true,
            colorSchemeIndex = 0
        )

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "INITIALIZING CORE SYSTEMS",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = ArcReactorCyan,
            letterSpacing = 4.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.testTag("startup_title")
        )

        Spacer(modifier = Modifier.height(16.dp))

        // HighTech progress line
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(4.dp)
                .background(Color.White.copy(alpha = 0.1f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .background(ArcReactorCyan)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = statusText,
            fontSize = 12.sp,
            color = HologramTextDim,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun BiometricScannerPanel(viewModel: JarvisViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "SECURITY CHECKPOINT",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = JarvisAccentGold,
            letterSpacing = 3.sp,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "RESTRICTED ACCESS — AUTHORIZED PERSONNEL ONLY",
            fontSize = 11.sp,
            color = HologramTextDim,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(64.dp))

        // Fingerprint Scan target zone
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(GlassyCyan)
                .border(2.dp, HologramBorder, CircleShape)
                .clickable { viewModel.toggleBiometricMockAuth() }
                .testTag("biometric_scanner"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Fingerprint,
                contentDescription = "Tap to verify bio-identity",
                tint = ArcReactorCyan,
                modifier = Modifier.size(72.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "HOLD BIO-SCAN SENSOR TO UNLOCK JARVIS",
            fontSize = 12.sp,
            color = ArcReactorCyan,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "(Simulated Smart Finger Scanner Setup)",
            fontSize = 10.sp,
            color = Color.White.copy(alpha = 0.3f),
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun MainAssistantHUD(viewModel: JarvisViewModel) {
    val logs by viewModel.commandLogs.collectAsStateWithLifecycle()
    val voiceState by viewModel.voiceState.collectAsStateWithLifecycle()
    val currentPrompt by viewModel.currentPrompt.collectAsStateWithLifecycle()
    val batteryStatus by viewModel.batteryStatus.collectAsStateWithLifecycle()
    val isAccessibilityActive by viewModel.accessibilityActive.collectAsStateWithLifecycle()
    val voiceMode by viewModel.voiceMode.collectAsStateWithLifecycle()
    val themeIndex by viewModel.uiThemeColorIndex.collectAsStateWithLifecycle()
    val userPreferencesName by viewModel.userPreferencesName.collectAsStateWithLifecycle()
    val simulatedStorageUsed by viewModel.simulatedStorageLoad.collectAsStateWithLifecycle()

    var activeTab by remember { mutableIntStateOf(0) } // 0: HUD Reactor, 1: Chats, 2: Device, 3: Storage Files, 4: Settings
    var isHologramFullScreen by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Set dynamic system color mapping
    val hudColor = when (themeIndex) {
        1 -> JarvisAccentGold
        2 -> JarvisAccentRed
        else -> ArcReactorCyan
    }

    if (isHologramFullScreen) {
        // Immersive Full Frame Hologram space
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(JarvisDarkBackground)
                .clickable { isHologramFullScreen = false }
        ) {
            CyberGridPattern(modifier = Modifier.fillMaxSize())

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "HOLOGRAM PROTOCOL ACTIVE",
                    fontSize = 14.sp,
                    color = hudColor,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 3.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                ArcReactorOrb(
                    modifier = Modifier.size(280.dp),
                    isListening = voiceState == VoiceState.LISTENING,
                    isThinking = voiceState == VoiceState.THINKING,
                    isSpeaking = voiceState == VoiceState.SPEAKING,
                    colorSchemeIndex = themeIndex
                )

                Spacer(modifier = Modifier.height(32.dp))

                HolographicWaveform(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(60.dp),
                    voiceState = voiceState,
                    colorSchemeIndex = themeIndex
                )

                Spacer(modifier = Modifier.height(16.dp))

                val lastResponse = logs.firstOrNull()?.response ?: "JARVIS standing by..."
                Text(
                    text = '"' + lastResponse + '"',
                    fontSize = 14.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(0.9f)
                )

                Spacer(modifier = Modifier.height(48.dp))

                Text(
                    text = "TAP ON HUD SCREEN TO DEACTIVATE PROJECTION",
                    fontSize = 11.sp,
                    color = hudColor.copy(alpha = 0.5f),
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }
        }
    } else {
        // Standard high fidelity dashboard layout
        Column(modifier = Modifier.fillMaxSize()) {
            
            // 1. Futuristic Header Bar (Sophisticated Dark Spec)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SYSTEM STATUS",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = hudColor,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "J.A.R.V.I.S.",
                            fontWeight = FontWeight.Light,
                            fontSize = 24.sp,
                            color = Color.White,
                            letterSpacing = (-0.5).sp,
                            fontFamily = FontFamily.SansSerif
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "v4.0.2",
                            fontSize = 10.sp,
                            color = hudColor.copy(alpha = 0.5f),
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }
                }

                // System diagnostics dots columns
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    listOf(
                        Pair("SIGNAL: 98%", hudColor),
                        Pair("CORE: 32.5°C", hudColor),
                        Pair("SYNC: OPTIMAL", Color(0xFF22C55E))
                    ).forEach { (text, color) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = text,
                                fontSize = 9.sp,
                                color = Color.White.copy(alpha = 0.7f),
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = HologramBorder.copy(alpha = 0.15f), thickness = 1.dp)

            // 2. Active Screen Section
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (activeTab) {
                    0 -> {
                        // TAB 0: HUD reactor panel with decorative side metrics
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Theme preset selection
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(GlassyWhite)
                                        .padding(2.dp)
                                ) {
                                    listOf("AQUA", "WARN", "ALERT").forEachIndexed { index, name ->
                                        val isThemeActive = index == themeIndex
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isThemeActive) hudColor.copy(alpha = 0.25f) else Color.Transparent)
                                                .clickable { viewModel.setHUDTheme(index) }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = name,
                                                fontSize = 9.sp,
                                                color = if (isThemeActive) Color.White else Color.White.copy(alpha = 0.4f),
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            // Beautiful Central Holographic Area
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                // Dynamic background decorative grid pattern representation
                                CyberGridPattern(modifier = Modifier.fillMaxSize().padding(12.dp))

                                // Inner holographic side indicators
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Left indicators column
                                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(modifier = Modifier.width(2.dp).height(20.dp).background(hudColor.copy(alpha = 0.4f)))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Column {
                                                Text("VOICE UPLINK", fontSize = 7.sp, color = hudColor.copy(alpha = 0.5f), fontFamily = FontFamily.Monospace)
                                                Text("CHANNEL 04", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                            }
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(modifier = Modifier.width(2.dp).height(20.dp).background(hudColor.copy(alpha = 0.4f)))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Column {
                                                Text("ACCESSIBILITY", fontSize = 7.sp, color = hudColor.copy(alpha = 0.5f), fontFamily = FontFamily.Monospace)
                                                Text(if (isAccessibilityActive) "SERVICE: ACTV" else "SYSTEM: OFF", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                            }
                                        }
                                    }

                                    // Right indicators column
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(16.dp),
                                        horizontalAlignment = Alignment.End
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text("BATTERY", fontSize = 7.sp, color = hudColor.copy(alpha = 0.5f), fontFamily = FontFamily.Monospace)
                                                Text("$batteryStatus% NORMAL", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(modifier = Modifier.width(2.dp).height(20.dp).background(hudColor.copy(alpha = 0.4f)))
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text("UPTIME", fontSize = 7.sp, color = hudColor.copy(alpha = 0.5f), fontFamily = FontFamily.Monospace)
                                                Text("04:12:33", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(modifier = Modifier.width(2.dp).height(20.dp).background(hudColor.copy(alpha = 0.4f)))
                                        }
                                    }
                                }

                                // Central core rotating reactor
                                Box(
                                    modifier = Modifier
                                        .size(220.dp)
                                        .clickable {
                                            if (voiceState == VoiceState.LISTENING) {
                                                viewModel.stopVoiceMic()
                                            } else {
                                                viewModel.startVoiceMic()
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    ArcReactorOrb(
                                        modifier = Modifier.size(200.dp),
                                        isListening = voiceState == VoiceState.LISTENING,
                                        isThinking = voiceState == VoiceState.THINKING,
                                        isSpeaking = voiceState == VoiceState.SPEAKING,
                                        colorSchemeIndex = themeIndex
                                    )
                                }
                            }

                            // 3. Dynamic Waveform & State representation
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                HolographicWaveform(
                                    modifier = Modifier
                                        .fillMaxWidth(0.9f)
                                        .height(44.dp),
                                    voiceState = voiceState,
                                    colorSchemeIndex = themeIndex
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                val stateLabel = when (voiceState) {
                                    VoiceState.LISTENING -> "LISTENING..."
                                    VoiceState.THINKING -> "COMPUTING SYSTEM LOGIC..."
                                    VoiceState.SPEAKING -> "JARVIS TALKING..."
                                    else -> "TAP ORB TO TRIGGER VOICE UPLINK"
                                }
                                Text(
                                    text = stateLabel,
                                    fontSize = 11.sp,
                                    color = hudColor,
                                    letterSpacing = 2.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    textAlign = TextAlign.Center
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // 4. Last Command Glass Capsule
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFF0F172A).copy(alpha = 0.5f))
                                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "LAST COMMAND",
                                        fontSize = 9.sp,
                                        color = hudColor,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    val lastCommandText = if (logs.isNotEmpty()) {
                                        "\"${logs.first().commandText}\""
                                    } else {
                                        "\"Jarvis, open Chrome and search for local weather.\""
                                    }
                                    Text(
                                        text = lastCommandText,
                                        fontSize = 12.sp,
                                        color = Color(0xFFCBD5E1), // Slate 300
                                        fontWeight = FontWeight.Light,
                                        fontFamily = FontFamily.SansSerif
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Action command tablets shortcuts
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Button(
                                    onClick = { isHologramFullScreen = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = hudColor.copy(alpha = 0.12f)),
                                    border = BorderStroke(1.dp, hudColor.copy(alpha = 0.3f)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AspectRatio,
                                        contentDescription = "Projection",
                                        tint = hudColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "PROJECTION",
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color.White
                                    )
                                }

                                Button(
                                    onClick = { viewModel.executeVoiceCommand("open chrome") },
                                    colors = ButtonDefaults.buttonColors(containerColor = GlassyCyan),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text("OPEN CHROME", fontSize = 9.sp, color = ArcReactorCyan)
                                }

                                Button(
                                    onClick = { viewModel.executeVoiceCommand("turn on flashlight") },
                                    colors = ButtonDefaults.buttonColors(containerColor = GlassyCyan),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text("LIGHT ON", fontSize = 9.sp, color = ArcReactorCyan)
                                }
                            }
                        }
                    }
                    1 -> {
                        // TAB 1: Chat interface panel
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "COMMUNICATION NETWORKS LOG",
                                fontSize = 12.sp,
                                color = hudColor,
                                letterSpacing = 2.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            // Conversations listing view
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF0F172A).copy(alpha = 0.5f))
                                    .border(1.dp, HologramBorder.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                    .padding(8.dp)
                            ) {
                                if (logs.isEmpty()) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ChatBubbleOutline,
                                            contentDescription = "No command logs",
                                            tint = HologramTextDim,
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "No communication databases active.",
                                            fontSize = 12.sp,
                                            color = Color.White.copy(alpha = 0.4f),
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        reverseLayout = false
                                    ) {
                                        items(logs.reversed()) { log ->
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 6.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (log.success) GlassyCyan else JarvisAccentRed.copy(alpha = 0.08f))
                                                    .border(
                                                        1.dp,
                                                        if (log.success) HologramBorder.copy(alpha = 0.15f) else JarvisAccentRed.copy(alpha = 0.2f),
                                                        RoundedCornerShape(8.dp)
                                                    )
                                                    .padding(10.dp)
                                            ) {
                                                Row(
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text(
                                                        text = "MASTER Command",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = hudColor,
                                                        fontFamily = FontFamily.Monospace
                                                    )
                                                    Text(
                                                        text = if (log.success) "SUCCESS" else "FAIL",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (log.success) ArcReactorCyan else JarvisAccentRed,
                                                        fontFamily = FontFamily.Monospace
                                                    )
                                                }
                                                Text(
                                                    text = log.commandText,
                                                    fontSize = 13.sp,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(top = 2.dp, bottom = 6.dp)
                                                )
                                                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                                                Text(
                                                    text = "JARVIS: " + log.response,
                                                    fontSize = 12.sp,
                                                    color = Color(0xFFD2E6FF),
                                                    modifier = Modifier.padding(top = 6.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Smart entry prompt builder
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = currentPrompt,
                                    onValueChange = { viewModel.updatePrompt(it) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 8.dp)
                                        .testTag("command_input"),
                                    placeholder = { Text("Compile command logic...", color = Color.White.copy(alpha = 0.3f), fontSize = 12.sp) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = hudColor,
                                        unfocusedBorderColor = HologramBorder.copy(alpha = 0.3f),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(hudColor.copy(alpha = 0.2f))
                                        .border(1.dp, hudColor, RoundedCornerShape(12.dp))
                                        .clickable {
                                            viewModel.executeVoiceCommand(currentPrompt)
                                        }
                                        .testTag("send_command_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = "Send execution parameters",
                                        tint = hudColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                    2 -> {
                        // TAB 2: Device control cockpit panel
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "SYSTEM MAINFRAME ACTUATORS",
                                fontSize = 12.sp,
                                color = hudColor,
                                letterSpacing = 2.sp,
                                fontFamily = FontFamily.Monospace
                            )

                            // Quick trigger grid
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                MatrixControlCard(
                                    title = "FLASHLIGHT CORE",
                                    statusLabel = "TRANSIT ON/OFF",
                                    icon = Icons.Default.FlashlightOn,
                                    color = hudColor,
                                    modifier = Modifier.weight(1f),
                                    onClick = { viewModel.executeVoiceCommand("turn on flashlight") }
                                )
                                MatrixControlCard(
                                    title = "STEALTH STEPS",
                                    statusLabel = "SILENT PROFILE",
                                    icon = Icons.Default.VolumeMute,
                                    color = JarvisAccentGold,
                                    modifier = Modifier.weight(1f),
                                    onClick = { viewModel.executeVoiceCommand("mute phone") }
                                )
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                MatrixControlCard(
                                    title = "WI-FI ROUTE",
                                    statusLabel = "LAUNCH INTERFACE",
                                    icon = Icons.Default.Wifi,
                                    color = hudColor,
                                    modifier = Modifier.weight(1f),
                                    onClick = { viewModel.executeVoiceCommand("wifi") }
                                )
                                MatrixControlCard(
                                    title = "BLUETOOTH LINK",
                                    statusLabel = "LAUNCH PAIRING",
                                    icon = Icons.Default.Bluetooth,
                                    color = hudColor,
                                    modifier = Modifier.weight(1f),
                                    onClick = { viewModel.executeVoiceCommand("bluetooth") }
                                )
                            }

                            // Dynamic sliders / meters
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF0F172A).copy(alpha = 0.5f))
                                    .border(1.dp, HologramBorder.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                    .padding(14.dp)
                            ) {
                                Text(
                                    text = "SONIC ATTENUATOR LEVEL",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontFamily = FontFamily.Monospace
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.VolumeUp, contentDescription = "Volume icon", tint = hudColor, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Slider(
                                        value = 0.66f,
                                        onValueChange = { viewModel.executeVoiceCommand("volume to " + (it * 100).toInt()) },
                                        colors = SliderDefaults.colors(
                                            thumbColor = hudColor,
                                            activeTrackColor = hudColor,
                                            inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("66%", fontSize = 11.sp, color = hudColor, fontFamily = FontFamily.Monospace)
                                }
                            }

                            // Diagnostics list
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(GlassyCyan)
                                    .border(1.dp, HologramBorder.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "CORE DIAGNOSTIC METRIC CELLS",
                                        fontSize = 10.sp,
                                        color = hudColor,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                    HorizontalDivider(color = hudColor.copy(alpha = 0.15f))
                                    DiagnosticLogLine(label = "ARC REACTOR THERMAL STATUS", value = "COMPUTED STABLE | 34.2 °C", isOk = true)
                                    DiagnosticLogLine(label = "ACCESSIBILITY PERMISSION GRID", value = if (isAccessibilityActive) "ONLINE & READY" else "OFFLINE GRID", isOk = isAccessibilityActive)
                                    DiagnosticLogLine(label = "BIOMETRICS INTEGRITY SEGMENT", value = "SECURED MAIN CORE", isOk = true)
                                }
                            }
                        }
                    }
                    3 -> {
                        // TAB 3: File Storage Manager sector audit
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "SECTOR SECURITY AND FILES MEMORY CELL AUDIT",
                                fontSize = 11.sp,
                                color = hudColor,
                                letterSpacing = 2.sp,
                                fontFamily = FontFamily.Monospace
                            )

                            // Glassmorphic storage statistics ring simulator
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF0F172A).copy(alpha = 0.5f))
                                    .border(1.dp, HologramBorder.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                                    .padding(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "MAIN HARDWARE DISK SIZE : 256 GB",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Color.White.copy(alpha = 0.4f),
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "USED STORAGE BLOCK IN DECAY",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "JARVIS modules scanned 74.2% block segments.",
                                            fontSize = 11.sp,
                                            color = HologramTextDim
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.size(72.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            progress = { simulatedStorageUsed },
                                            modifier = Modifier.size(72.dp),
                                            color = hudColor,
                                            strokeWidth = 6.dp,
                                            trackColor = Color.White.copy(alpha = 0.1f)
                                        )
                                        Text(
                                            text = "74%",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }

                            // Folder listing details
                            Text(
                                text = "MEMORY SECTOR DIRECTORIES",
                                fontSize = 10.sp,
                                color = hudColor,
                                fontFamily = FontFamily.Monospace
                            )

                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                item {
                                    FolderFileSelectorRow(title = "JARVIS_CORES", details = "12 files | 42.4 GB", icon = Icons.Default.FolderSpecial, color = hudColor)
                                }
                                item {
                                    FolderFileSelectorRow(title = "SECURE_VAULT", details = "4 files | 1.8 GB", icon = Icons.Default.Lock, color = JarvisAccentGold)
                                }
                                item {
                                    FolderFileSelectorRow(title = "DOWNLOAD_SECTORS", details = "142 files | 12.1 GB", icon = Icons.Default.DownloadForOffline, color = hudColor)
                                }
                                item {
                                    FolderFileSelectorRow(title = "TELEMETRY_MAIN_LOGS", details = "890 records | 148 MB", icon = Icons.Default.Terminal, color = hudColor)
                                }
                            }
                        }
                    }
                    4 -> {
                        // TAB 4: Core configuration settings panel
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "CORE CONFIGURATION CELL",
                                fontSize = 12.sp,
                                color = hudColor,
                                letterSpacing = 2.sp,
                                fontFamily = FontFamily.Monospace
                            )

                            // Setup continuous vs push mode
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF0F172A).copy(alpha = 0.5f))
                                    .border(1.dp, HologramBorder.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                    .padding(14.dp)
                            ) {
                                Text(
                                    text = "INTELLIGENT SPEECH ACQUISITION MODE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                val modesList = listOf(
                                    Pair("push_to_talk", "PUSH-TO-TALK ACTIVE"),
                                    Pair("always_listening", " Hey Jarvis WAKE WORD ACTIVATED")
                                )

                                modesList.forEach { (key, name) ->
                                    val isModeActive = key == voiceMode
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isModeActive) GlassyCyan else Color.Transparent)
                                            .clickable { viewModel.setVoiceMode(key) }
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = name,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isModeActive) hudColor else Color.White.copy(alpha = 0.7f),
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clip(CircleShape)
                                                .background(if (isModeActive) hudColor else Color.White.copy(alpha = 0.1f))
                                        )
                                    }
                                }
                            }

                            // Master Accessibility settings triggering launcher
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF0F172A).copy(alpha = 0.5f))
                                    .border(1.dp, HologramBorder.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "AUTOMATION SERVICES CONFIGURATION",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontFamily = FontFamily.Monospace
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = "Accessibility Service Node", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text(text = "Controls automated writing actions", fontSize = 10.sp, color = HologramTextDim)
                                    }
                                    Switch(
                                        checked = isAccessibilityActive,
                                        onCheckedChange = {
                                            try {
                                                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                }
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                // Fail silently
                                            }
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = hudColor,
                                            checkedTrackColor = hudColor.copy(alpha = 0.35f)
                                        )
                                    )
                                }
                            }

                            // Erase/Purge Databases
                            Button(
                                onClick = { viewModel.clearAllCommandHistory() },
                                colors = ButtonDefaults.buttonColors(containerColor = JarvisAccentRed.copy(alpha = 0.12f)),
                                border = BorderStroke(1.dp, JarvisAccentRed.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.DeleteForever, contentDescription = "Purge logs", tint = JarvisAccentRed)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("PURGE COMMAND DATABASES LOGS", fontSize = 11.sp, color = Color.White, fontFamily = FontFamily.Monospace)
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            // Framework Footer Diagnostics
                            Text(
                                text = "STARK WORKSPACE CELL ID: fb3e18ec\nJARVIS SPEECH CORE v9.2.22",
                                color = Color.White.copy(alpha = 0.15f),
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // 3. Cyber Dock Bottom Navigation Tabs
            NavigationBar(
                containerColor = JarvisDarkBackground,
                tonalElevation = 8.dp,
                modifier = Modifier.navigationBarsPadding()
            ) {
                listOf(
                    Pair(Icons.Default.Adjust, "HUD REACTOR"),
                    Pair(Icons.Default.ChatBubbleOutline, "COMMS"),
                    Pair(Icons.Default.DeveloperMode, "COCKPIT"),
                    Pair(Icons.Default.FolderOpen, "SECTOR"),
                    Pair(Icons.Default.Settings, "DECAY")
                ).forEachIndexed { index, pair ->
                    val isTabSelected = index == activeTab
                    NavigationBarItem(
                        selected = isTabSelected,
                        onClick = { activeTab = index },
                        icon = {
                            Icon(
                                imageVector = pair.first,
                                contentDescription = pair.second,
                                tint = if (isTabSelected) hudColor else Color.White.copy(alpha = 0.4f)
                            )
                        },
                        label = {
                            Text(
                                text = pair.second,
                                fontSize = 8.sp,
                                color = if (isTabSelected) hudColor else Color.White.copy(alpha = 0.4f),
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = hudColor.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun MatrixControlCard(
    title: String,
    statusLabel: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0F172A).copy(alpha = 0.5f))
            .border(1.dp, color.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Column {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = statusLabel,
                fontSize = 9.sp,
                color = Color.White.copy(alpha = 0.4f),
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun DiagnosticLogLine(label: String, value: String, isOk: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            color = Color.White.copy(alpha = 0.5f),
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = value,
            fontSize = 9.sp,
            color = if (isOk) ArcReactorCyan else JarvisAccentRed,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun FolderFileSelectorRow(
    title: String,
    details: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    var scanned by remember { mutableStateOf(false) }
    var runningScan by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0F172A).copy(alpha = 0.5f))
            .border(1.dp, color.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
            .clickable {
                if (!scanned && !runningScan) {
                    runningScan = true
                }
            }
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(
                    text = if (runningScan) "AUDITING SYSTEM SECTORS..." else if (scanned) "$details | SECURE" else details,
                    fontSize = 10.sp,
                    color = if (runningScan) color else Color.White.copy(alpha = 0.4f),
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(color.copy(alpha = 0.15f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = if (runningScan) "SCAN" else if (scanned) "VERIFIED" else "AUDIT",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontFamily = FontFamily.Monospace
            )
        }
    }

    if (runningScan) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(1800)
            runningScan = false
            scanned = true
        }
    }
}
