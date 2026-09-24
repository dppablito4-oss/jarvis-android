# ADR 0005: Abstracción del Proveedor de Inteligencia Artificial (AssistantGateway)

## Contexto

El mercado de modelos de lenguaje (LLMs) y capacidades de voz evoluciona rápidamente. Aunque OpenAI es el proveedor de referencia actual (Responses API, Streaming, Realtime API), existen otros proveedores comerciales (Anthropic Claude, Google Gemini) y modelos locales optimizados para dispositivos móviles (ONNX Runtime GenAI, ExecuTorch, Ollama local / MediaPipe LLM Inference).
Acoplar el agente o el enrutador de herramientas directamente al SDK de OpenAI limitaría la portabilidad futura y dificultaría la ejecución en modo sin conexión (offline).

## Decisión

Definir un contrato agnóstico `AssistantGateway` en la capa de arquitectura del asistente.
El agente interactúa exclusivamente con `AssistantGateway`, enviando intenciones y recibiendo flujos de tokens, audio o llamadas de herramientas (`ToolCall`).
El SDK oficial `openai-java` se implementará como una concreción (`OpenAiAssistantGateway`), encapsulando las particularidades de red, serialización JSON y headers de OpenAI sin que el resto del sistema lo sepa. En el futuro, podrán agregarse implementaciones como `LocalLlmAssistantGateway` o `GeminiAssistantGateway` sin alterar una sola línea de la lógica del agente.

## Consecuencias

### Positivas
- Desacoplamiento total del proveedor de IA.
- Posibilidad de alternar dinámicamente entre OpenAI en la nube y modelos locales en el dispositivo cuando no haya conexión a Internet.
- Facilidad extrema para realizar tests unitarios con un `FakeAssistantGateway`.

### Negativas
- Necesidad de mantener una capa de traducción entre los modelos de datos internos de Jarvis y las estructuras específicas del SDK de OpenAI.
