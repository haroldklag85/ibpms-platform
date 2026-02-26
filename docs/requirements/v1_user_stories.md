# Historias de Usuario (BDD / Gherkin) - iBPMS V1

Este documento contiene las Historias de Usuario formales para el MVP Táctico (V1). Todas las historias aquí redactadas se restringen **estrictamente** al bloque "MUST HAVE" y "SHOULD HAVE" definido en `v1_moscow_scope_validation.md`. 
*Cualquier funcionalidad relacionada con IA Agentic o Módulos Verticales (RAG, Scraping, OCR) queda explícitamente fuera de esta versión.*

---

## ÉPICA 1: Orquestación y Workbenches (El Motor Core)
Esta épica aborda la capacidad fundamental del sistema: recibir un requerimiento, enrutarlo como una tarea (Task) y permitir que el usuario la gestione en su Bandeja Unificada (Inbox).

### US-001: Obtener Tareas Pendientes en la Bandeja Unificada (Inbox)
**Como** Analista / Usuario de Negocio
**Quiero** visualizar una lista consolidada de mis tareas pendientes (BPMN o Kanban) al ingresar a la plataforma
**Para** saber exactamente qué gestiones operativas debo priorizar y resolver hoy.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Unified Inbox Loading
  Scenario: Un usuario autenticado solicita sus tareas pendientes
    Given que el usuario "juan.perez" ha iniciado sesión exitosamente con el rol "Analista_Legal"
    And existen 3 tareas activas asignadas a él y 2 tareas asignadas al grupo "Analista_Legal" en la base de datos
    When el cliente frontend realiza una petición GET a "/api/v1/workbox/pending?page=1&size=50&sort=sla_asc"
    Then el sistema debe retornar un HTTP STATUS 200 OK
    And el payload JSON debe contener un arreglo de objetos tipo "Task" bajo el nodo "content" (Paginación Spring)
    And cada objeto "Task" debe incluir "id" (UUID), "titulo" (String), "fecha_vencimiento_sla" (ISO-8601), "origen" (BPMN o Kanban) y "estado"
```
**Trazabilidad UX:** Wireframes Pantalla 1 (Bandeja Unificada).

---

### US-002: Reclamar una Tarea de Grupo (Claim Task)
**Como** Analista / Usuario de Negocio
**Quiero** poder "reclamar" (asignarme) una tarea que actualmente pertenece a la cola de todo mi grupo
**Para** evitar que otro compañero trabaje en el mismo caso de forma paralela y duplicar esfuerzos.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Task Claiming
  Scenario: Reclamar una tarea exitosamente
    Given una tarea con ID "TK-099" cuyo estado es "UNASSIGNED" y pertenece al grupo "Soporte_IT"
    And el usuario "maria.lopez" pertenece al grupo "Soporte_IT"
    When "maria.lopez" realiza un POST a "/api/v1/workbox/tasks/TK-099/claim"
    Then el sistema debe retornar un HTTP STATUS 200 OK
    And el "assignee" de la tarea debe actualizarse a "maria.lopez" en la tabla 'ACT_RU_TASK' (Camunda DB)
    And la tarea debe desaparecer de la vista "Tareas de Grupo" de los demás usuarios instantáneamente (WebSockets/Polling)

  Scenario: Intento de reclamar una tarea ya asignada a otro (Condición de Carrera)
    Given la tarea "TK-099" ya fue reclamada por un colega 1 segundo antes
    When "maria.lopez" realiza un POST a "/api/v1/workbox/tasks/TK-099/claim"
    Then el sistema debe retornar un HTTP STATUS 409 Conflict
    And el mensaje de error debe indicar "La tarea ya fue asignada a otro usuario."
```
**Trazabilidad UX:** Wireframes Pantalla 1 (Botón: Asignarme Tarea / Claim).

---

## ÉPICA 2: Generación Dinámica de Formularios (Data to JSON)
Aborda la capacidad para abstraer la UI del código, permitiendo que las tareas rendericen formularios inyectables según su configuración.

