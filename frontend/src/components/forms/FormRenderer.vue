<template>
  <!-- TIN-1: Shadow DOM Host Container -->
  <div ref="hostRef" class="w-full"></div>
</template>

<script setup lang="ts">
import { useIntegrationStore } from '@/stores/useIntegrationStore';
import apiClient from '@/services/apiClient';
import { ref, onMounted, onBeforeUnmount, createApp, h, watch, watchEffect, type VNode, Teleport, reactive, computed } from 'vue';
import jexl from 'jexl';
import IMask from 'imask';
import { z } from 'zod';
import { IMaskDirective } from 'vue-imask';
import { useDebounceFn } from '@vueuse/core';
import FormWizard from './FormWizard.vue';
import { useWizardValidation } from '@/composables/useWizardValidation';

// @Traceability: Retro-Remediación ADR-006
const integrationStore = useIntegrationStore();

const props = defineProps<{ schema: any[], mockContext?: Record<string, any> }>();
const formData = defineModel<Record<string, any>>({ default: () => ({}) });
const emit = defineEmits(['stage-change']);

const hostRef = ref<HTMLElement | null>(null);
let shadowApp: any = null;

// TIN-5: Ghost Cleanup Hooks
const isSubmitted = ref(false);
const uploadedUuids = ref<string[]>([]);

const markFileUploaded = (uuid: string) => {
   if (!uploadedUuids.value.includes(uuid)) {
      uploadedUuids.value.push(uuid);
   }
};

const notifySubmit = () => {
   isSubmitted.value = true;
};

defineExpose({ notifySubmit, markFileUploaded });

