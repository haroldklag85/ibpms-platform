# 📦 Handoff Document - Sprint 9: Desacoplamiento DMN (Modulith Java V1)

**Atención: Equipo de Agentes de Desarrollo Backend (Java Squad)**
Este documento contiene las especificaciones arquitectónicas (Contrato) para la ejecución del Sprint 9. Su misión es aislar toda la lógica de evaluación de Reglas de Negocio (DMN) en un espacio separado, preparándonos para la V2 sin romper el límite de **1 JVM (Backend)** de la fase V1.

---

## 🎯 Objetivo del Sprint
Convertir el directorio `backend/` en un proyecto **Maven Multi-Módulo**.
Actualmente, todo el código (BPMN y DMN) vive mezclado dentro de `ibpms-core`. El objetivo es extraer el código relacionado a la Inteligencia DMN hacia un nuevo módulo Maven llamado `ibpms-dmn-engine` y consumirlo como una librería (jar) en el módulo principal.

## 🏛️ Restricción Arquitectónica (CRÍTICA)
Debido a la estricta infraestructura táctica (IaaS V1), **está PROHIBIDO desplegar el `ibpms-dmn-engine` como un microservicio independiente (Spring Boot en otro puerto)**. Eso consumiría memoria RAM excesiva del servidor en este momento.

*   **Regla "Modulith":** Ambos módulos (`ibpms-core` y `ibpms-dmn-engine`) deben fusionarse en tiempo de compilación (`mvn clean package`). El contenedor final de Docker seguirá ejecutando un único archivo JAR (el de `ibpms-core`), que llevará incrustadas las clases compiladas del motor DMN.

## 🛠 Pasos de Acción Inmediata para el Agente Java:

1.  **Refactorización Maven (Multi-Módulo):**
    *   Crea un archivo `pom.xml` padre en la raíz `backend/` de tipo `<packaging>pom</packaging>`.
    *   Declara los módulos: `<module>ibpms-core</module>` y `<module>ibpms-dmn-engine</module>`.
    *   Crea la carpeta `backend/ibpms-dmn-engine` con su respectivo `pom.xml`, definiendo dependencias como `camunda-engine-dmn` pero **sin el plugin de Spring Boot Maven** (no debe ser ejecutable por sí mismo).
2.  **Extracción de Código (DMN):**
    *   Migra cualquier lógica de evaluación de tablas de decisión, interfaces y conectores DMN o interacción con AI (Generador JSON a DMN) hacia el nuevo módulo `ibpms-dmn-engine`.
    *   Aplica **Arquitectura Hexagonal**: Este módulo oculto debe tener un puerto de entrada (Interfaz) claro, por ejemplo `DmnEvaluationPort`.
3.  **Inyección en el Core (Ligado Estático):**
    *   Modifica el `pom.xml` de `ibpms-core` para que declare como dependencia al módulo `<artifactId>ibpms-dmn-engine</artifactId>`.
    *   Asegúrate de que la Inyección de Dependencias (Spring `@ComponentScan`) alcance los paquetes del nuevo módulo para que inyecte el motor de reglas de forma transparente en el motor de Procesos.

¡Procedan a ejecutar el Sprint 9! Asegúrense de correr `mvn clean install` desde la raíz de `backend/` para validar que la compilación es exitosa.
