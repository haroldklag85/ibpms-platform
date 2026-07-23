# Handoff Infra/BD: Tuning de Conexiones (J-04)

**Objetivo:** Incrementar la tolerancia de la infraestructura de desarrollo local a cargas concurrentes altas (E2E testing), específicamente para prevenir la asfixia del pool de conexiones PostgreSQL y Camunda BPM.

**Instrucciones Arquitectónicas:**
1. **HikariCP Pool:** En el archivo `application-dev.yml` (y si existe `application-e2e.yml` en el backend), ubicar la configuración de datasource de Hikari e incrementar agresivamente el `maximum-pool-size` a 50 y el `minimum-idle` a 10.
2. **Timeouts de Hikari:** Ajustar `connection-timeout` a 20000ms para darle margen a Playwright antes de declinar transacciones en picos de carga.
3. **Docker Config:** Si aplica, revisar el `docker-compose.yml` para asegurar que PostgreSQL no tenga un hard-limit de conexiones que choque con la configuración de Spring.

**Alineación Arquitectónica:**
- Tuning estrictamente limitado a perfiles de prueba/dev (ADR-009 PostgreSQL Database).
- Permite viabilizar la carga masiva en entorno local sin rediseñar la persistencia asíncrona (Aprobación directiva Jefatura).

> **INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
> 1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
> 2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
> 3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_infra.md`.
> 4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_infra.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
> 5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.
