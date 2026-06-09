import os
import xml.etree.ElementTree as ET
import time

reports_dir = r"C:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform\backend\ibpms-core\target\surefire-reports"
now = time.time()

real_errors = {}

for file in os.listdir(reports_dir):
    if file.endswith(".xml") and file.startswith("TEST-"):
        filepath = os.path.join(reports_dir, file)
        mtime = os.path.getmtime(filepath)
        if now - mtime > 900: # 15 mins
            continue
        try:
            tree = ET.parse(filepath)
            root = tree.getroot()
            for testcase in root.findall(".//testcase"):
                err_node = testcase.find("error")
                fail_node = testcase.find("failure")
                node = err_node if err_node is not None else fail_node
                if node is not None:
                    classname = testcase.get("classname")
                    name = testcase.get("name")
                    msg = node.get("message", "")
                    text = node.text or ""
                    
                    if "failure threshold" in msg or "failure threshold" in text:
                        continue
                    
                    # Find the real cause in the stack trace
                    cause = msg
                    lines = text.splitlines()
                    for line in lines:
                        if "Caused by:" in line:
                            cause = line.strip()
                            
                    key = (classname, name)
                    real_errors[key] = (msg, cause)
        except Exception as e:
            print("Error parsing", file, e)

print(f"Found {len(real_errors)} unique real failure/error cases (excluding context threshold):")
for (classname, name), (msg, cause) in sorted(real_errors.items()):
    print(f"CLASS: {classname}")
    print(f"METHOD: {name}")
    print(f"MSG: {msg}")
    print(f"CAUSE: {cause}")
    print("-" * 50)
