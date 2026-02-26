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
*   **Medición de Desempeño:** Capacidad funcional, como parte del modelo base, para medir el desempeño y la eficiencia en la ejecución de actividades, equipos y flujos.
*   **IA de Metadatos Operativos:** Creación automática (mediante IA) de metadatos asociados a las partes y objetos del proceso, permitiendo enriquecer la información y facilitar la gestión interna de la plataforma.
*   **Formularios Inteligentes (Data to JSON):** Un *Form Engine* que convierte inputs humanos en estructurados (JSON) para evaluar reglas.
*   **Experiencia (Workbenches):** Bandeja unificada con filtros nativos por Estado/Prioridad/Tiempo, consulta de adjuntos e historial de decisión.
*   **Alertas y SLAs:** Notificaciones proactivas sobre eventos del ciclo de vida (ej. fechas de finalización, inputs de colegas) y alertas corporativas exclusivas (Correo electrónico y Notificaciones In-app).
*   **Integración O365 (Bidireccional):** Plugin de Outlook para convertir correos en expedientes y responder desde la plataforma.
*   **Generador Documental Jurídico (SGDEA):** Inyectar el JSON en plantillas para generar contratos PDF inmutables con validez de *Record*.
*   **Seguridad (ABAC):** RBAC avanzado, SSO con IAM y Bitácora de Auditoría inmutable.
*   **Analítica Operativa (BAM - V1):** Dashboards de "Process Health" para visualizar cuellos de botella en vuelo.
*   **Consultor Digital AI & Mejora Continua (V2):** La plataforma no solo orquesta, sino que ingiere el histórico de operaciones a través de Machine Learning para: (1) Diagnosticar fricciones, (2) Proponer proactivamente rediseños de procesos con métricas objetivas de eficiencia, y (3) Evolucionar la automatización basado en datos reales (Continuous Learning Profile), generando valor de consultoría sostenida.
*   **Auditor Digital AI (ISO 9001 Compliance - V2):** Módulo tipo Plug-in que audita de forma continua procesos inter-sistema y evalúa operaciones manuales complementarias. Identifica desviaciones métricas, riesgos operativos y oportunidades normativa, autogenerando "hallazgos y recomendaciones accionables" sin intervención humana, asegurando la calidad y el cumplimiento normativo (Compliance-as-a-Service).

## 5. Backlog Management (Épicas V1)

### Épica 1: Bandeja de Tareas Unificada (Unified Inbox & Workbenches)
*   **US 1.1:** *Como usuario*, quiero ver una lista consolidada de mis tareas y proyectos ordenados por SLA.
*   **US 1.2:** *Como sistema (ABAC)*, debo filtrar las tareas basándome en los permisos del usuario final.

### Épica 2: Orquestación, Reglas e IA
*   **US 2.1:** *Como Administrador*, quiero subir versiones de modelos BPMN/DMN desde una interfaz web sin despliegues técnicos.
*   **US 2.2:** *Como usuario*, quiero escribir en lenguaje natural una nueva política de riesgos y que la IA la traduzca a DMN (IA Natural Flow).

### Épica 3: Captura Inbound y Generación Documental
*   **US 3.1:** *Como usuario*, quiero usar un Plugin de O365 para convertir un correo entrante en un proceso formal.
*   **US 3.2:** *Como sistema*, al aprobar un expediente, quiero generar un PDF contractual y enviarlo por correo directamente desde el WorkBench.
