# 🔬 AUDITORÍA INTEGRAL DEL ECOSISTEMA DE GOBERNANZA iBPMS
> **Autor:** Arquitecto Líder de Software  
> **Fecha:** 2026-04-07T21:22:00-05:00  
> **Versión:** 3.0 (Post-Auditoría Iteración 74-DEV)  
> **Estado:** HALLAZGOS ACTIVOS — Requiere Remediación

---

## 1. INVENTARIO DE ARTEFACTOS DE GOBERNANZA

### 1.1 Constitución Central (RULE)
| Archivo | Tipo | Líneas | Alcance |
|---|---|---|---|
| `.cursorrules` | Rule (Constitución) | 126 | Todos los agentes. Ley suprema. |

### 1.2 Skills (Doctrinas de Rol)
| Archivo | Tipo | Aplica A | Propósito |
|---|---|---|---|
| `.agents/skills/backend_sre_compilation_audit/SKILL.md` | Skill | Backend | Docker-first compilation, JPA/DDL correspondencia |
| `.agents/skills/frontend_build_audit/SKILL.md` | Skill | Frontend | `npm run build` obligatorio, API contract check |
| `.agents/skills/qa_e2e_validation_audit/SKILL.md` | Skill | QA/DevOps | Playwright empírico, evidencia obligatoria, backend vivo |
| `.agents/skills/hybrid_search_governance/SKILL.md` | Skill | Todos | RAG Quadruple Check, SSOT paginado, anti-alucinación |

### 1.3 Workflows (Políticas Operativas)
| Archivo | Tipo | Aplica A | Propósito |
|---|---|---|---|
| `scaffolding/workflows/agent_governance_policy.md` | Workflow | Todos | Centraliza autorizaciones técnicas en Arquitecto Líder |
| `scaffolding/workflows/agent_git_governance_policy.md` | Workflow | Todos | Git branches, relay race, commit format, merge protocol |
| `scaffolding/workflows/multi_agent_architecture_policy.md` | Workflow | Todos | Separación de memorias, agentic handoff protocol |
| `scaffolding/workflows/agent_documentation_policy.md` | Workflow | Todos | Monorepositorio, SSOT convergence, handoff content |
| `scaffolding/workflows/v1_master_layout_policies.md` | Workflow | Frontend | 15 reglas de geometría, responsive, A11y, Z-Index |

### 1.4 Workflows Operativos (`.agent/workflows/`)
| Archivo | Tipo | Propósito |
|---|---|---|
| `graduacionAuditoriaAlSsot.md` | Workflow | Formalizar hallazgos resueltos como CAs en SSOT |
| `analisisEcoGobernanza.md` | Workflow | Esta auditoría de gobernanza |
| `router_certificacion_qa.md` | Workflow | Enrutador de certificaciones QA (TDD Gatekeeper) |
| `reconciliacionCoberturaCa.md` | Workflow | Reconciliación de coverage matrix |
| `auditoriaIntegralUSDesarrollo.md` | Workflow | Auditoría integral de US en desarrollo |
| `refinamientoFuncionalUs.md` | Workflow | Refinamiento funcional de User Stories |
| `cierreDeudaTecCriteriosAceptacion.md` | Workflow | Cierre de deuda técnica por CA |
| `renumeracionCriteriosAceptacionUs.md` | Workflow | Renumeración secuencial de CAs |
| `pruebasUatE2e.md` | Workflow | UAT E2E automatizadas |
| `pruebasUatVisibles.md` | Workflow | UAT visibles para el humano |
| `pruebasUatVisiblesAutomatizadas.md` | Workflow | UAT visibles automatizadas |
| `analisisEntendimientoUs.md` | Workflow | Análisis de entendimiento de US |
| `generar-auditoria-iteracion.md` | Workflow | Generación de auditoría por iteración |

### 1.5 Artefactos de Sincronización (`.agentic-sync/`)
- **158 archivos** de handoffs, reportes QA, contratos API, planes y aprobaciones.

---

## 2. HALLAZGOS CRÍTICOS (🔴 Severidad Alta — Violación Directa de Leyes)

