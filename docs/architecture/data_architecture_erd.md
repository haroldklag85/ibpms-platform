# Arquitectura de Datos (V1) - Modelo Físico PostgreSQL 15+ (con pgvector)

> **[AUDITORÍA ARQUITECTURA - ADR 005]:** El modelo físico original estaba diseñado para MySQL 8. Con la introducción de requisitos MLOps y RAG (Sprint 10), se migró oficialmente la persistencia a PostgreSQL 15+ con `pgvector`. Este entorno soporta los expedientes (Data Relacional/JSONB) y la Base de Conocimiento de la IA concurrente en un solo motor (1 VM).

Este documento define el diseño de la base de datos relacional y vectorial (Entity-Relationship Diagram - ERD) para la **Versión 1 (V1)** de la Plataforma iBPMS. 

Al estar fundamentados en **Arquitectura Hexagonal y Domain-Driven Design (DDD)**, es críticamente importante entender la demarcación de responsabilidades entre nuestras "Tablas de Negocio" (Hexágono Core) y las "Tablas del Motor de Procesos" (Camunda 7).

## 1. Patrón Dual-Schema (Core vs. Motor)

Debido a que hemos "empotrado" el motor Camunda 7 dentro del backend Spring Boot como un adaptador para acelerar el Time-to-Market (TTM), la base de datos PostgreSQL consolidada contendrá dos mundos separados que **nunca interactúan directamente mediante Foreign Keys a nivel de SQL**:

1.  **Esquema del Motor (Camunda `ACT_*`):** Tablas nativas que el motor BPM usa para la tokenización de flujos y estado (`ACT_RU_EXECUTION`, `ACT_RU_TASK`, `ACT_HI_PROCINST`, etc.). **El equipo de desarrollo NUNCA debe hacer `SELECT` o `INSERT` directo sobre estas tablas.** Toda interacción es mediada por la API Java de Camunda.
2.  **Esquema de Negocio (iBPMS Core `ibpms_*`):** Las tablas construidas por nosotros para almacenar los Expedientes (Casos de uso agnósticos), los payloads JSON de los formularios (Apalancando la columna `JSONB` de PostgreSQL) y la referencia documental (SGDEA). Además de las entidades RAG para vectores de IA.

*Esta separación garantiza que en la V2, cuando reemplacemos Camunda 7 por Kubernetes/Zeebe, nuestra base de datos core `ibpms_*` quedará intacta.*

---

## 2. Diagrama Entidad-Relación (ERD) - Capa de Negocio Core

El siguiente diagrama Mermaid ilustra el modelo físico exclusivo para las entidades de negocio.

