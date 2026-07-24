# 🚨 INFORME FORENSE CRÍTICO — UAT RONDA 2 (BUGS PERSISTENTES)
## Iteración 84-DEV-LANE-ROLE-UAT-R2
**Fecha:** 2026-07-17 | **PM/PO-IA** | **MANDATORIO — Tolerancia Cero**

---

> [!CAUTION]
> ## AUTOCRÍTICA PM/PO-IA
> La auditoría Ronda 3 FALLÓ porque verificó **estructura de código** (el código existe) pero NO verificó **comportamiento runtime** (el código funciona). Específicamente:
>
> - **R2-03 (FormDesigner):** El auditor verificó que el endpoint `GET /{technicalName}` EXISTE y que `showPatternModal.value = false` EXISTE — pero **NUNCA verificó que los nombres de campos del DTO coinciden con lo que el frontend espera.** El agente que escribió `fetchForm` inventó `schemaVariables` — un campo que JAMÁS existió en `FormDesignDTO.java`.
> - **R2-01 (Deploy):** El auditor NUNCA revisó la lógica interna del deploy endpoint. El endpoint existe y acepta POST, pero hace un **chequeo manual de rol** (`BPMN_Release_Manager`) que no fue informado ni verificado.
> - **R2-02 (Menú):** Efecto cascada del R2-01. El interceptor global de Axios destruye la topología del menú ante CUALQUIER 403.
>
> **Lección aprendida:** Las auditorías futuras deben verificar el flujo E2E completo (request → response → parsing), no solo la existencia del código.

---

## 1. DIAGNÓSTICO POR BUG

### 🔴 R2-01: Deploy retorna 403 Forbidden

**Severidad:** CRÍTICA | **Tipo:** Bug de configuración de roles

**Cadena de fallo:**
```
Frontend: POST /api/v1/design/processes/deploy (con Bearer token)
  ↓
SecurityConfig: .requestMatchers("/api/v1/design/processes/**").permitAll() → PASA ✅
  ↓
JwtAuthFilter: Puede o no poblar SecurityContext (el path es permitAll)
  ↓
BpmnDesignController.java (L120-148): CHEQUEO MANUAL DE ROL
  auth.getAuthorities().stream()
    .anyMatch(a -> a.getAuthority().contains("BPMN_Release_Manager"))
  → FALSE (usuario no tiene este rol)
  → !hasRole && !isSandbox → return 403 Forbidden ❌
```

