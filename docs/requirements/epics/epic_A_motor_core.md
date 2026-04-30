# [TÍTULO DE LA ÉPICA]
> **Fuente:** Modularizado desde `v1_user_stories.md` | **Fecha:** 2026-04-10
> **Coverage:** `.agentic-sync/coverage_matrix.md` (centralizada)

---
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

### US-001: Obtener Tareas Pendientes en el Workdesk
**Como** Analista / Usuario de Negocio
**Quiero** visualizar una lista consolidada de mis tareas pendientes (BPMN o Kanban) al ingresar a la plataforma (Workdesk)
**Para** saber exactamente qué gestiones operativas debo priorizar y resolver hoy.


> [!IMPORTANT]
> **Dependencias Externas Críticas de la US-001:**
> - **US-002 (Reclamar Tarea / Pantalla 1):** Los WebSockets de desaparición instantánea de tareas (CA-06, CA-13) dependen de que US-002 publique el evento de asignación al reclamar. Sin este evento, las tareas reclamadas por otros seguirán visibles fantasma en la grilla.
> - **US-029 (Completar Tarea / Pantalla 2):** Toda ejecución de tarea (abrir formulario, enviar datos, completar) está FUERA de alcance de US-001. El Workdesk solo lista y prioriza; US-029 ejecuta.
> - **US-036 (RBAC / Pantalla 14):** La delegación segura (CA-15) y el Skill-Based Routing (CA-16) consumen la matriz de roles, habilidades y jerarquía organizacional administrada en Pantalla 14. Sin RBAC, la delegación y el Anti Cherry-Picking no pueden funcionar.
> - **US-005 (Despliegue BPMN / Pantalla 6):** La columna "Avance" (CA-17, CA-23) necesita conocer la estructura del proceso BPMN desplegado (total de User Tasks) para calcular el porcentaje de progreso.
> - **US-008 (Kanban / Pantalla 3):** Las tareas Kanban consolidadas en la grilla (CA-03) provienen del módulo de Kanban. La degradación elegante (CA-07, CA-18) prioriza estas tareas cuando Camunda cae.
> - **US-031 (Gantt / Pantalla 10.B):** Las tareas de proyectos tradicionales también se consolidan en la grilla unificada del Workdesk.


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


  # ==============================================================================
  # E. REMEDIACIONES POST-AUDITORÍA (Sprint Remediation Brief 2026-04-05)
  # Origen: docs/requirements/us001_functional_analysis.md
  # Tickets: REM-001-01 a REM-001-05
  # Propósito: Cerrar GAPs de implementación detectados por el workflow
  #            /analisisEntendimientoUs.md tras finalizar las 17 iteraciones
  #            de la Auditoría Integral del Backlog.
  # ==============================================================================

  Scenario: [REMEDIACIÓN] Resolución de Contradicción de Paginación y Búsqueda (CA-19)
    # Origen: REM-001-01 — GAP-1 del us001_functional_analysis.md
    # Resuelve la contradicción entre CA-01 (50 tareas), CA-02 (búsqueda híbrida),
    # CA-09 (15 tarjetas) y CA-10 (búsqueda server-side exclusiva).
    Given la necesidad de unificar el modelo de paginación y búsqueda del Workdesk
    Then queda DEFINIDO el modelo canónico de paginación como:
    And 1. El Backend retornará bloques de 15 registros por página (CA-09 es el límite visual canónico). El "50" del CA-01 se interpreta como ejemplo ilustrativo, NO como contrato técnico.
    And 2. La búsqueda es EXCLUSIVAMENTE Server-Side (CA-10 ANULA al CA-02). El Frontend NO filtrará en memoria local. Todo filtrado y búsqueda se ejecutará contra la Base de Datos con índices `pg_trgm` y Debounce de 300ms.
    And 3. El Hard Limit de 100 registros del CA-10 es un candado de seguridad: si un request manipula el query param `size` a un valor mayor a 100, el Backend retornará `HTTP 400 Bad Request`.
    And 4. Queda ANULADO el comportamiento híbrido del CA-02 (filtrado client-side + petición paralela al servidor). Toda búsqueda emite una única petición al Backend.

  Scenario: [REMEDIACIÓN] Contrato API Estandarizado para la Grilla del Workdesk (CA-20)
    # Origen: REM-001-02 — GAP-2 del us001_functional_analysis.md
    Given la necesidad de alinear Frontend y Backend en el contrato REST de la Grilla Unificada
    Then el Backend expondrá los siguientes endpoints documentados con OpenAPI/Swagger annotations:
    And `GET /api/v1/workdesk/tasks` — Grilla Unificada con query params:
    And   - `page` (int, default: 1) — Número de página.
    And   - `size` (int, default: 15, max: 100) — Registros por página.
    And   - `search` (string, opcional) — Texto libre para búsqueda server-side con `pg_trgm`.
    And   - `origin` (enum: `ALL`, `BPMN`, `KANBAN`, `GANTT`, default: `ALL`) — Filtro por tipo de tarea.
    And   - `status` (enum: `ALL`, `PENDING`, `IN_PROGRESS`, `OVERDUE`, default: `ALL`) — Filtro por estado.
    And   - `sort` (string, default: `sla_asc`) — Ordenamiento por SLA ascendente forzoso (CA-01).
    And El Response Structure del DTO sanitizado (CA-14) será:
    And   `{ data: [{ id, name, type_badge, sla_deadline, sla_color, status, progress_percent, assignee_name, financial_impact }], pagination: { page, size, total_records, total_pages } }`
    And `GET /api/v1/workdesk/tasks/{userId}` — Grilla delegada (CA-04, CA-15), con validación RBAC perimetral que verifica jerarquía.

  Scenario: [REMEDIACIÓN] Definición del Skill-Based Routing y Skipeo Justificado (CA-21)
    # Origen: REM-001-03 — GAP-3 del us001_functional_analysis.md
    Given la activación del interruptor administrativo "Atender Siguiente" (CA-08, CA-16) con Skill-Based Routing
    Then el modelo de habilidades del operario seguirá esta estructura:
    And 1. Las habilidades (skills) se administran en la Pantalla 14 (US-036) como un array de etiquetas simples asociadas al usuario (Ej: `["creditos_hipotecarios", "seguros_vida", "reclamos"]`). V1 NO soporta niveles de experticia (diferido a V2).
    And 2. El algoritmo de asignación cruzará la etiqueta de la categoría de la tarea más antigua/crítica contra el array de skills del operario. Si hay match, se asigna.
    And 3. Si NINGUNA tarea del sistema coincide con los skills del operario, el Backend asignará la tarea más antigua/crítica independientemente del skill (Fallback Universal), y registrará un WARNING en el Audit Log: "Asignación sin match de skill para el usuario {userId}".
    And 4. El "Skipeo Justificado" (CA-16) presentará un Dropdown con motivos predefinidos: "Cliente no responde", "Requiere documentación adicional", "Fuera de mi área de conocimiento", "Otro". Si selecciona "Otro", se habilita un campo de texto libre obligatorio (mínimo 10 caracteres).
    And 5. Cada Skip queda registrado como asiento inmutable en el Audit Log con: `{userId, taskId, skip_reason, timestamp}`. Un operario que acumule más de 3 Skips consecutivos activará una alerta al Supervisor.

  Scenario: [REMEDIACIÓN] Filtros Facetados para la Grilla del Workdesk (CA-22)
    # Origen: REM-001-04 — GAP-4 del us001_functional_analysis.md
    Given la grilla unificada del Workdesk con buscador de texto server-side (CA-10, CA-19)
    Then la interfaz incorporará una barra de filtros facetados ADICIONAL al buscador de texto:
    And - Filtro por Tipo de Tarea: `[Todos]` / `[⚡ Procesos BPMN]` / `[📅 Proyectos Gantt]` / `[📋 Kanban]` — mapea al query param `origin` del CA-20.
    And - Filtro por Estado SLA: `[Todos]` / `[🟢 Al día]` / `[🟡 Por vencer]` / `[🔴 Vencida]` — mapea al query param `status` del CA-20.
    And - Los filtros se aplicarán como query params adicionales en la petición server-side (CA-19), NO como filtrado local en el navegador.
    And - Los filtros activos se mostrarán como "Chips" removibles sobre la grilla para dar feedback visual de los filtros aplicados.
    And - Al activar un filtro, la paginación se reinicia a la página 1 automáticamente.
    And - Los filtros seleccionados se preservarán en la sesión mediante `KeepAlive` (CA-12), de modo que al navegar y regresar al Workdesk, los filtros sigan activos.

  Scenario: [REMEDIACIÓN] Fórmula Determinista para la Columna "Avance" (CA-23)
    # Origen: REM-001-05 — GAP-5 del us001_functional_analysis.md
    Given la 4ta columna "Avance" de la grilla unificada del Workdesk (CA-03, CA-17)
    Then el cálculo del porcentaje de avance seguirá las siguientes fórmulas según el tipo de tarea:
    And 1. **Tareas BPMN:** `Avance = (Índice ordinal de la UserTask actual) / (Total de UserTasks del proceso BPMN desplegado) × 100`. Ejemplo: si el proceso tiene 5 User Tasks y la actual es la 3ra, el avance es 60%.
    And 2. **Tareas Kanban:** `Avance = (Índice ordinal de la columna actual) / (Total de columnas del tablero Kanban) × 100`. Ejemplo: si el tablero tiene 4 columnas (TODO, DOING, REVIEW, DONE) y la tarea está en REVIEW (3ra), el avance es 75%.
    And 3. **Tareas Gantt:** `Avance = Porcentaje de completitud reportado manualmente por el asignado`. No se calcula automáticamente.
    And 4. La representación visual será una **barra de progreso horizontal** (progress bar) con el porcentaje numérico superpuesto (Ej: `[████████░░] 75%`).
    And 5. Si el Backend no puede calcular el avance (Ej: proceso BPMN sin User Tasks definidas o con estructura no lineal con Gateways paralelos), la columna mostrará `N/D` (No Disponible) en lugar de un porcentaje erróneo.


  # ==============================================================================
  # F. REFINAMIENTO FUNCIONAL POST-CUESTIONARIO (2026-04-05)
  # Origen: Cuestionario de 45 preguntas del workflow /refinamientoFuncionalUs.md
  # Propósito: Cerrar huecos descubiertos durante el refinamiento de la US-001.
  # ==============================================================================

  Scenario: [REFINAMIENTO] Umbrales Configurables del Semáforo SLA (CA-24)
    # Origen: Pregunta #6 del Refinamiento Funcional
    # Resuelve: No existían umbrales numéricos para los colores del semáforo SLA.
    Given el Ticking Engine de semáforos SLA del CA-05 y CA-11
    Then los umbrales de transición de color se definirán en porcentaje del tiempo restante respecto al total del SLA:
    And 🟢 **Verde:** Más del 50% del tiempo total restante.
    And 🟡 **Amarillo:** Entre el 50% y el 15% del tiempo total restante.
    And 🔴 **Rojo:** Menos del 15% del tiempo total restante.
    And ⚫ **Vencida (Negro/Gris):** 0% — la fecha límite ya pasó.
    And estos porcentajes serán los valores POR DEFECTO del sistema, pero cada tenant podrá personalizarlos desde la configuración administrativa (US-036 / Pantalla 14) si sus operaciones requieren umbrales diferentes.
    And el Frontend calculará el color localmente usando `sla_deadline` del DTO (CA-20) comparado contra `Date.now()`.

  Scenario: [REFINAMIENTO] Recálculo de Semáforos al Volver de Pestaña Inactiva (CA-25)
    # Origen: Pregunta #7 del Refinamiento Funcional
    # Resuelve: requestAnimationFrame se pausa en pestañas inactivas del navegador.
    Given el Global Heartbeat Store basado en requestAnimationFrame (CA-11)
    When el navegador pausa el requestAnimationFrame porque el usuario minimizó la pestaña o cambió a otra
    Then al detectar el evento `visibilitychange` del navegador (la pestaña vuelve a estar activa), el Heartbeat Store ejecutará un recálculo INMEDIATO de todos los semáforos SLA visibles usando `Date.now()` como referencia.
    And si durante la inactividad alguna tarea cambió de color (Ej: pasó de Amarillo a Rojo), el cambio se reflejará instantáneamente sin esperar el próximo ciclo del requestAnimationFrame.
    And si la inactividad superó los 5 minutos, se activará además el mecanismo de auto-refresco del CA-31.

  Scenario: [REFINAMIENTO] Relleno Automático de Página tras Remoción por WebSocket (CA-26)
    # Origen: Pregunta #9 del Refinamiento Funcional
    # Resuelve: La página queda con 14 de 15 tarjetas al desaparecer una por WebSocket.
    Given la desaparición animada de una tarea vía WebSocket (CA-06, CA-13) en una página de 15 tarjetas (CA-09)
    Then el Frontend acumulará las remociones por WebSocket durante una ventana de 5 segundos (consistente con el throttling del CA-13).
    And al finalizar la ventana, si la página tiene menos de 15 tarjetas, el Frontend emitirá UNA SOLA petición silenciosa al Backend solicitando las tarjetas faltantes para rellenar la página a su capacidad de 15.
    And las tarjetas nuevas aparecerán con una animación sutil de fade-in para no confundir al usuario con apariciones repentinas.
    And si la página completa queda vacía (todas las tareas fueron reclamadas), se aplicará la regla del CA-12: redirigir automáticamente a la Página 1.

  Scenario: [REFINAMIENTO] Vocabulario Completo de Acciones WebSocket (CA-27)
    # Origen: Pregunta #10 del Refinamiento Funcional
    # Resuelve: Solo existía la acción REMOVE; faltaban acciones para otros eventos de la grilla.
    Given la conexión WebSocket para sincronización en tiempo real del Workdesk (CA-06, CA-13)
    Then el Backend emitirá mensajes atómicos (CA-13) con el siguiente vocabulario estandarizado de acciones:
    And `REMOVE` — Una tarea fue reclamada por otro usuario o reasignada. El Frontend la desvanece (CA-13).
    And `ADD` — Una nueva tarea fue asignada al usuario (por reclamo, por rotación de Skill-Based Routing, o por asignación forzosa de un supervisor). El Frontend la incorpora a la grilla respetando el ordenamiento SLA (CA-01).
    And `UPDATE` — Un campo visible de una tarea existente cambió (Ej: el estado SLA pasó a OVERDUE, el Impacto Financiero cambió). El Frontend actualiza la celda afectada sin recargar la fila completa.
    And `PRIORITY_CHANGE` — El orden global de priorización cambió (Ej: una tarea recibió el badge 🔥 Impacto). El Frontend reordena la grilla localmente.
    And cada mensaje WebSocket seguirá la estructura: `{ action: 'REMOVE|ADD|UPDATE|PRIORITY_CHANGE', taskId: 'TK-123', payload?: {...} }`.
    And para `ADD` y `UPDATE`, el `payload` contendrá ÚNICAMENTE los campos del DTO sanitizado del CA-20 que cambiaron.

  Scenario: [REFINAMIENTO] Prevención de Condición de Carrera en "Atender Siguiente" (CA-28)
    # Origen: Pregunta #16 del Refinamiento Funcional
    # Resuelve: 200 operarios presionando "Atender Siguiente" simultáneamente podrían recibir la misma tarea.
    Given la activación del modo "Atender Siguiente" (CA-08, CA-16) con múltiples operarios conectados simultáneamente
    Then el Backend garantizará la asignación atómica de tareas utilizando bloqueo pesimista en la Base de Datos (SELECT ... FOR UPDATE SKIP LOCKED) para evitar que dos operarios reciban la misma tarea.
    And si un operario solicita una tarea y esta ya fue asignada a otro en la misma fracción de segundo, el Backend seleccionará automáticamente la SIGUIENTE tarea disponible que coincida con los skills del operario (CA-21), retornando la tarea correcta sin error visible.
    And el operario NUNCA recibirá un error "Tarea ya asignada" al presionar "Atender Siguiente" — el Backend resolverá la colisión internamente y le dará la siguiente tarea válida.
    And este mecanismo es análogo al del US-002 CA-01 (Reclamo Simultáneo), pero aplicado al algoritmo de enrutamiento forzoso en lugar del reclamo manual.

  Scenario: [REFINAMIENTO] Contadores en Filtros Facetados del Workdesk (CA-29)
    # Origen: Pregunta #17 del Refinamiento Funcional
    # Resuelve: Los filtros facetados (CA-22) no mostraban cuántas tareas existen en cada categoría.
    Given la barra de filtros facetados del Workdesk (CA-22)
    Then cada opción de filtro mostrará entre paréntesis el conteo total de tareas de esa categoría:
    And Ejemplo de filtros con contadores: `[Todos (62)]` / `[⚡ BPMN (45)]` / `[📋 Kanban (12)]` / `[📅 Gantt (5)]` / `[🟢 Al día (40)]` / `[🟡 Por vencer (14)]` / `[🔴 Vencida (8)]`.
    And el Backend retornará los contadores como parte del response de la grilla (CA-20), en un objeto adicional: `facets: { origin: { BPMN: 45, KANBAN: 12, GANTT: 5 }, status: { PENDING: 40, IN_PROGRESS: 14, OVERDUE: 8 } }`.
    And los contadores se actualizarán con cada petición a la grilla, NO en tiempo real por WebSocket (para evitar ruido visual excesivo).

  Scenario: [REFINAMIENTO] Rate Limiting para el Endpoint de la Grilla del Workdesk (CA-30)
    # Origen: Pregunta #30 del Refinamiento Funcional
    # Resuelve: No existía límite de cuántas veces un usuario puede solicitar la grilla.
    Given el endpoint principal `GET /api/v1/workdesk/tasks` del CA-20
    Then el API Gateway impondrá un Rate Limiting de máximo 60 peticiones por minuto por usuario autenticado.
    And si se supera, retornará `HTTP 429 Too Many Requests` con el mensaje: "Has realizado demasiadas consultas. Espera unos segundos antes de intentarlo de nuevo."
    And este límite protege contra scripts automatizados que hagan polling agresivo para monitorear cambios, ya que la sincronización en tiempo real se resuelve vía WebSocket (CA-06, CA-13, CA-27), NO por polling al endpoint REST.

  Scenario: [REFINAMIENTO] Auto-Refresco Pasivo al Volver de Inactividad Prolongada (CA-31)
    # Origen: Pregunta #31 del Refinamiento Funcional
    # Resuelve: El KeepAlive (CA-12) podría mostrar datos obsoletos tras horas de inactividad.
    Given la preservación del Workdesk en RAM mediante KeepAlive (CA-12)
    When el usuario regresa al Workdesk después de haber estado en otra pestaña/vista por más de 5 minutos
    Then el Frontend ejecutará un refresco silencioso en segundo plano: emitirá una petición al endpoint de la grilla (CA-20) y actualizará los datos de la tabla SIN destruir el componente KeepAlive ni resetear los filtros del usuario.
    And durante el refresco (que típicamente dura <1 segundo), la grilla mostrará un indicador sutil de actualización (Ej: un shimmer sobre las filas existentes) para que el usuario sepa que los datos se están renovando.
    And si la petición de refresco falla (error de red), la grilla mantendrá los datos del KeepAlive con un Toast discreto: "No se pudo actualizar. Mostrando datos de la última sincronización."
    And el umbral de 5 minutos es consistente con el CA-25 (recálculo de semáforos al volver de inactividad).


