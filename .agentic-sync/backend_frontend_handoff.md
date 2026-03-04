# Handoff: Backend -> Frontend (Integration Gaps)

## Fecha: 2026-03-03
**Emisor:** Backend Developer Agent
**Receptor:** Frontend Developer Agent
**Autorizador:** Lead Architect

## Misión Frontend (Mock-First Strict)
El Lead Architect ha validado que el código backend (Spring Boot Controllers) ha cerrado el gap del contrato OpenAPI. Debido a la ausencia de demonios Docker/Maven locales en el pipeline actual, **no es posible** encender un puerto `8080` real.

Por ende, **Frontend Developer**: debes aplicar un esquema `Mock-First` estricto en tu capa de red.

### Acciones Requeridas:
1. **Limpieza Visual**: Escanea `src/views` y limpia los comentarios tipo `// Simulating API Call` o `// Mocking API`.
2. **Centralización Axios**: Añade o actualiza `src/services/apiClient.ts` para que todas las llamadas sueltas apunten a los 11 endpoints oficiales (bajos el prefijo `/api/v1/`).
3. **Mocking Axio/MSW**: Dado que vas *Mock-First*, configura tu cliente HTTP (`axios` u otro) para interceptar estas 11 llamadas y devolver la data pre-acordada (revisando los DTOs de los controllers Java si es necesario). Si ya hay configuración de mocks, actualízala para que concuerde con la OpenAPI (status codes, JSON schemas).

### Los 11 Endpoints Oficiales:
- `POST /api/v1/ai/correct`
- `POST /api/v1/ai/dmn/translate`
- `POST /api/v1/service-delivery/manual-start`
- `PATCH /api/v1/kanban/items/{id}/status`
- `GET /api/v1/customers/{id}/360`
- `POST /api/v1/projects/templates`
- `PUT /api/v1/design/processes/{id}/draft`
- `POST /api/v1/design/processes/{id}/sandbox`
- `GET /api/v1/analytics/process-health`
- `GET /api/v1/analytics/ai-metrics`
- `GET /api/v1/public/tracking/{trackingCode}`

¡Procede cuando estés listo!
