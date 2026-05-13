# 🧠→🕵️ Handoff: Arquitecto Líder → QA
# T-16 a T-20: Certificación Final Sprint 7.1 (Journey J-02)

**Emitido por:** 🧠 ARQUITECTO LÍDER
**Destinatario:** 🕵️ QA
**Fecha:** 2026-05-12T22:00:00-05:00
**Sprint:** 7 — Iteración 7.1
**Prioridad:** 🔴 Alta
**Dependencia:** T-19 (Frontend Tabs)

## 📖 LECTURAS OBLIGATORIAS (Ejecutar ANTES de codificar)

```bash
# 1. Política del Enjambre (LEY GLOBAL 0, 1, 2, 3)
cat .cursorrules

# 2. Skill principal del agente receptor
cat .agents/skills/qa_e2e_validation_audit/SKILL.md

# 3. Skills transversales aplicables
cat .agents/skills/clean_code_standards/SKILL.md
cat .agents/skills/zero_mock_enforcement/SKILL.md

# 4. Handoff actual (este archivo)
cat .agentic-sync/T-16_T-20_QA_Certification_Handoff.md
```

> ⚠️ **LEY GLOBAL 3 — Trazabilidad Inversa:** TODO el código nuevo de testing DEBE llevar
> la anotación @Traceability referenciando a la US que certifica.
> Esto es INNEGOCIABLE.

## 🔬 Diagnóstico del Arquitecto

Nos acercamos al cierre del Sprint 7.1. Se han purgado los mocks estáticos en gran parte del sistema, y la base de datos Liquibase ha sido certificada en T-21. Sin embargo, existen 4 tareas críticas de QA (T-16, T-17, T-18, T-20) que aún operan con infraestructura obsoleta o no han sido ejecutadas post-remediación P0.

| Violación/Hallazgo | Ubicación | Detalle |
|--------------------|:---------:|---------|
| T-18: Mocks DDL Estáticos | `BpmnIntegrationTest.java` | Las pruebas de integración BPMN (US-005) aún usan scripts DDL in-memory u objects mockeados en lugar del esquema real de Liquibase, invalidando la certeza Zero-Mock. |
| T-17: Falta Cobertura en Backend | `GenericFormIntegrationTest.java` | No se han ejecutado ni validado las pruebas de integración para la Generic Form Base (US-039). |
| T-16: FormDesigner E2E sin Persistencia Real | Tests Playwright de Modeler | Faltan pruebas que verifiquen la carga y guardado del FormDesigner E2E en ambiente estático (US-003). |
| T-20: Regresiones J-04 no validadas | Suite Playwright J-04 | Tras el hotfix P0 (`FormDefinitionFullAdapter`), no se han vuelto a correr los 18 tests para verificar `GREEN BUILD`. |

## 🎯 Instrucciones Quirúrgicas

### Paso 1: Certificación FormDesigner (T-16, US-003)

Ejecutar y documentar la suite de pruebas E2E en Playwright para el `FormDesigner` y asegurar que la persistencia y carga dinámica funcionen con el backend en entorno Zero-Mock. Asegúrate de que los selectores usen `data-testid` alineados.

### Paso 2: Certificación GenericFormIntegrationTest (T-17, US-039)

**Archivo:** `backend/ibpms-core/src/test/java/.../GenericFormIntegrationTest.java`

Ejecuta la suite de pruebas de integración de Spring Boot para US-039. Valida Metadatos, Autoguardado y Botones Pánico. Si existen errores por falta de dependencias Mocks (por culpa del `WebMvcTest` mal aislado), corrígelos usando `@MockBean` para infraestructura no testeada en la capa o transiciona a Testcontainers completo.

### Paso 3: Refactorización BPMN Integration Test a Liquibase (T-18, US-005)

**Archivo:** `backend/ibpms-core/src/test/java/.../BpmnIntegrationTest.java` (o equivalente)

Reemplaza la creación de esquemas manual por la carga de Testcontainers con Postgres y Liquibase.

