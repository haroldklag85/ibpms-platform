# [TÍTULO DE LA ÉPICA]
> **Fuente:** Modularizado desde `v1_user_stories.md` | **Fecha:** 2026-04-10
> **Coverage:** `.agentic-sync/coverage_matrix.md` (centralizada)

---
### US-027: Copiloto IA (Auditoría ISO 9001 y Generador Consultivo BPMN)
**Como** Arquitecto Modelador de Procesos
**Quiero** un asistente IA interactivo embebido en el diseñador (Pantalla 6)
**Para** que audite mis diagramas buscando brechas de calidad (ISO 9001 y BPMN 2.0), O genere un proceso BPMN 2.0 desde cero a partir de documentos adjuntos e iteraciones de preguntas aclaratorias en lenguaje natural.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: AI Copilot Generator, SRE Layout, AppSec & UX Governance

  # ==============================================================================
  # A. EFICIENCIA SRE, AUTO-LAYOUT Y AHORRO DE TOKENS (GAPs 18, 19, 20)
  # ==============================================================================
  Scenario: Soberanía Geométrica y Prevención de Spaghetti (Auto-Layout) (CA-01)
    Given la incapacidad de los LLMs para calcular coordenadas (X,Y) espaciales precisas
    When el Agente IA genera el proceso (Hit-The-Canvas)
    Then el LLM devolverá EXCLUSIVAMENTE el marcado lógico semántico (`<bpmn:process>`).
    And la arquitectura PROHÍBE que el LLM calcule topología `BPMNDi`.
    And un Middleware Backend (Librería de Auto-Layout) inyectará matemáticamente las coordenadas X,Y antes de enviarlo al Frontend, previniendo el colapso geométrico del navegador.

  Scenario: Minificación de Tokens Prompeados (Context Optimization) (CA-02)
    Given el Arquitecto solicita a la IA "Auditar" el diagrama actual (ISO 9001) o extenderlo
    Then el Backend TIENE PROHIBIDO enviar el XML crudo con coordenadas al LLM.
    And decantará el XML a un JSON semántico ligero (puramente Nodos y Flujos), abaratando el costo de la facturación Cloud (Tokens) en un 70%.

  Scenario: Ingesta Documental Asíncrona, Multimodal y Antivirus (CA-03)
    Given que el Arquitecto sube un PDF/DOCX o Imagen de flujograma al Dropzone (Max 5 archivos / 100 págs)
    Then el Frontend mostrará una métrica de límite dinámico (Ej: `Páginas: 45/100`).
    And el archivo pasará por un escáner Anti-Malware (ClamAV Cloud) en milisegundos.
    And la extracción de texto y visión multimodal (GPT-4V/Tika) se delegará a una Cola de RabbitMQ con WebWorkers, sin saturar los Hilos HTTP.

  # ==============================================================================
  # B. SEGURIDAD APPSEC, RAG POISONING Y DO-W
  # ==============================================================================
  Scenario: RAG Efímero, Aislamiento Vectorial y Anti-Poisoning (CA-04)
    Given la vectorización de documentos en `pgvector`
    Then los TextChunks nacerán con un `Time-To-Live (TTL)` efímero atado a la sesión del Chat.
    And toda consulta a la base vectorial incluirá el `tenant_id` y `session_id` obligatoriamente.
    And al cerrar el diseñador, la base vectorial y los archivos en S3 se autodestruirán, previniendo RAG Poisoning corporativo transversal y el "Embedding Bloat".

  Scenario: Mitigación Denial of Wallet (DoW) y Prevención XSS/Prompt Injection (CA-05)
    Given la exposición del Endpoint del LLM a los empleados
    Then el API Gateway impondrá Rate Limiting estricto (Ej: Max 5 generaciones/min).
    And el Backend seudonimizará los nombres de tareas (PII) antes de enviarlos al LLM.
    And el Frontend aplicará `DOMPurify` brutal sobre el XML entrante para evitar Cross-Site Scripting (XSS) reflectivo.
    And si el Backend detecta "Prompt Injection" intencional 3 veces consecutivas, castigará al usuario revocando dinámicamente el `ROLE_PROCESS_ARCHITECT` y alertará al CISO.

  # ==============================================================================
  # C. RESTRICCIONES BPMN Y COMPORTAMIENTO COGNITIVO
  # ==============================================================================
  Scenario: Topología Restringida, Traducción Activa y Manejo de Bucles (CA-06)
    Given la generación de XML a partir de NLP
    Then el Agente estará limitado en V1 a instanciar: `UserTasks`, `ServiceTasks`, `Gateways` y `ErrorBoundaryEvents` para planes B.
    And tiene PROHIBIDO generar Sub-Procesos Embebidos (`CallActivities`) o Eventos de Señal complejos.
    And ante directivas de "Repetir proceso", dibujará un `SequenceFlow` en reversa (Loop), prohibiendo la duplicación lineal.
    And sin importar el idioma del PDF (Inglés/Mandarín), generará el XML y el Chat estrictamente en Español (Traducción Activa).

  Scenario: Triage Conversacional, Píldoras Rápidas y Roles Faltantes (CA-07)
    Given que el LLM detecta contradicciones documentales o roles inexistentes en EntraID
    When la IA pausa la inyección y genera una consulta (Triage)
    Then dosificará las preguntas (Máx 3 por lote) y ofrecerá "Píldoras de Respuesta Rápida" (Ej: `[Usar Rol Existente]`, `[Omitir]`).
    And si debe crear un rol nuevo, usará un ID temporal (Ej: `rol_dummy`) e inyectará un `TextAnnotation` (Nota Adhesiva) recordando al humano crearlo.
    And los Gateways dibujados por IA NO tendrán expresiones matemáticas inyectadas, delegando esa lógica al humano.

  # ==============================================================================
  # D. UX, RECUPERABILIDAD Y PREVENCIÓN DE ERRORES
  # ==============================================================================
  Scenario: UX No Bloqueante, Transmutación Visual y Undo Atómico (CA-08)
    Given el evento Hit-the-Canvas y la espera de respuestas
    Then el Chat NO bloqueará el Canvas (Modal Overlay prohibido); el usuario mantendrá capacidades de `Drag to Pan` y `Zoom` libremente.
    And las nuevas cajas inyectadas brillarán con un "Halo Verde" efímero.
    And si el humano presiona `CTRL + Z`, el framework revertirá atómicamente (en 1 solo paso) toda la inyección de la IA.
    And la IA aplicará "Smart Merge", respetando las cajas que el humano haya borrado a mano previamente.
    And si el chat está minimizado, un Badge Rojo y un PING sonoro alertarán de preguntas pendientes.

  Scenario: Tolerancia Humana a ISO 9001 y Limpieza de Notas (CA-09)
    Given las alertas ISO 9001 (con Popovers gráficos de Antes/Después) y Notas Adhesivas en el lienzo
    When el Arquitecto ignora una alerta visual editando otras partes 3 veces consecutivas
    Then el Copiloto desistirá asumiendo la responsabilidad humana (Override), silenciando la alerta visual y guardando el log forense con tipografía en **Negritas** para decisiones categóricas.
    And si el Frontend detecta que el humano vinculó el Formulario (Pantalla 7) esperado, borrará automáticamente la Nota Adhesiva obsoleta asociada.

  Scenario: El Antídoto contra el Despliegue Fantasma (Executable Flag) (CA-10)
    Given que la IA generó un flujo con caminos lógicos rotos o inconclusos
    When el XML incluya la etiqueta `<bpmn:process isExecutable="false">`
    Then el Frontend cruzará esta bandera e imprimirá un Banner Bloqueante Rojo sobre el Canvas dictando: "Diseño Corrompido por la IA. Repare el Nodo [ID] antes de desplegar".
    And el botón `[🚀 DESPLEGAR]` (US-005) permanecerá físicamente inhabilitado.
