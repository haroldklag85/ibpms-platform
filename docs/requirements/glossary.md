# Glosario de Términos del Dominio iBPMS
**Última actualización:** 2026-04-05

> [!NOTE]
> Este glosario unifica la terminología utilizada en todos los artefactos de requerimientos. Ante ambigüedad semántica, **este documento prevalece** sobre cualquier uso informal en US, CAs o documentación técnica.

---

## A

| Término | Definición | Contexto de uso |
|---|---|---|
| **Actividad** | Unidad de trabajo modelada dentro de un proceso BPMN (User Task, Service Task, Send Task). Puede ser humana o automática. | Diseñador BPMN (Pantalla 6) |
| **Agente IA** | Componente de software autónomo con un rol especializado (Backend, Frontend, QA, Orquestador) que ejecuta tareas dentro de los límites de su contexto inyectado. | US-052, US-053 |
| **Antigravity** | Nombre del ecosistema de desarrollo multi-agente IA. También nombre comercial de la plataforma y metodología de trabajo del equipo. | Transversal |
| **API Key** | Credencial criptográfica que permite a sistemas externos autenticarse contra las APIs del iBPMS. Se muestra una sola vez al crearla y se almacena hasheada (SHA-256). | US-042 (DevPortal) |
| **Archivado (Archiving)** | Proceso automático que mueve registros históricos (>12 meses) de tablas operacionales a almacenamiento frío (Cold Storage) para evitar degradación de rendimiento. | NFR-PER-03, US-017 |
| **Auto-Claim** | Mecanismo que asigna automáticamente una tarea al primer usuario que la visualiza si es el único candidato elegible. | US-002 |

## B

| Término | Definición | Contexto de uso |
|---|---|---|
| **BAM (Business Activity Monitoring)** | Dashboard que muestra métricas en tiempo real sobre la salud y rendimiento de los procesos de negocio. | US-009, US-018 (Pantalla 5) |
| **Bandeja / Workdesk** | Vista consolidada donde un usuario ve todas sus tareas pendientes, agrupadas por tipo (BPMN, Kanban, Gantt) con priorización SLA. | US-001 (Pantalla 1) |
| **BPMN** | Business Process Model and Notation. Estándar internacional para modelar procesos de negocio visualmente. | US-005, Pantalla 6 |
| **Borrador (Draft)** | Estado temporal de un formulario que aún no ha sido enviado al Backend. Se persiste en LocalStorage y opcionalmente en servidor con TTL de 72h. | US-017, US-029 |

## C

| Término | Definición | Contexto de uso |
|---|---|---|
| **CA / Criterio de Aceptación** | Condición verificable en formato Gherkin (Given/When/Then) que define cuándo una US se considera terminada. | Transversal |
| **Camunda** | Motor BPMN de código abierto que ejecuta los procesos de negocio. V1 usa Camunda 7 embebido (Java). V2 migrará a Zeebe. | Arquitectura Core |
| **Caso / Expediente** | Instancia viva de un proceso de negocio. Un caso tiene un ID único, un estado, tareas asociadas y documentos vinculados. Sinónimo: "Process Instance". | US-022, US-024, US-026 |
| **CIAM** | Customer Identity and Access Management. Gestión de identidades para usuarios externos (clientes). | US-050 |
| **Claim (Reclamar)** | Acción por la cual un usuario se asigna una tarea de grupo a sí mismo, convirtiéndose en su propietario. | US-002 |
| **CQRS** | Command Query Responsibility Segregation. Patrón que separa las operaciones de escritura (Commands) de las de lectura (Queries) en capas independientes. | US-017 |

## D

