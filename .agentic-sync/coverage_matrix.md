# 📊 Matriz de Cobertura de Implementación (iBPMS V1)

> **Última actualización:** 2026-04-05 | **Responsable:** Arquitecto Líder
> **Leyenda:** ✅ Implementado | ⏳ En progreso | ❌ Pendiente | 🚫 Excluido (V2+) | 🔄 Remediación pendiente

## Instrucciones de Uso

1. **¿Quién actualiza esta matriz?** Cada agente de desarrollo (Backend/Frontend) DEBE marcar sus CAs como ✅ después de hacer `git commit` y `git push` (ver `agent_git_governance_policy.md` §2).
2. **¿Quién la audita?** El Arquitecto Líder ejecuta `/reconciliacionCoberturaCa.md` al cierre de cada Sprint para cruzar esta matriz contra `git log` y detectar falsos positivos.
3. **¿Cómo se lee?** Cada US tiene su tabla. Las columnas Back/Front/QA indican si esa capa fue implementada. La columna Handoff referencia el archivo de delegación.

---

## US-001: Bandeja de Entrada Unificada (Hybrid Workdesk)

| CA | Título (corto) | Back | Front | QA | Sprint | Handoff | Notas |
|----|----------------|------|-------|----|--------|---------|-------|
| CA-1 | Vista 360 Grid paginada | ✅ | ✅ | ✅ | S-1 | handoff_*_us001 | Validado en epic1_qa_remediation_report |
| CA-2 | Búsqueda Híbrida Reactiva | ✅ | ✅ | ✅ | S-1 | handoff_*_us001 | Remediación aplicada |
| CA-3 | Data Grid tabular 5 cols | ✅ | ✅ | ✅ | S-1 | handoff_*_us001 | Remediación aplicada |
| CA-4 | Toggle Delegación Mis Tareas/Equipo | ✅ | ✅ | ✅ | S-1 | handoff_*_us001 | Remediación aplicada |
| CA-5 | SLA Ticking Engine Vivo | ✅ | ✅ | ✅ | S-1 | handoff_*_us001 | Remediación aplicada |
| CA-6 | Ghost Deletion STOMP WebSocket | ✅ | ✅ | ✅ | S-1 | handoff_*_us001 | Remediación aplicada |
| CA-7 | Tolerancia Fallas CQRS | ✅ | ⏳ | ❌ | S-1 | handoff_backend_us001 | Backend completado |
| CA-8 | Anti-Cherry Picking Feature Flag | ✅ | ✅ | ✅ | S-1 | handoff_*_us001 | Remediación aplicada |

### Resumen US-001
- **Total CAs:** 8 | **✅ Completos (3 capas):** 6 (75%) | **⏳ Parcial:** 1 (12.5%) | **❌ Pendiente:** 1 (12.5%)

---

## US-003: IDE Web Low-Code para Formularios Inteligentes (iForm)

| Rango CA | Back | Front | QA | Sprint | Handoff |
|----------|------|-------|----|--------|---------|
| CA-1 a CA-20 | ⏳ | ⏳ | ❌ | — | Iteraciones previas (sin handoff explícito) |
| CA-21 a CA-25 | ✅ | ✅ | ❌ | S-2 | handoff_*_US003_CA21_CA25 |
| CA-26 a CA-30 | ✅ | ✅ | ❌ | S-2 | handoff_*_US003_CA26_CA30 |
| CA-31 a CA-35 | ✅ | ✅ | ❌ | S-2 | handoff_*_US003_CA31_CA35 |
| CA-36 a CA-40 | ✅ | ✅ | ❌ | S-2 | handoff_*_US003_CA36_CA40 |
| CA-41 a CA-45 | ✅ | ✅ | ❌ | S-2 | handoff_*_US003_CA41_CA45 |
| CA-46 a CA-50 | ✅ | ✅ | ❌ | S-2 | handoff_*_US003_CA46_CA50 |
| CA-51 a CA-54 | ✅ | ✅ | ❌ | S-2 | handoff_*_US003_CA51_CA54 |
| CA-55 a CA-59 | ✅ | ✅ | ❌ | S-2 | handoff_*_US003_CA55_CA59 |
| CA-60 a CA-64 | ✅ | ✅ | ❌ | S-2 | handoff_*_US003_CA60_CA64 |
| CA-65 a CA-69 | ✅ | ✅ | ❌ | S-2 | handoff_*_US003_CA65_CA69 |
| CA-87 | ✅ | ❌ | ❌ | S-69 | handoff_backend_us003_rem_ca87 |
| CA-88 | ✅ | ✅ | ❌ | S-69 | handoff_frontend_us003_rem_ca88 |
| CA-90 | ✅ | ✅ | ❌ | S-69 | handoff_frontend_us003_rem_ca90 |
| CA-91 | ✅ | ❌ | ❌ | S-69 | handoff_backend_us003_rem_ca91 |
| CA-92 | ✅ | ✅ | ❌ | S-69 | handoff_frontend_us003_rem_ca92 |
| CA-93 | ✅ | ✅ | ❌ | S-69 | handoff_frontend_us003_rem_ca93 |
| CA-70+ (otros) | ❌ | ❌ | ❌ | — | — |

