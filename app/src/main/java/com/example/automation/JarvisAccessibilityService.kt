package com.example.automation

import android.accessibilityservice.AccessibilityService
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class JarvisAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("JarvisAccessibility", "Accessibility Service Connected")
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Monitor events if needed, like keeping track of current window
    }

    override fun onInterrupt() {
        Log.d("JarvisAccessibility", "Accessibility Service Interrupted")
        instance = null
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    /**
     * Types text into the currently focused input field.
     */
    fun typeTextInFocusedField(text: String): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        val focusedNode = findFocusedNode(rootNode)
        return if (focusedNode != null) {
            val arguments = Bundle().apply {
                putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
            }
            val result = focusedNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
            focusedNode.recycle()
            result
        } else {
            false
        }
    }

    /**
     * Traverses the visual node tree to locate the active focused input field.
     */
    private fun findFocusedNode(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.isFocused && (node.className == "android.widget.EditText" || node.isEditable)) {
            return node
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = findFocusedNode(child)
            if (found != null) {
                return found
            }
        }
        return null
    }

    /**
     * Finds a button or element matching the target text and clicks it.
     */
    fun clickElementByText(targetText: String): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        val list = rootNode.findAccessibilityNodeInfosByText(targetText)
        for (node in list) {
            if (node.isClickable) {
                val success = node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                node.recycle()
                if (success) return true
            } else {
                // Try parent if child is not clickable
                var parent = node.parent
                while (parent != null) {
                    if (parent.isClickable) {
                        val success = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        parent.recycle()
                        if (success) return true
                    }
                    parent = parent.parent
                }
            }
        }
        return false
    }

    /**
     * Performs a system-wide hardware action.
     */
    fun performSystemAction(actionId: Int): Boolean {
        return performGlobalAction(actionId)
    }

    companion object {
        @Volatile
        var instance: JarvisAccessibilityService? = null
            private set

        val isServiceRunning: Boolean
            get() = instance != null
    }
}
