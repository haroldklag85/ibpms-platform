# Análisis Funcional: US-030 - Instanciar y Planificar un Proyecto Ágil (Sprints/Kanban)

**Ejecutado por:** `[⚙️ PRODUCT OWNER]` | **Fecha:** 2026-04-17
**Workflow Aplicado:** `/analisisEntendimientoUs.md`
**Fuente de Verdad (SSOT):** `docs/requirements/epics/epic_A_motor_core.md` (CA-1)

---

## 1. Resumen del Entendimiento
La **US-030** es el mecanismo administrativo primario mediante el cual el `Scrum Master` o `Líder de Proyecto` da vida a un proyecto de base metodológica Ágil, basándose en la configuración o plantilla general importada (WBS). Su propósito es ser el "Hub Ágil" donde se estructura y prioriza el backlog del equipo antes y durante la ejecución.

## 2. Objetivo Principal
Proveer una zona de gobierno (Pantalla 10) desde donde el Líder de Proyecto inyecta responsabilidades y tareas al equipo, administrando un flujo de trabajo sin saltos estructurados rígidos (BPMN), operando nativamente en un engranaje Kanban simple en su Versión 1.

## 3. Alcance Funcional
El alcance técnico abarca **desde la creación formal del marco en la Pantalla 9 hasta la administración continua en la Pantalla 10**:
*   **INICIA:** Con el arranque de un proyecto marcado con metodología "Ágil" en la Pantalla 9 (Gestor de Proyectos de portafolio).
*   **TERMINA:** Con el despliegue y gestión de las tareas en un único lienzo o "Backlog Continuo" (Pantalla 10), donde las tarjetas vivirán hasta ser completadas.

## 4. Lista de Funcionalidades Incluidas
Debido al recorte táctico explicitado en el documento (CA-1), la historia ha sido severamente limitada a la funcionalidad más nuclear posible:
1.  **Backlog Continuo (Flat Flow):** Consolidación de un "Tablero Sin Tiempos" o "Kanban Continuo". En este tablero único, las tareas residen y fluyen desde un estado *To Do* estático hacia el final, sin estar enjauladas temporalmente por fechas de Sprint bi-semanales.

## 5. Lista de Brechas, Gaps o Ambigüedades Detectadas
Al carecer de Criterios de Aceptación robustos (solo posee uno que documenta una exclusión), la US-030 sufre de vacíos estructurales severos que bloquean su desarrollo:

*   **GAP-1 (Gestión CRUD de Tarjetas):** ¿Cómo inyecta el Líder una nueva tarjeta al lienzo en la Pantalla 10? No hay un CA que explique el alta, baja y modificación de tarjetas.
*   **GAP-2 (Asignación de Dueños - Assignees):** La descripción de la US habla de "asignar responsables directos". Sin embargo, no existe un CA que defina cómo se hace esta asignación o si respeta limitaciones de grupo (RBAC). 
*   **GAP-3 (Origen del Backlog WBS):** El enunciado menciona "utilizando una estructura base (WBS)". ¿Cómo se importan esas tareas del WBS a la Pantalla 10? ¿O el usuario debe escribirlas manualmente una a una?

## 6. Lista de Exclusiones (Fuera de Alcance)
*   **Timeboxes y Sprints:** Expresamente bloqueados en el CA-1. No habrá cuadros de tiempo como "Sprint 1 (Del 1 al 15)". La gestión de Scrum formal es un descarte de la V1.
*   **Puntos de Historia y Velocidad de Equipo:** Elementos del Scrum de alto nivel no están cubiertos por el Backlog continuo.

## 7. Observaciones de Alineación o Riesgos
### Clasificación MoSCoW
*   **Should Have o Could Have:** Podría debatirse frente al BPMN clásico, pero si el producto promete un híbrido transaccional ágil, es indispensable (Must Have) para activar el Kanban.

### Resumen de Dependencias con otras User Stories
*   **Dependencia con US-008 (Mover Tarjeta Kanban):** La US-030 define la *creación/gobernanza* del backlog en la Pantalla 10 (Hub), mientras que las tareas inyectadas aquí luego vivirán la experiencia operativa definida por la US-008 en el Tablero de Trabajo (Pantalla 3). Sin la US-030, la US-008 no tendrá tareas vivas que mover.
*   **Dependencia con US-006 (Plantillero Transversal):** La US-006 proveerá las carpetas base (WBS) o "Plantillas Ágiles" de las cuales se nutre la creación base de la US-030.

### Dependencia Bloqueante Absoluta (Riesgo Técnico)
*   **Orfandad Documental:** La historia, en su estado actual, **no es programable por Backend ni testeable por QA**, ya que solo documenta "lo que NO tiene" (No timebox), pero no tiene CAs sobre los endpoints POST, PATCH ni directivas de UI sobre qué debe presionar el líder para inyectar tarjetas a la base de datos PostgreSQL mediante Eventos. Debe entrar en Refinamiento Urgente.
