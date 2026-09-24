package com.jarvis.assistant.device.api

/**
 * Contrato específico para la interacción con privilegios de superusuario (Root UID 0).
 *
 * Se implementará concretamente en la Fase 8 mediante libsu y RootService (Binder IPC).
 */
interface RootDeviceBridge : PrivilegedBridge {
    /**
     * Verifica si el binario su está presente y si el gestor de root (Magisk/KernelSU/APatch)
     * ha concedido permisos a la aplicación.
     */
    override suspend fun isAvailable(): Boolean

    /**
     * Inicia la conexión con el daemon RootService aislado a través de Binder IPC.
     */
    suspend fun bindRootService(): Boolean

    /**
     * Comprueba si el entorno SELinux está en modo Enforcing o Permissive.
     */
    suspend fun isSelinuxEnforcing(): Boolean
}
