# Refinamiento Funcional: US-004 — Iniciar un Proceso mediante Webhook (Plugin O365 Listener)

**Ejecutado por:** `[⚙️ PRODUCT OWNER]` | **Fecha:** 2026-04-17
**Workflow Aplicado:** `/refinamientoFuncionalUs.md`
**Fuente de Verdad (SSOT):** `docs/requirements/epics/epic_A_motor_core.md` (CA-1 al CA-12)
**Análisis Previo:** `docs/requirements/us004_functional_analysis.md` (GAP-1: Anti-Malware, GAP-2: Whitelist Admin)

---

## 1. Adecuación Funcional (20 Preguntas)

**1.** El CA-1 define idempotencia por `id_mensaje`. ¿El campo `id_mensaje` es generado por Microsoft Graph (Message-ID del correo) o es un UUID fabricado por nuestro APIM intermedio? Si es el Message-ID nativo de Exchange, ¿qué ocurre cuando el mismo correo llega desde dos buzones diferentes (shared mailbox + personal mailbox) con el mismo Message-ID?

**2.** El CA-1 devuelve `HTTP 200 OK` silencioso a los duplicados. ¿No debería devolver `HTTP 202 Accepted` para indicar que ya fue recibido previamente y está en proceso, diferenciándolo semánticamente del 200 del primer procesamiento exitoso? ¿O el emisor (Graph) no distingue entre ambos?

**3.** El CA-2 bloquea patrones `no-reply@`, `mailer-daemon@`. ¿Existe una lista exhaustiva y administrable de estos patrones, o se hardcodean en el código? ¿Qué pasa con variantes como `noreply@`, `do-not-reply@`, `bounced@`, `postmaster@`? ¿Debería esta lista vivir en la misma tabla de Whitelist del CA-12 pero como "Blacklist"?

**4.** El CA-3 persiste payloads malformados en la tabla de "Payloads Huérfanos". ¿Cuál es la política de retención de estos registros? ¿Se purgan automáticamente después de 30/60/90 días, o se acumulan indefinidamente generando deuda de almacenamiento?

**5.** El CA-3 guarda el payload completo del JSON malformado. Si el JSON malformado contiene datos PII (nombres, correos, documentos de identidad), ¿se aplica el enmascaramiento PII de la US-000 antes de persistirlo en la tabla forense, o la auditoría forense justifica almacenar PII en claro?

**6.** El CA-4 valida el dominio del remitente. ¿Qué ocurre con correos reenviados (forwarded) donde el "From" original es de un dominio autorizado pero el "Sender" intermediario no lo es? ¿Se valida el campo `From`, el `Sender`, o el `Return-Path`?

**7.** El CA-4 rechaza con `HTTP 403 Forbidden` si el dominio no coincide. ¿Este rechazo se registra también en la tabla de Payloads Huérfanos del CA-3, o solo se genera un log? Sin el registro en tabla, los intentos de acceso no autorizado desaparecerían del radar forense.

**8.** El CA-5 dispara un correo de alerta al SysAdmin cuando Camunda falla. ¿Qué mecanismo de envío se usa? ¿Si Camunda cae y simultáneamente el servicio de correo (SMTP/Exchange) también está caído, existe un fallback (Ej: Slack, Teams, SMS)?

**9.** El CA-5 dice "dispara inmediatamente un correo". ¿Se aplica rate-limiting a estas alertas? Si Camunda está intermitente y fallan 200 Webhooks en 10 minutos, ¿el SysAdmin recibiría 200 correos individuales? ¿O se agrupan en un digest cada N minutos?

**10.** El CA-6 encola payloads en RabbitMQ cuando Camunda cae. ¿Cuál es el orden de procesamiento al recuperarse? ¿FIFO estricto (respetando el orden de llegada original)? ¿Qué pasa si durante la indisponibilidad de Camunda un dominio fue removido de la Whitelist (CA-4/CA-12)? ¿Se re-valida la Whitelist al desencolanprocesar, o se asume que ya fue validado previamente?

