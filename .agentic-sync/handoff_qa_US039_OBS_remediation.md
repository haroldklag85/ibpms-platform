# Handoff QA — Remediación OBS-3 (Auditoría I-72-DEV)
## US-039 | Rama: `sprint-3/informe_auditoriaSprint1y2`

---

## Contexto (Orden del Arquitecto Líder)

La auditoría integral de la Iteración 72-DEV para US-039 (CA-4 al CA-8) fue **APROBADA CON OBSERVACIONES**. Se detectó el hallazgo **OBS-3** que requiere tu intervención quirúrgica.

> **Referencia de Auditoría:** `auditoria_integral_us039_iteracion72DEV.md`, sección "Consolidado de Hallazgos".

---

## Hallazgo Asignado

### OBS-3 — Código de Respuesta HTTP Incorrecto en Test (Severidad: 🟡 Menor)
**CA afectado:** CA-4
**Archivo:** `backend/ibpms-core/src/test/java/com/ibpms/poc/infrastructure/web/security/GenericFormIntegrationTest.java`
**Línea afectada:** L102 (`.statusCode(200)`)

**Problema:**
El test `testCa4_ValidPayloadShouldPass` espera un HTTP `200 OK`, pero el controller `GenericFormController.java` L45 retorna `ResponseEntity.noContent().build()` que emite HTTP `204 No Content`. Este test **fallará en integración real** contra el servidor levantado.

**Solución Exacta:**
```java
// GenericFormIntegrationTest.java — ANTES (línea ~102):
    .statusCode(200);

// GenericFormIntegrationTest.java — DESPUÉS:
    .statusCode(204);
```

**Razonamiento:** El HTTP 204 es semánticamente correcto para un POST que completa una tarea sin retornar body. El test debe alinearse al contrato real del controller.

---

## Verificación Obligatoria
1. Asegurar que el test pasa compilación: `mvn test-compile -pl ibpms-core`
2. Si tienes Docker disponible, ejecutar: `mvn test -pl ibpms-core -Dtest=GenericFormIntegrationTest#testCa4_ValidPayloadShouldPass`
3. Verificar que los otros 5 tests de la suite siguen pasando sin regresión.

---

## Hallazgo Adicional (Verificación de Consistencia — NO es cambio de código)

### OBS-2 — Reevaluación: La `@Size(max=10)` YA EXISTE ✅
**CA afectado:** CA-5
**Estado:** **CERRADO — Falso Positivo en la Auditoría**

Durante la generación del reporte de auditoría, se identificó que `GenericFormConfigUpdateRequest.java` carecía de `@Size(max=10)`. Sin embargo, una inspección posterior del archivo revela que **la anotación ya estaba implementada en L11**:
```java
@Size(max = 10, message = "Whitelist cannot exceed 10 variables")
private List<String> whitelist;
```
Además, el controller `ProcessDesignController.java` L33 aplica `@Valid` correctamente. **No se requiere acción adicional para OBS-2.**

**Tarea QA:** Agregar un test de regresión que documente esta validación:
```java
@Test
@DisplayName("CA-5: PUT /generic-form-config con >10 claves en whitelist -> HTTP 400")
void testCa5_WhitelistExceeds10ShouldFail() {
    List<String> tooManyKeys = IntStream.range(0, 11)
        .mapToObj(i -> "var_" + i)
        .collect(Collectors.toList());

    Map<String, Object> payload = new HashMap<>();
    payload.put("whitelist", tooManyKeys);

    given()
        .contentType(ContentType.JSON)
        .body(payload)
    .when()
        .put("/design/processes/test-process/generic-form-config")
    .then()
        .statusCode(400);
}
```

---

## Restricciones QA
1. **Cambio mínimo**: Corregir 1 línea (200 → 204) y agregar 1 test nuevo de regresión.
2. Correspondencia Gherkin bidireccional: cada test DEBE referenciar el CA que valida via `@DisplayName`.
3. **No usar** `git stash`. Solo `git commit` + `git push`.
4. **Convención de commit:** `fix(qa): OBS-3 correct HTTP status assertion and add CA-5 whitelist regression test`

---

> **INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
> 1. Este cambio es de **ejecución directa** — NO requiere fase PLANNING dado que son correcciones quirúrgicas con instrucciones exactas.
> 2. Ejecuta los cambios, compila, verifica tests, y haz `git commit` + `git push`.
> 3. Al finalizar, graba tu confirmación de cierre en `.agentic-sync/approval_request_qa.md` indicando: `OBS-3 REMEDIADA + Test Regresión OBS-2 añadido — commit: <hash>`.
> 4. Dile al Humano: *"Humano, he remediado OBS-3 y registrado el cierre en `.agentic-sync/approval_request_qa.md`. Entrégale este mensaje al Arquitecto Líder."*
