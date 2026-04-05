# Análisis Funcional Definitivo: US-039 (Formulario Genérico Base - El Camaleón Operativo)

## 1. Resumen del Entendimiento
La US-039 describe una interfaz comodín (Pantalla 7.B) que el sistema inyecta autómaticamente cuando una Tarea Humana (UserTask) carece de un formulario personalizado o cuando es un Kanban huérfano. Es una pantalla "minimalista" diseñada para tareas de trámite rápido, soportando únicamente una grilla de Solo Lectura con la información clave del caso y una zona inferior para carga de archivos adjuntos y observaciones de flujo.

## 2. Objetivo Principal
Reducir sustancialmente el trabajo burocrático (Overhead) de los Arquitectos IT, evitando que malgasten tiempo construyendo decenas de Formularios "Fantasma" solo para que un operario pueda oprimir el botón "Siguiente" o subir un PDF. Busca estandarizar los pases de manos rápidos.

## 3. Alcance Funcional Definido
**Inicia:** En la parametrización de la tarea (Pre-Flight, Pantalla 6), cuando el Arquitecto asigna el formulario vacío/camaleónico (`sys_generic_form`).
**Termina:** En la renderización de la tarea para el usuario final, incluyendo las inyecciones forzadas de texto si hay un desvío (Botón de Pánico / Error Event).

## 4. Lista de Funcionalidades Incluidas
- **Pre-Flight Analizer (SRE Governance):** Bloquea el despliegue del proceso si la tarea a la que se le ató el Camaleón pertenece a Alta Dirección (VP), Aprobadores Financieros, o exige Firma Legal (Anti-Bypass).
- **Anti-Context Bleeding (Filtro BFF):** El Backend for Frontend depura los N variables sucias del proceso de Camunda, inyectando al cliente un payload limpio solo con los datos básicos del negocio para no saturar memoria.
- **Botón de Pánico (Mutación):** Si el operador oprime "Devolver" o "Cancelar" (Error Events), la pantalla levanta un modal forzando obligatoriamente una justificación narrativa (Mínimo 20 caracteres) previo al rechazo en motor.

## 5. Lista de Brechas, Gaps o Ambigüedades Detectadas
- **Fuga de Estado de Adjuntos (⚠️ CA-3):** Cuando el operador escoge usar los "Botones de Pánico" y aborta/devuelve la etapa, inyecta su justificación de 20 caracteres y el sistema detona un "Error Event". Sin embargo, si durante esta misma tarea el operador previamente arrastró un Excel/PDF al Dropzone, el texto de la US-039 no aclara estructuralmente qué ocurre con los binarios temporales asociados a este `TaskID`. ¿Se destruyen o se enlazan al "Error Event"? Al dejar la tarea, podríamos generar huérfanos documentales o peor, indexar un documento en SGD a una tarea que técnicamente fue abortada.

## 6. Lista de Exclusiones (Fuera de Alcance V1)
- Lógicas complejas, componentes matemáticos o validaciones Zod asimétricas en este lienzo (Exclusivo de US-003).
- Tipado estricto `Toggle` binario para controlar UI. Se utiliza lógica de estructura semántica.

## 7. Observaciones de Alineación o Riesgos
**Riesgo Medio:** Faltan las tuercas transaccionales que unen esta historia genérica con el guardado estricto de borrador que dicta la US-029. De resto, el bloqueo Pre-Flight (CA-1) blinda excelentemente la usabilidad directiva, previniendo que formularios escuetos manejen millones de dólares.
