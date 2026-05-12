# Workflow: Habilitación de Entorno para Pruebas QA Manuales (Human Testing)

**Objetivo:** Estandarizar la preparación del ambiente de pruebas para QA manual o sesiones exploratorias. Este protocolo garantiza que el equipo evalúe el sistema real (Zero-Mock), bajo un entorno estable que preserve la persistencia de datos a lo largo del tiempo para flujos de prueba complejos.

---

## 🟩 Fase 1: Levantamiento de Infraestructura y Preservación de Datos

A diferencia de los entornos automatizados, el entorno de QA Manual **NO DEBE ser destruido** entre sesiones. Esto permite a los testers retomar tareas pausadas, revisar expedientes en Workdesk de días anteriores y simular escenarios de larga duración.

1. **Levantar Servicios Base (Sin Destrucción):**
   * **Comando:**
     ```bash
     docker compose up -d ibpms-postgres ibpms-rabbitmq ibpms-redis
     ```
   * **Resultado Esperado:** Contenedores en estado `Up` y `(healthy)`.
   * **⚠️ Regla Estricta:** NUNCA ejecutar comandos como `docker compose down -v` o `rm -f ibpms-postgres` a menos que el equipo de Arquitectura autorice un *Hard Reset*.

2. **Arrancar el Backend Core:**
   * **Comando:**
     ```bash
     docker compose up -d ibpms-core
     ```
   * **Resultado Esperado:** Tras ejecutar `docker logs ibpms-core-dev --tail 100`, debe visualizarse `Started Application in X seconds`. Liquibase se encargará de inyectar las tablas y sembrar datos si es la primera vez, y los preservará en ejecuciones futuras.

3. **Gobernanza ante Caídas (Escalamiento Inmediato):**
   * **Protocolo:** Si al revisar los logs del Backend se detecta un `BUILD FAILURE` o el contenedor se detiene inesperadamente, el tester **NO debe intentar reparar el entorno** (ej. borrando cachés). 
   * **Acción:** Escalar la falla de infraestructura inmediatamente al equipo de desarrollo para no contaminar el diagnóstico del problema.

## 🟦 Fase 2: Perfiles de Autenticación Pre-cargados (RBAC)

Gracias al *Data Seeding* de Liquibase, la base de datos se inicializa con perfiles básicos para cubrir los principales roles de acceso sin necesidad de crearlos manualmente.

**Matriz Oficial de Cuentas de Prueba (RBAC):**
| Rol del Sistema | Credencial (Email) | Contraseña Estándar |
| :--- | :--- | :--- |
| **Analista N1** (Ejecutor) | `analista1@ibpms.local` | `admin123` |
| **Analista N2** (Revisor) | `analista2@ibpms.local` | `admin123` |
| **Administrador** (Config) | `admin@ibpms.local` | `admin123` |
| **Súper Admin** (Break-Glass)| `root@ibpms.local` | `admin123` |
*(Nota: Validar si existen otros usuarios en el archivo `users-seed.sql` dependiendo del Sprint activo).*

## 🟧 Fase 3: Frontend y Validación Zero-Mock

Esta fase garantiza que el QA visualiza el sistema bajo los estándares estrictos de cero emuladores, conectándose fielmente al Spring Boot.

1. **Certificación Anti-Mocks por Consola:**
   * **Directorio:** `cd frontend`
   * **Comando:**
     ```bash
     npm run lint:mocks
     ```
   * **Resultado Esperado:** `0 vulnerabilidades`. Todo el código cumple el ADR-010. No se requiere inspección adicional en el panel *Network* del navegador.

2. **Verificar Proxy hacia el Backend Real:**
   * **Acción:** Revisar que el archivo `frontend/vite.config.ts` contenga `proxy: { '/api': { target: 'http://127.0.0.1:8080' } }`. (No se necesita un archivo `.env` local).

3. **Despliegue del Frontend (Modo Desarrollo):**
   * **Acción:** Para las pruebas manuales se utiliza el modo de desarrollo, ya que permite al QA interactuar, detectar errores visuales en consola e inspeccionar componentes fácilmente.
   * **Comando:**
     ```bash
     npm run dev
     ```
   * **Resultado Esperado:** El terminal indicará que el sitio está disponible (usualmente en `http://localhost:5173`). El tester debe abrir este enlace en su navegador e iniciar la certificación usando la Matriz de Autenticación de la Fase 2.

---
> **Fin del Workflow.** Una vez terminada la sesión, el QA simplemente puede cerrar la terminal del frontend. Los datos en PostgreSQL persistirán para la sesión de mañana de manera automática.
