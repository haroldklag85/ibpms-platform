# ⚙️ Handoff Backend — Cierre Sprint 6.2 (P1)

> Emisor: Arquitecto Líder | Fecha: 2026-04-20 | Ejecutar: EN PARALELO con Frontend

## Diagnóstico
El Frontend tenía la llamada a `POST /api/v1/auth/emergency-login` COMENTADA. Ahora la va a descomentar. Necesitamos asegurar que el endpoint existe y retorna un JWT real.

## TAREA ÚNICA: Verificar/Crear endpoint emergency-login

1. Confirma que el endpoint `POST /api/v1/auth/emergency-login` existe
2. Confirma que acepta `{ email: String, password: String }`
3. Confirma que valida contra BCrypt usando PasswordEncoder
4. Confirma que retorna `{ token: "JWT..." }` con claims:
   - `sub` = userId (ej: `analista_n1`)
   - `roles` = `["ROLE_OPERARIO"]` (array de strings)
   - `tenant_id` = `"tenant_alpha"`
5. SecurityConfig.java L69 ya tiene `.requestMatchers(HttpMethod.POST, "/api/v1/auth/emergency-login").permitAll()`

### Test de Verificación:
```bash
curl -X POST http://localhost:8080/api/v1/auth/emergency-login \
  -H "Content-Type: application/json" \
  -d '{"email":"analista_n1@alpha.com","password":"Test123!"}'
```

Debe retornar HTTP 200 con JWT decodificable que contenga los claims mencionados.

## VALIDACIÓN
- Commit: `fix(backend): verify emergency-login returns real JWT with claims`
- Push: `sprint-6/uat-certification`