```
**Trazabilidad UX:** Wireframes Pantalla 6 (Diseñador BPMN - Panel Lateral de Copilot interactivo y Dropzone).

---

### US-032: Orquestación de IA y Generative Task (RAG)
**Como** Arquitecto Funcional
**Quiero** disponer de tareas especializadas en IA dentro del diseñador BPMN
**Para** modelar flujos donde un Agente de IA analiza documentos y redacta contenido estructurado sin interrumpir el motor lógico de Camunda.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Componentes AI-Native BPMN y Controles
  Scenario: Output Estricto basado en Schema JSON (CA-1 - Opción A)
    Given el Arquitecto configura una "Generative Task (RAG)" en la Pantalla 6
    Then el motor de IA tiene prohibido generar formato visual (HTML/Docs) directamente
    And está forzado a devolver la información estructurada mediante un Esquema JSON (Extraer y Rellenar)
    And el iBPMS fusiona ese JSON con la plantilla inmutable oficial antes de mostrársela al usuario final.

  Scenario: Desbloqueo de Conocimiento PII con Políticas Estrictas (CA-2)
    Given la tarea "Generative Task (RAG)" necesita consultar la Bóveda SGDEA
    When el LLM busca contexto en documentos clasificados como Privados o PII
    Then se permite la lectura para la generación de la respuesta
    But la política de seguridad (RBAC/DLP) enmascara o prohíbe exponer directamente estos datos sensibles al usuario que no tiene dichos privilegios.
    
  Scenario: Vectorización de Conocimiento a Demanda (CA-3)
    Given el Administrador de Conocimiento sube archivos al SGDEA
    Then dispone de un botón "[Actualizar Memoria IA (Embeddings)]"
    And la vectorización a la base de datos vectorial (Ej: Milvus/Pinecone) no ocurre automáticamente en cada subida de archivo para no degradar el rendimiento, sino de forma controlada y explícita.

  Scenario: Botón de Pánico Anti-Alucinaciones (CA-4)
    Given un usuario humano revisa un borrador generado por la IA en su formulario (Pantalla 7)
    Then dispone de un botón global estilo "[👎 Reportar Alucinación / Error IA]"
    And al accionarlo, se cancela el comportamiento automático, se emite una alerta al "Ingeniero de Prompts" y el proceso se redirige al flujo manual por defecto.

  Scenario: Trazabilidad a las Fuentes (Citas Interactivas) (CA-5)
    Given la "Generative Task (RAG)" emite una respuesta argumentativa
    Then el texto debe incluir referencias o hipervínculos a los IDs documentales usados como contexto
    And cuando el usuario hace clic, el iBPMS lo redirige al visor del SGDEA con la sección exacta resaltada, garantizando verificación humana.

  Scenario: Budget Configurable de Tokens LLM (CA-6)
    Given el Arquitecto configura la tarea cognitiva en la Pantalla 6
    Then existe un parámetro limitante de "Budget de Tokens / Consumo Mensual"
    And si el proceso agota su cuota asiganada, se corta el acceso a la IA y el motor enruta a las ramas B (flujos manuales alternativos) automáticamente.

  Scenario: Gobernanza de Prompts Centralizada (CA-7)
    Given la necesidad de alterar las instrucciones base de los Agentes RAG
    Then existe una pantalla separada llamada "Enterprise Prompt Library"
    And solo los usuarios con el rol especializado `prompt_engineer` tienen permisos CRUD sobre estos prompts globales, dejando a los Arquitectos BPMN únicamente con la facultad de consumirlos.

  Scenario: Tolerancia a Fallos Multi-LLM (Failover Pattern) (CA-8)
    Given la tarea "Generative Task (RAG)" está configurada para consumir un modelo principal (Ej: Azure OpenAI GPT-4o)
    When este proveedor primario sufre una caída (Downtime / HTTP 503)
    Then el Agente Orquestador del iBPMS no detiene el proceso inmediatamente
    And realiza un salto automático transparente (Failover) a un modelo de respaldo configurado (Ej: AWS Bedrock Claude 3.5) para garantizar la continuidad operativa.

  Scenario: Auditoría Transparente de IA sin Marcas de Agua (CA-9)
    Given un PDF oficial generado a partir de una plantilla con texto redactado por IA
    When el usuario humano aprueba el texto y el sistema emite el documento final
    Then el documento PDF NO incluye advertencias públicas ni marcas de agua de "Generado por IA" para conservar la imagen corporativa
    And el iBPMS persiste en su base de auditoría interna un registro estricto inmutable indicando "Borrador Generado por IA - Validado y Aceptado bajo responsabilidad del usuario [Nombre] con ID [X]".

  Scenario: Parametrización de Límites de Lectura Comprensiva (CA-10)
    Given un expediente que incluye anexos documentales extremadamente extensos (+500 páginas)
    When el proceso pasa los anexos como contexto al Agente RAG
    Then el Arquitecto BPMN puede haber parametrizado "Límites de Extracción" en la configuración de la tarea cognitiva
    And el sistema recorta inteligentemente el contexto a enviar (Ej: "Evaluar solo las primeras 20 páginas" o "Límite: 100k tokens") previniendo gastos desmesurados de cómputo.

  Scenario: Control Bidireccional de Tono Redaccional (CA-11)
    Given el Arquitecto BPMN arrastra una Generative Task al lienzo en Pantalla 6
    Then el panel de propiedades incluye un Dropdown "Tono de Comunicación" (Ej: Empático, Formal/Legal, Comercial)
    And esta instrucción se inyecta dinámicamente como Sistema al Prompt principal sin requerir que el Arquitecto reescriba el Prompt base de la librería.

  Scenario: Validación Invisible de Doble Agente (Self-Reflection) (CA-12)
    Given el modelo LLM principal genera un borrador de respuesta
    Then en flujos de criticidad alta, el iBPMS enruta temporalmente ese borrador a un segundo "Agente Validador Invisible"
    And si el Validador detecta Alucinaciones graves o violaciones de PII, obliga al modelo principal a reescribir la respuesta internamente antes de presentársela al analista humano en su Workdesk.

  Scenario: Auditoría Legal del Prompt Exacto (CA-13)
    Given que el "Ingeniero de Prompts" altera el prompt oficial corporativo frecuentemente
    When un proceso cognitivo finaliza y guarda la respuesta generada
    Then la base de datos almacena el texto íntegro e inmutable del Prompt específico que se usó en ese milisegundo exacto
    And permitiendo auditorías forenses (¿Qué le ordenamos a la IA ese día?) años después del evento de ejecución.

  Scenario: Bucle de Retroalimentación Humana (RLHF) (CA-14)
    Given el Abogado recibe un borrador generado por la IA en su formulario
    When el Abogado rechaza el texto y lo reescribe manualmente antes de enviar
    Then el iBPMS guarda el par de datos "[Borrador IA Original] vs [Texto Humano Final]" en una base de datos de telemetría MLOps
    And este corpus queda disponible para futuras sesiones de ajuste fino (Fine-tuning) del modelo base corporativo.

  Scenario: Aseguramiento DLP e IT Security en Nube Pública (CA-15)
    Given que el LLM está hospedado fuera de la infraestructura local (Ej: Azure, OpenAI)
    When el iBPMS emite el llamado de red con el contexto (Cuerpo de PQRS)
    Then un interceptor de Seguridad IT / DLP (Data Loss Prevention) evalúa y enmascara PII (Nombres, Cédulas, Tags) reemplazándolos por Hash-Tokens pseudo-anonimizados
    And la IA procesa los hashes, y al devolver la respuesta redactada, el interceptor re-hidrata los Hashes a su valor original PII dentro del perímetro seguro local.

  Scenario: Traducción Activa de Salida (CA-16 - Diferido a V2)
    Given el cliente escribe en un idioma extranjero (Ej: Inglés)
    # NOTA: Diferido a V2. En V1 la IA entiende el inglés pero la instrucción general del Prompt fuerza la respuesta en Español.

  Scenario: Adjuntos Generativos y Bucle de RLHF Documental (CA-17)
    Given una tarea "Generative Task (RAG)" configurada para exportar un archivo .DOCX
    When la IA redacta el contenido y genera el documento asociado al proceso
    Then si el humano no lo acepta y edita el archivo Word subiéndolo de nuevo (o haciendo comentarios)
    And el iBPMS captura el "Delta" (diferencias) entre el documento IA y la corrección humana para usarlo como métrica de retroalimentación de calidad.

  Scenario: Bucle Iterativo por Notas o Comentarios (CA-18)
    Given el usuario revisa el borrador generado por la IA y no está satisfecho
    When en lugar de editarlo manualmente, opta por la revisión guiada
    Then utiliza un panel de "Notas / Comentarios" para instruir correcciones (Ej: "Hazlo más corto y cordial")
    And la tarea cognitiva vuelve a ejecutarse tomando ese comentario humano como contexto mandatorio para el re-intento.

  Scenario: Selección de Modelo a Nivel de Ejecución (CA-19)
    Given el Arquitecto BPMN ha parametrizado "Metadatos de Sugerencia" indicando qué IA usar (Ej: Nivel Inferior)
    When la tarea cognitiva llega al Workdesk del usuario final
    Then el Usuario Ejecutor es quien tiene la potestad final en la UI para elegir qué modelo exacto procesará la solicitud, utilizando la sugerencia como base.

  Scenario: Termómetro de Seguridad (Confidence Score) (CA-20)
    Given el modelo LLM genera una respuesta
    Then el sistema debe mostrar visualmente en el Workdesk un "Confidence Score" (Nivel de Certeza)
    And advirtiendo al revisor humano si la certidumbre matemática de la IA es peligrosamente baja.

  Scenario: Transparencia Cognitiva Continua (Chain of Thought visible) (CA-21)
    Given que el LLM estructura un argumento complejo
    Then el sistema debe solicitar y capturar el "Chain of Thought" (Paso a paso lógico de la IA)
    And exponerlo como un log oculto pero auditable en la metadata de la instancia para que el administrador/humano entienda el "por qué" de la decisión.

  Scenario: Contexto Humano Ad-Hoc en Vivo (CA-22)
    Given la memoria base de la IA (SGDEA) está limitada
    Then el usuario final que está revisando la tarea puede, en tiempo real, adjuntar un PDF local desde su PC
    And ordenar a la IA que reevalúe y genere un nuevo borrador incluyendo ese documento exclusivo y saltándose el RAG tradicional.

  Scenario: Versionamiento y Máquina del Tiempo de Prompts (CA-23)
    Given el modulo Enterprise Prompt Library
    When el Ingeniero de Prompts realiza alteraciones al texto de instrucción
    Then el sistema crea versiones inmutables al estilo Git (v1, v2)
    And existe un mecanismo de reversión instantánea (Rollback) por si el nuevo prompt causa degradación operativa generalizada.

  Scenario: Cola de Procesamiento por Lotes (Batch Dispatcher) (CA-24)
    Given un volumen alto de invocaciones a la IA
    Then el sistema enruta estas tareas a una "Cola de Despacho" paramétrizable por el Administrador de Prompts
    And esta cola maneja límites de concurrencia, reintentos por falla de la API, y estrategias Backoff automáticas.

  Scenario: Interfaz Asíncrona sin Bloqueo de Navegación (CA-25)
    Given la generación de IA está ejecutándose en la Cola de Despacho (CA-24)
    Then la Interfaz de UI presenta un mensaje personalizado indicando que el proceso "Está siendo procesado por IA"
    And NO bloquea al usuario, permitiéndole paralelamente atender otras tareas u operar otras pantallas del iBPMS libremente.

  Scenario: Gatillo Exclusivamente Imperial (Acción Humana) (CA-26)
    Given un flujo de procesamiento que involucra el componente de IA
    Then el sistema tiene una regla arquitectónica imperativa: la invocación a la IA no puede ocurrir por auto-transición 100% de fondo de Camunda
    And exige siempre que el Gatillo (Trigger) originario haya sido el "Clic" explícito de un Usuario Humano en la pantalla precedente o actual, impidiendo escapes automatizados ciegos.

  Scenario: UX de Carga Asíncrona (Prevención de Streaming) (CA-27)
    Given la generación de un documento IA
    Then la interfaz de usuario utiliza un "Loading Spinner" tradicional y peticiones HTTP estándar en lugar de WebSockets (Efecto Máquina de Escribir)
    And priorizando la eficiencia de memoria del servidor web B2B frente a la espectacularidad visual.

  Scenario: Regeneración Parcial por Fragmentos (CA-28)
    Given un borrador extenso generado por la IA en la pantalla del analista
    When el usuario selecciona únicamente un párrafo y hace clic en "Comentar / Corregir"
    Then el iBPMS enruta a la IA exclusivamente el fragmento seleccionado junto con la instrucción humana (Ej: "Haz este párrafo más formal")
    And la IA devuelve el fragmento modificado, fusionándose in-place sin necesidad de reescribir ni gastar tokens en el texto adyacente que ya fue aprobado.

  Scenario: Privacidad de Auditoría Cognitiva (CA-29)
    Given que el proceso generó métricas de "Confidence Score" y "Chain of Thought"
    Then estas métricas son de consumo estrictamente interno
    And por ningún motivo se exponen al Ciudadano Externo en la Pantalla 18 (Portal B2B/B2C).

  Scenario: RAG Multimodal Controlado (V1) (CA-30)
    Given la ingesta de documentos anexos para contextualizar a la IA
    Then el Agente RAG soporta en su V1 la lectura de documentos PDF, DOCX e Imágenes (OCR integrado a la API de visión)
    And excluyendo formalmente notas de voz o video (Diferido a V2).

  Scenario: Eficiencia de Contexto Pre-Empaquetado (IA Amarrada) (CA-31)
    Given una tarea generativa que requiere datos externos (Ej: Saldos ERP)
    Then la IA tiene prohibido usar "Function Calling" autónomo para ir a buscar datos por su cuenta (Gasto excesivo de tokens y memoria de razonamiento)
    And la arquitectura dicta que Camunda Engine, mediante Service Tasks previas y baratas, extraiga la data y se la entregue pre-empaquetada en el Prompt a la IA para que esta se limite únicamente a redactar.
```
**Trazabilidad UX:** Pantalla 6 (BPMN Designer Palette), Pantalla 12 (SGDEA), Pantalla 7 (Form Builder UI).

---


### US-052: Motor de Orquestación Multi-Agente IA (Arquitectura y Gobernanza de Contextos)
Descripción: 
**Como** Administrador de la Plataforma iBPMS, 
**Quiero** configurar y operar un motor de inteligencia artificial compuesto por 4 Agentes Especializados (Orquestador, Backend, Frontend y QA) con inyección de contexto dinámica y reglas diferenciadas, 
**Para** evitar la saturación de tokens (Context Overload), prevenir alucinaciones mediante separación estricta de memorias y emular una fábrica de software autónoma segura dentro del iBPMS.

Contexto de Negocio & Arquitectura
Actualmente, los motores de IA monolíticos pierden el contexto o alucinan si se les sobrecarga con reglas. Esta historia establece la infraestructura para que el iBPMS administre reglas globales (CORE) que aplican a todos los agentes, y políticas modulares (Específicas) que solo se inyectan en tiempo de ejecución ("Just-in-Time"), replicando el modelo exitoso de Antigravity.

**Criterios de Aceptación (CA)**
```gherkin
CA-01: Definición del Rol "Arquitecto Orquestador"
Criterio: El sistema debe inicializar un Agente Maestro sin capacidad de escritura de código productivo. Given que un usuario solicita la creación de un nuevo proceso BPMN complejo a la IA, When la petición ingresa al motor de orquestación, Then el sistema invoca exclusiva y aisladamente al "Agente Orquestador", And este agente debe generar contratos de delegación (Handoffs JSON/Markdown) dirigidos a los Agentes Especialistas en lugar de intentar programar la solución.

CA-02: Separación Estricta de Memoria entre Especialistas (Backend, Frontend, QA)
Criterio: Los estados conversacionales de los 4 agentes jamás deben compartirse directamente para prevenir contaminación cruzada. Given que el Orquestador ha diseñado un Handoff para el "Especialista Backend", When el sistema despierta al Agente Backend en su propio hilo de ejecución (Thread), Then el Agente Backend DEBE tener un "System Prompt" en blanco respecto a las charlas del Orquestador, conociendo única y exclusivamente las instrucciones pasadas a través del paquete Handoff.

CA-03: Administración de Reglas CORE Universales (Equivalente a .cursorrules)
Criterio: Existencia de un repositorio de directrices globales obligatorias. Given que un administrador de plataforma ha configurado reglas críticas de seguridad (Ej. Inmunidad de Arranque / Zero-Trust Git) en el panel de Configuración AI Core, When el sistema arranca cualquier instancia de los 4 Agentes de IA, Then el motor iBPMS inyecta automáticamente esas reglas CORE en el inicio del System Prompt, consumiéndolas obligatoriamente en cada inferencia de red neuronal.

CA-04: Inyección Modular "Just-In-Time" (Equivalente a scaffolding/workflows/)
Criterio: Optimización de tokens mediante políticas específicas de rol bajo demanda. Given que el sistema almacena manuales extensos (Reglas UX/UI, Arquitectura Hexagonal Java, Guías funcionales QA), When el Orquestador delega una tarea de interfaz de usuario al "Agente Frontend", Then el motor iBPMS inyecta en la memoria RAM del Agente Frontend únicamente la Política Modular de "Reglas UX/UI", omitiendo el peso de los manuales de Java o QA para maximizar la capacidad de razonamiento del LLM sin sobrepasar su Ventana de Contexto (Context Limit).

CA-05: El Humano como Bus de Datos (Enrutador de Aprobaciones)
Criterio: La aplicación del Gobierno Técnico estricto donde el humano no es aprobador autónomo. Given que un Agente Especialista (ej. Backend) termina su plan de implementación y requiere validación, When la IA emite un mensaje de estado PENDING_APPROVAL, Then la UI del iBPMS no le pide al humano que lo valide técnicamente, sino que le notifica: "El Agente Backend requiere revisión técnica. Lleva este plan al Agente Orquestador", And el motor iBPMS transfiere el payload al Orquestador, quien lo audita, evalúa los "GAPs", y emite el veredicto definitivo de regreso a la cola de ejecución.

Notas de Implementación (Non-Functional Requirements)
Aislamiento Tecnológico: Las llamadas a la API de LLM (OpenAI / Gemini) deben hacerse en sesiones HTTP aisladas.
Bandeja de Entrada Común: Simular la carpeta .agentic-sync/ creando una tabla en Base de Datos ai_handoff_queue donde los agentes depositarán sus contratos en estado DRAFT, APPROVED y STASHED.
```


### US-053: Antigravity Command Center (Fábrica de Agentes IA y Arbitraje FinOps B2B)
**Como** Administrador del Tenant (Cliente B2B)
**Quiero** un panel de control para crear "Agentes de IA" y gestionar mi consumo mediante un Modelo Híbrido (Cuota de Suscripción Base vs. Billetera de Reserva Prepaga)
**Para** orquestar fuerza laboral artificial en mis procesos BPMN sin riesgo de facturas sorpresa, garantizando que mis flujos críticos no colapsen por falta de fondos y auditando el costo exacto de cada Agente.

