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
|  [📥] Inbox (12)  |   ----------------------------------------------------------  |
|  [📂] Proyectos   |   👋 Buenos días, @Harolt. ¿Qué necesitas iniciar hoy?        |
|  [📊] Dashboards  |   ----------------------------------------------------------  |
|                   |   [ RECURSOS HUMANOS ]                                        |
|  --- ADMIN ---    |   +-------------------+  +-------------------+                |
|  [⚙️] Diseño BPMN |   | 🏖️ Vacaciones    |  | 🤝 Onboarding     |                |
|  [📝] Formularios |   | Solicitar días de |  | Ingreso de nuevo  |                |
|  [🧠] Reglas IA   |   | descanso legales. |  | empleado.         |                |
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

## Pantalla 1: Bandeja Unificada (Unified Inbox)
**Objetivo:** El buzón consolidado. A diferencia de la Pantalla 0 (donde se *inician* cosas), aquí es donde el usuario *recibe* el trabajo que otras personas o sistemas le asignaron, o donde *retoma* procesos que dejó guardados como Borradores.

```text
+-----------------------------------------------------------------------------------+
|  [LOGO iBPMS]  |  🔍 Buscar tarea, caso, ID...        |  🔔 (3) | 👤 Mi Perfil  |
+-----------------------------------------------------------------------------------+
|  [🏠] Inicio      |  Filtros: [Bandeja] [Urgentes] [Mis Procesos] [Borradores]    |
|  ---------------  |---------------------------------------------------------------|
|  Inbox (12)       |  TARJETAS DE TAREAS (Lista Vertical)  [✅ Aprobar Seleccionados]|
|  Borradores (2)<--|                                                               |
|  Proyectos / KB   |  +---------------------------------------------------------+  |
|  Mis Procesos     |  | [✏️] [BORRADOR]    [ GUARDADO: Hace 1 hr ]              |  |
|  Dashboards (BI)  |  |     Proceso: Req. de Compra < $5000                     |  |
|  Reglas IA (DMN)  |  |     [ 🗑️ Descartar Borrador ]  [ ▶️ Retomar Proceso ] -------> |  | << Clic abre Pantalla 2
|  Admin / Setup    |  +---------------------------------------------------------+  |
|                   |                                                               |
|                   |  +---------------------------------------------------------+  |
|                   |  | [x] [SLA: 2 Hrs] [URGENTE]                              |  |
|                   |  |     Tarea: Aprobar Contrato Legal 001                   |  |
|                   |  |     Proceso: Onboarding Cliente X - Iniciado por: Juan P|  |
|                   |  |     [ Ver Adjuntos (2) ]  [ Asignarme Tarea / Claim ]   |  |
|                   |  +---------------------------------------------------------+  |
|                   |                                                               |
|                   |  +---------------------------------------------------------+  |
|                   |  | [ ] [SLA: 1 Día] [NORMAL]                               |  |
|                   |  |     Tarea: Revisión Financiera Vuelo Comercial          |  |
|                   |  |     [ Ver Adjuntos (0) ]  [ ↩️ Liberar (Unclaim) ] -----> |  | << Clic abre Formulario Dinámico (Pantalla 2)
|                   |  +---------------------------------------------------------+  |
+-----------------------------------------------------------------------------------+
```

---

## Pantalla 2: Vista de Detalle / Formulario Dinámico (JSON Data Entry)
**Objetivo:** La pantalla donde el usuario ejecuta el trabajo. Captura la data estructurada (JSON payload) y muestra historial. Integra "Minitareas Kanban".

---

## Pantalla 1B: Bandeja de Entrada Avanzada (Filtros Docketing / M365)
**Objetivo:** Vista evolucionada del Inbox para usuarios "Power Users" (Ej: Legal, Operaciones) que gestionan altos volúmenes y requieren filtros cruzados avanzados importados de los prototipos UI2.html.

