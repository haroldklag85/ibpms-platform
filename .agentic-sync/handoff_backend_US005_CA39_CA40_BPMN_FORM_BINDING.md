# 🧠→⚙️ Handoff: ARQUITECTO LÍDER → BACKEND - JAVA
# sprint-01-DevDavid-BPMN: Estabilización del Catálogo de Formularios Activos para Vinculación BPMN (US-005 CA-39/CA-40)

**Emitido por:** 🧠 ARQUITECTO LÍDER
**Destinatario:** ⚙️ BACKEND - JAVA
**Fecha:** 2026-06-22T15:24:00-05:00
**Sprint:** sprint-01-DevDavid-BPMN
**Prioridad:** 🔴 Alta — Requerimiento urgente del cliente
**Dependencia:** Ninguna — arranca primero
**Rama de trabajo:** `DevDavid`

---

## 📖 LECTURAS OBLIGATORIAS (Ejecutar ANTES de codificar)

```bash
# 1. Arquitectura Core del proyecto
cat docs/architecture/arquitecturar.md

# 2. Skill principal del agente Backend
cat .agents/skills/backend_sre_compilation_audit/SKILL.md

# 3. Skills transversales aplicables
cat .agents/skills/clean_code_standards/SKILL.md
cat .agents/skills/zero_mock_enforcement/SKILL.md

# 4. ADRs relevantes
cat docs/architecture/adr-001-hexagonal-architecture.md
cat docs/architecture/adr-003-camunda7-embedded.md

# 5. SSOT de la US
cat docs/requirements/v1_user_stories_index.md
# Luego lee la sección US-005 en:
cat docs/requirements/epics/epic_B_formularios_bpmn.md
# Busca específicamente: CA-39 (FormKey como Dropdown) y CA-40 (Consistencia de Patrón)

# 6. Contratos API
cat docs/sprints/gobernanza_pm/API_CONTRACTS.md
```

> ⚠️ **LEY GLOBAL 3 — Trazabilidad Inversa:** TODO el código nuevo DEBE llevar
> la anotación @Traceability o comentario `// @Traceability: US-005, CA-39, CA-40`.
> Esto es INNEGOCIABLE.

> ⚠️ **POLÍTICA ANTIAMNESIA:** Antes de codificar, DEBES re-entrenar tu contexto leyendo los archivos listados arriba. PROHIBIDO asumir cómo funciona el proyecto.

---

## 🔬 Diagnóstico del Arquitecto

El BPMN Designer del frontend llama a `GET /api/v1/forms/active?processKey={key}` para popular el dropdown de FormKey en las UserTasks. Sin embargo, cuando el usuario abre el BpmnDesigner.vue con un proceso de prueba (`processId=datos`), **el dropdown aparece vacío** a pesar de que existen formularios guardados en la base de datos.

La investigación forense revela que el endpoint `FormCatalogController` existe en el backend, pero su lógica de filtrado por `processKey` podría estar devolviendo resultados vacíos si el parámetro no coincide con ningún formulario registrado. Adicionalmente, la relación Form↔Process vive exclusivamente en el XML BPMN (atributo `camunda:formKey`), NO en una tabla relacional dedicada.

| Violación/Hallazgo | Ubicación | Detalle |
|--------------------|:---------:|---------|
| Endpoint de catálogo posiblemente filtra demasiado estricto | `FormCatalogController.java` | Si filtra por `processKey` y no hay forms asociados a ese key, retorna lista vacía |
| No hay tests del controller | `FormDesignControllerTest.java` | Clase vacía: `public class FormDesignControllerTest {}` |
| Fallback a mocks en Frontend | `BpmnDesigner.vue:2601-2607` | Si el endpoint falla, el Frontend muestra 4 formularios hardcodeados falsos |
| API_CONTRACTS.md desactualizado | `docs/sprints/gobernanza_pm/API_CONTRACTS.md` | El endpoint `GET /api/v1/forms/active` NO está documentado en los contratos |

**Componentes Backend existentes reutilizables:**

| Componente | Ubicación | Propósito |
|-----------|:---------:|---------|
| `FormCatalogController` | `backend/ibpms-core/.../infrastructure/web/` | Controller que sirve el catálogo de forms activos |
| `FormDesignService` | `backend/ibpms-core/.../application/service/FormDesignService.java` | Servicio con `listarCatalogo()` y `crear()` |
| `FormDefinitionPort` | `backend/ibpms-core/.../application/port/out/FormDefinitionPort.java` | Puerto hexagonal de persistencia |
| `ibpms_form_design` | Tabla PostgreSQL | Almacena: id, name, technical_name, pattern, status |

---

## 🎯 Instrucciones Quirúrgicas

### Paso 1: Investigar el endpoint actual `GET /api/v1/forms/active`