```mermaid
erDiagram
    %% Definición de Entidades

    ibpms_case {
        uuid id PK "UUID nativo PostgreSQL"
        varchar(100) definition_key "Ej: prestamo_hipotecario"
        varchar(100) business_key "ID visible por el usuario. Ej: RQ-2026-001"
        varchar(50) status "ACTIVE, COMPLETED, SUSPENDED, CANCELLED"
        json payload "Variables de negocio capturadas por el frontend. (PostgreSQL JSONB)"
        char(36) process_instance_id "ID del Motor Camunda (UUID)"
        varchar(100) created_by "Usuario/Sistema creador"
        timestamp created_at 
        timestamp updated_at
    }

    ibpms_task {
        uuid id PK "UUID nativo PostgreSQL"
        uuid case_id FK "Agrupador al Expediente"
        varchar(255) name "Nombre legible. Ej: Validar Identidad"
        varchar(100) definition_key "ID técnico en el BPMN. Ej: task_validar_id"
        varchar(100) assignee "UUID del usuario asignado"
        uuid parent_task_id FK "Si es Sub-Tarea Ad-hoc (Intake Kanban)"
        json candidate_groups "Array JSON de Roles ABAC/RBAC. Ej: ['Jefe_Ventas']"
        varchar(50) status "PENDING, CLAIMED, COMPLETED"
        int priority "1 al 100"
        timestamp due_date "SLA Expiration Date"
        varchar(100) camunda_task_id "Binding con la tarea del motor"
        timestamp created_at
        timestamp completed_at
    }

    ibpms_ui_template {
        uuid id PK "UUID nativo PostgreSQL"
        varchar(100) name "Ej: iform_maestro_compras"
        varchar(50) type "VUE_COMPONENT, ZOD_SCHEMA, JSON"
        text raw_code "El código Vue3 / Typings bruto"
        varchar(50) version "v1.0"
        timestamp created_at
    }

    ibpms_document {
        char(36) id PK "UUID SGDEA"
        char(36) case_id FK "Contexto de negocio"
        varchar(100) document_type "Categoría. Ej: Contrato_Firmado, Adjunto_Correo"
        varchar(255) file_name "Nombre físico"
        varchar(500) blob_uri "Ruta en Azure Managed Disks / Blob Storage"
        varchar(64) sha256_hash "Firma inmutable para validez legal"
        varchar(100) uploaded_by "Creador (Humano o OCR)"
        timestamp uploaded_at
    }

    sys_role {
        uuid id PK "UUID nativo PostgreSQL"
        varchar(100) name "Nombre del Rol. Ej: BPMN_Credito_Analista"
        varchar(50) type "GLOBAL o PROCESS_GENERATED (Por Hook)"
        varchar(100) process_definition_id "NULL para globales"
        varchar(100) lane_id "ID del carril BPMN"
    }

    ibpms_audit_log {
        uuid id PK "UUID nativo PostgreSQL"
        varchar(50) entity_type "CASE o TASK"
        char(36) entity_id "ID de la Entidad afectada"
        varchar(100) event_type "Ej: STATUS_CHANGED, VARIABLE_UPDATED"
        varchar(100) performed_by "Autor del cambio"
        json event_data "Diff del cambio real (Para trazabilidad Timeline)"
        timestamp created_at
    }

    %% Relaciones (Foreign Keys Lógicas o Físicas)
    ibpms_case ||--o{ ibpms_task : "contiene (1:N)"
    ibpms_task ||--o{ ibpms_task : "delega sub-tareas (1:N ad-hoc)"
    ibpms_case ||--o{ ibpms_document : "consolida (1:N)"
    ibpms_case ||--o{ ibpms_audit_log : "registra historial (1:N) vía Javers"
    ibpms_task ||--o{ ibpms_audit_log : "registra historial (1:N) vía Javers"
    ibpms_case ||--o| ibpms_ui_template : "usa vista (N:1)"
    ibpms_task }o--o{ sys_role : "asignado a grupos lógicos"
```

---

## 3. Diccionario Físico y Decisiones Técnicas Clave

### A. Uso del Tipo Nativo `JSONB` (PostgreSQL)
*   **Columna:** `ibpms_case.payload` e `ibpms_task.candidate_groups`
*   **Justificación:** Históricamente, las plataformas de procesos sufren del antipatrón *Entity-Attribute-Value (EAV)*, creando tablas gigantescas de clave-valor para guardar las variables del negocio. Al explotar la columna nativa `JSONB` de Postgres, logramos:
    1.  Ocultar la estructura dinámica (Formularios "Lego") directamente en una sola fila.
    2.  Permitir indexación secundaria: PostgreSQL permite generar Índices GIN sobre los campos del JSONB si necesitamos buscar, por ejemplo, todos los casos donde `payload->>'monto_aprobado' > 1000`.

### B. Llaves Primarias como UUID v4 (`UUID`)
*   **Justificación:** Se prohíbe el uso de `BIGINT AUTO_INCREMENT` para los IDs primarios. Se utiliza el **tipo nativo `UUID` de PostgreSQL** (16 bytes en disco vs 36 bytes de CHAR(36)), habilitando `gen_random_uuid()` como default y previniendo colisiones asincrónicas y ataques de enumeración (IDOR). Esta decisión también anticipa el ecosistema Cloud-Native (V2).

### C. La Conexión con Camunda (Acoplamiento Suave)
*   Las columnas `ibpms_case.process_instance_id` y `ibpms_task.camunda_task_id` son las **únicas anclas** que amarran nuestro Dominio Hexagonal con el Motor BPM empotrado.
*   Si el Motor Camunda ordena un salto de tarea, nuestra capa de *Application Service* en Spring Boot atrapa el evento mediante la API de Java y simplemente replica el estado a nuestra tabla independiente `ibpms_task`. 

