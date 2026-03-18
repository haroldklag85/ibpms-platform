# Historias de Usuario (BDD / Gherkin) - iBPMS V1

Este documento contiene las Historias de Usuario formales para el MVP Táctico (V1). Todas las historias aquí redactadas se restringen **estrictamente** al bloque "MUST HAVE" y "SHOULD HAVE" definido en `v1_moscow_scope_validation.md`. 
*Cualquier funcionalidad relacionada con IA Agentic o Módulos Verticales (RAG, Scraping, OCR) queda explícitamente fuera de esta versión.*

---

## ÉPICA TRANSVERSAL 0: Gobernanza de Errores y Seguridad Global
*(MUST HAVE)* - Reglas arquitectónicas universales (Manejo de Excepciones y PII) que aplican transversalmente a TODAS las interacciones del iBPMS para evitar redundancias en historias individuales.

### US-000: Resiliencia Integrada y Enmascaramiento PII Visual
**Como** Arquitecto de Plataforma
**Quiero** establecer reglas globales de comportamiento ante fallos HTTP y datos sensibles
**Para** proteger la estabilidad de la UX y blindar datos como TC/SSN en la capa de vista pública.

**Criterios de Aceptación Universales (Gherkin):**
```gherkin
Feature: Universal Error Handling and Privacy Governance
  Scenario: Degradar Grácilmente ante Fallas HTTP 500/503 (Cortes Integración)
    Given una pantalla intentando cargar data externa (Ej: API de Camunda, Grafana, CRM)
    When el subsistema no responda al 'Timeout' o el Nginx devuelva HTTP 5xx
    Then el Frontend interceptará globalmente la falla impidiendo "Pantallas Blancas de la Muerte"
    And montará un componente de estado fallido `[ErrorStateGlobal]` instando a reintentar
    And el Backend arrojará el dump íntegro a los logs ELK sin devolver su stacktrace JSON al puerto cliente.

  Scenario: Triage Semántico de Validaciones UI (HTTP 400/422)
    Given un usuario disparando un 'Submit' de guardado
    When el validador Zod/DTO repela la petición porque faltan propiedades estrictas
    Then la API vomitará HTTP 400 devolviendo un array DTO estandarizado: `{field, issue, translatedMessage}`
    And la vista SPA identificará e inyectará los bordes rojos explícitamente y solo en los `<inputs>` culpables.

  Scenario: Bloqueo de Concurrencia Optimista (HTTP 409)
    Given un registro siendo observado por el Usuario A y el Usuario B a la vez
    When el Usuario A estampa su guardado subiendo y persistiendo la `Version N`
    And el Usuario B pulsa 'Guardar' microsegundos después manteniendo la `Version N-1` local
    Then el motor DB ejecutará control de concurrencia optimista rechazando la inyección
    And la API responderá un HTTP 409 Conflict, obligando a repintar el browser del Usuario B con un aviso: "Datos oxidados, registro alterado reciéntemente".

  Scenario: Enmascaramiento Dinámico de Identidad Personal (PII Redaction)
    Given la captura de texto libre no estructurado (Ej. Emails entrantes desde Exchange o comentarios)
    When el string se despache desde la API hacia la pantalla interactiva de un operario (Sin superpoderes)
    Then un interceptor regex o LLM hallará secuencias numéricas/textuales que referencien Tarjetas de Crédito, Documentos Hipotecarios/Médicos
    And oscurecerá o mutará perentoriamente esos caracteres por hashes `[CONFIDENCIAL - CLASE PII]` antes de rehidratar el Frontend.
```
**Trazabilidad UX:** Transversal a Formularios, Bandejas Docketing (Pantalla 1B) y Grillas del sistema global.

---

## ÉPICA 1: Orquestación y Workbenches (El Motor Core)
Esta épica aborda la capacidad fundamental del sistema: recibir un requerimiento, enrutarlo como una tarea (Task) y permitir que el usuario la gestione en su Workdesk (Escritorio de Tareas).

### US-001: Obtener Tareas Pendientes en el Workdesk
**Como** Analista / Usuario de Negocio
**Quiero** visualizar una lista consolidada de mis tareas pendientes (BPMN o Kanban) al ingresar a la plataforma (Workdesk)
**Para** saber exactamente qué gestiones operativas debo priorizar y resolver hoy.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Workdesk Loading and Real-Time Grid
  Scenario: Carga Inicial con Paginación y Prioridad SLA (CA-1)
    Given que el usuario "juan.perez" ingresa al Workdesk
    When el sistema consulta las tareas pendientes
    Then el backend retorna estrictamente el primer bloque de tareas (Paginadas, Ej: 50) para proteger el performance
    And el ordenamiento forzoso por defecto es "SLA más Crítico (Fecha Vencimiento Ascendente) primero", sin importar origen.

  Scenario: Búsqueda Estratégica Híbrida local vs remota (CA-2)
    Given la barra de búsqueda en el Workdesk
    When Juan busca el caso "EXP-90X"
    Then el Frontend filtra inmediatamente sobre las 50 tareas precargadas en memoria
    And paralelamente dispara una petición asíncrona a la Base de Datos para asegurar que no existan coincidencias en otras páginas ocultas, refrescando el resultado final.

  Scenario: Consolidación UI Unificada de BPMN y Kanban (CA-3)
    Given la disparidad estructural entre tareas de Proceso (Camunda) y Proyectos (Gantt)
    When se renderiza la tabla o tarjetas unificadas en la misma pestaña activa
    Then la interfaz utiliza un patrón de "Data Grid Universal" garantizando 5 columnas estandarizadas: [Nombre, SLA, Estado, Avance, Recurso]
    And añade un ícono o badge visual a la izquierda del Nombre (Ej: ⚡ Flujo, 📅 Proyecto) permitiendo identificación rápida sin corromper la uniformidad de la tabla.

  Scenario: Alternador de Vistas por Delegación (CA-4)
    Given que un Asistente le delega permisos temporales a su Jefe (Juan)
    Then el Workdesk de Juan muestra un Interruptor o Dropdown (Toggle) en la cabecera
    And permite alternar entre `[Mis Tareas]` y `[Tareas de mi Asistente]` sin mezclar visualmente los contextos, evitando desorden operativo.

  Scenario: Ticking Engine Vivo y Semáforos SLA (CA-5)
    Given las tarjetas o filas del Workdesk cargadas en pantalla
    Then el componente visual de SLA actúa como un temporizador dinámico "vivo" (Tick-Tock)
    And cambia de color forzosamente (Semáforo: Verde, Amarillo, Rojo) en tiempo real al acercarse al límite temporal configurado, sin requerir refresco (F5) ni peticiones de polling constantes.

  Scenario: Desaparición Fantasma por Bloqueo o Asignación Externa (CA-6)
    Given una tarea visible en la "Cola de Grupo" de Juan
    When un colega la "Reclama" o un "Project Manager (PM)" asigna forzosamente la tarea a otro especialista
    Then el sistema mediante WebSockets (o Server-Sent Events) recibe el pulso de asignación
    And oculta instantáneamente esa tarjeta de la vista de Juan para erradicar colisiones.

  Scenario: Degradación Elegante ante Falla BPMN (CA-7)
    Given una caída temporal de la API / Base de Datos transaccional de Camunda
    When el usuario carga su Workdesk en ese instante
    Then la interfaz carga exitosamente las tareas nativas (Planificador Kanban) que siguen vivas y accesibles
    And proyecta un Toast/Banner amable alertando: "Sincronización de Procesos (BPMN) degradada temporalmente. Estamos trabajando para solucionarlo".

  Scenario: Intervención Administrativa Anti Cherry-Picking (CA-8)
    Given que el Administrador Global del cliente detecta sesgos de selección (operarios ignorando tareas complejas)
    When enciende la bandera del sistema de "Enrutamiento Forzoso" (Feature Toggle)
    Then la vista de Workdesk de los operarios oculta la tabla/lista selectiva
    And presenta un único call-to-action gigante: `[Atender Siguiente Tarea]`, forzando a resolver matemáticamente lo más crítico o antiguo.

  Scenario: Paginación Máxima de Tarjetas de Tarea (CA-9)
    Given el Workdesk de un operario (Pantalla 1) con un volumen de casos asignados que supera la capacidad visual
    When se renderiza la lista o grilla unificada de tareas
    Then la interfaz debe dividir y renderizar las tarjetas a través de una paginación
    And establecerá un límite estricto de máximo 15 tarjetas (Task Cards) por página, para garantizar el rendimiento y la legibilidad.
```
**Trazabilidad UX:** Wireframes Pantalla 1 (Workdesk - Escritorio de Tareas).

---

### US-002: Reclamar una Tarea de Grupo (Claim Task)
**Como** Analista / Usuario de Negocio
**Quiero** poder "reclamar" (asignarme) una tarea que actualmente pertenece a la cola de todo mi grupo
**Para** evitar que otro compañero trabaje en el mismo caso de forma paralela y duplicar esfuerzos.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Task Claiming and Reassignment
  Scenario: Reclamo Simultáneo (Condición de Carrera) (CA-1)
    Given dos analistas visualizan la tarea "TK-099" en la Cola de Grupo
    When ambos hacen clic en [Reclamar] en el mismo segundo
    Then el sistema inscribe al primero en llegar a la Base de Datos como `assignee`
    And al segundo le retorna un HTTP 409 Conflict mostrando un Modal Amable: "Lo sentimos, María se te adelantó por un segundo".

  Scenario: Reclamo Masivo en Lote (Bulk Claim) (CA-2)
    Given la vista general de cuadrícula en el Workdesk
    When el analista selecciona múltiples casillas (Ej. 10 tareas) y pulsa [Reclamar Seleccionadas]
    Then el sistema ejecuta una transacción Batch y asigna en lote a su bandeja
    And si alguna de esas 10 ya fue tomada, le notifica un resumen de éxito/fallo parcial (Ej. "9 reclamadas exitosamente, 1 ya no disponible").

  # NOTA CONTEXTUAL PO: (CA-3) Límite estructural de secuestro simultáneo queda diferido para la V2.

  Scenario: Liberación con Mensajería Interna (Peer-to-Peer Handoff) (CA-4)
    Given un analista con una tarea en progreso que desea traspasar a un compañero
    When pulsa [Liberar Tarea] en la Pantalla 5
    Then el sistema devuelve la tarea a la Cola Grupal
    And despliega opcionalmente un campo para adjuntar un Mensaje Interno (Ej: "@Pedro, te liberé este caso para que lo tomes").

  Scenario: Exploración Segura (Modo Sólo Lectura) (CA-5)
    Given una tarea en la Cola Grupal
    When el analista hace doble clic para abrir el detalle del caso
    Then el sistema renderiza el formulario y los anexos en "Modo Sólo Lectura"
    And NO altera el `assignee` en la Base Datos hasta que pulse explícitamente el botón físico de [Reclamar].

  Scenario: Prevención de Abandono (Ghost Job Timeout) (CA-6)
    Given que la tarea fue reclamada pero no ha sufrido modificaciones de estado
    Then un Cron Job estructurado en el Backend rastrea las transacciones con inactividad superior al SLA
    And ejecuta un "Auto-Unclaim", purgando al `assignee` inactivo y devolviendo el caso a la Cola Grupal para rescate.

  Scenario: Persistencia de Borradores (Draft Savings) (CA-7)
    Given un formulario parcialmente diligenciado
    When el analista oprime [Liberar Tarea]
    Then el motor persiste todo el JSON cargado hasta ese momento usando la API de Variables de Camunda
    And el siguiente compañero que la reclame encontrará el progreso intacto en su interfaz.

  Scenario: Despojo Forzoso de Tarea por Supervisor (CA-8)
    Given que la tarea "TK-099" pertenece a Juan, pero él se ausentó
    When un gerente con Rol de Supervisor ingresa a la vista de monitoreo
    Then dispone de controles con privilegios elevados para ejecutar un "Forced Unclaim" manual
    And despojando inmediatamente a Juan y devolviendo la tarea a disponibilidad pública.

  Scenario: Trazabilidad Forense en Pop-Up (Auditoría Profunda) (CA-9)
    Given las constantes reclamaciones, liberaciones y despojos sobre un mismo Caso
    Then la interfaz expone la funcionalidad de "Ver Trazabilidad" (Botón de Bitácora)
    And despliega un Pop-Up cargando el historial completo de rotación cronológica del atributo `assignee` de la base de datos de Auditoría.

  Scenario: Resiliencia Periférica (Offline Local Sync) (CA-10)
    Given que la red hacia la Base de Datos Core sufre un micro-corte temporal
    When el analista oprime [Reclamar]
    Then el Frontend "miente" visualmente colocando la tarea en "Mi Bandeja" (Almacenamiento Local Temporal)
    And genera procesos automáticos de ruteo/re-intento sincrónico por detrás hasta que confirme físicamente en el Motor (Degradación controlada).
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
  Scenario: Seleccionar Patrón de Formulario (CA-1)
    Given que el desarrollador crea un nuevo recurso en la sección "Formularios"
    When el modal pregunta "¿Qué arquitectura de formulario desea?"
    Then el usuario puede elegir "Patrón B: iForm Maestro (Expediente Multi-Etapa)"
    And el lienzo visual se estructura para basar el renderizado en la variable "Current_Stage" de Camunda

  Scenario: Análisis Bidireccional de Código en Tiempo Real (CA-2)
    Given que el usuario está en el Canvas del "iForm Maestro"
    When arrastra un "Input Text (Monto Aprobado)" y marca "Requerido"
    Then el panel derecho "Mónaco IDE" de código actualizado escribe automáticamente:
      """javascript
      const schema = z.object({ monto_aprobado: z.number().positive() })
      """
    And si el usuario borra la línea de Zod en el panel de código, el componente visual pierde instantáneamente su validación de Requerido.

  Scenario: Iconos de Ayuda en Pestañas de Código (CA-3)
    Given el Arquitecto está en la Pantalla 7 en la vista de Mónaco IDE
    Then a la par de las pestañas `<script setup>` y `<style scoped>` debe haber un icono de ayuda [?]
    And al hacer hover, un Tooltip explica de forma concisa la función de cada pestaña (Ej: "Aquí va la lógica de Vue y validaciones Zod" / "Aquí va el CSS del componente").

  Scenario: [Arquitectura] Sandboxing Estricto contra XSS (AST Evaluator) (CA-4)
    Given que el constructor del iForm inyectó una regla de negocio Javascript en un campo dinámico
    When el motor de renderizado de la Pantalla 7 interpreta el formulario en el navegador
    Then la plataforma prohíbe estructuralmente el uso de la función `eval()`
    And toda expresión JS es analizada y ejecutada internamente mediante un intérprete de gramática seguro (Abstract Syntax Tree Parser) que rechaza cualquier intento de manipulación del `window`, `document` o peticiones `fetch`.

  Scenario: [Arquitectura] Factoría Reactiva de Zod On-The-Fly (CA-5)
    Given la estructura JSON del formulario generada por el IDE
    When el motor requiere validar los campos renderizados en pantalla
    Then el sistema NO emite archivos estáticos de código fuente JS para re-compilar
    And instanciará dinámicamente el esquema global cruzado utilizando la factoría de validaciones `zod` conectada en tiempo real a la memoria reactiva (`reactive()`) de Vue.

  Scenario: [Arquitectura] Aislamiento Perimetral CSS (Shadow DOM) (CA-6)
    Given que el usuario redactó reglas exóticas de CSS para colorear botones específicos de su Formulario
    When la pantalla cliente dibuja el componente en el Workdesk
    Then el iBPMS encapsulará todo el componente inyectado usando el estándar HTML5 "Shadow DOM"
    And ninguna de las clases CSS inyectadas podrá sangrar (Style Bleed) hacia el exterior ni distorsionar la barra superior o menús laterales de Tailwind corporativo.

  Scenario: [Arquitectura] Render Functions y Teleportación (CA-7)
    Given una directriz para renderizar componentes infinitamente anidados (Ej: Grillas dentro de Módulos dentro de Secciones)
    Then el motor subyacente de Vue prescindirá del HTML rígido (`<template>`) utilizando funciones programáticas puras de Virtual DOM (`h()`) para renderizado ultrarrápido
    And los Tooltips dinámicos o ventanas emergentes forzarán el uso de la etiqueta nativa `<Teleport to="body">` para romper el encierro del z-index y sobreponerse perfectamente en la jerarquía visual del monitor.
    And los Tooltips dinámicos o ventanas emergentes forzarán el uso de la etiqueta nativa `<Teleport to="body">` para romper el encierro del z-index y sobreponerse perfectamente en la jerarquía visual del monitor.

  Scenario: [Arquitectura ERP] Navegación Modular y Agrupación de Malla (CA-8)
    Given que el Arquitecto está diseñando un "iForm Maestro" de alta densidad (Ej: 100+ campos)
    When el usuario arrastra componentes visuales al lienzo
    Then la Pantalla 7 dispondrá nativamente de agrupadores tipo "Micro-ERP": Pestañas (Tabs), Paneles Acordeón (Collapsible Panels) y Grillas de Datos Anidadas (DataTables con CRUD)
    And para evitar el diseño infinito vertical, segmentando el formulario visualmente a nivel departamental (Ej: Pestaña "Datos Clínicos", Pestaña "Análisis Legal").

  Scenario: [Arquitectura Multi-Expediente] Cohabitación de Maestros en un Proceso (CA-9)
    Given un Proceso BPMN (Camunda) de largo aliento con distintas esferas de confidencialidad
    When el Arquitecto vincula formularios a las Tareas (`UserTasks`)
    Then la plataforma permitirá asociar MÚLTIPLES "iForm Maestros" distintos dentro de una misma Instancia de Proceso (Ej: Maestro Comercial al inicio, Maestro Contractual al final)
    And cada Maestro gestionará su propio esquema Zod y persistencia independiente en CQRS, evitando la creación de un único "Monolito JSON" inmanejable para toda la organización.

  Scenario: Inmersión Funcional "Alt+Tab Zero" (Full-Screen Focus) (CA-10)
    Given un Operario abriendo un "iForm Maestro" desde su Bandeja en Pantalla 2
    When la interfaz dibuja el componente en el navegador
    Then el sistema forzará que el Contenedor ocupe el 100% del Ancho y Alto útil (Experiencia ERP Inmersiva)
    And embebiendo sub-elementos (Previsores PDF, Calculadoras en línea) dentro del mismo componente Vue para evitar forzar al usuario a minimizar el iBPMS o abrir aplicaciones satélite para resolver la Tarea.
  Scenario: Paleta de Componentes Base HTML5 (Formulario Simple y Maestro) (CA-11)
    Given que el desarrollador está diseñando un formulario en el lienzo de la Pantalla 7
    Then la plataforma debe proveer una paleta tipificada que garantice la renderización de todos los estándares HTML5 y su mapeo a Zod:
    And Textos: `text` (Corto), `textarea` (Largo), `password`, `email`, `url`, `tel`, `search`, `hidden`.
    And Números y Rangos: `number`, `range`, `meter`, `progress`.
    And Fechas y Tiempos: `date`, `datetime-local`, `time`, `month`, `week`.
    And Selección y Opciones: `select` (con sus `option` y `optgroup`), `datalist`, `checkbox`, `radio`.
    And Selección y Opciones: `select` (con sus `option` y `optgroup`), `datalist`, `checkbox`, `radio`.
    And Estructura y Acción: `submit`, `reset`, `button`, `image`, `file`, `color`, `output`, `fieldset`, `legend`, `label`.
    And cada uno de estos componentes emitirá su tipo de dato UI y su esquema Zod correspondiente para la validación bidireccional.

  Scenario: [Integración Motor] Drag & Drop Sensorial de Process Variables (CA-12)
    Given que el Arquitecto está diseñando en el Mónaco IDE (Pantalla 7) vinculado a una User Task (Ej: "Aprobar Crédito")
    When importa el archivo subyacente `.bpmn` o conecta el IDE mediante API contra Camunda para leer el Diccionario de Datos del proceso activo
    Then el IDE enlistará las `Process Variables` detectadas en un panel lateral
    And al arrastrar una variable (Ej. `monto_credito` tipado numérico) hacia el Canvas, el motor adivinará su componente Vue lógico (`<InputNumber>`) y redactará automáticamente `z.number()` en el panel de Zod.

  Scenario: [Arquitectura de Datos] Mapeo de Entradas y Salidas Form-To-Process (CA-13)
    Given el panel de propiedades globales del formulario en el IDE
    Then el desarrollador dispondrá de dos matrices declarativas de vinculación (Input/Output Mapping):
    And 1. Carga (`onMounted`): El mapeo estricto de qué variables leerá de la API de Camunda para pre-poblar o condicionar el formulario (`prefillData` de US-029).
    And 2. Retorno (Submit): Un contrato explícito indicando qué porción específica de la data recolectada se serializará y reinyectará en las `Process Variables` del motor al hacer el POST `Complete Task`, blindando el motor de basura transaccional.

  Scenario: [Accionadores] Botones Nativos de Estado Camunda (Task Lifecycle) (CA-14)
    Given la necesidad de gobernar el ciclo vital del Token directamente desde la UI sin que el desarrollador escriba llamadas Axios manuales
    When el Arquitecto abre la paleta de componentes "Botones de Acción" en Pantalla 7
    Then dispondrá de componentes drag&drop *Smart Buttons* pre-conectados a las APIs core (`/workbox/tasks`):
    And `[Completar]` (Avanza el proceso enviando I/O), `[Reclamar/Liberar]`, `[Auto-Guardar Borrador]` (Persistencia Local de US-029), y `[Generar BPMN Error]` (Desvío técnico hacia un Evento Intermedio de Error en Camunda).

  Scenario: [Arquitectura de Resiliencia] Captura Automática de Errores Core (Smart Buttons) (CA-15)
    Given el desarrollador ha finalizado el diseño del "iForm Maestro" y procede a hacer clic en `[Publicar]` en la Pantalla 7
    When el motor de compilación Mónaco IDE analiza el código fuente del componente Vue generado
    Then el IDE verificará explícitamente la presencia de manejadores de eventos (Action Listeners) conectados a la API de Camunda (Ej. Los Smart Buttons de la Paleta)
    And la plataforma inyectará nativamente "por debajo" un bloque global `try/catch` o un Interceptor de red sobre dichos botones.
    And si las APIs de Camunda o CQRS (US-029) retornan un HTTP 5xx (Timeout o Crash), el Smart Button suspenderá su estado de carga (Spinner) y detonará automáticamente un Componente Visual Genérico de Error (Toast/Snackbar corporativo alertando: "Fallo de Comunicación con el Motor Central").
    And bajo ninguna circunstancia se permitirá la publicación de un formulario "Mudo" (Sin captura de errores de red), descargando esta responsabilidad arquitectónica de las manos del desarrollador (Enfoque No-Code Seguro).

  Scenario: [Arquitectura Flexible] Constraint de Bajo Acoplamiento Form-To-Process (CA-16)
    Given que el Arquitecto vinculó el diccionario de un `.bpmn` al panel IDE para el "iForm Maestro"
    When existan discrepancias lógicas entre los campos visuales dibujados y las variables detectadas por Camunda
    Then el lienzo IDE no impondrá bloqueos duros ('No Mapeado') ni abortará la compilación de la UI
    And la coerción de coherencia entre ambas capas recaerá estrictamente sobre el ciclo de QA automatizado (US-028)
    And preservando la agilidad del IDE y el bajo acoplamiento arquitectónico entre el Frontend/Zod y el Motor Central.

  Scenario: Soporte de Motores de Lenguaje (Language Servers en Web IDE) (CA-17)
    Given que el usuario edita el código fuente de un Formulario en las pestañas del Mónaco IDE (Pantalla 7)
    Then la plataforma proveerá Autocompletado, Syntax Highlighting y Linting estricto exclusivamente para: TypeScript, Vue (SFC), SCSS/Tailwind y JSON.
    And descartará a nivel arquitectónico motores de terceros como GraphQL o YAML para mantener el Bundle Size del Editor ultraligero y consistente con la estrategia REST API del Core iBPMS.

  Scenario: [Onboarding Embebido] Tooltips de Ayuda Visual (Propiedades Avanzadas) (CA-18)
    Given que el Arquitecto No-Code selecciona un componente visual en el lienzo para configurar sus Propiedades Avanzadas
    Then el panel lateral (Propiedades) mostrará iconos de ayuda `[?]` al lado de cada título
    And al hacer hover sobre los iconos, el sistema desplegará Tooltips didácticos con el siguiente mapeo funcional:
    | Propiedad | Contenido del Tooltip "Para Dummies" |
    |---|---|
    | **ID (Variable Name)** | "Ésta es 'La Cédula' única del campo para el sistema. Usa solo minúsculas y guiones bajos, sin espacios (Ej: nombre_cliente)." |
    | **Label (Nombre Visible)** | "La pregunta o título oficial que leerá la persona humana (Ej: ¿Cuál es su nombre?)." |
    | **Placeholder / Descripción** | "Texto ejemplo tipo 'fantasma' que guía al usuario y desaparece cuando él escribe (Ej: +57 321 000 0000)." |
    | **Camunda Variable (I/O Binding)** | "El puente de datos. Nombra el 'bolsillo' exacto donde el motor central de Camunda guardará esta respuesta." |
    | **Validaciones Zod** | "El Policía. Marca como Requerido para obligar la respuesta, o usa los límites numéricos para que un texto no sea demasiado largo o corto." |
    | **Regex Automático** | "Escáneres mágicos. Elige una regla (Ej: Correo, Tarjeta de Crédito) para impedir que el usuario ingrese datos inválidos." |
    | **Cross-Field Logic** | "Lógica condicional. Configura si la obligatoriedad de este campo depende de lo que el usuario respondió en otra pregunta." |
    And esta funcionalidad estará aislada del Mónaco IDE, focalizándose única y exclusivamente en la usabilidad del panel Clic-and-Drop.

  Scenario: Maximización de Lienzo Visual (Contracción de Mónaco IDE) (CA-19)
    Given el Arquitecto está diseñando un formulario complejo en el Canvas de la Pantalla 7
    When no necesita visualizar o interactuar con el código en tiempo real (Vue/Zod)
    Then dispondrá de un control visual (Ej. un botón de colapso "`>`" en el borde del panel) para ocultar completamente el "Mónaco IDE"
    And al contraerse, el área del Lienzo Visual se expandirá fluidamente para ocupar el 100% del espacio central, mejorando la ergonomía visual del diseño No-Code.

**Nota Post-MVP (V2):** *Motor White-Label & Theming. El sistema estará cimentado sobre Tailwind CSS V4 / Native Variables, permitiendo en fases posteriores la exposición de un Panel Administrativo para la sobreescritura dinámica de Brand Tokens (Colores primarios, radios y fuentes) por Tenant. Excluido del MVP V1 para priorizar features transaccionales.*
    
**Trazabilidad UX:** Wireframes Pantalla 7 (iForm Builder - Web IDE Bidireccional).
  Scenario: Permisos de Sobrescritura en Campos (CA-20)
    Given un usuario en la Etapa 2 abre un iForm Maestro
    Then puede sobrescribir los valores ingresados previamente en la Etapa 1
    But solo si su Rol RBAC tiene permisos explícitos de escritura sobre esos campos, de lo contrario se renderizan como "Solo Lectura".

  Scenario: Enrutador de Archivos Adjuntos por TRD (CA-21)
    Given un usuario sube un archivo en un componente de Adjuntos del formulario
    Then el sistema lee la configuración de las Tablas de Retención Documental (TRD) del proceso
    And rutea el archivo automáticamente a la Bóveda SGDEA Interna (Pantalla 12) o a Microsoft SharePoint según indique la TRD
    And NO se guarda en la Base de Datos transaccional (Diferido a V2).

  Scenario: Validación Proactiva de Zod (CA-22)
    Given un usuario final está diligenciando el formulario en su Workdesk
    When incumple una regla de validación (Ej: escribe 3 números en un campo que exige 10)
    Then el formulario muestra el mensaje de error "en vivo" proactivamente mientras teclea, sin esperar al botón de [Enviar].

  Scenario: Estilos CSS Corporativos Estandarizados V1 (CA-23)
    Given el Arquitecto diseña un formulario
    Then todos los componentes visuales heredan la hoja de estilos de "Marca Corporativa" global
    And NO es posible customizar el color/fuente de cada botón individualmente en V1 para asegurar consistencia (Diferido a V2).

  Scenario: Auto-Guardado de Borrador en Workdesk (CA-24)
    Given un usuario final está llenando un formulario extenso en la Pantalla 2
    Then cada interacción se guarda automáticamente como un borrador en caché local (o BD temporal)
    And si el usuario cierra la pestaña por error, al volver a abrir la tarea, recupera los datos ingresados no enviados.

  Scenario: Reglas de Visibilidad Condicional (CA-25)
    Given el Arquitecto configura la propiedad "Dependencia Visual" de un campo B
    When en el lienzo visual el usuario final marca un Checkbox A
    Then el campo B aparece dinámicamente ("Campo Fantasma") empujando el resto de la estructura hacia abajo (layout reactivo Vue).

  Scenario: Prevención Contra Borrado de Formularios Activos (CA-26)
    Given el Arquitecto intenta eliminar el "Form_Solicitud_V1" en la Pantalla 7
    When el sistema detecta que existen instancias de procesos "en vuelo" que requieren de este formulario
    Then se cancela la eliminación y se muestra un mensaje de Error: "Prohibido: Este formulario está siendo usado por N procesos activos."

  Scenario: Control de Versiones de Diseño de Formulario (CA-27)
    Given el Arquitecto modifica un formulario guardado
    Then al presionar guardar, el IDE genera una nueva versión inmutable (v2) del `.vue`/`JSON`
    And permite consultar y restaurar versiones anteriores en caso de daño en el diseño.

  Scenario: Bitácora de Auditoría a Nivel de Campo (CA-28)
    Given el usuario "maria.lopez" sobrescribe un valor que había puesto "juan.perez" en una etapa previa
    Then el backend inserta un registro en una tabla de auditoría (Ej: FormFieldValueAudit)
    And un Revisor puede ver un panel flotante "Bitácora" que lista "María cambió 'Costo' de 100 a 150 a las 14:00".

  Scenario: Dropdown Alimentado por Exportación CSV (CA-29)
    Given el Arquitecto agrega un componente Dropdown (Select) al Lienzo
    Then en el panel de propiedades tiene la opción de "Cargar archivo .CSV"
    And al subir el archivo, el Dropdown se puebla automáticamente con las opciones (Ej: Países, Áreas, Tipos de Documento) en lugar de tipearlas una a una.

  Scenario: Autocompletado mediante Integración API / BD Externa (CA-30)
    Given el Arquitecto diseña un formulario en la Pantalla 7
    When configura un campo (Ej: "Cédula") para que sea el gatillo (trigger) de una consulta externa
    Then puede vincular ese campo a un Endpoint del Hub (Pantalla 11) o a datos de otros procesos
    And al usuario final tipear la cédula y perder el foco (blur), el formulario autocompleta los campos destino (Ej: "Nombre", "Dirección") automáticamente.

  Scenario: Componente de Firma Electrónica Manuscrita (CA-31)
    Given el Arquitecto requiere formalizar un acuerdo en el formulario
    Then puede arrastrar un componente de "Firma a Mano Alzada" (Canvas HTML5) al Lienzo
    And el usuario final puede dibujar su firma con el mouse o pantalla táctil
    And el sistema guarda la firma como una imagen (Ej: Base64/PNG) anexa al Payload del formulario.

  Scenario: Validaciones Cruzadas entre Múltiples Campos (CA-32)
    Given un formulario tiene un componente "Fecha de Inicio" y "Fecha de Fin"
    When el usuario final ingresa una "Fecha de Fin" que es anterior a la "Fecha de Inicio"
    Then el esquema Zod dinámico evalúa la regla cruzada (refinement)
    And muestra inmediatamente un mensaje de error impidiendo el avance, indicando la inconsistencia temporal.

  Scenario: Exportación a PDF del Formulario Diligenciado (CA-33)
    Given un usuario final ha completado de llenar los datos requeridos en pantalla
    Then dispone de un botón global estilo "[⬇️ Exportar a PDF]"
    And al presionarlo, el sistema genera y descarga un PDF con formato de "Documento Físico" que contiene todos los campos y valores renderizados de manera limpia para impresión.

  Scenario: Grupos de Campos Repetibles (Data Grids / Tablas) (CA-34)
    Given el Arquitecto necesita recopilar una lista de longitud variable (Ej: "Múltiples Co-Deudores")
    Then puede utilizar un componente de "Grupo Repetible" (Field Array)
    And el usuario final verá un botón "[+ Agregar]" para duplicar dinámicamente el conjunto de campos configurados sin afectar el esquema Zod subyacente.

  Scenario: Ayudantes Locales (Tooltips y Placeholders) (CA-35)
    Given el Arquitecto configura un campo complejo en el Lienzo
    Then puede configurar un texto "Placeholder" (texto gris de fondo)
    And puede configurar un "Tooltip" (icono ℹ️ que al hacer hover muestra una descripción detallada)
    And el Arquitecto es libre de usar ambos mecanismos simultáneamente para guiar al usuario final.

  Scenario: Máscaras de Entrada (Input Masks) para Formatos Específicos (CA-36)
    Given el Arquitecto configura un campo numérico como "Ingresos Brutos" o "Cédula"
    Then puede aplicarle una Máscara de Formato (Ej: Moneda, Teléfono, Fecha)
    And mientras el usuario final teclea (Ej: "150000"), el sistema formatea visualmente el valor en vivo (Ej: "$ 150.000,00") sin alterar el valor numérico real bajo el capó.

  Scenario: Visor Histórico Inmutable para Auditoría (CA-37)
    Given un usuario Auditor accede a un proceso completado hace años para revisión
    Then el sistema renderiza el formulario con su diseño original exacto
    But todos los componentes están estrictamente en modo "Solo Lectura", sin botón de [Enviar] y congelados contra cualquier manipulación.

  Scenario: Restricciones de Longitud Dinámicas (Zod min/max) (CA-38)
    Given el Arquitecto configura un campo de texto largo (Textarea)
    Then puede definir en el panel de propiedades "Caracteres Mínimos" y "Máximos"
    And el lienzo genera instantáneamente la regla Zod correspondiente (Ej: `z.string().min(5).max(100)`)
    And bloquea el input visualmente cuando el usuario alcanza el límite.

  Scenario: Condicionamiento de Archivos Adjuntos (CA-39)
    Given el Arquitecto agrega un componente de "Subida de Archivos"
    Then el panel de propiedades debe permitir restringir el "Peso Máximo (MB)" y los "Tipos Permitidos (Ej: .pdf, .jpg, .xml)"
    And si el usuario intenta subir un archivo no permitido, el sistema lo rechaza proactivamente antes de enviarlo al servidor.

  Scenario: Dropdown de Búsqueda Interactiva (Searchable Select) (CA-40)
    Given el Arquitecto necesita presentar una lista extensa de opciones (Ej: 195 Países)
    Then el componente Dropdown (Select) debe incluir por defecto un motor de búsqueda interno (Typeahead)
    And permite al usuario teclear para filtrar la lista instantáneamente sin tener que usar el scroll manual.

  Scenario: Restricciones en Grillas Repetibles (Min/Max Rows) (CA-41)
    Given el Arquitecto utiliza un Data Grid (Grupo Repetible)
    Then puede configurar mediante el panel de propiedades cuántas filas como mínimo debe llenar el usuario, y un tope máximo (Ej: Min: 1, Max: 3)
    And el esquema Zod asegura que el arreglo (`z.array`) cumpla estas restricciones bloqueando el botón [+ Agregar] al llegar al límite.

  Scenario: Soporte Multi-Idioma (i18n) (CA-42 - Diferido a V2)
    # NOTA: Diferido a V2. Para V1 todos los formularios son creados y operados estáticamente en Español.
    Given el Arquitecto diseña un formulario
    Then puede habilitar soporte multi-idioma para cambiar el idioma condicionalmente.

  Scenario: Data Binding (Precarga Automática desde Camunda) (CA-43)
    Given un usuario "Carlos" tiene variables persistidas de etapas anteriores almacenadas en el proceso de Camunda
    Then el IDE de Formularios mapea automáticamente todas las variables (variables del motor) usando sus IDs Técnicos
    And cuando el usuario abre la Pantalla 2, los campos coincidentes se auto-rellenan con esos datos históricos almacenados.

  Scenario: Componentes de Calendario y Rangos de Fechas (CA-44 - Rango Diferido V2)
    Given el Arquitecto necesita recopilar fechas
    Then dispone de un componente DatePicker estándar (Selección de un solo día) en V1.
    # NOTA: La selección compleja de "Rango de Fechas" (Drag and Drop en calendario) se difiere a V2.

  Scenario: Multi-Select Visual (Pastillas/Etiquetas) (CA-45)
    Given el Arquitecto configura un campo desplegable que permite selección múltiple
    Then el diseñador o el panel de propiedades permite elegir la presentación visual: "Chips/Etiquetas" o "Pastillas"
    And el usuario final puede eliminar selecciones individuales haciendo clic en la 'x' de la pastilla correspondiente.

  Scenario: Sello Visual de Aprobatoria con Rol (CA-46)
    Given un usuario con Rol "Gerente" completa una etapa de revisión en un iForm Maestro
    Then esa etapa genera un "Badge/Sello" visual estático incrustado
    And dicho sello muestra el Nombre del Usuario y su Rol (Ej: "Aprobado por Juan Pérez - Gerente de Área") para visibilidad en etapas subsecuentes.

  Scenario: Campos Ocultos (Hidden Inputs) para Metadata (CA-47)
    Given el Arquitecto necesita enviar datos técnicos que el usuario NO debe ver ni alterar
    Then puede arrastrar un componente "Campo Oculto (Hidden Input)" al Lienzo
    And puede asignarle valores variables (Ej: `sys_request_id`)
    And esos datos viajan transparentemente en el Payload JSON final al enviarse la tarea.

  Scenario: Validaciones Condicionales (Required-If) (CA-48)
    Given el Arquitecto configura la propiedad "Requerido Condicional" del Campo B
    When en el lienzo el usuario final marca "Sí" en el Campo A
    Then el esquema Zod dinámico hace que el Campo B se vuelva obligatorio
    And si marca "No", el Campo B es opcional y no bloquea el envío del formulario.

  Scenario: Restricción de Cantidad Mínima y Máxima de Adjuntos (CA-49)
    Given el Arquitecto agrega un componente de "Subida de Archivos"
    Then puede habilitar en las propiedades un requerimiento de Volumen (Ej: Mínimo 2 archivos, Máximo 5)
    And el sistema previene el envío del formulario si no se cumple esta cuota exacta.

  Scenario: Traducción Silenciosa de Formatos (Mascara Front vs Dato Back) (CA-50)
    Given el usuario final digita "1.500.230" en un input numérico con máscara visual
    When el formulario se procesa para hacer el POST al motor de tareas (Camunda)
    Then el IDE despoja el formato estético en secreto y envía el Integer/Float puro (`1500230`)
    And garantizando la integridad de los datos para la analítica y reglas de negocio.

  Scenario: Grillas Editables con Protección y Auditoría Parcial (CA-51)
    Given un usuario "Analista 2" requiere agregar filas a un Data Grid donde "Analista 1" ya insertó datos
    Then el Analista 2 puede visualizar y editar toda la grilla si tiene permisos
    And cualquier fila modificada o eliminada que perteneciera al Analista 1 dejará un rastro en la Bitácora de Auditoría (CA-12).

  Scenario: Feedback Visual en Llamadas a APIs (Estado Indeterminado) (CA-52)
    Given el usuario final ingresa un dato en un campo que dispara una llamada de Autocompletado (CA-14)
    When la interconexión con el sistema externo está procesándose
    Then el botón global de [Enviar Formulario] se deshabilita temporalmente
    And muestra un indicador de carga (spinner), evitando envíos prematuros o datos rotos.

  Scenario: Enmascaramiento de Inputs de Múltiple Tipo (Contraseñas / Sensibles) (CA-53)
    Given el Arquitecto requiere capturar información sensible (Ej: APIs Keys, Claves)
    Then dispone del tipo de Campo "Contraseña (Password)"
    And el texto digitado por el usuario final se oculta inmediatamente bajo asteriscos (****).

  Scenario: Limpieza Automática por Lógica Condicional (CA-54)
    Given un campo B es dependiente de que el campo A tenga el valor "X"
    When el usuario final había llenado el campo B, pero decide cambiar el campo A al valor "Y"
    Then el campo B desaparece visualmente (CA-9) Y suelta (limpia null/undefined) los datos almacenados
    And evitando que viajen datos "fantasma" al motor asociados a ramas muertas del formulario.

  Scenario: Grillas y Organización Multicolumna (Layouts) (CA-55)
    Given el Arquitecto está diseñando la distribución espacial del formulario
    Then puede arrastrar y soltar componentes "Lado a Lado" organizándolos en múltiples columnas (Ej: 2, 3 o 4 columnas)
    And este layout es renderizado mediante CSS Grid / Flexbox de Tailwind en el `.vue` final adaptándose al espacio del Workdesk.

  Scenario: Vista de Imprimible y de Solo-Lectura Plana (View-Mode) (CA-56)
    Given un usuario que tiene el rol de "Visualizador" (Solo aprobar, no rellenar datos) abre la tarea
    Then el sistema NO le muestra un formulario lleno de Inputs deshabilitados y grises
    And le renderiza un componente de "Vista de Lectura / Print-Friendly" donde los datos parecen un documento de texto limpio sin bordes de formulario interactivo.

  Scenario: Candado de Solo-Lectura Basado en Fórmulas (CA-57)
    Given el Arquitecto configura un campo B que depende del valor de un campo A
    Then puede usar el panel de propiedades para establecer una "Fórmula de Bloqueo" (Ej: `if A == 'Extranjero' then disable B`)
    And el motor Zod / Vue deshabilita visualmente (Solo-Lectura) el campo B en tiempo real cuando se cumple la condición.

  Scenario: Cronómetro de Productividad en Formulario (Timer Component) (CA-58)
    Given el Arquitecto necesita medir Tiempos y Movimientos de los empleados
    Then dispone de un componente "Timer / Cronómetro" que ofrece tres modos de configuración:
    And 1. Cronómetro Activo a Demanda (Con botones de Play/Pausa/Reset manuales).
    And 2. Cronómetro en Segundo Plano (Mide el tiempo exacto que la ventana del formulario estuvo en foco).
    And 3. Cronómetro Sincronizado por API (Conectado a un sistema externo de Time-Tracking).
    And el resultado viaja en los metadatos globales del Payload final.

  Scenario: Botón de Reset Dual-Verification (CA-59)
    Given el Arquitecto agrega un botón "Restablecer Formulario"
    When el usuario final lo oprime por error o a propósito
    Then el sistema debe exigir una "Doble Verificación" (Modal de confirmación: "¿Está seguro que desea borrar todos los datos ingresados?")
    And solo si se confirma, el estado reactivo del componente se limpia a cero.

  Scenario: Arrastrar y Soltar (Drag & Drop) Expandido para Adjuntos (CA-60)
    Given el formulario contiene un componente de Subir Archivos
    Then el usuario no está obligado a usar el botón táctil "Buscar Archivo"
    And puede arrastrar múltiples archivos simultáneamente desde su escritorio / SO y soltarlos sobre la zona definida en pantalla para iniciar la carga (Dropzone).

  Scenario: Captura de Geolocalización (GPS) Embebida (CA-61)
    Given el Arquitecto diseña un formulario para trabajadores en terreno
    Then puede arrastrar un componente "Captura GPS" (Obtener Ubicación)
    And cuando el usuario lo presiona, el navegador solicita permiso y captura las coordenadas (Latitud / Longitud) precisas integrándolas automáticamente al esquema.

  Scenario: Lector Nativo de Código de Barras / QR (CA-62)
    Given el proceso requiere leer etiquetas físicas o documentos
    Then el Arquitecto dispone de un componente "Escaner QR/Barcode"
    And este componente invoca la API moderna de navegadores (WebRTC/MediaDevices) para usar la cámara del dispositivo móvil/laptop
    And el valor escaneado rellena el campo objetivo automáticamente.

  Scenario: Auto-Validación de Regex Comunes (Email/URL) (CA-63)
    Given el Arquitecto configura un campo de texto y le asigna el tipo "Email" o "URL"
    Then el IDE aplica implícitamente la validación de Expresión Regular correspondiente (Ej: `z.string().email()`)
    And el sistema provee feedback visual inmediato de error si el usuario tipea algo como `carlos@gmail` sin dominio TLD.

  Scenario: Mensajes de Ayuda / Hint Texts Multi-Estado (CA-64)
    Given el Arquitecto configura un campo con requisitos complejos (Ej: Contraseña Segura)
    Then puede definir múltiples mensajes de estado (Hint Texts) debajo del componente
    And el color/icono de cada mensaje cambia dinámicamente ("❌ a ✅") conforme el usuario va cumpliendo cada criterio (Ej: Mayúscula, Número, Longitud) en tiempo real.

  Scenario: Rechazo de Modo Oscuro en V1 (CA-65 - Diferido a V2)
    # NOTA: Diferido a V2. Para V1, los formularios generados forzarán Light Mode independientemente del SO/Dispositivo.
    Given el Arquitecto despliega el formulario
    Then el formulario se renderiza siempre en paleta corporativa clara.

  Scenario: Conversor de Moneda Automático (CA-66 - Diferido a V2)
    # NOTA: Diferido a V2.
    Given el Arquitecto configura un campo monetario
    Then el formulario ofrecería conversión de tasa de cambio a COP en vivo al pie del componente.

  Scenario: Componente WYSIWYG de Texto Enriquecido (CA-67 - Diferido a V2)
    # NOTA: Diferido a V2. Para V1 solo existe Texto Plano Textarea.
    Given el Arquitecto requiere que el usuario entregue justificaciones extensas
    Then dispone de un componente de Texto Enriquecido (Word-like, con negritas, listas y cursivas).
```
**Trazabilidad UX:** Wireframes Pantalla 7 (IDE Web Pro-Code para Formularios).

