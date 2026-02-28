# Historias de Usuario (BDD / Gherkin) - iBPMS V1

Este documento contiene las Historias de Usuario formales para el MVP Táctico (V1). Todas las historias aquí redactadas se restringen **estrictamente** al bloque "MUST HAVE" y "SHOULD HAVE" definido en `v1_moscow_scope_validation.md`. 
*Cualquier funcionalidad relacionada con IA Agentic o Módulos Verticales (RAG, Scraping, OCR) queda explícitamente fuera de esta versión.*

---

## ÉPICA 1: Orquestación y Workbenches (El Motor Core)
Esta épica aborda la capacidad fundamental del sistema: recibir un requerimiento, enrutarlo como una tarea (Task) y permitir que el usuario la gestione en su Workdesk (Escritorio de Tareas).

### US-001: Obtener Tareas Pendientes en el Workdesk
**Como** Analista / Usuario de Negocio
**Quiero** visualizar una lista consolidada de mis tareas pendientes (BPMN o Kanban) al ingresar a la plataforma (Workdesk)
**Para** saber exactamente qué gestiones operativas debo priorizar y resolver hoy.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Workdesk Loading
  Scenario: Un usuario autenticado solicita sus tareas pendientes
    Given que el usuario "juan.perez" ha iniciado sesión exitosamente con el rol "Analista_Legal"
    And existen 3 tareas activas asignadas a él y 2 tareas asignadas al grupo "Analista_Legal" en la base de datos
    When el cliente frontend realiza una petición GET a "/api/v1/workbox/pending?page=1&size=50&sort=sla_asc"
    Then el sistema debe retornar un HTTP STATUS 200 OK
    And el payload JSON debe contener un arreglo de objetos tipo "Task" bajo el nodo "content" (Paginación Spring)
    And cada objeto "Task" debe incluir "id" (UUID), "titulo" (String), "fecha_vencimiento_sla" (ISO-8601), "origen" (BPMN o Kanban) y "estado"
```
**Trazabilidad UX:** Wireframes Pantalla 1 (Workdesk - Escritorio de Tareas).

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

## ÉPICA 2: IDE Web Pro-Code para Formularios (Vue 3, Zod & Dual-Pattern)
Aborda la capacidad para diseñar interfaces de usuario mediante herramientas visuales que, por debajo, compilan archivos `.vue` y esquemas de validación estrictos en lugar de JSON interpretado. También exige elegir el patrón de diseño arquitectónico del formulario.

### US-003: Instanciar y Generar un Formulario "iForm Maestro" vs "Simple"
**Como** Arquitecto Frontend / Administrador
**Quiero** elegir el tipo de formulario y ver cómo el sistema genera código Vue 3 y Zod en tiempo real mientras arrastro componentes
**Para** no tener deudas técnicas (Vendor Lock-in) y construir expedientes (iForm Maestros) que manejen etapas dinámicamente.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Web IDE Form Code Generation
  Scenario: Seleccionar Patrón de Formulario
    Given que el desarrollador crea un nuevo recurso en la sección "Formularios"
    When el modal pregunta "¿Qué arquitectura de formulario desea?"
    Then el usuario puede elegir "Patrón B: iForm Maestro (Expediente Multi-Etapa)"
    And el lienzo visual se estructura para basar el renderizado en la variable "Current_Stage" de Camunda

  Scenario: Análisis Bidireccional de Código en Tiempo Real
    Given que el usuario está en el Canvas del "iForm Maestro"
    When arrastra un "Input Text (Monto Aprobado)" y marca "Requerido"
    Then el panel derecho "Mónaco IDE" de código actualizado escribe automáticamente:
      """javascript
      const schema = z.object({ monto_aprobado: z.number().positive() })
      """
    And si el usuario borra la línea de Zod en el panel de código, el componente visual pierde instantáneamente su validación de Requerido.
```
**Trazabilidad UX:** Wireframes Pantalla 7 (IDE Web Pro-Code para Formularios).

---

### US-028: Auto-Generación de Test Suites (Zod / Jest)
**Como** Ingeniero de Calidad (QA)
**Quiero** que el diseñador de formularios exponga un botón para generar los test unitarios y e2e
**Para** asegurar en mi CI/CD que el comportamiento complejo del "iForm Maestro" no se rompa antes de compilar el frontal.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Automated Form Testing Generation
  Scenario: Descargar/Copiar Suite de Pruebas
    Given el usuario ha finalizado el diseño del "iForm Maestro_Credito.vue"
    When hace clic en el botón "[⚡ GENERADOR DE TESTS]"
    Then el IDE lee la estructura Zod
    And genera y muestra un script de prueba `.spec.ts` (Jest) que inyecta payloads intencionalmente rotos para asegurar que Zod emita un "HTTP 400 Bad Request" local en cliente.
