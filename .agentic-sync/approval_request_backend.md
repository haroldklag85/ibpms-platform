# Solicitud de Aprobación Técnica - US-036 (Fase Final - CA17-CA22)

David (Backend Dev) solicita revisión del plan para la implementación de los criterios finales de gobernanza.

## Alcance Propuesto
1. **Auditoría Forense (CA-17):**
   - Refinamiento de `RoleService` para capturar deltas JSON de permisos (otorgados/quitados).
   - Nuevo endpoint: `GET /api/v1/admin/roles/{id}/audit-logs`.
2. **Workdesk RLS (CA-20):**
   - Endurecimiento de `RowLevelSecurityAspect` para garantizar aislamiento de filas en la bandeja global.
3. **Service Accounts (CA-22):**
   - Expiración obligatoria (default 365 días, max 730 días).
   - Consolidación de Hashing SHA-256.

## Riesgos Identificados
- El cálculo de deltas JSON puede ser costoso si no se optimiza; se usará una comparación superficial de IDs de permisos para la V1.

## Acción Requerida
Por favor, valide que el enfoque de "JSON Delta" y el interceptor AOP cumplen con las expectativas de la US-036 antes de proceder a EXECUTION.
