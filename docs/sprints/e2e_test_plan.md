# E2E Test Plan — iBPMS V1 (Alt B: UAT Driven)

> **Framework:** Playwright v1.59+ (Chromium)  
> **Entorno:** Docker Nativo (Vite dev server `:3000` → proxy `/api` → Spring Boot `:8080/api/v1`)  
> **Ejecución:** `npx playwright test` desde `/frontend`  
> **Última Actualización:** 2026-04-16 (Estrategia UAT Code Freeze)  
> **Status:** Adaptado para la Estrategia B (Test Pyramid)

---

## Cambio Estratégico (Code Freeze)

Bajo la nueva arquitectura de Sprints, Playwright queda **inhabilitado temporalmente durante el Sprint 1** (dedicado exclusivamente a blindar las APIs y la lógica vía Unit/Integration DB Tests). 
Toda la carga de E2E ha sido centralizada en el **Sprint 2 (UAT Automatizado)**.

---

## Estructura de Directorios E2E (Mapeados a US)

```
frontend/e2e/
├── smoke/                      ← Infraestructura Base (Sprint 0)
│   └── app-loads.spec.ts       ✅ IMPLEMENTADO
├── uat-j04-workdesk/           ← Operativa (US-001, US-002)
│   ├── workdesk-sla.spec.ts    ← Tareas ordenadas y Semáforos
│   ├── task-claim.spec.ts      ← US-002 Atender/Liberar + WebSockets
│   └── form-execution.spec.ts  ← Flujo básico UI
├── uat-j02-modeler/            ← Constructores (US-003, US-005, US-028, US-039)
│   ├── bpmn-designer.spec.ts   
│   ├── form-builder.spec.ts    
│   └── code-gen-zod.spec.ts    
├── uat-j03-admin/              ← Seguridad & IdP (US-036, US-038, US-048)
│   ├── rbac-isolation.spec.ts  ← Gaslighting (404), FOUC prevention
│   └── mfa-internal.spec.ts    
└── helpers/
    ├── auth.helpers.ts         ← JWT mock estricto (Roles Admin vs User)
    └── fixtures/               
```

---

## Planeación de Ejecución de Playwright

### Sprint 0 — Gate de Infraestructura
**Objetivo:** Garantizar que el Stack Docker corre.
- S0-SMOKE-01: Backend DB Responds (✅ Completado)
- S0-SMOKE-02: Vue UI env file parse (✅ Completado)

### Sprint 1 — API Test Pyramid
**Estado:** `OMITIDO PARA PLAYWRIGHT`. 
Durante S1, el Agente QA se apoya en Postman/REST Assured y Vitest. No hay desarrollo E2E.

### Sprint 2 — Gate UAT Maestro (Maratón E2E)
**Objetivo:** Guiados por la matriz `uat_rtm_matrix.md`, probar los casos de uso manuales en la pantalla usando Playwright contra la DB de test.

| Test File (UAT) | RTM Requisito | US Evaluada |
|-----------------|---------------|-------------|
| `task-claim.spec.ts` | UAT-J04-01 | US-002 |
| `workdesk-sla.spec.ts` | UAT-J04-02 | US-001 |
| `rbac-isolation.spec.ts` | UAT-J03-01 | US-036, US-038 |
| `bpmn-designer.spec.ts` | UAT-J02-01 | US-005 |
| `form-builder.spec.ts` | UAT-J02-02 | US-003, US-028 |

---

## Patrones de Calidad (No-Negociables)

1. **Cero-Mocks Dinámicos:** Playwright debe probar conectándose a la Base de Datos H2/PostgreSQL-Test real del backend. Las aserciones mockeadas `route.fulfill` quedan prohibidas excepto para llamadas externas reales (API de Microsoft Graph / EntraID).
2. **Screenshots por Gate:** Playwright debe generar las fotos de evidencia que permitan al "Jefe de Equipo" firmar visualmente el UAT sin tener que correr la máquina entera.

```typescript
// Extraer evidencia fotográfica
await page.screenshot({
    path: `test-results/evidencia-uat-us001.png`,
    fullPage: true
});
```

---

## Historial de Cambios

| Fecha | Cambio | Autor |
|-------|--------|-------|
| 2026-04-13 | Creación inicial | Arquitecto Lead |
| 2026-04-16 | Adaptación Drástica a Test Pyramid. S1 Removido de E2E. Centralización en Sprint 2 de todo UAT. | Arquitecto Lead |
