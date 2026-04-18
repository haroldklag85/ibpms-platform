# Requerimientos No Funcionales (NFRs) - Atributos de Calidad
**Producto:** Plataforma iBPMS (Intelligent Business Process Management System)  
**Stack V1:** Spring Boot, Vue 3/Vite, PostgreSQL (JSONB), Camunda 7, RabbitMQ, Azure (IaaS/PaaS)  
**Última actualización:** 2026-04-05

Este documento centraliza las métricas y atributos sistémicos (Non-Functional Requirements - NFRs) que la plataforma iBPMS debe cumplir para asegurar su viabilidad técnica, comercial y su operación segura. Está estructurado basándose en los estándares de calidad de software (ISO/IEC 25010).

---

## 1. Seguridad (Security & Privacy)

Define la capacidad del sistema para proteger la información y los datos en reposo y en tránsito.

*   **NFR-SEC-01 (Zero Trust):** Todo tráfico interno (VNet en V1, Service Mesh en V2) entre componentes y bases de datos DEBE estar cifrado obligatoriamente usando TLS 1.2 o superior (`mTLS` para V2). El Backend NO asumirá confianza del API Gateway y re-validará la firma de los tokens JWT internamente.
*   **NFR-SEC-02 (Cifrado de PII):** Todo campo detectado como "Personal Identifiable Information" (Ej. Datos biométricos, financieros o médicos) dentro de los *payloads* JSON dinámicos, estará cifrado en reposo mediante Transparent Data Encryption (TDE) u ofuscamiento a nivel de capa de aplicación, cumpliendo regulaciones como GDPR / Hábeas Data. *(Referencia: US-017 CA-12 define cifrado at-rest y a nivel de aplicación con llaves en Azure Key Vault)*.
*   **NFR-SEC-03 (Protección Perimetral):** Cualquier punto de entrada síncrono DEBE estar escudado detrás de un Web Application Firewall (WAF) activo y un API Gateway para prevenir ataques de inyección, DDoS y *spoofing* (Ej. Webhooks de Microsoft Graph validados por ClientState).
*   **NFR-SEC-04 (Manejo de Secretos):** Está terminantemente prohibido el *hardcoding* de credenciales, tokens o Connection Strings. Todo secreto será inyectado en tiempo de ejecución desde bóvedas seguras (Azure Key Vault) utilizando Managed Identities.
*   **NFR-SEC-05 (Rate Limiting):** Las APIs públicas y los endpoints de alta frecuencia (Ej. guardado de borradores) DEBEN implementar rate-limiting por usuario/IP para prevenir abuso. *(Referencia: US-017 CA-14 define un máximo de 6 guardados/minuto por tarea)*.

## 2. Rendimiento y Eficiencia (Performance)

Define los tiempos de respuesta del sistema bajo condiciones de operación normal y picos.

*   **NFR-PER-01 (Latencia de Bandejas):** Las consultas de listados (Bandejas Universales de Casos y Tareas) deben resolverse y entregarse al Frontend en **menos de 800 milisegundos (p95)**. Esto justifica la prohibición absoluta de *Full Table Scans* en campos JSON y obliga el uso del patrón *Metadata Indexing* (Tablas llave-valor aplanadas).
*   **NFR-PER-02 (Latencia Inicial de Formularios):** El renderizado en frío de los formularios dinámicos (Monolito Vue 3/Vite) basados en el constructor `ui_schema` en el cliente no debe exceder de **2 segundos** desde redes corporativas estándar.
*   **NFR-PER-03 (Archiving & Bloating):** Para prevenir la degradación de I/O en la base de datos operacional, cualquier tabla con crecimiento logarítmico (Ej. `ibpms_audit_log`, `form_event_store`) debe tener configurado **Table Partitioning por Fecha**. Registros mayores a 12 meses se archivarán automáticamente a *Cold Storage*. *(Referencia: US-017 CA-18 define política de archivado anual del Event Store)*.
*   **NFR-PER-04 (Latencia de Transacciones CQRS):** La operación de completar una tarea (endpoint `/complete`) debe resolverse en **menos de 5 segundos (p95)** bajo condiciones normales, incluyendo persistencia del evento, señalización a Camunda y limpieza del borrador. *(Referencia: US-017 CA-17)*.
*   **NFR-PER-05 (Rollback Controlado):** En caso de fallo de Camunda post-persistencia, el rollback compensatorio (Saga inversa) debe completarse en un máximo de **17 segundos** incluyendo 3 reintentos. *(Referencia: US-017 CA-10)*.

