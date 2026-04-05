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
	
  # ==============================================================================
  # A. US-001.1
  # ==============================================================================
  # A. DESEMPEÑO SRE, EFECTO ESTAMPIDA Y PAGINACIÓN (Anti-DDoS)
  # ==============================================================================
  Scenario: Paginación Segura, Carga Matutina y Búsqueda Server-Side (CA-10)
    Given la entrada concurrente de usuarios (Thundering Herd) a las 8:00 AM
    When el Frontend solicita la grilla unificada de tareas
    Then el Backend absorberá el impacto utilizando Caché (Redis/Memcached) para las consultas unificadas base.
    And implementará Paginación Server-Side estricta, prohibiendo búsquedas híbridas client-side (Cierre de Jitter visual).
    And el Backend aplicará un "Hard Limit" arquitectónico, retornando `HTTP 400` si la red solicita manipular la paginación a `> 100` registros (Prevención DDoS).
    And las búsquedas de texto usarán índices optimizados (Ej: `pg_trgm`) en BD y el Frontend aplicará un Debounce de 300ms antes de emitir el Request.
    And el Frontend mostrará un Skeleton Loader transicional, prohibiendo Spinners que bloqueen la pantalla.
    And el buscador solo rastreará tareas "Vivas/Completables", excluyendo casos históricos cerrados y tareas "Suspendidas" en Camunda.

  # ==============================================================================
  # B. UX, ACCESIBILIDAD (A11y) Y EFICIENCIA DE MEMORIA
  # ==============================================================================
  Scenario: El Reloj de un Solo Corazón y Accesibilidad Visual (Anti DOM-Thrashing) (CA-11)
    Given la necesidad de renderizar 50 temporizadores de SLA "vivos" en pantalla
    Then la arquitectura Frontend TIENE PROHIBIDO instanciar múltiples `setInterval` por tarjeta.
    And implementará un `Global Heartbeat Store` en Vue/Pinia basado en `requestAnimationFrame` del cual todas las tarjetas heredarán la reactividad pasivamente.
    And los colores del semáforo SLA estarán obligatoriamente acompañados de Iconografía (⚡ Rojo, ⏳ Amarillo, ✔️ Verde) usando SVGs in-line para garantizar la legibilidad en el 8% de daltónicos y evitar cargas asíncronas de PNGs.
    And la UI poseerá un interruptor `[Mute]` para silenciar notificaciones sonoras push de vencimiento de SLA.

  Scenario: Ergonomía Visual, KeepAlive y Empty States Gamificados (CA-12)
    Given la navegación intensiva del operador entre el Workdesk y los Formularios
    When el operador regresa al Workdesk presionando "Atrás"
    Then el Frontend utilizará `<keep-alive>` cacheando la página, filtros y scroll en RAM, garantizando carga en 0ms.
    And si el operador resuelve todas las tareas de la página actual y queda vacía, la grilla lo redirigirá automáticamente a la Página 1 (Prevención de Last Page Empty).
    And si la bandeja total llega a cero, se renderizará un `Empty State` con Gamificación pasiva (Ilustración de felicitación) en lugar de una tabla muerta.
    And la Grilla soportará "Densidad Condensada" y se degradará a "Card Layout" en móviles (<768px) ocultando las columnas 4 y 5.
    And los detalles secundarios se mostrarán vía Tooltips sobre el Nombre (Zero-Click Context).
    And la botonera de paginación estará fija (Sticky) arriba y abajo de la tabla.

  Scenario: Minificación WebSocket, Desvanecimiento y Throttling (CA-13)
    Given la necesidad de sincronizar eventos en tiempo real (Ej: Tarea reclamada por otra persona o Batch Uploads)
    Then el payload del WebSocket será atómico, enviando solo la instrucción y el ID (Ej: `{action: 'REMOVE', id: 'TK-123'}`) ahorrando 99% de I/O de red.
    And el Frontend aplicará un `Debounce/Throttling` inyectando actualizaciones masivas en bloques de 2 segundos para no congelar el renderizado del Main Thread.
    And el Frontend NO hará desaparecer la fila de golpe (evitando saltos de renglón).
    And ejecutará una animación CSS (`opacity: 0`) acompañada de un Toast discreto: "Tarea reclamada por otro equipo".
    And la identidad de terceros en la tabla grupal se ofuscará mostrando solo "En gestión por otro Agente" (Privacidad Operativa).

  # ==============================================================================
  # C. PREVENCIÓN DE FUGAS (IDOR, PII) Y SEGURIDAD
  # ==============================================================================
  Scenario: Sanitización del Payload DTO, Aislamiento Multi-Tenant y SQLi (CA-14)
    Given el retorno de datos desde la Base de Datos hacia el Workdesk
    Then el Backend emitirá un DTO estrictamente sanitizado, purgando contraseñas, PII y las variables internas de Camunda para prevenir Data Leaks en la Pestaña "Network".
    And las 5 columnas estándar serán rígidas (Polimorfismo columnar prohibido en V1 para asegurar performance).
    And toda consulta a la capa Repository inyectará OBLIGATORIAMENTE `tenantId = :myTenant` y aplicará el `bind` del ORM, neutralizando inyecciones SQL (`SQLi`).
    And si la plataforma detecta un error `401 Unauthorized` por caída severa, destruirá la sesión local exigiendo Re-Login, sin confiar visualmente en cachés obsoletos.

  Scenario: Delegación Segura (Prevención IDOR) e Interfaz Cinética (CA-15)
    Given el Toggle para ver las tareas de "Mi Asistente"
    When el Ejecutivo presiona el botón enviando el `user_id` del asistente
    Then el Backend VALIDARÁ PERIMETRALMENTE el RBAC, comprobando que el Ejecutivo logueado sea jerárquicamente el superior de ese ID.
    And si se altera la URL para espiar a otro usuario, el servidor arrojará `403 Forbidden` (Prevención IDOR).
    And al cargar la vista delegada, el Frontend aplicará un destello visual o Banner permanente alertando: "Estás viendo el escritorio de [Nombre]", mitigando errores operativos.

  # ==============================================================================
  # D. ENRUTAMIENTO INTELIGENTE Y REGLAS DE NEGOCIO
  # ==============================================================================
  Scenario: Anti Cherry-Picking y Enrutamiento por Habilidades (Skill-Based) (CA-16)
    Given la activación del interruptor administrativo "Atender Siguiente" (Anti Cherry-Picking)
    When el operario oprime el botón
    Then el motor Backend NO asignará ciegamente la tarea más crítica del sistema global.
    And cruzará matemáticamente la tarea más antigua/crítica contra el "Array de Skills" funcionales del operario (Skill-Based Routing).
    And proveerá un mecanismo de "Pausa / Skipeo Justificado" si la tarea exige contactar a un cliente que no responde, previniendo el secuestro operativo.
    And este interruptor administrativo dejará huella inmutable en el Audit Log Central, prohibiendo encendidos fantasma en madrugadas.

  Scenario: Jerarquía Multi-Origen y Resolución de Ambigüedades (CA-17)
    Given la unificación de tareas de Camunda (BPMN) y Entidades Locales (Kanban)
    When dos tareas de orígenes distintos expiren exactamente en la misma hora
    Then la base de datos resolverá el desempate aplicando una regla de ordenamiento por "Prioridad de Impacto Financiero" y luego "Fecha de Creación".
    And las tareas sin fecha de vencimiento (`dueDate = null`) se ponderarán matemáticamente como "SLA Infinito" enviándose al fondo del grid (`NULLS LAST`).
    And si una tarea tiene un impacto financiero masivo, el Grid inyectará un badge `[Impacto 🔥]` que rebatirá el orden visual del SLA general, posicionándola en Top 1.
    And la 4ta Columna "Avance" mapeará el nombre literal de la tarea BPMN contra el total de etapas del proceso de forma determinista.

  Scenario: Degradación Elegante Multi-Motor y Prioridad de Reapertura (CA-18)
    Given una caída temporal de la API transaccional de Camunda (HTTP 500)
    When el usuario carga su Workdesk en ese instante
    Then la interfaz aplicará Degradación Elegante, cargando exitosamente las tareas Kanban vivas de la Base Relacional sin emitir un 500 fatal screen.
    And proyectará un Toast advirtiendo: "Sincronización BPMN degradada".
    And si el operario hace Logout y entra en otra máquina, el Workdesk priorizará abrir su tablero general unificado en lugar de forzarlo a entrar a la tarea específica de ayer.
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

  Scenario: Amnesia Transaccional al Liberar Tarea (Protección del Motor) (CA-7)
    Given un formulario parcialmente diligenciado (Borrador vivo temporalmente en LocalStorage según US-029)
    When el analista oprime el botón [Liberar Tarea] para devolver el caso a la Cola Grupal
    Then el Frontend advierte mediante un Modal bloqueante: "Perderá los datos no enviados si devuelve el caso".
    And si el analista acepta, el sistema purga inmediatamente el LocalStorage de esa tarea en su navegador.
    And el Backend TIENE ESTRICTAMENTE PROHIBIDO enviar mutaciones o payloads JSON parciales a Camunda para su guardado.
    And el siguiente compañero que reclame la tarea la recibirá con el formulario 100% en blanco o con su prefillData original, garantizando la higiene absoluta de la Base de Datos Transaccional.

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

  Scenario: [Arquitectura] Render Functions, Teleportación y Z-Index Orchestrator (CA-07)
    Given una directriz para renderizar componentes infinitamente anidados (Ej: Grillas dentro de Módulos dentro de Secciones)
    Then el motor subyacente de Vue prescindirá del HTML rígido (`<template>`) utilizando funciones programáticas puras de Virtual DOM (`h()`) para renderizado ultrarrápido
    And los Tooltips y Modales usarán la etiqueta nativa `<Teleport to="body">` rompiendo el z-index local.
    But OBLIGATORIAMENTE el DOM instanciará un Orquestador Global (Z-Index Manager) con jerarquía dogmática:
    And `Z-900` para Modales UI, `Z-1000` para Tooltips, y `Z-5000` restrictivo para Cobertura de Errores Fatales (SweetAlert/Toasts), garantizando que las fallas de red del Motor NUNCA queden ocultas detrás del formulario.

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
    And Estructura y Acción: `submit`, `reset`, `button`, `image`, `file`, `color`, `output`, `fieldset`, `legend`, `label`.
    And cada uno de estos componentes emitirá su tipo de dato UI y su esquema Zod correspondiente para la validación bidireccional.

  Scenario: Componente de Ventana Emergente (Pop-ups Informativos) (CA-11B)
    Given la necesidad del Arquitecto de mostrar "Avisos" o Términos y Condiciones obligatorios
    When arrastra el componente "Modal Informativo" a la grilla y lo asocia a un Botón (Ej: "Ver Políticas") o a una Regla de Estado de Carga
    Then la plataforma invocará un `<Teleport to="body">` (bajo la jerarquía SRE Z-900) para oscurecer el fondo.
    And presentará un diálogo flotante (Pop-up) en lectura plana, con un botón obligatorio de [Entendido] para cerrarlo.
    But por gobernanza V.I.D.A., este componente es estéril (Carece de `I/O Binding` a Camunda); existe puramente para control de notificaciones UI y no contamina el Request JSON.

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

  Scenario: Validación Reactiva Zod Defensiva (Debounce & Blur) (CA-22)
    Given un usuario final está diligenciando un iForm Maestro denso en su Workdesk
    When incumple una regla de validación (Ej: escribe 3 números en un campo que exige 10)
    Then el formulario NO re-evaluará el AST global de Zod en cada pulsación de tecla (Keystroke) para proteger el Event Loop del navegador (Prevenir DOM Thrashing).
    And la inyección del error en vivo se disparará exclusivamente mediante validación perezosa (`@blur` al perder el foco) O mediante un `Debounce` estricto de 400ms después de que el operario deje de escribir.

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
	
	  # ==============================================================================
  # E. HERRAMIENTAS AVANZADAS, SIMULACIÓN Y QA AUTOMATIZADO
  # ==============================================================================
  Scenario: Generación Autónoma de Pruebas Unitarias QA (Auto-Vitest) (CA-68)
    Given un formulario visual completamente tipado y validado mediante la capa Zod
    When el Arquitecto de Diseño despliega el menú "Herramientas Avanzadas" y selecciona [Generar Suite de Pruebas]
    Then el Motor de Formulario (ZodBuilder) analizará el Árbol AST del esquema
    And auto-escribirá un archivo de código `.spec.ts` completo (Vitest/Jest) abarcando pruebas de Límites (Boundary Tests), validaciones de Nulos y coerción de Tipos
    And entregándole a los ingenieros de QA una cobertura base del 80% en cero segundos, acortando dramáticamente el tiempo de salida a producción (Time-to-Market).

  Scenario: Simulador Multi-Rol en Tiempo Real (iForm Maestro) (CA-69)
    Given el diseño de un formulario "Maestro" multi-etapa que atraviesa varias áreas operativas (Ej: Área Comercial -> Área Legal)
    When el diseñador finaliza el mapeo condicional y activa el `[Modo Simulador]` en el Header
    Then la interfaz inhabilitará la edición y desplegará un Dropdown de "Simular como Rol: [X]"
    And al seleccionar "Área Legal", el DOM silenciará u ocultará inmediatamente los campos configurados como `Read-Only` o `Hidden` para ese rol específico
    And permitiendo auditar lógicamente el control de acceso en caliente, sin necesidad de compilar o cambiar de usuario real en el sistema.

  # ==============================================================================
  # F. EXPOSICIÓN B2C (PÚBLICA) Y RESILIENCIA OPERATIVA
  # ==============================================================================
  Scenario: Modo Trámite Público Perimetral (Bypass JWT Seguro) (CA-70)
    Given la necesidad de someter un formulario a clientes externos sin credenciales EntraID (Ej: Formulario PQR / Denuncias)
    When el Arquitecto activa el parámetro 🌐 `[Permitir Enlace Público]`
    Then el sistema generará una URL Criptográfica transitoria
    And el Router Vue (US-051) marcará la ruta con `meta: { isPublic: true }`, eximiendo la intercepción de autenticación JWT.
    And OBLIGATORIAMENTE, el API Gateway montará políticas de *Rate Limiting* estricto y exigirá inyección de *reCAPTCHA v3* en el DOM para evitar que ataques de denegación de servicio (DDoS/Bots) llenen la base de datos de basura anónima.

  Scenario: Máquina del Tiempo JSON (Soft-Versioning Local) (CA-71)
    Given un usuario diagramando un formulario complejo que accidentalmente borra una pestaña o un Grid entero
    When navega a la sección de "Herramientas Avanzadas > Historial JSON"
    Then el sistema revelará un listado cronológico de "*Snapshots* Dinámicos" del esquema
    And permitirá inyectar y sobreescribir el AST visual del lienzo (`restore()`) devolviendo la interfaz exactamente al estado estructural de hace 15, 30 o 60 minutos, previniendo crisis por pérdida de trabajo.

  Scenario: Resiliencia Periférica Offline y Tolerancia a Conflictos (CA-72)
    Given un operador llenando un formulario crítico que sufre un micro-corte de Red (HTTP 5xx / Network Error)
    Then el sistema ejecutará un Fallback serializando el JSON hacia el `LocalStorage` del navegador de forma segura.
    When la red regrese y el Service Worker intente empujar ("Sync") el borrador guardado localmente hacia el Servidor
    Then el Frontend deberá OBLIGATORIAMENTE adjuntar el `VersionId` (Optimistic Hash) original del caso.
    And si un supervisor ya había modificado o cancelado el caso en el Servidor durante ese periodo Offline, el Backend detonará un `HTTP 409 Conflict`, previniendo que la data vieja sobreescriba corruptamente la verdad transaccional.

  # ==============================================================================
  # 3.1 mejoras	
  # ==============================================================================
  # A. EXPANSIÓN B2B: GENERACIÓN POR IA Y LIBRERÍA DE FRAGMENTOS
  # ==============================================================================
  Scenario: El Escáner Mágico (AI Prompt-to-Form & Document-to-Form) (CA-73)
    Given el lienzo en blanco del IDE de Formularios (Pantalla 7)
    When el Arquitecto sube un documento legacy (PDF/Imagen) o escribe un Prompt en lenguaje natural (Ej: "Genera formulario de crédito hipotecario")
    Then el Asistente IA Multimodal analizará el documento o texto.
    And autogenerará el layout visual en Vue 3 y el esquema Zod de manera instantánea, mapeando tipos de datos, labels y campos requeridos.
    And el Arquitecto retomará el control manual sobre el lienzo generado para refinar la UI, reduciendo el "Time-to-Market" de la digitalización.

  Scenario: Diccionario Global y Fragmentos Reutilizables (Snippets) (CA-74)
    Given la necesidad de estandarizar la recolección de datos en toda la empresa (Prevenir Torre de Babel)
    Then la plataforma TIENE PROHIBIDO leer variables de Camunda para autogenerar el formulario (El proceso no dicta el dato).
    And el IDE desplegará un autocompletado conectado al "Diccionario de Datos Maestro", sugiriendo variables corporativas (Ej: `cliente_id`) que heredan validaciones Regex pre-aprobadas.
    And el Arquitecto podrá seleccionar un grupo de campos y pulsar `[Guardar como Fragmento]`, empaquetándolos como un "Lego" reutilizable en la Paleta lateral.

  # ==============================================================================
  # B. GOBERNANZA DE DATOS (V.I.D.A.) Y SHIFT-LEFT SECURITY
  # ==============================================================================
  Scenario: El Peaje Analítico (Data Diet / Prevención de Campos Huérfanos) (CA-75)
    Given el panel de propiedades de cualquier componente visual en el IDE
    When el Arquitecto arrastra un nuevo campo
    Then el sistema le exigirá OBLIGATORIAMENTE declarar el "Destino Estratégico" del dato (Dropdown: `Regla DMN`, `Integración Externa`, `Documento PDF SGDEA`, `Analítica Pasiva`).
    And si el dato se marca como `Analítica Pasiva` (no aporta a la ruta crítica del proceso), el IDE DESHABILITARÁ y bloqueará físicamente el switch de "Obligatorio" (Zod required).
    And garantizando arquitectónicamente la "Dieta de Datos" e impidiendo generar fricción al usuario final por datos inútiles.

  Scenario: El Sello Radiactivo de Privacidad (Data Classification PII) (CA-76)
    Given el Arquitecto agregando campos confidenciales (Ej: Cédula, Diagnóstico Médico, Sueldo)
    Then dispondrá de un Master Switch de Ciberseguridad: `[🔒 Clasificar como PII / Sensible]`.
    And al activarlo, el IDE inyectará un metadato estructurado en el esquema Zod.
    And esta etiqueta instruirá imperativamente al Backend para que ofusque/encripte este dato en reposo (AES-256) y lo censure si es enviado al motor analítico (BAM) o a los Agentes LLM.
    And los campos tipo "Password" enmascararán el valor en la UI (`***`) nativamente.

  Scenario: Integración Autocompletado Gobernado y Escudo Anti-DDoS (CA-77)
    Given el Arquitecto diseña un campo configurado como "Gatillo" de autocompletado externo (Ej: Buscar RUT)
    Then el IDE TIENE ESTRICTAMENTE PROHIBIDO permitir la inyección de URLs o código JavaScript crudo (`fetch` / `axios`) en las propiedades del campo (Prevención SSRF).
    And obligará al usuario a seleccionar exclusivamente un "Conector Homologado" previamente registrado en el Hub de Integraciones (US-033).
    And el Frontend aplicará un `Debounce` obligatorio de 500ms al teclear, delegando la petición al BFF (Backend) para evitar fugas de datos desde el cliente.

  # ==============================================================================
  # C. ARQUITECTURA CORE: COMPILACIÓN BIDIRECCIONAL Y RENDERIZADO
  # ==============================================================================
  Scenario: Factoría Reactiva Zod On-The-Fly y Renderizado Bidireccional (CA-78)
    Given el entorno dividido: Canvas Visual (Izquierda) y Mónaco IDE (Derecha)
    When el Arquitecto arrastra un componente visual y marca restricciones (Ej: Requerido, Mínimo 5)
    Then el Mónaco IDE redactará en vivo el código Vue 3 y la regla matemática `z.string().min(5)`.
    And el enlace es bidireccional: si se borra la regla en el JS, el Canvas pierde la validación en milisegundos.
    And al renderizarse en el Workdesk operativo, el sistema NO descargará archivos `.js` estáticos.
    And instanciará el esquema dinámicamente usando una factoría `Zod` conectada a la memoria reactiva (`reactive()`) de Vue.

  Scenario: Sandboxing Estricto y Aislamiento Perimetral (Anti-XSS/RCE) (CA-79)
    Given que el Arquitecto inyecta lógica condicional (Cross-Field Logic: `if Monto > 1000`) o CSS exótico
    When el Formulario se renderiza operativamente en el Workdesk
    Then el Frontend encapsulará el componente utilizando `Shadow DOM`, impidiendo que el CSS distorsione el Layout corporativo (Style Bleed).
    And prohibirá estructuralmente la función `eval()` o `new Function()`. Toda expresión JS será parseada por un Abstract Syntax Tree (AST Sandbox) ciego a `window`, `document` o `fetch`.

  # ==============================================================================
  # D. ESTRUCTURAS COMPLEJAS, ESTADO Y RESILIENCIA
  # ==============================================================================
  Scenario: Reactividad Controlada en Formularios Densos (Lazy Validation) (CA-80)
    Given un usuario final diligenciando un "iForm Maestro" con alta densidad de inputs (+100 campos)
    When el usuario digita información a alta velocidad
    Then la validación proactiva de Zod TIENE PROHIBIDO ejecutarse en el evento síncrono por cada tecla presionada (`@input`).
    And el Frontend aplicará `Lazy Validation`, evaluando el esquema individualmente al perder el foco (`@blur`), protegiendo el Main Thread de Vue (Prevención DOM Thrashing).
    And las Máscaras Visuales (Ej: `$ 1.500,00`) mostrarán formato estético en UI, pero el formulario despojará la máscara en secreto y enviará el valor numérico crudo (`1500`) en el Submit.

  Scenario: Anclaje de Versión para Procesos In-Flight (Lazy Patching) (CA-81)
    Given que el Arquitecto publica la `V2` de un Formulario añadiendo campos obligatorios
    When un operario abre en el Workdesk un caso vivo (In-Flight) instanciado hace 2 meses bajo la `V1`
    Then el BFF (Backend for Frontend) inyectará ESTRICTAMENTE el JSON Schema de la versión `V1` originaria a la tarea en vuelo.
    And el sistema TIENE PROHIBIDO exigirle al usuario final campos de la V2 que no existían cuando él inició el trámite, evitando Crash 500 por desajuste de JSON.

  Scenario: Autoguardado Volátil, Limpieza de Fantasmas y Smart Buttons (CA-82)
    Given un usuario operando un formulario en el Workdesk
    Then cada interacción disparará un "Auto-Guardado" silente en LocalStorage atado al `Task_ID`.
    And si el campo B es visible solo cuando A es "Sí", y el usuario cambia A a "No", el campo B desaparece Y PURGA automáticamente su valor interno (Limpieza de Data Fantasma).
    And si el usuario sube PDFs al `<Dropzone>` (Upload-First) pero cierra la pestaña sin hacer Submit, el Frontend disparará un `Beacon` asíncrono ordenando al Backend destruir esos archivos huérfanos.
    And dispondrá de "Smart Buttons" nativos (`[Completar]`, `[⚠️ Escalar Error BPMN]`) envueltos en interceptores de red globales `try/catch`.

  Scenario: Sandbox de Pruebas Zod In-Browser (Shift-Left QA) (CA-83)
    Given el diseño finalizado del iForm Maestro
    Then el IDE proveerá una "Consola QA embebida" (Simulator).
    And generará automáticamente Payloads extremos (Fuzzing) simulando Paths Felices y Tristes en la memoria RAM del navegador, certificando matemáticamente el contrato antes del despliegue.

  Scenario: Manejo Amigable de Errores de Sintaxis en el Mónaco IDE (CA-84)
    Given el Arquitecto está editando el código Vue o Zod manualmente en el panel de Mónaco IDE
    When introduce un error de sintaxis (Ej: falta una coma, llave de cierre, o tipado incorrecto)
    Then la plataforma TIENE PROHIBIDO colapsar con una pantalla blanca (Fatal Error) impidiendo seguir trabajando
    And el editor Mónaco interceptará el error de compilación en tiempo real (debounced)
    And subrayará de rojo (Squiggly Line) la línea conflictiva
    And proyectará en la zona inferior un panel amigable con mensajes legibles para un humano (Ej: "Hay un error de sintaxis cerca de la línea 14").

  Scenario: Auto-Guardado y Recuperación de Sesión en el Diseñador (CA-85)
    Given el Arquitecto está construyendo un formulario extenso en la Pantalla 7 (IDE Web)
    When ocurre una desconexión de red, apagón, o un cierre accidental de la pestaña
    Then el sistema debe garantizar la preservación del progreso inyectando el estado del lienzo en el `LocalStorage` del navegador de forma reactiva a cada cambio.
    And al regresar a la Pantalla 7, la aplicación detectará el borrador huérfano y mostrará un banner amigable: "Detectamos un borrador no guardado. ¿Desea restaurar su trabajo previo?" permitiendo recuperar el Canvas intacto.

  Scenario: Catálogo y Explorador de Formularios (Form Manager Dashboard) (CA-86)
    Given la necesidad del Arquitecto de buscar, re-editar o consultar versiones de formularios pre-existentes
    When el usuario ingresa al módulo de "Formularios" (Pantalla 7 Principal)
    Then EL SISTEMA NO CARGARÁ el IDE en blanco directamente, sino que presentará un "Catálogo o Grilla de Formularios"
    And esta Grilla incluirá un Buscador `Server-side` para buscar por Nombre de Negocio o ID Técnico.
    And cada fila o tarjeta mostrará: 
      - Nombre del Formulario (Ej: "Onboarding VIP")
      - Tipo: (Simple vs iForm Maestro)
      - Versión Activa (Ej: `v3`)
      - Fecha de Última Modificación y Autor
    And al hacer clic sobre un formulario, se abrirá en el Lienzo IDE. Si se desea ver el historial de diseño de ese formulario en particular, la grilla ofrecerá la opción de [Ver Historial de Versiones] para realizar Rollbacks.

  # ==============================================================================
  # G. REMEDIACIONES POST-AUDITORÍA (Sprint Remediation Brief 2026-04-05)
  # Origen: docs/requirements/us003_gap_remediation_brief.md
  # Tickets: REM-003-01 a REM-003-07
  # Propósito: Cerrar GAPs de implementación detectados por el workflow
  #            /analisisEntendimientoUs.md tras finalizar las 17 iteraciones
  #            de la Auditoría Integral del Backlog.
  # ==============================================================================

  Scenario: [REMEDIACIÓN] Persistencia Versionada del Diseño JSON del Formulario (CA-87)
    # Origen: REM-003-01 | Decisión PO: Opción A PostgreSQL JSONB
    Given que el Arquitecto finaliza el diseño de un formulario en el Canvas (Pantalla 7) y presiona [Guardar]
    When el IDE serializa el AST del esquema visual (JSON del Canvas + Esquema Zod + Metadatos)
    Then el Backend persistirá el diseño completo en la tabla relacional `ibpms_form_definitions` utilizando una columna JSONB de PostgreSQL para el cuerpo del esquema.
    And cada guardado generará una nueva fila inmutable con `version_id` autoincremental, `created_by`, `created_at` y un hash SHA-256 del contenido para detección de colisiones.
    And el Backend expondrá los endpoints REST: `GET /api/v1/forms/{formId}/versions` (listar versiones) y `POST /api/v1/forms/{formId}` (crear nueva versión).
    And TIENE PROHIBIDO utilizar Object Storage (S3/MinIO) como motor primario en V1; la columna JSONB de PostgreSQL es la fuente de verdad transaccional del diseño.

  Scenario: [REMEDIACIÓN] Separación Arquitectónica de Contextos IDE vs Workdesk (CA-88)
    # Origen: REM-003-02
    Given la coexistencia de lógica de diseño (IDE, Pantalla 7) y lógica de operación (Workdesk, Pantalla 2) dentro de la US-003
    Then el Frontend TIENE OBLIGACIÓN de mantener una separación física de módulos entre ambos contextos.
    And los composables/hooks de validación Zod operativa (Workdesk) residirán en un directorio distinto (`composables/workdesk/`) a los composables del IDE (`composables/ide/`).
    And los CAs de validación Lazy @blur (CA-22, CA-80) aplican EXCLUSIVAMENTE al contexto Workdesk.
    And los CAs de errores de Mónaco (CA-84) y Language Servers (CA-17) aplican EXCLUSIVAMENTE al contexto IDE.
    And ningún composable del IDE debe importar dependencias del Workdesk ni viceversa, para prevenir regresiones cruzadas.

  Scenario: [REMEDIACIÓN] Directriz de Complementariedad QA Sandbox vs Auto-Vitest (CA-89)
    # Origen: REM-003-03
    Given la coexistencia de dos herramientas QA: Sandbox In-Browser (CA-83) y Auto-Vitest (CA-68)
    Then la plataforma los tratará como herramientas COMPLEMENTARIAS con dominios distintos:
    And el Sandbox In-Browser (CA-83) es la herramienta de quick-check en tiempo de diseño, utilizada por el Arquitecto de Formularios en la Pantalla 7 para validar contratos Zod instantáneamente sin salir del IDE. No genera archivos persistentes.
    And el Auto-Vitest (CA-68) es la herramienta de regresión persistente, utilizada por el Ingeniero QA para generar archivos `.spec.ts` que se integran al pipeline CI/CD y aseguran cobertura de regresión a largo plazo.
    And TIENE PROHIBIDO considerar ambas herramientas como redundantes o eliminar una en favor de la otra.

  Scenario: [REMEDIACIÓN] Límites de Rendimiento y Lazy Mount para iForm Maestro (CA-90)
    # Origen: REM-003-04
    Given un Arquitecto diseñando un iForm Maestro de alta densidad en el Canvas
    When la cantidad de componentes visuales supere el umbral configurable `MAX_FORM_FIELDS` (Valor por defecto: 200 campos)
    Then el IDE emitirá una advertencia visual amigable (Banner amarillo, NO un bloqueo duro) indicando que el formulario supera el límite recomendado de campos y el rendimiento del navegador del operario podría degradarse.
    And para formularios que superen el umbral, el Motor de Renderizado del Workdesk activará OBLIGATORIAMENTE un patrón de Lazy Mount donde solo la pestaña o acordeón activo monta su DOM, preservando el Main Thread de Vue.
    And el equipo de QA deberá ejecutar un test de carga con un formulario de 250+ campos y 3 grillas anidadas, midiendo Time-to-Interactive (TTI) para certificar que no exceda 3 segundos en un navegador estándar.

  Scenario: [REMEDIACIÓN] Validación de Contrato de Integración con US-029 (CA-91)
    # Origen: REM-003-05
    Given la dependencia crítica de la US-003 con la US-029 (Persistencia CQRS) para Auto-Guardado, Smart Buttons e I/O Mapping
    Then el Arquitecto de Software TIENE OBLIGACIÓN de certificar la existencia de los siguientes contratos de la US-029 antes de considerar la US-003 como feature-complete:
    And Endpoint de Auto-Guardado: `POST /api/v1/drafts/{taskId}` (Persistir borrador parcial).
    And Endpoint de Recuperación: `GET /api/v1/drafts/{taskId}` (Reconstruir borrador al reabrir tarea).
    And Endpoint de Completado: `POST /api/v1/tasks/{taskId}/complete` (Smart Button Completar con I/O Mapping).
    And Endpoint de Limpieza: `DELETE /api/v1/drafts/{taskId}` (Purgar borrador post-submit).
    And si alguno de estos contratos no existe al momento de la integración, se generará un ticket bloqueante contra la US-029 antes de pasar a QA.

  Scenario: [REMEDIACIÓN] Política de Expiración y Limpieza de LocalStorage (CA-92)
    # Origen: REM-003-06
    Given la acumulación progresiva de datos en LocalStorage por los mecanismos de Auto-Guardado (CA-24, CA-85), Resiliencia Offline (CA-72) y Snapshots JSON (CA-71)
    Then el Frontend implementará un servicio `LocalStorageGarbageCollector` que se ejecutará automáticamente al iniciar la SPA.
    And aplicará una regla de expiración temporal: eliminará entradas con `timestamp` superior a 7 días naturales.
    And aplicará una regla de cuota espacial: si el volumen total de entradas con prefijo `ibpms_draft_` o `ibpms_snapshot_` supera 50MB estimados, purgará las más antiguas primero (FIFO).
    And registrará un log discreto en la consola del navegador: `[GC] Purged N stale drafts (X KB freed)`.
    And TIENE PROHIBIDO tocar claves de LocalStorage que no pertenezcan al dominio de formularios del iBPMS.

  Scenario: [REMEDIACIÓN] Componente Unificado de Vista Solo-Lectura (CA-93)
    # Origen: REM-003-07
    Given la coexistencia de dos modos de lectura: Visor Histórico para Auditoría (CA-37) y Vista Imprimible para Visualizadores (CA-56)
    Then el Frontend implementará un único componente base `FormReadOnlyView` con una prop `mode` que acepta dos valores:
    And `mode="audit"`: Renderiza el formulario con metadatos de auditoría visibles (quién modificó, cuándo, qué campo cambió) para consumo del Rol Auditor.
    And `mode="print"`: Renderiza el formulario como un documento de texto limpio sin bordes de input ni metadatos técnicos, optimizado para impresión y lectura plana.
    And ambos modos comparten el mismo motor de renderizado de campos (zero duplication), diferenciándose únicamente en la capa de presentación de metadatos.
    And si técnicamente la unificación genera complejidad excesiva, el Arquitecto Frontend puede mantener dos componentes separados SIEMPRE Y CUANDO compartan un composable base común para evitar duplicación de lógica de lectura.

