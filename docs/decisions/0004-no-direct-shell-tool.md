# ADR 0004: Prohibición de Herramientas de Shell Crudo Arbitrario para el Agente

## Contexto

Cuando un agente impulsado por LLM tiene acceso a herramientas de privilegios elevados (Shizuku o Root), surge la tentación de otorgarle una herramienta universal de texto libre, como `executeShell(command: String)` o `runSu(command: String)`.
Sin embargo, los modelos de lenguaje pueden hallucinar parámetros, malinterpretar instrucciones, ser vulnerables a inyecciones de prompt indirectas (ej. texto malicioso en una página web o mensaje recibido) o emitir comandos destructivos (ej. `rm -rf /data`, filtración de datos de usuario, alteración de particiones críticas).

## Decisión

El agente **NUNCA** dispondrá de herramientas generales de shell, adb o root crudo.
En su lugar:
1. Toda herramienta expuesta al agente será **semántica, fuertemente tipada y con parámetros validados** (ej. `openApp(packageName: String)`, `forceStopApp(packageName: String)`, `setVolume(stream: AudioStream, level: Int)`).
2. Cada herramienta declarará obligatoriamente:
   - `riskLevel: RiskLevel` (`LOW`, `MEDIUM`, `HIGH`, `CRITICAL`)
   - `requiresConfirmation: Boolean`
   - `requiredCapability: DeviceCapability`
3. Un `SafetyInterceptor` interceptará cualquier acción de riesgo alto o crítico antes de su ejecución, requiriendo validación explícita del usuario en el dispositivo.

## Consecuencias

### Positivas
- Prevención total de inyecciones de comandos shell arbitrarios.
- El usuario mantiene el control absoluto sobre acciones destructivas o de privacidad sensible.
- Modelo de seguridad auditable y robusto.

### Negativas
- Cada nueva acción de sistema debe modelarse formalmente como una herramienta Kotlin en lugar de permitir que el LLM improvise scripts shell.