**Criterios de Aceptación (CA)**
```gherkin
Feature: AI Agent Factory, B2B Token Arbitrage & BPMN FinOps Resilience

  # ==============================================================================
  # A. LÓGICA DE CONSUMO HÍBRIDO (ARBITRAJE DE TOKENS) Y DASHBOARD VISUAL
  # ==============================================================================
  Scenario: Bifurcación Visual de Suscripción vs. Billetera (The Antigravity UI) (CA-01)
    Given la interfaz del "Antigravity Command Center" (Panel de Gobernanza IA)
    Then el Frontend renderizará dos secciones financieramente independientes:
    And 1. "MODEL QUOTA" (La Suscripción): Barras de progreso horizontales separadas por Tier de Inteligencia (Ej: `Gemini 1.5 Flash` vs `Gemini 1.5 Pro`). Se miden en Tokens virtualizados y muestran su fecha/hora de reseteo automático mensual.
    And 2. "MODEL CREDITS" (La Reserva Prepaga): Un contador numérico general tipo cuenta bancaria con el Saldo Vitalicio comprado por el cliente.

  Scenario: Transición Controlada y Bloqueo de Factura Sorpresa (Opt-In Overages) (CA-02)
    Given que un Agente IA agota el 100% de la "Model Quota" de su Tier asignado
    When el Agente intenta ejecutar una nueva inferencia para un proceso
    Then el sistema verificará el interruptor maestro `[Enable AI Credit Overages]` en la UI.
    And si está APAGADO, la transacción se aborta inmediatamente (Hard-Stop) para proteger el presupuesto del cliente.
    And si está ENCENDIDO, el sistema ejecuta un Auto-Deduct silencioso, restando los tokens de la "Billetera Prepaga" (Model Credits), aplicando un multiplicador de costo si el Agente usa un modelo Premium.

  Scenario: Alertas Proactivas de Umbral (Thresholds) (CA-03)
    Given el consumo en tiempo real de una "Model Quota"
    When la barra de consumo alcance matemáticamente el 80% y luego el 95%
    Then un proceso asíncrono despachará alertas automatizadas (Campana UI y Email) al Administrador del Tenant.
    And advirtiendo el inminente bloqueo operativo o la transición inminente hacia la facturación prepaga.

  # ==============================================================================
  # B. FÁBRICA DE AGENTES Y PRESUPUESTOS POR ROL
  # ==============================================================================
  Scenario: Creación de Agentes y Control de Gasto Granular (CA-04)
    Given la pestaña "Fábrica de Agentes"
    When el Administrador pulsa `[+ Crear Nuevo Agente]`
    Then el sistema exigirá definir: Nombre, Motor LLM Agnóstico (Ej: Gemini Ultra), y el `System Prompt` (Rol y Reglas del agente).
    And el panel incluirá un candado financiero individual: `[x] Autorizar a este Agente a consumir de la Billetera Prepaga`.
    And si este candado está desmarcado, el Agente NUNCA podrá gastar dinero extra, fallando silenciosamente al agotarse la cuota gratuita mensual, incluso si el interruptor maestro del Tenant está encendido.

  # ==============================================================================
  # C. RESILIENCIA DEL MOTOR BPMN ANTE FALTA DE FONDOS
  # ==============================================================================
  Scenario: Suspensión Elegante de Service Tasks (Camunda Incident) (CA-05)
    Given un Proceso BPMN automatizado que invoca a un Agente IA en segundo plano
    When el Backend detecta que la Suscripción está agotada Y la Billetera Prepaga no tiene fondos (o el Overage está apagado)
    Then la arquitectura TIENE ESTRICTAMENTE PROHIBIDO lanzar una excepción fatal HTTP 500 que destruya la instancia del proceso de negocio.
    And el Worker interceptará el fallo financiero y levantará un "Incidente de Camunda" (Estado: `ESPERANDO_SALDO_IA`).
    And la tarea quedará congelada de manera indefinida hasta que el cliente recargue fondos y el Administrador presione `[Reintentar]` en la cabina de control, retomando el flujo ileso.

  # ==============================================================================
  # D. ADMINISTRACIÓN DE CARTERA, CADUCIDAD Y TRAZABILIDAD
  # ==============================================================================
  Scenario: Reglas de Caducidad Asimétrica (Rollover y Reset) (CA-06)
    Given la llegada del día 1 de cada mes a las 00:00 UTC
    Then un Cron Job reseteará las "Model Quotas" (Suscripción base) a su valor nominal inicial (Use-it-or-lose-it).
    And el saldo de los "Model Credits" (Billetera Prepaga) TIENE PROHIBIDO ser reseteado o caducar, acumulándose vitaliciamente mes a mes.

  Scenario: Trazabilidad FinOps Exacta en la Factura (Billing Source) (CA-07)
    Given una invocación exitosa a cualquier API de IA (Google/Anthropic)
    Then el Backend registrará el costo real consumido leyendo el Payload de respuesta (Prompt Tokens + Completion Tokens).
    And inyectará OBLIGATORIAMENTE en la bitácora inmutable una columna `billing_source` cuyo valor será `SUBSCRIPTION_QUOTA` o `OVERAGE_WALLET`.
    And registrará el `Agent_ID` asociado, permitiendo exportar reportes gerenciales para auditar qué procesos salieron gratis y cuáles costaron saldo de reserva.

  Scenario: Inyección Manual de Saldo Offline (MVP V1) (CA-08)
    Given un cliente que adquiere un paquete de "Tokens de Reserva" pagando una factura externa (Offline)
    Then el sistema proveerá un endpoint administrativo protegido (Exclusivo para el Súper Admin del iBPMS).
    And permitirá inyectar recargas manuales (Top-Ups) sumando créditos a la billetera vitalicia del Tenant.
    And la integración nativa de pasarelas de pago automáticas (Stripe/PayPal) queda diferida para V2.

Scenario: Downgrade Automático por Falta de Fondos Premium (Fallback Cognitivo)
    Given un Agente IA configurado para usar un modelo Premium (Ej: Gemini Ultra) y el interruptor Overage apagado
    When el Agente intenta inferir y el Billing Engine rechaza la transacción por fondos insuficientes en su Tier
    Then el Backend TIENE PROHIBIDO suspender la tarea BPMN de manera inmediata levantando el incidente.
    And el motor intentará un "Downgrade Fallback" automático hacia el modelo Estándar (Ej: Gemini Flash) SI Y SOLO SI este Tier aún posee cuota mensual gratuita.
    And si el modelo Estándar logra resolverlo, el proceso avanza estampando en la auditoría: `[PROCESADO_POR_FALLBACK]`.
    And solo si el modelo Estándar también agota sus tokens (Bolsa en 0), el Worker levantará el incidente en Camunda (`ESPERANDO_SALDO_IA`), priorizando siempre la continuidad operativa.

```
---

### US-054: Integración Agnóstica de Modelos Fundacionales (LLM Plugin Engine)
**Como** Arquitecto de IA / Motor de Procesamiento (Backend)
**Quiero** invocar a diferentes proveedores de Inteligencia Artificial (Ej: Gemini, OpenAI, Claude) de forma estandarizada y agnóstica utilizando un sistema de Plugins y "Raw Ports"
**Para** proteger a la plataforma ibpms-core del vendor lock-in corporativo, evitar incorporar librerías gigantescas asfixiantes (como LangChain), aislar los Secretos de autenticación (Zero-Trust) y permitir la transición fluida (Fallback) en caso de que uno de los proveedores sufra una caída regional.

> [!IMPORTANT]
> **Directiva de Implementación: Refactorización desde OpenClaw (Código Fuente de Referencia)**
> Esta historia de usuario DEBE implementarse tomando como base de referencia arquitectónica y funcional el código fuente original del proyecto OpenClaw, ubicado en el workspace local:
> `C:\Users\HaroltAndrésGómezAgu\.gemini\antigravity\scratch\openclaw_workspace`
>
> Los módulos de referencia principales son:
> - `src/plugin-sdk/provider-entry.ts` — Fábrica de registro de Proveedores LLM (defineSingleProviderPluginEntry, ProviderPlugin)
> - `src/plugin-sdk/provider-stream.ts` — Composición de wrappers de streaming por familia (ProviderStreamFamily, composeProviderStreamWrappers)
> - `src/plugin-sdk/provider-stream-shared.ts` — Helpers compartidos de streaming y decodificación de tool calls
> - `src/plugin-sdk/provider-auth.ts` — Gestión de autenticación multi-perfil (AuthProfileStore, OAuthCredential, PKCE)
> - `src/plugin-sdk/provider-auth-runtime.ts` — Runtime de resolución de API Keys y auth por modelo (resolveApiKeyForProvider, getRuntimeAuthForModel)
> - `src/plugin-sdk/provider-tools.ts` — Normalización de Tool Schemas por proveedor (Gemini, xAI compat)
> - `src/plugin-sdk/provider-model-shared.ts` — Catálogo de modelos, replay policies y compatibilidad (ReplayFamily, ModelCompat)
> - `src/plugin-sdk/provider-onboard.ts` — Onboarding y preset de proveedores (applyProviderConfigWithDefaultModels)
> - `src/plugin-sdk/provider-catalog-shared.ts` — Catálogo compartido de modelos configurados por proveedor
> - `src/plugin-sdk/provider-http.ts` — Transporte HTTP nativo (fetchWithTimeout, postJsonRequest, assertOkOrThrowHttpError)
> - `src/plugin-sdk/retry-runtime.ts` — Política de reintentos y rate limiting (RetryConfig, createRateLimitRetryRunner)
> - `src/plugin-sdk/provider-enable-config.ts` — Habilitación condicional de proveedores por configuración
>
> **Ejercicio de Refactorización Obligatorio:** El código de OpenClaw está escrito en TypeScript (Node.js). El equipo de desarrollo DEBE ejecutar un ejercicio formal de refactorización (Porting) para transcribir los contratos, interfaces y algoritmos centrales hacia Java 21 / Spring Boot 3, respetando la Arquitectura Hexagonal (ADR-001) y el patrón de Puertos y Adaptadores del iBPMS. Se prohíbe la copia literal sin adaptación; se exige la comprensión profunda de cada componente antes de su transposición.

