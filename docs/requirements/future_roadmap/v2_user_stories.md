---

# 🚀 ROADMAP VERSIÓN 2 (V2) - EN REFINAMIENTO
*(Todas las funcionalidades, épicas e historias de usuario declaradas a partir de este punto pertenecen estructural y financieramente a la Fase 2 del Proyecto iBPMS. No forman parte del alcance del MVP V1).*

---

## ÉPICA V2-00: Cierre de Deuda Técnica de la Versión 1
Recopilación de Criterios de Aceptación que fueron definidos en la V1 pero diferidos formalmente durante la auditoría arquitectónica ARQ-005 (sprint-6, 2026-05-01). Estos CAs tienen esqueleto backend parcial y requieren completar su implementación funcional.

> **Origen:** Épica B — IDE Formularios, Diseño BPMN & Reglas DMN (US-005)
> **Decisión de diferimiento:** Arquitecto Líder + PO (2026-05-01)
> **Referencia de auditoría:** `.agentic-sync/audit_ARQ005_bloque3.md`

### US-V2-DT-001: Funcionalidades Diferidas del Diseñador BPMN (Pantalla 6)

**Como** Arquitecto de Procesos (BPMN Designer)
**Quiero** que se completen las funcionalidades avanzadas del Diseñador BPMN que fueron diferidas durante la V1
**Para** mejorar la experiencia de diseño, la seguridad de datos y la robustez de las integraciones externas.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Deuda Técnica V1 — Diseñador BPMN (US-005)

  Scenario: Diff Visual entre Versiones (CA-28 - Diferido desde V1)
    # ORIGEN: epic_B_formularios_bpmn.md, US-005, Línea 1473
    # NOTA: Escenario documentado en V1 pero diferido por complejidad de implementación.
    Given el Arquitecto navega al Historial de Versiones y selecciona v2 y v3 para comparar
    Then el sistema muestra un Diff visual resaltando nodos agregados (verde), eliminados (rojo) y modificados (amarillo).

  Scenario: Colores Personalizados en Carriles y Tareas (CA-37 - Diferido desde V1)
    # ORIGEN: epic_B_formularios_bpmn.md, US-005, Línea 1531
    # NOTA: Escenario documentado en V1 pero diferido por baja prioridad funcional.
    Given el Arquitecto selecciona un Carril o Tarea en el Lienzo
    Then puede asignarle un color personalizado desde una paleta de colores para distinguir departamentos.

  Scenario: Validación Lógica de Cláusulas OneOf/AnyOf (CA-53 - Diferido desde V1)
    # ORIGEN: epic_B_formularios_bpmn.md, US-005, Línea 1633
    # NOTA: Esqueleto backend existe (DataMapperGrid + PreFlight). Falta lógica interna de validación OpenAPI.
    Given una API que exige el dato X *o* el dato Y mediante las cláusulas Swagger (OneOf / AnyOf)
    When el Frontend despliega el `<DataMapperGrid>`
    Then agrupa visualmente las filas afectadas bajo la etiqueta `[ 🔀 Requiere mapear al menos UNO ]`
    And el Pre-Flight Analyzer verificará el grupo lógico en conjunto: Si falta al menos uno, alerta roja y aborta despliegue. Si ambos están vacíos, aborta. Si uno está lleno, autoriza el pase a Producción.

  Scenario: Shift-Left Security para Datos Sensibles (PII/PHI) (CA-54 - Diferido desde V1)
    # ORIGEN: epic_B_formularios_bpmn.md, US-005, Línea 1639
    # NOTA: Sin implementación en V1. Requiere flag PII en modelo de variables + redacción en Camunda History.
    Given el mapeo de una variable clasificada con el flag `[🔒 Dato Sensible PII]` desde la Pantalla 7 (Zod)
    When la Service Task dispara la integración hacia la API externa
    Then el dato crudo viaja obligatoriamente encriptado por el túnel HTTP/TLS
    And el motor de auditoría histórica de Camunda (History Level) tiene estrictamente PROHIBIDO persistir el valor real en texto plano dentro de sus logs, reemplazándolo obligatoriamente por un hash o la viñeta `[REDACTED_PII]`.

  Scenario: Delegación Transparente de Conversión Binaria (Multipart/Base64) (CA-56 - Diferido desde V1)
    # ORIGEN: epic_B_formularios_bpmn.md, US-005, Línea 1651
    # NOTA: OutboundDispatcherService existe con CircuitBreaker/Retry pero sin auto-detección de formato binario.
    Given un componente Zod de tipo `<InputFile>` mapeado hacia un atributo del Payload destino
    When el Arquitecto despliega y llega el momento de la ejecución
    Then el flujo UI no exige que el Arquitecto indique la técnica de conversión
    And el Worker (Backend) intercepta el mapping, consulta en caliente el requerimiento del Swagger (Multipart-FormData vs Base64), y lo transmuta automáticamente antes de inyectar la data a la trama HTTP de salida.
