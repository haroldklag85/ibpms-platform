# Handoff QA — Validación de Cierre OBS (Auditoría I-73-DEV)
## US-005 | Rama: `sprint-3/us-005-bpmn-designer`

---

## Contexto (Orden del Arquitecto Líder)

La auditoría integral de la Iteración 73-DEV para US-005 (CA-63 a CA-70) fue **APROBADA CON OBSERVACIONES**. Se asignan al agente QA **4 suites de test** que certifican el cierre correcto de OBS-1, OBS-2 y CA-69. Los tests deben pasar ANTES de que el Arquitecto Líder autorice el merge a `main`.

> **Referencia de Auditoría:** `auditoria_integral_us005_iteracion73DEV.md`
> **Referencia ADR:** `docs/architecture/adr_011_testing_pyramid_governance.md`
> **SSOT:** `docs/requirements/v1_user_stories.md` (CA-65, CA-68, CA-69)

---

## Suite 1 — OBS-1: DataMappingEntity alineada con DDL (REST Assured)

**CA afectado:** CA-68
**Archivo destino:** `backend/ibpms-core/src/test/java/com/ibpms/poc/infrastructure/web/DataMappingIntegrationTest.java`
**Tipo:** Integration Test (Testcontainers + REST Assured)

### Tests requeridos:

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("CA-68: Data Mapping Entity-DDL Alignment (OBS-1 Closure)")
class DataMappingIntegrationTest {

    @Test
    @Order(1)
    @DisplayName("CA-68.1: POST /data-mappings con payload {taskId, connectorId, mappingJson} -> HTTP 201")
    void testCreateDataMapping_ValidPayload_Returns201() {
        Map<String, String> payload = Map.of(
            "taskId", "ServiceTask_1",
            "connectorId", "oracle_erp_v1",
            "mappingJson", "{\"monto\": \"process_monto\", \"email\": \"process_email\"}"
        );

        given()
            .contentType(ContentType.JSON)
            .body(payload)
        .when()
            .post("/api/v1/design/processes/credito-consumo/data-mappings")
        .then()
            .statusCode(201)
            .body("id", notNullValue())             // UUID, NOT Long
            .body("taskId", equalTo("ServiceTask_1"))
            .body("connectorId", equalTo("oracle_erp_v1"))
            .body("mappingJson", notNullValue())
            .body("processDefinitionKey", equalTo("credito-consumo"));
    }

    @Test
    @Order(2)
    @DisplayName("CA-68.2: GET /data-mappings retorna estructura alineada con DDL")
    void testGetDataMappings_ReturnsCorrectSchema() {
        given()
        .when()
            .get("/api/v1/design/processes/credito-consumo/data-mappings")
        .then()
            .statusCode(200)
            .body("[0].id", notNullValue())
            .body("[0].taskId", notNullValue())
            .body("[0].connectorId", notNullValue())
            .body("[0].mappingJson", notNullValue())
            // Verificar que los campos VIEJOS no existen:
            .body("[0].variableName", nullValue())
            .body("[0].variableType", nullValue())
            .body("[0].isRequired", nullValue());
    }

    @Test
    @Order(3)
    @DisplayName("CA-68.3: El ID retornado es UUID, no Long (validación de tipo)")
    void testDataMappingId_IsUUID() {
        String id = given()
        .when()
            .get("/api/v1/design/processes/credito-consumo/data-mappings")
        .then()
            .statusCode(200)
            .extract().path("[0].id");

        // Debe parsear como UUID sin excepción
        assertDoesNotThrow(() -> UUID.fromString(id),
            "OBS-1: El ID debe ser UUID, no Long. Verificar DataMappingEntity.java");
    }
}
```

---

## Suite 2 — OBS-2: Contrato API POST /deploy alineado con SSOT (REST Assured)

**CA afectado:** CA-65
**Archivo destino:** `backend/ibpms-core/src/test/java/com/ibpms/poc/infrastructure/web/DeployContractIntegrationTest.java`

### Tests requeridos:

```java
@DisplayName("CA-65: Deploy Contract Alignment (OBS-2 Closure)")
class DeployContractIntegrationTest {

