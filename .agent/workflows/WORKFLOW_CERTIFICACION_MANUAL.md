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
