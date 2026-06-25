# 🧪 Informe Técnico QA — Journey J-02 (BPMN + Forms) | Sprint PM-01

| Campo | Valor |
|-------|-------|
| **Sprint** | PM-01 (Fase de Estabilización) |
| **Iteración** | Sprint01-UAT-J02 |
| **Journey** | J-02 — Diseño BPMN y Formularios |
| **Cadena de Capacidad** | Cadena 4 (BPMN E2E) + Cadena 3 (Forms E2E) |
| **US Cubiertas** | US-005 (BPMN Designer), US-003 (Form Designer/Catalog) |
| **CAs Foco** | CA-39 (Dropdown FormKey real), CA-40 (Filtro Simple vs Maestro) |
| **Tester Humano** | Harold |
| **Agente QA** | Antigravity (QA Lead) |
| **Rama** | DevDavid |
| **Fecha Inicio** | 2026-06-22 |
| **Estado** | 🟡 EN EJECUCIÓN |

---

## Estado de Cadenas de Capacidad

| Cadena | Estado | Relevancia para J-02 |
|--------|--------|---------------------|
| Cadena 3 — Forms E2E | 🟡 EN PROGRESO | US-003 (Diseñador Forms) — foco de Misiones 2-3 |
| Cadena 4 — BPMN E2E | 🟡 EN PROGRESO | US-005 (Motor BPMN) — foco de Misiones 4-6 |
| Cadena 1 — Seguridad | ✅ COMPLETADA | Prerequisito satisfecho (Auth, RBAC) |

---

## Dependencias del Journey

| Journey | Depende de | Estado |
|---------|------------|--------|
| **J-02** | Ninguno | ✅ Sin dependencias bloqueantes |

---

## Registro de Misiones

### Misión 0: Precondiciones de Infraestructura
- **Estado**: ✅ PASS
- **Fecha/Hora**: 2026-06-22 22:40 COT
- **Pasos**: 

| Paso | Acción | Resultado Esperado | Resultado Real | Veredicto |
|:----:|--------|-------------------|----------------|:---------:|
| 0.1 | `docker ps` | Contenedores Docker Up (healthy) | ✅ `ibpms-postgres-uat` (5433, healthy), `ibpms-redis-uat` (6379, healthy), `ibpms-rabbitmq-uat` (5672, healthy) — todos Up 37 min | ✅ PASS |
| 0.2 | `curl -s http://localhost:8080/actuator/health` | `{"status":"UP"}` | ✅ Navegador muestra `{"status":"UP"}` en `localhost:8080/actuator/health` | ✅ PASS |
| 0.3 | Abrir `http://localhost:5173` | Página de Login renderiza | ✅ Login renderiza en `/login`: "Docketing — Plataforma iBPMS Corporativa" con botón "Ingresar con Microsoft Entra ID" + opción "BREAK-GLASS RECOVERY (IT ONLY)" | ✅ PASS |

- **Observaciones de entorno**:
  - Login usa **Microsoft Entra ID** (SSO corporativo) como método principal de autenticación, no email/password tradicional.
  - Existe opción "BREAK-GLASS RECOVERY (IT ONLY)" como acceso de emergencia.
  - Contenedor adicional detectado: `ibpms-azurite-dev` (emulador Azure Storage, puerto 10002) — servicio complementario.
- **Bugs descubiertos**: Ninguno
- **Evidencia**: 3 capturas de pantalla proporcionadas por Harold (docker ps, health check, login UI)
- **Veredicto de misión**: ✅ **PASS** — Infraestructura completamente operativa

---

### Misión 1: Login y Navegación Base
- **Estado**: ⚠️ PASS CON OBSERVACIONES
- **Fecha/Hora**: 2026-06-24 19:35 COT
- **Pasos**:

| Paso | Acción | Resultado Esperado | Resultado Real | Veredicto |
|:----:|--------|-------------------|----------------|:---------:|
| 1.1 | Autenticarse vía Microsoft Entra ID o Break-Glass | Redirige al Portal (`/`), menú lateral visible | ✅ Login exitoso. Portal carga con saludo "Buenos días, @[Super_Administrador]". Menú lateral visible con Grupos A-D. Usuario: Super admin | ✅ PASS |
| 1.2 | Navegar al módulo Modelador desde el menú lateral | Debe existir sección/ruta `/admin/modeler/*` accesible | ❌ No existe ruta padre `/admin/modeler/`. Al navegar directamente a esa URL → **404 "Página no encontrada"**. El sidebar tiene links directos a sub-páginas (Modelador BPMN, Catálogo Formularios, etc.) bajo "Grupo C: Diseño y..." pero no hay landing page del Modelador | ⚠️ FAIL (no bloqueante) |
| 1.3 | Acceder al sub-módulo de Formularios | Debe navegar a `/admin/modeler/forms` sin error | ⚠️ El sidebar SÍ tiene "Catálogo Formularios" que apunta a `/admin/modeler/forms`. Harold reportó que el acceso directo por URL funciona y la vista carga correctamente con 3 formularios reales. La percepción de FAIL fue por la ausencia de flujo jerárquico (Modelador hub → Forms) | ⚠️ PASS con observación |