```
**Trazabilidad UX:** Wireframes Pantalla 1 (Workdesk - Escritorio de Tareas).

---

### US-002: Reclamar una Tarea de Grupo (Claim Task)
**Como** Analista / Usuario de Negocio
**Quiero** poder "reclamar" (asignarme) una tarea que actualmente pertenece a la cola de todo mi grupo
**Para** evitar que otro compañero trabaje en el mismo caso de forma paralela y duplicar esfuerzos.

> [!IMPORTANT]
> **Dependencias Externas Críticas de la US-002:**
> - **US-001 (Workdesk / Pantalla 1):** La grilla del Workdesk es donde el operario visualiza las tareas de la Cola de Grupo y donde aparece el botón [Reclamar]. Los WebSockets de desaparición instantánea (US-001 CA-06, CA-13, CA-27) dependen de que la US-002 EMITA el evento WebSocket al momento del Commit de reclamo/liberación/despojo. Sin este disparo, las tareas reclamadas permanecen como "fantasmas visibles" en las pantallas de todos los compañeros.
> - **US-029 (Completar Tarea / Pantalla 2):** La ejecución del formulario y el patrón de borrador en LocalStorage están definidos en la US-029. La Amnesia Transaccional (CA-07) de la US-002 depende del esquema de persistencia temporal de la US-029 para saber exactamente qué purgar al liberar.
> - **US-036 (RBAC / Pantalla 14):** La validación perimetral del Despojo Forzoso (CA-08, CA-13) consume la jerarquía organizacional y la relación `team_id` administrada en la Pantalla 14. Sin RBAC, un supervisor de cualquier departamento podría despojar tareas de departamentos que no le corresponden.
> - **US-001 CA-28 (Prevención de Condición de Carrera):** El mecanismo de bloqueo atómico en base de datos para el modo "Atender Siguiente" (US-001 CA-28) es análogo al requerido por el CA-11 de US-002. Ambos necesitan `SELECT FOR UPDATE SKIP LOCKED` para garantizar exclusión mutua.

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
	
	
  # ==============================================================================
  # B. REMEDIACIONES POST-AUDITORÍA (Sprint Remediation Brief 2026-04-05)
  # Origen: docs/requirements/us002_functional_analysis.md
  # Tickets: REM-002-01 a REM-002-05
  # Propósito: Cerrar GAPs de implementación detectados por el workflow
  #            /analisisEntendimientoUs.md antes del inicio de desarrollo de US-002.
  # Estado: US-002 NO ha sido desarrollada aún. Estos CAs se inyectan ANTES
  #         de la construcción para blindar la implementación desde el origen.
  # ==============================================================================

  Scenario: [REMEDIACIÓN] Mecanismo Atómico de BD para Reclamo Simultáneo (CA-11)
    # Origen: REM-002-01 — GAP-1 del us002_functional_analysis.md
    # Resuelve: El CA-01 exige HTTP 409 pero no define el mecanismo que lo garantiza.
    Given la concurrencia de múltiples analistas reclamando la misma tarea (CA-01)
    Then el Backend OBLIGATORIAMENTE utilizará el comando nativo `TaskService.claim(taskId, userId)` de Camunda como primera opción, el cual internamente aplica exclusión atómica.
    And si la tarea NO es una tarea Camunda (Ej: tarea Kanban o Gantt sin motor BPMN), el Backend aplicará un bloqueo pesimista en PostgreSQL (`SELECT ... FOR UPDATE SKIP LOCKED`) antes de escribir el campo `assignee`, garantizando que solo un Thread de Java gane la escritura.
    And el Thread perdedor recibirá una excepción controlada que se traducirá en el `HTTP 409 Conflict` del CA-01 con el mensaje amable al Frontend.
    And para el Reclamo Masivo (CA-02), cada tarea del lote se procesará con el mismo mecanismo atómico individual. Si 3 de 10 ya fueron tomadas en la fracción de segundo, el response del Batch retornará: `{ claimed: 7, conflicts: [{ taskId: 'TK-101', reason: 'Already claimed by María' }, ...] }`.
    And este mecanismo es análogo al US-001 CA-28 (Prevención de Condición de Carrera en "Atender Siguiente") y DEBE reutilizar el mismo patrón de Repository.

  Scenario: [REMEDIACIÓN] Emisión Obligatoria de Evento WebSocket Post-Commit (CA-12)
    # Origen: REM-002-02 — GAP-2 del us002_functional_analysis.md
    # Resuelve: Tras reclamo/liberación, los compañeros no son notificados y ven tareas "fantasma".
    Given la transacción exitosa de Reclamo, Liberación, Auto-Unclaim o Despojo Forzoso en la Base de Datos
    Then el Backend DEBE emitir, en el MISMO instante del Commit de la transacción, un evento WebSocket hacia el canal de broadcast del grupo/tenant afectado.
    And el evento seguirá el vocabulario estandarizado del US-001 CA-27:
    And   - Al RECLAMAR (individual o masivo): emitir `{ action: 'REMOVE', taskId: 'TK-123' }` hacia el canal grupal, para que todos los compañeros vean desaparecer la tarea de su cola.
    And   - Al LIBERAR o AUTO-UNCLAIM: emitir `{ action: 'ADD', taskId: 'TK-123', payload: {...} }` hacia el canal grupal, para que todos los compañeros vean RE-APARECER la tarea en su cola.
    And   - Al DESPOJAR FORZOSAMENTE (CA-08): emitir `REMOVE` hacia la bandeja personal del operario despojado Y `ADD` hacia el canal grupal simultáneamente.
    And si el servidor WebSocket no está disponible al momento del Commit, el evento se encola en una Dead Letter Queue (RabbitMQ) para reintento automático, garantizando entrega eventual (At-Least-Once Delivery).
    And esta emisión es la pieza que CONECTA la US-002 con el mecanismo de desaparición instantánea de la US-001 (CA-06, CA-13).

  Scenario: [REMEDIACIÓN] Validación Perimetral Organizacional en Despojo Forzoso (CA-13)
    # Origen: REM-002-03 — GAP-3 del us002_functional_analysis.md
    # Resuelve: Un supervisor de un departamento puede despojar tareas de otro departamento.
    Given la ejecución del Forced Unclaim por un Supervisor (CA-08)
    Then el Backend OBLIGATORIAMENTE cruzará el `team_id` del Supervisor autenticado contra el `team_id` asignado a la Tarea (Silo Data Protection).
    And si el Supervisor NO pertenece al mismo equipo/departamento que administra la tarea, el Backend retornará `HTTP 403 Forbidden` con el mensaje: "No tiene permisos para gestionar tareas de este equipo."
    And solo el jefe directo dentro de la jerarquía organizacional del equipo podrá ejecutar el despojo, consumiendo la matriz de roles y jerarquía de la US-036 (Pantalla 14).
    And cada intento de despojo (exitoso o rechazado) quedará registrado como asiento inmutable en el Audit Log con: `{ supervisorId, targetUserId, taskId, teamId, action: 'FORCE_UNCLAIM', result: 'SUCCESS|DENIED', timestamp }`.
    And la Vista de Monitoreo del Supervisor (CA-08) solo mostrará las tareas de SU equipo, aplicando el filtro `team_id` desde la consulta SQL base (prevención IDOR nativa sin depender del Frontend).

  Scenario: [REMEDIACIÓN] Contrato API Estandarizado para Operaciones de Reclamo (CA-14)
    # Origen: REM-002-04 — GAP-4 del us002_functional_analysis.md
    Given la necesidad de alinear Frontend y Backend en las operaciones de posesión de tareas
    Then el Backend expondrá los siguientes endpoints documentados con OpenAPI/Swagger annotations:
    And `POST /api/v1/tasks/{taskId}/claim` — Reclamo individual. Body: vacío (el userId se obtiene del JWT). Response 200: `{ taskId, assignee, claimedAt }`. Response 409: `{ conflictUser, message }`.
    And `POST /api/v1/tasks/bulk-claim` — Reclamo masivo. Body: `{ taskIds: ['TK-1', 'TK-2', ...] }`. Response 200: `{ claimed: [...], conflicts: [...] }`. Límite máximo de 20 tareas por lote (Hard Limit).
    And `POST /api/v1/tasks/{taskId}/release` — Liberación. Body: `{ message?: string }` (mensaje opcional del CA-04, máximo 500 caracteres). Response 200: `{ taskId, releasedAt }`.
    And `POST /api/v1/tasks/{taskId}/force-unclaim` — Despojo forzoso (solo Supervisores). Body: `{ reason?: string }`. Response 200: `{ taskId, previousAssignee, forcedBy, timestamp }`. Response 403: si no pertenece al equipo (CA-13).
    And `GET /api/v1/tasks/{taskId}/audit-trail` — Historial de trazabilidad (CA-09). Response 200: `{ entries: [{ action, userId, userName, timestamp, reason? }] }`.
    And todos los endpoints aplicarán OBLIGATORIAMENTE el filtro `tenantId` del JWT y el bind ORM anti-SQLi (consistente con US-001 CA-14).

  Scenario: [REMEDIACIÓN] Definición del Ghost Job Timeout y Pre-Aviso al Operario (CA-15)
    # Origen: REM-002-05 — GAP-5 del us002_functional_analysis.md
    # Resuelve: El CA-06 no define umbral, criterio de inactividad ni pre-aviso.
    Given el Cron Job de detección de tareas abandonadas (CA-06)
    Then el mecanismo de Auto-Unclaim seguirá las siguientes reglas:
    And 1. **Definición de "inactividad":** Una tarea se considera inactiva si el operario asignado NO ha ejecutado NINGUNA acción registrable sobre ella (completar, guardar borrador, adjuntar archivo, o cambiar estado) durante el período configurado. El mero hecho de tener la tarea abierta en pantalla NO cuenta como actividad.
    And 2. **Umbral por defecto:** 4 horas laborales de inactividad. Este umbral será configurable por tenant desde la configuración administrativa (US-036 / Pantalla 14), con un rango válido de 1 hora a 24 horas.
    And 3. **Pre-aviso obligatorio:** Cuando la tarea alcance el 75% del umbral (Ej: a las 3 horas de un umbral de 4), el sistema enviará un Toast persistente al operario: "Tu tarea [TK-123] será devuelta a la cola grupal en 1 hora por inactividad. Realiza una acción para evitarlo."
    And 4. **Ejecución del Auto-Unclaim:** Al cumplirse el 100% del umbral, el Cron Job ejecutará el unclaim automático, activará la Amnesia Transaccional del CA-07 (purga de datos parciales), y emitirá los eventos WebSocket del CA-12 (ADD hacia la cola grupal, REMOVE de la bandeja personal del operario).
    And 5. **Registro de Auditoría:** Cada Auto-Unclaim quedará registrado en el historial de trazabilidad del CA-09 con el motivo: `{ action: 'AUTO_UNCLAIM', reason: 'Inactividad de X horas', previousAssignee, timestamp }`.
    And 6. **Frecuencia del Cron Job:** Se ejecutará cada 15 minutos para detectar tareas que superen el umbral. No se ejecutará fuera del horario laboral configurado del tenant.


  # ==============================================================================
  # C. REFINAMIENTO FUNCIONAL POST-CUESTIONARIO (2026-04-05)
  # Origen: Cuestionario de 45 preguntas del workflow /refinamientoFuncionalUs.md
  # Propósito: Cerrar huecos descubiertos durante el refinamiento de la US-002.
  # ==============================================================================

  Scenario: [REFINAMIENTO] Superficie de Lectura del Mensaje Interno al Liberar (CA-16)
    # Origen: Pregunta #7 del Refinamiento Funcional
    # Resuelve: El CA-04 define que se puede escribir un mensaje al liberar, pero no dónde se lee.
    Given la liberación de una tarea con Mensaje Interno adjunto (CA-04)
    Then el mensaje se almacenará como una "Nota Interna" adherida a la tarea en la Base de Datos.
    And cuando el siguiente operario abra la tarea (en Modo Solo Lectura CA-05 o tras Reclamar), verá un Banner informativo fijo en la parte superior del formulario: "📝 Nota del operario anterior: [contenido del mensaje] — [Nombre, hace X horas]".
    And la nota permanecerá visible hasta que el nuevo reclamante ejecute su primera acción registrable sobre la tarea (Ej: guardar borrador, completar, o adjuntar archivo).
    And si la tarea se libera y reclama múltiples veces, solo se mostrará la nota MÁS RECIENTE (no se acumulan).
    And la nota NO es un sistema de mensajería. No existe un buzón de notificaciones internas. Es una etiqueta adherida a la tarea, como un "post-it" físico.

  Scenario: [REFINAMIENTO] Limpieza de Archivos Adjuntos Transitorios al Liberar (CA-17)
    # Origen: Pregunta #8 del Refinamiento Funcional
    # Resuelve: Los archivos subidos al servidor antes de liberar quedan en un "limbo" sin dueño.
    Given la Amnesia Transaccional del CA-07 activada al liberar una tarea
    Then los archivos adjuntos subidos al almacenamiento del servidor durante la sesión del operario que libera serán marcados como "Adjuntos Transitorios" (estado `orphaned`).
    And un proceso de limpieza (Scheduled Job) eliminará los archivos con estado `orphaned` después de 24 horas, permitiendo una ventana de recuperación en caso de error operativo.
    And el siguiente operario que reclame la tarea NO verá los archivos transitorios del operario anterior. Solo verá los adjuntos que ya estaban confirmados en sesiones anteriores completadas (via US-029 "Completar Tarea").
    And si la tarea nunca es reclamada por otro operario, los archivos transitorios se eliminan igualmente después de 24 horas.
    And este mecanismo garantiza que la Amnesia Transaccional sea TOTAL: no solo se borran los datos del formulario (LocalStorage) sino también los archivos del servidor, evitando "basura digital" acumulada por sesiones fallidas.

  Scenario: [REFINAMIENTO] Actualización del Modo Solo Lectura ante Reclamo Externo (CA-18)
    # Origen: Pregunta #12 del Refinamiento Funcional
    # Resuelve: El explorador en modo lectura no se entera si otro reclama la tarea.
    Given un analista explorando una tarea en Modo Solo Lectura (CA-05) mientras otro compañero la reclama
    When el Backend emite el evento WebSocket `REMOVE` (CA-12) tras el Commit del reclamo
    Then el explorador recibirá el evento y el formulario mostrará un Banner de aviso superpuesto: "⚠️ Esta tarea fue reclamada por otro compañero y ya no está disponible."
    And el botón [Reclamar] dentro del formulario se deshabilitará visualmente (gris + candado).
    And el analista podrá continuar leyendo el formulario (no se cierra abruptamente) pero no podrá ejecutar ninguna acción sobre la tarea.
    And al cerrar el formulario, la tarea ya no aparecerá en la Cola del Equipo de la grilla (consistente con CA-12/CA-13 de US-001).

  Scenario: [REFINAMIENTO] Extensión de Tiempo ante Pre-Aviso de Auto-Unclaim (CA-19)
    # Origen: Pregunta #14 del Refinamiento Funcional
    # Resuelve: El timeout de inactividad castiga procesos complejos que requieren lectura prolongada.
    Given el Pre-Aviso persistente del CA-15 punto 3 ("Tu tarea será devuelta en 1 hora por inactividad")
    Then el Banner incluirá dos botones de acción:
    And 1. **[Necesito más tiempo]:** Reinicia el contador de inactividad por un ciclo completo (4 horas más, o el umbral configurado del tenant). Cada extensión queda registrada en el historial de trazabilidad del CA-09 con motivo: `{ action: 'TIMEOUT_EXTENDED', userId, taskId, timestamp }`.
    And 2. **[Guardar borrador]:** Ejecuta un guardado del borrador actual en LocalStorage (consistente con US-029), lo cual reinicia el contador automáticamente ya que constituye una acción registrable.
    And se permite un máximo de 2 extensiones consecutivas por tarea. Tras la segunda extensión, si el operario sigue inactivo, el Auto-Unclaim se ejecutará sin opción de postergación adicional.
    And el supervisor del equipo será notificado cuando un operario solicite extensiones, como señal de alerta temprana de posible atasco operativo.

  Scenario: [REFINAMIENTO] Motivos Enriquecidos en la Trazabilidad Forense (CA-20)
    # Origen: Pregunta #18 del Refinamiento Funcional
    # Resuelve: El historial de trazabilidad (CA-09) solo mostraba rotación sin motivos.
    Given el Pop-Up de Trazabilidad del CA-09
    Then cada entrada del historial incluirá un campo `action_type` legible por humanos con los siguientes valores posibles:
    And `CLAIMED` — "Reclamada voluntariamente" (CA-01/CA-02).
    And `RELEASED` — "Liberada por el operario" + mensaje interno si existe (CA-04).
    And `FORCE_UNCLAIMED` — "Despojada por supervisor: [Nombre del supervisor]" + motivo si existe (CA-08/CA-13).
    And `AUTO_UNCLAIMED` — "Liberada automáticamente por inactividad de [X] horas" (CA-06/CA-15).
    And `TIMEOUT_EXTENDED` — "Tiempo de inactividad extendido por el operario" (CA-19).
    And `BULK_CLAIMED` — "Reclamada como parte de un lote de [N] tareas" (CA-02).
    And el Pop-Up mostrará los eventos como un timeline vertical con íconos de color por tipo: 🟢 reclamos, 🔵 liberaciones, 🟠 despojos, 🔴 auto-unclaims, ⏰ extensiones.

  Scenario: [REFINAMIENTO] Rollback del Optimistic UI tras Fallo Persistente de Red (CA-21)
    # Origen: Pregunta #20 del Refinamiento Funcional
    # Resuelve: Si la red nunca vuelve, la "mentira visual" del CA-10 nunca se deshace.
    Given la activación del Optimistic UI durante un micro-corte de red (CA-10)
    Then el Frontend ejecutará la siguiente estrategia de reintentos:
    And 1. **Reintentos con backoff exponencial:** 3 intentos con intervalos de 2s, 4s, 8s (total: 14 segundos de espera máxima).
    And 2. **Durante los reintentos:** La tarea aparece en "Mi Bandeja" con un indicador visual sutil (ícono de sincronización giratorio ⟳) que comunica: "Confirmando con el servidor..."
    And 3. **Si los 3 reintentos fallan:** El Frontend ejecutará un rollback visual: retira la tarea de "Mi Bandeja", la devuelve a la "Cola del Equipo" en la grilla, y muestra un Modal informativo: "No pudimos confirmar tu reclamo porque la conexión con el servidor no se restableció. La tarea sigue disponible en la cola del equipo."
    And 4. **El rollback NUNCA ocurrirá silenciosamente.** El operario siempre será informado explícitamente del fracaso para que no crea que tiene una tarea que no le pertenece.
    And 5. Si la red se recupera DESPUÉS del rollback, el operario deberá reclamar la tarea manualmente de nuevo.

  Scenario: [REFINAMIENTO] Separación Visual entre Cola de Grupo y Bandeja Personal (CA-22)
    # Origen: Pregunta #39 del Refinamiento Funcional
    # Resuelve: No existía distinción visual entre las tareas del grupo y las tareas propias.
    Given la grilla unificada del Workdesk (US-001)
    Then la pantalla principal mostrará dos pestañas/tabs en la parte superior de la grilla:
    And **Tab 1: "Mi Bandeja ([N])"** — Muestra las tareas asignadas al operario autenticado. Los botones disponibles por fila: [Abrir], [Liberar]. El botón [Reclamar] NO aparece aquí.
    And **Tab 2: "Cola del Equipo ([M])"** — Muestra las tareas sin asignar del grupo/equipo. Los botones disponibles por fila: [Explorar] (CA-05), [Reclamar] (CA-01). Los checkboxes para Bulk Claim (CA-02) solo aparecen en esta tab.
    And los contadores ([N] y [M]) se actualizarán con cada petición a la grilla y con los eventos WebSocket (CA-12): un REMOVE en la Cola incrementa N y decrementa M; un ADD en la Cola decrementa N e incrementa M.
    And la Tab activa se preservará en el KeepAlive (consistente con US-001 CA-12).
    And el toggle de delegación del US-001 CA-04/CA-15 agrega una tercera tab temporal: "Bandeja de [Nombre del subalterno] ([P])".

  Scenario: [REFINAMIENTO] Agregación de Eventos WebSocket para Operaciones Masivas (CA-23)
    # Origen: Pregunta #45 del Refinamiento Funcional
    # Resuelve: El Bulk Claim de 20 tareas generaría 20 eventos individuales en ráfaga.
    Given la ejecución exitosa de un Bulk Claim (CA-02) de N tareas
    Then el Backend NO emitirá N eventos WebSocket individuales.
    And en su lugar, emitirá UN SOLO mensaje agregado con una acción de tipo batch del vocabulario WebSocket (US-001 CA-27): `{ action: 'BULK_REMOVE', taskIds: ['TK-1', 'TK-2', ..., 'TK-N'], claimedBy: 'userId' }`.
    And el Frontend de cada compañero conectado procesará el array y desvanecerá todas las tareas listadas con una animación escalonada (150ms de delay entre cada desvanecimiento) para evitar que 20 tarjetas desaparezcan simultáneamente de forma confusa.
    And para la operación inversa (si existiera una "liberación masiva" en V2), se usaría: `{ action: 'BULK_ADD', taskIds: [...], payload: [...] }`.
    And este patrón reduce el tráfico WebSocket de `N × Usuarios_Conectados` mensajes a `1 × Usuarios_Conectados`, logrando hasta un 95% de ahorro de red en operaciones de lote.

```
**Trazabilidad UX:** Wireframes Pantalla 1 (Botón: Asignarme Tarea / Claim).

