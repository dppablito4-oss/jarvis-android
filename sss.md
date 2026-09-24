# PROMPT MAESTRO PARA CODEX
## JARVIS Android — Fase 0: clonado, auditoría, rescate y organización de repositorios

> **Objetivo de esta fase:** preparar un repositorio Android nativo propio para un asistente tipo “Jarvis”, clonar los proyectos de referencia, estudiar sus componentes, identificar qué funcionalidad aporta cada uno y ordenar esas piezas dentro de nuestra arquitectura **sin intentar integrarlas funcionalmente todavía**.
>
> Esta fase NO busca tener el asistente funcionando. NO busques completar voz, wake word, OpenAI, Accessibility, Shizuku o root. Solo queremos dejar una base limpia, documentada, trazable y preparada para las siguientes fases.

---

# 1. CONTEXTO DEL PROYECTO

Estamos construyendo un asistente Android nativo, escrito principalmente en **Kotlin**, pensado para funcionar como un asistente 24/7 similar conceptualmente a Jarvis.

La aplicación deberá evolucionar posteriormente hacia estas capacidades:

- convertirse en asistente predeterminado de Android;
- activarse por palabra clave;
- conversar mediante OpenAI;
- ejecutar herramientas mediante function/tool calling;
- abrir y controlar aplicaciones;
- inspeccionar interfaces mediante Accessibility;
- leer notificaciones;
- ejecutar automatizaciones;
- disponer de memoria local;
- utilizar Shizuku cuando los permisos Android normales no sean suficientes;
- soportar root en una fase avanzada;
- mantener una arquitectura desacoplada para que ningún proveedor, framework o método de privilegios sea obligatorio.

Sin embargo, **en esta tarea no debes implementar todavía esas funciones**.

Tu trabajo es preparar correctamente el terreno.

---

# 2. PRINCIPIO FUNDAMENTAL

NO conviertas nuestro proyecto en un fork de ninguno de los proyectos estudiados.

Nuestro repositorio será independiente.

Los repositorios externos deben tratarse como:

1. referencias técnicas;
2. fuentes de ideas arquitectónicas;
3. dependencias potenciales;
4. fuentes de componentes reutilizables solamente cuando su licencia lo permita.

Debes mantener claramente separado:

```text
NUESTRO CÓDIGO
vs.
CÓDIGO UPSTREAM / REFERENCIA
```

No mezcles archivos externos directamente con `app/src/main/...` durante esta fase.

---

# 3. REPOSITORIOS A CLONAR

Clona exactamente estos proyectos como fuentes de referencia.

## 3.1 Dicio

Repositorio:

```text
https://github.com/DicioTeam/dicio-android.git
```

Objetivo de estudio:

- captura de audio;
- ciclo de vida del micrófono;
- wake word;
- integración con OpenWakeWord;
- importación de modelos `.tflite`;
- STT;
- TTS;
- arquitectura de skills;
- acciones Android;
- notificaciones;
- multimedia;
- linterna;
- Compose/MVVM/Hilt/DataStore si resultan útiles.

IMPORTANTE:

Dicio utiliza GPL-3.0.

Por ahora:

- NO copiar código de Dicio al código principal;
- NO copiar clases completas;
- NO portar implementaciones literalmente;
- usarlo como referencia de comportamiento y arquitectura;
- documentar las clases relevantes y cómo funcionan.

Etiqueta interna:

```text
REFERENCE_ONLY_GPL
```

---

## 3.2 OpenWakeWord

Repositorio:

```text
https://github.com/dscripka/openWakeWord.git
```

Objetivo de estudio:

- arquitectura del detector de wake word;
- preprocesamiento de audio;
- mel-spectrogram;
- feature embeddings;
- clasificador;
- entrenamiento de palabras personalizadas;
- exportación de modelos;
- uso de TFLite / ONNX;
- herramientas de entrenamiento;
- ejemplos de streaming de audio.

Queremos utilizarlo posteriormente para crear nuestro propio modelo:

```text
jarvis.tflite
```

o equivalente.

En esta fase:

- no entrenar todavía el modelo;
- no integrar Python dentro de Android;
- identificar scripts, notebooks, modelos y documentación necesarios;
- crear una nota de cómo sería el pipeline futuro de entrenamiento.

---

## 3.3 Hark

Repositorio:

```text
https://github.com/OpenAppCapabilityProtocol/hark.git
```

Objetivo de estudio prioritario:

