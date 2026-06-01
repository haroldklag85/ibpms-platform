import os
import xml.etree.ElementTree as ET

reports_dir = r"c:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform\backend\ibpms-core\target\surefire-reports"

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
                    node = fail_node if fail_node is not None else err_node
                    classname = testcase.get("classname")
                    name = testcase.get("name")
                    text = node.text if node.text else ""
                    print(f"==================================================")
                    print(f"FAILED TEST: {classname}.{name}")
                    print(f"MESSAGE: {node.get('message', '')}")
                    print(f"STACK TRACE:")
                    lines = text.splitlines()
                    for line in lines[:15]: # Print first 15 lines of stack trace
                        print("  " + line)
                    print(f"==================================================")
        except Exception as e:
            print(f"Error parsing {file}: {e}")
