# Auditoría Técnica: Hark

## Información

- **Repositorio:** `https://github.com/OpenAppCapabilityProtocol/hark.git`
- **Commit:** `f5c502130c20935e9e9c5b1970e2a8410d0b96fc`
- **Licencia:** Apache License 2.0 (`Apache-2.0`)
- **Lenguaje:** Kotlin (capa de plataforma Android) / Dart (UI Flutter)
- **Build:** Gradle Kotlin DSL (`build.gradle.kts`), Flutter SDK
- **Estado:** Activo, asistente de voz de código abierto con integración profunda como Asistente de Sistema Android

---

## Componentes interesantes

1. **`HarkVoiceInteractionService`**
   - **Archivo:** `android/app/src/main/kotlin/com/oacp/hark/HarkVoiceInteractionService.kt`
   - **Módulo:** `:android:app`
   - **Responsabilidad:** Extiende `android.service.voice.VoiceInteractionService`. Es el punto de entrada oficial exigido por Android para que la aplicación califique como Asistente Predeterminado del Sistema (`ROLE_ASSISTANT`). Mantiene el estado de disponibilidad del servicio en el ciclo de vida del SO.

2. **`voice_interaction_service.xml`**
   - **Archivo:** `android/app/src/main/res/xml/voice_interaction_service.xml`
   - **Responsabilidad:** Descriptor de metadatos del servicio:
     - `android:sessionService`: Vincula con `HarkSessionService`.
     - `android:recognitionService`: Vincula con `HarkRecognitionService`.
     - `android:supportsAssist="true"`: Permite recibir capturas de pantalla y contexto de la ventana activa al invocar el asistente.
     - `android:supportsLaunchVoiceAssistFromKeyguard="true"`: Permite invocar el asistente con la pantalla bloqueada.
     - `android:supportsLocalInteraction="true"`.

3. **`HarkSession` & `HarkSessionService`**
   - **Archivo:** `android/app/src/main/kotlin/com/oacp/hark/HarkSession.kt` y `HarkSessionService.kt`
   - **Responsabilidad:** Extiende `VoiceInteractionSession`. Se ejecuta cuando el usuario activa el gesto del asistente (swipe desde la esquina o pulsación prolongada de home/power). Lanza el overlay de interfaz de usuario.

4. **`WakeWordService`**
   - **Archivo:** `android/app/src/main/kotlin/com/oacp/hark/WakeWordService.kt`
   - **Responsabilidad:** Servicio en primer plano para detección continua de palabras de activación y puente con el servicio de interacción de voz.

---

## Flujo interno

```text
Gesto Asistente / Pulsación Larga
              │
              ▼
    [VoiceInteractionService] (Android Framework)
              │
              ▼
    [HarkSessionService -> HarkSession]
              │
              ├─► onShow() -> Extrae contexto / AssistContent
              └─► Inicia OverlayActivity / Dialog
```

---

## Dependencias relevantes

- Android Framework `android.service.voice.*`
- `androidx.core:core-ktx`

---

## Qué queremos rescatar

- **Estructura de `VoiceInteractionService` y Manifiesto Android:** `REIMPLEMENT`
  - Reimplementar limpiamente en Kotlin puro para Jarvis Android en Fase 1, sin arrastrar Flutter ni dependencias externas innecesarias.
- **Configuración de `ROLE_ASSISTANT` y metadata XML:** `ADAPT`
- **Gestión de ciclo de vida de sesión (`VoiceInteractionSession`):** `ADAPT`
- **Capa Flutter UI:** `DO_NOT_USE`
  - Descartada por completo. Toda la UI de Jarvis Android será nativa con Jetpack Compose.

---

## Riesgos

- **Requisitos de permisos del sistema:** `android.permission.BIND_VOICE_INTERACTION` debe ser protegido y solo el sistema puede enlazarlo.
- **Fragmentación por fabricantes (OEMs):** En marcas como Xiaomi (MIUI/HyperOS), Huawei/Honor (MagicOS) o Samsung (OneUI), la asignación del asistente predeterminado a veces es reescrita o requiere navegación manual del usuario en Ajustes del Sistema -> Aplicaciones Predeterminadas -> Asistente Digital.

---

## Decisión

**UTILIZAR PARCIALMENTE (`REFERENCE_ASSISTANT_FRAMEWORK`)**
Adoptar como referencia directa para la arquitectura del subsistema Android System Assistant (`assistant/system/`) en la Fase 1. Descartar la UI de Flutter.
