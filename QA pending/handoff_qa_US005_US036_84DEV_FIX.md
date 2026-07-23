# 🧪 HANDOFF QA (DIFERIDO) — Iteración Correctiva 84-DEV-LANE-ROLE-FIX

> **Tipo:** QA diferido — Validación por UAT humano
> **Fecha de emisión:** 2026-07-14
> **Estado:** 📋 PENDIENTE — Se ejecutará como UAT manual post-correcciones

---

## 1. Metadatos

| Campo | Valor |
|-------|-------|
| **Iteración** | `84-DEV-LANE-ROLE-FIX` |
| **US** | US-005 + US-036 |
| **Necesita QA automatizado** | **NO** — Reemplazado por UAT humano |
| **Rama Git** | `DevDavid` |

---

## 2. Contexto de Diferimiento

Esta iteración correctiva no requiere ejecución de QA automatizado por las siguientes razones:
1. Son correcciones quirúrgicas a código existente (no features nuevas)
2. El PM-IA indicó explícitamente: "Validación por UAT humano post-correcciones"
3. Las correcciones serán validadas por Harold mediante pruebas UAT manuales

---

## 3. Scenarios de Validación UAT (Para el humano)

Los siguientes escenarios deben validarse manualmente:

### Escenario 1: Sincronización de Lanes al Deploy (D-01)
```gherkin
Given un diagrama BPMN con 2 lanes ("Analista", "Gerente")
When despliego el diagrama desde el IDE BPMN
Then las 2 lanes aparecen en la tabla `ibpms_bpmn_lane`
And cada registro tiene `process_design_id` NOT NULL
```

### Escenario 2: Validaciones 400/404 en Asignaciones (D-03)
```gherkin
Given estoy en la pantalla de Identidad y Gobernanza
When intento asignar lanes a un rol inexistente vía API
Then recibo error 404 "Role not found"

Given un rol existente
When intento asignar un lane con ID inexistente
Then recibo error 400 "Lane not found"
```

### Escenario 3: assigned_by con usuario real (D-02)
```gherkin
Given estoy autenticado como "harold@ibpms.com"
When creo una asignación lane-role
Then el campo `assigned_by` en la BD muestra "harold@ibpms.com" (no "system")
```

### Escenario 4: Toasts de error visibles (D-09)
```gherkin
Given el backend está detenido (API no disponible)
When intento expandir un proceso para ver sus lanes
Then veo un toast rojo con mensaje de error descriptivo

Given el backend está detenido
When intento eliminar un rol
Then veo un toast rojo de error
And el rol NO desaparece de la lista
```

---

## 4. Referencia

> Aplica el skill `.agents/skills/qa_e2e_validation_audit/SKILL.md` para garantizar la Ley de Correspondencia Gherkin (Test vs User Story). Todo CA sin test correspondiente debe reportarse como Cobertura Faltante.
