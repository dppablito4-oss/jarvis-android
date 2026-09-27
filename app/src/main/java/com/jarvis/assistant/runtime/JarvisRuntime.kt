package com.jarvis.assistant.runtime

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.jarvis.assistant.agent.safety.DefaultSafetyInterceptor
import com.jarvis.assistant.agent.tools.ConfirmationHandler
import com.jarvis.assistant.agent.tools.SecureToolRouter
import com.jarvis.assistant.agent.tools.ToolResult
import com.jarvis.assistant.assistant.voice.AssistantGateway
import com.jarvis.assistant.assistant.voice.GatewayEvent
import com.jarvis.assistant.assistant.voice.GatewayToolOutput
import com.jarvis.assistant.automation.accessibility.AndroidDeviceAutomation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray
import org.json.JSONObject
import java.text.Normalizer

data class RuntimeState(
    val isBusy: Boolean = false,
    val lastRequest: String = "",
    val lastMessage: String = "Listo",
    val lastObservation: String = "",
    val lastSuccess: Boolean? = null
)

/** Runtime del agente: serializa órdenes y enruta acciones locales o del proveedor cloud. */
class JarvisRuntime(
    context: Context,
    private val assistantGateway: AssistantGateway? = null
) {
    private val appContext = context.applicationContext
    val capabilityProvider = AndroidCapabilityProvider(appContext)
    private val automation = AndroidDeviceAutomation(appContext)
    private val router = SecureToolRouter(
        safetyInterceptor = DefaultSafetyInterceptor(capabilityProvider),
        confirmationHandler = ConfirmationHandler { _, _, _ -> false }
    )
    private val planner = LocalCommandPlanner(appContext)
    private val mutableState = MutableStateFlow(RuntimeState())
    private val executionMutex = Mutex()
    private var previousConversationResponseId: String? = null
    private var uiAutomationAuthorized = false

    val state: StateFlow<RuntimeState> = mutableState.asStateFlow()
    val toolDefinitions get() = router.getToolDefinitions()
    val hasAssistantGateway: Boolean get() = assistantGateway != null

    init {
        router.registerTool(OpenAppTool(automation))
        router.registerTool(GlobalNavigationTool(automation, GlobalNavigationTool.Action.HOME))
        router.registerTool(GlobalNavigationTool(automation, GlobalNavigationTool.Action.BACK))
        router.registerTool(GlobalNavigationTool(automation, GlobalNavigationTool.Action.RECENTS))
        router.registerTool(InspectScreenTool(automation))
        router.registerTool(ClickNodeTool(automation))
        router.registerTool(TypeTextTool(automation))
        router.registerTool(ScrollScreenTool(automation))
    }

    suspend fun execute(request: String): ToolResult = executionMutex.withLock {
        if (request.isBlank()) {
            return@withLock ToolResult("runtime", false, "La instrucción está vacía")
        }
        mutableState.value = mutableState.value.copy(isBusy = true, lastRequest = request)
        val result = try {
            val call = planner.plan(request)
            when {
                call != null -> router.route(call.toolName, call.arguments)
                assistantGateway != null -> executeWithAssistant(request)
                else -> ToolResult(
                    toolName = "assistant_gateway",
                    isSuccess = false,
                    outputMessage = "El proveedor cloud no está configurado",
                    errorMessage = "Configura la clave desde Motor IA en la aplicación"
                )
            }
        } catch (error: Throwable) {
            ToolResult(
                toolName = "runtime",
                isSuccess = false,
                outputMessage = "No se pudo completar la instrucción",
                errorMessage = error.message ?: error::class.java.simpleName
            )
        }
        mutableState.value = RuntimeState(
            isBusy = false,
            lastRequest = request,
            lastMessage = result.errorMessage ?: result.outputMessage,
            lastObservation = result.data["screen"] as? String ?: "",
            lastSuccess = result.isSuccess
        )
        result
    }

    private suspend fun executeWithAssistant(request: String): ToolResult {
        val gateway = assistantGateway ?: return ToolResult(
            toolName = "assistant_gateway",
            isSuccess = false,
            outputMessage = "Proveedor no configurado"
        )
        if (hasExplicitScreenConsent(request) || hasUiAutomationIntent(request)) {
            uiAutomationAuthorized = true
        }
        // La percepción se habilita únicamente tras una orden explícita de pantalla o automatización.
        val availableTools = router.getToolDefinitions().filterNot { definition ->
            definition.name in UI_AUTOMATION_TOOLS && !uiAutomationAuthorized
        }
        var events = gateway.streamCompletion(
            userPrompt = request,
            conversationHistory = emptyList(),
            availableTools = availableTools,
            previousResponseId = previousConversationResponseId
        )
        var lastToolResult: ToolResult? = null

        repeat(MAX_TOOL_ROUNDS) { roundIndex ->
            val text = StringBuilder()
            val calls = mutableListOf<GatewayEvent.ToolCallRequested>()
            var responseId: String? = null
            var gatewayError: Throwable? = null
            events.collect { event ->
                when (event) {
                    is GatewayEvent.ContentDelta -> text.append(event.text)
                    is GatewayEvent.ToolCallRequested -> calls += event
                    is GatewayEvent.Error -> gatewayError = event.throwable
                    is GatewayEvent.Completed -> responseId = event.responseId
                }
            }
            gatewayError?.let { error ->
                return ToolResult(
                    toolName = "openai_responses",
                    isSuccess = false,
                    outputMessage = "El proveedor no pudo completar la solicitud",
                    errorMessage = error.message ?: error::class.java.simpleName
                )
            }
            if (calls.isEmpty()) {
                previousConversationResponseId = responseId ?: previousConversationResponseId
                return ToolResult(
                    toolName = "openai_responses",
                    isSuccess = text.isNotBlank(),
                    outputMessage = text.toString().ifBlank { "El modelo no devolvió contenido" },
                    data = lastToolResult?.data.orEmpty()
                )
            }
            if (roundIndex == MAX_TOOL_ROUNDS - 1) {
                return ToolResult(
                    toolName = "openai_responses",
                    isSuccess = false,
                    outputMessage = "Se alcanzó el límite de acciones encadenadas",
                    errorMessage = "El modelo solicitó más de $MAX_TOOL_ROUNDS rondas de herramientas"
                )
            }
            val completedResponseId = responseId ?: return ToolResult(
                toolName = "openai_responses",
                isSuccess = false,
                outputMessage = "Respuesta incompleta del proveedor",
                errorMessage = "Falta responseId para continuar las herramientas"
            )
            val outputs = calls.map { call ->
                val result = runCatching {
                    router.route(call.toolName, jsonObjectToMap(JSONObject(call.argumentsJson)))
                }.getOrElse { error ->
                    ToolResult(call.toolName, false, "La herramienta falló", errorMessage = error.message)
                }
                lastToolResult = result
                GatewayToolOutput(call.callId, result.toGatewayJson())
            }
            events = gateway.continueCompletion(completedResponseId, outputs, availableTools)
        }
        return ToolResult(
            toolName = "openai_responses",
            isSuccess = false,
            outputMessage = "No se pudo completar el ciclo de herramientas"
        )
    }

    private fun ToolResult.toGatewayJson(): String = JSONObject().apply {
        put("success", isSuccess)
        put("message", outputMessage)
        errorMessage?.let { put("error", it) }
        if (data.isNotEmpty()) put("data", JSONObject(data))
    }.toString()

    private fun hasExplicitScreenConsent(request: String): Boolean {
        val normalized = normalizeText(request)
        return SCREEN_CONSENT_PHRASES.any(normalized::contains)
    }

    private fun hasUiAutomationIntent(request: String): Boolean {
        val normalized = normalizeText(request)
        return UI_AUTOMATION_PHRASES.any(normalized::contains)
    }

    fun resetConversation() {
        previousConversationResponseId = null
        uiAutomationAuthorized = false
    }

    private fun jsonObjectToMap(json: JSONObject): Map<String, Any?> = buildMap {
        json.keys().forEach { key -> put(key, jsonValue(json.get(key))) }
    }

    private fun jsonValue(value: Any?): Any? = when (value) {
        JSONObject.NULL -> null
        is JSONObject -> jsonObjectToMap(value)
        is JSONArray -> List(value.length()) { index -> jsonValue(value.get(index)) }
        else -> value
    }

    private fun normalizeText(value: String): String = Normalizer.normalize(
        value.trim().lowercase(),
        Normalizer.Form.NFD
    ).replace("\\p{Mn}+".toRegex(), "")

    private companion object {
        const val MAX_TOOL_ROUNDS = 12
        val SCREEN_CONSENT_PHRASES = listOf(
            "pantalla", "mira esto", "observa esto", "interfaz visible", "lo que ves"
        )
        val UI_AUTOMATION_TOOLS = setOf("inspect_screen", "click_node", "type_text", "scroll_screen")
        val UI_AUTOMATION_PHRASES = listOf(
            "envia", "enviar", "mensaje", "whatsapp", "contacto", "escribele",
            "respondele", "publica", "comparte", "pulsa", "toca", "selecciona"
        )
    }
}

