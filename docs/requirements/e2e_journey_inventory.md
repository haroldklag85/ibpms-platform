# Inventario de Flujos de Negocio E2E (Business Journeys) — iBPMS V1

**Autor:** Product Owner (Antigravity)  
**Fecha:** 2026-04-05  
**Estado:** ⏳ Propuesta para validación PO  
**Método de extracción:** Análisis cruzado de `actors_catalog.md` × `scope_master_v1.md` × `traceability_matrix.md` × dependencias explícitas en `v1_user_stories.md`

> [!IMPORTANT]
> Este inventario es el primer paso para cerrar la capa de escenarios E2E/UAT. Cada journey listado aquí necesita ser desarrollado en detalle (Etapa 2) antes de poder escribir tests.  
> **Ley Anti-Alucinación:** Solo se listan journeys cuyas US y actores están confirmados en los artefactos existentes. Los GAPs están marcados explícitamente.

---

## Metodología de Construcción

Se aplicaron 3 filtros cruzados para identificar los journeys:

1. **Por actor:** Para cada uno de los 12 actores del `actors_catalog.md`, mapear su journey principal (¿qué hace esta persona de principio a fin?).
2. **Por dependencia inter-US:** Las secciones `[!IMPORTANT] Dependencias Externas Críticas` dentro de `v1_user_stories.md` revelan las cadenas de US que se necesitan mutuamente.
3. **Por épica cruzada:** Los journeys que cruzan 3+ épicas son los más críticos porque son donde la integración falla primero.

---

## Catálogo de Journeys

### Journeys de Criticidad Alta (MUST testear E2E)

---

#### J-01: Intake de Queja por Correo → Resolución Operativa → Cierre y Notificación

| Campo | Valor |
|---|---|
| **Actor principal** | Líder SAC |
| **Actores secundarios** | Agente IA, Operario, Cliente Externo, Worker RabbitMQ, Motor Camunda |
| **Objetivo de negocio** | Un correo de queja de un cliente se recibe, clasifica automáticamente, genera un caso, se resuelve operativamente y se notifica al cliente. |
| **Criticidad** | 🔴 ALTA — Es el flujo más largo del sistema, cruza 7 épicas y 10+ US. Cualquier falla de integración aquí es visible para el cliente final. |
| **Épicas cruzadas** | 9 (IA/Buzones) → 10 (Intake/CRM) → 1 (Workdesk) → 2 (Formularios) → 16 (CQRS) → 15 (Notificaciones) → Portal B2C |
| **US involucradas** | US-037, US-016, US-004, US-012, US-013, US-014, US-040, US-022, US-023, US-001, US-002, US-029, US-017, US-049, US-026, US-000 |
| **FR relacionados** | FR-17, FR-15, FR-35, FR-21, FR-01, FR-02, FR-05, FR-45, FR-44, FR-25 |
| **NFR impactados** | NFR-PER-01 (latencia bandejas), NFR-PER-04 (latencia CQRS), NFR-AVA-04 (resiliencia MQ), NFR-OBS-02 (tracing distribuido), NFR-SEC-02 (PII) |
| **Pantallas** | P16 → P2C → P4 → P1 → P2 → Portal B2C |
| **Precondiciones conocidas** | Buzón configurado (US-037), proceso BPMN desplegado (US-005), formulario publicado (US-003), RBAC configurado (US-036), Motor de notificaciones activo (US-049) |

**Cadena de pasos (alto nivel):**

| # | Actor | Acción | US | Pantalla |
|---|---|---|---|---|
| 1 | Sistema (MS Graph) | Correo llega al buzón corporativo | US-004 | — |
| 2 | Agente IA | Clasifica intención y enriquece con datos CRM | US-012, US-013, US-014 | P2C |
| 3 | Agente IA | Ejecuta pre-triaje (¿es spam? ¿es duplicado?) | US-040 | P4 |
| 4 | Líder SAC | Revisa propuesta IA y confirma creación de caso | US-022 | P2C/P4 |
| 5 | Sistema | Correlaciona hilo de correo al caso | US-023 | — |
| 6 | Motor Camunda | Instancia proceso BPMN y genera tarea | — | — |
| 7 | Operario | Ve tarea en Workdesk con semáforo SLA | US-001 | P1 |
| 8 | Operario | Reclama tarea de cola grupal | US-002 | P1 |
| 9 | Operario | Abre formulario, llena datos, adjunta archivos | US-029 | P2 |
| 10 | Sistema (CQRS) | Persiste evento inmutable + señaliza Camunda | US-017 | — |
| 11 | Worker RabbitMQ | Envía notificación al cliente | US-049 | — |
| 12 | Cliente Externo | Consulta estado y descarga documento | US-026 | Portal B2C |

