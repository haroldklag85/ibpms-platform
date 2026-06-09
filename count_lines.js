const fs = require('fs');
const path = require('path');

function countLines(dir, extensions, ignoreDirs) {
    let lines = 0;
    if (!fs.existsSync(dir)) return 0;

    const files = fs.readdirSync(dir);
    for (const file of files) {
        const fullPath = path.join(dir, file);
        const stat = fs.statSync(fullPath);

        if (stat.isDirectory()) {
            if (!ignoreDirs.includes(file)) {
                lines += countLines(fullPath, extensions, ignoreDirs);
            }
        } else {
            const ext = path.extname(file).toLowerCase();
            if (extensions.includes(ext) || extensions.length === 0) {
                try {
                    const content = fs.readFileSync(fullPath, 'utf8');
                    lines += content.split('\n').length;
                } catch (e) {
                    // ignore binary or unreadable files
                }
            }
        }
    }
    return lines;
}

const ignoreDirs = ['node_modules', 'dist', 'target', '.git', 'coverage', '.idea', '.vscode'];

const backendExts = ['.java', '.xml', '.properties', '.yml', '.yaml', '.sql'];
const frontendExts = ['.ts', '.js', '.vue', '.html', '.css', '.scss', '.json'];
const docsExts = ['.md', '.txt', '.json', '.html', '.csv'];
const qaExts = ['.md', '.json', '.ts', '.yml', '.yaml'];

const backendLines = countLines('backend', backendExts, ignoreDirs);
const frontendLines = countLines('frontend', frontendExts, ignoreDirs);
const docsLines = countLines('docs', docsExts, ignoreDirs);

// Let's assume QA is under .agent, .agents, .agentic-sync, tests, or similar
let qaLines = 0;
qaLines += countLines('.agent', qaExts, ignoreDirs);
qaLines += countLines('.agents', qaExts, ignoreDirs);
qaLines += countLines('.agentic-sync', qaExts, ignoreDirs);
if (fs.existsSync('frontend/e2e')) {
    qaLines += countLines('frontend/e2e', ['.ts', '.js'], ignoreDirs);
}
if (fs.existsSync('backend/src/test')) {
    qaLines += countLines('backend/src/test', ['.java'], ignoreDirs);
}

console.log(JSON.stringify({
    backend: backendLines,
    frontend: frontendLines,
    qa: qaLines,
    docs: docsLines
}, null, 2));
