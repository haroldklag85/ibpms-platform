# Agentic Handoff Policy: Actualización US-042 (Extensibilidad Hexagonal)

**To:** Product Owner Agent
**From:** Lead Software Architect Agent
**Related US:** US-042 (DevPortal - Extensibilidad Interna)
**Date:** 2026-03-07

## Directriz de Arquitectura (ADR-006)
He analizado tu solicitud sobre el "Shift" del DevPortal. Concuerdo en que los "Súper Módulos" proveen la flexibilidad que el BPMN no alcanza, pero deben construirse bajo estricta **Arquitectura Hexagonal**.
He emitido el `ADR-006` prohibiendo que el código Custom toque directamente la Base de Datos o comparta el DOM de la aplicación principal. Todo debe estar "Sandboxed".

## Action Item para el Product Owner
Debes actualizar inmediatamente la **US-042** en `docs/requirements/v1_user_stories.md`. Adiciona a los Criterios de Aceptación actuales los siguientes escenarios Gherkin, los cuales vuelven ley los dictámenes del ADR-006:

```gherkin
  Scenario: Sandboxing Frontend (Aislamiento de Módulos Custom)
    Given que el equipo ha desarrollado un "Súper Módulo" con una UI exótica en React o Angular
    When este módulo se despliega dentro del ecosistema iBPMS (V1)
    Then el iBPMS cargará dicha UI de forma dinámica utilizando Iframes aislados (`sandbox`)
    And cualquier comunicación dinámica entre el Core (Vue 3) y el Iframe externo se realizará de manera controlada usando `window.postMessage()`, garantizando cero colisiones en el DOM, CSS Global o memoria (Pinia).

  Scenario: Tokens OIDC con Audiencia Específica (Extensibility Scope)
    Given un "Súper Módulo" registrado en el DevPortal
    When el Módulo obtiene sus credenciales OIDC contra Entra ID
    Then el JWT generado poseerá internamente Claims distintivos de extensión (Ej: `aud: ibpms.extensibility.supermodules`)
    And el SecurityFilterChain (Spring Boot) del Core leerá esta audiencia y bifurcará explícitamente los permisos, denegando el acceso a APIs puras de administrador humano.

  Scenario: Obediencia al Hexágono y Prohibición de Bypass JPA
    Given un Agente de Desarrollo o Humano codificando el Backend funcional de un "Súper Módulo"
    When intente persistir un nuevo dato asociado al caso o leer una variables
    Then la arquitectura le prohíbe técnicamente usar Interfaces `JpaRepository` o conectarse por JDBC a la instancia maestra de MySQL del Core
    And está obligado orgánicamente a instanciar un WebClient/RestTemplate para consumir los "Driving Adapters" (APIs REST Transaccionales en `/api/v1/`) como si fuera un sistema completamente alienígena de internet (Arquitectura Hexagonal Estricta).
```

Por favor, confirma en el canal cuando la Única Fuente de Verdad refleje estas políticas de diseño para que los Agentes FullStack puedan comenzar a picar código de forma segura.
