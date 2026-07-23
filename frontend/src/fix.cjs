const fs = require('fs');
const path = require('path');
const file = path.join('C:\\Users\\HaroltAndrésGómezAgu\\ProyectoAntigravity\\ibpms-platform\\frontend\\src\\components\\workdesk\\TaskPreviewModal.vue');
let content = fs.readFileSync(file, 'utf8');

// Buscamos el boton
const regex = /<button @click="handleClaim" :disabled="isLoading \|\| isClaiming" class="px-5 py-2 bg-indigo-600 text-white rounded-md font-bold text-sm hover:bg-indigo-700 shadow-sm transition flex gap-2 items-center disabled:opacity-50" data-test="btn-claim">\s*<span v-if="isClaiming" class="animate-spin h-4 w-4 border-2 border-white border-t-transparent rounded-full"><\/span>\s*\{\{\s*isClaiming \? 'Reclamando...' : 'Reclamar Tarea'\s*\}\}\s*<\/button>/g;

content = content.replace(regex, `<button v-if="!readOnly" @click="handleClaim" :disabled="isLoading || isClaiming || isAlreadyClaimed" class="px-5 py-2 bg-indigo-600 text-white rounded-md font-bold text-sm hover:bg-indigo-700 shadow-sm transition flex gap-2 items-center disabled:opacity-50 disabled:bg-gray-400 disabled:cursor-not-allowed" data-test="btn-claim">
                   <span v-if="isAlreadyClaimed" class="text-sm">🔒</span>
                   <span v-else-if="isClaiming" class="animate-spin h-4 w-4 border-2 border-white border-t-transparent rounded-full"></span>
                   {{ isAlreadyClaimed ? 'No Disponible' : (isClaiming ? 'Reclamando...' : 'Reclamar Tarea') }}
               </button>`);

fs.writeFileSync(file, content);
console.log('Done replacing');
