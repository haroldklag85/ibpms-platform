import os
import re

test_dir = r"C:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform\backend\ibpms-core\src\test\java"

tests = []
for root, dirs, files in os.walk(test_dir):
    for file in files:
        if file.endswith(".java"):
            path = os.path.join(root, file)
            with open(path, 'r', encoding='utf-8', errors='ignore') as f:
                content = f.read()
            extends_match = re.search(r"class\s+(\w+)\s+extends\s+(\w+)", content)
            extends_class = extends_match.group(2) if extends_match else None
            class_name = extends_match.group(1) if extends_match else None
            
            if extends_class in ["AbstractIntegrationTest", "AbstractLocalE2ETest", "TestcontainersBaseIT"]:
                tests.append({
                    "class": class_name,
                    "file": file,
                    "extends": extends_class,
                    "path": path
                })

print("Found tests extending integration bases:")
for t in sorted(tests, key=lambda x: x["extends"]):
    print(f"- {t['class']} extends {t['extends']} ({t['file']})")
