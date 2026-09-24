package com.jarvis.assistant.agent.tools

import com.jarvis.assistant.agent.safety.SafetyEvaluationResult
import com.jarvis.assistant.agent.safety.SafetyInterceptor

fun interface ConfirmationHandler {
    suspend fun confirm(tool: ToolDefinition, arguments: Map<String, Any?>, reason: String): Boolean
}

/**
 * Implementación única de despacho. Ninguna herramienta registrada se ejecuta sin evaluación
 * previa de capacidad, riesgo y confirmación.
 */
class SecureToolRouter(
    private val safetyInterceptor: SafetyInterceptor,
    private val confirmationHandler: ConfirmationHandler
) : ToolRouter {
    private val tools = linkedMapOf<String, Tool>()

    override fun registerTool(tool: Tool) {
        require(tool.definition.name.isNotBlank()) { "El nombre de la herramienta es obligatorio" }
        check(tools.putIfAbsent(tool.definition.name, tool) == null) {
            "La herramienta ${tool.definition.name} ya está registrada"
        }
    }

    override fun getToolDefinitions(): List<ToolDefinition> = tools.values.map(Tool::definition)

    override suspend fun route(toolName: String, arguments: Map<String, Any?>): ToolResult {
        val tool = tools[toolName] ?: return ToolResult(
            toolName = toolName,
            isSuccess = false,
            outputMessage = "Herramienta desconocida",
            errorMessage = "No existe una herramienta registrada con el nombre $toolName"
        )

        return when (
            val evaluation = safetyInterceptor.evaluateAction(
                toolName = toolName,
                parameters = arguments,
                safetyMetadata = tool.definition.safetyMetadata
            )
        ) {
            SafetyEvaluationResult.Approved -> tool.execute(arguments)
            is SafetyEvaluationResult.Denied -> ToolResult(
                toolName = toolName,
                isSuccess = false,
                outputMessage = "Acción bloqueada",
                errorMessage = evaluation.reason
            )
            is SafetyEvaluationResult.RequiresConfirmation -> {
                if (confirmationHandler.confirm(tool.definition, arguments, evaluation.reason)) {
                    tool.execute(arguments)
                } else {
                    ToolResult(
                        toolName = toolName,
                        isSuccess = false,
                        outputMessage = "Acción cancelada por el usuario"
                    )
                }
            }
        }
    }
}