- **Observaciones de entorno**:
  - El menú lateral es **dinámico** — servido por el backend vía `GET /api/v1/users/me/menu-layout` (componente `MenuLayoutController.java`)
  - **Grupo C: Diseño y Modelado Low-Code** contiene 8 items: Modelador BPMN, Catálogo Formularios, Diseñador Formularios, DMN Intelligence, Librería Prompts, Formulario Genérico, Visual Mapper, Constructor de Proyecto
  - Las rutas `/admin/modeler/bpmn`, `/admin/modeler/forms`, `/admin/modeler/forms/designer`, `/admin/modeler/dmn` SÍ existen como rutas planas independientes en el router
  - La ruta padre `/admin/modeler/` NO existe — no hay componente landing ni redirect definido
- **Bugs descubiertos**: BUG-J02-001, BUG-J02-002 (ver tabla de bugs)
- **Evidencia**: 3 capturas de Harold (portal con sidebar, 404 en /admin/modeler/, gestor de formularios)
- **Veredicto de misión**: ⚠️ **PASS CON OBSERVACIONES** — Login y acceso a sub-módulos funciona vía sidebar. Bugs de navegación documentados como no-bloqueantes.

---

### Misión 2: Catálogo de Formularios (US-003)
- **Estado**: ✅ PASS (después de re-test post-fix)
- **Fecha/Hora original**: 2026-06-24 19:58 COT (FAIL)
- **Fecha/Hora re-test**: 2026-06-24 21:00 COT (PASS)

#### Ejecución Original (pre-fix) — FAIL

| Paso | Acción | Resultado Esperado | Resultado Real | Veredicto |
|:----:|--------|-------------------|----------------|:---------:|
| 2.1 | Navegar a `/admin/modeler/forms` | Catálogo renderiza con formularios reales de PostgreSQL | ⚠️ Catálogo renderizaba 3 formularios hardcodeados que no correspondían a la BD real | ⚠️ FAIL |
| 2.2 | Verificar petición HTTP GET en DevTools | Petición GET al backend con respuesta JSON real | ❌ No se evidenciaba petición HTTP GET real al backend | 🔴 FAIL |
| 2.3 | Verificar que los datos son reales | Formularios reales de PostgreSQL | ❌ Datos hardcodeados confirmados (FRM-001, FRM-002, FRM-003) | 🔴 FAIL |

#### RE-TEST (post-fix commit `ef18729d`) — ✅ PASS

| Paso | Acción | Resultado Esperado | Resultado Real | Veredicto |
|:----:|--------|-------------------|----------------|:---------:|
| 2.1 | Navegar a `/admin/modeler/forms` | Catálogo renderiza con formularios reales de PostgreSQL | ✅ Catálogo muestra 3 formularios REALES de la BD: `qa_form_complex_schema` (Complex QA Form), `DATOSPERSONALES` (DatosPersonales), `SOLICITUD_ONBOARDING_(V1)` (Solicitud Onboarding V1). Son DIFERENTES a los hardcodeados anteriores. | ✅ PASS |
| 2.2 | Verificar petición HTTP GET en DevTools | Petición GET al backend con respuesta JSON real | ✅ DevTools muestra petición `GET https://localhost:5173/api/v1/forms` → Status **200 OK**, Content-Type `application/json`. El request va al puerto 5173 (proxy Vite) que reenvía al backend :8080. Comportamiento normal. | ✅ PASS |
| 2.3 | Verificar que los datos son reales | Formularios reales de PostgreSQL | ✅ JSON de respuesta muestra array con 3 objetos reales: author, name, uri, type (SIMPLE), version (1), updatedAt con timestamps reales y distintos. **Zero mocks confirmado.** | ✅ PASS |

- **Validación de Contrato API**:
  - Endpoint: `GET /api/v1/forms` → ✅ Respuesta 200 OK con array JSON de formularios
  - Observación: La URL en DevTools muestra `:5173` (proxy Vite), no `:8080` directamente. Esto es comportamiento estándar del dev server, no una discrepancia de contrato.
