# 🧠→⚙️ Handoff Bug-Fix: ARQUITECTO LÍDER → BACKEND - JAVA
# BUG-US005-VERSIONS-BE: Corrección del endpoint de Historial de Versiones

**Emitido por:** 🧠 ARQUITECTO LÍDER
**Destinatario:** ⚙️ BACKEND - JAVA
**Fecha:** 2026-06-06T13:58:00-05:00
**Rama de corrección:** `DevDavid/bugfix/US-005-versions-api`
**Prioridad:** 🔴 Alta
**Dependencia:** Ninguna

---

## 📖 LECTURAS OBLIGATORIAS (Ejecutar ANTES de codificar)

```bash
# 1. Política del Enjambre (Leyes Globales 0-4)
cat .cursorrules

# 2. Skill principal del agente Backend
cat .agents/skills/backend_sre_compilation_audit/SKILL.md

# 3. Skills transversales
cat .agents/skills/clean_code_standards/SKILL.md
cat .agents/skills/addyosmani_sre_discipline/SKILL.md

# 4. ADRs relevantes al bug
cat docs/architecture/adr-001-hexagonal-architecture.md

# 5. Diagnóstico del bug
cat .agentic-sync/bug_diagnosis_us005_versions.md
```

> ⚠️ **LEY GLOBAL 3 — Trazabilidad Inversa:** TODO el código modificado DEBE llevar
> `// @Traceability: US-005, CA-15, BUG-FIX: Retornar lista vacía de versiones si el proceso no tiene despliegues`.
> Esto es INNEGOCIABLE.

---

## 🔬 Diagnóstico del Arquitecto

El endpoint `/api/v1/design/processes/{processDefinitionKey}/versions` arroja `IllegalArgumentException` (HTTP 400) cuando un proceso no ha sido persistido o es un borrador. Además, las llaves de la respuesta no se mapean completamente con los campos requeridos por el frontend (fecha, autor y estado).

