# Handoff de Remediación QA: US-034 CA-3 (Jerarquías Prioritarias)

## 📌 Contexto
Durante la Auditoría Integral (Iteración 1) del iBPMS V1, el Agente Arquitecto Líder (QA) ha detectado una brecha en la especificación arquitectónica del sistema de mensajería (RabbitMQ) relacionada a la Historia de Usuario **US-034: Orquestación a través de RabbitMQ**.

El **CA-3 (Jerarquización de Supervivencia)** exige que el clúster soporte *Priority Queues* para segregar el tráfico Crítico (P1) del tráfico Batch (P3), previniendo que procesos lentos de IA asfixien notificaciones financieras en caso de saturación.
Sin embargo, actualmente `RabbitMqTopologyConfig.java` crea las colas sin el parámetro `x-max-priority` activado, funcionando como un sistema plano FIFO.

## 🎯 Objetivo de este Handoff
El Agente Backend Developer debe modificar el archivo de configuración del ecosistema RabbitMQ para inyectar correctamente el soporte de máxima prioridad en la topología, preparando las colas para acatar correctamente las reglas de P1, P2 y P3 descritas en la US.

## 🛠 Tareas de Ejecución (Backend)

1. **Modificación de `RabbitMqTopologyConfig.java`**:
   - Abrir y modificar `c:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform\backend\ibpms-core\src\main\java\com\ibpms\poc\infrastructure\mq\config\RabbitMqTopologyConfig.java`.
   - Localizar el método `dlxArgs()` o el punto donde se construyen los `Map<String, Object> args` para las colas de negocio (notifications, aiGeneration, integrationsWebook, bpmnEvents, taskRescue).
   - Agregar el atributo obligatorio en la configuración AMQP de RabbitMQ para habilitar colas prioritarias: `args.put("x-max-priority", 10);` (10 es el techo seguro recomendado para la partición por prioridades).
   - Asegurarse de que el argumento se inyecte correctamente en todos los constructores de llamadas a `new Queue(...)` (excluyendo la DLQ, ya que esta no requiere prioridades).

2. **Validación de Componentes Afectados**:
   - Confirmar que la sintaxis de Spring AMQP sea compatible verificando que los imports incluyan todo lo respectivo o realizando test con JUnit/Testcontainers.

## ⚠️ Reglas de Gobernanza y Zero-Trust
- **NO DEBES** modificar archivos relacionados con la base de datos o front-end, céntrate estrictamente en el ecosistema AMQP.
- Las colas existentes en RabbitMQ al añadir el parámetro `x-max-priority` NO pueden ser mutadas en caliente si ya existen en disco, sin embargo para propósitos del entorno de test/construcción bastará actualizar la anotación en Java y compilar. Los contenedores de DB y MQ se recrearán bajo Testcontainers.
- Cuando finalices, deberás correr la suite de Tests general de AMQP (`mvn clean test` enfocado a Rabbit).

## 🏁 Criterio de Éxito
- La declaración `@Bean` de las colas de transaccionalidad muestra que están inicializadas con argumentos que incluyen la propiedad `x-max-priority`.
- Una vez verifiques esto, responde a tu Prompt finalizando el Handoff exitosamente.
