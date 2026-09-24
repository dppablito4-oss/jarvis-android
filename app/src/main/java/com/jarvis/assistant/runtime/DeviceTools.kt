package com.jarvis.assistant.runtime

import com.jarvis.assistant.agent.safety.RiskLevel
import com.jarvis.assistant.agent.safety.ToolSafetyMetadata
import com.jarvis.assistant.agent.tools.Tool
import com.jarvis.assistant.agent.tools.ToolDefinition
import com.jarvis.assistant.agent.tools.ToolResult
import com.jarvis.assistant.core.model.DeviceCapability
import com.jarvis.assistant.device.api.DeviceAutomation

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
                val label = node.text ?: node.contentDescription ?: node.className.orEmpty()
                "[${node.id}] $label${if (node.isClickable) " (click)" else ""}${if (node.isEditable) " (input)" else ""}"
            }
        return ToolResult(definition.name, true, "Pantalla observada", data = mapOf("screen" to flattened))
    }

    private fun flatten(node: com.jarvis.assistant.device.api.ScreenNode, target: MutableList<com.jarvis.assistant.device.api.ScreenNode>) {
        target += node
        node.children.forEach { flatten(it, target) }
    }
}
