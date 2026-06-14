# 🧠→⚙️🎨 Handoff: 🧠 ARQUITECTO LÍDER → ⚙️ BACKEND - JAVA & 🎨 FRONTEND - VUE
# T-02: Modeler Bugfix - Persistencia de Borrador en Procesos Nuevos (v0)

**Emitido por:** [🧠 ARQUITECTO LÍDER]
**Destinatario:** [⚙️ BACKEND - JAVA] & [🎨 FRONTEND - VUE]
**Fecha:** 2026-06-10T19:50:00-05:00
**Sprint:** 6 — Iteración 1
**Prioridad:** 🔴 Alta
**Dependencia:** T-01 completada

---

## 📖 LECTURAS OBLIGATORIAS (Ejecutar ANTES de codificar)

```bash
# 1. Política del Enjambre (Leyes Globales)
cat .cursorrules

# 2. Skills principales de compilación y validación SRE
cat .agents/skills/backend_sre_compilation_audit/SKILL.md
cat .agents/skills/frontend_build_audit/SKILL.md

# 3. Skills transversales y de disciplina técnica requeridos
cat .agents/skills/clean_code_standards/SKILL.md
cat .agents/skills/yudhi_architecture_compliance/SKILL.md
cat .agents/skills/zero_mock_enforcement/SKILL.md

# 4. Handoff de la tarea actual
cat .agentic-sync/handoff_US005_creacion_procesos.md
```

> ⚠️ **LEY GLOBAL 3 — Trazabilidad Inversa:** TODO el código nuevo DEBE llevar la anotación @Traceability o comentario `// @Traceability: US-005, CA-15`. Esto es INNEGOCIABLE.

---

## 🔬 Diagnóstico del Arquitecto

Al iniciar el lienzo del modelador desde cero, el frontend solo inicializa el estado reactivo local (`processId = 'solicitud-tc3'`) y actualiza la URL, pero no registra el proceso en el backend. Cuando el auto-guardado en background realiza una petición `PUT /api/v1/design/processes/solicitud-tc3/draft`, el backend intenta buscar la entidad relacional por su `technicalId`. Al no encontrarla, lanza una excepción `IllegalArgumentException`, la cual retorna un código HTTP 400. El frontend captura el error y asume ciegamente que es una falla de red, mostrando una alerta de "Modo Offline" falsa.

| Violación/Hallazgo | Ubicación | Detalle |
|--------------------|:---------:|---------|
| Excepción por Proceso Inexistente en Guardado | `BpmnDesignService.java:110` | `guardarBorradorPorTechnicalId` hace lookup por `processKey`. Si no lo encuentra, lanza un `orElseThrow(...)` que resulta en HTTP 400. |
| Toast de Error de Red Indiscriminado | `BpmnDesigner.vue:3413` | El bloque `catch` en `saveDraft()` muestra incondicionalmente el banner de error de red sin evaluar el código HTTP. |

---

## 🎯 Instrucciones Quirúrgicas

### Paso 1: Auto-creación de Borrador en el Backend

**Archivo:** `backend/ibpms-core/src/main/java/com/ibpms/poc/application/service/BpmnDesignService.java`

Modificar el método `guardarBorradorPorTechnicalId` para auto-crear el proceso si no se encuentra en la base de datos:

```java
    // @Traceability: US-005, CA-15
    @Traceability(US = "US-005", CA = {"CA-10"})
    public void guardarBorradorPorTechnicalId(String processKey, String xml, String userId) {
        BpmnProcessDesign domain = designPort.findByTechnicalId(processKey)
                .orElseGet(() -> {
                    // Si el proceso es nuevo, lo creamos automáticamente en estado BORRADOR con versión 0
                    String processName = capitalizeTechnicalId(processKey);
                    return BpmnProcessDesign.crear(
                            processName,
                            BpmnProcessDesign.FormPattern.SIMPLE,
                            userId
                    );
                });
        domain.updateDraft(xml);
        designPort.save(domain);

        auditPort.logAction(domain.getId(), "SAVE_DRAFT", userId, domain.getCurrentVersion(), null);
    }

    // Método auxiliar para capitalizar slugs
    private String capitalizeTechnicalId(String slug) {
        if (slug == null || slug.isEmpty()) return "Proceso Sin Título";
        return java.util.Arrays.stream(slug.split("-"))
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
                .collect(java.util.stream.Collectors.joining(" "));
    }
```

Modificar también la firma del constructor del modelo de dominio si se requiere instanciarlo con el slug técnico correspondiente. En `BpmnProcessDesign.java`, verificar los métodos de creación para asegurar compatibilidad.

### Paso 2: Evaluación Semántica de Errores en el Frontend

**Archivo:** `frontend/src/views/admin/Modeler/BpmnDesigner.vue`

Modificar `saveDraft()` para evaluar si el error es de conexión o de aplicación:

