# Requerimientos Funcionales y Estrategia Exponencial (PRD)
**Producto:** Plataforma iBPMS (Intelligent Business Process Management System) -> Agentic Workflows

## Objetivo Estratégico
Desarrollar una plataforma integral de gestión de procesos y proyectos que permita a las organizaciones acelerar su transformación digital, migrando procesos manuales a modelos digitales de forma ágil, estructurada y medible.
**Esto permite a la compañía adaptarse rápidamente a los constantes cambios del entorno económico y a los nuevos retos detonados por la Inteligencia Artificial (IA).** El foco principal es habilitar la modelación rápida de procesos, incorporando reglas de negocio, validaciones, métricas de desempeño y control operativo.

**El North Star Metric:** Mediremos el **"Time-to-Value (TTV) de Orquestación"**: El tiempo promedio que tarda un usuario de negocio desde que tiene una necesidad hasta que la regla, proceso o subproceso está operando en producción, sin tocar código.

## 2. Product Discovery y Pain Points

**Contexto:** Los procesos operativos críticos varían significativamente según el cliente y su industria, siendo este dominio el gran "océano azul" donde radica el mayor valor de automatización de nuestra solución. Típicamente, estos procesos (aprobaciones, altas, contratos) están fragmentados y los usuarios interactúan con múltiples sistemas (Office 365, ERP, correos), generando cuellos de botella y pérdida de información.

**Pain Points Identificados:**
1. **Dispersión de Tareas:** Falta de un punto único de entrada (Unified Inbox).
2. **Falta de Trazabilidad:** Nula visibilidad del estado "end-to-end" ni SLAs.
3. **Reglas Rígidas:** Lógica de enrutamiento requiere intervención de TI.
4. **Captura Manual:** Leer correos y descargar adjuntos consume horas hombre.

**Segmentos a Disrumpir:**
*   **Para IT:** Plataforma centralizada y auditable (Hexagonal), delegando la creación a negocio.
*   **Para Negocio:** Autonomía para construir aplicaciones operativas complejas en minutos usando lenguaje natural.

## 3. Roadmap Estratégico (V1 a V2: Pivote AI-Centric)

| Fase / Capa | MVP Táctico Extendido (V1 - 4 a 5 Meses TTM) | Innovación Disruptiva (V2 AI-Native) |
| :--- | :--- | :--- |
| **Motor Central** | Orquestador Táctico Limpio (Camunda) con capacidades robustas para Kanban, Agilidad y minitareas. | **AI-Centric Engine (Multi-Agent System):** Reemplazo absoluto del BPMN rígido. Sistema multi-agente que enruta dinámicamente basado en "Intento" (Intent) y resolución de objetivos en tiempo real. |
| **UX / Frontend** | Workbenches Web, Formularios Dinámicos y Tableros Kanban. | **Zero-UI (Headless) & Conversational:** Ejecución directa vía chatbots, Slack/Teams y correos interactivos. |
| **Lógica / Reglas**| Tablas DMN manuales y **Traductor de IA (LLM API, Ej. Claude)** que convierte lenguaje natural a reglas DMN ejecutables. | **Razonamiento en Tiempo Real:** La IA detecta excepciones no programadas y sugiere/opera soluciones *on-the-fly*. |
| **Analítica & Mejora** | **Dashboards BAM & Salud Operativa** desplegados sobre infraestructura de BI analítico (Grafana/PowerBI). | **Consultor Digital AI (ML):** Aprendizaje continuo. La plataforma actúa como consultor, detectando ineficiencias ocultas y proponiendo rediseños basados en datos reales de operación. |
| **Auditoría & Control**| Bitácoras pasivas de lectura/escritura (Audit Logs). | **Auditor AI (Plug-in Compliance):** Auditoría activa y continua (ISO 9001) sobre procesos gestionados y evaluación de operaciones manuales periféricas. Genera hallazgos de riesgo accionables. |
| **Arquitectura**  | Arquitectura Hexagonal estricta (Aísla el motor V1 para evitar Deuda Técnica y permitir el reemplazo de V2). | Agentic RAG y Graph DB. Los procesos, automatizaciones y funcionalidades nacen a partir de las capacidades de la IA, siendo la inteligencia el núcleo del diseño. |

## 4. Requerimientos Core del Dominio

