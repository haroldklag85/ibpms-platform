# Modelo C4 - Táctico (V1): Plataforma iBPMS (PoC)

Este documento contiene la representación de la Arquitectura **V1 (Estado Actual Táctico)**, la cual asume las actuales limitaciones de infraestructura (Azure VMs cerradas, acoplamiento transaccional y dependencia estricta de MySQL Relacional).

## Nivel 1: Diagrama de Contexto (System Context V1)

Muestra los actores que interactúan con la plataforma bajo el esquema de integración inicial.

```mermaid
C4Context
    title Diagrama de Contexto (V1) - Plataforma iBPMS Táctica
    
    Person(usuario_negocio, "Usuario de Negocio", "Procesa tareas en bandeja. Consulta Dashboards BAM.")
    Person(admin_reglas, "Funcional / Admin", "Configura flujos (BPMN/DMN) y reglas en NLP.")
    
    System(ibpms, "Plataforma iBPMS (V1 Monolítica)", "Orquestador unificado sobre Azure VMs.")
    
    System_Ext(plugin_o365, "Plugin Office 365", "Web Add-in nativo en el correo del usuario.")
    System_Ext(o365_graph, "Microsoft Graph / Webhooks", "Push asíncrono y API (Lectura/Escritura).")
    System_Ext(erp, "ERP Local", "Sistema corporativo transaccional (SOAP/REST).")
    System_Ext(crm, "CRM (Customer Rel. Mgmt)", "Datos de clientes para cruce de IA.")
    System_Ext(iam, "Proveedor de Identidad", "Directorio Activo / VPN.")

    Rel(usuario_negocio, ibpms, "Pull Bandeja / BAM", "HTTPS (WAF)")
    Rel(usuario_negocio, plugin_o365, "Convierte correos a casos", "Clic")
    Rel(plugin_o365, ibpms, "Inicia Procesos y Adjuntos", "REST/HTTPS")
    Rel(admin_reglas, ibpms, "Dicta reglas IA / Sube BPMN", "HTTPS")
    Rel(o365_graph, ibpms, "Avisa llegada de correo", "Webhook Push")
    Rel(ibpms, o365_graph, "Gestiona borradores y mails", "REST/Graph API")
    Rel(ibpms, erp, "Ejecuta Transacciones síncronas", "REST/SOAP")
    Rel(ibpms, crm, "Consulta vista 360 cliente", "REST")
    Rel_U(ibpms, iam, "Autentica usuarios", "SAML")
```

## Nivel 2: Diagrama de Contenedores (Container Diagram V1)

Abre la iBPMS mostrando el monolito transaccional obligado por el uso del motor Camunda 7 sobre MySQL.

```mermaid
C4Container
    title Diagrama de Contenedores (V1) - Limitado a VMs y MySQL
    
    Person(usuario, "Usuarios (Vía WAF/VPN)", "Acceso seguro.")
    Container(plugin_o365, "Plugin Outlook", "Vue/JS", "Iframe dentro de Outlook.")
    
    System_Boundary(c1, "VNet Azure (QA/Prod) - VMs IaaS") {
        Container(apim, "Azure APIM", "Gateway Facade", "Punto de entrada único.")
        Container(webapp, "Frontend SPA", "Vue 3 / Vite", "Bandejas y UI Principal.")
        Container(grafana, "Dashboard BAM", "Grafana", "Embebido por iframe en Frontend para analítica en vivo.")
        
        System_Boundary(be_vm, "Backend Monolítico (Spring Boot 3)") {
            Container(backend, "API Backend Core", "REST/Java", "Lógica, reglas y webhooks receptores.")
            Container(engine, "Motor BPM/DMN Empotrado", "Camunda 7 (.jar)", "Motor acoplado.")
            Container(doc_gen, "Generador Docs Oficiales", "FOP/PDFBox (.jar)", "Embebido. Combina Templates + Data.")
        }
        
        Container(llm_local, "Motor IA Perimetral", "Llama 3 / vLLM", "Hospedado en VM Privada GPU/CPU.")
        
        ContainerDb(db, "Base de Datos Consolidada", "MySQL 8", "Operativa y Estado BPM.")
        ContainerDb(blob, "Almacenamiento Discos", "Azure Managed Disks", "Bóveda física.")
    }
    
    System_Ext(o365_graph, "MS Graph", "Webhooks y REST API.")
    System_Ext(crm_sys, "CRM Corporativo", "REST API.")
    
    Rel(usuario, apim, "Solicita vistas", "HTTPS")
    Rel(plugin_o365, apim, "Comandos Push", "HTTPS")
    Rel(apim, webapp, "Sirve UI", "HTTPS")
    Rel(webapp, apim, "Consume APIs", "JSON/HTTPS")
    Rel(webapp, grafana, "Carga Iframe", "HTTPS")
    Rel(apim, backend, "Enruta peticiones y Webhooks", "HTTPS (TLS 1.2+ Interno)")
    
    Rel(backend, engine, "Integra Tareas", "Memoria Java API")
    Rel(backend, doc_gen, "Ordena fabricar PDF", "Memoria")
    Rel(doc_gen, blob, "Salva Original Legal (Hash)", "Java IO")
    Rel(backend, llm_local, "Traduce NLP / Infiriendo Intents", "HTTPS Interno")
    
    Rel(o365_graph, apim, "Push Webhook Nuevo Correo", "HTTPS")
    Rel(backend, o365_graph, "Crea Drafts / Envía Mail", "HTTPS (Graph API)")
    Rel(backend, crm_sys, "Pide Perfil Cliente", "HTTPS")
    
    Rel(engine, db, "Escribe Estado (JDBC)", "TCP/3306 (TLS)")
    Rel(grafana, db, "Consulta Panel Analítico", "TCP/3306")
    Rel(backend, db, "Escribe Datos Negocio", "TCP/3306 (TLS)")
```

