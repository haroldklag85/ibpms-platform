# 🏗️ Handoff Consolidado INFRA — Soporte Zero-Mock (Scanner CI/CD + Movimiento de Archivos)

## 1. Metadatos y SSOT
- **Iteración:** 90-DEV-sprint6
- **Rama Git:** `sprint-6/uat-certification`
- **Objetivo:** Asegurar que el pipeline CI/CD bloquea automáticamente cualquier reintroducción de mocks y que el WorkdeskMockup se mueva correctamente.
- **Flujo:** 0️⃣ Infra (puede ejecutarse en paralelo con Backend y Frontend)

---

## 2. Tareas

### TAREA-INFRA-001: Integrar Anti-Mock Scanner en el Pipeline CI

**Contexto:** El scanner `frontend/scripts/anti-mock-scanner.js` actualmente solo se ejecuta como pre-commit hook local. Si un desarrollador bypasea el hook (con `--no-verify`), los mocks llegan al repositorio.

**Acción:**
- Agregar un step en el pipeline de CI (GitHub Actions / Azure DevOps — según lo que use el proyecto) que ejecute:
  ```bash
  cd frontend && node scripts/anti-mock-scanner.js
  ```
- Si el exit code es `1`, el pipeline **DEBE fallar** y bloquear el merge a `main` y a `sprint-*`.
- Ubicar este step **antes** del `npm run build` para fallar rápido.

### TAREA-INFRA-002: Mover WorkdeskMockup.vue a fixtures

**Acción:**
```bash
mkdir -p frontend/src/tests/fixtures
git mv frontend/src/views/WorkdeskMockup.vue frontend/src/tests/fixtures/WorkdeskMockup.vue
```
- Verificar que NO queden referencias en `frontend/src/router/index.ts`.
- Si existe una ruta tipo `{ path: '/workdesk-mockup', component: () => import('../views/WorkdeskMockup.vue') }`, eliminarla.

### TAREA-INFRA-003: Verificar exclusión de fixtures del scanner

**Contexto:** El scanner excluye archivos `.spec.ts`, `.test.ts` y `__tests__/`, pero NO excluye `tests/fixtures/`. Verificar que al mover `WorkdeskMockup.vue` a `tests/fixtures/`, el scanner **no lo escanee** porque su path ya no está en `src/views/`, `src/components/` ni `src/store/`.

**Resultado esperado:** El scanner no detecta `WorkdeskMockup.vue` tras moverlo porque `src/tests/` no está en `SCAN_DIRS`.

---

## 3. INSTRUCCIONES OPERATIVAS

1. Ejecuta las tareas directamente. No requieres aprobación del Arquitecto para cambios de infraestructura de CI.
2. **Commit y Push** en `sprint-6/uat-certification`.
3. Verifica que el pipeline corre el scanner correctamente con una ejecución de prueba.
