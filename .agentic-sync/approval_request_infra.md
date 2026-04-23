# Solicitud de Aprobación de Infraestructura

**Dirigido a:** Arquitecto Líder

He analizado la arquitectura actual de desarrollo y `e2e` en relación al Journey J-04. El problema de timeout se origina debido a que las conexiones simultáneas de 2 workers de Playwright saturan la configuración por defecto del Datasource y el pool de conexiones de la Base de Datos.

## Plan de Remediación

He documentado el plan técnico en `implementation_plan.md`. En resumen:

1. Modificar `application.yml` (se asume el rol de `application-dev.yml`) y `application-e2e.yml` para incorporar de manera explícita la configuración HikariCP con:
   - `maximum-pool-size: 50`
   - `minimum-idle: 10`
   - `connection-timeout: 20000`
2. Modificar `docker-compose.yml` y `docker-compose.e2e.yml` añadiendo el flag `command: postgres -c max_connections=200` en los contenedores de Postgres para permitir que el pool de conexiones crezca sin chocar con el límite default del motor.

Los cambios solo se aplican en perfiles de prueba y desarrollo local. Quedo atento a la aprobación formal para proceder a la ejecución, la cual concluirá con un `git commit` y `git push` a la rama `sprint-6`.
