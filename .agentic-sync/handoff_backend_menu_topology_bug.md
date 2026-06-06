---
title: "Remediación Bug (UAT) - Menú Dinámico Vacío para SUPER_ADMIN"
agent: "Backend"
branch: "DevDavid"
us: "US-036"
---

# 1. Metadatos de la Delegación
*   **Rol Asignado:** Backend
*   **Rama de Trabajo:** `DevDavid`
*   **Bug Reportado:** El usuario `Super_Administrador` no visualiza ningún menú en el Frontend (retorna "Sin Topología de Menús").
*   **Alineación Arquitectónica:** Zero-Trust pero con Bypass Nativo para Roles Fundacionales (CA-27).

# 2. Diagnóstico Arquitectónico
En la iteración pasada, creaste `MenuLayoutService.java`. Su método `computeTopologyForUser(String username)` itera sobre los permisos asociados al rol. Sin embargo, debido a que el rol `SUPER_ADMIN` es inmutable y fundacional, no posee permisos explícitamente amarrados en la base de datos (o la semilla `DataSeeder` no se los asignó). Como resultado, la topología calculada es un array vacío `[]`.

# 3. Entregables Esperados (Plan de Remediación)
1.  **Modificar `MenuLayoutService.java`:** Intervenir el método `computeTopologyForUser`. Si el usuario posee el rol `SUPER_ADMIN` o `SYSTEM_ADMIN`, el servicio debe retornar inmediatamente la lista completa de los 7 `MACRO_MODULES` de forma implícita (Bypass lógico).
2.  **Pruebas Unitarias:** Actualizar `MenuLayoutServiceTest.java` (o crearlo si no existe) para asegurar que la regla de `SUPER_ADMIN` no se rompa en futuras refactorizaciones.

⚠️ **IMPORTANTE:** Todo desarrollo o configuración debe quedar exhaustivamente documentado. Se exige **PRECISIÓN QUIRÚRGICA** en cada cambio para evitar efectos colaterales.

> **Compilación obligatoria:** Ejecuta el protocolo Zero-Trust SRE documentado en `.agents/skills/backend_sre_compilation_audit/SKILL.md` (§0 a §2).

# 4. INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN
1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_backend.md`.
4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_backend.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.
