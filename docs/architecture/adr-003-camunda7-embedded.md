# ADR 003: Motor BPMN Táctico - Camunda 7 Embedded (Acoplamiento V1)

**Estado:** Aceptado
**Fecha:** 2026-02-23
**Contexto de la Decisión:** Arquitectura Core Backend V1

## Contexto y Problema
El requerimiento base de la plataforma es orquestar flujos de trabajo (Workflows) y tomar decisiones de negocio (DMN). El estándar indiscutible de la industria actual es Camunda. Sin embargo, Camunda Platform 8 (Zeebe) representa un salto radical hacia una arquitectura Nativa de la Nube, requiriendo orquestadores remotos vía gRPC, bases de datos no relacionales intensivas (Elasticsearch) y un ecosistema distribuido.

Nuestra V1 (Prueba de Concepto y Go-To-Market Táctico) está fuertemente limitada a funcionar sobre máquinas virtuales (IaaS Azure VMs) y una base relacional monolítica consolidada (MySQL 8). Implementar el ecosistema completo de Zeebe en V1 elevaría exponencialmente el costo de infraestructura, la fricción operativa y el *Time-to-Market*. 

Teníamos que decidir cómo soportar de manera robusta los procesos de negocio en un esquema monolítico transicional sin envenenar todo el código a futuro.

## Decisión
Hemos decidido **utilizar Camunda 7 Embedded (como dependencia `.jar`) dentro de la aplicación Spring Boot 3**, compartiendo la misma transacción (JDBC) y Base de Datos MySQL 8 que el dominio de negocio. 

## Justificación
1. **Time-to-Market y Simplicidad Operativa:** Para V1, es indispensable que los desarrolladores puedan encender el Backend localmente en segundos. Empotrar el motor significa que un solo proceso Java Virtual Machine (JVM) y una sola conexión a base de datos resuelven la orquestación. No hay que orquestar contenedores satélite.
2. **Consistencia Transaccional CA (Cap Theorem):** Al usar el motor empotrado, si nuestra lógica de negocio (`ibpms_case` insert) y el paso de BPMN fallan a la vez, Spring `@Transactional` hace rollback a ambos simultáneamente. Nos ahorra implementar Sagas o Patrones *Outbox* complicados transitoriamente.

## Consecuencias y Mitigación Obligatoria
*   **Deuda Técnica Aceptada:** Somos conscientes de que esta arquitectura genera un **Monolito Acoplado por Base de Datos**, limitando el escalamiento elástico de los "Workers" independientemente del Core. Camunda 8 es el estándar a largo plazo.
*   **Plan de Mitigación Cero-Acoplamiento (Hexagonal):** Para garantizar que el reemplazo de Camunda 7 por un esquema *Event-Driven* (V2) sea indoloro, establecimos por mandato el uso estricto de **Arquitectura Hexagonal (Ports & Adapters)**.
    *   Los Casos de Uso del negocio (`Application Services`) expondrán interfaces puras de Java.
    *   Camunda 7 solo existirá en el anillo exterior como un **Adaptador Secundario (Driven)**. 
    *   Cualquier desarrollador que inserte anotaciones `@Camunda` o llamadas a su API dentro de las entidades de dominio será rechazado en *Code Review*.
El Gateway APIM (Patrón Strangler) será la clave para estrangular estos flujos en el futuro.