### US-003: Completar una Tarea con Payload de Formulario JSON
**Como** Analista / Usuario de Negocio
**Quiero** diligenciar la información requerida de mi caso en una vista estructurada y enviarlos
**Para** finalizar exitosamente la actividad y que el motor continúe al siguiente paso del flujo.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Task Completion with Form Data
  Scenario: Enviar datos válidos de formulario
    Given la tarea "TK-100" asignada a "carlos.ruiz" requiere el formulario "Form_Aprobacion_V1"
    And "Form_Aprobacion_V1" exige el campo obligatorio numérico "monto_aprobado"
    When "carlos.ruiz" realiza un POST a "/api/v1/workbox/tasks/TK-100/complete"
    And incluye en el body el JSON '{"variables": {"monto_aprobado": 1500, "comentarios": "Ok"}}'
    Then el sistema debe retornar un HTTP STATUS 200 OK
    And la tarea "TK-100" marca su estado como "COMPLETED"
    And las variables del JSON se persisten asociadas a la instancia del proceso

  Scenario: Enviar datos inválidos (Violación del JSON Schema)
    Given la tarea "TK-100" requiere el campo obligatorio "monto_aprobado" numérico
    When "carlos.ruiz" realiza un POST a "/api/v1/workbox/tasks/TK-100/complete"
    And incluye un JSON vacío '{"variables": {}}'
    Then el sistema valida el payload contra el JSON Schema registrado para "Form_Aprobacion_V1"
    And el sistema debe retornar un HTTP STATUS 400 Bad Request
    And el error format JSON debe especificar `{"error": "ValidationFailed", "fields": [{"field": "monto_aprobado", "message": "Required"}]}`
```
**Trazabilidad UX:** Wireframes Pantalla 2 (Vista de Detalle / Formulario Dinámico).

---

## ÉPICA 3: Inicio y Recepción (Triggers)
Capacidad de iniciar procesos operacionales tanto de forma manual (Pantalla 0) como reactiva (Webhook).

### US-004: Iniciar un Proceso mediante Webhook (Plugin O365 Listener)
**Como** Sistema (APIM / MS Graph / Webhook)
**Quiero** inyectar un payload automatizado a un Endpoint público de la plataforma
**Para** instanciar un caso de negocio nuevo automáticamente sin intervención manual humana.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Process Instantiation via Webhook
  Scenario: Webhook O365 válido inicia proceso de Onboarding
    Given que la definición de proceso BPMN "onboarding_cliente" está activa
    When se recibe un POST en el APIM a "/api/v1/webhooks/trigger/o365_inbox"
    And el payload contiene un "id_mensaje_correo" valido
    Then el sistema debe retornar HTTP STATUS 201 Created
    And debe retornar el "process_instance_id" del nuevo caso
    And el motor BPMN arranca en la primera tarea de configuración del proceso
    And las variables del payload inicial quedan inyectadas al entorno del proceso
```
**Trazabilidad UX:** Wireframes Pantalla 11 (Hub de Integraciones: Eventos Entrantes).

---

## ÉPICA 4: Diseño de Procesos (BPMN) y Estructuración de Proyectos
Esta épica aborda el rol del Arquitecto/Administrador para modelar cómo fluye el trabajo, ya sea mediante un diagrama BPMN estricto o un esqueleto de Proyecto por Fases.

### US-005: Desplegar y Versionar un Modelo de Proceso (BPMN)
**Como** Arquitecto de Procesos
**Quiero** importar un archivo `.bpmn` (BPMN 2.0 XML) generado en el Diseñador Web y desplegarlo en el motor
**Para** que la plataforma sepa cómo enrutar las tareas secuenciales, paralelas y compuertas lógicas de mi proceso oficial.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: BPMN Process Deployment
  Scenario: Despliegue exitoso de un diagrama BPMN válido
    Given que el usuario Arquitecto ha diseñado el flujo "Aprobacion_Credito_v2.bpmn"
    When el usuario sube el archivo invocando un POST multipart/form-data a "/api/v1/design/processes/deploy"
    Then el motor (Ej. Camunda) debe validar la sintaxis XML del archivo
    And el sistema debe retornar HTTP STATUS 201 Created
    And el sistema debe generar una nueva "Version" del "Process Definition ID" (Ej. Credito:2)
    And las nuevas instancias usarán esta versión sin afectar a las que ya estaban "En Vuelo" (In-Flight)

  Scenario: Intento de despliegue con diagrama inválido (BPMN Roto)
    Given un archivo "Proceso_Roto.bpmn" al que le falta un "End Event" necesario
    When el usuario realiza el POST a "/api/v1/design/processes/deploy"
    Then el motor debe denegar el despliegue
    And el sistema debe retornar HTTP STATUS 422 Unprocessable Entity
    And el payload debe contener el mensaje parseado: "El diagrama no es instanciable. Falta End Event."