```
**Trazabilidad UX:** Wireframes Pantalla 7 (IDE Web Pro-Code para Formularios).

---

### US-028: Simulador de Contratos Zod en Memoria (In-Browser QA Sandbox)
**Como** Ingeniero de Calidad (QA) / Arquitecto Frontend
**Quiero** un entorno de simulación (Sandbox) integrado directamente en el Diseñador Web (Pantalla 7) que inyecte Payloads extremos (Feliz y Triste) contra el esquema Zod en tiempo real
**Para** garantizar que las reglas matemáticas, de obligatoriedad y formato (Regex) funcionen perfectamente antes de asociar el formulario a Camunda, sin generar código muerto ni depender de pipelines de CI/CD externos.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Integrated BDD Zod Testing Sandbox

  Scenario: Ejecución Interna In-Browser (Zero Dead Code) (CA-1)
    Given la estructura JSON del Formulario generada por el IDE web en Pantalla 7
    When el usuario oprime el botón `[🧪 SIMULAR CONTRATO ZOD]`
    Then el sistema NO descargará archivos `.spec.ts` físicos al disco duro local.
    And abrirá un "Panel de Consola QA" (Split View) integrado en el mismo IDE.
    And ejecutará las validaciones en tiempo real utilizando la memoria RAM del navegador contra el objeto Zod reactivo.

  Scenario: Boundary Testing Pragmático y Ciego (Type-Based Fuzzing) (CA-2)
    Given el esquema Zod compilado en memoria
    When el motor de simulación arranca
    Then el sistema autogenerará un Payload Dummy basado estrictamente en los Tipos Base y Restricciones matemáticas de Zod (Ej: Inyectar un string de 5 caracteres "AAAAA" si la regla es `.min(5)`), sin intentar adivinar la semántica del negocio.
    And presentará en la Consola dos evaluaciones automáticas:
      1. Path Feliz (100%): Inyecta el Payload válido generado y aserta visualmente `success: true`.
      2. Path Triste (Empty): Inyecta un Payload vacío `{}` y aserta que Zod devuelva los errores de `Required` correspondientes.

  Scenario: Modificación Manual del Mock Payload (Edición en Caliente) (CA-3)
    Given el Panel de Consola QA abierto y el Path Feliz generado
    Then el QA podrá editar libremente el código JSON del "Payload de Prueba" en un mini-editor de texto incrustado.
    And al teclear o borrar comillas, el motor de Zod reevaluará instantáneamente el Payload arrojando los errores de validación en tiempo real, permitiendo al humano probar Regex complejos (Ej: Cédulas o NITs) a mano.

  Scenario: Aislamiento Puro de Lógica de Negocio (Zero-Network Mocking) (CA-4)
    Given un formulario con campos que dependen de llamadas asíncronas a APIs externas (Data Sources)
    When se ejecuta el Simulador Zod
    Then el motor evaluará ÚNICAMENTE el método `zod.safeParse()` sobre el esquema estático.
    And omitirá cualquier intento de invocar el ciclo de vida de Vue (Ej: `onMounted`), garantizando que no se disparen peticiones de red (Axios/Fetch) ni se requieran librerías de Mocking complejas (MSW/vi.mock), asumiendo que la prueba valida el contrato de datos final y no la interfaz gráfica.

  Scenario: Fuzzing Ciego Recursivo (Topología de Datos Anidados) (CA-5)
    Given un formulario con reglas de colección (DataGrid o Grupo Repetible) como `z.array().min(2)`
    When el Sandbox arranca su Payload Generator
    Then iterará de forma superficial (Shallow Fuzzing) creando recursivamente una lista con exactamente 2 sub-objetos completos (Strings/Numbers basura tipificados)
    And el Arquitecto sólo tendrá que enmendar los valores irreales, pero no perderá tiempo redactando corchetes ni llaves (`[] {}`) para satisfacer la estructura base.

  Scenario: Dropdown Dinámico para Formularios Multi-Etapa (CA-6)
    Given un iForm Maestro que muta campos dependiendo de su fase (`Current_Stage`)
    When se abre la Consola del Sandbox
    Then la cabecera mostrará un Dropdown obligatorio `[ 🎭 Etapa a Simular: Radicación 🔻 ]`
    And al cambiarlo, la Variable en el Store altera en caliente el Zod Schema revelando u ocultando validaciones
    And el Fuzzer regenera el Payload JSON abarcando estrictamente la etapa seleccionada sin arrojar Falsos Positivos de validaciones aplanadas.

  Scenario: Visibilidad Dual Absoluta de Transformaciones (Split-Panel) (CA-7)
    Given una regla Zod que muta el dato del input (Ej: `.transform()` de String a Entero)
    When el Sandbox evalúa en tiempo real
    Then el IDE estará tajantemente seccionado en dos paneles:
    And Izquierda Editable: `[ 📥 Payload Crudo (Lo que digita el usuario) ]`
    And Derecha (Read-Only): `[ 📤 Payload Parseado (Lo que viaja a Camunda) ]`
    And el QA certifica de un pestañeo cómo el framework limpia espacios, parsea números o blanquea campos por omisión (Drop Keys).

  Scenario: Bloqueo Sincrónico Aceptable y Delegación de Regex Rotos (CA-8)
    Given una validación `.regex()` exigente o un `.superRefine()` de lógica temporal cruzada (Ej: `FechaInicio > FechaFin`)
    When el Sandbox inyecta el Dummy Data ("AAAAA" o Fechas cruzadas inconexas)
    Then la optimización prematura (Web Workers) está prohibida en V1
    And el Sandbox aceptará un bloqueo síncrono sub-milimétrico (`.safeParse` nativo)
    And pintará inescrupulosamente de ROJO el "Path Feliz" autogenerado informando de la ruptura Regex (Ej: `Placa Inválida`)
    And es responsabilidad primaria del Humano (QA) entrar al mini-editor y digitar voluntariamente un valor semántico válido ("ABC-123") para curar la barrera intencional creada por Zod.

  Scenario: Representación Visual Traducida (Human-Readable Errors) (CA-9)
    Given un Path Triste donde el Payload se va de cara contra 15 reglas Zod obligatorias conjuntas
    When el Sandbox invoca `.safeParse` y escupe el `ZodError` HTTP 400
    Then prohibido renderizar el array críptico Json en la UI del QA
    And el Frontend debe destripar la traza y repintarla como un Listado HTML con viñetas amigables:
    And "❌ [cliente.direccion.ciudad] - Este campo es obligatorio."
    And "❌ [monto_credito] - Debe ser mayor a 0."

  Scenario: Amnesia Prohibida del Payload Editor (Persistencia Local) (CA-10)
    Given la ventana de edición cruzada `[ 📥 Payload Crudo ]` donde el Arquitecto modificó 10 campos manualmente durante 5 minutos para simular un Path Feliz complejo
    When el usuario cierra la pantalla por accidente o refresca (`F5`)
    Then la directiva `@vueuse/core` invocará a `useLocalStorage('zod_mock_form_{ID}')`
    And el JSON artesanal renacerá exactamente como fue dejado, evitando destrucción por amnesia en sesiones continuas del QA.

  Scenario: Sello Criptográfico de Certificación BD (Cumplimiento ISO) (CA-11)
    Given el panel derecho (Parsed Payload) de un formulario en VERDE demostrando la validación estricta Zod en el Sandbox
    When se ilumina y se empuja el mega-botón: `[ 🏆 CERTIFICAR CONTRATO ZOD ]`
    Then se dispara un `POST /api/v1/design/forms/{id}/certify`
    And la Base de Datos plasma un sello: `ibpms_forms.is_qa_certified = true`
    And se registra inamoviblemente en el log histórico `ibpms_audit_log` el Test exacto: "El Tester Juan certificó la V2 del Formulario el DD/MM/AAAA. Payload utilizado: {JSON...}"
    And sirviendo esta bandera (is_qa_certified) de Guardrail para la Pantalla 6; que arrojará una Warning mandatoria de "Pre-Flight" bloqueando el botón `[🚀 DESPLEGAR Proceso]` si el BPMN intenta ir a Prod con un formulario sin sello QA.
```
**Trazabilidad UX:** Wireframes Pantalla 7 (Panel QA).


### US-029: Ejecución y Envío de Formulario (iForm Maestro o Simple)
**Como** Analista / Usuario de Negocio
**Quiero** diligenciar la información de mi sección habilitada en la vista de la tarea (Pantalla 2) y presionar "Enviar"
**Para** finalizar exitosamente mi actividad y que el motor continúe al siguiente paso del proceso.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Task Completion with Form Data

  Scenario: Enviar datos válidos de formulario (CA-1)
    Given la tarea "TK-100" asignada a "carlos.ruiz" requiere el formulario "Form_Aprobacion_V1"
    And "Form_Aprobacion_V1" exige el campo obligatorio numérico "monto_aprobado"
    When "carlos.ruiz" realiza un POST a "/api/v1/workbox/tasks/TK-100/complete"
    And incluye en el body el JSON '{"variables": {"monto_aprobado": 1500, "comentarios": "Ok"}}'
    Then el sistema debe retornar un HTTP STATUS 200 OK
    And la tarea "TK-100" marca su estado como "COMPLETED"
    And las variables del JSON se persisten asociadas a la instancia del proceso.

  Scenario: Enviar datos inválidos (Violación del JSON Schema) (CA-2)
    Given la tarea "TK-100" requiere el campo obligatorio "monto_aprobado" numérico
    When "carlos.ruiz" realiza un POST a "/api/v1/workbox/tasks/TK-100/complete"
    And incluye un JSON vacío '{"variables": {}}'
    Then el sistema valida el payload contra el JSON Schema registrado para "Form_Aprobacion_V1"
    And el sistema debe retornar un HTTP STATUS 400 Bad Request
    And el error format JSON debe especificar `{"error": "ValidationFailed", "fields": [{"field": "monto_aprobado", "message": "Required"}]}`

  Scenario: Trazabilidad Volátil y Recolección de Basura (LocalStorage & GC) (CA-3)
    Given un formulario extenso ("Maestro_Onboarding") abierto en el Workdesk
    When el analista diligencia la mitad de los campos y accidentalmente cierra la pestaña
    Then el Frontend recuperará el progreso utilizando almacenamiento estricto en el navegador (`LocalStorage` mediante `@vueuse/core`) atado al Task_ID.
    And cuando el POST a `/complete` finaliza exitosamente (HTTP 200 OK), el Frontend ejecutará una purga síncrona destruyendo inmediatamente la llave de ese caso específico.
    And un proceso silencioso global al inicio de la sesión eliminará cualquier borrador huérfano en la PC del usuario que supere las 72 horas de antigüedad, previniendo cuellos de memoria.

  Scenario: Consistencia Transaccional Cruda (ACID Fallback over Sagas) (CA-4)
    Given un Payload de formulario (`/complete`) perfectamente validado en Zod que llega al Backend
    When el motor orquestador (Camunda 7) sufre un Crash o Timeout HTTP en su API REST interna
    Then el Backend iBPMS abortará inmediatamente la transacción (Rollback de Persistencia CQRS)
    And devolverá un error HTTP 500 Crudo ("Motor No Disponible") a la UI en Pantalla 2
    And se prohíbe a nivel arquitectónico generar falsos positivos HTTP 202 ("Guardado para después") para eludir el colapso del proceso judicial de fondo.

  Scenario: Inyección Megalítica de Contexto (Patrón BFF) (CA-5)
    Given la entrada física a la vista de la tarea (Pantalla 2)
    When el Frontend inicializa el componente Vue
    Then despachará exactamente UNA (1) única petición consolidada GET `/api/v1/workbox/tasks/{id}/form-context`
    And el Backend obrará como BFF *(Backend for Frontend)* inyectando en un solo Mega-DTO la triada: El JSON Schema de Zod, la configuración de Layout de Vue, y las Variables de Solo Lectura extraídas de Camunda (`prefillData`) para poblar inputs en un solo *tick* de renderizado.

  Scenario: Seguridad Asimétrica y Micro-Tokens Criptográficos (Zero-Trust) (CA-6)
    Given una validación asíncrona externa (Ej: Validar NIT) gatillada desde el Frontend (`OnBlur`)
    When el Backend consulta la API externa exitosamente y retorna al Frontend un "Micro-Token JWT" firmado criptográficamente de corta duración (Ej: TTL 15 min)
    Then al momento del Submit final (`/complete`), el Frontend adjuntará este Micro-Token.
    And el Backend (Zero-Trust) omitirá realizar una segunda llamada de red externa bloqueante, limitándose a verificar matemáticamente la validez de su propia firma en el Micro-Token para autorizar la transacción ACID en milisegundos.

  Scenario: Integridad de Asignación Concurrente (Implicit Locking) (CA-7)
    Given que una tarea "TK-400" está explícitamente asignada al analista `maria.perez` en el motor
    When el analista `pedro.gomez` intercepta vulnerablemente la URL o el JWT Payload e intenta someter un POST a `/tasks/TK-400/complete`
    Then el Core iBPMS examina deductivamente el `{delegatedUserId}` transaccional contra la identidad central del Security Context
    And aborta transaccionalmente la colisión inyectando un lapidario `HTTP 403 Forbidden` o `409 Conflict`, extirpando la necesidad pesada de emitir *ETags* a través del flujo asíncrono.

  Scenario: Hibridación de Datos Históricos vs Nuevos Contratos (Lazy Patching) (CA-8)
    Given el BFF inyectando `prefillData` de una Instancia antigua (V1) hacia un Formulario Zod nuevo (V2)
    When existan campos obligatorios nuevos en la V2 que no venían en la data histórica de Camunda (`null` o `undefined`)
    Then el esquema Zod reactivo los evaluará inmediatamente como inválidos
    And el Frontend pintará dichos inputs en ROJO y bloqueará físicamente el botón de [Enviar]
    And obligando procedimentalmente al analista a contactar al cliente y digitar el dato faltante en la UI para poder avanzar el proceso (Amnistía en Lectura, Guillotina en Escritura).

  Scenario: Desacoplamiento de Carga Binaria (Upload-First Pattern) (CA-9)
    Given un formulario Zod que incluye un componente `<InputFile>`
    When el usuario final adjunta un documento pesado (Ej: PDF de 10MB)
    Then el Frontend ejecutará una carga asíncrona temprana (Pre-Submit) hacia la Bóveda SGDEA (`/api/v1/documents/upload-temp`) obteniendo un Identificador Único (`UUID`).
    And al presionar [Enviar], el POST a `/complete` enviará EXCLUSIVAMENTE el JSON plano referenciando el ID (`{"cedula_pdf": "UUID-123"}`), teniendo PROHIBIDO arquitectónicamente enviar payloads Multipart o Base64 contra el motor de procesos Camunda.
	
  # ==============================================================================
  # US-029.1
  # ==============================================================================
  # A. INICIALIZACIÓN, BFF Y RECUPERACIÓN DE BORRADORES (UX & SRE)
  # ==============================================================================
  Scenario: Inyección Megalítica de Contexto (Patrón BFF) (CA-10)
    Given la entrada física a la vista de la tarea operativa (Pantalla 2)
    When el Frontend inicializa el componente Vue
    Then despachará UNA (1) única petición GET consolidada a `/api/v1/workbox/tasks/{id}/form-context`.
    And el Backend (BFF) inyectará el Mega-DTO: [Esquema Zod Vigoroso + Layout UI + Data Histórica (prefillData)].
    And este DTO incluirá la versión exacta del esquema (`schema_version`) para prevenir choques generacionales si el Arquitecto modifica el diseño mientras el caso está en vuelo.

  Scenario: Autoguardado Híbrido y Cifrado PII en LocalStorage (CA-11)
    Given la digitación continua de un analista en un formulario extenso
    Then el Frontend guardará el borrador (Draft) en el `LocalStorage` del navegador atado al `Task_ID`.
    But si el esquema Zod marca campos como `PII/Sensibles` (US-003), el Frontend DEBE aplicar cifrado simétrico (AES) usando una llave derivada de la sesión antes de escribir en LocalStorage.
    And disparará peticiones silenciosas de Merge Commit al Backend (Snapshot Volátil) solo bajo un Debounce de 10s de inactividad, usando una validación Zod "Parcial" (permitiendo nulos pero castigando tipos inválidos).

  # ==============================================================================
  # B. EJECUCIÓN, IDEMPOTENCIA Y ADUANA DE ARCHIVOS (APPSEC)
  # ==============================================================================
  Scenario: Idempotencia y Protección Anti-Doble Clic (El Dedo Tembloroso) (CA-12)
    Given el usuario pulsa [Enviar Formulario] múltiples veces por ansiedad o lag
    When el Payload JSON impacta el endpoint POST `/complete`
    Then el Frontend inyectará un Header `Idempotency-Key` (UUID único por montaje de componente).
    And el API Gateway/Backend procesará únicamente la primera transacción.
    And las peticiones subsecuentes idénticas retornarán un `HTTP 200 OK` silenciado desde la Caché, protegiendo a Camunda de excepciones `OptimisticLocking` o doble gasto en el Event Sourcing.

  Scenario: Desacoplamiento de Carga Binaria (Upload-First) y Escudo Anti-IDOR (CA-13)
    Given el patrón donde el cliente envía un UUID de un PDF en el POST final (`{"cedula": "UUID-123"}`)
    When el Backend recibe el Payload de cierre de formulario
    Then la arquitectura TIENE ESTRICTAMENTE PROHIBIDO enlazar ciegamente ese archivo a la tarea.
    And el Backend validará en la tabla de adjuntos temporales que `UUID-123` pertenezca al `user_id` logueado Y haya sido subido en el contexto de esa misma `task_id` (Defensa Anti-IDOR).
    And si detecta un UUID ajeno, abortará la transacción con `HTTP 403 Forbidden`.
    And un Cron Job nocturno destruirá físicamente de S3/SGDEA cualquier archivo temporal (TTL > 24h) sin confirmación transaccional para evitar facturas por basura infinita.

  Scenario: Seguridad Asimétrica y Prevención Replay en Micro-Tokens (CA-14)
    Given una validación asíncrona externa (Ej: Validar NIT) gatillada `OnBlur` que retorna un Micro-Token
    When el Frontend adjunta este token en el POST `/complete` final
    Then el Backend verificará matemáticamente su firma (Zero-Trust) para no repetir la llamada externa.
    And la arquitectura PROHÍBE el re-uso de tokens (Replay Attacks); el Token DEBE contener en Claims el `taskId` exacto y un `jti` que será invalidado en Redis un milisegundo después del Submit.

  # ==============================================================================
  # C. VALIDACIÓN ZERO-TRUST Y FIELD-LEVEL RBAC
  # ==============================================================================
  Scenario: Zod Isomórfico y Guillotina de Datos Fantasma (CA-15)
    Given la existencia de esquemas Zod bidireccionales
    When un atacante bypassea la UI enviando un POST adulterado vía API (Ej: Editando un campo de 'Solo Lectura')
    Then el Backend ejecutará OBLIGATORIAMENTE el mismo `schema.json` Zod utilizado en el diseño.
    And cruzará los permisos de escritura del Rol del usuario contra los campos recibidos; si inyectó datos no autorizados, aplicará un `.strip()` silencioso descartando el campo adulterado, o abortará con `HTTP 403 Forbidden`.
    And rechazará con `HTTP 400 Bad Request` cualquier asimetría de tipos de datos.

  # ==============================================================================
  # D. PERSISTENCIA CQRS Y PROTECCIÓN DE CAMUNDA ENGINE
  # ==============================================================================
  Scenario: Exclusión Topológica Estratégica de Camunda y ACID Fallback (CA-16)
    Given el cierre exitoso de la transacción CQRS (Guardado del Evento Inmutable)
    When el Backend notifica a Camunda 7 para avanzar el Token BPMN (`taskService.complete()`)
    Then el Backend TIENE ESTRICTAMENTE PROHIBIDO empujar el Payload masivo (Textos largos, JSONs) hacia la tabla `ACT_RU_VARIABLE` del Engine.
    And solo enviará un DTO minificado con las variables lógicas requeridas por los Gateways.
    And si Camunda sufre Timeout (HTTP 5xx), el Backend aplicará un Rollback estricto de la transacción CQRS (Saga) y devolverá HTTP 500 Crudo, previniendo falsos positivos de guardado en UI.

  Scenario: Consistencia Eventual UX y Read-Your-Own-Writes (RYOW) (CA-17)
    Given que el POST a `/complete` finaliza exitosamente (HTTP 200 OK)
    Then el Frontend ejecutará síncronamente una purga, destruyendo la llave del borrador en el `LocalStorage`.
    And eliminará proactivamente esa tarea específica del Store en RAM (Pinia) del Workdesk ANTES de redirigir al usuario al Home (RYOW).
    And esto garantizará que el usuario no vea su tarea "ya completada" flotando como un fantasma en su bandeja por culpa de la latencia CQRS.
    
  Scenario: Integridad de Asignación Concurrente (Implicit Locking) (CA-18)
    Given que una tarea "TK-400" está asignada explícitamente a `maria.perez`
    When `pedro.gomez` intercepta vulnerablemente la URL e intenta someter un POST a `/tasks/TK-400/complete`
    Then el Core iBPMS examina deductivamente el `assignee` de la tarea contra la identidad central del Security Context.
    And aborta transaccionalmente la colisión inyectando un lapidario `HTTP 403 Forbidden`.

