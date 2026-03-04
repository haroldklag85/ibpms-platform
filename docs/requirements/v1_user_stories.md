# Historias de Usuario (BDD / Gherkin) - iBPMS V1

Este documento contiene las Historias de Usuario formales para el MVP Táctico (V1). Todas las historias aquí redactadas se restringen **estrictamente** al bloque "MUST HAVE" y "SHOULD HAVE" definido en `v1_moscow_scope_validation.md`. 
*Cualquier funcionalidad relacionada con IA Agentic o Módulos Verticales (RAG, Scraping, OCR) queda explícitamente fuera de esta versión.*

---

## ÉPICA 1: Orquestación y Workbenches (El Motor Core)
Esta épica aborda la capacidad fundamental del sistema: recibir un requerimiento, enrutarlo como una tarea (Task) y permitir que el usuario la gestione en su Workdesk (Escritorio de Tareas).

### US-001: Obtener Tareas Pendientes en el Workdesk
**Como** Analista / Usuario de Negocio
**Quiero** visualizar una lista consolidada de mis tareas pendientes (BPMN o Kanban) al ingresar a la plataforma (Workdesk)
**Para** saber exactamente qué gestiones operativas debo priorizar y resolver hoy.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Workdesk Loading
  Scenario: Un usuario autenticado solicita sus tareas pendientes
    Given que el usuario "juan.perez" ha iniciado sesión exitosamente con el rol "Analista_Legal"
    And existen 3 tareas activas asignadas a él y 2 tareas asignadas al grupo "Analista_Legal" en la base de datos
    When el cliente frontend realiza una petición GET a "/api/v1/workbox/pending?page=1&size=50&sort=sla_asc"
    Then el sistema debe retornar un HTTP STATUS 200 OK
    And el payload JSON debe contener un arreglo de objetos tipo "Task" bajo el nodo "content" (Paginación Spring)
    And cada objeto "Task" debe incluir "id" (UUID), "titulo" (String), "fecha_vencimiento_sla" (ISO-8601), "origen" (BPMN o Kanban) y "estado"
```
**Trazabilidad UX:** Wireframes Pantalla 1 (Workdesk - Escritorio de Tareas).

---

### US-002: Reclamar una Tarea de Grupo (Claim Task)
**Como** Analista / Usuario de Negocio
**Quiero** poder "reclamar" (asignarme) una tarea que actualmente pertenece a la cola de todo mi grupo
**Para** evitar que otro compañero trabaje en el mismo caso de forma paralela y duplicar esfuerzos.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Task Claiming
  Scenario: Reclamar una tarea exitosamente
    Given una tarea con ID "TK-099" cuyo estado es "UNASSIGNED" y pertenece al grupo "Soporte_IT"
    And el usuario "maria.lopez" pertenece al grupo "Soporte_IT"
    When "maria.lopez" realiza un POST a "/api/v1/workbox/tasks/TK-099/claim"
    Then el sistema debe retornar un HTTP STATUS 200 OK
    And el "assignee" de la tarea debe actualizarse a "maria.lopez" en la tabla 'ACT_RU_TASK' (Camunda DB)
    And la tarea debe desaparecer de la vista "Tareas de Grupo" de los demás usuarios instantáneamente (WebSockets/Polling)

  Scenario: Intento de reclamar una tarea ya asignada a otro (Condición de Carrera)
    Given la tarea "TK-099" ya fue reclamada por un colega 1 segundo antes
    When "maria.lopez" realiza un POST a "/api/v1/workbox/tasks/TK-099/claim"
    Then el sistema debe retornar un HTTP STATUS 409 Conflict
    And el mensaje de error debe indicar "La tarea ya fue asignada a otro usuario."

  Scenario: Liberar una tarea reclamada (Unclaim / CA-3)
    Given el usuario "maria.lopez" tiene asignada la tarea "TK-099" en la vista de "Mis Tareas"
    And ha diligenciado parcialmente el formulario (Borrador/Draft)
    When oprime el botón [ Liberar Tarea ]
    And opcionalmente rellena el campo "Motivo de Liberación"
    Then el sistema ejecuta el endpoint `/unclaim` enviando el motivo opcional y el payload actual
    And la tarea actualiza su `assignee` a null en Camunda pero conserva las variables como "Draft"
    And reaparece de inmediato en la "Cola Grupal" para los demás compañeros, quienes verán el progreso parcial.
    And el sistema guarda un log de auditoría (oculto en BD para métricas BAM) indicando la devolución.

  Scenario: Reasignar o Escalar una tarea (Reassign / CA-4)
    Given el usuario "maria.lopez" tiene asignada la tarea "TK-099"
    When oprime el botón [ Reasignar ]
    Then el Modal de reasignación debe listar compañeros de su mismo grupo Y usuarios de niveles superiores (Escalamiento) basados en la Jerarquía definida en el BPMN Modeler (Pantalla 6).
    When selecciona a "carlos.gerente", proporciona un motivo y confirma
    Then el sistema verifica la configuración "Ping-Pong Limit" (Ej. N=1 reasignaciones permitidas). Si se supera, bloquea la acción.
    And si es válida, ejecuta el endpoint `/reassign` configurando a "carlos.gerente" como nuevo `assignee`
    And el sistema guarda el log de auditoría para trazabilidad BAM.
    And NO emite ninguna notificación por correo electrónico, recargándose silenciosamente en el Workdesk del nuevo dueño.
```
**Trazabilidad UX:** Wireframes Pantalla 1 (Botón: Asignarme Tarea / Claim).

---

## ÉPICA 2: IDE Web Pro-Code para Formularios (Vue 3, Zod & Dual-Pattern)
Aborda la capacidad para diseñar interfaces de usuario mediante herramientas visuales que, por debajo, compilan archivos `.vue` y esquemas de validación estrictos en lugar de JSON interpretado. También exige elegir el patrón de diseño arquitectónico del formulario.

### US-003: Instanciar y Generar un Formulario "iForm Maestro" vs "Simple"
**Como** Arquitecto Frontend / Administrador
**Quiero** elegir el tipo de formulario y ver cómo el sistema genera código Vue 3 y Zod en tiempo real mientras arrastro componentes
**Para** no tener deudas técnicas (Vendor Lock-in) y construir expedientes (iForm Maestros) que manejen etapas dinámicamente.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Web IDE Form Code Generation
  Scenario: Seleccionar Patrón de Formulario
    Given que el desarrollador crea un nuevo recurso en la sección "Formularios"
    When el modal pregunta "¿Qué arquitectura de formulario desea?"
    Then el usuario puede elegir "Patrón B: iForm Maestro (Expediente Multi-Etapa)"
    And el lienzo visual se estructura para basar el renderizado en la variable "Current_Stage" de Camunda

  Scenario: Análisis Bidireccional de Código en Tiempo Real
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

  Scenario: Permisos de Sobrescritura en Campos (CA-4)
    Given un usuario en la Etapa 2 abre un iForm Maestro
    Then puede sobrescribir los valores ingresados previamente en la Etapa 1
    But solo si su Rol RBAC tiene permisos explícitos de escritura sobre esos campos, de lo contrario se renderizan como "Solo Lectura".

  Scenario: Enrutador de Archivos Adjuntos por TRD (CA-5)
    Given un usuario sube un archivo en un componente de Adjuntos del formulario
    Then el sistema lee la configuración de las Tablas de Retención Documental (TRD) del proceso
    And rutea el archivo automáticamente a la Bóveda SGDEA Interna (Pantalla 12) o a Microsoft SharePoint según indique la TRD
    And NO se guarda en la Base de Datos transaccional (Diferido a V2).

  Scenario: Validación Proactiva de Zod (CA-6)
    Given un usuario final está diligenciando el formulario en su Workdesk
    When incumple una regla de validación (Ej: escribe 3 números en un campo que exige 10)
    Then el formulario muestra el mensaje de error "en vivo" proactivamente mientras teclea, sin esperar al botón de [Enviar].

  Scenario: Estilos CSS Corporativos Estandarizados V1 (CA-7)
    Given el Arquitecto diseña un formulario
    Then todos los componentes visuales heredan la hoja de estilos de "Marca Corporativa" global
    And NO es posible customizar el color/fuente de cada botón individualmente en V1 para asegurar consistencia (Diferido a V2).

  Scenario: Auto-Guardado de Borrador en Workdesk (CA-8)
    Given un usuario final está llenando un formulario extenso en la Pantalla 2
    Then cada interacción se guarda automáticamente como un borrador en caché local (o BD temporal)
    And si el usuario cierra la pestaña por error, al volver a abrir la tarea, recupera los datos ingresados no enviados.

  Scenario: Reglas de Visibilidad Condicional (CA-9)
    Given el Arquitecto configura la propiedad "Dependencia Visual" de un campo B
    When en el lienzo visual el usuario final marca un Checkbox A
    Then el campo B aparece dinámicamente ("Campo Fantasma") empujando el resto de la estructura hacia abajo (layout reactivo Vue).

  Scenario: Prevención Contra Borrado de Formularios Activos (CA-10)
    Given el Arquitecto intenta eliminar el "Form_Solicitud_V1" en la Pantalla 7
    When el sistema detecta que existen instancias de procesos "en vuelo" que requieren de este formulario
    Then se cancela la eliminación y se muestra un mensaje de Error: "Prohibido: Este formulario está siendo usado por N procesos activos."

  Scenario: Control de Versiones de Diseño de Formulario (CA-11)
    Given el Arquitecto modifica un formulario guardado
    Then al presionar guardar, el IDE genera una nueva versión inmutable (v2) del `.vue`/`JSON`
    And permite consultar y restaurar versiones anteriores en caso de daño en el diseño.

  Scenario: Bitácora de Auditoría a Nivel de Campo (CA-12)
    Given el usuario "maria.lopez" sobrescribe un valor que había puesto "juan.perez" en una etapa previa
    Then el backend inserta un registro en una tabla de auditoría (Ej: FormFieldValueAudit)
    And un Revisor puede ver un panel flotante "Bitácora" que lista "María cambió 'Costo' de 100 a 150 a las 14:00".

  Scenario: Dropdown Alimentado por Exportación CSV (CA-13)
    Given el Arquitecto agrega un componente Dropdown (Select) al Lienzo
    Then en el panel de propiedades tiene la opción de "Cargar archivo .CSV"
    And al subir el archivo, el Dropdown se puebla automáticamente con las opciones (Ej: Países, Áreas, Tipos de Documento) en lugar de tipearlas una a una.

  Scenario: Autocompletado mediante Integración API / BD Externa (CA-14)
    Given el Arquitecto diseña un formulario en la Pantalla 7
    When configura un campo (Ej: "Cédula") para que sea el gatillo (trigger) de una consulta externa
    Then puede vincular ese campo a un Endpoint del Hub (Pantalla 11) o a datos de otros procesos
    And al usuario final tipear la cédula y perder el foco (blur), el formulario autocompleta los campos destino (Ej: "Nombre", "Dirección") automáticamente.

  Scenario: Componente de Firma Electrónica Manuscrita (CA-15)
    Given el Arquitecto requiere formalizar un acuerdo en el formulario
    Then puede arrastrar un componente de "Firma a Mano Alzada" (Canvas HTML5) al Lienzo
    And el usuario final puede dibujar su firma con el mouse o pantalla táctil
    And el sistema guarda la firma como una imagen (Ej: Base64/PNG) anexa al Payload del formulario.

  Scenario: Validaciones Cruzadas entre Múltiples Campos (CA-16)
    Given un formulario tiene un componente "Fecha de Inicio" y "Fecha de Fin"
    When el usuario final ingresa una "Fecha de Fin" que es anterior a la "Fecha de Inicio"
    Then el esquema Zod dinámico evalúa la regla cruzada (refinement)
    And muestra inmediatamente un mensaje de error impidiendo el avance, indicando la inconsistencia temporal.

  Scenario: Exportación a PDF del Formulario Diligenciado (CA-17)
    Given un usuario final ha completado de llenar los datos requeridos en pantalla
    Then dispone de un botón global estilo "[⬇️ Exportar a PDF]"
    And al presionarlo, el sistema genera y descarga un PDF con formato de "Documento Físico" que contiene todos los campos y valores renderizados de manera limpia para impresión.

  Scenario: Grupos de Campos Repetibles (Data Grids / Tablas) (CA-18)
    Given el Arquitecto necesita recopilar una lista de longitud variable (Ej: "Múltiples Co-Deudores")
    Then puede utilizar un componente de "Grupo Repetible" (Field Array)
    And el usuario final verá un botón "[+ Agregar]" para duplicar dinámicamente el conjunto de campos configurados sin afectar el esquema Zod subyacente.

  Scenario: Ayudantes Locales (Tooltips y Placeholders) (CA-19)
    Given el Arquitecto configura un campo complejo en el Lienzo
    Then puede configurar un texto "Placeholder" (texto gris de fondo)
    And puede configurar un "Tooltip" (icono ℹ️ que al hacer hover muestra una descripción detallada)
    And el Arquitecto es libre de usar ambos mecanismos simultáneamente para guiar al usuario final.

  Scenario: Máscaras de Entrada (Input Masks) para Formatos Específicos (CA-20)
    Given el Arquitecto configura un campo numérico como "Ingresos Brutos" o "Cédula"
    Then puede aplicarle una Máscara de Formato (Ej: Moneda, Teléfono, Fecha)
    And mientras el usuario final teclea (Ej: "150000"), el sistema formatea visualmente el valor en vivo (Ej: "$ 150.000,00") sin alterar el valor numérico real bajo el capó.

  Scenario: Visor Histórico Inmutable para Auditoría (CA-21)
    Given un usuario Auditor accede a un proceso completado hace años para revisión
    Then el sistema renderiza el formulario con su diseño original exacto
    But todos los componentes están estrictamente en modo "Solo Lectura", sin botón de [Enviar] y congelados contra cualquier manipulación.

  Scenario: Restricciones de Longitud Dinámicas (Zod min/max) (CA-22)
    Given el Arquitecto configura un campo de texto largo (Textarea)
    Then puede definir en el panel de propiedades "Caracteres Mínimos" y "Máximos"
    And el lienzo genera instantáneamente la regla Zod correspondiente (Ej: `z.string().min(5).max(100)`)
    And bloquea el input visualmente cuando el usuario alcanza el límite.

  Scenario: Condicionamiento de Archivos Adjuntos (CA-23)
    Given el Arquitecto agrega un componente de "Subida de Archivos"
    Then el panel de propiedades debe permitir restringir el "Peso Máximo (MB)" y los "Tipos Permitidos (Ej: .pdf, .jpg, .xml)"
    And si el usuario intenta subir un archivo no permitido, el sistema lo rechaza proactivamente antes de enviarlo al servidor.

  Scenario: Dropdown de Búsqueda Interactiva (Searchable Select) (CA-24)
    Given el Arquitecto necesita presentar una lista extensa de opciones (Ej: 195 Países)
    Then el componente Dropdown (Select) debe incluir por defecto un motor de búsqueda interno (Typeahead)
    And permite al usuario teclear para filtrar la lista instantáneamente sin tener que usar el scroll manual.

  Scenario: Restricciones en Grillas Repetibles (Min/Max Rows) (CA-25)
    Given el Arquitecto utiliza un Data Grid (Grupo Repetible)
    Then puede configurar mediante el panel de propiedades cuántas filas como mínimo debe llenar el usuario, y un tope máximo (Ej: Min: 1, Max: 3)
    And el esquema Zod asegura que el arreglo (`z.array`) cumpla estas restricciones bloqueando el botón [+ Agregar] al llegar al límite.

  Scenario: Soporte Multi-Idioma (i18n) (CA-26 - Diferido a V2)
    # NOTA: Diferido a V2. Para V1 todos los formularios son creados y operados estáticamente en Español.
    Given el Arquitecto diseña un formulario
    Then puede habilitar soporte multi-idioma para cambiar el idioma condicionalmente.

  Scenario: Data Binding (Precarga Automática desde Camunda) (CA-27)
    Given un usuario "Carlos" tiene variables persistidas de etapas anteriores almacenadas en el proceso de Camunda
    Then el IDE de Formularios mapea automáticamente todas las variables (variables del motor) usando sus IDs Técnicos
    And cuando el usuario abre la Pantalla 2, los campos coincidentes se auto-rellenan con esos datos históricos almacenados.

  Scenario: Componentes de Calendario y Rangos de Fechas (CA-28 - Rango Diferido V2)
    Given el Arquitecto necesita recopilar fechas
    Then dispone de un componente DatePicker estándar (Selección de un solo día) en V1.
    # NOTA: La selección compleja de "Rango de Fechas" (Drag and Drop en calendario) se difiere a V2.

  Scenario: Multi-Select Visual (Pastillas/Etiquetas) (CA-29)
    Given el Arquitecto configura un campo desplegable que permite selección múltiple
    Then el diseñador o el panel de propiedades permite elegir la presentación visual: "Chips/Etiquetas" o "Pastillas"
    And el usuario final puede eliminar selecciones individuales haciendo clic en la 'x' de la pastilla correspondiente.

  Scenario: Sello Visual de Aprobatoria con Rol (CA-30)
    Given un usuario con Rol "Gerente" completa una etapa de revisión en un iForm Maestro
    Then esa etapa genera un "Badge/Sello" visual estático incrustado
    And dicho sello muestra el Nombre del Usuario y su Rol (Ej: "Aprobado por Juan Pérez - Gerente de Área") para visibilidad en etapas subsecuentes.

  Scenario: Campos Ocultos (Hidden Inputs) para Metadata (CA-31)
    Given el Arquitecto necesita enviar datos técnicos que el usuario NO debe ver ni alterar
    Then puede arrastrar un componente "Campo Oculto (Hidden Input)" al Lienzo
    And puede asignarle valores variables (Ej: `sys_request_id`)
    And esos datos viajan transparentemente en el Payload JSON final al enviarse la tarea.

  Scenario: Validaciones Condicionales (Required-If) (CA-32)
    Given el Arquitecto configura la propiedad "Requerido Condicional" del Campo B
    When en el lienzo el usuario final marca "Sí" en el Campo A
    Then el esquema Zod dinámico hace que el Campo B se vuelva obligatorio
    And si marca "No", el Campo B es opcional y no bloquea el envío del formulario.

  Scenario: Restricción de Cantidad Mínima y Máxima de Adjuntos (CA-33)
    Given el Arquitecto agrega un componente de "Subida de Archivos"
    Then puede habilitar en las propiedades un requerimiento de Volumen (Ej: Mínimo 2 archivos, Máximo 5)
    And el sistema previene el envío del formulario si no se cumple esta cuota exacta.

  Scenario: Traducción Silenciosa de Formatos (Mascara Front vs Dato Back) (CA-34)
    Given el usuario final digita "1.500.230" en un input numérico con máscara visual
    When el formulario se procesa para hacer el POST al motor de tareas (Camunda)
    Then el IDE despoja el formato estético en secreto y envía el Integer/Float puro (`1500230`)
    And garantizando la integridad de los datos para la analítica y reglas de negocio.

  Scenario: Grillas Editables con Protección y Auditoría Parcial (CA-35)
    Given un usuario "Analista 2" requiere agregar filas a un Data Grid donde "Analista 1" ya insertó datos
    Then el Analista 2 puede visualizar y editar toda la grilla si tiene permisos
    And cualquier fila modificada o eliminada que perteneciera al Analista 1 dejará un rastro en la Bitácora de Auditoría (CA-12).

  Scenario: Feedback Visual en Llamadas a APIs (Estado Indeterminado) (CA-36)
    Given el usuario final ingresa un dato en un campo que dispara una llamada de Autocompletado (CA-14)
    When la interconexión con el sistema externo está procesándose
    Then el botón global de [Enviar Formulario] se deshabilita temporalmente
    And muestra un indicador de carga (spinner), evitando envíos prematuros o datos rotos.

  Scenario: Enmascaramiento de Inputs de Múltiple Tipo (Contraseñas / Sensibles) (CA-37)
    Given el Arquitecto requiere capturar información sensible (Ej: APIs Keys, Claves)
    Then dispone del tipo de Campo "Contraseña (Password)"
    And el texto digitado por el usuario final se oculta inmediatamente bajo asteriscos (****).

  Scenario: Limpieza Automática por Lógica Condicional (CA-38)
    Given un campo B es dependiente de que el campo A tenga el valor "X"
    When el usuario final había llenado el campo B, pero decide cambiar el campo A al valor "Y"
    Then el campo B desaparece visualmente (CA-9) Y suelta (limpia null/undefined) los datos almacenados
    And evitando que viajen datos "fantasma" al motor asociados a ramas muertas del formulario.

  Scenario: Grillas y Organización Multicolumna (Layouts) (CA-39)
    Given el Arquitecto está diseñando la distribución espacial del formulario
    Then puede arrastrar y soltar componentes "Lado a Lado" organizándolos en múltiples columnas (Ej: 2, 3 o 4 columnas)
    And este layout es renderizado mediante CSS Grid / Flexbox de Tailwind en el `.vue` final adaptándose al espacio del Workdesk.

  Scenario: Vista de Imprimible y de Solo-Lectura Plana (View-Mode) (CA-40)
    Given un usuario que tiene el rol de "Visualizador" (Solo aprobar, no rellenar datos) abre la tarea
    Then el sistema NO le muestra un formulario lleno de Inputs deshabilitados y grises
    And le renderiza un componente de "Vista de Lectura / Print-Friendly" donde los datos parecen un documento de texto limpio sin bordes de formulario interactivo.

  Scenario: Candado de Solo-Lectura Basado en Fórmulas (CA-41)
    Given el Arquitecto configura un campo B que depende del valor de un campo A
    Then puede usar el panel de propiedades para establecer una "Fórmula de Bloqueo" (Ej: `if A == 'Extranjero' then disable B`)
    And el motor Zod / Vue deshabilita visualmente (Solo-Lectura) el campo B en tiempo real cuando se cumple la condición.

  Scenario: Cronómetro de Productividad en Formulario (Timer Component) (CA-42)
    Given el Arquitecto necesita medir Tiempos y Movimientos de los empleados
    Then dispone de un componente "Timer / Cronómetro" que ofrece tres modos de configuración:
    And 1. Cronómetro Activo a Demanda (Con botones de Play/Pausa/Reset manuales).
    And 2. Cronómetro en Segundo Plano (Mide el tiempo exacto que la ventana del formulario estuvo en foco).
    And 3. Cronómetro Sincronizado por API (Conectado a un sistema externo de Time-Tracking).
    And el resultado viaja en los metadatos globales del Payload final.

  Scenario: Botón de Reset Dual-Verification (CA-43)
    Given el Arquitecto agrega un botón "Restablecer Formulario"
    When el usuario final lo oprime por error o a propósito
    Then el sistema debe exigir una "Doble Verificación" (Modal de confirmación: "¿Está seguro que desea borrar todos los datos ingresados?")
    And solo si se confirma, el estado reactivo del componente se limpia a cero.

  Scenario: Arrastrar y Soltar (Drag & Drop) Expandido para Adjuntos (CA-44)
    Given el formulario contiene un componente de Subir Archivos
    Then el usuario no está obligado a usar el botón táctil "Buscar Archivo"
    And puede arrastrar múltiples archivos simultáneamente desde su escritorio / SO y soltarlos sobre la zona definida en pantalla para iniciar la carga (Dropzone).

  Scenario: Captura de Geolocalización (GPS) Embebida (CA-45)
    Given el Arquitecto diseña un formulario para trabajadores en terreno
    Then puede arrastrar un componente "Captura GPS" (Obtener Ubicación)
    And cuando el usuario lo presiona, el navegador solicita permiso y captura las coordenadas (Latitud / Longitud) precisas integrándolas automáticamente al esquema.

  Scenario: Lector Nativo de Código de Barras / QR (CA-46)
    Given el proceso requiere leer etiquetas físicas o documentos
    Then el Arquitecto dispone de un componente "Escaner QR/Barcode"
    And este componente invoca la API moderna de navegadores (WebRTC/MediaDevices) para usar la cámara del dispositivo móvil/laptop
    And el valor escaneado rellena el campo objetivo automáticamente.

  Scenario: Auto-Validación de Regex Comunes (Email/URL) (CA-47)
    Given el Arquitecto configura un campo de texto y le asigna el tipo "Email" o "URL"
    Then el IDE aplica implícitamente la validación de Expresión Regular correspondiente (Ej: `z.string().email()`)
    And el sistema provee feedback visual inmediato de error si el usuario tipea algo como `carlos@gmail` sin dominio TLD.

  Scenario: Mensajes de Ayuda / Hint Texts Multi-Estado (CA-48)
    Given el Arquitecto configura un campo con requisitos complejos (Ej: Contraseña Segura)
    Then puede definir múltiples mensajes de estado (Hint Texts) debajo del componente
    And el color/icono de cada mensaje cambia dinámicamente ("❌ a ✅") conforme el usuario va cumpliendo cada criterio (Ej: Mayúscula, Número, Longitud) en tiempo real.

  Scenario: Rechazo de Modo Oscuro en V1 (CA-49 - Diferido a V2)
    # NOTA: Diferido a V2. Para V1, los formularios generados forzarán Light Mode independientemente del SO/Dispositivo.
    Given el Arquitecto despliega el formulario
    Then el formulario se renderiza siempre en paleta corporativa clara.

  Scenario: Conversor de Moneda Automático (CA-50 - Diferido a V2)
    # NOTA: Diferido a V2.
    Given el Arquitecto configura un campo monetario
    Then el formulario ofrecería conversión de tasa de cambio a COP en vivo al pie del componente.

  Scenario: Componente WYSIWYG de Texto Enriquecido (CA-51 - Diferido a V2)
    # NOTA: Diferido a V2. Para V1 solo existe Texto Plano Textarea.
    Given el Arquitecto requiere que el usuario entregue justificaciones extensas
    Then dispone de un componente de Texto Enriquecido (Word-like, con negritas, listas y cursivas).
