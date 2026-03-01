# Wireframes Conceptuales (Baja Fidelidad) - iBPMS V1

Este documento define la estructura visual y de experiencia de usuario (UX) para el MVP Táctico Extendido (V1). Cada "Pantalla" representa una vista de la aplicación SPA (Single Page Application).

---

## Pantalla 0: Menú Global y Portal de Servicios (Service Catalog / App Launcher)
**Objetivo:** Basado en patrones UX empresariales modernos (ej. Workday, ServiceNow), en lugar de un árbol de carpetas infinito, la plataforma presenta un "Portal de Apps". Cada proceso o dominio (RRHH, Compras) aparece como un módulo o "Tile". Es aquí donde el usuario o cliente **Inicia un Nuevo Proceso**.

```text
+-----------------------------------------------------------------------------------+
|  [LOGO iBPMS]  |  🔍 Buscar procesos, políticas...    |  🔔 (3) | 👤 Mi Perfil  |
+-----------------------------------------------------------------------------------+
|  [🏠] Inicio      |   ⭐ Mis Procesos Frecuentes: [🏖️ Vacaciones] [🛒 Req. Compra]|
|  [�] Workdesk(12)|   ----------------------------------------------------------  |
|  [📥] Inbox (5)   |   👋 Buenos días, @Harolt. ¿Qué necesitas iniciar hoy?        |
|  [📂] Proyectos   |   ----------------------------------------------------------  |
|  [📊] Dashboards  |   [ RECURSOS HUMANOS ]                                        |
|                   |   +-------------------+  +-------------------+                |
|  --- ADMIN ---    |   | 🏖️ Vacaciones    |  | 🤝 Onboarding     |                |
|  [🔌] Integración |   |                   |  |                   |                |
|  [🛡️] Seguridad   |   | [ 🚀 INICIAR ]    |  | [ 🚀 INICIAR ]    |                |
|  [📦] Extensión   |   +-------------------+  +-------------------+                |
|                   |                                                               |
|                   |   [ COMPRAS Y LEGAL ]                                         |
|                   |   +-------------------+  +-------------------+                |
|                   |   | 🛒 Req. de Compra |  | ⚖️ Revisión NDA    |                |
|                   |   | Compras < $5000   |  | Aprobación legal  |                |
|                   |   |                   |  | confidencialidad. |                |
|                   |   | [ 🚀 INICIAR ]    |  | [ 🚀 INICIAR ]    |                |
|                   |   +-------------------+  +-------------------+                |
|                   |                                                               |
|                   |   *(Estos "Tiles" se generan dinámicamente si el rol del      |
|                   |     usuario tiene permisos para verlos)*                      |
+-----------------------------------------------------------------------------------+
```

---

## Pantalla 1: Workdesk (Escritorio de Tareas)
**Objetivo:** El escritorio consolidado. A diferencia de la Pantalla 0 (donde se *inician* cosas), aquí es donde el usuario *recibe* el trabajo que otras personas o sistemas le asignaron, o donde *retoma* procesos que dejó guardados como Borradores.

```text
+-----------------------------------------------------------------------------------+
|  [LOGO iBPMS]  |  🔍 Buscar tarea, caso, ID...        |  🔔 (3) | 👤 Mi Perfil  |
+-----------------------------------------------------------------------------------+
|  [🏠] Inicio      |  Filtros: [Bandeja] [Urgentes] [Mis Procesos] [Borradores]    |
|  ---------------  |---------------------------------------------------------------|
|  [📋] Workdesk(12)|  TARJETAS DE TAREAS (Lista Vertical)  [✅ Aprobar Seleccionados]|
|  [📥] Inbox (5)   |                                                               |
|  [📂] Proyectos   |  +---------------------------------------------------------+  |
|  [📊] Mis Procesos|  | [✏️] [BORRADOR]    [ GUARDADO: Hace 1 hr ]              |  |
|  Dashboards (BI)  |  |     Proceso: Req. de Compra < $5000                     |  |
|  Reglas IA (DMN)  |  |     [ 🗑️ Descartar Borrador ]  [ ▶️ Retomar Proceso ] -------> |  | << Clic abre Pantalla 2
|  Admin / Setup    |  +---------------------------------------------------------+  |
|                   |                                                               |
|                   |  +---------------------------------------------------------+  |
|                   |  | [x] [🟢 SD EXTERNO] [SLA: 2 Hrs] [URGENTE]              |  |
|                   |  |     Tarea: Aprobar Contrato Legal 001                   |  |
|                   |  |     Servicio: Onboarding Cliente -> 🏢 Banco Alpha      |  |
|                   |  |     [ Ver Adjuntos (2) ]  [ Asignarme Tarea / Claim ]   |  |
|                   |  +---------------------------------------------------------+  |
|                   |                                                               |
|                   |  +---------------------------------------------------------+  |
|                   |  | [ ] [🏢 INTERNO] [SLA: 1 Día] [NORMAL]                  |  |
|                   |  |     Tarea: Revisión Financiera Vuelo Comercial          |  |
|                   |  |     Proceso: Flujo RRHH -> 👤 Solicitante: Carlos Dev   |  |
|                   |  |     [ Ver Adjuntos (0) ]  [ ↩️ Liberar (Unclaim) ] -----> |  | << Clic abre Formulario Dinámico (Pantalla 2)
|                   |  +---------------------------------------------------------+  |
|                   |                                                               |
|                   |  +---------------------------------------------------------+  |
|                   |  | [ ] [⚡ ÁGIL] [SLA: 30 Mins] [CRÍTICA]                  |  |
|                   |  |     Ticket: Reiniciar Servidor X (No BPMN)              |  |
|                   |  |     Tablero: Incidencias Nivel 2                        |  |
|                   |  |     [ Ver Comentarios ]   [ Mover a 'Done' ]            |  |
|                   |  +---------------------------------------------------------+  |
+-----------------------------------------------------------------------------------+
```

