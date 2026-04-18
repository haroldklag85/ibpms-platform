# [TÍTULO DE LA ÉPICA]
> **Fuente:** Modularizado desde `v1_user_stories.md` | **Fecha:** 2026-04-10
> **Coverage:** `.agentic-sync/coverage_matrix.md` (centralizada)

---
### US-011: Filtrado Transversal en Bandeja Avanzada (Docketing)
**Como** Analista Legal / Supervisor de Operaciones
**Quiero** filtrar mi bandeja de entrada estructurada (Pantalla 1B) mediante dropdowns relacionales ("Cliente", "Proyecto", "Rango de Fechas") y etiquetas booleanas de actividad ("Acuses", "Tareas Creadas")
**Para** localizar rápidamente eventos críticos o cargas de trabajo asociadas a cuentas clave sin abrir cada correo individualmente.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Advanced Relational Inbox Filtering
  Scenario: Filtrado compuesto determinista (Cliente + Proyecto)  (CA-1)
    Given el usuario autenticado está navegando la Bandeja Avanzada (Pantalla 1B)
    And hay 500 ítems en la bandeja, de los cuales 5 pertenecen al Cliente "Global Tech" y el Proyecto "Patente-XZ"
    When el usuario selecciona "Global Tech" en el selector 'Filtro Cliente'
    And el usuario selecciona "Patente-XZ" en el selector 'Filtro Proyecto'
    Then el API del Backend debe ejecutar una query cruzada contra 'ibpms_metadata_index'
    And el Frontend debe renderizar exclusivamente los 5 ítems exactos en menos de 1 segundo (Paginado)
    And la UI debe mostrar un estado "Empty State" si la combinación no retorna resultados

  Scenario: Filtrado por Label Booleano generado por IA (Acuses) (CA-2)
    Given la bandeja contiene ítems marcados por la IA con el boolean flag 'is_acknowledgment_sent: true'
    When el usuario marca el checkbox "Actividad: Acuse Enviado"
    Then el sistema debe ocultar todos los correos donde 'is_acknowledgment_sent: false' o nulo

  Scenario: Triage por Sentimiento y Urgencia (Predicción IA) (CA-3)
    Given la metadata enriquecida del correo proveniente de la US-013 (Ej: `sentiment: URGENCE_HIGH`)
    When el analista de SAC filtra la bandeja usando el dropdown "Urgencia y Sentimiento"
    Then el sistema filtra reestructurando la grilla para mostrar primero los correos que contengan quejas operativas o riesgos legales altos
    And garantizando un enfoque de First-In/First-Out ajustado por criticidad (Weighted FIFO).

  Scenario: Detección de Archivos y Tipificación Estructural (CA-4)
    Given que el correo contiene múltiples archivos adjuntos
    When el analista filtra por el concepto "Contiene: Contratos Firmados"
    Then el filtro de la Pantalla 1B obvia la extensión pura del archivo (.pdf)
    And cruza la búsqueda contra el tag de clasificación documental `doc_type` generado por la IA, retornando solo los correos cuyo contenido semántico coincida.

  Scenario: Monitoreo Activo de Acuerdos de Nivel de Servicio (SLA) (CA-5)
    Given los correos entrantes mapeados contra una política de respuesta máxima de 24 horas (SLA)
    When el analista de SAC aplica el filtro rápido de semáforo "Mostrar: SLA por Vencer (< 2 horas)"
    Then el sistema expone exclusivamente los correos que están a punto de romper el requerimiento legal de tiempo operativo, ocultando correos recientes de ingreso temprano.

  Scenario: Búsqueda Semántica de Texto Completo (Full-Text Search) (6A-6)
    Given un analista buscando la aguja en el pajar con la palabra "Indemnización"
    When digite dicha palabra en la barra de búsqueda global de la Pantalla 1B
    Then el motor de Backend (Elasticsearch o similar) NO buscará solo en el Asunto
    And indexará la búsqueda contra el cuerpo del correo, y el texto interior de los anexos (OCR) entregando el correo exacto donde reside dicho patrón.

  Scenario: Control de Concurrencia SAC y Bloqueo de Correos  (CA-7)
    Given un buzón compartido accedido por 5 analistas de SAC simultáneamente
    When el Analista "A" da clic para leer un nuevo "Correo Huérfano"
    Then el sistema inscribe un Soft-Lock en la Base de Datos asociando ese correo al `User_ID` del Analista "A"
    And cuando el Analista "B" filtre la bandeja en la vista "Mis Correos Asignados", no verá el correo del "A", evitando que dos humanos gestionen el mismo ticket y generen respuestas duplicadas.
