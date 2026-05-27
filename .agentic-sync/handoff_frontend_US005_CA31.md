# 🏗️ Handoff de Frontend: Etiquetas de Estado en el Catálogo de Procesos (US-005, CA-31)

## 1. Metadatos y SSOT
- **Iteración/Sprint:** Sprint 6
- **Rama de trabajo:** `sprint-6`
- **User Story:** US-005 (Desplegar y Versionar un Modelo de Proceso (BPMN))
- **Criterio de Aceptación:** CA-31 (Etiquetas de Estado en el Catálogo de Procesos)
- **Path del SSOT:** [epic_B_formularios_bpmn.md](file:///c:/Users/HaroltAndrésGómezAgu/ProyectoAntigravity/ibpms-platform/docs/requirements/epics/epic_B_formularios_bpmn.md#L1491)
- **Flujo de Trabajo:** Fase de Implementación (GREEN) -> Verificación con Vitest.

## 2. Alineación Arquitectónica y ADRs
- **ADR 002 (Vue 3 / Componentes Reactivos):** Usar la reactividad y directivas estándar de Vue 3 (`v-if`, interpolation) para renderizar dinámicamente los estados basándose en el modelo de datos.
- **Zero-Mock Policy:** El frontend debe consumir e interpretar la data real mapeada en el componente, sin mocks internos en el HTML.
- **Trazabilidad:** Agregar el comentario obligatorio `// @Traceability: US-005, CA-31 Etiquetas de Estado en el Catálogo` en el bloque HTML o JS modificado.

## 3. Rutas Exactas y Contexto Preexistente
- **Archivo a Modificar:** [BpmnDesigner.vue](file:///c:/Users/HaroltAndrésGómezAgu/ProyectoAntigravity/ibpms-platform/frontend/src/views/admin/Modeler/BpmnDesigner.vue)
- **Ruta/Contexto:** Alrededor del Drawer del Explorador de Procesos (línea 652).
- **Código a reemplazar:**
  ```html
  <span class="text-[10px] font-bold uppercase rounded-full px-2 py-0.5" :class="{'bg-green-100 text-green-800': p.status==='ACTIVO', 'bg-yellow-100 text-yellow-800': p.status==='BORRADOR', 'bg-gray-100 text-gray-700': p.status==='ARCHIVADO'}">{{ p.status }}</span>
  ```

## 4. Snippets Prescriptivos (El "Qué" y el "Cómo")
Reemplaza la línea 652 por la siguiente lógica reactiva que evalúa y formatea el estado usando interpolación en el template:

```html
                      <!-- @Traceability: US-005, CA-31 Etiquetas de Estado en el Catálogo -->
                      <span class="text-[10px] font-bold uppercase rounded-full px-2 py-0.5" :class="{'bg-green-100 text-green-800': p.status==='ACTIVO', 'bg-yellow-100 text-yellow-800': p.status==='BORRADOR', 'bg-gray-100 text-gray-700': p.status==='ARCHIVADO'}">
                        {{ p.status === 'BORRADOR' ? '📝 BORRADOR' : (p.status === 'ACTIVO' ? `✅ ACTIVO (v${p.version})` : (p.status === 'ARCHIVADO' ? '📦 ARCHIVADO' : p.status)) }}
                      </span>
```

## 5. Matriz de QA y Testing Atómico
| Test Name | CA Evaluado | Comportamiento Esperado |
| --- | --- | --- |
| `Debe renderizar las etiquetas visuales de estado exactas en el catálogo de procesos` | CA-31 | El componente renderizará `📝 BORRADOR`, `✅ ACTIVO (v3)` y `📦 ARCHIVADO` en el DOM, pasando la prueba a verde. |

## 6. Mensaje de Despacho
"Build obligatorio: Ejecuta el protocolo Zero-Trust UI documentado en `.agents/skills/frontend_build_audit/SKILL.md`."
