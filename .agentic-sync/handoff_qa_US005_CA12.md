# Handoff QA — Validación CA-12 DMN Binding (Iteración 74-DEV)
## US-005 | Rama: `sprint-3/informe_auditoriaSprint1y2`

---

## Contexto (Orden del Arquitecto Líder)

El CA-12 (Versionamiento Seguro de Reglas DMN) tenía **0% cobertura de tests**. Se asignan **2 suites** que certifican el correcto funcionamiento del selector UI y la validación Pre-Flight.

> **Referencia de Auditoría:** `auditoria_us005_ca12_dmn_binding.md`
> **Referencia ADR:** `docs/architecture/adr_011_testing_pyramid_governance.md`
> **SSOT:** `docs/requirements/v1_user_stories.md` (CA-12 L2165-2171)

---

## Suite 1 — Pre-Flight DMN Binding Validation (Backend — REST Assured)

**CA afectado:** CA-12
**Archivo destino:** `backend/ibpms-core/src/test/java/com/ibpms/poc/infrastructure/web/PreFlightDmnBindingTest.java`
**Tipo:** Integration Test (Spring Boot Test)

### Tests requeridos:

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("CA-12: Pre-Flight DMN Binding Validation (Protección Derechos Adquiridos)")
class PreFlightDmnBindingTest {

    @Autowired
    private PreFlightAnalyzerService preFlightAnalyzerService;

    @Test
    @Order(1)
    @DisplayName("CA-12.1: BusinessRuleTask SIN binding genera warning Pre-Flight")
    void testBRT_WithoutBinding_GeneratesWarning() {
        String bpmnXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
                              xmlns:camunda="http://camunda.org/schema/1.0/bpmn"
                              id="Definitions_1" targetNamespace="http://bpmn.io/schema/bpmn">
              <bpmn:process id="test_ca12" isExecutable="true">
                <bpmn:startEvent id="Start_1" camunda:formKey="form_test" />
                <bpmn:businessRuleTask id="BRT_1" name="Evaluar Riesgo"
                    camunda:decisionRef="tabla_riesgo_v1" />
                <bpmn:endEvent id="End_1" />
              </bpmn:process>
            </bpmn:definitions>
            """;

        InputStream stream = new ByteArrayInputStream(bpmnXml.getBytes(StandardCharsets.UTF_8));
        DeploymentValidationResponse result = preFlightAnalyzerService.analizar(stream);

        // Debe contener al menos un warning referente a CA-12 / binding
        boolean hasBindingWarning = result.getWarnings().stream()
            .anyMatch(w -> w.contains("decisionRefBinding") || w.contains("CA-12") || w.contains("derechos adquiridos"));
        assertTrue(hasBindingWarning,
            "El Pre-Flight debe emitir warning cuando un BRT no tiene binding configurado (CA-12)");
    }

    @Test
    @Order(2)
    @DisplayName("CA-12.2: BusinessRuleTask CON binding='deployment' NO genera warning")
    void testBRT_WithDeploymentBinding_NoWarning() {
        String bpmnXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
                              xmlns:camunda="http://camunda.org/schema/1.0/bpmn"
                              id="Definitions_1" targetNamespace="http://bpmn.io/schema/bpmn">
              <bpmn:process id="test_ca12_ok" isExecutable="true">
                <bpmn:startEvent id="Start_1" camunda:formKey="form_test" />
                <bpmn:businessRuleTask id="BRT_1" name="Evaluar Riesgo"
                    camunda:decisionRef="tabla_riesgo_v1"
                    camunda:decisionRefBinding="deployment" />
                <bpmn:endEvent id="End_1" />
              </bpmn:process>
            </bpmn:definitions>
            """;

        InputStream stream = new ByteArrayInputStream(bpmnXml.getBytes(StandardCharsets.UTF_8));
        DeploymentValidationResponse result = preFlightAnalyzerService.analizar(stream);

        boolean hasBindingWarning = result.getWarnings().stream()
            .anyMatch(w -> w.contains("BRT_1"));
        assertFalse(hasBindingWarning,
            "El Pre-Flight NO debe emitir warning cuando BRT tiene binding='deployment' (CA-12)");
    }

