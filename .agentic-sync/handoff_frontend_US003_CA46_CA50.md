# Contrato de Arquitectura Frontend (US-003 Iteración 10: CA-46 al CA-50)

**Rol:** Desarrollador Frontend Vue 3 / TypeScript (Componentes y Formularios Zod).
**Objetivo:** Desarrollar los mecanismos de enmascaramiento superficial, validaciones condicionales complejas y campos invisibles.

## 📋 Contexto y Criterios de Aceptación (Alcance Estricto):
Esta iteración define controles estéticos y reglas condicionales puras sobre `FormDesigner.vue`:

*   **CA-46 (Sello Visual de Aprobatoria con Rol):** En el renderizador final, si el Formulario detecta que una etapa ha sido finalizada y cuenta en el `prefillData` (Iteración 9) con datos del Revisor (Ej. `approvedBy`), debe renderizar nativamente un bloque/Badge estático visual (Ej. "Aprobado por X - Gerente").
*   **CA-47 (Campos Ocultos - Hidden Inputs):** Añade el componente `hidden` a la paleta IDE. Este no se pinta nunca en modo ejecución (solo se ve como un bloque gris en el modo IDE de diseño). Su Zod asociado es un `z.any()` o `z.string()`.
*   **CA-48 (Validaciones Condicionales Required-If):** Agrega a las propiedades avanzadas de un componente un selector booleano de dependencia. Extiende el AST Zod en el Editor para que genere lógicas del estilo condicional si A=Válido entonces B=Aplica regla. (Pista: Mapeo de validación reactiva sobre el objeto intermedio mediante `.superRefine` si es necesario, asegurando coherencia cruzada).
*   **CA-49 (Restricción de Cantidad Mínima y Máxima de Adjuntos):** En el componente `file` de subida de adjuntos, añade Mínimo y Máximo de archivos. Evita la subida a nivel Vue si `files.length` no cumple la cota.
*   **CA-50 (Traducción Silenciosa de Formatos - Stripping):** Sobre las Máscaras de Entrada (añadidas en CA-36), debes asegurar un hook previo al envío. Cuando el operario pulsa `[Enviar]`, clona el objeto `formData` reactivo, recorre el Diccionario del esquema y en los inputs Numéricos borra comas espurias o símbolos de peso (Stripping "$ 150.000" a `150000`) **ANTES** de ejecutar Axios POST contra Camunda.

## 📐 Reglas de Desarrollo:
1. El stripping del CA-50 es crítico para no explotar la BD PostgreSQL detrás de Camunda mediante NumberFormatExceptions.
2. Asegura que el *Hidden Input* soporte bindig para inyectar IDs técnicos.

## 🛑 MANDATO LOCAL GATEKEEPER (REGLA DE ORO)
Tienes **ESTRICTAMENTE PROHIBIDO** hacer un `git commit` directamente. 
Edita el FormDesigner.vue, ZodBuilder.ts y utilitarios. Luego encierra el trabajo en caja fuerte local:
`git stash save "temp-frontend-US003-ca46-ca50"`

Reporta con un mensaje de "Éxito Stash" exclusivo cuando finalices el comando.