---

### US-028: Auto-Generación de Test Suites Zod/Vitest (Shift-Left QA)
**Como** Ingeniero de Calidad (QA) / Arquitecto Frontend
**Quiero** que el diseñador de formularios exponga un botón para generar los test unitarios y de integración de Payload
**Para** asegurar en mi CI/CD que el comportamiento complejo del "iForm Maestro" (Zod) no reviente el motor de Camunda antes de compilar el frontal, probando exclusivamente la capa lógica y evadiendo dependencias externas.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Automated BDD Form Testing Generation

  Scenario: Cobertura Lógica Pura (Zod Schema Validation vs DOM)
    Given la estructura JSON del Formulario generada por el IDE web
    When el usuario oprime [⚡ GENERADOR DE TESTS]
    Then el IDE lee exclusivamente la estructura de validación Zod
    And genera un script de prueba `.spec.ts` acotado a Behavior Driven Development (BDD) del Payload JSON
    And omite intencionalmente cualquier aserción sobre el DOM interactivo (`@vue/test-utils`), blindando el test contra refactores de CSS y Layout.

  Scenario: Aislamiento Total de APIs (Mocking Boilerplate)
    Given que el formulario "Maestro_Onboarding" llama a una API de "Mapeo de Puestos" para autocompletar dropdowns
    When se ejecuta la generación de la Suite
    Then el autogenerador rastrea la llamada externa asíncrona
    And escupe bloques de código Boilerplate usando `vi.mock()` o interceptores (MSW) para devolver respuestas vacías estáticas (Ej: `{ status: 200, data: [] }`)
    And bloquea la red por defecto, obligando al QA a rellenar el Mock manualmente.

  Scenario: Boundary Testing Pragmático (Happy vs Sad Extremo)
    Given que el Arquitecto ha definido 10 campos obligatorios y 3 opcionales
    When el autogenerador procesa las matrices de prueba para `it()`
    Then emitirá exactamente DOS bloques de aserciones fundamentales:
    And 1. Path Feliz 100%: Payload con datos dummy (faker.js) que pasa el parser exitosamente.
    And 2. Path Triste Extremo: Payload vacío (`{}`) o valores nulos para garantizar el choque masivo contra los validadores `Required` y emitir el `ZodError` HTTP 400.

  Scenario: Descarga Pasiva del Binario (Blob Download)
    Given el string de JavaScript en memoria compilado por el Generador
    When finaliza el cruce de datos
    Then el Frontend dispara una descarga nativa silenciosa del archivo `[nombre_form].spec.ts` al disco local del QA
    And se abstiene perentoriamente de inyectar *Commits* directos vía API hacia Git/GitLab para evitar colisiones DevSecOps.

  Scenario: Formulario Multi-Etapa (State Machine de Camunda)
    Given un "iForm Maestro" que altera dinámicamente sus campos y validaciones cruzadas dependiendo de la propiedad `Current_Stage` inyectada por el motor BPMN
    Then el autogenerador interpretará cada alteración lógica como esquemas independientes
    And emitirá descriptores separados (`describe('Stage: Radicación')`, `describe('Stage: Análisis')`)
    And inyectará forzosamente la variable de estado dentro del mock payload para probar las dependencias condicionales (Refines/Unions) por separado.

  Scenario: Renovación Destructiva del Código (Sobrescribir y Perder)
    Given que el QA ya curó un archivo de test manual de la 'Versión 1' del Formulario en su Notebook
    When el Arquitecto lanza la 'Versión 2' visual en el portal
    Then el botón `[⚡ GENERADOR DE TESTS]` simplemente descargará un molde limpio (V2) recién pintado
    And es estricta responsabilidad del humano (QA) hacer el *diff/merge* de sus validaciones manuales antiguas (V1) contra la plantilla nativa (V2) en su entorno Git local.

  Scenario: Responsabilidad Manual en Time Mocking (Fechas Relativas)
    Given un formulario Zod con lógicas de fecha estrictamente relativas (Ej: Mayor a la Fecha Actual Estricta)
    When el IDE autogenera el Payload Feliz
    Then insertará un objeto estándar estático (`new Date('1990-01-01')`) incapaz de predecir el offset del futuro
    And forzará el fallo del test en CI/CD el día posterior delegando la responsabilidad de inyectar `vi.setSystemTime()` o manipular el reloj global netamente a las manos del QA humano en su merge local.

  Scenario: Inyección Estricta de Aliasing Absoluto (@/utils)
    Given que el Zod invoca esquemas compartidos en una carpeta externa raíz compartida (`src/utils/validators.ts`)
    Then el autogenerador jamás intentará reempacar las dependencias transversalmente
    And redactará un import asumiendo el alias estándar Vue/Vite: `import { x } from '@/utils/validators'`
    And el QA será el dueño del error en compilación si el `tsconfig.json` de su propio *runner* difiere de esta regla.

  Scenario: Aislamiento Puro de Estado Global (Ignorar Pinia/VueX)
    Given un *iForm Maestro* con potentes side-effects que interactúa con un estado global (Store de Pinia) para almacenar borradores web
    When el compilador procesa para emitir el `.spec.ts`
    Then se limitará herméticamente a invocar `.parse()` sobre objeto literal del formulario
    And JAMÁS escribirá código de *setup* inyectando referencias cruzadas o Mock Store (Ej: `const store = useMyStore()`), garantizando funciones puras y no una prueba de integración del ecosistema Vuex/Pinia.

  Scenario: Inyección Tipificada Mock File API (Upload File Forms)
    Given un input diseñado por el Arquitecto para subir *Anexos PDF*, rígidamente blindado en Zod como `instanceof File` y validado por *Mime-Type*
    When se procesa el Path Feliz Unitario
    Then el test autogenerado escribirá código imperativo instanciando interfaces web nativas puras (Ej: `new File(["buffer"], "doc.pdf", { type: "application/pdf" })`) 
    And anulando fallos técnicos del parser donde fallaría por inyectar *Strings* simples allí donde la librería Zod exige forzósamente un *Blob* estructurado.

  Scenario: Ceguera Visual de Cobertura (Delegación a CI/CD Runner)
    Given el proceso de hacer clic en `[⚡ GENERADOR DE TESTS]`
    Then la interfaz (IDE Pantalla 7) descargará un `Blob` sin procesar el porcentaje global de *assertions* correctas (Istanbul/C8 Coverage Stats)
    And el Arquitecto no verá estadísticas, diagramas en pastel ni el estado en caliente, asumiendo una compilación ciega y unidireccional cuyo veredicto es propiedad absoluta de la consola Terminal del QA o el *Runner* CI/CD (Ej: GitLab) remoto.
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

  Scenario: Trazabilidad de Borrador Volátil (LocalStorage Draft)
    Given un formulario extenso ("Maestro_Onboarding") abierto en el Workdesk
    When el analista diligencia la mitad de los campos y accidentalmente cierra la pestaña
    Then el Frontend recuperará el progreso utilizando almacenamiento estricto en el navegador (`LocalStorage` mediante `@vueuse/core`)
    And el Backend iBPMS se librará por completo de gestionar la basura transaccional de borradores incompletos hasta que el humano presione físicamente [Enviar].

  Scenario: Consistencia Transaccional Cruda (ACID Fallback over Sagas)
    Given un Payload de formulario (`/complete`) perfectamente validado en Zod que llega al Backend
    When el motor orquestador (Camunda 7) sufre un Crash o Timeout HTTP en su API REST interna
    Then el Backend iBPMS abortará inmediatamente la transacción (Rollback de Persistencia CQRS)
    And devolverá un error HTTP 500 Crudo ("Motor No Disponible") a la UI en Pantalla 2
    And se prohibe a nivel arquitectónico generar falsos positivos HTTP 202 ("Guardado para después") para eludir el colapso del proceso judicial de fondo.

  Scenario: Inyección Megalítica de Contexto (Patrón BFF)
    Given la entrada física a la vista de la tarea (Pantalla 2)
    When el Frontend inicializa el componente Vue
    Then despachará exactamente UNA (1) única petición consolidada GET `/api/v1/workbox/tasks/{id}/form-context`
    And el Backend obrará como BFF *(Backend for Frontend)* inyectando en un solo Mega-DTO la triada: El JSON Schema de Zod, la configuración de Layout de Vue, y las Variables de Solo Lectura extraídas de Camunda (`prefillData`) para poblar inputs en un solo *tick* de renderizado.

  Scenario: Seguridad Asimétrica (Frontend Shift-Left vs Backend Zero-Trust)
    Given un formulario que requiere validar asíncronamente un NIT gubernamental
    When el operario cambia el foco (`OnBlur`) del input
    Then el Frontend bloquea y muestra un Spinner de carga visual garantizando la *User Experience* fluida (Shift-Left)
    But antes de insertar en Camunda, al presionar [Enviar], el Backend asume el principio arquitectónico Zero-Trust
    And re-evaluará ciegamente el NIT contra la base de gobierno externa, ignorando la validación exitosa previa del Frontend para blindar el Modelo de Datos.

  Scenario: Integridad de Asignación Concurrente (Implicit Locking)
    Given que una tarea "TK-400" está explícitamente asignada al analista `maria.perez` en el motor
    When el analista `pedro.gomez` intercepta vulnearablemente la URL o el JWT Payload e intenta someter un POST a `/tasks/TK-400/complete`
    Then el Core iBPMS examina deductivamente el `{delegatedUserId}` transaccional contra la identidad central del Security Context
    And aborta transaccionalmente la colisión inyectando un lapidario `HTTP 403 Forbidden` o `409 Conflict`, extirpando la necesidad pesada de emitir *ETags* (Optimistic Locking visual) a través del flujo asíncrono.
```
**Trazabilidad UX:** Wireframes Pantalla 2 (Vista de Detalle / Formulario Dinámico).

---

### US-039: Formulario Genérico Base (Default Inyectado)
**Como** PMO / Owner del iBPMS
**Quiero** disponer de un modelo de formulario genérico pre-asociado a todas las tareas por defecto
**Para** no tener que invertir tiempo dibujando decenas de formularios básicos en la Pantalla 7 cuando las tareas son únicamente procedimentales (tracking de % avance y adjuntar evidencia).

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Auto-vinculación de Pantalla 7.B a tareas desnudas
  Scenario: Inyección Automática de Metadata de Proyecto y Reglas de Completitud
    Given un usuario que apertura una Tarea operativa
    When la tarea NO tiene un "iForm_Key" dinámico asignado a su diseño (ya sea en BPMN P6 o Gantt P8)
    Then el motor renderiza la vista de Workdesk inyectando automáticamente la Pantalla 7.B como interfaz de llenado
    And el formulario debe presentar en modo Solo Lectura la metadata principal heredada de la Instancia de Proyecto (Ej: Presupuesto, Cliente)
    And el formulario obliga al usuario a diligenciar un campo de "Comentario/Observación" antes de enviar
    And persiste un Porcentaje (%) de avance numérico. Si es menor a 100%, el estado debe ser 'En Progreso'. Si es 100%, el estado pasa obligatoriamente a 'Terminado'.
```
**Trazabilidad UX:** Wireframes Pantalla 7.B (Formulario Genérico Base).

---

## ÉPICA 3: Inicio y Recepción (Triggers)
Capacidad de iniciar procesos operacionales tanto de forma manual (Pantalla 0) como reactiva (Webhook).

### US-004: Iniciar un Proceso mediante Webhook (Plugin O365 Listener)
**Como** Sistema (APIM / MS Graph / Webhook)
**Quiero** inyectar un payload automatizado a un Endpoint público de la plataforma
**Para** instanciar un caso de negocio nuevo automáticamente sin intervención manual humana.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Secure Webhook Intake and Human Approval
  Scenario: Idempotencia ante Gatillos Nerviosos (CA-1)
    Given que la API recibe un Webhook POST con el `id_mensaje: xyz-123`
    When a los pocos milisegundos recibe un segundo y tercer POST con el mismo `id_mensaje`
    Then el sistema reconoce la duplicidad en la tabla de transacciones de entrada
    And procesa solo el primero, devolviendo a los duplicados un HTTP 200 OK silencioso (Idempotencia) sin crear tareas clones.

  Scenario: Bloqueo Duro de Cuentas de Sistema (Auto-responders) (CA-2)
    Given un correo entrante detectado por el Webhook
    When el remitente corresponde a un patrón de sistema (Ej. `no-reply@...`, `mailer-daemon@...`)
    Then el Endpoint de Webhook rechaza la petición en el perímetro (HTTP 400 Bad Request)
    And no guarda la transacción en Camunda ni notifica, cortando de raíz los ciclos infinitos.

  Scenario: Trazabilidad de Payloads Basura (CA-3)
    Given un Webhook atacante o malformado (JSON sin la estructura requerida)
    Then la API de Entrada retorna inmediatamente un HTTP 400 Bad Request
    And persiste el rastro en una tabla transaccional especial llamada "Payloads Huérfanos/Fallidos" para auditoría forense del equipo IT.

  Scenario: Verificación de Dominio Autorizado (Whitelist) (CA-4)
    Given un payload válido y bien formado
    When la API extrae el dominio del remitente (Ej. `@ibm.com`)
    Then consulta la Base de Datos Core buscando si `@ibm.com` es un cliente matriculado en iBPMS
    And si no existe coincidencia, el Webhook se rechaza (HTTP 403 Forbidden) antes de despertar a Camunda.

  Scenario: Notificación de Falla Administrativa (CA-5)
    Given un Webhook válido de un cliente registrado
    When el motor BPMN (Camunda) sufre un error interno al intentar instanciar la variable
    Then el sistema aborta la creación
    And dispara inmediatamente un correo electrónico de alerta de falla crítica al Administrador del Sistema.

  Scenario: Resiliencia Periférica con Colas (RabbitMQ) (CA-6)
    Given una caída severa del motor BPMN (Camunda Offline)
    When el Webhook recibe payloads válidos de O365
    Then el sistema actúa como Buffer, encolando las peticiones en el broker de mensajería (RabbitMQ)
    And una vez Camunda regresa a estar Online, el Job procesa la cola instaurando los casos de manera diferida, garantizando cero pérdida de información.

  Scenario: Parametrización de Peso y Bloqueo de Límite (CA-7)
    Given un payload de Webhook con adjuntos anidados
    When el tamaño global de los archivos supera el límite paramétrico por defecto (Ej: 10MB)
    Then el Endpoint corta y frena la descarga (`HTTP 413 Payload Too Large`)
    And es administrable (El límite se puede aumentar a pedido del cliente).

  Scenario: Intake Triage y Aprobación Humana Obligatoria (CA-8 & CA-9)
    Given un Webhook aprobado y procesado exitosamente por la plataforma
    Then el motor BPMN NO instancia el proceso oficial definitivo (Ej. "Onboarding")
    And en su lugar, instancia una "Tarea de Pre-Triaje" visible en la Pantalla 16 (Intake)
    And obligando por política a que un Operario Humano visualice la solicitud, valide los datos del correo, y oprima físicamente [Aprobar y Crear Caso] para detonar el flujo oficial de negocio.

  Scenario: Seguridad Geométrica Híbrida (HMAC) (CA-10)
    Given la exposición pública de la URL del Webhook a internet
    Then por defecto, la API exige validar la firma criptográfica HMAC en los Headers contra un Secreto compartido con Microsoft Graph
    And el Administrador IT posee un switch en la UI para apagar el requerimiento HMAC y solo usar Bearer Tokens si la integración del cliente es heredada (Legacy).
