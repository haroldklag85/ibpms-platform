# 🧪 HANDOFF QA — Iteración 3 (Sprint 6.2)
## Auditoría Arquitectural: US-003 (iForm Maestro) + US-039 (Formulario Genérico)

**Fecha de Emisión:** 2026-04-20  
**Emitido por:** Arquitecto Líder (Antigravity)  
**Destinatario:** Equipo QA / UAT Lead  
**Protocolo:** Zero-Hallucination | Certificación Forense  

---

## 📋 Resumen Ejecutivo

Este handoff establece el **plan de pruebas obligatorio** para certificar los cambios derivados de la auditoría de las US-003 y US-039. Se incluyen escenarios funcionales, de seguridad, y de regresión, organizados por prioridad y complejidad.

---

## 1. Matriz de Escenarios de Prueba

### 🔴 ALTA PRIORIDAD — Seguridad y VIP Pre-Flight

| ID | Escenario | Tipo | US | CA | Resultado Esperado |
|---|---|---|---|---|---|
| **QA-039-01** | Usuario VIP intenta abrir tarea `sys_generic_form` | Seguridad | US-039 | CA-1/6 | HTTP 403 con mensaje "RESTRICCIÓN VIP" |
| **QA-039-02** | Agregar nuevo rol VIP desde BD y validar bloqueo sin re-deploy | Seguridad | US-039 | CA-6 | El nuevo rol queda bloqueado inmediatamente |
| **QA-039-03** | Segregación de Funciones — Initiator intenta auto-aprobar | Seguridad | US-039 | CA-6 | HTTP 403 con mensaje "Segregación de Funciones (SoD)" |
| **QA-039-04** | Submit con `panicAction=CANCELLED` SIN justificación | Validación | US-039 | CA-8 | HTTP 400: "panicJustification must be >= 20 characters" |
| **QA-039-05** | Submit con `panicAction=CANCELLED` con justificación < 20 chars | Validación | US-039 | CA-8 | HTTP 400 |

---

### 🟡 MEDIA PRIORIDAD — Funcional y Whitelist

| ID | Escenario | Tipo | US | CA | Resultado Esperado |
|---|---|---|---|---|---|
| **QA-039-06** | GET /generic-form-context devuelve solo variables de whitelist | Funcional | US-039 | CA-2 | `prefillData` contiene solo las claves configuradas, sin `_internal_*`, `camunda_*`, `zeebe_*` |
| **QA-039-07** | PUT /generic-form-config con 11 claves en whitelist | Validación | US-039 | CA-5 | HTTP 400: "Whitelist cannot exceed 10 variables" |
| **QA-039-08** | PUT /generic-form-config con 5 claves válidas | Funcional | US-039 | CA-5 | HTTP 200. GET posterior devuelve solo esas 5 variables en prefillData |
| **QA-039-09** | Submit normal (sin panic) con observaciones válidas | Funcional | US-039 | CA-4 | HTTP 204. Tarea completada en Camunda. Variables `generic_form_*` disponibles en proceso |
| **QA-039-10** | Submit con observaciones < 10 caracteres | Validación | US-039 | CA-4 | Debe fallar (frontend Zod + backend si aplica) |
| **QA-039-11** | Submit con > 5 attachmentUuids | Validación | US-039 | CA-4 | HTTP 400 |
| **QA-003-01** | Crear formulario con 200+ campos en FormDesigner | Rendimiento | US-003 | CA-37 | Banner de advertencia visible. Botón de agregar deshabilitado al superar 200 |

---

### 🟢 BAJA PRIORIDAD — UX y Draft Management

