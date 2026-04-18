# Handoff Backend — Remediación CA-12 (Iteración 74-DEV)
## US-005 | Rama: `sprint-3/informe_auditoriaSprint1y2`

---

## Contexto (Orden del Arquitecto Líder)

La auditoría de CA-12 (Versionamiento Seguro de Reglas DMN) reveló que el backend **YA tiene implementada** la validación Pre-Flight en `PreFlightAnalyzerService.java` (líneas 208-221). Sin embargo, se requiere un **hardening menor** para alinear el mensaje de warning con la semántica SSOT y asegurar que el default explícito sea `deployment`.

> **Referencia de Auditoría:** `auditoria_us005_ca12_dmn_binding.md`
> **SSOT:** `docs/requirements/v1_user_stories.md` (CA-12 L2165-2171)
> **Archivo:** `backend/ibpms-core/src/main/java/com/ibpms/poc/application/service/PreFlightAnalyzerService.java`

---

## Tarea 1 — Hardening del Warning Pre-Flight para BusinessRuleTask (CA-12)

**Método afectado:** `analizar(InputStream bpmnStream)` — Líneas 208-221

**Estado actual:** La validación existe y funciona. Solo se requiere ajustar el mensaje para alinearlo con la semántica SSOT de "protección de derechos adquiridos".

**Cambio solicitado:**

```java
// ANTES (líneas 208-221):
// CA-12: Late Binding DMN por Defecto en BusinessRuleTasks
Collection<BusinessRuleTask> brTasks = modelInstance.getModelElementsByType(BusinessRuleTask.class);
for (BusinessRuleTask brt : brTasks) {
    String decisionRef = brt.getCamundaDecisionRef();
    String binding = brt.getCamundaDecisionRefBinding();
    String version = brt.getCamundaDecisionRefVersion();
    
    if (decisionRef != null && !decisionRef.isBlank()) {
        if ((binding == null || (!binding.equals("latest") && !binding.equals("deployment"))) && 
            (version == null || version.isBlank())) {
            response.addWarning(brt.getId(), "BusinessRuleTask enlaza a un DMN (" + decisionRef + ") pero carece de camunda:decisionRefBinding='latest' o version explícita. Puede generar inconsistencias de resolución.");
        }
    }
}

// DESPUÉS:
// CA-12: Versionamiento Seguro de Reglas DMN (Protección de Derechos Adquiridos)
Collection<BusinessRuleTask> brTasks = modelInstance.getModelElementsByType(BusinessRuleTask.class);
for (BusinessRuleTask brt : brTasks) {
    String decisionRef = brt.getCamundaDecisionRef();
    String binding = brt.getCamundaDecisionRefBinding();
    
    if (decisionRef != null && !decisionRef.isBlank()) {
        if (binding == null || binding.isBlank()) {
            response.addWarning(brt.getId(),
                "⚠️ BusinessRuleTask '" + (brt.getName() != null ? brt.getName() : brt.getId()) +
                "' enlaza a DMN (" + decisionRef + ") sin camunda:decisionRefBinding configurado. " +
                "El motor asumirá 'latest' por defecto, lo cual puede violar la protección de derechos adquiridos (CA-12). " +
                "Recomendación: Configure 'deployment' en el Modeler para garantizar que los casos en vuelo se evalúen con la versión DMN vigente al nacer el caso.");
        } else if ("latest".equals(binding)) {
            response.addWarning(brt.getId(),
                "ℹ️ BusinessRuleTask '" + (brt.getName() != null ? brt.getName() : brt.getId()) +
                "' usa Late Binding (LATEST). Los casos en vuelo se evaluarán con la última versión DMN publicada. " +
                "Confirme que este comportamiento es intencional y no viola compromisos contractuales.");
        }
        // binding="deployment" → OK silencioso (default seguro)
    }
}
```

---

## Verificación Obligatoria
1. Compilar exitosamente: `mvn clean compile -pl ibpms-core`
2. Ejecutar tests existentes: `mvn test -pl ibpms-core` — 0 failures
3. Verificar que el Pre-Flight emite warning cuando un BRT no tiene binding configurado

---

## Restricciones Arquitectónicas
1. **NO crear entidades JPA nuevas.** CA-12 opera exclusivamente sobre el XML BPMN parseado.
2. **NO modificar endpoints REST.** La validación vive en el servicio Pre-Flight existente.
3. **No usar** `git stash`. Solo `git commit` + `git push`.
4. **Convención de commit:**
   - `fix(US-005): CA-12 harden Pre-Flight DMN binding warning with SSOT-aligned messaging`

---

> **INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
> 1. **EJECUCIÓN PARALELA:** Puedes ejecutar en paralelo con el Frontend (no hay dependencia cruzada para este CA).
> 2. Ejecuta el cambio y crea un commit atómico.
> 3. Compila y verifica tests después del commit.
> 4. Rama: `sprint-3/informe_auditoriaSprint1y2`
> 5. Al finalizar, dile al Humano: *"Humano, he remediado CA-12 Backend (Pre-Flight hardening). Entrégale este mensaje al Arquitecto Líder."*
