# Informe Final de Fase 0: Clonado, Auditoría, Rescate y Organización Arquitectónica

> **Proyecto:** Jarvis Android
> **Fase:** Phase 0 — Architecture & Upstream Research
> **Fecha de Finalización:** 2026-09-24
> **Responsable:** Antigravity AI Assistant

---

## 1. Repositorios Clonados y Estado de Inspección

Se clonaron con éxito y auditaron los 8 proyectos upstream especificados. Todos los clones se encuentran alojados en el directorio `external/`, protegidos contra versionado accidental en Git mediante reglas estrictas en `.gitignore` (salvo `external/README.md` y `external/manifest.md`).

| Repositorio | Branch | Commit HEAD | Licencia | Clasificación |
|---|---|---|---|---|
| **dicio-android** | `main` | `7b68d11b66544188a75bf07bb6e082cebd7d0725` | GNU GPL v3.0 | `REFERENCE_ONLY` (`REFERENCE_ONLY_GPL`) |
| **openWakeWord** | `main` | `368c03716d1e92591906a84949bc477f3a834455` | Apache 2.0 | `REFERENCE` (`TRAINING_PIPELINE`) |
| **hark** | `main` | `f5c502130c20935e9e9c5b1970e2a8410d0b96fc` | Apache 2.0 | `REFERENCE` (`REFERENCE_ASSISTANT_FRAMEWORK`) |
| **open-jarvis** | `main` | `04873998933830ce582018fe4664ebce5ec64d2f` | MIT | `REFERENCE` / `ADAPT` |
| **DeVA** | `main` | `a5301893c7acf57d83335ae9ba49283cefd7d2f2` | Personal Use (Restrictiva) | `REFERENCE_ONLY` (`REFERENCE_ONLY_LICENSE_RESTRICTED`) |
| **Shizuku-API** | `master` | `a27f6e4151ba7b39965ca47edb2bf0aeed7102e5` | MIT | `FUTURE_DEPENDENCY` / `REFERENCE` |
| **libsu** | `master` | `4910d8dcc1ea3273246614b356fba56e1ce002a5` | Apache 2.0 | `FUTURE_DEPENDENCY` / `REFERENCE` |
| **openai-java** | `main` | `3221905d7b5f0c6c09d72d8ddc7f8c6ec2d861fe` | Apache 2.0 | `FUTURE_DEPENDENCY` |

---

## 2. Componentes Encontrados Más Destacados

1. **Inferencia de Wake Word en TFLite (Dicio):**
   - Clases: `OpenWakeWordDevice` y `OwwModel`.
   - Hallazgo clave: Dicio resuelve la ejecución de openWakeWord en Android sin Python mediante tres intérpretes TFLite encadenados: `melspectrogram.tflite` -> buffer circular de 76x32 -> `embedding.tflite` -> `wake.tflite`.
2. **System Assistant Role (Hark):**
   - Clases: `HarkVoiceInteractionService`, `HarkSessionService`, `HarkSession` y descriptor `voice_interaction_service.xml`.
   - Hallazgo clave: Muestra la configuración exacta requerida por Android para calificar como asistente del sistema (`ROLE_ASSISTANT`), capturar el gesto global y recibir capturas/contexto con `supportsAssist="true"`.
3. **Inspección de UI y Ejecutor de Acciones (Open Jarvis):**
   - Clases: `ScreenReader`, `ActionExecutor`, `RiskyActionConfirmation`.
   - Hallazgo clave: Algoritmo recursivo para extraer textos y descriptores del árbol `AccessibilityNodeInfo` y despachar acciones secuenciales con evaluación previa de riesgo.
4. **Coordinación Concurrente de Voz y Pantalla Numerada (DeVA):**
   - Clases: `SpeechCoordinator`, `ScreenAnalysis`.
   - Hallazgo clave: Uso de `kotlinx.coroutines.sync.Mutex` para evitar que el reconocimiento de voz (STT) escuche la síntesis (TTS), y asignación de identificadores numéricos `[1]`, `[2]` a elementos de pantalla para facilitar el razonamiento al LLM.
5. **IPC Privilegiado con Binder (Shizuku & libsu):**
   - Clases: `rikka.shizuku.Shizuku` y `com.topjohnwu.superuser.ipc.RootService`.
   - Hallazgo clave: La forma moderna y segura de interactuar tanto con Shizuku como con Root es a través de Binder IPC con contratos AIDL, evitando tuberías de texto crudo de shell.

---

## 3. Arquitectura Creada

Se estableció el scaffold inicial de contratos bajo el paquete base provisional `com.jarvis.assistant`:

