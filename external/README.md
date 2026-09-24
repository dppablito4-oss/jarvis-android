# External Upstream Repositories

Este directorio contiene clones locales de repositorios upstream y referencias técnicas para el proyecto Jarvis Android.

> [!WARNING]
> Estos repositorios **NO** forman parte del código fuente de Jarvis Android ni deben incluirse en el control de versiones principal.
> Están ignorados vía `.gitignore` excepto por este archivo y `manifest.md`.

## Repositorios de Referencia

1. **dicio-android** (`REFERENCE_ONLY_GPL`)
   - Captura de audio, ciclo de vida de micrófono, importación TFLite.
   - Licencia: GNU GPL v3.0 (Estricto: No copiar código al core).

2. **openWakeWord** (`REFERENCE / TRAINING`)
   - Arquitectura de detector wake-word, feature embeddings, audio streaming, pipeline de entrenamiento.
   - Licencia: Apache 2.0.

3. **hark** (`REFERENCE_ASSISTANT_FRAMEWORK`)
   - `ROLE_ASSISTANT`, `VoiceInteractionService`, asistente de sistema en Android.
   - Licencia: Apache 2.0.

4. **open-jarvis** (`REFERENCE / ADAPT`)
   - `AccessibilityService`, `ScreenReader`, árbol de UI, jerarquía de acciones.
   - Licencia: Apache 2.0.

5. **DeVA** (`REFERENCE_ONLY_LICENSE_RESTRICTED`)
   - Coordinación de agente conversacional y accesibilidad.
   - Licencia: Restrictiva / Uso personal y educativo (Estricto: No copiar código).

6. **Shizuku-API** (`FUTURE_DEPENDENCY / REFERENCE`)
   - Binder IPC, ejecución de comandos con privilegios shell.
   - Licencia: Apache 2.0.

7. **libsu** (`FUTURE_DEPENDENCY / REFERENCE`)
   - `RootService`, IPC root seguro.
   - Licencia: Apache 2.0.

8. **openai-java** (`FUTURE_DEPENDENCY`)
   - SDK oficial de OpenAI en Java/Kotlin para Responses, Streaming y Tool Calling.
   - Licencia: Apache 2.0.

Consulta `manifest.md` para el registro detallado de cada clon, commits, licencias verificadas y clasificación.
