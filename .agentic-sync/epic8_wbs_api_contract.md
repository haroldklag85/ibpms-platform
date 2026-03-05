# 📑 API Contract: Pantalla 8 (Project Template Builder)

## Backend Handoff Status
- **Component**: `ibpms-core` (Project Design Domain)
- **Status**: ✅ Modelos 5 Capas, Upsert Topológico (Kahn's Sort), Validación `form_key` completados.
- **Ready for Frontend**: YES.

---

## 🚀 Endpoints Disponibles

**Base Path:** `/api/v1/design/projects/templates`

### 1. `POST /` (Deep Save)
Ejecuta de manera atómica (`@Transactional`) el reemplazo agresivo del árbol completo. Las tareas viajan anidadas.
*(NOTA UX: El motor detecta dependencias circulares (A->B->A) con TopSort. Si hay ciclos, abortará con HTTP 400 antes de insertar en DB).*

**Payload (Deep Save DTO):**
```json
{
  "id": "puede-ir-nulo-si-es-nuevo",
  "name": "Template de Construcción",
  "description": "...",
  "phases": [
    {
      "id": "fase1",
      "name": "Fase 1",
      "orderIndex": 1,
      "milestones": [
         {
           "id": "hito1",
           "name": "Hito Cimientos",
           "orderIndex": 1,
           "tasks": [
              {
                "id": "tarea-xyz",
                "name": "Excavación",
                "estimatedHours": 80,
                "formKey": "form_excavacion",
                "orderIndex": 1
              }
           ]
         }
      ]
    }
  ],
  "dependencies": [
     {
       "sourceTaskId": "tarea-xyz",
       "targetTaskId": "tarea-abc",
       "dependencyType": "FS",
       "lagHours": 0
     }
  ]
}
```

**Response (201 Created):** Retorna el mismo DTO garantizando los UUID generados para cruce.
**Error (400 Bad Request):** Ciclo detectado en `dependencies`.

### 2. `POST /{id}/publish`
Saca a la plantilla de estado `DRAFT` y la marca como lista para instanciar en Pantalla P9.
*(NOTA LÓGICA AC-1: El backend escudriñará que TODAS las tareas de la plantilla tengan un `formKey` atado. Si encuentra un nodo `null`, aborta).*

**Payload:** Vacío
**Response (200 OK):** `"Template Published Successfully"`
**Error (422 Unprocessable Entity):** `"Integrity Error: La tarea 'X' no tiene un form_key asignado."`

---

## 🛑 Guardrails Frontend
1. Al arrastrar un Data-Edge (flecha) en el árbol React Flow Vue 3 del WBS, es responsabilidad del frontend pintar de rojo si el usuario hizo un nodo cíclico. Aún así, Backend funge como Zero-Trust (HTTP 400).
2. Si reciben un HTTP 422 al publicar, iluminar en la UI las cajas grises que no tengan Form asignado.
