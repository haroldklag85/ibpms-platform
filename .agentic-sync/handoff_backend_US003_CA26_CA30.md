# Contrato de Arquitectura Backend (US-003 Iteración 6: CA-26 al CA-30)

**Rol:** Desarrollador Backend Java/Spring Boot (Arquitectura Hexagonal).
**Objetivo:** Desarrollar los mecanismos de base de datos y validaciones de red contra Camunda para proteger y versionar los diseños de formularios.

## 📋 Contexto y Criterios de Aceptación (Alcance Estricto):
En esta fase, la plataforma debe adquirir resistencia contra pérdidas de datos y auditoría pura (CA-26 y CA-27).

Debes abordar:
*   **CA-26 (Validación E2E contra Borrado Activo):** Modifica el endpoint de eliminación en `FormDesignController` (o agrégalo si no existe para la API de V1). Si se intenta enviar un reseteo profundo o DELETE, debes usar el cliente REST de Camunda (o Java API) para revisar el `HistoryService` y `RuntimeService`. Si el Process Definition ligado a este form tiene instancias de proceso *Activas*, tira una excepción de control (ej. `409 Conflict`) bloqueando el borrado.
*   **CA-27 (Control de Versiones y Clonador DB):** Cuando el frontend haga un UPDATE sobre un formulario (o guarde una versión nueva), guarda el payload anterior en una tabla secundaria, por ejemplo `form_design_versions`. Crea un endpoint `GET /api/v1/forms/{id}/versions` para que el frontend pueda listar las versiones pasadas.

## 📐 Reglas de Desarrollo:
1. Respeta la **Arquitectura Hexagonal**. (Inbound Adapters -> App Ports -> Domain -> Outbound Ports -> Secondary Adapters).
2. Para el CA-26, asegúrate de devolver un `ResponseEntity` conciso y entendible por la GUI.

## 🛑 MANDATO LOCAL GATEKEEPER (REGLA DE ORO)
Tienes **ESTRICTAMENTE PROHIBIDO** hacer `git commit` a la rama principal.
Al finalizar el código de API y servicios en Java, abre una terminal y bloquea tu código:
`git add . && git commit -m "chore(sync): save progress" && git push origin HEAD`

Notifícame textualmente apenas hayas completado el Stash.
