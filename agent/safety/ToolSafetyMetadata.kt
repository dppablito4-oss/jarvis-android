package com.jarvis.assistant.agent.safety

import com.jarvis.assistant.core.model.DeviceCapability

/**
 * Metadatos de seguridad obligatorios que toda herramienta debe declarar.
 *
 * El agente NO puede ejecutar operaciones arbitrarias ni herramientas de texto libre
 * como shell("..."). Toda acción debe estar semánticamente modelada con estos metadatos.
 */
data class ToolSafetyMetadata(
    /** Nivel de riesgo operacional evaluado para la herramienta */
    val riskLevel: RiskLevel,

    /** Indica si requiere confirmación explícita del usuario antes del despacho */
    val requiresConfirmation: Boolean,

    /** Capacidad requerida del dispositivo para poder ejecutar esta herramienta */
    val requiredCapability: DeviceCapability,

    /** Permiso de Android específico requerido (si aplica, ej. android.permission.CAMERA) */
    val requiredPermission: String? = null
)