---

## Pantalla 2: Vista de Detalle / Formulario Dinámico (JSON Data Entry)
**Objetivo:** La pantalla donde el usuario ejecuta el trabajo. Captura la data estructurada (JSON payload) y muestra historial. Integra "Minitareas Kanban".

---

## Pantalla 1B: Inbox Corporativo & Copiloto IA (Split View)
**Objetivo:** Vista evolucionada del Buzón de Correo (Master-Detail) reservada estrictamente para el rol **Líder de SAC (Servicio al Cliente)** u homólogos de Intake. La pantalla se divide verticalmente (Panel Izquierdo para la lista de correos y filtros, Panel Derecho para leer el correo activo y gestionar el Copiloto IA). Esta es la entrada principal del *Intelligent Intake* (Plan A).

```text
+-------------------------------------------------------------------------------------------------------+
|  [LOGO iBPMS]  |  🔍 Buscar en todos los campos, adjuntos o remitentes...          |  🔔 | 👤 Perfil|
|-------------------------------------------------------------------------------------------------------|
|  [📥] Inbox     |                    MASTER PANE (Lista de Correos) |      DETAIL PANE (Lector & IA)   |
|  [🧠] Config IA | [ Filtro Cliente v ] [Proyecto v] [ 📅 Rango ] | [< Volver] Subject: Project Update |
|  [📄] Plantillas| Actividades: [x] Tarea [ ] Proy [ ] Intake SGDEA | -------------------------------- |
|  ---------------|--------------------------------------------------| (Remitente: John Doe)            |
|                 |  Remitente | Asunto          | Fecha | Cliente | Hi team,                         |
|                 |  ----------|-----------------|-------|---------| Following up on our discussion,  |
|                 | >John Doe  | Project Update  | 5:45p | ClientC | I've attached the Q3 report.     |
|                 |  Jane S.   | Re: Milestone 1 | 4:30p | Innovat | Please review by EOD Friday.    |
|                 |  alert@sys | Deadline Passed | 6:15p | GlobalT |                                  |
|                 |                                                  | ================================ |
|                 |                                                  | [✨] COPILOTO IA (Sugerencias)   |
|                 |                                                  | ================================ |
|                 |                                                  | [ Extracción: Informe Finan. ]   |
|                 |                                                  | [ Cliente: Client Corp. ]        |
|                 |                                                  | [ Fecha Clave: Viernes_Q3 ]      |
|                 |                                                  | [✔️ Aprobar Extracción IA]       |
|                 |                                                  | -------------------------------- |
|                 |                                                  | [ RESPUESTA SUGERIDA (Borrador) ]|
|                 |                                                  | "Hello John, thank you..."       |
|                 |                                                  | [ ❌ Rechazar ] [ 🚀 Responder ] |
|                 |                                                  | -------------------------------- |
|                 |                                                  | [ ➕ CREAR SERVICE DELIVERY ]    |
|                 |                                                  | "Pasa este correo al Embudo."    |
+-------------------------------------------------------------------------------------------------------+
```

---

---