---

### US-004: Iniciar un Proceso mediante Webhook (Plugin O365 Listener)
**Como** Sistema (APIM / MS Graph / Webhook)
**Quiero** inyectar un payload automatizado a un Endpoint público de la plataforma
**Para** instanciar un caso de negocio nuevo automáticamente sin intervención manual humana.

> [!IMPORTANT]
> **Dependencias Externas Críticas de la US-004:**
> - **US-001 (Workdesk / Pantalla 1):** Las tareas de Pre-Triaje generadas por el Webhook (CA-8, CA-9) deben consolidarse visualmente en la grilla unificada del Workdesk para que los operarios las detecten y atiendan. Sin esta integración, los correos entrantes aprobados quedarían invisibles en una bandeja aislada (Pantalla 16) que nadie consulta proactivamente.
> - **US-036 (RBAC & Portal Administrativo / Pantalla 14):** La Whitelist de dominios autorizados (CA-4, CA-12), el límite parametrizable de peso de adjuntos (CA-7) y el switch de seguridad HMAC/Bearer (CA-10) requieren formularios de configuración administrativa expuestos en la Pantalla 14. Sin RBAC, estos parámetros quedarían hardcodeados sin capacidad de gobierno por tenant.
> - **US-000 (Arquitectura Transversal):** Los mecanismos de Idempotencia (CA-1), bloqueo de auto-responders (CA-2) y auditoría de payloads fallidos (CA-3) consumirán los interceptores globales de ExceptionHandler y el estándar de respuesta de error `{ field, issue, translatedMessage }` definidos por la US-000.
> - **Infraestructura RabbitMQ (Bloqueante):** La resiliencia ante caída del motor BPMN (CA-6) y el fail-secure del escaneo Anti-Malware (CA-11) dependen de que la topología de RabbitMQ (Exchanges, DLQ, DLX) esté desplegada y operativa en Docker/Kubernetes. Sin esta infraestructura, los Webhooks se perderían irrecuperablemente durante las ventanas de indisponibilidad de Camunda o del escáner.


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

  # ==============================================================================
  # B. REMEDIACIONES POST-AUDITORÍA (Análisis Funcional 2026-04-17)
  # Origen: docs/requirements/us004_functional_analysis.md
  # Tickets: REM-004-01 a REM-004-02
  # Propósito: Cerrar GAPs de definición detectados por el workflow
  #            /analisisEntendimientoUs.md antes del inicio de desarrollo de US-004.
  # Estado: US-004 NO ha sido desarrollada aún. Estos CAs se inyectan ANTES
  #         de la construcción para blindar la implementación desde el origen.
  # ==============================================================================

  Scenario: [REMEDIACIÓN] Sanitización Anti-Malware de Adjuntos en Webhook (CA-11)
    # Origen: REM-004-01 — GAP-1 del us004_functional_analysis.md
    # Resuelve: El CA-7 limita el peso pero no define escaneo de contenido malicioso.
    Given un payload de Webhook con archivos adjuntos que superan la validación de peso del CA-7 (≤ 10MB)
    Then el Backend OBLIGATORIAMENTE someterá cada archivo adjunto a un escaneo Anti-Malware antes de persistirlo en almacenamiento.
    And 1. En V1, el escaneo se realizará mediante una librería de detección de firmas integrada (Ej: ClamAV vía API REST o contenedor sidecar) invocada sincrónicamente antes del guardado.
    And 2. Si el escaneo detecta un archivo sospechoso o infectado, el sistema retornará `HTTP 422 Unprocessable Entity` con el mensaje: `{ "error": "MALWARE_DETECTED", "file": "<nombre>", "message": "El archivo adjunto fue rechazado por políticas de seguridad." }`.
    And 3. El archivo rechazado NO se persistirá en almacenamiento productivo. Se registrará únicamente su hash SHA-256 y metadatos (nombre, tamaño, remitente, timestamp) en la tabla de "Payloads Huérfanos/Fallidos" del CA-3 con el motivo `MALWARE_QUARANTINE`.
    And 4. Si el servicio de escaneo Anti-Malware no está disponible (caída del sidecar), el sistema aplicará un Fail-Secure: rechazará el archivo retornando `HTTP 503 Service Unavailable` y encolará el payload completo en la DLQ de RabbitMQ (CA-6) para reintento posterior cuando el escáner se recupere.
    And 5. Los archivos que pasen el escaneo exitosamente se almacenarán con un flag `scan_status: CLEAN` y el hash SHA-256 para verificación futura.

  Scenario: [REMEDIACIÓN] Administración de Whitelist de Dominios Autorizados (CA-12)
    # Origen: REM-004-02 — GAP-2 del us004_functional_analysis.md
    # Resuelve: El CA-4 valida dominios pero no define dónde ni cómo se administra la lista.
    Given la necesidad de gestionar la lista de dominios autorizados para el Webhook de ingesta (CA-4)
    Then la administración de la Whitelist seguirá las siguientes reglas:
    And 1. La Whitelist se almacenará como una tabla relacional `ibpms_webhook_allowed_domains` con los campos: `{ id, domain, tenant_id, description, created_by, created_at, is_active }`.
    And 2. La interfaz administrativa para gestionar la Whitelist se expondrá en la Pantalla 14 (US-036 — Portal Administrativo), dentro de una sección dedicada "Integraciones > Dominios Autorizados".
    And 3. Los endpoints CRUD serán:
    And   - `GET /api/v1/admin/webhook/allowed-domains` — Listar dominios (paginado, filtrable).
    And   - `POST /api/v1/admin/webhook/allowed-domains` — Agregar dominio. Body: `{ domain: "@ejemplo.com", description?: "Cliente XYZ" }`. Validación: formato de dominio válido, no duplicado por tenant.
    And   - `DELETE /api/v1/admin/webhook/allowed-domains/{id}` — Desactivar dominio (soft-delete con `is_active = false`). No se permite hard-delete para preservar trazabilidad.
    And 4. Solo usuarios con rol `ADMIN_SISTEMA` o `ADMIN_TENANT` podrán gestionar la Whitelist. Cualquier otro rol recibirá `HTTP 403 Forbidden`.
    And 5. Cada modificación (alta, baja, reactivación) quedará registrada en el Audit Log con: `{ userId, action: 'WHITELIST_ADD|WHITELIST_REMOVE|WHITELIST_REACTIVATE', domain, timestamp }`.
    And 6. El Backend aplicará caché en memoria (Redis, TTL: 5 minutos) sobre la Whitelist para no consultar la BD en cada Webhook entrante, garantizando performance sub-milisegundo en la validación del CA-4.

  # ==============================================================================
  # C. REFINAMIENTO DE VALOR DE NEGOCIO (Análisis 45 Preguntas - 2026-04-17)
  # Resoluciones estratégicas de producto para la ingesta y manipulación humana
  # ==============================================================================

  Scenario: Purga Automática de Correos Rechazados (Registro de Basura) (CA-13)
    Given que el sistema registra internamente los correos rechazados (malformados, no autorizados o con virus)
    When estos registros cumplen 30 días de haber sido recibidos
    Then el sistema de manera automática depura (purga) físicamente estos registros
    And garantizando no saturar la base de datos de auditoría y evitar la retención eterna de datos innecesarios.

  Scenario: Experiencia de Pre-visión y Rechazo en el Triaje (CA-14)
    Given que el operador humano se dispone a evaluar una Tarea de Pre-Triaje
    When abra el registro entrante
    Then el sistema debe mostrar una pre-visualización clara del cuerpo del mensaje original y sus anexos
    And debe proveer al operador los botones [Aprobar Ingesta] y [Rechazar Petición]
    And obligando a que si oprime Rechazar, tenga que ingresar obligatoriamente una razón o motivo breve, cambiando el estado final a Cancelado.

  Scenario: Canalización del Trámite Específico (CA-15)
    Given un operador humano que confirma en la interfaz que el correo es válido y debe procesarse
    When oprime el botón de [Aprobar Ingesta]
    Then el sistema obliga al operador a seleccionar desde un menú desplegable el "Tipo de Proceso" que debe iniciar (Ej: "Queja", "Alta de Cliente")
    And solo al seleccionarlo, el motor arranca el viaje real y oficial dentro de Camunda.

  Scenario: Reloj de Atención al Ciudadano/Cliente en Cola (CA-16)
    Given que el buzón acaba de recibir y legitimar un correo
    Then el sistema asigna a la tarea de "Pre-Triaje" un reloj propio (SLA de Entrada) paramétrico
    And si el tiempo transcurre sin que un operador clasifique o apruebe el requerimiento, la tarea se pondrá en semáforo Amarillo y Rojo, visibilizándola como una emergencia de atención en el tablero general.

  Scenario: Recepción Acusada de Inmediato (Experiencia Diferida) (CA-17)
    Given que Microsoft u otro buzonazo envía un correo con anexos al iBPMS
    When la petición es recibida
    Then el motor responde Inmediatamente (Sub-segundo) "Mensaje Recibido" al emisor externo para evitar cuellos de botella
    And las tareas de procesamiento exigentes (como el escaneo Anti-virus) ocurren "en el patio trasero" (asíncrono) sin dejar la puerta principal bloqueada.

