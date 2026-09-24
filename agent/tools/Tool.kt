package com.jarvis.assistant.agent.tools

/**
 * Interfaz base para cualquier herramienta ejecutable por el asistente.
 *
 * Las herramientas representan acciones semánticas y controladas (ej. open_app,
 * set_volume, go_home, force_stop_app, set_brightness, send_message).
 */
interface Tool {
    /** Definición descriptiva y metadatos de seguridad de la herramienta */
    val definition: ToolDefinition

    /**
     * Ejecuta la herramienta con los parámetros semánticos validados.
     *
     * @param arguments Mapa clave-valor de argumentos deserializados de la llamada del LLM.
     * @return Resultado de la ejecución con reporte de éxito o error.
     */
    suspend fun execute(arguments: Map<String, Any?>): ToolResult
}
