import re
import os

req_file = r'C:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform\docs\requirements\v1_user_stories.md'
wire_file = r'C:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform\docs\architecture\v1_wireframes.md'

with open(req_file, 'r', encoding='utf-8') as f:
    req_content = f.read()

with open(wire_file, 'r', encoding='utf-8') as f:
    wire_content = f.read()

# Parse Pantallas from wireframes
pantallas = []
pantalla_matches = re.finditer(r'## (Pantalla [0-9A-Z.]+): *(.*)', wire_content)
for m in pantalla_matches:
    p_id = m.group(1).strip()
    p_name = m.group(2).strip()
    pantallas.append({"id": p_id, "name": p_name})

# Parse User Stories
us_blocks = re.split(r'### (US-\d+:.*)', req_content)[1:] # Split by US header, drop everything before first US

stories = []
for i in range(0, len(us_blocks), 2):
    header = us_blocks[i].strip()
    body = us_blocks[i+1]
    
    us_match = re.match(r'(US-\d+):\s*(.*)', header)
    us_id = us_match.group(1).strip()
    us_title = us_match.group(2).strip()
    
    # Count scenarios
    ac_count = len(re.findall(r'^\s*Scenario:', body, re.MULTILINE))
    
    # Extract trace
    trace_match = re.search(r'\*\*Trazabilidad UX:\*\*(.*)', body)
    trace_raw = trace_match.group(1).strip() if trace_match else "N/A"
    
    # Find mentioned screens
    mentioned = []
    for p in pantallas:
        if p["id"] in trace_raw:
             mentioned.append(p["id"])
    
    stories.append({
        "id": us_id,
        "title": us_title,
        "ac_count": ac_count,
        "trace_raw": trace_raw,
        "pantallas_mapped": mentioned
    })

# Analytics
us_no_pantallas = [s for s in stories if not s["pantallas_mapped"] and s["trace_raw"] != "N/A"]
us_no_ac = [s for s in stories if s["ac_count"] == 0]

all_mapped_pantallas = set()
for s in stories:
    for p in s["pantallas_mapped"]:
        all_mapped_pantallas.add(p)

pantallas_huerfanas = [p for p in pantallas if p["id"] not in all_mapped_pantallas]

# Generate Markdown
md = ["# Reporte Analítico: User Stories vs Wireframes (Gap Analysis)\n"]

md.append("## 1. Mapeo General (US -> Pantallas)\n")
md.append("| Historia de Usuario | Título | Criterios Gherkin | Pantallas Asociadas |")
md.append("|---|---|---|---|")
for s in stories:
    mapped = ", ".join(s["pantallas_mapped"]) if s["pantallas_mapped"] else ("Pendiente" if s["trace_raw"] == "N/A" else f"Desconexión ({s['trace_raw']})")
    md.append(f"| {s['id']} | {s['title']} | {s['ac_count']} Escenarios | {mapped} |")

md.append("\n## 2. Gaps Funcionales (Desconexiones Detectadas)\n")

md.append("### 2.1 Historias sin Criterios de Aceptación (Gaps de Detalle)")
if us_no_ac:
    for s in us_no_ac:
        md.append(f"- 🔴 **{s['id']}**: {s['title']}")
else:
    md.append("- ✅ *Todas las historias poseen al menos 1 escenario Gherkin formalizado.*")

md.append("\n### 2.2 Historias sin Trazabilidad UX (Gaps Visuales)")
us_no_trace = [s for s in stories if s["trace_raw"] == "N/A" or not s["pantallas_mapped"]]
if us_no_trace:
    for s in us_no_trace:
        md.append(f"- 🔴 **{s['id']}**: {s['title']} -> *Trazabilidad declarada:* {s['trace_raw']}")
else:
    md.append("- ✅ *Todas las historias mapean a una pantalla existente.*")

md.append("\n### 2.3 Pantallas Huérfanas (Gaps de Requerimiento)")
md.append("> *Pantallas definidas en el diseño que NO están siendo respaldadas por ninguna Historia de Usuario explícita.*")
if pantallas_huerfanas:
    for p in pantallas_huerfanas:
        md.append(f"- ⚠️ **{p['id']}**: {p['name']}")
else:
    md.append("- ✅ *No hay pantallas huérfanas.*")

out_file = r'C:\Users\HaroltAndrésGómezAgu\.gemini\antigravity\brain\8a367ae0-4ee5-44e6-8544-4e2e60588a84\us_vs_wireframes_gap_report.md'
with open(out_file, 'w', encoding='utf-8') as f:
    f.write("\n".join(md))

print(f"Report Generated: {out_file}")
