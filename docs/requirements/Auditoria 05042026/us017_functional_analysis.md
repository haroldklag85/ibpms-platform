# Análisis de Entendimiento: US-017 — Ejecución y Persistencia Inmutable de Formularios (CQRS & Event Sourcing)

**Ejecutado por:** Analista de Producto Senior / Arquitecto Funcional  
**Fecha:** 2026-04-05  
**Historia:** US-017 — Ejecución y Persistencia Inmutable de Formularios (CQRS & Event Sourcing)  
**Pantalla asociada:** Pantalla 2 (compartida con US-029)  
**Estado de desarrollo:** NO DESARROLLADA  
**CAs vigentes:** 16 (CA-01 a CA-16)  
**SSOT:** `docs/requirements/v1_user_stories.md` (líneas 5599–5743)  
**Referencia cruzada:** `us029_functional_analysis.md` (US-029 con 34 CAs — historia gemela de Frontend)

---

## 1. Resumen del Entendimiento

La US-017 es la **"historia gemela de Backend"** de la US-029. Mientras la US-029 gobierna lo que el operario VE y HACE en la Pantalla 2 (la experiencia), la US-017 gobierna lo que el servidor PROCESA y ALMACENA cuando el operario presiona [Enviar] (la persistencia).

### En lenguaje simple:

> **US-029** = "Cómo el operario llena el formulario y presiona Enviar"  
> **US-017** = "Qué pasa por dentro del servidor cuando el operario presiona Enviar"

La US-017 define tres conceptos arquitectónicos clave:
1. **CQRS (Command Query Responsibility Segregation):** Separar la "escritura" (guardar el formulario) de la "lectura" (consultar los datos). Escribir va a una tabla inmutable de eventos; leer va a una tabla analítica optimizada.
2. **Event Sourcing:** Cada envío de formulario es un "evento" que se guarda COMPLETO e INMUTABLE (como un acta notarial). Nunca se borra ni se modifica. Si se necesita "corregir", se genera un NUEVO evento.
3. **Protección de Camunda:** El motor de procesos (Camunda 7) NO recibe los datos pesados. Solo recibe un mini-resumen ("aprobado: sí/no") para tomar decisiones de enrutamiento.

### Problema crucial identificado:

La US-017 tiene **11 de sus 16 CAs duplicados** textualmente (o casi textualmente) con CAs de la US-029. Esto confirma y amplifica el GAP-1 detectado en el análisis de la US-029, donde se identificaron 13 CAs duplicados. La Política de Propiedad del CA-19 de la US-029 ya define QUIÉN manda sobre cada aspecto compartido, pero la US-017 aún NO referencia esa política.

---

## 2. Objetivo Principal

**Objetivo de negocio:** Garantizar que cada envío de formulario quede registrado de forma inmutable, con trazabilidad forense completa, y que el motor de procesos Camunda sea protegido contra contaminación de datos masivos.

**Objetivo técnico:** Implementar la capa de persistencia CQRS con Event Sourcing, donde:
- El **Command Side** (escritura) graba el evento `FORM_SUBMITTED` en una tabla inmutable de PostgreSQL.
- El **Query Side** (lectura) proyecta asincrónicamente los datos a tablas relacionales planas para dashboards y analítica.
- La **integración con Camunda** se limita a un DTO mínimo con solo las variables de enrutamiento.

---

## 3. Alcance Funcional Definido

### ✅ DENTRO del alcance EXCLUSIVO de la US-017 (no duplicado con US-029):

| # | Funcionalidad | CAs |
|---|---|---|
| 1 | Event Sourcing: grabación inmutable de `FORM_SUBMITTED` | CA-12 |
| 2 | Proyección asíncrona a tablas analíticas planas | CA-12 |
| 3 | Exclusión topológica: DTO mínimo a Camunda | CA-13 |
| 4 | Rollback compensatorio (Saga inversa) si Camunda falla | CA-14 |
| 5 | Auto-Claim implícito para tareas de grupo sin assignee | CA-15 |
| 6 | Trazabilidad de rechazos históricos (`rejectionLogs`) en BFF | CA-16 |

