# Sprint 6.V2 — Estabilización Táctica y Certificación J-04 (Hard Reset)

> **Objetivo Central:** Sanear la compilación del Backend (Deuda Técnica), restaurar la credibilidad de la Matriz de Cobertura, y garantizar un pipeline de pruebas (Unitarias → Integración Local → E2E Playwright) 100% libre de mocks, enfocado **única y exclusivamente en el Journey J-04 (El Operario MVP)**.
> **Rama:** `sprint-6/uat-certification-v2`
> **Estrategia RAM/Docker:** Uso de `docker-compose.yml` (Dev) para integración local para evitar colapso de RAM. Cambio a `docker-compose.e2e.yml` solo para certificación UAT final.
> **Gobernanza:** Zero-Mock, Arquitectura Hexagonal estricta, DDD, Patrón Estrangulador (V1).

---

## 1. Congelamiento Táctico (Journeys Excluidos)
Para garantizar foco absoluto y evitar la dispersión de esfuerzos que corrompió la matriz de cobertura anterior, los siguientes Journeys quedan **CONGELADOS** y transferidos al Sprint 7+:
- 🧊 **J-02** (Diseñador BPMN)
- 🧊 **J-03** (Intake O365)
- 🧊 **J-05** (Gobernanza Administrativa)
- 🧊 **J-06** (Hub Ágil)
- 🧊 **J-07** (Arquitecto IA DMN/Copilot)
- 🧊 **J-08** (Resiliencia Avanzada)
- 🧊 **J-SEC** (Pentest Transversal)

---

## 2. Scope Funcional Activo (Journey J-04)
El Sprint 6 se centrará únicamente en los componentes y APIs que impactan la vida del Operario.
- **US Involucradas:** US-001 (Workdesk), US-002 (Claim Task), US-008 (Kanban), US-029 (Ejecución de Formulario), US-036 (RBAC Core), US-039 (Formulario Genérico).
- **Excepción:** US-017 (CQRS Event Sourcing) queda diferida a V2 según dictamen de auditoría previa.

---

## 3. Hoja de Ruta (Iteraciones)

### Iteración 6.0: Saneamiento Documental y Compilación Base
- **Paso 1:** Reconstruir `.agentic-sync/coverage_matrix.md` ejecutando una re-auditoría automatizada y manual contra las Épicas Gherkin del SSOT.
- **Paso 2:** Solucionar el Bloqueante P0 (Evasión de compilación): Restaurar y corregir el código alojado en `test_broken_files` (`RoleHierarchyServiceTest`, `RoleServiceIntegrationTest` de la US-036).
- **Gate 6.0:** `mvn clean verify` debe pasar en verde al 100% sin exclusiones silenciosas de carpetas.

### Iteración 6.1: Infraestructura de Integración Local (Zero-OOM)
- **Paso 1:** Re-configurar `AbstractIntegrationTest` para perfiles locales (`@Profile("local-dev")`). En lugar de levantar Testcontainers efímeros (que provocan colapso en máquinas con <3GB RAM libres), los tests apuntarán dinámicamente al contenedor persistente `ibpms-postgres` levantado en el `docker-compose.yml` del ambiente Dev.
- **Paso 2:** Asegurar que el **ADR-010** (Prohibición absoluta de H2 en memoria) se mantenga intacto.

### Iteración 6.2: Fixtures y Data Seed (Preparación E2E)
- **Paso 1:** Crear scripts vía Liquibase (`test-data.sql`) o un `SeedService` para poblar el estado predecible exigido por `casos_uso_uat_j04.md`.
- **Paso 2:** Inyectar perfiles de autenticación (`perito_a`, `director_1`), tareas BPMN en estado `TODO`, grupos RBAC y definiciones de formularios en la base de datos real.

### Iteración 6.3: Certificación UAT J-04 (Playwright)
- **Paso 1:** Activar `docker-compose.e2e.yml` para aislar el ambiente de UAT.
- **Paso 2:** Automatizar y ejecutar los 45 escenarios del documento `docs/uat/casos_uso_uat_j04.md`.
- **Paso 3:** Si se descubren Mocks residuales en componentes UI (ej. Tablero Kanban), la prueba se pausará, se remediará el endpoint faltante en el Backend real, y se continuará (Tolerancia Cero a Mocks).

---

## 4. Criterios de Éxito Global (Definition of Done)
1. ✅ La matriz de cobertura refleja al 100% la verdad documental de Gherkin para las US del J-04.
2. ✅ Cero clases de test en "carpetas rotas" excluidas de la compilación de Maven.
3. ✅ Tests de Integración del backend corriendo establemente contra Postgres/RabbitMQ locales, sin H2.
4. ✅ Playwright E2E ejecutando validaciones con Data Seed real, sin timeouts por estado vacío.
