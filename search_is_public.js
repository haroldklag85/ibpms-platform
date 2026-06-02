const fs = require('fs');
const path = require('path');

function searchDir(dir) {
    const files = fs.readdirSync(dir);
    for (const file of files) {
        const fullPath = path.join(dir, file);
        const stat = fs.statSync(fullPath);
        if (stat.isDirectory()) {
            searchDir(fullPath);
        } else if (file.endsWith('.sql') || file.endsWith('.xml') || file.endsWith('.yaml')) {
            const content = fs.readFileSync(fullPath, 'utf8');
            if (content.toLowerCase().includes('is_public')) {
                console.log(`Found in: ${fullPath}`);
            }
        }
    }
}

searchDir('backend/ibpms-core/src/main/resources/db/changelog');
