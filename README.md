# Jarvis Android

> **Estado actual del proyecto:** `Functional Alpha — foundation in progress`

## Vision

Asistente Android nativo modular diseñado para operar de forma continua (24/7) en el dispositivo, integrarse como el Asistente Predeterminado de Android (`ROLE_ASSISTANT`), procesar voz en tiempo real, ejecutar herramientas seguras y controlar la interfaz mediante accesibilidad, con soporte opcional y resiliente para privilegios elevados (Shizuku y Root).

---

## Current Status

```text
[~] System Assistant (servicios y descriptor compilados; falta validar en dispositivo)
[ ] Wake Word
[ ] Voice
[~] OpenAI (Responses API conectada en debug con `gpt-6-astra`)
[~] Tool Calling (router local seguro y herramientas básicas)
[~] Accessibility (servicio y percepción implementados; falta validar en dispositivo)
[ ] Notifications
[ ] Memory
[ ] Shizuku
[ ] Root

[x] Compilable Android application
[x] Architecture
[x] Upstream research
```

*Nota: La aplicación ya compila y contiene un corte vertical local. Las capacidades marcadas `[~]` requieren instalación y validación en un dispositivo; voz streaming, proveedor cloud, memoria y automatización avanzada siguen pendientes.*

---

## Estructura del Proyecto

```text
jarvis-android/
├── app/                  # Aplicación Android, UI Compose y servicios del sistema
├── assistant/            # Integración con Android System Assistant, Wake Word y Audio
├── agent/                # Núcleo del agente, Planificador, Tools, Contexto y Seguridad
├── core/                 # Utilidades comunes, modelos base, red y logging
├── device/               # Abstracciones de puentes de hardware y sistema (Android, Accessibility, Shizuku, Root)
├── docs/                 # Documentación técnica, arquitectura, ADRs y auditorías upstream
│   ├── architecture/     # Visión de capas, diagramas de flujo y jerarquía de bridges
│   ├── decisions/        # Architecture Decision Records (ADRs)
│   ├── upstream/         # Auditorías de los 8 repositorios, mapa de componentes y procedencia
│   └── PHASE_0_REPORT.md # Informe formal de cierre de Fase 0
├── external/             # Clones de referencia técnica (ignorados por Git)
│   ├── README.md         # Documentación de repositorios externos
│   └── manifest.md       # Metadatos, branches, commits y licencias de los clones
├── README.md             # Este documento
├── ROADMAP.md            # Planificación de fases de desarrollo
├── settings.gradle.kts   # Módulos Gradle
├── build.gradle.kts      # Plugins comunes
├── gradlew / gradlew.bat # Wrapper reproducible
└── .gitignore            # Configuración de exclusiones de Git
```

---

## Repositorios Upstream de Referencia (en `external/`)

1. **dicio-android** (`REFERENCE_ONLY_GPL`): Referencia para audio continuo y OpenWakeWord con TFLite.
2. **openWakeWord** (`REFERENCE / TRAINING`): Pipeline de entrenamiento del modelo clasificador `jarvis.tflite`.
3. **hark** (`REFERENCE_ASSISTANT_FRAMEWORK`): Referencia para `VoiceInteractionService` y `ROLE_ASSISTANT`.
4. **open-jarvis** (`REFERENCE / ADAPT`): Referencia para lectura de pantalla y ejecución con accesibilidad.
5. **DeVA** (`REFERENCE_ONLY_LICENSE_RESTRICTED`): Referencia conceptual para coordinación de audio y UI numerada.
6. **Shizuku-API** (`FUTURE_DEPENDENCY`): Integración futura en Fase 7 para operaciones privileged shell.
7. **libsu** (`FUTURE_DEPENDENCY`): Integración futura en Fase 8 para operaciones seguras con superusuario (RootService).
8. **openai-java** (`FUTURE_DEPENDENCY`): SDK oficial de OpenAI para Fase 3 (Voz) y Fase 4 (Tools).

Para más detalles, consulta [docs/upstream/COMPONENT_MAP.md](docs/upstream/COMPONENT_MAP.md) y [ROADMAP.md](ROADMAP.md).

## Configuración local del proveedor

Las builds `debug` pueden leer estas propiedades desde `local.properties`, archivo excluido de Git:

```properties
JARVIS_OPENAI_API_KEY=...
JARVIS_OPENAI_MODEL=gpt-6-astra
JARVIS_BACKGROUND_MODEL=gpt-6-luna
```

La build `release` fuerza la clave a vacío. Antes de distribuir la aplicación se reemplazará la
autenticación directa por un backend o credenciales efímeras; una clave embebida en un APK debug
solo es aceptable para desarrollo personal.
