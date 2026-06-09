# Handoff Backend — Feedback Diferenciado en Emergency Login

> **Emisor:** Arquitecto Líder  
> **Receptor:** Agente Backend  
> **Iteración:** 83-DEV  
> **Sprint:** 6.2  
> **Prioridad:** Alta  
> **Origen:** Incidente UAT 2026-04-20 — Login sin feedback al usuario  

---

## 1. Contexto

Actualmente el método `emergencyLogin()` en `AuthSyncController.java` (líneas 82-124) devuelve el **mismo mensaje genérico** `"Credenciales Inválidas"` tanto cuando el usuario no existe en la base de datos como cuando la contraseña es incorrecta. Esto impide que el sistema comunique de manera diferenciada la causa del fallo, generando confusión operativa (el usuario no sabe si su cuenta no existe o si su contraseña es incorrecta).

### Estado Actual (Problemático)

```java
// Línea 93-94: Usuario no encontrado
if (userOpt.isEmpty()) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(Map.of("message", "Credenciales Inválidas"));  // ← Genérico
}

// Línea 99-100: Contraseña incorrecta
if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(Map.of("message", "Credenciales Inválidas"));  // ← Mismo mensaje
}
```

---

## 2. Requerimiento

Modificar el endpoint `POST /api/v1/auth/emergency-login` para que retorne **códigos de error semánticos diferenciados** en el cuerpo JSON de la respuesta, permitiendo al Frontend distinguir el motivo del fallo.

> [!IMPORTANT]  
> **Decisión de Seguridad (Arquitecto Líder):** En entornos de producción, este nivel de detalle podría considerarse una vulnerabilidad de enumeración de usuarios. Sin embargo, dado que el **Emergency Login es un protocolo Break-Glass restringido a IT** (no expuesto al público), y que la IP del solicitante ya se audita por protocolo CA-4, se autoriza el feedback diferenciado para maximizar la operatividad del equipo de soporte.

---

## 3. Contrato de Respuesta Requerido

### 3.1 Usuario No Encontrado

```http
POST /api/v1/auth/emergency-login
Content-Type: application/json

{ "email": "no-existe@alpha.com", "password": "cualquiera" }
```

**Respuesta esperada:**

```http
HTTP/1.1 401 Unauthorized
Content-Type: application/json

{
    "code": "USER_NOT_FOUND",
    "message": "No existe una cuenta asociada al correo proporcionado."
}
```

### 3.2 Contraseña Incorrecta

```http
POST /api/v1/auth/emergency-login
Content-Type: application/json

{ "email": "admin@alpha.com", "password": "incorrecta" }
```

**Respuesta esperada:**

```http
HTTP/1.1 401 Unauthorized
Content-Type: application/json

{
    "code": "INVALID_PASSWORD",
    "message": "La contraseña proporcionada es incorrecta."
}
```

### 3.3 Cuenta Deshabilitada (Ya existente — solo normalizar formato)

```http
HTTP/1.1 403 Forbidden
Content-Type: application/json

{
    "code": "ACCOUNT_DISABLED",
    "message": "La cuenta existe pero se encuentra deshabilitada. Contacte al administrador."
}
```

### 3.4 Login Exitoso (Sin cambios)

```http
HTTP/1.1 200 OK
Content-Type: application/json

{
    "token": "eyJhbGciOi...",
    "message": "Emergency login successful"
}
```

### 3.5 Campos Faltantes (Ya existente — solo normalizar formato)

```http
HTTP/1.1 400 Bad Request
Content-Type: application/json

{
    "code": "MISSING_FIELDS",
    "message": "Los campos 'email' y 'password' son obligatorios."
}
```

---

## 4. Archivo a Modificar

| Archivo | Ruta Completa |
|---------|---------------|
| `AuthSyncController.java` | `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/web/security/AuthSyncController.java` |

### 4.1 Cambios Requeridos en `emergencyLogin()` (Líneas 82-124)

Refactorizar los bloques de validación para incluir el campo `code` en cada respuesta de error:

```java
@PostMapping("/emergency-login")
public ResponseEntity<?> emergencyLogin(@RequestBody Map<String, String> creds) {
    String email = creds.get("email");
    String rawPassword = creds.get("password");

    if (email == null || rawPassword == null) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(Map.of("code", "MISSING_FIELDS", 
                         "message", "Los campos 'email' y 'password' son obligatorios."));
    }

    Optional<UserEntity> userOpt = userRepository.findByEmail(email);
    
    if (userOpt.isEmpty()) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Map.of("code", "USER_NOT_FOUND", 
                         "message", "No existe una cuenta asociada al correo proporcionado."));
    }

    UserEntity user = userOpt.get();

    if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Map.of("code", "INVALID_PASSWORD", 
                         "message", "La contraseña proporcionada es incorrecta."));
    }

    if (!user.getIsActive()) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(Map.of("code", "ACCOUNT_DISABLED", 
                         "message", "La cuenta existe pero se encuentra deshabilitada. Contacte al administrador."));
    }

    // ... resto del flujo de generación de JWT sin cambios ...
}
```

---

## 5. Consideraciones Técnicas

- **No romper el contrato existente.** El campo `message` debe seguir existiendo para retrocompatibilidad. El campo `code` es **nuevo y aditivo**.
- **El status HTTP se preserva:** `401` para user not found y password incorrecta, `403` para cuenta deshabilitada, `400` para campos faltantes.
- **Logging CA-53:** El `SensitiveDataLoggerAdvice` ya enmascara el campo `password` en logs. Verificar que los nuevos mensajes de error no filtren información sensible al log.

---

## 6. Criterios de Aceptación

| # | Criterio | Verificación |
|---|----------|-------------|
| CA-1 | El endpoint retorna `code: "USER_NOT_FOUND"` cuando el email no existe en `ibpms_security_user` | Test unitario |
| CA-2 | El endpoint retorna `code: "INVALID_PASSWORD"` cuando el email existe pero el hash BCrypt no coincide | Test unitario |
| CA-3 | El endpoint retorna `code: "ACCOUNT_DISABLED"` cuando `is_active = false` | Test unitario |
| CA-4 | El endpoint retorna `code: "MISSING_FIELDS"` cuando falta email o password en el body | Test unitario |
| CA-5 | El campo `message` contiene un texto legible en español para cada caso | Inspección |
| CA-6 | El status HTTP es `401` para CA-1 y CA-2, `403` para CA-3, `400` para CA-4 | Test integración |
