# Validación de Mercado: Módulo Vertical Legal Docketing (V1)

**Rol Análisis:** Product Owner / Strategy
**Contexto:** Se ha realizado una investigación exhaustiva de las plataformas de *Legal Docketing* líderes en la industria (sistemas de gestión procesal y plazos legales) para validar que nuestra arquitectura base (V1) conectada con nuestro Módulo SaaS (V2) cubra u optimice el estándar del mercado.

---

## 1. Análisis de Funcionalidades Core (Mercado vs. Nuestro iBPMS)

Según la investigación de mercado, una plataforma de Docketing Legal estándar compite mediante las siguientes funcionalidades clave. A continuación se cruzan contra nuestro diseño:

### A. Calendario Basado en Reglas Judiciales (Rule-Based Calendaring)
*   **Estándar de Mercado:** El software tiene pre-cargadas las reglas (Leyes/Códigos) de cientos de cortes. Si el usuario ingresa "Notificación de Demanda", el software calcula automáticamente todas las fechas de audiencias posteriores según la ley aplicable (saltando festivos).
*   **Cobertura iBPMS (V1):** **Superamos el estándar.** En lugar de mantener una base de datos estática y costosa de todas las leyes del país (que obliga a actualizaciones de software constantes), nuestro *Modulo Docketing (Calculadora de Plazos NLP)* utiliza LLMs para extraer la regla directamente del contenido del correo o la orden del juez, inyectando el SLA calculado dinámicamente sobre la Tarea Unificada de la *Pantalla 1 (V1)*.

### B. Notificaciones y Centralización de Fechas (Reminders / Ticklers)
*   **Estándar de Mercado:** Enviar correos y generar pop-ups ("Ticklers") a los abogados 5, 3 y 1 día antes del vencimiento.
*   **Cobertura iBPMS (V1):** **Cubierto Nativo.** Esta es la naturaleza misma de nuestro Motor BPM (V1). Los "Timers" de Camunda y los SLAs definidos en las tareas en la *Pantalla 6* desencadenan los recordatorios e incluso escalamientos (re-asignar a un Gerente) automáticamente.

### C. Integración Automática de Correos y PACER (Court ECM)
*   **Estándar de Mercado:** Leer correos electrónicos y conectarse a sistemas judiciales (ej. PACER en EE. UU. o portales de la Rama Judicial en LatAm) para descargar recibos y fallos, evitando la digitación humana.
*   **Cobertura iBPMS (V2 SaaS Expansión Futura):** **Abordaje Superior (Agentic).** Nuestro *Módulo RPA Web Scraping Silencioso* está diseñado exactamente para este vacío. Navega autónomamente las páginas judiciales como un humano, hace el *scraping* del estado procesal, y la API dispara un evento Inbound en la *Pantalla 11*.

### D. Gestión Documental Acoplada al Caso
*   **Estándar de Mercado:** Todos los documentos y fallos notificados deben quedar almacenados en el mismo software que calcula los plazos, para prevenir pérdida de información.
*   **Cobertura iBPMS (V1):** **Cubierto Nativo.** Nuestra Bóveda Central SGDEA (Pantalla 12) vincula el Hash (SHA-256) del documento (el PDF de la demanda) directamente a la instancia del proceso (`Process_Instance`).

---

## 2. Gaps Funcionales y Recomendaciones para V1

Nuestra combinación de "Motor Transaccional (V1) + Agentes IA Cognitivos (V1)" no solo cubre el estándar de un software de Docketing actual, sino que lo moderniza eliminando el "Data Entry" casi al 100%.

Sin embargo, surgió un **Hallazgo de Usabilidad (UX)** crucial de la revisión del mercado que debemos agregar a nuestro Roadmap V2:

### ⚠️ Nuevo Requerimiento V1: "Vista de Calendario Legal Transversal"
*   **El Gap:** Nuestra V1 tiene un *Inbox Unificado* de Tareas en lista vertical (Pantalla 1) y Dashboards estadísticos (Pantalla 5). Pero los sistemas de Docketing puros obligan a tener una **Vista de Calendario Mensual Interactivo**. Los abogados piensan en meses/semanas visuales, no en listas Kanban para sus juicios.
*   **Acción Recomendada:** Actualizar la especificación de nuestro **Roadmap V1** para incluir un componente "Global Docket Calendar" en el Menú Principal. Esta vista consumirá la columna `due_date` de nuestra tabla `ibpms_task` (MySQL) y pintará, en un mes calendario interactivo, todos los vencimientos críticos de la firma en colores (Rojo = Litigio, Azul = Administrativo).

---
**Conclusión de Producto:**
La arquitectura ideada valida comercialmente. Con la adición de la *"Vista de Calendario"* en V1, el Módulo M365/Docketing de nuestro MVP V1 será altamente competitivo frente a softwares legados especializados.
