# 🔬 Análisis Quirúrgico del Entorno Local — iBPMS Platform

> **Fuente:** Lectura directa de `pom.xml`, `package.json`, `docker-compose.yml`, `Dockerfile`, `requirements.txt`, `application.yml` y archivos de configuración del proyecto. Zero suposiciones.

---

## 📐 Arquitectura General

El proyecto está compuesto por **4 capas independientes** que deben ejecutarse juntas:

| Capa | Ruta | Tecnología |
|------|------|------------|
| **Backend Core** | `backend/ibpms-core` | Java 17 + Spring Boot 3.2.3 |
| **Motor DMN** | `backend/ibpms-dmn-engine` | Java 17 + Camunda DMN Engine 7.20.0 |
| **Frontend** | `frontend/` | Node.js + Vue 3 + Vite |
| **RPA Scraper** | `rpa/judicial-scraper` | Python 3.11 |
| **Infraestructura** | `docker-compose.yml` | Docker + PostgreSQL + RabbitMQ + Redis |

---

## 🧰 COMPONENTES A INSTALAR (Listado Definitivo)

### 1. ☕ Java Development Kit (JDK) 17

**Evidencia exacta en el código:**
- `backend/pom.xml` línea 23: `<java.version>17</java.version>`
- `backend/ibpms-core/pom.xml` línea 17: `<java.version>17</java.version>`
- `backend/ibpms-core/Dockerfile` línea 4: `FROM maven:3.9-eclipse-temurin-17 AS builder`
- `backend/ibpms-core/Dockerfile` línea 18: `FROM eclipse-temurin:17-jre-alpine`

**Versión requerida:** `Java 17` (LTS). La distribución recomendada es **Eclipse Temurin 17** (la misma que usan los Dockerfiles).

---

### 2. 📦 Apache Maven 3.9.x

**Evidencia exacta en el código:**
- `docker-compose.yml` línea 64: `image: maven:3.9.6-eclipse-temurin-21`
- `backend/ibpms-core/Dockerfile` línea 4: `FROM maven:3.9-eclipse-temurin-17 AS builder`
- `Makefile` línea 37: `cd backend/ibpms-core && ./mvnw verify`

**Versión requerida:** `Maven 3.9.x` (mínimo 3.9.0). Se puede usar el Maven Wrapper (`mvnw`) incluido en el proyecto.

---

### 3. 🟢 Node.js (con npm)

**Evidencia exacta en el código:**
- `frontend/package.json`: `"vite": "^5.1.4"` → Vite 5 requiere Node.js ≥ 18
- `frontend/package.json`: `"@types/node": "^20.11.20"` → Tipos de Node 20 declarados explícitamente
- `frontend/tsconfig.json`: `"target": "ES2020"`, `"module": "ESNext"`

**Versión requerida:** `Node.js 20.x LTS` (la versión de tipos `@types/node ^20.x` lo confirma). npm se instala junto con Node.js.

---

### 4. 🐳 Docker Desktop (con Docker Compose)

**Evidencia exacta en el código:**
- `docker-compose.yml` existe en la raíz con 4 servicios definidos
- `Makefile` línea 4: `DOCKER_COMPOSE = docker compose`
- `setup_local.sh` línea 22-24: detecta `docker-compose` o `docker compose`

**Componentes que Docker levanta automáticamente:**

| Servicio | Imagen Docker | Puerto |
|----------|--------------|--------|
| PostgreSQL + pgVector | `ankane/pgvector:latest` | `5432` |
| RabbitMQ + Management UI | `rabbitmq:3-management` | `5672`, `15672` |
| Redis | `redis:7-alpine` | `6379` |
| Backend Spring Boot (opcional vía Docker) | `maven:3.9.6-eclipse-temurin-21` | `8080` |

**Versión requerida:** Docker Desktop para Windows con soporte de `docker compose` (v2+). **No se necesita instalar PostgreSQL, RabbitMQ ni Redis de forma nativa** — Docker los gestiona.

---

### 5. 🐘 PostgreSQL con extensión pgVector

**Evidencia exacta en el código:**
- `docker-compose.yml` línea 7: `image: ankane/pgvector:latest`
- `application.yml` línea 9: `url: jdbc:postgresql://${POSTGRES_HOST:localhost}:5432/${POSTGRES_DB:ibpms_db}`
- `application.yml` línea 12: `driver-class-name: org.postgresql.Driver`
- `application.yml` línea 18: `dialect: org.hibernate.dialect.PostgreSQLDialect`
- `application-uat.yml` línea 38: `type: postgres`
- `ibpms-core/pom.xml` línea 22-25: dependencia `pgvector 0.1.5`

**Credenciales configuradas (valores por defecto):**
```
DB:       ibpms_db
Usuario:  ibpms_user
Password: ibpms_password
Puerto:   5432
```

> ⚠️ **CRÍTICO:** El driver configurado es `org.postgresql.Driver` (PostgreSQL puro). El `pom.xml` también tiene `mysql-connector-j` pero **no se usa en `application.yml` principal** — el datasource activo apunta a PostgreSQL.

