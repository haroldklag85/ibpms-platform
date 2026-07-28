# 🔐 HANDOFF FRONTEND — Iniciativa PI-SHIELD | Iteración PI-001-SHIELD

> **Clasificación:** ⛔ CONFIDENCIAL — USO INTERNO EXCLUSIVO
> **Generado por:** Agente Arquitecto Líder (Orquestador)
> **Fecha de emisión:** 2026-07-24T14:19:00-05:00
> **Autorización del Propietario:** Harold Gómez — Visto bueno emitido 2026-07-24

---

## Pre-Handoff Checklist — PI-001-SHIELD (Frontend)

| # | Verificación | Estado | Evidencia |
|---|---|---|---|
| 1 | Iniciativa autorizada por el Propietario del producto | ✅ | Harold Gómez — visto bueno 2026-07-24 |
| 2 | No requiere Contratos de API (sin endpoints expuestos) | ✅ | Iniciativa forense — operación sobre sistema de archivos |
| 3 | Prerrequisito: Backend R1 commiteado y pusheado | ⚠️ | **Este agente comienza SOLO cuando el Backend confirme push de R1** |
| 4 | Rama Git activa | ✅ | `feature/pi-shield-ip-protection` |

**Resultado:** ✅ APROBADO — Iniciar SOLO después de recibir confirmación de push del Backend

---

## 1. Metadatos y SSOT

| Parámetro | Valor |
|---|---|
| **Iteración** | `PI-001-SHIELD` |
| **Iniciativa** | `PI-SHIELD — Protección de Propiedad Intelectual IBPMS Platform` |
| **Rol asignado** | Agente Frontend (Vue 3 / TypeScript) |
| **Técnica a implementar** | R1 — Esteganografía Unicode (SOLO Frontend) |
| **Técnicas EXCLUIDAS** | R2 (es responsabilidad del Backend), R3, R4, R5 |
| **Rama Git** | `feature/pi-shield-ip-protection` |
| **Necesita QA** | NO (validación es forense, no funcional) |
| **SSOT Principal** | `docs/IP_PROTECTION_STRATEGY.md` |
| **SSOT Técnico** | `docs/pi_shield_architect_prompt.md` |
| **Propietario** | Harold Gómez |
| **Dependencia de entrada** | ✅ Agente Backend ha hecho `git push` de R1 en archivos `.java` |
| **Flujo de Ejecución** | Backend R1 (✅ done) → **Frontend R1 (este agente)** → Backend R2 |

> ⚠️ **IMPORTANTE:** Todo desarrollo debe quedar exhaustivamente documentado. Se exige **PRECISIÓN QUIRÚRGICA** en cada cambio para evitar efectos colaterales. **PROHIBIDO** modificar la lógica de negocio, el comportamiento de ningún componente Vue, ni la firma de ninguna función TypeScript al insertar la marca.

---

## 2. Alineación Arquitectónica y ADRs

### ADRs Consultados

| ADR | Impacto en esta Iniciativa |
|---|---|
| **ADR-002** (Vue 3 + Vite + TypeScript) | Los archivos `.vue` y `.ts` objetivo siguen las convenciones del stack aprobado. La inserción NO debe alterar la funcionalidad de ningún componente. |
| **ADR-010** (Pirámide Testing) | El build de producción `npm run build` DEBE completarse sin errores post-inserción. Los tests Vitest existentes DEBEN pasar al 100%. |

### Validación del Stack Tecnológico

| Capa | Restricción |
|---|---|
| **Frontend** | Vue 3.4.x / TypeScript 5.2.x / Vite 5.1.x — **PROHIBIDO** introducir dependencias nuevas en `package.json` |
| **Herramienta de inserción** | Edición directa de archivos existentes con caracteres Unicode. Sin scripts adicionales ni módulos de Node. |

### Trazabilidad de la Solución

**R1 — Esteganografía Unicode en Vue/TypeScript:** La técnica inserta caracteres Unicode de ancho cero (U+200B y U+200D) dentro de comentarios de bloque y strings de texto en archivos `.vue` y `.ts`. El compilador Vite/TypeScript trata estos caracteres dentro de strings como parte del contenido del string, sin afectar el DOM, el comportamiento reactivo de Vue, ni el estado de Pinia. Los caracteres dentro de comentarios son ignorados por el compilador. **Compatibilidad total con ADR-002 y el stack Vite.**