```
**Trazabilidad UX:** Wireframes Pantalla 2 (Vista de Detalle / Formulario Dinámico).

---

### US-039: Formulario Genérico Base (Pantalla 7.B - El Camaleón Operativo)
**Como** PMO / Owner del iBPMS
**Quiero** disponer de un modelo de formulario genérico pre-asociado a tareas operativas simples
**Para** no invertir tiempo dibujando decenas de formularios básicos en la Pantalla 7 cuando la actividad es netamente procedimental (captura de evidencia, observaciones y tracking de avance).

> [!IMPORTANT]
> **Dependencias Externas Críticas de la US-039:**
> - **US-003 (Pantalla 7 / IDE):** El Pre-Flight Analyzer que decide si un formulario genérico es admisible reside en la lógica de despliegue compartida con el IDE de formularios.
> - **US-005 (Despliegue BPMN):** El Pre-Flight Analyzer se ejecuta durante el pipeline de despliegue del BPMN (Pantalla 6). La whitelist configurable (CA-5) es una propiedad del Process Definition.
> - **US-029 (Persistencia CQRS):** El auto-guardado de borradores (CA-7) consume los mismos endpoints de draft que los iForm Maestros.
> - **US-036 (RBAC / Pantalla 14):** La lista de Roles VIP que bloquean el uso del formulario genérico (CA-6) se administra desde la columna `is_vip_restricted` en `ibpms_roles`.
> - **US-034 (RabbitMQ):** Los Error Events disparados por los Botones de Pánico (CA-8) se enrutan a través del broker de mensajería para el procesamiento asíncrono.

> [!CAUTION]
> **HANDOFF TÉCNICO V1 (QA SRE CERTIFIED):**
> 1. Eliminación y prohibición del uso de variables de tipo `Toggle` binario (ej. `requiere_evidencia`) como lógica de UI en este documento, usando en su lugar un enfoque semántico estructural sin ambigüedades.
> 2. Prevención de colisiones de Namespace garantizada mediante inyección de `Whitelist Regex` en el BFF, evitando envenenamiento de los context variables del Engine.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Auto-vinculación Camaleónica y Resiliencia de Pantalla 7.B

  Scenario: Inyección Explícita (Anti-Bypass) y Restricción VIP (Pre-Flight) (CA-1)
    Given la necesidad de usar el Formulario Genérico Base (`sys_generic_form`) en un BPMN
    When el Arquitecto lo selecciona en el Dropdown de la `UserTask` en la Pantalla 6
    Then el Pre-Flight Analyzer auditará el Rol y la criticidad de esa tarea.
    And si el Rol está tipificado estructuralmente como "Alta Dirección", "Aprobador Financiero" o la tarea exige "Sello Legal", el Pre-Flight BLOQUEARÁ el despliegue (❌ Hard-Stop).
    And prohibirá usar el formulario genérico, forzando la creación de un iForm Maestro formal (Pantalla 7) que cumpla con los estándares pesados de auditoría.
    And para tareas Kanban huérfanas, el sistema inyectará la Pantalla 7.B silenciosamente.

  Scenario: Prevención de Context Bleeding (Filtro Anti-Basura BFF) (CA-2)
    Given un operario que apertura una tarea operativa con el Formulario Genérico
    When el BFF (Backend for Frontend) compila el DTO de inicialización (`prefillData`)
    Then el Backend aplicará un `Whitelist Regex` o filtro estricto sobre el Payload de Camunda.
    And extraerá y enviará EXCLUSIVAMENTE los metadatos de negocio vitales (Ej: `Case_ID`, `Client_Name`, `Priority`, `SLA`), ocultando las 200+ variables técnicas transaccionales del proceso.
    And el Frontend renderizará la Pantalla 7.B coronada por una cuadrícula superior de Solo Lectura ultraligera, evitando la sobrecarga cognitiva del operario.

  Scenario: Mutación Camaleónica de Interfaz y Botón de Pánico (Error Event) (CA-3)
    Given la renderización de la Pantalla 7.B
    When el operario deba escalar o devolver transversalmente el ticket (Ej: Evidencia Insuficiente)
    Then la interfaz exhibirá, además del recuadro principal, un bloque inferior de "Excepciones" o Botones de Pánico (Aprobado / Retorno al Generador / Cancelar).
    And al cliquear un botón de pánico, el Frontend forzará procesalmente la inyección de una observación justificativa mandatoria (Min: 20 caracteres) antes de consumar un Error Event o Escalamiento en el Motor de Camunda.


  # ==============================================================================
  # B. REMEDIACIONES POST-AUDITORÍA (Sprint Remediation Brief 2026-04-05)
  # Origen: docs/requirements/us039_functional_analysis.md
  # Tickets: REM-039-01 a REM-039-05
  # Propósito: Cerrar GAPs de implementación detectados por el workflow
  #            /analisisEntendimientoUs.md tras finalizar las 17 iteraciones
  #            de la Auditoría Integral del Backlog.
  # ==============================================================================

  Scenario: [REMEDIACIÓN] Definición del Cuerpo Editable del Formulario Genérico (CA-4)
    # Origen: REM-039-01 — GAP-1 del us039_functional_analysis.md
    Given la necesidad de que el operario capture evidencia, observaciones y tracking de avance en la Pantalla 7.B
    Then el cuerpo editable del Formulario Genérico Base contendrá OBLIGATORIAMENTE los siguientes campos pre-construidos:
    And 1. `textarea` "Observaciones / Notas del Operario" (obligatorio, min 10 chars, max 2000 chars) como campo principal de captura de texto libre.
    And 2. `dropzone` "Adjuntos de Evidencia" (opcional, max 5 archivos, max 10MB por archivo, tipos permitidos: PDF, JPG, PNG, DOCX, XLSX) para carga drag-and-drop de documentos de soporte.
    And 3. `select` "Resultado de la Gestión" (obligatorio, opciones configurables por proceso: Ej: "Aprobado", "Rechazado", "Pendiente de Información", "Escalado") como clasificador estandarizado del outcome de la tarea.
    And estos tres campos son el set mínimo fijo; TIENE PROHIBIDO agregar campos adicionales en runtime porque para formularios complejos se debe usar un iForm Maestro (US-003).
    And la estructura visual será: [Cuadrícula Metadatos Solo-Lectura] arriba, [Cuerpo Editable: Observaciones + Adjuntos + Resultado] al centro, [Botones de Pánico] abajo.

  Scenario: [REMEDIACIÓN] Configuración de Whitelist Regex por Proceso (CA-5)
    # Origen: REM-039-02 — GAP-2 del us039_functional_analysis.md
    Given la exigencia de filtrar variables técnicas de Camunda mediante Whitelist Regex (CA-2)
    Then la Whitelist será configurable POR PROCESO, no global, para soportar que cada BPMN tenga variables de negocio distintas (Ej: Proceso A usa `Case_ID`, Proceso B usa `Folio_Number`).
    And la configuración se realizará en la Pantalla 6 (Modeler BPMN) como una propiedad del Process Definition, en un panel "Variables Visibles en Formulario Genérico".
    And el Arquitecto del BPMN podrá definir una lista de hasta 10 claves de variables permitidas (Ej: `Case_ID, Client_Name, Priority, SLA, Due_Date`).
    And si NO se configura ninguna whitelist, el BFF aplicará un fallback seguro mostrando SOLO las 4 variables por defecto: `Case_ID`, `Instance_Name`, `Priority` y `Created_At`.
    And TIENE PROHIBIDO mostrar variables con prefijo `_internal_`, `camunda_`, o `zeebe_` independientemente de la whitelist configurada.

  Scenario: [REMEDIACIÓN] Catálogo Configurable de Roles VIP para Bloqueo Pre-Flight (CA-6)
    # Origen: REM-039-03 — GAP-3 del us039_functional_analysis.md
    Given la restricción de que tareas VIP no pueden usar el Formulario Genérico (CA-1)
    Then la lista de Roles VIP que disparan el Hard-Stop del Pre-Flight Analyzer será configurable desde la Pantalla 14 (RBAC) y NO hardcodeada en el código.
    And la tabla `ibpms_roles` incluirá una columna booleana `is_vip_restricted` (default: false) que el Super Admin activará para los roles que NO deben operar con formularios genéricos.
    And los tres roles mencionados en el CA-1 ("Alta Dirección", "Aprobador Financiero", "Sello Legal") serán marcados como `is_vip_restricted = true` durante el seed de datos inicial del sistema.
    And el Pre-Flight Analyzer consultará esta tabla en tiempo de despliegue del BPMN para evaluar si las UserTasks asignadas a esos carriles (Lanes) pueden usar `sys_generic_form`.

  Scenario: [REMEDIACIÓN] Persistencia y Auto-Guardado del Formulario Genérico (CA-7)
    # Origen: REM-039-04 — GAP-4 del us039_functional_analysis.md
    Given que el operario puede redactar observaciones extensas en el formulario genérico
    Then el Formulario Genérico consumirá los mismos endpoints de borrador definidos en la US-029 (Persistencia CQRS):
    And `POST /api/v1/drafts/{taskId}` para auto-guardado cada 30 segundos o al detectar inactividad de teclado (debounce 10s).
    And `GET /api/v1/drafts/{taskId}` para recuperar el borrador al reabrir la tarea.
    And `DELETE /api/v1/drafts/{taskId}` para limpiar el borrador tras submit exitoso.
    And si el operario cierra la pestaña accidentalmente, al reabrir la tarea encontrará un banner: "Se detectó un borrador no enviado. ¿Desea restaurarlo?" (mismo patrón del CA-85 de US-003).
    And los datos finales de submit (observaciones + adjuntos + resultado) se persistirán como variables del proceso en Camunda mediante `runtimeService.setVariables()`.

  Scenario: [REMEDIACIÓN] Mapeo Explícito de Botones de Pánico a Eventos BPMN (CA-8)
    # Origen: REM-039-05 — GAP-5 del us039_functional_analysis.md
    Given los tres Botones de Pánico del Formulario Genérico (CA-3)
    Then cada botón tendrá un comportamiento BPMN estrictamente definido:
    And Botón "Aprobado": Invoca `taskService.complete(taskId, variables)` inyectando `generic_form_result = "APPROVED"` como variable del proceso. El flujo continúa normalmente por el Sequence Flow default.
    And Botón "Retorno al Generador": Invoca `taskService.complete(taskId, variables)` inyectando `generic_form_result = "RETURNED"`. El BPMN DEBE tener un Exclusive Gateway posterior que evalúe esta variable para redirigir el token a la tarea anterior del flujo. Si el Gateway no existe, la tarea se completa sin retorno (fail-safe).
    And Botón "Cancelar": Invoca un BPMN Error Event con `errorCode = "TASK_CANCELLED_BY_OPERATOR"`. El BPMN DEBE tener un Error Boundary Event capturando este código. Si no existe el Boundary Event, Camunda propagará el error al proceso padre o a la morgue de incidentes (Incident).
    And los tres botones comparten la precondición del CA-3: observación justificativa de min 20 caracteres obligatoria ANTES de ejecutar cualquier acción.

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

> [!IMPORTANT]
> **Dependencias Externas Críticas de la US-005:**
> - **US-003 (Pantalla 7 / IDE Formularios):** Los FormKeys del Dropdown (CA-39) consumen el catálogo de formularios. La consistencia Simple/Maestro (CA-40) es dictada por US-003. El Pre-Flight valida integridad del mapping contra variables Zod del formulario (CA-68).
> - **US-007 (DMN / Pantalla 4):** El Dropdown de Business Rule Tasks (CA-61) consume tablas DMN creadas en US-007. El binding LATEST/DEPLOYMENT (CA-12) es co-responsabilidad.
> - **US-033 (Hub de Integraciones / Pantalla 11):** El Dropdown de conectores API (CA-45) consume los conectores registrados en US-033. La inmutabilidad de Swagger (CA-52) es co-responsabilidad. El catálogo de Topics (CA-70) se administra desde Pantalla 11.
> - **US-036 (RBAC / Pantalla 14):** Los roles Designer/Release Manager (CA-21) y los roles autogenerados desde Lanes (CA-6) se administran en Pantalla 14.
> - **US-029 (Persistencia CQRS):** El auto-guardado de borradores (CA-19) consume los endpoints de draft. Las variables de formulario persistidas validan contra el mapping del BPMN.
> - **US-034 (RabbitMQ):** El Retry Pattern (CA-58) de Service Tasks procesa vía colas. Los reintentos automáticos dependen de la taxonomía de prioridad (US-034 CA-6).
> - **US-000 (Resiliencia Integrada):** La morgue de tokens / Centro de Incidentes (CA-13) reside en la capa de resiliencia. Las instancias Sandbox (CA-67) son visibles en Pantalla 15.A.
> - **US-049 (Notificaciones):** Las notificaciones de aprobación/rechazo de despliegue (CA-69) se canalizan vía el sistema de notificaciones.
> - **US-039 (Formulario Genérico):** El Pre-Flight Analyzer que bloquea el uso de `sys_generic_form` en tareas VIP (CA-1 de US-039) reside en el pipeline de despliegue de US-005.

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

---refinamiento---
Scenario: Versionamiento Seguro de Reglas DMN (Protección de Derechos Adquiridos) (CA-12)
    Given un proceso V1 con tokens en vuelo que se aproxima a una Business Rule Task (DMN)
    When el Director de Riesgos publica una nueva versión de la tabla DMN (V2)
    Then el Arquitecto BPMN DEBE haber configurado previamente en el Modeler si la compuerta usa `Binding: LATEST` o `Binding: DEPLOYMENT`.
    And si elige `LATEST`, el motor evaluará con la nueva V2 publicada (Late Binding).
    And si elige `DEPLOYMENT` (Por defecto), el motor evaluará EXCLUSIVAMENTE contra la versión exacta de la DMN que estaba activa en el milisegundo en que nació el caso (Deployment Binding).
    And garantizando así la protección jurídica y previniendo rechazos ilegales a clientes por cambiar las "reglas del juego" a mitad del trámite.

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

Scenario: Mapeo Obligatorio de Parámetros en Subprocesos (In/Out Mapping) (CA-60)
    Given el Arquitecto arrastra una `Call Activity` (Llamar Proceso Hijo) al lienzo en la Pantalla 6
    When intenta configurar las propiedades de este nodo
    Then el sistema TIENE ESTRICTAMENTE PROHIBIDO permitir el despliegue si no se configura la matriz de "In/Out Mapping".
    And el panel debe obligar a mapear "Qué variables le entrego al hijo al nacer" (Input) y "Qué variables me devuelve el hijo al terminar" (Output).
    And si el Arquitecto deja esta matriz vacía, el Pre-Flight Analyzer bloqueará el despliegue (❌) con el error: "El Subproceso nacerá ciego por falta de datos".

Scenario: Vinculación Estricta del Cerebro Lógico (Business Rule Task a DMN) (CA-61)
    Given el Arquitecto arrastra un nodo `Business Rule Task` (Regla de Negocio) para evaluar una decisión
    When configura las propiedades del nodo
    Then el panel de propiedades NO permitirá escribir código libre.
    And mostrará un Dropdown obligatorio llamado `[ 🧠 Tabla de Decisión (Decision_Ref) ]` que lista todas las tablas DMN creadas en la Pantalla 4 (US-007).
    And si el nodo no tiene una tabla DMN amarrada, el Pre-Flight Analyzer abortará el despliegue a Producción (❌).

Scenario: Prohibición de Trabajo Síncrono en Camunda (External Task Pattern) (CA-62)
    Given el Arquitecto configura un nodo automático como una `Service Task` o `Send Task`
    When el motor procesa el XML para el despliegue
    Then la arquitectura del iBPMS TIENE ESTRICTAMENTE PROHIBIDO usar `Java Delegates` o expresiones síncronas que ejecuten código pesado dentro del hilo (Thread) principal de Camunda.
    And el motor forzará estructuralmente el uso del patrón `External Task` (Trabajadores Externos).
    And Camunda simplemente publicará la intención de trabajo en un Topic (Ej: `topic="generar_pdf"`), liberando su memoria inmediatamente, a la espera de que los microservicios satélite (Workers) hagan el trabajo pesado y reporten el resultado asíncronamente.



  # ==============================================================================
  # B. REMEDIACIONES POST-AUDITORÍA (Sprint Remediation Brief 2026-04-05)
  # Origen: docs/requirements/us005_functional_analysis.md
  # Tickets: REM-005-01 a REM-005-06
  # Propósito: Cerrar GAPs de implementación detectados por el workflow
  #            /analisisEntendimientoUs.md tras finalizar las 17 iteraciones
  #            de la Auditoría Integral del Backlog.
  # ==============================================================================

Scenario: Aislamiento Transaccional del Sandbox en Producción (Zero-Blast Radius) (CA-63)
    Given la ejecución de una simulación de proceso directamente en Producción (Modo Sandbox activado)
    When el token simulado alcanza una `ServiceTask` externa (Hub US-033) o una `SendTask` (Correos US-049)
    Then el Engine inyectará obligatoriamente una variable/header oculto en el contexto: `X-Sandbox-Mode: true`.
    And los Workers de Integración y Notificaciones interceptarán esta bandera de forma imperativa.
    And ABORTARÁN cualquier petición de red HTTP o envío de correo SMTP real.
    And devolverán un `Mock Response` (HTTP 200 OK simulado) al motor Camunda, protegiendo a los clientes y sistemas ERP de recibir basura transaccional durante las pruebas del Arquitecto.

Scenario: Intervención de Emergencia sobre Bloqueo Pesimista (Break-Lock)  (CA-64)
    Given un proceso BPMN bloqueado para edición por el "Lock Pesimista" de un Arquitecto ausente o cuyo PC falló
    When la parálisis del lienzo afecta el mantenimiento y un usuario con el rol `Super_Admin` accede al Catálogo (Pantalla 6)
    Then el sistema le habilitará un botón de emergencia rojo `[ 🔓 Romper Candado (Break-Lock) ]`.
    And al ejecutarlo, el Backend destruirá el lock en la Base de Datos, liberando el proceso para edición inmediata.
    And registrará inamoviblemente en el Audit Log quién y cuándo forzó la liberación del diseño corporativo retenido por otro empleado.

  Scenario: [REMEDIACIÓN] Contrato API Explícito para el Endpoint de Despliegue (CA-65)
    # Origen: REM-005-01 — GAP-1 del us005_functional_analysis.md
    Given la necesidad de alinear Frontend y Backend en el contrato de despliegue BPMN (CA-1)
    Then el endpoint `POST /api/v1/design/processes/deploy` aceptará un `multipart/form-data` con los siguientes campos:
    And Campo obligatorio `file` (tipo: file, extensión: `.bpmn`, max: 5MB) — el diagrama BPMN 2.0 XML.
    And Campo obligatorio `deploy_comment` (tipo: string, min: 10 chars) — justificación del despliegue para el audit log.
    And Campo opcional `force_deploy` (tipo: boolean, default: false) — si `true`, salta las advertencias ⚠️ del Pre-Flight (pero NO los errores ❌).
    And el Response Body del `201 Created` incluirá obligatoriamente: `deployment_id`, `process_definition_id`, `process_definition_key`, `version` (int), `deployed_at` (ISO 8601 UTC), `deployed_by` (user_id).
    And existirá un endpoint separado de validación: `POST /api/v1/design/processes/validate` que ejecuta el Pre-Flight Analyzer sin desplegar, retornando la lista de errores y advertencias en formato JSON.
    And el contrato se documentará con OpenAPI/Swagger annotations en el Controller.

  Scenario: [REMEDIACIÓN] Persistencia del Lock Pesimista en Base de Datos (CA-66)
    # Origen: REM-005-02 — GAP-2 del us005_functional_analysis.md
    Given el mecanismo de Lock Pesimista para edición concurrente (CA-16, CA-43, CA-64)
    Then el lock se persistirá en una tabla `ibpms_process_locks` con columnas: `process_definition_key` (PK), `locked_by` (FK user_id), `locked_at` (timestamp UTC), `browser_session_id` (para detectar tabs cerradas).
    And el lock aplica por `process_definition_key` (todo el proceso, no por versión específica).
    And el lock NO expiará automáticamente por tiempo (consistente con CA-43) pero SÍ se liberará automáticamente si el Backend detecta que la sesión WebSocket/SSE del navegador del Arquitecto se desconecta (heartbeat cada 30 segundos).
    And si el heartbeat falla 3 veces consecutivas (90 segundos sin respuesta), el lock se libera automáticamente y se registra en `ibpms_audit_log`: "[AUTO-RELEASE] Lock del proceso X liberado por desconexión del usuario Y".
    And el Break-Lock de emergencia (CA-64, rol Super Admin) actualizará la misma tabla y registrará quién forzó la liberación.
    And al reiniciar el servidor de aplicación, los locks persistidos en BD sobreviven y siguen vigentes.

  Scenario: [REMEDIACIÓN] Límites y Gobernanza del Sandbox en Producción (CA-67)
    # Origen: REM-005-03 — GAP-3 del us005_functional_analysis.md
    Given la ejecución de instancias Sandbox directamente en el motor de producción (CA-20, CA-41, CA-63)
    Then el sistema impondrá un límite máximo de 3 instancias Sandbox concurrentes a nivel global del sistema.
    And si un Arquitecto intenta iniciar una cuarta simulación, el sistema la rechazará con el mensaje: "Límite de Sandbox alcanzado (3/3). Espere a que finalice una simulación en curso."
    And cada instancia Sandbox tendrá un timeout de auto-destrucción de 10 minutos. Si el token no ha completado su recorrido en ese tiempo, el motor la anulará automáticamente y registrará: "[SANDBOX-TIMEOUT] Instancia sandbox {id} destruida por timeout (10min)."
    And las instancias Sandbox serán visibles en la Pantalla 15.A (Centro de Incidentes) con un badge visual "[🧪 SANDBOX]" para diferenciarlas de instancias reales, pero NO se mostrarán en los dashboards operativos del Workdesk.
    And el contador de instancias Sandbox activas se almacenará en Redis (`ibpms:sandbox:count`) con TTL de 15 minutos como failsafe.

  Scenario: [REMEDIACIÓN] Persistencia del Data Mapping como Extension Properties del BPMN XML (CA-68)
    # Origen: REM-005-04 — GAP-4 del us005_functional_analysis.md
    Given la configuración del DataMapperGrid (CA-49 a CA-57) donde el Arquitecto mapea variables visualmente
    Then el mapping finalizado se persistirá como `camunda:inputOutput` extension properties dentro del nodo `ServiceTask` del XML BPMN, garantizando portabilidad del diagrama.
    And adicionalmente, se almacenará una copia indexada del mapping en la tabla `ibpms_data_mappings` (columnas: `process_definition_key`, `task_id`, `connector_id`, `mapping_json`, `last_validated_at`) para consultas rápidas y validación cruzada.
    And si el Arquitecto modifica el formulario en la Pantalla 7 (US-003) y elimina o renombra una variable Zod que está referenciada en un mapping existente, el Pre-Flight Analyzer lo detectará como Error ❌: "Variable '{varName}' referenciada en el mapping de la tarea '{taskName}' ya no existe en el formulario."
    And el Pre-Flight Analyzer validará la integridad de TODOS los mappings del BPMN antes de permitir el despliegue.

  Scenario: [REMEDIACIÓN] Flujo Completo de Solicitud de Despliegue con Rechazo y Notificación (CA-69)
    # Origen: REM-005-05 — GAP-5 del us005_functional_analysis.md
    Given el workflow de Solicitud de Despliegue del Designer al Release Manager (CA-34)
    Then la solicitud se implementará como un registro en la tabla `ibpms_deploy_requests` (columnas: `id`, `process_definition_key`, `requested_by`, `requested_at`, `status` ENUM: PENDING/APPROVED/REJECTED, `reviewed_by`, `reviewed_at`, `review_comment`).
    And al presionar [📩 Solicitar Despliegue], se creará una tarea visible en el Workdesk del Release Manager con los botones [🚀 Aprobar y Desplegar] y [❌ Rechazar].
    And al Rechazar, el Release Manager TIENE OBLIGACIÓN de ingresar un comentario de rechazo (min 20 chars) explicando qué debe corregir el Designer.
    And el Designer recibirá una notificación (bell icon + email vía US-049) informando si su solicitud fue aprobada o rechazada, junto con el comentario del Release Manager.
    And existirá un historial visible en la Pantalla 6: "[📜 Historial de Solicitudes]" listando todas las solicitudes anteriores con su estado, revisor y comentario.

  Scenario: [REMEDIACIÓN] Catálogo de External Task Topics con Validación Pre-Flight (CA-70)
    # Origen: REM-005-06 — GAP-6 del us005_functional_analysis.md
    Given la obligatoriedad de External Task Pattern (CA-62) donde cada Service Task se suscribe a un Topic
    Then el sistema mantendrá un catálogo oficial de Topics en la tabla `ibpms_external_task_topics` (columnas: `topic_name`, `description`, `worker_class`, `is_active`, `registered_at`).
    And el campo Topic en las propiedades de la Service Task (Pantalla 6) será un Dropdown que consume este catálogo, NO un campo de texto libre.
    And los Topics pre-registrados obligatorios para V1 serán: `ibpms.send_email` (US-049), `ibpms.sync_erp` (NetSuite), `ibpms.sync_sharepoint`, `ibpms.generate_pdf`, `ibpms.ai_copilot` (US-017), `ibpms.webhook_outbound` (US-004).
    And el Pre-Flight Analyzer validará que cada Service Task del BPMN tenga un Topic que exista en el catálogo. Si el Topic no existe, emitirá Error ❌: "La tarea '{taskName}' refiere al topic '{topicName}' que no está registrado en el catálogo de Workers."
    And el Administrador IT podrá registrar nuevos Topics desde una sección administrable en la Pantalla 11 (Hub de Integraciones).


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

  Scenario: Tipificación Estricta de Plantilla (Tradicional vs Ágil) (CA-3)
    Given que la PMO acciona la creación de una Nueva Plantilla en la Pantalla 8
    When el sistema levanta el Modal de Creación
    Then obliga explícitamente a clasificar la plantilla seleccionando un tipo rígido: `[Tradicional (Gantt)]` o `[Ágil (Sprints)]`
    And esta clasificación gobierna el comportamiento del lienzo: Si elije "Ágil", el botón de relacionar dependencias (Fin-a-Inicio) desaparece permanentemente del UI y se prohíbe crear conceptos estructurales como "Hitos".

  Scenario: Transición Formulario a DONE en Ágil (CA-4)
    Given una tarea instanciada en el Tablero Kanban (Ágil) originada desde una Plantilla
    And esta tarea tiene el "Formulario_QA" asociado en su definición maestra
    When el desarrollador termina el trabajo y oprime enviar el formulario
    Then el sistema autoevalúa la completitud de la data y, en caso de éxito, arrastra logísticamente la tarjeta a la columna "DONE" del Sprint, aplicando un Definition of Done duro atado a data.

  Scenario: Independencia Evolutiva Locativa (CA-5)
    Given un Scrum Master que instanció un Proyecto Ágil basado en la Plantilla V1.0
    When el Scrum Master elimina 5 de las tareas heredadas del Backlog local del proyecto porque no aplican a su Sprint
    Then el borrado es estrictamente Local (Muta solo el Proyecto Instanciado)
    And la Plantilla original inmutable "V1.0" no pierde las tareas orgánicamente y futuros proyectos las seguirán heredando intactas.

```
**Trazabilidad UX:** Wireframes Pantalla 8 (Project Template Builder).


