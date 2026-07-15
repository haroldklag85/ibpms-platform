# 📦 Handoff QA — Iteración 84-DEV-LANE-ROLE
# Micro-Sprint 6: Integración E2E y Certificación (Lane-Role Assignment)

---

## 1. Metadatos y SSOT

| Campo | Valor |
|-------|-------|
| **Iteración** | `84-DEV-LANE-ROLE` |
| **US** | US-005 (Extensión: Lane Actor Assignment) + US-036 (Extensión: RBAC Lane Integration) |
| **CAs** | Extensión funcional aprobada por PO — Validar 7 escenarios E2E |
| **Rama Git** | `feature/lane-role-assignment` |
| **Agente** | QA |
| **Dependencias** | ✅ TODOS los anteriores completados (Infra + Backend + Frontend pusheados) |
| **SSOT** | `docs/requirements/v1_user_stories_index.md` → Epic B (US-005) + Epic E (US-036) |
| **Flujo de Trabajo** | Infra/BD → Backend → Frontend → **QA** |
| **API Contracts** | `docs/sprints/gobernanza_pm/API_CONTRACTS.md` — Sección 5.9 Lane Management |

---

## 2. Alineación Arquitectónica y ADRs

### ADRs Aplicables

| ADR | Impacto |
|-----|--------|
| ADR-010 (Testing Pyramid) | Pruebas E2E contra PostgreSQL real (Docker, puerto 5433). H2 PROHIBIDO. Testcontainers PROHIBIDO. Playwright para CT visual. |
| ADR-009 (PostgreSQL) | Verificar tablas nuevas (`ibpms_bpmn_lane`, `ibpms_lane_role_assignment`) en PostgreSQL real. |

### Confirmación de Stack
- **Testing E2E:** Playwright (navegador real) + REST Assured (API)
- **BD:** PostgreSQL real en Docker (puerto 5433)
- **Prohibiciones:** No H2, no mocks, no mockAdapter, no stubs, no JSON estáticos

---

## 3. Rutas Exactas y Contexto

### Endpoints a Validar (verificados en API_CONTRACTS.md Sección 5.9)

| Endpoint | Método | Propósito |
|----------|--------|-----------|
| `/api/v1/admin/lanes?processKey={key}` | GET | Listar lanes de un proceso |
| `/api/v1/admin/roles/{roleId}/lane-assignments` | GET | Obtener asignaciones lane↔rol |
| `/api/v1/admin/roles/{roleId}/lane-assignments` | PUT | Guardar asignaciones I/E |

### Vistas Frontend a Validar

| Vista | Ruta | Componente |
|-------|------|------------|
| BPMN Modeler | `/admin/modeler/bpmn` | `BpmnDesigner.vue` — Panel de propiedades Lane |
| Identity Governance | `/admin/security/identity` | `IdentityGovernance.vue` — Vista jerárquica Proceso→Lanes |

### Tablas de BD a Verificar

| Tabla | Verificación |
|-------|-------------|
| `ibpms_bpmn_lane` | Contiene lanes tras deploy de BPMN con Pool+Lanes |
| `ibpms_lane_role_assignment` | Contiene asignaciones tras guardar I/E en RBAC |

---

## 4. Escenarios de Validación E2E (7 Scenarios)

> 📚 **SKILL OBLIGATORIO:** Aplica el skill `.agents/skills/qa_e2e_validation_audit/SKILL.md` para garantizar la Ley de Correspondencia Gherkin (Test vs User Story). Todo escenario sin test correspondiente debe reportarse como Cobertura Faltante.

### NFR/QA Strategy (parametrizada por PM-IA):
> Pruebas E2E: Crear BPMN con Pool+Lanes → Desplegar → Verificar tabla `ibpms_bpmn_lane` → Editar Rol en RBAC → Asignar Lane → Verificar tabla `ibpms_lane_role_assignment`. Regresión: J-02 y J-04 no deben romper.

| # | Escenario | Precondición | Pasos | Criterio PASS |
|---|-----------|-------------|-------|---------------|
| E1 | Crear BPMN con Pool + 2 Lanes | BPMN Modeler abierto | 1. Crear nuevo BPMN<br>2. Añadir Pool<br>3. Añadir 2 Lanes ("Contabilidad", "Aprobación") | Lanes visibles en canvas, panel propiedades muestra campos al seleccionar |
| E2 | Asignar nombre y actor al Lane | Lane "Contabilidad" seleccionado | 1. Escribir nombre en input<br>2. Escribir actor en input actor | Valores reflejados en XML BPMN (verificar Export) |
| E3 | Vincular rol RBAC al Lane | Lane "Contabilidad" seleccionado | 1. Abrir dropdown de roles<br>2. Seleccionar "ROLE_PERITO" | `camunda:candidateGroups` escrito en XML. Badge muestra "✅ Rol vinculado" |
| E4 | Desplegar el BPMN | BPMN con Pool+Lanes guardado | 1. Click "Desplegar"<br>2. Verificar toast | Toast muestra roles generados + `SELECT count(*) FROM ibpms_bpmn_lane WHERE process_design_id = ?` retorna 2 |
| E5 | Verificar lanes en RBAC | Rol "ROLE_PERITO" existe | 1. Ir a `/admin/security/identity`<br>2. Editar "ROLE_PERITO"<br>3. Sección "Definición BPMN" | El proceso desplegado aparece con 2 lanes expandibles |
| E6 | Asignar I/E por Lane | Modal de edición de rol abierto | 1. Expandir proceso<br>2. Marcar checkbox E para Lane "Contabilidad"<br>3. Guardar | `SELECT * FROM ibpms_lane_role_assignment WHERE role_id = ? AND can_execute = true` retorna 1 fila |
| E7 | Regresión J-02 y J-04 | Entorno estable | 1. Ejecutar journey J-02<br>2. Ejecutar journey J-04 | Ambos journeys pasan sin regresión |

