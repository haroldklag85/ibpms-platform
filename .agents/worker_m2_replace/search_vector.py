import os

src_dir = r"C:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform\backend\ibpms-core\src\main\java"

for root, dirs, files in os.walk(src_dir):
    for file in files:
        if file.endswith(".java"):
            filepath = os.path.join(root, file)
            try:
                with open(filepath, "r", encoding="utf-8") as f:
                    content = f.read()
                    if "vector" in content.lower():
                        print(f"Match in {filepath}")
                        for line_no, line in enumerate(content.splitlines(), 1):
                            if "vector" in line.lower():
                                print(f"  {line_no}: {line.strip()}")
            except Exception as e:
                pass
