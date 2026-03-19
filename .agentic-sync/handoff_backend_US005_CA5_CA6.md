# Contrato de Arquitectura Backend (Iteración 16 | US-005: CA-5 y CA-6)

**Rol:** Desarrollador Backend Java/Spring Boot.
**Objetivo:** Extender el Analizador Pre-Flight (XML Parsing) e inyectar el simulador de Seguridad Autogenerada (RBAC).

## 📋 Contexto y Criterios de Aceptación:
El Arquitecto Líder ha ordenado purgar el alcance V2 (Migraciones). Te enfocarás exclusivamente en el despliegue V1 de modelos.

*   **CA-5 (Nomenclatura Obligatoria de Instancia):**
    El Analizador Pre-Flight actual debe extenderse. Al recibir el XML, busca en el nodo raíz (`bpmn:process`) una extensión (ej. `camunda:property name="ReglaNomenclatura"` o equivalente en tu DTO). Si el proceso NO posee esta regla paramétrica (Ej: `PREFIJO-{Var}`), debes **bloquear el despliegue**, sumar el error a la lista de validación y retornar HTTP 422 advirtiendo: `"Debe definir cómo se llamarán los casos de este proceso"`.
*   **CA-6 (Autogeneración de Roles RBAC desde Lanes):**
    Si el Pre-Flight aprueba (201 Created), simula el despliegue. Durante este proceso, parsea el XML en búsqueda de todos los carriles (`bpmn:lane`). Por cada carril encontrado, imprime en consola y retorna en un array del response DTO la creación simulada de un Rol del sistema.
    *   **Nomenclatura del Rol:** `BPMN_[ProcessId]_[LaneName]` (Reemplaza espacios por guiones bajos).
    *   **Ejemplo:** `BPMN_Flujo_Onboarding_Aprobadores_Legales`.

## 📐 Reglas de Desarrollo:
1. Localiza el servicio Java `PreFlightAnalyzerService.java` creado en la iteración previa.
2. Agrega la lógica DOM/XML para leer `<bpmn:property name="ReglaNomenclatura">` del nodo Process.
3. Agrega la lógica para extraer los `<bpmn:lane>` y compilar la lista de Roles auto-generados. Añade `generatedRoles` al `DeploymentValidationResponse.java` en caso de éxito 201.

## 🛑 MANDATO LOCAL GATEKEEPER (REGLA DE ORO)
Tienes **ESTRICTAMENTE PROHIBIDO** hacer `git commit` a principal.
Congela tu Backend en un stash cuando superes la compilación local:
`git stash save "temp-backend-US005-ca5-ca6"`

Informa textualmente la confirmación del guardado.
