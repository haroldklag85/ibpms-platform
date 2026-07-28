# Handoff Arquitectura - Frontend (Iteración 84-DEV-LANE-ROLE-UAT-R2)

## Metadatos
- **US:** US-005 + US-036
- **Rama:** `DevDavid`
- **Exclusiones:** V2, funcionalidades IA Cognitiva, CRM/Portal, QA automatizado
- **Alineación Arquitectónica:**
  - ADR-002 (Vue 3 Microfrontends): Se respeta la estructura y se corrigen los mapeos a API reales.

## Contexto Técnico
Se han detectado dos bugs críticos en el Frontend en UAT humano (R2-02 y R2-03):
- **R2-02 (Menú desaparece):** El interceptor global de Axios en `apiClient.ts` tiene un catch-all que asume que CUALQUIER 403 sin código es una revocación de privilegios, destruyendo todo el menú (`purgeTopology()`). Esto falla porque el 403 del deploy operacional entra allí.
- **R2-03 (FormDesigner no carga formularios):** En `useFormDesignerStore.ts`, el frontend intenta leer `response.data.schemaVariables`, `response.data.title`, y `response.data.versionId`. ¡Ninguno de esos campos existe en el DTO del backend `FormDesignDTO`! Los correctos son `formFields`, `name`, y `version`.

## Especificaciones Técnicas (Qué hacer)

1. **Corrección de R2-02 en `frontend/src/services/apiClient.ts` (aprox L248-253):**
   - Modifica el interceptor global para que el menú SÓLO se purgue si el error incluye un código de revocación explícito.
   - Reemplaza el bloque catch-all destructivo por:
   ```typescript
   if (error.response?.data?.code === 'ACCESS_REVOKED' || error.response?.data?.code === 'ROLE_REVOKED') {
       console.warn('CA-32: Revocación de acceso confirmada (403). Purgando topología local.');
       const menuStore = useMenuStore();
       menuStore.purgeTopology();
   } else {
       console.warn('CA-32: 403 operacional (no es revocación de privilegios). URL: ' + error.config?.url);
   }
   ```

2. **Corrección de R2-03 en `frontend/src/stores/useFormDesignerStore.ts` (aprox L284-301):**
   - En la función `fetchForm`, mapea los campos que realmente entrega el backend.
   - Código esperado (líneas afectadas):
   ```typescript
   if (response.data && response.data.formFields) {
       canvasFields.value = typeof response.data.formFields === 'string' 
          ? JSON.parse(response.data.formFields) 
          : response.data.formFields;
       
       formTitle.value = response.data.name || formTitle.value;
   ```
   - Y para la versión de esquema en la misma función:
   ```typescript
   currentSchemaVersion.value = response.data.version || 1;
   ```

## Instrucciones y Restricciones
- **REGLA OBLIGATORIA (R2-03):** ANTES de codificar el frontend, DEBES ejecutar `view_file` en el archivo `backend/ibpms-core/src/main/java/com/ibpms/poc/application/dto/FormDesignDTO.java` para constatar los campos reales.
- **REGLA OBLIGATORIA (R2-02):** ANTES de modificar el interceptor, DEBES leer `apiClient.ts` (líneas 208-267 aprox) completo con `view_file` para entender la cadena de `if/else if/else` y NO ROMPER los controles de `PROMPT_INJECTION` o `SECURITY_VIOLATION`.
- ⚠️ **IMPORTANTE:** Todo desarrollo o configuración debe quedar exhaustivamente documentado. Se exige **PRECISIÓN QUIRÚRGICA** en cada cambio para evitar efectos colaterales en funcionalidades existentes. Se han detectado regresiones en iteraciones previas; cualquier daño a funcionalidades adyacentes será motivo de rechazo inmediato. 
- 🚫 **PROHIBICIÓN ESTRICTA:** Queda prohibido modificar `BpmnDesigner.vue` o `IdentityGovernance.vue`. No toques CSS/HTML del panel Lane, ya funciona correctamente con Tailwind.

## Protocolo de Pre-Validación (Criterios de Aceptación Técnicos)
> **📋 DIRECTIVA DE INFRAESTRUCTURA HÍBRIDA:**
> El backend Spring Boot corre en consola local (NO en Docker) en el puerto `8080`. Antes de iniciar tu trabajo, ejecuta el protocolo de pre-validación:
> 1. `curl -s http://localhost:8080/actuator/health` → Debe responder `{"status":"UP"}`.
> 2. Si no responde, arráncalo con: `cd backend && mvn spring-boot:run -pl ibpms-core -Dspring-boot.run.profiles=default`.
> 3. Verifica los servicios Docker: `docker ps` → PostgreSQL (`5433`), Redis (`6379`) y RabbitMQ (`5672`) deben estar `Up (healthy)`.
> **PROHIBIDO** levantar el backend vía Docker o modificar el `docker-compose.yml`.

> 🚫 **Build obligatorio:** Ejecuta el protocolo Zero-Trust UI documentado en `.agents/skills/frontend_build_audit/SKILL.md`.

## Instrucciones Operativas y de Comunicación
1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_frontend.md`.
4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_frontend.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION` y programa la solución.
6. **ANTES del commit final**, actualiza `docs/sprints/gobernanza_pm/CHANGELOG_NO_TECNICO.md` explicando tu trabajo en lenguaje sencillo y amigable para un CEO (QUÉ hiciste y PARA QUÉ sirve). 
7. Finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.

> 📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
> - Aplica estrictamente el protocolo de pruebas **TDD** documentado en `.agents/skills/tdd_first/SKILL.md` (Red -> Green -> Refactor) antes de hacer lógica.
> - Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md` (naming, error handling, modularidad).