```
**Trazabilidad UX:** Wireframes Pantalla 6 (Diseñador BPMN Integrado / CRUD de Procesos).

---

### US-006: Diseñar la Estructura (WBS) de un Proyecto (Templates)
**Como** Director de Proyectos / Administrador
**Quiero** crear una Plantilla de Proyecto definiendo Fases (Sprints/Etapas) y pre-asignando Formularios a tareas genéricas
**Para** que cuando el negocio inicie un proyecto real, se instancie automáticamente toda la estructura y la gente sepa qué formularios debe llenar en cada fase.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Project Template Builder
  Scenario: Creación de un esqueleto de proyecto (WBS) con hitos y formularios mapeados
    Given que el administrador está configurando el "Template de Apertura de Tienda"
    When el usuario envía un POST a "/api/v1/design/projects/templates"
    And el body JSON incluye 2 Fases ("Pre-Obra", "Obra") y 1 Tarea ("Presupuesto") en la Fase 1, enlazada al formulario "Form_Presupuesto"
    Then el sistema debe retornar HTTP STATUS 201 Created
    And el "Project Template ID" debe guardarse en la BD listo para ser instanciado por el usuario operativo
```
**Trazabilidad UX:** Wireframes Pantalla 8 (Project Builder).

---

## ÉPICA 5: Modelado de Reglas de Negocio con IA (DMN)
Permite a los usuarios de negocio (no técnicos) generar reglas lógicas complejas utilizando lenguaje natural.

### US-007: Generar tabla DMN desde lenguaje natural (NLP)
**Como** Arquitecto de Procesos / Usuario de Negocio
**Quiero** escribir una regla de negocio en lenguaje natural (ej. "Si el reclamo es mayor a 1000, rechazar")
**Para** que la IA la traduzca instantáneamente a una tabla DMN (JSON/XML) sin tener que aprender la notación técnica.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: NLP to DMN Translation via LLM API
  Scenario: Traducción exitosa de lenguaje natural a tabla DMN
    Given que el usuario está en el Taller DMN con IA
    When el usuario envía una petición POST a "/api/v1/ai/dmn/translate"
    And incluye el prompt "Si el monto es mayor a 500, requiere 'Aprobacion_Gerente', sino 'Aprobacion_Lider'"
    Then el sistema debe retornar HTTP STATUS 200 OK
    And el payload debe contener el string literal del XML DMN validado y parseable por el motor
    And el DMN debe contener las entradas (Inputs) y salidas (Outputs) inferidas correctamente
```
**Trazabilidad UX:** Wireframes Pantalla 4 (Taller DMN asitido con IA).

---

## ÉPICA 6: Gestión Ágil y Kanban
Habilita el trabajo no estructurado dentro de la plataforma para proyectos que no requieren diagramas BPMN deterministas.

### US-008: Mover Tarjeta en Tablero Kanban (Cambio de Estado)
**Como** Ejecutor / Analista
**Quiero** arrastrar una tarjeta de un estado a otro en mi tablero (Ej. de "Haciendo" a "Hecho")
**Para** actualizar el estatus de mi trabajo sin navegar por múltiples pantallas.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Kanban Board Task Management
  Scenario: Mover tarjeta de una columna a otra
    Given el tablero del proyecto "Implementación ERP" con columnas "TODO", "DOING", "DONE"
    And la tarea Kanban "KT-050" está en estado "TODO"
    When el usuario realiza un PATCH a "/api/v1/projects/kanban/tasks/KT-050/status"
    And el payload es '{"new_status": "DOING"}'
    Then el sistema debe retornar HTTP STATUS 200 OK
    And actualizar el timestamp de "last_modified" en la tabla 'ibpms_kanban_tasks'
    And el payload de respuesta debe retornar el objeto completo serializado `{ "id": "KT-050", "status": "DOING", "version": 2 }`
    And la UI debe propagar el evento vía WebSockets para que la tarea "KT-050" se refleje en la columna "DOING" para los demás miembros del equipo conectadas al tablero
```
**Trazabilidad UX:** Wireframes Pantalla 3 (Tableros de Proyecto Kanban).

