# Análisis Funcional Definitivo: US-031 (Planificación y Ejecución de Proyecto Tradicional / Gantt)

## 1. Resumen del Entendimiento
La US-031 define el Planner Maestro para la metodología Cascade/Tradicional. Materializa el despliegue de tareas usando lógica DAG estricta (Dependencias Fin-Inicio), Semáforos de Sobrecarga de recursos humanos, re-diagramación dinámica y el manejo férreo de Líneas Base auditables para control gerencial e inyección viva al BPMN transaccional.

## 2. Objetivo Principal
Darle a un Project Manager una topología Gantt viva en la Pantalla 10.B que reaccione matemáticamente al contexto humano (Festivos y Horas Hábiles), a la vez que permite inyecciones a la vena de procesos en vuelo (Hot-Swaps) sin desconectar el andamiaje del modelo backend que rige los SLAs.

## 3. Alcance Funcional Definido
**Inicia:** Tras la instanciación de un Proyecto de marco Tradicional.
**Termina:** En el congelamiento de "Líneas Base" auditables, la sobreposición del progreso diario, y la auto-compensación algorítmica de tiempos.

## 4. Lista de Funcionalidades Incluidas
- **Adaptatividad Matemática Gantt (CA-1):** Suma forzosa de "Longitud Geométrica" (Stretching) si la tarea colisiona transversalmente con Días Festivos Globales.
- **Protección DAG anti-ciclos (CA-2):** El Gantt (WebClient) rechaza estructuralmente ciclos relacionales infinitos (A depende de B y B depende de A).
- **Semáforo Cognitivo (CA-3):** Advierte mediante balizas rojas sin bloquear (Soft-Alert) cuando un PM intenta asignar un empleado sobre el 100% de su capacidad en la semana transcurrida.
- **Reprogramación y Línea Base Mutable (CA-4):** Permite reajustar los tiempos creando forzosamente Snapshot `V+N` reteniendo la desviación contra la primera predicción.
- **Hot-Swapping de Cabina Viva (CA-5):** Si Re-asigno a "Pedro" por "Luis" en el Gantt, el motor BPMN le quita síncronamente el Token en la bandeja viva de Pedro y se la entrega a Luis, evitando crasheos o estancamientos.
- **Apropiación Híbrida (CA-6):** Soporta inyección "Directa a Usuario" o delegación perimetral a "Pool de Grupo".

## 5. Lista de Brechas, Gaps o Ambigüedades Detectadas
- **Fricción Asincrónica Calendario vs Tarea (⚠️ CRÍTICO CA-1/CA-5):** CA-1 dicta auto-compensación contra "Días Festivos". CA-5 permite el transplante (Hot-Swap) de usuarios a mitad del vuelo. Si el T4 (10 Días) transiciona bruscamente de "Pedro_Colombia (Festivos latinos)" a "Luis_España (Días hábiles puros)", **la US No Especifica el Recálculo Póstumo**. ¿El Gantt recalcula el SLA residual sobre la marcha inyectando/removiendo feriados de Luis, y mutando la V2 de Línea base, o la hereda ciegamente asumiendo el calendario del antecesor? Este GAP destruirá predicciones financieras multiregionales.

## 6. Lista de Exclusiones (Fuera de Alcance V1)
- Lógica de Análisis Camino Crítico (PERT) explícitamente rechazada (CA-7).
- Indicadores Earned Value Management (EVM) financieros diferidos a V2 (CA-8).

## 7. Observaciones de Alineación o Riesgos
**Riesgo Fuerte:** Mantener la sincronía entre un render Vue de Gantt interactivo (X,Y) y el motor Camunda de asignaciones requerirá WebSockets feroces entre el `ibpms_planner` (JPA) y `ibpms_bpm_engine` o la arquitectura decaerá en inconsistencias de estado.