```
**Trazabilidad UX:** Prototipo UI2.html / Pantalla 1B.

---

### US-012: Propuesta de respuesta para correo entrante (con revisión humana)
**Como** gestor de un buzón corporativo
**Quiero** recibir un borrador de respuesta basado en el contexto del hilo y precedentes
**Para** contestar más rápido y con consistencia.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Generación de Borradores de Respuesta Interactivos
  Scenario: Doble Borrador (Acuse Inmediato y Respuesta de Fondo)
    Given la entrada de un correo electrónico al buzón de SAC
    When el agente IA procesa el contenido exitosamente
    Then debe generar y presentar dos borradores independientes en la Pantalla 2C:
    And 1. "Acuse de Recibo": Respuesta corta confirmando radicación.
    And 2. "Respuesta de Fondo": Borrador técnico para solucionar la petición.
    And cada borrador tiene un ciclo de vida UI independiente, permitiendo enviar el Acuse hoy y gestionar el Fondo mañana.

  Scenario: Prevención de Alucinaciones en Variables Críticas de Negocio
    Given la generación del "Borrador de Fondo" por parte del LLM
    When el motor de IA detecte la necesidad de comprometer fechas, montos económicos o nombres de responsables
    Then tiene estrictamente PROHIBIDO pre-llenar estos datos asumiéndolos del contexto
    And el texto generado inyectará placeholders visuales (Ej: `[INGRESAR_MONTO]`)
    And el Frontend inhabilitará el botón "Aprobar y Enviar" hasta que el analista reemplace manualmente dichos condicionales.

  Scenario: Restricción Bilingüe (Solo EN/ES)
    Given la recepción de un correo en un idioma diferente a Español o Inglés (Ej. Alemán)
    When el sistema detecte el idioma origen
    Then traducirá y mostrará el correo original en Español al analista para su comprensión (Panel Izquierdo)
    And se ABSTENDRÁ de generar un borrador automático de respuesta en Alemán, mostrando una alerta de "Idioma no soportado para auto-redacción", obligando al humano a escribir la respuesta.

  Scenario: Confianza en la Intervención y Tono Humano
    Given que el analista decide modificar sustancialmente el borrador de fondo propuesto por la IA
    When el usuario presione el botón "Guardar Edición y Enviar"
    Then el sistema confía íntegramente en el criterio del humano y ejecuta el envío sin re-validaciones (Override Total)
    And el texto final enviado entra al bucle de aprendizaje MLOps (US-015) para alinear futuras propuestas a ese nuevo tono.

  Scenario: Contexto Acotado del Historial (Sliding Window Context)
    Given un correo que pertenece a un hilo monumental de 60 correos previos
    When el backend ensamble el "Prompt" para solicitar el borrador de respuesta a la IA
    Then inyectará únicamente los 5 correos más recientes de la cadena cronológica
    And truncará el resto para eficientar el consumo de Tokens y evitar degradación de contexto del LLM.

  Scenario: Inyección Dinámica de Disculpas Institucionales (Tone-Matching)
    Given el análisis de metadata del correo (US-013) que arroja un 'sentiment_score' de Rabia Extrema o Amenaza Legal
    When el Agente IA redacte el "Borrador de Fondo"
    Then el System Prompt obligará al modelo a omitir frases comerciales genéricas o "happy-talk"
    And forzará la inserción de una Disculpa Institucional formal y empática al inicio del texto para desescalar el conflicto.

  Scenario: Ceguera Transaccional (Prohibición de Promesas)
    Given la redacción de la respuesta de fondo por parte del LLM en la V1
    When el LLM analice el requerimiento del cliente (Ej: "¿Mi póliza cubre este choque?")
    Then el motor tiene explícitamente PROHIBIDO afirmar, negar o garantizar estados transaccionales o coberturas que vivan en BD externas (Ej: "Sí lo cubrimos")
    And el borrador se limitará perentoriamente a indicar que "El caso se encuentra en revisión" y a solicitar información, formatos o documentos adicionales si hacen falta.
```
**Trazabilidad UX:** Prototipos UI1.html y UI4.html / Pantalla 2C.

