# Auditoría Técnica: OpenAI Java SDK

## Información

- **Repositorio:** `https://github.com/openai/openai-java.git`
- **Commit:** `3221905d7b5f0c6c09d72d8ddc7f8c6ec2d861fe`
- **Licencia:** Apache License 2.0 (`Apache-2.0`)
- **Lenguaje:** Kotlin / Java
- **Build:** Gradle Kotlin DSL (`build.gradle.kts`)
- **Estado:** Activo, biblioteca cliente oficial de OpenAI para el ecosistema JVM/Android

---

## Componentes interesantes

1. **`OpenAIClient` & `OpenAIClientAsync`**
   - **Archivos:** `openai-java-core/src/main/kotlin/com/openai/client/OpenAIClient.kt`
   - **Módulo:** `openai-java-core`
   - **Responsabilidad:** Punto de entrada síncrono y asíncrono para consumir los servicios de OpenAI. Soporta configuración de `apiKey`, timeouts, proxies y headers personalizados.

2. **Responses API (`openai-java-core-services-responses`)**
   - **Archivos:** `openai-java-core-services-responses` y `models-beta-responses`
   - **Módulo:** Responses API oficial que unifica chat completions, function calling estructurado y streaming de tokens.

3. **Manejo de Streaming y Server-Sent Events (SSE)**
   - **Archivos:** `openai-java-core/src/main/kotlin/com/openai/core/handlers/SseHandler.kt` y `StreamHandler.kt`
   - **Módulo:** `openai-java-core`
   - **Responsabilidad:** Decodificación eficiente de eventos por bloques (`AsyncStreamResponse`), fundamental para reproducir voz en streaming o mostrar respuestas en tiempo real con baja latencia.

4. **Realtime API Models (`openai-java-core-models-realtime`)**
   - **Archivos:** `openai-java-core-models-realtime`
   - **Módulo:** Modelos para la API Realtime de baja latencia con WebSocket y audio bidireccional.

5. **Tool / Function Calling**
   - **Módulo:** `models-general` y `models-beta`
   - **Responsabilidad:** Serialización tipada de esquemas JSON Schema para definición de herramientas (`ChatCompletionTool`) y parsing de llamadas de función (`ToolCall`).

---

## Flujo interno

```text
Entrada de Usuario / Audio Transcrito
                 │
                 ▼
       [AssistantGateway] (Interfaz agnóstica de Jarvis)
                 │
                 ▼
     [OpenAiAssistantGateway] (Implementación SDK)
                 │
                 ├─► ChatCompletionCreateParams (con definiciones de Tools)
                 │
                 ▼
          [OpenAIClientAsync] (OkHttp transport)
                 │ (SSE / WebSockets)
                 ▼
       [Stream de Respuesta / Tool Calls]
                 │
      ┌──────────┴──────────┐
      ▼                     ▼
[Delta de Texto/Voz]    [ToolCall("open_app", args)]
```

---

## Dependencias relevantes

- Módulos Maven:
  - `com.openai:openai-java-client-okhttp`
  - `com.openai:openai-java-core`
- `com.squareup.okhttp3:okhttp`
- `com.fasterxml.jackson.core:jackson-databind`

---

## Qué queremos rescatar

- **Integración oficial como librería en Fase 3 y 4:** `FUTURE_DEPENDENCY`
  - Utilizar el SDK oficial para hablar con OpenAI en lugar de mantener clientes HTTP manuales o propensos a errores.
- **Abstracción `AssistantGateway`:** `REIMPLEMENT`
  - En esta Fase 0, diseñar el contrato agnóstico `AssistantGateway` que desacople totalmente el Agente de OpenAI, permitiendo alternar entre OpenAI, modelos locales (Ollama/ONNX/GGUF) o proveedores alternativos en el futuro sin modificar la lógica del agente.

---

## Riesgos

- **Seguridad de API Keys:** NUNCA quemar la API key en el código fuente ni en el APK compilado. Debe ser provista por el usuario mediante configuración segura (`EncryptedSharedPreferences` / Android Keystore) o consumida a través de un proxy backend seguro.
- **Latencia de red:** En redes móviles inestables, las llamadas a la nube pueden experimentar demoras. Se requiere soporte robusto de streaming y timeouts configurables.
- **Costos de tokens:** El envío continuo de árboles de interfaz mediante visión o accesibilidad puede elevar el consumo de tokens si no se optimiza el contexto enviado.

---

## Decisión

**UTILIZAR (`FUTURE_DEPENDENCY`)**
Diseñar en esta fase la abstracción agnóstica `AssistantGateway` e incorporar la dependencia `openai-java` en la Fase 3 (Voz) y Fase 4 (Tools).
