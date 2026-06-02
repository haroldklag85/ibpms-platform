const fs = require('fs');
const path = require('path');

function searchDir(dir) {
    const files = fs.readdirSync(dir);
    for (const file of files) {
        const fullPath = path.join(dir, file);
        const stat = fs.statSync(fullPath);
        if (stat.isDirectory()) {
            if (file === 'node_modules' || file === '.git' || file === 'target') continue;
            searchDir(fullPath);
        } else if (file.endsWith('.java') || file.endsWith('.xml') || file.endsWith('.yml') || file.endsWith('.md') || file.endsWith('.txt')) {
            const content = fs.readFileSync(fullPath, 'utf8');
            if (content.toLowerCase().includes('zero-trust')) {
                console.log(`Found "zero-trust" in: ${fullPath}`);
            }
        }
    }
}

searchDir('.');