```
**Trazabilidad UX:** Pantalla 11 (Hub de Integraciones: Eventos Entrantes) y Pantalla 16 (Bandeja Inteligente de Intake).

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
  Scenario: Despliegue exitoso de un diagrama BPMN válido (CA-1)
    Given que el usuario Arquitecto ha diseñado el flujo "Aprobacion_Credito_v2.bpmn"
    When el usuario sube el archivo invocando un POST multipart/form-data a "/api/v1/design/processes/deploy"
    Then el motor (Ej. Camunda) debe validar la sintaxis XML del archivo
    And el sistema debe retornar HTTP STATUS 201 Created
    And el sistema debe generar una nueva "Version" del "Process Definition ID" (Ej. Credito:2)
    And las nuevas instancias usarán esta versión sin afectar a las que ya estaban "En Vuelo" (In-Flight)

  Scenario: Intento de despliegue con diagrama inválido (BPMN Roto) (CA-2)
    Given un archivo "Proceso_Roto.bpmn" al que le falta un "End Event" necesario
    When el usuario realiza el POST a "/api/v1/design/processes/deploy"
    Then el motor debe denegar el despliegue
    And el sistema debe retornar HTTP STATUS 422 Unprocessable Entity
    And el payload debe contener el mensaje parseado: "El diagrama no es instanciable. Falta End Event."

  Scenario: Análisis Semántico en "Pre-Flight" de un diagrama complejo (Ejecutabilidad) (CA-3)
    Given el Arquitecto importa un diagrama BPMN 2.0 ("Proceso_Core.bpmn") que contiene Subprocesos, Start Events de Mensaje y Tareas de Servicio
    When el usuario solicita la validación previa al despliegue ("Pre-Flight Analyze")
    Then el motor semántico debe parsear los componentes avanzados
    And identificar si alguna `ServiceTask` carece de su propiedad `Delegate Expression` (Ejecución de código)
    And identificar si alguna `UserTask` carece de una vinculación de `Form Key`
    And identificar si alguna `ExclusiveGateway` carece de un flujo por defecto (`Default Flow`)
    And el sistema debe renderizar en Pantalla 6 la lista de Errores (❌) y Advertencias (⚠️) para que el Arquitecto los corrija antes del despliegue.

  Scenario: Validación Estricta de Start Event Form (Sincronización US-024) (CA-4)
    Given el Arquitecto solicita el despliegue de un nuevo modelo BPMN
    When el analizador Pre-Flight evalúa el nodo inicial (`StartEvent`)
    Then es mandatorio que el Start Event posea una vinculación estricta a un formulario (`Form Key` = `iForm Maestro` o `Simple`)
    And si carece de este formulario, el sistema rechaza el despliegue (HTTP 422) porque rompería la capacidad de instanciación manual forzada (Plan B).

  Scenario: Obligatoriedad de Nomenclatura de Instancia (ID Único) (CA-5)
    Given el Arquitecto configura las propiedades globales del BPMN antes de desplegar
    When intenta ejecutar el despliegue hacia el motor (Camunda)
    Then el sistema verifica que se haya definido la "Regla de Nomenclatura" (Paramétrica Ej: `PREFIJO-{Var}` o Secuencial Automática) para las futuras instancias
    And si esta regla no está definida en la metadata del proceso, el despliegue se bloquea advirtiendo: "Debe definir cómo se llamarán los casos de este proceso".

  Scenario: Autogeneración de Roles RBAC desde Carriles (Lanes) (CA-6)
    Given el Arquitecto importa un diagrama interactivo BPMN ("Flujo_Onboarding.bpmn")
    And el diagrama contiene un Carril (Lane) llamado "Aprobadores_Legales"
    And dentro de ese carril existe la Tarea "Firmar_Contrato" asociada al template "Form_Firma"
    When el usuario realiza el POST a "/api/v1/design/processes/deploy" con éxito
    Then el backend debe crear automáticamente el Rol de Sistema "BPMN_Flujo_Onboarding_Aprobadores_Legales"
    And el sistema debe asociar automáticamente a este Rol los permisos de escritura sobre "Form_Firma" y ejecución sobre la tarea "Firmar_Contrato"
    And el Rol autogenerado queda disponible en el Módulo de Seguridad (Pantalla 14) para asignarle usuarios.

  Scenario: Ley del Abuelo o Grandfathering Estricto por Defecto (CA-7)
    Given existen 15 instancias activas ("En Vuelo") ejecutándose con la Versión 1 de un proceso
    When el Arquitecto presiona `[🚀 DESPLEGAR V2]`
    Then el sistema asume 100% coexistencia pacífica por defecto
    And la V1 sigue viva en background procesando a las instancias antiguas hasta su conclusión
    And la migración forzada JAMÁS es el comportamiento predeterminado, requiriendo un acto explícito y manual.

  Scenario: Cirugía Quirúrgica de Instancias (No Guillotina) (CA-8)
    Given el Arquitecto requiere forzar la migración de instancias de V1 a V2
    When accede al panel `[Gestor de Instancias Activas]`
    Then el sistema TIENE PROHIBIDO ofrecer un botón de "Migrar Todos" de forma masiva ciega
    And debe desplegar una lista con checkboxes individuales permitiendo al Arquitecto seleccionar con pinzas cuáles instancias específicas someterá al salto de versión.

  Scenario: Bloqueo Topológico Duro Pre-Migración (CA-9)
    Given el Arquitecto intenta migrar la Instancia #45 (V1) hacia la V2
    And la Instancia #45 se encuentra actualmente pausada en el nodo `Tarea_Analisis`
    When el motor evalúa el Plan de Migración (Migration Plan)
    Then si el nodo `Tarea_Analisis` fue eliminado o no existe en la topología de la V2, el checkbox de selección se deshabilita
    And el sistema bloquea la migración arrojando: "Imposible migrar la Instancia #45. El nodo actual no existe en la Versión 2. Esta instancia debe terminar en V1 o ser anulada."

  Scenario: Prohibición Absoluta de Data-Patching Humano en TI (CA-10)
    Given la migración forzada hacia una V2 que exige un nuevo campo Zod "Obligatorio" (Ej: `Cédula`) que no existía en la V1
    Then el sistema TIENE ESTRICTAMENTE PROHIBIDO levantar un modal para que el usuario de TI (Systems Admin/Arquitecto) digite o invente ese dato faltante
    And garantizando así la Segregación de Funciones (SoD) y evitando la falsedad ideológica en la base de datos documental.

  Scenario: Amnistía Técnica y Cobro en Aduana (Lazy Validation) (CA-11)
    Given la migración del escenario anterior (CA-5.4) donde falta el dato obligatorio `Cédula`
    When el motor ejecuta el salto técnico a la V2
    Then inyecta silenciosamente un valor nulo (`null`) en la base de datos para no colapsar el hilo de ejecución (Amnistía Técnica)
    And cuando el operario de negocio abra esa instancia en su Workdesk (Pantalla 2), el Frontend renderizará el formulario Zod V2, detectará el `null` imperdonable, pintará el campo en ROJO y bloqueará físicamente el avance funcional hasta que el dueño del proceso pregunte y digite la `Cédula` real (Lazy Validation).

  Scenario: Principio de Ley Vigente para Reglas de Decisión DMN (Late Binding) (CA-12)
    Given un proceso V1 con tokens en vuelo que se aproxima a una Business Rule Task (DMN)
    When el Director de Riesgos actualiza y publica una nueva versión de la tabla DMN
    Then los tokens de la V1 (junto con los de las nuevas versiones) que pisen la compuerta un milisegundo después de la publicación, serán evaluados con la nueva regla matemática
    And demostrando que las reglas DMN no tienen nostalgia y aplican Late Binding.

  Scenario: Tablero de Resiliencia y Morgue de Tokens (CA-13)
    Given un error técnico no controlado durante una migración asíncrona (Ej: Caída de red o base de datos)
    Then el operario de negocio JAMÁS verá un stacktrace o error técnico en su Workdesk
    And el token roto pasará a estado `INCIDENT` y será canalizado exclusivamente a la Pantalla 15.A (SysAdmin) en la pestaña `[🚨 Centro de Incidentes]`
    And otorgando a Soporte Nivel 3 los botones tácticos: `[🔄 Retry (Electrochoque)]` o `[💀 Abortar Caso]`.

  Scenario: Cicatriz Forense de Auditoría Inmutable (CA-14)
    Given la culminación o visualización de una instancia que sufrió una migración forzada estructural
    When un Auditor o Usuario consulta la Vista 360 del Caso (Pantalla 17) o el historial del Workdesk
    Then el sistema inyecta obligatoriamente una franja visual inamovible: `[⚠️ MIGRACIÓN ESTRUCTURAL: Este caso inició bajo la Versión X y fue promovido forzosamente a la Versión Y el DD/MM/YYYY por el Administrador Z]`
    And blindando legalmente a la compañía ante demandas por vacíos procedimentales.

  Scenario: Rollback a Versión Anterior con Historial (CA-15)
    Given el Arquitecto detecta que la versión 3 de un proceso tiene un error lógico post-despliegue
    When navega al panel de "Historial de Versiones" en la Pantalla 6
    Then el sistema debe listar todas las versiones desplegadas previamente (v1, v2, v3) con fecha y autor
    And el Arquitecto puede seleccionar "Restaurar v2" con un solo clic
    And el sistema re-despliega la v2 como la nueva versión activa (v4 internamente = copia de v2)
    And las instancias en vuelo de v3 siguen corriendo hasta terminar naturalmente (salvo Migración Forzada explícita).

  Scenario: Bloqueo Pesimista de Edición Concurrente (CA-16)
    Given el Arquitecto "maria.lopez" abre el proceso "Solicitud_Credito" en el Diseñador (Pantalla 6)
    And el sistema le otorga un "Lock" exclusivo sobre ese proceso
    When el Arquitecto "carlos.gerente" intenta abrir el mismo proceso simultáneamente
    Then el sistema debe mostrar un mensaje: "🔒 Este proceso está siendo editado por maria.lopez desde las 10:15 AM"
    And debe bloquear los controles de edición del lienzo, dejando solo el modo "Solo Lectura" para el segundo usuario.

  Scenario: Copiloto IA Bajo Demanda (CA-17)
    Given el Arquitecto está diseñando un diagrama BPMN en el lienzo
    Then el Copiloto IA NO ejecuta análisis automático en tiempo real
    When el Arquitecto hace clic explícitamente en el botón [🧠 Consultar Copiloto IA]
    Then el sistema envía el XML del diagrama actual al endpoint de IA
    And renderiza las sugerencias y alertas ISO 9001 en el Panel de Feedback inferior.

  Scenario: Pre-Flight Extendido con Validaciones Avanzadas (CA-18)
    Given el Arquitecto solicita un "Pre-Flight Analyze" sobre un diagrama complejo
    Then el sistema debe validar, además de las reglas base (ServiceTask, UserTask, Gateway):
    And identificar si algún `TimerEvent` carece de la expresión de duración configurada (Ej. `R/PT1H`)
    And identificar si algún `MessageEvent` (Intermedio o de Inicio) carece de la correlación o nombre del mensaje (`MessageRef`)
    And identificar si algún `CallActivity` apunta a un `ProcessDefinitionKey` que NO existe desplegado en el motor
    And clasificar los hallazgos como Error (❌ bloquea despliegue) o Advertencia (⚠️ informativa).

  Scenario: Auto-Guardado del Diagrama en Borrador (CA-19)
    Given el Arquitecto está editando un diagrama BPMN en la Pantalla 6
    Then el sistema debe guardar automáticamente un borrador del XML cada 30 segundos (Best Practice Auto-Save)
    And si el usuario cierra el navegador sin desplegar, al volver a abrir el proceso encontrará el último borrador recuperado
    And el sistema debe mostrar un indicador discreto "✅ Guardado" en la barra de estado tras cada auto-guardado exitoso.

  Scenario: Simulación en Sandbox Antes de Desplegar (CA-20)
    Given el Arquitecto tiene un diagrama BPMN listo pero no ha sido desplegado aún
    When presiona el botón [🧪 Probar en Sandbox]
    Then el sistema debe generar una instancia temporal (no persiste en producción) del proceso
    And avanzar visualmente paso a paso mostrando por qué nodo (tarea/compuerta/evento) fluiría un caso de prueba ficticio
    And al finalizar la simulación, destruir la instancia temporal sin dejar rastro en la base de datos de producción.

  Scenario: Separación de Roles RBAC Diseñador vs Release Manager (CA-21)
    Given un usuario con rol "BPMN_Designer" abre un proceso en la Pantalla 6
    Then puede dibujar, importar, exportar y consultar al Copiloto IA
    But el botón [🚀 DESPLEGAR] debe estar deshabilitado (gris) para este rol
    When un usuario con rol "BPMN_Release_Manager" abre el mismo proceso
    Then puede ver el diagrama y presionar [🚀 DESPLEGAR] para enviarlo al motor
    And ambos roles son asignables desde el Módulo de Seguridad (Pantalla 14) y un usuario puede tener ambos simultáneamente.
    And estos roles son GLOBALES (aplican a todos los procesos sin granularidad por módulo). La granularidad por proceso se difiere a V2.

  Scenario: Paleta BPMN 2.0 Estándar Completa con UX Priorizada (CA-22)
    Given el Arquitecto abre el Diseñador en la Pantalla 6
    Then la Paleta BPMN 2.0 debe contener TODOS los elementos del estándar (incluyendo Conditional, Link, Cancel Events, Complex Gateway, Ad-Hoc y Event Sub-Process)
    But los elementos más usados (Start/End, User Task, Service Task, Exclusive/Parallel Gateway) deben aparecer como iconos principales visibles
    And los elementos avanzados/exóticos deben estar agrupados bajo submenús colapsables ("Más Eventos...", "Más Compuertas...")
    And esto evita saturar visualmente un principiante pero no limita a un experto.

  Scenario: Catálogo / Biblioteca de Procesos Desplegados (CA-23)
    Given el Arquitecto accede a la Pantalla 6
    Then debe existir un Panel lateral o pestaña "Explorador de Procesos" que liste todos los procesos diseñados
    And cada entrada muestra: Nombre, Versión Activa, Fecha de Último Despliegue y Autor
    And al hacer clic en un proceso, se carga en el Lienzo para su edición o consulta.

  Scenario: Text Annotations (Notas Adhesivas BPMN) en el Lienzo (CA-24)
    Given el Arquitecto está diseñando un diagrama
    Then debe poder arrastrar un componente "Text Annotation" desde la Paleta al Lienzo
    And escribir comentarios explicativos que se renderizan visualmente sobre el diagrama
    And estas anotaciones se persisten en el archivo .bpmn XML como parte del estándar.

  Scenario: Zoom, Minimap y Navegación Visual (CA-25)
    Given el Arquitecto trabaja con un diagrama con más de 3 carriles y 20+ nodos
    Then el Lienzo debe soportar controles de Zoom (+/-) y "Ajustar a Pantalla"
    And un Mini-Mapa (panorámico) en la esquina inferior derecha para navegar rápidamente entre secciones lejanas del diagrama.

  Scenario: Naming Dual - Nombre de Negocio y Nombre Técnico (CA-26)
    Given el Arquitecto crea una User Task y escribe "Llenar Formulario de Crédito" como nombre visible
    Then el panel de Propiedades debe ofrecer un segundo campo: "ID Técnico (Technical Name)"
    And si el Arquitecto no lo rellena, el sistema debe auto-generar un slug (Ej: `llenar_formulario_de_credito`)
    And el motor Camunda usará el ID Técnico internamente, mientras que la UI del Workdesk mostrará el Nombre de Negocio.

  Scenario: Plantillas BPMN Prediseñadas (CA-27)
    Given el Arquitecto presiona "Nuevo Proceso" en la Pantalla 6
    Then un Modal debe ofrecer la opción "Empezar desde Cero" o "Usar Plantilla"
    And las plantillas disponibles incluyen ejemplos comunes (Ej: "Aprobación Simple", "Onboarding Cliente", "Incidencia IT")
    And al seleccionar una plantilla, se carga en el Lienzo como punto de partida editable.

  Scenario: Diff Visual entre Versiones (CA-28 - Diferido a V2)
    # NOTA: Este escenario queda documentado pero su implementación se difiere a la Versión 2 del producto.
    Given el Arquitecto navega al Historial de Versiones y selecciona v2 y v3 para comparar
    Then el sistema muestra un Diff visual resaltando nodos agregados (verde), eliminados (rojo) y modificados (amarillo).

  Scenario: Copiar y Pegar Fragmentos entre Procesos (CA-29)
    Given el Arquitecto tiene abiertos dos procesos en pestañas distintas de la Pantalla 6
    When selecciona un fragmento (Ej: un Sub-Proceso con 5 tareas) del Proceso A y ejecuta "Copiar"
    Then debe poder "Pegar" ese fragmento en el Lienzo del Proceso B
    And el sistema debe re-mapear los IDs internos para evitar colisiones XML.

  Scenario: Límite de Complejidad Parametrizable y Advertencia de Mala Práctica (CA-30)
    Given el sistema tiene configurado un umbral de complejidad máxima (Ej: 100 nodos por defecto, parametrizable)
    When el Arquitecto excede ese umbral dibujando el nodo número 101
    Then el sistema debe mostrar una advertencia visual: "⚠️ Mala Práctica de Diseño: Este proceso supera los 100 nodos"
    And debe detallar los riesgos: "Procesos complejos son difíciles de mantener, propensos a errores y degradan el rendimiento del motor"
    And la advertencia NO bloquea el despliegue, solo informa. El umbral es configurable por un Admin.

  Scenario: Etiquetas de Estado en el Catálogo de Procesos (CA-31)
    Given el Catálogo de Procesos desplegados (CA-14) lista todos los procesos
    Then cada proceso debe tener una etiqueta visual de estado:
    And "📝 BORRADOR" si nunca ha sido desplegado al motor (solo existe como XML guardado)
    And "✅ ACTIVO (v3)" si tiene al menos una versión desplegada y operativa
    And "📦 ARCHIVADO" si fue retirado de operación (CA-23).

  Scenario: Archivar un Proceso sin Instancias Activas (CA-32)
    Given el Arquitecto selecciona un proceso "Proceso_Obsoleto" en el Catálogo
    And NO existen instancias "En Vuelo" de ese proceso
    When presiona el botón [📦 Archivar]
    Then el sistema cambia el estado del ProcessDefinition a "ARCHIVADO"
    And no se podrán crear nuevas instancias de ese proceso
    And el proceso deja de estar visible para los usuarios operativos, pero permanece en BD para auditoría
    But si existen instancias activas, el botón Archivar está deshabilitado con el tooltip: "No se puede archivar: X instancias en ejecución".

  Scenario: Invalidación Automática del Pre-Flight tras Edición (CA-33)
    Given el Arquitecto ejecutó el Pre-Flight Analyzer y obtuvo resultado "✅ Sin Errores"
    When posteriormente modifica el diagrama (agrega/elimina/cambia un nodo)
    Then el estado del Pre-Flight debe resetearse automáticamente a "⚠️ Pendiente de re-validación"
    And el botón [🚀 DESPLEGAR] debe requerir una nueva ejecución del Pre-Flight antes de habilitarse.

  Scenario: Solicitar Despliegue al Release Manager (CA-34)
    Given el Designer ha terminado de diseñar y el Pre-Flight está aprobado
    When presiona el botón [📩 Solicitar Despliegue]
    Then el sistema cambia el estado del proceso a "PENDIENTE_APROBACIÓN_DESPLIEGUE"
    And crea automáticamente una tarea en el Workdesk del usuario con rol "BPMN_Release_Manager"
    And el Release Manager ve esta tarea en su bandeja con el botón [🚀 Aprobar y Desplegar] o [❌ Rechazar].

  Scenario: SLA Configurable por Tarea Individual o Global (CA-35)
    Given el Arquitecto configura un UserTask en el Panel de Propiedades de la Pantalla 6
    Then el campo "SLA" puede tener un valor específico por tarea (Ej: "4 horas" para "Analizar", "48 horas" para "Firmar")
    And adicionalmente debe existir un SLA Global a nivel de ProcessDefinition (Ej: "5 días hábiles para el proceso completo")
    And las reglas de negocio o el Diseñador definen cuál prevalece en caso de conflicto.

  Scenario: Link Directo a Sub-Proceso desde Call Activity (CA-36)
    Given el Arquitecto selecciona una Call Activity en el Lienzo que apunta al proceso hijo "Proceso_Riesgo"
    Then el Panel de Propiedades debe mostrar un link clickeable: "[🔗 Abrir Sub-Proceso: Proceso_Riesgo]"
    And al hacer clic, se abre el proceso hijo en una nueva pestaña del Diseñador para editarlo o consultarlo.

  Scenario: Colores Personalizados en Carriles y Tareas (CA-37 - Diferido a V2)
    # NOTA: Este escenario queda documentado pero su implementación se difiere a la Versión 2 del producto.
    Given el Arquitecto selecciona un Carril o Tarea en el Lienzo
    Then puede asignarle un color personalizado desde una paleta de colores para distinguir departamentos.

  Scenario: Autocompletado de Variables en Expresiones (CA-38 - Diferido a V2)
    # NOTA: Diferido a V2.
    Given el Arquitecto escribe una condición en una Compuerta Exclusiva (Ej: `${monto > 5000}`)
    Then el sistema ofrece autocompletado de variables disponibles basándose en los formularios asociados al proceso.

  Scenario: FormKey como Dropdown Validado desde Pantalla 7 (CA-39)
    Given el Arquitecto selecciona una User Task en el Lienzo de la Pantalla 6
    When accede al campo "📄 Formulario Asociado" en el Panel de Propiedades
    Then el campo debe ser un Dropdown (NO texto libre) que lista los formularios registrados en la Pantalla 7
    And cada opción del Dropdown muestra: Nombre del formulario, Tipo (🟢 Simple o 🔵 iForm Maestro), y si es Maestro, el número de etapas configuradas
    And si no se selecciona ningún formulario, el Pre-Flight lo marca como Error.

  Scenario: Consistencia de Patrón de Formulario por Proceso (CA-40)
    Given el Arquitecto crea un nuevo proceso en la Pantalla 6
    Then al inicio debe elegir el patrón de formulario: "Patrón A: Formulario Simple" o "Patrón B: iForm Maestro"
    And esta decisión es inmutable para ese proceso (consistente con US-003)
    And si eligió Patrón A, cada User Task mostrará en el Dropdown solo formularios "Simple"
    And si eligió Patrón B, todas las User Tasks compartirán el mismo iForm Maestro y el Dropdown filtrará solo formularios "Maestro".

  Scenario: Sandbox Simulado en Motor de Producción en V1 (CA-41)
    Given el iBPMS V1 opera con un único motor Camunda (no hay ambiente de Desarrollo separado)
    Then el botón [🧪 Sandbox] genera instancias temporales directamente en el motor de producción
    And estas instancias se marcan como "SANDBOX_TEST" y se auto-destruyen al finalizar la simulación
    And la separación real de ambientes (Dev vs Prod) se difiere a V2.

  Scenario: Registro de Auditoría de Diseño tipo Git-Log (CA-42)
    Given el Arquitecto realiza cualquier acción sobre un proceso (importar, editar, guardar borrador, solicitar despliegue, archivar, restaurar versión)
    Then el sistema debe crear una entrada en un log de auditoría persistente (BD) con: Acción, Usuario, Timestamp y Versión Afectada
    And este log debe ser visible para Administradores en un panel "📜 Historial de Cambios" (estilo Git Log) dentro de la Pantalla 6.

  Scenario: Lock Manual sin Expiración Automática (CA-43)
    Given el Arquitecto "maria.lopez" tiene el Lock sobre un proceso
    And permanece inactiva por más de 30 minutos
    Then el Lock NO expira automáticamente
    And otros usuarios que intenten editar verán: "🔒 Bloqueado por maria.lopez. Contacte al usuario para solicitar la liberación."
    And la liberación es un proceso manual: María debe cerrar su pestaña o presionar un botón "Liberar Edición".

  Scenario: Soporte Multi-Pool para Modelado de Colaboración (CA-44)
    Given el Arquitecto crea un nuevo diagrama BPMN en la Pantalla 6
    Then puede agregar múltiples Pools al Lienzo representando actores internos y externos (Ej: "Mi Empresa", "Banco Externo", "Proveedor")
    And puede conectar los Pools con Message Flows (flechas de mensaje) para modelar la interacción
    And los Pools externos son representaciones visuales (cajas negras) que no se ejecutan en el motor Camunda interno
    And esto provee claridad documental y de auditoría sobre quién habla con quién.

  Scenario: Service Task con Dropdown de Conectores API del Hub (CA-45)
    Given el Arquitecto coloca una Service Task en el Lienzo y abre su Panel de Propiedades
    Then el campo "Conector / API" debe ser un Dropdown que lista los conectores registrados en la Pantalla 11 (Hub de Integraciones)
    And cada opción muestra: Nombre del conector, Tipo (REST/SOAP/GraphQL) y Sistema Destino
    And para V1, los conectores pre-armados obligatorios son:
    And - 📧 Microsoft O365 / Exchange (Correo corporativo)
    And - 📁 Microsoft SharePoint (Gestión documental)
    And - 💰 Oracle NetSuite (ERP/Financiero)
    And si el conector necesario NO existe aún en el Hub, consultar CA-37.

  Scenario: MessageEvent como Placeholder de Integración Futura (CA-46)
    Given el Arquitecto necesita modelar una integración con un sistema externo cuyo conector API aún no fue registrado en el Hub (Pantalla 11)
    Then debe usar un MessageEvent (Intermediate Throw/Catch) como marcador visual temporal
    And el Pre-Flight Analyzer debe clasificar este nodo como Advertencia (⚠️): "MessageEvent sin conector API asociado. Considere crear el conector en el Hub y migrar a Service Task."
    And cuando el conector sea registrado posteriormente, el Arquitecto puede reemplazar el MessageEvent por una Service Task enlazada al nuevo conector.

  Scenario: [Onboarding Embebido] Iconos de Ayuda Globales en el Diseñador (CA-47)
    Given el Arquitecto (de cualquier perfil o seniority) selecciona un componente visual en el Lienzo de la Pantalla 6
    Then tanto en la barra superior de herramientas como al lado de cada título del Panel de Propiedades aparecerá un ícono de ayuda `[?]`
    And al hacer hover, el sistema desplegará el Componente de Tooltip Estándar (reutilizado de la US-003).

  Scenario: Tooltips Ricos interactivos y Mapeo de Errores de Sintaxis (CA-48)
    Given la visualización del Tooltip en el Diseñador BPMN
    Then el contenido didáctico estará codificado de forma estática ("quemado") para la V1
    And el componente soportará formato HTML enriquecido permitiendo incrustar hipervínculos azules hacia la documentación oficial
    When el Arquitecto ingresa una expresión inválida o código basura en un campo de configuración (Ej: Listener Script o Condición de Gateway)
    Then el ícono de ayuda y su respectivo Tooltip mutarán dinámicamente a color ROJO para alertar el error de sintaxis visualmente.

  Scenario: Mapeo Visual Estricto (Prohibición de JSON Crudo) (CA-49)
    Given que el Arquitecto selecciona un Conector API (Ej: Oracle) en una Service Task (Pantalla 6)
    When el Frontend despliega el sub-panel de Integración
    Then el sistema TIENE ESTRICTAMENTE PROHIBIDO renderizar un `<textarea>` libre para inyección manual de JSON Payload.
    And debe renderizar un componente `<DataMapperGrid>` de dos columnas: Columna Izquierda (Campos fijos dictados por el Swagger del Hub en Pantalla 11) vs Columna Derecha (Dropdown interactivo).
    And el Dropdown de la derecha consumirá el Diccionario de Datos del proceso (Variables Zod de la Pantalla 7), permitiendo al usuario emparejarlas visualmente con clics.

  Scenario: Coerción Inteligente y Seguridad de Tipos (Type-Safety) (CA-50)
    Given la matriz de mapeo visual `<DataMapperGrid>`
    When el usuario despliega la lista de variables origen para emparejarlas con un destino
    Then el Frontend aplicará un filtro dinámico: mostrará deshabilitadas (sombreadas en gris) con un tooltip explicativo de "Tipo Incompatible" a aquellas variables (Zod) cuyo tipo de dato (String, Number, Boolean) NO coincida matemáticamente con el tipo esperado por el sistema externo.
    And anulando desde el diseño de la UI la posibilidad de enviar un Error 400 (Type Mismatch) a Producción.

  Scenario: Inyección de Valores Constantes (Hardcoding Controlado) (CA-51)
    Given que la API externa requiere un dato que no proviene del Formulario del cliente (Ej: `Country_Code`)
    Then la Columna Derecha del `<DataMapperGrid>` permitirá al usuario alternar entre [Variable Dinámica Zod] y [Valor Estático].
    And si elige [Valor Estático], podrá digitar el texto crudo inyectándolo de forma segura en el Payload saliente.

  Scenario: Inmutabilidad Estricta ante Mutación de Swagger (Zero-Breakage) (CA-52)
    Given un proceso V1 desplegado que utiliza el conector `Oracle_API_v1`
    When el Administrador actualiza el contrato (Swagger) en el Hub (Pantalla 11) renombrando o eliminando variables esperadas
    Then el sistema bloquea la sobrescritura y fuerza la creación de un nuevo conector `Oracle_API_v2`
    And el proceso V1 que ya estaba en el motor sigue funcionando intacto con la versión vieja en caché ("Zero-Breakage Policy")
    And si el Arquitecto desea usar la nueva versión, debe entrar a la Pantalla 6, seleccionar la v2, re-mapear y desplegar una nueva versión temporal del proceso.

  Scenario: Validación Lógica de Cláusulas OneOf/AnyOf (CA-53)
    Given una API que exige el dato X *o* el dato Y mediante las cláusulas Swagger (OneOf / AnyOf)
    When el Frontend despliega el `<DataMapperGrid>`
    Then agrupa visualmente las filas afectadas bajo la etiqueta `[ 🔀 Requiere mapear al menos UNO ]`
    And el Pre-Flight Analyzer verificará el grupo lógico en conjunto: Si falta al menos uno, alerta roja y aborta despliegue. Si ambos están vacíos, aborta. Si uno está lleno, autoriza el pase a Producción.

  Scenario: Shift-Left Security para Datos Sensibles (PII/PHI) (CA-54)
    Given el mapeo de una variable clasificada con el flag `[🔒 Dato Sensible PII]` desde la Pantalla 7 (Zod)
    When la Service Task dispara la integración hacia la API externa
    Then el dato crudo viaja obligatoriamente encriptado por el túnel HTTP/TLS
    And el motor de auditoría histórica de Camunda (History Level) tiene estrictamente PROHIBIDO persistir el valor real en texto plano dentro de sus logs, reemplazándolo obligatoriamente por un hash o la viñeta `[REDACTED_PII]`.

  Scenario: Mapeo Reestringido de Headers Dinámicos (CA-55)
    Given que la API exige metadatos de usuario por cada transacción (Ej: `User_ID`) en las cabeceras REST
    Then el Data Mapper ofrecerá una tercera pestaña visual denominada `[ 🔑 HEADERS DINÁMICOS ]`
    And la UI aplicará severas restricciones denegando la inserción de texto libre o crudo para prevenir Header Injection.
    And obligará a mapear valores usando únicamente variables pre-validadas del formulario (Zod) o Macros seguras del Sistema.

  Scenario: Delegación Transparente de Conversión Binaria (Multipart/Base64) (CA-56)
    Given un componente Zod de tipo `<InputFile>` mapeado hacia un atributo del Payload destino
    When el Arquitecto despliega y llega el momento de la ejecución
    Then el flujo UI no exige que el Arquitecto indique la técnica de conversión
    And el Worker (Backend) intercepta el mapping, consulta en caliente el requerimiento del Swagger (Multipart-FormData vs Base64), y lo transmuta automáticamente antes de inyectar la data a la trama HTTP de salida.

  Scenario: Ley de Omisión Pura de Llaves Nulas (Drop Key by Default) (CA-57)
    Given una variable Zod marcada como Opcional que el usuario no diligenció en el Runtime (cuyo valor es `null` o vacío)
    When la petición es empaquetada hacia el sistema remoto
    Then el Backend aniquila y purga la llave entera ("Key") del JSON saliente, evitando enviar sintaxis propensa a crashes (Ej: `"campo": null`)
    And la única excepción será si el Swagger explicíta la obligación del campo como `nullable: true`, obligando al envío literal.

  Scenario: Resiliencia Asíncrona Parametrizable (Retry Pattern Visual) (CA-58)
    Given la configuración de una Task API Integrada en Pantalla 6
    Then el sistema expone un sub-panel `[ ⚙️ Estrategia de Fallo (Retries) ]`
    And permite configurar intentos asíncronos y ventana retardo temporal (Ej. 3 intentos espaciados por 5 mins)
    And los reintentos operan como Background Jobs (Job Executor) liberando ram de la UI, y si la póliza se agota, canaliza automáticamente el Thread BPMN hacia el Boundary Error Event modelado de rescate humano.

  Scenario: Amnesia Selectiva Obligatoria de Datos No Mapeados (Output Pruning) (CA-59)
    Given una respuesta de la API externa que retorna un Payload gigante (Ej: JSON de 15 MB)
    And el Arquitecto sólo enlazó visualmente 1 variable diminuta (`Ticket_ID`) en la pestaña de `[ 📥 OUTPUT MAPPING ]`
    When arriba el Payload y se graba el Ticket_ID en el Bolsillo Global de Variables (Process Runtime)
    Then de manera sincrónica el motor de Camunda invoca la poda total (Garbage Collection)
    And destruye los remanentes masivos no procesados liberando I/O y evitando contaminar la Base de Datos operativa del motor BPM.
```
**Trazabilidad UX:** Wireframes Pantalla 6 (Diseñador BPMN) y Pantalla 14 (RBAC).

---

### US-006: Diseñar la Estructura Base (WBS) de una Plantilla de Proyecto
**Como** PMO / Director de Proyectos / Administrador
**Quiero** crear una Plantilla Maestra definiendo jerárquicamente las Fases y Tareas Secuenciales, y pre-asignar Formularios a cada tarea genérica
**Para** que exista un molde estandarizado (WBS) que evite re-trabajo cuando un Gerente desee instanciar un proyecto nuevo (ya sea usando metodología Tradicional/Gantt o metodología Ágil).

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Standalone Project Template Builder (WBS)
  Scenario: Profundidad Controlada de WBS (CA-1)
    Given que el PMO diseña una estructura jerárquica en la Pantalla 8
    When el usuario intenta anidar tareas de forma vertical ("Sub-fase de Sub-fase de Tarea")
    Then el sistema restringe estructuralmente la profundidad a un máximo estricto de 5 Niveles
    And si se intenta exceder, deshabilita el botón "+" y proyecta una advertencia de tope arquitectónico ("Profundidad Máxima Alcanzada").

  Scenario: Versionamiento Seguro de Plantillas Vivas (CA-2)
    Given una Plantilla V1.0 que está siendo utilizada y consumida por 50 Proyectos Vivos
    When la PMO edita la plantilla (Ej. Le agrega 3 Fases nuevas) y oprime [Actualizar Producción]
    Then el sistema NO muta los 50 proyectos vivos (se anclan al Snapshot originario inmutable V1.0)
    And emite la versión V2.0 exclusivamente disponible para nuevas aperturas de Proyectos, requiriendo en paralelo Aprobación Administrativa mediante un Botón Rojo Fuerte [Pushear Nueva Versión] para forzar validación por partida doble.

  Scenario: Tipificación Estricta de Plantilla (Tradicional vs Ágil) (CA-11)
    Given que la PMO acciona la creación de una Nueva Plantilla en la Pantalla 8
    When el sistema levanta el Modal de Creación
    Then obliga explícitamente a clasificar la plantilla seleccionando un tipo rígido: `[Tradicional (Gantt)]` o `[Ágil (Sprints)]`
    And esta clasificación gobierna el comportamiento del lienzo: Si elije "Ágil", el botón de relacionar dependencias (Fin-a-Inicio) desaparece permanentemente del UI y se prohíbe crear conceptos estructurales como "Hitos".

  Scenario: Transición Formulario a DONE en Ágil (CA-12)
    Given una tarea instanciada en el Tablero Kanban (Ágil) originada desde una Plantilla
    And esta tarea tiene el "Formulario_QA" asociado en su definición maestra
    When el desarrollador termina el trabajo y oprime enviar el formulario
    Then el sistema autoevalúa la completitud de la data y, en caso de éxito, arrastra logísticamente la tarjeta a la columna "DONE" del Sprint, aplicando un Definition of Done duro atado a data.

  Scenario: Independencia Evolutiva Locativa (CA-13)
    Given un Scrum Master que instanció un Proyecto Ágil basado en la Plantilla V1.0
    When el Scrum Master elimina 5 de las tareas heredadas del Backlog local del proyecto porque no aplican a su Sprint
    Then el borrado es estrictamente Local (Muta solo el Proyecto Instanciado)
    And la Plantilla original inmutable "V1.0" no pierde las tareas orgánicamente y futuros proyectos las seguirán heredando intactas.
```
**Trazabilidad UX:** Wireframes Pantalla 8 (Project Template Builder).

---

### US-030: Instanciar y Planificar un Proyecto Ágil (Sprints/Kanban)
**Como** Scrum Master / Agile Coach
**Quiero** instanciar un nuevo proyecto Ágil utilizando una estructura base (WBS) y gestionar su Backlog
**Para** poder planificar iteraciones, asignar responsables directos y liberar tareas hacia los tableros Kanban operativos (Pantalla 3).

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Agile Project Instantiation and Planning
  Scenario: Instanciación sin Sprints en V1 (Postergación Táctica) (CA-14)
    Given un proyecto instanciado bajo metodología Ágil en la Pantalla 9
    When el líder de proyecto abre el Agile Hub (Pantalla 10)
    Then el sistema NO utiliza iteraciones con fechas (Sprints) para la Versión 1 del producto
    And el lienzo funciona como un Tablero General de Kanban Continuo (Flujo sin Timebox) donde las tareas se mapean directamente de ToDo a Done, aplazando el marco Scrum complejo para V2.
```
**Trazabilidad UX:** Wireframes Pantalla 9 (Gestor de Proyectos) y Pantalla 10 (Hub Ágil).

---

### US-031: Planificación y Ejecución de Proyecto Tradicional (Gantt)
**Como** Project Manager (Tradicional)
**Quiero** visualizar un proyecto instanciado como un diagrama de Gantt, asignar mis recursos, presupuestos y fijar la Línea Base
**Para** que el motor de orquestación (Camunda) inicie la ejecución automática del proyecto despachando la primera secuencia de tareas a las bandejas (Workdesk) de los asignados.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Traditional Project Planning and Baseline Execution
  Scenario: Geometría Adaptativa por Colisión con Días Festivos (CA-3)
    Given la tarea X planificada para el lunes 12, con duración de 3 días laborables
    When el calendario maestro global marca repentinamente el lunes 12 como "Día Festivo Nacional"
    Then el motor de cálculos del Diagrama de Gantt estira automáticamente la caja visual de la tarea hacia la derecha compensando el día muerto (Fin: Jueves 15) sin requerir re-planificación humana obligatoria.

  Scenario: Protección Estructural contra Deadlocks Circulares (CA-4)
    Given que el PM crea dependencia "T1 -> T2" (Fin-Inicio) arrastrando flechas en el Lienzo 10.B
    When el PM arrastra erróneamente la dependencia contraria "T2 -> T1" creando un Ciclo Infinito
    Then el WebClient bloquea y aborta inmediatamente el cruce relacional (Error Geométrico visual) e impide guardarlo en la Base de Datos para garantizar un motor DAG limpio.

  Scenario: Sobrecarga Permisible con Semáforo Sensorial (CA-5)
    Given la matriz de 40 horas laborables semanales para un humano
    When el PM planifica tareas apiladas sobre la empleada "María" superando el 150% de su capacidad en la misma semana cronológica
    Then el sistema "permite" teóricamente la mala práctica (dejando al PM violar la métrica)
    And como contramedida, enciende agresivas Balizas Visuales Rojas (Marcador de Recurso Sobrecargado) a un costado del nombre de la analista.

  Scenario: Re-planificación Activa y Multi-Líneas Base (Baseline Rupture) (CA-6)
    Given un proyecto que lleva 2 meses en Ejecución Viva (Basado sobre Línea Base "V1")
    When el PM requiera estirar los tiempos un 30% a solicitud formal del cliente
    Then el sistema permite pausar y "Reprogramar" formalmente el nodo vivo en el lienzo visual de la Pantalla 10.B
    And fuerza al PM a guardar y pisar una nueva Línea Base Evolutiva (Ej: V2_Reprogramada), preservando en el log histórico la desviación financiera/temporal ocurrida frente al V1 primitivo para auditoría de Gerencia.

  Scenario: Hot-Swaps en Cabina de Mando (Reasignación de Silla Ejecutiva) (CA-7)
    Given una tarea vital (T4) de Línea Base activa rebotando infructuosamente en el Workdesk del analista 'Pedro' por su ausencia repentina
    When el Project Manager se adentra en la Pantalla 10.B (Cabina General Gantt Transaccional) e invoca la tarjeta temporal viva (T4)
    Then el sistema posibilita el borrado nominal en duro de 'Pedro' para inyectar sobre vuelo el usuario 'Luis'
    And el motor BPMN retira perentoriamente la carta de la delegación de Pedro, materializándola sincrónicamente en el Workdesk de su co-equipero para no frustrar la métrica de entrega del T4.

  Scenario: Modos Flexibles de Reclamo (Pool vs Empleado Directo) (CA-8)
    Given la responsabilidad del PM de instanciar tareas en el motor Gantt
    Then el PMo goza del Switch parametrizable de Asignamiento en su UX
    And ostenta la facultad imperativa de designar nominalmente la Tarea Hacia un Usuario Exacto (`maria.lopez`)
    And o puede prescindir de asimetrías tácticas y tirarlo en bandeja común al Grupo Jerárquico General ("Equipo Legal"), forzando que ellos ejerzan Auto-Apropiación (US-002: Claim Task) por competencia.
    
  # NOTA CONTEXTUAL PO: (CA-9 Camino Crítico PERT) y (CA-10: Avance Financiero EVM) diferidos expresamente a V2 del MVP.
