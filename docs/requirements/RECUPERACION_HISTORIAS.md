### US-029: Ejecución y Envío de Formulario (iForm Maestro o Simple)
**Como** Analista / Usuario de Negocio
**Quiero** diligenciar la información de mi sección habilitada en la vista de la tarea (Pantalla 2) y presionar "Enviar"
**Para** finalizar exitosamente mi actividad y que el motor continúe al siguiente paso del proceso.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Task Completion with Form Data

  Scenario: Enviar datos válidos de formulario (CA-1)
    Given la tarea "TK-100" asignada a "carlos.ruiz" requiere el formulario "Form_Aprobacion_V1"
    And "Form_Aprobacion_V1" exige el campo obligatorio numérico "monto_aprobado"
    When "carlos.ruiz" realiza un POST a "/api/v1/workbox/tasks/TK-100/complete"
    And incluye en el body el JSON '{"variables": {"monto_aprobado": 1500, "comentarios": "Ok"}}'
    Then el sistema debe retornar un HTTP STATUS 200 OK
    And la tarea "TK-100" marca su estado como "COMPLETED"
    And las variables del JSON se persisten asociadas a la instancia del proceso.

  Scenario: Enviar datos inválidos (Violación del JSON Schema) (CA-2)
    Given la tarea "TK-100" requiere el campo obligatorio "monto_aprobado" numérico
    When "carlos.ruiz" realiza un POST a "/api/v1/workbox/tasks/TK-100/complete"
    And incluye un JSON vacío '{"variables": {}}'
    Then el sistema valida el payload contra el JSON Schema registrado para "Form_Aprobacion_V1"
    And el sistema debe retornar un HTTP STATUS 400 Bad Request
    And el error format JSON debe especificar `{"error": "ValidationFailed", "fields": [{"field": "monto_aprobado", "message": "Required"}]}`

  Scenario: Trazabilidad Volátil y Recolección de Basura (LocalStorage & GC) (CA-3)
    Given un formulario extenso ("Maestro_Onboarding") abierto en el Workdesk
    When el analista diligencia la mitad de los campos y accidentalmente cierra la pestaña
    Then el Frontend recuperará el progreso utilizando almacenamiento estricto en el navegador (`LocalStorage` mediante `@vueuse/core`) atado al Task_ID.
    And cuando el POST a `/complete` finaliza exitosamente (HTTP 200 OK), el Frontend ejecutará una purga síncrona destruyendo inmediatamente la llave de ese caso específico.
    And un proceso silencioso global al inicio de la sesión eliminará cualquier borrador huérfano en la PC del usuario que supere las 72 horas de antigüedad, previniendo cuellos de memoria.

  Scenario: Consistencia Transaccional Cruda (ACID Fallback over Sagas) (CA-4)
    Given un Payload de formulario (`/complete`) perfectamente validado en Zod que llega al Backend
    When el motor orquestador (Camunda 7) sufre un Crash o Timeout HTTP en su API REST interna
    Then el Backend iBPMS abortará inmediatamente la transacción (Rollback de Persistencia CQRS)
    And devolverá un error HTTP 500 Crudo ("Motor No Disponible") a la UI en Pantalla 2
    And se prohíbe a nivel arquitectónico generar falsos positivos HTTP 202 ("Guardado para después") para eludir el colapso del proceso judicial de fondo.

  Scenario: Inyección Megalítica de Contexto (Patrón BFF) (CA-5)
    Given la entrada física a la vista de la tarea (Pantalla 2)
    When el Frontend inicializa el componente Vue
    Then despachará exactamente UNA (1) única petición consolidada GET `/api/v1/workbox/tasks/{id}/form-context`
    And el Backend obrará como BFF *(Backend for Frontend)* inyectando en un solo Mega-DTO la triada: El JSON Schema de Zod, la configuración de Layout de Vue, y las Variables de Solo Lectura extraídas de Camunda (`prefillData`) para poblar inputs en un solo *tick* de renderizado.

  Scenario: Seguridad Asimétrica y Micro-Tokens Criptográficos (Zero-Trust) (CA-6)
    Given una validación asíncrona externa (Ej: Validar NIT) gatillada desde el Frontend (`OnBlur`)
    When el Backend consulta la API externa exitosamente y retorna al Frontend un "Micro-Token JWT" firmado criptográficamente de corta duración (Ej: TTL 15 min)
    Then al momento del Submit final (`/complete`), el Frontend adjuntará este Micro-Token.
    And el Backend (Zero-Trust) omitirá realizar una segunda llamada de red externa bloqueante, limitándose a verificar matemáticamente la validez de su propia firma en el Micro-Token para autorizar la transacción ACID en milisegundos.

  Scenario: Integridad de Asignación Concurrente (Implicit Locking) (CA-7)
    Given que una tarea "TK-400" está explícitamente asignada al analista `maria.perez` en el motor
    When el analista `pedro.gomez` intercepta vulnerablemente la URL o el JWT Payload e intenta someter un POST a `/tasks/TK-400/complete`
    Then el Core iBPMS examina deductivamente el `{delegatedUserId}` transaccional contra la identidad central del Security Context
    And aborta transaccionalmente la colisión inyectando un lapidario `HTTP 403 Forbidden` o `409 Conflict`, extirpando la necesidad pesada de emitir *ETags* a través del flujo asíncrono.

  Scenario: Hibridación de Datos Históricos vs Nuevos Contratos (Lazy Patching) (CA-8)
    Given el BFF inyectando `prefillData` de una Instancia antigua (V1) hacia un Formulario Zod nuevo (V2)
    When existan campos obligatorios nuevos en la V2 que no venían en la data histórica de Camunda (`null` o `undefined`)
    Then el esquema Zod reactivo los evaluará inmediatamente como inválidos
    And el Frontend pintará dichos inputs en ROJO y bloqueará físicamente el botón de [Enviar]
    And obligando procedimentalmente al analista a contactar al cliente y digitar el dato faltante en la UI para poder avanzar el proceso (Amnistía en Lectura, Guillotina en Escritura).

  Scenario: Desacoplamiento de Carga Binaria (Upload-First Pattern) (CA-9)
    Given un formulario Zod que incluye un componente `<InputFile>`
    When el usuario final adjunta un documento pesado (Ej: PDF de 10MB)
    Then el Frontend ejecutará una carga asíncrona temprana (Pre-Submit) hacia la Bóveda SGDEA (`/api/v1/documents/upload-temp`) obteniendo un Identificador Único (`UUID`).
    And al presionar [Enviar], el POST a `/complete` enviará EXCLUSIVAMENTE el JSON plano referenciando el ID (`{"cedula_pdf": "UUID-123"}`), teniendo PROHIBIDO arquitectónicamente enviar payloads Multipart o Base64 contra el motor de procesos Camunda.
	
  # ==============================================================================
  # US-029.1
  # ==============================================================================
  # A. INICIALIZACIÓN, BFF Y RECUPERACIÓN DE BORRADORES (UX & SRE)
  # ==============================================================================
  Scenario: Inyección Megalítica de Contexto (Patrón BFF) (CA-10)
    Given la entrada física a la vista de la tarea operativa (Pantalla 2)
    When el Frontend inicializa el componente Vue
    Then despachará UNA (1) única petición GET consolidada a `/api/v1/workbox/tasks/{id}/form-context`.
    And el Backend (BFF) inyectará el Mega-DTO: [Esquema Zod Vigoroso + Layout UI + Data Histórica (prefillData)].
    And este DTO incluirá la versión exacta del esquema (`schema_version`) para prevenir choques generacionales si el Arquitecto modifica el diseño mientras el caso está en vuelo.

  Scenario: Autoguardado Híbrido y Cifrado PII en LocalStorage (CA-11)
    Given la digitación continua de un analista en un formulario extenso
    Then el Frontend guardará el borrador (Draft) en el `LocalStorage` del navegador atado al `Task_ID`.
    But si el esquema Zod marca campos como `PII/Sensibles` (US-003), el Frontend DEBE aplicar cifrado simétrico (AES) usando una llave derivada de la sesión antes de escribir en LocalStorage.
    And disparará peticiones silenciosas de Merge Commit al Backend (Snapshot Volátil) solo bajo un Debounce de 10s de inactividad, usando una validación Zod "Parcial" (permitiendo nulos pero castigando tipos inválidos).

  # ==============================================================================
  # B. EJECUCIÓN, IDEMPOTENCIA Y ADUANA DE ARCHIVOS (APPSEC)
  # ==============================================================================
  Scenario: Idempotencia y Protección Anti-Doble Clic (El Dedo Tembloroso) (CA-12)
    Given el usuario pulsa [Enviar Formulario] múltiples veces por ansiedad o lag
    When el Payload JSON impacta el endpoint POST `/complete`
    Then el Frontend inyectará un Header `Idempotency-Key` (UUID único por montaje de componente).
    And el API Gateway/Backend procesará únicamente la primera transacción.
    And las peticiones subsecuentes idénticas retornarán un `HTTP 200 OK` silenciado desde la Caché, protegiendo a Camunda de excepciones `OptimisticLocking` o doble gasto en el Event Sourcing.

  Scenario: Desacoplamiento de Carga Binaria (Upload-First) y Escudo Anti-IDOR (CA-13)
    Given el patrón donde el cliente envía un UUID de un PDF en el POST final (`{"cedula": "UUID-123"}`)
    When el Backend recibe el Payload de cierre de formulario
    Then la arquitectura TIENE ESTRICTAMENTE PROHIBIDO enlazar ciegamente ese archivo a la tarea.
    And el Backend validará en la tabla de adjuntos temporales que `UUID-123` pertenezca al `user_id` logueado Y haya sido subido en el contexto de esa misma `task_id` (Defensa Anti-IDOR).
    And si detecta un UUID ajeno, abortará la transacción con `HTTP 403 Forbidden`.
    And un Cron Job nocturno destruirá físicamente de S3/SGDEA cualquier archivo temporal (TTL > 24h) sin confirmación transaccional para evitar facturas por basura infinita.

  Scenario: Seguridad Asimétrica y Prevención Replay en Micro-Tokens (CA-14)
    Given una validación asíncrona externa (Ej: Validar NIT) gatillada `OnBlur` que retorna un Micro-Token
    When el Frontend adjunta este token en el POST `/complete` final
    Then el Backend verificará matemáticamente su firma (Zero-Trust) para no repetir la llamada externa.
    And la arquitectura PROHÍBE el re-uso de tokens (Replay Attacks); el Token DEBE contener en Claims el `taskId` exacto y un `jti` que será invalidado en Redis un milisegundo después del Submit.

  # ==============================================================================
  # C. VALIDACIÓN ZERO-TRUST Y FIELD-LEVEL RBAC
  # ==============================================================================
  Scenario: Zod Isomórfico y Guillotina de Datos Fantasma (CA-15)
    Given la existencia de esquemas Zod bidireccionales
    When un atacante bypassea la UI enviando un POST adulterado vía API (Ej: Editando un campo de 'Solo Lectura')
    Then el Backend ejecutará OBLIGATORIAMENTE el mismo `schema.json` Zod utilizado en el diseño.
    And cruzará los permisos de escritura del Rol del usuario contra los campos recibidos; si inyectó datos no autorizados, aplicará un `.strip()` silencioso descartando el campo adulterado, o abortará con `HTTP 403 Forbidden`.
    And rechazará con `HTTP 400 Bad Request` cualquier asimetría de tipos de datos.

  # ==============================================================================
  # D. PERSISTENCIA CQRS Y PROTECCIÓN DE CAMUNDA ENGINE
  # ==============================================================================
  Scenario: Exclusión Topológica Estratégica de Camunda y ACID Fallback (CA-16)
    Given el cierre exitoso de la transacción CQRS (Guardado del Evento Inmutable)
    When el Backend notifica a Camunda 7 para avanzar el Token BPMN (`taskService.complete()`)
    Then el Backend TIENE ESTRICTAMENTE PROHIBIDO empujar el Payload masivo (Textos largos, JSONs) hacia la tabla `ACT_RU_VARIABLE` del Engine.
    And solo enviará un DTO minificado con las variables lógicas requeridas por los Gateways.
    And si Camunda sufre Timeout (HTTP 5xx), el Backend aplicará un Rollback estricto de la transacción CQRS (Saga) y devolverá HTTP 500 Crudo, previniendo falsos positivos de guardado en UI.

  Scenario: Consistencia Eventual UX y Read-Your-Own-Writes (RYOW) (CA-17)
    Given que el POST a `/complete` finaliza exitosamente (HTTP 200 OK)
    Then el Frontend ejecutará síncronamente una purga, destruyendo la llave del borrador en el `LocalStorage`.
    And eliminará proactivamente esa tarea específica del Store en RAM (Pinia) del Workdesk ANTES de redirigir al usuario al Home (RYOW).
    And esto garantizará que el usuario no vea su tarea "ya completada" flotando como un fantasma en su bandeja por culpa de la latencia CQRS.
    
  Scenario: Integridad de Asignación Concurrente (Implicit Locking) (CA-18)
    Given que una tarea "TK-400" está asignada explícitamente a `maria.perez`
    When `pedro.gomez` intercepta vulnerablemente la URL e intenta someter un POST a `/tasks/TK-400/complete`
    Then el Core iBPMS examina deductivamente el `assignee` de la tarea contra la identidad central del Security Context.
    And aborta transaccionalmente la colisión inyectando un lapidario `HTTP 403 Forbidden`.

