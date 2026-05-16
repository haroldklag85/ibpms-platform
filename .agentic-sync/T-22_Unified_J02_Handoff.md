# 🧠→🤖 Handoff Unificado: Arquitecto Líder → Enjambre (QA, Backend, Frontend)
# T-22: Orquestación J-02 (Low-Code) y Cierre J-04

**Emitido por:** 🧠 ARQUITECTO LÍDER
**Destinatario:** 🕵️ QA E2E, ⚙️ BACKEND - JAVA, 🎨 FRONTEND - VUE3
**Fecha:** 2026-05-13T00:50:00-05:00
**Sprint:** 7 — Iteraciones 7.1 y 7.2
**Prioridad:** 🔴 Alta
**Dependencia:** Ninguna

---

## 📖 LECTURAS OBLIGATORIAS (Ejecutar ANTES de codificar)

```bash
# 1. Política del Enjambre (LEY GLOBAL 0, 1, 2, 3)
cat .cursorrules

# 2. Skills principales de los agentes
cat ibpms-platform/.agents/skills/qa_automation/SKILL.md
cat ibpms-platform/.agents/skills/backend_java_spring/SKILL.md
cat ibpms-platform/.agents/skills/frontend_vue3_tailwind/SKILL.md

# 3. Skills transversales aplicables
cat ibpms-platform/.agents/skills/zero_mock_enforcement/SKILL.md
cat ibpms-platform/.agents/skills/architect_handoff_protocol/SKILL.md

# 4. ADRs relevantes (Política Zero-Mock)
cat ibpms-platform/docs/architecture/adr_010_testing_pyramid_governance.md
```

> ⚠️ **LEY GLOBAL 3 — Trazabilidad Inversa:** TODO el código nuevo DEBE llevar
> la anotación @Traceability o comentario `// @Traceability: Orquestación J-02 (T-22)`.
> Esto es INNEGOCIABLE.

---

## 🔬 Diagnóstico del Arquitecto

La estabilización del entorno J-04 finalizó exitosamente (T-20.4). Ahora se requiere recertificar el Happy Path (QA). Concurrentemente, inicia el ensamblaje del ecosistema J-02 (IDE Formularios, BPMN, DMN). La directiva estricta es **Zero-Mock V2** partiendo de una Base de Datos vacía.

| Violación/Hallazgo (Deuda) | Ubicación | Detalle |
|--------------------|:---------:|---------|
| Bloqueo P0 J-04 (QA) | `e2e/certification/*.spec.ts` | El flujo debe ejecutarse ahora que los fallos de Red, DOM y RBAC están remediados. |
| Mocks Frontend J-02 | `frontend/src/stores/` | Los stores asociados a US-003, US-005, US-007 mantienen datos estáticos y lógica simulada que viola ADR-010. |
| Plumbing Backend J-02 | `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/web/` | Faltan/No están expuestos los endpoints canónicos para guardar Formularios JSON y desplegar esquemas XML BPMN/DMN. |

---

## 🎯 Instrucciones Quirúrgicas

### Paso 1: Recertificación QA (Carril Paralelo A)
**Objetivo:** Ejecutar la suite E2E sobre el J-04.
- Ejecutar `npx playwright test` sobre los specs de Workdesk, Kanban y Kill-Switch.
- **Inmutabilidad:** NO modificar aserciones (Ley Global 4).

### Paso 2: Plumbing Backend (Carril Paralelo B.1)
**Objetivo:** Exponer APIs reales para Formularios, BPMN y DMN.
- Revisar y ajustar los REST Controllers asociados a US-003, US-005 y US-007.
- Eliminar cualquier retorno estático (Zero-Mock V2).
- Garantizar que los métodos POST/PUT guarden en la DB vacía.

### Paso 3: Wiring Frontend (Carril Secuencial B.2)
**Objetivo:** Conectar el Frontend a las APIs recién expuestas.
- Purgar todo `setTimeout` o mock en los stores (`useFormStore`, `useBpmnStore`, `useDmnStore`).
- Implementar peticiones AXIOS reales con manejo `try/catch/finally` para evitar bloqueos del DOM.

---

## Criterios de Aceptación (DoD)

| # | Criterio | Evidencia |
|---|----------|-----------|
| 1 | **QA:** J-04 Happy Path en verde. | Reporte final de Playwright (`100% passed` en los specs P0). |
| 2 | **Backend:** Endpoints de guardado (Form, BPMN, DMN) expuestos y enlazados a la DB. | Búsqueda `grep "mock" src/main/java/com/ibpms/poc/infrastructure/web/` → 0 resultados en endpoints J-02. Compilación exitosa `mvn clean compile`. |
| 3 | **Frontend:** Mocks eliminados y promesas asíncronas seguras (finally). | Búsqueda `grep "setTimeout" src/stores/` → 0 resultados en J-02. Compilación exitosa `npm run type-check`. |
| 4 | **Global:** Build exitoso y trazabilidad. | Validar marcadores `@Traceability: Orquestación J-02 (T-22)` inyectados en todos los archivos tocados. Commit en rama. |

---

## 🚦 SECUENCIA DE EJECUCIÓN

