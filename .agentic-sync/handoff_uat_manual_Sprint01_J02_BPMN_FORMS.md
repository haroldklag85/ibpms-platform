# Handoff: Certificación UAT Manual — Sprint PM-01, Journey J-02 (BPMN + Forms)

## Metadata
- **Tipo:** Certificación UAT Manual (HCT — Human Certification Testing)
- **Workflow de referencia:** `.agent/workflows/WORKFLOW_CERTIFICACION_MANUAL.md` v2
- **Sprint:** PM-01 (Fase de Estabilización)
- **Journey:** J-02 (Diseño BPMN y Formularios)
- **US cubiertas:** US-005 (BPMN Designer), US-003 (Form Designer/Catalog)
- **CAs foco principal:** CA-39 (Dropdown FormKey con formularios activos reales), CA-40 (Filtro por patrón Simple vs Maestro)
- **Rama:** DevDavid
- **Tester Humano:** Harold
- **Arquitecto Líder:** Aprobado

## Contexto del Sprint Cerrado
En esta iteración del Sprint PM-01 se ejecutaron los siguientes commits reales en la rama `DevDavid`:

| Commit | Descripción | Archivo Clave Modificado |
|--------|-------------|--------------------------|
| `262472f0` | fix(forms): corregir catálogo de formularios activos para binding BPMN CA-39/CA-40 | Frontend: FormList, useFormStore |
| `1763d8b5` | fix(bpmn): eliminar mock fallback y corregir carga de formularios en dropdown CA-39/CA-40 | Frontend: BpmnDesigner.vue |
| `be875759` | fix(e2e): Fix flaky welcome modal test and configure docker teardown | Frontend: e2e/certification/, playwright.config.ts |

## Alcance de la Certificación UAT
El Agente QA debe guiar al Tester Humano (Harold) por un recorrido milimétrico de las siguientes funcionalidades **REALES y FÍSICAS** del sistema:

### Misión 0: Precondiciones de Infraestructura
**Objetivo:** Garantizar que el entorno local está levantado y operativo.

| Paso | Acción | Verificación Esperada |
|:----:|--------|----------------------|
| 0.1 | Verificar Docker: `docker ps` | Deben aparecer `ibpms-postgres-uat` (puerto 5433), `ibpms-redis-uat` (puerto 6379), `ibpms-rabbitmq-uat` (puerto 5672) — todos con status `Up (healthy)` |
| 0.2 | Verificar Backend: `curl -s http://localhost:8080/actuator/health` | Debe responder `{"status":"UP"}` |
| 0.3 | Verificar Frontend: Abrir `http://localhost:5173` en el navegador | Debe cargar la página de Login (`/login`) con el formulario de autenticación |

### Misión 1: Login y Navegación Base
**Objetivo:** Certificar que el usuario puede autenticarse y acceder al módulo de administración.

| Paso | Acción del Tester | Resultado Esperado | CA |
|:----:|-------------------|--------------------|----|
| 1.1 | Abrir `http://localhost:5173/login` | Se muestra la vista de Login (`Login.vue`) con campos email y password |  |
| 1.2 | Ingresar credenciales válidas de administrador y hacer click en "Iniciar Sesión" | Redirige al Portal (`/`) o al Workdesk (`/workdesk`). El menú lateral de navegación debe ser visible | |
| 1.3 | En el menú lateral, buscar y hacer click en la sección de "Modelador" o "Diseñador" que lleve a las rutas `/admin/modeler/*` | Debe navegar correctamente sin mostrar error 404 |  |

### Misión 2: Catálogo de Formularios (US-003)
**Objetivo:** Certificar que el catálogo de formularios funciona y muestra datos reales de la base de datos PostgreSQL.
**Ruta real:** `http://localhost:5173/admin/modeler/forms`
**Componente:** `FormList.vue`

| Paso | Acción del Tester | Resultado Esperado | CA |
|:----:|-------------------|--------------------|----|
| 2.1 | Navegar a `http://localhost:5173/admin/modeler/forms` | Se renderiza la vista de catálogo de formularios. NO debe estar vacía si hay formularios en la BD | |
| 2.2 | Abrir DevTools (F12) → Pestaña Red (Network) | Verificar que se hace una petición HTTP GET al backend (puerto 8080) para cargar la lista de formularios. La respuesta debe ser un array JSON con formularios reales |  |
| 2.3 | Verificar visualmente que cada formulario listado tiene nombre, tipo/categoría y estado | Los formularios deben ser datos reales de la BD PostgreSQL, NO datos hardcodeados | |

