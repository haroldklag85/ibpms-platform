# Handoff: Certificación E2E de J-04 — Iteración 2 (Post-Corrección Arquitecto)

**Destinatario:** [🕵️ QA - E2E]
**Emitido por:** [🧠 ARQUITECTO LÍDER]
**Fecha:** 2026-05-11 (Iteración 2)
**Contexto:** Re-ejecución post-corrección del `@PreAuthorize` en `SessionRevocationController.java`.

---

## 🔄 Contexto de esta Iteración

El Arquitecto Líder **ya resolvió las 3 decisiones pendientes** de tu reporte anterior. Los cambios aplicados son:

### Correcciones Aplicadas
1. **D-RBAC (RESUELTO):** Se amplió el `@PreAuthorize` del controlador de revocación:
   ```java
   // SessionRevocationController.java, línea 17
   // ANTES: @PreAuthorize("hasRole('ADMIN_IT')")
   // AHORA:
   @PreAuthorize("hasAnyRole('ADMIN_IT', 'SUPER_ADMIN')")
   ```
   → Esto desbloquea los tests `CU-KS-01`, `CU-KS-02`, `CU-KS-03` para el usuario `admin@alpha.com` (`ROLE_SUPER_ADMIN`).

2. **D-HMAC (RESUELTO):** Tu estrategia tolerante (aceptar 202 o 401) fue aprobada. Sin cambios requeridos.

3. **D-MOCK-PURGE (APROBADO):** Queda autorizado deprecar `us008-kanban-hub.spec.ts`. Renómbralo a `us008-kanban-hub.spec.ts.deprecated` o elimínalo.

---

## 🎯 Objetivo de esta Iteración

1. **Compilar el Backend** para certificar que la corrección del `@PreAuthorize` no introduce errores.
2. **Deprecar** el spec legacy con mocks (`us008-kanban-hub.spec.ts`).
3. **Re-ejecutar los 18 tests** de certificación J-04 contra infraestructura real.
4. **Reportar resultados** con evidencia de logs de ejecución.

---

## 🛑 REGLAS DE GOBERNANZA Y SKILLS (OBLIGATORIAS)

### 0. Skill de Ejecución (OBLIGATORIO LEER ANTES DE CODIFICAR)
Para asegurar que tu ejecución se adhiera a las directrices de la plataforma, **debes leer y aplicar estrictamente** el siguiente skill:
- **`cat .agents/skills/qa_e2e_validation_audit/SKILL.md`** (Auditoría y Validación E2E con Playwright)

Este skill obliga a:
- Correr Playwright contra los contenedores reales (PostgreSQL, RabbitMQ, Redis) levantados por `docker-compose.e2e.yml`.
- **Prohibir terminantemente** el uso de `route.fulfill()` o cualquier interceptor de red que simule respuestas del backend.
- Validar que los datos de prueba provienen exclusivamente del `seed-e2e.sql`, no de arrays estáticos inyectados en el DOM.

### 1. ADR-011: Gobernanza de Pirámide de Testing
Tienes **estrictamente prohibido certificar validez con tests basados en bases de datos en memoria (H2) o infraestructuras mockeadas.**
- Los tests de Playwright deben correr contra la instancia de base de datos aprovisionada mediante tu orquestador en `docker-compose.e2e.yml`.
- Usa el `seed-e2e.sql` disponible (ubicado en `src/main/resources/seed-e2e.sql`) para inicializar el estado del ambiente antes de ejecutar los tests de interfaz de usuario.

### 2. LEY GLOBAL 3: Trazabilidad Inversa / Anti-Amnesia Institucional (.cursorrules)
Recuerda documentar tus scripts con los trazadores correctos.
```typescript
// @Traceability: US-036, US-008
test('US-036: Un SUPER_ADMIN puede expulsar a un operario usando el Mass Deallocation', async ({ page }) => { ... });
```

### 3. LEY GLOBAL 2: Zero-Trust Compilation
- **Backend:** Antes de ejecutar tests, compila el backend con `mvn compile -f backend/ibpms-core/pom.xml` para validar que el cambio en `@PreAuthorize` no generó errores.
- **Frontend:** Si modificas specs, ejecuta `npx tsc --noEmit` en la carpeta `frontend/` para validar tipos.

---

## 📋 Secuencia de Ejecución (Paso a Paso)

```bash
# PASO 1: Compilar Backend (validar corrección @PreAuthorize)
cd backend/ibpms-core
mvn compile -DskipTests
# Esperar: BUILD SUCCESS

# PASO 2: Deprecar spec legacy con mocks
mv frontend/e2e/certification/us008-kanban-hub.spec.ts frontend/e2e/certification/us008-kanban-hub.spec.ts.deprecated

# PASO 3: Levantar infraestructura E2E
docker compose -f docker-compose.e2e.yml up -d

# PASO 4: Backend nativo (PROHIBIDO docker compose up ibpms-core)
.\start-e2e.bat
# Esperar: "Started Application" + "Tomcat started on port 8080"

# PASO 5: Verificar Data Seed
docker exec ibpms-postgres-uat psql -U ibpms -d ibpms -c "SELECT COUNT(*) FROM ibpms_workdesk_projection;"
# Esperado: count >= 5

# PASO 6: Frontend
cd frontend && npm run dev
# Esperar: "Local: http://localhost:5173"

# PASO 7: Ejecutar specs de certificación J-04
npx playwright test \
  certification/us036-kill-switch-break-glass.e2e.spec.ts \
  certification/us008-kanban-zeromock.e2e.spec.ts \
  certification/us004-webhook-intake-pipeline.e2e.spec.ts \
  --reporter=html,list
```

---

## ✅ Criterios de Éxito (Definition of Done)

| # | Criterio | Evidencia Esperada |
|---|----------|-------------------|
| 1 | Backend compila sin errores post-corrección | Log de `BUILD SUCCESS` |
| 2 | Spec legacy `us008-kanban-hub.spec.ts` deprecado | Archivo renombrado a `.deprecated` |
| 3 | 18 tests ejecutados sin `route.fulfill()` | Log de Playwright sin warnings de mock |
| 4 | Tests `CU-KS-01/02/03` pasan con HTTP 200 | Evidencia de que `ROLE_SUPER_ADMIN` es aceptado |
| 5 | Trazabilidad `@Traceability` presente en cada spec | Revisión de headers en los 3 archivos |

---

**Recuerda:** Empezar tu respuesta asumiendo tu rol `[🕵️ QA - E2E]` y confirmar la recepción del handoff.
