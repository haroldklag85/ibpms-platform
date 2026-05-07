import os
import json
import math

# Configuración
ROOT_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..'))
DIRECTORIOS_A_ESCANEAR = [
    os.path.join(ROOT_DIR, 'backend', 'ibpms-core', 'src'),
    os.path.join(ROOT_DIR, 'frontend', 'src'),
    os.path.join(ROOT_DIR, 'frontend', 'e2e')
]
EXCLUSIONES = ['node_modules', 'target', 'dist', '.git', '.agentic-sync', '.agent', 'assets', 'public']
EXTENSIONES_VALIDAS = ['.java', '.vue', '.ts', '.js', '.css']
TAMAÑO_LOTE = 5
OUTPUT_DIR = os.path.join(ROOT_DIR, '.agent', 'queue')

def is_valid_file(filepath):
    for exc in EXCLUSIONES:
        if exc in filepath:
            return False
    _, ext = os.path.splitext(filepath)
    return ext in EXTENSIONES_VALIDAS

def discover_files():
    all_files = []
    for dir_to_scan in DIRECTORIOS_A_ESCANEAR:
        if not os.path.exists(dir_to_scan):
            continue
        for root, dirs, files in os.walk(dir_to_scan):
            dirs[:] = [d for d in dirs if d not in EXCLUSIONES]
            for file in files:
                filepath = os.path.join(root, file)
                if is_valid_file(filepath):
                    # Guardar ruta relativa al workspace para que sea portable
                    all_files.append(os.path.relpath(filepath, ROOT_DIR))
    
    # Ordenar para mantener consistencia arquitectónica (Backend primero, luego Front)
    return sorted(all_files)

def generate_batches():
    files = discover_files()
    total_files = len(files)
    total_batches = math.ceil(total_files / TAMAÑO_LOTE)
    
    if not os.path.exists(OUTPUT_DIR):
        os.makedirs(OUTPUT_DIR)
        
    print(f"[*] Encontrados {total_files} archivos a auditar.")
    print(f"[*] Generando {total_batches} lotes de {TAMAÑO_LOTE} archivos...")

    progress_lines = ["# Registro de Progreso de Lotes de Auditoría\n\n"]
    
    for i in range(total_batches):
        start_idx = i * TAMAÑO_LOTE
        batch_files = files[start_idx:start_idx + TAMAÑO_LOTE]
        lote_id = f"lote_{str(i+1).zfill(3)}"
        
        lote_data = {
            "lote_id": lote_id,
            "status": "PENDING",
            "archivos": batch_files
        }
        
        with open(os.path.join(OUTPUT_DIR, f"{lote_id}.json"), 'w', encoding='utf-8') as f:
            json.dump(lote_data, f, indent=4, ensure_ascii=False)
            
        progress_lines.append(f"- [ ] **{lote_id}**: {len(batch_files)} archivos pendientes.\n")

    with open(os.path.join(OUTPUT_DIR, "progreso_lotes.md"), 'w', encoding='utf-8') as f:
        f.writelines(progress_lines)

    print(f"[+] Lotes generados exitosamente en: .agent/queue/")

if __name__ == "__main__":
    generate_batches()
