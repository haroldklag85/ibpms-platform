log_path = r"C:\Users\HaroltAndrésGómezAgu\.gemini\antigravity\brain\6e237bbb-b3f8-4efd-9420-248c3a1d6a6e\.system_generated\tasks\task-737.log"

with open(log_path, 'r', encoding='utf-8', errors='ignore') as f:
    lines = f.readlines()

print("Printing Spring Boot startup and Liquibase logs from the beginning...")
count = 0
for i, line in enumerate(lines):
    if "liquibase" in line.lower() or "starting" in line.lower() or "active profile" in line.lower() or "datasource" in line.lower():
        print(f"Line {i}: {line.strip()}")
        count += 1
        if count > 50:
            break
