# 🔐 PROMPT — Arquitecto Líder: Orquestación PI-001 (Protección de Propiedad Intelectual)

> **Documento de activación para el Agente Arquitecto Líder**  
> **Clasificación:** CONFIDENCIAL — USO INTERNO  
> **Fecha:** 2026-07-24  
> **Iniciativa:** PI-SHIELD — Blindaje de Propiedad Intelectual IBPMS Platform  
> **Autorización:** Harold Gómez (Gerente / Propietario del producto) — Visto bueno emitido 2026-07-24

---

## INSTRUCCIÓN DIRECTA AL ARQUITECTO LÍDER

```
Actúas EXCLUSIVAMENTE como Agente Arquitecto Líder (Orquestador) dentro del 
proyecto ibpms-platform.

Tu misión en esta sesión es coordinar la implementación técnica de la iniciativa 
PI-SHIELD (Protección de Propiedad Intelectual), que comprende DOS técnicas 
aprobadas por el propietario del producto Harold Gómez:

  - TÉCNICA R1: Marca de Agua Esteganográfica Unicode (Whitespace Steganography)
  - TÉCNICA R2: Sellado Criptográfico SHA-256 + Token RFC 3161 (Timestamp Authority)

ANTES de cualquier acción, ejecuta el protocolo completo del workflow 
`.agent/workflows/cierreDeudaTecCriteriosAceptacion.md` desde la Fase 0.PRE 
hasta la Fase 2, sustituyendo el concepto de "Historia de Usuario" por 
"Iniciativa PI-SHIELD" en todos tus artefactos.

Lee también obligatoriamente el documento estratégico confidencial en:
  docs/IP_PROTECTION_STRATEGY.md

Este documento contiene la descripción técnica completa de ambas técnicas 
aprobadas y es tu SSOT (Single Source of Truth) para esta iniciativa.
```

---

## PARÁMETROS DE ORQUESTACIÓN

| Parámetro | Valor |
|---|---|
| **Iteración** | `PI-001-SHIELD` |
| **Iniciativa** | `PI-SHIELD` (No es US estándar — es iniciativa de PI) |
| **Técnicas a implementar** | `R1 — Esteganografía Unicode`, `R2 — Hash SHA-256 + RFC 3161` |
| **Rama Git de trabajo** | `feature/pi-shield-ip-protection` (crear si no existe) |
| **Exclusiones** | R3, R4, R5 (aprobadas pero fuera del scope de esta iteración) |
| **Necesita QA** | `no` (esta iteración no expone endpoints ni UI — la validación es forense) |
| **NFR Strategy** | Los artefactos generados (hash, token RFC 3161, script extractor) deben almacenarse en bóveda offline, NO en el repositorio Git de la alianza |

---

## CONTEXTO TÉCNICO DETALLADO — PARA CONSTRUIR LOS HANDOFFS

### TÉCNICA R1 — Esteganografía Unicode (Whitespace Steganography)

**¿Qué es?**  
Inserción de caracteres Unicode de ancho cero (invisibles) dentro de archivos 
de código fuente críticos, codificando la cadena de propiedad `"HAROLD-GOMEZ-IBPMS-2026"` 
en binario mediante el mapeo:
- Bit `0` → Carácter `U+200B` (Zero Width Space)
- Bit `1` → Carácter `U+200D` (Zero Width Joiner)

**Propiedades obligatorias del resultado:**
- Los caracteres NO son visibles en ningún editor (VS Code, IntelliJ, GitHub diff, GitHub web)
- El código debe compilar y ejecutarse SIN NINGUNA DIFERENCIA funcional
- NO deben aparecer en salida de `grep`, `cat`, ni escáneres de licencias convencionales
- El mensaje de autoría solo es recuperable con un script extractor privado

**Archivos objetivo PRIORIZADOS para esta iniciativa:**  
Los archivos objetivo se clasifican por criticidad de arquitectura. El agente Backend 
debe aplicar la marca en archivos Java y el agente Frontend en archivos Vue/TypeScript.