| Hallazgo | Ubicación | Detalle |
|----------|:---------:|---------|
| IllegalArgumentException sin capturar | [BpmnDesignController.java:256-264](file:///y:/home/haroltandrsgmezagu/proyectos/ibpms-platform/backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/web/BpmnDesignController.java#L256-L264) | Invoca a `bpmnDesignService.obtenerPorTechnicalId` que lanza error si el technical_id no existe. Debe atrapar la excepción y devolver lista vacía. |

---

## 🎯 Instrucciones Quirúrgicas

### Paso 1: Modificar el endpoint en el Controlador del Backend
**Archivo:** `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/web/BpmnDesignController.java`

Modifica el método `getProcessVersions` para interceptar `IllegalArgumentException` y retornar `200 OK` con un array vacío. También valida si la versión actual es `0` para retornar una lista vacía, y agrega el mapeo de campos DTO para `date`, `author` y `status`.

```java
    // @Traceability: US-005, CA-15, BUG-FIX: Retornar lista vacía de versiones si el proceso no tiene despliegues
    @GetMapping("/{processDefinitionKey}/versions")
    public ResponseEntity<List<Map<String, Object>>> getProcessVersions(@PathVariable("processDefinitionKey") String processDefinitionKey) {
        try {
            var dto = bpmnDesignService.obtenerPorTechnicalId(processDefinitionKey);
            if (dto == null || dto.getCurrentVersion() == null || dto.getCurrentVersion() == 0) {
                return ResponseEntity.ok(List.of());
            }
            List<Map<String, Object>> versions = List.of(
                Map.of(
                    "versionId", dto.getCurrentVersion(),
                    "deploymentId", "dep-" + processDefinitionKey,
                    "isLatest", true,
                    "date", dto.getUpdatedAt() != null ? dto.getUpdatedAt().toString() : "",
                    "author", dto.getCreatedBy() != null ? dto.getCreatedBy() : "Sistema",
                    "status", dto.getStatus() != null ? dto.getStatus() : "BORRADOR"
                )
            );
            return ResponseEntity.ok(versions);
        } catch (IllegalArgumentException e) {
            // Retorna una lista vacía de manera segura si no existe el proceso en base de datos
            return ResponseEntity.ok(List.of());
        }
    }
```

⚠️ **RESTRICCIÓN CRÍTICA:** NO modifiques NADA fuera del método `getProcessVersions`.

---

## ✅ Criterios de Aceptación (DoD)

| # | Criterio | Evidencia |
|---|----------|-----------|
| 1 | El test `testGetProcessVersionsNotFoundReturnsEmptyList` pasa exitosamente | Ejecutar `mvn test -Dtest=SandboxGovernanceTest` en consola del host nativo y observar BUILD SUCCESS |
| 2 | Compilación sin errores y advertencias de tipado | Ejecutar el build de Maven del proyecto core sin fallos |
| 3 | Código documentado con @Traceability | `git diff` muestra comentarios con la anotación `@Traceability: US-005, CA-15, BUG-FIX` |
| 4 | No se usó `git stash` | Confirmar que los cambios se guardaron directo usando `git commit` y `git push` |

---

## 🚦 SECUENCIA DE EJECUCIÓN

1. Leer las lecturas obligatorias (Sección 2).
2. Crear y cambiar a la rama: `git checkout -b DevDavid/bugfix/US-005-versions-api`.
3. Aplicar las correcciones quirúrgicas en `BpmnDesignController.java`.
4. Ejecutar el build y pruebas en el host nativo Windows (usando Maven en `backend/ibpms-core`):
   `mvn clean test -Dtest=SandboxGovernanceTest`
5. Certificar que el test `testGetProcessVersionsNotFoundReturnsEmptyList` pasa.
6. Realizar el commit y push:
   `git add . && git commit -m "fix(backend): US-005 BUG-FIX get process versions empty list" && git push origin DevDavid/bugfix/US-005-versions-api`

---

## 📋 DIRECTIVA DE INFRAESTRUCTURA HÍBRIDA

> El backend Spring Boot corre en consola local (NO en Docker) en el puerto `8080`.
> 1. Si no responde: `cd backend/ibpms-core && mvn spring-boot:run -Dspring-boot.run.profiles=default`.
> 2. **PROHIBIDO** levantar el backend vía Docker o modificar `docker-compose.yml`.

---

## 📋 Instrucciones para Copiar y Pegar

```
Asume el rol de ⚙️ BACKEND - JAVA (Agente de Corrección Quirúrgica de Bugs).

ANTES DE HACER CUALQUIER COSA, lee obligatoriamente estos archivos en este orden:
1. cat .cursorrules
2. cat .agents/skills/backend_sre_compilation_audit/SKILL.md
3. cat .agents/skills/clean_code_standards/SKILL.md
4. cat docs/architecture/adr-001-hexagonal-architecture.md
5. cat .agentic-sync/bug_diagnosis_us005_versions.md
6. cat .agentic-sync/handoff_bugfix_backend_us005_versions.md

TU MISIÓN:
1. Posicionarte en la rama del bugfix: git checkout -b DevDavid/bugfix/US-005-versions-api
2. Aplicar la corrección quirúrgica en getProcessVersions en BpmnDesignController.java
3. Documentar con // @Traceability: US-005, CA-15, BUG-FIX...
4. Compilar y ejecutar pruebas con mvn clean test -Dtest=SandboxGovernanceTest
5. Commit: git add . && git commit -m "fix(backend): US-005 BUG-FIX get process versions empty list" && git push

REGLAS INQUEBRANTABLES:
- PROHIBIDO modificar archivos fuera del alcance del handoff.
- PROHIBIDO crear funcionalidades nuevas. Solo reparar.
- PROHIBIDO omitir @Traceability en el código modificado.
- PROHIBIDO usar git stash. Solo git commit + git push.
```

**INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
1. Inicia estrictamente en modo `PLANNING` y elabora un plan de corrección documentado en `implementation_plan.md`.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.**
3. Guarda tu solicitud de revisión en `.agentic-sync/approval_request_bugfix_backend.md`.
4. Dile al Humano: *"He dejado mi solicitud en `.agentic-sync/approval_request_bugfix_backend.md`. Por favor, ve al chat del Bug-Fix Lead y entrégale el mensaje."*
5. Espera el veredicto. Si aprobado, pasa a `EXECUTION`, corrige, compila y haz `git commit` + `git push`.
