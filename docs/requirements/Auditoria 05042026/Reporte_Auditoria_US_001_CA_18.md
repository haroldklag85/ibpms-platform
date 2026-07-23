# Reporte de Auditoría Forense: US-001 - CA-18
**Fecha:** 2026-05-06
**Auditor:** Agente Arquitecto Líder
**Protocolo:** Top-Down (Cero Búsqueda Semántica)

## 1. Objetivo de la Auditoría
Validar el cumplimiento del **CA-18** (Degradación Elegante Multi-Motor y Prioridad de Reapertura) de la historia **US-001**. 
Este requerimiento establece los protocolos de resiliencia ante fallos sistémicos:
*   Si la API transaccional de Camunda cae (HTTP 500), el Workdesk debe aplicar **Degradación Elegante** cargando las tareas Kanban de la Base Relacional sin emitir una pantalla fatal (fail-closed).
*   Se proyectará un **Toast** advirtiendo "Sincronización BPMN degradada".
*   Si el operario hace Logout y entra en otra máquina, el enrutador priorizará abrir el **tablero general unificado** (Workdesk) ignorando la última URL cacheada o tarea específica.

## 2. Ruta de Navegación Estructural
1. Lectura del SSOT en `docs/requirements/epics/epic_A_motor_core.md`.
2. Inspección del manejador de errores global y consulta en `WorkdeskQueryController.java` para constatar el comportamiento ante excepciones de Camunda.
3. Evaluación de renderizado en `frontend/src/views/Workdesk.vue` para certificar la visualización de la degradación (Toast vs Banner).
4. Rastreo del ciclo de vida de autenticación y navegación en `frontend/src/stores/authStore.ts`, `frontend/src/router/RouteGuards.ts`, y `frontend/src/views/Login.vue`.

## 3. Hallazgos Estratégicos y Deuda Técnica
Se detectaron fallos críticos en la implementación técnica de la resiliencia sistémica (Back) y del diseño de interfaz (Front), aunque el ruteo sí se resolvió según lo estipulado.

*   **Fail-Open Crítico (Backend):**
    *   La interceptación de la excepción existe (el controlador captura errores que contienen la palabra "Camunda").
    *   *Defecto:* En lugar de encapsular el fallo y delegar la carga al repositorio relacional (Kanban), el controlador retorna forzosamente una colección vacía (`new PageImpl<>(Collections.emptyList())`). Esto vacía íntegramente la bandeja de entrada, borrando visualmente las tareas Kanban que sí estaban vivas. **Esta es una violación directa del principio de Degradación Elegante**.
*   **Brecha de Diseño UX (Frontend):**
    *   El CA exigía el renderizado de un *Toast flotante* para informar al usuario de la degradación, preservando intacto el layout de la herramienta de trabajo.
    *   *Defecto:* Se implementó un Banner en bloque inyectado en el DOM con clases `border-b flex items-center`. Esto desplaza verticalmente el panel de control (75/25 split) y perturba el diseño estricto exigido.
*   **Cumplimiento Positivo (Navegación Post-Login):**
    *   La persistencia de ruta fue exitosamente deshabilitada para el portal cautivo. Tras iniciar sesión vía SSO EntraID (`router.push('/')`) o modo local (`router.push('/workdesk')`), el sistema borra el historial fragmentado y dirige unificadamente al portal principal, cumpliendo la priorización dictada por el CA.

## 4. Inyección de Trazabilidad
He preservado la memoria técnica inyectando las siguientes trazas:
*   `WorkdeskQueryController.java` (Línea 132): `@Traceability(US = "US-001", CA = {"CA-18"})` evidenciando el retorno de la lista vacía (`emptyPage`) como deuda de fail-open.
*   `Workdesk.vue` (Línea 126): `<!-- @Traceability(US = "US-001", CA = {"CA-18"}) ... -->` reportando la implementación errónea del Banner vs Toast.

## 5. Actualización de Deuda Técnica
La bitácora `task.md` fue alimentada con los pormenores del hallazgo en el Backend como prioridad funcional para la corrección pre-certificación QA.

**ESTADO DE LA AUDITORÍA:** COMPLETADA.
