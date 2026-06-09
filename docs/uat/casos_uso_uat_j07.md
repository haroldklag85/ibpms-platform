# Journey J-07: Arquitecto IA — Diseño BPMN, DMN Cognitivo y Copiloto IA

> **Journey:** J-07 — Certificación del Flujo de Diseño Asistido por IA
> **Actor principal:** Arquitecto de Procesos / Diseñador BPMN / Release Manager
> **Criticidad:** 🟠 ALTA (US-005 completada 97%, US-007 bloqueada por IDOR ~48%, US-027 ~65%)
> **US Cruzadas:** US-005, US-007, US-027, US-003, US-036
> **Épicas:** Formularios/BPMN (Épica B) + IA Cognitiva (Épica G) + Seguridad (Épica E)
> **Fecha:** 2026-04-19
> **Autor:** Agente PO (Antigravity)
> **Formato:** Híbrido (Manual paso-a-paso + links a `.spec.ts`)
> **Enfoque PO:** Certificar el ciclo de vida completo del diseño: NLP→DMN → BPMN → Pre-Flight → Copilot → Despliegue

---

## Narrativa del Journey

Este Journey certifica el flujo completo de un Arquitecto de Procesos que diseña un proceso de negocio end-to-end usando las herramientas de IA del iBPMS. El Arquitecto genera primero las reglas de negocio (DMN) usando lenguaje natural (US-007), luego diseña el diagrama BPMN en el lienzo (US-005), consulta al Copiloto IA para validación ISO 9001 (US-027), ejecuta el Pre-Flight Analyzer, y finalmente solicita el despliegue al Release Manager. Se validan las integraciones entre las 3 US y la coherencia de la gobernanza RBAC.

```
┌───────────────────────────────────────────────────────────────────────────────┐
│ FASE 1: DMN Cognitivo — NLP a Tablas de Decisión (US-007)                    │
│ FASE 2: Diseño BPMN — Lienzo del Arquitecto (US-005)                        │
│ FASE 3: Copiloto IA — Auditoría ISO 9001 y Sugerencias (US-027)             │
│ FASE 4: Pre-Flight y Versionamiento — Gobernanza de Despliegue (US-005)     │
│ FASE 5: Despliegue y Coexistencia — Release Manager (US-005)                │
│ FASE 6: Simulación Sandbox — Pruebas en Producción Aisladas (US-005)        │
└───────────────────────────────────────────────────────────────────────────────┘
```

---

## Precondiciones

| # | Precondición | Verificación | US Origen |
|---|-------------|-------------|-----------|
| PRE-1 | Usuario `arquitecto@alpha.com` con rol `ROLE_PROCESS_ARCHITECT` | JWT con rol efectivo | US-036 |
| PRE-2 | Usuario `designer@alpha.com` con rol `BPMN_Designer` (sin Deploy) | JWT restringido | US-005 CA-21 |
| PRE-3 | Usuario `releaser@alpha.com` con rol `BPMN_Release_Manager` | JWT con permiso deploy | US-005 CA-21 |
| PRE-4 | Al menos 2 formularios publicados en Pantalla 7 (1 Simple, 1 iForm Maestro) | `GET /api/v1/forms?status=PUBLISHED` ≥2 | US-003 |
| PRE-5 | Conectores API registrados en Pantalla 11 (O365, SharePoint, NetSuite) | `GET /api/v1/connectors` ≥3 | US-005 CA-45 |
| PRE-6 | Motor Camunda operativo | Health check 200 | US-000 |
| PRE-7 | LLM API operativa (OpenAI/Gemini) | Health check o mock configurado | US-007, US-027 |
| PRE-8 | Redis operativo (caché DMN y Sandbox counter) | `PING` → `PONG` | US-007 CA-2, US-005 CA-67 |

---

## FASE 1: DMN Cognitivo — NLP a Tablas de Decisión

### CU-J07-01: Catálogo de Tablas DMN — Dashboard Inicial
**CA Mapeado:** US-007 CA-17
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Arquitecto | Navega a Pantalla 4 (Módulo DMN) | Sistema NO carga chat NLP en blanco directamente |
| 2 | Sistema | Presenta Catálogo / Grilla de Tablas DMN | Grid con buscador server-side |
| 3 | Verificación | Cada fila muestra: Nombre, Decision_Ref, Versión Activa, Estado, Fecha, Autor, Nº filas | Anatomía completa |
| 4 | Arquitecto | Busca: "riesgo" en buscador | Filtro server-side retorna DMNs que contengan "riesgo" en nombre o Decision_Ref |
| 5 | Verificación | `GET /api/v1/dmn?status=ACTIVE&search=riesgo&page=1&size=20` | Paginación server-side funcional |
| 6 | Arquitecto | Presiona [+ Nueva Tabla DMN] | Se abre el Chat NLP / Editor DMN |
**Automatización:** `e2e/specs/j-07/dmn-catalog-dashboard.spec.ts`