**Archivo:** `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/web/FormCatalogController.java`

Lee este archivo completo. Identifica:
- ¿Qué parámetros de query acepta? (¿`processKey`? ¿`pattern`?)
- ¿Qué service/port llama?
- ¿Qué filtros aplica sobre los formularios?

Si el endpoint filtra por `processKey` de forma obligatoria, ese es el bug: **los formularios simples no están asociados a un `processKey` al momento de crearlos en el Form Designer**. El `processKey` solo se establece cuando el BPMN asigna el `formKey` en una UserTask.

### Paso 2: Corregir la lógica de filtrado

**Archivo:** `FormCatalogController.java` y/o el servicio que llama

El endpoint DEBE:
1. Si `processKey` está presente: filtrar formularios por ese proceso.
2. Si `processKey` está AUSENTE o vacío: **retornar TODOS los formularios activos** (sin filtro de proceso).
3. SIEMPRE filtrar por `status = ACTIVE` (no retornar borradores ni archivados).
4. SIEMPRE incluir los campos: `id`, `name`, `technicalName`, `pattern` (SIMPLE o IFORM_MAESTRO).

```java
// @Traceability: US-005, CA-39, CA-40
@GetMapping("/api/v1/forms/active")
public ResponseEntity<List<FormCatalogDTO>> getActiveForms(
        @RequestParam(required = false) String processKey,
        @RequestParam(required = false) String pattern) {
    
    List<FormCatalogDTO> forms;
    if (processKey != null && !processKey.isBlank()) {
        forms = formDesignService.listarFormulariosActivosPorProceso(processKey);
    } else {
        forms = formDesignService.listarTodosLosFormulariosActivos();
    }
    
    // CA-40: Filtrar por patrón si se especifica
    if (pattern != null && !pattern.isBlank()) {
        forms = forms.stream()
            .filter(f -> f.getPattern().equalsIgnoreCase(pattern))
            .collect(Collectors.toList());
    }
    
    return ResponseEntity.ok(forms);
}
```

### Paso 3: Crear/verificar el DTO de catálogo

**Archivo:** Crear o verificar `FormCatalogDTO.java` en `infrastructure/web/dto/`

```java
// @Traceability: US-005, CA-39
public record FormCatalogDTO(
    UUID id,
    String name,
    String technicalName,
    String pattern,  // "SIMPLE" o "IFORM_MAESTRO"
    Integer version,
    boolean isQaCertified
) {}
```

### Paso 4: Asegurar que el servicio retorna formularios activos

**Archivo:** `FormDesignService.java`

Verifica que el método `listarCatalogo()` o equivalente:
1. Consulta la tabla `ibpms_form_design` filtrando por `status != 'DELETED'`
2. Retorna al menos los formularios que el usuario ya ha creado
3. Si no existe un método que retorne todos los forms activos, créalo:

```java
// @Traceability: US-005, CA-39
public List<FormCatalogDTO> listarTodosLosFormulariosActivos() {
    return formDesignRepository.findByStatusNot("DELETED").stream()
        .map(this::toCatalogDTO)
        .collect(Collectors.toList());
}
```

### Paso 5: Escribir test del endpoint

**Archivo:** `FormDesignControllerTest.java` (actualmente VACÍO)

Escribe al menos 2 tests:

```java
// @Traceability: US-005, CA-39
@Test
void getActiveForms_sinProcessKey_retornaTodosLosFormsActivos() {
    // Given: existen formularios en la BD
    // When: GET /api/v1/forms/active (sin processKey)
    // Then: retorna HTTP 200 con lista no vacía
}

// @Traceability: US-005, CA-40
@Test
void getActiveForms_conPatternSimple_retornaSoloSimples() {
    // Given: existen formularios SIMPLE e IFORM_MAESTRO
    // When: GET /api/v1/forms/active?pattern=SIMPLE
    // Then: retorna solo los de patrón SIMPLE
}
```

---

## ✅ Criterios de Aceptación (DoD)

| # | Criterio | Evidencia |
|---|----------|-----------|
| 1 | `GET /api/v1/forms/active` sin parámetros retorna TODOS los formularios activos (no vacío) | `curl http://localhost:8080/api/v1/forms/active` → respuesta con al menos 1 formulario |
| 2 | `GET /api/v1/forms/active?pattern=SIMPLE` retorna solo formularios con patrón SIMPLE | `curl "http://localhost:8080/api/v1/forms/active?pattern=SIMPLE"` → cada item tiene `pattern: "SIMPLE"` |
| 3 | Los formularios creados en el Form Designer aparecen en la respuesta del endpoint | Crear un form en `localhost:5173/admin/modeler/forms/designer`, luego llamar al endpoint y verificar que aparece |
| 4 | `FormDesignControllerTest` tiene al menos 2 tests pasando | `mvn test -pl ibpms-core -Dtest=FormDesignControllerTest` → 2 tests PASSED |
| 5 | Compilación exitosa + commit en rama `DevDavid` | `mvn clean compile -pl ibpms-core` → BUILD SUCCESS |