> [!WARNING]
> **GAP-J01-01:** Entre el paso 6 (Camunda instancia proceso) y el paso 7 (tarea aparece en Workdesk) no existe CA que documente el mecanismo exacto de señalización. ¿Es un polling del Workdesk o un evento push? Inferido como polling por el CA-10 de US-001 (búsqueda server-side), pero no confirmado explícitamente.

> **GAP-J01-02:** La US-040 (Embudo de Intake) filtra spam/duplicados pero no define qué pasa con el correo descartado: ¿se notifica al remitente? ¿Se marca como leído? ¿Se mueve a trash? Verificar con la US-040 CAs.

---

#### J-02: Diseñar Proceso BPMN → Crear Formulario → Desplegar → Ejecutar Primera Tarea

| Campo | Valor |
|---|---|
| **Actor principal** | Arquitecto de Procesos (BPM Analyst) |
| **Actores secundarios** | Operario, Motor Camunda |
| **Objetivo de negocio** | Modelar un nuevo proceso de negocio, crear el formulario que lo acompaña, desplegarlo y verificar que la primera tarea se ejecuta correctamente. |
| **Criticidad** | 🔴 ALTA — Es el journey de "primer uso" de la plataforma. Si falla, nadie puede crear nada. |
| **Épicas cruzadas** | 4 (BPMN) → 2 (Formularios) → 1 (Workdesk) → 16 (CQRS) |
| **US involucradas** | US-005, US-003, US-029, US-017, US-001, US-002 |
| **FR relacionados** | FR-08, FR-04, FR-05, FR-45, FR-01, FR-02 |
| **NFR impactados** | NFR-PER-02 (latencia formularios), NFR-MNT-01 (aislamiento Camunda), NFR-MNT-02 (data-driven UI) |
| **Pantallas** | P6 → P7 → P1 → P2 |
| **Precondiciones conocidas** | RBAC configurado con rol "Arquitecto BPM" (US-036), acceso a Pantalla 6 y 7 (actors_catalog.md) |

**Cadena de pasos:**

| # | Actor | Acción | US | Pantalla |
|---|---|---|---|---|
| 1 | Arquitecto BPM | Modela proceso BPMN en lienzo visual | US-005 | P6 |
| 2 | Arquitecto BPM | Configura lanes, gateways y user tasks | US-005 | P6 |
| 3 | Arquitecto BPM | Crea formulario iForm vinculado al user task | US-003 | P7 |
| 4 | Arquitecto BPM | Define esquema Zod y layout Vue | US-003 | P7 |
| 5 | Arquitecto BPM | Despliega proceso con versión semántica | US-005 | P6 |
| 6 | Sistema | Inicia instancia del proceso (manual o webhook) | US-004/US-024 | — |
| 7 | Operario | Ve primera tarea en Workdesk | US-001 | P1 |
| 8 | Operario | Reclama y abre formulario | US-002, US-029 | P1→P2 |
| 9 | Operario | Llena formulario y envía | US-029 | P2 |
| 10 | Sistema (CQRS) | Persiste y avanza flujo BPMN | US-017 | — |

> **GAP-J02-01:** No hay CA que documente cómo se vincula un formulario iForm (US-003) a un User Task del BPMN (US-005). ¿Es por configuración en el modeler? ¿Es por `formKey` de Camunda? DECISIÓN PENDIENTE.

---

#### J-03: Configurar RBAC → Crear Roles → Asignar Usuario → Verificar Gobernanza Visual

| Campo | Valor |
|---|---|
| **Actor principal** | Oficial de Seguridad (CISO) |
| **Actores secundarios** | Operario (víctima del control de acceso) |
| **Objetivo de negocio** | Garantizar que un usuario sin permisos NO puede acceder a pantallas restringidas, y que la gobernanza visual anti-FOUC funciona correctamente. |
| **Criticidad** | 🔴 ALTA — Fallas aquí son vulnerabilidades de seguridad. |
| **Épicas cruzadas** | 13 (RBAC) → 15 (Gobernanza Visual) → 1 (Workdesk) |
| **US involucradas** | US-036, US-038, US-048, US-051, US-001 |
| **FR relacionados** | FR-30, FR-31, FR-32, FR-33, FR-01 |
| **NFR impactados** | NFR-SEC-01 (Zero Trust), NFR-SEC-04 (secretos), NFR-OBS-05 (auditoría secretos) |
| **Pantallas** | P14 → Transversal (Router/Sidebar) → P1 |

