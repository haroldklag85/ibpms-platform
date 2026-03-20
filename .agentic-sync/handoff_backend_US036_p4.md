# Handoff Backend - Iteración 39 (US-036: CA-16 a CA-18)

## Propósito
Esta es la fase de Fiscalización y Auditoría del Gestor de Identidades (Identity Governance). Se incorporará la capacidad de exportar las matrices a formato analítico y se guardará la Inmutabilidad de las asignaciones de poder.

## Criterios de Aceptación Cubiertos (Backend)
* **CA-16 (Informes Densos de Fiscalización):** Crear el endpoint `GET /api/v1/admin/roles/export`. El backend debe cruzar `UserEntity`, `RoleEntity` y `ProcessPermissionEntity`, compilando un archivo CSV (o Excel ligero) usando Apache Commons CSV o similar. Debe retornar el archivo crudo con `Content-Type: text/csv`.
* **CA-17 (Traza Indeleble de Otorgamiento):** Crear la entidad `RoleAuditLogEntity` (o análoga). Todo llamado a la asignación de roles/permisos (`POST /roles`) debe guardar quién fue el Admin ejecutante (`SecurityContextHolder.getContext()`), el `Timestamp` y el Delta JSON exacto de permisos otorgados o quitados. Exponer un endpoint de lectura para el Frontend.
* **CA-18 (Segregación Automática SoD):** **[NO-OP ARCHITECTURAL]** El escenario delega a la **V2** la creación de motores anti-colisión de "Quien hace no aprueba". El backend en esta V1 asumirá este riesgo y **NO** implementará barreras lógicas que impidan lanzar y aprobar el mismo trámite. 

## Tareas Java (Prioridad 1 - Tarea Bloqueante)
1. Implementar `RoleAuditLogEntity` interceptando las mutaciones de Rol.
2. Construir generador CSV/Excel de la Sábana de Permisos.