```text
+-----------------------------------------------------------------------------------+
|  [LOGO Docketing/iBPMS]  |  🔍 Buscar en todos los campos...       |  🔔 | 👤 Perfil|
|-----------------------------------------------------------------------------------|
|  [🏠] Bandeja Entrada |  [ Filtro Cliente v ] [ Filtro Proyecto v ] [ 📅 Rango ]  |
|  [🧠] Config. IA      |  -------------------------------------------------------  |
|  [🔍] Búsqueda Avz.   |  Actividades: [x] Acuse Enviado [ ] Tarea [ ] Proy Creado |
|  [📄] Plantillas      |  [ Limpiar Filtros ]                                      |
|  ---------------      |-----------------------------------------------------------|
|                       |  | Remitente | Asunto | Previsualización | Fecha | Cliente|
|                       |  |-----------|--------|------------------|-------|--------|
|                       |  | John Doe  | Update | Hi team, ...     | 5:45p | ClientC|
|                       |  | Jane S.   | Re: M..| Sounds good...   | 4:30p | Innovat|
|                       |  | alert@sys | Alert! | Cambio de Deadl..| 6:15p | GlobalT|
+-----------------------------------------------------------------------------------+
```

---

## Pantalla 2C: Detalle de Correo con Copiloto IA (Smart Suggestions)
**Objetivo:** Materialización del asistente "Human-in-the-Loop" derivado de los prototipos UI1, UI3 y UI4. La vista se divide horizontalmente: arriba el correo original, y anclado abajo un panel colapsable de Inteligencia Artificial que desmenuza recomendaciones atómicas accionables y borradores de respuesta.

```text
+-----------------------------------------------------------------------------------+
|  [< Volver]   Project Update & Next Steps  (De: John Doe)                         |
|-----------------------------------------------------------------------------------|
|  (Panel Superior - Renderizado del Correo)                                        |
|  Hi team,                                                                         |
|  Following up on our discussion, I've attached the financial report for Q3.       |
|  Please review it by EOD Friday, October 28th, 2024.                              |
|                                                                                   |
|  ===============================================================================  |
|  [v] SUGERENCIAS INTELIGENTES DE IA (Panel Inferior Colapsable - UI4)             |
|  ===============================================================================  |
|                                                                                   |
|  [ TARJETAS DE EXTRACCIÓN SEMÁNTICA ]                                             |
|  +--------------------+  +--------------------+  +-----------------------------+  |
|  | Clasificación      |  | Cliente Asociado   |  | Fechas Clave                |  |
|  | [ Informe Finan. ] |  | [ Client Corp. ]   |  | [ 28 de Octubre, 2024 ]     |  |
|  | [✔️ Aceptar] [❌ R]|  | [✔️ Aceptar] [❌ R]|  | [✔️ Aceptar] [❌ Rechazar ] |  |
|  +--------------------+  +--------------------+  +-----------------------------+  |
|                                                                                   |
|  [ RESPUESTA SUGERIDA M365 (Bilingual Draft) ]                                    |
|  +-----------------------------------------------------------------------------+  |
|  | ✨ "Hello John, Thank you for the update. We have received the financial    |  |
|  | report for Q3 and will review it by the deadline this Friday..."            |  |
|  |                                                                             |  |
|  | [ ❌ Rechazar ]                              [ 🚀 Aceptar y Enviar Email ]  |  |
|  +-----------------------------------------------------------------------------+  |
|                                                                                   |
|  [ INTEGRACIÓN CON GESTOR DE PROYECTOS (BPMN / Kanban) ]                          |
|  +-----------------------------------------------------------------------------+  |
|  | Crear tarea: "Revisar informe financiero Q3"                                |  |
|  | Fecha de vencimiento: 28 de Octubre, 2024                                   |  |
|  |                                                                             |  |
|  |                                [ ❌ Rechazar ] [ 🚀 Aceptar y Crear Tarea ] |  |
|  +-----------------------------------------------------------------------------+  |
+-----------------------------------------------------------------------------------+
```

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

## Pantalla 6: CRUD de Procesos y Diseñador BPMN
**Objetivo:** Interfaz administrativa para la gestión del ciclo de vida de los procesos (Crear, Editar, Desplegar) e importar/dibujar flujos BPMN mediante bpmn-js integrado.

