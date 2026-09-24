# ADR 0003: Abstracción Jerárquica de Control del Dispositivo (DeviceBridge)

## Contexto

Para que el asistente ejecute acciones en el teléfono (abrir aplicaciones, cambiar ajustes, interactuar con la pantalla, detener procesos), Android ofrece múltiples mecanismos con diferentes niveles de privilegios:
1. APIs estándar de Android (Intents, MediaSession, Settings).
2. `AccessibilityService` (clics, texto, scroll en pantalla).
3. `Shizuku` (privilegios de shell adb UID 2000 sin root).
4. `Root` (superusuario UID 0 vía libsu).

Si el agente o el planificador invocaran directamente estas APIs concretas, cualquier cambio en permisos o ausencia de root rompería el sistema o requeriría condicionales dispersos por todo el código.

## Decisión

Definir una jerarquía de abstracciones unificadas (`DeviceBridge`, `DeviceAutomation`, `PrivilegedBridge`, `RootDeviceBridge`, `ShizukuDeviceBridge`) coordinadas por un `CapabilityResolver`.
El agente emite intenciones semánticas puras (ej. `openApp`, `clickElement`, `forceStopApp`), y el `CapabilityResolver` selecciona la mejor implementación disponible siguiendo la regla de menor privilegio con fallback inteligente:
- Si existe API estándar de Android -> se usa API estándar.
- Si requiere interacción visual -> se usa Accessibility.
- Si requiere privilegios avanzados y Shizuku está activo -> se usa Shizuku.
- Si requiere privilegios de bajo nivel y Root está activo -> se usa Root.

## Consecuencias

### Positivas
- Aislamiento total: el agente no sabe si una acción se resolvió por accesibilidad, por intent o por Shizuku.
- Resiliencia: si Shizuku o Root no están presentes en el teléfono, el asistente continúa funcionando en un 90% con las capacidades normales de Android y accesibilidad.
- Pruebas unitarias sencillas mediante implementaciones simuladas (mocks/stubs).

### Negativas
- Mayor número de interfaces e intermediarios en la capa de dispositivo.