```
**Trazabilidad UX:** Wireframes Pantalla 7 (Botones Inferiores).

### US-029: Ejecución y Envío de Formulario (iForm Maestro o Simple)
**Como** Analista / Usuario de Negocio
**Quiero** diligenciar la información de mi sección habilitada en la vista de la tarea (Pantalla 2) y presionar "Enviar"
**Para** finalizar exitosamente mi actividad y que el motor continúe al siguiente paso del proceso.

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

  Scenario: Análisis Semántico en "Pre-Flight" de un diagrama complejo (Ejecutabilidad)
    Given el Arquitecto importa un diagrama BPMN 2.0 ("Proceso_Core.bpmn") que contiene Subprocesos, Start Events de Mensaje y Tareas de Servicio
    When el usuario solicita la validación previa al despliegue ("Pre-Flight Analyze")
    Then el motor semántico debe parsear los componentes avanzados
    And identificar si alguna `ServiceTask` carece de su propiedad `Delegate Expression` (Ejecución de código)
    And identificar si alguna `UserTask` carece de una vinculación de `Form Key`
    And identificar si alguna `ExclusiveGateway` carece de un flujo por defecto (`Default Flow`)
    And el sistema debe renderizar en Pantalla 6 la lista de Errores (❌) y Advertencias (⚠️) para que el Arquitecto los corrija antes del despliegue.
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

### US-027: Copiloto IA Tutor (Auditoría BPMN 2.0 e ISO 9001)
**Como** Arquitecto Modelador de Procesos
**Quiero** un asistente IA embebido en el diseñador (Pantalla 6) que evalúe mi diagrama en tiempo real
**Para** recibir sugerencias de mejora de arquitectura, identificar antipatrones (Ej. dead-ends) y confirmar que el flujo cumple principios de calidad tipo ISO 9001.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: AI Copilot para Diseño BPMN y Auditoría ISO
  Scenario: Diagnóstico de calidad y sugerencia de ISO 9001
    Given que el Arquitecto está dibujando un proceso en el lienzo y hace clic en "Consultar a IA Copilot"
    When el sistema envía el XML en memoria y el contexto semántico al LLM API
    Then el Agente IA debe analizar la estructura devolviendo un reporte en la UI
    And destacar áreas de mejora (Ej: "La compuerta no tiene validación humana, lo cual es riesgoso bajo control ISO 9001")
    And sugerir componentes correctos de BPMN 2.0 para reemplazar antipatrones.
```
**Trazabilidad UX:** Wireframes Pantalla 6 (Diseñador BPMN).

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

### US-012: Propuesta de respuesta para correo entrante (con revisión humana)
**Como** gestor de un buzón corporativo
**Quiero** recibir un borrador de respuesta basado en el contexto del hilo y precedentes
**Para** contestar más rápido y con consistencia.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Generación de Borradores de Respuesta Interactivos
  Scenario: Sistema genera borrador bilingüe esperando revisión humana
    Given un correo electrónico entrante recibido en el buzón corporativo
    When la IA analiza el contexto y detecta el idioma (Ej: ES o EN)
    Then el sistema debe generar al menos 1 borrador de respuesta en el mismo idioma detectado
    And presentar el borrador en la interfaz bloqueando el envío automático
    And permitir al usuario "Aprobar", "Editar" o "Rechazar" el borrador
    And el sistema no debe enviar el correo hasta que el usuario ejecute una acción afirmativa
    And el sistema debe conservar un identificador de trazabilidad (conversation_id)
```
**Trazabilidad UX:** Prototipos UI1.html y UI4.html / Pantalla 2C.

---

### US-013: Identificación automática de cliente y enriquecimiento desde CRM (ONS)
**Como** gestor de un buzón corporativo
**Quiero** que el asistente identifique el cliente por el dominio del remitente y consulte el CRM ONS
**Para** contextualizar la respuesta y adaptar el tono.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Enriquecimiento CRM de Hilos de Correo
  Scenario: Match exitoso con CRM ONS
    Given un correo entrante con dominio '@cliente.com'
    When el sistema consulta la API del CRM ONS usando el dominio
    Then si existe coincidencia, asocia el correo a un 'client_profile' (account/contact)
    And el LLM ajusta el nivel de formalidad del borrador basado en la data del perfil del cliente
  
  Scenario: Cliente no identificado
    Given un correo entrante donde el dominio no existe en CRM ONS
    Then el sistema marca el correo visualmente como "Cliente no identificado"
    And sugiere una tarjeta atómica para solicitar datos o registrar al cliente nuevo en CRM
```