### ⚠️ DUPLICADO con US-029 (ya cubierto por la historia gemela):

| CA US-017 | Equivalente US-029 | Aspecto |
|---|---|---|
| CA-01 | CA-01 (US-029) | Envío válido → COMPLETED |
| CA-02 | CA-02 (US-029) | Envío inválido → HTTP 400 |
| CA-03 | CA-05 / CA-10 (US-029) | BFF Mega-DTO |
| CA-04 | CA-08 (US-029) | Lazy Patching |
| CA-05 | CA-09 / CA-13 (US-029) | Upload-First + Anti-IDOR |
| CA-06 | CA-03 / CA-11 (US-029) | Borrador + Cifrado PII + LocalStorage |
| CA-07 | CA-17 (US-029) | RYOW (purga Pinia + LocalStorage) |
| CA-08 | CA-12 (US-029) | Idempotencia Anti-Doble Clic |
| CA-09 | CA-15 (US-029) | Zod Isomórfico |
| CA-10 | CA-06 / CA-14 (US-029) | Micro-Tokens + Anti-Replay |
| CA-11 | CA-07 / CA-18 (US-029) | Implicit Locking |

**Resultado:** 11 de 16 CAs están duplicados. Solo **5 CAs son exclusivos** de la US-017 (CA-12, CA-13, CA-14, CA-15, CA-16).

---

## 4. Lista de Funcionalidades Incluidas (16 CAs)

### Bloque A — Ejecución Base y Validación (2 CAs) — ⚠️ DUPLICADOS
- **CA-01:** Envío exitoso → COMPLETED + persistencia inmutable
- **CA-02:** Envío inválido → HTTP 400 con detalle por campo

### Bloque B — Inicialización y Contexto UI (2 CAs) — ⚠️ DUPLICADOS
- **CA-03:** BFF Mega-DTO con schema_version
- **CA-04:** Lazy Patching (V1 → V2 campos en rojo)

### Bloque C — Carga Binaria y Seguridad (1 CA) — ⚠️ DUPLICADO
- **CA-05:** Upload-First + Anti-IDOR + Cron Job 24h

### Bloque D — Resiliencia Offline y UX (3 CAs) — ⚠️ DUPLICADOS
- **CA-06:** Draft LocalStorage + cifrado AES PII + Merge Commit 10s + purga 72h
- **CA-07:** RYOW (purga Pinia antes de redirect)
- **CA-08:** Idempotencia con `Idempotency-Key` UUID

### Bloque E — Seguridad Zero-Trust (3 CAs) — ⚠️ DUPLICADOS
- **CA-09:** Zod Isomórfico transpilado a JSON Schema en CI/CD + strip silencioso
- **CA-10:** Micro-Tokens JWT con `jti` invalidado en Redis post-submit
- **CA-11:** Implicit Locking (assignee vs SecurityContext)

### Bloque F — Arquitectura CQRS y Protección del Motor (3 CAs) — ✅ EXCLUSIVOS
- **CA-12:** 🟢 **EXCLUSIVO.** Event Sourcing: `FORM_SUBMITTED_EVENT` en tabla inmutable + Worker asíncrono de proyección a tablas analíticas planas.
- **CA-13:** 🟢 **EXCLUSIVO.** Exclusión topológica: solo DTO mínimo a Camunda (`{aprobado: true, form_storage_id: "ABC-123"}`). PROHIBIDO enviar payload masivo a `ACT_RU_VARIABLE`.
- **CA-14:** 🟢 **EXCLUSIVO.** ACID Fallback con Saga inversa: si Camunda falla → Rollback de CQRS → HTTP 500 crudo → SIN falsos positivos HTTP 202.

### Bloque G — Reasignaciones y Colisiones Group-Level (2 CAs) — ✅ EXCLUSIVOS
- **CA-15:** 🟢 **EXCLUSIVO.** Auto-Claim implícito: si la tarea es de grupo (sin assignee), el Backend ejecuta `taskService.claim()` silenciosamente antes del Submit.
- **CA-16:** 🟢 **EXCLUSIVO.** Trazabilidad de rechazos: BFF inyecta `rejectionLogs` con dictamen, responsable y fecha cuando una tarea fue devuelta.

