# Contrato de Arquitectura Backend (US-003 Iteración 12: CA-55 al CA-59)

**Rol:** Desarrollador Backend Java/Spring Boot.
**Objetivo:** Adaptar el DTO y el Storage para absorber metadatos estéticos avanzados (Multicolumnas) y la métrica técnica de uso del formulario (Tiempos y Movimientos).

## 📋 Contexto y Criterios de Aceptación (Alcance Estricto):
En esta Iteración 12 la carga recae fuertemente en el modelado visual, pero como sistema integral, el backend debe estar preparado:

*   **CA-55 (Layout Multicolumna):** El esquema del formulario (JSON guardado en BD `FormStorageController.java`) empezará a recibir objetos `container` anidados que incluirán variables como `columns: 2` o `columns: 4`. Asegúrate de que los Validadores de la base de datos documental (Postgre JSONB o JPA) no rechacen estos nodos estructurales de diseño visual.
*   **CA-58 (Componente Cronómetro/Timer):** El frontend emitirá dentro del Payload de la tarea (cuando se pulsa Enviar) una métrica de tiempo calculada ("Tiempo en Foco" o "Timer Manual"). Asegura que el `TaskCompleteRequest.java` que refactorizaste en la It.10 acepte enteros numéricos (`trackingSeconds`) generados por este nuevo componente sin disparar alertas de `StrictPrimitiveTyping`. 

## 📐 Reglas de Desarrollo:
1. Reafirma que la estrategia de `UnknownProperties` permita a los metadatos de telemetría de UI entrar al motor limpiamente.
2. Si tienes DTOs restrictivos para el esquema visual, actualiza el `FormFieldMetadataDTO.java` de backend para incluir `columns` (Integer) y `timerMode` (String).

## 🛑 MANDATO LOCAL GATEKEEPER (REGLA DE ORO)
Tienes **ESTRICTAMENTE PROHIBIDO** hacer `git commit` a la rama principal.
En cuanto blindes las validaciones DTO, congélalo:
`git stash save "temp-backend-US003-ca55-ca59"`

Informa textualmente al Arquitecto Líder apenas termine el empaquetado.