private data class PlannedToolCall(
    val toolName: String,
    val arguments: Map<String, Any?> = emptyMap()
)

/** Planificador determinista para mantener operaciones básicas disponibles sin red. */
private class LocalCommandPlanner(private val context: Context) {
    fun plan(rawRequest: String): PlannedToolCall? {
        val request = normalize(rawRequest)
        return when {
            request in setOf("inicio", "ve al inicio", "ir al inicio", "home") -> PlannedToolCall("go_home")
            request in setOf("atras", "volver", "regresa", "ve atras") -> PlannedToolCall("go_back")
            request.contains("recientes") -> PlannedToolCall("recent_apps")
            request in setOf("observa", "inspecciona", "inspecciona pantalla", "que hay en pantalla") ->
                PlannedToolCall("inspect_screen")
            request.startsWith("abre ") || request.startsWith("abrir ") -> {
                val label = request.substringAfter(' ').trim()
                resolvePackage(label)?.let { PlannedToolCall("open_app", mapOf("packageName" to it)) }
            }
            else -> null
        }
    }

    private fun resolvePackage(requestedLabel: String): String? {
        if (requestedLabel.contains('.')) {
            context.packageManager.getLaunchIntentForPackage(requestedLabel)?.let { return requestedLabel }
        }
        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val activities = if (android.os.Build.VERSION.SDK_INT >= 33) {
            context.packageManager.queryIntentActivities(
                launcherIntent,
                PackageManager.ResolveInfoFlags.of(0)
            )
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.queryIntentActivities(launcherIntent, 0)
        }
        return activities
            .map { it to normalize(it.loadLabel(context.packageManager).toString()) }
            .sortedBy { (_, label) -> kotlin.math.abs(label.length - requestedLabel.length) }
            .firstOrNull { (_, label) -> label == requestedLabel || label.contains(requestedLabel) }
            ?.first?.activityInfo?.packageName
    }

    private fun normalize(value: String): String = Normalizer.normalize(
        value.trim().lowercase(),
        Normalizer.Form.NFD
    ).replace("\\p{Mn}+".toRegex(), "")
}
