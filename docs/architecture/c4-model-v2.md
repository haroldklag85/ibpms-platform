# Modelo C4 - Estratégico (V2): Plataforma iBPMS (AI-Centric / Agentic Workflows)

Este documento contiene la representación de la Arquitectura **V2 (Estado Futuro Estratégico)**, asumiendo su madurez y migración completa hacia un modelo SaaS Multitenant desplegado sobre Kubernetes, apoyado por el **Patrón Strangler** introducido en V1. El diseño abandona el BPMN rígido en favor de un **Sistema Multi-Agente** basado en resolución de Intents y RAG (Retrieval-Augmented Generation).

## Nivel 1: Diagrama de Contexto (System Context V2)

```mermaid
C4Context
    title Diagrama de Contexto (V2) - SaaS Agentic Pleno
    
    Person(usuario_negocio, "Usuario de Negocio", "Procesa tareas en bandeja / Consulta BAM / Interactúa vía Chat.")
    Person(admin_reglas, "Analista / Admin", "Genera reglas con NLP y monitorea Agentes.")
    
    System(ibpms, "Plataforma AI-Centric V2", "Enruta intents y ejecuta flujos ad-hoc mediante Sistema Multi-Agente.")
    
    System_Ext(chatbots, "Zero-UI / Chatbots", "Slack, MS Teams, Asistentes Virtuales (Ejecución Conversacional).")
    System_Ext(plugin_o365, "Office 365 Web Add-in", "Convierte correos en Push interactivo.")
    System_Ext(azure_openai, "Fundational LLM", "Azure OpenAI / Claude (Razonamiento en tiempo real).")
    System_Ext(whatsapp_api, "WhatsApp Business API", "Notificaciones y resoluciones Omnicanal.")
    System_Ext(erp, "Ecosistema ERP/Bancos", "APIs de microservicios transaccionales.")
    System_Ext(iam, "Identity Provider Federado", "Azure AD / Okta (SAML/OIDC).")
    System_Ext(fuentes_externas, "Fuentes Oficiales / Juzgados", "Páginas o edictos extraídos vía Web Scraping (Sector Legal).")
    System_Ext(app_config, "Azure App Configuration", "SaaS Feature Flags Tenant Control.")

    Rel(usuario_negocio, ibpms, "Single Page App / Docketing", "HTTPS (WAF)")
    Rel(chatbots, ibpms, "Inyecta Intents / Conversa", "Webhooks / HTTPS")
    Rel(usuario_negocio, plugin_o365, "Bandeja embebida O365", "HTTPS")
    Rel(plugin_o365, ibpms, "Interactúa y asocia Input", "REST")
    Rel(ibpms, azure_openai, "Traduce Texto / RAG Documental", "Síncrono/HTTPS")
    Rel(ibpms, app_config, "Pull Feature Flags", "REST/Auth")
    Rel(ibpms, fuentes_externas, "Scraping Silencioso Diario", "HTTPS Pull")
    Rel(ibpms, whatsapp_api, "Zero-UI Push Actionable", "HTTPS/Webhook")
    Rel(ibpms, erp, "Sincroniza y compensa por Sagas", "REST/gRPC")
    Rel_U(ibpms, iam, "Valida JWT y Roles Base", "OIDC")
```

## Nivel 2: Diagrama de Contenedores (Container Diagram V2)

Abre la plataforma AI-Centric mostrando su partición en microservicios, Agentes Activos y Base de Datos Vectorial/Grafos.

