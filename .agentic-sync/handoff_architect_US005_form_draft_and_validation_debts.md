# Handoff de Deuda Técnica Arquitectónica: Sincronización de Borradores y Validación Cruzada de Formularios

**Emitido por:** COPILOTO IA / AGENTE DE DESARROLLO
**Destinatario:** ARQUITECTO LÍDER / EQUIPO DE DESARROLLO
**Prioridad:** 🔴 Alta (Bloqueante para Estabilidad de Producción)
**Áreas Afectadas:** Backend (Core API, Validadores Pre-Flight) y Frontend (Stores de Vistas)
**Historias de Usuario:** US-005 (BPMN Lifecycle), US-003 (Form Engine), US-024 (Zero-Bypass Form Start)

---

## 🔍 Hallazgo 1: Conflictos en Auto-Save de Borradores (Drafting Local vs. API)

### Descripción del Problema
Actualmente, existe un desacoplamiento entre el método HTTP utilizado por el cliente del frontend y el configurado en los controladores del backend para la sincronización remota de borradores de formularios:
1.  **Frontend**: La store encargada del formulario genérico del Workdesk (`useGenericFormStore`) realiza un guardado automático periódico utilizando el método `PUT` hacia la ruta de borradores (`/drafts/{taskId}`).
2.  **Backend**: El controlador del backend (`TaskDraftApiController`) define la recepción de esta misma ruta exclusivamente bajo el método de creación `POST` (`@PostMapping`).
3.  **Impacto**: Cada vez que el autoguardado del frontend se dispara, el backend rechaza la petición con un error `HTTP 405 Method Not Allowed`. La store captura este error de red y degrada graciosamente el estado a guardado local únicamente (`LocalStorage`), bloqueando silenciosamente el respaldo centralizado en el servidor.
4.  **Inconsistencia Interna**: La API de integración del backend expone el guardado de borradores de procesos con el verbo `PUT` en otros controladores, generando incoherencias de diseño REST.

### Solución Acordada (Estrategia de Alineación)
*   **Cambio en Backend**: Modificar el endpoint en el controlador del backend (`TaskDraftApiController`) para utilizar la anotación `@PutMapping("/drafts/{taskId}")` en lugar de `@PostMapping`. Semánticamente, un borrador de tarea se actualiza de forma idempotente con los cambios parciales del operario, haciendo de `PUT` el verbo correcto.
*   **Cambio en Frontend**: Unificar las referencias del cliente de API en el frontend para apuntar ordenadamente a este método y asegurar que la store limpie las alertas de desconexión al recibir un estado exitoso del servidor.

---

## 🔍 Hallazgo 2: Falta de Validación Cruzada en Pre-Flight Checks (Catálogo)

### Descripción del Problema
El validador de diagramas del backend (`CamundaBpmnValidationAdapter`) realiza validaciones estructurales de los procesos antes de permitir el despliegue (como asegurar que no haya nodos huérfanos o ciclos infinitos). Sin embargo:
1.  **Validación Superficial**: Para las propiedades `formKey` en User Tasks o Start Events, el validador únicamente verifica que el atributo de texto no sea nulo ni vacío en el XML.
2.  **Omisión del Catálogo**: El validador es agnóstico a la persistencia del sistema; no realiza consultas contra la base de datos de definiciones de formularios (`form_definitions`) para asegurar que el formulario de enlace realmente exista y esté activo.
3.  **Riesgo**: Un analista o arquitecto puede ingresar una clave de formulario inválida, desactualizada o eliminada de forma manual en el XML, desplegar el proceso exitosamente, y causar caídas críticas en tiempo de ejecución (HTTP 404) cuando los operarios intenten reclamar la tarea en el Workdesk.

### Solución Acordada (Estrategia de Alineación)
*   **Implementación de Validación Cruzada (Hard-Stop)**: Modificar el validador en el backend (`CamundaBpmnValidationAdapter`) para inyectar el puerto o servicio del catálogo de formularios.
*   **Severidad**: Configurar la validación como un `ERROR` duro (Hard-Stop). El motor debe denegar el despliegue del proceso BPMN si el validador no encuentra el identificador del formulario referenciado en estado activo dentro del catálogo de iBPMS. Esto garantiza la integridad referencial y previene fallos transaccionales en producción.