### CU-J07-02: Generación DMN por NLP — Streaming SSE Fila por Fila
**CA Mapeado:** US-007 CA-01, US-007 CA-07, US-007 CA-08
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Arquitecto | En Chat NLP escribe: "Si el monto es menor a 1000, aprobar automáticamente. Si es entre 1000 y 5000, enviar a revisión. Si supera 5000, rechazar" | — |
| 2 | Frontend | Pre-renderiza Skeleton Loader en la grilla | Animación visual de carga progresiva |
| 3 | Backend | Abre canal SSE (Server-Sent Events) | NO usa HTTP síncrono bloqueante (anti-504) |
| 4 | Frontend | Renderiza Fila 1: `monto < 1000 → Aprobar` | Aparece en grilla en <8 segundos (TTFR) |
| 5 | Frontend | Renderiza Fila 2 y Fila 3 progresivamente | — |
| 6 | Sistema | Inyecta automáticamente **Hit Policy: FIRST** | Atributo `hitPolicy="FIRST"` en XML DMN |
| 7 | Sistema | Inyecta **Fila Catch-All** final: `else → Revisión Humana` 🔒 | Fila inamovible con candado visual |
| 8 | Verificación | Grilla muestra columna "Explainable DMN" (XAI) | Cada fila tiene traducción humana legible |
| 9 | Verificación | Variables son planas (sin dot notation) | Solo variables de primer nivel |
| 10 | Verificación | Sin Date-Math en expresiones FEEL | Fechas pre-calculadas como enteros |
**Automatización:** `e2e/specs/j-07/dmn-nlp-sse-generation.spec.ts`

### CU-J07-03: Caché Criptográfica — Evitar Doble Pago LLM
**CA Mapeado:** US-007 CA-02, US-007 CA-20
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Arquitecto | Envía exactamente el mismo prompt del CU-J07-02 | — |
| 2 | Backend | Calcula hash de (Prompt normalizado + Diccionario) | — |
| 3 | Backend | Match en Redis → devuelve tabla cacheada | HTTP 200 instantáneo (sin LLM call) |
| 4 | Verificación | Facturación LLM | CERO invocaciones nuevas. Tabla servida desde caché |
| 5 | Arquitecto | Envía prompt ligeramente diferente: "SI EL MONTO es menor a 1000, APROBAR automáticamente..." | — |
| 6 | Backend | Normaliza: lowercase + trim + collapse spaces → MISMO hash | — |
| 7 | Backend | Match en Redis | Misma tabla cacheada retornada (normalización CA-20) |
**Automatización:** `e2e/specs/j-07/dmn-cache-crypto-hash.spec.ts`

### CU-J07-04: Simulador de Decisiones DMN (Sandbox)
**CA Mapeado:** US-007 CA-11, US-007 CA-15
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Arquitecto | Presiona [🧪 Probar DMN / Simulator] | Panel de prueba visible |
| 2 | Arquitecto | Ingresa variables de prueba: `{monto: 3500}` | — |
| 3 | Backend | `POST /api/v1/dmn/{id}/evaluate-test` | Evaluación en motor FEEL de Camunda (no en JS del frontend) |
| 4 | Response | `{matched_rule_index: 1, output: {decision: "Revisión"}}` | — |
| 5 | Frontend | Ilumina en **verde** la Fila 2 (monto entre 1000-5000) | Visualización XAI intuitiva |
| 6 | Arquitecto | Prueba con `{monto: 500}` | Fila 1 se ilumina: "Aprobar" |
| 7 | Arquitecto | Prueba con `{monto: null}` | Fila Catch-All se ilumina: "Revisión Humana" |
| 8 | Verificación | Variables de prueba NO persistidas | Efímeras. Se pierden al cerrar Pantalla 4 |
**Automatización:** `e2e/specs/j-07/dmn-simulator-decisions.spec.ts`

### CU-J07-05: Publicación DMN con Confirmación Anti-Pánico
**CA Mapeado:** US-007 CA-12, US-007 CA-06
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Arquitecto | Presiona [Publicar V1] | Modal inevitable: "Digite CONFIRMO_V1 para publicar" |
| 2 | Arquitecto | Escribe "confirmo_v1" (minúsculas) | Sistema acepta (case-insensitive o requiere exacto) |
| 3 | Backend | `POST /api/v1/dmn/{id}/publish` | HTTP 200. Status cambia a ACTIVE |
| 4 | Backend | Warm-Up Cache en Camunda | DMN cargada anticipadamente para eliminar latencia en frío |
| 5 | Verificación | Catálogo muestra: "✅ ACTIVA (v1)" | Etiqueta de estado actualizada |
| 6 | Arquitecto | Intenta modificar directamente con `PUT /api/v1/dmn/{id}` | **HTTP 403 Forbidden** — Genera V2 obligatoriamente (inmutabilidad CA-06) |
| 7 | Arquitecto | Presiona [⏪ Revertir a V1] (visible en historial) | Rollback disponible |
**Automatización:** `e2e/specs/j-07/dmn-publish-confirm-panic.spec.ts`