---

### US-014: Sugerencia de acciones (tareas) sin ejecución automática
**Como** gestor de un buzón corporativo
**Quiero** que el asistente sugiera acciones asociadas al correo (crear tarea, asignar responsable, solicitar info)
**Para** acelerar el flujo de trabajo sin perder control.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Sugerencia de Acciones Atómicas (Human-in-the-Loop)
  Scenario: Presentación y ejecución de acciones sugeridas
    Given un análisis de correo completado
    Then el sistema configura y genera una lista de N acciones sugeridas
    And cada acción exhibe: tipo, descripción, responsable sugerido, prioridad y fecha
    And el usuario puede explícitamente "Aprobar" o "Rechazar" cada tarjeta individual
    And si el usuario aprueba "Crear Tarea", el sistema llama la API interna confirmando el task_id
    And si el usuario no aprueba explícitamente, no se ejecuta ninguna alteración en el sistema interno
```

---

### US-015: Feedback y aprendizaje supervisado
**Como** gestor de un buzón corporativo
**Quiero** que el sistema aprenda de mis ediciones y rechazos
**Para** que las propuestas mejoren con el tiempo.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Feedback Loop para Aprendizaje Automático
  Scenario: Registro de correcciones y rechazos humanos
    Given un borrador propuesto por la IA
    When el usuario edita el texto del borrador antes de enviarlo
    Then el sistema registra en auditoría la versión original vs la versión final (Delta)
    When el usuario presiona "Rechazar" sobre una propuesta
    Then el sistema debe solicitar un motivo de rechazo de un catálogo predefinido
    And el sistema debe almacenar métricas de tasas de aceptación, edición y rechazo categorizadas para re-entrenamiento
```

---

### US-016: Gestión multi-buzón con políticas por buzón
**Como** administrador del asistente
**Quiero** configurar políticas por buzón (tono, idioma por defecto, categorías, aprobadores)
**Para** adaptar el comportamiento a cada canal corporativo.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Configuración de 'Mailbox Policy' Dinámicas
  Scenario: Aplicación de políticas diferenciadas sin reiniciar el sistema
    Given múltiples buzones corporativos registrados
    When el administrador define un 'mailbox_policy' configurando idioma, nivel de formalidad, disclaimers y escalamiento
    Then las propuestas generadas por el LLM aplican inmediatamente este contexto en sus prompts
    And los nuevos cambios de política operan sobre el siguiente correo entrante sin requerir 'redeploy' de código
```

---

### US-017: Trazabilidad y alineación con eDiscovery (M365)
**Como** auditor o compliance officer
**Quiero** trazabilidad de decisiones y acciones, alineada a eDiscovery
**Para** asegurar cumplimiento y auditoría.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Integridad Operacional de Datos M365
  Scenario: Auditoría de Custodia de Correos
    Given el procesamiento de un correo corporativo
    Then el sistema inserta un registro 'audit_id' inmutable
    And captura el timestamp, buzón de origen, intención inferida, acciones sugeridas y decisión humana
    And el sistema debe mantener referencias y metadatos del correo original sin duplicar innecesariamente el cuerpo completo
    And permite exportar reportes respetando eDiscovery sin romper la cadena de custodia
```

---

### US-018: Métricas de desempeño y calidad
**Como** líder de operación/servicio
**Quiero** ver métricas de desempeño del asistente
**Para** medir Retorno de Inversión (ROI) y mejora continua.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Cuadro de Mando de Desempeño Inteligente (AI Dashboards)
  Scenario: Consolidación de retorno y eficiencia
    Given el Líder de Operación ingresa al dashboard
    Then puede reportar la tasa de aceptación, edición, rechazo, y tiempo medio de respuesta
    And visualizar las acciones orgánicas creadas segmentadas por buzón y por idioma
    And configurar comparativas "antes vs después" mediante un 'baseline' histórico
    And visualizar en un panel de control la telemetría de fallos de integración (Graph/CRM)
