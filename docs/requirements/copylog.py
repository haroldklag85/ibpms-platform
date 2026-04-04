import os
import codecs

# We are dynamically constructing this so we don't have to put accented characters in string literals
users_dir = "C:\\Users"
user_target = None
for d in os.listdir(users_dir):
    if d.startswith("Harolt"):
        user_target = os.path.join(users_dir, d)
        break

if user_target:
    src = os.path.join(user_target, ".gemini", "antigravity", "brain", "8a367ae0-4ee5-44e6-8544-4e2e60588a84", ".system_generated", "logs", "overview.txt")
    dest = os.path.join(user_target, "ProyectoAntigravity", "ibpms-platform", "docs", "requirements", "TRANSCRIPCION_COMPLETA_AGENTE.md")
    
    if os.path.exists(src):
        with codecs.open(src, "r", "utf-8") as fin:
            content = fin.read()
        
        with codecs.open(dest, "w", "utf-8") as fout:
            fout.write("# TRANSCRIPCIÓN DEL AGENTE\n\n```text\n")
            fout.write(content)
            fout.write("\n```\n")
        print("COPIA EXITOSA A:", dest)
    else:
        print("Log source not found:", src)
else:
    print("User directory not found")
