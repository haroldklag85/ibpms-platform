log_path = r"C:\Users\HaroltAndrésGómezAgu\.gemini\antigravity\brain\6e237bbb-b3f8-4efd-9420-248c3a1d6a6e\.system_generated\tasks\task-1161.log"

with open(log_path, 'r', encoding='utf-8', errors='ignore') as f:
    lines = f.readlines()

print("Execution order of tests:")
for i, line in enumerate(lines):
    if "Running com.ibpms.poc" in line:
        print(f"Line {i}: {line.strip()}")