---

## 🚦 SECUENCIA DE EJECUCIÓN

1. Ejecutar protocolo de pre-validación de backend (health check en puerto 8080)
2. Leer `FormCatalogController.java` completo para entender estado actual
3. Leer `FormDesignService.java` para entender métodos de listado existentes
4. Corregir el endpoint para que retorne formularios sin requerir `processKey`
5. Escribir tests en `FormDesignControllerTest.java`
6. Compilación obligatoria: Ejecuta el protocolo Zero-Trust SRE documentado en `.agents/skills/backend_sre_compilation_audit/SKILL.md` (§0 a §2). Si hay bloqueos de infraestructura, aplica el protocolo de reporte 3B.
7. Verificar con curl que el endpoint responde correctamente
8. Actualizar `docs/sprints/gobernanza_pm/CHANGELOG_NO_TECNICO.md` con lenguaje no técnico:
   > "Se corrigió el servicio que lista los formularios disponibles para que el diseñador de procesos BPMN pueda encontrarlos y asociarlos a las tareas del flujo de trabajo."
9. `git add . && git commit -m "fix(forms): corregir catálogo de formularios activos para binding BPMN CA-39/CA-40" && git push origin DevDavid`

---

## 📋 DIRECTIVA DE INFRAESTRUCTURA HÍBRIDA:
> El backend Spring Boot corre en consola local (NO en Docker) en el puerto `8080`. Antes de iniciar tu trabajo, ejecuta el protocolo de pre-validación:
> 1. `curl -s http://localhost:8080/actuator/health` → Debe responder `{"status":"UP"}`.
> 2. Si no responde, arráncalo con: `cd backend && mvn spring-boot:run -pl ibpms-core -Dspring-boot.run.profiles=default`.
> 3. Verifica los servicios Docker: `docker ps` → PostgreSQL (`5433`), Redis (`6379`) y RabbitMQ (`5672`) deben estar `Up (healthy)`.
> **PROHIBIDO** levantar el backend vía Docker o modificar el `docker-compose.yml`.

---

## 📋 Instrucciones para Copiar y Pegar

```
Asume el rol de ⚙️ Desarrollador Backend Java.

ANTES DE HACER CUALQUIER COSA, lee obligatoriamente estos archivos en este orden exacto:

1. cat docs/architecture/arquitecturar.md
2. cat .agents/skills/backend_sre_compilation_audit/SKILL.md
3. cat .agents/skills/clean_code_standards/SKILL.md
4. cat .agents/skills/zero_mock_enforcement/SKILL.md
5. cat docs/architecture/adr-001-hexagonal-architecture.md
6. cat .agentic-sync/handoff_backend_US005_CA39_CA40_BPMN_FORM_BINDING.md

TU MISIÓN:

1. Investigar y corregir el endpoint GET /api/v1/forms/active para que retorne TODOS los formularios activos cuando no se proporciona processKey
2. Asegurar filtrado por pattern (SIMPLE vs IFORM_MAESTRO) para soporte de CA-40
3. Escribir tests en FormDesignControllerTest.java (actualmente vacío)
4. Compilación: Ejecutar protocolo Zero-Trust SRE (.agents/skills/backend_sre_compilation_audit/SKILL.md)
5. Verificar con curl: curl http://localhost:8080/api/v1/forms/active
6. Bitácora: Agrega entrada en docs/sprints/gobernanza_pm/CHANGELOG_NO_TECNICO.md
7. Commit: git add . && git commit -m "fix(forms): corregir catálogo de formularios activos para binding BPMN CA-39/CA-40" && git push origin DevDavid

REGLAS INQUEBRANTABLES:
- PROHIBIDO crear mocks o datos falsos. Usa la BD real PostgreSQL en puerto 5433.
- PROHIBIDO modificar la tabla ibpms_form_design. Solo modificar código Java.
- PROHIBIDO romper endpoints existentes. Precisión quirúrgica.
- Todo código nuevo DEBE tener @Traceability: US-005, CA-39, CA-40
- Es OBLIGATORIO actualizar el CHANGELOG_NO_TECNICO.md antes del commit final.
```

---

**INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_BACKEND_US005_CA39.md`.
4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_BACKEND_US005_CA39.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION` y programa la solución.
6. **ANTES del commit final**, actualiza `docs/sprints/gobernanza_pm/CHANGELOG_NO_TECNICO.md` explicando tu trabajo en lenguaje sencillo y amigable para un CEO.
7. Finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama `DevDavid`. Queda estrictamente prohibido usar git stash.

> 📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
> - Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md`.
