---
title: "Handoff QA - US-038 (CA-06 al CA-12)"
role: "QA"
epic: "US-038 - Asignación Multi-Rol y Sincronización EntraID"
iteration: "02-DEV-038-DAVID"
branch: "DevDavid"
---

# Handoff Arquitectónico: QA SDET

## 1. Contexto y Objetivos
El objetivo de esta iteración es validar empíricamente (E2E) las implementaciones de Segregación de Funciones (SoD), Delegación Jerárquica con mensajería de contingencia y las vistas de Anomalías de la US-038.

**Exclusiones:** El CA-09 ha sido EXCLUIDO y no requiere validación.

## 2. Directiva de Infraestructura Híbrida (OBLIGATORIA — Leer ANTES de cualquier acción)

> ⚠️ **CAMBIO ARQUITECTÓNICO CRÍTICO — Infraestructura Dividida:**
> El proceso de Spring Boot (`ibpms-core`) **YA NO CORRE DENTRO DE DOCKER**. Se ejecuta directamente en la consola del host Windows (JVM local) en el puerto `8080`. Los servicios de soporte (PostgreSQL, Redis, RabbitMQ) **SÍ SIGUEN EN DOCKER**.

**Topología de Infraestructura Vigente:**

| Servicio | Ejecución | Puerto Host | Validación |
|----------|-----------|:-----------:|------------|
| **Spring Boot (ibpms-core)** | **Consola local (JVM host)** | `8080` | `curl http://localhost:8080/actuator/health` |
| PostgreSQL | Docker (`ibpms-postgres-uat`) | `5433` → 5432 | `docker ps --filter name=ibpms-postgres` |
| Redis | Docker (`ibpms-redis-uat`) | `6379` | `docker ps --filter name=ibpms-redis` |
| RabbitMQ | Docker (`ibpms-rabbitmq-uat`) | `5672` / `15672` | `docker ps --filter name=ibpms-rabbitmq` |

**Protocolo de Pre-Validación (EJECUTAR ANTES de escribir o correr cualquier test):**

1. **Paso 1 — Verificar que Spring Boot está corriendo:**
   ```powershell
   curl -s http://localhost:8080/actuator/health
   ```
   - **Si responde `{"status":"UP"}`:** El backend está operativo. Continuar.
   - **Si no responde o da error de conexión:** Pasar al Paso 2.

2. **Paso 2 — Arrancar Spring Boot en consola (SOLO si el Paso 1 falló):**
   ```powershell
   cd backend
   mvn spring-boot:run -pl ibpms-core -Dspring-boot.run.profiles=default
   ```
   - Esperar hasta ver en la consola: `Tomcat started on port 8080` y `Started IbpmsCoreApplication`.
   - Repetir el Paso 1 para confirmar el arranque exitoso.

3. **Paso 3 — Verificar servicios Docker complementarios:**
   ```powershell
   docker ps --format "table {{.Names}}\t{{.Status}}"
   ```
   - Confirmar que `ibpms-postgres-uat`, `ibpms-redis-uat` y `ibpms-rabbitmq-uat` estén con status `Up` y `(healthy)`.
   - Si algún contenedor no está corriendo: `docker compose up -d` desde la raíz del proyecto.

4. **Paso 4 — Verificar Frontend (necesario para tests E2E de UI):**
   ```powershell
   curl -s http://localhost:5173
   ```
   - Si no responde: `cd frontend && npm run dev` en background.

> 🚫 **PROHIBICIONES ESTRICTAS:**
> - **PROHIBIDO** intentar levantar el backend con `docker compose up ibpms-core` o crear un servicio Docker para el backend.
> - **PROHIBIDO** modificar el `docker-compose.yml` para añadir el servicio de backend.
> - **PROHIBIDO** asumir que el backend está corriendo sin ejecutar el health check del Paso 1.
> - **PROHIBIDO** matar o reiniciar el proceso de Spring Boot sin justificación técnica documentada.

## 3. Alineación Arquitectónica y QA
* **ADR-010 (Testing Pyramid):** Debes validar que el flujo E2E cubre todas las capas. Se exige un entorno local levantado (Backend en consola + Docker para BD/Cache/MQ + Frontend en dev server).
* **Zero-Mock Policy:** Prohibido el uso de mocks en los tests de Playwright. Debes consumir los endpoints reales.

## 4. Credenciales de Prueba
* **Usuario:** `root@ibpms.local`
* **Contraseña:** `Root#Temp4Sys`

## 5. Requerimientos Técnicos (Entregables)

### 5.1 Suite E2E (Playwright)
Crear un archivo de test `us-038-iteration2-sod-delegation.spec.ts` que valide:
* **CA-06 (SoD):** Intentar aprobar una tarea en Camunda donde el aprobador sea igual al creador y validar que se bloquea en UI y que la anomalía aparece en el nuevo endpoint GET `/api/v1/security/anomalies`.
* **CA-07 y CA-08:** Validar el formulario de delegación de perfil (Fecha Inicio/Fin) y asegurarse que la solicitud hace POST exitosamente hacia `/api/v1/security/delegations`.
* **CA-10 y CA-11:** Validar visualmente la presencia de chips o badges en la UI correspondientes a los múltiples roles del usuario autenticado.
* **CA-12:** Validar el comportamiento del "Tablero de Anomalías", interactuando con el botón "Marcar como Subsanado" que ejecuta PUT `/api/v1/security/anomalies/{id}/resolve`.

### 5.2 Endpoints Backend a Validar (Contratos Reales)
| Endpoint | Método | Propósito | CA |
|----------|--------|-----------|:--:|
| `/api/v1/security/anomalies` | GET | Listar anomalías no resueltas | CA-12 |
| `/api/v1/security/anomalies/{id}/resolve` | PUT | Resolver una anomalía | CA-12 |
| `/api/v1/security/delegations` | POST | Crear delegación temporal | CA-07 |
| `/actuator/health` | GET | Health check del backend | Pre-validación |

Aplica el skill `.agents/skills/qa_e2e_validation_audit/SKILL.md` para garantizar la Ley de Correspondencia Gherkin (Test vs User Story). Todo CA sin test correspondiente debe reportarse como Cobertura Faltante.

> ⚠️ **IMPORTANTE:** Todo desarrollo o configuración debe quedar exhaustivamente documentado. Se exige **PRECISIÓN QUIRÚRGICA** en cada cambio para evitar efectos colaterales en funcionalidades existentes. Se han detectado regresiones en iteraciones previas; cualquier daño a funcionalidades adyacentes será motivo de rechazo inmediato.

---
**INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_qa.md`.
4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_qa.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.

> 📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
> - Aplica estrictamente el protocolo de pruebas **TDD** documentado en `.agents/skills/tdd_first/SKILL.md` (Red -> Green -> Refactor) antes de hacer lógica.
> - Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md` (naming, error handling, modularidad).
