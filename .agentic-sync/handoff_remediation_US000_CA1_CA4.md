---
name: Handoff Remediación Auditoría US-000 Bloque 1
description: Handoff determinista para remediar los Gaps identificados en CA-1 a CA-4 correspondientes a Motor Core (API/Vue).
author: Arquitecto Líder
version: 1.0.0
---

# 🏗️ Handoff de Remediación (Auditoría Forense)

## 📌 1. Metadatos y SSOT
- **Iteración:** Iteración 1 - Auditoría Técnica Forense (Bloque 1)
- **User Story:** US-000 (Resiliencia Integrada y Enmascaramiento PII Visual)
- **Criterios de Aceptación a Remediar:** CA-1, CA-2, CA-3, CA-4
- **Path del SSOT:** `docs/requirements/v1_user_stories_index.md` -> `docs/requirements/epics/epic_A_motor_core.md`. **(PROHIBIDO usar `v1_user_stories.md`)**
- **Flujo de Trabajo:** Backend (API y Excepciones) -> Frontend (Axios e UI) -> QA (Validación Automática)

---

## 📌 2. Alineación Arquitectónica y ADRs
- **Validación de ADRs:** Cumplimiento irrestricto de ADR-001 (Arquitectura Hexagonal). Separación estricta de responsabilidades en la captura global de errores (`@RestControllerAdvice`).
- **Lineamientos Transversales:**
  - **Zero-Trust (Outbound):** No basta con enmascarar los *requests*. Es mandatorio aplicar enmascaramiento de identidad (CC, SSN) en los *responses* serializados salientes (CA-4).
  - **Supresión de Exposición (CA-1):** Se debe prohibir la inyección de `ex.getMessage()` o trazas al front. El rastro irá exclusívamente por log a ELK.
  - **UX/UI Profesional:** Queda categóricamente prohibido el uso de la primitiva `window.alert()` del navegador en las capas de intercepción Axios. 
- **Trazabilidad de la Solución:** Al centralizar en `GlobalExceptionHandler` el tipo de excepción de concurrencia optimista (`ObjectOptimisticLockingFailureException`) de Hibernate, cerramos el gap existente donde los conflictos de actualización explotaban como Errores 500 no capturados. Al usar un `ErrorStoreGlobal` logramos desacoplamiento nativo en Vue 3 para CA-1.

---

## 📌 3. Rutas Exactas y Contexto Preexistente

### Backend (Spring Boot Core)
1. **Archivo:** `ibpms-core/src/main/java/com/ibpms/poc/infrastructure/web/GlobalExceptionHandler.java`
   - *Estado Actual:* Captura `Exception` general exponiendo `ex.getMessage()`. Aplana los errores de `@Valid` en una cadena de texto en vez de estructurarlos. No intercepta concurrencia de Hibernate.
2. **Archivo:** `ibpms-core/src/main/java/com/ibpms/poc/infrastructure/web/interceptor/SensitiveDataLoggerAdvice.java` (O creación de equivalente para Responses)
   - *Estado Actual:* Enmascara bodies entrantes (RequestBody). No depura el payload JSON que retorna hacia los clientes externos.

### Frontend (Vue 3 / Vite)
1. **Archivo:** `frontend/src/services/apiClient.ts`
   - *Estado Actual:* Dispara alertas de navegador (`alert('Fatal Level 0 Dispatching');`) ante fallas [500, 502, 503].
2. **Archivo a Crear/Adaptar:** `frontend/src/stores/errorStore.ts` y componente visual `<ErrorStateGlobal>`
   - *Estado Actual:* Inexistente o desconectado del interceptor.

---

## 📌 4. Snippets Prescriptivos (El "Qué" y el "Cómo")

### **Backend: Manejo de Excepciones y Triage (CA-1, CA-2, CA-3)**
Inyectar estos manejadores en `GlobalExceptionHandler.java`:

```java
// CA-1: Blindaje de Fallas HTTP 500 a clientes
@ApiResponse(responseCode = "500", description = "Error interno - Blindado", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/problem+json"))
@ExceptionHandler(Exception.class)
public ProblemDetail handleGeneral(Exception ex) {
    log.error("💥 ERROR CRITICO DEL SISTEMA ENVIADO A ELK: ", ex); // Delegación a Logback/ELK
    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
    problem.setType(java.util.Objects.requireNonNull(URI.create("https://ibpms.com/errors/internal-error")));
    problem.setTitle("Error interno del servidor");
    // PROHIBIDO USAR ex.getMessage(). 
    problem.setDetail("Fallo del servidor reportado. Se ha generado un registro forense y equipo IT ha sido notificado.");
    return problem;
}

// CA-2: Retorno Estructurado para Triage UI (400)
@ExceptionHandler(MethodArgumentNotValidException.class)
public ProblemDetail handleValidationError(MethodArgumentNotValidException ex) {
    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    problem.setType(URI.create("https://ibpms.com/errors/validation-error"));
    problem.setTitle("Error de validación");
    problem.setDetail("Se hallaron errores de validación en la solicitud.");
    
    // Mapeo estructurado {field, issue} para que Frontend dibuje inputs rojos
    List<Map<String, String>> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> Map.of("field", fe.getField(), "issue", fe.getDefaultMessage()))
            .collect(Collectors.toList());
            
    problem.setProperty("errors", fieldErrors);
    return problem;
}

// CA-3: Bloqueo de Concurrencia Optimista (409)
@ExceptionHandler(org.springframework.orm.ObjectOptimisticLockingFailureException.class)
public ProblemDetail handleConcurrency(org.springframework.orm.ObjectOptimisticLockingFailureException ex) {
    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
    problem.setType(URI.create("https://ibpms.com/errors/optimistic-lock"));
    problem.setTitle("Conflicto de Múltiples Operadores");
    problem.setDetail("Datos oxidados. El registro fue modificado por otro operador recientemente. Por favor, refresque y vuelva a intentar.");
    return problem;
}
```

