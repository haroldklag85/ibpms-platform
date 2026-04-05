# Análisis Funcional Definitivo: US-008 (Mover Tarjeta en Tablero Kanban)

## 1. Resumen del Entendimiento
La US-008 define toda la experiencia operativa de un analista gestionando su trabajo no-estructurado mediante un tablero visual Kanban. Integra reactividad WebSockets, cobro de tiempo manual (Time Tracking) vs Reloj de SLA, y arquitectónicamente prohíbe el uso de CMMN (Case Management de Camunda) a favor de un CRUD rápido relacional (Spring JPA) para velocidad.

## 2. Objetivo Principal
Dar fluidez absoluta (Single Page Application UX) al operario. Permite arrastrar tareas, registrar horas facturables y gestionar dependencias burocráticas (invocar BPMNs pesados desde tarjetas ágiles) sin perder los tiempos analíticos.

## 3. Alcance Funcional Definido
**Inicia:** Desde que la tarjeta se visualiza en la Columna TODO.
**Termina:** Cuando aterriza en DONE, se inmutabiliza su formulario y se sella su reloj de tiempos; o es interceptada por un Flujo BPMN estructurado (Polimorfismo).

## 4. Lista de Funcionalidades Incluidas
- **Websockets Puros:** Transmisión de estados en tiempo real sin polling.
- **Trazabilidad Bloqueante:** Modal mandatorio de "Por Qué" al arrastrar a BLOCKED.
- **Desacople SLA/Timer:** SLA en contra es global e implacable. Time Tracking es manual acumulativo y apagable, registrando en `ibpms_time_logs`.
- **Append-Only Financiero:** Prohibición absoluta de borrar registros de tiempo ya reportados.
- **Arquitectura DAG Limpia vs BPMN:** Empleo de API REST PATCH de alta velocidad, pero con capacidad de invocar un Workflow pesudo (BPMN Asíncrono) para aprobaciones formales (Salto Híbrido).
- **Gobernanza de Tableros:** Máximo 7 columnas en el tablero.

## 5. Lista de Brechas, Gaps o Ambigüedades Detectadas
- **Limbo Event-Driven (⚠️ CRÍTICO CA-7):** El Salto Híbrido expone que una tarjeta Kanban invoca a un BPMN asíncrono para, por ejemplo, solicitar una "Macro-Aprobación Gerencial", y queda esperando el evento de retorno. ¿Qué pasa si el BPMN dura estancado 6 meses? Como el CA-1 dicta que "los SLA no se congelan", la tarjeta ágil se desangrará en métricas negativamente afectando al equipo, sin que ellos tengan culpa ni botón para abortar o exigir Timeouts transaccionales al Gateway de Camunda. Falta un mecanismo de TimeOut/Fallback desde el mundo Ágil al Estructurado.
- **Pérdida Transaccional de Timer por Transición (⚠️ MEDIO CA-3):** Si la tarjeta se mueve a DONE, el motor "apaga definitivamente" el Timer. Al transicionar (Drag & Drop) sin apretar *STOP* explícito antes, ¿el Frontend dispara un evento de "flush_time" o descarta brutalmente los minutos en caché (Thread/Vue State)? La omisión de un Hook de guardado forzoso previo al cambio de estado derivará en pérdida de horas facturables.

## 6. Lista de Exclusiones (Fuera de Alcance V1)
- Soporte CMMN (Rechazado en V1 para evitar fatiga de motor).
- Limitación temporal WIP (Work in Progress Limit) no se enuncia explícita, solo Max de Columnas (7).

## 7. Observaciones de Alineación o Riesgos
**Riesgo Operativo:** Es imperativo establecer directrices de mitigación para la inyección de Horas Falsas en caso de que un analista olvide apagar el Timer y este sume 72 horas el fin de semana. El Apped-Only exige auditar muy bien la frontera de corrección.
