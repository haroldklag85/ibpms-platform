### 📩 Mensaje para el Arquitecto Líder 
_*(Copia y envíale esto al Líder Técnico de tu equipo de desarrollo)*_

**Asunto: Ejecución del Engineering Blueprint (Pantalla 15) - Administración de Orígenes SAC**

Hola Arquitecto,

Acabamos de sanear una brecha estructural crítica en nuestra arquitectura V1. Todo el flujo de Recepción Inteligente (Intake) y el Agente MLOps asumían que "los correos llegaban solos", pero carecíamos de la tubería técnica para que el área de Infraestructura configure esas conexiones. 

Acabamos de despachar la **Épica 13 (US-037)** y su Blueprint de Implementación respectivo (`implementation_plan_pantalla15.md`). Este módulo (Pantalla 15) es el CRUD administrativo donde inyectaremos las Credenciales de OAuth (MS Graph) y configuraremos el Polling de Exchange.

**Reglas de oro que notarás en el Blueprint:**
1. **Seguridad Total (Refinada):** Los *Client Secrets* de Exchange van externalizados a Azure Key Vault, manteniendo la base de datos estéril y segura.
2. **Ping en Vivo:** Nadie puede guardar un buzón si el API de Microsoft no devuelve un HTTP 200 en ese preciso instante.
3. **Réplica Operativa:** Queda rotundamente prohibido hacer un `DELETE` en Exchange por el hecho de "leer" un correo. Funcionaremos con "Soft-Deletes" a la Papelera de Exchange solo si un humano elimina la tarea en el iBPMS.

A continuación, he preparado el Prompt formalizado para inyectar a tu escuadrón de Agentes de Desarrollo (Back, Front, QA y DevOps). Por favor, córrelo en su entorno.

---

### 🚀 Prompt para el Escuadrón de Agentes (Back / Front / QA / DevOps)
_*(Copia y pega este bloque en el input de tu equipo de Agentes de Software)*_

**[INICIO DEL PROMPT]**

**Objetivo de la Misión:**
Equipo, hemos liberado el Blueprint Técnico para la **Épica 13 (US-037): Configuración Administrativa de Buzones SAC (Pantalla 15)**. Su trabajo es traducir los requerimientos arquitectónicos en código puro (Spring Boot + Vue 3).

**Documentos de Referencia:**
*   `implementation_plan_pantalla15.md` -> (Lean este archivo primero, tiene toda la estructura técnica).
*   `v1_user_stories.md` (Buscar "Épica 13" / "CA-1 a CA-10").

**Distribución de Tareas:**

**🛠️ 1. Agente Backend (Java / Spring Boot):**
*   **Entidad y Seguridad:** Crea la tabla `SacMailbox.java`. Integra **Azure Key Vault** para almacenar el `clientSecret` de forma segura, guardando únicamente el `keyVaultReferenceId` en la tabla de la Base de Datos relacional. Queda prohibido guardar el secreto en texto plano o encriptado directamente en la BD.
*   **Gateway de Conexión:** Desarrolla el `MailboxConnectionManager.java`. Si el request viene con `protocol=GRAPH`, el código debe intentar autenticarse en vivo contra `https://graph.microsoft.com`. Si Microsoft rechaza las credenciales, debes lanzar `ConnectionValidationException` (HTTP 400).
*   **El Motor (Polling):** Escribe el CronJob asíncrono `@Scheduled` cada 5 minutos. Debe tener un *Redis Lock* (`DistributedLock`) para evitar doble-lectura. Implementa la regla "Fallback" (CA-3): Si falla la IA del Agente 3, instancia el `defaultBpmnProcessId`.
*   **Soft Delete Event:** Súscribete al evento de borrado de folios e implementa la orden para que Microsoft mueva el correo original a "Elementos Eliminados". ¡NADA DE HARD-DELETES!

**💅 2. Agente Frontend (Vue 3 / TypeScript):**
*   **Vista y Ruteo:** Crea `/views/admin/SacConfigManager.vue` bajo la Pantalla 15.
*   **Formulario Reactivo:** Implementa el CRUD para las variables del buzón (Alias, Tenants, Secrets). Muestra una advertencia visual (Banner/Alerta) estricta indicando que el protocolo IMAP está **deprecado por Microsoft**, e impone el flujo OAuth 2.0 (Graph API) impidiendo la inserción de puertos inseguros.
*   **UX (Botones de Seguridad):** El botón `[Guardar]` debe nacer deshabilitado. Solo se habilita si el usuario presiona `[🧪 Probar Conexión]` y el Backend devuelve HTTP 200.
*   **Toggle de Emergencia (CA-8):** Agrega un switch en la grilla para "Pausar" activamente la succión de correos de un tenant rebelde.

**🧪 3. Agente QA (Automatización de Pruebas):**
*   **Vitest (UI):** Renderiza el Componente `.vue` usando `@vue/test-utils` y `vitest`. Simula escribir datos basura en los inputs y asegúrate de hacer un `expect(saveButton.isDisabled).toBe(true)`.
*   **JUnit (Back):** Escribe la prueba unitaria `MailboxConnectionManagerTest.java`. Inyecta un token Microsoft falso (MockWebServer) y valídame que lance la excepción impidiendo el guardado en base de datos.
*   **Integration:** Simula un correo procesado fallidamente por la IA con *Nivel de Certeza 0%*, y aserta que el framework de Camunda arranca automáticamente el `defaultBpmnProcessId`.

**☁️ 4. Agente DevOps (CI / CD & Infraestructura):**
*   **Infraestructura Local:** Modifica el `docker-compose.yml` local añadiendo una imagen base de **Redis Cache** (requerida obligatoriamente para el `DistributedLock` de Spring Boot y el polling concurrente).
*   **Variables de Entorno (Azure Pipelines):** Configura la inyección de los secretos troncales de MS Graph y la conectividad a nivel de Red/Permisos hacia Azure Key Vault en los pipelines YAML de Release correspondientes al Back-end.

**🤝 Protocolo de Sincronización Multi-Agente (Handoff & .agentic-sync):**
Para garantizar la estrategia de "Vertical Slice" y cero pérdida de memoria en este desarrollo, ustedes (los agentes) deben cumplir este protocolo estricto:
1. **Directorio `.agentic-sync/`:** El Agente Backend debe depositar allí la documentación exacta del contrato API generado (endpoints, firmas y payloads DTO) una vez implementado el `MailboxConnectionManager.java`.
2. **Regla de Handoff (API-First):** El Agente Frontend NO DEBE comenzar a codificar la vista `SacConfigManager.vue` hasta que el Agente Backend confirme un "BUILD SUCCESS" y haya escrito el contrato en `.agentic-sync/`.
3. **Zero-Trust Output:** Las tareas no se cierran de forma conversacional. El Agente QA y DevOps deben anexar un reporte forense en `.agentic-sync/` demostrando gráficamente que las suites `vitest` y `mvn test` retornaron *Exit Code 0*.

Por favor, procedan escalonadamente respetando el protocolo Handoff y repórtense cuando el flujo End-to-End esté validado.
**[FIN DEL PROMPT]**
