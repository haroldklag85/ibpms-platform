# Runbook: Diagnóstico de Fallos de Autenticación en iBPMS

> **Autor:** Arquitecto Líder AI  
> **Fecha:** 2026-04-20  
> **Sprint:** 6.2  
> **Estado:** Vigente  
> **Origen:** Incidente "Integración Cíclica 500" — Sprint 6.2  

---

## 1. Propósito

Este runbook establece el **procedimiento estándar de diagnóstico** cuando un usuario o agente reporta un fallo de login en cualquier entorno de la plataforma iBPMS. Su objetivo es reducir el tiempo medio de resolución (MTTR) de incidentes de autenticación, evitando las trampas de diagnóstico documentadas en el incidente del Sprint 6.2.

> [!IMPORTANT]
> **Lectura obligatoria** antes de escalar cualquier ticket de "Error 500 en Login" o "Integración Cíclica".

---

## 2. Árbol de Decisión de Diagnóstico

```
¿Hay error de login?
│
├── Paso 1: ¿El contenedor backend está UP?
│   │
│   │   docker ps --filter "name=ibpms-core-dev" --format "{{.Status}}"
│   │
│   ├── [Restarting / Exited] ──► IR A §3 (Fallo de Arranque)
│   │
│   └── [Up X minutes] ──► Paso 2
│
├── Paso 2: ¿El backend responde al health endpoint?
│   │
│   │   Invoke-RestMethod -Uri "http://localhost:8080/actuator/health"
│   │
│   ├── [Error / Timeout] ──► El backend arrancó pero JPA crasheó.
│   │                          IR A §3 (Fallo de Arranque)
│   │
│   └── [{"status":"UP"}] ──► Paso 3
│
├── Paso 3: ¿El endpoint de auth responde directamente? (sin proxy Vite)
│   │
│   │   Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/emergency-login" `
│   │     -Method Post -ContentType "application/json" `
│   │     -Body '{"email":"root@ibpms.local","password":"test"}'
│   │
│   ├── [401 Unauthorized] ──► Backend OK. Credenciales inválidas.
│   │                          IR A §4 (Validación de Credenciales)
│   │
│   ├── [200 + JWT Token] ──► Backend OK. Auth funciona.
│   │                          Si el frontend sigue mostrando error:
│   │                          IR A §5 (Proxy Vite / Mock Adapter)
│   │
│   └── [500 Internal] ──► Error real en el controlador de auth.
│                           IR A §6 (Error en el Controlador)
│
├── Paso 4: IR A §4 (Validación de Credenciales)
│
└── Paso 5: IR A §5 (Proxy / Mock)
```

---

## 3. Fallo de Arranque del Backend

### 3.1 Diagnóstico

```powershell
# Ver los últimos logs del contenedor
docker logs ibpms-core-dev --tail 100

# Buscar patrones conocidos de fallo
docker logs ibpms-core-dev --tail 200 | Select-String "BUILD FAILURE|AnnotationException|UnsatisfiedDependency|BeanCreationException"
```

### 3.2 Errores Conocidos y Resolución

| Patrón en Logs | Causa | Resolución |
|----------------|-------|------------|
| `AnnotationException: targets an unknown entity` | Bytecode huérfano de entidad JPA renombrada/eliminada | `docker exec ibpms-core-dev mvn clean compile -DskipTests` + restart |
| `DuplicateMappingException` | Dos clases `.class` con el mismo `@Entity(name=...)` | Purgar `target/` y verificar que no haya duplicados de Entity name |
| `UnsatisfiedDependencyException` | Bean no encontrado (Repository o Service mal configurado) | Verificar `@ComponentScan`, `@EntityScan` paths en `Application.java` |
| `BUILD FAILURE` + `javac` errors | Error de compilación en código Java | Consultar la línea exacta del error en la salida de Maven |
| `Connection refused` a PostgreSQL | PostgreSQL no arrancó antes que el backend | `docker-compose restart ibpms-core` (wait for healthcheck) |

### 3.3 Procedimiento de Limpieza Radical

Cuando ningún diagnóstico individual funciona:

```powershell
# 1. Detener todo
docker-compose down

# 2. Purgar bytecode huérfano del host
Remove-Item -Recurse -Force ".\backend\ibpms-core\target" -ErrorAction SilentlyContinue

# 3. Levantar infraestructura primero
docker-compose up -d ibpms-postgres ibpms-rabbitmq ibpms-redis

# 4. Esperar healthchecks (30 segundos)
Start-Sleep -Seconds 30

# 5. Levantar backend con compilación limpia
docker-compose up -d ibpms-core

# 6. Monitorear arranque
docker logs -f ibpms-core-dev
# Esperar "Started Application in X.XXX seconds"
```

