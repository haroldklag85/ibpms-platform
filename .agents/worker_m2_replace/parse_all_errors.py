import os
import xml.etree.ElementTree as ET

reports_dir = r"c:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform\backend\ibpms-core\target\surefire-reports"
unique_causes = {}

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
                    msg = node.get("message", "")
                    text = node.text or ""
                    
                    if "failure threshold" in msg or "failure threshold" in text:
                        continue
                    
                    # Find root cause or SQL error
                    lines = text.splitlines()
                    cause = msg
                    for line in lines:
                        if "Caused by:" in line or "ERROR:" in line or "Exception:" in line:
                            # keep updating to find the last or deepest cause
                            cause = line.strip()
                    
                    classname = testcase.get("classname")
                    method = testcase.get("name")
                    unique_causes[(classname, method)] = (msg, cause, lines[:10])
        except Exception as e:
            print("Error parsing", file, e)

with open("all_real_errors.txt", "w", encoding="utf-8") as out_f:
    out_f.write(f"Total unique real failures: {len(unique_causes)}\n")
    for (classname, method), (msg, cause, trace_head) in unique_causes.items():
        out_f.write(f"=== {classname}.{method} ===\n")
        out_f.write(f"  Msg  : {msg}\n")
        out_f.write(f"  Cause: {cause}\n")
        out_f.write(f"  Trace:\n")
        for line in trace_head:
            out_f.write(f"    {line}\n")
        out_f.write("\n")

