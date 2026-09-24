# ADR 0001: Adopción de Kotlin Nativo y Jetpack Compose

## Contexto

El proyecto Jarvis Android busca operar de manera continua (24/7), integrarse profundamente con el sistema operativo Android (`VoiceInteractionService`, `AccessibilityService`, `NotificationListenerService`, `AudioRecord` en background) y comunicarse de forma óptima con subsistemas como Shizuku (Binder IPC) y Root (`libsu`). Frameworks multiplataforma como Flutter o React Native introducen capas adicionales de abstracción (puentes JNI/PInvoke, overhead de memoria, complejidad de sincronización con servicios de background de Android y problemas para gestionar ventanas de overlay tipo `TYPE_APPLICATION_OVERLAY`).

## Decisión

El proyecto será desarrollado **100% en Kotlin nativo**, utilizando la suite moderna de Android Jetpack (Corrutinas, Flow, Jetpack Compose para interfaces de usuario, Hilt/Koin para inyección de dependencias y Room para persistencia). Se descarta cualquier framework híbrido o multiplataforma para el código de producción.

## Consecuencias

### Positivas
- Acceso directo y sin fricción a todas las APIs de Android y servicios del sistema.
- Ciclo de vida predecible y eficiente para servicios foreground en segundo plano.
- Integración nativa con IPC Binder (Shizuku, libsu).
- Máximo rendimiento y menor huella de memoria RAM.

### Negativas
- El código está acoplado a la plataforma Android y no puede reutilizarse en iOS o Web (lo cual no es un objetivo del proyecto).
