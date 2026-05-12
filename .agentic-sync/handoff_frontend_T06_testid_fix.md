# 🧠→🎨 Handoff: Arquitecto Líder → Frontend
# T-06 FIX: Agregar `data-testid="delegation-dropdown"` al Workdesk (Desbloquea CU-HEX-06/07)

**Emitido por:** [🧠 ARQUITECTO LÍDER]
**Destinatario:** [🎨 FRONTEND - VUE]
**Fecha:** 2026-05-11T22:57:00-05:00
**Sprint:** 7 — Iteración 7.1
**Prioridad:** 🔴 ALTA (Desbloquea certificación QA)
**Dependencia:** Ninguna — es una corrección quirúrgica de 1 línea

---

## 📖 LECTURAS OBLIGATORIAS (Ejecutar ANTES de codificar)

```bash
# 1. Política del Enjambre (LEY GLOBAL 0, 1, 2, 3)
cat .cursorrules

# 2. Tu skill de build audit
cat .agents/skills/frontend_build_audit/SKILL.md

# 3. Estándar de calidad de handoffs (referencia)
cat .agents/skills/handoff_quality_standard/SKILL.md
```

> ⚠️ **LEY GLOBAL 3 — Trazabilidad Inversa:** TODO el código nuevo DEBE incluir comentarios `// @Traceability: US-001, CA-04`. Esto es INNEGOCIABLE.

---

## 🔬 Diagnóstico del Arquitecto

El QA certificó 5/7 tests ✅. Los 2 tests pendientes (CU-HEX-06/CU-HEX-07) **no fallaron por lógica**, sino porque el spec busca `data-testid="delegation-dropdown"` y en `Workdesk.vue` el `<select>` de delegación tiene `data-testid="toggle-delegation"`.

**Esto es una discrepancia de nomenclatura, NO un bug funcional.**

| Componente | testid Actual (línea 51) | testid que QA busca |
|------------|:------------------------:|:-------------------:|
| `<select>` de delegación | `toggle-delegation` | `delegation-dropdown` |

### Estado real del Frontend (post-investigación):

| Hallazgo | Estado | Línea |
|----------|:------:|:-----:|
| `delegatedAssistants` ya NO es campo fantasma | ✅ Resuelto | Línea 54: `authStore.delegatedAssistants` (sin `as any`) |
| `fetchDelegatedAssistants()` invocado en `onMounted` | ✅ Implementado | Línea 596 |
| Error handling con Toast (no `alert()`) | ⚠️ Parcial | Línea 674 usa `alert()` — debe migrar a Toast |
| `data-testid` para QA | ❌ Discrepancia | `toggle-delegation` vs `delegation-dropdown` |

---

## 🎯 Instrucciones Quirúrgicas

### Paso 1: Agregar alias `data-testid="delegation-dropdown"` al `<select>` (1 línea)

**Archivo:** `frontend/src/views/Workdesk.vue`
**Línea:** 51

Cambiar:
```html
data-testid="toggle-delegation"
```
Por:
```html
data-testid="delegation-dropdown"
```

> **Nota:** Si otros tests ya usan `toggle-delegation`, mantener ambos:
> `data-testid="delegation-dropdown"` como el oficial.

### Paso 2: Reemplazar `alert()` en línea 674 con Toast

**Archivo:** `frontend/src/views/Workdesk.vue`
**Línea:** 674

Cambiar:
```javascript
alert('No tiene permisos para ver el escritorio de este usuario.');
```
Por:
```javascript
// @Traceability: US-001, CA-15 — Fail-Fast delegación denegada (Toast, no alert)
store.errorMessage = 'No tiene permisos para ver el escritorio de este usuario.';
store.isError = true;
```

### Paso 3: Agregar `data-testid="workdesk-container"` al div raíz

**Archivo:** `frontend/src/views/Workdesk.vue`
**Línea:** 2

Cambiar:
```html
<div class="h-full flex flex-col relative bg-gray-50 font-['Inter']" v-cloak>
```
Por:
```html
<div class="h-full flex flex-col relative bg-gray-50 font-['Inter']" v-cloak data-testid="workdesk-container">
```

### Paso 4: Build

```bash
cd frontend
npm run build
```

---

## Criterios de Aceptación (DoD)

| # | Criterio | Evidencia |
|---|----------|-----------|
| 1 | `data-testid="delegation-dropdown"` presente en el `<select>` | `grep "delegation-dropdown" Workdesk.vue` → 1 resultado |
| 2 | Cero usos de `alert()` en Workdesk.vue | `grep "alert(" Workdesk.vue` → 0 resultados |
| 3 | `data-testid="workdesk-container"` en div raíz | `grep "workdesk-container" Workdesk.vue` → 1 resultado |
| 4 | `npm run build` → Build successful | Log de terminal |
| 5 | Commit en rama de sprint | `git log -1` |

---

## 🚦 SECUENCIA DE EJECUCIÓN

1. Leer `.cursorrules` y skills listados arriba.
2. Aplicar los 3 cambios quirúrgicos (Pasos 1-3).
3. Build: `npm run build`.
4. Commit: `git add . && git commit -m "fix(US-001/CA-04): align delegation testid + purge alert → toast" && git push`

---

**RECUERDA:** Esta es una corrección de 3 líneas. NO refactorices lógica adicional (LEY GLOBAL 0, §3 — Libertad Controlada).
