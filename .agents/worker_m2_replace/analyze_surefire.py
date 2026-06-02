import os
import xml.etree.ElementTree as ET

# Use relative paths to avoid Windows username encoding issues
current_dir = os.path.dirname(os.path.abspath(__file__))
reports_dir = os.path.abspath(os.path.join(current_dir, "..", "..", "backend", "ibpms-core", "target", "surefire-reports"))

print("Scanning directory:", reports_dir)
if not os.path.exists(reports_dir):
    print("Directory does not exist:", reports_dir)
    exit(1)

failures = []
errors = []

for file in sorted(os.listdir(reports_dir)):
    if file.endswith(".xml") and file.startswith("TEST-"):
        filepath = os.path.join(reports_dir, file)
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

print(f"\nTOTAL FAILURES: {len(failures)}")
print(f"TOTAL ERRORS: {len(errors)}")

# Group by failure messages to see the real root causes
cause_groups = {}
for c, n, m in failures + errors:
    # simplify message
    simplified = m.split(":")[-1].strip() if ":" in m else m
    # limit length
    simplified = (simplified[:150] + "...") if len(simplified) > 150 else simplified
    cause_groups[simplified] = cause_groups.get(simplified, 0) + 1

print("\n=== TOP ROOT CAUSES ===")
for msg, count in sorted(cause_groups.items(), key=lambda x: x[1], reverse=True):
    print(f"[{count} times]: {msg}")
