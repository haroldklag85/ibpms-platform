import os
import xml.etree.ElementTree as ET
import time

reports_dir = os.path.join("..", "..", "backend", "ibpms-core", "target", "surefire-reports")

failures = []
errors = []
now = time.time()

if not os.path.exists(reports_dir):
    print(f"Directory not found: {reports_dir}")
    exit(1)

for file in os.listdir(reports_dir):
    if file.endswith(".xml") and file.startswith("TEST-"):
        filepath = os.path.join(reports_dir, file)
        mtime = os.path.getmtime(filepath)
        if now - mtime > 900:
            continue
        try:
            tree = ET.parse(filepath)
            root = tree.getroot()
            for testcase in root.findall(".//testcase"):
                fail_node = testcase.find("failure")
                err_node = testcase.find("error")
                if fail_node is not None or err_node is not None:
                    node = fail_node if fail_node is not None else err_node
                    classname = testcase.get("classname")
                    name = testcase.get("name")
                    msg = node.get('message', '')
                    item = (classname, name, msg)
                    if fail_node is not None:
                        failures.append(item)
                    else:
                        errors.append(item)
        except Exception as e:
            print(f"Error parsing {file}: {e}")

with open("fresh_failures.txt", "w", encoding="utf-8") as f:
    f.write(f"TOTAL FRESH FAILURES: {len(failures)}\n")
    f.write(f"TOTAL FRESH ERRORS: {len(errors)}\n\n")
    f.write("=== FRESH FAILURES ===\n")
    for c, n, m in sorted(failures):
        f.write(f"{c}.{n}: {m}\n")
    f.write("\n=== FRESH ERRORS ===\n")
    for c, n, m in sorted(errors):
        f.write(f"{c}.{n}: {m}\n")
print(f"Successfully wrote {len(failures)} failures and {len(errors)} errors to fresh_failures.txt.")
