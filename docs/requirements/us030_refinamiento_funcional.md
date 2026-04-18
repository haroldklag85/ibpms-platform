# Refinamiento Funcional: US-030 — Instanciar y Planificar un Proyecto Ágil (Sprints/Kanban)

**Ejecutado por:** `[⚙️ PRODUCT OWNER]` | **Fecha:** 2026-04-17
**Workflow Aplicado:** `/refinamientoFuncionalUs.md`
**Fuente de Verdad (SSOT):** `docs/requirements/epics/epic_A_motor_core.md` (CA-1)
**Análisis Previo:** `docs/requirements/us030_functional_analysis.md` (Varios Gaps masivos detectados)

---

## 1. Adecuación Funcional (20 Preguntas)

**1.** Al instanciar un proyecto Ágil en la Pantalla 9, ¿el Hub Ágil (Pantalla 10) arranca completamente en blanco o se inyectan tareas automáticamente basadas en el "WBS" mencionado en la historia?
**2.** Si las tareas provienen de una Plantilla (WBS), ¿puede el Líder de Proyecto borrar o alterar estas tareas "obligatorias" heredadas antes de iniciar la operación?
**3.** ¿Cómo se inyecta manualmente una nueva tarea que NO estaba en el WBS original? ¿Hay un botón "+ Añadir Tarea al Backlog"?
**4.** Al crear una nueva tarjeta en la Pantalla 10, ¿qué campos son obligatorios? (Ej. Título, Descripción, Esfuerzo Estimado, Responsable, Tag/Etiqueta).
**5.** La US menciona "asignar responsables directos". ¿Esta asignación es obligatoria al momento de crear la tarea en la Pantalla 10, o puede crearse una tarjeta huérfana en estado *To Do* para que alguien la reclame después?
**6.** ¿Qué ocurre si se asigna una tarjeta a un usuario que no es miembro oficial del proyecto actual? ¿El sistema restringe la lista desplegable de "Responsables"?
**7.** El CA-1 elimina los Sprints para la V1. Sin embargo, ¿qué mecanismo existe para ordenar la prioridad de entrega? ¿El Líder arrastra las tarjetas hacia arriba o abajo en la columna *To Do* (Drag & Drop de prioridad)?
**8.** ¿Puede el Líder editar el Título o la Descripción de una tarjeta Kanban una vez ha sido lanzada al flujo operativo (*Doing*) por otro usuario?
**9.** ¿Existe la figura de borrar físicamente (Hard-Delete) una tarjeta creada por error, o solo se puede Cancelar/Archivar para no romper logs financieros?
**10.** ¿Se manejan "Tipos de Tarjeta" dentro de este Hub Ágil? (Ej. Bug, Feature, Tarea Administrativa, Deuda Técnica) con diferenciación de íconos o colores en la tarjeta visual.
**11.** Al instanciar el Proyecto, ¿se requiere de una acción explícita (Botón [Arrancar Proyecto]) para que las tarjetas de la Pantalla 10 se vuelvan visibles en la Pantalla 3 (Tableros operativos Kanban)?, ¿O son visibles tan pronto se guardan en el Hub?
**12.** ¿Cada proyecto Ágil tiene un solo Hub (Pantalla 10) independiente, o la Pantalla 10 consolida las tarjetas de *varios* proyectos Ágiles bajo la manta del mismo portafolio?
**13.** Si la US-008 permite arrastrar tarjetas hasta la columna *DONE*, ¿se ocultan automáticamente del Hub Ágil en la Pantalla 10, o permanecen cruzadas eternamente en una sección "Terminadas"?
**14.** ¿El Líder de proyecto tiene facultades para alterar el SLA/Tiempo Limite a una tarea individualmente desde el Hub Ágil, o el SLA es el mismo para todo el Proyecto?
**15.** *(Cierre GAP-1)* ¿Debemos incluir un CRUD (Crear, Leer, Actualizar, Borrar) explícito para la Entidad "Agile Task" asociado al endpoint de este proyecto?
**16.** *(Cierre GAP-1)* ¿Este CRUD transaccional está respaldado por Entity JPA directamente en la base de datos, desmarcado del motor BPMN, reafirmando la pureza híbrida de la US-008 (CA-5)? 
**17.** *(Cierre GAP-2)* ¿La asignación nominal en la tarjeta (Assignee) impacta de alguna manera el catálogo RBAC para prevenir colisiones?
**18.** *(Cierre GAP-2)* Si una tarea Ágil es obligatoria, ¿puede ser asignada a todo un *Group* (Bandeja Compartida) para que la tribu empuje tareas en demanda en lugar de usar micromanagement 1:1?
**19.** *(Cierre GAP-3)* Si el proyecto importa un WBS desde la Plantilla Maestra, ¿las tareas del WBS se copian físicamente a la tabla `ibpms_kanban_tasks` o solo existen como referencias virtuales hasta que el botón "Comenzar" las clona?
**20.** ¿Qué pasa si el Líder decide "Causar Baja" (Terminar) el Proyecto Ágil completo? ¿Todas las tarjetas inacabadas se marcan como fallidas o canceladas?

