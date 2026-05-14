# Casos de Uso UAT — Journey J-02 (v4 — Certificación E2E)

> **Journey:** Diseñar → Modelar → Vincular → Desplegar → Ejecutar flujo real de Siniestros con 4 desenlaces E2E  
> **Proceso BPMN:** `insurance_claims_complex.bpmn` — Gestión y Liquidación de Siniestros V2  
> **Actor principal:** Arquitecto de Procesos (BPM Analyst)  
> **Actores secundarios:** Analista N1 (Adjusters), Perito A, Perito B, Supervisor (Directors), Motor Camunda, Mock Workers  
> **Criticidad:** 🔴 ALTA — Journey de primer uso y demostración integral de la plataforma  
> **US involucradas:** US-003, US-005, US-007, US-028, US-039, US-029, US-001, US-002, US-008  
> **Fecha:** 2026-04-19 (v4 — Reescritura Certificación E2E)  
> **Autor:** Agente PO + Antigravity

> [!IMPORTANT]
> **v4 — Reescritura Integral:** Este documento reemplaza J-02 v3. Cambios principales:
> - Proceso BPMN real: `insurance_claims_complex.bpmn` (5 lanes, DMN, multi-instance, sub-process, compensation)
> - **4 formularios** definidos campo-por-campo alineados a `FormDesigner.vue`
> - **4 flujos E2E** (Happy Path + 3 desenlaces alternos / error)
> - **DMN Intelligence** creación + vinculación con BusinessRuleTask
> - Mock Workers para Service Tasks (`reserve-funds`, `rollback-funds`)
> - Multi-instancia con **2 peritos distintos** (Perito A, Perito B)
> - Formulario **Genérico** probado en actividad de **tablero Kanban**
> - CAs explícitos por escenario + propiedades avanzadas de FormDesigner

---

## Precondiciones

| # | Precondición | Verificación |
|---|-------------|-------------|
| PRE-01 | Usuario autenticado con rol `ROLE_BPM_ARCHITECT` o `ROLE_SUPER_ADMIN` | Token JWT con permisos de diseño |
| PRE-02 | Motor Camunda operativo | GET `/api/v1/engine-rest/version` → 200 |
| PRE-03 | Docker Compose E2E activo (PG + Redis + Camunda + RabbitMQ) | `docker-compose.e2e.yml` |
| PRE-04 | Acceso a P7 (FormDesigner), P6 (BpmnDesigner), P15 (DmnIntelligence) | RBAC configurado |
| PRE-05 | Archivo `insurance_claims_complex.bpmn` disponible en disco | `docs/uat/bpmn_examples/` |
| PRE-06 | 2 usuarios Perito creados: `perito_a` y `perito_b` en grupo `Adjusters` | Seed data fixtures |
| PRE-07 | 1 usuario Supervisor en grupo `Directors` | Seed data fixtures |
| PRE-08 | 1 usuario Analista N1 en grupo `Adjusters` | Seed data fixtures |
| PRE-09 | Mock Workers registrados para topics `reserve-funds` y `rollback-funds` | Simulador de pruebas o consola Admin |

---

## Inventario BPMN del Proceso

> Referencia: `insurance_claims_complex.bpmn`

### Elementos del Proceso

| Símbolo | ID | Nombre | Lane | Tipo |
|---------|-----|--------|------|------|
| ▶ Message Start | `StartEvent_ClaimReceived` | Siniestro Reportado (API/App) | Sistema | Start Event (Message) |
| ⬛ Business Rule | `BusinessRule_FraudCheck` | Evaluación Automática Cobertura (DMN) | Sistema | Business Rule Task |
| ◇ Exclusive GW | `Gateway_FraudDecision` | ¿Veredicto DMN? | Sistema | Exclusive Gateway |
| ■ End (Terminate) | `EndEvent_AutoRejected` | Siniestro Terminado por Sistema | Sistema | End Event (Terminate) |
| 👤 User Task | `Task_ManualReview` | Auditar Información Siniestro | Analista N1 | User Task (`camunda:formKey`) |
| ⏱ Boundary Timer | `Event_ReviewTimeout` | Límite Legal (72 Horas) | Analista N1 | Boundary Timer |
| 👤 User Task | `Task_Escalation` | Veredicto de Supervisor Vencido | Comité | User Task (`camunda:formKey`) |
| ■ End | `End_Escalation` | Forzado a Escalamiento Legal | Comité | End Event |
| ◇ Parallel GW | `Gateway_Parallel_Docs` | Bifurcar Investigación | Analista N1 | Parallel Gateway (Fork) |
| 📩 Catch Message | `Event_WaitForPoliceReport` | Vigilancia API (Docs Policiales) | Sistema | Intermediate Catch (Message) |
| 👤 User Task (MI) | `Task_DamageAssessment` | Evaluar Daños Dinámicamente | Peritos | User Task Multi-Instance |
| ◇ Parallel GW | `Gateway_Parallel_Join` | Esperar Convergencia | Comité | Parallel Gateway (Join) |
| ▢ Sub-Process | `SubProcess_Investigation` | Aprobación Consolidada Siniestro | Comité | Embedded Sub-Process |
| ⚡ Script Task | `Task_CalculateFormula` | Fórmula Matemática Liquidación | (Sub) | Script Task |
| 👤 User Task | `Task_DirectorApproval` | Firma Final (Director) | (Sub) | User Task (`camunda:formKey`) |
| ⬛ Service Task | `Task_ReserveFunds` | Bloquear Fondos Tesorería | Finanzas | External Task (Worker) |
| 🔄 Compensation | `Event_ReserveCompensate` | Compensación Reserva | Finanzas | Boundary (Compensation) |
| ⬛ Service Task | `Task_CancelReserve` | Soltar Fondos / Rollback | Finanzas | Compensation Task (Worker) |
| 📞 Call Activity | `CallActivity_Payment` | Enlazar Microservicio SAP | Finanzas | Call Activity |
| ⚡ Error Boundary | `Event_PaymentError` | Fallo Transferencia Bancaria | Finanzas | Boundary (Error) |
| 🔄 Throw Compensate | `Event_ThrowCompensate` | Disparar Undo Financiero | Finanzas | Intermediate Throw |
| ■ End | `EndEvent_Error` | Cancelado por Banco Crítico | Finanzas | End Event |
| ■ End | `EndEvent_ClaimClosed` | Siniestro Entregado y Cerrado | Finanzas | End Event |

**Totales:** 5 Lanes, 3 User Tasks + 1 Multi-Instance, 1 Business Rule, 2 Service Tasks, 1 Script Task, 1 Call Activity, 2 Parallel GW, 1 Exclusive GW, 1 Sub-Process, 2 Boundary Events, 4 End Events, 2 Messages, 1 Error, 1 Compensation

---

## DEFINICIÓN DE FORMULARIOS (Campo por Campo)