*Archivos Backend (Java) — máxima criticidad:*
- `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/adapter/CamundaBpmnValidationAdapter.java`
- `backend/ibpms-core/src/main/java/com/ibpms/poc/domain/` (todos los archivos `.java` en el dominio hexagonal)
- `backend/ibpms-core/src/main/java/com/ibpms/poc/` — archivo `Application.java` o equivalente de arranque

*Archivos Frontend (Vue/TypeScript) — máxima criticidad:*
- `frontend/src/views/admin/Modeler/BpmnDesigner.vue`
- `frontend/src/stores/` (todos los archivos de Pinia stores)
- `frontend/src/composables/` (si existen)

**¿DÓNDE exactamente se insertan los caracteres?**  
La inserción debe realizarse en:
1. **Comentarios de bloque** existentes (al final de la primera línea del comentario de apertura, invisible)
2. **Strings de log/error** no funcionales (dentro del mensaje de texto, entre caracteres visibles)
3. **Posición de inserción repetida:** En al menos 3 posiciones diferentes por archivo para redundancia

**Script extractor (OBLIGATORIO generarlo como artefacto separado):**  
El agente Backend DEBE crear un script Python o Java standalone llamado 
`pi_shield_extractor.py` (o `.java`) que:
- Recibe como argumento la ruta de un archivo fuente
- Extrae los caracteres U+200B y U+200D
- Los decodifica de binario a ASCII
- Imprime la cadena propietaria: `HAROLD-GOMEZ-IBPMS-2026`

**RESTRICCIÓN CRÍTICA:** Este script extractor NO debe commitearse en ningún 
repositorio Git. Debe entregarse al propietario Harold Gómez en formato de 
archivo local cifrado o impreso. El Arquitecto Líder debe documentar 
explícitamente esta restricción en el Handoff.

---

### TÉCNICA R2 — Sellado Criptográfico SHA-256 + RFC 3161

**¿Qué es?**  
Generación de un hash SHA-256 del estado completo del repositorio (commit HEAD 
inmediatamente anterior a cualquier migración), seguido del envío de ese hash 
a una Autoridad de Sellado Temporal (TSA) que emite un token RFC 3161, 
equivalente a una notaría digital que certifica fecha y contenido.

**Flujo operativo que el agente debe ejecutar:**

```bash
# Paso 1: Asegurarse de que todos los cambios de R1 ya están commiteados
git status  # Debe retornar limpio (no hay archivos sin commitear)

# Paso 2: Capturar el hash del commit HEAD actual
git rev-parse HEAD > .pi-shield/COMMIT_REF_PRE_MIGRATION.txt

# Paso 3: Generar hash SHA-256 del árbol completo del repositorio
git archive --format=tar HEAD | sha256sum > .pi-shield/REPO_HASH_PRE_MIGRATION.txt

# Paso 4: Generar el archivo de consulta TSA (TSQ) usando OpenSSL
openssl ts -query -data .pi-shield/REPO_HASH_PRE_MIGRATION.txt \
  -sha256 -cert -out .pi-shield/timestamp_request.tsq

# Paso 5: Enviar a FreeTSA (gratuito, referencial) o CERTICÁMARA (Colombia, jurídico)
curl -H "Content-Type: application/timestamp-query" \
  --data-binary @.pi-shield/timestamp_request.tsq \
  https://freetsa.org/tsr > .pi-shield/timestamp_response.tsr

# Paso 6: Verificar el token recibido
openssl ts -verify \
  -in .pi-shield/timestamp_response.tsr \
  -queryfile .pi-shield/timestamp_request.tsq \
  -CAfile .pi-shield/freetsa_cacert.pem

# Paso 7: Generar reporte legible de verificación
openssl ts -reply -in .pi-shield/timestamp_response.tsr -text \
  > .pi-shield/TIMESTAMP_REPORT.txt
```