---

### US-013: Identificación automática de cliente y enriquecimiento desde posible conexion con CRM (ONS)
**Como** gestor de un buzón corporativo
**Quiero** que el asistente identifique el cliente por el dominio del remitente y/o consulte el CRM ONS
**Para** contextualizar la respuesta y adaptar el tono.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Enriquecimiento CRM de Hilos de Correo
  Scenario: Match exitoso con CRM ONS
    Given un correo entrante con dominio '@cliente.com'
    When el sistema consulta la API del CRM ONS usando el dominio
    Then si existe coincidencia, asocia el correo a un 'client_profile' (account/contact)
    And el LLM ajusta el nivel de formalidad del borrador basado en la data del perfil del cliente
  
  Scenario: Cliente no identificado
    Given un correo entrante donde el dominio no existe en CRM ONS
    Then el sistema marca el correo visualmente como "Cliente no identificado"
    And sugiere una tarjeta atómica para solicitar datos o registrar al cliente nuevo en CRM

  Scenario: Extracción Masiva de Metadata Operativa (Preparación para Docketing)
    Given un correo electrónico entrante en crudo
    When el Motor LLM evalúe su contenido para hallar al Cliente CRM
    Then PARALELAMENTE DEBE emitir un objeto JSON estandarizado conteniendo metadatos críticos de negocio:
    And 1. `sentiment_score`: Evaluación de frustración o amenaza legal.
    And 2. `predicted_service`: El Proyecto SD al que pre-asume pertenece la solicitud.
    And 3. `attachments_classification`: Un arreglo donde cataloga (Ej. "Es un contrato", "Es un comprobante") el tipo de archivo recibido sin requerir apertura humana.
    And esta metadata debe persistirse en 'ibpms_metadata_index' para viabilizar los filtros de la US-011.

  Scenario: Fallback a Metadata Interna (Sin CRM)
    Given la configuración global administrada donde el flag `ENABLE_CRM_INTEGRATION` está apagado o la API del CRM no responde
    When el sistema intente asociar el correo a un Cliente o Proyecto
    Then el motor de Backend buscará coincidencias de cruce en el `ibpms_service_delivery_catalog` interno
    And relacionará el correo con proyectos o instancias previas de Camunda que compartan el mismo dominio, garantizando continuidad operativa en la US-011 sin depender de bases de datos externas.

  Scenario: Enrutado Semántico para Dominios Multi-Proyecto
    Given un dominio corporativo (Ej. `@amazon.com`) que posee múltiples proyectos/servicios activos en el iBPMS simultáneamente
    When el Agente IA lea el correo entrante
    Then el sistema NO etiquetará estúpidamente el correo con todos los proyectos a la vez
    And el LLM estará obligado a cruzar el texto del cuerpo del mensaje contra las descripciones de los proyectos activos, seleccionando matemáticamente el `predicted_service` más coherente para el analista.

  Scenario: Lista Negra de Dominios Públicos (Blacklist)
    Given la recepción de un correo proveniente de un proveedor público masivo (Ej. `@gmail.com`, `@outlook.com`, `@yahoo.es`)
    When el sistema intente ejecutar el motor de "Match por Dominio"
    Then el backend interceptará la ejecución cotejando el dominio contra la tabla `ibpms_public_domains_blacklist`
    And anulará la vinculación por dominio para evitar colisiones masivas de privacidad cruzada entre clientes distintos
    And forzará al Motor IA a buscar identificadores únicos (Cédulas, RUT, Teléfonos, Nombres Completos, Números de Factura) EXCLUSIVAMENTE dentro del cuerpo del mensaje o firmas para establecer el Match.