```
**Trazabilidad UX:** Wireframes Pantalla 7 (IDE Web Pro-Code para Formularios).

---

### US-028: Auto-Generación de Test Suites (Zod / Jest)
**Como** Ingeniero de Calidad (QA)
**Quiero** que el diseñador de formularios exponga un botón para generar los test unitarios y e2e
**Para** asegurar en mi CI/CD que el comportamiento complejo del "iForm Maestro" no se rompa antes de compilar el frontal.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Automated Form Testing Generation
  Scenario: Descargar/Copiar Suite de Pruebas
    Given el usuario ha finalizado el diseño del "iForm Maestro_Credito.vue"
    When hace clic en el botón "[⚡ GENERADOR DE TESTS]"
    Then el IDE lee la estructura Zod
    And genera y muestra un script de prueba `.spec.ts` (Jest) que inyecta payloads intencionalmente rotos para asegurar que Zod emita un "HTTP 400 Bad Request" local en cliente.
```
**Trazabilidad UX:** Wireframes Pantalla 7 (Botones Inferiores).

### US-029: Ejecución y Envío de Formulario (iForm Maestro o Simple)
**Como** Analista / Usuario de Negocio
**Quiero** diligenciar la información de mi sección habilitada en la vista de la tarea (Pantalla 2) y presionar "Enviar"
**Para** finalizar exitosamente mi actividad y que el motor continúe al siguiente paso del proceso.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Task Completion with Form Data
  Scenario: Enviar datos válidos de formulario
    Given la tarea "TK-100" asignada a "carlos.ruiz" requiere el formulario "Form_Aprobacion_V1"
    And "Form_Aprobacion_V1" exige el campo obligatorio numérico "monto_aprobado"
    When "carlos.ruiz" realiza un POST a "/api/v1/workbox/tasks/TK-100/complete"
    And incluye en el body el JSON '{"variables": {"monto_aprobado": 1500, "comentarios": "Ok"}}'
    Then el sistema debe retornar un HTTP STATUS 200 OK
    And la tarea "TK-100" marca su estado como "COMPLETED"
    And las variables del JSON se persisten asociadas a la instancia del proceso

  Scenario: Enviar datos inválidos (Violación del JSON Schema)
    Given la tarea "TK-100" requiere el campo obligatorio "monto_aprobado" numérico
    When "carlos.ruiz" realiza un POST a "/api/v1/workbox/tasks/TK-100/complete"
    And incluye un JSON vacío '{"variables": {}}'
    Then el sistema valida el payload contra el JSON Schema registrado para "Form_Aprobacion_V1"
    And el sistema debe retornar un HTTP STATUS 400 Bad Request
    And el error format JSON debe especificar `{"error": "ValidationFailed", "fields": [{"field": "monto_aprobado", "message": "Required"}]}`
```
**Trazabilidad UX:** Wireframes Pantalla 2 (Vista de Detalle / Formulario Dinámico).

---

## ÉPICA 3: Inicio y Recepción (Triggers)
Capacidad de iniciar procesos operacionales tanto de forma manual (Pantalla 0) como reactiva (Webhook).

### US-004: Iniciar un Proceso mediante Webhook (Plugin O365 Listener)
**Como** Sistema (APIM / MS Graph / Webhook)
**Quiero** inyectar un payload automatizado a un Endpoint público de la plataforma
**Para** instanciar un caso de negocio nuevo automáticamente sin intervención manual humana.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Process Instantiation via Webhook
  Scenario: Webhook O365 válido inicia proceso de Onboarding
    Given que la definición de proceso BPMN "onboarding_cliente" está activa
    When se recibe un POST en el APIM a "/api/v1/webhooks/trigger/o365_inbox"
    And el payload contiene un "id_mensaje_correo" valido
    Then el sistema debe retornar HTTP STATUS 201 Created
    And debe retornar el "process_instance_id" del nuevo caso
    And el motor BPMN arranca en la primera tarea de configuración del proceso
    And las variables del payload inicial quedan inyectadas al entorno del proceso
```
**Trazabilidad UX:** Wireframes Pantalla 11 (Hub de Integraciones: Eventos Entrantes).

---

## ÉPICA 4: Diseño de Procesos (BPMN) y Estructuración de Proyectos
Esta épica aborda el rol del Arquitecto/Administrador para modelar cómo fluye el trabajo, ya sea mediante un diagrama BPMN estricto o un esqueleto de Proyecto por Fases.