```text
jarvis-android/
├── core/
│   └── model/
│       ├── DeviceCapability.kt        # Enum de capacidades (NORMAL, ACCESSIBILITY, SHIZUKU, ROOT, etc.)
│       ├── CapabilityProvider.kt      # Consulta de capacidades en runtime
│       └── CapabilityResolver.kt      # Resolución de menor privilegio con fallback
├── assistant/
│   └── voice/
│       └── AssistantGateway.kt        # Contrato agnóstico de proveedor de IA (OpenAI/Local/Gemini)
├── agent/
│   ├── safety/
│   │   ├── RiskLevel.kt               # Niveles de riesgo (LOW, MEDIUM, HIGH, CRITICAL)
│   │   ├── ToolSafetyMetadata.kt      # Metadatos obligatorios por herramienta
│   │   └── SafetyInterceptor.kt       # Interceptor de seguridad y confirmación
│   ├── tools/
│   │   ├── Tool.kt                    # Interfaz semántica de herramienta
│   │   ├── ToolDefinition.kt          # Esquema de herramienta y resultado
│   │   └── ToolRouter.kt              # Registro y despacho de herramientas
│   └── planner/
│       └── ActionPlan.kt              # Modelos ActionPlan y ActionStep
├── device/
│   └── api/
│       ├── DeviceBridge.kt            # Interfaz raíz de puentes
│       ├── DeviceAutomation.kt        # Contrato de interacción de UI (home, click, type, etc.)
│       ├── ScreenNode.kt              # Modelo de datos inmutable de nodos de pantalla
│       ├── PrivilegedBridge.kt        # Operaciones privilegiadas (sin shell arbitrario)
│       ├── ShizukuDeviceBridge.kt     # Stub para integración de Shizuku
│       └── RootDeviceBridge.kt        # Stub para integración de Root
├── docs/
│   ├── architecture/OVERVIEW.md       # Diagrama de flujo y reglas de capas
│   ├── decisions/                     # 5 ADRs formales (Kotlin, Upstream, Bridges, Shell, AI Gateway)
│   └── upstream/                      # 8 auditorías, COMPONENT_MAP.md y PROVENANCE.md
├── external/                          # 8 repositorios clonados e ignorados
├── README.md                          # Estado del proyecto y visión
├── ROADMAP.md                         # Fases 0 a 9
└── .gitignore                         # Exclusiones de Git
```

---

## 4. Código Reutilizable y Adaptable

Bajo licencias permisivas (Apache-2.0 y MIT):
- **De Hark (Apache-2.0):** Descriptor XML `voice_interaction_service.xml` y la estructura de ciclo de vida de `VoiceInteractionSession`.
- **De Open Jarvis (MIT):** Estructura del modelo de árbol de accesibilidad (`ScreenReader`), despachador secuencial de acciones y lógica conceptual de confirmación de acciones riesgosas.
- **De openWakeWord (Apache-2.0):** Pesos base de preprocesamiento (`melspectrogram.tflite` y `embedding.tflite`), especificaciones de normalización de audio y notebooks de síntesis de audio para entrenamiento.

---

## 5. Código que Debemos Reimplementar Obligatoriamente

- **Inferencia de Wake Word (Dicio - GPL-3.0):**
  - **Motivo:** Evitar la contaminación copyleft de la GPL-3.0 en el núcleo de Jarvis.
  - **Plan:** Diseñar desde cero nuestro propio runtime en Kotlin que cargue TensorFlow Lite con corrutinas y Flow propios.
- **Coordinador de Habla y Análisis de Pantalla (DeVA - Licencia Restrictiva):**
  - **Motivo:** Licencia de uso personal/educativo incompatible con distribución abierta o comercial.
  - **Plan:** Reimplementar de forma independiente nuestro propio `SpeechCoordinator` (gestión de Mutex de audio) y nuestro propio formateador de texto de pantalla para prompts de LLM.
- **System Assistant nativo (Hark - Flutter):**
  - **Motivo:** Hark acopla su interfaz a Flutter.
  - **Plan:** Reimplementar la UI del overlay 100% en Jetpack Compose nativo.

---

## 6. Dependencias Futuras Planificadas

- **Fase 1 (Android Assistant & UI):**
  - `androidx.core:core-ktx`
  - `androidx.lifecycle:lifecycle-runtime-ktx`
  - `androidx.compose.ui:ui` & `androidx.compose.material3:material3`
- **Fase 2 (Wake Word):**
  - `org.tensorflow:tensorflow-lite:2.14.0` (o `org.tensorflow:tensorflow-lite-gpu`)
- **Fase 3 & 4 (OpenAI & Herramientas):**
  - `com.openai:openai-java-client-okhttp`
  - `com.squareup.okhttp3:okhttp`
  - `org.jetbrains.kotlinx:kotlinx-serialization-json`
- **Fase 6 (Memoria & Persistencia):**
  - `androidx.room:room-runtime` & `androidx.room:room-ktx`
  - `androidx.room:room-compiler` (KSP)
- **Fase 7 (Shizuku):**
  - `dev.rikka.shizuku:api:13.1.5`
  - `dev.rikka.shizuku:provider:13.1.5`
- **Fase 8 (Root):**
  - `com.github.topjohnwu.libsu:core:5.2.1`
  - `com.github.topjohnwu.libsu:service:5.2.1`

---

## 7. Análisis de Riesgos

1. **Licencias:** Riesgo mitigado. Dicio y DeVA están catalogados como `REFERENCE_ONLY` y aislados fuera del código productivo. Ningún archivo con copyright restrictivo ha sido transferido.
2. **Seguridad y Privilegios:** El diseño prohíbe herramientas de texto libre como `shell("...")` o `su("...")`. En Fase 0 se definieron los contratos de seguridad; el despacho obligatorio mediante `SafetyInterceptor` se implementó posteriormente al iniciar la Functional Alpha.
3. **Restricciones de Background en Android:** En Android 12+, los servicios en segundo plano tienen limitaciones severas para acceder al micrófono o lanzar Activities desde background. Esto se resolverá en la Fase 1 asumiendo formalmente el rol `ROLE_ASSISTANT` del sistema operativo, el cual otorga excepciones legítimas para invocación por voz y captura asistida.
4. **Fragmentación de Fabricantes (OEMs - Xiaomi, Honor, Huawei, Samsung):** Capas de personalización como MagicOS o MIUI pueden sobrescribir o pausar servicios en background agresivamente. Será necesario documentar los pasos de exclusión de optimización de batería y configuración manual del Asistente Digital Predeterminado.

---

## 8. Próximo Paso Recomendado

El siguiente paso obligatorio del proyecto es:

```text
Phase 1 — Implementar el Assistant base de Android.
```

> [!CAUTION]
> De acuerdo con los principios de la Fase 0, **no se ha iniciado la implementación de la Fase 1**. El desarrollo se detiene en este punto a la espera de la orden explícita del usuario.
