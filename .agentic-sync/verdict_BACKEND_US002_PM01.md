# ✅ VEREDICTO ARQUITECTO LÍDER — Handoff Backend US-002 PM-01

> **Fecha:** 2026-06-03T22:57:00-05:00
> **Emisor:** Arquitecto Líder
> **Destinatario:** Agente Backend Especialista
> **En respuesta a:** `approval_request_BACKEND.md`

---

## VEREDICTO: ✅ APROBADO CON OBSERVACIONES

Felicito al Agente Backend por la auditoría de discrepancias. Las 10 desviaciones detectadas entre el handoff prescriptivo y la realidad del código son **legítimas y bien documentadas**. Procede a modo EXECUTION con las siguientes resoluciones:

---

## Resoluciones sobre las 4 Decisiones de Diseño

### 1. CA-15 — `tenantId` en `AgileTask`: ✅ APROBADA la propuesta principal

**APROBADO: Agregar campo `tenantId` al domain model + JPA entity + migración Liquibase.**

Justificación: El aislamiento multitenant es un invariante arquitectónico (ADR-009). Usar `teamId` como proxy sería una deuda técnica innecesaria porque `teamId` y `tenantId` no son equivalentes (un tenant puede tener múltiples teams). La migración Liquibase con `DEFAULT 'default'` es correcta y no romperá nada existente.

**Observación obligatoria:** En el `GhostJobScheduler` corregido, al iterar las tareas claimed, si `tenantId` es `null` o `'default'`, usa el timeout global (`claimProperties.getGhostTimeout()`). Solo consulta `getTimeoutForTenant()` si `tenantId` tiene un valor explícito distinto de `'default'`. Esto garantiza backward-compatibility.

### 2. CA-20 — Enum `ClaimActionType` sin cambiar firma: ✅ APROBADA

**APROBADO: Crear el enum pero mantener la firma `String` en `ClaimAuditService`.**

Justificación correcta: cambiar la firma del servicio de auditoría implicaría refactorizar TODOS los callers en un scope que no corresponde. El patrón `ClaimActionType.CLAIMED.name()` da typesafety en los callers nuevos/corregidos. Migrar la firma completa al enum queda documentada como deuda técnica para V2.

**Observación obligatoria:** Normaliza las inconsistencias existentes:
- `"FORCE_UNCLAIM"` → debe pasarse como `ClaimActionType.FORCE_UNCLAIMED.name()` = `"FORCE_UNCLAIMED"` (con "ED" final)
- Verifica que todos los callers existentes en `AgileTaskService` y `GhostJobScheduler` pasen el valor correcto del enum. Si detectas alguno que pase el string antiguo `"FORCE_UNCLAIM"`, corrígelo a `"FORCE_UNCLAIMED"` para unificar.

### 3. CA-17 — Orphaned files por `status = "UPLOADED"`: ✅ APROBADA

**APROBADO: `status = "UPLOADED"` + `uploadedAt < now - 24h` = orphaned.**

Justificación válida: el criterio `UPLOADED` vs `CONFIRMED` es semántico y no requiere columna adicional — es la ausencia de confirmación la que define el estado orphaned.

**Observación obligatoria:** El query del cleanup scheduler DEBE excluir archivos que pertenezcan a tareas con `status = 'CLAIMED'` (tarea activamente trabajada), incluso si tienen >24h. Esto protege contra el edge-case de un operario que suba archivos y luego extienda timeout dos veces. Sugiero:
```sql
DELETE FROM ibpms_temp_documents 
WHERE status = 'UPLOADED' 
  AND uploaded_at < :cutoff
  AND (task_id IS NULL OR task_id NOT IN 
       (SELECT id FROM ibpms_agile_tasks WHERE status = 'CLAIMED'))
```

### 4. `@PreUpdate` Bug Preexistente: ✅ ACEPTADO FUERA DE SCOPE

**APROBADO como documentación de deuda técnica.** No corrijas el `@PreUpdate` en este scope — pero documéntalo en un comentario Javadoc directamente en la línea del `@PreUpdate` de `AgileTaskJpaEntity`:

```java
/**
 * ⚠️ DEUDA TÉCNICA (Sprint PM-01): Este @PreUpdate resetea lastActivityAt en CADA save(),
 * lo que interfiere con la lógica de ghost timeout del CA-15. Pendiente refactorizar
 * para que solo se actualice cuando hay una acción registrable real del usuario.
 * Ref: approval_request_BACKEND.md — Decisión #4
 */
```

Esto garantiza que el próximo sprint no pierda el contexto.

---

## Observaciones Generales de Ejecución

1. **Orden de ejecución obligatorio:** (a) Migración Liquibase → (b) Domain model `AgileTask` + JPA entity → (c) Enum `ClaimActionType` → (d) `GhostJobScheduler` fix → (e) `AgileTaskService` extend-timeout limits → (f) `TransitoryFileCleanupScheduler` → (g) `TaskClaimApiController` deprecation → (h) Tests → (i) Compilación.
2. **Compilación obligatoria final:** `mvn clean compile -pl ibpms-core` DEBE pasar sin errores.
3. **Git:** `git add -A && git commit -m "feat(US-002): CA-15,CA-17,CA-19,CA-20 — ghost timeout per-tenant, orphaned cleanup, extend limits, enriched audit [PM-01]" && git push origin sprint-8/pm-01/us-002-claim`
4. **PROHIBIDO:** modificar la lógica de claim/unclaim/bulk-claim/force-unclaim. Solo toca lo prescrito.

---

## Firma

**Veredicto:** ✅ APROBADO CON OBSERVACIONES
**Autorizado por:** Arquitecto Líder
**Fecha:** 2026-06-03T22:57
**Válido hasta:** Fin del Sprint PM-01
