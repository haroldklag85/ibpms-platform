# 🔧 HANDOFF: Deuda Técnica ARQ-005 — Tests de Integración
> **Fecha:** 2026-05-01 | **Arquitecto Líder:** Auditoría Forense Completada
> **Rama:** `sprint-6` | **Commit Base:** `4b11eeed`
> **Prioridad:** 🟡 MEDIA — No bloquea certificación, pero viola ADRs activos
> **Agente Destino:** 🔴 BACKEND (exclusivo) — Frontend, QA e Infra/BD sin acciones

---

## 📋 Resumen Ejecutivo

Se detectaron **4 violaciones** en la suite de tests de integración durante la auditoría ARQ-005. Todas son del ámbito **Backend exclusivamente**. No requieren cambios en Frontend, Infra ni BD.

### ADRs Violados
| ADR | Sección | Violación |
|:---:|---------|-----------|
| **ADR-010 §2** | REST Assured obligatorio para contratos HTTP | 3 tests usan `MockMvc` en vez de `RestAssured` |
| **Zero-Mock** | Política de gobierno | 1 test usa `@MockBean` para simular cliente LLM externo |

---

## 🔴 HANDOFF BACKEND

### DT-TEST-001: Migrar 3 tests MockMvc → REST Assured

**Violación:** ADR-010 §2 establece que los tests de integración HTTP deben usar REST Assured (black-box, puerto real RANDOM_PORT) para validar el flujo HTTP completo incluyendo filtros de seguridad.

**Archivos afectados:**

#### 1. `IdentityManagementIntegrationTest.java`
- **Ubicación:** `src/test/java/com/ibpms/poc/infrastructure/web/security/`
- **Líneas:** 94 líneas totales
- **Problema:** Usa `@AutoConfigureMockMvc` + `MockMvc` + `@SpringBootTest` (sin WebEnvironment explícito, hereda RANDOM_PORT del padre pero no lo usa)
- **Tests afectados:** 3 (`testZeroToleranceEntropy`, `testKillSwitchIsolation`, `testSoftDeleteGuardReturns405`)
- **Acción:**
  1. Eliminar `@AutoConfigureMockMvc` y campo `MockMvc mockMvc`
  2. Agregar `@LocalServerPort private int port;`
  3. En `@BeforeEach`, inicializar `RestAssured.port = port;`
  4. Reescribir las 3 aserciones usando `given().contentType(JSON).body(payload).when().post("/api/v1/admin/users").then().statusCode(400)`
  5. Mantener `@WithMockUser` o migrar a JWT real si el endpoint lo exige

#### 2. `RoleAuditIntegrationTest.java`
- **Ubicación:** `src/test/java/com/ibpms/poc/infrastructure/web/security/`
- **Líneas:** 77 líneas totales
- **Problema:** Mismo patrón — `@AutoConfigureMockMvc` + `MockMvc`
- **Tests afectados:** 2 (`testIso27001RoleMatrixExport_BlobDecoding`, `testJsonDeltaAudit_SurgicalGrantPrecision`)
- **Acción:**
  1. Misma migración que el archivo anterior
  2. **NOTA ESPECIAL:** El test `testJsonDeltaAudit_SurgicalGrantPrecision` NO usa HTTP — es un test de repositorio puro (guarda en BD y lee). Este test podría quedarse sin MockMvc porque no hace llamadas HTTP. Evaluar si debe moverse a un test `@DataJpaTest` separado o mantenerlo como está sin MockMvc.

#### 3. `GenerativeSreIntegrationTest.java`
- **Ubicación:** `src/test/java/com/ibpms/poc/infrastructure/web/`
- **Líneas:** 140 líneas totales
- **Problema:** Usa `MockMvc` + `@AutoConfigureMockMvc` + `@MockBean` (doble violación)
- **Tests afectados:** 2 (`testRateLimiting_GenerativeEndpoint_Throws429AfterThreshold`, `testZeroCostCache_IdenticalPromptsServeFromMemory`)
- **Acción:** Ver DT-TEST-001 + DT-TEST-002 combinados abajo

---

### DT-TEST-002: Eliminar `@MockBean LlmExternalClient` en GenerativeSreIntegrationTest

**Violación:** Política Zero-Mock — `@MockBean` inyecta un bean falso en el contexto de Spring, violando el principio de que los tests de integración deben usar infraestructura real o stubs controlados.

**Archivo:** `GenerativeSreIntegrationTest.java` L78-79
```java
// ❌ VIOLACIÓN Zero-Mock
@MockBean
private LlmExternalClient llmClient;
```

**Acción prescriptiva (2 opciones, elegir UNA):**

**Opción A — WireMock Stub (RECOMENDADA):**
1. Agregar dependencia `wiremock-standalone` al `pom.xml` (scope test)
2. Crear un `@TestConfiguration` que registre un `WireMockServer` en el puerto dinámico
3. Configurar stub: `stubFor(post("/api/llm/generate").willReturn(okJson("{\"result\": \"...\"}")));`
4. Apuntar el `LlmExternalClient` a la URL del WireMock via `@DynamicPropertySource`
5. Eliminar `@MockBean` y `when(...).thenReturn(...)` — el WireMock simula el endpoint HTTP real

