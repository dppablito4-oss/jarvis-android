# Jarvis Android

> **Estado actual del proyecto:** `Phase 0 — Architecture & Upstream Research`

## Vision

Asistente Android nativo modular diseñado para operar de forma continua (24/7) en el dispositivo, integrarse como el Asistente Predeterminado de Android (`ROLE_ASSISTANT`), procesar voz en tiempo real, ejecutar herramientas seguras y controlar la interfaz mediante accesibilidad, con soporte opcional y resiliente para privilegios elevados (Shizuku y Root).

---

## Current Status

```text
[ ] System Assistant
[ ] Wake Word
[ ] Voice
[ ] OpenAI
[ ] Tool Calling
[ ] Accessibility
[ ] Notifications
[ ] Memory
[ ] Shizuku
[ ] Root

[x] Architecture
[x] Upstream research
```

*Nota: Ninguna funcionalidad física del asistente está implementada ni activa todavía. La Fase 0 establece los cimientos de arquitectura, auditoría de código upstream, reglas de licencias, contratos e interfaces mínimas.*

---

## Estructura del Proyecto

```text
jarvis-android/
├── app/                  # Aplicación Android base, Application class, DI
├── assistant/            # Integración con Android System Assistant, Wake Word y Audio
├── agent/                # Núcleo del agente, Planificador, Tools, Contexto y Seguridad
├── automation/           # Accesibilidad, Notificaciones, Intents, Eventos y Triggers
├── core/                 # Utilidades comunes, modelos base, red y logging
├── device/               # Abstracciones de puentes de hardware y sistema (Android, Accessibility, Shizuku, Root)
├── memory/               # Almacenamiento local, grafo relacional y preferencias
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
