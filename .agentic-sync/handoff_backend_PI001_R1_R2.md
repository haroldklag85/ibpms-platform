# 🔐 HANDOFF BACKEND — Iniciativa PI-SHIELD | Iteración PI-001-SHIELD

> **Clasificación:** ⛔ CONFIDENCIAL — USO INTERNO EXCLUSIVO
> **Generado por:** Agente Arquitecto Líder (Orquestador)
> **Fecha de emisión:** 2026-07-24T14:19:00-05:00
> **Autorización del Propietario:** Harold Gómez — Visto bueno emitido 2026-07-24

---

## Pre-Handoff Checklist — PI-001-SHIELD

| # | Verificación | Estado | Evidencia |
|---|---|---|---|
| 1 | Iniciativa autorizada por el Propietario del producto | ✅ | Harold Gómez — visto bueno 2026-07-24 |
| 2 | No requiere Contratos de API (sin endpoints expuestos) | ✅ | Iniciativa forense — operación sobre sistema de archivos |
| 3 | Sin prerrequisitos de cadena de capacidad | ✅ | Iniciativa transversal pre-migración |
| 4 | Rama Git creada o a crear | ✅ | `feature/pi-shield-ip-protection` |

**Resultado:** ✅ APROBADO para ejecución

---

## 1. Metadatos y SSOT

| Parámetro | Valor |
|---|---|
| **Iteración** | `PI-001-SHIELD` |
| **Iniciativa** | `PI-SHIELD — Protección de Propiedad Intelectual IBPMS Platform` |
| **Rol asignado** | Agente Backend (Java / Spring Boot) |
| **Técnicas a implementar** | R1 — Esteganografía Unicode + R2 — Sellado SHA-256 + RFC 3161 |
| **Técnicas EXCLUIDAS** | R3, R4, R5 (fuera del scope de esta iteración) |
| **Rama Git** | `feature/pi-shield-ip-protection` (crear si no existe) |
| **Necesita QA** | NO (validación es forense, no funcional) |
| **SSOT Principal** | `docs/IP_PROTECTION_STRATEGY.md` |
| **SSOT Técnico** | `docs/pi_shield_architect_prompt.md` |
| **Propietario** | Harold Gómez |
| **Flujo de Ejecución** | Backend R1 → Frontend R1 → Backend R2 → Arquitecto Audita |

> ⚠️ **IMPORTANTE:** Todo desarrollo debe quedar exhaustivamente documentado. Se exige **PRECISIÓN QUIRÚRGICA** en cada cambio para evitar efectos colaterales. **PROHIBIDO** modificar la lógica de negocio de ningún archivo al insertar la marca. **PROHIBIDO** alterar la firma de ningún método, constructor o interfaz pública.

---

## 2. Alineación Arquitectónica y ADRs

### ADRs Consultados

| ADR | Impacto en esta Iniciativa |
|---|---|
| **ADR-001** (Hexagonal) | Los archivos Java objetivo pertenecen a `domain/` e `infrastructure/`. La inserción NO debe alterar la separación de capas. |
| **ADR-002** (Vue 3) | No aplica para el Backend — aplica al agente Frontend. |
| **ADR-010** (Pirámide Testing) | Los tests JUnit existentes DEBEN pasar al 100% post-inserción. Si algún test falla, la técnica está mal implementada. |

### Validación del Stack Tecnológico

| Capa | Restricción |
|---|---|
| **Backend** | Java 17 / Spring Boot 3.2.3 — PROHIBIDO introducir dependencias nuevas en `pom.xml` |
| **Herramientas R2** | OpenSSL (CLI del OS), curl (CLI del OS), git (CLI del OS) — todo con utilidades estándar del sistema |

### Trazabilidad de la Solución

**R1 — Esteganografía Unicode:** La técnica opera exclusivamente sobre el contenido de texto de archivos existentes, insertando caracteres Unicode de ancho cero (invisibles) en comentarios y strings de log. No introduce imports, dependencias, ni modifica la lógica de ejecución. El compilador Java trata los caracteres invisibles dentro de strings como parte del string literal, lo que no afecta el comportamiento del sistema. **Compatibilidad total con ADR-001.**

