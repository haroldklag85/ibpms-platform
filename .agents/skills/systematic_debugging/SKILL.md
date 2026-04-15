---
name: Systematic Debugging Protocol
description: Protocolo de debugging metódico de 5 pasos obligatorio para todo agente que reciba un rechazo de auditoría o reporte de bug. Prohibido proponer fixes antes de completar el diagnóstico. Adaptado de las mejores prácticas de la industria (mbcoalson/systematic-debugging, Skills Directory 2026).
version: 1.0.0
triggers:
  - "El código fue rechazado"
  - "Hay un bug"
  - "Test fallando"
  - "Error en compilación"
  - "Violación arquitectónica detectada"
---

# 🔬 Protocolo de Debugging Sistemático (Zero-Guess)

## 📌 Propósito
Este skill obliga a TODO agente que reciba un rechazo, bug report o test fallido a seguir un protocolo de 5 pasos **ANTES** de proponer o implementar cualquier corrección. El objetivo es eliminar el patrón de "corrección a ciegas" donde el agente cambia código arbitrariamente esperando que el problema desaparezca.

---

## 🚫 Anti-Patrones Estrictamente Prohibidos

1. **Corrección Especulativa:** Prohibido cambiar código sin haber reproducido y aislado el problema primero.
2. **Shotgun Debugging:** Prohibido cambiar múltiples archivos simultáneamente esperando que "algo funcione".
3. **Ignorar el Stack Trace:** Prohibido leer solo el mensaje de error. Leer el stack trace completo y los logs de contexto.
4. **Corrección Sin Test:** Prohibido enviar un fix sin un test que pruebe que el problema está resuelto.

---

## ✅ Protocolo de 5 Pasos (RIDC-V)

### Paso 1: REPRODUCIR 🔄
- **Objetivo:** Confirmar que el problema existe y es reproducible.
- **Acción:** Ejecutar el test fallido, compilar el código, o recrear el escenario exacto descrito en el reporte de rechazo.
- **Entregable:** Evidencia del fallo (log, stack trace, screenshot).
- **Si no se reproduce:** Documentar "No reproducible bajo condiciones X" y escalar al Arquitecto con contexto.

### Paso 2: AISLAR 🔍
- **Objetivo:** Identificar el archivo, método y línea exacta donde se origina el problema.
- **Acción:**
  - Usar `git diff` para ver qué cambió desde el último estado funcional.
  - Usar `git bisect` si el punto de introducción no es obvio.
  - Reducir el problema al mínimo componente que lo reproduce (ej. ¿falla el Repository solo, o falla cuando el Service lo llama?).
- **Entregable:** Path del archivo + número de línea + nombre del método donde se origina el fallo.

### Paso 3: DIAGNOSTICAR 🧠
- **Objetivo:** Entender la CAUSA RAÍZ (no el síntoma).
- **Acción:** Responder estas 3 preguntas obligatorias:
  1. **¿Qué debería hacer este código?** (Comportamiento esperado según el CA/Handoff)
  2. **¿Qué hace realmente?** (Comportamiento observado)
  3. **¿Por qué hay discrepancia?** (Causa raíz: ¿falta de datos? ¿lógica invertida? ¿tipo incorrecto? ¿violación de ADR?)
- **Entregable:** Diagnóstico en 1-3 oraciones que expliquen la causa raíz.

### Paso 4: CORREGIR 🔧
- **Objetivo:** Implementar la corrección **mínima** necesaria.
- **Reglas:**
  - Cambiar SOLO lo necesario para resolver la causa raíz diagnosticada.
  - NO refactorizar código adyacente que no esté relacionado con el bug.
  - Si la corrección requiere cambios en >3 archivos, notificar al Arquitecto antes de proceder.
  - Verificar que la corrección NO introduce nuevas violaciones de ADRs.
- **Entregable:** Diff de los cambios con justificación por línea.

### Paso 5: VERIFICAR ✅
- **Objetivo:** Confirmar que el fix funciona Y no rompe nada más.
- **Acción:**
  1. Ejecutar el test que originalmente falló → debe pasar ahora.
  2. Ejecutar la suite de tests completa del módulo afectado → 0 regresiones.
  3. Compilar el proyecto completo (usando el protocolo Zero-Trust de compilación correspondiente).
- **Entregable:** Log de ejecución mostrando tests pasando + compilación exitosa.

---

## 📊 Plantilla de Reporte de Debugging

```markdown
## Debugging Report — [ID del Rechazo/Bug]

| Paso | Resultado |
|------|-----------|
| 1. Reproducir | ✅/❌ [Evidencia] |
| 2. Aislar | Archivo: `X`, Método: `Y`, Línea: `Z` |
| 3. Diagnosticar | Causa raíz: [descripción] |
| 4. Corregir | Diff: [X archivos, Y líneas] |
| 5. Verificar | Tests: X/X pasando, Build: ✅ |
```

---

## ⚖️ DIRECTIVAS DE COMPORTAMIENTO

1. **Nunca saltar pasos.** Incluso si "ya sabes" cuál es el problema. El protocolo es secuencial.
2. **Documenta SIEMPRE.** Si no lo documentaste, no lo diagnosticaste.
3. **La corrección mínima gana.** Un fix de 2 líneas > un refactoring de 200 líneas.
4. **Si el Paso 3 no tiene respuesta clara**, pide ayuda al Arquitecto. No adivines la causa raíz.

## 🎯 Gatillo de Ejecución
Siempre que un agente reciba: un rechazo de auditoría (Fase 4), un test fallido, un error de compilación, o un reporte de bug, DEBE aplicar este protocolo ANTES de proponer cualquier corrección.