| Término | Definición | Contexto de uso |
|---|---|---|
| **Delegación** | Transferencia temporal de permisos de un usuario a otro, con fecha de expiración y restricción de alcance. | US-036 |
| **DevPortal** | Portal de Desarrolladores donde se gestionan API Keys, se registran módulos externos y se consulta documentación técnica. | US-042 (Pantalla 13) |
| **DLQ (Dead Letter Queue)** | Cola especial de RabbitMQ donde llegan los mensajes que fallan todos los reintentos. Requieren intervención manual. | US-034 |
| **DMN** | Decision Model and Notation. Estándar para modelar reglas de negocio como tablas de decisión ejecutables. | US-007 |
| **Docketing** | Bandeja avanzada de alto volumen para equipos SAC (Servicio al Cliente). Soporta filtrado transversal multi-campo. | US-011 (Pantalla 1B) |

## E

| Término | Definición | Contexto de uso |
|---|---|---|
| **EntraID (Azure AD)** | Servicio de identidad de Microsoft para autenticación empresarial. El iBPMS sincroniza roles desde EntraID. | US-038 |
| **Épica** | Agrupación funcional de Historias de Usuario que comparten un objetivo de negocio común. El iBPMS tiene 17 épicas (0-17). | Scope Master |
| **Event Sourcing** | Patrón de persistencia donde cada cambio se almacena como un evento inmutable en lugar de sobrescribir el estado actual. | US-017 |

## F

| Término | Definición | Contexto de uso |
|---|---|---|
| **FOUC** | Flash of Unstyled/Unauthorized Content. Parpadeo visual que ocurre cuando la UI muestra elementos antes de validar permisos. | US-051 |
| **Formulario iForm Maestro** | Formulario complejo multi-etapa que soporta múltiples roles, validaciones condicionales y campos dinámicos por fase del proceso. | US-003 |
| **Formulario Simple** | Formulario de una sola etapa con campos estáticos. No varía por rol ni fase. | US-003 |

## G - H

| Término | Definición | Contexto de uso |
|---|---|---|
| **Gaslighting 404** | Técnica de seguridad: cuando un usuario sin permisos adivina una URL protegida, el sistema muestra "Página no encontrada" (404) en lugar de "Acceso denegado" (403) para no confirmar la existencia de la ruta. | US-051 |
| **Health Check** | Endpoint que valida la conectividad y estado de los componentes internos (BD, Camunda, RabbitMQ). | NFR-OBS-03 |

## I

| Término | Definición | Contexto de uso |
|---|---|---|
| **IdP (Identity Provider)** | Proveedor de identidad. Puede ser externo (EntraID) o interno (módulo propio del iBPMS). | US-038, US-048 |
| **iForm** | Nombre comercial del sistema de formularios del iBPMS. Incluye IDE pro-code, simulador Zod y motor de renderizado. | Épica 2 |
| **Intake** | Proceso de recepción y triaje de solicitudes entrantes (correos, formularios web) antes de convertirlas en casos formales. | US-040 |

## K - L

| Término | Definición | Contexto de uso |
|---|---|---|
| **Kanban** | Metodología ágil basada en tableros visuales con columnas que representan estados. | US-008, US-030 (Pantalla 3) |
| **Lane** | Carril horizontal en un diagrama BPMN que representa un rol o participante del proceso. | US-005, US-036 |

## M

| Término | Definición | Contexto de uso |
|---|---|---|
| **Magic Link** | URL temporal y única enviada por correo para que un cliente externo cree su cuenta sin registro público. | US-050 |
| **MLOps** | Prácticas de operaciones para modelos de Machine Learning: entrenamiento, despliegue, monitoreo y re-entrenamiento. | US-015, US-044 |
| **MoSCoW** | Método de priorización: Must Have, Should Have, Could Have, Won't Have. | Scope Master, v1_moscow_scope_validation.md |

## N - O

| Término | Definición | Contexto de uso |
|---|---|---|
| **Notificación Outbound** | Correo electrónico o alerta enviada desde el iBPMS hacia clientes o usuarios internos mediante plantillas dinámicas. | US-049 |
| **Operario / Analista** | Usuario que ejecuta tareas operativas: reclama tareas, llena formularios, cambia estados Kanban. | Múltiples US |

## P

