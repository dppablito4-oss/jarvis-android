package com.jarvis.assistant.automation.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.view.accessibility.AccessibilityNodeInfo
import com.jarvis.assistant.device.api.DeviceAutomation
import com.jarvis.assistant.device.api.ScreenNode
import com.jarvis.assistant.device.api.ScrollDirection

class AndroidDeviceAutomation(
    private val context: Context
) : DeviceAutomation {
    private val service get() = JarvisAccessibilityService.current()

    override suspend fun isAvailable(): Boolean = JarvisAccessibilityService.isConnected

    override suspend fun home(): Boolean =
        service?.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME) == true

    override suspend fun back(): Boolean =
        service?.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK) == true

    override suspend fun recentApps(): Boolean =
        service?.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS) == true

    override suspend fun openApp(packageName: String): Boolean {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName) ?: return false
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        return true
    }

    override suspend fun inspectScreen(): ScreenNode? = service?.inspectScreen()

    override suspend fun click(nodeId: Int): Boolean = service?.click(nodeId) == true

    override suspend fun clickCoordinates(x: Int, y: Int): Boolean =
        service?.clickCoordinates(x, y) == true

    override suspend fun typeText(nodeId: Int, text: String): Boolean =
        service?.typeText(nodeId, text) == true

    override suspend fun scroll(direction: ScrollDirection): Boolean {
        val action = when (direction) {
            ScrollDirection.UP, ScrollDirection.LEFT -> AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
            ScrollDirection.DOWN, ScrollDirection.RIGHT -> AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
        }
        return service?.scroll(action) == true
    }
}
