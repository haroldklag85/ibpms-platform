# Handoff Arquitectura - Backend (Iteración 84-DEV-LANE-ROLE-UAT-R2)

## Metadatos
- **US:** US-005 + US-036
- **Rama:** `DevDavid`
- **Exclusiones:** V2, funcionalidades IA Cognitiva, CRM/Portal, QA automatizado
- **Alineación Arquitectónica:**
  - ADR-001 (Arquitectura Hexagonal): Se respetan controladores, adaptadores y capa de dominio.
  - ADR-009 (PostgreSQL + Liquibase): Se usará Liquibase para la migración SQL requerida.
  - El proceso Spring Boot corre en host local (8080), BD en Docker.

## Contexto Técnico
Se ha detectado un bug crítico (R2-01) en UAT humano donde el despliegue de definiciones BPMN retorna 403 Forbidden.
Causa Raíz: El endpoint `/api/v1/design/processes/deploy/{processId}` en `BpmnDesignController.java` (aprox L120) exige un rol manual `BPMN_Release_Manager` que nunca fue creado en la BD ni asignado a usuarios. Además, el control manual es inconsistente porque no acepta `SUPER_ADMIN` como fallback, a diferencia de otros controladores.

## Especificaciones Técnicas (Qué hacer)
Debes realizar exactamente 3 cambios atómicos:

1. **Modificar el chequeo manual en BpmnDesignController.java** (aprox L120) para aceptar `SUPER_ADMIN` como fallback:
   ```java
   boolean hasRole = auth != null && auth.getAuthorities().stream()
       .anyMatch(a -> a.getAuthority().contains("BPMN_Release_Manager")
                    || a.getAuthority().contains("SUPER_ADMIN"));
   ```

2. **Crear el rol `BPMN_Release_Manager` en seed data:**
   Crea una migración SQL `063-seed-bpmn-release-manager-role.sql` en `backend/ibpms-core/src/main/resources/db/changelog/changes/`
   ```sql
   -- 063-seed-bpmn-release-manager-role.sql
   -- US-005/US-036: Seed del rol BPMN_Release_Manager para deploy granular
   INSERT INTO ibpms_security_role (id, name, description, is_system_role, created_at)
   SELECT gen_random_uuid(), 'ROLE_BPMN_Release_Manager', 'Rol especializado para despliegue de definiciones BPMN', true, NOW()
   WHERE NOT EXISTS (SELECT 1 FROM ibpms_security_role WHERE name = 'ROLE_BPMN_Release_Manager');
   ```
   Y regístralo en `db.changelog-master.yaml` agregando al final:
   ```yaml
     - include:
         file: changes/063-seed-bpmn-release-manager-role.sql
   ```

3. **Agregar log de auditoría al deploy:**
   En `BpmnDesignController.java`, DESPUÉS del chequeo de rol exitoso (ej. L148), agrega el log:
   ```java
   log.info("Deploy autorizado para usuario={} con rol={}", 
       auth != null ? auth.getName() : "anonymous", 
       hasRole ? "BPMN_Release_Manager/SUPER_ADMIN" : "sandbox_mode");
   ```

## Instrucciones y Restricciones
- **ANTES DE PROGRAMAR:** Lee `BpmnDesignController.java` completo (`view_file`).
- **VERIFICACIÓN OBLIGATORIA:** Haz un `grep_search` de `BPMN_Release_Manager` en `BpmnDesignController.java` para encontrar TODOS los lugares donde se usa. **Verifica específicamente L500 (`reviewDeployRequest`) y L603.** Si allí también se restringe a `BPMN_Release_Manager` exclusivamente (ej. `@PreAuthorize("hasRole('BPMN_Release_Manager')")`), **DEBES corregirlo** para que también acepte `SUPER_ADMIN` (ej. `hasAnyRole('BPMN_Release_Manager', 'SUPER_ADMIN')`). Tu fix de L120 será inútil si fallan en L500 o L603.
- ⚠️ **IMPORTANTE:** Todo desarrollo o configuración debe quedar exhaustivamente documentado. Se exige **PRECISIÓN QUIRÚRGICA** en cada cambio para evitar efectos colaterales en funcionalidades existentes. Se han detectado regresiones en iteraciones previas; cualquier daño a funcionalidades adyacentes será motivo de rechazo inmediato. 

## Protocolo de Pre-Validación (Criterios de Aceptación Técnicos)
> **📋 DIRECTIVA DE INFRAESTRUCTURA HÍBRIDA:**
> El backend Spring Boot corre en consola local (NO en Docker) en el puerto `8080`. Antes de iniciar tu trabajo, ejecuta el protocolo de pre-validación:
> 1. `curl -s http://localhost:8080/actuator/health` → Debe responder `{"status":"UP"}`.
> 2. Si no responde, arráncalo con: `cd backend && mvn spring-boot:run -pl ibpms-core -Dspring-boot.run.profiles=default`.
> 3. Verifica los servicios Docker: `docker ps` → PostgreSQL (`5433`), Redis (`6379`) y RabbitMQ (`5672`) deben estar `Up (healthy)`.
> **PROHIBIDO** levantar el backend vía Docker o modificar el `docker-compose.yml`.

> 🚫 **Compilación obligatoria:** Ejecuta el protocolo Zero-Trust SRE documentado en `.agents/skills/backend_sre_compilation_audit/SKILL.md` (§0 a §2). Si hay bloqueos de infraestructura, aplica el protocolo de reporte 3B.

## Instrucciones Operativas y de Comunicación
1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_backend.md`.
4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_backend.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION` y programa la solución.
6. **ANTES del commit final**, actualiza `docs/sprints/gobernanza_pm/CHANGELOG_NO_TECNICO.md` explicando tu trabajo en lenguaje sencillo y amigable para un CEO (QUÉ hiciste y PARA QUÉ sirve). 
7. Finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.

> 📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
> - Aplica estrictamente el protocolo de pruebas **TDD** documentado en `.agents/skills/tdd_first/SKILL.md` (Red -> Green -> Refactor) antes de hacer lógica.
> - Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md` (naming, error handling, modularidad).
