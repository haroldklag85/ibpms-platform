# Workflow: Arranque y Remediación del Servidor Spring Boot (Perfil E2E Estático)

**Propósito:** Este workflow automatiza la remediación, compilación y levantamiento del backend `ibpms-core` bajo el entorno estático E2E (ADR-010), previniendo errores de caché y bloqueos de puertos. 

> [!TIP]
> Todo este flujo ha sido paquetizado en el script `C:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform\start-backend-e2e.bat` para ejecución directa en 1 clic.

## Pasos del Flujo

### 1. Limpieza de puertos (`taskkill`)
Asegura que no existan procesos Java ni Node huérfanos bloqueando el puerto 8080 (Backend) o el 5173 (Vite/Playwright). Evita el error `Address already in use` o bloqueos de memoria limitantes detectados históricamente.
```cmd
taskkill /F /IM java.exe
taskkill /F /IM node.exe
```

### 2. Limpieza de Caché y Recompilación (`mvn clean`)
Limpia la carpeta `target` para evitar problemas de caché de clases (ej. `ClassNotFoundException` para `SagaCompensationException`) originados al cambiar de implementaciones sin recompilar.
```cmd
cd backend\ibpms-core
..\..\maven\apache-maven-3.9.6\bin\mvn.cmd clean
```

### 3. Arranque con Perfil E2E (`mvn spring-boot:run`)
Levanta el servidor inyectando el perfil estático `e2e` para conectarse a los puertos expuestos por `docker-compose.e2e.yml` (Postgres `localhost:5433` y RabbitMQ `localhost:5673`).
```cmd
..\..\maven\apache-maven-3.9.6\bin\mvn.cmd spring-boot:run -Dspring-boot.run.profiles=e2e
```

### 4. Validación de Salud (`curl /actuator/health`)
Comprueba que el endpoint Actuator devuelva un estado `UP` indicando que el servidor completó el inicio.
```cmd
curl.exe -s http://localhost:8080/actuator/health
```