```
**Trazabilidad UX:** Wireframes Pantalla 10.B (Planner Tradicional - Gantt) y Pantalla 1 (Workdesk).

---

### US-027: Copiloto IA (Auditoría ISO 9001 y Generador Consultivo BPMN)
**Como** Arquitecto Modelador de Procesos
**Quiero** un asistente IA interactivo embebido en el diseñador (Pantalla 6)
**Para** que audite mis diagramas buscando brechas de calidad (ISO 9001), O genere un proceso BPMN 2.0 desde cero a partir de documentos adjuntos e iteraciones de preguntas aclaratorias en lenguaje natural.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: AI Copilot para Diseño y Auditoría BPMN

  Scenario: Diagnóstico de Calidad y Sugerencia ISO 9001 (Modo Tutor Alerta)
    Given que el Arquitecto está dibujando un proceso en el lienzo y hace clic en "Consultar a IA Copilot"
    When el sistema envía el XML en memoria y el contexto semántico al LLM API
    Then el Agente IA debe analizar la estructura devolviendo un reporte en la UI
    And destacar áreas de mejora (Ej: "⚠️ Alerta ISO 9001: La compuerta no tiene validación humana")
    And sugerir componentes correctos de BPMN 2.0 para reemplazar antipatrones
    But la IA NUNCA bloqueará el diseño o el despliegue; emitirá mensajes de alerta y el usuario Arquitecto tendrá la decisión final vinculante.

  Scenario: Ingesta Documental, RAG Teórico y Setup Inicial (Modo Generador Analógico)
    Given un lienzo en blanco en la Pantalla 6
    When el Arquitecto activa el modo "Generar Proceso con IA"
    Then el sistema despliega un chat conversacional y habilita un área de Dropzone
    And permite adjuntar archivos de soporte de negocio (Ej: Manuales PDF, Procedimientos DOCX) junto con un prompt inicial de intención conceptual
    And el Agente IA Backend cruza este prompt contra una Base de Conocimiento "RAG Interna" pre-cargada con el estándar nativo BPMN 2.0 (Camunda Engine Syntax), manuales ISO 9001 y patrones arquitectónicos de iBPMS para anclar el contexto y anular las alucinaciones técnicas desde el minuto 0.

  Scenario: Entrevista Aclaratoria Anti-Alucinación de Mapeo de Roles (Iterative Prompting)
    Given que el LLM procesa la solicitud inicial, el RAG interno y los documentos adjuntos
    When el motor detecta vacíos lógicos procedimentales o roles documentales no existentes en el sistema actual (Ej: "Detecto Rol 'Gerencia', mapear a existente o crear genérico?")
    Then en lugar de inventar Lanes o lógica (Alucinación Zero-Tolerance), devuelve una pregunta aclaratoria al Arquitecto en el chat
    And el proceso de renderizado de cajas se pausa estrictamente hasta que el humano resuelva funcionalmente la duda conversacionalmente.

  Scenario: Renderizado Automático de XML en el Lienzo (Hit-the-Canvas)
    Given que la IA ha recolectado y entendido el 100% del requerimiento lógico
    When el Arquitecto da la orden final de "Generar Diagrama"
    Then el backend (Agentic Engine) estructura y valida el esqueleto XML basado en el estándar rígido BPMN 2.0 (restringido por el RAG semántico)
    And el Frontend de la Pantalla 6 lo inyecta y dibuja visualmente en el lienzo nativo (`bpmn-js`) utilizando una librería genérica de `Auto-Layout` estructural (arborescente de Izquierda a Derecha)
    And el Arquitecto retoma el control manual absoluto para conectar formularios, mapear APIs y embellecer/reacomodar las coordenadas (X, Y) antes de oprimir el botón de [DESPLEGAR].

  Scenario: Límite de Parada Anti-Fatiga en Entrevista (Triage Batching)
    Given la activación del modo Entrevista Aclaratoria Anti-Alucinación
    When el LLM detecta un alto volumen de vacíos lógicos (Ej: 15 excepciones no documentadas en el PDF)
    Then la IA dosificará las preguntas obligatoriamente en lotes de máximo 3 consultas por interacción en el chat
    And preservando la UX del Arquitecto contra la fatiga cognitiva hasta resolver el 100% de la ambigüedad por etapas.

  Scenario: Segregación Crítica de Formularios (Separation of Concerns hacia US-003)
    Given que la IA modela de forma exitosa requerimientos de intervención humana (`User Task`)
    When el PDF especifica los campos del formulario (Ej: Nombre, Ingresos, Cédula)
    Then el Agente BPMN se abstiene explícitamente de generar el "Esquema VUE/Zod" interno para cumplir con el Principio de Responsabilidad Única
    And únicamente anclará un componente nativo de BPMN 2.0 (`TextAnnotation` o "Nota Adhesiva") sobre la tarea visual dictando: "AI Info: Construir aquí el Formulario Zod con el Auxiliar de la US-003".

  Scenario: Trazabilidad y Auto-Destrucción del Hilo (Storage Management)
    Given la culminación exitosa de la generación conversacional y el despliegue de la versión en el motor
    Then el Agente purga de su memoria volátil el chat para preservar almacenamiento
    And extrae un resumen del hilo lógico como "Bitácora Copilot" persistiendo ese documento anexo inamovible a la Verisón del XML en la Base de Datos para futuras auditorías de compliance.

  Scenario: Edición Conversacional Retroactiva (Soft-Undo)
    Given que el Arquitecto comete un error en alguna instrucción en el chat de la Pantalla 6
    Then el sistema permite modificar o corregir textualmente su mensaje previo enviado a la IA
    And el motor re-calibra recursivamente la lógica y las cajas asimilando la nueva reescritura.

  Scenario: Límites de Contexto Perimetral Documental
    Given el proceso de subida de adjuntos (Dropzone) inicial 
    When el usuario intenta subir anexos que exceden los límites paramétricos
    Then la plataforma impone limitantes restrictivos duros: Topecito estadístico de "5 archivos máximo" o un consolidado de "100 páginas en total" para impedir ataques de agotamiento por Tokens hacia los Endpoints de Vertex/OpenAI.

  Scenario: Diseño Paramétrico de Ramificaciones (Gateways vs Timers)
    Given la traducción lingüística a XML estructurado por la IA
    When la IA reconoce desvíos en el flujo natural del negocio
    Then forzosamente diagramará un `ExclusiveGateway` riguroso conectado a Expresiones Variables JSON (`${credito == 'rechazado'}`)
    And ante indefiniciones temporales ("esperar una respuesta") delegará por omisión la designación a Tareas Manuales (`UserTask`) sin arriesgar Timer Events ciegos no explícitos en el documento original.

  Scenario: Tratamiento de Reintentos Críticos y Roles Fantasmas
    Given la identificación de Integraciones a Sistemas Externos (Ej. Buró de Crédito)
    When la IA pinta un `ServiceTask`, consultará proactivamente al humano sobre planes B (Caminos compensatorios / HTTP 500)
    And no tendrá potestad en absoluto de transar Usuarios, Grupos o Roles Active Directory en duro en Pantalla 14, conformándose ciegamente con renderizar *Lanes* referenciales.

  Scenario: Seguridad Perimetral y Red-Teaming (Prompt Injection)
    When el Arquitecto intenta violentar las directrices ordenando intromisión ajena al diseño (Ej. Inyección SQL, Hackeo o Consultas exógenas)
    Then la IA retornará neutral y corporativamente: "Lo siento, mi configuración bloquea este comportamiento. Solo puedo diseñar diagramas BPMN"
    And silenciosamente detona el disparo de una Alerta Security levantando un *Flag* del Sistema de Auditoría (US-042) hacia el Rol causante.

  Scenario: Modos Borrador Restringidos (Executable Flag)
    Given un flujo con excepciones críticas (Caminos rotos) inaceptadas por Camunda Engine
    When el Arquitecto se obstina en inyectar el diagrama Hit-the-Canvas a medio digerir
    Then la IA pinta el XML pero somete el Diagrama asignando forzósamente la etiqueta `<bpmn:process isExecutable="false">` 
    And incapacitando dicho Proceso de Instanciarse Operativamente por cualquier Botón de P0, blindando la salud del motor hasta que otro Arquitecto lo corrija manualmente.

  Scenario: Feedback Loop Distribuido de Aprendizaje Continuo
    Given el modelo final exportado al lienzo visual y modificado mecánicamente por el operador mediante flechas manuales o borrados
    Then el backend recogerá esa asimetría entre "Lo que sugirió la IA" vs "Lo que el humano dejó final"
    And enviará la traza (Deltas de Modificación) al repositorio vectorial (RAG) o como fine-tuning pasivo del tenant para automejorar y acentuar las inferencias corporativas de cara a futuros diseños similares.
```
**Trazabilidad UX:** Wireframes Pantalla 6 (Diseñador BPMN - Panel Lateral de Copilot interactivo y Dropzone).

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

  Scenario: Prevención de Variables Fantasma (Diccionario Restringido)
    Given el Taller DMN con IA (Pantalla 4)
    When el usuario redacta el prompt con la regla de negocio
    Then la interfaz obliga al usuario a mapear textualmente sus palabras contra un Dropdown Selectivo de "Variables Disponibles" (Inputs/Outputs extraídos de los Formularios previos)
    And el LLM rechaza procesar columnas que no existan en el diccionario duro.

  Scenario: Acoplamiento Rígido de Rutas de Salida (BPMN Outputs)
    Given que la DMN direcciona el flujo BPMN (Ej: "Aprobado" va a Tarea A, "Rechazado" va a Tarea B)
    When el LLM genera la columna de Output de la Matriz DMN
    Then el Agente IA Backend restringe el vocabulario de salida del LLM para que coincida 100% con los "Gateway Paths" existentes en el diagrama de la Pantalla 6, previniendo errores de enrutamiento.

  Scenario: El LLM como Auditor Lógico Estructural (Anti-Contradicciones)
    Given una regla donde el usuario escribe "Rechazar montos >100" y "Aprobar montos >50"
    When envia el prompt a generar
    Then el LLM detecta matemáticamente la colisión de los rangos para $150
    And en vez de generar XML basura, aborta el proceso retornando un "Error de Sintaxis de Negocios" indicándole al humano la contradicción en el chat.

  Scenario: Red de Seguridad por Defecto (Catch-All Rule)
    Given que el LLM genera exitosamente una tabla DMN de 5 reglas lógicas
    Then el sistema inyecta incondicional y automáticamente una 6ta regla final (Hit Policy: ANY/UNIQUE)
    And esta fila actúa como "Catch-All": Si los inputs no hacen match con ninguna de las 5 reglas, el Output forzado será "Requiere Revisión Humana", evitando fallas silenciosas en producción.

  Scenario: Human in the Loop (Aprobación Visual DMN)
    Given el XML generado por el LLM en el backend
    When retorna al Frontend
    Then el usuario no lo inyecta automáticamente a producción
    And es redirigido a una "Calculadora Visual" (Spreadsheet Mode)
    And para activarlo, el usuario debe oprimir explícitamente [Sellar y Aprobar DMN].

  Scenario: Edición Analógica Post-IA
    Given la matriz DMN visual generada en la UI
    When el usuario detecta que el 95% está bien pero un rango está mal
    Then puede editar numéricamente la celda de la columna en duro, omitiendo al LLM para afinar el 5% restante sin reprocesar el prompt.

  Scenario: Versionamiento y Gobernanza Viva
    Given una tabla DMN activa (V1) corriendo en Camunda
    When el usuario pide a la IA cambiar los topes financieros produciendo V2
    Then V2 queda en estado "Borrador de DMN"
    And se requiere la aprobación explícita de un "Administrador del Sistema" mediante un botón maestro para publicar la V2
    And las tareas en ejecución hoy seguirán transitando bajo las leyes de la V1.

  Scenario: Arquitectura LLM Hexagonal (Agnóstica)
    Given el llamado del Backend a la API del Modelo de IA
    Then el código se estructura usando Patrón Adaptador/Puerto
    And permite cambiar mediante variable de entorno entre `OpenAI GPT-4o`, `Claude 3 Opus` o `Google Gemini` según evolución de leyes de datos sin refactorizar el núcleo del Taller DMN.

  Scenario: Auditoría Forense de Prompts
    Given la generación final de un archivo `.dmn`
    Then el sistema graba en BD el XML generado junto con la llave foránea del usuario ejecutor, la estampa de tiempo, y el "Texto Prompt Exacto" que originó esta matriz, como prueba forense ante auditorías legales.

  Scenario: Límites de Complejidad Configurables
    Given un usuario intentando armar una red de políticas masivas (Ej. 500 ramificaciones)
    When el sistema detecta que la tabulación excede el límite de N variables (Configurable por Administrador, Ej. max 50 filas)
    Then bloquea la generación previniendo desbordamiento del contexto del LLM y sugiriendo subdividir el flujo.

  Scenario: Modo Desarrollador (Bypass de IA y Autoría Manual)
    Given un usuario técnico (Ej. Desarrollador o Arquitecto Avanzado) en la Pantalla 4
    When decide no utilizar el chat de Inteligencia Artificial
    Then el sistema debe proveer una pestaña de "Autoría Manual"
    And permitirle construir la tabla DMN usando la interfaz típica de Spreadsheet (Hoja de cálculo) desde cero
    And o permitirle hacer un Drag & Drop para importar un archivo `.dmn` externo (Ej. creado en Camunda Modeler de escritorio) directamente al motor.

  Scenario: Reutilización Modular de Reglas DMN (Globales) (CA-12)
    Given la creación exitosa de un Modelo DMN ("Scoring Riesgo") en la Pantalla 4 conectado al Proceso A
    When el Analista diseña un Proceso B totalmente distinto en la Pantalla 6
    Then el sistema le permite invocar y enlazar esa misma tabla DMN "Scoring Riesgo" existente
    And fomentando la reutilización transversal de políticas sin duplicar lógica en el motor.

  Scenario: Aislamento de Responsabilidad Temporal (Sin Date-Math IA) (CA-13)
    Given el Taller DMN interactivo
    When el usuario solicita reglas basadas en intervalos de tiempo complejos (Ej. "Si pasaron 30 días")
    Then el sistema recomienda en su UI utilizar "Timers" nativos de Camunda (Eventos BPMN)
    And no exige a la IA inferir operaciones matemáticas de fechas en la V1 protegiendo la fiabilidad.

  Scenario: Política Defensiva Obligatoria en Datos Nulos (CA-14)
    Given una columna de entrada donde el usuario olvidó digitar el valor en la Pantalla 7
    When el motor ejecuta el caso evaluando "Null" contra las reglas condicionales
    Then la tabla DMN autogenerada por la IA debe contener SIEMPRE una directriz que intercepta el valor "Null"
    And lo redirecciona obligatoriamente por diseño hacia una salida de precaución (Ej. "Revisión Humana").

  Scenario: Política de Restricción Rígida de Choque (Hit Policy UNIQUE) (CA-15)
    Given la naturaleza abstracta y no determinista de los usuarios de negocio
    When la IA transita del NLP hacia la estructura DMN formal
    Then el motor de generación encripta estructuralmente la tabla a Hit Policy "UNIQUE"
    And garantizando matemáticamente que solo 1 regla podrá ser verdadera a la vez, erradicando fallos catastróficos por traslape numérico de negocio.

  Scenario: Escalabilidad Estructural de Pantallas P6 > P7 > P4 (CA-16)
    Given que el Arquitecto está diagramando en la Pantalla 6 pero olvidó crear el Formulario (Pantalla 7) antes
    When selecciona la [Business Rule Task] y necesita variables para armar la red DMN
    Then el Modal invocado le permite "Crear Nuevo Formulario al Vuelo" (Mini-Pantalla 7) sin perder el progreso de su diagrama
    And una vez creadas las variables rápidas (P7), el flujo lo salta a la Pantalla 4 (Taller DMN) para mapearlas.
```
**Trazabilidad UX:** Wireframes Pantalla 4 (Taller DMN) y su invocación desde Pantalla 6 (Diseñador BPMN).

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
  Scenario: Propagación de Estado en Tiempo Real (Websockets)
    Given el tablero del proyecto "Implementación ERP" con columnas "TODO", "DOING", "DONE"
    And la tarea Kanban "KT-050" está en estado "TODO"
    When el usuario realiza un PATCH a "/api/v1/projects/kanban/tasks/KT-050/status"
    And el payload es '{"new_status": "DOING"}'
    Then el sistema debe retornar HTTP STATUS 200 OK
    And actualizar el timestamp de "last_modified" en la tabla 'ibpms_kanban_tasks'
    And el payload de respuesta debe retornar el objeto completo serializado `{ "id": "KT-050", "status": "DOING", "version": 2 }`
    And la UI debe propagar el evento vía WebSockets para que la tarea "KT-050" se refleje en la columna "DOING" para los demás miembros del equipo conectadas al tablero

  Scenario: Trazabilidad Cualitativa del Bloqueador (CA-1)
    Given una tarjeta en progreso dentro del Kanban
    When el desarrollador la arrastra a la columna "Blocked" (Impedimento)
    Then la interfaz levanta obligatoriamente un Modal exigiendo el Motivo del Bloqueo
    And el SLA (reloj de entrega) de la tarea NO se congela, continuando su conteo natural para mantener la fidelidad de la métrica operativa.

  Scenario: Inmutabilidad de Formularios en Completitud (CA-2)
    Given una tarjeta que acaba de aterrizar en la columna "DONE" habiendo validado el formulario
    When el usuario intenta modificar las variables o el formulario histórico
    Then el sistema renderiza la data en modo "Solo Lectura" absoluto
    And rechaza cualquier POST de actualización en el Backend para evitar alteraciones a la historia forense del negocio.

  Scenario: Independencia del Timer (Esfuerzo Humano) vs Reloj SLA (CA-3)
    Given una tarjeta Kanban que posee un reloj SLA global (Tiempo Total de Entrega) corriendo en contra desde su creación
    When el operario necesita registrar sus "Horas Sudadas" (Esfuerzo neto / Billable Hours)
    Then la interfaz provee un módulo de "Time-Tracking" manual (Digitación acumulativa o botón [Start/Stop Timer]) totalmente independiente del SLA
    And la disponibilidad de este Timer es gobernada rígidamente por la Columna en la que resida la tarjeta:
    And - En [TODO]: El Timer está oculto y bloqueado (No se puede trabajar sin arrastrarla).
    And - En [DOING]: El Timer está habilitado para Play/Stop a voluntad cuantas veces requiera.
    And - En [BLOCKED]: El Timer sigue disponible (Garantizando el cobro del tiempo usado para "des-bloquear" la tarea).
    And - En [DONE]: El Timer se bloquea y apaga definitivamente, sellando la sumatoria histórica.

  Scenario: Anti-Multitasking de Propiedad (Single-Assignee) (CA-4)
    Given el despliegue de las tarjetas Kanban extraídas de la Plantilla WBS
    When la Tribu o el Líder intentan asignar una tarjeta a dos personas para trabajo conjunto
    Then el motor restringe de raíz la operación, imponiendo una política estricta de 1:1 (Un Solo Dueño por Tarjeta)
    And garantizando así que no haya dilución de responsabilidad del SLA.

  Scenario: [Arquitectura] Prohibición de Motor CMMN y Reglas de Instanciación Ágil
    Given un Scrum Master instanciando un Proyecto derivado de la Plantilla Tipificada "Agile Sprint" (US-006)
    When la plataforma de iBPMS inyecte las tarjetas de tareas ("To Do") en el Motor Transaccional
    Then el Backend prohíbe la creación de diagramas rígidos `.cmmn` 
    And persiste la anatomía transaccional de cada tarea "Ágil" como meros registros de Base de Datos Relacional (`Entities`) enlazados a su Proyecto instanciado, usando el poder crudo de Spring Data JPA.

  Scenario: [Arquitectura] Máquina de Estados Pura (State Machine) frente al Salto Anárquico 
    Given la volatilidad de un Tablero Kanban donde un desarrollador arrastra constantemente su tarjeta ("In Progress" -> "Blocked" -> "In Progress" -> "Done" -> "QA Rejected")
    Then garantizamos una experiencia de usuario sub-segundo sin overhead BPMN
    And el iBPMS procesa estas mutaciones de estado en la Entidad (JPA) a través de una API REST ultra veloz (Ej: `PATCH /api/v1/proyectos/{pid}/kanban/{tid}/state`) y registra todas las transiciones como eventos inmutables en la Tabla de Auditoría general de la plataforma transversal.

  Scenario: [Arquitectura] Event-Driven hacia Modelos Estructurados (Salto Híbrido)
    Given una travesía asíncrona Ágil (La tarea Kanban está en estado "In Progress" o "QA Approval")
    When el negocio requiere para darla por `Done` ejecutar una Macro-Aprobación Estructurada, Secuencial y Gerencial
    Then la mutación del Estado Kanban invoca asíncronamente un "Process Instantiation" aislado del Workflow estructurado (BPMN normal)
    And cuando el flujo clásico de Camunda termine, este orquestador emitirá un evento publicándolo de regreso al componente Ágil marcando la casilla original del Tablero como Finalizada o Aprobada, conectando lo impredecible con lo burocrático de forma pura.

  Scenario: Gobernanza de Estados y Columnas Dinámicas (Opción B)
    Given la necesidad operativa de adaptar el flujo Kanban añadiendo un nuevo estado al ciclo
    When el usuario presiona el botón "Añadir Columna" en la Pantalla 3
    Then el sistema valida que el usuario ostente exclusivamente el Roll de 'Scrum_Master' o 'Lider_Proyecto' en la tabla de miembros
    And el motor Backend efectúa una validación dura (Hard-Limit) rechazando transacciones que excedan un máximo de 7 columnas por tablero para la Versión 1, previniendo sobrecarga visual.

  Scenario: [Arquitectura] Tabla Polimórfica Única para Consolidación de Esfuerzos (BAM)
    Given la necesidad corporativa de cruzar costos de horas-hombre transversales en la Pantalla 5
    When un empleado registre 2 horas en una "Tarea BPMN" y 3 horas en una "Tarjeta Kanban"
    Then el Backend prohibe guardar dichas horas en las tablas específicas de cada módulo
    And fuerza al sistema a canalizar el guardado hacia una única tabla polimórfica (`ibpms_time_logs`) 
    And distinguiéndolas únicamente por la columna `reference_type` (`TASK_BPMN`, `TASK_AGILE`, `TASK_GANTT`), simplificando matemáticamente la reportería financiera.

  Scenario: [Arquitectura] Componente Frontend Agnóstico Universal (`<UniversalSlaTimer>`)
    Given la disparidad visual entre la Bandeja Workdesk (Pantalla 1), el Tablero Ágil (Pantalla 3) y el Gantt Tradicional (Pantalla 10.B)
    When el desarrollador deba mostrar el reloj de SLA o el Timer de "Play/Stop"
    Then el framework del iBPMS le denegará desarrollar HTML/Vue personalizado en cada pantalla
    And lo obligará a instanciar y re-utilizar el micro-componente atómico transversal `<UniversalSlaTimer>`.
    And este componente será "Tonto" (Dumb Component), consumiendo APIs centrales de tiempo sin conocer la naturaleza funcional de la tarea que lo aloja.

  Scenario: [Arquitectura] Inmutabilidad de Costos Incurridos (Anti-Manipulación)
    Given que el empleado ha presionado "Stop" en su temporizador y la plataforma envía el LOG a la base de datos central
    When el usuario o su jefe intenten editar o borrar ese registro de tiempo (Ej: Modificar de 4 horas a 2 horas)
    Then la API de Time Tracking denegará el Método DELETE/PUT (Comportamiento *Append-Only*)
    And el log se convertirá en un asiento financiero inmutable; las correcciones solo podrán hacerse añadiendo asientos contables en negativo mediante un proceso de auditoría superior manual.
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

  Scenario: Aislamiento Estricto de Datos (Multi-Tenancy)
    Given la arquitectura SaaS multi-cliente de la plataforma iBPMS
    When el JWT de Grafana es generado por el Backend para renderizar la Pantalla 5
    Then el token debe inyectar criptográficamente el `Tenant_ID` del usuario activo
    And la Base de Datos o la consulta subyacente de Grafana debe forzar obligatoriamente el filtrado por este Tenant (Ej. Row-Level Security) previniendo fugas de datos operativos hacia clientes vecinos.

  Scenario: Capacidad de Perforación Interactiva (Drill-Down UI)
    Given el Dashboard visual en la Pantalla 5 que muestra una alerta de "15 Tareas Bloqueadas"
    When el gerente hace clic sobre el segmento de la gráfica circular
    Then el sistema debe interceptar el evento de anclaje de Grafana
    And redireccionar la UI del iBPMS automáticamente a la Bandeja de Trabajo (Pantalla 1) o Hub Ágil (Pantalla 10)
    And pre-filtrar la vista exacta con las 15 tarjetas implicadas para tomar acción inmediata.

  Scenario: Segregación de Roles para Monitoreo Activo (RBAC)
    Given un empleado raso con rol "Analista" o "Ejecutor" intentando acceder a URL de reportes macro
    When navegue hacia la Pantalla 5 (BAM)
    Then el Frontend interceptará la ruta y mostrará un mensaje de "Acceso Denegado"
    And el Backend rechazará la generación del Token de Grafana, reservando esta vista exclusivamente para jerarquías directivas (Ej. `Gerente_Operaciones`, `Scrum_Master`).

  Scenario: Frecuencia de Refresco Asíncrona (Protección Transaccional)
    Given el inmenso volumen de eventos emitidos en tiempo real por el motor Camunda
    When Grafana ejecute los queries analíticos pesados para renderizar la Pantalla 5
    Then NO atacará directamente la base de datos transaccional caliente (Master DB)
    And leerá de una Base de Datos Analítica o Réplica (Ej. Elasticsearch o DataWarehouse) alimentada por un CronJob/CDC que se actualiza estrictamente cada 10 minutos para proteger la estabilidad del servicio en vivo.

  Scenario: Autoservicio de BI Analítico (Grafana Editor Nativo)
    Given que los tableros pre-cargados (Vencimientos, Costos, Ciclos) no cubren una métrica atípica solicitada por un cliente
    When el gerente seleccione la opción "BAM Avanzado" en la Pantalla 5
    Then el iBPMS cargará la Interfaz Nivel Editor Nativa de Grafana embebida
    And otorgará permisos formales de "Editor" al usuario, permitiéndole arrastrar bloques, cambiar colores de tortas y personalizar sus propias métricas ad-hoc limitadas a su Tenant_ID.
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

  Scenario: Tolerancia a Fallos por Variables Ausentes (Missing Keys)
    Given una plantilla `.docx` que incluye la etiqueta `<<segundo_apellido>>` obligatoria en su sintaxis
    When el motor documental (FOP) sea invocado y la variable no exista o sea NULA en el payload enviado por Camunda
    Then el motor NO debe abortar la transacción (Evitando HTTP 400 y rotura de flujos de negocio)
    And debe sobrellevar la carencia inyectando automáticamente la frase "N/A" o un espacio en blanco seguro en el documento final.

  Scenario: Expansión Dinámica de Tablas y Vectores (Bucles)
    Given que el JSON de entrada contiene un Array de objetos (Ej: Lista de 5 productos comprados)
    When la plantilla documental contenga sentencias iterativas de tipo `#foreach` en filas de una tabla de Word
    Then el motor SGDEA clonará la fila tantas veces como elementos existan en el array inyectando sus respectivas propiedades, posibilitando documentos hiper-dinámicos de longitud variable en la V1.

  Scenario: Gobernanza de Persistencia (Almacenamiento Perenne vs Vuelo Efímero)
    Given la invocación del servicio REST `/api/v1/documents/generate`
    When el proceso o usuario llamador configure explícitamente el flag `storageMode`
    Then el Back-End acatará rígidamente la directriz:
    And Si es `EPHEMERAL`: El documento se renderiza, se entrega el base64/link de 15min y se destruye físicamente de RAM/Disco.
    And Si es `PERSISTENT`: El PDF se consolida inmutablemente en el Storage (S3/Azure) amarrado al UID del Expediente, garantizando trazabilidad y registro perpetuo exigible por Ley.

  Scenario: Acorazado Forense y Firma Digital del Documento Físico
    Given la configuración de una plantilla de Alto Riesgo Legal
    When el motor finaliza el ensamblado del PDF final
    Then NO se limitará a guardar el Hash SHA-256 en la base de datos (ibpms_audit_log)
    And incrustará en paralelo un "Certificado Criptográfico PKI" estructural dentro del mismo archivo PDF
    And y estampará visualmente en los márgenes de las páginas un Código QR (o Sello de Agua Legal) verificable externamente, asegurando la no-repulsa de autoría.

  Scenario: Versión Retroactiva Activa en Auditorías Históricas
    Given un Cliente instanciado hace 2 años cuando regía el "Contrato Laboral V1"
    When un auditor re-visite en Pantalla 12 dicho caso y el sistema requiera re-descargar o consultar su contrato
    Then el motor SGDEA buscará y ensamblará el PDF contra la plantilla V1 almacenada en el repositorio histórico (Time-Travel Rendering)
    And prohibirá rotundamente la utilización de la plantilla "V4" actual para casos pasados, protegiendo las cláusulas vigentes al momento de la firma original.
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

  Scenario: Triage por Sentimiento y Urgencia (Predicción IA)
    Given la metadata enriquecida del correo proveniente de la US-013 (Ej: `sentiment: URGENCE_HIGH`)
    When el analista de SAC filtra la bandeja usando el dropdown "Urgencia y Sentimiento"
    Then el sistema filtra reestructurando la grilla para mostrar primero los correos que contengan quejas operativas o riesgos legales altos
    And garantizando un enfoque de First-In/First-Out ajustado por criticidad (Weighted FIFO).

  Scenario: Detección de Archivos y Tipificación Estructural
    Given que el correo contiene múltiples archivos adjuntos
    When el analista filtra por el concepto "Contiene: Contratos Firmados"
    Then el filtro de la Pantalla 1B obvia la extensión pura del archivo (.pdf)
    And cruza la búsqueda contra el tag de clasificación documental `doc_type` generado por la IA, retornando solo los correos cuyo contenido semántico coincida.

  Scenario: Monitoreo Activo de Acuerdos de Nivel de Servicio (SLA)
    Given los correos entrantes mapeados contra una política de respuesta máxima de 24 horas (SLA)
    When el analista de SAC aplica el filtro rápido de semáforo "Mostrar: SLA por Vencer (< 2 horas)"
    Then el sistema expone exclusivamente los correos que están a punto de romper el requerimiento legal de tiempo operativo, ocultando correos recientes de ingreso temprano.

  Scenario: Búsqueda Semántica de Texto Completo (Full-Text Search)
    Given un analista buscando la aguja en el pajar con la palabra "Indemnización"
    When digite dicha palabra en la barra de búsqueda global de la Pantalla 1B
    Then el motor de Backend (Elasticsearch o similar) NO buscará solo en el Asunto
    And indexará la búsqueda contra el cuerpo del correo, y el texto interior de los anexos (OCR) entregando el correo exacto donde reside dicho patrón.

  Scenario: Control de Concurrencia SAC y Bloqueo de Correos
    Given un buzón compartido accedido por 5 analistas de SAC simultáneamente
    When el Analista "A" da clic para leer un nuevo "Correo Huérfano"
    Then el sistema inscribe un Soft-Lock en la Base de Datos asociando ese correo al `User_ID` del Analista "A"
    And cuando el Analista "B" filtre la bandeja en la vista "Mis Correos Asignados", no verá el correo del "A", evitando que dos humanos gestionen el mismo ticket y generen respuestas duplicadas.
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
  Scenario: Doble Borrador (Acuse Inmediato y Respuesta de Fondo)
    Given la entrada de un correo electrónico al buzón de SAC
    When el agente IA procesa el contenido exitosamente
    Then debe generar y presentar dos borradores independientes en la Pantalla 2C:
    And 1. "Acuse de Recibo": Respuesta corta confirmando radicación.
    And 2. "Respuesta de Fondo": Borrador técnico para solucionar la petición.
    And cada borrador tiene un ciclo de vida UI independiente, permitiendo enviar el Acuse hoy y gestionar el Fondo mañana.

  Scenario: Prevención de Alucinaciones en Variables Críticas de Negocio
    Given la generación del "Borrador de Fondo" por parte del LLM
    When el motor de IA detecte la necesidad de comprometer fechas, montos económicos o nombres de responsables
    Then tiene estrictamente PROHIBIDO pre-llenar estos datos asumiéndolos del contexto
    And el texto generado inyectará placeholders visuales (Ej: `[INGRESAR_MONTO]`)
    And el Frontend inhabilitará el botón "Aprobar y Enviar" hasta que el analista reemplace manualmente dichos condicionales.

  Scenario: Restricción Bilingüe (Solo EN/ES)
    Given la recepción de un correo en un idioma diferente a Español o Inglés (Ej. Alemán)
    When el sistema detecte el idioma origen
    Then traducirá y mostrará el correo original en Español al analista para su comprensión (Panel Izquierdo)
    And se ABSTENDRÁ de generar un borrador automático de respuesta en Alemán, mostrando una alerta de "Idioma no soportado para auto-redacción", obligando al humano a escribir la respuesta.

  Scenario: Confianza en la Intervención y Tono Humano
    Given que el analista decide modificar sustancialmente el borrador de fondo propuesto por la IA
    When el usuario presione el botón "Guardar Edición y Enviar"
    Then el sistema confía íntegramente en el criterio del humano y ejecuta el envío sin re-validaciones (Override Total)
    And el texto final enviado entra al bucle de aprendizaje MLOps (US-015) para alinear futuras propuestas a ese nuevo tono.

  Scenario: Contexto Acotado del Historial (Sliding Window Context)
    Given un correo que pertenece a un hilo monumental de 60 correos previos
    When el backend ensamble el "Prompt" para solicitar el borrador de respuesta a la IA
    Then inyectará únicamente los 5 correos más recientes de la cadena cronológica
    And truncará el resto para eficientar el consumo de Tokens y evitar degradación de contexto del LLM.

  Scenario: Inyección Dinámica de Disculpas Institucionales (Tone-Matching)
    Given el análisis de metadata del correo (US-013) que arroja un 'sentiment_score' de Rabia Extrema o Amenaza Legal
    When el Agente IA redacte el "Borrador de Fondo"
    Then el System Prompt obligará al modelo a omitir frases comerciales genéricas o "happy-talk"
    And forzará la inserción de una Disculpa Institucional formal y empática al inicio del texto para desescalar el conflicto.

  Scenario: Ceguera Transaccional (Prohibición de Promesas)
    Given la redacción de la respuesta de fondo por parte del LLM en la V1
    When el LLM analice el requerimiento del cliente (Ej: "¿Mi póliza cubre este choque?")
    Then el motor tiene explícitamente PROHIBIDO afirmar, negar o garantizar estados transaccionales o coberturas que vivan en BD externas (Ej: "Sí lo cubrimos")
    And el borrador se limitará perentoriamente a indicar que "El caso se encuentra en revisión" y a solicitar información, formatos o documentos adicionales si hacen falta.
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

  Scenario: Extracción Masiva de Metadata Operativa (Preparación para Docketing)
    Given un correo electrónico entrante en crudo
    When el Motor LLM evalúe su contenido para hallar al Cliente CRM
    Then PARALELAMENTE DEBE emitir un objeto JSON estandarizado conteniendo metadatos críticos de negocio:
    And 1. `sentiment_score`: Evaluación de frustración o amenaza legal.
    And 2. `predicted_service`: El Proyecto SD al que pre-asume pertenece la solicitud.
    And 3. `attachments_classification`: Un arreglo donde cataloga (Ej. "Es un contrato", "Es un comprobante") el tipo de archivo recibido sin requerir apertura humana.
    And esta metadata debe persistirse en 'ibpms_metadata_index' para viabilizar los filtros de la US-011.

  Scenario: Fallback a Metadata Interna (Sin CRM)
    Given la configuración global administrada donde el flag `ENABLE_CRM_INTEGRATION` está apagado o la API del CRM no responde
    When el sistema intente asociar el correo a un Cliente o Proyecto
    Then el motor de Backend buscará coincidencias de cruce en el `ibpms_service_delivery_catalog` interno
    And relacionará el correo con proyectos o instancias previas de Camunda que compartan el mismo dominio, garantizando continuidad operativa en la US-011 sin depender de bases de datos externas.

  Scenario: Enrutado Semántico para Dominios Multi-Proyecto
    Given un dominio corporativo (Ej. `@amazon.com`) que posee múltiples proyectos/servicios activos en el iBPMS simultáneamente
    When el Agente IA lea el correo entrante
    Then el sistema NO etiquetará estúpidamente el correo con todos los proyectos a la vez
    And el LLM estará obligado a cruzar el texto del cuerpo del mensaje contra las descripciones de los proyectos activos, seleccionando matemáticamente el `predicted_service` más coherente para el analista.

  Scenario: Lista Negra de Dominios Públicos (Blacklist)
    Given la recepción de un correo proveniente de un proveedor público masivo (Ej. `@gmail.com`, `@outlook.com`, `@yahoo.es`)
    When el sistema intente ejecutar el motor de "Match por Dominio"
    Then el backend interceptará la ejecución cotejando el dominio contra la tabla `ibpms_public_domains_blacklist`
    And anulará la vinculación por dominio para evitar colisiones masivas de privacidad cruzada entre clientes distintos
    And forzará al Motor IA a buscar identificadores únicos (Cédulas, RUT, Teléfonos, Nombres Completos, Números de Factura) EXCLUSIVAMENTE dentro del cuerpo del mensaje o firmas para establecer el Match.
```
**Trazabilidad UX:** Wireframes Pantalla 1B (Bandeja Docketing).

---

### US-014: Sugerencia de acciones (tareas) operativas
**Como** gestor de un buzón corporativo
**Quiero** que el asistente sugiera acciones operativas (crear tarea, iniciar proceso) asociadas al correo
**Para** acelerar el flujo de trabajo funcional sin perder el control manual.

> 🧠 **Sinergia Arquitectónica (Ecosistema Inteligente):**
> Esta historia es el "Sistema Nervioso Central" operativa del buzón y orquesta estrechamente con el siguiente ecosistema:
>
> 📌 **Ecosistema Intake (BPMN / SD):**
> - **Embudo de Cuarentena (US-040):** Si la intención detectada implica arrancar un "Nuevo Proceso SD", la US-014 somete obligatoriamente esta Acción sugerida a la Bandeja de Aprobación de Intakes (Pantalla 16) para que un Líder la despache.
> - **Confirmación Formal 'Plan A' (US-022):** Si la Acción del Embudo (US-040) se aprueba, la US-022 asume el control enviando un correo de confirmación formal al cliente y consolidando el nacimiento del Flujo en Camunda.
> - **Escape Manual 'Plan B' (US-024):** Si la IA detrás de la US-014 falla absolutamente y no sugiere ninguna Action Card útil, el subsistema recae grácilmente sobre la US-024, permitiendo al Administrador crear el Proceso a mano.
>
> 📌 **Ecosistema de Pre-Procesamiento y Retorno:**
> - **Entrada de Datos (US-013):** Consume la Metadata pre-calculada (`predicted_service`, `sentiment`). La US-014 es "ciega" sin la US-013.
> - **Presentación UI (US-011):** Dibuja las "Action Cards" nativamente dentro de la Bandeja Docketing pública de SAC y etiqueta los correos procesados.
> - **Enrutamiento Atómico (US-030/US-008):** Si la intención detectada es de baja complejidad (Petición simple), inicializa una tarjeta Kanban directamente en el proyecto, saltándose Camunda.
> - **Workdesk (US-001) y RBAC (US-036):** Despacha la tarea validada al escritorio personal del analista, respetando la estricta matriz de roles y permisos del proyecto.
> - **Retroalimentación MLOps (US-015):** Si el operador de la US-011 edita o rechaza manualmente a la inteligencia en sus sugerencias, ese evento viaja a la base de datos de entrenamiento continuo del modelo.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Action Cards Operativas y Orquestación Funcional (Human-in-the-Loop)
  Scenario: Bifurcación Sensible al Contexto (Nueva Tarea vs Nuevo Proceso)
    Given la generación de sugerencias operativas (Action Cards) basadas en metadatos
    When la Inteligencia Artificial evalúe la intención transaccional del correo
    Then si este pertenece a un proyecto existente, sugerirá crear una "Nueva Tarea" ligada a ese Proceso/Sprint Kanban
    And si es una petición huérfana (nuevo requerimiento), sugerirá instanciar un "Nuevo Proceso SD", enviando esta tarjeta de creación forzosamente hacia el Embudo Administrativo (US-040).

  Scenario: Edición Activa de la Tarjeta (Human-in-the-Loop Feedback)
    Given la presentación visual de una Action Card latente en la Pantalla 1B
    When el Analista (humano) decida repriorizar o reasignar los datos sugeridos por la máquina antes de aprobar (Ej: Cambiar Urgencia "Media" a "Alta")
    Then el Frontend habilitará la edición libre e in-situ del payload sugerido
    And el sistema creará la entidad resultante con los datos humanos (sobreescribiendo los algorítmicos)
    And transmitirá la rectificación al motor de telemetría MLOps subyacente (US-015) para curar los pesos de inferencia futuros.

  Scenario: Trazabilidad Permanente del Correo Original (Inbox Behavior)
    Given la aprobación formal de la Action Card (creando la tarea o servicio SD definitivo en el back)
    When se consolide la mutación externa hacia Camunda o Kanban
    Then el servidor NO borrará ni desaparecerá forzosamente el correo original de la Bandeja Docketing pública de SAC (Pantalla 1B)
    And la UI le estampará un badge/etiqueta visual persistente referenciando el `[Status: Actividad Creada]` y el ID destino
    And un clon/copia del correo original formará obligatoriamente la primera pieza probatoria (Attachment 1) de la hoja de ruta del nuevo caso en el iBPMS.
```
**Trazabilidad UX:** Wireframes Pantalla 1B (Bandeja Docketing) y Pantalla 16 (Intake Administrativo).

---

### US-015: Feedback y Aprendizaje Supervisado (Nightly MLOps Batch)
**Como** Líder de Operaciones / Arquitecto IA
**Quiero** que el sistema aprenda de las correcciones humanas sin colapsar el performance transaccional diario
**Para** garantizar una evolución cognitiva continua (Zero-Touch) aislando el entrenamiento en ventanas nocturnas controladas.