### D. Inmutabilidad Documental y Legal (`ibpms_document`)
*   Se almacena obligatoriamente una columna `sha256_hash` al momento de inyectar un documento o captura (OCR).
*   La tabla en sí NO guarda binarios (Antipatrón). Se utiliza `blob_uri` apuntando físicamente a Azure Storage. La validación del hash en cada descarga certifica que el archivo en el volumen no fue alterado físicamente por un ransomware o un administrador curioso.

---

## 4. Diccionario de Datos Físico (Data Dictionary)

A continuación se detalla la estructura física de las tablas del esquema principal (`ibpms_*` y `sys_*`). Esta tabulación es la **fuente de verdad universal** para la construcción de los scripts DDL / Liquibase.

### 4.1. Esquema Maestro (Reference Data)

**Tabla:** `sys_catalog`
| Columna | Tipo de Dato | Llave | Nulable | Descripción |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `VARCHAR(50)` | PK | NO | Identificador único del catálogo (Ej. `TIPO_IDENTIFICACION`). |
| `description` | `VARCHAR(100)` | | NO | Nombre legible del catálogo. |

**Tabla:** `sys_catalog_item`
| Columna | Tipo de Dato | Llave | Nulable | Descripción |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `CHAR(36)` | PK | NO | UUID v4. |
| `catalog_id` | `VARCHAR(50)` | FK | NO | Referencia limitante a `sys_catalog.id`. |
| `code` | `VARCHAR(50)` | | NO | Código técnico alfanumérico del ítem (Ej. `CC`, `NIT`). |
| `label` | `VARCHAR(100)` | | NO | Etiqueta a mostrar en la UI de los formularios (Ej. `Cédula de Ciudadanía`). |
| `is_active` | `BOOLEAN` | | NO | Control de vigencia lógica. Default `true`. |

### 4.2. Esquema Core (Expedientes y Tareas)

**Tabla:** `ibpms_case`
| Columna | Tipo de Dato | Llave | Nulable | Descripción |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `UUID` | PK | NO | UUID nativo PostgreSQL del expediente (`gen_random_uuid()`). |
| `type` | `VARCHAR(50)` | | NO | Clasificador de orquestación (`BPMN`, `KANBAN`, `CASE_MGMT`). |
| `definition_key` | `VARCHAR(100)` | | NO | Identificador estático de la definición del proceso de negocio. |
| `business_key` | `VARCHAR(100)` | | NO | Radicado o Código de negocio visible corporativo (Ej. `RQ-2026-001`). Indexado. |
| `status` | `VARCHAR(50)` | | NO | Estado macro (`ACTIVE`, `COMPLETED`, `SUSPENDED`, `CANCELLED`). |
| `payload` | `JSON` | | SÍ | Instancia de un Object JSON masivo con la data capturada interactiva de UI. |
| `process_instance_id` | `CHAR(36)` | | SÍ | ID lógico de enganche asíncrono hacia el Motor Camunda 7. |
| `created_at` | `TIMESTAMP` | | NO | Fecha de radiqué o inicio inmutable. |
| `deleted_at` | `TIMESTAMP` | | SÍ | Marcador para funcionalidad de Borrado Lógico (Soft-Delete). |

**Tabla:** `ibpms_task`
| Columna | Tipo de Dato | Llave | Nulable | Descripción |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `UUID` | PK | NO | UUID nativo PostgreSQL de la Tarea Unificada. |
| `case_id` | `UUID` | FK | NO | Enlace irrompible al flujo padre (`ibpms_case.id`). Indexado. |
| `name` | `VARCHAR(255)` | | NO | Actividad legible pintada gráficamente en la Bandeja Unificada. |
| `source_type` | `VARCHAR(50)` | | NO | Conector de API subyacente de origen (`BPMN` o `KANBAN`). |
| `ref_id` | `VARCHAR(100)` | | NO | ID real en la capa oscura de Camunda o la tarjeta ágil. |
| `assignee` | `VARCHAR(100)` | | SÍ | UUID OIDC (Log-in de correo) o persona física vinculada. |
| `parent_task_id`| `CHAR(36)` | FK | SÍ | Soporte explícito para "Delegación Ad-Hoc" de sub-tareas Kanban, atadas a otra tarea Padre. |
| `candidate_groups`| `JSON` | | SÍ | Matriz literal de ABAC roles permitidos (Ej. `["Admin", "Revisor"]`). |
| `status` | `VARCHAR(50)` | | NO | Ciclo de acción (`PENDING`, `CLAIMED`, `COMPLETED`). |
| `due_date` | `TIMESTAMP` | | SÍ | Fecha lógicia de expiramiento o caducidad roja para escalamientos operacionales (SLA). Indexado. |
| `created_at` | `TIMESTAMP` | | NO | Timestmap milisegundo de disponibilidad generacional a Bandeja. |
| `deleted_at` | `TIMESTAMP` | | SÍ | Indicador de eliminación lógica de tarea caducada. |

