---
name: hybrid_search_governance
description: >
  Skill obligatoria para todos los agentes constructores (Arquitecto, Backend, Frontend).
  Operacionaliza la LEY GLOBAL 0 (RAG-First Deep Context) definiendo un protocolo de búsqueda
  híbrida ("Triple Check") que obliga a validar contexto semántico, dependencias estructurales
  y alineación con gobernanza ANTES de proponer o escribir cualquier línea de código.
triggers:
  - "Cuando cualquier agente reciba un ticket de Sprint y se prepare para analizar o modificar código."
  - "Antes de proponer un refactoring, corrección de bug o implementación de User Story."
  - "Al detectar una discrepancia entre código existente y documentación de gobernanza."
applies_to:
  - Arquitecto Líder
  - Backend
  - Frontend
---

# PROTOCOLO DE GOBERNANZA: MOTOR DE BÚSQUEDA HÍBRIDA (SEMÁNTICA + ESTRUCTURAL)

> **Versión:** 1.0 | **Última Actualización:** 2026-04-04
> **Ley Padre:** LEY GLOBAL 0 — RAG-First Deep Context (`.cursorrules`)

## 0. AVISO DE SISTEMA (CONTEXTO CRÍTICO)

El "Codebase Semantic Search" de Antigravity es una herramienta de **similitud conceptual**, NO de lógica estructural. Para evitar alucinaciones en este proyecto de +120k LOC, el Agente tiene **PROHIBIDO** confiar exclusivamente en resultados semánticos. Se debe aplicar validación estructural (`grep_search`) en cada paso.

**Herramientas REALES disponibles en este IDE:**

| Herramienta | Tipo | Uso en este protocolo |
|-------------|------|----------------------|
| Codebase Semantic Search (`@codebase`) | Semántica (vectorial) | FASE 1: Descubrimiento conceptual |
| `grep_search` (ripgrep) | Estructural (textual/regex) | FASE 2: Validación de dependencias |
| `view_file` | Lectura directa | FASE 2 y 3: Inspección de archivos |
| `list_dir` | Navegación de árbol | FASE 2: Mapeo de arquitectura |

⚠️ **PROHIBIDO** invocar herramientas que no existan en este IDE (ej. `ast-grep`, `Knowledge RAG` como tool call, `tree-sitter`). Si necesitas una capacidad que no tienes, informa al Humano.

---

## 1. FLUJO DE TRABAJO OBLIGATORIO (THE "TRIPLE CHECK")

Para cada ticket de Sprint, el Agente debe ejecutar estas tres fases **en orden estricto** antes de escribir código:

### FASE 1: Descubrimiento Semántico (El "QUÉ")

- **Herramienta:** Codebase Semantic Search (`@codebase`).
- **Objetivo:** Identificar el área de impacto conceptual.
- **Instrucción:** Describe la intención en lenguaje natural (ej: "Lógica de cálculo de impuestos en el dominio de facturación", "Componente de login con autenticación OIDC").
- **Restricción de calidad:** Si la búsqueda semántica devuelve resultados que claramente no son relevantes al concepto buscado, **descártalos y refina tu consulta** antes de continuar. Si no encuentras resultados relevantes tras 2 intentos de reformulación, informa al Humano: *"Mi búsqueda semántica no encontró referencias suficientes sobre X, necesito que amplíes mi contexto."*
- **Salida esperada:** Lista de archivos candidatos que probablemente contengan o dependan de la funcionalidad objetivo.

### FASE 2: Validación Estructural (El "CÓMO")

- **Herramienta:** `grep_search` (ripgrep) + `view_file` + `list_dir`.
- **Objetivo:** Trazar la jerarquía real de la Arquitectura Hexagonal y confirmar dependencias.

**Leyes de Validación por Stack:**

#### JAVA (Spring Boot 3 — Arquitectura Hexagonal):
Si encuentras una **Interface** en `/domain/ports/`, busca OBLIGATORIAMENTE todas sus implementaciones:
```
grep_search: "implements {NombreDelPuerto}" en Includes: ["*.java"], SearchPath: infrastructure/adapters/
```
Si encuentras un **Caso de Uso** (`@Service`), valida que inyecta los puertos correctos:
```
grep_search: "@Service" en Includes: ["*.java"], SearchPath: application/
```
Si modificas una **Entidad** (`@Entity`), verifica su Repository y cualquier migración DDL:
```
grep_search: "{NombreDeLaEntidad}" en Includes: ["*.java"], SearchPath: infrastructure/
```