```
**Trazabilidad UX:** Wireframes Pantalla 1B (Bandeja Docketing).

---


### US-014: Sugerencia de acciones (tareas) operativas
**Como** gestor de un buzón corporativo
**Quiero** que el asistente sugiera acciones operativas (crear tarea, iniciar proceso) asociadas al correo
**Para** acelerar el flujo de trabajo funcional sin perder el control manual.

> 🧠 **Sinergia Arquitectónica (Ecosistema Inteligente):**
> Esta historia es el "Sistema Nervioso Central" operativa del buzón y orquesta estrechamente con el siguiente ecosistema:
>
> 📌 **Ecosistema Intake (BPMN / SD):**
> - **Embudo de Cuarentena (US-040):** Si la intención detectada implica arrancar un "Nuevo Proceso SD", la US-014 somete obligatoriamente esta Acción sugerida a la Bandeja de Aprobación de Intakes (Pantalla 16) para que un Líder la despache.
> - **Confirmación Formal 'Plan A' (US-022):** Si la Acción del Embudo (US-040) se aprueba, la US-022 asume el control enviando un correo de confirmación formal al cliente y consolidando el nacimiento del Flujo en Camunda.
> - **Escape Manual 'Plan B' (US-024):** Si la IA detrás de la US-014 falla absolutamente y no sugiere ninguna Action Card útil, el subsistema recae grácilmente sobre la US-024, permitiendo al Administrador crear el Proceso a mano.
>
> 📌 **Ecosistema de Pre-Procesamiento y Retorno:**
> - **Entrada de Datos (US-013):** Consume la Metadata pre-calculada (`predicted_service`, `sentiment`). La US-014 es "ciega" sin la US-013.
> - **Presentación UI (US-011):** Dibuja las "Action Cards" nativamente dentro de la Bandeja Docketing pública de SAC y etiqueta los correos procesados.
> - **Enrutamiento Atómico (US-030/US-008):** Si la intención detectada es de baja complejidad (Petición simple), inicializa una tarjeta Kanban directamente en el proyecto, saltándose Camunda.
> - **Workdesk (US-001) y RBAC (US-036):** Despacha la tarea validada al escritorio personal del analista, respetando la estricta matriz de roles y permisos del proyecto.
> - **Retroalimentación MLOps (US-015):** Si el operador de la US-011 edita o rechaza manualmente a la inteligencia en sus sugerencias, ese evento viaja a la base de datos de entrenamiento continuo del modelo.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Action Cards Operativas y Orquestación Funcional (Human-in-the-Loop)
  Scenario: Bifurcación Sensible al Contexto (Nueva Tarea vs Nuevo Proceso)
    Given la generación de sugerencias operativas (Action Cards) basadas en metadatos
    When la Inteligencia Artificial evalúe la intención transaccional del correo
    Then si este pertenece a un proyecto existente, sugerirá crear una "Nueva Tarea" ligada a ese Proceso/Sprint Kanban
    And si es una petición huérfana (nuevo requerimiento), sugerirá instanciar un "Nuevo Proceso SD", enviando esta tarjeta de creación forzosamente hacia el Embudo Administrativo (US-040).

  Scenario: Edición Activa de la Tarjeta (Human-in-the-Loop Feedback)
    Given la presentación visual de una Action Card latente en la Pantalla 1B
    When el Analista (humano) decida repriorizar o reasignar los datos sugeridos por la máquina antes de aprobar (Ej: Cambiar Urgencia "Media" a "Alta")
    Then el Frontend habilitará la edición libre e in-situ del payload sugerido
    And el sistema creará la entidad resultante con los datos humanos (sobreescribiendo los algorítmicos)
    And transmitirá la rectificación al motor de telemetría MLOps subyacente (US-015) para curar los pesos de inferencia futuros.

  Scenario: Trazabilidad Permanente del Correo Original (Inbox Behavior)
    Given la aprobación formal de la Action Card (creando la tarea o servicio SD definitivo en el back)
    When se consolide la mutación externa hacia Camunda o Kanban
    Then el servidor NO borrará ni desaparecerá forzosamente el correo original de la Bandeja Docketing pública de SAC (Pantalla 1B)
    And la UI le estampará un badge/etiqueta visual persistente referenciando el `[Status: Actividad Creada]` y el ID destino
    And un clon/copia del correo original formará obligatoriamente la primera pieza probatoria (Attachment 1) de la hoja de ruta del nuevo caso en el iBPMS.
```
**Trazabilidad UX:** Wireframes Pantalla 1B (Bandeja Docketing) y Pantalla 16 (Intake Administrativo).

