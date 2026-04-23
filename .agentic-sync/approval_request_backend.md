# Solicitud de Aprobación - Backend (J-04 Optimización Concurrencia)

Arquitecto Líder, he elaborado el plan de implementación para abordar la mitigación de los timeouts en el entorno E2E.

**Estrategia Propuesta:**
1. **Adición de Dependencias:** Inclusión de `spring-boot-starter-cache` en `pom.xml` para aprovechar las abstracciones nativas de caché.
2. **Refactorización Hexagonal:** Desplazamiento de la lógica de consulta desde `WorkdeskQueryController` hacia una nueva clase `WorkdeskQueryService` para habilitar inyección segura AOP.
3. **Caché Distribuido (Redis):** Se habilita `@Cacheable("workdesk_tasks")` en el nuevo servicio basando la llave del caché en tenant, usuario, búsqueda y páginación.
4. **Invalidación (Eviction):** Debido al alto costo de la fragmentación de `@CacheEvict` a lo largo de docenas de servicios dispares (Sagas, DMN, Webhooks), se optó por la alternativa de **"TTL corto"** especificada en el Handoff. Se usará un TTL global de 10 segundos para "workdesk_tasks" mediante `RedisCacheManagerBuilderCustomizer`.

El plan completo se encuentra en `implementation_plan.md`.

¿Apruebas la arquitectura propuesta y el refactor hacia el Application Service para proceder al modo EXECUTION?