```text
+-----------------------------------------------------------------------------------+
|  Admin > Gestión de Procesos (BPMN)                     [+ Crear Nuevo Proceso]   |
+-----------------------------------------------------------------------------------+
|                                                                                   |
|  [⬆️ Importar .bpmn] | [⬇️ Exportar] | [⏪ Revertir a V1.x] | [🚀 Desplegar Producción]|
|  -------------------------------------------------------------------------------  |
|                                                                                   |
|   HERRAMIENTAS   |  LIENZO DE DIBUJO BPMN (Integración bpmn-js)                     |
|                  |  +------------------------------------------------------------+  |
|   (o) Evento     |  | Pool: Solicitud de Crédito                                 |  |
|   [ ] Tarea      |  +------------------------------------------------------------+  |
|   < > Compuerta  |  | Lane: Radicador  | (Inicio) -> [ Llenar Info ] ->          |  |
|   [=] Carril     |  +------------------------------------------------------------+  |
|                  |  | Lane: Aprobador  |          -> [ Validar ] -> (Fin)        |  |
|                  |  +------------------------------------------------------------+  |
|                  |                                                                |
|  -------------------------------------------------------------------------------  |
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

## Pantalla 7: Constructor de Formularios Inteligentes (JSON Form Builder)
**Objetivo:** Interfaz administrativa (drag and drop) para crear los formularios que visualizará el usuario final en cada tarea. El motor detrás genera y valida un JSON Schema estándar.

```text
+-----------------------------------------------------------------------------------+
|  Admin > Formularios                                        [+ Crear Formulario]  |
+-----------------------------------------------------------------------------------+
|  Nombre del Formulario: [ Form_Aprobacion_Credito_V1   ]    [💾 Guardar Versión]  |
|  Vista Previa Responsiva:  [ 💻 Escritorio ]  [ 📱 Móvil ]                        |
|  -------------------------------------------------------------------------------  |
|                                                                                   |
|  COMPONENTES DISPONIBLES |  LIENZO DEL FORMULARIO (Drag & Drop)                   |
|                          |                                                        |
|  [T] Texto Corto         |  +--------------------------------------------------+  |
|  [===] Párrafo Largo     |  | [T] Ingrese Monto Aprobado                       |  |
|  [123] Número            |  | Variable JSON (key): `monto_aprobado`            |  |
|  [📅] Fecha              |  | Obligatorio: (x) Sí  ( ) No                      |  |
|  [v] Desplegable         |  +--------------------------------------------------+  |
|  [(x)] Radio Button      |                                                        |
|  [[x]] Checkbox          |  +--------------------------------------------------+  |
|  [📎] Subir Archivos     |  | [v] Seleccione el Centro de Costos               |  |
|  [H1] Título Sección     |  | Variable JSON (key): `centro_costos`             |  |
|                          |  | Opciones: [ IT, Ventas, Legal ]                  |  |
|                          |  +--------------------------------------------------+  |
|                          |                                                        |
|  -------------------------------------------------------------------------------  |
|  VISTA PREVIA DE DATOS (JSON Generado en tiempo de ejecución):                    |
|  { "monto_aprobado": 1500, "centro_costos": "IT" }                                |
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
|  [ ROLES DEL SISTEMA (Perfiles que pueden amarrarse a un Carril BPMN) ]           |
|                                                                                   |
|  [ 🛡️ VPE_Finanzas ]                                  [ Editar ] [ Desactivar ]   |
|  - Nivel de Jerarquía: 2 (Director)                                               |
|  - Miembros Asignados: 👤 @Juan_CFO, 👤 @Maria_Treasury                           |
|  - Permisos Globales: `dashboard.view_all`, `process.override_sla`                |
|                                                                                   |
|  [ 🛡️ Radicador_Oficina ]                             [ Editar ] [ Desactivar ]   |
|  - Nivel de Jerarquía: 5 (Operativo)                                              |
|  - Miembros Asignados: (Grupo de AD: `oficinas_bogota`)                           |
|  - Permisos Globales: `task.create`, `task.read_own`                              |
|                                                                                   |
|  -------------------------------------------------------------------------------  |
|  [ AUDITORÍA DE ACCESOS INMUTABLE ]                                               |
|  - 10:45am: Administrador añadió a @Pedro al rol "VPE_Finanzas"                   |
|  - 09:30am: Rol "Radicador_Oficina" fue asignado al Carril 1 del Proceso Crédito  |
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