---

### US-015: Feedback y Aprendizaje Supervisado (Nightly MLOps Batch)
**Como** Líder de Operaciones / Arquitecto IA
**Quiero** que el sistema aprenda de las correcciones humanas sin colapsar el performance transaccional diario
**Para** garantizar una evolución cognitiva continua (Zero-Touch) aislando el entrenamiento en ventanas nocturnas controladas.

> 🧠 **Arquitectura de Aprendizaje en 2 Fases (V1):**
> Para proteger la estabilidad de la Base de Datos Transaccional (Camunda/Kanban) durante el día, el MLOps se bifurca en:
> - **Día (Observador Pasivo):** Registra silenciosamente el "Delta" entre lo que sugirió la IA y la decisión final del humano en la UI.
> - **Noche (Agente Data Scientist):** Un Cron-Job autónomo procesa masivamente los logs del día y afina los vectores/pesos sin intervención de ingenieros humanos.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: MLOps Feedback Loop Architecture (2-Phase)
  Scenario: Fase 1 - Recolección Diurna (Observación No Bloqueante NFR)
    Given un componente IA emitiendo una sugerencia o borrador en la Pantalla 1B
    When un Analista humano edita, rechaza o reclasifica dicha sugerencia antes de enviarla
    Then el Frontend disparará la discrepancia (Lo que dijo la IA vs Lo que escribió el Humano) hacia el endpoint dedicado `POST /api/v1/mlops/feedback` bajo el patrón "Fire-and-Forget"
    And queda estrictamente PROHIBIDO interceptar o añadir latencia a la transacción core de Camunda/Entity al guardar casos en la BD principal
    And el servicio Backend (Spring Boot) empujará este log a un tópico en RabbitMQ o lo procesará de forma 100% asíncrona para insertarlo en `ibpms_mlops_feedback_log` sin competir por los hilos de conexión de PostgreSQL.

  Scenario: Fase 2 - Despertar del Agente Data Scientist (Batch Nocturno)
    Given la ventana de mantenimiento parametrizada (Ej: 02:00 AM, Semanal, Mensual) en la Épica 15
    When el motor de Cron-Jobs dispara el Agente Data Scientist (Proceso Autónomo)
    Then el Agente consultará masivamente la tabla `ibpms_mlops_feedback_log` filtrando los logs acumulados
    And ejecutará la consolidación de patrones repetitivos exigiendo un "Consenso Mínimo" (Ej: Aprende el patrón SI Y SOLO SI existen al menos 2 analistas distintos corrigiendo lo mismo).
    And si encuentra discrepancias lógicas (Analista A lo categorizó 'Ventas', Analista B lo categorizó 'Soporte'), el Agente ignorará por completo el patrón por considerarlo "Contradictorio".
    And ignorará matemáticamente los "Patrones Negativos" (Cuando el analista simplemente oprime `[Descartar/Eliminar Propuesta]`), asumiendo que el rechazo se debe a ruido/spam y no a un error cognitivo de clasificación, ahorrando poder de cómputo.
    And le dará un peso aritmético multiplicador a la corrección dependiendo del Rol (Ej: Corrección de un Líder pesa x5 frente a la de un Junior).
    And actualizará los pesos de la Base de Conocimiento (RAG) para clasificaciones Y asimilará las correcciones de redacción humana de la Pantalla 1B para imitar el Estilo Institucional (NLG).

  Scenario: Trazabilidad y Purga del Turno Nocturno
    Given la finalización exitosa del fine-tuning nocturno
    Then el Agente Data Scientist emitirá un reporte consolidado al log de auditoría del sistema: "Matriz actualizada basada en N correcciones"
    And marcará los registros procesados en la tabla `ibpms_mlops_feedback_log` con el flag `status: trained`.

  Scenario: Declaración de Incompetencia Diurna (Límite Paramétrico)
    Given un correo electrónico confuso procesado durante el día
    When la red neuronal calcula un Confidence Score por debajo del umbral parametrizado en la Épica 15 (Configuración Global)
    Then la IA se declara incompetente explícitamente y deja la tarea en blanco
    And enruta el caso obligatoriamente al "Fallback Humano" sin intentar adivinar, generando el primer log de falla para que el Data Scientist Nocturno lo califique como un "Patrón Desconocido".

  Scenario: Resiliencia Nocturna en PostgreSQL y Dead-Lettering (NFR)
    Given el Cron-Job de aprendizaje neuronal (Agente Data Scientist) leyendo forzosamente los logs desde RabbitMQ
    When recalcule vectores RAG y la base PostgreSQL arroje latencia o timeout a las 03:00 AM
    Then el worker abortará emitiendo un NACK a RabbitMQ
    And al acumular 3 NACKs sucesivos, un Dead Letter Exchange (DLX) enviará la carga fallida a la cola residual `mlops-dlq`
    And notificará al SysAdmin por correo/webhook salvando la metadata cruda de la pérdida para intervención manual.
