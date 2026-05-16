# Workflow: Habilitación y Validación de Entornos para Certificación E2E (Playwright)

**Objetivo:** Estandarizar estrictamente el proceso de preparación y verificación de la suite E2E para todos los desarrolladores locales. Esto garantiza el cumplimiento de la gobernanza Zero-Mock, asegura la consistencia determinista de los datos y previene falsos negativos.

> **Nota de Gobernanza Post-Ejecución:** La documentación, actualización de matrices de cobertura y reporte final NO forman parte de este workflow manual de desarrollo; dichas tareas son responsabilidad exclusiva de los Agentes Auditores AI.

---

## 🟩 Fase 1: Verificación de Infraestructura y Resiliencia Backend

Esta fase asegura que los servicios base estén levantados y aplica tolerancia a fallos controlada para el motor central.

1. **Verificar estado de los contenedores Docker:**
   * **Comando:** 
     ```bash
     docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
     ```
   * **Resultado Esperado:** Los contenedores `ibpms-postgres-uat`, `ibpms-rabbitmq-uat` y `ibpms-redis-uat` deben tener status `(healthy)`. `ibpms-core-dev` debe estar `Up`.

2. **Validar compilación de Spring Boot (Tolerancia de 3 Intentos):**
   * **Comando:** 
     ```bash
     docker logs ibpms-core-dev --tail 100
     ```
   * **Resultado Esperado:** Mensaje `Started Application in X seconds`.
   * **Flujo de Resiliencia (En caso de `BUILD FAILURE`):**
     1. Si Maven falla, reiniciar el contenedor para forzar un nuevo ciclo de resolución: `docker restart ibpms-core-dev`.
     2. Repetir hasta un **máximo de 3 intentos**.
     3. Si tras el tercer intento persiste el fallo de compilación, **reportar falla crítica de infraestructura al equipo** y abortar inmediatamente el workflow.

## 🟦 Fase 2: Aislamiento Inmutable y Sembrado (Data Seeding)

Garantiza que cada ejecución E2E corra sobre un estado limpio, 100% determinista e inmutable.

1. **Destrucción y Recreación de Base de Datos:**
   * **Acción:** Antes de iniciar cualquier suite de certificación, el volumen de la base de datos UAT **debe ser destruido y recreado**. Esto evita colisiones con tareas completadas en iteraciones previas.
   * **Comando Secuencial:**
     ```bash
     docker compose stop ibpms-postgres
     docker compose rm -v -f ibpms-postgres
     docker compose up -d ibpms-postgres
     docker restart ibpms-core-dev
     ```
   * **Resultado Esperado:** Al reiniciar el Core, Liquibase inyectará desde cero todo el esquema limpio (incluyendo `form_event_store`) y cargará la semilla inicial estandarizada (`45-us001-workdesk-seed.sql`).

## 🟧 Fase 3: Frontend y Gobernanza Anti-Mock

Asegurar que el FrontEnd apunta al entorno real y que no rompe el ADR-010.

1. **Ejecutar Linter Anti-Mocks:**
   * **Directorio:** Desde la terminal en `/frontend`
   * **Comando:** 
     ```bash
     npm run lint:mocks
     ```
   * **Resultado Esperado:** `0 vulnerabilidades`.

2. **Validar enrutamiento del API (Proxy):**
   * **Acción:** Comprobar que en `frontend/vite.config.ts` existe la configuración de `proxy: { '/api': { target: 'http://127.0.0.1:8080' } }`. (Vite realiza el forward nativo, haciendo innecesario el uso de un `.env` local).

## 🚀 Fase 4: Framework de Pruebas y Gestión de Sesiones

Alistamiento obligatorio del motor Headless y control estricto de JWT.

1. **Generación Explícita de Token E2E:**
   * **Acción:** El workflow exige *explícitamente* generar una sesión fresca antes de correr los flujos autenticados de CQRS o Workdesk.
   * **Comando:**
     ```bash
     npx playwright test e2e/certification/emergency-login.spec.ts --project=login-tests
     ```
   * **Resultado Esperado:** Aprobación del test y generación automática del archivo de estado en `frontend/e2e/playwright/.auth/user.json`.

2. **Binarios de Navegación (One-Time Setup):**
   * **Comando (si no se han instalado previo):** `npx playwright install chromium`

## 🎯 Fase 5: Ejecución y Monitoreo Estratégico

> **⚠️ Condición de Carrera (Socket Hang Up):** Debido a que el entorno inmutable compila el proyecto completo y ejecuta Liquibase desde cero, Spring Boot abre el puerto TCP `8080` *antes* de que el contexto de Tomcat y la seguridad estén completamente iniciados. **No se puede automatizar la espera únicamente con un ping o comprobación de socket TCP (ej. `Test-NetConnection`)**, ya que dará un falso positivo. Lanzar Playwright prematuramente causará errores de proxy (`socket hang up`) en Vite.

1. **Espera Obligatoria (Luz Verde Visual):**
   * **Acción:** Antes de lanzar la suite, debes confirmar en el log del backend que el contenedor finalizó completamente su inicialización.
   * **Comando:** `docker logs -f ibpms-core-dev`
   * **Validación:** Esperar **exclusivamente** la aparición del mensaje `Started Application in X seconds`.

2. **Lanzar la Suite E2E de Certificación:**
   * **Comando:**
     ```bash
     npx playwright test e2e/certification/ --project=authenticated
     ```
   * **Resultado Esperado:** Ejecución limpia (Green Build) de la suite sin falsos negativos de infraestructura.

3. **Monitoreo en Tiempo Real (Debug Visual):**
   * **Comando:** Mantener abierta una terminal secundaria con:
     ```bash
     docker logs -f ibpms-core-dev
     ```
   * **Justificación:** Si Playwright falla, el log mostrará inmediatamente si fue un Timeout del DOM o un problema en la Saga/CQRS dentro de Spring Boot.