> 🧠 **Arquitectura de Aprendizaje en 2 Fases (V1):**
> Para proteger la estabilidad de la Base de Datos Transaccional (Camunda/Kanban) durante el día, el MLOps se bifurca en:
> - **Día (Observador Pasivo):** Registra silenciosamente el "Delta" entre lo que sugirió la IA y la decisión final del humano en la UI.
> - **Noche (Agente Data Scientist):** Un Cron-Job autónomo procesa masivamente los logs del día y afina los vectores/pesos sin intervención de ingenieros humanos.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: MLOps Feedback Loop Architecture (2-Phase)
  Scenario: Fase 1 - Recolección Diurna (Observación No Bloqueante NFR)
    Given un componente IA emitiendo una sugerencia o borrador en la Pantalla 1B
    When un Analista humano edita, rechaza o reclasifica dicha sugerencia antes de enviarla
    Then el Frontend disparará la discrepancia (Lo que dijo la IA vs Lo que escribió el Humano) hacia el endpoint dedicado `POST /api/v1/mlops/feedback` bajo el patrón "Fire-and-Forget"
    And queda estrictamente PROHIBIDO interceptar o añadir latencia a la transacción core de Camunda/Entity al guardar casos en la BD principal
    And el servicio Backend (Spring Boot) empujará este log a un tópico en RabbitMQ o lo procesará de forma 100% asíncrona para insertarlo en `ibpms_mlops_feedback_log` sin competir por los hilos de conexión de PostgreSQL.

  Scenario: Fase 2 - Despertar del Agente Data Scientist (Batch Nocturno)
    Given la ventana de mantenimiento parametrizada (Ej: 02:00 AM, Semanal, Mensual) en la Épica 15
    When el motor de Cron-Jobs dispara el Agente Data Scientist (Proceso Autónomo)
    Then el Agente consultará masivamente la tabla `ibpms_mlops_feedback_log` filtrando los logs acumulados
    And ejecutará la consolidación de patrones repetitivos exigiendo un "Consenso Mínimo" (Ej: Aprende el patrón SI Y SOLO SI existen al menos 2 analistas distintos corrigiendo lo mismo).
    And si encuentra discrepancias lógicas (Analista A lo categorizó 'Ventas', Analista B lo categorizó 'Soporte'), el Agente ignorará por completo el patrón por considerarlo "Contradictorio".
    And ignorará matemáticamente los "Patrones Negativos" (Cuando el analista simplemente oprime `[Descartar/Eliminar Propuesta]`), asumiendo que el rechazo se debe a ruido/spam y no a un error cognitivo de clasificación, ahorrando poder de cómputo.
    And le dará un peso aritmético multiplicador a la corrección dependiendo del Rol (Ej: Corrección de un Líder pesa x5 frente a la de un Junior).
    And actualizará los pesos de la Base de Conocimiento (RAG) para clasificaciones Y asimilará las correcciones de redacción humana de la Pantalla 1B para imitar el Estilo Institucional (NLG).

  Scenario: Trazabilidad y Purga del Turno Nocturno
    Given la finalización exitosa del fine-tuning nocturno
    Then el Agente Data Scientist emitirá un reporte consolidado al log de auditoría del sistema: "Matriz actualizada basada en N correcciones"
    And marcará los registros procesados en la tabla `ibpms_mlops_feedback_log` con el flag `status: trained`.

  Scenario: Declaración de Incompetencia Diurna (Límite Paramétrico)
    Given un correo electrónico confuso procesado durante el día
    When la red neuronal calcula un Confidence Score por debajo del umbral parametrizado en la Épica 15 (Configuración Global)
    Then la IA se declara incompetente explícitamente y deja la tarea en blanco
    And enruta el caso obligatoriamente al "Fallback Humano" sin intentar adivinar, generando el primer log de falla para que el Data Scientist Nocturno lo califique como un "Patrón Desconocido".

  Scenario: Resiliencia Nocturna en PostgreSQL y Dead-Lettering (NFR)
    Given el Cron-Job de aprendizaje neuronal (Agente Data Scientist) leyendo forzosamente los logs desde RabbitMQ
    When recalcule vectores RAG y la base PostgreSQL arroje latencia o timeout a las 03:00 AM
    Then el worker abortará emitiendo un NACK a RabbitMQ
    And al acumular 3 NACKs sucesivos, un Dead Letter Exchange (DLX) enviará la carga fallida a la cola residual `mlops-dlq`
    And notificará al SysAdmin por correo/webhook salvando la metadata cruda de la pérdida para intervención manual.
```
**Trazabilidad UX:** Wireframes Pantalla 1B (Bandeja Docketing).

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

  Scenario: Toggle de Activación Cognitiva por Buzón
    Given la configuración local de un buzón en la Pantalla 15.B
    Then el Administrador posee un Master Switch `[Habilitar IA Copilot]`
    And si el switch está apagado, el correo ingresa a la bandeja como un Intake 100% manual, sin análisis de sentimiento, sin extracción CRM y sin sugerencias, ahorrando tokens en buzones de bajo valor.

  Scenario: Restricción del Catálogo de Servicios (White-Listing de Acciones)
    Given que la IA generativa puede alucinar o sugerir procesos fuera de la jurisdicción del área
    When el Administrador configura un buzón específico (Ej: `soporte_tecnico@`)
    Then la Pantalla 15.B despliega una lista de checkboxes con todos los servicios SD disponibles en el Catálogo de la empresa.
    And el Administrador puede seleccionar explícitamente cuáles son los ÚNICOS procesos que la IA tiene permitido sugerir en este buzón. Las inferencias hacia procesos no seleccionados serán bloqueadas y el borrador de acción quedará vacío.

  Scenario: Umbral de Confianza Cognitiva Independiente (Confidence Score)
    Given la pestaña "Variables de Entorno" dentro de la configuración específica de un buzón
    When el líder de negocio edita las propiedades de ese buzón
    Then puede asignar matemáticamente (0-100) el `Minimum Confidence Score` local.
    And cualquier inferencia heurística de LA IA en ESE buzón que no supere el puntaje, se considerará "Anómala" e invocará el `[Fallback Humano]` (US-015).
    And esto permite tener buzones críticos (Ej: Legales) exigiendo 95% de confianza, y buzones laxos (Ej: Info General) exigiendo 70%, sin pisarse entre ellos.

  Scenario: Enrutamiento Táctico y SLA por Defecto
    Given la entrada de un nuevo correo a un buzón específico (Ej: `reclamos@`)
    When la IA procesa el mensaje a las 3:00 AM y genera las propuestas (A la espera de validación humana diurna)
    Then la política del buzón forzará a la IA a asignar un SLA de Gracia (Ej: 2 horas) y una Criticidad (Ej: Alta) pre-parametrizada para ese buzón.
    And el sistema enrutará la visualización de este correo EXCLUSIVAMENTE a los usuarios que posean el Rol/Dueño asociado a ese buzón (RBAC), impidiendo que el Intake sea público para toda la empresa.

  Scenario: Control de Tono y Firmas Corporativas (NLG)
    Given que la IA generó un borrador de respuesta (US-012)
    Then la política del buzón obligará a la IA a reescribir la respuesta bajo el "Tono" parametrizado (Ej: "Corporativo y Gélido" para quejas, "Persuasivo" para ventas).
    And inyectará automáticamente en el borrador la Plantilla de Firma asociada a ese buzón (Nombre del Canal, Disclaimer de Privacidad, Links).

  Scenario: Parseo Multilingüe Estricto (Inglés/Español)
    Given la política de Idioma del buzón ajustada a "Match Automático (V1)"
    When ingresa un correo en Inglés
    Then la IA procesará, analizará y sugerirá el borrador de respuesta OBLIGATORIAMENTE en Inglés. Si entra en Español, el ciclo completo será en Español.

  Scenario: Alerta UI de Desconexión de Buzón (Token Expirado)
    Given que el conector IMAP/GraphAPI de Office 365 pierde permisos sobre un buzón (Sesión expirada o revocada)
    Then el iBPMS dejará de leer el buzón en silencio
    And levantará de inmediato una "Alerta Crítica Visual" en la Pantalla 15.B (Local) marcando el buzón en Rojo.
**Trazabilidad UX:** Wireframes Pantalla 15.B (Configuración Local de Buzones SAC).

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
**Trazabilidad UX:** Wireframes Pantalla 5 (Dashboards / BAM).

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
  Scenario: El Master Switch y el Modo Standalone (Bypass del CRM)
    Given la configuración global en la Pantalla 15.A (Épica 15)
    When el Administrador apaga el `Master Switch [Integración Continua CRM]`
    Then el iBPMS entra en "Modo Standalone" u "Orquestador Maestro".
    And pausa (oculta) inmediatamente todos los servicios antiguos importados del CRM.
    And a partir de ese momento, la lista desplegable de servicios en los Intakes y las reglas de la IA (US-016) se alimentarán EXCLUSIVAMENTE de los Flujos/Procesos internos modelados nativamente en la Pantalla 8 (Low-Code). No existe modo híbrido en V1.

  Scenario: Ingesta Plana del Catálogo (Consulta Base)
    Given el Master Switch del CRM en `ON`
    When el subsistema iBPMS sincroniza el catálogo por API (Vía OAuth2)
    Then el CRM devuelve los servicios importando estrictamente la Metadata Comercial Esencial (Nombre y Descripción) atada a un `service_ref_id`.
    And el iBPMS ignorará e impedirá la importación de 'Grupos o Equipos' de asignación del CRM, delegando el enrutamiento 100% al motor RBAC interno del iBPMS.

  Scenario: El CRM como 'Source of Truth' Indiscutible (Nomenclatura)
    Given la sincronización periódica del catálogo Activa
    When el CRM envía el nombre de un servicio (Ej. "Reemplazo de Tarjeta de Crédito")
    And un usuario intentó renombrarlo manualmente en el iBPMS a "Sustitución TDC"
    Then el iBPMS "aplastará" y sobrescribirá el nombre manual, restaurándolo obligatoriamente a la nomenclatura dictada por el CRM.

  Scenario: Ocultamiento Silencioso de Servicios Eliminados
    Given que un Gerente Comercial elimina o inactiva el Servicio "Venta de Seguros" directamente en las entrañas de Salesforce/CRM
    When el iBPMS ejecuta su siguiente ciclo de sincronización nocturno y detecta la ausencia del `service_ref_id`
    Then el sistema ocultará automáticamente ese Item de la lista desplegable para los operarios humanos (Pantalla 0 y 1B).
    And la base de datos actualizará su estado informándole a la Inteligencia Artificial (US-016) que dicho servicio ya no es sugerible, evitando alucinaciones de Catálogo.

  Scenario: Inmunidad Histórica (In-Flight Cases)
    Given la desactivación o borrado de un Servicio en el CRM (Ej: "Venta de Seguros")
    And que existen 50 Casos de dicho servicio operando "En Progreso" dentro de las bandejas del iBPMS
    Then la eliminación comercial NO afectará transaccionalmente a estos casos vivos.
    And continuarán su ciclo de vida y facturación normal hasta cerrarse utilizando la metadata inmutable que poseían al momento de su creación.

  Scenario: Activación de Modo Sobrevivencia por Caída de CRM y Feedback Visual
    Given el backend del CRM externo se encuentra inalcanzable (Timeout HTTP 5xx) o la red falla
    And existe sincronización previa almacenada en la memoria Caché (Redis/Motor Interno)
    When los operarios o clientes abren formularios para iniciar nuevos casos
    Then el iBPMS permitirá la creación asíncrona utilizando el catálogo cacheado sin bloquear la operación.
    And desplegará un Banner Naranja de Advertencia en la UI indicando: *"Precaución: El CRM está inalcanzable. Se está operando con el Catálogo en Modo Caché. Posible desactualización"*.
```
**Trazabilidad UX:** Wireframes Pantalla 0 (Service Catalog).

---

### US-020: Estrategias de Sincronización Flexible
**Como** Administrador del Sistema
**Quiero** habilitar múltiples estrategias de refresco del catálogo CRM (Schedulers)
**Para** balancear la carga de red sin perder la precisión de la oferta comercial.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: CRON y Sincronización Nocturna del Catálogo
  Scenario: Sincronización calendarizada rígida a las 11:00 PM
    Given la necesidad imperativa de no impactar la red durante el horario hábil
    When se alcanza el 'cron trigger' en el backend Spring Boot a las 23:00 Horas (11:00 PM)
    Then el motor iBPMS dispara una tarea asíncrona que hace un full-fetch del catálogo del CRM.
    And refresca la tabla interna o el 'Redis Cache' con las altas, bajas y modificaciones comerciales.
    And registra el resultado del lote (OK/FAIL) en la tabla `ibpms_audit_log` para visibilidad del SysAdmin en la mañana.

  Scenario: Tolerancia a Fallas en Mitad de Lote (Retry Queue / RabbitMQ)
    Given un error de red o timeout durante la sincronización nocturna de miles de servicios
    When el proceso falla a la mitad del lote de ingesta
    Then el Backend no realizará un commit parcial (BD), sino que utilizará el módulo de **RabbitMQ** para apilar la tarea fallida.
    And ejecutará una política automática de "Reintentos de Resiliencia" (Ej: 3 intentos cada 15 min).
    And SI Y SOLO SI al finalizar los reintentos no hay éxito, descartará el lote defectuoso, preservando intacto el Catálogo de ayer, e informará el Error Crítico al administrador.

  Scenario: Sincronización Manual de Emergencia (Botón de Pánico) y Cool-down
    Given el panel de Configuración de Integraciones del Administrador (Pantalla 15.A)
    When presiona el botón `[Sincronizar CRM Ahora]` por fuera del horario nocturno
    Then el sistema forzará la descarga inmediata del catálogo.
    And deshabilitará (grisará) el botón aplicando una regla de "Cool-Down" (Enfriamiento) obligatoria de 15 minutos para prevenir saturación (Anti-DDoS) hacia el servidor del propio cliente CRM.

  Scenario: Actualización UI Sin Recarga (WebSockets)
    Given la ejecución exitosa de una Sincronización de Emergencia en pleno horario laboral diurno
    And cientos de agentes de Call Center operando dentro del iBPMS
    Then el servidor (Node.js/Spring Boot) despachará un evento Push/WebSocket hacia los clientes conectados.
    And los menús desplegables de "Catálogo de Servicios" se refrescarán y re-renderizarán automáticamente en la UI de todos los usuarios sin exigirles presionar F5.
**Trazabilidad UX:** Tarea Backend (Sin Vista UI requerida).

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
**Como** Líder de SAC (Servicio al Cliente)
**Quiero** enviar un correo de confirmación de servicio a un Cliente desde un buzón corporativo en el iBPMS
**Para** notificarlo, dejar evidencia auditable, y generar una tarea encolada ("Solicitud de Creación SD") sin instanciar ciegamente un proceso basura.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Intake Controlado Plan A (Email Trigger)
  Scenario: Creación de Tarea Administrativa en vez de Service Delivery (CA-1)
    Given el Líder de SAC envía un correo de confirmación a un cliente desde un buzón (Ej: auditorias@ibpms.com) indicando un servicio (Plantilla TO-BE)
    When el correo saliente se envía satisfactoriamente
    Then el sistema registra el correo como evento auditable
    And genera un 'correlation id' asociando al Cliente (CRM ID) y al 'template_id'
    And el sistema no inicia una instancia BPMN en Camunda
    And el sistema crea una Tarea de Usuario ("Crear Service Delivery") asignada al Líder de SAC o Admin
```
**Trazabilidad UX:** Wireframes Pantalla 2 (Interacciones de Correo) transicionando a Pantalla 16 (Intake).

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
**Trazabilidad UX:** Orquestación Backend.

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
    
  Scenario: Renderizado Dinámico del Start Event (Formulario Obligatorio)
    Given un Administrador ocluye el botón de [ Crear Servicio/Case ] (Pantalla 0 / 9)
    When elije un proceso específico del Catálogo (Ej: "Solicitud Comercial")
    Then el Frontend no despliega un formulario genérico
    And realiza una asignación al motor (Camunda/Backend) para obtener el esquema del "Start Event Form" asociado específicamente a ese BPMN
    And obliga al Administrador a diligenciar estricta y únicamente los metadatos y variables marcadas como obligatorias en el diseño original de ese proceso antes de instanciarlo.
    And registra una auditoría de inicialización manual obligatoria con label 'trigger_type=MANUAL' en BD.

  Scenario: Gobernanza de la Nomenclatura del Service Delivery (ID Único)
    Given la parametrización de un proceso en el Catálogo de Servicios (Pantalla 8/10)
    When el Administrador diseña los metadatos obligatorios para la creación
    Then el sistema le permite definir obligatoriamente la regla del Identificador Único del caso (Case ID)
    And puede elegir entre dos modalidades:
      1. Codificación Paramétrica: Un prefijo fijo más variables del formulario (Ej: REQ-{Año}-{CRM_ID}).
      2. Autogeneración (Consecutivo): Un Hash o Número Serial secuencial dictado por la base de datos (Ej: SD-000142).
    And una vez instanciado el caso (Plan B), ese ID inmutable se convierte en la llave visible para buscarlo en el Workdesk.

  Scenario: Enrutamiento Inicial a Cola de Grupo (CA-3)
    Given una instancia creada manualmente en el Plan B
    When Camunda despacha la primera Tarea de Usuario (User Task) según el BPMN
    Then la ruta de asignación natural de esa tarea debe recaer en la "Cola de Grupo" de los especialistas
    And NO se asigna automáticamente (assignee) al Administrador que la creó, dejando que el flujo operativo normal actúe.

  Scenario: Visibilidad Restringida del Catálogo (CA-4)
    Given un Administrador seleccionando "Crear Caso Forzado"
    When el Frontend consulta la lista de procesos disponibles
    Then el catálogo oculta dinámicamente aquellos procesos sobre los cuales el Administrador no tiene el Rol de Creación asignado explícitamente en la definición del BPMN (Sincronizado con US-005).

  Scenario: Persistencia Volátil (Limpieza de Caché) (CA-5)
    Given un Administrador diligenciando el Formulario de Inicio (Plan B)
    When interrumpe el proceso cerrando la pestaña o cancelando la acción
    Then el sistema purga el caché del formulario instantáneamente sin guardar ningún "Borrador de Arranque"
    And asumiendo que un Bypass abortado no debe dejar rastro residual en el sistema.

  Scenario: Instanciación Agnóstica Multicliente (Anti-Clones) (CA-6)
    Given un Administrador instanciando un caso y digitando un CRM_ID (Ej: Cliente A)
    When el sistema detecta que ya existe un proceso idéntico (Ej: "Petición Comercial") vivo para ese mismo Cliente A
    Then el sistema NO bloquea ni genera alertas visuales para impedir la creación
    And permite la instanciación de N procesos paralelos para el mismo cliente de forma agnóstica.

  Scenario: Gestión del Ciclo de Vida del Caso Operativo (CRUD) (CA-7)
    Given el nacimiento de un caso manual en el motor BPMN
    Then el sistema debe proveer una interfaz de administración global (CRUD Real) sobre la instancia "In-Flight"
    And permitiendo a usuarios estrictamente autorizados Consultar sus variables.
    And si edita (Update) variables en caliente, el sistema restringe los cambios únicamente a variables "informativas/descriptivas" para evitar corromper las compuertas lógicas (Gateways) del BPMN.
    And si decide eliminar/abortar (Delete) el caso, la acción ejecuta una 'Anulación Lógica' (Soft Delete) marcando el caso como cancelado y exigiendo obligatoriamente un motivo de anulación que quedará trazado en la bitácora de auditoría.

  Scenario: Pre-poblado Opcional CRM (Integración ONS) (CA-8)
    Given el Administrador digita el CRM_ID en el formulario Start Event
    Then el sistema invoca inmediatamente al proveedor externo (CRM) si la conectividad general (Épica 15) está encendida
    And auto-pobla los campos secundarios del formulario (Teléfono, Correo, Nombre) para agilizar el llenado, siendo esta funcionalidad de gracia opcional y sujeta a disponibilidad de la red.

  Scenario: Soporte Documental de Confianza Directa (CA-9)
    Given la disponibilidad de subir anexos PDF en el Start Form
    When el Administrador sube un soporte documental físico
    Then el sistema asume confianza plena en el archivo sin someterlo a pre-escaneos antivirus extremos ni MLOps de sanitización, dado el perímetro seguro del usuario.
    And la carga de estos soportes es completamente opcional, a menos que el diseño del BPMN lo imponga por contrato.

  Scenario: Segmentación Analítica de Origen (CA-10)
    Given un ecosistema con procesos instanciados manualmente ("Plan B") y automáticos ("Plan A")
    When el módulo de BAM (Dashboards US-009/US-018) consolida la data global
    Then el sistema debe emitir reportes cruzados utilizando el campo `trigger_type` (MANUAL vs AI)
    And obligando a que las visualizaciones segmenten volumétricamente cuántos casos nacieron del "Bypass" contra la cuota nativa procesada inteligentemente.

  Scenario: Heredabilidad Directa del SLA Global (CA-11)
    Given la instanciación de un caso bajo el paradigma Plan B (Bypass)
    When el proceso comienza a contabilizar sus tiempos de atención (Tick-Tock)
    Then el proceso hereda exactamente las mismas políticas formales de Nivel de Servicio (SLA) definidas globalmente para su tipo de trámite
    And NO dispondrá de ningún mecanismo en la UI de creación (Start Form) que le permita al Administrador sobrescribir o agilizar artificialmente la métrica de ese caso particular.

  Scenario: Silencio Transaccional ante Anulación (CA-12)
    Given una instancia manual en progreso (Plan B)
    When un Administrador autorizado ejerce la acción restrictiva de 'Soft Delete' (CA-7)
    Then el caso se cancela y anula internamente dentro de Camunda
    And el sistema mantiene un estricto Silencio Transaccional hacia el exterior, NO despachando ningún correo electrónico de notificación, advertencia o disculpa hacia el cliente final o solicitante.
```

**Trazabilidad UX:** Wireframes Pantalla 16 (Intake Administrativo).

---

### US-040: Embudo Inteligente de Intake (Pre-Triaje y Descarte IA)
**Como** Administrador / Líder de Service Delivery
**Quiero** visualizar las Action Cards generadas por IA del Plan A en un formato de embudo de cuarentena
**Para** decidir si las instancio forzosamente rellenando huecos, si apruebo la intención de la IA (Convirtiéndolos en Service Delivery BPMN) o si los descarto.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Intelligent Intake Funnel Management
  Scenario: Virtual Scroll / Paginación en Embudo
    Given el Administrador abre la Pantalla 16
    When la base de datos contiene más de 25 Intakes en Cuarentena
    Then el API del Backend debe soportar paginación dura (`?limit=25&offset=0`) con opciones de página de 25, 50 o 100 elementos
    
  Scenario: SLA de Embudo "Cuarentena"
    Given un Intake capturado vía correo ha estado en cuarentena por un tiempo mayor al configurado en las políticas globales (SLA Default)
    Then el Backend debe marcar el registro con una bandera de 'SLA_BREACHED'
    And el Frontend debe renderizar esa fila o Card resaltada en color rojo en la Pantalla 16
    
  Scenario: Feedback MLOps Post-Descarte
    Given el Administrador da clic en el botón [ 🗑️ Descartar ] sobre un Intake
    Then el Backend no ejecuta un borrado físico inmediato (Hard Delete)
    And emite un evento (Notificación webhook) al subsistema de Inteligencia Artificial para el reentrenamiento
    And finalmente marca el registro con un 'Soft Delete' y lo oculta del Frontend
    
  Scenario: Forzar Mapeo Manual con CRM Opcional
    Given el Administrador da clic en el botón [ ✏️ Forzar Mapeo Manual ] por fallo de la IA
    When se despliega el Modal de catálogos agrupados para forzar la creación manual
    Then el campo `CRM_ID` debe ser opcional (nullable en base de datos)
    And si las variables extraídas por el correo están incompletas (Ej. Falta "Monto"), el proceso debe instanciarse de todas formas en Camunda omitiendo esa restricción. La variable será exigida posteriormente a nivel de Tarea Humana (Workdesk).
    
  Scenario: Prevención de Concurrencia Optimista
    Given dos Administradores ('Admin_A' y 'Admin_B') visualizan el mismo Intake en Cuarentena en la Pantalla 16 simultáneamente
    When 'Admin_A' aprueba el Intake y 2 segundos después 'Admin_B' intenta aprobar el mismo Intake
    Then el Backend debe rechazar la segunda petición mediante validación de control de concurrencia optimista (EJ: `@Version` en el Entity) o validación de estado.
    
  Scenario: Ventana de Gracia / Botón "Deshacer"
    Given el Administrador presiona "Aprobar Creación" en el Embudo
    When el Frontend envía el payload de creación al Backend
    Then el Frontend debe renderizar un 'Toast' interactivo con botón `[Deshacer]` visible y activo por N segundos paramétricos (Ej: 10s)
    And el Backend postergará el gatillado asíncrono hacia Camunda hasta que expire dicha ventana de gracia, permitiendo abortar limpiamente.
    
  Scenario: Restricción de Anexos por RBAC (Link a Inbox)
    Given un Administrador revisa un Intake en la Pantalla 16
    When hace clic para ver detalles del correo original ("Link to Inbox")
    Then el Frontend verifica si los roles del usuario logueado coinciden con los roles permitidos en el Inbox de SAC
    And si coinciden, lo enruta a la Pantalla 1B para ver el correo íntegro con Anexos descargables
    And si no coinciden, muestra únicamente un 'Summary/Plain Text' en un panel o modal, sin los adjuntos originales.

  Scenario: SLA Diferenciado para Creación (Semilla)
    Given múltiples correos entrantes procesados por la IA
    When el motor genera "Action Cards" en estado "Pendiente_De_Validacion_Plan_A"
    Then el sistema muestra únicamente tarjetas clasificadas como 'Nueva Instancia (Semilla)'
    And permite parametrizar acuerdos de nivel de servicio (SLA) para la atención de estas tarjetas en el embudo
    And nota: Las tarjetas de tipo "Aporte a Caso Vivo (Inyección)" quedan aplazadas para el MVP V2, el sistema V1 no generará sugerencias sobre instancias en vuelo.

  Scenario: Agrupación y Filtro por Plantilla Sugerida
    Given un embudo saturado con centenares de Action Cards
    Then la interfaz expone filtros multifunción que permiten al Administrador aislar u ordenar las tarjetas
    And permite filtrar específicamente por la "Plantilla Sugerida" por la IA para procesar en lote todas las tarjetas de un mismo tipo de trámite.

  Scenario: Alerta de Vencimiento de SLA y Notificación a Jefatura
    Given una Action Card estacionada en el embudo cuyo temporizador SLA expira
    Then la tarjeta cambia visualmente a color ROJO en la grilla de la Pantalla 16
    And el sistema dispara un evento unificado enviando un correo electrónico y una alerta in-app al "Jefe / Supervisor" parametrizado orgánicamente para ese Administrador o Buzón.

  Scenario: Completitud Forzosa, Guardado de Borradores y Continuidad del SLA
    Given que el administrador presiona [Aprobar] en una tarjeta incompleta
    Then el sistema despliega un Modal bloqueante exigiendo diligenciar los campos
    And permite utilizar el botón [Guardar Borrador] para persistir el avance parcial sin perder datos
    And el reloj del SLA de Cuarentena NO se congela durante este estado de borrador, continuando su conteo natural para mantener la métrica de eficiencia intacta.

  Scenario: Rutas de Asignación (Directa vs Pool)
    Given el Administrador que completa una Action Card correctamente
    When procede a confirmarla para crear el Service Delivery
    Then el formulario le ofrece dos modos de asignación del caso naciente:
    And 1. Asignación Directa: Escoger en un combo a un operario específico (Ej: Pedro Hernández).
    And 2. Envío a Pool: Dejar la asignación abierta para que caiga a la bandeja general y sea tomada por demanda.

  Scenario: Delegación de Cuarentena (Reasignación entre Administradores)
    Given una Action Card en el embudo asignada por defecto a un Administrador Central
    When este administrador determina que no posee el conocimiento para validarla
    Then puede usar un botón [Reasignar Tarjeta] para transferir la propiedad exclusiva de esa tarjeta en cuarentena hacia el embudo de otro Administrador perito.

  Scenario: Papelera de Reciclaje Transitoria (Soft Delete temporal)
    Given el Administrador presiona [Descartar] sobre una Action Card
    Then la tarjeta desaparece de la vista principal del Embudo
    And es movida a una vista de "Papelera de Reciclaje" en la Pantalla 16
    And el sistema ejecuta un Job programado que elimina física y definitivamente (Hard Delete SQL) esta tarjeta tras 2 días hábiles (Time-To-Live).

  Scenario: Recuperación Total desde la Papelera (Restaurar como Nueva)
    Given un Administrador buscando una Action Card descartada por error en la Papelera de Reciclaje
    When oprime el botón [Restaurar]
    Then la tarjeta abandona la papelera y reingresa a la cabecera (inicio) de la vista principal del Embudo
    And es tratada operativamente como una tarjeta nueva reiniciando o adaptando su contexto de SLA para permitir su procesamiento.

  Scenario: Edición de Datos en Modo Papelera (Admin Override)
    Given una Action Card descartada habitando temporalmente en la Papelera
    When un Administrador con los privilegios adecuados accede a inspeccionarla
    Then este posee el botón de [Editar] activo, permitiéndole alterar y corregir las variables o metadatos extraídos originales antes o durante el acto de restaurarla hacia el embudo principal.

  Scenario: Triunfo del Humano sobre la Máquina (Concurrencia vs IA)
    Given una tarjeta en el Embudo a punto de auto-aprobarse por la IA en el background tras una re-evaluación
    When un Administrador humano presiona [Descartar] o interactúa con la misma tarjeta en ese mismo milisegundo
    Then el motor de base de datos otorga prioridad absoluta a la transacción humana, bloqueando y revirtiendo la auto-instanciación de la IA.

  Scenario: Auto-Aprobación MLOps (Feature Toggle Opcional)
    Given la configuración del sistema global
    Then el administrador posee una bandera 'Feature Toggle' para encender la "Auto-Instanciación IA"
    And si está encendido y el modelo supera el 98% de confianza, la tarjeta se aprueba sola saltando el embudo humano.

  Scenario: Distintivo Visual de Origen IA en Workdesk Operativo
    Given una Instancia inyectada generada a partir de la aprobación de una Action Card (Manual o Automática)
    When el operador de Trinchera la recibe y visualiza en su lista del Workdesk (Inbox Pantalla 5)
    Then el Frontend renderiza un distintivo gráfico inconfundible (Ej: Ícono de IA o Marco de color)
    And alerta al operador que la existencia de este caso provino originalmente de deducción MLOps.
