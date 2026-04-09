# 🏗️ Protocolo de Handoff: Iteración 78-DEV - US-001 (Cierre Deuda)
> **Arquitecto Líder (Antigravity)** - Generado bajo Protocolo Cierre de Deuda Técnica.

## 1. Metadatos y SSOT
*   **Iteración/Sprint:** 78-DEV.
*   **Rama de Trabajo Base:** `sprint-3/informe_auditoriaSprint1y2`.
*   **User Story Objetivo:** US-001 (Obtener Tareas Pendientes en el Workdesk).
*   **Criterios de Aceptación a Desarrollar:** CA-22 (Filtros Facetados), CA-29 (Contadores en Filtros), CA-30 (Rate Limiting API). *(Nota: Se descartan funcionalidades ligadas a la V2).*
*   **SSOT Original:** `ibpms-platform/docs/requirements/v1_user_stories.md`.

## 2. Flujo de Trabajo y Dependencia de Ejecución (Secuencial)
El trabajo de estos agentes debe ser **ESTRICTAMENTE SECUENCIAL**:
1.  **Agente Backend:** Inicia desarrollando la lógica de Rate Limiting y las querys de repositorio, incluyendo de inmediato sus test unitarios al Data Layer.
2.  **Agente Frontend:** Procede solo cuando el Backend confirma. Consume los nuevos DTOs asíncronos y gestiona los estados de HTTP 429.
3.  **Agente QA:** Finaliza trazando el End-to-End o validaciones cruzadas.

## 3. Estrategia QA y NFR Atómico
*   **Estrategia:** Ejecución de pruebas unitarias dirigidas específicamente al **Repository Data Layer** del Backend.
*   **Regla de Seguridad (NFR):** Se debe garantizar perimetraje riguroso en las consultas SQL (verificar aserciones de protección *Tenant Isolation* y *Row-Level Security* en cada faceta).

## 4. Rutas Exactas y Contexto
*   **Back:** `WorkdeskController.java`, `WorkdeskQueryServiceImpl.java`, Test suite del Repository respectivo.
*   **Front:** Componente de Grilla Vue (`src/views/workdesk/WorkdeskView.vue`), handler Axios en `workdesk.service.ts`.

## 5. Snippets Prescriptivos (El "Qué")
**Backend (Pilar de Repositorio/Agrupación):**
Proyección JPQL para el CA-29:
```java
@Query("SELECT new com.ibpms.dto.FacetCountDto(w.status, COUNT(w)) " +
       "FROM WorkdeskReadModel w WHERE w.tenantId = :tenantId GROUP BY w.status")
```
**Frontend (Pilar Visual):**
Control de chips y badge contenedor del contador (CA-22) reaccionando al límite (CA-30):
```vue
<div class="filters-container">
  <button v-for="facet in facetList" :key="facet.status" @click="applyFilter(facet.status)">
    {{ facet.statusName }} <span class="facet-badge">{{ facet.count }}</span>
  </button>
</div>
```

## 6. Mensaje de Despacho (Protocolo Zero-Trust Autorizado)
*Ver sección de copiado para el humano.*
