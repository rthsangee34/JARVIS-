package com.example.ai

import android.util.Log
import com.example.BuildConfig
import com.example.network.Content
import com.example.network.GenerateContentRequest
import com.example.network.GenerationConfig
import com.example.network.Part
import com.example.network.RetrofitClient
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

data class ProcessedCommand(
    val speechResponse: String,
    val actionType: String, // "OPEN_APP", "TOGGLE_FLASHLIGHT", "TYPE_TEXT", "VOLUME_ADJUST", "WIFI_TOGGLE", "BLUETOOTH_TOGGLE", "WEB_SEARCH", "CHAT", "SILENT_MODE", "BATTERY_CHECK"
    val actionParameter: String = "",
    val isSuccess: Boolean = true
)

class JarvisIntentEngine {

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    /**
     * Translates a raw spoken or typed command into structured physical trigger parameters.
     */
    suspend fun processCommand(rawText: String, batteryLevel: Int): ProcessedCommand {
        val cleanedText = rawText.lowercase().trim()

        // 1. Core Rule-Based Fast Handlers (Instant response even offline)
        val localMatch = checkForLocalIntent(cleanedText, batteryLevel)
        if (localMatch != null) {
            return localMatch
        }

        // 2. Multi-turn AI Intent Mapping Powered by Gemini 3.5 Flash
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w("JarvisIntentEngine", "Gemini API key is unconfigured. Operating in Offline Smart Simulator Mode.")
            return generateOfflineModelAlternative(cleanedText, batteryLevel)
        }