```
**Trazabilidad UX:** Wireframes Pantalla 16 (Intelligent Intake y Embudo Administrativo).

---

### US-025: Experiencia de 'Cards' Dinámicas por Rol
**Como** Arquitecto de Producto UI
**Quiero** segmentar las Tarjetas Kanban y Dashboards por el rol específico del que mira
**Para** evitar ruido cognitivo y entregar exactamente lo que cada persona necesita (Visibilidad, Ejecución o Seguimiento).

**Criterios de Aceptación (Gherkin):**
```gherkin
 Feature: Arquitectura de Visibilidad Basada en Roles (UX RBAC)
  Scenario: Privilegio Absoluto del System Admin (Omnipresencia) (CA-1)
    Given un usuario autenticado con el rol global de `system_admin`
    When el Frontend (Vue Router) renderiza la Master Page
    Then el Sidebar despliega incondicionalmente todos los accesos (Inicio, Workdesk, Inbox, Proyectos, Dashboards, Configuración, Integraciones, Seguridad, SGDEA)
    And el Header activa todas las herramientas de gestión global (Campana de Notificaciones Full, Búsqueda Inter-Dominio)
    And el Main Content (Dashboard) renderiza todas las Action Cards estratégicas sin censura.

  Scenario: Segregación Estructural del Operario Base (Workdesk Only) (CA-2)
    Given un usuario operativo (Ej: `auditor_junior`, `contractor`) sin privilegios de gestión
    When ingresa a la plataforma
    Then el Sidebar oculta proactivamente (no renderiza sus Nodos DOM) los módulos de Administración (Reglas IA, Configuración, Integraciones, Seguridad, Proyectos Macro)
    And restringe su Sidebar estrictamente a `[🏠 Inicio]`, `[📋 Workdesk]`, y `[📂 Histórico Propio]`
    And en el Main Content (Dashboard/Workdesk), el motor oculta el botón global de `[ + Iniciar Nuevo Proceso ]` si su rol no tiene derechos de instanciación ("Semilla").

  Scenario: Experiencia Aislada del Líder de Intake (Inbox SAC) (CA-3)
    Given un usuario autenticado con el rol especializado `sac_leader` (Gestor de Cuarentena)
    When navega la aplicación
    Then el Sidebar le renderiza el acceso privilegiado al módulo `[📥 Inbox Inteligente]` (Pantallas 1B y 16)
    And el Header le habilita notificaciones específicas sobre SLAs rotos de correos entrantes (Plan A)
    And el Main Content del Dashboard central le proyecta Cards analíticas enfocadas en "Volumen de Embudo actual" y "Tasa de Descarte IA".

  Scenario: Visibilidad del Project Manager y Líderes Ágiles (CA-4)
    Given un usuario con rol de `pm` (Project Manager) o `scrum_master`
    When expande su navegación lateral
    Then el Sidebar le desbloquea el acceso al ecosistema de Proyectos (Pantallas 8, 9, 10, 15)
    And se habilita dinámicamente el botón `[+ Nuevo Proyecto]` en el Main Content correspondiente
    And el Sidebar sigue ocultando herramientas de bajo nivel (Reglas IA, Seguridad, Integraciones API) ajenas a su dominio.

  Scenario: Seguridad Perimetral Frontend (Router Navigation Guards) (CA-5)
    Given que un usuario operativo (sin permisos) intenta forzar la entrada a un modulo prohibido pegando la URL directa (Ej: `/admin/security`) en el navegador
    Then el archivo `RouteGuards.ts` del Frontend intercepta la navegación antes de montar el componente
    And redirige al usuario forzosamente al `[🏠 Inicio]`
    And despliega un Toast indicando que carece de privilegios para acceder a esa sección, protegiendo el Main Content (Router View) de manipulaciones.

  Scenario: Conflicto Multi-Rol (Selector de Perfil Activo) (CA-6)
    Given un usuario autenticado que posee simultáneamente múltiples roles contradictorios en base de datos (Ej: `auditor_junior` y `sac_leader`)
    When el Frontend inicializa la Master Page
    Then en lugar de fusionar caóticamente ambas interfaces, el sistema detecta la multiplicidad
    And inyecta en el Header Superior un "Selector de Perfil Activo" (Dropdown)
    And permite al usuario elegir explícitamente con qué "sombrero" (rol) quiere operar en la sesión actual, redibujando el Sidebar y Main Content instantáneamente para ajustarse a esa única intención.

  Scenario: Refresco Forzoso por Alteración de Privilegios en Caliente (CA-7)
    Given un usuario operando activamente en la plataforma
    When un Administrador de Sistema modifica, revoca o inyecta nuevos roles a dicho usuario desde la Pantalla 14 (Seguridad)
    Then el cambio no se refleja mágicamente "en vivo" arriesgando el estado actual de los formularios
    And el sistema exige un Log-Out / Log-In forzoso (Muerte y regeneración del Token JWT), obligando al usuario a re-autenticarse para que el Frontend parsee desde cero el nuevo árbol de visibilidad del DOM.

  Scenario: Degradación Responsiva (Web Desktop vs Dispositivos Móviles) (CA-8)
    Given el sistema de diseño (UI/UX) conceptual de la plataforma
    When un usuario (incluso con privilegios máximos de Administrador) accede desde un navegador móvil (Viewport < 768px)
    Then la arquitectura Frontend aplica una separación estricta de responsabilidades visuales
    And oculta físicamente herramientas de modelado complejo (Motor BPMN, Diseñador de Formularios Pro-Code, Matrices de Seguridad) que son ergonómicamente inviables en móvil
    And restringe la experiencia móvil puramente a operaciones transaccionales (Workdesk, Aprobaciones simples, y Vista de Dashboards).

  Scenario: Impersonación Transaccional para Soporte (Ver Sistema Como...) (CA-9)
    Given un usuario con rol de `system_admin` atendiendo un ticket de soporte de un empleado operativo
    When el administrador activa la función "Impersonate" o "Ver Sistema Como" desde la Pantalla 14, seleccionando a dicho empleado
    Then el Frontend (Vue) reacciona absorbiendo y limitándose estrictamente al Token simulado de ese empleado
    And el Administrador pierde temporalmente la visión de su Mega-Layout y observa la pantalla exactamente mutilada e idéntica a la que ve el operario al otro lado del mundo, facilitando el diagnóstico de UI.

  Scenario: Política de Ocultamiento Físico (DOM Removal) sobre Atenuación (Disabled) (CA-10)
    Given una pantalla genérica donde coexisten elementos libres y elementos restringidos (Ej: Botón "Aprobar Gasto > $5M")
    When un usuario sin el rol paramétrico (`Aprobador_Financiero`) renderiza esa pantalla
    Then el Frontend obedece una estricta directriz de "Ocultamiento Físico" (`v-if` / destrucción del Nodo DOM)
    And NO DEBE bajo ninguna circunstancia renderizar el botón en estado 'Gris/Atenuado' (`disabled="true"`), previniendo que atributos manipulables desde las DevTools del navegador expongan funcionalidades restringidas.

  Scenario: Estados de Carga Mixtos (Skeleton a Spinner) (CA-11)
    Given un usuario que solicita cargar un volumen de datos desde el backend (Ej: Abrir el Workdesk)
    When el motor Vue inicia la petición HTTP
    Then el sistema dibuja inmediatamente un "Skeleton Screen" (Estructura gris parpadeante) para gratificanción visual instantánea
    And si la latencia de red supera los 5 segundos paramétricos sin recibir el Payload de Camunda, el Skeleton muta mediante una transición suave hacia un "Spinner" de carga clásico que bloquea interacciones preventivamente.

  Scenario: Recompensa Psicológica en Pantallas Vacías (Empty States) (CA-12)
    Given un usuario operativo que vacía exitosamente su Workdesk de tareas pendientes
    When el array de tareas en memoria llega a cero (0)
    Then la interfaz rechaza tajantemente renderizar una tabla de datos vacía o un texto plano "Sin datos"
    And despliega un componente visual de "Empty State Ilustrado" (Ej: Arte gráfico premium con el mensaje de felicitación: "¡Genial, no hay nada pendiente por hoy!") para fomentar el bienestar mental del operario.

  Scenario: Manejo de Errores Transaccionales No Bloqueantes (CA-13)
    Given el fallo de una operación atómica en el backend (Ej: HTTP 500 al intentar aprobar una tarea)
    When el Frontend captura el código de error
    Then el sistema utiliza estrictamente notificaciones efímeras tipo "Toast" renderizadas en la esquina superior derecha
    And estas notificaciones de error desaparecen de forma autónoma tras 5 segundos, evitando interrumpir críticamente el layout visual del usuario con Modals bloqueantes (A menos que el error sea la pérdida total de la sesión).

  Scenario: Micro-interacción de Deshacer (Soft-Undo) (CA-14)
    Given un usuario que ejerce una acción destructiva de bajo impacto visual (Ej: Archivar/Borrar una tarjeta en un Tablero Kanban)
    When la acción es gatillada desde la UI
    Then la tarjeta desaparece inmediatamente de la columna para dar fluidez visual al usuario
    And el sistema despliega un Toast en la parte inferior ("Tarea Archivada [Deshacer]")
    And posterga la petición DELETE al backend otorgando una ventana de gracia de 5 segundos donde el usuario puede cancelar la aniquilación de la data.

  Scenario: Navegación Profunda y Ubicuidad (Breadcrumbs) (CA-15)
    Given un usuario operando en el 3er o 4to nivel de profundidad de la arquitectura de información (Ej: `Proyectos > Alpha > Fase 2 > Tarea Especifica`)
    When el usuario desplaza su vista hacia el Header maestro
    Then encuentra obligatoriamente habilitado un rastro de Migas de Pan (Breadcrumbs) interactivo y clickeable
    And permitiéndole "saltar hacia atrás" en el árbol genealógico del proceso sin tener que recurrir al botón genérico 'Back' del navegador.

  Scenario: Densidad de UI Paramétrica Global (CA-16)
    Given la diversidad de preferencias ergonómicas entre usuarios gerenciales y usuarios de alto volumen transaccional
    Then el módulo de Configuración de Perfil (Header) expone un "Selector de Densidad de Interfaz" (Comfortable vs Compact)
    And esta variable se transmite transversalmente por el Vue Store/Context
    And altera el Padding, Márgenes y tamaños de fuente de *todas* las tarjetas, tablas y modales del ecosistema instantáneamente.

  Scenario: Feedback Transaccional de Salida (Animaciones de Router/Store) (CA-17)
    Given la necesidad humana de seguimiento visual al completar un trabajo
    When un operario presiona [Aprobar] en una tarea del Workdesk
    Then la entidad NO desaparece con un corte abrupto de 0 milisegundos
    And se exige al Arquitecto Frontend empotrar una transición CSS fluida (Ej: `Fade-Out` / `Slide-Up` de 400ms) para que el ojo asimile orgánicamente que el objeto viajó hacia la historia o al servidor.

  Scenario: Optimización del Viewport de Lectura (Header No-Pegajoso) (CA-18)
    Given un usuario consumiendo una lista masiva de datos (Ej. Tabla de Auditoría o Embudo Plan A)
    When el operario comienza a realizar Scroll vertical profundo hacia el Sur del DOM
    Then la barra de navegación superior global (Master Header) abandona el Viewport deslizándose hacia arriba
    And NO se mantiene anclada o pegajosa (Sticky), priorizando la liberación de píxeles cuadrados máximos para el Main Content de lectura intensiva.

  Scenario: Tolerancia Base a la Desconexión (Offline Survival Mode) (CA-19)
    Given una interrupción temporal de la conectividad de red del usuario (Caída de WiFi)
    When el usuario intenta navegar o el Vue Router detecta el quiebre de sockets
    Then la aplicación rechaza tajantemente romperse hacia la clásica pantalla blanca o el dinosaurio del navegador
    And el App Shell (Sidebar y Header) se mantiene 100% dibujado y congelado, renderizando en el Main Content un componente de "Reconectando..." para salvaguardar la ilusión de inmersión y estabilidad de la plataforma.

  Scenario: Renderizado Delegado al Cliente (CSR Architecture) (CA-20)
    Given la naturaleza B2B interna e instrumentada del iBPMS (Detrás de login corporativo, SEO irrelevante)
    When el Arquitecto de Software define el paradigma de carga
    Then el ecosistema se construirá estrictamente bajo Client-Side Rendering (SPA Vue.js standard), descartando el alto costo y latencia del Server-Side Rendering (Nuxt)
    And el contrato visual exige absorber esos 1-2 segundos de ensamblaje en el cliente (Browser) mediante el uso hiper-agresivo de Skeleton Loaders.

  Scenario: Toasts Fatales (Nivel 0 - Imborrables) (CA-21)
    Given la ocurrencia de un evento crítico del sistema (Ej: Caída de Base de Datos, Breach de SLA Multimillonario de un Cliente VIP)
    When el Frontend recibe la señal Websocket o HTTP 500 fatal
    Then el sistema despliega un "Toast Nivel 0" (Alerta Crítica, usualmente en color Rojo Sangre o Negro)
    And esta alerta desafía la regla estándar del auto-borrado: NO desaparece bajo ninguna circunstancia natural, obligando al usuario a realizar un clic físico y consciente sobre la [x] para garantizar el acuse de recibo del desastre.

  Scenario: Flujo Visual a Alta Escala (DOM Virtualization) (CA-22)
    Given que el servidor le responde al Frontend con un payload que contiene 5,000 Action Cards históricas en la Pantalla 16
    When Vue.js reciba la matriz de datos y se disponga a renderizar
    Then la tabla/grilla utilizará obligatoriamente un motor de "Virtual Scrolling" (DOM Virtualization)
    And el navegador solo dibujará físicamente los 20 Nodos HTML que están dentro del viewport visible del usuario, destruyendo recicladamente los que quedan arriba, blindando la plataforma contra desbordamientos de Memoria RAM en PCs de bajos recursos.

  Scenario: Geometría de Foco Accesible (Power User A11y) (CA-23)
    Given un analista experto que opera la plataforma a máxima velocidad utilizando atajos de teclado y la tecla [TAB]
    When el foco del DOM aterriza sobre cualquier botón interactivo, Input o Tarjeta
    Then el CSS/Tailwind debe inyectar obligatoriamente un contorno visual estridente (Ej: Un "Aura Azul" o Ring Focus)
    And garantizando que el usuario jamás necesite usar el mouse para adivinar dónde está operando actualmente.

  Scenario: Internacionalización Estructural (I18n Pre-Cargada) (CA-24)
    Given los planes de despliegue escalar del iBPMS V1
    When se consolide el Layout Maestro (Sidebar, Header y Títulos Estructurales)
    Then estos componentes no deben nacer "Hardcodeados" en Español
    And deben envolverse bajo la arquitectura `vue-i18n` (O equivalente), suministrando desde el Día 1 un selector vivo de idiomas [ES / EN] en el Header para probar la plasticidad lingüística del framework.

  Scenario: Alerta Silenciosa de Inyecciones (WebSockets Mágicos) (CA-25)
    Given un operario estacionado pasivamente mirando su Workdesk sin interactuar con el mouse
    When Camunda (Backend) le asigna subitamente un nuevo caso caliente por detrás
    Then la interfaz NO le obligará a actualizar la página (F5) ni usar técnicas arcaicas de Polling
    And un WebSocket notificará a Pinia (Vue Store), el cual detonará una pastilla flotante no-intrusiva: "✨ 1 Nueva tarea asignada", la cual al ser clickeada inyecta la nueva fila en su vista en vivo.

  Scenario: Maximización de Lienzo (Sidebar Colapsable Voluntario) (CA-26)
    Given un operario que debe transcribir un formulario complejo en una laptop pequeña (Viewport width limitado)
    When siente que el menú estructural le roba espacio vital de su pantalla (Pantalla 2)
    Then el Header le provee un "Toggle de Hamburguesa"
    And al accionarlo, el Sidebar global colapsa con una transición de 300ms reduciéndose a una banda estrecha de iconos, devolviéndole +250px de aire puro al lienzo de ejecución.

  Scenario: Soft-Lock de Inactividad (Pausa de Sesión Flotante) (CA-27)
    Given que el JWT Token del usuario expira por inactividad prolongada
    When el Frontend intercepta el 401 Unauthorized
    Then en lugar de ejecutar una recarga dura y violenta hacia la landing page de Login destruyendo su progreso visual en vivo
    And el sistema inyecta un "Modal de Bloqueo de Sesión de Pantalla Completa" (Efecto Glassmorphism), oscureciendo el entorno operativo pero dejando ver que su trabajo sigue ahí debajo, exigiendo únicamente reingresar la contraseña en ese mismo modal para resucitar el token y destrabar la pantalla.

  Scenario: Renderizado Diferido para Tableros Densos (Lazy Loading) (CA-28)
    Given el Dashboard de Gerencia (Pantalla 5) el cual aloja simultáneamente 10 gráficas estadísticas pesadas y múltiples tablas
    When la página se monta (Mounted Lifecycle)
    Then Vue NO solicitará los datos ni renderizará los canvas de las 10 gráficas simultáneamente
    And implementará el patrón IntersectionObserver (Lazy Loading), renderizando y consultando la Base de Datos o Apache ECharts únicamente a medida que la gráfica particular penetra en el campo visual del usuario al hacer Scroll Vertical.

  Scenario: Visualización de Contexto ONS en Pestañas (Tab-Based UI) (CA-29)
    Given la necesidad de renderizar un área de trabajo (Workdesk o Intake) que posee un hilo de correos o historial asociado (Contexto Pre-SD)
    When el Frontend construye la interfaz del Main Content Area
    Then el diseño abandonará definitivamente el enfoque de "Pantalla Dividida" (Split-Screen) para maximizar el lienzo de trabajo
    And implementará un modelo ergonómico basado en Pestañas (Tabs) consumiendo el 100% del espacio disponible
    And la "Pestaña 1 [Formulario Operativo]" estará dedicada exclusivamente a la visualización y diligenciamiento del formulario de Camunda
    And la "Pestaña 2 [Contexto y Correos]" contendrá exclusivamente el registro del hilo de correos originales (US-023) y el historial de eventos.

  Scenario: Renderizado Estricto de iForm Maestros vs Formularios Simples (CA-30)
    Given que el sistema se dispone a renderizar la "Pestaña 1 [Formulario Operativo]"
    When el motor evalúa la naturaleza del formulario asociado a la tarea actual
    Then si es un Formulario Simple, este se renderizará de golpe hacia abajo (Flat layout)
    And si es un "iForm Maestro" (Expediente Multi-Etapa), el renderizador (MaestroFormRender.vue) TIENE PROHIBIDO listar los inputs de forma plana hacia abajo
    And el sistema debe respetar e inyectar estrictamente el componente Vue original diseñado para esa etapa específica
    And preservando intacto su CSS, UI Density, Columnas y Cuadrículas (Grids) originales configurados en el Form Designer
    And la interfaz debe estar coronada obligatoriamente por un componente "Stepper" en la parte superior para trazabilidad de las etapas.
```

**Trazabilidad UX:** Layout Maestro (Sidebar Lateral, Header Superior) y Pantallas 0 (Dashboard) y 1 (Workdesk).

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

**Trazabilidad UX:** Wireframes Pantalla 18 (Portal B2B/B2C del Cliente).

---

## ÉPICA 12: Gobierno de Identidad y Accesos (RBAC Multirrol)
Garantizar que la plataforma soporta el modelo corporativo real donde un usuario ejerce múltiples funciones simultáneamente mediante asignación de múltiples roles y grupos de EntraID.

### US-038: Asignación Multi-Rol y Sincronización EntraID
**Como** Administrador de Seguridad
**Quiero** asignar o sincronizar múltiples roles (Globales y de Proceso) a un mismo usuario autenticado
**Para** que pueda acceder a las distintas bandejas y tareas correspondientes a todos sus 'sombreros' operativos sin necesidad de tener cuentas separadas.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Multitenant RBAC & Multiple Roles Assignment

  # A. El Inicio de Sesión y la Muerte Súbita
  Scenario: Revocación Inmediata de Acceso (CA-1)
    Given un usuario operando activamente dentro del iBPMS
    When su cuenta es desactivada o removida de todos los grupos de acceso en EntraID (Directorio Activo)
    Then el sistema debe detectar la invalidez del Token y expulsarlo inmediatamente hacia la pantalla de Login
    And no permitirá la ejecución de ninguna transacción en vuelo, arrojando HTTP 401 Unauthorized.

  Scenario: Prevención del Secuestro de Roles (CA-2)
    Given un usuario que ayer poseía el rol "Líder_SAC" pero que hoy le fue revocado en EntraID
    When el usuario con una sesión antigua (Token vivo de ayer) intenta aprobar un reclamo
    Then el backend intercepta la petición y valida asíncronamente los Claims actuales antes del Commit
    And rechaza la operación si el rol ya no le pertenece, forzando un refresco de sus permisos (Log-Out silencioso o recarga de la Master Page).

  # B. La Pelea de Permisos
  Scenario: Resolución de Permisos Contradictorios (Regla del Más Restrictivo) (CA-3)
    Given un usuario que hereda simultáneamente un "Rol A" (Permite Borrado) y un "Rol B" (Prohíbe Borrado estricto) sobre el mismo objeto
    When el motor de políticas de Vue y Spring Security evalúan el acceso
    Then el sistema debe resolver el conflicto aplicando siempre "La Regla del Rol Más Restrictivo" (Deny-Overrides)
    And inhabilitando físicamente el botón de [Borrar] en la UI.

  Scenario: Detección de Conflicto de Segregación de Funciones (Juez y Parte) (CA-4)
    Given un usuario al que se le han asignado por error los roles exclusivos de "Creador de Pedido" y "Aprobador Financiero"
    When el usuario inicia sesión
    Then el sistema le permite ingresar y operar
    And dispara inmediatamente una Alerta Roja asíncrona ("Conflicto SoD Detectado") hacia el Módulo de Anomalías del Administrador
    And dejará una traza en la Auditoría etiquetando todas las transacciones de este usuario con un flag `WARNING_SoD_CONFLICT` hasta que el administrador depure sus roles en el AD.

  # C. La Bandeja de Entrada Mágica (Workdesk)
  Scenario: Consolidación Transversal en el Workdesk (CA-5)
    Given un usuario con múltiples sombreros (Ej: 3 roles operativos distintos)
    When abre su vista de Workdesk
    Then el sistema consolida TODAS sus tareas en una única lista unificada y limpia
    And NO fragmenta la pantalla en múltiples pestañas ni lo obliga a saltar entre perfiles (La segregación de vistas complejas se abordará en historias de Dashboarding, no en la trinchera operativa).

  Scenario: Insignia de Procedencia del Rol en la Tarjeta (CA-6)
    Given la lista de tareas unificadas en el Workdesk
    Then cada tarjeta (Card o Fila) debe inyectar un Badge/Etiqueta visual discreta (Ej: `Rol: Aprobador_Nivel_2`)
    And explicándole al usuario exactamente bajo qué "Sombrero" o prerrogativa de negocio se le está exigiendo resolver ese caso específico.

  Scenario: Retorno al Pool Común por Renuncia (CA-7)
    Given una tarea instanciada y encolada específicamente bajo un candidato de rol (Ej: "BPMN_Credito_Aprobador")
    When el único usuario en toda la empresa que poseía ese rol renuncia y es desactivado
    Then la tarea NO entra en Dead-Letter ni se pierde en un agujero negro
    And la tarea permanece visible y viva en el Pool (Cola del Grupo)
    And esperando a que el Administrador de TI asigne ese rol a un nuevo empleado para que pueda visualizarla y reclamarla.

  # D. Infraestructura de Mentiras (Fallas)
  Scenario: Autenticación de Respaldo por Falla de Azure (Puerta Secreta) (CA-8)
    Given que la infraestructura de Microsoft EntraID sufre una caída global (HTTP 503)
    When los usuarios intentan loguearse a través de OAuth/OIDC
    Then el sistema fallará elegantemente
    And debe existir una URL local oculta (Backdoor pre-configurado en Frontend) que permita autenticación directa (Email/Contraseña) contra la tabla interna de usuarios
    And solo disponible para un listado minúsculo de Administradores Locales (Break-Glass Accounts) para mantener la plataforma viva durante la crisis.

  Scenario: Mapeo Flexible de Identidades (CA-9)
    Given la arquitectura global de autenticación del iBPMS
    Then el sistema debe soportar tres (3) modos de operación paramétricos para la ingesta de Roles:
    And 1. Sincronía Total (1:1): Los grupos y roles se inyectan 100% desde Azure EntraID.
    And 2. IdP Híbrido: Azure solo se usa para el "Login" (Validar existencia), pero el iBPMS asigna localmente sus propios roles en su base de datos.
    And 3. IdP Local Absoluto: El sistema funge como su propio Autenticador (Módulo interno de Usuarios y Passwords).

  # E. Auditoría y Rastro de Migas
  Scenario: Trazabilidad Quirúrgica en Logs (CA-10)
    Given un usuario multi-rol ejecutando una transacción crítica (Ej: Aprobar Pago)
    When el Backend estampa el evento en la bitácora de auditoría (AiAuditLogEntity)
    Then la tupla de base de datos debe almacenar el máximo detalle forense
    And registrando no solo el `user_id` y `timestamp`, sino también un JSON con el "Contexto de Roles Activos" (`active_claims`) en el milisegundo exacto de la ejecución para peritajes legales.

  Scenario: Restricción del Préstamo de Llaves (CA-11)
    Given un empleado multi-rol que se ausenta temporalmente
    When intenta ceder uno de sus sombreros críticos a un compañero desde la UI de la plataforma
    Then el Frontend bloquea la intención careciendo de una interfaz para "Transferencia de Roles en Caliente"
    And obliga procedimentalmente a que el compañero solicite el permiso transitorio a través del conducto regular (Directorio Activo de TI).

  # F. Delegaciones 
  Scenario: Trazabilidad de la Delegación (Workdesk) (CA-12)
    Given que María le delega una de sus tareas de Workdesk a Carlos (US-001)
    When María ejecuta la transferencia
    Then la auditoría debe grabar el evento indicando "Delegan: María -> Carlos"
    And la tarea aparece en la bandeja de Carlos junto con un Popup o Banner exigiéndole ACEPTAR o RECHAZAR la delegación encomendada.

  Scenario: Rebote Natural por Rechazo de Delegación (CA-13)
    Given la delegación pendiente descrita en CA-12
    When Carlos presiona el botón [Rechazar Delegación] en el Popup
    Then la tarea abandona inmediatamente el Workdesk de Carlos
    And vuelve a rebotar como un boomerang regresando a ser la responsabilidad exclusiva en el Workdesk de María (Propietaria original), conservando todo el historial en la auditoría.

  Scenario: Pre-Autorización a Pasajeros Nuevos (CA-14)
    Given un usuario que acaba de ser contratado e instanciado en Microsoft EntraID hoy a las 8:00 AM
    When el usuario ingresa por primera vez a la URL del iBPMS a las 8:05 AM
    Then el sistema NO lo detiene en pantallas de "Cree su perfil" ni "Espere aprobación"
    And mapea al vuelo (Just-In-Time Provisioning) sus atributos y roles del Token de Azure, dejándolo pasar derecho hacia su Workdesk operativo instantáneamente.

  # G. Visuales (Lo que ve el ojo)
  Scenario: Rendimiento Estricto en Parseo de Mega-Roles (CA-15)
    Given un Gerente General que porta un listado demencial de +80 roles y permisos combinados en su perfil de Azure
    When el usuario hace el proceso de Login y redirección
    Then el tiempo de cómputo del Frontend para desenredar iterativamente ese árbol de permisos y renderizar el Layout (Sidebar/Header)
    And no debe superar los 500ms bajo ninguna circunstancia (Complejidad O(1) o indexación en Pinia Store), garantizando que su ingreso se sienta inmediato.

  Scenario: Indicador Tipográfico de Dominio (Header) (CA-16)
    Given un usuario multi-rol que finaliza su carga inicial
    Then el Master Header, justo debajo o al lado de su Nombre/Avatar, debe renderizar un micro-texto o chip
    And resumiendo visualmente los 2 o 3 "Sombreros Principales" (Ej: `Director Comercial | Aprobador VIP`) que el parseador calculó que está usando hoy, validándole al operario que sus permisos subieron correctamente.

  # H. Módulo de Tablero de Anomalías de Seguridad (NUEVO)
  Scenario: Tablero de Resolución de Anomalías de Seguridad (CA-17)
    Given que el sistema ha detectado conflictos graves (Ej: CA-4 SoD Conflict - Juez y Parte)
    When un Administrador de Seguridad ingresa a la Pantalla de Configuración / RBAC (Pantalla 14)
    Then el sistema debe darle acceso a una pestaña especializada denominada "Tablero de Anomalías"
    And este tablero listará en color Rojo todas las incidencias de seguridad vivas detectadas por el motor
    And obligará al Administrador a revisar el caso, subsanar el error a nivel EntraID/Local, y presionar físicamente un botón `[ ✅ Marcar como Subsanado ]` para apagar la alerta.

  # I. Mantenimiento Evolutivo de Recuperación (V2)
  Scenario: Postergación de Reset de Password para V2 (CA-18)
    Given que el sistema opera en modo de IdP Local (Tabla propia de usuarios sin Azure)
    When un usuario olvida su contraseña
    Then la responsabilidad del Frontend y Backend de crear pantallas transaccionales de "Recuperar Contraseña via Email / OTP" queda estrictamente aplazada fuera del alcance del MVP V1.
    And el proceso de recuperación manual en V1 queda relegado a una solicitud verbal/correo al Administrador del Sistema.

```
**Trazabilidad UX:** Wireframes Pantalla 14 (Seguridad RBAC) y Tablero de Anomalías.

---

### US-048: Módulo Gestor Propio de Identidades (Internal IdP)
**Como** Súper Administrador del Sistema
**Quiero** gestionar centralizadamente los usuarios y roles internos del iBPMS
**Para** tener un control granular sobre quién accede a qué funcionalidades, incluso si el cliente no tiene un IdP externo robusto.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Gestión de Identidades Internas (Internal IdP)
  Scenario: Creación Exclusiva por Administrador (V1 Centralizada) (CA-1)
    Given la necesidad de registrar un nuevo empleado en el iBPMS (Modo Standalone)
    Then para el MVP V1, el Súper Administrador es el único facultado para crear cuentas mediante un formulario interno (Pantalla 14)
    And la creación de cuentas mediante portales públicos de "Regístrate Aquí" queda estrictamente diferida para V2.

  Scenario: Gobernanza Estricta de Contraseñas Seguras (CA-2)
    Given la creación o actualización de una credencial local
    Then el Frontend (Formulario y Login) y el Backend deben acatar y forzar políticas Enterprise de seguridad
    And exigiendo obligatoriamente: Mínimo 8 caracteres, 1 Mayúscula, 1 Número y 1 Símbolo Especial
    And bloqueando el botón de [Guardar] si la entropía de la clave es débil.

  Scenario: Destrabe Administrativo de Credenciales (Reset Manual) (CA-3)
    Given un usuario bloqueado u olvidadizo (sin soporte de Auto-Reset vía Email en V1)
    Then el Súper Administrador visualizará un botón de emergencia `[Generar Clave Temporal]` en la ficha del empleado
    And al accionarlo, el sistema reemplaza el hash anterior y devuelve una cadena temporal visible por única vez para que el Admin la comunique verbalmente.

  Scenario: Fábrica de Roles Dinámicos (Role CRUD) (CA-4)
    Given una organización que no cuenta con grupos de Directorio Activo (EntraID)
    Then el módulo Interno de Identidad (Pantalla 14) debe poseer una pestaña de "Gestión de Roles"
    And permitiendo al Súper Administrador bautizar roles nuevos a voluntad (Ej: `Analista_Riesgo_Senior`)
    And otorgando una interfaz drag-and-drop o checkboxes multiselect para asociar usuarios hacia esos sombreros creados.

  Scenario: El Botón de Emergencia (Kill Switch Activo/Inactivo) (CA-5)
    Given un empleado enfrentando un proceso disciplinario o despido en tiempo real
    Then la tabla de usuarios (CRUD) expone un Toggle Switch visible `[Estado: Activo/Inactivo]`
    And al apagarlo, el Backend no solo marca el registro lógico como inactivo, sino que destruye activamente cualquier sesión viva (JWT/Redis) de ese usuario, expulsándolo instantáneamente al Login.

  Scenario: Asignación Híbrida de Múltiples Sombreros Locales (CA-6)
    Given la vista de edición de un usuario
    Then la interfaz debe permitir la selección múltiple (Ej: un Dropdown de selección múltiple)
    And logrando que el Administrador asigne libremente 1, 2 o N roles organizacionales al mismo individuo convergentes en una única sesión local (Sinergia con US-038).

  Scenario: Mutación de Interfaz en Modo Híbrido EntraID (CA-7)
    Given que el sistema se configuró para delegar el "Login de Acceso" a Microsoft EntraID, pero gobernar los "Roles" de forma local (iBPMS)
    When el Súper Administrador edita el perfil de un individuo
    Then la interfaz de Frontend oscurece o desaparece forzosamente los campos de "Contraseña" y "Cambiar Clave" en esa ficha
    And impidiendo intentos de alteración de credenciales locales que le pertenecen exclusivamente al IdP externo, evitando colisiones de Sincronía.
```
**Trazabilidad UX:** Pantalla 14 (Panel IdP Local).

---

### US-041: Vista 360 del Cliente (Consolidación Global Externa)
**Como** Ejecutivo de Cuenta
**Quiero** visualizar un perfil consolidado agrupando el progreso de un Cliente Específico
**Para** saber exactamente el estado en el que se encuentran todos sus requerimientos sin importar en qué proyecto técnico viven (Ágiles o BPMN).

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Consolidación Transversal de Requerimientos y Workflows
  Scenario: Renderizado de Vista 360 para Cuenta / Cliente
    Given un Ejecutivo de Cuenta navega el perfil de un Cliente Específico en su directorio
    Then la interfaz agrupa y presenta TODAS las instancias de procesos BPMN y proyectos Ágiles que posean el mismo CRM_ID
    And posee un botón toggle para alternar entre la vista de "Operación Activa" y el "Histórico" (Archivado)
    And consolida el porcentaje de avance global calculado explícitamente por 'Esfuerzo' en un Gauge semaforizado
    And permite al ejecutivo forzar un [Inicio Rápido de Instancia] manual ahorrando la asociación del CRM_ID pre-quemado.

  Scenario: Segregación de Comentarios Confidenciales
    Given procesos técnicos e hilos de chat interno que contienen comentarios entre operarios
    Then la Vista 360 externa omite tajantemente estos comentarios internos, visualizando únicamente status, transiciones y el 'Front-Facing Metadata'.

  Scenario: Degradación Elegante ante falla parcial (Micro-frontends)
    Given una caída de comunicación (Timeout API) con The CRM Central
    When se intenta cargar la vista 360 del Customer Account Rule
    Then se renderiza la información local cacheada de Camunda parcialmente
    And muestra un banner claro informando: "Se ha tenido algunos inconvenientes en nuestras fuentes, estamos trabajando en solucionarlo" notificando via sistema al Administrador IT.
```
**Trazabilidad UX:** Wireframes Pantalla 17 (Vista 360 del Cliente).

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


---

## ÉPICA 8: Extensiones Cognitivas AI-Native (Cognitive BPMN)
Aborda la integración nativa de Inteligencia Artificial (LLMs, RAG) en el modelado BPMN (Pantalla 6) y la ejecución del iBPMS, evolucionando de procesos secuenciales estáticos a procesos aumentados cognitivamente.

### US-032: Orquestación de IA y Generative Task (RAG)
**Como** Arquitecto Funcional
**Quiero** disponer de tareas especializadas en IA dentro del diseñador BPMN
**Para** modelar flujos donde un Agente de IA analiza documentos y redacta contenido estructurado sin interrumpir el motor lógico de Camunda.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Componentes AI-Native BPMN y Controles
  Scenario: Output Estricto basado en Schema JSON (CA-1 - Opción A)
    Given el Arquitecto configura una "Generative Task (RAG)" en la Pantalla 6
    Then el motor de IA tiene prohibido generar formato visual (HTML/Docs) directamente
    And está forzado a devolver la información estructurada mediante un Esquema JSON (Extraer y Rellenar)
    And el iBPMS fusiona ese JSON con la plantilla inmutable oficial antes de mostrársela al usuario final.

  Scenario: Desbloqueo de Conocimiento PII con Políticas Estrictas (CA-2)
    Given la tarea "Generative Task (RAG)" necesita consultar la Bóveda SGDEA
    When el LLM busca contexto en documentos clasificados como Privados o PII
    Then se permite la lectura para la generación de la respuesta
    But la política de seguridad (RBAC/DLP) enmascara o prohíbe exponer directamente estos datos sensibles al usuario que no tiene dichos privilegios.
    
  Scenario: Vectorización de Conocimiento a Demanda (CA-3)
    Given el Administrador de Conocimiento sube archivos al SGDEA
    Then dispone de un botón "[Actualizar Memoria IA (Embeddings)]"
    And la vectorización a la base de datos vectorial (Ej: Milvus/Pinecone) no ocurre automáticamente en cada subida de archivo para no degradar el rendimiento, sino de forma controlada y explícita.

  Scenario: Botón de Pánico Anti-Alucinaciones (CA-4)
    Given un usuario humano revisa un borrador generado por la IA en su formulario (Pantalla 7)
    Then dispone de un botón global estilo "[👎 Reportar Alucinación / Error IA]"
    And al accionarlo, se cancela el comportamiento automático, se emite una alerta al "Ingeniero de Prompts" y el proceso se redirige al flujo manual por defecto.

  Scenario: Trazabilidad a las Fuentes (Citas Interactivas) (CA-5)
    Given la "Generative Task (RAG)" emite una respuesta argumentativa
    Then el texto debe incluir referencias o hipervínculos a los IDs documentales usados como contexto
    And cuando el usuario hace clic, el iBPMS lo redirige al visor del SGDEA con la sección exacta resaltada, garantizando verificación humana.

  Scenario: Budget Configurable de Tokens LLM (CA-6)
    Given el Arquitecto configura la tarea cognitiva en la Pantalla 6
    Then existe un parámetro limitante de "Budget de Tokens / Consumo Mensual"
    And si el proceso agota su cuota asiganada, se corta el acceso a la IA y el motor enruta a las ramas B (flujos manuales alternativos) automáticamente.

  Scenario: Gobernanza de Prompts Centralizada (CA-7)
    Given la necesidad de alterar las instrucciones base de los Agentes RAG
    Then existe una pantalla separada llamada "Enterprise Prompt Library"
    And solo los usuarios con el rol especializado `prompt_engineer` tienen permisos CRUD sobre estos prompts globales, dejando a los Arquitectos BPMN únicamente con la facultad de consumirlos.

  Scenario: Tolerancia a Fallos Multi-LLM (Failover Pattern) (CA-8)
    Given la tarea "Generative Task (RAG)" está configurada para consumir un modelo principal (Ej: Azure OpenAI GPT-4o)
    When este proveedor primario sufre una caída (Downtime / HTTP 503)
    Then el Agente Orquestador del iBPMS no detiene el proceso inmediatamente
    And realiza un salto automático transparente (Failover) a un modelo de respaldo configurado (Ej: AWS Bedrock Claude 3.5) para garantizar la continuidad operativa.

  Scenario: Auditoría Transparente de IA sin Marcas de Agua (CA-9)
    Given un PDF oficial generado a partir de una plantilla con texto redactado por IA
    When el usuario humano aprueba el texto y el sistema emite el documento final
    Then el documento PDF NO incluye advertencias públicas ni marcas de agua de "Generado por IA" para conservar la imagen corporativa
    And el iBPMS persiste en su base de auditoría interna un registro estricto inmutable indicando "Borrador Generado por IA - Validado y Aceptado bajo responsabilidad del usuario [Nombre] con ID [X]".

  Scenario: Parametrización de Límites de Lectura Comprensiva (CA-10)
    Given un expediente que incluye anexos documentales extremadamente extensos (+500 páginas)
    When el proceso pasa los anexos como contexto al Agente RAG
    Then el Arquitecto BPMN puede haber parametrizado "Límites de Extracción" en la configuración de la tarea cognitiva
    And el sistema recorta inteligentemente el contexto a enviar (Ej: "Evaluar solo las primeras 20 páginas" o "Límite: 100k tokens") previniendo gastos desmesurados de cómputo.

  Scenario: Control Bidireccional de Tono Redaccional (CA-11)
    Given el Arquitecto BPMN arrastra una Generative Task al lienzo en Pantalla 6
    Then el panel de propiedades incluye un Dropdown "Tono de Comunicación" (Ej: Empático, Formal/Legal, Comercial)
    And esta instrucción se inyecta dinámicamente como Sistema al Prompt principal sin requerir que el Arquitecto reescriba el Prompt base de la librería.

  Scenario: Validación Invisible de Doble Agente (Self-Reflection) (CA-12)
    Given el modelo LLM principal genera un borrador de respuesta
    Then en flujos de criticidad alta, el iBPMS enruta temporalmente ese borrador a un segundo "Agente Validador Invisible"
    And si el Validador detecta Alucinaciones graves o violaciones de PII, obliga al modelo principal a reescribir la respuesta internamente antes de presentársela al analista humano en su Workdesk.

  Scenario: Auditoría Legal del Prompt Exacto (CA-13)
    Given que el "Ingeniero de Prompts" altera el prompt oficial corporativo frecuentemente
    When un proceso cognitivo finaliza y guarda la respuesta generada
    Then la base de datos almacena el texto íntegro e inmutable del Prompt específico que se usó en ese milisegundo exacto
    And permitiendo auditorías forenses (¿Qué le ordenamos a la IA ese día?) años después del evento de ejecución.

  Scenario: Bucle de Retroalimentación Humana (RLHF) (CA-14)
    Given el Abogado recibe un borrador generado por la IA en su formulario
    When el Abogado rechaza el texto y lo reescribe manualmente antes de enviar
    Then el iBPMS guarda el par de datos "[Borrador IA Original] vs [Texto Humano Final]" en una base de datos de telemetría MLOps
    And este corpus queda disponible para futuras sesiones de ajuste fino (Fine-tuning) del modelo base corporativo.

  Scenario: Aseguramiento DLP e IT Security en Nube Pública (CA-15)
    Given que el LLM está hospedado fuera de la infraestructura local (Ej: Azure, OpenAI)
    When el iBPMS emite el llamado de red con el contexto (Cuerpo de PQRS)
    Then un interceptor de Seguridad IT / DLP (Data Loss Prevention) evalúa y enmascara PII (Nombres, Cédulas, Tags) reemplazándolos por Hash-Tokens pseudo-anonimizados
    And la IA procesa los hashes, y al devolver la respuesta redactada, el interceptor re-hidrata los Hashes a su valor original PII dentro del perímetro seguro local.

  Scenario: Traducción Activa de Salida (CA-16 - Diferido a V2)
    Given el cliente escribe en un idioma extranjero (Ej: Inglés)
    # NOTA: Diferido a V2. En V1 la IA entiende el inglés pero la instrucción general del Prompt fuerza la respuesta en Español.

  Scenario: Adjuntos Generativos y Bucle de RLHF Documental (CA-17)
    Given una tarea "Generative Task (RAG)" configurada para exportar un archivo .DOCX
    When la IA redacta el contenido y genera el documento asociado al proceso
    Then si el humano no lo acepta y edita el archivo Word subiéndolo de nuevo (o haciendo comentarios)
    And el iBPMS captura el "Delta" (diferencias) entre el documento IA y la corrección humana para usarlo como métrica de retroalimentación de calidad.

  Scenario: Bucle Iterativo por Notas o Comentarios (CA-18)
    Given el usuario revisa el borrador generado por la IA y no está satisfecho
    When en lugar de editarlo manualmente, opta por la revisión guiada
    Then utiliza un panel de "Notas / Comentarios" para instruir correcciones (Ej: "Hazlo más corto y cordial")
    And la tarea cognitiva vuelve a ejecutarse tomando ese comentario humano como contexto mandatorio para el re-intento.

  Scenario: Selección de Modelo a Nivel de Ejecución (CA-19)
    Given el Arquitecto BPMN ha parametrizado "Metadatos de Sugerencia" indicando qué IA usar (Ej: Nivel Inferior)
    When la tarea cognitiva llega al Workdesk del usuario final
    Then el Usuario Ejecutor es quien tiene la potestad final en la UI para elegir qué modelo exacto procesará la solicitud, utilizando la sugerencia como base.

  Scenario: Termómetro de Seguridad (Confidence Score) (CA-20)
    Given el modelo LLM genera una respuesta
    Then el sistema debe mostrar visualmente en el Workdesk un "Confidence Score" (Nivel de Certeza)
    And advirtiendo al revisor humano si la certidumbre matemática de la IA es peligrosamente baja.

  Scenario: Transparencia Cognitiva Continua (Chain of Thought visible) (CA-21)
    Given que el LLM estructura un argumento complejo
    Then el sistema debe solicitar y capturar el "Chain of Thought" (Paso a paso lógico de la IA)
    And exponerlo como un log oculto pero auditable en la metadata de la instancia para que el administrador/humano entienda el "por qué" de la decisión.

  Scenario: Contexto Humano Ad-Hoc en Vivo (CA-22)
    Given la memoria base de la IA (SGDEA) está limitada
    Then el usuario final que está revisando la tarea puede, en tiempo real, adjuntar un PDF local desde su PC
    And ordenar a la IA que reevalúe y genere un nuevo borrador incluyendo ese documento exclusivo y saltándose el RAG tradicional.

  Scenario: Versionamiento y Máquina del Tiempo de Prompts (CA-23)
    Given el modulo Enterprise Prompt Library
    When el Ingeniero de Prompts realiza alteraciones al texto de instrucción
    Then el sistema crea versiones inmutables al estilo Git (v1, v2)
    And existe un mecanismo de reversión instantánea (Rollback) por si el nuevo prompt causa degradación operativa generalizada.

  Scenario: Cola de Procesamiento por Lotes (Batch Dispatcher) (CA-24)
    Given un volumen alto de invocaciones a la IA
    Then el sistema enruta estas tareas a una "Cola de Despacho" paramétrizable por el Administrador de Prompts
    And esta cola maneja límites de concurrencia, reintentos por falla de la API, y estrategias Backoff automáticas.

  Scenario: Interfaz Asíncrona sin Bloqueo de Navegación (CA-25)
    Given la generación de IA está ejecutándose en la Cola de Despacho (CA-24)
    Then la Interfaz de UI presenta un mensaje personalizado indicando que el proceso "Está siendo procesado por IA"
    And NO bloquea al usuario, permitiéndole paralelamente atender otras tareas u operar otras pantallas del iBPMS libremente.

  Scenario: Gatillo Exclusivamente Imperial (Acción Humana) (CA-26)
    Given un flujo de procesamiento que involucra el componente de IA
    Then el sistema tiene una regla arquitectónica imperativa: la invocación a la IA no puede ocurrir por auto-transición 100% de fondo de Camunda
    And exige siempre que el Gatillo (Trigger) originario haya sido el "Clic" explícito de un Usuario Humano en la pantalla precedente o actual, impidiendo escapes automatizados ciegos.

  Scenario: UX de Carga Asíncrona (Prevención de Streaming) (CA-27)
    Given la generación de un documento IA
    Then la interfaz de usuario utiliza un "Loading Spinner" tradicional y peticiones HTTP estándar en lugar de WebSockets (Efecto Máquina de Escribir)
    And priorizando la eficiencia de memoria del servidor web B2B frente a la espectacularidad visual.

  Scenario: Regeneración Parcial por Fragmentos (CA-28)
    Given un borrador extenso generado por la IA en la pantalla del analista
    When el usuario selecciona únicamente un párrafo y hace clic en "Comentar / Corregir"
    Then el iBPMS enruta a la IA exclusivamente el fragmento seleccionado junto con la instrucción humana (Ej: "Haz este párrafo más formal")
    And la IA devuelve el fragmento modificado, fusionándose in-place sin necesidad de reescribir ni gastar tokens en el texto adyacente que ya fue aprobado.

  Scenario: Privacidad de Auditoría Cognitiva (CA-29)
    Given que el proceso generó métricas de "Confidence Score" y "Chain of Thought"
    Then estas métricas son de consumo estrictamente interno
    And por ningún motivo se exponen al Ciudadano Externo en la Pantalla 18 (Portal B2B/B2C).

  Scenario: RAG Multimodal Controlado (V1) (CA-30)
    Given la ingesta de documentos anexos para contextualizar a la IA
    Then el Agente RAG soporta en su V1 la lectura de documentos PDF, DOCX e Imágenes (OCR integrado a la API de visión)
    And excluyendo formalmente notas de voz o video (Diferido a V2).

  Scenario: Eficiencia de Contexto Pre-Empaquetado (IA Amarrada) (CA-31)
    Given una tarea generativa que requiere datos externos (Ej: Saldos ERP)
    Then la IA tiene prohibido usar "Function Calling" autónomo para ir a buscar datos por su cuenta (Gasto excesivo de tokens y memoria de razonamiento)
    And la arquitectura dicta que Camunda Engine, mediante Service Tasks previas y baratas, extraiga la data y se la entregue pre-empaquetada en el Prompt a la IA para que esta se limite únicamente a redactar.
