package com.jarvis.assistant.device.api

/**
 * Representación inmutable y desacoplada de un nodo o elemento interactivo de la interfaz.
 *
 * Aísla al resto del sistema de las clases directas de Android (AccessibilityNodeInfo).
 */
data class ScreenNode(
    val id: Int,
    val text: String?,
    val contentDescription: String?,
    val className: String?,
    val packageName: String?,
    val isClickable: Boolean,
    val isEditable: Boolean,
    val isPassword: Boolean = false,
    val isScrollable: Boolean,
    val boundsInScreen: RectBounds,
    val children: List<ScreenNode> = emptyList()
)

data class RectBounds(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int
) {
    val centerX: Int get() = (left + right) / 2
    val centerY: Int get() = (top + bottom) / 2
}

enum class ScrollDirection {
    UP,
    DOWN,
    LEFT,
    RIGHT
}
