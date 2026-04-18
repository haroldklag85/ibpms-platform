# 🤝 Handoff de Arquitectura: Sprint 1 (Fase Shift-Left Backend)

> **Destinatario:** Agente de Backend / Java Spring Boot
> **Alcance:** Creación de Suites REST Assured para 11 US (Code Freeze en vigor)
> **Directiva:** Cero Mocks transversales. Uso exigido de `AbstractIntegrationTest` (Testcontainers).

---

## Bloque 1: Track de Seguridad y Autenticación (US-036, US-038, US-048)

**Objetivo:** El Backend debe demostrar mediante tests automatizados que el cortafuegos RBAC impide acceso no autorizado.
**Acciones:**
1. Crear `SecurityIntegrationTest.java`.
2. Forjar Requests HTTP vía `REST Assured` inyectando JWTs maliciosos, expirados, o con Roles degradados.
3. El controlador protegido DEBE emitir estado `HTTP 403 Forbidden` u `HTTP 401 Unauthorized` bajo reglas estrictas de la arquitectura API (EntraID Context).
4. Corroborar el aislamiento Mutli-Tenant (Tenant A no puede leer registros de Tenant B en endpoints GET).

## Bloque 2: Track de Eventos Asíncronos (US-034)

**Objetivo:** Garantizar que los mensajes de eventos y SLA no se pierden en la infraestructura.
**Acciones:**
1. Sumar `RabbitMQContainer` al singleton en `AbstractIntegrationTest.java` para proveer mensajería E2E a la JVM.
2. Escribir pruebas que manden un evento a una Exchange DDL y corroboren que el consumidor `@RabbitListener` inserta el histórico en PostgreSQL efímero.

## Bloque 3: Módulo IDE (US-003, US-005, US-028)

**Objetivo:** Las API de persistencia generativa y BPMN deben reaccionar robustamente a malos inputs.
**Acciones:**
1. Crear `FormSchemaControllerTest.java` mandando JSON Schemas malformados esperando un elegante `HTTP 400 Bad Request` validado jerárquicamente por validaciones Zod-transpuestas o Schema Validators.
2. `BpmnModelControllerTest.java`: Enviar XML dañado y comprobar el rechazo seguro del Parser de Camunda/Zeebbe antes de corromper la BD.

## Firmas de Recepción y Criterio de Gate final Backend
- [ ] Leído y analizado.
- [ ] Todas las 11 US auditadas reportan Verde localmente vía REST Assured.
- [ ] Plugin Jacoco configurado localmente en `pom.xml` para emisión del Coverage del Sprint 1.
