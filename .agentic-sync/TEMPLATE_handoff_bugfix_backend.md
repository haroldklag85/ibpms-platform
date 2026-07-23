# 🔧→⚙️ Handoff Bug-Fix: BUG-FIX LEAD → BACKEND - JAVA
# BUG-[ID]: [TÍTULO DEL BUG — COMPLETAR POR EL BUG-FIX LEAD]

**Emitido por:** 🔧 BUG-FIX LEAD (Orquestador de Correcciones)
**Destinatario:** ⚙️ BACKEND - JAVA
**Fecha:** [COMPLETAR — ISO 8601]
**Rama de corrección:** `DevDavid/bugfix/[COMPLETAR]`
**Prioridad:** [🔴 Alta | 🟡 Media | 🟢 Baja]
**Dependencia:** Ninguna (o indicar handoff de Infra/BD si aplica)

---

## 📖 LECTURAS OBLIGATORIAS (Ejecutar ANTES de codificar)

```bash
# 1. Política del Enjambre (Leyes Globales 0-4)
cat .cursorrules

# 2. Skill principal del agente Backend
cat .agents/skills/backend_sre_compilation_audit/SKILL.md

# 3. Skills transversales
cat .agents/skills/clean_code_standards/SKILL.md
cat .agents/skills/zero_mock_enforcement/SKILL.md

# 4. ADRs relevantes al bug
cat docs/architecture/adr-001-hexagonal-architecture.md
cat docs/architecture/[COMPLETAR — ADR adicional si aplica]

# 5. Diagnóstico del bug
cat .agentic-sync/bug_diagnosis_[ID].md

# 6. Arquitectura general del proyecto
cat docs/architecture/arquitecturar.md
```

> ⚠️ **LEY GLOBAL 3 — Trazabilidad Inversa:** TODO el código modificado DEBE llevar
> `// @Traceability: US-[XXX], CA-[XX], BUG-FIX: [descripción del parche]`.
> Esto es INNEGOCIABLE.

---

## 🔬 Diagnóstico del Bug-Fix Lead

[COMPLETAR POR EL BUG-FIX LEAD — Descripción precisa del bug con evidencia forense]

| Hallazgo | Ubicación | Detalle |
|----------|:---------:|---------|
| [COMPLETAR] | [Archivo:línea] | [Descripción técnica precisa] |

**Componentes existentes reutilizables detectados:**
| Componente | Ubicación | Relevancia |
|------------|:---------:|------------|
| [COMPLETAR si aplica] | [Archivo] | [Por qué es relevante] |

---

## 🎯 Instrucciones Quirúrgicas

### Paso 1: [COMPLETAR — Título del paso]
**Archivo:** `[COMPLETAR — ruta/relativa/al/archivo]`

[COMPLETAR — Descripción de lo que se debe corregir]

```java
// @Traceability: US-[XXX], CA-[XX], BUG-FIX: [descripción]
// [COMPLETAR — Snippet de código ejecutable, NO pseudocódigo]
```

### Paso 2: [COMPLETAR — Si hay más pasos]
**Archivo:** `[COMPLETAR]`

[COMPLETAR]

> ⚠️ **RESTRICCIÓN CRÍTICA:** NO modifiques NADA fuera de los archivos listados en estas instrucciones.
> Cualquier cambio fuera de alcance será motivo de RECHAZO INMEDIATO.

> ⚠️ **IMPORTANTE:** Todo desarrollo debe quedar exhaustivamente documentado. Se exige **PRECISIÓN QUIRÚRGICA** en cada cambio para evitar efectos colaterales en funcionalidades existentes. Cualquier daño a funcionalidades adyacentes será motivo de rechazo inmediato.

---

## ✅ Criterios de Aceptación (DoD)

| # | Criterio | Evidencia |
|---|----------|-----------|
| 1 | El bug reportado ya no se reproduce | [COMPLETAR — Comando o acción para verificar] |
| 2 | No se introdujeron regresiones en la compilación | `mvn clean compile test` → BUILD SUCCESS |
| 3 | Código documentado con @Traceability | `grep "@Traceability.*BUG-FIX" [archivo]` → 1+ resultado |
| 4 | Tomcat arranca correctamente en puerto 8080 | `curl -s http://localhost:8080/actuator/health` → `{"status":"UP"}` |
| 5 | Commit atómico en rama de bugfix | `git log -n 1 --oneline` → `fix([alcance]): ...` |

