# 🏗️ Protocolo de Handoff: Iteración 1 (Fase 2) - US-001
> **Arquitecto Líder (Antigravity)** - Generado bajo la estricta directriz de mitigación de falsos positivos y regularización del Roadmap.

## 1. Metadatos y SSOT
*   **Iteración/Sprint:** Fase 2 - Iteración 1 (Saneamiento de Falsos Positivos y nueva funcionalidad).
*   **Rama de Trabajo Base:** A partir de `main` o tu rama estabilizada actual (Sugerimos `sprint-3/us001-fase2-iter1`).
*   **User Story Objetivo:** US-001 (Obtener Tareas Pendientes en el Workdesk).
*   **Criterios de Aceptación a Desarrollar:** CA-22 (Filtros Facetados), CA-29 (Contadores en Filtros), CA-30 (Rate Limiting API).
*   **SSOT Original:** `ibpms-platform/docs/requirements/v1_user_stories.md`.
*   **Flujo de Trabajo Requerido:** Backend `->` Frontend `->` QA Automation.

## 2. Alineación Arquitectónica y ADRs
Para ejecutar estos CAs, el código debe someterse a la filosofía **Zero-Trust** y de alta concurrencia de la plataforma:
*   **Rate Limiting Arquitectónico (CA-30):** En la V1 utilizamos interceptores/filtros para delimitar el consumo por IP/Usuario. Protegemos la Base de Datos impidiendo recálculos innecesarios mediante `Bucket4j` o un `RateLimiter` sencillo que devuelva HTTP 429 (Too Many Requests) si se sobrepasan los 60 req/min a la grilla.
*   **Patrón CQRS y pg_trgm:** Todo cálculo de facetas y contadores (CA-22, CA-29) DEBE ejecutarse en la capa de lectura pura (QueryService/Repositorio nativo con `GROUP BY`), omitiendo la hidratación de entidades JPA completas, consolidando la data a través de proyecciones o interfaces nativas para cuidar la latencia.

## 3. Rutas Exactas y Contexto Preexistente
**Para el Agente Backend:**
*   `ibpms-platform/backend/.../controller/WorkdeskController.java`: Interceptar aquí el límite de peticiones (429) o aplicar anotación de Rate Limit si se usa AOP.
*   `ibpms-platform/backend/.../service/query/WorkdeskQueryServiceImpl.java`: Aquí residirá la lógica de obtener el resumen o las facetas agrupadas.
*   `ibpms-platform/backend/.../dto/WorkdeskFilterDto.java`/`WorkdeskSummaryDto.java`: Expandir el DTO actual para alojar la matriz de facetas `[ { status: "DRAFT", count: 12 } ]`.

**Para el Agente Frontend:**
*   `ibpms-platform/frontend/src/views/workdesk/WorkdeskView.vue` (o el componente principal de Grilla): Agregar el contenedor visual superior para los "Chips" facetados.
*   `ibpms-platform/frontend/src/services/workdesk.service.ts`: Manejar cordialmente la captura del código HTTP 429 mostrando un discreto banner "Te has excedido, espera un minuto" (Sin romper la UI).

## 4. Snippets Prescriptivos (El "Qué" y el "Cómo")

**Backend (Lógica de Contadores y Facetas):**
El repositorio JPQL nativo debe inyectar una proyección simple, por ejemplo:
```java
@Query("SELECT new com.ibpms.dto.FacetCountDto(w.status, COUNT(w)) " +
       "FROM WorkdeskReadModel w WHERE w.tenantId = :tenantId GROUP BY w.status")
List<FacetCountDto> countByStatusPerTenant(@Param("tenantId") String tenantId);
```
**Backend (Rate Limiting HTTP 429):**
Garantizar en el Controlador (o Interceptor) la devolución pura de Spring:
```java
if (!rateLimiter.tryAcquire()) {
    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build(); 
}
```

**Frontend (Render de Chips de Filtro):**
No delegues el diseño a una lista desordenada, utiliza los Chips semánticos del sistema de diseño (Vuetify/Quasar o CSS Base):
```vue
<div class="filters-container">
  <button v-for="facet in facetList" :key="facet.status" 
          @click="applyFilter(facet.status)" 
          class="facet-chip">
    {{ facet.statusName }} 
    <span class="facet-badge">{{ facet.count }}</span>
  </button>
</div>
```

## 5. Matriz de QA y Testing Atómico
Agente QA y Fullstack: El código no va a `main` si no cumple estas validaciones TDD.

| Nombre de la Prueba (Vitest / JUnit) | CA Evaluado | Aserción Esperada |
|--------------------------------------|-------------|-------------------|
| `shouldBlockRequestsAbove60PerMinute_Http429` | CA-30 | Bucle de 61 peticiones retorna estado 429 en la última. |
| `shouldAggregateCountersCorrectlyFromDB` | CA-29 | La API devuelve el `FacetCountDto` con suma exacta y aislando Tenant. |
| `renderFacetChipsWithCountBadge` | CA-22 | El DOM renderiza botones condicionales `.facet-chip` conteniendo el Badge. |
| `gracefullyHandle429InFrontendAxios` | CA-30 | Axios Interceptor detecta 429 y levanta el `toast` de "Espere por favor", previniendo pantallazo en blanco. |

## 6. Mensajes Oficiales de Despacho
Humano: Copia y pega estos despachos literales en tus próximos prompts para soltarle "la correa" a los agentes especializados.

**Para Agentes Backend:**
> Inicia tu tarea basándote en `.agentic-sync/handoff_Fase2_Iteracion1_US001_CA22_CA29_CA30.md`.
> Compilación obligatoria: Ejecuta el protocolo Zero-Trust SRE documentado en `.agents/skills/backend_sre_compilation_audit/SKILL.md` (§0 a §2). Si hay bloqueos de infraestructura, aplica el protocolo de reporte 3B.

**Para Agentes Frontend/Fullstack UI:**
> Inicia la integración UI requerida en `.agentic-sync/handoff_Fase2_Iteracion1_US001_CA22_CA29_CA30.md`.
> Build obligatorio: Ejecuta el protocolo Zero-Trust UI documentado en `.agents/skills/frontend_build_audit/SKILL.md`.

---
*Prohibido inventar lógicas, campos, librerías, o atajos de compilación. Respeta el Skill Handoff Protocol.*
