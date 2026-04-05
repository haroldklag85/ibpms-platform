# Análisis Funcional Definitivo: US-014 (Sugerencia de Tarjetas Acción Operativa - Suggest)

## 1. Resumen del Entendimiento
La US-014 representa el momento culminante (Call-To-Action) del análisis donde el LLM no solo tipifica, sino que toma una decisión ejecutiva recomendada para el humano: Crear una Tarea Aislada o Abrir un Instanciamiento Maestro de Camunda SD.

## 2. Objetivo Principal
Agilizar el *Intake* (Radicación). Pasar del correo amorfo al objeto de negocio fuertemente tipado de iBPMS (Process Instance o Kanban Card) con el mínimo de manipulación tabular.

## 3. Alcance Funcional Definido
**Inicia:** El UI (Pantalla 1B) dibuja la "Action Card" generada.
**Termina:** El usuario Acepta la tarjeta, lo que traslada el correo a la BD Camunda/Kanban y mantiene una copia inmutable en la lista general.

## 4. Lista de Funcionalidades Incluidas
- **Bifurcación Condicional (CA-1):** Determinar si es Tarea Secundaria vs Proceso Nuevo y despachar hacia Embudo de Aprobación.
- **Edición Formularia (CA-2):** El usuario altera URGENCIAS, NOMBRES que la máquina sugirió. Genera log Delta asíncrono para MLOps.
- **Trazabilidad Perenne (CA-3):** Los correos no "Desaparecen" tras su consumo, la P1B funciona como histórico inmutable. El correo clona su `.eml / texto` hacia la Instancia recién creada siendo el Attachment Cero.

## 5. Lista de Brechas, Gaps o Ambigüedades Detectadas
- **Alucinación Funcional en Adjuntos Vivos (⚠️ CA-3):** El CA-3 establece taxativamente que el correo procesado generará "un Clon/copia del correo original formando obligatoriamente la primera pieza probatoria (Attachment 1) de la hoja de ruta en el iBPMS". **GAP:** Si esto significa que el Backend genera físicamente un archivo PDF/EML y lo graba, entonces ES UN ARCHIVO LEGAL SGDEA Persistente. Siguiendo las leyes de la Iteración 6 (US-035 Generación Documental y SHA-256 SharePoint), este clon debe tener firma hash y enviarse a O365 obligatoriamente, pero no hay cruce referenciado de este flujo a US-035. Existe un desalineamiento de Persistencia Forense Inbound.

## 6. Lista de Exclusiones (Fuera de Alcance V1)
- Ejecución automática de Acción Autónoma (Sin click "Create"). Human-In-The-Loop es obligatorio (Confirmación requerida).

## 7. Observaciones de Alineación o Riesgos
**Fricción de Concurrencia:** Durante la inserción del "Clon" en Camunda (BPMN), el motor es extremadamente ineficiente guardando JSON o Strings Masivos como una variable de motor Activa (`ACT_RU_VARIABLE`). El correo clonado NO debe de guardarse dentro de la instancia misma, debe asociarse como URL hacia el metadato maestro en `ibpms_metadata_index`. Guardarlo "Adentro" del WorkItem penalizará drásticamente el performance de Java/Camunda 8.