```
**Trazabilidad UX:** Pantalla 6 (BPMN Designer Palette), Pantalla 12 (SGDEA), Pantalla 7 (Form Builder UI).

---

## ÉPICA 9: Hub de Integraciones y Conectores (Integration Hub - Pantalla 11)
Gobierna la comunicación bidireccional entre el motor iBPMS y los sistemas externos (CRM, ERP, SGDEA de terceros como SharePoint), garantizando resiliencia y estandarización mediante conectores reutilizables.

### US-033: Catálogo de API y Mapeo Visual
**Como** Arquitecto Técnico / Interfaz
**Quiero** disponer de un Hub centralizado para configurar conectores HTTP
**Para** asociar de forma visual las variables de procesos BPMN con endpoints externos, gestionando tolerancia a fallos y reglas de seguridad de nivel Enterprise.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: API Connector Configuration and Resiliency
  Scenario: Exclusividad Rest JSON (Delegación a APIM) (CA-1)
    Given la necesidad de interactuar con un sistema Core de tecnología antigua (Ej: SOAP XML)
    Then el Integration Hub del iBPMS emite su comunicación internamente en formato estándar `REST JSON`
    And la arquitectura delega imperativamente la conversión de protocolos a la capa de API Management (APIM) middleware corporativo, manteniendo el iBPMS libre de librerías legacy.

  Scenario: Compatibilidad de Autenticación Segura (CA-2)
    Given que el Arquitecto configura un nuevo Conector en la Pantalla 11
    Then el formulario debe disponer de soporte imperativo para inyección de Headers `Basic Auth` y credenciales `OAuth 2.0 / JWT`
    And estos credenciales deberán estar almacenados en la Bóveda de Secretos encriptada, jamás en texto plano.

  Scenario: Ausencia de Agentes Locales On-Premise (CA-3)
    Given la necesidad de conectar el iBPMS en la Nube con el ERP interno corporativo
    Then el iBPMS no provee "Agentes VPN Inversos" ni demonios de instalación local
    And la arquitectura asume que el acceso infraestructural está resuelto mediante puertos habilitados en el FireWall corporativo bajo responsabilidad exclusiva del área de IT.

  Scenario: Tolerancia a Caídas y Retry Asíncrono (CA-4)
    Given la invocación a la API externa de SharePoint que retorna error HTTP 500
    Then el motor encola la petición fallida en base de datos (Dead Letter Queue controlada)
    And realiza intentos de backoff silenciosos (Ej: cada 5 minutos por 1 hora)
    And si persisten los fallos, enruta el task a modalidad de "Fallback" alertando al analista humano en el Workdesk de la interrupción integrativa.

  Scenario: Data Mapping Gráfico (Drag and Drop) (CA-5)
    Given la configuración de un catálogo de retorno (Ej: CRM devuelve el perfil del cliente)
    Then la interfaz expone visualmente un árbol JSON de variables de entrada a la izquierda vs variables del BPMN a la derecha
    And permite dibujar conexiones (Drag & Drop mapping) sin requerir que el Arquitecto estructure JSONPath a mano.

  Scenario: Trazabilidad y Logs Híbridos (CA-6)
    Given una prueba de conexión fallida por Timeout
    Then la Pantalla 11 emite en su consola técnica el Log Raw inmediato del error para el diseñador
    And las ejecuciones fallidas en producción se delegan adicionalmente al sistema APIM o a la bitácora interna de Kibana para trazabilidad forense.

  Scenario: Validación Profunda de Payload Fantasma (CA-7)
    Given una API mal implementada que devuelve un estatus `HTTP 200 OK` pero el cuerpo del JSON incluye el key `{"error": true, "code": "USER_NOT_FOUND"}`
    Then el Hub permite al Arquitecto declarar "Reglas Límite de Payload" para que el iBPMS separe visualmente si una petición fue exitosa lógicamente (no solo analizando el status header HTTP).

  Scenario: Censura DLP en Logs del Sistema (CA-8)
    Given que la API devuelve inintencionalmente la llave primaria secreta o contraseña de una transacción
    Then la política general de censura y DLP intercepta el contenido saliente hacia el log (`stdout`/Kibana/Pantalla11)
    And enmascara los atributos coincidentes (Hashes visuales ocultos `***`) bloqueando la exposición a un desarrollador o Arquitecto BPMN no autorizado.

  Scenario: Directorio Global Reutilizable de Contenedores (CA-9)
    Given un Arquitecto a punto de conectar la extracción del SharePoint en un proceso
    Then la UI de "Agregar Conector" en la Pantalla 6 ofrece primero un buscador sobre el "Directorio de Conectores Registrados" 
    And permite re-utilizar el conector genérico sin volver a ingresar claves ni endpoints globales, favoreciendo el reciclaje.

  Scenario: Componente Playground de Pruebas (CA-10)
    Given que el Arquitecto terminó el Mapping Drag and Drop
    Then dispone de un botón `[▶️ Run / Probar Conector]` en la misma Pantalla 11
    And puede inyectar variables estáticas Dummy obteniendo el Raw Response de SharePoint/CRM en ese instante para verificar funcionamiento antes de comitear al catálogo.

  Scenario: Parametrización Humana de Timeouts (CA-11)
    Given un conector que apunta a un ERP legacy lento
    Then la interfaz de Pantalla 11 expone un input para definir `Timeout (ms)` exacto por Conector
    And previene que un solo API sature el motor Camunda por quedarse en estado colgante esperando infinitamente.

  Scenario: Lista Negra de Seguridad de Red (SSRF Prevention) (CA-12)
    Given un Arquitecto intentando mapear un Endpoint hacia un host interno malicioso (Ej: `localhost`, `127.0.0.1` o IPs locales de la BD)
    Then el sistema de Validación de Guardrails de IT intercepta el guardado
    And rechaza conectores que apunten a dominios listados en la "Blacklist Confidencial" configurada por el equipo de ciberseguridad.

  Scenario: Parseo Inteligente de Fechas (CA-13)
    Given una respuesta de CRM en donde el campo `birth_date` llega formateado de forma exótica (`20241231`)
    When el usuario arrastra la variable al Drag & Drop
    Then el Hub ofrece una opción "Forzar Formateo de Fecha"
    And convierte automáticamente el valor al estándar corporativo ISO-8601 impidiendo errores de parseo en el motor BPMN más adelante.

  Scenario: Interfaz Multipart/Form-Data para Anexos Pesados (CA-14)
    Given la necesidad imperativa de recuperar y enviar PDFs al SharePoint
    Then el Hub de Integración no se limita únicamente al Content-Type `application/json`
    And provee soporte técnico transparente para subida y descarga asíncrona de Binary Large Objects (BLOBs) mediante `multipart/form-data`.

  Scenario: Agresivo Ahorro de Red por Caché en Memoria (CA-15)
    Given 50 procesos BPMN simultáneos pidiendo un catálogo inmutable (Ej: Catálogo de Sucursales ERP)
    Then el Arquitecto puede encender el "Switch de Caché" interactivo en el Conector
    And parametrizar un TTL (Ej: Valid for 10 min) para que el iBPMS responda instantáneamente desde RAM local evadiendo 49 llamadas de red innecesarias al ERP.

  Scenario: Despliegue Manual de Entornos V1 / Variables V2 (CA-16)
    Given la necesidad de apuntar conectores a Producción (Ej: de `crm-qa` a `crm-prod`)
    Then en el alcance de V1, el Arquitecto de integraciones debe actualizar las URLs manualmente
    # NOTA: Diferido a V2: Orquestación automática transversal mediante Variables de Entorno (`{{crm_base_url}}`).

  Scenario: Autonomía de Firmas Criptográficas de Payload (CA-17)
    Given una integración hacia una pasarela bancaria que exige firma HMAC-SHA256
    Then la configuración del Conector exhibe un panel de `Security Signatures`
    And permite auto-firmar ciegamente el payload adjuntando la rúbrica matemática en los Headers garantizando no-repudio técnico.

  Scenario: Traversado Nativo de Paginación Recursiva (CA-18)
    Given que el CRM expone un listado masivo en páginas pequeñas (offset/limit de a 100)
    Then el Conector es consciente de estructuras de paginación
    And permite configurar la navegación automática "NextPage" hasta obtener el dataset completo sin que el Arquitecto deba modelar un 'For Loop' grotesco en el BPMN.

  Scenario: Versionamiento Estricto No Destructivo (CA-19)
    Given un Arquitecto modificando el Conector "CRM Cliente v1" que está amarrado ya a 50 procesos vivos
    When agrega un campo obligatorio nuevo 
    Then el iBPMS prohíbe el Sobre-escritura instantánea (la cual rompería la empresa)
    And fuerza el guardado estricto como nueva reliquia inmutable "CRM Cliente v2", forzando una migración gradual proceso por proceso.

  Scenario: Refreshing Invisible de Identidades Temporales (CA-20)
    Given una conexión JWT/OAuth2 donde el token de acceso expira a los 60 minutos
    When un proceso se despierta a las 2 horas intentando conectar
    Then el Hub ejecuta internamente, sin intervención humana, el `refresh_token` contra el Identity Provider
    And obtiene un nuevo Token válido, emite la invocación y mantiene el flujo operativo limpio ininterrumpidamente.

  Scenario: Habilitación de Entradas Inbound (Webhooks) (CA-21)
    Given la necesidad de que el iBPMS sea un ente reactivo a sistemas externos (Ej: CRM actualiza un dato on-demand)
    Then la interfaz de Pantalla 11 expone la capacidad de generar dinámicamente "URLs de Webhooks Inbound"
    And asignando tokens generados nativamente para que sistemas externos llamen al iBPMS e interactúen con instancias de proceso activas (Signal Events / Message Events).

  Scenario: Transformación mediante Inyección de Código Custom (CA-22)
    Given una respuesta legacy en XML con estructuras irregulares inmanejables por el mapeo Drag & Drop simple
    Then el conector habilita opcionalmente un editor integrado "Code Injector" (JS/Python seguro - Sandboxed)
    And permitiendo al Arquitecto escribir scripts deterministas obligados a transformar el payload crudo hacia el JSON esperado por el Engine de forma manual.

  Scenario: Tercerización de Tráfico y Encolamiento (Throttling) (CA-23)
    Given picos de transaccionalidad donde 10,000 procesos invocan a un SharePoint externo simultáneamente
    Then el iBPMS delega la contención (Rate Limiting) a la Cola de Mensajería corporativa (Ej: RabbitMQ / Apache Kafka)
    And no asume internamente la gestión masiva de peticiones limitantes para evitar caídas de servidor.

  Scenario: Aislamiento por Seguridad Condicionada (CA-24 - Diferido a V2)
    Given la existencia de Conectores clasificados como "Altamente Confidenciales" (Saldos ERP)
    Then en el alcance actual de V1 todos los arquitectos autenticados en el BPM Designer tienen visibilidad transversal del catálogo de conectores
    # NOTA: Diferido a V2 la securización del catálogo de conectores por roles estrictos de RBAC.

  Scenario: Encriptación de Payload Militar (CA-25)
    Given el mandato de transferir payloads (Cuerpos HTTP) ultra-sensibles (Ej: Historias Clínicas)
    Then el Hub además de forzar TLS (HTTPS) en tránsito
    And posee soporte de auto-cifrado y descifrado nivel Payload utlizando criptografía asimétrica (PGP) garantizando impenetrabilidad absoluta incluso en reposo en logs intermedios de APIM del proveedor.
```
**Trazabilidad UX:** Wireframes Pantalla 11 (Integration Hub).

  Scenario: Privacidad de Auditoría Cognitiva (CA-29)
    Given que el proceso generó métricas de "Confidence Score" y "Chain of Thought"
    Then estas métricas son de consumo estrictamente interno
    And por ningún motivo se exponen al Ciudadano Externo en la Pantalla 18 (Portal B2B/B2C).

  Scenario: RAG Multimodal Controlado (V1) (CA-30)
    Given la ingesta de documentos anexos para contextualizar a la IA
    Then el Agente RAG soporta en su V1 la lectura de documentos PDF, DOCX e Imágenes (OCR integrado a la API de visión)
    And excluyendo formalmente notas de voz o video (Diferido a V2).

  Scenario: Eficiencia de Contexto Pre-Empaquetado (IA Amarrada) (CA-31)
    Given una tarea generativa que requiere datos externos (Ej: Saldos ERP)
    Then la IA tiene prohibido usar "Function Calling" autónomo para ir a buscar datos por su cuenta (Gasto excesivo de tokens y memoria de razonamiento)
    And la arquitectura dicta que Camunda Engine, mediante Service Tasks previas y baratas, extraiga la data y se la entregue pre-empaquetada en el Prompt a la IA para que esta se limite únicamente a redactar.
```
**Trazabilidad UX:** Pantalla 6 (BPMN Designer Palette), Pantalla 12 (SGDEA), Pantalla 7 (Form Builder UI).

---


## ÉPICA 10: Event Driven Architecture & Central Message Broker
Formaliza la infraestructura de encolamiento transversal del iBPMS asegurando que tareas de alta latencia o llamadas a sistemas masivos (Cognitive RAG, APIs externas) no saturen la base de datos relacional ni el pool de hilos de Camunda.

### US-034: Orquestación a través de RabbitMQ
**Como** Administrador de Infraestructura / Backend
**Quiero** delegar el rate-limiting y el encolamiento asíncrono a un Message Broker de grado Enterprise (RabbitMQ)
**Para** garantizar resiliencia extrema frente a picos transaccionales, evitando desbordamientos de memoria (OOM) y caídas de subsistemas.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Central Message Queue Orchestration
  Scenario: Broker Exclusivo de Alta Demanda (CA-1)
    Given la necesidad de procesar transacciones asíncronas pesadas (IA, Mails, Integraciones)
    Then el iBPMS enruta estos eventos imperativamente a `RabbitMQ` (o Kafka) configurado como clúster
    And prohíbe explícitamente el uso de tablas relacionales (SQL) como mecanismo de encolamiento de alto tráfico para prevenir bloqueos de base de datos (Database Deadlocks).

  Scenario: Dashboard Técnico de DLQ (Monitor Visual) (CA-2)
    Given un fallo masivo en un proveedor externo que atasca 5,000 mensajes en la cola de errores
    Then el iBPMS provee una pantalla de monitoreo transversal para el Rol de Administrador IT
    And permite visualizar el tamaño de la 'Dead Letter Queue' (DLQ)
    And expone botones críticos de acción masiva: `[Purgar Cola]` y `[Reintentar Mensajes Forzosamente]`.

  Scenario: Jerarquización de Supervivencia (Priority Queues) (CA-3)
    Given una saturación temporal de procesamiento en los Workers del sistema
    When ingresan simultáneamente eventos VIP (Ej: Notificaciones de aprobaciones financieras críticas) y eventos de latencia tolerable (Ej: Generación RAG de resúmenes)
    Then RabbitMQ clasifica el tráfico en "Priority Queues" pre-configuradas basándose en metadatos del evento
    And asegura que los procesos de Nivel 1 (Críticos) sean desencolados y procesados antes que las tareas de Nivel 3 (Batch), garantizando el SLA de negocio intacto a pesar del cuello de botella global.
```
**Trazabilidad UX:** Operación Backend e Infraestructura (Dead Letter Queue IT Dashboard).

---

## ÉPICA 11: Bóveda Documental SGDEA (Pantalla 12)
Define la arquitectura de almacenamiento, inmutabilidad y recuperación de documentos (Expedientes y Anexos), integrándose nativamente con SharePoint como gestor documental primario en V1, sentando las bases para Tablas de Retención (TRD) y firmas digitales.

### US-035: Integración SharePoint y Auditoría Documental
**Como** Analista / Auditor de Cumplimiento
**Quiero** que el iBPMS gestione los expedientes en SharePoint manteniendo trazabilidad matemática estricta
**Para** garantizar que la evidencia aportada por clientes o generada por IA sea inmutable, centralizada y legalmente auditable.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: SharePoint Vault and Single Source of Truth
  Scenario: Almacenamiento Delegado Basado en Enlaces (CA-1)
    Given la subida de un documento de 10MB en la Pantalla 16 (Intake)
    Then la arquitectura postula a SharePoint como "Single Source of Truth" físico de los PDFs
    And el iBPMS únicamente almacena en su base de datos relacional la URL directa del activo, su ID referencial y la Metadata de auditoría, evadiendo duplicación de costos de Storage (S3).

  Scenario: Creación Dinámica de Taxonomía Sub-Carpetas (CA-2)
    Given un Arquitecto modelando una captura documental en la Pantalla 6
    When configura la actividad paramétricamente para generar "Casos Independientes"
    Then el iBPMS expone un Pop-Up para definir la ruta base en SharePoint
    And en tiempo de ejecución, el motor invoca el API de SharePoint creando proactivamente la sub-carpeta unívoca para ese expediente (Ej: `/ProcesoA/Caso1234/`) antes de inyectar los documentos.

  Scenario: Elusión de Seguridad Perimetral SharePoint (Service Account) (CA-3)
    Given un Usuario de Negocio que posee Rol de Lectura en el iBPMS pero carece de licencia SharePoint
    Then el módulo documental utiliza un App Registration (Súper Cuenta de Servicios - EntraID) para extraer el PDF del repositorio
    And lo proyecta en la Pantalla 12 evadiendo los bloqueos nativos de SharePoint frente al usuario final.
    # NOTA: Diferido a V2 el "RBAC Cruzado" (User Delegation OAuth2).
```
**Trazabilidad UX:** Wireframes Pantalla 12 (Bóveda Documental SGDEA Central).
```gherkin
  Scenario: Marcado Metadato para Tablas de Retención V1 (CA-4)
    Given la necesidad legal de destruir tutelas tras 5 años (TRD)
    Then en la V1, el iBPMS inyecta una Fecha de Expiración como Metadato estructurado directo a la taxonomía de SharePoint
    And delega la incineración automatizada (Deletion Policies) al motor nativo de Microsoft 365.
    # NOTA: Diferido a V1.2 el cronómetro destructor interno propio del iBPMS.

  Scenario: Inmutabilidad por Versionamiento Incremental (CA-5)
    Given un analista intentando "Reemplazar" un contrato que quedó mal redactado en el sistema
    Then en el expediente de la Pantalla 12 el botón de sobre-escritura destructiva está censurado
    And forcejea la obligatoriedad funcional de subir el nuevo archivo bajo el mecanismo de "Nueva Versión" (v1.1) reteniendo acceso forense e inmutable al borrador v1.0.

  Scenario: Despacho de Integración E-Signature (CA-6)
    Given un documento generado que requiere validez legal del firmante
    Then el Módulo Documental posee el andamiaje (Hooks) para interactuar vía API con proveedores de Firma Digital (Ej: DocuSign/AdobeSign)
    And actualiza el estado del expediente en la Pantalla 12 a "Firmado" una vez los Webhooks Inbound confirman el OTP legal del ciudadano.

  Scenario: Componente Visor Empotrado (Iframe Preview) (CA-7)
    Given la necesidad de leer un anexo para tomar una decisión en un proceso
    Then la interfaz de Tareas (Pantalla 12 empotrada en Workdesk) renderiza un Visor de Documentos Nativos asíncrono
    And impide obligar al analista a descargar el PDF ciegamente hacia las carpetas `Descargas/` locales de su Sistema Operativo, reteniendo el foco en el flujo iBPMS.

  Scenario: Blindaje Criptográfico Anti-Fraude (SHA-256) (CA-8)
    Given la delegación del archivo físico hacia el servidor SharePoint de TI (CA-1)
    Then en el milisegundo anterior a la carga, el iBPMS calcula el HASH criptográfico SHA-256 del binario original
    And sella esta huella matemática inmutablemente en la Base de Datos transaccional del iBPMS para detectar futuras y silenciosas alteraciones directamente en SharePoint.

  Scenario: Lector Óptico Diferido (OCR Zonal) (CA-9)
    Given imágenes de documentos de identidad (Cédulas) escaneadas
    Then en V1 estas se gestionan como Binary/Image objects convencionales
    # NOTA: Diferido a V2 el procesamiento neuronal OCR para extracción estructurada de texto zonal.

  Scenario: Inyección Activa de Metadata de Negocio (CA-10)
    Given el traspaso exitoso del documento PDF hacia la granja SharePoint
    Then el iBPMS adjunta un Payload extendido de Propiedades Personalizadas (Ej: `ibpms_processName`, `ibpms_caseStatus`) al nodo del documento
    And permitiendo a los usuarios externos buscar documentos utilizando las herramientas de búsqueda Nativas de O365 mediante filtros semánticos del negocio.

  Scenario: Límite Infraestructural de Carga y Silencio Parcial (CA-11)
    Given un usuario intentando subir un archivo estúpidamente pesado (Ej: Video 4K de 5GB)
    Then la Pantalla 12 intercepta la carga en el Fronend guiada por un parámetro global `MAX_FILE_SIZE` (Ej: 50MB) configurado por IT
    And emite un error de UI "genérico" o "silencioso" al usuario final (Ej: "Error en la Carga, archivo muy pesado")
    And simultáneamente dispara una alerta técnica detallada en el Log del Administrador del Sistema para auditoría de abusos.

  Scenario: Lista Blanca Estricta de Extensiones (MIME Types) (CA-12)
    Given el riesgo inminente de inyección de Malware (Ej: `.exe`, `.bat`)
    Then la Bóveda SGDEA opera exclusivamente bajo arquitectura de "Lista Blanca" (Whitelist)
    And rechaza radicalmente cualquier archivo que no esté explícitamente parametrizado (Ej: `application/pdf`, `image/jpeg`, `application/msword`).

  Scenario: Visibilidad Transparente de Atributos de Auditoría (CA-13)
    Given la Tabla/Grilla visual del expediente en Pantalla 12
    Then el diseño UI no esconde la data legal
    And expone nativamente en columnas primarias la "Fecha Extrema (Vencimiento TRD)" y el "Hash SHA-256" para que el analista confirme la inmutabilidad física con un solo vistazo.
    # NOTA: Opciones de "Botón del Pánico/Censura de archivos erróneos" diferido a V2.

  Scenario: Consolidación Multi-Anexo (PDF Merge Tool) (CA-14)
    Given un expediente con 10 archivos PDF fragmentados que deben enviarse a una Superintendencia
    Then la Pantalla 12 posee un botón de acción masiva `[Combinar PDFs (Merge)]`
    And el iBPMS compila transitoriamente las páginas de los archivos seleccionados en un único documento maestro PDF sin corromper los originales.

  Scenario: Delegación de Escaneo Anti-Malware (CA-15)
    Given el ingreso de nuevos documentos a la Bóveda
    Then la arquitectura V1 asume ciegamente la robustez de los Defensores Nativos de Microsoft
    And confía en que SharePoint 365 interceptará virus silenciosamente, librando al iBPMS de conectar con AntiVirus dedicados en este MVP.
    # NOTA: Herramientas de "Anotación Gráfica (Highlighting)" sobre PDFs diferidas a V1.2.

  Scenario: Búsqueda Semántica Delegada (Full-Text Search) (CA-16)
    Given un analista utilizando el buscador global del iBPMS para buscar la palabra "Tornillo"
    Then el iBPMS delega la consulta profunda al motor nativo de indexación de SharePoint
    And este último busca el término *dentro del contenido textual* de los PDFs y devuelve los matches, inyectando los resultados en la UI del iBPMS.

  Scenario: Orquestación SGDEA a Inteligencia RAG (Vectorización Segura) (CA-17)
    Given un analista en Pantalla 12 que decide oprimir `[Usar para IA]` sobre un contrato de 100 páginas
    Then el módulo documental envía asíncronamente el ID de ese archivo a la "Cola de Eventos IA (RabbitMQ - CA-34)"
    And el cerebro LLM procede a desencolar y devorar el contenido (si es PDF o WORD habilitado) para poblar su memoria de Embeddings sin congelar la ventana del usuario.
```
**Trazabilidad UX:** Wireframes Pantallas 12, 16 y 6.

---

## ÉPICA 12: Identity Governance & Control de Acceso (Pantalla 14)
Define la arquitectura centralizada de seguridad, delegación y segregación de datos. Establece a la Pantalla 14 como el único "Cuartel General" donde el Súper Administrador orquesta qué Humanos, APIs y Roles pueden interactuar con los procesos modelados en el iBPMS.

### US-036: Matriz de Control de Acceso Basado en Roles (RBAC)
**Como** Oficial de Seguridad de la Información (CISO) / Super Admin
**Quiero** gestionar centralizadamente todos los permisos, perfiles y delegaciones en la Pantalla 14
**Para** garantizar cumplimiento ISO 27001, prevenir accesos no autorizados a datos sensibles y auditar la segregación de funciones (SoD).

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Identity Governance & RBAC Architecture
  Scenario: Hibridación de Roles EntraID vs Locales (CA-1)
    Given una organización iniciando el iBPMS
    Then la Pantalla 14 soporta doble motor de mapeo de identidades
    And permite importar automáticamente Roles/Grupos desde Microsoft EntraID (SSO) 
    And provee un "Fallback" interno para crear y asignar Roles 100% locales en la BD del iBPMS si el cliente tiene baja madurez corporativa.

  Scenario: El Guardián Absoluto (Root Super Admin) (CA-2)
    Given el despliegue inicial (Día Cero) del iBPMS
    Then el sistema inyecta por defecto un único usuario `[Super_Administrador]` inborrable a nivel de base de datos
    And este rol es el único con potestad absoluta para ingresar a la Pantalla 14 y delegar poder (crear otros administradores).

  Scenario: Clonación de Perfiles por Plantilla (CA-3)
    Given la necesidad de dar el mismo set de 15 permisos a 50 asesores nuevos
    Then la Pantalla 14 permite la creación de un `[Rol Plantilla]` lógico que atrapa esos permisos
    And permite asignar ese `[Rol Plantilla]` en bloque a los 50 usuarios con un solo click (Mass Assignment).

  Scenario: Segregación Iniciador vs Ejecutor (CA-4)
    Given la matriz de permisos de la Pantalla 14
    Then expone casillas de verificación (Checkboxes) granulares y explícitas para cada Proceso BPMN publicado
    And diferencia a nivel de base de datos el permiso booleano `can_initiate_process` (Cliente) vs `can_execute_tasks` (Cocinero).

  Scenario: Privacidad Visual de Colas (Data Segregation Local) (CA-5)
    Given dos analistas (Juan y María) pertenecientes al mismo Rol "Analista_Créditos"
    Then al ingresar a su Workdesk (Pantalla 5), la arquitectura forza un filtro de base de datos a nivel de registro (Row-Level Security)
    And garantiza que María SOLO visualice los folios/casos asignados a ella, ocultando tajantemente el trabajo de sus pares a menos que sea una "Cola Compartida Pública".

  Scenario: Herencia de Roles Piramidal (CA-6)
    Given una estructura jerárquica corporativa
    Then la Pantalla 14 permite que el rol `[Gerente_Riesgo]` sea configurado para "Heredar" atómicamente el 100% de los permisos subyacentes del rol `[Analista_Riesgo]` minimizando la redundancia de clics en la matriz.

  Scenario: Inmutabilidad por Desactivación Suave (Soft-Delete) (CA-7)
    Given que el empleado Juan renuncia a la empresa y es desconectado del SSO
    Then el Súper Admin en Pantalla 14 NO puede borrar físicamente (DELETE SQL) la identidad de Juan 
    And el sistema le asigna un sello de `[Usuario Inactivo]`, congelando su estado pero preservando eternamente su nombre en los registros de auditoría de los casos que resolvió en el pasado.

  Scenario: Aprovisionamiento de Transeúntes (Ciudadano Interno) (CA-8)
    Given un empleado recién contratado que se loguea en el iBPMS vía SSO por primera vez
    Then el motor RBAC le auto-provisiona un perfil inofensivo por defecto llamado `[Ciudadano_Interno]`
    And este perfil arranca con capacidades nulas hasta que el Súper Admin configure explícitamente en Pantalla 14 qué Procesos Generales (Ej: Vacaciones) tienen permitida "Autogestión por defecto".

  Scenario: Módulo de Delegación Autónoma Temporal (CA-9)
    Given un Gerente que se marcha a vacaciones por 15 días
    Then la Pantalla 14 le provee un panel de Autogestión (Delegación) para cedar sus poderes a un suplente (Ej: Su asistente)
    And esta cesión de Rol requiere obligatoriamente estampar el `[Rango_de_Fechas]` (Fecha Inicio / Fin) para revocarse automáticamente.
    And toda la transacción de traspaso de poder queda flaggeada transaccionalmente para la bitácora del CISO.

  Scenario: Creación de Robots de Integración (API Keys / Service Accounts) (CA-10)
    Given la necesidad de que el ERP corporativo lance casos en el iBPMS 24/7 sin interacción humana
    Then la Pantalla 14 posee un módulo paralelo de "Cuentas de Servicio M2M"
    And permite generar Tokens Criptográficos (API Keys) atándolos a Roles específicos, prohibiendo que los sistemas externos operen con credenciales de humanos vulnerables.
    
  Scenario: Respeto ciego al Autenticador Perimetral (EntraID MFA) (CA-11)
    Given una tarea crítica que un gerente va a ejecutar en su bandeja
    Then la arquitectura de seguridad V1 asume 100% de confianza en el Token emitido por Microsoft EntraID
    And el iBPMS NO reconstruye un componente duplicado de Doble Factor (MFA) propio en pantalla, delegando esta validación criptográfica al Identity Provider original.

  Scenario: Exclusión de Ocultamiento de Campos (Scope Limit) (CA-12)
    Given un usuario intentando ocultar la columna "Salario" de un formulario en base al rol
    Then la directriz aclara que la Pantalla 14 administra accesos a la "Instancia Completa" (El Formulario entero)
    And delega la responsabilidad técnica de ocultar campos individuales a la algoritmia del Pro-Code Builder (Pantalla 7) durante el diseño del Vue Component.
```
**Trazabilidad UX:** Wireframes Pantalla 14 (Identity & Role Governance).
```gherkin
  Scenario: Desacoplamiento de Roles Estáticos vs Dinámicos (BPMN Lanes) (CA-13)
    Given la asignación de trabajo en el motor Camunda
    Then el módulo de Permisos reconoce y respeta dos vías de asignación: 
    Los Roles Estáticos (Asignados en la Pantalla 14 manualmente de por vida al usuario) y los Roles Dinámicos/Variables (Inyectados en tiempo real por el BPMN a través de *Expression Lanes*).

  Scenario: El Botón Táctico de Exorcismo (Kill-Session) (CA-14)
    Given un evento de despido disciplinario hostil a mediodía
    Then la Pantalla 14 expone un botón rojo `[Revocar Todo y Matar Sesión]` en la ficha del empleado
    And la arquitectura exige que el Backend destruya activamente los JWT almacenados en caché/Redis de ese analista forzando su deslogueo TCP instantáneo, sin tener que esperar que su Token de 1 hora expire.

  Scenario: Bypass Anónimo de Procesos (URLs Públicas) (CA-15)
    Given la necesidad ciudadana de radicar PQRS sin crear cuentas
    Then la Pantalla 14 (y la Configuración del Proceso) cuenta con un Switch Especial `[Permitir Trámite Público]`
    And al activarse, el generador de Formularios expone un endpoint/URL sin requerimiento de Bearer Token en las rutas de Vue Router, posibilitando el inicio ciego de instancias BPMN por navegadores anónimos en internet.

  Scenario: Informes Densos de Fiscalización (Auditoría CISO) (CA-16)
    Given la temporada de Auditoría ISO 27001
    Then la Pantalla 14 permite generar el reporte matrizal de *Identity Governance*
    And compila una sábana CSV / Excel descargable cruzando `[Todos los Usuarios/Robots]` contra `[Todos los Roles Activos]` y `[Todos los Procesos Iniciables/Ejecutables]`.

  Scenario: Traza Indeleble de Otorgamiento (CA-17)
    Given un practicante que misteriosamente amanece con el rol `[Gerente_Financiero]`
    Then la Pestaña de Auditoría de Seguridad de Pantalla 14 muestra el JSON delta exacto
    And expone qué Administrador Ejecutante (ID Humano), en qué milisegundo UTC (Timestamp), inyectó la sobre-escritura de permisos.

  Scenario: Omisión Estricta de Segregación de Funciones Automática (SoD) (CA-18)
    Given la ley del "Quien hace no aprueba"
    Then para el MVP V1, el motor iBPMS NO frena estructuralmente a un humano si el BPMN le enruta "Crear Cheque" y "Aprobar Cheque" al mismo tiempo
    And asume este riesgo operativo difiriendo los motores complejos de "Conflict of Interest Avoidance" a V2, confiando en que el diseño del proceso en Pantalla 6 asigne humanos distintos para el flujo iterativo.
```
**Trazabilidad UX:** Wireframes Pantallas 14, 6, 7 y Workdesk (5).

