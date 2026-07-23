# Flujo de Auditoría Programática por Lotes (Batch-Driven SOP)

Este workflow rige el comportamiento del Agente cuando ejecuta auditorías y modificaciones masivas impulsadas por la cola de lotes autogenerada (Python). 

## 🎯 Objetivo
Procesar un lote (5 archivos a la vez) de manera secuencial. El Agente usa sus propias herramientas internas para evaluar, documentar (Javadoc/JSdoc) e inyectar trazabilidad físicamente en el código fuente, garantizando que el límite de salida del chat no colapse.

---

## ⚙️ FASE 1: Inicialización Estricta
Al inicio de la sesión, el agente debe:
1. **Cargar Memoria del Negocio:** Leer exhaustivamente `docs/requirements/v1_user_stories_index.md` y las épicas en `docs/requirements/epics/` para entender el modelo funcional.
2. **Consultar Estado de Cola:** Leer el archivo `.agent/queue/progreso_lotes.md` para identificar el primer lote que está pendiente (marcado con `[ ]`).
3. **Adquirir Lote:** Leer el contenido del archivo JSON correspondiente (ej. `.agent/queue/lote_001.json`). Aquí encontrarás los 5 archivos que procesarás en este turno.

---

## 🧭 FASE 2: Iteración, Análisis y Mutación de Código
Para **cada uno de los 5 archivos** del lote, ejecutar secuencialmente este proceso:

1. **Lectura Profunda:** Usa `view_file` para leer el código fuente.
2. **Validación Cognitiva (LLM):** Evalúa para qué sirve este componente y con qué CA (Criterio de Aceptación) encaja lógicamente. También detecta si viola la arquitectura hexagonal.
3. **Inyección Física (Uso de Tools):** Usa tu herramienta de edición de código (`replace_file_content` o `multi_replace_file_content`) para mutar el archivo e inyectarle:
   - **Documentación Faltante:** Si el código no tiene Javadoc (Java) o bloque de comentarios (Vue/TS) que explique su propósito de negocio, créalo y escríbelo.
   - **Trazabilidad:** Inyecta en la cabecera `// @Traceability: US-XXX - CA-YY`. Si no lograste mapearlo a ningún requerimiento, inyecta `// @Orphan: No se encontró CA`.

*⚠️ PRECAUCIÓN:* Debes aplicar los cambios archivo por archivo en tiempo real usando tus tools. NO intentes escribir los 5 bloques de código mutado en el chat.

---

## ⚖️ FASE 3: Contabilidad y Deuda Técnica
1. **Actualización Viva de Matriz:** Si el código le pertenece a un CA, abre `.agentic-sync/coverage_matrix.md` y cambia el estado a `✅`. ESTRICTAMENTE PROHIBIDO borrar filas.
2. **Registro de Deuda en `task.md`:** Ve al archivo `task.md` local de tu conversación y agrega como pendientes:
   - Los archivos que marcaste como `@Orphan` (Código fantasma).
   - Violaciones de arquitectura detectadas.

---

## ✅ FASE 4: Cierre del Lote (Checkpointing)
Una vez que los 5 archivos han sido mutados exitosamente y la contabilidad está al día:
1. Usa tu herramienta de edición para abrir el JSON del lote (`.agent/queue/lote_XXX.json`) y cambiar la clave `"status"` de `"PENDING"` a `"COMPLETED"`.
2. Ve al archivo `.agent/queue/progreso_lotes.md` y cambia la viñeta de ese lote de `[ ]` a `[x]`.
3. Reporta al usuario en el chat que el Lote X ha sido finalizado y estás listo para que lance la orden de procesar el siguiente lote.
