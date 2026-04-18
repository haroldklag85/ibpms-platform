# Análisis Funcional Definitivo: US-006 (Diseñar la Estructura Base (WBS) de una Plantilla de Proyecto)

## 1. Resumen del Entendimiento
La US-006 parametriza la creación de Plantillas Maestras (Templates) para la orquestación del trabajo. A diferencia de un BPMN (Proceso dinámico en vivo), esto define un "Esqueleto" de actividades estáticas, jerárquicas (WBS) o ágiles (Sprints) que los Gerentes pueden clonar repetitivamente para evitar crear proyectos complejos desde cero.

## 2. Objetivo Principal
Estandarizar y acelerar la creación de proyectos repetitivos en la organización, otorgando a la PMO una herramienta centralizada (Pantalla 8) donde encapsulan Fases, Sub-fases, Tareas, dependencias y formularios Zod pre-atados, forzando un marco de trabajo corporativo uniforme.

## 3. Alcance Funcional Definido
**Inicia:** Cuando la PMO decide crear una Nueva Plantilla en la Pantalla 8, seleccionando su tipología rígida (Ágil vs Tradicional).
**Termina:** En el despliegue del Snapshot "V1.0" inmutable de la plantilla que queda disponible globalmente para instanciar nuevos proyectos.

## 4. Lista de Funcionalidades Incluidas
- **Definidor WBS Drag&Drop:** Interfaz jerárquica para anidar tareas.
- **Límite de Profundidad:** Bloqueo arquitectónico a un máximo de 5 niveles de anidación.
- **Versionamiento Inmutable Parcial (Snapshot):** Actualizar la plantilla maestra genera V2, pero congela a los proyectos Vivos sobre la V1 original.
- **Comportamiento Polimórfico (Ágil vs Gantt):** La UI muta si el PMO elige "Ágil", desapareciendo las Flechas de Dependencia (Fin-a-Inicio) y los "Hitos".
- **Definición de Terminado Forzoso (DoD Zod):** Arrastra logísticamente una tarea a estado "DONE" solo si el Frontend certifica criptográficamente el llenado del Formulario atado.
- **Locatividad Evolutiva:** Los Scrum Masters pueden borrar tareas del clon en su proyecto instanciado, sin alterar la Maestra.

## 5. Lista de Brechas, Gaps o Ambigüedades Detectadas
- **Fuga de Referencia en Assets (⚠️ CRÍTICO ARQUITECTÓNICO):** El CA-2 garantiza que los proyectos Vivos no se alteren cuando la Maestra pasa a V2 (quedan en V1). Pero ¿qué sucede con los Formularios vinculados a esas tareas maestras? Si un formulario X de la V1 se elimina físicamente porque ya la V2 no lo demanda, los proyectos vivos en V1 colapsarán por un UUID (`form_id`) muerto. El requerimiento carece de una cláusula de "Bloqueo de Borrado por Reference Count" en DB.
- **Exclusividad Ágil en DoD (CA-4):** El texto especifica que al llenar el Formulario QA la tarea se arrastra a "DONE" para proyectos Ágiles. ¿Qué ocurre con proyectos "Tradicionales"? Nunca se menciona.

## 6. Lista de Exclusiones (Fuera de Alcance V1)
- Auto-Asignación de recursos (Personas/Empleados) en la Plantilla Maestra. Solo se asignan Roles/Skills, no Nombres.
- Exportación/Importación masiva XML/MS Project (`Diferido a V2`).

## 7. Observaciones de Alineación o Riesgos
**Riesgo Medio:** Se necesita una directriz de integridad referencial. Una plantilla nunca debe permitir la deshidratación de sus Form Keys mientras existan instancias vivas de sus versiones anteriores.
