import re

log_path = r"C:\Users\HaroltAndrésGómezAgu\.gemini\antigravity\brain\6e237bbb-b3f8-4efd-9420-248c3a1d6a6e\.system_generated\tasks\task-1161.log"
output_path = r"C:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform\.agents\worker_verification_8\failures_summary.txt"

with open(log_path, 'r', encoding='utf-8', errors='ignore') as f:
    lines = f.readlines()

out_lines = []
out_lines.append(f"Total lines in log: {len(lines)}\n")

failures = []
for i, line in enumerate(lines):
    if "<<< FAILURE!" in line or "<<< ERROR!" in line:
        failures.append((i, line))

out_lines.append(f"Found {len(failures)} failure/error marks:\n")
for idx, line in failures:
    out_lines.append(f"\n========================================\n")
    out_lines.append(f"Line {idx}: {line.strip()}\n")
    out_lines.append(f"========================================\n")
    # Print 10 lines before and 40 lines after to get the stack trace/cause
    start = max(0, idx - 10)
    end = min(len(lines), idx + 40)
    for j in range(start, end):
        prefix = "-> " if j == idx else "   "
        out_lines.append(f"{prefix}{j}: {lines[j].strip()}\n")

with open(output_path, 'w', encoding='utf-8') as f_out:
    f_out.writelines(out_lines)

print("failures_summary.txt written successfully.")
