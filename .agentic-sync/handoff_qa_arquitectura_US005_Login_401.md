# 🤝 QA Handoff: Incidente de Autenticación (HTTP 401) en Break-Glass Login

**Fecha:** 2026-05-24
**Origen:** 🕵️ QA E2E (Misión 1 - Preparación de Entorno)
**Destino:** 🏗️ Arquitecto Líder (Backend / SecOps)
**Contexto:** Pruebas UAT / E2E de la US-005 (BPMN Modeler). Bloqueo de acceso al entorno local.

---

## 🚨 Descripción del Incidente
Durante la ejecución de las pruebas pre-flight para la certificación de la US-005, se identificó un bloqueo total de acceso en la capa de UI. El flujo de "SSO Federado" fue evitado utilizando el **Break-Glass Recovery (Acceso de Emergencia)** para loguear a un usuario Semilla. Aunque el bug inicial de enrutamiento duplicado en el Frontend (`/api/v1/api/v1/`) fue subsanado, la petición ahora está siendo interceptada y rechazada por la capa de seguridad del Backend (Spring Security) con un código `HTTP 401 Unauthorized`.

---

## 🔬 Metodología de Diagnóstico (Cómo se probó)

Se desplegó un **Sub-Agente Headless (Playwright)** configurado con monitoreo estricto de Red y Consola DOM para interactuar con la interfaz de Vite levantada en local, evadiendo falsos positivos de caché de navegador.

**Secuencia Ejecutada (Automation Flow):**
1. **Navegación:** `http://localhost:5173/` (Vite Dev Server).
2. **Acción UI:** Interacción con el toggle `[data-testid="break-glass-toggle"]` para desplegar el formulario de emergencia.
3. **Payload Inyectado:** 
   - `Email`: `admin@alpha.com`
   - `Password`: `admin123`
   - `Justificación`: `Testing emergency login error`
4. **Trigger:** Submit en el botón `<button>ACTIVAR ACCESO DE EMERGENCIA</button>`.

---

## 📡 Trazabilidad de Red (Puertos y Protocolos)

- **Frontend:** Ejecutándose en local mediante `npm run dev` (Vite) sobre el puerto `TCP/5173` vía HTTP.
- **Backend (Proxy):** Las llamadas son enviadas al Gateway/Proxy de Vite y re-enrutadas al contenedor Spring Boot local.
- **Endpoint Interceptado:** `POST http://localhost:5173/api/v1/auth/emergency/login`
- **Respuesta API:** `HTTP 401 Unauthorized`
- **Trazabilidad DOM (Vue/Axios):** 
  - `BROWSER CONSOLE: error - Failed to load resource: the server responded with a status of 401 (Unauthorized)`
  - `BROWSER CONSOLE: warning - CA-27: Emitiendo Soft-Lock por Expiración de Token en Backend`
  - `UI Toast Alert: ALERTA DEL SISTEMA: NIVEL 0`

---

## 🌱 Estado del Seed (Insumos de Datos)

Se verificó el script de datos de inicialización E2E en la ruta:
`backend/ibpms-core/target/classes/seed-e2e.sql`

La identidad probada se encuentra insertada correctamente en la tabla `ibpms_security_user`:
```sql
INSERT INTO ibpms_security_user (id, username, email, password_hash, status, is_external_idp, created_at) VALUES 
(gen_random_uuid(), 'admin', 'admin@alpha.com', '$2b$10$1OHQ9PUOg9z6LChpq2gtF.6lfkZww5rBsFXjtBA4YBwZkwHVlgmri', 'ACTIVE', false, CURRENT_TIMESTAMP)
```

**Hipótesis de Datos:**
El hash bcrypt insertado (`$2b$10$1OHQ9PUOg9z6LChpq2gtF.6lfkZww5rBsFXjtBA4YBwZkwHVlgmri`) corresponde de forma estándar al texto plano `admin123`. Sin embargo, la denegación sugiere una desalineación entre este hash y la configuración del `PasswordEncoder` en el contexto de Spring.

---

## 🛠️ Plan de Acción Solicitado al Arquitecto Líder

Para destrabar el E2E de la US-005, necesitamos que ejecutes las siguientes acciones correctivas:

1. **Inspección de Logs del Backend (CRÍTICO):**
   - Dirígete a la consola de tu contenedor Docker o terminal donde corre el proceso de `Spring Boot`.
   - Revisa el Stacktrace exacto generado en el instante del POST. Necesitamos identificar si la excepción proviene de:
     - `BadCredentialsException` (Fallo directo de contraseña/hash).
     - `DisabledException` / `LockedException` (Estado de cuenta).
     - `MissingCsrfTokenException` (Problemas de CSRF pre-auth).
     - Un filtro de CORS rechazando el pre-flight `OPTIONS`.
     - Falta de un Custom Header (Ej. `X-Tenant-ID`) en el endpoint `/emergency/login`.

2. **Verificar Configuración del Provider:**
   - Validar que el `DaoAuthenticationProvider` o el `EmergencyAuthenticationProvider` en el paquete de `security/` esté utilizando la misma fuerza de `salt rounds` (10) que los hashes inyectados en el `seed-e2e.sql`.

3. **Retorno de Handoff:**
   - Una vez ajustado el `application.yml` o el `SecurityConfig`, por favor recompila el backend y avísale al equipo de QA para re-lanzar el agente Playwright.
