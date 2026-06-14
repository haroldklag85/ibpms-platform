# Agentic Handoff: Creación de Casos de Uso UAT — Sprint 1

**To:** Product Owner Agent [⚙️ PRODUCT OWNER]  
**From:** Lead Software Architect Agent [🧠 ARQUITECTO LÍDER]  
**Sprint:** 1  
**Gate Previo:** Sprint 0 aprobado (2026-04-13)  
**Date:** 2026-04-13  
**Prioridad:** 🔴 BLOQUEANTE — Sin estos UATs no hay Gate Funcional S1

---

## Contexto

El Sprint 0 fue aprobado por el Jefe de Equipo. Los UATs de los journeys J-04 y J-02 ya existen en `docs/uat/`. Ahora necesitamos los **UATs granulares por US** para cubrir cada Criterio de Aceptación del Sprint 1.

El flujo de negocio de la plataforma iBPMS es:
```
Formularios → BPMN → Workdesk → CQRS
```

---

## 🛑 INSTRUCCIÓN CRÍTICA: CÓMO LEER LAS HISTORIAS DE USUARIO

### ⛔ PROHIBIDO leer `docs/requirements/v1_user_stories.md`
Ese archivo está deprecado. Es demasiado grande y causa timeout/OOM.

### ✅ Protocolo correcto (3 pasos):

**Paso 1:** Lee el índice maestro para ubicar la US en su épica:
```
docs/requirements/v1_user_stories_index.md
```

**Paso 2:** Abre SOLO el archivo de épica correspondiente:

| US | Archivo de Épica (dentro de `docs/requirements/epics/`) |
|----|--------------------------------------------------------|
| US-001 | `epic_A_motor_core.md` |
| US-002 | `epic_A_motor_core.md` |
| US-017 | `epic_A_motor_core.md` |
| US-029 | `epic_B_formularios_bpmn.md` |
| US-003 | `epic_B_formularios_bpmn.md` |
| US-005 | `epic_B_formularios_bpmn.md` |
| US-036 | `epic_E_seguridad_identidad_config.md` |
| US-038 | `epic_E_seguridad_identidad_config.md` |
| US-048 | `epic_E_seguridad_identidad_config.md` |

**Paso 3:** Dentro de cada archivo de épica, busca la US por su ID (ej: `## US-002`). Los archivos son grandes (100-200 KB), usa búsqueda por texto.

---

## Formato Obligatorio de Salida

Cada documento UAT DEBE seguir la plantilla establecida en:
- `docs/uat/casos_uso_uat_j04.md` (referencia de formato)
- `docs/uat/casos_uso_uat_j02.md` (referencia de formato)

Estructura obligatoria:
```markdown
# Casos de Uso UAT — [US-XXX: Nombre]

> **US:** US-XXX — [Nombre]
> **Actor principal:** [Rol]
> **Criticidad:** 🔴/🟡/🟢
> **Épica:** [Nombre]
> **Fecha:** YYYY-MM-DD
> **Autor:** Agente PO

---
## Precondiciones
| # | Precondición | Verificación |
---
## Escenarios UAT
### CU-[ID]-01: [Título]
**CA Mapeado:** CA-XX, CA-XX
| Paso | Actor | Acción | Resultado Esperado |
**Criterio de aceptación:** [Resumen]
---
## Escenarios Negativos
---
## Matriz de Trazabilidad
| Escenario | US | CAs Cubiertos | Prioridad |
```

---

## Ejecución por Iteraciones

Ejecuta las siguientes 6 iteraciones **en orden**, una por una. Cada iteración produce UN documento UAT.

---

### ITERACIÓN 1 de 6 — US-001: CAs Faltantes del Workdesk

**Fuente:** `docs/requirements/epics/epic_A_motor_core.md` → sección `## US-001`

**Instrucciones:**
1. Lee la US-001 en el archivo de épica
2. Compara contra los CAs ya cubiertos en `docs/uat/casos_uso_uat_j04.md` (que cubre CA-01, CA-05 genéricamente)
3. Crea escenarios para los **15 CAs restantes**, agrupados por dominio:
   - **WebSocket (CA-6, CA-13, CA-26, CA-27):** Ghost deletion, throttling, vocabulario
   - **SLA (CA-5, CA-11, CA-24, CA-25, CA-31):** Ticking engine, semáforos, tab inactiva
   - **Delegación (CA-4, CA-15):** Toggle Mis Tareas/Equipo, anti-IDOR
   - **Anti-Abuse (CA-8, CA-16, CA-21, CA-28):** Cherry-picking, skill routing, race condition
4. Incluye al menos 3 escenarios negativos

**Salida:** `docs/uat/casos_uso_uat_us001_sprint1.md`

---

### ITERACIÓN 2 de 6 — US-002: Task Claiming Completo

**Fuente:** `docs/requirements/epics/epic_A_motor_core.md` → sección `## US-002`