**Código exacto del chequeo** — [BpmnDesignController.java](file:///c:/Users/USER/Desktop/Proyectos/Harold Ibpms/ibpms-platform/backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/web/BpmnDesignController.java) L120, L146-149:
```java
// L120
boolean hasRole = auth != null && auth.getAuthorities().stream()
    .anyMatch(a -> a.getAuthority().contains("BPMN_Release_Manager"));

// L146-149
if (!hasRole && !isSandbox) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(Map.of("error", "Acceso Denegado. Se requiere el rol BPMN_Release_Manager o modo Sandbox."));
}
```

**¿Por qué `auth` puede ser null?** Porque `SecurityConfig.java` L80 declara el path como `.permitAll()`, lo que significa que Spring Security NO exige autenticación para esa ruta. El `JwtAuthFilter` intentará poblar el SecurityContext si hay un token válido, pero si el token falta, está expirado, o el filtro no procesa bien la request multipart, `auth` será `null`.

**CORRECCIÓN REQUERIDA:**

| Opción | Descripción | Recomendación |
|--------|-------------|:------------:|
| **A** | Cambiar en `SecurityConfig.java` L80: excluir `/deploy` del `.permitAll()` y requerir autenticación | ✅ **Recomendada** |
| **B** | Agregar `@PreAuthorize("hasRole('BPMN_Release_Manager')")` al método deploy Y remover el chequeo manual | Alternativa |
| **C** | Asegurar que el frontend envía header `X-Sandbox-Mode: true` durante desarrollo/testing | Workaround rápido |

**Corrección Opción A — Detalle:**

En [SecurityConfig.java](file:///c:/Users/USER/Desktop/Proyectos/Harold Ibpms/ibpms-platform/backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/security/SecurityConfig.java) L80:
```diff
- .requestMatchers("/api/v1/design/processes/**", "/api/v1/design/sandbox/**").permitAll()
+ .requestMatchers("/api/v1/design/sandbox/**").permitAll()
+ .requestMatchers("/api/v1/design/processes/**").authenticated()
```

**PERO ATENCIÓN:** Esto requiere que el `JwtAuthFilter` SIEMPRE procese correctamente el token para requests multipart. El agente DEBE verificar leyendo `JwtAuthFilter.java` que no hay exclusiones para multipart requests.

**Corrección Opción C — Workaround rápido (deploy funciona inmediatamente):**

En [BpmnDesigner.vue](file:///c:/Users/USER/Desktop/Proyectos/Harold Ibpms/ibpms-platform/frontend/src/views/admin/Modeler/BpmnDesigner.vue) L3588, agregar el header:
```typescript
const formData = new FormData();
// ... append fields ...
deployResponse = await integrationStore.deployProcess(formData);
```

En [useIntegrationStore.ts](file:///c:/Users/USER/Desktop/Proyectos/Harold Ibpms/ibpms-platform/frontend/src/stores/useIntegrationStore.ts) L43-45:
```diff
  deployProcess(payload: any) {
-   return this.post(`/design/processes/deploy`, payload, { headers: { 'Content-Type': 'multipart/form-data' } });
+   return this.post(`/design/processes/deploy`, payload, { headers: { 'Content-Type': 'multipart/form-data', 'X-Sandbox-Mode': 'true' } });
  },
```

> **⚠️ DECISIÓN DEL HUMANO:** Harold, ¿el deploy debe requerir el rol `BPMN_Release_Manager` en producción? Si es así, necesitamos asignar ese rol al usuario de pruebas. Si NO (para desarrollo), usamos la Opción C con el header de sandbox.

---

### 🔴 R2-02: Menú Lateral DESAPARECE después del 403

**Severidad:** ALTA | **Tipo:** Efecto cascada de R2-01

**Cadena de fallo:**
```
R2-01 → Controller retorna 403 con body: {"error": "Acceso Denegado..."}
  ↓
apiClient.ts (L208-267): Interceptor global de respuestas
  ↓
¿403? → Verifica response.data.code:
  - 'SECURITY_VIOLATION' → No (body no tiene 'code')
  - 'PROMPT_INJECTION' → No
  - 'PRIVILEGES_CHANGED' → No
  - ELSE (catch-all) → ⚡ SE EJECUTA ESTE BRANCH ⚡
    ↓
    menuStore.purgeTopology() → layout.value = [] → menú DESTRUIDO
    ↓
    MainLayout.vue: layout.length === 0 → muestra "Sin Topología de Menús"
```

**Código exacto del interceptor destructivo** — [apiClient.ts](file:///c:/Users/USER/Desktop/Proyectos/Harold Ibpms/ibpms-platform/frontend/src/services/apiClient.ts) L248-253:
```typescript
// CA-32: catch-all para CUALQUIER 403 sin code específico
console.warn('CA-32: Revocación de acceso detectada (403). Purgando topología local.');
const menuStore = useMenuStore();
menuStore.purgeTopology();  // ← DESTRUYE el menú
```

**¿Por qué no se recupera?** Nadie llama `fetchMenuLayout()` después del purge. El menú solo se carga en `onMounted` de `MainLayout.vue` (ya ejecutado) o al cambiar de rol.

**CORRECCIÓN REQUERIDA:**

En [apiClient.ts](file:///c:/Users/USER/Desktop/Proyectos/Harold Ibpms/ibpms-platform/frontend/src/services/apiClient.ts) L248-253, el catch-all del 403 debe discriminar entre errores operacionales (como el deploy) y revocaciones reales de privilegios:

```diff
  } else {
-   console.warn('CA-32: Revocación de acceso detectada (403). Purgando topología local.');
-   const menuStore = useMenuStore();
-   menuStore.purgeTopology();
+   // CA-32: Solo purgar topología si el 403 viene con indicador explícito de revocación
+   // Los 403 operacionales (deploy sin rol, acceso denegado a recurso específico) NO deben destruir la sesión
+   if (error.response.data?.code === 'ACCESS_REVOKED' || error.response.data?.code === 'ROLE_REVOKED') {
+     console.warn('CA-32: Revocación de acceso detectada (403). Purgando topología local.');
+     const menuStore = useMenuStore();
+     menuStore.purgeTopology();
+   } else {
+     console.warn('403 operacional (no es revocación). Recurso: ' + error.config?.url);
+   }
  }
```

**EFECTO:** Los 403 del deploy (y cualquier otro 403 operacional) ya NO destruirán el menú. Solo los 403 con `code: 'ACCESS_REVOKED'` o `'ROLE_REVOKED'` lo harán.

---

### 🔴 R2-03: FormDesigner SIGUE sin Cargar Formularios

**Severidad:** CRÍTICA | **Tipo:** Campo inventado por agente (ALUCINACIÓN)

**Cadena de fallo:**
```
Frontend: GET /api/v1/forms/UATV3 → Backend responde 200 OK ✅
  ↓
Backend retorna JSON con campo: "formFields" (Array)
  ↓
Frontend (useFormDesignerStore.ts L284): 
  if (response.data.schemaVariables) → ❌ undefined (NO existe "schemaVariables")
  ↓
  → return { success: false, message: 'El formulario no contiene un esquema válido.' }
  ↓
  → showPatternModal.value = false NUNCA se ejecuta
  → Toast de error + Dialog "Crear Nuevo Formulario" persiste
```

**El campo que el backend REALMENTE devuelve** — [FormDesignDTO.java](file:///c:/Users/USER/Desktop/Proyectos/Harold Ibpms/ibpms-platform/backend/ibpms-core/src/main/java/com/ibpms/poc/application/dto/FormDesignDTO.java):
```java
private List<FormFieldMetadataDTO> formFields;  // ← ESTE es el nombre real
```

**El campo que el frontend BUSCA** — [useFormDesignerStore.ts](file:///c:/Users/USER/Desktop/Proyectos/Harold Ibpms/ibpms-platform/frontend/src/stores/useFormDesignerStore.ts) L284:
```typescript
if (response.data && response.data.schemaVariables) {  // ← INVENTADO. No existe.
```

**Todos los mismatches:**

| Backend Devuelve | Frontend Busca | ¿Coincide? |
|-----------------|----------------|:----------:|
| `formFields` (array) | `schemaVariables` | 🔴 **NO** |
| `name` (string) | `title` → `name` (fallback) | ⚠️ Funciona por fallback |
| `version` (int) | `versionId` → `version` (fallback) | ⚠️ Funciona por fallback |

**CORRECCIÓN EXACTA:**

En [useFormDesignerStore.ts](file:///c:/Users/USER/Desktop/Proyectos/Harold Ibpms/ibpms-platform/frontend/src/stores/useFormDesignerStore.ts) L284-287:

```diff
- if (response.data && response.data.schemaVariables) {
-     canvasFields.value = typeof response.data.schemaVariables === 'string' 
-        ? JSON.parse(response.data.schemaVariables) 
-        : response.data.schemaVariables;
+ if (response.data && response.data.formFields) {
+     canvasFields.value = typeof response.data.formFields === 'string' 
+        ? JSON.parse(response.data.formFields) 
+        : response.data.formFields;
```

**REGLA:** El agente DEBE leer primero `FormDesignDTO.java` para ver los nombres de campos REALES antes de escribir el código del frontend.

---

## 2. RESUMEN Y PRIORIZACIÓN

| Bug | Tipo | Severidad | Archivos | Complejidad |
|-----|------|:---------:|----------|:-----------:|
| **R2-01** | Config/Security | 🔴 CRÍTICA | `SecurityConfig.java` o `useIntegrationStore.ts` | Media |
| **R2-02** | Interceptor destructivo | 🟡 ALTA | `apiClient.ts` L248-253 | Baja |
| **R2-03** | Campo inventado | 🔴 CRÍTICA | `useFormDesignerStore.ts` L284-287 | Baja (1 línea) |

### Orden de ejecución:
```
MC-A: R2-03 (FormDesigner) → 1 línea de cambio, impacto máximo
MC-B: R2-02 (Interceptor 403) → 5 líneas de cambio
MC-C: R2-01 (Deploy 403) → Requiere decisión del humano (Opción A, B, o C)
```

---

## 3. BLAST RADIUS ESTRICTO

### ✅ AUTORIZADO MODIFICAR

| Archivo | Bug | Zona Exacta |
|---------|-----|-------------|
| `useFormDesignerStore.ts` | R2-03 | L284-287 SOLAMENTE |
| `apiClient.ts` | R2-02 | L248-253 (catch-all del 403) |
| `SecurityConfig.java` O `useIntegrationStore.ts` | R2-01 | Según opción elegida |

### 🚫 PROHIBIDO TOCAR

| Archivo | Razón |
|---------|-------|
| `BpmnDesigner.vue` | Panel Lane ya funciona — no regresar |
| `FormDesignController.java` | Endpoint GET funciona correctamente |
| `FormDesignDTO.java` | Los nombres de campos del backend son CORRECTOS |
| `BpmnLaneService.java` | Fixes D-01 a D-05 intactos |
| Todo el panel Lane CSS | Ya usa Tailwind correctamente |

---

## 4. DECISIÓN REQUERIDA DEL HUMANO (Harold)

> [!WARNING]
> **Para R2-01 necesito tu decisión antes de instruir al Arquitecto:**
>
> **¿El deploy debe requerir `BPMN_Release_Manager` en producción?**
> - **Opción A:** Sí → Hay que asignar ese rol al usuario de pruebas en la BD
> - **Opción B:** No → Remover el chequeo manual del controller
> - **Opción C:** Para desarrollo, usar header `X-Sandbox-Mode: true` (workaround temporal)
