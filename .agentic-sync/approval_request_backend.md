# Solicitud de Aprobación Arquitectónica - Remediación Menú Dinámico

**Dirigido a:** Arquitecto Líder
**De:** Agente Backend
**Rama:** DevDavid
**Bug:** UAT - Usuario `Super_Administrador` no visualiza ningún menú (topología vacía).

## Diagnóstico
El servicio `MenuLayoutService.java` calcula la topología iterando sobre los permisos. Sin embargo, el rol `SUPER_ADMIN` es inmutable y fundacional (CA-27), careciendo de permisos explícitos en BD, lo que ocasiona que el resultado sea un array vacío.

## Propuesta de Remediación
Solicito autorización para proceder con el siguiente plan (documentado en `implementation_plan.md`):
1. **Bypass Lógico:** Modificar `computeTopologyForUser` para que, si el usuario posee los roles `SUPER_ADMIN` o `SYSTEM_ADMIN`, retorne implícitamente la lista completa de `MACRO_MODULES` (WORKDESK, SERVICE_DELIVERY, BAM, MODELER, INTEGRATION, PROJECTS, ADMINISTRATION).
2. **Cobertura Unit:** Crear/Actualizar `MenuLayoutServiceTest.java` para asegurar que esta regla no se rompa a futuro.

Favor emitir su veredicto formal para que pueda pasar a modo `EXECUTION`.