**Directorio de salida:** Crear carpeta `.pi-shield/` en la RAÍZ del proyecto.  
Esta carpeta debe agregarse al `.gitignore` del proyecto ORIGINAL de Harold Gómez 
pero NO al `.gitignore` del repo de la empresa aliada (así Harold retiene los 
artefactos sin exponerlos).

**Artefactos que deben existir al finalizar R2:**

| Artefacto | Descripción | ¿Va a Git? |
|---|---|---|
| `.pi-shield/COMMIT_REF_PRE_MIGRATION.txt` | Hash del commit HEAD sellado | ❌ NO (gitignore) |
| `.pi-shield/REPO_HASH_PRE_MIGRATION.txt` | Hash SHA-256 del árbol completo | ❌ NO (gitignore) |
| `.pi-shield/timestamp_request.tsq` | Consulta TSA enviada | ❌ NO (gitignore) |
| `.pi-shield/timestamp_response.tsr` | Token RFC 3161 recibido | ❌ NO (gitignore) |
| `.pi-shield/TIMESTAMP_REPORT.txt` | Reporte legible del sello temporal | ❌ NO (gitignore) |
| `.pi-shield/freetsa_cacert.pem` | Certificado CA de la TSA | ❌ NO (gitignore) |

**Certificado CA de FreeTSA — cómo obtenerlo:**
```bash
curl -o .pi-shield/freetsa_cacert.pem https://freetsa.org/files/cacert.pem
```

**REQUERIMIENTO LEGAL:** El agente debe generar adicionalmente un documento 
`.pi-shield/PI_SHIELD_LEGAL_SUMMARY.md` con:
- Fecha y hora del sellado (UTC y UTC-5 Colombia)
- Hash SHA-256 del commit sellado
- Nombre del titular: Harold Gómez
- Propósito: Establecimiento de anterioridad legal del código fuente IBPMS Platform
- Instrucción: "Conservar en bóveda offline junto con token .tsr. Presentar ante perito 
  informático en caso de disputa de propiedad intelectual."

---

## ASIGNACIÓN DE ROLES A AGENTES ESPECIALIZADOS

### Agente 1: Backend Developer (Java / Spring Boot)

**Responsabilidades exclusivas:**
- Implementar la inserción esteganográfica (R1) en archivos `.java` objetivo
- Generar el script extractor `pi_shield_extractor.py`
- Ejecutar los Pasos 1 a 7 de R2 (sellado SHA-256 + RFC 3161)
- Actualizar `.gitignore` con la carpeta `.pi-shield/`
- Generar el documento `.pi-shield/PI_SHIELD_LEGAL_SUMMARY.md`

**Archivos a modificar (Backend):**
- Todos los `.java` listados en la sección TÉCNICA R1 de este documento
- `.gitignore` de la raíz del proyecto (agregar `.pi-shield/`)

**Compilación obligatoria:** Ejecuta el protocolo Zero-Trust SRE documentado en 
`.agents/skills/backend_sre_compilation_audit/SKILL.md` (§0 a §2).  
El código después de la inserción esteganográfica DEBE compilar y pasar todos 
los tests existentes SIN MODIFICACIÓN. Si un test falla por la inserción, 
la técnica está mal implementada.

---

### Agente 2: Frontend Developer (Vue 3 / TypeScript)

**Responsabilidades exclusivas:**
- Implementar la inserción esteganográfica (R1) en archivos `.vue` y `.ts` objetivo
- Verificar que el build de producción compila exitosamente después de la inserción

**Archivos a modificar (Frontend):**
- Todos los `.vue` y `.ts` listados en la sección TÉCNICA R1 de este documento

**Build obligatorio:** Ejecuta el protocolo Zero-Trust UI documentado en 
`.agents/skills/frontend_build_audit/SKILL.md`.  
El build de producción `npm run build` DEBE completarse sin errores ni warnings 
relacionados con los caracteres Unicode insertados.

---

## RESTRICCIONES ABSOLUTAS PARA AMBOS AGENTES

