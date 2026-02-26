# Reporte de Auditoría Funcional vs Arquitectura C4 (Iteración 3: Escala Estratégica)

Como auditor de alineación, he ejecutado la validación estricta de los **9 nuevos escenarios y capacidades estratégicas** acordados en la actualización reciente contra los diagramas C4 (V1 y V2) existentes.

El objetivo es determinar qué elementos funcionales no están soportados visual o lógicamente en la arquitectura, para poder inyectarlos.

---

## Parte 1: Evaluación por Escenarios Estratégicos

### Escenario 1: Creación de Reglas por Lenguaje Natural (IA)
*   **A) L1 Contexto:** ⚠️ **GAP-L1**. No hay ningún sistema externo de **Inteligencia Artificial (LLM)** modelado interactuando con la plataforma (ni OpenAI, ni Azure AI, ni modelo local).
*   **B) L2 Contenedores:** ⚠️ **GAP-L2**. No se evidencia quién orquestará el "Prompting" seguro ni protegerá el flujo de AI.
*   **C) L3 Componentes:** ⚠️ **GAP-L3**. En el backend de Spring Boot no existe el adaptador de salida (`AILlmOutboundAdapter`) que traduzca el Lenguaje Natural al JSON estructurado de DMN.

### Escenario 2: Notificaciones Omnicanal (WhatsApp y Alertas Proactivas)
*   **A) L1 Contexto:** ⚠️ **GAP-L1**. El diagrama muestra O365, pero falta la frontera externa del Proveedor SMS/Mensajería (WhatsApp/Meta API/Twilio) al que enviaremos las alertas de SLAs de tareas.
*   **B) L2 Contenedores:** ⚠️ **GAP-L2**. Carecemos de un contenedor lógico o servicio interno tipo *Notification Gateway / Alarming Engine*.
*   **C) L3 Componentes:** ⚠️ **GAP-L3**. Faltan el `NotificationUseCase` y un adaptador de salida (ej. `TwilioAdapter` o `MetaGraphAdapter`) en el componente de Negocio.

### Escenario 3: Generador Documental Jurídico Automático
*   **A) L1 Contexto:** OK. El "Gestor Documental" es el receptor y está mapeado.
*   **B) L2 Contenedores:** ⚠️ **AMBIGÜEDAD**. Se muestra el "ECM Adaptador" para transferir binarios, pero ¿qué contenedor lógico "combina" el Word/HTML con el JSON del proceso para **fabricar/renderear** el contrato físico antes de guardarlo? ¿Está empotrado en el Backend Core o es un Microservicio de "Template Rendering" aparte?
*   **C) L3 Componentes:** ⚠️ **GAP-L3**. Solo hay un puerto `ECM Outbound Adapter` para escritura. Falta un adaptader de Generación (`DocumentTemplateEngineAdapter`), como XDocReport o Apache FOP dentro de los Driven Adapters.

### Escenario 4: Integración Office 365 Directa (Plugin / Web Add-in)
*   **A) L1 Contexto:** OK a medias. MS 365 existe y el Usuario existe, pero la interacción directa desde Outlook no se declara.
*   **B) L2 Contenedores:** ⚠️ **GAP-L2**. Un Plugin de Outlook/Office 365 es realmente una aplicación Web pequeña (HTML/JS) iframeada dentro del cliente de Office. En L2 solo tenemos una "SPA Vue 3". Falta el Contenedor "O365 Web Add-in" (el front-end desplegado para Outlook). 
*   **C) L3 Componentes:** OK. Los *REST Controllers* propuestos procesan la orden venga del Plugin o de la Web.

### Escenario 5: Analítica / Dashboards y Salud de Procesos (BAM)
*   **A) L1 Contexto:** OK. Los actores `Líder / Usuario` consumirán la info.
*   **B) L2 Contenedores:** ⚠️ **GAP-L2**. En V2 modelamos `ElasticSearch` como base de datos de lectura rápida "Indexed Query DB". Sin embargo, no hay ningún contenedor tipo "Visualization Engine" o "Business Intelligence Server" (Ej: Grafana, Kibana, o Superset) que renderice los Dashboards de manera nativa sin programar todo en Vue.
*   **C) L3 Componentes:** ⚠️ **GAP-L3**. Falta en el core de Spring Boot un `Analytics/MetricUseCase` para entregar reportes consolidados (aggregation queries) al front.

### Escenario 6: Gestión de Proyectos Histórica
*   **A) L1 Contexto:** OK. Se maneja al igual que los procesos regulares.
*   **B) L2 Contenedores:** OK. La BD relacional soportará el modelo de histórico.
*   **C) L3 Componentes:** ⚠️ **GAP-L3**. Solo modelamos las Entidades `Expediente` y `Tarea`. Al formalizar "Project Management", es obligatorio modelar el Agregado `Proyecto`, que contiene una lista de `Expedientes` y `Microtareas` en Arquitectura Hexagonal.

---

## Parte 2: Detección de SOBRANTES

1.  **Sobrante en L2 y L3 V1: `Inbound Poller (Java Scheduler)`**
    *   **¿Qué outcome soporta?**: Buscar correos periódicamente simulando un webhook, leyendo la bandeja.
    *   **Tensión:** Ahora que hemos pivotado funcionalmente a un "O365 Plugin" interactivo (Push humano) y usaremos Webhooks de Graph, un Poller es un paradigma legado, pesado (batch), incompatible con lo ágil, y propenso a errores en lectura de carpetas compartidas.
    *   **Recomendación:** Pregunto para certificar: ¿Reemplazamos, matamos e incineramos el *Poller* para delegar el 100% de la carga de "Crear Tareas desde Correo" interactiva y ágilmente a través de las pulsaciones en el Web Add-In y Graph Webhooks?

---

## Parte 3: PREGUNTAS CRÍTICAS (Ambigüedades a Resolver)

Para cerrar este nuevo ciclo estratégico en C4 (inyectando todos estos contenedores y subsistemas nuevos a los diagramas), responde las siguientes definiciones:

1.  **Motor de Inteligencia Artificial (IA):** Para la automatización del lenguaje natural, ¿Vamos a modelar una integración corporativa hacia **Azure OpenAI / ChatGPT Enterprise** (nube gestionada segura) o preferiremos un modelo perimetral (Ej: Llama 3 hospedado en VMs propias) por seguridad de información confidencial?
2.  **Motor Generador de Documentos:** Para crear los PDFs con "Validez Jurídica" combinando plantillas y datos, ¿Cargamos ese peso computacional embebido como una "Librería" (.jar tipo Apache PDFBox) directo en el Backend Monolítico Spring Boot, o creamos un pequeño Microservicio independiente dedicado solo al *Template Rendering* para no estresar el backend cuando un proceso masivo genere 40,000 cartas de despido a la vez?
3.  **Tecnología de Dashboards (BAM):** Dado que tenemos ElasticSearch en V2/V1. ¿Quieren modelar explícitamente **Kibana/Grafana** como el panel analítico oficial incrustado (Iframe) para los líderes, o desarrollamos pantallas personalizadas desde cero en Vue.js usando librerías de gráficos consumiendo consultas de la BD?
4.  **WhatsApp/Alertas (Proveedor Externo):** ¿Dibujo la integración saliente hacia **WhatsApp Business Cloud API (Meta directo)**, o ustedes suelen centralizar esto por un Broker tipo Twilio / MessageBird?
5.  **¿Matamos el Batch Poller?:** Como propuse en "Sobrantes", ¿puedo borrar el componente `O365 Poller / Scheduled Task` de V1, dado que usaremos el Plugin dinámico y Webhooks?
