# Casos de Uso UAT — Journey J-02

> **Journey:** Crear Formulario → Diseñar Proceso BPMN → Desplegar → Ejecutar Primera Tarea  
> **Flujo de negocio:** Formularios → BPMN → Workdesk → CQRS  
> **Actor principal:** Arquitecto de Procesos (BPM Analyst)  
> **Actores secundarios:** Operario, Motor Camunda  
> **Criticidad:** 🔴 ALTA — Es el journey de "primer uso" de la plataforma.  
> **Épicas cruzadas:** Formularios (É2) → BPMN (É4) → Workdesk (É1) → CQRS (É16)  
> **US involucradas:** US-003, US-005, US-029, US-017, US-001, US-002  
> **Fecha:** 2026-04-13 (v2 — reordenado por flujo de negocio)  
> **Autor:** Agente PO + Arquitecto Lead

> [!IMPORTANT]
> **Orden corregido en v2:** El flujo sigue la lógica de transformación digital real:
> primero se define **qué datos capturar** (Formularios), luego **cómo fluyen** (BPMN),
> después **quién los trabaja** (Workdesk) y finalmente **cómo persisten** (CQRS).

---

## Precondiciones

| # | Precondición | Verificación |
|---|-------------|-------------|
| PRE-01 | Usuario autenticado con rol `ROLE_BPM_ARCHITECT` o `ROLE_SUPER_ADMIN` | Token JWT con permisos de diseño |
| PRE-02 | Motor Camunda operativo | GET `/api/v1/engine-rest/version` retorna versión |
| PRE-03 | Acceso a Pantalla P7 (Designer iForm) y P6 (Modeler BPMN) | RBAC configurado (US-036) |
| PRE-04 | Ningún proceso previo requerido (escenario de "primer uso") | — |

---

## Escenarios UAT

---

### ETAPA 1: FORMULARIOS — ¿Qué datos necesito capturar?

---

### CU-J02-01: Arquitecto BPM accede al Designer de Formularios

**US:** US-003

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Arquitecto BPM | Navega a `/admin/modeler/forms` | Pantalla: lista de formularios (vacía si es primer uso) |
| 2 | Arquitecto BPM | Hace clic en "Nuevo Formulario" | Se abre el Designer iForm (P7) con canvas vacío |
| 3 | Sistema | Renderiza canvas del diseñador | Paleta de componentes a la izquierda, canvas central, panel de propiedades a la derecha |

**Criterio de aceptación:** El diseñador carga con la paleta de componentes disponibles (text, date, select, grid, etc.).

---

### CU-J02-02: Arquitecto BPM diseña un formulario con múltiples tipos de campo

**US:** US-003

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Arquitecto BPM | Asigna nombre: "Form Prueba Sprint-0" | Título visible en el canvas |
| 2 | Arquitecto BPM | Arrastra campo "Texto" al canvas | Campo de texto aparece con label editable |
| 3 | Arquitecto BPM | Configura: label="Observaciones", variable="observaciones", requerido=true | Panel de propiedades actualizado |
| 4 | Arquitecto BPM | Arrastra campo "Fecha" al canvas | Campo de fecha aparece |
| 5 | Arquitecto BPM | Configura: label="Fecha del evento", variable="fecha_evento" | — |
| 6 | Arquitecto BPM | Arrastra campo "Select" al canvas | Campo select con opciones editables |
| 7 | Arquitecto BPM | Configura opciones: "Aprobado", "Rechazado", "Pendiente" | Dropdown funcional en preview |
| 8 | Arquitecto BPM | (Opcional) Arrastra un "Grid Repetible" con 2 columnas | Grid editable con filas dinámicas |

**Criterio de aceptación:** El formulario tiene ≥3 campos de tipos distintos (text, date, select). El diseñador soporta drag & drop.

---

### CU-J02-03: Arquitecto BPM genera esquema Zod automáticamente

