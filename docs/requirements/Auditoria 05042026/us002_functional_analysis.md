# Análisis Funcional: US-002 (Reclamar una Tarea de Grupo)

**Fecha de Ejecución:** 2026-04-08
**Rol:** Product Owner / Software Architect
**Workflow Aplicado:** `/analisisEntendimientoUs.md`

## 1. Resumen del entendimiento
La historia de usuario **US-002 (Reclamar una Tarea de Grupo / Claim Task)** define la mecánica de pre-asignación y transferencia de propiedad de tareas dentro del ecosistema BPMN (Camunda). El entendimiento central es que esta historia regula la exclusividad operativa (quién es el dueño del caso) para evitar la duplicidad de esfuerzos, gestionando desde el momento en que una tarea está disponible en una cola grupal hasta que es asignada a un operario específico, devuelta voluntariamente, o despojada (reactiva o proactivamente). Incluye la orquestación en tiempo real de la UI para todos los operarios conectados y salvaguardas estrictas a nivel de transacciones concurrentes en la base de datos.

## 2. Objetivo principal
El objetivo principal de negocio y funcional es garantizar un único Punto de Verdad Operativo (Single Point of Truth) respecto a la propiedad de la tarea en tiempo real. Esto evita el retrabajo (que dos analistas atiendan el mismo expediente) y previene la retención dañina (abandonos de SLA), asegurando que el estado de "Asignación" sea atómico, trazable y sincrónico en todas las pantallas del equipo (vía WebSockets).

## 3. Alcance funcional
**Hasta dónde llega:** El alcance abarca de manera exclusiva las operaciones de mutación sobre la propiedad de la tarea (`assignee`). Esto incluye el acto de reclamar (individual o en lote), explorar sin alterar, devolver voluntariamente con notas asociadas, despojar forzosamente vía RBAC y el Auto-Unclaim por inactividad. Incluye también los mecanismos de UI resiliente y la limpieza transaccional de estados locales.
**Dónde termina:** Termina en la frontera de la ejecución del negocio; es decir, **NO** incluye cómo se llena el formulario, cómo se emiten validaciones de negocio en los campos (Zod), ni cómo se avanza el flujo de Camunda (Submit). Esos alcances pertenecen a la US-029 y US-003.

## 4. Lista de funcionalidades incluidas
- **Reclamo Individual y Masivo Atómico:** Asignación segura con prevención de condición de carrera mediante `SELECT FOR UPDATE SKIP LOCKED` o la API de Camunda (`TaskService.claim`), con soporte masivo de hasta 20 tareas (CA-1, CA-2, CA-11, CA-14).
- **Emisión Sincrónica (WebSockets):** Disparo de eventos tipo Batch (`BULK_REMOVE`/`ADD`) post-commit para re-renderizar de manera fluida (desvanecimiento escalonado) las pantallas del resto del equipo (CA-12, CA-23).
- **Modo Sólo Lectura (Exploratorio):** Apertura de casos para visualización sin alterar el `assignee`, con expulsión reactiva no-obstructiva (Banner amarillo) si otro operario la toma en paralelo (CA-5, CA-18).
- **Peer-to-Peer Handoff (Transición Voluntaria):** Liberación de tareas acompañada de "Notas Internas" efímeras (estilo post-it) para heredar contexto al siguiente compañero (CA-4, CA-16).
- **Limpieza Transaccional ("Amnesia"):** Borrado disciplinado del LocalStorage para purgar formularios no completados, unido a limpieza desfasada (24h) de adjuntos transitorios `orphaned` (CA-7, CA-17).
- **SLA Ghost Job Timeout:** Rutina recurrente de limpieza (defecto: 4 horas inactividad) para desencadenar *Auto-Unclaim*. Integra pre-avisos (75% del tiempo) en la UI y sistema de auto-extensiones limitado (2 veces) (CA-6, CA-15, CA-19).
- **Despojo Jerárquico IDOR-Safe (Supervisores):** Ejecución de Forced Unclaim resguardado por las validaciones perimetrales de la matriz organizacional (`team_id`), documentando quién despoja a quién (CA-8, CA-13).
- **Resiliencia Optimistic UI vs Fallos de Red:** Reclamos procesados visualmente en tiempo-cero ("Mentira Banca") que hacen rollback elocuente y notificado al operario tras agotar ruteos de backoff exponencial (CA-10, CA-21).
- **Desacoplamiento Visual:** Implementación de pestañas separadas "Mi Bandeja" y "Cola del Equipo" (CA-22).
- **Registro Forense y Auditoría:** Pop-up histórico (Timeline) reflejando inmutables razones y actores detrás de cada mutación transaccional (CA-9, CA-20).

