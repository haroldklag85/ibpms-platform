import os
import re

FRONTEND_DIR = r"C:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform\frontend\src"

def process_vue_files():
    vue_files = []
    for root, dirs, files in os.walk(FRONTEND_DIR):
        if 'views' in root or 'components' in root:
            for f in files:
                if f.endswith('.vue'):
                    vue_files.append(os.path.join(root, f))
    
    modified_count = 0
    for file_path in vue_files:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
            
        if 'apiClient' in content or 'api }' in content or 'api,' in content:
            # Check if it has an import
            if re.search(r'import\s+.*?apiClient.*?;?', content) or re.search(r'import\s+\{\s*api\s*\}.*?;?', content):
                # We need to replace it
                # Remove imports
                content = re.sub(r'import\s+apiClient\s+from\s+[\'"]@/services/apiClient[\'"];?\n?', '', content)
                content = re.sub(r'import\s+\{\s*api\s*\}\s+from\s+[\'"]@/services/apiClient[\'"];?\n?', '', content)
                content = re.sub(r'import\s+\{\s*apiClient\s*\}\s+from\s+[\'"]@/services/apiClient[\'"];?\n?', '', content)
                
                # Add integrationStore import
                import_stmt = "import { useIntegrationStore } from '@/stores/useIntegrationStore';\n"
                if 'import { useIntegrationStore }' not in content:
                    content = re.sub(r'(<script[^>]*>\n)', r'\1' + import_stmt, content)
                
                # Add store initialization
                store_init = "const integrationStore = useIntegrationStore();\n"
                if store_init not in content:
                    # Find a good place to put it, e.g., after the imports
                    # Usually after the last import
                    last_import_match = list(re.finditer(r'import\s+.*?\n', content))
                    if last_import_match:
                        last_import = last_import_match[-1]
                        content = content[:last_import.end()] + "\n" + store_init + content[last_import.end():]
                
                # Replace api calls
                content = re.sub(r'\bapiClient\.', 'integrationStore.', content)
                content = re.sub(r'\bapi\.', 'integrationStore.', content)
                
                # Add Traceability comment if not exists
                trace_comment = "// @Traceability: Retro-Remediación ADR-006\n"
                if trace_comment not in content:
                    content = content.replace(store_init, trace_comment + store_init)
                    
                with open(file_path, 'w', encoding='utf-8') as f:
                    f.write(content)
                modified_count += 1
                print(f"Refactored {file_path}")
                
    print(f"Modified {modified_count} vue files.")

def fix_setinterval():
    # BpmnDesigner.vue
    fpath = os.path.join(FRONTEND_DIR, 'views', 'admin', 'Modeler', 'BpmnDesigner.vue')
    if os.path.exists(fpath):
        with open(fpath, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # Add timeStore import if missing
        if 'useTimeStore' not in content:
            content = re.sub(r'(<script[^>]*>\n)', r"\1import { useTimeStore } from '@/stores/timeStore';\n", content)
            
        if 'const timeStore =' not in content:
            content = re.sub(r'(const integrationStore.*?\n)', r'\1const timeStore = useTimeStore();\n', content)
            
        # Replace setInterval
        # heartbeatInterval = setInterval(async () => { ... }, 30000)
        # autoSaveInterval = setInterval(async () => { ... }, 60000)
        # setInterval(() => { autoSaveAgo.value++; }, 1000);
        
        content = re.sub(r'heartbeatInterval\s*=\s*setInterval\(async\s*\(\)\s*=>\s*\{([\s\S]*?)\},\s*30000\);',
                         r'watch(() => timeStore.currentTick, async (tick) => {\n  if (tick % 30000 < 1000) {\1}\n}); // @Traceability: Retro-Remediación ADR-006', content)
                         
        content = re.sub(r'autoSaveInterval\s*=\s*setInterval\(async\s*\(\)\s*=>\s*\{([\s\S]*?)\},\s*60000\);',
                         r'watch(() => timeStore.currentTick, async (tick) => {\n  if (tick % 60000 < 1000) {\1}\n}); // @Traceability: Retro-Remediación ADR-006', content)
                         
        content = re.sub(r'setInterval\(\(\)\s*=>\s*\{\s*autoSaveAgo\.value\+\+;\s*\},\s*1000\);',
                         r'watch(() => timeStore.currentTick, (tick) => {\n  if (tick % 1000 < 500) { autoSaveAgo.value++; }\n});', content)
        
        with open(fpath, 'w', encoding='utf-8') as f:
            f.write(content)
        print("Refactored BpmnDesigner.vue timers")

    # IntakeTriageView.vue
    fpath = os.path.join(FRONTEND_DIR, 'views', 'IntakeTriageView.vue')
    if os.path.exists(fpath):
        with open(fpath, 'r', encoding='utf-8') as f:
            content = f.read()
            
        if 'useTimeStore' not in content:
            content = re.sub(r'(<script[^>]*>\n)', r"\1import { useTimeStore } from '@/stores/timeStore';\n", content)
            
        if 'const timeStore =' not in content:
            content = re.sub(r'(const [a-zA-Z0-9_]+\s*=\s*.*?\n)', r'\1const timeStore = useTimeStore();\n', content, count=1)
            
        content = re.sub(r'pollingInterval\s*=\s*setInterval\(\(\)\s*=>\s*\{([\s\S]*?)\},\s*10000\);',
                         r'watch(() => timeStore.currentTick, (tick) => {\n  if (tick % 10000 < 1000) {\1}\n}); // @Traceability: Retro-Remediación ADR-006', content)
        content = re.sub(r'let pollingInterval.*?\n', '', content)
        content = re.sub(r'clearInterval\(pollingInterval\);?', '', content)
        
        with open(fpath, 'w', encoding='utf-8') as f:
            f.write(content)
        print("Refactored IntakeTriageView.vue timers")
        
    # useFormStore.ts
    fpath = os.path.join(FRONTEND_DIR, 'stores', 'useFormStore.ts')
    if os.path.exists(fpath):
        with open(fpath, 'r', encoding='utf-8') as f:
            content = f.read()
            
        if 'useTimeStore' not in content:
            content = "import { useTimeStore } from '@/stores/timeStore';\n" + content
            
        if 'const timeStore =' not in content:
            content = re.sub(r'(export const useFormStore = defineStore.*?\n.*?\n)', r'\1    const timeStore = useTimeStore();\n', content)
            
        content = re.sub(r'undoTimer\s*=\s*setInterval\(\(\)\s*=>\s*\{([\s\S]*?)\},\s*5000\);',
                         r'watch(() => timeStore.currentTick, (tick) => {\n  if (tick % 5000 < 1000) {\1}\n}); // @Traceability: Retro-Remediación ADR-006', content)
        content = re.sub(r'let undoTimer.*?\n', '', content)
        content = re.sub(r'clearInterval\(undoTimer\);?', '', content)
        
        with open(fpath, 'w', encoding='utf-8') as f:
            f.write(content)
        print("Refactored useFormStore.ts timers")

if __name__ == "__main__":
    process_vue_files()
    fix_setinterval()