---

## 🚦 SECUENCIA DE EJECUCIÓN

1. Leer las lecturas obligatorias (Sección 2)
2. Posicionarse en la rama: `git checkout DevDavid/bugfix/[nombre]`
3. Ejecutar el diagnóstico local para confirmar el bug
4. Aplicar las correcciones quirúrgicas (Sección 4)
5. **Compilación obligatoria:** Ejecuta el protocolo Zero-Trust SRE documentado en `.agents/skills/backend_sre_compilation_audit/SKILL.md` (§0 a §2). Si hay bloqueos de infraestructura, aplica el protocolo de reporte 3B.
6. Commit: `git add . && git commit -m "fix([alcance]): US-XXX BUG-FIX [descripción]" && git push origin DevDavid/bugfix/[nombre]`

---

## 📋 DIRECTIVA DE INFRAESTRUCTURA HÍBRIDA

> El backend Spring Boot corre en consola local (NO en Docker) en el puerto `8080`.
> 1. `curl -s http://localhost:8080/actuator/health` → Debe responder `{"status":"UP"}`.
> 2. Si no responde: `cd backend && mvn spring-boot:run -pl ibpms-core -Dspring-boot.run.profiles=default`.
> 3. Servicios Docker: `docker ps` → PostgreSQL (`5433`), Redis (`6379`), RabbitMQ (`5672`) → `Up (healthy)`.
> **PROHIBIDO** levantar el backend vía Docker o modificar `docker-compose.yml`.

---

## 📋 Instrucciones para Copiar y Pegar

```
Asume el rol de ⚙️ BACKEND - JAVA (Agente de Corrección Quirúrgica de Bugs).

ANTES DE HACER CUALQUIER COSA, lee obligatoriamente estos archivos en este orden:

1. cat .cursorrules
2. cat .agents/skills/backend_sre_compilation_audit/SKILL.md
3. cat .agents/skills/clean_code_standards/SKILL.md
4. cat docs/architecture/adr-001-hexagonal-architecture.md
5. cat .agentic-sync/bug_diagnosis_[ID].md
6. cat .agentic-sync/handoff_bugfix_backend.md

TU MISIÓN:
1. Posicionarte en la rama: git checkout DevDavid/bugfix/[nombre]
2. Confirmar el bug leyendo el diagnóstico
3. Aplicar SOLO las correcciones quirúrgicas indicadas
4. Documentar con @Traceability
5. Compilar: mvn clean compile test
6. Commit: git add . && git commit -m "fix([alcance]): US-XXX BUG-FIX [desc]" && git push

REGLAS INQUEBRANTABLES:
- PROHIBIDO modificar archivos fuera del alcance del handoff.
- PROHIBIDO crear funcionalidades nuevas. Solo reparar.
- PROHIBIDO omitir @Traceability en el código modificado.
- PROHIBIDO usar git stash. Solo git commit + git push.
- Documentar la solución con comentarios de referencia a la US y CA.
```

---

**INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
1. Inicia estrictamente en modo `PLANNING` y elabora un plan de corrección documentado en `implementation_plan.md`.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero.
3. Guarda tu solicitud de revisión en `.agentic-sync/approval_request_bugfix_backend.md`.
4. Dile al Humano: *"He dejado mi solicitud en `.agentic-sync/approval_request_bugfix_backend.md`. Por favor, ve al chat del Bug-Fix Lead y entrégale el mensaje."*
5. Espera el veredicto. Si aprobado, pasa a `EXECUTION`, corrige, compila y haz `git commit` + `git push`.

> 📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
> - Aplica estrictamente **TDD** documentado en `.agents/skills/tdd_first/SKILL.md` (Red -> Green -> Refactor).
> - Aplica estrictamente **Clean Code** documentado en `.agents/skills/clean_code_standards/SKILL.md`.