**R2 — Sellado SHA-256 + RFC 3161:** Opera exclusivamente mediante herramientas CLI externas (git, openssl, curl). No modifica ningún archivo de código fuente Java. Los artefactos generados se almacenan en `.pi-shield/` (excluida de Git) y en bóveda offline.

### Riesgos Arquitectónicos Identificados

| Riesgo | Probabilidad | Mitigación |
|---|---|---|
| Un scanner de licencias detecte los caracteres ZW | Muy baja | Los caracteres ZW no son detectados por git diff estándar ni por scanners convencionales |
| Un test de strings compare contenido exacto con hardcode | Baja | Antes del commit, compilar y ejecutar `mvn test` — si falla, revisar posición de inserción |
| El certificado CA de FreeTSA cambie | Baja | Descargarlo fresco justo antes de ejecutar R2: `curl -o .pi-shield/freetsa_cacert.pem https://freetsa.org/files/cacert.pem` |

---

## 📋 DIRECTIVA DE INFRAESTRUCTURA HÍBRIDA

El backend Spring Boot corre en consola local (NO en Docker) en el puerto `8080`. Para la compilación y pruebas de regresión post-inserción:

1. `curl -s http://localhost:8080/actuator/health` → Debe responder `{"status":"UP"}`.
2. Si no responde, arráncalo con: `cd backend && mvn spring-boot:run -pl ibpms-core -Dspring-boot.run.profiles=default`.
3. Verifica los servicios Docker: `docker ps` → PostgreSQL (`5433`), Redis (`6379`) y RabbitMQ (`5672`) deben estar `Up (healthy)`.

**PROHIBIDO** levantar el backend vía Docker o modificar el `docker-compose.yml`.

---

## 3. Rutas Exactas y Contexto Preexistente

### 3.1 Archivos Java Objetivo — TÉCNICA R1 (Esteganografía Unicode)

Los siguientes archivos deben recibir la marca esteganográfica. El agente Backend DEBE verificar su existencia antes de modificarlos:

| Prioridad | Ruta del Archivo | Descripción |
|---|---|---|
| 🔴 Crítica | `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/adapter/CamundaBpmnValidationAdapter.java` | Adaptador BPMN — capa de mayor criticidad arquitectónica |
| 🔴 Crítica | `backend/ibpms-core/src/main/java/com/ibpms/poc/Application.java` | Punto de arranque de la aplicación |
| 🟡 Alta | `backend/ibpms-core/src/main/java/com/ibpms/poc/domain/` | Todos los archivos `.java` en el dominio hexagonal puro (subdirectorios incluidos) |

> **Protocolo de verificación de existencia:** Antes de modificar cualquier archivo, ejecuta `dir` o `ls` sobre las rutas indicadas. Si un archivo no existe en la ruta exacta, reportar en `approval_request_BACKEND_PI001.md` con la ruta alternativa encontrada.

### 3.2 Archivos de Configuración — TÉCNICA R2 (Sellado)

| Archivo | Acción requerida |
|---|---|
| `.gitignore` (raíz del proyecto) | Agregar la línea `.pi-shield/` y la línea `pi_shield_extractor.py` al final del bloque de exclusiones existente |
| `.pi-shield/` (carpeta nueva) | Crear en la raíz del proyecto. **NO commitear.** |

---

## 4. Snippets Prescriptivos

### 4.1 TÉCNICA R1 — Algoritmo de Inserción Esteganográfica

#### Cadena propietaria a codificar
```
HAROLD-GOMEZ-IBPMS-2026
```

#### Mapeo de bits a caracteres Unicode
- Bit `0` → `U+200B` (Zero Width Space)
- Bit `1` → `U+200D` (Zero Width Joiner)