---

## 5. Brechas, GAPs y Ambigüedades Detectadas

### 🔴 GAP-1 (CRÍTICO): Duplicación Masiva de 11 CAs con la US-029

**El problema:** 11 de los 16 CAs de la US-017 están duplicados textualmente con CAs de la US-029. Esto genera ambigüedad de propiedad: ¿cuál historia es la "dueña" de cada CA?

**Contexto de US-029:** La US-029 ya resolvió este problema con el **CA-19 (Política de Propiedad Exclusiva)**, que establece:
- US-029 = FUENTE AUTORITATIVA para Frontend, UX, validación en navegador, archivos, borradores.
- US-017 = FUENTE AUTORITATIVA para CQRS, Event Sourcing, protección de Camunda, Saga, validación Backend.

**Sin embargo, la US-017 NO referencia el CA-19 de la US-029.** Los 11 CAs duplicados siguen existiendo en la US-017 sin etiquetado, lo que anula el efecto reconciliatorio del CA-19.

**Recomendación:** Los 11 CAs duplicados de la US-017 (CA-01 a CA-11) deben etiquetarse con una nota de referencia que diga: *"Este CA tiene equivalente en US-029. La fuente autoritativa para el aspecto Frontend/UX es la US-029. La fuente autoritativa para el aspecto Backend/CQRS es esta US-017."* Alternativamente, los CAs duplicados que son 100% Frontend (Ej: RYOW de Pinia, LocalStorage) podrían moverse a un apéndice de "CAs heredados de US-029" para reducir la confusión.

---

### 🟡 GAP-2 (MEDIO): Ausencia de Bloque de Dependencias

**El problema:** La US-017 NO tiene el bloque `[!IMPORTANT] Dependencias Externas Críticas` que sí tienen la US-029, US-039, US-001 y US-002. La US-017 depende críticamente de:
- **US-003:** Los esquemas Zod que valida el Backend en el CA-09 se generan en la US-003.
- **US-029:** Es la historia gemela que define el Frontend. Sin la US-029, no hay UI que envíe datos al Backend.
- **US-034 (RabbitMQ):** Si el Worker asíncrono de proyección (CA-12) usa colas de mensajería para procesar eventos.
- **US-035 (SharePoint/SGDEA):** El Upload-First del CA-05 necesita la bóveda documental.
- **US-036 (RBAC):** El strip silencioso del CA-09 necesita la matriz de roles para saber qué campos puede escribir cada usuario.

**Recomendación:** Generar e inyectar el bloque de dependencias antes de los CAs, consistente con el formato ya establecido en US-029/US-002/US-001.

---

### 🟡 GAP-3 (MEDIO): Draft Sync sin Endpoint GET de Recuperación

**El problema:** El CA-06 define que el Frontend guarda borradores (Merge Commit) en el servidor, pero NO define un endpoint para RECUPERAR el borrador del servidor. El CA de la US-029 (CA-24) define `PUT /draft` para guardar, pero TAMPOCO define un `GET /draft` explícito.

**Relación con US-029:** El CA-26 de la US-029 (Pre-aviso de caducidad) menciona: "si el borrador local ya expiró pero existe un Draft en el servidor, el formulario recuperará el progreso del servidor como fallback". Esto IMPLICA que debe existir un `GET /draft/{taskId}`, pero nunca se formaliza el endpoint.

**Recomendación:** Definir explícitamente `GET /api/v1/workbox/tasks/{taskId}/draft` como parte del contrato API, ya sea en la US-017 (como dueña de la persistencia) o en la US-029 (como consumidora del endpoint).

---

### 🟡 GAP-4 (MEDIO): Auto-Claim del CA-15 Contradice el Flujo Explícito de US-002

**El problema:** El CA-15 de la US-017 dice que si una tarea es de grupo (sin assignee directo) y el operario presiona [Enviar], el Backend ejecuta un `taskService.claim()` silencioso "una fracción de milisegundo antes" de completar. Esto es un **Auto-Claim Implícito**.

