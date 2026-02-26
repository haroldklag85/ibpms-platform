# Análisis de Gaps y Validación Funcional (Ampliación de Alcance)

He analizado a profundidad tus 7 comentarios y el nuevo "Objetivo Estratégico" transversal que has aportado. Este nuevo nivel de detalle es oro puro porque revela **dimensiones funcionales enteras que no estaban modeladas en la arquitectura C4 actual**, y clarifica cómo esperan los usuarios operar la plataforma.

A continuación, respondo a cada uno de tus planteamientos y determino el **GAP Arquitectónico** resultante.

---

### 1. Creación y Asociación de Formularios
> **User:** "que modulo crea formularios y lo asocia a un proceso"

*   **Análisis:** En nuestra V1 teníamos los "Micro-frontends" para renderizar formularios, pero no el módulo que los *construye* y *orquesta*. En la práctica, Camunda/Zeebe usan "FormKeys" (un ID que dice "para esta tarea, carga el formulario X").
*   **GAP Identificado (Falta Componente):** Necesitamos integrar a la plataforma un **Form Builder & Engine** (como Form.io o Camunda Forms) que permita armar JSON Schemas drag-and-drop. Este será un nuevo contenedor lógico en el Frontend y el Backend administrará el repositorio de estas definiciones.

### 2. Creación de Reglas con Lenguaje Natural (IA)
> **User:** "donde se especifica que las reglas de negocio, eventos, pueden ser creadas por el usuario final a traves de lenguaje natural y que por medio de IA esta pueda transformar en acciones a la plataforma"

*   **Análisis:** Lo habíamos catalogado como un simple "Roadmap" en el Plan original, pero tu requerimiento lo vuelve *Core*. Para transformar "Si el cliente es VIP, el descuento es 10%" en una tabla DMN (XML) ejecutable.
*   **GAP Identificado (Falta AI-Adapter):** En el diagrama C4 de Componentes (L3), nos falta un **AI Translator Adapter** (conectado vía API a OpenAI/Azure OpenAI, o un LLM propietario) que tome texto plano del frontend, lo pase a un *Prompt* estructurado, reciba el XML del DMN y lo inyecte directamente en el catálogo de reglas.

### 3. Desarrollo Técnico de Módulos (Extensibilidad)
> **User:** "en que componente funcional en el rol tecnico de desarrollo se puede construir basado en las funcionalidades de ibpms modulos mucho mas robustos y complejos que los realizados por bpm"

*   **Análisis:** El marco para lograr esto está en el Nivel L2 de nuestra arquitectura mediante el **API Backend Core** y el **Event Broker**. El desarrollador no toca el motor BPM; en su lugar, construye un Microservicio completamente nuevo (ej. `Módulo de Nómina`), lo engancha al *Kafka/Broker* o hace llamadas *REST/GraphQL API* a nuestro iBPMS para iniciar procesos o guardar en el SGDEA.
*   **Resolución en C4:** Esto justifica plenamente la **Arquitectura Hexagonal**. En el C4 actual esto se soporta mediante los *REST/GraphQL Controllers* y la publicación de nuestras APIs OpenAPI. Todo el iBPMS será consumible como un "Backend as a Service".

### 4. Resguardo y Generación de Documentos con Validez Jurídica
> **User:** "un proceso automatizado puede que requiera generar con todo su input un documento de valides juridica, que funcionalidad permite esto y ademas de resguardar en SGDEA"

*   **Análisis:** Actualmente solo teníamos el "SGDEA (Bóveda)" para guardar archivos que enviaba el usuario. Falta la máquina que ensambla contratos. Al final de un proceso de RR.HH., el sistema toma el JSON del empleado, lo inyecta en una plantilla Word/HTML y sella un PDF.
*   **GAP Identificado (Falta Document Engine):** C4 Nivel L2 y L3 deben incluir un **Document Template Engine** (Ej. XDocReport / Apache FOP / Docmosis). Este microservicio escucha el evento "Empleado Contratado", mezcla los datos con la plantilla oficial, genera el PDF, y directamente invoca al adaptador del SGDEA para guardarlo como un "Registro Oficial Inmutable", cumpliendo con Tablas de Retención.

### 5. El Nuevo Objetivo Estratégico General
Tus especificaciones de Proyectos, O365 Plugin, Alertas y Analítica expanden enormemente la plataforma. Generan los siguientes GAPS masivos contra nuestro diseño previo:

*   **GAP de Dominio (Gestión de Proyectos PMI vs Ágil):** Nuestro diseño solo contemplaba `Casos` y `Flujos (BPM)`. Agregar "Gestión de Proyectos" implica modelar nuevas entidades centrales en Java: `Proyecto`, `Cronograma`, `Dependencias`, `Microtareas (tipo Jira)`. El "Motor" debe usarse para mover estados del proyecto en sí.
*   **GAP Frontend de Integración (Plugin O365):** En el diseño actual teníamos un *Inbound Listener* backend que devoraba correos de forma automática. El nuevo requerimiento exige un **Office Web Add-in**. Un actor (Usuario) estará en su Outlook, hará clic en un botón del panel lateral derecho y dirá "Convertir este correo al Proyecto X". Es una interacción humana, de Frontend.
*   **GAP Omnicanalidad (WhatsApp/Alertas):** No había en C4 ningún motor de notificaciones salientes más allá del envío de correos por O365. Se requiere un **Notification Outbound Adapter** (Webhook hacia Twilio / Meta API para WhatsApp).
*   **GAP Analítica e Inteligencia de Negocios (BI):** Mencionas *Dashboards estratégicos y operativos*. Actualmente teníamos "ElasticSearch" abstracto. Requeriremos un contenedor visual claro en C4, como **Kibana / OpenSearch Dashboards / Apache Superset**, enlazado directamente a los datos de históricos para resolver esto sin carga a la BD Transaccional.

### 6. Integración SGDEA y Módulo de RR.HH.
> **User:** "En este caso, los flujos de pre-boarding... se gestionen bajo tablas de retención documental... no puedan ser alterados..."

*   **Análisis:** Validado y totalmente alineado. Esto cruza el GAP #4 (Document Generator) con nuestro `EcmOutboundAdapter`. Para que el documento tenga validez jurídica total, el adaptador que diseñaremos en Java no solo debe "guardar" el archivo físico, sino enviar metadata inmutable (Timestamp, Firma de Sistema, ID de Proceso, Serie Documental para aplicar la TRD).
*   **Resolución en Código (PoC):** En nuestra prueba de concepto, modelaremos el `Expediente` con un método `vincularRegistroOficial()` simulando el inmutabilidad legal.

---

### Siguientes Pasos
Todos estos requerimientos son **excelentes bases para un producto Enterprise**. Procederé a actualizar el documento oficial `functional_requirements.md` para inyectar este "Objetivo Estratégico" exactamente con tus términos (Dashboard, Kanban de Proyectos, Plugin O365, Formularios, WhatsApp, IA).

Luego de ello, estos **7 nuevos GAPS** que acabamos de descubrir deberán ser añadidos a los Diagramas C4 para que la arquitectura quede realmente alineada a tus ambiciones de negocio.
