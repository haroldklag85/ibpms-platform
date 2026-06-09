# Handoff: Estabilización de Infraestructura E2E (BUG-S6-004)

## 1. Contexto y Objetivos
**US / BUG:** BUG-S6-002, BUG-S6-003, BUG-S6-004 (Timeouts Sistémicos en Suite E2E J-04)
**Rama de Trabajo:** `sprint-6`
**Objetivo:** Ajustar la configuración de infraestructura local (Docker / PostgreSQL) para soportar la ejecución masiva y paralela de 55 escenarios E2E sin asfixiar la base de datos ni provocar caídas por falta de conexiones.

## 2. Alineación Arquitectónica
- Se respeta el **ADR-009** (PostgreSQL).
- **Restricción Local:** La máquina host cuenta con 16GB de RAM. Los límites de memoria y conexiones deben ajustarse moderadamente para no saturar el sistema operativo host.

## 3. Requerimientos Técnicos
- La base de datos debe ser capaz de manejar al menos 100 conexiones concurrentes.
- El pool de Spring Boot (HikariCP) debe estar equilibrado con la capacidad de PostgreSQL.

## 4. Tareas a Ejecutar
1. Modificar el `docker-compose.yml` en la raíz del proyecto para PostgreSQL:
   - Añadir el comando `command: ["postgres", "-c", "max_connections=150", "-c", "shared_buffers=1GB"]` al servicio `ibpms-postgres`.
2. Modificar el archivo de configuración del Backend (ej. `application-dev.yml` / `application.yml`):
   - Ajustar `spring.datasource.hikari.maximum-pool-size` a `40` (o un valor óptimo que evite el agotamiento del pool durante test masivos).
   - Ajustar `spring.datasource.hikari.connection-timeout` a `20000` (20 segundos).

## 5. Criterios de Aceptación
- [ ] La base de datos local puede levantar exitosamente con los nuevos parámetros de `max_connections`.
- [ ] El backend no emite errores de "Connection pool exhausted" en los logs de Docker durante la ejecución paralela.

## 6. Instrucciones Operativas y de Compilación
> **INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
> 1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
> 2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
> 3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_infra.md`.
> 4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_infra.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
> 5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.

*Compilación obligatoria:* Valida la integridad del compose mediante `docker-compose config`.