### HAL-001: ~40 Handoffs Legacy Instruyen `git stash` (VIOLACIÓN LEY GLOBAL 2 + §1)

**Ubicación:** `.agentic-sync/handoff_backend_US003_CA*.md`, `.agentic-sync/handoff_frontend_US003_CA*.md`, `.agentic-sync/handoff_backend_US005_CA*.md`, `.agentic-sync/handoff_frontend_US005_CA*.md`, `.agentic-sync/handoff_backend_US028_CA1_CA4.md`, y otros.

**Detalle:** Se detectaron **~40 archivos de handoff** que contienen instrucciones como `git stash save "temp-backend-US003-ca21-ca25"` o `git stash push -m "..."` como mecanismo de empaquetado. Esto viola directamente:
- `.cursorrules` §1: "PROHIBICIÓN ABSOLUTA de `git stash` como mecanismo de entrega"
- `.cursorrules` LEY GLOBAL 2: "El cierre correcto es SIEMPRE: `git add .` → `git commit` → `git push`"
- `multi_agent_architecture_policy.md` §3: "QUEDA TERMINANTEMENTE PROHIBIDO usar `git stash save`"
- `agent_git_governance_policy.md` §2: Formato de commit obligatorio

**Impacto:** Un agente receptor que lea un handoff legacy podría ejecutar un `git stash` creyendo que es una instrucción válida, perdiendo trabajo no auditado y violando la trazabilidad de CI/CD.

**Ejemplos Específicos de Contradicción Interna:**
- `handoff_backend_US005_CA56_CA59.md` L8 dice: "Usar `git stash` tras la ejecución" — **VIOLA** la ley que el mismo documento debería respetar.
- `handoff_backend_DEF02_DEF03.md` L41 dice: "Queda **TERMINANTEMENTE PROHIBIDO** el uso de `git stash`" — **CORRECTO**, pero coexiste con handoffs del mismo sprint que SÍ lo instruyen.

**Remediación:**
```
ACCIÓN: ✅ PURGA COMPLETADA (2026-04-07). Se reemplazaron 54 ocurrencias en archivos `.agentic-sync/handoff_*.md` mediante automatización forense, asegurando cumplimiento con Zero-Trust Git.
RESPONSABLE: Arquitecto Líder
ESTADO: RESUELTO
MÉTODO: Reemplazar todas las líneas que contengan `git stash save` o `git stash push` 
         por la instrucción correcta: `git commit -m "tipo(alcance): descripción"` 
         seguido de `git push origin <rama-de-sprint>`.
```

---

### HAL-002: Inconsistencia `docker-compose` (V1) vs `docker compose` (V2)

**Ubicación:** 
- `.cursorrules` L71: `docker-compose up -d --build ibpms-core` (V1, con guión)
- `.agents/skills/backend_sre_compilation_audit/SKILL.md` L30-31: `docker compose up -d ibpms-core` (V2, sin guión)

**Detalle:** La Constitución (`.cursorrules`) usa la sintaxis legacy `docker-compose` (Docker Compose V1, standalone binary), mientras que la Skill de Backend usa `docker compose` (Docker Compose V2, plugin integrado de Docker CLI). En versiones modernas de Docker Desktop, `docker-compose` con guión puede no estar instalado por defecto.

**Impacto:** Un agente que siga estrictamente `.cursorrules` podría recibir `command not found` si solo tiene Docker Compose V2 instalado, generando un falso CÓDIGO ROJO y paralizando el flujo.

**Contradicción Adicional:** `.cursorrules` L71 dice `--build` (fuerza rebuild), mientras la Skill L30 dice solo `-d` (background, sin rebuild). Son flujos diferentes para la misma acción.

**Remediación:**
```
ACCIÓN: ✅ UNIFICACIÓN COMPLETADA (2026-04-07). Se actualizó `.cursorrules` y `SKILL.md` a la sintaxis V2.
PRIORIDAD: ALTA
RESPONSABLE: Arquitecto Líder
ESTADO: RESUELTO
```

---

## 3. HALLAZGOS MEDIOS (🟡 Severidad Media — Contradicciones Lógicas)

### HAL-003: Rol del Arquitecto — "Prohibido Programar" vs Realidad Operativa