### US-005: Desplegar y Versionar un Modelo de Proceso (BPMN)
**Como** Arquitecto de Procesos
**Quiero** importar un archivo `.bpmn` (BPMN 2.0 XML) generado en el Diseñador Web y desplegarlo en el motor
**Para** que la plataforma sepa cómo enrutar las tareas secuenciales, paralelas y compuertas lógicas de mi proceso oficial.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: BPMN Process Deployment
  Scenario: Despliegue exitoso de un diagrama BPMN válido
    Given que el usuario Arquitecto ha diseñado el flujo "Aprobacion_Credito_v2.bpmn"
    When el usuario sube el archivo invocando un POST multipart/form-data a "/api/v1/design/processes/deploy"
    Then el motor (Ej. Camunda) debe validar la sintaxis XML del archivo
    And el sistema debe retornar HTTP STATUS 201 Created
    And el sistema debe generar una nueva "Version" del "Process Definition ID" (Ej. Credito:2)
    And las nuevas instancias usarán esta versión sin afectar a las que ya estaban "En Vuelo" (In-Flight)

  Scenario: Intento de despliegue con diagrama inválido (BPMN Roto)
    Given un archivo "Proceso_Roto.bpmn" al que le falta un "End Event" necesario
    When el usuario realiza el POST a "/api/v1/design/processes/deploy"
    Then el motor debe denegar el despliegue
    And el sistema debe retornar HTTP STATUS 422 Unprocessable Entity
    And el payload debe contener el mensaje parseado: "El diagrama no es instanciable. Falta End Event."

  Scenario: Análisis Semántico en "Pre-Flight" de un diagrama complejo (Ejecutabilidad)
    Given el Arquitecto importa un diagrama BPMN 2.0 ("Proceso_Core.bpmn") que contiene Subprocesos, Start Events de Mensaje y Tareas de Servicio
    When el usuario solicita la validación previa al despliegue ("Pre-Flight Analyze")
    Then el motor semántico debe parsear los componentes avanzados
    And identificar si alguna `ServiceTask` carece de su propiedad `Delegate Expression` (Ejecución de código)
    And identificar si alguna `UserTask` carece de una vinculación de `Form Key`
    And identificar si alguna `ExclusiveGateway` carece de un flujo por defecto (`Default Flow`)
    And el sistema debe renderizar en Pantalla 6 la lista de Errores (❌) y Advertencias (⚠️) para que el Arquitecto los corrija antes del despliegue.

  Scenario: Autogeneración de Roles RBAC desde Carriles (Lanes)
    Given el Arquitecto importa un diagrama interactivo BPMN ("Flujo_Onboarding.bpmn")
    And el diagrama contiene un Carril (Lane) llamado "Aprobadores_Legales"
    And dentro de ese carril existe la Tarea "Firmar_Contrato" asociada al template "Form_Firma"
    When el usuario realiza el POST a "/api/v1/design/processes/deploy" con éxito
    Then el backend debe crear automáticamente el Rol de Sistema "BPMN_Flujo_Onboarding_Aprobadores_Legales"
    And el sistema debe asociar automáticamente a este Rol los permisos de escritura sobre "Form_Firma" y ejecución sobre la tarea "Firmar_Contrato"
    And el Rol autogenerado queda disponible en el Módulo de Seguridad (Pantalla 14) para asignarle usuarios.

  Scenario: Migración Dual de Instancias En Vuelo (CA-5)
    Given el Arquitecto despliega la versión 3 de "Aprobacion_Credito.bpmn"
    And existen 15 instancias activas ("En Vuelo") ejecutándose con la versión 2
    Then el sistema debe ofrecer dos opciones mutuamente excluyentes al Arquitecto:
    And Opción A: "Mantener instancias actuales en v2" (coexistencia pacífica, las nuevas instancias usan v3)
    And Opción B: "Forzar migración de las 15 instancias activas a v3" (el motor reapunta los tokens BPMN)
    And en ambos casos, el sistema registra un Audit Log con la decisión tomada.

  Scenario: Rollback a Versión Anterior con Historial (CA-6)
    Given el Arquitecto detecta que la versión 3 de un proceso tiene un error lógico post-despliegue
    When navega al panel de "Historial de Versiones" en la Pantalla 6
    Then el sistema debe listar todas las versiones desplegadas previamente (v1, v2, v3) con fecha y autor
    And el Arquitecto puede seleccionar "Restaurar v2" con un solo clic
    And el sistema re-despliega la v2 como la nueva versión activa (v4 internamente = copia de v2)
    And las instancias en vuelo de v3 siguen corriendo hasta terminar naturalmente (salvo Migración Forzada explícita).

  Scenario: Bloqueo Pesimista de Edición Concurrente (CA-7)
    Given el Arquitecto "maria.lopez" abre el proceso "Solicitud_Credito" en el Diseñador (Pantalla 6)
    And el sistema le otorga un "Lock" exclusivo sobre ese proceso
    When el Arquitecto "carlos.gerente" intenta abrir el mismo proceso simultáneamente
    Then el sistema debe mostrar un mensaje: "🔒 Este proceso está siendo editado por maria.lopez desde las 10:15 AM"
    And debe bloquear los controles de edición del lienzo, dejando solo el modo "Solo Lectura" para el segundo usuario.

  Scenario: Copiloto IA Bajo Demanda (CA-8)
    Given el Arquitecto está diseñando un diagrama BPMN en el lienzo
    Then el Copiloto IA NO ejecuta análisis automático en tiempo real
    When el Arquitecto hace clic explícitamente en el botón [🧠 Consultar Copiloto IA]
    Then el sistema envía el XML del diagrama actual al endpoint de IA
    And renderiza las sugerencias y alertas ISO 9001 en el Panel de Feedback inferior.

  Scenario: Pre-Flight Extendido con Validaciones Avanzadas (CA-9)
    Given el Arquitecto solicita un "Pre-Flight Analyze" sobre un diagrama complejo
    Then el sistema debe validar, además de las reglas base (ServiceTask, UserTask, Gateway):
    And identificar si algún `TimerEvent` carece de la expresión de duración configurada (Ej. `R/PT1H`)
    And identificar si algún `MessageEvent` (Intermedio o de Inicio) carece de la correlación o nombre del mensaje (`MessageRef`)
    And identificar si algún `CallActivity` apunta a un `ProcessDefinitionKey` que NO existe desplegado en el motor
    And clasificar los hallazgos como Error (❌ bloquea despliegue) o Advertencia (⚠️ informativa).

  Scenario: Auto-Guardado del Diagrama en Borrador (CA-10)
    Given el Arquitecto está editando un diagrama BPMN en la Pantalla 6
    Then el sistema debe guardar automáticamente un borrador del XML cada 30 segundos (Best Practice Auto-Save)
    And si el usuario cierra el navegador sin desplegar, al volver a abrir el proceso encontrará el último borrador recuperado
    And el sistema debe mostrar un indicador discreto "✅ Guardado" en la barra de estado tras cada auto-guardado exitoso.

  Scenario: Simulación en Sandbox Antes de Desplegar (CA-11)
    Given el Arquitecto tiene un diagrama BPMN listo pero no ha sido desplegado aún
    When presiona el botón [🧪 Probar en Sandbox]
    Then el sistema debe generar una instancia temporal (no persiste en producción) del proceso
    And avanzar visualmente paso a paso mostrando por qué nodo (tarea/compuerta/evento) fluiría un caso de prueba ficticio
    And al finalizar la simulación, destruir la instancia temporal sin dejar rastro en la base de datos de producción.

  Scenario: Separación de Roles RBAC Diseñador vs Release Manager (CA-12)
    Given un usuario con rol "BPMN_Designer" abre un proceso en la Pantalla 6
    Then puede dibujar, importar, exportar y consultar al Copiloto IA
    But el botón [🚀 DESPLEGAR] debe estar deshabilitado (gris) para este rol
    When un usuario con rol "BPMN_Release_Manager" abre el mismo proceso
    Then puede ver el diagrama y presionar [🚀 DESPLEGAR] para enviarlo al motor
    And ambos roles son asignables desde el Módulo de Seguridad (Pantalla 14) y un usuario puede tener ambos simultáneamente.
    And estos roles son GLOBALES (aplican a todos los procesos sin granularidad por módulo). La granularidad por proceso se difiere a V2.

  Scenario: Paleta BPMN 2.0 Estándar Completa con UX Priorizada (CA-13)
    Given el Arquitecto abre el Diseñador en la Pantalla 6
    Then la Paleta BPMN 2.0 debe contener TODOS los elementos del estándar (incluyendo Conditional, Link, Cancel Events, Complex Gateway, Ad-Hoc y Event Sub-Process)
    But los elementos más usados (Start/End, User Task, Service Task, Exclusive/Parallel Gateway) deben aparecer como iconos principales visibles
    And los elementos avanzados/exóticos deben estar agrupados bajo submenús colapsables ("Más Eventos...", "Más Compuertas...")
    And esto evita saturar visualmente un principiante pero no limita a un experto.

  Scenario: Catálogo / Biblioteca de Procesos Desplegados (CA-14)
    Given el Arquitecto accede a la Pantalla 6
    Then debe existir un Panel lateral o pestaña "Explorador de Procesos" que liste todos los procesos diseñados
    And cada entrada muestra: Nombre, Versión Activa, Fecha de Último Despliegue y Autor
    And al hacer clic en un proceso, se carga en el Lienzo para su edición o consulta.

  Scenario: Text Annotations (Notas Adhesivas BPMN) en el Lienzo (CA-15)
    Given el Arquitecto está diseñando un diagrama
    Then debe poder arrastrar un componente "Text Annotation" desde la Paleta al Lienzo
    And escribir comentarios explicativos que se renderizan visualmente sobre el diagrama
    And estas anotaciones se persisten en el archivo .bpmn XML como parte del estándar.

  Scenario: Zoom, Minimap y Navegación Visual (CA-16)
    Given el Arquitecto trabaja con un diagrama con más de 3 carriles y 20+ nodos
    Then el Lienzo debe soportar controles de Zoom (+/-) y "Ajustar a Pantalla"
    And un Mini-Mapa (panorámico) en la esquina inferior derecha para navegar rápidamente entre secciones lejanas del diagrama.

  Scenario: Naming Dual - Nombre de Negocio y Nombre Técnico (CA-17)
    Given el Arquitecto crea una User Task y escribe "Llenar Formulario de Crédito" como nombre visible
    Then el panel de Propiedades debe ofrecer un segundo campo: "ID Técnico (Technical Name)"
    And si el Arquitecto no lo rellena, el sistema debe auto-generar un slug (Ej: `llenar_formulario_de_credito`)
    And el motor Camunda usará el ID Técnico internamente, mientras que la UI del Workdesk mostrará el Nombre de Negocio.

  Scenario: Plantillas BPMN Prediseñadas (CA-18)
    Given el Arquitecto presiona "Nuevo Proceso" en la Pantalla 6
    Then un Modal debe ofrecer la opción "Empezar desde Cero" o "Usar Plantilla"
    And las plantillas disponibles incluyen ejemplos comunes (Ej: "Aprobación Simple", "Onboarding Cliente", "Incidencia IT")
    And al seleccionar una plantilla, se carga en el Lienzo como punto de partida editable.

  Scenario: Diff Visual entre Versiones (CA-19 - Diferido a V2)
    # NOTA: Este escenario queda documentado pero su implementación se difiere a la Versión 2 del producto.
    Given el Arquitecto navega al Historial de Versiones y selecciona v2 y v3 para comparar
    Then el sistema muestra un Diff visual resaltando nodos agregados (verde), eliminados (rojo) y modificados (amarillo).

  Scenario: Copiar y Pegar Fragmentos entre Procesos (CA-20)
    Given el Arquitecto tiene abiertos dos procesos en pestañas distintas de la Pantalla 6
    When selecciona un fragmento (Ej: un Sub-Proceso con 5 tareas) del Proceso A y ejecuta "Copiar"
    Then debe poder "Pegar" ese fragmento en el Lienzo del Proceso B
    And el sistema debe re-mapear los IDs internos para evitar colisiones XML.

  Scenario: Límite de Complejidad Parametrizable y Advertencia de Mala Práctica (CA-21)
    Given el sistema tiene configurado un umbral de complejidad máxima (Ej: 100 nodos por defecto, parametrizable)
    When el Arquitecto excede ese umbral dibujando el nodo número 101
    Then el sistema debe mostrar una advertencia visual: "⚠️ Mala Práctica de Diseño: Este proceso supera los 100 nodos"
    And debe detallar los riesgos: "Procesos complejos son difíciles de mantener, propensos a errores y degradan el rendimiento del motor"
    And la advertencia NO bloquea el despliegue, solo informa. El umbral es configurable por un Admin.

  Scenario: Etiquetas de Estado en el Catálogo de Procesos (CA-22)
    Given el Catálogo de Procesos desplegados (CA-14) lista todos los procesos
    Then cada proceso debe tener una etiqueta visual de estado:
    And "📝 BORRADOR" si nunca ha sido desplegado al motor (solo existe como XML guardado)
    And "✅ ACTIVO (v3)" si tiene al menos una versión desplegada y operativa
    And "📦 ARCHIVADO" si fue retirado de operación (CA-23).

  Scenario: Archivar un Proceso sin Instancias Activas (CA-23)
    Given el Arquitecto selecciona un proceso "Proceso_Obsoleto" en el Catálogo
    And NO existen instancias "En Vuelo" de ese proceso
    When presiona el botón [📦 Archivar]
    Then el sistema cambia el estado del ProcessDefinition a "ARCHIVADO"
    And no se podrán crear nuevas instancias de ese proceso
    And el proceso deja de estar visible para los usuarios operativos, pero permanece en BD para auditoría
    But si existen instancias activas, el botón Archivar está deshabilitado con el tooltip: "No se puede archivar: X instancias en ejecución".

  Scenario: Invalidación Automática del Pre-Flight tras Edición (CA-24)
    Given el Arquitecto ejecutó el Pre-Flight Analyzer y obtuvo resultado "✅ Sin Errores"
    When posteriormente modifica el diagrama (agrega/elimina/cambia un nodo)
    Then el estado del Pre-Flight debe resetearse automáticamente a "⚠️ Pendiente de re-validación"
    And el botón [🚀 DESPLEGAR] debe requerir una nueva ejecución del Pre-Flight antes de habilitarse.

  Scenario: Solicitar Despliegue al Release Manager (CA-25)
    Given el Designer ha terminado de diseñar y el Pre-Flight está aprobado
    When presiona el botón [📩 Solicitar Despliegue]
    Then el sistema cambia el estado del proceso a "PENDIENTE_APROBACIÓN_DESPLIEGUE"
    And crea automáticamente una tarea en el Workdesk del usuario con rol "BPMN_Release_Manager"
    And el Release Manager ve esta tarea en su bandeja con el botón [🚀 Aprobar y Desplegar] o [❌ Rechazar].

  Scenario: SLA Configurable por Tarea Individual o Global (CA-26)
    Given el Arquitecto configura un UserTask en el Panel de Propiedades de la Pantalla 6
    Then el campo "SLA" puede tener un valor específico por tarea (Ej: "4 horas" para "Analizar", "48 horas" para "Firmar")
    And adicionalmente debe existir un SLA Global a nivel de ProcessDefinition (Ej: "5 días hábiles para el proceso completo")
    And las reglas de negocio o el Diseñador definen cuál prevalece en caso de conflicto.

  Scenario: Link Directo a Sub-Proceso desde Call Activity (CA-27)
    Given el Arquitecto selecciona una Call Activity en el Lienzo que apunta al proceso hijo "Proceso_Riesgo"
    Then el Panel de Propiedades debe mostrar un link clickeable: "[🔗 Abrir Sub-Proceso: Proceso_Riesgo]"
    And al hacer clic, se abre el proceso hijo en una nueva pestaña del Diseñador para editarlo o consultarlo.

  Scenario: Colores Personalizados en Carriles y Tareas (CA-28 - Diferido a V2)
    # NOTA: Este escenario queda documentado pero su implementación se difiere a la Versión 2 del producto.
    Given el Arquitecto selecciona un Carril o Tarea en el Lienzo
    Then puede asignarle un color personalizado desde una paleta de colores para distinguir departamentos.

  Scenario: Autocompletado de Variables en Expresiones (CA-29 - Diferido a V2)
    # NOTA: Diferido a V2.
    Given el Arquitecto escribe una condición en una Compuerta Exclusiva (Ej: `${monto > 5000}`)
    Then el sistema ofrece autocompletado de variables disponibles basándose en los formularios asociados al proceso.

  Scenario: FormKey como Dropdown Validado desde Pantalla 7 (CA-30)
    Given el Arquitecto selecciona una User Task en el Lienzo de la Pantalla 6
    When accede al campo "📄 Formulario Asociado" en el Panel de Propiedades
    Then el campo debe ser un Dropdown (NO texto libre) que lista los formularios registrados en la Pantalla 7
    And cada opción del Dropdown muestra: Nombre del formulario, Tipo (🟢 Simple o 🔵 iForm Maestro), y si es Maestro, el número de etapas configuradas
    And si no se selecciona ningún formulario, el Pre-Flight lo marca como Error.

  Scenario: Consistencia de Patrón de Formulario por Proceso (CA-31)
    Given el Arquitecto crea un nuevo proceso en la Pantalla 6
    Then al inicio debe elegir el patrón de formulario: "Patrón A: Formulario Simple" o "Patrón B: iForm Maestro"
    And esta decisión es inmutable para ese proceso (consistente con US-003)
    And si eligió Patrón A, cada User Task mostrará en el Dropdown solo formularios "Simple"
    And si eligió Patrón B, todas las User Tasks compartirán el mismo iForm Maestro y el Dropdown filtrará solo formularios "Maestro".

  Scenario: Sandbox Simulado en Motor de Producción en V1 (CA-32)
    Given el iBPMS V1 opera con un único motor Camunda (no hay ambiente de Desarrollo separado)
    Then el botón [🧪 Sandbox] genera instancias temporales directamente en el motor de producción
    And estas instancias se marcan como "SANDBOX_TEST" y se auto-destruyen al finalizar la simulación
    And la separación real de ambientes (Dev vs Prod) se difiere a V2.

  Scenario: Registro de Auditoría de Diseño tipo Git-Log (CA-33)
    Given el Arquitecto realiza cualquier acción sobre un proceso (importar, editar, guardar borrador, solicitar despliegue, archivar, restaurar versión)
    Then el sistema debe crear una entrada en un log de auditoría persistente (BD) con: Acción, Usuario, Timestamp y Versión Afectada
    And este log debe ser visible para Administradores en un panel "📜 Historial de Cambios" (estilo Git Log) dentro de la Pantalla 6.

  Scenario: Lock Manual sin Expiración Automática (CA-34)
    Given el Arquitecto "maria.lopez" tiene el Lock sobre un proceso
    And permanece inactiva por más de 30 minutos
    Then el Lock NO expira automáticamente
    And otros usuarios que intenten editar verán: "🔒 Bloqueado por maria.lopez. Contacte al usuario para solicitar la liberación."
    And la liberación es un proceso manual: María debe cerrar su pestaña o presionar un botón "Liberar Edición".

  Scenario: Soporte Multi-Pool para Modelado de Colaboración (CA-35)
    Given el Arquitecto crea un nuevo diagrama BPMN en la Pantalla 6
    Then puede agregar múltiples Pools al Lienzo representando actores internos y externos (Ej: "Mi Empresa", "Banco Externo", "Proveedor")
    And puede conectar los Pools con Message Flows (flechas de mensaje) para modelar la interacción
    And los Pools externos son representaciones visuales (cajas negras) que no se ejecutan en el motor Camunda interno
    And esto provee claridad documental y de auditoría sobre quién habla con quién.

  Scenario: Service Task con Dropdown de Conectores API del Hub (CA-36)
    Given el Arquitecto coloca una Service Task en el Lienzo y abre su Panel de Propiedades
    Then el campo "Conector / API" debe ser un Dropdown que lista los conectores registrados en la Pantalla 11 (Hub de Integraciones)
    And cada opción muestra: Nombre del conector, Tipo (REST/SOAP/GraphQL) y Sistema Destino
    And para V1, los conectores pre-armados obligatorios son:
    And - 📧 Microsoft O365 / Exchange (Correo corporativo)
    And - 📁 Microsoft SharePoint (Gestión documental)
    And - 💰 Oracle NetSuite (ERP/Financiero)
    And si el conector necesario NO existe aún en el Hub, consultar CA-37.

  Scenario: MessageEvent como Placeholder de Integración Futura (CA-37)
    Given el Arquitecto necesita modelar una integración con un sistema externo cuyo conector API aún no fue registrado en el Hub (Pantalla 11)
    Then debe usar un MessageEvent (Intermediate Throw/Catch) como marcador visual temporal
    And el Pre-Flight Analyzer debe clasificar este nodo como Advertencia (⚠️): "MessageEvent sin conector API asociado. Considere crear el conector en el Hub y migrar a Service Task."
    And cuando el conector sea registrado posteriormente, el Arquitecto puede reemplazar el MessageEvent por una Service Task enlazada al nuevo conector.
