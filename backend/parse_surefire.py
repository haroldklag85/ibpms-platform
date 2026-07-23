import os
import xml.etree.ElementTree as ET

reports_dir = r"c:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform\backend\ibpms-core\target\surefire-reports"

if not os.path.exists(reports_dir):
    print("Surefire reports directory does not exist.")
    exit(1)

failures = 0
errors = 0

for file in os.listdir(reports_dir):
    if file.endswith(".xml") and file.startswith("TEST-"):
        filepath = os.path.join(reports_dir, file)
        try:
            tree = ET.parse(filepath)
            root = tree.getroot()
            for testcase in root.findall(".//testcase"):
                fail_node = testcase.find("failure")
                err_node = testcase.find("error")
                if fail_node is not None or err_node is not None:
                    name = testcase.get("name")
                    classname = testcase.get("classname")
                    node = fail_node if fail_node is not None else err_node
                    message = node.get("message", "")
                    # print only the first few failures/errors to not overwhelm
                    if failures + errors < 30:
                        print(f"[{'FAIL' if fail_node is not None else 'ERROR'}] {classname}.{name}")
                        print(f"  Message: {message}")
                    if fail_node is not None:
                        failures += 1
                    else:
                        errors += 1
        except Exception as e:
            print(f"Error parsing {file}: {e}")

print(f"\nTotal failures: {failures}, Total errors: {errors}")