**Cadena de pasos:**

| # | Actor | Acción | US | Pantalla |
|---|---|---|---|---|
| 1 | CISO | Crea nuevo rol "Operario_Limitado" en matriz RBAC | US-036 | P14 |
| 2 | CISO | Asigna permisos: solo Workdesk (P1) y Formularios (P2) | US-036 | P14 |
| 3 | CISO | Crea/sincroniza usuario desde EntraID | US-038 | P14 |
| 4 | CISO | Asigna rol "Operario_Limitado" al usuario | US-038 | P14 |
| 5 | Operario | Inicia sesión. Sidebar muestra SOLO Workdesk | US-051 | Transversal |
| 6 | Operario | Intenta acceder a URL /admin/modeler (adivinación) | US-051 | Transversal |
| 7 | Sistema | Muestra 404 (Gaslighting), no 403 | US-051 | Transversal |
| 8 | Operario | Accede al Workdesk normalmente | US-001 | P1 |

---

#### J-04: Recepción de Tarea → Ejecución de Formulario → Completar → Persistencia CQRS

| Campo | Valor |
|---|---|
| **Actor principal** | Operario / Analista |
| **Actores secundarios** | Motor Camunda, Worker RabbitMQ |
| **Objetivo de negocio** | El journey más básico del "día a día" del operario: recibir tarea, llenar formulario, enviar, que el motor avance. |
| **Criticidad** | 🔴 ALTA — Si esto no funciona, nadie trabaja. |
| **Épicas cruzadas** | 1 (Workdesk) → 2 (Formularios) → 16 (CQRS) |
| **US involucradas** | US-001, US-002, US-029, US-017, US-000 |
| **FR relacionados** | FR-01, FR-02, FR-05, FR-45 |
| **NFR impactados** | NFR-PER-01 (latencia bandejas), NFR-PER-02 (latencia formularios), NFR-PER-04 (latencia CQRS), NFR-PER-05 (rollback), NFR-SEC-05 (rate limiting borradores) |
| **Pantallas** | P1 → P2 |
| **Dependencias explícitas documentadas** | US-029 → US-002 (BLOQUEANTE), US-029 → US-003 (BLOQUEANTE), US-029 → US-017 (GEMELA), US-001 → US-002 (WebSocket), US-001 → US-036 (RBAC) |

**Cadena de pasos:**

| # | Actor | Acción | US + CA | Pantalla |
|---|---|---|---|---|
| 1 | Operario | Ingresa al Workdesk, ve tareas ordenadas por SLA | US-001 CA-01 | P1 |
| 2 | Operario | Semáforo SLA en amarillo (urgente) | US-001 CA-05 | P1 |
| 3 | Operario | Reclama tarea de cola grupal | US-002 CA-01 | P1 |
| 4 | Sistema | WebSocket elimina tarea de pantallas de compañeros | US-002 CA-12 | P1 (otros) |
| 5 | Operario | Abre detalle de tarea → BFF carga Mega-DTO | US-029 CA-05/CA-10 | P2 |
| 6 | Operario | Llena formulario con autoguardado en LocalStorage | US-029 CA-03/CA-11 | P2 |
| 7 | Operario | Adjunta archivo (Upload-First pattern) | US-029 CA-09 | P2 |
| 8 | Operario | Envía formulario (POST /complete) | US-029 CA-01 | P2 |
| 9 | Sistema | Validación Zod isomórfica (Frontend + Backend) | US-029 CA-02 | P2 / Backend |
| 10 | Sistema (CQRS) | Persiste evento inmutable en form_event_store | US-017 | — |
| 11 | Motor Camunda | Señaliza y avanza al siguiente nodo BPMN | US-017 | — |
| 12 | Operario | Ve confirmación RYOW y la tarea desaparece del Workdesk | US-029 CA-17 | P1 |

---

### Journeys de Criticidad Media

---

#### J-05: Onboarding de Cliente Externo → Magic Link → Acceso al Portal B2C

