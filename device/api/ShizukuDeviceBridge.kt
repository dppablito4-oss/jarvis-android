package com.jarvis.assistant.device.api

/**
 * Contrato específico para la integración con Shizuku (privilegios de nivel shell / adb UID 2000).
 *
 * Se implementará concretamente en la Fase 7 integrando dev.rikka.shizuku:api.
 */
interface ShizukuDeviceBridge : PrivilegedBridge {
    /**
     * Verifica si el servicio Shizuku está ejecutándose y si la aplicación tiene permiso concedido.
     */
    override suspend fun isAvailable(): Boolean

    /**
     * Solicita la vinculación con el Binder del servidor Shizuku.
     */
    suspend fun requestBinding(): Boolean

    /**
     * Consulta la versión del servidor de Shizuku activo en el dispositivo.
     */
    suspend fun getServerVersion(): Int?
}