```mermaid
C4Container
    title Diagrama de Contenedores (V2) - Multi-Agent System on AKS
    
    Person(usuario, "Usuarios Seguros (JWT)", "Acceso vía Web/Móvil/Chat.")
    Container(chatbots, "Zero-UI Gateway", "Slack/Teams", "Canales conversacionales.")
    
    System_Boundary(c1, "Klúster Kubernetes (AKS) - Service Mesh mTLS") {
        Container(apim, "Azure API Facade", "API Management", "Valida JWT en borde e inyecta Claims.")
        Container(webapp, "Micro-Frontends (MFE)", "Vite / Vue 3", "Rendereador base Lego.")
        Container(grafana, "Dashboards y BAM", "Grafana", "Servidor analítico Iframeado.")
        
        System_Boundary(ms_layer, "Layer de Microservicios Core") {
            Container(backend, "Command Service", "Spring Boot", "Decodifica Sagas, y enruta Autenticación.")
            Container(query_service, "Query Service (CQRS)", "GraphQL", "Sirve lecturas masivas para Bandejas.")
            Container(conversational_adapter, "Conversational Adapter", "Node.js", "Recibe webhooks de chat y los traduce a Intents.")
            Container(dmn, "DaaS (Motor Decisiones)", "SaaS Remoto", "Decisiones rápidas parametrizadas.")
            Container(ai_auditor, "Auditor AI (Plug-in)", "Python/ML", "Audita continuamente procesos buscando Riesgo (ISO9001).")
            Container(ai_consultant, "Consultor AI Service", "Python/ML", "Detecta fricciones históricas y rediseña procesos.")
        }

        System_Boundary(vertical_saas, "Módulos Verticales Comerciales (Super Apps)") {
            Container(rpa_scraper, "Silent RPA / OCR Scraper", "Python", "Crawler de sentencias y OCR de facturas Hoteleras.")
        }

        Container(broker, "Mensajería Core", "Apache Kafka", "Broker central Event-Driven.")
        Container(engine, "Multi-Agent Orchestrator", "LangChain/Semantic Kernel", "Reemplaza a Zeebe. Motor central basado en razonamiento e intentos.")
        
        ContainerDb(db_graph, "Agentic Graph DB", "Neo4j / CosmosGremlin", "Almacena grafos de conocimiento corporativo (RAG).")
        ContainerDb(db_read, "Indexed Query DB", "ElasticSearch", "Copia proyectada para Bandejas.")
        ContainerDb(db_write, "Business Data DB", "PostgreSQL", "Guarda el modelo de entidades base.")
    }
    
    System_Ext(azure_openai, "Foundational LLM", "Azure OpenAI")
    System_Ext(whatsapp_api, "WhatsApp API", "Webhooks Meta")
    System_Ext(fuentes_externas, "Juzgados / Entidades", "Públicas")
    System_Ext(app_config, "Feature Config", "Azure App Configuration")
    
    Rel(usuario, apim, "Solicita vistas / APIs", "HTTPS")
    Rel(chatbots, apim, "Chatea Intents", "HTTPS")
    Rel(rpa_scraper, fuentes_externas, "Realiza Web Scraping", "HTTPS")
    Rel(rpa_scraper, broker, "Encola Evento o Extracción OCR", "Kafka Proto")
    Rel(apim, conversational_adapter, "Enruta chats", "HTTPS")
    Rel(conversational_adapter, broker, "Encola Intento de Usuario", "Kafka Proto")
    Rel(apim, backend, "Enruta Comandos CRUD", "gRPC")
    Rel(backend, app_config, "Carga Feature Flags Live", "HTTPS")
    Rel(apim, query_service, "Enruta Lecturas", "REST")
    
    Rel(backend, broker, "Publica eventos CRUD", "Kafka Proto")
    Rel(broker, engine, "Consume Topicos (Jobs/Intents)", "gRPC")
    
    Rel(engine, azure_openai, "Infiere Siguiente Paso", "HTTPS (Prompt)")
    Rel(engine, db_graph, "Inquiere Contexto/RAG", "Cypher/Gremlin")
    Rel(ai_auditor, db_read, "Inspecciona Histórico Logs", "SQL/API")
    Rel(ai_consultant, db_read, "Entrena Patrones de Eficiencia", "API")
    
    Rel(engine, db_write, "Aplica Estado Acordado", "JDBC")
    Rel(engine, db_read, "Exporta Log Histórico", "gRPC")
    Rel(engine, dmn, "Consulta reglas fijas", "REST/Síncrono")
```

---

## Decisiones Arquitectónicas Justificadas (ADR Addendum)

Las siguientes ambigüedades funcionales de la V1 fueron resueltas para este diseño Cloud-Native (V2):

### 1. Motor DMN: ¿Empotrado (.jar) vs DaaS (Microservicio Síncrono Libre)?
En la Fase V1 se usa **empotrado** dentro de Spring Boot ("`.jar`") para simplificar la infraestructura monolítica (una sola VM levanta todo). Sin embargo, en la V2, la separación como **DaaS (Decisions-as-a-Service)** expuesta como un pod de Kubernetes trae múltiples **beneficios de escalamiento asimétrico**. 
*   **Justificación:** Un simulador de tarjetas de crédito o "Tabulador de Riesgos" complejo requiere miles de transacciones por segundo al motor DMN que *no tocan* el estado de un de Workflow, por ello escalar el `DmnContainer` a 15 réplicas (mientra el Motor de Flujo de Tareas se queda en 2 réplicas) ahorra inmensamente los costos de CPU/RAM en la nube.

### 2. Manejo de Roles Complejo (ABAC) ¿JWT Front vs Spring Boot DB?
*   **Problema:** El "Permiso por Rol" (RBAC - Ej. `AdminRole`) es muy pequeño y viaja fácilmente en el payload de un token JWT al hacer *Log-in* a la aplicación. Sin embargo, el **ABAC** promete permisos por Atributo de Entidad (Ej: "Solo puedo ver los expedientes asigandos al Sub-departamento Zona Sur con Riesgo Alto"). Ese nivel granular generaría un JWT de 5 Megabytes, provocando rechazo HTTP y violación de seguridad si el token se roba.
*   **Solución (V1/V2):** El Frontend usará un token JWT **liviano** filtrado por el APIM para saber de quién es la sesión general. Pese a ello, las llamadas REST exigirán un Componente especializado en Spring Boot (`AuthUseCase / PolicyEngine`) que cruzará la identidad del token (ID) contra las matrices relacionales locales y delegaciones en vivo dentro de la Base de Datos antes de pintar los resultados de la `Tasklist`, manteniendo así al 100% de la funcionalidad de *Case Management*.
