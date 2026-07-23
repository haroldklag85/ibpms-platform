import os
import time

reports_dir = r"C:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform\backend\ibpms-core\target\surefire-reports"

if not os.path.exists(reports_dir):
    print("Reports dir does not exist")
    exit(0)

files = []
for file in os.listdir(reports_dir):
    if file.endswith(".xml") and file.startswith("TEST-"):
        filepath = os.path.join(reports_dir, file)
        mtime = os.path.getmtime(filepath)
        files.append((mtime, file))

files.sort(reverse=True)
print("TOP 30 NEWEST REPORT FILES:")
for mtime, name in files[:30]:
    print(f"{time.strftime('%Y-%m-%d %H:%M:%S', time.localtime(mtime))} - {name}")
