const fs = require('fs');

const file = 'C:\\Users\\HaroltAndrésGómezAgu\\ProyectoAntigravity\\ibpms-platform\\frontend\\src\\components\\forms\\generic\\GenericFormBody.vue';
let content = fs.readFileSync(file, 'utf8');

// Add imports for session lock, onMounted, onUnmounted, nextTick
if (!content.includes('useSessionLock')) {
    content = content.replace(
        "import { computed, ref } from 'vue'",
        "import { computed, ref, onMounted, onUnmounted, nextTick } from 'vue'\nimport { useSessionLock } from '@/composables/workdesk/useSessionLock'"
    );
}

// Inject lock usage and beforeunload inside script setup
if (!content.includes('const { isLocked } = useSessionLock(store.taskId)')) {
    content = content.replace(
        "const router = useRouter()",
        `const router = useRouter()
const { isLocked } = useSessionLock(store.taskId)

// FRONT-029-12: Anti-Envío Accidental (beforeunload)
const handleBeforeUnload = (e: BeforeUnloadEvent) => {
    if (store.syncState !== 'SYNCED' && store.observations.length > 0) {
        e.preventDefault()
        e.returnValue = ''
    }
}
onMounted(() => {
    window.addEventListener('beforeunload', handleBeforeUnload)
})
onUnmounted(() => {
    window.removeEventListener('beforeunload', handleBeforeUnload)
})
`
    );
}

// Inject remaining hours logic
if (!content.includes('const hoursRemaining = computed(')) {
    content = content.replace(
        "const missingRequiredFields = computed(() => {",
        `// FRONT-029-10: Pre-Aviso Caducidad
const hoursRemaining = computed(() => {
    if (!store.prefillData?.draftExpiresAt) return null;
    const expires = new Date(store.prefillData.draftExpiresAt).getTime();
    const now = new Date().getTime();
    const diffHours = (expires - now) / (1000 * 60 * 60);
    return diffHours > 0 && diffHours < 24 ? Math.ceil(diffHours) : null;
})

const missingRequiredFields = computed(() => {`
    );
}

// Inject auto scroll logic in onConfirmClick
if (!content.includes('scrollIntoView({ behavior:')) {
    content = content.replace(
        `  if (!isValid.value || missingRequiredFields.value.length > 0) {
    showInlineError.value = true
    return
  }`,
        `  if (!isValid.value || missingRequiredFields.value.length > 0) {
    showInlineError.value = true
    nextTick(() => {
      document.querySelector('.border-red-500, .text-red-600')?.scrollIntoView({ behavior: 'smooth', block: 'center' })
    })
    return
  }`
    );
}

// Inject template elements
// 1. Session Lock banner and disable buttons/inputs.
content = content.replace(
  '<div class="relative bg-white p-6 rounded-lg shadow-sm border border-gray-200">',
  `<div class="relative bg-white p-6 rounded-lg shadow-sm border border-gray-200">
    <!-- FRONT-029-11: Detección Sesión Duplicada -->
    <div v-if="isLocked" class="mb-4 p-4 bg-blue-50 border border-blue-200 rounded-md text-blue-800 flex items-center gap-3">
        <span class="text-2xl">⚠️</span>
        <div>
            <h4 class="font-bold">Formulario abierto en otra pestaña</h4>
            <p class="text-sm">Por seguridad, la edición en esta pestaña ha sido bloqueada. Continúa en la pestaña activa.</p>
        </div>
    </div>
    
    <!-- FRONT-029-10: Pre-Aviso Caducidad Borrador -->
    <div v-if="hoursRemaining !== null && !isLocked" class="mb-4 p-3 bg-yellow-50 border border-yellow-200 rounded-md text-yellow-800 text-sm flex gap-2 items-center">
        <span class="text-lg">⏰</span>
        <strong>Quedan {{ hoursRemaining }} horas antes de que el borrador se elimine del servidor.</strong>
    </div>
    
    <!-- Pointer events none if locked -->
    <div :class="{'opacity-50 pointer-events-none': isLocked}">`
);

// Close the pointer events wrapper div before OVERLAYS
content = content.replace(
  '<!-- OVERLAYS FRONT-029-01 & FRONT-029-02 -->',
  `</div>\n    <!-- OVERLAYS FRONT-029-01 & FRONT-029-02 -->`
);

fs.writeFileSync(file, content);
console.log('Done GenericFormBody.vue');
