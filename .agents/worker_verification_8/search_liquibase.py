import os

root_dir = r"C:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform"

matches = []
for root, dirs, files in os.walk(root_dir):
    if ".git" in root or ".agents" in root:
        continue
    for file in files:
        if file.endswith((".java", ".properties", ".yml", ".yaml")):
            path = os.path.join(root, file)
            try:
                with open(path, 'r', encoding='utf-8', errors='ignore') as f:
                    for i, line in enumerate(f):
                        if "spring.liquibase.enabled" in line or "liquibase.enabled" in line:
                            matches.append((path, i + 1, line.strip()))
            except Exception as e:
                pass

print("Found references:")
for path, line_no, content in matches:
    print(f"- {path}:{line_no} -> {content}")
