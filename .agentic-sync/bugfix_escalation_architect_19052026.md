# 🔧→🧠 Escalamiento de Bug-Fix al Arquitecto Líder

**Bug:** Error HTTP 500 en BreakGlassLogin e IDOR Blocked Exception en consola backend
**US/CA afectado:** US-000 (General Security & Bootstrap)
**Rama:** `bugfix/DevDavid-emergency-login`
**Agente ejecutor:** Bug-Fix Lead / Full-Stack
**Archivos modificados:**
- `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/jpa/repository/security/UserRepository.java`
- `backend/ibpms-core/src/main/java/com/ibpms/poc/application/service/security/UserService.java`
- `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/startup/DataSeeder.java`
- `frontend/src/services/apiClient.ts`
- `frontend/src/views/admin/Modeler/BpmnDesigner.vue`
**Certificación Bug-Fix Lead:** ✅ PASS

## Resumen de la Corrección

1. **Backend - Mitigación del 500 Error**: El error 500 fue provocado por el método `UserRepository.findByEmail` retornando una `NonUniqueResultException` cuando la tabla de base de datos tenía múltiples registros `root@ibpms.local` debido a desincronizaciones de `DataSeeder` (alteraciones manuales del username sumado a una política `ddl-auto=update` que falló en aplicar retroactivamente el constraint unique `uk_*`). Se actualizó a `findFirstByEmail` para dotar de resiliencia al sistema (fail-safe) y se ajustó el seeder para verificar duplicidad por email antes de insertar, previniendo el crecimiento de la deuda técnica de datos sucios.
2. **Frontend - Prevención de Spam de IDOR y TypeError**: Se identificó que la llamada a `destroyCopilotSession` carecía del parámetro `sessionId` requerido, y que `BpmnDesigner.vue` intentaba llamar a la función inexistente `integrationStore.destroyCopilotSession` en los hooks del ciclo de vida (generando bugs funcionales silenciados del lado del cliente). Se corrigió la firma del método en `apiClient.ts` y se aplicó una protección contra invocaciones en vacío dentro del designer.

## Solicitud
Se solicita la doble certificación del Arquitecto Líder para confirmar que el parche no viola ADRs ni introduce deuda técnica, y que está habilitado para realizar merge a DevDavid (o main si corresponde).
