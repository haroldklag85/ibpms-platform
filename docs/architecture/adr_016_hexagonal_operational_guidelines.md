<!-- @Traceability: US-003 - ADR-001 -->
# ADR-016: Directrices Operativas de Adopción de Arquitectura Hexagonal y DDD

**Status:** Aceptado
**Date:** 2026-06-01
**Context:** Alineación de desarrollo en la plataforma Core iBPMS (ibpms-platform)
**Autor:** Antigravity (Lead Software Architect AI) / Tech Lead Team

## 1. Contexto

La adopción de la Arquitectura Hexagonal y DDD (establecida en el **ADR-001**) es crucial para evitar el acoplamiento tecnológico y garantizar la mantenibilidad del motor Core iBPMS. Sin embargo, la auditoría inicial de la historia **US-003 (iForm Builder)** reveló múltiples desviaciones críticas en varias historias de usuario:
*   **Contaminación JPA:** Modelos de dominio (`AllowedDomain`, `OrphanPayload`, `TriageTask`, `WebhookTransaction` y el subpaquete `agile/*`) importaban directamente anotaciones de persistencia (`jakarta.persistence.*`, `@Entity`, `@Table`) e Hibernate, acoplando el motor de base de datos relacional directamente con las reglas de negocio.
*   **Fuga de Dependencias de Spring Data en Puertos:** El puerto de dominio `TriageTaskRepository` utilizaba clases específicas del framework Spring Data (`Page` y `Pageable`), comprometiendo el aislamiento de la capa de dominio.
*   **Inconsistencia en Namespaces:** Coexistencia de paquetes plurales (`adapters`) e interfaces duplicadas en subpaquetes de puertos (`ports`), fragmentando la organización física del código.
*   **API Redundantes y Controladores Fuera de Capa:** Existencia de múltiples controladores exponiendo endpoints similares (`TaskDraftController` vs `TaskDraftApiController`) y ubicados fuera del namespace estándar `infrastructure/web`.

Para mitigar estas desviaciones de forma sostenible tanto por desarrolladores humanos como por agentes de IA autónomos en futuras historias de usuario, se hace necesario un conjunto de **directrices operativas estructuradas y comprobables de forma automatizada**.

---

## 2. Decisiones Operativas Obligatorias

Se establecen las siguientes directrices y reglas inquebrantables de desarrollo y validación:

### A. Pureza de Modelos de Dominio (Pure POJOs)
*   **Regla:** Las clases dentro del paquete `com.ibpms.poc.domain.model` (y todos sus subpaquetes) deben ser **POJOs de Java puros**.
*   **Prohibición de Importaciones:** Queda terminantemente prohibido importar paquetes como `jakarta.persistence.*`, `org.hibernate.*`, o cualquier anotación relacionada con bases de datos u ORMs.
*   **Segregación de Persistencia:** Si un modelo de dominio requiere persistencia relacional, se debe crear una entidad espejo con el sufijo `JpaEntity` (ej: `TaskDraftJpaEntity.java`) dentro de `com.ibpms.poc.infrastructure.jpa.entity`.
*   **Mapeadores Limpios:** Se debe utilizar **MapStruct** dentro del paquete de infraestructura para transformar objetos de dominio en entidades JPA y viceversa. El orden de los compiladores en `pom.xml` debe declarar siempre Lombok antes de MapStruct para evitar mappers con métodos vacíos.

### B. Firma Limpia de Puertos de Dominio (Desacoplamiento de Frameworks)
*   **Regla:** Las interfaces ubicadas en `com.ibpms.poc.domain.port` deben definir firmas agnósticas de cualquier framework o tecnología externa.
*   **Prohibición de Paginación Acoplada:** Queda prohibido el uso de `org.springframework.data.domain.Page` o `Pageable` en la firma de los métodos de los puertos.
*   **Estandarización de Paginación de Dominio:** Se debe utilizar paginación basada en tipos primitivos (`int page`, `int size`) o usar una abstracción pura de dominio como `DomainPage<T>`. Los adaptadores de infraestructura correspondientes se encargarán de traducir estos parámetros a los formatos específicos del framework de persistencia.

### C. Nomenclatura Estricta y Singular
*   **Regla:** Para evitar fragmentación e inconsistencias en la organización del código, todas las capas deben usar nombres en **singular** en sus paquetes raíz de la aplicación:
    *   **Puertos de Dominio/Aplicación:** `com.ibpms.poc.application.port.in` y `com.ibpms.poc.application.port.out`.
    *   **Adaptadores de Infraestructura:** `com.ibpms.poc.infrastructure.adapter`.
*   **Prohibición:** Se prohíbe el uso de carpetas en plural como `adapters` o `ports` en cualquier parte del código fuente del backend.

### D. Ubicación y Exposición de Controladores REST
*   **Regla:** Todos los controladores REST expuestos por la aplicación deben residir dentro del paquete `com.ibpms.poc.infrastructure.web` (capa de adaptadores de entrada en la periferia hexagonal).
*   **Prohibición:** Se prohíbe la creación de paquetes de nivel superior como `com.ibpms.poc.api.controller` o `com.ibpms.poc.controller` fuera de la jerarquía de infraestructura.

### E. Trazabilidad Técnica Obligatoria
*   **Regla:** Cada archivo modificado o creado bajo esta gobernanza arquitectónica **debe** comenzar exactamente en su línea 1 con la etiqueta de trazabilidad:
    `// @Traceability: US-XXX - ADR-001` (o su equivalente en XML/HTML comment para archivos de recursos).

---

## 3. Control Automatizado en el Build Pipeline (ArchUnit)

Para evitar que estas directrices queden relegadas a documentación pasiva y se verifiquen de forma interactiva tanto por desarrolladores como por agentes de desarrollo, se implementa una **Validación Estricta de Arquitectura via ArchUnit**.

*   El test de arquitectura se ubicará en la suite de pruebas unitarias (`HexagonalArchitectureArchUnitTest.java`).
*   **Fallo de Construcción:** Cualquier violación del aislamiento del dominio (ej. importar `@Entity` en dominio) o del namespace de controladores y adaptadores provocará que el comando `mvn test` falle inmediatamente.
*   **Replicabilidad (US-XXX):** Este test verificará dinámicamente todo el árbol del paquete `com.ibpms.poc`, haciendo que cualquier nueva historia de usuario que se implemente quede protegida automáticamente por los mismos controles de aislamiento sin necesidad de escribir nuevas pruebas de arquitectura para cada US-XXX.

---

## 4. Consecuencias y Cierre

*   **Positivas:**
    *   **Cero Regresiones:** El pipeline de CI/CD actúa como un escudo automático contra la contaminación del dominio por JPA o Spring Data.
    *   **Directriz Clara para Agentes de IA:** Los subagentes autónomos y asistentes de codificación entienden los límites exactos de los paquetes, reduciendo el riesgo de alucinación arquitectónica.
    *   **Portabilidad Garantizada:** El núcleo de negocio de `ibpms-platform` queda 100% blindado contra dependencias del motor de base de datos o el motor BPM.
*   **Negativas/Riesgos Aceptados:**
    *   Ligero incremento de tiempo inicial en el modelado por la creación explícita de `JpaEntity` y mappers.
    
---

## 5. Revisión y Aprobación

*   **Aprobado por:** Lead Software Architect AI
*   **Aprobado por:** Tech Lead Team
*   **Aprobado por:** QA Automation Team