| Campo | Valor |
|---|---|
| **Actor principal** | Ejecutivo de Cuenta |
| **Actores secundarios** | Cliente Externo, Worker RabbitMQ |
| **Objetivo de negocio** | Vincular un cliente nuevo al Portal B2C de forma segura sin registro público. |
| **Criticidad** | 🟡 MEDIA |
| **Épicas cruzadas** | 15 (CIAM) → Portal B2C |
| **US involucradas** | US-050, US-049, US-026 |
| **FR relacionados** | FR-34, FR-44, FR-25 |
| **Pantallas** | P4 → (correo) → Portal B2C |

**Cadena:** Ejecutivo invita (US-050) → Sistema envía Magic Link (US-049) → Cliente crea contraseña (US-050) → Cliente consulta estado (US-026).

---

#### J-06: Crear Proyecto Ágil → Asignar Tareas Kanban → Mover Estados → Ver Dashboard BAM

| Campo | Valor |
|---|---|
| **Actor principal** | PMO |
| **Actores secundarios** | Operario |
| **Objetivo de negocio** | Gestionar un proyecto con metodología ágil usando tablero Kanban y monitorear su salud en el dashboard BAM. |
| **Criticidad** | 🟡 MEDIA |
| **Épicas cruzadas** | 6 (Kanban) → 7 (BAM) |
| **US involucradas** | US-030, US-008, US-009, US-018 |
| **FR relacionados** | FR-12, FR-13, FR-26, FR-27 |
| **Pantallas** | P10 → P3 → P5 |

**Cadena:** PMO crea proyecto ágil (US-030) → Asigna tareas al backlog → Operario mueve tarjetas Kanban (US-008) → PMO revisa dashboard BAM (US-009, US-018).

---

#### J-07: Configurar SLA → Proceso Ejecutándose → Semáforo Quiebra → Escalamiento

| Campo | Valor |
|---|---|
| **Actor principal** | PMO |
| **Actores secundarios** | Operario, Sistema |
| **Objetivo de negocio** | Configurar niveles de servicio y verificar que las alertas de quiebre funcionan en tiempo real durante la ejecución de tareas. |
| **Criticidad** | 🟡 MEDIA |
| **Épicas cruzadas** | 14 (SLA) → 1 (Workdesk) → 15 (Notificaciones) |
| **US involucradas** | US-043, US-001, US-049 |
| **FR relacionados** | FR-39, FR-01, FR-44 |
| **Pantallas** | P19 → P1 |

**Cadena:** PMO configura calendario y SLA (US-043) → Proceso se ejecuta → Tarea acumula tiempo → Semáforo cambia a rojo (US-001 CA-05) → Sistema envía toast/notificación (US-049).

---

#### J-08: Registrar Buzón SAC → Conectar MS Graph → Recibir Correos → Activar Triaje IA

| Campo | Valor |
|---|---|
| **Actor principal** | Administrador de TI |
| **Actores secundarios** | Líder SAC, Agente IA |
| **Objetivo de negocio** | Conectar un nuevo buzón corporativo al sistema y verificar que el pipeline de IA procesa los correos correctamente. |
| **Criticidad** | 🟡 MEDIA |
| **Épicas cruzadas** | 9 (Buzones/IA) → 3 (Triggers) |
| **US involucradas** | US-037, US-016, US-004, US-012 |
| **FR relacionados** | FR-17, FR-35, FR-15 |
| **Pantallas** | P16 → (webhook) → P2C |

**Cadena:** Admin TI registra buzón (US-037) → Configura políticas (US-016) → Webhook recibe primer correo (US-004) → Agente IA clasifica (US-012).

---

#### J-09: DevPortal — Crear API Key → Consumir API Externa → Rate Limiting

| Campo | Valor |
|---|---|
| **Actor principal** | Desarrollador Externo |
| **Actores secundarios** | Super Admin |
| **Objetivo de negocio** | Un integrador externo obtiene credenciales y puede consumir las APIs del iBPMS dentro de los límites configurados. |
| **Criticidad** | 🟡 MEDIA |
| **Épicas cruzadas** | 12 (Integraciones) → 15 (DevPortal) |
| **US involucradas** | US-042, US-033, US-046 |
| **FR relacionados** | FR-40, FR-36, FR-43 |
| **Pantallas** | P13 |

**Cadena:** Admin crea cuenta de servicio (US-042) → API Key mostrada una sola vez → Dev Externo consume endpoint → Sistema aplica rate-limiting (US-046).

---

#### J-10: Configurar Agentes IA → Ejecutar Tarea Cognitiva → Monitorear Consumo FinOps

