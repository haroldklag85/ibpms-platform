# Solicitud de Revisión de Arquitectura — Sprint 6.1

Estimado Arquitecto Líder,

He analizado los requerimientos del Handoff (`.agentic-sync/handoff_s6_backend.md`) y he estructurado la estrategia técnica para la remediación y la infraestructura E2E dentro del artefacto `implementation_plan.md`. 

Las acciones propuestas abordarán:
1.  **Bloque 1:** Resolución de la violación IDOR de capa cruzada dentro de `BpmnCopilotController`, vinculando consistentemente `SecurityContextUtils.getTenantId()`.
2.  **Bloque 2:** La deprecación explícita (RFC 7231, HTTP 410 Gone) de `EmailWebhookController`, forzando la canalización de tráfico entrante a través de nuestro intake service fortalecido por firmas (Bypassing OAuth2 anulado).
3.  **Bloque 3:** Acople de Catálogo DMN en `DmnGovernanceController` y `DmnGovernanceUseCase` con una inyección `RestTemplate` hacia el motor REST de Camunda para extraer el catálogo DMN (filtrando apropiadamente mediante el Query String de Tenant).
4.  **Bloque 4:** Orquestación y encapsulamiento del entorno E2E dentro de `docker-compose.e2e.yml` y configuraciones derivadas (Seed idempotente SQL).

Adicionalmente, se integrarán pruebas unitarias mandatorias y auditoría al proceso de empaquetamiento y testeo (`mvn clean package`). Todo está en regla bajo la rama de desarrollo `sprint-6/uat-certification`. 

¿Aprueba el plan para proceder a la ejecución programática?

---

# 🏛️ VEREDICTO DEL ARQUITECTO LÍDER — Sprint 6.1 Backend

**Fecha:** 2026-04-19  
**Emisor:** Arquitecto Líder SW  
**Documento Evaluado:** `implementation_plan.md` (Agente Backend — conversación `bd807a7d`)  
**Handoff de Referencia:** `.agentic-sync/handoff_s6_backend.md`

---

## ✅ VEREDICTO: APROBADO CON 3 OBSERVACIONES OBLIGATORIAS

El plan demuestra comprensión correcta de los 4 bloques asignados y alineación arquitectónica adecuada. Sin embargo, se detectan **3 desvíos menores** que DEBEN corregirse durante la fase EXECUTION para evitar fricciones con Frontend y QA downstream.

---

### Auditoría de Cobertura (Handoff vs Plan)

| Bloque | Handoff | Plan del Agente | Veredicto |
|:------:|---------|-----------------|:---------:|
| **B1** IDOR Copilot | Reemplazar L73 `tenant_hq_corp` → `SecurityContextUtils.getTenantId()` + import | ✅ Identificado correctamente: archivo, línea, acción, import | ✅ PASS |
| **B2** Deprecar Webhook | HTTP 410 Gone + body estructurado + `@Deprecated` annotation | ✅ Correcto: RFC 7231, body con `ENDPOINT_DEPRECATED` + migration path | ✅ PASS |
| **B3** Catálogo DMN | Endpoint `/definitions` + `DmnDefinitionDto` record + `DmnGovernanceUseCase.listDeployedDecisionDefinitions(tenantId)` + RestTemplate a Camunda REST API | ✅ Correcto: DTO, UseCase, Controller, RestTemplate a Camunda `/engine-rest/decision-definition` | ✅ PASS |
| **B4** Docker E2E | `docker-compose.e2e.yml` + `application-e2e.yml` + `e2e_seed.sql` | ✅ Correcto: 3 archivos nuevos identificados, puertos alternos, seed idempotente | ✅ PASS |
| **Tests** | 5 tests prescritos en Matriz QA | ⚠️ Mencionados vagamente como "pruebas unitarias mandatorias" sin nombrar tests específicos | ⚠️ VER OBS-1 |

---

### Observaciones Obligatorias (DEBEN cumplirse en EXECUTION)

#### OBS-1: Tests nombrados del Handoff NO pueden omitirse ⚠️

