# Auditoría Técnica: DeVA

## Información

- **Repositorio:** `https://github.com/Devanshupardeshi/DeVA.git`
- **Commit:** `a5301893c7acf57d83335ae9ba49283cefd7d2f2`
- **Licencia:** Personal Use License (Restrictiva: solo uso personal y educativo, prohibido uso comercial o distribución sin permiso)
- **Lenguaje:** Kotlin (Android nativo con Jetpack Compose)
- **Build:** Gradle Kotlin DSL (`build.gradle.kts`), compileSdk 35
- **Estado:** Activo, asistente de voz interactivo y agente de tareas multi-paso para Android

---

## Componentes interesantes

1. **`ConversationalAgentService`**
   - **Archivo:** `app/src/main/java/com/deva/voice/ConversationalAgentService.kt`
   - **Módulo:** `:app`
   - **Responsabilidad:** Servicio Foreground continuo que mantiene el overlay interactivo (glow border, vista delta) en ventana flotante del sistema (`WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY`) y coordina las peticiones de usuario.

2. **`SpeechCoordinator`**
   - **Archivo:** `app/src/main/java/com/deva/voice/utilities/SpeechCoordinator.kt`
   - **Módulo:** `:app`
   - **Responsabilidad:** Coordinador de concurrencia de audio basado en `kotlinx.coroutines.sync.Mutex`. Previene la colisión entre síntesis de voz (TTS) y reconocimiento de voz (STT), asegurando que el micrófono no escuche la propia voz del asistente (eliminación de eco lógica).

3. **`ScreenAnalysis` & `Perception`**
   - **Archivos:** `app/src/main/java/com/deva/voice/v2/perception/ScreenAnalysis.kt` y `Perception.kt`
   - **Módulo:** `:app`
   - **Responsabilidad:** Representación serializable de la pantalla para el LLM. Numera los elementos interactivos visibles como `[1] Botón Enviar`, `[2] Campo de texto` y almacena un mapa `Map<Int, AccessibilityNodeInfo>` para que el planificador pueda responder con un entero y el ejecutor accione directamente ese nodo.

4. **`TriggerMonitoringService` & Triggers**
   - **Archivos:** `app/src/main/java/com/deva/voice/triggers/TriggerMonitoringService.kt`
   - **Módulo:** `:app`
   - **Responsabilidad:** Monitoreo reactivo de eventos del sistema (conexión de cargador, nivel de batería, reinicio del dispositivo `BootReceiver`, llegada de notificaciones con `DeVANotificationListenerService`).

---

## Flujo interno

```text
Evento de activación (Voz / Trigger / Gesto)
                     │
                     ▼
       [ConversationalAgentService]
                     │
          [SpeechCoordinator] (Mutex)
           ├── Silencia TTS
           └── Activa STT / Entrada de Usuario
                     │
                     ▼
         [Perception Module] ──► Extrae ScreenAnalysis [1..N]
                     │
                     ▼
         [Agent Model / LLM] ──► Genera Acción(id=2, "escribir texto")
                     │
                     ▼
         [ActionExecutor] ────► Opera sobre elemento del mapa
```

---

## Dependencias relevantes

- Android Jetpack Compose
- `com.google.ai.client.generativeai` (Google Gemini SDK)
- `com.google.firebase:firebase-crashlytics`
- Android Accessibility & Notification Listener Services

---

## Qué queremos rescatar

- **Coordinación de audio con Mutex:** `REIMPLEMENT`
  - La idea de coordinar TTS y STT usando un mutex concurrente es excelente para evitar retroalimentación acústica. Se reimplementará desde cero con diseño limpio.
- **Indexación numérica de elementos de pantalla para el LLM:** `REIMPLEMENT`
  - Reimplementar nuestro propio serializador de nodos UI para el contexto del agente (`agent/context/`).
- **Arquitectura de triggers reactivos:** `REFERENCE_ONLY`
- **Código fuente, clases o archivos:** `DO_NOT_USE` (Estrictamente prohibido por la licencia restrictiva).

---

## Riesgos

- **Licencia restrictiva no comercial:** Cualquier copia literal o derivada de código violaría los términos de la licencia de DeVA. No se debe copiar ningún archivo.
- **Acoplamiento extremo con Gemini SDK propietario:** DeVA está cableado rígidamente a Gemini. Jarvis Android requiere una abstracción agnóstica (`AssistantGateway`).

---

## Decisión

**SOLO REFERENCIA (`REFERENCE_ONLY_LICENSE_RESTRICTED`)**
Estudiar conceptualmente la coordinación concurrente de audio (SpeechCoordinator) y la representación numerada del árbol de UI para LLMs. Prohibido copiar código fuente o importar dependencias del autor.
