package com.jarvis.assistant.runtime

import com.jarvis.assistant.agent.safety.RiskLevel
import com.jarvis.assistant.agent.safety.ToolSafetyMetadata
import com.jarvis.assistant.agent.tools.Tool
import com.jarvis.assistant.agent.tools.ToolDefinition
import com.jarvis.assistant.agent.tools.ToolResult
import com.jarvis.assistant.core.model.DeviceCapability
import com.jarvis.assistant.device.api.DeviceAutomation
import com.jarvis.assistant.device.api.ScrollDirection

class OpenAppTool(private val automation: DeviceAutomation) : Tool {
    override val definition = ToolDefinition(
        name = "open_app",
        description = "Abre una aplicación instalada mediante su nombre de paquete",
        parametersSchemaJson = """{"type":"object","properties":{"packageName":{"type":"string"}},"required":["packageName"]}""",
        safetyMetadata = ToolSafetyMetadata(
            riskLevel = RiskLevel.MEDIUM,
            requiresConfirmation = false,
            requiredCapability = DeviceCapability.NORMAL_ANDROID
        )
    )

    override suspend fun execute(arguments: Map<String, Any?>): ToolResult {
        val packageName = arguments["packageName"] as? String
            ?: return failure("Falta packageName")
        val success = automation.openApp(packageName)
        return ToolResult(definition.name, success, if (success) "Aplicación abierta" else "No se pudo abrir", errorMessage = if (success) null else "Paquete no disponible")
    }

    private fun failure(message: String) = ToolResult(definition.name, false, "Argumentos inválidos", errorMessage = message)
}

class GlobalNavigationTool(
    private val automation: DeviceAutomation,
    private val action: Action
) : Tool {
    enum class Action(val toolName: String) { HOME("go_home"), BACK("go_back"), RECENTS("recent_apps") }

    override val definition = ToolDefinition(
        name = action.toolName,
        description = "Ejecuta la navegación global ${action.name}",
        parametersSchemaJson = """{"type":"object","properties":{}}""",
        safetyMetadata = ToolSafetyMetadata(
            riskLevel = RiskLevel.MEDIUM,
            requiresConfirmation = false,
            requiredCapability = DeviceCapability.ACCESSIBILITY
        )
    )

    override suspend fun execute(arguments: Map<String, Any?>): ToolResult {
        val success = when (action) {
            Action.HOME -> automation.home()
            Action.BACK -> automation.back()
            Action.RECENTS -> automation.recentApps()
        }
        return ToolResult(definition.name, success, if (success) "Acción ejecutada" else "Acción no disponible")
    }
}

class InspectScreenTool(private val automation: DeviceAutomation) : Tool {
    override val definition = ToolDefinition(
        name = "inspect_screen",
        description = "Inspecciona la interfaz visible y devuelve una representación estructurada",
        parametersSchemaJson = """{"type":"object","properties":{}}""",
        safetyMetadata = ToolSafetyMetadata(
            riskLevel = RiskLevel.LOW,
            requiresConfirmation = false,
            requiredCapability = DeviceCapability.ACCESSIBILITY
        )
    )

    override suspend fun execute(arguments: Map<String, Any?>): ToolResult {
        val root = automation.inspectScreen()
            ?: return ToolResult(definition.name, false, "No se pudo observar la pantalla")
        val flattened = buildList { flatten(root, this) }
            .filter { !it.text.isNullOrBlank() || !it.contentDescription.isNullOrBlank() || it.isClickable || it.isEditable }
            .take(120)
            .joinToString("\n") { node ->
                val label = when {
                    node.isPassword -> "[campo de contraseña oculto]"
                    node.isEditable -> "[campo editable oculto]"
                    else -> sanitizeVisibleText(
                        node.text ?: node.contentDescription ?: node.className.orEmpty()
                    )
                }
                "[${node.id}] $label${if (node.isClickable) " (click)" else ""}${if (node.isEditable) " (input)" else ""}"
            }
        return ToolResult(definition.name, true, "Pantalla observada", data = mapOf("screen" to flattened))
    }

