# Upstream Repositories Manifest

Este documento registra los metadatos exactos de los 8 repositorios clonados en `external/` para investigación técnica y diseño arquitectónico en la **Fase 0**.

---

## 1. Dicio Android

- **Nombre:** dicio-android
- **URL:** `https://github.com/DicioTeam/dicio-android.git`
- **Branch detectada:** `main`
- **Commit HEAD:** `7b68d11b66544188a75bf07bb6e082cebd7d0725`
- **Fecha de inspección:** 2026-09-24
- **Licencia detectada:** GNU General Public License v3.0 (`GPL-3.0`)
- **Lenguaje principal:** Kotlin (Android)
- **Build system:** Gradle (Kotlin DSL, `build.gradle.kts`, Version Catalog `libs.versions.toml`)
- **Módulos importantes:**
  - `:app` (UI Jetpack Compose, DI Hilt, integración de audio y ciclo de vida)
  - `:skill` (SDK base para definición y evaluación de skills/intents)
  - `:sentences-compiler-plugin` (Plugin compilador Kotlin para oraciones de reconocimiento)
  - `:app:org.stypox.dicio.io.wake.oww` (Integración de OpenWakeWord con TensorFlow Lite)
  - `:app:org.stypox.dicio.io.speech` (TTS y dispositivos de salida de habla)
- **Uso previsto:** Referencia de captura de audio con AudioRecord, ciclo de vida de escucha en segundo plano, e integración de openWakeWord en runtime Android con TFLite.
- **Clasificación:** `REFERENCE_ONLY` (Etiqueta estricta: `REFERENCE_ONLY_GPL`)

---

## 2. openWakeWord

- **Nombre:** openWakeWord
- **URL:** `https://github.com/dscripka/openWakeWord.git`
- **Branch detectada:** `main`
- **Commit HEAD:** `368c03716d1e92591906a84949bc477f3a834455`
- **Fecha de inspección:** 2026-09-24
- **Licencia detectada:** Apache License 2.0 (`Apache-2.0`)
- **Lenguaje principal:** Python
- **Build system:** Setuptools / PyProject (`setup.py`)
- **Módulos importantes:**
  - `openwakeword/model.py` (Clase central `Model`, extracción de mel-spectrogram y embeddings)
  - `openwakeword/vad.py` (Voice Activity Detection basado en Silero VAD)
  - `openwakeword/train.py` (Script de entrenamiento y fine-tuning)
  - `openwakeword/custom_verifier_model.py` (Verificadores acústicos para mitigar falsos positivos)
  - `examples/streaming_server.py` y `examples/detect_from_microphone.py`
  - `docs/synthetic_data_generation.md` (Pipeline de generación de audio sintético con TTS)
- **Uso previsto:** Base de investigación y pipeline de entrenamiento para generar el modelo personalizado `jarvis.tflite` y exportar `melspectrogram.tflite` + `embedding.tflite`.
- **Clasificación:** `REFERENCE` / `TRAINING_PIPELINE`

---

## 3. Hark

- **Nombre:** hark
- **URL:** `https://github.com/OpenAppCapabilityProtocol/hark.git`
- **Branch detectada:** `main`
- **Commit HEAD:** `f5c502130c20935e9e9c5b1970e2a8410d0b96fc`
- **Fecha de inspección:** 2026-09-24
- **Licencia detectada:** Apache License 2.0 (`Apache-2.0`)
- **Lenguaje principal:** Kotlin (Android nativo platform) / Dart (Flutter frontend)
- **Build system:** Gradle (`build.gradle.kts`) + Flutter Engine
- **Módulos importantes:**
  - `android/app/src/main/kotlin/com/oacp/hark/HarkVoiceInteractionService.kt` (`VoiceInteractionService` de Android)
  - `android/app/src/main/kotlin/com/oacp/hark/HarkSessionService.kt` y `HarkSession.kt` (Gestión de sesión del asistente de sistema)
  - `android/app/src/main/kotlin/com/oacp/hark/HarkRecognitionService.kt` (`RecognitionService` base)
  - `android/app/src/main/kotlin/com/oacp/hark/WakeWordService.kt` (Servicio de escucha continuo en background)
  - `android/app/src/main/res/xml/voice_interaction_service.xml` (Descriptor XML para `ROLE_ASSISTANT`)
  - `packages/hark_platform/android/.../WakeWordDetector.kt`
