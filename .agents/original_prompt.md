## 2026-06-07T05:43:00Z

El proyecto consiste en completar el desarrollo y estabilización de la historia **US-005 (BPMN Modeler - Rediseño de Barra de Herramientas, Paneles Laterales y Corrección de Bugs)**, y resolver dos bugs de estabilidad críticos detectados en la plataforma iBPMS (V1), aplicando de forma estricta el patrón TDD (Test-Driven Development) y directrices de inmutabilidad de regresiones.

**Working directory:** Y:\home\haroltandrsgmezagu\proyectos\ibpms-platform
**Integrity mode:** development

---

## 📋 Contexto y Reglas de Contexto de Sesión
- **Exclusividad de Alcance**: Durante esta sesión queda establecido de forma estricta que solo se trabajará sobre el alcance de la **US-005** y los bugs asociados. Queda prohibido desviar la atención a otras historias o introducir alucinaciones de código fuera del alcance directo.
- **Leyes de Gobernanza**: Cumplir rigurosamente las directrices de `.cursorrules`, en especial:
    - *Ley Global 1*: Iniciar siempre los mensajes con el Collar de Identidad correspondiente.
    - *Ley Global 2*: Prohibición absoluta de `git stash` (usar `git commit` y `git push` en ramas de sprint).
    - *Ley Global 3*: Trazabilidad Inversa en código mediante anotaciones `@Traceability` o comentarios `// @Traceability: US-005, CA-XX`.
- **Handoff Quality Standard (HQS)**: Todo handoff técnico generado entre subagentes debe seguir estrictamente la plantilla de 7 secciones del archivo `.agents/skills/handoff_quality_standard/SKILL.md`. Cada subagente debe definir explícitamente las habilidades (`skills`) a emplear para su entrega.
- **Skills de Subagentes Mandatarios**: En los handoffs y ejecuciones técnicas de los subagentes, es obligatorio invocar y utilizar las siguientes habilidades:
    - `addyosmani_sre_discipline` (Disciplina SRE, Zero-Mock y validación estricta)
    - `addyosmani_planning` (Estructura de planificación fina)
    - `addyosmani_code_review` (Auditoría de código de doble ojo)
    - `yudhi_architecture_compliance` (Cumplimiento de arquitectura hexagonal y ADRs)
    - `yudhi_database_migrations` (Validaciones transaccionales y de Liquibase sin DDLs espurios)

---

## 🔬 Diagnóstico de Bugs a Resolver

### Bug 1: El auto-guardado no funciona y el lienzo se pierde tras inactividad
- **Causa Raíz**: 
    1. En `BpmnDesigner.vue` (`onMounted`), no se inicia el motor de tiempo `timeStore.startEngine()`, manteniendo congelada la variable `timeStore.currentTick` y paralizando el temporizador de auto-guardado y los heartbeats del candado.
    2. Al crear o cargar un proceso, no se sincroniza reactivamente el parámetro `processId` en la URL de navegación (`router.replace`), provocando que cualquier remontaje del componente Vue redirija erróneamente al Welcome Modal.
    3. Al expirar el candado pesimista en el servidor por falta de latidos, no existe un mecanismo visual claro para renovarlo.
- **Solución Requerida**: 
    - Iniciar y detener el motor de tiempo con `timeStore.startEngine()` y `timeStore.stopEngine()` en los ganchos correspondientes.
    - Sincronizar reactivamente el query parameter `processId` en la URL.
    - Mostrar un banner no bloqueante de advertencia cuando el candado expire por inactividad, con un botón interactivo para renovarlo en un solo clic.

### Bug 2: Error "Error cargando el XML del proceso" al seleccionar un Draft en recientes
- **Causa Raíz**:
    1. Cuando el frontend solicita el XML de un proceso draft recién creado (cuyo XML no ha sido persistido o es nulo en base de datos), el servicio en backend lanza un `IllegalArgumentException` no controlado en el controlador.
    2. Esto devuelve un error HTTP 500/400 que rompe la carga del lienzo y dispara un toast rojo.
- **Solución Requerida**:
    - Modificar `getProcessXml` en `BpmnDesignController.java` para capturar `IllegalArgumentException` and retornar un estado `200 OK` con un XML BPMN vacío con estructura básica por defecto.
    - Si el borrador seleccionado no tiene XML en base de datos, el frontend debe inicializar el lienzo con dicha plantilla vacía (evento de inicio básico) asociada a su ID técnico.

