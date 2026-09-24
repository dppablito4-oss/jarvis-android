# Mapa de Componentes Upstream

Este documento mapea cada capacidad planificada para **Jarvis Android** con los proyectos de referencia investigados, identificando los componentes exactos, archivos y la estrategia de adopción.

---

## Matriz de Componentes

| Capacidad Jarvis | Repo estudiado | Componente upstream exacto | Archivo upstream | Uso previsto | Acción |
|---|---|---|---|---|---|
| **Wake Word Runtime** | Dicio Android | `OpenWakeWordDevice`, `OwwModel` | `org/stypox/dicio/io/wake/oww/OpenWakeWordDevice.kt` | Orquestación de inferencia TFLite en 3 fases (`melspectrogram`, `embedding`, `classifier`) | **Reimplementar** (sin copiar código GPL-3.0) |
| **Wake Word Training** | openWakeWord | `openwakeword.model`, `openwakeword.train` | `openwakeword/model.py`, `train.py`, `docs/synthetic_data_generation.md` | Pipeline offline para sintetizar dataset de voz y exportar `jarvis.tflite` | **Adaptar / Pipeline** |
| **Foreground Audio Lifecycle** | Dicio Android | `WakeService` | `org/stypox/dicio/io/wake/WakeService.kt` | Ciclo de vida de captura de micrófono con `AudioRecord` en background | **Reimplementar** |
| **Android System Assistant** | Hark | `HarkVoiceInteractionService` | `android/app/src/main/kotlin/com/oacp/hark/HarkVoiceInteractionService.kt` | `VoiceInteractionService` para calificar como `ROLE_ASSISTANT` en Android | **Reimplementar** (100% Kotlin nativo) |
| **Assistant Metadata XML** | Hark | `voice_interaction_service.xml` | `android/app/src/main/res/xml/voice_interaction_service.xml` | Configuración de manifest para `supportsAssist`, `supportsLaunchVoiceAssistFromKeyguard` | **Adaptar** |
| **Assistant Session** | Hark | `HarkSessionService`, `HarkSession` | `android/app/src/main/kotlin/com/oacp/hark/HarkSession.kt` | Gestión del evento `onShow` al disparar el gesto de asistente del sistema | **Adaptar** |
| **Accessibility Service** | Open Jarvis | `JarvisAccessibilityService` | `com/openjarvis/accessibility/JarvisAccessibilityService.kt` | Conexión al servicio de accesibilidad y flags de interactividad | **Adaptar** |
| **Screen Parser** | Open Jarvis | `ScreenReader` | `com/openjarvis/accessibility/ScreenReader.kt` | Recorrido recursivo de `AccessibilityNodeInfo` y búsqueda de nodos | **Adaptar** |
| **UI Numeric Indexing** | DeVA | `ScreenAnalysis`, `Perception` | `com/deva/voice/v2/perception/ScreenAnalysis.kt` | Representación de nodos numerados `[1]`, `[2]` con mapa de punteros | **Reimplementar** (diseño propio sin copiar) |
| **Accessibility Actions** | Open Jarvis | `ActionExecutor` | `com/openjarvis/accessibility/ActionExecutor.kt` | Despacho de clics, escritura y gestos de navegación global | **Adaptar** |
| **Speech Concurrency** | DeVA | `SpeechCoordinator` | `com/deva/voice/utilities/SpeechCoordinator.kt` | Coordinación de exclusión mutua con Mutex entre TTS y STT | **Reimplementar** (evita eco y feedback) |
| **Agent Core & Plan** | Open Jarvis | `AgentCore`, `ActionPlan` | `com/openjarvis/agent/AgentCore.kt`, `ActionPlan.kt` | Bucle de ejecución del agente y deserialización de planes | **Rediseñar** |
| **Safety Interceptor** | Open Jarvis | `RiskyActionConfirmation` | `com/openjarvis/agent/RiskyActionConfirmation.kt` | Clasificación de riesgo de acciones y diálogo de confirmación previa | **Adaptar** |
| **Graph / Memory DB** | Open Jarvis | `GraphifyRepository`, `AppNode` | `com/openjarvis/graphify/GraphifyRepository.kt` | Base de datos Room para persistencia de contexto y grafo relacional | **Adaptar** |
| **Shizuku Privilege Bridge** | Shizuku-API | `rikka.shizuku.Shizuku` | `api/src/main/java/rikka/shizuku/Shizuku.java` | Verificación de permisos y ejecución con privilegios shell (UID 2000) | **Integrar luego** (Fase 7, SDK oficial) |
| **Root Service Bridge** | libsu | `Shell`, `RootService` | `com/topjohnwu/superuser/ipc/RootService.java` | Servicio Binder IPC para ejecución segura con superusuario (UID 0) | **Integrar luego** (Fase 8, SDK oficial) |
| **AI Cloud Provider** | openai-java | `OpenAIClientAsync`, Responses API | `com/openai/client/OpenAIClientAsync.kt` | Consumo de streaming, function calling estructurado y audio realtime | **Integrar luego** (Fase 3 y 4, SDK oficial) |

---

## Resumen por Clasificación

1. **Reimplementar desde cero (sin transferencia de código):**
   - Inferencia TFLite de OpenWakeWord (inspirado en Dicio, pero propio).
   - `SpeechCoordinator` (coordinación de mutex TTS/STT de DeVA).
   - `ScreenAnalysis` (indexación numérica de pantalla de DeVA).
   - `VoiceInteractionService` (desacoplado de Flutter).

2. **Adaptar / Rescatar conceptos:**
   - `ScreenReader` y `ActionExecutor` (Open Jarvis, licencia MIT).
   - `RiskyActionConfirmation` (Open Jarvis, licencia MIT).
   - Descriptor XML de Asistente de Sistema (Hark, Apache-2.0).

3. **Librerías / Dependencias Oficiales Futuras:**
   - `openai-java` (Fase 3 / 4)
   - `dev.rikka.shizuku:api` (Fase 7)
   - `com.github.topjohnwu.libsu:core` + `:service` (Fase 8)
