import xml.etree.ElementTree as ET

filepath = r"C:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform\backend\ibpms-core\target\surefire-reports\TEST-com.ibpms.poc.FormCompletionSagaTest.xml"
output_file = r"C:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform\.agents\worker_m2_replace\saga_error_detail.txt"

try:
    tree = ET.parse(filepath)
    root = tree.getroot()
    for testcase in root.findall(".//testcase"):
        err = testcase.find("error")
        if err is not None and "threshold" not in err.get("message", ""):
            with open(output_file, "w", encoding="utf-8") as out:
                out.write(err.text or "")
            print("Wrote full error to saga_error_detail.txt")
            break
except Exception as e:
    print("Error:", e)