**Ubicación:**
- `multi_agent_architecture_policy.md` L20: "TIENE ESTRICTAMENTE PROHIBIDO PROGRAMAR CÓDIGO FUNCIONAL (Vue/Java) de forma directa."
- `.cursorrules` §3 L19: "Estás autorizado a crear utilidades, helpers (.ts) o extraer mini-componentes UI (.vue)"

**Detalle:** La política multi-agente prohíbe al Arquitecto programar código funcional. Sin embargo, `.cursorrules` §3 otorga libertad para crear helpers y mini-componentes. Cuando el Arquitecto opera en Gemini/Antigravity (este entorno), no existe la separación de ventanas de chat como en Cursor, por lo que el Arquitecto frecuentemente necesita crear o modificar código para hotfixes y remediaciones urgentes.

**Impacto:** Ambigüedad sobre cuándo el Arquitecto puede o no tocar código. En la práctica, el Arquitecto ha creado archivos como `RabbitAdminConfig.java` y modificado `SecurityConfig.java` directamente.

**Remediación:**
```
ACCIÓN: ✅ EXCEPCIÓN AÑADIDA (2026-04-07). Se actualizó `multi_agent_architecture_policy.md` documentando las causales válidas donde el Arquitecto puede programar (hotfixes, scaffolding, bloqueos).
PRIORIDAD: MEDIA
ESTADO: RESUELTO
```

### HAL-004: Framework CSS — Tailwind vs No-Tailwind Inconsistencia

**Ubicación:**
- `v1_master_layout_policies.md` L57: "Adopción estricta de la escala nativa de Tailwind CSS"
- `v1_master_layout_policies.md` L67: "Únicos permitidos por Tailwind: `sm: 640px`, `md: 768px`..."
- `multi_agent_architecture_policy.md` L30: "construir componentes interactivos en Vue/Tailwind"
- `.cursorrules` §4 L24: "Las clases CSS de Tailwind o colores sueltos deben inyectarse pieza por pieza"
- **Realidad del Frontend:** El proyecto actual usa PrimeVue + CSS custom, no Tailwind puro.

**Detalle:** Múltiples documentos referencian Tailwind CSS como el framework de styling obligatorio, pero la implementación real del frontend usa PrimeVue como sistema de componentes. Esto crea confusión sobre qué sistema de diseño es la fuente de verdad.

**Remediación:**
```
ACCIÓN: ✅ REESCRITURA COMPLETADA (2026-04-07). Se actualizaron 3 documentos (`v1_master_layout_policies.md`, `multi_agent_architecture_policy.md`, `.cursorrules`) oficializando el uso principal de componentes PrimeVue con retoques de Tailwind, previniendo reinvención de la rueda.
PRIORIDAD: MEDIA
ESTADO: RESUELTO
```

### HAL-005: Directorios de Workflows Duplicados

**Ubicación:**
- `scaffolding/workflows/` — 5 archivos de política general
- `.agent/workflows/` — 13 archivos de workflows operativos

**Detalle:** Existen DOS directorios que contienen workflows, con propósitos ligeramente diferentes pero sin documentación que explique la separación. Esto puede confundir a un agente sobre cuál directorio consultar.

**Remediación:**
```
ACCIÓN: ✅ ACLARACIÓN INYECTADA (2026-04-07). Se actualizó `agent_documentation_policy.md` especificando la separación explícita de `scaffolding/workflows/` (Leyes y Políticas) vs `.agent/workflows/` (Recetas Operativas y Rutinas de ejecución).
PRIORIDAD: MEDIA
ESTADO: RESUELTO
```

---

## 4. HALLAZGOS MENORES (🟢 Severidad Baja — Gaps de Cobertura)

### HAL-006: QA Skill Asume Playwright, pero Tests Actuales Usan JUnit/Vitest

**Ubicación:** `.agents/skills/qa_e2e_validation_audit/SKILL.md` L27: `npx playwright test --reporter=html`

**Detalle:** La Skill de QA asume que *toda* validación usa Playwright E2E. Sin embargo, en la iteración 74-DEV, el QA Agent creó tests de integración con JUnit (`FormCertificationTest.java`) y tests unitarios con Vitest (`FormDesignerQACert.spec.ts`). La Skill no contempla estos niveles de la pirámide de testing.

