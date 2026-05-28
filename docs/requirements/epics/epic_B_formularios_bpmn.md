# [TÍTULO DE LA ÉPICA]
> **Fuente:** Modularizado desde `v1_user_stories.md` | **Fecha:** 2026-04-10
> **Coverage:** `.agentic-sync/coverage_matrix.md` (centralizada)

---

### US-003: Instanciar y Generar un Formulario "iForm Maestro" vs "Simple"
**Como** Arquitecto Frontend / Administrador
**Quiero** elegir el tipo de formulario y ver cómo el sistema genera código Vue 3 y Zod en tiempo real mientras arrastro componentes
**Para** no tener deudas técnicas (Vendor Lock-in) y construir expedientes (iForm Maestros) que manejen etapas dinámicamente.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Web IDE Form Code Generation
  Scenario: Seleccionar Patrón de Formulario (CA-1)
    Given que el desarrollador crea un nuevo recurso en la sección "Formularios"
    When el modal pregunta "¿Qué arquitectura de formulario desea?"
    Then el usuario puede elegir "Patrón B: iForm Maestro (Expediente Multi-Etapa)"
    And el lienzo visual se estructura para basar el renderizado en la variable "Current_Stage" de Camunda

  Scenario: Análisis Bidireccional de Código en Tiempo Real (CA-2)
    Given que el usuario está en el Canvas del "iForm Maestro"
    When arrastra un "Input Text (Monto Aprobado)" y marca "Requerido"
    Then el panel derecho "Mónaco IDE" de código actualizado escribe automáticamente:
      """javascript
      const schema = z.object({ monto_aprobado: z.number().positive() })
      """
    And si el usuario borra la línea de Zod en el panel de código, el componente visual pierde instantáneamente su validación de Requerido.

  Scenario: Iconos de Ayuda en Pestañas de Código (CA-3)
    Given el Arquitecto está en la Pantalla 7 en la vista de Mónaco IDE
    Then a la par de las pestañas `<script setup>` y `<style scoped>` debe haber un icono de ayuda [?]
    And al hacer hover, un Tooltip explica de forma concisa la función de cada pestaña (Ej: "Aquí va la lógica de Vue y validaciones Zod" / "Aquí va el CSS del componente").

  Scenario: [Arquitectura] Sandboxing Estricto contra XSS (AST Evaluator) (CA-4)
    Given que el constructor del iForm inyectó una regla de negocio Javascript en un campo dinámico
    When el motor de renderizado de la Pantalla 7 interpreta el formulario en el navegador
    Then la plataforma prohíbe estructuralmente el uso de la función `eval()`
    And toda expresión JS es analizada y ejecutada internamente mediante un intérprete de gramática seguro (Abstract Syntax Tree Parser) que rechaza cualquier intento de manipulación del `window`, `document` o peticiones `fetch`.

  Scenario: [Arquitectura] Factoría Reactiva de Zod On-The-Fly (CA-5)
    Given la estructura JSON del formulario generada por el IDE
    When el motor requiere validar los campos renderizados en pantalla
    Then el sistema NO emite archivos estáticos de código fuente JS para re-compilar
    And instanciará dinámicamente el esquema global cruzado utilizando la factoría de validaciones `zod` conectada en tiempo real a la memoria reactiva (`reactive()`) de Vue.

  Scenario: [Arquitectura] Aislamiento Perimetral CSS (Shadow DOM) (CA-6)
    Given que el usuario redactó reglas exóticas de CSS para colorear botones específicos de su Formulario
    When la pantalla cliente dibuja el componente en el Workdesk
    Then el iBPMS encapsulará todo el componente inyectado usando el estándar HTML5 "Shadow DOM"
    And ninguna de las clases CSS inyectadas podrá sangrar (Style Bleed) hacia el exterior ni distorsionar la barra superior o menús laterales de Tailwind corporativo.

  Scenario: [Arquitectura] Render Functions, Teleportación y Z-Index Orchestrator (CA-07)
    Given una directriz para renderizar componentes infinitamente anidados (Ej: Grillas dentro de Módulos dentro de Secciones)
    Then el motor subyacente de Vue prescindirá del HTML rígido (`<template>`) utilizando funciones programáticas puras de Virtual DOM (`h()`) para renderizado ultrarrápido
    And los Tooltips y Modales usarán la etiqueta nativa `<Teleport to="body">` rompiendo el z-index local.
    But OBLIGATORIAMENTE el DOM instanciará un Orquestador Global (Z-Index Manager) con jerarquía dogmática:
    And `Z-900` para Modales UI, `Z-1000` para Tooltips, y `Z-5000` restrictivo para Cobertura de Errores Fatales (SweetAlert/Toasts), garantizando que las fallas de red del Motor NUNCA queden ocultas detrás del formulario.

  Scenario: [Arquitectura ERP] Navegación Modular y Agrupación de Malla (CA-8)
    Given que el Arquitecto está diseñando un "iForm Maestro" de alta densidad (Ej: 100+ campos)
    When el usuario arrastra componentes visuales al lienzo
    Then la Pantalla 7 dispondrá nativamente de agrupadores tipo "Micro-ERP": Pestañas (Tabs), Paneles Acordeón (Collapsible Panels) y Grillas de Datos Anidadas (DataTables con CRUD)
    And para evitar el diseño infinito vertical, segmentando el formulario visualmente a nivel departamental (Ej: Pestaña "Datos Clínicos", Pestaña "Análisis Legal").

  Scenario: [Arquitectura Multi-Expediente] Cohabitación de Maestros en un Proceso (CA-9)
    Given un Proceso BPMN (Camunda) de largo aliento con distintas esferas de confidencialidad
    When el Arquitecto vincula formularios a las Tareas (`UserTasks`)
    Then la plataforma permitirá asociar MÚLTIPLES "iForm Maestros" distintos dentro de una misma Instancia de Proceso (Ej: Maestro Comercial al inicio, Maestro Contractual al final)
    And cada Maestro gestionará su propio esquema Zod y persistencia independiente en CQRS, evitando la creación de un único "Monolito JSON" inmanejable para toda la organización.

  Scenario: Inmersión Funcional "Alt+Tab Zero" (Full-Screen Focus) (CA-10)
    Given un Operario abriendo un "iForm Maestro" desde su Bandeja en Pantalla 2
    When la interfaz dibuja el componente en el navegador
    Then el sistema forzará que el Contenedor ocupe el 100% del Ancho y Alto útil (Experiencia ERP Inmersiva)
    And embebiendo sub-elementos (Previsores PDF, Calculadoras en línea) dentro del mismo componente Vue para evitar forzar al usuario a minimizar el iBPMS o abrir aplicaciones satélite para resolver la Tarea.
  Scenario: Paleta de Componentes Base HTML5 (Formulario Simple y Maestro) (CA-11)
    Given que el desarrollador está diseñando un formulario en el lienzo de la Pantalla 7
    Then la plataforma debe proveer una paleta tipificada que garantice la renderización de todos los estándares HTML5 y su mapeo a Zod:
    And Textos: `text` (Corto), `textarea` (Largo), `password`, `email`, `url`, `tel`, `search`, `hidden`.
    And Números y Rangos: `number`, `range`, `meter`, `progress`.
    And Fechas y Tiempos: `date`, `datetime-local`, `time`, `month`, `week`.
    And Selección y Opciones: `select` (con sus `option` y `optgroup`), `datalist`, `checkbox`, `radio`.
    And Estructura y Acción: `submit`, `reset`, `button`, `image`, `file`, `color`, `output`, `fieldset`, `legend`, `label`.
    And cada uno de estos componentes emitirá su tipo de dato UI y su esquema Zod correspondiente para la validación bidireccional.

  Scenario: Componente de Ventana Emergente (Pop-ups Informativos) (CA-11B)
    Given la necesidad del Arquitecto de mostrar "Avisos" o Términos y Condiciones obligatorios
    When arrastra el componente "Modal Informativo" a la grilla y lo asocia a un Botón (Ej: "Ver Políticas") o a una Regla de Estado de Carga
    Then la plataforma invocará un `<Teleport to="body">` (bajo la jerarquía SRE Z-900) para oscurecer el fondo.
    And presentará un diálogo flotante (Pop-up) en lectura plana, con un botón obligatorio de [Entendido] para cerrarlo.
    But por gobernanza V.I.D.A., este componente es estéril (Carece de `I/O Binding` a Camunda); existe puramente para control de notificaciones UI y no contamina el Request JSON.

  Scenario: [Integración Motor] Drag & Drop Sensorial de Process Variables (CA-12)
    Given que el Arquitecto está diseñando en el Mónaco IDE (Pantalla 7) vinculado a una User Task (Ej: "Aprobar Crédito")
    When importa el archivo subyacente `.bpmn` o conecta el IDE mediante API contra Camunda para leer el Diccionario de Datos del proceso activo
    Then el IDE enlistará las `Process Variables` detectadas en un panel lateral
    And al arrastrar una variable (Ej. `monto_credito` tipado numérico) hacia el Canvas, el motor adivinará su componente Vue lógico (`<InputNumber>`) y redactará automáticamente `z.number()` en el panel de Zod.

  Scenario: [Arquitectura de Datos] Mapeo de Entradas y Salidas Form-To-Process (CA-13)
    Given el panel de propiedades globales del formulario en el IDE
    Then el desarrollador dispondrá de dos matrices declarativas de vinculación (Input/Output Mapping):
    And 1. Carga (`onMounted`): El mapeo estricto de qué variables leerá de la API de Camunda para pre-poblar o condicionar el formulario (`prefillData` de US-029).
    And 2. Retorno (Submit): Un contrato explícito indicando qué porción específica de la data recolectada se serializará y reinyectará en las `Process Variables` del motor al hacer el POST `Complete Task`, blindando el motor de basura transaccional.

  Scenario: [Accionadores] Botones Nativos de Estado Camunda (Task Lifecycle) (CA-14)
    Given la necesidad de gobernar el ciclo vital del Token directamente desde la UI sin que el desarrollador escriba llamadas Axios manuales
    When el Arquitecto abre la paleta de componentes "Botones de Acción" en Pantalla 7
    Then dispondrá de componentes drag&drop *Smart Buttons* pre-conectados a las APIs core (`/workbox/tasks`):
    And `[Completar]` (Avanza el proceso enviando I/O), `[Reclamar/Liberar]`, `[Auto-Guardar Borrador]` (Persistencia Local de US-029), y `[Generar BPMN Error]` (Desvío técnico hacia un Evento Intermedio de Error en Camunda).

  Scenario: [Arquitectura de Resiliencia] Captura Automática de Errores Core (Smart Buttons) (CA-15)
    Given el desarrollador ha finalizado el diseño del "iForm Maestro" y procede a hacer clic en `[Publicar]` en la Pantalla 7
    When el motor de compilación Mónaco IDE analiza el código fuente del componente Vue generado
    Then el IDE verificará explícitamente la presencia de manejadores de eventos (Action Listeners) conectados a la API de Camunda (Ej. Los Smart Buttons de la Paleta)
    And la plataforma inyectará nativamente "por debajo" un bloque global `try/catch` o un Interceptor de red sobre dichos botones.
    And si las APIs de Camunda o CQRS (US-029) retornan un HTTP 5xx (Timeout o Crash), el Smart Button suspenderá su estado de carga (Spinner) y detonará automáticamente un Componente Visual Genérico de Error (Toast/Snackbar corporativo alertando: "Fallo de Comunicación con el Motor Central").
    And bajo ninguna circunstancia se permitirá la publicación de un formulario "Mudo" (Sin captura de errores de red), descargando esta responsabilidad arquitectónica de las manos del desarrollador (Enfoque No-Code Seguro).

  Scenario: [Arquitectura Flexible] Constraint de Bajo Acoplamiento Form-To-Process (CA-16)
    Given que el Arquitecto vinculó el diccionario de un `.bpmn` al panel IDE para el "iForm Maestro"
    When existan discrepancias lógicas entre los campos visuales dibujados y las variables detectadas por Camunda
    Then el lienzo IDE no impondrá bloqueos duros ('No Mapeado') ni abortará la compilación de la UI
    And la coerción de coherencia entre ambas capas recaerá estrictamente sobre el ciclo de QA automatizado (US-028)
    And preservando la agilidad del IDE y el bajo acoplamiento arquitectónico entre el Frontend/Zod y el Motor Central.

  Scenario: Soporte de Motores de Lenguaje (Language Servers en Web IDE) (CA-17)
    Given que el usuario edita el código fuente de un Formulario en las pestañas del Mónaco IDE (Pantalla 7)
    Then la plataforma proveerá Autocompletado, Syntax Highlighting y Linting estricto exclusivamente para: TypeScript, Vue (SFC), SCSS/Tailwind y JSON.
    And descartará a nivel arquitectónico motores de terceros como GraphQL o YAML para mantener el Bundle Size del Editor ultraligero y consistente con la estrategia REST API del Core iBPMS.

  Scenario: [Onboarding Embebido] Tooltips de Ayuda Visual (Propiedades Avanzadas) (CA-18)
    Given que el Arquitecto No-Code selecciona un componente visual en el lienzo para configurar sus Propiedades Avanzadas
    Then el panel lateral (Propiedades) mostrará iconos de ayuda `[?]` al lado de cada título
    And al hacer hover sobre los iconos, el sistema desplegará Tooltips didácticos con el siguiente mapeo funcional:
    | Propiedad | Contenido del Tooltip "Para Dummies" |
    |---|---|
    | **ID (Variable Name)** | "Ésta es 'La Cédula' única del campo para el sistema. Usa solo minúsculas y guiones bajos, sin espacios (Ej: nombre_cliente)." |
    | **Label (Nombre Visible)** | "La pregunta o título oficial que leerá la persona humana (Ej: ¿Cuál es su nombre?)." |
    | **Placeholder / Descripción** | "Texto ejemplo tipo 'fantasma' que guía al usuario y desaparece cuando él escribe (Ej: +57 321 000 0000)." |
    | **Camunda Variable (I/O Binding)** | "El puente de datos. Nombra el 'bolsillo' exacto donde el motor central de Camunda guardará esta respuesta." |
    | **Validaciones Zod** | "El Policía. Marca como Requerido para obligar la respuesta, o usa los límites numéricos para que un texto no sea demasiado largo o corto." |
    | **Regex Automático** | "Escáneres mágicos. Elige una regla (Ej: Correo, Tarjeta de Crédito) para impedir que el usuario ingrese datos inválidos." |
    | **Cross-Field Logic** | "Lógica condicional. Configura si la obligatoriedad de este campo depende de lo que el usuario respondió en otra pregunta." |
    And esta funcionalidad estará aislada del Mónaco IDE, focalizándose única y exclusivamente en la usabilidad del panel Clic-and-Drop.

  Scenario: Maximización de Lienzo Visual (Contracción de Mónaco IDE) (CA-19)
    Given el Arquitecto está diseñando un formulario complejo en el Canvas de la Pantalla 7
    When no necesita visualizar o interactuar con el código en tiempo real (Vue/Zod)
    Then dispondrá de un control visual (Ej. un botón de colapso "`>`" en el borde del panel) para ocultar completamente el "Mónaco IDE"
    And al contraerse, el área del Lienzo Visual se expandirá fluidamente para ocupar el 100% del espacio central, mejorando la ergonomía visual del diseño No-Code.

**Nota Post-MVP (V2):** *Motor White-Label & Theming. El sistema estará cimentado sobre Tailwind CSS V4 / Native Variables, permitiendo en fases posteriores la exposición de un Panel Administrativo para la sobreescritura dinámica de Brand Tokens (Colores primarios, radios y fuentes) por Tenant. Excluido del MVP V1 para priorizar features transaccionales.*
    
