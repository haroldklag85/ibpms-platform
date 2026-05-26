# 🧠→🏗️ Handoff: QA E2E → Arquitectura/Backend
# US-005-BUG: Fallo Crítico (403 Forbidden) en Despliegue Sandbox (Zero-Blast Radius)

**Emitido por:** 🕵️ QA E2E
**Destinatario:** 🏗️ ARQUITECTO LÍDER / ⚙️ BACKEND
**Fecha:** 2026-05-23T17:07:00-05:00
**Sprint:** V1 — Certificación Zero-Mock
**Prioridad:** 🔴 Alta
**Dependencia:** Ninguna

## 📖 LECTURAS OBLIGATORIAS (Ejecutar ANTES de codificar)

```bash
# 1. Política del Enjambre (LEY GLOBAL 0, 1, 2, 3)
cat .cursorrules

# 2. Skill principal del agente receptor
cat .agents/skills/backend_sre_compilation_audit/SKILL.md

# 3. Skills transversales aplicables
cat .agents/skills/clean_code_standards/SKILL.md
cat .agents/skills/zero_mock_enforcement/SKILL.md

# 4. Handoff actual (Este documento)
cat .agentic-sync/handoff_qa_arquitectura_US005.md
```

> ⚠️ **LEY GLOBAL 3 — Trazabilidad Inversa:** TODO el código nuevo DEBE llevar la anotación @Traceability o comentario `// @Traceability: US-005, CA-63`. Esto es INNEGOCIABLE.

## 🔬 Diagnóstico del QA E2E

Durante la recertificación E2E automatizada de la historia de usuario **US-005** (CA-63, CA-67), se identificó que el backend rechaza la petición HTTP de despliegue (`/api/v1/design/processes/deploy`) devolviendo **HTTP 403 Forbidden** únicamente cuando se inyecta la cabecera `X-Sandbox-Mode: true`.
El interceptor `SandboxInterceptor.java` funciona correctamente exigiendo la cabecera, pero la capa de `SecurityConfig` está bloqueando la ruta o rechazando la cabecera en el pre-flight de CORS.

| Violación/Hallazgo | Ubicación | Detalle |
|--------------------|:---------:|---------|
| Bloqueo CORS de Cabecera | `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/security/SecurityConfig.java` | La cabecera `X-Sandbox-Mode` no está autorizada explícitamente en el arreglo de `allowedHeaders`. |
| Autorización de Endpoint | `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/web/BpmnDesignController.java:36` | Posible `@PreAuthorize` o `SecurityFilterChain` rechazando la invocación si la firma Sandbox muta el contexto JWT. |

## 🎯 Instrucciones Quirúrgicas

### Paso 1: Permitir cabecera CORS en SecurityConfig

**Archivo:** `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/security/SecurityConfig.java`

Agrega `X-Sandbox-Mode` a los `allowedHeaders` en la configuración de CORS.

```java
// Snippet prescriptivo — NO es pseudocódigo, es código ejecutable
CorsConfiguration configuration = new CorsConfiguration();
configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173", "http://127.0.0.1:5173"));
configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
// @Traceability: US-005, CA-63
configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Sandbox-Mode"));
configuration.setExposedHeaders(Arrays.asList("X-Sandbox-Mode"));
configuration.setAllowCredentials(true);
```

### Paso 2: Desbloqueo del Controlador (Si Aplica)

**Archivo:** `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/web/BpmnDesignController.java`

Revisar y mitigar cualquier restricción en `@PreAuthorize` que requiera roles ajenos al `ROLE_SUPER_ADMIN` o `ROLE_BPMN_RELEASE_MANAGER` que afecte de forma excluyente al Sandbox.

## Criterios de Aceptación (DoD)

| # | Criterio | Evidencia |
|---|----------|-----------|
| 1 | El pre-flight CORS permite explícitamente la cabecera Sandbox | `grep -E "X-Sandbox-Mode.*allowedHeaders" SecurityConfig.java` arroja resultados |
| 2 | Compilación limpia de Backend (Zero Errors) | `mvn clean compile test-compile` sin errores |
| 3 | Commit formalizado | `git log -1` muestra el fix |

## 🚦 SECUENCIA DE EJECUCIÓN

1. Inyectar `X-Sandbox-Mode` en el `CorsConfigurationSource` de `SecurityConfig.java`.
2. Validar que no haya restricciones residuales en el `BpmnDesignController.java`.
3. Compilar usando maven (en powershell): `cd backend\ibpms-core; ..\..\maven\apache-maven-3.9.6\bin\mvn.cmd clean compile test-compile`
4. Commit: `git add . && git commit -m "fix(security): habilitar CORS y pre-flight para X-Sandbox-Mode [US-005]" && git push`

## 📋 Instrucciones para Copiar y Pegar

```text
Asume el rol de 🏗️ ARQUITECTO LÍDER.

ANTES DE HACER CUALQUIER COSA, lee obligatoriamente estos archivos en este orden exacto:

1. cat .cursorrules
2. cat .agents/skills/backend_sre_compilation_audit/SKILL.md
3. cat .agents/skills/clean_code_standards/SKILL.md
4. cat .agents/skills/zero_mock_enforcement/SKILL.md
5. cat .agentic-sync/handoff_qa_arquitectura_US005.md

TU MISIÓN:

1. Modificar `SecurityConfig.java` para autorizar explícitamente el header `X-Sandbox-Mode` en el `CorsConfigurationSource`.
2. Reparar el `BpmnDesignController` si existen restricciones `@PreAuthorize` bloqueantes.
3. Build/Compile: `cd backend\ibpms-core; ..\..\maven\apache-maven-3.9.6\bin\mvn.cmd clean compile test-compile`
4. Commit: `git add .; git commit -m "fix(security): habilitar CORS para X-Sandbox-Mode [US-005]"`

REGLAS INQUEBRANTABLES:
- DEBES incluir "// @Traceability: US-005, CA-63" en los bloques de código CORS alterados.
- PROHIBIDO saltar la verificación de compilación Maven.
- PROHIBIDO modificar el `SandboxInterceptor.java` eliminando su obligatoriedad (es requerido funcionalmente).
```
