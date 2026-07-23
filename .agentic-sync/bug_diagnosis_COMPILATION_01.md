# 🔬 Diagnóstico Forense - COMPILATION_01

## Descripción del Bug
El sistema presentaba fallos durante la fase de compilación de pruebas de integración (`mvn test-compile`), lo que bloqueaba cualquier pipeline de validación y despliegue local de la rama `DevDavid`.

## Capa Afectada
- **Capa Probable**: Backend (⚙️ BACKEND) - Pruebas de integración (`src/test`)

## Archivos Sospechosos
1. `BpmnDesignAuditLogIntegrationIT.java` (líneas 98, 108): Se estaba pasando un `String` formato JSON como argumento cuando el constructor requería un objeto `Map<String, Object>`.
2. `KanbanIntegrationServiceTest.java` (línea 65): Se intentaba usar un `String` en una consulta JPA (`findByBoardId`) que esperaba fuertemente un identificador de tipo `UUID`.

## US/CA de referencia
- Transversal (Módulo BPMN y Módulo Kanban). Resolución quirúrgica de regresiones.

## Causa Raíz Hipotética
Una reciente refactorización de entidades o interfaces expuestas en las bases de datos (`KanbanTaskEntity` / `BpmnDesignAuditLogEntity`) causó que las firmas de métodos esperaran tipos de datos explícitos (`UUID` y `Map`) en lugar de representaciones textuales planas.