---



### US-027: Copiloto IA (Auditoría ISO 9001 y Generador Consultivo BPMN)
**Como** Arquitecto Modelador de Procesos
**Quiero** un asistente IA interactivo embebido en el diseñador (Pantalla 6)
**Para** que audite mis diagramas buscando brechas de calidad (ISO 9001 y BPMN 2.0), O genere un proceso BPMN 2.0 desde cero a partir de documentos adjuntos e iteraciones de preguntas aclaratorias en lenguaje natural.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: AI Copilot Generator, SRE Layout, AppSec & UX Governance

  # ==============================================================================
  # A. EFICIENCIA SRE, AUTO-LAYOUT Y AHORRO DE TOKENS (GAPs 18, 19, 20)
  # ==============================================================================
  Scenario: Soberanía Geométrica y Prevención de Spaghetti (Auto-Layout) (CA-01)
    Given la incapacidad de los LLMs para calcular coordenadas (X,Y) espaciales precisas
    When el Agente IA genera el proceso (Hit-The-Canvas)
    Then el LLM devolverá EXCLUSIVAMENTE el marcado lógico semántico (`<bpmn:process>`).
    And la arquitectura PROHÍBE que el LLM calcule topología `BPMNDi`.
    And un Middleware Backend (Librería de Auto-Layout) inyectará matemáticamente las coordenadas X,Y antes de enviarlo al Frontend, previniendo el colapso geométrico del navegador.

  Scenario: Minificación de Tokens Prompeados (Context Optimization) (CA-02)
    Given el Arquitecto solicita a la IA "Auditar" el diagrama actual (ISO 9001) o extenderlo
    Then el Backend TIENE PROHIBIDO enviar el XML crudo con coordenadas al LLM.
    And decantará el XML a un JSON semántico ligero (puramente Nodos y Flujos), abaratando el costo de la facturación Cloud (Tokens) en un 70%.

  Scenario: Ingesta Documental Asíncrona, Multimodal y Antivirus (CA-03)
    Given que el Arquitecto sube un PDF/DOCX o Imagen de flujograma al Dropzone (Max 5 archivos / 100 págs)
    Then el Frontend mostrará una métrica de límite dinámico (Ej: `Páginas: 45/100`).
    And el archivo pasará por un escáner Anti-Malware (ClamAV Cloud) en milisegundos.
    And la extracción de texto y visión multimodal (GPT-4V/Tika) se delegará a una Cola de RabbitMQ con WebWorkers, sin saturar los Hilos HTTP.

  # ==============================================================================
  # B. SEGURIDAD APPSEC, RAG POISONING Y DO-W
  # ==============================================================================
  Scenario: RAG Efímero, Aislamiento Vectorial y Anti-Poisoning (CA-04)
    Given la vectorización de documentos en `pgvector`
    Then los TextChunks nacerán con un `Time-To-Live (TTL)` efímero atado a la sesión del Chat.
    And toda consulta a la base vectorial incluirá el `tenant_id` y `session_id` obligatoriamente.
    And al cerrar el diseñador, la base vectorial y los archivos en S3 se autodestruirán, previniendo RAG Poisoning corporativo transversal y el "Embedding Bloat".

  Scenario: Mitigación Denial of Wallet (DoW) y Prevención XSS/Prompt Injection (CA-05)
    Given la exposición del Endpoint del LLM a los empleados
    Then el API Gateway impondrá Rate Limiting estricto (Ej: Max 5 generaciones/min).
    And el Backend seudonimizará los nombres de tareas (PII) antes de enviarlos al LLM.
    And el Frontend aplicará `DOMPurify` brutal sobre el XML entrante para evitar Cross-Site Scripting (XSS) reflectivo.
    And si el Backend detecta "Prompt Injection" intencional 3 veces consecutivas, castigará al usuario revocando dinámicamente el `ROLE_PROCESS_ARCHITECT` y alertará al CISO.

  # ==============================================================================
  # C. RESTRICCIONES BPMN Y COMPORTAMIENTO COGNITIVO
  # ==============================================================================
  Scenario: Topología Restringida, Traducción Activa y Manejo de Bucles (CA-06)
    Given la generación de XML a partir de NLP
    Then el Agente estará limitado en V1 a instanciar: `UserTasks`, `ServiceTasks`, `Gateways` y `ErrorBoundaryEvents` para planes B.
    And tiene PROHIBIDO generar Sub-Procesos Embebidos (`CallActivities`) o Eventos de Señal complejos.
    And ante directivas de "Repetir proceso", dibujará un `SequenceFlow` en reversa (Loop), prohibiendo la duplicación lineal.
    And sin importar el idioma del PDF (Inglés/Mandarín), generará el XML y el Chat estrictamente en Español (Traducción Activa).

  Scenario: Triage Conversacional, Píldoras Rápidas y Roles Faltantes (CA-07)
    Given que el LLM detecta contradicciones documentales o roles inexistentes en EntraID
    When la IA pausa la inyección y genera una consulta (Triage)
    Then dosificará las preguntas (Máx 3 por lote) y ofrecerá "Píldoras de Respuesta Rápida" (Ej: `[Usar Rol Existente]`, `[Omitir]`).
    And si debe crear un rol nuevo, usará un ID temporal (Ej: `rol_dummy`) e inyectará un `TextAnnotation` (Nota Adhesiva) recordando al humano crearlo.
    And los Gateways dibujados por IA NO tendrán expresiones matemáticas inyectadas, delegando esa lógica al humano.

  # ==============================================================================
  # D. UX, RECUPERABILIDAD Y PREVENCIÓN DE ERRORES
  # ==============================================================================
  Scenario: UX No Bloqueante, Transmutación Visual y Undo Atómico (CA-08)
    Given el evento Hit-the-Canvas y la espera de respuestas
    Then el Chat NO bloqueará el Canvas (Modal Overlay prohibido); el usuario mantendrá capacidades de `Drag to Pan` y `Zoom` libremente.
    And las nuevas cajas inyectadas brillarán con un "Halo Verde" efímero.
    And si el humano presiona `CTRL + Z`, el framework revertirá atómicamente (en 1 solo paso) toda la inyección de la IA.
    And la IA aplicará "Smart Merge", respetando las cajas que el humano haya borrado a mano previamente.
    And si el chat está minimizado, un Badge Rojo y un PING sonoro alertarán de preguntas pendientes.

  Scenario: Tolerancia Humana a ISO 9001 y Limpieza de Notas (CA-09)
    Given las alertas ISO 9001 (con Popovers gráficos de Antes/Después) y Notas Adhesivas en el lienzo
    When el Arquitecto ignora una alerta visual editando otras partes 3 veces consecutivas
    Then el Copiloto desistirá asumiendo la responsabilidad humana (Override), silenciando la alerta visual y guardando el log forense con tipografía en **Negritas** para decisiones categóricas.
    And si el Frontend detecta que el humano vinculó el Formulario (Pantalla 7) esperado, borrará automáticamente la Nota Adhesiva obsoleta asociada.

  Scenario: El Antídoto contra el Despliegue Fantasma (Executable Flag) (CA-10)
    Given que la IA generó un flujo con caminos lógicos rotos o inconclusos
    When el XML incluya la etiqueta `<bpmn:process isExecutable="false">`
    Then el Frontend cruzará esta bandera e imprimirá un Banner Bloqueante Rojo sobre el Canvas dictando: "Diseño Corrompido por la IA. Repare el Nodo [ID] antes de desplegar".
    And el botón `[🚀 DESPLEGAR]` (US-005) permanecerá físicamente inhabilitado.
```
**Trazabilidad UX:** Wireframes Pantalla 6 (Diseñador BPMN - Panel Lateral de Copilot interactivo y Dropzone).

---

## ÉPICA 5: Modelado de Reglas de Negocio con IA (DMN)
Permite a los usuarios de negocio (no técnicos) generar reglas lógicas complejas utilizando lenguaje natural.


### US-007: Generador Cognitivo de DMN (NLP a Tablas de Decisión)
**Como** Arquitecto de Procesos / Usuario de Negocio
**Quiero** escribir políticas de negocio en lenguaje natural (ej. "Aprobar si monto < 1000")
**Para** que el iBPMS las traduzca de forma segura, asíncrona y estructurada a una tabla matemática DMN (Hit Policy: FIRST), erradicando la ambigüedad humana sin exponer datos PII a modelos LLM externos y protegiendo el performance del servidor.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: NLP to DMN Translation, SRE Architecture & AppSec Governance

  # ==============================================================================
  # A. SRE, DESEMPEÑO Y ARQUITECTURA CLOUD (Anti-Timeout y Ahorro)
  # ==============================================================================
  Scenario: Streaming Asíncrono de Generación (Server-Sent Events) (CA-01)
    Given la latencia inherente de los modelos fundacionales (LLMs) al generar tablas complejas
    When el usuario envía el Prompt
    Then la arquitectura TIENE PROHIBIDO usar peticiones HTTP síncronas bloqueantes que arriesguen un error `504 Gateway Timeout`.
    And el Backend abrirá un canal de Server-Sent Events (SSE).
    And el Frontend pre-renderizará la grilla DMN visualmente (Fila por Fila) a medida que lleguen los fragmentos, mitigando la ansiedad del usuario mediante un Skeleton Loader.

  Scenario: Mitigación Denial of Wallet (DoW) y Caché Criptográfica (CA-02)
    Given la exposición de la API del LLM a los constructores internos
    Then el API Gateway impondrá un Rate Limiting estricto (Ej: Max 5 generaciones/minuto por usuario) para evitar facturas catastróficas.
    And el Backend calculará el Hash del (Prompt + Diccionario); si existe un match exacto en Redis, devolverá la tabla DMN cacheadada instantáneamente, evadiendo el costo Cloud del LLM.

  Scenario: Garbage Collection y Compresión XML (CA-03)
    Given las múltiples iteraciones (Borradores) que un usuario genera en el Chat
    Then los XMLs temporales NO sellados vivirán en el LocalStorage del Frontend y serán purgados físicamente de PostgreSQL a las 24h mediante un Job.
    And al aprobar (Sellar) la versión final, el Backend purgará todos los espacios en blanco inútiles (XML Minification) antes de hacer COMMIT, reduciendo drásticamente el peso en disco.
    And el Backend despachará un Job asíncrono para inicializar el DMN en Camunda (Warm-Up Cache) eliminando la latencia en frío de la primera ejecución operativa.

  # ==============================================================================
  # B. SEGURIDAD (APPSEC), PRIVACIDAD Y HARDENING
  # ==============================================================================
  Scenario: Sandboxing Anti-RCE y Prevención XSS (CA-04)
    Given la inyección de código por parte de la IA o de un humano en modo desarrollador
    Then el motor FEEL de Camunda será encapsulado en un Sandbox estricto de Java, bloqueando cualquier intento de Ejecución Remota de Código (RCE) o acceso a métodos del sistema.
    And el Middleware envolverá el Prompt en "System Instructions" anti-Jailbreak.
    And el Frontend aplicará escapado HTML estricto (DOMPurify) a toda celda renderizada para prevenir Cross-Site Scripting (XSS).
    And los Secrets de las APIs de IA (OpenAI/Anthropic) vivirán exclusivamente inyectados como Variables de Entorno en el Vault, jamás en base de datos.

  Scenario: Seudonimización PII y Anti-Spoofing Forense (CA-05)
    Given el mapeo de variables estructurales hacia el LLM externo
    Then el Backend seudonimizará los nombres de las variables (Ocultando indicios PII) antes de enviarlos a la nube.
    And al guardar la Auditoría del Prompt, el Backend extraerá la identidad criptográfica del Autor directamente desde el Token JWT, haciendo imposible la falsificación de identidad (Spoofing) desde el cliente.

  Scenario: Inmutabilidad DMN y RBAC (BOLA/IDOR) (CA-06)
    Given una tabla DMN en estado "Activa" o "Aprobada"
    When un atacante o usuario intenta hacer un `PUT` directo al endpoint REST para alterar un valor
    Then el Backend interceptará y arrojará `403 Forbidden` absoluto; cualquier modificación análoga o por IA genera obligatoriamente una V2.
    And al reciclar DMNs globales, el Backend validará que el usuario posea el rol `ROLE_PROCESS_ARCHITECT` y pertenezca al `Tenant_ID` dueño de la regla.

  # ==============================================================================
  # C. INTEGRIDAD MATEMÁTICA Y REGLAS DE NEGOCIO
  # ==============================================================================
  Scenario: Resolución Matemática Segura (Hit Policy: FIRST) y Catch-All (CA-07)
    Given la posibilidad de que el LLM o el humano generen rangos solapados
    When el motor ensamble la tabla DMN final
    Then inyectará estructuralmente la política `Hit Policy = FIRST` (evaluación descendente con parada en la primera coincidencia), erradicando las excepciones de colisión de Camunda (`UNIQUE`).
    And inyectará una Fila Final inamovible (Candado Visual 🔒) actuando como "Catch-All Rule" obligatoria para valores Nulos, enrutando a `[Revisión Humana]`.

  Scenario: Variables Planas, Coerción de Tipos y Prohibición Date-Math (CA-08)
    Given las directrices del Diccionario Zod de la Pantalla 7
    Then la IA tiene prohibido usar "Dot Notation" (Ej: `Cliente.Mora`); procesará exclusivamente variables planas de primer nivel para V1.
    And el Backend validará que los tipos de datos en la regla coincidan con Zod (Type Coercion).
    And la IA tiene prohibido hacer Date-Math (resta de fechas); el Frontend/Zod pre-calculará esos valores enteros antes del flujo.
    And toda evaluación de texto aplicará funciones de normalización `lowercase()` en FEEL para ignorar la sensibilidad a mayúsculas.

  Scenario: Límites Cognitivos, Outputs Atómicos y Validación Inversa (CA-09)
    Given la generación de XML DMN (Vía IA o Modo Desarrollador XML Upload)
    Then la estructura generada exigirá Salidas Atómicas (Máximo 1 Output Compuesto en V1).
    And el tamaño del Prompt será truncado al "Token Limit" para evitar fallas de contexto.
    And el Backend ejecutará un `Overlap Check` y rechazará XMLs con disyunciones vacías.
    And el Backend impondrá un Hard-Stop paramétrico de máximo 50 filas generadas; superarlo bloquea la transacción.

  # ==============================================================================
  # D. EXPERIENCIA DE USUARIO (UX/UI) Y GOBERNANZA
  # ==============================================================================
  Scenario: Ergonomía de Alta Densidad y Rescate (Virtual Scrolling) (CA-10)
    Given el renderizado de una matriz de 50 filas x 10 columnas en la Pantalla 4
    Then Vue 3 implementará `Virtual Scrolling` (DOM perezoso) para evitar congelamientos de RAM en el cliente.
    And la grilla soportará navegación nativa por teclado (Enter, Tab) imitando a MS Excel.
    And el `LocalStorage` rescatará cualquier edición análoga en curso si el usuario cierra la pestaña por accidente.

  Scenario: Explicabilidad Visual (XAI) y Simulador de Decisiones (CA-11)
    Given la incomprensión de sintaxis matemática por usuarios de negocio
    Then la UI inyectará una columna autogenerada "Explainable DMN" que traduzca el código FEEL a oraciones humanas legibles.
    And existirá un botón `[🧪 Probar DMN / Simulator]` donde el usuario podrá digitar variables de prueba (Ej: Mora=500), y el Frontend iluminará visualmente en verde la Fila que Camunda ejecutaría en la vida real.

  Scenario: Contención de Pánico y Trazabilidad del Chat (CA-12)
    Given que el usuario termina de editar la DMN y decide publicar
    When el usuario presiona [Publicar V2]
    Then el Frontend desplegará un Modal Inevitable exigiendo digitar `CONFIRMO_V2` para evitar clics accidentales.
    And existirá un botón de `[ ⏪ Revertir a V1 ]` explícito para rollback rápido.
    And el historial del Chat NLP persistirá visualmente atado a esa Versión, y los colores de la grilla cumplirán la norma WCAG AA para diferenciar celdas hechas por IA vs editadas a mano.
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

  Scenario: [Arquitectura] Prohibición de Motor CMMN y Reglas de Instanciación Ágil (CA-5)
    Given un Scrum Master instanciando un Proyecto derivado de la Plantilla Tipificada "Agile Sprint" (US-006)
    When la plataforma de iBPMS inyecte las tarjetas de tareas ("To Do") en el Motor Transaccional
    Then el Backend prohíbe la creación de diagramas rígidos `.cmmn` 
    And persiste la anatomía transaccional de cada tarea "Ágil" como meros registros de Base de Datos Relacional (`Entities`) enlazados a su Proyecto instanciado, usando el poder crudo de Spring Data JPA.

  Scenario: [Arquitectura] Máquina de Estados Pura (State Machine) frente al Salto Anárquico  (CA-6)
    Given la volatilidad de un Tablero Kanban donde un desarrollador arrastra constantemente su tarjeta ("In Progress" -> "Blocked" -> "In Progress" -> "Done" -> "QA Rejected")
    Then garantizamos una experiencia de usuario sub-segundo sin overhead BPMN
    And el iBPMS procesa estas mutaciones de estado en la Entidad (JPA) a través de una API REST ultra veloz (Ej: `PATCH /api/v1/proyectos/{pid}/kanban/{tid}/state`) y registra todas las transiciones como eventos inmutables en la Tabla de Auditoría general de la plataforma transversal.

  Scenario: [Arquitectura] Event-Driven hacia Modelos Estructurados (Salto Híbrido) (CA-7)
    Given una travesía asíncrona Ágil (La tarea Kanban está en estado "In Progress" o "QA Approval")
    When el negocio requiere para darla por `Done` ejecutar una Macro-Aprobación Estructurada, Secuencial y Gerencial
    Then la mutación del Estado Kanban invoca asíncronamente un "Process Instantiation" aislado del Workflow estructurado (BPMN normal)
    And cuando el flujo clásico de Camunda termine, este orquestador emitirá un evento publicándolo de regreso al componente Ágil marcando la casilla original del Tablero como Finalizada o Aprobada, conectando lo impredecible con lo burocrático de forma pura.

  Scenario: Gobernanza de Estados y Columnas Dinámicas (Opción B)  (CA-8)
    Given la necesidad operativa de adaptar el flujo Kanban añadiendo un nuevo estado al ciclo
    When el usuario presiona el botón "Añadir Columna" en la Pantalla 3
    Then el sistema valida que el usuario ostente exclusivamente el Roll de 'Scrum_Master' o 'Lider_Proyecto' en la tabla de miembros
    And el motor Backend efectúa una validación dura (Hard-Limit) rechazando transacciones que excedan un máximo de 7 columnas por tablero para la Versión 1, previniendo sobrecarga visual.

  Scenario: [Arquitectura] Tabla Polimórfica Única para Consolidación de Esfuerzos (BAM)  (CA-9)
    Given la necesidad corporativa de cruzar costos de horas-hombre transversales en la Pantalla 5
    When un empleado registre 2 horas en una "Tarea BPMN" y 3 horas en una "Tarjeta Kanban"
    Then el Backend prohibe guardar dichas horas en las tablas específicas de cada módulo
    And fuerza al sistema a canalizar el guardado hacia una única tabla polimórfica (`ibpms_time_logs`) 
    And distinguiéndolas únicamente por la columna `reference_type` (`TASK_BPMN`, `TASK_AGILE`, `TASK_GANTT`), simplificando matemáticamente la reportería financiera.

  Scenario: [Arquitectura] Componente Frontend Agnóstico Universal (`<UniversalSlaTimer>`)  (CA-10)
    Given la disparidad visual entre la Bandeja Workdesk (Pantalla 1), el Tablero Ágil (Pantalla 3) y el Gantt Tradicional (Pantalla 10.B)
    When el desarrollador deba mostrar el reloj de SLA o el Timer de "Play/Stop"
    Then el framework del iBPMS le denegará desarrollar HTML/Vue personalizado en cada pantalla
    And lo obligará a instanciar y re-utilizar el micro-componente atómico transversal `<UniversalSlaTimer>`.
    And este componente será "Tonto" (Dumb Component), consumiendo APIs centrales de tiempo sin conocer la naturaleza funcional de la tarea que lo aloja.

  Scenario: [Arquitectura] Inmutabilidad de Costos Incurridos (Anti-Manipulación)  (CA-11)
    Given que el empleado ha presionado "Stop" en su temporizador y la plataforma envía el LOG a la base de datos central
    When el usuario o su jefe intenten editar o borrar ese registro de tiempo (Ej: Modificar de 4 horas a 2 horas)
    Then la API de Time Tracking denegará el Método DELETE/PUT (Comportamiento *Append-Only*)
    And el log se convertirá en un asiento financiero inmutable; las correcciones solo podrán hacerse añadiendo asientos contables en negativo mediante un proceso de auditoría superior manual.
```
**Trazabilidad UX:** Wireframes Pantalla 3 (Tableros de Proyecto Kanban).

---


### US-030: Instanciar y Planificar un Proyecto Ágil (Sprints/Kanban)
**Como** Scrum Master / Agile Coach
**Quiero** instanciar un nuevo proyecto Ágil utilizando una estructura base (WBS) y gestionar su Backlog
**Para** poder planificar iteraciones, asignar responsables directos y liberar tareas hacia los tableros Kanban operativos (Pantalla 3).

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Agile Project Instantiation and Planning
  Scenario: Instanciación sin Sprints en V1 (Postergación Táctica) (CA-1)
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
  Scenario: Geometría Adaptativa por Colisión con Días Festivos (CA-1)
    Given la tarea X planificada para el lunes 12, con duración de 3 días laborables
    When el calendario maestro global marca repentinamente el lunes 12 como "Día Festivo Nacional"
    Then el motor de cálculos del Diagrama de Gantt estira automáticamente la caja visual de la tarea hacia la derecha compensando el día muerto (Fin: Jueves 15) sin requerir re-planificación humana obligatoria.

  Scenario: Protección Estructural contra Deadlocks Circulares (CA-2)
    Given que el PM crea dependencia "T1 -> T2" (Fin-Inicio) arrastrando flechas en el Lienzo 10.B
    When el PM arrastra erróneamente la dependencia contraria "T2 -> T1" creando un Ciclo Infinito
    Then el WebClient bloquea y aborta inmediatamente el cruce relacional (Error Geométrico visual) e impide guardarlo en la Base de Datos para garantizar un motor DAG limpio.

  Scenario: Sobrecarga Permisible con Semáforo Sensorial (CA-3)
    Given la matriz de 40 horas laborables semanales para un humano
    When el PM planifica tareas apiladas sobre la empleada "María" superando el 150% de su capacidad en la misma semana cronológica
    Then el sistema "permite" teóricamente la mala práctica (dejando al PM violar la métrica)
    And como contramedida, enciende agresivas Balizas Visuales Rojas (Marcador de Recurso Sobrecargado) a un costado del nombre de la analista.

  Scenario: Re-planificación Activa y Multi-Líneas Base (Baseline Rupture) (CA-4)
    Given un proyecto que lleva 2 meses en Ejecución Viva (Basado sobre Línea Base "V1")
    When el PM requiera estirar los tiempos un 30% a solicitud formal del cliente
    Then el sistema permite pausar y "Reprogramar" formalmente el nodo vivo en el lienzo visual de la Pantalla 10.B
    And fuerza al PM a guardar y pisar una nueva Línea Base Evolutiva (Ej: V2_Reprogramada), preservando en el log histórico la desviación financiera/temporal ocurrida frente al V1 primitivo para auditoría de Gerencia.

  Scenario: Hot-Swaps en Cabina de Mando (Reasignación de Silla Ejecutiva) (CA-5)
    Given una tarea vital (T4) de Línea Base activa rebotando infructuosamente en el Workdesk del analista 'Pedro' por su ausencia repentina
    When el Project Manager se adentra en la Pantalla 10.B (Cabina General Gantt Transaccional) e invoca la tarjeta temporal viva (T4)
    Then el sistema posibilita el borrado nominal en duro de 'Pedro' para inyectar sobre vuelo el usuario 'Luis'
    And el motor BPMN retira perentoriamente la carta de la delegación de Pedro, materializándola sincrónicamente en el Workdesk de su co-equipero para no frustrar la métrica de entrega del T4.

  Scenario: Modos Flexibles de Reclamo (Pool vs Empleado Directo) (CA-6)
    Given la responsabilidad del PM de instanciar tareas en el motor Gantt
    Then el PMo goza del Switch parametrizable de Asignamiento en su UX
    And ostenta la facultad imperativa de designar nominalmente la Tarea Hacia un Usuario Exacto (`maria.lopez`)
    And o puede prescindir de asimetrías tácticas y tirarlo en bandeja común al Grupo Jerárquico General ("Equipo Legal"), forzando que ellos ejerzan Auto-Apropiación (US-002: Claim Task) por competencia.
    
  # NOTA CONTEXTUAL PO: (CA-7 Camino Crítico PERT) y (CA-8: Avance Financiero EVM) diferidos expresamente a V2 del MVP.
```
**Trazabilidad UX:** Wireframes Pantalla 10.B (Planner Tradicional - Gantt) y Pantalla 1 (Workdesk).

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
  Scenario: Renderizado exitoso del Dashboard de Grafana (CA-1)
    Given un usuario autenticado con Rol "Gerente_Operaciones"
    When la aplicación frontend solicita renderizar el iframe interactivo en la Pantalla 5
    Then el API Gateway debe emitir un JWT de corta duración (Grafana Auth Proxy) con rol de "Viewer"
    And el iframe debe renderizar correctamente el tablero pasándole variables de entorno `&var-TenantID=T123`
    And el dashboard debe mostrar obligatoriamente un panel de "Tareas Vencidas por SLA" consultando la vista materializada `vw_task_sla_breach`

  Scenario: Aislamiento Estricto de Datos (Multi-Tenancy) (CA-2)
    Given la arquitectura SaaS multi-cliente de la plataforma iBPMS
    When el JWT de Grafana es generado por el Backend para renderizar la Pantalla 5
    Then el token debe inyectar criptográficamente el `Tenant_ID` del usuario activo
    And la Base de Datos o la consulta subyacente de Grafana debe forzar obligatoriamente el filtrado por este Tenant (Ej. Row-Level Security) previniendo fugas de datos operativos hacia clientes vecinos.

  Scenario: Capacidad de Perforación Interactiva (Drill-Down UI) (CA-3)
    Given el Dashboard visual en la Pantalla 5 que muestra una alerta de "15 Tareas Bloqueadas"
    When el gerente hace clic sobre el segmento de la gráfica circular
    Then el sistema debe interceptar el evento de anclaje de Grafana
    And redireccionar la UI del iBPMS automáticamente a la Bandeja de Trabajo (Pantalla 1) o Hub Ágil (Pantalla 10)
    And pre-filtrar la vista exacta con las 15 tarjetas implicadas para tomar acción inmediata.

  Scenario: Segregación de Roles para Monitoreo Activo (RBAC) (CA-4)
    Given un empleado raso con rol "Analista" o "Ejecutor" intentando acceder a URL de reportes macro
    When navegue hacia la Pantalla 5 (BAM)
    Then el Frontend interceptará la ruta y mostrará un mensaje de "Acceso Denegado"
    And el Backend rechazará la generación del Token de Grafana, reservando esta vista exclusivamente para jerarquías directivas (Ej. `Gerente_Operaciones`, `Scrum_Master`).

  Scenario: Frecuencia de Refresco Asíncrona (Protección Transaccional) (CA-5)
    Given el inmenso volumen de eventos emitidos en tiempo real por el motor Camunda
    When Grafana ejecute los queries analíticos pesados para renderizar la Pantalla 5
    Then NO atacará directamente la base de datos transaccional caliente (Master DB)
    And leerá de una Base de Datos Analítica o Réplica (Ej. Elasticsearch o DataWarehouse) alimentada por un CronJob/CDC que se actualiza estrictamente cada 10 minutos para proteger la estabilidad del servicio en vivo.

  Scenario: Autoservicio de BI Analítico (Grafana Editor Nativo) (CA-6)
    Given que los tableros pre-cargados (Vencimientos, Costos, Ciclos) no cubren una métrica atípica solicitada por un cliente
    When el gerente seleccione la opción "BAM Avanzado" en la Pantalla 5
    Then el iBPMS cargará la Interfaz Nivel Editor Nativa de Grafana embebida
    And otorgará permisos formales de "Editor" al usuario, permitiéndole arrastrar bloques, cambiar colores de tortas y personalizar sus propias métricas ad-hoc limitadas a su Tenant_ID.

  Scenario: Aplanamiento de Datos Transaccionales para Analítica Rápida (Data Flattening / CDC) (CA-7)
    Given que el motor de Dashboards (Grafana) necesita graficar variables de negocio almacenadas en los JSON de Camunda
    When una tarea se completa o una variable es inyectada en el motor
    Then la arquitectura TIENE PROHIBIDO permitir que Grafana haga queries complejos (Full Table Scans) sobre las tablas operativas Blob de Camunda (`ACT_RU_VARIABLE`).
    And el Backend iBPMS implementará un proceso asíncrono de "Aplanamiento" (Change Data Capture o Event Listener).
    And extraerá las variables estratégicas del JSON y las insertará en una tabla relacional plana y columnar (Ej: `ibpms_business_metrics_flat`).
    And Grafana consumirá exclusivamente esta tabla plana, garantizando tiempos de carga en milisegundos sin impactar el Core.


```
**Trazabilidad UX:** Wireframes Pantalla 5 (Dashboards y Panel de Control - BAM).

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


