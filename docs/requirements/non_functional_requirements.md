# Requerimientos No Funcionales (NFRs) - Atributos de Calidad

Este documento centraliza las métricas y atributos sistémicos (Non-Functional Requirements - NFRs) que la plataforma iBPMS debe cumplir para asegurar su viabilidad técnica, comercial y su operación segura. Está estructurado basándose en los estándares de calidad de software (ISO/IEC 25010).

---

## 1. Seguridad (Security & Privacy)

Define la capacidad del sistema para proteger la información y los datos en reposo y en tránsito (especialmente considerando la transición hacia un modelo Multi-tenant V2).

*   **NFR-SEC-01 (Zero Trust):** Todo tráfico interno (VNet en V1, Service Mesh en V2) entre microservicios y bases de datos DEBE estar cifrado obligatoriamente usando TLS 1.2 o superior (`mTLS` para V2). El Backend NO asumirá confianza del API Gateway y re-validará la firma de los tokens JWT internamente.
*   **NFR-SEC-02 (Cifrado de PII):** Todo campo detectado como "Personal Identifiable Information" (Ej. Datos biométricos, financieros o médicos) dentro de los *payloads* JSON dinámicos, estará cifrado en reposo mediante Transparent Data Encryption (TDE) u ofuscamiento a nivel de capa de aplicación, cumpliendo regulaciones como GDPR / Hábeas Data.
*   **NFR-SEC-03 (Protección Perimetral):** Cualquier punto de entrada síncrono DEBE estar escudado detrás de un Web Application Firewall (WAF) activo y un API Gateway para prevenir ataques de inyección, DDoS y *spoofing* (Ej. Webhooks de Microsoft Graph validados por ClientState).
*   **NFR-SEC-04 (Manejo de Secretos):** Está terminantemente prohibido el *hardcoding* de credenciales, tokens o Connection Strings. Todo secreto será inyectado en tiempo de ejecución de bóvedas seguras (Azure Key Vault) utilizando Managed Identities.

## 2. Rendimiento y Eficiencia (Performance)

Define los tiempos de respuesta del sistema bajo condiciones de operación normal y picos.

*   **NFR-PER-01 (Latencia de Bandejas):** Las consultas de listados (Bandejas Universales de Casos y Tareas) deben resolverse y entregarse al Frontend en **menos de 800 milisegundos (p95)**. Esto justifica la prohibición absoluta de *Full Table Scans* en campos JSON y obliga el uso del patrón *Metadata Indexing* (Tablas llave-valor aplanadas).
*   **NFR-PER-02 (Latencia Inicial de Formularios):** El renderizado en frío de los Micro-Frontends (MFE) basados en el constructor `ui_schema` en el cliente no debe exceder de **2 segundos** desde redes corporativas estándar.
*   **NFR-PER-03 (Archiving & Bloating):** Para prevenir la degradación de I/O en la base de datos operacional, cualquier tabla con crecimiento logarítmico (Ej. `ibpms_audit_log`) debe tener configurado **Table Partitioning por Fecha**. Logs transaccionales mayores a 12 meses se archivarán automáticamente a *Cold Storage*.

## 3. Escalabilidad y Elasticidad (Scalability)

Define la capacidad de manejar picos de trabajo y crecer horizontal o verticalmente.

*   **NFR-SCA-01 (Escalabilidad Táctica V1):** Al ser una arquitectura de Máquinas Virtuales (IaaS), los componentes de capa de Presentación y Core Backend deben soportar la inyección dentro de *Virtual Machine Scale Sets (VMSS)* para escalado en base a umbrales de CPU (>75%).
*   **NFR-SCA-02 (Escalabilidad Estratégica V2):** Para la arquitectura Cloud-Native, el núcleo deberá transicionar hacia clústeres de Kubernetes (AKS), aislando componentes críticos (Ej. Generador DMN, OCR Asíncrono) para escalar asimétricamente e integrando un Broker de Eventos (Kafka) para desacoplamiento puro.

## 4. Confiabilidad y Disponibilidad (Availability)

*   **NFR-AVA-01 (Uptime Target):** La plataforma requiere un SLA de disponibilidad garantizado del **99.9%**, minimizando ventanas de mantenimiento agresivas.
*   **NFR-AVA-02 (Idempotencia):** Dada la naturaleza asíncrona y la probabilidad de reintentos por red (Timeouts), todas las APIs mutables críticas (como Crear Expediente, Completar Tarea, Avanzar Flujo) deben implementar y exigir un `Idempotency-Key` único, evitando duplicidad de *side-effects*.
*   **NFR-AVA-03 (Backup & DR - RTO/RPO V1):** Para el entorno SaaS Táctico inicial, se firma comercialmente un **Recovery Point Objective (RPO) máximo de 2 horas** y un **Recovery Time Objective (RTO) de 8 horas**. Esto será soportado por respaldos incrementales continuos de la BD y copias georredundantes de Azure Managed Disks, con PITR garantizado de 35 días.

## 5. Cumplimiento Normativo (Compliance / Legal)

*   **NFR-CMP-01 (Inmutabilidad de Registros - SGDEA):** Todo artefacto final legal (Contratos, Certificados, Recibos) debe ser inyectado a la Bóveda con un **Hash Criptográfico (SHA-256)** que demuestre que el fichero exacto no ha sido alterado desde el segundo de su creación, garantizando el *No-Repudio*.
*   **NFR-CMP-02 (Destrucción Segura):** El sistema debe aplicar políticas automáticas de Tablas de Retención Documental (TRD) calculando de forma exacta la fecha de caducidad de un artefacto legal para su "Quema" permanente (expurgo y destrucción de datos físicos y lógicos) dictaminado por los Agentes de Riesgo / AI Auditor (V2).

## 6. Mantenibilidad y Evolutividad (Maintainability)

*   **NFR-MNT-01 (Aislamiento de Lógica - Clean Architecture):** En la Fase 1 (Monolítica), se exige la adopción dogmática de la **Arquitectura Hexagonal**. Camunda 7 y la base de datos (`MySQL`) deben operar fuera del dominio Core para evitar acoplamiento y asegurar el salto fácil a V2 con Zeebe.
*   **NFR-MNT-02 (Inmutabilidad Estructural UI):** Si el diseño de los formularios de interacción se modifica, la plataforma no debe necesitar intervención de código, despliegues TI ni re-empaquetamiento (*100% Data-Driven UI* a partir de Schemas JSON).
*   **NFR-MNT-03 (Entorno Local Paritario):** Para agilizar el *developer onboarding*, el equipo TI debe tener la capacidad de ejecutar un comando simple (Ej. `docker-compose up`) que levante de forma incondicional dependencias (MySQL, Camunda empotrado y Backend de Spring Boot) idénticas físicamente al entorno productivo.
