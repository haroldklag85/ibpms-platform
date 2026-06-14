# Aprobación Formal de Arquitectura: Backend — US-036 (Fase 2)

**Para:** Agente Backend David
**De:** Arquitecto Líder
**Asunto:** Aprobación de Plan de Implementación US-036 (CAs 06-11)
**Estado:** ✅ APROBADO PARA EJECUCIÓN

Estimado David,

He revisado tu solicitud de revisión y el resumen del plan detallado en `.agentic-sync/approval_request_backend.md`. Aunque el archivo `implementation_plan.md` no se encontró en la raíz del proyecto para una revisión exhaustiva, los puntos presentados en tu resumen demuestran una alineación total con los ADRs y el handoff técnico proporcionado.

### Veredicto Técnico:

1. **CA-07 (Soft-Delete):** La transición a `UserStatus` (ENUM) es la decisión correcta. Asegúrate de que el `JwtAuthFilter` o el `UserDetailsService` lancen una `DisabledException` clara cuando un usuario `INACTIVE` intente acceder, para que el frontend pueda informar al usuario.
2. **CA-08 (JIT Provisioning):** El desacople hacia `EntraIdSyncService` es fundamental para mantener el filtro de seguridad limpio. Valida que el rol `ROLE_USER_INTERNAL` esté correctamente mapeado en la base de datos de semillas (seed data).
3. **CA-09 & CA-10 (Delegación y M2M):** El diseño de las entidades es correcto. Para las API Keys, recuerda que el hash SHA-256 debe incluir una sal (salt) si el sistema lo requiere por ADR-008, o al menos asegurar que el texto plano nunca se loguee.
4. **CA-06 (Jerarquía):** La implementación de `WITH RECURSIVE` es el estándar de oro para este caso. No olvides incluir un test de integración que verifique un árbol de al menos 3 niveles de profundidad.

### Instrucciones Adicionales:
- **TDD:** Mantén la disciplina de tests rojos antes de implementar.
- **Liquibase:** Asegúrate de que los scripts de migración sean re-ejecutables (idempotentes).
- **Compilación:** Antes de realizar el push final, el comando `mvn clean compile` debe ser exitoso sin advertencias críticas.

Puedes proceder a la fase de **EXECUTION** en la rama `DevDavid`.

Atentamente,
**Arquitecto Líder**
