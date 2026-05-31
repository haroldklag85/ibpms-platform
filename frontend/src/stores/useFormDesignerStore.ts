import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import apiClient from '@/services/apiClient';
import { ZodBuilder } from '@/views/admin/Modeler/ZodBuilder';
import { useAuthStore } from '@/stores/authStore';
// @ts-ignore
import jexl from 'jexl';

export const useFormDesignerStore = defineStore('formDesigner', () => {
  // State
  const canvasFields = ref<any[]>([]);
  const formTitle = ref('Solicitud Onboarding (V1)');
  const formPattern = ref<'SIMPLE' | 'IFORM_MAESTRO' | null>(null);
  const activeStageSim = ref('ALL');
  const visualRules = ref<{fieldA: string, operator: string, fieldB: string, errorMessage: string}[]>([]);
  const formVersions = ref<any[]>([]);
  const isPublic = ref(false);
  const certificationState = ref<'none' | 'certified' | 'revoked'>('none');
  const currentSchemaVersion = ref(1);
  const bpmnCoherenceResults = ref<any[]>([]);
  const formKey = ref('');
  const zodParseError = ref<boolean | string>(false);
  
  // AI State
  const aiPrompt = ref('');
  const isScanningAi = ref(false);

  // Fuzzer State
  const fuzzerErrors = ref<{msg: string, isRefine: boolean}[]>([]);
  const superRefineCount = computed(() => visualRules.value.length);

  const toolboxCategories = ref([
    {
      name: "Mis Fragmentos",
      items: [] as any[]
    },
    {
      name: "Texto",
      items: [
        { icon: 'Ab', label: 'Input Text', desc: 'Validación Regex', type: 'text', placeholder: 'Ej: Juan Pérez', required: true, zodType: 'string', camundaVariable: '' },
        { icon: '🔑', label: 'Password', desc: 'Dato Sensible (CA-53)', type: 'password', placeholder: 'Ingrese contraseña', required: true, zodType: 'string', camundaVariable: '' },
        { icon: '📧', label: 'Email', desc: 'Validación Zod .email()', type: 'email', placeholder: 'correo@ejemplo.com', required: true, zodType: 'string', camundaVariable: '' },
        { icon: '🔗', label: 'URL', desc: 'Validación Zod .url()', type: 'url', placeholder: 'https://ejemplo.com', required: false, zodType: 'string', camundaVariable: '' },
        { icon: '📝', label: 'Long Text', desc: 'Textarea (2+ filas)', type: 'textarea', placeholder: 'Comentarios...', required: false, zodType: 'string', camundaVariable: '' },
      ]
    },
    {
      name: "Numérico & Fechas",
      items: [
        { icon: '#', label: 'Number Field', desc: 'Zod min/max', type: 'number', placeholder: '0.00', required: true, zodType: 'number', camundaVariable: '' },
        { icon: '📅', label: 'Date Picker', desc: 'DD/MM/YYYY', type: 'date', placeholder: 'Seleccionar Fecha', required: false, zodType: 'string', camundaVariable: '' },
        { icon: '⏰', label: 'Time Picker', desc: 'HH:MM AM/PM', type: 'time', placeholder: 'Seleccionar Hora', required: false, zodType: 'string', camundaVariable: '' },
      ]
    },
    {
      name: "Selección",
      items: [
        { icon: '≡', label: 'Dropdown', desc: 'Soporta Array CSV', type: 'select', placeholder: '-- Seleccione --', required: true, zodType: 'string', options: ['Opción A', 'Opción B'], camundaVariable: '' },
        { icon: '🔄', label: 'Async Typeahead', desc: 'API Fetch (CA-30)', type: 'async_select', placeholder: 'Buscar en API...', required: true, zodType: 'string', asyncUrl: '', camundaVariable: '' },
        { icon: '☑️', label: 'Checkbox', desc: 'Booleano Múltiple', type: 'checkbox', placeholder: 'Marcar opción', required: false, zodType: 'boolean', camundaVariable: '' },
        { icon: '🔘', label: 'Radio Button', desc: 'Opción Única', type: 'radio', placeholder: '', required: true, zodType: 'string', options: ['Opción 1', 'Opción 2'], camundaVariable: '' },
      ]
    },
    {
      name: "Avanzados",
      items: [
        { icon: '📎', label: 'File Upload', desc: 'SGDEA Vault Embed', type: 'file', placeholder: 'Arrastra PDF aquí', required: false, zodType: 'any', camundaVariable: '' },
        { icon: '✍️', label: 'Firma Digital', desc: 'Canvas HTML5 (CA-31)', type: 'signature', placeholder: 'Dibuja tu firma', required: true, zodType: 'string', camundaVariable: '' },
        { icon: '📌', label: 'GPS Geolocation', desc: 'Coordenadas HTML5 (CA-61)', type: 'gps', placeholder: 'Ubicación...', required: true, zodType: 'string', camundaVariable: '' },
        { icon: '📷', label: 'Scan QR', desc: 'WebRTC Dummy (CA-62)', type: 'qr', placeholder: 'Código QR...', required: true, zodType: 'string', camundaVariable: '' },
      ]
    },
    {
      name: "Layouts (CA-8, CA-34)",
      items: [
        { icon: '🗂️', label: 'Contenedor', desc: 'Panel Agrupador', type: 'container', placeholder: 'Nueva Sección de Datos', required: false, zodType: 'object', camundaVariable: '', children: [] },
        { icon: '📇', label: 'Pestañas (Tabs)', desc: 'Multivista Horizontal', type: 'tabs', placeholder: 'Contenedor de Pestañas', required: false, zodType: 'object', camundaVariable: '', activeTab: 0, children: [] },
        { icon: '↕️', label: 'Acordeón', desc: 'Paneles Colapsables', type: 'accordion', placeholder: 'Acordeón Estructurado', required: false, zodType: 'object', camundaVariable: '', children: [] },
        { icon: '📑', label: 'Data Grid', desc: 'Fila Repetible', type: 'field_array', placeholder: 'Nueva Tabla', required: false, zodType: 'array', camundaVariable: '', children: [] },
        { icon: 'ℹ️', label: 'Modal Informativo', desc: 'Teleport Z-900 (Estéril)', type: 'info_modal', placeholder: 'Contenido del modal...', tooltipText: 'Título del Pop-up', required: false, zodType: 'none', camundaVariable: '' },
        { icon: '👁️‍🗨️', label: 'Hidden Input', desc: 'ID/Token Silencioso (CA-47)', type: 'hidden', placeholder: '', required: false, zodType: 'any', camundaVariable: '' }
      ]
    },
    {
      name: "Accionadores (CA-14)",
      items: [
        { icon: '💾', label: 'Guardar Borrador', desc: 'API DRAFT', type: 'button_draft', placeholder: '', required: false, zodType: 'none', camundaVariable: '' },
        { icon: '✅', label: 'Completar Tarea', desc: 'API POST Complete', type: 'button_submit', placeholder: '', required: false, zodType: 'none', camundaVariable: '' },
        { icon: '❌', label: 'Rechazar Tarea', desc: 'BPMN Error', type: 'button_reject', placeholder: '', required: false, zodType: 'none', camundaVariable: '' },
      ]
    }
  ]);

  // Context & UI State
  const authStore = useAuthStore();
  const simulatorContext = ref({ rbacRole: authStore.roles?.[0] || 'ADMIN' });
  const activeCodeTab = ref<'TEMPLATE' | 'SCRIPT' | 'ZOD' | 'STYLE' | 'JSON'>('TEMPLATE');
  const localJsonCode = ref('');
  const editingField = ref<any>(null);
  const idCounter = ref(1);

  // Actions
  const generateAiForm = async (promptText: string) => {
    if (!promptText) return;
    isScanningAi.value = true;
    try {
        // BUG-S7-001 / BUG-B FIX: baseURL del apiClient ya incluye /api/v1 — rutas relativas sin prefijo
        const response = await apiClient.post('/design/forms/generate', { prompt: promptText });
        if (response.data && response.data.schema) {
            canvasFields.value = typeof response.data.schema === 'string' ? JSON.parse(response.data.schema) : response.data.schema;
            return { success: true, message: 'Formulario generado por LMM con éxito' };
        }
    } catch(e) {
        return { success: false, message: 'Falla de conexión LMM (CA-73)' };
    } finally {
        isScanningAi.value = false;
    }
  };

  const saveAsFragment = (node: any) => {
    const fragmentCategory = toolboxCategories.value.find(c => c.name === 'Mis Fragmentos');
    if (fragmentCategory) {
       fragmentCategory.items.push(JSON.parse(JSON.stringify(node)));
       localStorage.setItem('workdesk_fragments', JSON.stringify(fragmentCategory.items));
    }
  };

  const fetchVersions = async () => {
    try {
        const res = await apiClient.get('/forms/mock_id_or_draft/versions'); // BUG-S7-001 / BUG-B FIX
        formVersions.value = res.data;
    } catch(e) {
        // Mock fallback for UI Demo if API is not fully seeded
        formVersions.value = [
           { id: 'v2.1', version: '2.1', updatedAt: new Date().toISOString() },
           { id: 'v1.0', version: '1.0', updatedAt: new Date(Date.now() - 86400000).toISOString() }
        ];
    }
  };

  const fetchForm = async (formId: string) => {
    try {
        const response = await apiClient.get(`/forms/${formId}`); // BUG-S7-001 / BUG-B FIX
        if (response.data && response.data.schemaVariables) {
            canvasFields.value = typeof response.data.schemaVariables === 'string' 
               ? JSON.parse(response.data.schemaVariables) 
               : response.data.schemaVariables;
            
            formTitle.value = response.data.title || response.data.name || formTitle.value;
            formPattern.value = response.data.pattern || null;
            
            if (response.data.isQaCertified) certificationState.value = 'certified';
            else if (response.data.certifiedSchemaHash) certificationState.value = 'revoked';
            currentSchemaVersion.value = response.data.versionId || 1;

            return { success: true, message: `Formulario ${formId} cargado desde API` };
        }
    } catch(e) {
        return { success: false, message: 'Error cargando formulario remoto desde API' };
    }
  };

  const checkBpmnCoherence = async (availableFieldsFlat: any[]) => {
    if (formKey.value) {
        try {
            const res = await apiClient.getBpmnVariables(formKey.value);
            const bpmnVars = res.data as string[];
            const zodFields = availableFieldsFlat.map(f => f.camundaVariable || f.id);
            
            bpmnCoherenceResults.value = [];
            
            bpmnVars.forEach(v => {
                if (zodFields.includes(v)) {
                   bpmnCoherenceResults.value.push({ name: v, icon: '✅', label: `Variable BPMN '${v}' → Campo Zod '${v}'`, class: 'text-green-400' });
                } else {
                   bpmnCoherenceResults.value.push({ name: v, icon: '⚠️', label: `Variable BPMN '${v}' → No encontrada en esquema Zod`, class: 'text-yellow-400' });
                }
            });
            
            zodFields.forEach(v => {
                if (!bpmnVars.includes(v)) {
                   bpmnCoherenceResults.value.push({ name: v, icon: 'ℹ️', label: `Campo Zod '${v}' → No declarado en BPMN`, class: 'text-blue-400' });
                }
            });
            
        } catch (e) {
            console.error("Error CA-17", e);
        }
    }
  };

  const runFuzzerZod = (fuzzerPayload: string) => {
    fuzzerErrors.value = [];
    if (fuzzerPayload.length > 50000) {
        fuzzerErrors.value = [{ msg: '[SECURITY BLOCK] - Límite de payload superado (Max 50KB). DDoS Prevention.', isRefine: false }];
        return { success: false, message: 'Payload abortado por políticas de firewall de capa 7.' };
    }
    try {
        const payload = JSON.parse(fuzzerPayload);
        const schema = ZodBuilder.buildSchema(canvasFields.value, visualRules.value);
        const result = schema.safeParse(payload);
        if (!result.success) {
            fuzzerErrors.value = result.error.issues.map(iss => {
                const isCrossField = visualRules.value.some(r => iss.path.includes(r.fieldA) || iss.path.includes(r.fieldB));
                return {
                    msg: `[${iss.path.join('.')}] - ${iss.message}`,
                    isRefine: isCrossField
                };
            });
            return { success: false, message: 'Payload con errores' };
        } else {
            return { success: true, message: 'Payload Válido 🎉' };
        }
    } catch(e: any) {
        fuzzerErrors.value = [{ msg: `[JSON Syntax Error] - ${e.message}`, isRefine: false }];
        return { success: false, message: 'Error de sintaxis JSON' };
    }
  };

  const restoreVersion = (ver: any) => {
    if (ver.schema) {
        canvasFields.value = typeof ver.schema === 'string' ? JSON.parse(ver.schema) : ver.schema;
        return { success: true, message: `Versión ${ver.version} restaurada exitosamente` };
    }
    const localDraft = localStorage.getItem('designer_draft_fallback');
    if (localDraft) {
        try {
            canvasFields.value = JSON.parse(localDraft);
            return { success: true, message: `Recuperación Forense Exitosa (${ver.version})` };
        } catch (e) {
            return { success: false, message: 'Memoria fría corrupta' };
        }
    } else {
        return { success: false, message: 'No hay huella forense en disco local' };
    }
  };

  const saveDraftToApi = async (title: string, rules: any) => {
      try {
          await apiClient.post('/forms/draft', { schema: canvasFields.value, title, formRules: rules }); // BUG-S7-001 / BUG-B FIX
          console.log('✅ Diseño auto-guardado en API (Modelador)');
      } catch (e) {
          localStorage.setItem('designer_draft_fallback', JSON.stringify(canvasFields.value));
          console.warn('⚠️ Fallback a LocalStorage activado para autoguardado del modelador');
      }
  };

  const cloneComponent = (original: any) => {
    const cloned = JSON.parse(JSON.stringify(original));
    cloned.id = `FIELD_${idCounter.value++}`;
    cloned.camundaVariable = cloned.id.toLowerCase();
    cloned.stage = activeStageSim.value === 'ALL' ? 'START_EVENT' : activeStageSim.value;
    if (cloned.type === 'container' || cloned.type === 'field_array') {
      cloned.children = [];
    }
    if (cloned.type === 'tabs') {
      cloned.children = [
        { id: `FIELD_${idCounter.value++}_tab1`, label: 'Tab 1', type: 'tab_pane', children: [] },
        { id: `FIELD_${idCounter.value++}_tab2`, label: 'Tab 2', type: 'tab_pane', children: [] }
      ];
      cloned.activeTab = 0;
    }
    if (cloned.type === 'accordion') {
      cloned.children = [
        { id: `FIELD_${idCounter.value++}_panel1`, label: 'Panel 1', type: 'accordion_panel', children: [] },
        { id: `FIELD_${idCounter.value++}_panel2`, label: 'Panel 2', type: 'accordion_panel', children: [] }
      ];
    }
    return cloned;
  };

  const evaluateMockVis = (node: any) => {
    if (!node.visibilityCondition) return true;
    try {
        return jexl.evalSync(node.visibilityCondition, { data: {}, context: simulatorContext.value });
    } catch {
        return true; 
    }
  };

  const flatFields = (fields: any[]): any[] => {
    let res: any[] = [];
    for (const f of fields) {
      if (f.type === 'container' || f.type === 'field_array') {
        if (f.children) res = res.concat(flatFields(f.children));
      } else {
        res.push(f);
      }
    }
    return res;
  };

  const availableStages = computed(() => {
    const stages = new Set<string>();
    const flatF = flatFields(canvasFields.value);
    flatF.forEach(f => {
      if (f.stage && f.stage !== 'START_EVENT' && f.stage !== 'ALL' && f.stage !== 'ANALYSIS' && f.stage !== 'DECISION') {
        stages.add(f.stage);
      }
    });
    return Array.from(stages);
  });

  const attemptTabChange = (targetTab: 'TEMPLATE' | 'SCRIPT' | 'ZOD' | 'STYLE' | 'JSON') => {
    if (activeCodeTab.value === 'JSON') {
        try {
            const parsed = JSON.parse(localJsonCode.value || JSON.stringify(canvasFields.value));
            canvasFields.value = parsed;
            zodParseError.value = false;
        } catch (e: any) {
            zodParseError.value = true;
            return { success: false, message: 'BARRICADA JSON: Estructura malformada. ' + e.message };
        }
    } else if (targetTab === 'JSON') {
        localJsonCode.value = JSON.stringify(canvasFields.value, null, 2);
    }
    activeCodeTab.value = targetTab;
    return { success: true };
  };

  const generateMockPath = (type: string, fuzzerPayloadRef: any) => {
    const buildMock = (fields: any[]): any => {
      let mock: any = {};
      fields.forEach(f => {
        if (f.type.startsWith('button_') || f.type === 'tabs' || f.type === 'accordion') return;
        
        const key = f.camundaVariable || f.id;
        
        if (f.type === 'container') {
          const childMocks = buildMock(f.children || []);
          Object.assign(mock, childMocks); // Flatten objects from container
        } else if (f.type === 'field_array') {
          // Zod validation for arrays requires an array of objects
          mock[key] = [buildMock(f.children || [])];
        } else {
          if (type === 'happy') {
            if (f.type === 'number' || f.type === 'timer') mock[key] = 42;
            else if (f.type === 'checkbox') mock[key] = true;
            else if (f.type === 'email') mock[key] = 'test@example.com';
            else if (f.type === 'url') mock[key] = 'https://example.com';
            else if (f.isMultiple) mock[key] = ['Option1'];
            else if (f.type === 'file' || f.type === 'signature') mock[key] = '550e8400-e29b-41d4-a716-446655440000';
            else mock[key] = 'Dummy Data';
          } else {
            mock[key] = null;
          }
        }
      });
      return mock;
    };
    
    fuzzerPayloadRef.value = JSON.stringify(buildMock(canvasFields.value), null, 2);
  };

  const certifyForm = async (formId: string, payload: string) => {
    // @Traceability: US-028 - CA-11 - Certificación de Contrato Zod
    try {
        const response = await apiClient.post(`/design/forms/${formId}/certify`, { payload }); // BUG-S7-001 / BUG-B FIX
        certificationState.value = 'certified';
        return { success: true, message: 'Contrato Zod Certificado con Éxito 🏆' };
    } catch(e: any) {
        if (e.response?.status === 409) {
            return { success: false, message: e.response.data?.message || 'Conflicto en la certificación (409)' };
        }
        return { success: false, message: 'Falla al certificar el formulario' };
    }
  };

  const generateVitestSpec = (availableFieldsFlat: any[]) => {
    let specStr = `import { describe, it, expect } from 'vitest';\n`;
    specStr += `import { taskSchema } from './${formTitle.value.replace(/[^a-zA-Z0-9]/g, '')}Schema';\n\n`;
    specStr += `describe('Form Validation: ${formTitle.value}', () => {\n`;
    
    specStr += `  it('debe aceptar un payload Happy Path con todos los campos requeridos', () => {\n`;
    specStr += `    const validData = {\n`;
    availableFieldsFlat.forEach(f => {
        const key = f.camundaVariable || f.id;
        if(f.required) {
            if(f.type === 'number' || f.type === 'timer') specStr += `      ${key}: 42,\n`;
            else if(f.type === 'checkbox') specStr += `      ${key}: true,\n`;
            else if(f.type === 'email') specStr += `      ${key}: 'test@test.com',\n`;
            else if(f.type === 'url') specStr += `      ${key}: 'https://test.com',\n`;
            else if(f.isMultiple) specStr += `      ${key}: ['Option1'],\n`;
            else if(f.type === 'file' || f.type === 'signature') specStr += `      ${key}: '550e8400-e29b-41d4-a716-446655440000',\n`;
            else specStr += `      ${key}: 'Dummy Data',\n`;
        }
    });
    specStr += `    };\n`;
    specStr += `    const result = taskSchema.safeParse(validData);\n`;
    specStr += `    expect(result.success).toBe(true);\n`;
    specStr += `  });\n\n`;

    availableFieldsFlat.filter(f => f.required).forEach(f => {
        const key = f.camundaVariable || f.id;
        specStr += `  it('debe fallar si falta el campo requerido: ${key}', () => {\n`;
        specStr += `    const invalidData = { /* Omitir ${key} deliberadamente */ };\n`;
        specStr += `    const result = taskSchema.safeParse(invalidData);\n`;
        specStr += `    expect(result.success).toBe(false);\n`;
        specStr += `  });\n\n`;
    });

    specStr += `});\n`;

    const blob = new Blob([specStr], { type: 'text/typescript' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${formTitle.value.replace(/[^a-zA-Z0-9]/g, '')}.spec.ts`;
    a.click();
    URL.revokeObjectURL(url);
  };

  const generateFieldHTML = (field: any, indent: string = '      ', parentBinding: string = 'formData'): string => {
    let tpl = '';
    
    if (field.type.startsWith('button_')) {
        tpl += `${indent}<div class="mt-6 field-${field.id.toLowerCase()} no-print" v-if="(typeof isAuditMode === 'undefined' ? false : !isAuditMode) && (typeof stage === 'undefined' ? true : stage !== 'AUDIT')">\n`;
        if (field.type === 'button_submit') {
          tpl += `${indent}  <button type="submit" class="w-full bg-indigo-600 text-white py-2 rounded shadow font-bold hover:bg-indigo-700 transition flex items-center justify-center gap-2" :disabled="typeof isAsyncLoading !== 'undefined' && isAsyncLoading">✅ ${field.label}</button>\n`;
        } else if (field.type === 'button_draft') {
          tpl += `${indent}  <button type="button" @click="saveDraft" class="w-full border-2 border-dashed border-gray-300 text-gray-700 py-2 rounded shadow-sm font-bold hover:bg-gray-100 transition flex items-center justify-center gap-2" :disabled="typeof isAsyncLoading !== 'undefined' && isAsyncLoading">💾 ${field.label}</button>\n`;
        } else if (field.type === 'button_reject') {
          tpl += `${indent}  <button type="button" @click="rejectTask" class="w-full bg-red-600 text-white py-2 rounded shadow-sm font-bold hover:bg-red-700 transition mt-2 flex items-center justify-center gap-2" :disabled="typeof isAsyncLoading !== 'undefined' && isAsyncLoading">❌ ${field.label}</button>\n`;
        }
        tpl += `${indent}</div>\n`;
        return tpl;
    }

    let vIfDir = '';
    if (field.visibilityCondition) {
        if (formPattern.value === 'IFORM_MAESTRO') {
           vIfDir = `v-if="stage === '${field.stage}' && (${field.visibilityCondition})" `;
        } else {
           vIfDir = `v-if="${field.visibilityCondition}" `;
        }
    } else if (formPattern.value === 'IFORM_MAESTRO') {
        vIfDir = `v-if="stage === '${field.stage}'" `;
    }

    const vModelBase = parentBinding === 'formData' 
      ? `formData.${field.camundaVariable || field.id}` 
      : `row.${field.camundaVariable || field.id}`;

    if (field.type === 'container' || field.type === 'field_array') {
       let containerClass = `${field.type === 'field_array' ? 'border-2 border-indigo-100' : 'border'} rounded-md p-4 bg-gray-50 field-${field.id.toLowerCase()}`;
       if (field.type === 'container' && field.columns && field.columns > 1) {
           containerClass += ` grid grid-cols-${field.columns} gap-4`; // CA-55
       }
       tpl += `${indent}<div ${vIfDir}class="${containerClass}">\n`;
       tpl += `${indent}  <h3 class="font-bold text-md mb-4">${field.label || 'Sección'}</h3>\n`;
       
       if (field.type === 'field_array') {
          tpl += `${indent}  <div v-for="(row, index) in ${vModelBase}" :key="index" class="p-4 border border-gray-200 bg-white mb-3 rounded isolate relative">\n`;
          tpl += `${indent}    <button type="button" @click="${vModelBase}.splice(index, 1)" class="absolute top-2 right-2 text-red-500 hover:text-red-700 font-bold no-print" title="Eliminar Fila">🗑</button>\n`;
       }

       if (field.children && field.children.length > 0) {
         for(const child of field.children) {
           if (field.type === 'field_array') tpl += generateFieldHTML(child, indent + '    ', 'row');
           else tpl += generateFieldHTML(child, indent + '  ', parentBinding);
         }
       }
       
       if (field.type === 'field_array') {
          tpl += `${indent}  </div>\n`;
          tpl += `${indent}  <button type="button" @click="${vModelBase}.push({})" class="text-sm border-2 border-dashed border-indigo-300 text-indigo-700 px-4 py-2 rounded hover:bg-indigo-50 font-bold w-full mt-2 no-print">[+ Agregar Fila]</button>\n`;
       }
       tpl += `${indent}</div>\n`;
    } else {
      tpl += `${indent}<div ${vIfDir}class="field-${field.id.toLowerCase()}">\n`;
      const ttip = field.tooltipText ? ` <span title="${field.tooltipText}" class="cursor-help text-indigo-500 font-bold ml-1 text-xs outline-none">ⓘ</span>` : '';
      tpl += `${indent}  <label class="block text-sm font-medium text-gray-700">${field.label}${field.required ? '*' : ''}${ttip}</label>\n`;
      
      // CA-56 Print Mode Wrapper
      tpl += `${indent}  <div v-if="!isPrintMode">\n`;

      const dsbObj = formPattern.value === 'IFORM_MAESTRO' ? `isAuditMode || stage === 'AUDIT' || (stage !== '${field.stage}' && ${field.soloLecturaPosterior || false})` : `isAuditMode`;
      const finalDsbObj = field.disableCondition ? `(${dsbObj}) || (${field.disableCondition})` : dsbObj; // CA-57
      const dsb = parentBinding === 'row' ? ` :disabled="${finalDsbObj} || row._locked"` : ` :disabled="${finalDsbObj}"`; // CA-51 Grid Locked Rows
      
      if (field.type === 'text' || field.type === 'number' || field.type === 'date' || field.type === 'time' || field.type === 'password' || field.type === 'email' || field.type === 'url') { // CA-53, CA-63
        if (field.mask) {
           // CA-36: Proxy Value/Event Masking
           tpl += `${indent}  <input type="${field.type === 'password' ? 'password' : 'text'}" :value="formatMask(${vModelBase}, '${field.mask}')" @change="(e) => { ${vModelBase} = unmask((e.target as HTMLInputElement).value, '${field.type}'); validateField('${field.camundaVariable || field.id}'); }" placeholder="${field.placeholder || field.mask}" class="form-input mt-1 w-full rounded-md border-gray-300 shadow-sm font-mono"${dsb} />\n`;
        } else {
           const nativeType = (field.type === 'email' || field.type === 'url' || field.type === 'password') ? field.type : field.type;
           tpl += `${indent}  <input type="${nativeType}" v-model.lazy="${vModelBase}" @blur="validateField('${field.camundaVariable || field.id}')" placeholder="${field.placeholder || ''}" class="form-input mt-1 w-full rounded-md border-gray-300 shadow-sm"${dsb} />\n`;
        }
        if (field.type === 'password') {
           // CA-64 Hints Multi-Estado
           tpl += `${indent}  <div class="mt-1 text-xs px-1 space-y-1 font-mono font-medium" v-if="${vModelBase}">\n`;
           tpl += `${indent}    <p :class="${vModelBase}.length >= 8 ? 'text-green-600' : 'text-gray-500'">Mínimo 8 caracteres {{${vModelBase}.length >= 8 ? '✅' : '❌'}}</p>\n`;
           tpl += `${indent}    <p :class="/[A-Z]/.test(${vModelBase}) ? 'text-green-600' : 'text-gray-500'">1 Mayúscula {{/[A-Z]/.test(${vModelBase}) ? '✅' : '❌'}}</p>\n`;
           tpl += `${indent}  </div>\n`;
        }
      } else if (field.type === 'textarea') {
        tpl += `${indent}  <textarea v-model.lazy="${vModelBase}" @blur="validateField('${field.camundaVariable || field.id}')" placeholder="${field.placeholder || ''}" class="form-input mt-1 w-full rounded-md border-gray-300 shadow-sm" rows="3"${dsb}></textarea>\n`;
      } else if (field.type === 'checkbox') {
        tpl += `${indent}  <div class="flex items-center gap-2 mt-1">\n${indent}    <input type="checkbox" v-model="${vModelBase}" class="rounded text-indigo-600 border-gray-300 focus:ring-indigo-500 shadow-sm"${dsb} />\n${indent}    <span class="text-sm text-gray-700">${field.placeholder || field.label}</span>\n${indent}  </div>\n`;
      } else if (field.type === 'radio') {
        tpl += `${indent}  <div class="flex flex-col gap-1 mt-1">\n${(field.options || ['Opción 1', 'Opción 2']).map((o:string) => `${indent}    <label class="flex items-center gap-2"><input type="radio" value="${o}" v-model="${vModelBase}" class="text-indigo-600 border-gray-300 focus:ring-indigo-500 shadow-sm"${dsb} /> <span class="text-sm text-gray-600 font-medium">${o}</span></label>`).join('\n')}\n${indent}  </div>\n`;
      } else if (field.type === 'select' || field.type === 'async_select') {
         if (field.isMultiple) {
             // CA-45: Multi Select Chips
             tpl += `${indent}  <div class="relative">\n`;
             if (field.type === 'select') {
                 tpl += `${indent}    <input list="list-${field.id}" @change="(e) => { const val = (e.target as HTMLInputElement).value; if(val && !${vModelBase}.includes(val)) { ${vModelBase}.push(val); (e.target as HTMLInputElement).value=''; } }" placeholder="${field.placeholder || 'Seleccione múltiple...'}" class="form-input mt-1 w-full rounded-md border-gray-300 shadow-sm"${dsb} />\n`;
                 tpl += `${indent}    <datalist id="list-${field.id}">\n${(field.options || ['Opción 1', 'Opción 2']).map((o:string) => `${indent}      <option value="${o}">${o}</option>`).join('\n')}\n${indent}    </datalist>\n`;
             } else {
                 tpl += `${indent}    <input list="list-${field.id}" @input="(e) => fetchAsyncOpts_${field.id}((e.target as HTMLInputElement).value)" @change="(e) => { const val = (e.target as HTMLInputElement).value; if(val && !${vModelBase}.includes(val)) { ${vModelBase}.push(val); (e.target as HTMLInputElement).value=''; } }" placeholder="${field.placeholder || 'Buscando en servidor...'}" class="form-input mt-1 w-full rounded-md border-gray-300 shadow-sm"${dsb} />\n`;
                 tpl += `${indent}    <datalist id="list-${field.id}">\n${indent}      <option v-for="opt in asyncOpts_${field.id}" :key="opt" :value="opt"></option>\n${indent}    </datalist>\n`;
             }
             tpl += `${indent}    <div class="flex flex-wrap gap-2 mt-2">\n`;
             tpl += `${indent}       <span v-for="(chip, idx) in ${vModelBase}" :key="chip" class="bg-indigo-100 text-indigo-800 text-xs px-2 py-1 rounded-full flex items-center gap-1 shadow-sm">\n`;
             tpl += `${indent}         {{ chip }}\n`;
             tpl += `${indent}         <button type="button" @click="${vModelBase}.splice(idx, 1)" class="font-bold hover:text-indigo-900 border-l border-indigo-200 pl-1 ml-1"${dsb}>&times;</button>\n`;
             tpl += `${indent}       </span>\n`;
             tpl += `${indent}    </div>\n`;
             tpl += `${indent}  </div>\n`;
         } else {
             if (field.type === 'select') {
                 tpl += `${indent}  <input list="list-${field.id}" v-model="${vModelBase}" placeholder="${field.placeholder || 'Seleccione...'}" class="form-input mt-1 w-full rounded-md border-gray-300 shadow-sm"${dsb} />\n`;
                 tpl += `${indent}  <datalist id="list-${field.id}">\n${(field.options || ['Opción 1', 'Opción 2']).map((o:string) => `${indent}    <option value="${o}">${o}</option>`).join('\n')}\n${indent}  </datalist>\n`;
             } else {
                 tpl += `${indent}  <input list="list-${field.id}" @input="(e) => fetchAsyncOpts_${field.id}((e.target as HTMLInputElement).value)" v-model="${vModelBase}" placeholder="${field.placeholder || 'Buscando en servidor...'}" class="form-input mt-1 w-full rounded-md border-gray-300 shadow-sm"${dsb} />\n`;
                 tpl += `${indent}  <datalist id="list-${field.id}">\n${indent}    <option v-for="opt in asyncOpts_${field.id}" :key="opt" :value="opt"></option>\n${indent}  </datalist>\n`;
             }
         }
      } else if (field.type === 'file') {
         const uTarget = parentBinding === 'formData' ? 'formData.value' : parentBinding;
         // CA-39: Binding de MaxSizeMb y AllowedExts
         const maxMb = field.maxSizeMb || 0;
         const exts = field.allowedExts || '';
         const minFs = field.minFiles || 0;
         const maxFs = field.maxFiles || 1;
         const multAttr = maxFs > 1 ? ' multiple' : '';
         
         // CA-60 Dropzone wrapper
         tpl += `${indent}  <div class="border-2 border-dashed border-gray-300 rounded-lg p-6 bg-gray-50 hover:bg-gray-100 transition text-center cursor-pointer relative" @dragover.prevent @drop.prevent="(e) => dropFile(e, '${field.camundaVariable || field.id}', ${uTarget}, ${maxMb}, '${exts}', ${minFs}, ${maxFs})">\n`;
         tpl += `${indent}     <span class="text-3xl mb-2 block">📥</span>\n`;
         tpl += `${indent}     <p class="text-sm font-bold text-gray-700">Arrastre archivos aquí (CA-60)</p>\n`;
         tpl += `${indent}     <p class="text-xs text-gray-500 mt-1 mb-3">o haga clic para seleccionar desde el navegador.</p>\n`;
         tpl += `${indent}     <input type="file" @change="(e) => uploadFile(e, '${field.camundaVariable || field.id}', ${uTarget}, ${maxMb}, '${exts}', ${minFs}, ${maxFs})" class="absolute inset-0 w-full h-full opacity-0 cursor-pointer no-print"${dsb}${multAttr} />\n`;
         tpl += `${indent}     <div v-if="${vModelBase}" class="mt-2 text-xs text-indigo-700 bg-indigo-50 py-1 px-2 rounded font-bold break-all border border-indigo-200">\n`;
         tpl += `${indent}       Archivo(s): {{ Array.isArray(${vModelBase}) ? ${vModelBase}.join(', ') : ${vModelBase} }}\n`;
         tpl += `${indent}     </div>\n`;
         tpl += `${indent}  </div>\n`;
      } else if (field.type === 'signature') {
         const oTarget = parentBinding === 'formData' ? 'formData.value' : parentBinding;
         tpl += `${indent}  <div class="border rounded bg-white p-2 mt-1">\n`;
         tpl += `${indent}    <canvas :id="'canvas_' + '${field.id}'" width="400" height="200" class="border border-gray-300 bg-gray-50 cursor-crosshair w-full" @mousedown="startSig($event, '${field.id}')" @mousemove="drawSig($event, '${field.id}')" @mouseup="endSig('${field.id}', '${field.camundaVariable || field.id}', ${oTarget})" @mouseleave="endSig('${field.id}', '${field.camundaVariable || field.id}', ${oTarget})" @touchstart="startSig($event, '${field.id}')" @touchmove="drawSig($event, '${field.id}')" @touchend="endSig('${field.id}', '${field.camundaVariable || field.id}', ${oTarget})"></canvas>\n`;
         tpl += `${indent}    <div class="flex justify-between mt-2 no-print">\n`;
         tpl += `${indent}       <button type="button" @click="clearSig('${field.id}', '${field.camundaVariable || field.id}', ${oTarget})" class="text-xs text-red-500 font-bold">Limpiar Firma</button>\n`;
         tpl += `${indent}       <span class="text-[10px] text-gray-400">Dibuja en el recuadro superior</span>\n`;
         tpl += `${indent}    </div>\n`;
         tpl += `${indent}  </div>\n`;
      } else if (field.type === 'timer') {
         if (field.timerMode === 'manual') {
            tpl += `${indent}  <div class="flex items-center gap-2 mt-1">\n`;
            tpl += `${indent}    <span class="text-xl font-mono bg-gray-100 px-3 py-1 rounded border">{{ ${vModelBase} || 0 }}s</span>\n`;
            tpl += `${indent}    <button type="button" @click="toggleTimer('${field.camundaVariable || field.id}', ${parentBinding === 'formData' ? 'formData.value' : 'row'})" class="bg-indigo-50 text-indigo-700 px-3 py-1 rounded text-xs font-bold hover:bg-indigo-100 transition no-print"${dsb}>▶/⏸</button>\n`;
            tpl += `${indent}    <button type="button" @click="resetTimer('${field.camundaVariable || field.id}', ${parentBinding === 'formData' ? 'formData.value' : 'row'})" class="bg-red-50 text-red-700 px-2 py-1 rounded text-xs font-bold hover:bg-red-100 transition no-print"${dsb}>↺</button>\n`;
            tpl += `${indent}  </div>\n`;
         } else {
            tpl += `${indent}  <div class="text-xs text-gray-500 italic flex items-center gap-1 mt-1">\n`;
            tpl += `${indent}    <span class="animate-pulse">⏱️</span> Cronómetro en segundo plano... ({{ ${vModelBase} || 0 }}s)\n`;
            tpl += `${indent}  </div>\n`;
         }
      } else if (field.type === 'gps') {
         const uTarget = parentBinding === 'formData' ? 'formData.value' : parentBinding;
         tpl += `${indent}  <div class="flex gap-2 mt-1">\n`;
         tpl += `${indent}    <input type="text" v-model="${vModelBase}" readonly placeholder="Coordenadas GPS (Lat, Lng)" class="form-input flex-1 rounded-md border-gray-300 shadow-sm bg-gray-100 italic"${dsb} />\n`;
         tpl += `${indent}    <button type="button" @click="captureGPS('${field.camundaVariable || field.id}', ${uTarget})" class="bg-indigo-600 text-white px-4 py-2 rounded shadow font-bold hover:bg-indigo-700 transition flex gap-1 items-center whitespace-nowrap"${dsb}>📌 Capturar GPS</button>\n`;
         tpl += `${indent}  </div>\n`;
      } else if (field.type === 'qr') {
         const uTarget = parentBinding === 'formData' ? 'formData.value' : parentBinding;
         tpl += `${indent}  <div class="flex gap-2 mt-1">\n`;
         tpl += `${indent}    <input type="text" v-model="${vModelBase}" placeholder="Valor escaneado (CA-62)" class="form-input flex-1 rounded-md border-gray-300 shadow-sm"${dsb} />\n`;
         tpl += `${indent}    <button type="button" @click="scanQR('${field.camundaVariable || field.id}', ${uTarget})" class="bg-teal-600 text-white px-4 py-2 rounded shadow font-bold hover:bg-teal-700 transition flex gap-1 items-center whitespace-nowrap"${dsb}>📷 Escanear QR</button>\n`;
         tpl += `${indent}  </div>\n`;
      } else if (field.type === 'hidden') {
         // CA-47: Componente Oculto Silencioso
         tpl += `${indent}  <input type="hidden" v-model="${vModelBase}" id="${field.camundaVariable || field.id}" />\n`;
      } else {
         tpl += `${indent}  <!-- Custom Component: ${field.type} -->\n`;
      }
      
      // CA-56 Print Mode Fallback
      tpl += `${indent}  </div>\n`; // End v-if !isPrintMode
      if (field.type !== 'hidden') {
          tpl += `${indent}  <div v-else class="text-sm text-gray-800 font-medium py-1 px-2 mb-1 mt-1 bg-white border-b border-dashed border-gray-300 min-h-[30px]">\n`;
          if (field.type === 'password') {
             tpl += `${indent}    <span class="text-gray-400 italic">*** Oculto ***</span>\n`;
          } else if (field.type === 'file') {
             tpl += `${indent}    <a v-if="${vModelBase}" :href="${vModelBase}" target="_blank" class="text-blue-600 underline">📎 Adjunto</a>\n`;
          } else if (field.type === 'signature') {
             tpl += `${indent}    <img v-if="${vModelBase}" :src="${vModelBase}" class="max-h-16" />\n`;
          } else if (field.type === 'checkbox') {
             tpl += `${indent}    <span>{{ ${vModelBase} ? '☑ Sí' : '☐ No' }}</span>\n`;
          } else if (field.type === 'timer') {
             tpl += `${indent}    <span>{{ ${vModelBase} || 0 }} seg.</span>\n`;
          } else {
             tpl += `${indent}    <span class="whitespace-pre-wrap">{{ Array.isArray(${vModelBase}) ? ${vModelBase}.join(', ') : (${vModelBase} || '---') }}</span>\n`;
          }
          tpl += `${indent}  </div>\n`;
      }
      
      // CA-28 Auditoria Forense Check
      if (field.enableAuditLog && field.type !== 'hidden') {
         tpl += `${indent}  <p class="text-[9px] text-gray-400 mt-1 uppercase tracking-wider font-mono">Modificado por: {{ currentUser?.name || 'Sistema' }}</p>\n`;
      }

      if (field.type !== 'hidden') {
         tpl += `${indent}  <span v-if="errors.${field.camundaVariable || field.id}" class="text-red-500 text-xs">{{ errors.${field.camundaVariable || field.id} }}</span>\n`;
      }
      tpl += `${indent}</div>\n`;
    }
    return tpl;
  };

  const computedCode = computed({
    get: () => {
      if (activeCodeTab.value === 'JSON') {
         return localJsonCode.value || JSON.stringify(canvasFields.value, null, 2);
      }
      if (activeCodeTab.value === 'TEMPLATE') {
        let tpl = `<template>\n  <form @submit.prevent="submitTask" class="space-y-4">`;
        tpl += `\n    <!-- CA-46: Sello Visual de Aprobatoria (Si existe en prefillData) -->\n    <div v-if="props.prefillData?.approvedBy" class="bg-green-50 border border-green-200 text-green-800 p-3 rounded-md flex items-center gap-3 no-print">\n      <span class="text-2xl">✅</span>\n      <div>\n        <p class="text-sm font-bold">Fase Aprobada Anteriomente</p>\n        <p class="text-xs">Revisor: {{ props.prefillData.approvedBy }}</p>\n      </div>\n    </div>\n`;
        if (canvasFields.value.length === 0) {
          tpl += `\n    <!-- Arrastra componentes al lienzo -->`;
        } else {
          for (const field of canvasFields.value) {
            tpl += generateFieldHTML(field, '    ');
          }
        }
        
        const hasSubmit = flatFields(canvasFields.value).some(f => f.type === 'button_submit');
        if (!hasSubmit && canvasFields.value.length > 0) {
            tpl += `\n    <button type="submit" class="w-full bg-blue-600 text-white font-bold py-2 rounded shadow hover:bg-blue-700 transition mt-6">Enviar Tarea (Auto)</button>`;
        }
        tpl += `\n  </form>\n</template>`;
        return tpl;
      } 
      
      if (activeCodeTab.value === 'SCRIPT') {
        let scr = `<script setup lang="ts">\nimport { ref, inject, watch, onMounted, onUnmounted } from 'vue';\nimport { z } from 'zod';\nimport { taskSchema } from './schema.zod.ts';\nimport apiClient from '@/services/apiClient';\n\n`;
        if (formPattern.value === 'IFORM_MAESTRO') {
          scr += `// IFORM_MAESTRO: Inyección de Etapa BPMN actual (Dual-Pattern CA-2)\nconst stage = inject('camunda_process_stage', 'START_EVENT');\n\n`;
        }
        
        scr += `// CA-37: Visor Histórico Inmutable para Auditoría\nconst isAuditMode = ref(false); // Cambiar a true si es histórico\n\n`;
        
        scr += `// CA-43: Recepción de Datos Precargados (BFF Pattern)\nconst props = defineProps<{ prefillData?: Record<string, any> }>();\n\n`;
        scr += `// CA-52: Control Asíncrono Global\nconst isAsyncLoading = ref(false);\n\n`;
        scr += `// CA-56: Modo Lectura Print/PDF\nconst isPrintMode = ref(false);\n\n`;
        
        const hasAudit = flatFields(canvasFields.value).some(f => f.enableAuditLog);
        if (hasAudit) {
           scr += `// Auditoría (CA-28): Injection Dummy de Usuario Actual\nconst currentUser = ref({ name: 'Admin Demo' });\n\n`;
        }

        const asyncFields = flatFields(canvasFields.value).filter(f => f.type === 'async_select' && f.asyncUrl);
        for (const field of asyncFields) {
           scr += `const asyncOpts_${field.id} = ref<string[]>([]);\n`;
           scr += `const fetchAsyncOpts_${field.id} = async (query: string) => {\n   if(query.trim().length === 0) { asyncOpts_${field.id}.value = []; return; }\n   try {\n      isAsyncLoading.value = true;\n      const res = await apiClient.get(\`${field.asyncUrl}?q=\${query}\`);\n      asyncOpts_${field.id}.value = Array.isArray(res.data) ? res.data.map(i => i.label || i.nombre || i.name || JSON.stringify(i)) : [];\n   } catch (e) { console.error('Typeahead Error (CA-30)', e); } finally { isAsyncLoading.value = false; }\n};\n\n`;
        }

        scr += `const formData = ref<Record<string, any>>({\n`;
        const directFields = canvasFields.value.filter(f => !f.type.startsWith('button_') && f.type !== 'container');
        for (const field of directFields) {
          if (field.type === 'field_array') {
             scr += `  ${field.camundaVariable || field.id}: [], // Grilla CA-34\n`;
          } else {
             let def = "''";
             if (field.type === 'number') def = 'null';
             if (field.type === 'checkbox') def = 'false';
             if (field.isMultiple && ['select', 'async_select'].includes(field.type)) def = '[]';
             scr += `  ${field.camundaVariable || field.id}: ${def}, // Binding CA-12/13\n`;
          }
        }
        scr += `});\n\nconst errors = ref<Record<string, string>>({});\n`;
        scr += `const taskId = 'MOCK_TASK_ID'; // Inyectar ID real\n\n`;
        scr += `// CA-43: Auto-map Pre-fill Binding\nonMounted(() => {\n  if (props.prefillData) {\n    for (const key in props.prefillData) {\n      if (key in formData.value) {\n        formData.value[key] = props.prefillData[key];\n      }\n    }\n  }\n});\n\n`;

        const hasDraft = flatFields(canvasFields.value).some(f => f.type === 'button_draft');
        const hasReject = flatFields(canvasFields.value).some(f => f.type === 'button_reject');
        const hasFile = flatFields(canvasFields.value).some(f => f.type === 'file');
        const hasGPS = flatFields(canvasFields.value).some(f => f.type === 'gps'); // CA-61
        const hasQR = flatFields(canvasFields.value).some(f => f.type === 'qr'); // CA-62
        const hasSignature = flatFields(canvasFields.value).some(f => f.type === 'signature');
        const hasMask = flatFields(canvasFields.value).some(f => f.mask);

        if (hasMask) {
           scr += `// CA-36: Enmascaramiento Dinámico Frontend-Only\n`;
           scr += `const formatMask = (val: string|number|null, _mask: string) => { if (val == null) return ''; return val.toString(); /* Inyección futura de libreria regex-mask */};\n`;
           scr += `const unmask = (val: string, type: string) => { const raw = val.replace(/[^a-zA-Z0-9.\\-@:]/g, ''); return type === 'number' ? parseFloat(raw)||null : raw; };\n\n`;
        }

        if (hasSignature) {
           scr += `// CA-31: Signature HTML5 Canvas Engine\n`;
           scr += `const sigState = ref<Record<string, {isDrawing: boolean, ctx: CanvasRenderingContext2D | null}>>({});\n`;
           scr += `const getCtx = (id: string, canvas: HTMLCanvasElement) => {\n  if(!sigState.value[id]) { sigState.value[id] = { isDrawing: false, ctx: canvas.getContext('2d') }; if(sigState.value[id].ctx) { sigState.value[id].ctx!.lineWidth = 2; sigState.value[id].ctx!.lineCap = 'round'; sigState.value[id].ctx!.strokeStyle = '#000'; } }\n  return sigState.value[id];\n};\n`;
           scr += `const startSig = (e: any, id: string) => { e.preventDefault(); const canvas = e.target as HTMLCanvasElement; const st = getCtx(id, canvas); if(!st.ctx) return; st.isDrawing = true; st.ctx.beginPath(); const rect = canvas.getBoundingClientRect(); const x = (e.clientX || e.touches?.[0].clientX) - rect.left; const y = (e.clientY || e.touches?.[0].clientY) - rect.top; st.ctx.moveTo(x, y); };\n`;
           scr += `const drawSig = (e: any, id: string) => { e.preventDefault(); const canvas = e.target as HTMLCanvasElement; const st = getCtx(id, canvas); if(!st || !st.isDrawing || !st.ctx) return; const rect = canvas.getBoundingClientRect(); const x = (e.clientX || e.touches?.[0].clientX) - rect.left; const y = (e.clientY || e.touches?.[0].clientY) - rect.top; st.ctx.lineTo(x, y); st.ctx.stroke(); };\n`;
           scr += `const endSig = (id: string, varName: string, targetObj: any) => { const st = sigState.value[id]; if(!st || !st.isDrawing) return; st.isDrawing = false; const canvas = document.getElementById('canvas_' + id) as HTMLCanvasElement; if(canvas) { targetObj[varName] = canvas.toDataURL('image/png'); } };\n`;
           scr += `const clearSig = (id: string, varName: string, targetObj: any) => { const canvas = document.getElementById('canvas_' + id) as HTMLCanvasElement; if(canvas) { const ctx = canvas.getContext('2d'); ctx?.clearRect(0,0, canvas.width, canvas.height); targetObj[varName] = ''; } };\n\n`;
        }

        scr += `// CA-24: Auto-Guardado Workdesk LocalStorage/API\nlet autoSyncDraftTimeout: any = null;\nwatch(formData, (newVal) => {\n  clearTimeout(autoSyncDraftTimeout);\n  autoSyncDraftTimeout = setTimeout(async () => {\n    try {\n      await apiClient.post('/forms/draft', newVal); // BUG-S7-001/BUG-B FIX: sin prefijo /api/v1\n      console.log('✅ Borrador auto-guardado en backend');\n    } catch (e) {\n      localStorage.setItem('workdesk_draft', JSON.stringify(newVal));\n      console.warn('⚠️ Fallback a LocalStorage para auto-guardado');\n    }\n  }, 2000);\n}, { deep: true });\n\n`;

        if (hasFile) {
           scr += `// CA-21, CA-39, CA-49: Conector Multipart File Upload + Constraints\nconst uploadFile = async (event: any, fieldId: string, targetObj: any, maxMb: number, exts: string, minFiles: number, maxFiles: number) => {\n  const target = event.target;\n  const files = target?.files;\n  if (!files || files.length === 0) return;\n  if (files.length < minFiles) { alert('Mínimo ' + minFiles + ' archivo(s) requeridos.'); target.value = ''; return; }\n  if (files.length > maxFiles) { alert('Máximo ' + maxFiles + ' archivo(s) permitidos.'); target.value = ''; return; }\n  let urls: string[] = [];\n  for (let i = 0; i < files.length; i++) {\n     const file = files[i];\n     if (maxMb > 0 && file.size > maxMb * 1024 * 1024) { alert('El archivo \\'' + file.name + '\\' excede el límite de ' + maxMb + 'MB.'); target.value = ''; return; }\n     if (exts) { const ext = '.' + file.name.split('.').pop()?.toLowerCase(); if (!exts.toLowerCase().includes(ext)) { alert('Extensión ' + ext + ' no permitida. Solo: ' + exts); target.value = ''; return; } }\n     const data = new FormData();\n     data.append('file', file);\n     try {\n       const res = await apiClient.post('/forms/upload', data, { headers: { 'Content-Type': 'multipart/form-data' } }); // BUG-S7-001/BUG-B FIX: sin prefijo /api/v1\n       urls.push(res.data.url || 'subido_exitosamente_' + i);\n     } catch (error) {\n       alert('Error subiendo \\'' + file.name + '\\': ' + (error as any).message);\n       return;\n     }\n  }\n  targetObj[fieldId] = urls.length > 1 ? JSON.stringify(urls) : urls[0];\n  alert('Archivo(s) subido(s) exitosamente');\n};\n\n`;
           scr += `// CA-60: Manejador Drag & Drop Dropzone\nconst dropFile = (event: any, fieldId: string, targetObj: any, maxMb: number, exts: string, minFiles: number, maxFiles: number) => {\n  const dt = event.dataTransfer;\n  if (dt && dt.files && dt.files.length > 0) {\n     uploadFile({ target: { files: dt.files } }, fieldId, targetObj, maxMb, exts, minFiles, maxFiles);\n  }\n};\n\n`;
        }

        if (hasGPS) {
           scr += `// CA-61: Embebido HTML5 GPS Geolocation\nconst captureGPS = (fieldId: string, targetObj: any) => {\n  if (!navigator.geolocation) { alert('Geolocalización no soportada en este navegador.'); return; }\n  navigator.geolocation.getCurrentPosition(\n    (pos) => { targetObj[fieldId] = \`Lat: \${pos.coords.latitude}, Lng: \${pos.coords.longitude}\`; },\n    (err) => { alert('Error obteniendo ubicación: ' + err.message); },\n    { enableHighAccuracy: true }\n  );\n};\n\n`;
        }

        if (hasQR) {
           scr += `// CA-62: WebRTC QR Scanner Mock/Dummy\nconst scanQR = (fieldId: string, targetObj: any) => {\n  // Para paso a producción requeriría importar librería de escaneo webRTC\n  const val = prompt('📸 [Simulador QR] Ingrese el resultado del Escaneo:', 'QR-MOCK-7788');\n  if (val) targetObj[fieldId] = val;\n};\n\n`;
        }


        const timers = flatFields(canvasFields.value).filter(f => f.type === 'timer');
        if (timers.length > 0) {
           scr += `// CA-58: Lógica de Cronómetros de Telemetría\n`;
           scr += `const timerIntervals: Record<string, ReturnType<typeof setInterval>> = {};\n`;
           scr += `const isTimerActive: Record<string, boolean> = {};\n`;
           scr += `const toggleTimer = (key: string, targetObj: any) => {\n`;
           scr += `  if (isTimerActive[key]) {\n`;
           scr += `     clearInterval(timerIntervals[key]);\n`;
           scr += `     isTimerActive[key] = false;\n`;
           scr += `  } else {\n`;
           scr += `     isTimerActive[key] = true;\n`;
           scr += `     if (typeof targetObj[key] !== 'number') targetObj[key] = 0;\n`;
           scr += `     timerIntervals[key] = setInterval(() => { targetObj[key]++; }, 1000);\n`;
           scr += `  }\n`;
           scr += `};\n`;
           scr += `const resetTimer = (key: string, targetObj: any) => {\n`;
           scr += `  clearInterval(timerIntervals[key]);\n`;
           scr += `  isTimerActive[key] = false;\n`;
           scr += `  targetObj[key] = 0;\n`;
           scr += `};\n`;
           const autoTimers = timers.filter(t => t.timerMode === 'background');
           if (autoTimers.length > 0) {
               scr += `onMounted(() => {\n`;
               for (const t of autoTimers) {
                   const key = t.camundaVariable || t.id;
                   scr += `  if (typeof formData.value['${key}'] !== 'number') formData.value['${key}'] = 0;\n`;
                   scr += `  timerIntervals['${key}'] = setInterval(() => { formData.value['${key}']++; }, 1000);\n`;
                   scr += `  isTimerActive['${key}'] = true;\n`;
               }
               scr += `});\n`;
           }
           scr += `onUnmounted(() => {\n`;
           scr += `  Object.values(timerIntervals).forEach(clearInterval);\n`;
           scr += `});\n\n`;
        }

        let phantomLogic = '';
        const fieldsWithCond = flatFields(canvasFields.value).filter(f => f.visibilityCondition || (f.requiredIfField && f.requiredIfValue));
        if (fieldsWithCond.length > 0) {
           phantomLogic += `  // CA-54: Purga de Phantom Data (Ocultos/Condicionales)\n`;
           for (const f of fieldsWithCond) {
               const key = f.camundaVariable || f.id;
               let condStr = '';
               let hasVis = false;
               if (f.visibilityCondition) {
                  condStr += `!(${f.visibilityCondition.replace(/formData\./g, 'cleanData.')})`;
                  hasVis = true;
               }
               if (f.requiredIfField && f.requiredIfValue) {
                  if (hasVis) condStr += ' || ';
                  condStr += `!(cleanData.${f.requiredIfField} === '${f.requiredIfValue}')`;
               }
               phantomLogic += `  if (${condStr}) { delete cleanData['${key}']; }\n`;
           }
        }

        scr += `// CA-22: Lazy Zod Validation\n`;
        scr += `const validateField = (fieldId: string) => {\n  const cleanData = JSON.parse(JSON.stringify(formData.value));\n  Object.keys(cleanData).forEach(k => { if (typeof cleanData[k] === 'string' && /^[\\d.,$]+$/.test(cleanData[k])) { const num = parseFloat(cleanData[k].replace(/[^\\d.-]/g, '')); if(!isNaN(num)) cleanData[k] = num; } });\n  const result = taskSchema.safeParse(cleanData);\n  if (!result.success) {\n    const issue = result.error.issues.find(iss => iss.path[0] === fieldId);\n    if (issue) errors.value[fieldId] = issue.message;\n    else delete errors.value[fieldId];\n  } else {\n    delete errors.value[fieldId];\n  }\n};\n\n`;

        scr += `// CA-15, CA-50: Smart Actions con Blindaje y Stripping Numerico\n`;
        scr += `const submitTask = async () => {\n  errors.value = {};\n`;
        scr += `  // CA-50: Stripping Silencioso de formato Numérico\n  const cleanData = JSON.parse(JSON.stringify(formData.value));\n`;
        scr += `  Object.keys(cleanData).forEach(k => { if (typeof cleanData[k] === 'string' && /^[\\d.,$]+$/.test(cleanData[k])) { const num = parseFloat(cleanData[k].replace(/[^\\d.-]/g, '')); if(!isNaN(num)) cleanData[k] = num; } });\n\n`;
        if (phantomLogic) scr += phantomLogic + '\n';
        scr += `  const result = taskSchema.safeParse(cleanData);\n  if (!result.success) {\n    result.error.issues.forEach(iss => {\n      if (iss.path[0]) errors.value[iss.path[0].toString()] = iss.message;\n    });\n    return;\n  }\n  try {\n    const payload = { variables: result.data };\n    await apiClient.post(\`/engine-rest/task/\${taskId}/complete\`, payload, { headers: { 'If-Match': props.prefillData?.versionId || '' } });\n    alert('Tarea Completada (Success)');\n  } catch (error: any) {\n    if (error.response?.status >= 500) {\n      localStorage.setItem('workdesk_draft_fallback', JSON.stringify(cleanData));\n      alert('⚠️ Error 5xx en servidor. Borrador protegido en LocalStorage y postergado (Offline Fallback CA-72).');\n    } else {\n      alert('Excepción de Red al Completar Tarea: ' + error.message);\n    }\n  }\n};\n`;
        
        if (hasDraft) {
          scr += `\nconst saveDraft = async () => {\n  try {\n    const cleanData = JSON.parse(JSON.stringify(formData.value));\n    Object.keys(cleanData).forEach(k => { if (typeof cleanData[k] === 'string' && /^[\\d.,$]+$/.test(cleanData[k])) { const num = parseFloat(cleanData[k].replace(/[^\\d.-]/g, '')); if(!isNaN(num)) cleanData[k] = num; } });\n`;
          if (phantomLogic) scr += phantomLogic;
          scr += `    await apiClient.post('/forms/draft', cleanData, { headers: { 'If-Match': props.prefillData?.versionId || '' } }); // BUG-S7-001/BUG-B FIX: sin prefijo /api/v1\n    alert('Borrador Guardado (Success)');\n  } catch (error: any) {\n    if (error.response?.status >= 500) {\n      localStorage.setItem('workdesk_draft_fallback', JSON.stringify(cleanData));\n      alert('⚠️ Error 5xx en servidor. Borrador protegido en LocalStorage (Offline Fallback CA-72).');\n    } else {\n      alert('Excepción de Red al Guardar Borrador: ' + error.message);\n    }\n  }\n};\n`;
        }
        if (hasReject) {
           scr += `\nconst rejectTask = async () => {\n  try {\n    await apiClient.post(\`/engine-rest/task/\${taskId}/bpmnError\`, { errorCode: 'REJECTED' });\n    alert('Excepción BPMN Disparada (Success)');\n  } catch (error) {\n    alert('Excepción de Red al Rechazar Tarea: ' + (error as any).message);\n  }\n};\n`;
        }

        scr += `<\/script>`;
        return scr;
      }

      if (activeCodeTab.value === 'STYLE') {
        return `<style scoped>\n/* Estilos inyectados por el motor Zod O-T-F (CA-5) */\n.form-input {\n  @apply w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500;\n}\n.form-select {\n  @apply w-full rounded-md border-gray-300 shadow-sm;\n}\n</style>`;
      }

      if (activeCodeTab.value === 'ZOD') {
        const walkNode = (fieldsArr: any[], isRoot: boolean): string => {
           let zc = `z.object({\n`;
           for(const field of fieldsArr) {
              if(field.type.startsWith('button_') || field.type === 'container') continue;
              if(field.type === 'field_array') {
                  if(!field.children || field.children.length === 0) continue;
                  let arrCode = `z.array(${walkNode(field.children, false)})`;
                  if(field.minRows) arrCode += `.min(${field.minRows}, "Mínimo ${field.minRows} filas")`;
                  if(field.maxRows) arrCode += `.max(${field.maxRows}, "Máximo ${field.maxRows} filas")`;
                  zc += `  ${field.camundaVariable || field.id}: ${arrCode}, // [GRILLA CA-41]\n`;
                  continue;
              }
              let zt = 'string';
              if(field.type === 'number') zt = 'number';
              if(field.type === 'checkbox') zt = 'boolean';
              
              let piiMod = field.isPII ? `.describe('isPII')` : ``;

              if (field.isMultiple && ['select', 'async_select'].includes(field.type)) {
                  zc += `  ${field.camundaVariable || field.id}: z.array(z.string())${field.required ? '.min(1, "Seleccione opción")' : '.optional()'}${piiMod}, // [${field.stage || 'GLOBAL'}]\n`;
              } else if (field.type === 'file' || field.type === 'signature') {
                  zc += `  ${field.camundaVariable || field.id}: z.string().uuid({ message: "Se requiere un UUID de Puntero S3" })${field.required ? '.min(1, "Campo requerido")' : '.optional()'}${piiMod}, // [${field.stage || 'GLOBAL'}]\n`;
              } else {
                  zc += `  ${field.camundaVariable || field.id}: z.${zt}()${field.required && field.type !== 'checkbox' ? '.min(1, "Campo requerido")' : '.optional()'}${piiMod}, // [${field.stage || 'GLOBAL'}]\n`;
              }
           }
           zc += isRoot ? `})` : `        })`;
           return zc;
        };

        let zc = `import { z } from 'zod';\n\nexport const taskSchema = ${walkNode(canvasFields.value, true)}`;
        
        // Inject CA-48 Condicionales Directly via SuperRefine
        const conditionalFields = flatFields(canvasFields.value).filter(f => f.requiredIfField && f.requiredIfValue);
        let crules = '';
        
        if (visualRules.value && visualRules.value.length > 0) {
            crules += `  // CA-32: Validaciones Cruzadas AST\n`;
            visualRules.value.forEach(r => {
               let failCond = '';
               if (r.operator === '>') failCond = `data.${r.fieldA} <= data.${r.fieldB}`;
               if (r.operator === '<') failCond = `data.${r.fieldA} >= data.${r.fieldB}`;
               if (r.operator === '==') failCond = `data.${r.fieldA} !== data.${r.fieldB}`;
               if (r.operator === '!=') failCond = `data.${r.fieldA} === data.${r.fieldB}`;
               crules += `  if (${failCond}) {\n    ctx.addIssue({ code: z.ZodIssueCode.custom, message: "${r.errorMessage}", path: ["${r.fieldA}"] });\n  }\n`;
            });
        }

        if (conditionalFields.length > 0) {
           crules += `  // CA-48: Validaciones Condicionales Declarativas\n`;
           conditionalFields.forEach(f => {
              crules += `  if (data.${f.requiredIfField} === '${f.requiredIfValue}' && !data.${f.camundaVariable || f.id}) {\n    ctx.addIssue({ code: z.ZodIssueCode.custom, message: "Campo obligatorio basado en ${f.requiredIfField}", path: ["${f.camundaVariable || f.id}"] });\n  }\n`;
           });
        }

        if (crules) {
           zc += `\n.superRefine((data, ctx) => {\n${crules}})`;
        }
        zc += `;\n\nexport type TaskSchemaPayload = z.infer<typeof taskSchema>;`;
        return zc;
      }

      return '';
    },
    set: (newCode: string) => {
      // CA-4: Parseo seguro usando Regex (AST Ligero in-memory), PROHIBIDO eval() o new Function()
      
      // 🛡️ XSS Barricade (Security Gate) AST
      if (/<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi.test(newCode) || /\bon\w+\s*=/gi.test(newCode)) {
         zodParseError.value = "⚠️ ALERTA (XSS): Sintaxis destructiva purgada. Se prohíben scripts o inyección de eventos DOM.";
         return; // Cut-off del Event Loop AST
      }
      zodParseError.value = ''; // Clean Gate Flag

      if (activeCodeTab.value === 'JSON') {
         localJsonCode.value = newCode;
      }
      else if (activeCodeTab.value === 'TEMPLATE') {
        const inputRegex = /v-model="formData\.([^"]+)"/g;
        let m;
        const ids = new Set<string>();
        while ((m = inputRegex.exec(newCode)) !== null) {
            ids.add(m[1]);
        }
        const currentFields = [...canvasFields.value];
        const newCanvasFields = [];
        for (const id of Array.from(ids)) {
            const exist = currentFields.find(f => f.camundaVariable === id || f.id === id);
            if (exist) {
                newCanvasFields.push(exist);
            } else {
                newCanvasFields.push({ id: id.toUpperCase(), camundaVariable: id, type: 'text', label: id, required: false, stage: 'START_EVENT' });
            }
        }
        canvasFields.value = newCanvasFields;
      } 
      else if (activeCodeTab.value === 'ZOD') {
        try {
          const regex = /^\s*([a-zA-Z0-9_]+):\s*(z\.(?:string|number|any|boolean)\(\)|z\.array\(z\.string\(\)\))(.*?)(?:\/\/\s*\[([^\]]+)\])?/gm;
          let match;
          const newCanvasFields = [];
          const currentFields = [...canvasFields.value];
          let parseCount = 0;
          
          while ((match = regex.exec(newCode)) !== null) {
              parseCount++;
              const varName = match[1];
              const zTypeRaw = match[2];
              const mods = match[3];
              const stage = match[4] ? match[4].trim() : "START_EVENT";

              const isReq = mods.includes('.min(') || !mods.includes('.optional()');
              const isMult = zTypeRaw.includes('z.array');
              
              let minL, maxL;
              const minMatch = mods.match(/\.min\((\d+)/);
              if (minMatch) minL = parseInt(minMatch[1], 10);
              const maxMatch = mods.match(/\.max\((\d+)/);
              if (maxMatch) maxL = parseInt(maxMatch[1], 10);
              
              let cType = 'text';
              if(isMult) cType = 'select'; // Prefer select if multiple

              const exist = currentFields.find(f => f.camundaVariable === varName || f.id === varName);
              newCanvasFields.push({
                 ...(exist || { id: varName.toUpperCase(), label: varName }),
                 camundaVariable: varName,
                 type: exist && exist.type !== cType && exist.type !== 'select' && exist.type !== 'async_select' && exist.type !== 'hidden' ? cType : (exist ? exist.type : cType),

                 required: isReq,
                 stage: stage,
                 isMultiple: isMult || exist?.isMultiple,
                 minLength: minL || exist?.minLength,
                 maxLength: maxL || exist?.maxLength
              });
          }
          
          if (newCode.includes('z.object({') && parseCount === 0 && newCode.includes(':')) {
              throw new Error('Sintaxis fallida o Regex roto');
          }

          if (newCanvasFields.length > 0 || newCode.includes('z.object({')) {
              canvasFields.value = newCanvasFields;
              zodParseError.value = false;
          }
        } catch (err) {
          zodParseError.value = true;
        }
      }
    }
  });

  return {
    availableStages,
    canvasFields,
    formTitle,
    formPattern,
    activeStageSim,
    visualRules,
    formVersions,
    isPublic,
    certificationState,
    currentSchemaVersion,
    bpmnCoherenceResults,
    formKey,
    zodParseError,
    aiPrompt,
    isScanningAi,
    fuzzerErrors,
    superRefineCount,
    toolboxCategories,
    simulatorContext,
    activeCodeTab,
    localJsonCode,
    editingField,
    idCounter,
    generateAiForm,
    saveAsFragment,
    fetchVersions,
    fetchForm,
    checkBpmnCoherence,
    runFuzzerZod,
    restoreVersion,
    saveDraftToApi,
    cloneComponent,
    evaluateMockVis,
    flatFields,
    attemptTabChange,
    generateMockPath,
    generateVitestSpec,
    certifyForm,
    computedCode
  };
});