```

---

## ÉPICA 10: Service Delivery (Catálogo de Servicios CRM Federado)
Implementar el paradigma de Delivery separando la "Definición Comercial" (CRM) de la "Ejecución Operativa" (iBPMS) mediante un catálogo consultado en tiempo real.

### US-019: Conectividad Resiliente y Modo Degradado
**Como** Arquitecto de Plataforma
**Quiero** conectar el sistema iBPMS a un catálogo en un CRM externo con caché intermedio
**Para** garantizar que los clientes puedan iniciar procesos incluso si el CRM sufre caídas.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Conectividad y Resiliencia CRM
  Scenario: Consulta base de servicios (CA-1)
    Given el conector CRM (Outbound Adapter) está configurado con autenticación OAuth2
    When el subsistema iBPMS consulta el catálogo por API
    Then el CRM devuelve los servicios incluyendo obligatoriamente su 'service_ref_id' y metadata comercial esencial (Nombre, Descripción)
    
  Scenario: Activación de Modo Degradado por caída de CRM (CA-2)
    Given el backend del CRM externo se encuentra inalcanzable (Timeout o HTTP 5xx)
    And existe sincronización previa en caché Redis
    When un cliente final abre el Portal de Catálogo de Servicios
    Then el iBPMS debe mostrar el catálogo cacheado
    And debe advertir visualmente la "última fecha de actualización"
    And debe permitir iniciar el requerimiento de servicio sin bloquear el Frontend
```

---

### US-020: Estrategias de Sincronización Flexible
**Como** Administrador del Sistema
**Quiero** habilitar múltiples estrategias de refresco del catálogo CRM (Schedulers)
**Para** balancear la carga de red sin perder la precisión de la oferta comercial.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: CRON y Event-Driven Sync
  Scenario: Sincronización calendarizada automática (CA-3)
    Given que la política de 'scheduled sync' está activada (Ej. "Cada 1 Hora")
    When se alcanza el 'cron trigger' en el backend
    Then el motor iBPMS refresca la tabla interna o el 'Redis Cache' con el catálogo del CRM
    And registra el resultado del lote (OK/FAIL) en la tabla 'ibpms_audit_log'
```

---

### US-021: Mapeo de Variables y Tolerance (Fricción Cero)
**Como** Administrador de Integraciones
**Quiero** mapear campos variables visualmente entre lo que dicta el CRM y lo que espera mi BPMN
**Para** que la operación fluya sin requerir modificar código Java cuando cambie una promoción.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Mapeo Configurable CRM a iBPMS
  Scenario: Trazabilidad de versiones de mapeo (CA-4)
    Given que el Administrador configuró un 'CRM Mapping JSON v1'
    When el Administrador publica una nueva configuración 'Mapping v2'
    Then todas las nuevas instancias (casos) iniciadas heredarán y utilizarán el 'Mapping v2'
    And el caso específico mantendrá la trazabilidad inmutable apuntando a qué versión de variables usó al nacer
    
  Scenario: Tolerancia a catálogos incompletos (CA-5)
    Given que el objeto 'Servicio' recuperado del CRM omite un campo no-crítico (Ej: 'URL_Imagen_Promocional')
    When el cliente presiona "Iniciar Nuevo Caso" en el portal (Pantalla 0)
    Then el iBPMS verifica si los 'campos mínimos requeridos' (service_id, cliente) existen
    And si se cumplen los mínimos, permite la instanciación e ignora el campo no-crítico faltante sin lanzar HTTP 500
```
**Trazabilidad UX:** Afecta a la **Pantalla 0: Service Catalog** y la **Pantalla 11: Hub de Integraciones**.

---

## ÉPICA 11: Intelligent Intake y Vistas Híbridas por Rol (Service Delivery)
Definir un modelo controlado de instanciación de procesos ("Plan A" vía correo y "Plan B" manual restrictivo), eliminando el anti-patrón de crear procesos BPMN basura ante simples respuestas de correo electrónico o alertas SPAM.

### US-022: Disparo 'Confirm-to-Create' por Correo (Plan A)
**Como** Analista o Gestor
**Quiero** enviar un correo de confirmación de servicio a un Cliente desde un buzón corporativo en el iBPMS
**Para** notificarlo, dejar evidencia auditable, y generar una tarea encolada ("Solicitud de Creación SD") sin instanciar ciegamente un proceso basura.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Intake Controlado Plan A (Email Trigger)
  Scenario: Creación de Tarea Administrativa en vez de Service Delivery (CA-1)
    Given un Gestor envía un correo de confirmación a un cliente desde un buzón (Ej: auditorias@ibpms.com) indicando un servicio (Plantilla TO-BE)
    When el correo saliente se envía satisfactoriamente
    Then el sistema registra el correo como evento auditable
    And genera un 'correlation id' asociando al Cliente (CRM ID) y al 'template_id'
    And el sistema no inicia una instancia BPMN en Camunda
    And el sistema crea una Tarea de Usuario ("Crear Service Delivery") asignada a un Responsable Admin
