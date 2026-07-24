# 🏗️ Handoff Arquitectónico - Backend

## 1. Metadatos y SSOT
- **Iteración:** 84-DEV-LANE-ROLE-UAT-BUGS
- **User Story:** US-005 + US-036 (Lane-Role Assignment — Corrección UAT)
- **Criterios de Aceptación:** Corrección de Bug UAT B-04 (Backend)
- **Rama Git:** `DevDavid`
- **Exclusiones:** V2, funcionalidades IA Cognitiva, CRM/Portal, QA automatizado
- **Necesita QA:** `no` (Validación por UAT humano post-corrección)
- **SSOT:** `.agentic-sync/po_instruction_84DEV_UAT_BUGS.md`

## 2. Alineación Arquitectónica y ADRs
- **ADR-001-hexagonal-architecture.md:** Se expone el nuevo endpoint GET manteniendo la responsabilidad en la capa de controladores y delegando a `formDesignService`.
- **Trazabilidad:** Se repara la discrepancia entre el Frontend y el Backend al proporcionar el endpoint REST que el Frontend espera consumir.

> **📋 DIRECTIVA DE INFRAESTRUCTURA HÍBRIDA:**
> El backend Spring Boot corre en consola local (NO en Docker) en el puerto `8080`. Antes de iniciar tu trabajo, ejecuta el protocolo de pre-validación:
> 1. `curl -s http://localhost:8080/actuator/health` → Debe responder `{"status":"UP"}`.
> 2. Si no responde, arráncalo con: `cd backend && mvn spring-boot:run -pl ibpms-core -Dspring-boot.run.profiles=default`.
> 3. Verifica los servicios Docker: `docker ps` → PostgreSQL (`5433`), Redis (`6379`) y RabbitMQ (`5672`) deben estar `Up (healthy)`.
> **PROHIBIDO** levantar el backend vía Docker o modificar el `docker-compose.yml`.

> ⚠️ **IMPORTANTE:** Todo desarrollo o configuración debe quedar exhaustivamente documentado. Se exige **PRECISIÓN QUIRÚRGICA** en cada cambio para evitar efectos colaterales en funcionalidades existentes. Se han detectado regresiones en iteraciones previas; cualquier daño a funcionalidades adyacentes será motivo de rechazo inmediato.

## 3. Rutas Exactas y Contexto Preexistente / 4. Snippets Prescriptivos

### B-04 Backend: Endpoint GET en FormDesignController
**Archivo:** `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/web/FormDesignController.java`
**Contexto:** El endpoint `GET /api/v1/forms/{technicalName}` no existe, lo que causa un error 405 en el frontend al intentar cargar un formulario.

**Acción:** 
1. Leer PRIMERO el controlador con la herramienta `view_file` para asimilar el contexto de la clase actual.
2. Agregar un método `GET` para buscar por `technicalName` justo antes del método `@PostMapping("/{id}")` que se ubica aprox en la línea 95.
3. Devolver un objeto de tipo `ResponseEntity<FormDesignDTO>`.
4. El método debe usar el `formDesignService` para buscar el formulario por `technicalName` y retornar sus datos completos (schema, title, pattern, etc.). Si no encuentra el formulario retornar 404 (NotFound).

```java
    @GetMapping("/{technicalName}")
    public ResponseEntity<FormDesignDTO> getForm(@PathVariable String technicalName) {
        // Implementar búsqueda de formulario por technicalName.
        // Utilizar el servicio formDesignService o repositorio para obtener la información.
        // Si no se encuentra, devolver ResponseEntity.notFound().build();
        // Si se encuentra, retornar ResponseEntity.ok(formDTO);
    }
```
*Asegúrate de NO inventar DTOs de respuesta; usar el mismo tipo FormDesignDTO que usa el POST /{id} u otros endpoints de lectura del controlador.*

## 5. Matriz de QA y Testing Atómico
*Omitido por requerimiento (`Necesita QA = no`). Validación delegada a UAT Humano.*

## 6. Mensaje de Despacho
> Compilación obligatoria: Ejecuta el protocolo Zero-Trust SRE documentado en `.agents/skills/backend_sre_compilation_audit/SKILL.md` (§0 a §2). Si hay bloqueos de infraestructura, aplica el protocolo de reporte 3B.

> **INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
> 1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
> 2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
> 3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_Backend.md`.
> 4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_Backend.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
> 5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION` y programa la solución.
> 6. **ANTES del commit final**, actualiza `docs/sprints/gobernanza_pm/CHANGELOG_NO_TECNICO.md` explicando tu trabajo en lenguaje sencillo y amigable para un CEO (QUÉ hiciste y PARA QUÉ sirve). 
> 7. Finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.

> 📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
> - Aplica estrictamente el protocolo de pruebas **TDD** documentado en `.agents/skills/tdd_first/SKILL.md` (Red -> Green -> Refactor) antes de hacer lógica.
> - Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md` (naming, error handling, modularidad).
