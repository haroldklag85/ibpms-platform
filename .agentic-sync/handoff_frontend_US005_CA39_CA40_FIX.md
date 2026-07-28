# Handoff Arquitectónico: Frontend (Fix Playwright y Cierre CA-39/CA-40)

## Metadata
- **US Target:** US-005 (Integración Formulario-BPMN)
- **CAs a resolver:** CA-39, CA-40
- **Rama:** DevDavid
- **Requisito Especial:** Ausencia de Agente QA. El Agente Frontend ASUME la responsabilidad de estabilizar y ejecutar la prueba E2E de Playwright y garantizar el cumplimiento.

## Contexto del Incidente
El Agente QA intentó certificar CA-39 y CA-40 ejecutando Playwright. Debido a un timeout de renderizado (aparentemente porque `RouteGuards.ts` interceptaba por falta de roles, o porque Vite tardaba en compilar el CSS), la prueba falló. Adicionalmente, el test de Playwright arrancó los contenedores E2E (`postgres-e2e-1`, etc.) pero no los destruyó al fallar, causando un bloqueo de puertos y pánico en el entorno. El Arquitecto Líder ya mató y purgó los contenedores.

## Objetivos del Frontend Agent
1. **Implementar el Teardown Global de Playwright:**
   - Crear/Configurar un archivo `global-teardown.ts` en `frontend/e2e/` (y enlazarlo como `globalTeardown` en `playwright.config.ts`) que ejecute: `docker compose -f ../docker-compose.e2e.yml down -v --remove-orphans`.
   - **Regla Fundamental:** Las pruebas NO pueden dejar contenedores corriendo al finalizar, sin importar si pasan o fallan.
2. **Solucionar el problema del E2E y los Mocks:**
   - Asegurarte de que `us005-bpmn-form-binding.e2e.spec.ts` se ejecute correctamente y no haga timeout (verificar si los roles en `.auth/user.json` son los correctos para que `RouteGuards.ts` permita la navegación al Diseñador BPMN).
   - Cerciorarte de que el Dropdown de formularios cargue los forms de la base de datos real.
3. **Verificación Definitiva:**
   - Ejecutar `npx playwright test us005-bpmn-form-binding` exitosamente de principio a fin, asegurando la destrucción final de los contenedores Docker por el teardown.
   - Realizar el commit final y empujar a `DevDavid` para cerrar el Sprint 01.

## 📋 DIRECTIVA DE INFRAESTRUCTURA HÍBRIDA
El backend Spring Boot corre en consola local (NO en Docker) en el puerto `8080`. Antes de iniciar tu trabajo, ejecuta el protocolo de pre-validación:
1. `curl -s http://localhost:8080/actuator/health` → Debe responder `{"status":"UP"}`.
2. Si no responde, arráncalo con: `cd backend && mvn spring-boot:run -pl ibpms-core -Dspring-boot.run.profiles=default`.
3. Verifica los servicios Docker: `docker ps` → PostgreSQL (`5433`), Redis (`6379`) y RabbitMQ (`5672`) deben estar `Up (healthy)`.
**PROHIBIDO** levantar el backend vía Docker o modificar el `docker-compose.yml`.

> 🛑 **REGLA FUNDAMENTAL E IRROMPIBLE DE TEARDOWN DOCKER:**
> Dado que has asumido temporalmente el rol de QA, tienes LA OBLIGACIÓN ESTRICTA Y MILIMÉTRICA de garantizar la destrucción de los contenedores `e2e` al finalizar o fallar la ejecución de Playwright. Configura el Teardown Global. Queda absolutamente prohibido dejar contenedores fantasma (ej. `postgres-e2e-1`) corriendo y bloqueando puertos del host.

⚠️ **IMPORTANTE:** Todo desarrollo o configuración debe quedar exhaustivamente documentado. Se exige **PRECISIÓN QUIRÚRGICA** en cada cambio para evitar efectos colaterales en funcionalidades existentes. Se han detectado regresiones en iteraciones previas; cualquier daño a funcionalidades adyacentes será motivo de rechazo inmediato.

**INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_Frontend.md`.
4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_Frontend.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION` y programa la solución.
6. **ANTES del commit final**, actualiza `docs/sprints/gobernanza_pm/CHANGELOG_NO_TECNICO.md` explicando tu trabajo en lenguaje sencillo y amigable para un CEO (QUÉ hiciste y PARA QUÉ sirve). 
7. Finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.

📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
- Aplica estrictamente el protocolo de pruebas **TDD** documentado en `.agents/skills/tdd_first/SKILL.md` (Red -> Green -> Refactor) antes de hacer lógica.
- Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md` (naming, error handling, modularidad).
- Build obligatorio: Ejecuta el protocolo Zero-Trust UI documentado en `.agents/skills/frontend_build_audit/SKILL.md`.
