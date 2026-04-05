# REPORTE DE CONSOLIDACIÓN Y PLAN DE REMEDIACIÓN PRIORIZADO (V2)
**Estrategia:** RAG-First Deep Context Analysis
**Scope Evaluado:** 17 Iteraciones (Auditoría Lineal y Transversal sobre `v1_user_stories.md`)

---

## 1. Resumen Ejecutivo de la Auditoría
Se ha concluido la revisión técnico-funcional del Single Source of Truth (SSOT) del producto iBPMS. Tras superar un análisis iterativo profundo, se validó que aunque las historias individuales exhiben un alto estándar técnico de redacción (Gherkin/BDD), **su integración sistémica arroja cicatrices y grietas transversales peligrosas**. 

La auditoría ha revelado que resolver los requerimientos de forma monolítica, tal cual están dictaminados, provocará colisiones en producción. Por ende, este reporte abstrae los hallazgos hacia **5 Ejes de Fricción Transversal (Macro-GAPs).**

---

## 2. Ejes de Fricción Transversal (Macro-GAPs Sistémicos)

### 🚨 EJE 1: Choque Tecnológico Isomórfico (La Falla Backend Java vs Frontend Zod)
*   **Orígenes del Gap:** Las métricas tempranas de arquitectura fijaron el uso de Spring Boot (Java) como Backend. Sin embargo, la **Iteración 17 (US-017 / CA-4621)** exige que el backend aplique y re-evalue "Isomórficamente" el mismo esquema estructural validado con `Zod` (Librería escrita en TypeScript Node.js exclusiva del ecosistema JavaScript). 
*   **Impacto (Severidad Crítica):** Un backend Java no puede interpretar funciones TypeScript de Zod, lo que impedirá la validación bidireccional de Formularios, abriendo vulnerabilidades tipo *Data Spoofing* o ralentizando el servidor si se intenta emular un intérprete pesado V8 (GraalVM Polyglot) en pleno Main Thread.
*   **Resolución Exigida:** Adoptar una herramienta en el pipeline (CI/CD) como `zod-to-json-schema` para transpilar el código TypeScript a estándar "RFC JSON Schema" neutro, y evaluarlo en tiempo récord dentro de Spring Boot utilizando la librería genérica `networknt/json-schema-validator`.

### 🚨 EJE 2: Bifurcación del Paradigma de Tareas (El Dilema CMMN vs Ágil Local)
*   **Orígenes del Gap:** Las **Iteraciones 1 al 4** confieren al BPM Engine (Zeebe/Camunda 7) el poder total sobre la bandeja de tareas empresariales (Worklist). Sin embargo, la **Iteración 16 (US-045)** decreta que el marco "Kanban Ágil rechaza a Camunda" y almacenará los formularios directamente en una tabla suelta `ibpms_kanban_tasks` con formato `JSONB`.
*   **Impacto (Severidad Mayor):** Destroza la "Vista 360" y la unificación operativa del operador final exigida en la **US-041**. El iBPMS queda con "dos cerebros paralelos e incomunicados": uno en la Tasklist Engine y otro en MySQL Ágil.
*   **Resolución Exigida:** Definir una "Projection Layer" (Event Sourcing a nivel de tabla plana) en el CQRS que absorba y fusione los UUIDs tanto de las tareas Zeebe como de las tablas híbridas Ágiles, para que el Frontend consuma UNA única fuente de lectura de bandejas de trabajo sin hacer complejas uniones lógicas.

### 🚨 EJE 3: Cuello de Botella Sistémico (Suicidio de RAM por Buffers)
*   **Orígenes del Gap:** La **Iteración 11 (US-035 Bóveda SGDEA)** determina el almacenamiento de PDFs ultra-pesados y densos forenses. En la milla final, la **Iteración 16 (US-049 Motor Outbox)** dictamina ingenuamente que, para notificar a un cliente, el Worker descargará los anexos *"temporalmente a la memoria RAM"* de la aplicación.
*   **Impacto (Severidad Catastrófica OOM):** Una cola asíncrona de 25 correos atascados en RabbitMQ que lleven anexos de 40MB exigirá alojar instantáneamente 1 Gigabyte en el Heap (Memoria) del servidor de correos. Al llegar al límite RAM del servidor de nube, un "Out Of Memory Exception" matará el Node o Pod del Worker y reseteará todo.
*   **Resolución Exigida:** Patrón de Arquitectura: *Direct Piping HTTP* a MS Graph sin lectura hacia el *Buffer* en el código de aplicación, o en su defecto, almacenamiento temporal transaccional en discos `/tmp` sin inyectarse en el Memory Heap.

