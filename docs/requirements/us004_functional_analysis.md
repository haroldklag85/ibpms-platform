# Análisis Funcional: US-004 - Iniciar un Proceso mediante Webhook (Plugin O365 Listener)

**Ejecutado por:** `[⚙️ PRODUCT OWNER]` | **Fecha:** 2026-04-17
**Workflow Aplicado:** `/analisisEntendimientoUs.md`
**Fuente de Verdad (SSOT):** `docs/requirements/epics/epic_A_motor_core.md` (CA-1 al CA-10)

---

## 1. Resumen del Entendimiento
La **US-004** define la compuerta de ingesta automatizada (Intake) del iBPMS. Permite que sistemas externos, específicamente buzones de correo institucionales a través de Microsoft Graph (O365) o API Management, inyecten peticiones a la plataforma de forma autónoma. Su enfoque central es altamente receloso de la sanidad de los datos, implementando filtros, validaciones perimetrales y mecanismos anti-corrupción antes de tocar al motor BPMN (Camunda).

## 2. Objetivo Principal
Asegurar que la inyección automática de casos desde internet sea resiliente y totalmente segura para el motor de procesos, delegando a un Humano la autorización y curación pre-operativa (Intake Triage) de la solicitud antes de instanciar un verdadero "Caso de Negocio" en Camunda.

## 3. Alcance Funcional
El alcance técnico abarca **desde el perímetro de red (API de ingesta) hasta la creación de una tarea de pre-validador (Triage)** en la bandeja correspondiente:
*   **INICIA:** En la exposición de un Endpoint público REST (Webhook) para consumo de APIM / MS Graph.
*   **TERMINA:** Con la creación exitosa de un "Caso de Triaje genérico" que espera validación humana en la Pantalla 16.
*(Nota: El avance y completitud del flujo interno del caso posterior a la aprobación escapa de esta historia, al igual que la configuración lógica dentro de la suite de Microsoft).*

## 4. Lista de Funcionalidades Incluidas
La US garantiza la construcción técnica de las siguientes características obligatorias:
*   **Idempotencia Transaccional:** Rechazo en O(1) de requests repetidos usando el `id_mensaje` (CA-1). 
*   **Anti-Loops de Correos:** Filtro semántico (Hard-Block) de bots y auto-responders en la puerta, devolviendo `HTTP 400` (CA-2).
*   **Auditoría de Basura (Orphaned Payloads):** Tabla transaccional forense para payloads malformados JSON que rebotan (CA-3).
*   **Aprobación en Lista Blanca (Whitelist):** Verificación forzosa del dominio del remitente contra la base de datos de clientes habilitados (CA-4).
*   **Notificaciones Activas a TI:** Disparo de email de alerta crítica al SysAdmin si ocurre una falla interna a nivel del BPMN (CA-5).
*   **Resiliencia Total (RabbitMQ):** Almacenaje temporal en Broker/Cola si Camunda cae, eliminando pérdida de información entrante (Zero Data Loss) (CA-6).
*   **Hard-Limit Perimetral:** Bloqueo ajustable por peso de archivos adjuntos, lanzando HTTP 413 si excede la métrica de 10MB (CA-7).
*   **Intake Triage (Bandeja Previa):** Desvío de la ingesta hacia una tarea de "Pre-Triaje" humano (Pantalla 16) en lugar de automatización ciega hacia procesos complejos (CA-8 & CA-9).
*   **Seguridad Criptográfica (HMAC/Bearer):** Validación severa de llaves compartidas contra MS Graph y switch opcional para soportar sistemas legados (CA-10).

## 5. Lista de Brechas, Gaps o Ambigüedades Detectadas
Al comparar los CAs contra la consistencia global del sistema, identifico los siguientes vacíos (GAP) para graduar:
*   **GAP-1 (Manejo de Limpieza Anti-Malware):** El CA-7 estipula límite máximo de peso de anexos (10MB) pero **NO aclara** si estos archivos pasan por sanitización Anti-Malware (ClamAV) en la frontera o si se guardan "crudos" en el disco, abriendo un riesgo de inyección.
*   **GAP-2 (Administración del Whitelist):** El CA-4 habla de la validación del dominio (`@ejemplo.com`) contra los clientes matriculados. Falta definir en qué interface administrativa y módulo se añade y administra esta lista blanca.

## 6. Lista de Exclusiones (Fuera de Alcance)
*   **Procesamiento Inteligente (NLP/IA):** Mapeo inteligente de los datos libres del correo hacia variables BPMN estructuradas. Esto pertenece a las US de IA (Agents).
*   **Desarrollo en Microsoft Azure:** Configuración perimetral de Microsoft 365, Power Automate o Logic Apps. La US incluye solo la API Endpoint receptora pasiva alojada en iBPMS.
*   **Autoruta Ciega (Straight-Through Processing):** Iniciación directa de flujos de negocio sin aprobación humana; la política del CA-8 fuerza obligatoriamente el Triaje Humano, por lo tanto, la autoruta está vedada en esta V1.

## 7. Observaciones de Alineación o Riesgos
### Clasificación MoSCoW
*   **Must Have:** Central para las integraciones corporativas pasivas del sistema. Bloque innegociable de la V1.

### Resumen de Dependencias con otras User Stories
*   **Dependencia con US-001 (Workdesk de Pendientes):** Existe un vacío funcional de visibilidad (GAP-3). La tarea del Pre-Triaje (CA-8/9) se pinta en la Pantalla 16 (Intake Triage), pero sus SLAs y semáforos DEBEN consolidarse centralizadamente visuales también en la grilla maestra construida por la US-001. De lo contrario, los operadores ignorarán los correos entrantes.
*   **Dependencia con US-036 (RBAC & Portal Administrativo):** Para que las validaciones del CA-4 (Whitelist de dominios) y CA-7 (Límite de 10MB parametrizable) y CA-10 (switch HMAC a Bearer) puedan ser controlados, la US-036 debe exponer formularios reactivos de configuración (Variables de Tenant).
*   **Dependencia con US-000 (Arquitectura Transversal):** La estrategia de captura de errores silenciosa e impedimentos HTTP de inyección basura (CA-1, CA-2 y CA-3) utilizarán obligatoriamente las lógicas de atrapadores (ExceptionHandler) estructuradas por la US-000 de arquitectura base.

### Dependencia Bloqueante Absoluta (Riesgo Técnico)
*   La historia detalla un proceso Event-Driven **(RabbitMQ / Cola de contingencia CA-6)**. El equipo de Backend NO PUEDE proceder con el esquema transaccional de esta US si la infraestructura del Broker (Exchanges y DLQs) no ha sido desplegada en Kubernetes/Docker previamente. Si Camunda falla y el DLQ no existe, perderíamos todos los Webhooks encolados irremediablemente.
