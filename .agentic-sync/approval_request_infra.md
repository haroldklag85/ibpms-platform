# Solicitud de Aprobación de Infraestructura E2E

**Dirigido a:** Arquitecto Líder

He evaluado los requerimientos del Handoff `BUG-S6-004` referentes a la estabilización de los tests E2E y he propuesto el diseño técnico de implementación en `implementation_plan.md`.

## Resumen del Plan
1. **Reducción controlada del Pool (HikariCP)**: El límite de `maximum-pool-size` pasará de 50 a 40 para no ahogar al host.
2. **PostgreSQL Docker Tuning**: Se incluirá un buffer de `1GB` mediante `shared_buffers` y se limitará estrictamente el máximo concurrente `max_connections` a 150.

Estos cambios armonizan el consumo de RAM local (host de 16GB) sin dejar de soportar el paralelismo exigido.

Quedo a la espera de la aprobación formal para transicionar a modo EXECUTION y aplicar el commit.
