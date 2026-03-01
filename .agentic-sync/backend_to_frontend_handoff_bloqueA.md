# Handoff Bloque A: Backend -> Frontend (Project Builder WBS)

**Ref:** US-006
**Objetivo:** Creación de Plantillas de Work Breakdown Structure (WBS)
**Seguridad OIDC:** Requiere `Role_Architect` o `Role_Admin_Intake` en el Token JWT.

El Backend ha finalizado la capa de dominio relacional híbrida, guardando el proyecto como fila SQL nativa y el arreglo de subfases anidadas como un String JSON compatible con MySQL 8.0 `(Dual Schema Data Architecture)`.

## Endpoint: POST `/api/v1/project-templates`

### 1. Request Body Esperado (JSON)
El formato debe seguir estrictamente esta jerarquía.

```json
{
  "name": "Implementación SAP ERP - Básico",
  "description": "Plantilla WBS para proyectos de consultoría SAP de 3 meses.",
  "category": "IT Constulting",
  "phases": [
    {
      "name": "Levantamiento de Requisitos",
      "description": "Fase inicial de toma de requerimientos con el área usuaria.",
      "orderIndex": 1,
      "defaultAssigneeRole": "Analista_Negocio"
    },
    {
      "name": "Desarrollo ABAP y Configuración",
      "description": "Customización del ambiente.",
      "orderIndex": 2,
      "defaultAssigneeRole": "Lider_Tecnico"
    },
    {
      "name": "Paso a Producción (Go-Live)",
      "description": "Despliegue y estabilización.",
      "orderIndex": 3,
      "defaultAssigneeRole": "Gestor_Despliegue"
    }
  ]
}
```

### 2. Respuesta Esperada (HTTP 201 Created)
El API devolverá el modelo de DTO adjuntando los `UUID` nativos de la base de datos y los sellos de auditoría de creación.

```json
{
  "id": "e45f9d2a-7b3b-419a-9e32-218bbad51ae0",
  "name": "Implementación SAP ERP - Básico",
  "description": "Plantilla WBS para proyectos de consultoría SAP de 3 meses.",
  "category": "IT Constulting",
  "phases": [ ... ],
  "createdAt": "2026-03-01T10:45:30.150Z",
  "createdBy": "john.architect@correo.com"
}
```

**Nota para Frontend:** Procuren usar un componente de validación de Formularios Dinámicos con "Add/Remove Sub-form" capaz de componer el array de `phases` antes de emitir el POST genérico.
