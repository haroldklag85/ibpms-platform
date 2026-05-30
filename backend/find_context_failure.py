import os
import xml.etree.ElementTree as ET

reports_dir = r"c:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform\backend\ibpms-core\target\surefire-reports"

if not os.path.exists(reports_dir):
    print("Surefire reports directory does not exist.")
    exit(1)

for file in os.listdir(reports_dir):
    if file.endswith(".xml") and file.startswith("TEST-"):
        filepath = os.path.join(reports_dir, file)
        try:
            tree = ET.parse(filepath)
            root = tree.getroot()
            for testcase in root.findall(".//testcase"):
                err_node = testcase.find("error")
                if err_node is not None:
                    text = err_node.text or ""
                    if "Caused by" in text:
                        print(f"File: {file} | Case: {testcase.get('name')}")
                        for line in text.splitlines():
                            if "Caused by" in line or "BeanCreationException" in line or "NoSuchBeanDefinitionException" in line or "UnsatisfiedDependencyException" in line:
                                print("  ", line.strip())
                        print("-" * 80)
                        break # Only print one testcase per class to keep output readable
        except Exception as e:
            print(f"Error parsing {file}: {e}")
