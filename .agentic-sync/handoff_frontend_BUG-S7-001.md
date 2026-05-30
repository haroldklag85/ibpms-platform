# CONTRATO DE DELEGACIÓN ARQUITECTÓNICA (HANDOFF FRONTEND)

## 1. Metadatos del Handoff
- **Iteración / Sprint:** Sprint 7 (Corrección de Bugs UAT)
- **Historia de Usuario:** US-003
- **IDs de Bugs:** BUG-S7-001 (Bloqueante) y error de red 404
- **Rama de Trabajo:** sprint-7/bugfix-uat
- **Arquitecto Responsable:** Líder
- **Rol Asignado:** Frontend Developer (Vue 3 / Vite)

### Alineación Arquitectónica (Gate de Entrada)
- **ADR-002 (Vue 3 Microfrontends):** Se requiere el uso de Pinia para manejar el estado del payload del formulario y la corrección de llamadas HTTP mediante Axios.
- **ADR-006 (Server-Driven UI / Zod):** La validación dinámica de Zod depende de un payload íntegro extraído del motor visual.

## 2. Contexto del Problema
Durante las pruebas humanas (UAT Journey J-02), se detectaron dos fallas críticas en el módulo `FormDesigner`:
1. **Payload Vacío al Probar Submit (BUG-S7-001):** Al hacer clic en "Probar [Submit]" para validar la "Integridad I/O de Camunda", el validador Zod recibe un objeto de payload vacío (`{}`). Esto causa que todos los campos requeridos (`numeroPoliza`, etc.) reporten error `Rule 'invalid_type': Required`. 
2. **Doble Concatenación de Prefijo API (404 Not Found):** En la consola del navegador se observa que el autoguardado en fallback a LocalStorage se activa debido a que las peticiones de red fallan con un error 404 en la ruta `http://localhost:5173/api/v1/api/v1/forms/draft`. Hay una duplicación evidente de `/api/v1`.

## 3. Requerimientos Técnicos (Qué construir)

### A. Corrección de Extracción de Payload (Zod Validation)
- Ubicar el evento de submit de validación en la vista o componente (probablemente en `FormDesigner.vue` o su respectivo store de Pinia).
- Identificar por qué el estado reactivo del formulario (los valores actuales introducidos en el canvas) no se está recolectando o pasando correctamente al esquema Zod al momento de ejecutar `schema.parse()` o `schema.safeParse()`.
- Corregir el binding o recolección para que se extraiga un objeto válido con las llaves correspondientes a cada campo (ej. `{ numeroPoliza: "valor" }`).

### B. Corrección de Doble Prefijo en API
- Rastrear las llamadas de red para guardar borradores (`forms/draft`). 
- Revisar la configuración de `axios` o la constante de URL base en las peticiones. Si ya se cuenta con una configuración global que añade `/api/v1`, asegurarse de que la llamada concreta no lo esté prefijando de nuevo.
- Corregir la ruta para que dispare correctamente a `/api/v1/forms/draft`.

## 4. Criterios de Aceptación a Validar

- **CA-Fix-01:** Al dar clic en "Probar [Submit]" con campos vacíos, Zod debe reportar error solo si el campo es requerido, y al rellenarlos correctamente, la validación Zod debe indicar éxito. El payload evaluado NO debe estar vacío (`{}`) si el formulario tiene inputs.
- **CA-Fix-02:** El guardado automático o manual de borrador (Draft) no debe retornar error 404 por doble concatenación de `/api/v1/api/v1/...`.

## 5. Restricciones Técnicas

> **📋 DIRECTIVA DE INFRAESTRUCTURA HÍBRIDA:**
> El backend Spring Boot corre en consola local (NO en Docker) en el puerto `8080`. Antes de iniciar tu trabajo, ejecuta el protocolo de pre-validación:
> 1. `curl -s http://localhost:8080/actuator/health` → Debe responder `{"status":"UP"}`.
> 2. Si no responde, arráncalo con: `cd backend && mvn spring-boot:run -pl ibpms-core -Dspring-boot.run.profiles=default`.
> 3. Verifica los servicios Docker: `docker ps` → PostgreSQL (`5433`), Redis (`6379`) y RabbitMQ (`5672`) deben estar `Up (healthy)`.
> **PROHIBIDO** levantar el backend vía Docker o modificar el `docker-compose.yml`.

> ⚠️ **IMPORTANTE:** Todo desarrollo o configuración debe quedar exhaustivamente documentado. Se exige **PRECISIÓN QUIRÚRGICA** en cada cambio para evitar efectos colaterales en funcionalidades existentes. Se han detectado regresiones en iteraciones previas; cualquier daño a funcionalidades adyacentes será motivo de rechazo inmediato. 

- *Build obligatorio: Ejecuta el protocolo Zero-Trust UI documentado en `.agents/skills/frontend_build_audit/SKILL.md`.*

## 6. Instrucciones Operativas

**INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_frontend.md`.
4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_frontend.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.
