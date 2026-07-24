# 🔴 Análisis de Errores CI/CD — PR #4 (DevDavid → main)

> **PR**: [#4 - Pull request US-034 - US-036 Y US-051](https://github.com/haroldklag85/ibpms-platform/pull/4)
> **Run**: [#277](https://github.com/haroldklag85/ibpms-platform/actions/runs/30042710350) | **Fecha**: 2026-07-23 20:35 UTC
> **Commit**: `b9c2a9e` — `fix(uat): resolve R3-01 deploy 400 and R3-02 FormDesigner TypeError bugs`
> **Actor**: `dorodrig` (David Rodríguez)
> **Estado Final**: ❌ **FAILURE** (mergeado a pesar de los errores)

---

## Resumen Ejecutivo

| Job | Resultado | Step Fallido | Duración |
|-----|-----------|--------------|----------|
| **Backend Build & Maven Verify** | ❌ Failure | Step 4: `Compile and Test (Quality Gate)` — `mvn clean verify` | 46s |
| **Frontend Vite Build & Crash Tests** | ❌ Failure | Step 7: `Execute Vitest Crash Tests` — `npm run test -- --run` | 1m 24s |
| **Build & Push to ACR** | ⏭️ Skipped | N/A (depende de backend + frontend) | 0s |
| **CD Deploy to Azure QA/PROD** | ⏭️ Skipped | N/A (depende de docker-build-push) | 0s |

> [!CAUTION]
> **Ambos Quality Gates fallaron**, lo que significa que tanto el backend Java como el frontend Vue 3 tienen errores de compilación/pruebas. Los jobs de Docker Build y Deploy se saltaron automáticamente porque dependen de que los Quality Gates pasen primero.

---

## 🔴 ERROR 1: Backend — `mvn clean verify` falló

**Job**: `Backend Build & Maven Verify`
**Step fallido**: Step 4 — `Compile and Test (Quality Gate)`
**Comando**: `mvn clean verify -DskipTests=false`
**Working directory**: `./backend/ibpms-core`

### ¿Qué significa este error?

El comando `mvn clean verify` ejecuta la compilación completa del proyecto Java Spring Boot **incluyendo todas las pruebas unitarias y de integración**. El fallo indica que:

1. **El código Java no compila correctamente**, ó
2. **Las pruebas unitarias/integración de Spring Boot están fallando**

### Causa probable

Dado que el commit menciona `fix(uat): resolve R3-01 deploy 400 and R3-02 FormDesigner TypeError bugs`, es probable que los cambios en el backend hayan introducido:
- Imports rotos o clases faltantes tras la modificación
- Tests que dependían de comportamiento anterior que fue alterado
- Conflictos de configuración Spring después de la resolución de conflictos Git

> [!NOTE]
> Los logs detallados del paso de Maven requieren autenticación en GitHub para ver la salida completa. Se pueden consultar en: [Backend Job Logs](https://github.com/haroldklag85/ibpms-platform/actions/runs/30042710350/job/89326296898#step:4:19083)

---

## 🔴 ERROR 2: Frontend — Vitest Crash Tests fallaron (9 tests rotos)

**Job**: `Frontend Vite Build & Crash Tests`
**Step fallido**: Step 7 — `Execute Vitest Crash Tests (Zero Exit Code required)`
**Comando**: `npm run test -- --run`
**Working directory**: `./frontend`

### Tests que fallaron

La página de GitHub Actions muestra **exactamente 9 tests fallidos** en múltiples archivos spec:

| # | Test Fallido | Archivo | Línea |
|---|-------------|---------|-------|
| 1 | `CA-27: valida la inmutabilidad de roles CORE (SUPER_ADMIN, NATIVE_ADMIN)` | [IdentityGovernance.spec.ts](file:///c:/Users/USER/Desktop/Proyectos/Harold%20Ibpms/ibpms-platform/frontend/src/views/admin/Security/__tests__/IdentityGovernance.spec.ts#L111) | L111 |
| 2 | `CA-13: Indicador de versión muestra V{N} + estado` | [FormDesignerQACert.spec.ts](file:///c:/Users/USER/Desktop/Proyectos/Harold%20Ibpms/ibpms-platform/frontend/src/views/admin/Modeler/__tests__/FormDesignerQACert.spec.ts#L96) | L96 |
| 3 | `CA-12: Badge muestra "revoked" cuando certification state cambia` | [FormDesignerQACert.spec.ts](file:///c:/Users/USER/Desktop/Proyectos/Harold%20Ibpms/ibpms-platform/frontend/src/views/admin/Modeler/__tests__/FormDesignerQACert.spec.ts#L64) | L64 |
| 4 | `CA-83: renders Autocompletar Fuzz button and populates payload on click` | [FormDesignerCA83.spec.ts](file:///c:/Users/USER/Desktop/Proyectos/Harold%20Ibpms/ibpms-platform/frontend/src/views/admin/Modeler/__tests__/FormDesignerCA83.spec.ts#L120) | L120 |
| 5 | `Bug 1: Debe reactivamente sincronizar processId en la URL query cuando cambie` | [BpmnDesigner.spec.ts](file:///c:/Users/USER/Desktop/Proyectos/Harold%20Ibpms/ibpms-platform/frontend/src/views/admin/Modeler/BpmnDesigner.spec.ts#L2099) | L2099 |
| 6 | `CA-32: Un error 403 con código general debe invocar purgeTopology() del MenuStore` | [axiosInterceptor.spec.ts](file:///c:/Users/USER/Desktop/Proyectos/Harold%20Ibpms/ibpms-platform/frontend/src/tests/services/axiosInterceptor.spec.ts#L63) | L63 |
| 7 | `MockPath_Returns_Array_For_FieldArray` | [useFormDesignerStore.spec.ts](file:///c:/Users/USER/Desktop/Proyectos/Harold%20Ibpms/ibpms-platform/frontend/src/stores/__tests__/useFormDesignerStore.spec.ts#L41) | L41 |
| 8 | `AvailableStages_Computed_Removes_Duplicates` | [useFormDesignerStore.spec.ts](file:///c:/Users/USER/Desktop/Proyectos/Harold%20Ibpms/ibpms-platform/frontend/src/stores/__tests__/useFormDesignerStore.spec.ts#L20) | L20 |
| 9 | `should revoke user session (CA-14)` | [rbacStore.spec.ts](file:///c:/Users/USER/Desktop/Proyectos/Harold%20Ibpms/ibpms-platform/frontend/src/stores/rbacStore.spec.ts#L25) | L25 |

### Análisis por categoría de los tests fallidos

#### 🔸 Módulo Security (US-036)
- **IdentityGovernance.spec.ts** — Test de inmutabilidad de roles CORE falla, posiblemente la lógica de protección de roles fue alterada

#### 🔸 Módulo Modeler / FormDesigner (US-028 / US-034)
- **FormDesignerQACert.spec.ts** (2 tests) — Certificación QA del diseñador de formularios, badges y versionado
- **FormDesignerCA83.spec.ts** — Sandbox de pruebas Zod / Fuzzing, el botón "Autocompletar Fuzz" no renderiza
- **BpmnDesigner.spec.ts** — Sincronización reactiva del processId en la URL

#### 🔸 Módulo Stores (Pinia)
- **useFormDesignerStore.spec.ts** (2 tests) — El store del FormDesigner tiene regresiones en computed properties
- **rbacStore.spec.ts** — La revocación de sesión (CA-14) no funciona como se espera

#### 🔸 Servicios / Interceptores
- **axiosInterceptor.spec.ts** — El interceptor HTTP no invoca `purgeTopology()` del MenuStore ante un 403

---

## ⚠️ Advertencia adicional: Deprecación de Node.js 20

Ambos jobs (Backend y Frontend) muestran un **warning de deprecación**:

> Node.js 20 actions are deprecated. Please update the following actions to use Node.js 22: `actions/checkout@v4`, `actions/setup-java@v3`, `actions/setup-node@v4`.
> Referencia: [GitHub Changelog — Deprecation of Node 20](https://github.blog/changelog/2025-09-19-deprecation-of-node-20-on-github-actions-runners/)

> [!WARNING]
> Esto **no causa el fallo actual**, pero eventualmente estas Actions dejarán de funcionar si no se actualizan.

---

## 🧠 Diagnóstico Raíz

### ¿Por qué fallan los tests?

1. **Deuda técnica acumulada**: Los 4 workflow runs previos de la rama `DevDavid` (Runs #185, #186, #187, #277) **todos fallaron** con `conclusion: failure`. Esto indica que la rama DevDavid **nunca** pasó el pipeline exitosamente antes de ser mergeada.

2. **Desincronización código ↔ tests**: Los cambios en componentes Vue (FormDesigner, IdentityGovernance, BpmnDesigner) y stores Pinia (rbacStore, useFormDesignerStore) no fueron acompañados de actualizaciones correspondientes en sus tests.

3. **Regresiones por resolución de conflictos**: El historial de commits muestra múltiples resoluciones de conflictos Git (`merge bugfix governance-500`, `merge bugfix emergency-login`, `Resolución de conflictos Sprint-6`), lo que sugiere que la resolución de merges pudo haber roto la coherencia entre los componentes y sus tests.

4. **El PR fue mergeado con los checks fallidos**: El PR #4 fue mergeado a `main` **a pesar de que los Quality Gates estaban rotos**. Esto es una violación grave del flujo CI/CD ya que introduce código roto en la rama principal.

---

## 📋 Recomendaciones de Acción

| Prioridad | Acción |
|-----------|--------|
| 🔴 **CRÍTICA** | Corregir los 9 tests de Vitest que están fallando en el frontend |
| 🔴 **CRÍTICA** | Corregir el error de compilación/tests Maven en el backend |
| 🟡 **ALTA** | Configurar **branch protection rules** en `main` para **requerir** que los checks pasen antes de permitir merge |
| 🟡 **ALTA** | Actualizar las GitHub Actions para usar Node.js 22 (`actions/checkout@v5`, `actions/setup-java@v4`, `actions/setup-node@v5`) |
| 🟢 **MEDIA** | Investigar localmente ejecutando `mvn clean verify` en `./backend/ibpms-core` y `npm run test -- --run` en `./frontend` para ver los errores completos |
