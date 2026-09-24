package com.jarvis.assistant.device.api

/**
 * Contrato para operaciones de sistema que requieren privilegios elevados (Shizuku o Root).
 *
 * CRÍTICO: NO incluye métodos de shell arbitrario como executeShell("...")
 * para garantizar la seguridad del sistema y prevenir inyecciones.
 */
interface PrivilegedBridge : DeviceBridge {
    /**
     * Fuerza la detención inmediata de un paquete de aplicación.
     */
    suspend fun forceStopApp(packageName: String): Boolean

    /**
     * Modifica un ajuste protegido del sistema (Ajustes Globales / Seguros).
     */
    suspend fun changeProtectedSetting(namespace: SettingNamespace, key: String, value: String): Boolean

    /**
     * Concede o revoca permisos especiales de runtime / appops de forma programática.
     */
    suspend fun modifyAppPermission(packageName: String, permission: String, grant: Boolean): Boolean

    /**
     * Ejecuta una operación privilegiada tipada y validada por el sistema.
     */
    suspend fun executePrivilegedOperation(operation: PrivilegedOperation): PrivilegedOperationResult
}

enum class SettingNamespace {
    GLOBAL,
    SECURE,
    SYSTEM
}

sealed class PrivilegedOperation {
    data class ClearAppCache(val packageName: String) : PrivilegedOperation()
    data class SetBatteryOptimization(val packageName: String, val enable: Boolean) : PrivilegedOperation()
    data class DisablePackage(val packageName: String) : PrivilegedOperation()
    data class EnablePackage(val packageName: String) : PrivilegedOperation()
}

data class PrivilegedOperationResult(
    val isSuccess: Boolean,
    val details: String? = null
)
