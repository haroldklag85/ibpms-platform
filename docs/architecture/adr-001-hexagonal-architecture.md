# ADR 001: Adopción de Arquitectura Hexagonal y Domain-Driven Design (DDD)

**Fecha:** 2026-02-22
**Estado:** Aceptado
**Contexto:** Plataforma Core iBPMS (Motor de Workflows y Case Management)

## Contexto y Problema

El motor de la plataforma iBPMS, al ser el núcleo de la orquestación corporativa, manejará reglas de negocio extremadamente críticas (expedientes, aprobaciones, priorizaciones). Históricamente, en aplicaciones empresariales similares (BPMs tradicionales), el código de negocio suele terminar fuertemente acoplado al framework subyacente (ej. invocar APIs nativas de Camunda, Zeebe o Spring Context directamente dentro de las validaciones de negocio).

Si este acoplamiento ocurre:
1. Reemplazar o actualizar el motor BPM en el futuro (ej. migrar de la Etapa 1 a la Etapa 2 Cloud-Native) se vuelve un proceso de reescritura masiva.
2. Hacer pruebas unitarias sobre reglas de SLA o avance de expedientes requeriría levantar la base de datos completa y el motor instalado (Pruebas de Integración lentas), dificultando el agilismo.

## Decisión

Para asegurar la **mantenibilidad, testeabilidad y resiliencia ante el Vendor Lock-in**, implementaremos **Arquitectura Hexagonal (Puertos y Adaptadores)** y principios de **Domain-Driven Design (DDD)** para aislar nuestro Core de Negocio de las capas de infraestructura.

Se aplican las siguientes reglas inquebrantables de diseño:

1. **Dominio Aislado (Entities & Domain Services):** El código dentro de la capa `domain` no debe tener absolutamente ninguna dependencia tecnológica. No puede importar `@Entity` de JPA, ni bibliotecas externas (como Kafka, Camunda o APIs Cloud). Solo usará tipos y colecciones primitivas de Java.
2. **Inversión de Dependencias (Puertos):** La capa de aplicación `application/ports` definirá "Puertos de Entrada" e "Interfaces de Puertos de Salida" para hablar con bases de datos, APIs de red, o el propio motor BPM.
3. **Adaptadores Descartables:** El código que hable con bases de datos (JPA / MySQL) o los motores de proceso (Zeebe / Flowable) residirá en la periferia `infrastructure/adapters` implementando los puertos. Si mañana cambia Camunda por otro motor, **solo** se borra y repone ese adaptador, protegiendo todo el `domain`.

## Consecuencias Positivas

*   **Pruebas Unitarias Aisladas:** Podemos probar cómo avanza un expediente de 0 a 100 sin necesidad de levantar bases de datos relacionales ni contextos de Spring Boot.
*   **Evolución Segura a Kubernetes (V2):** Al migrar en el futuro al Patrón Strangler en AKS, la tecnología "externa" rotará sin romper las tripas formales de nuestros esquemas de negocio.

## Consecuencias Negativas

*   **Curva de Aprendizaje:** Exigirá al equipo desarrollador aprender bien dónde ubicar las lógicas (Controllers vs UseCases vs Entities) para evitar la filtración de dominio hacia infraestructura.
*   **Verbosity Inicial:** Se requiere la escritura de más clases/interfaces en código inicial (múltiples DTOs, mappers, Ports) comparado con el anti-patrón de *Script de Transacción* (Spring Data directo en RestController).
