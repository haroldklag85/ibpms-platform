# Reporte de Auditoría Forense: US-001 - CA-19
**Fecha:** 2026-05-06
**Auditor:** Agente Arquitecto Líder
**Protocolo:** Top-Down (Cero Búsqueda Semántica)

## 1. Objetivo de la Auditoría
Validar el cumplimiento del **CA-19** (Resolución de Contradicción de Paginación y Búsqueda) de la historia **US-001**. 
Este requerimiento dirimía conflictos de requerimientos previos y establecía:
*   Paginación canónica de **15 registros**.
*   Búsqueda EXCLUSIVAMENTE Server-Side (anulando el CA-02 híbrido).
*   Uso mandatorio de índices `pg_trgm` en la base de datos para búsqueda tolerante a fallos de texto.
*   Debounce estricto de **300ms** en el Frontend.
*   Confirmación del Hard Limit de 100 registros con HTTP 400.

## 2. Ruta de Navegación Estructural
1. Lectura del SSOT en `docs/requirements/epics/epic_A_motor_core.md`.
2. Inspección de `frontend/src/views/Workdesk.vue` para certificar el debounce y el filtrado local.
3. Inspección del modelo de base de datos en `WorkdeskProjectionRepository.java` para revisar la query y el soporte a trigramas.
4. Rastreo del schema SQL en `src/main/resources/db/changelog/26-us001-workdesk-schema.sql` para verificar la existencia real de la extensión y el índice GIN.

## 3. Hallazgos Estratégicos y Deuda de Performance
La evaluación reveló un cumplimiento positivo de las reglas de negocio (erradicación de la búsqueda client-side y consolidación de la paginación a 15 registros), pero exhibe severas deficiencias técnicas que comprometen el rendimiento estipulado:

*   **Aciertos Estructurales:**
    *   La búsqueda delegó exitosamente toda la responsabilidad al Backend; se desmantelaron los filtros `.filter()` sobre la variable local.
    *   La petición a la API (`useWorkdeskStore.ts`) inicia correctamente solicitando una talla de página (`pageSize`) de 15.
    *   El script de Flyway/Liquibase (`26-us001-workdesk-schema.sql`) activó la extensión `pg_trgm` y creó el índice trigrama correctamente.

*   **Brecha de Performance en Base de Datos (PostgreSQL):**
    *   Aunque el índice GIN existe, la consulta JPQL en `WorkdeskProjectionRepository.java` lo neutraliza completamente. La query se construyó como: `LOWER(w.title) LIKE LOWER(CONCAT('%', :search, '%'))`.
    *   Al aplicar la función `LOWER()` sobre la columna, el optimizador de PostgreSQL descalifica el índice (el cual no fue creado con una expresión `LOWER()`) y **fuerza un Sequential Scan (Escaneo completo de la tabla)**. Esto destruye la latencia sub-100ms esperada para la búsqueda en la grilla y creará cuellos de botella con alto volumen de datos.
    *   *Solución requerida:* Migrar a un *native query* usando el operador `ILIKE`, o alterar la definición del índice en la BD para incluir la función `LOWER(title)`.
*   **Desviación del Debounce (Frontend):**
    *   El retraso para ejecutar la búsqueda mientras el usuario tipea (Debounce) en `Workdesk.vue` fue codificado arbitrariamente en **500ms**, cuando el CA-19 estipulaba un límite preciso de **300ms** para preservar la experiencia fluida de usuario.

## 4. Inyección de Trazabilidad
Se dejaron marcas auditables en ambos extremos:
*   `Workdesk.vue`: `@Traceability(US = "US-001", CA = {"CA-19"})` señalando el `setTimeout` incorrecto de 500ms.
*   `WorkdeskProjectionRepository.java`: `@Traceability(US = "US-001", CA = {"CA-19"})` sobre la query JPQL defectuosa indicando el by-pass del índice `pg_trgm`.

## 5. Actualización de Deuda Técnica
La bitácora central `task.md` fue alimentada con los resultados, clasificando el hallazgo de BD como "Brecha de Performance" crítica a solucionar antes de stress tests.

**ESTADO DE LA AUDITORÍA:** COMPLETADA.