> ⚠️ **Nota:** Los CAs CA-1 a CA-20 fueron implementados en iteraciones tempranas antes de la formalización del protocolo de handoffs. Requieren reconciliación con `git log`.

### Resumen US-003
- **CAs con Handoff explícito:** CA-21 a CA-69 (~49 CAs) | **Delegados Back+Front:** ✅ | **QA:** ❌ Pendiente
- **CAs sin Handoff:** CA-1 a CA-20, CA-70+ | **Estado:** Requiere reconciliación

---

## US-005: Modelador BPMN (Diseñador de Procesos)

| Rango CA | Back | Front | QA | Sprint | Handoff |
|----------|------|-------|----|--------|---------|
| CA-1 a CA-4 | ✅ | ✅ | ❌ | S-3 | handoff_*_US005_CA1_CA4 |
| CA-5 a CA-6 | ✅ | ✅ | ❌ | S-3 | handoff_*_US005_CA5_CA6 |
| CA-7 a CA-10 | ✅ | ✅ | ❌ | S-3 | handoff_*_US005_CA7_CA10 |
| CA-11 a CA-15 | ✅ | ✅ | ❌ | S-3 | handoff_*_US005_CA11_CA15 |
| CA-16 a CA-20 | ✅ | ✅ | ❌ | S-3 | handoff_*_US005_CA16_CA20 |
| CA-21 a CA-25 | ✅ | ✅ | ❌ | S-3 | handoff_*_US005_CA21_CA25 |
| CA-26 a CA-30 | ✅ | ✅ | ❌ | S-3 | handoff_*_US005_CA26_CA30 |
| CA-31 a CA-35 | ✅ | ✅ | ❌ | S-3 | handoff_*_US005_CA31_CA35 |
| CA-36 a CA-40 | ✅ | ✅ | ❌ | S-3 | handoff_*_US005_CA36_CA40 |
| CA-41 a CA-45 | ✅ | ✅ | ❌ | S-3 | handoff_*_US005_CA41_CA45 |
| CA-46 a CA-50 | ✅ | ✅ | ❌ | S-3 | handoff_*_US005_CA46_CA50 |
| CA-51 a CA-55 | ✅ | ✅ | ❌ | S-3 | handoff_*_US005_CA51_CA55 |
| CA-56 a CA-59 | ✅ | ✅ | ❌ | S-3 | handoff_*_US005_CA56_CA59 |
| CA-60 a CA-62 | ✅ | ✅ | ❌ | S-3 | handoff_*_US005_CA60_CA62 |

### Resumen US-005
- **Total CAs con Handoff:** ~62 | **Delegados Back+Front:** ✅ | **QA:** ❌ Pendiente total

---

## US-017 (ex US-029): Persistencia Hexagonal CQRS y Task Completion

| CA | Título (corto) | Back | Front | QA | Sprint | Notas |
|----|----------------|------|-------|----|--------|-------|
| CA-1 | Enviar datos válidos POST /complete | ❌ | ❌ | ❌ | — | Nuevo (refactored) |
| CA-2 | Validación JSON Schema 400 | ❌ | ❌ | ❌ | — | Nuevo (refactored) |
| CA-3 | Inyección BFF Megalítica | ❌ | ❌ | ❌ | — | Refactored de US-029 |
| CA-4 | Lazy Patching V1→V2 | ❌ | ❌ | ❌ | — | Refactored de US-029 |
| CA-5 | Upload-First + Anti-IDOR | ❌ | ❌ | ❌ | — | 🔄 Remediación |
| CA-6 | Draft Sync + Cifrado PII LS | ❌ | ❌ | ❌ | — | 🔄 Remediación |
| CA-7 | RYOW Consistencia Eventual | ❌ | ❌ | ❌ | — | 🔄 Remediación |
| CA-8 | Idempotencia Anti-Doble-Clic | ❌ | ❌ | ❌ | — | 🔄 Remediación |
| CA-9 | Zod Isomórfico Guillotina | ❌ | ❌ | ❌ | — | Refactored de US-029 |
| CA-10 | Micro-Tokens Anti-Replay | ❌ | ❌ | ❌ | — | 🔄 Remediación |
| CA-11 | Implicit Locking Concurrencia | ❌ | ❌ | ❌ | — | Refactored de US-029 |
| CA-12 | CQRS Event Sourcing | ❌ | ❌ | ❌ | — | Refactored de US-029 |
| CA-13 | Exclusión Topológica Camunda | ❌ | ❌ | ❌ | — | Refactored de US-029 |
| CA-14 | ACID Fallback Saga Inverso | ❌ | ❌ | ❌ | — | Refactored de US-029 |
| CA-15 | Auto-Claim Group-Level | ❌ | ❌ | ❌ | — | 🔄 Remediación |
| CA-16 | Trazabilidad Rechazos BFF | ❌ | ❌ | ❌ | — | 🔄 Remediación |