**Trazabilidad UX:** Wireframes Pantalla 7 (iForm Builder - Web IDE Bidireccional).
  Scenario: Permisos de Sobrescritura en Campos (CA-20)
    Given un usuario en la Etapa 2 abre un iForm Maestro
    Then puede sobrescribir los valores ingresados previamente en la Etapa 1
    But solo si su Rol RBAC tiene permisos explícitos de escritura sobre esos campos, de lo contrario se renderizan como "Solo Lectura".

  Scenario: Enrutador de Archivos Adjuntos por TRD (CA-21)
    Given un usuario sube un archivo en un componente de Adjuntos del formulario
    Then el sistema lee la configuración de las Tablas de Retención Documental (TRD) del proceso
    And rutea el archivo automáticamente a la Bóveda SGDEA Interna (Pantalla 12) o a Microsoft SharePoint según indique la TRD
    And NO se guarda en la Base de Datos transaccional (Diferido a V2).

  Scenario: Validación Reactiva Zod Defensiva (Debounce & Blur) (CA-22)
    Given un usuario final está diligenciando un iForm Maestro denso en su Workdesk
    When incumple una regla de validación (Ej: escribe 3 números en un campo que exige 10)
    Then el formulario NO re-evaluará el AST global de Zod en cada pulsación de tecla (Keystroke) para proteger el Event Loop del navegador (Prevenir DOM Thrashing).
    And la inyección del error en vivo se disparará exclusivamente mediante validación perezosa (`@blur` al perder el foco) O mediante un `Debounce` estricto de 400ms después de que el operario deje de escribir.

  Scenario: Estilos CSS Corporativos Estandarizados V1 (CA-23)
    Given el Arquitecto diseña un formulario
    Then todos los componentes visuales heredan la hoja de estilos de "Marca Corporativa" global
    And NO es posible customizar el color/fuente de cada botón individualmente en V1 para asegurar consistencia (Diferido a V2).

  Scenario: Auto-Guardado de Borrador en Workdesk (CA-24)
    Given un usuario final está llenando un formulario extenso en la Pantalla 2
    Then cada interacción se guarda automáticamente como un borrador en caché local (o BD temporal)
    And si el usuario cierra la pestaña por error, al volver a abrir la tarea, recupera los datos ingresados no enviados.

  Scenario: Reglas de Visibilidad Condicional (CA-25)
    Given el Arquitecto configura la propiedad "Dependencia Visual" de un campo B
    When en el lienzo visual el usuario final marca un Checkbox A
    Then el campo B aparece dinámicamente ("Campo Fantasma") empujando el resto de la estructura hacia abajo (layout reactivo Vue).

  Scenario: Prevención Contra Borrado de Formularios Activos (CA-26)
    Given el Arquitecto intenta eliminar el "Form_Solicitud_V1" en la Pantalla 7
    When el sistema detecta que existen instancias de procesos "en vuelo" que requieren de este formulario
    Then se cancela la eliminación y se muestra un mensaje de Error: "Prohibido: Este formulario está siendo usado por N procesos activos."

  Scenario: Control de Versiones de Diseño de Formulario (CA-27) [REMEDIACIÓN]
    Given un formulario existente cargado en el IDE de la Pantalla 7 con identificador `currentFormId`
    When el Arquitecto presiona el botón "💾 Guardar Versión" en la barra de herramientas del diseñador
    Then el Frontend realiza un POST a `/api/v1/forms/{formId}` enviando el payload del diseño (esquema, campos, plantilla)
    And el Backend genera una nueva versión inmutable (N+1) si el formulario está activo, retornando la información de versión
    And la store del diseñador refresca la lista de versiones consultando `/api/v1/forms/{id}/versions` sin mocks
    And al seleccionar una versión del historial, la función `restoreVersion` decodifica y restaura el esquema reactivo de los campos.

  Scenario: Bitácora de Auditoría a Nivel de Campo (CA-28)
    Given el usuario "maria.lopez" sobrescribe un valor que había puesto "juan.perez" en una etapa previa
    Then el backend inserta un registro en una tabla de auditoría (Ej: FormFieldValueAudit)
    And un Revisor puede ver un panel flotante "Bitácora" que lista "María cambió 'Costo' de 100 a 150 a las 14:00".

  Scenario: Dropdown Alimentado por Exportación CSV (CA-29)
    Given el Arquitecto agrega un componente Dropdown (Select) al Lienzo
    Then en el panel de propiedades tiene la opción de "Cargar archivo .CSV"
    And al subir el archivo, el Dropdown se puebla automáticamente con las opciones (Ej: Países, Áreas, Tipos de Documento) en lugar de tipearlas una a una.

  Scenario: Autocompletado mediante Integración API / BD Externa (CA-30) [REMEDIACIÓN]
    Given un campo en el Canvas configurado con `enableAutocomplete` habilitado, `autocompleteUrl` y mappings de atributos en formato JSON
    When el código compilado genera un handler `@blur` que invoca a la función asíncrona generada `handleAutocomplete_[fieldId]()`
    Then en tiempo de ejecución, al perder el foco (blur) el usuario, se realiza una petición GET al endpoint con el query parameter `?q=[valor]`
    And el componente `FormRenderer.vue` intercepta la respuesta mapeando dinámicamente cada campo según las llaves configuradas en el formulario.

  Scenario: Componente de Firma Electrónica Manuscrita (CA-31)
    Given el Arquitecto requiere formalizar un acuerdo en el formulario
    Then puede arrastrar un componente de "Firma a Mano Alzada" (Canvas HTML5) al Lienzo
    And el usuario final puede dibujar su firma con el mouse o pantalla táctil
    And el sistema guarda la firma como una imagen (Ej: Base64/PNG) anexa al Payload del formulario.

  Scenario: Validaciones Cruzadas entre Múltiples Campos (CA-32)
    Given un formulario tiene un componente "Fecha de Inicio" y "Fecha de Fin"
    When el usuario final ingresa una "Fecha de Fin" que es anterior a la "Fecha de Inicio"
    Then el esquema Zod dinámico evalúa la regla cruzada (refinement)
    And muestra inmediatamente un mensaje de error impidiendo el avance, indicando la inconsistencia temporal.

  Scenario: Exportación a PDF del Formulario Diligenciado (CA-33)
    Given un usuario final ha completado de llenar los datos requeridos en pantalla
    Then dispone de un botón global estilo "[⬇️ Exportar a PDF]"
    And al presionarlo, el sistema genera y descarga un PDF con formato de "Documento Físico" que contiene todos los campos y valores renderizados de manera limpia para impresión.

  Scenario: Grupos de Campos Repetibles (Data Grids / Tablas) (CA-34)
    Given el Arquitecto necesita recopilar una lista de longitud variable (Ej: "Múltiples Co-Deudores")
    Then puede utilizar un componente de "Grupo Repetible" (Field Array)
    And el usuario final verá un botón "[+ Agregar]" para duplicar dinámicamente el conjunto de campos configurados sin afectar el esquema Zod subyacente.

  Scenario: Ayudantes Locales (Tooltips y Placeholders) (CA-35)
    Given el Arquitecto configura un campo complejo en el Lienzo
    Then puede configurar un texto "Placeholder" (texto gris de fondo)
    And puede configurar un "Tooltip" (icono ℹ️ que al hacer hover muestra una descripción detallada)
    And el Arquitecto es libre de usar ambos mecanismos simultáneamente para guiar al usuario final.

  Scenario: Máscaras de Entrada (Input Masks) para Formatos Específicos (CA-36)
    Given el Arquitecto configura un campo numérico como "Ingresos Brutos" o "Cédula"
    Then puede aplicarle una Máscara de Formato (Ej: Moneda, Teléfono, Fecha)
    And mientras el usuario final teclea (Ej: "150000"), el sistema formatea visualmente el valor en vivo (Ej: "$ 150.000,00") sin alterar el valor numérico real bajo el capó.

  Scenario: Visor Histórico Inmutable para Auditoría (CA-37)
    Given un usuario Auditor accede a un proceso completado hace años para revisión
    Then el sistema renderiza el formulario con su diseño original exacto
    But todos los componentes están estrictamente en modo "Solo Lectura", sin botón de [Enviar] y congelados contra cualquier manipulación.

  Scenario: Restricciones de Longitud Dinámicas (Zod min/max) (CA-38)
    Given el Arquitecto configura un campo de texto largo (Textarea)
    Then puede definir en el panel de propiedades "Caracteres Mínimos" y "Máximos"
    And el lienzo genera instantáneamente la regla Zod correspondiente (Ej: `z.string().min(5).max(100)`)
    And bloquea el input visualmente cuando el usuario alcanza el límite.

  Scenario: Condicionamiento de Archivos Adjuntos (CA-39)
    Given el Arquitecto agrega un componente de "Subida de Archivos"
    Then el panel de propiedades debe permitir restringir el "Peso Máximo (MB)" y los "Tipos Permitidos (Ej: .pdf, .jpg, .xml)"
    And si el usuario intenta subir un archivo no permitido, el sistema lo rechaza proactivamente antes de enviarlo al servidor.

  Scenario: Dropdown de Búsqueda Interactiva (Searchable Select) (CA-40)
    Given el Arquitecto necesita presentar una lista extensa de opciones (Ej: 195 Países)
    Then el componente Dropdown (Select) debe incluir por defecto un motor de búsqueda interno (Typeahead)
    And permite al usuario teclear para filtrar la lista instantáneamente sin tener que usar el scroll manual.

  Scenario: Restricciones en Grillas Repetibles (Min/Max Rows) (CA-41)
    Given el Arquitecto utiliza un Data Grid (Grupo Repetible)
    Then puede configurar mediante el panel de propiedades cuántas filas como mínimo debe llenar el usuario, y un tope máximo (Ej: Min: 1, Max: 3)
    And el esquema Zod asegura que el arreglo (`z.array`) cumpla estas restricciones bloqueando el botón [+ Agregar] al llegar al límite.

  Scenario: Soporte Multi-Idioma (i18n) (CA-42 - Diferido a V2)
    # NOTA: Diferido a V2. Para V1 todos los formularios son creados y operados estáticamente en Español.
    Given el Arquitecto diseña un formulario
    Then puede habilitar soporte multi-idioma para cambiar el idioma condicionalmente.

  Scenario: Data Binding (Precarga Automática desde Camunda) (CA-43)
    Given un usuario "Carlos" tiene variables persistidas de etapas anteriores almacenadas en el proceso de Camunda
    Then el IDE de Formularios mapea automáticamente todas las variables (variables del motor) usando sus IDs Técnicos
    And cuando el usuario abre la Pantalla 2, los campos coincidentes se auto-rellenan con esos datos históricos almacenados.

  Scenario: Componentes de Calendario y Rangos de Fechas (CA-44 - Rango Diferido V2)
    Given el Arquitecto necesita recopilar fechas
    Then dispone de un componente DatePicker estándar (Selección de un solo día) en V1.
    # NOTA: La selección compleja de "Rango de Fechas" (Drag and Drop en calendario) se difiere a V2.

  Scenario: Multi-Select Visual (Pastillas/Etiquetas) (CA-45)
    Given el Arquitecto configura un campo desplegable que permite selección múltiple
    Then el diseñador o el panel de propiedades permite elegir la presentación visual: "Chips/Etiquetas" o "Pastillas"
    And el usuario final puede eliminar selecciones individuales haciendo clic en la 'x' de la pastilla correspondiente.

  Scenario: Sello Visual de Aprobatoria con Rol (CA-46)
    Given un usuario con Rol "Gerente" completa una etapa de revisión en un iForm Maestro
    Then esa etapa genera un "Badge/Sello" visual estático incrustado
    And dicho sello muestra el Nombre del Usuario y su Rol (Ej: "Aprobado por Juan Pérez - Gerente de Área") para visibilidad en etapas subsecuentes.

  Scenario: Campos Ocultos (Hidden Inputs) para Metadata (CA-47)
    Given el Arquitecto necesita enviar datos técnicos que el usuario NO debe ver ni alterar
    Then puede arrastrar un componente "Campo Oculto (Hidden Input)" al Lienzo
    And puede asignarle valores variables (Ej: `sys_request_id`)
    And esos datos viajan transparentemente en el Payload JSON final al enviarse la tarea.

  Scenario: Validaciones Condicionales (Required-If) (CA-48)
    Given el Arquitecto configura la propiedad "Requerido Condicional" del Campo B
    When en el lienzo el usuario final marca "Sí" en el Campo A
    Then el esquema Zod dinámico hace que el Campo B se vuelva obligatorio
    And si marca "No", el Campo B es opcional y no bloquea el envío del formulario.

  Scenario: Restricción de Cantidad Mínima y Máxima de Adjuntos (CA-49)
    Given el Arquitecto agrega un componente de "Subida de Archivos"
    Then puede habilitar en las propiedades un requerimiento de Volumen (Ej: Mínimo 2 archivos, Máximo 5)
    And el sistema previene el envío del formulario si no se cumple esta cuota exacta.

  Scenario: Traducción Silenciosa de Formatos (Mascara Front vs Dato Back) (CA-50)
    Given el usuario final digita "1.500.230" en un input numérico con máscara visual
    When el formulario se procesa para hacer el POST al motor de tareas (Camunda)
    Then el IDE despoja el formato estético en secreto y envía el Integer/Float puro (`1500230`)
    And garantizando la integridad de los datos para la analítica y reglas de negocio.

  Scenario: Grillas Editables con Protección y Auditoría Parcial (CA-51)
    Given un usuario "Analista 2" requiere agregar filas a un Data Grid donde "Analista 1" ya insertó datos
    Then el Analista 2 puede visualizar y editar toda la grilla si tiene permisos
    And cualquier fila modificada o eliminada que perteneciera al Analista 1 dejará un rastro en la Bitácora de Auditoría (CA-12).

  Scenario: [REMEDIACIÓN] Feedback Visual en Llamadas a APIs (Estado Indeterminado) (CA-52)
    # Origen: us_003_audit_report.md | Decisión: Deshabilitado reactivo de controles y spinner CSS animado
    Given el usuario final ingresa un dato en un campo que dispara una llamada de Autocompletado (CA-14 / CA-30)
    When la interconexión con el sistema externo mediante apiClient.get está procesándose (isAsyncLoading es true)
    Then el botón global de finalización y envío del formulario se deshabilita reactivamente (:disabled="isAsyncLoading")
    And muestra un indicador de carga con spinner CSS animado ("animate-spin"), evitando envíos prematuros o datos rotos
    And este estado de carga expuesto se propaga reactivamente desde FormRenderer hasta TaskViewerModal.

  Scenario: Enmascaramiento de Inputs de Múltiple Tipo (Contraseñas / Sensibles) (CA-53)
    Given el Arquitecto requiere capturar información sensible (Ej: APIs Keys, Claves)
    Then dispone del tipo de Campo "Contraseña (Password)"
    And el texto digitado por el usuario final se oculta inmediatamente bajo asteriscos (****).

  Scenario: Limpieza Automática por Lógica Condicional (CA-54)
    Given un campo B es dependiente de que el campo A tenga el valor "X"
    When el usuario final había llenado el campo B, pero decide cambiar el campo A al valor "Y"
    Then el campo B desaparece visualmente (CA-9) Y suelta (limpia null/undefined) los datos almacenados
    And evitando que viajen datos "fantasma" al motor asociados a ramas muertas del formulario.

  Scenario: Grillas y Organización Multicolumna (Layouts) (CA-55)
    Given el Arquitecto está diseñando la distribución espacial del formulario
    Then puede arrastrar y soltar componentes "Lado a Lado" organizándolos en múltiples columnas (Ej: 2, 3 o 4 columnas)
    And este layout es renderizado mediante CSS Grid / Flexbox de Tailwind en el `.vue` final adaptándose al espacio del Workdesk.

  Scenario: Vista de Imprimible y de Solo-Lectura Plana (View-Mode) (CA-56)
    Given un usuario que tiene el rol de "Visualizador" (Solo aprobar, no rellenar datos) abre la tarea
    Then el sistema NO le muestra un formulario lleno de Inputs deshabilitados y grises
    And le renderiza un componente de "Vista de Lectura / Print-Friendly" donde los datos parecen un documento de texto limpio sin bordes de formulario interactivo.

  Scenario: Candado de Solo-Lectura Basado en Fórmulas (CA-57)
    Given el Arquitecto configura un campo B que depende del valor de un campo A
    Then puede usar el panel de propiedades para establecer una "Fórmula de Bloqueo" (Ej: `if A == 'Extranjero' then disable B`)
    And el motor Zod / Vue deshabilita visualmente (Solo-Lectura) el campo B en tiempo real cuando se cumple la condición.

  Scenario: Cronómetro de Productividad en Formulario (Timer Component) (CA-58)
    Given el Arquitecto necesita medir Tiempos y Movimientos de los empleados
    Then dispone de un componente "Timer / Cronómetro" que ofrece tres modos de configuración:
    And 1. Cronómetro Activo a Demanda (Con botones de Play/Pausa/Reset manuales).
    And 2. Cronómetro en Segundo Plano (Mide el tiempo exacto que la ventana del formulario estuvo en foco).
    And 3. Cronómetro Sincronizado por API (Conectado a un sistema externo de Time-Tracking).
    And el resultado viaja en los metadatos globales del Payload final.

  Scenario: Botón de Reset Dual-Verification (CA-59)
    Given el Arquitecto agrega un botón "Restablecer Formulario"
    When el usuario final lo oprime por error o a propósito
    Then el sistema debe exigir una "Doble Verificación" (Modal de confirmación: "¿Está seguro que desea borrar todos los datos ingresados?")
    And solo si se confirma, el estado reactivo del componente se limpia a cero.

  Scenario: Arrastrar y Soltar (Drag & Drop) Expandido para Adjuntos (CA-60)
    Given el formulario contiene un componente de Subir Archivos
    Then el usuario no está obligado a usar el botón táctil "Buscar Archivo"
    And puede arrastrar múltiples archivos simultáneamente desde su escritorio / SO y soltarlos sobre la zona definida en pantalla para iniciar la carga (Dropzone).

  Scenario: Captura de Geolocalización (GPS) Embebida (CA-61)
    Given el Arquitecto diseña un formulario para trabajadores en terreno
    Then puede arrastrar un componente "Captura GPS" (Obtener Ubicación)
    And cuando el usuario lo presiona, el navegador solicita permiso y captura las coordenadas (Latitud / Longitud) precisas integrándolas automáticamente al esquema.

  Scenario: Lector Nativo de Código de Barras / QR (CA-62)
    Given el proceso requiere leer etiquetas físicas o documentos
    Then el Arquitecto dispone de un componente "Escaner QR/Barcode"
    And este componente invoca la API moderna de navegadores (WebRTC/MediaDevices) para usar la cámara del dispositivo móvil/laptop
    And el valor escaneado rellena el campo objetivo automáticamente.

  Scenario: Auto-Validación de Regex Comunes (Email/URL) (CA-63)
    Given el Arquitecto configura un campo de texto y le asigna el tipo "Email" o "URL"
    Then el IDE aplica implícitamente la validación de Expresión Regular correspondiente (Ej: `z.string().email()`)
    And el sistema provee feedback visual inmediato de error si el usuario tipea algo como `carlos@gmail` sin dominio TLD.

  Scenario: Mensajes de Ayuda / Hint Texts Multi-Estado (CA-64)
    Given el Arquitecto configura un campo con requisitos complejos (Ej: Contraseña Segura)
    Then puede definir múltiples mensajes de estado (Hint Texts) debajo del componente
    And el color/icono de cada mensaje cambia dinámicamente ("❌ a ✅") conforme el usuario va cumpliendo cada criterio (Ej: Mayúscula, Número, Longitud) en tiempo real.

  Scenario: Rechazo de Modo Oscuro en V1 (CA-65 - Diferido a V2)
    # NOTA: Diferido a V2. Para V1, los formularios generados forzarán Light Mode independientemente del SO/Dispositivo.
    Given el Arquitecto despliega el formulario
    Then el formulario se renderiza siempre en paleta corporativa clara.

  Scenario: Conversor de Moneda Automático (CA-66 - Diferido a V2)
    # NOTA: Diferido a V2.
    Given el Arquitecto configura un campo monetario
    Then el formulario ofrecería conversión de tasa de cambio a COP en vivo al pie del componente.

  Scenario: Componente WYSIWYG de Texto Enriquecido (CA-67 - Diferido a V2)
    # NOTA: Diferido a V2. Para V1 solo existe Texto Plano Textarea.
    Given el Arquitecto requiere que el usuario entregue justificaciones extensas
    Then dispone de un componente de Texto Enriquecido (Word-like, con negritas, listas y cursivas).
	
	  # ==============================================================================
  # E. HERRAMIENTAS AVANZADAS, SIMULACIÓN Y QA AUTOMATIZADO
  # ==============================================================================
  Scenario: Generación Autónoma de Pruebas Unitarias QA (Auto-Vitest) (CA-68)
    Given un formulario visual completamente tipado y validado mediante la capa Zod
    When el Arquitecto de Diseño despliega el menú "Herramientas Avanzadas" y selecciona [Generar Suite de Pruebas]
    Then el Motor de Formulario (ZodBuilder) analizará el Árbol AST del esquema
    And auto-escribirá un archivo de código `.spec.ts` completo (Vitest/Jest) abarcando pruebas de Límites (Boundary Tests), validaciones de Nulos y coerción de Tipos
    And entregándole a los ingenieros de QA una cobertura base del 80% en cero segundos, acortando dramáticamente el tiempo de salida a producción (Time-to-Market).

  Scenario: Simulador Multi-Rol en Tiempo Real (iForm Maestro) (CA-69)
    Given el diseño de un formulario "Maestro" multi-etapa que atraviesa varias áreas operativas (Ej: Área Comercial -> Área Legal)
    When el diseñador finaliza el mapeo condicional y activa el `[Modo Simulador]` en el Header
    Then la interfaz inhabilitará la edición y desplegará un Dropdown de "Simular como Rol: [X]"
    And al seleccionar "Área Legal", el DOM silenciará u ocultará inmediatamente los campos configurados como `Read-Only` o `Hidden` para ese rol específico
    And permitiendo auditar lógicamente el control de acceso en caliente, sin necesidad de compilar o cambiar de usuario real en el sistema.

  # ==============================================================================
  # F. EXPOSICIÓN B2C (PÚBLICA) Y RESILIENCIA OPERATIVA
  # ==============================================================================
  Scenario: Modo Trámite Público Perimetral (Bypass JWT Seguro) (CA-70)
    Given la necesidad de someter un formulario a clientes externos sin credenciales EntraID (Ej: Formulario PQR / Denuncias)
    When el Arquitecto activa el parámetro 🌐 `[Permitir Enlace Público]`
    Then el sistema generará una URL Criptográfica transitoria
    And el Router Vue (US-051) marcará la ruta con `meta: { isPublic: true }`, eximiendo la intercepción de autenticación JWT.
    And OBLIGATORIAMENTE, el API Gateway montará políticas de *Rate Limiting* estricto y exigirá inyección de *reCAPTCHA v3* en el DOM para evitar que ataques de denegación de servicio (DDoS/Bots) llenen la base de datos de basura anónima.

  Scenario: Máquina del Tiempo JSON (Soft-Versioning Local) (CA-71)
    Given un usuario diagramando un formulario complejo que accidentalmente borra una pestaña o un Grid entero
    When navega a la sección de "Herramientas Avanzadas > Historial JSON"
    Then el sistema revelará un listado cronológico de "*Snapshots* Dinámicos" del esquema
    And permitirá inyectar y sobreescribir el AST visual del lienzo (`restore()`) devolviendo la interfaz exactamente al estado estructural de hace 15, 30 o 60 minutos, previniendo crisis por pérdida de trabajo.

  Scenario: Resiliencia Periférica Offline y Tolerancia a Conflictos (CA-72)
    Given un operador llenando un formulario crítico que sufre un micro-corte de Red (HTTP 5xx / Network Error)
    Then el sistema ejecutará un Fallback serializando el JSON hacia el `LocalStorage` del navegador de forma segura.
    When la red regrese y el Service Worker intente empujar ("Sync") el borrador guardado localmente hacia el Servidor
    Then el Frontend deberá OBLIGATORIAMENTE adjuntar el `VersionId` (Optimistic Hash) original del caso.
    And si un supervisor ya había modificado o cancelado el caso en el Servidor durante ese periodo Offline, el Backend detonará un `HTTP 409 Conflict`, previniendo que la data vieja sobreescriba corruptamente la verdad transaccional.

  # ==============================================================================
  # 3.1 mejoras	
  # ==============================================================================
  # A. EXPANSIÓN B2B: GENERACIÓN POR IA Y LIBRERÍA DE FRAGMENTOS
  # ==============================================================================
  Scenario: El Escáner Mágico (AI Prompt-to-Form & Document-to-Form) (CA-73)
    Given el lienzo en blanco del IDE de Formularios (Pantalla 7)
    When el Arquitecto sube un documento legacy (PDF/Imagen) o escribe un Prompt en lenguaje natural (Ej: "Genera formulario de crédito hipotecario")
    Then el Asistente IA Multimodal analizará el documento o texto.
    And autogenerará el layout visual en Vue 3 y el esquema Zod de manera instantánea, mapeando tipos de datos, labels y campos requeridos.
    And el Arquitecto retomará el control manual sobre el lienzo generado para refinar la UI, reduciendo el "Time-to-Market" de la digitalización.

  Scenario: [REMEDIACIÓN] Diccionario Global y Fragmentos Reutilizables (Snippets) (CA-74)
    Given la necesidad de estandarizar la recolección de datos en toda la empresa (Prevenir Torre de Babel)
    Then la plataforma TIENE PROHIBIDO leer variables de Camunda para autogenerar el formulario (El proceso no dicta el dato).
    And el IDE consultará al BFF REST API `/api/v1/design/dictionary` para desplegar un autocompletado en el input de camundaVariable sugiriendo variables corporativas.
    And al seleccionar una variable corporativa, el componente visual heredará automáticamente propiedades pre-aprobadas como `label`, `isPII` y `type`.
    And el Arquitecto podrá seleccionar campos y persistirlos como fragmentos de diseño en `/api/v1/design/snippets` mediante `POST`, actualizando dinámicamente la categoría "Mis Fragmentos" de la paleta.

  # ==============================================================================
  # B. GOBERNANZA DE DATOS (V.I.D.A.) Y SHIFT-LEFT SECURITY
  # ==============================================================================
  Scenario: El Peaje Analítico (Data Diet / Prevención de Campos Huérfanos) (CA-75)
    Given el panel de propiedades de cualquier componente visual en el IDE
    When el Arquitecto arrastra un nuevo campo
    Then el sistema le exigirá OBLIGATORIAMENTE declarar el "Destino Estratégico" del dato (Dropdown: `Regla DMN`, `Integración Externa`, `Documento PDF SGDEA`, `Analítica Pasiva`).
    And si el dato se marca como `Analítica Pasiva` (no aporta a la ruta crítica del proceso), el IDE DESHABILITARÁ y bloqueará físicamente el switch de "Obligatorio" (Zod required).
    And garantizando arquitectónicamente la "Dieta de Datos" e impidiendo generar fricción al usuario final por datos inútiles.

  Scenario: El Sello Radiactivo de Privacidad (Data Classification PII) (CA-76)
    Given el Arquitecto agregando campos confidenciales (Ej: Cédula, Diagnóstico Médico, Sueldo)
    Then dispondrá de un Master Switch de Ciberseguridad: `[🔒 Clasificar como PII / Sensible]`.
    And al activarlo, el IDE inyectará un metadato estructurado en el esquema Zod.
    And esta etiqueta instruirá imperativamente al Backend para que ofusque/encripte este dato en reposo (AES-256) y lo censure si es enviado al motor analítico (BAM) o a los Agentes LLM.
    And los campos tipo "Password" enmascararán el valor en la UI (`***`) nativamente.

  Scenario: [REMEDIACIÓN] Integración Autocompletado Gobernado y Escudo Anti-DDoS (CA-77)
    Given el Arquitecto diseña un campo configurado como "Gatillo" de autocompletado externo (Ej: Buscar RUT)
    Then el IDE TIENE ESTRICTAMENTE PROHIBIDO permitir la inyección de URLs o código JavaScript crudo (`fetch` / `axios`) en las propiedades del campo (Prevención SSRF).
    And obligará al usuario a seleccionar exclusivamente un "Conector Homologado" previamente registrado mediante un dropdown select en el panel de propiedades.
    And el validador estricto `validateSchemaSecurity` del JSON rechazará la persistencia o cambio de pestaña si se inyectan URLs crudas o comandos de ejecución JS.
    And el Frontend aplicará un `Debounce` obligatorio de 500ms al teclear mediante `useDebounceFn`, delegando la petición al proxy BFF para evitar fugas de datos desde el cliente.

  # ==============================================================================
  # C. ARQUITECTURA CORE: COMPILACIÓN BIDIRECCIONAL Y RENDERIZADO
  # ==============================================================================
  Scenario: Factoría Reactiva Zod On-The-Fly y Renderizado Bidireccional (CA-78)
    Given el entorno dividido: Canvas Visual (Izquierda) y Mónaco IDE (Derecha)
    When el Arquitecto arrastra un componente visual y marca restricciones (Ej: Requerido, Mínimo 5)
    Then el Mónaco IDE redactará en vivo el código Vue 3 y la regla matemática `z.string().min(5)`.
    And el enlace es bidireccional: si se borra la regla en el JS, el Canvas pierde la validación en milisegundos.
    And al renderizarse en el Workdesk operativo, el sistema NO descargará archivos `.js` estáticos.
    And instanciará el esquema dinámicamente usando una factoría `Zod` conectada a la memoria reactiva (`reactive()`) de Vue.

  Scenario: Sandboxing Estricto y Aislamiento Perimetral (Anti-XSS/RCE) (CA-79)
    Given que el Arquitecto inyecta lógica condicional (Cross-Field Logic: `if Monto > 1000`) o CSS exótico
    When el Formulario se renderiza operativamente en el Workdesk
    Then el Frontend encapsulará el componente utilizando `Shadow DOM`, impidiendo que el CSS distorsione el Layout corporativo (Style Bleed).
    And prohibirá estructuralmente la función `eval()` o `new Function()`. Toda expresión JS será parseada por un Abstract Syntax Tree (AST Sandbox) ciego a `window`, `document` o `fetch`.

  # ==============================================================================
  # D. ESTRUCTURAS COMPLEJAS, ESTADO Y RESILIENCIA
  # ==============================================================================
  Scenario: Reactividad Controlada en Formularios Densos (Lazy Validation) (CA-80)
    Given un usuario final diligenciando un "iForm Maestro" con alta densidad de inputs (+100 campos)
    When el usuario digita información a alta velocidad
    Then la validación proactiva de Zod TIENE PROHIBIDO ejecutarse en el evento síncrono por cada tecla presionada (`@input`).
    And el Frontend aplicará `Lazy Validation`, evaluando el esquema individualmente al perder el foco (`@blur`), protegiendo el Main Thread de Vue (Prevención DOM Thrashing).
    And las Máscaras Visuales (Ej: `$ 1.500,00`) mostrarán formato estético en UI, pero el formulario despojará la máscara en secreto y enviará el valor numérico crudo (`1500`) en el Submit.

  Scenario: Anclaje de Versión para Procesos In-Flight (Lazy Patching) (CA-81)
    Given que el Arquitecto publica la `V2` de un Formulario añadiendo campos obligatorios
    When un operario abre en el Workdesk un caso vivo (In-Flight) instanciado hace 2 meses bajo la `V1`
    Then el BFF (Backend for Frontend) inyectará ESTRICTAMENTE el JSON Schema de la versión `V1` originaria a la tarea en vuelo.
    And el sistema TIENE PROHIBIDO exigirle al usuario final campos de la V2 que no existían cuando él inició el trámite, evitando Crash 500 por desajuste de JSON.

  Scenario: Autoguardado Volátil, Limpieza de Fantasmas y Smart Buttons (CA-82)
    Given un usuario operando un formulario en el Workdesk
    Then cada interacción disparará un "Auto-Guardado" silente en LocalStorage atado al `Task_ID`.
    And si el campo B es visible solo cuando A es "Sí", y el usuario cambia A a "No", el campo B desaparece Y PURGA automáticamente su valor interno (Limpieza de Data Fantasma).
    And si el usuario sube PDFs al `<Dropzone>` (Upload-First) pero cierra la pestaña sin hacer Submit, el Frontend disparará un `Beacon` asíncrono ordenando al Backend destruir esos archivos huérfanos.
    And dispondrá de "Smart Buttons" nativos (`[Completar]`, `[⚠️ Escalar Error BPMN]`) envueltos en interceptores de red globales `try/catch`.

  Scenario: [REMEDIACIÓN] Sandbox de Pruebas Zod In-Browser (Shift-Left QA) (CA-83)
    Given el diseño finalizado del iForm Maestro
    Then el IDE proveerá una "Consola QA embebida" (Simulator) con opciones para autocompletar Happy, Sad y Fuzz payloads.
    And la opción Fuzz generará automáticamente Payloads extremos (Fuzzing) con tipos erróneos y strings fuera de límites, simulando Paths Felices y Tristes en la memoria RAM del navegador, certificando matemáticamente el contrato antes del despliegue.

  Scenario: Manejo Amigable de Errores de Sintaxis en el Mónaco IDE (CA-84)
    Given el Arquitecto está editando el código Vue o Zod manualmente en el panel de Mónaco IDE
    When introduce un error de sintaxis (Ej: falta una coma, llave de cierre, o tipado incorrecto)
    Then la plataforma TIENE PROHIBIDO colapsar con una pantalla blanca (Fatal Error) impidiendo seguir trabajando
    And el editor Mónaco interceptará el error de compilación en tiempo real (debounced)
    And subrayará de rojo (Squiggly Line) la línea conflictiva
    And proyectará en la zona inferior un panel amigable con mensajes legibles para un humano (Ej: "Hay un error de sintaxis cerca de la línea 14").

  Scenario: Auto-Guardado y Recuperación de Sesión en el Diseñador (CA-85)
    Given el Arquitecto está construyendo un formulario extenso en la Pantalla 7 (IDE Web)
    When ocurre una desconexión de red, apagón, o un cierre accidental de la pestaña
    Then el sistema debe garantizar la preservación del progreso inyectando el estado del lienzo en el `LocalStorage` del navegador de forma reactiva a cada cambio.
    And al regresar a la Pantalla 7, la aplicación detectará el borrador huérfano y mostrará un banner amigable: "Detectamos un borrador no guardado. ¿Desea restaurar su trabajo previo?" permitiendo recuperar el Canvas intacto.

  Scenario: Catálogo y Explorador de Formularios (Form Manager Dashboard) (CA-86)
    Given la necesidad del Arquitecto de buscar, re-editar o consultar versiones de formularios pre-existentes
    When el usuario ingresa al módulo de "Formularios" (Pantalla 7 Principal)
    Then EL SISTEMA NO CARGARÁ el IDE en blanco directamente, sino que presentará un "Catálogo o Grilla de Formularios"
    And esta Grilla incluirá un Buscador `Server-side` para buscar por Nombre de Negocio o ID Técnico.
    And cada fila o tarjeta mostrará: 
      - Nombre del Formulario (Ej: "Onboarding VIP")
      - Tipo: (Simple vs iForm Maestro)
      - Versión Activa (Ej: `v3`)
      - Fecha de Última Modificación y Autor
    And al hacer clic sobre un formulario, se abrirá en el Lienzo IDE. Si se desea ver el historial de diseño de ese formulario en particular, la grilla ofrecerá la opción de [Ver Historial de Versiones] para realizar Rollbacks.

  # ==============================================================================
  # G. REMEDIACIONES POST-AUDITORÍA (Sprint Remediation Brief 2026-04-05)
  # Origen: docs/requirements/us003_gap_remediation_brief.md
  # Tickets: REM-003-01 a REM-003-07
  # Propósito: Cerrar GAPs de implementación detectados por el workflow
  #            /analisisEntendimientoUs.md tras finalizar las 17 iteraciones
  #            de la Auditoría Integral del Backlog.
  # ==============================================================================

  Scenario: [REMEDIACIÓN] Persistencia Versionada del Diseño JSON del Formulario (CA-87)
    # Origen: REM-003-01 | Decisión PO: Opción A PostgreSQL JSONB
    Given que el Arquitecto finaliza el diseño de un formulario en el Canvas (Pantalla 7) y presiona [Guardar]
    When el IDE serializa el AST del esquema visual (JSON del Canvas + Esquema Zod + Metadatos)
    Then el Backend persistirá el diseño completo en la tabla relacional `ibpms_form_definitions` utilizando una columna JSONB de PostgreSQL para el cuerpo del esquema.
    And cada guardado generará una nueva fila inmutable con `version_id` autoincremental, `created_by`, `created_at` y un hash SHA-256 del contenido para detección de colisiones.
    And el Backend expondrá los endpoints REST: `GET /api/v1/forms/{formId}/versions` (listar versiones) y `POST /api/v1/forms/{formId}` (crear nueva versión).
    And TIENE PROHIBIDO utilizar Object Storage (S3/MinIO) como motor primario en V1; la columna JSONB de PostgreSQL es la fuente de verdad transaccional del diseño.

  Scenario: [REMEDIACIÓN] Separación Arquitectónica de Contextos IDE vs Workdesk (CA-88)
    # Origen: REM-003-02
    Given la coexistencia de lógica de diseño (IDE, Pantalla 7) y lógica de operación (Workdesk, Pantalla 2) dentro de la US-003
    Then el Frontend TIENE OBLIGACIÓN de mantener una separación física de módulos entre ambos contextos.
    And los composables/hooks de validación Zod operativa (Workdesk) residirán en un directorio distinto (`composables/workdesk/`) a los composables del IDE (`composables/ide/`).
    And los CAs de validación Lazy @blur (CA-22, CA-80) aplican EXCLUSIVAMENTE al contexto Workdesk.
    And los CAs de errores de Mónaco (CA-84) y Language Servers (CA-17) aplican EXCLUSIVAMENTE al contexto IDE.
    And ningún composable del IDE debe importar dependencias del Workdesk ni viceversa, para prevenir regresiones cruzadas.

  Scenario: [REMEDIACIÓN] Directriz de Complementariedad QA Sandbox vs Auto-Vitest (CA-89)
    # Origen: REM-003-03
    Given la coexistencia de dos herramientas QA: Sandbox In-Browser (CA-83) y Auto-Vitest (CA-68)
    Then la plataforma los tratará como herramientas COMPLEMENTARIAS con dominios distintos:
    And el Sandbox In-Browser (CA-83) es la herramienta de quick-check en tiempo de diseño, utilizada por el Arquitecto de Formularios en la Pantalla 7 para validar contratos Zod instantáneamente sin salir del IDE. No genera archivos persistentes.
    And el Auto-Vitest (CA-68) es la herramienta de regresión persistente, utilizada por el Ingeniero QA para generar archivos `.spec.ts` que se integran al pipeline CI/CD y aseguran cobertura de regresión a largo plazo.
    And TIENE PROHIBIDO considerar ambas herramientas como redundantes o eliminar una en favor de la otra.

  Scenario: [REMEDIACIÓN] Límites de Rendimiento y Lazy Mount para iForm Maestro (CA-90)
    # Origen: REM-003-04
    Given un Arquitecto diseñando un iForm Maestro de alta densidad en el Canvas
    When la cantidad de componentes visuales supere el umbral configurable `MAX_FORM_FIELDS` (Valor por defecto: 200 campos)
    Then el IDE emitirá una advertencia visual amigable (Banner amarillo, NO un bloqueo duro) indicando que el formulario supera el límite recomendado de campos y el rendimiento del navegador del operario podría degradarse.
    And para formularios que superen el umbral, el Motor de Renderizado del Workdesk activará OBLIGATORIAMENTE un patrón de Lazy Mount donde solo la pestaña o acordeón activo monta su DOM, preservando el Main Thread de Vue.
    And el equipo de QA deberá ejecutar un test de carga con un formulario de 250+ campos y 3 grillas anidadas, midiendo Time-to-Interactive (TTI) para certificar que no exceda 3 segundos en un navegador estándar.

  Scenario: [REMEDIACIÓN] Validación de Contrato de Integración con US-029 (CA-91)
    # Origen: REM-003-05
    Given la dependencia crítica de la US-003 con la US-029 (Persistencia CQRS) para Auto-Guardado, Smart Buttons e I/O Mapping
    Then el Arquitecto de Software TIENE OBLIGACIÓN de certificar la existencia de los siguientes contratos de la US-029 antes de considerar la US-003 como feature-complete:
    And Endpoint de Auto-Guardado: `POST /api/v1/drafts/{taskId}` (Persistir borrador parcial).
    And Endpoint de Recuperación: `GET /api/v1/drafts/{taskId}` (Reconstruir borrador al reabrir tarea).
    And Endpoint de Completado: `POST /api/v1/tasks/{taskId}/complete` (Smart Button Completar con I/O Mapping).
    And Endpoint de Limpieza: `DELETE /api/v1/drafts/{taskId}` (Purgar borrador post-submit).
    And si alguno de estos contratos no existe al momento de la integración, se generará un ticket bloqueante contra la US-029 antes de pasar a QA.

  Scenario: [REMEDIACIÓN] Política de Expiración y Limpieza de LocalStorage (CA-92)
    # Origen: REM-003-06
    Given la acumulación progresiva de datos en LocalStorage por los mecanismos de Auto-Guardado (CA-24, CA-85), Resiliencia Offline (CA-72) y Snapshots JSON (CA-71)
    Then el Frontend implementará un servicio `LocalStorageGarbageCollector` que se ejecutará automáticamente al iniciar la SPA.
    And aplicará una regla de expiración temporal: eliminará entradas con `timestamp` superior a 7 días naturales.
    And aplicará una regla de cuota espacial: si el volumen total de entradas con prefijo `ibpms_draft_` o `ibpms_snapshot_` supera 50MB estimados, purgará las más antiguas primero (FIFO).
    And registrará un log discreto en la consola del navegador: `[GC] Purged N stale drafts (X KB freed)`.
    And TIENE PROHIBIDO tocar claves de LocalStorage que no pertenezcan al dominio de formularios del iBPMS.

  Scenario: [REMEDIACIÓN] Componente Unificado de Vista Solo-Lectura (CA-93)
    # Origen: REM-003-07
    Given la coexistencia de dos modos de lectura: Visor Histórico para Auditoría (CA-37) y Vista Imprimible para Visualizadores (CA-56)
    Then el Frontend implementará un único componente base `FormReadOnlyView` con una prop `mode` que acepta dos valores:
    And `mode="audit"`: Renderiza el formulario con metadatos de auditoría visibles (quién modificó, cuándo, qué campo cambió) para consumo del Rol Auditor.
    And `mode="print"`: Renderiza el formulario como un documento de texto limpio sin bordes de input ni metadatos técnicos, optimizado para impresión y lectura plana.
    And ambos modos comparten el mismo motor de renderizado de campos (zero duplication), diferenciándose únicamente en la capa de presentación de metadatos.
    And si técnicamente la unificación genera complejidad excesiva, el Arquitecto Frontend puede mantener dos componentes separados SIEMPRE Y CUANDO compartan un composable base común para evitar duplicación de lógica de lectura.

