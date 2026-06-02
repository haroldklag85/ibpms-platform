import os
import re

test_dir = r"C:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform\backend\ibpms-core\src\test\java"

springboot_tests = []
for root, dirs, files in os.walk(test_dir):
    for file in files:
        if file.endswith(".java"):
            path = os.path.join(root, file)
            with open(path, 'r', encoding='utf-8', errors='ignore') as f:
                content = f.read()
            if "@SpringBootTest" in content:
                # Check if it extends AbstractIntegrationTest
                extends_match = re.search(r"class\s+\w+\s+extends\s+(\w+)", content)
                extends_class = extends_match.group(1) if extends_match else None
                springboot_tests.append({
                    "file": file,
                    "path": path,
                    "extends": extends_class
                })

print("Found SpringBootTest classes:")
for test in springboot_tests:
    ext_str = f"extends {test['extends']}" if test['extends'] else "does NOT extend any class"
    print(f"- {test['file']} ({ext_str})")