---

## 🎯 Requerimientos y Decisiones de Rediseño (Alineados en `/grill-me`)

### R1. Barra de Herramientas en Stepper Secuencial
La barra superior se organiza en un Stepper de 6 pasos de izquierda a derecha:
- **Paso 1: Inicio** (antiguo Biblioteca). Contiene el explorador de procesos, importar, exportar y el botón manual **"Guardar"** como control secundario.
- **Paso 2: Modelado** (Canvas + Consultar Copiloto IA).
- **Paso 3: Simulación** (Validar y Simular, Limpiar Trayectoria).
- **Paso 4: Trazabilidad** (Historial de Versiones y Auditoría de Cambios unificados).
- **Paso 5: Despliegue** (Solicitar Despliegue, lista de solicitudes en modo Solo Lectura para roles `BPMN_Designer`).
- **Paso 6: Operación** (Gestor de Instancias).
- **Detalle Estético**: Fondo translúcido con desenfoque de fondo (Glassmorphism: `backdrop-filter: blur(8px)`) y bordes semi-transparentes finos.
- **Etiquetas de Estado**: Los elementos visuales `"BORRADOR"` y `"SANDBOX"` deben rediseñarse como badges planos, redondeados, de solo lectura, con **tonos grises suaves** y tooltip explicativo al pasar el cursor (sin efectos hover de botón ni invitaciones a cliquear).
- **Control de Versión**: La cabecera mostrará la versión de iBPMS (`v0`, `v1`, `v2`...) vinculada al `Version Tag` de Camunda (inicializado en `"1"` para procesos nuevos).
- **Paso 6 Deshabilitado**: En procesos en borrador (`v0`), el Paso 6 debe mostrarse grisáceo y deshabilitado con un tooltip aclarativo.

### R2. Cajón Lateral Derecho Unificado (Trazabilidad Drawer)
- El historial de versiones y la bitácora de auditoría se unificarán en un **panel lateral derecho deslizable (Drawer) no bloqueante** que empuje y reduzca el lienzo de forma adaptativa.
- El panel se compone de dos pestañas: "Historial de Versiones" y "Auditoría de Cambios", detallando metadatos (versión, fecha/hora, autor, estado).
- Al abrirse, debe ocultar de forma limpia el panel de propiedades nativo de bpmn-js y restaurarlo al cerrarse.
- En procesos en borrador (`v0`), el historial de versiones mostrará un estado vacío descriptivo: *"No hay versiones publicadas aún"*.
- Las acciones de despliegue o rechazo en la auditoría requerirán un comentario de justificación mínimo obligatorio de 20 caracteres.

### R3. Notificaciones y Sincronización
- En caso de fallas de red o errores de persistencia en el auto-guardado en segundo plano, se notificará al usuario mediante un **Toast temporal de 5 segundos** que se desvanece solo (evitando banners persistentes intrusivos).
- El estado del guardado se reflejará discretamente en la barra de estado superior ("Guardado hace x segundos", "Validado").

---

## 📋 Estrategia de Pruebas y TDD (Gobernanza de Regresión)

Se debe seguir estrictamente la estrategia de **Test-Driven Development (TDD)**:
1. **Paso Inicial**: Antes de escribir o modificar código, localiza o crea las pruebas unitarias/integración correspondientes a los cambios.
2. **Ejecutar Pruebas (Fallo Inicial)**: Ejecuta las pruebas para confirmar que fallan (luz roja).
3. **Desarrollar**: Implementa el código mínimo necesario para solucionar los bugs y cumplir el diseño.
4. **Ejecutar Pruebas (Éxito final)**: Ejecuta las pruebas nuevamente para validar el paso a verde.
5. **Inmutabilidad de Regresión**: Está prohibido alterar aserciones o casos de prueba preexistentes de sprints anteriores con el fin de forzar luz verde. Si fallan, se corrige el código del negocio, no los tests.

### Comandos de Ejecución de Pruebas

#### A. Backend (JUnit e Integración)
Asegurar que la base de datos estática esté activa en los puertos indicados por `application-test.yml`.
```bash
mvn -f backend/pom.xml test -pl ibpms-core -Dtest=BpmnDeployContractTest,DataMappingIntegrityTest,SandboxGovernanceTest
```