```
**Trazabilidad UX:** Wireframes Pantalla 6 (Diseñador BPMN) y Pantalla 14 (RBAC).

---

### US-006: Diseñar la Estructura (WBS) de un Proyecto (Templates)
**Como** Director de Proyectos / Administrador
**Quiero** crear una Plantilla de Proyecto definiendo Fases (Sprints/Etapas) y pre-asignando Formularios a tareas genéricas
**Para** que cuando el negocio inicie un proyecto real, se instancie automáticamente toda la estructura y la gente sepa qué formularios debe llenar en cada fase.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Project Template Builder
  Scenario: Creación de un esqueleto de proyecto (WBS) con hitos y formularios mapeados
    Given que el administrador está configurando el "Template de Apertura de Tienda"
    When el usuario envía un POST a "/api/v1/design/projects/templates"
    And el body JSON incluye 2 Fases ("Pre-Obra", "Obra") y 1 Tarea ("Presupuesto") en la Fase 1, enlazada al formulario "Form_Presupuesto"
    Then el sistema debe retornar HTTP STATUS 201 Created
    And el "Project Template ID" debe guardarse en la BD listo para ser instanciado por el usuario operativo
```
**Trazabilidad UX:** Wireframes Pantalla 8 (Project Builder).

---

### US-027: Copiloto IA Tutor (Auditoría BPMN 2.0 e ISO 9001)
**Como** Arquitecto Modelador de Procesos
**Quiero** un asistente IA embebido en el diseñador (Pantalla 6) que evalúe mi diagrama en tiempo real
**Para** recibir sugerencias de mejora de arquitectura, identificar antipatrones (Ej. dead-ends) y confirmar que el flujo cumple principios de calidad tipo ISO 9001.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: AI Copilot para Diseño BPMN y Auditoría ISO
  Scenario: Diagnóstico de calidad y sugerencia de ISO 9001
    Given que el Arquitecto está dibujando un proceso en el lienzo y hace clic en "Consultar a IA Copilot"
    When el sistema envía el XML en memoria y el contexto semántico al LLM API
    Then el Agente IA debe analizar la estructura devolviendo un reporte en la UI
    And destacar áreas de mejora (Ej: "La compuerta no tiene validación humana, lo cual es riesgoso bajo control ISO 9001")
    And sugerir componentes correctos de BPMN 2.0 para reemplazar antipatrones.
```
**Trazabilidad UX:** Wireframes Pantalla 6 (Diseñador BPMN).

---

## ÉPICA 5: Modelado de Reglas de Negocio con IA (DMN)
Permite a los usuarios de negocio (no técnicos) generar reglas lógicas complejas utilizando lenguaje natural.

### US-007: Generar tabla DMN desde lenguaje natural (NLP)
**Como** Arquitecto de Procesos / Usuario de Negocio
**Quiero** escribir una regla de negocio en lenguaje natural (ej. "Si el reclamo es mayor a 1000, rechazar")
**Para** que la IA la traduzca instantáneamente a una tabla DMN (JSON/XML) sin tener que aprender la notación técnica.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: NLP to DMN Translation via LLM API
  Scenario: Traducción exitosa de lenguaje natural a tabla DMN
    Given que el usuario está en el Taller DMN con IA
    When el usuario envía una petición POST a "/api/v1/ai/dmn/translate"
    And incluye el prompt "Si el monto es mayor a 500, requiere 'Aprobacion_Gerente', sino 'Aprobacion_Lider'"
    Then el sistema debe retornar HTTP STATUS 200 OK
    And el payload debe contener el string literal del XML DMN validado y parseable por el motor
    And el DMN debe contener las entradas (Inputs) y salidas (Outputs) inferidas correctamente
```
**Trazabilidad UX:** Wireframes Pantalla 4 (Taller DMN asitido con IA).

---

## ÉPICA 6: Gestión Ágil y Kanban
Habilita el trabajo no estructurado dentro de la plataforma para proyectos que no requieren diagramas BPMN deterministas.

### US-008: Mover Tarjeta en Tablero Kanban (Cambio de Estado)
**Como** Ejecutor / Analista
**Quiero** arrastrar una tarjeta de un estado a otro en mi tablero (Ej. de "Haciendo" a "Hecho")
**Para** actualizar el estatus de mi trabajo sin navegar por múltiples pantallas.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Kanban Board Task Management
  Scenario: Mover tarjeta de una columna a otra
    Given el tablero del proyecto "Implementación ERP" con columnas "TODO", "DOING", "DONE"
    And la tarea Kanban "KT-050" está en estado "TODO"
    When el usuario realiza un PATCH a "/api/v1/projects/kanban/tasks/KT-050/status"
    And el payload es '{"new_status": "DOING"}'
    Then el sistema debe retornar HTTP STATUS 200 OK
    And actualizar el timestamp de "last_modified" en la tabla 'ibpms_kanban_tasks'
    And el payload de respuesta debe retornar el objeto completo serializado `{ "id": "KT-050", "status": "DOING", "version": 2 }`
    And la UI debe propagar el evento vía WebSockets para que la tarea "KT-050" se refleje en la columna "DOING" para los demás miembros del equipo conectadas al tablero
```
**Trazabilidad UX:** Wireframes Pantalla 3 (Tableros de Proyecto Kanban).

---

## ÉPICA 7: Dashboards y Reportería Operativa (BAM)
Exposición de la salud de los procesos en vuelo para la toma de decisiones gerenciales.

### US-009: Visualizar Salud del Proceso (BAM Dashboard)
**Como** Líder de Operaciones / Gerente
**Quiero** acceder visualizar un dashboard analítico con el conteo de tareas retrasadas y volúmenes operativos
**Para** identificar cuellos de botella y reasignar cargas de trabajo en tiempo real.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Process Health Analytics
  Scenario: Renderizado exitoso del Dashboard de Grafana
    Given un usuario autenticado con Rol "Gerente_Operaciones"
    When la aplicación frontend solicita renderizar el iframe interactivo en la Pantalla 5
    Then el API Gateway debe emitir un JWT de corta duración (Grafana Auth Proxy) con rol de "Viewer"
    And el iframe debe renderizar correctamente el tablero pasándole variables de entorno `&var-TenantID=T123`
    And el dashboard debe mostrar obligatoriamente un panel de "Tareas Vencidas por SLA" consultando la vista materializada `vw_task_sla_breach`
```
**Trazabilidad UX:** Wireframes Pantalla 5 (Dashboards y Panel de Control - BAM).

---

## ÉPICA 8: Generador Documental Jurídico (SGDEA)
*(SHOULD HAVE)* - Producción controlada de artefactos legales a partir del estado final de un caso.

### US-010: Generar y Descargar PDF a partir de datos del caso
**Como** Analista / Gestor Documental
**Quiero** que el sistema ensamble un PDF inmutable (Ej. un Contrato) con los datos finales del caso
**Para** enviarlo a firma o entregarlo al cliente sin errores de "copy-paste".

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Legal PDF Generation from Template
  Scenario: Ensamblar PDF usando plantilla del SGDEA y Variables de la Instancia
    Given la instancia de proceso "PI-888" finalizada en estado "APPROVED"
    And la plantilla "Contrato_Laboral_V3.docx" almacenada en el repositorio
    When el usuario realiza un POST a "/api/v1/documents/generate/PI-888"
    And especifica '{"template_id": "Contrato_Laboral_V3"}'
    Then el motor Documental (FOP/PDFBox) debe resolver el mapeo inyectando el árbol `json_variables` de la instancia "PI-888" en las etiquetas `<<key>>` de la plantilla
    And el sistema debe registrar el checksum SHA-256 del archivo generado en `ibpms_audit_log` para inmutabilidad legal
    And el sistema retorna HTTP STATUS 200 OK con un link "Signed URL" (AWS S3 Presigned o Azure Blob SAS) expirable en 15 minutos para la descarga del archivo `Contrato_Laboral_V3_PI-888.pdf`
```
**Trazabilidad UX:** Wireframes Pantalla 12 (Bóveda Documental y Generación).

---

## ÉPICA 9: M365 Copilot & Bandeja Docketing (Transicionado a MVP V1)
*(Esta épica fue pivotada de V2 a V1 para garantizar el Product-Market Fit como plataforma AI-First).*

### US-011: Filtrado Transversal en Bandeja Avanzada (Docketing)
**Como** Analista Legal / Supervisor de Operaciones
**Quiero** filtrar mi bandeja de entrada estructurada (Pantalla 1B) mediante dropdowns relacionales ("Cliente", "Proyecto", "Rango de Fechas") y etiquetas booleanas de actividad ("Acuses", "Tareas Creadas")
**Para** localizar rápidamente eventos críticos o cargas de trabajo asociadas a cuentas clave sin abrir cada correo individualmente.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Advanced Relational Inbox Filtering
  Scenario: Filtrado compuesto determinista (Cliente + Proyecto)
    Given el usuario autenticado está navegando la Bandeja Avanzada (Pantalla 1B)
    And hay 500 ítems en la bandeja, de los cuales 5 pertenecen al Cliente "Global Tech" y el Proyecto "Patente-XZ"
    When el usuario selecciona "Global Tech" en el selector 'Filtro Cliente'
    And el usuario selecciona "Patente-XZ" en el selector 'Filtro Proyecto'
    Then el API del Backend debe ejecutar una query cruzada contra 'ibpms_metadata_index'
    And el Frontend debe renderizar exclusivamente los 5 ítems exactos en menos de 1 segundo (Paginado)
    And la UI debe mostrar un estado "Empty State" si la combinación no retorna resultados

  Scenario: Filtrado por Label Booleano generado por IA (Acuses)
    Given la bandeja contiene ítems marcados por la IA con el boolean flag 'is_acknowledgment_sent: true'
    When el usuario marca el checkbox "Actividad: Acuse Enviado"
    Then el sistema debe ocultar todos los correos donde 'is_acknowledgment_sent: false' o nulo
```
**Trazabilidad UX:** Prototipo UI2.html / Pantalla 1B.

---

### US-012: Propuesta de respuesta para correo entrante (con revisión humana)
**Como** gestor de un buzón corporativo
**Quiero** recibir un borrador de respuesta basado en el contexto del hilo y precedentes
**Para** contestar más rápido y con consistencia.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Generación de Borradores de Respuesta Interactivos
  Scenario: Sistema genera borrador bilingüe esperando revisión humana
    Given un correo electrónico entrante recibido en el buzón corporativo
    When la IA analiza el contexto y detecta el idioma (Ej: ES o EN)
    Then el sistema debe generar al menos 1 borrador de respuesta en el mismo idioma detectado
    And presentar el borrador en la interfaz bloqueando el envío automático
    And permitir al usuario "Aprobar", "Editar" o "Rechazar" el borrador
    And el sistema no debe enviar el correo hasta que el usuario ejecute una acción afirmativa
    And el sistema debe conservar un identificador de trazabilidad (conversation_id)
```
**Trazabilidad UX:** Prototipos UI1.html y UI4.html / Pantalla 2C.

---

### US-013: Identificación automática de cliente y enriquecimiento desde CRM (ONS)
**Como** gestor de un buzón corporativo
**Quiero** que el asistente identifique el cliente por el dominio del remitente y consulte el CRM ONS
**Para** contextualizar la respuesta y adaptar el tono.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Enriquecimiento CRM de Hilos de Correo
  Scenario: Match exitoso con CRM ONS
    Given un correo entrante con dominio '@cliente.com'
    When el sistema consulta la API del CRM ONS usando el dominio
    Then si existe coincidencia, asocia el correo a un 'client_profile' (account/contact)
    And el LLM ajusta el nivel de formalidad del borrador basado en la data del perfil del cliente
  
  Scenario: Cliente no identificado
    Given un correo entrante donde el dominio no existe en CRM ONS
    Then el sistema marca el correo visualmente como "Cliente no identificado"
    And sugiere una tarjeta atómica para solicitar datos o registrar al cliente nuevo en CRM
```

---

### US-014: Sugerencia de acciones (tareas) sin ejecución automática
**Como** gestor de un buzón corporativo
**Quiero** que el asistente sugiera acciones asociadas al correo (crear tarea, asignar responsable, solicitar info)
**Para** acelerar el flujo de trabajo sin perder control.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Sugerencia de Acciones Atómicas (Human-in-the-Loop)
  Scenario: Presentación y ejecución de acciones sugeridas
    Given un análisis de correo completado
    Then el sistema configura y genera una lista de N acciones sugeridas
    And cada acción exhibe: tipo, descripción, responsable sugerido, prioridad y fecha
    And el usuario puede explícitamente "Aprobar" o "Rechazar" cada tarjeta individual
    And si el usuario aprueba "Crear Tarea", el sistema llama la API interna confirmando el task_id
    And si el usuario no aprueba explícitamente, no se ejecuta ninguna alteración en el sistema interno
    
  Scenario: Detección y Enmascaramiento PII (Seguridad)
    Given la entrada de un correo electrónico crudo en el buzón (Pantalla 1B)
    When la IA analiza el cuerpo del mensaje antes de persistirlo para su paso al Embudo Admin (Pantalla 16)
    Then la IA debe identificar patrones PII (Tarjetas de Crédito, SSN, Datos Médicos) y enmascararlos (`[CONFIDENCIAL]`)
    And el payload enmascarado será la única versión transferida al Plan A para salvaguardar la privacidad.
```

---

### US-015: Feedback y aprendizaje supervisado (Human-in-the-loop MLOps)
**Como** Analista del Área de Servicio (SAC)
**Quiero** que el agente inteligente me proponga respuestas y acciones, permitiéndome editarlas, aprobarlas o rechazarlas
**Para** garantizar que la máquina no tome decisiones autónomas erróneas y aprenda continuamente de mis correcciones en la Pantalla 1B.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Asistente SAC y Triaje con Aprobación Humana Obligatoria
  Scenario: Disparador Diferido del Acuse de Recibo (CA-1)
    Given la llegada de un correo electrónico de un cliente al buzón parametrizado
    When el Agente Inteligente 3 analiza el contenido y genera un borrador de Respuesta
    Then el iBPMS NO envía el correo automáticamente al cliente
    And retiene el borrador en la Pantalla 1B hasta que el humano lo revise y presione específicamente el botón `[Enviar Acuse]` o `[Cancelar Propuesta]`.

  Scenario: Tolerancia al Riesgo y Declaración de Incertidumbre (CA-2)
    Given un correo electrónico confuso o ininteligible
    When la red neuronal calcula un Confidence Score por debajo del umbral parametrizado
    Then el Agente 3 se declara incompetente explícitamente y deja la tarea en blanco para el humano
    And expone un botón amarillo de `[Retroalimentar IA]` que despliega un Modal para que el analista (opcionalmente) le enseñe a clasificar ese caso anómalo.

  Scenario: Consumo Contextual de Historial Cliente (Exchange API) (CA-3)
    Given la necesidad de redactar una respuesta útil al cliente
    Then el Agente 3 se conecta vía API a Microsoft Exchange (O365) para recuperar el hilo de correos recientes del cliente
    And utiliza este contexto para generar el borrador, ignorando la Bóveda SGDEA (SharePoint) para este propósito específico.

  Scenario: Tono Dinámico Inyectado por Administrador (CA-4)
    Given la generación de texto natural (NLG) hacia un ciudadano
    Then el tono (Formal, Empático, Urgente) no está hardcodeado en la lógica
    And hereda las directrices directamente del *System Prompt* parametrizado previamente por el Administrador del Buzón en la configuración global.

  Scenario: Creación Exclusiva de Tareas Sugeridas (CA-5)
    Given una solicitud de cliente que requiere múltiples acciones inter-área
    Then el Agente 3 NO instancia los procesos subyacentes mágicamente en el backend
    And se limita a proponer la lista de "Tareas / Actividades a Instanciar" en la Pantalla 1B, requiriendo el click humano de `[Aprobar Lista]`.

  Scenario: Resiliencia ante Archivos Densos (Ceguera V1) (CA-6)
    Given un correo con un anexo `.xlsx` de 40 MB lleno de macros financieras
    Then en el alcance de la V1, el Agente 3 ignora el contenido computacional profundo de los anexos (Excel, XMLs)
    And basa su razonamiento exclusivamente en el texto del cuerpo del correo y los PDFs/Word básicos.

  Scenario: Trazabilidad Operativa sin Reporte Formal (CA-7)
    Given una acción sugerida por la IA y aprobada por un analista
    Then el iBPMS guarda el registro transaccional "Aprobado por: X, Sugerido por: IA" en la base de datos para trazabilidad forense
    And no la proyecta como una "Etiqueta Policial de Auditoría" perturbadora en los informes comerciales diarios.
```