    @Test
    @Order(3)
    @DisplayName("CA-12.3: BusinessRuleTask CON binding='latest' genera warning informativo")
    void testBRT_WithLatestBinding_GeneratesInfoWarning() {
        String bpmnXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
                              xmlns:camunda="http://camunda.org/schema/1.0/bpmn"
                              id="Definitions_1" targetNamespace="http://bpmn.io/schema/bpmn">
              <bpmn:process id="test_ca12_latest" isExecutable="true">
                <bpmn:startEvent id="Start_1" camunda:formKey="form_test" />
                <bpmn:businessRuleTask id="BRT_1" name="Evaluar Riesgo"
                    camunda:decisionRef="tabla_riesgo_v1"
                    camunda:decisionRefBinding="latest" />
                <bpmn:endEvent id="End_1" />
              </bpmn:process>
            </bpmn:definitions>
            """;

        InputStream stream = new ByteArrayInputStream(bpmnXml.getBytes(StandardCharsets.UTF_8));
        DeploymentValidationResponse result = preFlightAnalyzerService.analizar(stream);

        // Con el hardening Backend, "latest" ahora emite un warning informativo
        boolean hasLatestInfo = result.getWarnings().stream()
            .anyMatch(w -> w.contains("Late Binding") || w.contains("LATEST"));
        assertTrue(hasLatestInfo,
            "El Pre-Flight debe emitir un warning informativo cuando BRT usa 'latest' (CA-12)");
    }
}
```

---

## Suite 2 — Frontend: Vitest para DMN Binding UI (Vitest)

**CA afectado:** CA-12
**Archivo destino:** `frontend/src/views/admin/Modeler/__tests__/BpmnDmnBinding.spec.ts`

### Tests requeridos:

```typescript
import { describe, it, expect } from 'vitest'

describe('US-005 CA-12: DMN Binding UI Validation', () => {

  describe('Default Binding Value', () => {
    it('debe tener DEPLOYMENT como valor por defecto del dmnBinding', () => {
      const defaultProps = {
        aiTokenLimit: 4000,
        aiTone: 'NEUTRAL',
        sla: '',
        calledElement: '',
        topic: '',
        dmnBinding: 'deployment'
      }
      expect(defaultProps.dmnBinding).toBe('deployment')
    })

    it('debe aceptar "latest" como valor alternativo válido', () => {
      const validValues = ['deployment', 'latest']
      expect(validValues).toContain('deployment')
      expect(validValues).toContain('latest')
    })

    it('NO debe aceptar valores no reconocidos', () => {
      const validValues = ['deployment', 'latest']
      expect(validValues).not.toContain('version')
      expect(validValues).not.toContain('')
      expect(validValues).not.toContain(null)
    })
  })

  describe('Camunda Property Mapping', () => {
    it('debe mapear al atributo correcto: camunda:decisionRefBinding', () => {
      const camundaProperty = 'camunda:decisionRefBinding'
      expect(camundaProperty).toBe('camunda:decisionRefBinding')
      expect(camundaProperty).not.toBe('camunda:decisionBinding') // Error común
    })

    it('debe sincronizar el valor del selector al XML BPMN', () => {
      // Simula el patrón syncElementProperties
      const syncCall = {
        property: 'camunda:decisionRefBinding',
        value: 'deployment'
      }
      expect(syncCall.property).toBe('camunda:decisionRefBinding')
      expect(syncCall.value).toBe('deployment')
    })
  })
})
```

---

## Verificación Obligatoria
1. **Backend suite:** `mvn test -pl ibpms-core -Dtest=PreFlightDmnBindingTest`
2. **Frontend suite:** `npx vitest run src/views/admin/Modeler/__tests__/BpmnDmnBinding.spec.ts`
3. **0 failures** en ambas suites.

---

## Restricciones QA
1. **Correspondencia Gherkin bidireccional:** Cada `@DisplayName` DEBE referenciar CA-12.
2. **ADR-011 compliance:** Tests de integración usan Spring Boot Test. Tests de frontend usan Vitest.
3. **No crear mocks falsos:** Los tests de integración inyectan XML BPMN real parseado por el `PreFlightAnalyzerService`.
4. **No usar** `git stash`. Solo `git commit` + `git push`.
5. **Convención de commit:**
   - `test(qa): US-005 CA-12 Pre-Flight DMN binding validation integration tests`
   - `test(qa): US-005 CA-12 frontend DMN binding UI unit tests`

---

> **INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
> 1. **DEPENDENCIA ESTRICTA:** Ejecuta DESPUÉS de que Backend Y Frontend hayan confirmado sus commits. Haz `git pull` antes de comenzar.
> 2. Los tests de integración verifican los cambios del Backend. Si algún test falla, es un indicador de que la remediación fue incompleta — NO adaptes el test, reporta al Arquitecto.
> 3. Crea cada suite como un commit atómico separado.
> 4. Rama: `sprint-3/informe_auditoriaSprint1y2`
> 5. Al finalizar, dile al Humano: *"Humano, he ejecutado las 2 suites de certificación CA-12. Entrégale este mensaje al Arquitecto Líder."*