#### Representación binaria de la cadena propietaria
La cadena `HAROLD-GOMEZ-IBPMS-2026` en ASCII/UTF-8 → cada carácter a 8 bits. Ejemplo simplificado:
- `H` = 72 → `01001000`
- `A` = 65 → `01000001`
- `-` = 45 → `00101101`
- ... (continuar para todos los caracteres)

#### Procedimiento de inserción en archivos Java

El agente debe insertar la secuencia de caracteres U+200B/U+200D **en al menos 3 posiciones por archivo**:

**Posición 1 — Al final de la primera línea de un comentario de bloque existente:**
```java
/*
 * [comentario existente]​‍‍​​‍‍​​‍​‍​‍​‍‍​‍​‍‍​‍​​‍​‍‍​‍​‍‍​‍​​‍​‍‍​‍​‍‍​‍​​‍​‍
 * [resto del comentario]
 */
```
> Los caracteres entre `]` y el salto de línea son la secuencia propietaria. Son invisibles en cualquier editor.

**Posición 2 — Dentro de un string de log existente (no funcional):**
```java
// Ejemplo: si existe log.debug("Validating BPMN process...");
log.debug("Validating BPMN​‍‍​​‍‍​​‍​‍​‍​‍‍​‍​‍‍​‍​​‍​‍‍​‍​‍‍​‍​​‍​‍‍​‍​‍‍​‍​​‍​‍ process...");
```

**Posición 3 — Al final de una línea de comentario de línea existente:**
```java
// Camunda BPMN validation adapter — Hexagonal Driven Port​‍‍​​‍‍​​‍​‍​‍​‍‍​‍​‍‍​‍​​‍
```

> ⚠️ **REGLA CRÍTICA:** Los caracteres esteganográficos DEBEN insertarse en posiciones que NO sean analizadas por el compilador como tokens Java. Los comentarios y el interior de string literals son las únicas posiciones seguras. **NUNCA** insertar entre tokens Java (keywords, operadores, identificadores).

#### Script de generación de la secuencia

El agente debe implementar el script extractor `pi_shield_extractor.py` con el siguiente algoritmo:

```python
#!/usr/bin/env python3
"""
PI-SHIELD Extractor v1.0
CLASIFICACIÓN: ⛔ CONFIDENCIAL — NO COMMITEAR A NINGÚN REPOSITORIO GIT
Propietario: Harold Gómez — IBPMS Platform 2026
"""
import sys

ZWS = '\u200b'  # bit 0 — Zero Width Space
ZWJ = '\u200d'  # bit 1 — Zero Width Joiner

def encode_watermark(text: str) -> str:
    """Codifica texto a secuencia de caracteres ZW para inserción."""
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

def main():
    if len(sys.argv) < 2:
        print("Uso: python pi_shield_extractor.py <ruta_archivo>")
        sys.exit(1)
    
    filepath = sys.argv[1]
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
    except (FileNotFoundError, IOError) as e:
        print(f"Error al leer el archivo: {e}")
        sys.exit(1)
    
    watermark = decode_watermark(content)
    
    if watermark:
        print(f"✅ MARCA DE AUTORÍA DETECTADA: {watermark}")
    else:
        print("❌ No se detectó marca de autoría en este archivo.")

if __name__ == '__main__':
    main()
```

> 🔒 **RESTRICCIÓN ABSOLUTA:** Este script **NO debe commitearse** en ningún repositorio Git. Entregarlo físicamente a Harold Gómez en formato de archivo local (impreso o USB cifrado offline). Documentar explícitamente en `PI_SHIELD_LEGAL_SUMMARY.md` que el extractor fue entregado al propietario.

---

### 4.2 TÉCNICA R2 — Sellado SHA-256 + RFC 3161 (Paso a Paso)

Ejecutar los siguientes pasos **EN ORDEN ESTRICTO** y **SOLO DESPUÉS** de que los cambios de R1 ya estén commiteados:

```powershell
# === PRERREQUISITO: R1 ya commiteado ===
git status  # DEBE retornar "nothing to commit, working tree clean"

# === PASO 0: Crear directorio de artefactos ===
mkdir .pi-shield

# === PASO 1: Capturar el hash del commit HEAD actual ===
git rev-parse HEAD | Out-File -FilePath .pi-shield\COMMIT_REF_PRE_MIGRATION.txt -Encoding utf8

# === PASO 2: Generar hash SHA-256 del árbol completo del repositorio ===
git archive --format=tar HEAD | certutil -hashfile - SHA256 | Out-File -FilePath .pi-shield\REPO_HASH_PRE_MIGRATION.txt -Encoding utf8
# Alternativa si certutil no está disponible:
# git archive --format=tar HEAD > .pi-shield\repo_snapshot.tar
# (sha256sum .pi-shield\repo_snapshot.tar) | Out-File -FilePath .pi-shield\REPO_HASH_PRE_MIGRATION.txt

# === PASO 3: Descargar certificado CA de FreeTSA ===
curl -o .pi-shield\freetsa_cacert.pem https://freetsa.org/files/cacert.pem

# === PASO 4: Crear el hash file para OpenSSL (requiere OpenSSL instalado) ===
# Crear un archivo de texto con el contenido del hash para la consulta TSA
$hashContent = Get-Content .pi-shield\REPO_HASH_PRE_MIGRATION.txt
$hashContent | Out-File -FilePath .pi-shield\hash_content.txt -Encoding utf8

# === PASO 5: Generar la consulta TSA (TSQ) ===
openssl ts -query -data .pi-shield\REPO_HASH_PRE_MIGRATION.txt -sha256 -cert -out .pi-shield\timestamp_request.tsq

# === PASO 6: Enviar a FreeTSA (TSA gratuita — valor referencial) ===
curl -H "Content-Type: application/timestamp-query" `
  --data-binary "@.pi-shield\timestamp_request.tsq" `
  https://freetsa.org/tsr -o .pi-shield\timestamp_response.tsr

# === PASO 7: Verificar el token recibido ===
openssl ts -verify `
  -in .pi-shield\timestamp_response.tsr `
  -queryfile .pi-shield\timestamp_request.tsq `
  -CAfile .pi-shield\freetsa_cacert.pem

# === PASO 8: Generar reporte legible ===
openssl ts -reply -in .pi-shield\timestamp_response.tsr -text `
  | Out-File -FilePath .pi-shield\TIMESTAMP_REPORT.txt -Encoding utf8
```

> ⚠️ **NOTA PARA WINDOWS:** Los comandos anteriores están adaptados para PowerShell. Si OpenSSL no está disponible en el PATH, verificar su instalación con `openssl version`. Si no está instalado, documentarlo en `approval_request_BACKEND_PI001.md` como bloqueador para que el Arquitecto decida la alternativa.

---

### 4.3 Artefacto Requerido — PI_SHIELD_LEGAL_SUMMARY.md

El agente DEBE crear el archivo `.pi-shield\PI_SHIELD_LEGAL_SUMMARY.md` con el siguiente contenido **adaptado a los valores reales** obtenidos durante la ejecución:

```markdown
# 📜 PI-SHIELD — Resumen Legal del Sellado Criptográfico

**Clasificación:** ⛔ CONFIDENCIAL — DOCUMENTO LEGAL
**Titular:** Harold Gómez
**IBPMS Platform — Protección de Propiedad Intelectual**

---

## Datos del Sellado Temporal

| Campo | Valor |
|---|---|
| **Fecha y hora del sellado (UTC)** | [COMPLETAR con timestamp del token .tsr] |
| **Fecha y hora del sellado (UTC-5 Colombia)** | [COMPLETAR] |
| **Hash SHA-256 del commit sellado** | [COMPLETAR con contenido de COMMIT_REF_PRE_MIGRATION.txt] |
| **Hash SHA-256 del árbol del repositorio** | [COMPLETAR con contenido de REPO_HASH_PRE_MIGRATION.txt] |
| **TSA utilizada** | FreeTSA.org (Gratuita / Referencial) |
| **Token RFC 3161** | `.pi-shield/timestamp_response.tsr` |

## Declaración de Titularidad

El código fuente del repositorio IBPMS Platform, en el estado exacto descrito por el hash SHA-256 
anterior, es propiedad intelectual exclusiva de:

**Harold Gómez**
(Documento de identidad del titular)

Este sellado demuestra que el código existía en su forma actual **antes** de cualquier migración 
al repositorio de empresa aliada alguna, conforme a la Ley 23 de 1982 (Colombia), la Decisión 
Andina 351 de 1993 y el Convenio de Berna para la Protección de Obras Literarias y Artísticas.

## Instrucciones de Custodia

1. Conservar este documento junto con el token `timestamp_response.tsr` en **bóveda offline** 
   (USB cifrado o caja fuerte física).
2. El script extractor `pi_shield_extractor.py` también debe conservarse en la misma bóveda offline.
3. En caso de disputa de propiedad intelectual, presentar estos artefactos ante un **perito 
   informático forense** quien puede verificar:
   - Que el token .tsr es auténtico (verificación OpenSSL con CA de FreeTSA)
   - Que el hash del repositorio sellado coincide con el código presentado
   - Que los archivos Java/Vue marcados contienen la cadena `HAROLD-GOMEZ-IBPMS-2026`

## Estado de los Artefactos

| Artefacto | Ubicación | ¿En Git? |
|---|---|---|
| `COMMIT_REF_PRE_MIGRATION.txt` | `.pi-shield/` | ❌ NO — gitignore |
| `REPO_HASH_PRE_MIGRATION.txt` | `.pi-shield/` | ❌ NO — gitignore |
| `timestamp_request.tsq` | `.pi-shield/` | ❌ NO — gitignore |
| `timestamp_response.tsr` | `.pi-shield/` | ❌ NO — gitignore |
| `TIMESTAMP_REPORT.txt` | `.pi-shield/` | ❌ NO — gitignore |
| `freetsa_cacert.pem` | `.pi-shield/` | ❌ NO — gitignore |
| `pi_shield_extractor.py` | Bóveda offline (NO en repo) | ❌ NO — bóveda |

---
*Generado durante la ejecución de la iniciativa PI-001-SHIELD — 2026-07-24*
```

---

## 5. Matriz de QA y Testing Atómico

### 5.1 Criterios de Aceptación Asignados al Backend

| CA | Descripción | Criterio de Verificación |
|---|---|---|
| **CA-PI-01** | Inserción Esteganográfica Backend (R1) | Script extractor retorna `HAROLD-GOMEZ-IBPMS-2026` en cada archivo Java marcado |
| **CA-PI-04** | Sellado Temporal RFC 3161 (R2) | Verificación OpenSSL retorna `Verification: OK` |
| **CA-PI-05** | Artefactos NO en Git | `git status` y `git log --all -- .pi-shield/` no muestran resultados |

### 5.2 Verificación Anti-Regresión Obligatoria

ANTES del commit final de R1, ejecutar la pirámide de tests existente:

```powershell
# Compilación obligatoria (Protocolo Zero-Trust SRE)
cd backend
mvn clean compile -pl ibpms-core

# Tests unitarios
mvn test -pl ibpms-core

# Si AMBOS comandos retornan BUILD SUCCESS → la inserción es correcta
# Si ALGUNO falla → la inserción tiene un error posicional — NO commitear
```

> **Criterio de rechazo inmediato:** Si `mvn test` falla después de la inserción, la técnica está mal implementada. Revisar que los caracteres ZW solo están dentro de comentarios o strings, nunca entre tokens Java.

### 5.3 Verificación de Invisibilidad (CA-PI-03 — Parcial Backend)

```powershell
# Verificar que grep convencional no detecta nada sospechoso
git diff HEAD --word-diff
# El diff NO debe mostrar caracteres nuevos visibles

# Verificar que el script extractor SÍ encuentra la marca
python pi_shield_extractor.py "backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/adapter/CamundaBpmnValidationAdapter.java"
# Debe retornar: ✅ MARCA DE AUTORÍA DETECTADA: HAROLD-GOMEZ-IBPMS-2026
```