- `ROLE_ASSISTANT`;
- `VoiceInteractionService`;
- configuración del asistente Android;
- activación mediante gesto del asistente;
- lifecycle del asistente;
- overlay;
- ciclo wake-word → activación;
- gestión de audio;
- integración Android de inferencia local;
- cualquier uso de ONNX/TFLite;
- manifiesto Android relevante;
- permisos;
- services;
- receivers;
- bindings.

No queremos adoptar su UI si no es nativa Kotlin/Compose.

Extraer principalmente conocimiento del lado Android.

Etiqueta:

```text
REFERENCE_ASSISTANT_FRAMEWORK
```

---

## 3.4 Open Jarvis

Repositorio:

```text
https://github.com/tokenarc/open-jarvis.git
```

Objetivo de estudio prioritario:

- `AccessibilityService`;
- lectura del árbol UI;
- representación del estado de pantalla;
- `ScreenReader`;
- ejecución de acciones;
- tap;
- type;
- scroll;
- back;
- home;
- Agent Loop;
- ActionPlan;
- ActionExecutor;
- memoria Room;
- repository pattern;
- historial;
- skills;
- tool routing;
- overlay;
- comunicación entre agente y automatización.

Localizar especialmente clases equivalentes a:

```text
AgentCore
ActionPlan
ActionExecutor
JarvisAccessibilityService
ScreenReader
GraphifyDB
GraphifyRepository
AnalysisEngine
```

No asumir que su arquitectura será la nuestra.

Debes mapear esas piezas hacia nuestras interfaces.

---

## 3.5 DeVA

Repositorio:

```text
https://github.com/Devanshupardeshi/DeVA.git
```

Objetivo de estudio:

- `ConversationalAgentService`;
- `AgentService`;
- componente `Eyes`;
- `SpeechCoordinator`;
- coordinación voz → agente → UI;
- ejecución de tareas multi-step;
- estrategia de Accessibility;
- arquitectura de agentes.

IMPORTANTE:

Su licencia actual está orientada a uso personal/educativo y restringe otros usos.

Por tanto:

- NO copiar código de DeVA;
- NO trasladar clases;
- NO incorporar sus archivos;
- estudiar únicamente diseño, flujo y separación de responsabilidades.

Etiqueta:

```text
REFERENCE_ONLY_LICENSE_RESTRICTED
```

---

## 3.6 Shizuku API

Repositorio:

```text
https://github.com/RikkaApps/Shizuku-API.git
```

Objetivo de estudio:

- API de Shizuku;
- Binder lifecycle;
- permisos;
- provider;
- demo;
- conexión;
- identidad shell;
- identidad root mediante Sui;
- patrones para ejecutar operaciones privilegiadas;
- manejo de desconexión;
- manejo de ausencia de Shizuku.

En esta fase:

- NO añadir Shizuku al producto final;
- NO pedir permisos;
- NO ejecutar acciones privilegiadas;
- preparar únicamente nuestra abstracción `PrivilegedBridge`.

---

## 3.7 libsu

Repositorio:

```text
https://github.com/topjohnwu/libsu.git
```

Objetivo de estudio:

- root shell;
- `RootService`;
- Binder/IPC;
- ejecución segura de operaciones root;
- lifecycle;
- gestión de errores;
- acceso privilegiado a archivos;
- módulos `core`, `service` y `nio`.

En esta fase:

- NO ejecutar `su`;
- NO requerir root;
- NO añadir comandos shell;
- preparar solamente la futura interfaz `RootDeviceBridge`.

---

## 3.8 OpenAI Java

Repositorio:

```text
https://github.com/openai/openai-java.git
```

Objetivo de estudio:

- Responses API;
- streaming;
- structured outputs si aplica;
- function/tool calling;
- manejo de errores;
- clientes OkHttp;
- ejemplos;
- modelos de request/response;
- soporte Realtime disponible;
- integración Java/Kotlin.

En esta fase:

- NO conectar todavía ninguna API key;
- NO almacenar secretos;
- NO implementar llamadas reales;
- NO crear backend todavía;
- identificar únicamente qué paquetes/clases usaremos posteriormente.

Etiqueta:

```text
FUTURE_DEPENDENCY
```

---

# 4. ESTRUCTURA DE DIRECTORIOS PARA LOS REPOS EXTERNOS

Dentro de nuestro repositorio crea:

```text
external/
├── README.md
├── manifest.md
├── dicio-android/
├── openWakeWord/
├── hark/
├── open-jarvis/
├── DeVA/
├── Shizuku-API/
├── libsu/
└── openai-java/
```

Pero hay una regla importante:

Los clones NO deben convertirse accidentalmente en contenido versionado del repositorio principal.

Preferencia:

```text
external/*
```

debe añadirse al `.gitignore`.

Excepto:

```text
external/README.md
external/manifest.md
```

Es decir, conserva documentación propia, pero no subas miles de archivos upstream a nuestro Git.

Añade algo equivalente a:

```gitignore
external/*
!external/README.md
!external/manifest.md
```

Asegúrate de que funciona correctamente.

---

# 5. CLONADO

Clona todos los repositorios.

Puedes utilizar inicialmente:

```bash
git clone --depth 1 <repo>
```

Si para analizar historial, tags o ramas fuera necesario, convierte selectivamente un clon en completo posteriormente.

Después de cada clon registra en:

```text
external/manifest.md
```

lo siguiente:

```text
Nombre
URL
branch detectada
commit HEAD
fecha de inspección
licencia detectada
lenguaje principal
build system
módulos importantes
uso previsto
clasificación:
- DEPENDENCY
- REFERENCE
- REFERENCE_ONLY
- FUTURE_DEPENDENCY
```

NO inventes licencias.

Léelas directamente desde:

```text
LICENSE
LICENSE.md
COPYING
README
```

según corresponda.

---

# 6. NUESTRO REPOSITORIO

Nuestro proyecto debe quedar organizado como una aplicación Android nativa.

Si el repositorio actual ya contiene código:

**NO LO BORRES.**

Primero inspecciónalo.

Si todavía está vacío, crea únicamente la estructura mínima necesaria.

Objetivo arquitectónico futuro:

```text
jarvis-android/
│
├── app/
│
├── core/
│   ├── common/
│   ├── model/
│   ├── database/
│   ├── network/
│   ├── security/
│   └── logging/
│
├── assistant/
│   ├── system/
│   ├── voice/
│   ├── wakeword/
│   ├── speech/
│   └── realtime/
│
├── agent/
│   ├── core/
│   ├── planner/
│   ├── tools/
│   ├── context/
│   └── safety/
│
├── automation/
│   ├── accessibility/
│   ├── notifications/
│   ├── intents/
│   ├── events/
│   └── scheduler/
│
├── device/
│   ├── api/
│   ├── android/
│   ├── accessibility/
│   ├── shizuku/
│   └── root/
│
├── memory/
│   ├── conversation/
│   ├── events/
│   ├── preferences/
│   └── semantic/
│
├── docs/
│   ├── architecture/
│   ├── upstream/
│   ├── decisions/
│   └── research/
│
└── external/
```

No es obligatorio convertir cada carpeta en un módulo Gradle todavía.

Primero decide qué merece realmente ser módulo.

Evita sobrearquitectura innecesaria.

---

# 7. ARQUITECTURA BASE QUE DEBES RESPETAR

El flujo conceptual será:

```text
WakeWord
   │
   ▼
Assistant Session
   │
   ▼
Agent
   │
   ▼
ToolRouter
   │
   ├── Android API
   ├── Accessibility
   ├── Notifications
   ├── Intents
   ├── Shizuku
   └── Root
```

OpenAI NO debe conocer directamente ninguna implementación Android.

Accessibility NO debe conocer OpenAI.

Shizuku NO debe conocer OpenAI.

Root NO debe conocer OpenAI.

Todo debe comunicarse mediante abstracciones internas.

---

# 8. INTERFACES QUE DEBEN EXISTIR COMO STUBS

Puedes crear interfaces vacías o con contratos mínimos.

NO implementarlas aún.

## DeviceBridge

```kotlin
interface DeviceBridge {
    suspend fun isAvailable(): Boolean
}
```

## DeviceAutomation

Proponer contratos para:

```text
home
back
recentApps
openApp
inspectScreen
click
typeText
scroll
```

Sin implementación real.

## PrivilegedBridge

Debe representar capacidades privilegiadas futuras.

Ejemplos conceptuales:

```text
forceStopApp
changeProtectedSetting
executePrivilegedOperation
```

NO incluir:

```text
executeArbitraryShell(command)
```

como herramienta pública del agente.

## RootDeviceBridge

Crear únicamente contrato/stub.

## ShizukuDeviceBridge

