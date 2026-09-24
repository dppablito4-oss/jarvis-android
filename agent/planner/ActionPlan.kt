package com.jarvis.assistant.agent.planner

/**
 * Plan de acción multi-paso descompuesto por el razonador del agente
 * para cumplir una meta compleja del usuario.
 */
data class ActionPlan(
    val planId: String,
    val userGoal: String,
    val steps: List<ActionStep>,
    val createdAtTimestamp: Long = System.currentTimeMillis()
)

/**
 * Paso individual dentro de un ActionPlan.
 */
data class ActionStep(
    val stepIndex: Int,
    val toolName: String,
    val arguments: Map<String, Any?>,
    val description: String,
    val requiresVerification: Boolean = false
)
