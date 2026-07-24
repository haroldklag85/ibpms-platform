# 🔬 Diagnóstico Forense — Dropdown FormKey Vacío (US-005 CA-39)

## Fecha: 2026-06-22
## Ejecutado por: 🧠 Arquitecto Líder (Investigación forense via subagente especializado)

---

## Causa Raíz Identificada

### CR-1: Formularios en estado `DRAFT` (NO `ACTIVE`)
**Severidad:** 🔴 ALTA — Causa principal del dropdown vacío

El `FormCatalogController.java` línea 58 filtra por `status == "ACTIVE"`:
```java
.filter(form -> "ACTIVE".equalsIgnoreCase(form.getStatus()))
```

Pero los formularios creados en el Form Designer se guardan con status `DRAFT` inicialmente. Solo se convierten a `ACTIVE` tras certificación QA (`/api/v1/design/forms/{id}/certify`). 

**Si el usuario nunca certificó sus formularios, el endpoint retorna `[]`.**

### CR-2: El `processKey=datos` puede no existir en BD
**Severidad:** 🔴 ALTA — Causa del fallback a mocks

El Frontend llama `integrationStore.getForms("datos")` → Backend busca `bpmnDesignService.obtenerPorTechnicalId("datos")`. Si ese ID no existe en la tabla de procesos BPMN, el servicio lanza una excepción. El catch block del Frontend silenciosamente reemplaza todo con 4 formularios mock hardcodeados.

### CR-3: Doble filtro de patrón
**Severidad:** 🟡 MEDIA

El Backend filtra por patrón del proceso en líneas 81-92. El Frontend vuelve a filtrar en `filteredForms` computed (líneas 2850-2854). Si hay mismatch, nada pasa ambos filtros.

### CR-4: Mapeo frágil de campos en Frontend
**Severidad:** 🟡 BAJA

Backend retorna `{ "id": "technical_name", "name": "Form Name", "type": "SIMPLE" }`. Frontend hace `f.key || f.id || f.formId`. Funciona pero es frágil — `f.key` siempre es `undefined` y cae a `f.id`.

### CR-5: Fallback silencioso a mocks
**Severidad:** 🔴 ALTA — Viola Zero-Mock Policy

En BpmnDesigner.vue líneas 2599-2607, CUALQUIER error en la carga de formularios activa un fallback silencioso a 4 formularios hardcodeados. El usuario no tiene ningún feedback de que la data es falsa.

---

## Cadena Completa de Carga de Formularios (Trazada empíricamente)

```
BpmnDesigner.vue: fetchForms()
    └─→ integrationStore.getForms(processId.value)
         └─→ useIntegrationStore.ts L91-93: this.get('/forms/active', { params: { processKey } })
              └─→ apiClient.get('/api/v1/forms/active?processKey=datos')
                   └─→ FormCatalogController.java @GetMapping("/active")
                        ├─→ bpmnDesignService.obtenerPorTechnicalId("datos")  // ⚠️ Puede lanzar EntityNotFoundException
                        ├─→ formDesignService.listarCatalogo()
                        │    └─→ FormDesignJpaAdapter.findAllActive()
                        │         └─→ formDesignRepository.findAll().filter(status != DELETED)
                        ├─→ .filter(status == "ACTIVE")  // 🔴 CR-1: Descarta DRAFT
                        └─→ .filter(type matches processPattern)  // 🟡 CR-3: Segundo filtro
```

---

## Archivos Afectados

| Archivo | Líneas Clave | Acción Requerida |
|---------|:------------:|------------------|
| `FormCatalogController.java` | L44-96 | Incluir formularios DRAFT en el filtro. Manejar processKey inexistente gracefully |
| `FormDesignJpaAdapter.java` | L43-49 | OK — ya retorna non-DELETED |
| `FormDesignService.java` | L51-54 | OK — `listarCatalogo()` delega correctamente |
| `BpmnDesigner.vue` | L2589-2609 | Eliminar mock fallback. Manejar error con lista vacía + console.error |
| `BpmnDesigner.vue` | L2850-2854 | Verificar `filteredForms` computed — doble filtro con backend |
| `BpmnDesigner.vue` | L522-534 | OK — `<select>` y `syncElementProperties` funcionan correctamente |
| `useIntegrationStore.ts` | L91-93 | Hacer processKey opcional correctamente |

---

## Respuesta Esperada del Endpoint (Corregido)

```json
// GET /api/v1/forms/active (sin parámetros)
[
  {
    "id": "SOLICITUD_ONBOARDING",
    "name": "Solicitud Onboarding",
    "type": "SIMPLE"
  },
  {
    "id": "iForm_Credito",
    "name": "Formulario de Crédito",
    "type": "MASTER",
    "stages": 3
  }
]
```

---

> **Nota:** Este diagnóstico fue generado mediante investigación forense directa del código fuente.
> No se usaron suposiciones ni documentación desactualizada. Cada hallazgo tiene archivo y línea exacta.
