package com.example.automation

import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.BatteryManager
import android.provider.Settings
import android.util.Log

class DeviceController(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager

    /**
     * Controls the device flashlight.
     */
    fun toggleFlashlight(enable: Boolean): Boolean {
        return try {
            val cameraId = cameraManager?.cameraIdList?.firstOrNull()
            if (cameraId != null) {
                cameraManager?.setTorchMode(cameraId, enable)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("DeviceController", "Flashlight execution error", e)
            false
        }
    }

    /**
     * Toggles Wi-Fi or opens Wi-Fi control center panel.
     */
    fun toggleWiFi() {
        try {
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("DeviceController", "WiFi activation error", e)
        }
    }

    /**
     * Toggles Bluetooth or opens Bluetooth configurations.
     */
    fun toggleBluetooth() {
        try {
            val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("DeviceController", "Bluetooth settings launch error", e)
        }
    }

    /**
     * Adjusts the system media stream volume.
     */
    fun setVolume(percentage: Int): Boolean {
        val manager = audioManager ?: return false
        return try {
            val maxVolume = manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val volumeValue = (maxVolume * (percentage / 100f)).toInt()
            manager.setStreamVolume(AudioManager.STREAM_MUSIC, volumeValue, AudioManager.FLAG_SHOW_UI)
            true
        } catch (e: Exception) {
            Log.e("DeviceController", "Volume set error", e)
            false
        }
    }

    /**
     * Launches external apps by their common packages, or searches the App store.
     */
    fun openApp(appName: String): String {
        val cleaned = appName.lowercase().trim()
        val packageName = when {
            cleaned.contains("chrome") -> "com.android.chrome"
            cleaned.contains("whatsapp") -> "com.whatsapp"
            cleaned.contains("youtube") -> "com.google.android.youtube"
            cleaned.contains("map") -> "com.google.android.apps.maps"
            cleaned.contains("gmail") -> "com.google.android.gm"
            cleaned.contains("spotify") -> "com.spotify.music"
            cleaned.contains("settings") -> "com.android.settings"
            else -> null
        }

        if (packageName != null) {
            try {
                val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    return "Opening $appName."
                }
            } catch (e: Exception) {
                Log.e("DeviceController", "Cannot find direct intent for $packageName", e)
            }
        }

        // Search packageManager for any app containing the name
        try {
            val pm = context.packageManager
            val apps = pm.getInstalledApplications(0)
            for (app in apps) {
                val label = pm.getApplicationLabel(app).toString().lowercase()
                if (label.contains(cleaned) || cleaned.contains(label)) {
                    val launchIntent = pm.getLaunchIntentForPackage(app.packageName)
                    if (launchIntent != null) {
                        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(launchIntent)
                        return "Opening ${pm.getApplicationLabel(app)}."
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("DeviceController", "App package search error", e)
        }

        // Fallback to website launch
        val searchQuery = Uri.encode(appName)
        return if (cleaned.contains("youtube")) {
            launchUrl("https://www.youtube.com")
            "Opening YouTube stream console."
        } else {
            launchUrl("https://www.google.com/search?q=$searchQuery")
            "App $appName not detected. Performing global web search."
        }
    }

    /**
     * Seamlessly launches full web URLs in secure environments.
     */
    fun launchUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("DeviceController", "Launch URL error", e)
        }
    }

    /**
     * Reads current percentage battery from the BatteryManager service.
     */
    fun getBatteryLevel(): Int {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        return bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1
    }

    /**
     * Mutes/Unmutes phone or sets silent mode based on context.
     */
    fun enableSilentMode(enable: Boolean): Boolean {
        val manager = audioManager ?: return false
        return try {
            val mode = if (enable) AudioManager.RINGER_MODE_SILENT else AudioManager.RINGER_MODE_NORMAL
            manager.ringerMode = mode
            true
        } catch (e: Exception) {
            Log.e("DeviceController", "Silent mode configuration error", e)
            // Launcher could open Do Not Disturb configuration as alternate if permissions restrict
            try {
                val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                true
            } catch (ex: Exception) {
                false
            }
        }
    }
}
