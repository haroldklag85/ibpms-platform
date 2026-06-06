---
name: Grep Search Mitigation Policy
description: Protocolo universal para mitigar errores de 'context canceled' y timeouts masivos al usar la herramienta de búsqueda en código.
version: 1.0.0
---

# 🔎 RGL-001: Política de Mitigación para `grep_search` (Timeout / Context Canceled)

## 📌 Contexto del Problema
Cuando los agentes intentan buscar variables, clases o textos a través del código usando la herramienta nativa `grep_search`, si se ejecuta sobre directorios demasiado amplios o sin filtros, el servidor de I/O sobrecarga la memoria y el hilo principal, resultando en un error **`context canceled`** (Timeout). Esto paraliza el análisis.

Para garantizar la fluidez cognitiva, **TODOS LOS AGENTES** (Backend, Frontend, QA, Arquitecto) están obligados a seguir estas directrices al realizar búsquedas en el sistema de archivos:

---

### 0️⃣ REGLA DE DOCUMENTACIÓN: `grep_search` DEPRECADO para archivos `.md` de Requerimientos

> [!CAUTION]
> **El motor `grep_search` falla sistémicamente sobre archivos `.md` en `docs/requirements/`** (incluso en archivos de 7 KB). La causa raíz es incompatibilidad del motor ripgrep interno con encoding CRLF + UTF-8 extendido (tildes, ñ, emojis). Este fallo fue verificado empíricamente el 2026-04-14.

**Protocolo obligatorio para consultar User Stories y Criterios de Aceptación:**

1. **Leer el índice:** `docs/requirements/v1_user_stories_index.md` → identificar el archivo de Épica.
2. **Leer el archivo de Épica:** `docs/requirements/epics/epic_X_*.md` con `view_file` (paginado si > 800 líneas).
3. **Búsqueda puntual (si es necesario):** Usar **PowerShell como PRIMERA opción** (no como fallback):
   ```powershell
   Select-String -Path "docs\requirements\epics\epic_A_motor_core.md" -Pattern "CA-28" | Select-Object LineNumber, Line
   ```
4. **Lookup programático (opcional):** Leer `docs/requirements/v1_user_stories_registry.json` para obtener el campo `file` de la US objetivo.

**PROHIBIDO:** Usar `grep_search` sobre archivos en `docs/requirements/`. **PROHIBIDO:** Leer `docs/requirements/v1_user_stories.md` (monolito deprecado, excluido del RAG).

---

### 1️⃣ REGLA DE PROFUNDIDAD: Prohibido Buscar desde la Raíz (Root-Scanning)
Nunca establezcas el parámetro `SearchPath` en la raíz del proyecto (ej. `ibpms-platform/`).
Debes "bajar" lógicamente en la taxonomía de carpetas hasta donde intuitivamente esté el componente.
* ❌ **INCORRECTO:** `SearchPath: C:\...\ibpms-platform\`
* ✅ **BACKEND CORRECTO:** `SearchPath: C:\...\ibpms-platform\backend\ibpms-core\src\main\java\com\ibpms\poc\`
* ✅ **FRONTEND CORRECTO:** `SearchPath: C:\...\ibpms-platform\frontend\src\components\`

### 2️⃣ REGLA DE FILTRADO: Uso Estricto y Obligatorio del parámetro `Includes`
El parámetro `Includes` **NO es opcional**. Todo Agente que invoque un motor de búsqueda debe proveer el arreglo explícito de extensiones o Wildcards de los archivos que desea parsear. Si no haces esto, el agente buscará dentro de binarios `.class`, dentro de `.git`, o imágenes `.png`.
* ❌ **INCORRECTO:** `Includes: []` o parámetro omitido.
* ✅ **BACKEND CORRECTO:** `Includes: ["*.java", "*.yml", "*.xml"]`
* ✅ **FRONTEND CORRECTO:** `Includes: ["*.vue", "*.ts", "*.json"]`

### 3️⃣ PLAN DE CONTINGENCIA: Shell Fallback Protocol
Si ejecutas `grep_search` respetando la Regla 1 y la Regla 2, pero a la primera o segunda ejecución recibes el error `Encountered error in step execution: context canceled`, **TIENES PROHIBIDO SEGUIR INTENTANDO CON LA MISMA HERRAMIENTA**.
Inmediatamente aplica el "Contingency Plan" usando la herramienta de terminal (`run_command`) y delegando la búsqueda al sistema operativo nativo (Powershell/CMD):

**Alternativa CMD:**
```batch
cmd.exe /c "findstr /s /m /i "TerminoDeBusqueda" C:\Ruta\Especifica\*.java"
```

**Alternativa PowerShell:**
```powershell
Select-String -Path "C:\Ruta\Especifica\*.ts" -Pattern "TerminoDeBusqueda" -Recurse -List | Select-Object Path
```

### 4️⃣ NAVEGACIÓN POR TAXONOMÍA: Árbol de Directorios (`list_dir` + `view_file`)
Si tanto `grep_search` como los shell fallbacks (Regla 3) siguen fallando, el Agente DEBE abandonar toda estrategia de búsqueda masiva y pasar a **navegación manual por taxonomía**:

**Para archivos de código (Java/Vue/TS):**
1. Usa `list_dir` para descender nivel por nivel desde el módulo sospechoso (ej. `backend/ibpms-core/src/main/java/com/ibpms/poc/domain/`).
2. Identifica visualmente el archivo candidato por su nombre semántico.
3. Usa `view_file` para leer su contenido y confirmar la presencia del término buscado.

**Para archivos de requerimientos (`.md` en `docs/requirements/`):**
1. Lee `docs/requirements/v1_user_stories_index.md` → identifica la Épica.
2. Lee `docs/requirements/epics/epic_X_*.md` con `view_file` paginado.
3. Este es el protocolo **PRIMARIO** para documentación (no un último recurso). Ver Regla 0.

**¿Cuándo preferir esta regla sobre la Regla 3?**
* Cuando el agente necesita **contexto estructural** (entender la forma del árbol, no solo encontrar un string).
* Cuando los comandos shell también arrojan errores de codificación o permisos en carpetas profundas.
* Como **protocolo de último recurso absoluto**: esta vía SIEMPRE funciona porque no depende de indexación ni de procesos de I/O masivo.

> ⚠️ **Advertencia:** Esta estrategia es más lenta (requiere múltiples pasos secuenciales). Úsala solo cuando las Reglas 1-3 hayan sido agotadas.

---

## 🎯 Gatillo de Evaluación y Autocorrección
Cada vez que un Agente escriba un *Thought* (`<thought>`) considerando usar la habilidad o herramienta de buscar archivos en volumen, DEBE invocar mentalmente **RGL-001** antes de enviar el comando. Si la llamada falla, debe invocar la Regla 3 inmediatamente.