**Dependencias Críticas:**
- **US-053 (Antigravity Command Center):** ⚠️ FUERTE. Las cuotas FinOps de Model Credits del Tenant gobiernan el presupuesto de tokens disponible para cada adaptador LLM.
- **US-056 (Memory Core Engine / RAG):** ⚠️ FUERTE. El `LlmChatPort` definido en CA-01 es la interfaz que US-056 consume para generar embeddings y ejecutar las fases de Dreaming.
- **ADR-012 (Integración Agnóstica LLM):** 🔴 BLOQUEANTE. Los adaptadores DEBEN seguir el patrón Zero-Dep de `RestClient` nativo.
- **ADR (Azure Key Vault):** 🔴 BLOQUEANTE. La bóveda de secretos debe estar operativa para la inyección de API Keys.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Architectural LLM Decoupling and Native Connectivity (ADR-012)

  # ==============================================================================
  # A. CONTRATOS HEXAGONALES DEL MOTOR LLM
  # Ref. OpenClaw: src/plugin-sdk/provider-entry.ts (ProviderPlugin, definePluginEntry)
  # Ref. OpenClaw: src/plugin-sdk/provider-model-shared.ts (ModelApi, ModelProviderConfig)
  # ==============================================================================
  Scenario: Invocación Agnóstica de Tareas Funcionales (Domain Port Isolation) (CA-01)
    # Ref. OpenClaw: provider-entry.ts — ProviderPlugin interface con id, label, auth[], catalog
    # Ref. OpenClaw: provider-model-shared.ts — ModelDefinitionConfig (id, contextWindow, reasoning, input)
    Given una tarea requerida de procesamiento LLM (Ej. Resumir Bandeja de Entrada o Evaluar Formulario) instanciada por Orquestador BPMN
    When el Caso de Uso (Application Service) invoque una solicitud cognitiva
    Then el servicio TIENE PROHIBIDO acoplarse y llamar directamente a Azure OpenAI o Google Vertex.
    And delegará su firma obligatoria hacia un puerto neutral `LlmChatPort(LlmContext, BusinessPrompt)`.
    And el Contexto transferido contendrá las fronteras máximas parametrizadas en la fábrica (Tokens máximos, temperatura) desde el Dominio al Adaptador, aislando las lógicas puramente transaccionales del formato de API.
    And OpenClaw implementa este patrón exacto con su `ProviderPlugin` interface que define: `id`, `label`, `docsPath`, `aliases`, `envVars`, `auth[]`, `catalog` y hooks opcionales (`wrapStreamFn`, `buildReplayPolicy`, `normalizeToolSchemas`) — el equipo debe estudiar este contrato como referencia directa del `LlmChatPort`.

  # ==============================================================================
  # B. TRANSPORTE HTTP NATIVO (ZERO-DEP)
  # Ref. OpenClaw: src/plugin-sdk/provider-http.ts (fetchWithTimeout, postJsonRequest)
  # Ref. OpenClaw: src/plugin-sdk/provider-stream-shared.ts (composeProviderStreamWrappers)
  # ==============================================================================
  Scenario: Zero-Dep Abstraction via RestClient (Evitación de Bloatware) (CA-02)
    # Ref. OpenClaw: provider-http.ts — fetchWithTimeout(), postJsonRequest(), assertOkOrThrowHttpError()
    # Ref. OpenClaw: provider-stream-shared.ts — StreamFn wrapper composition, HTML entity decoding
    Given la implementación del Adapter (`AzureOpenAiAdapter` / `GoogleGeminiAdapter`) subyacente al Port
    Then el Backend REST iBPMS TIENE RESTRICCIÓN TOTAL para importar librerías tipo `spring-ai` o `langchain4j`.
    And el adaptador implementará la conexión de red (HTTP Request/Streaming Websockets) EXCLUSIVAMENTE a través de construcciones NATIVAS standard (Ej: `RestClient` o `WebClient` fluido de Spring Boot 3).
    And minimizando radicalmente la superficie de ataque del código (Cero vulnerabilidades CVE importadas por terceros) y controlando la serialización de JSON con Jackson puro.
    And OpenClaw demuestra este paradigma Zero-Dep en `provider-http.ts` donde TODO el transporte HTTP se resuelve con `fetch()` nativo de Node + helpers `fetchWithTimeout()` y `postJsonRequest()` — SIN importar axios, got ni SDKs de proveedor.

  # ==============================================================================
  # C. GESTIÓN ZERO-TRUST DE SECRETOS
  # Ref. OpenClaw: src/plugin-sdk/provider-auth.ts (AuthProfileStore, OAuthCredential, PKCE)
  # Ref. OpenClaw: src/plugin-sdk/provider-auth-runtime.ts (resolveApiKeyForProvider)
  # ==============================================================================
  Scenario: Inyección Zero-Trust de Secret Level Tokens (Key Vault) (CA-03)
    # Ref. OpenClaw: provider-auth.ts — ensureAuthProfileStore(), listProfilesForProvider(), upsertAuthProfile()
    # Ref. OpenClaw: provider-auth.ts — generatePkceVerifierChallenge(), buildOauthProviderAuthResult()
    # Ref. OpenClaw: provider-auth-runtime.ts — resolveApiKeyForProvider(), getRuntimeAuthForModel()
    Given la inicialización del Adapter dentro de la capa `infrastructure` en el microservicio
    When el conector requiera la API Key de Anthropic o Bearer Auth de Microsoft Graph
    Then se prohíbe que cualquier variable queme su ruta en código (`hardcoding`) o baje su token hacia el Frontend VUE SPA.
    And las credenciales fluirán vía inyección efímera inyectada (`Azure Key Vault` Secret ref en runtime) hacia los Headers HTTP autorizados que parten desde el servidor back-end ibpms a la DMZ pública de internet.
    And OpenClaw implementa este patrón con un `AuthProfileStore` persistente que resuelve credenciales por `providerId` + `profileId`, soporta `OAuthCredential` con PKCE y almacena tokens en un filesystem encriptado — iBPMS transpondrá este contrato hacia Azure Key Vault como backend de almacenamiento seguro.

  # ==============================================================================
  # D. ESTABILIDAD DE CACHÉ DE PROMPTS (FINOPS)
  # Ref. OpenClaw: src/plugin-sdk/provider-stream.ts (ProviderStreamFamily, stream wrappers)
  # Ref. OpenClaw: src/plugin-sdk/provider-model-shared.ts (ReplayPolicy, sanitizeReplayHistory)
  # ==============================================================================
  Scenario: Estabilidad FinOps del Caché de Prompts (Prompt Cache Stability) (CA-04)
    # Ref. OpenClaw: provider-stream.ts — buildProviderStreamFamilyHooks() con familias: google-thinking, openai-responses-defaults, etc.
    # Ref. OpenClaw: provider-model-shared.ts — buildReplayPolicy() para cada familia (OpenAI, Anthropic, Google Gemini)
    # Ref. OpenClaw: provider-stream-shared.ts líneas 154 — applyAnthropicEphemeralCacheControlMarkers (inserción de control de caché por bloque)
    Given un flujo conversacional o historial cognitivo que deba ser enviado al Adaptador LLM
    When el motor estructural ensamble el `Payload` (JSON) para la inferencia con el proveedor
    Then el Backend tiene PROHIBIDO alterar, truncar o reescribir tokens en la cabecera estática (`System Prompt` o bloque de instrucciones maestras).
    And rotará y apilará obligatoriamente los nuevos contextos (variables BPMN o mensajes nuevos) EXCLUSIVAMENTE en la cola ("Tail") del array.
    And esto garantizará un hit casi absoluto en el "Context Caching" de los LLMs (ej: OpenAI/Anthropic), preservando agresivamente los "Model Credits" (FinOps B2B).
    And OpenClaw implementa esta disciplina con sus `ReplayPolicy` builders que generan payloads compatibles con cada proveedor y sus `EphemeralCacheControlMarkers` (Anthropic) que marcan bloques inmutables para prompt caching.

  # ==============================================================================
  # E. RESILIENCIA Y FALLBACK COGNITIVO
  # Ref. OpenClaw: src/plugin-sdk/retry-runtime.ts (RetryConfig, createRateLimitRetryRunner)
  # Ref. OpenClaw: src/plugin-sdk/provider-onboard.ts (cascada de modelos fallback)
  # ==============================================================================
  Scenario: Resiliencia Dinámica y Fallback Cognitivo Automático (CA-05)
    # Ref. OpenClaw: retry-runtime.ts — resolveRetryConfig(), retryAsync(), createRateLimitRetryRunner()
    # Ref. OpenClaw: provider-onboard.ts — extractAgentDefaultModelFallbacks() para cascada de modelos
    # Ref. OpenClaw: provider-http.ts — assertOkOrThrowHttpError() y fetchWithTimeoutGuarded()
    Given un corte, timeout, asfixia (Rate Limit HTTP 429) o error de servidor (HTTP 500) devolviendo un fallo por parte del modelo IA primario (Ej: Azure OpenAI GPT-4o)
    When el Adaptador actual falle su promesa de resolver el prompt
    Then se PROHÍBE retornar inmediatamente una excepción técnica letal que aborte la tarea del orquestador Camunda.
    And iniciará un "Fallback Pasivo" transparente derivando el mismo contexto hacia el Adaptador Secundario Autorizado (Ej: Gemini 1.5 Flash).
    And únicamente si todos los modelos de la cascada de Fallback fracasan estrepitosamente, la plataforma recién detendrá el hilo, emitiendo el incidente `ERROR_COGNITIVO_BPMN`.
    And OpenClaw implementa esta resiliencia con `createRateLimitRetryRunner()` (reintentos con backoff para 429s), `fetchWithTimeoutGuarded()` (timeout configurable) y `extractAgentDefaultModelFallbacks()` (cascada de modelos).

  # ==============================================================================
  # F. AUTENTICACIÓN MULTI-TENANT Y DELEGACIÓN OAUTH
  # Ref. OpenClaw: src/plugin-sdk/provider-auth.ts (AuthProfileStore, OAuth, PKCE)
  # Ref. OpenClaw: src/plugin-sdk/provider-auth-runtime.ts (ProviderPreparedRuntimeAuth)
  # ==============================================================================
  Scenario: Perfiles de Autenticación Dinámicos Multi-Tenant (Auth Profiles) (CA-06)
    # Ref. OpenClaw: provider-auth.ts — ensureAuthProfileStore(), OAuthCredential, generatePkceVerifierChallenge()
    # Ref. OpenClaw: provider-auth.ts — upsertAuthProfileWithLock() (concurrencia segura), writeOAuthCredentials()
    # Ref. OpenClaw: provider-auth-runtime.ts — ResolvedProviderRuntimeAuth, ProviderPreparedRuntimeAuth
    Given un entorno multi-tenant o de ejecución paralela con distintos clientes (Tenants)
    When el sistema de adaptación HTTP solicite la conexión hacia un proveedor (OpenAI/Anthropic)
    Then la plataforma TIENE PROHIBIDO usar una sola Key global incrustada o forzada en el backend.
    And el motor delegará un `ProviderAuthRuntime` que consultará un "Perfil de Autenticación de Bóveda" (Ej: Perfil A para Depto Legal, Perfil B para Finanzas).
    And esto habilitará soportar esquemas de delegación OAuth (como plugins interactivos) y rotación de tokens por Tenant, asegurando segregación financiera.
    And OpenClaw implementa este patrón con un `AuthProfileStore` por agente que almacena credenciales por `providerId`/`profileId`, soporta flujos OAuth con PKCE, y resuelve auth en runtime via `getRuntimeAuthForModel()`.
    
    # -------------------------------------------------------------------------
    # Contexto Funcional y Visión UX (Delegación OAuth para Proveedores IA)
    # -------------------------------------------------------------------------
    # • La Experiencia (UX): En lugar de tener que ir a Google AI Studio o Azure, generar una "API Key" alfanumérica larguísima y pegarla cruda en la plataforma, iBPMS lanzará un flujo interactivo en formato de ventana flotante tipo "Login with Google Workspace / Gemini". El usuario ingresa a su cuenta personal, autoriza los permisos (Delegación OAuth), y pasa a operar el LLM bajo su propia suscripción y cuota.
    # • Lo que pasa "Bajo el Capó" (Backend): Técnicamente el sistema SÍ usa un token (un puente conversacional Access/Refresh Token), pero este es gestionado enteramente de forma silenciosa por el ProviderAuthRuntime. iBPMS tramita la rotación criptográfica automáticamente y el token se resguarda de forma encriptada en la Bóveda (Azure Key Vault), atado exclusivamente al perfil multi-tenant.
    # • El Escenario Paralelo (Multi-Cuenta): Con esta misma lógica, un usuario podría tener abierta una pestaña en iBPMS donde un proceso corporativo usa el "Azure OpenAI" (pagado por la compañía), y otra pestaña o perfil de agente donde configura el uso de "Su Gemini Personal" conectado a sus cuotas, sin que exista cruce o choque de seguridad entre ambas credenciales en el código.

  # ==============================================================================
  # G. NORMALIZACIÓN DE TOOL SCHEMAS POR PROVEEDOR
  # Ref. OpenClaw: src/plugin-sdk/provider-tools.ts (normalizeGeminiToolSchemas, stripUnsupportedSchemaKeywords)
  # ==============================================================================
  Scenario: Normalización Agnóstica de Tool Schemas para Function Calling (CA-07)
    # Ref. OpenClaw: provider-tools.ts — normalizeGeminiToolSchemas(), cleanSchemaForGemini(), stripXaiUnsupportedKeywords()
    # Ref. OpenClaw: provider-tools.ts — buildProviderToolCompatFamilyHooks() para compatibilidad cross-provider
    Given que los procesos BPMN expondrán herramientas (tools/functions) al LLM para ejecutar acciones en el sistema
    When el adaptador LLM necesite enviar los schemas de las herramientas disponibles al proveedor
    Then el sistema implementará un `ToolSchemaNormalizerService` que adapte automáticamente los schemas JSON-Schema de las herramientas según las restricciones del proveedor activo.
    And para Google Gemini: eliminará keywords no soportadas como `format`, `patternProperties`, `$ref` (replicando `cleanSchemaForGemini()` de OpenClaw).
    And para xAI: eliminará `minLength`, `maxLength`, `minItems`, `maxItems` (replicando `stripXaiUnsupportedKeywords()` de OpenClaw).
    And para OpenAI: aplicará compatibilidad directa sin transformación (schema pass-through).
    And el sistema PROHIBIRÁ la exposición de schemas internos o sensibles hacia el proveedor externo (Zero-Trust Schema Sanitization).

  # ==============================================================================
  # H. CATÁLOGO DINÁMICO DE MODELOS
  # Ref. OpenClaw: src/plugin-sdk/provider-catalog-shared.ts (ConfiguredProviderCatalogEntry)
  # Ref. OpenClaw: src/plugin-sdk/provider-onboard.ts (applyProviderConfigWithDefaultModels)
  # ==============================================================================
  Scenario: Catálogo Dinámico de Modelos por Proveedor (CA-08)
    # Ref. OpenClaw: provider-catalog-shared.ts — readConfiguredProviderCatalogEntries(), supportsNativeStreamingUsageCompat()
    # Ref. OpenClaw: provider-onboard.ts — applyProviderConfigWithDefaultModels(), applyProviderConfigWithModelCatalog()
    Given la necesidad de gestionar múltiples modelos por proveedor con distintas capacidades (reasoning, vision, context window)
    Then el sistema mantendrá un `ModelCatalogRegistry` que permita:
    And 1. **Registro Declarativo:** Cada adaptador de proveedor declarará sus modelos disponibles con metadata: `id`, `name`, `contextWindow`, `reasoning` (boolean), `input` (text/image/document).
    And 2. **Merge Inteligente:** Los modelos declarados por configuración YAML del administrador se fusionarán con los defaults del proveedor sin sobrescribirlos (patrón `mode: merge` de OpenClaw).
    And 3. **Streaming Usage Compat:** Modelos que soporten usage reporting en streaming serán marcados automáticamente para optimizar el tracking FinOps (replicando `applyProviderNativeStreamingUsageCompat()` de OpenClaw).
    And 4. **Resolución por Alias:** Los modelos podrán tener aliases (e.g., `gpt-4o` → `azure/gpt-4o-2024-08-06`) para simplificar la referencia desde los procesos BPMN.

  # ==============================================================================
  # I. AISLAMIENTO DE HISTORIAL COGNITIVO (RAG MEMORY SLOT)
  # Ref. OpenClaw: src/memory-host-sdk/ (ver US-056 para detalle completo)
  # ==============================================================================
  Scenario: Aislamiento del Historial Cognitivo a través de Vector Database (RAG Memory Slot) (CA-09)
    # Ref. OpenClaw: src/memory-host-sdk/dreaming.ts — Pipeline de consolidación cognitiva
    # Ref. OpenClaw: src/context-engine/types.ts — ContextEngine.assemble() para ensamblaje bajo presupuesto de tokens
    # Ref. Cruzada: US-056 (Memory Core Engine) — Esta historia define la implementación completa del RAG
    Given un largo ciclo de vida de un proceso (BPMN extendido por meses o agentes interactuando constantemente)
    When la ventana de contexto literal del LLM (Ej. 128k tokens) amenace con desbordarse o elevar drásticamente los costos
    Then el Plugin de Integración tiene RESTRICCIÓN TOTAL para simplemente enviar todos los historiales pasados en texto crudo (Raw Transcripts).
    And empleará un "Memory Core Engine" (una ranura formal de Memoria de Contexto, definida exhaustivamente en US-056).
    And automáticamente promoverá el historial transaccional antiguo hacia *Embeddings* guardados en una base Vectorial (Ej: `pgvector` de la DB del ADR correspondiente).
    And cuando el LLM necesite contexto pasado, el adaptador inyectará únicamente los "Chunks" semánticamente relevantes al prompt actual, creando un RAG pasivo.

  # ==============================================================================
  # J. DIRECTIVAS DE REFACTORIZACIÓN Y TRAZABILIDAD AL CÓDIGO FUENTE
  # ==============================================================================
  Scenario: Mapa de Transposición OpenClaw → iBPMS para LLM Plugin Engine (Refactoring Ledger) (CA-10)
    Given la directiva de implementación basada en el código fuente de OpenClaw
    Then el equipo de desarrollo DEBE mantener un documento de trazabilidad (`docs/architecture/llm-plugin-engine-refactoring-ledger.md`) que mapee:
    And 1. Cada archivo TypeScript de OpenClaw → su clase/interfaz Java equivalente en iBPMS.
    And 2. Decisiones de diseño donde se divergió del patrón original y la justificación técnica.
    And 3. Funciones de OpenClaw descartadas y la razón.
    And el mapa mínimo obligatorio de transposición es:
    And | OpenClaw (TypeScript) | iBPMS (Java) |
    And | `ProviderPlugin` interface (provider-entry.ts) | `LlmChatPort` (domain port) |
    And | `defineSingleProviderPluginEntry()` (provider-entry.ts) | `LlmProviderRegistryService` (Spring Bean) |
    And | `ProviderStreamFamily` + `composeProviderStreamWrappers()` (provider-stream.ts) | `StreamingResponseComposer` (infra service) |
    And | `AuthProfileStore` + `OAuthCredential` (provider-auth.ts) | `ProviderAuthProfileAdapter` (infra adapter → Key Vault) |
    And | `resolveApiKeyForProvider()` (provider-auth-runtime.ts) | `SecretResolutionService` (infra service) |
    And | `normalizeGeminiToolSchemas()` (provider-tools.ts) | `ToolSchemaNormalizerService` (infra service) |
    And | `ModelDefinitionConfig` (provider-model-shared.ts) | `LlmModelDefinition` (domain model) |
    And | `RetryConfig` + `createRateLimitRetryRunner()` (retry-runtime.ts) | `LlmRetryPolicy` (infra component) |
    And | `readConfiguredProviderCatalogEntries()` (provider-catalog-shared.ts) | `ModelCatalogRegistry` (Spring Bean) |
    And | `fetchWithTimeout()` + `postJsonRequest()` (provider-http.ts) | `RestClient` / `WebClient` nativo (Spring Boot 3) |
