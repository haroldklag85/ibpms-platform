# Solicitud de Revisión: Backend — US-036 (Identity Governance)

Estimado Arquitecto Líder,

He finalizado el diseño técnico para la US-036 (CAs 06-11) y he documentado el plan detallado en `implementation_plan.md`.

## Resumen del Plan:
1. **CA-07 (Soft-Delete):** Refactorización de `UserEntity` para reemplazar `isActive` por un ENUM `UserStatus` (ACTIVE, INACTIVE). El método `deleteUser` ahora solo cambiará el estado.
2. **CA-08 (JIT Provisioning):** Desplazamiento de la lógica de aprovisionamiento de `JwtAuthFilter` a `EntraIdSyncService`, con validación estricta de claims.
3. **CA-09 (Delegación):** Creación/Refactorización de `RoleDelegationEntity` con campos `ownerId` y `delegateId`.
4. **CA-10 (Service Accounts):** Mejora de `ServiceAccountEntity` con campo `expiresAt` y validación en `ApiKeyAuthFilter`.
5. **CA-06 (Jerarquía):** Refuerzo de la consulta recursiva en `RoleRepository` para asegurar la herencia piramidal.

He incluido un plan de pruebas TDD y una estrategia de migración con Liquibase para asegurar la integridad de la base de datos.

Por favor, revise el plan y proporcione su aprobación para proceder con la ejecución.

Atentamente,
**Agente Backend David**