### CU-J07-06: Virtual Scrolling y Buscador In-App para Grilla Grande
**CA Mapeado:** US-007 CA-10, US-007 CA-24
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Arquitecto | Genera tabla DMN de 45 filas x 6 columnas | — |
| 2 | Verificación DOM | Solo ~20 filas renderizadas en DOM (Virtual Scrolling) | Sin congelamiento de RAM |
| 3 | Arquitecto | Presiona Ctrl+F (buscador in-app intercepta tecla nativa) | Buscador integrado aparece en barra de grilla |
| 4 | Arquitecto | Busca "Revisión" | Resalta en amarillo TODAS las coincidencias (incluyendo filas fuera del viewport) |
| 5 | Arquitecto | Presiona [↓ Siguiente] | Scroll automático a la siguiente coincidencia |
| 6 | Verificación | Navegación por teclado (Enter, Tab) estilo Excel | Funcional entre celdas |
**Automatización:** `e2e/specs/j-07/dmn-virtual-scrolling-search.spec.ts`

---

## FASE 2: Diseño BPMN — Lienzo del Arquitecto

### CU-J07-07: Catálogo de Procesos y Creación Nuevo con Paleta Completa
**CA Mapeado:** US-005 CA-23, US-005 CA-22, US-005 CA-27
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Arquitecto | Abre Pantalla 6 (Diseñador BPMN) | Panel lateral "Explorador de Procesos" con lista de procesos |
| 2 | Verificación | Cada entrada muestra: Nombre, Versión Activa, Fecha, Autor | Anatomía CA-23 |
| 3 | Arquitecto | Presiona [Nuevo Proceso] | Modal: "Empezar desde Cero" / "Usar Plantilla" |
| 4 | Arquitecto | Selecciona Plantilla "Aprobación Simple" | Lienzo carga con nodos pre-configurados |
| 5 | Verificación Paleta | Elementos principales visibles: Start/End, User Task, Service Task, Exclusive/Parallel Gateway | — |
| 6 | Verificación Paleta | Elementos avanzados bajo submenús: "Más Eventos...", "Más Compuertas..." | No saturación visual CA-22 |
| 7 | Arquitecto | Arrastra Text Annotation al lienzo, escribe nota | Persistida en XML BPMN (CA-24) |
**Automatización:** `e2e/specs/j-07/bpmn-catalog-palette.spec.ts`

### CU-J07-08: Configuración de User Task con FormKey Dropdown Validado
**CA Mapeado:** US-005 CA-39, US-005 CA-40
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Arquitecto | Elige patrón "Patrón B: iForm Maestro" al crear proceso | Decisión inmutable para este proceso (CA-40) |
| 2 | Arquitecto | Selecciona User Task en el lienzo | Panel de Propiedades visible |
| 3 | Arquitecto | Accede al campo "📄 Formulario Asociado" | **Dropdown** (NO texto libre) que lista formularios de Pantalla 7 (CA-39) |
| 4 | Verificación | Dropdown filtra solo formularios tipo "🔵 iForm Maestro" (por patrón elegido) | Filtro activo por CA-40 |
| 5 | Verificación | Cada opción muestra: Nombre, Tipo, Nº etapas si Maestro | Información completa |
| 6 | Arquitecto | Selecciona "Formulario Onboarding v3" | FormKey vinculado al nodo |
| 7 | Arquitecto | Configura Naming Dual: Nombre Negocio="Llenar Datos", ID Técnico=auto-generado | `llenar_datos` como slug (CA-26) |
**Automatización:** `e2e/specs/j-07/bpmn-usertask-formkey-dropdown.spec.ts`

### CU-J07-09: Service Task con Conector API del Hub y Data Mapper
**CA Mapeado:** US-005 CA-45, US-005 CA-49, US-005 CA-50, US-005 CA-54, US-005 CA-62
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Arquitecto | Coloca Service Task en el lienzo | Panel de Propiedades |
| 2 | Arquitecto | Accede al campo "Conector / API" | **Dropdown** lista conectores de Pantalla 11: O365, SharePoint, NetSuite |
| 3 | Arquitecto | Selecciona "Oracle NetSuite" | Sub-panel de integración se abre |
| 4 | Verificación | Renderiza `<DataMapperGrid>` de 2 columnas (NO textarea JSON libre) | CA-49: Prohibición de JSON crudo |
| 5 | Verificación | Columna izquierda: campos fijos del Swagger de NetSuite | Dictados por contrato API |
| 6 | Verificación | Columna derecha: Dropdown de variables Zod del formulario asociado | Diccionario de datos del proceso |
| 7 | Arquitecto | Intenta emparejar variable String con campo Number del destino | Variable **deshabilitada (gris)** + tooltip "Tipo Incompatible" (CA-50) |
| 8 | Arquitecto | Empareja variable PII `[🔒 Dato Sensible]` con campo del destino | Badge visual de dato sensible. Encriptado en tránsito (CA-54) |
| 9 | Verificación | Service Task usa External Task Pattern (NO Java Delegate) | Topic registrado en catálogo (CA-62, CA-70) |
**Automatización:** `e2e/specs/j-07/bpmn-service-task-data-mapper.spec.ts`

