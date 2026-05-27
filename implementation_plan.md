# Plan de Implementación - US-005, CA-05 (Backend)

Este plan detalla los pasos para resolver el criterio de aceptación CA-05 de la historia de usuario US-005, el cual requiere que cuando falte la propiedad de extensión `ReglaNomenclatura` en un proceso BPMN, la validación retorne exactamente el mensaje: `"Debe definir cómo se llamarán los casos de este proceso."`.

## 1. Archivos a Modificar
- [CamundaBpmnValidationAdapter.java](file:///C:/Users/HaroltAndrésGómezAgu/ProyectoAntigravity/ibpms-platform/backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/adapters/CamundaBpmnValidationAdapter.java) (Líneas ~206-208)

## 2. Cambios Específicos
En `CamundaBpmnValidationAdapter.java`, dentro del método `validateBpmnStream`:
```java
// Cambiar esto:
if (!hasNomenclature) {
    response.addError("Process", "Debe definir cómo se llamarán los casos de este proceso (Propiedad: ReglaNomenclatura).");
}

// Por esto:
if (!hasNomenclature) {
    // @Traceability: US-005, CA-05
    response.addError("Process", "Debe definir cómo se llamarán los casos de este proceso.");
}
```

## 3. Pruebas de Verificación
- Ejecutar la prueba de integración `DeployNomenclatureGovernanceCA05Test` usando Maven local:
  `..\maven\apache-maven-3.9.6\bin\mvn.cmd test -pl ibpms-core -Dtest=DeployNomenclatureGovernanceCA05Test`
- Confirmar que la prueba pasa en verde.

## 4. Control de Versiones (Post-Aprobación)
- Realizar el commit y push correspondientes.
