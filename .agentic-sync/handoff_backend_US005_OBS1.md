# Handoff Arquitectónico — BACKEND (OBS-1 / US-005 CA-68)

## 1. 🗂️ METADATOS DEL HANDOFF
- **Rol Destino**: Backend (Java 17, Spring Boot, Liquibase)
- **Iteración/Slot**: Sprint PM-01, Slot 3
- **Épica/Feature**: B — Formularios/BPMN
- **Historia de Usuario**: US-005 (OBS-1: Desalineación Entity/DDL)
- **Alineación Arquitectónica**: ADR-001 (Hexagonal), ADR-009 (PostgreSQL).

## 2. 🎯 CONTEXTO Y OBJETIVO
El motor BPMN (US-005) se encuentra al 97%, pero existe una desalineación crítica (OBS-1 / CA-68) entre el DDL de Liquibase y las entidades JPA que representan los modelos/despliegues BPMN. 
**Objetivo**: Reconciliar las entidades de negocio (como `BpmProcessEntity` o análogos) con los changelogs de Liquibase para lograr paridad estricta y evitar excepciones de Hibernate en el arranque.

## 3. 🧩 CAs A IMPLEMENTAR / DEFECTOS A CORREGIR
- **OBS-1**: Corregir discrepancias entre esquema SQL (Liquibase) y la definición `@Entity` / `@Table` / `@Column` en Java. 
- Debes crear un script de parche (`X-us005-obs1-fix.sql`) si la BD carece de columnas requeridas por el código, o ajustar el código Java para reflejar la DB actual.

## 4. 📋 DIRECTIVA DE INFRAESTRUCTURA HÍBRIDA Y REGLAS ESTRICTAS
El backend Spring Boot corre en consola local (NO en Docker) en el puerto `8080`.
1. `curl -s http://localhost:8080/actuator/health` → Debe responder `{"status":"UP"}`.
2. Si no responde, arráncalo con: `cd backend && mvn spring-boot:run -pl ibpms-core -Dspring-boot.run.profiles=default`.
3. Verifica Docker: `docker ps` → PostgreSQL (`5433`), Redis (`6379`), RabbitMQ (`5672`).

**REGLAS:**
- **Zero Mocks**: Prohibido mockear datos.
- Mapea correctamente los tipos (ej. JSONB a `Map<String, Object>` usando `io.hypersistence.utils`).

## 5. 🚦 SECUENCIA DE EJECUCIÓN (SRE AUDIT)
1. Analizar el Entity y el Liquibase DDL actual.
2. Hacer el fix.
3. Compilación obligatoria: Ejecuta el protocolo Zero-Trust SRE (`mvn clean compile`).
4. Arrancar la app y validar que Hibernate no arroje errores de validación de esquema.
5. Actualizar `docs/sprints/gobernanza_pm/CHANGELOG_NO_TECNICO.md` (QUÉ y PARA QUÉ, sin jerga técnica).
6. Commit y push.

## 6. INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN
1. Inicia estrictamente en modo `PLANNING` y elabora un plan documentado en `implementation_plan.md`.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.**
3. Guarda tu solicitud de revisión en `.agentic-sync/approval_request_BACKEND_OBS1.md`.
4. Detente y dile al Humano: *"Humano, he dejado mi solicitud en `.agentic-sync/approval_request_BACKEND_OBS1.md`. Entrégala al Arquitecto."*
5. Espera en este chat. Cuando el humano regrese con la aprobación, pasa a modo `EXECUTION`.
6. ANTES del commit final, actualiza `CHANGELOG_NO_TECNICO.md`.
7. Finaliza con `git commit` y `git push` en `sprint-8/pm-01/us-007-030-bpmn`. Queda prohibido usar git stash.