| Campo | Valor |
|---|---|
| **Actor principal** | Administrador de IA / MLOps Engineer |
| **Actores secundarios** | Agente IA |
| **Objetivo de negocio** | Configurar el motor multi-agente, ejecutar una tarea cognitiva dentro de un proceso BPMN y verificar que el consumo de tokens FinOps se registra. |
| **Criticidad** | 🟡 MEDIA |
| **Épicas cruzadas** | 17 (Agentes IA) → 11 (Cognitive BPMN) |
| **US involucradas** | US-052, US-053, US-032, US-044 |
| **FR relacionados** | FR-18, FR-19, FR-38, FR-41 |
| **Pantallas** | Command Center → P15 |

**Cadena:** Admin IA configura límites de tokens (US-044) → Configura agentes (US-052) → Proceso BPMN ejecuta tarea RAG (US-032) → Command Center muestra consumo (US-053).

---

### Journeys de Criticidad Baja (Nice-to-have para E2E)

---

#### J-11: Crear Proyecto Tradicional (Gantt) → Definir Dependencias → Ruta Crítica → BAM

| Campo | Valor |
|---|---|
| **Actor principal** | PMO |
| **US involucradas** | US-031, US-006, US-009 |
| **Criticidad** | ⚪ BAJA |
| **Pantallas** | P8 → P10.B → P5 |

---

#### J-12: Generar Reglas DMN con IA → Vincular a Proceso BPMN → Ejecutar Gateway

| Campo | Valor |
|---|---|
| **Actor principal** | Arquitecto BPM |
| **US involucradas** | US-007, US-005, US-027 |
| **Criticidad** | ⚪ BAJA |
| **Pantallas** | P6 |

---

#### J-13: Generar Documento PDF → Firmar con Hash → Almacenar en SharePoint

| Campo | Valor |
|---|---|
| **Actor principal** | Sistema (automatizado post-formulario) |
| **US involucradas** | US-010, US-035 |
| **Criticidad** | ⚪ BAJA |
| **Pantallas** | P2 (descarga) |

---

## Validación Cruzada

### Cobertura de US MUST por Journeys

| US | Journey(s) | ¿Cubierta? |
|----|-----------|------------|
| US-000 | J-01, J-04 (transversal) | ✅ |
| US-001 | J-01, J-02, J-03, J-04, J-07 | ✅ |
| US-002 | J-01, J-02, J-04 | ✅ |
| US-003 | J-02 | ✅ |
| US-004 | J-01, J-08 | ✅ |
| US-005 | J-02, J-12 | ✅ |
| US-006 | J-11 | ✅ |
| US-007 | J-12 | ✅ |
| US-008 | J-06 | ✅ |
| US-009 | J-06, J-07, J-11 | ✅ |
| US-010 | J-13 | ✅ (SHOULD) |
| US-011 | — | ❌ **GAP** |
| US-012 | J-01, J-08 | ✅ |
| US-013 | J-01 | ✅ |
| US-014 | J-01 | ✅ |
| US-015 | — | ❌ **Justificado** (batch nocturno, no E2E UI) |
| US-016 | J-01, J-08 | ✅ |
| US-017 | J-01, J-02, J-04 | ✅ |
| US-018 | J-06 | ✅ |
| US-019 | J-01 (implícito: CRM activo) | ✅ (precondición) |
| US-020 | J-01 (implícito) | ✅ (precondición) |
| US-021 | — | ❌ **GAP** |
| US-022 | J-01 | ✅ |
| US-023 | J-01 | ✅ |
| US-024 | J-02 (variante: inicio manual) | ✅ |
| US-025 | — | ❌ **GAP** |
| US-026 | J-01, J-05 | ✅ |
| US-027 | J-12 | ✅ |
| US-029 | J-01, J-02, J-04 | ✅ |
| US-030 | J-06 | ✅ |
| US-031 | J-11 | ✅ |
| US-032 | J-10 | ✅ |
| US-033 | J-09 | ✅ |
| US-034 | J-01, J-04 (implícito: mensajería) | ✅ (infraestructura) |
| US-035 | J-13 | ✅ |
| US-036 | J-03 (principal) + precondición de todos | ✅ |
| US-037 | J-01, J-08 | ✅ |
| US-038 | J-03 | ✅ |
| US-039 | J-04 (variante: formulario genérico) | ✅ |
| US-040 | J-01 | ✅ |
| US-041 | — | ❌ **GAP** |
| US-042 | J-09 | ✅ |
| US-043 | J-07 | ✅ |
| US-044 | J-10 | ✅ |
| US-045 | — | ❌ **GAP** |
| US-046 | J-09 | ✅ |
| US-048 | J-03 | ✅ |
| US-049 | J-01, J-05, J-07 | ✅ |
| US-050 | J-05 | ✅ |
| US-051 | J-03 | ✅ |
| US-052 | J-10 | ✅ |
| US-053 | J-10 | ✅ |

