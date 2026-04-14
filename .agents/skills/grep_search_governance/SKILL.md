---
name: Grep Search Mitigation Policy
description: Protocolo universal para mitigar errores de 'context canceled' y timeouts masivos al usar la herramienta de búsqueda en código.
version: 1.0.0
---

# 🔎 RGL-001: Política de Mitigación para `grep_search` (Timeout / Context Canceled)

## 📌 Contexto del Problema
Cuando los agentes intentan buscar variables, clases o textos a través del código usando la herramienta nativa `grep_search`, si se ejecuta sobre directorios demasiado amplios o sin filtros, el servidor de I/O sobrecarga la memoria y el hilo principal, resultando en un error **`context canceled`** (Timeout). Esto paraliza el análisis.

Para garantizar la fluidez cognitiva, **TODOS LOS AGENTES** (Backend, Frontend, QA, Arquitecto) están obligados a seguir estas 3 directrices al realizar búsquedas en el sistema de archivos:

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

---

## 🎯 Gatillo de Evaluación y Autocorrección
Cada vez que un Agente escriba un *Thought* (`<thought>`) considerando usar la habilidad o herramienta de buscar archivos en volumen, DEBE invocar mentalmente **RGL-001** antes de enviar el comando. Si la llamada falla, debe invocar la Regla 3 inmediatamente.
