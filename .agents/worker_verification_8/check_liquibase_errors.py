log_path = r"C:\Users\HaroltAndrésGómezAgu\.gemini\antigravity\brain\6e237bbb-b3f8-4efd-9420-248c3a1d6a6e\.system_generated\tasks\task-737.log"

with open(log_path, 'r', encoding='utf-8', errors='ignore') as f:
    lines = f.readlines()

print("Searching for Liquibase or context load exceptions...")
for i, line in enumerate(lines):
    if ("liquibase" in line.lower() or "exception" in line.lower()) and "error" in line.lower():
        if i < 4116: # focus before the first failure
            print(f"Line {i}: {line.strip()}")
            # Print next 3 lines
            for j in range(i+1, min(i+4, len(lines))):
                print(f"   {lines[j].strip()}")
            print("-" * 40)
