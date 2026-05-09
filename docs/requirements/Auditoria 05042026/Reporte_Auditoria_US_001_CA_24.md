# Reporte de Auditoría Forense: US-001 - CA-24
**Fecha:** 2026-05-06
**Auditor:** Agente Arquitecto Líder
**Protocolo:** Top-Down (Cero Búsqueda Semántica)

## 1. Objetivo de la Auditoría
Validar el cumplimiento del **CA-24** (Umbrales Configurables del Semáforo SLA) de la historia **US-001** (Motor Core). 
Este criterio dictamina que el semáforo SLA de las tarjetas (Verde, Amarillo, Rojo, Negro) opere mediante cálculos porcentuales del tiempo total de resolución. Además, dichos porcentajes de transición deben ser configurables por cada Tenant (US-036), dejando de lado esquemas puramente rígidos.

## 2. Ruta de Navegación Estructural
Atendiendo al flujo estricto Top-Down sin indexación de búsqueda semántica:
1. Lectura del SSOT: `docs/requirements/epics/epic_A_motor_core.md`.
2. Identificación de la capa de visualización: `frontend/src/views/Workdesk.vue`.
3. Verificación de provisión de datos SLA y DTOs a través del State Manager: `frontend/src/stores/useWorkdeskStore.ts`.
4. Verificación de consumo de Preferencias Globales: `frontend/src/stores/usePreferencesStore.ts`.

## 3. Hallazgos: Hardcoding de Lógica de Negocio
Se detectaron brechas severas de deuda técnica y violación al diseño del producto:
*   **Acierto Reactivo:** El cálculo es responsivo respecto a `timeStore.currentTick`, logrando la viveza visual de los "SLA Pills" a medida que pasa el tiempo.
*   **Brecha Funcional 1 (SLA_THRESHOLDS rígidos):** En `Workdesk.vue`, los umbrales transaccionales (Verde >50%, Amarillo >15%) están codificados como constantes (Hardcoded) dentro del script bajo `SLA_THRESHOLDS`. El componente no consume configuraciones globales ni invoca a `usePreferencesStore.ts`, imposibilitando a los Tenants personalizar sus políticas de atención según lo dicta la US-036.
*   **Brecha Funcional 2 (Ventana SLA Global de 48H rígida):** Para que el frontend logre calcular un "porcentaje restante", necesita conocer la longitud total esperada (ventana) del SLA (es decir, el 100%). El Backend no está enviando la `fecha_creacion` de la tarea o el `sla_total_ms` a través del `WorkdeskGlobalItemDTO`. En consecuencia, el Front-End declara estáticamente: `const totalSlaWindow = 48 * 60 * 60 * 1000;`. Esto arruina la integridad de los datos para cualquier proceso cuyo SLA varíe de 48 horas (por ejemplo, procesos de alta criticidad a 2 horas, o baja prioridad a 5 días).

## 4. Inyección de Trazabilidad
Para visibilizar y atar la corrección al ciclo de calidad:
*   **En `Workdesk.vue`:** Se inyectó `// @Traceability(US = "US-001", CA = {"CA-24", "CA-20", "CA-36"})` sobre la declaración estática `SLA_THRESHOLDS` para señalar la violación de las directrices CA-24 (Umbrales) y CA-36 (Tenant Settings).

## 5. Actualización de Deuda Técnica
La anomalía arquitectónica fue redactada en el documento de escalamiento ágil `scaffolding/tasks/task.md`. Se recomienda extender el Payload DTO enviado por el Backend con los atributos necesarios (`sla_total_ms` o `creationDate`) y acoplar el semáforo al `usePreferencesStore.ts` tras refactorizar su capacidad para administrar SLAs de Tenant.

**ESTADO DE LA AUDITORÍA:** COMPLETADA.