### CU-J07-10: Business Rule Task Vinculada a DMN del CU-J07-05
**CA Mapeado:** US-005 CA-61, US-005 CA-12
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Arquitecto | Arrastra Business Rule Task al lienzo | — |
| 2 | Arquitecto | Accede a propiedades del nodo | Panel muestra Dropdown obligatorio `[ 🧠 Tabla de Decisión (Decision_Ref) ]` |
| 3 | Verificación | Dropdown lista DMNs de Pantalla 4 (US-007) | **NO permite código libre** (CA-61) |
| 4 | Arquitecto | Selecciona la DMN "Matriz Riesgo Crediticio" (publicada en CU-J07-05) | Decision_Ref vinculado |
| 5 | Arquitecto | Configura Binding: `DEPLOYMENT` (por defecto) | Motor evalúa con versión DMN del momento del despliegue (CA-12) |
| 6 | Arquitecto | Añade Exclusive Gateway DESPUÉS de la Business Rule Task | Gateway evalúa output de DMN |
| 7 | Arquitecto | Configura rama para "Revisión Humana" (Catch-All) → User Task | Ruta hacia revisión humana |
**Automatización:** `e2e/specs/j-07/bpmn-business-rule-dmn-binding.spec.ts`

### CU-J07-11: Bloqueo Pesimista de Edición Concurrente
**CA Mapeado:** US-005 CA-16, US-005 CA-43, US-005 CA-66
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Arquitecto (arquitecto@alpha.com) | Abre proceso "Solicitud_Credito" en Pantalla 6 | Lock exclusivo otorgado |
| 2 | Designer (designer@alpha.com) | Intenta abrir el mismo proceso | "🔒 Editado por arquitecto@alpha.com desde las 10:15 AM" |
| 3 | Designer | Controles de edición bloqueados | Modo "Solo Lectura" |
| 4 | Verificación | Arquitecto permanece inactivo 35 minutos | Lock NO expira (CA-43) |
| 5 | Designer | Sigue viendo "🔒 Bloqueado" | — |
| 6 | Verificación BD | `ibpms_process_locks` | Registro con `process_definition_key`, `locked_by`, `locked_at`, `browser_session_id` |
| 7 | Arquitecto | Cierra pestaña. Heartbeat falla 3x (90s) | Lock liberado automáticamente |
| 8 | Verificación | `ibpms_audit_log` | "[AUTO-RELEASE] Lock liberado por desconexión" |
**Automatización:** `e2e/specs/j-07/bpmn-pessimistic-lock.spec.ts`

---

## FASE 3: Copiloto IA — Auditoría ISO 9001 y Sugerencias

### CU-J07-12: Activación Explícita del Copiloto (Bajo Demanda)
**CA Mapeado:** US-005 CA-17, US-027 CA-01, US-027 CA-02
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Arquitecto | Diseña diagrama BPMN con 8 nodos | Copiloto IA NO ejecuta análisis automático en tiempo real |
| 2 | Arquitecto | Presiona explícitamente [🧠 Consultar Copiloto IA] | — |
| 3 | Backend | `POST /api/v1/ai/copilot/generate` con `SseEmitter` (180s timeout) | Canal SSE abierto |
| 4 | Frontend | Recibe sugerencias vía streaming | Panel de Feedback inferior muestra alertas ISO 9001 |
| 5 | Verificación RBAC | Solo `ROLE_PROCESS_ARCHITECT` / `BPMN_DESIGNER` pueden activar | `@PreAuthorize` activo (CA-02 US-027) |
| 6 | Operario random | Intenta `POST /api/v1/ai/copilot/generate` | **HTTP 403 Forbidden** |
**Automatización:** `e2e/specs/j-07/copilot-explicit-activation.spec.ts`

### CU-J07-13: Sugerencias del Copiloto con Action Pills
**CA Mapeado:** US-027 (Action Pills, Pre-Flight)
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Copiloto | Analiza XML del diagrama BPMN | — |
| 2 | Copiloto | Retorna sugerencias: "Considere agregar Timer Boundary Event a la tarea 'Analizar Solicitud' para SLA" | — |
| 3 | Frontend | Renderiza Action Pills interactivas | Botones: [✅ Aplicar] [❌ Ignorar] [📋 Copiar al Portapapeles] |
| 4 | Arquitecto | Presiona [✅ Aplicar] en sugerencia 1 | Nodo Timer añadido al lienzo automáticamente |
| 5 | Copiloto | Detecta Gateway sin rama "Revisión Humana" | Alerta: "⚠️ El Gateway X no contempla todos los outputs de la DMN" |
| 6 | Verificación | Action Pills visibles y funcionales | Test `CopilotActionPills.spec.ts` ✅ |
**Automatización:** `e2e/specs/j-07/copilot-action-pills.spec.ts`

### CU-J07-14: Destrucción Efímera de Sesión RAG (Boundary)
**CA Mapeado:** US-027 CA-04
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Arquitecto | Ha tenido 5 interacciones con el Copiloto en la sesión | — |
| 2 | Arquitecto | Presiona [🗑️ Limpiar Sesión] o navega fuera de Pantalla 6 | — |
| 3 | Backend | `DELETE /api/v1/ai/copilot/session/{session_id}` | HTTP 200 OK |
| 4 | Verificación BD | `ibpms_memory_vectors WHERE session_id = X` | Vectores RAG destruidos |
| 5 | Arquitecto | Regresa y consulta al Copiloto | Sesión nueva. Sin contexto previo |
| 6 | Verificación | tenantId extraído del JWT (POST-PARCHE IDOR) | Solo destruye sesiones del tenant autenticado |
**Automatización:** `e2e/specs/j-07/copilot-session-destroy.spec.ts`

