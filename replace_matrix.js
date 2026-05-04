const fs = require('fs');

const path = '.agentic-sync/coverage_matrix.md';
let content = fs.readFileSync(path, 'utf8');

const lines = content.split('\n');
const newLines = [];

for (let i = 0; i < lines.length; i++) {
    let line = lines[i];

    // Check if it is a table header containing | QA |
    if (line.includes('| QA |')) {
        line = line.replace('| QA |', '| Unitarios | Componente | Integración | E2E | UAT |');
        newLines.push(line);
        continue;
    }

    // Check if it is a separator line (e.g. |----|----|...)
    if (line.match(/^\|(?:\s*-+\s*\|)+$/)) {
        // Find the index of the QA column in the header (which is the previous line)
        // Wait, the previous line in newLines is already replaced. So we check lines[i-1]
        const prevLine = lines[i - 1];
        if (prevLine && prevLine.includes('| QA |')) {
            const parts = prevLine.split('|');
            let qaIndex = -1;
            for (let j = 0; j < parts.length; j++) {
                if (parts[j].trim() === 'QA') {
                    qaIndex = j;
                    break;
                }
            }
            if (qaIndex !== -1) {
                const sepParts = line.split('|');
                // Replace the QA separator with 5 separators
                sepParts[qaIndex] = '----|----|----|----|----';
                line = sepParts.join('|');
            }
        }
        newLines.push(line);
        continue;
    }

    // Check if it is a table row in a table that HAS a QA column
    // We can infer this by looking backwards for the closest header
    let inTableWithQA = false;
    let qaIndex = -1;
    for (let j = i; j >= 0; j--) {
        if (!lines[j].trim().startsWith('|')) break; // Not in a table
        if (lines[j].includes('| QA |')) {
            inTableWithQA = true;
            const parts = lines[j].split('|');
            for (let k = 0; k < parts.length; k++) {
                if (parts[k].trim() === 'QA') {
                    qaIndex = k;
                    break;
                }
            }
            break;
        }
    }

    if (inTableWithQA && qaIndex !== -1 && line.trim().startsWith('|') && !line.match(/^\|(?:\s*-+\s*\|)+$/)) {
        const parts = line.split('|');
        if (parts.length > qaIndex) {
            const qaVal = parts[qaIndex].trim();
            // Duplicate the QA value across the 5 new columns
            parts[qaIndex] = ` ${qaVal} | ${qaVal} | ${qaVal} | ${qaVal} | ${qaVal} `;
            line = parts.join('|');
        }
    }

    newLines.push(line);
}

fs.writeFileSync(path, newLines.join('\n'), 'utf8');
console.log('Matrix updated successfully.');