Sin embargo, la US-002 (Reclamar Tarea) y la US-029 (CA-07/CA-18) establecen que:
- El operario DEBE haber reclamado la tarea ANTES de poder abrirla para edición (US-029 dependencia bloqueante).
- El Implicit Locking RECHAZA con HTTP 403 cualquier intento de completar sin assignee (CA-07/CA-18).

**Contradicción:** Si el operario abre una tarea de grupo SIN reclamarla (porque el CA-15 promete un auto-claim silencioso), pero el CA-18 de la US-029 dice que sin assignee se obtiene HTTP 403... ¿qué gana?

**Recomendación:** Decidir una de dos opciones:
1. **Opción A (Consistente con US-002/US-029):** Eliminar el CA-15 de la US-017. El operario SIEMPRE debe reclamar explícitamente antes de completar. Las tareas de grupo se reclaman desde el Tab "Disponibles" del Workdesk (US-002 CA-01).
2. **Opción B (Flujo mixto):** Mantener el CA-15 pero acotarlo: el Auto-Claim implícito SOLO aplica si la tarea es de grupo, NO tiene assignee, Y el operario tiene el rol RBAC correcto para ese grupo. En este caso, el CA-18 debe incluir una excepción que diga "excepto cuando el CA-15 de US-017 ejecuta Auto-Claim".

---

### 🟡 GAP-5 (MEDIO): Falta Definición del Esquema de la Tabla de Eventos

**El problema:** El CA-12 dice que se inyecta un `FORM_SUBMITTED_EVENT` en una "tabla inmutable de Eventos", pero NO se define:
- ¿Cómo se llama la tabla? (Ej: `event_store`, `form_events`, `cqrs_events`?)
- ¿Qué columnas tiene? (Ej: `event_id`, `event_type`, `task_id`, `user_id`, `payload_json`, `schema_version`, `created_at`?)
- ¿Qué tipos de eventos existen además de `FORM_SUBMITTED`? (Ej: `FORM_DRAFT_SAVED`, `FORM_REJECTED`, `FILE_UPLOADED`?)
- ¿El Worker de proyección es un proceso de la misma aplicación (in-process) o un microservicio separado?
- ¿Qué tablas analíticas se generan desde la proyección?

**Recomendación:** Definir el contrato del Event Store como un CA adicional o como una nota arquitectónica en el bloque de la US-017.

---

## 6. Lista de Exclusiones (Fuera de Alcance)

| # | Aspecto | Dónde vive |
|---|---|---|
| 1 | Experiencia del operario en Pantalla 2 (UI, spinners, modales) | US-029 (Frontend) |
| 2 | Diseño de formularios (campos, layout, esquema Zod) | US-003 (IDE Pantalla 7) |
| 3 | Reclamar tarea antes de completar | US-002 (Pantalla 1) |
| 4 | Bóveda documental de archivos | US-035 (SharePoint/SGDEA) |
| 5 | Gestión de roles RBAC | US-036 (Pantalla 14) |
| 6 | Cola de mensajería (si aplica al Worker de proyección) | US-034 (RabbitMQ) |
| 7 | Dashboards de Analítica que consumen las tablas proyectadas | US-009 (BAM Dashboard) |
| 8 | Feedback visual de envío (overlay, spinner, confirmación) | US-029 (CA-20, CA-21) |
| 9 | Autoguardado en LocalStorage / cifrado PII | US-029 (CA-11) — FUENTE AUTORITATIVA per CA-19 |
| 10 | Upload-First con barra de progreso y límites | US-029 (CA-28, CA-29) |

---

## 7. Observaciones de Alineación y Riesgos para Continuar

### 🔴 Riesgo Alto: Duplicación NO Reconciliada en US-017