### CU-J07-15: Resiliencia del Copiloto ante Fallas del LLM
**CA Mapeado:** US-027 (AI Recovery)
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Arquitecto | Consulta al Copiloto mientras LLM API tiene problemas (504/503) | — |
| 2 | Frontend | Intercepta error | Mensaje amigable: "El asistente no está disponible temporalmente. Puede continuar diseñando sin IA" |
| 3 | Verificación | No hay pantalla blanca ni stacktrace | Degradación graceful (US-000) |
| 4 | Verificación | Funcionalidad de diseño BPMN sigue operativa | Solo el panel de Copiloto está degradado |
| 5 | Verificación | Test `BpmnAiRecovery.spec.ts` | ✅ Ya certificado |
**Automatización:** `e2e/specs/j-07/copilot-ai-recovery.spec.ts`

---

## FASE 4: Pre-Flight y Versionamiento — Gobernanza de Despliegue

### CU-J07-16: Pre-Flight Analyzer — Validaciones Base y Avanzadas
**CA Mapeado:** US-005 CA-3, US-005 CA-18, US-005 CA-4
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Arquitecto | Presiona [🔍 Pre-Flight Analyze] | — |
| 2 | Sistema | Ejecuta `POST /api/v1/design/processes/validate` | Pre-Flight sin desplegar |
| 3 | Verificación | Valida Start Event con formulario asociado | ❌ Error si falta FormKey (CA-4) |
| 4 | Verificación | Valida nomenclatura de instancia definida | ❌ Error si no hay regla de nomenclatura (CA-5) |
| 5 | Verificación | Detecta TimerEvent sin expresión de duración | ⚠️ Advertencia (CA-18) |
| 6 | Verificación | Detecta MessageEvent sin correlación | ⚠️ Advertencia (CA-18) |
| 7 | Verificación | Detecta CallActivity apuntando a proceso inexistente | ❌ Error (CA-18) |
| 8 | Verificación | Valida Service Task con Topic registrado en catálogo | ❌ Error si topic no existe (CA-70) |
| 9 | Verificación | Valida Business Rule Task con DMN + Gateway Catch-All | ❌ Error si falta rama "Revisión Humana" (US-007 CA-14) |
| 10 | Sistema | Renderiza lista de Errores (❌) y Advertencias (⚠️) | Panel visual inferior en Pantalla 6 |
**Automatización:** `e2e/specs/j-07/bpmn-preflight-analyzer.spec.ts`

### CU-J07-17: Invalidación Automática del Pre-Flight tras Edición
**CA Mapeado:** US-005 CA-33
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Arquitecto | Ejecuta Pre-Flight → resultado "✅ Sin Errores" | Estado: OK |
| 2 | Arquitecto | Agrega un nuevo nodo al diagrama (User Task) | — |
| 3 | Sistema | Estado del Pre-Flight se resetea automáticamente | "⚠️ Pendiente de re-validación" |
| 4 | Verificación | Botón [🚀 DESPLEGAR] requiere nueva ejecución del Pre-Flight | Botón deshabilitado hasta re-validar |
**Automatización:** `e2e/specs/j-07/bpmn-preflight-invalidation.spec.ts`

### CU-J07-18: Auto-Guardado del Diagrama en Borrador
**CA Mapeado:** US-005 CA-19
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Arquitecto | Edita diagrama durante 2 minutos | — |
| 2 | Sistema | Auto-guarda borrador XML cada 30 segundos | Indicador discreto "✅ Guardado" en barra de estado |
| 3 | Arquitecto | Cierra navegador sin desplegar | — |
| 4 | Arquitecto | Reabre el proceso en Pantalla 6 | Último borrador recuperado automáticamente |
| 5 | Verificación | Diferencia entre último guardado y estado actual | Máximo 30 segundos de pérdida |
**Automatización:** `e2e/specs/j-07/bpmn-autosave-draft.spec.ts`

### CU-J07-19: Rollback a Versión Anterior del Proceso
**CA Mapeado:** US-005 CA-15
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Arquitecto | Navega al panel "Historial de Versiones" en Pantalla 6 | Lista: v1 (fecha, autor), v2 (fecha, autor), v3 (fecha, autor) |
| 2 | Arquitecto | Selecciona "Restaurar v2" | — |
| 3 | Sistema | Re-despliega v2 como nueva versión activa (v4 = copia de v2) | HTTP 201 Created con `version: 4` |
| 4 | Verificación | Instancias v3 en vuelo | Siguen corriendo naturalmente (Grandfathering CA-7) |
| 5 | Verificación | Nuevas instancias | Se crean con v4 (restauración de v2) |
**Automatización:** `e2e/specs/j-07/bpmn-version-rollback.spec.ts`

---

## FASE 5: Despliegue y Coexistencia — Release Manager

