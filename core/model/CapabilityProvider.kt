package com.jarvis.assistant.core.model

/**
 * Proveedor responsable de consultar el estado en tiempo de ejecución
 * de una o más capacidades del dispositivo.
 */
interface CapabilityProvider {
    /**
     * Verifica si una capacidad específica está disponible y autorizada en el dispositivo.
     */
    suspend fun hasCapability(capability: DeviceCapability): Boolean

    /**
     * Obtiene el conjunto completo de capacidades actualmente habilitadas en el sistema.
     */
    suspend fun getAvailableCapabilities(): Set<DeviceCapability>
}