### **Backend: ResponseBody PII Redaction (CA-4)**
Para la salida (Outbound Data Leak Prevention), implementar extensión en `web/interceptor` sobreescribiendo el `ResponseBodyAdvice`:

```java
@ControllerAdvice
public class OutboundPiiMaskingAdvice implements org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice<Object> {
    
    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return true; 
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, 
                                  Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        if (body == null) return null;
        try {
            // Ejemplo Base de esterilizacion CA-4
            String bodyStr = body.toString(); // O serializado con ObjectMapper si es DTO
            String masked = bodyStr.replaceAll("(\\d{3}-\\d{2}-\\d{4}|\\d{16})", "[CONFIDENCIAL - CLASE PII]");
            // Retornar objeto limpio remapeado (Se requiere ObjectMapper y Jackson para parseo efectivo)
            // (El agente Backend es responsable de la correcta re-serialización del object tree)
            // Nota de handoff: Aplicarlo solo a Strings o respuestas en crudo para evitar romper objetos complejos,
            // o crear anotaciones custom @PiiProtected en los DTOs.
        } catch(Exception ignored) {}
        return body;
    }
}
```

### **Frontend: Erradicar `alert()` (CA-1, CA-3)**
En `apiClient.ts`, eliminar `alert()` de la intercepción de response y utilizar stores globales (Pinia) para centralización del colapso:

```typescript
// frontend/src/services/apiClient.ts
// Reemplazar la línea de alerta en Error Network / Error 500
// CA-21 & CA-1: Alertas Rojas Imborrables a través de Vue Store
if (error.response && [500, 502, 503, 504].includes(error.response.status)) {
    console.error('Fatal Level 0 Dispatching');
    const event = new CustomEvent('global-error-dispatch', { detail: { 
        code: error.response.status,
        message: `Colapso del Servidor / Integración Cíclica`
    }});
    window.dispatchEvent(event);
    return Promise.reject(error);
}

// Interceptar CA-3
if (error.response && error.response.status === 409) {
    if(error.response.data?.type?.includes("optimistic-lock")) {
        console.warn('Bloqueo de Concurrencia UI Disparado');
        const event = new CustomEvent('optimistic-lock-dispatch');
        window.dispatchEvent(event);
    }
}
```
*(El agente Frontend debe crear el listener en App.vue o ErrorStateGlobal para capturar estos CustomEvents e invalidar la pantalla).*

---

## 📌 5. Matriz de QA y Testing Atómico

Dirigido al equipo y suite de Pruebas Automáticas (TDD Unit y E2E):

| Test Name | CA Evaluado | Aserción Esperada por QA |
| :--- | :--- | :--- |
| `test_Api_Returns400_WithValidationStructure` | CA-2 | El JSON Body de error (Mapeo RFC) retorna un property `errors` que es Array de `{field, issue}` y no un string plano. |
| `test_Concurrent_Updates_Yields_409` | CA-3 | Insertar Entity(Version 1), alterar Version a 2 simultaneo, forzar Exception y afirmar retorno de HttpStatus 409 desde el controlador. |
| `test_UncaughtExceptions_LogToELK_MaskStackTrace` | CA-1 | `ProblemDetail.getDetail()` **no contiene** referencias de líneas de java, null pointers o stacktraces. Es un texto estéril de aviso para el cliente. |
| `test_Frontend_Interceptor_EmitsCustomEvent` | CA-1 | Mock Axios Error 500 -> Garantizar que `apiClient` dispara un EventTarget ("global-error-dispatch") hacia Vue y no usa `alert()`. |

---

## 📌 6. Despacho (Action Directives)

Para ejecutar este Handoff, instruirse a cada Agente con las siguientes rutinas de calidad irrompibles:

**PARA EL AGENTE BACKEND:**
> "Compilación obligatoria: Ejecuta el protocolo Zero-Trust SRE documentado en `.agents/skills/backend_sre_compilation_audit/SKILL.md` (§0 a §2). Si hay bloqueos de infraestructura, aplica el protocolo de reporte 3B."

**PARA EL AGENTE FRONTEND:**
> "Build obligatorio: Ejecuta el protocolo Zero-Trust UI documentado en `.agents/skills/frontend_build_audit/SKILL.md`."

**PARA EL AGENTE QA:**
> "Certificación obligatoria: Extender las suites en vitest (`*.spec.ts`) y JUnit Testcontainers para asegurar el pase absoluto de las pruebas referenciadas en la matriz de QA del Handoff."
