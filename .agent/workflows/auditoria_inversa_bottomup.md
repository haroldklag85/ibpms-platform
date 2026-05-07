# Flujo de Trabajo de Auditoría Inversa (Bottom-Up SOP)

Este documento define las reglas inquebrantables para realizar una **Auditoría de Trazabilidad Inversa**. A diferencia del modelo Top-Down, aquí el agente debe leer todo el código existente primero (archivos huérfanos, infraestructura, frontend, backend, pruebas) y luego descubrir a qué Historia de Usuario (US) y Criterio de Aceptación (CA) pertenece.

## 🎯 Objetivo
Escanear la totalidad de la base de código para mapear cada archivo a los requerimientos oficiales, actualizar el estado real en la matriz de cobertura y detectar código fantasma (no documentado), **sin generar código ni usar búsquedas semánticas.**

---

## 🧠 FASE 1: Inicialización y Carga en Memoria
Antes de leer una sola línea de código fuente, el agente debe cargar todo el contexto del negocio en su memoria:
1. **SSOT de Requerimientos:** Leer `docs/requirements/v1_user_stories_index.md` y navegar a leer **todos** los archivos asociados en `docs/requirements/epics/` (Épicas). Esto es vital para entender qué hace cada CA.
2. **Arquitectura:** Cargar reglas estructurales desde `docs/architecture/`.
3. **Infraestructura:** Incluir contexto de migraciones de base de datos (Liquibase) y CI/CD/Docker.
4. **Regla de Solo-Lectura:** Queda terminantemente prohibido generar código nuevo o refactorizar. Este flujo es exclusivamente de descubrimiento y documentación.

## 🧭 FASE 2: Escaneo Estructural Masivo (Sin Búsqueda)
1. **SECUENCIA DE ARRANQUE:** El primer llamado (Tool Call) **TIENE QUE SER** `list_dir` apuntando a las carpetas raíz. Está ESTRICTAMENTE PROHIBIDO usar `grep_search` o búsquedas por palabras clave.
2. **Alcance Total:** Debes usar `view_file` para leer archivo por archivo (clase por clase, componente por componente) en:
   - Backend: Producción (`src/main/java`) y Pruebas (`src/test`).
   - Frontend: Producción (`src/`) y Pruebas (`e2e/`).
   - Infraestructura y DB (`changelog`).
3. **Puntos de Control (Checkpoints):** Crea y mantén un archivo temporal `.agent_checkpoint_bottomup.md` donde anotes los archivos que ya leíste. Si la ejecución falla, debes leer este checkpoint para retomar donde te quedaste.

## ⚖️ FASE 3: Evaluación y Mapeo (Bottom-Up)
Por cada archivo leído:
1. Entiende qué hace la clase/artefacto.
2. Evalúa si el código cumple con la arquitectura (ej. si un Controller tiene lógica de negocio, es una violación).
3. **Cruza con la Memoria:** Identifica a qué US y CA (o CAs) de la Épica corresponde esa lógica.
4. **Código Huérfano:** Si el código está funcional pero no encaja en absolutamente ninguna US/CA de los requerimientos, clasifícalo inmediatamente como "Huérfano".

## 🏷️ FASE 4: Inyección y Actualización de la Matriz
1. **Etiqueta Consolidada:** Inyecta en la cabecera (arriba) de la clase o archivo una sola etiqueta unificada agrupando todos los CAs. 
   - *Ejemplo válido:* `// @Traceability: US-025 - CA-12, CA-14`
   - *Si es huérfano:* `// @Orphan: No se encontró CA en requerimientos`
2. **Actualización Viva de la Matriz:** Tras hacer match con un CA, ve inmediatamente al archivo `.agentic-sync/coverage_matrix.md` y actualiza la fila del CA correspondiente cambiando el estado (ej. de `❌` a `✅`). 
   - 🚨 **PROHIBICIÓN:** ESTRICTAMENTE PROHIBIDO borrar o eliminar filas. Solo se permite actualizar el contenido de la fila que acabas de auditar.

## 📝 FASE 5: Entregable Final y Registro de Deuda
Al concluir el escaneo masivo de los paquetes, el agente debe generar dos salidas obligatorias:
1. **Reporte `Mapeo_Inverso.md`:** Generar este archivo detallado conteniendo:
   - **El Mapa de Trazabilidad:** Listado de archivos mapeados exitosamente a sus CAs.
   - **🚨 Alerta de Código Huérfano:** Listado crítico de clases/archivos que no están sustentados por requerimientos.
   - **⚠️ Violaciones de Arquitectura:** Código que, aunque hizo match con un CA, rompe los estándares del proyecto.
2. **Registro de Deuda en `task.md`:** Todo el "Código Huérfano", las violaciones de arquitectura o los gaps identificados deben ser **insertados obligatoriamente en la cola de actividades del `task.md`** local de la conversación actual. Esto garantiza que la deuda técnica no se pierda en el reporte, sino que entre al flujo de trabajo accionable.