---

### US-016: Gestión multi-buzón con políticas por buzón
**Como** administrador del asistente
**Quiero** configurar políticas por buzón (tono, idioma por defecto, categorías, aprobadores)
**Para** adaptar el comportamiento a cada canal corporativo.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Configuración de 'Mailbox Policy' Dinámicas
  Scenario: Aplicación de políticas diferenciadas sin reiniciar el sistema
    Given múltiples buzones corporativos registrados
    When el administrador define un 'mailbox_policy' configurando idioma, nivel de formalidad, disclaimers y escalamiento
    Then las propuestas generadas por el LLM aplican inmediatamente este contexto en sus prompts
    And los nuevos cambios de política operan sobre el siguiente correo entrante sin requerir 'redeploy' de código
```

---

### US-018: Métricas de desempeño y calidad
**Como** líder de operación/servicio
**Quiero** ver métricas de desempeño del asistente
**Para** medir Retorno de Inversión (ROI) y mejora continua.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Cuadro de Mando de Desempeño Inteligente (AI Dashboards)
  Scenario: Consolidación de retorno y eficiencia
    Given el Líder de Operación ingresa al dashboard
    Then puede reportar la tasa de aceptación, edición, rechazo, y tiempo medio de respuesta
    And visualizar las acciones orgánicas creadas segmentadas por buzón y por idioma
    And configurar comparativas "antes vs después" mediante un 'baseline' histórico
    And visualizar en un panel de control la telemetría de fallos de integración (Graph/CRM)
```

---

## ÉPICA 10: Service Delivery (Catálogo de Servicios CRM Federado)
Implementar el paradigma de Delivery separando la "Definición Comercial" (CRM) de la "Ejecución Operativa" (iBPMS) mediante un catálogo consultado en tiempo real.

### US-019: Conectividad Resiliente y Modo Degradado
**Como** Arquitecto de Plataforma
**Quiero** conectar el sistema iBPMS a un catálogo en un CRM externo con caché intermedio
**Para** garantizar que los clientes puedan iniciar procesos incluso si el CRM sufre caídas.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Conectividad y Resiliencia CRM
  Scenario: Consulta base de servicios (CA-1)
    Given el conector CRM (Outbound Adapter) está configurado con autenticación OAuth2
    When el subsistema iBPMS consulta el catálogo por API
    Then el CRM devuelve los servicios incluyendo obligatoriamente su 'service_ref_id' y metadata comercial esencial (Nombre, Descripción)
    
  Scenario: Activación de Modo Degradado por caída de CRM (CA-2)
    Given el backend del CRM externo se encuentra inalcanzable (Timeout o HTTP 5xx)
    And existe sincronización previa en caché Redis
    When un cliente final abre el Portal de Catálogo de Servicios
    Then el iBPMS debe mostrar el catálogo cacheado
    And debe advertir visualmente la "última fecha de actualización"
    And debe permitir iniciar el requerimiento de servicio sin bloquear el Frontend
```

---

### US-020: Estrategias de Sincronización Flexible
**Como** Administrador del Sistema
**Quiero** habilitar múltiples estrategias de refresco del catálogo CRM (Schedulers)
**Para** balancear la carga de red sin perder la precisión de la oferta comercial.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: CRON y Event-Driven Sync
  Scenario: Sincronización calendarizada automática (CA-3)
    Given que la política de 'scheduled sync' está activada (Ej. "Cada 1 Hora")
    When se alcanza el 'cron trigger' en el backend
    Then el motor iBPMS refresca la tabla interna o el 'Redis Cache' con el catálogo del CRM
    And registra el resultado del lote (OK/FAIL) en la tabla 'ibpms_audit_log'
```

---

### US-021: Mapeo de Variables y Tolerance (Fricción Cero)
**Como** Administrador de Integraciones
**Quiero** mapear campos variables visualmente entre lo que dicta el CRM y lo que espera mi BPMN
**Para** que la operación fluya sin requerir modificar código Java cuando cambie una promoción.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Mapeo Configurable CRM a iBPMS
  Scenario: Trazabilidad de versiones de mapeo (CA-4)
    Given que el Administrador configuró un 'CRM Mapping JSON v1'
    When el Administrador publica una nueva configuración 'Mapping v2'
    Then todas las nuevas instancias (casos) iniciadas heredarán y utilizarán el 'Mapping v2'
    And el caso específico mantendrá la trazabilidad inmutable apuntando a qué versión de variables usó al nacer
    
  Scenario: Tolerancia a catálogos incompletos (CA-5)
    Given que el objeto 'Servicio' recuperado del CRM omite un campo no-crítico (Ej: 'URL_Imagen_Promocional')
    When el cliente presiona "Iniciar Nuevo Caso" en el portal (Pantalla 0)
    Then el iBPMS verifica si los 'campos mínimos requeridos' (service_id, cliente) existen
    And si se cumplen los mínimos, permite la instanciación e ignora el campo no-crítico faltante sin lanzar HTTP 500
```
**Trazabilidad UX:** Afecta a la **Pantalla 0: Service Catalog** y la **Pantalla 11: Hub de Integraciones**.

---

## ÉPICA 11: Intelligent Intake y Vistas Híbridas por Rol (Service Delivery)
Definir un modelo controlado de instanciación de procesos ("Plan A" vía correo y "Plan B" manual restrictivo), eliminando el anti-patrón de crear procesos BPMN basura ante simples respuestas de correo electrónico o alertas SPAM.

### US-022: Disparo 'Confirm-to-Create' por Correo (Plan A)
**Como** Líder de SAC (Servicio al Cliente)
**Quiero** enviar un correo de confirmación de servicio a un Cliente desde un buzón corporativo en el iBPMS
**Para** notificarlo, dejar evidencia auditable, y generar una tarea encolada ("Solicitud de Creación SD") sin instanciar ciegamente un proceso basura.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Intake Controlado Plan A (Email Trigger)
  Scenario: Creación de Tarea Administrativa en vez de Service Delivery (CA-1)
    Given el Líder de SAC envía un correo de confirmación a un cliente desde un buzón (Ej: auditorias@ibpms.com) indicando un servicio (Plantilla TO-BE)
    When el correo saliente se envía satisfactoriamente
    Then el sistema registra el correo como evento auditable
    And genera un 'correlation id' asociando al Cliente (CRM ID) y al 'template_id'
    And el sistema no inicia una instancia BPMN en Camunda
    And el sistema crea una Tarea de Usuario ("Crear Service Delivery") asignada al Líder de SAC o Admin
```

---

### US-023: Correlación Continua del Hilo
**Como** Sistema Core ONS
**Quiero** mantener un Tracking/Threading ininterrumpido a lo largo del correo entrante/saliente
**Para** que la comunicación con el cliente nunca se pierda y quede subsumida en el Service Delivery una vez éste nazca.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Threads y Message-ID Tracking
  Scenario: Concatenación de respuestas al Contexto Pre-SD (CA-2)
    Given el sistema envió un correo "Confirm-to-Create" (US-022) y el cliente responde a dicho email
    When el Webhook entrante de M365 captura el correo
    Then vincula la respuesta al 'correlation id' previo
    And cuando el Admin finalmente ejecuta "Crear Service Delivery", vincula todo ese hilo previo de correos (Pre-SD Context) a la instancia madre del BPMN (SD).
```

---

### US-024: Creación Global Restringida (Plan B)
**Como** Administrador del Sistema
**Quiero** un botón de instanciación manual forzada
**Para** arrancar un proceso (SD) sin requerir el paso del correo (Plan A), con validación estricta de mis permisos.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Intake Manual Plan B (Seguridad)
  Scenario: Prevención de instanciación no autorizada (CA-3)
    Given que el usuario autenticado tiene un rol de 'Colaborador_Operativo'
    When busca instanciar un Service Delivery globalmente sin correo
    Then la interfaz bloquea la acción o el API rechaza la solicitud (HTTP 403 Forbidden)
    
  Scenario: Creación Exitoso bajo Administrador
    Given un Administrador ocluye el botón de [ Crear Servicio/Case ] (Pantalla 0 / 9)
    And selecciona la Plantilla TO-BE y asocia el ID de Cliente
    When presiona el botón crear
    Then el sistema inicializa la instancia BPMN en el motor (Camunda)
    And registra una auditoría de inicialización manual con trigger_type=MANUAL
    
  Scenario: Virtual Scroll / Paginación en Embudo (CA-4)
    Given el Administrador abre la Pantalla 16
    When la base de datos contiene más de 25 Intakes en Cuarentena
    Then el API del Backend debe soportar paginación dura (`?limit=25&offset=0`) con opciones de página de 25, 50 o 100 elementos
    
  Scenario: SLA de Embudo "Cuarentena" (CA-5)
    Given un Intake capturado vía correo ha estado en cuarentena por un tiempo mayor al configurado en las políticas globales (SLA Default)
    Then el Backend debe marcar el registro con una bandera de 'SLA_BREACHED'
    And el Frontend debe renderizar esa fila o Card resaltada en color rojo en la Pantalla 16
    
  Scenario: Feedback MLOps Post-Descarte (CA-6)
    Given el Administrador da clic en el botón [ 🗑️ Descartar ] sobre un Intake
    Then el Backend no ejecuta un borrado físico inmediato (Hard Delete)
    And emite un evento (Notificación webhook) al subsistema de Inteligencia Artificial para el reentrenamiento
    And finalmente marca el registro con un 'Soft Delete' y lo oculta del Frontend
    
  Scenario: Forzar Mapeo Manual con CRM Opcional (CA-7)
    Given el Administrador da clic en el botón [ ✏️ Forzar Mapeo Manual ] por fallo de la IA
    When se despliega el Modal de catálogos agrupados para forzar la creación manual
    Then el campo `CRM_ID` debe ser opcional (nullable en base de datos)
    And si las variables extraídas por el correo están incompletas (Ej. Falta "Monto"), el proceso debe instanciarse de todas formas en Camunda omitiendo esa restricción. La variable será exigida posteriormente a nivel de Tarea Humana (Workdesk).
    
  Scenario: Prevención de Concurrencia Optimista (CA-8)
    Given dos Administradores ('Admin_A' y 'Admin_B') visualizan el mismo Intake en Cuarentena en la Pantalla 16 simultáneamente
    When 'Admin_A' aprueba el Intake y 2 segundos después 'Admin_B' intenta aprobar el mismo Intake
    Then el Backend debe rechazar la segunda petición mediante validación de control de concurrencia optimista (EJ: `@Version` en el Entity) o validación de estado.
    
  Scenario: Ventana de Gracia / Botón "Deshacer" (CA-9)
    Given el Administrador presiona "Crear Service Delivery" (Plan A o B)
    When el Frontend envía el payload de creación al Backend
    Then el Frontend debe renderizar un 'Toast' interactivo con botón `[Deshacer]` visible y activo por N segundos paramétricos (Ej: 10s)
    And el Backend postergará el gatillado asíncrono hacia Camunda hasta que expire dicha ventana de gracia, permitiendo abortar limpiamente.
    
  Scenario: Restricción de Anexos por RBAC (Link a Inbox) (CA-10)
    Given un Administrador revisa un Intake (Plan A) en la Pantalla 16
    When hace clic para ver detalles del correo original ("Link to Inbox")
    Then el Frontend verifica si los roles del usuario logueado coinciden con los roles permitidos en el Inbox de SAC
    And si coinciden, lo enruta a la Pantalla 1B para ver el correo íntegro con Anexos descargables
    And si no coinciden, muestra únicamente un 'Summary/Plain Text' en un panel o modal, sin los adjuntos originales.
```

---

### US-025: Experiencia de 'Cards' Dinámicas por Rol
**Como** Arquitecto de Producto UI
**Quiero** segmentar las Tarjetas Kanban y Dashboards por el rol específico del que mira
**Para** evitar ruido cognitivo y entregar exactamente lo que cada persona necesita (Visibilidad, Ejecución o Seguimiento).

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Vistas UX Segregadas por Intención
  Scenario: Renderizado de Card de 'Embudos de Creación' para el Admin
    Given un usuario 'Administrador' (Rol: admin_sd) ingresa al Dashboard (Home)
    Then la Interfaz presenta una "Card: Servicios por Crear" agrupando las llamadas del Plan A ("12 Pendientes de creación")
    
  Scenario: Renderizado de Card Operativa para Ejecutor de Trincheras
    Given un usuario 'Colaborador' (Rol: auditor_senior) ingresa al Workflow (Inbox)
    Then la interfaz NO le muestra el botón "Crear Servicios"
    And presenta tarjetas agrupadas estrictamente por 'Plantillas', ejemplo: "Auditoría Express — 7 Tareas pendientes en rojo".
```

---

## ÉPICA 8: Extensiones Cognitivas AI-Native (Cognitive BPMN)
Aborda la integración nativa de Inteligencia Artificial (LLMs, RAG) en el modelado BPMN (Pantalla 6) y la ejecución del iBPMS, evolucionando de procesos secuenciales estáticos a procesos aumentados cognitivamente.

### US-032: Orquestación de IA y Generative Task (RAG)
**Como** Arquitecto Funcional
**Quiero** disponer de tareas especializadas en IA dentro del diseñador BPMN
**Para** modelar flujos donde un Agente de IA analiza documentos y redacta contenido estructurado sin interrumpir el motor lógico de Camunda.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Componentes AI-Native BPMN y Controles
  Scenario: Output Estricto basado en Schema JSON (CA-1 - Opción A)
    Given el Arquitecto configura una "Generative Task (RAG)" en la Pantalla 6
    Then el motor de IA tiene prohibido generar formato visual (HTML/Docs) directamente
    And está forzado a devolver la información estructurada mediante un Esquema JSON (Extraer y Rellenar)
    And el iBPMS fusiona ese JSON con la plantilla inmutable oficial antes de mostrársela al usuario final.

  Scenario: Desbloqueo de Conocimiento PII con Políticas Estrictas (CA-2)
    Given la tarea "Generative Task (RAG)" necesita consultar la Bóveda SGDEA
    When el LLM busca contexto en documentos clasificados como Privados o PII
    Then se permite la lectura para la generación de la respuesta
    But la política de seguridad (RBAC/DLP) enmascara o prohíbe exponer directamente estos datos sensibles al usuario que no tiene dichos privilegios.
    
  Scenario: Vectorización de Conocimiento a Demanda (CA-3)
    Given el Administrador de Conocimiento sube archivos al SGDEA
    Then dispone de un botón "[Actualizar Memoria IA (Embeddings)]"
    And la vectorización a la base de datos vectorial (Ej: Milvus/Pinecone) no ocurre automáticamente en cada subida de archivo para no degradar el rendimiento, sino de forma controlada y explícita.

  Scenario: Botón de Pánico Anti-Alucinaciones (CA-4)
    Given un usuario humano revisa un borrador generado por la IA en su formulario (Pantalla 7)
    Then dispone de un botón global estilo "[👎 Reportar Alucinación / Error IA]"
    And al accionarlo, se cancela el comportamiento automático, se emite una alerta al "Ingeniero de Prompts" y el proceso se redirige al flujo manual por defecto.

  Scenario: Trazabilidad a las Fuentes (Citas Interactivas) (CA-5)
    Given la "Generative Task (RAG)" emite una respuesta argumentativa
    Then el texto debe incluir referencias o hipervínculos a los IDs documentales usados como contexto
    And cuando el usuario hace clic, el iBPMS lo redirige al visor del SGDEA con la sección exacta resaltada, garantizando verificación humana.

  Scenario: Budget Configurable de Tokens LLM (CA-6)
    Given el Arquitecto configura la tarea cognitiva en la Pantalla 6
    Then existe un parámetro limitante de "Budget de Tokens / Consumo Mensual"
    And si el proceso agota su cuota asiganada, se corta el acceso a la IA y el motor enruta a las ramas B (flujos manuales alternativos) automáticamente.

  Scenario: Gobernanza de Prompts Centralizada (CA-7)
    Given la necesidad de alterar las instrucciones base de los Agentes RAG
    Then existe una pantalla separada llamada "Enterprise Prompt Library"
    And solo los usuarios con el rol especializado `prompt_engineer` tienen permisos CRUD sobre estos prompts globales, dejando a los Arquitectos BPMN únicamente con la facultad de consumirlos.
```
**Trazabilidad UX:** Pantalla 6 (BPMN Designer Palette), Pantalla 12 (SGDEA), Pantalla 7 (Form Builder UI).
  Scenario: Renderizado de Vista 360 para Cuenta / Cliente
    Given un Ejecutivo de Cuenta navega el perfil de un Cliente Específico en su directorio
    Then la interfaz agrupa y presenta TODAS las tareas BPMN y Ágiles atadas a ese CRM_ID 
    And permite resolver la clásica duda del cliente: "¿En qué etapa exacta va mío?"
