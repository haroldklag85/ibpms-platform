---
name: frontend_build_audit
description: Skill obligatoria para el Agente Frontend. Exige auto-compilación con npm/Vite y auditoría de build exitoso antes de entregar cualquier tarea o realizar un handoff.
triggers:
  - "Cuando el agente Frontend termine de escribir código Vue/TypeScript y se prepare para hacer el stash o reportar completitud."
  - "Al crear o modificar componentes Vue, Stores Pinia o clientes API (apiClient.ts)."
---

# MANDATO DE AUTO-COMPILACIÓN Y DISCIPLINA SRE (FRONTEND)

🛑 **REGLA DE SUPERVIVENCIA CERO-CONFIANZA (ZERO-TRUST BUILD)**

A partir de este momento, TIENES ESTRICTAMENTE PROHIBIDO asumir que tu código Vue/TypeScript funciona solo por haberlo escrito. Tu flujo de trabajo cambia obligatoriamente a validación de build en caliente:

## 1. PROHIBIDO EL HANDOFF CIEGO
Antes de enviar cualquier estado a QA, al Arquitecto, o notificar que has terminado, **DEBES** ejecutar la siguiente secuencia desde la carpeta `frontend/`:

```bash
npm run build
```

Este comando ejecutará el compilador Vite/TypeScript que verificará:
*   Errores de tipado TypeScript (interfaces, tipos, generics).
*   Imports rotos o dependencias circulares.
*   Errores de sintaxis en templates Vue (SFC).
*   Resolución correcta de alias de rutas (`@/`).

## 2. AUDITORÍA DE BUILD (GATEKEEPER DE CONSOLA)
Inmediatamente después de ejecutar el build, debes leer activamente la salida de la terminal:

*   Si observas un `Type error`, `Cannot find module`, `SyntaxError` o cualquier mensaje en rojo, **SE TE PROHÍBE ENTREGAR LA TAREA**.
*   Debes auto-corregir los errores de tipado, imports o templates rotos hasta que la consola reporte:
    > `✓ built in Xs`

## 3. AUDITORÍA DE LINTING (SEGUNDA BARRERA)
Si el proyecto tiene configurado un linter (ESLint), ejecuta adicionalmente:

```bash
npm run lint
```

*   Si hay errores críticos de lint (no warnings), corrígelos antes de empaquetar.
*   Los warnings pueden reportarse pero no bloquean la entrega.

## 4. LEY DE CORRESPONDENCIA API (Frontend vs Backend)
Si durante tu desarrollo consumiste un endpoint nuevo o modificaste un DTO existente:
*   **ES TU OBLIGACIÓN** verificar que el contrato del endpoint (URL, método HTTP, estructura del JSON) coincida con lo documentado en el handoff de `.agentic-sync/` o en el OpenAPI del Backend.
*   Si detectas que tu código frontend asume campos que NO existen en el contrato del Backend, **DETENTÉ** y reporta la discrepancia al Humano Enrutador antes de empaquetar.

**Tu código NO es válido hasta que el build de Vite lo demuestre sin errores.**
Una vez que valides el éxito del build, efectúa tu empaquetado seguro (`git stash save "temp-frontend-US[X]"`) y notifica al Humano Enrutador.