Crear únicamente contrato/stub.

## Tool

Crear una interfaz genérica para herramientas del agente.

## ToolRouter

Crear solamente estructura y contratos.

NO conectar LLM.

## AssistantGateway

Contrato para proveedor de IA.

Debe permitir que en el futuro existan:

```text
OpenAI
modelo local
otro proveedor
```

sin cambiar el agente.

---

# 9. MAPEO DE FUNCIONALIDADES UPSTREAM

Crea:

```text
docs/upstream/COMPONENT_MAP.md
```

Debe contener una tabla como:

| Capacidad Jarvis | Repo estudiado | Componente upstream | Uso | Acción |
|---|---|---|---|---|
| Wake word | Dicio | ... | Referencia | Reimplementar |
| Wake word model | OpenWakeWord | ... | Entrenamiento | Adaptar |
| Assistant role | Hark | ... | Referencia | Reimplementar |
| VoiceInteractionService | Hark | ... | Referencia | Reimplementar |
| Accessibility | Open Jarvis | ... | Base conceptual | Adaptar |
| Screen parser | Open Jarvis | ... | Base conceptual | Adaptar |
| Agent loop | DeVA/Open Jarvis | ... | Referencia | Rediseñar |
| Shizuku | Shizuku-API | demo/... | Futuro | Integrar luego |
| Root | libsu | RootService | Futuro | Integrar luego |
| OpenAI | openai-java | Responses | SDK | Integrar luego |

Completa nombres de archivos y clases reales después de inspeccionarlos.

---

# 10. AUDITORÍA DE CADA REPO

Para cada repositorio crea:

```text
docs/upstream/<repo>.md
```

Por ejemplo:

```text
docs/upstream/dicio.md
docs/upstream/openwakeword.md
docs/upstream/hark.md
docs/upstream/open-jarvis.md
docs/upstream/deva.md
docs/upstream/shizuku.md
docs/upstream/libsu.md
docs/upstream/openai-java.md
```

Cada documento debe tener:

## Información

```text
Repositorio:
Commit:
Licencia:
Lenguaje:
Build:
Estado:
```

## Componentes interesantes

Lista de:

- clase;
- archivo;
- módulo;
- responsabilidad.

## Flujo interno

Describe cómo funciona.

## Dependencias relevantes

Registrar únicamente las importantes.

## Qué queremos rescatar

Clasificar como:

```text
COPY_ALLOWED
ADAPT
REIMPLEMENT
REFERENCE_ONLY
DEPENDENCY
DO_NOT_USE
```

## Riesgos

- licencia;
- APIs privadas;
- APIs obsoletas;
- permisos;
- root;
- background restrictions;
- fabricante;
- consumo energético;
- seguridad.

## Decisión

Una conclusión corta:

```text
UTILIZAR
UTILIZAR PARCIALMENTE
SOLO REFERENCIA
DESCARTAR
```

Sin implementar nada todavía.

---

# 11. REGLAS DE LICENCIAS

Extremadamente importante.

ANTES de copiar cualquier archivo debes inspeccionar su licencia.

No copies código con licencia incompatible.

En esta fase:

### Dicio

```text
GPL-3.0
→ referencia
→ no copiar código al core
```

### DeVA

```text
licencia restrictiva
→ solo estudiar
→ no copiar
```

Para todos los demás:

- comprobar LICENSE actual;
- registrar licencia;
- conservar copyright;
- conservar NOTICE cuando corresponda;
- no asumir compatibilidad;
- no copiar automáticamente aunque sea open source.

Si reutilizas literalmente cualquier fragmento permitido por licencia:

1. registra archivo de origen;
2. URL;
3. commit;
4. licencia;
5. modificaciones.

Crear:

```text
docs/upstream/PROVENANCE.md
```

---

# 12. NO IMPLEMENTAR TODAVÍA

Esta instrucción es crítica.

NO hacer en esta fase:

- conectar OpenAI;
- configurar API keys;
- crear servidor;
- abrir WebSocket;
- usar Realtime;
- activar micrófono permanentemente;
- pedir `ROLE_ASSISTANT`;
- registrar funcionalmente `VoiceInteractionService`;
- habilitar Accessibility;
- controlar apps;
- crear gestos;
- leer WhatsApp;
- leer notificaciones;
- ejecutar Shizuku;
- ejecutar root;
- ejecutar shell;
- entrenar wake word;
- crear overlay funcional;
- desarrollar UI completa;
- optimizar batería;
- hacer hacks específicos de HONOR/MagicOS.