---

### 6. 🐇 RabbitMQ

**Evidencia exacta en el código:**
- `docker-compose.yml` líneas 26-42: imagen `rabbitmq:3-management`
- `application.yml` líneas 24-28: host/port/user/pass configurados
- `ibpms-core/pom.xml` línea 72: `spring-boot-starter-amqp`

**Credenciales:**
```
Host:     localhost
Puerto:   5672 (AMQP) / 15672 (Admin UI)
Usuario:  guest
Password: guest
```

---

### 7. 🔴 Redis 7

**Evidencia exacta en el código:**
- `docker-compose.yml` línea 47: `image: redis:7-alpine`
- `application.yml` líneas 30-32: `host: localhost`, `port: 6379`
- `ibpms-core/pom.xml` línea 67: `spring-boot-starter-data-redis`

---

### 8. 🐍 Python 3.11 (Solo para el módulo RPA)

**Evidencia exacta en el código:**
- `rpa/judicial-scraper/Dockerfile` línea 2: `FROM python:3.11-slim`
- `rpa/judicial-scraper/requirements.txt`:
  ```
  requests==2.31.0
  beautifulsoup4==4.12.3
  ```

**Versión requerida:** `Python 3.11.x`. El módulo RPA es independiente del resto del stack. Solo es necesario si se usa el scraper judicial.

---

## 🌐 Puertos en uso

| Puerto | Servicio |
|--------|----------|
| `8080` | Backend Spring Boot |
| `5432` | PostgreSQL |
| `5672` | RabbitMQ AMQP |
| `15672` | RabbitMQ Management UI |
| `6379` | Redis |
| `5173` | Frontend Vite (dev server) |

---

## 🗄️ Stack Técnico Detallado

### Backend
| Tecnología | Versión Exacta |
|------------|----------------|
| Spring Boot | `3.2.3` |
| Java | `17` |
| Maven | `3.9.x` |
| Camunda BPM (Spring Boot Starter) | `7.21.0` |
| Camunda DMN Engine | `7.20.0` |
| PostgreSQL Driver | Managed by Spring Boot 3.2.3 |
| pgvector Java | `0.1.5` |
| Liquibase | Managed by Spring Boot 3.2.3 |
| JJWT (JWT) | `0.12.5` |
| MapStruct | `1.5.5.Final` |
| Lombok | `1.18.30` |
| BouncyCastle | `1.78` |
| Spring Security + OAuth2 Resource Server | Managed by Spring Boot |
| Spring WebFlux (WebClient) | Managed by Spring Boot |
| Azure Storage Blob SDK | `12.25.1` |
| SpringDoc OpenAPI (Swagger) | `2.5.0` |
| Resilience4j Circuit Breaker | `3.1.1` |
| Bucket4j Rate Limiter | `8.9.0` |
| Javers Audit | `7.3.7` |
| Apache PDFBox | `3.0.1` |
| OpenPDF | `1.3.36` |
| Testcontainers BOM | `1.20.4` |
| JaCoCo | `0.8.11` |

### Frontend
| Tecnología | Versión Exacta |
|------------|----------------|
| Vue.js | `^3.4.19` |
| Vite | `^5.1.4` |
| TypeScript | `^5.2.2` |
| Vue Router | `^4.2.5` |
| Pinia (State) | `^2.1.7` |
| TailwindCSS | `^3.4.1` |
| Axios | `^1.6.7` |
| BPMN.js | `^18.12.1` |
| Monaco Editor | `^0.55.1` |
| STOMP.js | `^7.3.0` |
| SockJS Client | `^1.6.1` |
| Zod | `^3.22.4` |
| VueUse | `^14.2.1` |
| Playwright | `^1.59.1` |
| Vitest | `^1.4.0` |

### Infraestructura (vía Docker)
| Tecnología | Versión Exacta |
|------------|----------------|
| PostgreSQL + pgVector | `ankane/pgvector:latest` |
| RabbitMQ + Management | `rabbitmq:3-management` |
| Redis | `redis:7-alpine` |

---

## 📋 PASO A PASO DE INSTALACIÓN EN WINDOWS

### PASO 1 — Instalar JDK 17 (Eclipse Temurin)

1. Ir a: https://adoptium.net/temurin/releases/?version=17
2. Descargar: `Windows x64 JDK .msi`
3. Ejecutar el instalador. Marcar la opción **"Set JAVA_HOME variable"**
4. Verificar en PowerShell:
   ```powershell
   java -version
   # Debe mostrar: openjdk version "17.x.x"
   ```

---

### PASO 2 — Instalar Apache Maven 3.9.x

