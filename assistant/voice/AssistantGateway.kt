package com.jarvis.assistant.assistant.voice

import com.jarvis.assistant.agent.tools.ToolDefinition
import kotlinx.coroutines.flow.Flow

/**
 * Contrato agnóstico para proveedores de Inteligencia Artificial (LLM / VLM / Audio).
 *
 * Desacopla completamente el núcleo de Jarvis de cualquier proveedor específico,
 * permitiendo conectar OpenAI en la nube, modelos locales (ONNX/GGUF) o alternativas
 * (Gemini, Claude) sin modificar la lógica del agente.
 */
interface AssistantGateway {
    /** Identificador del proveedor actual (ej. "openai", "local-onnx", "gemini") */
    val providerId: String

    /**
     * Envía una consulta conversacional al modelo con la lista de herramientas disponibles
     * y retorna un flujo de respuestas (tokens de texto o llamadas a herramientas).
     */
    suspend fun streamCompletion(
        userPrompt: String,
        conversationHistory: List<GatewayMessage>,
        availableTools: List<ToolDefinition>,
        previousResponseId: String? = null
    ): Flow<GatewayEvent>

    /** Continúa una respuesta después de ejecutar todas las herramientas solicitadas. */
    suspend fun continueCompletion(
        previousResponseId: String,
        toolOutputs: List<GatewayToolOutput>,
        availableTools: List<ToolDefinition>
    ): Flow<GatewayEvent>
}

sealed class GatewayEvent {
    /** Fragmento de texto generado en streaming */
    data class ContentDelta(val text: String) : GatewayEvent()

    /** Petición del modelo para ejecutar una o más herramientas estructuradas */
    data class ToolCallRequested(
        val callId: String,
        val toolName: String,
        val argumentsJson: String
    ) : GatewayEvent()

    /** Finalización de la generación */
    data class Completed(
        val responseId: String,
        val finishReason: String
    ) : GatewayEvent()

    /** Error producido durante la comunicación con el proveedor */
    data class Error(val throwable: Throwable) : GatewayEvent()
}

data class GatewayToolOutput(
    val callId: String,
    val output: String
)

data class GatewayMessage(
    val role: MessageRole,
    val content: String,
    val toolCallId: String? = null
)

enum class MessageRole {
    SYSTEM,
    USER,
    ASSISTANT,
    TOOL
}