```typescript
// @Traceability: US-005, CA-15
const saveDraft = async () => {
  if (!modelerInstance) return;
  try {
    const { xml } = await modelerInstance.saveXML({ format: true });
    
    await integrationStore.saveProcessDraft(processId.value, { xml });
    lastSavedXml.value = xml;
    console.log('[AutoSave] Draft XML saved to Backend API successfully (CA-19)');
  } catch (err: any) {
    // CA-10: Offline degradation warning - Evaluar si el error es de red
    const isNetworkError = !err.response || err.code === 'ERR_NETWORK' || err.response?.status === 503;
    if (isNetworkError) {
      showToast('⚠️ Modo Offline: Guardado en API falló. Revisa tu conexión de red.', 'error');
    } else {
      const serverMsg = err.response?.data?.message || err.response?.data?.error || 'Fallo al procesar el borrador en el servidor.';
      showToast(`❌ Error al guardar borrador: ${serverMsg}`, 'error');
    }
    console.error('[AutoSave] Failed:', err);
  }
};
```

---

## Criterios de Aceptación (DoD)

| # | Criterio | Evidencia |
|---|----------|-----------|
| 1 | Tests unitarios y de integración de backend pasan con éxito | Ejecutar `wsl --cd /home/haroltandrsgmezagu/proyectos/ibpms-platform/backend/ibpms-core mvn test -Dtest=BpmnDeployContractTest` -> **BUILD SUCCESS** (incluyendo el test `testPutDraftForNonExistentProcessCreatesItSuccessfully`) |
| 2 | Compilación de Frontend y tests de Vitest pasan | Ejecutar `wsl --cd /home/haroltandrsgmezagu/proyectos/ibpms-platform/frontend npx vitest run src/views/admin/Modeler/BpmnDesigner.spec.ts` -> **PASS** |
| 3 | Trazabilidad del código | `grep -rn "@Traceability: US-005, CA-15" ...` devuelve la anotación en los archivos modificados. |

---

## 🚦 SECUENCIA DE EJECUCIÓN

1. Modificar `BpmnDesignService.java` en el backend para implementar la auto-creación.
2. Modificar `BpmnDesigner.vue` en el frontend para implementar la discriminación de errores.
3. Ejecutar los tests de backend en WSL:
   `wsl --cd /home/haroltandrsgmezagu/proyectos/ibpms-platform/backend/ibpms-core mvn test -Dtest=BpmnDeployContractTest`
4. Ejecutar los tests de frontend en WSL:
   `wsl --cd /home/haroltandrsgmezagu/proyectos/ibpms-platform/frontend npx vitest run src/views/admin/Modeler/BpmnDesigner.spec.ts`
5. Ejecutar la compilación del frontend:
   `wsl --cd /home/haroltandrsgmezagu/proyectos/ibpms-platform/frontend npm run build`
6. Consolidar y empujar los cambios:
   `git add . && git commit -m "fix(modeler): auto-create process draft on put draft if non-existent" && git push origin sprint-6`

---

## 📋 Instrucciones para Copiar y Pegar (Prompt de Subagente)

```
Asume el rol de ⚙️ BACKEND - JAVA & 🎨 FRONTEND - VUE.

Para esta tarea es OBLIGATORIO utilizar y cumplir con la disciplina de los siguientes skills de desarrollo:
- addyosmani_planning (planificación rigurosa)
- addyosmani_sre_discipline (estrategia y validación estricta de supervivencia)
- addyosmani_code_review (revisión de código e integridad del diff)
- yudhi_architecture_compliance (cumplimiento de estándares de arquitectura)
- yudhi_database_migrations (buenas prácticas de base de datos)

ANTES DE HACER CUALQUIER COSA, lee obligatoriamente estos archivos en este orden exacto:

1. cat .cursorrules
2. cat .agents/skills/backend_sre_compilation_audit/SKILL.md
3. cat .agents/skills/frontend_build_audit/SKILL.md
4. cat .agents/skills/yudhi_architecture_compliance/SKILL.md
5. cat .agentic-sync/handoff_US005_creacion_procesos.md

TU MISIÓN:
1. Modificar BpmnDesignService.java en el backend para auto-crear un proceso nuevo (v0, BORRADOR) si se recibe una llamada de guardado de borrador (PUT /draft) y este no existe en base de datos.
2. Modificar BpmnDesigner.vue en el frontend para evaluar semánticamente los errores del auto-guardado y no mostrar el banner de red si el error es lógico de servidor (HTTP 400).
3. Validar el backend ejecutando los tests en WSL:
   wsl --cd /home/haroltandrsgmezagu/proyectos/ibpms-platform/backend/ibpms-core mvn test -Dtest=BpmnDeployContractTest
4. Validar el frontend ejecutando los tests en WSL:
   wsl --cd /home/haroltandrsgmezagu/proyectos/ibpms-platform/frontend npx vitest run src/views/admin/Modeler/BpmnDesigner.spec.ts
5. Certificar el frontend ejecutando `npm run build` en WSL.
6. Hacer commit convencional de los cambios a la rama sprint-6 y hacer push.

REGLAS INQUEBRANTABLES:
- Prohibido usar git stash.
- Usar obligatoriamente // @Traceability: US-005, CA-15.
```