```

---

### US-026: Portal del Cliente Externo (Vistas Tácticas y Estratégicas)
**Como** Cliente Externo (B2B/B2C)
**Quiero** ingresar a un portal web autenticado para ver el estado de mis Peticiones/Servicios
**Para** no tener que llamar al contact center y tener trazabilidad total (Táctica y Estratégica) de mis trámites.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: External Customer Portal (Service Delivery)
  Scenario: Acceso a Vista Táctica (Estado en Tiempo Real)
    Given un Cliente Externo autenticado (Ej: portal.ibpms.com) mediante Azure AD B2C
    When el cliente ingresa a su panel principal
    Then el sistema debe renderizar una lista con sus Service Deliveries "En Curso"
    And mostrar en qué etapa exacta del proceso se encuentra visualmente (Tracker)

  Scenario: Acceso a Vista Estratégica (Dashboard y SLAs)
    Given el mismo cliente navegando en la pestaña "Histórico y Desempeño"
    Then el sistema renderizará métricas de "Servicios Finalizados a Tiempo" vs "Retrasados"
    And listará todos los Service Deliveries concluidos permitiendo la descarga de su respectivo PDF (SGDEA)
```
**Trazabilidad UX:** Wireframes Pantalla 18 (Portal B2B/B2C del Cliente).

---

## ÉPICA 12: Gobierno de Identidad y Accesos (RBAC Multirrol)
Garantizar que la plataforma soporta el modelo corporativo real donde un usuario ejerce múltiples funciones simultáneamente mediante asignación de múltiples roles y grupos de EntraID.

### US-030: Asignación Multi-Rol y Sincronización EntraID
**Como** Administrador de Seguridad
**Quiero** asignar o sincronizar múltiples roles (Globales y de Proceso) a un mismo usuario autenticado
**Para** que pueda acceder a las distintas bandejas y tareas correspondientes a todos sus 'sombreros' operativos sin necesidad de tener cuentas separadas.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Multitenant RBAC & Multiple Roles Assignment
  Scenario: Resolución de permisos híbridos (Global + Proceso)
    Given que el usuario "maria.lider" está autenticada vía Azure AD (OIDC)
    And tiene sincronizado el Rol Global "Líder de SAC"
    And tiene sincronizado el Rol de Proceso "BPMN_Credito_Aprobador"
    When el usuario solicita su menú de navegación
    Then el sistema debe renderizar el botón de "Inbox" (Pantalla 1B) basado en su Rol Global
    And el sistema debe renderizar el botón de "Workdesk"
    When el usuario abre el "Workdesk"
    Then el backend debe retornar en la lista de tareas pendientes (US-001) los casos asignados explícitamente a "maria.lider", Y ADEMÁS los asignados al candidato "BPMN_Credito_Aprobador"
    And el usuario puede ejecutar exitosamente acciones derivadas de ambos roles en una sola sesión sin conflicto.
```
**Trazabilidad UX:** Wireframes Pantalla 14 (Seguridad RBAC).

---

## ÉPICA 8: Extensiones Cognitivas AI-Native (Cognitive BPMN)
Aborda la integración nativa de Inteligencia Artificial (LLMs, RAG) en el modelado BPMN (Pantalla 6) y la ejecución del iBPMS, evolucionando de procesos secuenciales estáticos a procesos aumentados cognitivamente.

### US-032: Orquestación de IA y Generative Task (RAG)
**Como** Arquitecto Funcional
**Quiero** disponer de tareas especializadas en IA dentro del diseñador BPMN
**Para** modelar flujos donde un Agente de IA analiza documentos y redacta contenido estructurado sin interrumpir el motor lógico de Camunda.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Componentes AI-Native BPMN y Controles
  Scenario: Output Estricto basado en Schema JSON (CA-1 - Opción A)
    Given el Arquitecto configura una "Generative Task (RAG)" en la Pantalla 6
    Then el motor de IA tiene prohibido generar formato visual (HTML/Docs) directamente
    And está forzado a devolver la información estructurada mediante un Esquema JSON (Extraer y Rellenar)
    And el iBPMS fusiona ese JSON con la plantilla inmutable oficial antes de mostrársela al usuario final.

  Scenario: Desbloqueo de Conocimiento PII con Políticas Estrictas (CA-2)
    Given la tarea "Generative Task (RAG)" necesita consultar la Bóveda SGDEA
    When el LLM busca contexto en documentos clasificados como Privados o PII
    Then se permite la lectura para la generación de la respuesta
    But la política de seguridad (RBAC/DLP) enmascara o prohíbe exponer directamente estos datos sensibles al usuario que no tiene dichos privilegios.
    
  Scenario: Vectorización de Conocimiento a Demanda (CA-3)
    Given el Administrador de Conocimiento sube archivos al SGDEA
    Then dispone de un botón "[Actualizar Memoria IA (Embeddings)]"
    And la vectorización a la base de datos vectorial (Ej: Milvus/Pinecone) no ocurre automáticamente en cada subida de archivo para no degradar el rendimiento, sino de forma controlada y explícita.

  Scenario: Botón de Pánico Anti-Alucinaciones (CA-4)
    Given un usuario humano revisa un borrador generado por la IA en su formulario (Pantalla 7)
    Then dispone de un botón global estilo "[👎 Reportar Alucinación / Error IA]"
    And al accionarlo, se cancela el comportamiento automático, se emite una alerta al "Ingeniero de Prompts" y el proceso se redirige al flujo manual por defecto.

  Scenario: Trazabilidad a las Fuentes (Citas Interactivas) (CA-5)
    Given la "Generative Task (RAG)" emite una respuesta argumentativa
    Then el texto debe incluir referencias o hipervínculos a los IDs documentales usados como contexto
    And cuando el usuario hace clic, el iBPMS lo redirige al visor del SGDEA con la sección exacta resaltada, garantizando verificación humana.

  Scenario: Budget Configurable de Tokens LLM (CA-6)
    Given el Arquitecto configura la tarea cognitiva en la Pantalla 6
    Then existe un parámetro limitante de "Budget de Tokens / Consumo Mensual"
    And si el proceso agota su cuota asiganada, se corta el acceso a la IA y el motor enruta a las ramas B (flujos manuales alternativos) automáticamente.

  Scenario: Gobernanza de Prompts Centralizada (CA-7)
    Given la necesidad de alterar las instrucciones base de los Agentes RAG
    Then existe una pantalla separada llamada "Enterprise Prompt Library"
    And solo los usuarios con el rol especializado `prompt_engineer` tienen permisos CRUD sobre estos prompts globales, dejando a los Arquitectos BPMN únicamente con la facultad de consumirlos.

  Scenario: Tolerancia a Fallos Multi-LLM (Failover Pattern) (CA-8)
    Given la tarea "Generative Task (RAG)" está configurada para consumir un modelo principal (Ej: Azure OpenAI GPT-4o)
    When este proveedor primario sufre una caída (Downtime / HTTP 503)
    Then el Agente Orquestador del iBPMS no detiene el proceso inmediatamente
    And realiza un salto automático transparente (Failover) a un modelo de respaldo configurado (Ej: AWS Bedrock Claude 3.5) para garantizar la continuidad operativa.

  Scenario: Auditoría Transparente de IA sin Marcas de Agua (CA-9)
    Given un PDF oficial generado a partir de una plantilla con texto redactado por IA
    When el usuario humano aprueba el texto y el sistema emite el documento final
    Then el documento PDF NO incluye advertencias públicas ni marcas de agua de "Generado por IA" para conservar la imagen corporativa
    And el iBPMS persiste en su base de auditoría interna un registro estricto inmutable indicando "Borrador Generado por IA - Validado y Aceptado bajo responsabilidad del usuario [Nombre] con ID [X]".

  Scenario: Parametrización de Límites de Lectura Comprensiva (CA-10)
    Given un expediente que incluye anexos documentales extremadamente extensos (+500 páginas)
    When el proceso pasa los anexos como contexto al Agente RAG
    Then el Arquitecto BPMN puede haber parametrizado "Límites de Extracción" en la configuración de la tarea cognitiva
    And el sistema recorta inteligentemente el contexto a enviar (Ej: "Evaluar solo las primeras 20 páginas" o "Límite: 100k tokens") previniendo gastos desmesurados de cómputo.

  Scenario: Control Bidireccional de Tono Redaccional (CA-11)
    Given el Arquitecto BPMN arrastra una Generative Task al lienzo en Pantalla 6
    Then el panel de propiedades incluye un Dropdown "Tono de Comunicación" (Ej: Empático, Formal/Legal, Comercial)
    And esta instrucción se inyecta dinámicamente como Sistema al Prompt principal sin requerir que el Arquitecto reescriba el Prompt base de la librería.

  Scenario: Validación Invisible de Doble Agente (Self-Reflection) (CA-12)
    Given el modelo LLM principal genera un borrador de respuesta
    Then en flujos de criticidad alta, el iBPMS enruta temporalmente ese borrador a un segundo "Agente Validador Invisible"
    And si el Validador detecta Alucinaciones graves o violaciones de PII, obliga al modelo principal a reescribir la respuesta internamente antes de presentársela al analista humano en su Workdesk.

  Scenario: Auditoría Legal del Prompt Exacto (CA-13)
    Given que el "Ingeniero de Prompts" altera el prompt oficial corporativo frecuentemente
    When un proceso cognitivo finaliza y guarda la respuesta generada
    Then la base de datos almacena el texto íntegro e inmutable del Prompt específico que se usó en ese milisegundo exacto
    And permitiendo auditorías forenses (¿Qué le ordenamos a la IA ese día?) años después del evento de ejecución.

  Scenario: Bucle de Retroalimentación Humana (RLHF) (CA-14)
    Given el Abogado recibe un borrador generado por la IA en su formulario
    When el Abogado rechaza el texto y lo reescribe manualmente antes de enviar
    Then el iBPMS guarda el par de datos "[Borrador IA Original] vs [Texto Humano Final]" en una base de datos de telemetría MLOps
    And este corpus queda disponible para futuras sesiones de ajuste fino (Fine-tuning) del modelo base corporativo.

  Scenario: Aseguramiento DLP e IT Security en Nube Pública (CA-15)
    Given que el LLM está hospedado fuera de la infraestructura local (Ej: Azure, OpenAI)
    When el iBPMS emite el llamado de red con el contexto (Cuerpo de PQRS)
    Then un interceptor de Seguridad IT / DLP (Data Loss Prevention) evalúa y enmascara PII (Nombres, Cédulas, Tags) reemplazándolos por Hash-Tokens pseudo-anonimizados
    And la IA procesa los hashes, y al devolver la respuesta redactada, el interceptor re-hidrata los Hashes a su valor original PII dentro del perímetro seguro local.

  Scenario: Traducción Activa de Salida (CA-16 - Diferido a V2)
    Given el cliente escribe en un idioma extranjero (Ej: Inglés)
    # NOTA: Diferido a V2. En V1 la IA entiende el inglés pero la instrucción general del Prompt fuerza la respuesta en Español.

  Scenario: Adjuntos Generativos y Bucle de RLHF Documental (CA-17)
    Given una tarea "Generative Task (RAG)" configurada para exportar un archivo .DOCX
    When la IA redacta el contenido y genera el documento asociado al proceso
    Then si el humano no lo acepta y edita el archivo Word subiéndolo de nuevo (o haciendo comentarios)
    And el iBPMS captura el "Delta" (diferencias) entre el documento IA y la corrección humana para usarlo como métrica de retroalimentación de calidad.

  Scenario: Bucle Iterativo por Notas o Comentarios (CA-18)
    Given el usuario revisa el borrador generado por la IA y no está satisfecho
    When en lugar de editarlo manualmente, opta por la revisión guiada
    Then utiliza un panel de "Notas / Comentarios" para instruir correcciones (Ej: "Hazlo más corto y cordial")
    And la tarea cognitiva vuelve a ejecutarse tomando ese comentario humano como contexto mandatorio para el re-intento.

  Scenario: Selección de Modelo a Nivel de Ejecución (CA-19)
    Given el Arquitecto BPMN ha parametrizado "Metadatos de Sugerencia" indicando qué IA usar (Ej: Nivel Inferior)
    When la tarea cognitiva llega al Workdesk del usuario final
    Then el Usuario Ejecutor es quien tiene la potestad final en la UI para elegir qué modelo exacto procesará la solicitud, utilizando la sugerencia como base.

  Scenario: Termómetro de Seguridad (Confidence Score) (CA-20)
    Given el modelo LLM genera una respuesta
    Then el sistema debe mostrar visualmente en el Workdesk un "Confidence Score" (Nivel de Certeza)
    And advirtiendo al revisor humano si la certidumbre matemática de la IA es peligrosamente baja.

  Scenario: Transparencia Cognitiva Continua (Chain of Thought visible) (CA-21)
    Given que el LLM estructura un argumento complejo
    Then el sistema debe solicitar y capturar el "Chain of Thought" (Paso a paso lógico de la IA)
    And exponerlo como un log oculto pero auditable en la metadata de la instancia para que el administrador/humano entienda el "por qué" de la decisión.

  Scenario: Contexto Humano Ad-Hoc en Vivo (CA-22)
    Given la memoria base de la IA (SGDEA) está limitada
    Then el usuario final que está revisando la tarea puede, en tiempo real, adjuntar un PDF local desde su PC
    And ordenar a la IA que reevalúe y genere un nuevo borrador incluyendo ese documento exclusivo y saltándose el RAG tradicional.

  Scenario: Versionamiento y Máquina del Tiempo de Prompts (CA-23)
    Given el modulo Enterprise Prompt Library
    When el Ingeniero de Prompts realiza alteraciones al texto de instrucción
    Then el sistema crea versiones inmutables al estilo Git (v1, v2)
    And existe un mecanismo de reversión instantánea (Rollback) por si el nuevo prompt causa degradación operativa generalizada.

  Scenario: Cola de Procesamiento por Lotes (Batch Dispatcher) (CA-24)
    Given un volumen alto de invocaciones a la IA
    Then el sistema enruta estas tareas a una "Cola de Despacho" paramétrizable por el Administrador de Prompts
    And esta cola maneja límites de concurrencia, reintentos por falla de la API, y estrategias Backoff automáticas.

  Scenario: Interfaz Asíncrona sin Bloqueo de Navegación (CA-25)
    Given la generación de IA está ejecutándose en la Cola de Despacho (CA-24)
    Then la Interfaz de UI presenta un mensaje personalizado indicando que el proceso "Está siendo procesado por IA"
    And NO bloquea al usuario, permitiéndole paralelamente atender otras tareas u operar otras pantallas del iBPMS libremente.

  Scenario: Gatillo Exclusivamente Imperial (Acción Humana) (CA-26)
    Given un flujo de procesamiento que involucra el componente de IA
    Then el sistema tiene una regla arquitectónica imperativa: la invocación a la IA no puede ocurrir por auto-transición 100% de fondo de Camunda
    And exige siempre que el Gatillo (Trigger) originario haya sido el "Clic" explícito de un Usuario Humano en la pantalla precedente o actual, impidiendo escapes automatizados ciegos.

  Scenario: UX de Carga Asíncrona (Prevención de Streaming) (CA-27)
    Given la generación de un documento IA
    Then la interfaz de usuario utiliza un "Loading Spinner" tradicional y peticiones HTTP estándar en lugar de WebSockets (Efecto Máquina de Escribir)
    And priorizando la eficiencia de memoria del servidor web B2B frente a la espectacularidad visual.

  Scenario: Regeneración Parcial por Fragmentos (CA-28)
    Given un borrador extenso generado por la IA en la pantalla del analista
    When el usuario selecciona únicamente un párrafo y hace clic en "Comentar / Corregir"
    Then el iBPMS enruta a la IA exclusivamente el fragmento seleccionado junto con la instrucción humana (Ej: "Haz este párrafo más formal")
    And la IA devuelve el fragmento modificado, fusionándose in-place sin necesidad de reescribir ni gastar tokens en el texto adyacente que ya fue aprobado.

  Scenario: Privacidad de Auditoría Cognitiva (CA-29)
    Given que el proceso generó métricas de "Confidence Score" y "Chain of Thought"
    Then estas métricas son de consumo estrictamente interno
    And por ningún motivo se exponen al Ciudadano Externo en la Pantalla 18 (Portal B2B/B2C).

  Scenario: RAG Multimodal Controlado (V1) (CA-30)
    Given la ingesta de documentos anexos para contextualizar a la IA
    Then el Agente RAG soporta en su V1 la lectura de documentos PDF, DOCX e Imágenes (OCR integrado a la API de visión)
    And excluyendo formalmente notas de voz o video (Diferido a V2).

  Scenario: Eficiencia de Contexto Pre-Empaquetado (IA Amarrada) (CA-31)
    Given una tarea generativa que requiere datos externos (Ej: Saldos ERP)
    Then la IA tiene prohibido usar "Function Calling" autónomo para ir a buscar datos por su cuenta (Gasto excesivo de tokens y memoria de razonamiento)
    And la arquitectura dicta que Camunda Engine, mediante Service Tasks previas y baratas, extraiga la data y se la entregue pre-empaquetada en el Prompt a la IA para que esta se limite únicamente a redactar.
```
**Trazabilidad UX:** Pantalla 6 (BPMN Designer Palette), Pantalla 12 (SGDEA), Pantalla 7 (Form Builder UI).