```

**Notas de Implementación (Non-Functional Requirements):**
- **Aislamiento Multi-Tenant:** Cada invocación LLM DEBE identificar el `tenant_id` del solicitante para resolver el perfil de autenticación y aplicar cuotas FinOps correctas.
- **FinOps:** Cada solicitud al proveedor generará un registro de consumo de tokens (input/output/cache_read/cache_write) vinculado al `billing_source` de US-053.
- **Resiliencia:** La cascada de Fallback aplicará backoff exponencial configurable (base 1s, max 30s, jitter random) antes de escalar al proveedor secundario.
- **Testing (Dry-Run):** Siguiendo la técnica descubierta en OpenClaw, los tests unitarios del LLM Plugin Engine usarán interceptores de respuesta HTTP mock para simular respuestas LLM sin llamar a APIs reales ($0 USD de costo). Los `ReplayPolicy` builders de OpenClaw (`provider-model-shared.ts`) proporcionan el patrón exacto de cómo construir respuestas de replay para testing.
- **Observabilidad:** Cada invocación LLM emitirá métricas Micrometer (latencia p50/p95/p99, tokens consumidos, cache hit ratio, fallback activations) para el dashboard de gobernanza IA (US-044).

**Trazabilidad UX:** Invisible (Operatividad Backend Server-Side) y ligada al ADR-012.

---

### US-056: Motor Central de Memoria Cognitiva y Fundaciones RAG (Memory Core Engine)
**Como** Arquitecto de IA / Motor de Procesamiento (Backend)
**Quiero** construir un motor de memoria cognitiva persistente que convierta automáticamente las transcripciones efímeras de las sesiones conversacionales con el LLM en *embeddings* vectoriales almacenados en una base de datos vectorial (`pgvector`)
**Para** permitir que los procesos BPMN de larga duración (semanas o meses) mantengan un contexto semántico acumulativo sin desbordar la ventana de tokens del LLM, habilitando la recuperación inteligente de información pasada (RAG — Retrieval-Augmented Generation) a costos operativos controlados.

> [!IMPORTANT]
> **Directiva de Implementación: Refactorización desde OpenClaw (Código Fuente de Referencia)**
> Esta historia de usuario DEBE implementarse tomando como base de referencia arquitectónica y funcional el código fuente original del proyecto OpenClaw, ubicado en el workspace local:
> `C:\Users\HaroltAndrésGómezAgu\.gemini\antigravity\scratch\openclaw_workspace`
>
> Los módulos de referencia principales son:
> - `src/memory-host-sdk/` — SDK del Host de Memoria (dreaming, embeddings, storage, events, runtime)
> - `src/context-engine/` — Motor de Contexto (types, registry, delegate)
> - `src/plugin-sdk/memory-core*.ts` — Fachada del Plugin de Memoria (MemoryIndexManager, MemorySearchManager)
> - `src/plugin-sdk/memory-lancedb.ts` — Integración con base vectorial LanceDB
> - `src/plugin-sdk/memory-host-search.ts` — Búsqueda semántica en memoria
>
> **Ejercicio de Refactorización Obligatorio:** El código de OpenClaw está escrito en TypeScript (Node.js). El equipo de desarrollo DEBE ejecutar un ejercicio formal de refactorización (Porting) para transcribir los contratos, interfaces y algoritmos centrales hacia Java 21 / Spring Boot 3, respetando la Arquitectura Hexagonal (ADR-001) y el patrón de Puertos y Adaptadores del iBPMS. Se prohíbe la copia literal sin adaptación; se exige la comprensión profunda de cada componente antes de su transposición.

**Dependencias Críticas:**
- **US-054 (LLM Plugin Engine):** 🔴 BLOQUEANTE. El `LlmChatPort` del CA-01 de US-054 es el canal por donde el Memory Core Engine invocará al LLM para generar embeddings y ejecutar las fases de consolidación cognitiva (Dreaming).
- **US-053 (Antigravity Command Center):** ⚠️ FUERTE. El consumo de tokens para generar embeddings y ejecutar las fases de Dreaming impactará las cuotas FinOps del Tenant (Model Quota / Model Credits).
- **ADR-012 (Integración Agnóstica LLM):** ⚠️ FUERTE. Los adaptadores de Embedding deben seguir el mismo patrón Zero-Dep de `RestClient` nativo.
- **ADR (pgvector):** 🔴 BLOQUEANTE. La extensión `pgvector` de PostgreSQL debe estar habilitada en la infraestructura antes de la implementación.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Cognitive Memory Core Engine, Dreaming Pipeline & RAG Foundations

  # ==============================================================================
  # A. ARQUITECTURA DEL MOTOR DE MEMORIA (CONTRATOS HEXAGONALES)
  # Ref. OpenClaw: src/context-engine/types.ts (ContextEngine interface)
  # Ref. OpenClaw: src/context-engine/registry.ts (ContextEngineFactory, resolveContextEngine)
  # ==============================================================================
  Scenario: Definición del Puerto Hexagonal de Memoria Cognitiva (MemoryPort) (CA-01)
    Given la necesidad de un motor de memoria desacoplado del proveedor de embeddings y del motor de almacenamiento vectorial
    Then el dominio del iBPMS definirá un Puerto (Interface Java) llamado `CognitiveMemoryPort` dentro de la capa `domain/port/outbound/`
    And este puerto expondrá las siguientes operaciones del ciclo de vida de memoria (inspiradas en el contrato `ContextEngine` de OpenClaw):
    And 1. **`ingest(MemoryIngestCommand)`** — Ingestar un mensaje conversacional nuevo (usuario o IA) en el almacén de memoria efímera de la sesión. Equivalente al método `ingest()` de OpenClaw.
    And 2. **`assemble(MemoryAssembleQuery)`** — Ensamblar el contexto relevante bajo un presupuesto de tokens para inyectarlo en el próximo prompt del LLM. Equivalente al método `assemble()` de OpenClaw.
    And 3. **`compact(MemoryCompactCommand)`** — Compactar el contexto cuando amenace con desbordar la ventana de tokens, generando resúmenes y podando turnos antiguos. Equivalente al método `compact()` de OpenClaw.
    And 4. **`recall(MemoryRecallQuery)`** — Buscar y recuperar chunks semánticamente relevantes desde la base vectorial persistente, dado un query textual del usuario o del proceso BPMN. (Operación RAG pura).
    And 5. **`promote(MemoryPromoteCommand)`** — Promover explícitamente transcripciones efímeras a embeddings persistentes en la base vectorial (triggerable manualmente o por el pipeline de Dreaming).
    And 6. **`dispose(String sessionId)`** — Liberar recursos de sesión al finalizar un proceso BPMN.
    And la implementación concreta (Adaptador) será inyectada por Spring IoC, permitiendo intercambiar el backend vectorial (pgvector, LanceDB, Pinecone) sin tocar el dominio.

  Scenario: Registro Dinámico de Motores de Contexto (Engine Registry) (CA-02)
    # Ref. OpenClaw: src/context-engine/registry.ts — registerContextEngine(), resolveContextEngine()
    Given que iBPMS podría soportar múltiples estrategias de gestión de contexto (legacy linear, RAG-first, hybrid)
    Then el sistema implementará un `MemoryEngineRegistry` (Singleton Spring Bean) que permita registrar implementaciones de `CognitiveMemoryPort` por identificador.
    And la resolución del motor activo se realizará mediante configuración (`application.yml`) con la propiedad `ibpms.memory.engine` (valores: `legacy`, `rag-core`, custom plugin IDs).
    And si el motor configurado no tiene una implementación registrada, el sistema fallará en el arranque (Fail-Fast) con un mensaje descriptivo listando los motores disponibles.
    And OpenClaw implementa este patrón exacto en su función `resolveContextEngine()` con un `ContextEngineRegistryState` global — el equipo debe estudiar este registry como referencia de diseño.

  # ==============================================================================
  # B. PIPELINE DE CONSOLIDACIÓN COGNITIVA ("DREAMING")
  # Ref. OpenClaw: src/memory-host-sdk/dreaming.ts (MemoryDreamingConfig, 3 fases)
  # ==============================================================================
  Scenario: Motor de Dreaming Trifásico (Light / Deep / REM) (CA-03)
    # Ref. OpenClaw: dreaming.ts líneas 73-132 (MemoryLightDreamingConfig, MemoryDeepDreamingConfig, MemoryRemDreamingConfig)
    Given la necesidad de consolidar la memoria efímera de las sesiones conversacionales en conocimiento persistente de largo plazo
    Then el iBPMS implementará un pipeline de consolidación cognitiva ("Dreaming") compuesto por 3 fases inspiradas en la neurociencia del sueño humano, replicando el modelo de OpenClaw:
    And 1. **Fase Light (Sueño Ligero):** Ejecutada cada 6 horas (Cron: `0 */6 * * *`). Lookback de 2 días. Procesa hasta 100 transcripciones recientes. Deduplica por similitud coseno (umbral 0.9). Velocidad: `fast`. Presupuesto: `cheap`. Fuentes: sesiones diarias, transcripciones activas, recall logs.
    And 2. **Fase Deep (Sueño Profundo):** Ejecutada diariamente (Cron: `0 3 * * *`). Procesa hasta 10 candidatos de alta relevancia (min score 0.8, mínimo 3 recalls, mínimo 3 queries únicos). Recency half-life: 14 días. Max age: 30 días. Incluye motor de Recovery automático que se activa cuando la salud de memoria cae por debajo del 35%. Velocidad: `balanced`. Presupuesto: `medium`.
    And 3. **Fase REM (Consolidación Profunda):** Ejecutada semanalmente (Cron: `0 5 * * 0`). Lookback de 7 días. Procesa hasta 10 patrones de alta fuerza (min pattern strength 0.75). Cruza memorias con resúmenes diarios y hallazgos deep para descubrir conexiones latentes. Velocidad: `slow`. Presupuesto: `expensive`.
    And cada fase es habitable/deshabitable de forma independiente mediante configuración YAML del administrador.
    And los valores por defecto DEBEN replicar exactamente los defaults de OpenClaw documentados en `dreaming.ts` (líneas 12-47) como punto de partida óptimo.

  Scenario: Parametrización Administrativa del Dreaming (CA-04)
    # Ref. OpenClaw: dreaming.ts líneas 58-66 (MemoryDreamingExecutionConfig) y 348-503 (resolveMemoryDreamingConfig)
    Given la necesidad del administrador de gobernar el costo y la agresividad de la consolidación cognitiva
    Then cada fase de Dreaming expondrá un `ExecutionConfig` con los siguientes controles:
    And 1. **`speed`**: `fast` | `balanced` | `slow` — Controla la profundidad del análisis LLM.
    And 2. **`thinking`**: `low` | `medium` | `high` — Controla el nivel de razonamiento del LLM.
    And 3. **`budget`**: `cheap` | `medium` | `expensive` — Controla el gasto de tokens por fase. Mapea internamente a model tiers (Ej: `cheap` → Gemini Flash, `expensive` → Gemini Pro/Ultra).
    And 4. **`model`** (opcional): Override explícito del modelo LLM a usar en la fase.
    And 5. **`maxOutputTokens`** (opcional): Límite de tokens de salida para el resumen generado.
    And 6. **`temperature`** (opcional, 0-2): Creatividad del modelo durante la consolidación.
    And 7. **`timeoutMs`** (opcional): Timeout máximo por invocación de fase.
    And la configuración seguirá el patrón de herencia de OpenClaw: valores defaults globales → overrides por fase → overrides por tenant.

  # ==============================================================================
  # C. CAPA DE EMBEDDINGS MULTI-PROVEEDOR
  # Ref. OpenClaw: src/memory-host-sdk/engine-embeddings.ts (proveedores)
  # Ref. OpenClaw: src/memory-host-sdk/host/embeddings*.ts (implementaciones)
  # ==============================================================================
  Scenario: Abstracción Agnóstica de Proveedores de Embedding (CA-05)
    # Ref. OpenClaw: engine-embeddings.ts — exports de Gemini, OpenAI, Voyage, Ollama, Mistral, Local
    Given que la generación de embeddings vectoriales es el corazón del RAG
    Then el iBPMS definirá una interfaz `EmbeddingProviderPort` (Puerto Hexagonal) con el contrato:
    And 1. **`generateEmbedding(String text): float[]`** — Genera un vector de embedding para un texto individual.
    And 2. **`generateBatchEmbeddings(List<String> texts): List<float[]>`** — Genera embeddings en lote optimizado.
    And 3. **`getModelId(): String`** — Retorna el identificador del modelo de embedding activo.
    And 4. **`getDimensions(): int`** — Retorna la dimensionalidad del vector (Ej: 768, 1536, 3072).
    And el sistema implementará adaptadores para al menos 2 proveedores en V1:
    And a) `GeminiEmbeddingAdapter` — Usando `text-embedding-004` (DEFAULT_GEMINI_EMBEDDING_MODEL de OpenClaw).
    And b) `OpenAiEmbeddingAdapter` — Usando `text-embedding-3-small` (DEFAULT_OPENAI_EMBEDDING_MODEL de OpenClaw).
    And la selección del proveedor activo la gobernará `ibpms.memory.embedding.provider` en `application.yml`.
    And PROHIBIDO importar SDKs pesados de estos proveedores. Se usará `RestClient` nativo de Spring Boot (consistente con US-054 CA-02).

  Scenario: Chunking Inteligente y Límites de Input (CA-06)
    # Ref. OpenClaw: src/memory-host-sdk/host/embedding-chunk-limits.ts (enforceEmbeddingMaxInputTokens)
    # Ref. OpenClaw: src/memory-host-sdk/host/internal.ts (chunkMarkdown, MemoryChunk)
    Given que los textos conversacionales pueden exceder los límites de input de los modelos de embedding
    Then el sistema implementará un `TextChunkingService` responsable de:
    And 1. **Segmentación Markdown-Aware:** Dividir textos largos en chunks respetando fronteras de párrafos, encabezados y bloques de código (replicando `chunkMarkdown` de OpenClaw).
    And 2. **Estimación de Bytes/Tokens:** Calcular el peso del chunk antes de enviarlo al proveedor, truncando preventivamente si excede el límite del modelo.
    And 3. **Metadata por Chunk:** Cada `MemoryChunk` resultante portará: `chunkId` (hash del contenido), `sourcePath` (origen), `startLine`, `endLine`, `score` (relevancia), `embedding` (vector).
    And 4. **Deduplicación por Similitud Coseno:** Antes de indexar, el sistema comparará el nuevo chunk contra los N más recientes usando `cosineSimilarity()` (exportado por OpenClaw en `engine-storage.ts`). Si la similitud supera el umbral configurable (default: 0.9 — `DEFAULT_MEMORY_LIGHT_DREAMING_DEDUPE_SIMILARITY`), el chunk se descarta como duplicado.

  # ==============================================================================
  # D. ALMACENAMIENTO VECTORIAL Y BÚSQUEDA SEMÁNTICA (RAG RETRIEVAL)
  # Ref. OpenClaw: src/memory-host-sdk/engine-storage.ts (MemorySearchManager, MemorySearchResult)
  # Ref. OpenClaw: src/memory-host-sdk/host/memory-schema.ts (ensureMemoryIndexSchema)
  # ==============================================================================
  Scenario: Esquema de Índice Vectorial en pgvector (CA-07)
    # Ref. OpenClaw: host/memory-schema.ts — ensureMemoryIndexSchema()
    Given la extensión `pgvector` habilitada en la instancia PostgreSQL del iBPMS
    Then el sistema creará la tabla `ibpms_memory_vectors` con el siguiente esquema:
    And 1. **`chunk_id`** (UUID, PK): Identificador único del chunk vectorizado.
    And 2. **`tenant_id`** (VARCHAR, NOT NULL, INDEX): Aislamiento multi-tenant estricto.
    And 3. **`session_id`** (VARCHAR, NOT NULL, INDEX): Sesión conversacional de origen.
    And 4. **`process_instance_id`** (VARCHAR, INDEX): Instancia BPMN asociada (nullable para contextos no-BPMN).
    And 5. **`agent_id`** (VARCHAR, INDEX): Identificador del agente IA que generó la interacción.
    And 6. **`source_type`** (VARCHAR, NOT NULL): Tipo de fuente (`CONVERSATION`, `DOCUMENT`, `FORM_EVENT`, `BPMN_VARIABLE`).
    And 7. **`content_text`** (TEXT, NOT NULL): Texto original del chunk (para display y auditoría).
    And 8. **`embedding`** (VECTOR(1536), NOT NULL): Vector de embedding generado por el proveedor. La dimensión será configurable según el modelo.
    And 9. **`metadata_json`** (JSONB): Metadatos adicionales (startLine, endLine, sourcePath, dreaming phase, etc.).
    And 10. **`dream_phase`** (VARCHAR): Fase de Dreaming que promovió este chunk (`LIGHT`, `DEEP`, `REM`, `MANUAL`).
    And 11. **`recall_count`** (INT, DEFAULT 0): Contador de veces que este chunk ha sido recuperado por queries RAG (usado por Deep Dreaming para scoring de relevancia).
    And 12. **`created_at`** (TIMESTAMP WITH TIME ZONE, NOT NULL, DEFAULT NOW()).
    And 13. **`expires_at`** (TIMESTAMP WITH TIME ZONE): Fecha de expiración configurable para garbage collection.
    And se creará un índice IVFFlat o HNSW sobre la columna `embedding` para búsquedas ANN (Approximate Nearest Neighbor) eficientes.
    And la tabla aplicará Row-Level Security (RLS) por `tenant_id` para garantizar aislamiento absoluto en entornos multi-tenant.

  Scenario: Búsqueda Semántica con Presupuesto de Tokens (Memory Recall) (CA-08)
    # Ref. OpenClaw: src/plugin-sdk/memory-host-search.ts — getActiveMemorySearchManager()
    # Ref. OpenClaw: src/memory-host-sdk/host/query-expansion.ts — expansión de queries
    Given una consulta del LLM o del proceso BPMN que necesite contexto histórico
    When el sistema invoque `recall(MemoryRecallQuery)` del `CognitiveMemoryPort`
    Then el adaptador ejecutará la siguiente secuencia:
    And 1. **Expansión de Query:** El texto de búsqueda será expandido semánticamente (replicando `query-expansion.ts` de OpenClaw) para maximizar la cobertura de resultados relevantes.
    And 2. **Vectorización del Query:** Se generará el embedding del query expandido usando el `EmbeddingProviderPort` activo.
    And 3. **Búsqueda ANN:** Se ejecutará una consulta `pgvector` con operador `<=>` (distancia coseno) contra `ibpms_memory_vectors`, filtrada por `tenant_id`, `process_instance_id` (si aplica) y `agent_id` (si aplica).
    And 4. **Scoring y Ranking:** Los resultados se ordenarán por proximidad coseno descendente. Se aplicará un umbral mínimo de score configurable (default: 0.7).
    And 5. **Presupuesto de Tokens:** El ensamblador cortará la lista de resultados cuando la suma acumulada de tokens de los chunks exceda el `tokenBudget` configurado, garantizando que la inyección al prompt nunca desborde la ventana del LLM.
    And 6. **Actualización de recall_count:** Los chunks devueltos incrementarán su `recall_count` en +1 (para alimentar el scoring de la fase Deep Dreaming).
    And la respuesta será un `MemoryRecallResult` con la lista de `MemoryChunk` ordenados, el total de tokens estimados y el score promedio.

  # ==============================================================================
  # E. SISTEMA DE EVENTOS DE MEMORIA Y OBSERVABILIDAD
  # Ref. OpenClaw: src/memory-host-sdk/events.ts (MemoryHostEvent, appendMemoryHostEvent)
  # ==============================================================================
  Scenario: Bitácora Inmutable de Eventos de Memoria (Memory Event Log) (CA-09)
    # Ref. OpenClaw: events.ts — MemoryHostRecallRecordedEvent, MemoryHostPromotionAppliedEvent, MemoryHostDreamCompletedEvent
    Given la necesidad de auditar todas las operaciones del motor de memoria
    Then el sistema registrará eventos tipados en la tabla `ibpms_memory_event_log` (append-only):
    And 1. **`memory.recall.recorded`**: Cada vez que un RAG recall se ejecuta. Registra: query, resultCount, lista de resultados (path, startLine, endLine, score).
    And 2. **`memory.promotion.applied`**: Cada vez que chunks efímeros son promovidos a embeddings persistentes. Registra: memoryPath, applied count, lista de candidatos (key, path, score, recallCount).
    And 3. **`memory.dream.completed`**: Cada vez que una fase de Dreaming finaliza. Registra: phase (light/deep/rem), lineCount generado, storageMode.
    And los eventos se persistirán como JSONL (JSON Lines) para compatibilidad con el formato de OpenClaw.
    And esta bitácora alimentará el dashboard de observabilidad IA (consumido por US-044) para que el Súper Administrador monitorice la salud cognitiva del sistema.

  Scenario: Métricas de Salud de Memoria y Auto-Recuperación (CA-10)
    # Ref. OpenClaw: dreaming.ts líneas 83-90 (MemoryDeepDreamingRecoveryConfig)
    Given que la calidad de la memoria vectorial puede degradarse por chunks obsoletos, duplicados o irrelevantes
    Then el sistema calculará un **Health Score** de memoria (0.0 a 1.0) basado en:
    And 1. Ratio de chunks con `recall_count > 0` vs total de chunks (Utilización).
    And 2. Edad promedio de los chunks activos vs `recencyHalfLifeDays` (Frescura).
    And 3. Distribución de similaridad inter-chunk (Diversidad — un valor demasiado alto indica saturación de duplicados).
    And si el Health Score cae por debajo de `0.35` (umbral configurable, replicando `DEFAULT_MEMORY_DEEP_DREAMING_RECOVERY_TRIGGER_BELOW_HEALTH` de OpenClaw), el sistema activará un ciclo de **Auto-Recovery** que:
    And a) Retroalimentará las últimas 30 días de transcripciones (lookback recovery).
    And b) Re-generará embeddings para hasta 20 candidatos de alta confianza.
    And c) Solo auto-escribirá chunks con confianza >= 0.97 (`autoWriteMinConfidence`).
    And d) Emitirá una alerta al Administrador si la recuperación no logra subir el health score por encima de 0.5.

  # ==============================================================================
  # F. ENSAMBLAJE DE CONTEXTO PARA EL PROMPT (ASSEMBLY)
  # Ref. OpenClaw: src/context-engine/types.ts — assemble() method, AssembleResult
  # Ref. OpenClaw: src/context-engine/delegate.ts — buildMemorySystemPromptAddition()
  # ==============================================================================
  Scenario: Ensamblaje Presupuestario de Contexto para el LLM (CA-11)
    # Ref. OpenClaw: types.ts líneas 6-13 (AssembleResult), líneas 224-238 (assemble params)
    Given que cada llamada al LLM tiene un presupuesto finito de tokens (ventana de contexto)
    When el `CognitiveMemoryPort.assemble()` sea invocado antes de una inferencia LLM
    Then el ensamblador construirá el contexto siguiendo esta jerarquía de prioridad:
    And 1. **System Prompt** (inmutable, head position — consistente con US-054 CA-04 Prompt Cache Stability).
    And 2. **Memoria Activa Reciente** (últimos N turnos de la sesión actual, sin vectorizar).
    And 3. **Chunks RAG Recuperados** (resultados del `recall()` ordenados por score, tail position).
    And 4. **System Prompt Addition** (inyección opcional del context engine, replicando `systemPromptAddition` de OpenClaw).
    And el resultado `MemoryAssembleResult` contendrá: `assembledMessages` (lista ordenada), `estimatedTokens` (total estimado), y `systemPromptAddition` (instrucciones complementarias).
    And si `estimatedTokens` excede el 80% del `tokenBudget`, el ensamblador ejecutará auto-compactación sobre los turnos más antiguos ANTES de inyectar los chunks RAG.
    And PROHIBIDO alterar la cabecera inmutable (System Prompt) — el tail-appending se aplica exclusivamente al historial y los chunks RAG.

  Scenario: Compactación Automática de Contexto (CA-12)
    # Ref. OpenClaw: src/context-engine/types.ts — compact() method, CompactResult
    # Ref. OpenClaw: src/context-engine/delegate.ts — delegateCompactionToRuntime()
    Given que una sesión conversacional acumula más turnos de los que caben en la ventana del LLM
    When el sistema detecte que el contexto supera el 85% del presupuesto de tokens
    Then ejecutará una compactación automática que:
    And 1. Invocará al LLM con un prompt de resumen para condensar los turnos más antiguos en un párrafo compacto.
    And 2. Reemplazará los turnos originales por el resumen generado en la memoria efímera de sesión.
    And 3. Registrará el resultado de la compactación: `tokensBefore`, `tokensAfter`, `summary` generado, `firstKeptEntryId`.
    And 4. Promoverá automáticamente los turnos eliminados al pipeline de Dreaming (fase Light) para su vectorización futura.
    And la compactación NUNCA eliminará turnos sin antes haberlos respaldado en el pipeline de promoción a embeddings.

  # ==============================================================================
  # G. GARBAGE COLLECTION Y CICLO DE VIDA
  # ==============================================================================
  Scenario: Expiración y Limpieza de Embeddings Obsoletos (CA-13)
    Given el crecimiento continuo de la tabla `ibpms_memory_vectors` a lo largo de meses de operación
    Then un Scheduled Job (Spring `@Scheduled`, protegido por ShedLock contra solapamiento) ejecutará semanalmente:
    And 1. **Purga por Expiración:** Eliminar chunks con `expires_at < NOW()`.
    And 2. **Purga por Irrelevancia:** Eliminar chunks con `recall_count = 0` y `created_at` mayor a 90 días (nunca fueron útiles).
    And 3. **Consolidación de Duplicados:** Fusionar chunks con similitud coseno > 0.95 conservando el de mayor `recall_count`.
    And 4. **Reindexación IVFFlat:** Tras la purga, ejecutar `REINDEX` sobre el índice vectorial para optimizar las búsquedas ANN.
    And PROHIBIDO ejecutar estas operaciones durante horas pico operativas. El Cron default será `0 4 * * 0` (Domingos 4:00 AM).

  # ==============================================================================
  # H. DIRECTIVAS DE REFACTORIZACIÓN Y TRAZABILIDAD AL CÓDIGO FUENTE
  # ==============================================================================
  Scenario: Mapa de Transposición OpenClaw → iBPMS (Refactoring Ledger) (CA-14)
    Given la directiva de implementación basada en el código fuente de OpenClaw
    Then el equipo de desarrollo DEBE mantener un documento de trazabilidad (`docs/architecture/memory-core-refactoring-ledger.md`) que mapee:
    And 1. Cada archivo TypeScript de OpenClaw → su clase/interfaz Java equivalente en iBPMS.
    And 2. Decisiones de diseño donde se divergió del patrón original y la justificación técnica.
    And 3. Funciones de OpenClaw descartadas y la razón (Ej: `loadSqliteVecExtension` → N/A, usamos pgvector nativo).
    And el mapa mínimo obligatorio de transposición es:
    And | OpenClaw (TypeScript) | iBPMS (Java) |
    And | `ContextEngine` interface (types.ts) | `CognitiveMemoryPort` (domain port) |
    And | `ContextEngineFactory` (registry.ts) | `MemoryEngineRegistry` (Spring Bean) |
    And | `MemoryDreamingConfig` (dreaming.ts) | `DreamingConfigProperties` (@ConfigurationProperties) |
    And | `MemoryEmbeddingProvider` (engine-embeddings.ts) | `EmbeddingProviderPort` (domain port) |
    And | `MemoryChunk` / `MemoryFileEntry` (internal.ts) | `MemoryChunk` / `MemoryFileEntry` (domain model) |
    And | `MemorySearchManager` (engine-storage.ts) | `VectorSearchAdapter` (infra adapter) |
    And | `MemoryHostEvent` (events.ts) | `MemoryAuditEvent` (domain event) |
    And | `ensureMemoryIndexSchema` (memory-schema.ts) | Flyway migration `V{N}__create_memory_vectors.sql` |
    And | `cosineSimilarity` (internal.ts) | `CosineSimilarityUtil` (shared utility) |
    And | LanceDB adapter (memory-lancedb.ts) | `PgVectorAdapter` (infra adapter) |
```