### Riesgos Arquitectónicos Identificados

| Riesgo | Probabilidad | Mitigación |
|---|---|---|
| Un test de Vitest compare strings exactos con hardcode | Baja | Ejecutar `npm run test` post-inserción — si falla, revisar posición |
| Vite en modo producción minimice/elimine comentarios con ZW chars | Muy baja | Los ZW dentro de strings de texto son parte del runtime, no eliminados |
| El linter (ESLint) reporte advertencia por caracteres no ASCII en strings | Baja | Si aparece, verificar reglas de `unicode-escapes` — ajustar posición de inserción a comentarios |

---

## 📋 DIRECTIVA DE INFRAESTRUCTURA HÍBRIDA (Para validación de build)

El backend Spring Boot corre en consola local (NO en Docker) en el puerto `8080`. Para las validaciones de build del Frontend:

1. El build de Vite (`npm run build`) es independiente del backend — no necesita que esté corriendo.
2. Si se requiere validar que el frontend consume endpoints reales (NO requerido en esta iniciativa), entonces verificar: `curl -s http://localhost:8080/actuator/health` → `{"status":"UP"}`.

**PROHIBIDO** levantar el backend vía Docker o modificar el `docker-compose.yml`.

---

## 3. Rutas Exactas y Contexto Preexistente

### 3.1 Archivos Vue/TypeScript Objetivo — TÉCNICA R1 (Esteganografía Unicode)

Los siguientes archivos deben recibir la marca esteganográfica. El agente Frontend DEBE verificar su existencia antes de modificarlos:

| Prioridad | Ruta del Archivo | Descripción |
|---|---|---|
| 🔴 Crítica | `frontend/src/views/admin/Modeler/BpmnDesigner.vue` | Componente principal del modelador BPMN — máxima criticidad IP |
| 🟡 Alta | `frontend/src/stores/` | **Todos** los archivos `.ts` que sean Pinia stores (verificar con `dir frontend/src/stores/`) |
| 🟡 Alta | `frontend/src/composables/` | Todos los archivos `.ts` en composables (si el directorio existe y tiene archivos) |

> **Protocolo de verificación de existencia:** Antes de modificar cualquier archivo, ejecuta `dir` o verificación de existencia. Si `BpmnDesigner.vue` no existe en la ruta exacta, busca el archivo equivalente del modelador BPMN en `frontend/src/views/`. Reportar cualquier discrepancia en `approval_request_FRONTEND_PI001.md`.

### 3.2 Inventario Requerido de Stores y Composables

Antes de comenzar la inserción, el agente DEBE listar los archivos en estas carpetas:
- `frontend/src/stores/*.ts` — Listar todos los archivos Pinia
- `frontend/src/composables/*.ts` — Listar todos los composables (si existen)

El objetivo es marcar al menos:
- **BpmnDesigner.vue** (obligatorio — máxima criticidad)
- **Al menos 3 stores de Pinia** (los más críticos arquitectónicamente)
- **Al menos 2 composables** (si existen)

---

## 4. Snippets Prescriptivos

### 4.1 TÉCNICA R1 — Algoritmo de Inserción en Vue/TypeScript

#### Cadena propietaria a codificar
```
HAROLD-GOMEZ-IBPMS-2026
```

#### Mapeo de bits a caracteres Unicode (idéntico al Backend)
- Bit `0` → `U+200B` (Zero Width Space)
- Bit `1` → `U+200D` (Zero Width Joiner)

> La secuencia de caracteres ZW generada es idéntica en Backend y Frontend. Esta consistencia es importante: un perito forense puede demostrar que la misma cadena propietaria aparece tanto en archivos Java como en archivos Vue, lo que refuerza la unicidad de autoría.

#### Procedimiento de inserción en archivos Vue (.vue)

El agente debe insertar la secuencia ZW en **al menos 3 posiciones por archivo**:

**Posición 1 — Dentro del bloque `<script setup>`, en el comentario de bloque del encabezado:**
```vue
<script setup lang="ts">
/**
 * BpmnDesigner — Componente principal del modelador BPMN​‍‍​​‍‍​​‍​‍​‍​‍‍​‍​‍‍
 * Parte del módulo de Diseño de Procesos — IBPMS Platform
 */
```
> Los caracteres ZW aparecen al final de la primera línea del comentario JSDoc, antes del salto de línea. Invisibles en VS Code y GitHub.