onMounted(() => {
  if (hostRef.value) {
    const shadowRoot = hostRef.value.attachShadow({ mode: 'open' });
    
    // Inyectamos Tailwind (Vite dev server) o genérico
    const link = document.createElement('link');
    link.rel = 'stylesheet';
    link.href = '/src/assets/index.tailwind.css'; // Fallback path
    shadowRoot.appendChild(link);

    // Fallback in case link fails due to Vite dev mode injection routing
    const tailwindCdn = document.createElement('script');
    tailwindCdn.src = 'https://cdn.tailwindcss.com?plugins=forms';
    shadowRoot.appendChild(tailwindCdn);

    const appContainer = document.createElement('div');
    appContainer.className = 'workdesk-form-engine p-4 min-h-screen bg-transparent';
    shadowRoot.appendChild(appContainer);

    shadowApp = createApp({
      setup() {
        // CA-11B: Memoria local del Info Modal
        const infoModalOpen = reactive<Record<string, boolean>>({});

        // CA-22: Configuración del Wizard (Multi-Stage)
        const stages = computed(() => {
            const stgs: string[] = [];
            const traverse = (nodes: any[]) => {
                for (const n of nodes) {
                    if (n.stage && !stgs.includes(n.stage)) stgs.push(n.stage);
                    if (n.children) traverse(n.children);
                }
            };
            traverse(props.schema);
            return stgs.length > 1 ? stgs : [];
        });

        const isWizard = computed(() => stages.value.length > 0);
        const currentStageIndex = ref(0);
        const currentStageName = computed(() => isWizard.value ? stages.value[currentStageIndex.value] : null);

        // Zod validation per stage using useWizardValidation (CA-22)
        const schemaConfigs = reactive<Record<string, z.ZodSchema>>({});
        const { wizardErrors, validateStep, hasStepErrors, clearStepErrors } = useWizardValidation(schemaConfigs);

        watchEffect(() => {
            if (!isWizard.value) return;
            stages.value.forEach(stg => {
                const nodesInStage: any[] = [];
                const traverse = (nodes: any[]) => {
                    for(const n of nodes) {
                        if ((n.stage || 'GLOBAL') === stg || !n.stage) {
                            nodesInStage.push(n);
                        }
                        if (n.children) traverse(n.children);
                    }
                };
                traverse(props.schema);
                
                const shape: any = {};
                nodesInStage.forEach(node => {
                    if (isVisible(node) && node.type !== 'container' && node.type !== 'info_modal') {
                        let fieldZod = z.any();
                        if (node.type === 'number') fieldZod = z.number({ invalid_type_error: "Debe ser numérico" });
                        else fieldZod = z.string({ invalid_type_error: "Inválido" });
                        
                        if (node.required) {
                            if (node.type !== 'number') {
                                fieldZod = (fieldZod as z.ZodString).min(1, 'Este campo es requerido');
                            }
                        } else {
                            fieldZod = fieldZod.optional().nullable();
                        }
                        
                        const key = node.camundaVariable || node.id;
                        shape[key] = fieldZod;
                    }
                });
                schemaConfigs[stg] = z.object(shape);
            });
        });

        const validateCurrentStageZod = () => {
            if (!isWizard.value || !currentStageName.value) return true;
            
            const isValid = validateStep(currentStageName.value, formData.value);
            
            if (!isValid) {
                // Focus on first error element
                const stepErrs = wizardErrors.value[currentStageName.value] || {};
                const firstErrKey = Object.keys(stepErrs)[0];
                if (firstErrKey) {
                    // Try to find the node id by variable or id
                    const findNodeId = (nodes: any[]): string | null => {
                         for (const n of nodes) {
                             if ((n.camundaVariable || n.id) === firstErrKey) return n.id;
                             if (n.children) {
                                 const found = findNodeId(n.children);
                                 if (found) return found;
                             }
                         }
                         return null;
                    };
                    const errorId = findNodeId(props.schema);
                    if (errorId) {
                        const el = hostRef.value?.shadowRoot?.querySelector(`#field-wrapper-${errorId}`);
                        if (el) {
                            el.scrollIntoView({ behavior: 'smooth', block: 'center' });
                            el.classList.add('ring-2', 'ring-red-500', 'animate-pulse', 'rounded');
                            setTimeout(() => el.classList.remove('ring-2', 'ring-red-500', 'animate-pulse', 'rounded'), 2000);
                        }
                    }
                }
                return false;
            }
            clearStepErrors(currentStageName.value);
            return true;
        };

        const checkStageState = () => {
             const isLast = !isWizard.value || currentStageIndex.value === stages.value.length - 1;
             emit('stage-change', { isLastStage: isLast, currentStage: currentStageName.value });
        };

        const nextStage = () => {
            if (validateCurrentStageZod()) {
                currentStageIndex.value++;
                checkStageState();
            }
        };

        const prevStage = () => {
            if (currentStageIndex.value > 0) {
                currentStageIndex.value--;
                checkStageState();
            }
        };

        watch(() => stages.value, () => {
             checkStageState();
        }, { immediate: true });

        // CA-54: GAP 8 - Mantenimiento Mnemónico (Auto-purga)
        watch(() => formData.value, (newVal) => {
            const traverseAndClear = (nodes: any[]) => {
                for (const node of nodes) {
                   if (node.children) traverseAndClear(node.children);
                   if (node.clearOnHide && !isVisible(node)) {
                       const key = node.camundaVariable || node.id;
                       if (newVal[key] !== undefined) {
                           delete newVal[key];
                       }
                   }
                }
            };
            traverseAndClear(props.schema);
        }, { deep: true });

        const getJexlContext = () => ({
            data: formData.value,
            context: props.mockContext || {} // CA-69: GAP 9 - Mimetismo RBAC
        });

        // TIN-3: Jexl Sandboxing Validator
        const isVisible = (node: any) => {
          if (!node.visibilityCondition && !node.disableCondition) return true;
          try {
             if (node.visibilityCondition) {
                 return jexl.evalSync(node.visibilityCondition, getJexlContext());
             }
             return true; 
          } catch(e) {
             console.warn('Jexl eval blocked execution (Safeguard):', e);
             return false;
          }
        };

        const isDisabled = (node: any) => {
            if (!node.disableCondition) return false;
            try { return jexl.evalSync(node.disableCondition, getJexlContext()); } 
            catch { return false; }
        };

        const asyncOptions = ref<Record<string, any[]>>({});
        const fetchAsyncData = useDebounceFn(async (url: string, query: string, fieldId: string) => {
            if(!url) return;
            try {
                // Simulando CA-77
                const res = await integrationStore.get(`${url}?q=${query}`);
                asyncOptions.value[fieldId] = res.data;
            } catch(e) {
                asyncOptions.value[fieldId] = [{ id: 'mock1', name: 'Dato Gobernado 1' }, { id: 'mock2', name: 'Dato Gobernado 2' }];
            }
        }, 500);

        const renderField = (node: any): VNode | null => {
           if (!isVisible(node)) return null;

           // CA-22: Filter by Stage if Wizard mode
           if (isWizard.value && node.stage && node.stage !== currentStageName.value) {
               return null;
           }

           const bindingKey = node.camundaVariable || node.id;
           const val = formData.value[bindingKey];
           const updateVal = (newVal: any) => { formData.value[bindingKey] = newVal; };
           const disabled = isDisabled(node);

           const labelVNode = h('label', { class: 'block text-sm font-bold text-gray-700 mb-1' }, [
               node.label,
               node.required ? h('span', { class: 'text-red-500 ml-1' }, '*') : null
           ]);

           let inputVNode: VNode | null = null;

           if (['text', 'password', 'email', 'url'].includes(node.type)) {
               // TIN-4: Input Masking Native
               const attrs: any = {
                   type: node.type,
                   value: val || '',
                   placeholder: node.placeholder || '',
                   disabled,
                   class: 'form-input w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm',
                   onInput: (e: any) => updateVal(e.target.value)
               };

               if (node.enableAutocomplete && node.autocompleteUrl) {
                   attrs.onBlur = async (e: any) => {
                       const queryVal = e.target.value;
                       if (!queryVal) return;
                       try {
                           const res = await apiClient.get(`${node.autocompleteUrl}?q=${queryVal}`);
                           if (res.data) {
                               const mappings = node.autocompleteMappings || [];
                               mappings.forEach((m: { from: string, to: string }) => {
                                   if (m.from && m.to) {
                                       formData.value[m.to] = res.data[m.from];
                                   }
                               });
                           }
                       } catch (err) {
                           console.error("Autocomplete runtime error (CA-30):", err);
                       }
                   };
               }
               
               let iMaskOptions: any = null;
               
               if (node.predefinedFormat === 'currency') {
                   iMaskOptions = { mask: Number, scale: 2, signed: false, thousandsSeparator: '.', padFractionalZeros: true, normalizeZeros: true, radix: ',' };
               } else if (node.predefinedFormat === 'phone') {
                   iMaskOptions = { mask: '+{00} 000-0000', lazy: false }; // Example phone mask
               } else if (node.predefinedFormat === 'idcard') {
                   iMaskOptions = { mask: '00.000.000' };
               } else if (node.predefinedFormat === 'regex' && node.mask) {
                   iMaskOptions = { mask: new RegExp(node.mask) };
               } else if (node.mask && !node.predefinedFormat) {
                   iMaskOptions = { mask: new RegExp(node.mask) }; // Fallback
               }

               if (iMaskOptions) {
                   attrs.onVnodeMounted = (vnode: VNode) => {
                       const el = vnode.el as HTMLInputElement;
                       if (el) {
                           const maskCore = IMask(el, iMaskOptions);
                           maskCore.on('accept', () => {
                               // Enviar unmasked RAW value al Zod Motor
                               updateVal(maskCore.unmaskedValue); 
                           });
                       }
                   };
               }
               inputVNode = h('input', attrs);
           }
           else if (node.type === 'number') {
               inputVNode = h('input', {
                   type: 'number',
                   value: val || '',
                   placeholder: node.placeholder || '',
                   disabled,
                   class: 'form-input w-full rounded-md border-gray-300 shadow-sm sm:text-sm',
                   onInput: (e: any) => updateVal(Number(e.target.value))
               });
           }
           else if (node.type === 'textarea') {
               inputVNode = h('textarea', {
                   value: val || '',
                   placeholder: node.placeholder || '',
                   rows: node.rows || 3,
                   disabled,
                   class: 'form-textarea w-full rounded-md border-gray-300 shadow-sm sm:text-sm',
                   onInput: (e: any) => updateVal(e.target.value)
               });
           }
           else if (node.type === 'select') {
               inputVNode = h('select', {
                   value: val || '',
                   disabled,
                   class: 'form-select w-full rounded-md border-gray-300 shadow-sm sm:text-sm',
                   onChange: (e: any) => updateVal(e.target.value)
               }, [
                   h('option', { value: '', disabled: true }, node.placeholder || 'Seleccione...'),
                   ...(node.options || []).map((opt: any) => h('option', { value: opt }, opt))
               ]);
           }
           else if (node.type === 'checkbox') {
               inputVNode = h('input', {
                   type: 'checkbox',
                   checked: !!val,
                   disabled,
                   class: 'text-indigo-600 rounded border-gray-300 focus:ring-indigo-500',
                   onChange: (e: any) => updateVal(e.target.checked)
               });
           }
           else if (node.type === 'async_select') {
               const fieldId = node.camundaVariable || node.id;
               inputVNode = h('div', { class: 'relative' }, [
                   h('input', {
                       value: val?.name || val || '',
                       placeholder: 'Búsqueda Typeahead (CA-77 500ms)...',
                       disabled,
                       class: 'form-input w-full rounded-md border-purple-300 shadow-sm sm:text-sm bg-purple-50 focus:border-purple-500 focus:ring-purple-500',
                       onInput: (e: any) => {
                           updateVal(e.target.value);
                           fetchAsyncData(node.asyncUrl, e.target.value, fieldId);
                       }
                   }),
                   (asyncOptions.value[fieldId] && asyncOptions.value[fieldId].length > 0) ? h('ul', {
                       class: 'absolute z-10 mt-1 w-full bg-white shadow-lg max-h-40 rounded-md py-1 text-base ring-1 ring-black ring-opacity-5 overflow-auto focus:outline-none sm:text-sm'
                   }, asyncOptions.value[fieldId].map((opt: any) => h('li', {
                       class: 'text-gray-900 cursor-pointer select-none relative py-2 pl-3 pr-9 hover:bg-purple-100',
                       onClick: () => {
                           updateVal(opt.name || opt);
                           asyncOptions.value[fieldId] = []; // clear
                       }
                   }, opt.name || opt))) : null
               ]);
           }
           else if (node.type === 'container') {
               return h('div', { class: 'p-4 border border-indigo-100 bg-indigo-50/20 rounded-lg mb-4' }, [
                   h('h3', { class: 'text-sm font-bold text-indigo-800 mb-3 uppercase tracking-wider' }, node.label),
                   h('div', { class: 'space-y-4' }, (node.children || []).map((child: any) => renderField(child)))
               ]);
           }
           else if (node.type === 'info_modal') {
               const isOpen = infoModalOpen[node.id] || false;
               
               const buttonVNode = h('button', {
                   class: 'bg-indigo-50 border border-indigo-200 text-indigo-700 hover:bg-indigo-100 hover:border-indigo-300 font-bold py-2 px-4 rounded shadow-sm text-sm flex items-center gap-2 transition focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 w-full sm:w-auto mt-2',
                   onClick: (e: Event) => {
                       e.preventDefault();
                       infoModalOpen[node.id] = true;
                   }
               }, [
                   h('span', { class: 'text-xl leading-none' }, 'ℹ️'),
                   node.label || 'Ver Información'
               ]);

               const teleportVNode = isOpen ? h(Teleport, { to: 'body' }, [
                   h('div', { class: 'fixed inset-0 bg-gray-900/60 z-[900] flex items-center justify-center p-4 backdrop-blur-sm' }, [
                       h('div', { class: 'bg-white rounded-xl shadow-2xl p-6 md:p-8 max-w-lg w-full transform transition-all border border-gray-200' }, [
                           h('div', { class: 'flex justify-between items-start mb-4 border-b pb-3 border-gray-100' }, [
                               h('h3', { class: 'text-lg font-bold text-gray-900 flex items-center gap-2' }, [
                                   h('span', { class: 'bg-indigo-100 text-indigo-600 rounded-lg px-2 py-1 text-sm mr-1' }, 'ℹ️'),
                                   node.tooltipText || 'Información Importante' // Titulo del tooltip
                               ]),
                               h('button', { 
                                   class: 'text-gray-400 hover:text-gray-600 text-2xl font-bold leading-none',
                                   onClick: (e: Event) => { e.preventDefault(); infoModalOpen[node.id] = false; }
                               }, '×')
                           ]),
                           h('div', { class: 'prose prose-sm prose-indigo mb-6 max-h-[60vh] overflow-y-auto text-gray-600 pr-2', innerHTML: node.placeholder?.replace(/\n/g, '<br/>') || 'No hay contenido provisto.' }),
                           h('div', { class: 'flex justify-end pt-4 border-t border-gray-100' }, [
                               h('button', {
                                   class: 'bg-indigo-600 text-white font-bold py-2 px-6 rounded-lg hover:bg-indigo-700 shadow-sm transition focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500',
                                   onClick: (e: Event) => { e.preventDefault(); infoModalOpen[node.id] = false; }
                               }, 'Entendido')
                           ])
                       ])
                   ])
               ]) : null;

               return h('div', { class: 'mb-4 flex flex-col items-start' }, [buttonVNode, teleportVNode]);
           }
           // Arrays, Tabs, Accordions can be extended here
           else {
               inputVNode = h('div', { class: 'text-xs text-gray-400 border border-dashed border-gray-200 p-2 rounded' }, `[Componente no soportado por Runtime Renderer: ${node.type}]`);
           }

           return h('div', { class: 'mb-4', id: `field-wrapper-${node.id}` }, [labelVNode, inputVNode]);
        };

        return () => {
            const children: VNode[] = [];
            
            // Form Fields rendering mapped per node
            const formFields = h('div', { class: 'space-y-1' }, props.schema.map(node => renderField(node)));

            if (isWizard.value && currentStageName.value) {
                const errorMapRaw = Object.keys(wizardErrors.value).reduce((acc, stg) => {
                    acc[stg] = hasStepErrors(stg);
                    return acc;
                }, {} as Record<string, boolean>);

                return h('div', { class: 'w-full' }, [
                    h(FormWizard, {
                        stages: stages.value,
                        currentStage: currentStageName.value,
                        errorMap: errorMapRaw,
                        onNextStep: nextStage,
                        onPrevStep: prevStage
                    }, {
                        default: () => formFields
                    })
                ]);
            } else {
                return h('div', { class: 'w-full' }, [formFields]);
            }
        };
      }
    });

    shadowApp.directive('imask', IMaskDirective);
    shadowApp.mount(appContainer);
  }
});

onBeforeUnmount(() => {
   if (shadowApp) {
       shadowApp.unmount();
   }
   
   // TIN-5: S3 Orphan Cleanup Event
   if (!isSubmitted.value && uploadedUuids.value.length > 0) {
       navigator.sendBeacon('/api/v1/documents/cleanup', JSON.stringify({ uuids: uploadedUuids.value }));
       console.log('📡 [TIN-5] SendBeacon ejecutado para limpiar archivos huérfanos de S3:', uploadedUuids.value);
   }
});
</script>
