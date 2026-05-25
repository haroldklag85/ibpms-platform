# 🧠→🏗️ Handoff: QA E2E → Arquitectura/Backend
# CRITICAL BUG: Crash de Spring Boot por ClassNotFoundException

**Emitido por:** 🕵️ QA E2E
**Destinatario:** 🏗️ ARQUITECTO LÍDER / ⚙️ BACKEND
**Fecha:** 2026-05-24T15:58:00-05:00
**Sprint:** V1 — Certificación Zero-Mock
**Prioridad:** 🔴 Blocker (Detiene toda la línea E2E)
**Dependencia:** Ninguna

## 📖 LECTURAS OBLIGATORIAS (Ejecutar ANTES de codificar)

```bash
# 1. Política del Enjambre (LEY GLOBAL 0, 1, 2, 3)
cat .cursorrules

# 2. Skill principal del agente receptor
cat .agents/skills/backend_sre_compilation_audit/SKILL.md

# 3. Handoff actual (Este documento)
cat .agentic-sync/handoff_qa_arquitectura_US005_Crash.md
```

> ⚠️ **LEY GLOBAL 3 — Trazabilidad Inversa:** Recuerda mantener `@Traceability: US-005` en cualquier cambio estructural para reparar este bloqueo.

## 🔬 Diagnóstico del QA E2E

Mientras preparaba la batería de pruebas final para certificar los escenarios **CA-3** y **CA-6** de la **US-005** (las cuales ya fueron saneadas en el frontend por QA), el entorno de pruebas fue reiniciado. Al intentar levantar el servidor Spring Boot nuevamente mediante Maven (`spring-boot:run`), este falló catastróficamente impidiendo el arranque del Contexto de Spring.

| Violación/Hallazgo | Ubicación | Detalle |
|--------------------|:---------:|---------|
| `ClassNotFoundException` | `com.ibpms.poc.domain.model.FormEvent` | La clase de dominio ha desaparecido, no está compilada o el paquete fue renombrado, causando un fallo en cascada. |
| Falla en Inyección de Dependencias | `FormEventRepositoryJpa` / `AutoClaimService` | Spring Boot no puede instanciar `formEventRepositoryJpa` porque la clase subyacente `FormEvent` no existe, lo cual a su vez rompe la instanciación de `autoClaimService`. |

**Fragmento del Stacktrace Real:**
```text
Caused by: org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'autoClaimService'...
Caused by: org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'formEventRepositoryJpa': Lookup method resolution failed...
Caused by: java.lang.NoClassDefFoundError: com/ibpms/poc/domain/model/FormEvent
Caused by: java.lang.ClassNotFoundException: com.ibpms.poc.domain.model.FormEvent
```

## 🎯 Instrucciones Quirúrgicas

### Paso 1: Rastrear y Restaurar Entidad de Dominio
Revisar si la clase `FormEvent` en el paquete `com.ibpms.poc.domain.model` fue borrada accidentalmente en un refactor reciente o si hay un desajuste entre las ramas. Si fue borrada o movida, ajustar las entidades JPA dependientes como `FormEventRepositoryJpa`.

### Paso 2: Validación de Compilación Completa
Asegurarse de limpiar la caché de Maven y recompilar explícitamente para garantizar que el `ClassLoader` obtenga todas las clases.

## Criterios de Aceptación (DoD)

| # | Criterio | Evidencia |
|---|----------|-----------|
| 1 | El Backend compila sin errores | `mvn clean compile` devuelve SUCCESS |
| 2 | Spring Boot levanta correctamente el puerto 8080 | `mvn spring-boot:run` finaliza la carga del contexto sin excepciones `UnsatisfiedDependencyException` |

## 🚦 SECUENCIA DE EJECUCIÓN

1. Analizar el código de `FormEventRepositoryJpa` y `AutoClaimService` para detectar qué entidad se rompió.
2. Restaurar `FormEvent.java` o corregir los repositorios huérfanos.
3. cd `backend/ibpms-core` && `mvn clean compile`
4. Encender el servidor: `mvn clean spring-boot:run "-Dspring-boot.run.profiles=e2e" "-Dmaven.test.skip=true"`
5. Notificar a QA que el entorno está encendido para correr Playwright.

## 📋 Instrucciones para Copiar y Pegar

```text
Asume el rol de 🏗️ ARQUITECTO LÍDER.

ANTES DE HACER CUALQUIER COSA, lee obligatoriamente estos archivos en este orden exacto:
1. cat .cursorrules
2. cat .agents/skills/backend_sre_compilation_audit/SKILL.md
3. cat .agentic-sync/handoff_qa_arquitectura_US005_Crash.md

TU MISIÓN:
1. Solucionar el fallo catastrófico de Spring Boot: `java.lang.ClassNotFoundException: com.ibpms.poc.domain.model.FormEvent`.
2. Restaurar la entidad faltante o eliminar el código muerto que la invoca (e.g., `formEventRepositoryJpa`).
3. Verificar que el servidor compile y arranque: `cd backend\ibpms-core; ..\..\maven\apache-maven-3.9.6\bin\mvn.cmd clean spring-boot:run "-Dspring-boot.run.profiles=e2e" "-Dmaven.test.skip=true"`
4. Avisarle a la sesión de QA cuando el servidor esté corriendo en el puerto 8080 para que pueda finalizar la certificación.
```
