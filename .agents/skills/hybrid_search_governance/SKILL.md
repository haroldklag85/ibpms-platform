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
- **Herramienta:** `grep_search` + `view_file` CON LIMITADORES.
- **Objetivo:** Verificar que el Gherkin respalda la existencia de este código.

**REGLA DE VIDA O MUERTE (StartLine/EndLine):**
El archivo `v1_user_stories.md` (o cualquier PRD) es gigante. Tienes **ESTRICTAMENTE PROHIBIDO** invocar `view_file` sobre estos archivos sin pasar los parámetros `StartLine` y `EndLine`.
1.  Usa `grep_search` con `MatchPerLine: true` (o el equivalente para capturar el número de línea) para buscar la clave de la US (ej. `"US-023"`).
2.  Usa `view_file` enviando StartLine = [Línea donde inicia la US] y EndLine = [Línea + 150].

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