```
**Trazabilidad UX:** Pantalla 6 (BPMN Designer), Pantalla 7 (Form Builder), Panel de Integraciones.
**Estado Infraestructura V1:** Tablas `ibpms_data_mappings`, `ibpms_external_task_topics` ya existen. Puertos hexagonales (`DataMappingPort`, `BpmnAuditPort`) implementados.

### US-V2-DT-002: Refactorización y Deuda Técnica de Backend (ARQ-005)

**Como** Arquitecto Backend
**Quiero** saldar la deuda técnica funcional identificada durante la auditoría ARQ-005
**Para** garantizar la resiliencia en alta concurrencia y flexibilidad multi-tenant.

**Items de Deuda Técnica:**
- **DT-V2-001:** Migrar `@Cacheable` a Redis distribuido (GAP-003)
- **DT-V2-002:** Hacer umbrales SLA configurables por tenant (GAP-004)
- **DT-V2-003:** Exponer `slaCreationDate` en DTO para cálculo dinámico de ventana SLA (GAP-006)

---


## ÉPICA V2-01: Gobernanza Activa y Erradicación de Antipatrones (Opinionated OS)
El iBPMS deja de ser un lienzo ciego y se convierte en un auditor inteligente del diseño de procesos. Interviene físicamente para evitar que las empresas democraticen la ineficiencia (como aprobadores redundantes que no mutan el modelo de datos).

### US-V2-001: Bloqueo Arquitectónico de Burocracia Humana (Hard-Stop)
**Como** Motor de Gobernanza (Opinionated OS)
**Quiero** auditar el I/O Binding de las tareas humanas en el Pre-Flight Analyzer (US-005)
**Para** bloquear físicamente el despliegue de procesos que modelen "sellos de goma" y firmas inútiles.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Forced DMN Adoption over Human Bureaucracy
  Scenario: Detección Temprana en el Lienzo (Linting en Tiempo Real) (CA-1)
    Given el Arquitecto diseñando un flujo en la Pantalla 6 (Canvas BPMN)
    When conecta dos (2) o más `UserTasks` humanas de forma secuencial
    And selecciona para la segunda tarea un Formulario que NO requiere inyección de campos nuevos (solo lectura de la tarea anterior)
    Then el motor de UI del Modeler debe dibujar la flecha (SequenceFlow) de conexión en color ROJO intermitente
    And proyectar un ícono de advertencia (⚠️) sobre la tarea redundante, alertando del antipatrón burocrático de forma inmediata antes del guardado.

  Scenario: Bloqueo Estructural en el Pre-Flight (Hard-Stop) (CA-2)
    Given las advertencias visuales del Canvas ignoradas
    When el Arquitecto intenta presionar [🚀 DESPLEGAR]
    And el motor Pre-Flight cruza el I/O Mapping contra el Esquema Zod confirmando la ausencia de mutación de datos
    Then el sistema CANCELA el despliegue cambiando el estado a ❌ ERROR CRÍTICO
    And el Copiloto IA arroja un modal bloqueante: "🛑 Antipatrón burocrático: La aprobación humana que no altera el contrato de datos no genera valor. Reemplace la tarea por una Regla DMN o condense la autoridad en un solo rol."

  Scenario: Inmunidad por Decisión de Enrutamiento (Gateway Decision Validity) (CA-3)
    Given la configuración de un "Sello de Goma" humano (Ej: Gerente que solo aprueba/rechaza)
    When el motor detecta que el output (salida) de la tarea de ese Gerente está directamente conectado a un Gateway Exclusivo (XOR) para definir la ruta del proceso
    Then el analizador interpreta la acción de "Toma de Decisión de Ruta" como una agregación de valor cognitivo ("Gateway Validity")
    And perdona la configuración, absteniéndose de lanzar el bloqueo rojo, permitiendo el despliegue del proceso.

  Scenario: Excepción por Responsabilidad Legal Expresa (Override Auditado) (CA-4)
    Given el bloqueo Hard-Stop del Pre-Flight disparado por un antipatrón redundante
    And que la empresa argumente fuerza mayor corporativa (Ej: ISO 9001 o mandato legal de doble firma)
    Then el modal bloqueante cuenta con un botón en fuente pequeña `[Ignorar Advertencia y Asumir Riesgo]`
    When el Arquitecto lo presiona
    Then el sistema le solicita una justificación de texto obligatoria y su contraseña de confirmación
    And permite el despliegue liberando el proceso, pero estampando el evento crudo en el Log de Auditoría bajo el tag `[BUREAUCRATIC_DEBT_ASSUMED]` responsabilizándolo permanentemente.
```
**Trazabilidad UX:** Pantalla 6 (BPMN Canvas) y Modal de Auditoría Pre-Flight.

