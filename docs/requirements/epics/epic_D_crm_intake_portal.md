# [TÍTULO DE LA ÉPICA]
> **Fuente:** Modularizado desde `v1_user_stories.md` | **Fecha:** 2026-04-10
> **Coverage:** `.agentic-sync/coverage_matrix.md` (centralizada)

---

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

> [!IMPORTANT]
> **Dependencias y Bloqueos Sistémicos (Análisis PO 2026-04-08):**
> - **US-036 (RBAC & Seguridad Perimetral) [DEPENDENCIA ESTRUCTURAL]:** El Frontend de la US-025 es enteramente "ciego y obediente"; depende a nivel atómico del token JWT (RBAC) expuesto por el backend de la US-036. Sin esa matriz de roles empaquetada, las directivas de ocultamiento (CA-1 al CA-4) carecen de insumo de verdad.
> - **US-001 (Workdesk) y US-002 (Reclamo de Tareas) [CONSUMIDORES]:** Son las pantallas operativas que implementarán directamente los mandatos UX dictaminados aquí (Soft-Undo de 5s, Skeleton Loaders y Websocket Push Alerts).
> - **US-009 (Dashboard de Salud) [CONSUMIDOR LAZY-LOAD]:** Consumidor exclusivo de la directiva de Lazy Loading cruzado (IntersectionObserver) para gráficas pesadas (CA-28).
> - **BLOQUEANTES DE DESARROLLO: NINGUNA.** El equipo FrontEnd cuenta con vía libre de desarrollo. Los arquitectos Vue pueden avanzar simulando un Store y Falseando Roles en Memoria (Pinia Mock) sin esperar un solo endpoint del Backend.
> - **Clasificación MoSCoW Oficial:** **MUST** (App Shell crítico base para todo el ecosistema de cliente SPA).


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

  # ==============================================================================
  # E. REMEDIACIONES TÁCTICAS POST-ANÁLISIS DE ARQUITECTURA (2026-04-08)
  # Origen: GAPs detectados en docs/requirements/us025_functional_analysis.md
  # Propósito: Blindar la auditoría en "Ver Sistema Como", asegurar peticiones DELETE
  #            en cierres súbitos, y eliminar asimetrías Server-Side Pagination vs Sockets.
  # ==============================================================================

  Scenario: [REMEDIACIÓN GAP-1] Trazabilidad Iso-Audit en Modo Impersonator (CA-31)
    # Resuelve: Evitar que un Admin cometa un fraude indetectable operando bajo la UI mutada de un usuario base (CA-9).
    Given el administrador operando el sistema en Modo Soporte ("Impersonate" activo bajo el token de Juan)
    When el administrador ejecuta una operación de escritura o negocio simulando ser Juan (Ej: Aprobar Tarea)
    Then el API Request Gateway garantizará que el JWT transmitido contenga un identificador híbrido obligando al motor de auditoría a registrar: `User: Juan | ImpersonatedBy: Admin_ID`.
    And será materialmente imposible, a nivel Backend o Frontend BFF, que el sistema asuma ciegamente que "Juan" aprobó el trámite, protegiendo a la empresa ante forenses ISO 27001.

  Scenario: [REMEDIACIÓN GAP-2] Beacon de Ejecución para Cierres Abruptos en Soft-Undo (CA-32)
    # Resuelve: Qué pasa si un analista oprime [Archivar], pero cierra Chrome en 2s sin esperar la caducidad del Soft-Undo de 5s.
    Given la ventana de gracia del CA-14 (el POST/DELETE se retrasa 5 segundos en VUE)
    When el usuario instaura la orden destructiva en la UI, pero repentinamente cierra la pestaña completa de su navegador Chrome/Edge
    Then el framework del FrontEnd reaccionará al evento `beforeunload` del ciclo de vida del DOM
    And forzará el envío asíncrono e instantáneo de la mutación estancada en el stack utilizando OBLIGATORIAMENTE la API nativa de navegador `navigator.sendBeacon()` hacia la ruta del Backend.
    And garantizando transaccionalmente que ninguna tarea "muerta" en la UI del Front siga viva en el Microservicio por culpa del cierre súbito.

  Scenario: [REMEDIACIÓN GAP-3] Derogación de Listas Colosales en Favor de Paginación Server-Side (CA-33)
    # Resuelve: El mandato "Virtual Scrolling de 5000 arrays" (CA-22) colisiona contra las barreras SRE de la US-001 (Paginación).
    Given la necesidad de cargar grillas en Pantallas densas (Ej: Histórico de 5000 Casos en Pantalla 16)
    When el Arquitecto UI diseñe el mecanismo de entrega visual
    Then SE RECHAZA OFICIALMENTE forzar la petición y descarga al cliente de arreglos masivos (5000 rows JSON), dejando sin efecto primario el CA-22 en vistas maestras de negocio.
    And primará ABSOLUTAMENTE el consumo de Paginación Sever-Side (`?page=1&size=20`), donde el "Virtual Scrolling" en Front operará iterativamente exigiendo la siguiente página (Infinite Scroll transparente via API), blindando tanto la Memoria RAM del navegador en CA-22 como CPU/DBA de Servidor.

  Scenario: [REMEDIACIÓN GAP-4] Resincronización Silenciosa Híbrida de WebSockets (CA-34)
    # Resuelve: Las inyecciones Sockets de Tareas (CA-25) desajustan matemáticamente las grillas "Server-Paginadas" del CA-33.
    Given la vista activa de una Tabla de Workdesk paginada exactamente a 20 filas operando en pantalla
    When una Alerta Silenciosa de Websockets (CA-25) reciba la inyección de una (1) tarea nueva en vivo proveniente de Camunda
    Then el cliente SPA (Vue) incorporará visualmente el registro mágico brillándolo momentáneamente
    And en background disparará una orden sorda de "Silent Page Invalidated" contra Pinia (Store) que obligará a re-solicitar silenciosamente bajo cuerdas al Backend el total Count (`totalElements`) para reajustar los números absolutos y relativos de todos los paginadores de la tabla (`Mostrando 21 de 1.831`), evitando descuadres lógicos del Framework UI.
	
	
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
