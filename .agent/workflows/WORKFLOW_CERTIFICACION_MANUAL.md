---
description: V2 (2026-06-02). Certificación UAT Manual sincronizada con la Gobernanza PM-IA y la metodología de Cadenas de Capacidad. Incluye gates de alineación estratégica, verificación de precondiciones por cadena, y actualización de bitácora no-técnica.
params:
  sprint_id: "Sprint PM activo (ej. PM-01)"
  iteracion_id: "Iteración dentro del sprint (ej. iter-01)"
  journey_id: "Journey UAT a certificar (ej. J-02, J-04, J-SEC)"
  usuario_tester: "Nombre del tester humano para la bitácora"
---

# Workflow: Certificación UAT Manual (HCT - Human Certification Testing) v2

**Gobernanza:** Zero-Trust E2E Testing + Gobernanza PM-IA
**Aplica a:** QA Lead, Testers, Arquitectos
**Última actualización:** 2026-06-02
**Versión:** 2.0

Este es el flujo de trabajo estandarizado para la certificación manual de casos de uso (Journeys). Debe ser orquestado conjuntamente entre el Agente QA y el Tester Humano, y está **sincronizado** con la metodología del PM-IA.

## Parámetros de Ejecución Actual
> **Sprint:** `[SPRINT_ID]`
> **Iteración:** `[ITERACION_ID]`
> **Journey:** `[JOURNEY_ID]`
> **Tester:** `[USUARIO_TESTER]`

## Estructura del Workflow
La certificación se construye y avanza mediante **Misiones** iterativas para evitar sobrecarga cognitiva.

### 0. Gate de Alineación Estratégica (NUEVO — Obligatorio)

> ⚠️ **REGLA PM-IA:** Antes de iniciar cualquier certificación, el Agente QA DEBE verificar la alineación con el roadmap del PM-IA.

1. **Leer el Roadmap:** `docs/sprints/gobernanza_pm/01_ROADMAP_Y_METODOLOGIA.md`
   - Verificar que el Journey a certificar corresponde a US que están en el Sprint activo.
   - Identificar la **Cadena de Capacidad** a la que pertenecen las US del Journey.

2. **Verificar Precondiciones de Cadena:**
   - Consultar la tabla de dependencias entre Journeys:

   | Journey | Depende de | Precondiciones |
   |---------|------------|----------------|
   | **J-02** | Ninguno | BPMN + Forms + DMN construidos |
   | **J-03** | Ninguno | RabbitMQ topology + O365 webhook |
   | **J-04** | **J-02** | Reutiliza instancias BPMN de J-02 |
   | **J-05** | Ninguno | DB limpia + Redis limpio |
   | **J-06** | **J-05** | RBAC/usuarios configurados en J-05 |
   | **J-07** | Ninguno | DMN + BPMN + Copilot AI services |
   | **J-08** | Ninguno | RabbitMQ + form_event_store DDL |
   | **J-SEC** | **Todos** | 2 tenants aislados + ClamAV + toda la infra |

   - Si el Journey depende de otro que NO ha sido certificado → **ADVERTIR** al tester y documentar el riesgo.

3. **Consultar Contratos de API:** Leer `docs/sprints/gobernanza_pm/API_CONTRACTS.md`
   - Los endpoints que se validen en el Journey DEBEN coincidir con los definidos en el contrato.
   - Si se detecta una discrepancia (ruta diferente, payload diferente) → documentarla como **hallazgo de contrato** en el informe.

4. **Consultar la Guía de Sprint:** Leer `docs/sprints/gobernanza_pm/SPRINT_01_GUIA_EJECUCION.md` (o la guía del sprint actual)
   - Verificar que las US a certificar fueron completadas en los slots de desarrollo correspondientes.
   - Si una US no fue completada en desarrollo → **NO se certifica**. Reportar al humano.

### 1. Inicialización (Misión 0)
- **Objetivo:** Setup de infraestructura, autenticación base, y precondiciones (Data Seed) de procesos transversales previos.
- **Acción Humana:** Levantar Docker, inyectar fixtures y confirmar conectividad.
- **Verificación de Backend:** Ejecutar `curl -s http://localhost:8080/actuator/health` → debe responder `{"status":"UP"}`. Si no responde, el tester debe arrancar Spring Boot en consola local (ver `docs/sprints/gobernanza_pm/01_ROADMAP_Y_METODOLOGIA.md` para instrucciones).
- **Verificación de Servicios Docker:** `docker ps` → PostgreSQL, Redis y RabbitMQ deben estar `Up (healthy)`.

### 2. Ejecución de Misiones (Loop)
- **Objetivo:** Ejecutar visualmente las Fases del Journey.
- **Evidencias:** Formato mixto. El tester debe recopilar:
  - 📸 Capturas de pantalla (UI rendered, Tostadas de éxito/error).
  - 📋 Logs de red y consola (F12, Red/Console tab) o server-side logs en caso de fallo crítico.
- **Validación de Contratos:** Durante la ejecución, el tester debe verificar que:
  - Las URLs de las peticiones HTTP coinciden con `API_CONTRACTS.md`.
  - Los payloads de request/response coinciden con los schemas definidos.
  - Si hay discrepancia → documentar como **Bug de Contrato** (categoría especial).

### 3. Protocolo de Brechas (Manejo de Bugs)
Si un paso no coincide con el Resultado Esperado:
1. **NO se detiene la ejecución general** (a menos que sea un bloqueante P0 crítico que rompa la UI/BD).
2. El tester **documenta la anomalía** inmediatamente en el Tracker oficial:
   👉 `docs/sprints/sprint_X_bugs.md`