---

## ÉPICA 9: Hub de Integraciones y Conectores (Integration Hub - Pantalla 11)
Gobierna la comunicación bidireccional entre el motor iBPMS y los sistemas externos (CRM, ERP, SGDEA de terceros como SharePoint), garantizando resiliencia y estandarización mediante conectores reutilizables.

### US-033: Catálogo de API y Mapeo Visual
**Como** Arquitecto Técnico / Interfaz
**Quiero** disponer de un Hub centralizado para configurar conectores HTTP
**Para** asociar de forma visual las variables de procesos BPMN con endpoints externos, gestionando tolerancia a fallos y reglas de seguridad de nivel Enterprise.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: API Connector Configuration and Resiliency
  Scenario: Exclusividad Rest JSON (Delegación a APIM) (CA-1)
    Given la necesidad de interactuar con un sistema Core de tecnología antigua (Ej: SOAP XML)
    Then el Integration Hub del iBPMS emite su comunicación internamente en formato estándar `REST JSON`
    And la arquitectura delega imperativamente la conversión de protocolos a la capa de API Management (APIM) middleware corporativo, manteniendo el iBPMS libre de librerías legacy.

  Scenario: Compatibilidad de Autenticación Segura (CA-2)
    Given que el Arquitecto configura un nuevo Conector en la Pantalla 11
    Then el formulario debe disponer de soporte imperativo para inyección de Headers `Basic Auth` y credenciales `OAuth 2.0 / JWT`
    And estos credenciales deberán estar almacenados en la Bóveda de Secretos encriptada, jamás en texto plano.

  Scenario: Ausencia de Agentes Locales On-Premise (CA-3)
    Given la necesidad de conectar el iBPMS en la Nube con el ERP interno corporativo
    Then el iBPMS no provee "Agentes VPN Inversos" ni demonios de instalación local
    And la arquitectura asume que el acceso infraestructural está resuelto mediante puertos habilitados en el FireWall corporativo bajo responsabilidad exclusiva del área de IT.

  Scenario: Tolerancia a Caídas y Retry Asíncrono (CA-4)
    Given la invocación a la API externa de SharePoint que retorna error HTTP 500
    Then el motor encola la petición fallida en base de datos (Dead Letter Queue controlada)
    And realiza intentos de backoff silenciosos (Ej: cada 5 minutos por 1 hora)
    And si persisten los fallos, enruta el task a modalidad de "Fallback" alertando al analista humano en el Workdesk de la interrupción integrativa.

  Scenario: Data Mapping Gráfico (Drag and Drop) (CA-5)
    Given la configuración de un catálogo de retorno (Ej: CRM devuelve el perfil del cliente)
    Then la interfaz expone visualmente un árbol JSON de variables de entrada a la izquierda vs variables del BPMN a la derecha
    And permite dibujar conexiones (Drag & Drop mapping) sin requerir que el Arquitecto estructure JSONPath a mano.

  Scenario: Trazabilidad y Logs Híbridos (CA-6)
    Given una prueba de conexión fallida por Timeout
    Then la Pantalla 11 emite en su consola técnica el Log Raw inmediato del error para el diseñador
    And las ejecuciones fallidas en producción se delegan adicionalmente al sistema APIM o a la bitácora interna de Kibana para trazabilidad forense.

  Scenario: Validación Profunda de Payload Fantasma (CA-7)
    Given una API mal implementada que devuelve un estatus `HTTP 200 OK` pero el cuerpo del JSON incluye el key `{"error": true, "code": "USER_NOT_FOUND"}`
    Then el Hub permite al Arquitecto declarar "Reglas Límite de Payload" para que el iBPMS separe visualmente si una petición fue exitosa lógicamente (no solo analizando el status header HTTP).

  Scenario: Censura DLP en Logs del Sistema (CA-8)
    Given que la API devuelve inintencionalmente la llave primaria secreta o contraseña de una transacción
    Then la política general de censura y DLP intercepta el contenido saliente hacia el log (`stdout`/Kibana/Pantalla11)
    And enmascara los atributos coincidentes (Hashes visuales ocultos `***`) bloqueando la exposición a un desarrollador o Arquitecto BPMN no autorizado.

  Scenario: Directorio Global Reutilizable de Contenedores (CA-9)
    Given un Arquitecto a punto de conectar la extracción del SharePoint en un proceso
    Then la UI de "Agregar Conector" en la Pantalla 6 ofrece primero un buscador sobre el "Directorio de Conectores Registrados" 
    And permite re-utilizar el conector genérico sin volver a ingresar claves ni endpoints globales, favoreciendo el reciclaje.

  Scenario: Componente Playground de Pruebas (CA-10)
    Given que el Arquitecto terminó el Mapping Drag and Drop
    Then dispone de un botón `[▶️ Run / Probar Conector]` en la misma Pantalla 11
    And puede inyectar variables estáticas Dummy obteniendo el Raw Response de SharePoint/CRM en ese instante para verificar funcionamiento antes de comitear al catálogo.

  Scenario: Parametrización Humana de Timeouts (CA-11)
    Given un conector que apunta a un ERP legacy lento
    Then la interfaz de Pantalla 11 expone un input para definir `Timeout (ms)` exacto por Conector
    And previene que un solo API sature el motor Camunda por quedarse en estado colgante esperando infinitamente.

  Scenario: Lista Negra de Seguridad de Red (SSRF Prevention) (CA-12)
    Given un Arquitecto intentando mapear un Endpoint hacia un host interno malicioso (Ej: `localhost`, `127.0.0.1` o IPs locales de la BD)
    Then el sistema de Validación de Guardrails de IT intercepta el guardado
    And rechaza conectores que apunten a dominios listados en la "Blacklist Confidencial" configurada por el equipo de ciberseguridad.

  Scenario: Parseo Inteligente de Fechas (CA-13)
    Given una respuesta de CRM en donde el campo `birth_date` llega formateado de forma exótica (`20241231`)
    When el usuario arrastra la variable al Drag & Drop
    Then el Hub ofrece una opción "Forzar Formateo de Fecha"
    And convierte automáticamente el valor al estándar corporativo ISO-8601 impidiendo errores de parseo en el motor BPMN más adelante.

  Scenario: Interfaz Multipart/Form-Data para Anexos Pesados (CA-14)
    Given la necesidad imperativa de recuperar y enviar PDFs al SharePoint
    Then el Hub de Integración no se limita únicamente al Content-Type `application/json`
    And provee soporte técnico transparente para subida y descarga asíncrona de Binary Large Objects (BLOBs) mediante `multipart/form-data`.

  Scenario: Agresivo Ahorro de Red por Caché en Memoria (CA-15)
    Given 50 procesos BPMN simultáneos pidiendo un catálogo inmutable (Ej: Catálogo de Sucursales ERP)
    Then el Arquitecto puede encender el "Switch de Caché" interactivo en el Conector
    And parametrizar un TTL (Ej: Valid for 10 min) para que el iBPMS responda instantáneamente desde RAM local evadiendo 49 llamadas de red innecesarias al ERP.

  Scenario: Despliegue Manual de Entornos V1 / Variables V2 (CA-16)
    Given la necesidad de apuntar conectores a Producción (Ej: de `crm-qa` a `crm-prod`)
    Then en el alcance de V1, el Arquitecto de integraciones debe actualizar las URLs manualmente
    # NOTA: Diferido a V2: Orquestación automática transversal mediante Variables de Entorno (`{{crm_base_url}}`).

  Scenario: Autonomía de Firmas Criptográficas de Payload (CA-17)
    Given una integración hacia una pasarela bancaria que exige firma HMAC-SHA256
    Then la configuración del Conector exhibe un panel de `Security Signatures`
    And permite auto-firmar ciegamente el payload adjuntando la rúbrica matemática en los Headers garantizando no-repudio técnico.

  Scenario: Traversado Nativo de Paginación Recursiva (CA-18)
    Given que el CRM expone un listado masivo en páginas pequeñas (offset/limit de a 100)
    Then el Conector es consciente de estructuras de paginación
    And permite configurar la navegación automática "NextPage" hasta obtener el dataset completo sin que el Arquitecto deba modelar un 'For Loop' grotesco en el BPMN.

  Scenario: Versionamiento Estricto No Destructivo (CA-19)
    Given un Arquitecto modificando el Conector "CRM Cliente v1" que está amarrado ya a 50 procesos vivos
    When agrega un campo obligatorio nuevo 
    Then el iBPMS prohíbe el Sobre-escritura instantánea (la cual rompería la empresa)
    And fuerza el guardado estricto como nueva reliquia inmutable "CRM Cliente v2", forzando una migración gradual proceso por proceso.

  Scenario: Refreshing Invisible de Identidades Temporales (CA-20)
    Given una conexión JWT/OAuth2 donde el token de acceso expira a los 60 minutos
    When un proceso se despierta a las 2 horas intentando conectar
    Then el Hub ejecuta internamente, sin intervención humana, el `refresh_token` contra el Identity Provider
    And obtiene un nuevo Token válido, emite la invocación y mantiene el flujo operativo limpio ininterrumpidamente.

  Scenario: Habilitación de Entradas Inbound (Webhooks) (CA-21)
    Given la necesidad de que el iBPMS sea un ente reactivo a sistemas externos (Ej: CRM actualiza un dato on-demand)
    Then la interfaz de Pantalla 11 expone la capacidad de generar dinámicamente "URLs de Webhooks Inbound"
    And asignando tokens generados nativamente para que sistemas externos llamen al iBPMS e interactúen con instancias de proceso activas (Signal Events / Message Events).

  Scenario: Transformación mediante Inyección de Código Custom (CA-22)
    Given una respuesta legacy en XML con estructuras irregulares inmanejables por el mapeo Drag & Drop simple
    Then el conector habilita opcionalmente un editor integrado "Code Injector" (JS/Python seguro - Sandboxed)
    And permitiendo al Arquitecto escribir scripts deterministas obligados a transformar el payload crudo hacia el JSON esperado por el Engine de forma manual.

  Scenario: Tercerización de Tráfico y Encolamiento (Throttling) (CA-23)
    Given picos de transaccionalidad donde 10,000 procesos invocan a un SharePoint externo simultáneamente
    Then el iBPMS delega la contención (Rate Limiting) a la Cola de Mensajería corporativa (Ej: RabbitMQ / Apache Kafka)
    And no asume internamente la gestión masiva de peticiones limitantes para evitar caídas de servidor.

  Scenario: Aislamiento por Seguridad Condicionada (CA-24 - Diferido a V2)
    Given la existencia de Conectores clasificados como "Altamente Confidenciales" (Saldos ERP)
    Then en el alcance actual de V1 todos los arquitectos autenticados en el BPM Designer tienen visibilidad transversal del catálogo de conectores
    # NOTA: Diferido a V2 la securización del catálogo de conectores por roles estrictos de RBAC.

  Scenario: Encriptación de Payload Militar (CA-25)
    Given el mandato de transferir payloads (Cuerpos HTTP) ultra-sensibles (Ej: Historias Clínicas)
    Then el Hub además de forzar TLS (HTTPS) en tránsito
    And posee soporte de auto-cifrado y descifrado nivel Payload utlizando criptografía asimétrica (PGP) garantizando impenetrabilidad absoluta incluso en reposo en logs intermedios de APIM del proveedor.
```
**Trazabilidad UX:** Wireframes Pantalla 11 (Integration Hub).

  Scenario: Privacidad de Auditoría Cognitiva (CA-29)
    Given que el proceso generó métricas de "Confidence Score" y "Chain of Thought"
    Then estas métricas son de consumo estrictamente interno
    And por ningún motivo se exponen al Ciudadano Externo en la Pantalla 18 (Portal B2B/B2C).

  Scenario: RAG Multimodal Controlado (V1) (CA-30)
    Given la ingesta de documentos anexos para contextualizar a la IA
    Then el Agente RAG soporta en su V1 la lectura de documentos PDF, DOCX e Imágenes (OCR integrado a la API de visión)
    And excluyendo formalmente notas de voz o video (Diferido a V2).

  Scenario: Eficiencia de Contexto Pre-Empaquetado (IA Amarrada) (CA-31)
    Given una tarea generativa que requiere datos externos (Ej: Saldos ERP)
    Then la IA tiene prohibido usar "Function Calling" autónomo para ir a buscar datos por su cuenta (Gasto excesivo de tokens y memoria de razonamiento)
    And la arquitectura dicta que Camunda Engine, mediante Service Tasks previas y baratas, extraiga la data y se la entregue pre-empaquetada en el Prompt a la IA para que esta se limite únicamente a redactar.
```
**Trazabilidad UX:** Pantalla 6 (BPMN Designer Palette), Pantalla 12 (SGDEA), Pantalla 7 (Form Builder UI).

---

## ÉPICA 10: Event Driven Architecture & Central Message Broker
Formaliza la infraestructura de encolamiento transversal del iBPMS asegurando que tareas de alta latencia o llamadas a sistemas masivos (Cognitive RAG, APIs externas) no saturen la base de datos relacional ni el pool de hilos de Camunda.

### US-034: Orquestación a través de RabbitMQ
**Como** Administrador de Infraestructura / Backend
**Quiero** delegar el rate-limiting y el encolamiento asíncrono a un Message Broker de grado Enterprise (RabbitMQ)
**Para** garantizar resiliencia extrema frente a picos transaccionales, evitando desbordamientos de memoria (OOM) y caídas de subsistemas.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Central Message Queue Orchestration
  Scenario: Broker Exclusivo de Alta Demanda (CA-1)
    Given la necesidad de procesar transacciones asíncronas pesadas (IA, Mails, Integraciones)
    Then el iBPMS enruta estos eventos imperativamente a `RabbitMQ` (o Kafka) configurado como clúster
    And prohíbe explícitamente el uso de tablas relacionales (SQL) como mecanismo de encolamiento de alto tráfico para prevenir bloqueos de base de datos (Database Deadlocks).

  Scenario: Dashboard Técnico de DLQ (Monitor Visual) (CA-2)
    Given un fallo masivo en un proveedor externo que atasca 5,000 mensajes en la cola de errores
    Then el iBPMS provee una pantalla de monitoreo transversal para el Rol de Administrador IT
    And permite visualizar el tamaño de la 'Dead Letter Queue' (DLQ)
    And expone botones críticos de acción masiva: `[Purgar Cola]` y `[Reintentar Mensajes Forzosamente]`.

  Scenario: Jerarquización de Supervivencia (Priority Queues) (CA-3)
    Given una saturación temporal de procesamiento en los Workers del sistema
    When ingresan simultáneamente eventos VIP (Ej: Notificaciones de aprobaciones financieras críticas) y eventos de latencia tolerable (Ej: Generación RAG de resúmenes)
    Then RabbitMQ clasifica el tráfico en "Priority Queues" pre-configuradas basándose en metadatos del evento
    And asegura que los procesos de Nivel 1 (Críticos) sean desencolados y procesados antes que las tareas de Nivel 3 (Batch), garantizando el SLA de negocio intacto a pesar del cuello de botella global.
