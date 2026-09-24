# Registro de Procedencia y Trazabilidad (Provenance)

Este documento registra la procedencia legal y técnica de cualquier concepto, estructura, especificación o componente adaptado de los repositorios externos durante el desarrollo de **Jarvis Android**.

---

## Política de Licencias y Propiedad Intelectual

1. **Código con Licencia Restrictiva / Copyleft Estricto:**
   - **Dicio Android (GPL-3.0):** No se copia ni portará código fuente literal al núcleo de Jarvis Android para evitar el contagio copyleft. Se utiliza estrictamente como especificación de comportamiento y referencia conceptual (`REFERENCE_ONLY_GPL`).
   - **DeVA (Personal Use License):** No se copia ni transfiere ningún archivo, clase o fragmento. Se utiliza estrictamente para estudio de arquitectura y flujo (`REFERENCE_ONLY_LICENSE_RESTRICTED`).

2. **Código Permisivo (Apache-2.0 / MIT):**
   - Cualquier fragmento o contrato adaptado de repositorios con licencias permisivas (Hark, Open Jarvis, openWakeWord, Shizuku, libsu, openai-java) mantendrá sus respectivos avisos de copyright y atribución según lo estipulado por sus licencias.

---

## Registro de Componentes y Adaptaciones

### 1. Descriptor de Asistente de Voz (`voice_interaction_service.xml`)
- **Origen:** Repositorio Hark (`android/app/src/main/res/xml/voice_interaction_service.xml`)
- **URL:** `https://github.com/OpenAppCapabilityProtocol/hark`
- **Commit de referencia:** `f5c502130c20935e9e9c5b1970e2a8410d0b96fc`
- **Licencia:** Apache License 2.0
- **Modificaciones realizadas:** Adaptación de las referencias de paquetes y clases para apuntar al namespace propio `com.jarvis.assistant.assistant.system.JarvisSessionService` y `JarvisRecognitionService`.

### 2. Contratos de Accesibilidad y Lectura de UI
- **Origen:** Open Jarvis (`app/src/main/java/com/openjarvis/accessibility/`)
- **URL:** `https://github.com/tokenarc/open-jarvis`
- **Commit de referencia:** `04873998933830ce582018fe4664ebce5ec64d2f`
- **Licencia:** MIT License
- **Modificaciones realizadas:** Desacoplamiento de la lógica de interfaz concreta a contratos e interfaces Kotlin puras (`DeviceAutomation`, `ScreenNode`, `ActionResult`) dentro de `com.jarvis.assistant.device.api` y `com.jarvis.assistant.automation.accessibility`.

### 3. Modelo de Prevención y Seguridad de Acciones Riesgosas
- **Origen:** Open Jarvis (`RiskyActionConfirmation.kt`)
- **URL:** `https://github.com/tokenarc/open-jarvis`
- **Commit de referencia:** `04873998933830ce582018fe4664ebce5ec64d2f`
- **Licencia:** MIT License
- **Modificaciones realizadas:** Rediseño completo en `com.jarvis.assistant.agent.safety` definiendo los enums `RiskLevel`, interfaces `SafetyPolicy` y `SafetyInterceptor` para evaluar confirmación antes del despacho de herramientas.

### 4. Normalización y Parámetros de Audio de OpenWakeWord
- **Origen:** openWakeWord (`openwakeword/model.py`)
- **URL:** `https://github.com/dscripka/openWakeWord`
- **Commit de referencia:** `368c03716d1e92591906a84949bc477f3a834455`
- **Licencia:** Apache License 2.0
- **Modificaciones realizadas:** Adopción de las especificaciones de entrada acústica (16 kHz, 16-bit mono, ventana de 1280 muestras = 80 ms, mel bins = 32, ventana de embeddings = 76x32) para las interfaces del procesador de audio en `com.jarvis.assistant.assistant.wakeword`.