**Opción B — TestConfiguration con Bean real stub:**
1. Crear clase `LlmExternalClientStub implements LlmExternalClient` en `src/test/java/.../config/`
2. Anotarla con `@Profile("sre-test")` y `@Primary`
3. Implementar `generateResponse()` retornando JSON estático
4. Eliminar `@MockBean` del test
5. El Spring Context inyecta el stub sin romper la cadena de beans

**Criterio de verificación:** El test debe pasar con `mvn test -pl ibpms-core -Dtest=GenerativeSreIntegrationTest` SIN ningún `@MockBean` en el archivo.

---

### DT-TEST-003: Estandarizar herencia de AbstractIntegrationTest

**Problema:** Existen 2 patrones incompatibles coexistiendo:
- **Patrón A (correcto):** Tests REST Assured heredan `AbstractIntegrationTest` → `RANDOM_PORT` → `@LocalServerPort`
- **Patrón B (a corregir):** Tests MockMvc heredan `AbstractIntegrationTest` pero agregan `@SpringBootTest` propio (override implícito sin WebEnvironment) + `@AutoConfigureMockMvc`

**Acción:**
1. **NO crear `AbstractMockMvcIntegrationTest`** — la migración de DT-TEST-001 eliminará la necesidad. Todos los tests HTTP migrarán a REST Assured.
2. Si queda algún test que legítimamente necesite MockMvc (test de controlador aislado, no integración), debe heredar de `AbstractIntegrationTest` y agregar `@AutoConfigureMockMvc` **sin override** de `@SpringBootTest`.
3. **Eliminar** las anotaciones `@SpringBootTest` duplicadas en los 3 archivos afectados (el padre ya la declara).

**Archivos donde eliminar `@SpringBootTest` duplicado:**
```
IdentityManagementIntegrationTest.java    L24: @SpringBootTest  ← ELIMINAR
RoleAuditIntegrationTest.java             L25: @SpringBootTest  ← ELIMINAR
GenerativeSreIntegrationTest.java         L32: @SpringBootTest  ← ELIMINAR
```

---

### DT-TEST-004: Purgar `@LocalServerPort` redundantes

**Problema:** 4 subclases re-declaran `@LocalServerPort private int port;` haciendo shadow del campo heredado. No es funcional sino cosmético — genera confusión y puede causar bugs sutiles si el padre cambia.

**Nota:** La búsqueda forense reveló que `AbstractIntegrationTest` **NO declara** `@LocalServerPort` (decisión documentada en L16-18 del padre). Por lo tanto, las subclases que lo declaran lo hacen correctamente. **Esta deuda queda CANCELADA** — no es un shadow, es la declaración legítima.

**Veredicto DT-TEST-004:** ✅ FALSO POSITIVO — No requiere acción.

---

## 📊 Matriz de Impacto

| ID | Archivos | Esfuerzo | Riesgo | Prioridad |
|:--:|:--------:|:--------:|:------:|:---------:|
| DT-TEST-001 | 3 | 🟡 2h | Bajo | P2 |
| DT-TEST-002 | 1 | 🟡 1.5h | Medio (nuevo dep WireMock) | P1 |
| DT-TEST-003 | 3 | 🟢 15min | Muy bajo | P3 |
| DT-TEST-004 | — | — | — | ~~CANCELADO~~ |

**Esfuerzo total estimado:** ~3.5 horas

---

## ✅ Criterios de Certificación (Checkpoints QA)

El Arquitecto Líder validará la remediación cuando:

1. **QA-DT-01:** `mvn test -pl ibpms-core` ejecuta sin `@MockBean` en `GenerativeSreIntegrationTest.java`
2. **QA-DT-02:** `grep -r "@AutoConfigureMockMvc" src/test/` retorna CERO resultados en los 3 archivos migrados
3. **QA-DT-03:** `grep -rn "@SpringBootTest" src/test/ | grep -v AbstractIntegrationTest` NO muestra los 3 archivos remediados con `@SpringBootTest` duplicado
4. **QA-DT-04:** Todos los tests migrados pasan con `RestAssured` haciendo HTTP real contra `RANDOM_PORT`
5. **QA-DT-05:** `mvn clean compile -pl ibpms-core` → BUILD SUCCESS (regresión cero)

---

## 🟢 HANDOFF FRONTEND
**Sin acciones.** La deuda técnica es 100% backend (tests Java).

## 🟢 HANDOFF QA / E2E
**Sin acciones de ejecución.** Los checkpoints QA-DT-01 a QA-DT-05 los ejecutará el Arquitecto Líder directamente.

## 🟢 HANDOFF INFRA / BD
**Sin acciones.** No hay cambios de schema, contenedores ni configuración de infraestructura.

---

## 🔄 Secuencia de Ejecución Recomendada

```
1. DT-TEST-003 (eliminar @SpringBootTest duplicados)     ← 15 min, desbloquea los demás
2. DT-TEST-002 (WireMock stub para LlmExternalClient)    ← 1.5h, más complejo
3. DT-TEST-001 (migrar MockMvc → RestAssured)            ← 2h, 3 archivos
4. mvn clean test -pl ibpms-core                          ← Validación final
```

**Modo de ejecución:** SECUENCIAL — cada paso depende del anterior.
