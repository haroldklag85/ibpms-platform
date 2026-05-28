# SOLICITUD DE REVISIÓN — Agente Frontend
## BUG-S7-001 | Sprint 7 | Rama: sprint-7/bugfix-uat
**Fecha:** 2026-05-28T01:41:00Z  
**Agente:** Frontend Developer (Vue 3 / Vite)  
**Destino:** Arquitecto Líder (revisión y aprobación)

---

## Resumen Ejecutivo

He completado el análisis forense de los dos bugs reportados en el handoff `handoff_frontend_BUG-S7-001.md`. Tengo identificada la causa raíz con precisión quirúrgica y un plan de corrección mínimo de 6 líneas de cambio efectivo en 2 archivos.

---

## Diagnóstico Confirmado

### BUG-A — Payload vacío en "Probar Submit" (CA-Fix-01)

**Archivo:** `frontend/src/views/admin/Modeler/FormDesigner.vue`  
**Función:** `simulateMockSubmit()`, línea 1262  
**Causa raíz:** El objeto `rawFormSubmission` se declara como `{}` vacío y **nunca se rellena** con las claves de los campos del canvas antes de ejecutar `schema.safeParse(rawFormSubmission)`. Como resultado, Zod intenta validar un objeto sin ninguna propiedad, y como los campos requeridos no existen en él, reporta error `invalid_type: Required` para todos los campos — lo cual parece correcto en superficie pero el problema real es que el mensaje de error no identifica los campos correctos porque el payload no tiene estructura.

**Corrección planificada:** Poblar `rawFormSubmission` recorriendo `availableFieldsFlat.value` antes del `safeParse`, asignando valores vacíos según tipo (`''`, `null`, `false`, `[]`). Esto garantiza que Zod recibe las claves correctas y puede reportar con precisión qué campos requeridos están sin llenar.

### BUG-B — Doble prefijo `/api/v1` (CA-Fix-02)

**Archivo:** `frontend/src/stores/useFormDesignerStore.ts`  
**Causa raíz:** `apiClient` tiene `baseURL: '/api/v1'` configurado en `services/apiClient.ts` (línea 7). Sin embargo, el store pasa rutas que ya incluyen `/api/v1` al cliente, resultando en `http://localhost:5173/api/v1/api/v1/forms/draft`.

**5 ocurrencias afectadas:**

| Línea | Ruta actual (BUGGY) | Ruta corregida |
|-------|---------------------|----------------|
| 107 | `/api/v1/design/forms/generate` | `/design/forms/generate` |
| 129 | `/api/v1/forms/mock_id_or_draft/versions` | `/forms/mock_id_or_draft/versions` |
| 142 | `/api/v1/forms/${formId}` | `/forms/${formId}` |
| 239 | `/api/v1/forms/draft` | `/forms/draft` |
| 332 | `/api/v1/design/forms/${formId}/certify` | `/design/forms/${formId}/certify` |

---

## Solicitud al Arquitecto Líder

Por favor revise y valide:

1. ✅ ¿Confirma que las rutas correctas del backend para estas operaciones son las indicadas en la columna "Ruta corregida"?
2. ✅ ¿El diseño de poblar `rawFormSubmission` con valores vacíos (no happy path) en "Probar Submit" es coherente con el contrato funcional de CA-Fix-01?
3. ✅ ¿Hay algún endpoint que tenga una ruta de base diferente a `/api/v1` que deba mantenerse con el prefijo explícito?

---

## Impacto Esperado

- **Archivos modificados:** 2 (`FormDesigner.vue`, `useFormDesignerStore.ts`)
- **Líneas de código cambiadas:** ~11 líneas (corrección mínima sin efectos colaterales)
- **Funcionalidades adyacentes en riesgo:** NINGUNA (cambios aislados a la función `simulateMockSubmit` y al prefijo de rutas)
- **Riesgo de regresión:** BAJO (los cambios son correctivos, no arquitectónicos)

---

## Estado del Agente Frontend

- [x] Handoff leído y comprendido
- [x] Código existente analizado forense
- [x] Causa raíz identificada con certeza
- [x] Plan documentado en `implementation_plan.md`
- [ ] Esperando aprobación del Arquitecto Líder para pasar a EJECUCIÓN
