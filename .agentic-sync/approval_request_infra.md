# Solicitud de Revisión — Agente Infra/BD (Iteración 08)

**Fecha:** 2026-05-08T11:03:00-05:00  
**Agente:** Infra/BD  
**US:** US-036 (Identity Governance)  
**CAs:** CA-29, CA-30, CA-31, CA-32  
**Rama:** DevDavid  

---

## Resumen del Diagnóstico

He realizado una auditoría empírica completa del stack Redis para soportar la Caché Híbrida (CA-32).

### Estado de Infraestructura Redis

| Componente | Estado |
|------------|--------|
| Contenedor `ibpms-redis-uat` | ✅ Up 16h, healthy, Redis 7.4.8 |
| Puerto 6379 expuesto | ✅ |
| PING/PONG | ✅ |
| Backend conectado a Redis | ✅ 2 clientes conectados |
| `spring.cache.type: redis` | ✅ |
| `@EnableCaching` | ✅ |
| `@Cacheable("menuTopology")` | ✅ en MenuLayoutService |
| `@CacheEvict("menuTopology")` | ✅ en MenuLayoutService |
| Backend health | ✅ `{"status":"UP"}` |

### Hallazgo y Propuesta

**Problema detectado:** El cache `menuTopology` no tiene TTL explícito (usa Redis default = ∞). Si `@CacheEvict` no se invoca por un bug, la caché queda stale indefinidamente.

**Solución propuesta:** Añadir TTL de 30 minutos para `menuTopology` en `CacheConfig.java`:

```java
.withCacheConfiguration("menuTopology",
    RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(30)))
```

### Impacto
- **CERO regresión** — Solo se modifica la configuración del cache `menuTopology`.
- **Ningún changeset Liquibase** — No se requiere (confirmado por el handoff).
- **Ningún cambio en docker-compose.yml** — Redis ya está operativo.

---

## Solicitud Formal

Arquitecto Líder: solicito su **aprobación** para proceder con la adición del TTL de seguridad en `CacheConfig.java`.

**Responda con:**
- ✅ **APROBADO** — para que proceda a modo EXECUTION
- ❌ **RECHAZADO + motivo** — para corregir antes de ejecutar
