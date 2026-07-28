---
description: Protocolo metódico de depuración y corrección de bugs para agentes. Evita correcciones "a ciegas" o prueba-y-error.
---

# Systematic Debugging Protocol

> ⚠️ **REGLA DE ORO:** Cuando te reporten un bug, un fallo en una prueba, o comportamiento inesperado, TIENES ESTRICTAMENTE PROHIBIDO saltar directamente a proponer una solución o reescribir código. Debes ejecutar rigurosamente este protocolo.

Este protocolo asegura que resolvemos la causa raíz real y no un síntoma, respaldado siempre por telemetría/logs y pruebas.

## EL PROTOCOLO (5 PASOS)

### 1. Reproducir (Reproduce)
Demuestra que el error existe y puede provocarse controladamente.
- ¿Hay una prueba unitaria/E2E existente que falle reproduciendo el problema? Si no, **crea una**.
- Muestra al usuario/Arquitecto el output exacto del error (stack trace, exit code, log de consola).
- _"No arreglamos lo que no podemos romper a voluntad."_

### 2. Aislar (Isolate)
Corta el sistema a la mitad hasta encontrar el componente defectuoso.
- Si falla un endpoint, ¿es el controller, el servicio, o el repositorio?
- Valida entradas y salidas en cada capa usando *logs* exhaustivos (`logging.debug`, `console.log`).
- No asumas qué parte está fallando. Demuéstralo con evidencia.

### 3. Diagnosticar (Diagnose)
Formula una hipótesis estructurada.
- Documenta: ¿Qué está pasando de verdad?
- Compara con el contrato original esperado o la Especificación. ¿Cuál es la discrepancia?
- Identifica la **Causa Raíz**. Si es un "NullPointerException", la causa raíz no es que esté nulo, es *por qué* llegó a estar nulo.

### 4. Corregir (Fix)
Implementa la corrección mínima y segura.
- Evita refactorizaciones profundas que no estén estrictamente ligadas al bug.
- Asegúrate de no introducir regresiones (side-effects).
- Usa los estándares descritos en `.agents/skills/clean_code_standards/SKILL.md`.

### 5. Verificar (Verify)
Demuestra que la corrección fue exitosa.
- Ejecuta la prueba creada en el Paso 1. Debe pasar en verde (`✅`).
- Ejecuta la suite de pruebas completa del dominio afectado.
- Informa los resultados de manera explícita incluyendo evidencia.

## INSTRUCCIONES DE USO PARA EL AGENTE

Cada vez que seas invocado para arreglar un defecto o un test fallido, tu primera respuesta debe ser:
`[SYSTEMATIC DEBUGGING] Iniciando paso 1: Reproduciendo el error...`
Y seguir el flujo secuencial, esperando autorización o usando comandos locales (`run_command`) si tienes los permisos.