```

---

### US-023: Correlación Continua del Hilo
**Como** Sistema Core ONS
**Quiero** mantener un Tracking/Threading ininterrumpido a lo largo del correo entrante/saliente
**Para** que la comunicación con el cliente nunca se pierda y quede subsumida en el Service Delivery una vez éste nazca.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Threads y Message-ID Tracking
  Scenario: Concatenación de respuestas al Contexto Pre-SD (CA-2)
    Given el sistema envió un correo "Confirm-to-Create" (US-022) y el cliente responde a dicho email
    When el Webhook entrante de M365 captura el correo
    Then vincula la respuesta al 'correlation id' previo
    And cuando el Admin finalmente ejecuta "Crear Service Delivery", vincula todo ese hilo previo de correos (Pre-SD Context) a la instancia madre del BPMN (SD).
```

---

### US-024: Creación Global Restringida (Plan B)
**Como** Administrador del Sistema
**Quiero** un botón de instanciación manual forzada
**Para** arrancar un proceso (SD) sin requerir el paso del correo (Plan A), con validación estricta de mis permisos.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Intake Manual Plan B (Seguridad)
  Scenario: Prevención de instanciación no autorizada (CA-3)
    Given que el usuario autenticado tiene un rol de 'Colaborador_Operativo'
    When busca instanciar un Service Delivery globalmente sin correo
    Then la interfaz bloquea la acción o el API rechaza la solicitud (HTTP 403 Forbidden)
    
  Scenario: Creación Exitoso bajo Administrador
    Given un Administrador ocluye el botón de [ Crear Servicio/Case ] (Pantalla 0 / 9)
    And selecciona la Plantilla TO-BE y asocia el ID de Cliente
    When presiona el botón crear
    Then el sistema inicializa la instancia BPMN en el motor (Camunda)
    And registra una auditoría de inicialización manual con trigger_type=MANUAL
```

---

### US-025: Experiencia de 'Cards' Dinámicas por Rol
**Como** Arquitecto de Producto UI
**Quiero** segmentar las Tarjetas Kanban y Dashboards por el rol específico del que mira
**Para** evitar ruido cognitivo y entregar exactamente lo que cada persona necesita (Visibilidad, Ejecución o Seguimiento).

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Vistas UX Segregadas por Intención
  Scenario: Renderizado de Card de 'Embudos de Creación' para el Admin
    Given un usuario 'Administrador' (Rol: admin_sd) ingresa al Dashboard (Home)
    Then la Interfaz presenta una "Card: Servicios por Crear" agrupando las llamadas del Plan A ("12 Pendientes de creación")
    
  Scenario: Renderizado de Card Operativa para Ejecutor de Trincheras
    Given un usuario 'Colaborador' (Rol: auditor_senior) ingresa al Workflow (Inbox)
    Then la interfaz NO le muestra el botón "Crear Servicios"
    And presenta tarjetas agrupadas estrictamente por 'Plantillas', ejemplo: "Auditoría Express — 7 Tareas pendientes en rojo".

  Scenario: Renderizado de Vista 360 para Cuenta / Cliente
    Given un Ejecutivo de Cuenta navega el perfil de un Cliente Específico en su directorio
    Then la interfaz agrupa y presenta TODAS las tareas BPMN y Ágiles atadas a ese CRM_ID 
    And permite resolver la clásica duda del cliente: "¿En qué etapa exacta va mío?"
```

---

### US-026: Portal del Cliente Externo (Vistas Tácticas y Estratégicas)
**Como** Cliente Externo (B2B/B2C)
**Quiero** ingresar a un portal web autenticado para ver el estado de mis Peticiones/Servicios
**Para** no tener que llamar al contact center y tener trazabilidad total (Táctica y Estratégica) de mis trámites.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: External Customer Portal (Service Delivery)
  Scenario: Acceso a Vista Táctica (Estado en Tiempo Real)
    Given un Cliente Externo autenticado (Ej: portal.ibpms.com) mediante Azure AD B2C
    When el cliente ingresa a su panel principal
    Then el sistema debe renderizar una lista con sus Service Deliveries "En Curso"
    And mostrar en qué etapa exacta del proceso se encuentra visualmente (Tracker)

  Scenario: Acceso a Vista Estratégica (Dashboard y SLAs)
    Given el mismo cliente navegando en la pestaña "Histórico y Desempeño"
    Then el sistema renderizará métricas de "Servicios Finalizados a Tiempo" vs "Retrasados"
    And listará todos los Service Deliveries concluidos permitiendo la descarga de su respectivo PDF (SGDEA)
```
**Trazabilidad UX:** Wireframes Pantalla 18 (Portal B2B/B2C del Cliente).
