# 🔍 Handoff QA (Pendiente) — BUG-J02-004, BUG-J02-005, BUG-J02-006

> **Estado**: ⏸️ QA PENDIENTE (necesita_qa = NO para esta iteración)  
> **US**: BUG01-JORNEY  
> **Rama**: `DevDavid`

---

## Bugs a Validar (cuando QA se active)

| Bug | Tipo | Endpoint/Vista | Validación |
|-----|------|---------------|-----------|
| BUG-J02-006 | Funcional Backend | `GET /api/v1/users/me/menu-layout` | Login con `ROLE_USER_INTERNAL` → menú NO vacío |
| BUG-J02-004 | Funcional Frontend | `BpmnDesigner.vue` — dropdown FormKey | Toggle filtro Simple/Maestro/Todos visible y funcional |
| BUG-J02-005 | Cosmético Frontend | `BpmnDesigner.vue` — dropdown FormKey | Estilos CSS corregidos (rounded, shadow, focus ring) |

## Gherkin de Referencia

```gherkin
Feature: Resolución de Bugs Journey J-02

  Scenario: BUG-J02-006 — Menú visible para ROLE_USER_INTERNAL
    Given un usuario con rol ROLE_USER_INTERNAL autenticado
    When accede al portal principal
    Then el menú lateral muestra al menos 1 grupo con items

  Scenario: BUG-J02-004 — Filtro de formularios en dropdown FormKey
    Given un proceso BPMN con patrón Simple abierto en el designer
    And se selecciona un UserTask
    When se visualiza la sección FormKey
    Then existe un toggle con opciones "Todos", "Simple" y "Maestro"
    And al seleccionar "Maestro" el dropdown muestra solo formularios maestros
    And al seleccionar "Todos" el dropdown muestra todos los formularios

  Scenario: BUG-J02-005 — Estilos CSS del dropdown FormKey
    Given un UserTask seleccionado en el BPMN designer
    When se visualiza el dropdown FormKey
    Then el select tiene bordes redondeados, sombra y focus ring visible
```

> Aplica el skill `.agents/skills/qa_e2e_validation_audit/SKILL.md` para garantizar la Ley de Correspondencia Gherkin.
