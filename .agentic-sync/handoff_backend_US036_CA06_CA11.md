# Handoff Técnico: Backend — US-036 (Identity Governance) - Fase 2

## 1. Contexto de la Tarea
- **Iteración:** 04-DEV-DAVID
- **US:** US-036 (Identity Governance)
- **CAs:** CA-06, CA-07, CA-08, CA-09, CA-10, CA-11
- **Rama:** `DevDavid`
- **SSOT:** [epic_E_seguridad_identidad_config.md](file:///c:/Users/USER/Desktop/Proyectos/Harold%20Ibpms/ibpms-platform/docs/requirements/epics/epic_E_seguridad_identidad_config.md)

## 2. Alineación Arquitectónica (ADR Compliance)
- **ADR-001 (Hexagonal):** Lógica de delegación y aprovisionamiento en `application/service`.
- **ADR-009 (UUID & Postgres):** Uso de UUID para todas las nuevas entidades.
- **ADR-011 (CQRS):** Separación de comandos de mutación (Soft-Delete) de consultas de auditoría.

## 3. Requerimientos Técnicos por Criterio (Backend)

### CA-06: Herencia Piramidal (Refuerzo)
- Asegurar que `RoleRepository.findRoleIdsInTree` use una query nativa `@Query` con `WITH RECURSIVE` en PostgreSQL para obtener la jerarquía completa de ancestros.

### CA-07: Soft-Delete (Inmutabilidad)
- **Entidad:** `UserEntity` debe tener un campo `status` (ENUM: ACTIVE, INACTIVE).
- **Lógica:** El método `deleteUser` en `UserService` no debe ejecutar `repository.delete()`. Debe cambiar el estado a `INACTIVE` y persistir.
- **Seguridad:** Los usuarios con estado `INACTIVE` deben ser rechazados en el proceso de autenticación (`UserDetailsService`).

### CA-08: JIT Provisioning (EntraID)
- En `EntraIdSyncService`, si un usuario se autentica vía SSO y no existe localmente, se debe crear automáticamente con el rol `ROLE_USER_INTERNAL` (nombre comercial: Ciudadano Interno).
- Si faltan claims básicos (email, name), lanzar excepción de negocio para que el frontend maneje el modal de completar perfil.

### CA-09: Módulo de Delegación Temporal
- **Nueva Entidad:** `RoleDelegationEntity`
  - `id` (UUID)
  - `ownerId` (UUID - Usuario que delega)
  - `delegateId` (UUID - Usuario suplente)
  - `startDate` (LocalDateTime)
  - `endDate` (LocalDateTime)
  - `active` (boolean)
- **Lógica:** Implementar un `ScheduledTask` o resolver dinámicamente en el `getEffectiveRoles` del usuario si existe una delegación activa para la fecha actual.

### CA-10: Cuentas de Servicio (M2M)
- **Nueva Entidad:** `ServiceAccountEntity`
  - `id` (UUID)
  - `name` (String)
  - `apiKeyHash` (String - SHA-256)
  - `roleId` (UUID - Rol asociado)
  - `expiresAt` (LocalDateTime)
- **Lógica:** Al generar, devolver la API Key en texto plano **solo una vez**. Almacenar solo el hash.
- **Filtro de Seguridad:** Añadir un `ApiKeyAuthenticationFilter` que valide estas llaves contra la base de datos.

### CA-11: No MFA Propio
- (Gobernanza) Prohibido implementar lógica de OTP/SMS/MFA en el backend. Se delega al token JWT de EntraID.

## 4. NFR/QA Strategy
- Desarrollar sobre la arquitectura en `docs/architecture/arquitecturar.md`.
- Asegurar que el usuario `root@ibpms.local` (Password: `Root#Temp4Sys`) mantenga acceso total tras los cambios de seguridad.

**INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_backend.md`.
4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_backend.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.

**📚 SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
- Aplica estrictamente el protocolo de pruebas **TDD** documentado en `.agents/skills/tdd_first/SKILL.md` (Red -> Green -> Refactor) antes de hacer lógica.
- Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md` (naming, error handling, modularidad).
- **Compilación obligatoria:** Ejecuta el protocolo Zero-Trust SRE documentado en `.agents/skills/backend_sre_compilation_audit/SKILL.md` (§0 a §2). Si hay bloqueos de infraestructura, aplica el protocolo de reporte 3B.