---

## ÉPICA 13: Configuración Administrativa de Buzones SAC (Pantalla 15)
Cubre la brecha arquitectónica de la gestión de conexiones de los orígenes de datos (Correos Electrónicos). Permite al Súper Administrador registrar físicamente las cuentas de "Atención al Cliente" para que el iBPMS pueda succionar los reclamos y aplicar la IA.

### US-037: CRUD de Conexiones de Buzones (Intake API)
**Como** Súper Administrador del Sistema
**Quiero** registrar y administrar libremente las cuentas de correo corporativo conectadas al iBPMS
**Para** definir de dónde el motor saca la información, qué protocolo usar, con qué frecuencia y a qué proceso BPMN enruta por defecto cuando la Inteligencia Artificial (Agente 3) no logra deducirlo.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Configuración de Orígenes SAC (Mailbox CRUD)
  Scenario: Soporte Multi-Protocolo de Conexión (Arch. Abierta) (CA-1)
    Given la ventana de registro de un nuevo Buzón SAC
    Then el Administrador tiene la opción de elegir el tipo de Conector
    And soporta autenticación moderna (OAuth 2.0 / MS Graph API) para ecosistemas Microsoft
    And soporta simultáneamente configuración legacy (IMAP / SMTP) con usuario y contraseña genéricos (Ej: Gmail, cPanel) para una arquitectura V1 abierta.

  Scenario: Centralización del Poder Organizacional (CA-2)
    Given el formulario de gestión de cuentas (Pantalla 15)
    Then este módulo está fuertemente bloqueado y pertenece exclusivamente al Súper Administrador
    And un "Líder de Área SAC" NO puede agregar un correo nuevo de forma autónoma, forzando un esquema de gobierno centralizado por IT.

  Scenario: Trazabilidad de Fallo (BPMN Default Rule) (CA-3)
    Given un correo altamente ininteligible donde el Agente MLOps (Agente 3) falla en deducir su categoría
    Then la configuración del buzón cuenta con un campo obligatorio: `[Proceso BPMN de Caída por Defecto]`
    And el iBPMS enrutará ciegamente este correo hacia ese proceso genérico pre-seleccionado (Ej: "Trámite de Reclamo Manual") para no dejar correos "en el limbo".

  Scenario: Sincronización Programada (Polling) y Manual (CA-4)
    Given la infraestructura de recolección de correos
    Then el sistema utiliza un Job de Polling configurado bajo mejores prácticas (Ej: cada 5 minutos) para evitar ahogar al servidor
    And expone adicionalmente un botón táctico `[🔄 Sincronizar Buzón Ahora]` en el Frontend para que el Administrador fuerce la lectura a demanda inmediata.

  Scenario: Ping de Conexión en Vivo Obligatorio (CA-5)
    Given el administrador registrando credenciales de MS Graph (OAuth)
    When oprime el botón de Guardar
    Then el iBPMS pausa el registro y dispara un ping transaccional en caliente contra el tenant de Microsoft
    And solo permite crear formalmente el Origen de Datos si Microsoft responde con un token 200 OK, abortando el proceso si las credenciales fallan.

  Scenario: Réplica Operativa iBPMS vs Exchange (No Destructiva) (CA-6)
    Given el proceso de "chupar" correos (Ingesta)
    Then el iBPMS NUNCA ejecuta comandos de `DELETE` físico contra el Exchange de origen por el simple hecho de leerlos
    And genera un folio replicado en la base de datos propia. Si un Súper Admin decide borrar (Hard-Delete) el caso en el iBPMS, el motor envía una instrucción de *Soft-Delete* hacia Microsoft (Mover a Papelera / Archivo) manteniendo la paridad.

  Scenario: Gobernalización Central del Blacklist en V1 (CA-7)
    Given la necesidad de bloquear SPAM o dominios maliciosos
    Then en el MVP (V1) la Pantalla 15 NO reconstruye formularios de Blacklist/Whitelist
    And delega el filtrado anti-spam 100% a las políticas perimetrales nativas configuradas por IT en Microsoft Exchange. (Reglas bidireccionales por API diferidas a V2).

  Scenario: Silenciador de Emergencia Táctil (CA-8)
    Given un ataque de SPAM o falla lógica en el enrutamiento de un Buzón
    Then la grilla del CRUD expone un Toggle Switch `[En Vivo / Pausado]` de desconexión inmediata
    And permite suspender temporalmente el Job de Polling para ese buzón en específico sin borrar permanentemente el registro ni sus tokens almacenados.

  Scenario: Excepción de Límites de Carga por Dominio (CA-9)
    Given que el límite global de archivos adjuntos del iBPMS es de 50MB
    Then el formulario del Buzón permite configurar un `Override`
    And otorga la capacidad de definir un límite en Megabytes customizado exclusivo para los correos succionados por esa cuenta en particular (Ej: 100MB para `planos@`).

  Scenario: Auditoría de Caducidad de Tokens M2M (CA-10)
    Given que los Secretos de Cliente OAuth en Entra ID caducan cada 6 meses
    Then la Pantalla 15 debe calcular el tiempo de vida de la conexión
    And si las credenciales fallan, el iBPMS inyecta una alerta en el Log de Auditoría y envía una notificación estructurada a los Administradores advirtiendo la desconexión del SAC.
```
**Trazabilidad UX:** Pantalla 15 (Configuraciones Genéricas / Logs).

---

## ÉPICA 14: Configuraciones Globales de Nivel de Servicio (SLA)
Permite a la PMO establecer las reglas del juego a nivel corporativo paramétricas (Matriz de días hábiles, umbrales de vencimiento).

### US-043: Configuración Global de Service Level Agreements (SLA)
**Como** PMO / Administrador Estratégico
**Quiero** disponer de una pantalla matriz de configuración central
**Para** que el motor de orquestación y el BAM no cuenten domingos o feriados en horas inhábiles ajustando la métrica a las "Horas reales corporativas".

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Business SLA Matrix Configuration
  Scenario: Definición Efectiva de Calendario Laboral (Horas Hábiles)
    Given el administrador accede a la sección de Configuración de SLAs o Matriz de Negocio
    When se habilitan los Días Hábiles forzosamente basados en Horas (Ej: Lunes a Viernes de 8:00 a 17:00) y opcionalmente un listado de Días Feriados
    Then el motor de temporizadores (Ticking Engine) debe calcular y pausar el contador de "Due Dates" respetando las horas inactivas y de descanso.
    And posee un toggle de parametrización para dictaminar si este cambio en la matriz recalculará retroactivamente las instancias actualmente vivas, o solo a las nuevas (hacia adelante).

  Scenario: Automatización de Festivos Externos
    Given la necesidad corporativa de bloquear días de asueto local
    Then la matriz integra una API Pública para consumir automáticamente el catálogo de Feriados del país del Tenant
    And si dicha API falla, hace fallback al calendario manual editado en UI por el PMO.
    
  Scenario: Alertas Preventivas de Quiebre de Nivel
    Given que la línea del tiempo matemática (Ticking) se aproxima al 100%
    Then el motor SLA envía alertas tempranas garantizando un tiempo buffer para el solucionador (Prevención).
```
**Trazabilidad UX:** Wireframes Pantalla 19 (Configuración SLA).

---

## Módulo: Developer Portal & Extensibilidad (Zero-Trust)

### US-042: DevPortal: Generación Segura de API Keys y Extensibilidad

**Como** Arquitecto de Software / Desarrollador Integrador
**Quiero** acceder a un Portal de Desarrolladores (Pantalla 13) para crear API Keys y registrar Módulos Externos
**Para** poder construir integraciones y "Súper Apps" externas que interactúen con el iBPMS sin comprometer el Performance ni la Seguridad del Core.

**Contexto de Diseño:**
El DevPortal habilitará el ecosistema "Componible" (V2-Ready). Dado que intervienen humanos creando integraciones, se requiere blindar la red aplicando límites perimetrales directamente asociados al registro de la llave. La arquitectura **no puede ser vulnerada** bajo ninguna circunstancia, garantizando que el desarrollador humano opere exclusivamente dentro de los carriles definidos por el Arquitecto.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Zero-Trust Developer Portal Security
  Scenario: Autodestrucción del Secreto (OWASP)
    Given que el desarrollador requiere un Service Principal (API Key) para su módulo externo
    When el sistema le revela el "Client Secret" en texto plano
    Then el sistema otorga un máximo de 3 oportunidades (intentos de visualización/copiado)
    And al agotar el tercer intento, el secreto se oculta permanentemente y "autodestruye" visualmente, obligando a generar uno nuevo si se perdió.

  Scenario: Aislamiento por Cliente (Row-Level Tenancy)
    Given un Módulo Externo autenticado
    When envía peticiones de consulta (GET) o mutación (POST)
    Then la arquitectura forza a nivel de Base de Datos que SÓLO pueda interactuar con la data y expedientes pertenecientes al Cliente que pagó y autorizó dicho Módulo.
    And tiene prohibición estructural de realizar Borrados Físicos (DELETE) en instacias Core de clientes.

  Scenario: Ceguera Intencional y Sub-scopes restrictivos
    Given una API Key generada
    Then su token JWT debe nacer "capado" con un Sub-Scope limitante (Ej: `App_Read_Only`)
    And garantizando que el módulo pueda listar o leer tareas para su procesamiento, pero matemáticamente el backend rechace cualquier intento de "Edición" (Ceguera Operativa forzada).

  Scenario: Prevención Anti-DDoS y Radar de Tráfico
    Given un módulo de terceros volviéndose errático y enviando ráfagas masivas
    Then el Azure APIM Gateway (o Kong local) activa un "Radar de Control" con Rate-Limiting estructurado
    And retorna HTTP 429 cortando la comunicación en el perímetro, protegiendo a la Base de Datos y al motor Camunda.

  Scenario: Cuarentena de Nuevos Módulos (Sandbox Inyectado)
    Given un Módulo Externo recién registrado en el DevPortal
    Then por defecto nace en estado `Quarantine` apuntando a las bases de datos `Sandbox/Mirror`
    And no puede interactuar con el entorno productivo real del iBPMS hasta que el Administrador Global certifique su comportamiento.

  Scenario: Revocación por Reporte Humano
    Given una sospecha de brecha de seguridad en un módulo externo
    When un administrador humano procesa el reporte y oprime `[Revocar Llave]` en la Pantalla 13
    Then el Token JWT principal del módulo y todos los de refresco caen de inmediato, generando un proceso de desconexión forzosa del entorno.

  Scenario: Fechas de Caducidad y Alertas Administrativas
    Given que todas las "Llaves de Sistema" nacen con un Time-to-Live (TTL) finito (Fecha de expiración)
    Then semanas antes del vencimiento, el sistema dispara automáticamente alertas tempranas hacia el correo del Administrador para su gestión oportuna, advirtiendo del inminente apagón del módulo.

  Scenario: Alertas Activas contra "Curiosidad Maliciosa"
    Given que el token de un módulo intenta ejecutar un Endpoint o tocar una carpeta / archivo fuera de su Scope pre-aprobado (HTTP 403 Forbidden)
    Then el iBPMS bloquea la petición
    And dispara inmediatamente una notificación/alerta en tiempo real al correo del Oficial de Seguridad detallando el intento de intrusión.

  Scenario: Trazabilidad Extrema (La Culpa Compartida)
    Given un Módulo Externo realizando acciones permitidas (Ej. Aprobando un caso)
    Then el Audit Ledger del sistema guarda el log asociando el autor indudablemente a `[App_De_Tercero: CRM_Bot]`, proveyendo evidencia legal irrefutable de que fue la máquina del proveedor quien manipuló los datos y no un humano de nuestra plantilla.

  Scenario: Sandboxing Frontend (Aislamiento de Módulos Custom)
    Given que el equipo ha desarrollado un "Súper Módulo" con una UI exótica en React o Angular
    When este módulo se despliega dentro del ecosistema iBPMS (V1)
    Then el iBPMS cargará dicha UI de forma dinámica utilizando Iframes aislados (`sandbox`)
    And cualquier comunicación dinámica entre el Core (Vue 3) y el Iframe externo se realizará de manera controlada usando `window.postMessage()`, garantizando cero colisiones en el DOM, CSS Global o memoria (Pinia).

  Scenario: Tokens OIDC con Audiencia Específica (Extensibility Scope)
    Given un "Súper Módulo" registrado en el DevPortal
    When el Módulo obtiene sus credenciales OIDC contra Entra ID
    Then el JWT generado poseerá internamente Claims distintivos de extensión (Ej: `aud: ibpms.extensibility.supermodules`)
    And el SecurityFilterChain (Spring Boot) del Core leerá esta audiencia y bifurcará explícitamente los permisos, denegando el acceso a APIs puras de administrador humano.

  Scenario: Obediencia al Hexágono y Prohibición de Bypass JPA
    Given un Agente de Desarrollo o Humano codificando el Backend funcional de un "Súper Módulo"
    When intente persistir un nuevo dato asociado al caso o leer una variables
    Then la arquitectura le prohíbe técnicamente usar Interfaces `JpaRepository` o conectarse por JDBC a la instancia maestra de MySQL del Core
    And está obligado orgánicamente a instanciar un WebClient/RestTemplate para consumir los "Driving Adapters" (APIs REST Transaccionales en `/api/v1/`) como si fuera un sistema completamente alienígena de internet (Arquitectura Hexagonal Estricta).
```
**Trazabilidad UX:** Pantalla 13 (DevPortal).

---

## ÉPICA 15: Parametrización Global y Límites del Sistema (Settings)
*(MUST HAVE)* - El "Cockpit" centralizado para Súper Administradores, donde se gobiernan los umbrales cognitivos, interruptores y caducidades arquitectónicas de todo el iBPMS en tiempo de ejecución, sin necesidad de despliegues de código o edición en la base de datos directa.

### US-044: Gobernanza de Inteligencia Artificial (AI Limits)
**Como** Súper Administrador
**Quiero** una pestaña de configuraciones dedicada al Motor Cognitivo
**Para** decidir empíricamente el grado de libertad, permisividad y "Tolerance Score" que se le otorga a los LLMs antes de declarar ineficacia.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Centro de Gobernanza IA (Global Threshold Configurator)

  Scenario: Feature Toggle del "Auto-Pilot" de Embudos (US-040)
    Given el panel de administración
    Then debe existir un Master Switch de `[Permitir Instanciación Autónoma AI (Zero-Touch)]`
    And si este interruptor está apagado, por más que la IA tenga 100% de certeza, todas las Action Cards se retendrán forzosamente en el Embudo de la Pantalla 16. Mantenimiento del Principio "Human-in-the-Loop Override".

  Scenario: Auditoría de Transparencia y Combate al Sobre-Ajuste (Audit Matrix)
    Given el proceso nocturno del Agente Data Scientist (US-015)
    Then el Administrador posee una pantalla "AI Audit Log" donde puede ver exactamente qué palabras, vectores o estilos aprendió a asociar la IA.
    And esta lista debe ser legible por humanos (Ej: `[Aprendizaje 1: La palabra 'Urgente' ahora levanta flag de prioridad Alta]`).
    And para prevenir el "Sobre-Ajuste" (Overfitting) de problemas antiguos, el Administrador puede seleccionar cualquier "Aprendizaje/Patrón" de esta matriz y oprimir el botón `[Eliminar Patrón]`, forzando a la IA a desaprender esa asociación obsoleta inmediatamente.

  Scenario: Rollback de Modelo MLOps vía Blue-Green SQL Swapping (NFR)
    Given una degradación de la calidad de respuesta de la IA (Ej: Alucinaciones reportadas a las 8:00 AM) y el uso de `pgvector` en IaaS sin versionamiento Git de registros
    When el Administrador presione el botón crítico `[Revertir Modelo Anterior]` en la P26.B
    Then el Backend ejecutará un "Blue-Green Data Swapping" a nivel de SQL
    And los vectores entrenados en la noche anterior (los cuales nacieron inactivos con `is_active_model = FALSE` y etiqueta temporal Ej. V.2026.03, pasando a TRUE tras finalizar el batch) se marcarán como `FALSE`
    And la bandera del modelo previo volverá a `TRUE` en 1 milisegundo, revirtiendo el cerebro del Agente (RAG/Embeddings) de forma instantánea.

  Scenario: Frecuencia de Ejecución del Agente Data Scientist
    Given la necesidad operativa de ahorrar cómputo y gestionar el umbral de evolución
    Then el administrador puede configurar la Frecuencia Cron del reentrenamiento (Ej: `Diario`, `Semanal`, `Mensual`) y la hora exacta (Ej: `02:00 AM`).
    And cuenta con una Política de Reintentos automáticos (Retry Queue) en caso de que el Job falle por Timeouts en la base de datos vectorial durante la madrugada.

  Scenario: Lista Negra Corporativa Anti-Adivinación (US-013)
    Given la necesidad de prevenir emparejamientos indeseados (Ej: `@gmail.com`)
    Then la interfaz exhibe un componente de tipo "Chip Input"
    And permite al administrador agregar, listar y eliminar cadenas de dominios públicos bajo la directriz "Ignorar Match por Dominio". Al inyectarlos, se escriben asíncronamente en la BD (`ibpms_public_domains_blacklist`).
```
**Trazabilidad UX:** Nueva pestaña en Pantalla 15.A (Configuración Global / Súper Administrador).

---

### US-045: Restricciones de Dominio Ágil y Documental (System Limits)
**Como** PMO / Arquitecto del Sistema
**Quiero** configurar techos duros (Hard Limits) numéricos a las funcionalidades operativas
**Para** evitar la degradación de Base de Datos y prevenir malas prácticas gerenciales (como Tableros Ágiles infinitos).

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Governing Agile Entropy and Storage Economics
  Scenario: Barrera de Densidad Kanban (US-008)
    Given la configuración de Restricciones UI
    Then el Administrador debe disponer de un control numérico `Kanban_Max_Columns`
    And no permitirá exceder el número pactado (Ej: 7) para evitar tableros ágiles inmanejables a nivel de scroll horizontal y UX. Cualquier intento de un Scrum Master para añadir una columna #8 fallará.
    
  Scenario: Ventana de Gracia Transaccional (Deshacer Intakes - US-024)
    Given la necesidad de proteger la creación humana accidental
    Then el panel expone un selector numérico en Segundos `[Creation_Grace_Period_Secs]`
    And dictamina universalmente cuánto tiempo dura vivo el Toast de "Deshacer" en todas las creaciones atómicas antes de que el Backend haga un COMMIT real a la base de datos transaccional en Camunda.

  Scenario: Gobernanza Económica de Ligas SGDEA Efímeras (US-010)
    Given la generación de PDFs Legales de alto costo que el cliente final puede consultar mediante una S3 URL Pre-Firmada
    When el usuario la comparta con los clientes para validación temporal ("Review Mode")
    Then el Administrador controla el `[Pre_Signed_URL_TTL_Hours]` dictando globalmente en el sistema la caducidad (TTL) de todos los links transaccionales generados (Volar el acceso al archivo tras 12 o 24 horas por seguridad).
```
**Trazabilidad UX:** Nueva pestaña en Pantalla 15.A (Restricciones Arquitectónicas / PMO).

---

### US-046: Gobernanza de Rendimiento e Integraciones (Data & Perf)
**Como** Analista de Infraestructura (SysAdmin)
**Quiero** manipular el comportamiento de lectura/escritura y polling del iBPMS
**Para** evitar saturar la red y proteger a las bases de datos de colapsos DWH.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: API Polling & Telemetry Thresholds
  Scenario: Master Switch del CRM ONS (US-013/019 Fallback Toggle)
    Given una caída nacional o intermitencia catastrófica en el CRM Externo de la empresa
    When el Administrador entra al Panel de Integraciones (Connections Dashboard)
    Then debe tener acceso directo a apagar el master switch `[Integración Continua CRM: OFF]`
    And al hacer esto, ordena inmediatamente al motor IBPM a refugiarse en la Metadata Interna local (Service Delivery Local Catalog) mitigando el error 500 y permitiendo facturar a pesar del CRM caído.

  Scenario: Cadencia de Polling de Dashboards (Anti-DDoS Interno - US-009)
    Given cientos de líderes de negocio con el "BAM Dashboard" abierto simultáneamente
    Then para evitar que las pantallas colapsen las réplicas de la Base de Datos con peticiones asíncronas
    And el Administrador manipula el `[BAM_Refresh_Rate_Ticks]` dictando cada cuántos minutos (Globalmente) el Frontend pedirá repintar gráficas a la BD, anulando comandos de refresco interactivos.

  Scenario: Regla de Retención y Purgado de Logs MLOps (Cold Storage NFR)
    Given el crecimiento exponencial de la tabla `ibpms_mlops_feedback_log` (Los Deltas capturados en el día por la US-015) en PostgreSQL 15+
    And que tenemos PROHIBIDO delegar la purga a `pg_cron` para proteger la salud del almacenamiento SSD transaccional
    Then un Scheduled Task del Backend (Spring Boot) buscará los JSONs pasados (>40 días)
    And los consolidará y trasladará por red segura (HTTPS SDK) al Azure Blob Storage
    And SOLO tras verificar el Hash/Éxito de la transferencia a Azure, el Backend lanzará el DELETE físico al motor SQL para borrarlos permanentemente.

  Scenario: Telemetría Global de Infraestructura (Mailbox Health)
    Given la caída de un Token OAuth de un buzón transaccional (US-016)
    When el motor detecte la falla de lectura
    Then además de la alerta local, se emitirá una Notificación Global en la Pantalla 15.A y se despachará un correo/webhook crítico al SysAdmin informando: "Integridad de Entrada Comprometida: Buzón X Desconectado".

  Scenario: Telemetría de Desfase Comercial (Sync Health)
    Given la falla definitiva de la sincronización nocturna o manual del Catálogo CRM (Agotamiento de reintentos RabbitMQ de la US-020)
    Then al arrancar la operación humana en la mañana (Ej: 8:00 AM)
    And el iBPMS forzará la exhibición de un "Banner Rojo Permanente" en la cabecera de la Pantalla 15.A indicándole al SysAdmin: *"CRÍTICO: La sincronización de catálogo falló. El iBPMS opera con una versión desactualizada de más de 24 horas"*.
```
**Trazabilidad UX:** Nueva pestaña en Pantalla 15.A (Performance y Conexiones / SysAdmin).

---
---

# 🚀 ROADMAP VERSIÓN 2 (V2) - EN REFINAMIENTO
*(Todas las funcionalidades, épicas e historias de usuario declaradas a partir de este punto pertenecen estructural y financieramente a la Fase 2 del Proyecto iBPMS. No forman parte del alcance del MVP V1).*

---

## ÉPICA V2-01: Gobernanza Activa y Erradicación de Antipatrones (Opinionated OS)
El iBPMS deja de ser un lienzo ciego y se convierte en un auditor inteligente del diseño de procesos. Interviene físicamente para evitar que las empresas democraticen la ineficiencia (como aprobadores redundantes que no mutan el modelo de datos).

### US-V2-001: Bloqueo Arquitectónico de Burocracia Humana (Hard-Stop)
**Como** Motor de Gobernanza (Opinionated OS)
**Quiero** auditar el I/O Binding de las tareas humanas en el Pre-Flight Analyzer (US-005)
**Para** bloquear físicamente el despliegue de procesos que modelen "sellos de goma" y firmas inútiles.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Forced DMN Adoption over Human Bureaucracy
  Scenario: Detección Temprana en el Lienzo (Linting en Tiempo Real) (CA-1)
    Given el Arquitecto diseñando un flujo en la Pantalla 6 (Canvas BPMN)
    When conecta dos (2) o más `UserTasks` humanas de forma secuencial
    And selecciona para la segunda tarea un Formulario que NO requiere inyección de campos nuevos (solo lectura de la tarea anterior)
    Then el motor de UI del Modeler debe dibujar la flecha (SequenceFlow) de conexión en color ROJO intermitente
    And proyectar un ícono de advertencia (⚠️) sobre la tarea redundante, alertando del antipatrón burocrático de forma inmediata antes del guardado.

  Scenario: Bloqueo Estructural en el Pre-Flight (Hard-Stop) (CA-2)
    Given las advertencias visuales del Canvas ignoradas
    When el Arquitecto intenta presionar [🚀 DESPLEGAR]
    And el motor Pre-Flight cruza el I/O Mapping contra el Esquema Zod confirmando la ausencia de mutación de datos
    Then el sistema CANCELA el despliegue cambiando el estado a ❌ ERROR CRÍTICO
    And el Copiloto IA arroja un modal bloqueante: "🛑 Antipatrón burocrático: La aprobación humana que no altera el contrato de datos no genera valor. Reemplace la tarea por una Regla DMN o condense la autoridad en un solo rol."

  Scenario: Inmunidad por Decisión de Enrutamiento (Gateway Decision Validity) (CA-3)
    Given la configuración de un "Sello de Goma" humano (Ej: Gerente que solo aprueba/rechaza)
    When el motor detecta que el output (salida) de la tarea de ese Gerente está directamente conectado a un Gateway Exclusivo (XOR) para definir la ruta del proceso
    Then el analizador interpreta la acción de "Toma de Decisión de Ruta" como una agregación de valor cognitivo ("Gateway Validity")
    And perdona la configuración, absteniéndose de lanzar el bloqueo rojo, permitiendo el despliegue del proceso.

  Scenario: Excepción por Responsabilidad Legal Expresa (Override Auditado) (CA-4)
    Given el bloqueo Hard-Stop del Pre-Flight disparado por un antipatrón redundante
    And que la empresa argumente fuerza mayor corporativa (Ej: ISO 9001 o mandato legal de doble firma)
    Then el modal bloqueante cuenta con un botón en fuente pequeña `[Ignorar Advertencia y Asumir Riesgo]`
    When el Arquitecto lo presiona
    Then el sistema le solicita una justificación de texto obligatoria y su contraseña de confirmación
    And permite el despliegue liberando el proceso, pero estampando el evento crudo en el Log de Auditoría bajo el tag `[BUREAUCRATIC_DEBT_ASSUMED]` responsabilizándolo permanentemente.
```
**Trazabilidad UX:** Pantalla 6 (BPMN Canvas) y Modal de Auditoría Pre-Flight.

---

### US-V2-002: El Asesino del "Síndrome de la Sandía" (Friction Tax Calculator)
**Como** CEO / Director de Transformación (BAM Dashboard)
**Quiero** que el sistema calcule exactamente cuánto dinero me cuesta la fricción y el tiempo muerto en mis procesos
**Para** tener argumentos financieros innegables que obliguen a los gerentes medios a automatizar sus feudos.

*Nota Estratégica: El síndrome de la sandía ocurre cuando los KPIs están "verdes" por fuera (el empleado cumplió su SLA de 2 horas para firmar), pero "rojos" por dentro (el caso estuvo estacionado 15 días esperando en la bandeja).*

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Friction Tax Telemetry (Value-Driven BAM)
  
  Scenario: Personalización del Dolor Financiero (Multiplicador de Gravedad Nodal) (CA-1)
    Given el Arquitecto diseñando la configuración financiera del proceso
    Then el sistema le permite asociar a un Nodo Humano específico (UserTask) un `[Costo_Fijo_Retraso_Hora]`
    And este valor desvincula la matemática del simple "salario del empleado"
    And permite traducir el Riesgo de Negocio (Ej: Un SLA incumplido en 'Aprobación VIP' cuesta $500/hora en multas) directamente al lenguaje gerencial del Tablero BAM.

  Scenario: El Reloj Justo (SLA-Aware Taxometer) (CA-2)
    Given un reclamo que cae a la bandeja de un usuario a las 6:00 PM del Viernes y es atendido a las 8:00 AM del Lunes
    When el motor analítico calcula el "Tiempo Muerto" (Wait Time) para monetizar la ineficiencia
    Then el algoritmo cruza obligatoriamente los timestamps contra la Matriz SLA Corporativa (US-043, Días/Horas Hábiles)
    And el Taxímetro se PAUSA automáticamente durante el fin de semana, cobrando $0 dólares por ese periodo
    And evitando "Data Contaminada" y desmotivación en los empleados por penalizaciones injustas fuera de su turno legal.

  Scenario: Telemetría de Sangría en Tiempo Real (Live Pain Counter) (CA-3)
    Given el Director de Transformación observando el BAM Dashboard en medio de la operación diurna
    Then el tablero expone un módulo gigante en ROJO denominado "Friction Tax: Dinero Quemado AHORA MISMO"
    And este indicador totaliza y grafica en Tiempo Real (Live) el costo acumulado por segundo de todos los casos que están atascados en las bandejas vivas
    And generando un sentido de urgencia psicológica para la intervención inmediata, en lugar de ser una simple autopsia post-mortem de fin de mes.
```
**Trazabilidad UX:** Dashboard Gerencial BAM (Grafana/Kibana) y Panel de Configuración Nodal.

---

### US-V2-003: Penalización por Carga Cognitiva en Formularios (Data Diet)
**Como** Motor UX del Sistema
**Quiero** auditar el destino de cada campo de UI creado en el "iForm Maestro"
**Para** impedir que la empresa capture datos "por si acaso" que nunca usa, saturando al usuario final.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Data Minimization and Form Strictness
  Scenario: El Peaje Analítico (Opcionalidad Forzada) (CA-1)
    Given un Diseñador intentando justificar un campo inútil para la operación alegando que su único fin es la exportación a PowerBI (Big Data ciego)
    When el IDE marca el campo como ROJO (Campo Huérfano)
    Then el Diseñador debe abrir obligatoriamente las propiedades del campo y seleccionar el [Destino Estratégico: Analítica Pasiva / Reportes]
    And al seleccionarlo, el esquema Zod subyacente DESHABILITA y BLOQUEA físicamente el switch de "Requerido" u "Obligatorio" para ese input
    And garantizando que la empresa pueda recolectar datos analíticos, pero prohibiendo estrictamente que se conviertan en una fricción bloqueante para el usuario final.

  Scenario: La Ley del Lector Garantizado (Campos de Texto Libre) (CA-2)
    Given la inserción por costumbre de un Área de Texto (Text-Area) para "Observaciones" que ninguna regla DMN puede evaluar matemáticamente
    When el Pre-Flight Analyzer audita el futuro de dicha variable en el proceso
    Then el motor le otorga el Indulto de Supervivencia ÚNICAMENTE si detecta uno de los tres "Lectores Garantizados" aguas abajo:
    And 1. Una `UserTask` humana donde un Analista leerá la variable.
    And 2. Una `GenerativeTask` (RAG / AI) que extraerá sentimiento o resumirá el texto.
    And 3. Un mapeo explícito de inyección SGDEA (Ej: Imprimir el campo dentro del PDF legal final).
    And bloqueándolo sin piedad si carece de estos 3 destinos.

  Scenario: Libertad de Bosquejo vs Guillotina en Producción (CA-3)
    Given el proceso creativo de un Arquitecto de Negocio
    When se encuentra en la Pantalla 3 (Form Builder) creando los inputs
    Then el sistema arroja un Soft-Lock visual (Iconos naranjas/rojos ⚠️) advirtiendo la falta de destino, pero permite guardar el trabajo tranquilamente en estado `DRAFT_INVALID` para no destruir la iteración.
    When días después, el Arquitecto une el formulario al BPMN en la Pantalla 6 e intenta pulsar el botón [🚀 DESPLEGAR A PRODUCCIÓN]
    Then el sistema arroja el Hard-Stop defintivo y ABORTA el Despliegue con un flag rojo ❌, obligándolo a higienizar (borrar o justificar) los campos huérfanos antes de impactar el Core productivo.
**Trazabilidad UX:** Pantalla 3 (Diseñador de Formularios - Form Builder).

---

### US-V2-004: Auto-Destrucción de Nodos Zombie (The Darwinian Engine)
**Como** Motor de Gobernanza MLOps (Agente Data Scientist de Turno Nocturno)
**Quiero** auditar el comportamiento histórico de los operarios humanos
**Para** sugerir la eliminación de reglas, tareas o firmas que la data empírica demuestre que son inútiles.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Continuous AI Pruning (Self-Healing Organization)
  Scenario: Slider de Tolerancia y Frenos de Emergencia (Parametrización MLOps) (CA-1)
    Given la configuración del Motor Darwiniano en la Pantalla 15.A (SysAdmin)
    When el cliente ajusta el `[Umbral_Inercia_Zombie]` y la `[Ventana_Analisis_Meses]`
    Then el sistema prohíbe estadísticamente bajar el umbral de inercia a menos del 85%
    And garantizando que si un humano rechaza o altera el 15% o más de los casos, la IA asume que SÍ está utilizando criterio cognitivo, bloqueando la etiqueta de "Zombie" para proteger la evaluación de riesgo real (Ej: Prevención de Fraude).

  Scenario: El Muro de Fuego Legal (Cero Skynet) (CA-2)
    Given el Agente Data Scientist descubriendo una Tarea Humana 99% inútil en la madrugada
    When consolida el hallazgo, calcula el ahorro en EBITDA y lo reporta a la Junta (Pantalla 5)
    Then el sistema TIENE ESTRICTAMENTE PROHIBIDO auto-parchear Producción de forma autónoma
    And exige que un rol fiduciario (PMO o Director) apruebe manualmente el hallazgo presionando `[Aceptar Hallazgo y Auto-Refactorizar]`, asumiendo la responsabilidad legal de la automatización por principio de Segregación de Funciones.

  Scenario: Cirugía Asistida y Respeto al SDLC (Generación de Drafts) (CA-3)
    Given la aprobación del Hallazgo Darwiniano por parte de la PMO
    When el Agente IA recibe la orden de ejecución
    Then el sistema NO altera la versión V1.0 que opera transaccionalmente en Producción
    And en background, el Agente abre la Pantalla 6 (Modeler), clona el mapa, extirpa la caja humana, inyecta la regla matemática DMN, sella las conexiones BPMN y lo guarda silenciosamente como `V1.1-DRAFT`
    And finalmente dispara un Ticket al Workdesk del Arquitecto IT diciendo: "Borrador de Optimización Generado. Revise las conexiones y presione Desplegar".
```
**Trazabilidad UX:** Pantalla 5 (AI Copilot / Evolution Findings) y Modeler (Auto-Refactor).
