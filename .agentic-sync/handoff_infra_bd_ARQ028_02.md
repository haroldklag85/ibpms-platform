# Handoff INFRA y BD — ARQ-028-02

> **Rama:** `sprint-6`
> **Estado:** 🟢 SIN ACCIONES REQUERIDAS

La refactorización **ARQ-028-02** (Mover `JdbcTemplate` de Application a Infrastructure a través de un Puerto Hexagonal) es un cambio estrictamente de código Java a nivel de clases y dependencias. 

- **Infraestructura:** No requiere cambios en `docker-compose.yml`, configuración de CI/CD ni variables de entorno.
- **Base de Datos:** El query ejecutado (`INSERT INTO ibpms_audit_log...`) y la firma de la tabla se mantienen exactamente igual. No se requieren nuevos `changesets` de Liquibase.

**Instrucción:** Archivar este ticket y esperar la próxima directiva.