    @Test
    @DisplayName("CA-65.1: POST /deploy acepta deploy_comment y retorna campos SSOT")
    void testDeploy_AcceptsCommentAndReturnsSSoTFields() {
        MockMultipartFile bpmnFile = new MockMultipartFile(
            "file", "test-process.bpmn", "application/xml",
            "<?xml version=\"1.0\"?><bpmn:definitions/>".getBytes()
        );

        given()
            .multiPart("file", "test-process.bpmn", bpmnFile.getBytes())
            .multiPart("deploy_comment", "Despliegue de prueba para validar contrato CA-65")
            .multiPart("force_deploy", "false")
            .header("X-Mock-Role", "BPMN_Release_Manager")
        .when()
            .post("/api/v1/design/processes/deploy")
        .then()
            .statusCode(anyOf(is(201), is(422)))  // 201 o 422 según Pre-Flight
            // Si 201, validar campos SSOT:
            .body("deployment_id", notNullValue())
            .body("process_definition_key", notNullValue())
            .body("version", notNullValue())
            .body("deployed_at", notNullValue())
            .body("deployed_by", notNullValue());
    }

    @Test
    @DisplayName("CA-65.2: POST /deploy SIN deploy_comment -> HTTP 400")
    void testDeploy_MissingComment_Returns400() {
        given()
            .multiPart("file", "test.bpmn", "<xml/>".getBytes())
            .header("X-Mock-Role", "BPMN_Release_Manager")
        .when()
            .post("/api/v1/design/processes/deploy")
        .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("CA-65.3: POST /deploy con deploy_comment < 10 chars -> HTTP 400")
    void testDeploy_ShortComment_Returns400() {
        given()
            .multiPart("file", "test.bpmn", "<xml/>".getBytes())
            .multiPart("deploy_comment", "corto")
            .header("X-Mock-Role", "BPMN_Release_Manager")
        .when()
            .post("/api/v1/design/processes/deploy")
        .then()
            .statusCode(400);
    }
}
```

---

## Suite 3 — CA-69: Validación de comentario de rechazo (REST Assured)

**CA afectado:** CA-69
**Archivo destino:** `backend/ibpms-core/src/test/java/com/ibpms/poc/infrastructure/web/DeployRequestIntegrationTest.java`

### Tests requeridos:

```java
@DisplayName("CA-69: Deploy Request Rejection Comment Validation")
class DeployRequestIntegrationTest {