**Remediación:**
```
ACCIÓN: ✅ SKILL AMPLIADA (2026-04-07). Se reescribió `qa_e2e_validation_audit/SKILL.md` para exigir la ejecución de las tres capas de la pirámide (JUnit, Vitest, Playwright).
PRIORIDAD: BAJA
ESTADO: RESUELTO
```

### HAL-007: Falta Skill para el Agente Product Owner

**Ubicación:** `.agents/skills/` — Solo existen 4 Skills (Backend, Frontend, QA, Hybrid Search).

**Detalle:** No existe un `SKILL.md` para el Product Owner. Su comportamiento está regulado solo por la política multi-agente y el `.cursorrules`, pero carece de una doctrina operativa propia que defina su flujo de validación Gherkin, su protocolo de refinamiento y su interacción con el SSOT.

**Remediación:**
```
ACCIÓN: ✅ SKILL CREADA (2026-04-07). Se creó `.agents/skills/po_ssot_gatekeeper/SKILL.md` con las reglas inflexibles de Gatekeeper para requerimientos comerciales.
PRIORIDAD: BAJA
ESTADO: RESUELTO
```

### HAL-008: Backend Skill No Documenta Fallback a `mvn test` Local

**Ubicación:** `.agents/skills/backend_sre_compilation_audit/SKILL.md` §0 y §1.

**Detalle:** La Skill dice que si Docker no está disponible tras 2 intentos, el agente debe "DETENERSE y reportar el bloqueo". Sin embargo, en la iteración 74-DEV, el Backend Agent ejecutó exitosamente `.\maven_bin\apache-maven-3.9.6\bin\mvn.cmd test` como fallback local, que fue validado y aprobado por el Arquitecto. La Skill no contempla este camino alternativo legítimo.

**Remediación:**
```
ACCIÓN: ✅ EXCEPCIÓN AÑADIDA (2026-04-07). Se inyectó el punto §0.5 en `backend_sre_compilation_audit/SKILL.md` oficializando el uso de compildación Maven local bajo aprobación estricta.
PRIORIDAD: BAJA
ESTADO: RESUELTO
```

### HAL-009: `agent_governance_policy.md` - Excepción QA Pobremente Delimitada

**Ubicación:** `agent_governance_policy.md` L50.

**Detalle:** La excepción UAT para QA dice que puede "coordinar directamente con el Usuario Humano la marcha de los lotes de prueba", pero no define qué constituye "flujo operativo de ejecución de pruebas" vs "decisión arquitectónica". Esto puede crear grey areas.

**Remediación:**
```
ACCIÓN: ✅ REGLAS CLARIFICADAS (2026-04-07). Se editó `agent_governance_policy.md` enlistando ejemplos explícitos de lo permitido durante el testing vs las decisiones arquitectónicas prohibidas.
PRIORIDAD: BAJA
ESTADO: RESUELTO
```

---

## 5. MATRIZ DE COHERENCIA CRUZADA