**Tabla:** `ibpms_ui_template` (Nuevo - V1.5)
| Columna | Tipo de Dato | Llave | Nulable | Descripción |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `CHAR(36)` | PK | NO | UUID v4. |
| `name` | `VARCHAR(100)` | | NO | Identificador único del template Pro-Code. |
| `type` | `VARCHAR(50)` | | NO | Enum: `VUE`, `ZOD`, `JSON`. |
| `raw_code` | `TEXT` | | NO | Código crudo editable desde el IDE web/Monaco. |
| `version` | `VARCHAR(50)` | | NO | SemVer de la UI. |
| `created_at` | `TIMESTAMP` | | NO | Default current time. |

### 4.3. Esquema de Metadatos y Optimización

**Tabla:** `ibpms_metadata_index`
| Columna | Tipo de Dato | Llave | Nulable | Descripción |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `CHAR(36)` | PK | NO | UUID v4 autogenerado en Java Backend. |
| `case_id` | `CHAR(36)` | FK | NO | Ancla al expediente a optimizar. |
| `search_key` | `VARCHAR(100)` | | NO | Key desmenuzada del JSON crudo (Ej. `monto_aprobado_2`). Indexada altamente por B-Tree. |
| `search_value_string` | `VARCHAR(255)` | | SÍ | Relleno exclusivo al ser variable textual o Fechas normalizadas ISO8601. |
| `search_value_number` | `DECIMAL(19,4)`| | SÍ | Relleno exclusivo al usar numémericos para poder evaluar `>`, `<`, `BETWEEN`. |

### 4.4. Esquema Legal y Bóveda SGDEA

**Tabla:** `ibpms_document`
| Columna | Tipo de Dato | Llave | Nulable | Descripción |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `CHAR(36)` | PK | NO | UUID SGDEA corporativo interno. |
| `case_id` | `CHAR(36)` | FK | NO | Expediente envoltorio originario. |
| `document_type_code`| `VARCHAR(100)` | | NO | Relación laxa (`code`) del sys catalog. (`RECIBO_PAGO`, etc). |
| `file_name` | `VARCHAR(255)` | | NO | String estético con la extensión detectada `.pdf, .docx, .png`. |
| `blob_uri` | `VARCHAR(500)` | | NO | Puntero Universal Absoluto a capa IaaS Azure Managed/Blob. |
| `sha256_hash` | `VARCHAR(64)` | | NO | Clave Hash determinística evaluada previo guardado validando NO repudio. |
| `retention_end_date`| `TIMESTAMP` | | SÍ | Cálculo futuro basado en el Archivo General de la Nación (TRD). |

### 4.5. Esquema Histórico y Trazabilidad

**Tabla:** `ibpms_audit_log`
| Columna | Tipo de Dato | Llave | Nulable | Descripción |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `CHAR(36)` | PK | NO | UUID v4. |
| `entity_type` | `VARCHAR(50)` | | NO | Enfoque impactado (`CASE`, `TASK`, `DOC`). |
| `entity_id` | `CHAR(36)` | | NO | Vínculo físico sobre dicha entidad superior. |
| `event_type` | `VARCHAR(100)` | | NO | Tipo textual preprogramado de la alteración (`STATUS_CHANGED`, etc) |
| `performed_by`| `VARCHAR(100)` | | NO | Sujeto ejecutor final, puede ser `Auto-Timer` o `User UUID`. |
| `event_data` | `JSON` | | SÍ | **Potenciado por Javers:** Guarda las instantáneas exactas del Payload ("Before/After") para certificar inmutabilidad ISO. |
| `created_at` | `TIMESTAMP` | | NO | **Regla DDL de Interfaz Física: POSTGRESQL TABLE PARTITION BY RANGE.** Fecha. |
