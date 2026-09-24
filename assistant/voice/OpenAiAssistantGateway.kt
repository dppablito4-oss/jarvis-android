package com.jarvis.assistant.assistant.voice

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.openai.client.OpenAIClient
import com.openai.client.okhttp.OpenAIOkHttpClient
import com.openai.core.JsonValue
import com.openai.models.responses.FunctionTool
import com.openai.models.responses.ResponseCreateParams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/** Implementación del proveedor mediante Responses API y el SDK oficial openai-java. */
class OpenAiAssistantGateway(
    apiKey: String,
    private val model: String,
    private val client: OpenAIClient = OpenAIOkHttpClient.builder().apiKey(apiKey).build()
) : AssistantGateway {
    init {
        require(apiKey.isNotBlank()) { "La API key de OpenAI no está configurada" }
        require(model.isNotBlank()) { "El modelo de OpenAI no está configurado" }
    }

    override val providerId: String = "openai-responses"
    private val mapper = ObjectMapper()

    override suspend fun streamCompletion(
        userPrompt: String,
        conversationHistory: List<GatewayMessage>,
        availableTools: List<com.jarvis.assistant.agent.tools.ToolDefinition>
    ): Flow<GatewayEvent> = flow {
        val contextualInput = buildString {
            conversationHistory.takeLast(12).forEach { message ->
                append(message.role.name.lowercase()).append(": ").appendLine(message.content)
            }
            append("user: ").append(userPrompt)
        }
        val builder = ResponseCreateParams.builder()
            .model(model)
            .instructions(SYSTEM_INSTRUCTIONS)
            .input(contextualInput)
            .maxOutputTokens(2_048)

        availableTools.forEach { builder.addTool(it.toFunctionTool()) }

        val response = client.responses().create(builder.build())
        var emittedText = false
        response.output().forEach { item ->
            if (item.isFunctionCall()) {
                val call = item.asFunctionCall()
                emit(
                    GatewayEvent.ToolCallRequested(
                        callId = call.callId(),
                        toolName = call.name(),
                        argumentsJson = call.arguments()
                    )
                )
            }
            val message = item.message().orElse(null)
            message?.content()?.forEach { content ->
                val output = content.outputText().orElse(null)
                if (output != null) {
                    emittedText = true
                    emit(GatewayEvent.ContentDelta(output.text()))
                }
            }
        }
        emit(GatewayEvent.Completed(if (emittedText) "completed" else "tool_call"))
    }.catch { throwable -> emit(GatewayEvent.Error(throwable)) }
        .flowOn(Dispatchers.IO)

    private fun com.jarvis.assistant.agent.tools.ToolDefinition.toFunctionTool(): FunctionTool {
        val schema: Map<String, Any?> = mapper.readValue(
            parametersSchemaJson,
            object : TypeReference<Map<String, Any?>>() {}
        )
        val parameters = FunctionTool.Parameters.builder().apply {
            schema.forEach { (key, value) ->
                putAdditionalProperty(key, JsonValue.from(value))
            }
        }.build()
        return FunctionTool.builder()
            .name(name)
            .description(description)
            .parameters(parameters)
            .strict(false)
            .build()
    }

    companion object {
        private const val SYSTEM_INSTRUCTIONS = """
            Eres Jarvis, un agente Android orientado a completar objetivos verificables.
            Usa herramientas cuando una acción física sea necesaria. Nunca inventes que una acción
            ocurrió. Prefiere la operación de menor privilegio y solicita confirmación para acciones
            sensibles. Si recibes una observación de pantalla, razona únicamente sobre elementos
            presentes. Responde en el idioma del usuario y de forma concisa.
        """
    }
}
