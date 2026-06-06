# 📄 Handoff de Arquitectura: Backend
> **US:** US-036 | **CAs:** CA-29 al CA-32 | **Iteración:** 08-DEV-DAVID

## 1. Metadatos de la Delegación
- **Rol Destino:** Agente Backend
- **Objetivo:** Implementar endpoint dinámico de topología UI y gobernanza de caché (Redis).
- **Alineación Arquitectónica:**
  - **ADR-001 (Hexagonal):** Toda lógica de roles y menús vive en `domain/` y `application/`. El endpoint `GET /api/v1/users/me/menu-layout` pertenece al adaptador REST.
  - **ADR-011 (CQRS V1):** La consulta del menú es un Read-Model, por lo tanto, no debe mutar estado y se puede optimizar.

## 2. Contexto de Negocio
Actualmente, no existe un endpoint que entregue el layout del menú consolidado al frontend. El frontend no debe sobrecargar el JWT con todos los módulos de todos los roles (Anti-JWT Bloat). El backend debe unificar matemáticamente (unión) los módulos permitidos de los múltiples roles que tenga el usuario, cachear la respuesta en Redis por desempeño y limpiar dicha caché cuando se revoquen/modifiquen roles, logrando una auto-curación Zero-Trust.

## 3. Criterios de Aceptación
- **CA-30 (Superposición Inclusiva Multirrol):** Si un usuario tiene múltiples roles asignados, el backend unirá sus permisos (`UNION` matemática) devolviendo un menú unificado sin duplicados.
- **CA-31 (Arquitectura Endpoint Dinámico):** Debe existir un endpoint `GET /api/v1/users/me/menu-layout` que retorne un JSON estructurado de los módulos principales (Workdesk, Service Delivery, BAM, Modeler, Integración, Proyectos, Administración) a los que tiene acceso.
- **CA-32 (Caché Híbrida y Auto-Curación Zero-Trust):** 
  - Anotar el cálculo del menú con `@Cacheable` (Redis) para no sobrecargar la BD por cada request.
  - Anotar métodos de revocación o modificación de roles con `@CacheEvict` para el usuario específico, garantizando que el caché se purgue automáticamente.

## 4. Directrices Técnicas y Arquitectónicas
- Implementar el endpoint en `UserController` o `UserSecurityController`.
- La lógica del cálculo (CA-30) se debe ubicar en un servicio de dominio o caso de uso (`UserMenuUseCase`), cruzando el ID del usuario con `sys_role` o la tabla pivote de roles (verificar implementación actual del módulo `IdentityGovernance`).
- Retornar un JSON limpio (ej. `["WORKDESK", "MODELER", "ADMINISTRATION"]` o un árbol estructurado según lo que el Frontend espere). Documentarlo claramente en tu Approval Request.
- **Compilación obligatoria:** Ejecuta el protocolo Zero-Trust SRE documentado en `.agents/skills/backend_sre_compilation_audit/SKILL.md` (§0 a §2). Si hay bloqueos de infraestructura, aplica el protocolo de reporte 3B.

## 5. Estructura de Archivos Esperada
- `src/main/java/com/ibpms/core/security/adapter/in/web/UserMenuController.java` (o similar)
- `src/main/java/com/ibpms/core/security/application/port/in/GetUserMenuUseCase.java`
- Configuración de caché habilitada (`@EnableCaching`).

## 6. Instrucciones Operativas y de Comunicación
> ⚠️ **IMPORTANTE:** Todo desarrollo o configuración debe quedar exhaustivamente documentado. Se exige **PRECISIÓN QUIRÚRGICA** en cada cambio para evitar efectos colaterales en funcionalidades existentes. Se han detectado regresiones en iteraciones previas; cualquier daño a funcionalidades adyacentes será motivo de rechazo inmediato.

> **INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
> 1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
> 2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
> 3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_backend.md`.
> 4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_backend.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
> 5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.

> 📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
> - Aplica estrictamente el protocolo de pruebas **TDD** documentado en `.agents/skills/tdd_first/SKILL.md` (Red -> Green -> Refactor) antes de hacer lógica.
> - Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md` (naming, error handling, modularidad).
