# 🤖 PROMPT DE DELEGACIÓN: Pantalla 8 (Project Template Builder - WBS)

**Rol Asumido para el Agente Receptor:** Fullstack Senior AI Developer (Vue 3 / Spring Boot / MySQL)
**Objetivo:** Desarrollar la Pantalla 8 del iBPMS, garantizando la creación estricta de plantillas jerárquicas (WBS) agnósticas a la ejecución, cumpliendo el SSOT del MVP V1.

---

## 🛑 CONTEXTO OBLIGATORIO (READ FIRST)
Antes de escribir código, debes tener claro el aislamiento funcional de esta pantalla:
1. **La Pantalla 8 NO ejecuta proyectos.** Solo crea la "Estructura Ósea" (Templates Mástros).
2. **La Pantalla 8 NO maneja metodologías (Ágil vs Tradicional).** Eso se decide al instanciar en la Pantalla 9.
3. El frontend enviará un JSON jerárquico profundo, pero el backend lo descompondrá en **5 tablas relacionales estrictas** con `ON DELETE CASCADE`. (La antigua columna JSON en base de datos quedó obsoleta según la auditoría de arquitectura local).

---

## 🏛️ REQUISITOS DE ARQUITECTURA (BACKEND - SPRING BOOT / LIQUIBASE)

1. **Migración Liquibase:** Crear el script de DDL para implementar las siguientes tablas (reemplazo del modelo JSON antiguo):
   - `ibpms_project_template` (id, name, description, status `[DRAFT, PUBLISHED]`, version, created_at, created_by)
   - `ibpms_pt_phase` (FK a template)
   - `ibpms_pt_milestone` (FK a phase, boolean `is_stage_gate`)
   - `ibpms_pt_task` (FK a milestone, `form_key`, int `duration_value`, enum `duration_unit`)
   - `ibpms_pt_dependency` (FK source_task, FK target_task, enum `type [FS, FF, SS, SF]`)

2. **Entidades JPA & Cascade:**
   - Dominio estrictamente jerárquico. `@OneToMany` con `CascadeType.ALL` y `orphanRemoval = true` desde Template hasta Task.

3. **Endpoints REST (Controlador):**
   - `POST /api/v1/design/projects/templates`: Recibe el árbol JSON y ejecuta un Upsert relacional.
   - `POST /api/v1/design/projects/templates/{id}/publish`: Cambia estado a `PUBLISHED` (Inmutable).

---

## 🎨 REQUISITOS FRONTEND (VUE 3 + VITE + PINIA)

1. **Estado Profundo (Pinia):** Implementar `useProjectTemplateStore.ts` que almacene la jerarquía y maneje las mutaciones locales (Drag & Drop) antes de enviar al backend.
2. **Componentes Clave:**
   - `WbsTreeView.vue`: Árbol recursivo. Permite `vue-draggable` para reordenar Fases, Hitos y Tareas.
   - `PropertyInspector.vue`: Panel lateral mutante.
     - *Si selecciona Hito:* Muestra toggle `[ ] Activar Stage-Gate (Bloqueo de Fase)`.
     - *Si selecciona Tarea:* Muestra selector de `form_key` (Extrae datos de la API de Formularios - Pantalla 7.B Obligatoria) y duración estimada.

---

## 🚥 CRITERIOS DE ACEPTACIÓN ESTRICTOS (GHERKIN / BDD) - PARA AUTOMATIZAR EN QA

Asegúrate de implementar que pasen las siguientes pruebas unitarias y de integración:

```gherkin
Feature: Restricciones de Dominio para Plantillas WBS

  Scenario: Bloqueo de Publicación por Tareas Huérfanas de Formulario (AC-1)
    Given un usuario administrador construyendo un WBS en la Pantalla 8
    And existe al menos una tarea en el árbol cuyo 'form_key' es NULO o vacío
    Then el computado Vue 'isPublishable' debe retornar FALSO
    And el botón [ PUBLICAR ] en la UI debe estar 'disabled'
    And el backend debe retornar HTTP 400 si se intenta forzar el POST a '/publish' con tareas sin formulario.

  Scenario: Validación de Grafo Acíclico Dirigido - Evitar Deadlocks (AC-2)
    Given una estructura donde Tarea A depende de Tarea B (A -> B)
    When el usuario intenta agregar una nueva dependencia indicando que Tarea B depende de Tarea A (Ciclo: A -> B -> A)
    Then la mutación local en el Store (Frontend) debe ser rechazada alertando "Ciclo Circular Detectado"
    And el Backend (Java) debe rechazar el payload HTTP con estado 422 Unprocessable Entity mediante una validación de clasificación topológica.

  Scenario: Inmutabilidad de Versiones Publicadas (AC-3)
    Given una plantilla de proyecto 'Apertura Sucursal' con status 'PUBLISHED'
    When el administrador la consulta mediante GET '/api/v1/design/projects/templates/{id}'
    Then el Frontend debe renderizar todo el 'WbsTreeView' y el 'PropertyInspector' en modo 'Read-Only'
    And el único botón de mutación permitido será [ Crear Nueva Versión ] (Que clona la estructura y resetea el status a 'DRAFT').
    And cualquier mutación directa vía POST hacia la versión publicada fallará (HTTP 409 Conflict).

  Scenario: Dependencias Trans-Fase (Cross-Phase) (AC-4)
    Given un WBS con "Fase 1" y "Fase 3"
    Then el sistema DEBE permitir que una Tarea dentro de "Fase 3" registre una dependencia (Ej. Finish-to-Start) apuntando a una Tarea dentro de "Fase 1".
    And la lógica de dependencias no debe aislarse a tareas "hermanas" bajo el mismo Hito.
```

---
**NOTA PARA EL AGENTE RECEPTOR:**
Acepta este prompt confirmando: *"Entendido. Reglas de Pantalla 8 (Inmutabilidad, DDL Relacional y Gherkins Trans-Fase) asimiladas. Procedo con la implementación estructurada."*