> Referencia técnica: [form_designer_field_audit.md](file:///C:/Users/HaroltAndrésGómezAgu/.gemini/antigravity/brain/8a367ae0-4ee5-44e6-8544-4e2e60588a84/form_designer_field_audit.md)

---

### FORM-01: 🔵 iForm Maestro — "Auditoría de Siniestro" (`frm_auditoria_siniestro`)

> **Vinculado a:** `Task_ManualReview` (Analista N1)  
> **Patrón:** iForm Maestro (multi-stage)  
> **Justificación:** Tarea compleja de investigación que requiere datos del cliente, evidencia documental, y decisión de ruta.

| # | Campo | `type` | `id` | `camundaVariable` | `stage` | `required` | Propiedades Avanzadas |
|---|-------|--------|------|-------------------|---------|-----------|----------------------|
| 1 | Número de Póliza | `text` | `NUMERO_POLIZA` | `numeroPoliza` | `INTAKE` | ✅ | P-07: minLength=8, P-08: maxLength=20, P-09: `regex`, P-10: `^[A-Z]{2}-[0-9]+$`, P-17: isPII=true, P-19: soloLecturaPosterior=true |
| 2 | Nombre Asegurado | `text` | `NOMBRE_ASEGURADO` | `nombreAsegurado` | `INTAKE` | ✅ | P-07: minLength=3, P-08: maxLength=100, P-16: enableAuditLog=true, P-17: isPII=true, P-19: soloLecturaPosterior=true |
| 3 | Email de Contacto | `email` | `EMAIL_CONTACTO` | `emailContacto` | `INTAKE` | ✅ | Zod `.email()` nativo, P-17: isPII=true |
| 4 | Teléfono | `text` | `TELEFONO` | `telefono` | `INTAKE` | ❌ | P-09: `phone` (IMask +XX XXX-XXXX), P-17: isPII=true |
| 5 | Fecha del Siniestro | `date` | `FECHA_SINIESTRO` | `fechaSiniestro` | `INTAKE` | ✅ | P-19: soloLecturaPosterior=true |
| 6 | Tipo de Siniestro | `select` | `TIPO_SINIESTRO` | `tipoSiniestro` | `INTAKE` | ✅ | options: ["Incendio", "Robo", "Accidente Vehicular", "Daño por Agua", "Responsabilidad Civil", "Otro"], P-19: soloLecturaPosterior=true |
| 7 | — Sección: Análisis Técnico — | `container` | `SECCION_ANALISIS` | — | `ANALYSIS` | — | P-29: columns=2, label="Análisis del Siniestro" |
| 8 | ↳ Monto Estimado Daño | `number` | `MONTO_ESTIMADO` | `montoEstimadoDano` | `ANALYSIS` | ✅ | P-09: `currency` (IMask $ 1.500,00), Zod min=0, P-21: isOutputToken=true |
| 9 | ↳ Zona Geográfica | `select` | `ZONA_GEO` | `zonaGeografica` | `ANALYSIS` | ✅ | options: ["Urbana", "Rural", "Industrial", "Marítima"] |
| 10 | Descripción Detallada | `textarea` | `DESC_DETALLADA` | `descripcionDetallada` | `ANALYSIS` | ✅ | P-07: minLength=20, P-08: maxLength=2000, P-16: enableAuditLog=true |
| 11 | Evidencia Fotográfica | `file` | `EVIDENCIA_FOTOS` | `evidenciaFotos` | `ANALYSIS` | ✅ | P-25: maxSizeMb=10, P-26: allowedExts=".jpg,.png,.pdf", P-27: minFiles=1, maxFiles=5 |
| 12 | ¿Requiere Peritaje? | `radio` | `REQUIERE_PERITAJE` | `requierePeritaje` | `ANALYSIS` | ✅ | options: ["Sí", "No"] |
| 13 | Listado de Peritos | `select` | `LISTADO_PERITOS` | `listadoPeritos` | `ANALYSIS` | ❌ | P-24: isMultiple=true, options: ["perito_a", "perito_b"], P-11: requiredIfField=`REQUIERE_PERITAJE`, P-12: requiredIfValue="Sí", P-13: visibilityCondition=`REQUIERE_PERITAJE == 'Sí'`, P-14: clearOnHide=true |
| 14 | Decisión del Analista | `select` | `DECISION_ANALISTA` | `decisionAnalista` | `DECISION` | ✅ | options: ["Aprobar Investigación Completa", "Cerrar por Insuficiencia"], P-16: enableAuditLog=true, P-21: isOutputToken=true |
| 15 | — Accionadores — | `button_draft` | `BTN_BORRADOR` | — | `ALL` | — | label="Guardar Borrador" |
| 16 | — | `button_submit` | `BTN_COMPLETAR` | — | `ALL` | — | label="Completar Auditoría" |
| 17 | — | `button_reject` | `BTN_RECHAZAR` | — | `ALL` | — | label="Rechazar Siniestro" |

**Total campos de dato:** 13 + 3 botones = **16 componentes**  
**Tipos usados:** text(3), email(1), date(1), select(3), number(1), textarea(1), file(1), radio(1), container(1), button_draft(1), button_submit(1), button_reject(1)  
**Propiedades avanzadas demostradas:** P-07/08 (min/max), P-09/10 (IMask currency/phone/regex), P-11/12 (requiredIf condicional), P-13/14 (visibilidad + purge), P-16 (audit forense), P-17 (PII), P-19 (solo-lectura posterior), P-21 (output token), P-24 (multi-select), P-29 (grid 2 cols)

---

### FORM-02: 🟢 Simple — "Veredicto Escalamiento" (`frm_veredicto_escalamiento`)

> **Vinculado a:** `Task_Escalation` (Supervisor / Comité)  
> **Patrón:** Simple (vista única)  
> **Justificación:** Decisión rápida de escalamiento legal ante timeout.

| # | Campo | `type` | `id` | `camundaVariable` | `required` | Propiedades Avanzadas |
|---|-------|--------|------|-------------------|-----------|----------------------|
| 1 | Nº Póliza (Read-Only) | `text` | `POLIZA_REF` | `numeroPoliza` | ✅ | P-15: disableCondition="true" (siempre disabled), P-20: isPrefilled=true |
| 2 | Motivo del Vencimiento | `textarea` | `MOTIVO_VENCIMIENTO` | `motivoVencimiento` | ✅ | P-07: minLength=15, P-08: maxLength=500 |
| 3 | Acción de Escalamiento | `select` | `ACCION_ESCALAMIENTO` | `accionEscalamiento` | ✅ | options: ["Derivar a Legal Externo", "Ampliar Plazo 48h", "Cerrar con Sanción Interna"] |
| 4 | Firma Digital Supervisor | `signature` | `FIRMA_SUPERVISOR` | `firmaSupervisor` | ✅ | CA-31: Canvas HTML5 |
| 5 | — | `button_submit` | `BTN_CONFIRMAR` | — | — | label="Confirmar Veredicto" |

**Total:** 4 campos de dato + 1 botón = **5 componentes**  
**Propiedades avanzadas demostradas:** P-15 (disable condition), P-20 (prefill), P-07/08 (validación), CA-31 (firma digital)

---

### FORM-03: 🔵 iForm Maestro — "Evaluación de Daños Perito" (`frm_evaluacion_danos`)

> **Vinculado a:** `Task_DamageAssessment` (Peritos — Multi-Instance)  
> **Patrón:** iForm Maestro (multi-stage)  
> **Justificación:** Cada perito evalúa independientemente con fotos, valoración y recomendación profesional.

| # | Campo | `type` | `id` | `camundaVariable` | `stage` | `required` | Propiedades Avanzadas |
|---|-------|--------|------|-------------------|---------|-----------|----------------------|
| 1 | ID Perito (auto) | `hidden` | `ID_PERITO` | `perito` | `ALL` | ❌ | CA-47: Token silencioso, P-20: isPrefilled=true (inyectado por multi-instance) |
| 2 | Nº Póliza (Read-Only) | `text` | `POLIZA_PERITO` | `numeroPoliza` | `INSPECTION` | ✅ | P-15: disableCondition="true", P-20: isPrefilled=true |
| 3 | GPS Ubicación Siniestro | `gps` | `GPS_SINIESTRO` | `gpsSiniestro` | `INSPECTION` | ✅ | CA-61: Coordenadas HTML5 |
| 4 | — Sección: Evaluación Técnica — | `container` | `SECCION_EVAL` | — | `VALUATION` | — | P-29: columns=2 |
| 5 | ↳ Tipo de Daño Observado | `select` | `TIPO_DANO_OBS` | `tipoDanoObservado` | `VALUATION` | ✅ | options: ["Estructural", "Superficial", "Total", "Parcial Reparable"] |
| 6 | ↳ Monto Valoración Perito | `number` | `MONTO_PERITO` | `montoPerito` | `VALUATION` | ✅ | P-09: `currency`, Zod min=0, max=999999999, P-21: isOutputToken=true |
| 7 | Registro Fotográfico Peritaje | `file` | `FOTOS_PERITAJE` | `fotosPeritaje` | `INSPECTION` | ✅ | P-25: maxSizeMb=15, P-26: ".jpg,.png,.heic", P-27: minFiles=2, maxFiles=10 |
| 8 | — Tabla de Ítems Dañados — | `field_array` | `ITEMS_DANADOS` | `itemsDanados` | `VALUATION` | ✅ | P-28: minRows=1, maxRows=20, children: [text("Item"), number("Costo"), select("Estado": ["Reparable","Pérdida Total"])] |
| 9 | Observaciones del Perito | `textarea` | `OBS_PERITO` | `observacionesPerito` | `VALUATION` | ✅ | P-07: minLength=30, P-08: maxLength=3000, P-16: enableAuditLog=true |
| 10 | Recomendación Final | `radio` | `RECOMENDACION` | `recomendacionPerito` | `VALUATION` | ✅ | options: ["Proceder con Liquidación", "Investigación Adicional Requerida", "Negar Cobertura"] |
| 11 | — | `button_draft` | `BTN_BORRADOR_P` | — | `ALL` | — | label="Guardar Progreso" |
| 12 | — | `button_submit` | `BTN_ENVIAR_P` | — | `ALL` | — | label="Enviar Evaluación" |

**Total:** 9 campos de dato + 1 hidden + 2 botones = **12 componentes**  
**Propiedades avanzadas demostradas:** CA-47 (hidden token), CA-61 (GPS), P-28 (field_array/grid), P-09 (currency), P-20 (prefill MI), P-21 (output), P-25/26/27 (file constraints)

---

### FORM-04: 🟢 Simple — "Firma Final Director" (`frm_firma_director`)

> **Vinculado a:** `Task_DirectorApproval` (dentro del Sub-Process)  
> **Patrón:** Simple (vista única)  
> **Justificación:** Aprobación ejecutiva rápida con todos los datos prefilled del proceso.

| # | Campo | `type` | `id` | `camundaVariable` | `required` | Propiedades Avanzadas |
|---|-------|--------|------|-------------------|-----------|----------------------|
| 1 | Nº Póliza (Read-Only) | `text` | `POLIZA_DIR` | `numeroPoliza` | ✅ | P-15: disableCondition="true", P-20: isPrefilled=true |
| 2 | Monto Liquidación Calculado | `number` | `PAGO_FINAL` | `pagoFinal` | ✅ | P-15: disableCondition="true", P-20: isPrefilled=true (viene del Script Task), P-09: `currency` |
| 3 | Resumen Ejecutivo | `textarea` | `RESUMEN_DIR` | `resumenDirector` | ✅ | P-07: minLength=10, P-08: maxLength=500 |
| 4 | Decisión Director | `radio` | `DECISION_DIR` | `decisionDirector` | ✅ | options: ["Aprobar Liquidación", "Rechazar y Devolver"], P-16: enableAuditLog=true, P-21: isOutputToken=true |
| 5 | Firma Digital Director | `signature` | `FIRMA_DIRECTOR` | `firmaDirector` | ✅ | CA-31: Canvas HTML5, P-16: enableAuditLog=true |
| 6 | — | `button_submit` | `BTN_FIRMAR` | — | — | label="Firmar y Autorizar" |
| 7 | — | `button_reject` | `BTN_DEVOLVER` | — | — | label="Rechazar" |

**Total:** 5 campos de dato + 2 botones = **7 componentes**  
**Propiedades avanzadas demostradas:** P-15 (disable), P-20 (prefill script), P-09 (currency), P-16 (audit), P-21 (output), CA-31 (firma)

---

### FORM-05: ⚪ Genérico — `sys_generic_form`

> **Vinculado a:** Actividad de tablero Kanban ágil (sin formulario diseñado)  
> **Patrón:** Genérico fallback del sistema  
> **Justificación:** Prueba del comportamiento automático para tareas sin iForm asignado.

**Campos automáticos (no diseñados):**
- MetadataGrid (datos precargados del proceso)
- Resultado de Gestión (`select`: Aprobar / Devolver / Cancelar)
- Observaciones (`textarea`, min 10 chars)
- Adjuntos (dropzone)
- PanicButtonBar (✅ Aprobar, ↩️ Devolver, ❌ Cancelar)

---

## FASE 1: DISEÑO DE FORMULARIOS

> **Objetivo:** Crear los 4 formularios (2 Maestro + 2 Simple) con la configuración avanzada definida arriba.  
> **US:** US-003

---

### CU-J02-01: Arquitecto crea iForm Maestro "Auditoría de Siniestro"

**US:** US-003 | **CAs:** CA-01, CA-02, CA-31, CA-38

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Arquitecto | Navega a `/admin/modeler/forms` → "Nuevo Formulario" | Designer iForm con canvas vacío |
| 2 | Sistema | Modal de selección de patrón | Opciones: 🟢 Simple / 🔵 iForm Maestro |
| 3 | Arquitecto | Selecciona "🔵 iForm Maestro" | Badge azul visible en header, selector Stage activo |
| 4 | Arquitecto | Asigna nombre: "Auditoría de Siniestro" | Título visible en canvas |
| 5 | Arquitecto | Arrastra los 16 componentes del FORM-01 al canvas según tabla anterior | Campos con stages INTAKE/ANALYSIS/DECISION configurados |
| 6 | Arquitecto | Para cada campo: abre Properties (⚙️) y configura las propiedades avanzadas de la tabla FORM-01 | P-07/08 (validación), P-09/10 (IMask), P-11/12 (requiredIf), P-13/14 (visibilidad), P-16 (audit), P-17 (PII), P-19 (solo-lectura), P-21 (output), P-24 (multi-select), P-29 (grid) |
| 7 | Arquitecto | Configura Logic Builder (CA-32): regla cruzada `MONTO_ESTIMADO > 0` | Regla activa en QA Sandbox |
| 8 | Arquitecto | Usa el Stage Simulator → selecciona "INTAKE" | Solo campos INTAKE visibles |
| 9 | Arquitecto | Cambia a Stage "ANALYSIS" | Campos ANALYSIS visibles, campos INTAKE en solo-lectura |
| 10 | Arquitecto | Presiona "🚀 Probar (Submit Mock)" con datos válidos | Validación Zod OK, toast success |

**Estado esperado:** ✅ PASA  
**Criterio:** Formulario con 16 componentes, 3 stages, ≥10 propiedades avanzadas configuradas. Stage simulator funcional.

---

### CU-J02-02: Arquitecto crea formularios Simple (FORM-02 y FORM-04)

**US:** US-003 | **CAs:** CA-01, CA-02, CA-31

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Arquitecto | Crea "Veredicto Escalamiento" con patrón 🟢 Simple | 5 componentes según tabla FORM-02 |
| 2 | Arquitecto | Configura firma digital (CA-31) + prefill póliza | Firma canvas + campo disabled |
| 3 | Arquitecto | Guarda y publica FORM-02 | POST `/api/v1/forms` → 201, estado "Publicado" |
| 4 | Arquitecto | Crea "Firma Final Director" con patrón 🟢 Simple | 7 componentes según tabla FORM-04 |
| 5 | Arquitecto | Configura prefill de `pagoFinal` (viene del ScriptTask) | Campo currency disabled |
| 6 | Arquitecto | Guarda y publica FORM-04 | POST `/api/v1/forms` → 201, estado "Publicado" |

**Estado esperado:** ✅ PASA

---

### CU-J02-03: Arquitecto crea iForm Maestro "Evaluación de Daños" (FORM-03)

**US:** US-003 | **CAs:** CA-47, CA-61, CA-41

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Arquitecto | Crea formulario con patrón 🔵 iForm Maestro | Designer activo |
| 2 | Arquitecto | Arrastra los 12 componentes del FORM-03 | Hidden (CA-47), GPS (CA-61), Data Grid (CA-41), File con constraints |
| 3 | Arquitecto | Configura Hidden Input `ID_PERITO` con isPrefilled=true | Token silencioso inyectado por MI |
| 4 | Arquitecto | Configura Data Grid "Ítems Dañados" con minRows=1, maxRows=20 | Grid repetible funcional |
| 5 | Arquitecto | Prueba Stage Simulator: INSPECTION → VALUATION | Transición de visibilidad correcta |
| 6 | Arquitecto | Guarda y publica FORM-03 | POST → 201, badge 🔵 MAESTRO |

**Estado esperado:** ✅ PASA  
**Criterio:** GPS, hidden token, field_array y file constraints configurados y funcionales.

---

### CU-J02-04: Arquitecto genera y valida esquema Zod (4 formularios)

**US:** US-003 | **CAs:** CA-06, CA-07

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Arquitecto | En FORM-01, navega a pestaña "Zod" en Monaco IDE | Esquema Zod auto-generado: `z.object({ numeroPoliza: z.string().regex(...).min(8).max(20), ... })` |
| 2 | Arquitecto | Abre QA Sandbox Fuzzer (CA-79) | Modal con payload editable |
| 3 | Arquitecto | Clic en "Autocompletar Happy" | Payload válido generado |
| 4 | Arquitecto | Ejecuta Fuzzer → resultado: "Payload Válido 🎉" | Validación Zod OK |
| 5 | Arquitecto | Modifica payload: borra `numeroPoliza` → ejecuta | Error Zod: `[NUMERO_POLIZA] - Required` |
| 6 | Arquitecto | Repite para FORM-02, 03, 04 | Todos los esquemas coherentes |

**Estado esperado:** ✅ PASA

---

## FASE 2: CREACIÓN DMN + MODELADO BPMN

> **Objetivo:** Crear la tabla DMN de cobertura y modelar/importar el proceso BPMN con vinculaciones.  
> **US:** US-005, US-007, US-028

---

### CU-J02-05: Arquitecto crea tabla DMN "Decide_Claim_Coverage"

**US:** US-007

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Arquitecto | Navega a `/admin/modeler/dmn` (DmnIntelligence) | Pantalla P15: lista de tablas DMN |
| 2 | Arquitecto | Crea nueva tabla: "Decide_Claim_Coverage" | Editor DMN abierto |
| 3 | Arquitecto | Configura inputs: `tipoSiniestro` (string), `montoEstimadoDano` (double) | Columnas de entrada |
| 4 | Arquitecto | Configura output: `claimDecision` (string: APPROVE/REVIEW/REJECT) | Columna de salida |
| 5 | Arquitecto | Crea reglas: | — |
| | | R1: tipoSiniestro="Robo" AND monto<5000 → APPROVE | Fast-track |
| | | R2: tipoSiniestro="Incendio" → REVIEW | Siempre revisión |
| | | R3: monto>500000 → REVIEW | Alto valor |
| | | R4: (default) → REJECT | Fuera de política |
| 6 | Arquitecto | Publica la tabla DMN | POST `/api/v1/dmn` → 201, estado Publicada |

**Estado esperado:** ✅ PASA  
**Criterio:** Tabla DMN con 2 inputs, 1 output, 4 reglas. Publicada y disponible para vincular.

---

### CU-J02-06: Arquitecto importa proceso BPMN desde archivo

**US:** US-028 | **CAs:** CA-01, CA-03

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Arquitecto | En el Modeler BPMN, clic en "⬆️ Importar" | Diálogo de selección de archivo |
| 2 | Arquitecto | Selecciona `insurance_claims_complex.bpmn` | — |
| 3 | Sistema | Parsea XML y renderiza el diagrama | 5 lanes, 22 elementos visibles, conexiones correctas |
| 4 | Sistema | Detecta `camunda:decisionRef="Decide_Claim_Coverage"` ya configurado | Si DMN existe → ✅ vinculado. Si no → ⚠️ Warning |
| 5 | Sistema | Pre-Flight ejecuta validación automática | Warnings esperados: User Tasks sin formKey (aún no vinculados) |

**Estado esperado:** ✅ PASA  
**Criterio:** El proceso complejo se renderiza con todos los elementos. Warnings de formKey esperados.

---

### CU-J02-07: Arquitecto vincula formularios a User Tasks vía `camunda:formKey`

**US:** US-005, US-003 | **CAs:** CA-30, CA-31

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Arquitecto | Selecciona `Task_ManualReview` → panel FormKey | Dropdown lista formularios publicados |
| 2 | Arquitecto | Selecciona "🔵 Auditoría de Siniestro" | `camunda:formKey="frm_auditoria_siniestro"` → indicador ✅ |
| 3 | Arquitecto | Selecciona `Task_Escalation` → panel FormKey | Dropdown activo |
| 4 | Arquitecto | Selecciona "🟢 Veredicto Escalamiento" | `camunda:formKey="frm_veredicto_escalamiento"` |
| 5 | Arquitecto | Selecciona `Task_DamageAssessment` → panel FormKey | Dropdown activo |
| 6 | Arquitecto | Selecciona "🔵 Evaluación de Daños Perito" | `camunda:formKey="frm_evaluacion_danos"` |
| 7 | Arquitecto | Selecciona `Task_DirectorApproval` (dentro del Sub-Process) → panel FormKey | Dropdown activo |
| 8 | Arquitecto | Selecciona "🟢 Firma Final Director" | `camunda:formKey="frm_firma_director"` |
| 9 | Sistema | Pre-Flight re-valida: todos los User Tasks tienen formKey | Badge "✅ Validado" |

**Estado esperado:** ✅ PASA  
**Criterio:** 4 User Tasks vinculados a 4 formularios distintos. Pre-Flight green.

---

### CU-J02-08: Arquitecto vincula DMN al BusinessRuleTask

**US:** US-005, US-007

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Arquitecto | Selecciona `BusinessRule_FraudCheck` | Panel de propiedades del BusinessRuleTask |
| 2 | Arquitecto | Verifica `camunda:decisionRef="Decide_Claim_Coverage"` | Ya viene del XML importado |
| 3 | Arquitecto | Verifica `camunda:resultVariable="claimDecision"` | Variable de resultado configurada |
| 4 | Arquitecto | Configura binding: `camunda:decisionRefBinding="latest"` | Siempre usar última versión DMN |

> [!NOTE]
> **Brecha B-20 (Sprint 6.1):** Actualmente el `decisionRef` se configura manualmente en el XML. En la Iteración 6.1 se implementará un dropdown visual análogo al de FormKey para seleccionar tablas DMN publicadas.

**Estado esperado:** ✅ PASA (manual) / ⚠️ UX B-20 pendiente

---

### CU-J02-09: Arquitecto exporta proceso BPMN y diagrama

**US:** US-028 | **CAs:** CA-01, CA-02

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Arquitecto | Clic en "⬇️ Exportar .bpmn" | Descarga XML con todos los formKeys y decisionRef |
| 2 | Arquitecto | Clic en "Exportar PNG" | Descarga imagen del diagrama |
| 3 | Arquitecto | Clic en "Exportar PDF" | Descarga PDF del diagrama |
| 4 | Arquitecto | Abre XML exportado con editor de texto | Verifica formKeys, decisionRef, SLA, lanes, sub-process |

**Estado esperado:** ✅ PASA

---

## FASE 3: DEPLOY + PRE-FLIGHT

> **Objetivo:** Desplegar el proceso complejo con validación Pre-Flight.  
> **US:** US-005

---

### CU-J02-10: Pre-Flight valida proceso complejo

**US:** US-005 | **CA:** CA-09

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Sistema | Pre-Flight ejecuta validación automática | Badge cambia de ⏳ → ✅ Validado |
| 2 | Sistema | Valida: ≥1 Start Event, ≥1 End Event, todos User Tasks con formKey | OK |
| 3 | Sistema | Valida: `decisionRef` resuelve contra DMN publicada | OK (si DMN existe) |
| 4 | Sistema | Valida: complejidad ≤ MAX_NODES (checkMaxNodes) | OK (22 nodos ≤ límite) |
| 5 | Sistema | Multi-Instance `Task_DamageAssessment` válido con `camunda:collection` | OK |

**Estado esperado:** ✅ PASA

---

### CU-J02-11: Release Manager despliega proceso

**US:** US-005 | **CAs:** CA-21, CA-65

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Arquitecto | Cambia mock role a "👑 Release Manager" | Botón "🚀 [VALIDAR Y DESPLEGAR]" visible |
| 2 | RM | Presiona desplegar | Modal: Nombre + Estrategia versionado |
| 3 | RM | Justificación: "Despliegue proceso siniestros v1 E2E" (≥10 chars) | Campo validado |
| 4 | RM | Confirma | POST `/api/v1/deployments` → 201 Created |
| 5 | Sistema | Toast: "Proceso desplegado v1 ✓" | Estado ACTIVO, badge SANDBOX desaparece |
| 6 | Motor Camunda | Proceso registrado en engine | GET `/api/v1/engine-rest/process-definition` incluye `Process_InsuranceClaim` |

**Estado esperado:** ✅ PASA

---

## FASE 4: EJECUCIÓN E2E — 4 FLUJOS

> **Objetivo:** Ejecutar 4 instancias del proceso con desenlaces distintos.  
> **US:** US-001, US-002, US-029

---

### FLUJO 1: 🟢 Happy Path — Liquidación exitosa completa

> **Ruta:** Start → DMN(REVIEW) → Auditoría N1 → Parallel[Docs + Peritos×2] → Sub[Script+Director] → ReserveFunds → Payment → ✅ EndEvent_ClaimClosed

#### CU-J02-F1-01: Iniciar caso de siniestro (Happy Path)

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Arquitecto | En Gestor de Instancias → "Crear Nuevo Caso" | Formulario inicial se abre |
| 2 | Arquitecto | Llena: tipoSiniestro="Incendio", montoEstimadoDano=150000, nombreAsegurado="Carlos E2E" | Variables de proceso |
| 3 | Arquitecto | Presiona "Iniciar" | POST `.../process-definition/Process_InsuranceClaim/start` |
| 4 | Motor | DMN ejecuta `Decide_Claim_Coverage` con input Incendio+150000 | R2: → `claimDecision = 'REVIEW'` |
| 5 | Motor | Gateway desvía a `Task_ManualReview` | Tarea en cola de Adjusters |

**Estado esperado:** ✅ PASA

#### CU-J02-F1-02: Analista N1 completa auditoría

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Analista N1 | En Workdesk, reclama `Task_ManualReview` | POST `/tasks/{id}/claim` → 200 |
| 2 | Sistema | Carga iForm Maestro "Auditoría de Siniestro" | 16 componentes, Stage INTAKE visible |
| 3 | Analista | Llena datos INTAKE: póliza="CO-12345678", nombre, email, fecha, tipo | Validaciones Zod OK |
| 4 | Analista | Avanza a Stage ANALYSIS: monto=150000 (currency), zona="Urbana", descripción(≥20chars) | Campos INTAKE en solo-lectura (P-19) |
| 5 | Analista | Sube 2 fotos de evidencia (.jpg, 3MB cada una) | File upload OK (≤10MB, ≤5 archivos) |
| 6 | Analista | Marca "¿Requiere Peritaje?" = "Sí" | Multi-select de peritos aparece (P-13 visibilidad) |
| 7 | Analista | Selecciona `perito_a` y `perito_b` en listado | Multi-select chips (P-24) |
| 8 | Analista | Stage DECISION: selecciona "Aprobar Investigación Completa" | P-16: audit log registra decisión |
| 9 | Analista | Presiona "Completar Auditoría" | Validación Zod client+server → POST `/tasks/{id}/complete` |
| 10 | Motor | Gateway Parallel abre 2 rutas: Message Catch + Multi-Instance Peritos | 2 tareas perito creadas + 1 espera mensaje |

**Estado esperado:** ✅ PASA

#### CU-J02-F1-03: Perito A y Perito B evalúan daños (Multi-Instance)

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Perito A | Login → Workdesk → reclama su instancia de `Task_DamageAssessment` | iForm Maestro "Evaluación de Daños" carga para perito_a |
| 2 | Sistema | Hidden token `ID_PERITO` = "perito_a" (auto-inyectado) | CA-47: invisible al usuario |
| 3 | Perito A | Captura GPS → Tipo daño="Estructural" → Monto=$120,000 | GPS (CA-61), currency (P-09) |
| 4 | Perito A | Sube 3 fotos peritaje (.jpg, ≤15MB) | File constraints OK |
| 5 | Perito A | Data Grid: agrega 3 ítems dañados con costos | field_array con minRows=1(OK) |
| 6 | Perito A | Observaciones (≥30 chars) + Recomendación="Proceder con Liquidación" | Validación OK |
| 7 | Perito A | Presiona "Enviar Evaluación" | Completar tarea → instancia MI parcial completada |
| 8 | Perito B | Login (sesión separada) → repite pasos 1-7 con datos distintos | Monto=$130,000, Recomendación="Proceder" |
| 9 | Motor | Ambas instancias MI completadas → token disponible en parallel join | Espera merge con Message Catch |

**Estado esperado:** ✅ PASA  
**Criterio:** 2 sesiones de usuario simultáneas, cada perito con su evaluación independiente. Multi-instance completado.

#### CU-J02-F1-04: Enviar mensaje policial + Parallel Join

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Arquitecto | En consola de pruebas Admin → envía `Msg_PoliceReport` al proceso | POST `/api/v1/engine-rest/message` con `messageName="Msg_PoliceReport"` |
| 2 | Motor | `Event_WaitForPoliceReport` resuelto | Token Message disponible |
| 3 | Motor | `Gateway_Parallel_Join` converge (peritos + mensaje) | Avanza a Sub-Process |

**Estado esperado:** ✅ PASA

#### CU-J02-F1-05: Sub-Process — Script + Firma Director

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Motor | `SubProcess_Investigation` inicia | Start_Sub → Script Task |
| 2 | Motor | `Task_CalculateFormula` ejecuta JS: `pagoFinal = 10000` | Variable de proceso actualizada |
| 3 | Director | Workdesk → reclama `Task_DirectorApproval` | Formulario "Firma Final Director" carga |
| 4 | Sistema | Campos prefilled: póliza (disabled), pagoFinal=$10,000 (disabled, currency) | P-20 prefill + P-15 disabled |
| 5 | Director | Escribe resumen ejecutivo (≥10 chars) | Validación OK |
| 6 | Director | Decisión="Aprobar Liquidación" + Firma Digital | CA-31 canvas, P-16 audit |
| 7 | Director | Presiona "Firmar y Autorizar" | POST complete → Sub-process termina |

**Estado esperado:** ✅ PASA

#### CU-J02-F1-06: Reserva de Fondos + Pago + Cierre

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Motor | `Task_ReserveFunds` activa External Worker topic `reserve-funds` | Mock Worker responde OK |
| 2 | Motor | `CallActivity_Payment` invoca `Process_Payment` | Mock Worker (o stub) responde OK |
| 3 | Motor | `EndEvent_ClaimClosed` alcanzado | **Instancia COMPLETED** |
| 4 | Arquitecto | Verifica en Dashboard BAM: estado "Cerrado ✅" | Métricas actualizadas |
| 5 | Arquitecto | GET `/api/v1/engine-rest/history/process-instance` | Estado = COMPLETED |

**Estado esperado:** ✅ PASA  
**Criterio:** Flujo completo de punta a punta: 4 User Tasks + 2 Service Tasks + 1 DMN + 1 Sub-Process completados.

---

### FLUJO 2: 🔴 Rechazo automático por DMN

> **Ruta:** Start → DMN(REJECT) → ❌ EndEvent_AutoRejected (Terminate)

#### CU-J02-F2-01: DMN rechaza siniestro fuera de política

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Arquitecto | Crea caso: tipoSiniestro="Otro", montoEstimadoDano=500 | Variables de proceso |
| 2 | Motor | DMN evalúa: regla R4 default → `claimDecision = 'REJECT'` | Gateway desvía a EndEvent_AutoRejected |
| 3 | Motor | `EndEvent_AutoRejected` (Terminate) alcanzado | **Instancia TERMINATED** |
| 4 | Sistema | NO se crean User Tasks | Verificar: 0 tareas en Workdesk |
| 5 | Arquitecto | History: Start → DMN → Gateway → End (3 nodos) | Flujo rápido sin intervención humana |

**Estado esperado:** ✅ PASA  
**Criterio:** DMN ejecuta en <1s, no se generan tareas humanas, proceso termina inmediatamente.

---

### FLUJO 3: ⏱ Timeout + Escalamiento Legal

> **Ruta:** Start → DMN(REVIEW) → Auditoría N1 ⏱(72h timeout simulado) → Escalamiento Supervisor → 🔶 End_Escalation

#### CU-J02-F3-01: Timeout de auditoría escala a supervisor

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Arquitecto | Crea caso: tipoSiniestro="Incendio", montoEstimadoDano=200000 | DMN → REVIEW → Task_ManualReview creada |
| 2 | Sistema | **Simular timeout:** modificar timer PT72H → PT5S (o inyectar job modification) | Timer boundary se dispara |
| 3 | Motor | `Event_ReviewTimeout` cancela `Task_ManualReview` | Tarea desaparece del Workdesk del Analista |
| 4 | Motor | `Task_Escalation` creada en grupo `Supervisors` | Tarea visible para Supervisor |
| 5 | Supervisor | Workdesk → reclama "Veredicto de Supervisor Vencido" | Formulario Simple FORM-02 carga |
| 6 | Sistema | Campo póliza prefilled + disabled | P-20 + P-15 OK |
| 7 | Supervisor | Motivo vencimiento (≥15 chars) + Acción="Derivar a Legal Externo" + Firma | Validación OK |
| 8 | Supervisor | Presiona "Confirmar Veredicto" | POST complete |
| 9 | Motor | `End_Escalation` alcanzado | **Instancia COMPLETED** (ruta escalamiento) |

**Estado esperado:** ✅ PASA  
**Criterio:** Boundary timer cancela tarea automáticamente. Formulario prefilled funcional. Ruta de escalamiento completa.

---

### FLUJO 4: 💥 Error de pago + Compensación financiera

> **Ruta:** Start → DMN(APPROVE fast-track) → ReserveFunds → Payment ERROR → Compensation(Rollback) → 🔴 EndEvent_Error

#### CU-J02-F4-01: Fast-track con error de pago y compensación

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Arquitecto | Crea caso: tipoSiniestro="Robo", montoEstimadoDano=3000 | DMN R1: Robo+<5000 → APPROVE |
| 2 | Motor | Gateway Fast-Track → `Task_ReserveFunds` directamente | Skip de auditoría humana |
| 3 | Motor | External Worker `reserve-funds` responde OK | Fondos bloqueados |
| 4 | Motor | `CallActivity_Payment` inicia | External Worker invocado |
| 5 | Mock Worker | **Simula error:** responde con `bpmnError("ERP_PAYMENT_EXCEPTION")` | Error boundary captura |
| 6 | Motor | `Event_PaymentError` dispara → `Event_ThrowCompensate` | Compensation throw ejecutado |
| 7 | Motor | `Task_CancelReserve` (isForCompensation=true) ejecuta rollback | Mock Worker `rollback-funds` responde OK |
| 8 | Motor | `EndEvent_Error` alcanzado | **Instancia COMPLETED** (ruta error) |
| 9 | Arquitecto | History: Start → DMN → GW → ReserveFunds → Payment(ERROR) → Compensate → Rollback → EndError | 8 nodos trazables |

**Estado esperado:** ✅ PASA  
**Criterio:** Compensación financiera ejecutada correctamente. Rollback verifiable en historia.

---

## FASE 5: FORMULARIO GENÉRICO EN KANBAN

> **Objetivo:** Validar `sys_generic_form` como fallback automático en actividad Kanban.  
> **US:** US-039, US-008

---

### CU-J02-K01: Crear actividad en Kanban sin formulario diseñado

**US:** US-008, US-039

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Operario | En tablero Kanban ágil → crea tarea manual: "Verificar documentos complementarios" | Tarea creada sin formKey |
| 2 | Operario | Abre la tarea | Sistema carga `sys_generic_form` automáticamente |
| 3 | Sistema | Renderiza: MetadataGrid + Resultado Gestión (select) + Observaciones (textarea) + Adjuntos | Formulario genérico funcional |
| 4 | Operario | Llena: Resultado="Aprobar", Observaciones="Documentación completa verificada OK" (≥10 chars) | Validación Zod OK |
| 5 | Operario | Presiona "✅ Aprobar" en PanicButtonBar | POST `/generic-form-complete` |
| 6 | Sistema | Tarea completada | Estado Kanban avanza |

**Estado esperado:** ✅ PASA  
**Criterio:** El formulario genérico funciona como fallback robusto para tareas sin diseño.

---

## FASE 6: OBSERVABILIDAD

> **Objetivo:** Verificar trazabilidad de los 4 flujos ejecutados.  
> **US:** US-001, US-005

---

### CU-J02-OBS-01: Dashboard BAM muestra los 4 flujos

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Arquitecto | Navega al Dashboard BAM | 4 instancias visibles |
| 2 | Sistema | Flujo 1: COMPLETED (Happy Path) | ✅ |
| 3 | Sistema | Flujo 2: TERMINATED (Rechazo DMN) | ✅ |
| 4 | Sistema | Flujo 3: COMPLETED (Escalamiento) | ✅ |
| 5 | Sistema | Flujo 4: COMPLETED (Error+Compensación) | ✅ |

**Estado esperado:** ✅ PASA

### CU-J02-OBS-02: Historial del motor completo

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Arquitecto | GET `/api/v1/engine-rest/history/process-instance` | 4 instancias con estados correctos |
| 2 | Arquitecto | Para F1: verifica activity-instance history | Start→DMN→GW→Audit→Parallel→[Msg+MI×2]→Join→Sub→Script→Director→Reserve→Pay→End |

**Estado esperado:** ✅ PASA

### CU-J02-OBS-03: Audit Log del Modeler

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Arquitecto | Abre proceso en Modeler → "📝 Auditoría" | Panel Audit Log |
| 2 | Sistema | Registros: IMPORTED → MODIFIED (formKeys) → DEPLOYED v1 | Tabla con acciones, usuario, fecha |

**Estado esperado:** ✅ PASA

---

## FASE 7: CERTIFICACIÓN OPERATIVA — WORKDESK, CLAIM & KANBAN

> **Objetivo:** Certificar las funcionalidades operativas diarias: bandeja de tareas (US-001), reclamo/liberación (US-002) y tablero ágil (US-008), utilizadas como vehículo transitorio en Fases 1-6 pero sin certificación propia.
> **US:** US-001, US-002, US-008
> **Precondiciones adicionales:**

| # | Precondición | Verificación |
|---|-------------|-------------|
| PRE-10 | Tablero Kanban ágil "QA Sprint E2E" con columnas: TODO, DOING, REVIEW, BLOCKED, DONE | Seed data |
| PRE-11 | 3 tarjetas Kanban pre-cargadas en columna TODO | Seed data |
| PRE-12 | 2 usuarios operarios en mismo equipo: `operario_a` y `operario_b` (grupo `Adjusters`) | Seed data |
| PRE-13 | 1 usuario supervisor: `supervisor_e2e` (grupo `Directors`, mismo `team_id` que operarios) | Seed data |
| PRE-14 | ≥20 tareas BPMN+Kanban mixtas en Cola del Equipo de `Adjusters` | Generadas por Fases 1-4 + seed |

---

### FASE 7A: WORKDESK — Grilla Unificada y SLA Vivo (US-001)

---

#### CU-J02-W01: Carga inicial con paginación server-side

**US:** US-001 | **CAs:** CA-10, CA-19, CA-20

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Operario A | Navega al Workdesk (`/workdesk`) | Grilla con Skeleton loader (no spinner) |
| 2 | Sistema | GET `/api/v1/workdesk/tasks?page=1&size=15&sort=sla_asc` | Response con `pagination: { page:1, size:15, total_records, total_pages }` |
| 3 | Sistema | Máximo 15 tarjetas renderizadas (CA-09) | Botonera paginación sticky arriba y abajo |
| 4 | Sistema | Ordenamiento SLA ascendente forzoso (CA-01) | Tarea con menor tiempo restante en posición 1 |
| 5 | Operario A | Navega a página 2 | GET con `page=2` → Nuevas 15 tarjetas |

**Estado esperado:** ✅ PASA
**Criterio:** Paginación server-side estricta. Sin filtrado client-side. 15 tarjetas/página.

---

#### CU-J02-W02: Búsqueda server-side con debounce

**US:** US-001 | **CAs:** CA-19, CA-20

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Operario A | Escribe "EXP-90" en barra de búsqueda | Debounce 300ms antes de emitir request |
| 2 | Sistema | GET `/api/v1/workdesk/tasks?page=1&size=15&search=EXP-90` | Resultados filtrados server-side (`pg_trgm`) |
| 3 | Sistema | Paginación reiniciada a página 1 | Consistencia con CA-22 |
| 4 | Operario A | Borra búsqueda | Grilla vuelve al estado completo paginado |

**Estado esperado:** ✅ PASA

---

#### CU-J02-W03: Filtros facetados con contadores

**US:** US-001 | **CAs:** CA-22, CA-29

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Sistema | Carga inicial Workdesk | Filtros: `[Todos (N)]` / `[⚡ BPMN (X)]` / `[📋 Kanban (Y)]` |
| 2 | Operario A | Selecciona "⚡ BPMN" | GET con `origin=BPMN` → Solo tareas de proceso |
| 3 | Sistema | Chip removible "⚡ BPMN" sobre la grilla | Feedback visual |
| 4 | Operario A | Adiciona filtro "🔴 Vencida" | GET con `origin=BPMN&status=OVERDUE` → Intersección |
| 5 | Sistema | Contadores: `facets: { origin: {...}, status: {...} }` | Números reflejan total real |
| 6 | Operario A | Remueve chip "⚡ BPMN" | Filtro revertido, paginación reinicia a p.1 |

**Estado esperado:** ✅ PASA

---

#### CU-J02-W04: Semáforo SLA vivo — Ticking Engine

**US:** US-001 | **CAs:** CA-05, CA-11, CA-24

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Sistema | Renderiza 15 tareas con SLA distintos | Semáforos: 🟢(>50%) 🟡(15-50%) 🔴(<15%) ⚫(vencida) |
| 2 | Sistema | Iconografía SVG acompañando colores (CA-11) | ✔️ Verde, ⏳ Amarillo, ⚡ Rojo (accesibilidad daltónicos) |
| 3 | Sistema | UN solo Global Heartbeat Store (rAF) | Prohibido: múltiples `setInterval` por tarjeta |
| 4 | Operario A | Espera 30s observando | Temporizadores se actualizan en vivo sin F5 |
| 5 | Sistema | Tarea próxima a vencer transiciona 🟡→🔴 | Cambio de color automático sin recarga |

**Estado esperado:** ✅ PASA
**Criterio:** Zero `setInterval` por tarjeta. Global Heartbeat. Transiciones automáticas.

---

#### CU-J02-W05: Recálculo SLA tras pestaña inactiva

**US:** US-001 | **CAs:** CA-25, CA-31

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Operario A | Workdesk con tareas en semáforo 🟡 | Estado inicial |
| 2 | Operario A | Cambia a otra pestaña por 6 minutos | rAF se pausa |
| 3 | Operario A | Regresa a pestaña Workdesk | `visibilitychange` detectado |
| 4 | Sistema | Recálculo INMEDIATO semáforos con `Date.now()` (CA-25) | 🟡→🔴 si venció durante inactividad |
| 5 | Sistema | Auto-refresco silencioso (CA-31, >5min) | Shimmer sutil → datos renovados sin destruir KeepAlive ni filtros |

**Estado esperado:** ✅ PASA

---

#### CU-J02-W06: Consolidación BPMN + Kanban en grilla unificada

**US:** US-001 | **CAs:** CA-03, CA-17, CA-23

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Sistema | Grilla carga | 5 columnas: Nombre, SLA, Estado, Avance, Recurso |
| 2 | Sistema | Tareas BPMN (insurance_claims) | Badge ⚡ izquierda del nombre |
| 3 | Sistema | Tareas Kanban (tablero ágil) | Badge 📋 izquierda del nombre |
| 4 | Sistema | Avance BPMN (CA-23) | `(Índice UserTask / Total UserTasks) × 100` → barra progreso |
| 5 | Sistema | Avance Kanban (CA-23) | `(Índice columna / Total columnas) × 100` |
| 6 | Sistema | Tareas sin `dueDate` | "SLA Infinito" → fondo del grid (NULLS LAST) |

**Estado esperado:** ✅ PASA

---

#### CU-J02-W07: KeepAlive y navegación de retorno

**US:** US-001 | **CAs:** CA-12

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Operario A | Configura filtros + scroll hasta fila 10 | Estado personalizado |
| 2 | Operario A | Abre formulario de una tarea | Sale del Workdesk |
| 3 | Operario A | Presiona "Atrás" | `<keep-alive>` restaura filtros, scroll, página |
| 4 | Sistema | Carga en 0ms sin petición al backend | KeepAlive funcional |
| 5 | Sistema | Si página queda vacía (todas resueltas) | Redirigir a Página 1 (Last Page Empty) |

**Estado esperado:** ✅ PASA

---

#### CU-J02-W08: WebSocket — Desaparición + relleno automático

**US:** US-001 | **CAs:** CA-06, CA-13, CA-26, CA-27

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Operario A | Workdesk con 15 tareas en Cola del Equipo | Página completa |
| 2 | Operario B | Reclama tarea TK-123 desde otra sesión | POST claim → 200 |
| 3 | Sistema | WS: `{ action: 'REMOVE', taskId: 'TK-123' }` | Operario A recibe |
| 4 | Workdesk A | Animación CSS `opacity: 0` (CA-13) | Sin salto abrupto de fila |
| 5 | Workdesk A | Toast: "Tarea reclamada por otro equipo" | Sin revelar identidad (privacidad) |
| 6 | Sistema | Ventana 5s → petición silenciosa relleno (CA-26) | Página vuelve a 15 tarjetas (fade-in sutil) |

**Estado esperado:** ✅ PASA

---

#### CU-J02-W09: Delegación segura — Toggle de vista

**US:** US-001 | **CAs:** CA-04, CA-15

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Supervisor | Activa Toggle "Ver tareas de [Operario A]" | GET `/api/v1/workdesk/tasks/{operarioA_id}` |
| 2 | Sistema | Banner: "Estás viendo el escritorio de [Operario A]" | Mitigación errores |
| 3 | Sistema | Validación RBAC: supervisor es superior jerárquico | 200 OK |
| 4 | Supervisor | Desactiva toggle | Vuelve a sus propias tareas |

**Estado esperado:** ✅ PASA

---

#### CU-J02-W10: Anti Cherry-Picking — "Atender Siguiente"

**US:** US-001 | **CAs:** CA-08, CA-16, CA-21, CA-28

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Admin | Activa Feature Toggle "Enrutamiento Forzoso" | Huella en Audit Log |
| 2 | Operario A | Workdesk cambia: oculta grilla selectiva | CTA gigante: `[Atender Siguiente Tarea]` |
| 3 | Operario A | Presiona "Atender Siguiente" | Backend cruza task antigua vs skills operario |
| 4 | Sistema | `SELECT FOR UPDATE SKIP LOCKED` (CA-28) | Asignación atómica |
| 5 | Sistema | Tarea asignada + formulario abierto | Sin opción de elegir otra |
| 6 | Operario A | Skip → Dropdown motivos (CA-21) | "Cliente no responde" etc. → Audit Log |

**Estado esperado:** ✅ PASA

---

### FASE 7B: CLAIM TASK — Reclamo, Liberación y Despojo (US-002)

> **Precondiciones:** ≥5 tareas en Cola del Equipo de `Adjusters`. `operario_a` y `operario_b` con sesiones simultáneas.

---

#### CU-J02-C01: Reclamo individual con asignación

**US:** US-002 | **CAs:** CA-01, CA-14

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Operario A | En "Cola del Equipo", visualiza TK-200 | Botones: [Explorar] [Reclamar] |
| 2 | Operario A | Presiona [Reclamar] | POST `/api/v1/tasks/TK-200/claim` |
| 3 | Sistema | 200: `{ taskId, assignee: 'operario_a', claimedAt }` | Tarea migra a "Mi Bandeja" |
| 4 | Workdesk B | WS `REMOVE` → TK-200 desaparece de cola de B | Sincronización instantánea |

**Estado esperado:** ✅ PASA

---

#### CU-J02-C02: Concurrencia optimista — 2 usuarios simultáneos

**US:** US-002 | **CAs:** CA-01, CA-11

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Ambos | A y B visualizan TK-201 | Botón [Reclamar] visible para ambos |
| 2 | Simultáneo | Ambos clic [Reclamar] en el mismo segundo | 2 POST concurrentes |
| 3 | Sistema | `SELECT FOR UPDATE SKIP LOCKED` | Solo 1 gana atómicamente |
| 4 | Ganador | 200: tarea asignada | En "Mi Bandeja" |
| 5 | Perdedor | 409: Modal "Lo sentimos, [Nombre] se te adelantó" | Sin stacktrace |

**Estado esperado:** ✅ PASA
**Criterio:** Exclusión mutua por BD. Zero duplicados. Modal amable.

---

#### CU-J02-C03: Reclamo masivo en lote (Bulk Claim)

**US:** US-002 | **CAs:** CA-02, CA-14, CA-23

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Operario A | Selecciona 5 checkboxes en Cola | [Reclamar Seleccionadas] activo |
| 2 | Operario A | Presiona [Reclamar Seleccionadas] | POST `/api/v1/tasks/bulk-claim` body: `{ taskIds: [...5] }` |
| 3 | Sistema | Batch atómico | `{ claimed: 4, conflicts: [{ taskId, reason }] }` |
| 4 | Sistema | WS `BULK_REMOVE` (CA-23) | 1 mensaje → desvanecimiento escalonado 150ms |
| 5 | Operario A | Resumen visual | "4 reclamadas, 1 ya no disponible" |

**Estado esperado:** ✅ PASA

---

#### CU-J02-C04: Exploración solo lectura + aviso de reclamo externo

**US:** US-002 | **CAs:** CA-05, CA-18

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Operario A | Doble clic en TK-202 de la Cola | Formulario en Modo Solo Lectura |
| 2 | Sistema | `assignee` NO se altera en BD | [Reclamar] visible dentro del formulario |
| 3 | Operario B | Reclama TK-202 durante exploración de A | WS `REMOVE` alcanza a A |
| 4 | Workdesk A | Banner: "⚠️ Tarea reclamada por otro compañero" | [Reclamar] se deshabilita (gris + candado) |
| 5 | Operario A | Puede seguir leyendo pero no actuar | Al cerrar, tarea ya no en Cola |

**Estado esperado:** ✅ PASA

---

#### CU-J02-C05: Liberación + Amnesia Transaccional + Nota interna

**US:** US-002 | **CAs:** CA-04, CA-07, CA-16, CA-17

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Operario A | TK-203 reclamada, formulario parcialmente llenado | Borrador LocalStorage + 1 archivo subido |
| 2 | Operario A | Presiona [Liberar Tarea] | Modal: "Perderá los datos no enviados" |
| 3 | Operario A | Confirma + mensaje: "@Pedro, revisalo" | POST `/tasks/TK-203/release` body: `{ message }` |
| 4 | Sistema | Purga LocalStorage + archivos `orphaned` | Amnesia total (CA-07, CA-17) |
| 5 | Sistema | WS `ADD` → TK-203 reaparece en Cola | Disponible para reclamar |
| 6 | Operario B | Reclama TK-203 y abre formulario | Formulario 100% en blanco |
| 7 | Sistema | Banner: "📝 Nota: @Pedro — [Op.A, hace X min]" (CA-16) | Visible hasta 1ra acción de B |

**Estado esperado:** ✅ PASA

---

#### CU-J02-C06: Despojo forzoso por supervisor

**US:** US-002 | **CAs:** CA-08, CA-13

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Supervisor | Vista monitoreo → TK-204 asignada a Op. A | [Forced Unclaim] visible |
| 2 | Supervisor | Ejecuta Forced Unclaim con motivo | POST `/tasks/TK-204/force-unclaim` |
| 3 | Sistema | Valida `team_id` supervisor = `team_id` tarea | 200 OK |
| 4 | Sistema | WS `REMOVE` bandeja de A + WS `ADD` Cola grupal | Despojo bidireccional |
| 5 | Sistema | Audit Log: `{ action: 'FORCE_UNCLAIM', result: 'SUCCESS' }` | Inmutable |

**Estado esperado:** ✅ PASA

---

#### CU-J02-C07: Trazabilidad forense pop-up

**US:** US-002 | **CAs:** CA-09, CA-20

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Operario A | Clic "Ver Trazabilidad" de TK-200 | Pop-Up timeline vertical |
| 2 | Sistema | GET `/api/v1/tasks/TK-200/audit-trail` | Historial rotación `assignee` |
| 3 | Sistema | Tipos: 🟢 CLAIMED, 🔵 RELEASED, 🟠 FORCE_UNCLAIMED, 🔴 AUTO_UNCLAIMED | Íconos color + timestamps |

**Estado esperado:** ✅ PASA

---

#### CU-J02-C08: Separación visual Cola vs Bandeja personal

**US:** US-002 | **CAs:** CA-22

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Sistema | Workdesk carga | 2 tabs: "Mi Bandeja ([N])" / "Cola del Equipo ([M])" |
| 2 | Operario A | Tab "Mi Bandeja" | Botones: [Abrir] [Liberar]. NO [Reclamar] |
| 3 | Operario A | Tab "Cola del Equipo" | Botones: [Explorar] [Reclamar]. Checkboxes Bulk |
| 4 | Operario A | Reclama 1 tarea | N+1, M-1 en tiempo real |
| 5 | Sistema | Tab activa preservada en KeepAlive | Consistente con US-001 CA-12 |

**Estado esperado:** ✅ PASA

---

### FASE 7C: KANBAN ÁGIL — Tablero, CRUD y Drag & Drop (US-008)

> **Proceso:** Tablero ágil independiente "QA Sprint E2E" (no vinculado al proceso BPMN de siniestros).

---

#### CU-J02-A01: Crear tablero ágil con columnas

**US:** US-008 | **CAs:** CA-05, CA-08

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | SM | Pantalla 10 → "Nuevo Proyecto Ágil" | Pop-Up: "Iniciar vacío" / "Usar Plantilla WBS" |
| 2 | SM | Selecciona "Iniciar vacío" | Hub con tablero sin tarjetas |
| 3 | SM | Columnas default: TODO, DOING, DONE | 3 columnas base |
| 4 | SM | Añade: REVIEW, BLOCKED | Total 5 ≤ 7 (hard-limit CA-08) |
| 5 | Sistema | Valida rol `Scrum_Master` o `Lider_Proyecto` | Solo estos roles |

**Estado esperado:** ✅ PASA

---

#### CU-J02-A02: CRUD de tarjetas Kanban

**US:** US-008, US-030 | **CAs:** CA-03, CA-04 (US-030)

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | SM | "+ Nueva Tarea" en barra superior | Slide-Panel con campos |
| 2 | SM | Título, Descripción(rich text), Esfuerzo=4h, Responsable=`operario_a` | Validados |
| 3 | SM | Guarda | POST → Tarjeta en TODO, visible en P3 |
| 4 | SM | Crea 2 tarjetas más | Total 3 en TODO |
| 5 | SM | Edita título existente | PUT → Actualización inmediata |
| 6 | SM | Elimina tarjeta con confirmación | Hard-Delete + Audit Log previo |

**Estado esperado:** ✅ PASA

---

#### CU-J02-A03: Drag & Drop entre columnas + propagación WebSocket

**US:** US-008 | **CAs:** CA-12, CA-06

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Operario A | Arrastra KT-001 de TODO → DOING | Drag visual con placeholder |
| 2 | Sistema | PATCH `.../kanban/tasks/KT-001/status` → `{ new_status: 'DOING' }` | 200: `{ status: 'DOING', version: 2 }` |
| 3 | Sistema | State Machine valida TODO→DOING (CA-06) | Transición permitida |
| 4 | Sistema | WS propaga a todos los conectados (CA-12) | Op. B ve KT-001 en DOING |
| 5 | Sistema | `last_modified` actualizado | Timestamp correcto |

**Estado esperado:** ✅ PASA
**Criterio:** Sub-segundo. Sin overhead BPMN. Propagación WS confirmada.

---

#### CU-J02-A04: Motivo obligatorio al mover a BLOCKED

**US:** US-008 | **CAs:** CA-01

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Operario A | Arrastra KT-001 de DOING → BLOCKED | Modal: "Motivo del Bloqueo" |
| 2 | Operario A | Escribe motivo (≥10 chars), confirma | PATCH status + motivo persistido |
| 3 | Sistema | SLA NO se congela (CA-01) | Reloj sigue corriendo |
| 4 | Sistema | Badge "🔴 BLOCKED" con tooltip motivo | Visible al hover |

**Estado esperado:** ✅ PASA

---

#### CU-J02-A05: Time Tracking Play/Stop por columna

**US:** US-008 | **CAs:** CA-03, CA-09

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Sistema | KT-001 en TODO | Timer oculto y bloqueado |
| 2 | Operario A | Arrastra a DOING | Timer habilitado: [▶ Start] visible |
| 3 | Operario A | [▶ Start] → 5 min → [⏹ Stop] | Tiempo acumulado: 5:00 |
| 4 | Operario A | Arrastra a BLOCKED | Timer sigue disponible |
| 5 | Operario A | Arrastra a DONE | Timer se bloquea definitivamente |
| 6 | Sistema | Log en `ibpms_time_logs` ref_type=`TASK_AGILE` | Asiento inmutable (CA-09) |

**Estado esperado:** ✅ PASA

---

#### CU-J02-A06: Inmutabilidad en DONE + Single-Assignee

**US:** US-008 | **CAs:** CA-02, CA-04

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Sistema | KT-002 en DONE | Badge ✅ |
| 2 | Operario A | Abre detalle | Solo Lectura absoluto, botones ocultos |
| 3 | Sistema | POST actualización → Backend rechaza | Inmutabilidad post-DONE (CA-02) |
| 4 | SM | Intenta asignar 2 usuarios a KT-003 | Error: política 1:1 (CA-04) |

**Estado esperado:** ✅ PASA

---

#### CU-J02-A07: Formulario genérico en tarea sin iForm

**US:** US-008, US-039 | **CAs:** CA-01/02 (US-039)

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Operario A | Abre KT-003 (sin formKey) | `sys_generic_form` carga |
| 2 | Sistema | MetadataGrid + Resultado + Observaciones + Adjuntos | Genérico funcional |
| 3 | Operario A | Resultado="Aprobar", Obs(≥10 chars) | Zod OK |
| 4 | Operario A | "✅ Aprobar" | Tarea avanza en Kanban |

**Estado esperado:** ✅ PASA

---

## Escenarios Negativos

---

### CU-J02-NEG-01: Guardar formulario Maestro sin campos

**US:** US-003

| Paso | Resultado Esperado |
|:----:|-------------------|
| 1 | Arquitecto intenta guardar FORM-01 con canvas vacío |
| 2 | Validación: "El formulario debe tener al menos 1 campo" |
| 3 | El formulario no se guarda |

**Estado esperado:** ✅ PASA

---

### CU-J02-NEG-02: Perito intenta completar evaluación con datos inválidos

**US:** US-029

| Paso | Resultado Esperado |
|:----:|-------------------|
| 1 | Perito A deja observaciones vacías (<30 chars) y 0 fotos |
| 2 | Presiona "Enviar Evaluación" |
| 3 | Validación Zod falla: `observacionesPerito: min 30 chars`, `fotosPeritaje: minFiles 2` |
| 4 | Errores inline visibles, botón bloqueado |

**Estado esperado:** ✅ PASA

---

### CU-J02-NEG-03: Desplegar proceso sin vinculación formKey

**US:** US-005

| Paso | Resultado Esperado |
|:----:|-------------------|
| 1 | Arquitecto importa BPMN sin vincular formKeys |
| 2 | Pre-Flight: Badge "⚠️ Advertencias" → "User Tasks sin FormKey" |
| 3 | Si RM intenta desplegar, se permite con warning (no hard-stop) |

**Estado esperado:** ✅ PASA

---

### CU-J02-NEG-04: Designer intenta desplegar (sin rol RM)

**US:** US-005

| Paso | Resultado Esperado |
|:----:|-------------------|
| 1 | Usuario con rol `DESIGNER` presiona "Solicitar Despliegue" |
| 2 | Botón "🚀 Desplegar" NO visible |
| 3 | POST directo: HTTP 403 Forbidden |

**Estado esperado:** ✅ PASA

---

### CU-J02-NEG-05: Import BPMN con decisionRef huérfano

**US:** US-028, US-007

| Paso | Resultado Esperado |
|:----:|-------------------|
| 1 | Importar BPMN que referencia `decisionRef="DMN_INEXISTENTE"` |
| 2 | Diagrama se renderiza correctamente |
| 3 | Pre-Flight: ⚠️ "DecisionRef 'DMN_INEXISTENTE' no encontrado" |

**Estado esperado:** ✅ PASA

---

### CU-J02-NEG-06: Formulario Genérico con observaciones inválidas

**US:** US-039

| Paso | Resultado Esperado |
|:----:|-------------------|
| 1 | Operario escribe "ok" (2 chars) en Observaciones del Genérico |
| 2 | Validación Zod: `z.string().min(10)` falla |
| 3 | Error inline visible, botón deshabilitado |

**Estado esperado:** ✅ PASA

---

### CU-J02-NEG-07: Director rechaza liquidación

**US:** US-029

| Paso | Resultado Esperado |
|:----:|-------------------|
| 1 | Director selecciona "Rechazar y Devolver" + firma |
| 2 | Presiona "Rechazar" |
| 3 | Variable `decisionDirector = 'Rechazar y Devolver'` persiste |
| 4 | El sub-process termina (comportamiento depende de la lógica post-sub) |

**Estado esperado:** ✅ PASA

---

### CU-J02-NEG-08: Hard Limit paginación >100 registros

**US:** US-001 | **CAs:** CA-10

| Paso | Resultado Esperado |
|:----:|-------------------|
| 1 | Request manipulado: GET `/api/v1/workdesk/tasks?page=1&size=500` |
| 2 | Backend retorna HTTP 400 Bad Request |
| 3 | "El parámetro size no puede superar 100 registros" |

**Estado esperado:** ✅ PASA

---

### CU-J02-NEG-09: IDOR en delegación — Espiar usuario sin jerarquía

**US:** US-001 | **CAs:** CA-15

| Paso | Resultado Esperado |
|:----:|-------------------|
| 1 | Operario A altera URL para ver tareas de otro departamento |
| 2 | Backend valida RBAC perimetral: A NO es superior jerárquico |
| 3 | HTTP 403 Forbidden: "No tiene permisos para ver este escritorio" |

**Estado esperado:** ✅ PASA

---

### CU-J02-NEG-10: Rate Limiting en endpoint de grilla

**US:** US-001 | **CAs:** CA-30

| Paso | Resultado Esperado |
|:----:|-------------------|
| 1 | Script dispara >60 peticiones/min a `/api/v1/workdesk/tasks` |
| 2 | Petición 61: HTTP 429 Too Many Requests |
| 3 | "Demasiadas consultas. Espera unos segundos." |

**Estado esperado:** ✅ PASA

---

### CU-J02-NEG-11: Sanitización DTO — Sin data leaks

**US:** US-001 | **CAs:** CA-14

| Paso | Resultado Esperado |
|:----:|-------------------|
| 1 | DevTools → Network → interceptar response de grilla |
| 2 | DTO sanitizado: sin PII, sin variables internas Camunda |
| 3 | Solo 5 columnas rígidas en payload (CA-14) |
| 4 | Toda consulta incluye `tenantId` inyectado |

**Estado esperado:** ✅ PASA

---

### CU-J02-NEG-12: Supervisor cross-team intenta despojo forzoso

**US:** US-002 | **CAs:** CA-13

| Paso | Resultado Esperado |
|:----:|-------------------|
| 1 | Supervisor Dept. B intenta `force-unclaim` de tarea Dept. A |
| 2 | Backend cruza `team_id` supervisor vs `team_id` tarea |
| 3 | HTTP 403: "No tiene permisos para gestionar tareas de este equipo" |
| 4 | Intento en Audit Log: `{ result: 'DENIED' }` |

**Estado esperado:** ✅ PASA

---

### CU-J02-NEG-13: Optimistic UI + Rollback tras fallo persistente

**US:** US-002 | **CAs:** CA-10, CA-21

| Paso | Resultado Esperado |
|:----:|-------------------|
| 1 | Micro-corte de red durante reclamo |
| 2 | Frontend "miente": tarea en "Mi Bandeja" con ⟳ giratorio |
| 3 | 3 reintentos (2s, 4s, 8s) todos fallan |
| 4 | Rollback visual: tarea devuelta a Cola |
| 5 | Modal: "No pudimos confirmar tu reclamo. La tarea sigue en la cola." |

**Estado esperado:** ✅ PASA

---

### CU-J02-NEG-14: Exceder 7 columnas en tablero Kanban

**US:** US-008 | **CAs:** CA-08

| Paso | Resultado Esperado |
|:----:|-------------------|
| 1 | Tablero con 7 columnas (Hard-Limit V1) |
| 2 | SM presiona "Añadir Columna" |
| 3 | HTTP 400: "Máximo 7 columnas por tablero en V1" |

**Estado esperado:** ✅ PASA

---

### CU-J02-NEG-15: Editar formulario de tarea en DONE

**US:** US-008 | **CAs:** CA-02

| Paso | Resultado Esperado |
|:----:|-------------------|
| 1 | Abrir tarea completada (DONE) |
| 2 | Todos los campos en Solo Lectura absoluto |
| 3 | POST/PUT de actualización → Backend rechaza |

**Estado esperado:** ✅ PASA

---

### CU-J02-NEG-16: Asignar 2 usuarios a tarjeta Kanban

**US:** US-008 | **CAs:** CA-04

| Paso | Resultado Esperado |
|:----:|-------------------|
| 1 | Intentar co-asignar `operario_a` y `operario_b` |
| 2 | Motor restringe: política 1:1 (Single-Assignee) |
| 3 | Error: "Solo un dueño por tarjeta en ejecución" |

**Estado esperado:** ✅ PASA

---

### CU-J02-NEG-17: Inmutabilidad de costos — Borrar time log

**US:** US-008 | **CAs:** CA-11

| Paso | Resultado Esperado |
|:----:|-------------------|
| 1 | Operario intenta DELETE/PUT sobre registro en `ibpms_time_logs` |
| 2 | API deniega: Append-Only |
| 3 | Correcciones solo via asiento contable negativo + auditoría |

**Estado esperado:** ✅ PASA

---

## Brechas Descubiertas en esta Certificación

| # | Brecha | Severidad | US | Sprint |
|---|--------|:---------:|:--:|:------:|
| B-J02-01 | Export PDF/PNG del diagrama potencialmente no implementado | 🟡 P2 | US-028 | 6.2 |
| B-J02-02 | B-20: Vinculación DMN↔BPMN no visual (decisionRef manual) | 🟠 P1 | US-005/007 | **6.1** |
| B-J02-03 | Formulario de inicio para "Crear Caso" posiblemente no implementado (Start Form) | 🟠 P1 | US-005 | 6.2 |
| B-J02-04 | Mock Workers para `reserve-funds` y `rollback-funds` necesitan configuración E2E | 🟡 P2 | Infra | **6.1** |
| B-J02-05 | Simulación de timer (PT72H→PT5S) requiere Camunda Job API o test facilitation | 🟡 P2 | Infra | 6.1 |
| B-J02-06 | Multi-Instance collection `listadoPeritos` debe ser inyectada como variable del proceso | 🟡 P2 | US-005 | 6.1 |

---

## Matriz de Trazabilidad

| Escenario | Fase | US | CAs Cubiertos | Flujo | Prioridad | Estado |
|-----------|:----:|:--:|:------------:|:-----:|:---------:|:------:|
| CU-J02-01 | Formularios | US-003 | CA-01,02,31,38 | — | MUST | ✅ |
| CU-J02-02 | Formularios | US-003 | CA-01,02,31 | — | MUST | ✅ |
| CU-J02-03 | Formularios | US-003 | CA-47,61,41 | — | MUST | ✅ |
| CU-J02-04 | Formularios | US-003 | CA-06,07 | — | MUST | ✅ |
| CU-J02-05 | DMN | US-007 | — | — | MUST | ✅ |
| CU-J02-06 | Import BPMN | US-028 | CA-01,03 | — | MUST | ✅ |
| CU-J02-07 | BPMN Binding | US-005,003 | CA-30,31 | — | MUST | ✅ |
| CU-J02-08 | DMN Binding | US-005,007 | — | — | MUST | ⚠️ B-20 |
| CU-J02-09 | Export | US-028 | CA-01,02 | — | SHOULD | ⚠️ |
| CU-J02-10 | Pre-Flight | US-005 | CA-09 | — | MUST | ✅ |
| CU-J02-11 | Deploy | US-005 | CA-21,65 | — | MUST | ✅ |
| CU-J02-F1-01 | Ejecución | US-001 | CA-01 | F1 | MUST | ✅ |
| CU-J02-F1-02 | Ejecución | US-002,029 | CA-05,10,12 | F1 | MUST | ✅ |
| CU-J02-F1-03 | Ejecución | US-029 | CA-47,61,41 | F1 | MUST | ✅ |
| CU-J02-F1-04 | Ejecución | US-005 | — | F1 | MUST | ✅ |
| CU-J02-F1-05 | Ejecución | US-029 | CA-31 | F1 | MUST | ✅ |
| CU-J02-F1-06 | Ejecución | US-001 | CA-05 | F1 | MUST | ✅ |
| CU-J02-F2-01 | Ejecución | US-001,007 | — | F2 | MUST | ✅ |
| CU-J02-F3-01 | Ejecución | US-002,029 | CA-31 | F3 | MUST | ✅ |
| CU-J02-F4-01 | Ejecución | US-001 | — | F4 | MUST | ✅ |
| CU-J02-K01 | Kanban | US-008,039 | CA-01,02 | — | MUST | ✅ |
| CU-J02-OBS-01 | Observabilidad | US-001 | CA-05 | — | SHOULD | ⚠️ |
| CU-J02-OBS-02 | Observabilidad | US-005 | CA-42 | — | MUST | ✅ |
| CU-J02-OBS-03 | Observabilidad | US-005 | CA-42 | — | MUST | ✅ |
| CU-J02-NEG-01 | Formularios | US-003 | — | — | MUST | ✅ |
| CU-J02-NEG-02 | Ejecución | US-029 | CA-06 | — | MUST | ✅ |
| CU-J02-NEG-03 | Deploy | US-005 | CA-09 | — | MUST | ✅ |
| CU-J02-NEG-04 | Deploy | US-005 | CA-21 | — | MUST | ✅ |
| CU-J02-NEG-05 | Import | US-028,007 | — | — | SHOULD | ✅ |
| CU-J02-NEG-06 | Genérico | US-039 | CA-04 | — | MUST | ✅ |
| CU-J02-NEG-07 | Ejecución | US-029 | — | — | SHOULD | ✅ |
| CU-J02-W01 | Workdesk | US-001 | CA-10,19,20 | — | MUST | ✅ |
| CU-J02-W02 | Workdesk | US-001 | CA-19,20 | — | MUST | ✅ |
| CU-J02-W03 | Workdesk | US-001 | CA-22,29 | — | MUST | ✅ |
| CU-J02-W04 | Workdesk | US-001 | CA-05,11,24 | — | MUST | ✅ |
| CU-J02-W05 | Workdesk | US-001 | CA-25,31 | — | MUST | ✅ |
| CU-J02-W06 | Workdesk | US-001 | CA-03,17,23 | — | MUST | ✅ |
| CU-J02-W07 | Workdesk | US-001 | CA-12 | — | MUST | ✅ |
| CU-J02-W08 | Workdesk | US-001 | CA-06,13,26,27 | — | MUST | ✅ |
| CU-J02-W09 | Workdesk | US-001 | CA-04,15 | — | MUST | ✅ |
| CU-J02-W10 | Workdesk | US-001 | CA-08,16,21,28 | — | MUST | ✅ |
| CU-J02-C01 | Claim | US-002 | CA-01,14 | — | MUST | ✅ |
| CU-J02-C02 | Claim | US-002 | CA-01,11 | — | MUST | ✅ |
| CU-J02-C03 | Claim | US-002 | CA-02,14,23 | — | MUST | ✅ |
| CU-J02-C04 | Claim | US-002 | CA-05,18 | — | MUST | ✅ |
| CU-J02-C05 | Claim | US-002 | CA-04,07,16,17 | — | MUST | ✅ |
| CU-J02-C06 | Claim | US-002 | CA-08,13 | — | MUST | ✅ |
| CU-J02-C07 | Claim | US-002 | CA-09,20 | — | MUST | ✅ |
| CU-J02-C08 | Claim | US-002 | CA-22 | — | MUST | ✅ |
| CU-J02-A01 | Kanban | US-008 | CA-05,08 | — | MUST | ✅ |
| CU-J02-A02 | Kanban | US-008,030 | CA-03,04 | — | MUST | ✅ |
| CU-J02-A03 | Kanban | US-008 | CA-12,06 | — | MUST | ✅ |
| CU-J02-A04 | Kanban | US-008 | CA-01 | — | MUST | ✅ |
| CU-J02-A05 | Kanban | US-008 | CA-03,09 | — | MUST | ✅ |
| CU-J02-A06 | Kanban | US-008 | CA-02,04 | — | MUST | ✅ |
| CU-J02-A07 | Kanban | US-008,039 | CA-01,02 | — | MUST | ✅ |
| CU-J02-NEG-08 | Workdesk | US-001 | CA-10 | — | MUST | ✅ |
| CU-J02-NEG-09 | Workdesk | US-001 | CA-15 | — | MUST | ✅ |
| CU-J02-NEG-10 | Workdesk | US-001 | CA-30 | — | MUST | ✅ |
| CU-J02-NEG-11 | Workdesk | US-001 | CA-14 | — | MUST | ✅ |
| CU-J02-NEG-12 | Claim | US-002 | CA-13 | — | MUST | ✅ |
| CU-J02-NEG-13 | Claim | US-002 | CA-10,21 | — | MUST | ✅ |
| CU-J02-NEG-14 | Kanban | US-008 | CA-08 | — | MUST | ✅ |
| CU-J02-NEG-15 | Kanban | US-008 | CA-02 | — | MUST | ✅ |
| CU-J02-NEG-16 | Kanban | US-008 | CA-04 | — | MUST | ✅ |
| CU-J02-NEG-17 | Kanban | US-008 | CA-11 | — | MUST | ✅ |

**Total: 57 escenarios UAT** (40 positivos + 17 negativos)  
**Cobertura Fase 1-6:** 4 flujos E2E (Happy Path + Rechazo DMN + Timeout/Escalamiento + Error/Compensación)  
**Cobertura Fase 7:** US-001 (10 CU + 4 NEG = 14), US-002 (8 CU + 2 NEG = 10), US-008 (7 CU + 4 NEG = 11)  
**Formularios probados: 5** (2 Maestro + 2 Simple + 1 Genérico Kanban)

---

## Historial de Cambios

| Fecha | Cambio | Autor |
|-------|--------|-------|
| 2026-04-13 | Creación inicial: 13 escenarios (orden BPMN → Forms) | Agente PO + Arquitecto Lead |
| 2026-04-13 | v2: Reordenado a flujo de negocio real + 2 escenarios negativos | Arquitecto Lead |
| 2026-04-19 | v3: Reescritura Total: +17 escenarios (32 total), iForm Maestro, US-028, US-039 | Agente PO + Arquitecto Lead |
| 2026-04-19 | **v4: Certificación E2E.** Proceso `insurance_claims_complex.bpmn`. 4 formularios campo-por-campo alineados a FormDesigner.vue. 4 flujos E2E (Happy+DMN Reject+Timeout+Compensation). DMN creation. Multi-instance 2 peritos. Kanban genérico. 31 escenarios. 6 brechas. B-20 formalizada. | Agente PO + Antigravity |
| 2026-05-13 | **v5: Fase 7 — Certificación Operativa.** +26 escenarios (10 Workdesk, 8 Claim, 7 Kanban, 10 NEG). Paginación server-side, SLA Ticking Engine, Concurrencia Optimista, Drag & Drop Kanban, Despojo Forzoso. Tablero ágil independiente. Total: 57 CU. | Agente PO (SSOT Gatekeeper) |