```
**Trazabilidad UX:** Wireframes Pantalla 7 (IDE Web Pro-Code para Formularios).

---

### US-028: Simulador de Contratos Zod en Memoria (In-Browser QA Sandbox)
**Como** Ingeniero de Calidad (QA) / Arquitecto Frontend
**Quiero** un entorno de simulación (Sandbox) integrado directamente en el Diseñador Web (Pantalla 7) que inyecte Payloads extremos (Feliz y Triste) contra el esquema Zod en tiempo real
**Para** garantizar que las reglas matemáticas, de obligatoriedad y formato (Regex) funcionen perfectamente antes de asociar el formulario a Camunda, sin generar código muerto ni depender de pipelines de CI/CD externos.


> [!IMPORTANT]
> **Dependencias Externas Críticas de la US-028:**
> - **US-003 (iForm IDE / Pantalla 7):** 🔴 BLOQUEANTE. El Sandbox opera sobre esquemas Zod generados por el IDE de la US-003. Sin formularios con esquema Zod compilado, el Sandbox no tiene materia prima sobre la cual ejecutar `safeParse()`. Además, la US-003 CA-16 delega explícitamente la validación de coherencia BPMN↔Form al "ciclo de QA automatizado (US-028)", convirtiendo a la US-028 en dueña de esa responsabilidad (ver CA-17).
> - **US-005 (Despliegue BPMN / Pantalla 6):** ⚠️ DOWNSTREAM. El CA-11 genera un guardrail (`is_qa_certified`) que la US-005 DEBE consumir para bloquear despliegues de procesos BPMN que incluyan formularios sin sello QA. Si US-005 se implementa sin conocimiento de este guardrail, los formularios irán a producción sin certificación formal. El CA-17 también requiere acceso al diccionario de variables BPMN desplegadas por la US-005 para la validación de coherencia.
> - **US-029 (Ejecución de Formulario / Pantalla 2):** ℹ️ INFORMATIVA. La US-029 ejecuta en runtime los mismos esquemas Zod que la US-028 valida en design-time. La coherencia entre ambas validaciones (una en IDE, otra en ejecución) es el puente funcional que garantiza que un formulario certificado se comportará idénticamente en producción. No es dependencia directa de código, pero sí de contrato de datos.
> - **US-036 (RBAC / Pantalla 14):** ℹ️ INFORMATIVA. El rol "Ingeniero de QA" o "Arquitecto BPM" que ejecuta la certificación (CA-11) debe tener permisos explícitos en la matriz RBAC para invocar `POST /api/v1/design/forms/{id}/certify`. En V1 se asume que el Arquitecto BPM y el Super Admin tienen este permiso por defecto.



**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Integrated BDD Zod Testing Sandbox

  Scenario: Ejecución Interna In-Browser (Zero Dead Code) (CA-1)
    Given la estructura JSON del Formulario generada por el IDE web en Pantalla 7
    When el usuario oprime el botón `[🧪 SIMULAR CONTRATO ZOD]`
    Then el sistema NO descargará archivos `.spec.ts` físicos al disco duro local.
    And abrirá un "Panel de Consola QA" (Split View) integrado en el mismo IDE.
    And ejecutará las validaciones en tiempo real utilizando la memoria RAM del navegador contra el objeto Zod reactivo.

  Scenario: Boundary Testing Pragmático y Ciego (Type-Based Fuzzing) (CA-2)
    Given el esquema Zod compilado en memoria
    When el motor de simulación arranca
    Then el sistema autogenerará un Payload Dummy basado estrictamente en los Tipos Base y Restricciones matemáticas de Zod (Ej: Inyectar un string de 5 caracteres "AAAAA" si la regla es `.min(5)`), sin intentar adivinar la semántica del negocio.
    And presentará en la Consola dos evaluaciones automáticas:
      1. Path Feliz (100%): Inyecta el Payload válido generado y aserta visualmente `success: true`.
      2. Path Triste (Empty): Inyecta un Payload vacío `{}` y aserta que Zod devuelva los errores de `Required` correspondientes.

  Scenario: Modificación Manual del Mock Payload (Edición en Caliente) (CA-3)
    Given el Panel de Consola QA abierto y el Path Feliz generado
    Then el QA podrá editar libremente el código JSON del "Payload de Prueba" en un mini-editor de texto incrustado.
    And al teclear o borrar comillas, el motor de Zod reevaluará instantáneamente el Payload arrojando los errores de validación en tiempo real, permitiendo al humano probar Regex complejos (Ej: Cédulas o NITs) a mano.

  Scenario: Aislamiento Puro de Lógica de Negocio (Zero-Network Mocking) (CA-4)
    Given un formulario con campos que dependen de llamadas asíncronas a APIs externas (Data Sources)
    When se ejecuta el Simulador Zod
    Then el motor evaluará ÚNICAMENTE el método `zod.safeParse()` sobre el esquema estático.
    And omitirá cualquier intento de invocar el ciclo de vida de Vue (Ej: `onMounted`), garantizando que no se disparen peticiones de red (Axios/Fetch) ni se requieran librerías de Mocking complejas (MSW/vi.mock), asumiendo que la prueba valida el contrato de datos final y no la interfaz gráfica.

  Scenario: Fuzzing Ciego Recursivo (Topología de Datos Anidados) (CA-5)
    Given un formulario con reglas de colección (DataGrid o Grupo Repetible) como `z.array().min(2)`
    When el Sandbox arranca su Payload Generator
    Then iterará de forma superficial (Shallow Fuzzing) creando recursivamente una lista con exactamente 2 sub-objetos completos (Strings/Numbers basura tipificados)
    And el Arquitecto sólo tendrá que enmendar los valores irreales, pero no perderá tiempo redactando corchetes ni llaves (`[] {}`) para satisfacer la estructura base.

  Scenario: Dropdown Dinámico para Formularios Multi-Etapa (CA-6)
    Given un iForm Maestro que muta campos dependiendo de su fase (`Current_Stage`)
    When se abre la Consola del Sandbox
    Then la cabecera mostrará un Dropdown obligatorio `[ 🎭 Etapa a Simular: Radicación 🔻 ]`
    And al cambiarlo, la Variable en el Store altera en caliente el Zod Schema revelando u ocultando validaciones
    And el Fuzzer regenera el Payload JSON abarcando estrictamente la etapa seleccionada sin arrojar Falsos Positivos de validaciones aplanadas.

  Scenario: Visibilidad Dual Absoluta de Transformaciones (Split-Panel) (CA-7)
    Given una regla Zod que muta el dato del input (Ej: `.transform()` de String a Entero)
    When el Sandbox evalúa en tiempo real
    Then el IDE estará tajantemente seccionado en dos paneles:
    And Izquierda Editable: `[ 📥 Payload Crudo (Lo que digita el usuario) ]`
    And Derecha (Read-Only): `[ 📤 Payload Parseado (Lo que viaja a Camunda) ]`
    And el QA certifica de un pestañeo cómo el framework limpia espacios, parsea números o blanquea campos por omisión (Drop Keys).

  Scenario: Bloqueo Sincrónico Aceptable y Delegación de Regex Rotos (CA-8)
    Given una validación `.regex()` exigente o un `.superRefine()` de lógica temporal cruzada (Ej: `FechaInicio > FechaFin`)
    When el Sandbox inyecta el Dummy Data ("AAAAA" o Fechas cruzadas inconexas)
    Then la optimización prematura (Web Workers) está prohibida en V1
    And el Sandbox aceptará un bloqueo síncrono sub-milimétrico (`.safeParse` nativo)
    And pintará inescrupulosamente de ROJO el "Path Feliz" autogenerado informando de la ruptura Regex (Ej: `Placa Inválida`)
    And es responsabilidad primaria del Humano (QA) entrar al mini-editor y digitar voluntariamente un valor semántico válido ("ABC-123") para curar la barrera intencional creada por Zod.

  Scenario: Representación Visual Traducida (Human-Readable Errors) (CA-9)
    Given un Path Triste donde el Payload se va de cara contra 15 reglas Zod obligatorias conjuntas
    When el Sandbox invoca `.safeParse` y escupe el `ZodError` HTTP 400
    Then prohibido renderizar el array críptico Json en la UI del QA
    And el Frontend debe destripar la traza y repintarla como un Listado HTML con viñetas amigables:
    And "❌ [cliente.direccion.ciudad] - Este campo es obligatorio."
    And "❌ [monto_credito] - Debe ser mayor a 0."

  Scenario: Amnesia Prohibida del Payload Editor (Persistencia Local) (CA-10)
    Given la ventana de edición cruzada `[ 📥 Payload Crudo ]` donde el Arquitecto modificó 10 campos manualmente durante 5 minutos para simular un Path Feliz complejo
    When el usuario cierra la pantalla por accidente o refresca (`F5`)
    Then la directiva `@vueuse/core` invocará a `useLocalStorage('zod_mock_form_{ID}')`
    And el JSON artesanal renacerá exactamente como fue dejado, evitando destrucción por amnesia en sesiones continuas del QA.

  Scenario: Sello Criptográfico de Certificación BD (Cumplimiento ISO) (CA-11)
    Given el panel derecho (Parsed Payload) de un formulario en VERDE demostrando la validación estricta Zod en el Sandbox
    When se ilumina y se empuja el mega-botón: `[ 🏆 CERTIFICAR CONTRATO ZOD ]`
    Then se dispara un `POST /api/v1/design/forms/{id}/certify`
    And la Base de Datos plasma un sello: `ibpms_forms.is_qa_certified = true`
    And se registra inamoviblemente en el log histórico `ibpms_audit_log` el Test exacto: "El Tester Juan certificó la V2 del Formulario el DD/MM/AAAA. Payload utilizado: {JSON...}"
    And sirviendo esta bandera (is_qa_certified) de Guardrail para la Pantalla 6; que arrojará una Warning mandatoria de "Pre-Flight" bloqueando el botón `[🚀 DESPLEGAR Proceso]` si el BPMN intenta ir a Prod con un formulario sin sello QA.

  # ==============================================================================
  # D. REMEDIACIONES POST-ANÁLISIS FUNCIONAL (2026-04-06)
  # Origen: docs/requirements/us028_functional_analysis.md — Sección 5 (GAPs)
  # Propósito: Cerrar las 6 brechas detectadas durante el análisis de entendimiento
  #            funcional de la US-028 previo al cierre de desarrollo iterativo.
  # ==============================================================================

  Scenario: [REMEDIACIÓN] Revocación Automática del Sello QA por Mutación del Esquema (CA-12)
    # Origen: GAP-028-01 — ¿Quién revoca el sello de certificación?
    # Resuelve: El CA-11 permite certificar, pero no define qué pasa si el Arquitecto
    #           modifica el formulario después de la certificación.
    Given un formulario con sello `is_qa_certified = true` y un hash SHA-256 del esquema Zod almacenado como `certified_schema_hash` en la tabla `ibpms_forms`
    When el Arquitecto de Procesos modifica cualquier propiedad del esquema Zod del formulario (agregar campo, cambiar tipo, alterar regla de validación, eliminar campo) y guarda los cambios en la Pantalla 7
    Then el Backend OBLIGATORIAMENTE recalculará el SHA-256 del esquema Zod resultante y lo comparará contra el `certified_schema_hash` almacenado.
    And si los hashes difieren, el sistema revocará automáticamente el sello: `is_qa_certified = false`, `certified_schema_hash = null`.
    And registrará en `ibpms_audit_log` un asiento inmutable: `{ action: 'QA_CERT_REVOKED', reason: 'Schema modified post-certification', previousHash, newHash, modifiedBy, timestamp }`.
    And la Pantalla 7 mostrará un Badge visual de advertencia en la cabecera del formulario: `[⚠️ Certificación QA revocada — Modificación detectada]`.
    And el Guardrail de la Pantalla 6 (US-005) impedirá desplegar el proceso BPMN hasta que el QA re-certifique la nueva versión del esquema.

  Scenario: [REMEDIACIÓN] Versionado del Sello por Generación del Esquema (CA-13)
    # Origen: GAP-028-02 — Granularidad del sello por versión del formulario
    # Resuelve: El sello debe pertenecer a una versión específica del esquema, no al formulario genérico.
    Given la tabla `ibpms_forms` que almacena el estado de certificación
    Then el sello de certificación QA estará vinculado estrictamente a la combinación `{form_id, schema_version}` y NO al `form_id` aislado.
    And cada vez que el Arquitecto publique una nueva versión del formulario (incremento de `schema_version`), la nueva versión nacerá mandatoriamente con `is_qa_certified = false`.
    And el historial de certificaciones anteriores permanecerá inmutable en `ibpms_audit_log` para trazabilidad forense.
    And la Consola del Sandbox (CA-1) mostrará en su cabecera el indicador: `[📋 Esquema V{N} — {Certificado ✅ | Sin certificar ⚠️}]` para que el QA sepa exactamente qué versión está simulando.

  Scenario: [REMEDIACIÓN] Anotación Explícita de Limitación del Fuzzer en SuperRefine (CA-14)
    # Origen: GAP-028-03 — Validaciones cruzadas entre campos (.superRefine)
    # Resuelve: El fuzzer genera datos basura que violan TODAS las reglas superRefine,
    #           haciendo que el Path Feliz nunca sea feliz en formularios complejos.
    Given un esquema Zod que contiene reglas `.superRefine()` o `.refine()` con lógica de validación cruzada entre campos (Ej: `FechaInicio < FechaFin`, `MontoAprobado <= MontoSolicitado`)
    When el Fuzzer automático genera el Path Feliz (CA-2)
    Then el sistema detectará la presencia de refinamientos cruzados en el esquema Zod analizando el AST de la definición.
    And para cada `.superRefine()` o `.refine()` detectado, el Sandbox pintará un indicador visual junto al Path Feliz: `[🔧 {N} validaciones cruzadas detectadas — Requieren corrección manual del QA]`.
    And el Path Feliz autogenerado aceptará que los campos involucrados en refinamientos cruzados fallen la validación sin considerar esto un defecto del fuzzer, pintándolos en NARANJA (advertencia) en lugar de ROJO (error).
    And la diferenciación visual entre NARANJA ("el fuzzer no puede resolver esto, es responsabilidad manual del QA") y ROJO ("el tipo base es incorrecto") permitirá al QA priorizar su intervención manual de forma eficiente.

  Scenario: [REMEDIACIÓN] Truncamiento y Compresión del Payload en Audit Log (CA-15)
    # Origen: GAP-028-04 — Límites del Payload en auditoría
    # Resuelve: Un formulario Maestro complejo puede generar un JSON de 50KB+ en el audit log.
    Given la ejecución del POST `/api/v1/design/forms/{id}/certify` (CA-11)
    When el Backend registra el payload utilizado en `ibpms_audit_log`
    Then el sistema aplicará un límite estricto de 32KB para el campo `payload_snapshot` del registro de auditoría.
    And si el JSON del payload supera los 32KB, el Backend lo comprimirá usando GZIP y almacenará el resultado como `bytea` en PostgreSQL con un flag `is_compressed = true`.
    And si después de la compresión el payload aún supera los 64KB (caso extremo), el Backend truncará el JSON almacenando solo los primeros 32KB y añadirá el campo `truncated = true` con el motivo: `"Payload exceeds 64KB compressed limit"`.
    And el endpoint `GET /api/v1/design/forms/{id}/certifications` que consulte el historial detectará el flag y descomprimirá o indicará el truncamiento al consumidor.

  Scenario: [REMEDIACIÓN] Control de Concurrencia en Certificación Simultánea (CA-16)
    # Origen: GAP-028-05 — Concurrencia de certificación
    # Resuelve: Dos QAs intentando certificar el mismo formulario simultáneamente.
    Given dos tester (QA-A y QA-B) que abren el Sandbox del mismo formulario simultáneamente y ambos ven el Path Feliz en VERDE
    When ambos presionan `[🏆 CERTIFICAR CONTRATO ZOD]` en el mismo instante
    Then el Backend aplicará concurrencia optimista usando el campo `schema_version` como token de control.
    And el primer POST exitoso grabará el sello `is_qa_certified = true` con el `certified_by = QA-A` y `certified_at = timestamp`.
    And el segundo POST recibirá un `HTTP 409 Conflict` con el mensaje: `"Este esquema ya fue certificado por {QA-A} hace {N} segundos. Recargue para ver el estado actualizado."`.
    And cada intento (exitoso o rechazado) quedará registrado en `ibpms_audit_log` para trazabilidad.

  Scenario: [REMEDIACIÓN] Validación Cruzada de Coherencia BPMN↔Form en el Sandbox (CA-17)
    # Origen: GAP-028-06 — US-003 CA-16 delega coherencia BPMN↔Form a US-028
    # Resuelve: La US-003 delega la validación de coherencia entre las variables del BPMN
    #           y los campos del esquema Zod a la US-028, pero ningún CA la incluía.
    Given un formulario vinculado a un User Task de un proceso BPMN mediante `formKey` (definido en US-003/US-005)
    And el diccionario de variables de entrada/salida declaradas en la definición del proceso BPMN para ese User Task
    When el QA abre el Sandbox (CA-1) para un formulario que ya tiene un `formKey` asociado a un proceso desplegado
    Then el Sandbox mostrará un panel adicional colapsable: `[🔗 Coherencia BPMN ↔ Zod]`.
    And este panel listará las variables BPMN declaradas y las comparará contra los campos del esquema Zod:
    And   - `✅ Variable BPMN 'monto_aprobado' → Campo Zod 'monto_aprobado' (z.number())` — Match encontrado.
    And   - `⚠️ Variable BPMN 'fecha_limite' → No encontrada en esquema Zod` — El proceso espera un dato que el formulario no captura.
    And   - `ℹ️ Campo Zod 'comentarios_internos' → No declarado en BPMN` — El formulario captura un dato que el proceso no consume (puede ser intencional para auditoría).
    And esta validación es INFORMATIVA (no bloqueante): muestra las discrepancias pero NO impide la certificación (CA-11), ya que la coherencia puede ser intencional (campos de solo auditoría, campos computados).
    And si el formulario NO tiene `formKey` asociado (Formulario Simple sin proceso), este panel se ocultará automáticamente mostrando: `[🔗 Sin proceso BPMN vinculado — Validación de coherencia no aplica]`.


```
**Trazabilidad UX:** Wireframes Pantalla 7 (Panel QA).

---

### US-029: Ejecución y Envío de Formulario (iForm Maestro o Simple)
**Como** Analista / Usuario de Negocio
**Quiero** diligenciar la información de mi sección habilitada en la vista de la tarea (Pantalla 2) y presionar "Enviar"
**Para** finalizar exitosamente mi actividad y que el motor continúe al siguiente paso del proceso.

