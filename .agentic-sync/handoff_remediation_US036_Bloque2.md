# Handoff de Remediación Integrada: US-036 (RBAC & Identity Governance) - Bloque 2

**Fecha/Hora:** 2026-04-18
**Contexto:** Auditoría Técnica y Forense de Sprint 4 (Seguridad) finalizada por el Agente Arquitecto Líder (Segunda Fase).
**Alcance:** Remediación de las brechas de seguridad funcionales y arquitecturales en los Criterios de Aceptación CA-6 a CA-10 de la US-036.

---

## 🛑 Hallazgos y GAPs Detectados (Auditoría Forense - Bloque 2)

Tras evaluar el estado del código para la segunda mitad de la matriz de gobierno de identidad, certificamos exitosamente el **CA-7** (Inmutabilidad por Soft-Delete está blindada mediante HTTP 405 y `isActive = false`) y el **CA-10** (Generación Opaca de Cuentas M2M mapeada correctamente con `SecureRandom` y SHA-256).

Sin embargo, reportamos GAPs críticos para el resto del bloque:

1. **GAP Arquitectónico (CA-6 - Herencia Piramidal):** El mapeo `parentRole` en `RoleEntity` existe, pero el motor de resolución estática y de DB carece de la sentencia `WITH RECURSIVE` exigida. El sistema actual NO HEREDA los permisos recursivamente: Si un gerente "A" hereda de un analista "B", el sistema no computa efectivamente los permisos del analista en tiempo real para el gerente.
2. **GAP Funcional (CA-8 - Aprovisionamiento de Ciudadano Interno):** Al iniciar sesión un usuario externo (SSO EntraID), el JWTAuthFilter o el Adapter de identidad no inyecta en la BD local el Rol mínimo (`[Ciudadano_Interno]`). Esta brecha deja identidades estériles atrapadas sin matriz basal.
3. **GAP de UI y Servicio (CA-9 - Delegación Autónoma Temporal):** Existen los esquemas SQL (`ibpms_sec_delegation_log`) y la Entidad `DelegationLogEntity.java`, pero **carecen de la infraestructura operativa**. Falta en el backend el endpoint para crear cesiones temporales y falta la capa reactiva (Tab de "Mis Delegaciones" o Modal en la Vista de Seguridad).

---

## 🛠️ Cuadrilla de Desarrollo: Directivas de Remediación

### Para el Agente Backend (Experto Data & Security)
1. **Resolución de Árbol de Permisos CTE (CA-6):** Implementar en `RoleRepository.java` (o equivalente) un Query Nativo con cláusula `WITH RECURSIVE role_tree AS (...)`, o de lo contrario, una lógica determinística memoizada a nivel de `RoleService` o `PermissionService` para construir un payload unificado que aplane los permisos `ProcessPermissionEntity` de un rol devolviendo sus permisos heredados.
2. **OIDC Provisioning Interceptor (CA-8):** En la entrada de SSO (p.ej. `AuthSyncController` o en `JwtSecurityFilter`), si la identidad tokenizada no figura en `userRepository`, crésela automáticamente atándole el rol por defecto buscado con un `.findByName("Ciudadano_Interno")`.
3. **Endpoint de Delegación (CA-9):** Crear en `UserAdminController.java` el `@PostMapping("/{id}/delegate")` recibiendo un DTO de Delegación (`recipientId`, `startDate`, `endDate`, `reason`). Validar que el startDate no sea retrospectivo, grabar en `DelegationLogEntity` y retornar el status.

### Para el Agente Frontend (Experto UI)
1. **Consola de Delegaciones (CA-9):** Construir o habilitar una pequeña sección (puede ser en el perfil de usuario o en una pestaña nueva en `RbacManager` llamada `RbacDelegationLog.vue`) que contenga el input de Fecha (Inicio a Fin) y Selector del usuario a ceder poder. Al someter, llamar a `POST /api/v1/admin/users/{id}/delegate`.
2. **Mocking Visual para Herencia (CA-6):** En el modal de "Roles", permitir indicar qué rol es el `parent_role`. Manda el `parentRole.id` explícito en los objetos de guardado `POST /api/v1/admin/roles/`.

### Para el Agente QA (Testing E2E)
1. **Aserciones CTE / Flat (CA-6):** Prueba atómicamente que al consultar los permisos consolidados de un usuario que posee Rol "A" que hereda de Rol "B", el array final expone íntegramente los checks cruzados. (Requisito fundamental para pasar QA).
2. **Test Vitest (CA-9):** Asegurar que las fechas invertidas o retroactivas disparen alertas validadas (Ej. Zod o VeeValidate en Frontend) e impidan el submit.

---

**Protocolo de Conformidad:** Inicien operaciones Inmediatamente sobre estas directivas. Se espera la misma eficacia mostrada en el Bloque 1 ("All-Green").