    private fun flatten(node: com.jarvis.assistant.device.api.ScreenNode, target: MutableList<com.jarvis.assistant.device.api.ScreenNode>) {
        target += node
        node.children.forEach { flatten(it, target) }
    }

    private fun sanitizeVisibleText(value: String): String = value
        .replace(EMAIL_PATTERN, "[correo oculto]")
        .replace(LONG_NUMBER_PATTERN, "[número sensible oculto]")

    private companion object {
        val EMAIL_PATTERN = Regex("[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}", RegexOption.IGNORE_CASE)
        val LONG_NUMBER_PATTERN = Regex("(?<!\\d)\\d(?:[ -]?\\d){7,}(?!\\d)")
    }
}

class ClickNodeTool(private val automation: DeviceAutomation) : Tool {
    override val definition = ToolDefinition(
        name = "click_node",
        description = "Pulsa un elemento visible usando el ID obtenido por inspect_screen",
        parametersSchemaJson = """{"type":"object","properties":{"nodeId":{"type":"integer"}},"required":["nodeId"]}""",
        safetyMetadata = ToolSafetyMetadata(
            riskLevel = RiskLevel.MEDIUM,
            requiresConfirmation = false,
            requiredCapability = DeviceCapability.ACCESSIBILITY
        )
    )

    override suspend fun execute(arguments: Map<String, Any?>): ToolResult {
        val nodeId = (arguments["nodeId"] as? Number)?.toInt()
            ?: return ToolResult(definition.name, false, "Falta nodeId")
        val success = automation.click(nodeId)
        return ToolResult(definition.name, success, if (success) "Elemento pulsado" else "No se pudo pulsar el elemento")
    }
}

class TypeTextTool(private val automation: DeviceAutomation) : Tool {
    override val definition = ToolDefinition(
        name = "type_text",
        description = "Escribe texto en un campo editable usando su ID de inspect_screen",
        parametersSchemaJson = """{"type":"object","properties":{"nodeId":{"type":"integer"},"text":{"type":"string"}},"required":["nodeId","text"]}""",
        safetyMetadata = ToolSafetyMetadata(
            riskLevel = RiskLevel.MEDIUM,
            requiresConfirmation = false,
            requiredCapability = DeviceCapability.ACCESSIBILITY
        )
    )

    override suspend fun execute(arguments: Map<String, Any?>): ToolResult {
        val nodeId = (arguments["nodeId"] as? Number)?.toInt()
            ?: return ToolResult(definition.name, false, "Falta nodeId")
        val text = arguments["text"] as? String
            ?: return ToolResult(definition.name, false, "Falta text")
        val success = automation.typeText(nodeId, text)
        return ToolResult(definition.name, success, if (success) "Texto escrito" else "No se pudo escribir el texto")
    }
}

class ScrollScreenTool(private val automation: DeviceAutomation) : Tool {
    override val definition = ToolDefinition(
        name = "scroll_screen",
        description = "Desplaza la interfaz visible en una dirección",
        parametersSchemaJson = """{"type":"object","properties":{"direction":{"type":"string","enum":["up","down","left","right"]}},"required":["direction"]}""",
        safetyMetadata = ToolSafetyMetadata(
            riskLevel = RiskLevel.LOW,
            requiresConfirmation = false,
            requiredCapability = DeviceCapability.ACCESSIBILITY
        )
    )

    override suspend fun execute(arguments: Map<String, Any?>): ToolResult {
        val direction = when ((arguments["direction"] as? String)?.lowercase()) {
            "up" -> ScrollDirection.UP
            "down" -> ScrollDirection.DOWN
            "left" -> ScrollDirection.LEFT
            "right" -> ScrollDirection.RIGHT
            else -> return ToolResult(definition.name, false, "Dirección inválida")
        }
        val success = automation.scroll(direction)
        return ToolResult(definition.name, success, if (success) "Pantalla desplazada" else "No se pudo desplazar")
    }
}