## Pantalla 2: Vista de Detalle / Formulario Dinámico (JSON Data Entry)
+-----------------------------------------------------------------------------------+
|  ⬅ Volver al Inbox | Tarea: Revisión Financiera | Estado: [ EN PROGRESO ]         |
+-----------------------------------------------------------------------------------+
|  PANEL IZQUIERDO: CONTEXTO & CHECKLIST    |  PANEL DERECHO: FORMULARIO DINÁMICO   |
|                                           |                                       |
|  [⏱️ TU DESEMPEÑO EN ESTA TAREA]          |  Monto Aprobado:                      |
|  SLA Asignado: 24 Horas                   |  [ $ 1,500.00                       ] |
|  Tiempo Ejecutando: 2h 15m (🟢 Excelente) |                                       |
|  ---------------------------------------  |  Comentarios de rechazo (opcional):   |
|                                           |  [                                  ] |
|  📄 Documentos Generados / Adjuntos:      |  [                                  ] |
|  [ PDF Factura.pdf ] [ Correo_O365.eml  ] |                                       |
|                                           |  Metadatos adicionales (Cliente):     |
|  📋 Minitareas (Kanban de Actividad):     |  Centro de Costo: [ IT-900 ]          |
|  [x] Validar firma de cliente             |                                       |
|  [ ] Cruzar contra presupuesto ERP        |  +---------------------------------+  |
|  [ ] Adjuntar pantallazo de validación    |  | [💾 Guardar Borrador]           |  |
|                                           |  | [⋮ Opciones] [RECHAZAR] [APROBAR] |  |
|                                           |  +---------------------------------+  |
|                                           |  *(Opciones: [🔄 Reasignar] [💬 Mención])*|
|  ⏳ Historial (Audit Log):                |                                       |
|  - 10:00am: Creada por Correo O365        |                                       |
|  - 10:05am: Bot AI validó formato         |                                       |
+-----------------------------------------------------------------------------------+
```

---

## Pantalla 3: Tableros Kanban (Gestión de Proyectos Operativos)
**Objetivo:** Vista ágil para procesos no secuenciales o para administrar casos macro sin usar BPMN estricto.

```text
+-----------------------------------------------------------------------------------+
|  Proyecto: Implementación Cliente Alpha | [+ Nueva Tarjeta Kanban]                |
+-----------------------------------------------------------------------------------+
|   TO DO (2)            |   IN PROGRESS (1)        |   DONE (5)                    |
|------------------------|--------------------------|-------------------------------|
| +--------------------+ | +----------------------+ | +---------------------------+ |
| | Tarea: Firma SLA   | | | Tarea: Auth Config   | | | (Tarjeta Completada)      | |
| | Asignado: @Maria   | | | Asignado: @Carlos    | | +---------------------------+ |
| +--------------------+ | | [1/3 Subtareas]      | |                               |
|                        | +----------------------+ |                               |
| +--------------------+ |                          |                               |
| | Tarea: Kickoff     | |                          |                               |
| | Asignado: @Juan    | |                          |                               |
| +--------------------+ |                          |                               |
+-----------------------------------------------------------------------------------+
```

---

## Pantalla 4: Taller de Reglas IA (LLM a DMN Translator)
**Objetivo:** La integración fundacional de IA donde un usuario de negocio escribe texto plano (Claude Opus API) y la plataforma autogenera el estándar DMN (Validación de V1).

```text
+-----------------------------------------------------------------------------------+
|  Configuración > Taller de Reglas de Negocio (Asistido por Claude IA)             |
+-----------------------------------------------------------------------------------+
|                                                                                   |
|  🤖 Asistente de Traducción DMN (Lenguaje Natural a Lógica):                      |
|  +-----------------------------------------------------------------------------+  |
|  | "Si el monto del vuelo es mayor a 1000 dólares y el solicitante es Analista,|  |
|  |  entonces requiere aprobación_jefe=true, sino aprobación_jefe=false."       |  |
|  +-----------------------------------------------------------------------------+  |
|                                   [ GENERAR TABLA DE DECISIÓN (DMN) ] -----(API)  |
|                                                                                   |
|  📊 Estructura DMN Generada (Validación Visual):                                  |
|  | Monto (Input) | Rol (Input) | -> | Aprobación_Jefe (Output) |                  |
|  |---------------|-------------|----|--------------------------|                  |
|  | > 1000        | "Analista"  | -> | TRUE                     | [✏️ Editar]      |
|  | <= 1000       | "Analista"  | -> | FALSE                    | [✏️ Editar]      |
|  | -             | "Gerente"   | -> | FALSE                    | [✏️ Editar]      |
|                                                                                   |
|  [ 🧪 Simulador: Monto=6000, Rol=Analista -> Resultado: TRUE ]  [ Probar Regla ]  |
|  -------------------------------------------------------------------------------  |
|  [ DESCARTAR REGLA ]                           [ ✔️ GUARDAR Y DESPLEGAR AL MOTOR ]|
+-----------------------------------------------------------------------------------+
```

---

## Pantalla 5: Dashboards BAM (Líder / Analítica Estratégica)
**Objetivo:** Visibilidad estratégica para líderes, inyectando gráficas de alto rendimiento que consumen los logs SQL de Camunda/Core sin reconstruir código frontend complejo. Permite observar eficiencia individual, grupal y cuellos de botella generales.

```text
+-----------------------------------------------------------------------------------+
|  Dashboards > Desempeño y Procesos (BAM)                 [ 📅 Este Mes v ]        |
+-----------------------------------------------------------------------------------+
|  [ 📊 SALUD GENERAL (Procesos & Proyectos) ]                                      |
|  📈 Volumen de Casos Semanales                 | ⏱️ Tiempos de Resolución (TTV) |
|  [ Gráfico de Barras integrado vía iFrame  ]   | [ Gráfico de Línea de Tendencia] |
|  [ o Web Component apuntando a base SQL    ]   | [ de tiempos promedio por cola ] |
|  -------------------------------------------------------------------------------  |
|  [ 👥 DESEMPEÑO DEL EQUIPO (Team Efficiency view by User & Role) ]                |
|  Ranking de Productividad (Top / Bottom Ejecutores):                              |
|  1. 👤 @Maria (Analista Legal) -> 45 Tareas cerradas a tiempo (98% Eficiencia)    |
|  2. 👤 @Carlos (Desarrollador) -> 12 Tareas cerradas, (SLA Promedio Roto en 5%)   |
|  -------------------------------------------------------------------------------  |
|  [ 🚦 CUELLOS DE BOTELLA ACTIVOS (Top 3) ]                                        |
|  1. "Aprobación Legal" -> 45 Tareas Atascadas (Promedio: 3 Días. SLA Roto)        |
|  2. "Revisión Contable" -> 12 Tareas Atascadas                                    |
|                                                                                   |
|                                             [ MOSTRAR REPORTE COMPLETO EN BI ]    |
+-----------------------------------------------------------------------------------+
```

---

## Pantalla 6: Diseñador Avanzado BPMN 2.0 (Model & Analyze)
**Objetivo:** Interfaz administrativa para la gestión del ciclo de vida de los procesos (Crear, Editar, Desplegar) e importar diagramas BPMN complejos. Incluye un *Analizador Semántico* que verifica si el diagrama importado es "Ejecutable" (Executable=true) dentro del motor, validando variables, integraciones y reglas antes del despliegue.

```text
+-----------------------------------------------------------------------------------+
|  Admin > Diseño de Procesos > "Solicitud de Crédito v2"         [ ⚙️ Propiedades ]|
+-----------------------------------------------------------------------------------+
|                                                                                   |
|  [⬆️ Importar .bpmn] | [⬇️ Exportar] | [🧠 Consultar Copiloto IA] | [🚀 DESPLEGAR]|
|  -------------------------------------------------------------------------------  |
|                                                                                   |
|   PALETA BPMN 2.0|  LIENZO DE DIBUJO (bpmn-js Integrado)                          |
|   ---------------|  +----------------------------------------------------------+  |
|   (o) Eventos    |  | Pool: Solicitud de Crédito                               |  |
|       - Start/End|  +----------------------------------------------------------+  |
|       - Timer    |  | Radicador  | (Inicio) -> [ Llenar Form ] -> < >          |  |
|       - Message  |  |            |                                 |           |  |
|       - Error    |  +----------------------------------------------|-----------+  |
|   [ ] Tareas     |  | Analista   |              [ Servicio CRM ] <-+           |  |
|       - User Task|  |            |                    |                        |  |
|       - Serv.Task|  |            |             [[ Sub-Proceso Riesgo ]]        |  |
|       - Call Act.|  |            |                    |                        |  |
|   < > Compuertas |  |            |                 ( ) Timer Event (24h)       |  |
|       - Exclusiva|  +----------------------------------------------------------+  |
|       - Paralela |                                                                |
|   [📄] Datos     |  ------------------------------------------------------------  |
|                  |  [ 🔎 PRE-FLIGHT ANALYZER & 🧠 AI AUDITOR FEEDBACK        ]    |
|                  |  [ ❌ Semántica ] User Task "Llenar Form" no tiene Form Key    |
|                  |  [ 🤖 Auditor IA] ISO 9001: Sugiero agregar un "User Task"     |
|                  |                   de aprobación manual después del Servicio    |
|                  |                   CRM para asegurar control de calidad (QMS).  |
+-----------------------------------------------------------------------------------+
|  PANEL DE PROPIEDADES (Pestañas de Configuración)                                 |
|                                                                                   |
|  [ Pestaña: Propiedades del Carril ("Aprobador") ]                                |
|  - 👥 Rol de Sistema Asociado (RBAC): [ VPE_Finanzas v ]  <- (Cruza con Pant. 14) |
|                                                                                   |
|  [ Pestaña: Propiedades de la Tarea ("[ Validar ]") ]                             |
|  - 📄 Formulario Asociado: [ Form_Aprobacion_Credito_V1.json v ]                  |
|  - ⏱️ SLA (Tiempo de Resolución): [ 24 Horas ]                                    |
|  - 🤖 Regla DMN (Pre-Condición): [ Ninguna ]                                      |
+-----------------------------------------------------------------------------------+
```

---

## Pantalla 7: IDE Web Pro-Code para Formularios (iForm Builder)
**Objetivo:** Interfaz administrativa dual (Low-Code/Pro-Code) integrada con Mónaco Editor (VS Code engine). El usuario diseña arrastrando componentes, pero por debajo el IDE genera archivos `.vue` puros, tipado estricto y esquemas de validación Zod. A diferencia de otros iBPMS, aquí no hay cajas negras.

```text
+-----------------------------------------------------------------------------------+
|  Admin > Formularios > Nuevo Formulario                     [💾 Guardar Versión]  |
+-----------------------------------------------------------------------------------+
|  [ 🏛️ ARQUITECTURA DEL FORMULARIO ]                                               |
|  ¿Qué tipo de Formulario deseas crear?                                            |
|  ( ) Formulario de Tarea Simple (Aplica solo a un paso del flujo)                 |
|  (x) iForm Maestro (Expediente) (Un solo componente reactivo que muestra u oculta |
|      secciones dependiendo del "Current_Stage" y el "User_Role" de Camunda).      |
|                                                                                   |
|  [ Vista: 🎨 DISEÑADOR VISUAL ]  |  [ Vista: 💻 CÓDIGO VUE 3 / ZOD (Mónaco IDE) ] |
|  -------------------------------------------------------------------------------  |
|                                                                                   |
|  COMPONENTES |  LIENZO DEL FORMULARIO (Secciones basadas en Etapas)               |
|              |                                                                    |
|  [H1] Título |  +--------------------------------------------------------------+  |
|  [T] Texto   |  | v-if="stage === 'comercial' || stage === 'riesgos'"          |  |
|  [num] Número|  | [ Section: Datos del Cliente (Solo Lectura para Riesgos) ]   |  |
|  [v] Select  |  | [T] Nombre Empresa: [__________________________]             |  |
|  [📎] Archivo|  | [num] Monto Solicitado: [______________________]             |  |
|              |  +--------------------------------------------------------------+  |
|              |                                                                    |
|  [⬇️ Swagger]|  +--------------------------------------------------------------+  |
|  (Auto bind) |  | v-if="stage === 'riesgos'"                                   |  |
|              |  | [ Section: Análisis Financiero (Editable) ]                  |  |
|              |  | [v] Nivel de Riesgo (Zod: Requerido)                         |  |
|              |  +--------------------------------------------------------------+  |
|                                                                                   |
|  -------------------------------------------------------------------------------  |
|  [ ⚡ GENERADOR DE TESTS ] [🧪 Crear spec.ts (Jest)] [🤖 Crear Prueba e2e (Cypress)]|
+-----------------------------------------------------------------------------------+
```

---

## Pantalla 9: Gestor y Constructor de Proyectos (Agile & Tradicional)
**Objetivo:** Interfaz administrativa para instanciar proyectos macro. Permite elegir la metodología de ejecución, asignar líderes y agregar la Metadata base.

```text
+-----------------------------------------------------------------------------------+
|  Admin > Gestión de Proyectos                               [+ Nuevo Proyecto]    |
+-----------------------------------------------------------------------------------+
|  Nombre del Proyecto: [ Implementación Core Bancario Cliente Alpha ]              |
|  Descripción:         [ Renovar infraestructura transaccional...   ]              |
|  Líder Asignado:      [ @Harolt (Scrum Master / PM)                ]              |
|  -------------------------------------------------------------------------------  |
|                                                                                   |
|  [⚙️ METODOLOGÍA DEL PROYECTO]                                                    |
|  Seleccione cómo se gestionará el trabajo operativo de este proyecto:             |
|                                                                                   |
|  (x) Ágil / Kanban Libre                                                          |
|      - Crea un tablero sin restricciones de flujo. Ideal para trabajo             |
|        no estructurado, seguimiento de incidentes o sprints.                      |
|                                                                                   |
|  ( ) Tradicional (BPMN / Casos Múltiples)                                         |
|      - El proyecto instanciará flujos de proceso pre-definidos.                   |
|      - Proceso Asociado: [ Seleccionar BPMN Desplegado... v ]                     |
|                                                                                   |
|  -------------------------------------------------------------------------------  |
|  [📂 METADATA Y CLASIFICACIÓN (Para Analítica y Búsqueda)]                        |
|  Presupuesto Total (USD): [ 150,000      ]    Prioridad: [ Alta v ]               |
|  Cliente Asociado:        [ Banco Alpha  ]    SLA Global: [ 90 Días ]             |
|                                                                                   |
|                               [ CANCELAR ]           [ 🚀 INICIALIZAR PROYECTO ]  |
+-----------------------------------------------------------------------------------+
```

---

## Pantalla 10: Hub de Gestión de Proyecto Ágil (Backlog & Roadmap)
**Objetivo:** El "Centro de Mando" para el Líder o Scrum Master de un proyecto ágil. Aquí se planifica el trabajo (Backlog), se agrupa en iteraciones (Sprints/Milestones) y se revisan las métricas antes de pasar al tablero Kanban operativo (Pantalla 3).

```text
+-----------------------------------------------------------------------------------+
|  Proyectos > 📁 Implementación Core Bancario Cliente Alpha     [ ⚙️ Configurar ]  |
+-----------------------------------------------------------------------------------+
|  Líder: @Harolt   |   Estado: [ 🏃 En Ejecución ]   |   Salud: [ 🟢 A Tiempo ]    |
|  -------------------------------------------------------------------------------  |
|  Vistas:  [ 📋 BACKLOG ]    [ 📊 TABLERO KANBAN ]    [ 📈 MÉTRICAS Y AUDITORÍA ]  |
|  -------------------------------------------------------------------------------  |
|                                                                                   |
|  [ Sprint 1: Autenticación Base ] (Activo)                 [ Terminar Sprint ]    |
|  -------------------------------------------------------------------------------  |
|  = TK-001 | 👤 @Maria  | Firma de SLA Legal              | [✔ DONE]               |
|  = TK-002 | 👤 @Carlos | Configurar Azure AD             | [▶ IN PROGRESS]        |
|  = TK-003 | 👤 Unassigned | Desplegar VM de BD           | [⏳ TO DO]             |
|                                                                                   |
|                                                                                   |
|  [ Product Backlog ] (Tareas No Asignadas)                     [ + Crear Tarea ]  |
|  -------------------------------------------------------------------------------  |
|  = TK-004 | Levantar requerimientos de Caja                  | [ Alta   ]         |
|  = TK-005 | Integrar firma digital MS Doc                    | [ Normal ]         |
|  = TK-006 | Pruebas de Carga 10k usuarios                    | [ Baja   ]         |
|                                                                                   |
|  *(Puedes arrastrar tareas del Backlog al Sprint Activo o al Tablero Kanban)*     |
+-----------------------------------------------------------------------------------+