---

## ÉPICA 7: Dashboards y Reportería Operativa (BAM)
Exposición de la salud de los procesos en vuelo para la toma de decisiones gerenciales.

### US-009: Visualizar Salud del Proceso (BAM Dashboard)
**Como** Líder de Operaciones / Gerente
**Quiero** acceder visualizar un dashboard analítico con el conteo de tareas retrasadas y volúmenes operativos
**Para** identificar cuellos de botella y reasignar cargas de trabajo en tiempo real.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Process Health Analytics
  Scenario: Renderizado exitoso del Dashboard de Grafana
    Given un usuario autenticado con Rol "Gerente_Operaciones"
    When la aplicación frontend solicita renderizar el iframe interactivo en la Pantalla 5
    Then el API Gateway debe emitir un JWT de corta duración (Grafana Auth Proxy) con rol de "Viewer"
    And el iframe debe renderizar correctamente el tablero pasándole variables de entorno `&var-TenantID=T123`
    And el dashboard debe mostrar obligatoriamente un panel de "Tareas Vencidas por SLA" consultando la vista materializada `vw_task_sla_breach`
```
**Trazabilidad UX:** Wireframes Pantalla 5 (Dashboards y Panel de Control - BAM).

---

## ÉPICA 8: Generador Documental Jurídico (SGDEA)
*(SHOULD HAVE)* - Producción controlada de artefactos legales a partir del estado final de un caso.

### US-010: Generar y Descargar PDF a partir de datos del caso
**Como** Analista / Gestor Documental
**Quiero** que el sistema ensamble un PDF inmutable (Ej. un Contrato) con los datos finales del caso
**Para** enviarlo a firma o entregarlo al cliente sin errores de "copy-paste".

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Legal PDF Generation from Template
  Scenario: Ensamblar PDF usando plantilla del SGDEA y Variables de la Instancia
    Given la instancia de proceso "PI-888" finalizada en estado "APPROVED"
    And la plantilla "Contrato_Laboral_V3.docx" almacenada en el repositorio
    When el usuario realiza un POST a "/api/v1/documents/generate/PI-888"
    And especifica '{"template_id": "Contrato_Laboral_V3"}'
    Then el motor Documental (FOP/PDFBox) debe resolver el mapeo inyectando el árbol `json_variables` de la instancia "PI-888" en las etiquetas `<<key>>` de la plantilla
    And el sistema debe registrar el checksum SHA-256 del archivo generado en `ibpms_audit_log` para inmutabilidad legal
    And el sistema retorna HTTP STATUS 200 OK con un link "Signed URL" (AWS S3 Presigned o Azure Blob SAS) expirable en 15 minutos para la descarga del archivo `Contrato_Laboral_V3_PI-888.pdf`
```
**Trazabilidad UX:** Wireframes Pantalla 12 (Bóveda Documental y Generación).

---

## ÉPICA 9: M365 Copilot & Bandeja Docketing (Transicionado a MVP V1)
*(Esta épica fue pivotada de V2 a V1 para garantizar el Product-Market Fit como plataforma AI-First).*

### US-011: Filtrado Transversal en Bandeja Avanzada (Docketing)
**Como** Analista Legal / Supervisor de Operaciones
**Quiero** filtrar mi bandeja de entrada estructurada (Pantalla 1B) mediante dropdowns relacionales ("Cliente", "Proyecto", "Rango de Fechas") y etiquetas booleanas de actividad ("Acuses", "Tareas Creadas")
**Para** localizar rápidamente eventos críticos o cargas de trabajo asociadas a cuentas clave sin abrir cada correo individualmente.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Advanced Relational Inbox Filtering
  Scenario: Filtrado compuesto determinista (Cliente + Proyecto)
    Given el usuario autenticado está navegando la Bandeja Avanzada (Pantalla 1B)
    And hay 500 ítems en la bandeja, de los cuales 5 pertenecen al Cliente "Global Tech" y el Proyecto "Patente-XZ"
    When el usuario selecciona "Global Tech" en el selector 'Filtro Cliente'
    And el usuario selecciona "Patente-XZ" en el selector 'Filtro Proyecto'
    Then el API del Backend debe ejecutar una query cruzada contra 'ibpms_metadata_index'
    And el Frontend debe renderizar exclusivamente los 5 ítems exactos en menos de 1 segundo (Paginado)
    And la UI debe mostrar un estado "Empty State" si la combinación no retorna resultados

  Scenario: Filtrado por Label Booleano generado por IA (Acuses)
    Given la bandeja contiene ítems marcados por la IA con el boolean flag 'is_acknowledgment_sent: true'
    When el usuario marca el checkbox "Actividad: Acuse Enviado"
    Then el sistema debe ocultar todos los correos donde 'is_acknowledgment_sent: false' o nulo
