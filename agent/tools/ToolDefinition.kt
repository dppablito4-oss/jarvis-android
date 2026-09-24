package com.jarvis.assistant.agent.tools

import com.jarvis.assistant.agent.safety.ToolSafetyMetadata

/**
 * Esquema descriptivo de una herramienta disponible para el agente o LLM.
 */
data class ToolDefinition(
    val name: String,
    val description: String,
    val parametersSchemaJson: String,
    val safetyMetadata: ToolSafetyMetadata
)

/**
 * Resultado estructurado tras la ejecución de una herramienta.
 */
data class ToolResult(
    val toolName: String,
    val isSuccess: Boolean,
    val outputMessage: String,
    val data: Map<String, Any?> = emptyMap(),
    val errorMessage: String? = null
)