#### VUE 3 (Composition API — TypeScript):
Si encuentras un **componente**, rastrea sus dependencias:
```
grep_search: "defineProps" en el archivo del componente → identifica datos de entrada
grep_search: "defineEmits" en el archivo del componente → identifica eventos de salida
grep_search: "{NombreDelComponente}" en Includes: ["*.vue"] → identifica padres que lo usan
```
Si el componente consume un **Store Pinia**, valida la coherencia:
```
grep_search: "defineStore" con Includes: ["*.ts"], SearchPath: frontend/src/stores/
grep_search: "use{NombreDelStore}" en Includes: ["*.vue", "*.ts"] → identifica consumidores
```

#### CROSS-STACK (API Contracts):
Si el ticket involucra comunicación Frontend↔Backend, valida correspondencia:
```
grep_search: "{endpoint}" en Includes: ["*.java"] (Controller) → confirma URL, método HTTP, DTO
grep_search: "{endpoint}" en Includes: ["*.ts"] (apiClient) → confirma que el frontend consume el mismo contrato
```

- **Salida esperada:** Mapa de dependencias confirmado estructuralmente (no solo por similitud semántica).

### FASE 3: Auditoría de Gobernanza (El "QUIÉN")

- **Herramienta:** `view_file` + `grep_search` sobre `docs/requirements/`.
- **Objetivo:** Comparar el código encontrado con el SSOT documental del iBPMS.

**Secuencia obligatoria:**

1. **Leer la User Story:** Usa `grep_search` para encontrar la US del ticket en `docs/requirements/v1_user_stories.md`. Lee sus Criterios de Aceptación Gherkin completos con `view_file`.
2. **Verificar alcance MoSCoW:** Confirma en `docs/requirements/v1_moscow_scope_validation.md` que la feature está clasificada como MUST o SHOULD para V1. Si está en WON'T HAVE, **DETENTE** y alerta al Humano.
3. **Validar contra NFRs:** Si el cambio afecta configuración de BD, caché, autenticación, o tiempos de respuesta, consulta `docs/requirements/non_functional_requirements.md`.
4. **Detectar contradicciones:** Si el código existente **contradice** lo estipulado en el Gherkin, **aplica la LEY GLOBAL 0 punto 2**: detente, lanza una alerta detallando la contradicción, y pide instrucciones al Humano. Está **PROHIBIDO** adivinar cuál tiene razón.

- **Salida esperada:** Confirmación de que el cambio propuesto está respaldado por el SSOT o alerta de contradicción.

---

## 2. REGLAS DE SEGURIDAD Y EMBARGO (ANTI-DESTRUCCIÓN)

### 2.1 ARCHIVOS BAJO EMBARGO (MODO LECTURA FORZADO)
Los siguientes archivos y directorios están **blindados**. El agente NO puede usar `replace_content`, `write_to_file (Overwrite)`, ni `git restore` sobre ellos:

| Archivo/Directorio Embargado | Razón | Quién puede modificarlo |
|------------------------------|-------|------------------------|
| `/domain/ports/` | Interfaces core de la Arquitectura Hexagonal | Solo Arquitecto Líder con aprobación humana |
| `docs/requirements/v1_user_stories.md` | SSOT contractual (Gherkin) | Solo Product Owner o Humano |
| `docs/requirements/functional_requirements.md` | PRD maestro | Solo Product Owner o Humano |
| `.cursorrules` | Constitución Global | Solo Humano |

**Archivos NO embargados** (los agentes pueden crear/modificar en su rama):
- `/domain/model/` — Entidades (si lo exige la US)
- `/domain/events/` — Eventos de dominio (si lo exige la US)
- `/application/` — Casos de uso
- `/infrastructure/` — Adaptadores, repositorios
- `frontend/src/` — Componentes, stores, vistas

### 2.2 ESCRITURA DE CÓDIGO (ALINEADO CON §1 DE .CURSORRULES)
- El agente **SÍ puede escribir código** en su rama aislada (`sprint-*/...` o `agent/...`), siempre que haya completado el Triple Check.
- Para archivos **EMBARGADOS**, el agente debe generar un **Artifact de Propuesta (Diff)** y esperar aprobación del Arquitecto Líder o del Humano antes de aplicarlo.
- El Humano o Arquitecto Líder son los únicos con permiso de Merge hacia `main`, conforme a `agent_git_governance_policy.md`.