### CU-J07-20: Separación de Roles — Designer Solicita, Release Manager Despliega
**CA Mapeado:** US-005 CA-21, US-005 CA-34
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Designer (designer@alpha.com) | Abre proceso con Pre-Flight aprobado | — |
| 2 | Designer | Verifica botón [🚀 DESPLEGAR] | **Deshabilitado (gris)** — rol `BPMN_Designer` no tiene permiso |
| 3 | Designer | Presiona [📩 Solicitar Despliegue] | HTTP 200. Status: `PENDIENTE_APROBACIÓN_DESPLIEGUE` |
| 4 | Sistema | Crea tarea en Workdesk del Release Manager | Tarea visible con [🚀 Aprobar y Desplegar] y [❌ Rechazar] |
| 5 | Release Manager (releaser@alpha.com) | Abre la tarea de aprobación | Ve diagrama + resultados Pre-Flight |
| 6 | Release Manager | Presiona [🚀 Aprobar y Desplegar] | Despliegue ejecutado al motor Camunda |
| 7 | Verificación | `POST /api/v1/design/processes/deploy` con `multipart/form-data` | `file` (.bpmn), `deploy_comment` (≥10 chars), response con `deployment_id`, `version`, `deployed_at` (CA-65) |
| 8 | Verificación | `ibpms_deploy_requests` | Registro con status=APPROVED, reviewed_by, reviewed_at |
**Automatización:** `e2e/specs/j-07/bpmn-deploy-role-separation.spec.ts`

### CU-J07-21: Rechazo de Despliegue con Comentario Obligatorio
**CA Mapeado:** US-005 CA-69
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Release Manager | Recibe solicitud de despliegue del Designer | — |
| 2 | Release Manager | Presiona [❌ Rechazar] | Modal: "Ingrese motivo del rechazo (mínimo 20 caracteres)" |
| 3 | Release Manager | Escribe: "Falta Tim" (14 chars) | Botón Confirmar deshabilitado |
| 4 | Release Manager | Escribe: "Falta Timer Boundary en tarea crítica de análisis legal" | — |
| 5 | Release Manager | Presiona [Confirmar Rechazo] | HTTP 200. Status: REJECTED |
| 6 | Designer | Recibe notificación (bell icon) | "❌ Solicitud rechazada: Falta Timer Boundary..." |
| 7 | Verificación | `ibpms_deploy_requests` | status=REJECTED, review_comment con texto completo |
**Automatización:** `e2e/specs/j-07/bpmn-deploy-rejection.spec.ts`

### CU-J07-22: Coexistencia Pacífica de Versiones (Grandfathering)
**CA Mapeado:** US-005 CA-7, US-005 CA-8, US-005 CA-9
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Sistema | 15 instancias activas ejecutándose con V1 del proceso | — |
| 2 | Arquitecto | Despliega V2 del proceso | — |
| 3 | Verificación | V1 sigue procesando las 15 instancias antiguas | Coexistencia 100% pacífica (CA-7) |
| 4 | Verificación | Nuevas instancias se crean con V2 | — |
| 5 | Arquitecto | Accede a [Gestor de Instancias Activas] para forzar migración | — |
| 6 | Verificación | NO hay botón "Migrar Todos" masivo ciego | Solo checkboxes individuales (CA-8: cirugía quirúrgica) |
| 7 | Arquitecto | Selecciona Instancia #45 en nodo `Tarea_Analisis` | — |
| 8 | Sistema | Evalúa Migration Plan: `Tarea_Analisis` existe en V2 | Checkbox habilitado ✅ |
| 9 | Arquitecto | Selecciona Instancia #12 en nodo `Tarea_Obsoleta` | — |
| 10 | Sistema | `Tarea_Obsoleta` NO existe en V2 | Checkbox **deshabilitado** + tooltip: "Nodo actual no existe en V2" (CA-9) |
**Automatización:** `e2e/specs/j-07/bpmn-version-coexistence.spec.ts`

### CU-J07-23: Autogeneración de Roles RBAC desde Carriles (Lanes)
**CA Mapeado:** US-005 CA-6
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Arquitecto | Importa diagrama BPMN con Carril "Aprobadores_Legales" | — |
| 2 | Arquitecto | Carril contiene Tarea "Firmar_Contrato" asociada a "Form_Firma" | — |
| 3 | Arquitecto | Despliega exitosamente vía `POST /api/v1/design/processes/deploy` | — |
| 4 | Sistema | Crea automáticamente Rol: `BPMN_Flujo_Onboarding_Aprobadores_Legales` | — |
| 5 | Verificación | Rol tiene permisos de escritura sobre "Form_Firma" y ejecución sobre "Firmar_Contrato" | — |
| 6 | Verificación | Rol visible en Pantalla 14 (Módulo de Seguridad US-036) | Asignable a usuarios |
**Automatización:** `e2e/specs/j-07/bpmn-rbac-auto-roles-lanes.spec.ts`

---

## FASE 6: Simulación Sandbox — Pruebas en Producción Aisladas

