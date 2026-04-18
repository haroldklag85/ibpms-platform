# Análisis Funcional Definitivo: US-046 (Gobernanza de Rendimiento e Integraciones)

## 1. Resumen del Entendimiento
La US-046 otorga palancas C-Level (SysAdmin) para asfixiar, restringir o reconectar la plomería de peticiones HTTP pesadas de The Data Warehouses y módulos Inbounds externos a fin de preservar la continuidad del servicio del iBPMS global durante crisis operativas.

## 2. Objetivo Principal
Contención Anti-DDoS interno, y protección de la capa de discos SSD NVMe en Bases de Datos mediante purgas Cold Storage y desactivación inmediata de flujos CRM en caso de averías empresariales de terceros.

## 3. Alcance Funcional Definido
**Inicia:** Interacción CISO en Pantalla 15.D (Connections Dashboard).
**Termina:** Un Timer Backend mueve archivos, o un API endpoint cambia de HTTP a `Local Catalog Fallback`.

## 4. Lista de Funcionalidades Incluidas
- **Master Switch CRM External API (CA-4306):** Override a la US-013/019 para aislar el iBPMS del CRM corporativo apagado/caído, permitiendo facturación/operación bajo metadatos desactualizados tolerados por la empresa.
- **BAM Throttling Rate (CA-4312):** Limitador reactivo contra usuarios adictos a la analítica (Protege BD relacional).
- **Blob Cold Storage Purge Rule (CA-4317):** Archivar logs MLOps pesados hacia Azure antes de su borrado físico local.
- **SysAdmin SOS Banners (CA-4329/4324):** Letreros color rojo que rompen la Interfaz Visual de Admins por alertas graves (Buzones o CRM off-sync).

## 5. Lista de Brechas, Gaps o Ambigüedades Detectadas
- **Alerta de Inanición de RAM en Application Server (⚠️ CA-4317):** Se indica que el "Backend Spring Boot" (Scheduled task) leerá los JSONLogs MLOps, los enrutará a Azure y luego hará commit `DELETE` sobre PostgreSQL. Esto es un patrón tóxico de escalabilidad de Nube: Trasladar GigaBytes de logs históricos a través de la memoria (Heap/Buffer) del Application Server (encargado de servir a clientes y ejecutar BPMN). Causará una inminente parálisis del Garbage Collector (STW) tumbando el nodo del iBPMS. **Mecanismo exigido:** Delega esta limpieza nativa a un "Azure Data Factory" pipeline directo Base-a-Base o usa Scripts asíncronos Python en colas de baja prioridad Serverless (Azure Functions), descargando al Core de esta tarea sucia.

## 6. Lista de Exclusiones (Fuera de Alcance V1)
- Data Lake direct feeding. Azure Blobs puros.

## 7. Observaciones de Alineación o Riesgos
El CA-4329 (Telemetría de Desfase Comercial Sync Health) es un salvadidas de negocio valiosísimo. Si el RabbitMQ no pudo reconciliar la Lista de Precios contra SAP en toda la noche, avisar categóricamente al SysAdmin la mañana siguiente de que la comercialización corre sobre bases de ayer evita demandas y fraudes masivos dentro del Tenant.
