# Análisis Funcional Definitivo: US-043 (Configuración Global de SLA y Business Calendar)

## 1. Resumen del Entendimiento
La US-043 define el motor del tiempo corporativo. Aborda la problemática de que los procesos BPMN corren cronómetros (Timers) absolutos (24/7) mientras que la vida real humana funciona con vacaciones, fines de semana y zonas horarias, configurando penalizaciones operativas y "Time-Warps".

## 2. Objetivo Principal
Inyectar un Calendario de Negocios (Business Calendar) customizado en el Job Executor de Camunda que pause, recálcule y lance los temporizadores relativos, garantizando justicia de medición (BAM).

## 3. Alcance Funcional Definido
**Inicia:** PMO configura días hábiles (Ej: L-V 8 a 17) en Matriz.
**Termina:** El BPMN intercepta eventos de due-date y frena cronómetros el viernes a las 17:00, reanudándolos el Lunes a las 08:00 AM, incluyendo alertas preventivas 80%.

## 4. Lista de Funcionalidades Incluidas
- **Inyección de Custom Business Calendar (CA-4043):** Alteración profunda en el Engine de Camunda para anular el Quartz o temporizador por defecto a fin de calcular fechas.
- **Bypass en Timers de Sistemas (CA-4051):** Procesos no humanos (Jobs MLOps) usan Property XML `isBusinessSla=false` y Camunda los ejecuta domingos o madrugadas ininterrumpidamente.
- **Soporte Timezones Híbridos (CA-4064):** Resuelve el conflicto Geográfico evaluando prioridades: UTC Trabajador > Candidate Group.
- **Early Warnings 80% (CA-4075):** Lanza webhooks automáticos antes de la ejecución legal obligatoria del Evento BPMN final (Ej: Dispara escalamientos a C-Level).
- **Fallback Gobernamental API (CA-4070):** Lista de Festivos Automática conectada a servicio público, o editable manual por si la API perece.

## 5. Lista de Brechas, Gaps o Ambigüedades Detectadas
- **Contradicción Arquitectónica Catastrófica de Motor Camunda (⚠️ CA-4061):** **GAP LETAL**. El recálculo retroactivo afirma que *"el sistema iterará la tabla ACT_RU_JOB actualizando los DUEDATE_ en lotes paginados SQL"*. Esto es un indicio irrevocable de que el arquitecto funcional estaba diseñando sobre **Camunda 7 (Arquitectura Spring Boot Monolítica Relacional con SQL)**. En **Camunda 8 (Zeebe)**, no existe una base de datos relacional transaccional accesible por SQL. Zeebe utiliza RocksDB interno y Stream de Eventos en tiempo real (gRPC). **No existe la tabla `ACT_RU_JOB` en C8**. Intentar alterar Due Dates de timers vivos en Camunda 8 requiere consumir el Zeebe API Endpoint `UpdateJobRetries` o `UpdateTimer`, lo cual es extremadamente complejo en vuelo. Este criterio condena al Backend a una implementación inviable. DEUDA ARQUITECTÓNICA URGENTE.

## 6. Lista de Exclusiones (Fuera de Alcance V1)
- Revertir retroactivamente tiempos transcurridos en el pasado (Solo calcula hacia el futuro en los vivos).

## 7. Observaciones de Alineación o Riesgos
Un Job "Batch masivo" recorriendo miles de instancias BPMN es mortal a nivel Heap-Memory si no de pagina rigurosamente; requeriría Spring Batch o RabbitMQ Aggregation, que está bien contemplado. El problema cardinal es C7 vs C8.
