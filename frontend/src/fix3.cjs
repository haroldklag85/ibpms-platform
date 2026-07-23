const fs = require('fs');
const file = 'C:\\Users\\HaroltAndrésGómezAgu\\ProyectoAntigravity\\ibpms-platform\\frontend\\src\\components\\workdesk\\TaskPreviewModal.vue';
let content = fs.readFileSync(file, 'utf8');

content = content.replace(
  '<button @click="handleClaim" :disabled="isLoading || isClaiming || isAlreadyClaimed" class="px-5 py-2 bg-indigo-600 text-white rounded-md font-bold text-sm hover:bg-indigo-700 shadow-sm transition flex gap-2 items-center disabled:opacity-50 disabled:bg-gray-400 disabled:cursor-not-allowed" data-test="btn-claim">',
  '<button v-if="!(readOnly && taskDetail?.assignee)" @click="handleClaim" :disabled="isLoading || isClaiming || isAlreadyClaimed" class="px-5 py-2 bg-indigo-600 text-white rounded-md font-bold text-sm hover:bg-indigo-700 shadow-sm transition flex gap-2 items-center disabled:opacity-50 disabled:bg-gray-400 disabled:cursor-not-allowed" data-test="btn-claim">'
);

const handleClaimRegex = /const handleClaim = async \(\) => \{\s+if \(!props.taskId\) return;\s+isClaiming.value = true;\s+try \{\s+await store.claimTask\(props.taskId\);\s+emit\('close'\);\s+\} catch\(e\) \{\s+emit\('close'\);\s+\} finally \{\s+isClaiming.value = false;\s+\}\s+\};/s;

const newHandleClaim = `const handleClaim = async () => {
    if (!props.taskId) return;
    isClaiming.value = true;
    try {
        await store.claimTask(props.taskId);
        emit('close');
    } catch(err) {
        if (err.response && err.response.status === 409) {
            isAlreadyClaimed.value = true;
        } else {
            emit('close'); 
        }
    } finally {
        isClaiming.value = false;
    }
};`;

content = content.replace(handleClaimRegex, newHandleClaim);
fs.writeFileSync(file, content);
console.log('Done OBS-F02 and OBS-F03');