```
**Trazabilidad UX:** Wireframes Pantalla 1B (Bandeja Docketing).

---



### US-016: Gestión multi-buzón con políticas por buzón
**Como** administrador del asistente
**Quiero** configurar políticas por buzón (tono, idioma por defecto, categorías, aprobadores)
**Para** adaptar el comportamiento a cada canal corporativo.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Configuración de 'Mailbox Policy' Dinámicas
  Scenario: Aplicación de políticas diferenciadas sin reiniciar el sistema
    Given múltiples buzones corporativos registrados
    When el administrador define un 'mailbox_policy' configurando idioma, nivel de formalidad, disclaimers y escalamiento
    Then las propuestas generadas por el LLM aplican inmediatamente este contexto en sus prompts
    And los nuevos cambios de política operan sobre el siguiente correo entrante sin requerir 'redeploy' de código

  Scenario: Toggle de Activación Cognitiva por Buzón
    Given la configuración local de un buzón en la Pantalla 15.B
    Then el Administrador posee un Master Switch `[Habilitar IA Copilot]`
    And si el switch está apagado, el correo ingresa a la bandeja como un Intake 100% manual, sin análisis de sentimiento, sin extracción CRM y sin sugerencias, ahorrando tokens en buzones de bajo valor.

  Scenario: Restricción del Catálogo de Servicios (White-Listing de Acciones)
    Given que la IA generativa puede alucinar o sugerir procesos fuera de la jurisdicción del área
    When el Administrador configura un buzón específico (Ej: `soporte_tecnico@`)
    Then la Pantalla 15.B despliega una lista de checkboxes con todos los servicios SD disponibles en el Catálogo de la empresa.
    And el Administrador puede seleccionar explícitamente cuáles son los ÚNICOS procesos que la IA tiene permitido sugerir en este buzón. Las inferencias hacia procesos no seleccionados serán bloqueadas y el borrador de acción quedará vacío.

  Scenario: Umbral de Confianza Cognitiva Independiente (Confidence Score)
    Given la pestaña "Variables de Entorno" dentro de la configuración específica de un buzón
    When el líder de negocio edita las propiedades de ese buzón
    Then puede asignar matemáticamente (0-100) el `Minimum Confidence Score` local.
    And cualquier inferencia heurística de LA IA en ESE buzón que no supere el puntaje, se considerará "Anómala" e invocará el `[Fallback Humano]` (US-015).
    And esto permite tener buzones críticos (Ej: Legales) exigiendo 95% de confianza, y buzones laxos (Ej: Info General) exigiendo 70%, sin pisarse entre ellos.

  Scenario: Enrutamiento Táctico y SLA por Defecto
    Given la entrada de un nuevo correo a un buzón específico (Ej: `reclamos@`)
    When la IA procesa el mensaje a las 3:00 AM y genera las propuestas (A la espera de validación humana diurna)
    Then la política del buzón forzará a la IA a asignar un SLA de Gracia (Ej: 2 horas) y una Criticidad (Ej: Alta) pre-parametrizada para ese buzón.
    And el sistema enrutará la visualización de este correo EXCLUSIVAMENTE a los usuarios que posean el Rol/Dueño asociado a ese buzón (RBAC), impidiendo que el Intake sea público para toda la empresa.

  Scenario: Control de Tono y Firmas Corporativas (NLG)
    Given que la IA generó un borrador de respuesta (US-012)
    Then la política del buzón obligará a la IA a reescribir la respuesta bajo el "Tono" parametrizado (Ej: "Corporativo y Gélido" para quejas, "Persuasivo" para ventas).
    And inyectará automáticamente en el borrador la Plantilla de Firma asociada a ese buzón (Nombre del Canal, Disclaimer de Privacidad, Links).

  Scenario: Parseo Multilingüe Estricto (Inglés/Español)
    Given la política de Idioma del buzón ajustada a "Match Automático (V1)"
    When ingresa un correo en Inglés
    Then la IA procesará, analizará y sugerirá el borrador de respuesta OBLIGATORIAMENTE en Inglés. Si entra en Español, el ciclo completo será en Español.

  Scenario: Alerta UI de Desconexión de Buzón (Token Expirado)
    Given que el conector IMAP/GraphAPI de Office 365 pierde permisos sobre un buzón (Sesión expirada o revocada)
    Then el iBPMS dejará de leer el buzón en silencio
    And levantará de inmediato una "Alerta Crítica Visual" en la Pantalla 15.B (Local) marcando el buzón en Rojo.
```
**Trazabilidad UX:** Wireframes Pantalla 15.B (Configuración Local de Buzones SAC).

