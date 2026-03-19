# 🚀 HANDOFF TÉCNICO V1: BACKEND (Java/Spring Boot) [RESTAURADO]
## 🎯 ITERACIÓN 30 - Módulo: Diseñador BPMN Avanzado (US-005)
**Criterios de Aceptación a Implementar:** CA-60 al CA-62 (Restricciones Estructurales Strict Mode).

---

### 🛑 REGLAS DE GOBERNANZA (Scope V1 Strict)
> [!WARNING]
> Esta iteración aborda la imposición de **Reglas de Juego V1** para el motor Camunda. Queda explícitamente sellado que NO estamos construyendo implementaciones de agentes inteligentes. Se busca blindar el parser XML y el motor para garantizar asincronicidad total y dependencia exclusiva de External Workers y Subprocesos mapeados rigurosamente.

---

### 🛠️ INSTRUCCIONES DE DISEÑO TÉCNICO

#### 1. CA-60: Mapeo Obligatorio (Call Activity)
- Interceptar despliegues usando un servicio validador (Ej. `BpmnDeploymentValidatorService`).
- Si detecta un elemento `<callActivity>`, validar obligatoriamente que sus extensiones de Camunda contengan mapeos explícitos in/out (`camunda:in` y `camunda:out`).
- Si están ausentes, lanzar un `BpmnValidationException` (que derivará en un HTTP 400 mediante nuestro ControllerAdvice global).

#### 2. CA-61: Vinculación Estricta de Cerebro Lógico (Business Rule Task)
- Al detectar una `<businessRuleTask>`, validar que contenga el atributo `camunda:decisionRef`. 
- Si no está presente, rechazar el despliegue indicando que el nodo "Cerebro" carece de DMN asignado.

#### 3. CA-62: Interdicción del Modo Síncrono (Extirpación de Java Delegates)
- Al detectar elementos `<serviceTask>` o `<sendTask>`, prohibir tajantemente la presencia de los atributos `camunda:class`, `camunda:delegateExpression` o `camunda:expression`.
- Exigir forzosamente que contengan `camunda:type="external"` y su respectivo `camunda:topic`.
- Cualquier nodo que intente ejecutar lógicas bloqueantes nativas cancelará inmediatamente la persistencia del archivo en la base de datos de Camunda.

---

### 🧪 Plan de Verificación Exigido
El agente Java debe:
1. Agregar Unit Tests inyectando XMLs mutilados probando que el Servicio repele el intento.
2. Asegurarse de retornar un DTO de Errores Semánticos compatible con los estándares de la Iteración 28.
3. **Respetar Gatekeeper Stash:** Al concluir, usar `git stash push -m "Iteracion 30: US-005 Backend Handoff CA-60 a CA-62 (Retrying)..."`.
