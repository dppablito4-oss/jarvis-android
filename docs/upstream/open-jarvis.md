# Auditoría Técnica: Open Jarvis

## Información

- **Repositorio:** `https://github.com/tokenarc/open-jarvis.git`
- **Commit:** `04873998933830ce582018fe4664ebce5ec64d2f`
- **Licencia:** MIT License (`MIT`)
- **Lenguaje:** Kotlin (Android nativo)
- **Build:** Gradle Groovy DSL (`build.gradle`), compileSdk 34, minSdk 26
- **Estado:** Activo, agente autónomo de control de dispositivos Android mediante accesibilidad y LLM

---

## Componentes interesantes

1. **`JarvisAccessibilityService`**
   - **Archivo:** `app/src/main/java/com/openjarvis/accessibility/JarvisAccessibilityService.kt`
   - **Responsabilidad:** Servicio de accesibilidad que expone la instancia global (`instance`) y métodos utilitarios como `openAppByPackage(packageName)` y ejecución de gestos.

2. **`ScreenReader`**
   - **Archivo:** `app/src/main/java/com/openjarvis/accessibility/ScreenReader.kt`
   - **Responsabilidad:** Recorrido jerárquico recursivo de `AccessibilityNodeInfo`. Extrae texto legible, descripciones de contenido (`contentDescription`), identificadores de vista y permite buscar nodos por texto insensible a mayúsculas.

3. **`ActionExecutor`**
   - **Archivo:** `app/src/main/java/com/openjarvis/accessibility/ActionExecutor.kt`
   - **Responsabilidad:** Intérprete secuencial de planes de acción (`ActionPlan`). Ejecuta:
     - `OPEN_APP`: Inicia intents de aplicaciones instaladas.
     - `CLICK`: Realiza clic por nodo o coordenadas mediante `performAction(ACTION_CLICK)`.
     - `TYPE`: Escribe texto en campos de entrada (`ACTION_SET_TEXT`).
     - `SCROLL`: Desplaza vistas verticales/horizontales.
     - `BACK` / `HOME`: Gestos de sistema (`GLOBAL_ACTION_BACK`, `GLOBAL_ACTION_HOME`).

4. **`AgentCore`**
   - **Archivo:** `app/src/main/java/com/openjarvis/agent/AgentCore.kt`
   - **Responsabilidad:** Núcleo del agente conversacional y de control. Combina prompt engineering, validación de respuestas de LLM con `LLMResponseValidator` y ejecución con reintentos.

5. **`Graphify` (Base de datos local Room)**
   - **Archivos:** `app/src/main/java/com/openjarvis/graphify/nodes/*` y `GraphifyRepository.kt`
   - **Responsabilidad:** Grafo de conocimiento local con nodos `AppNode`, `ContactNode`, `PatternNode`, `ProviderNode` y aristas `EdgeEntity`. Permite recordar patrones de interacción con apps específicas.

6. **`RiskyActionConfirmation`**
   - **Archivo:** `app/src/main/java/com/openjarvis/agent/RiskyActionConfirmation.kt`
   - **Responsabilidad:** Interceptor de seguridad que clasifica acciones de alto riesgo (ej. compras, borrado de datos, envíos de mensajes) y solicita confirmación del usuario antes de proceder.

---

## Flujo interno

```text
Instrucción de Usuario ("Abre Telegram y escribe a Carlos")
                     │
                     ▼
          [AgentCore & PromptEngine]
                     │
                     ▼
          [LLM Provider / UniversalAdapter]
                     │
                     ▼
             [ActionPlan JSON]
                     │
         [RiskyActionConfirmation]
            ├── (Riesgo Alto) ──► Diálogo de Confirmación
            └── (Seguro) ────────► [ActionExecutor]
                                          │
                  ┌───────────────────────┴───────────────────────┐
                  ▼                                               ▼
          [ScreenReader]                                [JarvisAccessibilityService]
    (Lectura de UI / NodeFinder)                   (click, type, scroll, global actions)
```

---

## Dependencias relevantes

- `androidx.room:room-runtime`, `androidx.room:room-ktx`
- `org.jetbrains.kotlinx:kotlinx-coroutines-android`
- `org.jetbrains.kotlinx:kotlinx-serialization-json`

---

## Qué queremos rescatar

- **Abstracción de ScreenReader:** `ADAPT`
  - Adaptar la técnica de extracción recursiva de nodos para nuestra capa `automation/accessibility/` y `device/accessibility/`.
- **Estructura de `ActionExecutor`:** `ADAPT`
  - Reestructurar el ejecutor de acciones para que implemente nuestra interfaz `DeviceAutomation` y no esté acoplado directamente al servicio de accesibilidad (permitiendo fallbacks con Shizuku/Root).
- **Modelo de seguridad y confirmación:** `ADAPT`
  - Adoptar la política de clasificación de riesgo (`RiskLevel`) para `agent/safety/`.
- **Modelo de base de datos relacional/grafo:** `REFERENCE`

---

## Riesgos

- **Inestabilidad del árbol de accesibilidad:** Los nodos en Android son volátiles y se reciclan rápidamente (`AccessibilityNodeInfo.recycle()`).
- **Permisos de accesibilidad:** El usuario debe habilitar explícitamente el servicio en Accesibilidad. Los fabricantes de Android 13+ aplican "Ajustes restringidos" para apps descargadas fuera de Google Play.
- **Acoplamiento con llamadas directas a LLM:** En Open Jarvis, `AgentCore` mezcla accesibilidad, UI y proveedores LLM. En nuestro Jarvis Android, deben estar completamente desacoplados.

---

## Decisión

**UTILIZAR PARCIALMENTE (`REFERENCE` / `ADAPT`)**
Reutilizar y adaptar la lógica de lectura e interacción con accesibilidad y el sistema de evaluación de riesgos, desacoplándola en nuestros módulos `automation/` y `agent/safety/`.
