import os
import xml.etree.ElementTree as ET
import time

reports_dir = r"C:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform\backend\ibpms-core\target\surefire-reports"

failures = []
errors = []

now = time.time()

for file in os.listdir(reports_dir):
    if file.endswith(".xml") and file.startswith("TEST-"):
        filepath = os.path.join(reports_dir, file)
        mtime = os.path.getmtime(filepath)
        # Check if modified in the last 15 minutes (900 seconds)
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

print(f"TOTAL FRESH FAILURES: {len(failures)}")
print(f"TOTAL FRESH ERRORS: {len(errors)}")

print("\n=== FRESH FAILURES ===")
for c, n, m in sorted(failures):
    print(f"{c}.{n}: {m[:150]}")

print("\n=== FRESH ERRORS ===")
for c, n, m in sorted(errors):
    print(f"{c}.{n}: {m[:150]}")
