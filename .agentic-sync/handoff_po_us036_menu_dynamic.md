# 📬 HANDOFF: Requerimiento de Producto (Product Owner)
**Fecha:** 2026-04-22
**De:** Equipo de Arquitectura / QA
**Para:** Product Owner (PO)
**Asunto:** Solicitud de Inclusión de Criterios de Aceptación (CAs) en US-036 — Gobernanza Dinámica de la Topología Visual (Menú Lateral)

---

## 1. Contexto y Hallazgo de Auditoría
Durante la auditoría de certificación de la **US-051 (Gobernanza Visual)** y la **US-036 (Identity Governance)**, identificamos una "Decisión Fantasma" (Scope Cut no documentado) introducida por el equipo de desarrollo técnico. 

Actualmente, si el CISO usa la Pantalla 14 (US-036) para forjar un nuevo rol (Ej: `R_LIDER_PROYECTOS`), **no existe ninguna forma en la UI de otorgarle acceso a los menús estructurales de la barra lateral** (como *Proyectos* o *Integración*). 

Esto ocurre porque el equipo de desarrollo, presuntamente por restricciones de tiempo en la V1, dejó "quemados" (Hardcoded) los permisos de estos menús en el código fuente del Frontend (`useMenuStore.ts`), exigiendo explícitamente poseer roles fundacionales como `SUPER_ADMIN` para desbloquear la barra lateral.

## 2. Solicitud de Acción (Action Item)
Se solicita oficialmente al PO la redacción e inclusión de nuevos Criterios de Aceptación (CAs) explícitos en la historia de usuario **US-036 (Identity Governance)**. 

Estos CAs deben definir claramente las reglas de negocio sobre cómo el CISO asignará el acceso a los 7 módulos topológicos de la plataforma al momento de crear o editar un rol, cerrando la brecha arquitectónica actual y obligando al equipo de desarrollo a eliminar la deuda técnica (Hardcoding).

---

## 3. Propuesta Base de Criterios de Aceptación (Gherkin)
Para facilitar su labor de refinamiento, sugerimos incorporar los siguientes escenarios en la **US-036**:

```gherkin
  Scenario: Asignación Granular de Módulos Estructurales en Fábrica de Roles (Topología de Menú)
    Given que el CISO (Super Admin) abre el modal "Forjar Nuevo Rol Transversal" en la Pantalla 14
    When configura el nombre y la herencia del nuevo rol
    Then el sistema debe desplegar una nueva sección visual llamada "Acceso a Módulos Globales (Topología UI)"
    And esta sección listará mediante Checkboxes los 7 grupos estructurales principales:
        - [ ] Operativo (Workdesk)
        - [ ] Service Delivery
        - [ ] Directivo (BAM)
        - [ ] Configuración (Modeler)
        - [ ] Integración
        - [ ] Proyectos
        - [ ] Administración
    And el CISO podrá marcar explícitamente cuáles módulos estarán visibles en la barra lateral para los usuarios que posean este rol.

  Scenario: Inyección Dinámica de la Barra Lateral basada en la Matriz RBAC (Anti-Hardcoding)
    Given que el CISO asignó acceso al menú "Proyectos" a un rol personalizado "R_LIDER"
    When un usuario con dicho rol inicia sesión
    Then el Frontend consumirá el endpoint dinámico `/api/v1/menu-layout` (o se inyectará en el JWT)
    And el Backend calculará el cruce de permisos del usuario y retornará un JSON indicando que la carpeta "Proyectos" DEBE renderizarse.
    And el motor de Vue.js construirá la barra lateral basado 100% en la configuración del Backend, quedando estrictamente prohibido el uso de roles Hardcoded en el router o stores (useMenuStore.ts).
```

## 4. Impacto Arquitectónico y Dependencias Técnicas
La aprobación de estos CAs detonará las siguientes tareas de desarrollo (TDD):
* **Backend:** Requiere materializar el endpoint `/api/v1/menu-layout` o incluir la matriz Topológica como un array de strings (Claims) dentro del Token JWT al momento del Login.
* **Frontend:** Requiere eliminar el objeto estático `.catch()` en `useMenuStore.ts` e inyectar Checkboxes adicionales en el modal de creación de roles de la Pantalla 14 (`IdentityGovernance.vue`).

---
**Firma:** *QA & Architectural Audit Team*
