# Solicitud de Aprobación Arquitectónica - Sprint01-UAT-HOTFIX (US-005)

**Arquitecto Líder:**
He analizado el handoff para la resolución de los bugs J02-001, J02-002 y J02-003. El plan de implementación ha sido documentado.

**Resumen del Plan:**
1. **BUG-J02-003 (Backend):** Modificar FormDirectoryService.java para eliminar el mock de datos, inyectar FormDesignService, consumir listarCatalogo(), mapear los resultados al formato esperado (Map<String, Object>) y aplicar la lógica de búsqueda. Se respetará estrictamente la arquitectura hexagonal y la política Zero-Mock.
2. **BUG-J02-001 (Frontend Router):** Agregar un redirect en rontend/src/router/index.ts de /admin/modeler a /admin/modeler/bpmn dentro del bloque de rutas autenticadas.
3. **BUG-J02-002 (Frontend BpmnDesigner):** Modificar rontend/src/views/admin/Modeler/BpmnDesigner.vue (línea ~4197) para que el método openCallActivity() abra la ruta /admin/modeler/bpmn en lugar de /admin/modeler.

**Protocolos a seguir:**
- Se aplicará el protocolo de compilación y validación SRE Zero-Trust para Backend (puerto 8080).
- Se aplicará el protocolo de build Zero-Trust para Frontend.
- Se actualizará el CHANGELOG_NO_TECNICO.md.

Por favor, otorga tu aprobación formal para proceder con la ejecución en modo EXECUTION.