*   **Gestión de Procesos y Proyectos (CRUD & Permisos):** Permitir la creación, edición, eliminación y visualización de procesos (secuenciales, paralelos, Kanban) y proyectos. Todo el ciclo de vida incluye la asignación de permisos granulares a otros usuarios para interactuar con la configuración y metadata asociada.
*   **Gestión de Ciclo de Vida BPMN (Versiones y Despliegues):** El sistema permite importar/crear modelos BPMN, versionarlos sistemáticamente (v1.0, v1.1, v2.0) y publicar/despublicar versiones de instancias activas sin afectar los flujos ya en ejecución.
*   **Plantillas Reutilizables (Templates):** Capacidad de guardar y reutilizar componentes operativos como plantillas de flujos, fragmentos DMN, esquemas de formularios dinámicos, configuraciones de SLA, plantillas de notificaciones y layouts de dashboards.
*   **Delegación y Sub-Tareas Ad-hoc:** Permite a un "Assignee" (Usuario asignado) crear sub-tareas dinámicas y asignarlas a terceros, las cuales deben resolverse para desbloquear o completar la Actividad/Tarea padre original. Incluye opciones de reasignación fluida.
*   **Medición de Desempeño:** Capacidad funcional, como parte del modelo base, para medir el desempeño y la eficiencia en la ejecución de actividades, equipos y flujos.
*   **IA de Metadatos Operativos:** Creación automática (mediante IA) de metadatos asociados a las partes y objetos del proceso, permitiendo enriquecer la información y facilitar la gestión interna de la plataforma.
*   **IDE Pro-Code de Formularios (Vue 3/Zod):** Un *Form Engine* web embebido (Low-Code/Pro-Code con Mónaco Editor) que diseña visualmente pero compila archivos Vue 3 nativos y esquemas Zod en vez de JSON. Define lógicas avanzadas usando el patrón "iForm Maestro".
*   **Gestión Documental Avanzada (Archivos & Metadatos):** Permite adjuntar documentos directamente al "Caso" (Process Instance) o a la "Tarea" específica, aplicando versionado, clasificación categórica (metadata) e inmutabilidad (Hash).
*   **Catálogo Federado de Servicios (CRM Sync):** Sincronización dinámica de catálogos desde el CRM.
*   **Experiencia (Workbenches):** Bandeja unificada con filtros.
*   **Generador Documental Jurídico (SGDEA):** Inyectar JSON en PDF.
*   **Seguridad, Auditoría Estricta y Trazabilidad (Bitácora):** Registra explícitamente "Quién hizo qué, cuándo (Timestamp exacto), el estado anterior/nuevo (Before/After), qué reglas DMN se aplicaron y qué decisión humana se tomó". Permite la exportación tabular asíncrona de la auditoría para entes de control interno.
*   **Analítica Operativa (BAM - V1):** Dashboards de "Process Health".
*   **Consultor Digital AI & Mejora Continua (V2):** ML para diagnosticar fricciones.
*   **Auditor Digital AI (ISO 9001 Compliance - V2):** Plug-in que audita automáticamente.

## 5. Backlog Management (Épicas V1)

### Épica 1: Bandeja de Tareas Unificada (Unified Inbox & Workbenches)
*   **US 1.1:** *Como usuario*, quiero ver una lista consolidada de mis tareas y proyectos ordenados por SLA.
*   **US 1.2:** *Como sistema (ABAC)*, debo filtrar las tareas basándome en los permisos del usuario final.

### Épica 2: Generación de Formularios Ide-Based (Vue 3 & Zod)
*   **US 2.1:** *Como Administrador/Desarrollador*, quiero elegir si voy a diseñar un "Formulario Simple" o un "iForm Maestro" al iniciar el Canvas.
*   **US 2.2:** *Como Desarrollador*, quiero una pestaña "Código" al lado de la visual que me muestre en tiempo real el código Vue 3 y Zod generado, permitiendo bindings con Swagger.

### Épica 3: Captura Inbound AI y Generación Documental
*   **US 3.1:** *Como Gestor de Casos (Human-in-the-Loop)*, quiero que un Asistente Virtual clasifique los correos corporativos, identifique al cliente vía CRM y me proponga un borrador de respuesta bilingüe.
*   **US 3.2:** *Como Auditor*, quiero que las decisiones sobre las sugerencias de la IA mantengan cadena de custodia intacta (eDiscovery) y provean métricas de eficiencia.
*   **US 3.3:** *Como sistema*, al aprobar un expediente, quiero generar un PDF contractual y enviarlo por correo directamente desde el WorkBench.

### Épica 4: Modelado BPMN, Proyectos y Catálogo Federado
*   **US 4.1:** *Como Arquitecto*, quiero un lienzo visual integrado (bpmn-js) para dibujar, importar y desplegar procesos sin compilar código Java.
*   **US 4.2:** *Como Administrador*, quiero configurar un mapa JSON sin tocar código que vincule los campos del CRM con las variables operativas de mi proceso BPMN.
*   **US 4.3:** *Como Usuario Final/Cliente*, quiero ver el catálogo disponible incluso si el CRM está caído temporalmente (Modo Degradado).
*   **US 4.4:** *Como Arquitecto Modelador*, quiero un "Copiloto IA Tutor" empotrado en el lienzo BPMN que audite en tiempo real mi diseño, me alerte de antipatrones BPMN 2.0 y sugiera mejoras basadas en ISO 9001.

### Épica 5: Service Delivery Intake Inteligente y Portal B2B
*   **US 5.1:** *Como Operador*, quiero enviar correos desde el iBPMS que "abran" la puerta al cliente pero *sin* instanciar un proceso, sino creando una tarea controlada ("Plan A") para evitar instancias BPMN basura.
*   **US 5.2:** *Como Administrador*, quiero tener un botón global protegido por rol ("Plan B") para crear Servicios Operativos manualmente asociando un cliente y una plantilla.
*   **US 5.3:** *Como Ejecutivo de Cuenta Interno*, quiero una Vista 360 (Card de Cliente) que me permita auditar todas las actividades pendientes consolidadas por servicio y etapa para responder dudas del cliente.
*   **US 5.4:** *Como Cliente Externo (B2B/B2C)*, quiero un Portal Web seguro para auto-consultar el estado (Táctico) de mis solicitudes activas y descargar el histórico finalizado (Estratégico).

### Épica 6: Ejecución, Reglas (DMN) e IA Analítica
*   **US 6.1:** *Como usuario*, quiero que el motor Camunda evalúe reglas DMN automáticamente (ej. derivar a VPE o auto-aprobar) según el payload del iForm maestro.
*   **US 6.2:** *Como usuario de negocio sin nociones de programación*, quiero escribir en lenguaje natural qué decisión quiero, y que la plataforma cree una tabla DMN usando IA.
*   **US 6.3:** *Como Analista*, quiero ver el Historial Causal (Audit Trail visual de Camunda) de por qué se tomó una decisión (Qué regla exacta hizo match).