## Nivel 3: Diagrama de Componentes Lógicos (Software Design View V1)

Demuestra cómo, a pesar de las limitaciones de V1, el backend empotrado y monolítico se protege internamente usando **Arquitectura Hexagonal**.

```mermaid
C4Component
    title Nivel 3 - Diseño de Software Interno (Arquitectura Hexagonal V1 - Spring Boot)
    
    Container_Boundary(api_app, "Microservicio Spring Boot Monolítico") {
        System_Boundary(puertos_entrada, "Driving Adapters") {
            Component(rest_ctrl, "REST Controllers", "Spring Web", "@RestController")
            Component(webhook_ctrl, "O365 Webhook", "Spring MVC", "Recibe push MS Graph")
        }

        System_Boundary(aplicacion, "Application UseCases") {
            Component(cm_usecase, "CaseManagement UseCase", "Interface", "IniciarCaso()")
            Component(auth_usecase, "Security Policy (ABAC)", "Interface", "Valida matrices locales.")
            Component(rule_usecase, "Rule Builder IA", "Interface", "Genera DMN.")
            Component(ff_usecase, "Feature Flags Service", "Interface", "Evalúa Toggles Multi-tenant.")
            Component(tx_manager, "Shared Transaction Manager", "Spring PlatformTx", "Controlador ACID Atómico.")
        }

        System_Boundary(dominio, "Dominio Core") {
            Component(expediente, "Expediente / Proyecto", "Java Pojo", "Negocio inmutable")
        }

        System_Boundary(puertos_salida, "Driven Adapters") {
            Component(camunda_adapter, "Camunda 7 API", "Java API", "Usa RuntimeService local")
            Component(ai_adapter, "Llama 3 Local Adapter", "REST API", "Inferencia de Texto/Reglas")
            Component(doc_adapter, "Template Renderer (.jar)", "Apache FOP", "Fabricante de PDFs Jurídicos")
            Component(jpa_adapter, "MySQL JPA Repositories", "Spring Data", "Base de datos")
            Component(erp_adapter, "ERP Connector", "Feign Client", "Gatillos a Legacy")
            Component(crm_adapter, "CRM Outbound Port", "Feign Client", "Consumo perfil 360")
            Component(graph_adapter, "Graph API Client", "MS SDK", "Crea/Lee correos M365")
        }
    }

    Rel(rest_ctrl, cm_usecase, "Ejecuta Case CRUD", "Interface")
    Rel(rest_ctrl, auth_usecase, "Valida Zero-Trust JWT", "Interface")
    Rel(rest_ctrl, rule_usecase, "Solicita regla verbal", "Interface")
    Rel(rest_ctrl, ff_usecase, "Consulta Enabled Features", "Interface")
    Rel(webhook_ctrl, cm_usecase, "Dispara auto-caso", "Interface")
    
    Rel(cm_usecase, expediente, "Modela", "Pojo")
    
    Rel(tx_manager, camunda_adapter, "Coordina Commit/Rollback", "Transacción Proxy")
    Rel(tx_manager, jpa_adapter, "Coordina Commit/Rollback", "Transacción Proxy")
    
    Rel(ff_usecase, jpa_adapter, "Lee Toggles V1", "Query")
    Rel(camunda_adapter, cm_usecase, "Implementa Workflow", "DI")
    Rel(ai_adapter, cm_usecase, "Infiriendo intenciones/datos", "DI")
    Rel(ai_adapter, rule_usecase, "Implementa Cerebro NLP a DMN", "DI")
    Rel(doc_adapter, cm_usecase, "Imprime Contratos/Cartas", "DI")
    Rel(jpa_adapter, auth_usecase, "Carga Matrices ABAC", "Spring Data")
    Rel(erp_adapter, cm_usecase, "Sincroniza External", "DI")
    Rel(crm_adapter, cm_usecase, "Enriquece expediente", "DI")
    Rel(graph_adapter, cm_usecase, "Envía Drafts al Usuario", "DI")
```