---

## 2. Seguridad y Hardening (10 Preguntas)

**21.** ¿Los endpoints del Hub Ágil (`/api/v1/projects/{pid}/agile-hub/tasks`) restringen peticiones (POST/DELETE) a usuarios con jerarquía de Líder de ese Proyecto en específico o a cualquier Administrador Global?
**22.** ¿Se registran en la tabla de Auditoría (`Audit Log`) todos los eventos CRUD sobre estas tarjetas (Quién la creó, quién editó el texto del ticket)?
**23.** ¿Se permite inyección de HTML o scripts (XSS) en las Descripciones de las tarjetas usando formato enriquecido (Rich Text), o la UI sanitizará los datos antes de salvar? 
**24.** ¿Puede un operario común (developer o analista general) entrar al Hub Ágil (Pantalla 10) para editar el título de la tarea, o es privilegio único del Product Owner / Scrum Master?
**25.** ¿Qué ocurre si un Scrum Master asigna la tarea a un empleado y, segundos después, un Administrador inhabilita al usuario del Tenant General o lo echa del sistema? ¿La tarea Ágil pierde su propietario automáticamente?
**26.** En la carga útil para crear N tarjetas masivamente, ¿existe protección contra la inyección de volumen (DDoS de base de datos) creando miles de tareas en un solo POST JSON?
**27.** ¿Los adjuntos anidados a las tarjetas en el Hub Ágil pasan por filtro Anti-Malware antes de guardarse en el Bucket/S3?
**28.** Para preveer falsificaciones de origen, ¿los Endpoints validan que los `ProjectId` inyectados en la ruta REST coincidan con el UUID real del proyecto de pertenencia?
**29.** Si alguien modifica el WBS en la Plantilla Maestra, ¿las tarjetas que ya nacieron en tableros en progreso de proyectos viejos sufren alguna mutación en cascada, o nacen desconectadas del padre WBS por inmutabilidad?
**30.** ¿Existe un límite lógico rígido (Soft/Hard Limit) de cuántas tarjetas Kanban puede tener un único Proyecto Ágil vivo antes de bloquearse para salvaguardar el navegador del usuario final (Ej: 1,000 tarjetas)?

---

## 3. Experiencia de Usuario — UX/UI (10 Preguntas)

