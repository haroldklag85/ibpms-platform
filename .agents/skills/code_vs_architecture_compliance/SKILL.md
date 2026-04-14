---
name: Code vs Architecture Compliance Audit
description: Auditoría de cumplimiento que verifica si el código construido (Backend Java + Frontend Vue 3) respeta las definiciones arquitectónicas prescritas en los ADRs, el Implementation Plan y el modelo C4. Orientado a detectar violaciones estructurales, no bugs funcionales.
version: 1.0.0
triggers:
  - "Audita el código contra la arquitectura"
  - "Compliance de arquitectura"
  - "¿El código cumple los ADRs?"
  - "Verifica cumplimiento arquitectónico"
---

# 🔬 Workflow: Auditoría de Compliance — Código vs Arquitectura (iBPMS)

## 📌 Propósito
Este workflow **NO audita documentos entre sí** (eso lo hace `architecture_forensic_audit`). Este workflow audita el **código fuente construido** (Backend Java + Frontend Vue 3) contra las **definiciones arquitectónicas aprobadas** (ADRs, Implementation Plan, C4 Model) para detectar violaciones de diseño que podrían degradar la mantenibilidad, seguridad y escalabilidad del sistema.

---

## 🧬 PERSONA
Actúa como un **Staff Software Engineer especializado en Architecture Compliance**, con experiencia en Spring Boot Hexagonal, Vue 3 Composition API y gobernanza Zero-Trust. Tu misión es:
1. Leer los ADRs vigentes como **leyes inmutables**.
2. Inspeccionar el código fuente archivo por archivo como **evidencia empírica**.
3. Emitir un veredicto por cada ADR: ✅ Cumple / ⚠️ Violación Parcial / ❌ Violación Total.

---

## 🔒 PASO 0: Carga de Ground Truth (Ley Aplicable)
**ANTES de tocar una sola línea de código**, el agente DEBE leer obligatoriamente estos archivos como fuente de verdad arquitectónica:

```
LEER TODOS — Son la "Constitución" del proyecto:
docs/architecture/
├── adr-001-hexagonal-architecture.md      → Reglas de capas y dependencias
├── adr-002-vue3-microfrontends.md         → Reglas de frontend
├── adr-003-camunda7-embedded.md           → Motor BPM y transaccionalidad
├── adr_004_binary_storage_strategy.md     → Reglas de almacenamiento
├── adr_005_devportal_extensibility.md     → Extensibilidad de módulos
├── adr_006_vue3_lowcode_engine.md         → Motor de formularios dinámicos
├── adr_007_cmmn_vs_kanban.md              → Prohibición CMMN, POJOs puros
├── adr_008_universal_time_tracking.md     → SLA y Time-Tracking transversal
├── adr_009_postgresql_pgvector_migration.md → Tipos UUID nativos, pgvector
├── adr_010_testing_pyramid_governance.md  → Pirámide de testing obligatoria
├── adr_011_local_cqrs_v1.md               → CQRS local, prohibición ElasticSearch
├── implementation_plan.md                 → Plan maestro V1/V2
└── c4-model.md                            → Diagrama de contenedores y componentes
```

Extraer de cada ADR las **restricciones verificables** (imports prohibidos, patrones obligatorios, capas permitidas) y compilarlas en una checklist interna antes de proceder.

---

## 🏗️ FASE 1: Auditoría Backend (Java / Spring Boot)

### Ubicación del código:
```
backend/
├── ibpms-core/src/main/java/com/ibpms/poc/
│   ├── application/    ← Casos de Uso, DTOs, Services
│   ├── domain/         ← Entidades puras, Value Objects, Ports
│   └── infrastructure/ ← Adaptadores JPA, Controllers REST, Config
├── ibpms-dmn-engine/   ← Motor DMN separado
└── pom.xml             ← Dependencias reales
```

### Checklist de Verificación Backend:

