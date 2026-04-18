# Topología de Enrutamiento RabbitMQ

Este mapa declara la infraestructura del Topic Exchange y Dead Letter Exchange de la plataforma (Implementado bajo CA-4 / US-034).

| Componente | Tipo | Nombre | Propósito | Configuración Especial |
|---|---|---|---|---|
| **Exchange** | Topic | `ibpms.exchange.topic` | Punto central de entrada. Direcciona mensajes a las colas especializadas en base al topic. | - |
| **Exchange** | Direct | `ibpms.exchange.dlx` | Dead Letter Exchange. Recibe mensajes envenenados o descartados por reintentos máximos. | - |
| **Queue** | DLQ | `ibpms.dlq.global` | Contenedor físico de los mensajes DLQ. Almacena temporalmente antes del archivo pasivo. | `x-message-ttl: 2592000000` (30d) |
| **Queue** | Business | `ibpms.notifications.email` | Notificaciones vía mail (US-034). | `x-dead-letter-exchange: ibpms.exchange.dlx` |
| **Queue** | Business | `ibpms.ai.generation` | Generación NLP asíncrona de BPMN y DMN. | `x-dead-letter-exchange: ibpms.exchange.dlx` |
| **Queue** | Business | `ibpms.integrations.webhook` | Llamadas webhook outbound a través del Hub. | `x-dead-letter-exchange: ibpms.exchange.dlx` |
| **Queue** | Business | `ibpms.bpmn.events` | Señales genéricas BPMN. | `x-dead-letter-exchange: ibpms.exchange.dlx` |
| **Queue** | Business | `ibpms.task.rescue` | Rescate de tareas para Anti-Cherry-Picking y reasignación en cola prioritaria. | `x-dead-letter-exchange: ibpms.exchange.dlx` |