**US:** US-003

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Arquitecto BPM | Presiona "Generar Schema" o navega a pestaña "Validación" | — |
| 2 | Sistema | Auto-genera esquema Zod basado en los campos configurados | Vista previa: `z.object({ observaciones: z.string().min(1), fecha_evento: z.date(), ... })` |
| 3 | Arquitecto BPM | (Opcional) Ajusta reglas de validación (ej: min length, regex) | Editor de schema con syntax highlighting |
| 4 | Sistema | Muestra preview del formulario con validación activa | Al dejar "Observaciones" vacío, error inline visible |

**Criterio de aceptación:** El esquema Zod se genera automáticamente y es coherente con las propiedades de los campos (required → `min(1)`, select → `z.enum(...)`, etc.).

---

### CU-J02-04: Arquitecto BPM guarda y publica el formulario

**US:** US-003

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Arquitecto BPM | Presiona "Guardar" | POST `/api/v1/forms` → 201 Created |
| 2 | Sistema | Muestra toast: "Formulario guardado ✓" | Estado = "Borrador" |
| 3 | Arquitecto BPM | Presiona "Publicar" (o el formulario se publica al vincularlo) | Estado cambia a "Publicado" |
| 4 | Arquitecto BPM | Navega a la lista de formularios | "Form Prueba Sprint-0" aparece con estado visible |

**Criterio de aceptación:** El formulario se persiste y está disponible para ser vinculado a un proceso BPMN.

---

### ETAPA 2: BPMN — ¿Cómo fluyen los datos entre personas?

---

### CU-J02-05: Arquitecto BPM accede al Modeler BPMN

**US:** US-005

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Arquitecto BPM | Navega a `/admin/modeler/processes` | Pantalla P6: lista de procesos BPMN |
| 2 | Arquitecto BPM | Hace clic en "Nuevo Proceso" | Se abre el lienzo BPMN visual (bpmn.js) |
| 3 | Sistema | Renderiza canvas con un Start Event por defecto | Lienzo interactivo con paleta de elementos BPMN |

**Criterio de aceptación:** El modeler carga con un Start Event y la paleta de herramientas BPMN.

---

### CU-J02-06: Arquitecto BPM modela un proceso y vincula el formulario existente

**US:** US-005, US-003

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Arquitecto BPM | Arrastra un "User Task" desde la paleta al lienzo | Nodo User Task aparece conectado al Start Event |
| 2 | Arquitecto BPM | Configura el User Task: nombre="Revisar solicitud", candidateGroup="operarios" | Panel de propiedades actualizado |
| 3 | Arquitecto BPM | **Vincula el formulario:** En "Form Key" o "Form ID", selecciona "Form Prueba Sprint-0" | Dropdown muestra formularios publicados. Vinculación guardada |
| 4 | Sistema | Indicador verde: "Formulario vinculado ✓" | El User Task sabe qué formulario renderizar |
| 5 | Arquitecto BPM | Arrastra un "End Event" y conecta: User Task → End | Flujo completo: Start → User Task → End |
| 6 | Arquitecto BPM | Configura un lane: "Operarios" | Lane visible en el lienzo |
| 7 | Arquitecto BPM | Asigna nombre al proceso: "Proceso de Prueba Sprint-0" | Metadata del proceso actualizada |

**Criterio de aceptación:** El proceso tiene Start → UserTask (con formulario vinculado) → End. El formulario creado en la Etapa 1 es seleccionable desde el modeler.

> [!IMPORTANT]
> **GAP-J02-01:** El mecanismo exacto de vinculación (¿formKey de Camunda? ¿campo custom en la BD?) es una decisión arquitectónica pendiente. Este escenario define la expectativa funcional: el Arquitecto BPM selecciona un formulario de una lista de formularios publicados.

---

### CU-J02-07: Arquitecto BPM guarda el proceso como borrador

**US:** US-005

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Arquitecto BPM | Presiona "Guardar" | POST `/api/v1/processes` con BPMN XML → 200/201 |
| 2 | Sistema | Muestra toast: "Proceso guardado ✓" | Estado = "Borrador" |
| 3 | Arquitecto BPM | Navega a la lista de procesos | "Proceso de Prueba Sprint-0" visible con estado "Borrador" |

