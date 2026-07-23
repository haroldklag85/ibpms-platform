import os
import re

FRONTEND_DIR = r"C:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform\frontend\src"

def fix_css_injections():
    vue_files = []
    for root, dirs, files in os.walk(FRONTEND_DIR):
        for f in files:
            if f.endswith('.vue'):
                vue_files.append(os.path.join(root, f))
                
    modified = 0
    for file_path in vue_files:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
            
        # Check if there is a <style> block
        if '<style' in content:
            style_parts = re.split(r'(<style[^>]*>)', content)
            new_parts = []
            changed = False
            for i, part in enumerate(style_parts):
                if i % 2 == 0 and i > 0: # This is inside a style block (after <style...>)
                    # Wait, re.split with group keeps the delimiter.
                    # parts: [0] = text before <style>, [1] = <style...>, [2] = text after <style> and before next
                    # If we split by <style>, then inside the style block is text until </style>
                    pass
            
            # Better regex approach
            def replacer(match):
                style_content = match.group(0)
                if 'useIntegrationStore();' in style_content:
                    style_content = re.sub(r'// @Traceability: Retro-Remediación ADR-006\n', '', style_content)
                    style_content = re.sub(r'const integrationStore = useIntegrationStore\(\);\n', '', style_content)
                    style_content = re.sub(r'import \{ useIntegrationStore \} from \'@/stores/useIntegrationStore\';\n', '', style_content)
                    return style_content
                return style_content
                
            new_content = re.sub(r'<style[^>]*>[\s\S]*?</style>', replacer, content)
            if new_content != content:
                with open(file_path, 'w', encoding='utf-8') as f:
                    f.write(new_content)
                modified += 1
                print(f"Fixed CSS in {file_path}")

if __name__ == "__main__":
    fix_css_injections()