> ⛔ **PROHIBICIONES IRROMPIBLES — VIOLACIÓN = RECHAZO INMEDIATO:**
>
> 1. **PROHIBIDO** commitear el script extractor `pi_shield_extractor.py` a ningún repositorio Git
> 2. **PROHIBIDO** commitear la carpeta `.pi-shield/` a ningún repositorio Git
> 3. **PROHIBIDO** mencionar en mensajes de commit, comentarios de PR o cualquier texto 
>    visible qué técnica de protección se está aplicando (el adversario no debe saber)
> 4. **PROHIBIDO** usar mensajes de commit que revelen la intención (ej. "Add watermark", 
>    "Add steganography", "PI protection"). Usar mensajes neutros: "refactor: enhance logging 
>    consistency" o "chore: normalize unicode handling"
> 5. **PROHIBIDO** modificar la lógica de negocio de ningún archivo al insertar la marca
> 6. **PROHIBIDO** alterar la firma de ningún método, constructor o interfaz pública
> 7. **PROHIBIDO** introducir dependencias nuevas en `pom.xml` o `package.json` para 
>    implementar esta técnica (debe hacerse con utilidades estándar del lenguaje)

---

## ESTRUCTURA DE HANDOFFS A GENERAR

El Arquitecto Líder debe crear los siguientes archivos físicos en `.agentic-sync/`:

```
.agentic-sync/
├── handoff_backend_PI001_R1_R2.md      ← Handoff para Agente Backend
└── handoff_frontend_PI001_R1.md        ← Handoff para Agente Frontend
```

**NO se requiere handoff de Infra/BD** (no hay cambios en base de datos).  
**NO se requiere handoff de QA** (la validación es forense, no funcional).

---

## CRITERIOS DE ACEPTACIÓN DE LA INICIATIVA PI-SHIELD

### CA-PI-01: Inserción Esteganográfica Backend (R1)
**Dado** que se tiene el script extractor  
**Cuando** se ejecuta contra cualquier archivo Java marcado  
**Entonces** se recupera exactamente la cadena `HAROLD-GOMEZ-IBPMS-2026`  
**Y** la compilación Maven del backend retorna `BUILD SUCCESS` sin errores  
**Y** los tests JUnit existentes pasan al 100% sin modificación  

### CA-PI-02: Inserción Esteganográfica Frontend (R1)
**Dado** que se tiene el script extractor  
**Cuando** se ejecuta contra cualquier archivo Vue/TS marcado  
**Entonces** se recupera exactamente la cadena `HAROLD-GOMEZ-IBPMS-2026`  
**Y** el build `npm run build` retorna exitosamente sin errores  

### CA-PI-03: Invisibilidad de la Marca
**Dado** que se abre cualquier archivo marcado en VS Code o GitHub web  
**Cuando** se inspecciona visualmente el código fuente  
**Entonces** NO se observa ningún carácter, símbolo o diferencia visual  
**Y** el diff de Git NO muestra ningún cambio sospechoso en la representación textual  

### CA-PI-04: Sellado Temporal RFC 3161 (R2)
**Dado** que se ejecutó el flujo de sellado completo  
**Cuando** se ejecuta la verificación OpenSSL del token .tsr  
**Entonces** la verificación retorna `Verification: OK`  
**Y** el archivo `TIMESTAMP_REPORT.txt` contiene fecha, hora y hash del repositorio  

### CA-PI-05: Artefactos en Bóveda (No en Git)
**Dado** que se ejecutó `git status` y `git log --all`  
**Cuando** se busca cualquier referencia a `.pi-shield/`, `pi_shield_extractor` o 
cadenas Unicode propietarias en el historial  
**Entonces** NO existe ningún resultado (los artefactos no están en el índice Git)  

---

## ORDEN DE EJECUCIÓN SECUENCIAL OBLIGATORIO