- **Uso previsto:** Referencia de implementación de System Assistant nativo (`ROLE_ASSISTANT`), `VoiceInteractionService`, `VoiceInteractionSession` y configuración de manifiesto. Descartando capa Flutter en favor de Kotlin/Compose nativo.
- **Clasificación:** `REFERENCE` (Etiqueta: `REFERENCE_ASSISTANT_FRAMEWORK`)

---

## 4. Open Jarvis

- **Nombre:** open-jarvis
- **URL:** `https://github.com/tokenarc/open-jarvis.git`
- **Branch detectada:** `main`
- **Commit HEAD:** `04873998933830ce582018fe4664ebce5ec64d2f`
- **Fecha de inspección:** 2026-09-24
- **Licencia detectada:** MIT License (`MIT`)
- **Lenguaje principal:** Kotlin (Android)
- **Build system:** Gradle (`build.gradle`, Groovy DSL)
- **Módulos importantes:**
  - `app/src/main/java/com/openjarvis/accessibility/JarvisAccessibilityService.kt` (Servicio de accesibilidad base)
  - `app/src/main/java/com/openjarvis/accessibility/ScreenReader.kt` (Recorrido recursivo de `AccessibilityNodeInfo` y parsing textual)
  - `app/src/main/java/com/openjarvis/accessibility/ActionExecutor.kt` (Ejecución de acciones secuenciales `click`, `type`, `scroll`, `openApp`)
  - `app/src/main/java/com/openjarvis/agent/AgentCore.kt` (Bucle de agente, gestión de prompt y validaciones)
  - `app/src/main/java/com/openjarvis/graphify/` (Base de datos Room para grafo de aplicaciones, contactos, patrones)
  - `app/src/main/java/com/openjarvis/agent/RiskyActionConfirmation.kt` (Modelo de seguridad y confirmación previa)
- **Uso previsto:** Base de adaptación conceptual para el subsistema de accesibilidad (`ScreenReader`, `ActionExecutor`), modelo de confirmación de riesgo y persistencia local Room.
- **Clasificación:** `REFERENCE` / `ADAPT`

---

## 5. DeVA

- **Nombre:** DeVA
- **URL:** `https://github.com/Devanshupardeshi/DeVA.git`
- **Branch detectada:** `main`
- **Commit HEAD:** `a5301893c7acf57d83335ae9ba49283cefd7d2f2`
- **Fecha de inspección:** 2026-09-24
- **Licencia detectada:** Personal Use License (Restrictiva: solo fines personales, educativos y no comerciales)
- **Lenguaje principal:** Kotlin (Android)
- **Build system:** Gradle (Kotlin DSL, `build.gradle.kts`)
- **Módulos importantes:**
  - `app/src/main/java/com/deva/voice/ConversationalAgentService.kt` (Servicio foreground de overlay y coordinación de agente)
  - `app/src/main/java/com/deva/voice/utilities/SpeechCoordinator.kt` (Coordinación concurrente con Mutex entre TTS y STT)
  - `app/src/main/java/com/deva/voice/v2/perception/ScreenAnalysis.kt` (Mapeo de elementos de pantalla indexados con enteros `[1]`, `[2]`)
  - `app/src/main/java/com/deva/voice/v2/perception/Perception.kt` y `SemanticParser.kt`
  - `app/src/main/java/com/deva/voice/v2/actions/ActionExecutor.kt`
  - `app/src/main/java/com/deva/voice/triggers/TriggerMonitoringService.kt` (Monitoreo de eventos del sistema: batería, boot, notificaciones)
- **Uso previsto:** Estudio arquitectónico de la coordinación de estados de voz (SpeechCoordinator), indexación de UI para LLM y triggers basados en eventos.
- **Clasificación:** `REFERENCE_ONLY` (Etiqueta estricta: `REFERENCE_ONLY_LICENSE_RESTRICTED`)

