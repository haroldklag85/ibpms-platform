# Análisis Funcional Definitivo: US-049 (Motor Central de Notificaciones Outbox)

## 1. Resumen del Entendimiento
La US-049 establece la única entidad dentro de la Arquitectura autorizada y configurada para disparar correos transaccionales y Websockets. Utiliza plantillas de renderizado de backend libre de Harcoding y orquesta un asincronismo tipo The Outbox Pattern.

## 2. Objetivo Principal
Aislar el tiempo de respuesta frágil de MS Graph/SMTP (Lentitud de Internet) para que Camunda y los operarios no sufran latencias síncronas. Encolar los despachos y auditar de forma indudable el HTML escupido con propósitos legales.

## 3. Alcance Funcional Definido
**Inicia:** Recepción de Orden de Envío (Camunda Tarea / API Push Local).
**Termina:** El Worker de Rabbit confirma Cód 200 HTTP Smtp o arroja WebSocket local. Grabando Evidence Log `outbox_log`.

## 4. Lista de Funcionalidades Incluidas
- **Template Engine FrontendCRUD (CA-4346):** Combate código spagetti. El asunto / html vive en la Base de Datos bajo un motor Thymeleaf/Moustache, y usa delimitadores `{{var}}` dinámicos.
- **Worker Asíncrono Rabit Outbox (CA-4352):** Fire And Forget para Camunda Worker.
- **Resiliencia Exponente (CA-4359):** Auto-reintentos TCP (Exponential Backoff, 1m, 5m, 15m) + Dead Letter Queue de Emails Huérfanos.
- **Auditoría Forense Legal (CA-4366):** Persiste el HTML del Email como Prueba Reina legal asociada a la carpeta del caso en la Vista 360 y UI Workdesk.
- **Digest-Spam Avoidance (CA-4373):** Throttling Batching (Consolida 150 emails de quiebre en un sumario gerencial de 1 email cada 15 min).
- **Outbound Attachment UUID Link (CA-4388):** Acoplador automático a SharePoint/S3 (Lee Token UUID Archivo, baja memoria, Attach).
- **Campana Local in_app Websockets (CA-4397):** Campana Master Header reactiva sin f5, guardando `is_read = false`.

## 5. Lista de Brechas, Gaps o Ambigüedades Detectadas
- **Escenario Duplicado (CA-4379 y CA-4388):** Textualmente son copias clonadas referidas al "Outbound Attachments SGDEA". Un error tipográfico de documentación de los PMs/BAs. 
- **Suicidio de Memoria RAM por Arrays Multi-Anexo (⚠️ CA-4384/4393):** Instrucción literal dictamina: *"El Worker descargará los binarios (Ej: PDF)... **temporalmente a la memoria RAM**. Y luego lo destruirá tras el 200 OK"*. **GAP CATASTRÓFICO Out-Of-Memory (OOM)**: Si un envío agrupa 4 anexos PDF de arquitectura de 25MB cada uno, el worker alojará `100MB` en el Heap. Si en RabbitMQ hay 30 correos encolados por reintentos tras una caída, el Node/Spring Worker consumirá `3,000 MB` de RAM súbitamente. El Garbage Collector morirá, tirando el contenedor Docker a reinicio cíclico. Un Message Broker Worker **JAMÁS debe leer binarios gigantes a RAM Pura de aplicación (Buffer)**, sino enlazarse vía Injección Stream (Pumping the Stream directly to MS Graph Request sin tocar RAM completa) o descargarlos al Disco Transitorio Flash (`/tmp`) del OS. 

## 6. Lista de Exclusiones (Fuera de Alcance V1)
- Respuesta transaccional por Email In-Bound para aprobar tareas sin login (Parseo de bandeja Outlook a Camunda). La US-049 es estrictamente **Outbound (Salida)**.

## 7. Observaciones de Alineación o Riesgos
Excelente decisión en el *Auditor Forense Html* (Guarda la evidencia). Y la arquitectura del Throttling Digest es vital en SaaS Financieros para no bloquear el Domain Trust de MailGun/SendGrid ante fallos operativos.