| Término | Definición | Contexto de uso |
|---|---|---|
| **Pantalla** | Referencia a un wireframe/mockup del diseño UX. Numeradas del 1 al 19 + Portal B2C + Command Center. | Transversal |
| **PII** | Personally Identifiable Information. Datos sensibles que requieren cifrado y enmascaramiento visual. | US-000, NFR-SEC-02 |
| **Proceso** | Modelo BPMN desplegado en Camunda que define el flujo de trabajo de inicio a fin. | US-005 |
| **Proyecto** | Contenedor que agrupa procesos, sprints y tareas bajo una plantilla WBS. Puede ser Ágil o Tradicional. | US-006, US-030, US-031 |

## R

| Término | Definición | Contexto de uso |
|---|---|---|
| **RAG** | Retrieval-Augmented Generation. Técnica de IA que combina búsqueda en documentos con generación de texto por LLM. | US-032 |
| **RabbitMQ** | Broker de mensajería (Message Queue) que desacopla componentes del sistema mediante colas. | US-034 |
| **RBAC** | Role-Based Access Control. Modelo de seguridad donde los permisos se asignan a roles, y los roles a usuarios. | US-036 (Pantalla 14) |
| **Rollback Compensatorio** | En Event Sourcing, proceso de crear un evento inverso para anular una transacción fallida, sin borrar el evento original. | US-017 |

## S

| Término | Definición | Contexto de uso |
|---|---|---|
| **SAC** | Servicio de Atención al Cliente. Área operativa que gestiona correos entrantes y reclamos. | Épica 9, US-016 |
| **Saga** | Patrón de transacciones distribuidas que coordina operaciones entre múltiples servicios con compensación en caso de fallo. | US-017 |
| **SGDEA** | Sistema de Gestión de Documentos Electrónicos de Archivo. Gestión documental con hash SHA-256 e inmutabilidad. | US-010, US-035 |
| **SLA** | Service Level Agreement. Tiempo máximo contractual para completar una tarea o resolver un caso. | US-043 (Pantalla 19) |
| **Scope Master** | Documento de gobierno que mapea cada US con su clasificación MoSCoW, estado y pantalla asociada. | scope_master_v1.md |
| **Sudo Mode** | Re-autenticación obligatoria antes de ejecutar acciones destructivas (purgar BD, eliminar tenant). | US-051 |

## T

| Término | Definición | Contexto de uso |
|---|---|---|
| **Tarea** | Unidad de trabajo asignada a un usuario. Puede originarse en BPMN (User Task), Kanban (Card) o Gantt (Actividad). | US-001 |
| **Tenant** | Organización cliente que usa la plataforma. El modelo actual es single-tenant; V2 será multi-tenant. | Arquitectura |
| **TOC** | Table of Contents. Índice navegable. | v1_user_stories.md |

## U - W

| Término | Definición | Contexto de uso |
|---|---|---|
| **Upload-First** | Patrón donde los archivos se suben a la bóveda documental ANTES de enviar el formulario, evitando timeouts y pérdida de datos. | US-029, US-035 |
| **WBS** | Work Breakdown Structure. Desglose jerárquico del trabajo de un proyecto en entregables y actividades. | US-006 (Pantalla 8) |
| **WebSocket** | Canal de comunicación bidireccional en tiempo real entre Backend y Frontend. Usado para notificaciones instantáneas (claim, revocación, SLA). | US-001, US-002, US-051 |
| **Workdesk** | Sinónimo de Bandeja. Pantalla 1 del iBPMS. | US-001 |

## Z

| Término | Definición | Contexto de uso |
|---|---|---|
| **Zero Trust** | Principio de seguridad: nunca confiar, siempre verificar. Aplica a Frontend (Router Guards), Backend (JWT re-validación) e infraestructura (mTLS). | NFR-SEC-01, US-051 |
| **Zod** | Librería TypeScript de validación de esquemas. Se usa isomórficamente: en Frontend (Vue) y transpilado a JSON Schema para Backend (Java). | US-003, US-028, US-029 |
