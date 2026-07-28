# 🎨 HANDOFF FRONTEND — Remediación CI/CD PR #4 (DevDavid → main)

> **Iteración**: `CICD-FIX-PR4`
> **US Afectadas**: US-005, US-028, US-034, US-036, US-051 (deuda técnica transversal)
> **Rama de trabajo**: `DevDavid`
> **Rol asignado**: Agente Frontend
> **Tipo**: Remediación de deuda técnica — NO es implementación de funcionalidad nueva
> **SSOT**: `docs/requirements/v1_user_stories_index.md` → Épicas A, B, E

---

## Pre-Handoff Checklist — CICD-FIX-PR4

| # | Verificación | Estado | Evidencia |
|---|---|---|---|
| 1 | Iteración autorizada por Arquitecto Líder | ✅ | Remediación de pipeline CI/CD bloqueante |
| 2 | Rama de trabajo confirmada | ✅ | `DevDavid` |
| 3 | No requiere nuevos endpoints | ✅ | No hay API nueva |
| 4 | 9 tests fallidos con causa raíz verificada | ✅ | Investigación forense archivo por archivo completada |

**Resultado**: ✅ APROBADO para ejecución

---

## 1. Metadatos y SSOT

- **Iteración**: CICD-FIX-PR4
- **Rama Git**: `DevDavid`
- **PR**: [#4 - Pull request US-034 - US-036 Y US-051](https://github.com/haroldklag85/ibpms-platform/pull/4)
- **Pipeline fallido**: GitHub Actions Run #277 — Job `Frontend Vite Build & Crash Tests`
- **Comando que falla**: `npm run test -- --run` en `./frontend`
- **Tests fallidos**: 9 exactos, todos identificados con causa raíz verificada

---

## 2. Alineación Arquitectónica y ADRs

| ADR | Impacto en esta tarea |
|-----|----------------------|
| **ADR-002** (Vue 3) | Componentes usan `<script setup>` — los tests deben respetar que las funciones internas NO son accesibles en `wrapper.vm` sin `defineExpose()` |
| **ADR-010** (Pirámide de Testing) | Los tests Vitest son Nivel 1 (unitarios) — deben probar comportamiento a través del DOM, no funciones internas |
| **ADR-014** (Error Observability Frontend) | Los interceptores HTTP tienen lógica específica de codes (`ACCESS_REVOKED`, `ROLE_REVOKED`) — los tests deben respetar esta semántica |

**Stack confirmado**: Vue 3 / Pinia / Axios / TypeScript / Vitest / Node.js 20 LTS

---

## 3. Diagnóstico Forense — 9 Tests Fallidos (Causas Raíz Verificadas)

> 🚫 **REGLA FUNDAMENTAL — CERO SUPOSICIONES**:
> Cada corrección que hagas DEBE basarse en la causa raíz verificada abajo. PROHIBIDO adivinar. PROHIBIDO cambiar código que no esté involucrado en el fallo. PROHIBIDO crear funciones, propiedades o constantes que no existan ya en el proyecto.

> 🚫 **CERO TOLERANCIA AL HARD-CODE**:
> PROHIBIDO inventar valores, funciones, tipos o estructuras que no existan en el código real del proyecto. Antes de referenciar cualquier propiedad, método o tipo, DEBES verificar que EXISTE leyendo el archivo fuente.

> 🚫 **CERO TOLERANCIA A LA IMAGINACIÓN**:
> PROHIBIDO pensar fuera del proyecto real. Si necesitas saber cómo funciona una función, LÉELA. No inventes comportamientos hipotéticos. Guíate EXCLUSIVAMENTE por el código real.

---

### ❌ TEST 1: `CA-27: valida la inmutabilidad de roles CORE (SUPER_ADMIN, NATIVE_ADMIN)`

**Archivo test**: `frontend/src/views/admin/Security/__tests__/IdentityGovernance.spec.ts` (L111)
**Archivo source**: `frontend/src/views/admin/Security/IdentityGovernance.vue`

**Causa raíz verificada**: El test llama `wrapper.vm.isCoreRole('SUPER_ADMIN')` y `wrapper.vm.openRoleModal()`. En Vue 3 `<script setup>`, las funciones declaradas internamente NO son accesibles en `wrapper.vm` a menos que el componente use `defineExpose()`. El componente `IdentityGovernance.vue` NO usa `defineExpose()` para estas funciones.

**Corrección OBLIGATORIA (ACTUALIZAR TEST)**:
1. Abre `IdentityGovernance.spec.ts`
2. Localiza el test en L111 (alrededor de esa línea)
3. En vez de invocar `wrapper.vm.isCoreRole()`, prueba el COMPORTAMIENTO visible:
   - Renderiza el componente con datos que incluyan roles CORE (`SUPER_ADMIN`, `NATIVE_ADMIN`)
   - Verifica que los botones de editar/eliminar están deshabilitados (`disabled`) para esos roles
   - Verifica mediante el DOM, NO mediante funciones internas
4. **PROHIBIDO** agregar `defineExpose()` al componente solo para que el test pase — eso es una solución artificial

---

### ❌ TESTS 2-3: `CA-13: Indicador de versión` y `CA-12: Badge revoked`

**Archivo test**: `frontend/src/views/admin/Modeler/__tests__/FormDesignerQACert.spec.ts` (L64 y L96)
**Archivo source**: `frontend/src/stores/useFormDesignerStore.ts` (método `fetchForm`)

**Causa raíz verificada**: El mock del test devuelve `{ data: { schemaVariables: "[]", isQaCertified: false, versionId: 5 } }`. Sin embargo, el store `useFormDesignerStore.ts` en su método `fetchForm` verifica `if (response.data && response.data.formFields)`. Como el mock usa `schemaVariables` en vez de `formFields`, y `versionId` en vez de `version`, el store salta la asignación de `certificationState` y `currentSchemaVersion`.

**Corrección OBLIGATORIA (ACTUALIZAR TEST)**:
1. Abre `FormDesignerQACert.spec.ts`
2. Localiza los mocks de `apiClient.get`
3. **Lee** `useFormDesignerStore.ts` → método `fetchForm` para confirmar los nombres exactos de los campos del DTO que usa
4. Actualiza el mock para usar los campos REALES del DTO: `formFields` (no `schemaVariables`), `version` (no `versionId`)
5. Verifica que la estructura del mock coincida con la estructura REAL que devuelve el endpoint `/forms/{id}`
6. **PROHIBIDO** inventar campos — lee el DTO real en el backend o el store que lo consume

---

### ❌ TEST 4: `CA-83: renders Autocompletar Fuzz button and populates payload on click`

**Archivo test**: `frontend/src/views/admin/Modeler/__tests__/FormDesignerCA83.spec.ts` (L120)
**Archivo source**: `frontend/src/views/admin/Modeler/FormDesigner.vue` (template) y `frontend/src/stores/useFormDesignerStore.ts` (método `generateMockPath`)

**Causa raíz verificada**: El template de `FormDesigner.vue` invoca `generateMockPath('fuzz')` con **1 solo argumento**. Pero la función `generateMockPath` en `useFormDesignerStore.ts` espera **2 argumentos**: `(type, fuzzerPayloadRef)` y ejecuta `fuzzerPayloadRef.value = ...`. Como `fuzzerPayloadRef` es `undefined`, lanza `TypeError: Cannot set properties of undefined (setting 'value')`.

**Corrección OBLIGATORIA (ACTUALIZAR SOURCE — opción elegida por el Arquitecto)**:
1. Abre `FormDesigner.vue`
2. Localiza en el template la invocación `generateMockPath('fuzz')`
3. Lee `useFormDesignerStore.ts` → método `generateMockPath` para verificar la firma exacta de la función y qué espera como segundo argumento
4. Identifica la variable `ref` local en `FormDesigner.vue` que debería pasarse como `fuzzerPayloadRef` (busca refs como `fuzzerPayload`, `fuzzPayload`, `mockPayload` o similar que ya existan)
5. Actualiza la invocación en el template para pasar AMBOS argumentos: `generateMockPath('fuzz', laRefQueEncontraste)`
6. **PROHIBIDO** crear una ref nueva si ya existe una que cumple el propósito — busca en el `<script setup>` del componente
7. **PROHIBIDO** modificar la función `generateMockPath` para hacerla tolerante a `undefined` — la decisión del Arquitecto es pasar ambos argumentos

---

### ❌ TEST 5: `Bug 1: Debe reactivamente sincronizar processId en la URL query cuando cambie`

**Archivo test**: `frontend/src/views/admin/Modeler/BpmnDesigner.spec.ts` (L2099)
**Archivo source**: `frontend/src/views/admin/Modeler/BpmnDesigner.vue` (~L3433)

**Causa raíz verificada**: El componente `BpmnDesigner.vue` fue refactorizado legítimamente para usar `window.history.replaceState()` (API nativa del navegador) en lugar de Vue Router `replace()` para actualizar la URL sin causar navegación. El test sigue espiando `useRouter().replace` (mocked como `mockReplace`) que ya no se invoca.

**Corrección OBLIGATORIA (ACTUALIZAR TEST)**:
1. Abre `BpmnDesigner.spec.ts`
2. Localiza el test alrededor de L2099
3. En vez de espiar `mockReplace` del router, espía `window.history.replaceState`:
   ```typescript
   const replaceStateSpy = vi.spyOn(window.history, 'replaceState');
   ```
4. Actualiza la aserción para verificar que `replaceStateSpy` fue llamado con los argumentos esperados (verifica en `BpmnDesigner.vue` ~L3433 qué argumentos pasa a `replaceState`)
5. **PROHIBIDO** revertir el refactoring del componente para volver a usar Vue Router — el refactoring fue legítimo

---

### ❌ TEST 6: `CA-32: Un error 403 con código general debe invocar purgeTopology() del MenuStore`

**Archivo test**: `frontend/src/tests/services/axiosInterceptor.spec.ts` (L63)
**Archivo source**: `frontend/src/services/apiClient.ts` (~L250)

**Causa raíz verificada**: El test simula un 403 genérico: `{ response: { status: 403, data: { message: 'Forbidden' } } }`. Pero en `apiClient.ts`, el interceptor de error 403 solo invoca `menuStore.purgeTopology()` si `error.response.data.code === 'ACCESS_REVOKED'` o `error.response.data.code === 'ROLE_REVOKED'`. El mock carece de la propiedad `code`, por lo que el interceptor correctamente cae en la rama `else` (403 operacional).

**Corrección OBLIGATORIA (ACTUALIZAR TEST)**:
1. Abre `axiosInterceptor.spec.ts`
2. Localiza el test alrededor de L63
3. Lee `apiClient.ts` ~L250 para confirmar las condiciones exactas que disparan `purgeTopology()`
4. Actualiza el mock del error para incluir el `code` correcto:
   ```typescript
   { response: { status: 403, data: { message: 'Forbidden', code: 'ACCESS_REVOKED' } } }
   ```
5. Verifica que el nombre del spy (`purgeSpy` o como se llame) coincide con el método real del MenuStore
6. **PROHIBIDO** modificar el interceptor en `apiClient.ts` para que purgue con TODOS los 403 — la lógica actual es correcta (solo purga ante revocación de acceso)

---

### ❌ TEST 7: `AvailableStages_Computed_Removes_Duplicates`

**Archivo test**: `frontend/src/stores/__tests__/useFormDesignerStore.spec.ts` (L20)
**Archivo source**: `frontend/src/stores/useFormDesignerStore.ts`

**Causa raíz verificada**: El test espera que `store.availableStages` exista como getter/computed. Sin embargo, `availableStages` **NO existe** en `useFormDesignerStore.ts` — ni como state, ni como getter, ni como computed.

**Corrección OBLIGATORIA (DECISIÓN DEL ARQUITECTO — Investigar y decidir)**:
1. Abre `useFormDesignerStore.ts` y busca `availableStages`, `stages`, `stage` en TODO el archivo
2. Busca en TODO el proyecto frontend si `availableStages` se usa en algún componente Vue:
   ```
   Busca "availableStages" en frontend/src/
   ```
3. **SI se usa en algún componente**: Un agente anterior probablemente eliminó el getter pero el componente lo necesita. Lee el componente para entender qué datos necesita y reconstruye el getter basándote en el componente real
4. **SI NO se usa en ningún componente**: El getter y el test son código huérfano. **ELIMINA** el test.
5. **PROHIBIDO** crear un getter inventado sin verificar si algo en el proyecto lo consume — guíate EXCLUSIVAMENTE por el código real

---

### ❌ TEST 8: `MockPath_Returns_Array_For_FieldArray`

**Archivo test**: `frontend/src/stores/__tests__/useFormDesignerStore.spec.ts` (L41)
**Archivo source**: `frontend/src/stores/useFormDesignerStore.ts` (método `generateMockPath` → helper `flatFields`)

**Causa raíz verificada**: `generateMockPath` usa un helper `flatFields()` que aplana todos los children de un `field_array` directamente en el objeto raíz, eliminando la agrupación parent. El test espera `parsed.gridData` como array, pero el resultado real es `undefined` porque los campos se aplanaron al nivel raíz.

**Corrección OBLIGATORIA (ACTUALIZAR TEST — decisión del Arquitecto: ajustar al comportamiento real)**:
1. Abre `useFormDesignerStore.ts` → localiza `generateMockPath` y su helper `flatFields`
2. Lee EXACTAMENTE cómo `flatFields` procesa un `field_array` — qué estructura produce
3. Actualiza el test para que las aserciones coincidan con el resultado REAL de `flatFields`:
   - Si `flatFields` aplana al nivel raíz, las propiedades hijas estarán directamente en el objeto resultado, NO dentro de `gridData`
   - Ajusta el `expect` para verificar las propiedades en la ubicación REAL donde `flatFields` las coloca
4. **PROHIBIDO** modificar `flatFields()` para cambiar su comportamiento de aplanamiento — la decisión del Arquitecto es ajustar el test al comportamiento real
5. **PROHIBIDO** asumir cómo funciona `flatFields` — LÉELO y basa tu corrección en lo que realmente hace

---

### ❌ TEST 9: `should revoke user session (CA-14)`

**Archivo test**: `frontend/src/stores/rbacStore.spec.ts` (L25)
**Archivo source**: `frontend/src/stores/rbacStore.ts` o `rbacStore.js`

**Causa raíz verificada**: La función `revokeUserSession` **SÍ está definida** dentro del `defineStore` de `rbacStore` (alrededor de L234), pero **NO fue incluida en el objeto `return`** del setup function del store. Por lo tanto, `store.revokeUserSession` es `undefined` y lanza `TypeError: store.revokeUserSession is not a function`.

**Corrección OBLIGATORIA (ACTUALIZAR SOURCE)**:
1. Abre `rbacStore.ts` (o `rbacStore.js` — verifica cuál existe)
2. Localiza la función `revokeUserSession` (~L234) — confirma que está definida
3. Busca el bloque `return { ... }` al final del `defineStore` setup function
4. Agrega `revokeUserSession` al objeto `return`:
   ```typescript
   return {
     // ... propiedades existentes,
     revokeUserSession,
   };
   ```
5. **PROHIBIDO** renombrar la función, moverla a otro lugar, o modificar su lógica — el único cambio es exponerla en el `return`
6. Verifica que no hay otras funciones definidas pero no expuestas que podrían causar problemas similares en el futuro

---

## 4. Orden de Ejecución Recomendado

| Orden | Prioridad | Test | Tipo de Corrección |
|:-----:|:---------:|------|-------------------|
| 1 | 🔴 | Test 9 (rbacStore) | SOURCE — agregar al return |
| 2 | 🔴 | Test 4 (FormDesigner CA-83) | SOURCE — pasar ambos args |
| 3 | 🟡 | Tests 2-3 (FormDesignerQACert) | TEST — mock desincronizado |
| 4 | 🟡 | Test 6 (axiosInterceptor) | TEST — mock incompleto |
| 5 | 🟡 | Test 5 (BpmnDesigner) | TEST — espía obsoleto |
| 6 | 🟡 | Test 1 (IdentityGovernance) | TEST — acceso a wrapper.vm |
| 7 | 🟡 | Test 8 (useFormDesignerStore) | TEST — aserción desalineada |
| 8 | 🔵 | Test 7 (availableStages) | INVESTIGAR → eliminar o reconstruir |

---

## 5. Verificación Final (EXIT GATE)

```powershell
cd frontend
npm run test -- --run
```

**Resultado esperado**: 0 failures. **Si hay failures, NO hagas push.**

Adicionalmente, verifica el build:
```powershell
npm run build
```

**Resultado esperado**: BUILD exitoso sin errores ni warnings críticos.

---

## 6. Políticas y Reglas Obligatorias

### 📋 DIRECTIVA DE INFRAESTRUCTURA HÍBRIDA
El backend Spring Boot corre en consola local (NO en Docker) en el puerto `8080`. Antes de iniciar tu trabajo, ejecuta el protocolo de pre-validación:
1. `curl -s http://localhost:8080/actuator/health` → Debe responder `{"status":"UP"}`.
2. Si no responde, arráncalo con: `cd backend && mvn spring-boot:run -pl ibpms-core -Dspring-boot.run.profiles=default`.
3. Verifica los servicios Docker: `docker ps` → PostgreSQL (`5433`), Redis (`6379`) y RabbitMQ (`5672`) deben estar `Up (healthy)`.
**PROHIBIDO** levantar el backend vía Docker o modificar el `docker-compose.yml`.

### 🛑 POLÍTICA ZERO-MOCK (`.agents/skills/zero_mock_enforcement/SKILL.md`)
- **PROHIBIDO** el uso de `mockAdapter.ts` o interceptores de red simulados en código de producción
- Los tests unitarios de Vitest SÍ pueden usar mocks de Axios (vi.mock) para aislar la lógica — pero los mocks DEBEN reflejar la estructura REAL del DTO del backend
- **PROHIBIDO** crear datos hardcodeados que simulen funcionalidad real en componentes de producción
- Todo mock de test debe basarse en la estructura REAL de la respuesta API documentada en `docs/sprints/gobernanza_pm/API_CONTRACTS.md`

### 📏 CLEAN CODE (`.agents/skills/clean_code_standards/SKILL.md`)
- **Tipado estricto**: PROHIBIDO usar `any` — define `interfaces` y `types` para contratos
- **Composición**: Setups menores a 100-150 líneas. Extrae lógica a Composables
- **Zero-Magic Strings**: Si hay enums u opciones, usa constantes compartidas
- **Ref/Reactive**: Usa `ref()` como estándar principal

### 🚫 REGLAS FUNDAMENTALES E IRROMPIBLES
> **CERO TOLERANCIA AL HARD-CODE**: Toda corrección debe basarse en el comportamiento REAL del sistema. PROHIBIDO inventar valores, funciones o estructuras.
>
> **CERO TOLERANCIA A LA SUPOSICIÓN**: Si algo no queda claro, DEBES leer el archivo fuente y verificar. PROHIBIDO asumir que un método existe o que un campo se llama de cierta manera.
>
> **CERO TOLERANCIA A LA IMAGINACIÓN**: Las correcciones deben alinearse con la arquitectura existente. PROHIBIDO crear soluciones fuera del patrón establecido.
>
> **CERO TOLERANCIA A PENSAR FUERA DEL PROYECTO REAL**: Toda referencia a clases, métodos, constantes o configuraciones DEBE verificarse contra el código real que existe en el repositorio. Si no existe, NO lo inventes.

---

> ⚠️ **IMPORTANTE:** Todo desarrollo o configuración debe quedar exhaustivamente documentado. Se exige **PRECISIÓN QUIRÚRGICA** en cada cambio para evitar efectos colaterales en funcionalidades existentes. Se han detectado regresiones en iteraciones previas; cualquier daño a funcionalidades adyacentes será motivo de rechazo inmediato.

---

## INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN

1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_FRONTEND_CICD.md`.
4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_FRONTEND_CICD.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION` y programa la solución.
6. **ANTES del commit final**, actualiza `docs/sprints/gobernanza_pm/CHANGELOG_NO_TECNICO.md` explicando tu trabajo en lenguaje sencillo y amigable para un CEO (QUÉ hiciste y PARA QUÉ sirve).
7. Finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama `DevDavid`. Queda estrictamente prohibido usar git stash.

> 📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
> - Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md` (naming, error handling, modularidad).
> - **Build obligatorio**: Ejecuta el protocolo Zero-Trust UI documentado en `.agents/skills/frontend_build_audit/SKILL.md`.

> 🧠 **POLÍTICA ANTIAMNESIA (OBLIGATORIA)**:
> Antes de tocar cualquier archivo, DEBES re-entrenar tu contexto leyendo:
> 1. `docs/architecture/arquitecturar.md` — Arquitectura Core
> 2. `docs/requirements/v1_user_stories_index.md` — Índice de US
> 3. `docs/sprints/gobernanza_pm/GUIA_ARQUITECTO_LIDER.md` — Gobernanza PM-IA
> La precisión de tu trabajo depende de que NO asumas cómo funciona el proyecto, sino que lo leas.