> [!IMPORTANT]
> **Dependencias Externas Críticas de la US-029:**
> - **US-003 (Catálogo de Formularios / Pantalla 7):** 🔴 BLOQUEANTE. Sin formularios diseñados (iForm Maestro o Simple), la US-029 no tiene NADA que ejecutar. La Pantalla 2 renderiza formularios creados en la US-003. Los esquemas Zod y el Layout de Vue que consume el BFF (CA-05/CA-10) se generan en la US-003.
> - **US-002 (Reclamar Tarea / Pantalla 1):** 🔴 BLOQUEANTE. Sin reclamo, la tarea no tiene `assignee` y los CAs de Implicit Locking (CA-07/CA-18) rechazarán todo intento de completar con HTTP 403. El operario DEBE haber reclamado la tarea ANTES de poder abrirla para edición.
> - **US-017 (CQRS & Event Sourcing):** ⚠️ HISTORIA GEMELA. Comparten el endpoint `/api/v1/workbox/tasks/{id}/complete`. La US-029 gobierna la experiencia del Frontend (Pantalla 2 UI + validación + archivos + borrador + UX). La US-017 gobierna la persistencia del Backend (CQRS + Event Sourcing + protección de Camunda + Rollback Saga). Los CAs duplicados entre ambas se reconcilian con la nota arquitectónica ADR de separación de responsabilidades.
> - **US-001 (Workdesk / Pantalla 1):** La navegación desde la grilla del Workdesk hacia el detalle de la tarea (Pantalla 2) depende de la infraestructura de rutas y el Store de Pinia de la US-001. El RYOW del CA-17 necesita que el Store de Pinia exista.
> - **US-036 (RBAC / Pantalla 14):** La validación de permisos per-campo del Zod Isomórfico (CA-15) consume la matriz de roles de la US-036 para determinar qué campos puede escribir cada rol.
> - **US-035 (SharePoint/SGDEA):** El Upload-First (CA-09) necesita que la bóveda documental temporal exista para almacenar los archivos pre-submit.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Task Completion with Form Data

  Scenario: Enviar datos válidos de formulario (CA-1)
    Given la tarea "TK-100" asignada a "carlos.ruiz" requiere el formulario "Form_Aprobacion_V1"
    And "Form_Aprobacion_V1" exige el campo obligatorio numérico "monto_aprobado"
    When "carlos.ruiz" realiza un POST a "/api/v1/workbox/tasks/TK-100/complete"
    And incluye en el body el JSON '{"variables": {"monto_aprobado": 1500, "comentarios": "Ok"}}'
    Then el sistema debe retornar un HTTP STATUS 200 OK
    And la tarea "TK-100" marca su estado como "COMPLETED"
    And las variables del JSON se persisten asociadas a la instancia del proceso.

  Scenario: Enviar datos inválidos (Violación del JSON Schema) (CA-2)
    Given la tarea "TK-100" requiere el campo obligatorio "monto_aprobado" numérico
    When "carlos.ruiz" realiza un POST a "/api/v1/workbox/tasks/TK-100/complete"
    And incluye un JSON vacío '{"variables": {}}'
    Then el sistema valida el payload contra el JSON Schema registrado para "Form_Aprobacion_V1"
    And el sistema debe retornar un HTTP STATUS 400 Bad Request
    And el error format JSON debe especificar `{"error": "ValidationFailed", "fields": [{"field": "monto_aprobado", "message": "Required"}]}`

  Scenario: Trazabilidad Volátil y Recolección de Basura (LocalStorage & GC) (CA-3)
    Given un formulario extenso ("Maestro_Onboarding") abierto en el Workdesk
    When el analista diligencia la mitad de los campos y accidentalmente cierra la pestaña
    Then el Frontend recuperará el progreso utilizando almacenamiento estricto en el navegador (`LocalStorage` mediante `@vueuse/core`) atado al Task_ID.
    And cuando el POST a `/complete` finaliza exitosamente (HTTP 200 OK), el Frontend ejecutará una purga síncrona destruyendo inmediatamente la llave de ese caso específico.
    And un proceso silencioso global al inicio de la sesión eliminará cualquier borrador huérfano en la PC del usuario que supere las 72 horas de antigüedad, previniendo cuellos de memoria.

  Scenario: Consistencia Transaccional Cruda (ACID Fallback over Sagas) (CA-4)
    Given un Payload de formulario (`/complete`) perfectamente validado en Zod que llega al Backend
    When el motor orquestador (Camunda 7) sufre un Crash o Timeout HTTP en su API REST interna
    Then el Backend iBPMS abortará inmediatamente la transacción (Rollback de Persistencia CQRS)
    And devolverá un error HTTP 500 Crudo ("Motor No Disponible") a la UI en Pantalla 2
    And se prohíbe a nivel arquitectónico generar falsos positivos HTTP 202 ("Guardado para después") para eludir el colapso del proceso judicial de fondo.

  Scenario: Inyección Megalítica de Contexto (Patrón BFF) (CA-5)
    Given la entrada física a la vista de la tarea (Pantalla 2)
    When el Frontend inicializa el componente Vue
    Then despachará exactamente UNA (1) única petición consolidada GET `/api/v1/workbox/tasks/{id}/form-context`
    And el Backend obrará como BFF *(Backend for Frontend)* inyectando en un solo Mega-DTO la triada: El JSON Schema de Zod, la configuración de Layout de Vue, y las Variables de Solo Lectura extraídas de Camunda (`prefillData`) para poblar inputs en un solo *tick* de renderizado.

  Scenario: Seguridad Asimétrica y Micro-Tokens Criptográficos (Zero-Trust) (CA-6)
    Given una validación asíncrona externa (Ej: Validar NIT) gatillada desde el Frontend (`OnBlur`)
    When el Backend consulta la API externa exitosamente y retorna al Frontend un "Micro-Token JWT" firmado criptográficamente de corta duración (Ej: TTL 15 min)
    Then al momento del Submit final (`/complete`), el Frontend adjuntará este Micro-Token.
    And el Backend (Zero-Trust) omitirá realizar una segunda llamada de red externa bloqueante, limitándose a verificar matemáticamente la validez de su propia firma en el Micro-Token para autorizar la transacción ACID en milisegundos.

  Scenario: Integridad de Asignación Concurrente (Implicit Locking) (CA-7)
    Given que una tarea "TK-400" está explícitamente asignada al analista `maria.perez` en el motor
    When el analista `pedro.gomez` intercepta vulnerablemente la URL o el JWT Payload e intenta someter un POST a `/tasks/TK-400/complete`
    Then el Core iBPMS examina deductivamente el `{delegatedUserId}` transaccional contra la identidad central del Security Context
    And aborta transaccionalmente la colisión inyectando un lapidario `HTTP 403 Forbidden` o `409 Conflict`, extirpando la necesidad pesada de emitir *ETags* a través del flujo asíncrono.

  Scenario: Hibridación de Datos Históricos vs Nuevos Contratos (Lazy Patching) (CA-8)
    Given el BFF inyectando `prefillData` de una Instancia antigua (V1) hacia un Formulario Zod nuevo (V2)
    When existan campos obligatorios nuevos en la V2 que no venían en la data histórica de Camunda (`null` o `undefined`)
    Then el esquema Zod reactivo los evaluará inmediatamente como inválidos
    And el Frontend pintará dichos inputs en ROJO y bloqueará físicamente el botón de [Enviar]
    And obligando procedimentalmente al analista a contactar al cliente y digitar el dato faltante en la UI para poder avanzar el proceso (Amnistía en Lectura, Guillotina en Escritura).

  Scenario: Desacoplamiento de Carga Binaria (Upload-First Pattern) (CA-9)
    Given un formulario Zod que incluye un componente `<InputFile>`
    When el usuario final adjunta un documento pesado (Ej: PDF de 10MB)
    Then el Frontend ejecutará una carga asíncrona temprana (Pre-Submit) hacia la Bóveda SGDEA (`/api/v1/documents/upload-temp`) obteniendo un Identificador Único (`UUID`).
    And al presionar [Enviar], el POST a `/complete` enviará EXCLUSIVAMENTE el JSON plano referenciando el ID (`{"cedula_pdf": "UUID-123"}`), teniendo PROHIBIDO arquitectónicamente enviar payloads Multipart o Base64 contra el motor de procesos Camunda.
	
  # ==============================================================================
  # US-029.1
  # ==============================================================================
  # A. INICIALIZACIÓN, BFF Y RECUPERACIÓN DE BORRADORES (UX & SRE)
  # ==============================================================================
  Scenario: Inyección Megalítica de Contexto (Patrón BFF) (CA-10)
    Given la entrada física a la vista de la tarea operativa (Pantalla 2)
    When el Frontend inicializa el componente Vue
    Then despachará UNA (1) única petición GET consolidada a `/api/v1/workbox/tasks/{id}/form-context`.
    And el Backend (BFF) inyectará el Mega-DTO: [Esquema Zod Vigoroso + Layout UI + Data Histórica (prefillData)].
    And este DTO incluirá la versión exacta del esquema (`schema_version`) para prevenir choques generacionales si el Arquitecto modifica el diseño mientras el caso está en vuelo.

  Scenario: Autoguardado Híbrido y Cifrado PII en LocalStorage (CA-11)
    Given la digitación continua de un analista en un formulario extenso
    Then el Frontend guardará el borrador (Draft) en el `LocalStorage` del navegador atado al `Task_ID`.
    But si el esquema Zod marca campos como `PII/Sensibles` (US-003), el Frontend DEBE aplicar cifrado simétrico (AES) usando una llave derivada de la sesión antes de escribir en LocalStorage.
    And disparará peticiones silenciosas de Merge Commit al Backend (Snapshot Volátil) solo bajo un Debounce de 10s de inactividad, usando una validación Zod "Parcial" (permitiendo nulos pero castigando tipos inválidos).

  # ==============================================================================
  # B. EJECUCIÓN, IDEMPOTENCIA Y ADUANA DE ARCHIVOS (APPSEC)
  # ==============================================================================
  Scenario: Idempotencia y Protección Anti-Doble Clic (El Dedo Tembloroso) (CA-12)
    Given el usuario pulsa [Enviar Formulario] múltiples veces por ansiedad o lag
    When el Payload JSON impacta el endpoint POST `/complete`
    Then el Frontend inyectará un Header `Idempotency-Key` (UUID único por montaje de componente).
    And el API Gateway/Backend procesará únicamente la primera transacción.
    And las peticiones subsecuentes idénticas retornarán un `HTTP 200 OK` silenciado desde la Caché, protegiendo a Camunda de excepciones `OptimisticLocking` o doble gasto en el Event Sourcing.

  Scenario: Desacoplamiento de Carga Binaria (Upload-First) y Escudo Anti-IDOR (CA-13)
    Given el patrón donde el cliente envía un UUID de un PDF en el POST final (`{"cedula": "UUID-123"}`)
    When el Backend recibe el Payload de cierre de formulario
    Then la arquitectura TIENE ESTRICTAMENTE PROHIBIDO enlazar ciegamente ese archivo a la tarea.
    And el Backend validará en la tabla de adjuntos temporales que `UUID-123` pertenezca al `user_id` logueado Y haya sido subido en el contexto de esa misma `task_id` (Defensa Anti-IDOR).
    And si detecta un UUID ajeno, abortará la transacción con `HTTP 403 Forbidden`.
    And un Cron Job nocturno destruirá físicamente de S3/SGDEA cualquier archivo temporal (TTL > 24h) sin confirmación transaccional para evitar facturas por basura infinita.

  Scenario: Seguridad Asimétrica y Prevención Replay en Micro-Tokens (CA-14)
    Given una validación asíncrona externa (Ej: Validar NIT) gatillada `OnBlur` que retorna un Micro-Token
    When el Frontend adjunta este token en el POST `/complete` final
    Then el Backend verificará matemáticamente su firma (Zero-Trust) para no repetir la llamada externa.
    And la arquitectura PROHÍBE el re-uso de tokens (Replay Attacks); el Token DEBE contener en Claims el `taskId` exacto y un `jti` que será invalidado en Redis un milisegundo después del Submit.

  # ==============================================================================
  # C. VALIDACIÓN ZERO-TRUST Y FIELD-LEVEL RBAC
  # ==============================================================================
  Scenario: Zod Isomórfico y Guillotina de Datos Fantasma (CA-15)
    Given la existencia de esquemas Zod bidireccionales
    When un atacante bypassea la UI enviando un POST adulterado vía API (Ej: Editando un campo de 'Solo Lectura')
    Then el Backend ejecutará OBLIGATORIAMENTE el mismo `schema.json` Zod utilizado en el diseño.
    And cruzará los permisos de escritura del Rol del usuario contra los campos recibidos; si inyectó datos no autorizados, aplicará un `.strip()` silencioso descartando el campo adulterado, o abortará con `HTTP 403 Forbidden`.
    And rechazará con `HTTP 400 Bad Request` cualquier asimetría de tipos de datos.

  # ==============================================================================
  # D. PERSISTENCIA CQRS Y PROTECCIÓN DE CAMUNDA ENGINE
  # ==============================================================================
  Scenario: Exclusión Topológica Estratégica de Camunda y ACID Fallback (CA-16)
    Given el cierre exitoso de la transacción CQRS (Guardado del Evento Inmutable)
    When el Backend notifica a Camunda 7 para avanzar el Token BPMN (`taskService.complete()`)
    Then el Backend TIENE ESTRICTAMENTE PROHIBIDO empujar el Payload masivo (Textos largos, JSONs) hacia la tabla `ACT_RU_VARIABLE` del Engine.
    And solo enviará un DTO minificado con las variables lógicas requeridas por los Gateways.
    And si Camunda sufre Timeout (HTTP 5xx), el Backend aplicará un Rollback estricto de la transacción CQRS (Saga) y devolverá HTTP 500 Crudo, previniendo falsos positivos de guardado en UI.

  Scenario: Consistencia Eventual UX y Read-Your-Own-Writes (RYOW) (CA-17)
    Given que el POST a `/complete` finaliza exitosamente (HTTP 200 OK)
    Then el Frontend ejecutará síncronamente una purga, destruyendo la llave del borrador en el `LocalStorage`.
    And eliminará proactivamente esa tarea específica del Store en RAM (Pinia) del Workdesk ANTES de redirigir al usuario al Home (RYOW).
    And esto garantizará que el usuario no vea su tarea "ya completada" flotando como un fantasma en su bandeja por culpa de la latencia CQRS.
    
  Scenario: Integridad de Asignación Concurrente (Implicit Locking) (CA-18)
    Given que una tarea "TK-400" está asignada explícitamente a `maria.perez`
    When `pedro.gomez` intercepta vulnerablemente la URL e intenta someter un POST a `/tasks/TK-400/complete`
    Then el Core iBPMS examina deductivamente el `assignee` de la tarea contra la identidad central del Security Context.
    And aborta transaccionalmente la colisión inyectando un lapidario `HTTP 403 Forbidden`.


  # ==============================================================================
  # E. REMEDIACIONES POST-AUDITORÍA (Sprint Remediation Brief 2026-04-05)
  # Origen: docs/requirements/us029_functional_analysis.md
  # Tickets: REM-029-01 a REM-029-06
  # Propósito: Cerrar GAPs detectados por el workflow /analisisEntendimientoUs.md
  #            antes del inicio de desarrollo de US-029.
  # Estado: US-029 NO ha sido desarrollada aún.
  # ==============================================================================

  Scenario: [REMEDIACIÓN] Reconciliación Arquitectónica US-029 / US-017 (CA-19)
    # Origen: REM-029-01 — GAP-1 del us029_functional_analysis.md
    # Resuelve: 13 CAs duplicados entre US-029 y US-017 generan riesgo de implementación divergente.
    Given la coexistencia de la US-029 (Frontend/UX) y la US-017 (Backend/CQRS) sobre el mismo endpoint `/complete` y la misma Pantalla 2
    Then se establece la siguiente POLÍTICA DE PROPIEDAD EXCLUSIVA para evitar duplicación:
    And **US-029 es la FUENTE AUTORITATIVA** para los siguientes aspectos: inicialización del formulario (BFF), autoguardado en LocalStorage, cifrado PII de borradores, feedback visual UX (spinner, confirmación, redirección), carga de archivos (Upload-First), idempotencia Anti-Doble Clic en Frontend, y validación Zod en el navegador.
    And **US-017 es la FUENTE AUTORITATIVA** para los siguientes aspectos: persistencia CQRS/Event Sourcing, proyección a tablas analíticas, protección topológica de Camunda (exclusión de variables masivas), Rollback Compensatorio (Saga inversa), validación Zod en el Backend (json-schema-validator), y Micro-Tokens criptográficos anti-replay.
    And cuando un CA de la US-029 mencione un comportamiento de Backend que esté definido en la US-017, la US-029 lo REFERENCIARÁ como dependencia (Ej: "consistente con US-017 CA-14") en lugar de redefinirlo.
    And cuando un CA de la US-017 mencione un comportamiento de Frontend que esté definido en la US-029, la US-017 lo REFERENCIARÁ como dependencia (Ej: "consistente con US-029 CA-20") en lugar de redefinirlo.
    And esta reconciliación garantiza que dos desarrolladores leyendo US diferentes NO produzcan implementaciones conflictivas del mismo endpoint.

  Scenario: [REMEDIACIÓN] Feedback Visual Durante el Proceso de Envío (CA-20)
    # Origen: REM-029-02 — GAP-2 del us029_functional_analysis.md
    # Resuelve: No se define qué ve el operario entre que presiona [Enviar] y recibe respuesta (2-5 segundos).
    Given la presión del botón [Enviar] en la Pantalla 2
    Then el Frontend ejecutará la siguiente secuencia visual para comunicar progreso:
    And 1. **Inmediatamente al hacer clic:** El botón [Enviar] se deshabilita, cambia su texto a "Enviando..." con un spinner integrado, y se aplica un overlay semitransparente sobre todo el formulario que bloquea cualquier interacción (previene edición accidental durante el proceso).
    And 2. **Durante la validación local (Zod Frontend):** El overlay muestra un indicador de texto: "Validando datos..."
    And 3. **Durante la llamada al Backend:** El texto cambia a: "Guardando en el servidor..."
    And 4. **Si ocurre un error (HTTP 400/500):** El overlay se retira inmediatamente, el botón se reactiva, y se muestran los errores específicos según CA-02 (validación) o CA-04 (motor caído). El formulario regresa al estado editable con TODOS los datos intactos.
    And el proceso completo NUNCA mostrará una pantalla en blanco ni dejará al operario sin información de lo que está pasando.

  Scenario: [REMEDIACIÓN] Confirmación Visual Post-Submit y Redirección Controlada (CA-21)
    # Origen: REM-029-03 — GAP-3 del us029_functional_analysis.md
    # Resuelve: No se define qué ve el operario después del envío exitoso ni si puede deshacer.
    Given la respuesta exitosa HTTP 200 del endpoint `/complete`
    Then el Frontend reemplazará el overlay de progreso con una pantalla de confirmación que muestra:
    And Un ícono de éxito animado (checkmark verde ✅) con el texto: "¡Tarea completada exitosamente!"
    And El identificador de la tarea completada (Ej: "TK-100 - Aprobación de Crédito").
    And Esta pantalla de confirmación se mostrará durante 3 segundos antes de redirigir automáticamente al Workdesk (Pantalla 1, Tab "Mi Bandeja" de US-002 CA-22).
    And El operario también puede hacer clic en "Ir al Workdesk" para redirigir inmediatamente sin esperar.
    And **NO existe funcionalidad de "deshacer" (Ctrl+Z) en V1.** Una tarea completada es irreversible. Si fue un error, el proceso BPMN tiene sus propios mecanismos de devolución/rechazo (US-017 CA-16 — rejectionLogs). Esta decisión es deliberada para proteger la integridad del Event Sourcing.
    And durante los 3 segundos de confirmación, el RYOW del CA-17 se ejecuta en paralelo (purga de LocalStorage + eliminación de Pinia).

  Scenario: [REMEDIACIÓN] Navegación de Formularios Multi-Etapa (Wizard Steps) (CA-22)
    # Origen: REM-029-04 — GAP-4 del us029_functional_analysis.md
    # Resuelve: Los formularios Maestro de múltiples pasos no tienen navegación definida.
    Given un iForm Maestro de la US-003 compuesto por N pasos/etapas (Wizard)
    Then la Pantalla 2 mostrará los siguientes elementos de navegación:
    And 1. **Barra de Progreso por Pasos:** En la parte superior del formulario, un indicador horizontal con los nombres de cada etapa (Ej: "① Datos del Cliente → ② Verificación → ③ Aprobación"). El paso activo se resalta en color primario. Los pasos completados muestran un checkmark verde. Los pasos con errores de validación muestran un indicador rojo.
    And 2. **Botones de Navegación:** En la parte inferior del formulario, botones [◀ Anterior] y [Siguiente ▶]. El botón [Siguiente] ejecuta la validación Zod del paso actual ANTES de permitir avanzar. Si hay errores, bloquea el avance y resalta los campos inválidos.
    And 3. **Botón [Enviar]:** Solo aparece visible en el ÚLTIMO paso. Reemplaza al botón [Siguiente]. No se puede enviar la tarea desde un paso intermedio.
    And 4. **Autoguardado per-Step:** El autoguardado del CA-11 guarda el borrador completo (todos los pasos) en cada Debounce, pero incluye un campo `currentStep: 3` en el JSON para que al reabrir el borrador, el formulario posicione al operario en el paso exacto donde dejó.
    And 5. **Navegación libre hacia atrás:** El operario puede retroceder a cualquier paso ya completado para revisar o modificar datos. La validación de pasos anteriores NO se re-ejecuta al retroceder, solo al avanzar o al enviar.

  Scenario: [REMEDIACIÓN] Gobernanza de Delegación para Completar Tareas (CA-23)
    # Origen: REM-029-05 — GAP-5 del us029_functional_analysis.md
    # Resuelve: No se define si un supervisor puede completar tareas de un subalterno.
    Given el toggle de delegación del US-001 CA-04/CA-15 que permite a un supervisor gestionar la bandeja de un subalterno
    Then se establece la siguiente política para la completación delegada:
    And 1. **El supervisor SÍ puede completar la tarea de un subalterno**, pero EXCLUSIVAMENTE si previamente ejecutó un Forced Unclaim (US-002 CA-08/CA-13) y luego un Claim a su propio nombre. Es decir: primero la quita del subalterno, luego se la auto-asigna, y entonces puede completarla. No existe un "completar en nombre de otro".
    And 2. **El CQRS Event Sourcing (US-017) registrará ambas acciones:** el evento `FORCE_UNCLAIMED` por el supervisor Y el evento `FORM_SUBMITTED` por el supervisor. La trazabilidad será completa.
    And 3. **El CA-07/CA-18 (Implicit Locking) NO se bypassea.** El supervisor debe ser el `assignee` actual para poder enviar. Esto elimina el riesgo de escalación de privilegios lateral.
    And 4. Esta decisión mantiene la integridad del principio "quien firma, es responsable" y evita ambigüedades legales en procesos regulados.

  Scenario: [REMEDIACIÓN] Contrato API del Merge Commit (Borrador en Servidor) (CA-24)
    # Origen: REM-029-06 — GAP-6 del us029_functional_analysis.md
    # Resuelve: El autoguardado silencioso al servidor no tiene endpoint ni reglas definidas.
    Given el Debounce de 10 segundos de inactividad del CA-11 que dispara un Merge Commit al Backend
    Then el Frontend enviará el borrador al siguiente endpoint:
    And `PUT /api/v1/workbox/tasks/{taskId}/draft` — Merge Commit de borrador parcial. Body: `{ currentStep?: number, partialData: {...}, schemaVersion: string }`.
    And **Validación:** El Backend ejecutará una validación Zod "Parcial" (todos los campos son opcionales EXCEPTO el tipo de dato: si el campo es numérico, el valor debe ser numérico o null). Si un campo tiene tipo incorrecto (Ej: texto en campo numérico), el Merge Commit descarta silenciosamente ESE campo pero guarda los demás. NO retorna error al Frontend.
    And **Response exitoso:** HTTP 204 No Content (silencioso, el operario no se entera).
    And **Response fallido:** HTTP 500 o timeout de red. El Frontend NO notifica al operario porque el borrador local (LocalStorage) ya tiene los datos protegidos. Un contador interno registra los fallos consecutivos: si acumula 3 fallos seguidos, muestra un Toast discreto: "El guardado automático en el servidor no está disponible. Tu borrador está seguro en tu navegador."
    And **Trazabilidad:** Los Merge Commits NO aparecen en el historial de trazabilidad del CA-09 de US-002. Son snapshots efímeros de trabajo en progreso, no eventos de negocio. Se almacenan en una tabla separada `task_drafts` con TTL de 72 horas (consistente con CA-03).
    And **Seguridad:** El endpoint aplica Implicit Locking — solo el `assignee` actual puede guardar borradores de su propia tarea. Intentos con otro userId retornan HTTP 403.


  # ==============================================================================
  # F. REFINAMIENTO FUNCIONAL POST-CUESTIONARIO (2026-04-05)
  # Origen: Cuestionario de 45 preguntas del workflow /refinamientoFuncionalUs.md
  # Propósito: Cerrar huecos descubiertos durante el refinamiento de la US-029.
  # ==============================================================================

  Scenario: [REFINAMIENTO] Scroll Automático y Foco en el Primer Campo con Error (CA-25)
    # Origen: Pregunta #2 del Refinamiento Funcional
    # Resuelve: En formularios largos (50+ campos), el operario no encuentra el campo con error.
    Given la respuesta HTTP 400 del CA-02 con un array de campos inválidos
    Then el Frontend ejecutará automáticamente un scroll suave hacia el PRIMER campo con error de validación.
    And le dará foco visual al campo (borde rojo pulsante + ícono de alerta) para que el operario vea EXACTAMENTE dónde está el problema.
    And si el formulario es multi-step (CA-22 Wizard), el Frontend primero navegará al paso que contiene el campo con error ANTES de hacer scroll.
    And el comportamiento aplica tanto para errores del Frontend (validación Zod local) como del Backend (HTTP 400), garantizando que el operario NUNCA tenga que buscar manualmente un error.

  Scenario: [REFINAMIENTO] Pre-Aviso de Caducidad de Borrador (CA-26)
    # Origen: Pregunta #3 del Refinamiento Funcional
    # Resuelve: El operario pierde su borrador tras 72h sin aviso (Ej: vacaciones).
    Given el proceso de limpieza de borradores huérfanos del CA-03 (TTL de 72 horas)
    Then el Frontend mostrará un Banner de pre-aviso cuando el borrador local tenga más de 48 horas de antigüedad: "⚠️ Tu borrador de esta tarea se eliminará automáticamente en [X] horas. Guarda o envía tu trabajo pronto."
    And el Banner aparecerá al abrir la tarea y permanecerá fijo en la parte superior del formulario (debajo de la Nota Interna del US-002 CA-16, si existe).
    And el operario podrá hacer clic en [Guardar ahora en el servidor] para forzar un Merge Commit (CA-24) que reiniciará el TTL de 72 horas en la tabla `task_drafts`.
    And si el borrador local ya expiró pero existe un Draft en el servidor (CA-24), el formulario recuperará el progreso del servidor como fallback, mostrando: "Recuperamos tu progreso guardado desde el servidor."
    And si AMBOS borradores expiraron (local y servidor), el formulario abrirá vacío con un Toast informativo discreto: "No se encontró ningún borrador guardado para esta tarea."

  Scenario: [REFINAMIENTO] Resiliencia ante Cambio de Versión de Esquema Mid-Flight (CA-27)
    # Origen: Pregunta #4 del Refinamiento Funcional
    # Resuelve: El operario trabaja 2 horas y al enviar, el servidor rechaza por versión obsoleta del formulario.
    Given un operario que abrió el formulario con `schema_version: V3` (CA-10) y trabajó durante un período prolongado
    When el Arquitecto despliega `schema_version: V4` mientras el operario está editando
    Then al presionar [Enviar], el Backend comparará la versión del esquema enviada (`V3`) con la versión actual (`V4`):
    And 1. **Si los cambios entre V3 y V4 son solo campos OPCIONALES nuevos:** El Backend ACEPTARÁ el envío con V3 y completará la tarea normalmente. Los campos opcionales nuevos se guardarán como `null`. El operario NO recibe ningún error.
    And 2. **Si los cambios incluyen campos OBLIGATORIOS nuevos:** El Backend retornará un HTTP 409 Conflict (NO un 400 genérico) con un mensaje legible: `{ "error": "SchemaVersionConflict", "message": "El formulario fue actualizado mientras trabajabas. Se requieren [N] campos nuevos." }`.
    And 3. **Ante un HTTP 409:** El Frontend mostrará un Modal informativo (NO destruirá el trabajo del operario): "El formulario fue actualizado con nuevos campos obligatorios. Tus datos están seguros. Al cerrar este aviso, el formulario se recargará con los campos nuevos y tus datos se mantendrán." Al aceptar, el Frontend recargará el Mega-DTO BFF con V4, aplicará los datos del operario como `prefillData`, y mostrará los campos nuevos en rojo (Lazy Patching CA-08).
    And 4. **EN NINGÚN CASO se perderán los datos que el operario ya digitó.** Los datos viajan en el LocalStorage/Draft y se reinyectan automáticamente tras la recarga del esquema.

  Scenario: [REFINAMIENTO] Aduana de Archivos: Tamaño Máximo, Tipos Permitidos y Defensa MIME (CA-28)
    # Origen: Pregunta #6 del Refinamiento Funcional
    # Resuelve: No existe límite de tamaño ni lista blanca de tipos de archivo.
    Given el Upload-First del CA-09 que envía archivos a `/api/v1/documents/upload-temp`
    Then el sistema aplicará las siguientes restricciones obligatorias:
    And 1. **Tamaño máximo por archivo:** 25 MB. Archivos que excedan este límite serán rechazados en el Frontend ANTES de iniciar la carga, con el mensaje: "El archivo supera el tamaño máximo permitido de 25 MB."
    And 2. **Tipo de archivos permitidos (Lista Blanca):** PDF (.pdf), Imágenes (.jpg, .jpeg, .png, .gif), Documentos Office (.docx, .xlsx, .pptx), Texto Plano (.txt, .csv). Cualquier otro tipo será rechazado con: "Tipo de archivo no permitido."
    And 3. **Validación MIME en el servidor:** El Backend verificará el tipo REAL del archivo (Magic Bytes / encabezado binario) independientemente de la extensión. Si alguien renombra un .exe a .pdf, el servidor lo detectará y rechazará con HTTP 415 Unsupported Media Type.
    And 4. **Cantidad máxima de archivos por formulario:** 10 archivos (total acumulado). Si el operario intenta adjuntar un undécimo, verá: "Has alcanzado el máximo de 10 archivos adjuntos por tarea."
    And 5. Estas restricciones podrán ser configurables por tenant en versiones futuras (V2).

  Scenario: [REFINAMIENTO] Feedback Visual Durante la Carga de Archivos (CA-29)
    # Origen: Pregunta #7 del Refinamiento Funcional
    # Resuelve: El operario no sabe qué pasa mientras sube un archivo de 10MB (15-30 segundos de espera).
    Given el patrón Upload-First del CA-09 durante la carga asíncrona de un archivo
    Then el componente de carga mostrará los siguientes elementos visuales:
    And 1. **Barra de progreso horizontal:** Con porcentaje numérico (Ej: "Subiendo... 45%") y color verde progresivo. La barra se actualizará en tiempo real con cada fragmento recibido por el servidor.
    And 2. **Nombre del archivo y tamaño:** Visible durante toda la carga (Ej: "📄 contrato_firmado.pdf — 8.2 MB").
    And 3. **Botón [✕ Cancelar carga]:** Permite abortar la carga en cualquier momento. Al cancelar, el archivo parcial en el servidor será marcado como `orphaned` y eliminado por el Cron Job del CA-13.
    And 4. **Al completar la carga:** La barra cambia a verde completo con checkmark (✅) y se muestra el archivo como un chip: "📄 contrato_firmado.pdf ✅ [🗑️ Eliminar]". El operario puede eliminar el archivo antes de enviar el formulario.
    And 5. **Si la carga falla:** Se muestra un mensaje rojo: "No se pudo subir el archivo. ¿Reintentar?" con botón [Reintentar].

  Scenario: [REFINAMIENTO] Detección de Sesión Duplicada en Múltiples Pestañas (CA-30)
    # Origen: Pregunta #12 del Refinamiento Funcional
    # Resuelve: Dos pestañas abiertas con la misma tarea sobrescriben sus borradores mutuamente.
    Given un operario que abre la misma tarea (Ej: TK-100) en dos pestañas del navegador simultáneamente
    Then el Frontend detectará la sesión duplicada utilizando un mecanismo de coordinación entre pestañas (BroadcastChannel API o SharedWorker).
    And la SEGUNDA pestaña mostrará un Banner de advertencia persistente: "⚠️ Esta tarea ya está abierta en otra pestaña. Los cambios que hagas aquí podrían perderse. Te recomendamos trabajar en una sola pestaña."
    And la segunda pestaña operará en modo de SOLO LECTURA: el operario podrá ver los datos pero los botones [Enviar], [Guardar borrador] y la edición de campos estarán deshabilitados.
    And al cerrar la primera pestaña, la segunda detectará la liberación y se reactivará como pestaña principal con un Toast: "Ahora eres la pestaña activa. Puedes continuar editando."

  Scenario: [REFINAMIENTO] Indicador de Estado de Sincronización del Borrador (CA-31)
    # Origen: Pregunta #13 del Refinamiento Funcional
    # Resuelve: El operario no sabe si su borrador está seguro en el servidor o solo en su PC.
    Given la edición activa de un formulario con autoguardado (CA-11 y CA-24)
    Then la barra superior de la Pantalla 2 mostrará un indicador de estado de sincronización con los siguientes estados posibles:
    And 1. **"☁️ Sincronizado"** (color verde discreto): El borrador existe tanto en el navegador como en el servidor. Si el operario cambia de PC, su trabajo estará disponible.
    And 2. **"💾 Solo en este navegador"** (color amarillo): El borrador existe solo en LocalStorage. El Merge Commit al servidor aún no se ha ejecutado (Ej: el Debounce de 10s no se ha disparado). Si el operario cambia de PC, no encontrará su progreso.
    And 3. **"⟳ Sincronizando..."** (animación giratoria): El Merge Commit (CA-24) se está enviando al servidor en este momento.
    And 4. **"⚠️ Sin conexión al servidor"** (color rojo): Los últimos 3 intentos de Merge Commit fallaron (consistente con CA-24). El borrador está seguro localmente pero no en el servidor.
    And al hacer clic en el indicador, se muestra un tooltip con la última hora de sincronización exitosa: "Última sincronización: hace 2 minutos."

  Scenario: [REFINAMIENTO] Diálogo Anti-Envío Accidental para Formularios Sin Obligatorios (CA-32)
    # Origen: Pregunta #15 del Refinamiento Funcional
    # Resuelve: Un formulario de solo confirmación (0 campos obligatorios) puede completarse con un solo clic accidental.
    Given un formulario donde TODOS los campos son opcionales o donde no existen campos de ingreso (Ej: formulario de confirmación con solo texto informativo)
    When el operario presiona el botón [Enviar]
    Then el Frontend mostrará un Modal de confirmación obligatorio ANTES de ejecutar el POST: "¿Estás seguro de que deseas completar esta tarea? Esta acción no se puede deshacer."
    And el Modal tendrá dos botones: [Cancelar] (cierra el modal, no envía nada) y [Sí, completar] (ejecuta el flujo normal del CA-01/CA-20).
    And este Modal solo aparece cuando el formulario NO tiene campos obligatorios. Los formularios CON campos obligatorios ya tienen la protección natural de la validación (el operario DEBE llenar algo para poder enviar), por lo que NO mostrarán este diálogo adicional.

  Scenario: [REFINAMIENTO] Distinción Visual de Campos de Solo Lectura (CA-33)
    # Origen: Pregunta #17 del Refinamiento Funcional
    # Resuelve: Los campos de solo lectura se confunden con los editables y el operario intenta escribir en ellos sin éxito.
    Given la renderización de campos con atributo `readOnly` provenientes del `prefillData` del BFF (CA-05/CA-10)
    Then los campos de solo lectura se renderizarán con los siguientes indicadores visuales obligatorios:
    And 1. **Fondo gris claro** (#F5F5F5) diferenciado del fondo blanco de campos editables.
    And 2. **Sin borde de edición** (borde sólido gris en vez del borde interactivo azul de los editables).
    And 3. **Ícono de candado (🔒)** posicionado a la izquierda del label del campo.
    And 4. **Cursor `not-allowed`** al pasar el mouse por encima, comunicando que no se puede interactuar.
    And 5. **Tooltip al hacer clic:** "Este campo es de solo lectura y contiene información de pasos anteriores del proceso."
    And estos estilos se aplicarán uniformemente a TODOS los tipos de campo (input, select, textarea, datepicker) para evitar inconsistencias visuales entre controles diferentes.

  Scenario: [REFINAMIENTO] Validación Zod Consciente de Campos Condicionales (CA-34)
    # Origen: Pregunta #18 del Refinamiento Funcional
    # Resuelve: La validación exige campos que el operario nunca vio porque la condición de visibilidad no se cumplió.
    Given un esquema Zod que define campos condicionales (Ej: "Si `decision === 'RECHAZADO'`, el campo `motivo_rechazo` es obligatorio")
    Then la validación del Frontend (CA-15 Zod Isomórfico) evaluará los campos obligatorios SOLO en función del estado actual de las condiciones del formulario.
    And si un campo condicional NO fue mostrado al operario (porque su condición de visibilidad no se cumplió), la validación lo IGNORARÁ completamente: no lo exigirá como obligatorio NI lo incluirá en el payload del POST `/complete`.
    And el Backend ejecutará la MISMA lógica condicional al validar: recibirá en el payload un campo `_visibleFields: ['campo_A', 'campo_B', ...]` que indica qué campos estuvieron activos. El Backend cruzará esta lista contra las reglas de condición del esquema Zod para determinar qué campos son obligatorios en ESE contexto.
    And si un atacante manipula `_visibleFields` para omitir un campo que SÍ debería ser obligatorio, el Backend recalculará las condiciones de forma independiente (usando los valores del payload) y detectará la inconsistencia, retornando HTTP 400.


```
**Trazabilidad UX:** Wireframes Pantalla 2 (Vista de Detalle / Formulario Dinámico).

---

### US-039: Formulario Genérico Base (Pantalla 7.B - El Camaleón Operativo)
**Como** PMO / Owner del iBPMS
**Quiero** disponer de un modelo de formulario genérico pre-asociado a tareas operativas simples
**Para** no invertir tiempo dibujando decenas de formularios básicos en la Pantalla 7 cuando la actividad es netamente procedimental (captura de evidencia, observaciones y tracking de avance).

> [!IMPORTANT]
> **Dependencias Externas Críticas de la US-039:**
> - **US-003 (Pantalla 7 / IDE):** El Pre-Flight Analyzer que decide si un formulario genérico es admisible reside en la lógica de despliegue compartida con el IDE de formularios.
> - **US-005 (Despliegue BPMN):** El Pre-Flight Analyzer se ejecuta durante el pipeline de despliegue del BPMN (Pantalla 6). La whitelist configurable (CA-5) es una propiedad del Process Definition.
> - **US-029 (Persistencia CQRS):** El auto-guardado de borradores (CA-7) consume los mismos endpoints de draft que los iForm Maestros.
> - **US-036 (RBAC / Pantalla 14):** La lista de Roles VIP que bloquean el uso del formulario genérico (CA-6) se administra desde la columna `is_vip_restricted` en `ibpms_roles`.
> - **US-034 (RabbitMQ):** Los Error Events disparados por los Botones de Pánico (CA-8) se enrutan a través del broker de mensajería para el procesamiento asíncrono.

> [!CAUTION]
> **HANDOFF TÉCNICO V1 (QA SRE CERTIFIED):**
> 1. Eliminación y prohibición del uso de variables de tipo `Toggle` binario (ej. `requiere_evidencia`) como lógica de UI en este documento, usando en su lugar un enfoque semántico estructural sin ambigüedades.
> 2. Prevención de colisiones de Namespace garantizada mediante inyección de `Whitelist Regex` en el BFF, evitando envenenamiento de los context variables del Engine.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Auto-vinculación Camaleónica y Resiliencia de Pantalla 7.B

  Scenario: Inyección Explícita (Anti-Bypass) y Restricción VIP (Pre-Flight) (CA-1)
    Given la necesidad de usar el Formulario Genérico Base (`sys_generic_form`) en un BPMN
    When el Arquitecto lo selecciona en el Dropdown de la `UserTask` en la Pantalla 6
    Then el Pre-Flight Analyzer auditará el Rol y la criticidad de esa tarea.
    And si el Rol está tipificado estructuralmente como "Alta Dirección", "Aprobador Financiero" o la tarea exige "Sello Legal", el Pre-Flight BLOQUEARÁ el despliegue (❌ Hard-Stop).
    And prohibirá usar el formulario genérico, forzando la creación de un iForm Maestro formal (Pantalla 7) que cumpla con los estándares pesados de auditoría.
    And para tareas Kanban huérfanas, el sistema inyectará la Pantalla 7.B silenciosamente.

  Scenario: Prevención de Context Bleeding (Filtro Anti-Basura BFF) (CA-2)
    Given un operario que apertura una tarea operativa con el Formulario Genérico
    When el BFF (Backend for Frontend) compila el DTO de inicialización (`prefillData`)
    Then el Backend aplicará un `Whitelist Regex` o filtro estricto sobre el Payload de Camunda.
    And extraerá y enviará EXCLUSIVAMENTE los metadatos de negocio vitales (Ej: `Case_ID`, `Client_Name`, `Priority`, `SLA`), ocultando las 200+ variables técnicas transaccionales del proceso.
    And el Frontend renderizará la Pantalla 7.B coronada por una cuadrícula superior de Solo Lectura ultraligera, evitando la sobrecarga cognitiva del operario.

  Scenario: Mutación Camaleónica de Interfaz y Botón de Pánico (Error Event) (CA-3)
    Given la renderización de la Pantalla 7.B
    When el operario deba escalar o devolver transversalmente el ticket (Ej: Evidencia Insuficiente)
    Then la interfaz exhibirá, además del recuadro principal, un bloque inferior de "Excepciones" o Botones de Pánico (Aprobado / Retorno al Generador / Cancelar).
    And al cliquear un botón de pánico, el Frontend forzará procesalmente la inyección de una observación justificativa mandatoria (Min: 20 caracteres) antes de consumar un Error Event o Escalamiento en el Motor de Camunda.


  # ==============================================================================
  # B. REMEDIACIONES POST-AUDITORÍA (Sprint Remediation Brief 2026-04-05)
  # Origen: docs/requirements/us039_functional_analysis.md
  # Tickets: REM-039-01 a REM-039-05
  # Propósito: Cerrar GAPs de implementación detectados por el workflow
  #            /analisisEntendimientoUs.md tras finalizar las 17 iteraciones
  #            de la Auditoría Integral del Backlog.
  # ==============================================================================

  Scenario: [REMEDIACIÓN] Definición del Cuerpo Editable del Formulario Genérico (CA-4)
    # Origen: REM-039-01 — GAP-1 del us039_functional_analysis.md
    Given la necesidad de que el operario capture evidencia, observaciones y tracking de avance en la Pantalla 7.B
    Then el cuerpo editable del Formulario Genérico Base contendrá OBLIGATORIAMENTE los siguientes campos pre-construidos:
    And 1. `textarea` "Observaciones / Notas del Operario" (obligatorio, min 10 chars, max 2000 chars) como campo principal de captura de texto libre.
    And 2. `dropzone` "Adjuntos de Evidencia" (opcional, max 5 archivos, max 10MB por archivo, tipos permitidos: PDF, JPG, PNG, DOCX, XLSX) para carga drag-and-drop de documentos de soporte.
    And 3. `select` "Resultado de la Gestión" (obligatorio, opciones configurables por proceso: Ej: "Aprobado", "Rechazado", "Pendiente de Información", "Escalado") como clasificador estandarizado del outcome de la tarea.
    And estos tres campos son el set mínimo fijo; TIENE PROHIBIDO agregar campos adicionales en runtime porque para formularios complejos se debe usar un iForm Maestro (US-003).
    And la estructura visual será: [Cuadrícula Metadatos Solo-Lectura] arriba, [Cuerpo Editable: Observaciones + Adjuntos + Resultado] al centro, [Botones de Pánico] abajo.

  Scenario: [REMEDIACIÓN] Configuración de Whitelist Regex por Proceso (CA-5)
    # Origen: REM-039-02 — GAP-2 del us039_functional_analysis.md
    Given la exigencia de filtrar variables técnicas de Camunda mediante Whitelist Regex (CA-2)
    Then la Whitelist será configurable POR PROCESO, no global, para soportar que cada BPMN tenga variables de negocio distintas (Ej: Proceso A usa `Case_ID`, Proceso B usa `Folio_Number`).
    And la configuración se realizará en la Pantalla 6 (Modeler BPMN) como una propiedad del Process Definition, en un panel "Variables Visibles en Formulario Genérico".
    And el Arquitecto del BPMN podrá definir una lista de hasta 10 claves de variables permitidas (Ej: `Case_ID, Client_Name, Priority, SLA, Due_Date`).
    And si NO se configura ninguna whitelist, el BFF aplicará un fallback seguro mostrando SOLO las 4 variables por defecto: `Case_ID`, `Instance_Name`, `Priority` y `Created_At`.
    And TIENE PROHIBIDO mostrar variables con prefijo `_internal_`, `camunda_`, o `zeebe_` independientemente de la whitelist configurada.

  Scenario: [REMEDIACIÓN] Catálogo Configurable de Roles VIP para Bloqueo Pre-Flight (CA-6)
    # Origen: REM-039-03 — GAP-3 del us039_functional_analysis.md
    Given la restricción de que tareas VIP no pueden usar el Formulario Genérico (CA-1)
    Then la lista de Roles VIP que disparan el Hard-Stop del Pre-Flight Analyzer será configurable desde la Pantalla 14 (RBAC) y NO hardcodeada en el código.
    And la tabla `ibpms_roles` incluirá una columna booleana `is_vip_restricted` (default: false) que el Super Admin activará para los roles que NO deben operar con formularios genéricos.
    And los tres roles mencionados en el CA-1 ("Alta Dirección", "Aprobador Financiero", "Sello Legal") serán marcados como `is_vip_restricted = true` durante el seed de datos inicial del sistema.
    And el Pre-Flight Analyzer consultará esta tabla en tiempo de despliegue del BPMN para evaluar si las UserTasks asignadas a esos carriles (Lanes) pueden usar `sys_generic_form`.

  Scenario: [REMEDIACIÓN] Persistencia y Auto-Guardado del Formulario Genérico (CA-7)
    # Origen: REM-039-04 — GAP-4 del us039_functional_analysis.md
    Given que el operario puede redactar observaciones extensas en el formulario genérico
    Then el Formulario Genérico consumirá los mismos endpoints de borrador definidos en la US-029 (Persistencia CQRS):
    And `POST /api/v1/drafts/{taskId}` para auto-guardado cada 30 segundos o al detectar inactividad de teclado (debounce 10s).
    And `GET /api/v1/drafts/{taskId}` para recuperar el borrador al reabrir la tarea.
    And `DELETE /api/v1/drafts/{taskId}` para limpiar el borrador tras submit exitoso.
    And si el operario cierra la pestaña accidentalmente, al reabrir la tarea encontrará un banner: "Se detectó un borrador no enviado. ¿Desea restaurarlo?" (mismo patrón del CA-85 de US-003).
    And los datos finales de submit (observaciones + adjuntos + resultado) se persistirán como variables del proceso en Camunda mediante `runtimeService.setVariables()`.

  Scenario: [REMEDIACIÓN] Mapeo Explícito de Botones de Pánico a Eventos BPMN (CA-8)
    # Origen: REM-039-05 — GAP-5 del us039_functional_analysis.md
    Given los tres Botones de Pánico del Formulario Genérico (CA-3)
    Then cada botón tendrá un comportamiento BPMN estrictamente definido:
    And Botón "Aprobado": Invoca `taskService.complete(taskId, variables)` inyectando `generic_form_result = "APPROVED"` como variable del proceso. El flujo continúa normalmente por el Sequence Flow default.
    And Botón "Retorno al Generador": Invoca `taskService.complete(taskId, variables)` inyectando `generic_form_result = "RETURNED"`. El BPMN DEBE tener un Exclusive Gateway posterior que evalúe esta variable para redirigir el token a la tarea anterior del flujo. Si el Gateway no existe, la tarea se completa sin retorno (fail-safe).
    And Botón "Cancelar": Invoca un BPMN Error Event con `errorCode = "TASK_CANCELLED_BY_OPERATOR"`. El BPMN DEBE tener un Error Boundary Event capturando este código. Si no existe el Boundary Event, Camunda propagará el error al proceso padre o a la morgue de incidentes (Incident).
    And los tres botones comparten la precondición del CA-3: observación justificativa de min 20 caracteres obligatoria ANTES de ejecutar cualquier acción.

```

**Trazabilidad UX:** Wireframes Pantalla 7.B (Formulario Genérico Base).
---


### US-005: Desplegar y Versionar un Modelo de Proceso (BPMN)
**Como** Arquitecto de Procesos
**Quiero** importar un archivo `.bpmn` (BPMN 2.0 XML) generado en el Diseñador Web y desplegarlo en el motor
**Para** que la plataforma sepa cómo enrutar las tareas secuenciales, paralelas y compuertas lógicas de mi proceso oficial.

> [!IMPORTANT]
> **Dependencias Externas Críticas de la US-005:**
> - **US-003 (Pantalla 7 / IDE Formularios):** Los FormKeys del Dropdown (CA-39) consumen el catálogo de formularios. La consistencia Simple/Maestro (CA-40) es dictada por US-003. El Pre-Flight valida integridad del mapping contra variables Zod del formulario (CA-68).
> - **US-007 (DMN / Pantalla 4):** El Dropdown de Business Rule Tasks (CA-61) consume tablas DMN creadas en US-007. El binding LATEST/DEPLOYMENT (CA-12) es co-responsabilidad.
> - **US-033 (Hub de Integraciones / Pantalla 11):** El Dropdown de conectores API (CA-45) consume los conectores registrados en US-033. La inmutabilidad de Swagger (CA-52) es co-responsabilidad. El catálogo de Topics (CA-70) se administra desde Pantalla 11.
> - **US-036 (RBAC / Pantalla 14):** Los roles Designer/Release Manager (CA-21) y los roles autogenerados desde Lanes (CA-6) se administran en Pantalla 14.
> - **US-029 (Persistencia CQRS):** El auto-guardado de borradores (CA-19) consume los endpoints de draft. Las variables de formulario persistidas validan contra el mapping del BPMN.
> - **US-034 (RabbitMQ):** El Retry Pattern (CA-58) de Service Tasks procesa vía colas. Los reintentos automáticos dependen de la taxonomía de prioridad (US-034 CA-6).
> - **US-000 (Resiliencia Integrada):** La morgue de tokens / Centro de Incidentes (CA-13) reside en la capa de resiliencia. Las instancias Sandbox (CA-67) son visibles en Pantalla 15.A.
> - **US-049 (Notificaciones):** Las notificaciones de aprobación/rechazo de despliegue (CA-69) se canalizan vía el sistema de notificaciones.
> - **US-039 (Formulario Genérico):** El Pre-Flight Analyzer que bloquea el uso de `sys_generic_form` en tareas VIP (CA-1 de US-039) reside en el pipeline de despliegue de US-005.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: BPMN Process Deployment
  Scenario: Despliegue exitoso de un diagrama BPMN válido (CA-1)
    Given que el usuario Arquitecto ha diseñado el flujo "Aprobacion_Credito_v2.bpmn"
    When el usuario sube el archivo invocando un POST multipart/form-data a "/api/v1/design/processes/deploy"
    Then el motor (Ej. Camunda) debe validar la sintaxis XML del archivo
    And el sistema debe retornar HTTP STATUS 201 Created
    And el sistema debe generar una nueva "Version" del "Process Definition ID" (Ej. Credito:2)
    And las nuevas instancias usarán esta versión sin afectar a las que ya estaban "En Vuelo" (In-Flight)

  Scenario: Intento de despliegue con diagrama inválido (BPMN Roto) (CA-2)
    Given un archivo "Proceso_Roto.bpmn" al que le falta un "End Event" necesario
    When el usuario realiza el POST a "/api/v1/design/processes/deploy"
    Then el motor debe denegar el despliegue
    And el sistema debe retornar HTTP STATUS 422 Unprocessable Entity
    And el payload debe contener el mensaje parseado: "El diagrama no es instanciable. Falta End Event."

  Scenario: Análisis Semántico en "Pre-Flight" de un diagrama complejo (Ejecutabilidad) (CA-3)
    Given el Arquitecto importa un diagrama BPMN 2.0 ("Proceso_Core.bpmn") que contiene Subprocesos, Start Events de Mensaje y Tareas de Servicio
    When el usuario solicita la validación previa al despliegue ("Pre-Flight Analyze")
    Then el motor semántico debe parsear los componentes avanzados
    And identificar si alguna `ServiceTask` carece de su propiedad `Delegate Expression` (Ejecución de código)
    And identificar si alguna `UserTask` carece de una vinculación de `Form Key`
    And identificar si alguna `ExclusiveGateway` carece de un flujo por defecto (`Default Flow`)
    And el sistema debe renderizar en Pantalla 6 la lista de Errores (❌) y Advertencias (⚠️) para que el Arquitecto los corrija antes del despliegue.

  Scenario: Validación Estricta de Start Event Form (Sincronización US-024) (CA-4)
    Given el Arquitecto solicita el despliegue de un nuevo modelo BPMN
    When el analizador Pre-Flight evalúa el nodo inicial (`StartEvent`)
    Then es mandatorio que el Start Event posea una vinculación estricta a un formulario (`Form Key` = `iForm Maestro` o `Simple`)
    And si carece de este formulario, el sistema rechaza el despliegue (HTTP 422) porque rompería la capacidad de instanciación manual forzada (Plan B).

  Scenario: Obligatoriedad de Nomenclatura de Instancia (ID Único) (CA-5)
    Given el Arquitecto configura las propiedades globales del BPMN antes de desplegar
    When intenta ejecutar el despliegue hacia el motor (Camunda)
    Then el sistema verifica que se haya definido la "Regla de Nomenclatura" (Paramétrica Ej: `PREFIJO-{Var}` o Secuencial Automática) para las futuras instancias
    And si esta regla no está definida en la metadata del proceso, el despliegue se bloquea advirtiendo: "Debe definir cómo se llamarán los casos de este proceso".

  Scenario: Autogeneración de Roles RBAC desde Carriles (Lanes) (CA-6)
    Given el Arquitecto importa un diagrama interactivo BPMN ("Flujo_Onboarding.bpmn")
    And el diagrama contiene un Carril (Lane) llamado "Aprobadores_Legales"
    And dentro de ese carril existe la Tarea "Firmar_Contrato" asociada al template "Form_Firma"
    When el usuario realiza el POST a "/api/v1/design/processes/deploy" con éxito
    Then el backend debe crear automáticamente el Rol de Sistema "BPMN_Flujo_Onboarding_Aprobadores_Legales"
    And el sistema debe asociar automáticamente a este Rol los permisos de escritura sobre "Form_Firma" y ejecución sobre la tarea "Firmar_Contrato"
    And el Rol autogenerado queda disponible en el Módulo de Seguridad (Pantalla 14) para asignarle usuarios.

  Scenario: Ley del Abuelo o Grandfathering Estricto por Defecto (CA-7)
    Given existen 15 instancias activas ("En Vuelo") ejecutándose con la Versión 1 de un proceso
    When el Arquitecto presiona `[🚀 DESPLEGAR V2]`
    Then el sistema asume 100% coexistencia pacífica por defecto
    And la V1 sigue viva en background procesando a las instancias antiguas hasta su conclusión
    And la migración forzada JAMÁS es el comportamiento predeterminado, requiriendo un acto explícito y manual.

  Scenario: Cirugía Quirúrgica de Instancias (No Guillotina) (CA-8)
    Given el Arquitecto requiere forzar la migración de instancias de V1 a V2
    When accede al panel `[Gestor de Instancias Activas]`
    Then el sistema TIENE PROHIBIDO ofrecer un botón de "Migrar Todos" de forma masiva ciega
    And debe desplegar una lista con checkboxes individuales permitiendo al Arquitecto seleccionar con pinzas cuáles instancias específicas someterá al salto de versión.

  Scenario: Bloqueo Topológico Duro Pre-Migración (CA-9)
    Given el Arquitecto intenta migrar la Instancia #45 (V1) hacia la V2
    And la Instancia #45 se encuentra actualmente pausada en el nodo `Tarea_Analisis`
    When el motor evalúa el Plan de Migración (Migration Plan)
    Then si el nodo `Tarea_Analisis` fue eliminado o no existe en la topología de la V2, el checkbox de selección se deshabilita
    And el sistema bloquea la migración arrojando: "Imposible migrar la Instancia #45. El nodo actual no existe en la Versión 2. Esta instancia debe terminar en V1 o ser anulada."

  Scenario: Prohibición Absoluta de Data-Patching Humano en TI (CA-10)
    Given la migración forzada hacia una V2 que exige un nuevo campo Zod "Obligatorio" (Ej: `Cédula`) que no existía en la V1
    Then el sistema TIENE ESTRICTAMENTE PROHIBIDO levantar un modal para que el usuario de TI (Systems Admin/Arquitecto) digite o invente ese dato faltante
    And garantizando así la Segregación de Funciones (SoD) y evitando la falsedad ideológica en la base de datos documental.

  Scenario: Amnistía Técnica y Cobro en Aduana (Lazy Validation) (CA-11)
    Given la migración del escenario anterior (CA-5.4) donde falta el dato obligatorio `Cédula`
    When el motor ejecuta el salto técnico a la V2
    Then inyecta silenciosamente un valor nulo (`null`) en la base de datos para no colapsar el hilo de ejecución (Amnistía Técnica)
    And cuando el operario de negocio abra esa instancia en su Workdesk (Pantalla 2), el Frontend renderizará el formulario Zod V2, detectará el `null` imperdonable, pintará el campo en ROJO y bloqueará físicamente el avance funcional hasta que el dueño del proceso pregunte y digite la `Cédula` real (Lazy Validation).

---refinamiento---
Scenario: Versionamiento Seguro de Reglas DMN (Protección de Derechos Adquiridos) (CA-12)
    Given un proceso V1 con tokens en vuelo que se aproxima a una Business Rule Task (DMN)
    When el Director de Riesgos publica una nueva versión de la tabla DMN (V2)
    Then el Arquitecto BPMN DEBE haber configurado previamente en el Modeler si la compuerta usa `Binding: LATEST` o `Binding: DEPLOYMENT`.
    And si elige `LATEST`, el motor evaluará con la nueva V2 publicada (Late Binding).
    And si elige `DEPLOYMENT` (Por defecto), el motor evaluará EXCLUSIVAMENTE contra la versión exacta de la DMN que estaba activa en el milisegundo en que nació el caso (Deployment Binding).
    And garantizando así la protección jurídica y previniendo rechazos ilegales a clientes por cambiar las "reglas del juego" a mitad del trámite.

  Scenario: Tablero de Resiliencia y Morgue de Tokens (CA-13)
    Given un error técnico no controlado durante una migración asíncrona (Ej: Caída de red o base de datos)
    Then el operario de negocio JAMÁS verá un stacktrace o error técnico en su Workdesk
    And el token roto pasará a estado `INCIDENT` y será canalizado exclusivamente a la Pantalla 15.A (SysAdmin) en la pestaña `[🚨 Centro de Incidentes]`
    And otorgando a Soporte Nivel 3 los botones tácticos: `[🔄 Retry (Electrochoque)]` o `[💀 Abortar Caso]`.

  Scenario: Cicatriz Forense de Auditoría Inmutable (CA-14)
    Given la culminación o visualización de una instancia que sufrió una migración forzada estructural
    When un Auditor o Usuario consulta la Vista 360 del Caso (Pantalla 17) o el historial del Workdesk
    Then el sistema inyecta obligatoriamente una franja visual inamovible: `[⚠️ MIGRACIÓN ESTRUCTURAL: Este caso inició bajo la Versión X y fue promovido forzosamente a la Versión Y el DD/MM/YYYY por el Administrador Z]`
    And blindando legalmente a la compañía ante demandas por vacíos procedimentales.

  Scenario: Rollback a Versión Anterior con Historial (CA-15)
    Given el Arquitecto detecta que la versión 3 de un proceso tiene un error lógico post-despliegue
    When navega al panel de "Historial de Versiones" en la Pantalla 6
    Then el sistema debe listar todas las versiones desplegadas previamente (v1, v2, v3) con fecha y autor
    And el Arquitecto puede seleccionar "Restaurar v2" con un solo clic
    And el sistema re-despliega la v2 como la nueva versión activa (v4 internamente = copia de v2)
    And las instancias en vuelo de v3 siguen corriendo hasta terminar naturalmente (salvo Migración Forzada explícita).

  Scenario: Bloqueo Pesimista de Edición Concurrente (CA-16)
    Given el Arquitecto "maria.lopez" abre el proceso "Solicitud_Credito" en el Diseñador (Pantalla 6)
    And el sistema le otorga un "Lock" exclusivo sobre ese proceso
    When el Arquitecto "carlos.gerente" intenta abrir el mismo proceso simultáneamente
    Then el sistema debe mostrar un mensaje: "🔒 Este proceso está siendo editado por maria.lopez desde las 10:15 AM"
    And debe bloquear los controles de edición del lienzo, dejando solo el modo "Solo Lectura" para el segundo usuario.

  Scenario: Copiloto IA Bajo Demanda (CA-17)
    Given el Arquitecto está diseñando un diagrama BPMN en el lienzo
    Then el Copiloto IA NO ejecuta análisis automático en tiempo real
    When el Arquitecto hace clic explícitamente en el botón [🧠 Consultar Copiloto IA]
    Then el sistema envía el XML del diagrama actual al endpoint de IA
    And renderiza las sugerencias y alertas ISO 9001 en el Panel de Feedback inferior.

  Scenario: Pre-Flight Extendido con Validaciones Avanzadas (CA-18)
    Given el Arquitecto solicita un "Pre-Flight Analyze" sobre un diagrama complejo
    Then el sistema debe validar, además de las reglas base (ServiceTask, UserTask, Gateway):
    And identificar si algún `TimerEvent` carece de la expresión de duración configurada (Ej. `R/PT1H`)
    And identificar si algún `MessageEvent` (Intermedio o de Inicio) carece de la correlación o nombre del mensaje (`MessageRef`)
    And identificar si algún `CallActivity` apunta a un `ProcessDefinitionKey` que NO existe desplegado en el motor
    And clasificar los hallazgos como Error (❌ bloquea despliegue) o Advertencia (⚠️ informativa).

  Scenario: Auto-Guardado del Diagrama en Borrador (CA-19)
    Given el Arquitecto está editando un diagrama BPMN en la Pantalla 6
    Then el sistema debe guardar automáticamente un borrador del XML cada 30 segundos (Best Practice Auto-Save)
    And si el usuario cierra el navegador sin desplegar, al volver a abrir el proceso encontrará el último borrador recuperado
    And el sistema debe mostrar un indicador discreto "✅ Guardado" en la barra de estado tras cada auto-guardado exitoso.

  Scenario: Simulación en Sandbox Antes de Desplegar (CA-20)
    Given el Arquitecto tiene un diagrama BPMN listo pero no ha sido desplegado aún
    When presiona el botón [🧪 Probar en Sandbox]
    Then el sistema debe generar una instancia temporal (no persiste en producción) del proceso
    And avanzar visualmente paso a paso mostrando por qué nodo (tarea/compuerta/evento) fluiría un caso de prueba ficticio
    And al finalizar la simulación, destruir la instancia temporal sin dejar rastro en la base de datos de producción.

  Scenario: Separación de Roles RBAC Diseñador vs Release Manager (CA-21)
    Given un usuario con rol "BPMN_Designer" abre un proceso en la Pantalla 6
    Then puede dibujar, importar, exportar y consultar al Copiloto IA
    But el botón [🚀 DESPLEGAR] debe estar deshabilitado (gris) para este rol
    When un usuario con rol "BPMN_Release_Manager" abre el mismo proceso
    Then puede ver el diagrama y presionar [🚀 DESPLEGAR] para enviarlo al motor
    And ambos roles son asignables desde el Módulo de Seguridad (Pantalla 14) y un usuario puede tener ambos simultáneamente.
    And estos roles son GLOBALES (aplican a todos los procesos sin granularidad por módulo). La granularidad por proceso se difiere a V2.

  Scenario: Paleta BPMN 2.0 Estándar Completa con UX Priorizada (CA-22)
    Given el Arquitecto abre el Diseñador en la Pantalla 6
    Then la Paleta BPMN 2.0 debe contener TODOS los elementos del estándar (incluyendo Conditional, Link, Cancel Events, Complex Gateway, Ad-Hoc y Event Sub-Process)
    But los elementos más usados (Start/End, User Task, Service Task, Exclusive/Parallel Gateway) deben aparecer como iconos principales visibles
    And los elementos avanzados/exóticos deben estar agrupados bajo submenús colapsables ("Más Eventos...", "Más Compuertas...")
    And esto evita saturar visualmente un principiante pero no limita a un experto.

  Scenario: Catálogo / Biblioteca de Procesos Desplegados (CA-23)
    Given el Arquitecto accede a la Pantalla 6
    Then debe existir un Panel lateral o pestaña "Explorador de Procesos" que liste todos los procesos diseñados
    And cada entrada muestra: Nombre, Versión Activa, Fecha de Último Despliegue y Autor
    And al hacer clic en un proceso, se carga en el Lienzo para su edición o consulta.

  Scenario: Text Annotations (Notas Adhesivas BPMN) en el Lienzo (CA-24)
    Given el Arquitecto está diseñando un diagrama
    Then debe poder arrastrar un componente "Text Annotation" desde la Paleta al Lienzo
    And escribir comentarios explicativos que se renderizan visualmente sobre el diagrama
    And estas anotaciones se persisten en el archivo .bpmn XML como parte del estándar.

  Scenario: Zoom, Minimap y Navegación Visual (CA-25)
    Given el Arquitecto trabaja con un diagrama con más de 3 carriles y 20+ nodos
    Then el Lienzo debe soportar controles de Zoom (+/-) y "Ajustar a Pantalla"
    And un Mini-Mapa (panorámico) en la esquina inferior derecha para navegar rápidamente entre secciones lejanas del diagrama.

  Scenario: Naming Dual - Nombre de Negocio y Nombre Técnico (CA-26)
    Given el Arquitecto crea una User Task y escribe "Llenar Formulario de Crédito" como nombre visible
    Then el panel de Propiedades debe ofrecer un segundo campo: "ID Técnico (Technical Name)"
    And si el Arquitecto no lo rellena, el sistema debe auto-generar un slug (Ej: `llenar_formulario_de_credito`)
    And el motor Camunda usará el ID Técnico internamente, mientras que la UI del Workdesk mostrará el Nombre de Negocio.

  Scenario: Plantillas BPMN Prediseñadas (CA-27)
    Given el Arquitecto presiona "Nuevo Proceso" en la Pantalla 6
    Then un Modal debe ofrecer la opción "Empezar desde Cero" o "Usar Plantilla"
    And las plantillas disponibles incluyen ejemplos comunes (Ej: "Aprobación Simple", "Onboarding Cliente", "Incidencia IT")
    And al seleccionar una plantilla, se carga en el Lienzo como punto de partida editable.

  Scenario: Diff Visual entre Versiones (CA-28 - Diferido a V2)
    # NOTA: Este escenario queda documentado pero su implementación se difiere a la Versión 2 del producto.
    Given el Arquitecto navega al Historial de Versiones y selecciona v2 y v3 para comparar
    Then el sistema muestra un Diff visual resaltando nodos agregados (verde), eliminados (rojo) y modificados (amarillo).

  Scenario: Copiar y Pegar Fragmentos entre Procesos (CA-29)
    Given el Arquitecto tiene abiertos dos procesos en pestañas distintas de la Pantalla 6
    When selecciona un fragmento (Ej: un Sub-Proceso con 5 tareas) del Proceso A y ejecuta "Copiar"
    Then debe poder "Pegar" ese fragmento en el Lienzo del Proceso B
    And el sistema debe re-mapear los IDs internos para evitar colisiones XML.

  Scenario: Límite de Complejidad Parametrizable y Advertencia de Mala Práctica (CA-30)
    Given el sistema tiene configurado un umbral de complejidad máxima (Ej: 100 nodos por defecto, parametrizable)
    When el Arquitecto excede ese umbral dibujando el nodo número 101
    Then el sistema debe mostrar una advertencia visual: "⚠️ Mala Práctica de Diseño: Este proceso supera los 100 nodos"
    And debe detallar los riesgos: "Procesos complejos son difíciles de mantener, propensos a errores y degradan el rendimiento del motor"
    And la advertencia NO bloquea el despliegue, solo informa. El umbral es configurable por un Admin.

  Scenario: Etiquetas de Estado en el Catálogo de Procesos (CA-31)
    Given el Catálogo de Procesos desplegados (CA-14) lista todos los procesos
    Then cada proceso debe tener una etiqueta visual de estado:
    And "📝 BORRADOR" si nunca ha sido desplegado al motor (solo existe como XML guardado)
    And "✅ ACTIVO (v3)" si tiene al menos una versión desplegada y operativa
    And "📦 ARCHIVADO" si fue retirado de operación (CA-23).

  Scenario: Archivar un Proceso sin Instancias Activas (CA-32)
    Given el Arquitecto selecciona un proceso "Proceso_Obsoleto" en el Catálogo
    And NO existen instancias "En Vuelo" de ese proceso
    When presiona el botón [📦 Archivar]
    Then el sistema cambia el estado del ProcessDefinition a "ARCHIVADO"
    And no se podrán crear nuevas instancias de ese proceso
    And el proceso deja de estar visible para los usuarios operativos, pero permanece en BD para auditoría
    But si existen instancias activas, el botón Archivar está deshabilitado con el tooltip: "No se puede archivar: X instancias en ejecución".

  Scenario: Invalidación Automática del Pre-Flight tras Edición (CA-33)
    Given el Arquitecto ejecutó el Pre-Flight Analyzer y obtuvo resultado "✅ Sin Errores"
    When posteriormente modifica el diagrama (agrega/elimina/cambia un nodo)
    Then el estado del Pre-Flight debe resetearse automáticamente a "⚠️ Pendiente de re-validación"
    And el botón [🚀 DESPLEGAR] debe requerir una nueva ejecución del Pre-Flight antes de habilitarse.

  Scenario: Solicitar Despliegue al Release Manager (CA-34)
    Given el Designer ha terminado de diseñar y el Pre-Flight está aprobado
    When presiona el botón [📩 Solicitar Despliegue]
    Then el sistema cambia el estado del proceso a "PENDIENTE_APROBACIÓN_DESPLIEGUE"
    And crea automáticamente una tarea en el Workdesk del usuario con rol "BPMN_Release_Manager"
    And el Release Manager ve esta tarea en su bandeja con el botón [🚀 Aprobar y Desplegar] o [❌ Rechazar].

  Scenario: SLA Configurable por Tarea Individual o Global (CA-35)
    Given el Arquitecto configura un UserTask en el Panel de Propiedades de la Pantalla 6
    Then el campo "SLA" puede tener un valor específico por tarea (Ej: "4 horas" para "Analizar", "48 horas" para "Firmar")
    And adicionalmente debe existir un SLA Global a nivel de ProcessDefinition (Ej: "5 días hábiles para el proceso completo")
    And las reglas de negocio o el Diseñador definen cuál prevalece en caso de conflicto.

  Scenario: Link Directo a Sub-Proceso desde Call Activity (CA-36)
    Given el Arquitecto selecciona una Call Activity en el Lienzo que apunta al proceso hijo "Proceso_Riesgo"
    Then el Panel de Propiedades debe mostrar un link clickeable: "[🔗 Abrir Sub-Proceso: Proceso_Riesgo]"
    And al hacer clic, se abre el proceso hijo en una nueva pestaña del Diseñador para editarlo o consultarlo.

  Scenario: Colores Personalizados en Carriles y Tareas (CA-37 - Diferido a V2)
    # NOTA: Este escenario queda documentado pero su implementación se difiere a la Versión 2 del producto.
    Given el Arquitecto selecciona un Carril o Tarea en el Lienzo
    Then puede asignarle un color personalizado desde una paleta de colores para distinguir departamentos.

  Scenario: Autocompletado de Variables en Expresiones (CA-38 - Diferido a V1)
    # NOTA: Originalmente diferido a V2. Reclasificado a V1 por decisión del PO (2026-05-01).
    Given el Arquitecto escribe una condición en una Compuerta Exclusiva (Ej: `${monto > 5000}`)
    Then el sistema ofrece autocompletado de variables disponibles basándose en los formularios asociados al proceso.

  Scenario: FormKey como Dropdown Validado desde Pantalla 7 (CA-39)
    Given el Arquitecto selecciona una User Task en el Lienzo de la Pantalla 6
    When accede al campo "📄 Formulario Asociado" en el Panel de Propiedades
    Then el campo debe ser un Dropdown (NO texto libre) que lista los formularios registrados en la Pantalla 7
    And cada opción del Dropdown muestra: Nombre del formulario, Tipo (🟢 Simple o 🔵 iForm Maestro), y si es Maestro, el número de etapas configuradas
    And si no se selecciona ningún formulario, el Pre-Flight lo marca como Error.

  Scenario: Consistencia de Patrón de Formulario por Proceso (CA-40)
    Given el Arquitecto crea un nuevo proceso en la Pantalla 6
    Then al inicio debe elegir el patrón de formulario: "Patrón A: Formulario Simple" o "Patrón B: iForm Maestro"
    And esta decisión es inmutable para ese proceso (consistente con US-003)
    And si eligió Patrón A, cada User Task mostrará en el Dropdown solo formularios "Simple"
    And si eligió Patrón B, todas las User Tasks compartirán el mismo iForm Maestro y el Dropdown filtrará solo formularios "Maestro".

  Scenario: Sandbox Simulado en Motor de Producción en V1 (CA-41)
    Given el iBPMS V1 opera con un único motor Camunda (no hay ambiente de Desarrollo separado)
    Then el botón [🧪 Sandbox] genera instancias temporales directamente en el motor de producción
    And estas instancias se marcan como "SANDBOX_TEST" y se auto-destruyen al finalizar la simulación
    And la separación real de ambientes (Dev vs Prod) se difiere a V2.

  Scenario: Registro de Auditoría de Diseño tipo Git-Log (CA-42)
    Given el Arquitecto realiza cualquier acción sobre un proceso (importar, editar, guardar borrador, solicitar despliegue, archivar, restaurar versión)
    Then el sistema debe crear una entrada en un log de auditoría persistente (BD) con: Acción, Usuario, Timestamp y Versión Afectada
    And este log debe ser visible para Administradores en un panel "📜 Historial de Cambios" (estilo Git Log) dentro de la Pantalla 6.

  Scenario: Lock Manual sin Expiración Automática (CA-43)
    Given el Arquitecto "maria.lopez" tiene el Lock sobre un proceso
    And permanece inactiva por más de 30 minutos
    Then el Lock NO expira automáticamente
    And otros usuarios que intenten editar verán: "🔒 Bloqueado por maria.lopez. Contacte al usuario para solicitar la liberación."
    And la liberación es un proceso manual: María debe cerrar su pestaña o presionar un botón "Liberar Edición".

  Scenario: Soporte Multi-Pool para Modelado de Colaboración (CA-44)
    Given el Arquitecto crea un nuevo diagrama BPMN en la Pantalla 6
    Then puede agregar múltiples Pools al Lienzo representando actores internos y externos (Ej: "Mi Empresa", "Banco Externo", "Proveedor")
    And puede conectar los Pools con Message Flows (flechas de mensaje) para modelar la interacción
    And los Pools externos son representaciones visuales (cajas negras) que no se ejecutan en el motor Camunda interno
    And esto provee claridad documental y de auditoría sobre quién habla con quién.

  Scenario: Service Task con Dropdown de Conectores API del Hub (CA-45)
    Given el Arquitecto coloca una Service Task en el Lienzo y abre su Panel de Propiedades
    Then el campo "Conector / API" debe ser un Dropdown que lista los conectores registrados en la Pantalla 11 (Hub de Integraciones)
    And cada opción muestra: Nombre del conector, Tipo (REST/SOAP/GraphQL) y Sistema Destino
    And para V1, los conectores pre-armados obligatorios son:
    And - 📧 Microsoft O365 / Exchange (Correo corporativo)
    And - 📁 Microsoft SharePoint (Gestión documental)
    And - 💰 Oracle NetSuite (ERP/Financiero)
    And si el conector necesario NO existe aún en el Hub, consultar CA-37.

  Scenario: MessageEvent como Placeholder de Integración Futura (CA-46)
    Given el Arquitecto necesita modelar una integración con un sistema externo cuyo conector API aún no fue registrado en el Hub (Pantalla 11)
    Then debe usar un MessageEvent (Intermediate Throw/Catch) como marcador visual temporal
    And el Pre-Flight Analyzer debe clasificar este nodo como Advertencia (⚠️): "MessageEvent sin conector API asociado. Considere crear el conector en el Hub y migrar a Service Task."
    And cuando el conector sea registrado posteriormente, el Arquitecto puede reemplazar el MessageEvent por una Service Task enlazada al nuevo conector.

  Scenario: [Onboarding Embebido] Iconos de Ayuda Globales en el Diseñador (CA-47)
    Given el Arquitecto (de cualquier perfil o seniority) selecciona un componente visual en el Lienzo de la Pantalla 6
    Then tanto en la barra superior de herramientas como al lado de cada título del Panel de Propiedades aparecerá un ícono de ayuda `[?]`
    And al hacer hover, el sistema desplegará el Componente de Tooltip Estándar (reutilizado de la US-003).

  Scenario: Tooltips Ricos interactivos y Mapeo de Errores de Sintaxis (CA-48)
    Given la visualización del Tooltip en el Diseñador BPMN
    Then el contenido didáctico estará codificado de forma estática ("quemado") para la V1
    And el componente soportará formato HTML enriquecido permitiendo incrustar hipervínculos azules hacia la documentación oficial
    When el Arquitecto ingresa una expresión inválida o código basura en un campo de configuración (Ej: Listener Script o Condición de Gateway)
    Then el ícono de ayuda y su respectivo Tooltip mutarán dinámicamente a color ROJO para alertar el error de sintaxis visualmente.

  Scenario: Mapeo Visual Estricto (Prohibición de JSON Crudo) (CA-49)
    Given que el Arquitecto selecciona un Conector API (Ej: Oracle) en una Service Task (Pantalla 6)
    When el Frontend despliega el sub-panel de Integración
    Then el sistema TIENE ESTRICTAMENTE PROHIBIDO renderizar un `<textarea>` libre para inyección manual de JSON Payload.
    And debe renderizar un componente `<DataMapperGrid>` de dos columnas: Columna Izquierda (Campos fijos dictados por el Swagger del Hub en Pantalla 11) vs Columna Derecha (Dropdown interactivo).
    And el Dropdown de la derecha consumirá el Diccionario de Datos del proceso (Variables Zod de la Pantalla 7), permitiendo al usuario emparejarlas visualmente con clics.

  Scenario: Coerción Inteligente y Seguridad de Tipos (Type-Safety) (CA-50)
    Given la matriz de mapeo visual `<DataMapperGrid>`
    When el usuario despliega la lista de variables origen para emparejarlas con un destino
    Then el Frontend aplicará un filtro dinámico: mostrará deshabilitadas (sombreadas en gris) con un tooltip explicativo de "Tipo Incompatible" a aquellas variables (Zod) cuyo tipo de dato (String, Number, Boolean) NO coincida matemáticamente con el tipo esperado por el sistema externo.
    And anulando desde el diseño de la UI la posibilidad de enviar un Error 400 (Type Mismatch) a Producción.

  Scenario: Inyección de Valores Constantes (Hardcoding Controlado) (CA-51)
    Given que la API externa requiere un dato que no proviene del Formulario del cliente (Ej: `Country_Code`)
    Then la Columna Derecha del `<DataMapperGrid>` permitirá al usuario alternar entre [Variable Dinámica Zod] y [Valor Estático].
    And si elige [Valor Estático], podrá digitar el texto crudo inyectándolo de forma segura en el Payload saliente.

  Scenario: Inmutabilidad Estricta ante Mutación de Swagger (Zero-Breakage) (CA-52)
    Given un proceso V1 desplegado que utiliza el conector `Oracle_API_v1`
    When el Administrador actualiza el contrato (Swagger) en el Hub (Pantalla 11) renombrando o eliminando variables esperadas
    Then el sistema bloquea la sobrescritura y fuerza la creación de un nuevo conector `Oracle_API_v2`
    And el proceso V1 que ya estaba en el motor sigue funcionando intacto con la versión vieja en caché ("Zero-Breakage Policy")
    And si el Arquitecto desea usar la nueva versión, debe entrar a la Pantalla 6, seleccionar la v2, re-mapear y desplegar una nueva versión temporal del proceso.

  Scenario: Validación Lógica de Cláusulas OneOf/AnyOf (CA-53 - Diferido a V2)
    # NOTA: Este escenario queda documentado pero su implementación se difiere a la Versión 2 del producto.
    Given una API que exige el dato X *o* el dato Y mediante las cláusulas Swagger (OneOf / AnyOf)
    When el Frontend despliega el `<DataMapperGrid>`
    Then agrupa visualmente las filas afectadas bajo la etiqueta `[ 🔀 Requiere mapear al menos UNO ]`
    And el Pre-Flight Analyzer verificará el grupo lógico en conjunto: Si falta al menos uno, alerta roja y aborta despliegue. Si ambos están vacíos, aborta. Si uno está lleno, autoriza el pase a Producción.

  Scenario: Shift-Left Security para Datos Sensibles (PII/PHI) (CA-54 - Diferido a V2)
    # NOTA: Este escenario queda documentado pero su implementación se difiere a la Versión 2 del producto.
    Given el mapeo de una variable clasificada con el flag `[🔒 Dato Sensible PII]` desde la Pantalla 7 (Zod)
    When la Service Task dispara la integración hacia la API externa
    Then el dato crudo viaja obligatoriamente encriptado por el túnel HTTP/TLS
    And el motor de auditoría histórica de Camunda (History Level) tiene estrictamente PROHIBIDO persistir el valor real en texto plano dentro de sus logs, reemplazándolo obligatoriamente por un hash o la viñeta `[REDACTED_PII]`.

  Scenario: Mapeo Reestringido de Headers Dinámicos (CA-55)
    Given que la API exige metadatos de usuario por cada transacción (Ej: `User_ID`) en las cabeceras REST
    Then el Data Mapper ofrecerá una tercera pestaña visual denominada `[ 🔑 HEADERS DINÁMICOS ]`
    And la UI aplicará severas restricciones denegando la inserción de texto libre o crudo para prevenir Header Injection.
    And obligará a mapear valores usando únicamente variables pre-validadas del formulario (Zod) o Macros seguras del Sistema.

  Scenario: Delegación Transparente de Conversión Binaria (Multipart/Base64) (CA-56 - Diferido a V2)
    # NOTA: Este escenario queda documentado pero su implementación se difiere a la Versión 2 del producto.
    Given un componente Zod de tipo `<InputFile>` mapeado hacia un atributo del Payload destino
    When el Arquitecto despliega y llega el momento de la ejecución
    Then el flujo UI no exige que el Arquitecto indique la técnica de conversión
    And el Worker (Backend) intercepta el mapping, consulta en caliente el requerimiento del Swagger (Multipart-FormData vs Base64), y lo transmuta automáticamente antes de inyectar la data a la trama HTTP de salida.

  Scenario: Ley de Omisión Pura de Llaves Nulas (Drop Key by Default) (CA-57)
    Given una variable Zod marcada como Opcional que el usuario no diligenció en el Runtime (cuyo valor es `null` o vacío)
    When la petición es empaquetada hacia el sistema remoto
    Then el Backend aniquila y purga la llave entera ("Key") del JSON saliente, evitando enviar sintaxis propensa a crashes (Ej: `"campo": null`)
    And la única excepción será si el Swagger explicíta la obligación del campo como `nullable: true`, obligando al envío literal.

  Scenario: Resiliencia Asíncrona Parametrizable (Retry Pattern Visual) (CA-58)
    Given la configuración de una Task API Integrada en Pantalla 6
    Then el sistema expone un sub-panel `[ ⚙️ Estrategia de Fallo (Retries) ]`
    And permite configurar intentos asíncronos y ventana retardo temporal (Ej. 3 intentos espaciados por 5 mins)
    And los reintentos operan como Background Jobs (Job Executor) liberando ram de la UI, y si la póliza se agota, canaliza automáticamente el Thread BPMN hacia el Boundary Error Event modelado de rescate humano.

  Scenario: Amnesia Selectiva Obligatoria de Datos No Mapeados (Output Pruning) (CA-59)
    Given una respuesta de la API externa que retorna un Payload gigante (Ej: JSON de 15 MB)
    And el Arquitecto sólo enlazó visualmente 1 variable diminuta (`Ticket_ID`) en la pestaña de `[ 📥 OUTPUT MAPPING ]`
    When arriba el Payload y se graba el Ticket_ID en el Bolsillo Global de Variables (Process Runtime)
    Then de manera sincrónica el motor de Camunda invoca la poda total (Garbage Collection)
    And destruye los remanentes masivos no procesados liberando I/O y evitando contaminar la Base de Datos operativa del motor BPM.

Scenario: Mapeo Obligatorio de Parámetros en Subprocesos (In/Out Mapping) (CA-60)
    Given el Arquitecto arrastra una `Call Activity` (Llamar Proceso Hijo) al lienzo en la Pantalla 6
    When intenta configurar las propiedades de este nodo
    Then el sistema TIENE ESTRICTAMENTE PROHIBIDO permitir el despliegue si no se configura la matriz de "In/Out Mapping".
    And el panel debe obligar a mapear "Qué variables le entrego al hijo al nacer" (Input) y "Qué variables me devuelve el hijo al terminar" (Output).
    And si el Arquitecto deja esta matriz vacía, el Pre-Flight Analyzer bloqueará el despliegue (❌) con el error: "El Subproceso nacerá ciego por falta de datos".

Scenario: Vinculación Estricta del Cerebro Lógico (Business Rule Task a DMN) (CA-61)
    Given el Arquitecto arrastra un nodo `Business Rule Task` (Regla de Negocio) para evaluar una decisión
    When configura las propiedades del nodo
    Then el panel de propiedades NO permitirá escribir código libre.
    And mostrará un Dropdown obligatorio llamado `[ 🧠 Tabla de Decisión (Decision_Ref) ]` que lista todas las tablas DMN creadas en la Pantalla 4 (US-007).
    And si el nodo no tiene una tabla DMN amarrada, el Pre-Flight Analyzer abortará el despliegue a Producción (❌).

Scenario: Prohibición de Trabajo Síncrono en Camunda (External Task Pattern) (CA-62)
    Given el Arquitecto configura un nodo automático como una `Service Task` o `Send Task`
    When el motor procesa el XML para el despliegue
    Then la arquitectura del iBPMS TIENE ESTRICTAMENTE PROHIBIDO usar `Java Delegates` o expresiones síncronas que ejecuten código pesado dentro del hilo (Thread) principal de Camunda.
    And el motor forzará estructuralmente el uso del patrón `External Task` (Trabajadores Externos).
    And Camunda simplemente publicará la intención de trabajo en un Topic (Ej: `topic="generar_pdf"`), liberando su memoria inmediatamente, a la espera de que los microservicios satélite (Workers) hagan el trabajo pesado y reporten el resultado asíncronamente.



  # ==============================================================================
  # B. REMEDIACIONES POST-AUDITORÍA (Sprint Remediation Brief 2026-04-05)
  # Origen: docs/requirements/us005_functional_analysis.md
  # Tickets: REM-005-01 a REM-005-06
  # Propósito: Cerrar GAPs de implementación detectados por el workflow
  #            /analisisEntendimientoUs.md tras finalizar las 17 iteraciones
  #            de la Auditoría Integral del Backlog.
  # ==============================================================================

Scenario: Aislamiento Transaccional del Sandbox en Producción (Zero-Blast Radius) (CA-63)
    Given la ejecución de una simulación de proceso directamente en Producción (Modo Sandbox activado)
    When el token simulado alcanza una `ServiceTask` externa (Hub US-033) o una `SendTask` (Correos US-049)
    Then el Engine inyectará obligatoriamente una variable/header oculto en el contexto: `X-Sandbox-Mode: true`.
    And los Workers de Integración y Notificaciones interceptarán esta bandera de forma imperativa.
    And ABORTARÁN cualquier petición de red HTTP o envío de correo SMTP real.
    And devolverán un `Mock Response` (HTTP 200 OK simulado) al motor Camunda, protegiendo a los clientes y sistemas ERP de recibir basura transaccional durante las pruebas del Arquitecto.

Scenario: Intervención de Emergencia sobre Bloqueo Pesimista (Break-Lock)  (CA-64)
    Given un proceso BPMN bloqueado para edición por el "Lock Pesimista" de un Arquitecto ausente o cuyo PC falló
    When la parálisis del lienzo afecta el mantenimiento y un usuario con el rol `Super_Admin` accede al Catálogo (Pantalla 6)
    Then el sistema le habilitará un botón de emergencia rojo `[ 🔓 Romper Candado (Break-Lock) ]`.
    And al ejecutarlo, el Backend destruirá el lock en la Base de Datos, liberando el proceso para edición inmediata.
    And registrará inamoviblemente en el Audit Log quién y cuándo forzó la liberación del diseño corporativo retenido por otro empleado.

  Scenario: [REMEDIACIÓN] Contrato API Explícito para el Endpoint de Despliegue (CA-65)
    # Origen: REM-005-01 — GAP-1 del us005_functional_analysis.md
    Given la necesidad de alinear Frontend y Backend en el contrato de despliegue BPMN (CA-1)
    Then el endpoint `POST /api/v1/design/processes/deploy` aceptará un `multipart/form-data` con los siguientes campos:
    And Campo obligatorio `file` (tipo: file, extensión: `.bpmn`, max: 5MB) — el diagrama BPMN 2.0 XML.
    And Campo obligatorio `deploy_comment` (tipo: string, min: 10 chars) — justificación del despliegue para el audit log.
    And Campo opcional `force_deploy` (tipo: boolean, default: false) — si `true`, salta las advertencias ⚠️ del Pre-Flight (pero NO los errores ❌).
    And el Response Body del `201 Created` incluirá obligatoriamente: `deployment_id`, `process_definition_id`, `process_definition_key`, `version` (int), `deployed_at` (ISO 8601 UTC), `deployed_by` (user_id).
    And existirá un endpoint separado de validación: `POST /api/v1/design/processes/validate` que ejecuta el Pre-Flight Analyzer sin desplegar, retornando la lista de errores y advertencias en formato JSON.
    And el contrato se documentará con OpenAPI/Swagger annotations en el Controller.

  Scenario: [REMEDIACIÓN] Persistencia del Lock Pesimista en Base de Datos (CA-66)
    # Origen: REM-005-02 — GAP-2 del us005_functional_analysis.md
    Given el mecanismo de Lock Pesimista para edición concurrente (CA-16, CA-43, CA-64)
    Then el lock se persistirá en una tabla `ibpms_process_locks` con columnas: `process_definition_key` (PK), `locked_by` (FK user_id), `locked_at` (timestamp UTC), `browser_session_id` (para detectar tabs cerradas).
    And el lock aplica por `process_definition_key` (todo el proceso, no por versión específica).
    And el lock NO expiará automáticamente por tiempo (consistente con CA-43) pero SÍ se liberará automáticamente si el Backend detecta que la sesión WebSocket/SSE del navegador del Arquitecto se desconecta (heartbeat cada 30 segundos).
    And si el heartbeat falla 3 veces consecutivas (90 segundos sin respuesta), el lock se libera automáticamente y se registra en `ibpms_audit_log`: "[AUTO-RELEASE] Lock del proceso X liberado por desconexión del usuario Y".
    And el Break-Lock de emergencia (CA-64, rol Super Admin) actualizará la misma tabla y registrará quién forzó la liberación.
    And al reiniciar el servidor de aplicación, los locks persistidos en BD sobreviven y siguen vigentes.

  Scenario: [REMEDIACIÓN] Límites y Gobernanza del Sandbox en Producción (CA-67)
    # Origen: REM-005-03 — GAP-3 del us005_functional_analysis.md
    Given la ejecución de instancias Sandbox directamente en el motor de producción (CA-20, CA-41, CA-63)
    Then el sistema impondrá un límite máximo de 3 instancias Sandbox concurrentes a nivel global del sistema.
    And si un Arquitecto intenta iniciar una cuarta simulación, el sistema la rechazará con el mensaje: "Límite de Sandbox alcanzado (3/3). Espere a que finalice una simulación en curso."
    And cada instancia Sandbox tendrá un timeout de auto-destrucción de 10 minutos. Si el token no ha completado su recorrido en ese tiempo, el motor la anulará automáticamente y registrará: "[SANDBOX-TIMEOUT] Instancia sandbox {id} destruida por timeout (10min)."
    And las instancias Sandbox serán visibles en la Pantalla 15.A (Centro de Incidentes) con un badge visual "[🧪 SANDBOX]" para diferenciarlas de instancias reales, pero NO se mostrarán en los dashboards operativos del Workdesk.
    And el contador de instancias Sandbox activas se almacenará en Redis (`ibpms:sandbox:count`) con TTL de 15 minutos como failsafe.

  Scenario: [REMEDIACIÓN] Persistencia del Data Mapping como Extension Properties del BPMN XML (CA-68)
    # Origen: REM-005-04 — GAP-4 del us005_functional_analysis.md
    Given la configuración del DataMapperGrid (CA-49 a CA-57) donde el Arquitecto mapea variables visualmente
    Then el mapping finalizado se persistirá como `camunda:inputOutput` extension properties dentro del nodo `ServiceTask` del XML BPMN, garantizando portabilidad del diagrama.
    And adicionalmente, se almacenará una copia indexada del mapping en la tabla `ibpms_data_mappings` (columnas: `process_definition_key`, `task_id`, `connector_id`, `mapping_json`, `last_validated_at`) para consultas rápidas y validación cruzada.
    And si el Arquitecto modifica el formulario en la Pantalla 7 (US-003) y elimina o renombra una variable Zod que está referenciada en un mapping existente, el Pre-Flight Analyzer lo detectará como Error ❌: "Variable '{varName}' referenciada en el mapping de la tarea '{taskName}' ya no existe en el formulario."
    And el Pre-Flight Analyzer validará la integridad de TODOS los mappings del BPMN antes de permitir el despliegue.

  Scenario: [REMEDIACIÓN] Flujo Completo de Solicitud de Despliegue con Rechazo y Notificación (CA-69)
    # Origen: REM-005-05 — GAP-5 del us005_functional_analysis.md
    Given el workflow de Solicitud de Despliegue del Designer al Release Manager (CA-34)
    Then la solicitud se implementará como un registro en la tabla `ibpms_deploy_requests` (columnas: `id`, `process_definition_key`, `requested_by`, `requested_at`, `status` ENUM: PENDING/APPROVED/REJECTED, `reviewed_by`, `reviewed_at`, `review_comment`).
    And al presionar [📩 Solicitar Despliegue], se creará una tarea visible en el Workdesk del Release Manager con los botones [🚀 Aprobar y Desplegar] y [❌ Rechazar].
    And al Rechazar, el Release Manager TIENE OBLIGACIÓN de ingresar un comentario de rechazo (min 20 chars) explicando qué debe corregir el Designer.
    And el Designer recibirá una notificación (bell icon + email vía US-049) informando si su solicitud fue aprobada o rechazada, junto con el comentario del Release Manager.
    And existirá un historial visible en la Pantalla 6: "[📜 Historial de Solicitudes]" listando todas las solicitudes anteriores con su estado, revisor y comentario.

  Scenario: [REMEDIACIÓN] Catálogo de External Task Topics con Validación Pre-Flight (CA-70)
    # Origen: REM-005-06 — GAP-6 del us005_functional_analysis.md
    Given la obligatoriedad de External Task Pattern (CA-62) donde cada Service Task se suscribe a un Topic
    Then el sistema mantendrá un catálogo oficial de Topics en la tabla `ibpms_external_task_topics` (columnas: `topic_name`, `description`, `worker_class`, `is_active`, `registered_at`).
    And el campo Topic en las propiedades de la Service Task (Pantalla 6) será un Dropdown que consume este catálogo, NO un campo de texto libre.
    And los Topics pre-registrados obligatorios para V1 serán: `ibpms.send_email` (US-049), `ibpms.sync_erp` (NetSuite), `ibpms.sync_sharepoint`, `ibpms.generate_pdf`, `ibpms.ai_copilot` (US-017), `ibpms.webhook_outbound` (US-004).
    And el Pre-Flight Analyzer validará que cada Service Task del BPMN tenga un Topic que exista en el catálogo. Si el Topic no existe, emitirá Error ❌: "La tarea '{taskName}' refiere al topic '{topicName}' que no está registrado en el catálogo de Workers."
    And el Administrador IT podrá registrar nuevos Topics desde una sección administrable en la Pantalla 11 (Hub de Integraciones).

  Scenario: [REMEDIACIÓN] Hard-Stop Estructural de Gobernanza (CA-71)
    # Origen: us005_audit_report.md — Ticket: REM-005-CA07 y CA09
    Given el analizador Pre-Flight de Despliegue BPMN
    When un Arquitecto intenta desplegar un diagrama con ambigüedades estructurales (ej. pasarelas divergentes sin flujos por defecto o procesos sin start/end events)
    Then el motor debe detectar la ambigüedad y arrojar un `PreFlightResultDTO` con severidad `ERROR`
    And bloquear físicamente el despliegue hacia producción retornando HTTP 422
    And garantizando que ningún proceso lógicamente roto pueda instanciarse.

  Scenario: [REMEDIACIÓN] Detección de Nodos Zombie o Colgados (CA-72)
    # Origen: us005_audit_report.md — Ticket: REM-005-CA22
    Given la validación topológica del motor BPMN en la etapa de diseño
    When se evalúa un nodo (UserTask, ServiceTask, Event) dentro del flujo
    Then el analizador debe verificar que todos los nodos (excepto StartEvent) posean al menos un flujo `incoming`
    And debe verificar que todos los nodos (excepto EndEvent) posean al menos un flujo `outgoing`
    And en caso de omisión, debe emitir un ERROR de "Nodo Zombie / Colgado" bloqueando el despliegue.

  Scenario: [REMEDIACIÓN] Detección Topológica de Bucles Infinitos Síncronos (CA-73)
    # Origen: us005_audit_report.md — Ticket: REM-005-CA23
    Given la validación topológica de seguridad (Anti-DoS) del motor
    When el motor procesa el grafo completo de SequenceFlows y Tareas
    Then debe discriminar aristas que apunten a "Wait States" (UserTask, Timer, ReceiveTask, CatchEvent)
    And aplicar Búsqueda en Profundidad (DFS) sobre el sub-grafo residual síncrono
    And si detecta un ciclo cerrado en tiempo de compilación/diseño, levantar un ERROR "INFINITE_LOOP_DETECTED"
    And abortar el despliegue para evitar caídas de CPU y denegación de servicio (DoS) en el contenedor.

  Scenario: [REMEDIACIÓN] Hard-Stop para Pasarelas Divergentes sin Convergencia (CA-74)
    # Origen: us005_audit_report.md — Ticket: REM-005-CA27
    Given la validación topológica del motor BPMN
    When un Arquitecto modela una pasarela paralela o inclusiva divergente (flujos de salida > 1)
    Then el analizador debe auditar si existe al menos una pasarela convergente correspondiente
    And si detecta pasarelas divergentes "huérfanas" de convergencia, debe levantar un ERROR "GATEWAY_CONVERGENCE_MISMATCH"
    And abortar el despliegue para prevenir la fuga de tokens y ejecuciones fantasma.

```
**Trazabilidad UX:** Wireframes Pantalla 6 (Diseñador BPMN) y Pantalla 14 (RBAC).

---


### US-006: Diseñar la Estructura Base (WBS) de una Plantilla de Proyecto
**Como** PMO / Director de Proyectos / Administrador
**Quiero** crear una Plantilla Maestra definiendo jerárquicamente las Fases y Tareas Secuenciales, y pre-asignar Formularios a cada tarea genérica
**Para** que exista un molde estandarizado (WBS) que evite re-trabajo cuando un Gerente desee instanciar un proyecto nuevo (ya sea usando metodología Tradicional/Gantt o metodología Ágil).

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Standalone Project Template Builder (WBS)
  Scenario: Profundidad Controlada de WBS (CA-1)
    Given que el PMO diseña una estructura jerárquica en la Pantalla 8
    When el usuario intenta anidar tareas de forma vertical ("Sub-fase de Sub-fase de Tarea")
    Then el sistema restringe estructuralmente la profundidad a un máximo estricto de 5 Niveles
    And si se intenta exceder, deshabilita el botón "+" y proyecta una advertencia de tope arquitectónico ("Profundidad Máxima Alcanzada").

  Scenario: Versionamiento Seguro de Plantillas Vivas (CA-2)
    Given una Plantilla V1.0 que está siendo utilizada y consumida por 50 Proyectos Vivos
    When la PMO edita la plantilla (Ej. Le agrega 3 Fases nuevas) y oprime [Actualizar Producción]
    Then el sistema NO muta los 50 proyectos vivos (se anclan al Snapshot originario inmutable V1.0)
    And emite la versión V2.0 exclusivamente disponible para nuevas aperturas de Proyectos, requiriendo en paralelo Aprobación Administrativa mediante un Botón Rojo Fuerte [Pushear Nueva Versión] para forzar validación por partida doble.

  Scenario: Tipificación Estricta de Plantilla (Tradicional vs Ágil) (CA-3)
    Given que la PMO acciona la creación de una Nueva Plantilla en la Pantalla 8
    When el sistema levanta el Modal de Creación
    Then obliga explícitamente a clasificar la plantilla seleccionando un tipo rígido: `[Tradicional (Gantt)]` o `[Ágil (Sprints)]`
    And esta clasificación gobierna el comportamiento del lienzo: Si elije "Ágil", el botón de relacionar dependencias (Fin-a-Inicio) desaparece permanentemente del UI y se prohíbe crear conceptos estructurales como "Hitos".

  Scenario: Transición Formulario a DONE en Ágil (CA-4)
    Given una tarea instanciada en el Tablero Kanban (Ágil) originada desde una Plantilla
    And esta tarea tiene el "Formulario_QA" asociado en su definición maestra
    When el desarrollador termina el trabajo y oprime enviar el formulario
    Then el sistema autoevalúa la completitud de la data y, en caso de éxito, arrastra logísticamente la tarjeta a la columna "DONE" del Sprint, aplicando un Definition of Done duro atado a data.

  Scenario: Independencia Evolutiva Locativa (CA-5)
    Given un Scrum Master que instanció un Proyecto Ágil basado en la Plantilla V1.0
    When el Scrum Master elimina 5 de las tareas heredadas del Backlog local del proyecto porque no aplican a su Sprint
    Then el borrado es estrictamente Local (Muta solo el Proyecto Instanciado)
    And la Plantilla original inmutable "V1.0" no pierde las tareas orgánicamente y futuros proyectos las seguirán heredando intactas.

  # ==============================================================================
  # REFERENCIA CRUZADA: Integración con US-030 (Hub Ágil)
  # Fecha de creación: 2026-04-17
  # Origen: Decisión arquitectónica del Sprint 3 — Slicing Planner
  # ==============================================================================

  Scenario: [INTEGRACIÓN] Habilitación del Clonaje WBS hacia el Hub Ágil (CA-6)
    # NOTA OBLIGATORIA: Este CA es una DEUDA de integración con la US-030 CA-2.
    # La US-030 se construirá SIN el botón "Usar Plantilla WBS" (solo "Iniciar vacío")
    # porque esta US-006 aún no está desarrollada. Cuando se construya la US-006,
    # el equipo DEBE habilitar la opción 2 del Pop-Up del CA-2 de la US-030.
    Given que la US-006 ha sido construida y las Plantillas WBS existen en la Pantalla 8
    When el Líder de Proyecto crea un nuevo Proyecto Ágil en la Pantalla 9 (US-030 CA-2)
    Then el sistema DEBERÁ habilitar la opción "Usar una Plantilla WBS" en el Pop-Up de Selección Inicial
    And el listado de plantillas será alimentado por el endpoint de la US-006 (catálogo de Plantillas activas)
    And el clonaje profundo de tareas hacia el Hub Ágil (Pantalla 10) seguirá las reglas de copia definidas en el CA-5 de esta US-006 (Independencia Evolutiva Locativa).

```
**Trazabilidad UX:** Wireframes Pantalla 8 (Project Template Builder).


---

### US-007: Generador Cognitivo de DMN (NLP a Tablas de Decisión)
**Como** Arquitecto de Procesos / Usuario de Negocio
**Quiero** escribir políticas de negocio en lenguaje natural (ej. "Aprobar si monto < 1000")
**Para** que el iBPMS las traduzca de forma segura, asíncrona y estructurada a una tabla matemática DMN (Hit Policy: FIRST), erradicando la ambigüedad humana sin exponer datos PII a modelos LLM externos y protegiendo el performance del servidor.

> [!IMPORTANT]
> **Dependencias Externas Críticas de la US-007:**
> - **US-005 (Despliegue BPMN / Pantalla 6):** La Business Rule Task del BPMN consume la DMN vía `Decision_Ref` (CA-61 de US-005). El binding LATEST vs DEPLOYMENT (CA-12 de US-005) decide qué versión DMN aplica en runtime. El Pre-Flight Analyzer debe validar la compatibilidad del Catch-All (CA-14 de US-007) contra el diseño del Gateway posterior.
> - **US-003 (IDE Formularios / Pantalla 7):** El Diccionario de variables Zod alimenta las columnas de entrada de la DMN. Si una variable Zod se renombra o elimina, la DMN se rompe silenciosamente. La invalidación de caché Redis (CA-16) depende de que US-003 publique el evento `FORM_SCHEMA_CHANGED`.
> - **US-036 (RBAC / Pantalla 14):** El rol `ROLE_PROCESS_ARCHITECT` que protege la creación y publicación de DMNs (CA-06) se administra desde la Pantalla 14.
> - **US-033 (Hub de Integraciones / Pantalla 11):** Si el output de una DMN gatilla una integración API (Ej: "Rechazar → Notificar CRM"), la Service Task posterior consume conectores del Hub.
> - **US-034 (RabbitMQ):** El evento de invalidación de caché `FORM_SCHEMA_CHANGED` (CA-16) se transmite vía el broker de mensajería. La publicación de una DMN V2 podría necesitar invalidar caché de Workers que evaluaban la V1.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: NLP to DMN Translation, SRE Architecture & AppSec Governance

  # ==============================================================================
  # A. SRE, DESEMPEÑO Y ARQUITECTURA CLOUD (Anti-Timeout y Ahorro)
  # ==============================================================================
  Scenario: Streaming Asíncrono de Generación (Server-Sent Events) (CA-01)
    Given la latencia inherente de los modelos fundacionales (LLMs) al generar tablas complejas
    When el usuario envía el Prompt
    Then la arquitectura TIENE PROHIBIDO usar peticiones HTTP síncronas bloqueantes que arriesguen un error `504 Gateway Timeout`.
    And el Backend abrirá un canal de Server-Sent Events (SSE).
    And el Frontend pre-renderizará la grilla DMN visualmente (Fila por Fila) a medida que lleguen los fragmentos, mitigando la ansiedad del usuario mediante un Skeleton Loader.

  Scenario: Mitigación Denial of Wallet (DoW) y Caché Criptográfica (CA-02)
    Given la exposición de la API del LLM a los constructores internos
    Then el API Gateway impondrá un Rate Limiting estricto (Ej: Max 5 generaciones/minuto por usuario) para evitar facturas catastróficas.
    And el Backend calculará el Hash del (Prompt + Diccionario); si existe un match exacto en Redis, devolverá la tabla DMN cacheadada instantáneamente, evadiendo el costo Cloud del LLM.

  Scenario: Garbage Collection y Compresión XML (CA-03)
    Given las múltiples iteraciones (Borradores) que un usuario genera en el Chat
    Then los XMLs temporales NO sellados vivirán en el LocalStorage del Frontend y serán purgados físicamente de PostgreSQL a las 24h mediante un Job.
    And al aprobar (Sellar) la versión final, el Backend purgará todos los espacios en blanco inútiles (XML Minification) antes de hacer COMMIT, reduciendo drásticamente el peso en disco.
    And el Backend despachará un Job asíncrono para inicializar el DMN en Camunda (Warm-Up Cache) eliminando la latencia en frío de la primera ejecución operativa.

  # ==============================================================================
  # B. SEGURIDAD (APPSEC), PRIVACIDAD Y HARDENING
  # ==============================================================================
  Scenario: Sandboxing Anti-RCE y Prevención XSS (CA-04)
    Given la inyección de código por parte de la IA o de un humano en modo desarrollador
    Then el motor FEEL de Camunda será encapsulado en un Sandbox estricto de Java, bloqueando cualquier intento de Ejecución Remota de Código (RCE) o acceso a métodos del sistema.
    And el Middleware envolverá el Prompt en "System Instructions" anti-Jailbreak.
    And el Frontend aplicará escapado HTML estricto (DOMPurify) a toda celda renderizada para prevenir Cross-Site Scripting (XSS).
    And los Secrets de las APIs de IA (OpenAI/Anthropic) vivirán exclusivamente inyectados como Variables de Entorno en el Vault, jamás en base de datos.

  Scenario: Seudonimización PII y Anti-Spoofing Forense (CA-05)
    Given el mapeo de variables estructurales hacia el LLM externo
    Then el Backend seudonimizará los nombres de las variables (Ocultando indicios PII) antes de enviarlos a la nube.
    And al guardar la Auditoría del Prompt, el Backend extraerá la identidad criptográfica del Autor directamente desde el Token JWT, haciendo imposible la falsificación de identidad (Spoofing) desde el cliente.

  Scenario: Inmutabilidad DMN y RBAC (BOLA/IDOR) (CA-06)
    Given una tabla DMN en estado "Activa" o "Aprobada"
    When un atacante o usuario intenta hacer un `PUT` directo al endpoint REST para alterar un valor
    Then el Backend interceptará y arrojará `403 Forbidden` absoluto; cualquier modificación análoga o por IA genera obligatoriamente una V2.
    And al reciclar DMNs globales, el Backend validará que el usuario posea el rol `ROLE_PROCESS_ARCHITECT` y pertenezca al `Tenant_ID` dueño de la regla.

  # ==============================================================================
  # C. INTEGRIDAD MATEMÁTICA Y REGLAS DE NEGOCIO
  # ==============================================================================
  Scenario: Resolución Matemática Segura (Hit Policy: FIRST) y Catch-All (CA-07)
    Given la posibilidad de que el LLM o el humano generen rangos solapados
    When el motor ensamble la tabla DMN final
    Then inyectará estructuralmente la política `Hit Policy = FIRST` (evaluación descendente con parada en la primera coincidencia), erradicando las excepciones de colisión de Camunda (`UNIQUE`).
    And inyectará una Fila Final inamovible (Candado Visual 🔒) actuando como "Catch-All Rule" obligatoria para valores Nulos, enrutando a `[Revisión Humana]`.

  Scenario: Variables Planas, Coerción de Tipos y Prohibición Date-Math (CA-08)
    Given las directrices del Diccionario Zod de la Pantalla 7
    Then la IA tiene prohibido usar "Dot Notation" (Ej: `Cliente.Mora`); procesará exclusivamente variables planas de primer nivel para V1.
    And el Backend validará que los tipos de datos en la regla coincidan con Zod (Type Coercion).
    And la IA tiene prohibido hacer Date-Math (resta de fechas); el Frontend/Zod pre-calculará esos valores enteros antes del flujo.
    And toda evaluación de texto aplicará funciones de normalización `lowercase()` en FEEL para ignorar la sensibilidad a mayúsculas.

  Scenario: Límites Cognitivos, Outputs Atómicos y Validación Inversa (CA-09)
    Given la generación de XML DMN (Vía IA o Modo Desarrollador XML Upload)
    Then la estructura generada exigirá Salidas Atómicas (Máximo 1 Output Compuesto en V1).
    And el tamaño del Prompt será truncado al "Token Limit" para evitar fallas de contexto.
    And el Backend ejecutará un `Overlap Check` y rechazará XMLs con disyunciones vacías.
    And el Backend impondrá un Hard-Stop paramétrico de máximo 50 filas generadas; superarlo bloquea la transacción.

  # ==============================================================================
  # D. EXPERIENCIA DE USUARIO (UX/UI) Y GOBERNANZA
  # ==============================================================================
  Scenario: Ergonomía de Alta Densidad y Rescate (Virtual Scrolling) (CA-10)
    Given el renderizado de una matriz de 50 filas x 10 columnas en la Pantalla 4
    Then Vue 3 implementará `Virtual Scrolling` (DOM perezoso) para evitar congelamientos de RAM en el cliente.
    And la grilla soportará navegación nativa por teclado (Enter, Tab) imitando a MS Excel.
    And el `LocalStorage` rescatará cualquier edición análoga en curso si el usuario cierra la pestaña por accidente.

  Scenario: Explicabilidad Visual (XAI) y Simulador de Decisiones (CA-11)
    Given la incomprensión de sintaxis matemática por usuarios de negocio
    Then la UI inyectará una columna autogenerada "Explainable DMN" que traduzca el código FEEL a oraciones humanas legibles.
    And existirá un botón `[🧪 Probar DMN / Simulator]` donde el usuario podrá digitar variables de prueba (Ej: Mora=500), y el Frontend iluminará visualmente en verde la Fila que Camunda ejecutaría en la vida real.

  Scenario: Contención de Pánico y Trazabilidad del Chat (CA-12)
    Given que el usuario termina de editar la DMN y decide publicar
    When el usuario presiona [Publicar V2]
    Then el Frontend desplegará un Modal Inevitable exigiendo digitar `CONFIRMO_V2` para evitar clics accidentales.
    And existirá un botón de `[ ⏪ Revertir a V1 ]` explícito para rollback rápido.
    And el historial del Chat NLP persistirá visualmente atado a esa Versión, y los colores de la grilla cumplirán la norma WCAG AA para diferenciar celdas hechas por IA vs editadas a mano.


  # ==============================================================================
  # E. REMEDIACIONES POST-AUDITORÍA (Sprint Remediation Brief 2026-04-05)
  # Origen: docs/requirements/us007_functional_analysis.md
  # Tickets: REM-007-01 a REM-007-06
  # Propósito: Cerrar GAPs de implementación detectados por el workflow
  #            /analisisEntendimientoUs.md tras finalizar las 17 iteraciones
  #            de la Auditoría Integral del Backlog.
  # ==============================================================================

  Scenario: [REMEDIACIÓN] Resolución de Persistencia Dual de Borradores DMN (CA-13)
    # Origen: REM-007-01 — GAP-1 del us007_functional_analysis.md
    # Corrige la contradicción del CA-03 que reclama LocalStorage Y PostgreSQL simultáneamente.
    Given la necesidad de persistir borradores DMN durante la iteración del chat NLP (CA-03)
    Then los borradores DMN seguirán la arquitectura de persistencia híbrida:
    And 1. Los borradores se persistirán PRIMARIAMENTE en PostgreSQL vía `POST /api/v1/dmn/drafts` (tabla `ibpms_dmn_drafts`, columnas: `id`, `user_id`, `prompt_hash`, `xml_content`, `created_at`, `expires_at`).
    And 2. El LocalStorage del Frontend actuará como CACHÉ DE SESIÓN ACTIVA para evitar peticiones redundantes al Backend mientras el usuario itera en la misma pestaña.
    And 3. Un Job Scheduler del Backend purgará físicamente de PostgreSQL los borradores con `expires_at` superado (TTL: 24 horas), consistente con el CA-03 original.
    And 4. Al sellar (aprobar) la versión final, el Backend eliminará todos los borradores asociados a ese `prompt_hash` y el Frontend destruirá su caché local.
    And queda ELIMINADA la ambigüedad del CA-03: PostgreSQL es la fuente de verdad de los borradores, LocalStorage es solo caché efímero.

  Scenario: [REMEDIACIÓN] Validación Pre-Flight del Catch-All DMN contra el BPMN (CA-14)
    # Origen: REM-007-02 — GAP-2 del us007_functional_analysis.md
    # Previene el "Silent Killer": aprobaciones automáticas por falta de Gateway post-DMN.
    Given la obligatoriedad de la fila Catch-All con output "Revisión Humana" en toda tabla DMN (CA-07)
    Then el Pre-Flight Analyzer de la US-005 (Pantalla 6) incluirá una regla de validación cruzada obligatoria:
    And al evaluar una Business Rule Task que referencie una DMN con Catch-All activo, el Pre-Flight verificará que INMEDIATAMENTE DESPUÉS de esa tarea exista un Exclusive Gateway que evalúe la variable de output de la DMN.
    And si el Gateway no contempla una rama que enrute el valor "Revisión Humana" (o su equivalente configurado) a una User Task, el Pre-Flight emitirá Error ❌ bloqueante: "La Business Rule Task '{taskName}' produce el output 'Revisión Humana' vía Catch-All, pero el Gateway posterior no tiene una rama que lo enrute a una tarea humana. El proceso desplegado ignoraría este caso silenciosamente."
    And esta validación se ejecutará en tiempo de despliegue del BPMN (no en tiempo de publicación de la DMN), porque es responsabilidad del diseño del proceso, no de la tabla de decisión.
    And si la Business Rule Task NO tiene un Gateway inmediatamente posterior (conecta directo a otra tarea), el Pre-Flight emitirá Advertencia ⚠️: "La Business Rule Task '{taskName}' no tiene Gateway posterior. Los outputs de la DMN serán ignorados."

  Scenario: [REMEDIACIÓN] Endpoint Dedicado para el Simulador de Decisiones DMN (CA-15)
    # Origen: REM-007-03 — GAP-3 del us007_functional_analysis.md
    Given la funcionalidad del Simulador de Decisiones (CA-11) que permite probar la DMN con variables ficticias
    Then la evaluación de prueba se ejecutará en el Backend, NO en el Frontend, para garantizar paridad con el motor FEEL de Camunda en producción.
    And el Backend expondrá el endpoint `POST /api/v1/dmn/{id}/evaluate-test` que aceptará un JSON con las variables de prueba (Ej: `{"monto": 5000, "mora_dias": 45}`).
    And el endpoint delegará la evaluación al motor DMN de Camunda en modo Sandbox (sin persistir resultados) y retornará: `{"matched_rule_index": 3, "output": {"decision": "Revisión Humana"}, "all_rules_evaluated": [...]}`.
    And el Frontend iluminará visualmente en verde la fila `matched_rule_index` retornada, consistente con el CA-11.
    And las variables de prueba NO se persisten como casos de test reutilizables en V1 (diferido a V2). Son efímeras y se pierden al cerrar la Pantalla 4.
    And TIENE PROHIBIDO implementar un parser FEEL en JavaScript en el Frontend para evitar discrepancias de evaluación con el motor real.

  Scenario: [REMEDIACIÓN] Invalidación de Caché Redis al Mutarse el Diccionario Zod (CA-16)
    # Origen: REM-007-04 — GAP-4 del us007_functional_analysis.md
    Given la caché Redis que usa el hash de (Prompt + Diccionario) como clave (CA-02)
    Then cuando un Arquitecto modifique el diccionario Zod de un formulario en la Pantalla 7 (US-003) — ya sea agregando, eliminando o renombrando una variable —, el Backend de formularios DEBE publicar un evento de dominio `FORM_SCHEMA_CHANGED` (vía RabbitMQ o evento interno).
    And el servicio DMN del Backend escuchará este evento y ejecutará una invalidación quirúrgica: purgará de Redis ÚNICAMENTE las entradas de caché cuyos hashes incluyan el `form_id` del formulario modificado.
    And NO se invalida toda la caché Redis (eso sería un nuke innecesario), solo las entradas vinculadas al diccionario que cambió.
    And al siguiente request del Arquitecto con el mismo Prompt, el sistema generará una DMN nueva con la IA usando el diccionario actualizado, y la cacheará con el nuevo hash.

  Scenario: [REMEDIACIÓN] Catálogo y Explorador de Tablas DMN (DMN Library Dashboard) (CA-17)
    # Origen: REM-007-05 — GAP-5 del us007_functional_analysis.md
    # Cierra el déficit estructural de gobernanza de artefactos respecto a US-003 (CA-86) y US-005 (CA-23).
    Given la necesidad del Arquitecto de buscar, re-editar o consultar versiones de tablas DMN existentes
    When el usuario ingresa al módulo DMN (Pantalla 4 Principal)
    Then EL SISTEMA NO CARGARÁ el chat NLP en blanco directamente, sino que presentará un "Catálogo o Grilla de Tablas DMN".
    And esta Grilla incluirá un Buscador `Server-side` para buscar por Nombre de Negocio o Decision_Ref (ID Técnico).
    And cada fila o tarjeta mostrará:
    And - Nombre de la Tabla (Ej: "Matriz de Riesgo Crediticio")
    And - Decision_Ref (Ej: `decision_risk_matrix`)
    And - Versión Activa (Ej: `v3`)
    And - Estado: "📝 BORRADOR" / "✅ ACTIVA" / "📦 ARCHIVADA"
    And - Fecha de Última Modificación y Autor
    And - Cantidad de filas de la tabla (Ej: "12 reglas")
    And al hacer clic sobre una DMN, se abrirá en el Editor/Chat NLP para su edición o consulta.
    And existirá un botón [📦 Archivar] que solo se habilitará si NO existen Business Rule Tasks activas en BPMN desplegados que referencien esa Decision_Ref.
    And el Backend expondrá el endpoint `GET /api/v1/dmn?status=ACTIVE&search=riesgo&page=1&size=20` con paginación server-side.

  Scenario: [REMEDIACIÓN] Contrato API Estandarizado para el Ciclo de Vida DMN (CA-18)
    # Origen: REM-007-06 — GAP-6 del us007_functional_analysis.md
    Given la necesidad de alinear Frontend y Backend en el contrato REST del módulo DMN
    Then el Backend expondrá los siguientes endpoints documentados con OpenAPI/Swagger annotations:
    And `POST /api/v1/dmn` — Crear nueva DMN (body: `{name, decision_ref, source: "NLP"|"XML_UPLOAD", prompt?}`) → Retorna `201 Created` con `{id, version, status: "DRAFT"}`.
    And `GET /api/v1/dmn` — Listar DMNs con filtros (query params: `status`, `search`, `page`, `size`) → Retorna lista paginada para el Catálogo (CA-17).
    And `GET /api/v1/dmn/{id}` — Obtener detalle completo de una DMN (XML, metadatos, historial de versiones).
    And `PUT /api/v1/dmn/{id}` — Actualizar DMN → genera V2 obligatoriamente (consistente con CA-06). Retorna `201 Created` con nueva versión.
    And `POST /api/v1/dmn/{id}/publish` — Publicar/Aprobar → commit al motor Camunda + warm-up cache (CA-03). Cambia status a "ACTIVE". Requiere confirmación `CONFIRMO_V{N}` (CA-12).
    And `POST /api/v1/dmn/{id}/rollback` — Rollback: crea una nueva versión que es copia de la versión anterior (CA-12).
    And `POST /api/v1/dmn/{id}/evaluate-test` — Simulador de decisiones (CA-15).
    And `POST /api/v1/dmn/drafts` — Crear/actualizar borrador temporal (CA-13).
    And `DELETE /api/v1/dmn/drafts/{id}` — Purgar borrador manualmente.
    And `POST /api/v1/dmn/{id}/archive` — Archivar DMN sin referencias activas (CA-17).


  # ==============================================================================
  # F. REFINAMIENTO FUNCIONAL POST-CUESTIONARIO (2026-04-05)
  # Origen: Cuestionario de 45 preguntas del workflow /refinamientoFuncionalUs.md
  # Propósito: Cerrar huecos descubiertos durante el refinamiento de la US-007.
  # ==============================================================================

  Scenario: [REFINAMIENTO] Resiliencia SSE ante Desconexiones Parciales (CA-19)
    # Origen: Pregunta #2 del Refinamiento Funcional
    # Resuelve: ¿Qué pasa si la conexión se corta a mitad de la generación de la tabla?
    Given que el canal SSE (CA-01) está emitiendo filas de la tabla DMN al Frontend en tiempo real
    When la conexión SSE se interrumpe inesperadamente (pérdida de red, cierre de pestaña, timeout del proxy)
    Then el Frontend preservará las filas parcialmente recibidas como un borrador incompleto visible en la grilla con un indicador visual "⚠️ Generación Interrumpida (12 de 30 filas recibidas)".
    And mostrará un botón `[🔄 Reintentar Generación]` que re-enviará el mismo prompt al Backend.
    And si el hash del prompt existe en caché Redis (CA-02), el Backend devolverá la tabla completa instantáneamente sin costo LLM adicional.
    And si NO existe en caché, el Backend iniciará una nueva generación SSE completa (no parcial).
    And las filas parciales anteriores se destruirán del DOM al recibir la primera fila de la nueva generación.

  Scenario: [REFINAMIENTO] Normalización del Prompt para Caché Inteligente (CA-20)
    # Origen: Pregunta #3 del Refinamiento Funcional
    # Resuelve: Dos prompts idénticos con diferente capitalización que pagan doble a la IA.
    Given el cálculo del hash de caché basado en (Prompt + Diccionario) del CA-02
    Then el Backend NORMALIZARÁ el prompt antes de calcular el hash, aplicando las siguientes transformaciones:
    And 1. Conversión a minúsculas (lowercase).
    And 2. Eliminación de espacios duplicados y espacios al inicio/final (trim + collapse).
    And 3. Eliminación de signos de puntuación irrelevantes (puntos finales, comas sueltas).
    And como resultado, los prompts "Aprobar si MONTO < 1000" y "aprobar si monto < 1000" producirán el MISMO hash y servirán la MISMA tabla cacheada, evitando costos LLM duplicados.

  Scenario: [REFINAMIENTO] Validación Post-Minificación del XML DMN (CA-21)
    # Origen: Pregunta #5 del Refinamiento Funcional
    # Resuelve: El riesgo de que la compresión XML (CA-03) corrompa el documento.
    Given el proceso de XML Minification del CA-03 que elimina espacios en blanco inútiles antes del COMMIT
    Then INMEDIATAMENTE DESPUÉS de la minificación, el Backend ejecutará un parse de validación del XML resultante contra el schema DMN de Camunda.
    And si el parse falla (XML corrupto o estructura inválida), el Backend CANCELARÁ la minificación y persistirá el XML ORIGINAL sin comprimir, registrando un WARNING en los logs: "Minificación abortada por riesgo de corrupción. Guardando XML original."
    And NUNCA se hará COMMIT de un XML minificado que no haya superado la validación de parseo.

  Scenario: [REFINAMIENTO] Rechazo de XML Upload con Hit Policy No Autorizada (CA-22)
    # Origen: Pregunta #7 del Refinamiento Funcional
    # Resuelve: El Modo Desarrollador acepta XMLs con Hit Policy diferente a FIRST, causando errores en runtime.
    Given la carga manual de un archivo XML DMN en Modo Desarrollador (CA-09)
    When el Backend recibe el XML subido por el usuario
    Then el Backend parseará el XML y verificará que el atributo `hitPolicy` de la etiqueta `<decisionTable>` sea estrictamente `FIRST`.
    And si el XML contiene una Hit Policy diferente (UNIQUE, COLLECT, RULE ORDER, OUTPUT ORDER, ANY), el Backend rechazará la carga con HTTP `422 Unprocessable Entity` y el mensaje: "La tabla DMN que subió usa la política de evaluación '{hitPolicy}', pero el sistema solo permite la política FIRST en la Versión 1. Por favor modifique su archivo y vuelva a intentarlo."
    And si el XML no contiene el atributo `hitPolicy`, el Backend lo inyectará automáticamente como `FIRST` antes de persistir.

  Scenario: [REFINAMIENTO] Rate Limiting Independiente para el Simulador de Decisiones (CA-23)
    # Origen: Pregunta #28 del Refinamiento Funcional
    # Resuelve: El endpoint evaluate-test (CA-15) no tiene Rate Limiting propio, permitiendo abuso contra el motor Camunda.
    Given el endpoint `POST /api/v1/dmn/{id}/evaluate-test` del Simulador de Decisiones (CA-15)
    Then el API Gateway impondrá un Rate Limiting independiente al del CA-02 (generación IA):
    And máximo 20 evaluaciones de prueba por minuto por usuario autenticado.
    And si se excede, el Backend retornará HTTP `429 Too Many Requests` con un mensaje amigable: "Has realizado demasiadas pruebas seguidas. Espera {remainingSeconds} segundos antes de probar nuevamente."
    And este límite es independiente del Rate Limiting de generación IA (CA-02) porque protege un recurso diferente (el motor Camunda de evaluación, no la API del LLM).

  Scenario: [REFINAMIENTO] Buscador In-App para Grilla DMN con Virtual Scrolling (CA-24)
    # Origen: Pregunta #34 del Refinamiento Funcional
    # Resuelve: Ctrl+F del navegador no encuentra texto en filas fuera del viewport cuando se usa Virtual Scrolling (CA-10).
    Given la grilla DMN con Virtual Scrolling activo (CA-10) donde solo las filas visibles están renderizadas en el DOM
    Then la grilla incorporará un buscador integrado activable con el atajo `Ctrl+F` (interceptando el evento nativo del navegador) o mediante un ícono de búsqueda `[🔍]` visible en la barra de herramientas de la grilla.
    And el buscador buscará en TODAS las filas de la tabla (incluyendo las no renderizadas en el viewport), resaltando en amarillo las coincidencias y navegando automáticamente (scroll) hasta la primera coincidencia.
    And soportará navegación entre resultados con botones `[↑ Anterior]` y `[↓ Siguiente]`.

  Scenario: [REFINAMIENTO] Timeout y SLA de Tiempo de Respuesta para Generación (CA-25)
    # Origen: Pregunta #41 del Refinamiento Funcional
    # Resuelve: No había un tiempo máximo definido para la generación SSE, dejando al usuario esperando indefinidamente.
    Given el envío de un prompt de generación DMN al Backend vía SSE (CA-01)
    Then el Frontend establecerá un timeout global de 30 segundos para la conexión SSE.
    And si transcurren más de 30 segundos sin recibir NINGUNA fila (ni siquiera la primera), el Frontend cerrará la conexión SSE y mostrará: "La generación tardó más de lo esperado. Esto puede ocurrir con políticas muy complejas. Pulse [🔄 Reintentar] para intentarlo nuevamente."
    And como referencia de rendimiento, el Time To First Row (tiempo desde el envío del prompt hasta la primera fila visible en la grilla) deberá ser inferior a 8 segundos bajo condiciones normales de red y carga.
    And si la generación ya comenzó (al menos 1 fila recibida) pero deja de emitir filas por más de 15 segundos consecutivos (stall), el Frontend activará el mecanismo de resiliencia del CA-19 (borrador parcial + reintentar).


  # ==============================================================================
  # G. REFINAMIENTO MODO MANUAL (AGNÓSTICO A IA)
  # Origen: Refinamiento con el PO (2026-05-02)
  # Propósito: Formalizar la alternativa de creación 100% humana (Sin IA).
  # ==============================================================================

  Scenario: [REFINAMIENTO MODO MANUAL] Coexistencia UI del Chat NLP y Grilla Visual (CA-26)
    Given que el Arquitecto ingresa a la Pantalla 4 para crear una tabla desde cero sin IA
    When despliega la grilla vacía para inicio manual
    Then el panel del Chat NLP (Generador Cognitivo) permanecerá abierto y visible en la interfaz.
    And el usuario podrá alternar entre escritura manual en la grilla y pedir sugerencias a la IA sin que un modo bloquee al otro.

  Scenario: [REFINAMIENTO MODO MANUAL] Binding Obligatorio con Diccionario Zod (CA-27)
    Given la necesidad de definir Columnas de Entrada (Inputs) en la grilla vacía
    When el usuario intenta agregar una nueva condición a la cabecera de la tabla
    Then el sistema NO permitirá escribir nombres de variables libres en texto crudo.
    And desplegará un Dropdown obligatorio que consuma el Diccionario de Datos Zod (US-003).
    And el usuario deberá arrastrar o seleccionar las variables pre-existentes para garantizar la integridad referencial del proceso.

  Scenario: [REFINAMIENTO MODO MANUAL] Validación de Sintaxis FEEL en Tiempo Real (CA-28)
    Given que el usuario digita reglas matemáticas o condicionales manualmente en las celdas (ej. `< 1000` o `"Aprobado"`)
    When el usuario interactúa con la grilla
    Then el Frontend ejecutará un motor ligero de validación de sintaxis FEEL en tiempo real.
    And si detecta un error de sintaxis (ej. caracteres no permitidos o tipo de dato inconsistente con Zod), marcará la celda inmediatamente con un borde rojo y un tooltip explicativo.
    And bloqueará el botón de [Guardar/Publicar] hasta que la celda sea corregida.

  Scenario: [REFINAMIENTO MODO MANUAL] Inyección Automática de Candado Catch-All (CA-29)
    Given la regla matemática estricta de Hit Policy FIRST (CA-07)
    When el usuario está construyendo la tabla manualmente fila por fila
    Then el Frontend inyectará y mantendrá automáticamente una fila final inamovible (Catch-All) con el candado 🔒.
    And el usuario NO podrá eliminar esta fila, asegurando que cualquier escenario no contemplado en las reglas superiores derive por defecto a un estado seguro (Ej: "Revisión Humana").

  Scenario: [REFINAMIENTO MODO MANUAL] Edición Posterior de Cargas XML (CA-30)
    Given el "Modo Desarrollador" que permite cargar archivos `.dmn` nativos (CA-09, CA-22)
    When el archivo XML es procesado, validado y cargado exitosamente en la plataforma
    Then la tabla resultante SERÁ totalmente editable dentro de la grilla visual.
    And el usuario podrá modificar celdas, agregar filas o corregir lógicas usando la interfaz estándar sin necesidad de resubir el XML en cada iteración.

  Scenario: [REFINAMIENTO MODO MANUAL] Límite de Capacidad Manual de SRE (CA-31)
    Given la salud del motor de evaluación Camunda y el rendimiento del Virtual Scrolling
    When el usuario añade filas manualmente a la tabla DMN
    Then el Frontend impondrá un Hard-Stop paramétrico de máximo 100 filas permitidas (superior a las 50 de la IA, pero acotado).
    And si el usuario intenta agregar la fila 101, el sistema deshabilitará el botón "+" y mostrará una advertencia de tope arquitectónico por salud de SRE.

  Scenario: [REFINAMIENTO MODO MANUAL] Trazabilidad y Versionamiento de Intervención Humana (CA-32)
    Given una tabla V1 originaria que fue generada íntegramente por la IA
    When un Arquitecto entra a la grilla y realiza modificaciones manuales (cambia un valor, borra una fila, etc.) y hace clic en [Publicar]
    Then el Backend incrementará la versión a V2 obligatoriamente.
    And en el log de auditoría y en el Catálogo DMN (CA-17), esta versión quedará etiquetada estrictamente con el badge visual "Modificada Manualmente", perdiendo el sello de pureza "100% IA".```
**Trazabilidad UX:** Wireframes Pantalla 4 (Taller DMN) y su invocación desde Pantalla 6 (Diseñador BPMN).

---