Solamente preparar el código y documentación.

---

# 13. NO COPIAR BASURA UPSTREAM

Al estudiar cada proyecto ignora, salvo que sea necesario:

```text
.idea/
build/
.gradle/
screenshots/
marketing/
fastlane/
CI no relevante
release assets
APK
binarios
claves
local.properties
API keys
Firebase secrets
telemetría ajena
branding
iconos
logos
```

NO importes nombres de paquetes de terceros en nuestra arquitectura.

Nuestro package namespace debe ser propio.

Si todavía no existe uno, usa temporalmente algo neutro como:

```text
com.jarvis.assistant
```

pero documenta que debe poder cambiarse.

---

# 14. SEGURIDAD DESDE EL DISEÑO

Preparar:

```text
agent/safety/
```

aunque inicialmente esté vacío.

El agente futuro NO debe disponer de:

```text
shell("...")
su("...")
adb("...")
```

como herramientas generales.

Las operaciones deberán ser semánticas y controladas:

```text
open_app
set_volume
go_home
force_stop_app
set_brightness
send_message
```

Y cada herramienta futura deberá declarar:

```text
riskLevel
requiresConfirmation
requiredCapability
requiredPermission
```

No implementes las acciones todavía.

Solo prepara el modelo de datos si encaja con la estructura.

---

# 15. CAPABILITY MODEL

Diseña un modelo preliminar para que podamos consultar:

```text
NORMAL_ANDROID
ACCESSIBILITY
NOTIFICATION_ACCESS
ASSISTANT_ROLE
SHIZUKU
ROOT
NETWORK
MICROPHONE
OVERLAY
```

Crear algo conceptualmente equivalente a:

```kotlin
enum class DeviceCapability
```

y:

```kotlin
interface CapabilityProvider
```

No conectar todavía permisos reales.

---

# 16. DOCUMENTACIÓN DE ARQUITECTURA

Crear:

```text
docs/architecture/OVERVIEW.md
```

Debe describir:

```text
Usuario
  ↓
Input
  ↓
Assistant Session
  ↓
Agent
  ↓
Planner
  ↓
ToolRouter
  ↓
Capability Resolver
  ↓
Device Bridge
```

Y la jerarquía:

```text
Android API
   ↓ fallback
Accessibility
   ↓ fallback
Shizuku
   ↓ fallback opcional
Root
```

Pero recuerda:

esto es una preferencia conceptual.

No todas las acciones necesariamente siguen el mismo orden.

El `CapabilityResolver` deberá decidir según cada acción.

---

# 17. REGISTRO DE DECISIONES

Crear:

```text
docs/decisions/
```

y al menos:

```text
0001-native-kotlin.md
0002-upstream-not-fork.md
0003-device-bridge-abstraction.md
0004-no-direct-shell-tool.md
0005-ai-provider-abstraction.md
```

Formato ADR simple:

```text
# Título

## Contexto

## Decisión

## Consecuencias
```

---

# 18. README PRINCIPAL

Actualizar o crear `README.md`.

Debe dejar claro que el proyecto está en:

```text
Phase 0 — Architecture & Upstream Research
```

Agregar:

## Vision

Asistente Android nativo modular.

## Current status

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

No afirmar que nada funciona si todavía no funciona.

---

# 19. ARCHIVO ROADMAP

Crear:

```text
ROADMAP.md
```

Con estas fases, SIN ejecutarlas:

```text
Phase 0
Repository + research + architecture

Phase 1
Android System Assistant

Phase 2
Wake Word

Phase 3
OpenAI Voice

Phase 4
Tool System

Phase 5
Accessibility

Phase 6
Memory + Events

Phase 7
Shizuku

Phase 8
Root

Phase 9
Autonomous routines
```

---

# 20. INFORME FINAL DE CODEX

Cuando termines NO continúes implementando.

Entrega un informe:

```text
docs/PHASE_0_REPORT.md
```

Debe incluir:

# Repositorios clonados

Estado de cada uno.

# Componentes encontrados

Los componentes más interesantes.

# Arquitectura creada

Directorios, módulos e interfaces.

# Código reutilizable

Qué puede potencialmente reutilizarse.

# Código que debemos reimplementar

Especialmente Dicio/DeVA o cualquier incompatibilidad.

# Dependencias futuras

Lista.

# Riesgos