**11.** El CA-6 habla de "cero pérdida de información". ¿Cuál es la capacidad máxima de la cola (en número de mensajes o MB)? ¿Existe un Dead Letter Exchange (DLX) configurado para mensajes que fallan incluso después de N reintentos al recuperarse Camunda?

**12.** El CA-7 impone un límite de 10MB. ¿Este límite aplica al peso total del payload HTTP (body completo incluyendo JSON + Base64 de adjuntos), o específicamente al peso binario descodificado de los archivos? Un archivo de 7MB en Base64 ocuparía ~9.3MB en el payload.

**13.** El CA-8/CA-9 definen que el Webhook genera una "Tarea de Pre-Triaje" en la Pantalla 16. ¿Qué estructura de datos contiene esta tarea de triaje? ¿Se extrae automáticamente del correo: asunto como título, cuerpo como descripción, adjuntos como anexos? ¿O llega como un blob opaco que el operario debe abrir y descifrar manualmente?

**14.** El CA-8/CA-9 obligan aprobación humana. ¿Qué opciones tiene el operario en la Pantalla 16 además de [Aprobar y Crear Caso]? ¿Existe un botón [Rechazar]? ¿Y [Solicitar Información Adicional] que genere una respuesta automática al remitente del correo? ¿Simplemente se archiva? ¿Qué estados de vida tiene la tarea de triaje?

**15.** El CA-8/CA-9 mencionan que el operario oprime [Aprobar y Crear Caso]. ¿El operario puede elegir CUÁL proceso BPMN instanciar (Ej: "Onboarding", "Reclamo", "Solicitud de Crédito"), o el sistema lo pre-selecciona basándose en reglas del correo? Si lo elige el operario, ¿de dónde aparece la lista de procesos disponibles?

**16.** ¿Las tareas de Pre-Triaje (Pantalla 16) tienen SLA propio? Si un correo llega a las 8:00 AM y nadie lo aprueba en 4 horas, ¿se escala automáticamente? ¿Se aplica el mismo semáforo SLA del Workdesk (US-001 CA-5) a estas tareas de intake?

**17.** El CA-10 define seguridad HMAC con un "Secreto compartido con Microsoft Graph". ¿Dónde se almacena este secreto? ¿En Azure Key Vault referenciado en `application.yml` (consistente con la infraestructura existente), o en la base de datos del tenant? ¿Es rotable sin downtime?

**18.** *(Cierre GAP-1)* El CA-11 de remediación define ClamAV como escáner. ¿El escaneo es bloqueante o async? Si un adjunto de 9MB demora 15 segundos en escanearse, ¿el Webhook queda en hold devolviendo un timeout al emisor (Graph)? ¿Debería el escaneo moverse a un pipeline asíncrono post-encolamiento?

**19.** *(Cierre GAP-2)* El CA-12 define la Whitelist como una tabla por tenant. ¿Un dominio puede estar autorizado para un tenant pero bloqueado para otro? ¿O la Whitelist es global? Si es por tenant, ¿cómo se determina a cuál tenant pertenece un Webhook entrante antes de validar la Whitelist?

**20.** *(Cierre GAP-2)* El CA-12 define caché Redis con TTL de 5 minutos. Si un administrador agrega un dominio urgente y el operador de un Webhook llega en los siguientes 4 minutos, la caché aún no refrescó. ¿Existe un mecanismo de invalidación inmediata de caché al hacer POST/DELETE en la Whitelist?

---

## 2. Seguridad y Hardening (10 Preguntas)

**21.** El endpoint del Webhook está expuesto a internet. ¿Se implementa rate-limiting a nivel de IP/API Gateway ANTES de la validación HMAC (CA-10)? ¿Cuántas peticiones por minuto por IP se permiten antes de devolver `HTTP 429`?

**22.** El CA-10 define HMAC como mecanismo de autenticación primario. ¿Se valida también el timestamp del request para prevenir ataques de replay? (Ej: rechazar requests con más de 5 minutos de antigüedad en su header `X-Timestamp`).