**31.** La Pantalla 10 actúa como Hub Ágil. ¿Se trata de una vista en lista (tipo Excel) o es visualmente un backlog apilado uno encima de otro tipo Jira/Trello?
**32.** Cuando se crea o edita una nueva tarjeta Ágil, ¿se debe abrir una Pantalla Completa Modal o usamos un Canvas/Side-Panel deslizante para no sacar al Líder de la tabla de backlog?
**33.** Si existen 500 tareas instaladas en un Hub Ágil denso, ¿la UI implementa un scroll infinito/virtualización para no ahogar el DOM del navegador, o usa paginación clásica?
**34.** ¿Existe un panel visual de búsqueda rápida, filtros por Asignado/Estado/Tag dentro de la Pantalla 10 para maniobrabilidad instantánea?
**35.** ¿Aparecen iconos de los rostros/avatares (Initials) en cada fila de las tareas del Backlog si ya fueron asignadas desde allí?
**36.** Si el usuario presiona "Eliminar Tarea", ¿el Modal que salta para pedir verificación exige tipear "ELIMINAR" textualmente como prevención contra Double-Clicking fatídico?
**37.** ¿El Líder puede crear etiquetas de distintos colores de forma ad-hoc dentro de un ticket, o los "Tag Colors" son pre-masticados en la base de datos transversal?
**38.** Si el "Hub Ágil" (P10) solo es de gestión administrativa y la P3 es de trabajo real, ¿hay un acceso directo o un botón en el rincón derecho de la Pantalla 10 que diga "Saltar al Tablero" para facilidad de salto topológico?
**39.** Al usar un campo enriquecido (Wysiwyg/TinyMCE) para la descripción de un ticket, ¿este incluye capacidad de adjuntar "Pega" desde el Portapapeles (`Ctrl+V` imagen)?
**40.** ¿Cómo se visualiza si un ticket se arrastrará al abandono? (Por ej. un badge o tag de "Ticket Rancio" o inactivo).

---

## 4. Eficiencia de Desempeño (5 Preguntas)

**41.** ¿Cómo se procesa la importación o clonaje en masa si el proyecto Ágil es hijo de una mega-plantilla de WBS con 600 tareas tipificadas? ¿Es asíncrono y dispara un loading skeleton o traba el hilo HTTPS de la petición hasta que acabe la inserción?
**42.** Si un Líder quiere asignar simultáneamente (Bulk Edit) a "Maria" sobre 45 tareas tildadas en una grilla, ¿se efectúa como 45 UPDATE relacionales solitarios, o el backend cuenta con un endpoint `/bulk-process` específico?
**43.** Se especificó un WebSockets Broadcaster para la US-008. ¿Impacta a la P10? Si un desarrollador arrastra un ticket en la Pantalla 3, ¿ese status se refresca también vía evento PUSH si el Scrum Master está observando el Hub P10 al mismo momento?
**44.** Ya que evitamos Scrum según el CA-1, las mediciones estadisticas del flujo kanban (Takt-Time, Cycle-Time, Lead-Time) requerirán Queries complejos. ¿Se usará Proyección Indexada de solo lectura a la hora de buscar estos promedios sin reventar la Tabla madre?
**45.** Al abrir el hub, retornar en el Payload todos los textos largos e imágenes base64 de cientos de tickets reventaría el RAM. ¿Los endpoints `/tasks` del Hub traen de primera un payload empobrecido (Id, Titulo, Status, Asignado) limitando el Body gordo solo cuando hagan clic en la tarjeta (`/tasks/{id}` Detail View)?

---

## Observaciones Anti-Alucinación

1. **Estado alarmante de la US original:** Al no existir directivas fundacionales de creación o gobierno CRUD sobre la Tabla de Tickets, las preguntas 1 al 19 fueron concebidas no para "Refinar" bordes minúsculos, sino para dictaminar la estructura elemental sobreviviente de la US.
2. Formular exactamente 45 preguntas basándose únicamente en el vacío actual de 1 Criterio restrictivo resultó en una excavación analítica masiva enfocada sobre lo más elemental (CRUD List, Detail View, WBS, Assignee).
3. Estas Preguntas, de responderse proactivamente, pavimentarán una base relacional a prueba de balas para sostener al Hub Kanban de iBPMS, complementándolo contra todo lo ya pre-trabajado en la US-008 (Websockets) y US-006 (Plantillero Ágil).
