# 🧪 HANDOFF QA — US-017 CA-19 a CA-26
# Validación E2E del Toast de Monitoreo de Conexión

> **De:** Arquitecto Líder (Orquestador)
> **Para:** Agente QA (E2E / Vitest / Playwright)
> **Fecha:** 2026-04-22
> **Iteración:** Cierre Deuda Técnica Sprint 6.2
> **Rama Git:** `sprint-6/uat-certification`
> **US:** US-017 — Ejecución y Persistencia Inmutable de Formularios (CQRS & Event Sourcing)
> **CAs:** CA-19, CA-20, CA-21, CA-22, CA-23, CA-24, CA-25, CA-26

---

## 1. Alcance de Validación

8 CAs (CA-19 a CA-26) de la Sección E de US-017 que implementan un componente Toast Flotante no intrusivo para monitoreo de estado de conexión. **100% Frontend — No hay Backend involucrado.**

## 2. Endpoints y Vistas a Verificar

| Capa | Artefacto | Acción QA |
|------|-----------|-----------|
| Frontend | `ConnectionToast.vue` | Verificar renderizado, posicionamiento, textos, transiciones |
| Frontend | `connectionStore.ts` | Vitest: transiciones de estado, computed properties |
| Frontend | `useConnectionStatus.ts` | Vitest: debounce, listeners cleanup |
| Frontend | `App.vue` | Verificar montaje global del componente |

## 3. Scenarios Gherkin de Referencia

### CA-19: Debounce Visual 5s
```gherkin
Scenario: Micro-corte de red no muestra Toast
  Given el usuario está navegando el Workdesk
  When la red se desconecta y se reconecta en menos de 5 segundos
  Then el Toast de conexión NO debe mostrarse en ningún momento

Scenario: Desconexión prolongada activa el Toast
  Given el usuario está navegando el Workdesk
  When la red se desconecta y permanece offline por más de 5 segundos
  Then el Toast de conexión aparece en la esquina inferior izquierda
```

### CA-20: Posicionamiento
```gherkin
Scenario: Toast en esquina inferior izquierda
  Given el Toast de conexión está visible
  Then su posición CSS es fixed, bottom: 1.5rem, left: 1.5rem
  And su z-index es 9990
  And su ancho máximo es 320px
```

### CA-21: Lenguaje de Negocio
```gherkin
Scenario: Textos sin jerga técnica
  Given el Toast está visible en estado OFFLINE
  Then muestra el texto "Trabajando sin conexión"
  And NO contiene las palabras: CQRS, STOMP, Event Sourcing, WebSocket, Sync Eventual, Engine
```

### CA-22: Operatividad Pasiva
```gherkin
Scenario: Toast no bloquea la interfaz
  Given el Toast de desconexión está visible
  Then el usuario puede interactuar con el contenido de la página debajo del Toast
  And NO existe un overlay de pantalla completa
```

### CA-25: Desvanecimiento
```gherkin
Scenario: Toast desaparece tras reconexión
  Given el Toast está visible por desconexión
  When la red se restablece
  Then el Toast muestra "Conexión restaurada" en verde
  And después de 3 segundos se desvanece (opacity 0, 500ms)
  And el componente se desmonta (no visible en DOM)
```

### CA-26: Colisión con ErrorStateGlobal
```gherkin
Scenario: ErrorStateGlobal tiene prioridad
  Given el Toast de conexión está visible
  When se dispara un error transaccional HTTP 500
  Then el ErrorStateGlobal se renderiza con z-index 9998
  And el Toast de conexión entra en estado SILENCED y no se renderiza
```

## 4. Estrategia de Testing

### Vitest (Unitarios)
- `connectionStore.spec.ts`: Transiciones de estado (ONLINE → OFFLINE → RECONNECTING → RESTORED → invisible)
- `useConnectionStatus.spec.ts`: Debounce de 5s, cleanup de listeners

### Playwright (E2E) — Si aplica
- Emulación de estado offline del navegador
- Verificación de posicionamiento CSS
- Verificación de textos visibles
- Verificación de que NO hay overlay bloqueante

## 5. Referencia Obligatoria

Aplica el skill `.agents/skills/qa_e2e_validation_audit/SKILL.md` para garantizar la Ley de Correspondencia Gherkin (Test vs User Story). Todo CA sin test correspondiente debe reportarse como Cobertura Faltante.

---

**INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md`.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.**
3. Debes guardar tu solicitud de revisión en `.agentic-sync/approval_request_qa.md`.
4. Al grabar el archivo, detente y dile al Humano: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_qa.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera el veredicto del Arquitecto antes de ejecutar.

> 📚 **SKILLS OBLIGATORIOS:**
> - TDD: `.agents/skills/tdd_first/SKILL.md`
> - Clean Code: `.agents/skills/clean_code_standards/SKILL.md`