### 🟡 EJE 4: Agujeros Críticos de Restricciones Lógicas (Security Auth Lifecycle)
*   **Orígenes del Gap:** La **Iteración 14 (US-038 EntraID)** impone una sincronización multi-rol y expulsiones síncronas de operadores "en vivo", pero choca contra la arquitectura definida de Tokens JWT Híbridos/Stateless. Adicionalmente, se registran dualismos tóxicos como permitir a Operarios auto-aprobarse tareas para la "V1" (Segregación de Funciones SoD debilitado).
*   **Impacto (Riesgo Regulatorio Moderado):** Usuarios expulsados o liquidados en Active Directory de Azure (EntraID) continúan con control intrusivo y destructor operativo durante el tiempo que dure la caducidad (TTL) vigente de su JWT en cache del PC.
*   **Resolución Exigida:** Integrar control de Cache Des-acoplado. El BFF (API Gateway) interrogará un *Redis Blacklist Storage* con cada solicitud transaccional antes de confiar en la firma JWT expirada. 

### 🟡 EJE 5: Obsolescencia y Anacronismo (Camunda 7 vs Camunda 8)
*   **Orígenes del Gap:** La **Iteración 15** intenta inyectar pausas al sistema accediendo e inyectando filas nativas de SQL hacia la tabla local `ACT_RU_JOB`.  
*   **Impacto (Riesgo Funcional):** Antigravity ya diagnosticó previamente que la arquitectura actual busca escalar al iBPMS en **Camunda 8 (Zeebe)**. Zeebe es Cloud-native, Stateless RocksDB y oculta su Base de Datos Relacional porque *NO LA TIENE*. Tratar de ejecutar SQL contra Zeebe ocasionará un choque estructural que frustrará la etapa de compilación.
*   **Resolución Exigida:** Reescritura del algoritmo SLA. El SLA debe gobernarse estrictamente con "Timer Boundary Events" dibujados visualmente en el CMMN o con Workers que midan fechas localmente (Zeebe Tasklist API).

---

## 3. Plan de Remediación Trazable Integrado (El Roadmap)

### Fase A: Estabilización Primaria (Prevención de Explosión Cloud)
- **Ticket REM-001:** Cambiar instrucción CA-4384 (US-049 Outbound). Eliminar uso de Buffers en Memoria JVM/Node, aplicar file-streaming estricto o descargas TempOS para correos masivos.
- **Ticket REM-002:** Eliminar la directriz CA-4317 que envía logs históricos a la Nube vía HTTP en el servidor de transacciones. Derivarlo a Azure Data Factory o Job Python local puro.
- **Ticket REM-003:** Instalar o configurar en el Pipeline `zod-to-json-schema` para mitigar el Gap Isomórfico Backend de Java. De esta red dependen ambos mundos.

### Fase B: Unión de Mundos (Respuesta CQRS)
- **Ticket REM-004:** Ampliación a US-017. Dictaminar la proyección relacional final (`ibpms_global_worklist_view`) que unirá los JSONB de "Kanban Puro" con las ID referenciales de Zeebe/Tasklist. Si no se resuelve ahora, toda la Analítica BAM del sistema (US-018) será inactiva.

### Fase C: Actualización SSOT (Gobernanza C8 + Seguridad)
- **Ticket REM-005:** Editar Gherkins de US-043 (SLA Global). Sustituir directrices y comandos SQL (`ACT_RU_JOB`) hacia directrices de invocación REST Zeebe/Tasklist (`update_task/timeout`), asegurando compatibilidad técnica pura C8.
- **Ticket REM-006:** Levantar en US-038 un contenedor Redis paralelo (`ibpms-auth-blacklist`) adosado al Gateway BFF para rechazar JWTs con Roles robados o liquidados antes de la muerte del Token (Mitigación Blacklist).

> *Fin del Diagnóstico y Entendimiento Consolidado de la Auditoría Integral RAG-First V1.*