---

## 4. Validación de Credenciales

### 4.1 Usuarios Registrados en UAT

```powershell
# Consultar la base de datos directamente
docker exec ibpms-postgres-uat psql -U ibpms_user -d ibpms_db -c "SELECT email, is_active FROM ibpms_security_user;"
```

**Estado actual conocido (2026-04-20):**

| Email | is_active | Notas |
|-------|-----------|-------|
| `root@ibpms.local` | `true` | Único usuario registrado en UAT |

> [!WARNING]
> Si se intenta login con emails como `admin@empresa.com` o `admin.local@empresa.com`, el backend responderá **401** correctamente porque esos usuarios **no existen en la base de datos**.

### 4.2 Crear un Usuario de Prueba

```powershell
docker exec ibpms-postgres-uat psql -U ibpms_user -d ibpms_db -c "
  INSERT INTO ibpms_security_user (id, email, username, password_hash, is_active, created_at)
  VALUES (
    gen_random_uuid(),
    'admin@ibpms.local',
    'admin',
    -- BCrypt hash de 'admin123'
    '\$2a\$10\$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    true,
    NOW()
  ) ON CONFLICT (email) DO NOTHING;
"
```

---

## 5. Proxy Vite / Mock Adapter

### 5.1 Verificar que el Proxy Apunte al Backend Correcto

```typescript
// vite.config.ts — Configuración actual
server: {
    proxy: {
        '/api': {
            target: 'http://127.0.0.1:8080',  // ← Debe coincidir con el puerto del backend
            changeOrigin: true,
            secure: false,
        },
    },
},
```

### 5.2 Verificar que el Mock Adapter NO Intercepte Auth

El archivo `mockAdapter.ts` **debe** tener passthrough para rutas de autenticación:

```typescript
// mockAdapter.ts — Línea 414 (CRÍTICO)
mock.onPost('/auth/emergency-login').passThrough();
mock.onAny().passThrough();  // Fallback para rutas no mockeadas
```

> [!CAUTION]
> Si estas líneas se eliminan o se mueven arriba de otro `mock.onPost(...)` que capture la ruta, el mock interceptará la petición y la auth real nunca se ejecutará.

### 5.3 Detección del Error de Enmascaramiento (Falso 500)

Si el backend responde correctamente pero el frontend muestra "Error 500":

1. Abrir DevTools del navegador (`F12`)
2. Ir a pestaña **Network**
3. Filtrar por `emergency-login`
4. Verificar el **código HTTP real** (puede ser 502 o 504, no 500)
5. Si es 502/504: el proxy no puede conectar con el backend → verificar Docker

---

## 6. Error Real en el Controlador de Auth

Si el endpoint `POST /api/v1/auth/emergency-login` responde **500** directamente (sin proxy):

```powershell
# 1. Obtener el stacktrace completo
docker logs ibpms-core-dev --tail 200 | Select-String "emergency-login" -Context 0,30

# 2. Verificar el X-Correlation-ID del response header para correlacionar con logs
```

### Errores Conocidos en el Controlador

| Patrón | Causa | Resolución |
|--------|-------|------------|
| `NullPointerException` en `EmergencyAuthController` | Campo `email` o `password` null en el body JSON | Verificar que el Content-Type sea `application/json` y el body sea válido |
| `BCryptPasswordEncoder` hash mismatch | La contraseña en DB no coincide con la que se envía | Verificar el hash BCrypt almacenado vs. el password enviado |
| `JPA connection pool exhausted` | Demasiadas conexiones activas | Reiniciar el contenedor backend |

---

## 7. Puertos y Entornos de Referencia

| Entorno | Puerto Frontend | Puerto Backend | URL de Prueba |
|---------|----------------|---------------|---------------|
| **DEV** | 5173 | 8080 | `http://localhost:5173/login?emergency=true` |
| **E2E** | 5174 | 8080 (mismo backend) | `http://localhost:5174/login?emergency=true` |

> [!NOTE]
> Ambos entornos frontend (DEV y E2E) apuntan al **mismo backend** en el puerto 8080. La diferencia es solo la instancia de Vite que los sirve. Si el backend cae, ambos entornos fallan simultáneamente.

---

## 8. Historial de Incidentes

| Fecha | Síntoma Reportado | Causa Real | MTTR | Referencia |
|-------|-------------------|------------|------|------------|
| 2026-04-20 | "Error 500 Integración Cíclica" | Bytecode huérfano JPA + enmascaramiento frontend | ~5h | [walkthrough](./walkthrough_cyclic_integration_500.md) |
