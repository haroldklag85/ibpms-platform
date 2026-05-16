# Solicitud de Aprobación — Agente QA SDET (Iteración 2)

**Fecha:** 2026-05-15  
**De:** Agente QA SDET  
**Para:** Arquitecto Líder  
**Asunto:** Plan de Certificación E2E + Fixes de Infraestructura — US-038 (CA-06 al CA-12, excluyendo CA-09)

---

## Resumen Ejecutivo

### Infraestructura: 3 Bugs Críticos Resueltos
El backend no arrancaba por 3 bloqueantes. Todos fueron corregidos quirúrgicamente:

| Bug | Archivo | Fix |
|-----|---------|-----|
| Ambiguous Mapping `/admin/queues/dlq/purge` | `DlqAdminController.java` | `@Profile("deprecated")` |
| Ambiguous Mapping `/auth/emergency-login` | `EmergencyLoginController.java` | `@Profile("deprecated")` |
| RabbitMQ PRECONDITION_FAILED (`x-message-ttl`) | `RabbitMQConfig.java` L67 | Added `x-message-ttl: 2592000000L` |

**Estado actual: Spring Boot corriendo en `localhost:8080` ✅**

### Plan de Tests E2E
Suite: `us-038-iteration2-sod-delegation.spec.ts` (6 tests, 3 bloques):

| Bloque | CAs | Enfoque |
|--------|-----|---------|
| A: SoD + CISO | CA-06, CA-12 | API anomalies + UI Tablero CISO |
| B: Delegaciones | CA-07, CA-08 | UI Form delegación + Revocación |
| C: Badges | CA-10, CA-11 | UI MainLayout role badges |

### Credenciales
- `root@ibpms.local` / `Root#Temp4Sys`

### Solicitud al Arquitecto
1. ¿Aprueba los 3 fixes de infraestructura como requisitos previos?
2. ¿Aprueba el plan de 6 CAs en 3 bloques?
3. ¿Alguna modificación requerida al plan?

---
*Archivo generado por el Agente QA SDET según protocolo del handoff L97-101.*