- **Bugs descubiertos**: BUG-J02-003 → ✅ **CORREGIDO Y VERIFICADO** en commit `ef18729d`
- **Evidencia post-fix**: 2 capturas de Harold (DevTools Headers con 200 OK, DevTools Response con JSON real)
- **Veredicto de misión**: ✅ **PASS** — Catálogo muestra datos reales de PostgreSQL. Zero mocks confirmado por evidencia DevTools.

---

### Misión 3: Crear un Formulario Nuevo (US-003)
- **Estado**: ✅ PASS
- **Fecha/Hora**: 2026-06-24 21:14 COT
- **Pasos**:

| Paso | Acción | Resultado Esperado | Resultado Real | Veredicto |
|:----:|--------|-------------------|----------------|:---------:|
| 3.1 | Click en "+ Crear Nuevo" o navegar a `/admin/modeler/forms/designer` | Diseñador de formularios abre | ✅ Diseñador abre con modal "Crear Nuevo Formulario (Dual-Pattern)". Muestra 2 opciones: **Formulario Simple** y **iForm Maestro** (relevante para CA-40). Paleta de componentes visible a la izquierda. | ✅ PASS |
| 3.2 | Configurar nombre "UAT_Formulario_Prueba_Harold" | Campo acepta el texto | ✅ Harold creó múltiples formularios de prueba: UAT_Formulario_Prueba_DavidR, DavidR2, DavidR3. Nombres aceptados sin error. | ✅ PASS |
| 3.3 | Agregar al menos un campo al formulario | Campo aparece en el lienzo | ✅ Campos agregados visualmente al formulario. La respuesta JSON del POST confirma `formFields` con definiciones de campos. | ✅ PASS |
| 3.4 | Guardar el formulario | Toast de éxito + POST/PUT exitoso en DevTools | ✅ DevTools confirma: `POST /api/v1/forms` → **201 Created**. Respuesta con UUID real (`11fc5702-...`), version 1, timestamps reales. Diálogo de validación E2E (CA-29): **"VALIDACIÓN EXITOSA"** + "BACKEND HTTP RESPONSE 201 CREATED!" | ✅ PASS |
| 3.5 | Regresar al catálogo y verificar persistencia | Formulario nuevo aparece en la lista | ✅ Catálogo muestra **6 formularios** (3 originales + 3 creados por Harold). Los nuevos: `UATDAVID` (21:08), `UATDAVID2` (21:10), `UATV3` (21:10) — todos con timestamps reales distintos. **Persistencia E2E confirmada.** | ✅ PASS |

- **Validación de Contrato API**:
  - Endpoint: `POST /api/v1/forms` → ✅ Respuesta 201 Created con JSON de formulario creado
  - Response incluye: id (UUID), name, technicalName, pattern, version, authorId, updatedAt, formFields
- **Observaciones**:
  - El modal "Dual-Pattern" confirma que la arquitectura Simple/Maestro (CA-40) está implementada en el diseñador
  - La validación E2E integrada (CA-29) se ejecuta automáticamente al guardar — buen diseño defensivo
- **Bugs descubiertos**: Ninguno
- **Evidencia**: 3 capturas de Harold (modal Dual-Pattern, DevTools POST 201, catálogo con 6 forms)
- **Veredicto de misión**: ✅ **PASS** — Creación y persistencia E2E de formularios funciona correctamente con datos reales.

---

### Misión 4: Diseñador BPMN — Apertura y Canvas (US-005)
- **Estado**: ✅ PASS
- **Fecha/Hora**: 2026-06-24 21:21 COT
- **Pasos**:

| Paso | Acción | Resultado Esperado | Resultado Real | Veredicto |
|:----:|--------|-------------------|----------------|:---------:|
| 4.1 | Navegar a `/admin/modeler/bpmn` | Modal de bienvenida aparece | ✅ Modal "Bienvenido al Diseñador iBPMS" carga correctamente. Muestra **Procesos Recientes** (Datos v0 DRAFT, Datos2 v0 DRAFT) y panel "Crear Nuevo Proceso" con campo nombre (placeholder "Ej. Proceso de Facturación") y selector de patrón (Simple/iForm). | ✅ PASS |
| 4.2 | Ingresar "UAT_Proceso_Harold" | Campo acepta el texto | ✅ Nombre "UAT_Proceso_Harold" ingresado. Patrón "Simple" seleccionado (verde). | ✅ PASS |
| 4.3 | Click en "Crear y Diseñar Proceso" | Canvas BPMN renderiza con StartEvent | ✅ Canvas renderiza con StartEvent (círculo). URL actualiza a `/admin/modeler/bpmn?processId=uatprocesoharold`. Toolbar de 5 fases visible: Inicio, Modelado, Simulación, Trazabilidad, Despliegue. Título "UAT_Proceso_Harold" con badges BORRADOR + SANDBOX. Panel Camunda Properties muestra: Nombre de Negocio, ID Técnico, SLA Global (72h), History TTL (180d). **Linter activo (CA-77)** advierte "El diagrama debe contener al menos un Evento de Fin (EndEvent)". | ✅ PASS |
| 4.4 | Click en el StartEvent | StartEvent se selecciona con panel de propiedades | ✅ StartEvent seleccionado con menú contextual de elementos BPMN (UserTask, Gateway, EndEvent, etc.). Panel de propiedades muestra: Nombre del Evento, Id de Evento (StartEvent_1), y sección **"FormKey (Start Event)"** con dropdown "Sin FormKey". | ✅ PASS |

