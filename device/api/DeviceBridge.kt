package com.jarvis.assistant.device.api

/**
 * Interfaz raíz para todos los puentes de interacción con hardware y sistema.
 */
interface DeviceBridge {
    /**
     * Comprueba si el puente está actualmente disponible y operativo en el dispositivo.
     */
    suspend fun isAvailable(): Boolean
}
