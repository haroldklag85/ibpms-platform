# 🔧 HANDOFF BACKEND — Remediación CI/CD PR #4 (DevDavid → main)

> **Iteración**: `CICD-FIX-PR4`
> **US Afectadas**: US-005, US-034, US-036 (deuda técnica transversal)
> **Rama de trabajo**: `DevDavid`
> **Rol asignado**: Agente Backend
> **Tipo**: Remediación de deuda técnica — NO es implementación de funcionalidad nueva
> **SSOT**: `docs/requirements/v1_user_stories_index.md` → Épicas A, B, E

---

## Pre-Handoff Checklist — CICD-FIX-PR4

| # | Verificación | Estado | Evidencia |
|---|---|---|---|
| 1 | Iteración autorizada por Arquitecto Líder | ✅ | Remediación de pipeline CI/CD bloqueante |
| 2 | Rama de trabajo confirmada | ✅ | `DevDavid` |
| 3 | No requiere cambios de esquema BD | ✅ | No hay DDL involucrado |
| 4 | No requiere nuevos endpoints | ✅ | No hay API nueva |

**Resultado**: ✅ APROBADO para ejecución

---

## 1. Metadatos y SSOT

- **Iteración**: CICD-FIX-PR4
- **Rama Git**: `DevDavid`
- **PR**: [#4 - Pull request US-034 - US-036 Y US-051](https://github.com/haroldklag85/ibpms-platform/pull/4)
- **Pipeline fallido**: GitHub Actions Run #277 — Job `Backend Build & Maven Verify`
- **Comando que falla**: `mvn clean verify -DskipTests=false` en `./backend/ibpms-core`
- **Secuencia Maven**: clean → compile → test (Surefire: `**/*Test.java`) → package → integration-test (Failsafe: `**/*IT.java`) → verify

---

## 2. Alineación Arquitectónica y ADRs

| ADR | Impacto en esta tarea |
|-----|----------------------|
| **ADR-001** (Hexagonal) | Verificar que los tests respeten la separación de capas. Tests unitarios (`*Test.java`) NO deben arrancar Spring Boot. |
| **ADR-010** (Pirámide de Testing) | Surefire (`*Test.java`) = unitarios. Failsafe (`*IT.java`) = integración. Respetar esta segmentación. |
| **ADR-009** (PostgreSQL + pgvector) | Integration tests apuntan a PostgreSQL en Docker (puerto 5433). |

**Stack confirmado**: Java 17 / Spring Boot 3.2.3 / Maven / JUnit 5 / Surefire 3.2.5 / Failsafe 3.2.5

---

## 3. Rutas Exactas y Contexto Preexistente

### Estructura del proyecto Maven
- **Parent POM**: `backend/pom.xml` (módulos: `ibpms-dmn-engine`, `ibpms-core`)
- **Module POM**: `backend/ibpms-core/pom.xml` (artifact: `ibpms-poc`)
- **Source**: `backend/ibpms-core/src/main/java/com/ibpms/poc/`
- **Tests**: `backend/ibpms-core/src/test/java/com/ibpms/poc/`

### Configuración de plugins de test (del POM real):

**Surefire (Unit Tests)**:
- `failIfNoTests: true`
- `includes: **/*Test.java`
- `excludes: **/*IT.java, **/*IntegrationTest.java`

**Failsafe (Integration Tests)**:
- `failIfNoTests: true`
- `includes: **/*IT.java`
- Executions: `integration-test`, `verify`

### Hallazgos de investigación forense ya verificados:

**A. 9 archivos de test VACÍOS (stubs sin `@Test`)**:
Estos archivos existen pero son clases vacías `public class ClassName {}`:
1. `com/ibpms/poc/infrastructure/adapter/BpmnAuditJpaAdapterTest.java`
2. `com/ibpms/poc/infrastructure/adapter/DataMappingJpaAdapterTest.java`
3. `com/ibpms/poc/application/service/AutoClaimServiceIntegrationTest.java`
4. `com/ibpms/poc/application/service/PromptPiiScrubberTest.java`
5. `com/ibpms/poc/infrastructure/persistence/TaskConcurrentClaimIntegrationTest.java`
6. `com/ibpms/poc/infrastructure/web/IdempotencyServiceTest.java`
7. `com/ibpms/poc/infrastructure/web/ClamAvScanTest.java`
8. `com/ibpms/poc/infrastructure/web/IdempotencyWebhookTest.java`
9. `com/ibpms/poc/application/usecase/dmn/DmnGovernanceUseCaseTest.java`

> ⚠️ **NOTA**: Los stubs vacíos NO deberían causar fallo de Surefire por sí solos (solo si NO hay otros tests con `@Test`). Pero son ruido que debe limpiarse.

**B. 8 archivos de test legacy DESHABILITADOS** (`.disabled`/`.bak`):
Estos archivos importan `com.ibpms.poc.AbstractIntegrationTest` que **NO EXISTE**. Si Maven los procesa como `.java`, causarían error de compilación:
1. `DelegationServiceIntegrationTest.java.disabled`
2. `RoleServiceIntegrationTest.java.disabled`
3. `IdentityManagementIntegrationTest.java.disabled`
4. `AuthSyncControllerTest.java.disabled`
5. `TaskClaimControllerTest.java.disabled`
6. `WorkdeskRepositoryTest.java.disabled`
7. `SandboxGovernanceTest.java.bak`
8. `AgileTaskRepositoryJpaTest.java.bak`

> ⚠️ **NOTA**: Maven NO debería compilar archivos `.disabled` o `.bak` porque no tienen extensión `.java`. Pero si hay una configuración que los incluye, causarán fallo de compilación.

---

## 4. Instrucciones de Diagnóstico y Corrección (OBLIGATORIAS)

> 🚫 **REGLA FUNDAMENTAL — CERO SUPOSICIONES**:
> TIENES PROHIBIDO asumir qué error causa el fallo. DEBES ejecutar el comando localmente, leer la salida real, y corregir basándote EXCLUSIVAMENTE en la evidencia.

### PASO 1: Reproducir el error localmente (OBLIGATORIO)

Ejecuta el siguiente comando **exacto** que ejecuta el pipeline CI:

```powershell
cd backend/ibpms-core
mvn clean verify -DskipTests=false
```

**Si falla en COMPILACIÓN** (fase `compile` o `test-compile`):
1. Lee CADA error de compilación del output de Maven
2. Para cada error, identifica el archivo y la línea exacta
3. Verifica si es un import roto, un método faltante, o una clase inexistente
4. Corrige SIN crear código nuevo que no exista en el proyecto — busca la versión correcta del método/clase

**Si falla en TESTS UNITARIOS** (fase `test`):
1. Lee la lista de tests fallidos de la sección `[ERROR] Tests run:`
2. Para cada test fallido, lee el test Y el source que testea
3. Identifica si el test está desactualizado o si el source tiene un bug
4. Corrige el test para que valide el comportamiento REAL del source, o corrige el source si hay un bug genuino

**Si falla en INTEGRATION TESTS** (fase `integration-test`/`verify`):
1. Verifica que Docker esté corriendo y los contenedores estén Up:
   ```powershell
   docker ps --format "table {{.Names}}\t{{.Status}}"
   ```
2. Los integration tests necesitan: PostgreSQL (5433), Redis (6379), RabbitMQ (5672)
3. Si los tests fallan por schemas/tablas inexistentes, verifica los changelogs de Liquibase

### PASO 2: Limpiar test stubs vacíos

Para cada uno de los 9 test stubs vacíos listados en la sección 3.A:
1. **Verifica** que realmente estén vacíos (sin métodos `@Test`)
2. Si están vacíos, **ELIMÍNALOS** — no aportan valor y son ruido
3. Si tienen tests pero están comentados, **evalúa** si los tests son relevantes y descomenta o elimina

### PASO 3: Verificar archivos legacy deshabilitados

Para los 8 archivos `.disabled`/`.bak`:
1. Verifica que Maven NO los esté procesando (no deberían compilar)
2. Si hay evidencia de que SÍ se procesan, muévelos fuera del directorio `src/test/java/` o elimínalos

### PASO 4: Verificación final (EXIT GATE)

```powershell
cd backend/ibpms-core
mvn clean verify -DskipTests=false
```

**El EXIT CODE DEBE SER 0. Si no es 0, NO hagas push.**

---

## 5. Matriz de Verificación

| Validación | Comando | Resultado Esperado |
|-----------|---------|-------------------|
| Compilación | `mvn clean compile` | BUILD SUCCESS |
| Tests unitarios | `mvn test` | BUILD SUCCESS, 0 failures |
| Tests integración | `mvn verify` | BUILD SUCCESS, 0 failures |
| Pipeline completo | `mvn clean verify -DskipTests=false` | BUILD SUCCESS, EXIT CODE 0 |

---

## 6. Políticas y Reglas Obligatorias

### 📋 DIRECTIVA DE INFRAESTRUCTURA HÍBRIDA
El backend Spring Boot corre en consola local (NO en Docker) en el puerto `8080`. Antes de iniciar tu trabajo, ejecuta el protocolo de pre-validación:
1. `curl -s http://localhost:8080/actuator/health` → Debe responder `{"status":"UP"}`.
2. Si no responde, arráncalo con: `cd backend && mvn spring-boot:run -pl ibpms-core -Dspring-boot.run.profiles=default`.
3. Verifica los servicios Docker: `docker ps` → PostgreSQL (`5433`), Redis (`6379`) y RabbitMQ (`5672`) deben estar `Up (healthy)`.
**PROHIBIDO** levantar el backend vía Docker o modificar el `docker-compose.yml`.

### 🛑 POLÍTICA ZERO-MOCK (`.agents/skills/zero_mock_enforcement/SKILL.md`)
- **PROHIBIDO** el uso de `mockAdapter.ts` o interceptores de red simulados
- **PROHIBIDO** usar Testcontainers (limitaciones de hardware local) — los tests de integración deben apuntar a los contenedores Docker estáticos ya corriendo
- Los tests unitarios (`*Test.java`) NO deben arrancar Spring Boot (`@SpringBootTest` PROHIBIDO en unitarios)
- Los tests de integración (`*IT.java`) SÍ arrancan Spring y se ejecutan con `mvn verify`

### 📏 CLEAN CODE (`.agents/skills/clean_code_standards/SKILL.md`)
- **Naming semántico**: Variables descriptivas, NO siglas oscuras
- **Retornos nulos PROHIBIDOS**: Usa `Optional<T>` o excepciones de dominio
- **Inmutabilidad**: Usa `record` para DTOs, `final` en inyecciones por constructor
- **Excepciones**: NO captures `Exception` genérica — usa excepciones concretas
- **Logging**: `@Slf4j`, PROHIBIDO `System.out.println`
- **Segmentación de tests**: `*Test.java` = unitarios (sin Spring). `*IT.java` = integración (con Spring)

### 🚫 REGLAS FUNDAMENTALES E IRROMPIBLES
> **CERO TOLERANCIA AL HARD-CODE**: Toda corrección debe basarse en el comportamiento REAL del sistema. PROHIBIDO inventar valores, funciones o estructuras.
>
> **CERO TOLERANCIA A LA SUPOSICIÓN**: Si algo no queda claro, DEBES leer el archivo fuente y verificar. PROHIBIDO asumir que un método existe.
>
> **CERO TOLERANCIA A LA IMAGINACIÓN**: Las correcciones deben alinearse con la arquitectura existente. PROHIBIDO crear soluciones fuera del patrón establecido.
>
> **CERO TOLERANCIA A PENSAR FUERA DEL PROYECTO REAL**: Toda referencia a clases, métodos, constantes o configuraciones DEBE verificarse contra el código real que existe en el repositorio. Si no existe, NO lo inventes.

---

> ⚠️ **IMPORTANTE:** Todo desarrollo o configuración debe quedar exhaustivamente documentado. Se exige **PRECISIÓN QUIRÚRGICA** en cada cambio para evitar efectos colaterales en funcionalidades existentes. Se han detectado regresiones en iteraciones previas; cualquier daño a funcionalidades adyacentes será motivo de rechazo inmediato.

---

## INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN

1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_BACKEND_CICD.md`.
4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_BACKEND_CICD.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION` y programa la solución.
6. **ANTES del commit final**, actualiza `docs/sprints/gobernanza_pm/CHANGELOG_NO_TECNICO.md` explicando tu trabajo en lenguaje sencillo y amigable para un CEO (QUÉ hiciste y PARA QUÉ sirve).
7. Finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama `DevDavid`. Queda estrictamente prohibido usar git stash.

> 📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
> - Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md` (naming, error handling, modularidad).
> - **Compilación obligatoria**: Ejecuta el protocolo Zero-Trust SRE documentado en `.agents/skills/backend_sre_compilation_audit/SKILL.md` (§0 a §2). Si hay bloqueos de infraestructura, aplica el protocolo de reporte 3B.

> 🧠 **POLÍTICA ANTIAMNESIA (OBLIGATORIA)**:
> Antes de tocar cualquier archivo, DEBES re-entrenar tu contexto leyendo:
> 1. `docs/architecture/arquitecturar.md` — Arquitectura Core
> 2. `docs/requirements/v1_user_stories_index.md` — Índice de US
> 3. `docs/sprints/gobernanza_pm/GUIA_ARQUITECTO_LIDER.md` — Gobernanza PM-IA
> La precisión de tu trabajo depende de que NO asumas cómo funciona el proyecto, sino que lo leas.