3. **Clasificar el bug por tipo:**
   | Tipo | Descripción | Acción |
   |------|-------------|--------|
   | **Bug Funcional** | La feature no hace lo esperado | Reportar en tracker |
   | **Bug de Contrato** | El endpoint/payload no coincide con API_CONTRACTS.md | Reportar + actualizar contrato |
   | **Bug de Mock** | Se detecta dato hardcodeado o mock en la respuesta | 🔴 **P0 — Bloqueante**. La US NO puede certificarse con mocks. |
   | **Bug de Cadena** | Una feature falla porque su US prerequisito no está completa | Reportar + documentar la dependencia rota |
4. El tester informa al Agente QA (yo) en el chat sobre el bug encontrado.
5. El tester continúa con el siguiente bloque/paso si el entorno lo permite (para maximizar el tiempo de reporting en una sola sesión).

### 4. Cierre y Aprobación
El Agente QA valida las respuestas del humano y firma la misión como:
- `PASS` — todo OK, sin bugs
- `PASS CON OBSERVACIONES` — bugs no bloqueantes encontrados
- `FAIL` — issues bloqueantes (P0/P1 o mocks detectados)

> ⚠️ **REGLA ANTI-FALSOS-POSITIVOS:** Una misión con mocks detectados (Bug de Mock) **NUNCA** puede certificarse como PASS. El veredicto mínimo es FAIL hasta que los mocks sean reemplazados por datos reales.

### 5. Informe Técnico QA (Regla Obligatoria)

**Regla:** Durante la ejecución de cualquier Journey, el Agente QA **DEBE** crear o actualizar un documento de Informe Técnico QA que registre con detalle quirúrgico todos los hallazgos reportados por el Tester Humano.

- **Ubicación del archivo:** `docs/qa/INFORME_TECNICO_QA_[JOURNEY_ID]_[SPRINT_ID].md`
- **Momento de creación:** Al inicio de la primera misión del Journey o cuando el Tester Humano reporte el primer hallazgo.
- **Actualización:** El informe se actualiza **cada vez** que el Tester Humano reporte resultados de una misión (PASS, bloqueo o rechazo).

**Contenido obligatorio por cada misión documentada:**

| Sección | Descripción |
|---------|-------------|
| **Cadena de Capacidad** | Identificar la cadena a la que pertenece el Journey y su estado de completitud |
| **Pasos ejecutados** | Tabla con cada paso, resultado (PASS/BLOQUEADO/FAIL) y observaciones textuales del humano |
| **Bugs descubiertos** | ID del bug, tipo (Funcional/Contrato/Mock/Cadena), severidad, causa raíz técnica (archivo + línea), commit del fix si fue resuelto |
| **Validación de Contratos API** | Discrepancias detectadas entre el comportamiento real y `API_CONTRACTS.md` |
| **Observaciones de entorno** | Limitaciones que no son bugs (ej: falta de servicio IA, permisos, etc.) |
| **Evidencia técnica** | Errores de consola DevTools, códigos HTTP, mensajes de error exactos reportados por el humano |
| **Línea de tiempo Git** | Commits relevantes con hash, fecha y descripción, obtenidos de `git log` |
| **Veredicto de misión** | PASS, PASS CON OBSERVACIONES, BLOQUEADA, o FAIL — con justificación |

**Restricciones:**
- El informe solo documenta lo que el Tester Humano reporta directamente. **NO se inventan, asumen ni imaginan resultados.**
- Los handoffs al Arquitecto Líder se documentan dentro del informe pero **NO se recomiendan proactivamente** — la función principal del agente es ejecutar pruebas, no prescribir soluciones.
- El informe es un documento vivo que se actualiza a lo largo de todo el Journey.

### 6. Actualización de Bitácora No-Técnica (NUEVO — Obligatorio)

> 📋 **REGLA PM-IA:** Al finalizar la certificación de un Journey, el Agente QA DEBE actualizar la bitácora no-técnica para que los stakeholders humanos comprendan qué se validó.

1. Leer `docs/sprints/gobernanza_pm/CHANGELOG_NO_TECNICO.md`.
2. Agregar una entrada con el siguiente formato:

```markdown
## [FECHA_LOCAL] — Certificación: [TÍTULO DEL JOURNEY EN LENGUAJE COTIDIANO]
**Autor**: [USUARIO_TESTER] (asistido por Agente QA)
**¿Qué es?**: Se verificó en vivo que [descripción de lo que se probó, sin tecnicismos]
**¿Para qué sirve?**: Confirmar que [beneficio para el usuario] funciona correctamente antes de entregarlo
**¿De dónde viene?**: Journey [JOURNEY_ID] — cubre las historias [US-XXX, US-YYY]
**¿Qué debería hacer?**: El sistema [comportamiento esperado verificado]
**Estado**: ✅ Certificado | ⚠️ Certificado con observaciones | ❌ No certificado
```

3. La entrada DEBE estar libre de jerga técnica.
4. Incluir el parámetro `[USUARIO_TESTER]` proporcionado al inicio del workflow.

### 7. Notificación al PM-IA (NUEVO — Cierre de Ciclo)

Al completar la certificación del Journey:
1. El Agente QA genera un resumen de 3 líneas con: Journey certificado, veredicto global, bugs críticos encontrados.
2. El Agente QA instruye al humano: *"Humano, lleva este resumen de certificación al chat del PM-IA o del Arquitecto Líder para actualizar el roadmap."*
3. El Arquitecto Líder o PM-IA actualiza la `coverage_matrix.md` con los resultados de QA.
