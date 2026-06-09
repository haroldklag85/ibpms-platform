# Auditoría de Remediación J-04: Cumplimiento Arquitectónico (ADR-001 y ADR-006)

**Fecha:** 13 de Mayo de 2026
**Objetivo:** Evaluar el nivel de acoplamiento, escalabilidad e integridad arquitectónica de la reciente remediación del componente **Workdesk (J-04)** frente a las directivas fundacionales de Hexagonal Architecture (ADR-001) y el motor Frontend Vue 3 (ADR-006).

---

## 1. Evaluación Backend: Arquitectura Hexagonal y DDD (ADR-001)

### Componentes Auditados
- `WorkboxTaskController.java` (Capa de Infraestructura - Adaptador REST)
- `WorkdeskTaskListener.java` (Capa de Infraestructura - Adaptador de Eventos / Camunda)

### Hallazgos de Cumplimiento ✅
1. **Aislamiento de la Capa de Infraestructura:** Las modificaciones recientes sobre el `WorkdeskTaskListener` para segregar el `topic` de WebSockets (aislamiento de tenants `GHOST_CLAIM`) recaen puramente en la capa de adaptadores periféricos. No hubo inyección de librerías STOMP ni referencias a *messaging brokers* dentro del Dominio de la tarea, cumpliendo estrictamente con el principio de dominio agnóstico.
2. **Definiciones del Contrato de Interfaz (Ports):** La adición masiva de metadatos OpenAPI (`@Operation`, `@ApiResponses`) en el `WorkboxTaskController` consolida la fachada REST sin filtrar comportamiento ni objetos persistentes directamente (uso de DTOs). 

### Infracciones o Brechas Detectadas (Gaps) ⚠️
1. **Delegación Orquestal Excesiva en Controladores:** Aunque el controlador delega apropiadamente en puertos de entrada, componentes vecinos (ej. `WorkdeskQueryController`) han acumulado excesiva lógica de limitación de peticiones (Rate Limiting manual mediante `Bucket`) y validación de Claims de JWT directamente en la definición del endpoint. Según el ADR-001, esta lógica transversal debería estar contenida en Filtros de Seguridad de Infraestructura o Interceptores, no dispersa dentro de los métodos del Controlador, ya que engrosa el adaptador REST ("*anti-patrón de Script de Transacción*").

---

## 2. Evaluación Frontend: Low-Code Engine y Reactividad (ADR-006)

### Componentes Auditados
- `Workdesk.vue` (Vista Principal, Grilla de Tareas y Métricas)
- `useWorkdeskStore.ts` (Gestión de Estado Centralizada - Pinia)

### Hallazgos de Cumplimiento ✅
1. **Ausencia de Inyección Arbitraria (Sandboxing Estricto):** La incorporación de la paginación a 15 elementos y la inyección reactiva del WebSocket (`_showGhostClaimToast`) hacia la UI respeta el flujo reactivo de Pinia dictado por el MVP de ADR-006. No existen dependencias a mutaciones del DOM crudas ni riesgo de evaluación dinámica (inyección XSS).
2. **Uso de Clases Utilitarias (Shadow DOM Posterged):** Toda la inyección de ergonomía (Tooltips semánticos como *title="SLA en Riesgo"* y botones condicionales) se ha compuesto orgánicamente sobre la malla de utilidades seguras de TailwindCSS, alineándose con la Excepción Táctica V1 del ADR-006, la cual prioriza componentes basados en plantillas `<template>` sobre componentes `Render (h())` en aras del *Velocity* de entrega.

### Infracciones o Brechas Detectadas (Gaps) ⚠️
1. **Fragmentación Asíncrona (Race Conditions):** Aunque el store maneja correctamente la conexión asíncrona de WebSockets, ciertas operaciones reactivas complejas carecen de bloques `finally` universales. Según la filosofía estricta de estabilidad UI abordada en el ADR-006, si se cae el canal STOMP, la experiencia de usuario puede quedar en estado zombi temporalmente al fallar el ciclo de degradación elegante local, pese a que la conexión a `CQRS` notifique la pérdida.

---

## 3. Plan de Remediación Sugerido (Hoja de Ruta)

A continuación, se define el *Roadmap* de corrección para los "Gaps" o infracciones menores detectadas que prevendrán vulneraciones más profundas al escalar el producto:

### Mitigación Backend (Alineación Estricta a ADR-001)
*   **Centralización del Rate Limiting:** 
    *   **Acción:** Refactorizar el uso directo de `Bucket4j` en el `WorkdeskQueryController`.
    *   **Solución:** Mover esta orquestación de tráfico a un filtro transversal (`Filter` de Spring Security o capa de `API Gateway`) permitiendo que el controlador REST asuma una función puramente de delegación hacia la capa `application/ports/in`.
*   **Consolidación de Identidad / Tenant:**
    *   **Acción:** Evitar recuperar el `TenantId` leyendo directamente la sesión JWT en el controlador REST.
    *   **Solución:** Aprovechar la inyección mediante `HandlerMethodArgumentResolver` que extraiga limpiamente el `TenantId` al DTO de origen o de forma transparente antes de tocar la capa de Aplicación.

### Mitigación Frontend (Alineación Estricta a ADR-006)
*   **Contratos de Estado Atómicos (Pinia):**
    *   **Acción:** Re-evaluar `useWorkdeskStore.ts` para agrupar todas las peticiones asíncronas de origen bajo un bloque global intermedio de manejo de errores.
    *   **Solución:** Implementar interceptores HTTP globales o *Store plugins* que apaguen limpiamente banderas de red `isLoading` sin importar la complejidad del evento (asegurando el cumplimiento de "Zero DOM Thrashing" implícito en aplicaciones empresariales Vue 3).

---
*Fin del Reporte*
