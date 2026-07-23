# CONTRATO DE DELEGACIÓN ARQUITECTÓNICA (HANDOFF FRONTEND - HOTFIX SPRINT 7)

## 1. Metadatos del Handoff
- **Iteración / Sprint:** Sprint 7 (Hotfix Corrección de Bugs UAT)
- **Historia de Usuario:** US-003
- **IDs de Bugs:** BUG-S7-001-HOTFIX (Bloqueante)
- **Agente Especialista:** Frontend Agent (Vue3 + Pinia + Axios)
- **Arquitecto Delegante:** Arquitecto Líder (IA)
- **Nivel de Severidad:** CRÍTICO (Bloquea UAT y despliegue)
- **Estado Actual:** INVESTIGACIÓN FORENSE COMPLETA

## 2. Descripción del Problema y Contexto

Tras la auditoría y ejecución de pruebas humanas, se evidenciaron dos fallos persistentes:
1. **Mensaje Emergente "FALLIDO: Rule 'too_small'":** El canvas (Virtual DOM) renderiza elementos desactivados sin `v-model`. Cuando el humano teclea datos en el Canvas y da click a "Probar (Submit)", esos datos no llegan a Vue. El método `simulateMockSubmit` cae en el _Fallback Skeleton_ y genera un payload con cadenas vacías `""`. Cuando `executableSchema.safeParse` corre sobre este skeleton, dispara fallos de validación (ej. campos con mínimo de caracteres). Al fallar, se invoca un `return;` que **ABORTA** prematuramente la llamada al API e impide guardar el formulario.
2. **Error 404 del API (Rutas con Doble Prefijo `/api/v1/api/v1/...`):** Aunque corregiste `useFormDesignerStore.ts`, quedaron otras vistas donde `integrationStore` aún invoca las rutas hardcodeadas con `/api/v1`. Como el `apiClient` ya inyecta `baseURL: '/api/v1'`, esto produce un 404 en el proxy de Vite.

## 3. Plan de Solución Arquitectónica (ACCIONES REQUERIDAS)

**Regla de Oro:** NO sobre-reescribas lógica funcional. Limítate a reparar con precisión quirúrgica.

### A. Reparar BUG Modal (FormDesigner.vue)
Abre `frontend/src/views/admin/Modeler/FormDesigner.vue`.
Ve al método `simulateMockSubmit`.
En la lógica del _Fallback Skeleton_ y _safeParse_:
1. Crea una variable bandera `let hasFallbackUsed = false;`.
2. En la condición `if (Object.keys(rawFormSubmission).length === 0)` asigna `hasFallbackUsed = true;`.
3. En la validación `if(!result.success)`, añade la condición para que **SOLO ABORTE** si el usuario proveyó datos reales y fallaron. Si se usó el skeleton (`hasFallbackUsed === true`), haz un `console.warn` y NO retornes, permitiendo que el flujo continúe hacia `integrationStore.post('/forms', dto);`.

### B. Limpiar Doble Prefijo en API Calls
Revisa y corrige los siguientes archivos, removiendo el prefijo `/api/v1` de las llamadas a `integrationStore`:

**1. `frontend/src/views/admin/Modeler/FormList.vue`**
- L112: `integrationStore.get('/api/v1/forms...')` -> `/forms...`
- L125: `integrationStore.delete('/api/v1/forms...')` -> `/forms...`

**2. `frontend/src/views/admin/Integration/DlqDashboard.vue`**
- L220: `integrationStore.get('/api/v1/admin/queues/dlq/summary')` -> `/admin/queues/dlq/summary`
- L231: `integrationStore.get('/api/v1/admin/queues/dlq/messages...`) -> `/admin/queues/dlq/messages...`
- L259: `integrationStore.delete('/api/v1/admin/queues/dlq/purge...`) -> `/admin/queues/dlq/purge...`
- L271: `integrationStore.post('/api/v1/admin/queues/dlq/retry')` -> `/admin/queues/dlq/retry`

**3. `frontend/src/components/forms/generic/EvidenceDropzone.vue`**
- L142: `integrationStore.post('/api/v1/documents/upload-temp...`) -> `/documents/upload-temp...`

## 4. Política Anti-Amnesia (OBLIGATORIA)
- Asegúrate de auto-compilar y ejecutar `npm run build` antes de devolver tu reporte (Backend y Frontend).
- Usa la skill `frontend_build_audit`.

## 5. Salida Esperada
- `git commit -am "fix(BUG-S7-001-HOTFIX): permitir save con skeleton y limpiar doble /api/v1"`
- `git push` a la rama `sprint-7/bugfix-uat`.
- Notificar al Arquitecto Líder (mediante el archivo `approval_request_frontend.md`) solicitando revisión.
