const fs = require('fs');

const path = '.agentic-sync/coverage_matrix.md';
let content = fs.readFileSync(path, 'utf8');
const lines = content.split('\n');
const newLines = [];

for (let i = 0; i < lines.length; i++) {
    let line = lines[i];

    // Modify header
    if (line.includes('| CA | Título (corto) | Back | Front | Unitarios |')) {
        if (!line.includes('Spec File')) {
            line = line.replace('| Notas |', '| Spec File | Notas |');
        }
        newLines.push(line);
        continue;
    }

    // Modify separator
    if (line.match(/^\|(?:\s*-+\s*\|)+$/)) {
        const prevLine = newLines[i - 1];
        if (prevLine && prevLine.includes('| Spec File |')) {
            // Count separators
            const expectedPipes = prevLine.split('|').length;
            const currentPipes = line.split('|').length;
            if (currentPipes < expectedPipes) {
                line = line.slice(0, -1) + '---|' + '|'; // add one column
                // Let's just regenerate the separator to match the header length
                line = '|' + Array(expectedPipes - 2).fill('---').join('|') + '|';
            }
        }
        newLines.push(line);
        continue;
    }

    // Modify row
    let inTable = false;
    for (let j = i; j >= 0; j--) {
        if (!lines[j].trim().startsWith('|')) break;
        if (lines[j].includes('| CA | Título (corto) | Back | Front | Unitarios |')) {
            inTable = true;
            break;
        }
    }

    if (inTable && line.trim().startsWith('|') && !line.match(/^\|(?:\s*-+\s*\|)+$/)) {
        // It's a data row
        const parts = line.split('|');
        const headerParts = newLines[i - 1].split('|').length;
        // Check if row has fewer columns than header
        if (parts.length < headerParts && !line.includes('Spec File')) {
            // We need to insert a column before "Notas". "Notas" is usually the last one.
            // parts length is e.g. 11, header is 12
            // The last real column is parts[parts.length-2]
            const notas = parts[parts.length - 2];
            
            // Look for existing spec references in "Notas" or "E2E" column
            let specFile = ' ❌ Ninguno ';
            const e2eColIndex = 8; // | (0) | CA (1) | Título (2) | Back (3) | Front (4) | Unit (5) | Comp (6) | Int (7) | E2E (8) | UAT (9) | Spec (10) | Notas (11) |
            
            if (parts.length > e2eColIndex) {
                const e2eText = parts[e2eColIndex];
                const match = e2eText.match(/([a-zA-Z0-9_-]+\.spec\.ts)/) || notas.match(/([a-zA-Z0-9_-]+\.spec\.ts)/);
                if (match) {
                    specFile = ` ${match[1]} `;
                }
            }

            parts.splice(parts.length - 2, 0, specFile);
            line = parts.join('|');
            
            // Also, update the "UAT" or "E2E" to ❌ if Spec is 'Ninguno' and it was marked as ✅?
            // The instruction says: "Si un CA no tiene un .spec.ts documentado, será invalidado temporalmente."
            if (specFile.includes('Ninguno')) {
                 if (parts[e2eColIndex].includes('✅')) {
                     parts[e2eColIndex] = parts[e2eColIndex].replace('✅', '❌');
                 }
                 const uatColIndex = 9;
                 if (parts[uatColIndex] && parts[uatColIndex].includes('✅')) {
                     parts[uatColIndex] = parts[uatColIndex].replace('✅', '❌');
                 }
                 line = parts.join('|');
            }
        }
    }

    newLines.push(line);
}

fs.writeFileSync(path, newLines.join('\n'), 'utf8');
console.log('Spec column added successfully.');
