import os
import xml.etree.ElementTree as ET

reports_dir = r"c:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform\backend\ibpms-core\target\surefire-reports"
output_file = r"c:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform\.agents\worker_m2_replace\errors_utf8.txt"

with open(output_file, "w", encoding="utf-8") as out:
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
                        text = node.text if node.text else ""
                        out.write(f"==================================================\n")
                        out.write(f"FAILED TEST: {classname}.{name}\n")
                        out.write(f"MESSAGE: {node.get('message', '')}\n")
                        out.write(f"STACK TRACE:\n")
                        lines = text.splitlines()
                        for line in lines[:30]: # Print first 30 lines of stack trace
                            out.write("  " + line + "\n")
                        out.write(f"==================================================\n\n")
            except Exception as e:
                out.write(f"Error parsing {file}: {e}\n")

print("Done writing errors_utf8.txt")