## ÉPICA 8: Generador Documental Jurídico (SGDEA)
*(SHOULD HAVE)* - Producción controlada de artefactos legales a partir del estado final de un caso.

### US-010: Generar y Descargar PDF a partir de datos del caso
**Como** Analista / Gestor Documental
**Quiero** que el sistema ensamble un PDF inmutable (Ej. un Contrato) con los datos finales del caso
**Para** enviarlo a firma o entregarlo al cliente sin errores de "copy-paste".

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Legal PDF Generation from Template
Scenario: Ensamblar PDF usando plantilla del SGDEA y Variables de la Instancia (CA-1)
    Given la instancia de proceso "PI-888" finalizada en estado "APPROVED"
    And la plantilla "Contrato_Laboral_V3.docx" almacenada en el repositorio maestro
    When el usuario realiza un POST a "/api/v1/documents/generate/PI-888"
    Then el motor Documental (FOP/PDFBox) inyecta el árbol `json_variables` en las etiquetas `<<key>>` de la plantilla
    And registra el checksum SHA-256 en `ibpms_audit_log` para inmutabilidad legal
    And el sistema retorna HTTP STATUS 200 OK con un enlace temporal de SharePoint Graph API (Pre-Authenticated Link) expirable en 15 minutos para su visualización.

  Scenario: Tolerancia a Fallos por Variables Ausentes (Missing Keys) (CA-2)
    Given una plantilla `.docx` que incluye la etiqueta `<<segundo_apellido>>` obligatoria en su sintaxis
    When el motor documental (FOP) sea invocado y la variable no exista o sea NULA en el payload enviado por Camunda
    Then el motor NO debe abortar la transacción (Evitando HTTP 400 y rotura de flujos de negocio)
    And debe sobrellevar la carencia inyectando automáticamente la frase "N/A" o un espacio en blanco seguro en el documento final.

  Scenario: Expansión Dinámica de Tablas y Vectores (Bucles) (CA-3)
    Given que el JSON de entrada contiene un Array de objetos (Ej: Lista de 5 productos comprados)
    When la plantilla documental contenga sentencias iterativas de tipo `#foreach` en filas de una tabla de Word
    Then el motor SGDEA clonará la fila tantas veces como elementos existan en el array inyectando sus respectivas propiedades, posibilitando documentos hiper-dinámicos de longitud variable en la V1.

Scenario: Gobernanza de Persistencia (SharePoint Vault vs Vuelo Efímero) (CA-4)
    Given la invocación del servicio REST `/api/v1/documents/generate`
    When el proceso configure explícitamente el flag `storageMode`
    Then el Back-End acatará rígidamente la directriz:
    And Si es `EPHEMERAL`: El documento se renderiza, se entrega el link de 15min y se destruye físicamente de RAM/Disco del servidor.
    And Si es `PERSISTENT`: El PDF se traslada e inyecta inmutablemente en Microsoft SharePoint (Única Bóveda Oficial SGDEA), amarrado a la sub-carpeta del UID del Expediente (Acorde a la US-035), garantizando registro perenne exigible por Ley, evadiendo cobros duplicados en S3/Azure.

  Scenario: Acorazado Forense y Firma Digital del Documento Físico (CA-5)
    Given la configuración de una plantilla de Alto Riesgo Legal
    When el motor finaliza el ensamblado del PDF final
    Then NO se limitará a guardar el Hash SHA-256 en la base de datos (ibpms_audit_log)
    And incrustará en paralelo un "Certificado Criptográfico PKI" estructural dentro del mismo archivo PDF
    And y estampará visualmente en los márgenes de las páginas un Código QR (o Sello de Agua Legal) verificable externamente, asegurando la no-repulsa de autoría.

  Scenario: Versión Retroactiva Activa en Auditorías Históricas (CA-6)
    Given un Cliente instanciado hace 2 años cuando regía el "Contrato Laboral V1"
    When un auditor re-visite en Pantalla 12 dicho caso y el sistema requiera re-descargar o consultar su contrato
    Then el motor SGDEA buscará y ensamblará el PDF contra la plantilla V1 almacenada en el repositorio histórico (Time-Travel Rendering)
    And prohibirá rotundamente la utilización de la plantilla "V4" actual para casos pasados, protegiendo las cláusulas vigentes al momento de la firma original.
```
**Trazabilidad UX:** Wireframes Pantalla 12 (Bóveda Documental y Generación).

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
	
	Scenario: Storage Garbage Collector para Archivos Huérfanos (Evitar Fuga Financiera) (CA-18)
    Given el patrón arquitectónico "Upload-First" (US-029) donde los archivos pesados se suben a la sub-carpeta `/upload-temp` de manera asíncrona temprana
    When el operario humano abandona la tarea, cierra la pestaña o descarta un Intake sin oprimir jamás el botón de [Enviar] Formulario
    Then esos archivos se convierten en "Archivos Huérfanos" (Binarios sin un Process_Instance_ID asociado en BD).
    And el iBPMS ejecutará un CronJob nocturno perentorio a las 03:00 AM
    And el Job consultará la API de Storage eliminando físicamente (Hard-Delete) cualquier archivo en `/upload-temp` que supere las 24 horas de antigüedad, tapando la hemorragia de costos por almacenamiento de basura no transaccional.
	
	
```
**Trazabilidad UX:** Wireframes Pantallas 12,16 y 6.

---


---

## ÉPICA 9: Inteligencia Artificial, MLOps y Buzones SAC
*(Esta épica fue pivotada de V2 a V1 para garantizar el Product-Market Fit como plataforma AI-First).*

### US-011: Filtrado Transversal en Bandeja Avanzada (Docketing)
**Como** Analista Legal / Supervisor de Operaciones
**Quiero** filtrar mi bandeja de entrada estructurada (Pantalla 1B) mediante dropdowns relacionales ("Cliente", "Proyecto", "Rango de Fechas") y etiquetas booleanas de actividad ("Acuses", "Tareas Creadas")
**Para** localizar rápidamente eventos críticos o cargas de trabajo asociadas a cuentas clave sin abrir cada correo individualmente.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Advanced Relational Inbox Filtering
  Scenario: Filtrado compuesto determinista (Cliente + Proyecto)  (CA-1)
    Given el usuario autenticado está navegando la Bandeja Avanzada (Pantalla 1B)
    And hay 500 ítems en la bandeja, de los cuales 5 pertenecen al Cliente "Global Tech" y el Proyecto "Patente-XZ"
    When el usuario selecciona "Global Tech" en el selector 'Filtro Cliente'
    And el usuario selecciona "Patente-XZ" en el selector 'Filtro Proyecto'
    Then el API del Backend debe ejecutar una query cruzada contra 'ibpms_metadata_index'
    And el Frontend debe renderizar exclusivamente los 5 ítems exactos en menos de 1 segundo (Paginado)
    And la UI debe mostrar un estado "Empty State" si la combinación no retorna resultados

  Scenario: Filtrado por Label Booleano generado por IA (Acuses) (CA-2)
    Given la bandeja contiene ítems marcados por la IA con el boolean flag 'is_acknowledgment_sent: true'
    When el usuario marca el checkbox "Actividad: Acuse Enviado"
    Then el sistema debe ocultar todos los correos donde 'is_acknowledgment_sent: false' o nulo

  Scenario: Triage por Sentimiento y Urgencia (Predicción IA) (CA-3)
    Given la metadata enriquecida del correo proveniente de la US-013 (Ej: `sentiment: URGENCE_HIGH`)
    When el analista de SAC filtra la bandeja usando el dropdown "Urgencia y Sentimiento"
    Then el sistema filtra reestructurando la grilla para mostrar primero los correos que contengan quejas operativas o riesgos legales altos
    And garantizando un enfoque de First-In/First-Out ajustado por criticidad (Weighted FIFO).

  Scenario: Detección de Archivos y Tipificación Estructural (CA-4)
    Given que el correo contiene múltiples archivos adjuntos
    When el analista filtra por el concepto "Contiene: Contratos Firmados"
    Then el filtro de la Pantalla 1B obvia la extensión pura del archivo (.pdf)
    And cruza la búsqueda contra el tag de clasificación documental `doc_type` generado por la IA, retornando solo los correos cuyo contenido semántico coincida.

  Scenario: Monitoreo Activo de Acuerdos de Nivel de Servicio (SLA) (CA-5)
    Given los correos entrantes mapeados contra una política de respuesta máxima de 24 horas (SLA)
    When el analista de SAC aplica el filtro rápido de semáforo "Mostrar: SLA por Vencer (< 2 horas)"
    Then el sistema expone exclusivamente los correos que están a punto de romper el requerimiento legal de tiempo operativo, ocultando correos recientes de ingreso temprano.

  Scenario: Búsqueda Semántica de Texto Completo (Full-Text Search) (6A-6)
    Given un analista buscando la aguja en el pajar con la palabra "Indemnización"
    When digite dicha palabra en la barra de búsqueda global de la Pantalla 1B
    Then el motor de Backend (Elasticsearch o similar) NO buscará solo en el Asunto
    And indexará la búsqueda contra el cuerpo del correo, y el texto interior de los anexos (OCR) entregando el correo exacto donde reside dicho patrón.

  Scenario: Control de Concurrencia SAC y Bloqueo de Correos  (CA-7)
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

### US-013: Identificación automática de cliente y enriquecimiento desde posible conexion con CRM (ONS)
**Como** gestor de un buzón corporativo
**Quiero** que el asistente identifique el cliente por el dominio del remitente y/o consulte el CRM ONS
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
```
**Trazabilidad UX:** Wireframes Pantalla 15.B (Configuración Local de Buzones SAC).

---

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



## ÉPICA 10: Service Delivery CRM, Intelligent Intake y Portal B2C
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

  Scenario: Mapeo Comercial-Técnico (Service to BPMN Binding)
    Given la importación exitosa del catálogo de servicios desde el CRM ONS (Ej: "Servicio 101: Crédito")
    When el Administrador configura el iBPMS en la Pantalla 15.A
    Then el sistema DEBE obligar a realizar un "Mapeo de Activación" estratégico.
    And por cada Servicio comercial del CRM, el Administrador debe seleccionar de un Dropdown a qué `Process Definition Key` (el mapa BPMN de la Pantalla 6) corresponde su ejecución operativa.
    And si un servicio no tiene un BPMN amarrado, el Frontend lo ocultará previniendo que un cliente intente arrancar un proceso fantasma que crashearía el Backend.

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
```	
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

Scenario: Gestión del Ciclo de Vida Operativo y Destrucción del Token (CA-7)
    Given el nacimiento de un caso en el motor BPMN (Plan B)
    Then el sistema debe proveer una interfaz de administración global sobre la instancia "In-Flight".
    When el Administrador autorizado decide abortar/eliminar el caso operativo (Acción Delete)
    Then la acción ejecuta un 'Soft Delete' en la Base de Datos relacional del iBPMS marcando el registro visual como CANCELADO (Exigiendo motivo de anulación para la bitácora).
    And SIMULTÁNEAMENTE, el Backend invoca imperativamente la REST API interna de Camunda (`DELETE /engine-rest/process-instance/{id}`)
    And aniquilando físicamente el Token en vuelo dentro del motor orquestador, garantizando que los Timers y SLAs de ese proceso mueran al instante, evitando falsas alertas o tareas zombies revividas.
	
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

Scenario: Evento Compensatorio SGDEA por Aborto de Caso (Saga Pattern Documental) (CA-13)
    Given un proceso vivo ("In-Flight") que ha acumulado archivos físicos en la bóveda SGDEA (SharePoint/S3)
    When un Administrador ejecuta el `Soft Delete / Abortar Caso` desde el Workdesk o panel administrativo
    Then el Backend NO se limitará a aniquilar el Token en Camunda.
    And despachará un Evento de Compensación asíncrono (Patrón Saga) hacia el Módulo Documental (US-035).
    And ordenará el archivado lógico, etiquetado (`status=ABORTED_ORPHAN`) o traslado a Papelera de todos los UUIDs físicos asociados a ese caso.
    And previniendo el pago de almacenamiento en la nube infinito por basura de procesos abortados.

```

**Trazabilidad UX:** Wireframes Pantalla 16 (Intake Administrativo).

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
Feature: External Customer Portal (Service Delivery) and Zero-Trust Boundary

  Scenario: Acceso a Vista Táctica (Estado en Tiempo Real) (CA-1)
    Given un Cliente Externo autenticado (Ej: portal.ibpms.com) mediante un Identity Provider (Ej: Azure AD B2C / Cognito)
    When el cliente ingresa a su panel principal
    Then el sistema debe renderizar una lista con sus Service Deliveries "En Curso"
    And mostrar en qué etapa exacta del proceso se encuentra visualmente (Tracker / Stepper) ocultando tajantemente las tareas internas (Backoffice) que no estén explícitamente marcadas como "Visibles para el Cliente" en el diseño del proceso BPMN.

  Scenario: Prevención Estructural BOLA / IDOR (Seguridad Perimetral Absoluta) (CA-2)
    Given el Cliente Externo autenticado cuyo Token JWT contiene criptográficamente su identificador único (Ej: `Claim: crm_id = "CUST-999"`)
    When el cliente intenta forzar la lectura de un caso ajeno manipulando directamente la URL o la API REST (Ej: `GET /api/v1/portal/cases/SD-500` donde SD-500 pertenece al cliente "CUST-111")
    Then el Backend (Security Filter Chain / Interceptor) TIENE ESTRICTAMENTE PROHIBIDO confiar en el ID del caso enviado en la URL.
    And el motor extraerá el `crm_id` del JWT, y forzará inyectar la cláusula en la consulta a la base de datos: `WHERE case_id = 'SD-500' AND owner_crm_id = 'CUST-999'`.
    And al no haber coincidencia matemática, el Backend escupirá un silencioso `HTTP 404 Not Found` (Ceguera intencional, en lugar de 403 Forbidden, para no confirmarle al atacante que el caso ajeno sí existe).
    And registrará un evento de `SECURITY_ANOMALY` en el Log de Auditoría por intento de escalamiento horizontal de privilegios.

  Scenario: Enmascaramiento de Trazabilidad Interna (Data Masking BFF) (CA-3)
    Given que el cliente abre el detalle de su caso lícito `SD-0045`
    Then el API del Portal Externo actuará como un filtro (BFF) aislando la instancia cruda de Camunda.
    And purgará y ocultará del Payload DTO cualquier metadata de consumo interno (Ej: `comentarios_analista`, `score_riesgo_interno`).
    And ocultará terminantemente cualquier traza de IA (Confidence Score, Chain of Thought), exponiendo al ciudadano EXCLUSIVAMENTE los "Front-Facing Metadata" previamente autorizados.

  Scenario: Acceso a Vista Estratégica y Descarga Segura de SGDEA (CA-4)
    Given el mismo cliente navegando en la pestaña "Histórico y Desempeño"
    Then el sistema renderizará métricas de "Servicios Finalizados a Tiempo" vs "Retrasados"
    And listará todos los Service Deliveries concluidos.
    When el cliente solicite descargar el contrato o PDF asociado a un caso cerrado
    Then el Backend validará la propiedad BOLA (CA-2) y generará una "Pre-Signed URL" temporal (Ej: 15 minutos de caducidad) apuntando a la Bóveda SGDEA (SharePoint/Azure) para su descarga segura y efímera.
    And garantizando que el PDF legal no pueda ser indexado por Google ni compartido públicamente por WhatsApp si el link es reenviado a un tercero no autorizado.

Scenario: Colaboración Bidireccional (El Cliente como Operario Externo) (CA-5)
    Given un Cliente Externo navegando el detalle de su Service Delivery en el Portal B2C
    When el proceso BPMN interno haya enrutado un requerimiento formal o Tarea Humana (Ej: "Subsanar Documento Faltante") explícitamente hacia el "Rol del Cliente Externo"
    Then la interfaz del Portal Externo mutará, abandonando el modo "Solo Lectura" (Museo).
    And inyectará y renderizará dinámicamente el Componente Zod (iForm) correspondiente a esa etapa directamente en el portal B2C.
    And permitirá al ciudadano diligenciar la data o adjuntar archivos (vía Patrón Upload-First de la US-029).
    And al oprimir [Enviar], el portal ejecutará el POST a `/complete` avanzando el Token de Camunda desde el exterior, transfiriendo la carga operativa del Analista Interno hacia el Cliente Final.

```
**Trazabilidad UX:** Wireframes Pantalla 18 (Portal B2B/B2C del Cliente).


---

### US-040: Embudo Inteligente de Intake (Pre-Triaje y Descarte IA)
**Como** Administrador / Líder de Service Delivery
**Quiero** visualizar las Action Cards generadas por IA del Plan A en un formato de embudo de cuarentena
**Para** decidir si las instancio forzosamente rellenando huecos, si apruebo la intención de la IA (Convirtiéndolos en Service Delivery BPMN) o si los descarto.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Intelligent Intake Funnel Management
  Scenario: Máquina de Estados Inmutable del Intake (Ciclo de Vida Estricto) (CA-0)
    Given la fragmentación funcional entre la captura del correo, la IA y la instanciación en Camunda
    Then la arquitectura Backend DEBE implementar una Máquina de Estados (State Machine) estricta para la entidad `Intake`, prohibiendo saltos anárquicos (lógica if/else suelta), bajo el siguiente flujo obligatorio:
    And 1. `RECEIVED`: El Webhook recibe el correo crudo. Es invisible para los humanos. La IA extrae entidades y CRM_ID (US-013).
    And 2. `QUARANTINE`: La IA terminó de procesar. La tarjeta cae al Embudo (Pantalla 16) esperando al Administrador humano. El SLA comienza a correr.
    And 3. `APPROVED_LOCKED`: El Administrador presiona [Aprobar]. La tarjeta entra en la ventana de gracia de 10s (Botón Deshacer). Se bloquea la fila en BD para evitar concurrencia optimista (dos admins tocando la misma tarjeta).
    And 4. `PROMOTED_TO_BPMN`: Venció la ventana de gracia. El Backend hace el POST a Camunda (creando el Process_Instance_ID), dispara la notificación de confirmación al cliente (Motor de Notificaciones US-049), y el Intake se marca como finalizado, desapareciendo del embudo visual.
    And 5. `DISCARDED_TRASH`: El humano oprimió la papelera. Se notifica al MLOps (US-015) y desaparece de las vistas activas esperando la purga de 48 hrs.
    And el Backend rechazará (HTTP 409 Conflict) cualquier intento de mutación que no respete estrictamente estas transiciones direccionales.
  
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

    Scenario: Disparo Automático de Onboarding B2C post-Intake (Cierre GAP CIAM)
    Given un Intake en cuarentena asociado a un correo de un cliente nuevo que NO existe en el Identity Provider local
    When el Administrador presiona [Aprobar] y la tarjeta se promueve a instancia BPMN tras la ventana de gracia
    Then el Backend disparará asíncronamente el flujo de la US-050 enviando un "Magic Link" de bienvenida al correo original.
    And atará el `Process_Instance_ID` recién nacido a su nuevo `CRM_ID`.
    And garantizando que al crear su contraseña y entrar al Portal B2C (US-026), el ciudadano vea su trámite inmediatamente activo sin procesos manuales de IT, cerrando el bucle de auto-servicio.
```
**Trazabilidad UX:** Wireframes Pantalla 16 (Intelligent Intake y Embudo Administrativo).

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

### US-050: Identidad y Onboarding de Clientes Externos (CIAM / Zero-Public-Signup)
**Como** Sistema Core (iBPMS)
**Quiero** enviar una invitación segura (Magic Link) al correo de un cliente externo
**Para** que pueda crear su contraseña y acceder al Portal B2C, garantizando que su usuario quede amarrado criptográficamente a su CRM_ID sin abrir formularios de registro público.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Secure Customer Onboarding and Identity (CIAM)

  Scenario: Prohibición de Registro Público (Zero-Public-Signup)
    Given la pantalla de Login del Portal Externo (portal.ibpms.com)
    Then la interfaz NO DEBE tener ningún enlace, botón o formulario que diga "Registrarse" o "Crear Cuenta".
    And la creación de identidades ciudadanas (External Users) solo puede nacer desde el interior del iBPMS (Vía API o evento interno), blindando el sistema contra bots y registros masivos fraudulentos.

  Scenario: Disparo de Invitación (Magic Link) por Evento o Botón
    Given un Cliente nuevo registrado en el CRM con el ID `CUST-999` y correo `juan@gmail.com`
    When el proceso BPMN llega a una tarea de "Invitar a Portal" O un analista oprime el botón [Invitar] en la Vista 360 del cliente
    Then el sistema generará un Token criptográfico de uso único (Magic Link).
    And el Motor de Notificaciones (US-049) enviará un correo a `juan@gmail.com` con el botón "Crear mi Contraseña de Acceso".
    And el Magic Link tendrá una caducidad (TTL) rígida paramétrica (Ej: 24 horas).

  Scenario: Aterrizaje y Vinculación Criptográfica (Account Claiming)
    Given el cliente Juan que hace clic en el Magic Link dentro de las 24 horas permitidas
    When aterriza en la página de "Definir Contraseña" del Portal B2C
    Then el sistema verifica que el Token no haya sido usado antes y bloquea la edición del campo de correo electrónico (Read-Only).
    And Juan digita su contraseña (cumpliendo políticas de seguridad corporativa).
    And el sistema inscribe la cuenta en el Identity Provider (Azure AD B2C o Cognito / Local).
    And OBLIGATORIAMENTE graba el valor `CUST-999` como un atributo inmutable (Custom Claim) dentro del Token del usuario (El "Bolsillo Secreto" de la cuenta).
    And garantizando que a partir de ese momento, cada vez que Juan inicie sesión, su Token JWT contenga su identificador, lo cual activará el escudo Anti-BOLA de la US-026 impidiendo que vea datos de otros clientes.
```

## ÉPICA 11: Extensiones Cognitivas AI-Native - Cognitive BPMN (US-032)

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


## ÉPICA 12: Hub Integraciones & Central Message Broker (US-033, US-034)

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
**Trazabilidad UX:** Wireframes Pantalla 11 (Integration Hub).
---

### US-034: Orquestación a través de RabbitMQ
**Como** Administrador de Infraestructura / Backend
**Quiero** delegar el rate-limiting y el encolamiento asíncrono a un Message Broker de grado Enterprise (RabbitMQ)
**Para** garantizar resiliencia extrema frente a picos transaccionales, evitando desbordamientos de memoria (OOM) y caídas de subsistemas.