---

## 6. Mensajes de Commit Permitidos (CAMUFLAJE OBLIGATORIO)

> ⛔ **PROHIBICIÓN ABSOLUTA:** Los mensajes de commit NO deben mencionar "watermark", "steganography", "PI protection", "marca", "propiedad intelectual" o cualquier término que revele la intención.

| Commit | Mensaje PERMITIDO |
|---|---|
| Inserción R1 en archivos Java | `refactor: normalize unicode handling in core domain logging` |
| Actualización `.gitignore` | `chore: update gitignore for local tooling artifacts` |
| (Los artefactos .pi-shield NO se commitean) | N/A |

---

## 🛑 PROHIBICIONES ABSOLUTAS — VIOLACIÓN = RECHAZO INMEDIATO

1. **PROHIBIDO** commitear el script extractor `pi_shield_extractor.py` a ningún repositorio Git
2. **PROHIBIDO** commitear la carpeta `.pi-shield/` a ningún repositorio Git
3. **PROHIBIDO** introducir dependencias nuevas en `pom.xml` para implementar esta técnica
4. **PROHIBIDO** modificar la firma de ningún método, constructor o interfaz pública
5. **PROHIBIDO** modificar la lógica de negocio de ningún archivo
6. **PROHIBIDO** mencionar en mensajes de commit o PR qué técnica se aplica
7. **PROHIBIDO** asumir que el backend está corriendo sin ejecutar el health check previo

---

## 📋 Compilación Obligatoria

**Compilación obligatoria:** Ejecuta el protocolo Zero-Trust SRE documentado en `.agents/skills/backend_sre_compilation_audit/SKILL.md` (§0 a §2). Si hay bloqueos de infraestructura, aplica el protocolo de reporte 3B.

---

## INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN

1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_BACKEND_PI001.md`.
4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_BACKEND_PI001.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION` y programa la solución.
6. **ANTES del commit final**, actualiza `docs/sprints/gobernanza_pm/CHANGELOG_NO_TECNICO.md` explicando tu trabajo en lenguaje sencillo y amigable para un CEO. Ejemplo de redacción: *"Se realizaron mejoras de normalización de texto en los archivos principales del motor de procesos para garantizar consistencia en el manejo de caracteres especiales."* — NO mencionar esteganografía ni PI.
7. **Orden de commits:**
   - Commit 1: Inserción R1 en archivos Java → Push → Notifica al Arquitecto
   - Commit 2: Actualización `.gitignore` → Push
   - R2: Ejecutar sellado DESPUÉS del push de R1, artefactos en bóveda offline (NO en Git)
8. Finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama `feature/pi-shield-ip-protection`. Queda estrictamente prohibido usar git stash.

> 📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
> - Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md`.
> - Aplica el skill de búsqueda `.agents/skills/grep_search_governance/SKILL.md` para localizar archivos objetivo.

---

## Secuencia de Entrega y Dependencias

```
PASO 1 (Este agente): Inserción R1 en archivos .java → commit neutro → push
    ↓
PASO 2 (Agente Frontend): Inserción R1 en archivos .vue/.ts → commit neutro → push
    ↓  
PASO 3 (Este agente): Ejecuta R2 (sellado SHA-256 + RFC 3161) → artefactos en .pi-shield/ (NO en Git)
    ↓
PASO 4 (Arquitecto Líder): Audita CA-PI-01 a CA-PI-05
    ↓
PASO 5 (Harold Gómez): Extrae .pi-shield/ a bóveda offline personal
```

> **Nota:** El agente Backend debe notificar al Arquitecto cuando R1 esté commiteado para que el agente Frontend pueda comenzar. Después de que el agente Frontend termine, el Backend retoma para ejecutar R2.

---

*Handoff generado por: Agente Arquitecto Líder — Iniciativa PI-001-SHIELD*
*Fecha: 2026-07-24 | Rama: feature/pi-shield-ip-protection | Clasificación: ⛔ CONFIDENCIAL*
