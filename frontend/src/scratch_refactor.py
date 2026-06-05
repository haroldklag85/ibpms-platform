import re

with open(r'c:\Users\USER\Desktop\Proyectos\Harold Ibpms\ibpms-platform\frontend\src\components\forms\FormRenderer.vue', 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Imports
content = content.replace(
    "import { useWizardValidation } from '@/composables/useWizardValidation';",
    "import { useWizardValidation } from '@/composables/useWizardValidation';\nimport { useTaskSync } from '@/composables/useTaskSync';"
)

# 2. useTaskSync
content = content.replace(
    "const submitForm = ref<() => Promise<void>>();\nconst submitError = ref<string | null>(null);",
    "const submitForm = ref<() => Promise<void>>();\nconst submitError = ref<string | null>(null);\n\n// @Traceability: US-029, CA-30, CA-31\nconst { isDuplicateTab, syncStatus } = useTaskSync(props.taskId || 'new');"
)

# 3. ReadOnly disabled + CSS
content = content.replace(
    "const disabled = isDisabled(node);",
    "const disabled = isDisabled(node) || node.readOnly;"
)
# update class for text inputs
content = re.sub(
    r"class: 'form-input w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm',",
    "class: 'form-input w-full rounded-md shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm ' + (node.readOnly ? 'bg-[#F5F5F5] border-[#e5e7eb] cursor-not-allowed pl-8' : 'border-gray-300'),",
    content
)
# update class for number inputs
content = re.sub(
    r"class: 'form-input w-full rounded-md border-gray-300 shadow-sm sm:text-sm',",
    "class: 'form-input w-full rounded-md shadow-sm sm:text-sm ' + (node.readOnly ? 'bg-[#F5F5F5] border-[#e5e7eb] cursor-not-allowed pl-8' : 'border-gray-300'),",
    content
)

# 4. wrap text and number inputs with relative div and padlock
def replace_input_vnode(match):
    text = match.group(0)
    return text + "\n               if (node.readOnly) {\n                   inputVNode = h('div', { class: 'relative w-full' }, [\n                       h('span', { class: 'absolute left-2 top-1/2 transform -translate-y-1/2 text-gray-500 z-10 select-none' }, '🔒'),\n                       inputVNode\n                   ]);\n               }"

# For text type block
content = re.sub(r"inputVNode = h\('input', attrs\);", replace_input_vnode, content)
# For number type block
content = re.sub(r"onBlur: \(\) => validateField\(node\)\n\s*}\);", replace_input_vnode, content)


# 5. Modify localSubmitForm and showEmptyConfirmModal
old_submit_block = """        const localSubmitError = ref<string | null>(null);
        const localSubmitForm = async () => {
            localSubmitError.value = null;
            submitError.value = null;
            try {
                const taskId = props.taskId || 'mock-task';
                await apiClient.post(`/api/v1/workbox/tasks/${taskId}/complete`, formData.value);
                notifySubmit();
                if (props.taskId) {
                    localStorage.removeItem(`draft_task_${props.taskId}`);
                }
            } catch (err: any) {
                console.error("Smart Button submit error:", err);
                localSubmitError.value = err.message || "Un error ha ocurrido durante la sumisión.";
                submitError.value = localSubmitError.value;
            }
        };
        submitForm.value = localSubmitForm;"""

new_submit_block = """        const localSubmitError = ref<string | null>(null);
        const showEmptyConfirmModal = ref(false); // CA-32

        const executeSubmit = async () => {
            localSubmitError.value = null;
            submitError.value = null;
            try {
                isAsyncLoading.value = true;
                const taskId = props.taskId || 'mock-task';
                await apiClient.post(`/api/v1/workbox/tasks/${taskId}/complete`, formData.value);
                notifySubmit();
                if (props.taskId) {
                    localStorage.removeItem(`draft_task_${props.taskId}`);
                }
            } catch (err: any) {
                console.error("Smart Button submit error:", err);
                localSubmitError.value = err.message || "Un error ha ocurrido durante la sumisión.";
                submitError.value = localSubmitError.value;
            } finally {
                isAsyncLoading.value = false;
            }
        };

        const localSubmitForm = async () => {
            // Check if validateCurrentStageZod exists and call it
            if (typeof validateCurrentStageZod === 'function') {
                if (!validateCurrentStageZod()) return;
            }

            let hasRequired = false;
            const traverseCheck = (nodes: any[]) => {
                for(const n of nodes) {
                    if (isVisible(n) && n.required) hasRequired = true;
                    if (n.children) traverseCheck(n.children);
                }
            };
            traverseCheck(props.schema);

            if (!hasRequired) {
                showEmptyConfirmModal.value = true;
                return;
            }
            await executeSubmit();
        };
        // Will set submitForm.value later after validateCurrentStageZod is defined"""

content = content.replace(old_submit_block, new_submit_block)

# Add submitForm.value = localSubmitForm at the end of setup
content = content.replace("const fetchAsyncData = useDebounceFn", "submitForm.value = localSubmitForm;\n\n        const fetchAsyncData = useDebounceFn")

# 6. Button submit logic
old_button_submit = """            else if (node.type === 'button_submit') {
                inputVNode = h('button', {
                    type: 'submit',
                    class: 'w-full px-4 py-2 font-bold text-white bg-indigo-600 hover:bg-indigo-700 rounded-md shadow-sm transition',
                    onClick: (e: Event) => {
                        e.preventDefault();
                        localSubmitForm();
                    }
                }, node.label || 'Submit');
            }"""

new_button_submit = """            else if (node.type === 'button_submit') {
                inputVNode = h('button', {
                    type: 'submit',
                    disabled: isDuplicateTab.value || isAsyncLoading.value,
                    class: 'w-full px-4 py-2 font-bold text-white bg-indigo-600 hover:bg-indigo-700 rounded-md shadow-sm transition' + (isDuplicateTab.value ? ' opacity-50 cursor-not-allowed' : ''),
                    onClick: (e: Event) => {
                        e.preventDefault();
                        if (!isDuplicateTab.value) {
                            localSubmitForm();
                        }
                    }
                }, [
                    isAsyncLoading.value ? h('span', { class: 'animate-spin mr-2 inline-block' }, '↻') : null,
                    isDuplicateTab.value ? 'Pestaña Duplicada (Bloqueado)' : (isAsyncLoading.value ? 'Guardando en el servidor...' : (node.label || 'Submit'))
                ]);
            }"""

content = content.replace(old_button_submit, new_button_submit)

# 7. Add Teleport and Duplicate Warning to children
old_return_children = """        return () => {
            const children: VNode[] = [];"""

new_return_children = """        return () => {
            const children: VNode[] = [];
            
            if (isDuplicateTab.value) {
                 children.push(h('div', {
                    class: 'error-banner alert-warning p-3 bg-yellow-100 text-yellow-800 border border-yellow-300 rounded-md mb-4 flex items-center font-bold',
                    role: 'alert'
                 }, '⚠️ Estás editando esta tarea en otra pestaña. Por seguridad, esta vista ha sido bloqueada.'));
            }

            if (showEmptyConfirmModal.value) {
                children.push(h(Teleport, { to: 'body' }, [
                    h('div', { class: 'fixed inset-0 bg-black/50 z-[900] flex items-center justify-center p-4' }, [
                        h('div', { class: 'bg-white p-6 rounded-lg max-w-md w-full shadow-xl text-left' }, [
                            h('h3', { class: 'text-lg font-bold mb-4' }, '¿Estás seguro de que deseas completar esta tarea?'),
                            h('p', { class: 'mb-4 text-gray-600' }, 'Esta acción no se puede deshacer.'),
                            h('div', { class: 'flex justify-end gap-2' }, [
                                h('button', {
                                    class: 'px-4 py-2 bg-gray-200 text-gray-800 rounded hover:bg-gray-300 font-medium',
                                    onClick: (e: Event) => { e.preventDefault(); showEmptyConfirmModal.value = false; }
                                }, 'Cancelar'),
                                h('button', {
                                    class: 'px-4 py-2 bg-indigo-600 text-white rounded hover:bg-indigo-700 font-bold flex items-center',
                                    onClick: (e: Event) => {
                                        e.preventDefault();
                                        showEmptyConfirmModal.value = false;
                                        executeSubmit();
                                    }
                                }, 'Sí, completar')
                            ])
                        ])
                    ])
                ]));
            }"""

content = content.replace(old_return_children, new_return_children)

with open(r'c:\Users\USER\Desktop\Proyectos\Harold Ibpms\ibpms-platform\frontend\src\components\forms\FormRenderer.vue', 'w', encoding='utf-8') as f:
    f.write(content)

print("Done")