**Notas de Implementación (Non-Functional Requirements):**
- **Aislamiento Multi-Tenant:** Todas las operaciones de memoria DEBEN filtrar por `tenant_id`. Un Tenant NUNCA puede acceder a embeddings de otro Tenant.
- **FinOps:** Cada operación de embedding generará un registro de consumo de tokens vinculado al `billing_source` de US-053 para auditoría de costos.
- **Resiliencia:** Si el proveedor de embeddings falla (HTTP 429/500), se aplicará el mismo Fallback Cognitivo de US-054 CA-05, intentando con el proveedor secundario antes de abortar.
- **Testing (Dry-Run):** Siguiendo la técnica descubierta en OpenClaw (Roadmap Quick Win #1), los tests unitarios del Memory Core Engine usarán interceptores de respuesta HTTP mock para simular embeddings sin llamar a APIs reales ($0 USD de costo).

**Trazabilidad UX:** Invisible (Operatividad Backend Server-Side). Dashboard de observabilidad en Pantalla 15.A (Gobernanza IA — US-044).

---

### US-057: Base de Conocimiento Inteligente y RAG Documental/Multimodal (Knowledge Base Engine)
**Como** Administrador de la Plataforma / Arquitecto de IA (Backend)
**Quiero** construir un motor de base de conocimiento que permita cargar, indexar y recuperar documentos empresariales (PDF, DOCX, Markdown, imágenes, hojas de cálculo) como *embeddings* vectoriales organizados en **Espacios de Conocimiento** (`KnowledgeSpaces`)
**Para** que cada agente de IA especializado del iBPMS (US-052) pueda acceder a contexto documental relevante a su rol o especialidad — normativa legal, manuales técnicos, políticas institucionales, artefactos de proceso — habilitando decisiones fundamentadas en conocimiento propio del dominio, no solo en el historial conversacional.

> [!IMPORTANT]
> **Directiva Arquitectónica: Relación con US-056 y ADR-013 (Estrategia RAG Dual)**
> Esta historia de usuario implementa el **segundo pilar** de la Estrategia RAG Dual definida en el ADR-013 (`docs/architecture/adr_013_dual_rag_strategy.md`):
>
> | Pilar | US | Puerto Hexagonal | Scope | Ciclo de Vida |
> |-------|-----|------------------|-------|---------------|
> | **RAG Conversacional** | US-056 | `CognitiveMemoryPort` | `session_id` / `process_instance_id` | Efímero → Dreaming → Expiración |
> | **RAG Documental** | **US-057** | `KnowledgeBasePort` | `knowledge_space_id` / `agent_id` / `role_id` | Persistente → Sincronización incremental |
>
> Ambos pilares comparten la **infraestructura de embeddings** (`EmbeddingProviderPort` de US-054) y la **base vectorial** (`pgvector` de ADR-009), pero operan sobre tablas, puertos y políticas de gobernanza **independientes**.
>
> **Diferencia esencial:**
> - US-056 responde: *"¿Qué se dijo en conversaciones anteriores?"* (memoria del proceso)
> - US-057 responde: *"¿Qué dice la normativa/manual/política relevante?"* (conocimiento del dominio)

> [!IMPORTANT]
> **Directiva de Implementación: Refactorización desde OpenClaw (Código Fuente de Referencia)**
> Esta historia de usuario DEBE implementarse extendiendo los patrones arquitectónicos del proyecto OpenClaw, ubicado en el workspace local:
> `C:\Users\HaroltAndrésGómezAgu\.gemini\antigravity\scratch\openclaw_workspace`
>
> Los módulos de referencia principales son:
> - `src/memory-host-sdk/host/internal.ts` — `listMemoryFiles()`, `buildFileEntry()`, `chunkMarkdown()` (indexación de archivos Markdown y multimodal)
> - `src/memory-host-sdk/host/multimodal.ts` — `classifyMemoryMultimodalPath()`, `MemoryMultimodalSettings` (soporte de imágenes y audio)
> - `src/memory-host-sdk/engine-embeddings.ts` — Proveedores de embedding multi-vendor (Gemini, OpenAI, Voyage, Ollama)
> - `src/memory-host-sdk/host/memory-schema.ts` — `ensureMemoryIndexSchema()` (esquema de índice con FTS5 + vectores)
> - `src/memory-host-sdk/host/query-expansion.ts` — `extractKeywords()` (expansión de queries para búsqueda semántica)
> - `src/memory-host-sdk/events.ts` — `MemoryHostEvent`, `appendMemoryHostEvent()` (bitácora de eventos)
>
> **Ejercicio de Refactorización Obligatorio:** El código de OpenClaw está escrito en TypeScript (Node.js). El equipo de desarrollo DEBE transponer los contratos y algoritmos hacia Java 21 / Spring Boot 3, respetando la Arquitectura Hexagonal (ADR-001). Se prohíbe la copia literal sin adaptación.

**Dependencias Críticas:**
- **US-056 (Memory Core Engine):** ⚠️ FUERTE. Ambas historias comparten `EmbeddingProviderPort`, `TextChunkingService` y la instancia `pgvector`. US-057 NO modifica la tabla `ibpms_memory_vectors` de US-056; opera sobre su propia tabla `ibpms_knowledge_vectors`.
- **US-054 (LLM Plugin Engine):** 🔴 BLOQUEANTE. El `EmbeddingProviderPort` del CA-05 de US-054 es el canal para generar embeddings de documentos.
- **US-053 (Antigravity Command Center):** ⚠️ FUERTE. La generación de embeddings para documentos cargados impactará las cuotas FinOps del Tenant.
- **US-052 (Multi-Agent Engine):** ⚠️ FUERTE. Los Knowledge Spaces se asignan a los Agentes Especializados definidos en US-052.
- **US-036 (RBAC):** ⚠️ FUERTE. El acceso a los Knowledge Spaces está gobernado por la matriz de roles y permisos de la taxonomía RBAC.
- **US-035 (SharePoint/SGDEA):** 🟡 DESEABLE (V2). Sincronización automática de documentos desde la bóveda documental externa.
- **ADR-013 (Dual RAG Strategy):** 🔴 BLOQUEANTE. Define la separación arquitectónica entre RAG conversacional y RAG documental.
- **ADR-009 (pgvector):** 🔴 BLOQUEANTE. La extensión `pgvector` debe estar habilitada en PostgreSQL.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Knowledge Base Engine, Document RAG & Multimodal Indexing

  # ==============================================================================
  # A. CONTRATOS HEXAGONALES DE LA BASE DE CONOCIMIENTO
  # Ref. OpenClaw: src/memory-host-sdk/host/internal.ts (listMemoryFiles, buildFileEntry)
  # Ref. ADR-013: Sección 3.1 — KnowledgeBasePort
  # ==============================================================================
  Scenario: Definición del Puerto Hexagonal de Base de Conocimiento (KnowledgeBasePort) (CA-01)
    Given la necesidad de un motor de conocimiento documental desacoplado del proveedor de embeddings y del formato de documento
    Then el dominio del iBPMS definirá un Puerto (Interface Java) llamado `KnowledgeBasePort` dentro de la capa `domain/port/outbound/`
    And este puerto expondrá las siguientes operaciones:
    And 1. **`indexDocument(DocumentIndexCommand)`** — Procesa un documento individual: parsea → chunkea → genera embeddings → almacena en pgvector. Retorna `DocumentIndexResult` con conteo de chunks generados.
    And 2. **`indexBatch(BatchDocumentIndexCommand)`** — Procesa múltiples documentos en lote con concurrencia controlada.
    And 3. **`recall(KnowledgeRecallQuery)`** — Búsqueda RAG: dado un query textual, recupera los chunks más relevantes del Knowledge Space indicado, filtrado por `agent_id` o `role_id`.
    And 4. **`syncKnowledgeSpace(KnowledgeSyncCommand)`** — Sincronización incremental: detecta documentos nuevos/modificados/eliminados y actualiza el índice vectorial.
    And 5. **`removeDocument(String knowledgeSpaceId, String documentId)`** — Elimina un documento y todos sus chunks del índice.
    And 6. **`status(String knowledgeSpaceId)`** — Retorna métricas del Knowledge Space: documentos indexados, chunks totales, proveedor de embedding activo, última sincronización.
    And la implementación concreta (Adaptador) será inyectada por Spring IoC, usando la misma instancia `pgvector` que US-056 pero operando sobre la tabla `ibpms_knowledge_vectors`.

  # ==============================================================================
  # B. CONCEPTO DE ESPACIOS DE CONOCIMIENTO (KNOWLEDGE SPACES)
  # ==============================================================================
  Scenario: Definición y Gobierno de Knowledge Spaces (CA-02)
    Given la necesidad de organizar documentos por dominio funcional y asignarlos a agentes o roles específicos
    Then el sistema implementará una entidad `KnowledgeSpace` persistida en PostgreSQL con el siguiente esquema:
    And 1. **`knowledge_space_id`** (UUID, PK): Identificador único del espacio.
    And 2. **`tenant_id`** (VARCHAR, NOT NULL): Aislamiento multi-tenant estricto.
    And 3. **`name`** (VARCHAR, NOT NULL): Nombre legible (Ej: "Normativa Legal", "Manuales Técnicos", "Políticas RRHH").
    And 4. **`description`** (TEXT): Descripción del alcance del espacio.
    And 5. **`assigned_agent_ids`** (JSONB): Lista de identificadores de agentes IA autorizados a consultar este espacio.
    And 6. **`assigned_role_ids`** (JSONB): Lista de roles funcionales (RBAC) cuyos agentes heredan acceso al espacio.
    And 7. **`embedding_model`** (VARCHAR): Modelo de embedding usado para este espacio (debe ser consistente para todos los chunks).
    And 8. **`created_at`** / **`updated_at`** (TIMESTAMPTZ).
    And un agente IA SOLO podrá consultar Knowledge Spaces asignados explícitamente (por `agent_id`) o heredados (por `role_id`). Consultas a espacios no autorizados retornarán resultado vacío (fail-silent, no error).
    And un Knowledge Space puede ser consultado por múltiples agentes simultáneamente (lectura compartida, escritura exclusiva del Administrador).

  # ==============================================================================
  # C. INGESTA Y PARSING DOCUMENTAL
  # Ref. OpenClaw: src/memory-host-sdk/host/internal.ts (chunkMarkdown, buildFileEntry)
  # Ref. OpenClaw: src/memory-host-sdk/host/multimodal.ts (classifyMemoryMultimodalPath)
  # ==============================================================================
  Scenario: Pipeline de Ingesta Documental Multi-Formato (CA-03)
    Given que los documentos empresariales se presentan en formatos heterogéneos
    Then el sistema implementará un `DocumentParserService` con parsers registrables por MIME type:
    And 1. **Markdown (.md):** Parser nativo reutilizando `chunkMarkdown()` de OpenClaw (transpuesto a Java). Zero dependencias externas.
    And 2. **Texto plano (.txt):** Parser trivial con chunking por tamaño de tokens.
    And 3. **PDF (.pdf):** Parser usando Apache PDFBox (licencia Apache 2.0, zero-dep compatible). Extracción de texto por página con metadata de número de página.
    And 4. **DOCX (.docx):** Parser usando Apache POI (licencia Apache 2.0). Extracción de texto con preservación de estructura de encabezados.
    And 5. **Imágenes (.png, .jpg, .webp):** Indexación multimodal siguiendo el patrón `buildMultimodalChunkForIndexing()` de OpenClaw. Genera un label descriptivo y un embedding multimodal si el proveedor lo soporta. Si no, usa OCR (Tesseract) como fallback para extraer texto.
    And 6. **Hojas de cálculo (.xlsx, .csv):** Parser usando Apache POI (XLSX) o parser nativo (CSV). Cada hoja/tabla se convierte en chunks tabulares con metadata de fila/columna.
    And cada parser deberá:
    And a) Extraer texto puro preservando estructura semántica (encabezados, párrafos, listas).
    And b) Generar metadata de localización (página, sección, fila) para citaciones.
    And c) Respetar un límite máximo de tamaño por documento configurable (default: 50 MB).
    And d) Emitir un evento `knowledge.document.parsed` con estadísticas (páginas, chars, chunks estimados).
    And el `DocumentParserService` será extensible mediante un patrón Strategy: nuevos parsers pueden registrarse sin modificar código existente.

  Scenario: Chunking Especializado para Documentos (CA-04)
    # Ref. OpenClaw: internal.ts — chunkMarkdown() con estimación CJK y soporte UTF-16
    Given que los documentos tienen estructuras más ricas que las transcripciones conversacionales
    Then el `TextChunkingService` (compartido con US-056) se extenderá con las siguientes capacidades:
    And 1. **Header-Aware Chunking:** Los encabezados Markdown (`# ## ###`) se usan como fronteras de chunk preferentes. Un chunk nunca corta un encabezado a la mitad.
    And 2. **Page-Aware Chunking (PDF):** Los saltos de página actúan como fronteras naturales de chunk. Metadata `page_number` se preserva en cada chunk.
    And 3. **Table-Aware Chunking:** Las tablas Markdown o tabulares (CSV/XLSX) se indexan como unidades completas si caben en el presupuesto de tokens del chunk, o se segmentan por bloques de filas preservando el header de columnas en cada chunk.
    And 4. **Overlap Configurable:** Cada chunk incluirá un overlap configurable (default: 50 tokens de OpenClaw) con el chunk anterior para preservar continuidad semántica.
    And 5. **Deduplicación por Hash:** Chunks con hash SHA-256 idéntico a uno existente en el índice se descartan (replicando `hashText()` de OpenClaw).
    And los parámetros de chunking (`maxTokensPerChunk`, `overlapTokens`, `minChunkTokens`) serán configurables por Knowledge Space en `application.yml`.

  # ==============================================================================
  # D. ALMACENAMIENTO VECTORIAL DOCUMENTAL
  # Ref. OpenClaw: src/memory-host-sdk/host/memory-schema.ts (ensureMemoryIndexSchema)
  # Ref. ADR-013: Sección 3.2 — Modelo de Datos Segregado
  # ==============================================================================
  Scenario: Esquema de Índice Vectorial para Base de Conocimiento (CA-05)
    Given la extensión `pgvector` habilitada en la instancia PostgreSQL del iBPMS (ADR-009)
    Then el sistema creará la tabla `ibpms_knowledge_vectors` (SEGREGADA de `ibpms_memory_vectors` de US-056) con el siguiente esquema:
    And 1. **`chunk_id`** (UUID, PK): Identificador único del chunk documental.
    And 2. **`tenant_id`** (VARCHAR, NOT NULL, INDEX): Aislamiento multi-tenant estricto.
    And 3. **`knowledge_space_id`** (VARCHAR, NOT NULL, INDEX): Espacio de conocimiento al que pertenece el chunk.
    And 4. **`document_id`** (UUID, NOT NULL, INDEX): Documento fuente del chunk.
    And 5. **`agent_id`** (VARCHAR, INDEX): Agente IA al que se asigna (nullable si la asignación es por Knowledge Space).
    And 6. **`role_id`** (VARCHAR, INDEX): Rol funcional al que se asigna (alternativa a agente).
    And 7. **`source_type`** (VARCHAR, NOT NULL): Tipo de fuente: `DOCUMENT`, `IMAGE`, `SPREADSHEET`, `POLICY`, `BPMN_ARTIFACT`.
    And 8. **`content_text`** (TEXT, NOT NULL): Texto extraído del chunk (para display, citaciones y auditoría).
    And 9. **`embedding`** (VECTOR, NOT NULL): Vector de embedding. La dimensionalidad se hereda del `EmbeddingProviderPort` activo de US-054.
    And 10. **`metadata_json`** (JSONB, NOT NULL): Metadatos del chunk: `{filename, page, section, mime_type, version, parser_used, start_line, end_line}`.
    And 11. **`document_version`** (INT, DEFAULT 1): Versión del documento que generó este chunk. Permite re-indexación incremental.
    And 12. **`recall_count`** (INT, DEFAULT 0): Veces que este chunk fue recuperado por queries RAG (para métricas de utilización).
    And 13. **`created_at`** / **`updated_at`** (TIMESTAMPTZ).
    And 14. **`expires_at`** (TIMESTAMPTZ, NULLABLE): `NULL` para documentos permanentes. Solo se usa si el Administrador configura TTL explícito.
    And se creará un índice HNSW sobre la columna `embedding` para búsquedas ANN eficientes.
    And la tabla aplicará Row-Level Security (RLS) por `tenant_id`.
    And PROHIBIDO almacenar chunks documentales en `ibpms_memory_vectors` (tabla de US-056). La segregación es arquitectónica (ADR-013).

  Scenario: Tabla de Registro de Documentos Indexados (CA-06)
    Given la necesidad de trackear qué documentos están indexados en cada Knowledge Space
    Then el sistema mantendrá una tabla de registro `ibpms_knowledge_documents`:
    And 1. **`document_id`** (UUID, PK).
    And 2. **`tenant_id`** (VARCHAR, NOT NULL).
    And 3. **`knowledge_space_id`** (VARCHAR, NOT NULL).
    And 4. **`filename`** (VARCHAR, NOT NULL): Nombre original del archivo.
    And 5. **`mime_type`** (VARCHAR, NOT NULL).
    And 6. **`size_bytes`** (BIGINT, NOT NULL).
    And 7. **`content_hash`** (VARCHAR, NOT NULL): SHA-256 del contenido original para detección de cambios.
    And 8. **`chunk_count`** (INT, NOT NULL): Cantidad de chunks generados.
    And 9. **`embedding_model`** (VARCHAR, NOT NULL): Modelo usado para generar embeddings.
    And 10. **`status`** (VARCHAR, NOT NULL): `PENDING`, `INDEXING`, `INDEXED`, `FAILED`, `STALE`.
    And 11. **`version`** (INT, DEFAULT 1): Versión del documento (incrementa con cada re-indexación).
    And 12. **`indexed_at`** / **`created_at`** / **`updated_at`** (TIMESTAMPTZ).
    And 13. **`error_message`** (TEXT, NULLABLE): Detalles del error si `status = FAILED`.

  # ==============================================================================
  # E. BÚSQUEDA RAG DOCUMENTAL
  # Ref. OpenClaw: src/memory-host-sdk/host/query-expansion.ts (extractKeywords)
  # Ref. OpenClaw: src/memory-host-sdk/host/types.ts (MemorySearchManager.search)
  # ==============================================================================
  Scenario: Búsqueda Semántica con Filtrado por Knowledge Space y Rol (CA-07)
    Given una consulta de un agente IA o un Service Task BPMN que necesite conocimiento especializado
    When el sistema invoque `recall(KnowledgeRecallQuery)` del `KnowledgeBasePort`
    Then el adaptador ejecutará la siguiente secuencia:
    And 1. **Validación de Acceso:** Verificar que el `agent_id` o `role_id` del solicitante tiene acceso al `knowledge_space_id` indicado. Si no, retornar resultado vacío.
    And 2. **Expansión de Query:** El texto de búsqueda será expandido semánticamente (replicando `extractKeywords()` de OpenClaw) para maximizar cobertura.
    And 3. **Vectorización del Query:** Generar embedding del query usando el `EmbeddingProviderPort` activo de US-054.
    And 4. **Búsqueda ANN:** Ejecutar consulta `pgvector` con operador `<=>` (distancia coseno) contra `ibpms_knowledge_vectors`, filtrada por: `tenant_id` AND `knowledge_space_id` AND (`agent_id` = solicitante OR `role_id` IN roles-del-solicitante).
    And 5. **Scoring y Ranking:** Resultados ordenados por proximidad coseno descendente. Umbral mínimo configurable (default: 0.65).
    And 6. **Presupuesto de Tokens:** La lista de resultados se trunca cuando la suma acumulada de tokens exceda el `tokenBudget`, garantizando que la inyección al prompt nunca desborde la ventana del LLM.
    And 7. **Citaciones:** Cada chunk devuelto incluirá su metadata de localización (`filename`, `page`, `section`) para que el LLM pueda citar fuentes en su respuesta.
    And 8. **Actualización de recall_count:** Los chunks devueltos incrementarán su `recall_count` en +1 para métricas de utilización.

  Scenario: Ensamblaje Dual — Combinación de RAG Conversacional y Documental (CA-08)
    # Ref. ADR-013: Sección 3.4 — Flujo de Ensamblaje Dual
    Given que un agente IA puede necesitar tanto contexto conversacional (US-056) como conocimiento documental (US-057)
    When el `AssemblerService` prepare el prompt para una inferencia LLM
    Then el sistema permitirá combinar resultados de AMBOS RAGs siguiendo esta jerarquía:
    And 1. **System Prompt** (inmutable, head position — US-054 CA-04).
    And 2. **Knowledge Space Chunks** (resultados de `KnowledgeBasePort.recall()`, ordenados por score).
    And 3. **Conversational Memory Chunks** (resultados de `CognitiveMemoryPort.recall()`, ordenados por score).
    And 4. **Memoria Activa Reciente** (últimos N turnos de la sesión actual).
    And los dos conjuntos de chunks se fusionarán con un **ranking unificado por score coseno**, respetando el `tokenBudget` global.
    And el LLM verá una sección claramente delimitada: `[CONTEXTO DOCUMENTAL]` seguida de `[CONTEXTO CONVERSACIONAL]` para que pueda distinguir fuentes.
    And si el `tokenBudget` no alcanza para ambos, el conocimiento documental tiene PRIORIDAD sobre el historial conversacional (el LLM necesita "saber" antes de "recordar").

  # ==============================================================================
  # F. ENDPOINTS REST / API DE GESTIÓN
  # ==============================================================================
  Scenario: API REST de Gestión de Knowledge Spaces (CA-09)
    Given la necesidad del Administrador de gestionar los espacios de conocimiento desde el panel de control
    Then el Backend expondrá los siguientes endpoints protegidos por RBAC (rol `ADMIN_AI` o `SUPER_ADMIN`):
    And 1. **`POST /api/v1/knowledge-spaces`** — Crear un nuevo Knowledge Space. Body: `{name, description, assignedAgentIds[], assignedRoleIds[]}`. Response: HTTP 201 con `knowledgeSpaceId`.
    And 2. **`GET /api/v1/knowledge-spaces`** — Listar Knowledge Spaces del Tenant. Response: HTTP 200 con array de espacios + métricas resumidas.
    And 3. **`GET /api/v1/knowledge-spaces/{id}`** — Detalle de un Knowledge Space con estadísticas de documentos y chunks.
    And 4. **`PUT /api/v1/knowledge-spaces/{id}`** — Actualizar nombre, descripción o asignaciones de agentes/roles.
    And 5. **`DELETE /api/v1/knowledge-spaces/{id}`** — Eliminar un Knowledge Space y TODOS sus documentos y chunks asociados. Requiere confirmación doble (header `X-Confirm-Delete: true`).

  Scenario: API REST de Gestión de Documentos (CA-10)
    Given la necesidad de cargar y administrar documentos dentro de un Knowledge Space
    Then el Backend expondrá:
    And 1. **`POST /api/v1/knowledge-spaces/{ksId}/documents`** — Upload de documento. Content-Type: `multipart/form-data`. Límite: 50 MB. Formatos aceptados: PDF, DOCX, MD, TXT, PNG, JPG, XLSX, CSV. Response: HTTP 202 Accepted con `documentId` y `status: PENDING` (la indexación es asíncrona).
    And 2. **`GET /api/v1/knowledge-spaces/{ksId}/documents`** — Listar documentos del espacio con status de indexación. Filtrable por `status` (INDEXED, PENDING, FAILED).
    And 3. **`GET /api/v1/knowledge-spaces/{ksId}/documents/{docId}`** — Detalle de un documento: chunks generados, tamaño, fecha de indexación, errores si los hubo.
    And 4. **`PUT /api/v1/knowledge-spaces/{ksId}/documents/{docId}`** — Re-subir una versión actualizada. Incrementa `version`, re-indexa eliminando chunks de la versión anterior.
    And 5. **`DELETE /api/v1/knowledge-spaces/{ksId}/documents/{docId}`** — Eliminar documento y todos sus chunks del índice vectorial.
    And 6. **`POST /api/v1/knowledge-spaces/{ksId}/sync`** — Trigger manual de sincronización incremental (detecta documentos modificados y re-indexa).

  # ==============================================================================
  # G. SINCRONIZACIÓN Y CICLO DE VIDA
  # ==============================================================================
  Scenario: Sincronización Incremental y Detección de Cambios (CA-11)
    Given que los documentos pueden actualizarse con nuevas versiones a lo largo del tiempo
    Then el `KnowledgeBasePort.syncKnowledgeSpace()` ejecutará:
    And 1. **Detección de cambios:** Compara el `content_hash` (SHA-256) actual del documento con el almacenado. Si difiere, marca como `STALE`.
    And 2. **Re-indexación selectiva:** Solo los documentos `STALE` se re-procesan (parsear → chunkear → embeddings). Los chunks de la versión anterior se eliminan DESPUÉS de que la nueva versión esté completamente indexada (atomic swap).
    And 3. **Detección de eliminaciones:** Documentos presentes en el registro pero ausentes del storage se marcan como `DELETED` y sus chunks se purgan.
    And 4. **Progress Reporting:** La sincronización emite eventos de progreso `knowledge.sync.progress` con `{completed, total, label}` (replicando `MemorySyncProgressUpdate` de OpenClaw).
    And la sincronización puede ser: a) **Manual** (vía endpoint `POST /sync`), b) **Programada** (Cron configurable, default: `0 2 * * *` = 2:00 AM diario), c) **Event-Driven** (post-upload automático).

  Scenario: Garbage Collection de Chunks Huérfanos (CA-12)
    Given el crecimiento de la tabla `ibpms_knowledge_vectors` a lo largo de meses de operación
    Then un Scheduled Job (Spring `@Scheduled`, protegido por ShedLock) ejecutará semanalmente:
    And 1. **Purga de chunks huérfanos:** Eliminar chunks cuyo `document_id` no exista en `ibpms_knowledge_documents` (huérfanos por eliminación incompleta).
    And 2. **Purga por expiración:** Eliminar chunks con `expires_at < NOW()` (si el Administrador configuró TTL).
    And 3. **Consolidación de versiones:** Eliminar chunks de versiones anteriores a la activa (`document_version < current_version`).
    And 4. **Reindexación HNSW:** Ejecutar `REINDEX` sobre el índice vectorial tras la purga para optimizar búsquedas ANN.
    And PROHIBIDO ejecutar durante horas pico. Cron default: `0 4 * * 0` (Domingos 4:00 AM).

  # ==============================================================================
  # H. OBSERVABILIDAD Y AUDITORÍA
  # ==============================================================================
  Scenario: Bitácora de Eventos de la Base de Conocimiento (CA-13)
    Given la necesidad de auditar todas las operaciones sobre la base de conocimiento
    Then el sistema registrará eventos tipados en la tabla `ibpms_knowledge_event_log` (append-only):
    And 1. **`knowledge.document.indexed`**: Documento indexado exitosamente. Registra: documentId, filename, chunkCount, embeddingModel, durationMs.
    And 2. **`knowledge.document.failed`**: Fallo de indexación. Registra: documentId, filename, errorMessage, parserUsed.
    And 3. **`knowledge.recall.recorded`**: Cada búsqueda RAG ejecutada. Registra: query, knowledgeSpaceId, agentId, resultCount, topScore, durationMs.
    And 4. **`knowledge.sync.completed`**: Sincronización finalizada. Registra: knowledgeSpaceId, docsUpdated, docsAdded, docsRemoved, durationMs.
    And 5. **`knowledge.space.modified`**: Cambios en la configuración del Knowledge Space (asignaciones de agentes/roles).
    And estos eventos alimentarán el dashboard de observabilidad IA (US-044).

  # ==============================================================================
  # I. DIRECTIVAS DE REFACTORIZACIÓN Y TRAZABILIDAD
  # ==============================================================================
  Scenario: Mapa de Transposición OpenClaw → iBPMS para Knowledge Base Engine (Refactoring Ledger) (CA-14)
    Given la directiva de implementación basada en el código fuente de OpenClaw
    Then el equipo de desarrollo DEBE mantener un documento de trazabilidad (`docs/architecture/knowledge-base-refactoring-ledger.md`) que mapee:
    And 1. Cada archivo TypeScript de OpenClaw → su clase/interfaz Java equivalente en iBPMS.
    And 2. Decisiones de diseño donde se divergió del patrón original y la justificación técnica.
    And 3. Funciones de OpenClaw reutilizadas vs. creadas desde cero para US-057.
    And el mapa mínimo obligatorio de transposición es:
    And | OpenClaw (TypeScript) | iBPMS (Java) |
    And | `listMemoryFiles()` + `buildFileEntry()` (internal.ts) | `DocumentDiscoveryService` (infra service) |
    And | `chunkMarkdown()` (internal.ts) | `TextChunkingService` (compartido con US-056) |
    And | `classifyMemoryMultimodalPath()` (multimodal.ts) | `MultimodalClassifierService` (infra service) |
    And | `buildMultimodalChunkForIndexing()` (internal.ts) | `MultimodalChunkBuilder` (infra component) |
    And | `MemorySearchManager` (types.ts) | `KnowledgeSearchAdapter` (infra adapter → pgvector) |
    And | `MemoryFileEntry` (internal.ts) | `IndexedDocument` (domain model) |
    And | `MemoryChunk` (internal.ts) | `DocumentChunk` (domain model, extends `MemoryChunk` de US-056) |
    And | `ensureMemoryIndexSchema()` (memory-schema.ts) | Flyway migration `V{N}__create_knowledge_vectors.sql` |
    And | `extractKeywords()` (query-expansion.ts) | `QueryExpansionService` (compartido con US-056) |
    And | `MemoryHostEvent` (events.ts) | `KnowledgeAuditEvent` (domain event) |
    And | N/A (nuevo) | `DocumentParserService` (Strategy pattern, extensible) |
    And | N/A (nuevo) | `KnowledgeSpaceEntity` (domain aggregate) |