---

## 5. Matriz de QA y Testing Atómico

| Test Name | Escenario | Aserción Esperada |
|-----------|-----------|-------------------|
| `test_lane_properties_panel_renders` | E1 | Panel de propiedades visible al seleccionar Lane |
| `test_lane_name_syncs_to_xml` | E2 | XML BPMN contiene nombre actualizado del Lane |
| `test_lane_role_linked_via_dropdown` | E3 | `camunda:candidateGroups` contiene nombre del rol |
| `test_deploy_creates_bpmn_lane_rows` | E4 | `ibpms_bpmn_lane` tiene 2 filas para el proceso |
| `test_rbac_shows_hierarchical_lanes` | E5 | Vista jerárquica renderiza procesos y lanes |
| `test_lane_assignment_persists_ie` | E6 | `ibpms_lane_role_assignment` tiene fila con `can_execute=true` |
| `test_regression_j02_j04_pass` | E7 | J-02 y J-04 pasan sin fallos |

**Evidencia obligatoria:**
- Screenshots de cada paso de cada escenario
- Consultas SQL a `ibpms_bpmn_lane` e `ibpms_lane_role_assignment` mostrando datos reales
- Log de ejecución de J-02 y J-04 mostrando PASS

---

## 6. Mensaje de Despacho

> ⚠️ **IMPORTANTE:** Todo desarrollo debe quedar exhaustivamente documentado. Se exige **PRECISIÓN QUIRÚRGICA**. Cualquier daño a funcionalidades adyacentes será motivo de rechazo inmediato.

> 📝 **POLÍTICA ANTIAMNESIA:** Antes de iniciar, lee `docs/architecture/arquitecturar.md`.

> 📋 **DIRECTIVA DE INFRAESTRUCTURA HÍBRIDA:**
> El backend Spring Boot corre en consola local (NO en Docker) en el puerto `8080`. Antes de iniciar:
> 1. `curl -s http://localhost:8080/actuator/health` → Debe responder `{"status":"UP"}`.
> 2. Si no responde, arráncalo con: `cd backend && mvn spring-boot:run -pl ibpms-core -Dspring-boot.run.profiles=default`.
> 3. Verifica Docker: `docker ps` → PostgreSQL (`5433`), Redis (`6379`) y RabbitMQ (`5672`) deben estar `Up (healthy)`.
> **PROHIBIDO** levantar el backend vía Docker o modificar el `docker-compose.yml`.
>
> 🛑 **REGLA FUNDAMENTAL DE TEARDOWN DOCKER (SOLO PARA QA):**
> Si levantas contenedores temporales (ej. `docker-compose.e2e.yml`), DEBES garantizar su destrucción al finalizar o fallar. Configura `global-teardown.ts` que ejecute `docker compose -f docker-compose.e2e.yml down -v --remove-orphans`. PROHIBIDO dejar contenedores fantasma.

> **INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
> 1. Inicia en modo `PLANNING` y elabora un plan de trabajo en `implementation_plan.md`.
> 2. **PROHIBIDO pedirle al Humano que apruebe tu plan.**
> 3. Guarda tu solicitud de revisión en `.agentic-sync/approval_request_QA.md`.
> 4. Dile al Humano: *"Humano, he dejado mi solicitud en `.agentic-sync/approval_request_QA.md`. Ve al chat del Arquitecto Líder y regrésame su respuesta."*
> 5. Espera el veredicto. Si aprueba, pasa a `EXECUTION`.
> 6. Actualiza `docs/sprints/gobernanza_pm/CHANGELOG_NO_TECNICO.md` antes del commit.
> 7. `git commit` y `git push` en la rama `feature/lane-role-assignment`. PROHIBIDO git stash.

> 📚 **SKILLS OBLIGATORIOS:**
> - `.agents/skills/qa_e2e_validation_audit/SKILL.md` — Ley de Correspondencia Gherkin
> - `.agents/skills/clean_code_standards/SKILL.md`

### Archivos INTOCABLES
- Tests E2E existentes (J-02, J-04) — NO MODIFICAR, solo EJECUTAR para regresión
- `Workdesk.vue`, `Login.vue`, `FormDesigner.vue`
- `router/index.ts`, `docker-compose.yml`
- Todas las migraciones Liquibase existentes
