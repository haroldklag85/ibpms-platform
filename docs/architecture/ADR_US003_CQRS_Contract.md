# Architecture Decision Record: ADR-004 — Form Drafts & CQRS Integration (US-003 & US-029)

## 1. Contexto (GAP REM-003-05)
La US-003 construye un entorno de diseño para formularios (iForm), pero delega la persistencia transitoria de la digitación (Drafting) y la inyección formal de variables de proceso (Submit/Complete) a la infraestructura operativa de la plataforma. Ha surgido el riesgo de desacoplamiento funcional al no haber un contrato explícito sobre cómo los "Smart Buttons" de `FormDesigner.vue` emitirán los comandos hacia el motor CQRS provisto por la US-029.

## 2. Decisión
Se formaliza el contrato transaccional entre el Frontend Workdesk y el Backend CQRS Engine:

### 2.1 Puntos de Entrada Exigidos para la US-029
El motor reactivo de formularios (`useZodRuntime`) asumirá la existencia de las siguientes interfaces resolutivas para su funcionamiento en la Pantalla 7 (Ejecución):

*1. Auto-Guardado y Recuperación de Borrador (Drafting)*
```http
POST /api/v1/tasks/{taskId}/draft
Content-Type: application/json
{
  "payload_snapshot": { ... },
  "stage": "ANALYSIS"
}

GET /api/v1/tasks/{taskId}/draft
Returns: 200 OK con payload_snapshot
```

*2. Operaciones Finales (Smart Buttons)*
```http
POST /api/v1/tasks/{taskId}/complete
Content-Type: application/json
{
  "variables": { ... } // El Output Token extraído por Zod
}

POST /api/v1/tasks/{taskId}/reject
```

## 3. Consecuencias (Resiliencia)
- **Garbage Collection:** La US-003 implementará limpieza de `localStorage` local, permitiendo que la US-029 sea la Single Source of Truth asíncrona robusta.
- **Validación Fuerte:** Se previene fallo orgánico en la US-003 bloqueando el despliegue a producción de los Smart Buttons si los endpoints de la US-029 fallan o varían su contrato OpenAPI.