**Criterio de aceptación:** El proceso se persiste como BPMN XML y es recuperable.

---

### ETAPA 3: DEPLOY — Poner a funcionar

---

### CU-J02-08: Arquitecto BPM despliega el proceso con versión semántica

**US:** US-005

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Arquitecto BPM | Presiona "Desplegar" en el modeler | — |
| 2 | Sistema | Modal de confirmación: "¿Desplegar v1.0.0?" | Versión semántica sugerida automáticamente |
| 3 | Arquitecto BPM | Confirma el despliegue | — |
| 4 | Sistema | POST `/api/v1/processes/{id}/deploy` → Camunda recibe BPMN XML | — |
| 5 | Sistema | Toast: "Proceso desplegado exitosamente v1.0.0 ✓" | Estado cambia de "Borrador" a "Desplegado" |
| 6 | Sistema | El proceso aparece en Motor Camunda | GET `/api/v1/engine-rest/process-definition` incluye el nuevo proceso |

**Criterio de aceptación:** El proceso es ejecutable por Camunda tras el despliegue.

---

### CU-J02-09: Se inicia una instancia del proceso

**US:** US-005 / US-024

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Arquitecto BPM | Presiona "Iniciar instancia" (o via webhook/API) | — |
| 2 | Sistema | POST `/api/v1/engine-rest/process-definition/{key}/start` | Instancia creada |
| 3 | Motor Camunda | Avanza al primer User Task | Tarea queda en estado CREATED |
| 4 | Sistema | La tarea se materializa en el Workdesk | Visible para operarios del candidateGroup |

**Criterio de aceptación:** La tarea generada por Camunda aparece en el Workdesk con el formulario vinculado.

---

### ETAPA 4: WORKDESK + CQRS — El operario trabaja

---

### CU-J02-10: Operario ve y ejecuta la primera tarea del proceso

**US:** US-001, US-002, US-029

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Operario | Navega al Workdesk (P1) | Ve la tarea "Revisar solicitud" del "Proceso de Prueba Sprint-0" |
| 2 | Operario | Reclama la tarea | Asignada al operario (CU-J04-02 aplica) |
| 3 | Operario | Abre la tarea | Formulario "Form Prueba Sprint-0" carga con los 3 campos diseñados en Etapa 1 |
| 4 | Operario | Llena: Observaciones="Todo OK", Fecha="hoy", Estado="Aprobado" | Campos válidos, sin errores Zod |
| 5 | Operario | Presiona "Enviar" | Tarea completada exitosamente |

**Criterio de aceptación:** El formulario diseñado por el Arquitecto BPM (Etapa 1) se renderiza correctamente para el Operario. La validación Zod funciona.

---

### CU-J02-11: El proceso avanza y finaliza — Persistencia CQRS

**US:** US-017

| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Sistema | Tras completar la tarea (CU-J02-10) | — |
| 2 | Sistema (CQRS) | Crea evento inmutable en form_event_store | INSERT: eventId, taskId, formData, timestamp, userId |
| 3 | Motor Camunda | Avanza al End Event | La instancia del proceso se completa (COMPLETED) |
| 4 | Sistema | No quedan tareas pendientes | GET `/api/v1/tasks?processInstanceId={id}` retorna 0 |
| 5 | Arquitecto BPM | Verifica en historial del proceso | Estado = "COMPLETED" con el detalle de datos llenados |

**Criterio de aceptación:** La instancia pasa de ACTIVE → COMPLETED. Los datos son inmutables en el event store.

---

## Escenarios Negativos

### CU-J02-NEG-01: Guardar formulario sin campos

| Paso | Resultado Esperado |
|:----:|-------------------|
| 1 | Arquitecto BPM intenta guardar formulario vacío (sin campos) |
| 2 | Validación: "El formulario debe tener al menos 1 campo" |
| 3 | El formulario no se guarda |

### CU-J02-NEG-02: Desplegar proceso con BPMN inválido