| ID | Escenario | Tipo | US | CA | Resultado Esperado |
|---|---|---|---|---|---|
| **QA-039-12** | Escribir en formulario, cerrar pestaña, reabrir | UX | US-039 | CA-7 | Banner amber: "Se detectó un borrador no enviado. ¿Desea restaurarlo?" |
| **QA-039-13** | Clic en "Restaurar" en banner de borrador | UX | US-039 | CA-7 | Campos de formulario recuperan texto previo. Banner desaparece |
| **QA-039-14** | Clic en "Descartar" en banner de borrador | UX | US-039 | CA-7 | Campos permanecen vacíos. Banner desaparece |
| **QA-039-15** | Indicador de sincronización cambia de estado | UX | US-039 | CA-7 | Al escribir: ambar "Borrador en Navegador" → al guardar: spinner → al completar: verde "Sincronizado" |
| **QA-039-16** | Botón "Limpiar Borrador" elimina draft local y remoto | UX | US-039 | CA-7 | LocalStorage limpia. Campos vacíos. Estado resetea a "Sincronizado" |
| **QA-003-02** | GC purga borradores > 7 días al iniciar app | Infraestructura | US-003 | — | DevTools → LocalStorage: entradas antiguas desaparecen. Log: "[GC] Purged N stale drafts" |

---

## 2. Procedimiento Detallado — Escenarios Críticos

### QA-039-01: VIP Pre-Flight Restrictor (Dinámico)

**Pre-condiciones:**
- Backend levantado con la migración `21-us039-generic-form-schema.sql` aplicada.
- 3 roles semilla en `ibpms_security_role` con `is_vip_restricted = true`:
  - `ALTA_DIRECCION`
  - `APROBADOR_FINANCIERO`
  - `SELLO_LEGAL`
- Una tarea activa en Camunda con `formKey = 'sys_generic_form'`.
- Un usuario con el rol `ROLE_ALTA_DIRECCION`.

**Pasos:**
1. Autenticarse con el usuario VIP.
2. Invocar `GET /api/v1/workbox/tasks/{taskId}/details` (o el equivalente del BpmTaskService).
3. Verificar que la respuesta es HTTP 403.

**Validación SQL post-test:**
```sql
-- Verificar roles VIP dinámicos
SELECT name, is_vip_restricted FROM ibpms_security_role WHERE is_vip_restricted = true;
-- Debe devolver 3 filas: ALTA_DIRECCION, APROBADOR_FINANCIERO, SELLO_LEGAL
```

---

### QA-039-02: Agregar Nuevo Rol VIP sin Re-Deploy

**Pre-condiciones:** Mismo entorno que QA-039-01.

**Pasos:**
1. Insertar un nuevo rol VIP directamente en BD:
```sql
INSERT INTO ibpms_security_role (id, name, description, is_vip_restricted, is_template, source)
VALUES (gen_random_uuid(), 'ROL_TEST_VIP', 'Rol de prueba QA', true, false, 'LOCAL');
```
2. Asignar este rol a un usuario de prueba.
3. Intentar abrir una tarea con `formKey = 'sys_generic_form'`.
4. **Sin reiniciar el backend.**
5. Verificar HTTP 403.

**Resultado esperado:** El nuevo rol es rechazado dinámicamente porque el `BpmTaskService` consulta la BD en tiempo real.

---

### QA-039-04 y QA-039-05: Botones de Pánico

**Pasos (Frontend):**
1. Abrir una tarea genérica como usuario operador normal.
2. Clic en botón "Cancelar Tarea" (rojo).
3. Verificar que aparece el modal con justificación obligatoria.
4. Intentar confirmar con texto < 20 caracteres → Botón "Confirmar" deshabilitado.
5. Escribir justificación >= 20 caracteres → Botón se habilita.
6. Confirmar → Tarea se cierra con `handleBpmnError("TASK_CANCELLED_BY_OPERATOR")`.

**Pasos (Backend via cURL):**
```bash
# Sin justificación → 400
curl -X POST http://localhost:8080/api/v1/workbox/tasks/{taskId}/generic-form-complete \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{"panicAction": "CANCELLED", "managementResult": "CANCELLED"}'

# Con justificación corta → 400
curl -X POST http://localhost:8080/api/v1/workbox/tasks/{taskId}/generic-form-complete \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{"panicAction": "CANCELLED", "panicJustification": "Corto", "managementResult": "CANCELLED"}'

# Con justificación válida → 204
curl -X POST http://localhost:8080/api/v1/workbox/tasks/{taskId}/generic-form-complete \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{"panicAction": "CANCELLED", "panicJustification": "El caso requiere ser cancelado por instrucción gerencial debido a duplicidad.", "managementResult": "CANCELLED"}'
```