## 3. Escalabilidad y Elasticidad (Scalability)

Define la capacidad de manejar picos de trabajo y crecer horizontal o verticalmente.

*   **NFR-SCA-01 (Escalabilidad Táctica V1):** Al ser una arquitectura de Máquinas Virtuales (IaaS) sobre Azure, los componentes de capa de Presentación y Core Backend deben soportar la inyección dentro de *Virtual Machine Scale Sets (VMSS)* para escalado en base a umbrales de CPU (>75%).
*   **NFR-SCA-02 (Escalabilidad Estratégica V2):** Para la arquitectura Cloud-Native, el núcleo deberá transicionar hacia clústeres de Kubernetes (AKS), aislando componentes críticos (Ej. Generador DMN, OCR Asíncrono) para escalar asimétricamente e integrando un Broker de Eventos (Kafka) para desacoplamiento puro.

## 4. Confiabilidad y Disponibilidad (Availability)

*   **NFR-AVA-01 (Uptime Target):** La plataforma requiere un SLA de disponibilidad garantizado del **99.9%**, minimizando ventanas de mantenimiento agresivas.
*   **NFR-AVA-02 (Idempotencia):** Dada la naturaleza asíncrona y la probabilidad de reintentos por red (Timeouts), todas las APIs mutables críticas (como Crear Expediente, Completar Tarea, Avanzar Flujo) deben implementar y exigir un `Idempotency-Key` único, evitando duplicidad de *side-effects*.
*   **NFR-AVA-03 (Backup & DR - RTO/RPO V1):** Para el entorno SaaS Táctico inicial, se firma comercialmente un **Recovery Point Objective (RPO) máximo de 2 horas** y un **Recovery Time Objective (RTO) de 8 horas**. Esto será soportado por respaldos incrementales continuos de la BD PostgreSQL y copias georredundantes de Azure Managed Disks, con PITR garantizado de 35 días.
*   **NFR-AVA-04 (Resiliencia de Mensajería):** Las colas RabbitMQ deben implementar Dead-Letter Queues (DLQ), reintentos con Exponential Backoff y persistencia de mensajes ante caídas del broker. *(Referencia: US-034 CA-04 a CA-10)*.

## 5. Cumplimiento Normativo (Compliance / Legal)

*   **NFR-CMP-01 (Inmutabilidad de Registros - SGDEA):** Todo artefacto final legal (Contratos, Certificados, Recibos) debe ser inyectado a la Bóveda con un **Hash Criptográfico (SHA-256)** que demuestre que el fichero exacto no ha sido alterado desde el segundo de su creación, garantizando el *No-Repudio*.
*   **NFR-CMP-02 (Destrucción Segura):** El sistema debe aplicar políticas automáticas de Tablas de Retención Documental (TRD) calculando de forma exacta la fecha de caducidad de un artefacto legal para su "Quema" permanente (expurgo y destrucción de datos físicos y lógicos) dictaminado por los Agentes de Riesgo / AI Auditor (V2).
*   **NFR-CMP-03 (Event Sourcing Inmutable):** Los eventos de negocio persistidos en el `form_event_store` son inmutables. Las correcciones se implementan como eventos compensatorios (Ej. `FORM_SUBMIT_ROLLED_BACK`), nunca como DELETE o UPDATE sobre eventos existentes. *(Referencia: US-017 CA-10)*.

## 6. Mantenibilidad y Evolutividad (Maintainability)

*   **NFR-MNT-01 (Aislamiento de Lógica - Clean Architecture):** En la Fase 1 (Monolítica), se exige la adopción dogmática de la **Arquitectura Hexagonal**. Camunda 7 y la base de datos (`PostgreSQL`) deben operar fuera del dominio Core para evitar acoplamiento y asegurar el salto fácil a V2 con Zeebe.
*   **NFR-MNT-02 (Inmutabilidad Estructural UI):** Si el diseño de los formularios de interacción se modifica, la plataforma no debe necesitar intervención de código, despliegues TI ni re-empaquetamiento (*100% Data-Driven UI* a partir de Schemas JSON).
*   **NFR-MNT-03 (Entorno Local Paritario):** Para agilizar el *developer onboarding*, el equipo TI debe tener la capacidad de ejecutar un comando simple (Ej. `docker-compose up`) que levante de forma incondicional dependencias (PostgreSQL, Camunda empotrado, RabbitMQ y Backend de Spring Boot) idénticas físicamente al entorno productivo.