### CU-J07-24: Sandbox con Intercepción de Integraciones Externas
**CA Mapeado:** US-005 CA-20, US-005 CA-41, US-005 CA-63
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Arquitecto | Presiona [🧪 Probar en Sandbox] | Instancia temporal generada en motor de producción (CA-41) |
| 2 | Sistema | Inyecta `X-Sandbox-Mode: true` en contexto del token simulado | — |
| 3 | Token simulado | Avanza paso a paso en visualización del lienzo | Animación visual del flujo del caso |
| 4 | Token alcanza | Service Task que llama a Oracle NetSuite | Worker intercepta `X-Sandbox-Mode: true` → ABORTA petición HTTP real |
| 5 | Worker | Retorna Mock Response (HTTP 200 simulado) al motor | ERP de producción NO recibe basura transaccional (CA-63) |
| 6 | Token alcanza | Send Task (correo) | Worker ABORTA envío SMTP real → Mock Response |
| 7 | Finalización | Token completa recorrido | Instancia Sandbox **auto-destruida** sin rastro en BD producción |
| 8 | Verificación | Instancia marcada como `SANDBOX_TEST` | Visible en Pantalla 15.A con badge [🧪 SANDBOX]. NO visible en Workdesk |
**Automatización:** `e2e/specs/j-07/bpmn-sandbox-simulation.spec.ts`

### CU-J07-25: Timeout de Auto-Destrucción de Sandbox (10 minutos)
**CA Mapeado:** US-005 CA-67
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Arquitecto | Inicia Sandbox con proceso largo (30 nodos) | — |
| 2 | Sistema | Token avanza lentamente durante 10 minutos | — |
| 3 | Minuto 10 | Sistema detecta timeout | Instancia anulada automáticamente |
| 4 | Verificación | Log | "[SANDBOX-TIMEOUT] Instancia sandbox {id} destruida por timeout (10min)" |
| 5 | Verificación | Redis `ibpms:sandbox:count` decrementado | Espacio liberado para nueva simulación |
**Automatización:** `e2e/specs/j-07/bpmn-sandbox-timeout.spec.ts`

---

## Escenarios Negativos

### CU-J07-NEG-01: Upload XML DMN con Hit Policy No Autorizada
**CA Mapeado:** US-007 CA-22
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Arquitecto | Carga manualmente XML DMN con `hitPolicy="COLLECT"` | — |
| 2 | Backend | Parsea XML | Detecta Hit Policy != FIRST |
| 3 | Response | **HTTP 422**: "La tabla DMN usa la política 'COLLECT', pero solo se permite FIRST en V1" | — |
| 4 | Arquitecto | Carga XML sin atributo `hitPolicy` | Backend inyecta automáticamente `FIRST` antes de persistir |

### CU-J07-NEG-02: Resiliencia SSE ante Desconexión Parcial
**CA Mapeado:** US-007 CA-19
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Arquitecto | Inicia generación DMN por NLP | SSE emitiendo filas |
| 2 | Red | Conexión SSE se interrumpe en la fila 12 de 30 | — |
| 3 | Frontend | Preserva 12 filas como borrador | Indicador: "⚠️ Generación Interrumpida (12 de 30 filas recibidas)" |
| 4 | Arquitecto | Presiona [🔄 Reintentar Generación] | Re-envía mismo prompt |
| 5 | Backend | Hash existe en caché Redis | Tabla completa devuelta instantáneamente |
| 6 | Frontend | Reemplaza borrador parcial con tabla completa | 30 filas renderizadas |

### CU-J07-NEG-03: Timeout SSE de 30 Segundos sin Primera Fila
**CA Mapeado:** US-007 CA-25
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Arquitecto | Envía prompt extremadamente complejo al LLM | — |
| 2 | Frontend | 30 segundos sin recibir NINGUNA fila | — |
| 3 | Frontend | Cierra conexión SSE | Mensaje: "La generación tardó más de lo esperado. Pulse [🔄 Reintentar]" |
| 4 | Arquitecto | Reintenta | Segunda generación puede ser más rápida |

### CU-J07-NEG-04: Break-Lock de Emergencia por Super Admin
**CA Mapeado:** US-005 CA-64
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Arquitecto | Tiene Lock sobre proceso crítico. PC falla | Lock persiste en BD |
| 2 | Super Admin | Accede al Catálogo de Procesos (Pantalla 6) | Ve botón rojo [🔓 Romper Candado (Break-Lock)] |
| 3 | Super Admin | Presiona Break-Lock | Backend destruye lock en BD |
| 4 | Verificación | Proceso liberado para edición inmediata | — |
| 5 | Verificación | `ibpms_audit_log` | "Super_Admin forzó liberación del diseño retenido por arquitecto@alpha.com" |

### CU-J07-NEG-05: Invalidación de Caché Redis al Mutar Diccionario Zod
**CA Mapeado:** US-007 CA-16
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Arquitecto | Modifica diccionario Zod en Pantalla 7: renombra variable "monto" a "amount" | — |
| 2 | Backend Formularios | Publica evento `FORM_SCHEMA_CHANGED` (vía RabbitMQ) | — |
| 3 | Servicio DMN | Escucha evento | Purga de Redis SOLO las entradas cuyos hashes incluyen el `form_id` modificado |
| 4 | Arquitecto | Regenera DMN con mismo prompt | LLM invocado nuevamente (caché invalidada) |
| 5 | Verificación | Nueva DMN usa variable "amount" (no "monto") | Diccionario actualizado reflejado |

---

## Matriz de Trazabilidad