- **Observaciones**:
  - El dropdown **FormKey** ya es visible en el StartEvent — esto confirma que la infraestructura para CA-39 está presente y será verificada en Misión 5
  - El Linter de gobernanza (CA-77) detecta automáticamente advertencias estructurales — buen diseño defensivo
  - Existen procesos previos reales en "Procesos Recientes" (Datos, Datos2) — confirma persistencia real
- **Bugs descubiertos**: Ninguno
- **Evidencia**: 4 capturas de Harold (modal vacío, modal con nombre, canvas BPMN, StartEvent seleccionado)
- **Veredicto de misión**: ✅ **PASS** — Diseñador BPMN carga, canvas renderiza, elementos interactivos, propiedades visibles.

---

### Misión 5: Dropdown FormKey — MISIÓN CRÍTICA (CA-39 / CA-40) ⭐
- **Estado**: ⏳ PENDIENTE

---

### Misión 6: Persistencia de la Vinculación
- **Estado**: ⏳ PENDIENTE

---

### Misión 7: Verificación RBAC
- **Estado**: ⏳ PENDIENTE

---

## Bugs Descubiertos

| # | ID | Tipo | Severidad | Misión | Descripción | Causa Raíz | Estado |
|---|-----|------|-----------|--------|-------------|------------|--------|
| 1 | BUG-J02-001 | Bug Funcional | P3 (Baja) | M1 | Ruta `/admin/modeler/` retorna 404. No existe ruta padre ni redirect para el módulo Modelador. | `frontend/src/router/index.ts` — Faltaba ruta padre. | ✅ **CERRADO** — Fix en commit `ef18729d`. Redirect `/admin/modeler` → `/admin/modeler/bpmn` agregado. |
| 2 | BUG-J02-002 | Bug Funcional | P3 (Baja) | M1 | En `BpmnDesigner.vue`, `window.open()` genera link a `/admin/modeler?processId=...` causando 404. | `frontend/src/views/admin/Modeler/BpmnDesigner.vue:4197` | ✅ **CERRADO** — Fix en commit `ef18729d`. Link corregido a `/admin/modeler/bpmn?processId=...` |
| 3 | **BUG-J02-003** | **🔴 Bug de Mock** | **P0 (BLOQUEANTE)** | M2 | El catálogo de formularios mostraba datos HARDCODEADOS. `FormDirectoryService.java` tenía variable `mockDirectory` con 3 forms fijos en memoria. | `backend/.../service/form/FormDirectoryService.java:13-17` — `mockDirectory` eliminado, ahora usa `FormDesignService.listarCatalogo()` para consultar PostgreSQL. | ✅ **CERRADO** — Fix en commit `ef18729d`. Zero mocks confirmado por Arquitecto. |

---

## Validación de Contratos API

| Endpoint Esperado | Contrato (API_CONTRACTS.md) | Comportamiento Real | Discrepancia |
|---|---|---|---|
| `GET /api/v1/forms` | Debe retornar formularios activos desde PostgreSQL | Retorna 3 formularios hardcodeados desde `FormDirectoryService.mockDirectory` (en memoria) | 🔴 **DISCREPANCIA CRÍTICA** — El endpoint existe pero sirve datos mock, no datos reales de la BD |
| `GET /api/v1/forms/active` | Retorna formularios activos reales (usado por BPMN dropdown) | SÍ consulta la BD real vía `FormDesignService.listarCatalogo()` | ⚠️ Inconsistencia: este endpoint funciona correctamente pero NO es usado por `FormList.vue` |

---

## Evidencia Técnica

_Se irá adjuntando conforme Harold reporte resultados de cada misión._

---

## Línea de Tiempo Git

_Pendiente: se recopilará al cierre de la certificación con `git log`._

---

## Veredicto Global

| Veredicto | Justificación |
|-----------|---------------|
| ⏳ **PENDIENTE** | Certificación en curso. No se han ejecutado misiones aún. |
