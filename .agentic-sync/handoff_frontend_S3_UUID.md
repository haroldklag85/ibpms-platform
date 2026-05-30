# 🏗️ Handoff: Frontend - Nuevo campo UUID de Bucket S3 para CA-39

## 1. Metadatos y SSOT
- **Iteración/Sprint:** sprint-7/bugfix-uat
- **User Story:** US-003
- **Criterios de Aceptación:** CA-39 (Condicionamiento de Archivos Adjuntos) - Soporte para configuración S3.
- **Path del SSOT:** `docs/requirements/epics/epic_B_formularios_bpmn.md`
- **Flujo de Trabajo:** Frontend -> QA

## 2. Alineación Arquitectónica y ADRs
- **Validación de ADRs:** Cumplimiento con ADR-006 (Esquema de UI) y ADR-008 (Frontend Vue 3).
- **Lineamientos Transversales:** El campo debe agregarse respetando la reactividad de Vue y el modelo de datos `canvasFields`. Dado que temporalmente no hay conexión real a AWS, este campo se marca en testing (`coverage_matrix.md`), pero su estructura en el JSON `schemaVariables` debe persistir intacta para cuando el backend esté listo.
- **Trazabilidad de la Solución:** La adición del input se realiza exclusivamente dentro del contexto `editingField.type === 'file'`, manteniendo el aislamiento de configuración de otros componentes.

## 3. Rutas Exactas y Contexto Preexistente
- **Archivo a Modificar:** `frontend/src/views/admin/Modeler/FormDesigner.vue`
- **Contexto:** En la sección bajo el comentario `<!-- CA-39: File Upload Constraints -->`, actualmente existen campos para `maxSizeMb`, `allowedExts` y `minFiles`. Se requiere inyectar un nuevo input para capturar el `s3BucketUuid`.

## 4. Snippets Prescriptivos
Agrega este bloque dentro del `div` de configuraciones de archivo (`v-if="editingField.type === 'file'"`), preferiblemente debajo de `minFiles`:

```html
<div class="flex gap-2 mb-2 mt-2">
  <div class="flex-1">
    <label class="block text-xs font-bold text-gray-700 mb-1">UUID de la Bucket S3 (Testing)</label>
    <input type="text" v-model="editingField.s3BucketUuid" class="w-full text-sm border-gray-300 rounded" placeholder="Ej: 550e8400-e29b-41d4-a716-446655440000" />
    <p class="text-[10px] text-gray-500 mt-1">* En fase de testing (falta de conexión real a AWS).</p>
  </div>
</div>
```

## 5. Matriz de QA y Testing Atómico
**Script a Modificar/Crear:** `frontend/tests/FormDesigner.spec.ts` (u homólogo en Playwright)

| Test Name | CA Evaluado | Aserción Esperada |
| :--- | :--- | :--- |
| `Renderiza input S3 Bucket UUID` | CA-39 | Al seleccionar un campo tipo "file", el input para `s3BucketUuid` debe ser visible y actualizable en el DOM. |

## 6. Mensaje de Despacho
Humano, por favor copia y pega el siguiente mensaje para el Agente Frontend:

> **Agente Frontend**, inicia la implementación de este handoff ubicado en `.agentic-sync/handoff_frontend_S3_UUID.md`. Build obligatorio: Ejecuta el protocolo Zero-Trust UI documentado en `.agents/skills/frontend_build_audit/SKILL.md`.
