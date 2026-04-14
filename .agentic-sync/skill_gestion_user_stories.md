# SKILL: Gestión de User Stories (Crear, Modificar, Eliminar)

> **Versión:** 1.0 | **Fecha:** 2026-04-10 | **Autor:** Arquitecto Líder  
> **Aplica a:** PO, Arquitecto Líder, cualquier agente que necesite modificar requisitos  
> **Prerequisito:** Conocer la estructura modularizada de `docs/requirements/epics/`

---

## Contexto Arquitectónico

Las User Stories del iBPMS V1 están **modularizadas en 7 archivos de épica**, NO en un archivo monolítico. El archivo original `v1_user_stories.md` está **DEPRECADO** y es de solo lectura.

### Artefactos del Ecosistema de Requisitos

| Artefacto | Ruta | Rol | ¿Se edita? |
|-----------|------|-----|:----------:|
| **Archivos de Épica** | `docs/requirements/epics/epic_*.md` | Contienen las US con sus CAs en Gherkin | ✅ SÍ |
| **Índice Maestro** | `docs/requirements/v1_user_stories_index.md` | Navegación: tabla con links a cada épica y estado | ✅ SÍ (cuando se agrega/elimina US) |
| **Registro JSON** | `docs/requirements/v1_user_stories_registry.json` | Lookup para agentes: mapea US → archivo | ✅ SÍ (cuando se agrega/elimina US) |
| **Coverage Matrix** | `.agentic-sync/coverage_matrix.md` | Estado de implementación (Back/Front/QA) | ✅ SÍ (cuando cambia estado de desarrollo) |
| **Monolito Legacy** | `docs/requirements/v1_user_stories.md` | Backup histórico — DEPRECADO | ⛔ NO TOCAR |

### Mapa de Épicas

| Épica | Archivo | Dominio |
|-------|---------|---------|
| A | `epic_A_motor_core.md` | Workdesk, Kanban, Triggers, CQRS |
| B | `epic_B_formularios_bpmn.md` | iForm IDE, Zod, BPMN, DMN |
| C | `epic_C_ia_mlops_sac.md` | Email IA, MLOps, Buzones SAC |
| D | `epic_D_crm_intake_portal.md` | CRM, Intake, Portal B2C |
| E | `epic_E_seguridad_identidad_config.md` | RBAC, IdP, SLA, DevPortal |
| F | `epic_F_dashboards_integraciones.md` | BAM, PDF, SharePoint, RabbitMQ, System Limits |
| G | `epic_G_ia_cognitiva_agentes_rag.md` | Copiloto IA, Multi-Agente, LLM, RAG Dual |

---

## Procedimiento 1: Modificar una User Story Existente

### Paso 1 — Localizar la US

**Opción A (Rápida):** Buscar en el JSON:
```
Abrir: docs/requirements/v1_user_stories_registry.json
Buscar: "US-XXX"
Resultado: { "file": "epics/epic_X_nombre.md", ... }
```

**Opción B (Visual):** Abrir el índice maestro:
```
Abrir: docs/requirements/v1_user_stories_index.md
Buscar la US en las tablas → el link te lleva al archivo de épica
```

### Paso 2 — Editar en el archivo de épica

1. Abrir el archivo de épica identificado en el Paso 1
2. Buscar `### US-XXX:` dentro del archivo
3. Realizar la modificación (agregar CA, cambiar texto, corregir Gherkin)
4. **Respetar el formato Gherkin existente** — todos los CAs usan bloques ```gherkin

### Paso 3 — Actualizar artefactos dependientes

| ¿Qué cambió? | ¿Qué actualizar? |
|---------------|-------------------|
| Se modificó texto de un CA existente | Solo el archivo de épica. No se tocan ni el índice ni el JSON. |
| Se agregaron nuevos CAs a una US existente | El archivo de épica + la `coverage_matrix.md` (agregar filas para los nuevos CAs con ❌). |
| Se cambió el nombre/título de una US | El archivo de épica + el `v1_user_stories_index.md` (actualizar título en la tabla) + el `v1_user_stories_registry.json` (actualizar campo `title`). |
| Se eliminó un CA | El archivo de épica + la `coverage_matrix.md` (marcar como 🚫 Excluido, NO borrar la fila). |

### Paso 4 — Commit

```
git add docs/requirements/epics/epic_X_*.md
git add .agentic-sync/coverage_matrix.md  # si aplica
git commit -m "refactor(requirements): US-XXX - [descripción del cambio]"
```

---

## Procedimiento 2: Crear una Nueva User Story

### Paso 1 — Determinar la épica destino

Preguntarse: **¿A qué dominio funcional pertenece esta US?**

| Si la US trata de... | Va en... |
|----------------------|----------|
| Tareas, workdesk, orquestación, proyectos, persistencia | `epic_A_motor_core.md` |
| Formularios, validación Zod, BPMN, DMN | `epic_B_formularios_bpmn.md` |
| Email IA, buzones, MLOps, docketing | `epic_C_ia_mlops_sac.md` |
| CRM, clientes, portal externo, intake | `epic_D_crm_intake_portal.md` |
| RBAC, identidad, seguridad, configuración global | `epic_E_seguridad_identidad_config.md` |
| Dashboards, reportes, integraciones, límites del sistema | `epic_F_dashboards_integraciones.md` |
| IA cognitiva, agentes, LLM, RAG | `epic_G_ia_cognitiva_agentes_rag.md` |

> **Regla de oro:** Si una US podría ir en 2 épicas, elegir la que tenga más US relacionadas o preguntar al Arquitecto Líder.

### Paso 2 — Asignar ID de US

1. Consultar el `v1_user_stories_registry.json` para ver el último ID asignado
2. Asignar el siguiente número disponible (ej: si la última es US-057, la nueva es US-058)
3. **NUNCA reutilizar un ID eliminado**

### Paso 3 — Redactar la US en el archivo de épica

Agregar al **final** del archivo de épica correspondiente, usando este template:

```markdown
---

