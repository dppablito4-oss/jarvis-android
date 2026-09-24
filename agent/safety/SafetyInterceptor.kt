package com.jarvis.assistant.agent.safety

/**
 * Interceptor de seguridad que valida los metadatos de una herramienta
 * y gestiona el flujo de confirmación con el usuario antes de proceder a la ejecución.
 */
interface SafetyInterceptor {
    /**
     * Evalúa si una acción puede ejecutarse de forma autónoma o debe pausarse
     * a la espera del consentimiento explícito del usuario.
     */
    suspend fun evaluateAction(
        toolName: String,
        parameters: Map<String, Any?>,
        safetyMetadata: ToolSafetyMetadata
    ): SafetyEvaluationResult
}

sealed class SafetyEvaluationResult {
    /** Autorizado para ejecución inmediata */
    data object Approved : SafetyEvaluationResult()

    /** Requiere confirmación del usuario mediante UI / diálogo */
    data class RequiresConfirmation(
        val reason: String,
        val riskLevel: RiskLevel
    ) : SafetyEvaluationResult()

    /** Rechazado por política de seguridad (ej. capacidad no concedida) */
    data class Denied(
        val reason: String
    ) : SafetyEvaluationResult()
}
