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
import com.jarvis.assistant.automation.accessibility.AndroidDeviceAutomation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
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

/** Composición inicial del corte vertical local. No requiere proveedor cloud ni secretos. */
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

    val state: StateFlow<RuntimeState> = mutableState.asStateFlow()
    val toolDefinitions get() = router.getToolDefinitions()
    val hasAssistantGateway: Boolean get() = assistantGateway != null

    init {
        router.registerTool(OpenAppTool(automation))
        router.registerTool(GlobalNavigationTool(automation, GlobalNavigationTool.Action.HOME))
        router.registerTool(GlobalNavigationTool(automation, GlobalNavigationTool.Action.BACK))
        router.registerTool(GlobalNavigationTool(automation, GlobalNavigationTool.Action.RECENTS))
        router.registerTool(InspectScreenTool(automation))
    }

    suspend fun execute(request: String): ToolResult {
        mutableState.value = mutableState.value.copy(isBusy = true, lastRequest = request)
        val call = planner.plan(request)
        val result = if (call != null) {
            router.route(call.toolName, call.arguments)
        } else if (assistantGateway != null) {
            executeWithAssistant(request)
        } else {
            ToolResult(
                toolName = "assistant_gateway",
                isSuccess = false,
                outputMessage = "El proveedor cloud no está configurado",
                errorMessage = "Configura JARVIS_OPENAI_API_KEY en local.properties y recompila debug"
            )
        }
        mutableState.value = RuntimeState(
            isBusy = false,
            lastRequest = request,
            lastMessage = result.errorMessage ?: result.outputMessage,
            lastObservation = result.data["screen"] as? String ?: "",
            lastSuccess = result.isSuccess
        )
        return result
    }

    private suspend fun executeWithAssistant(request: String): ToolResult {
        val gateway = assistantGateway ?: return ToolResult(
            toolName = "assistant_gateway",
            isSuccess = false,
            outputMessage = "Proveedor no configurado"
        )
        val observation = if (capabilityProvider.hasCapability(
                com.jarvis.assistant.core.model.DeviceCapability.ACCESSIBILITY
            )
        ) {
            router.route("inspect_screen", emptyMap()).data["screen"] as? String
        } else {
            null
        }
        val prompt = buildString {
            appendLine(request)
            if (!observation.isNullOrBlank()) {
                appendLine()
                appendLine("Observación actual del dispositivo:")
                append(observation)
            }
        }

        val text = StringBuilder()
        var toolResult: ToolResult? = null
        var gatewayError: Throwable? = null
        gateway.streamCompletion(
            userPrompt = prompt,
            conversationHistory = emptyList(),
            availableTools = router.getToolDefinitions()
        ).collect { event ->
            when (event) {
                is GatewayEvent.ContentDelta -> text.append(event.text)
                is GatewayEvent.ToolCallRequested -> {
                    toolResult = router.route(event.toolName, jsonObjectToMap(JSONObject(event.argumentsJson)))
                }
                is GatewayEvent.Error -> gatewayError = event.throwable
                is GatewayEvent.Completed -> Unit
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
        return toolResult ?: ToolResult(
            toolName = "openai_responses",
            isSuccess = text.isNotBlank(),
            outputMessage = text.toString().ifBlank { "El modelo no devolvió contenido" }
        )
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
}

private data class PlannedToolCall(
    val toolName: String,
    val arguments: Map<String, Any?> = emptyMap()
)

/** Planificador determinista de respaldo: mantiene operaciones básicas disponibles sin red. */
private class LocalCommandPlanner(private val context: Context) {
    fun plan(rawRequest: String): PlannedToolCall? {
        val request = normalize(rawRequest)
        return when {
            request in setOf("inicio", "ve al inicio", "ir al inicio", "home") -> PlannedToolCall("go_home")
            request in setOf("atras", "volver", "regresa", "ve atras") -> PlannedToolCall("go_back")
            request.contains("recientes") -> PlannedToolCall("recent_apps")
            request.contains("observa") || request.contains("inspecciona") || request.contains("que hay en pantalla") ->
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