**23.** Si el switch del CA-10 desactiva HMAC y solo usa Bearer Tokens (modo Legacy), ¿qué tipo de token se acepta? ¿JWT validado contra un JWKS endpoint, o un API Key estático? Si es API Key estática, ¿se rota periódicamente?

**24.** El CA-3 persiste payloads malformados para auditoría. ¿Se aplica un límite de tamaño al payload que se persiste en la tabla forense? Un atacante podría enviar payloads de 100MB con JSON inválido, causando un ataque de almacenamiento (Storage Bombing).

**25.** ¿Se implementa validación de Content-Type estricto? El endpoint solo debería aceptar `application/json`. ¿Qué ocurre si se envía `multipart/form-data` o `text/xml`? ¿Se rechaza con `HTTP 415 Unsupported Media Type` antes de procesar?

**26.** El CA-11 define almacenamiento del hash SHA-256 de archivos. ¿Se usa este hash para detectar si el mismo archivo fue enviado previamente por otro remitente (deduplicación de adjuntos cross-tenant)? ¿O la deduplicación solo opera dentro del mismo tenant?

**27.** ¿El endpoint del Webhook requiere HTTPS exclusivo (TLS 1.2+), o acepta conexiones HTTP planas? ¿Existe un redirect forzado de HTTP a HTTPS o un rechazo directo?

**28.** El CA-2 bloquea auto-responders por patrón de email. ¿Se valida también el header `Auto-Submitted` (RFC 3834) que los servidores de correo estándar incluyen en respuestas automáticas? Este header es más confiable que los patrones de dirección.

**29.** ¿El payload del Webhook se valida contra un JSON Schema estricto (Ej: OpenAPI Schema Validator) antes de cualquier procesamiento de negocio? ¿O solo se valida que sea "JSON parseable" sin estructura definida?

**30.** El CA-12 define que solo `ADMIN_SISTEMA` y `ADMIN_TENANT` pueden administrar la Whitelist. ¿El endpoint de administración de Whitelist vive en la misma API que el Webhook público, o en un API Gateway separado (interno vs externo)? Si comparte API, ¿un atacante que comprometa el endpoint público podría escalar hacia los endpoints administrativos?

---

## 3. Experiencia de Usuario — UX/UI (10 Preguntas)

**31.** La Pantalla 16 (Intake Triage) recibe las tareas de Pre-Triaje. ¿Cómo se diferencia visualmente de la Pantalla 1 (Workdesk)? ¿Es una pestaña adicional dentro del Workdesk, una vista completamente separada en el menú lateral, o un widget embebido?

**32.** Cuando el operario abre una tarea de Pre-Triaje en la Pantalla 16, ¿se muestra una previsualización (preview) del cuerpo del correo original con formato HTML enriquecido (negritas, imágenes inline, tablas), o solo se muestra texto plano sanitizado?

**33.** ¿Los archivos adjuntos del Webhook son descargables directamente desde la Pantalla 16, o el operario debe hacer clic en un link y abrirlos en una pestaña nueva? ¿Existe una previsualización inline para PDFs y imágenes (thumbnails)?

**34.** ¿La Pantalla 16 muestra indicadores visuales del estado de seguridad del payload? Ej: un badge "✅ HMAC Verificado", "✅ Anti-Malware: CLEAN", "✅ Dominio Autorizado" para darle confianza al operario de que el correo pasó todas las validaciones perimetrales.

**35.** Si el operario presiona [Rechazar] en la tarea de triaje (si existe esta opción), ¿qué feedback visual recibe? ¿Un modal de confirmación con campo de motivo obligatorio? ¿Se genera alguna respuesta automática al remitente del correo notificándole del rechazo?

**36.** ¿Las tareas de Pre-Triaje en la Pantalla 16 soportan el Reclamo de la US-002 (Claim)? Si hay 5 operarios de intake autorizados, ¿pueden pisarse reclamando la misma tarea de triaje? ¿O la Pantalla 16 opera con asignación automática round-robin?