### Resumen US-017
- **Total CAs:** 16 | **✅ Completos:** 0 | **🔄 Remediación:** 6 | **❌ Pendiente:** 16 (100%)

---

## US-028: Auto-Generación de Test Suites Zod/Vitest

| Rango CA | Back | Front | QA | Sprint | Handoff |
|----------|------|-------|----|--------|---------|
| CA-1 a CA-4 | ✅ | ✅ | ❌ | S-4 | handoff_*_US028_CA1_CA4 |
| CA-4 a CA-6 | ✅ | ✅ | ❌ | S-4 | handoff_*_US028_CA4_CA6 |
| CA-7 a CA-9 | ✅ | ✅ | ❌ | S-4 | handoff_*_US028_CA7_CA9 |
| CA-10 a CA-11 | ✅ | ✅ | ❌ | S-4 | handoff_*_US028_CA10_CA11 |

### Resumen US-028
- **Total CAs con Handoff:** ~11 | **Delegados Back+Front:** ✅ | **QA:** ❌ Pendiente

## US-036: RBAC, Zero-Trust y Gobernanza de Seguridad (ISO 27001)

| CA | Título (corto) | Back | Front | QA | Sprint | Handoff / Notas |
|----|----------------|------|-------|----|--------|-----------------|
| CA-6 | Roles VIP Visuales (Pantalla 14) | ✅ | ✅ | ✅ | S-3 | Backend OK / Frontend UI Insignias Integrado |
| CA-19 | Liquibase Schema Roles/Permisos | ✅ | ❌ | ✅ | S-3 | handoff_backend_DEF02_DEF03 / Backend OK |
| CA-20 | RLS Interceptor AOP (assignee_id) | ✅ | ❌ | ✅ | S-3 | Backend OK |
| CA-21 | Kill Session & Dummy JWT Blacklist | ✅ | ❌ | ✅ | S-3 | Backend OK |
| CA-22 | Service Accounts API Keys (SHA-256) | ✅ | ✅ | ✅ | S-3 | Backend OK / UI Modal Integrado |
| CA-23 | Lazy Evaluation Tareas Delegadas | ✅ | ❌ | ✅ | S-3 | Backend OK |
| CA-24 | Reporte Generador ISO 27001 | ✅ | ✅ | ✅ | S-3 | Backend OK / Botón Descarga CSV Integrado |
| CA-25 | Trazabilidad Inmutable (Audit Trail) | ✅ | ❌ | ✅ | S-3 | Backend completado implícitamente mediante logs sudoers |

### Resumen US-036
- **Total CAs con Handoff Backend:** 7 (CA-19 al CA-25) | **Delegado Back:** ✅ 100% | **Front:** ✅ Parcial (CA-22, CA-24) | **QA:** ✅ 100% Completado

---

## Otras US con Handoffs

| US | Handoff / CAs | Back | Front | QA | Notas |
|----|---------------|------|-------|----|-------|
| US-034 | CA-4 a CA-10 | ✅ | ✅ | ✅ | Remediación Dashboard DLQ (CA-8 Frontend validado) |
| US-038 | 3 partes (p1-p3) | ✅ | ✅ | ❌ | Dashboard/BAM |
| US-039 | CA-4 a CA-8 | ✅ | ✅ | ✅ | Formulario Genérico Base (Hardening OBS-1, OBS-2 Frontend OK) / QA Gatekeeper Red Stage Activo |
| US-043 | 1 handoff + CA6 deuda | ✅ | ✅ | ❌ | Deuda técnica pendiente |
| US-048 | 1 handoff | ✅ | ✅ | ❌ | — |

> ⚠️ Estas US requieren desglose CA-a-CA detallado por el Arquitecto en la próxima reconciliación.

---

## Resumen Global de Cobertura

| Métrica | Valor |
|---------|-------|
| **US con desarrollo iniciado** | 9 (US-001, 003, 005, 017, 028, 036, 038, 039, 043, 048) |
| **CAs con handoff Backend+Frontend** | ~187+ (estimado) |
| **CAs validados por QA** | ~25 (US-001, US-034, US-036, US-039 parcial) |
| **Principal Brecha** | 🔴 **QA es bajo en US tempranas (US-003, US-005, US-017).** |
| **US-017 (CQRS)** | 🔴 **0% implementado (16 CAs pendientes, 6 de remediación)** |

---

> **⚡ Próxima acción recomendada:** Ejecutar `/reconciliacionCoberturaCa.md` sobre US-003 y US-005 para granularizar los rangos de CA y cruzar contra `git log --grep="CA-"`.
