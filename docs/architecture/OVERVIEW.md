# Jarvis Android — Arquitectura del Sistema

## 1. Visión General

**Jarvis Android** es un asistente autónomo nativo para el sistema operativo Android, diseñado con una arquitectura modular y desacoplada. Su objetivo es operar de forma continua (24/7), integrarse a nivel de sistema como el asistente predeterminado del dispositivo y ejecutar acciones complejas mediante un sistema inteligente de herramientas.

---

## 2. Flujo de Ejecución del Sistema

El flujo completo desde la interacción del usuario hasta la ejecución física en el dispositivo sigue un pipeline unidireccional y desacoplado:

```text
Usuario
   │ (Voz / Gesto / Notificación / Trigger)
   ▼
[Input Layer] (AudioRecord / VoiceInteractionService / Overlay)
   │
   ▼
[Assistant Session] (Ciclo de vida, Audio Focus, Mutex de Voz)
   │
   ▼
[Agent] (Manejo de contexto, memoria conversacional, estado)
   │
   ▼
[Planner] (Razonamiento, descomposición de metas en ActionPlan)
   │
   ▼
[ToolRouter] (Resolución y despacho de herramientas semánticas)
   │
   ▼
[Safety Interceptor] (Validación de RiskLevel y confirmación de usuario)
   │
   ▼
[Capability Resolver] (Evaluación de permisos, disponibilidad y fallback)
   │
   ▼
[Device Bridge] (Ejecución concreta en la capa adecuada de Android)
```

---

## 3. Jerarquía y Estrategia de Fallback (Device Bridge)

Para ejecutar cualquier acción en el dispositivo (ej. abrir una app, hacer clic, cambiar un ajuste, forzar detención), Jarvis Android consulta al `CapabilityResolver`.

La preferencia conceptual de ejecución sigue la regla del **menor privilegio necesario**:

```text
┌──────────────────────────────────────────────┐
│             1. Android API Normal            │ (Intents, MediaSession, Telephony, Settings)
└──────────────────────┬───────────────────────┘
                       │ Fallback si no hay API pública
                       ▼
┌──────────────────────────────────────────────┐
│           2. Accessibility Service           │ (Inspección de UI, Clics, Scroll, Type, Gestos)
└──────────────────────┬───────────────────────┘
                       │ Fallback si la UI no es interactuable / en background
                       ▼
┌──────────────────────────────────────────────┐
│           3. Shizuku (Shell UID 2000)        │ (pm, am force-stop, appops, protected settings)
└──────────────────────┬───────────────────────┘
                       │ Fallback opcional si requiere privilegios de sistema
                       ▼
┌──────────────────────────────────────────────┐
│           4. Root (UID 0 - libsu)            │ (Operaciones a bajo nivel, particiones, SELinux)
└──────────────────────────────────────────────┘
```

> [!IMPORTANT]
> **No todas las acciones siguen linealmente este orden.**
> El `CapabilityResolver` evalúa de forma inteligente la naturaleza de la acción:
> - Si la acción es `openApp`, el método directo es `Android API (Intent)` sin requerir accesibilidad.
> - Si la acción es `clickElement("Aceptar")`, el método directo es `Accessibility Service`.
> - Si la acción es `forceStopApp("com.target")`, no existe API pública estándar; el resolver salta directamente a `Shizuku` o `Root`.

---

## 4. Reglas Críticas de Aislamiento Arquitectónico

1. **Agnosticismo de IA (`AssistantGateway`):**
   - El agente no conoce la implementación de OpenAI, ni bibliotecas específicas de red.
   - Todo LLM se comunica a través de la interfaz `AssistantGateway`.

2. **Aislamiento de Android:**
   - Ni OpenAI ni el modelo de razonamiento conocen clases directas de Android (`Context`, `Intent`, `AccessibilityNodeInfo`).
   - El agente solo produce y consume modelos de datos puros en Kotlin (`ActionPlan`, `ToolCall`, `UiElement`).

3. **Seguridad y Prohibición de Shell Arbitrario:**
   - El agente futuro **NUNCA** tendrá disponible herramientas genéricas como `shell("...")`, `su("...")` o `adb("...")`.
   - Todas las herramientas son fuertemente tipadas y semánticas:
     - `openApp(packageName)`
     - `clickElement(elementId)`
     - `setVolume(stream, level)`
     - `forceStopApp(packageName)`
   - Cada herramienta declara su `RiskLevel` y si requiere confirmación explícita del usuario (`requiresConfirmation`).

---

## 5. Estructura Modular de Paquetes

- `com.jarvis.assistant.core`: Utilidades comunes, logging, seguridad básica, modelos transversales.
- `com.jarvis.assistant.assistant`: Integración con Android (`ROLE_ASSISTANT`), `VoiceInteractionService`, detección de wake word y TTS/STT.
- `com.jarvis.assistant.agent`: Motor del agente, planificador, gestión de contexto y herramientas (`ToolRouter`, `SafetyInterceptor`).
- `com.jarvis.assistant.automation`: Servicios de accesibilidad, captura de árbol de UI, listeners de notificaciones y disparadores (triggers).
- `com.jarvis.assistant.device`: Abstracciones de puentes de hardware y sistema (`AndroidBridge`, `AccessibilityBridge`, `ShizukuBridge`, `RootBridge`).
- `com.jarvis.assistant.memory`: Repositorio local de memoria de trabajo, preferencias de usuario y base de conocimiento.