---

## 3. Tests Automatizados Existentes

### Backend: `GenericFormIntegrationTest.java`

**Ubicación:** `backend/ibpms-core/src/test/java/com/ibpms/poc/infrastructure/web/security/GenericFormIntegrationTest.java`

| Test | CA | Estado |
|---|---|---|
| `testCa4_ShortObservationsShouldFail` | CA-4 | ⚠️ Depende de endpoint activo |
| `testCa4_TooManyAttachmentsShouldFail` | CA-4 | ⚠️ Depende de endpoint activo |
| `testCa4_ValidPayloadShouldPass` | CA-4 | ⚠️ Depende de endpoint activo |
| `testCa5_InternalVariablesDiscarded` | CA-5 | ⚠️ Mock endpoint |
| `testCa5_WhitelistExceeds10ShouldFail` | CA-5 | ⚠️ Mock endpoint |
| `testCa7_DraftAutosave` | CA-7 | ⚠️ Mock endpoint |
| `testCa8_CancelledRequiresJustification` | CA-8 | ⚠️ Mock endpoint |

> [!WARNING]
> **Los tests existentes usan Testcontainers pero muchos dependen de endpoints mock que pueden no estar completamente implementados.** Se recomienda ejecutarlos con `mvn test -Dtest=GenericFormIntegrationTest` para identificar cuáles pasan en el entorno actual y cuáles necesitan ajustes.

### Tests Requeridos (Nuevos)

| ID | Descripción | Tipo | Prioridad |
|---|---|---|---|
| **QA-TEST-01** | VIP dinámico: Insertar rol, verificar bloqueo sin re-deploy | Integration | 🔴 Alta |
| **QA-TEST-02** | SoD: Initiator no puede auto-aprobar | Integration | 🔴 Alta |
| **QA-TEST-03** | Whitelist configurable: Cambiar whitelist, verificar prefillData | Integration | 🟡 Media |
| **QA-TEST-04** | Draft restoration: Banner aparece con borrador previo (Vitest) | Component | 🟢 Baja |
| **QA-TEST-05** | EvidenceDropzone: Archivo > 10MB rechazado (Vitest) | Component | 🟢 Baja |

---

## 4. Checklist de Regresión

Antes de certificar, ejecutar estos comandos:

```bash
# Backend — Compilación y tests unitarios
cd backend/ibpms-core
mvn clean compile -q
mvn test -q

# Frontend — Build check
cd frontend
npm run build

# Frontend — Tests Vitest (si existen)
npm run test -- --run
```

---

## 5. Criterios de Certificación

| Criterio | Umbral Mínimo |
|---|---|
| **Tests Críticos (QA-039-01 a 05)** | 100% Pass |
| **Tests Funcionales (QA-039-06 a 11)** | 100% Pass |
| **Tests UX (QA-039-12 a 16)** | 80% Pass (tolerancia visual) |
| **Regresión Backend** | `mvn compile` sin errores |
| **Regresión Frontend** | `npm run build` sin errores |

---

## 6. Datos de Prueba Requeridos

| Dato | Descripción | SQL / Seed |
|---|---|---|
| Usuario operador | Sin rol VIP | Ya en seed de usuarios |
| Usuario VIP | Con `ROLE_ALTA_DIRECCION` | Ya en seed |
| Tarea genérica | `formKey = 'sys_generic_form'` | Desplegar proceso BPMN de prueba con este formKey |
| Proceso con whitelist | `ibpms_bpmn_process_design.generic_form_whitelist` configurado | `UPDATE ibpms_bpmn_process_design SET generic_form_whitelist = '["Case_ID", "amount", "priority"]' WHERE technical_id = 'proceso_test'` |
| Draft viejo | `localStorage` con timestamp > 7 días | Insertar manualmente en DevTools |

---

> [!IMPORTANT]
> **Fecha límite de certificación:** La auditoría de la Iteración 3 requiere que los escenarios QA-039-01 a QA-039-05 (seguridad) estén certificados antes de cerrar el Sprint 6.2. Los escenarios UX (QA-039-12 a 16) pueden certificarse en el Sprint 6.3 sin bloquear el release.
