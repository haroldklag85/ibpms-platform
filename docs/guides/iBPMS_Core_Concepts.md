# iBPMS V1: Guía de Conceptos Core y Ecosistema (Onboarding Manual)

Este documento es la **Guía Definitiva de Conceptos** diseñada para sincronizar la misma "arquitectura mental" entre Gerentes de Producto, Desarrolladores, Arquitectos y Agentes de IA que interactúen con la plataforma iBPMS (Intelligent Business Process Management System).

Para comprender cómo funciona la plataforma, es vital entender que construimos un ecosistema de **piezas de Lego (Formularios, Reglas, Eventos, Metadatos)** que se ensamblan sobre diferentes **estructuras (Procesos o Proyectos)**.

---

## 1. Módulos Estructurales (Las Pistas)

Existen tres formas fundamentales en las que el trabajo fluye en la plataforma. Elegir una u otra depende de la naturaleza del problema de negocio.

### A. Proceso (BPMN)
Un Proceso es una secuencia **predeterminada, repetitiva y altamente estructurada** de actividades que transforma una entrada en una salida bajo reglas estrictas de negocio.
*   **Analogía:** Las vías de un tren. El vehículo solo puede ir por donde el Arquitecto dibujó los rieles.
*   **Uso:** Flujos transaccionales inmutables (Aprobaciones de Crédito, Solicitudes de Vacaciones).
*   **¿Cómo funciona?** El Arquitecto "dibuja" el flujograma (BPMN 2.0). Cuando se instancia, el Motor empuja las tareas de una en una a las bandejas (Inbox) de los usuarios correctos. El ejecutor en la Tarea 1 no tiene por qué preocuparse de qué pasa en la Tarea 4.

### B. Proyecto Ágil (Kanban / Case Management)
Un Proyecto Ágil es un esfuerzo temporal y **no determinista**. No es repetitivo; el equipo descubre el camino a medida que avanza colaborativamente.
*   **Analogía:** Un viaje en auto por carretera abierta (Off-road). Tienes un destino, pero tú decides qué ruta tomar minuto a minuto.
*   **Uso:** Desarrollo de software, gestión de incidentes de TI, diseño de productos.
*   **¿Cómo funciona?** No hay un diagrama rígido. Las tareas nacen libremente en un "Backlog" y el equipo las mueve entre columnas (TODO -> DOING -> DONE) en la medida de su capacidad. 

### C. Proyecto Tradicional / Cascada (El Híbrido)
Un "Proyecto Tradicional / Waterfall" es un macro-esfuerzo que exige cumplimiento secuencial estricto de Fases inflexibles (Ej: No puedes pintar el techo si no has vaciado los cimientos).

> [!TIP]
> **¿Por qué usamos el Motor BPM para abstraer Proyectos Tradicionales?**
> A nivel técnico de arquitectura, **un Proyecto en Cascada es simplemente un Macro-Proceso**.
> En lugar de programar una "Máquina de Estados" compleja desde cero en BD (MySQL) para controlar que el proyecto no salte de la "Fase 1" a la "Fase 3", **disfrazamos el Proyecto.** El Arquitecto dibuja un diagrama BPMN muy simple donde cada "caja" representa una Fase entera de meses de duración. Cuando el Motor BPMN dicta que es momento de pasar a la Fase 2, el Proyecto salta ciegamente a la Fase 2 arrastrando todas sus tareas. Esto nos regala rigor secuencial, timers y eventos compensatorios "gratis" usando el poder matemático del Motor Core.

---

## 2. Los Componentes Operativos (Las Piezas de Lego)

Estas son las piezas dinámicas que se conectan (se "pegan") a las estructuras mencionadas arriba para darles inteligencia y evitar escribir código fuente repetitivo.

### A. Formularios Inteligentes (El "Recolector de Datos")
No son páginas web HTML estáticas ("Hardcoded"). Son **esquemas JSON dinámicos** renderizados al vuelo.
*   **El Concepto:** Su único propósito es capturar información de un humano (input) y convertirla en una variable estructurada (Ej: `{"monto_solicitado": 5000}`) que la computadora pueda leer matemáticamente.
*   **¿Cómo se acoplan?** Se le asocian directamente a las cajas del (BPMN) o a las tarjetas del (Kanban). El proceso/proyecto no puede continuar si el humano no ha llenado y validado el formulario específico de esa etapa.

