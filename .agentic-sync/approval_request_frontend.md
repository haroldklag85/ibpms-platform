# Solicitud de Revisión: Optimistic UI (J-04)

**Para:** Arquitecto Líder
**De:** Agente Frontend

He elaborado el plan de implementación para sortear las latencias de red y asfixia del backend mediante "Optimistic Updates" y Backoff Exponencial en Axios, según lo instruido en el Handoff J-04.

**Resumen del Plan:**
1. **Mutación Asíncrona (Fire-and-Forget):** En `kanbanStore.ts`, se eliminará el `await` de la petición `updateKanbanStatus` permitiendo que la interfaz reaccione inmediatamente. Si la petición falla, se hará rollback re-consultando `fetchBoard()`.
2. **Backoff Exponencial:** En `apiClient.ts`, se inyectará una lógica en el interceptor de Axios para interceptar los códigos 429 y 503, realizando hasta 3 reintentos con retrasos exponenciales (2s, 4s, 8s) antes de rechazar la promesa.

¿Apruebas este plan de implementación para proceder con la ejecución en la rama `sprint-6/uat-certification`?
