# Análisis Funcional y de Entendimiento: US-034

## Historia Analizada
**US-034: Orquestación a través de RabbitMQ**

---

### 1. Resumen del Entendimiento
La US-034 es una historia de carácter **Arquitectónico y de Resiliencia** que dicta cómo el iBPMS debe lidiar con eventos asíncronos masivos (envío de correos, integraciones externas pesadas, peticiones IA). Obliga a delegar la gestión del tráfico a un *Message Broker* dedicado (RabbitMQ o Kafka), prohibiendo usar la base de datos transaccional (SQL) como cola de mensajes para evitar bloqueos y caídas catastróficas. Además, exige gobernanza visual sobre los fallos (DLQ Dashboard) y garantías de QoS (Quality of Service) a través de priorización de colas.

### 2. Objetivo Principal
Proteger la integridad transaccional y el *uptime* de la plataforma frente a "tsunamis" de solicitudes concurrentes. Garantiza que las tareas críticas de negocio no se congelen por el atasco de tareas secundarias y dota al equipo IT de herramientas de recuperación ante caídas masivas de sub-sistemas downstream.

### 3. Alcance Funcional Definido
Abarca el diseño imperativo del backend transaccional (API y Workers) para la derivación de procesos asíncronos mediante RabbitMQ. Incluye también un alcance a nivel de Frontend enfocado exclusivamente al perfil de Administrador IT, para construir un Panel de control que exponga y manipule la "Dead Letter Queue" (DLQ).

### 4. Lista de Funcionalidades Incluidas
1. **Delegación de Eventos Pesados:** Configuración de un clúster RabbitMQ (o Kafka) para el enrutamiento de transacciones asíncronas de inteligencia artificial, mensajería e integraciones.
2. **Prohibición Anti-Patrón SQL:** Regla estricta a nivel codificación para no crear "Scheduler tables" que colapsen por deadlocks en base de datos.
3. **Monitor Visual DLQ (Dead Letter Queue):** Una pantalla de monitoreo IT que expone topográficamente el volumen de mensajes muertos o atascados.
4. **Rescate Masivo (Botones de Acción):** Interfaz para "Purgar la cola DLQ" (Descartar) y "Reintentar Forzosamente" (Rescatar y volver a encolar) en caso de caída temporal originada por error humano o proveedor caído.
5. **Enrutamiento por Priority Queues:** Implementación funcional de "Quality of Service", donde el Broker lee y prioriza los mensajes L1 (Críticos) sobre los L3 (Batches), despachándolos al NodeWorker sin importar si el Batch tiene ventaja cronológica en la cola.

### 5. Lista de Brechas, GAPs o Ambigüedades Detectadas
- **GAP de Granularidad DLQ (Visibilidad en Reintentos):** La US especifica que existen botones de purga y reintento "Masivos". Sin embargo, no aclara si el IT Admin puede inspeccionar individualmente un mensaje DLQ (leer su payload de error) antes de decidir o si la operación *Reintentar* dispara todo ciegamente. 
- **GAP de Trazabilidad en Repetibles Críticos (Idempotencia):** La historia detalla "Reintentar Mensajes Forzosamente". No dictamina reglas de *Idempotencia* en los Workers receptores, generando un riesgo de que presionar `Retry` dos veces por error humano despache dos pagos o dos notificaciones legales duplicadas.

### 6. Lista de Exclusiones (Fuera de Alcance)
- Lógica individual de las integraciones o envíos directos de correo; el Broker orquesta los "sobres", no su contenido ni destinatario.
- Infraestructura de Orquestación o Core de Camunda (BPMN Workflow); el RabbitMQ aquí expuesto sirve a la capa de integración técnica, no al motor de procesos de negocio.
- Auto-escalado de infraestructura de servidores Worker ante colas elevadas; esto delega exclusivamente el orden de consumo, no la asignación elástica de Kubernetes/Docker.

### 7. Observaciones de Alineación o Riesgos (Arquitectura)
> [!WARNING]
> La **US-049** indica que el correo se descarga de RabbitMQ y el worker atiende un arreglo UUID, mientras esta **US-034** especifica que el broker debe prohibir usar BDs en SQL para encolamientos. Asegurar que las librerías de Message Broker en el Backend (Ej. Spring AMQP o similares) no usen fallbacks embebidos (como H2 de Spring Batch) y en su lugar se adhirieran fidedignamente al servidor AMQP remoto. Además, se requiere aplicar políticas de idempotencia (Distributed Redis Locks) explícitas en cada Worker Catcher para evitar que el resurgimiento de DLQ duplique tareas irrecuperables.
