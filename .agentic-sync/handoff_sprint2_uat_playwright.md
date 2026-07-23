# 🎭 Handoff de Arquitectura: Sprint 2 (Fase UAT & E2E)

> **Destinatario:** Agente QA Automation / Frontend Lead
> **Alcance:** Aislamiento, Setup y Ejecución del End-to-End Automatizado.
> **Directiva:** Las pruebas se escriben EN FRENTE del DOM interactuando contra una infraestructura PostgreSQL/Redis real encendida físicamente vía Docker Compose (o equivalentes integrados).

---

## Bloque 1: Inicialización del Ecosistema Playwright

**Objetivo:** Instalar el Driver sin colisionar con la configuración de Vitest de la fase pasada.
**Acciones:**
1. Inicializar `@playwright/test` preferentemente en una carpeta aislada `frontend/e2e` o `tests/e2e` para no mezclar dependencias de DOM con `happy-dom`/`jsdom`.
2. Configurar `playwright.config.ts` para exponer contextos limpios de Navegadores Chromium.
3. El frontend y backend DEBEN ser servidos localmente en puertos fijos (Ej. 8080 Backend, 5173 Frontend).

---

## Bloque 2: Casos Críticos de Flujo Funcional (Happy Paths Reales)

**Objetivo:** Playwright emulará clics humanos y capturará fallos de interfaz visual (NFRs funcionales).

### 2.1 Journey Workdesk & Reclamo Concurrente (US-001 / US-002)
1. **Flujo de Reclamo (Atomic Claim):** Iniciar sesión como `Analista A`. Clic en botón "Atender" -> Esperar interceptación `200 OK` de `/claim` -> Comprobar ruteo visual a `FormDesigner` (o vista análoga).
2. **Ghost Deletion E2E (La prueba Final de Fuego):** 
   - Abrir **Contexto A** (Analista Juan) y **Contexto B** (Analista Roberto) en paralelo vía Playwright.
   - Analista Juan hace clic en "Atender" en la Tarea `T-999`.
   - Analista Roberto DEBE presenciar un re-renderizado reactivo en el DOM (remoción estricta de la fila o etiqueta de reclamado) en *menos de 1 segundo* tras el STOMP push, **sin recargar** la página (F5 prohibido en el test).

### 2.2 Validación IDE y Formularios (US-003, US-028)
1. Interactuar con los modales o grillas visuales que consumen los JSON de Zod e inyectar payloads visuales inválidos intencionalmente.

---

## Reglas de Integridad (Gate de Salida Sprint 2)
- [ ] No usar "selectores mágicos" frágiles (ej. `div > span:nth-child(3)`). Utilizar `data-testid` o Roles funcionales (ej. `getByRole('button', { name: 'Atender' })`).
- [ ] Generar Reporte de Ejecución HTML Nativo de Playwright por corrida para mi firma como Arquitecto.