```java
// @Traceability: US-005, CA-XX (Reemplazo DDL mock por Liquibase Testcontainer)
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class BpmnIntegrationTest {
    @Container
    public static PostgreSQLContainer<?> postgresDB = new PostgreSQLContainer<>("postgres:15.3")
      .withDatabaseName("ibpms_test")
      .withUsername("postgres")
      .withPassword("postgres");
      
    @DynamicPropertySource
    public static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresDB::getJdbcUrl);
        // ... (etc)
    }
}
```

### Paso 4: Re-ejecución Playwright J-04 (T-20)

**Acción:** Ejecuta el conjunto completo de 18 pruebas en la suite de Playwright (`npm run test:e2e` o `npx playwright test`) y asegura un resultado GREEN global, validando que el hotfix en `FormDefinitionFullAdapter` no rompió ningún flujo.

## Criterios de Aceptación (DoD)

| # | Criterio | Evidencia |
|---|----------|-----------|
| 1 | Tests Backend T-17 en Verde | `mvn test -Dtest=GenericFormIntegrationTest` reporta BUILD SUCCESS. |
| 2 | BPMN usa Liquibase en Tests | La clase `BpmnIntegrationTest` arranca mediante Testcontainers/Liquibase sin fallos de esquema. |
| 3 | T-20 Playwright J-04 Verde | El reporte HTML de Playwright muestra 18/18 pruebas superadas. |
| 4 | Cero Mocks Restantes | Búsqueda forense no revela `Mockito.when()` en la lógica de negocio central si no es puramente para infraestructura externa de I/O de terceros. |
| 5 | **Auditoría de Aserciones** | **LEY GLOBAL 4:** El agente QA entrega un Diff demostrando que NINGÚN bloque de aserción original de negocio (`assert`, `expect`) fue alterado o relajado durante la migración a Testcontainers. |

## 🚦 SECUENCIA DE EJECUCIÓN

1. Refactorizar `BpmnIntegrationTest.java` (T-18).
2. Ejecutar pruebas unitarias de backend `mvn test` (T-17, T-18).
3. Levantar backend y ejecutar Playwright para FormDesigner (T-16).
4. Ejecutar Playwright para la suite J-04 (T-20).
5. Actualizar `task.md` pasando las tareas T-16, T-17, T-18, y T-20 a `✅ CERTIFICADO`.
6. Commit: `git add . && git commit -m "test(e2e): certificacion integral QA sprint 7.1 y refactor a Testcontainers" && git push`

## 📋 Instrucciones para Copiar y Pegar

```text
Asume el rol de 🕵️ QA.

ANTES DE HACER CUALQUIER COSA, lee obligatoriamente estos archivos en este orden exacto:

1. cat .cursorrules
2. cat .agentic-sync/T-16_T-20_QA_Certification_Handoff.md

TU MISIÓN:

1. Refactorizar `BpmnIntegrationTest.java` (US-005) eliminando los DDL in-memory y migrando a Testcontainers + Liquibase para usar la base de datos real en memoria (T-18).
2. Ejecutar y certificar `GenericFormIntegrationTest` (T-17).
3. Ejecutar las pruebas E2E de Playwright del `FormDesigner` (T-16).
4. Ejecutar toda la suite de 18 tests E2E J-04 y garantizar un resultado GREEN (T-20).
5. Actualizar las tareas T-16, T-17, T-18 y T-20 en `task.md` a ✅ CERTIFICADO.
6. Commit: `git add . && git commit -m "test(qa): final certification sprint 7.1" && git push`

REGLAS INQUEBRANTABLES:
- **LEY GLOBAL 4:** Tienes ESTRICTAMENTE PROHIBIDO modificar los bloques lógicos de validación (`assert`, `expect`, `Given-When-Then`) de las pruebas de regresión certificadas en Sprints anteriores. Solo tienes permitido alterar la configuración de infraestructura (Ej. el `@BeforeEach` o el Setup de Testcontainers).
- Antes de completar la tarea, DEBES presentar un Diff de auditoría confirmando que las aserciones de negocio de T-18 y T-20 no fueron reducidas.
- Inyectar // @Traceability: US-XXX, CA-XX en toda prueba refactorizada o creada.
- El build completo (Frontend y Backend test suites) debe terminar en SUCCESS.
```
