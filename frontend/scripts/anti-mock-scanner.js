import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

// Directories to scan
const SCAN_DIRS = [
  path.join(__dirname, '../src'),
  path.join(__dirname, '../../backend')
];

// Patterns that trigger the linter
const BANNED_PATTERNS = [
  { regex: /(?:const|let|var)\s+mock[A-Z0-9_a-z]*\s*=/g, description: "Hardcoded mock variable declaration" },
  { regex: /validateStatus:\s*\(\)\s*=>\s*true/g, description: "Bypassing HTTP error statuses (validateStatus: () => true)" }
];

let hasErrors = false;

function scanFile(filePath) {
  const rawContent = fs.readFileSync(filePath, 'utf-8');
  const content = rawContent.split('\n').filter(line => !line.includes('// zero-mock-ignore')).join('\n');
  let fileHasErrors = false;

  BANNED_PATTERNS.forEach(pattern => {
    const matches = [...content.matchAll(pattern.regex)];
    if (matches.length > 0) {
      if (!fileHasErrors) {
        console.error(`\n❌ Anti-Mock Violation in: ${filePath}`);
        fileHasErrors = true;
        hasErrors = true;
      }
      matches.forEach(match => {
        console.error(`  -> Found banned pattern: "${pattern.description}"`);
        // Find line number
        const lineNumber = content.substring(0, match.index).split('\n').length;
        console.error(`     Line ${lineNumber}: ${match[0]}`);
      });
    }
  });
}

function walkDir(dir) {
  if (!fs.existsSync(dir)) return;
  const files = fs.readdirSync(dir);
  for (const file of files) {
    const fullPath = path.join(dir, file);
    const stat = fs.statSync(fullPath);
    if (stat.isDirectory()) {
      walkDir(fullPath);
    } else {
      if (fullPath.endsWith('.broken')) {
        console.error(`\n❌ Anti-Evasion Violation in: ${fullPath}`);
        console.error(`  -> Found ghost test file (.broken extension). Broken tests must be fixed, not hidden.`);
        hasErrors = true;
      } else if ((fullPath.endsWith('.vue') || fullPath.endsWith('.ts') || fullPath.endsWith('.js')) && !fullPath.endsWith('.spec.ts') && !fullPath.endsWith('.test.ts') && !fullPath.includes('__tests__')) {
        scanFile(fullPath);
      }
    }
  }
}

console.log("🔍 Scanning for hardcoded mocks and security bypasses...");

SCAN_DIRS.forEach(dir => walkDir(dir));

if (hasErrors) {
  console.error("\n🚫 COMMIT BLOCKED: Mock data or validation bypasses detected in production code.");
  console.error("Please remove hardcoded mocks. Mocking should only happen in tests.");
  process.exit(1);
} else {
  console.log("✅ Anti-Mock scan passed. No violations found.");
  process.exit(0);
}
