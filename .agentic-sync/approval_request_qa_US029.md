# 🛡️ Certificación de QA: US-029 (Ejecución de Formulario - Bloque 1)

**Agente Responsable:** Agente QA (Antigravity)
**Modo:** Certificación Estricta (Zero-Trust)
**Fecha:** 2026-05-03

## 📊 Resumen Ejecutivo
Se han auditado los 14 escenarios de remediación crítica definidos para la US-029.
**Estado Global:** ⚠️ **RECHAZADO (FAIL)**
Se encontró una violación arquitectónica en el Frontend respecto al patrón "Upload-First" (CA-09). 13 escenarios pasaron con éxito.

---

## 🔎 Matriz de Evidencia y Veredicto

| ID | Escenario | Estado | Evidencia Arquitectónica (Código/Log) |
|----|-----------|--------|---------------------------------------|
| **QA-029-01** | POST `/complete` payload vacío → HTTP 400 | ✅ PASS | `FormCompletionService.java:94-102` usa `JsonSchemaFactory` para validar el JSON Schema estricto. |
| **QA-029-02** | POST `/complete` con tipo inválido → HTTP 400 | ✅ PASS | Incluido intrínsecamente en la validación JSON Schema (retorna `ValidationFailed`). |
| **QA-029-03** | `CompletarTareaService` no pasa variables masivas | ✅ PASS | `CompletarTareaService.java:44` llama a `extractGatewayVariables(variables)` filtrando colecciones. |
| **QA-029-04** | Upload temp con archivo >25MB → HTTP 400 | ✅ PASS | `S3DocumentTempService.java:41` lanza 400 si `file.getSize() > MAX_FILE_SIZE` (25MB). |
| **QA-029-05** | Upload temp con spoofing de extensión → HTTP 415 | ✅ PASS | `S3DocumentTempService.java:55` detecta magic bytes con Apache Tika y bloquea `application/x-executable`. |
| **QA-029-06** | Upload temp extensión no permitida → HTTP 400 | ✅ PASS | `S3DocumentTempService.java:51` cruza extensión contra un `ALLOWED_EXTENSIONS` whitelist. |
| **QA-029-07** | POST `/complete` con IDOR de otro user → HTTP 403 | ✅ PASS | `FormCompletionService.java:112` chequea regex de UUID y previene Anti-IDOR validando `userId` y `taskId`. |
| **QA-029-08** | POST `/complete` condicionales adulteradas | ✅ PASS | `FormCompletionService.java:104` valida `_visibleFields` retornando 400 ante omisiones forzadas. |
| **QA-029-09** | `TaskDraftService` sin import de JPA | ✅ PASS | `TaskDraftService.java:6` inyecta dependencia invertida vía `AgileTaskPort` (Hexagonal OK). |
| **QA-029-10** | `FormBffCoreService` minifiedDto a Camunda | ✅ PASS | `FormBffCoreService.java:107` crea `minifiedDto` excluyendo el mega-payload. |
| **QA-029-11** | UI: Botón disable + Overlay submit | ✅ PASS | `GenericFormBody.vue:56` muestra overlay y `useSubmitFeedback.ts` gobierna el estado reactivo. |
| **QA-029-12** | UI: ✅ + redirect + localStorage purge | ✅ PASS | `GenericFormBody.vue:146` ejecuta RYOW eliminando drafts, el array de Pinia y redirige tras 3s. |
| **QA-029-13** | UI: Enviar JSON plano (No Multipart) | ❌ **FAIL** | `genericFormStore.ts:185` sigue adjuntando `formData.append('evidenceFiles', f)` y enviando `multipart/form-data` al completado en lugar de usar `Upload-First`. |
| **QA-029-14** | UI: HTTP 409 SchemaConflictModal | ✅ PASS | `GenericFormBody.vue:162` atrapa 409 y muestra Modal No Destructivo para refrescar estructura. |

---

## 🚨 Reporte de Bloqueo (QA-029-13)

El escenario **QA-029-13** falló en la certificación. El Agente Frontend NO completó el GAP-04. 
En `genericFormStore.ts`, el método `submitForm()` sigue usando `FormData` multipart enviando los archivos de evidencia en la misma transacción del formulario:

```typescript
// genericFormStore.ts L185
files.value.forEach((f) => {
  formData.append('evidenceFiles', f)
})
await apiClient.post(`/workbox/tasks/${taskId.value}/generic-form-complete`, formData, {
  headers: { 'Content-Type': 'multipart/form-data' }
})
```

**Acción Requerida:** 
Se debe devolver este Handoff al Agente Frontend para que refactorice el método `submitForm()`:
1. Subir cada archivo por separado a `POST /api/v1/documents/upload-temp`.
2. Extraer los UUIDs de respuesta.
3. Enviar el POST final de completado con un JSON puro que contenga los UUIDs en un array.

Esperando instrucciones para continuar o escalar el Fail de vuelta al Agente Frontend.