| Escenario | US Principal | CAs Cubiertos | Fase | Estado Esperado |
|-----------|:-----------:|:------------:|:----:|:--------------:|
| CU-J07-01 | US-007 | CA-17 | DMN | ✅ PASA |
| CU-J07-02 | US-007 | CA-01, CA-07, CA-08 | DMN | ✅ PASA |
| CU-J07-03 | US-007 | CA-02, CA-20 | DMN | ✅ PASA |
| CU-J07-04 | US-007 | CA-11, CA-15 | DMN | ✅ PASA |
| CU-J07-05 | US-007 | CA-12, CA-06 | DMN | ✅ PASA |
| CU-J07-06 | US-007 | CA-10, CA-24 | DMN | ✅ PASA |
| CU-J07-07 | US-005 | CA-22, CA-23, CA-27 | BPMN | ✅ PASA |
| CU-J07-08 | US-005 | CA-26, CA-39, CA-40 | BPMN | ✅ PASA |
| CU-J07-09 | US-005 | CA-45, CA-49, CA-50, CA-54, CA-62, CA-70 | BPMN | ✅ PASA |
| CU-J07-10 | US-005/007 | CA-61, CA-12 | BPMN | ✅ PASA |
| CU-J07-11 | US-005 | CA-16, CA-43, CA-66 | BPMN | ✅ PASA |
| CU-J07-12 | US-005/027 | CA-17, CA-01, CA-02 | Copilot | ✅ PASA |
| CU-J07-13 | US-027 | Action Pills | Copilot | ✅ PASA |
| CU-J07-14 | US-027 | CA-04 | Copilot | ⚠️ PARCIAL (IDOR pendiente) |
| CU-J07-15 | US-027 | AI Recovery | Copilot | ✅ PASA |
| CU-J07-16 | US-005 | CA-3, CA-4, CA-18, CA-70 | Pre-Flight | ✅ PASA |
| CU-J07-17 | US-005 | CA-33 | Pre-Flight | ✅ PASA |
| CU-J07-18 | US-005 | CA-19 | Pre-Flight | ✅ PASA |
| CU-J07-19 | US-005 | CA-15 | Pre-Flight | ✅ PASA |
| CU-J07-20 | US-005 | CA-21, CA-34, CA-65 | Deploy | ✅ PASA |
| CU-J07-21 | US-005 | CA-69 | Deploy | ✅ PASA |
| CU-J07-22 | US-005 | CA-7, CA-8, CA-9 | Deploy | ✅ PASA |
| CU-J07-23 | US-005 | CA-6 | Deploy | ✅ PASA |
| CU-J07-24 | US-005 | CA-20, CA-41, CA-63 | Sandbox | ✅ PASA |
| CU-J07-25 | US-005 | CA-67 | Sandbox | ✅ PASA |
| CU-J07-NEG-01 | US-007 | CA-22 | Negativo | ✅ PASA |
| CU-J07-NEG-02 | US-007 | CA-19 | Negativo | ✅ PASA |
| CU-J07-NEG-03 | US-007 | CA-25 | Negativo | ✅ PASA |
| CU-J07-NEG-04 | US-005 | CA-64 | Negativo | ✅ PASA |
| CU-J07-NEG-05 | US-007 | CA-16 | Negativo | ⚠️ PARCIAL |

---

## Resumen de Cobertura J-07

| US | CAs Cubiertos | Total CAs US | % Cubierto en J-07 |
|----|:------------:|:----------:|:-------------------:|
| US-005 | CA-3,4,6,7,8,9,12,15,16,17,18,19,20,21,22,23,26,27,33,34,39,40,41,43,45,49,50,54,61,62,63,64,65,66,67,69,70 | 70 | **53%** (37 CAs) |
| US-007 | CA-01,02,06,07,08,10,11,12,15,16,17,19,20,22,24,25 | 25 | **64%** (16 CAs) |
| US-027 | CA-01,02,04, Action Pills, Pre-Flight, AI Recovery, AI Injection | ~10 | **70%** (7 CAs) |
| US-003 | (validación cruzada FormKey/Diccionario) | — | N/A |
| US-036 | (validación RBAC cruzada) | — | N/A |

---

## Brechas Descubiertas (Pre-Ejecución)

| # | Brecha | Severidad | US | Escenario | Acción Requerida |
|---|--------|:---------:|:--:|-----------|-----------------|
| B-11 | Borradores DMN GC no implementado (`DmnDraftCleanupScheduler`) | 🟡 P2 | US-007 | — | Implementar Job de purga 24h para tabla `ibpms_dmn_drafts` |
| B-12 | Seudonimización PII pre-LLM no verificada en DMN | 🟠 P1 | US-007 | — | CA-05: Interceptor PII antes del envío al LLM |
| B-13 | OBS-1 US-005 CA-68: Entity/DDL mismatch data mappings | 🟡 P2 | US-005 | — | Alinear `ibpms_data_mappings` con Entity JPA |
| B-14 | OBS-2 US-005 CA-65: Contrato API /deploy incompleto | 🟡 P2 | US-005 | CU-J07-20 | Completar OpenAPI annotations |
| B-15 | CAs remediación US-007 (13-18) parcialmente auditados | 🟡 P2 | US-007 | — | Auditoría pendiente de persistencia dual, endpoint simulador formal |
