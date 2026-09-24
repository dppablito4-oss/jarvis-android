package com.jarvis.assistant.device.api

/**
 * Contrato de automatización de interacción con la pantalla y navegación del dispositivo.
 */
interface DeviceAutomation : DeviceBridge {
    /** Navega a la pantalla de inicio del sistema (HOME) */
    suspend fun home(): Boolean

    /** Ejecuta el gesto de retroceso global (BACK) */
    suspend fun back(): Boolean

    /** Abre la vista de aplicaciones recientes (RECENTS) */
    suspend fun recentApps(): Boolean

    /**
     * Inicia una aplicación instalada por su nombre de paquete.
     */
    suspend fun openApp(packageName: String): Boolean

    /**
     * Inspecciona la jerarquía visual activa y extrae el árbol de nodos interactivos.
     */
    suspend fun inspectScreen(): ScreenNode?

    /**
     * Realiza una pulsación (clic) sobre un elemento específico o coordenadas.
     */
    suspend fun click(nodeId: Int): Boolean
    suspend fun clickCoordinates(x: Int, y: Int): Boolean

    /**
     * Introduce texto en el elemento de entrada actualmente enfocado o identificado.
     */
    suspend fun typeText(nodeId: Int, text: String): Boolean

    /**
     * Desplaza el contenido de una vista en la dirección especificada.
     */
    suspend fun scroll(direction: ScrollDirection): Boolean
}