---

## 6. Shizuku API

- **Nombre:** Shizuku-API
- **URL:** `https://github.com/RikkaApps/Shizuku-API.git`
- **Branch detectada:** `master`
- **Commit HEAD:** `a27f6e4151ba7b39965ca47edb2bf0aeed7102e5`
- **Fecha de inspección:** 2026-09-24
- **Licencia detectada:** MIT License (`MIT`)
- **Lenguaje principal:** Java / AIDL
- **Build system:** Gradle (`build.gradle`)
- **Módulos importantes:**
  - `:api` (`rikka.shizuku.Shizuku`: ciclo de vida de Binder, verificación y solicitud de permisos shell)
  - `:aidl` (`IShizukuService.aidl`, `IShizukuApplication.aidl`, `IRemoteProcess.aidl`)
  - `:provider` (`ShizukuProvider`: inicialización y recepción de Binder)
  - `:demo` (`UserService`: ejemplo de servicio ejecutado en el proceso privilegiado de Shizuku)
- **Uso previsto:** Dependencia de integración en Fase 7 para la abstracción `ShizukuDeviceBridge`, permitiendo ejecutar operaciones privilegiadas (ej. `force-stop`, `appops`, settings globales) sin requerir root.
- **Clasificación:** `FUTURE_DEPENDENCY` / `REFERENCE`

---

## 7. libsu

- **Nombre:** libsu
- **URL:** `https://github.com/topjohnwu/libsu.git`
- **Branch detectada:** `master`
- **Commit HEAD:** `4910d8dcc1ea3273246614b356fba56e1ce002a5`
- **Fecha de inspección:** 2026-09-24
- **Licencia detectada:** Apache License 2.0 (`Apache-2.0`)
- **Lenguaje principal:** Java
- **Build system:** Gradle (Kotlin DSL, `build.gradle.kts`)
- **Módulos importantes:**
  - `:core` (`com.topjohnwu.superuser.Shell`: gestión segura de sesiones root y callbacks en UI thread)
  - `:service` (`com.topjohnwu.superuser.ipc.RootService`: Binder IPC para ejecutar un daemon independiente con privilegios de root)
  - `:io` (`SuFile`, `SuFileInputStream`: operaciones I/O en rutas protegidas `/data`)
  - `:nio` (`FileSystemManager`: operaciones avanzadas de sistema de archivos root)
- **Uso previsto:** Dependencia de integración en Fase 8 para la abstracción `RootDeviceBridge`, utilizando `RootService` (Binder IPC) en lugar de tuberías de texto crudo de shell `su`.
- **Clasificación:** `FUTURE_DEPENDENCY` / `REFERENCE`

---

## 8. OpenAI Java

- **Nombre:** openai-java
- **URL:** `https://github.com/openai/openai-java.git`
- **Branch detectada:** `main`
- **Commit HEAD:** `3221905d7b5f0c6c09d72d8ddc7f8c6ec2d861fe`
- **Fecha de inspección:** 2026-09-24
- **Licencia detectada:** Apache License 2.0 (`Apache-2.0`)
- **Lenguaje principal:** Kotlin / Java
- **Build system:** Gradle (Kotlin DSL, `build.gradle.kts`)
- **Módulos importantes:**
  - `openai-java-client-okhttp` (Transporte HTTP asíncrono con OkHttp)
  - `openai-java-core-services-responses` y `openai-java-core-models-beta-responses` (Responses API oficial)
  - `openai-java-core-models-realtime` (Modelos y eventos de la API Realtime de voz)
  - `openai-java-core` (Manejo de SSE / Streaming: `SseHandler`, `StreamHandler`)
- **Uso previsto:** SDK oficial para la integración del proveedor OpenAI (`AssistantGateway`) en Fase 3 y Fase 4, soportando streaming, function/tool calling estructurado y audio realtime.
- **Clasificación:** `FUTURE_DEPENDENCY`