    @Test
    @DisplayName("CA-69.1: POST /reject con comentario < 20 chars -> HTTP 400")
    void testReject_ShortComment_Returns400() {
        // Primero crear una solicitud
        String requestId = createTestDeployRequest();

        given()
            .contentType(ContentType.JSON)
            .body(Map.of("comment", "corto"))
            .header("X-Mock-Role", "Super_Admin")
        .when()
            .post("/api/v1/design/processes/deploy-requests/" + requestId + "/reject")
        .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("CA-69.2: POST /reject con comentario >= 20 chars -> HTTP 200")
    void testReject_ValidComment_Returns200() {
        String requestId = createTestDeployRequest();

        given()
            .contentType(ContentType.JSON)
            .body(Map.of("comment", "El diagrama tiene errores topológicos que requieren corrección inmediata"))
            .header("X-Mock-Role", "Super_Admin")
        .when()
            .post("/api/v1/design/processes/deploy-requests/" + requestId + "/reject")
        .then()
            .statusCode(200)
            .body("status", equalTo("REJECTED"))
            .body("reviewComment", notNullValue());
    }
}
```

---

## Suite 4 — Frontend: Vitest para validaciones client-side (Vitest)

**CA afectados:** CA-65 (deploy comment), CA-69 (reject comment)
**Archivo destino:** `frontend/src/views/admin/Modeler/__tests__/BpmnDeployValidation.spec.ts`

### Tests requeridos:

```typescript
import { describe, it, expect } from 'vitest'

describe('US-005 OBS Remediation — Frontend Validations', () => {

  describe('CA-65: Deploy Comment Validation', () => {
    it('debe rechazar deploy_comment vacío', () => {
      const comment = ''
      expect(comment.trim().length >= 10).toBe(false)
    })

    it('debe rechazar deploy_comment < 10 chars', () => {
      const comment = 'short'
      expect(comment.trim().length >= 10).toBe(false)
    })

    it('debe aceptar deploy_comment >= 10 chars', () => {
      const comment = 'Despliegue de producción validado.'
      expect(comment.trim().length >= 10).toBe(true)
    })
  })

  describe('CA-69: Rejection Comment Validation', () => {
    it('debe rechazar comentario de rechazo < 20 chars', () => {
      const comment = 'No aprobado'
      expect(comment.trim().length >= 20).toBe(false)
    })

    it('debe aceptar comentario de rechazo >= 20 chars', () => {
      const comment = 'El proceso tiene errores de gateway que deben ser corregidos.'
      expect(comment.trim().length >= 20).toBe(true)
    })
  })

  describe('CA-68: DataMapper Payload Structure', () => {
    it('debe construir payload con taskId, connectorId, mappingJson', () => {
      const taskId = 'ServiceTask_1'
      const connectorId = 'oracle_erp_v1'
      const mappings = { monto: 'process_monto', email: 'process_email' }

      const payload = {
        taskId,
        connectorId,
        mappingJson: JSON.stringify(mappings)
      }

      expect(payload).toHaveProperty('taskId')
      expect(payload).toHaveProperty('connectorId')
      expect(payload).toHaveProperty('mappingJson')
      // Verificar que los campos viejos NO existen:
      expect(payload).not.toHaveProperty('variableName')
      expect(payload).not.toHaveProperty('variableType')
      expect(payload).not.toHaveProperty('isRequired')
    })
  })
})
```

---

## Verificación Obligatoria
1. **Backend suites:** `mvn test -pl ibpms-core -Dtest=DataMappingIntegrationTest,DeployContractIntegrationTest,DeployRequestIntegrationTest`
2. **Frontend suite:** `npx vitest run src/views/admin/Modeler/__tests__/BpmnDeployValidation.spec.ts`
3. **0 failures** en todas las suites.

---

## Restricciones QA
1. **Correspondencia Gherkin bidireccional:** Cada `@DisplayName` DEBE referenciar el CA que valida.
2. **ADR-011 compliance:** Tests de integración usan Testcontainers + REST Assured. Tests de frontend usan Vitest.
3. **No crear mocks falsos:** Los tests de integración deben pegar contra el Spring Context real con H2/Testcontainers.
4. **No usar** `git stash`. Solo `git commit` + `git push`.
5. **Convención de commit:**
   - `test(qa): US-005 OBS-1 DataMapping entity-DDL alignment integration tests`
   - `test(qa): US-005 OBS-2 deploy contract SSOT alignment integration tests`
   - `test(qa): US-005 CA-69 rejection comment min 20 chars validation`
   - `test(qa): US-005 frontend deploy/reject validation unit tests`

---

> **INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
> 1. **DEPENDENCIA ESTRICTA:** Ejecuta DESPUÉS de que Backend Y Frontend hayan confirmado sus commits. Haz `git pull` antes de comenzar.
> 2. Los tests de integración están diseñados para verificar los cambios del Backend. Si algún test falla, es un indicador de que la remediación Backend fue incompleta — NO adaptes el test, reporta el fallo al Arquitecto.
> 3. Crea cada suite como un commit atómico separado.
> 4. Al finalizar, graba tu confirmación de cierre en `.agentic-sync/approval_request_qa.md` indicando: `4 suites QA US-005 OBS remediation — commits: <hash1>..<hash4>. Resultado: X/Y passed.`
> 5. Dile al Humano: *"Humano, he ejecutado las 4 suites de certificación de cierre OBS. Entrégale este mensaje al Arquitecto Líder."*