> [!IMPORTANT]
> **Dependencias Externas Críticas de la US-034:**
> - **US-004 (Webhook):** Los webhooks entrantes que exceden la capacidad del motor se encolan en RabbitMQ (CA-6 de US-004: Resiliencia Periférica con Colas).
> - **US-000 (Resiliencia Integrada):** El health check del clúster RabbitMQ (CA-10) se integra como componente del circuito de salud compuesto de la plataforma.
> - **US-049 (Notificaciones):** Todas las notificaciones por email se despachan como mensajes P2 vía las colas de RabbitMQ.
> - **US-033 (Hub de Integraciones):** Los conectores a sistemas externos (MS Graph, ERP) producen mensajes en las colas de integración.
> - **US-017 (IA Copilot):** Las generaciones de IA (RAG, DMN) son productores Nivel P3 (Batch) en el sistema de prioridades.
> - **US-036 (RBAC):** El acceso al Dashboard DLQ (CA-8) está restringido al rol `ADMIN_IT` administrado en la Pantalla 14.
> - **US-038 (JWT/Seguridad):** El botón de Purga de DLQ requiere autenticación Sudo-Mode definida en la infraestructura de seguridad de US-038.
> - **US-039 (Formulario Genérico):** Los Error Events disparados por los Botones de Pánico se enrutan a través del broker como mensajes P1.


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


  # ==============================================================================
  # B. REMEDIACIONES POST-AUDITORÍA (Sprint Remediation Brief 2026-04-05)
  # Origen: docs/requirements/us034_functional_analysis.md
  # Tickets: REM-034-01 a REM-034-07
  # Propósito: Cerrar GAPs de implementación detectados por el workflow
  #            /analisisEntendimientoUs.md tras finalizar las 17 iteraciones
  #            de la Auditoría Integral del Backlog.
  # ==============================================================================

  Scenario: [REMEDIACIÓN] Catálogo Oficial de Exchanges, Queues y Routing Keys (CA-4)
    # Origen: REM-034-01 — GAP-1 del us034_functional_analysis.md
    Given la necesidad de prevenir la proliferación desordenada de colas y exchanges en el clúster RabbitMQ
    Then el Arquitecto de Software TIENE OBLIGACIÓN de mantener un catálogo centralizado de la topología de mensajería en el repositorio bajo `docs/architecture/rabbitmq_topology.md` que incluya:
    And 1. Exchange principal: `ibpms.exchange.topic` (tipo Topic) como punto de entrada único para todos los productores.
    And 2. Colas nombradas con convención: `ibpms.{dominio}.{accion}` (Ej: `ibpms.notifications.email`, `ibpms.ai.generation`, `ibpms.integrations.webhook`, `ibpms.bpmn.events`).
    And 3. Routing Keys con convención: `{dominio}.{prioridad}.{accion}` (Ej: `notifications.p1.send`, `ai.p3.generate`, `integrations.p2.sync`).
    And 4. Dead Letter Exchange: `ibpms.exchange.dlx` que enruta a la cola `ibpms.dlq.global`.
    And TIENE PROHIBIDO que cualquier developer cree exchanges o colas ad-hoc sin registrarlas previamente en el catálogo y obtener aprobación del Arquitecto.

  Scenario: [REMEDIACIÓN] Idempotencia Obligatoria en Workers Consumidores (CA-5)
    # Origen: REM-034-02 — GAP-2 del us034_functional_analysis.md
    Given el riesgo de procesamiento duplicado por reintentos manuales desde la DLQ (CA-2) o reintentos automáticos
    Then todo Worker consumidor del iBPMS TIENE OBLIGACIÓN de implementar un mecanismo de idempotencia basado en `message_id`:
    And cada mensaje producido incluirá un header `x-idempotency-key` (UUID v4 generado por el productor).
    And el Worker consultará una tabla `ibpms_processed_messages` (columnas: `idempotency_key`, `processed_at`, `queue_name`, TTL: 72 horas) antes de procesar.
    And si el `idempotency_key` ya existe en la tabla, el Worker hará ACK silencioso del mensaje sin reprocesarlo.
    And la tabla `ibpms_processed_messages` se purgará automáticamente vía un scheduled job cada 24 horas, eliminando registros con más de 72 horas de antigüedad.
    And como alternativa de mayor rendimiento, el Arquitecto podrá reemplazar la tabla SQL por un SET de Redis con TTL de 72 horas (`SISMEMBER ibpms:idempotency {key}`).

  Scenario: [REMEDIACIÓN] Taxonomía Formal de Niveles de Prioridad (CA-6)
    # Origen: REM-034-03 — GAP-3 del us034_functional_analysis.md
    Given la necesidad de jerarquizar el tráfico en Priority Queues (CA-3) con criterios claros
    Then el sistema implementará exactamente 3 niveles de prioridad con la siguiente taxonomía fija:
    And Nivel P1 (Crítico / SLA < 5min): Notificaciones de aprobaciones financieras, Kill-Session (US-036 CA-14), Error Events de Camunda, alertas de seguridad. Prefetch count: 1 (procesamiento atómico garantizado).
    And Nivel P2 (Normal / SLA < 30min): Envío de emails transaccionales (US-049), sincronización EntraID (US-038), webhooks de integración (US-004). Prefetch count: 10.
    And Nivel P3 (Batch / SLA < 4h): Generación RAG de resúmenes (US-017), reportes masivos (US-036 CA-16), limpieza de borradores (US-003 CA-92). Prefetch count: 50.
    And la prioridad se asignará como header del mensaje (`x-priority: P1|P2|P3`) por el productor en el momento de publicar. Si no se especifica, el default es P2.
    And TIENE PROHIBIDO que un productor asigne P1 a eventos que no cumplan con la definición anterior sin aprobación del Arquitecto.

  Scenario: [REMEDIACIÓN] Estrategia de Retry Automático con Backoff Exponencial (CA-7)
    # Origen: REM-034-04 — GAP-4 del us034_functional_analysis.md
    Given la ausencia de reintentos automáticos antes de enviar un mensaje a la DLQ
    Then el clúster RabbitMQ implementará una política de retry automático obligatoria antes de derivar a la Dead Letter Queue:
    And Intento 1: Inmediato (0ms delay).
    And Intento 2: Delay de 5 segundos (via `x-message-ttl` en cola de retry).
    And Intento 3: Delay de 30 segundos.
    And Intento 4 (final): Delay de 2 minutos. Si falla, el mensaje se enruta al DLX (`ibpms.exchange.dlx`) con header `x-delivery-count: 4`.
    And el Worker diferenciará errores transitorios (IOException, TimeoutException → reintentar) de errores permanentes (ValidationException, IllegalArgumentException → DLQ directo sin reintentos).
    And todo mensaje que llegue a la DLQ llevará los headers: `x-original-queue`, `x-first-death-reason`, `x-delivery-count`, `x-last-error-message` para diagnóstico.

  Scenario: [REMEDIACIÓN] Implementación del Dashboard DLQ como Pantalla Custom del iBPMS (CA-8)
    # Origen: REM-034-05 — GAP-5 del us034_functional_analysis.md
    Given la necesidad de un Dashboard visual de DLQ accesible para el Administrador IT (CA-2)
    Then el Dashboard será una pantalla custom del iBPMS (componente Vue) accesible desde la navegación principal, NO un enlace externo al Management UI de RabbitMQ.
    And la pantalla consumirá un endpoint Backend `GET /api/v1/admin/queues/dlq/summary` que retornará: total de mensajes, agrupación por cola de origen (`x-original-queue`), y timestamp del mensaje más antiguo.
    And el botón `[Reintentar Mensajes]` invocará `POST /api/v1/admin/queues/dlq/retry` y requerirá un modal de confirmación con la advertencia: "Se reintentarán N mensajes. Los Workers deben ser idempotentes (CA-5)."
    And el botón `[Purgar Cola]` invocará `DELETE /api/v1/admin/queues/dlq/purge` y requerirá autenticación Sudo-Mode (US-038) con justificación obligatoria de 20+ caracteres.
    And toda acción sobre la DLQ quedará registrada en `ibpms_audit_log` con: `user_id`, `action` (RETRY|PURGE), `message_count`, `timestamp_utc`.
    And el acceso a esta pantalla estará restringido al rol `ADMIN_IT` configurado en la Pantalla 14 (US-036).

  Scenario: [REMEDIACIÓN] Política de TTL y Purgado Automático de la Dead Letter Queue (CA-9)
    # Origen: REM-034-06 — GAP-6 del us034_functional_analysis.md
    Given el riesgo de crecimiento indefinido de la DLQ en producción
    Then la cola `ibpms.dlq.global` implementará un TTL de 30 días naturales (`x-message-ttl: 2592000000ms`) para todos los mensajes.
    And los mensajes que excedan 30 días serán purgados automáticamente por RabbitMQ sin intervención humana.
    And ANTES de purgar, un scheduled job (`DlqArchiveJob`, ejecutado diariamente) copiará los mensajes próximos a expirar (TTL < 48h) a una tabla de archivo `ibpms_dlq_archive` (columnas: `message_id`, `original_queue`, `headers_json`, `body_summary` truncado a 1KB, `archived_at`) para auditoría forense.
    And la tabla `ibpms_dlq_archive` tendrá su propia política de retención: 180 días, purgada por el `LocalStorageGarbageCollector` de infraestructura.

  Scenario: [REMEDIACIÓN] Health Check del Clúster RabbitMQ Integrado al Circuito de Resiliencia (CA-10)
    # Origen: REM-034-07 — GAP-7 del us034_functional_analysis.md
    Given la criticidad del clúster RabbitMQ como infraestructura troncal de la plataforma
    Then el Backend expondrá un endpoint de salud `GET /actuator/health/rabbitmq` que verifique la conectividad al clúster cada 15 segundos.
    And si el health check falla 3 veces consecutivas (45 segundos sin respuesta), el sistema activará un Circuit Breaker (estado OPEN) en todos los productores de mensajes.
    And durante el Circuit Breaker OPEN, los productores almacenarán temporalmente los mensajes en un buffer local en memoria (máximo 1000 mensajes, FIFO) por un máximo de 5 minutos.
    And si RabbitMQ regresa dentro de los 5 minutos (Circuit Breaker HALF-OPEN → CLOSED), el buffer se drenará automáticamente reenviando los mensajes encolados.
    And si RabbitMQ NO regresa en 5 minutos, los mensajes del buffer se persistirán en una tabla de emergencia `ibpms_queue_fallback` y se disparará una alerta crítica al SysAdmin: "RabbitMQ Offline — N mensajes en fallback SQL de emergencia."
    And este endpoint de salud se integrará con la US-000 (Resiliencia Integrada) como parte del health check compuesto `/actuator/health`.


```
**Trazabilidad UX:** Operación Backend e Infraestructura (Dead Letter Queue IT Dashboard).

---

## ÉPICA 13: Hub Integraciones & Central Message Broker (US-033, US-034)
Cubre la brecha arquitectónica de la gestión de conexiones de los orígenes de datos (Correos Electrónicos). Permite al Súper Administrador registrar físicamente las cuentas de "Atención al Cliente" para que el iBPMS pueda succionar los reclamos y aplicar la IA.


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

  # ==============================================================================
  # B. REMEDIACIONES POST-AUDITORÍA (Sprint Remediation Brief 2026-04-05)
  # Origen: docs/requirements/us036_functional_analysis.md
  # Tickets: REM-036-01 a REM-036-07
  # Propósito: Cerrar GAPs de implementación detectados por el workflow
  #            /analisisEntendimientoUs.md tras finalizar las 17 iteraciones
  #            de la Auditoría Integral del Backlog.
  # ==============================================================================

  Scenario: [REMEDIACIÓN] Modelo de Datos Relacional para la Matriz RBAC (CA-19)
    # Origen: REM-036-01 — GAP-1 del us036_functional_analysis.md
    Given la necesidad de persistir roles, permisos, asignaciones y herencia piramidal definidos en los CA-1 a CA-18
    Then el Backend TIENE OBLIGACIÓN de implementar el siguiente esquema relacional mínimo en PostgreSQL:
    And Tabla `ibpms_roles` con columnas: `id`, `name`, `description`, `parent_role_id` (FK auto-referencia para herencia CA-6), `is_template` (boolean para Rol Plantilla CA-3), `source` (ENUM: ENTRA_ID | LOCAL), `created_at`, `updated_at`.
    And Tabla `ibpms_permissions` con columnas: `id`, `resource` (Ej: PROCESS, FORM, ADMIN_PANEL), `action` (ENUM: INITIATE, EXECUTE, READ, WRITE, DELETE), `process_definition_id` (FK nullable para permisos por proceso CA-4).
    And Tabla pivote `ibpms_role_permissions` para la relación N:M entre roles y permisos.
    And Tabla pivote `ibpms_user_roles` con columnas: `user_id`, `role_id`, `assigned_by`, `assigned_at`, soportando Mass Assignment (CA-3) mediante INSERT batch.
    And la herencia piramidal (CA-6) se resolverá mediante una query recursiva CTE (`WITH RECURSIVE`) que recorra `parent_role_id` para computar los permisos efectivos de un rol en tiempo de consulta.
    And el esquema se gestionará mediante scripts Liquibase versionados en `db/changelog/`.

  Scenario: [REMEDIACIÓN] Estrategia de Row-Level Security para Privacidad de Colas (CA-20)
    # Origen: REM-036-02 — GAP-2 del us036_functional_analysis.md
    Given la exigencia de que cada operario visualice SOLO sus folios asignados en el Workdesk (CA-5)
    Then la implementación V1 utilizará un interceptor centralizado a nivel de aplicación (Spring AOP `@Aspect` o un `Specification` base de JPA) que inyecte automáticamente el filtro `WHERE assignee_id = :currentUserId` en TODAS las queries del Workdesk.
    And TIENE PROHIBIDO implementar el filtro como un WHERE manual en cada Repository method, ya que un endpoint olvidado filtraría datos ajenos.
    And si en el futuro se migra a RLS nativo de PostgreSQL (`CREATE POLICY`), el interceptor de aplicación se desactivará sin afectar la lógica de negocio.
    And para las Colas Compartidas Públicas, el interceptor reconocerá un flag `is_shared_queue = true` en la definición del proceso y omitirá el filtro de usuario, permitiendo visibilidad colectiva.

  Scenario: [REMEDIACIÓN] Infraestructura de Blacklist JWT para Kill-Session (CA-21)
    # Origen: REM-036-03 — GAP-3 del us036_functional_analysis.md
    Given la funcionalidad de Kill-Session (CA-14) que exige destruir sesiones activas instantáneamente
    Then la implementación del botón Kill-Session en Pantalla 14 invocará un endpoint `POST /api/v1/admin/users/{userId}/revoke-session`.
    And este endpoint insertará el `jti` (JWT ID) del token activo del usuario en una blacklist de Redis con TTL igual al tiempo restante de vida del token (max 15 minutos según política de US-038 CA-01).
    And el Spring Security Filter consultará esta blacklist en cada request entrante en menos de 5ms.
    And esta implementación TIENE DEPENDENCIA DIRECTA con la US-038 CA-01 (Fail-Open Policy), la cual define el comportamiento cuando Redis no está disponible.
    And el equipo que desarrolle la US-036 TIENE OBLIGACIÓN de coordinarse con el equipo de la US-038 para compartir el mismo servicio de blacklist Redis, prohibiendo crear implementaciones paralelas.

  Scenario: [REMEDIACIÓN] Política de Seguridad para API Keys de Service Accounts (CA-22)
    # Origen: REM-036-04 — GAP-4 del us036_functional_analysis.md
    Given la funcionalidad de creación de Service Accounts M2M (CA-10) que genera API Keys sin política de ciclo de vida
    Then toda API Key generada en Pantalla 14 TIENE OBLIGACIÓN de incluir una fecha de expiración configurable (por defecto: 365 días, máximo: 730 días).
    And la API Key se almacenará hasheada con SHA-256 en la tabla `ibpms_service_accounts`; el valor en texto plano solo se mostrará UNA VEZ al momento de la creación (como GitHub Personal Access Tokens).
    And la Pantalla 14 mostrará un indicador visual de API Keys próximas a expirar (menos de 30 días) con alerta amarilla, y expiradas con alerta roja.
    And el Super Admin podrá regenerar (rotar) una API Key existente, deprecando la anterior inmediatamente e invalidando todas las sesiones activas del Service Account.
    And todo uso de API Key se registrará en la tabla `ibpms_audit_log` con: `service_account_id`, `endpoint_invocado`, `timestamp_utc`, `ip_origen`.

  Scenario: [REMEDIACIÓN] Comportamiento de Delegación sobre Tareas In-Flight (CA-23)
    # Origen: REM-036-05 — GAP-5 del us036_functional_analysis.md
    Given un Gerente que activa una delegación temporal a un suplente (CA-9)
    When la delegación entra en vigencia según el rango de fechas configurado
    Then el suplente heredará TANTO el rol delegado COMO las tareas ya asignadas al delegante en la bandeja del Workdesk (tareas in-flight).
    And las tareas nuevas que lleguen durante el periodo de delegación también se enrutarán al suplente.
    And al expirar la delegación, las tareas NO completadas por el suplente regresarán automáticamente a la bandeja del delegante original con un sello visual: "[Retornada post-delegación]".
    And toda la operación de transferencia y retorno de tareas quedará registrada en `ibpms_audit_log` para trazabilidad CISO.

  Scenario: [REMEDIACIÓN] Alcance Explícito del Reporte ISO 27001 en V1 (CA-24)
    # Origen: REM-036-06 — GAP-6 del us036_functional_analysis.md
    Given la funcionalidad de generación de reportes de Identity Governance (CA-16)
    Then para V1 el reporte se generará exclusivamente bajo demanda (on-demand) mediante un botón en Pantalla 14, sin generación programada automática (cron).
    And el reporte incluirá la fecha y hora UTC de generación, el usuario que lo solicitó, y un hash SHA-256 del contenido para certificar integridad.
    And cada reporte generado se persistirá como registro histórico en la tabla `ibpms_audit_reports` para comparación entre periodos (Ej: "Estado de permisos en Enero vs Febrero").
    And la generación programada (cron + envío por email al CISO) queda explícitamente DIFERIDA a V2.

  Scenario: [REMEDIACIÓN] Directriz de Coordinación US-036 vs US-038 (CA-25)
    # Origen: REM-036-07 — GAP-7 del us036_functional_analysis.md
    Given el solapamiento funcional entre US-036 (UI y reglas de negocio RBAC) y US-038 (infraestructura JWT, Redis, Sync EntraID)
    Then la directriz oficial de separación de responsabilidades es:
    And US-036 es responsable de: la Pantalla 14 (UI completa), la lógica de negocio de roles/permisos, los CRUDs de usuario/rol/delegación, y la generación de reportes.
    And US-038 es responsable de: la infraestructura de autenticación (JWT lifecycle, Redis blacklist, Fail-Open Policy), la sincronización periódica con EntraID, y el Sudo-Mode para operaciones destructivas.
    And el servicio de blacklist Redis es un componente COMPARTIDO: ambas historias lo consumen pero su implementación canónica reside en US-038.
    And TIENE PROHIBIDO que la US-036 implemente su propia lógica de invalidación de tokens separada de la US-038.
    And ambas historias DEBEN ser asignadas al mismo Arquitecto de Software para garantizar coherencia en el diseño de seguridad.

```
**Trazabilidad UX:** Wireframes Pantallas 14, 6, 7 y Workdesk (5).

---




### US-038: Asignación Multi-Rol y Sincronización EntraID
**Como** Administrador de Seguridad
**Quiero** asignar o sincronizar múltiples roles (Globales y de Proceso) a un mismo usuario autenticado
**Para** que pueda acceder a las distintas bandejas y tareas correspondientes a todos sus 'sombreros' operativos sin necesidad de tener cuentas separadas.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Multitenant RBAC, EntraID Sync & Identity Governance (Microservices Ready)

  # ==============================================================================
  # A. INICIO DE SESIÓN, JIT Y LA MUERTE SÚBITA (Arquitectura Stateless)
  # ==============================================================================
  Scenario: Tolerancia a Fallos del Kill-Switch (Redis Fail-Open Policy) (CA-01)
    Given la arquitectura de validación de Tokens (JWT) que consulta una Lista Negra en memoria (Redis) en <5ms para bloquear usuarios despedidos
    When el clúster de Redis sufre una caída temporal (Timeout) o partición de red (SPOF)
    Then la arquitectura exigirá un TTL máximo de 15 minutos al Token JWT base, y aplicará protección "Fail-Open Degradado".
    And el Gateway validará matemáticamente el Token JWT y PERMITIRÁ peticiones de sólo lectura (GET) para mantener viva la vista 360.
    And FORZARÁ "Fail-Closed" en toda mutación destructiva de estado (POST/PUT/DELETE) exigiendo escalamiento "Sudo-Mode", taponando vulnerabilidades de Separación de Funciones (SoD) si un usuario revocado aprovecha sus 15 minutos en la sombra.
    And paralelamente disparará una alerta técnica crítica al SysAdmin indicando: "Caché Offline - Operando en Degradación Segura sin Lista Negra".

  Scenario: Filtro de la Mochila Pesada (Anti-Token Bloat) (CA-02)
    Given un usuario (Ej: Gerente General) que pertenece a más de 150 grupos de seguridad en Microsoft EntraID
    When el Backend recibe el payload de grupos/roles del Identity Provider (o el flag de exceso `_odata.nextLink`)
    Then la arquitectura TIENE PROHIBIDO empaquetar cientos de roles irrelevantes dentro del Token JWT, previniendo que los servidores web colapsen con el error HTTP 431.
    And aplicará un "Filtro de Prefijo" estricto, ingiriendo e inyectando únicamente los roles que comiencen con la nomenclatura oficial de la plataforma (Ej: `ibpms_rol_*`).

  Scenario: Aprovisionamiento Just-In-Time (JIT) con Guardrail de Claims Mínimos Vitales (CA-03)
    Given un usuario nuevo que ingresa por primera vez a la URL del iBPMS vía SSO (EntraID)
    When el motor de Aprovisionamiento (JIT) parsea el Token de Microsoft para crearlo localmente
    Then el Backend evaluará el Token contra una matriz de "Claims Mínimos Vitales" (Ej: `Sucursal_ID`, `Codigo_Jefe`) necesarios para el enrutamiento de Camunda.
    And si el perfil corporativo de EntraID viene COMPLETO, lo deja pasar al Workdesk asignándole el rol inofensivo `[Ciudadano_Interno]`.
    But si el perfil corporativo está INCOMPLETO, el Frontend interceptará el acceso.
    And renderizará un Modal bloqueante de `[Completar Perfil Local]`, forzando al empleado a seleccionar o digitar los datos faltantes antes de habilitarle la plataforma, protegiendo la integridad del motor BPMN.

  # ==============================================================================
  # B. INFRAESTRUCTURA DE EMERGENCIAS Y CIERRE DE CICLO
  # ==============================================================================
  Scenario: Protocolo Break-Glass con Cierre de Ciclo Obligatorio (CA-04)
    Given que la infraestructura de Microsoft EntraID sufre una caída global (HTTP 503)
    Then el sistema habilitará un login de emergencia local ("Break-Glass Account") protegido por IP Whitelisting (Solo Red Corporativa/VPN).
    And por cumplimiento estricto de ciberseguridad (ISO 27001), ESTÁ PROHIBIDO referirse a esto como un "Backdoor" u ocultar la URL en el Frontend.
    And su uso exitoso disparará automáticamente alertas de Severidad Alta a la Gerencia de TI.
    When se restablezca el servicio de EntraID (resolución de la crisis)
    Then el iBPMS bloqueará las pantallas administrativas del Súper Admin con una alerta crítica (Tablero de Anomalías).
    And le exigirá mediante un Modal Inevitable rotar la contraseña o destruir las credenciales locales de la cuenta Break-Glass utilizada, erradicando el riesgo de dejar una "Puerta Trasera" durmiente.

  # ==============================================================================
  # C. LA PELEA DE PERMISOS Y ANOMALÍAS
  # ==============================================================================
  Scenario: Resolución Aditiva de Permisos (RBAC Simple) (CA-05)
    Given un usuario que hereda simultáneamente "Rol A" (Solo Lectura) y "Rol B" (Lectura y Escritura)
    Then el motor de políticas aplicará un modelo "Aditivo" estándar (Allow-Overrides) para la visibilidad de la UI.
    And el usuario gozará del súper-conjunto de permisos, evitando la extrema complejidad computacional de motores de reglas negativas (Deny-Overrides) en el MVP V1.

  Scenario: Detección y Contención de Segregación de Funciones (Juez y Parte) (CA-06)
    Given un usuario al que EntraID le ha inyectado por error roles incompatibles (Ej: "Creador de Pedido" y "Aprobador Financiero")
    When el usuario intenta aprobar una instancia de proceso que ÉL MISMO originó
    Then el sistema DEBE BLOQUEAR matemáticamente la transacción en el backend (Regla Fija: `Creator_ID != Approver_ID`).
    And el sistema le permitirá operar el resto de su día (Ej: Aprobar los pedidos de sus compañeros), pero JAMÁS cruzar el límite ético sobre su propia data.
    And disparará una Alerta Roja asíncrona hacia el Tablero de Anomalías de Seguridad (CA-12).

  # ==============================================================================
  # D. DELEGACIÓN Y RESCATE DE TAREAS (SRE Guaranteed Delivery)
  # ==============================================================================
  Scenario: Proxy Temporal de Autoridad y Exorcismo de Tareas Garantizado (CA-07)
    Given una Directora ("María") que sale de vacaciones por 15 días y tiene tareas operativas retenidas bajo su usuario (`assignee = maria`)
    When utiliza la Pantalla 14 para delegar su Rol jerárquico a un Coordinador ("Carlos")
    Then el sistema exige definir una [Fecha_Inicio] y [Fecha_Fin] estricta para la delegación.
    And la bitácora de auditoría estampará en cada acción de Carlos: "Ejecutado por: Carlos (En representación de: María)".
    And SIMULTÁNEAMENTE el iBPMS encola un evento asíncrono de "Auto-Unclaim Masivo" en el Message Broker (RabbitMQ) hacia Camunda.
    And si Camunda se encuentra Offline o en mantenimiento (HTTP 503), el Worker aplicará una Política de Reintentos (Retry Policy) y Dead Letter Queue (DLQ).
    And garantizando matemáticamente que el evento no se pierda y las tareas de María sean devueltas a la "Cola de Grupo" cuando el motor reviva, erradicando los Zombies irrecuperables.

  Scenario: El Exorcismo de Tareas por Despido (CA-08)
    Given una tarea operativa en Camunda asignada explícitamente a un empleado (`assignee = juan.perez`)
    When Juan renuncia y su perfil es desactivado en el módulo de seguridad
    Then el iBPMS no asumirá que Camunda se entera automáticamente.
    And el módulo de Identidad emitirá un evento interno asíncrono hacia RabbitMQ (con política de reintentos y DLQ igual al CA-07).
    And el Worker desencolará la orden, irá a Camunda y ejecutará un `Unclaim` masivo sobre TODAS las tareas vivas de Juan, devolviéndolas a disponibilidad pública para salvar los SLAs.

  # ==============================================================================
  # E. CONSOLIDACIÓN VISUAL Y TRAZABILIDAD EXTREMA
  # ==============================================================================
  Scenario: Trazabilidad Quirúrgica (Distributed Tracing V2 Ready) (CA-09)
    Given un usuario multi-rol ejecutando una transacción crítica
    When el Backend estampa el evento en la bitácora de auditoría
    Then almacenará el `user_id`, `timestamp` y un JSON inmutable con los "Roles Activos" (Claims) de su JWT en ese milisegundo exacto.
    And OBLIGATORIAMENTE inyectará un `Correlation-ID` o `Trace-ID` transversal en los Headers HTTP, garantizando que al migrar a Microservicios (V2), los auditores puedan rastrear el hilo de la transacción a través de todas las bases de datos.

  Scenario: Consolidación Transversal e Insignia de Procedencia (CA-10)
    Given un usuario con 3 roles operativos distintos
    When abre su vista de Workdesk
    Then el sistema consolida TODAS sus tareas en una única grilla unificada sin forzar saltos de perfil.
    And inyecta un Badge visual discreto en cada fila (Ej: `Rol: Aprobador_Nivel_2`) explicándole al usuario bajo qué prerrogativa de negocio se le exige resolver ese caso específico.

  Scenario: Indicador Tipográfico de Dominio en Cabecera (CA-11)
    Given el usuario multi-rol navegando la plataforma
    Then el Master Header renderizará un micro-texto o chip resumiendo visualmente sus 2 o 3 "Sombreros Principales" (Ej: `Director Comercial | Aprobador VIP`), validando que su sincronización con EntraID fue exitosa.

  # ==============================================================================
  # F. TABLERO DE ANOMALÍAS Y MANTENIMIENTO
  # ==============================================================================
  Scenario: Tablero de Resolución de Anomalías de Seguridad (CA-12)
    Given que el sistema detecta alertas de seguridad pasivas (Ej: El Conflicto SoD del CA-06 o el Break-Glass del CA-04)
    When el Administrador de Seguridad ingresa a la Pantalla de Configuración / RBAC (Pantalla 14)
    Then el sistema debe darle acceso a una pestaña especializada denominada "Tablero de Anomalías"
    And este tablero listará en color Rojo todas las incidencias de seguridad vivas detectadas por el motor.
    And obligará al Administrador a revisar el caso, subsanar el error a nivel EntraID/Local, y presionar físicamente un botón `[ ✅ Marcar como Subsanado ]` para limpiar la alerta del sistema.

  Scenario: Postergación de Reset de Password para V2 (CA-13)
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


## ÉPICA 14: Configuraciones Globales de Nivel de Servicio - SLA (US-043)
Permite a la PMO establecer las reglas del juego a nivel corporativo paramétricas (Matriz de días hábiles, umbrales de vencimiento).

