import os
import xml.etree.ElementTree as ET

# Use relative paths to avoid Windows non-ASCII encoding issues
reports_dir = "target/surefire-reports"
output_file = "../../.agents/teamwork_preview_worker_verification_6/test_run_failures.log"

failures = []
errors = []

total_run = 0
total_failures = 0
total_errors = 0
total_skipped = 0

if not os.path.exists(reports_dir):
    print(f"Error: reports directory '{reports_dir}' does not exist.")
    exit(1)

for file in os.listdir(reports_dir):
    if file.startswith("TEST-") and file.endswith(".xml"):
        filepath = os.path.join(reports_dir, file)
        try:
            tree = ET.parse(filepath)
            root = tree.getroot()
            
            if root.tag == "testsuite":
                total_run += int(root.attrib.get("tests", 0))
                total_failures += int(root.attrib.get("failures", 0))
                total_errors += int(root.attrib.get("errors", 0))
                total_skipped += int(root.attrib.get("skipped", 0))
                
                for testcase in root.findall("testcase"):
                    classname = testcase.attrib.get("classname", "")
                    name = testcase.attrib.get("name", "")
                    
                    # check for failure
                    failure_node = testcase.find("failure")
                    if failure_node is not None:
                        fail_type = failure_node.attrib.get("type", "Failure")
                        fail_msg = failure_node.attrib.get("message", "")
                        stacktrace = failure_node.text or ""
                        failures.append({
                            "class": classname,
                            "test": name,
                            "type": fail_type,
                            "message": fail_msg,
                            "stacktrace": stacktrace.strip()
                        })
                    
                    # check for error
                    error_node = testcase.find("error")
                    if error_node is not None:
                        err_type = error_node.attrib.get("type", "Error")
                        err_msg = error_node.attrib.get("message", "")
                        stacktrace = error_node.text or ""
                        errors.append({
                            "class": classname,
                            "test": name,
                            "type": err_type,
                            "message": err_msg,
                            "stacktrace": stacktrace.strip()
                        })
        except Exception as e:
            print(f"Error parsing {file}: {e}")

# De-duplicate to prevent double counting
unique_failures = []
seen_fails = set()
for f in failures:
    key = (f["class"], f["test"])
    if key not in seen_fails:
        seen_fails.add(key)
        unique_failures.append(f)

unique_errors = []
seen_errs = set()
for e in errors:
    key = (e["class"], e["test"])
    if key not in seen_errs:
        seen_errs.add(key)
        unique_errors.append(e)

with open(output_file, "w", encoding="utf-8") as out:
    out.write("==================================================\n")
    out.write("TEST RUN FAILURE LOG\n")
    out.write("==================================================\n")
    out.write(f"Parsed Suite Counts (from XML files):\n")
    out.write(f"Total Tests Run: {total_run}\n")
    out.write(f"Total Failures: {total_failures} (Unique parsed: {len(unique_failures)})\n")
    out.write(f"Total Errors: {total_errors} (Unique parsed: {len(unique_errors)})\n")
    out.write(f"Total Skipped: {total_skipped}\n\n")
    
    if unique_failures:
        out.write("=== FAILURES ===\n")
        for idx, f in enumerate(unique_failures, 1):
            out.write(f"{idx}. {f['class']}.{f['test']}\n")
            out.write(f"   Type: {f['type']}\n")
            out.write(f"   Message: {f['message']}\n")
            out.write("   Stacktrace:\n")
            lines = f['stacktrace'].split("\n")
            for line in lines[:15]:
                out.write(f"      {line}\n")
            if len(lines) > 15:
                out.write(f"      ... ({len(lines)-15} more lines)\n")
            out.write("\n")
            
    if unique_errors:
        out.write("=== ERRORS ===\n")
        for idx, e in enumerate(unique_errors, 1):
            out.write(f"{idx}. {e['class']}.{e['test']}\n")
            out.write(f"   Type: {e['type']}\n")
            out.write(f"   Message: {e['message']}\n")
            out.write("   Stacktrace:\n")
            lines = e['stacktrace'].split("\n")
            for line in lines[:15]:
                out.write(f"      {line}\n")
            if len(lines) > 15:
                out.write(f"      ... ({len(lines)-15} more lines)\n")
            out.write("\n")

print(f"Successfully generated {output_file}")
print(f"Unique Failures found: {len(unique_failures)}, Unique Errors found: {len(unique_errors)}")