La US-029 ya hizo su parte con el CA-19 (Política de Propiedad). Pero la US-017 sigue teniendo 11 CAs duplicados sin etiqueta. Si un desarrollador lee SOLO la US-017, implementará CAs que son responsabilidad de la US-029 (Ej: localStorage, RYOW Pinia, feedback visual del error 400). Esto genera:
- **Doble trabajo** (dos personas implementan lo mismo).
- **Conflicto de merge** (dos PRs tocan los mismos archivos).
- **Divergencia de comportamiento** (el CA-06 de US-017 dice "guardar a cada tecla", pero el CA-11 de US-029 dice "guardar con Debounce 10s" — ¿cuál es correcto?).

### 🟡 Riesgo Medio: CA-15 (Auto-Claim) Puede Romper el Flujo de US-002

Si se implementa el Auto-Claim implícito del CA-15, el flujo de reclamar tareas (US-002) pierde sentido para tareas de grupo: ¿para qué reclamar si el sistema reclama automáticamente al enviar? Esto puede confundir al operario que ve la tarea en el Tab "Disponibles" pero nunca la reclama porque sabe que "se reclama sola al enviar".

### ✅ Fortalezas de la US-017 (Aspectos Exclusivos):

| # | Fortaleza | CA |
|---|---|---|
| 1 | **Event Sourcing real** — Cada envío es un evento inmutable, forense, irrefutable | CA-12 |
| 2 | **Protección de Camunda** — Solo DTO mínimo al motor, previene degradación | CA-13 |
| 3 | **Honestidad operativa** — Saga inversa + HTTP 500 crudo, jamás falsos positivos | CA-14 |
| 4 | **Trazabilidad de rechazos** — `rejectionLogs` inyectados en el BFF | CA-16 |

### 📊 Mapa de Reconciliación US-029 ↔ US-017

```
┌─────────────────────────────────────────────────────────────┐
│                    ENDPOINT COMPARTIDO                       │
│            POST /api/v1/workbox/tasks/{id}/complete          │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  US-029 (34 CAs)              US-017 (16 CAs)              │
│  ═══════════════              ═══════════════               │
│  FUENTE AUTORITATIVA:         FUENTE AUTORITATIVA:          │
│  • Frontend / UX              • Backend / CQRS              │
│  • Validación navegador       • Event Sourcing              │
│  • Upload-First UX            • Protección Camunda          │
│  • LocalStorage + PII         • Rollback Saga               │
│  • Overlay/Spinner/✅          • Validación Backend (JSON)   │
│  • Wizard Navigation          • Micro-Tokens Redis          │
│  • Draft sincronización       • Auto-Claim (?) CA-15        │
│  • Pestañas duplicadas        • rejectionLogs CA-16         │
│  • Campos condicionales       • Proyección analítica        │
│  • Campos solo-lectura                                      │
│  • Anti-envío accidental                                    │
│                                                             │
│  CA-19 ════════════════╗                                    │
│  (Política de          ║      ← FALTA REFERENCIA →          │
│   Propiedad)           ║      Los 11 CAs duplicados de      │
│                        ║      US-017 NO referencian CA-19    │
│                        ╚════════════════════════════════════ │
└─────────────────────────────────────────────────────────────┘
```

### Inventario de la US-017:

| Categoría | CAs | Rango |
|---|---|---|
| **Duplicados con US-029** | 11 | CA-01 a CA-11 |
| **Exclusivos de US-017** | 5 | CA-12 a CA-16 |
| **Total** | **16** | ← 5 CAs aportan valor único |

### Hoja de ruta sugerida para resolver los GAPs:

1. **GAP-1 (Duplicación):** Etiquetar los 11 CAs duplicados como "Referencia cruzada con US-029 CA-XX. Fuente autoritativa según CA-19 de US-029." — Se puede resolver con un bloque de texto inyectado, NO requiere mover CAs.
2. **GAP-2 (Dependencias):** Generar el bloque `[!IMPORTANT]` con las dependencias críticas.
3. **GAP-3 (GET /draft):** Definir el endpoint de lectura de borrador como un CA adicional.
4. **GAP-4 (Auto-Claim):** Decidir con el Product Owner si se mantiene o se elimina el CA-15.
5. **GAP-5 (Event Store):** Definir el esquema de la tabla de eventos como nota arquitectónica.