**37.** ¿La Pantalla 11 (Hub de Integraciones) muestra métricas en tiempo real del Webhook? Ej: "Webhooks recibidos hoy: 142", "Rechazados por Whitelist: 7", "En cola (Camunda caído): 0", "Malware detectado: 1". ¿Estos contadores son informativos o filtrables?

**38.** ¿Existe algún estado de carga (skeleton/spinner) visible al operario mientras el payload del Webhook se encola en RabbitMQ (CA-6)? ¿O este proceso es completamente invisible y asíncrono desde la perspectiva de la UI?

**39.** ¿La Pantalla 16 diferencia visualmente las tareas de triaje por canal de origen? Ej: "📧 Correo O365", "🔗 API REST Directa", "🤖 Power Automate". ¿O todos los Webhooks se ven iguales?

**40.** Cuando el operario aprueba y selecciona el proceso BPMN a instanciar (Pregunta #15), ¿se le muestra un formulario de mapeo donde pueda asignar campos del correo (asunto, remitente, cuerpo) a variables del proceso BPMN? ¿O el mapeo es automático y preconfigurado?

---

## 4. Eficiencia de Desempeño (5 Preguntas)

**41.** ¿Cuál es el tiempo máximo de respuesta aceptable (SLA técnico) del endpoint de Webhook desde la recepción del POST hasta el `HTTP 200/202`? Microsoft Graph espera respuestas en menos de 3 segundos; si el escaneo Anti-Malware (CA-11) demora más, Graph podría reintentar y chocar con la idempotencia del CA-1.

**42.** Si Camunda está caído y se acumulan 10,000 mensajes en RabbitMQ (CA-6), ¿cuál es el throughput esperado (mensajes/segundo) al procesar la cola cuando Camunda vuelve? ¿Se procesan en paralelo (N consumers) o en serie (1 consumer)?

**43.** El CA-12 aplica caché Redis con TTL de 5 minutos para la Whitelist. ¿Se ha considerado el tamaño potencial de la Whitelist en clientes enterprise (Ej: 5,000 dominios autorizados)? ¿La consulta de caché es O(1) via hash lookup o O(N) via set membership check?

**44.** ¿Cuántos Webhooks concurrentes puede atender el sistema simultáneamente sin degradación? ¿Existe un pool de threads dedicado para el endpoint de Webhook separado del pool general de la API REST, evitando que una ráfaga de Webhooks asfixie las operaciones normales del Workdesk?

**45.** El escaneo Anti-Malware sincrónico (CA-11) añade latencia al pipeline. Si el archivo adjunto es de 10MB y el escaneo demora 8 segundos, el response total superaría los 10 segundos. ¿Se ha evaluado mover el escaneo a un pipeline asíncrono donde el Webhook devuelve `HTTP 202 Accepted` inmediatamente y el escaneo se ejecuta en background antes de crear la tarea de triaje?

---

## Observaciones Anti-Alucinación

1. **Todas las 45 preguntas están ancladas** exclusivamente al contenido textual de la US-004 (CA-1 al CA-12) registrado en `epic_A_motor_core.md` y a los GAPs documentados en `us004_functional_analysis.md`. No se inventaron capacidades ni se extrapolaron fechas o métricas no contenidas en la documentación.

2. **Áreas con definición insuficiente detectadas:**
   - La Pantalla 16 (Intake Triage) no tiene wireframe ni US propia dedicada. Su comportamiento funcional se infiere exclusivamente del CA-8/CA-9 de esta US-004, lo cual hace difícil formular preguntas de UX con certeza sobre su anatomía visual.
   - No existe definición sobre cómo el operario de triaje comunica el rechazo o aprobación al remitente original del correo (ciclo de respuesta).
   - La relación entre la Pantalla 11 (Hub de Integraciones) y la US-004 no posee CA propio; la trazabilidad UX la menciona pero sin contrato funcional que la respalde.

3. **Preguntas #18, #19 y #20 están diseñadas específicamente** para cerrar los GAPs-1 y GAP-2 detectados en el análisis funcional previo, conforme lo exige la "Regla Especial para el Cierre de Brechas" del workflow.
