# 🚀 Guía Definitiva de Ejecución Local (iBPMS)

¡Bienvenido al nuevo paradigma de ejecución! Hemos transicionado la plataforma de un modelo estricto de _"Todo en Docker"_ a un modelo **Híbrido Host-First**.

¿Por qué? Porque ejecutar el código Java nativamente en tu máquina te ofrece una compilación casi instantánea, un consumo de memoria inferior y, lo más importante, la capacidad de poner _break-points_ de depuración (debugging) sin fricción en tu IDE. 

Solo usamos Docker para lo que Docker hace mejor: **Infraestructura Inmutable** (Postgres, RabbitMQ, Redis, Azurite).

---

## 📂 Directorio de Trabajo Obligatorio

El error número 1 al intentar lanzar los scripts es no estar en la carpeta correcta. 
Toda ejecución debe ocurrir obligatoriamente dentro del directorio principal del proyecto (`ibpms-platform`).

> [!WARNING]
> Si abres una terminal de PowerShell y ves `C:\Users\...\ProyectoAntigravity>`, **estás un nivel muy arriba**.
> 
> **La solución:** Ejecuta `cd ibpms-platform` antes de invocar cualquier comando.

---

## 🛠️ Los 3 Scripts Mágicos

Hemos empaquetado todo el ciclo de vida (Levantar Docker + Pausa Técnica + Invocar el Maven embebido del proyecto) en 3 simples archivos `.bat`.

### 1. Entorno de Desarrollo Local (DEV)
Úsalo cuando vayas a programar, depurar o hacer cambios al código en el día a día.

**Comando en Terminal:**
```powershell
.\start-dev.bat
```

**¿Qué hace por ti?**
1. Ejecuta `docker-compose up -d ibpms-postgres ibpms-rabbitmq ibpms-redis`.
2. Espera unos segundos a que las bases de datos respiren.
3. Toma la versión de Maven nativa que bajamos (`apache-maven-3.9.6`) e invoca `spring-boot:run` inyectando el perfil `dev`.

### 2. Entorno Aislado de Pruebas (E2E)
Úsalo exclusivamente cuando vayas a certificar una Historia de Usuario (como la validación del _ConnectionToast_ US017) o cuando Playwright lo requiera. Está separado para no ensuciar tu base de datos de desarrollo.

**Comando en Terminal:**
```powershell
.\start-e2e.bat
```

**¿Qué hace por ti?**
1. Ejecuta `docker-compose -f docker-compose.e2e.yml up -d` (Esto trae Camunda, Azurite y la BD en puertos desplazados como `:5433`).
2. Espera a que se asienten.
3. Invoca Maven con el perfil `e2e`.

### 3. Ejecución de Pruebas Puras (Tests)
Úsalo antes de hacer un `git push` para confirmar que no rompiste ningún contrato existente de Java.

**Comando en Terminal:**
```powershell
.\run-tests.bat
```
Simplemente invoca `mvn clean test` de forma limpia y consolidada.

---

## 🛑 Cómo Apagar Todo Correctamente

Cuando termines tu jornada, es vital liberar la memoria y apagar las conexiones.

**1. Apagar el Servidor Java:**
Ve a la consola donde ejecutaste el `.bat` y presiona la combinación:
`Ctrl + C` (Te preguntará si deseas terminar el trabajo por lotes, presiona `S` y luego `Enter`).

**2. Apagar la Infraestructura (Docker):**
Dependiendo del entorno que estuviste usando, ejecuta uno de los siguientes comandos en la raíz del proyecto (`ibpms-platform`):

*Si estabas en DEV:*
```powershell
docker-compose down
```

*Si estabas en E2E:*
```powershell
docker-compose -f docker-compose.e2e.yml down
```

---

## 🚑 Troubleshooting (Resolución de Problemas)

> [!NOTE]
> **Error:** `El término '.\start-e2e.bat' no se reconoce...`
> **Causa:** No estás en la carpeta `ibpms-platform`.
> **Solución:** Escribe `cd ibpms-platform` y vuelve a intentar.

> [!CAUTION]
> **Error:** `Web server failed to start. Port 8080 was already in use.`
> **Causa:** Ya tenías otro script corriendo o la JVM se quedó trabada en memoria.
> **Solución:** Abre PowerShell y ejecuta `taskkill /F /IM java.exe` para destruir forzosamente el proceso bloqueador. Luego intenta correr el script de nuevo.