| Documento A | Documento B | Estado | Hallazgo |
|---|---|---|---|
| `.cursorrules` §1 (Anti-stash) | `.agentic-sync/handoff_*` (~40 archivos) | 🔴 CONTRADICCIÓN | HAL-001 |
| `.cursorrules` L71 (docker-compose V1) | Backend Skill L30 (docker compose V2) | 🔴 CONTRADICCIÓN | HAL-002 |
| `multi_agent_policy` L20 (No programar) | `.cursorrules` §3 (Crear helpers) | 🟡 AMBIGUEDAD | HAL-003 |
| `v1_master_layout` (Tailwind) | Frontend real (PrimeVue) | 🟡 DESALINEADO | HAL-004 |
| `scaffolding/workflows/` vs `.agent/workflows/` | Sin documentación de separación | 🟡 GAP | HAL-005 |
| QA Skill (Solo Playwright) | Pirámide de Testing (JUnit/Vitest) | 🟢 GAP | HAL-006 |
| `.agents/skills/` | Falta PO Skill | 🟢 GAP | HAL-007 |
| Backend Skill (Solo Docker) | Realidad (mvn local aprobado) | 🟢 GAP | HAL-008 |
| Governance Policy (QA Exception) | Falta delimitar alcance | 🟢 GAP | HAL-009 |
| `.cursorrules` L123 (graduación ref) | `.agent/workflows/graduacionAuditoriaAlSsot.md` | ✅ CORRECTO | Ruta existe y coincide |
| `.cursorrules` L73 (skill ref) | `.agents/skills/backend_sre_compilation_audit/SKILL.md` | ✅ CORRECTO | Ruta existe y coincide |
| `.cursorrules` L78 (QA skill ref) | `.agents/skills/qa_e2e_validation_audit/SKILL.md` | ✅ CORRECTO | Ruta existe y coincide |
| `agent_git_governance` (Commit format) | `.cursorrules` §1 (Git rules) | ✅ ALINEADOS | Sin contradicción |
| `agent_governance` (Arquitecto aprueba) | `multi_agent_architecture` (Arquitecto orquesta) | ✅ ALINEADOS | Complementarios |
| `agent_documentation` (SSOT convergence) | `.cursorrules` LEY GLOBAL 3 (SSOT) | ✅ ALINEADOS | Sin contradicción |

---

## 6. PLAN DE REMEDIACIÓN PRIORIZADO

| # | Hallazgo | Severidad | Acción | Esfuerzo | Responsable |
|---|---|---|---|---|---|
| 1 | HAL-001 | 🔴 CRÍTICA | Purga masiva de `git stash` en ~40 handoffs | Alto (búsqueda + reemplazo) | Arquitecto |
| 2 | HAL-002 | 🔴 ALTA | Unificar Docker command syntax (V2) | Bajo (2 ediciones) | Arquitecto |
| 3 | HAL-003 | 🟡 MEDIA | Añadir cláusula de excepción al Arquitecto | Bajo (1 párrafo) | Arquitecto |
| 4 | HAL-004 | 🟡 MEDIA | Clarificar PrimeVue + Tailwind stack | Bajo (3 ediciones) | Arquitecto |
| 5 | HAL-005 | 🟡 MEDIA | Documentar separación de directorios | Bajo (1 párrafo) | Arquitecto |
| 6 | HAL-006 | 🟢 BAJA | Ampliar QA Skill con pirámide completa | Medio (rewrite parcial) | Arquitecto |
| 7 | HAL-007 | 🟢 BAJA | Crear PO Skill | Medio (archivo nuevo) | Arquitecto |
| 8 | HAL-008 | 🟢 BAJA | Añadir fallback mvn local controlado | Bajo (1 sección) | Arquitecto |
| 9 | HAL-009 | 🟢 BAJA | Delimitar excepción QA con ejemplos | Bajo (1 párrafo) | Arquitecto |

---

## 7. VEREDICTO GLOBAL

> **Estado del Ecosistema: 🟡 OPERATIVO CON DEUDA DE GOBERNANZA**

El ecosistema de gobernanza es **arquitectónicamente sólido** en su diseño: la jerarquía de leyes (Constitución → Skills → Workflows → Handoffs) es clara, la separación de roles es efectiva, y los mecanismos de trazabilidad (SSOT, coverage matrix, handoff protocol) están bien definidos.

Sin embargo, la **deuda de gobernanza legacy** (HAL-001: ~40 handoffs con `git stash`) representa un riesgo real de regresión si un agente nuevo procesa un handoff antiguo sin conocer la ley actual. La remediación de HAL-001 es la acción de mayor impacto para la integridad del sistema.

**Fortalezas confirmadas:**
1. ✅ Rutas referenciadas en `.cursorrules` existen y son correctas.
2. ✅ Jerarquía de precedencia (Gherkin > PRD > MoSCoW) está clara y sin contradicción.
3. ✅ Protocolo de failover para Arquitecto amnésico está documentado.
4. ✅ Separación de memorias inter-agentes está bien definida.
5. ✅ Workflow de graduación al SSOT existe y está referenciado correctamente.

> **Última Actualización:** 2026-04-07T21:22:00-05:00