#### B. Frontend (Playwright E2E y CT con GPU Acelerada en WSL2)
Correr las pruebas forzando el uso de la GPU local (NVIDIA RTX 5080) a través de `PLAYWRIGHT_USE_GPU=true`:
- **Certificación E2E**:
  ```bash
  PLAYWRIGHT_USE_GPU=true npx playwright test --config=playwright.e2e.config.ts
  ```
- **Component Testing (CT)**:
  ```bash
  PLAYWRIGHT_USE_GPU=true npm run test:ct
  ```
- **Build de Verificación**:
  ```bash
  npm run --prefix frontend build
  ```

---

## 🏁 Acceptance Criteria (DoD)

### UI Barra de Herramientas & Stepper
- [ ] El Paso 1 se llama visualmente "Inicio" y contiene el botón "Guardar" manual como control secundario.
- [ ] Las etiquetas "BORRADOR" y "SANDBOX" son badges planos grises suaves de solo lectura con tooltips informativos.
- [ ] El Paso 6 ("Operación") se muestra grisáceo y deshabilitado para procesos en `v0`.
- [ ] La cabecera muestra la versión del historial de iBPMS (secuencial v0, v1, v2...) y se sincroniza con el Tag de Camunda inicializado en "1".
- [ ] La barra de herramientas aplica efectos de Glassmorphic (fondo translúcido con blur y bordes finos).

### Cajón Deslizable de Trazabilidad
- [ ] Al pulsar "Versiones" o "Auditoría" se abre un cajón lateral derecho no bloqueante que empuje/reduce el canvas BPMN.
- [ ] El cajón oculta el panel de propiedades nativo de Camunda al abrirse y lo restaura al cerrarse.
- [ ] El cajón tiene pestañas para "Historial de Versiones" y "Auditoría de Cambios".
- [ ] Para un proceso nuevo en borrador (v0), el historial de versiones muestra el estado vacío "No hay versiones publicadas aún".

### Estabilidad y Calidad de Código
- [ ] El backend maneja `IllegalArgumentException` en el endpoint `/xml` y devuelve el XML por defecto con HTTP 200.
- [ ] El motor de ticks de `timeStore` se inicia y detiene adecuadamente en `BpmnDesigner.vue` (`startEngine` y `stopEngine`).
- [ ] La URL se mantiene sincronizada con el query parameter `processId` al crear o cargar procesos.
- [ ] Existe un banner no bloqueante en caso de pérdida/expiración de candado para re-adquirir.
- [ ] Los errores de red en el auto-guardado se reportan con un Toast temporal de 5 segundos.
- [ ] Los tests de regresión del backend compilan y finalizan con éxito.
- [ ] El build de frontend (`npm run build`) y las pruebas CT/E2E compilan y pasan en verde con la aceleración por GPU.

## 2026-06-09T18:55:35Z

El usuario ha otorgado la autorización explícita de desarrollo para la US-005. Revive el flujo de trabajo, levanta los subagentes necesarios y finaliza la implementación completa del Stepper superior, el panel deslizable derecho de trazabilidad (drawer con pestañas de versiones y auditoría) y las correcciones asociadas al Bug 1 y Bug 2. Asegúrate de compilar y correr todas las pruebas (Vitest, JUnit y Playwright E2E con GPU) confirmando que pasan en verde.

## 2026-06-10T00:34:28Z

Fix welcome modal and tech ID misalignment in BPMN Modeler (US-005) when loading process with typographic name-key mismatch.

Working directory: z:/home/haroltandrsgmezagu/proyectos/ibpms-platform
Integrity mode: development

Note: The regression tests have already been written in BpmnDesigner.spec.ts and their initial failure has been verified. The detailed handoff with architectural rules and surgical instructions is located at:
.agentic-sync/handoff_US005_bug_guardado.md

Please follow the handoff instructions strictly to apply the fix, run the tests to verify they pass (green light), run the production build, and commit the changes conventions-style to the sprint branch.

## Requirements

### R1. Technical ID Immutability (processId)
The technical ID of an existing process must remain unchanged after loading. Modify the watcher of `currentProcessName` in `BpmnDesigner.vue` to only generate the ID technical slug if the process is completely new (v0). Prevent overwriting the technical ID during process load.

### R2. Test-Driven Development (TDD)
Write unit test cases in `BpmnDesigner.spec.ts` that verify:
1. Changing the business name on a new process (v0) generates the slug ID.
2. Loading an existing process (v1+) keeps the technical ID unchanged, even if it does not match the name.
Verify that tests fail initially, then pass after implementation.

