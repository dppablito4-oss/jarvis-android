package com.jarvis.assistant.agent.safety

/**
 * Nivel de riesgo operacional asociado a cada herramienta o acción.
 */
enum class RiskLevel {
    /** Acciones de solo lectura o sin impacto observable (ej. obtener hora, leer batería) */
    LOW,

    /** Acciones que alteran estado visual o ajustes menores reversibles (ej. abrir app, subir volumen) */
    MEDIUM,

    /** Acciones que modifican datos del usuario o realizan envíos externos (ej. enviar mensaje, eliminar recordatorio) */
    HIGH,

    /** Acciones potencialmente destructivas o de configuración de sistema (ej. detener forzadamente app, desinstalar, root) */
    CRITICAL
}
