# Certificación de Bug-Fix: Historial de Versiones (US-005)
**ID de Corrección:** CERT-US005-VERSIONS
**Fecha:** 2026-06-06T14:02:00-05:00
**Rama consolidada:** `bugfix/DevDavid-us-005-versions-api`
**Veredicto:** ✅ PASS (Doble Certificación Completada)

---

## 🛠️ Resumen de la Corrección
Se resolvió el bug reportado en el panel **Historial de Versiones** del BPMN Modeler, eliminando los datos simulados fijos del frontend ante errores de la API, alineando el contrato de datos del backend e implementando un manejo limpio del estado de "lista vacía" en ambos extremos.

### Capa Backend (Java/Spring Boot)
- **Archivo modificado:** [BpmnDesignController.java](file:///y:/home/haroltandrsgmezagu/proyectos/ibpms-platform/backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/web/BpmnDesignController.java#L253-L279)
- **Parche aplicado:** Se interceptó `IllegalArgumentException` dentro de `getProcessVersions` para responder `200 OK` con un array vacío `[]` si el proceso no se ha persistido en base de datos.
- **Alineación de contrato:** Se mapearon los campos de fecha (`date`), autor (`author`) y estado (`status`) derivados del DTO a la respuesta JSON, asignando valores por defecto seguros en caso de nulos.
- **Trazabilidad:** Añadido comentario `// @Traceability: US-005, CA-15, BUG-FIX: Retornar lista vacía de versiones si el proceso no tiene despliegues`.

### Capa Frontend (Vue 3/TypeScript)
- **Archivos modificados:** 
  - [BpmnDesigner.vue](file:///y:/home/haroltandrsgmezagu/proyectos/ibpms-platform/frontend/src/views/admin/Modeler/BpmnDesigner.vue#L2256-L2273) (Lógica Script + Template)
  - [BpmnDesigner.spec.ts](file:///y:/home/haroltandrsgmezagu/proyectos/ibpms-platform/frontend/src/views/admin/Modeler/BpmnDesigner.spec.ts#L2031-L2044) (Pruebas unitarias)
- **Parche aplicado:**
  - Se removieron los mocks de Ana y Carlos en el catch de `fetchVersions`, sustituyéndolos por un array vacío `[]`.
  - Se mapeó la respuesta del JSON (`versionId` a `version`, `isLatest` a `status` [ACTIVO/ARCHIVADO]).
  - Se inyectó en el template HTML un mensaje de lista vacía: `"No hay versiones publicadas aún."` cuando `versionHistory.length === 0` con el identificador `data-testid="no-versions-msg"`.
- **Trazabilidad:** Añadido comentario `// @Traceability: US-005, CA-15, BUG-FIX: Limpiar mocks del historial de versiones y mapear respuesta del backend`.

---

## 🔬 Verificación y Evidencia Física

### 1. Pruebas Backend (Green Phase)
Se ejecutó la suite `SandboxGovernanceTest` en consola del host local resultando en éxito total (`BUILD SUCCESS`):
- **Test Objetivo:** `testGetProcessVersionsNotFoundReturnsEmptyList` -> ✅ PASS
- **Comando:** `mvn test -Dtest=SandboxGovernanceTest`
- **Evidencia en log:**
  `[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0`
  `[INFO] ibpms-poc 0.0.1-SNAPSHOT ........................... SUCCESS`
  `[INFO] BUILD SUCCESS`

### 2. Pruebas Frontend (Vitest)
Se agregó y ejecutó la especificación de prueba:
- **Test Objetivo:** `Debe renderizar mensaje de no hay versiones cuando el backend retorna una lista vacia` -> ✅ PASS
- **Comando:** `npx vitest run src/views/admin/Modeler/BpmnDesigner.spec.ts`
- **Resultado:** Compilación finalizada exitosamente sin warnings con `npm run build`.

---

## 🏆 Cierre de Tarea
Con los deltas verificados en local por los subagentes y confirmados mediante ejecución directa en el host, procedemos a dar el bugfix por **Completado y Certificado**.
La rama `bugfix/DevDavid-us-005-versions-api` contiene los cambios necesarios y está lista para revisión.