### US-XXX: [Título Descriptivo de la Funcionalidad]
**Como** [Rol del Actor]
**Quiero** [acción que desea realizar]
**Para** [beneficio de negocio que obtiene].

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: [Nombre de la Feature]

  Scenario: CA-1 — [Título del Criterio]
    Given [precondición]
    When [acción del usuario]
    Then [resultado esperado]
    And [condición adicional]
`` `
```

> **Convención de numeración de CAs:** Cada US inicia su numeración de CAs desde CA-1. Los CAs son relativos a la US, no globales.

### Paso 4 — Actualizar los 3 artefactos de navegación

**4.1 — Índice Maestro** (`v1_user_stories_index.md`):
- Agregar una fila en la tabla de la épica correspondiente:
```markdown
| US-XXX | [Título de la US] | ❌ Pendiente |
```

**4.2 — Registro JSON** (`v1_user_stories_registry.json`):
- Agregar la entrada en el objeto `stories`:
```json
"US-XXX": { "file": "epics/epic_X_nombre.md", "epic": "X", "title": "Título", "status": "pending" }
```

**4.3 — Coverage Matrix** (`.agentic-sync/coverage_matrix.md`):
- Agregar una nueva sección al final del archivo:
```markdown
## US-XXX: [Título]
**Épica:** X — [Nombre] | **Estado:** ❌ PENDIENTE

| CA | Título (corto) | Back | Front | QA | Sprint | Notas |
|----|----------------|------|-------|----|--------|-------|
| CA-1 | [Título] | ❌ | ❌ | ❌ | — | Nueva |
```

### Paso 5 — Actualizar el contador de US en el índice

En `v1_user_stories_index.md`, actualizar la línea del header:
```
> **Total US:** [nuevo total]
```

### Paso 6 — Commit

```
git add docs/requirements/epics/epic_X_*.md
git add docs/requirements/v1_user_stories_index.md
git add docs/requirements/v1_user_stories_registry.json
git add .agentic-sync/coverage_matrix.md
git commit -m "feat(requirements): US-XXX - [título corto de la nueva US]"
```

---

## Procedimiento 3: Eliminar o Anular una User Story

> **REGLA:** Las US NUNCA se borran físicamente. Se marcan como anuladas para preservar la trazabilidad.

### Paso 1 — En el archivo de épica

Agregar un banner de anulación al inicio de la US:

```markdown
### US-XXX: [Título Original]

> [!CAUTION]
> **⛔ US ANULADA (YYYY-MM-DD).** Razón: [explicación]. Reemplazada por US-YYY (si aplica).
```

### Paso 2 — En los artefactos de navegación

- **Índice:** Cambiar estado a `🚫 Anulada`
- **JSON:** Cambiar `"status": "deprecated"`
- **Coverage:** Marcar todos los CAs como `🚫`

### Paso 3 — Commit

```
git commit -m "deprecate(requirements): US-XXX anulada - [razón corta]"
```

---

## Procedimiento 4: Mover una US entre Épicas

En casos excepcionales, una US puede cambiar de dominio.

### Pasos

1. **Cortar** el bloque completo de la US (desde `### US-XXX:` hasta antes del siguiente `### US-`) del archivo de épica origen.
2. **Pegar** al final del archivo de épica destino.
3. **Actualizar** `v1_user_stories_index.md` (mover la fila a la nueva épica).
4. **Actualizar** `v1_user_stories_registry.json` (cambiar el campo `"file"` y `"epic"`).
5. **Commit** con mensaje: `refactor(requirements): US-XXX migrada de epic_X a epic_Y - [razón]`

---

## Reglas de Oro (Checklist de Validación)

Antes de hacer commit, verificar:

- [ ] **¿Edité el archivo de épica correcto?** (verificar con el JSON)
- [ ] **¿Actualicé el índice maestro?** (solo si agregué/eliminé/renombré US)
- [ ] **¿Actualicé el JSON registry?** (solo si agregué/eliminé/movió US)
- [ ] **¿Actualicé la coverage_matrix?** (solo si agregué/eliminé CAs)
- [ ] **¿Usé formato Gherkin para los CAs?** (obligatorio)
- [ ] **¿NO toqué `v1_user_stories.md`?** (está deprecado ⛔)
- [ ] **¿El archivo de épica sigue teniendo 7-10 US?** (si excede 10, consultar al Arquitecto sobre split)

---

## Diagrama de Decisión Rápida

```
¿Qué necesito hacer?
│
├── Modificar texto/CA existente
│   └── Editar solo el archivo de épica → commit
│
├── Agregar CAs a una US existente
│   └── Editar archivo de épica + coverage_matrix → commit
│
├── Crear nueva US
│   └── Editar: épica + índice + JSON + coverage → commit
│
├── Anular/Deprecar una US
│   └── Marcar en: épica + índice + JSON + coverage → commit
│
└── Mover US entre épicas
    └── Cortar/pegar épica + actualizar índice + JSON → commit
```

---

> **⚠️ Recordatorio:** Si un agente te dice que no puede leer las User Stories por timeout, verifica que esté leyendo los archivos de `epics/` y NO el monolito deprecado `v1_user_stories.md`.
