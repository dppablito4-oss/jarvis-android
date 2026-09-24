package com.jarvis.assistant.agent.safety

import com.jarvis.assistant.core.model.CapabilityProvider

/** Política base: niega capacidades ausentes y detiene acciones que requieren consentimiento. */
class DefaultSafetyInterceptor(
    private val capabilityProvider: CapabilityProvider
) : SafetyInterceptor {
    override suspend fun evaluateAction(
        toolName: String,
        parameters: Map<String, Any?>,
        safetyMetadata: ToolSafetyMetadata
    ): SafetyEvaluationResult {
        if (!capabilityProvider.hasCapability(safetyMetadata.requiredCapability)) {
            return SafetyEvaluationResult.Denied(
                "La capacidad ${safetyMetadata.requiredCapability} no está disponible"
            )
        }

        return if (safetyMetadata.requiresConfirmation) {
            SafetyEvaluationResult.RequiresConfirmation(
                reason = "La acción $toolName requiere aprobación explícita",
                riskLevel = safetyMetadata.riskLevel
            )
        } else {
            SafetyEvaluationResult.Approved
        }
    }
}