1. **QA:** `npx playwright test` (Ejecutar sobre specs J-04).
2. **QA:** `git add . && git commit -m "test(e2e): recertificacion final limpia flujo p0 j-04 [T-22]"`
3. **Backend:** Modificar Controllers J-02 para persistencia real.
4. **Backend:** `mvn clean compile`
5. **Backend:** `git add . && git commit -m "feat(core): conexion de endpoints j-02 para persistencia zero-mock [T-22]"`
6. **Frontend:** Modificar Stores/Componentes J-02 consumiendo AXIOS.
7. **Frontend:** `npm run type-check` y `npm run build`
8. **Frontend:** `git add . && git commit -m "feat(ui): wiring de disenhadores lowcode contra backend real j-02 [T-22]"`

---

## 📋 Instrucciones de Copiar y Pegar

### Para el Agente 🕵️ QA E2E
```text
Asume el rol de 🕵️ Agente QA E2E.

ANTES DE HACER CUALQUIER COSA, lee obligatoriamente estos archivos en este orden exacto:

1. cat .cursorrules
2. cat ibpms-platform/.agents/skills/qa_automation/SKILL.md
3. cat ibpms-platform/docs/architecture/adr_010_testing_pyramid_governance.md
4. cat ibpms-platform/.agentic-sync/T-22_Unified_J02_Handoff.md

TU MISIÓN:

1. Levanta el entorno Zero-Mock y ejecuta los tests de Playwright para el ecosistema J-04 (Workdesk, Kanban, Kill-Switch).
2. Genera el reporte confirmando el "Green Build" o mapea los fallos si los hay.
3. Commit: `git add . && git commit -m "test(e2e): recertificacion final limpia flujo p0 j-04 [T-22]"`

REGLAS INQUEBRANTABLES:
- DEBES observar estrictamente la Ley Global 4 (Inmutabilidad): PROHIBIDO modificar aserciones de negocio.
- DEBES inyectar `// @Traceability: Recertificación Final J-04 (T-22)` si dejas comentarios o cambias URIs.
- PROHIBIDO el uso de `git stash`.
```

### Para el Agente ⚙️ BACKEND - JAVA
```text
Asume el rol de ⚙️ Agente Backend.

ANTES DE HACER CUALQUIER COSA, lee obligatoriamente estos archivos en este orden exacto:

1. cat .cursorrules
2. cat ibpms-platform/.agents/skills/backend_java_spring/SKILL.md
3. cat ibpms-platform/.agents/skills/zero_mock_enforcement/SKILL.md
4. cat ibpms-platform/docs/architecture/adr_010_testing_pyramid_governance.md
5. cat ibpms-platform/.agentic-sync/T-22_Unified_J02_Handoff.md

TU MISIÓN:

1. Conecta el "plumbing" del ecosistema Low-Code (J-02): Expón y asegura los endpoints REST de guardado/despliegue para US-003 (Formularios), US-005 (BPMN) y US-007 (DMN).
2. Build/Compile: `mvn clean compile`
3. Commit: `git add . && git commit -m "feat(core): conexion de endpoints j-02 para persistencia zero-mock [T-22]"`

REGLAS INQUEBRANTABLES:
- DEBES observar la Política Zero-Mock V2 Estricta (ADR-010): PROHIBIDO usar mocks o "stub objects" en los controladores. Conecta los Inbound REST con la persistencia real partiendo de una BD vacía.
- DEBES inyectar `// @Traceability: Orquestación J-02 (T-22)` en los métodos modificados.
- PROHIBIDO el uso de `git stash`.
```

### Para el Agente 🎨 FRONTEND - VUE3
```text
Asume el rol de 🎨 Agente Frontend.

ANTES DE HACER CUALQUIER COSA, lee obligatoriamente estos archivos en este orden exacto:

1. cat .cursorrules
2. cat ibpms-platform/.agents/skills/frontend_vue3_tailwind/SKILL.md
3. cat ibpms-platform/.agents/skills/zero_mock_enforcement/SKILL.md
4. cat ibpms-platform/docs/architecture/adr_010_testing_pyramid_governance.md
5. cat ibpms-platform/.agentic-sync/T-22_Unified_J02_Handoff.md

TU MISIÓN:

1. Conecta la UI del ecosistema Low-Code (J-02): Enchufa el IDE de Formularios (US-003), BPMN Designer (US-005) y DMN Intelligence (US-007) con el backend real. Elimina mocks en Pinia. Implementa bloques try/catch/finally en las llamadas asíncronas.
2. Build/Compile: `npm run type-check` y `npm run build`
3. Commit: `git add . && git commit -m "feat(ui): wiring de disenhadores lowcode contra backend real j-02 [T-22]"`

REGLAS INQUEBRANTABLES:
- DEBES observar la Política Zero-Mock V2 Estricta (ADR-010): PROHIBIDO simular APIs con `setTimeout`. Las interacciones atacan endpoints reales.
- DEBES inyectar `// @Traceability: Orquestación J-02 (T-22)` en las funciones asíncronas modificadas.
- PROHIBIDO usar `alert()` o `confirm()` nativos. Utiliza notificaciones o modales Vue 3.
```