### Misión 3: Crear un Formulario Nuevo (US-003)
**Objetivo:** Certificar que se puede crear un formulario desde cero y que quede persistido en la BD.
**Ruta real:** `http://localhost:5173/admin/modeler/forms/designer`
**Componente:** `FormDesigner.vue` (115KB)

| Paso | Acción del Tester | Resultado Esperado | CA |
|:----:|-------------------|--------------------|----|
| 3.1 | Desde el catálogo de formularios (Misión 2), hacer click en el botón para crear un formulario nuevo o navegar directamente a `/admin/modeler/forms/designer` | Se abre el diseñador de formularios con un lienzo vacío o con opciones de configuración |  |
| 3.2 | Configurar el nombre del formulario con un nombre distintivo, ej: "UAT_Formulario_Prueba_Harold" | El campo de nombre debe aceptar el texto |  |
| 3.3 | Agregar al menos un campo al formulario (ej: un campo de texto, un select, etc.) usando la paleta de componentes del diseñador | El campo debe aparecer visualmente en el lienzo del formulario |  |
| 3.4 | Guardar el formulario usando el botón de guardar | Debe aparecer una notificación de éxito (toast/tostada). Verificar en DevTools (Red) que se envió un POST/PUT al backend y la respuesta fue exitosa (200/201) |  |
| 3.5 | Regresar al catálogo de formularios (`/admin/modeler/forms`) | El formulario recién creado "UAT_Formulario_Prueba_Harold" debe aparecer en la lista | |

### Misión 4: Diseñador BPMN — Apertura y Canvas (US-005)
**Objetivo:** Certificar que el Diseñador BPMN carga correctamente, que el modal de bienvenida funciona, y que el canvas BPMN se renderiza.
**Ruta real:** `http://localhost:5173/admin/modeler/bpmn`
**Componente:** `BpmnDesigner.vue` (222KB)

| Paso | Acción del Tester | Resultado Esperado | CA |
|:----:|-------------------|--------------------|----|
| 4.1 | Navegar a `http://localhost:5173/admin/modeler/bpmn` | La vista del diseñador BPMN comienza a cargar. Debe aparecer un modal de bienvenida pidiendo nombre del proceso (si no hay proceso cargado) |  |
| 4.2 | En el modal de bienvenida, ingresar "UAT_Proceso_Harold" en el campo de nombre del proceso | El campo acepta el texto. El input tiene placeholder "Ej. Proceso de Facturación" |  |
| 4.3 | Hacer click en "Crear y Diseñar Proceso" (o botón equivalente visible) | El modal se cierra. El canvas BPMN se renderiza mostrando al menos un StartEvent (círculo verde/vacío). El toolbar superior debe mostrar el nombre "UAT_Proceso_Harold" |  |
| 4.4 | Verificar que el canvas tiene elementos interactivos: hacer click en el StartEvent | El StartEvent se selecciona visualmente (borde azul o resaltado). Debe aparecer un panel de propiedades a la derecha o en alguna zona del diseñador |  |

### Misión 5: Agregar UserTask y Verificar Dropdown FormKey (CA-39 / CA-40) ⭐ MISIÓN CRÍTICA
**Objetivo:** Esta es la misión central. Certificar que al seleccionar un UserTask en el canvas BPMN, el dropdown de "Formulario (Ref)" o "FormKey" muestra formularios activos REALES cargados desde la BD PostgreSQL, y que el filtro Simple/Maestro funciona.

