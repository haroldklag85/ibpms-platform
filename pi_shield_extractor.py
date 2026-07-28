#!/usr/bin/env python3
"""
PI-SHIELD Extractor v1.0
CLASIFICACION: CONFIDENCIAL - NO COMMITEAR A NINGUN REPOSITORIO GIT
Propietario: Harold Gomez - IBPMS Platform 2026
"""
import sys
import os

ZWS = '\u200b'  # bit 0 - Zero Width Space
ZWJ = '\u200d'  # bit 1 - Zero Width Joiner

def encode_watermark(text: str) -> str:
    """Codifica texto a secuencia de caracteres ZW para insercion."""
    bits = ''.join(format(ord(c), '08b') for c in text)
    return ''.join(ZWJ if b == '1' else ZWS for b in bits)

def decode_watermark(content: str) -> str:
    """Extrae y decodifica la marca de agua de un archivo fuente."""
    zw_chars = [c for c in content if c in (ZWS, ZWJ)]
    if not zw_chars:
        return ""
    bits = ''.join('1' if c == ZWJ else '0' for c in zw_chars)
    # Agrupar en bloques de 8 bits y convertir a ASCII
    chars = []
    for i in range(0, len(bits) - len(bits) % 8, 8):
        byte = bits[i:i+8]
        code = int(byte, 2)
        if 32 <= code <= 126:  # Caracteres ASCII imprimibles
            chars.append(chr(code))
    return ''.join(chars)

def verify_file(filepath: str):
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
    except (FileNotFoundError, IOError) as e:
        print(f"[ERROR] No se pudo leer: {filepath} - {e}")
        return False
    
    watermark = decode_watermark(content)
    basename = os.path.basename(filepath)
    
    if watermark:
        print(f"[OK] {basename}: MARCA DETECTADA: {watermark}")
        return True
    else:
        print(f"[MISS] {basename}: No se detecto marca")
        return False

def main():
    if len(sys.argv) >= 2:
        # Modo extractor individual
        filepath = sys.argv[1]
        try:
            with open(filepath, 'r', encoding='utf-8') as f:
                content = f.read()
        except (FileNotFoundError, IOError) as e:
            print(f"Error al leer el archivo: {e}")
            sys.exit(1)
        
        watermark = decode_watermark(content)
        if watermark:
            print(f"MARCA DE AUTORIA DETECTADA: {watermark}")
        else:
            print("No se detecto marca de autoria en este archivo.")
    else:
        # Modo verificacion masiva
        base = r"c:\Users\USER\Desktop\Proyectos\Harold Ibpms\ibpms-platform\backend\ibpms-core\src\main\java\com\ibpms\poc"
        
        critical = [
            os.path.join(base, "infrastructure", "adapter", "CamundaBpmnValidationAdapter.java"),
            os.path.join(base, "Application.java"),
        ]
        
        print("=" * 60)
        print("PI-SHIELD R1 - Verificacion de Marcas")
        print("=" * 60)
        
        ok = 0
        miss = 0
        for f in critical:
            if verify_file(f):
                ok += 1
            else:
                miss += 1
        
        # Verificar sample del dominio
        domain_samples = []
        for root, dirs, files in os.walk(os.path.join(base, "domain")):
            dirs[:] = [d for d in dirs if d not in ('target',)]
            for f in files:
                if f.endswith('.java'):
                    domain_samples.append(os.path.join(root, f))
        
        for f in domain_samples:
            if verify_file(f):
                ok += 1
            else:
                miss += 1
        
        print(f"\nRESUMEN: {ok} archivos verificados con marca, {miss} sin marca (pueden ser archivos sin posicion valida)")
        print("=" * 60)

if __name__ == '__main__':
    main()