### 2.3 PROHIBICIÓN DE SCRIPTS DE LIMPIEZA AUTOMÁTICA
Queda terminantemente prohibido el uso de scripts automáticos de limpieza de código que no hayan sido auditados estructuralmente para validar que no borran:
- Decoradores de Spring Boot (`@Service`, `@Transactional`, `@Entity`, `@Repository`)
- Anotaciones de validación (`@Valid`, `@NotNull`, `@Size`)
- Decoradores Vue (`defineProps`, `defineEmits`, `defineExpose`)

---

## 3. PATRONES DE BÚSQUEDA ESTRUCTURAL (PLANTILLAS GREP_SEARCH)

Cuando el Agente necesite precisión estructural, debe usar estos patrones predefinidos con `grep_search`:

### Java (Backend)

| Búsqueda | Query (regex) | Includes | SearchPath |
|----------|---------------|----------|------------|
| Puertos (Interfaces) | `"interface.*Port"` | `["*.java"]` | `backend/**/domain/ports/` |
| Implementaciones de Puerto | `"implements.*{NombrePuerto}"` | `["*.java"]` | `backend/**/infrastructure/` |
| Casos de Uso | `"@Service"` | `["*.java"]` | `backend/**/application/` |
| Entidades JPA | `"@Entity"` | `["*.java"]` | `backend/**/domain/model/` |
| Controllers REST | `"@RestController"` | `["*.java"]` | `backend/**/infrastructure/adapters/in/` |
| Queries/Repositorios | `"extends.*Repository"` | `["*.java"]` | `backend/**/infrastructure/` |

### Vue 3 / TypeScript (Frontend)

| Búsqueda | Query (regex) | Includes | SearchPath |
|----------|---------------|----------|------------|
| Props de componente | `"defineProps"` | `["*.vue"]` | `frontend/src/` |
| Emits de componente | `"defineEmits"` | `["*.vue"]` | `frontend/src/` |
| Stores Pinia | `"defineStore"` | `["*.ts"]` | `frontend/src/stores/` |
| Consumidores de Store | `"use{NombreStore}"` | `["*.vue", "*.ts"]` | `frontend/src/` |
| Rutas del Router | `"path:"` | `["router*.ts"]` | `frontend/src/router/` |
| Cliente API | `"apiClient"` | `["*.ts"]` | `frontend/src/` |

### Cross-Stack

| Búsqueda | Query | Includes | SearchPath |
|----------|-------|----------|------------|
| Endpoint Backend | `"@GetMapping\|@PostMapping\|@PutMapping\|@DeleteMapping"` | `["*.java"]` | `backend/` |
| Endpoint Frontend | `"apiClient\.\(get\|post\|put\|delete\)"` | `["*.ts"]` | `frontend/src/` |

---

## 4. PROTOCOLO DE RESPUESTA DEL AGENTE

Antes de proponer cualquier cambio de código, el agente debe haber completado internamente este checklist:

- ☐ **FASE 1 completada:** He realizado búsqueda semántica para entender el concepto y su área de impacto.
- ☐ **FASE 2 completada:** He validado estructuralmente (via `grep_search`) que mi cambio no rompe dependencias en la cadena Puerto→Caso de Uso→Adaptador (Java) o Padre→Componente→Store (Vue).
- ☐ **FASE 3 completada:** He verificado contra el SSOT (`v1_user_stories.md`) que mi cambio está respaldado por un Criterio de Aceptación Gherkin vigente.

**Si omite alguna fase, debe justificar explícitamente por qué** (ej: "FASE 2 omitida porque el cambio es exclusivamente de documentación y no afecta código fuente").

**Si detecta una contradicción en cualquier fase:**
> 🚨 *"ALERTA LEY 0: He detectado una contradicción entre [fuente A] y [fuente B]. Detalles: [explicación]. Me detengo y solicito instrucciones del Humano antes de continuar."*

---

## 5. EXCEPCIONES Y LÍMITES

Esta Skill **NO aplica** en los siguientes escenarios:
- Cambios exclusivos de documentación (`.md`) que no afectan código fuente.
- Correcciones tipográficas o de formato en comentarios de código.
- Ejecución de workflows operativos de auditoría (`/analisisEcoGobernanza`, `/auditoriaIntegralUs`, etc.).

En estos casos, el agente puede proceder directamente sin ejecutar el Triple Check completo, pero debe declarar la excepción explícitamente.
