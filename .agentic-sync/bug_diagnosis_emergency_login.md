# 🔬 Diagnóstico Forense: Error 500 en Break-Glass Login e IDOR Blocked
**Bug-Fix Lead Report**

## 1. Síntomas Reportados
- Error HTTP 500 en la vista `BreakGlassLogin.vue` al invocar `/api/v1/auth/emergency-login`.
- Alerta de seguridad en logs del Backend: `AccessDeniedException: IDOR Blocked: Session does not belong to the current tenant` disparada por `RagSessionCleanerUseCase`.
- Advertencias de Hibernate sobre restricciones únicas faltantes (`uk_*`).

## 2. Capas Afectadas
- **Backend (⚙️ BACKEND)**: `UserService.java`, `UserRepository.java`, `RagSessionCleanerUseCase.java`
- **Frontend (🎨 FRONTEND)**: `apiClient.ts`, `BpmnDesigner.vue`
- **Infraestructura (🗄️ INFRA/BD)**: PostgreSQL (esquema `ibpms_security_user` duplicado)

## 3. Causa Raíz (Análisis Forense)

### A) Error 500 en `/auth/emergency-login`
El error se debe a una **falla de unicidad inducida por DDL Update**. 
1. `DataSeeder.java` inyecta al usuario root verificando el username `[Super_Administrador]`. Si este username es alterado posteriormente en BD, el seeder vuelve a inyectar `root@ibpms.local`.
2. Como la propiedad `spring.jpa.hibernate.ddl-auto=update` no impuso la restricción única retroactivamente por la existencia de duplicados (evidenciado por los warnings `uk_*`), el sistema permite múltiples registros con el email `root@ibpms.local`.
3. Cuando la UI invoca `userService.findByEmail("root@ibpms.local")`, el motor de Spring Data JPA intenta retornar un `Optional<UserEntity>`, pero al encontrar múltiples filas arroja un `NonUniqueResultException`. Esta excepción técnica no es interceptada formalmente en `GlobalExceptionHandler`, filtrándose como un `500 Internal Server Error`.

### B) Error `[APPSEC-IDOR-BLOCK]` en `RagSessionCleanerUseCase`
Este error obedece a un **Gap de Gobernanza y Handoff de QA**:
1. El script automatizado `handoff_s6_qa.md` inyecta hardcodeado el parámetro `?sessionId=session_beta_001`.
2. Cuando se ejecuta con credenciales de `tenant_alpha`, `RagSessionCleanerUseCase.java:34` detecta una discrepancia estructural y bloquea la purga por seguridad (IDOR).
3. Adicionalmente, la UI (`apiClient.ts` y `BpmnDesigner.vue`) tiene un bug estructural: el método `destroyCopilotSession()` se diseñó sin el parámetro obligatorio `sessionId`, y se invoca desde una Store inexistente (`integrationStore`), generando una asimetría entre UI y API.

## 4. Plan de Acción (Corrección Quirúrgica)

1. **Backend**: 
   - Refactorizar `UserRepository.java` para utilizar `findFirstByEmailOrderByIdAsc(String email)` mitigando el crash ante datos sucios.
   - Refactorizar `DataSeeder.java` para prevenir la reinserción si el email ya existe.
2. **Infraestructura/BD**: (Opcional para el DBA) Sanear los duplicados y forzar un `ALTER TABLE ADD CONSTRAINT`.
3. **Frontend**:
   - Reparar `apiClient.ts` añadiendo la firma correcta `(sessionId: string)` y el query string respectivo.
   - Reparar la inyección `useIntegrationStore` en `BpmnDesigner.vue` reemplazándola por `apiClient.destroyCopilotSession()`.