**Instrucciones:**
1. Lee TODOS los CAs de US-002
2. Los J-04 ya cubren claiming genérico (CU-J04-02, CU-J04-10), necesitamos detalle por CA
3. Cubre: claim, unclaim, validación de permisos, WebSocket ghost, y todos los demás CAs
4. Incluye al menos 2 escenarios negativos

**Salida:** `docs/uat/casos_uso_uat_us002.md`

---

### ITERACIÓN 3 de 6 — US-029: Ejecución de Formulario

**Fuente:** `docs/requirements/epics/epic_B_formularios_bpmn.md` → sección `## US-029`

**Instrucciones:**
1. Lee TODOS los CAs de US-029
2. Enfócate SOLO en CAs de UI/UX (los CAs de CQRS viven en US-017, no te corresponden aquí)
3. Cubre: apertura, renderizado dinámico, validación Zod, autoguardado, Upload-First, envío
4. Incluye al menos 3 escenarios negativos

**Salida:** `docs/uat/casos_uso_uat_us029.md`

---

### ITERACIÓN 4 de 6 — US-005: Validación Modeler BPMN (TRACK B)

**Fuente:** `docs/requirements/epics/epic_B_formularios_bpmn.md` → sección `## US-005`

**Instrucciones:**
1. Esta US está **implementada al 97%** pero QA al 0%
2. Crea escenarios de **validación** (no de construcción)
3. Selecciona los **10 CAs más críticos**
4. Verifica: carga del modeler, drag & drop, guardado, despliegue, versionado
5. Incluye al menos 2 escenarios negativos

**Salida:** `docs/uat/casos_uso_uat_us005_validacion.md`

---

### ITERACIÓN 5 de 6 — US-003: Validación IDE Formularios (TRACK B)

**Fuente:** `docs/requirements/epics/epic_B_formularios_bpmn.md` → sección `## US-003`

**Instrucciones:**
1. Esta US está **completada** pero QA al 0%
2. Crea escenarios de **validación**
3. Selecciona los **10 CAs más críticos**
4. Verifica: designer drag & drop, configuración de campos, schema Zod, preview, guardar/recuperar
5. Incluye al menos 2 escenarios negativos

**Salida:** `docs/uat/casos_uso_uat_us003_validacion.md`

---

### ITERACIÓN 6 de 6 — US-036/038/048: Validación Seguridad Consolidada (TRACK B)

**Fuente:** `docs/requirements/epics/epic_E_seguridad_identidad_config.md` → secciones `## US-036`, `## US-038`, `## US-048`

**Instrucciones:**
1. Las 3 US están **completadas** pero QA parcial o 0%
2. Crea UN documento consolidado con las 3 US
3. Selecciona **5 CAs más críticos de cada US** (15 total)
4. US-036: roles, permisos, restricciones
5. US-038: multi-rol, herencia, EntraID sync
6. US-048: crear usuario, reset password, login, lockout
7. Incluye al menos 3 escenarios negativos (1 por US)

**Salida:** `docs/uat/casos_uso_uat_seguridad_validacion.md`

---

## Reglas Obligatorias

1. **NO inventes CAs.** Todo escenario mapea a un CA real del archivo de épica.
2. **NO leas `v1_user_stories.md`.** Solo archivos de épica.
3. **Matriz de Trazabilidad** obligatoria al final de cada documento.
4. **MoSCoW:** Marca cada escenario como MUST, SHOULD, COULD o WON'T.
5. **Mínimo 2-3 escenarios negativos** por documento.
6. **Formato tabla paso-a-paso** obligatorio (no Gherkin libre).
7. **Guarda cada salida** en `docs/uat/` con el nombre indicado.

---

## Reporte al Finalizar Cada Iteración

```
✅ Iteración X/6 completada
- Documento: docs/uat/[nombre].md
- Escenarios positivos: N
- Escenarios negativos: N  
- CAs cubiertos: [lista]
- CAs NO cubiertos (justificar): [lista]
- Gaps o preguntas abiertas: [lista]
```

---

## Referencias

| Documento | Ruta |
|-----------|------|
| Índice US | `docs/requirements/v1_user_stories_index.md` |
| Épica A | `docs/requirements/epics/epic_A_motor_core.md` |
| Épica B | `docs/requirements/epics/epic_B_formularios_bpmn.md` |
| Épica E | `docs/requirements/epics/epic_E_seguridad_identidad_config.md` |
| UAT J-04 | `docs/uat/casos_uso_uat_j04.md` |
| UAT J-02 | `docs/uat/casos_uso_uat_j02.md` |
| Sprint 1 Plan | `docs/sprints/sprint_plan_s1.md` |
| E2E Test Plan | `docs/sprints/e2e_test_plan.md` |
| Coverage Matrix | `.agentic-sync/coverage_matrix.md` |

---

**Mensaje para el Humano:** Este handoff establece el alcance funcional del Sprint 1 para el Agente PO. Tras completar las 6 iteraciones, el PO debe notificar al Arquitecto Lead para la revisión cruzada de cobertura de CAs antes del Gate Funcional.