```
**Trazabilidad UX:** Pantalla 11 (Hub de Integraciones: Eventos Entrantes) y Pantalla 16 (Bandeja Inteligente de Intake).

---

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

> [!IMPORTANT]
> **Dependencias Externas Críticas de la US-030:**
> - **US-008 (Mover Tarjeta Kanban / Pantalla 3):** Las tarjetas creadas en el Hub (P10) pasan a vivir operativamente en la P3. Sin el motor de estados y WebSockets de la US-008, las tarjetas no podrían ejecutarse. La política 1:1 de la US-008 CA-4 rige en la ejecución; la multi-asignación del CA-5 de esta US-030 solo aplica en planificación.
> - **US-006 (Plantillero Transversal / WBS):** El CA-2 de esta US-030 depende de la existencia de Plantillas WBS creadas y administradas por la US-006. Sin plantillas, el Líder solo puede crear proyectos vacíos.
> - **US-001 (Workdesk / Pantalla 1):** Las tarjetas asignadas nominalmente (CA-5) deben aparecer en la bandeja unificada del Workdesk del operario asignado para que las detecte y trabaje.
> - **US-000 (Arquitectura Transversal):** Los interceptores de error, la auditoría centralizada y la sanitización XSS del CA-11 consumen las capas base definidas por la US-000.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Agile Project Instantiation and Planning
  Scenario: Instanciación sin Sprints en V1 (Postergación Táctica) (CA-1)
    Given un proyecto instanciado bajo metodología Ágil en la Pantalla 9
    When el líder de proyecto abre el Agile Hub (Pantalla 10)
    Then el sistema NO utiliza iteraciones con fechas (Sprints) para la Versión 1 del producto
    And el lienzo funciona como un Tablero General de Kanban Continuo (Flujo sin Timebox) donde las tareas se mapean directamente de ToDo a Done, aplazando el marco Scrum complejo para V2.

  # ==============================================================================
  # B. REFINAMIENTO FUNCIONAL INTEGRAL (45 Preguntas Respondidas - 2026-04-17)
  # Origen: docs/requirements/us030_refinamiento_funcional.md
  # Propósito: Construir los pilares CRUD, UX y de seguridad completamente
  #            ausentes de la definición original de esta User Story.
  # ==============================================================================

  Scenario: Arranque Selectivo — Backlog en Blanco vs Plantilla WBS Clonada (CA-2)
    Given que el Líder de Proyecto confirma la creación de un nuevo Proyecto Ágil en la Pantalla 9
    Then el sistema presenta un Pop-Up de Selección Inicial con dos alternativas:
    And 1. **"Iniciar vacío":** El Hub Ágil (Pantalla 10) se crea sin tareas, listo para recibir inyección manual.
    And 2. **"Usar una Plantilla WBS":** Se despliega un listado de Plantillas previamente creadas (US-006). El Líder selecciona una y el sistema clona físicamente todas las tareas del WBS como registros independientes en estado "Para Hacer" (TO DO).
    And el clonaje es una copia profunda: las tareas clonadas son entidades autónomas desconectadas de la plantilla padre. Cambios futuros en la plantilla NO afectan a los proyectos ya instanciados.
    And si la plantilla contiene más de 50 tareas, el clonaje se realiza en segundo plano mostrando un indicador de progreso visual (esqueleto animado).

  Scenario: Inyección Manual y Gobierno CRUD de Tarjetas Ágiles (CA-3)
    Given el Hub Ágil (Pantalla 10) de un proyecto activo
    Then el Líder de Proyecto o Scrum Master dispondrá de un botón "+ Nueva Tarea" prominente en la barra superior para inyectar tarjetas en cualquier momento.
    And al presionarlo, se desliza un panel lateral (Canvas/Slide-Panel) con los siguientes campos:
    And - **Título** (obligatorio, texto corto)
    And - **Descripción** (obligatorio, editor de texto enriquecido con soporte para pegar imágenes desde el portapapeles Ctrl+V, sanitizado contra inyección de scripts maliciosos)
    And - **Esfuerzo Estimado** (opcional, numérico en horas)
    And - **Esfuerzo Real** (opcional, numérico en horas, actualizable durante la ejecución)
    And - **Responsable(s)** (opcional, uno o varios usuarios del proyecto via combo-box multi-select)
    And - **Etiqueta/Tag** (opcional, seleccionable con colores ad-hoc creados por el mismo usuario)
    And - **Notas Adicionales** (opcional, campo de texto libre)
    And las tarjetas son visibles en la Pantalla 3 (Tablero Kanban operativo) inmediatamente después de guardarse.
    And el Líder puede editar el Título y la Descripción de cualquier tarea en cualquier momento, incluso si ya está en progreso por otro usuario.

  Scenario: Eliminación Física con Auditoría Forense Obligatoria (CA-4)
    Given una tarjeta existente en el Hub Ágil que el Líder desea destruir
    When presiona el botón de eliminación sobre la tarjeta
    Then el sistema solicita confirmación mediante un diálogo simple de aceptación
    And al confirmar, la tarjeta se elimina FÍSICAMENTE de la base de datos (Hard-Delete)
    And ANTES del borrado, el sistema graba un registro inmutable en la tabla de Auditoría con: ID de la tarea eliminada, Título, usuario que ejecutó la eliminación, fecha y hora exacta.

  Scenario: Asignación Flexible Multi-Persona desde el Hub de Planificación (CA-5)
    Given una tarjeta en el Hub Ágil (Pantalla 10) que requiere asignación de responsables
    Then el Líder podrá asignar uno o varios usuarios del proyecto como responsables en cualquier momento del ciclo de vida de la tarea (antes, durante o después de iniciarla).
    And podrá cambiar, agregar o remover responsables libremente.
    And **Reconciliación con US-008 CA-4 (Single-Assignee):** En el Hub de PLANIFICACIÓN (P10) se permite designar múltiples responsables para visibilidad y reparto de trabajo. Cuando la tarea se ejecuta en el Tablero OPERATIVO (P3), la política 1:1 rige: solo un usuario a la vez puede reclamar y trabajar activamente la tarjeta, garantizando un único dueño del SLA.
    And la lista desplegable de candidatos se filtra exclusivamente a los miembros activos y registrados en el proyecto actual. Usuarios fuera del proyecto no aparecen.

  Scenario: Priorización Visual por Arrastre Vertical (Drag & Drop) (CA-6)
    Given la lista de tareas desplegada en el Hub Ágil estilo Backlog
    Then la prioridad de cada tarea estará determinada por su posición vertical en la lista: las de arriba son las más urgentes, las de abajo las menos prioritarias.
    And el Líder podrá arrastrar las tarjetas hacia arriba o abajo para reordenarlas.
    And este orden se persiste en la base de datos como un campo de posición numérica y se refleja consistentemente en todas las vistas del proyecto.

  Scenario: Doble Vista Consolidada — Proyecto Individual vs Portafolio (CA-7)
    Given la necesidad de gestionar múltiples proyectos Ágiles simultáneamente
    Then la Pantalla 10 ofrecerá un selector de vista en la barra superior con dos modos:
    And 1. **"Vista Proyecto":** Muestra exclusivamente las tarjetas del proyecto seleccionado (comportamiento por defecto).
    And 2. **"Vista Portafolio":** Consolida las tarjetas de todos los proyectos Ágiles activos bajo la jurisdicción del Líder, agrupadas por proyecto, permitiendo una visión gerencial unificada.

  Scenario: Archivo Inteligente de Tareas Completadas (CA-8)
    Given una tarjeta que ha llegado al estado "DONE" (Completada) en el tablero operativo (Pantalla 3, US-008)
    Then la tarjeta se ocultará automáticamente del backlog visible en la Pantalla 10 para no saturar la vista del Líder.
    And existirá un toggle o filtro "Mostrar Completadas" que al activarse revelará el histórico de tareas terminadas como una sección plegable al final de la lista.

  Scenario: Modificación de SLA Individual con Bitácora de Cambios (CA-9)
    Given una tarjeta activa cuyo plazo de entrega original resulta insuficiente
    When el Líder modifica el tiempo límite (SLA) desde el panel de detalle de la tarjeta
    Then el sistema acepta la modificación y la aplica inmediatamente al reloj de la tarea.
    And TODA modificación de SLA queda registrada en un log visible con: valor anterior, valor nuevo, quién lo cambió y cuándo, garantizando trazabilidad ante auditorías de gestión.

  Scenario: Cierre de Proyecto con Cascada Controlada de Cancelación (CA-10)
    Given un Líder que decide dar de baja un Proyecto Ágil completo
    When confirma la acción de "Terminar Proyecto" desde la Pantalla 9
    Then todas las tarjetas que no hayan alcanzado el estado "DONE" se marcan automáticamente como "CANCELADA"
    And desaparecen de las bandejas de trabajo (Workdesk/Kanban) de los operarios asignados
    And el proyecto pasa a estado "Cerrado" y queda en modo solo-lectura para consulta histórica.

  Scenario: [SEGURIDAD] Blindaje de Acceso y Protección contra Abuso (CA-11)
    Given la exposición de los endpoints del Hub Ágil
    Then solo los usuarios con rol de Scrum Master o Líder del proyecto específico podrán crear, editar y eliminar tarjetas. Los operarios regulares tendrán acceso de solo lectura al Hub.
    And todo campo de texto enriquecido (Descripción, Notas) será sanitizado contra inyección de scripts (XSS) antes de persistirse.
    And las operaciones masivas (crear múltiples tareas de golpe) estarán limitadas a un máximo de 50 tareas por petición para prevenir saturación intencional.
    And existirá un límite rígido de 500 tarjetas activas por proyecto en V1. Al alcanzarlo, el sistema impedirá crear nuevas tarjetas hasta que se archiven o eliminen las existentes.
    And si un usuario asignado como responsable es desactivado del sistema, sus tarjetas se marcarán con alerta visual "Sin Propietario" para reasignación inmediata por el Líder.

  Scenario: [UX] Anatomía Visual del Backlog Moderno (CA-12)
    Given la renderización de la Pantalla 10 (Hub Ágil)
    Then el backlog se presenta en formato de lista vertical estilo Jira/Linear (no como tablero de columnas)
    And cada fila de tarjeta muestra: Título, nombre del/los Responsable(s), Etiqueta con color, y Estado actual.
    And la lista implementa scroll infinito con virtualización del navegador para soportar cientos de tarjetas sin degradar la experiencia visual.
    And existirá un panel de búsqueda rápida con filtros por Estado, por Asignado y por Etiqueta en la barra superior.
    And las etiquetas/tags son de colores personalizados (ad-hoc) que el propio usuario puede crear y bautizar libremente.
    And en la esquina superior derecha existirá un acceso directo **"Saltar al Tablero →"** para navegar instantáneamente a la Pantalla 3 (Kanban operativo) del mismo proyecto.

  Scenario: [UX] Detección Visual de Tareas Abandonadas — Ticket Rancio (CA-13)
    Given una tarjeta en el backlog que lleva más de 15 días naturales sin actividad alguna (sin cambios de estado, sin ediciones, sin registro de esfuerzo)
    Then el sistema aplicará automáticamente un indicador visual de abandono compuesto por:
    And 1. Un borde lateral izquierdo en color ámbar/naranja visible en la fila de la tarjeta.
    And 2. Un badge con ícono de reloj y el texto "🕐 Inactivo X días" en color ámbar debajo del título.
    And 3. Una tonalidad de fondo sutilmente más cálida que la de las tarjetas activas.
    And este indicador desaparecerá automáticamente cuando la tarjeta registre cualquier tipo de actividad (edición, movimiento de estado, registro de esfuerzo).

  Scenario: [RENDIMIENTO] Carga Liviana, Reactividad Cruzada y Operaciones Masivas (CA-14)
    Given la apertura inicial del Hub Ágil con potencialmente cientos de tarjetas
    Then los datos de la lista principal traerán únicamente campos ligeros (ID, Título, Estado, Asignado, Etiqueta). La Descripción completa, Notas y demás campos pesados solo se cargarán cuando el Líder haga clic en una tarjeta individual.
    And si un operario mueve una tarjeta en la Pantalla 3 (Kanban), el cambio de estado se reflejará en tiempo real en la Pantalla 10 si el Líder la tiene abierta simultáneamente.
    And para asignaciones masivas (Ej: asignar 30 tareas a "María" de una vez), existirá un mecanismo de selección múltiple (checkbox) y acción agrupada que se procesará como una sola operación consolidada, no 30 peticiones individuales.

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


### US-017: Ejecución y Persistencia Inmutable de Formularios (CQRS & Event Sourcing)
**Como** Analista / Motor Backend Hexagonal
**Quiero** diligenciar la información de mi tarea, almacenando las subidas temporales (Drafts) y transacciones finales de forma inmutable
**Para** garantizar cero bloqueos concurrentes, trazabilidad absoluta y finalizar exitosamente mi actividad sin contaminar el motor de Camunda (separando lectura de escritura).

> [!IMPORTANT]
> **Dependencias Externas Críticas de la US-017:**
> - **US-029 (Pantalla 2 / Frontend UX):** ⚠️ HISTORIA GEMELA. Comparten el endpoint `POST /api/v1/workbox/tasks/{id}/complete`. La US-029 gobierna la experiencia del Frontend (UI, validación Zod en navegador, Upload-First UX, LocalStorage, feedback visual, Wizard). La US-017 gobierna la persistencia del Backend (CQRS, Event Sourcing, protección de Camunda, Rollback Saga, validación Backend). La reconciliación se formaliza en el CA-19 de la US-029 (Política de Propiedad Exclusiva).
> - **US-003 (Catálogo de Formularios / Pantalla 7):** 🔴 BLOQUEANTE. Los esquemas Zod que el Backend valida mediante `json-schema-validator` (transpilados en CI/CD) se generan en la US-003. Sin esquemas, la validación Backend es imposible.
> - **US-002 (Reclamar Tarea / Pantalla 1):** 🔴 BLOQUEANTE. Sin reclamo, la tarea no tiene `assignee` y los CAs de Implicit Locking de la US-029 (CA-07/CA-18) rechazarán todo intento de completar con HTTP 403. El CA-04 de esta US-017 define una excepción controlada para tareas de grupo.
> - **US-035 (SharePoint/SGDEA):** ⚠️ FUERTE. La bóveda documental temporal que almacena archivos pre-submit (Upload-First) es un servicio externo que la US-017 debe vincular transaccionalmente a los eventos CQRS.
> - **US-036 (RBAC / Pantalla 14):** ⚠️ FUERTE. La validación Backend Zero-Trust necesita la matriz de roles para resolver el strip silencioso de campos no autorizados (delegado desde US-029 CA-15 Zod Isomórfico).
> - **US-034 (RabbitMQ):** 🟡 DESEABLE. El Worker asíncrono de proyección del CA-01 puede utilizar colas de mensajería para procesar eventos de forma resiliente. Si RabbitMQ no está disponible, el Worker operará in-process como fallback.
> - **US-009 (BAM Dashboard / Pantalla 5):** 🟡 CONSUMIDOR. Los dashboards de analítica consumen las tablas proyectadas por el Worker del CA-01. Sin la proyección, los dashboards no tienen datos actualizados.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Hexagonal CQRS Persistence, Zero-Trust Validation and Task Completion

  # ==============================================================================
  # A. ARQUITECTURA CQRS, EVENT SOURCING Y PROTECCIÓN DEL MOTOR
  # ==============================================================================
  Scenario: Separación de Responsabilidades y Event Sourcing (CQRS) (CA-01)
    Given un JSON perfectamente validado resultante del "iForm Maestro"
    When el analista pulsa [Enviar Final] realizando POST a `/api/v1/workbox/tasks/{id}/complete`
    Then el Backend separará el flujo arquitectónico: inyectará el Comando (`Form_Submitted_Event`) en la tabla inmutable de Eventos garantizando el historial forense exacto
    And un Worker asíncrono proyectará (`Projection`) esos datos a la tabla relacional aplanada para habilitar lecturas hiperveloces desde los Dashboards y Analítica.

  Scenario: Exclusión Topológica Estratégica de Camunda Engine (CA-02)
    Given el cierre exitoso de la transacción CQRS (Guardado del Evento Inmutable validado en Postgres)
    When el Backend notifica a Camunda 7 para avanzar el Token BPMN (`taskService.complete()`)
    Then el Backend TIENE ESTRICTAMENTE PROHIBIDO empujar el Payload masivo de negocio (Textos largos, JSONs complejos) hacia la tabla `ACT_RU_VARIABLE` del Engine
    And a Camunda solo se le enviará un DTO minificado (Ej: `{ "aprobado": true, "form_storage_id": "ABC-123" }`) con las variables lógicas estrictamente requeridas por los Gateways de enrutamiento.

  Scenario: Consistencia Transaccional Cruda (ACID Fallback over Sagas) (CA-03)
    Given el Payload aplanado y guardado exitosamente en CQRS
    When el motor orquestador (Camunda 7) sufre un Crash o Timeout HTTP 5xx en su API REST interna al intentar avanzar la tarea
    Then el Backend iBPMS abortará inmediatamente la transacción base ejecutando un Rollback Compensatorio (Patrón Saga inverso) sobre la persistencia en PostgreSQL
    And devolverá un error HTTP 500 Crudo ("Motor No Disponible") a la UI en Pantalla 2
    And se prohíbe a nivel arquitectónico generar falsos positivos HTTP 202 ("Guardado para después") para eludir el colapso del proceso judicial de fondo, unificando la verdad visual con el estado real del Motor.

  # ==============================================================================
  # B. REASIGNACIONES, COLISIONES GROUP-LEVEL Y TRAZABILIDAD
  # ==============================================================================
  Scenario: Auto-Claim Controlado para Tareas de Grupo No Asignadas (CA-04)
    # NOTA: Este CA resuelve el GAP-4 del us017_functional_analysis.md.
    # El Auto-Claim aplica EXCLUSIVAMENTE a tareas de grupo sin assignee.
    # Para tareas con assignee, rige el Implicit Locking de US-029 CA-07/CA-18.
    Given que una tarea "TK-500" está disponible en un grupo de trabajo (Ej: "Abogados") pero NO tiene un `assignee` directo asignado en Camunda
    When un usuario legitimado bajo la taxonomía RBAC del grupo abre la tarea e intenta presionar [Enviar] (`/complete`)
    Then el Backend evaluará la siguiente lógica ANTES de procesar el POST:
    And 1. **Si la tarea YA tiene `assignee`:** Se aplica el Implicit Locking normal (US-029 CA-07/CA-18). Solo el `assignee` registrado puede completar. Cualquier otro usuario recibe HTTP 403.
    And 2. **Si la tarea NO tiene `assignee` (tarea de grupo):** El Backend ejecutará transaccionalmente un `taskService.claim(taskId, userId)` asignando silenciosamente la tarea al operario ANTES de empujar el `FORM_SUBMITTED_EVENT` al Event Store.
    And 3. **El Auto-Claim genera un evento CQRS propio:** Se grabará un evento `TASK_AUTO_CLAIMED` en la tabla de eventos (CA-06) con el `userId`, `taskId` y `timestamp`, inmediatamente seguido del `FORM_SUBMITTED_EVENT`. La trazabilidad será completa.
    And 4. **Protección contra Race Condition:** Si dos operarios intentan completar la misma tarea de grupo simultáneamente, el Backend usará un lock optimista en Camunda (`OptimisticLockingException`). El PRIMERO en llegar gana el Claim + Submit. El SEGUNDO recibirá HTTP 409 Conflict con mensaje: "Esta tarea ya fue reclamada y completada por otro operario."
    And 5. **Consistencia con US-002:** El Auto-Claim de este CA NO reemplaza el flujo de Reclamar Tarea de la US-002. El operario PUEDE reclamar explícitamente desde el Tab "Disponibles" (US-002 CA-01) para reservar la tarea ANTES de abrirla. El Auto-Claim solo se activa si el operario abrió la tarea SIN reclamarla previamente y decide enviarla directamente.

  Scenario: Trazabilidad Activa de Rechazos Históricos en BFF (CA-05)
    Given una tarea devuelta a un especialista por un analista de control de calidad desde una fase superior (Rechazo Ope/BPMN)
    When el especialista abre el iFormulario para enmendar su trabajo documentado
    Then el Frontend (a través del llamado unificado `/form-context`) no solo recibirá el `prefillData` histórico
    And también recibirá inyectado OBLIGATORIAMENTE un array (Ej: `rejectionLogs`) con el dictamen exacto, responsable y fecha del rechazo
    And mostrando esta causal de devolución como un Alert inyectado en el Canvas central del formulario (Solo Lectura), previniendo que el usuario repita una reparación a ciegas guiado solo por la telepatía.


  # ==============================================================================
  # C. REMEDIACIONES POST-AUDITORÍA (Sprint Remediation Brief 2026-04-05)
  # Origen: docs/requirements/us017_functional_analysis.md
  # Tickets: REM-017-01 a REM-017-03
  # Propósito: Cerrar GAPs 1, 3 y 5 detectados por el workflow
  #            /analisisEntendimientoUs.md antes del inicio de desarrollo.
  # Estado: US-017 NO ha sido desarrollada aún.
  # ==============================================================================

  Scenario: [REMEDIACIÓN] Definición del Esquema del Event Store (CA-06)
    # Origen: REM-017-01 — GAP-5 del us017_functional_analysis.md
    # Resuelve: El CA-01 menciona "tabla inmutable de Eventos" pero no define nombre, columnas ni tipos de evento.
    Given la necesidad de almacenar eventos inmutables con trazabilidad forense completa (CA-01)
    Then el Event Store se implementará en la tabla `form_event_store` de PostgreSQL con el siguiente esquema mínimo obligatorio:
    And 1. **`event_id`** (UUID, PK): Identificador único e inmutable del evento.
    And 2. **`event_type`** (VARCHAR, NOT NULL): Tipo del evento. Valores admitidos en V1: `FORM_SUBMITTED`, `FORM_DRAFT_SAVED`, `TASK_AUTO_CLAIMED`, `FORM_REJECTED`.
    And 3. **`task_id`** (VARCHAR, NOT NULL, INDEX): Identificador de la tarea de Camunda asociada.
    And 4. **`process_instance_id`** (VARCHAR, NOT NULL, INDEX): Identificador de la instancia del proceso BPMN.
    And 5. **`user_id`** (VARCHAR, NOT NULL): Identificador del operario que generó el evento (extraído del SecurityContext JWT).
    And 6. **`payload_json`** (JSONB, NOT NULL): Contenido íntegro del formulario enviado, almacenado como JSON binario para consultas analíticas.
    And 7. **`schema_version`** (VARCHAR, NOT NULL): Versión del esquema Zod/JSON Schema con la que se validó el payload (Ej: `V3`). Consistente con el `schema_version` del Mega-DTO BFF de la US-029.
    And 8. **`created_at`** (TIMESTAMP WITH TIME ZONE, NOT NULL, DEFAULT NOW()): Momento exacto de grabación del evento. Usado para ordenamiento cronológico.
    And 9. **`idempotency_key`** (UUID, UNIQUE): Llave de idempotencia recibida del Frontend (US-029 CA-12) para prevenir grabación duplicada de eventos.
    And la tabla TIENE ESTRICTAMENTE PROHIBIDO ejecutar operaciones `UPDATE` o `DELETE`. Los registros son inmutables (append-only).
    And el Worker de proyección asíncrona (CA-01) será un componente in-process (mismo JVM) que consumirá los eventos mediante un polling periódico (cada 500ms) o mediante un `@TransactionalEventListener` de Spring. Si US-034 (RabbitMQ) está disponible, el Worker podrá migrar a consumo por cola como mejora de rendimiento.
    And las tablas de proyección analítica (Query Side) se definirán como vistas materializadas o tablas aplanadas según las necesidades específicas de US-009 (BAM Dashboard).

  Scenario: [REMEDIACIÓN] Endpoint de Lectura y Limpieza de Borradores del Servidor (CA-07)
    # Origen: REM-017-02 — GAP-3 del us017_functional_analysis.md
    # Resuelve: El autoguardado al servidor (US-029 CA-24 PUT /draft) no tiene contrapartida GET para recuperar borradores.
    Given la necesidad de que el Frontend pueda recuperar borradores almacenados en el servidor (US-029 CA-26 fallback)
    Then la US-017, como fuente autoritativa de la persistencia, expondrá los siguientes endpoints de borradores:
    And 1. **`GET /api/v1/workbox/tasks/{taskId}/draft`** — Recupera el borrador más reciente del servidor para la tarea indicada. Response: HTTP 200 con `{ currentStep?: number, partialData: {...}, schemaVersion: string, updatedAt: timestamp }`. Si no existe borrador, retorna HTTP 404.
    And 2. **`PUT /api/v1/workbox/tasks/{taskId}/draft`** — Guarda o actualiza el borrador (ya definido en US-029 CA-24). Body: `{ currentStep?: number, partialData: {...}, schemaVersion: string }`. Response: HTTP 204 No Content.
    And 3. **`DELETE /api/v1/workbox/tasks/{taskId}/draft`** — Elimina el borrador tras submit exitoso. Se invoca automáticamente como parte del flujo de `FORM_SUBMITTED_EVENT`. Response: HTTP 204.
    And **Seguridad:** Los tres endpoints aplican Implicit Locking — solo el `assignee` actual (o un usuario con Auto-Claim válido del CA-04) puede operar sobre borradores de su tarea. Intentos con otro userId retornan HTTP 403.
    And **Almacenamiento:** Los borradores se persisten en la tabla `task_drafts` (consistente con US-029 CA-24) con TTL de 72 horas. Un Cron Job diario eliminará borradores huérfanos con `updated_at` > 72h.
    And **Diferencia con Event Store:** Los borradores NO son eventos inmutables. Son snapshots efímeros de trabajo en progreso que se sobrescriben en cada Merge Commit y se destruyen tras el submit. NO aparecen en la tabla `form_event_store` del CA-06.

  Scenario: [REMEDIACIÓN] Referencia Cruzada con US-029 y Política de Propiedad (CA-08)
    # Origen: REM-017-03 — GAP-1 del us017_functional_analysis.md
    # Resuelve: Formaliza la reconciliación entre US-017 (Backend) y US-029 (Frontend) tras la eliminación de los 11 CAs duplicados.
    Given la eliminación de los CAs duplicados (antiguos CA-01 a CA-11 originales) que se solapaban con la US-029
    Then se establece la siguiente DIRECTIVA DE RECONCILIACIÓN entre las historias gemelas:
    And 1. **US-017 es la FUENTE AUTORITATIVA** para: persistencia CQRS/Event Sourcing (CA-01), exclusión topológica de Camunda (CA-02), Rollback Saga (CA-03), Auto-Claim transaccional (CA-04), trazabilidad de rechazos (CA-05), esquema del Event Store (CA-06), y endpoints de borradores del servidor (CA-07).
    And 2. **US-029 es la FUENTE AUTORITATIVA** para: experiencia de Frontend (Pantalla 2 UI), validación Zod en navegador, feedback visual (spinner/overlay/confirmación), autoguardado en LocalStorage, cifrado PII, Upload-First UX, Wizard, idempotencia en Frontend, pestañas duplicadas, campos condicionales, y campos de solo lectura.
    And 3. **Aspectos compartidos delegados:** Cuando un CA de la US-017 necesite describir un comportamiento de Frontend (Ej: "el Frontend muestra un error"), REFERENCIARÁ el CA correspondiente de la US-029 (Ej: "consistente con US-029 CA-20") sin redefinirlo. Aplica recíprocamente desde la US-029 hacia la US-017 según el CA-19 de la US-029.
    And 4. **Endpoint compartido:** `POST /api/v1/workbox/tasks/{id}/complete` es implementado UNA SOLA VEZ en el Backend. La US-017 define QUÉ hace el servidor al recibirlo. La US-029 define QUÉ envía el Frontend y QUÉ muestra antes/durante/después.
    And 5. **Merge Commit Rule:** Si un desarrollador necesita modificar un CA que toca AMBAS historias, debe generar un PR que referencie AMBAS US (Ej: "Implements US-017 CA-01 + US-029 CA-01") para garantizar revisión cruzada.


  # ==============================================================================
  # D. REFINAMIENTO FUNCIONAL POST-CUESTIONARIO (2026-04-05)
  # Origen: docs/requirements/us017_refinamiento_funcional.md (45 preguntas)
  # Tickets: REF-017-01 a REF-017-10
  # Propósito: Cerrar vacíos funcionales descubiertos durante el refinamiento
  #            profundo de 45 preguntas estratificadas.
  # ==============================================================================

  Scenario: [REFINAMIENTO] Exclusión de Borradores del Event Store (CA-09)
    # Origen: REF-017-01 — Pregunta #2 del refinamiento funcional
    # Resuelve: Evitar que el Event Store se sature con eventos triviales de autoguardado.
    Given la existencia de 4 tipos de eventos definidos en el CA-06 del Event Store
    Then el tipo de evento `FORM_DRAFT_SAVED` se ELIMINA de la lista de eventos admitidos en el Event Store (`form_event_store`)
    And los borradores (Drafts) viven EXCLUSIVAMENTE en la tabla `task_drafts` (CA-07), que NO es inmutable y se sobrescribe con cada Merge Commit
    And los tipos de eventos admitidos en V1 del Event Store quedan reducidos a 3: `FORM_SUBMITTED`, `TASK_AUTO_CLAIMED`, `FORM_REJECTED`
    And esta separación garantiza que la bóveda de eventos solo contenga "actas notariales" (momentos trascendentes) y no "notas de borrador" (trabajo en progreso).

  Scenario: [REFINAMIENTO] Rollback Compensatorio Inmutable con Retry y Timeout (CA-10)
    # Origen: REF-017-02 — Preguntas #8, #9, #10 del refinamiento funcional
    # Resuelve: Define que el Rollback NO borra el evento original sino que genera un evento de compensación, y establece timeout + retry.
    Given que el CA-03 define un Rollback Compensatorio cuando Camunda falla
    Then el Rollback TIENE ESTRICTAMENTE PROHIBIDO eliminar físicamente (`DELETE`) el evento `FORM_SUBMITTED` del Event Store (eso violaría la inmutabilidad del CA-06)
    And en su lugar, el Rollback generará un evento compensatorio `FORM_SUBMIT_ROLLED_BACK` en la misma tabla `form_event_store` con una referencia (`original_event_id`) al evento original anulado
    And el Worker de proyección del CA-01 DEBERÁ ser consciente de estos eventos compensatorios: al proyectar, si un `FORM_SUBMITTED` tiene un `FORM_SUBMIT_ROLLED_BACK` posterior, el evento original se excluye de la tabla analítica  
    And **Timeout:** El Backend esperará un máximo de **10 segundos** la respuesta de Camunda antes de considerar que el motor está caído
    And **Retry:** Antes de ejecutar el Rollback, el Backend reintentará la comunicación con Camunda **3 veces** con esperas crecientes (1s, 2s, 4s = 7 segundos de retry + 10s de timeout final = 17 segundos máximos de espera total en el peor caso)
    And si los 3 reintentos fallan, ENTONCES se ejecuta el Rollback Compensatorio y se devuelve HTTP 500 al Frontend.

  Scenario: [REFINAMIENTO] Estructura Obligatoria del Registro de Rechazo (CA-11)
    # Origen: REF-017-03 — Preguntas #14, #15 del refinamiento funcional
    # Resuelve: Define qué campos contiene el `rejectionLogs` del CA-05 y cómo se presenta el historial.
    Given la inyección de `rejectionLogs` en el BFF `/form-context` definida en el CA-05
    Then cada entrada del array `rejectionLogs` contendrá OBLIGATORIAMENTE los siguientes campos:
    And 1. **`rejectedBy`** (string): Nombre completo del revisor que ejecutó el rechazo (no anonimizado — la trazabilidad prevalece en V1).
    And 2. **`rejectedAt`** (timestamp ISO 8601): Fecha y hora exacta del rechazo.
    And 3. **`reason`** (string, max 1000 caracteres): Dictamen textual explicando el motivo del rechazo, escrito por el revisor.
    And 4. **`stageName`** (string): Nombre de la etapa BPMN donde ocurrió el rechazo (Ej: "Control de Calidad", "Aprobación Legal").
    And 5. **`taskId`** (string): Identificador de la tarea que fue rechazada.
    And **Presentación en UI (delegado a US-029):** El rechazo MÁS RECIENTE se muestra como Alert principal en la Pantalla 2. El historial completo (si hay más de 1 rechazo) se muestra como sección plegable debajo del Alert, ordenado del más reciente al más antiguo.

  Scenario: [REFINAMIENTO] Cifrado At-Rest de Datos PII en el Event Store (CA-12)
    # Origen: REF-017-04 — Pregunta #21 del refinamiento funcional
    # Resuelve: Los datos personales en la bóveda de eventos deben estar protegidos igual que en el LocalStorage del navegador.
    Given que la US-029 CA-11 exige cifrado PII en el LocalStorage del navegador
    And que la columna `payload_json` del Event Store puede contener campos PII (Ej: cédula, teléfono, dirección)
    Then la base de datos PostgreSQL DEBE tener habilitado cifrado at-rest (Transparent Data Encryption o equivalente en la infraestructura) para proteger los datos almacenados en disco
    And adicionalmente, los campos marcados como `PII/Sensibles` en el esquema Zod (US-003) se cifrarán a nivel de aplicación (AES-256) ANTES de escribir el `payload_json` al Event Store
    And la llave de cifrado se gestionará a través del servicio de secretos de la infraestructura (Azure Key Vault / AWS KMS), siendo DIFERENTE de la llave usada en el LocalStorage del CA-11 de US-029
    And para consultas analíticas que requieran datos PII, el Worker de proyección descifrará los campos específicos al proyectar a las tablas analíticas, que a su vez estarán protegidas por permisos de rol `AUDITOR`/`ADMIN_IT`.

  Scenario: [REFINAMIENTO] Validación de Pertenencia al Grupo en Auto-Claim (CA-13)
    # Origen: REF-017-05 — Pregunta #28 del refinamiento funcional
    # Resuelve: El Auto-Claim del CA-04 no verifica explícitamente que el usuario pertenezca al grupo de la tarea.
    Given que el CA-04 define un Auto-Claim para tareas de grupo sin `assignee`
    Then ANTES de ejecutar el `taskService.claim()`, el Backend DEBERÁ verificar OBLIGATORIAMENTE que el `userId` extraído del JWT sea miembro activo del `candidateGroup` configurado para esa tarea en Camunda
    And esta verificación se realizará consultando `taskService.createTaskQuery().taskCandidateUser(userId)` o equivalente
    And si el usuario NO pertenece al grupo, el Auto-Claim se ABORTA con HTTP 403 Forbidden y mensaje: "No tiene permisos para reclamar tareas de este grupo de trabajo"
    And esta validación es complementaria al Implicit Locking de US-029 CA-07/CA-18, y NO lo reemplaza.

  Scenario: [REFINAMIENTO] Rate-Limiting en Endpoints de Borradores (CA-14)
    # Origen: REF-017-06 — Pregunta #29 del refinamiento funcional
    # Resuelve: Protección contra saturación del servidor por exceso de guardados automáticos.
    Given que el endpoint `PUT /draft` puede recibir peticiones frecuentes por el Debounce de 10s de US-029 CA-24
    Then los endpoints de borradores (`PUT`, `GET`, `DELETE` del CA-07) tendrán un Rate-Limit de **6 peticiones por minuto por tarea** (consistente con el Debounce de 10 segundos)
    And las peticiones que excedan este límite recibirán HTTP 429 Too Many Requests con header `Retry-After: 10`
    And el Frontend (US-029) deberá atrapar este HTTP 429 silenciosamente (sin mostrar error al operario) y reintentar en el próximo ciclo de Debounce.

  Scenario: [REFINAMIENTO] Referencia de Evento Visible para el Operario (CA-15)
    # Origen: REF-017-07 — Pregunta #34 del refinamiento funcional
    # Resuelve: El operario necesita un "número de comprobante" para poder citar a soporte ante cualquier incidencia.
    Given el envío exitoso de un formulario (CA-01 `FORM_SUBMITTED`)
    Then la respuesta HTTP 200 del endpoint `POST /complete` incluirá en el body un campo `eventReference` con un código legible de máximo 12 caracteres (Ej: `EVT-A3F8K9`)
    And este código será una representación corta y legible del `event_id` UUID del evento grabado en el Event Store
    And el Frontend (US-029 CA-21) mostrará esta referencia en la pantalla de confirmación: "Tarea completada exitosamente. Referencia: EVT-A3F8K9"
    And el operario podrá citar esta referencia a Soporte Técnico para rastrear su envío específico en el Event Store.

  Scenario: [REFINAMIENTO] Eliminación de Borrador como Parte del Flujo de Submit (CA-16)
    # Origen: REF-017-08 — Pregunta #39 del refinamiento funcional
    # Resuelve: Evita borradores fantasma eliminando el draft DURANTE el submit, no después.
    Given que el endpoint `POST /complete` finaliza exitosamente (FORM_SUBMITTED + Camunda avanzado)
    Then como ÚLTIMO paso del flujo transaccional (antes de retornar HTTP 200), el Backend ejecutará automáticamente la eliminación del borrador (`DELETE /draft`) asociado a esa `taskId` en la tabla `task_drafts`
    And esta eliminación se ejecuta dentro de la MISMA transacción del `FORM_SUBMITTED` — si la eliminación del draft falla, NO se aborta el submit (el submit tiene prioridad)
    And el Frontend (US-029) ejecutará la purga de LocalStorage DESPUÉS de recibir el HTTP 200, pero el servidor ya habrá limpiado su parte independientemente.

  Scenario: [REFINAMIENTO] SLA de Latencia Máxima para el Endpoint /complete (CA-17)
    # Origen: REF-017-09 — Pregunta #41 del refinamiento funcional
    # Resuelve: Define el tiempo máximo aceptable que el operario espera tras presionar [Enviar].
    Given que el operario ve el spinner de espera (US-029 CA-20) al presionar [Enviar]
    Then el endpoint `POST /api/v1/workbox/tasks/{id}/complete` DEBERÁ completar su ciclo completo (validación Backend + grabación Event Store + notificación a Camunda + response) en un máximo de **5 segundos** en condiciones normales de operación
    And en el peor caso (con los 3 reintentos del CA-10 por falla transitoria de Camunda), el tiempo máximo extendido será de **17 segundos** antes de devolver HTTP 500
    And si el Backend detecta que el procesamiento superará los 5 segundos SIN error de Camunda (Ej: lentitud de PostgreSQL), registrará un log de alerta para monitoreo proactivo
    And NO se emitirán respuestas HTTP 202 ("aceptado para después") — el resultado siempre será síncrono: HTTP 200 (éxito) o HTTP 5xx (error).

  Scenario: [REFINAMIENTO] Política de Archivado Anual del Event Store (CA-18)
    # Origen: REF-017-10 — Pregunta #44 del refinamiento funcional
    # Resuelve: Previene degradación del rendimiento por acumulación masiva de eventos a lo largo de los años.
    Given que la tabla `form_event_store` acumulará volúmenes crecientes de datos año tras año
    Then se implementará una política de archivado automático con las siguientes reglas:
    And 1. **Eventos con `created_at` mayor a 12 meses** se moverán automáticamente a una tabla de archivo (`form_event_store_archive`) mediante un Job programado mensual.
    And 2. La tabla de archivo tiene IDÉNTICO esquema que la tabla principal pero reside en un tablespace optimizado para lecturas infrecuentes.
    And 3. Las tablas de proyección analítica del CA-01 NO se archivan (se mantienen activas para dashboards).
    And 4. Los eventos archivados siguen siendo INMUTABLES y consultables bajo demanda — el archivado NO es un borrado, es una reubicación.
    And 5. Los usuarios con rol `AUDITOR` podrán consultar eventos archivados a través de una interfaz administrativa (diferido a V2).

  # ==============================================================================
  # E. REFINAMIENTO UX/UI PARA ESCONDER COMPLEJIDAD CQRS (MONITOREO DE CONEXIÓN)
  # Origen: Refactorización Híbrida solicitada por Diseño UX (2026-04-20)
  # Propósito: Reemplazar el bloque técnico "CQRS Engine / Sync Eventual" con un
  #            componente de "Toast" no intrusivo y orientado a negocio.
  # ==============================================================================

  Scenario: [UX/UI] Monitoreo Asíncrono No Intrusivo (Debounce Visual) (CA-19)
    Given la necesidad de comunicar el estado de sincronización (CQRS/Event Sourcing) a un usuario de negocio
    Then el Frontend TIENE PROHIBIDO mostrar bloques técnicos fijos (Ej: "Estado: Sincronizando CQRS Engine").
    And implementará un debounce visual de 5 segundos. Es decir, las micro-sincronizaciones (ej. edición rápida de un campo) operarán de manera invisible si se resuelven en menos de 5 segundos.
    And si la sincronización supera los 5 segundos (latencia o desconexión), se activará el componente Toast Flotante del CA-20.

  Scenario: [UX/UI] Anatomía y Posicionamiento del Toast Flotante (CA-20)
    Given la activación del componente de monitoreo asíncrono
    Then se proyectará como un "Toast Flotante" (Notificación pequeña y transitoria).
    And OBLIGATORIAMENTE se ubicará en la esquina inferior izquierda de la pantalla, evitando colisiones con acciones críticas (Fitts's Law), perfiles de usuario o modales centrales.

  Scenario: [UX/UI] Lenguaje Orientado a Negocio (Prohibición de Jerga) (CA-21)
    Given que el Toast Flotante emite un estado de conexión
    Then el lenguaje TIENE PROHIBIDO incluir términos arquitectónicos (`CQRS`, `STOMP`, `Event Sourcing`).
    And debe utilizar semántica de negocio estandarizada.
    And Estados permitidos: "Guardando cambios...", "Trabajando sin conexión", o "Conexión restaurada".

  Scenario: [UX/UI] Interfaz Cinética y Operatividad Pasiva en Desconexión (CA-22)
    Given una desconexión de red que activa el estado "Trabajando sin conexión"
    Then el Toast Flotante SERÁ NO-BLOQUEANTE.
    And el usuario PODRÁ continuar ejecutando "operatividad pasiva" en la interfaz actual (ej. interactuar con modales abiertos, copiar texto de lectura, desplazarse por el Workdesk).
    And no se desplegará una pantalla completa de error que interrumpa su lectura o trabajo en curso inmediato.

  Scenario: [UX/UI] Transición Predictiva a Modo Degradado (CA-23)
    Given un estado persistente de desconexión detectado por el frontend
    Then el Toast Flotante mutará a un estado visual explícito de "Modo Degradado" (ej. ícono 🔴 / 🟡).
    And el componente indicará claramente que los datos ingresados desde ese momento se almacenarán en modo "Borrador Local" (LocalStorage de US-029) y estarán pendientes de envío definitivo.

  Scenario: [UX/UI] Reconexión Silenciosa en Background (CA-24)
    Given un usuario trabajando en Modo Degradado
    When la red se restablece en el navegador
    Then el Frontend ejecutará el barrido y sincronización pendiente de forma automática en el *background*.
    And NO exigirá que el usuario oprima botones de "Reintentar" ni modales bloqueantes.

  Scenario: [UX/UI] Feedback Positivo y Desvanecimiento de Éxito (CA-25)
    Given la sincronización exitosa de los datos pendientes tras una reconexión
    Then el Toast Flotante pasará brevemente a un estado de éxito (ej. color verde: "Cambios guardados").
    And permanecerá en pantalla un máximo de 3 segundos (Regla de Hicks).
    And finalmente se desvanecerá, dejando la pantalla limpia y retornando al estado de invisibilidad (CA-19).

  Scenario: [UX/UI] Prevención Contra Colisiones Visuales en Error Fuerte (CA-26)
    Given la convivencia del Toast Flotante de conexión y los Modales de Error Transaccional
    When la base de datos o el motor devuelven un error duro de negocio (HTTP 4XX / 500)
    Then la notificación de error transaccional de negocio toma PRIORIDAD ABSOLUTA.
    And se proyectará a través de los modales estándar (o `Toast de Error Critico` superior derecho) de la US-000.
    And el Toast inferior izquierdo de conexión entrará en estado de latencia/silencio para no saturar al usuario con doble mensajería.
```

**Trazabilidad UX:** Wireframes Pantalla 2 (Vista de Tarea) y BFF Invisible.

---

