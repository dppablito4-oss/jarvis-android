package com.jarvis.assistant.agent.tools

/**
 * Enrutador de herramientas del agente.
 *
 * Responsable del registro, descubrimiento y despacho seguro de llamadas
 * a herramientas sin acoplarse directamente a ningún modelo de lenguaje.
 */
interface ToolRouter {
    /**
     * Registra una nueva herramienta en el catálogo disponible.
     */
    fun registerTool(tool: Tool)

    /**
     * Obtiene la lista de definiciones de todas las herramientas registradas
     * para suministrar a la API de herramientas del proveedor de IA.
     */
    fun getToolDefinitions(): List<ToolDefinition>

    /**
     * Busca y despacha la ejecución de una herramienta por su nombre.
     *
     * @param toolName Nombre único de la herramienta (ej. "open_app").
     * @param arguments Argumentos decodificados de la llamada de función.
     */
    suspend fun route(toolName: String, arguments: Map<String, Any?>): ToolResult
}