1. Ir a: https://maven.apache.org/download.cgi
2. Descargar: `apache-maven-3.9.x-bin.zip`
3. Descomprimir en `C:\tools\maven\`
4. Agregar a variables de entorno:
   - `MAVEN_HOME` = `C:\tools\maven\apache-maven-3.9.x`
   - Agregar `%MAVEN_HOME%\bin` al `PATH`
5. Verificar:
   ```powershell
   mvn -version
   # Debe mostrar: Apache Maven 3.9.x
   ```

> **Alternativa:** El proyecto tiene Maven Wrapper (`./mvnw`). Si usas la terminal Git Bash/WSL, puedes usar `./mvnw` directamente sin instalar Maven globalmente.

---

### PASO 3 — Instalar Node.js 20 LTS

1. Ir a: https://nodejs.org/en/download/
2. Descargar: `Node.js 20.x LTS` para Windows (instalador `.msi`)
3. Ejecutar el instalador (incluye npm automáticamente)
4. Verificar:
   ```powershell
   node -version   # Debe mostrar v20.x.x
   npm -version    # Debe mostrar 10.x.x
   ```

---

### PASO 4 — Instalar Docker Desktop

1. Ir a: https://www.docker.com/products/docker-desktop/
2. Descargar e instalar `Docker Desktop for Windows`
3. Al abrir Docker Desktop, asegurarse de que esté corriendo (ícono en barra de tareas)
4. Verificar:
   ```powershell
   docker -version
   docker compose version
   ```

---

### PASO 5 — Instalar Python 3.11 (Solo si vas a usar el módulo RPA)

1. Ir a: https://www.python.org/downloads/release/python-3110/
2. Descargar: `Windows installer (64-bit)`
3. En el instalador, marcar **"Add Python to PATH"**
4. Verificar:
   ```powershell
   python --version   # Debe mostrar Python 3.11.x
   pip --version
   ```

---

## 🚀 CÓMO LEVANTAR EL PROYECTO EN LOCAL

### A. Levantar la Infraestructura (PostgreSQL + RabbitMQ + Redis)

```powershell
cd "c:\Users\USER\Desktop\Proyectos\Harold Ibpms\ibpms-platform"
docker compose up -d
```

Verificar que los 3 servicios estén `healthy`:
```powershell
docker compose ps
```

---

### B. Levantar el Backend (Spring Boot)

```powershell
cd "c:\Users\USER\Desktop\Proyectos\Harold Ibpms\ibpms-platform\backend\ibpms-core"
mvn spring-boot:run
```

O usando el Maven Wrapper:
```powershell
./mvnw spring-boot:run
```

El backend levanta en: `http://localhost:8080/api/v1`  
Swagger UI disponible en: `http://localhost:8080/api/v1/swagger-ui/index.html`

---

### C. Levantar el Frontend (Vue 3 + Vite)

```powershell
cd "c:\Users\USER\Desktop\Proyectos\Harold Ibpms\ibpms-platform\frontend"
npm install
npm run dev
```

El frontend levanta en: `http://localhost:5173`  
El proxy de Vite redirige `/api` → `http://localhost:8080` automáticamente.

---

### D. Levantar el RPA Scraper (Opcional)

```powershell
cd "c:\Users\USER\Desktop\Proyectos\Harold Ibpms\ibpms-platform\rpa\judicial-scraper"
pip install -r requirements.txt
python scraper.py
```

---

## ⚠️ Notas Críticas Detectadas

1. **MySQL vs PostgreSQL:** El `setup_local.sh` menciona MySQL (puerto 3306) pero el `docker-compose.yml` y `application.yml` actuales usan **PostgreSQL (puerto 5432)**. El script de setup está desactualizado. **Usar Docker Compose directamente.**

2. **Azurite (Azure Storage emulado):** El `nginx.conf` tiene un proxy a `azurite:10000` y el `application.yml` usa una connection string de Azurite. Para emular Azure Blob Storage localmente, se puede instalar Azurite:
   ```powershell
   npm install -g azurite
   azurite --loose
   ```

3. **ClamAV:** El `application.yml` tiene `ibpms.clamav.url: http://localhost:3310`. Si alguna funcionalidad de escaneo de virus se activa, se necesitaría ClamAV corriendo. Para desarrollo local no es bloqueante.

4. **Maven Compile Order:** El módulo `ibpms-core` depende de `ibpms-dmn-engine`. Se debe compilar desde la carpeta padre `backend/` la primera vez:
   ```powershell
   cd "c:\Users\USER\Desktop\Proyectos\Harold Ibpms\ibpms-platform\backend"
   mvn clean install -DskipTests
   ```

---

## ✅ Checklist de Instalación

- [ ] JDK 17 instalado y `JAVA_HOME` configurado
- [ ] Apache Maven 3.9.x instalado y en `PATH`
- [ ] Node.js 20.x LTS instalado
- [ ] Docker Desktop instalado y corriendo
- [ ] `docker compose up -d` ejecutado (PostgreSQL + RabbitMQ + Redis levantados)
- [ ] Backend compilado con `mvn clean install -DskipTests` desde `backend/`
- [ ] Backend corriendo en puerto `8080`
- [ ] `npm install` ejecutado en `frontend/`
- [ ] Frontend corriendo en puerto `5173`
- [ ] *(Opcional)* Python 3.11 + pip instalados para módulo RPA