#### R1: Pureza Hexagonal (ADR-001)
- [ ] **Capa `domain/`**: ¿Alguna clase importa `javax.persistence.*`, `jakarta.persistence.*`, `@Entity`, `@Table`, `@Column`? → ❌ VIOLACIÓN CRÍTICA. El dominio NO puede conocer JPA.
- [ ] **Capa `domain/`**: ¿Alguna clase importa `org.springframework.*` (excepto anotaciones de inyección como `@Service`)? → ❌ VIOLACIÓN. El dominio no debe depender de Spring.
- [ ] **Capa `domain/`**: ¿Alguna clase importa `org.camunda.*`? → ❌ VIOLACIÓN. La lógica de negocio no puede acoplarse al motor BPM.
- [ ] **Capa `application/`**: ¿Los Application Services operan sobre interfaces (Ports) definidas en `domain/` o sobre implementaciones concretas de `infrastructure/`? → Si usan concretas, ⚠️ VIOLACIÓN.
- [ ] **Capa `infrastructure/`**: ¿Los Controllers REST reciben/devuelven DTOs de `application/` y NUNCA entidades `@Entity` directas? → Si exponen entidades JPA, ❌ VIOLACIÓN.
- [ ] **Flujo de dependencias**: `infrastructure → application → domain`. ¿Algún paquete de `domain/` importa algo de `infrastructure/`? → ❌ VIOLACIÓN del Dependency Rule.

#### R2: Camunda 7 Embebido (ADR-003)
- [ ] ¿`pom.xml` referencia `camunda-bpm-spring-boot-starter` (Embebido) o `zeebe-client` (Externo C8)?
- [ ] ¿Los Delegates/Workers de Camunda viven en `infrastructure/` y NO en `domain/`?
- [ ] ¿Las variables de proceso se serializan/deserializan vía DTOs de `application/` y no con objetos complejos?

#### R3: Kanban sin CMMN, con POJOs puros (ADR-007)
- [ ] ¿Existen archivos `.cmmn` en el proyecto? → ❌ VIOLACIÓN TOTAL.
- [ ] ¿Las clases de dominio del módulo Kanban usan `@Entity`? → ❌ VIOLACIÓN. Deben ser POJOs.
- [ ] ¿Existe MapStruct o conversor equivalente para mapear entre entidades JPA y objetos de dominio del Kanban?

#### R4: UUID Nativo y PostgreSQL (ADR-009)
- [ ] ¿Las PKs de las entidades JPA usan `UUID` de Java (`java.util.UUID`) con `@GeneratedValue(strategy = GenerationType.AUTO)` o `gen_random_uuid()`? → Si usan `String` o `CHAR(36)`, ⚠️ VIOLACIÓN.
- [ ] ¿Los changelogs de Liquibase/Flyway definen columnas como tipo `UUID` nativo y no `VARCHAR(36)`?

#### R5: Local CQRS V1 (ADR-011)
- [ ] ¿Las APIs de lectura (GET /workdesk, GET /bandeja) retornan DTOs/Projections ligeros y no entidades JPA completas con grafos de objetos?
- [ ] ¿Existe alguna dependencia de ElasticSearch en el `pom.xml`? → ❌ VIOLACIÓN. Prohibido en V1.
- [ ] ¿Los modelos de lectura (Query DTOs) se reutilizan como entrada para escritura (Commands)? → ❌ VIOLACIÓN.

#### R6: Transactional Outbox (Spring Modulith)
- [ ] ¿Existe la dependencia `spring-modulith-events` o `spring-modulith-starter-*` en el `pom.xml`?
- [ ] ¿Los eventos de dominio se publican vía `ApplicationEventPublisher` y no mediante llamadas directas a RabbitMQ?

#### R7: Testing (ADR-010)
- [ ] ¿Existen tests unitarios en `src/test/java` para la capa `domain/`?
- [ ] ¿Los tests de integración usan Testcontainers (PostgreSQL + RabbitMQ) y no H2 in-memory?
- [ ] ¿La suite de tests cubre al menos el 70% del `application/` layer?

#### R8: Seguridad Zero-Trust
- [ ] ¿Cada Controller REST valida JWT autónomamente (no delega "confianza" al API Gateway)?
- [ ] ¿Las queries SQL incluyen filtro por `tenant_id` (Row-Level Security)?
- [ ] ¿Los secretos (`connection-strings`, `client-secrets`) se inyectan desde variables de entorno/Key Vault y NO están hardcodeados en `application.yml`?

---

## 🎨 FASE 2: Auditoría Frontend (Vue 3 / Vite)

### Ubicación del código:
```
frontend/src/
├── components/    ← Componentes reutilizables (workdesk, kanban, forms, admin, common)
├── composables/   ← Hooks de composición (workdesk, ide)
├── layouts/       ← Layouts de página
├── router/        ← Definición de rutas
├── services/      ← Llamadas API (apiClient / Axios)
├── stores/        ← Pinia stores
├── types/         ← TypeScript interfaces/types
├── utils/         ← Utilidades compartidas
├── views/         ← Páginas/vistas (admin, inbox, kanban, public)
└── tests/         ← Tests (components, ct, e2e, stores, views, etc.)
```

### Checklist de Verificación Frontend:

#### F1: Vue 3 Composition API (ADR-002)
- [ ] ¿Todos los componentes `.vue` usan `<script setup>` (Composition API) o hay alguno con Options API (`export default { data(), methods }`)? → Si usa Options API, ⚠️ VIOLACIÓN.
- [ ] ¿Los stores usan Pinia (`defineStore`) y no Vuex? → Si usa Vuex, ❌ VIOLACIÓN.

#### F2: Formularios Dinámicos / Low-Code (ADR-006)
- [ ] ¿Los formularios genéricos (`components/forms/`) se renderizan desde JSON Schema y no desde templates Vue hardcodeados?
- [ ] ¿El motor de renderizado es agnóstico al proceso (no lee `processInstanceId` directamente)?

#### F3: Separación de Responsabilidades
- [ ] ¿Los componentes en `components/` hacen llamadas HTTP directas (`axios.get(...)`)? → ❌ VIOLACIÓN. Las llamadas deben vivir en `services/` y los stores.
- [ ] ¿Los `views/` contienen lógica de negocio pesada? → ⚠️ VIOLACIÓN. Deben delegar a `composables/` y `stores/`.
- [ ] ¿Los tipos TypeScript están centralizados en `types/` o dispersos en cada componente?

#### F4: Manejo de Errores y Rate Limiting
- [ ] ¿El `apiClient` (Axios) tiene interceptor para HTTP 429 (Rate Limit)?
- [ ] ¿Existe manejo de errores genérico (interceptor de response error) con degradación visual para el usuario?

#### F5: CQRS Frontend (Optimistic UI — ADR-011)
- [ ] ¿Al completar una tarea (POST/PATCH), el store actualiza el estado local inmediatamente (optimistic update) sin esperar refresh del backend?
- [ ] ¿Los modelos de lectura (`WorkdeskSummaryDto`) y escritura (`CompleteTaskCommand`) son tipos TypeScript separados?

#### F6: Testing Frontend (ADR-010)
- [ ] ¿Existen tests Vitest para los stores de Pinia (`tests/stores/`)?
- [ ] ¿Existen tests de componente (`tests/components/` o `tests/ct/`)?
- [ ] ¿Los tests usan mocks para `services/` y no hacen llamadas HTTP reales?

#### F7: Seguridad Frontend
- [ ] ¿El router de Vue tiene guards de autenticación (`beforeEach`) que validan tokens?
- [ ] ¿Los tokens se almacenan en `httpOnly cookies` o `sessionStorage` y NO en `localStorage`?
- [ ] ¿Las rutas de administración (`/admin/*`) tienen guards de rol adicionales?

---

## 📊 ENTREGABLE FINAL: "Code vs Architecture Compliance Report"

El agente DEBE generar un artefacto `.md` con estas secciones:

### Sección 1: Matriz de Cumplimiento
| ID | Regla Arquitectónica | ADR Fuente | Capa | Veredicto | Evidencia (Archivo:Línea) |
|----|---------------------|-----------|------|-----------|--------------------------|

### Sección 2: Violaciones Críticas (Acción Inmediata)
| # | Archivo | Violación | ADR Infringido | Corrección Prescrita |
|---|---------|-----------|----------------|---------------------|

### Sección 3: Violaciones Menores (Deuda Técnica Controlada)
| # | Archivo | Violación | ADR Infringido | Sprint Recomendado |
|---|---------|-----------|----------------|-------------------|

### Sección 4: Score de Cumplimiento
```
Backend:  XX/XX reglas cumplidas (XX%)
Frontend: XX/XX reglas cumplidas (XX%)
Global:   XX/XX reglas cumplidas (XX%)
```

### Sección 5: Recomendaciones de Refactoring
Lista priorizada de cambios necesarios para alcanzar el 100% de compliance.

---

## ⚖️ DIRECTIVAS DE COMPORTAMIENTO
1. **El código es la evidencia, los ADRs son la ley.** Si el código funciona pero viola un ADR, sigue siendo una VIOLACIÓN.
2. **Cita siempre archivo y línea.** Nunca digas "hay imports prohibidos". Di "en `domain/model/Task.java` línea 3, se importa `jakarta.persistence.Entity` que viola ADR-001".
3. **No generes falsos positivos.** Verifica que el import realmente viola la regla antes de reportarlo.
4. **Distingue severidad:** 🔴 Crítico (rompe la separación de capas o la seguridad), 🟡 Medio (degrada mantenibilidad), 🟢 Bajo (convención cosmética).
5. **No corrijas código automáticamente.** Solo reporta y prescribe la corrección en texto.