**Posición 2 — Dentro de un string de mensaje de error o log existente:**
```typescript
// Si existe algo como: console.debug('BPMN diagram loaded')
console.debug('BPMN diagram​‍‍​​‍‍​​‍​‍​‍​‍‍​‍​‍‍​‍​​‍​‍‍​‍​‍‍​‍​​‍​‍‍​‍​‍‍​‍​​‍​‍ loaded')
```

**Posición 3 — En un comentario de línea descriptivo existente:**
```typescript
// Inicializar el motor BPMN con la configuración de la plataforma​‍‍​​‍‍​​‍​‍​‍​‍‍
```

#### Procedimiento de inserción en archivos TypeScript (.ts — Pinia Stores)

Para cada store de Pinia, seguir el mismo patrón de 3 posiciones:

**Posición 1 — Comentario de bloque del archivo:**
```typescript
/**
 * Store: [nombre del store] — IBPMS Platform​‍‍​​‍‍​​‍​‍​‍​‍‍​‍​‍‍
 * Gestiona el estado de [descripción]
 */
```

**Posición 2 — Comentario inline en una acción del store:**
```typescript
// Cargar datos desde la API del backend​‍‍​​‍‍​​‍​‍​‍​‍‍
async function fetchData() {
```

**Posición 3 — String de log o error (si existe):**
```typescript
// Si existe: throw new Error('Store initialization failed')
throw new Error('Store initialization​‍‍​​‍‍​​‍​‍​‍​‍‍​‍​‍‍​‍​​‍​‍ failed')
```

