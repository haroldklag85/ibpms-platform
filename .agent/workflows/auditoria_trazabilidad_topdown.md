# Flujo de Trabajo de Auditoría de Trazabilidad Estricta (SOP)

Este documento define el comportamiento algorítmico y las reglas inquebrantables para los agentes de IA al ejecutar auditorías de Historias de Usuario (US) y Criterios de Aceptación (CA) en la plataforma iBPMS.

## 🎯 Objetivo
Garantizar la trazabilidad del código, auditar el cumplimiento arquitectónico y detectar brechas de implementación **sin generar código nuevo ni depender de búsquedas semánticas propensas a alucinaciones.**

---

## ⚙️ FASE 1: Inicialización y Contexto Estricto
1. **Reglas Base:** Leer y acatar obligatoriamente `.cursorrules` para definir lo permitido y prohibido.
2. **Arquitectura:** Cargar contexto estructural leyendo exclusivamente el directorio `docs/architecture/`.
3. **Fuente de la Verdad (SSOT):** Leer la US y el CA objetivo **únicamente** desde `docs/requirements/v1_user_stories_index.md`. **REGLA TÉCNICA:** Al leer el índice de requerimientos y este mismo archivo workflow, debes usar obligatoriamente la herramienta `view_file` con el parámetro `IsSkillFile: true` para internalizar las instrucciones de forma neuronal. Está estrictamente prohibido usar `IsSkillFile: true` para leer código fuente.

## 🧭 FASE 2: Navegación Estructural (Top-Down)
1. **SECUENCIA DE ARRANQUE OBLIGATORIA (ANTI-ALUCINACIÓN):** Tu primerísimo llamado a una herramienta (Tool Call) al iniciar la auditoría **TIENE QUE SER** obligatoriamente `list_dir` apuntando a las carpetas raíz (`backend/ibpms-core/src/main/java/` o `frontend/src/`).
2. **PENALIZACIÓN AUTOMÁTICA (HARD-FAIL):** Queda ESTRICTAMENTE PROHIBIDO utilizar herramientas como `grep_search`, búsquedas semánticas del IDE, o cualquier comando de búsqueda global. Si detectas que tú o algún sub-agente intentan usar una herramienta de búsqueda para encontrar el código, debes **ABORTAR LA EJECUCIÓN INMEDIATAMENTE** y registrar una "Violación Crítica de Protocolo" en el `task.md`.
3. **NAVEGACIÓN ESTRUCTURAL PURA:** Tu única forma autorizada para encontrar código es usar `list_dir` y `view_file` (sin IsSkillFile). Debes abrir los directorios y leer los archivos manualmente de capas externas a internas. Tienes un **límite máximo de 10 archivos analizados por capa** para evitar desviaciones.
   - **Backend:** `Controller` → `Service/UseCase` → `Repository/Port` → `Entity/DTO`.
   - **Frontend:** `Router` → `View` → `Component` → `Store/Composable` → `API Client`.

## 🔍 FASE 3: Lectura Profunda y Validación
1. **Evaluación de Completitud:** Leer el código para determinar si el CA está implementado totalmente, parcialmente o si está ausente.
2. **Pausa de Comprensión (Micro-Summary):** Antes de pasar a leer el siguiente archivo, estás obligado a generar internamente un resumen detallado (mínimo 3 a 5 líneas) explicando la lógica profunda de la clase que acabas de leer y cómo se vincula arquitectónicamente al CA evaluado.
3. **Evaluación Arquitectónica:** Validar si el código cumple con las reglas de arquitectura.
3. **Regla de Solo-Lectura (No Programar):** 
   - Si falta código, **NO PROGRAMARLO**.
   - Si hay violaciones arquitectónicas, **NO REFACTORIZAR**. 
   - *Todo hallazgo se reporta, nada se repara de forma autónoma durante la auditoría.*

## 🏷️ FASE 4: Inyección de Trazabilidad
1. **Alcance:** Modificar código de Producción (`src/main`, `src/`) y código de Pruebas (Unitarios y E2E).
2. **Formato Estándar:** `// @Traceability: US-XXX - CA-YY` (Adaptando el símbolo de comentario según el lenguaje `//`, `<!-- -->`, `/* */`).
3. **Granularidad Dinámica:** 
   - A nivel de **Clase/Archivo** si todo el documento responde al CA.
   - A nivel de **Método/Bloque** si el archivo es compartido por múltiples lógicas.
4. **Unificación de Historial:** Si existe una etiqueta de trazabilidad antigua (ej. `// Feature: US-010`), **UNIFICARLA** en el nuevo formato (ej. `// @Traceability: US-010, US-025 - CA-12`). No se deben borrar etiquetas previas.

## 📝 FASE 5: Ejecución y Entregables (Autonomía)
1. **Autonomía Total:** El agente construirá su plan de acción interno y lo ejecutará **de principio a fin sin pedir permiso** para cada modificación.
2. **Protección de la Matriz de Cobertura:** Al actualizar `.agentic-sync/coverage_matrix.md`, queda ESTRICTAMENTE PROHIBIDO borrar o eliminar filas existentes. Solo estás autorizado a **añadir o actualizar** el estado de la fila correspondiente al Criterio de Aceptación (CA) que estás evaluando en esa iteración.
3. **Generación de Reporte:** Al finalizar, generar obligatoriamente un reporte Markdown llamado `Reporte_Auditoria_US_XXX.md` que contenga:
   - 🗺️ **Ruta Estructural Navegada:** Documentar explícitamente el árbol de carpetas exacto que se navegó usando `list_dir` para localizar las clases (evidencia de rastreo manual).
   - Archivos de producción y tests etiquetados con éxito.
   - 🚨 **Brechas de Implementación:** Código faltante o parcial, indicando la clase/ubicación donde se interrumpió el rastro.
   - ⚠️ **Violaciones de Arquitectura:** Código que cumple el CA pero rompe las reglas técnicas.
4. **Actualización de Tareas (task.md):** Además de generar el reporte individual, el agente debe **escribir todos los hallazgos pendientes en el `task.md`** local de la conversación actual, para garantizar que el trabajo pendiente quede registrado en la pila de tareas.
