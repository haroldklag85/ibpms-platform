# Schema Arquitectónico de Componentes UI (Dynamic Forms)

**Última Actualización:** 2026-02-25
**Alineación:** Este documento cumple el "Must Have #4 (Formularios Dinámicos Nativos)" y rige la comunicación entre Spring Boot (Camunda) y Vue 3.

## 1. Patrón Arquitectónico (Server-Driven UI)
Para evitar recompilar y redesplegar el frontend de Vue 3 cada vez que el negocio agrega un nuevo campo a un formulario de Expediente (Ej: "¿Tiene antecedente penal?"), la plataforma iBPMS utiliza el patrón **Server-Driven UI**. 

El Backend **NO** expone HTML. El backend expone un JSON puramente semántico que describe "Qué pedir", y el Frontend de Vue 3 tiene una librería de Micro-componentes (`<FormRenderer />`) que sabe cómo convertir cada nodo de ese JSON en un `<input>`, un `<select>` o un botón de Tailwind.

---

## 2. JSON Schema Contract v1.0

Cuando el Frontend consume la API `/api/v1/tasks/{taskId}/form`, el Backend deberá retornar una estructura idéntica a esta:

```json
{
  "formId": "frm_crear_cliente",
  "title": "Registro de Cliente Nuevo",
  "version": "1.0",
  "layout": "vertical", 
  "components": [
    {
      "id": "customer_id",
      "type": "text",
      "label": "Número de Identificación",
      "placeholder": "Ej: 10203040",
      "required": true,
      "readonly": false,
      "validation": {
        "regex": "^[0-9]{5,15}$",
        "errorMessage": "La identificación debe ser numérica (5-15 dígitos)."
      },
      "defaultValue": null
    },
    {
      "id": "customer_type",
      "type": "select",
      "label": "Tipo de Persona",
      "required": true,
      "options": [
        { "value": "FISICA", "label": "Persona Física" },
        { "value": "JURIDICA", "label": "Persona Jurídica" }
      ]
    },
    {
      "id": "country_id",
      "type": "catalog_select",
      "label": "País de Residencia",
      "required": true,
      "catalogSource": "/api/v1/catalogs/countries",
      "dependentOn": null
    },
    {
      "id": "birth_date",
      "type": "date",
      "label": "Fecha de Nacimiento",
      "required": false,
      "validation": {
        "max": "TODAY"
      }
    },
    {
      "id": "ai_draft_suggested",
      "type": "textarea",
      "label": "Borrador M365 Sugerido (Validar)",
      "readonly": false,
      "hidden": false,
      "rows": 8,
      "defaultValue": "Estimado cliente, hemos recibido su solicitud..."
    }
  ],
  "actions": [
    {
      "id": "btn_submit",
      "type": "submit",
      "label": "Guardar y Continuar",
      "theme": "primary"
    },
    {
      "id": "btn_cancel",
      "type": "cancel",
      "label": "Cancelar Tarea",
      "theme": "secondary"
    }
  ]
}
```

---

## 3. Diccionario de Componentes (`components[].type`)

El Agente Frontend (Vue 3) está **obligado** a construir un componente aislado por cada uno de estos tipos base y ensamblarlos dinámicamente:

| Tipo (`type`) | Componente Vue 3 | Descripción Arquitectónica |
| :--- | :--- | :--- |
| `text` | `<AppInputText />` | Campo de texto plano de una línea. Soporta RegEx en el nodo de validación. |
| `textarea` | `<AppTextarea />` | Caja de texto multilínea, vital para revisar los borradores que escupe el Copiloto M365 NLP. |
| `number` | `<AppInputNumber />` | Campo numérico estricto, permite decimas o rangos Mín/Máx. |
| `date` | `<AppDatePicker />` | Selector de fechas. Debe exportar el dato en formato ISO-8601 (`YYYY-MM-DD`). |
| `select` | `<AppSelect />` | Lista desplegable con opciones **estáticas** hardcodeadas en el nodo `options`. |
| `catalog_select` | `<AppCatalogSelect />` | **CRÍTICO:** Manda un llamado REST asíncrono hacia `catalogSource` para poblar la lista dinámica (Ej: Países, Ciudades, Monedas). |
| `checkbox` | `<AppCheckbox />` | Retorna un valor Booleano (`true`/`false`). |

---

## 4. Reglas de Salida del Payload (Al Enviar)
Cuando el usuario interactúa con la UI (creada dinámicamente por este JSON) y oprime el botón `submit`, el Frontend **NO** debe mandar toda la estructura de vuelta.

El Payload que el Agente Frontend mandará hacia `POST /api/v1/tasks/{taskId}/complete` será únicamente un bloque llave-valor de las variables extraídas, usando el atributo `id` como la llave:

```json
{
  "variables": {
    "customer_id": "11440001",
    "customer_type": "FISICA",
    "country_id": "CO",
    "birth_date": "1990-05-15",
    "ai_draft_suggested": "Estimado cliente, hemos recibido su solicitud... (Modificado por el Humano)"
  }
}
```

## 5. Instrucciones de Cierre para los Agentes IA
*   **Agente Backend:** Nunca envíes HTML en tus respuestas de tareas. Utiliza el framework de Camunda (Camunda Forms, o custom serializations) para exportar el contrato JSON superior.
*   **Agente Frontend:** Inicia tu código creando un `<Component :is="resolverType(component.type)" />` en Vue 3 para iterar limpiamente el arreglo de componentes.
