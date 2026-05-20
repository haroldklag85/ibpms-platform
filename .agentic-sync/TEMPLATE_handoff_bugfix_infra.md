# 🔧→🗄️ Handoff Bug-Fix: BUG-FIX LEAD → INFRA/BD
# BUG-[ID]: [TÍTULO DEL BUG — COMPLETAR POR EL BUG-FIX LEAD]

**Emitido por:** 🔧 BUG-FIX LEAD (Orquestador de Correcciones)
**Destinatario:** 🗄️ INFRA/BD
**Fecha:** [COMPLETAR — ISO 8601]
**Rama de corrección:** `DevDavid/bugfix/[COMPLETAR]`
**Prioridad:** [🔴 Alta | 🟡 Media | 🟢 Baja]
**Dependencia:** Ninguna

---

## 📖 LECTURAS OBLIGATORIAS (Ejecutar ANTES de intervenir)

```bash
# 1. Política del Enjambre (Leyes Globales 0-4)
cat .cursorrules

# 2. ADRs de infraestructura
cat docs/architecture/adr_009_postgresql_pgvector_migration.md
cat docs/architecture/rabbitmq_topology.md

# 3. Skills transversales
cat .agents/skills/clean_code_standards/SKILL.md

# 4. ERD canónico
cat docs/architecture/data_architecture_erd.md

# 5. Diagnóstico del bug
cat .agentic-sync/bug_diagnosis_[ID].md

# 6. Arquitectura general del proyecto
cat docs/architecture/arquitecturar.md
```

> ⚠️ **LEY GLOBAL 3 — Trazabilidad Inversa:** TODO cambio de infraestructura DEBE llevar
> el comentario `-- @Traceability: US-[XXX], CA-[XX], BUG-FIX: [descripción]` en SQL
> o `# @Traceability` en YAML/Docker. INNEGOCIABLE.

---

## 🔬 Diagnóstico del Bug-Fix Lead

[COMPLETAR POR EL BUG-FIX LEAD — Descripción precisa del bug de infraestructura]

| Hallazgo | Ubicación | Detalle |
|----------|:---------:|---------|
| [COMPLETAR] | [Archivo:línea o Tabla:Columna] | [Descripción técnica] |

---

## 🎯 Instrucciones Quirúrgicas

### Paso 1: [COMPLETAR — Título del paso]
**Archivo:** `[COMPLETAR — ruta/relativa/al/archivo o tabla afectada]`

[COMPLETAR — Descripción de lo que se debe corregir]

```sql
-- @Traceability: US-[XXX], CA-[XX], BUG-FIX: [descripción]
-- [COMPLETAR — SQL ejecutable, NO pseudocódigo]
```

> ⚠️ **RESTRICCIÓN CRÍTICA:** NO modifiques NADA fuera de los archivos listados.
> **PROHIBIDO** ejecutar SQL directo contra la BD. Todo cambio DEBE ir en un changeset Liquibase.

> ⚠️ **IMPORTANTE:** Se exige **PRECISIÓN QUIRÚRGICA** en cada cambio. Inspeccionar los scripts de Liquibase existentes ANTES de crear uno nuevo para evitar Amnesia Institucional (tablas duplicadas o columnas ya existentes).

---

## ✅ Criterios de Aceptación (DoD)

| # | Criterio | Evidencia |
|---|----------|-----------|
| 1 | El bug de infraestructura está resuelto | [COMPLETAR — Verificación] |
| 2 | No hay drift en el esquema | Liquibase ejecuta sin errores al arrancar Spring Boot |
| 3 | Contenedores Docker saludables | `docker ps` → todos con status `Up (healthy)` |
| 4 | Código documentado con @Traceability | Changeset contiene comentario de trazabilidad |
| 5 | Commit atómico en rama de bugfix | `git log -n 1 --oneline` → `fix(infra): ...` |

---

## 🚦 SECUENCIA DE EJECUCIÓN

1. Leer las lecturas obligatorias (Sección 2)
2. Posicionarse en la rama: `git checkout DevDavid/bugfix/[nombre]`
3. Inspeccionar los scripts Liquibase existentes para prevenir duplicación
4. Aplicar las correcciones quirúrgicas (Sección 4)
5. **Validación de esquema obligatoria:** Ejecuta verificaciones de sintaxis Liquibase antes de hacer push.
6. Commit: `git add . && git commit -m "fix(infra): US-XXX BUG-FIX [descripción]" && git push origin DevDavid/bugfix/[nombre]`

---

## 📋 Instrucciones para Copiar y Pegar

```
Asume el rol de 🗄️ INFRA/BD (Agente de Corrección Quirúrgica de Bugs de Infraestructura).

ANTES DE HACER CUALQUIER COSA, lee obligatoriamente estos archivos en este orden:

1. cat .cursorrules
2. cat docs/architecture/adr_009_postgresql_pgvector_migration.md
3. cat docs/architecture/data_architecture_erd.md
4. cat .agents/skills/clean_code_standards/SKILL.md
5. cat .agentic-sync/bug_diagnosis_[ID].md
6. cat .agentic-sync/handoff_bugfix_infra.md

TU MISIÓN:
1. Posicionarte en la rama: git checkout DevDavid/bugfix/[nombre]
2. Confirmar el bug leyendo el diagnóstico
3. Inspeccionar Liquibase existente para evitar Amnesia Institucional
4. Aplicar SOLO las correcciones quirúrgicas indicadas
5. Documentar con @Traceability
6. Verificar sintaxis Liquibase
7. Commit: git add . && git commit -m "fix(infra): US-XXX BUG-FIX [desc]" && git push

REGLAS INQUEBRANTABLES:
- PROHIBIDO ejecutar SQL directo contra PostgreSQL.
- PROHIBIDO modificar docker-compose.yml para añadir el servicio de backend.
- PROHIBIDO crear tablas o columnas que ya existan (inspeccionar antes).
- PROHIBIDO usar git stash. Solo git commit + git push.
- Documentar con @Traceability en SQL y YAML.
```

---

**INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
1. Inicia en modo `PLANNING`. Documenta tu plan en `implementation_plan.md`.
2. **PROHIBIDO pedirle al Humano que apruebe.** Guarda tu plan en `.agentic-sync/approval_request_bugfix_infra.md`.
3. Dile al Humano: *"He dejado mi solicitud en `.agentic-sync/approval_request_bugfix_infra.md`. Por favor, ve al chat del Bug-Fix Lead y entrégale el mensaje."*
4. Espera el veredicto. Si aprobado, ejecuta, verifica y haz `git commit` + `git push`.
