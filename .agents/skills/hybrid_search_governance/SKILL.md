---
name: hybrid_search_governance
description: >
  Skill obligatoria para todos los agentes (Arquitecto, Backend, Frontend, QA).
  Operacionaliza la LEY GLOBAL 0 (RAG-First Deep Context) definiendo un estricto protocolo 
  de "Cuádruple Check" (Conocimiento, Semántica, Estructura, Gobernanza) para blindar la 
  ventana de contexto, prevenir alucinaciones arquitectónicas y anclar el código al SSOT.
triggers:
  - "Cuando cualquier agente reciba un ticket de Sprint y se prepare para analizar o modificar código."
  - "Antes de proponer un refactoring, corrección de bug o implementación de User Story."
  - "Al detectar una discrepancia entre código existente y documentación de gobernanza."
  - "Cuando el humano reporte o sospeche una alucinación, pérdida de contexto, o cuando se deba crear arquitectura desde cero."
applies_to:
  - Arquitecto Líder
  - Backend
  - Frontend
  - QA / DevOps
---

# PROTOCOLO DE GOBERNANZA: MOTOR DE BÚSQUEDA HÍBRIDA EXTREMA (RAG ESTRUCTURADO)

> **Versión:** 2.1 (QA Integration) | **Última Actualización:** 2026-04-05
> **Ley Padre:** LEY GLOBAL 0 — RAG-First Deep Context (`.cursorrules`)

## 0. AVISO DE SISTEMA (DEFENSA DE LA VENTANA DE CONTEXTO)

El "Codebase Semantic Search" y las herramientas de lectura de este IDE pueden envenenar y colapsar tu memoria a corto plazo (ventana de contexto) si consultas demasiados archivos o archivos muy largos. Tienes **PROHIBIDO** confiar ciegamente en resultados abstractos y **PROHIBIDO** leer documentos masivos enteros. 

---

## 1. FLUJO DE TRABAJO OBLIGATORIO (THE "QUADRUPLE CHECK")

Para cada ticket de Sprint, ejecuta estas cuatro fases **en orden estricto** antes de escribir código:

### FASE 0: Auditoría de Conocimientos (El "CONTEXTO PASIVO")
- **Acción:** Revisa silenciosamente tu `<persistent_context>` (los Knowledge Items o KIs inyectados al inicio del chat).
- **Regla:** Si existe un resumen de KI directamente relacionado con tu módulo (ej. "iBPMS Development Governance", "Roles y Permisos"), **DEBES** leer sus archivos internos relevantes usando `view_file` antes de hacer búsquedas amplias. Respeta la curaduría humana pre-existente.

### FASE 1: Descubrimiento Semántico Vectorial (El "QUÉ")
- **Herramienta:** Codebase Semantic Search (`@codebase`).
- **Objetivo:** Identificar el área inicial de impacto.
- **Regla Anti-Amnesia:** **NUNCA** leas a profundidad más de los **3 principales resultados**. Si necesitas leer un 4to archivo, tu búsqueda es muy genérica. Refínela ("Componente login" -> "Componente login con inyección OIDC").

### FASE 2: Mapeo y Validación Estructural (El "CÓMO")
- **Herramienta:** `list_dir` + `grep_search`.
- **Objetivo:** Trazar límites modulares y dependencias sin leer el contenido interno falso/mockeado.

**Secuencia de Supervivencia Estructural:**
1.  **Límites de Dominio:** Primero usa `list_dir` en zonas raíz (`backend/` o `frontend/src/`) para entender en qué sub-carpetas lógicas se divide el proyecto y limitar tu búsqueda.
2.  **Grep Anti-Ruido:** Usa plantillas `grep_search`. Fija siempre el `SearchPath` a la carpeta `src/main/` o análogas de código de producción para ignorar tests y mocks.

**Patrones de Grep de Producción (Java/Vue):**

| Estructura Objetivo | Query `grep_search` | Target `SearchPath` Restringido |
|---------------------|----------------------|--------------------------------|
| **Puertos Java** | `"interface.*Port"` | `.../domain/ports/` (o base del Bounded Context) |
| **Adaptadores Java** | `"implements.*Port"` | `.../infrastructure/` (¡excluir `src/test/`!) |
| **Componente Vue (Props)** | `"defineProps"` | `frontend/src/components/` |
| **Componente Vue (Stores)**| `"use.*Store"` | `frontend/src/` |

### FASE 3: Auditoría de Gobernanza SSOT (El "QUIÉN" y "POR QUÉ")
- **Herramienta:** `view_file` + PowerShell `Select-String` (para documentación `.md`). `grep_search` solo para código fuente.
- **Objetivo:** Verificar que el Gherkin respalda la existencia de este código.

**PROTOCOLO DE ACCESO AL SSOT DE REQUERIMIENTOS (Obligatorio):**

> [!CAUTION]
> `grep_search` está **DEPRECADO** para archivos `.md` en `docs/requirements/` (ver `.agents/skills/grep_search_governance/SKILL.md` Regla 0). Usar PowerShell como primera opción.

1. **Navegar por Taxonomía:** Lee `docs/requirements/v1_user_stories_index.md` para identificar el archivo de Épica que contiene tu US.
2. **Leer la Épica:** Usa `view_file` sobre `docs/requirements/epics/epic_X_*.md` con `StartLine` y `EndLine` (máximo 150 líneas por bloque) si el archivo supera las 800 líneas.
3. **Búsqueda puntual (si es necesario):** Usa PowerShell:
   ```powershell
   Select-String -Path "docs\requirements\epics\epic_A_motor_core.md" -Pattern "US-001" | Select-Object LineNumber, Line
   ```
4. **PROHIBIDO:** Leer `docs/requirements/v1_user_stories.md` (monolito deprecado, excluido del RAG vía `.cursorignore`).

Si el código *contradice* el SSOT paginado, te detienes y lanzas alerta de LEY GLOBAL 0 explícita hacia el Humano.

---

## 2. REGLAS DE SEGURIDAD Y EMBARGO (ANTI-DESTRUCCIÓN)

### 2.1 MODO LECTURA PARA CAPAS FUNDACIONALES
Los agentes SÍ programan, pero las siguientes carpetas/archivos requieren **Aprobación de Diff**:
- `/domain/ports/` (Cualquier cambio a interfaces afecta n-adaptadores).
- `docs/requirements/*.md` (SSOT Contractual).
- `.cursorrules` (Constitución).

Puedes modificar `/domain/model`, `/infrastructure/`, o `frontend/src/` y hacer commits directos a tu propia rama `sprint-*/...`.

---

## 3. PROTOCOLO DE RESPUESTA Y AUTO-VERIFICACIÓN

Antes de proponer cualquier cambio (o hacer un git commit), debes haber concluido mentalmente o explícitamente:
- [x] Leí el Contexto Persistente (KIs).
- [x] Usé búsqueda semántica (limitada a top-3 heurístico).
- [x] Usé `list_dir` / `grep_search` apuntando a `src/main` (filtrando mocks/test noise).
- [x] Leí el SSOT con `view_file` usando OBLIGATORIAMENTE rangos de líneas (`StartLine`/`EndLine`).

Cualquier omisión deberá ser alertada verbalmente como una excepción justificada.
