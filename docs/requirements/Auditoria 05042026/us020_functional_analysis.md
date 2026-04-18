# Análisis Funcional Definitivo: US-020 (Estrategias de Sincronización Flexible Schedulers)

## 1. Resumen del Entendimiento
La US-020 complementa a la US-019 definiendo los parámetros de motor de Job Execution (Scheduling & Telemetry) para ejecutar con seguridad descargas masivas de data externa.

## 2. Objetivo Principal
Balancear la frescura de datos del producto comercial vs Estrés del Gateway corporativo durante el Core Banking Business Hour.

## 3. Alcance Funcional Definido
**Inicia:** Un Cron Spring Trigger se enciende a las 23:00.
**Termina:** El Catálogo está actualizado, la caché purgada (Flush) e inyectada vía Push al navegador de los Analistas in-vivo sin Refresh.

## 4. Lista de Funcionalidades Incluidas
- **Cron Nocturno (CA-1):** Full-Fetch con commit masivo de datos (Altas y bajas) a `ibpms_service_catalog`. Registra `ibpms_audit_log` de estado del batch.
- **Dead-Letter Resiliency Batch (CA-2):** Falla el HTTP por Timeout en la mitad -> Evita commits incompletos -> Apila en RabbitMQ con Trazabilidad Retry (3 iteraciones, 15m) -> Falla -> Aborto Limpio Transaccional.
- **Cool-Down Pánico Manual (CA-3):** El SysAdmin empuja el Request en caliente de Día y se le bloquea el botón 15 min por Seguridad Rate Limit (Anti-DDoS al endpoint del cliente).
- **Socket Invalidation Refresh (CA-4):** WebSockets para repintar las grillas de toda la población al cambiar el catálogo remoto de día.

## 5. Lista de Brechas, Gaps o Ambigüedades Detectadas
- **Alucinación Funcional de Arquitectura Front-End (⚠️ CA-4):** Exige "*despachar un evento WebSocket hacia los clientes conectados para que los menús desplegables re-rendericen automáticamente SIN exigir F5*". **GAP:** Es un Capricho Tecnológico Desproporcionado (Tech Debt voluntario). Sostener tuberías TCP Múltiples (WebSockets o SSE) forzosamente abiertas hacia miles de navegadores SPA `Vue.js/React` exclusivamente para esperar que una "Tabla de Diccionario Administrativa" cambie (evento que ocurre 1 vez al mes en pleno día laboral), requiere infraestructura en la nube dedicada solo para Socket Routing (SignalR / Redis PubSub). El costo/complejidad vs valor del CA-4 es fatal para V1. Un simple invalidado `SWR (State-While-Revalidate)` Fetch al enfocar la pestaña usando `Vue Query / Apollo` resolvería esto por milisegundos gratis mediante HTTP Stateless sin quemar memoria RAM de WebSockets.

## 6. Lista de Exclusiones (Fuera de Alcance V1)
- Sincronización parcia asíncrona "CDC" (Change Data Capture) o Webhooks Outbound del CRM (El iBPMS es quien tira (Pulling/Fetch), no funciona a través del CRM tirando eventos PUSH (Webhook) al iBPMS V1 para mantener simplicidad).

## 7. Observaciones de Alineación o Riesgos
**Fricción de Inmensidad de Datos (CA-2):** "Full Fetch del catálogo". Los CRMs corporativos como Dynamics/SFDC pueden contener miles de atributos/productos obsoletos. Si no existe una bandera lógica de paginación en el CRM, el Fetch de las 23:00 podría colapsar con un String JSON de 200MB fallando por Heap Java Memory Space un "Lote Transaccional Fuerte" de ORM Hibernate. Requerirá cursores y particionado SQL local.
