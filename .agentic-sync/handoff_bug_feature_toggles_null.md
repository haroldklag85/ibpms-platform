# 🔬 Handoff Técnico: Diagnóstico y Resolución del Bug de Feature Toggles (`changed_by` NULL)

**Para:** Arquitecto Líder / Equipo de Desarrollo Backend  
**De:** Antigravity AI Coding Assistant  
**Asunto:** Diagnóstico de `DataIntegrityViolationException` en `/api/v1/workdesk/feature-toggles`  
**Estado:** Analizado y Listo para Implementación  

---

## 1. Síntomas y Logs del Error
Durante las pruebas en el ambiente E2E, se registró la siguiente excepción en los logs del sistema ([task-739.log](file:///C:/Users/HaroltAndr%C3%A9sG%C3%B3mezAgu/.gemini/antigravity/brain/3e7c0262-cc13-496b-bff5-21d9ca2181d7/.system_generated/tasks/task-739.log)):

```text
2026-05-29T01:03:06.109-05:00 ERROR 9328 --- [ibpms-core] [io-8080-exec-11] o.h.engine.jdbc.spi.SqlExceptionHelper   : ERROR: null value in column "changed_by" of relation "ibpms_feature_toggles" violates not-null constraint
  Detail: Failing row contains (bc7f0c53-20e4-43f0-8282-df6465dc0b70, 2026-05-29 01:03:06.042938, null, t, tenant_alpha, FORCE_ROUTING).
2026-05-29T01:03:06.124-05:00 ERROR 9328 --- [ibpms-core] [io-8080-exec-11] c.i.p.i.web.GlobalExceptionHandler       : 🚨 ERROR CRITICO DEL SISTEMA ENVIADO A ELK [TraceID: 1a922b92-7537-452d-8303-3030e6c592e5]: 
org.springframework.dao.DataIntegrityViolationException: could not execute statement
```

---

## 2. Causa Raíz (Análisis del Código)

El error ocurre al intentar actualizar un Feature Toggle que no existe previamente en la base de datos para el inquilino solicitado (e.g., `FORCE_ROUTING` para `tenant_alpha`).

1. **Definición de Esquema:** La tabla `ibpms_feature_toggles` tiene configurada la restricción `changed_by VARCHAR(100) NOT NULL`.
2. **Falla en el Servicio:** En la clase [FeatureToggleService.java](file:///C:/Users/HaroltAndr%C3%A9sG%C3%B3mezAgu/ProyectoAntigravity/ibpms-platform/backend/ibpms-core/src/main/java/com/ibpms/poc/application/service/FeatureToggleService.java#L59), el método `updateFeatureToggle` hace lo siguiente:
   ```java
   @Override
   @Transactional
   public boolean updateFeatureToggle(String tenantId, String toggleKey, Boolean enabled) {
       FeatureToggleEntity toggle = featureTogglePort.findByTenantIdAndToggleKey(tenantId, toggleKey)
               .orElseGet(() -> {
                   FeatureToggleEntity newToggle = new FeatureToggleEntity();
                   newToggle.setTenantId(tenantId);
                   newToggle.setToggleKey(toggleKey);
                   return newToggle; // <-- changedBy queda en NULL
               });
       
       boolean newValue = enabled != null ? enabled : false;
       toggle.setEnabled(newValue);
       featureTogglePort.save(toggle); // <-- Lanza DataIntegrityViolationException
   ```
   Como se observa, cuando el toggle es instanciado por primera vez dentro de `orElseGet`, el campo `changedBy` nunca se define, lo que provoca que Hibernate envíe una sentencia SQL con `changed_by = null`.

---

## 3. Propuesta de Corrección (Opciones)

### Opción A (Recomendada - Preserva la API actual)
Establecer un valor predeterminado como `"SYSTEM"` o `"ADMIN"` en el bloque `orElseGet` de [FeatureToggleService.java](file:///C:/Users/HaroltAndr%C3%A9sG%C3%B3mezAgu/ProyectoAntigravity/ibpms-platform/backend/ibpms-core/src/main/java/com/ibpms/poc/application/service/FeatureToggleService.java#L59) para garantizar que nunca sea nulo al crearse transicionalmente:

```java
        FeatureToggleEntity toggle = featureTogglePort.findByTenantIdAndToggleKey(tenantId, toggleKey)
                .orElseGet(() -> {
                    FeatureToggleEntity newToggle = new FeatureToggleEntity();
                    newToggle.setTenantId(tenantId);
                    newToggle.setToggleKey(toggleKey);
                    newToggle.setChangedBy("SYSTEM"); // Fallback de creación inicial
                    newToggle.setChangedAt(LocalDateTime.now());
                    return newToggle;
                });
```

### Opción B (Robusta - Pasa el usuario autenticado)
Modificar la firma de la interfaz del caso de uso [UpdateFeatureToggleUseCase.java](file:///C:/Users/HaroltAndr%C3%A9sG%C3%B3mezAgu/ProyectoAntigravity/ibpms-platform/backend/ibpms-core/src/main/java/com/ibpms/poc/application/port/in/UpdateFeatureToggleUseCase.java) y de [FeatureToggleService.java](file:///C:/Users/HaroltAndr%C3%A9sG%C3%B3mezAgu/ProyectoAntigravity/ibpms-platform/backend/ibpms-core/src/main/java/com/ibpms/poc/application/service/FeatureToggleService.java) para recibir el parámetro `changedBy`.

1. **En el Controlador:**
   ```java
   // FeatureToggleController.java
   String changedBy = (authentication != null) ? authentication.getName() : "SYSTEM";
   boolean enabled = updateFeatureToggleUseCase.updateFeatureToggle(tenantId, key, reqEnabled, changedBy);
   ```
2. **En el Servicio:**
   ```java
   toggle.setChangedBy(changedBy);
   toggle.setChangedAt(LocalDateTime.now());
   ```

---

## 4. Archivos Involucrados en el Gobierno Técnico
* **Entidad JPA:** [FeatureToggleEntity.java](file:///C:/Users/HaroltAndr%C3%A9sG%C3%B3mezAgu/ProyectoAntigravity/ibpms-platform/backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/jpa/entity/FeatureToggleEntity.java)
* **Caso de Uso Port:** [UpdateFeatureToggleUseCase.java](file:///C:/Users/HaroltAndr%C3%A9sG%C3%B3mezAgu/ProyectoAntigravity/ibpms-platform/backend/ibpms-core/src/main/java/com/ibpms/poc/application/port/in/UpdateFeatureToggleUseCase.java)
* **Servicio Orquestador:** [FeatureToggleService.java](file:///C:/Users/HaroltAndr%C3%A9sG%C3%B3mezAgu/ProyectoAntigravity/ibpms-platform/backend/ibpms-core/src/main/java/com/ibpms/poc/application/service/FeatureToggleService.java)
* **Controlador REST:** [FeatureToggleController.java](file:///C:/Users/HaroltAndr%C3%A9sG%C3%B3mezAgu/ProyectoAntigravity/ibpms-platform/backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/web/FeatureToggleController.java)