---

### US-V2-002: El Asesino del "Síndrome de la Sandía" (Friction Tax Calculator)
**Como** CEO / Director de Transformación (BAM Dashboard)
**Quiero** que el sistema calcule exactamente cuánto dinero me cuesta la fricción y el tiempo muerto en mis procesos
**Para** tener argumentos financieros innegables que obliguen a los gerentes medios a automatizar sus feudos.

*Nota Estratégica: El síndrome de la sandía ocurre cuando los KPIs están "verdes" por fuera (el empleado cumplió su SLA de 2 horas para firmar), pero "rojos" por dentro (el caso estuvo estacionado 15 días esperando en la bandeja).*

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Friction Tax Telemetry (Value-Driven BAM)
  
  Scenario: Personalización del Dolor Financiero (Multiplicador de Gravedad Nodal) (CA-1)
    Given el Arquitecto diseñando la configuración financiera del proceso
    Then el sistema le permite asociar a un Nodo Humano específico (UserTask) un `[Costo_Fijo_Retraso_Hora]`
    And este valor desvincula la matemática del simple "salario del empleado"
    And permite traducir el Riesgo de Negocio (Ej: Un SLA incumplido en 'Aprobación VIP' cuesta $500/hora en multas) directamente al lenguaje gerencial del Tablero BAM.

  Scenario: El Reloj Justo (SLA-Aware Taxometer) (CA-2)
    Given un reclamo que cae a la bandeja de un usuario a las 6:00 PM del Viernes y es atendido a las 8:00 AM del Lunes
    When el motor analítico calcula el "Tiempo Muerto" (Wait Time) para monetizar la ineficiencia
    Then el algoritmo cruza obligatoriamente los timestamps contra la Matriz SLA Corporativa (US-043, Días/Horas Hábiles)
    And el Taxímetro se PAUSA automáticamente durante el fin de semana, cobrando $0 dólares por ese periodo
    And evitando "Data Contaminada" y desmotivación en los empleados por penalizaciones injustas fuera de su turno legal.

  Scenario: Telemetría de Sangría en Tiempo Real (Live Pain Counter) (CA-3)
    Given el Director de Transformación observando el BAM Dashboard en medio de la operación diurna
    Then el tablero expone un módulo gigante en ROJO denominado "Friction Tax: Dinero Quemado AHORA MISMO"
    And este indicador totaliza y grafica en Tiempo Real (Live) el costo acumulado por segundo de todos los casos que están atascados en las bandejas vivas
    And generando un sentido de urgencia psicológica para la intervención inmediata, en lugar de ser una simple autopsia post-mortem de fin de mes.
