
package com.example.shortslock

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class ShortsAccessibilityService : AccessibilityService() {

    private var inShorts = false

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.packageName == "com.google.android.youtube") {
            val rootInActiveWindow = rootInActiveWindow
            val isShorts = isShortsUI(rootInActiveWindow)

            if (isShorts && !inShorts) {
                inShorts = true
                sendBroadcast(Intent("RESUME_TIMER"))
            } else if (!isShorts && inShorts) {
                inShorts = false
                sendBroadcast(Intent("PAUSE_TIMER"))
            }
        }
    }

    private fun isShortsUI(nodeInfo: AccessibilityNodeInfo?): Boolean {
        if (nodeInfo == null) {
            return false
        }

        val nodes = nodeInfo.findAccessibilityNodeInfosByText("Shorts")
        if (nodes.isNotEmpty()) {
            return true
        }

        for (i in 0 until nodeInfo.childCount) {
            if (isShortsUI(nodeInfo.getChild(i))) {
                return true
            }
        }

        return false
    }

    override fun onInterrupt() {}
}