| Paso | Acción del Tester | Resultado Esperado | CA |
|:----:|-------------------|--------------------|----|
| 5.1 | En el canvas BPMN, usar la paleta de herramientas para agregar un elemento "User Task" (tarea de usuario) al diagrama | Un rectángulo de UserTask aparece en el canvas con un ícono de persona |  |
| 5.2 | Hacer click en el UserTask recién creado para seleccionarlo | El UserTask se resalta. El panel de propiedades se actualiza mostrando las propiedades específicas del UserTask |  |
| 5.3 | En el panel de propiedades, buscar el campo/dropdown etiquetado como "Formulario (Ref)", "FormKey", "Form Reference" o similar | Debe existir un dropdown o selector para vincular un formulario. **Si NO existe el dropdown → reportar como BUG BLOQUEANTE** | CA-39 |
| 5.4 | Hacer click en el dropdown de FormKey para desplegar las opciones | El dropdown debe mostrar una lista de formularios reales cargados desde la BD. Los nombres deben coincidir con los formularios que viste en el catálogo (Misión 2). El formulario "UAT_Formulario_Prueba_Harold" (creado en Misión 3) DEBE aparecer aquí. **Si el dropdown está vacío o muestra datos hardcodeados → BUG BLOQUEANTE (Bug de Mock)** | CA-39 |
| 5.5 | Abrir DevTools (F12) → Red → Verificar la petición HTTP que cargó los formularios al abrir el dropdown | Debe haber una petición GET al backend (puerto 8080) que retorna un array JSON con los formularios activos. La URL debe apuntar a un endpoint del backend, NO a un archivo local | CA-39 |
| 5.6 | Si el dropdown tiene filtro o categorías (Simple / Maestro), probar a filtrar | Al filtrar por "Simple" solo deben aparecer formularios de tipo Simple. Al filtrar por "Maestro" solo deben aparecer formularios de tipo Maestro. **Si no hay filtro visible, documentar como observación (NO como bug — el filtro puede estar implementado de otra forma)** | CA-40 |
| 5.7 | Seleccionar un formulario del dropdown (ej: "UAT_Formulario_Prueba_Harold") | El dropdown se cierra. El nombre del formulario seleccionado debe quedar visible en el campo. El panel de propiedades debe reflejar la vinculación |  |

### Misión 6: Persistencia de la Vinculación
**Objetivo:** Verificar que la vinculación FormKey→UserTask persiste después de guardar y recargar.

| Paso | Acción del Tester | Resultado Esperado | CA |
|:----:|-------------------|--------------------|----|
| 6.1 | Con el formulario vinculado al UserTask (Misión 5), hacer click en "Guardar Borrador" (💾) en la barra superior | Debe aparecer notificación de guardado exitoso. Verificar en DevTools que se envió un PUT/POST al backend |  |
| 6.2 | Recargar la página completa (F5 o Ctrl+R) | El diseñador BPMN debe recargar el proceso "UAT_Proceso_Harold". Si aparece el modal de bienvenida, puede significar que el proceso no se persistió correctamente → documentar como observación |  |
| 6.3 | Si el proceso recargó correctamente, seleccionar el UserTask nuevamente | El campo FormKey debe mostrar el formulario previamente vinculado ("UAT_Formulario_Prueba_Harold"). **Si está vacío después de recargar → reportar como BUG funcional de persistencia** |  |

### Misión 7: Verificación de Seguridad (RBAC)
**Objetivo:** Verificar que las rutas del Modelador están protegidas por roles.

| Paso | Acción del Tester | Resultado Esperado | CA |
|:----:|-------------------|--------------------|----|
| 7.1 | Cerrar sesión (Logout) | Regresa a `/login` |  |
| 7.2 | Intentar navegar directamente a `http://localhost:5173/admin/modeler/bpmn` sin estar autenticado | Debe redirigir a `/login` o mostrar error 404 (Security by Obscurity — el sistema muestra 404 falso en lugar de 403, esto es comportamiento diseñado según `RouteGuards.ts`) |  |

## Reglas de Evidencia (del Workflow CERTIFICACION_MANUAL v2)
- 📸 **Capturas de pantalla** para cada paso crítico (Misiones 4 y 5 son obligatorias)
- 📋 **Logs de Network** (DevTools F12 → Red) para las peticiones HTTP al backend en Misiones 2, 3, 5
- Todo hallazgo se clasifica como: Bug Funcional / Bug de Contrato / Bug de Mock / Bug de Cadena
- **Un Bug de Mock es BLOQUEANTE P0** — la US no puede certificarse si se detectan mocks

## Archivos de Referencia (todos existen físicamente)
- Journey J-02 completo: `docs/uat/casos_uso_uat_j02.md`
- Épica US-005: `docs/requirements/epics/epic_B_formularios_bpmn.md` (US-005 comienza en línea ~1275)
- Contratos API: `docs/sprints/gobernanza_pm/API_CONTRACTS.md`
- Guía Sprint 01: `docs/sprints/gobernanza_pm/SPRINT_01_GUIA_EJECUCION.md`
- Workflow de certificación: `.agent/workflows/WORKFLOW_CERTIFICACION_MANUAL.md`

## Veredicto Esperado
Al finalizar todas las misiones, el Agente QA debe emitir un veredicto:
- **PASS** — Todo OK, sin bugs
- **PASS CON OBSERVACIONES** — Bugs no bloqueantes encontrados
- **FAIL** — Bugs bloqueantes (P0/P1 o mocks detectados)
