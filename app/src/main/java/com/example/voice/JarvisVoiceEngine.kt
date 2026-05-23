package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class JarvisVoiceEngine(
    private val context: Context,
    private val onStateChange: (VoiceState) -> Unit,
    private val onSpeechResult: (String) -> Unit,
    private val onError: (String) -> Unit
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var isTtsReady = false

    init {
        // Initialize Text to Speech
        tts = TextToSpeech(context, this)

        // Initialize Speech Recognizer on UI Thread (must be context's main looper)
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(JarvisRecognitionListener())
            }
        } else {
            Log.e("JarvisVoiceEngine", "Speech recognition is not available on this device")
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("JarvisVoiceEngine", "US English is not supported or missing data")
                onError("Voice synthesis language not fully supported")
            } else {
                isTtsReady = true
                tts?.setPitch(1.0f)
                tts?.setSpeechRate(1.1f) // Calm, crisp intelligence speed
                speak("Systems fully initialized. Master, JARVIS is online.")
            }
        } else {
            Log.e("JarvisVoiceEngine", "TTS Initialization failed")
            onError("Voice synthesis initialization failed")
        }
    }

    fun speak(text: String) {
        if (isTtsReady && tts != null) {
            onStateChange(VoiceState.SPEAKING)
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "JARVIS_RESPONSE_ID")
        } else {
            Log.e("JarvisVoiceEngine", "TTS is not ready yet")
        }
    }

    fun startListening() {
        if (speechRecognizer == null) {
            onError("Speech engine unavailable. Typing prompt is fully functional below.")
            return
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }
        try {
            tts?.stop() // Shut up when user wants to talk
            speechRecognizer?.startListening(intent)
            onStateChange(VoiceState.LISTENING)
        } catch (e: Exception) {
            Log.e("JarvisVoiceEngine", "Failed starting speech recognition", e)
            onError("Could not initialize voice microphone")
        }
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        onStateChange(VoiceState.IDLE)
    }

    fun destroy() {
        tts?.stop()
        tts?.shutdown()
        speechRecognizer?.destroy()
    }

    private inner class JarvisRecognitionListener : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            onStateChange(VoiceState.LISTENING)
        }

        override fun onBeginningOfSpeech() {
            onStateChange(VoiceState.LISTENING)
        }

        override fun onRmsChanged(rmsdB: Float) {
            // Can be used for custom live orb visualizer scaling
        }

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {
            onStateChange(VoiceState.THINKING)
        }

        override fun onError(error: Int) {
            val errorMessage = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                SpeechRecognizer.ERROR_CLIENT -> "Client speech component error"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Audio permission not granted"
                SpeechRecognizer.ERROR_NETWORK -> "Network error inside speech recognizer"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout during speech search"
                SpeechRecognizer.ERROR_NO_MATCH -> "No command detected"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Mic engine is currently busy"
                SpeechRecognizer.ERROR_SERVER -> "Server protocol error"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Voice timeout: Speake sooner"
                else -> "Speech recognition interrupted"
            }
            Log.e("JarvisVoiceEngine", "Recognition error: $errorMessage ($error)")
            onStateChange(VoiceState.IDLE)
            if (error != SpeechRecognizer.ERROR_NO_MATCH && error != SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                onError(errorMessage)
            }
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                onSpeechResult(matches[0])
            } else {
                onStateChange(VoiceState.IDLE)
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            // Real-time voice visual stream
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }
}

enum class VoiceState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING
}
