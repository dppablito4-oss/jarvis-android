package com.jarvis.assistant.core.model

/**
 * Resuelve la mejor vía o bridge de ejecución para una acción determinada,
 * aplicando la regla del menor privilegio necesario y fallback inteligente.
 */
interface CapabilityResolver {
    /**
     * Determina la capacidad óptima disponible para ejecutar una operación dada.
     *
     * @param preferredPreferences Lista ordenada de capacidades candidatas para la acción.
     * @return La primera capacidad disponible en el dispositivo, o null si ninguna es viable.
     */
    suspend fun resolveExecutionCapability(preferredPreferences: List<DeviceCapability>): DeviceCapability?
}
