#!/usr/bin/env node

const fs = require('fs');
const path = require('path');

const TEST_DIR = path.resolve(__dirname, '../backend/ibpms-core/src/test/java');

// Red lines: imports or annotations indicating Spring Boot context loading inside *Test.java (unit tests)
const LEAKY_PATTERNS = [
  /@SpringBootTest/,
  /@WebMvcTest/,
  /@DataJpaTest/,
  /@JsonTest/,
  /@MockBean/,
  /@SpyBean/,
  /@ActiveProfiles/,
  /extends\s+AbstractIntegrationIT/,
  /extends\s+AbstractLocalE2EIT/,
  /extends\s+BaseWebMvcIT/,
  /import\s+org\.springframework\.boot\.test/,
  /import\s+org\.springframework\.test\.context/
];

function scanDirectory(dir, failures = []) {
  const files = fs.readdirSync(dir);
  
  for (const file of files) {
    const fullPath = path.join(dir, file);
    const stat = fs.statSync(fullPath);
    
    if (stat.isDirectory()) {
      scanDirectory(fullPath, failures);
    } else if (file.endsWith('Test.java') && !file.endsWith('IT.java')) {
      const content = fs.readFileSync(fullPath, 'utf8');
      
      const foundLeaks = [];
      for (const pattern of LEAKY_PATTERNS) {
        if (pattern.test(content)) {
          foundLeaks.push(pattern.toString());
        }
      }
      
      if (foundLeaks.length > 0) {
        failures.push({
          file: path.relative(path.resolve(__dirname, '..'), fullPath),
          leaks: foundLeaks
        });
      }
    }
  }
  
  return failures;
}

console.log('🕵️ Running Anti-Integration Leak Scanner on unit tests (*Test.java)...');
const failures = scanDirectory(TEST_DIR);

if (failures.length > 0) {
  console.error('\n🛑 ERROR: Detected Integration Leak in pure unit tests!');
  console.error('Unit tests ending with "*Test.java" MUST NOT load Spring Boot context.');
  console.error('Rename these files to end with "*IT.java" if they require Spring, or remove Spring dependencies.');
  console.error('\nLeaking files list:');
  failures.forEach(f => {
    console.error(`  - 📄 ${f.file}`);
    console.error(`       Violations: ${f.leaks.join(', ')}`);
  });
  console.error('\nCommit BLOCKED. Please fix the file naming or annotations.\n');
  process.exit(1);
} else {
  console.log('✅ Success: No integration leaks detected in unit tests.');
  process.exit(0);
}
