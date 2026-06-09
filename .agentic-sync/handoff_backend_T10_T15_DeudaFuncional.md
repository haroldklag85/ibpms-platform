# 🧠→⚙️ Handoff: Arquitecto Líder → Agente Backend
# T-12/T-15: Auditoría y Cierre de Deuda Funcional Backend

**Emitido por:** 🧠 ARQUITECTO LÍDER
**Destinatario:** ⚙️ BACKEND - JAVA
**Fecha:** 2026-05-12T10:25:00-05:00
**Sprint:** 7 — Iteración 7.1
**Prioridad:** 🔴 Alta
**Dependencia:** Ninguna

## 📖 LECTURAS OBLIGATORIAS (Ejecutar ANTES de codificar)

```bash
# 1. Política del Enjambre (LEY GLOBAL 0, 1, 2, 3)
cat .cursorrules

# 2. Skill principal del agente receptor (Asegurar Compilación Nata)
cat .agents/skills/backend_sre_compilation_audit/SKILL.md

# 3. Skills transversales aplicables
cat .agents/skills/clean_code_standards/SKILL.md
cat .agents/skills/zero_mock_enforcement/SKILL.md

# 4. Requerimientos Funcionales (Epic A)
cat docs/requirements/epics/epic_A_motor_core.md
```

> ⚠️ **LEY GLOBAL 3 — Trazabilidad Inversa:** TODO el código nuevo DEBE llevar
> la anotación @Traceability o comentario `// @Traceability: US-XXX, CA-XX`.
> Esto es INNEGOCIABLE.

## 🔬 Diagnóstico del Arquitecto

La funcionalidad para las tareas T-12 y T-15 fue implementada en ciclos anteriores, sin embargo, se omitió la inyección de los marcadores de Trazabilidad Inversa exigidos por la arquitectura para certificar las tareas.

| Violación/Hallazgo | Ubicación | Detalle |
|--------------------|:---------:|---------|
| Falta Traceability CA-06 | `backend/ibpms-core/src/main/java/com/ibpms/poc/application/services/AutoClaimService.java` | La clase y método programado carecen de `// @Traceability: US-002, CA-06` |
| Falta Traceability CA-16 | `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/messaging/FormSchemaChangedRabbitListener.java` | La clase carece de `// @Traceability: US-007, CA-16` |

## 🎯 Instrucciones Quirúrgicas

### Paso 1: Inyectar trazabilidad en AutoClaimService

**Archivo:** `backend/ibpms-core/src/main/java/com/ibpms/poc/application/services/AutoClaimService.java`

Agrega el comentario de trazabilidad en la declaración de la clase.

```java
// @Traceability: US-002, CA-06
@Service
@RequiredArgsConstructor
@Slf4j
public class AutoClaimService {
    // Lógica existente inalterada
}
```

### Paso 2: Inyectar trazabilidad en FormSchemaChangedRabbitListener

**Archivo:** `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/messaging/FormSchemaChangedRabbitListener.java`

Agrega el comentario de trazabilidad en la declaración de la clase.

```java
// @Traceability: US-007, CA-16
@Component
@RequiredArgsConstructor
@Slf4j
public class FormSchemaChangedRabbitListener {
    // Lógica existente inalterada
}
```

## Criterios de Aceptación (DoD)

| # | Criterio | Evidencia |
|---|----------|-----------|
| 1 | `AutoClaimService.java` tiene tag CA-06 | `grep "@Traceability: US-002, CA-06" backend/ibpms-core/src/main/java/com/ibpms/poc/application/services/AutoClaimService.java` retorna resultados |
| 2 | `FormSchemaChangedRabbitListener.java` tiene tag CA-16 | `grep "@Traceability: US-007, CA-16" backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/messaging/FormSchemaChangedRabbitListener.java` retorna resultados |
| 3 | Compilación Exitosa y Commit | `mvn clean compile` exitoso en el path especificado |

## 🚦 SECUENCIA DE EJECUCIÓN

1. Modificar `backend/ibpms-core/src/main/java/com/ibpms/poc/application/services/AutoClaimService.java` para inyectar `// @Traceability: US-002, CA-06`.
2. Modificar `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/messaging/FormSchemaChangedRabbitListener.java` para inyectar `// @Traceability: US-007, CA-16`.
3. Compilar: `cd backend/ibpms-core && mvn clean compile`
4. Commit: `git add . && git commit -m "chore(backend): inyectar marcadores de trazabilidad para T-12 y T-15" && git push`

## 📋 Instrucciones para Copiar y Pegar

```text
Asume el rol de ⚙️ BACKEND - JAVA.

ANTES DE HACER CUALQUIER COSA, lee obligatoriamente estos archivos en este orden exacto:

1. cat .cursorrules
2. cat .agents/skills/backend_sre_compilation_audit/SKILL.md
3. cat .agents/skills/clean_code_standards/SKILL.md
4. cat docs/requirements/epics/epic_A_motor_core.md
5. cat .agentic-sync/handoff_backend_T10_T15_DeudaFuncional.md

TU MISIÓN:

1. Inyecta el marcador `// @Traceability: US-002, CA-06` en `AutoClaimService.java`.
2. Inyecta el marcador `// @Traceability: US-007, CA-16` en `FormSchemaChangedRabbitListener.java`.
3. Build/Compile: `cd backend/ibpms-core && mvn clean compile`
4. Commit: `git add . && git commit -m "chore(backend): inyectar marcadores de trazabilidad para T-12 y T-15" && git push`

REGLAS INQUEBRANTABLES:
- DEBES preservar el código funcional existente; tu única tarea es inyectar trazabilidad.
- DEBES asegurar la compilación nativa vía Maven en el host.
- PROHIBIDO el uso de pseudocódigo. Las inyecciones deben ser exactas tal cual se describen en el handoff.
```