| Paso | Resultado Esperado |
|:----:|-------------------|
| 1 | Arquitecto BPM intenta desplegar proceso sin End Event |
| 2 | Motor Camunda rechaza: error de validación BPMN |
| 3 | UI muestra error descriptivo: "El proceso no tiene End Event" |
| 4 | El proceso mantiene estado "Borrador" |

### CU-J02-NEG-03: Desplegar proceso con formulario no publicado

| Paso | Resultado Esperado |
|:----:|-------------------|
| 1 | Arquitecto BPM vincula un formulario que aún está en "Borrador" al User Task |
| 2 | Al intentar desplegar, sistema advierte: "El formulario 'X' no está publicado" |
| 3 | Opción: "¿Publicar automáticamente?" o bloquear despliegue |

### CU-J02-NEG-04: Operario abre tarea cuyo formulario fue eliminado

| Paso | Resultado Esperado |
|:----:|-------------------|
| 1 | Un formulario vinculado a un proceso desplegado es eliminado (edge case) |
| 2 | Operario intenta abrir la tarea en el Workdesk |
| 3 | Mensaje de error: "Formulario no encontrado. Contacte al administrador." |
| 4 | La tarea no se puede completar (estado inconsistente reportado) |

---

## Flujo Completo del Journey (Resumen Visual)

```
ETAPA 1: FORMULARIOS                    ETAPA 2: BPMN
┌──────────────────────┐                ┌──────────────────────┐
│ CU-01: Accede        │                │ CU-05: Accede        │
│ CU-02: Diseña campos │                │ CU-06: Modela +      │
│ CU-03: Schema Zod    │───────────────▶│        vincula form  │
│ CU-04: Guarda/Publica│  (form listo)  │ CU-07: Guarda        │
└──────────────────────┘                └─────────┬────────────┘
                                                  │
                                                  ▼
ETAPA 4: WORKDESK + CQRS               ETAPA 3: DEPLOY
┌──────────────────────┐                ┌──────────────────────┐
│ CU-10: Operario ve   │                │ CU-08: Despliega     │
│        y ejecuta     │◀───────────────│ CU-09: Inicia        │
│ CU-11: CQRS persiste │  (tarea en WD) │        instancia     │
│        + Camunda end │                └──────────────────────┘
└──────────────────────┘
```

---

## Matriz de Trazabilidad

| Escenario | Etapa | US | CAs | Prioridad |
|-----------|:-----:|:--:|:---:|:---------:|
| CU-J02-01 | Formularios | US-003 | — | MUST |
| CU-J02-02 | Formularios | US-003 | — | MUST |
| CU-J02-03 | Formularios | US-003 | — | MUST |
| CU-J02-04 | Formularios | US-003 | — | MUST |
| CU-J02-05 | BPMN | US-005 | — | MUST |
| CU-J02-06 | BPMN | US-005/US-003 | — | MUST |
| CU-J02-07 | BPMN | US-005 | — | MUST |
| CU-J02-08 | Deploy | US-005 | — | MUST |
| CU-J02-09 | Deploy | US-005/US-024 | — | MUST |
| CU-J02-10 | Workdesk | US-001/002/029 | CA-01 (c/u) | MUST |
| CU-J02-11 | CQRS | US-017 | — | MUST |
| CU-J02-NEG-01 | Formularios | US-003 | — | MUST |
| CU-J02-NEG-02 | Deploy | US-005 | — | MUST |
| CU-J02-NEG-03 | Deploy | US-005/003 | — | SHOULD |
| CU-J02-NEG-04 | Workdesk | US-029 | — | SHOULD |

**Total: 15 escenarios UAT** (11 positivos + 4 negativos)

---

## Historial de Cambios

| Fecha | Cambio | Autor |
|-------|--------|-------|
| 2026-04-13 | Creación inicial: 13 escenarios (orden BPMN → Forms) | Agente PO + Arquitecto Lead |
| 2026-04-13 | **v2: Reordenado a flujo de negocio real: Forms → BPMN → Deploy → Workdesk → CQRS.** Agregados 2 escenarios negativos (NEG-03, NEG-04). Agregado diagrama de flujo visual. | Arquitecto Lead (por directiva del Jefe de Equipo) |
