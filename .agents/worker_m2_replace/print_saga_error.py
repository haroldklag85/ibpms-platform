import xml.etree.ElementTree as ET

filepath = r"C:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform\backend\ibpms-core\target\surefire-reports\TEST-com.ibpms.poc.FormCompletionSagaTest.xml"

try:
    tree = ET.parse(filepath)
    root = tree.getroot()
    print("ROOT TAG:", root.tag)
    print("ATTRIBS:", root.attrib)
    for child in root:
        print("CHILD TAG:", child.tag, "ATTRIBS:", child.attrib)
        for testcase in child.findall(".//testcase"):
            print("  TESTCASE TAG:", testcase.tag, "NAME:", testcase.get("name"))
        # Let's search directly
        if child.tag == "testcase":
            print("  TESTCASE name:", child.get("name"))
            fail = child.find("failure")
            err = child.find("error")
            print("  has failure:", fail is not None, "has error:", err is not None)
            if fail is not None:
                print("  failure msg:", fail.get("message"))
                print("  failure text (first 10 lines):")
                lines = (fail.text or "").splitlines()
                for line in lines[:100]:
                    print("    ", line)
            if err is not None:
                print("  error msg:", err.get("message"))
                print("  error text (first 100 lines):")
                lines = (err.text or "").splitlines()
                for line in lines[:100]:
                    print("    ", line)
except Exception as e:
    print("Error:", e)
