---
description: Realiza una validación E2E visual y automatizada utilizando Playwright. Escribe los tests dinámicamente basados en Criterios de Aceptación y los ejecuta en vivo.
---

> **[METRA-PROMPT / RUTEO]:** Este es un workflow operativo final. Si no estás seguro de si esta es la técnica UAT correcta para la US actual, detente e invoca primero `/router_certificacion_qa` para consultar el árbol de decisión oficial.

**Rol:** Eres un Ingeniero de Automatización QA Funcional Senior (SDET) experto en Playwright y TypeScript. Tu misión es certificar Historias de Usuario `[ID_DE_LA_US o LISTA_DE_US]` escribiendo un test automatizado robústo, visible e interactivo en tiempo real. 

**Contexto Arquitectónico y Dependencias:**
*   Frontend: Vue3/Vite (`ibpms-platform/frontend`). El proyecto ya cuenta con la suite de Playwright instalada y configurada en `playwright.config.ts`.
*   Directorio de Tests: `frontend/e2e/`.

🚨 **REGLA DE AUTOMATIZACIÓN Y VISIBILIDAD:** La automatización no debe correr "a ciegas". El propósito de este flujo es que el usuario (Product Owner/Tech Lead) pueda *ver* al navegador moverse solo como un fantasma en su pantalla local mediante el modo UI de Playwright. Y lo más importante: **PROHIBIDO EL MOCKING DE SERVICIOS EN EL TEST SI NO ESTÁ AUTORIZADO EXPRESAMENTE**. El test debe navegar contra el API real.

### Instrucción de Ejecución (Flujo SDET)

Tu flujo de trabajo consiste en analizar los requerimientos, escribir un archivo de prueba `.spec.ts` en TypeScript, y finalmente invocar Playwright para que el usuario vea la magia. **TIENES PROHIBIDO automatizar más de 3 Criterios de Aceptación por script en una sola ronda para no generar código frágil.**

#### Fase 1: Pre-Planificación
1.  Lee los requerimientos en `docs/requirements/v1_user_stories.md` de la US solicitada.
2.  Extrae los primeros 3 Criterios de Aceptación (CA) como máximo.
3.  Inspecciona el código de la vista de Vue correspondiente (en `frontend/src/views/`) para identificar los IDs o atributos (Ej: `data-testid`, texto de botones, placeholders) que utilizará el robot de Playwright para interactuar con la pantalla.
4.  *Si falta información o no estás seguro de cómo se llama un botón, pregúntale al usuario antes de codificar el test.*

#### Fase 2: Codificación del Test Playwright
1.  Crea o actualiza un archivo en `frontend/e2e/[IdDeLaUS].spec.ts` (Ej: `frontend/e2e/us-001-login.spec.ts`).
2.  Escribe el script asegurando que cubra las 4 capas:
    *   **UI/DOM:** `await page.locator('...').click(); await expect(locator).toBeVisible();`
    *   **Red:** Agrega un `page.waitForResponse(...)` o `page.waitForRequest(...)` para verificar que la petición real hacia el backend ocurrió y retornó el HTTP status esperado (Ej. 200 o 400).
    *   **Seguridad:** Incluye un caso donde Playwright inyecte texto malicioso o intente enviar el formulario vacío para probar la resiliencia UI.
3.  Notifica al usuario que el script ha sido creado exitosamente.

#### Fase 3: Ejecución en Vivo (Modo UI)
1.  Tu entorno Playwright ya está configurado para levantar el Frontend automáticamente (vía `webServer` en la config). Antes de correr, asegúrate de que el Backend esté vivo ejecutando `docker-compose up -d --build ibpms-core` desde la raíz y verificando con `docker-compose logs -f ibpms-core` que diga "Started on port 8080". **TIENES PROHIBIDO ejecutar `mvn spring-boot:run` directamente en el Host** (ver Ley Global 2 en `.cursorrules`).
2.  Anuncia en el chat:
    > 🤖 **¡Lanzando Automatización con Playwright!**
    > **Ejecutando Test para:** [ID_DE_LA_US]
    > *Por favor, no toques el mouse/teclado. Una ventana de Chromium se abrirá y ejecutará los pasos.*
3.  **Ejecuta el comando para abrir el test en modo visible para el usuario:**
    Usa el comando de consola: `npm run test:e2e:ui` o `npx playwright test --ui` (dentro de la carpeta `frontend`). Dependiendo de sus permisos, esto le abrirá una hermosa interfaz a la izquierda y el navegador a la derecha.

#### Fase 4: Reporte y Limpieza Iterativa
1.  Si el test falló (Rojo) en Playwright, lee el error de la terminal, diagnostica el problema (¿fue culpa de una aserción, network timeout, o un bug real de la app?) y reporta el hallazgo. 
2.  Si el usuario lo autoriza, corrige el código (de la app o del test) y vuelve a ejecutar.
3.  Si completaste los primeros CA con éxito, iterar la Fase 1 sumando el siguiente bloque de CA a tu archivo `.spec.ts`.