```
**Trazabilidad UX:** Prototipo UI2.html / Pantalla 1B.

---

### US-012: Extracción de Intención y Enriquecimiento CRM Bilingüe (Copiloto AI)
**Como** Gestor de Casos (Human-in-the-Loop)
**Quiero** que al abrir un ticket/correo (Pantalla 2C), la IA ya haya procesado el texto para clasificar la intención, inferir el cliente cruzando el dominio con el CRM interno, y proponer un borrador en el idioma original del correo
**Para** tener contexto operacional inmediato sin necesidad de navegar al CRM ni traducir la petición manualmente.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: AI Semantic Extraction and Draft Generation
  Scenario: Extracción de CRM y generación de Borrador Bilingüe (Inglés)
    Given que el listener de O365 recibe un correo en Inglés ("We are very angry about the downtime...") desde "cto@bancoalpha.com"
    And el demonio Backend-AI intercepta el payload antes de mostrarlo en el Inbox
    When la IA analiza el texto del correo
    Then el sistema debe extraer el dominio "bancoalpha.com" y consultar la API del CRM ONS
    And debe etiquetar el correo con 'Cliente: Banco Alpha', 'Sentimiento: Severo/Negativo'
    And el LLM debe generar un borrador de disculpa formal estandarizado estrictamente en Inglés (detectando el idioma base)
    And el borrador debe guardarse temporalmente en caché y renderizarse en el "Panel Recomendado de IA" (Pantalla 2C) bloqueado hasta revisión

  Scenario: Identificación de Fechas Clave (Deadlines OCR/NLP)
    Given el correo contiene el string literal "We grant you an extension until Nov 30, 2024"
    When la IA analiza el texto
    Then la IA debe generar una tarjeta atómica de "Sugerencia: Cambiar SLA" con el payload '{"new_deadline": "2024-11-30T23:59:59Z"}'
```
**Trazabilidad UX:** Prototipos UI1.html y UI4.html / Pantalla 2C.

---

### US-013: Orquestación Asistida (Aceptación/Rechazo Human-in-the-Loop)
**Como** Analista Legal / Supervisor de Operaciones
**Quiero** revisar atómicamente cada sugerencia construida por la IA (Fechas, Borradores, Recomendación de Creación de Proyecto) usando botones explícitos de "Aceptar" o "Rechazar"
**Para** mantener el control absoluto sobre las acciones ejecutadas, garantizando el cumplimiento de políticas corporativas y alimentando el sistema MLOps con mis rechazos.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Human-in-the-Loop Orchestration
  Scenario: Aprobación de Borrador y Envío Síncrono MS Graph API
    Given el analizador IA propuso un borrador de respuesta
    And el usuario se encuentra leyendo el detalle del Inbox (Pantalla 2C)
    When el usuario modifica manualmente 3 palabras del borrador
    And el usuario presiona el botón [🚀 Aprobar y Enviar Respuesta]
    Then el backend debe actualizar el payload final
    And debe consumir la MS Graph API (/users/{id}/sendMail) asumiendo la identidad corporativa
    And el caso se marca como "CERRADO" en la bandeja del usuario

  Scenario: Rechazo de Tarjeta Atómica (Feedback RAG)
    Given la IA recomendó "Crear Tarea: Iniciar Devolución de Dinero"
    When el usuario presiona [❌ Rechazar] en esa tarjeta individual
    Then el sistema requiere que el usuario seleccione un motivo de exclusión genérico (Dropdown: "Falso Positivo", "Política no Aplica")
    And el registro de auditoría (ibpms_audit_log) guarda el flag de "IA_REJECTED" para re-entrenamiento del modelo
    And el usuario puede continuar operando la tarea manualmente sin que se bloquee el flujo
```
**Trazabilidad UX:** Prototipos UI3.html, UI1.html y UI4.html / Pantalla 2C.