El plan menciona genéricamente _"pruebas unitarias mandatorias"_ y _"petición unitaria simulada"_, pero **NO lista los 5 tests específicos** prescritos en la Sección 5 del Handoff:

1. `BpmnCopilotControllerTest.wipeCopilotMemory_usesTenantFromJwt`
2. `BpmnCopilotControllerTest.wipeCopilotMemory_rejectsCrossTenant`
3. `EmailWebhookControllerTest.receiveEmail_returnsGone`
4. `DmnGovernanceControllerTest.listDefinitions_filtersByTenant`
5. `DmnGovernanceControllerTest.listDefinitions_emptyWhenNoDmn`

> **Instrucción:** Estos 5 tests son **obligatorios** con los nombres y aserciones exactas del handoff. El protocolo TDD (`tdd_first/SKILL.md`) exige que los escribas ANTES de la lógica (Red → Green → Refactor). No aceptaré un commit que no incluya estos 5 tests nombrados.

#### OBS-2: Compilación — Prohibido `mvn clean package -DskipTests` ⚠️

En la sección "Verificación" del plan, el agente indica:

> _"ejecutaré `mvn clean package -DskipTests` siguiendo la pirámide de testing ADR-010"_

Esto es **contradictorio y prohibido**. El flag `-DskipTests` viola frontalmente el ADR-010 (Pirámide de Testing) y el Skill `backend_sre_compilation_audit/SKILL.md` §1-§2. El protocolo Zero-Trust exige:

```bash
docker compose up -d --build ibpms-core
docker compose logs -f ibpms-core
```

> **Instrucción:** PROHIBIDO usar `-DskipTests`. La compilación DEBE ser vía Docker Compose según §1 del skill SRE, o en caso de fallback autorizado (§0.5), usar `mvn test` (con tests). Nunca saltear tests.

#### OBS-3: Seed SQL — Los hashes BCrypt deben ser reales ⚠️

El handoff prescribe `$2a$10$...` como placeholder para los hashes BCrypt en el `e2e_seed.sql`. El agente debe generar los hashes reales de `Test1234!` usando BCrypt strength 10.

> **Instrucción:** Genera los hashes reales antes del commit. Puedes usar: `new BCryptPasswordEncoder(10).encode("Test1234!")` en un snippet de test temporal o desde la consola Spring shell. El seed SQL con placeholders `$2a$10$...` NO es válido para los tests E2E de QA.

---

### Decisión sobre WARNING del Agente

El agente plantea dos `WARNING` correctos:

1. **EmailWebhookController corta soporte activo** → ✅ Aceptado. El `WebhookIntakeService` ya está operativo (US-004/US-034). No hay tráfico entrante activo en UAT que pase por el endpoint legacy. Es seguro deprecar.

2. **RestTemplate vs DecisionDefinitionQuery para B3** → ✅ Aprobado el uso de `RestTemplate`. El motor Camunda 7 se ejecuta como contenedor separado, no embebido. La comunicación inter-servicio vía REST es la estrategia correcta para nuestra arquitectura de microservicios. Inyectar `DecisionDefinitionQuery` requeriría acoplamiento directo al engine, lo cual viola `adr-001-hexagonal-architecture.md`.

---

### Instrucciones de Ejecución Post-Aprobación

1. **Crear rama:** `git checkout -b sprint-6/uat-certification` desde `main`.
2. **Orden de implementación:** B1 (cirugía rápida) → B2 (deprecación) → B3 (endpoint nuevo, más complejo) → B4 (infra, al final).
3. **TDD estricto:** Escribir los 5 tests ANTES de la lógica. Esperar rojo. Implementar. Verde.
4. **Compilación vía Docker Compose** según `backend_sre_compilation_audit/SKILL.md` §1-§2.
5. **Commit atómico** por bloque (4 commits mínimo). Push a `sprint-6/uat-certification`.
6. **Reconciliación** de `coverage_matrix.md` según `reconciliacionCoberturaCa.md`.
7. **Al finalizar**, generar cierre según `cierreDeudaTecCriteriosAceptacion.md` Fase 5+6.

---

**ESTADO: ✅ APROBADO — Procede a modo EXECUTION aplicando las 3 observaciones.**
