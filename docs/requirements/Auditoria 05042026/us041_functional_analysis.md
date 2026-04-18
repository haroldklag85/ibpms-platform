# Análisis Funcional Definitivo: US-041 (Vista 360 del Cliente / Consolidación Global)

## 1. Resumen del Entendimiento
La US-041 proyecta visualmente la amalgama de la vida operativa de un cliente. Reúne proyectos Ágiles Paralelos y Cadenas de Procesos Seriales (BPMN) de Camunda en un solo Dashboard (Customer ID Pivot).

## 2. Objetivo Principal
Exponer el "Estado Maestro" o "Avance de Esfuerzo" del Lead de un solo vistazo, separando lo completado de lo volátil, e inyectando lógica de fallback (Micro-frontends degradation) si el Core CRM crashea.

## 3. Alcance Funcional Definido
**Inicia:** Ejecutivo hace Query por CRM_ID.
**Termina:** El sistema carga la grilla consolidada de instancias Camunda + Tableros Ágiles (US-005+008).

## 4. Lista de Funcionalidades Incluidas
- **Macro-Tracker Semaforizado (CA-3335):** Avance global basado en "Esfuerzo".
- **Degradación Elegante Cacheada (CA-3343):** Si hay timeout contra el CRM central, levanta metadata interna y sobrevive la caída emitiendo warning.
- **Filtro de Ruido Oculto (CA-3339):** Obliga aislar/ocultar comentarios entre analistas, dejando pasar a la traza 360 solo los eventos puros.

## 5. Lista de Brechas, Gaps o Ambigüedades Detectadas
- **Ficticio Avance Mapeado de 'Esfuerzo' en BPMN (⚠️ CA-3335):** Exige calcular un "Porcentaje de avance calculado explícitamente por **Esfuerzo**". **GAP:** En Agilidad (Scrum), el "Story Point" o "Esfuerzo" existe, pero Camunda Platform **no tiene el concepto de Esfuerzo Atómico nativo paramétrico por nodo**. Camunda solo tiene variables (`time_in_state` o `progress`). Pretender mapear analíticamente en un Gauge Radial un "Avance de Esfuerzo" obligaría a Hardcodear pesos porcentuales artificiales (`Task 1 = 30%`, `Task 2 = 70%`) en una tabla espejo, corrompiendo la dinámica viva de los workflows BPMN si un admin agrega nodos a mitad del año sin actualizar la matriz esotérica de contrapesos.
- **Fuga de PII por Reducción Viewport (⚠️ CA-3340):** Indica que "La Vista excluye comentarios". Si esta regla existe como bandera `v-if` condicional dentro del componente VUE (Vue Store), abriendo DevTools el humano filtrará PII. El Backend DEBE purgar/excluir los nodos privados en la Serialización de Spring Boot EntityToDTO; *jamás* entregar el Objeto Camunda Activo Base64 total a las capas bajas de la red interna si es para lectura Ejecutiva.

## 6. Lista de Exclusiones (Fuera de Alcance V1)
- Inteligencia predictiva del Cliente (Churn Rate) o Next Best Action (Todo analítico queda por fuera, es puramente Descriptivo Estado 360).

## 7. Observaciones de Alineación o Riesgos
Instanciar rápido desde esta vista (CA-3336) asumiendo por defecto que los casos son de ese cliente ahorrará 1.4 clics por caso, una notable mejora de eficiencia.