        return try {
            val systemSystemPrompt = """
                You are J.A.R.V.I.S., the ultimate loyal and highly advanced cybernetic AI assistant modeled after Tony Stark's companion.
                Your task is to analyze user commands and map them directly into a system automation action.
                You MUST return your response as a valid JSON object matching the following outline:
                {
                  "speechResponse": "polite vocal response text JARVIS speaks",
                  "actionType": "OPEN_APP", "TOGGLE_FLASHLIGHT", "TYPE_TEXT", "VOLUME_ADJUST", "WIFI_TOGGLE", "BLUETOOTH_TOGGLE", "WEB_SEARCH", "CHAT", "SILENT_MODE", "BATTERY_CHECK",
                  "actionParameter": "target parameter string (e.g. app name, text to type, or search term)"
                }
                Current system metrics for context: Battery level is $batteryLevel%.
                Ensure JSON is formatted cleanly. Do not wrap in markdown or block codes.
            """.trimIndent()

            val request = GenerateContentRequest(
                contents = listOf(Content(parts = listOf(Part(text = rawText)))),
                generationConfig = GenerationConfig(
                    temperature = 0.5f,
                    responseMimeType = "application/json"
                ),
                systemInstruction = Content(parts = listOf(Part(text = systemSystemPrompt)))
            )

            val rawResponse = RetrofitClient.service.generateContent(
                model = "gemini-3.5-flash",
                apiKey = apiKey,
                request = request
            )

            val jsonText = rawResponse.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!jsonText.isNullOrEmpty()) {
                parseJsonCommand(jsonText)
            } else {
                generateOfflineModelAlternative(cleanedText, batteryLevel)
            }
        } catch (e: Exception) {
            Log.e("JarvisIntentEngine", "Gemini transaction error. Falling back to offline heuristics.", e)
            generateOfflineModelAlternative(cleanedText, batteryLevel)
        }
    }

    private fun parseJsonCommand(json: String): ProcessedCommand {
        return try {
            // Clean markdown syntax wrapping if model bypassed configuration
            var sanit = json.trim()
            if (sanit.startsWith("```json")) {
                sanit = sanit.removePrefix("```json")
            }
            if (sanit.endsWith("```")) {
                sanit = sanit.removeSuffix("```")
            }
            sanit = sanit.trim()

            val adapter = moshi.adapter(Map::class.java)
            val map = adapter.fromJson(sanit) ?: throw Exception("Invalid JSON structure")
            
            val speechStr = map["speechResponse"]?.toString() ?: "Operational parameters recognized."
            val actionStr = map["actionType"]?.toString() ?: "CHAT"
            val paramStr = map["actionParameter"]?.toString() ?: ""

            ProcessedCommand(
                speechResponse = speechStr,
                actionType = actionStr,
                actionParameter = paramStr,
                isSuccess = true
            )
        } catch (e: Exception) {
            Log.e("JarvisIntentEngine", "JSON parsing issue: $json", e)
            ProcessedCommand(
                speechResponse = "Parsing transaction complete. Executing as high-order conversational chat.",
                actionType = "CHAT",
                actionParameter = json
            )
        }
    }

    private fun checkForLocalIntent(text: String, batteryLevel: Int): ProcessedCommand? {
        return when {
            text.startsWith("open ") -> {
                val app = text.removePrefix("open ").trim()
                ProcessedCommand(
                    speechResponse = "Accessing hardware mainframes. Launching ${app.uppercase()} now.",
                    actionType = "OPEN_APP",
                    actionParameter = app
                )
            }
            text.contains("flashlight on") || text.contains("turn on flashlight") || text.contains("enable light") -> {
                ProcessedCommand(
                    speechResponse = "Shoring backup energy. Core flashlight beam is now fully illuminated.",
                    actionType = "TOGGLE_FLASHLIGHT",
                    actionParameter = "true"
                )
            }
            text.contains("flashlight off") || text.contains("turn off flashlight") || text.contains("disable light") -> {
                ProcessedCommand(
                    speechResponse = "Deactivating focal beam. Light systems powered down.",
                    actionType = "TOGGLE_FLASHLIGHT",
                    actionParameter = "false"
                )
            }
            text.startsWith("type ") || text.startsWith("write ") -> {
                val statement = text.substringAfter("type").trim()
                    .removePrefix("text")
                    .removePrefix("message")
                    .trim()
                ProcessedCommand(
                    speechResponse = "Focused input stream detected. Transferring string sequences to system controller.",
                    actionType = "TYPE_TEXT",
                    actionParameter = statement
                )
            }
            text.contains("search youtube for ") || text.startsWith("youtube ") -> {
                val query = text.substringAfter("search youtube for ").removePrefix("search ").trim()
                ProcessedCommand(
                    speechResponse = "Initiating communications relay. Querying YouTube servers for $query.",
                    actionType = "WEB_SEARCH",
                    actionParameter = "https://www.youtube.com/results?search_query=" + query.replace(" ", "+")
                )
            }
            text.contains("google ") || text.contains("search for ") || text.contains("search web for ") -> {
                val query = text.substringAfter("search for ")
                    .substringAfter("search web for ")
                    .substringAfter("google ")
                    .trim()
                ProcessedCommand(
                    speechResponse = "Accessing external network cells. Looking up information for $query.",
                    actionType = "WEB_SEARCH",
                    actionParameter = "https://www.google.com/search?q=" + query.replace(" ", "+")
                )
            }
            text.contains("wifi") || text.contains("wi-fi") -> {
                ProcessedCommand(
                    speechResponse = "Splicing local network parameters. Loading Wi-Fi connections panel, Master.",
                    actionType = "WIFI_TOGGLE"
                )
            }
            text.contains("bluetooth") -> {
                ProcessedCommand(
                    speechResponse = "Synching peripheral relays. Pulling Bluetooth connection matrices.",
                    actionType = "BLUETOOTH_TOGGLE"
                )
            }
            text.contains("silent mode") || text.contains("mute phone") -> {
                ProcessedCommand(
                    speechResponse = "Command acknowledged. Entering stealth parameters. Device set to silent mode.",
                    actionType = "SILENT_MODE"
                )
            }
            text.contains("battery") -> {
                ProcessedCommand(
                    speechResponse = "Master, arc reactor cores are operating at $batteryLevel% remaining capacity.",
                    actionType = "BATTERY_CHECK"
                )
            }
            else -> null
        }
    }

    private fun generateOfflineModelAlternative(text: String, batteryLevel: Int): ProcessedCommand {
        // Conversational response synthesis (heuristics database)
        val speech = when {
            text.contains("hello") || text.contains("hi") || text.contains("hey jarvis") -> {
                "Hello, Master. JARVIS mainframes are active, listening and ready for your next automation command."
            }
            text.contains("who are you") || text.contains("your name") -> {
                "I am J.A.R.V.I.S, your cybernetic assistant, engineered to manage tasks, automate integrations, and command security parameters."
            }
            text.contains("how are you") || text.contains("status") -> {
                "System diagnostics are fully sound, Master. Internal mainframes are holding stable at optimal computational temperatures. Core power allocation is at $batteryLevel%."
            }
            text.contains("thank you") || text.contains("thanks") -> {
                "The pleasure is entirely mine, Master Stark. Always at your disposal."
            }
            else -> {
                "Computing requested calculations: '$text'. I am currently operating offline, but I have registered this record in the main log cells."
            }
        }
        return ProcessedCommand(
            speechResponse = speech,
            actionType = "CHAT",
            actionParameter = text
        )
    }
}
