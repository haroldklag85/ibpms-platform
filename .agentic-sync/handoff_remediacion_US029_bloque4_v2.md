# 🏛️ ACTA DE CIERRE — US-029: Bloque 4 (Deuda Técnica V2)
# Documentación Formal de GAPs Diferidos
**Fecha:** 2026-05-03 | **Iteración:** 5 | **Arquitecto Líder:** Antigravity

---

## 📊 Estado de la US-029 Completa

| Bloque | GAPs | Estado | Fecha |
|--------|------|--------|-------|
| B1 | 14 | ✅ CERTIFICADO (14/14 QA PASS) | 2026-05-03 |
| B2 | 10 | ✅ CERTIFICADO (10/10 QA PASS) | 2026-05-03 |
| B3 | 7 | ✅ CERRADO (consolidado en B2) | 2026-05-03 |
| **B4** | **4** | **🔵 DIFERIDO V2** — este documento | 2026-05-03 |
| **Total** | **24 PASS + 4 V2** | **Sprint 6: CERTIFICADA** | — |

---

## 🔵 GAP-02: Micro-Tokens JWT para Validaciones Asíncronas (CA-06)

**Severidad:** V2 | **Dependencia bloqueante:** Redis (no disponible en V1)

### Descripción
Generar tokens JWT efímeros (TTL 60s) con claims específicos (`taskId`, `formSchemaVersion`, `jti`) para validaciones asíncronas externas (ej: llamadas a servicios de verificación crediticia, validación de identidad).

### Especificación Técnica V2
```
Componente: MicroTokenService.java
Dependencia: spring-boot-starter-data-redis + jjwt
Flujo:
  1. Frontend solicita micro-token: GET /api/v1/forms/{taskId}/micro-token
  2. Backend genera JWT con claims: { taskId, userId, schemaVersion, jti: UUID, exp: now+60s }
  3. Frontend envía token a servicio externo para validación
  4. Servicio externo retorna resultado al Backend con el token
  5. Backend valida token (no expirado, jti no consumido en Redis)
  6. Redis SET jti:consumed EX 300 (previene replay)
```

### Prerequisitos V2
- [ ] Redis disponible en infraestructura
- [ ] Definir catálogo de servicios externos que requieren micro-tokens
- [ ] ADR para política de TTL y rotación de signing keys

---

## 🔵 GAP-07: Anti-Replay con JTI + Redis (CA-14)

**Severidad:** V2 | **Dependencia bloqueante:** Redis (no disponible en V1)

### Descripción
Prevenir ataques de replay donde un token de completado es interceptado y reenviado. Cada submit genera un `jti` (JWT ID) único que se invalida en Redis tras el primer uso.

### Especificación Técnica V2
```
Componente: AntiReplayFilter.java (Spring Security Filter)
Dependencia: spring-boot-starter-data-redis
Flujo:
  1. Frontend genera jti = crypto.randomUUID() antes del submit
  2. POST /complete incluye header X-Idempotency-Key: {jti}
  3. Backend verifica: Redis GET replay:{jti}
     - Si existe → HTTP 409 "Request already processed"
     - Si no existe → Redis SET replay:{jti} EX 3600 + procesar
  4. Diferencia con IdempotencyPort actual:
     - IdempotencyPort usa JPA (persistente, lento)
     - AntiReplayFilter usa Redis (efímero, rápido, TTL automático)
```

### Prerequisitos V2
- [ ] Redis disponible en infraestructura
- [ ] Migrar IdempotencyJpaAdapter a Redis (o mantener dual-write)
- [ ] Definir TTL de ventana anti-replay (sugerido: 1h)

### Mitigación actual V1
`CompletarTareaService.java` L28-31 implementa idempotencia vía JPA (`IdempotencyPort.existe()`). Es funcional pero más lento y no tiene auto-expiración.

---

## 🔵 GAP-11: Reconciliación Formal US-029/US-017 (CA-19)

**Severidad:** DOC | **Dependencia:** Ninguna (solo documentación)

### Descripción
Documentar formalmente la política de propiedad entre US-029 (Ejecución de Formulario) y US-017 (Asignación de Tareas) para evitar conflictos de ownership en campos compartidos.

### Especificación Técnica V2
```
Documento: docs/architecture/ADR_reconciliation_US029_US017.md
Contenido:
  1. US-017 es OWNER de: assignee, candidateGroups, claimTimestamp
  2. US-029 es OWNER de: formPayload, draftPayload, schemaVersion, attachments
  3. Campos compartidos con regla LAST-WRITER-WINS:
     - status: US-017 controla CLAIMED/UNCLAIMED, US-029 controla DRAFT/COMPLETED
     - lastActivityAt: ambos escriben, gana el timestamp más reciente
  4. Prohibición: US-029 NUNCA modifica assignee. US-017 NUNCA modifica formPayload.
```

### Acción recomendada
Se puede cerrar en V1 creando el ADR sin cambios de código. Es puramente documentación de gobierno.

---

## 🔵 GAP-14: Wizard Multi-Step para iForms Maestro (CA-22)

**Severidad:** V2 | **Dependencia:** US-003 iForm Maestro fase 2

### Descripción
Implementar flujo wizard multi-step con barra de progreso, navegación [Anterior]/[Siguiente], y validación por paso. Aplica SOLO para formularios tipo iForm Maestro (multi-sección). El formulario genérico (US-039) es single-step por definición.

### Especificación Técnica V2
```
Componente: FormWizard.vue
Props: { steps: FormStep[], currentStep: number }
FormStep: { id: string, title: string, fields: ZodSchema, isValid: boolean }

UI:
  - Barra de progreso horizontal con pasos numerados
  - Cada paso se valida con Zod antes de permitir avanzar
  - [Siguiente] → valida paso actual → si OK avanza
  - [Anterior] → navega sin validar (preserva datos)
  - Último paso: [Enviar] reemplaza [Siguiente]
  - Estado de cada paso: ⬜ pendiente | 🔵 actual | ✅ completo | ❌ con errores

Integración con Draft:
  - autoSaveDraft() guarda { currentStep, stepsData: { [stepId]: payload } }
  - Al restaurar draft, posiciona en el último paso editado
```

### Prerequisitos V2
- [ ] US-003 iForm Maestro fase 2 implementada (definición de secciones/pasos)
- [ ] Schema Zod por sección (no monolítico)
- [ ] UX review de la barra de progreso

---

## 📌 Instrucciones para los Agentes (Sin acción requerida en Sprint 6)

Este Bloque 4 **NO genera handoffs ejecutables**. Los 4 GAPs quedan documentados como deuda técnica formal con las siguientes condiciones de activación:

| GAP | Condición de Activación |
|-----|------------------------|
| GAP-02 | Redis disponible en infraestructura de producción |
| GAP-07 | Redis disponible + catálogo de servicios externos definido |
| GAP-11 | Puede cerrarse en cualquier momento (solo ADR) |
| GAP-14 | US-003 iForm Maestro fase 2 completada |

**Ningún agente debe implementar estos GAPs en Sprint 6.**

---

## ✅ Veredicto Final US-029

**US-029 "Ejecución y Envío de Formulario" queda CERTIFICADA para Sprint 6.**

- 34 Criterios de Aceptación auditados
- 24 GAPs remediados y certificados (QA PASS)
- 4 GAPs diferidos con especificación técnica V2 completa
- 6 CAs con cobertura previa (detectados en auditoría original)
- 0 bloqueantes abiertos
