package com.jarvis.assistant.core.model

/**
 * Representa las capacidades del dispositivo y niveles de privilegio
 * que el asistente puede requerir para ejecutar distintas acciones.
 *
 * NOTA: El namespace com.jarvis.assistant es preliminar y configurable.
 */
enum class DeviceCapability {
    /** Capacidades estándar de Android sin permisos especiales (Intents públicos, Media, etc.) */
    NORMAL_ANDROID,

    /** Acceso e interacción mediante el servicio de accesibilidad de Android */
    ACCESSIBILITY,

    /** Capacidad de escuchar y procesar notificaciones del sistema */
    NOTIFICATION_ACCESS,

    /** Integración oficial como Asistente Predeterminado del Sistema (ROLE_ASSISTANT) */
    ASSISTANT_ROLE,

    /** Operaciones con privilegios de shell (UID 2000) a través de Shizuku */
    SHIZUKU,

    /** Acceso con privilegios de superusuario (UID 0) a través de libsu / RootService */
    ROOT,

    /** Conectividad de red para comunicación con proveedores en la nube */
    NETWORK,

    /** Acceso continuo al micrófono para detección de wake-word y captura de voz */
    MICROPHONE,

    /** Permiso para mostrar ventanas flotantes sobre otras aplicaciones (SYSTEM_ALERT_WINDOW) */
    OVERLAY
}