```

---

## ÉPICA 11: Bóveda Documental SGDEA (Pantalla 12)
Define la arquitectura de almacenamiento, inmutabilidad y recuperación de documentos (Expedientes y Anexos), integrándose nativamente con SharePoint como gestor documental primario en V1, sentando las bases para Tablas de Retención (TRD) y firmas digitales.

### US-035: Integración SharePoint y Auditoría Documental
**Como** Analista / Auditor de Cumplimiento
**Quiero** que el iBPMS gestione los expedientes en SharePoint manteniendo trazabilidad matemática estricta
**Para** garantizar que la evidencia aportada por clientes o generada por IA sea inmutable, centralizada y legalmente auditable.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: SharePoint Vault and Single Source of Truth
  Scenario: Almacenamiento Delegado Basado en Enlaces (CA-1)
    Given la subida de un documento de 10MB en la Pantalla 16 (Intake)
    Then la arquitectura postula a SharePoint como "Single Source of Truth" físico de los PDFs
    And el iBPMS únicamente almacena en su base de datos relacional la URL directa del activo, su ID referencial y la Metadata de auditoría, evadiendo duplicación de costos de Storage (S3).

  Scenario: Creación Dinámica de Taxonomía Sub-Carpetas (CA-2)
    Given un Arquitecto modelando una captura documental en la Pantalla 6
    When configura la actividad paramétricamente para generar "Casos Independientes"
    Then el iBPMS expone un Pop-Up para definir la ruta base en SharePoint
    And en tiempo de ejecución, el motor invoca el API de SharePoint creando proactivamente la sub-carpeta unívoca para ese expediente (Ej: `/ProcesoA/Caso1234/`) antes de inyectar los documentos.

  Scenario: Elusión de Seguridad Perimetral SharePoint (Service Account) (CA-3)
    Given un Usuario de Negocio que posee Rol de Lectura en el iBPMS pero carece de licencia SharePoint
    Then el módulo documental utiliza un App Registration (Súper Cuenta de Servicios - EntraID) para extraer el PDF del repositorio
    And lo proyecta en la Pantalla 12 evadiendo los bloqueos nativos de SharePoint frente al usuario final.
    # NOTA: Diferido a V2 el "RBAC Cruzado" (User Delegation OAuth2).

  Scenario: Marcado Metadato para Tablas de Retención V1 (CA-4)
    Given la necesidad legal de destruir tutelas tras 5 años (TRD)
    Then en la V1, el iBPMS inyecta una Fecha de Expiración como Metadato estructurado directo a la taxonomía de SharePoint
    And delega la incineración automatizada (Deletion Policies) al motor nativo de Microsoft 365.
    # NOTA: Diferido a V1.2 el cronómetro destructor interno propio del iBPMS.

  Scenario: Inmutabilidad por Versionamiento Incremental (CA-5)
    Given un analista intentando "Reemplazar" un contrato que quedó mal redactado en el sistema
    Then en el expediente de la Pantalla 12 el botón de sobre-escritura destructiva está censurado
    And forcejea la obligatoriedad funcional de subir el nuevo archivo bajo el mecanismo de "Nueva Versión" (v1.1) reteniendo acceso forense e inmutable al borrador v1.0.

  Scenario: Despacho de Integración E-Signature (CA-6)
    Given un documento generado que requiere validez legal del firmante
    Then el Módulo Documental posee el andamiaje (Hooks) para interactuar vía API con proveedores de Firma Digital (Ej: DocuSign/AdobeSign)
    And actualiza el estado del expediente en la Pantalla 12 a "Firmado" una vez los Webhooks Inbound confirman el OTP legal del ciudadano.

  Scenario: Componente Visor Empotrado (Iframe Preview) (CA-7)
    Given la necesidad de leer un anexo para tomar una decisión en un proceso
    Then la interfaz de Tareas (Pantalla 12 empotrada en Workdesk) renderiza un Visor de Documentos Nativos asíncrono
    And impide obligar al analista a descargar el PDF ciegamente hacia las carpetas `Descargas/` locales de su Sistema Operativo, reteniendo el foco en el flujo iBPMS.

  Scenario: Blindaje Criptográfico Anti-Fraude (SHA-256) (CA-8)
    Given la delegación del archivo físico hacia el servidor SharePoint de TI (CA-1)
    Then en el milisegundo anterior a la carga, el iBPMS calcula el HASH criptográfico SHA-256 del binario original
    And sella esta huella matemática inmutablemente en la Base de Datos transaccional del iBPMS para detectar futuras y silenciosas alteraciones directamente en SharePoint.

  Scenario: Lector Óptico Diferido (OCR Zonal) (CA-9)
    Given imágenes de documentos de identidad (Cédulas) escaneadas
    Then en V1 estas se gestionan como Binary/Image objects convencionales
    # NOTA: Diferido a V2 el procesamiento neuronal OCR para extracción estructurada de texto zonal.

  Scenario: Inyección Activa de Metadata de Negocio (CA-10)
    Given el traspaso exitoso del documento PDF hacia la granja SharePoint
    Then el iBPMS adjunta un Payload extendido de Propiedades Personalizadas (Ej: `ibpms_processName`, `ibpms_caseStatus`) al nodo del documento
    And permitiendo a los usuarios externos buscar documentos utilizando las herramientas de búsqueda Nativas de O365 mediante filtros semánticos del negocio.

  Scenario: Límite Infraestructural de Carga y Silencio Parcial (CA-11)
    Given un usuario intentando subir un archivo estúpidamente pesado (Ej: Video 4K de 5GB)
    Then la Pantalla 12 intercepta la carga en el Fronend guiada por un parámetro global `MAX_FILE_SIZE` (Ej: 50MB) configurado por IT
    And emite un error de UI "genérico" o "silencioso" al usuario final (Ej: "Error en la Carga, archivo muy pesado")
    And simultáneamente dispara una alerta técnica detallada en el Log del Administrador del Sistema para auditoría de abusos.

  Scenario: Lista Blanca Estricta de Extensiones (MIME Types) (CA-12)
    Given el riesgo inminente de inyección de Malware (Ej: `.exe`, `.bat`)
    Then la Bóveda SGDEA opera exclusivamente bajo arquitectura de "Lista Blanca" (Whitelist)
    And rechaza radicalmente cualquier archivo que no esté explícitamente parametrizado (Ej: `application/pdf`, `image/jpeg`, `application/msword`).

  Scenario: Visibilidad Transparente de Atributos de Auditoría (CA-13)
    Given la Tabla/Grilla visual del expediente en Pantalla 12
    Then el diseño UI no esconde la data legal
    And expone nativamente en columnas primarias la "Fecha Extrema (Vencimiento TRD)" y el "Hash SHA-256" para que el analista confirme la inmutabilidad física con un solo vistazo.
    # NOTA: Opciones de "Botón del Pánico/Censura de archivos erróneos" diferido a V2.

  Scenario: Consolidación Multi-Anexo (PDF Merge Tool) (CA-14)
    Given un expediente con 10 archivos PDF fragmentados que deben enviarse a una Superintendencia
    Then la Pantalla 12 posee un botón de acción masiva `[Combinar PDFs (Merge)]`
    And el iBPMS compila transitoriamente las páginas de los archivos seleccionados en un único documento maestro PDF sin corromper los originales.

  Scenario: Delegación de Escaneo Anti-Malware (CA-15)
    Given el ingreso de nuevos documentos a la Bóveda
    Then la arquitectura V1 asume ciegamente la robustez de los Defensores Nativos de Microsoft
    And confía en que SharePoint 365 interceptará virus silenciosamente, librando al iBPMS de conectar con AntiVirus dedicados en este MVP.
    # NOTA: Herramientas de "Anotación Gráfica (Highlighting)" sobre PDFs diferidas a V1.2.

  Scenario: Búsqueda Semántica Delegada (Full-Text Search) (CA-16)
    Given un analista utilizando el buscador global del iBPMS para buscar la palabra "Tornillo"
    Then el iBPMS delega la consulta profunda al motor nativo de indexación de SharePoint
    And este último busca el término *dentro del contenido textual* de los PDFs y devuelve los matches, inyectando los resultados en la UI del iBPMS.

  Scenario: Orquestación SGDEA a Inteligencia RAG (Vectorización Segura) (CA-17)
    Given un analista en Pantalla 12 que decide oprimir `[Usar para IA]` sobre un contrato de 100 páginas
    Then el módulo documental envía asíncronamente el ID de ese archivo a la "Cola de Eventos IA (RabbitMQ - CA-34)"
    And el cerebro LLM procede a desencolar y devorar el contenido (si es PDF o WORD habilitado) para poblar su memoria de Embeddings sin congelar la ventana del usuario.
```
**Trazabilidad UX:** Wireframes Pantallas 12, 16 y 6.

---

## ÉPICA 12: Identity Governance & Control de Acceso (Pantalla 14)
Define la arquitectura centralizada de seguridad, delegación y segregación de datos. Establece a la Pantalla 14 como el único "Cuartel General" donde el Súper Administrador orquesta qué Humanos, APIs y Roles pueden interactuar con los procesos modelados en el iBPMS.

### US-036: Matriz de Control de Acceso Basado en Roles (RBAC)
**Como** Oficial de Seguridad de la Información (CISO) / Super Admin
**Quiero** gestionar centralizadamente todos los permisos, perfiles y delegaciones en la Pantalla 14
**Para** garantizar cumplimiento ISO 27001, prevenir accesos no autorizados a datos sensibles y auditar la segregación de funciones (SoD).

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Identity Governance & RBAC Architecture
  Scenario: Hibridación de Roles EntraID vs Locales (CA-1)
    Given una organización iniciando el iBPMS
    Then la Pantalla 14 soporta doble motor de mapeo de identidades
    And permite importar automáticamente Roles/Grupos desde Microsoft EntraID (SSO) 
    And provee un "Fallback" interno para crear y asignar Roles 100% locales en la BD del iBPMS si el cliente tiene baja madurez corporativa.

  Scenario: El Guardián Absoluto (Root Super Admin) (CA-2)
    Given el despliegue inicial (Día Cero) del iBPMS
    Then el sistema inyecta por defecto un único usuario `[Super_Administrador]` inborrable a nivel de base de datos
    And este rol es el único con potestad absoluta para ingresar a la Pantalla 14 y delegar poder (crear otros administradores).

  Scenario: Clonación de Perfiles por Plantilla (CA-3)
    Given la necesidad de dar el mismo set de 15 permisos a 50 asesores nuevos
    Then la Pantalla 14 permite la creación de un `[Rol Plantilla]` lógico que atrapa esos permisos
    And permite asignar ese `[Rol Plantilla]` en bloque a los 50 usuarios con un solo click (Mass Assignment).

  Scenario: Segregación Iniciador vs Ejecutor (CA-4)
    Given la matriz de permisos de la Pantalla 14
    Then expone casillas de verificación (Checkboxes) granulares y explícitas para cada Proceso BPMN publicado
    And diferencia a nivel de base de datos el permiso booleano `can_initiate_process` (Cliente) vs `can_execute_tasks` (Cocinero).

  Scenario: Privacidad Visual de Colas (Data Segregation Local) (CA-5)
    Given dos analistas (Juan y María) pertenecientes al mismo Rol "Analista_Créditos"
    Then al ingresar a su Workdesk (Pantalla 5), la arquitectura forza un filtro de base de datos a nivel de registro (Row-Level Security)
    And garantiza que María SOLO visualice los folios/casos asignados a ella, ocultando tajantemente el trabajo de sus pares a menos que sea una "Cola Compartida Pública".

  Scenario: Herencia de Roles Piramidal (CA-6)
    Given una estructura jerárquica corporativa
    Then la Pantalla 14 permite que el rol `[Gerente_Riesgo]` sea configurado para "Heredar" atómicamente el 100% de los permisos subyacentes del rol `[Analista_Riesgo]` minimizando la redundancia de clics en la matriz.

  Scenario: Inmutabilidad por Desactivación Suave (Soft-Delete) (CA-7)
    Given que el empleado Juan renuncia a la empresa y es desconectado del SSO
    Then el Súper Admin en Pantalla 14 NO puede borrar físicamente (DELETE SQL) la identidad de Juan 
    And el sistema le asigna un sello de `[Usuario Inactivo]`, congelando su estado pero preservando eternamente su nombre en los registros de auditoría de los casos que resolvió en el pasado.

  Scenario: Aprovisionamiento de Transeúntes (Ciudadano Interno) (CA-8)
    Given un empleado recién contratado que se loguea en el iBPMS vía SSO por primera vez
    Then el motor RBAC le auto-provisiona un perfil inofensivo por defecto llamado `[Ciudadano_Interno]`
    And este perfil arranca con capacidades nulas hasta que el Súper Admin configure explícitamente en Pantalla 14 qué Procesos Generales (Ej: Vacaciones) tienen permitida "Autogestión por defecto".

  Scenario: Módulo de Delegación Autónoma Temporal (CA-9)
    Given un Gerente que se marcha a vacaciones por 15 días
    Then la Pantalla 14 le provee un panel de Autogestión (Delegación) para cedar sus poderes a un suplente (Ej: Su asistente)
    And esta cesión de Rol requiere obligatoriamente estampar el `[Rango_de_Fechas]` (Fecha Inicio / Fin) para revocarse automáticamente.
    And toda la transacción de traspaso de poder queda flaggeada transaccionalmente para la bitácora del CISO.

  Scenario: Creación de Robots de Integración (API Keys / Service Accounts) (CA-10)
    Given la necesidad de que el ERP corporativo lance casos en el iBPMS 24/7 sin interacción humana
    Then la Pantalla 14 posee un módulo paralelo de "Cuentas de Servicio M2M"
    And permite generar Tokens Criptográficos (API Keys) atándolos a Roles específicos, prohibiendo que los sistemas externos operen con credenciales de humanos vulnerables.
    
  Scenario: Respeto ciego al Autenticador Perimetral (EntraID MFA) (CA-11)
    Given una tarea crítica que un gerente va a ejecutar en su bandeja
    Then la arquitectura de seguridad V1 asume 100% de confianza en el Token emitido por Microsoft EntraID
    And el iBPMS NO reconstruye un componente duplicado de Doble Factor (MFA) propio en pantalla, delegando esta validación criptográfica al Identity Provider original.

  Scenario: Exclusión de Ocultamiento de Campos (Scope Limit) (CA-12)
    Given un usuario intentando ocultar la columna "Salario" de un formulario en base al rol
    Then la directriz aclara que la Pantalla 14 administra accesos a la "Instancia Completa" (El Formulario entero)
    And delega la responsabilidad técnica de ocultar campos individuales a la algoritmia del Pro-Code Builder (Pantalla 7) durante el diseño del Vue Component.

  Scenario: Desacoplamiento de Roles Estáticos vs Dinámicos (BPMN Lanes) (CA-13)
    Given la asignación de trabajo en el motor Camunda
    Then el módulo de Permisos reconoce y respeta dos vías de asignación: 
    Los Roles Estáticos (Asignados en la Pantalla 14 manualmente de por vida al usuario) y los Roles Dinámicos/Variables (Inyectados en tiempo real por el BPMN a través de *Expression Lanes*).

  Scenario: El Botón Táctico de Exorcismo (Kill-Session) (CA-14)
    Given un evento de despido disciplinario hostil a mediodía
    Then la Pantalla 14 expone un botón rojo `[Revocar Todo y Matar Sesión]` en la ficha del empleado
    And la arquitectura exige que el Backend destruya activamente los JWT almacenados en caché/Redis de ese analista forzando su deslogueo TCP instantáneo, sin tener que esperar que su Token de 1 hora expire.

  Scenario: Bypass Anónimo de Procesos (URLs Públicas) (CA-15)
    Given la necesidad ciudadana de radicar PQRS sin crear cuentas
    Then la Pantalla 14 (y la Configuración del Proceso) cuenta con un Switch Especial `[Permitir Trámite Público]`
    And al activarse, el generador de Formularios expone un endpoint/URL sin requerimiento de Bearer Token en las rutas de Vue Router, posibilitando el inicio ciego de instancias BPMN por navegadores anónimos en internet.

  Scenario: Informes Densos de Fiscalización (Auditoría CISO) (CA-16)
    Given la temporada de Auditoría ISO 27001
    Then la Pantalla 14 permite generar el reporte matrizal de *Identity Governance*
    And compila una sábana CSV / Excel descargable cruzando `[Todos los Usuarios/Robots]` contra `[Todos los Roles Activos]` y `[Todos los Procesos Iniciables/Ejecutables]`.

  Scenario: Traza Indeleble de Otorgamiento (CA-17)
    Given un practicante que misteriosamente amanece con el rol `[Gerente_Financiero]`
    Then la Pestaña de Auditoría de Seguridad de Pantalla 14 muestra el JSON delta exacto
    And expone qué Administrador Ejecutante (ID Humano), en qué milisegundo UTC (Timestamp), inyectó la sobre-escritura de permisos.

  Scenario: Omisión Estricta de Segregación de Funciones Automática (SoD) (CA-18)
    Given la ley del "Quien hace no aprueba"
    Then para el MVP V1, el motor iBPMS NO frena estructuralmente a un humano si el BPMN le enruta "Crear Cheque" y "Aprobar Cheque" al mismo tiempo
    And asume este riesgo operativo difiriendo los motores complejos de "Conflict of Interest Avoidance" a V2, confiando en que el diseño del proceso en Pantalla 6 asigne humanos distintos para el flujo iterativo.
```
**Trazabilidad UX:** Wireframes Pantallas 14, 6, 7 y Workdesk (5).