### Resumen de Cobertura

| Métrica | Valor |
|---|---|
| **Total US** | 53 |
| **US cubiertas por al menos 1 journey** | 47 (88.7%) |
| **US no cubiertas** | 6 |
| **US no cubiertas justificadas** | 1 (US-015: batch nocturno) |
| **US con GAP de cobertura** | 5 (US-011, US-021, US-025, US-041, US-045) |

### Cobertura de Actores

| Actor | Journey principal | ¿Cubierto? |
|---|---|---|
| Operario / Analista | J-04, J-01 | ✅ |
| Líder SAC | J-01 | ✅ |
| Arquitecto BPM | J-02, J-12 | ✅ |
| PMO | J-06, J-07, J-11 | ✅ |
| Ejecutivo de Cuenta | J-05 | ✅ |
| Super Admin | J-03, J-09 (implícito) | ✅ |
| CISO | J-03 | ✅ |
| Admin TI | J-08 | ✅ |
| Admin IA | J-10 | ✅ |
| Cliente Externo | J-01 (paso 12), J-05 | ✅ |
| Desarrollador Externo | J-09 | ✅ |
| Agente IA (Bot) | J-01, J-08, J-10, J-12 | ✅ |

**Cobertura de actores: 12/12 (100%)** ✅

---

## GAPs Descubiertos

| ID | US no cubiertas | Propuesta de resolución |
|---|---|---|
| GAP-INV-01 | **US-011 (Docketing SAC)** — No tiene journey propio. Podría ser un paso del J-01 (la bandeja avanzada es donde el Líder SAC ve el pipeline completo antes de actuar). | Integrar como paso 3.5 del J-01 |
| GAP-INV-02 | **US-021 (Mapeo de Variables CRM)** — Es una configuración administrativa del Admin TI. Podría ser precondición del J-01 o un journey micro de configuración. | Crear micro-journey J-14 de configuración CRM o agregar como precondición del J-01 |
| GAP-INV-03 | **US-025 (Cards Dinámicas por Rol)** — Debería aparecer como validación visual en el J-01 (paso 7: el Operario ve la tarea como Card) o en el J-06. | Integrar como validación visual en J-04 y J-06 |
| GAP-INV-04 | **US-041 (Vista 360 Cliente)** — Es una pantalla administrativa. Debería integrarse en el J-05 como paso post-onboarding: "Ejecutivo ve la consolidación del cliente". | Agregar paso al J-05 |
| GAP-INV-05 | **US-045 (Restricciones de Dominio)** — Es configuración administrativa. Podría ser precondición transversal o integrada al J-06 (límites de sprints). | Precondición transversal de J-06 y J-11 |

## Supuestos No Aprobados

| ID | Supuesto |
|---|---|
| SNA-INV-01 | Se asume que US-015 (MLOps batch nocturno) no requiere journey E2E UI. Si el PO quiere validar el ciclo MLOps, se necesitaría un J-14 backend-only con test de integración REST Assured. |
| SNA-INV-02 | Se asume que las US de configuración administrativa (US-021, US-045) son precondiciones de otros journeys, no journeys independientes. |
| SNA-INV-03 | Se asume que el J-04 (tarea simple) es suficiente para validar el "día a día" sin necesidad de separar un journey por tipo de formulario (Maestro vs Simple vs Genérico). |

## Decisiones Pendientes para Etapa 2

| ID | Decisión requerida |
|---|---|
| DP-INV-01 | ¿Se incorporan las 5 US con GAP como pasos/precondiciones de journeys existentes o se crean micro-journeys dedicados? |
| DP-INV-02 | ¿Se documenta el J-01 completo (12 pasos, el más largo) primero como piloto, o se empieza por el J-04 (más corto, 12 pasos pero más simple)? |
| DP-INV-03 | ¿Cuántos journeys se detallan en la Etapa 2? Recomendación: los 4 de criticidad Alta primero (J-01, J-02, J-03, J-04). |

---

## Historial de Cambios

| Fecha | Cambio | Autor |
|---|---|---|
| 2026-04-05 | Creación inicial: 13 journeys inventariados, 88.7% cobertura US, 100% cobertura actores | PO (Antigravity) |
