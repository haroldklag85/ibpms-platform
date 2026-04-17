# Sprint 1 — Estrategia "Test Pyramid" (Validación Backend & Frontend)

> **Sprint:** 1
> **Estrategia:** Alternativa B (Shift-Left Testing)
> **Prerequisito:** Gate Sprint Puente aprobado (Botones y WebSockets de US-001/US-002 integrados).
> **Objetivo:** Blindar la lógica de negocio de las 11(once) Historias de Usuario completadas creando una densa capa de pruebas Unitarias y de Integración. Cero enfoque en el DOM (Playwright desactivado temporalmente).

---

## Reglas de Gobernanza y Roles

- **Code Freeze Activo:** Ningún agente puede proponer, construir ni diseñar nuevos requerimientos de negocio.
- **TDD / Aislamiento:** Cualquier bug funcional que encontremos en las 11 US, se modelará primero creando un Unit Test que lo haga fallar, seguido por la corrección en el código del controlador/composable.
- **Arquitecto Líder (Agente IA):** Emite handoffs para creación de pruebas. No codifica.
- **Agentes Ejecutores:** Crean simulaciones (Testcontainers) y ejecutan `mvn test` o `vitest`.

---

## Ejecución: La Pirámide Estructural

### TRACK A: Infraestructura de Pruebas Aisladas (Día 1)
Desplegar las herramientas que garantizan que nuestras aserciones van contra contenedores acoplados pero desechables localmente y no emulados.

- **Integración Backend:** Implementar `Testcontainers` (PostgreSQL, RabbitMQ, Redis) dentro de `src/test/java/...`
- **Configuración Frontend:** Asegurar que `vitest` con `happy-dom` esté configurado para testear stores Pinia (RBAC / Workdesk) aislados de los componentes Vue UI.

### TRACK B: Integración y Validaciones de Controladores (Días 2 y 3)
Asegurar que los controladores no rompan y aíslen la seguridad correctamente (Nivel 3 y 4 de testing). El foco absoluto está en REST Assured.

Se ejecutarán aserciones unitarias/integración para todas las métricas funcionales documentadas de estas US:
- **Core de Orquestación:** US-001 (Workdesk), US-002 (Claiming), US-005 (BPMN Modeler).
- **Control de Seguridad:** US-036 (RBAC & EntraID), US-038 (IdP), US-048 (Service Accounts).
- **Módulo IDE Forms:** US-003 (Mapeo Zod/UI), US-028 (Auto-generación Tests), US-039.
- **Módulo Transaccional:** US-034 (RabbitMQ), US-043 (SLA Paramétrico).

#### Ejemplos de Pruebas Obligatorias (No-Negociables)
- Controladores deben emitir 403 Forbidden cuando el RLS/IDOR se activa.
- Test de concurrencia: Dos Request al mismo `/claim` de US-002 deben responder exitoso a 1hilo, y HTTP 409 Conflict al perdedor.

---

## Criterios de Aceptación (Gate para Salir del Sprint 1)

1. El reporte de Cobertura Backend (Jacoco) debe certificar paso verde `BUILD SUCCESS` al ejecutar `mvn clean test`.
2. El reporte Frontend (Vitest) debe emitir verde en los almacenes críticos de permisos (RBAC) y cronómetros.
3. Se han emitido formalmente Tickets de Bug para todas las anomalías que no lograron estabilizarse y se arreglaron antes de cerrar el Sprint.
4. **Firmado para E2E:** El cimiento metodológico es suficientemente sólido para declarar que cualquier error futuro en UAT es exclusivamente problema de enlace de UI o DOM, y no un fallo de negocio.