```
**Trazabilidad UX:** Wireframes Pantalla 2 (Vista de Detalle / Formulario Dinámico).

---

### US-039: Formulario Genérico Base (Pantalla 7.B - El Camaleón Operativo)
**Como** PMO / Owner del iBPMS
**Quiero** disponer de un modelo de formulario genérico pre-asociado a tareas operativas simples
**Para** no invertir tiempo dibujando decenas de formularios básicos en la Pantalla 7 cuando la actividad es netamente procedimental (captura de evidencia, observaciones y tracking de avance).

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Auto-vinculación Camaleónica y Resiliencia de Pantalla 7.B

  Scenario: Inyección Explícita (Anti-Bypass) y Restricción VIP (Pre-Flight) (CA-1)
    Given la necesidad de usar el Formulario Genérico Base (`sys_generic_form`) en un BPMN
    When el Arquitecto lo selecciona en el Dropdown de la `UserTask` en la Pantalla 6
    Then el Pre-Flight Analyzer auditará el Rol y la criticidad de esa tarea.
    And si el Rol está tipificado estructuralmente como "Alta Dirección", "Aprobador Financiero" o la tarea exige "Sello Legal", el Pre-Flight BLOQUEARÁ el despliegue (❌ Hard-Stop).
    And prohibirá usar el formulario genérico, forzando la creación de un iForm Maestro formal (Pantalla 7) que cumpla con los estándares pesados de auditoría.
    And para tareas Kanban huérfanas, el sistema inyectará la Pantalla 7.B silenciosamente.

  Scenario: Prevención de Context Bleeding (Filtro Anti-Basura BFF) (CA-2)
    Given un operario que apertura una tarea operativa con el Formulario Genérico
    When el BFF (Backend for Frontend) compila el DTO de inicialización (`prefillData`)
    Then el Backend aplicará un `Whitelist Regex` o filtro estricto sobre el Payload de Camunda.
    And extraerá y enviará EXCLUSIVAMENTE los metadatos de negocio vitales (Ej: `Case_ID`, `Client_Name`, `Priority`, `SLA`), ocultando las 200+ variables técnicas transaccionales del proceso.
    And el Frontend renderizará la Pantalla 7.B coronada por una cuadrícula superior de Solo Lectura ultraligera, evitando la sobrecarga cognitiva del operario.

  Scenario: Mutación Camaleónica de Interfaz y Botón de Pánico (Error Event) (CA-3)
    Given la renderización de la Pantalla 7.B
    When el operario deba escalar o devolver transversalmente el ticket (Ej: Evidencia Insuficiente)
    Then la interfaz exhibirá, además del recuadro principal, un bloque inferior de "Excepciones" o Botones de Pánico (Aprobado / Retorno al Generador / Cancelar).
    And al cliquear un botón de pánico, el Frontend forzará procesalmente la inyección de una observación justificativa mandatoria (Min: 20 caracteres) antes de consumar un Error Event o Escalamiento en el Motor de Camunda.
```

> [!CAUTION]
> **HANDOFF TÉCNICO V1 (QA SRE CERTIFIED):**
> 1. Eliminación y prohibición del uso de variables de tipo `Toggle` binario (ej. `requiere_evidencia`) como lógica de UI en este documento, usando en su lugar un enfoque semántico estructural sin ambigüedades.
> 2. Prevención de colisiones de Namespace garantizada mediante inyección de `Whitelist Regex` en el BFF, evitando envenenamiento de los context variables del Engine.

**Trazabilidad UX:** Wireframes Pantalla 7.B (Formulario Genérico Base).
