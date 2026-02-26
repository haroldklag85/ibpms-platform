# Reporte de Auditoría Funcional vs Arquitectura C4 (iBPMS)

Como auditor de alineación, he ejecutado un cruce estricto entre los escenarios de negocio listados en el Checklist de Requerimientos (DMN, Case Management, CQRS, Exchange, Saga, SGDEA, RBAC/ABAC) y la representación viva en los diagramas C4 (L1 Context, L2 Containers, L3 Components).

## Parte 1: Evaluación por Escenarios de Negocio Clave

### Escenario 1: Triggers por Correo Institucional (O365)
*   **A) L1 Contexto:** Actor externo `Microsoft 365 / Exchange` está modelado enviando el Webhook. ✅ **OK**.
*   **B) L2 Contenedores:** Identificamos el punto de entrada `Inbound Listener` (Node/Go), el intermediario `Event Broker` y el data store de binarios `Azure Storage Disks` a través del SGDEA. ✅ **OK**.
*   **C) L3 Componentes:** Identificamos el Adapter `Event Listeners (@KafkaListener)`, el UseCase `CaseManagement UseCase`. ⚠️ **GAP-L3**: No existe en el diagrama el componente encargado de extraer adjuntos o interactuar con el *Inbound Listener*. El diagrama L3 está 100% centrado en Spring Boot recibiendo el Kafka, pero obvió el diseño del conector de Graph API que sí sale en el diagrama funcional del plan.

### Escenario 2: Case Management & Vistas Paralelas (Kanban / Agile)
*   **A) L1 Contexto:** Actor `Usuario de Negocio` monitorea flujos. ✅ **OK**.
*   **B) L2 Contenedores:** Frontend SPA renderiza; la base `Estado iBPMS` persiste. ⚠️ **GAP-L2**: CQRS prometía una separación de lecturas (Elastic/OpenSearch) vs escrituras. El L2 solo tiene una única base de datos genérica `MySQL / PostgreSQL`. Si 1000 usuarios consultan tableros Kanban pesados, impactarán el log transaccional del motor C8.
*   **C) L3 Componentes:** `CaseManagement UseCase` modelado y existe Entidad `Expediente / Caso`. ✅ **OK**.

### Escenario 3: Motor de Reglas (DMN / Tablas de Decisión)
*   **A) L1 Contexto:** Actor `Analista / Admin` configura DMN. ✅ **OK**.
*   **B) L2 Contenedores:** Contenedor `DaaS (Motor Reglas)` identificado. ✅ **OK**.
*   **C) L3 Componentes:** ⚠️ **GAP-L3**. El diagrama de componentes L3 del *iBPMS Core* no muestra ningún puerto de salida (*Driven Adapter*) diseñado para invocar al motor DaaS. Se modeló el uso de Camunda y el ERP, pero si `Domain Services` necesita evaluar un score SLA mediante DMN, no tiene el Adapter ("DmnRestClientAdapter") modelado.

### Escenario 4: Asignación Dinámica (Colas, RBAC, ABAC)
*   **A) L1 Contexto:** Proveedor Identidad `IdP (Directorio Activo)` modelado. ✅ **OK**.
*   **B) L2 Contenedores:** El Frontend se comunica con el API Backend, pero no se evidencia un contenedor especializado o policy engine para el *Access Control*. ⚠️ **AMBIGÜEDAD**. ¿El Gateway APIM inyecta los *claims* o el *API Backend Core* resuelve roles pesados contra la DB?
*   **C) L3 Componentes:** Hay una entidad `Tarea Humana` y un POJO de asignación. ✅ **OK**.

### Escenario 5: Integración Transaccional con el ERP (Saga)
*   **A) L1 Contexto:** `ERP / Core Bancario` modelado. ✅ **OK**.
*   **B) L2 Contenedores:** ⚠️ **GAP-L2**: En el nivel 2, la flecha `Sagas` sale del *Kafka Broker* pero no toca ningún conector de salida (*Outbound Connector*) explícito. El contenedor de API Backend expone *GraphQL/REST*, pero ¿quién materializa realmente la llamada HTTP asíncrona al ERP post-Kafka si Camunda delega el Job worker?
*   **C) L3 Componentes:** `ERP Connector (Feign Client)` y `Workflow Engine UseCase`. ✅ **OK** parcial, aunque choca con el GAP de L2.

### Escenario 6: Gestión Documental Integrada (SGDEA / MoReq)
*   **A) L1 Contexto:** `Gestor Documental Externo` (SharePoint) opcional está modelado. ✅ **OK**.
*   **B) L2 Contenedores:** Contenedor `Módulo SGDEA (Nativo)` y `Bóveda (Blobs)`. ✅ **OK**.
*   **C) L3 Componentes:** ⚠️ **GAP-L3**: Nuestro *iBPMS Core* en Spring Boot no tiene ningún componente en `Puertos de Salida` modelado para conectarse al `Módulo SGDEA`. Si avanza el proceso y necesita leer/tapar un expediente, no hay adaptador CMIS o REST a ese contenedor interno.

---

## Parte 2: Detección de SOBRANTES

Tras peinar el C4 model, destaco lo siguiente:
1.  **Sobrante en L2: MySQL / PostgreSQL "Guarda Variables".**
    *   **Tensión / ¿Qué outcome soporta?**: Prometimos que usaríamos Zeebe/C8. Estos motores modernos no usan bases relacionales, su arquitectura interna se basa en *RocksDB* embebido más exportación a ElasticSearch (Recordar requerimiento: *Alta Volumetría / CQRS*).
    *   **Recomendación:** Preguntar si estamos usando Camunda 7 (Relacional) o Camunda 8 (RocksDB + Elastic). Si es el 8, el MySQL en L2 es un falso/sobrante para el estado del motor.

---

## Parte 3: PREGUNTAS CRÍTICAS (Ambigüedades a Resolver)

Para poder cerrar estos Gaps en C4 y que los YAML de API y el código PoC no nazcan defectuosos, responde por favor lo siguiente:

1.  **Motor BPM (Camunda 7 vs 8):** El documento menciona "Zeebe / C8 / Flowable" pero al lado dibuja un MySQL monolítico. Si usamos un modelo de "Alta Escalabilidad CQRS" en la PoC, ¿Nuestra base relacional MySQL es solo para el *CaseManagement/BusinessData* y el estado del BPM lo delegaremos limpiamente a un NoSQL (o en memoria de Zeebe), o forzaremos C7?
2.  **Arquitectura DMN:** ¿El motor DMN va a ser completamente síncrono por capa HTTP como un SaaS independiente (`DaaSContainer`), o decidimos integrarlo como una librería empotrada `.jar` dentro de nuestro `API Backend Core` en Spring Boot (lo cual cambiaría radicalmente L2 y L3)?
3.  **Manejo de Roles (ABAC):** ¿La validación de permisos granulares para *Pull* de tareas en la bandeja las hará el frontend filtrando por token JWT, o necesitamos diseñar un caso de uso estricto de seguridad `SecurityUseCase` en el Backend de Spring? 
4.  **Capa Documental en PoC:** Dado el GAP-L3 entre el Backend y el SGDEA, para esta PoC táctica de Java... ¿Simulamos el almacenamiento apuntando directamente a un disco/S3, o quieres que ya dejemos diseñado el `EcmOutboundAdapter` formal aunque esté "en blanco"?
