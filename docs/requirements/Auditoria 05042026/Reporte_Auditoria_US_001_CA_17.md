# Reporte de Auditoría Forense: US-001 - CA-17
**Fecha:** 2026-05-06
**Auditor:** Agente Arquitecto Líder
**Protocolo:** Top-Down (Cero Búsqueda Semántica)

## 1. Objetivo de la Auditoría
Validar el cumplimiento del **CA-17** (Jerarquía Multi-Origen y Resolución de Ambigüedades) de la historia **US-001**. 
Este requerimiento define las reglas matemáticas de priorización (sorting) y presentación visual:
*   Si dos tareas expiran en la misma hora, ordenar por Prioridad de Impacto y luego por Fecha de Creación.
*   Las tareas sin SLA (`dueDate = null`) deben ir al final (`NULLS LAST`).
*   Las tareas de impacto masivo deben forzar un rebase top-1 y mostrar el badge `[Impacto 🔥]`.
*   Mapeo determinista del porcentaje de la columna "Avance" basado en el nombre.

## 2. Ruta de Navegación Estructural
1. Extracción de directrices desde `docs/requirements/epics/epic_A_motor_core.md`.
2. Análisis de JPQL en `WorkdeskProjectionRepository.java` para constatar la estructura matemática de ordenamiento (`ORDER BY`).
3. Verificación de lógica de presentación en `Workdesk.vue` para certificar el badge dinámico `🔥 Impacto`.
4. Revisión del DTO (`WorkdeskGlobalItemDTO.java`) documentado previamente para certificar la frontera de impacto masivo (`>= 8`).

## 3. Hallazgos Estratégicos y Deuda Técnica

*   **Brecha de Ordenamiento Matemático (SLA Destruido en Backend):**
    *   *Defecto:* La consulta nativa JPQL (`ORDER BY w.impactLevel DESC, w.slaExpirationDate ASC NULLS LAST`) aplica el nivel de impacto de forma *generalizada* antes que la expiración SLA.
    *   *Consecuencia:* Esto interrumpe el principio FIFO/SLA de la bandeja. Cualquier tarea con impacto 5 se posará encima de una tarea de impacto 4 que expire en una hora. El CA requería que el impacto rompiera el SLA *solo* si era "Masivo" (>= 8) o que funcionara como *desempate* si el SLA era idéntico. Adicionalmente, el Repositorio omitió el desempate por Fecha de Creación estipulado por negocio.
*   **Acierto de Regla Nula:**
    *   La directriz `NULLS LAST` se integró exitosamente en la consulta JPQL de Base de Datos.
*   **Acierto Visual (Frontend):**
    *   La lógica de inyección de UI en `Workdesk.vue` evalúa correctamente la propiedad `financialImpactHigh` y proyecta fielmente el badge `🔥 Impacto` requerido.
*   **Brecha Columna de Avance (Ver CA-23):**
    *   El mapeo determinista contra la cantidad de etapas fue ignorado. El motor no emite el `progressPercent`, forzando al Frontend a mostrar "N/D" (ya documentado).

## 4. Inyección de Trazabilidad
*   `WorkdeskProjectionRepository.java` (Línea 24): `@Traceability(US = "US-001", CA = {"CA-17"})` marcando el query de ordenamiento defectuoso.
*   `Workdesk.vue` (Línea 285): `<!-- @Traceability(US = "US-001", CA = {"CA-17"}) -->` inyectado para dejar constancia del acierto en la capa de UI respecto al rendering de impacto masivo.

## 5. Actualización de Deuda Técnica
Documentado en `task.md` detallando el Mismatch de Ordenamiento. Esta falla es candidata de severidad Alta porque pervierte la matriz de criticidad de las operaciones del negocio, rompiendo la promesa de atención temporal (SLA).

**ESTADO DE LA AUDITORÍA:** COMPLETADA.
