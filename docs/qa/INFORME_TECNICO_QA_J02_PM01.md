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
- **Estado**: 🔴 FAIL — Bug de Mock P0 BLOQUEANTE
- **Fecha/Hora**: 2026-06-24 19:58 COT
- **Pasos**:

| Paso | Acción | Resultado Esperado | Resultado Real | Veredicto |
|:----:|--------|-------------------|----------------|:---------:|
| 2.1 | Navegar a `/admin/modeler/forms` | Catálogo renderiza con formularios reales de PostgreSQL | ⚠️ Catálogo renderiza y muestra 3 formularios, PERO Harold reporta que **no corresponden a los formularios reales en la BD**. Los forms mostrados son datos hardcodeados en el backend | ⚠️ FAIL |
| 2.2 | Verificar petición HTTP GET en DevTools (F12 → Red) | Petición GET al backend con respuesta JSON de formularios reales | ❌ **No se logra evidenciar la URL que obtiene los forms ni GET del backend** en la pestaña Network. Las peticiones visibles son solo cargas de scripts/recursos estáticos | 🔴 FAIL |
| 2.3 | Verificar que los datos son reales (no hardcodeados) | Formularios con datos reales de PostgreSQL | ❌ **Los datos son HARDCODEADOS**. Harold confirma: "se presume que los datos visualizados son datos hard-core, se tiene forms reales que no se visualizan" | 🔴 FAIL |

- **Investigación Forense del Código** (confirmada por análisis del código fuente):
  - **El frontend está LIMPIO** — `FormList.vue` (L147-158) hace una llamada real vía `integrationStore.get('/forms')` → `apiClient.get()` → `GET /api/v1/forms`
  - **El BUG está en el BACKEND** — `FormDirectoryService.java` (L13-17) tiene una variable llamada literalmente `mockDirectory` con 3 formularios hardcodeados en memoria
  - **Comentario del desarrollador en L12:** *"Estructura en memoria según requerimiento de Misión (Evasión de BD compleja para acelerar Boot)"*
  - **Datos hardcodeados confirmados:**
    - `FRM-001` → "Solicitud de Crédito Express" (author: System)
    - `FRM-002` → "Alta de Empleado (Onboarding)" (author: Admin)
    - `FRM-003` → "Reclamación Seguro (PQR)" (author: AuditAgent)
  - **No se ejecuta NINGUNA consulta SQL** a PostgreSQL en `FormDirectoryService.searchForms()`
  - **INCONSISTENCIA DETECTADA:** Existe un endpoint SEPARADO `GET /api/v1/forms/active` (en `FormCatalogController.java`) que SÍ consulta la BD real vía `FormDesignService.listarCatalogo()`. Este endpoint es usado por el dropdown BPMN (Misión 5), pero NO por el catálogo `FormList.vue`.
- **Bugs descubiertos**: BUG-J02-003 (P0 — Bug de Mock BLOQUEANTE)
- **Evidencia**: 2 capturas de Harold (DevTools Network, catálogo con datos hardcodeados)
- **Veredicto de misión**: 🔴 **FAIL** — US-003 NO puede certificarse con datos mock. La variable `mockDirectory` en el backend es evidencia irrefutable.

---

### Misión 3: Crear un Formulario Nuevo (US-003)
- **Estado**: 🟠 BLOQUEADA — Depende de resolución de BUG-J02-003
- **Justificación**: Esta misión requiere crear un formulario y verificar que aparece en el catálogo. Dado que el catálogo sirve datos hardcodeados (BUG-J02-003), no es posible verificar la persistencia real. Se pospone hasta que el Arquitecto corrija el bug.

---

### Misión 4: Diseñador BPMN — Apertura y Canvas (US-005)
- **Estado**: ⏳ PENDIENTE

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
| 1 | BUG-J02-001 | Bug Funcional | P3 (Baja) | M1 | Ruta `/admin/modeler/` retorna 404. No existe ruta padre ni redirect para el módulo Modelador. Las 5 sub-rutas (`/bpmn`, `/forms`, etc.) están definidas como hermanas planas sin padre. | `frontend/src/router/index.ts` — Las rutas L70-98 son siblings directos de `/`. No hay ruta para `admin/modeler` sin sufijo. Catch-all en L216-220 captura y muestra `NotFound404.vue` | 🟡 Abierto |
| 2 | BUG-J02-002 | Bug Funcional | P3 (Baja) | M1 | En `BpmnDesigner.vue`, un `window.open()` genera link a `/admin/modeler?processId=...` (sin sub-ruta `/bpmn`), lo que causa 404 al abrir Call Activities en nueva pestaña. | `frontend/src/views/admin/Modeler/BpmnDesigner.vue:4197` — Referencia a ruta inexistente `/admin/modeler?processId=` en lugar de `/admin/modeler/bpmn?processId=` | 🟡 Abierto |
| 3 | **BUG-J02-003** | **🔴 Bug de Mock** | **P0 (BLOQUEANTE)** | M2 | El catálogo de formularios (`FormList.vue`) muestra datos HARDCODEADOS del backend. `FormDirectoryService.java` tiene una variable `mockDirectory` con 3 formularios fijos en memoria. NO consulta PostgreSQL. Los formularios reales de la BD no aparecen en el catálogo. | **`backend/.../service/form/FormDirectoryService.java:13-17`** — Variable `mockDirectory = List.of(...)` con 3 formularios hardcodeados. Método `searchForms()` (L19-28) filtra esta lista en memoria. Comentario L12: *"Evasión de BD compleja para acelerar Boot"*. El controller `FormDirectoryController.java` (L26-30) sirve `GET /api/v1/forms` desde este mock. | 🔴 **BLOQUEANTE — Requiere Handoff al Arquitecto** |

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
