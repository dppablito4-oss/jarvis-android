# ADR 0002: Arquitectura Independiente en Lugar de Fork de Upstream

## Contexto

Existen múltiples asistentes y proyectos de automatización en Android en código abierto, entre ellos Dicio Android, Hark, Open Jarvis y DeVA. Cada uno aborda un aspecto parcial (Dicio: wake-word/TTS; Hark: VoiceInteractionService/Flutter; Open Jarvis: accesibilidad; DeVA: coordinación de LLM y pantalla). Sin embargo:
1. Dicio utiliza licencia copyleft GPL-3.0.
2. DeVA utiliza una licencia restrictiva de uso personal/educativo.
3. Hark está fuertemente acoplado a Flutter en su capa visual.
4. Open Jarvis acopla la UI, el cliente LLM y la accesibilidad en una estructura monolítica.

Hacer un fork de cualquiera de ellos arrastraría deuda técnica, incompatibilidades de licencias y limitaciones de diseño.

## Decisión

Jarvis Android será un **repositorio independiente con arquitectura propia**. Los proyectos upstream se mantienen en `external/` exclusivamente como referencias técnicas de estudio, fuentes de diseño y guías de mejores prácticas. Ningún código con licencias incompatibles (GPL-3.0 o licencias propietarias/restrictivas) será copiado a la base de código principal.

## Consecuencias

### Positivas
- Total libertad y control sobre la arquitectura y el modelo de dominio.
- Cumplimiento estricto de licencias y propiedad intelectual limpia.
- Código limpio, moderno y sin arrastrar código obsoleto o acoplamientos indeseados de terceros.

### Negativas
- Requiere reimplementar desde cero ciertos componentes (como la canalización de inferencia TFLite para OpenWakeWord o la coordinación de voz).
