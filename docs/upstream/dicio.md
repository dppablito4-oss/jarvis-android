# Auditoría Técnica: Dicio Android

## Información

- **Repositorio:** `https://github.com/DicioTeam/dicio-android.git`
- **Commit:** `7b68d11b66544188a75bf07bb6e082cebd7d0725`
- **Licencia:** GNU General Public License v3.0 (`GPL-3.0`)
- **Lenguaje:** Kotlin (100% Android nativo)
- **Build:** Gradle Kotlin DSL (`build.gradle.kts`), Version Catalog (`gradle/libs.versions.toml`), Android compileSdk 35, minSdk 21, targetSdk 35
- **Estado:** Activo, asistente de voz libre y de código abierto para Android

---

## Componentes interesantes

1. **`OpenWakeWordDevice`**
   - **Archivo:** `app/src/main/kotlin/org/stypox/dicio/io/wake/oww/OpenWakeWordDevice.kt`
   - **Módulo:** `:app`
   - **Responsabilidad:** Gestiona la descarga inicial y verificación de archivos `.tflite` (`melspectrogram.tflite`, `embedding.tflite`, `wake.tflite`), inicializa `OwwModel` y coordina la recepción de muestras PCM float de audio.

2. **`OwwModel`**
   - **Archivo:** `app/src/main/kotlin/org/stypox/dicio/io/wake/oww/OwwModel.kt`
   - **Módulo:** `:app`
   - **Responsabilidad:** Ejecución en cascada con TensorFlow Lite (`org.tensorflow.lite.Interpreter`). En el commit auditado transforma bloques de 1152 muestras en 5 filas de 32 mel-features, acumula una ventana FIFO de 76x32, alimenta el modelo de embeddings y ejecuta el clasificador devolviendo la probabilidad (0.0 a 1.0).

3. **`WakeService`**
   - **Archivo:** `app/src/main/kotlin/org/stypox/dicio/io/wake/WakeService.kt`
   - **Módulo:** `:app`
   - **Responsabilidad:** Servicio en segundo plano (Foreground Service) que gestiona el ciclo de vida continuo del micrófono, control de audio focus y emisión de eventos de detección a través de Kotlin StateFlow.

4. **`AndroidTtsSpeechDevice`**
   - **Archivo:** `app/src/main/kotlin/org/stypox/dicio/io/speech/AndroidTtsSpeechDevice.kt`
   - **Módulo:** `:app`
   - **Responsabilidad:** Envoltura sobre `android.speech.tts.TextToSpeech`, manejo de cola de habla, listener de fin de locución y selección de idioma.

5. **`SkillContext` & `Skill`**
   - **Archivo:** `skill/src/main/java/org/dicio/skill/context/SkillContext.kt` y `Skill.kt`
   - **Módulo:** `:skill`
   - **Responsabilidad:** Contratos desacoplados para skills que reciben intents o texto y devuelven un `InteractionPlan` sin acoplarse directamente a la UI.

---

## Flujo interno

1. El usuario inicia la detección continua o el sistema arranca `WakeService`.
2. `WakeService` obtiene el micrófono vía `AudioRecord` (16 kHz, mono, PCM 16-bit) y `OpenWakeWordDevice` convierte las muestras a floats normalizados `[-1.0, 1.0]`.
3. Los chunks usados por la implementación auditada son de 1152 muestras (72 ms) y se envían a `OpenWakeWordDevice.processFrame(audio)`. Esto difiere del runtime Python upstream, que recomienda múltiplos de 1280 muestras (80 ms).
4. `OwwModel` ejecuta en TFLite:
   - `melInterpreter`: genera espectrograma.
   - Cada llamada produce 5x32 mel-features y actualiza el buffer circular de 76x32.
   - `embInterpreter`: genera embedding acústico.
   - `wakeInterpreter`: clasifica si corresponde a la palabra clave.
5. Si el score supera el umbral configurado (ej. 0.5), se detiene el clasificador y se dispara la activación del asistente.

---

## Dependencias relevantes

- `org.tensorflow:tensorflow-lite:2.14.0`
- `com.squareup.okhttp3:okhttp` (para descarga de modelos)
- `androidx.datastore:datastore-preferences`
- `com.google.dagger:hilt-android`
- `org.vosk:vosk-android` (STT offline alternativo)

---

## Qué queremos rescatar

- **Canalización TFLite para OpenWakeWord:** `REIMPLEMENT`
  - Reimplementar desde cero con arquitectura limpia y propia la carga de los 3 modelos TFLite (`melspectrogram.tflite`, `embedding.tflite`, `jarvis.tflite`) y la lógica de buffers de audio, sin copiar código fuente.
- **Patrón de Foreground Service para Audio:** `REFERENCE_ONLY`
- **Gestión de TTS:** `REIMPLEMENT`

---

## Riesgos

- **Licencia GPL-3.0:** Copiar cualquier clase o fragmento directamente obligaría a licenciar todo Jarvis Android bajo GPL-3.0. **Prohibido copiar código fuente.**
- **Consumo de batería:** La inferencia TFLite continua cada 80 ms consume CPU si no se implementa VAD previo (Voice Activity Detection) o delegación NNAPI/GPU.
- **Bloqueo del micrófono por el sistema:** En Android 11+ los servicios en background tienen restricciones estrictas sobre `RECORD_AUDIO`.

---

## Decisión

**SOLO REFERENCIA (`REFERENCE_ONLY_GPL`)**
Utilizar exclusivamente como especificación funcional y técnica de cómo orquestar OpenWakeWord con TensorFlow Lite en Android nativo. No copiar código fuente.
