# Handoff Frontend - Cierre Deuda Técnica (US-043: CA-6)

## Propósito
Explotar visualmente la variable de *Early Warning* inyectada por el Backend para que el operario reaccione antes del vencimiento legal.

## Criterios de Aceptación Cubiertos (Frontend)
* **CA-6 (Alertas Preventivas / Visualización en Workdesk):** 
    - La grilla de Tareas (Inbox/Workdesk) lee periódicamente las variables de la tarea.
    - Si el DTO de la tarea incluye el flag `isSlaAtRisk: true`, la fila de dicha tarea mutará de estado neutral a un estado de alerta **Amarillo/Naranja** severo (mientras que las Vencidas usan Rojo).
    - Se incluirá un *Tooltip* o *Badge* parpadeante indicando: "⚠️ SLA en Riesgo (<20% restante)".

## Tareas Vue (Prioridad 2 - Suspendido hasta Fase 1)
1. Modificar el componente de Grilla de Tareas (`InboxView` / `Workdesk`).
2. Implementar la validación condicional CSS o Tailwind basada en el atributo `isSlaAtRisk`.
3. Adicionar los iconos de advertencia en la experiencia del usuario.