```
**Trazabilidad UX:** Dashboard Gerencial BAM (Grafana/Kibana) y Panel de Configuración Nodal.

---

### US-V2-003: Penalización por Carga Cognitiva en Formularios (Data Diet)
**Como** Motor UX del Sistema
**Quiero** auditar el destino de cada campo de UI creado en el "iForm Maestro"
**Para** impedir que la empresa capture datos "por si acaso" que nunca usa, saturando al usuario final.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Data Minimization and Form Strictness
  Scenario: El Peaje Analítico (Opcionalidad Forzada) (CA-1)
    Given un Diseñador intentando justificar un campo inútil para la operación alegando que su único fin es la exportación a PowerBI (Big Data ciego)
    When el IDE marca el campo como ROJO (Campo Huérfano)
    Then el Diseñador debe abrir obligatoriamente las propiedades del campo y seleccionar el [Destino Estratégico: Analítica Pasiva / Reportes]
    And al seleccionarlo, el esquema Zod subyacente DESHABILITA y BLOQUEA físicamente el switch de "Requerido" u "Obligatorio" para ese input
    And garantizando que la empresa pueda recolectar datos analíticos, pero prohibiendo estrictamente que se conviertan en una fricción bloqueante para el usuario final.

  Scenario: La Ley del Lector Garantizado (Campos de Texto Libre) (CA-2)
    Given la inserción por costumbre de un Área de Texto (Text-Area) para "Observaciones" que ninguna regla DMN puede evaluar matemáticamente
    When el Pre-Flight Analyzer audita el futuro de dicha variable en el proceso
    Then el motor le otorga el Indulto de Supervivencia ÚNICAMENTE si detecta uno de los tres "Lectores Garantizados" aguas abajo:
    And 1. Una `UserTask` humana donde un Analista leerá la variable.
    And 2. Una `GenerativeTask` (RAG / AI) que extraerá sentimiento o resumirá el texto.
    And 3. Un mapeo explícito de inyección SGDEA (Ej: Imprimir el campo dentro del PDF legal final).
    And bloqueándolo sin piedad si carece de estos 3 destinos.

  Scenario: Libertad de Bosquejo vs Guillotina en Producción (CA-3)
    Given el proceso creativo de un Arquitecto de Negocio
    When se encuentra en la Pantalla 3 (Form Builder) creando los inputs
    Then el sistema arroja un Soft-Lock visual (Iconos naranjas/rojos ⚠️) advirtiendo la falta de destino, pero permite guardar el trabajo tranquilamente en estado `DRAFT_INVALID` para no destruir la iteración.
    When días después, el Arquitecto une el formulario al BPMN en la Pantalla 6 e intenta pulsar el botón [🚀 DESPLEGAR A PRODUCCIÓN]
    Then el sistema arroja el Hard-Stop defintivo y ABORTA el Despliegue con un flag rojo ❌, obligándolo a higienizar (borrar o justificar) los campos huérfanos antes de impactar el Core productivo.
```
**Trazabilidad UX:** Pantalla 3 (Diseñador de Formularios - Form Builder).

---

### US-V2-004: Auto-Destrucción de Nodos Zombie (The Darwinian Engine)
**Como** Motor de Gobernanza MLOps (Agente Data Scientist de Turno Nocturno)
**Quiero** auditar el comportamiento histórico de los operarios humanos
**Para** sugerir la eliminación de reglas, tareas o firmas que la data empírica demuestre que son inútiles.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Continuous AI Pruning (Self-Healing Organization)
  Scenario: Slider de Tolerancia y Frenos de Emergencia (Parametrización MLOps) (CA-1)
    Given la configuración del Motor Darwiniano en la Pantalla 15.A (SysAdmin)
    When el cliente ajusta el `[Umbral_Inercia_Zombie]` y la `[Ventana_Analisis_Meses]`
    Then el sistema prohíbe estadísticamente bajar el umbral de inercia a menos del 85%
    And garantizando que si un humano rechaza o altera el 15% o más de los casos, la IA asume que SÍ está utilizando criterio cognitivo, bloqueando la etiqueta de "Zombie" para proteger la evaluación de riesgo real (Ej: Prevención de Fraude).

  Scenario: El Muro de Fuego Legal (Cero Skynet) (CA-2)
    Given el Agente Data Scientist descubriendo una Tarea Humana 99% inútil en la madrugada
    When consolida el hallazgo, calcula el ahorro en EBITDA y lo reporta a la Junta (Pantalla 5)
    Then el sistema TIENE ESTRICTAMENTE PROHIBIDO auto-parchear Producción de forma autónoma
    And exige que un rol fiduciario (PMO o Director) apruebe manualmente el hallazgo presionando `[Aceptar Hallazgo y Auto-Refactorizar]`, asumiendo la responsabilidad legal de la automatización por principio de Segregación de Funciones.

  Scenario: Cirugía Asistida y Respeto al SDLC (Generación de Drafts) (CA-3)
    Given la aprobación del Hallazgo Darwiniano por parte de la PMO
    When el Agente IA recibe la orden de ejecución
    Then el sistema NO altera la versión V1.0 que opera transaccionalmente en Producción
    And en background, el Agente abre la Pantalla 6 (Modeler), clona el mapa, extirpa la caja humana, inyecta la regla matemática DMN, sella las conexiones BPMN y lo guarda silenciosamente como `V1.1-DRAFT`
    And finalmente dispara un Ticket al Workdesk del Arquitecto IT diciendo: "Borrador de Optimización Generado. Revise las conexiones y presione Desplegar".
