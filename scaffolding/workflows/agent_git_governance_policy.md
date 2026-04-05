---
description: Política oficial y protocolo de gobierno para el uso de Git Local y GitHub Remoto aplicable a humanos y Agentes IA.
---

# Gobierno Estratégico de Versionamiento (Git / GitHub) en Entorno Multi-Agente

## Contexto
El iBPMS es desarrollado por un escuadrón iterativo compuesto por un Humano (Product Owner Master / Code-Reviewer) y un set de Agentes de IA Especializados (Arquitecto lider Software, Frontend, Backend, QA, Product Owner ). Para evitar la sobreescritura destructiva, corrupción del `main` y conflictos de código, se establece esta política de obligado cumplimiento.

## 1. Topología de Ramas (Branches)

### El Tronco Sagrado (`main` / `master`)
- **Naturaleza:** Es la Vitrina de Producción Oficial.
- **Regla Estricta:** NINGÚN Agente Especialista (Ej. Arquitecto lider Software, Frontend, Backend, QA, Product Owner ) tiene autoridad para hacer un Commit o Push directo a esta rama cuando esté desarrollando código transaccional.
- **Condición de Entrada:** El código solo entra a `main` si compila, no rompe el sistema y ha sido aprobado con test end to end.

### Ramas de Agentes (Sprints y Carrera de Relevos)
- **Naturaleza:** Universos paralelos de trabajo vinculados a una Historia de Usuario (US). Operan bajo la modalidad de **Carrera de Relevos (Rama Única por US)**.
- **Nomenclatura Obligatoria:** `sprint-{numero}/us-{numero}-{descripcion}`
- **Ejemplo:** `sprint-3/us-052-orquestacion`, `sprint-3/us-012-login`.
- **Regla Estricta:** NO se deben crear ramas divididas por rol (`/backend/`, `/frontend/`) para evitar el "integration hell". Cuando se desarrolle una historia, **TODOS los agentes (Back, Front, QA) heredarán y trabajarán SECUENCIALMENTE sobre la misma rama compartida**. El agente siempre deberá hacer `git pull` antes de comenzar para obtener el código que le dejó el agente anterior.

### Ramas del Arquitecto Líder (Hotfixes y Core)
- **Naturaleza:** La "Ambulancia" o el equipo de mantenimiento pesado. Usadas exclusivamente por el Agente Orquestador (Arquitecto lider software) para apagar incendios (bugs), actualizar librerías o resolver fallas inter-modulares que no pertenecen a una US particular.
- **Nomenclatura Obligatoria:** `architect/hotfix/{descripcion-error}` o `architect/chore/{descripcion-mantenimiento}`
- **Ejemplo:** `architect/hotfix/crash-base-datos`, `architect/chore/actualizar-dependencias`.

### Ramas del Agente Product Owner (Refinamiento e Iteraciones)
- **Naturaleza:** Espacio analítico y de documentación. Utilizadas exclusivamente por el Agente Product Owner para redactar, ajustar o refinar Historias de Usuario, Criterios de Aceptación, o planificar iteraciones sin bloquear a los agentes que están programando código.
- **Nomenclatura Obligatoria:** `po/refinement/{descripcion}` o `po/sprint-{numero}/{descripcion}`
- **Ejemplo:** `po/refinement/us-052-ai-agents`, `po/sprint-3/planning`.

### Ramas Humanas (`human/{sprint}/{feature}` o Ediciones en Caliente)
- **Naturaleza:** Espacio de trabajo exclusivo para el usuario humano, idealmente atado al sprint actual.
- **Regla Estricta:** Si el usuario decide meter mano directamente al código, debe hacerlo en su rama (Ej: `human/sprint-1/fix-boton`). Sin embargo, para cambios exclusivos de **Documentación (Ej. `v1_user_stories.md`)**, el Arquitecto Orquestador tiene permiso especial para hacer commits directos al `main` en modo "Chore/Docs" bajo explícita orden del Humano para proteger la Verdad Única (SSOT).


## 2. Flujo Operativo y Articulación (Paso a Paso)

Cuando se asigne una nueva Tarea Técnica:

1. **Planificación y Aislamiento (El Agente):** 
   - El agente recibe la tarea (asociada a una User Story).
   - Ejecuta: `git checkout main` y `git pull` (Asegura tener lo último).
   - Si la rama de la US ya existe (creada por un agente anterior en la Carrera de Relevos):
     - Ejecuta: `git checkout sprint-{n}/us-{n}-{desc}` y `git pull`.
   - Si la rama NO existe (es el primer agente en trabajar la US):
     - Ejecuta: `git checkout -b sprint-{n}/us-{n}-{desc}`.
   - Construye la solución, edita archivos, hace pruebas (si aplica).

2. **Sincronización Transitoria (El Agente):**
   - El agente hace commit de su trabajo en la rama compartida de la US.
   - Ejecuta: `git push origin sprint-{n}/us-{n}-{desc}`.
   - Informa al Orquestador y al Humano: *"He finalizado mi tarea en la rama de la US"*.

3. **La Aduana y el Merge (Orquestador / Humano):**
   - El Humano (o el Agente QA/Orquestador si se le da autoridad) evalúa que el código en esa rama paralela funciona y no rompe nada.
   - Solo tras la validación, se ejecuta la Fusión (Pull Request o Merge Local) empujando desde la rama del agente hacia el `main`.
   - Se elimina la rama temporal del agente para mantener limpio el repositorio.

## 3. Resolución de Conflictos
- Si un agente detecta un conflicto al intentar fusionar (Merge Conflict), DEBE DETENERSE. No está autorizado a sobreescribir líneas conflictivas sin consultar al Arquitecto Orquestador o al Humano. El Humano actuará como "Juez" para resolver qué versión prevalece.

## 4. Políticas de Sincronización Remota (Nube)
- **Regla de Sincronización:** Cada vez que el Humano arranque el día de trabajo, o un Agente sea despertado, DEBEN ejecutar `git fetch origin main` y `git status` para comparar el estado del disco duro con el de la Nube (GitHub), garantizando que nadie esté trabajando con "Memorias Viejas".
- **Blindaje Documental:** Si el humano realiza modificaciones manuales en archivos de requerimientos o flujos, el Agente Orquestador orquestará un `git commit` inmediato (Snapshot de Seguridad) contra el `main` para evitar que otros agentes, por amnesia temporal, retrocedan el trabajo del humano.