>>> Clic en [+ Crear Tarea] en el Backlog abre el siguiente Modal:

+-----------------------------------------------------------------------------------+
|  [⚙️] Configurar Tarea Ágil (Modal)                                               |
|  -------------------------------------------------------------------------------  |
|  Título: [ Levantar requerimientos de Caja                                   ]    |
|  Asignado a:  [ 👤 @Maria v ]                 Prioridad: [ 🔴 Alta v ]            |
|  Puntos de Historia (Agile): [ 5 ]            SLA (Tiempo Máximo): [ 48 Horas ]   |
|                                                                                   |
|  [ 📄 RECOLECCIÓN DE DATOS (Asociar Formulario Inteligente) ]                     |
|  ¿Esta tarea requiere que el ejecutor llene datos estructurados al finalizarla?   |
|  ( ) Ninguno (Solo mover la tarjeta a "Terminado")                                |
|  (x) Requerir Formulario JSON Guardado -> [ Form_Checklist_Caja_V1 v ]            |
|      ↳ (Este formulario fue creado previamente por Admin en la Pantalla 7)        |
|                                                                                   |
|                                            [ CANCELAR ]   [ 💾 GUARDAR TAREA ]    |
+-----------------------------------------------------------------------------------+
```

---

## Pantalla 11: Hub de Integraciones (APIs & Webhooks)
**Objetivo:** El panel administrativo donde TI o el analista configura cómo el iBPMS "escucha" al mundo exterior (Inbound) y cómo "actúa" sobre sistemas legado (Outbound), como el ERP o el CRM.

```text
+-----------------------------------------------------------------------------------+
|  Configuración > Integraciones & Eventos              [+ Nueva Conexión Externa]  |
+-----------------------------------------------------------------------------------+
|  Sección 1: CONEXIONES SALIENTES (Outbound / El iBPMS envía datos)                |
|  -------------------------------------------------------------------------------  |
|  Estas conexiones estarán disponibles como "Cajas de Acción" en el Lienzo BPMN.   |
|                                                                                   |
|  [🔌 Crear API ERP_Finanzas ]                                                     |
|  - Tipo: REST Client (POST)                                                       |
|  - URL Base: `https://api.empresa.com/erp/v2/pagos`                               |
|  - Autenticación: [ OAuth 2.0 (Bearer Token) v ]                                  |
|  - Mapeo de Variables: "Enviar `monto_aprobado` del Formulario al campo `amount`" |
|                                                                                   |
|                                                                                   |
|  Sección 2: EVENTOS ENTRANTES (Inbound / Webhooks)                                |
|  -------------------------------------------------------------------------------  |
|  Escucha señales externas para crear nuevas tareas o proyectos automáticamente.   |
|                                                                                   |
|  [📡 Webhook: Recepción de Correo O365 ]                  [ COPY WEBHOOK URL ]    |
|  - Evento Dedo: `POST /api/webhooks/v1/o365-trigger`                              |
|  - Acción: [ 🔥 Iniciar Proceso (BPMN) v ] -> Seleccionado: [ Onboarding_Client ] |
|  - Payload: "Guardar `mail.body` como descripción inicial."                       |
|                                                                                   |
|  [📡 Señal: Sensor IoT / Alarma SAP ]                     [ COPY WEBHOOK URL ]    |
|  - Evento Dedo: `POST /api/webhooks/v1/sap-trigger`                               |
|  - Acción: [ 📋 Crear Tarjeta Kanban v ] -> Tablero: [ Soporte IT Nivel 2 ]       |
+-----------------------------------------------------------------------------------+
```

---

## Pantalla 12: Bóveda Documental y SGDEA (Record Management)
**Objetivo:** El repositorio central oficial (Gestor Documental). Aquí aterrizan todos los PDFs generados automáticamente por el sistema al final de un proceso, manteniéndose inmutables y regidos por las Tablas de Retención Documental (TRD) para cumplimiento legal.

```text
+-----------------------------------------------------------------------------------+
|  Gestión Documental > Bóveda Central (SGDEA)                                      |
+-----------------------------------------------------------------------------------+
|  [🔍 Buscar Contrato, ID Proceso, Metadato...]     Filtros: [Por Vencer] [Firmados]
|  -------------------------------------------------------------------------------  |
|                                                                                   |
|  [📂 EXPLORADOR DE EXPEDIENTES]                   [📄 DETALLE DEL DOCUMENTO]      |
|                                                                                   |
|  🗂️ 2026 - Contratos Clientes                     | Nombre: Contrato_Alpha_Signed.pdf
|  ├── 📁 Exp: Implementación Alpha (ID: 991)       | Origen: Proceso Onboarding X
|  │   ├── 📎 Carta_Bienvenida.pdf                  | Fecha de Creado: 2026-02-15 
|  │   └── 📎 Contrato_Alpha_Signed.pdf  <<         | Huella Digital (SHA256): 9f86d...
|  │                                                | 
|  🗂️ 2026 - Auditoría y RRHH                       | [ Reglas de Retención (TRD) ]
|  ├── 📁 Exp: Onboarding Empleados                 | - Serie: Acuerdos Comerciales
|                                                   | - Tiempo Vivo: 10 Años
|                                                   | - Acción al caducar: [ Destruir ]
|                                                   |
|  -------------------------------------------------------------------------------  |
|  [🔗 INTEGRACIÓN EXTERNA SGDEA]                                                   |
|  ¿Enviar copia espejo a un repositorio corporativo de terceros?                   |
|  ( ) Ninguno (Usar Bóveda Interna Habilitada)                                     |
|  (x) Microsoft SharePoint [ Configurar Sitio v ]                                  |
|  ( ) NetDocuments REST API [ Configurar Auth v ]                                  |
+-----------------------------------------------------------------------------------+
```

---

## Pantalla 13: Portal de Desarrolladores y Extensibilidad (Módulos Complejos)
**Objetivo:** El "Centro de Control API" donde los administradores y desarrolladores externos pueden registrar "Súper Módulos" personalizados (Ej. un front-end en React propio para RRHH) de manera que puedan consumir el motor de estado y orquestación del iBPMS de forma segura.

```text
+-----------------------------------------------------------------------------------+
|  Configuración > Portal de Desarrolladores (API & Apps)       [+ Registrar App]   |
+-----------------------------------------------------------------------------------+
|                                                                                   |
|  [🔌 TUS APLICACIONES CONECTADAS (Súper Módulos Externos)]                        |
|  -------------------------------------------------------------------------------  |
|                                                                                   |
|  [ 🏦 App Core Crédito_React_V2 ]                         Estado: [ 🟢 Activo ]   |
|  - Tipo de Integración: Headless API Consumer                                     |
|  - Permisos (Scopes): `process.start`, `task.complete`, `dmn.evaluate`            |
|                                                                                   |
|  Credenciales de Acceso (OAuth 2.0 / JWT):                                        |
|  - Client ID: `app-credito-prod-8f92j`                   [ 📋 COPIAR ]            |
|  - Client Secret: `***************************`          [ 👁️ REVELAR / ROTAR ]    |
|                                                                                   |
|  -------------------------------------------------------------------------------  |
|                                                                                   |
|  [ 👥 Portal Autoservicio RRHH ]                          Estado: [ 🟡 Mantenimiento]|
|  - Tipo de Integración: IFrame Embebido en Menú Principal                         |
|  - URL de Inyección: `https://rrhh-interno.empresa.com/self-service`              |
|  - Permisos (Scopes): `user.read`, `task.read_own`                                |
|                                                                                   |
|  -------------------------------------------------------------------------------  |
|  [📚 DOCUMENTACIÓN TÉCNICA Y SANDBOX]                                             |
|  [ Explorar Swagger/OpenAPI del Motor iBPMS ]                                     |
|  [ Descargar Colección de Postman           ]                                     |
+-----------------------------------------------------------------------------------+
```

---

## Pantalla 14: Gobierno de Identidad y Accesos (RBAC/ABAC)
**Objetivo:** Panel de administración donde Operaciones de TI crea los esquemas de seguridad corporativa, agrupa usuarios, y crea los "Perfiles/Roles" que luego el dueño del proceso asocia a los "Lanes" en el Diseñador BPMN (Pantalla 6). Garantiza la segregación de datos.

```text
+-----------------------------------------------------------------------------------+
|  Seguridad > Roles y Jerarquías (RBAC)                      [+ Crear Nuevo Rol]   |
+-----------------------------------------------------------------------------------+
|  [🔗 Sincronizar con AD / EntraID ]      [ 🎭 Ver Sistema Como: @Seleccionar... v]|
|  -------------------------------------------------------------------------------  |
|                                                                                   |
|  [ ROLES GLOBALES DE SISTEMA (Creados Manualmente) ]                              |
|                                                                                   |
|  [ 🛡️ VPE_Finanzas ]                                  [ Editar ] [ Desactivar ]   |
|  - Nivel de Jerarquía: 2 (Director)                                               |
|  - Miembros Asignados: 👤 @Juan_CFO, 👤 @Maria_Treasury                           |
|  - Permisos Globales: `dashboard.view_all`, `process.override_sla`                |
|                                                                                   |
|  -------------------------------------------------------------------------------  |
|  [ ROLES DE PROCESO (Autogenerados por Diseñador BPMN - Pantalla 6) ]             |
|                                                                                   |
|  [ ⚙️ BPMN_Credito_Analista_Riesgos ]                 [ Asignar Usuarios ]        |
|  - Origen: Carril "Analista_Riesgos" en Proceso "Credito_Hipotecario_v2"          |
|  - Miembros Asignados: (Grupo de AD: `oficinas_bogota`)                           |
|  - Permisos Automáticos: Ejecutar Tarea `task_validar`, Leer/Escribir `iForm_A`   |
|                                                                                   |
|  -------------------------------------------------------------------------------  |
|  [ AUDITORÍA DE ACCESOS INMUTABLE ]                                               |
|  - 10:45am: Administrador añadió a @Pedro al rol "VPE_Finanzas"                   |
|  - 09:30am: Al desplegar BPMN_Crédito, el sistema autogeneró el rol "BPMN_Credito_Analista_Riesgos".|
+-----------------------------------------------------------------------------------+
```

---

## Pantalla 15: Panel Consolidado de Proyecto (Unified Project Dashboard)
**Objetivo:** El "Centro de Mando Supremo" de un Proyecto específico. Resuelve la necesidad de visibilidad híbrida. Aquí, un Project Manager o ejecutivo puede ver *absolutamente todas* las actividades pendientes cruzadas: tanto las Instancias de Procesos Tradicionales (BPMN) que pertenecen estructuralmente a este proyecto, como las Tarjetas Ágiles (Kanban) manuales, en una sola línea de tiempo y lista unificada.

```text
+-----------------------------------------------------------------------------------+
|  Proyectos > 📁 Implementación Core Bancario Cliente Alpha     [ ⚙️ Configurar ]  |
+-----------------------------------------------------------------------------------+
|  Líder: @Harolt   |   Estado: [ 🏃 En Ejecución ]   |   Salud: [ 🟢 A Tiempo ]    |
|  -------------------------------------------------------------------------------  |
|  Vistas: [ 🌐 RESUMEN GLOBAL ]   [ 📋 BACKLOG ]  [ 📊 TABLERO KANBAN ]            |
|  -------------------------------------------------------------------------------  |
|                                                                                   |
|  [ 🚦 TODAS LAS ACTIVIDADES PENDIENTES DEL PROYECTO (Híbrido Ágil + Tradicional)] |
|  Filtros: [Todas v] [Solo Ágil] [Solo BPMN] [Mis Tareas]                          |
|  -------------------------------------------------------------------------------  |
|  | Tipo   | Actividad / Tarea Ejecutándose | Asignado | Vencimiento | Estado   |  |
|  |--------|--------------------------------|----------|-------------|----------|  |
|  | [BPMN] | ⚙️ Aprobación Legal de SLA     | @Maria   | Hoy, 5:00p  | URGENTE  |  | << Instancia Camunda nativa
|  | [AGIL] | 📌 Configurar Servidor Azure AD| @Carlos  | Mañana      | EN CURSO |  | << Tarjeta Kanban manual
|  | [BPMN] | ⚙️ Firma Dig. Contrato         | @John    | 24 Oct      | PENDIENTE|  |
|  | [AGIL] | 📌 Pruebas Carga 10k           | Unassig. | Sprint 1    | TO DO    |  |
|                                                                                   |
|  [ + Iniciar Nuevo Proceso BPMN ]               [ + Nueva Tarea Kanban (Agile) ]  |
|  -------------------------------------------------------------------------------  |
|                                                                                   |
|  [ 📉 MÉTRICAS Y AUDITORÍA DEL PROYECTO (Audit Log Consolidado) ]                 |
|  - (BPMN) Hace 2h: @Maria completó Formulario de Viabilidad Técnica.              |
|  - (AGIL) Hace 5h: @Carlos movió "Req. de Cajas" a IN PROGRESS.                   |
|  - (CORE) Ayer: Proyecto iniciado por API Webhook O365.                           |
+-----------------------------------------------------------------------------------+
```

---

## Pantalla 16: Intelligent Intake y Embudo Administrativo (Plan A/B)
**Objetivo:** El "Centro de Cuarentena" para el rol Administrador. Aquí aterrizan las "Intenciones de Servicio" (Plan A) capturadas por el correo antes de ensuciar el motor BPMN. El Admin puede forzar creaciones manuales (Plan B). Evita el anti-patrón de instanciación basura.

```text
+-----------------------------------------------------------------------------------+
|  Service Delivery > Intake y Creación (Vista Solo Administrador)                  |
+-----------------------------------------------------------------------------------+
|  [ 🌐 CREACIÓN MANUAL (PLAN B) ]                                                |
|  [ + Iniciar Nuevo Service Delivery Vía Formulario Seguro ]                       |
|  -------------------------------------------------------------------------------  |
|                                                                                   |
|  [ 📥 EMBUDO DE INTAKE ACTIVO (PLAN A - Correos Salientes en Cuarentena) ]        |
|  "Tickets esperando validación humana para convertirse en un Proceso Real".       |
|  -------------------------------------------------------------------------------  |
|                                                                                   |
|  [ Card 1: Confirmación de Auditoría ]                                            |
|  - Cliente Identificado (CRM_ID): 🏢 InnovateTech Corp.                           |
|  - Plantilla (TO-BE) Sugerida: Auditoría Express_v2                               |
|  - Origen: Correo enviado por @John desde `auditorias@ibpms.com`                  |
|  - Hilo de Conversación (Thread): Activo (2 Correos)                              |
|                                                                                   |
|  [ 🗑️ Descartar (Es Spam/Conversación) ]      [ 🚀 CREAR SERVICE DELIVERY (BPMN) ]|
|                                                                                   |
|  -------------------------------------------------------------------------------  |
|                                                                                   |
|  [ Card 2: Falso Positivo ]                                                       |
|  - Cliente Identificado (CRM_ID): Pendiente (No match)                            |
|  - Plantilla (TO-BE) Sugerida: Desconocida                                        |
|  - Origen: Respuesta de "Fuera de la oficina" a `legal@ibpms.com`                 |
|                                                                                   |
|  [ 🗑️ Descartar (Es Spam/Conversación) ]      [ ✏️ Forzar Mapeo Manual ]          |
+-----------------------------------------------------------------------------------+
```

---

## Pantalla 17: Vista 360 del Cliente (Customer Card Account Management)
**Objetivo:** Consolidar el ruido operativo en un reporte digerible para Ejecutivos C-Level o Key Account Managers. Responde a la pregunta "Dónde está el trámite de este cliente".

```text
+-----------------------------------------------------------------------------------+
|  CRM / Clientes > 🏢 Detalle: Banco Alpha (ID: CRM-8891)                          |
+-----------------------------------------------------------------------------------+
|  [ 📞 Contactos ]   [ 💼 Contratos ]   [ 📊 SLA Histórico: 98% Cumplimiento ]     |
|  -------------------------------------------------------------------------------  |
|                                                                                   |
|  [ 🟢 SERVICE DELIVERIES ACTIVOS (BPMN) ]                                         |
|  1. Implementación Core Transaccional V2                                          |
|     - SLA Prometido: 90 Días  |   Llevamos: 45 Días                               |
|     - Estado Actual: 🚦 EN PROGRESO -> [ Tarea Actual: Pruebas de Carga 10k ]     |
|     - Cuello de Botella Detectado: Ninguno.                                       |
|                                                                                   |
|  2. Revisión Legal de Contrato de Mantenimiento Anual                             |
|     - SLA Prometido: 5 Días   |   Llevamos: 6 Días                                |
|     - Estado Actual: 🔴 ATRASADO -> [ Tarea Actual: Firma de VPE Finanzas ]       |
|                                                                                   |
|  -------------------------------------------------------------------------------  |
|  [ ⚡ KANBANS ÁGILES ASOCIADOS (Incidencias Rápidas) ]                            |
|  - Ticket de Soporte 098: Falla de login en portal (En progreso por Nivel 2)      |
|                                                                                   |
|  -------------------------------------------------------------------------------  |
|  [ 📥 INTAKES EN CUARENTENA (Esperando Instanciación Plan A) ]                    |
|  - Correo: "Revisar cotización anexa..." (Esperando validación de Administrador)  |
+-----------------------------------------------------------------------------------+
```

---

## Pantalla 18: Portal del Cliente Externo (Customer Portal B2B/B2C)
**Objetivo:** Interfaz de solo lectura y autoatención para los clientes externos (Ej. Representantes Legales de empresas o ciudadanos). Resuelve el autorreporte para "Service Delivery" logrando total transparencia de extremo a extremo (Vista Táctica y Estratégica). Autenticado vía Azure AD B2C.

```text
+-----------------------------------------------------------------------------------+
|  [LOGO iBPMS Customer Portal]            | Hola, María Gómez (Banco Alpha) [🚪] |
+-----------------------------------------------------------------------------------+
|                                                                                   |
|  [ 📈 VISTA ESTRATÉGICA: Tu Resumen Histórico ]                                   |
|  Servicios Solicitados: 15  |  Finalizados con Éxito: 14  |  En Ejecución: 1  |
|                                                                                   |
|  -------------------------------------------------------------------------------  |
|                                                                                   |
|  [ 🔍 VISTA TÁCTICA: Seguimiento en Tiempo Real ]                                 |
|                                                                                   |
|  📋 Radicado: SD-2024-9981 (Auditoría Trimestral Q3)                              |
|  Asignado a: Equipo Legal iBPMS                                                   |
|  SLA Objetivo: 15 de Noviembre, 2024                                              |
|                                                                                   |
|  [Progreso del Servicio - Tracker Simplificado]                                   |
|  ( ✔️ ) 1. Recibimos tu solicitud          [ Oct 20 ]                             |
|  ( ✔️ ) 2. Validación Documental           [ Oct 22 ]                             |
|  ( 🏃 ) 3. Ejecución de la Auditoría       <-- [ USTED ESTÁ AQUÍ ]                |
|  (    ) 4. Emisión Final del Documento                                            |
|                                                                                   |
|  -------------------------------------------------------------------------------  |
|                                                                                   |
|  [ 📂 TUS DOCUMENTOS (SGDEA Exportables) ]                                        |
|  📄 Auditoría_Q2_Final.pdf (Firmado el 15/Ago) [ ⬇️ Descargar Copia Certificada ] |
|  📄 Contrato_Marco_Firmado.pdf                 [ ⬇️ Descargar Copia Certificada ] |
+-----------------------------------------------------------------------------------+
```