```
**Trazabilidad UX:** Pantalla 5 (AI Copilot / Evolution Findings) y Modeler (Auto-Refactor).

---

### US-V2-005: Desacoplamiento de Workers a Microservicios (Node.js)

**ÉPICA Afectada:** Arquitectura Core & Resiliencia Cloud (V2)  
**Valor de Negocio:** Garantizar que los fallos u operaciones pesadas en componentes externos/sistemas integrados (Ej. Office 365, NLP, OCR) no impacten bajo ninguna circunstancia el rendimiento ni la estabilidad del sistema principal (ibpms-core) ni el servicio a los usuarios finales.  
**Dependencias:** Repositorio principal estabilizado y mecanismos de M2M Auth implementados.

**Descripción:**
Como Administrador de Arquitectura TI de iBPMS,
quiero extraer el agente "External Task Client" de Camunda fuera del monolito en Spring Boot,
para que las tareas automatizadas pesadas o inestables se procesen en un microservicio aislado de Node.js,
y así evitar caídas en el event-loop principal que interactúa con portal ciudadano y frontales de la plataforma.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: External Worker Decoupling via Node.js
  Scenario: Creación del Scaffold Minimalista de Node.js (CA-1)
    Given un entorno Docker/Docker Compose del ecosistema iBPMS local
    When se despliega el contenedor `ibpms-worker-node` alojando la librería `camunda-external-task-client-js`
    Then el trabajador se levanta consumiendo menos de 50MB de RAM y puede sondear exitosamente el endpoint del core `api/v1/engine-rest/external-task/fetchAndLock`.

  Scenario: Extracción de Lógica Monolítica (CA-2)
    Given que el monolito `ibpms-core` originalmente manejaba tareas mediante el Auto-Configuration client
    When se migran los delegados (ej. procesamiento de correos SAC, NLP de DMN, y firmas digitales) a scripts de TypeScript en el worker
    Then el `application.yml` del monolito mantiene deshabilitado permanentemente el servicio (`camunda.bpm.client.disable: true`) sin generar pérdida en el flujo de los diagramas BPMN.

  Scenario: Paso de Seguridad Intranet (M2M Auth) (CA-3)
    Given que la API REST del monolito está protegida por Spring Security (Zero-Trust)
    When el Worker Node.js solicita tareas
    Then el Worker adjunta un Token Cliente-Servidor (JWT M2M o API Key interno) garantizado y es autorizado para interactuar en el pool por parte del Core, emitiendo un reporte 200 OK.

  Scenario: Test de Aislamiento de Fallo (CA-4)
    Given un escenario crítico donde una integración falla o entra en timeout masivo
    When el Worker Node.js colapsa, consumiendo memoria bruta hasta matar su propio contenedor
    Then el `ibpms-core` monolítico sigue respondiendo 100% verde y operando la UI sin registrar alteraciones en su ApplicationContext
    And el Docker daemon regenera automáticamente el worker (restart: always).
```
**Trazabilidad Arquitectónica:** Repositorio `ibpms-worker-node`, `docker-compose.yml`, Spring Security Config.