---


### US-037: CRUD de Conexiones de Buzones (Intake API)
**Como** Súper Administrador del Sistema
**Quiero** registrar y administrar libremente las cuentas de correo corporativo conectadas al iBPMS
**Para** definir de dónde el motor saca la información, qué protocolo usar, con qué frecuencia y a qué proceso BPMN enruta por defecto cuando la Inteligencia Artificial (Agente 3) no logra deducirlo.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Configuración de Orígenes SAC (Mailbox CRUD)
  Scenario: Soporte Multi-Protocolo de Conexión (Arch. Abierta) (CA-1)
    Given la ventana de registro de un nuevo Buzón SAC
    Then el Administrador tiene la opción de elegir el tipo de Conector
    And soporta autenticación moderna (OAuth 2.0 / MS Graph API) para ecosistemas Microsoft
    And soporta simultáneamente configuración legacy (IMAP / SMTP) con usuario y contraseña genéricos (Ej: Gmail, cPanel) para una arquitectura V1 abierta.

  Scenario: Centralización del Poder Organizacional (CA-2)
    Given el formulario de gestión de cuentas (Pantalla 15)
    Then este módulo está fuertemente bloqueado y pertenece exclusivamente al Súper Administrador
    And un "Líder de Área SAC" NO puede agregar un correo nuevo de forma autónoma, forzando un esquema de gobierno centralizado por IT.

  Scenario: Trazabilidad de Fallo (BPMN Default Rule) (CA-3)
    Given un correo altamente ininteligible donde el Agente MLOps (Agente 3) falla en deducir su categoría
    Then la configuración del buzón cuenta con un campo obligatorio: `[Proceso BPMN de Caída por Defecto]`
    And el iBPMS enrutará ciegamente este correo hacia ese proceso genérico pre-seleccionado (Ej: "Trámite de Reclamo Manual") para no dejar correos "en el limbo".

  Scenario: Sincronización Programada (Polling) y Manual (CA-4)
    Given la infraestructura de recolección de correos
    Then el sistema utiliza un Job de Polling configurado bajo mejores prácticas (Ej: cada 5 minutos) para evitar ahogar al servidor
    And expone adicionalmente un botón táctico `[🔄 Sincronizar Buzón Ahora]` en el Frontend para que el Administrador fuerce la lectura a demanda inmediata.

  Scenario: Ping de Conexión en Vivo Obligatorio (CA-5)
    Given el administrador registrando credenciales de MS Graph (OAuth)
    When oprime el botón de Guardar
    Then el iBPMS pausa el registro y dispara un ping transaccional en caliente contra el tenant de Microsoft
    And solo permite crear formalmente el Origen de Datos si Microsoft responde con un token 200 OK, abortando el proceso si las credenciales fallan.

  Scenario: Réplica Operativa iBPMS vs Exchange (No Destructiva) (CA-6)
    Given el proceso de "chupar" correos (Ingesta)
    Then el iBPMS NUNCA ejecuta comandos de `DELETE` físico contra el Exchange de origen por el simple hecho de leerlos
    And genera un folio replicado en la base de datos propia. Si un Súper Admin decide borrar (Hard-Delete) el caso en el iBPMS, el motor envía una instrucción de *Soft-Delete* hacia Microsoft (Mover a Papelera / Archivo) manteniendo la paridad.

  Scenario: Gobernalización Central del Blacklist en V1 (CA-7)
    Given la necesidad de bloquear SPAM o dominios maliciosos
    Then en el MVP (V1) la Pantalla 15 NO reconstruye formularios de Blacklist/Whitelist
    And delega el filtrado anti-spam 100% a las políticas perimetrales nativas configuradas por IT en Microsoft Exchange. (Reglas bidireccionales por API diferidas a V2).

  Scenario: Silenciador de Emergencia Táctil (CA-8)
    Given un ataque de SPAM o falla lógica en el enrutamiento de un Buzón
    Then la grilla del CRUD expone un Toggle Switch `[En Vivo / Pausado]` de desconexión inmediata
    And permite suspender temporalmente el Job de Polling para ese buzón en específico sin borrar permanentemente el registro ni sus tokens almacenados.

  Scenario: Excepción de Límites de Carga por Dominio (CA-9)
    Given que el límite global de archivos adjuntos del iBPMS es de 50MB
    Then el formulario del Buzón permite configurar un `Override`
    And otorga la capacidad de definir un límite en Megabytes customizado exclusivo para los correos succionados por esa cuenta en particular (Ej: 100MB para `planos@`).

  Scenario: Auditoría de Caducidad de Tokens M2M (CA-10)
    Given que los Secretos de Cliente OAuth en Entra ID caducan cada 6 meses
    Then la Pantalla 15 debe calcular el tiempo de vida de la conexión
    And si las credenciales fallan, el iBPMS inyecta una alerta en el Log de Auditoría y envía una notificación estructurada a los Administradores advirtiendo la desconexión del SAC.
```
**Trazabilidad UX:** Pantalla 15 (Configuraciones Genéricas / Logs).

---



