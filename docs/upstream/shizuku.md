# Auditoría Técnica: Shizuku API

## Información

- **Repositorio:** `https://github.com/RikkaApps/Shizuku-API.git`
- **Commit:** `a27f6e4151ba7b39965ca47edb2bf0aeed7102e5`
- **Licencia:** MIT License (`MIT`)
- **Lenguaje:** Java / AIDL
- **Build:** Gradle Groovy DSL (`build.gradle`)
- **Estado:** Activo, estándar en el ecosistema Android para operaciones privilegiadas con permisos de nivel `adb` (shell UID 2000)

---

## Componentes interesantes

1. **`rikka.shizuku.Shizuku`**
   - **Archivo:** `api/src/main/java/rikka/shizuku/Shizuku.java`
   - **Módulo:** `:api`
   - **Responsabilidad:** Fachada principal del cliente. Métodos clave:
     - `pingBinder()`: Verifica si el servicio Shizuku está en ejecución.
     - `checkSelfPermission()` y `requestPermission()`: Ciclo de vida de permisos.
     - `addBinderReceivedListener()` y `addBinderDeadListener()`: Detección asíncrona de conexión y desconexión con el servidor Shizuku.
     - `newProcess(String[] cmd, String[] env, String dir)`: Ejecución de procesos remotos con identidad shell (UID 2000).

2. **`ShizukuBinderWrapper` & `SystemServiceHelper`**
   - **Archivos:** `api/src/main/java/rikka/shizuku/ShizukuBinderWrapper.java` y `SystemServiceHelper.java`
   - **Responsabilidad:** Permite invocar servicios del sistema (`IPackageManager`, `IActivityManager`, `IWindowManager`) pasando la identidad de Shizuku a través de IPC Binder.

3. **`IShizukuService.aidl`**
   - **Archivo:** `aidl/src/main/aidl/moe/shizuku/server/IShizukuService.aidl`
   - **Responsabilidad:** Definición formal de la interfaz IPC Binder entre el proceso cliente de la app y el proceso servidor de Shizuku.

4. **`UserService` (Demo)**
   - **Archivo:** `demo/src/main/java/rikka/shizuku/demo/service/UserService.java`
   - **Responsabilidad:** Demostración de arquitectura de servicio desacoplado ejecutándose dentro del proceso privilegiado mediante `Shizuku.UserServiceArgs`.

---

## Flujo interno

```text
Jarvis (App Process UID 10xxx)
              │
    [Shizuku.pingBinder()] ──(False)──► Fallback a permisos normales
              │ (True)
              ▼
    [Shizuku.checkSelfPermission()]
              │ (Granted)
              ▼
    [Privileged Operation] (ej. am force-stop, pm disable, appops)
              │ (Binder IPC)
              ▼
    [Shizuku Server Process] (UID 2000 / Shell)
              │
              ▼
    Android Framework Services (system_server)
```

---

## Dependencias relevantes

- Módulos Maven: `dev.rikka.shizuku:api:13.1.5` y `dev.rikka.shizuku:provider:13.1.5`

---

## Qué queremos rescatar

- **Integración oficial como librería en Fase 7:** `DEPENDENCY`
  - En la Fase 7, añadir las dependencias Maven de Shizuku API.
- **Abstracción `ShizukuDeviceBridge`:** `REIMPLEMENT`
  - En esta Fase 0, diseñar el contrato `ShizukuDeviceBridge` que oculte los detalles de Binder y permita verificar si Shizuku está activo sin acoplar el resto del agente a la librería.
- **Manejo de ciclo de vida del Binder:** `ADAPT`
  - Detección de pérdida de conexión y reconexión elegante.

---

## Riesgos

- **Disponibilidad:** El usuario debe tener Shizuku instalado y activado previamente (vía depuración inalámbrica o adb por PC). No se puede garantizar que esté siempre disponible.
- **Desconexión en caliente:** Si el usuario reinicia el dispositivo sin depuración inalámbrica automática, Shizuku deja de estar activo. Nuestra arquitectura debe soportar fallback transparente sin crashear.
- **Seguridad:** El agente NO debe exponer una herramienta de texto libre `run_shizuku_command("...")`. Toda operación privilegiada debe ser semántica y tipada (ej. `forceStopPackage(pkg)`).

---

## Decisión

**UTILIZAR (`FUTURE_DEPENDENCY`)**
Preparar la interfaz `ShizukuDeviceBridge` e incorporar la dependencia oficial en la Fase 7.
