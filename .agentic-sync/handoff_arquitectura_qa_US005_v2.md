# 🧠→🕵️ Handoff: Arquitecto Líder → QA E2E
# US-005-TEST-FIX: Corrección de Payload en Test de Sandbox

**Emitido por:** 🧠 ARQUITECTO LÍDER
**Destinatario:** 🕵️ QA E2E
**Fecha:** 2026-05-23T18:18:00-05:00
**Sprint:** V1 — Certificación Zero-Mock
**Prioridad:** 🔴 Alta
**Dependencia:** Tarea Backend (Bypass de Rol)

## 📖 LECTURAS OBLIGATORIAS (Ejecutar ANTES de codificar)

```bash
# 1. Política del Enjambre (LEY GLOBAL 0, 1, 2, 3)
cat .cursorrules

# 2. Skill principal del agente receptor
cat .agents/skills/qa_e2e_validation_audit/SKILL.md

# 3. Skills transversales aplicables
cat .agents/skills/clean_code_standards/SKILL.md
cat .agents/skills/zero_mock_enforcement/SKILL.md

# 4. Handoff actual (Este documento)
cat .agentic-sync/handoff_arquitectura_qa_US005_v2.md
```

> ⚠️ **LEY GLOBAL 3 — Trazabilidad Inversa:** TODO el código nuevo DEBE llevar la anotación @Traceability o comentario `// @Traceability: US-005, CA-63`. Esto es INNEGOCIABLE.

## 🔬 Diagnóstico del Arquitecto

El Backend está desarrollando la corrección para omitir el control de roles cuando el parámetro `X-Sandbox-Mode` es verdadero. No obstante, la auditoría del test E2E (`us005-bpmn-modeler-persistence.e2e.spec.ts`) detectó un defecto en la capa de QA: el método nativo `fetch` está enviando un body de tipo `application/json`.
El endpoint objetivo (`/api/v1/design/processes/deploy`) exige un formato `multipart/form-data` con el archivo BPMN, lo que detonará un error `HTTP 415 Unsupported Media Type` en cuanto se levante el bloqueo de roles.

| Violación/Hallazgo | Ubicación | Detalle |
|--------------------|:---------:|---------|
| Malformación de Fetch (E2E) | `us005-bpmn-modeler-persistence.e2e.spec.ts:106` | El test utiliza `JSON.stringify` en lugar de `FormData`, contraviniendo la firma del endpoint que requiere un `MultipartFile`. |

## 🎯 Instrucciones Quirúrgicas

### Paso 1: Re-escribir la petición Fetch a FormData

**Archivo:** `frontend/e2e/certification/us005-bpmn-modeler-persistence.e2e.spec.ts`

Sustituye la estructura `JSON.stringify` y la cabecera `Content-Type` por un objeto `FormData` nativo para emular correctamente la subida de archivo que hace la UI de la plataforma.

```typescript
// Snippet prescriptivo — NO es pseudocódigo, es código ejecutable
// @Traceability: US-005, CA-63 Corrección de MediaType para Sandbox E2E
      const status = await page.evaluate(async () => {
        const token = localStorage.getItem('ibpms_token') || 'mock-token';
        try {
          const formData = new FormData();
          const xmlContent = '<?xml version="1.0" encoding="UTF-8"?><bpmn:definitions id="Definitions_1"><bpmn:process id="Process_Sandbox" isExecutable="true"><bpmn:startEvent id="StartEvent_1" /></bpmn:process></bpmn:definitions>';
          const blob = new Blob([xmlContent], { type: 'text/xml' });
          
          formData.append('file', blob, 'sandbox.bpmn');
          formData.append('deploy_comment', 'Sandbox test');
          
          const res = await fetch('/api/v1/design/processes/deploy', {
            method: 'POST',
            headers: {
              'Authorization': `Bearer ${token}`,
              'X-Sandbox-Mode': 'true'
              // Fetch calcula el Content-Type multipart automáticamente cuando pasas FormData
            },
            body: formData
          });
          return res.status;
        } catch (e) {
          return 500;
        }
      });
      expect(status).toBeLessThan(300);
```

## Criterios de Aceptación (DoD)

| # | Criterio | Evidencia |
|---|----------|-----------|
| 1 | El test E2E de Sandbox envía un objeto `FormData` | Inspección de `us005-bpmn-modeler-persistence.e2e.spec.ts` |
| 2 | La ejecución de la suite Playwright muestra PASS (requiere backend actualizado) | `npx playwright test` |
| 3 | Build/Compilación exitosa + Commit en rama | Ejecución limpia del pipeline y commit |

## 🚦 SECUENCIA DE EJECUCIÓN

1. Editar `frontend/e2e/certification/us005-bpmn-modeler-persistence.e2e.spec.ts` aplicando el snippet.
2. Coordinar o esperar el commit del Agente Backend para tener el endpoint corregido.
3. Ejecutar las pruebas E2E con Playwright: `cd frontend && npx playwright test`
4. Commit: `git add . && git commit -m "test(e2e): actualizar fetch a multipart para Sandbox [US-005]" && git push`

## 📋 Instrucciones para Copiar y Pegar

```text
Asume el rol de 🕵️ QA E2E.

ANTES DE HACER CUALQUIER COSA, lee obligatoriamente estos archivos en este orden exacto:

1. cat .cursorrules
2. cat .agents/skills/qa_e2e_validation_audit/SKILL.md
3. cat .agents/skills/clean_code_standards/SKILL.md
4. cat .agents/skills/zero_mock_enforcement/SKILL.md
5. cat .agentic-sync/handoff_arquitectura_qa_US005_v2.md

TU MISIÓN:

1. Modificar el script de certificación E2E `us005-bpmn-modeler-persistence.e2e.spec.ts` (Sección CA-63).
2. Reemplazar la subida en formato JSON por `FormData` (siguiendo el snippet del handoff), ya que el endpoint backend consume MultipartFile.
3. Ejecutar las pruebas E2E con Playwright tras la remediación del backend: `cd frontend && npx playwright test`
4. Commit: `git add .; git commit -m "test(e2e): actualizar fetch a multipart para Sandbox [US-005]"`

REGLAS INQUEBRANTABLES:
- DEBES remover la declaración estática de `Content-Type: application/json` del encabezado `fetch`.
- PROHIBIDO cerrar la US si aún hay fallos HTTP 4xx o 5xx.
```