### B. Reglas de Negocio Asistidas por IA (El "Cerebro")
Son decisiones lógicas escritas en tablas estándar (DMN) que reemplazan la programación engorrosa de bucles `IF/THEN`.
*   **El Concepto (AI NLP-to-DMN):** Para evitar la fricción técnica, el usuario de negocio redacta un enunciado (Prompt): *"Si el monto > $5,000, entonces el output es RECHAZO"*. La plataforma conecta vía API a una Inteligencia Artificial Generativa Comercial (Ej. Claude Opus) que modela matemáticamente el archivo XML/JSON de la tabla DMN sin intervención humana.
*   **¿Cómo se acoplan?**
    *   **En Procesos (BPMN):** Actúan como *Compuertas (Gateways)* lógicas. El motor inyecta el JSON del formulario contra la Regla DMN, y la respuesta ("APROBADO") bifurca la vía del tren automáticamente.
    *   **En Proyectos (Kanban):** Actúan como *Validadores de Cambio de Estado*. Si intento mover una tarjeta de DOING a DONE, el tablero rechaza la acción si el DMN determina que falta un documento.

### C. Eventos (El "Sistema Nervioso")
Son cosas que pasan en el entorno externo o interno que sirven como "gatillos" sin requerir la intervención humana en la interfaz.
*   **Eventos Entrantes (Inbound Webhooks/Timers):** Escuchan al mundo. Por ejemplo, el "Plugin de Outlook O365" es un disparador de Eventos Inbound. Al detectar un correo nuevo, envía la señal e instancía automáticamente un Proceso, o si pasan 24 horas (Timer), escala un Proyecto de urgente a crítico.
*   **Eventos Salientes (Outbound REST APIs):** Hablan con el mundo. Al llegar al final del Proceso/Proyecto, un Evento Final envía automáticamente el Payload JSON en un `POST` call silencioso al ERP (SAP/Oracle) para liquidar un pago y asentar los libros contables.

---

## 3. Metadatos (El Contexto de Negocio Inteligente)

Si los Proyectos/Procesos son el contenedor, y los Formularios capturan el dato en crudo, los **Metadatos** son la *etiqueta universal* que dota de sentido analítico al mar de información.

*   **El Concepto:** En un sistema con millones de registros, buscar en los *payloads* en bruto (el `body` de todos los JSONs de cada formulario) es un suicidio de rendimiento. Los **Metadatos de Negocio** (Ej. Cliente, Presupuesto, Tipo de Reclamo) se definen explícitamente y se **extraen** de los formularios o del cascarón del proyecto hacia una tabla indexada lateralmente (Llave-Valor) al momento de guardar.
*   **Trazabilidad Herencia (Cascade Context):**
    *   Nace el Proyecto "Implementar Sucursal" con Metadatos: `[Centro Costos: IT] [Prioridad: Alta]`.
    *   Cuando nace un Proceso paralelo "Solicitud Compra Servidores" debajo de ese proyecto, **hereda** automáticamente todos los Metadatos.
*   **El Súper-Poder del Metadato:**
    *   *Operativo:* El analista abre su Inbox (Pantalla 1) y gracias a los Metadatos, puede filtrar `Quiero ver mis tareas que sean [Prioridad: Alta]` sin abrir un solo caso.
    *   *Gerencial:* El Líder abre los Dashboards BAM (Pantalla 5). El BI de Grafana consulta únicamente la tabla ágil de Metadatos y el estado del motor para dibujar el Pipeline en subsegundos: *"Tenemos 400 procesos atascados pertenecientes al [Centro Costos: IT]"*.

---

### Resumen del Flujo de Valor "End-to-End"
1. Un **Evento** Inbound (Ej. Botón, Webhook O365) instancía la "carretera" (Sea un **Proceso** BPMN o un **Proyecto** Ágil).
2. El sistema adhiere los **Metadatos** del negocio para brindar contexto a los dueños.
3. Las tareas llegan al Ejecutor, este abre el task y diligencia un **Formulario Inteligente** JSON.
4. Ese Payload JSON choca contra una **Regla de Negocio (DMN)** creada por IA, que autoriza el paso al momento.
5. El sistema avanza automáticamente, concluye, genera historial en Bóveda Documental y lanza un **Evento** Outbound cerrando el ciclo.

Este es el manifiesto operativo del ecosistema iBPMS V1. Todo desarrollo y microservicio de Backend/Frontend creado por los programadores debe atenerse a este modelo mental.
