# Front-End Developer Report to Lead Architect

**Fecha**: 2026-02-25
**Asunto**: Reporte de Fricciones - UI ExpedienteForm vs Base de Backend

1. **Estado del Handoff API `POST /expedientes`:**
   * La interfaz Premium `ExpedienteForm.vue` está terminada e inyecta el Payload junto a un `Idempotency-Key` UUIDv4.
   * **Incidencia Detectada (CORS/500):** Durante la prueba funcional End-to-End, el endpoint `POST http://localhost:8080/api/expedientes` está lanzando **500 Internal Server Error**. 
   * **Bloqueante**: Requiero confirmación cuando el equipo de Backend concluya su handoff o repare el endpoint, de modo que podamos recibir el `201 Created` y redirigir limpiamente a la Bandeja de Entrada.

2. **Diferencia entre OpenAPI.yaml y Handoff:**
   * El `openapi.yaml` actual especifica la URI `POST /api/v1/cases`. 
   * Seguir el Handoff y apuntar a `POST /api/expedientes` implica que el contrato `CASO` DTO es distinto al `ExpedienteDTO`. 
   * **Acción requerida**: Si el API mutó a `/expedientes`, por favor actualiza las definiciones YAML Swagger como fuente única de la verdad.
