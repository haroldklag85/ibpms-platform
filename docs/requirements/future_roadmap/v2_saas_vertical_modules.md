# Definición Funcional: Módulos Verticales SaaS (Roadmap V2)

**Versión Objetivo:** iBPMS V2 (Expansión SaaS AI-Centric)
**Rol Análisis:** Product Owner / Strategy
**Contexto:** Mientras que la V1 se centra en el Motor Core (Workflow, Formularios, Tareas), la **Visión V2** es transformar la plataforma en un producto SaaS de alto valor ("Agentic Software"). Para lograr esto sin modificar el motor central, diseñaremos "Súper Módulos" o "Verticales de Industria" que se conectan al iBPMS como clientes API.

Este documento define funcionalmente los 3 Módulos Verticales hiper-especializados prioritarios para lograr el *Product-Market Fit* profundo en los sectores Legal y Hotelero.

*(Nota Estratégica: El Módulo "Copiloto AI para Buzones Corporativos M365" y las capacidades de "Gestión Avanzada Docketing" fueron movidos al MVP de la **Versión 1** por decisión de Producto, para asegurar que la plataforma nazca ya como un producto "Agentic" IA-First).*

---

## 1. Módulo Vertical LegalTech: Asistente Jurídico (RAG + LLM)

**El Problema:** Los abogados gastan horas buscando jurisprudencia previa, cláusulas en contratos antiguos y redactando contestaciones repetitivas. El iBPMS V1 solo les permite aprobar tareas y generar PDFs fijos.
**La Solución V2:** Un módulo "Copiloto" incrustado en la vista de la tarea (Pantalla 2) que ha "leído" toda la Bóveda Documental (SGDEA).

### Funcionalidades Core:
*   **Búsqueda Semántica (RAG - Retrieval-Augmented Generation):** Permite al abogado abrir un chat en la tarea y preguntar: *"¿Qué dijimos en el contrato de confidencialidad de Banco Alpha en 2024 respecto a multas?"*. El módulo busca en el Vector Database, recupera el párrafo exacto y genera una respuesta inteligible citando el PDF original.
*   **Generador Dinámico de Cláusulas:** En lugar de plantillas estáticas (V1), el abogado pide: *"Redacta una cláusula de fuerza mayor adaptada para la pandemia"*. El Módulo LLM inyecta el texto generado en el Formulario JSON del caso.

---

## 2. Módulo Vertical LegalTech: RPA Web Scraping Silencioso

**El Problema:** La "Vigilancia Judicial". Para iniciar un proceso de cobranza o actualizar el estado de un caso, un paralegal debe entrar todos los días a las páginas web de la Rama Judicial del país, buscar un número de radicado y copiar los cambios a mano.
**La Solución V2:** Agentes automatizados que navegan la web por el humano.

### Funcionalidades Core:
*   **Monitoreo Transaccional Nocturno:** El módulo recibe la lista de radicados vivos desde el motor iBPMS. Cada madrugada, lanza *headless browsers* (Playwright/Puppeteer) que se loguean en los portales gubernamentales, extraen el texto de los nuevos estados judiciales (Scraping) y los convierten en JSON.
*   **Inyección a Procesos (Start/Update):** Si el Scraper detecta que un juez publicó un fallo, dispara automáticamente un Webhook (Pantalla 11) que Inicia el Subproceso "Apelación de Fallo", asignando de inmediato la tarea al abogado líder a las 7:00 AM.

---

## 3. Módulo Vertical HospitalityTech: Captura Desatendida (OCR/ICR)

**El Problema:** En el Front-Desk de un hotel, los recepcionistas pierden demasiado tiempo digitando a mano los datos del Pasaporte, ID o Tarjeta del cliente en los Formularios Dinámicos. Esto rompe la experiencia de bienvenida.
**La Solución V2:** Automatización de entrada de datos visual.

### Funcionalidades Core:
*   **Extracción Estructurada desde Imagen:** El recepcionista simplemente le toma una foto al Pasaporte o a la Cédula con una Tablet conectada a la Pantalla 0. El módulo OCR (Optical Character Recognition) lee la imagen, el modelo ICR (Intelligent Character Recognition) extrae los campos clave (Nombre, Número, Fecha de Nacimiento) y **autocompleta** mágicamente el Formulario JSON de la Pantalla 2 (Check-in).
*   **Validación contra Listas Negras (Eventos Outbound):** Al sacar los datos del pasaporte en 1 segundo, el módulo envía los datos extraídos contra una API policial para confirmar identidad, todo antes de que el recepcionista salude al huésped formalmente.

---

---

### Resumen de Arquitectura SaaS (V2)
Ninguno de estos 3 módulos requiere modificar la tabla de Tareas Unificadas ni el Motor de Camunda de la V1. 

Operan bajo el principio de **Hexagonal Agnosticismo:**
Son microservicios API individuales o Aplicaciones Integradas (registradas en el Developer Portal / Pantalla 13) que usarán los permisos de sus `Client_Id` para:
1. Leer un evento del iBPMS.
2. Hacer su magia pesada de IA o Scraping en servidores propios con GPUs.
3. Actualizar la Tarea Unificada de la V1 con la información extraída a través del API Gateway.

Esto garantiza un modelo de negocio SaaS escalable donde "Se cobra el Motor Base (V1) + Suscripción Extra por Súper Módulo Vertical (V2)".