```

**Notas de Implementación (Non-Functional Requirements):**
- **Aislamiento Multi-Tenant:** Todas las operaciones de conocimiento DEBEN filtrar por `tenant_id`. Un Tenant NUNCA puede acceder a documentos o chunks de otro Tenant. Row-Level Security (RLS) habilitado en `ibpms_knowledge_vectors`.
- **FinOps:** Cada operación de embedding documental generará un registro de consumo de tokens con `billing_source = KNOWLEDGE_INDEXING` (distinto de `CONVERSATION_MEMORY` de US-056), vinculado al `billing_source` de US-053.
- **Resiliencia:** Si el proveedor de embeddings falla durante la indexación, el documento queda en `status = FAILED` con `error_message` detallado. El Administrador puede reintentar vía `POST /sync` o re-upload.
- **Testing (Dry-Run):** Los tests unitarios usarán embeddings mock de dimensión fija (Ej: vector de 10 dimensiones con valores aleatorios reproducibles) para validar el pipeline sin llamar a APIs reales ($0 USD de costo).
- **Límites Operativos:** Default: máximo 500 documentos por Knowledge Space, máximo 50,000 chunks por Tenant. Configurables en `application.yml`.
- **Consistencia de Embeddings:** Todos los documentos de un Knowledge Space DEBEN usar el MISMO modelo de embedding. Cambiar el modelo requiere re-indexación completa del espacio.

**Trazabilidad UX:** Panel de gestión de Knowledge Spaces en Pantalla 15.B (Gobernanza IA — US-044). Endpoints REST consumidos por el panel administrativo del Antigravity Command Center (US-053).

---