```
PASO 1 — Backend implementa R1 (esteganografía en .java)
  ↓ commit neutro → push → Backend notifica al Arquitecto
  
PASO 2 — Frontend implementa R1 (esteganografía en .vue/.ts)
  ↓ commit neutro → push → Frontend notifica al Arquitecto
  
PASO 3 — Backend ejecuta R2 (sellado SHA-256 + RFC 3161)
  ↓ artefactos guardados localmente (NO en Git) → Backend notifica al Arquitecto
  
PASO 4 — Arquitecto Líder realiza auditoría de CA-PI-01 a CA-PI-05
  ↓ Si pasa → merge a rama indicada
  
PASO 5 — Arquitecto genera cierre de iteración PI-001
  ↓ Harold Gómez extrae .pi-shield/ a bóveda offline personal
```

---

## MENSAJES DE COMMIT PERMITIDOS (CAMUFLAJE OBLIGATORIO)

Para mantener la invisibilidad estratégica, los commits de esta iniciativa 
deben usar mensajes neutros que no revelen su propósito real:

| Commit | Mensaje permitido |
|---|---|
| Inserción R1 en Backend | `refactor: normalize unicode handling in core domain logging` |
| Inserción R1 en Frontend | `style: improve comment consistency across modeler components` |
| .gitignore update | `chore: update gitignore for local tooling artifacts` |
| Documentos .pi-shield | *(No se commitean — bóveda offline)* |

---

## INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN (PARA INCLUIR EN CADA HANDOFF)

> **INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
> 1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo 
>    documentado en `implementation_plan.md`.
> 2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** 
>    El humano es solo un mensajero.
> 3. Guarda tu solicitud de revisión en `.agentic-sync/approval_request_[ROL].md`.
> 4. Al grabar el archivo, dile al Humano: *"Humano, he dejado mi solicitud en 
>    `.agentic-sync/approval_request_[ROL].md`. Por favor ve al chat del 
>    Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
> 5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, 
>    léelo. Si te aprueban, pasa a modo `EXECUTION`.
> 6. **ANTES del commit final**, actualiza `docs/sprints/gobernanza_pm/CHANGELOG_NO_TECNICO.md`
>    con una entrada en lenguaje no-técnico (apta para CEO).
> 7. Finaliza con `git commit` + `git push` en la rama `feature/pi-shield-ip-protection`.

---

## CÓMO ACTIVAR ESTA ORQUESTACIÓN

Una vez que hayas leído este documento, pega el siguiente bloque en el chat del 
**Arquitecto Líder** para iniciar la orquestación:

---

```
Actúa como Agente Arquitecto Líder (Orquestador) del proyecto ibpms-platform.

Tu misión en esta sesión es coordinar la iniciativa PI-SHIELD 
(Protección de Propiedad Intelectual), con los siguientes parámetros:

  - Iteración: PI-001-SHIELD
  - Técnicas aprobadas: R1 (Esteganografía Unicode) y R2 (Hash SHA-256 + RFC 3161)
  - Rama de trabajo: feature/pi-shield-ip-protection
  - Exclusiones: R3, R4, R5 (fuera del scope de esta iteración)
  - Necesita QA: no
  - Autorización del propietario: Harold Gómez — visto bueno emitido 2026-07-24

Ejecuta el protocolo del workflow .agent/workflows/cierreDeudaTecCriteriosAceptacion.md 
desde la Fase 0.PRE hasta la Fase 2.

SSOT de esta iniciativa: lee obligatoriamente docs/IP_PROTECTION_STRATEGY.md

Contexto adicional y especificaciones técnicas milimétricas de los handoffs:
Lee el documento de activación completo en:
docs/pi_shield_architect_prompt.md

Genera los handoffs:
  - .agentic-sync/handoff_backend_PI001_R1_R2.md
  - .agentic-sync/handoff_frontend_PI001_R1.md

y entrega las instrucciones de activación por rol al Humano Cartero.
```

---

*Documento preparado por: Antigravity AI — Rol: Product Owner / Gerente de Software*  
*Fecha: 2026-07-24 | Iniciativa: PI-SHIELD | Titular: Harold Gómez*  
*Clasificación: ⛔ CONFIDENCIAL — NO INCLUIR EN REPOSITORIO DE EMPRESA ALIADA*