## 7. Observabilidad (Observability)

Define la capacidad del sistema de ser monitoreado, diagnosticado y depurado en tiempo real.

*   **NFR-OBS-01 (Logging Estructurado):** Todos los componentes (Backend Spring Boot, Workers RabbitMQ, Jobs asíncronos) DEBEN emitir logs en formato JSON estructurado con campos obligatorios: `timestamp`, `level`, `correlationId`, `userId`, `action`, `duration_ms`. Está prohibido loguear datos PII en texto plano.
*   **NFR-OBS-02 (Tracing Distribuido):** Cada petición HTTP entrante debe generar un `correlationId` (UUID) que se propagará a través de todas las capas (API Gateway → Backend → Camunda → RabbitMQ → Workers) para permitir la reconstrucción completa de una transacción en caso de incidente.
*   **NFR-OBS-03 (Health Checks & Readiness):** El Backend debe exponer un endpoint `/actuator/health` que valide la conectividad con PostgreSQL, Camunda Engine, RabbitMQ y Azure Key Vault. El VMSS debe consumir este endpoint para decisiones de escalado y auto-healing.
*   **NFR-OBS-04 (Alerting Proactivo):** Los umbrales críticos generarán alertas automáticas (email + dashboard):
    - Latencia de bandejas > 800ms sostenida 5 minutos → Alerta `WARNING`
    - Cola DLQ de RabbitMQ con > 10 mensajes → Alerta `CRITICAL`
    - Espacio en disco de PostgreSQL > 85% → Alerta `WARNING`
    - Fallos de autenticación > 50 en 1 minuto → Alerta `CRITICAL` (posible ataque)
*   **NFR-OBS-05 (Auditoría de Acceso a Secretos):** Cada visualización de credenciales, tokens OAuth o API Keys en la interfaz administrativa DEBE generar un registro de auditoría inmutable con: `quién`, `cuándo`, `qué secreto`, `dirección IP`. *(Referencia: US-051 "El Ojo de Sauron")*.

## 8. Internacionalización (i18n)

Define la capacidad del sistema de operar en múltiples idiomas y zonas horarias.

*   **NFR-I18N-01 (Idioma de UI):** El Frontend (Vue 3) debe soportar al menos **Español (es-CO)** e **Inglés (en-US)** como idiomas base, con la arquitectura preparada para agregar más idiomas sin cambios de código (archivos JSON de traducción externalizados).
*   **NFR-I18N-02 (Zonas Horarias):** Todos los timestamps persistidos en PostgreSQL DEBEN almacenarse en **UTC**. La presentación al usuario se convertirá a la zona horaria de su perfil (Ej. UTC-5 Bogotá). *(Referencia: US-043 CA-4 define manejo de husos horarios en SLAs)*.
*   **NFR-I18N-03 (Formatos Locales):** Los formatos de fecha, hora, moneda y números deben respetar el locale del usuario (Ej. `DD/MM/YYYY` para es-CO vs `MM/DD/YYYY` para en-US). Se prohíbe el hardcoding de formatos.

---

## Historial de Cambios

| Fecha | Cambio |
|---|---|
| 2026-04-05 | Corrección de MySQL→PostgreSQL (NFR-MNT-01, NFR-MNT-03). Corrección de MFE→Monolito Vue 3/Vite (NFR-PER-02). |
| 2026-04-05 | Nuevas categorías: Observabilidad (§7: NFR-OBS-01 a NFR-OBS-05) e Internacionalización (§8: NFR-I18N-01 a NFR-I18N-03). |
| 2026-04-05 | Nuevos NFRs: NFR-SEC-05 (Rate Limiting), NFR-PER-04 (Latencia CQRS), NFR-PER-05 (Rollback), NFR-AVA-04 (Resiliencia MQ), NFR-CMP-03 (Event Sourcing Inmutable). |
| 2026-04-05 | Referencias cruzadas a US-017, US-034, US-043 y US-051 para trazabilidad. |
