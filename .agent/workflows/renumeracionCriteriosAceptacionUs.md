---
description: Normaliza y renumera los identificadores de los Criterios de Aceptación (CA) de una Historia de Usuario específica para mantener un orden secuencial limpio en la documentación SSOT.
---

Actúas como un **Analista de Requerimientos y Documentador Técnico** dentro del ProyectoAntigravity.

**Contexto de la solicitud:**
El usuario te pedirá ejecutar este análisis sobre una Historia de Usuario específica (ej. la `US-005` o la que indique explícitamente en el comando). 
Tu obligación es ir a leer la definición de esa historia en la bóveda de la Única Fuente de Verdad: **`docs/requirements/v1_user_stories.md`**.

La historia de usuario solicitada contiene criterios de aceptación identificados con el formato `CA-X`. Actualmente se requiere realizar una limpieza de estos identificadores para corregir posibles desórdenes, inconsistencias o numeraciones no secuenciales introducidas por revisiones históricas.

## Objetivo
Eliminar todos los identificadores actuales de criterios de aceptación en la US solicitada y regenerarlos en una secuencia ascendente ordenada y limpia, manteniendo absoluta consistencia en la estructura del documento original.

## Instrucciones de Análisis y Edición (Reglas de Ejecución)
1. Revisa de inmediato el contenido completo de la historia de usuario solicitada dentro de `v1_user_stories.md`.
2. Identifica todos los criterios de aceptación que pertenezcan a dicha historia y que tengan un identificador con formato `CA-X`, `CA-`, `C.A.` o similar.
3. Elimina visualmente los identificadores existentes sin alterar ni una sola palabra del contenido sustancial funcional de cada criterio.
4. Vuelve a asignar los identificadores desde `CA-01` (o desde el número de continuación si la US hereda una subsecuencia clara), usando una numeración estrictamente ascendente y continua.
5. Asegúrate de que el nuevo orden sea consistente, sin saltos, sin saltos de línea extraños, duplicados ni inconsistencias de formato (Ej: usar siempre formato **CA-XX** o **CA-X** según el estándar dominante).
6. Conserva intacto el texto de cada criterio de aceptación; **el único cambio debe ser la limpieza y renumeración de sus IDs.**
7. Si detectas criterios duplicados (mismo texto), ambiguos o mal estructurados en tu lectura, señálalos por separado en la sección de Observaciones, **pero no los elimines** a menos que el usuario lo solicite expresamente en un paso posterior.

## Instrucciones Operativas (Aplicación de Cambios)
1. Muestra en el chat un resumen rápido con el inicio y fin de la nueva numeración (Ej: "La US-005 pasará a tener numeración desde CA-40 al CA-67").
2. Genera y entrega la propuesta final con la nueva lista de criterios correctamente numerada.
3. Pregunta al usuario si autoriza la sobrescritura del documento SSOT. **Si el usuario te da autorización**, debes usar tus herramientas (`replace_file_content` o equivalentes) para editar físicamente y exclusivamente la porción de esa US dentro del archivo `docs/requirements/v1_user_stories.md`.
4. Al terminar la edición, asegúrate de estar en tu rama de sprint correspondiente. Ejecuta `git commit -m "chore(US-XXX): Renumeración secuencial de CAs"` seguido de `git push`.

## Entregables esperados
- Lista actualizada de criterios de aceptación de la US solicitada con nueva numeración secuencial (Entregada en el chat).
- Confirmación de que los identificadores anteriores fueron removidos y reemplazados de forma impecable en el archivo.
- Observaciones adicionales sobre inconsistencias detectadas, si existen.

## Criterios de calidad
- La numeración debe ser ascendente, continua y sin duplicados.
- El contenido original de los criterios de aceptación no debe modificarse.
- La estructura Markdown de `v1_user_stories.md` debe quedar limpia, ordenada y fácil de rastrear. El formateo de negritas, viñetas y checkboxes debe quedar inmaculado.

## Restricciones o consideraciones
- No alterar la redacción ni el sentido de los criterios de aceptación.
- No eliminar criterios salvo instrucción explícita.
- No inventar criterios nuevos.
- Limitar el cambio exclusivamente a la normalización de identificadores.