### US-043: Configuración Global de Service Level Agreements (SLA)
**Como** PMO / Administrador Estratégico
**Quiero** disponer de una pantalla matriz de configuración central
**Para** que el motor de orquestación y el BAM no cuenten domingos o feriados en horas inhábiles ajustando la métrica a las "Horas reales corporativas".

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Business SLA Matrix Configuration and Multi-Zone Time-Warp Prevention
  Scenario: Inyección Arquitectónica del BusinessCalendar en Camunda Engine (CA-1)
    Given el administrador accede a la Matriz de Negocio (Pantalla 19)
    When se habilitan los Días Hábiles forzosamente basados en Horas (Ej: Lunes a Viernes de 8:00 a 17:00)
    Then el iBPMS TIENE ESTRICTAMENTE PROHIBIDO dejar que Camunda calcule los SLAs operativos usando su reloj UTC absoluto (24/7).
    And el Backend DEBE inyectar un Custom `BusinessCalendar` en el *Job Executor* del Engine de Camunda.
    And este Custom Calendar interceptará matemáticamente los `Timer Boundary Events` y `Due Dates` de tareas Humanas (`UserTasks`), leyendo en caliente la Matriz SLA de la BD.
    And garantizando que si una tarea (SLA 4 Hrs) entra un Viernes a las 16:00, el motor pause su cronómetro el fin de semana, detonando el Lunes a las 11:00 AM, protegiendo las métricas operativas (BAM).

  Scenario: Exención de Pausa para Timers Netamente Sistémicos (CA-2)
    Given procesos transaccionales autónomos (Ej: Conciliaciones MLOps o Purga de Datos) que deben ejecutarse los Domingos a las 3:00 AM
    When el Timer Event de tipo "System" se dispare según su configuración BPMN (Start Timer / System Catch)
    Then el Custom `BusinessCalendar` TIENE PROHIBIDO pausar estos cronómetros o recalcularlos al Lunes.
    And el Arquitecto BPMN deberá estipular visualmente una propiedad de extensión en Camunda (Ej: `camunda:property name="isBusinessSla" value="false"`) para saltar el bloqueo del calendario corporativo en hilos de máquina.

  Scenario: Recálculo Retroactivo Restringido a Batch Job (Anti-Deadlocks) (CA-3)
    Given que el administrador altera el rango de horas hábiles (Ej: de 17:00 a 16:30) y activa el Toggle de "Aplicar Retroactivamente a Tareas Vivas"
    When el PMO oprime `[Aplicar Matriz]`
    Then el Backend REST rechaza estructuralmente ejecutar el recálculo masivo de manera síncrona/inmediata en esa misma petición HTTP para prevenir Timeouts y Deadlocks de BD.
    And el sistema encolará un Job Asíncrono de tipo Batch por detrás que consumirá exclusivamente gRPC o la API asíncrona de Zeebe 8, modificando los Timer Boundary Events de forma nativa sin interactuar jamás con bases relacionales SQL, preservando la arquitectura RocksDB Stateless.
    And el UI mostrará un Modal informativo: "Recálculo masivo en progreso. Los SLAs vivos se actualizarán gradualmente en los próximos minutos".

  Scenario: Husos Horarios Estrictos en Geografías Híbridas (Timezones) (CA-4)
    Given que el cliente (Tenant) opera con usuarios en diferentes zonas horarias (Ej: UTC-5 Bogotá y UTC+1 Madrid)
    When el Custom `BusinessCalendar` intercepta un Timer de una Tarea Humana Asignada
    Then el motor priorizará la Zona Horaria (Timezone) predefinida en el Perfil del Trabajador `Assignee` o del `Candidate Group` en su defecto.
    And si un analista Europeo recibe un tarea, el fin de semana del motor de Camunda comenzará a aplicar 6 horas antes que para su homólogo en América, asegurando justicia laboral y SLAs inquebrantables cross-border.

  Scenario: Automatización de Festivos Externos con Fallback (CA-5)
    Given la necesidad legal de bloquear los contadores de SLA durante días de asueto local
    Then la matriz se sincroniza con una API Pública gubernamental o en la nube para auto-poblar los Días Feriados del Tenant específico.
    And si la API proveedora se cae, el sistema hace un "Fallback" a un grid manual editable en la Pantalla 19 por el PMO.

  Scenario: Alertas Preventivas de Quiebre de Nivel (Early Warning) (CA-6)
    Given que el temporizador (Ticking Engine) de una tarea se aproxima al 80% o "2 Horas restantes" de su tiempo total
    Then el motor SLA dispara automáticamente una alerta (hacia el Motor de Notificaciones US-049).
    And garantizando tiempo de reacción humano antes del verdadero vencimiento legal/operativo.
```
**Trazabilidad UX:** Wireframes Pantalla 19 (Configuración SLA).

---






## ÉPICA 15: Developer Portal, Settings y Límites del Sistema (US-042, US-044, US-045, US-046)
*(MUST HAVE)* - El "Cockpit" centralizado para Súper Administradores, donde se gobiernan los umbrales cognitivos, interruptores y caducidades arquitectónicas de todo el iBPMS en tiempo de ejecución, sin necesidad de despliegues de código o edición en la base de datos directa.


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




### US-044: Gobernanza de Inteligencia Artificial (AI Limits & MLOps)
**Como** Súper Administrador
**Quiero** una pestaña de configuraciones dedicada al Motor Cognitivo
**Para** gobernar empíricamente el grado de libertad de la IA, auditar sus sesgos, gestionar las listas negras y controlar el ciclo de vida de los modelos sin colapsar la base de datos de producción.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: AI Governance Center, Telemetría MLOps y Micro-Control Cognitivo

  # ==============================================================================
  # A. GOBERNANZA DE INTAKE Y AUTO-PILOT
  # ==============================================================================
  Scenario: Feature Toggle Global del "Auto-Pilot" y No-Retroactividad (CA-01)
    Given el panel de administración central de IA en la Pantalla 15.A
    Then debe existir un Master Switch de `[Permitir Instanciación Autónoma AI]`.
    And este switch opera de forma GLOBAL (apaga o enciende la IA para todos los procesos en V1).
    And si está apagado, TODAS las Action Cards caerán forzosamente al Embudo Humano (Pantalla 16).
    And si el Administrador vuelve a ENCENDER el switch, la IA TIENE PROHIBIDO autoprocesar las tarjetas que ya estaban en cuarentena de manera retroactiva, exigiendo revisión humana para las antiguas y aplicando el Auto-Pilot solo a los correos nuevos.

  Scenario: Parametrización de Certeza Dinámica (Tolerance Score) (CA-02)
    Given el motor de inferencia que calcula la confianza matemática de sus predicciones
    Then el Súper Administrador DEBE disponer de un Slider o Campo Numérico (Ej: 0-100%) en la UI.
    And prohibiendo el "hardcoding" en el backend, este umbral dictará la Certeza Mínima Requerida en tiempo real.
    And cualquier inferencia por debajo del umbral parametrizado será enviada obligatoriamente a revisión humana (Fallback).

  # ==============================================================================
  # B. AUDITORÍA ANTI-OVERFITTING (TRANSPARENCIA Y AMNESIA)
  # ==============================================================================
  Scenario: Traducción Semántica de Tensores (Explainable AI - XAI) (CA-03)
    Given el proceso nocturno del Agente Data Scientist
    Then el Administrador posee una pantalla "AI Audit Log".
    And el sistema utilizará un micro-LLM auxiliar inverso (XAI) para traducir los deltas vectoriales matemáticos hacia "Jerga Legible por Humanos" (Ej: `Aprendizaje 1: La palabra 'Reclamo' levanta flag de prioridad Alta`).
    
  Scenario: Efecto Cascada de la Amnesia (Negative Prompting Cache) (CA-04)
    Given la pantalla de "AI Audit Log"
    When el Administrador selecciona un Patrón obsoleto o erróneo y oprime `[Eliminar Patrón]`
    Then el sistema NO ejecutará un costoso reentrenamiento de la BD Vectorial en caliente.
    And inyectará instantáneamente el patrón rechazado como un "Negative Prompt" (System Instruction) en la caché de memoria RAM del LLM.
    And forzará el desaprendizaje cognitivo en tiempo real en milisegundos, delegando el borrado físico de los vectores para el proceso Batch de la madrugada.

  # ==============================================================================
  # C. ROLLBACK Y GESTIÓN DE BASES DE DATOS VECTORIALES
  # ==============================================================================
  Scenario: Integridad Transaccional en Blue-Green Swapping y Límite N-1 (CA-05)
    Given un escenario de degradación aguda de la IA (Ej: Alucinaciones masivas)
    When el Administrador presione el botón de emergencia `[Revertir Modelo Anterior]`
    Then el Backend ejecutará un "Blue-Green Data Swapping" SQL en milisegundos (`is_active_model = FALSE/TRUE`).
    And la plataforma V1 solo soportará memoria de reversión **N-1** (El modelo de hoy y el de ayer) para proteger los costos Cloud.
    And las transacciones de Camunda en vuelo que fallen en ese microsegundo sufrirán Degradación Elegante, siendo reintentadas por RabbitMQ a los 5 segundos contra el modelo ya restaurado.

  Scenario: Garbage Collection Vectorial (Ahorro Cloud) (CA-06)
    Given la generación constante de snapshots vectoriales tras los reentrenamientos y rollbacks
    Then el sistema ejecutará un Job de Mantenimiento programado semanal (Ej: Domingos 03:00 AM).
    And ejecutará un `HARD DELETE` físico sobre cualquier modelo marcado como inactivo (`is_active_model = FALSE`) que supere los 7 días de antigüedad, evitando facturas desmesuradas en `pgvector`.

  # ==============================================================================
  # D. OPERATIVIDAD MLOPS DEL DATA SCIENTIST Y RESILIENCIA
  # ==============================================================================
  Scenario: Prevención de Solapamiento de Cron Jobs (ShedLock Mutex) (CA-07)
    Given el reentrenamiento masivo programado (Ej: Diario a las 02:00 AM)
    When el servidor intenta lanzar la instancia de hoy, pero la instancia de ayer sigue en estado `RUNNING` (Ej: el procesamiento tomó 26 horas)
    Then el Backend DEBE utilizar un Database Lock (Ej: librería `ShedLock` o Mutex nativo).
    And al detectar el candado, ABORTARÁ silenciosamente la ejecución del Job nuevo (Skip).
    And prevendrá el colapso del servidor por *Out of Memory* (OOM), emitiendo una alerta técnica al SysAdmin.

  Scenario: Manejo de Errores Silenciosos y Aislamiento Tenant (CA-08)
    Given una falla persistente en el Job Nocturno (Ej: Timeouts en la BD)
    When el Job de reentrenamiento falla durante 3 días consecutivos
    Then el sistema TIENE PROHIBIDO detener la operación diurna o apagar los Embudos de Inteligencia Artificial.
    And entrará en "Modo Supervivencia", operando con el último modelo estable conocido y encendiendo una Alerta Roja inborrable en el Dashboard de SysAdmin.
    And en despliegues Multitenant, el reentrenamiento usará Colas Dedicadas por Tenant en RabbitMQ, asegurando que el volumen de un Cliente no asfixie el reentrenamiento de los demás.

  # ==============================================================================
  # E. SEGURIDAD PERIMETRAL
  # ==============================================================================
  Scenario: Sensibilidad y Normalización Absoluta de Lista Negra (El Guardia Tonto) (CA-09)
    Given el componente de inyección de dominios prohibidos (Blacklist)
    When el Administrador inyecta un dominio "sucio" en la UI (Ej: `  @GMAIL.COM  `)
    Then el Interceptor del Backend TIENE ESTRICTAMENTE PROHIBIDO guardar el input crudo.
    And aplicará obligatoriamente una normalización de limpieza (`.toLowerCase().trim()`) ANTES del Commit SQL en la tabla `ibpms_public_domains_blacklist`.
    And el motor aplicará esta misma normalización a los correos entrantes antes de comparar, garantizando un blindaje matemático total contra bypasseos de ciberseguridad por errores de digitación.

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

  Scenario: Persistencia Híbrida de Formularios en Ágil (JSONB Pocket)
    Given que la arquitectura Ágil/Kanban rechaza el uso de Camunda (CMMN/BPMN) para favorecer la velocidad pura de Base de Datos Relacional (JPA)
    When un Arquitecto asocie un Formulario Zod (iForm Maestro o Genérico) a una Tarjeta Kanban y el operario oprime [Guardar Progreso]
    Then la tabla relacional `ibpms_kanban_tasks` DEBE contar con una columna especializada de tipo `JSONB` (o su equivalente estructurado).
    And el Backend serializará y guardará el Payload completo validado por Zod directamente dentro de esta columna de la entidad.
    And garantizando que la tarjeta Ágil soporte la captura de datos estructurados sin ensuciar la base de datos con tablas hijas.
    And OBLIGATORIAMENTE este ID KanBan convivirá con el ecosistema de Zeebe en una capa de Proyección CQRS Central (Ej: ibpms_global_worklist_view), inyectando una "Vista 360" en ES/RDBMS que aborte el divorcio entre tareas CMMN y Tareas Ágiles.
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

### US-049: Motor Central de Notificaciones y Plantillas (Outbound Engine)
**Como** Administrador del Sistema / PMO
**Quiero** disponer de un motor centralizado que gestione todas las salidas de correos electrónicos y notificaciones
**Para** administrar plantillas dinámicas visualmente, evitar textos quemados en el código fuente y garantizar que el envío de correos no bloquee transaccionalmente el motor de Camunda.

**Criterios de Aceptación (Gherkin):**
Feature: Central Outbound Notification Engine
```gherkin
  Scenario: Prohibición de Textos Quemados (Hardcoding) en Backend
    Given la necesidad estructural del sistema de enviar correos electrónicos (Ej: Confirmaciones US-022, Alertas US-040)
    Then la arquitectura TIENE ESTRICTAMENTE PROHIBIDO que los desarrolladores redacten el HTML o el asunto (Subject) de los correos dentro del código fuente (Java/Node).
    And el sistema debe proveer un CRUD de "Plantillas de Notificación" (Templates) en la Interfaz Administrativa (Pantalla 15), utilizando un motor de renderizado estándar (Ej: Thymeleaf, FreeMarker o Handlebars).
    And las plantillas deben soportar inyección de variables dinámicas (Ej: `Hola {{cliente.nombre}}, tu caso {{caso.id}} ha sido radicado`).

  Scenario: Despacho Asíncrono Estricto (Patrón Outbox)
    Given que el motor Camunda llega a una `SendTask` o `ServiceTask` configurada para notificar al cliente
    When el hilo de ejecución intenta despachar el correo a través del servidor SMTP o MS Graph API
    Then el motor de procesos NO ESPERARÁ la respuesta del servidor de correos (Evitando el bloqueo del Main Thread y Timeouts transaccionales).
    And empaquetará el payload del correo y lo arrojará a una Cola de Mensajería dedicada (Ej: `outbound-email-queue` en RabbitMQ definida en US-034).
    And un Worker independiente desencolará y ejecutará el envío real hacia internet.

  Scenario: Resiliencia y Tolerancia a Caídas del Servidor de Correo
    Given el Worker independiente procesando la cola de correos salientes
    When el servidor SMTP corporativo del cliente (Ej: Office 365 / Exchange) sufre una caída temporal (HTTP 503 / Timeout)
    Then el Worker no descartará el correo ni fallará la transacción de negocio principal.
    And aplicará una política de reintentos con "Exponential Backoff" (Ej: reintentar en 1 min, luego en 5 min, luego en 15 min).
    And si agota los reintentos máximos, trasladará el correo a una Dead-Letter Queue (DLQ) y emitirá una alerta visual en el Dashboard de TI para intervención manual.

  Scenario: Auditoría Forense de Salida (Outbound Audit Trail)
    Given un correo electrónico de respuesta de fondo o confirmación enviado al cliente
    When el servidor SMTP confirma el despacho (Status 200 OK)
    Then el Motor de Notificaciones debe generar una copia inmutable del HTML exacto y los metadatos de envío.
    And debe registrar esta copia en la tabla `ibpms_outbox_log` vinculada al `Process_Instance_ID`.
    And debe proyectar este registro visualmente en la pestaña "Contexto y Correos" del Workdesk proveyendo al analista de una prueba legal irrefutable de qué se le dijo al ciudadano y cuándo.

  Scenario: Agrupación Anti-Spam (Digest / Throttling)
    Given un error de diseño de un Arquitecto (Ej: Ciclo infinito en BPMN) o una caída masiva de SLAs donde 150 casos vencen simultáneamente
    When el motor dispara las alertas hacia el correo del "Jefe de Área"
    Then el Notification Engine aplicará una regla paramétrica de "Agrupación Temporal" (Throttling Window, Ej: 15 minutos) por destinatario.
    And en lugar de bombardear al Jefe con 150 correos individuales colapsando su bandeja, el motor consolidará los eventos en un único correo tipo "Digest": `[Alerta Masiva: 150 SLAs han sido vulnerados en los últimos 15 min. Vaya al Dashboard]`, protegiendo la reputación del dominio (Anti-Spam).

  Scenario: Extracción e Inyección de Anexos Físicos con Streaming Activo (Outbound Zero-RAM)
    Given el Motor de Notificaciones procesando un correo en la cola de salida (RabbitMQ)
    When la tarea transaccional de Camunda incluya un Array de identificadores documentales (Ej: `attachments: ["UUID-A"]`)
    Then el Worker de Notificaciones hará una pausa antes de conectarse al servidor SMTP.
    And se autenticará contra la Bóveda SGDEA (SharePoint - US-035) utilizando esos UUIDs.
    And TIENE PROHIBIDO descargar binarios corporativos hacia la memoria RAM (Heap) del Servidor para evitar Out Of Memory (OOM).
    And realizará Piping HTTP bidireccional (Streams directos) hacia MS Graph API, o en su defecto recaerá en staging OS de memoria Flash (`/tmp`).
    And conectará en caliente el pipeline al formato adjunto (`Attachments`) en la trama del correo electrónico saliente, manteniendo el NodeWorker inmutable.

Scenario: Infraestructura de Notificaciones In-App (WebSocket Campana)
    Given la necesidad de alertar a un usuario internamente (Ej: SLA a punto de vencer, Tarjeta IA asignada)
    When el Motor de Notificaciones procesa un evento configurado con el canal `IN_APP`
    Then el sistema persistirá el registro en la tabla relacional `ibpms_inapp_notifications` con estado `is_read = false`.
    And despachará instantáneamente un push payload vía WebSocket al Frontend del usuario objetivo.
    And el Frontend incrementará el contador rojo (Badge) de la Campana en el Master Header de forma reactiva, sin requerir refresco de pantalla (F5).
    And la UI proveerá un endpoint ligero `PATCH /read` que se disparará al abrir el panel, atenuando el contador.

```
### US-050: Identidad y Onboarding de Clientes Externos (CIAM / Zero-Public-Signup)
**Como** Sistema Core (iBPMS)
**Quiero** enviar una invitación segura (Magic Link) al correo de un cliente externo
**Para** que pueda crear su contraseña y acceder al Portal B2C, amarrando su usuario criptográficamente a su CRM_ID sin abrir formularios de registro público.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Secure Customer Onboarding and Identity (CIAM)

  Scenario: Prohibición de Registro Público (Zero-Public-Signup)
    Given la pantalla de Login del Portal Externo (portal.ibpms.com)
    Then la interfaz NO DEBE tener ningún enlace, botón o formulario que diga "Registrarse" o "Crear Cuenta".
    And la creación de identidades ciudadanas (External Users) solo puede nacer desde el interior del iBPMS (Vía API o evento interno), blindando el sistema contra bots.

  Scenario: Disparo de Invitación (Magic Link) por Evento
    Given un Cliente nuevo registrado en el CRM con el ID `CUST-999` y correo `juan@gmail.com`
    When el proceso BPMN llega a una tarea de "Invitar a Portal" O un analista oprime [Invitar] en la Vista 360
    Then el sistema generará un Token criptográfico de uso único (Magic Link).
    And el Motor de Notificaciones enviará un correo a `juan@gmail.com` con el botón "Crear mi Contraseña de Acceso".
    And el Magic Link tendrá una caducidad (TTL) rígida paramétrica (Ej: 24 horas).

  Scenario: Aterrizaje y Vinculación Criptográfica (Account Claiming)
    Given el cliente Juan que hace clic en el Magic Link dentro del tiempo permitido
    When aterriza en la página de "Definir Contraseña" del Portal B2C
    Then el sistema verifica que el Token no haya sido usado antes y bloquea la edición del campo de correo electrónico (Read-Only).
    And Juan digita su contraseña (cumpliendo políticas corporativas).
    And el sistema inscribe la cuenta en el Identity Provider (Azure AD B2C / Local).
    And OBLIGATORIAMENTE graba el valor `CUST-999` como un atributo inmutable (Custom Claim) dentro del Token del usuario (El "Bolsillo Secreto").
    And garantizando que a partir de ese momento, el candado Anti-BOLA (US-026) lea este atributo en cada inicio de sesión, impidiendo matemáticamente que Juan vea datos de otros clientes.
```

---
### US-051: Matriz de Gobernanza Visual y Enrutamiento RBAC (Frontend)
**Como** Administrador de Seguridad (CISO) / Arquitecto Frontend
**Quiero** que el motor de Vue.js gestione la visibilidad del DOM, la navegación de rutas y el estado reactivo con seguridad militar
**Para** garantizar cero fugas de información por parpadeos visuales (FOUC), proteger contra la adivinación de rutas por atacantes (URL Guessing), y soportar la fusión de múltiples roles dinámicos sin asfixiar la UX.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Frontend Visual Governance, Anti-FOUC and SRE Router Guards

  # ==============================================================================
  # A. RESOLUCIÓN DEL ESTADO Y PREVENCIÓN DE AMNESIA (GAPs 16 y 17)
  # ==============================================================================
  Scenario: Hidratación Síncrona del Estado Reactivo (Anti-Amnesia de F5)
    Given la arquitectura Single Page Application (SPA) basada en Vue 3 y Pinia
    When un usuario logueado presiona [F5] o recarga directamente una URL profunda (Ej: `/admin/modeler`)
    Then el interceptor de navegación (`router.beforeResolve`) TIENE PROHIBIDO evaluar los permisos instantáneamente.
    And deberá invocar una promesa bloqueante (`await hydrateAuth()`) forzando al Router a esperar a que Pinia recupere el Token del LocalStorage y recalcule los Claims.
    And previniendo falsos positivos de expulsión (403) causados por la latencia de lectura de la memoria RAM.

Scenario: Renderizado Progresivo Estricto y FOUC Controlado (LCP Optimization)
    Given el proceso de montaje de la aplicación (SPA Vue 3)
    When el usuario ingresa a la URL
    Then el Frontend renderizará INMEDIATAMENTE el App Shell (Sidebar y Header Maestros) basándose en los Claims básicos del JWT en Caché para garantizar una métrica óptima de Largest Contentful Paint (LCP).
    And el `[Skeleton Loader Transversal]` se aplicará ESTRICTAMENTE solo sobre el contenedor `<Router View>` (Main Content).
    And este Skeleton central solo se destruirá cuando las promesas asíncronas de permisos RBAC del Backend se resuelvan completamente.
    And garantizando fluidez de navegación ultrarrápida sin generar parpadeos (FOUC) de botones prohibidos en la zona de trabajo.

  # ==============================================================================
  # B. DEFENSA PERIMETRAL Y RUTAS (GAP 18)
  # ==============================================================================
  Scenario: Gaslighting Cibernético (Security by Obscurity 404 vs 403)
    Given un usuario operativo o externo que adivina e intenta acceder a una URL restringida (URL Guessing)
    When el Router Guard intercepta la navegación detectando permisos insuficientes (Token válido, pero sin Rol)
    Then la arquitectura TIENE PROHIBIDO redirigirlo al Workdesk `/` emitiendo un "403 Forbidden" (lo cual confirmaría que la ruta confidencial existe).
    And el Router inyectará de frente el componente `NotFound404.vue` (Página no encontrada) manteniendo intacta la URL en la barra de direcciones.
    And impidiendo matemáticamente que un hacker logre mapear la estructura de directorios del sistema.

  Scenario: Jerarquía de Redirección y Atesorador de Enlaces
    Given el Router Guard evaluando una excepción de acceso
    When determina la causal de la penalización
    Then si el Token JWT EXPIRÓ (401): Redirigirá pasivamente a `/login`, limpiando el Storage.
    And si el Token VIVE pero el usuario guardó un "Hyperlink Viejo" de un menú al que ya no tiene acceso: Aplicará el escenario de Falso 404 SIN destruir su LocalStorage, protegiendo los borradores lícitos que esté trabajando en otras pestañas.

  Scenario: Excepciones Perimetrales Controladas (Magic Links y Docs)
    Given la existencia de rutas transitorias y documentación técnica
    Then el Router Guard poseerá una bandera `meta: { isPublic: true }`.
    And omitirá la evaluación RBAC pesada para: Pantallas B2C accedidas mediante "Magic Links" (US-050), y Rutas técnicas locales (Swagger/Storybook), acelerando la carga sin comprometer el Core.

  # ==============================================================================
  # C. COMPOSICIÓN DINÁMICA DE MENÚS Y PRIVILEGIOS
  # ==============================================================================
  Scenario: Backend-Driven UI, Auto-Colapso de Nodos y Caché de Menú
    Given la fusión de múltiples roles en un mismo usuario
    When el Sidebar calcula las carpetas a renderizar
    Then la matriz de "Permisos vs Rutas" NO vivirá codificada en duro (Hardcoded) en el Router de Vue, sino que será inyectada mediante un JSON asíncrono desde el Backend.
    And si el cruce de roles oculta todos los sub-menús de una categoría padre (Ej: Ocultamos BPMN y Formularios), la carpeta padre completa "Administración" se ocultará automáticamente del DOM (Auto-Collapse).
    And el árbol de navegación resultante será cacheado en Pinia tras el Login para no re-computar directivas en cada transición de vista.

  Scenario: Dashboard Bifurcado por Composición de Widgets
    Given la ruta raíz del sistema `/` (Workdesk)
    When diferentes roles (Operador vs Súper Admin) acceden a la misma URL
    Then el sistema TIENE PROHIBIDO redirigir a rutas hardcodeadas separadas (Ej: `/dashboard-admin`).
    And utilizará la misma vista raíz inyectando dinámicamente (Component Composition) los *Widgets* (Grafana vs Grillas Kanban) según los permisos aditivos de Pinia en la misma coordenada web.

  Scenario: Dependencias Cruzadas y Privilegios de Solo Lectura (Granularidad CRUD)
    Given un Arquitecto de Procesos que necesita invocar una Regla IA dentro de su diagrama BPMN
    Then el Frontend le otorgará un privilegio degradado (Read-Only) hacia la ruta del Diccionario de la IA.
    And le permitirá consultar el catálogo, pero la directiva condicional a nivel de componente ocultará/destruirá físicamente los botones de `[+ Nueva Regla]` y `[Eliminar]`, reservados para el Administrador IA.

  # ==============================================================================
  # D. CONTROLES DE ALTA FRICCIÓN Y SALVAVIDAS
  # ==============================================================================
  Scenario: Re-Autenticación para Funciones Destructivas (Sudo Mode)
    Given una sesión iniciada bajo el rol máximo de `ROLE_SUPER_ADMIN`
    When este usuario intenta ejecutar una acción destructiva (Ej: Purgar BD, Borrar Tenant)
    Then la validación estándar del Router NO es suficiente.
    And el Frontend suspenderá el POST y renderizará un "Re-Prompt" (Modal de Seguridad) exigiendo la re-digitación de la contraseña o token EntraID para confirmar la transacción, previniendo secuestros de sesión en PCs desbloqueadas.

  Scenario: Auditoría Forzosa al Revelar Secretos API (El Ojo de Sauron)
    Given el rol `ROLE_INTEGRITY_ENGINEER` ingresando a la vista "Integraciones API"
    When el componente se monta para mostrar credenciales o Tokens OAuth estáticos
    Then los Secretos se renderizarán ofuscados por defecto (`*****************`).
    And al hacer clic en "Mostrar 👁️", el Frontend disparará obligatoriamente un evento asíncrono de Telemetría (Audit-Log POST) hacia el backend registrando la visualización del secreto en ese milisegundo.

  Scenario: Revocación en Caliente y Botón de Pánico Incondicional (Return Home)
    Given la operativa en tiempo real del Frontend
    When un Súper Administrador revoca un rol a un usuario conectado
    Then un evento WebSocket (`[ROLE_REVOKED]`) obligará a Pinia a expulsar al usuario al `/login` en vivo.
    And en caso de que un usuario quede atrapado en un "Dead Loop" de redirecciones por fallos de permisos locales, el *Master Layout* garantizará la renderización incondicional del botón `[Cerrar Sesión / Ir al Inicio]` por fuera del `router-view` para forzar la limpieza del estado.
