# 🏗️ Handoff: Backend - Corrección de Tipos en Delegación (Type Mismatch)

## 1. Metadatos y SSOT (Single Source of Truth)
- **Iteración/Sprint:** 6.2-DEV
- **User Story / Tarea:** Resolución de Incidencia Crítica Nivel 0 (Colapso del Servidor Error 500) reportado en Workdesk.
- **Path del SSOT:** `docs/architecture/data_architecture_erd.md`.
- **Flujo de Trabajo:** Backend -> QA.

## 2. Alineación Arquitectónica y ADRs (Architecture Decision Records)
- **Validación de ADRs:** Esta delegación remedia una violación estricta del **ADR-009 (PostgreSQL pgvector migration)**, el cual exige el uso de tipos nativos `UUID` en la base de datos, prohibiendo la emulación mediante `VARCHAR(36)` o `VARCHAR(50)`.
- **Trazabilidad de la Solución:** La tabla `ibpms_security_delegation` fue creada erróneamente usando `VARCHAR(50)` para `delegator_id` y `substitute_id`. Esto provoca un `PSQLException` fatal debido a que Hibernate intenta ejecutar consultas contra la base de datos enviando el objeto `UUID` nativo extraído de la clase `UserEntity`. El ajuste prescriptivo alineará el modelo físico DDL con la abstracción JPA.

## 3. Rutas Exactas y Contexto Preexistente
- **Archivo Objetivo:** `backend/ibpms-core/src/main/resources/db/changelog/28-consolidate-delegation.sql`
- **Estado Actual:** En las líneas 19 y 20 de dicho archivo, las columnas se definen como `VARCHAR(50) NOT NULL`. Dado que estamos estabilizando entornos locales y no hemos ejecutado migraciones a producción de este script, la instrucción es **modificar directamente el changelog de creación** (y solicitar el reinicio de los volúmenes en Docker) para evitar acumulación de deuda técnica.

## 4. Snippets Prescriptivos (El "Qué" y el "Cómo")
Debes modificar exactamente las sentencias DDL en el script de Liquibase:

```sql
-- Reemplazar la sentencia de creación actual por:
CREATE TABLE IF NOT EXISTS ibpms_security_delegation (
    id UUID PRIMARY KEY,
    delegator_id UUID NOT NULL,
    substitute_id UUID NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    is_active BOOLEAN DEFAULT true,
    reason VARCHAR(255)
);
```

**Nota Operativa para el Backend:** Debido a que modificar un changelog ya ejecutado romperá el MD5 hash en la tabla `databasechangelog` de PostgreSQL, **debes indicar al usuario (o ejecutar en tu terminal)** el borrado del volumen docker local asociado a postgres (`docker volume rm ibpms-platform_postgres_data`) antes de volver a levantar el contenedor.

## 5. Matriz de QA y Testing Atómico
**Validación esperada (Agente QA / TDD):**
- **Clase de Prueba Recomendada:** Un repositorio test en `src/test/java/com/ibpms/poc/infrastructure/jpa/repository/security/DelegationRepositoryTest.java` (si existe).

| Test Name | CA Evaluado | Aserción Esperada |
|-----------|-------------|-------------------|
| `verifySubsituteIdQueryDoesNotThrowSqlGrammarException` | Cumplimiento ADR-009 | El repositorio JPA logra hacer un `.findBySubstituteIdAndIsActiveTrue()` recibiendo un `UUID` sin explotar a nivel JDBC. |

## 6. Mensaje de Despacho (Comunicación al Agente Especialista)
**Para Agente Backend:**
> "Compilación obligatoria: Ejecuta el protocolo Zero-Trust SRE documentado en `.agents/skills/backend_sre_compilation_audit/SKILL.md` (§0 a §2). Si hay bloqueos de infraestructura, aplica el protocolo de reporte 3B."
