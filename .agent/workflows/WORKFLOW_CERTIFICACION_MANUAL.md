# Workflow: Certificación UAT Manual (HCT - Human Certification Testing)

**Gobernanza:** Zero-Trust E2E Testing
**Aplica a:** QA Lead, Testers, Arquitectos

Este es el flujo de trabajo estandarizado para la certificación manual de casos de uso (Journeys). Debe ser orquestado conjuntamente entre el Agente QA y el Tester Humano.

## Parámetros de Ejecución Actual
> **Sprint:** `[SPRINT_ID]`
> **Iteración:** `[ITERACION_ID]`
> **Journey:** `[JOURNEY_ID]`

## Estructura del Workflow
La certificación se construye y avanza mediante **Misiones** iterativas para evitar sobrecarga cognitiva.

### 1. Inicialización (Misión 0)
- **Objetivo:** Setup de infraestructura, autenticación base, y precondiciones (Data Seed) de procesos transversales previos.
- **Acción Humana:** Levantar Docker, inyectar fixtures y confirmar conectividad.

### 2. Ejecución de Misiones (Loop)
- **Objetivo:** Ejecutar visualmente las Fases del Journey.
- **Evidencias:** Formato mixto. El tester debe recopilar:
  - 📸 Capturas de pantalla (UI rendered, Tostadas de éxito/error).
  - 📋 Logs de red y consola (F12, Red/Console tab) o server-side logs en caso de fallo crítico.

### 3. Protocolo de Brechas (Manejo de Bugs)
Si un paso no coincide con el Resultado Esperado:
1. **NO se detiene la ejecución general** (a menos que sea un bloqueante P0 crítico que rompa la UI/BD).
2. El tester **documenta la anomalía** inmediatamente en el Tracker oficial:
   👉 `docs/sprints/sprint_6_bugs.md`
3. El tester informa al Agente QA (yo) en el chat sobre el bug encontrado.
4. El tester continúa con el siguiente bloque/paso si el entorno lo permite (para maximizar el tiempo de reporting en una sola sesión).

### 4. Cierre y Aprobación
El Agente QA valida las respuestas del humano y firma la misión como `PASS`, `PASS CON OBSERVACIONES` (bugs no bloqueantes), o `FAIL`.

### 5. Informe Técnico QA (Regla Obligatoria)

**Regla:** Durante la ejecución de cualquier Journey, el Agente QA **DEBE** crear o actualizar un documento de Informe Técnico QA que registre con detalle quirúrgico todos los hallazgos reportados por el Tester Humano.

- **Ubicación del archivo:** `docs/qa/INFORME_TECNICO_QA_[JOURNEY_ID]_[SPRINT_ID].md`
- **Momento de creación:** Al inicio de la primera misión del Journey o cuando el Tester Humano reporte el primer hallazgo.
- **Actualización:** El informe se actualiza **cada vez** que el Tester Humano reporte resultados de una misión (PASS, bloqueo o rechazo).

**Contenido obligatorio por cada misión documentada:**

| Sección | Descripción |
|---------|-------------|
| **Pasos ejecutados** | Tabla con cada paso, resultado (PASS/BLOQUEADO/FAIL) y observaciones textuales del humano |
| **Bugs descubiertos** | ID del bug, severidad, causa raíz técnica (archivo + línea), commit del fix si fue resuelto |
| **Observaciones de entorno** | Limitaciones que no son bugs (ej: falta de servicio IA, permisos, etc.) |
| **Evidencia técnica** | Errores de consola DevTools, códigos HTTP, mensajes de error exactos reportados por el humano |
| **Línea de tiempo Git** | Commits relevantes con hash, fecha y descripción, obtenidos de `git log` |
| **Veredicto de misión** | PASS, PASS CON OBSERVACIONES, BLOQUEADA, o FAIL — con justificación |

**Restricciones:**
- El informe solo documenta lo que el Tester Humano reporta directamente. **NO se inventan, asumen ni imaginan resultados.**
- Los handoffs al Arquitecto Líder se documentan dentro del informe pero **NO se recomiendan proactivamente** — la función principal del agente es ejecutar pruebas, no prescribir soluciones.
- El informe es un documento vivo que se actualiza a lo largo de todo el Journey.

