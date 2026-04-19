# Sprint 5.1 — Cierre de Deuda Técnica (Remediación de Seguridad y Persistencia)

> **Inicio:** 2026-04-18 | **Tipo:** Mini-Sprint Focalizado
> **Rama:** `sprint-5/iteracion4` | **Arquitecto Líder:** Orquestador
> **Prerrequisito:** Sprint 5 (Iteraciones 1–4) cerrado y mergeado

---

## 1. Objetivo del Sprint

Remediar las brechas **críticas de seguridad y persistencia** identificadas durante la auditoría de reconciliación del Sprint 5. Este sprint NO introduce funcionalidades nuevas — solo cierra la deuda técnica documentada en `coverage_matrix.md` para las User Stories US-002, US-007 y US-029.

### Metas de Cobertura

| User Story | Antes (Sprint 5) | Objetivo (Sprint 5.1) | Delta |
|------------|:-----------------:|:---------------------:|:-----:|
| US-002 (Claim Task) | ~9% | ~50% | +41% |
| US-007 (DMN Generator) | ~48% | ~75% | +27% |
| US-029 (Form Submit) | ~55% | ~80% | +25% |

---

## 2. Alcance (21 CAs Seleccionados)

### 2.1 US-002 — Reclamar una Tarea de Grupo (6 CAs)

| CA | Título | Prioridad | Brecha Actual | Acción |
|:--:|--------|:---------:|---------------|--------|
| CA-1 | Reclamo Simultáneo | 🔴 P0 | `assignee` hardcodeado `"e2e_user"`, BD comentada | Inyectar `userId` de JWT; descomentar persistencia BD |
| CA-11 | Bloqueo Atómico BD | 🔴 P0 | Solo Redis SETNX, sin `SKIP LOCKED` | Activar `findByIdForUpdate()` del repo JPA existente |
| CA-5 | Modo Solo Lectura | 🟡 P1 | Sin endpoint read-only | `GET /api/v1/workbox/tasks/{id}/preview` |
| CA-6 | Ghost Job Timeout | 🟡 P1 | Umbral no configurable por tenant | Parametrizar `AutoClaimService` con `@ConfigurationProperties` |
| CA-8 | Despojo Forzoso Supervisor | 🟡 P1 | Endpoint no existe | `POST /force-unclaim` con validación `ROLE_SUPERVISOR` |
| CA-9 | Trazabilidad Forense | 🟡 P1 | Sin tabla ni endpoint de auditoría | Tabla `claim_audit_log` + `GET /audit-trail` |

### 2.2 US-007 — Generador Cognitivo DMN (9 CAs)

| CA | Título | Prioridad | Brecha Actual | Acción |
|:--:|--------|:---------:|---------------|--------|
| CA-6 | IDOR Activo | 🔴 P0 | `tenantId` hardcodeado en Controller | Inyectar `tenantId` desde JWT `SecurityContext` |
| CA-2 | Caché Multi-Tenant Rota | 🔴 P0 | Caché no segmentada por tenant | Segmentar por `tenantId` real |
| CA-5 | PII en Prompt | 🟡 P1 | Sin seudonimización | Pre-procesamiento PII antes del LLM |
| CA-3 | GC Borradores XML | 🟡 P1 | Sin scheduler de limpieza | Implementar `DmnDraftCleanupScheduler` (30 días) |
| CA-4 | Sandboxing XSS | 🟡 P1 | XSS en render DOM no verificado | Sanitizar XML antes de renderizar en canvas Vue |
| CA-13 | Persistencia Dual Borradores | 🟡 P1 | No existe | BD + Redis para resiliencia |
| CA-14 | Endpoint Simulador | 🟡 P1 | No existe | `POST /api/v1/dmn/simulate` funcional |
| CA-15 | Invalidación Caché Redis | 🟡 P1 | No existe | Invalidar caché al actualizar DMN |
| CA-16→18 | Catálogo DMN + Contrato API | 🟡 P1 | No existe | Listado + CRUD básico + OpenAPI annotations |

### 2.3 US-029 — Submit de Formulario (6 CAs)

| CA | Título | Prioridad | Brecha Actual | Acción |
|:--:|--------|:---------:|---------------|--------|
| CA-5 | BFF con Mock | 🔴 P0 | `mockEventSourcingRepository` activo | Conectar `FormBffCoreService` a BD real |
| CA-2 | Zod campo-a-campo | 🔴 P0 | Errores no detallados por campo | Retornar `errors[]` RFC 7807 con detalle por campo |
| CA-3 | TTL LocalStorage QA | 🟡 P1 | Sin test de expiración | Test E2E de limpieza de borradores |
| CA-4 | Saga Camunda QA | 🟡 P1 | Sin test de compensación | Test de integración simulando fallo post-persist |
| CA-6 | Owner Check QA | 🟡 P1 | Sin test de rechazo 403 | Test con userId ≠ assignee → HTTP 403 |
| CA-7 | Implicit Locking QA | 🟡 P1 | Sin test de verificación | Validar bloqueo en `FormCompletionService` |

---

## 3. CAs Explícitamente Diferidos (V2)

| US | CAs | Justificación |
|----|:---:|---------------|
| US-002 | CA-2, CA-4, CA-7, CA-10, CA-14 | Funcionalidad nueva (bulk-claim, mensaje, offline, OpenAPI) |
| US-007 | CA-8, CA-9 | Validación avanzada DMN — requiere investigación |
| US-029 | CA-25→34 | Refinamiento UX — no bloquea producción |

---

## 4. Ejecución (1 Iteración, 3 Agentes Secuenciales)

| Paso | Agente | Scope | Dependencia |
|:----:|--------|-------|:-----------:|
| 1️⃣ | **Backend** | P0: JWT injection, BD real, IDOR fix. P1: force-unclaim, audit-trail, GC, PII, BFF real, simulador | Ninguna |
| 2️⃣ | **Frontend** | Modo read-only, sanitización XSS canvas, feedback Zod campo-a-campo | ✅ Backend pusheado |
| 3️⃣ | **QA** | Tests integración: Saga, Owner Check, TTL, audit-trail + reconciliación | ✅ Frontend pusheado |

---

## 5. Quality Gates

- **Backend:** `mvn clean package` BUILD SUCCESS
- **Frontend:** `npm run test:unit` (100% green) + `npm run build` (0 errores)
- **QA:** Reconciliación final vs `coverage_matrix.md` con objetivos de cobertura cumplidos

---

## 6. Gobernanza

- Todos los agentes aplican workflows: `cierreDeudaTecCriteriosAceptacion.md`, `router_certificacion_qa.md`, `reconciliacionCoberturaCa.md`
- Skills obligatorios: TDD-First, Clean Code, Zero-Trust SRE (Backend), Frontend Build Audit
- Al cierre: actualizar `coverage_matrix.md` y generar acta `cierre_sprint_5_1.md`
