# RabbitMQ Topology Documentation

## Queues
- **ibpms.dlq**: Dead Letter Queue para almacenar mensajes que fallaron procesamiento.
- **ibpms.task.queue**: Cola para procesar tareas asíncronas.
- **ibpms.events.queue**: Cola de eventos de sistema.

## Exchanges
- **ibpms.dlx.exchange**: Dead Letter Exchange para enrutamiento a DLQ.
- **ibpms.direct.exchange**: Exchange principal para tareas.

## Bindings
- **ibpms.task.queue** -> **ibpms.direct.exchange** con rounting key `task.routing.key`
- **ibpms.dlq** -> **ibpms.dlx.exchange** con routing key `dlq.routing.key`

## Retry Policies
El sistema usa un backoff exponencial con retry máximo para evitar saturación de colas en caso de fallos transitorios en conectores externos.
