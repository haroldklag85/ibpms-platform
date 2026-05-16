# 🧠→🧪 Handoff: Arquitecto Líder → Agente QA
# T-12/T-15: Certificación Zero-Mock para Deuda Funcional Backend

**Emitido por:** 🧠 ARQUITECTO LÍDER
**Destinatario:** 🧪 AGENTE QA - INTEGRATION & E2E
**Fecha:** 2026-05-12T10:25:00-05:00
**Sprint:** 7 — Iteración 7.1
**Prioridad:** 🔴 Alta
**Dependencia:** Tareas T-12 y T-15 de Backend deben estar finalizadas (Inyección de Trazabilidad).

## 📖 LECTURAS OBLIGATORIAS (Ejecutar ANTES de codificar)

```bash
# 1. Política del Enjambre (LEY GLOBAL 0, 1, 2, 3)
cat .cursorrules

# 2. Skill principal del agente receptor (QA Zero-Mock Certification)
cat .agents/skills/qa_e2e_validation_audit/SKILL.md

# 3. Skills transversales aplicables
cat .agents/skills/clean_code_standards/SKILL.md
cat .agents/skills/zero_mock_enforcement/SKILL.md

# 4. Requerimientos Funcionales
cat docs/requirements/epics/epic_A_motor_core.md
```

> ⚠️ **LEY GLOBAL 3 — Trazabilidad Inversa:** TODO el código nuevo DEBE llevar
> la anotación @Traceability o comentario `// @Traceability: US-XXX, CA-XX`.
> Esto es INNEGOCIABLE.

## 🔬 Diagnóstico del Arquitecto

El sistema carece de Tests de Integración reales para el servicio de despojo de tareas (Auto-Unclaim) y el listener de invalidación de caché RabbitMQ, violando el ADR-010 (Zero-Mock Policy). Se corre el riesgo de un despojo cruzado entre Tenants en el entorno de producción debido a falta de cobertura.

| Violación/Hallazgo | Ubicación | Detalle |
|--------------------|:---------:|---------|
| Faltan Tests de Integración CA-06 | `backend/ibpms-core/src/test/java/com/ibpms/poc/application/services/AutoClaimServiceIntegrationTest.java` | Archivo de test no existe o carece de validación de Tenant |
| Faltan Tests de Integración CA-16 | `backend/ibpms-core/src/test/java/com/ibpms/poc/infrastructure/messaging/FormSchemaChangedRabbitListenerTest.java` | Archivo de test no existe para certificar consumo de RabbitMQ |

## 🎯 Instrucciones Quirúrgicas

### Paso 1: Crear Test de Integración de Auto-Unclaim (CA-06)

**Archivo:** `backend/ibpms-core/src/test/java/com/ibpms/poc/application/services/AutoClaimServiceIntegrationTest.java`

Crea un test de integración basado en Spring Boot que valide la expiración del claim respetando el tenant.

```java
// @Traceability: US-002, CA-06
@SpringBootTest
@ActiveProfiles("test")
public class AutoClaimServiceIntegrationTest {
    // Implementar prueba de integración con H2 o TestContainers.
    // Crear tareas para tenant_A y tenant_B simulando un tiempo pasado.
    // Ejecutar lógica de AutoClaim y verificar que solo expiren las correspondientes al tenant actual sin tocar las otras.
}
```

### Paso 2: Crear Test de Integración RabbitMQ (CA-16)

**Archivo:** `backend/ibpms-core/src/test/java/com/ibpms/poc/infrastructure/messaging/FormSchemaChangedRabbitListenerTest.java`

Crea el test de integración para el Listener.

```java
// @Traceability: US-007, CA-16
@SpringBootTest
@ActiveProfiles("test")
public class FormSchemaChangedRabbitListenerTest {
    // Validar recepción del evento JSON y el llamado a AiDmnCacheService.evictAll()
    // Si no hay redis configurado, verificar estricto mediante Mockito Spy u otra estrategia.
}
```

## Criterios de Aceptación (DoD)

| # | Criterio | Evidencia |
|---|----------|-----------|
| 1 | Existencia de `AutoClaimServiceIntegrationTest.java` con tag de CA-06 | `ls backend/ibpms-core/src/test/java/com/ibpms/poc/application/services/AutoClaimServiceIntegrationTest.java` |
| 2 | Existencia de `FormSchemaChangedRabbitListenerTest.java` con tag de CA-16 | `ls backend/ibpms-core/src/test/java/com/ibpms/poc/infrastructure/messaging/FormSchemaChangedRabbitListenerTest.java` |
| 3 | Suite de pruebas exitosa y Commit | Ejecución de `mvn test` termina en SUCCESS |

## 🚦 SECUENCIA DE EJECUCIÓN

1. Crear `AutoClaimServiceIntegrationTest.java`
2. Crear `FormSchemaChangedRabbitListenerTest.java`
3. Ejecutar suite de pruebas: `cd backend/ibpms-core && mvn test`
4. Commit: `git add . && git commit -m "test(backend): integración zero-mock para T-12 y T-15" && git push`

## 📋 Instrucciones para Copiar y Pegar

```text
Asume el rol de 🧪 AGENTE QA - INTEGRATION & E2E.

ANTES DE HACER CUALQUIER COSA, lee obligatoriamente estos archivos en este orden exacto:

1. cat .cursorrules
2. cat .agents/skills/qa_e2e_validation_audit/SKILL.md
3. cat .agents/skills/zero_mock_enforcement/SKILL.md
4. cat docs/requirements/epics/epic_A_motor_core.md
5. cat .agentic-sync/handoff_qa_T10_T15_DeudaFuncional.md

TU MISIÓN:

1. Crea el test de integración `AutoClaimServiceIntegrationTest.java` que valide el despojo de tareas (US-002 CA-06) asegurando el aislamiento multi-tenant.
2. Crea el test de integración `FormSchemaChangedRabbitListenerTest.java` que certifique el consumo de eventos RabbitMQ (US-007 CA-16).
3. Build/Compile: `cd backend/ibpms-core && mvn test`
4. Commit: `git add . && git commit -m "test(backend): integración zero-mock para T-12 y T-15" && git push`

REGLAS INQUEBRANTABLES:
- DEBES respetar la política Zero-Mock para la persistencia transaccional (usar H2 o TestContainers).
- PROHIBIDO el uso de `@MockBean` para capa de datos transaccional en pruebas de integración, permitiéndose solo en adaptadores externos (como caché) si es estrictamente necesario y justificado.
- OBLIGATORIO inyectar los tags de trazabilidad en todos los tests nuevos (Ley Global 3).
```
