const fs = require('fs');
const file = 'C:\\Users\\HaroltAndrésGómezAgu\\ProyectoAntigravity\\ibpms-platform\\frontend\\src\\components\\workdesk\\TaskPreviewModal.vue';
let content = fs.readFileSync(file, 'utf8');
content = content.replace('<button v-if="!readOnly" @click="handleClaim"', '<button @click="handleClaim"');
fs.writeFileSync(file, content);
console.log('Fixed button!');