> ⚠️ **REGLA CRÍTICA PARA TYPESCRIPT:** Los caracteres ZW SOLO son seguros dentro de:
> - Comentarios de bloque (`/** */` o `/* */`)
> - Comentarios de línea (`//`)
> - Contenido de string literals (entre comillas `'`, `"` o backtick `` ` ``)
> 
> **NUNCA** insertar en: nombres de variables, funciones, clases, interfaces, imports, decoradores, o tokens TypeScript.

---

### 4.2 Verificación de Invisibilidad Post-Inserción

```powershell
# Verificar que el diff de Git no muestra caracteres visibles sospechosos
git diff HEAD

# El diff mostrará cambios de línea pero SIN caracteres nuevos visibles
# Es normal que git muestre las líneas modificadas — lo importante es que no haya
# caracteres visibles nuevos (letras, símbolos, espacios adicionales visibles)

# Verificar que el build de producción compila exitosamente
cd frontend
npm run build
# DEBE completar sin errores ni warnings relacionados con Unicode

# Verificar que los tests existentes pasan
npm run test
# DEBE completar con todos los tests en PASS
```

---

## 5. Matriz de QA y Testing Atómico

### 5.1 Criterios de Aceptación Asignados al Frontend

| CA | Descripción | Criterio de Verificación |
|---|---|---|
| **CA-PI-02** | Inserción Esteganográfica Frontend (R1) | Script extractor (entregado por Backend) retorna `HAROLD-GOMEZ-IBPMS-2026` en archivos Vue/TS marcados |
| **CA-PI-03** (parcial) | Invisibilidad de la Marca | VS Code y GitHub web no muestran caracteres visibles; `npm run build` exitoso |

### 5.2 Verificación Anti-Regresión Obligatoria

ANTES del commit final de R1, ejecutar el build completo:

```powershell
cd frontend

# Build de producción (Protocolo Zero-Trust UI)
npm run build
# DEBE retornar: "built in X.XXs" sin errores

# Tests Vitest
npm run test
# TODOS los tests deben pasar — si alguno falla, revisar la posición de inserción
```

> **Criterio de rechazo inmediato:** Si `npm run build` o `npm run test` falla después de la inserción, la técnica está mal implementada en ese archivo. Revertir la inserción en ese archivo específico y reportar la posición problemática en `approval_request_FRONTEND_PI001.md`.

### 5.3 Validación del Extractor (Post-Build)

El agente Backend habrá generado el script `pi_shield_extractor.py`. El agente Frontend DEBE solicitar al operador humano que ejecute el extractor sobre los archivos marcados como verificación forense:

```powershell
# Ejemplo de verificación (si Python está disponible)
python pi_shield_extractor.py "frontend/src/views/admin/Modeler/BpmnDesigner.vue"
# Debe retornar: ✅ MARCA DE AUTORÍA DETECTADA: HAROLD-GOMEZ-IBPMS-2026

python pi_shield_extractor.py "frontend/src/stores/[nombre_store].ts"
# Debe retornar: ✅ MARCA DE AUTORÍA DETECTADA: HAROLD-GOMEZ-IBPMS-2026
```

---

## 6. Mensajes de Commit Permitidos (CAMUFLAJE OBLIGATORIO)

> ⛔ **PROHIBICIÓN ABSOLUTA:** Los mensajes de commit NO deben mencionar "watermark", "steganography", "PI protection", "marca", "propiedad intelectual" o cualquier término que revele la intención.

| Commit | Mensaje PERMITIDO |
|---|---|
| Inserción R1 en BpmnDesigner.vue | `style: improve comment consistency across modeler components` |
| Inserción R1 en Stores | `refactor: normalize unicode handling in store definitions` |
| Inserción R1 en Composables | `chore: standardize comment format in composables` |

---

## 🛑 PROHIBICIONES ABSOLUTAS — VIOLACIÓN = RECHAZO INMEDIATO

1. **PROHIBIDO** commitear el script extractor `pi_shield_extractor.py` (no es tu responsabilidad — lo maneja el Backend)
2. **PROHIBIDO** commitear ningún artefacto en `.pi-shield/` (no es tu responsabilidad)
3. **PROHIBIDO** introducir dependencias nuevas en `package.json` para implementar esta técnica
4. **PROHIBIDO** modificar la lógica de ningún componente Vue ni la firma de ninguna función TypeScript
5. **PROHIBIDO** alterar el comportamiento visual o funcional de ningún componente al insertar la marca
6. **PROHIBIDO** mencionar en mensajes de commit, comentarios de PR o cualquier texto visible qué técnica se aplica
7. **PROHIBIDO** iniciar este trabajo antes de que el Agente Backend confirme que su push de R1 fue exitoso

---

## 📋 Build Obligatorio

**Build obligatorio:** Ejecuta el protocolo Zero-Trust UI documentado en `.agents/skills/frontend_build_audit/SKILL.md`.

---

## INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN

1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_FRONTEND_PI001.md`.
4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_FRONTEND_PI001.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION` y programa la solución.
6. **ANTES del commit final**, actualiza `docs/sprints/gobernanza_pm/CHANGELOG_NO_TECNICO.md` con una entrada en lenguaje no-técnico (apta para CEO). Ejemplo: *"Se realizaron mejoras de estandarización de comentarios y formato en los componentes del Modelador de Procesos y los módulos de gestión de estado de la plataforma."* — NO mencionar esteganografía ni PI.
7. Finaliza con `git commit` + `git push` en la rama `feature/pi-shield-ip-protection`. Queda estrictamente prohibido usar git stash.
8. **Una vez completado el push**, notifica al Arquitecto Líder (vía el Humano Cartero) con el mensaje: *"Frontend R1 completado y pusheado en feature/pi-shield-ip-protection. El Agente Backend puede proceder con la ejecución de R2 (sellado criptográfico)."*

> 📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
> - Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md`.
> - Aplica el skill de búsqueda `.agents/skills/grep_search_governance/SKILL.md` para localizar archivos objetivo de forma segura.

---

## Secuencia de Ejecución y Dependencias

```
PASO 1 (Backend — PREREQUISITO ✅): Inserción R1 en archivos .java → commit → push
    ↓
PASO 2 (Este agente): Inserción R1 en archivos .vue y .ts → commit neutro → push
    ↓
PASO 3 (Backend — SIGUIENTE): Ejecuta R2 (sellado SHA-256 + RFC 3161) 
    → artefactos guardados localmente en .pi-shield/ (NO en Git)
    ↓
PASO 4 (Arquitecto Líder): Audita CA-PI-01 a CA-PI-05
```

---

*Handoff generado por: Agente Arquitecto Líder — Iniciativa PI-001-SHIELD*
*Fecha: 2026-07-24 | Rama: feature/pi-shield-ip-protection | Clasificación: ⛔ CONFIDENCIAL*
