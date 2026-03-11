# Entorno de Integración UAT - Docker Compose (PostgreSQL PgVector)

El entorno UAT para pruebas End-to-End **está alineado a la Arquitectura Oficial ADR-005**.
Este entorno consolida el motor del proceso de negocio y las exigencias de almacenamiento matemático (Vectores KNN) de nuestra Inteligencia Artificial.

### ¿Qué se habilitó?
* **PostgreSQL + PgVector (`ibpms-postgres-uat`):** Puerto 5432. **Motor Único.** Guardará la tokenización de Camunda 7, los JSON del negocio, y almacenará las incrustaciones matemáticas (Embeddings) del Agente Data Scientist para MLOps.
* **RabbitMQ (`ibpms-rabbitmq-uat`):** Puerto 5672 (AMQP) y 15672 (Management UI). Regula reintentos de publicación MLOps nocturnos (US-034) y resiliencia asíncrona hacia APIs externas (O365).
* **Redis Alpine (`ibpms-redis-uat`):** Puerto 6379. Evita colisiones operativas en tableros masivos mediante candados distribuidos (Distributed Locks) y cacheo de catálogos V1.

### ¿Cómo iniciar las Pruebas?
Al contar en tu máquina con Docker Desktop o Docker Engine instalado, ejecuta en la raíz de la plataforma:
```bash
docker-compose up -d
```
Esperar a que levante por completo y posteriormente arrancar el backend Spring Boot.