Android, licencias, permisos, background, fabricantes, seguridad.

# Próximo paso recomendado

Debe ser únicamente:

```text
Phase 1 — implementar el Assistant base de Android.
```

NO comenzar Phase 1.

---

# 21. VALIDACIONES FINALES

Antes de terminar verifica:

```text
[ ] El repo principal sigue siendo compilable si ya lo era.
[ ] No se ha eliminado código existente.
[ ] external/ está ignorado por Git.
[ ] No se añadieron secretos.
[ ] No se añadieron API keys.
[ ] No se ejecutó root.
[ ] No se ejecutó Shizuku.
[ ] No se habilitó Accessibility.
[ ] No se conectó OpenAI.
[ ] Cada upstream tiene documentación.
[ ] Cada licencia está registrada.
[ ] Existe COMPONENT_MAP.md.
[ ] Existe PROVENANCE.md.
[ ] Existe ROADMAP.md.
[ ] Existe PHASE_0_REPORT.md.
[ ] La arquitectura separa Agent / Android / Accessibility / Shizuku / Root.
```

---

# 22. COMPORTAMIENTO ESPERADO DURANTE LA TAREA

Trabaja de forma autónoma.

No me preguntes por decisiones menores.

Si encuentras diferencias entre este documento y el estado actual de un repositorio externo:

1. usa el estado real actual del repositorio;
2. documenta la diferencia;
3. no inventes clases;
4. adapta el mapa de componentes.

Si una ruta o clase mencionada aquí ya no existe:

- encuentra su equivalente actual;
- registra el cambio.

Si un repositorio no compila:

- NO intentes repararlo en profundidad;
- documenta el problema;
- continúa analizando el código.

Si un repositorio no puede clonarse:

- registra el error exacto;
- continúa con los demás.

---

# 23. RESULTADO DESEADO

Al terminar quiero poder abrir nuestro repositorio y encontrar algo parecido a:

```text
jarvis-android/
├── app/
├── assistant/
├── agent/
├── automation/
├── core/
├── device/
├── memory/
│
├── docs/
│   ├── architecture/
│   ├── decisions/
│   ├── research/
│   ├── upstream/
│   └── PHASE_0_REPORT.md
│
├── external/
│   ├── README.md
│   └── manifest.md
│
├── README.md
├── ROADMAP.md
└── .gitignore
```

Y fuera del tracking Git, físicamente disponibles para estudio:

```text
external/dicio-android
external/openWakeWord
external/hark
external/open-jarvis
external/DeVA
external/Shizuku-API
external/libsu
external/openai-java
```

El resultado importante de esta fase NO es cantidad de código.

El resultado importante es que:

```text
cada pieza tenga un lugar,
cada upstream tenga una función,
cada licencia esté controlada,
y nuestra arquitectura siga siendo nuestra.
```

---

# 24. ORDEN DE EJECUCIÓN

Ejecuta exactamente en este orden:

```text
1. Inspeccionar el repositorio actual.
2. Crear/ajustar .gitignore.
3. Crear external/.
4. Clonar los 8 upstream.
5. Registrar commit/licencia/branch.
6. Auditar Dicio.
7. Auditar OpenWakeWord.
8. Auditar Hark.
9. Auditar Open Jarvis.
10. Auditar DeVA.
11. Auditar Shizuku.
12. Auditar libsu.
13. Auditar openai-java.
14. Construir COMPONENT_MAP.md.
15. Definir arquitectura local.
16. Crear contratos/stubs mínimos.
17. Crear ADRs.
18. Crear PROVENANCE.md.
19. Crear ROADMAP.md.
20. Crear PHASE_0_REPORT.md.
21. Verificar build del proyecto propio.
22. Ejecutar git status.
23. Detenerse.
```

NO continúes automáticamente con implementación.

---

# 25. GIT

No hagas push salvo que el entorno ya esté explícitamente configurado y autorizado para ello.

Sí puedes preparar commits locales coherentes.

Preferencia:

```text
chore: initialize Jarvis Android architecture

docs: add upstream repository research

chore: add device capability abstractions

docs: add phase 0 upstream component map
```

No mezclar todo en un único commit gigante si puede separarse limpiamente.

Al final muestra:

```bash
git status
git log --oneline -10
```

y resume qué cambió.

---

# FIN DEL PROMPT

Detente cuando la **Fase 0** esté completada.

No implementes Fase 1 hasta recibir una nueva orden.
