package com.jarvis.assistant.runtime

import com.jarvis.assistant.device.api.DeviceAutomation
import com.jarvis.assistant.device.api.RectBounds
import com.jarvis.assistant.device.api.ScreenNode
import com.jarvis.assistant.device.api.ScrollDirection
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InspectScreenToolTest {
    @Test
    fun `redacts editable password email and long numbers`() = runBlocking {
        val root = node(
            text = "contacto usuario@example.com cuenta 4111 1111 1111 1111",
            children = listOf(
                node(text = "secreto", editable = true),
                node(text = "clave-real", password = true)
            )
        )
        val output = InspectScreenTool(FakeAutomation(root)).execute(emptyMap())
            .data["screen"] as String

        assertFalse(output.contains("usuario@example.com"))
        assertFalse(output.contains("4111 1111 1111 1111"))
        assertFalse(output.contains("secreto"))
        assertFalse(output.contains("clave-real"))
        assertTrue(output.contains("[correo oculto]"))
        assertTrue(output.contains("[campo editable oculto]"))
        assertTrue(output.contains("[campo de contraseña oculto]"))
    }

    private fun node(
        text: String,
        editable: Boolean = false,
        password: Boolean = false,
        children: List<ScreenNode> = emptyList()
    ) = ScreenNode(
        id = text.hashCode(),
        text = text,
        contentDescription = null,
        className = "TextView",
        packageName = "test",
        isClickable = false,
        isEditable = editable,
        isPassword = password,
        isScrollable = false,
        boundsInScreen = RectBounds(0, 0, 1, 1),
        children = children
    )
}

private class FakeAutomation(private val root: ScreenNode) : DeviceAutomation {
    override suspend fun isAvailable() = true
    override suspend fun home() = true
    override suspend fun back() = true
    override suspend fun recentApps() = true
    override suspend fun openApp(packageName: String) = true
    override suspend fun inspectScreen() = root
    override suspend fun click(nodeId: Int) = true
    override suspend fun clickCoordinates(x: Int, y: Int) = true
    override suspend fun typeText(nodeId: Int, text: String) = true
    override suspend fun scroll(direction: ScrollDirection) = true
}
