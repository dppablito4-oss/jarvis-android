# Jarvis Android — Roadmap de Desarrollo

Este documento establece las fases de desarrollo planificadas para la construcción integral de **Jarvis Android**, desde la investigación inicial hasta la autonomía completa en el dispositivo.

---

## Estado de las Fases

- [x] **Phase 0: Repository + research + architecture**
- [ ] **Phase 1: Android System Assistant**
- [ ] **Phase 2: Wake Word**
- [ ] **Phase 3: OpenAI Voice**
- [ ] **Phase 4: Tool System**
- [ ] **Phase 5: Accessibility**
- [ ] **Phase 6: Memory + Events**
- [ ] **Phase 7: Shizuku**
- [ ] **Phase 8: Root**
- [ ] **Phase 9: Autonomous routines**

---

## Detalle de Fases

### Phase 0: Repository + Research + Architecture
- Clonado y auditoría técnica de los 8 repositorios upstream de referencia.
- Análisis de licencias (GPL-3.0, restrictivas, Apache-2.0, MIT) y matriz de procedencia.
- Definición de la arquitectura desacoplada y diseño de capas del sistema.
- Creación de contratos, stubs de interfaces clave y modelo de capacidades (`DeviceCapability`, `DeviceBridge`, `ToolRouter`, `AssistantGateway`).
- Registro de Decisiones de Arquitectura (ADRs).

### Phase 1: Android System Assistant
- Implementación de `VoiceInteractionService` y `VoiceInteractionSession` nativos en Kotlin.
- Registro formal en Android Manifest para calificar como `ROLE_ASSISTANT` predeterminado.
- Manejo de gesto de invocación del sistema (asistente) y atajos de accesibilidad.
- Ventana de overlay flotante básica con Jetpack Compose para interacción visual.

### Phase 2: Wake Word
- Pipeline offline en Python para entrenar el modelo clasificador `jarvis.tflite` usando el pipeline de OpenWakeWord.
- Implementación del runtime nativo Android con TensorFlow Lite (`melspectrogram.tflite`, `embedding.tflite`, `jarvis.tflite`).
- Captura de audio eficiente en segundo plano con `AudioRecord` (16 kHz, mono) y Voice Activity Detection (VAD).
- Coordinador de audio con Mutex para silenciar micrófono durante locuciones TTS.

### Phase 3: OpenAI Voice
- Integración del SDK oficial `openai-java` como concreción de `AssistantGateway`.
- Soporte para streaming de texto y transcripción de voz (STT / TTS).
- Exploración de la API Realtime de OpenAI (WebSockets) para interacción conversacional bidireccional de baja latencia.
- Configuración segura de credenciales mediante Android Keystore / EncryptedSharedPreferences (sin almacenar llaves en código fuente).

### Phase 4: Tool System
- Implementación del enrutador de herramientas (`ToolRouter`).
- Definición semántica de herramientas estándar (`open_app`, `set_volume`, `get_battery_status`, `send_message`, `set_timer`).
- Serialización y deserialización de llamadas estructuradas (`ToolCall` / Function Calling).
- Interceptor de seguridad (`SafetyInterceptor`) y diálogos de confirmación para acciones riesgosas.

### Phase 5: Accessibility
- Implementación de `JarvisAccessibilityService`.
- Lector e indexador de pantalla (`ScreenReader`, `ScreenAnalysis`) con elementos numerados `[1..N]`.
- Despacho de acciones de interfaz: clics por coordenadas o nodo, introducción de texto (`ACTION_SET_TEXT`), scroll y gestos globales (`BACK`, `HOME`, `RECENTS`).
- Fallback y validación de cambios de estado de pantalla.

### Phase 6: Memory + Events
- Base de datos local Room para persistencia de sesiones conversacionales e historial de interacciones.
- Grafo de conocimiento relacional sobre aplicaciones instaladas y preferencias del usuario.
- `NotificationListenerService` para lectura e interpretación de notificaciones entrantes.
- Triggers reactivos basados en eventos del sistema (cargador conectado, nivel de batería, reinicio del sistema).

### Phase 7: Shizuku
- Integración de la biblioteca oficial `rikka.shizuku:api`.
- Concreción de `ShizukuDeviceBridge`.
- Ejecución de operaciones privilegiadas de nivel shell (UID 2000): detención forzada de aplicaciones (`am force-stop`), gestión de permisos (`pm grant/revoke`, `appops`) y lectura/escritura de ajustes globales protegidos (`settings put global`).
- Fallback automático cuando Shizuku no esté activo.

### Phase 8: Root
- Integración de la biblioteca oficial `com.github.topjohnwu.libsu:core` y `:service`.
- Concreción de `RootDeviceBridge` utilizando `RootService` (comunicación tipada Binder IPC con UID 0).
- Soporte para operaciones de bajo nivel (inspección de archivos en `/data`, control de procesos avanzados).
- Aislamiento estricto de seguridad: el modelo de lenguaje nunca ejecutará scripts shell libres con `su`.

### Phase 9: Autonomous Routines
- Motor de planificación multi-paso autónomo con corrección de errores en bucle cerrado (observar -> razonar -> actuar -> verificar).
- Ejecución programada de rutinas en segundo plano con `WorkManager`.
- Notificaciones de resumen y aprendizaje adaptativo de patrones de uso del usuario.
