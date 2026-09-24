package com.jarvis.assistant.automation.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.jarvis.assistant.device.api.RectBounds
import com.jarvis.assistant.device.api.ScreenNode
import java.util.concurrent.atomic.AtomicInteger

class JarvisAccessibilityService : AccessibilityService() {
    private val indexedNodes = linkedMapOf<Int, AccessibilityNodeInfo>()

    override fun onServiceConnected() {
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        if (instance === this) instance = null
        indexedNodes.clear()
        super.onDestroy()
    }

    fun inspectScreen(): ScreenNode? {
        indexedNodes.clear()
        val counter = AtomicInteger(1)
        return rootInActiveWindow?.let { mapNode(it, counter, depth = 0) }
    }

    fun click(nodeId: Int): Boolean {
        var node = indexedNodes[nodeId] ?: return false
        while (!node.isClickable && node.parent != null) node = node.parent
        return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }

    fun typeText(nodeId: Int, text: String): Boolean {
        val node = indexedNodes[nodeId] ?: return false
        val arguments = Bundle().apply {
            putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
        }
        return node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
    }

    fun scroll(action: Int): Boolean {
        val scrollable = indexedNodes.values.firstOrNull { it.isScrollable } ?: return false
        return scrollable.performAction(action)
    }

    fun clickCoordinates(x: Int, y: Int): Boolean {
        val path = Path().apply { moveTo(x.toFloat(), y.toFloat()) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 60))
            .build()
        return dispatchGesture(gesture, null, null)
    }

    private fun mapNode(node: AccessibilityNodeInfo, counter: AtomicInteger, depth: Int): ScreenNode {
        val id = counter.getAndIncrement()
        indexedNodes[id] = node
        val bounds = android.graphics.Rect().also(node::getBoundsInScreen)
        val children = if (depth < MAX_DEPTH) {
            buildList {
                for (index in 0 until node.childCount) {
                    node.getChild(index)?.let { add(mapNode(it, counter, depth + 1)) }
                }
            }
        } else {
            emptyList()
        }

        return ScreenNode(
            id = id,
            text = node.text?.toString(),
            contentDescription = node.contentDescription?.toString(),
            className = node.className?.toString(),
            packageName = node.packageName?.toString(),
            isClickable = node.isClickable,
            isEditable = node.isEditable,
            isScrollable = node.isScrollable,
            boundsInScreen = RectBounds(bounds.left, bounds.top, bounds.right, bounds.bottom),
            children = children
        )
    }

    companion object {
        private const val MAX_DEPTH = 30

        @Volatile
        private var instance: JarvisAccessibilityService? = null

        val isConnected: Boolean get() = instance != null

        fun current(): JarvisAccessibilityService? = instance
    }
}
