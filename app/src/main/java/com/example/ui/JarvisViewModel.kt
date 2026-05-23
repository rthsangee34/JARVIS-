package com.example.ui

import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.database.AppDatabase
import com.example.database.CommandLog
import com.example.database.JarvisRepository
import com.example.automation.DeviceController
import com.example.automation.JarvisAccessibilityService
import com.example.ai.JarvisIntentEngine
import com.example.voice.JarvisVoiceEngine
import com.example.voice.VoiceState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class JarvisViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val database = AppDatabase.getDatabase(context)
    private val repository = JarvisRepository(database.jarvisDao())
    private val deviceController = DeviceController(context)
    private val intentEngine = JarvisIntentEngine()

    private var voiceEngine: JarvisVoiceEngine? = null

    // UI States
    private val _voiceState = MutableStateFlow(VoiceState.IDLE)
    val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

    private val _isBooted = MutableStateFlow(false)
    val isBooted: StateFlow<Boolean> = _isBooted.asStateFlow()

    private val _bootProgress = MutableStateFlow(0f)
    val bootProgress: StateFlow<Float> = _bootProgress.asStateFlow()

    private val _bootStatusText = MutableStateFlow("POWER CORE: OFFLINE")
    val bootStatusText: StateFlow<String> = _bootStatusText.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _uiThemeColorIndex = MutableStateFlow(0) // 0 for Blue HUD, 1 for Gold/Orange, 2 for Red Warning
    val uiThemeColorIndex: StateFlow<Int> = _uiThemeColorIndex.asStateFlow()

    private val _currentPrompt = MutableStateFlow("")
    val currentPrompt: StateFlow<String> = _currentPrompt.asStateFlow()

    private val _batteryStatus = MutableStateFlow(100)
    val batteryStatus: StateFlow<Int> = _batteryStatus.asStateFlow()

    private val _accessibilityActive = MutableStateFlow(false)
    val accessibilityActive: StateFlow<Boolean> = _accessibilityActive.asStateFlow()

    private val _voiceMode = MutableStateFlow("push_to_talk") // "push_to_talk", "always_listening", "offline_assistant", "background"
    val voiceMode: StateFlow<String> = _voiceMode.asStateFlow()

    private val _userPreferencesName = MutableStateFlow("Tony Stark")
    val userPreferencesName: StateFlow<String> = _userPreferencesName.asStateFlow()

    // File Manager simulated state
    private val _simulatedStorageLoad = MutableStateFlow(0.74f) // 74% used
    val simulatedStorageLoad: StateFlow<Float> = _simulatedStorageLoad.asStateFlow()

    // Query logs flow
    val commandLogs: StateFlow<List<CommandLog>> = repository.commandLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Sync states initially
        updateSystemMetrics()
        runStartupSequence()
        
        // Initialize voice engines on main looper
        Handler(Looper.getMainLooper()).post {
            try {
                voiceEngine = JarvisVoiceEngine(
                    context = context,
                    onStateChange = { newState -> _voiceState.value = newState },
                    onSpeechResult = { text -> executeVoiceCommand(text) },
                    onError = { err -> 
                        Log.e("JarvisViewModel", "Voice Engine error: $err")
                        viewModelScope.launch {
                            repository.logCommand("VOICE_MIC_FAIL", err, "NONE", false)
                        }
                    }
                )
            } catch (e: Exception) {
                Log.e("JarvisViewModel", "Could not fire Speech components in sandbox core", e)
            }
        }
    }

    private fun runStartupSequence() {
        viewModelScope.launch {
            _bootStatusText.value = "CONNECTING ARC REACTOR..."
            kotlinx.coroutines.delay(400)
            _bootProgress.value = 0.25f
            _bootStatusText.value = "CHARGING ION THRUSTERS: 25%"
            kotlinx.coroutines.delay(400)
            _bootProgress.value = 0.55f
            _bootStatusText.value = "SENSORY NETWORKS ONLINE: 55%"
            kotlinx.coroutines.delay(400)
            _bootProgress.value = 0.85f
            _bootStatusText.value = "INTEGRATING AUTOMATION CORES: 85%"
            kotlinx.coroutines.delay(400)
            _bootProgress.value = 1.0f
            _bootStatusText.value = "J.A.R.V.I.S VERSION 9.0 COMPLETED"
            kotlinx.coroutines.delay(300)
            _isBooted.value = true
        }
    }

    fun toggleBiometricMockAuth() {
        _isAuthenticated.value = !_isAuthenticated.value
        if (_isAuthenticated.value) {
            voiceEngine?.speak("Security protocol bypassed. Welcome back, Master.")
        } else {
            voiceEngine?.speak("Mainframes locked. Scanning biometrics.")
        }
    }

    fun updatePrompt(text: String) {
        _currentPrompt.value = text
    }

    fun setVoiceMode(mode: String) {
        _voiceMode.value = mode
        viewModelScope.launch {
            repository.saveSetting("VOICE_MODE", mode)
        }
        voiceEngine?.speak("Operational mode configured to " + mode.replace("_", " ") + ".")
    }

    fun setHUDTheme(index: Int) {
        _uiThemeColorIndex.value = index
    }

    fun speakText(text: String) {
        voiceEngine?.speak(text)
    }

    fun startVoiceMic() {
        voiceEngine?.startListening()
    }

    fun stopVoiceMic() {
        voiceEngine?.stopListening()
    }

    fun updateSystemMetrics() {
        _batteryStatus.value = deviceController.getBatteryLevel()
        _accessibilityActive.value = JarvisAccessibilityService.isServiceRunning
    }

    /**
     * Entry point to execute commands, typed or spoken.
     */
    fun executeVoiceCommand(text: String) {
        if (text.isEmpty()) return
        
        _currentPrompt.value = ""
        _voiceState.value = VoiceState.THINKING

        viewModelScope.launch {
            // Fetch battery status context
            val battery = deviceController.getBatteryLevel()
            val processed = intentEngine.processCommand(text, battery)

            // Speak the response voice synthesizer
            voiceEngine?.speak(processed.speechResponse)

            // Execute the system automation action
            val actionResult = executeAutomation(processed.actionType, processed.actionParameter)

            // Logger to Room database
            repository.logCommand(
                commandText = text,
                response = processed.speechResponse,
                actionType = processed.actionType,
                success = actionResult
            )

            // Sync permission levels and battery icons
            updateSystemMetrics()
        }
    }

    /**
     * Executes the concrete hardware/software action corresponding to the intent.
     */
    private fun executeAutomation(actionType: String, parameter: String): Boolean {
        Log.d("AutomationCore", "Executing $actionType with parameter '$parameter'")
        
        return when (actionType) {
            "OPEN_APP" -> {
                deviceController.openApp(parameter)
                true
            }
            "TOGGLE_FLASHLIGHT" -> {
                val shouldEnable = parameter.lowercase() == "true"
                deviceController.toggleFlashlight(shouldEnable)
            }
            "TYPE_TEXT" -> {
                val service = JarvisAccessibilityService.instance
                if (service != null && JarvisAccessibilityService.isServiceRunning) {
                    service.typeTextInFocusedField(parameter)
                } else {
                    // Try clipboard option or alert accessibility is off
                    deviceController.openApp("whatsapp")
                    false
                }
            }
            "VOLUME_ADJUST" -> {
                val level = parameter.filter { it.isDigit() }.toIntOrNull() ?: 50
                deviceController.setVolume(level)
            }
            "WIFI_TOGGLE" -> {
                deviceController.toggleWiFi()
                true
            }
            "BLUETOOTH_TOGGLE" -> {
                deviceController.toggleBluetooth()
                true
            }
            "WEB_SEARCH" -> {
                deviceController.launchUrl(parameter)
                true
            }
            "SILENT_MODE" -> {
                deviceController.enableSilentMode(true)
            }
            "BATTERY_CHECK" -> {
                true
            }
            "CHAT" -> {
                true
            }
            else -> {
                // Default web fallback
                if (parameter.isNotEmpty()) {
                    deviceController.launchUrl("https://www.google.com/search?q=" + java.net.URLEncoder.encode(parameter, "UTF-8"))
                    true
                } else {
                    false
                }
            }
        }
    }

    fun clearAllCommandHistory() {
        viewModelScope.launch {
            repository.clearLogs()
        }
        voiceEngine?.speak("Records database purged successfully.")
    }

    override fun onCleared() {
        super.onCleared()
        voiceEngine?.destroy()
    }
}
