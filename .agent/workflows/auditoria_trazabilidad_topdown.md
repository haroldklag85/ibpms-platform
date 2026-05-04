# Flujo de Trabajo de Auditoría de Trazabilidad Estricta (SOP)

Este documento define el comportamiento algorítmico y las reglas inquebrantables para los agentes de IA al ejecutar auditorías de Historias de Usuario (US) y Criterios de Aceptación (CA) en la plataforma iBPMS.

## 🎯 Objetivo
Garantizar la trazabilidad del código, auditar el cumplimiento arquitectónico y detectar brechas de implementación **sin generar código nuevo ni depender de búsquedas semánticas propensas a alucinaciones.**

---

## ⚙️ FASE 1: Inicialización y Contexto Estricto
1. **Reglas Base:** Leer y acatar obligatoriamente `.cursorrules` para definir lo permitido y prohibido.
2. **Arquitectura:** Cargar contexto estructural leyendo exclusivamente el directorio `docs/architecture/`.
3. **Fuente de la Verdad (SSOT):** Leer la US y el CA objetivo **únicamente** desde `docs/requirements/v1_user_stories_index.md`. Queda prohibido confiar en documentos de "Handoff" históricos de `.agentic-sync` para validar requerimientos.

## 🧭 FASE 2: Navegación Estructural (Top-Down)
1. **Cero Búsqueda Semántica:** Queda estrictamente prohibido usar búsquedas por palabras clave para localizar lógica.
2. **Rastreo Manual:** La navegación debe partir de las capas externas hacia las internas:
   - **Backend:** `Controller` → `Service/UseCase` → `Repository/Port` → `Entity/DTO`.
   - **Frontend:** `Router` → `View` → `Component` → `Store/Composable` → `API Client`.

## 🔍 FASE 3: Lectura Profunda y Validación
1. **Evaluación de Completitud:** Leer el código para determinar si el CA está implementado totalmente, parcialmente o si está ausente.
2. **Evaluación Arquitectónica:** Validar si el código cumple con las reglas de arquitectura.
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
2. **Generación de Reporte:** Al finalizar, generar obligatoriamente un reporte Markdown llamado `Reporte_Auditoria_US_XXX.md` que contenga:
   - Archivos de producción y tests etiquetados con éxito.
   - 🚨 **Brechas de Implementación:** Código faltante o parcial, indicando la clase/ubicación donde se interrumpió el rastro.
   - ⚠️ **Violaciones de Arquitectura:** Código que cumple el CA pero rompe las reglas técnicas.