## 5. Lista de brechas, gaps o ambigüedades
*Aunque las remediaciones CA-11 al CA-23 cerraron la mayoría de los hallazgos de análisis previos, permanecen las siguientes fricciones:*
- **Definición Estricta de "Heartbeat" (CA-15):** Se decreta que el timeout se evade solo con operaciones registrables, pero no se especifica si se dispondrá de mecanismos "Auto-Save" transparentes en el motor para reiniciar el timer si el usuario tarda 5 horas redactando un fallo de tutela exhaustivo sin haber pulsado "guardar borrador". Si la US-029 no incorpora auto-save al momento del typeo, el usuario será castigado con un Auto-Unclaim inmerecido.
- **Transición Híbrida Rol/Equipo en Escalamiento (CA-13):** Las validaciones estáticas del `team_id` no definen el flujograma cuando una tarea cruza (es reasignada o reclamada) a un Staff de Controlaría que puede estar fuera del `team_id` originario de origen. ¿Quién despoja si el usuario no pertenece ya a un grupo local?
- **Rollback en la Liberación:** El *Optimistic UI* (CA-10/CA-21) rige fuertemente la reclamación de tareas. No se especifica si el acto de *Liberar/Unclaim* goza de la misma resiliencia o si requiere validación sincrónica estricta contra backend para no perder la "Amnesia Transaccional".

## 6. Lista de exclusiones (o aspectos fuera de alcance)
- **Ejecución y Persistencia de la Data del Expediente:** Purgar el LocalStorage sí es su tarea, pero persistir los datos de negocio en Camunda **no** lo es.
- **El renderizado del contenido dinámico y validación Zod:** Está excluido el trazado de la interfaz de lo que contiene la tarea internamente (US-003, US-028, US-029).
- **Notificaciones Push y Chats:** El mensaje de Peer-Handoff (CA-16) limita su existencia a un texto adherido bajo la tarea y **NO** es un sistema de notificaciones/Emails, ni se integra a Teams/Slack.
- **Restricción global corporativa:** Límite estructural de secuestro (cuantas tareas un analista puede tener al mismo tiempo) ha sido arrojado formalmente a V2 (Nota CA-3).
- **Analíticas Complejas:** Calcular Camino Crítico (PERT) o avances financieros basados en rotación de reclamos es exclusivo de post-lanzamiento V2 (Nota SSOT).

## 7. Observaciones de alineación o riesgos
- **Clasificación MoSCoW:** **MUST**. (Requisito Obligatorio, confirmado en el artefacto `scope_master_v1.md`).
- **Resumen de Dependencias con otras User Stories:**
  - **US-001 (Workdesk / Pantalla 1):** Dependencia obligatoria. Provee la grilla visual de la Cola de Grupo, los mecanismos de WebSockets base y las lógicas de bloqueo atómico que fueron refactorizadas e inyectadas aquí (CA-11).
  - **US-036 (RBAC / Pantalla 14):** Dependencia de Integridad. Administra la jerarquía `team_id`. Si US-036 no está lista, las comprobaciones de perimetraje orgánico de Despojo (CA-13) fallarán al no tener de dónde alimentar la matriz de permisos.
  - **US-029 (Completar Tarea / Pantalla 2):** Dependencia paralela. La US-002 necesita interactuar en modo destrucción local "LocalStorage purge" (CA-07) sobre las especificaciones de borrador que la US-029 haya levantado.
- **Dependencia Bloqueante:** **US-001 y US-036 son bloqueantes a nivel Backend.** Sin el API de WebSocket configurado estructuralmente en el Workbench (US-001), los CA-12 y CA-23 de la US-002 fallarán dramáticamente a nivel E2E ya que la desaparición automática transaccional es la columna vertebral UX de la plataforma y el perimetraje IDOR de RBAC (US-036) la espina de seguridad en auditoría del CA-13.
