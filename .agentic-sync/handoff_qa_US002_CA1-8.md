# Handoff: AI QA / DEVOPS AGENT
**Iteración:** 65-DEV (US-002 / CA-1 al CA-8)
**Contexto de Memoria Aislada:** Pruebas E2E y Ciberseguridad.

## 1. MISIÓN
Auditar la robustez del Motor de Reclamos de la US-002, atacando las vulnerabilidades (Idempotencia y Race Conditions).

## 2. MATRIZ DE CERTIFICACIÓN OBLIGATORIA (QA)
1. **Audit V2 Exclusión:** Revisa que ni Frontend ni Backend deslizaron librerías de V2.
2. **Pessimistic Lock / Concurrency:** Manda dos POST a la vez para simular Race Condition en `claim`. Un HTTP 200 y un HTTP 409 DEBEN producirse.
3. **IDOR Scan:** Simula request como Tenant-A sobre la tarea del Tenant-B. Certifica `403 Forbidden` / `AccessDenied`.
4. **Throttling Lote (CA-02):** Envía Payload con 51 Tareas en el array a `/claim-batch`. Certifica HTTP 400 por Constraint Violation `@Size`.
5. **DOM Jitter:** Evalúa reflow de Vue. Garantiza que la clase `.list-leave-active` existe para evitar parpadeos visuales al reclamar.

## 3. ENTREGABLE ESTRICTO
Sin refactorizar lógica profunda, elabora el Reporte E2E (GO/NO-GO). 
Guarda tus scripts/specs en:
`git stash save "temp-qa-US002-CA1-8"`
