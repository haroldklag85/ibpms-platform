import re
import os

def resolve_changelog():
    path = 'docs/sprints/gobernanza_pm/CHANGELOG_NO_TECNICO.md'
    with open(path, 'r', encoding='latin-1') as f:
        content = f.read()
    
    pattern = re.compile(r'<<<<<<< HEAD\n(.*?)=======\n(.*?)>>>>>>> [a-f0-9]+', re.DOTALL)
    match = pattern.search(content)
    if match:
        head_content = match.group(1)
        dev_david_content = match.group(2)
        resolved = head_content.strip() + '\n\n' + dev_david_content.strip() + '\n'
        new_content = content[:match.start()] + resolved + content[match.end():]
        with open(path, 'w', encoding='latin-1') as f:
            f.write(new_content)
        print("CHANGELOG_NO_TECNICO.md resolved.")

def resolve_api_contracts():
    path = 'docs/sprints/gobernanza_pm/API_CONTRACTS.md'
    with open(path, 'r', encoding='latin-1') as f:
        content = f.read()
    
    pattern = re.compile(r'<<<<<<< HEAD\n(.*?)=======\n(.*?)>>>>>>> [a-f0-9]+', re.DOTALL)
    match = pattern.search(content)
    if match:
        head_content = match.group(1)
        dev_david_content = match.group(2)
        
        resolved = dev_david_content.strip() + "\n\n<!-- Exclusive from main -->\n" + head_content.strip() + '\n'
        new_content = content[:match.start()] + resolved + content[match.end():]
        with open(path, 'w', encoding='latin-1') as f:
            f.write(new_content)
        print("API_CONTRACTS.md resolved.")

resolve_changelog()
resolve_api_contracts()
