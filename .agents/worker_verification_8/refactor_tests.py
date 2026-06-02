# -*- coding: utf-8 -*-
import os
import re

files_to_modify = [
    r"backend/ibpms-core/src/test/java/com/ibpms/poc/integration/kanban/KanbanStateTransitionIT.java",
    r"backend/ibpms-core/src/test/java/com/ibpms/poc/infrastructure/camunda/OrphanedTaskCleanupIntegrationTest.java",
    r"backend/ibpms-core/src/test/java/com/ibpms/poc/integration/kanban/TimeTrackingIT.java"
]

base_dir = r"C:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform"

for rel_path in files_to_modify:
    path = os.path.join(base_dir, rel_path)
    if not os.path.exists(path):
        print("WARNING: Path does not exist:", path)
        continue
        
    print("Modifying:", rel_path)
    with open(path, "r", encoding="utf-8", errors="ignore") as f:
        lines = f.readlines()
        
    # 1. Traceability comment on line 1
    trace_comment = "// @Traceability: US-007 - ADR-001\n"
    if lines:
        if lines[0].startswith("// @Traceability:"):
            lines[0] = trace_comment
        else:
            lines.insert(0, trace_comment)
            
    content = "".join(lines)
    
    # 2. Add import
    import_stmt = "import com.ibpms.poc.AbstractIntegrationTest;\n"
    if "import com.ibpms.poc.AbstractIntegrationTest;" not in content and "package com.ibpms.poc;" not in content:
        content = re.sub(r"(package\s+[a-zA-Z0-9.]+;)", r"\1\n\n" + import_stmt, content, count=1)
        
    # 3. Extend class
    match = re.search(r"\bclass\s+(\w+)(?:\s+extends\s+(\w+))?", content)
    if match:
        class_name = match.group(1)
        extends_name = match.group(2)
        if extends_name is None:
            old_decl = "class " + class_name
            new_decl = "class " + class_name + " extends AbstractIntegrationTest"
            content = content.replace(old_decl, new_decl, 1)
            
    with open(path, "w", encoding="utf-8") as f:
        f.write(content)
    print("Successfully modified class:", class_name)
