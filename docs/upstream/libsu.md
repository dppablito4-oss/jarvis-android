# Auditoría Técnica: libsu

## Información

- **Repositorio:** `https://github.com/topjohnwu/libsu.git`
- **Commit:** `4910d8dcc1ea3273246614b356fba56e1ce002a5`
- **Licencia:** Apache License 2.0 (`Apache-2.0`)
- **Lenguaje:** Java
- **Build:** Gradle Kotlin DSL (`build.gradle.kts`)
- **Estado:** Activo, biblioteca de referencia creada por el autor de Magisk (topjohnwu) para operaciones seguras con privilegios de superusuario (root) en Android

---

## Componentes interesantes

1. **`com.topjohnwu.superuser.Shell`**
   - **Archivo:** `core/src/main/java/com/topjohnwu/superuser/Shell.java`
   - **Módulo:** `:core`
   - **Responsabilidad:** Gestión de la sesión principal de superusuario (`su`). Maneja pooling de shells, ejecución síncrona o asíncrona mediante Builders y despacha resultados en el `UiThreadHandler`.
   - `Shell.isAppGrantedRoot()`: Verificación segura del estado de root sin colgar la UI.

2. **`com.topjohnwu.superuser.ipc.RootService`**
   - **Archivo:** `service/src/main/java/com/topjohnwu/superuser/ipc/RootService.java`
   - **Módulo:** `:service`
   - **Responsabilidad:** La característica más potente de libsu. Permite levantar un servicio remoto desacoplado que corre directamente con privilegios de root (`UID 0`) y se comunica con la aplicación cliente mediante IPC Binder estándar (AIDL). Esto elimina la necesidad de pasar comandos en texto por tuberías de `su`.

3. **`SuFile`, `SuFileInputStream`, `SuFileOutputStream`**
   - **Archivos:** `io/src/main/java/com/topjohnwu/superuser/io/*`
   - **Módulo:** `:io`
   - **Responsabilidad:** Abstracciones tipo `java.io.File` para leer y escribir archivos en directorios protegidos por el sistema (`/data/data/...`, particiones de sistema) a través de canales I/O optimizados.

4. **`FileSystemManager` & `RemoteFileChannel`**
   - **Archivos:** `nio/src/main/java/com/topjohnwu/superuser/nio/*`
   - **Módulo:** `:nio`
   - **Responsabilidad:** Sistema de archivos avanzado basado en Java NIO que corre dentro del servicio root y transmite buffers de alto rendimiento al proceso de la app.

---

## Flujo interno

```text
Jarvis Android (Proceso No Privilegiado)
               │
      [RootDeviceBridge.isAvailable()]
               │ (Root disponible)
               ▼
     [RootService.bind(Intent, ServiceConnection)]
               │ (Daemon arrancado con UID 0 vía su)
               ▼
     [Binder IPC / AIDL Interface]
               │ (Comandos tipados y binarios, sin fork manual)
               ▼
     [RootServiceServer (UID 0)]
```

---

## Dependencias relevantes

- Módulos Maven:
  - `com.github.topjohnwu.libsu:core:5.2.1`
  - `com.github.topjohnwu.libsu:service:5.2.1`
  - `com.github.topjohnwu.libsu:io:5.2.1`

---

## Qué queremos rescatar

- **Arquitectura de `RootService` (Binder IPC):** `REIMPLEMENT` / `ADAPT`
  - Evitar por completo la mala práctica de invocar `Runtime.getRuntime().exec("su")` con strings de texto. Cuando se implemente root en la Fase 8, se usará `RootService` con contratos AIDL tipados.
- **Abstracción `RootDeviceBridge`:** `REIMPLEMENT`
  - Contrato limpio en `device/root/` que aísla la presencia o ausencia de root del resto de la arquitectura.

---

## Riesgos

- **Disponibilidad muy minoritaria:** La gran mayoría de los usuarios no tienen root en sus dispositivos de uso diario. El asistente debe funcionar de forma excelente sin root.
- **Riesgo crítico de seguridad y estabilidad:** Errores con UID 0 pueden brickear el sistema o comprometer datos privados del usuario. Por tanto, el agente NUNCA debe disponer de una herramienta general de shell (`su(cmd)`). Todas las acciones root deben ser semánticas y requerir confirmación explícita previa (`SafetyInterceptor`).

---

## Decisión

**UTILIZAR (`FUTURE_DEPENDENCY`)**
Diseñar el contrato `RootDeviceBridge` en esta fase e integrar la dependencia oficial `libsu` en la Fase 8 para operaciones avanzadas.