## Acceptance Criteria

### TDD & Stability
- [ ] Test cases verifying technical ID immutability are written in BpmnDesigner.spec.ts.
- [ ] Tests fail when executed before fixing the code.
- [ ] Tests pass (green light) after the fix is implemented.
- [ ] Vitest test suite runs successfully with `npm run test:unit` inside WSL.
- [ ] Production build compiles cleanly with `npm run build` inside WSL.
- [ ] Conventional Git commit is pushed to Sprint branch.

Required Skills to apply:
- addyosmani_sre_discipline
- addyosmani_planning
- addyosmani_code_review
- yudhi_architecture_compliance
- yudhi_database_migrations

## 2026-06-10T01:46:55Z

Fix HTTP 400 Bad Request when saving or auto-saving drafts for newly created processes (v0) that are not yet persisted in the database (US-005).

Working directory: z:/home/haroltandrsgmezagu/proyectos/ibpms-platform
Integrity mode: development

Note: The TDD integration test has already been written in BpmnDeployContractTest.java and its initial failure has been verified (returned 400 instead of 200). The detailed handoff with architectural rules and surgical instructions is located at:
.agentic-sync/handoff_US005_creacion_procesos.md

Please follow the handoff instructions strictly to apply the fixes in both the Java backend (BpmnDesignService.java) and Vue frontend (BpmnDesigner.vue). Verify that all tests pass, run the production build, and commit the changes conventions-style to the sprint branch.

## Requirements

### R1. Auto-creation of Process Drafts in Backend
When the frontend auto-saves or manually saves a draft using `PUT /api/v1/design/processes/{id}/draft`, the backend currently throws an `IllegalArgumentException` and returns HTTP 400 if the process doesn't exist in the database.
- Modify `BpmnDesignService.guardarBorradorPorTechnicalId` (and any associated service/port logic) to **automatically create and persist** a new process record in the database if it is not found by its `technicalId`.
- The new process should be initialized with:
  - `status = BpmnProcessDesign.Status.BORRADOR`
  - `currentVersion = 0`
  - `name` derived from the technical ID (e.g. capitalized slug: "Solicitud TC3" for key "solicitud-tc3") or parsed from the BPMN XML.
  - `formPattern` defaulted to `SIMPLE` (or parsed from the XML metadata if available).
- Ensure this auto-creation is atomic, audited via `auditPort`, and returns HTTP 200/201 to the frontend.

### R2. Proper Frontend Network Error Handling
Currently, any failure during `saveDraft()` in `BpmnDesigner.vue` triggers a false network connection warning toast: `"Modo Offline: Guardado en API falló. Revisa tu conexión de red."`.
- Modify `saveDraft()` in `BpmnDesigner.vue` to inspect the Axios error response.
- Only show the network connection banner if there is a real network failure (e.g., `error.code === 'ERR_NETWORK'` or status code 503).
- For status code 400 or other application-level exceptions, display a specific error toast reflecting the actual issue returned by the backend.

### R3. Test-Driven Development (TDD)
- Update/Add JUnit integration tests in `BpmnDesignControllerIT.java` or `BpmnDeployContractTest.java` to verify that sending a `PUT /{id}/draft` request for a non-existent process successfully creates the process as a `BORRADOR` and returns HTTP 200.
- Update/Add Vitest unit tests in `BpmnDesigner.spec.ts` to assert that:
  - Successful auto-saving of a new process does not show the error banner.
  - Actual network failures (HTTP 503/Network Error) display the offline error banner correctly.

## Acceptance Criteria

### Draft Persistence & Validation
- [ ] Auto-saving a newly created process (v0) on the modeler UI returns HTTP 200 and does not display any offline/network error banner.
- [ ] The newly created process is registered in the database and visible in the Process Catalog (`catalogProcesses`) with the label `📝 BORRADOR`.
- [ ] All JUnit integration tests and Vitest unit tests pass successfully.
- [ ] Both frontend and backend builds compile cleanly.
- [ ] Git commit conforms to conventional standards and is pushed to the sprint branch.

Required Skills to apply:
- addyosmani_sre_discipline
- addyosmani_planning
- addyosmani_code_review
- yudhi_architecture_compliance
- yudhi_database_migrations