```
**Trazabilidad UX:** Componentes de Navegación Global Vue Router (`router/index.ts`) y Menú Lateral (`MainLayout.vue`).

---

## ÉPICA 16: Persistencia Hexagonal y Patrón CQRS
Regula la inmutabilidad de los datos recolectados, previniendo la contaminación del Motor BPMN y aislando las lecturas masivas de las escrituras transaccionales.

### US-017: Ejecución y Persistencia Inmutable de Formularios (CQRS & Event Sourcing)
**Como** Analista / Motor Backend Hexagonal
**Quiero** diligenciar la información de mi tarea, almacenando las subidas temporales (Drafts) y transacciones finales de forma inmutable
**Para** garantizar cero bloqueos concurrentes, trazabilidad absoluta y finalizar exitosamente mi actividad sin contaminar el motor de Camunda (separando lectura de escritura).


**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Hexagonal CQRS Persistence, Zero-Trust Validation and Task Completion

  # ==============================================================================
  # A. EJECUCIÓN BASE Y VALIDACIÓN DE DATOS (HAPPY & SAD PATHS)
  # ==============================================================================
  Scenario: Enviar datos válidos de formulario (CA-1)
    Given la tarea "TK-100" asignada a "carlos.ruiz" requiere el formulario "Form_Aprobacion_V1"
    And "Form_Aprobacion_V1" exige el campo obligatorio numérico "monto_aprobado"
    When "carlos.ruiz" realiza un POST a "/api/v1/workbox/tasks/TK-100/complete"
    And incluye en el body el JSON '{"variables": {"monto_aprobado": 1500, "comentarios": "Ok"}}'
    Then el sistema debe retornar un HTTP STATUS 200 OK
    And la tarea "TK-100" marca su estado interno como "COMPLETED"
    And las variables del JSON se persisten inmutablemente asociadas a la instancia del proceso.

  Scenario: Enviar datos inválidos (Violación del JSON Schema) (CA-2)
    Given la tarea "TK-100" requiere el campo obligatorio "monto_aprobado" numérico
    When "carlos.ruiz" realiza un POST a "/api/v1/workbox/tasks/TK-100/complete"
    And incluye un JSON vacío '{"variables": {}}'
    Then el sistema valida el payload contra el JSON Schema registrado para "Form_Aprobacion_V1"
    And el sistema debe retornar un HTTP STATUS 400 Bad Request
    And el error format JSON debe especificar de forma estructurada: `{"error": "ValidationFailed", "fields": [{"field": "monto_aprobado", "message": "Required"}]}`

  # ==============================================================================
  # B. INICIALIZACIÓN Y CONTEXTO UI (PATRÓN BFF Y LAZY PATCHING)
  # ==============================================================================
  Scenario: Inyección Megalítica de Contexto (Patrón BFF) (CA-3)
    Given la entrada física a la vista de la tarea operativa (Pantalla 2)
    When el Frontend inicializa el componente Vue
    Then despachará EXACTAMENTE UNA (1) única petición GET consolidada a `/api/v1/workbox/tasks/{id}/form-context`
    And el Backend obrará como BFF (Backend for Frontend) inyectando en un Mega-DTO la triada: [Esquema Zod Vigoroso + Layout UI de Vue + Variables Históricas de Solo Lectura extraídas de Camunda (`prefillData`)]
    And este DTO incluirá obligatoriamente la versión exacta del esquema (`schema_version`) para poblar inputs en un solo tick de renderizado y prevenir choques generacionales si el Arquitecto modifica el diseño mientras el caso está en vuelo.

  Scenario: Hibridación de Datos Históricos vs Nuevos Contratos (Lazy Patching) (CA-4)
    Given el BFF inyectando `prefillData` de una Instancia antigua (V1) hacia un Formulario Zod nuevo (V2)
    When existan campos obligatorios nuevos en la V2 que no venían en la data histórica de Camunda (`null` o `undefined`)
    Then el esquema Zod reactivo los evaluará inmediatamente como inválidos iluminando dichos inputs en ROJO
    And el Frontend bloqueará físicamente el botón de [Enviar]
    And obligará procedimentalmente al analista a auditar el dato, contactar al cliente y digitar la información faltante en la UI para poder avanzar el proceso (Amnistía en Lectura, Guillotina en Escritura).

  # ==============================================================================
  # C. CARGA BINARIA Y SEGURIDAD PERIMETRAL DE ARCHIVOS
  # ==============================================================================
  Scenario: Desacoplamiento de Carga Binaria (Upload-First) y Escudo Anti-IDOR (CA-5)
    Given un formulario Zod que incluye un componente `<InputFile>`
    When el usuario final adjunta un documento pesado (Ej: PDF de 10MB)
    Then el Frontend ejecutará una carga asíncrona temprana (Pre-Submit) hacia la Bóveda SGDEA (`/api/v1/documents/upload-temp`) obteniendo un Identificador Único (`UUID`)
    And al presionar [Enviar], el POST a `/complete` enviará EXCLUSIVAMENTE el JSON plano referenciando el ID (`{"cedula_pdf": "UUID-123"}`), teniendo PROHIBIDO arquitectónicamente enviar payloads Multipart o Base64 contra el motor de procesos Camunda
    And la arquitectura TIENE ESTRICTAMENTE PROHIBIDO enlazar ciegamente ese archivo a la tarea
    And el Backend validará en la tabla de adjuntos temporales que `UUID-123` pertenezca al `user_id` logueado Y haya sido subido en el contexto de esa misma `task_id` (Defensa Anti-IDOR)
    And si detecta un UUID ajeno, abortará la transacción con `HTTP 403 Forbidden`
    And un Cron Job nocturno destruirá físicamente de S3/SGDEA cualquier archivo temporal (TTL > 24h) sin confirmación transaccional para evitar facturas por almacenamiento basura.

  # ==============================================================================
  # D. RESILIENCIA OFFLINE, UX EVENTUAL Y PROTECCIÓN DE ESTADO
  # ==============================================================================
  Scenario: Trazabilidad Volátil, Draft Sync y Cifrado PII en LocalStorage (CA-6)
    Given la digitación continua de un analista en un iForm masivo abierto en el Workdesk
    Then el Frontend guardará el borrador (Draft) asíncronamente en el `LocalStorage` del navegador atado al `Task_ID` (mediante `@vueuse/core`) a cada tecla
    But si el esquema Zod marca campos como `PII/Sensibles` (US-003), el Frontend DEBE aplicar cifrado simétrico (AES) usando una llave derivada de la sesión antes de escribir en LocalStorage
    And disparará peticiones silenciosas de *Merge Commit* al Backend (Snapshot Volátil) SOLO bajo un Debounce ininterrumpido de 10s de inactividad, usando una validación Zod "Parcial" (permitiendo nulos pero castigando tipos inválidos)
    And cuando el POST a `/complete` finalice exitosamente (HTTP 200 OK), el Frontend ejecutará una purga síncrona destruyendo inmediatamente la llave temporal de ese caso específico
    And un Cron silencioso global eliminará cualquier borrador huérfano en la PC del usuario que supere las 72 horas de antigüedad, previniendo cuellos de memoria.

  Scenario: Consistencia Eventual UX y Read-Your-Own-Writes (RYOW) (CA-7)
    Given que el POST a `/complete` finaliza exitosamente (HTTP 200 OK)
    Then además de purgar el LocalStorage, el Frontend eliminará proactivamente esa tarea específica del Store en RAM (Pinia) del Workdesk ANTES de redirigir al usuario al Home (RYOW)
    And esto garantizará que el usuario no vea su tarea "ya completada" flotando como un fantasma en su bandeja por culpa del micro-retraso asíncrono de la proyección de lectura del CQRS en la Base de Datos.

  Scenario: Idempotencia y Protección Anti-Doble Clic (El Dedo Tembloroso) (CA-8)
    Given el usuario pulsa [Enviar Formulario] múltiples veces por ansiedad o lag de red
    When el Payload JSON impacta el endpoint POST `/complete`
    Then el Frontend inyectará obligatoriamente un Header `Idempotency-Key` (UUID único por montaje de componente)
    And el API Gateway/Backend procesará únicamente la primera transacción
    And las peticiones subsecuentes idénticas retornarán un `HTTP 200 OK` silenciado desde la Caché, protegiendo a Camunda de excepciones `OptimisticLocking` o doble gasto en el Event Sourcing.

  # ==============================================================================
  # E. SEGURIDAD ZERO-TRUST, ISOMORFISMO Y PREVENCIÓN DE COLISIONES
  # ==============================================================================
  Scenario: Zod Isomórfico y Guillotina de Datos Fantasma (Choque Gnoseológico) (CA-9)
    Given la existencia de esquemas Zod bidireccionales en el ecosistema
    When un atacante bypassea la UI enviando un POST adulterado vía API REST (Ej: Editando un campo oculto o de 'Solo Lectura')
    Then los esquemas Zod de Frontend se transpilarán en CI/CD a estándar RFC JSONSchema, y el API Gateway/BFF en Java ejecutará la validación estrictamente mediante la librería genérica json-schema-validator, anulando el cuello de botella de emuladores JS.
    And cruzará los permisos de escritura del Rol del usuario contra los campos recibidos; si inyectó datos no autorizados, aplicará un `.strip()` silencioso descartando el campo adulterado, o abortará con `HTTP 403 Forbidden`
    And rechazará con `HTTP 400 Bad Request` cualquier asimetría de tipos de datos.

  Scenario: Seguridad Asimétrica y Prevención Replay en Micro-Tokens (CA-10)
    Given una validación asíncrona externa (Ej: Validar NIT) gatillada `OnBlur` en el Frontend
    When el Backend consulta la API externa exitosamente y retorna al Frontend un "Micro-Token JWT" firmado criptográficamente de corta duración (Ej: TTL 15 min)
    Then al momento del Submit final (`/complete`), el Frontend adjuntará este Micro-Token en el payload
    And el Backend (Zero-Trust) omitirá realizar una segunda llamada de red externa bloqueante, limitándose a verificar matemáticamente la validez de su propia firma en el Micro-Token para autorizar la transacción ACID en milisegundos
    And la arquitectura PROHÍBE el re-uso de tokens (Replay Attacks); el Token DEBE contener en sus Claims el `taskId` exacto y un `jti` que será invalidado en Redis un milisegundo después del Submit exitoso.

  Scenario: Integridad de Asignación Concurrente (Implicit Locking) (CA-11)
    Given que una tarea "TK-400" está explícitamente asignada al analista `maria.perez` en el motor
    When el analista `pedro.gomez` intercepta vulnerablemente la URL o el JWT Payload e intenta someter un POST a `/tasks/TK-400/complete`
    Then el Core iBPMS examina deductivamente el `{delegatedUserId}` transaccional y el `assignee` de Camunda contra la identidad central del Security Context (JWT)
    And aborta transaccionalmente la colisión inyectando un lapidario `HTTP 403 Forbidden` o `409 Conflict`, extirpando la necesidad pesada de emitir *ETags* a través del flujo asíncrono.

  # ==============================================================================
  # F. ARQUITECTURA CQRS, EVENT SOURCING Y PROTECCIÓN DEL MOTOR
  # ==============================================================================
  Scenario: Separación de Responsabilidades y Event Sourcing (CQRS) (CA-12)
    Given un JSON perfectamente validado resultante del "iForm Maestro"
    When el analista pulsa [Enviar Final] realizando POST a `/api/v1/workbox/tasks/{id}/complete`
    Then el Backend separará el flujo arquitectónico: inyectará el Comando (`Form_Submitted_Event`) en la tabla inmutable de Eventos garantizando el historial forense exacto
    And un Worker asíncrono proyectará (`Projection`) esos datos a la tabla relacional aplanada para habilitar lecturas hiperveloces desde los Dashboards y Analítica.

  Scenario: Exclusión Topológica Estratégica de Camunda Engine (CA-13)
    Given el cierre exitoso de la transacción CQRS (Guardado del Evento Inmutable validado en Postgres)
    When el Backend notifica a Camunda 7 para avanzar el Token BPMN (`taskService.complete()`)
    Then el Backend TIENE ESTRICTAMENTE PROHIBIDO empujar el Payload masivo de negocio (Textos largos, JSONs complejos) hacia la tabla `ACT_RU_VARIABLE` del Engine
    And a Camunda solo se le enviará un DTO minificado (Ej: `{ "aprobado": true, "form_storage_id": "ABC-123" }`) con las variables lógicas estrictamente requeridas por los Gateways de enrutamiento.

  Scenario: Consistencia Transaccional Cruda (ACID Fallback over Sagas) (CA-14)
    Given el Payload aplanado y guardado exitosamente en CQRS
    When el motor orquestador (Camunda 7) sufre un Crash o Timeout HTTP 5xx en su API REST interna al intentar avanzar la tarea
    Then el Backend iBPMS abortará inmediatamente la transacción base ejecutando un Rollback Compensatorio (Patrón Saga inverso) sobre la persistencia en PostgreSQL
    And devolverá un error HTTP 500 Crudo ("Motor No Disponible") a la UI en Pantalla 2
    And se prohíbe a nivel arquitectónico generar falsos positivos HTTP 202 ("Guardado para después") para eludir el colapso del proceso judicial de fondo, unificando la verdad visual con el estado real del Motor.

  # ==============================================================================
  # G. REASIGNACIONES Y COLISIONES GROUP-LEVEL (GAPs RESUELTOS)
  # ==============================================================================
  Scenario: Auto-Claim Implícito sobre Tareas No Asignadas (Group-Level) (CA-15)
    Given que una tarea "TK-500" está disponible en un grupo de trabajo (Ej: "Abogados") pero NO tiene un `assignee` directo asignado en Camunda
    When un usuario legitimado bajo la taxonomía RBAC interviene el iFormulario y presiona [Enviar] (`/complete`)
    Then el Backend (BFF) NO abortará la consulta por falla de exclusividad ("Implicit Locking" del CA-11)
    And en su lugar, ejecutará transaccionalmente un comando `taskService.claim()` asignando silenciosamente el caso al operario una fracción de milisegundo antes de empujar el Event_Sourced_Command (CQRS) final.
    And esto garantizará la fluidez de operación para Worklists comunitarias sin forzar un clic inútil en un botón "Reclamar".

  Scenario: Trazabilidad Activa de Rechazos Históricos en BFF (De-duplicación) (CA-16)
    Given una tarea devuelta a un especialista por un analista de control de calidad desde una fase superior (Rechazo Ope/BPMN)
    When el especialista abre el iFormulario para enmendar su trabajo documentado
    Then el Frontend (a través del llamado unificado `/form-context`) no solo recibirá el `prefillData` histórico
    And también recibirá inyectado OBLIGATORIAMENTE un array (Ej: `rejectionLogs`) con el dictamen exacto, responsable y fecha del rechazo
    And mostrando esta causal de devolución como un Alert inyectado en el Canvas central del formulario (Solo Lectura), previniendo que el usuario repita una reparación a ciegas guiado solo por la telepatía.
```

**Trazabilidad UX:** Wireframes Pantalla 2 (Vista de Tarea) y BFF Invisible.

---

### US-052: Motor de Orquestación Multi-Agente IA (Arquitectura y Gobernanza de Contextos)
Descripción: 
**Como** Administrador de la Plataforma iBPMS, 
**Quiero** configurar y operar un motor de inteligencia artificial compuesto por 4 Agentes Especializados (Orquestador, Backend, Frontend y QA) con inyección de contexto dinámica y reglas diferenciadas, 
**Para** evitar la saturación de tokens (Context Overload), prevenir alucinaciones mediante separación estricta de memorias y emular una fábrica de software autónoma segura dentro del iBPMS.

Contexto de Negocio & Arquitectura
Actualmente, los motores de IA monolíticos pierden el contexto o alucinan si se les sobrecarga con reglas. Esta historia establece la infraestructura para que el iBPMS administre reglas globales (CORE) que aplican a todos los agentes, y políticas modulares (Específicas) que solo se inyectan en tiempo de ejecución ("Just-in-Time"), replicando el modelo exitoso de Antigravity.

**Criterios de Aceptación (CA)**
```gherkin
CA-01: Definición del Rol "Arquitecto Orquestador"
Criterio: El sistema debe inicializar un Agente Maestro sin capacidad de escritura de código productivo. Given que un usuario solicita la creación de un nuevo proceso BPMN complejo a la IA, When la petición ingresa al motor de orquestación, Then el sistema invoca exclusiva y aisladamente al "Agente Orquestador", And este agente debe generar contratos de delegación (Handoffs JSON/Markdown) dirigidos a los Agentes Especialistas en lugar de intentar programar la solución.

CA-02: Separación Estricta de Memoria entre Especialistas (Backend, Frontend, QA)
Criterio: Los estados conversacionales de los 4 agentes jamás deben compartirse directamente para prevenir contaminación cruzada. Given que el Orquestador ha diseñado un Handoff para el "Especialista Backend", When el sistema despierta al Agente Backend en su propio hilo de ejecución (Thread), Then el Agente Backend DEBE tener un "System Prompt" en blanco respecto a las charlas del Orquestador, conociendo única y exclusivamente las instrucciones pasadas a través del paquete Handoff.

CA-03: Administración de Reglas CORE Universales (Equivalente a .cursorrules)
Criterio: Existencia de un repositorio de directrices globales obligatorias. Given que un administrador de plataforma ha configurado reglas críticas de seguridad (Ej. Inmunidad de Arranque / Zero-Trust Git) en el panel de Configuración AI Core, When el sistema arranca cualquier instancia de los 4 Agentes de IA, Then el motor iBPMS inyecta automáticamente esas reglas CORE en el inicio del System Prompt, consumiéndolas obligatoriamente en cada inferencia de red neuronal.

CA-04: Inyección Modular "Just-In-Time" (Equivalente a scaffolding/workflows/)
Criterio: Optimización de tokens mediante políticas específicas de rol bajo demanda. Given que el sistema almacena manuales extensos (Reglas UX/UI, Arquitectura Hexagonal Java, Guías funcionales QA), When el Orquestador delega una tarea de interfaz de usuario al "Agente Frontend", Then el motor iBPMS inyecta en la memoria RAM del Agente Frontend únicamente la Política Modular de "Reglas UX/UI", omitiendo el peso de los manuales de Java o QA para maximizar la capacidad de razonamiento del LLM sin sobrepasar su Ventana de Contexto (Context Limit).

CA-05: El Humano como Bus de Datos (Enrutador de Aprobaciones)
Criterio: La aplicación del Gobierno Técnico estricto donde el humano no es aprobador autónomo. Given que un Agente Especialista (ej. Backend) termina su plan de implementación y requiere validación, When la IA emite un mensaje de estado PENDING_APPROVAL, Then la UI del iBPMS no le pide al humano que lo valide técnicamente, sino que le notifica: "El Agente Backend requiere revisión técnica. Lleva este plan al Agente Orquestador", And el motor iBPMS transfiere el payload al Orquestador, quien lo audita, evalúa los "GAPs", y emite el veredicto definitivo de regreso a la cola de ejecución.

Notas de Implementación (Non-Functional Requirements)
Aislamiento Tecnológico: Las llamadas a la API de LLM (OpenAI / Gemini) deben hacerse en sesiones HTTP aisladas.
Bandeja de Entrada Común: Simular la carpeta .agentic-sync/ creando una tabla en Base de Datos ai_handoff_queue donde los agentes depositarán sus contratos en estado DRAFT, APPROVED y STASHED.
```


### US-053: Antigravity Command Center (Fábrica de Agentes IA y Arbitraje FinOps B2B)
**Como** Administrador del Tenant (Cliente B2B)
**Quiero** un panel de control para crear "Agentes de IA" y gestionar mi consumo mediante un Modelo Híbrido (Cuota de Suscripción Base vs. Billetera de Reserva Prepaga)
**Para** orquestar fuerza laboral artificial en mis procesos BPMN sin riesgo de facturas sorpresa, garantizando que mis flujos críticos no colapsen por falta de fondos y auditando el costo exacto de cada Agente.

**Criterios de Aceptación (CA)**
```gherkin
Feature: AI Agent Factory, B2B Token Arbitrage & BPMN FinOps Resilience

  # ==============================================================================
  # A. LÓGICA DE CONSUMO HÍBRIDO (ARBITRAJE DE TOKENS) Y DASHBOARD VISUAL
  # ==============================================================================
  Scenario: Bifurcación Visual de Suscripción vs. Billetera (The Antigravity UI) (CA-01)
    Given la interfaz del "Antigravity Command Center" (Panel de Gobernanza IA)
    Then el Frontend renderizará dos secciones financieramente independientes:
    And 1. "MODEL QUOTA" (La Suscripción): Barras de progreso horizontales separadas por Tier de Inteligencia (Ej: `Gemini 1.5 Flash` vs `Gemini 1.5 Pro`). Se miden en Tokens virtualizados y muestran su fecha/hora de reseteo automático mensual.
    And 2. "MODEL CREDITS" (La Reserva Prepaga): Un contador numérico general tipo cuenta bancaria con el Saldo Vitalicio comprado por el cliente.

  Scenario: Transición Controlada y Bloqueo de Factura Sorpresa (Opt-In Overages) (CA-02)
    Given que un Agente IA agota el 100% de la "Model Quota" de su Tier asignado
    When el Agente intenta ejecutar una nueva inferencia para un proceso
    Then el sistema verificará el interruptor maestro `[Enable AI Credit Overages]` en la UI.
    And si está APAGADO, la transacción se aborta inmediatamente (Hard-Stop) para proteger el presupuesto del cliente.
    And si está ENCENDIDO, el sistema ejecuta un Auto-Deduct silencioso, restando los tokens de la "Billetera Prepaga" (Model Credits), aplicando un multiplicador de costo si el Agente usa un modelo Premium.

  Scenario: Alertas Proactivas de Umbral (Thresholds) (CA-03)
    Given el consumo en tiempo real de una "Model Quota"
    When la barra de consumo alcance matemáticamente el 80% y luego el 95%
    Then un proceso asíncrono despachará alertas automatizadas (Campana UI y Email) al Administrador del Tenant.
    And advirtiendo el inminente bloqueo operativo o la transición inminente hacia la facturación prepaga.

  # ==============================================================================
  # B. FÁBRICA DE AGENTES Y PRESUPUESTOS POR ROL
  # ==============================================================================
  Scenario: Creación de Agentes y Control de Gasto Granular (CA-04)
    Given la pestaña "Fábrica de Agentes"
    When el Administrador pulsa `[+ Crear Nuevo Agente]`
    Then el sistema exigirá definir: Nombre, Motor LLM Agnóstico (Ej: Gemini Ultra), y el `System Prompt` (Rol y Reglas del agente).
    And el panel incluirá un candado financiero individual: `[x] Autorizar a este Agente a consumir de la Billetera Prepaga`.
    And si este candado está desmarcado, el Agente NUNCA podrá gastar dinero extra, fallando silenciosamente al agotarse la cuota gratuita mensual, incluso si el interruptor maestro del Tenant está encendido.

  # ==============================================================================
  # C. RESILIENCIA DEL MOTOR BPMN ANTE FALTA DE FONDOS
  # ==============================================================================
  Scenario: Suspensión Elegante de Service Tasks (Camunda Incident) (CA-05)
    Given un Proceso BPMN automatizado que invoca a un Agente IA en segundo plano
    When el Backend detecta que la Suscripción está agotada Y la Billetera Prepaga no tiene fondos (o el Overage está apagado)
    Then la arquitectura TIENE ESTRICTAMENTE PROHIBIDO lanzar una excepción fatal HTTP 500 que destruya la instancia del proceso de negocio.
    And el Worker interceptará el fallo financiero y levantará un "Incidente de Camunda" (Estado: `ESPERANDO_SALDO_IA`).
    And la tarea quedará congelada de manera indefinida hasta que el cliente recargue fondos y el Administrador presione `[Reintentar]` en la cabina de control, retomando el flujo ileso.

  # ==============================================================================
  # D. ADMINISTRACIÓN DE CARTERA, CADUCIDAD Y TRAZABILIDAD
  # ==============================================================================
  Scenario: Reglas de Caducidad Asimétrica (Rollover y Reset) (CA-06)
    Given la llegada del día 1 de cada mes a las 00:00 UTC
    Then un Cron Job reseteará las "Model Quotas" (Suscripción base) a su valor nominal inicial (Use-it-or-lose-it).
    And el saldo de los "Model Credits" (Billetera Prepaga) TIENE PROHIBIDO ser reseteado o caducar, acumulándose vitaliciamente mes a mes.

  Scenario: Trazabilidad FinOps Exacta en la Factura (Billing Source) (CA-07)
    Given una invocación exitosa a cualquier API de IA (Google/Anthropic)
    Then el Backend registrará el costo real consumido leyendo el Payload de respuesta (Prompt Tokens + Completion Tokens).
    And inyectará OBLIGATORIAMENTE en la bitácora inmutable una columna `billing_source` cuyo valor será `SUBSCRIPTION_QUOTA` o `OVERAGE_WALLET`.
    And registrará el `Agent_ID` asociado, permitiendo exportar reportes gerenciales para auditar qué procesos salieron gratis y cuáles costaron saldo de reserva.

  Scenario: Inyección Manual de Saldo Offline (MVP V1) (CA-08)
    Given un cliente que adquiere un paquete de "Tokens de Reserva" pagando una factura externa (Offline)
    Then el sistema proveerá un endpoint administrativo protegido (Exclusivo para el Súper Admin del iBPMS).
    And permitirá inyectar recargas manuales (Top-Ups) sumando créditos a la billetera vitalicia del Tenant.
    And la integración nativa de pasarelas de pago automáticas (Stripe/PayPal) queda diferida para V2.

Scenario: Downgrade Automático por Falta de Fondos Premium (Fallback Cognitivo)
    Given un Agente IA configurado para usar un modelo Premium (Ej: Gemini Ultra) y el interruptor Overage apagado
    When el Agente intenta inferir y el Billing Engine rechaza la transacción por fondos insuficientes en su Tier
    Then el Backend TIENE PROHIBIDO suspender la tarea BPMN de manera inmediata levantando el incidente.
    And el motor intentará un "Downgrade Fallback" automático hacia el modelo Estándar (Ej: Gemini Flash) SI Y SOLO SI este Tier aún posee cuota mensual gratuita.
    And si el modelo Estándar logra resolverlo, el proceso avanza estampando en la auditoría: `[PROCESADO_POR_FALLBACK]`.
    And solo si el modelo Estándar también agota sus tokens (Bolsa en 0), el Worker levantará el incidente en Camunda (`ESPERANDO_SALDO_IA`), priorizando siempre la continuidad operativa.

```
---

