package com.jarvis.assistant.assistant.voice

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.openai.client.OpenAIClient
import com.openai.client.okhttp.OpenAIOkHttpClient
import com.openai.core.JsonValue
import com.openai.models.responses.FunctionTool
import com.openai.models.responses.ResponseCreateParams
import com.openai.models.responses.ResponseInputItem
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
        availableTools: List<com.jarvis.assistant.agent.tools.ToolDefinition>,
        previousResponseId: String?
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
        previousResponseId?.let(builder::previousResponseId)

        availableTools.forEach { builder.addTool(it.toFunctionTool()) }

        emitResponse(client.responses().create(builder.build()))
    }.catch { throwable -> emit(GatewayEvent.Error(throwable)) }
        .flowOn(Dispatchers.IO)

    override suspend fun continueCompletion(
        previousResponseId: String,
        toolOutputs: List<GatewayToolOutput>,
        availableTools: List<com.jarvis.assistant.agent.tools.ToolDefinition>
    ): Flow<GatewayEvent> = flow {
        require(toolOutputs.isNotEmpty()) { "Debe existir al menos un resultado de herramienta" }
        val input = toolOutputs.map { result ->
            ResponseInputItem.ofFunctionCallOutput(
                ResponseInputItem.FunctionCallOutput.builder()
                    .callId(result.callId)
                    .output(result.output)
                    .build()
            )
        }
        val builder = ResponseCreateParams.builder()
            .model(model)
            .instructions(SYSTEM_INSTRUCTIONS)
            .previousResponseId(previousResponseId)
            .inputOfResponse(input)
            .maxOutputTokens(2_048)
        availableTools.forEach { builder.addTool(it.toFunctionTool()) }
        emitResponse(client.responses().create(builder.build()))
    }.catch { throwable -> emit(GatewayEvent.Error(throwable)) }
        .flowOn(Dispatchers.IO)

    private suspend fun kotlinx.coroutines.flow.FlowCollector<GatewayEvent>.emitResponse(
        response: com.openai.models.responses.Response
    ) {
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
        emit(
            GatewayEvent.Completed(
                responseId = response.id(),
                finishReason = if (emittedText) "completed" else "tool_call"
            )
        )
    }

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
            ocurrió. Prefiere la operación de menor privilegio. Una orden directa del usuario ya
            autoriza sus pasos rutinarios: ejecútala sin pedir confirmación adicional. Conserva los
            datos dados en turnos anteriores; si el usuario completa un dato faltante, combínalo con
            el objetivo pendiente en vez de volver a preguntarlo. Haz una sola pregunta breve solo
            cuando falte un dato imprescindible. Para operar interfaces, inspecciona la pantalla,
            usa los IDs visibles y verifica el resultado. Si recibes una observación de pantalla,
            razona únicamente sobre elementos presentes. Responde en el idioma del usuario y de
            forma concisa, ideal para ser leída mediante TTS.
        """
    }
}
