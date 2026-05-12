# Handoff: Certificación E2E de J-04 (Kanban, Break-Glass, Webhook)

**Destinatario:** [🕵️ QA - E2E]
**Emitido por:** [🧠 ARQUITECTO LÍDER]
**Fecha:** 2026-05-11
**Contexto:** Pruebas finales para sellar el Sprint J-04.

## 🎯 Objetivo Principal (Misión)
El Backend y Frontend han cerrado la deuda técnica arquitectónica. Tu misión es certificar estas implementaciones ejecutando pruebas automatizadas que garanticen la funcionalidad bajo infraestructura real y validar que la LEY GLOBAL 2 (Zero-Mock Compilation) y el ADR-011 se cumplen rigurosamente.

## 🛠️ Especificaciones Técnicas a Certificar

### 1. Tablero Kanban (US-008)
- **Validación E2E:** Asegúrate de probar el Drag & Drop en el tablero interactivo. Verifica que mover una tarea a la columna `BLOCKED` despliegue correctamente un modal pidiendo la justificación.
- **Validación de Persistencia:** Verifica que el Frontend ahora consume el endpoint real `PATCH /api/v1/kanban-tasks/tasks/{id}/state` y no usa simuladores locales.

### 2. Kill-Switch / Mass Deallocation (US-036 / US-038)
- **Validación E2E:** El equipo Frontend ha implementado (o está por integrar) un botón "Kill-Switch" o "Exorcización" dentro de la consola administrativa. Debes crear un script en Playwright que ingrese como `SUPER_ADMIN`, ubique a un usuario específico y lance la acción destructiva de revocación de sesión.
- **Comprobación:** Asegúrate de que, una vez ejecutado, los requests subsecuentes de ese usuario reciban un error HTTP 401 (debido a la revocación efectiva de sus JWTs a través de Redis en el backend).

### 3. Webhook Intake de Seguridad (US-004)
- **Validación E2E:** Asegura que enviar datos a `POST /inbound/email-webhook` retorne `HTTP 410 GONE`. Verifica que el sistema nuevo basado en RabbitMQ (`POST /api/v1/intake/webhook`) esté operando y el payload de prueba sea encolado y consumido correctamente por el `WebhookIntakeListener`.

---

## 🛑 REGLAS DE GOBERNANZA Y SKILLS (OBLIGATORIAS)

### 0. Skills de Ejecución (OBLIGATORIO LEER ANTES DE CODIFICAR)
Para asegurar que tu ejecución se adhiera a las directrices de la plataforma, **debes leer y aplicar estrictamente** el siguiente skill de QA:
- **`cat .agents/skills/qa_e2e_validation_audit/SKILL.md`** (Auditoría y Validación E2E con Playwright)

### 1. ADR-011: Gobernanza de Pirámide de Testing
Tienes **estrictamente prohibido certificar validez con tests basados en bases de datos en memoria (H2) o infraestructuras mockeadas.** 
- Los tests de Playwright deben correr contra la instancia de base de datos aprovisionada mediante tu orquestador en `docker-compose.e2e.yml`.
- Usa el `seed-e2e.sql` disponible (ubicado en `src/main/resources/seed-e2e.sql`) para inicializar el estado del ambiente antes de ejecutar los tests de interfaz de usuario.

### 2. Trazabilidad Inversa y Documentación
Recuerda documentar tus scripts con los trazadores correctos.
```typescript
// @Traceability: US-036, US-008
test('US-036: Un SUPER_ADMIN puede expulsar a un operario usando el Mass Deallocation', async ({ page }) => { ... });
```
Además, deberás reportar si todos los criterios de aceptación Gherkin han sido satisfechos al pie de la letra, consultando siempre los documentos SSOT.

---
## 📋 Tareas Puntuales para QA
1. Revisar los scripts de E2E Playwright actuales e integrar las pruebas para `BLOCKED` Kanban y la nueva UI del *Break-Glass*.
2. Asegurar que las dependencias de RabbitMQ y Postgres (vía `docker-compose.e2e.yml`) levantan satisfactoriamente y corren el `seed-e2e.sql`.
3. Ejecutar los scripts localmente y corroborar que devuelvan `PASS` sin intervención de mocks de red (`route.fulfill()`).
4. Documentar los resultados y dar cierre final a esta certificación.

¡Confío en tu rigor! Recuerda empezar tu respuesta asumiendo tu rol [🕵️ QA - E2E] y confirmar tu plan de pruebas.
