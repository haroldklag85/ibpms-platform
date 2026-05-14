# Solicitud de Aprobación — Agente QA SDET

**Fecha:** 2026-05-14  
**De:** Agente QA SDET  
**Para:** Arquitecto Líder  
**Asunto:** Plan de Certificación E2E para US-038 (CA-01 al CA-05)

---

## Resumen del Plan

He completado la fase PLANNING para la US-038. Mi plan consiste en crear un único script `us-038-security-edge-cases.spec.ts` que certifique 5 Criterios de Aceptación de Edge Cases de Ciberseguridad:

| CA | Título | Estrategia | Capa |
|----|--------|-----------|------|
| CA-01 | Fail-Open Policy | API: Verificar GET exitoso con Redis activo + Kill-Session funcional | Red/Backend |
| CA-02 | Anti-Token Bloat | API: Decodificar JWT y validar prefijo `ibpms_rol_` en todos los roles | Red/Seguridad |
| CA-03 | JIT Guardrail 428 | API: POST `/auth/sync` con token sin claims → HTTP 428 | Red/Backend |
| CA-04 | Break-Glass Protocol | UI: Navegar a `/login`, activar Break-Glass, llenar formulario con justificación | UI/DOM/Red |
| CA-05 | Aditividad RBAC | API: Inspeccionar JWT multi-rol, confirmar fusión sin conflictos | Red/Seguridad |

## Hallazgos Pre-Identificados

### ⚠️ BUG POTENCIAL (CA-04)
**`BreakGlassLogin.vue` línea 97** apunta a `/api/v1/auth/emergency/login` (con slash), pero el backend (`AuthSyncController.java` línea 90) expone `/api/v1/auth/emergency-login` (con guión). Esto causará un **HTTP 404** en la UI cuando un usuario intente el Break-Glass.

**Recomendación:** Corregir la ruta en `BreakGlassLogin.vue` a `/api/v1/auth/emergency-login` antes de la ejecución E2E.

### ⚠️ LIMITACIÓN (CA-01)
No es posible detener Redis desde Playwright sin acceso SSH al contenedor. La validación Fail-Open se limitará a confirmar la operación correcta con Redis activo.

## Estado del Entorno
- ✅ PostgreSQL: Healthy (puerto 5433)
- ✅ Redis: Healthy (puerto 6379)
- ✅ Backend (ibpms-core): Up (puerto 8080, API respondiendo)
- ⚠️ Frontend: Caído — necesita `npm run dev` antes de ejecutar tests UI
- ⚠️ RabbitMQ: Unhealthy (no impacta US-038)

## Solicitud Formal

Arquitecto Líder, solicito su aprobación para pasar a modo **EXECUTION** y ejecutar este plan. En particular:

1. ¿Aprueba la corrección de ruta del Break-Glass (`/emergency/login` → `/emergency-login`)?
2. ¿Confirma que puedo proceder con la validación parcial del CA-01 (sin detener Redis)?
3. ¿Alguna observación o ajuste al plan?

Quedo a la espera de su veredicto formal.

---
*Archivo generado automáticamente por el Agente QA SDET según protocolo del handoff.*
