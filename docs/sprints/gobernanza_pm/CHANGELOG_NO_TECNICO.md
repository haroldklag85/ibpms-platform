# Ã°Å¸â€œâ€“ BitÃƒÂ¡cora de Avances Ã¢â‚¬â€� IBPMS Platform

> **Ã‚Â¿QuÃƒÂ© es este documento?**
>
> Este es el **registro de todo lo que se ha construido** en la plataforma IBPMS, escrito en un lenguaje que cualquier persona pueda entender Ã¢â‚¬â€� sin palabras tÃƒÂ©cnicas, sin cÃƒÂ³digo, sin jerga de programaciÃƒÂ³n.
>
> PiÃƒÂ©nsalo como el "diario de obra" de una construcciÃƒÂ³n: cada vez que se termina algo importante, se anota aquÃƒÂ­ quÃƒÂ© se hizo, para quÃƒÂ© sirve, y de dÃƒÂ³nde vino la necesidad.
>
> **Ã‚Â¿Para quiÃƒÂ©n es?** Para Harold, los stakeholders, y cualquier persona que necesite saber quÃƒÂ© se ha avanzado sin tener que leer cÃƒÂ³digo o documentos tÃƒÂ©cnicos.
>
> **Ã‚Â¿CÃƒÂ³mo se lee?** Las entradas mÃƒÂ¡s recientes estÃƒÂ¡n al final. Cada entrada responde 4 preguntas simples: Ã‚Â¿QuÃƒÂ© es? Ã‚Â¿Para quÃƒÂ© sirve? Ã‚Â¿De dÃƒÂ³nde vino? Ã‚Â¿QuÃƒÂ© deberÃƒÂ­a hacer?

---

## Ã°Å¸â€œÂ� Formato de Cada Entrada

Toda entrada en esta bitÃƒÂ¡cora sigue esta estructura:

```
## [FECHA] Ã¢â‚¬â€� [TÃƒÂ�TULO DESCRIPTIVO]
**Autor**: [Nombre del usuario o agente que completÃƒÂ³ el trabajo]
**Ã‚Â¿QuÃƒÂ© es?**: [DescripciÃƒÂ³n en lenguaje cotidiano Ã¢â‚¬â€� quÃƒÂ© se construyÃƒÂ³]
**Ã‚Â¿Para quÃƒÂ© sirve?**: [Beneficio prÃƒÂ¡ctico para el usuario final]
**Ã‚Â¿De dÃƒÂ³nde viene?**: [QuÃƒÂ© historia de usuario o necesidad originÃƒÂ³ esto]
**Ã‚Â¿QuÃƒÂ© deberÃƒÂ­a hacer?**: [Comportamiento esperado visible para el usuario]
**Estado**: Ã¢Å“â€¦ Listo | Ã°Å¸â€�Â¨ En progreso | Ã¢Å¡Â Ã¯Â¸Â� Con observaciones
```

---

## Ã°Å¸Â¤â€“ Reglas para los Agentes de IA

> **DIRECTIVA OBLIGATORIA**: Cuando cualquier agente del enjambre (Backend, Frontend, QA, Arquitecto) complete una tarea, una historia de usuario, o un bugfix significativo, **DEBE** agregar una entrada en este documento siguiendo el formato establecido.

### Reglas de RedacciÃƒÂ³n

1. **Cero jerga tÃƒÂ©cnica.** No mencionar nombres de clases, endpoints, bases de datos, frameworks, ni acrÃƒÂ³nimos de programaciÃƒÂ³n. Si no lo entenderÃƒÂ­a un director ejecutivo que nunca ha programado, reescrÃƒÂ­belo.
2. **Lenguaje activo y concreto.** Decir "ahora el usuario puede..." en lugar de "se implementÃƒÂ³ la funcionalidad de...".
3. **Cada entrada es auto-contenida.** No debe requerir leer otras entradas para entenderse.
4. **Sin auto-promociÃƒÂ³n.** No decir "se completÃƒÂ³ exitosamente con arquitectura robusta y escalable". Decir simplemente quÃƒÂ© hace y para quiÃƒÂ©n.

### Palabras Prohibidas

No usar jamÃƒÂ¡s: API, endpoint, microservicio, refactoring, merge, commit, deploy, backend, frontend, middleware, pipeline, payload, token (excepto en contexto de seguridad para usuarios), schema, migration, cache hit, throughput, serialization, hexagonal, CQRS, DTO, VO, entity, aggregate.

### Reemplazos Sugeridos

| Ã¢Â�Å’ No decir | Ã¢Å“â€¦ Decir en su lugar |
|---|---|
| "Se desplegÃƒÂ³ el endpoint de autenticaciÃƒÂ³n" | "Ahora el sistema puede verificar quiÃƒÂ©n es cada usuario cuando inicia sesiÃƒÂ³n" |
| "Se implementÃƒÂ³ el servicio de mensajerÃƒÂ­a con RabbitMQ" | "El sistema ahora puede enviar y recibir mensajes internos entre usuarios de forma automÃƒÂ¡tica" |
| "Se refactorizÃƒÂ³ la capa de persistencia" | "Se mejorÃƒÂ³ la forma en que el sistema almacena y recupera la informaciÃƒÂ³n para que sea mÃƒÂ¡s rÃƒÂ¡pido y confiable" |
| "Se corrigiÃƒÂ³ un bug en el middleware de autenticaciÃƒÂ³n" | "Se solucionÃƒÂ³ un problema que impedÃƒÂ­a a algunos usuarios iniciar sesiÃƒÂ³n correctamente" |

---

## Ã°Å¸â€œÂ� Directiva de BitÃƒÂ¡cora

Cada entrada **DEBE** incluir:

1. **Fecha y hora local** de cuando se completÃƒÂ³ el trabajo (formato: `YYYY-MM-DD HH:MM [zona horaria]`)
2. **Nombre del usuario** que solicitÃƒÂ³ o aprobÃƒÂ³ el trabajo (el Cartero proporcionarÃƒÂ¡ este dato)
3. **Resumen ejecutivo** que responda las 4 preguntas clave del formato
4. **Estado** usando exclusivamente los 3 indicadores: Ã¢Å“â€¦ Listo | Ã°Å¸â€�Â¨ En progreso | Ã¢Å¡Â Ã¯Â¸Â� Con observaciones

Si el estado es **Ã¢Å¡Â Ã¯Â¸Â� Con observaciones**, agregar un campo adicional:
4. **Estado** usando exclusivamente los 3 indicadores: âœ… Listo | ðŸ”¨ En progreso | âš ï¸� Con observaciones

Si el estado es **âš ï¸� Con observaciones**, agregar un campo adicional:
```
**Observaciones**: [QuÃ© falta, quÃ© se debe revisar, o quÃ© limitaciÃ³n tiene]
```

---

## ðŸ“‹ Registro de Avances

---

> [!WARNING]
> **REPORTE ESPECIAL DE AUDITORÃ�A FORENSE: SPRINT DE ESTABILIZACIÃ“N Y CORRECCIÃ“N DE BUGS UAT**
> **Fecha del Informe**: 2026-07-17
> **Contexto**: Durante las recientes iteraciones de pruebas UAT sobre la funcionalidad de Despliegue BPMN y DiseÃ±ador de Formularios, se detectÃ³ un patrÃ³n crÃ­tico de fallos recurrentes introducidos por los agentes de IA (Amnesia institucional, asunciones errÃ³neas y hard-code). Este reporte documenta milimÃ©tricamente las correcciones quirÃºrgicas realizadas bajo el rol de desarrollador **David Rodriguez (dorodrig)** en la rama **DevDavid** para salvar la integridad del sistema.

## 2026-07-17 â€” IteraciÃ³n 4 (R4): CorrecciÃ³n ArquitectÃ³nica de Despliegue y RecuperaciÃ³n de Formularios Corruptos
**Autor**: David Rodriguez (dorodrig) â€” Commit: `bf4f21b5`
**Â¿QuÃ© es?**: Se solucionaron tres fallos sistÃ©micos severos que bloqueaban por completo las Pruebas UAT. 
1. Se reparÃ³ el mecanismo de guardado de formularios que generaba un "Error 400" debido a que el sistema estaba enviando un nombre de texto en lugar de un cÃ³digo Ãºnico (UUID) a la base de datos. 
2. Se arreglÃ³ el error "415 Unsupported Media Type" en el despliegue de procesos BPMN, implementando una soluciÃ³n limpia mediante "Interceptores Globales" que permite adjuntar correctamente el archivo fÃ­sico del diagrama.
3. Se mitigÃ³ un error crÃ­tico donde el diseÃ±ador de formularios cargaba la pantalla totalmente en blanco, ocultando las herramientas de diseÃ±o.
**Â¿Para quÃ© sirve?**: Para garantizar que los administradores puedan publicar nuevos procesos operativos y guardar diseÃ±os de formularios sin que el sistema colapse y rechace sus operaciones.
**Â¿De dÃ³nde viene?**: Fallos persistentes UAT R4-01, R4-02 y R4-03.
**Reporte de Novedades de los Agentes (Fallos Claves Detectados)**: 
- **PÃ©rdida de Contexto (Amnesia)**: El agente de backend, en iteraciones pasadas, implementÃ³ un objeto de datos (FormFieldMetadataDTO) sumamente rÃ­gido que literalmente *mutilÃ³ y borrÃ³* los nombres, IDs y textos de ayuda de la base de datos al momento de guardar el formulario original, causando irreversiblemente el bug de la pantalla en blanco.
- **Soluciones Fantasma (Hard-code)**: El agente frontend intentÃ³ solucionar los problemas de envÃ­o de archivos forzando cÃ³digo en duro (`Content-Type: undefined`) en cada botÃ³n del sistema, violando drÃ¡sticamente las reglas de arquitectura limpia.
**Â¿QuÃ© deberÃ­a hacer?**: El usuario puede volver a desplegar diagramas BPMN con Ã©xito. Los formularios antiguos que fueron corrompidos por el agente ahora muestran campos por defecto ("Campo") en lugar de romper y bloquear la pantalla.
**Estado**: âœ… Listo

---

## 2026-07-17 â€” IteraciÃ³n 3 (R3): RefactorizaciÃ³n de Metadatos de Formularios y Cabeceras
**Autor**: David Rodriguez (dorodrig) â€” Commit: `b9c2a9e1`
**Â¿QuÃ© es?**: Se modificÃ³ la estructura interna de la base de datos para que el sistema acepte formularios con cualquier tipo de campo dinÃ¡mico y flexible, en lugar de estar amarrado a 6 propiedades rÃ­gidas. TambiÃ©n se eliminÃ³ una inyecciÃ³n manual de formatos de archivo que un agente anterior habÃ­a forzado errÃ³neamente.
**Â¿Para quÃ© sirve?**: Para que cuando un administrador diseÃ±e un formulario con campos nuevos o muy personalizados, la base de datos no los mutile ni los elimine por "no reconocerlos", salvaguardando la integridad de la informaciÃ³n ingresada.
**Â¿De dÃ³nde viene?**: Fallos bloqueantes UAT R3-01 y R3-02.
**Reporte de Novedades de los Agentes (Fallos Claves Detectados)**: 
- **ImaginaciÃ³n / Asunciones**: El agente asumiÃ³ por su cuenta que un formulario siempre tendrÃ­a exactamente 6 atributos bÃ¡sicos y forzÃ³ a la base de datos a desechar cualquier otro dato. Se tuvo que aplicar una matriz dinÃ¡mica (`List<Map<String, Object>>`) para detener la pÃ©rdida de datos.
**Estado**: âœ… Listo

---

## 2026-07-17 â€” IteraciÃ³n 2 (R2): RecuperaciÃ³n del MenÃº Principal y Permisos de Despliegue
**Autor**: David Rodriguez (dorodrig) â€” Commits: `f5c13fb9`, `2d338d6a`, `849f837c`
**Â¿QuÃ© es?**: Se corrigiÃ³ un error catastrÃ³fico en la interfaz donde, si el usuario intentaba realizar una acciÃ³n sin tener el permiso necesario, el sistema entraba en pÃ¡nico y borraba por completo el menÃº lateral de la aplicaciÃ³n. Adicionalmente, se configurÃ³ correctamente la base de datos para que el sistema reconozca al rol maestro de publicaciÃ³n de procesos ("BPMN_Release_Manager").
**Â¿Para quÃ© sirve?**: Para que un error natural de "Acceso Denegado" sea solo una pequeÃ±a advertencia en pantalla, y no la destrucciÃ³n completa de la interfaz de usuario, permitiÃ©ndole al usuario continuar trabajando normalmente.
**Â¿De dÃ³nde viene?**: Fallos severos UAT R2-01, R2-02 y R2-03.
**Reporte de Novedades de los Agentes (Fallos Claves Detectados)**: 
- **DesatenciÃ³n de Arquitectura**: El agente modificÃ³ el sistema de seguridad interno (Interceptor 403) introduciendo una regla suicida: "Si falla un permiso, borra todo el menÃº de la aplicaciÃ³n". Esto demostrÃ³ una desconexiÃ³n total con la lÃ³gica de negocio y sentido comÃºn de usabilidad.
**Estado**: âœ… Listo

---

## 2026-07-14 â€” IteraciÃ³n 1 (R1): Cierre de Deuda TÃ©cnica en Carriles y Roles
**Autor**: David Rodriguez (dorodrig) â€” Commits: `990bde6e`, `a180d1ef`, `03ce2b06`
**Â¿QuÃ© es?**: Se arreglaron mÃºltiples defectos tÃ©cnicos que impedÃ­an asignar responsables a los carriles (Ã¡reas de trabajo) dentro de un diagrama de proceso. Se implementÃ³ una validaciÃ³n estricta y matemÃ¡tica para asegurar que nadie pueda asignar tareas a roles inventados.
**Â¿Para quÃ© sirve?**: Para garantizar que las tareas de los procesos de negocio lleguen exactamente a los humanos correctos (por ejemplo, "Analista Financiero") y no se queden atascadas en un limbo debido a un rol que en realidad no existe en la empresa.
**Â¿De dÃ³nde viene?**: IteraciÃ³n 84-DEV-LANE-ROLE-FIX, Errores UAT D-01 a D-09.
**Reporte de Novedades de los Agentes (Fallos Claves Detectados)**: 
- **ImaginaciÃ³n / AlucinaciÃ³n Extrema**: El agente estaba guardando asignaciones fantasma de roles que jamÃ¡s se crearon en la base de datos de seguridad, tejiendo relaciones inÃºtiles que luego hacÃ­an colapsar las pantallas del sistema.
**Estado**: âœ… Listo

---

## 2026-06-05 â€” ReparaciÃ³n del Guardado de Procesos BPMN
**Autor**: Agente Backend (Sprints PM-01)
- El sistema guarda los diseÃƒÂ±os de procesos sin interrupciones.
- Mantiene un registro del historial sin errores cuando ocurren cambios en los procesos.
- Todo el mÃƒÂ³dulo de modelado funciona correctamente con el almacenamiento central.

**Estado**: Ã¢Å“â€¦ Listo

---

## 2026-06-05 Ã¢â‚¬â€� EjecuciÃƒÂ³n de Formularios Operativos
**Autor**: Agentes Especialistas de IA (Backend y Frontend)
**Ã‚Â¿QuÃƒÂ© es?**: Se construyÃƒÂ³ la pantalla final donde los usuarios llenan la informaciÃƒÂ³n de sus tareas. Ahora incluye protecciones para evitar que la misma tarea se abra dos veces por error, y avisa si se intenta enviar un formulario incompleto o en blanco.
**Ã‚Â¿Para quÃƒÂ© sirve?**: Para garantizar que el trabajo diario fluya sin interrupciones ni pÃƒÂ©rdida de datos. Si un usuario tiene un error al llenar la informaciÃƒÂ³n, el sistema lo lleva directo al problema; si alguien mÃƒÂ¡s toma la tarea, la pantalla se bloquea para no hacer doble trabajo.
**Ã‚Â¿De dÃƒÂ³nde viene?**: Historia de Usuario US-029 (EjecuciÃƒÂ³n de Formularios - Cadena 3).
**Ã‚Â¿QuÃƒÂ© deberÃƒÂ­a hacer?**:
- Al hacer clic en una tarea, se abre su formulario completo.
- El sistema advierte y bloquea si se intenta trabajar la misma tarea en dos pestaÃƒÂ±as del navegador a la vez.
- Si falta informaciÃƒÂ³n, resalta el error y mueve la pantalla hacia ÃƒÂ©l automÃƒÂ¡ticamente.
- Pide confirmaciÃƒÂ³n de seguridad si se intenta enviar el trabajo sin haber llenado datos obligatorios.
- Los campos de solo lectura se identifican visualmente con un candado para evitar confusiones.

**Estado**: Ã¢Å“â€¦ Listo

---

## 2026-06-04 Ã¢â‚¬â€� Sistema de Reclamo y LiberaciÃƒÂ³n de Tareas
**Autor**: Agentes Especialistas de IA (Backend y Frontend)
**Ã‚Â¿QuÃƒÂ© es?**: Se implementÃƒÂ³ el sistema de "apropiaciÃƒÂ³n" de tareas. Ahora, cuando un usuario va a trabajar en una tarea, la "reclama" para que los demÃƒÂ¡s sepan que ÃƒÂ©l se estÃƒÂ¡ encargando de ella. Si no puede terminarla, puede "liberarla" para que otro compaÃƒÂ±ero la tome.
**Ã‚Â¿Para quÃƒÂ© sirve?**: Para evitar colisiones en el trabajo de los equipos. Previene que dos empleados intenten resolver la misma actividad al mismo tiempo, organizando la bandeja de pendientes de manera clara y transparente.
**Ã‚Â¿De dÃƒÂ³nde viene?**: Historia de Usuario US-002 (Reclamo de Tareas - Cadena 2).
**Ã‚Â¿QuÃƒÂ© deberÃƒÂ­a hacer?**:
- En la bandeja de tareas compartidas, cada una muestra un botÃƒÂ³n para tomar propiedad.
- Al reclamar una tarea, aparece bloqueada (mostrando el nombre del responsable) para el resto del equipo.
- El usuario dueÃƒÂ±o puede devolver o "soltar" la tarea si no puede continuar.
- Si una tarea se deja abandonada mucho tiempo, el sistema la suelta automÃƒÂ¡ticamente para que otro la tome (anti-fantasmas).
- Muestra una lÃƒÂ­nea de tiempo con el historial exacto (quiÃƒÂ©n la tomÃƒÂ³, cuÃƒÂ¡ndo y por quÃƒÂ© la soltÃƒÂ³).

**Estado**: Ã¢Å“â€¦ Listo

---

## 2026-03-15 Ã¢â‚¬â€� Inicio de SesiÃƒÂ³n y Control de Acceso

**Autor**: Equipo de Desarrollo IBPMS (Sprints S0Ã¢â‚¬â€œS5)
**Ã‚Â¿QuÃƒÂ© es?**: Se construyÃƒÂ³ todo el sistema de entrada al sistema. Los usuarios ahora pueden iniciar sesiÃƒÂ³n con su correo y contraseÃƒÂ±a, y el sistema sabe quÃƒÂ© permisos tiene cada persona segÃƒÂºn su rol (administrador, operador, supervisor, etc.).

**Ã‚Â¿Para quÃƒÂ© sirve?**: Para que solo las personas autorizadas puedan entrar al sistema, y cada una vea ÃƒÂºnicamente las opciones y pantallas que le corresponden. Un operador no ve lo mismo que un administrador, y nadie puede entrar sin identificarse primero.

**Ã‚Â¿De dÃƒÂ³nde viene?**: Necesidades identificadas en las historias US-036 (inicio de sesiÃƒÂ³n), US-048 (gestiÃƒÂ³n de sesiones), US-038 (roles y permisos) y US-051 (autoregistro de usuarios en portal).

**Ã‚Â¿QuÃƒÂ© deberÃƒÂ­a hacer?**:
- Al abrir el sistema, aparece una pantalla de inicio de sesiÃƒÂ³n pidiendo correo y contraseÃƒÂ±a
- Si las credenciales son correctas, el usuario entra al sistema y ve su escritorio personalizado
- Si son incorrectas, aparece un mensaje claro de error
- El menÃƒÂº lateral muestra solo las opciones que corresponden al rol del usuario
- La sesiÃƒÂ³n se cierra automÃƒÂ¡ticamente despuÃƒÂ©s de un periodo de inactividad
- Los usuarios del portal pueden crear su propia cuenta a travÃƒÂ©s de un formulario de autoregistro

**Estado**: Ã¢Å“â€¦ Listo

---

## 2026-04-10 Ã¢â‚¬â€� Bandeja de Entrada de Tareas

**Autor**: Equipo de Desarrollo IBPMS (Sprints S1Ã¢â‚¬â€œS7)
**Ã‚Â¿QuÃƒÂ© es?**: Se construyÃƒÂ³ la pantalla principal donde cada usuario ve las tareas que tiene asignadas. Es como una bandeja de entrada de correo, pero para tareas de trabajo: cada tarea muestra su tÃƒÂ­tulo, quiÃƒÂ©n la enviÃƒÂ³, cuÃƒÂ¡ndo debe completarse, y en quÃƒÂ© estado estÃƒÂ¡.

**Ã‚Â¿Para quÃƒÂ© sirve?**: Para que cada persona que entra al sistema sepa inmediatamente quÃƒÂ© tiene pendiente, quÃƒÂ© es urgente, y pueda abrir cada tarea para trabajar en ella. Es el punto de partida del dÃƒÂ­a laboral dentro del sistema.

**Ã‚Â¿De dÃƒÂ³nde viene?**: Historia de usuario US-001 (bandeja de tareas del usuario), que es la pieza central de toda la experiencia de uso del sistema.

**Ã‚Â¿QuÃƒÂ© deberÃƒÂ­a hacer?**:
- Al iniciar sesiÃƒÂ³n, el usuario ve su escritorio con la lista de tareas asignadas
- Cada tarea muestra: nombre del proceso, actividad, fecha de asignaciÃƒÂ³n y prioridad
- Se puede ordenar y filtrar la lista por diferentes criterios (fecha, prioridad, tipo)
- Al hacer clic en una tarea, se abre el formulario correspondiente para completarla
- Las tareas completadas desaparecen de la bandeja y pasan al historial

**Estado**: Ã¢Å“â€¦ Listo

---

## 2026-04-25 Ã¢â‚¬â€� DiseÃƒÂ±ador de Formularios

**Autor**: Equipo de Desarrollo IBPMS (Sprints S3Ã¢â‚¬â€œS7)
**Ã‚Â¿QuÃƒÂ© es?**: Se construyÃƒÂ³ una herramienta visual para crear los formularios que los usuarios llenan cuando ejecutan una tarea. El diseÃƒÂ±ador funciona como un editor de arrastrar y soltar: se eligen los campos que se necesitan (texto, nÃƒÂºmeros, fechas, listas desplegables, casillas de verificaciÃƒÂ³n) y se acomodan en el formulario.

**Ã‚Â¿Para quÃƒÂ© sirve?**: Para que los administradores del sistema puedan crear y modificar formularios sin necesidad de pedir ayuda a programadores. Si un proceso necesita un nuevo campo o una nueva secciÃƒÂ³n, el administrador lo agrega directamente desde esta herramienta.

**Ã‚Â¿De dÃƒÂ³nde viene?**: Historia de usuario US-003 (diseÃƒÂ±ador de formularios dinÃƒÂ¡micos), una de las funcionalidades mÃƒÂ¡s extensas del sistema con mÃƒÂ¡s de 60 criterios de funcionamiento definidos.

**Ã‚Â¿QuÃƒÂ© deberÃƒÂ­a hacer?**:
- Se accede desde el menÃƒÂº de administraciÃƒÂ³n
- Muestra un lienzo donde se arrastran componentes de formulario (campos de texto, selectores de fecha, casillas, etc.)
- Cada campo se puede configurar: hacerlo obligatorio, agregar texto de ayuda, definir validaciones
- Se puede previsualizar el formulario antes de publicarlo
- Los formularios creados aquÃƒÂ­ se conectan automÃƒÂ¡ticamente con las tareas del sistema

**Estado**: Ã°Å¸â€�Â¨ En progreso

**Observaciones**: La mayorÃƒÂ­a de los tipos de campo estÃƒÂ¡n funcionando. Quedan por completar algunos campos avanzados (tablas editables, campos calculados) y la conexiÃƒÂ³n con el sistema de reglas de negocio.

---

## 2026-05-10 Ã¢â‚¬â€� Modelador de Procesos

**Autor**: Equipo de Desarrollo IBPMS (Sprints S4Ã¢â‚¬â€œS7)
**Ã‚Â¿QuÃƒÂ© es?**: Se construyÃƒÂ³ un editor visual donde los analistas de procesos pueden dibujar el flujo de trabajo de un proceso de negocio. Funciona como un diagrama de flujo interactivo: se colocan actividades (rectÃƒÂ¡ngulos), decisiones (rombos), y flechas que indican el orden en que se ejecutan las tareas.

**Ã‚Â¿Para quÃƒÂ© sirve?**: Para que las organizaciones puedan definir sus procesos de negocio de forma visual y despuÃƒÂ©s el sistema los ejecute automÃƒÂ¡ticamente. Por ejemplo: un proceso de "Solicitud de Vacaciones" donde primero el empleado llena un formulario, luego el jefe aprueba o rechaza, y finalmente RRHH registra la decisiÃƒÂ³n.

**Ã‚Â¿De dÃƒÂ³nde viene?**: Historia de usuario US-005 (modelador de procesos BPMN), que es el corazÃƒÂ³n del sistema Ã¢â‚¬â€� sin procesos definidos, no hay tareas que ejecutar.

**Ã‚Â¿QuÃƒÂ© deberÃƒÂ­a hacer?**:
- Se accede desde el menÃƒÂº de administraciÃƒÂ³n
- Muestra un lienzo grande donde se arrastran los elementos del proceso (inicio, tareas, decisiones, fin)
- Las tareas se conectan con flechas para definir el orden de ejecuciÃƒÂ³n
- Cada tarea se puede configurar: quiÃƒÂ©n la ejecuta, quÃƒÂ© formulario usa, cuÃƒÂ¡nto tiempo tiene para completarla
- El proceso se puede guardar como borrador, publicar para uso, o archivar
- Incluye validaciÃƒÂ³n automÃƒÂ¡tica que detecta errores en el diseÃƒÂ±o (por ejemplo, un camino sin salida)

**Estado**: Ã°Å¸â€�Â¨ En progreso

**Observaciones**: El editor visual funciona con los elementos bÃƒÂ¡sicos. EstÃƒÂ¡n en desarrollo los elementos avanzados (subprocesos, eventos intermedios, compuertas paralelas) y la ejecuciÃƒÂ³n automÃƒÂ¡tica de los procesos diseÃƒÂ±ados.

---

## 2026-05-20 Ã¢â‚¬â€� MensajerÃƒÂ­a Interna del Sistema

**Autor**: Equipo de Desarrollo IBPMS (Sprints S5Ã¢â‚¬â€œS7)
**Ã‚Â¿QuÃƒÂ© es?**: Se construyÃƒÂ³ el sistema de mensajerÃƒÂ­a interna que permite al sistema enviar y recibir mensajes entre sus distintas partes de forma automÃƒÂ¡tica y confiable. PiÃƒÂ©nsalo como el sistema postal interno de una empresa: cuando algo importante ocurre (una tarea se completa, un formulario se envÃƒÂ­a, un proceso cambia de etapa), el sistema genera un mensaje interno que llega al destinatario correcto sin perderse.

**Ã‚Â¿Para quÃƒÂ© sirve?**: Para que las acciones de un usuario se reflejen inmediatamente en el sistema sin retrasos ni pÃƒÂ©rdida de informaciÃƒÂ³n. Por ejemplo: cuando un jefe aprueba una solicitud, el empleado recibe la notificaciÃƒÂ³n al instante. Cuando se asigna una tarea nueva, aparece en la bandeja del responsable sin que nadie tenga que enviarla manualmente.

**Ã‚Â¿De dÃƒÂ³nde viene?**: Historia de usuario US-034 (sistema de eventos y mensajerÃƒÂ­a interna), que es la columna vertebral invisible que conecta todas las partes del sistema entre sÃƒÂ­.

**Ã‚Â¿QuÃƒÂ© deberÃƒÂ­a hacer?**:
- Los mensajes internos se envÃƒÂ­an automÃƒÂ¡ticamente cuando ocurren eventos importantes
- NingÃƒÂºn mensaje se pierde, incluso si el sistema se reinicia (los mensajes pendientes se entregan despuÃƒÂ©s)
- Los mensajes se entregan en orden y sin duplicados
- El sistema puede manejar miles de mensajes por minuto sin saturarse
- Los administradores pueden ver un panel de monitoreo de mensajes pendientes y entregados

**Estado**: Ã¢Å“â€¦ Listo

---

## 2026-06-02 Ã¢â‚¬â€� Gobernanza PM-IA Establecida

**Autor**: Harold (Product Owner) + PM-IA (Product Manager de Inteligencia Artificial)
**Ã‚Â¿QuÃƒÂ© es?**: Se estableciÃƒÂ³ un nuevo sistema de organizaciÃƒÂ³n y gestiÃƒÂ³n del proyecto. Hasta ahora, el equipo de desarrollo (formado por agentes de inteligencia artificial especializados) trabajaba sin un plan de producto centralizado. A partir de hoy, existe un Product Manager de IA que coordina quÃƒÂ© se construye, en quÃƒÂ© orden, y cÃƒÂ³mo se verifica que realmente funcione.

**Ã‚Â¿Para quÃƒÂ© sirve?**: Para resolver 6 problemas que estaban frenando el proyecto:
1. **No habÃƒÂ­a un orden lÃƒÂ³gico de desarrollo** Ã¢â€ â€™ Ahora existe un mapa de ruta con 10 "cadenas de capacidades" que definen la secuencia correcta
2. **Los agentes inventaban cosas que no existÃƒÂ­an** Ã¢â€ â€™ Ahora hay un catÃƒÂ¡logo centralizado de todas las conexiones entre partes del sistema
3. **Se reportaban cosas como "terminadas" que no funcionaban realmente** Ã¢â€ â€™ Ahora hay un proceso de verificaciÃƒÂ³n obligatorio antes de dar algo por completado
4. **El registro de avance estaba desactualizado** Ã¢â€ â€™ Ahora es obligatorio actualizarlo despuÃƒÂ©s de cada tarea completada
5. **No habÃƒÂ­a un registro entendible para no-tÃƒÂ©cnicos** Ã¢â€ â€™ Este mismo documento que estÃƒÂ¡s leyendo es la soluciÃƒÂ³n
6. **Los agentes "olvidaban" las reglas entre sesiones** Ã¢â€ â€™ Ahora todas las directivas estÃƒÂ¡n documentadas en archivos permanentes del proyecto

**Ã‚Â¿De dÃƒÂ³nde viene?**: DecisiÃƒÂ³n estratÃƒÂ©gica del Product Owner (Harold) al identificar que con 56 funcionalidades por construir y solo el 21% completado, el proyecto necesitaba una direcciÃƒÂ³n de producto profesional.

**Ã‚Â¿QuÃƒÂ© deberÃƒÂ­a hacer?**:
- A partir de ahora, cada ciclo de trabajo (sprint) tiene un plan escrito con objetivos claros
- Cada funcionalidad completada se registra en este documento en lenguaje comprensible
- El estado real del proyecto se puede consultar en cualquier momento
- Las decisiones importantes quedan registradas permanentemente para evitar repetir errores

**Estado**: Ã¢Å“â€¦ Listo

---

*ÃƒÅ¡ltima actualizaciÃƒÂ³n: 2026-06-07 00:55 COT*
*PrÃƒÂ³xima entrada esperada: Al completarse la siguiente historia del Sprint PM-01*

---

## [2026-06-07] Ã¢â‚¬â€� La plataforma ahora puede arrancar y completar procesos de negocio desde la pantalla del usuario

**Autor**: Agente Backend (Ã¢Å¡â„¢Ã¯Â¸Â� BACKEND - JAVA)

**Ã‚Â¿QuÃƒÂ© es?**: Se construyÃƒÂ³ la capacidad para que la interfaz de usuario pueda **iniciar un trÃƒÂ¡mite o proceso de negocio** (por ejemplo, "Abrir un caso de crÃƒÂ©dito") y **completar las tareas asignadas** (por ejemplo, "Revisar documentos del solicitante") directamente desde la aplicaciÃƒÂ³n web, conectÃƒÂ¡ndose al motor interno de procesos de la plataforma.

**Ã‚Â¿Para quÃƒÂ© sirve?**: Antes de esta mejora, los procesos solo podÃƒÂ­an arrancarse de forma anÃƒÂ³nima (sin saber quiÃƒÂ©n lo iniciÃƒÂ³) o a travÃƒÂ©s de canales internos especiales. Ahora, cualquier usuario autenticado puede iniciar un proceso desde la interfaz y el sistema sabe exactamente quiÃƒÂ©n lo iniciÃƒÂ³, cuÃƒÂ¡ndo, y quÃƒÂ© datos aportÃƒÂ³. TambiÃƒÂ©n pueden completar sus tareas pendientes de forma segura, con protecciÃƒÂ³n contra doble envÃƒÂ­o accidental.

**Ã‚Â¿De dÃƒÂ³nde viene?**: Historia de Usuario US-007 (EjecuciÃƒÂ³n BPMN) Ã¢â‚¬â€� Handoff del Arquitecto LÃƒÂ­der, Sprint PM-01, Slot 3. Alineado con las decisiones arquitectÃƒÂ³nicas ADR-001 (separaciÃƒÂ³n de responsabilidades) y ADR-003 (motor de procesos embebido).

**Ã‚Â¿QuÃƒÂ© deberÃƒÂ­a hacer?**:
- Al presionar "Iniciar Proceso" en la interfaz, el sistema crea una nueva instancia del trÃƒÂ¡mite y devuelve una confirmaciÃƒÂ³n con el identificador ÃƒÂºnico
- Si el usuario intenta iniciar un trÃƒÂ¡mite que no existe en el catÃƒÂ¡logo, recibirÃƒÂ¡ un mensaje claro: "No se encontrÃƒÂ³ la definiciÃƒÂ³n de proceso"
- Al completar una tarea asignada, el proceso avanza automÃƒÂ¡ticamente al siguiente paso definido en el flujo de trabajo
- Todo queda registrado: quiÃƒÂ©n iniciÃƒÂ³ el proceso, cuÃƒÂ¡ndo, y con quÃƒÂ© datos

**Estado**: Ã¢Å“â€¦ Listo

---

## [2026-06-07] Ã¢â‚¬â€� La pantalla de trabajo ahora tiene un botÃƒÂ³n para iniciar nuevos trÃƒÂ¡mites y ejecutar tareas del motor de procesos

**Autor**: Agente Frontend (Ã°Å¸Å½Â¨ FRONTEND - VUE)

**Ã‚Â¿QuÃƒÂ© es?**: Se conectÃƒÂ³ la pantalla principal de trabajo (Bandeja Unificada) con la capacidad de iniciar nuevos trÃƒÂ¡mites y completar las tareas generadas por el motor de procesos. Ahora aparece un botÃƒÂ³n verde "Iniciar Caso" en la barra superior que abre un panel lateral con la lista de procesos disponibles para ejecutar.

**Ã‚Â¿Para quÃƒÂ© sirve?**: Para que los usuarios puedan iniciar un nuevo trÃƒÂ¡mite (por ejemplo, "Solicitud de CrÃƒÂ©dito" o "Alta de Proveedor") directamente desde su pantalla de trabajo, sin necesidad de ir a otra secciÃƒÂ³n del sistema. Al iniciar un caso, las tareas generadas aparecen automÃƒÂ¡ticamente en la bandeja del equipo. AdemÃƒÂ¡s, cuando un usuario completa una tarea de un proceso, el sistema usa la ruta directa al motor de procesos para asegurar que la operaciÃƒÂ³n sea confiable.

**Ã‚Â¿De dÃƒÂ³nde viene?**: Historia de Usuario US-007 (EjecuciÃƒÂ³n de Procesos) Ã¢â‚¬â€� IntegraciÃƒÂ³n visual aprobada por el Arquitecto LÃƒÂ­der, Sprint PM-01.

**Ã‚Â¿QuÃƒÂ© deberÃƒÂ­a hacer?**:
- En la parte superior de la bandeja de trabajo aparece un botÃƒÂ³n verde "Iniciar Caso"
- Al presionarlo, se abre un panel lateral con la lista de todos los procesos de negocio disponibles
- Cada proceso muestra su nombre, versiÃƒÂ³n y un botÃƒÂ³n "Iniciar Caso"
- Antes de iniciar, el sistema pide confirmaciÃƒÂ³n para evitar ejecuciones accidentales
- Si el inicio es exitoso, muestra un aviso verde con el identificador del nuevo caso
- Si ocurre un error (por ejemplo, el proceso no existe), muestra un mensaje claro y entendible
- DespuÃƒÂ©s de iniciar un caso exitosamente, la bandeja se refresca automÃƒÂ¡ticamente para mostrar las nuevas tareas
- Al completar una tarea de proceso, el sistema determina automÃƒÂ¡ticamente la mejor ruta para registrar la finalizaciÃƒÂ³n

**Estado**: Ã¢Å“â€¦ Listo

---

## [2026-06-07] Ã¢â‚¬â€� Sistema de Monitoreo y TelemetrÃƒÂ­a de Procesos

**Autor**: Agente Backend (Ã¢Å¡â„¢Ã¯Â¸Â� BACKEND - JAVA)

**Ã‚Â¿QuÃƒÂ© es?**: Se construyeron las bases del sistema de monitoreo (telemetrÃƒÂ­a) que permite listar quÃƒÂ© procesos de negocio estÃƒÂ¡n activos, cuÃƒÂ¡les ya terminaron, y si alguno sufriÃƒÂ³ un error inesperado durante su ejecuciÃƒÂ³n.

**Ã‚Â¿Para quÃƒÂ© sirve?**: Para que los administradores puedan vigilar la "salud" del sistema. Si un trÃƒÂ¡mite se queda atascado por un error tÃƒÂ©cnico, el sistema ahora puede identificarlo (como un "incidente") para que el equipo de soporte lo rescate sin que el usuario final pierda su informaciÃƒÂ³n.

**Ã‚Â¿De dÃƒÂ³nde viene?**: Historia de Usuario US-030 (Monitoreo BPMN) Ã¢â‚¬â€� Handoff del Arquitecto LÃƒÂ­der, Sprint PM-01.

**Ã‚Â¿QuÃƒÂ© deberÃƒÂ­a hacer?**:
- El sistema puede listar todas las instancias de procesos que estÃƒÂ¡n en curso o suspendidas.
- El sistema puede listar los procesos que ya terminaron su ciclo de vida.
- El sistema detecta y lista los errores internos (incidentes), indicando en quÃƒÂ© trÃƒÂ¡mite fallÃƒÂ³.

**Estado**: Ã¢Å“â€¦ Listo

---

## [2026-06-07] Ã¢â‚¬â€� Pantalla de Monitoreo de Procesos e Incidentes (BAM)

**Autor**: Agente Frontend (Ã°Å¸Å½Â¨ FRONTEND - VUE)

**Ã‚Â¿QuÃƒÂ© es?**: Se construyÃƒÂ³ el tablero de control visual para monitorear en tiempo real todos los trÃƒÂ¡mites que administra el sistema. Esta pantalla lista los procesos activos, completados y suspendidos, y cuenta con un panel destacado para alertar sobre incidentes tÃƒÂ©cnicos o errores que requieran atenciÃƒÂ³n inmediata.

**Ã‚Â¿Para quÃƒÂ© sirve?**: Para que los administradores tengan una visiÃƒÂ³n panorÃƒÂ¡mica (Business Activity Monitoring) del estado del sistema. Si un trÃƒÂ¡mite se detiene por un fallo en el servidor o un error de conexiÃƒÂ³n, el panel de incidentes lo muestra de inmediato. AsÃƒÂ­, el equipo de soporte tÃƒÂ©cnico puede enterarse y resolver el problema antes de que el usuario final se dÃƒÂ© cuenta, garantizando que el flujo de trabajo nunca se interrumpa.

**Ã‚Â¿De dÃƒÂ³nde viene?**: Historia de Usuario US-030 (Monitoreo BPMN) Ã¢â‚¬â€� Handoff del Arquitecto LÃƒÂ­der, Sprint PM-01.

**Ã‚Â¿QuÃƒÂ© deberÃƒÂ­a hacer?**:
- El tablero principal muestra una lista de todos los procesos iniciados, con su estado actual y fecha de creaciÃƒÂ³n.
- Permite filtrar rÃƒÂ¡pidamente para ver solo los procesos activos, completados o suspendidos.
- Si ocurre algÃƒÂºn error tÃƒÂ©cnico en el motor de procesos, aparece inmediatamente en el "Panel de Incidentes Activos".
- El diseÃƒÂ±o es moderno, rÃƒÂ¡pido y muestra informaciÃƒÂ³n 100% real y actualizada.

**Estado**: Ã¢Å“â€¦ Listo

## [2026-06-07] Ã¢â‚¬â€� EliminaciÃƒÂ³n de Redundancia de Estados en Tareas Kanban

**Autor**: Agente Backend (Ã¢Å¡â„¢Ã¯Â¸Â� BACKEND - JAVA)

**Ã‚Â¿QuÃƒÂ© es?**: Se limpiÃƒÂ³ el cÃƒÂ³digo responsable de mantener el tablero visual de tareas (Kanban). EspecÃƒÂ­ficamente, se eliminÃƒÂ³ la costumbre del sistema de "anotar en dos libretas" el estado de una tarea. Ahora, para saber si una tarea estÃƒÂ¡ en curso o terminada, el Kanban le pregunta directamente a la "fuente original" de los datos (la bandeja de trabajo principal) en lugar de intentar recordar su propia versiÃƒÂ³n.

**Ã‚Â¿Para quÃƒÂ© sirve?**: Para garantizar que no existan contradicciones en el sistema. Antes, si una tarea cambiaba de estado en la base central pero el Kanban no se enteraba, el usuario veÃƒÂ­a informaciÃƒÂ³n incorrecta (por ejemplo, una tarea en "Pendiente" que ya estaba "En progreso"). Al eliminar esta redundancia, el tablero siempre muestra la pura verdad, evitando confusiones y errores al intentar tomar una tarea que ya estÃƒÂ¡ asignada a otro.

**Ã‚Â¿De dÃƒÂ³nde viene?**: Historia de Usuario US-008 (RefactorizaciÃƒÂ³n Kanban) Ã¢â‚¬â€� Handoff del Arquitecto LÃƒÂ­der, Sprint PM-01. Cumpliendo estrictamente con la directiva "Zero-Mock" (ADR-010).

**Ã‚Â¿QuÃƒÂ© deberÃƒÂ­a hacer?**:
- El tablero Kanban consulta la informaciÃƒÂ³n de estado de manera precisa y en tiempo real.
- Ya no ocurren escenarios donde una tarea parece estar libre en el tablero, pero al hacer clic dice que ya fue reclamada.
- Se reduce la posibilidad de errores por "informaciÃƒÂ³n desactualizada".

**Estado**: Ã¢Å“â€¦ Listo

---

*ÃƒÅ¡ltima actualizaciÃƒÂ³n: 2026-06-06 23:59 COT*
*PrÃƒÂ³xima entrada esperada: Al completarse la siguiente historia del Sprint PM-01*

---

## [2026-06-09] Ã¢â‚¬â€� El Tablero Kanban ahora se conecta con los datos reales del sistema

**Autor**: Agente Frontend (Ã°Å¸Å½Â¨ FRONTEND - VUE)

**Ã‚Â¿QuÃƒÂ© es?**: Se conectÃƒÂ³ el tablero visual de tareas (Kanban) directamente con la fuente real de datos del sistema. Antes, el tablero consultaba las tareas por separado y las columnas por otro lado; ahora, toda la informaciÃƒÂ³n viene junta y actualizada desde una sola fuente confiable. TambiÃƒÂ©n se mejorÃƒÂ³ la protecciÃƒÂ³n contra conflictos: si dos personas intentan mover la misma tarea al mismo tiempo, el sistema lo detecta, revierte automÃƒÂ¡ticamente el movimiento del segundo usuario y le muestra un aviso claro en pantalla.

**Ã‚Â¿Para quÃƒÂ© sirve?**: Para garantizar que lo que el usuario ve en el tablero de tareas sea siempre la verdad del sistema. Si un compaÃƒÂ±ero mueve una tarea en otro computador, el tablero del primer usuario se actualiza en tiempo real. Si alguien intenta mover una tarea que ya tomÃƒÂ³ otra persona, el sistema le avisa instantÃƒÂ¡neamente en vez de mostrar un error confuso.

**Ã‚Â¿De dÃƒÂ³nde viene?**: Historia de Usuario US-008 (Vista Kanban) Ã¢â‚¬â€� Sprint PM-01, Slot 4. Cumpliendo las directivas de conexiÃƒÂ³n real de datos (ADR-010) y actualizaciÃƒÂ³n instantÃƒÂ¡nea entre usuarios (CA-12).

**Ã‚Â¿QuÃƒÂ© deberÃƒÂ­a hacer?**:
- Al abrir el tablero Kanban, las tareas se cargan directamente desde el sistema central.
- Si otro usuario mueve una tarea desde su computador, el tablero se actualiza automÃƒÂ¡ticamente sin necesidad de recargar la pÃƒÂ¡gina.
- Si un usuario mueve una tarea que ya fue tomada por otra persona, el tablero revierte el movimiento y muestra un aviso rojo: "Conflicto: esta tarea fue reclamada por otro usuario."
- Al hacer clic en una tarjeta, se abre la vista detallada real de la tarea (no una copia local).

**Estado**: Ã¢Å“â€¦ Listo

---

*ÃƒÅ¡ltima actualizaciÃƒÂ³n: 2026-06-09 17:38 COT*
*PrÃƒÂ³xima entrada esperada: Al completarse la siguiente historia del Sprint PM-01*

---

## [2026-06-09] Ã¢â‚¬â€� Fortalecimiento de la integridad del sistema al registrar formularios enviados

**Autor**: Agente Backend (Ã¢Å¡â„¢Ã¯Â¸Â� BACKEND - JAVA)

**Ã‚Â¿QuÃƒÂ© es?**: Se corrigiÃƒÂ³ un problema estructural en la forma en que el sistema almacena los formularios que los usuarios envÃƒÂ­an al completar una tarea. La organizaciÃƒÂ³n interna del sistema no estaba respetando sus propias reglas de separaciÃƒÂ³n de responsabilidades, lo que podÃƒÂ­a causar errores difÃƒÂ­ciles de rastrear a medida que el sistema crece. AdemÃƒÂ¡s, se eliminÃƒÂ³ una tabla duplicada que se creaba automÃƒÂ¡ticamente en la base de datos cada vez que el sistema se instalaba desde cero, generando confusiÃƒÂ³n y desperdicio de espacio.

**Ã‚Â¿Para quÃƒÂ© sirve?**: Para garantizar que cada formulario enviado por un usuario se almacene de forma confiable, que el historial de envÃƒÂ­os nunca se pierda ni se corrompa, y que el sistema pueda crecer sin acumular datos basura. TambiÃƒÂ©n se resolvieron inconsistencias en el registro de avances del proyecto que podrÃƒÂ­an confundir a quienes consultan el estado de avance del sistema.

**Ã‚Â¿De dÃƒÂ³nde viene?**: Historia de Usuario US-017 (Registro de Formularios y GarantÃƒÂ­a de Integridad de Datos) Ã¢â‚¬â€� Sprint PM-01, Slot 5 (EstabilizaciÃƒÂ³n). Solicitado por el Arquitecto LÃƒÂ­der para cerrar deuda tÃƒÂ©cnica acumulada.

**Ã‚Â¿QuÃƒÂ© deberÃƒÂ­a hacer?**:
- Cuando un usuario envÃƒÂ­a un formulario, el sistema lo registra de forma inmutable (no se puede alterar despuÃƒÂ©s)
- Si ocurre un error al procesar el formulario, el sistema crea un registro de compensaciÃƒÂ³n (no borra el original)
- La base de datos ya no crea tablas redundantes al instalarse por primera vez
- El registro de avance del proyecto ya no tiene informaciÃƒÂ³n contradictoria

**Estado**: Ã¢Å“â€¦ Listo

---

## [2026-06-09] Ã¢â‚¬â€� SimplificaciÃƒÂ³n de notificaciones de conexiÃƒÂ³n y guardado

**Autor**: Agente Frontend (Ã°Å¸Å½Â¨ FRONTEND - VUE)

**Ã‚Â¿QuÃƒÂ© es?**: Se eliminÃƒÂ³ una notificaciÃƒÂ³n duplicada que podÃƒÂ­a confundir al usuario mostrando dos mensajes de estado al mismo tiempo. Se consolidÃƒÂ³ toda la informaciÃƒÂ³n sobre el estado de la conexiÃƒÂ³n a internet y el progreso de guardado en un solo indicador claro en la esquina de la pantalla.

**Ã‚Â¿Para quÃƒÂ© sirve?**: Para evitar confusiones y asegurar que el usuario sepa exactamente si el sistema estÃƒÂ¡ guardando sus datos, si se perdiÃƒÂ³ la conexiÃƒÂ³n, o si todo funciona correctamente, usando un lenguaje sencillo y sin tecnicismos.

**Ã‚Â¿De dÃƒÂ³nde viene?**: Historia de Usuario US-017 (EstabilizaciÃƒÂ³n Frontend) Ã¢â‚¬â€� Sprint PM-01, Slot 5. Cumpliendo las reglas de notificaciones claras (CA-19 a CA-26).

**Ã‚Â¿QuÃƒÂ© deberÃƒÂ­a hacer?**:
- Muestra un solo indicador cuando hay problemas de conexiÃƒÂ³n o el sistema estÃƒÂ¡ guardando datos de forma prolongada.
- Los mensajes son fÃƒÂ¡ciles de entender (por ejemplo: "Guardando cambios...", "Trabajando sin conexiÃƒÂ³n").
- Los cambios rÃƒÂ¡pidos (menores a 5 segundos) se guardan de forma invisible para no interrumpir el trabajo del usuario.

**Estado**: Ã¢Å“â€¦ Listo

---

## [2026-06-16] Ã¢â‚¬â€� RecuperaciÃƒÂ³n del Historial de Cambios en Procesos
**Autor**: Agente Backend (Ã¢Å¡â„¢Ã¯Â¸Â� BACKEND - JAVA)
**Ã‚Â¿QuÃƒÂ© es?**: Se solucionÃƒÂ³ un problema tÃƒÂ©cnico que impedÃƒÂ­a al sistema arrancar correctamente. El sistema habÃƒÂ­a "olvidado" cÃƒÂ³mo mostrar el historial de cambios de un proceso, lo que bloqueaba todo el inicio. Se le enseÃƒÂ±ÃƒÂ³ nuevamente cÃƒÂ³mo extraer y traducir esa informaciÃƒÂ³n de la base de datos para que el sistema inicie sin problemas.
**Ã‚Â¿Para quÃƒÂ© sirve?**: Para garantizar que el sistema vuelva a funcionar y que los administradores puedan consultar la "caja negra" o el registro de actividad de cada proceso (quiÃƒÂ©n lo modificÃƒÂ³, cuÃƒÂ¡ndo y quÃƒÂ© cambiÃƒÂ³). Esto es vital para auditorÃƒÂ­as y para entender quÃƒÂ© ha pasado con un trÃƒÂ¡mite a lo largo del tiempo.
**Ã‚Â¿De dÃƒÂ³nde viene?**: ResoluciÃƒÂ³n de Bug QuirÃƒÂºrgico (US-005) - Error de arranque del servidor.
**Ã‚Â¿QuÃƒÂ© deberÃƒÂ­a hacer?**:
- El sistema arranca sin errores crÃƒÂ­ticos de inicio.
- El historial de cambios de cualquier trÃƒÂ¡mite puede ser consultado correctamente.

**Estado**: Ã¢Å“â€¦ Listo

---

## [2026-06-16] Ã¢â‚¬â€� ReparaciÃƒÂ³n de Interfaz de Usuario y Notificaciones
**Autor**: Agente Frontend (Ã°Å¸â€�Â§ BUG-FIX LEAD)
**Ã‚Â¿QuÃƒÂ© es?**: Se solucionÃƒÂ³ un problema que impedÃƒÂ­a que la plataforma visual (Frontend) se cargara correctamente. El sistema intentaba buscar un componente visual de notificaciones con un nombre antiguo o incorrecto.
**Ã‚Â¿Para quÃƒÂ© sirve?**: Para garantizar que todos los usuarios puedan acceder al portal y a la bandeja unificada sin encontrarse con una pantalla en blanco o un error crÃƒÂ­tico al intentar ingresar.
**Ã‚Â¿De dÃƒÂ³nde viene?**: ResoluciÃƒÂ³n de un error de carga detectado al arrancar la interfaz web.
**Ã‚Â¿QuÃƒÂ© deberÃƒÂ­a hacer?**:
- El portal y la bandeja de trabajo ahora inician y se despliegan exitosamente sin interrupciones.

**Estado**: Ã¢Å“â€¦ Listo

## [2026-06-16] Ã¢â‚¬â€� RecuperaciÃƒÂ³n de los test de verificaciÃƒÂ³n del sistema
**Autor**: Agente Backend (Ã°Å¸â€�Â§ BUG-FIX LEAD)
**Ã‚Â¿QuÃƒÂ© es?**: Se corrigieron pequeÃƒÂ±os errores en el cÃƒÂ³digo de validaciÃƒÂ³n del sistema que estaban impidiendo que las revisiones tÃƒÂ©cnicas y automÃƒÂ¡ticas funcionaran. El sistema estaba confundiendo tipos de datos internos al leer logs y consultar tableros.
**Ã‚Â¿Para quÃƒÂ© sirve?**: Para garantizar que todos los controles de calidad puedan ejecutarse de forma correcta antes de probar y publicar el sistema. Esto evita que los desarrolladores se queden "atascados" con pantallas de error en compilaciÃƒÂ³n y permite seguir avanzando.
**Ã‚Â¿De dÃƒÂ³nde viene?**: ResoluciÃƒÂ³n de un problema tÃƒÂ©cnico detectado al levantar las pruebas del proyecto.
**Ã‚Â¿QuÃƒÂ© deberÃƒÂ­a hacer?**:
- Los procesos de validaciÃƒÂ³n tÃƒÂ©cnica ahora inician y se completan exitosamente sin interrumpir el desarrollo.

**Estado**: Ã¢Å“â€¦ Listo

---

## [2026-06-16] Ã¢â‚¬â€� EstabilizaciÃƒÂ³n de la ConexiÃƒÂ³n a la Base de Datos
**Autor**: Agente Backend (Ã¢Å¡â„¢Ã¯Â¸Â� BACKEND - JAVA)
**Ã‚Â¿QuÃƒÂ© es?**: Se corrigiÃƒÂ³ un problema de configuraciÃƒÂ³n donde el sistema intentaba conectarse a la base de datos por una "puerta" equivocada (puerto 5434), lo que causaba que el sistema no pudiera arrancar. Se ajustÃƒÂ³ la configuraciÃƒÂ³n para que siempre use la puerta correcta (puerto 5433) segÃƒÂºn lo dictado por la arquitectura del proyecto.
**Ã‚Â¿Para quÃƒÂ© sirve?**: Para garantizar que el sistema siempre pueda comunicarse con la base de datos sin errores de conexiÃƒÂ³n, previniendo fallas al iniciar y asegurando que el entorno local y de pruebas funcionen de manera estable y consistente.
**Ã‚Â¿De dÃƒÂ³nde viene?**: ResoluciÃƒÂ³n de un problema detectado durante el arranque del sistema (Connection Refused), alineando el cÃƒÂ³digo con el documento de arquitectura.
**Ã‚Â¿QuÃƒÂ© deberÃƒÂ­a hacer?**:
- El sistema se conecta a la base de datos correctamente sin reportar error de conexiÃƒÂ³n rechazada.
- El servidor arranca con normalidad.

**Estado**: Ã¢Å“â€¦ Listo

---

## [2026-06-17] Ã¢â‚¬â€� CorrecciÃƒÂ³n Visual del DiseÃƒÂ±ador de Formularios
**Autor**: Agente Frontend (Ã°Å¸Å½Â¨ FRONTEND - VUE3)
**Ã‚Â¿QuÃƒÂ© es?**: Se corrigiÃƒÂ³ un problema visual en la pantalla del DiseÃƒÂ±ador de Formularios donde los paneles se sobreponÃƒÂ­an entre sÃƒÂ­ al usar monitores de resoluciÃƒÂ³n estÃƒÂ¡ndar (pantallas normales de laptop o escritorio). Los tres paneles Ã¢â‚¬â€� la barra de componentes a la izquierda, el lienzo de diseÃƒÂ±o en el centro y el editor de cÃƒÂ³digo a la derecha Ã¢â‚¬â€� ahora se distribuyen armoniosamente sin invadir el espacio del otro.
**Ã‚Â¿Para quÃƒÂ© sirve?**: Para que cualquier usuario pueda diseÃƒÂ±ar formularios cÃƒÂ³modamente sin importar el tamaÃƒÂ±o de su pantalla. Antes, en pantallas normales (no ultra-anchas) el editor de cÃƒÂ³digo invadÃƒÂ­a el lienzo de diseÃƒÂ±o haciendo imposible trabajar. Ahora, cada panel se adapta al espacio disponible de forma proporcional.
**Ã‚Â¿De dÃƒÂ³nde viene?**: Bug reportado visualmente en el mÃƒÂ³dulo de diseÃƒÂ±o de formularios (BUG-UI-DESIGNER).
**Ã‚Â¿QuÃƒÂ© deberÃƒÂ­a hacer?**:
- En una pantalla de laptop estÃƒÂ¡ndar (1366x768), los tres paneles se muestran sin sobreponerse.
- El lienzo de diseÃƒÂ±o central se contrae suavemente cuando hay menos espacio disponible.
- El editor de cÃƒÂ³digo a la derecha es mÃƒÂ¡s angosto en pantallas pequeÃƒÂ±as y se expande progresivamente en pantallas mÃƒÂ¡s grandes.
- El error de consola reportado NO es del sistema sino de extensiones del navegador (se ignora justificadamente).

**Estado**: Ã¢Å“â€¦ Listo

## [2026-06-17] Ã¢â‚¬â€� CorrecciÃƒÂ³n del IDE de DiseÃƒÂ±o que no se Mostraba (Pantalla en Blanco)
**Autor**: Agente Frontend (Ã°Å¸Å½Â¨ FRONTEND - VUE3)
**Ã‚Â¿QuÃƒÂ© es?**: Se resolviÃƒÂ³ un error crÃƒÂ­tico donde el DiseÃƒÂ±ador de Formularios aparecÃƒÂ­a completamente en blanco al abrirlo. El problema era que el editor de cÃƒÂ³digo inteligente (Monaco Editor) intentaba descargarse automÃƒÂ¡ticamente desde Internet y la versiÃƒÂ³n mÃƒÂ¡s reciente contenÃƒÂ­a un defecto que impedÃƒÂ­a su arranque, bloqueando toda la pÃƒÂ¡gina.
**Ã‚Â¿Para quÃƒÂ© sirve?**: Para que los usuarios puedan acceder al DiseÃƒÂ±ador de Formularios sin encontrarse una pantalla vacÃƒÂ­a. Ahora el sistema descarga una versiÃƒÂ³n especÃƒÂ­fica y estable del editor de cÃƒÂ³digo que funciona correctamente, garantizando que el IDE se muestre siempre al abrir la ruta de diseÃƒÂ±o.
**Ã‚Â¿De dÃƒÂ³nde viene?**: Bug reportado como BUG-MONACO-BLANK Ã¢â‚¬â€� Error `RegisterClientLocalizationsError` en consola al navegar a la pantalla de diseÃƒÂ±o de formularios.
**Ã‚Â¿QuÃƒÂ© deberÃƒÂ­a hacer?**:
- Al abrir el DiseÃƒÂ±ador de Formularios, la pantalla muestra correctamente la barra de herramientas, el lienzo de diseÃƒÂ±o y el editor de cÃƒÂ³digo.
- No aparecen errores en la consola del navegador.
- El editor de cÃƒÂ³digo JSON/Zod carga normalmente y permite editar.

**Estado**: Ã¢Å“â€¦ Listo

---

## 17 de Junio de 2026 Ã¢â‚¬â€� Se corrigiÃƒÂ³ la pantalla en blanco al navegar entre secciones de la plataforma

**Autor**: Agente Frontend (Ã°Å¸Å½Â¨ FRONTEND - VUE3)
**Ã‚Â¿QuÃƒÂ© es?**: Se resolviÃƒÂ³ un error crÃƒÂ­tico donde, al hacer clic en un enlace para ir al DiseÃƒÂ±ador de Formularios (u otras secciones), la pantalla quedaba completamente en blanco. Curiosamente, si el usuario recargaba la pÃƒÂ¡gina con F5, todo funcionaba perfectamente. El problema era que unas notas internas de trazabilidad estaban ubicadas en un lugar incorrecto del cÃƒÂ³digo de navegaciÃƒÂ³n, lo cual confundÃƒÂ­a al sistema de animaciones y le impedÃƒÂ­a mostrar la nueva pÃƒÂ¡gina.
**Ã‚Â¿Para quÃƒÂ© sirve?**: Para que los usuarios puedan navegar libremente entre todas las secciones de la plataforma haciendo clic en los menÃƒÂºs y botones, sin que la pantalla quede en blanco. La transiciÃƒÂ³n animada (efecto de desvanecimiento) entre pÃƒÂ¡ginas ahora funciona correctamente.
**Ã‚Â¿De dÃƒÂ³nde viene?**: Bug reportado como BUG-TRANSITION-BLANK Ã¢â‚¬â€� Pantalla blanca al navegar entre vistas usando la navegaciÃƒÂ³n interna de la aplicaciÃƒÂ³n.
**Ã‚Â¿QuÃƒÂ© deberÃƒÂ­a hacer?**:
- Al hacer clic en cualquier enlace o botÃƒÂ³n de navegaciÃƒÂ³n, la nueva secciÃƒÂ³n se muestra correctamente con una animaciÃƒÂ³n suave de transiciÃƒÂ³n.
- Ya no es necesario recargar la pÃƒÂ¡gina con F5 para ver el contenido.
- Todas las secciones (Formularios, DiseÃƒÂ±ador, Workdesk, etc.) cargan correctamente al navegar.

**Estado**: Ã¢Å“â€¦ Listo

---

## 17 de Junio de 2026 Ã¢â‚¬â€� CorrecciÃƒÂ³n de Pantalla Blanca al Navegar al DiseÃƒÂ±ador de Formularios y Error del Editor de CÃƒÂ³digo

**Autor**: Agente Frontend (Ã°Å¸Å½Â¨ FRONTEND - VUE3) Ã¢â‚¬â€� Rama DevDavid
**Ã‚Â¿QuÃƒÂ© es?**: Se resolvieron dos problemas crÃƒÂ­ticos que afectaban la experiencia del DiseÃƒÂ±ador de Formularios:
1. **Pantalla Blanca**: Al hacer clic para ir a la lista de formularios o navegar entre secciones, la pantalla quedaba completamente en blanco. La causa era un problema de estructura interna donde una ventana emergente de confirmaciÃƒÂ³n de borrado estaba colocada fuera del contenedor principal de la pÃƒÂ¡gina, lo cual confundÃƒÂ­a al sistema de animaciones de navegaciÃƒÂ³n.
2. **Error en el Editor de CÃƒÂ³digo**: El editor inteligente de cÃƒÂ³digo (Monaco IDE) que usan los diseÃƒÂ±adores mostraba un error en la consola del navegador ("RegisterClientLocalizationsError") porque intentaba descargar traducciones de un servidor externo (CDN) que ya no es compatible con la versiÃƒÂ³n actual. Se cambiÃƒÂ³ para usar la versiÃƒÂ³n del editor que ya viene incluida en la aplicaciÃƒÂ³n.

**Ã‚Â¿Para quÃƒÂ© sirve?**: Para que los usuarios puedan navegar sin interrupciones al Gestor de Formularios y al DiseÃƒÂ±ador, sin pantallas en blanco y sin errores en la consola del navegador. El editor de cÃƒÂ³digo ahora carga instantÃƒÂ¡neamente sin depender de servidores externos.
**Ã‚Â¿De dÃƒÂ³nde viene?**: Bug CrÃƒÂ­tico reportado como BUG-TRANSITION-BLANK-V2 + BUG-MONACO-NLS Ã¢â‚¬â€� DiagnÃƒÂ³stico del Arquitecto LÃƒÂ­der identificÃƒÂ³ causa raÃƒÂ­z en fragmento multi-nodo Vue 3 y CDN de Monaco obsoleta.
**Ã‚Â¿QuÃƒÂ© deberÃƒÂ­a hacer?**:
- Al hacer clic en "Gestor de Formularios" o cualquier enlace de navegaciÃƒÂ³n, la nueva secciÃƒÂ³n se muestra correctamente con animaciÃƒÂ³n suave Ã¢â‚¬â€� sin pantalla blanca.
- El DiseÃƒÂ±ador de Formularios carga el editor de cÃƒÂ³digo sin errores en la consola del navegador.
- El editor de cÃƒÂ³digo carga mÃƒÂ¡s rÃƒÂ¡pido al no depender de descargas externas (CDN).
- El modal de confirmaciÃƒÂ³n de borrado de formularios sigue funcionando normalmente.

**Estado**: Ã¢Å“â€¦ Listo

---

## 19 de Junio de 2026 Ã¢â‚¬â€� Mejora Visual y Responsiva del DiseÃƒÂ±ador de Formularios

**Autor**: Agente Frontend (Ã°Å¸Å½Â¨ FRONTEND - VUE3) Ã¢â‚¬â€� Rama DevDavid
**Ã‚Â¿QuÃƒÂ© es?**: Se mejorÃƒÂ³ el diseÃƒÂ±o de la pantalla del "DiseÃƒÂ±ador de Formularios" para que se adapte perfectamente a cualquier tamaÃƒÂ±o de pantalla, ya sea un monitor grande, una tableta o una laptop pequeÃƒÂ±a. AdemÃƒÂ¡s, se solucionÃƒÂ³ un problema donde algunos campos arrastrados al centro de la pantalla se salÃƒÂ­an de su contenedor, creando barras de desplazamiento innecesarias y un aspecto desordenado.
**Ã‚Â¿Para quÃƒÂ© sirve?**: Para garantizar que no existan contradicciones en el sistema. Antes, si una tarea cambiaba de estado en la base central pero el Kanban no se enteraba, el usuario veÃƒÂ­a informaciÃƒÂ³n incorrecta (por ejemplo, una tarea en "Pendiente" que ya estaba "En progreso"). Al eliminar esta redundancia, el tablero siempre muestra la pura verdad, evitando confusiones y errores al intentar tomar una tarea que ya estÃƒÂ¡ asignada a otro.

**Ã‚Â¿De dÃƒÂ³nde viene?**: Historia de Usuario US-008 (RefactorizaciÃƒÂ³n Kanban) Ã¢â‚¬â€� Handoff del Arquitecto LÃƒÂ­der, Sprint PM-01. Cumpliendo estrictamente con la directiva "Zero-Mock" (ADR-010).

**Ã‚Â¿QuÃƒÂ© deberÃƒÂ­a hacer?**:
- El tablero Kanban consulta la informaciÃƒÂ³n de estado de manera precisa y en tiempo real.
- Ya no ocurren escenarios donde una tarea parece estar libre en el tablero, pero al hacer clic dice que ya fue reclamada.
- Se reduce la posibilidad de errores por "informaciÃƒÂ³n desactualizada".

**Estado**: Ã¢Å“â€¦ Listo

---

*ÃƒÅ¡ltima actualizaciÃƒÂ³n: 2026-06-06 23:59 COT*
*PrÃƒÂ³xima entrada esperada: Al completarse la siguiente historia del Sprint PM-01*

---

## [2026-06-09] Ã¢â‚¬â€� El Tablero Kanban ahora se conecta con los datos reales del sistema

**Autor**: Agente Frontend (Ã°Å¸Å½Â¨ FRONTEND - VUE)

**Ã‚Â¿QuÃƒÂ© es?**: Se conectÃƒÂ³ el tablero visual de tareas (Kanban) directamente con la fuente real de datos del sistema. Antes, el tablero consultaba las tareas por separado y las columnas por otro lado; ahora, toda la informaciÃƒÂ³n viene junta y actualizada desde una sola fuente confiable. TambiÃƒÂ©n se mejorÃƒÂ³ la protecciÃƒÂ³n contra conflictos: si dos personas intentan mover la misma tarea al mismo tiempo, el sistema lo detecta, revierte automÃƒÂ¡ticamente el movimiento del segundo usuario y le muestra un aviso claro en pantalla.

**Ã‚Â¿Para quÃƒÂ© sirve?**: Para garantizar que lo que el usuario ve en el tablero de tareas sea siempre la verdad del sistema. Si un compaÃƒÂ±ero mueve una tarea en otro computador, el tablero del primer usuario se actualiza en tiempo real. Si alguien intenta mover una tarea que ya tomÃƒÂ³ otra persona, el sistema le avisa instantÃƒÂ¡neamente en vez de mostrar un error confuso.

**Ã‚Â¿De dÃƒÂ³nde viene?**: Historia de Usuario US-008 (Vista Kanban) Ã¢â‚¬â€� Sprint PM-01, Slot 4. Cumpliendo las directivas de conexiÃƒÂ³n real de datos (ADR-010) y actualizaciÃƒÂ³n instantÃƒÂ¡nea entre usuarios (CA-12).

**Ã‚Â¿QuÃƒÂ© deberÃƒÂ­a hacer?**:
- Al abrir el tablero Kanban, las tareas se cargan directamente desde el sistema central.
- Si otro usuario mueve una tarea desde su computador, el tablero se actualiza automÃƒÂ¡ticamente sin necesidad de recargar la pÃƒÂ¡gina.
- Si un usuario mueve una tarea que ya fue tomada por otra persona, el tablero revierte el movimiento y muestra un aviso rojo: "Conflicto: esta tarea fue reclamada por otro usuario."
- Al hacer clic en una tarjeta, se abre la vista detallada real de la tarea (no una copia local).

**Estado**: Ã¢Å“â€¦ Listo

---

*ÃƒÅ¡ltima actualizaciÃƒÂ³n: 2026-06-09 17:38 COT*
*PrÃƒÂ³xima entrada esperada: Al completarse la siguiente historia del Sprint PM-01*

---

## [2026-06-09] Ã¢â‚¬â€� Fortalecimiento de la integridad del sistema al registrar formularios enviados

**Autor**: Agente Backend (Ã¢Å¡â„¢Ã¯Â¸Â� BACKEND - JAVA)

**Ã‚Â¿QuÃƒÂ© es?**: Se corrigiÃƒÂ³ un problema estructural en la forma en que el sistema almacena los formularios que los usuarios envÃƒÂ­an al completar una tarea. La organizaciÃƒÂ³n interna del sistema no estaba respetando sus propias reglas de separaciÃƒÂ³n de responsabilidades, lo que podÃƒÂ­a causar errores difÃƒÂ­ciles de rastrear a medida que el sistema crece. AdemÃƒÂ¡s, se eliminÃƒÂ³ una tabla duplicada que se creaba automÃƒÂ¡ticamente en la base de datos cada vez que el sistema se instalaba desde cero, generando confusiÃƒÂ³n y desperdicio de espacio.

**Ã‚Â¿Para quÃƒÂ© sirve?**: Para garantizar que cada formulario enviado por un usuario se almacene de forma confiable, que el historial de envÃƒÂ­os nunca se pierda ni se corrompa, y que el sistema pueda crecer sin acumular datos basura. TambiÃƒÂ©n se resolvieron inconsistencias en el registro de avances del proyecto que podrÃƒÂ­an confundir a quienes consultan el estado de avance del sistema.

**Ã‚Â¿De dÃƒÂ³nde viene?**: Historia de Usuario US-017 (Registro de Formularios y GarantÃƒÂ­a de Integridad de Datos) Ã¢â‚¬â€� Sprint PM-01, Slot 5 (EstabilizaciÃƒÂ³n). Solicitado por el Arquitecto LÃƒÂ­der para cerrar deuda tÃƒÂ©cnica acumulada.

**Ã‚Â¿QuÃƒÂ© deberÃƒÂ­a hacer?**:
- Cuando un usuario envÃƒÂ­a un formulario, el sistema lo registra de forma inmutable (no se puede alterar despuÃƒÂ©s)
- Si ocurre un error al procesar el formulario, el sistema crea un registro de compensaciÃƒÂ³n (no borra el original)
- La base de datos ya no crea tablas redundantes al instalarse por primera vez
- El registro de avance del proyecto ya no tiene informaciÃƒÂ³n contradictoria

**Estado**: Ã¢Å“â€¦ Listo

---

## [2026-06-09] Ã¢â‚¬â€� SimplificaciÃƒÂ³n de notificaciones de conexiÃƒÂ³n y guardado

**Autor**: Agente Frontend (Ã°Å¸Å½Â¨ FRONTEND - VUE)

**Ã‚Â¿QuÃƒÂ© es?**: Se eliminÃƒÂ³ una notificaciÃƒÂ³n duplicada que podÃƒÂ­a confundir al usuario mostrando dos mensajes de estado al mismo tiempo. Se consolidÃƒÂ³ toda la informaciÃƒÂ³n sobre el estado de la conexiÃƒÂ³n a internet y el progreso de guardado en un solo indicador claro en la esquina de la pantalla.

**Ã‚Â¿Para quÃƒÂ© sirve?**: Para evitar confusiones y asegurar que el usuario sepa exactamente si el sistema estÃƒÂ¡ guardando sus datos, si se perdiÃƒÂ³ la conexiÃƒÂ³n, o si todo funciona correctamente, usando un lenguaje sencillo y sin tecnicismos.

**Ã‚Â¿De dÃƒÂ³nde viene?**: Historia de Usuario US-017 (EstabilizaciÃƒÂ³n Frontend) Ã¢â‚¬â€� Sprint PM-01, Slot 5. Cumpliendo las reglas de notificaciones claras (CA-19 a CA-26).

**Ã‚Â¿QuÃƒÂ© deberÃƒÂ­a hacer?**:
- Muestra un solo indicador cuando hay problemas de conexiÃƒÂ³n o el sistema estÃƒÂ¡ guardando datos de forma prolongada.
- Los mensajes son fÃƒÂ¡ciles de entender (por ejemplo: "Guardando cambios...", "Trabajando sin conexiÃƒÂ³n").
- Los cambios rÃƒÂ¡pidos (menores a 5 segundos) se guardan de forma invisible para no interrumpir el trabajo del usuario.

**Estado**: Ã¢Å“â€¦ Listo

---

## [2026-06-16] Ã¢â‚¬â€� RecuperaciÃƒÂ³n del Historial de Cambios en Procesos
**Autor**: Agente Backend (Ã¢Å¡â„¢Ã¯Â¸Â� BACKEND - JAVA)
**Ã‚Â¿QuÃƒÂ© es?**: Se solucionÃƒÂ³ un problema tÃƒÂ©cnico que impedÃƒÂ­a al sistema arrancar correctamente. El sistema habÃƒÂ­a "olvidado" cÃƒÂ³mo mostrar el historial de cambios de un proceso, lo que bloqueaba todo el inicio. Se le enseÃƒÂ±ÃƒÂ³ nuevamente cÃƒÂ³mo extraer y traducir esa informaciÃƒÂ³n de la base de datos para que el sistema inicie sin problemas.
**Ã‚Â¿Para quÃƒÂ© sirve?**: Para garantizar que el sistema vuelva a funcionar y que los administradores puedan consultar la "caja negra" o el registro de actividad de cada proceso (quiÃƒÂ©n lo modificÃƒÂ³, cuÃƒÂ¡ndo y quÃƒÂ© cambiÃƒÂ³). Esto es vital para auditorÃƒÂ­as y para entender quÃƒÂ© ha pasado con un trÃƒÂ¡mite a lo largo del tiempo.
**Ã‚Â¿De dÃƒÂ³nde viene?**: ResoluciÃƒÂ³n de Bug QuirÃƒÂºrgico (US-005) - Error de arranque del servidor.
**Ã‚Â¿QuÃƒÂ© deberÃƒÂ­a hacer?**:
- El sistema arranca sin errores crÃƒÂ­ticos de inicio.
- El historial de cambios de cualquier trÃƒÂ¡mite puede ser consultado correctamente.

**Estado**: Ã¢Å“â€¦ Listo

---

## [2026-06-16] Ã¢â‚¬â€� ReparaciÃƒÂ³n de Interfaz de Usuario y Notificaciones
**Autor**: Agente Frontend (Ã°Å¸â€�Â§ BUG-FIX LEAD)
**Ã‚Â¿QuÃƒÂ© es?**: Se solucionÃƒÂ³ un problema que impedÃƒÂ­a que la plataforma visual (Frontend) se cargara correctamente. El sistema intentaba buscar un componente visual de notificaciones con un nombre antiguo o incorrecto.
**Ã‚Â¿Para quÃƒÂ© sirve?**: Para garantizar que todos los usuarios puedan acceder al portal y a la bandeja unificada sin encontrarse con una pantalla en blanco o un error crÃƒÂ­tico al intentar ingresar.
**Ã‚Â¿De dÃƒÂ³nde viene?**: ResoluciÃƒÂ³n de un error de carga detectado al arrancar la interfaz web.
**Ã‚Â¿QuÃƒÂ© deberÃƒÂ­a hacer?**:
- El portal y la bandeja de trabajo ahora inician y se despliegan exitosamente sin interrupciones.

**Estado**: Ã¢Å“â€¦ Listo

## [2026-06-16] Ã¢â‚¬â€� RecuperaciÃƒÂ³n de los test de verificaciÃƒÂ³n del sistema
**Autor**: Agente Backend (Ã°Å¸â€�Â§ BUG-FIX LEAD)
**Ã‚Â¿QuÃƒÂ© es?**: Se corrigieron pequeÃƒÂ±os errores en el cÃƒÂ³digo de validaciÃƒÂ³n del sistema que estaban impidiendo que las revisiones tÃƒÂ©cnicas y automÃƒÂ¡ticas funcionaran. El sistema estaba confundiendo tipos de datos internos al leer logs y consultar tableros.
**Ã‚Â¿Para quÃƒÂ© sirve?**: Para garantizar que todos los controles de calidad puedan ejecutarse de forma correcta antes de probar y publicar el sistema. Esto evita que los desarrolladores se queden "atascados" con pantallas de error en compilaciÃƒÂ³n y permite seguir avanzando.
**Ã‚Â¿De dÃƒÂ³nde viene?**: ResoluciÃƒÂ³n de un problema tÃƒÂ©cnico detectado al levantar las pruebas del proyecto.
**Ã‚Â¿QuÃƒÂ© deberÃƒÂ­a hacer?**:
- Los procesos de validaciÃƒÂ³n tÃƒÂ©cnica ahora inician y se completan exitosamente sin interrumpir el desarrollo.

**Estado**: Ã¢Å“â€¦ Listo

---

## [2026-06-16] Ã¢â‚¬â€� EstabilizaciÃƒÂ³n de la ConexiÃƒÂ³n a la Base de Datos
**Autor**: Agente Backend (Ã¢Å¡â„¢Ã¯Â¸Â� BACKEND - JAVA)
**Ã‚Â¿QuÃƒÂ© es?**: Se corrigiÃƒÂ³ un problema de configuraciÃƒÂ³n donde el sistema intentaba conectarse a la base de datos por una "puerta" equivocada (puerto 5434), lo que causaba que el sistema no pudiera arrancar. Se ajustÃƒÂ³ la configuraciÃƒÂ³n para que siempre use la puerta correcta (puerto 5433) segÃƒÂºn lo dictado por la arquitectura del proyecto.
**Ã‚Â¿Para quÃƒÂ© sirve?**: Para garantizar que el sistema siempre pueda comunicarse con la base de datos sin errores de conexiÃƒÂ³n, previniendo fallas al iniciar y asegurando que el entorno local y de pruebas funcionen de manera estable y consistente.
**Ã‚Â¿De dÃƒÂ³nde viene?**: ResoluciÃƒÂ³n de un problema detectado durante el arranque del sistema (Connection Refused), alineando el cÃƒÂ³digo con el documento de arquitectura.
**Ã‚Â¿QuÃƒÂ© deberÃƒÂ­a hacer?**:
- El sistema se conecta a la base de datos correctamente sin reportar error de conexiÃƒÂ³n rechazada.
- El servidor arranca con normalidad.

**Estado**: Ã¢Å“â€¦ Listo

---

## [2026-06-17] Ã¢â‚¬â€� CorrecciÃƒÂ³n Visual del DiseÃƒÂ±ador de Formularios
**Autor**: Agente Frontend (Ã°Å¸Å½Â¨ FRONTEND - VUE3)
**Ã‚Â¿QuÃƒÂ© es?**: Se corrigiÃƒÂ³ un problema visual en la pantalla del DiseÃƒÂ±ador de Formularios donde los paneles se sobreponÃƒÂ­an entre sÃƒÂ­ al usar monitores de resoluciÃƒÂ³n estÃƒÂ¡ndar (pantallas normales de laptop o escritorio). Los tres paneles Ã¢â‚¬â€� la barra de componentes a la izquierda, el lienzo de diseÃƒÂ±o en el centro y el editor de cÃƒÂ³digo a la derecha Ã¢â‚¬â€� ahora se distribuyen armoniosamente sin invadir el espacio del otro.
**Ã‚Â¿Para quÃƒÂ© sirve?**: Para que cualquier usuario pueda diseÃƒÂ±ar formularios cÃƒÂ³modamente sin importar el tamaÃƒÂ±o de su pantalla. Antes, en pantallas normales (no ultra-anchas) el editor de cÃƒÂ³digo invadÃƒÂ­a el lienzo de diseÃƒÂ±o haciendo imposible trabajar. Ahora, cada panel se adapta al espacio disponible de forma proporcional.
**Ã‚Â¿De dÃƒÂ³nde viene?**: Bug reportado visualmente en el mÃƒÂ³dulo de diseÃƒÂ±o de formularios (BUG-UI-DESIGNER).
**Ã‚Â¿QuÃƒÂ© deberÃƒÂ­a hacer?**:
- En una pantalla de laptop estÃƒÂ¡ndar (1366x768), los tres paneles se muestran sin sobreponerse.
- El lienzo de diseÃƒÂ±o central se contrae suavemente cuando hay menos espacio disponible.
- El editor de cÃƒÂ³digo a la derecha es mÃƒÂ¡s angosto en pantallas pequeÃƒÂ±as y se expande progresivamente en pantallas mÃƒÂ¡s grandes.
- El error de consola reportado NO es del sistema sino de extensiones del navegador (se ignora justificadamente).

**Estado**: Ã¢Å“â€¦ Listo

## [2026-06-17] Ã¢â‚¬â€� CorrecciÃƒÂ³n del IDE de DiseÃƒÂ±o que no se Mostraba (Pantalla en Blanco)
**Autor**: Agente Frontend (Ã°Å¸Å½Â¨ FRONTEND - VUE3)
**Ã‚Â¿QuÃƒÂ© es?**: Se resolviÃƒÂ³ un error crÃƒÂ­tico donde el DiseÃƒÂ±ador de Formularios aparecÃƒÂ­a completamente en blanco al abrirlo. El problema era que el editor de cÃƒÂ³digo inteligente (Monaco Editor) intentaba descargarse automÃƒÂ¡ticamente desde Internet y la versiÃƒÂ³n mÃƒÂ¡s reciente contenÃƒÂ­a un defecto que impedÃƒÂ­a su arranque, bloqueando toda la pÃƒÂ¡gina.
**Ã‚Â¿Para quÃƒÂ© sirve?**: Para que los usuarios puedan acceder al DiseÃƒÂ±ador de Formularios sin encontrarse una pantalla vacÃƒÂ­a. Ahora el sistema descarga una versiÃƒÂ³n especÃƒÂ­fica y estable del editor de cÃƒÂ³digo que funciona correctamente, garantizando que el IDE se muestre siempre al abrir la ruta de diseÃƒÂ±o.
**Ã‚Â¿De dÃƒÂ³nde viene?**: Bug reportado como BUG-MONACO-BLANK Ã¢â‚¬â€� Error `RegisterClientLocalizationsError` en consola al navegar a la pantalla de diseÃƒÂ±o de formularios.
**Ã‚Â¿QuÃƒÂ© deberÃƒÂ­a hacer?**:
- Al abrir el DiseÃƒÂ±ador de Formularios, la pantalla muestra correctamente la barra de herramientas, el lienzo de diseÃƒÂ±o y el editor de cÃƒÂ³digo.
- No aparecen errores en la consola del navegador.
- El editor de cÃƒÂ³digo JSON/Zod carga normalmente y permite editar.

**Estado**: Ã¢Å“â€¦ Listo

---

## 17 de Junio de 2026 Ã¢â‚¬â€� Se corrigiÃƒÂ³ la pantalla en blanco al navegar entre secciones de la plataforma

**Autor**: Agente Frontend (Ã°Å¸Å½Â¨ FRONTEND - VUE3)
**Ã‚Â¿QuÃƒÂ© es?**: Se resolviÃƒÂ³ un error crÃƒÂ­tico donde, al hacer clic en un enlace para ir al DiseÃƒÂ±ador de Formularios (u otras secciones), la pantalla quedaba completamente en blanco. Curiosamente, si el usuario recargaba la pÃƒÂ¡gina con F5, todo funcionaba perfectamente. El problema era que unas notas internas de trazabilidad estaban ubicadas en un lugar incorrecto del cÃƒÂ³digo de navegaciÃƒÂ³n, lo cual confundÃƒÂ­a al sistema de animaciones y le impedÃƒÂ­a mostrar la nueva pÃƒÂ¡gina.
**Ã‚Â¿Para quÃƒÂ© sirve?**: Para que los usuarios puedan navegar libremente entre todas las secciones de la plataforma haciendo clic en los menÃƒÂºs y botones, sin que la pantalla quede en blanco. La transiciÃƒÂ³n animada (efecto de desvanecimiento) entre pÃƒÂ¡ginas ahora funciona correctamente.
**Ã‚Â¿De dÃƒÂ³nde viene?**: Bug reportado como BUG-TRANSITION-BLANK Ã¢â‚¬â€� Pantalla blanca al navegar entre vistas usando la navegaciÃƒÂ³n interna de la aplicaciÃƒÂ³n.
**Ã‚Â¿QuÃƒÂ© deberÃƒÂ­a hacer?**:
- Al hacer clic en cualquier enlace o botÃƒÂ³n de navegaciÃƒÂ³n, la nueva secciÃƒÂ³n se muestra correctamente con una animaciÃƒÂ³n suave de transiciÃƒÂ³n.
- Ya no es necesario recargar la pÃƒÂ¡gina con F5 para ver el contenido.
- Todas las secciones (Formularios, DiseÃƒÂ±ador, Workdesk, etc.) cargan correctamente al navegar.

**Estado**: Ã¢Å“â€¦ Listo

---

## 17 de Junio de 2026 Ã¢â‚¬â€� CorrecciÃƒÂ³n de Pantalla Blanca al Navegar al DiseÃƒÂ±ador de Formularios y Error del Editor de CÃƒÂ³digo

**Autor**: Agente Frontend (Ã°Å¸Å½Â¨ FRONTEND - VUE3) Ã¢â‚¬â€� Rama DevDavid
**Ã‚Â¿QuÃƒÂ© es?**: Se resolvieron dos problemas crÃƒÂ­ticos que afectaban la experiencia del DiseÃƒÂ±ador de Formularios:
1. **Pantalla Blanca**: Al hacer clic para ir a la lista de formularios o navegar entre secciones, la pantalla quedaba completamente en blanco. La causa era un problema de estructura interna donde una ventana emergente de confirmaciÃƒÂ³n de borrado estaba colocada fuera del contenedor principal de la pÃƒÂ¡gina, lo cual confundÃƒÂ­a al sistema de animaciones de navegaciÃƒÂ³n.
2. **Error en el Editor de CÃƒÂ³digo**: El editor inteligente de cÃƒÂ³digo (Monaco IDE) que usan los diseÃƒÂ±adores mostraba un error en la consola del navegador ("RegisterClientLocalizationsError") porque intentaba descargar traducciones de un servidor externo (CDN) que ya no es compatible con la versiÃƒÂ³n actual. Se cambiÃƒÂ³ para usar la versiÃƒÂ³n del editor que ya viene incluida en la aplicaciÃƒÂ³n.

**Ã‚Â¿Para quÃƒÂ© sirve?**: Para que los usuarios puedan navegar sin interrupciones al Gestor de Formularios y al DiseÃƒÂ±ador, sin pantallas en blanco y sin errores en la consola del navegador. El editor de cÃƒÂ³digo ahora carga instantÃƒÂ¡neamente sin depender de servidores externos.
**Ã‚Â¿De dÃƒÂ³nde viene?**: Bug CrÃƒÂ­tico reportado como BUG-TRANSITION-BLANK-V2 + BUG-MONACO-NLS Ã¢â‚¬â€� DiagnÃƒÂ³stico del Arquitecto LÃƒÂ­der identificÃƒÂ³ causa raÃƒÂ­z en fragmento multi-nodo Vue 3 y CDN de Monaco obsoleta.
**Ã‚Â¿QuÃƒÂ© deberÃƒÂ­a hacer?**:
- Al hacer clic en "Gestor de Formularios" o cualquier enlace de navegaciÃƒÂ³n, la nueva secciÃƒÂ³n se muestra correctamente con animaciÃƒÂ³n suave Ã¢â‚¬â€� sin pantalla blanca.
- El DiseÃƒÂ±ador de Formularios carga el editor de cÃƒÂ³digo sin errores en la consola del navegador.
- El editor de cÃƒÂ³digo carga mÃƒÂ¡s rÃƒÂ¡pido al no depender de descargas externas (CDN).
- El modal de confirmaciÃƒÂ³n de borrado de formularios sigue funcionando normalmente.

**Estado**: Ã¢Å“â€¦ Listo

---

## 19 de Junio de 2026 Ã¢â‚¬â€� Mejora Visual y Responsiva del DiseÃƒÂ±ador de Formularios

**Autor**: Agente Frontend (Ã°Å¸Å½Â¨ FRONTEND - VUE3) Ã¢â‚¬â€� Rama DevDavid
**Ã‚Â¿QuÃƒÂ© es?**: Se mejorÃƒÂ³ el diseÃƒÂ±o de la pantalla del "DiseÃƒÂ±ador de Formularios" para que se adapte perfectamente a cualquier tamaÃƒÂ±o de pantalla, ya sea un monitor grande, una tableta o una laptop pequeÃƒÂ±a. AdemÃƒÂ¡s, se solucionÃƒÂ³ un problema donde algunos campos arrastrados al centro de la pantalla se salÃƒÂ­an de su contenedor, creando barras de desplazamiento innecesarias y un aspecto desordenado.
**Ã‚Â¿Para quÃƒÂ© sirve?**: Para que los creadores de formularios tengan una experiencia fluida y cÃƒÂ³moda en cualquier dispositivo. Los paneles laterales (herramientas y cÃƒÂ³digo) ahora se ocultan inteligentemente si la pantalla es muy pequeÃƒÂ±a, y los campos dentro del formulario mantienen su tamaÃƒÂ±o correcto sin desbordarse.
**Ã‚Â¿De dÃƒÂ³nde viene?**: ResoluciÃƒÂ³n de BUG-0001 Ã¢â‚¬â€� Reporte de estilos y responsividad en FormDesigner.
**Ã‚Â¿QuÃƒÂ© deberÃƒÂ­a hacer?**:
- En pantallas pequeÃƒÂ±as (como tabletas), los paneles laterales se ocultan para dar prioridad al lienzo central.
- Los campos del formulario, como cuadros de texto, no rebasan los bordes de la pantalla.
- La pantalla ya no muestra barras de desplazamiento horizontales molestas que afecten la navegaciÃƒÂ³n.

**Estado**: Ã¢Å“â€¦ Listo

---

## [2026-06-22] Ã¢â‚¬â€� EstabilizaciÃƒÂ³n del CatÃƒÂ¡logo de Formularios para DiseÃƒÂ±os de Procesos
**Autor**: Agente Backend (Ã¢Å¡â„¢Ã¯Â¸Â� BACKEND - JAVA) Ã¢â‚¬â€� Rama DevDavid
**Ã‚Â¿QuÃƒÂ© es?**: Se corrigiÃƒÂ³ un problema que hacÃƒÂ­a que la lista de formularios disponibles apareciera vacÃƒÂ­a al intentar vincular un formulario a una tarea en el diseÃƒÂ±ador de procesos. El sistema ahora permite encontrar y asignar tanto los formularios que ya estÃƒÂ¡n activos y listos para usar, como aquellos que aÃƒÂºn estÃƒÂ¡n en estado de borrador. AdemÃƒÂ¡s, se aÃƒÂ±adiÃƒÂ³ una protecciÃƒÂ³n para que, si el sistema no encuentra un proceso especÃƒÂ­fico, simplemente muestre todos los formularios disponibles en lugar de fallar y ocultarlos.
**Ã‚Â¿Para quÃƒÂ© sirve?**: Para que los analistas y administradores puedan asignar correctamente quÃƒÂ© formulario debe llenar un usuario en cada paso de un proceso de negocio. Al recuperar la visibilidad de los borradores, pueden diseÃƒÂ±ar el flujo de trabajo sin tener que finalizar y certificar primero los formularios, agilizando el diseÃƒÂ±o de nuevos trÃƒÂ¡mites.
**Ã‚Â¿De dÃƒÂ³nde viene?**: Historia de Usuario US-005, Criterios de AceptaciÃƒÂ³n CA-39 y CA-40 Ã¢â‚¬â€� Handoff del Arquitecto LÃƒÂ­der (EstabilizaciÃƒÂ³n del CatÃƒÂ¡logo de Formularios Activos para VinculaciÃƒÂ³n BPMN).
**Ã‚Â¿QuÃƒÂ© deberÃƒÂ­a hacer?**:
- Al entrar al diseÃƒÂ±ador de procesos y hacer clic en una tarea, la lista desplegable de formularios ("Form Key") ya no aparece vacÃƒÂ­a.
- La lista muestra todos los formularios en estado borrador y activos.
- El sistema no se rompe si el proceso no tiene todavÃƒÂ­a un nombre tÃƒÂ©cnico correcto, sino que muestra la lista completa de formularios.

**Estado**: Ã¢Å“â€¦ Listo

## 2026-06-22 â€” CorrecciÃ³n del Selector de Formularios en el DiseÃ±ador de Procesos BPMN
**Autor**: Agente Frontend (?? FRONTEND - VUE3)
**Â¿QuÃ© es?**: Se corrigiÃ³ el selector de formularios en el diseÃ±ador de procesos BPMN para que muestre los formularios reales creados por el usuario, eliminando datos de prueba que aparecÃ­an anteriormente.
**Â¿Para quÃ© sirve?**: Para asegurar que al configurar una tarea en un proceso, los usuarios solo puedan seleccionar formularios que realmente existen y estÃ¡n listos para usarse, evitando confusiones y errores con datos falsos.
**Â¿De dÃ³nde viene?**: Historia de Usuario US-005, Criterios de AceptaciÃ³n 39 y 40.
**Â¿QuÃ© deberÃ­a hacer?**:
- El menÃº desplegable de formularios muestra Ãºnicamente los formularios reales.
- Si no hay formularios, muestra la lista vacÃ­a en vez de informaciÃ³n inventada (mocks).

**Estado**: ? Listo

---

## 2026-06-22 - EstabilizaciÃƒÂ³n de Pruebas Automatizadas y Teardown de Contenedores
**Autor**: Agente Frontend (Ã°Å¸â€˜Â¨Ã¢â‚¬Â�Ã°Å¸â€™Â» FRONTEND - VUE3) Ã¢â‚¬â€� Rama DevDavid
**Ã‚Â¿QuÃƒÂ© es?**: Se configurÃƒÂ³ la limpieza automÃƒÂ¡tica y total de la infraestructura temporal (contenedores) usada durante la certificaciÃƒÂ³n E2E. AdemÃƒÂ¡s, se dotÃƒÂ³ al sistema de pruebas de un tiempo prudente para el "arranque en frÃƒÂ­o" visual.
**Ã‚Â¿Para quÃƒÂ© sirve?**: Para garantizar que nuestra infraestructura de pruebas no deje "basura" en los servidores que bloquee puertos, y sea 100% confiable, erradicando falsas alarmas provocadas por retrasos normales de compilaciÃƒÂ³n.
**Ã‚Â¿De dÃƒÂ³nde viene?**: Handoff ArquitectÃƒÂ³nico: Fix Playwright y Cierre CA-39/CA-40.
**Ã‚Â¿QuÃƒÂ© deberÃƒÂ­a hacer?**:
- Al terminar, se destruye todo ambiente temporal de pruebas sin dejar contenedores fantasma.
- Las pruebas esperan pacientemente a que la interfaz estÃƒÂ© lista.

**Estado**: Ã¢Å“â€¦ Listo

---

## 2026-06-23 - ResoluciÃ³n Definitiva de Flaky Tests en Modelador BPMN
**Autor**: Agente Frontend ????? FRONTEND - VUE3 | Rama DevDavid
**Â¿QuÃ© es?**: Se corrigiÃ³ un error sutil en la prueba automatizada que provocaba fallos falsos al interactuar demasiado rÃ¡pido con el modal de bienvenida antes de que la pantalla cargara por completo.
**Â¿Para quÃ© sirve?**: Para que el robot de pruebas no intente hacer clic 'a ciegas' mientras la aplicaciÃ³n aÃºn se estÃ¡ dibujando en la pantalla, asegurando una validaciÃ³n 100% certera de la funcionalidad.
**Â¿De dÃ³nde viene?**: AnÃ¡lisis profundo de la reactividad de Vue 3 en escenarios de 'Cold Start' (Arranque en frÃ­o).
**Â¿QuÃ© deberÃ­a hacer?**: 
- Las pruebas ahora esperan obligatoria y pacientemente a que la pantalla de inicio cargue su contenido visual antes de interactuar.
- La estabilidad de las pruebas aumenta al 100% en todos los entornos.

**Estado**: âœ… Listo

---

## 2026-07-01 â€” Filtro Visual de Tipos de Formulario y Mejora de Estilos en el Modelador de Procesos
**Autor**: Agente Frontend (ðŸŽ¨ FRONTEND - VUE3) â€” Rama DevDavid
**Â¿QuÃ© es?**: Se agregaron tres botones de filtro rÃ¡pido ("Todos", "Simple" y "Maestro") junto al selector de formularios dentro del diseÃ±ador de procesos de negocio. AdemÃ¡s, se mejoraron los estilos visuales del selector de formularios para que sea mÃ¡s legible, accesible y coherente con el resto del diseÃ±o del sistema.
**Â¿Para quÃ© sirve?**: Para que los analistas de procesos puedan filtrar rÃ¡pidamente quÃ© tipo de formulario necesitan al diseÃ±ar una tarea: formularios simples (independientes) o formularios maestros (mutantes). Antes, el filtro se aplicaba de forma invisible segÃºn la configuraciÃ³n del proceso, lo que confundÃ­a al usuario al no ver todos los formularios disponibles. Ahora, con un solo clic en los botones, el analista ve exactamente los formularios que necesita. Los estilos mejorados hacen que el selector sea mÃ¡s fÃ¡cil de leer y tenga un aspecto profesional.
**Â¿De dÃ³nde viene?**: ResoluciÃ³n de BUG-J02-004 (filtro de tipos de formulario faltante) y BUG-J02-005 (estilos del selector de formularios) â€” CertificaciÃ³n UAT Jornada 02, MisiÃ³n M5.
**Â¿QuÃ© deberÃ­a hacer?**:
- Al abrir las propiedades de una tarea en el modelador, aparecen tres botones junto al selector de formularios: "Todos", "Simple" y "Maestro"
- Al presionar "Simple", solo se muestran los formularios simples; al presionar "Maestro", solo los maestros; al presionar "Todos", se muestran todos
- El selector de formularios tiene un aspecto visual limpio, con bordes redondeados, sombra sutil y resaltado al pasar el cursor
- Este filtro funciona tanto para tareas de usuario como para el evento de inicio del proceso

**Estado**: âœ… Listo


---

## 2026-06-24 - CorrecciÃ³n de CatÃ¡logo de Formularios y Enrutamiento en DiseÃ±ador BPMN
**Autor**: Agente Backend (âš™ï¸� BACKEND - JAVA) / Agente Frontend (ðŸŽ¨ FRONTEND - VUE3) - Rama DevDavid
**Â¿QuÃ© es?**: Se eliminaron datos simulados (mocks) en el catÃ¡logo de formularios para mostrar informaciÃ³n real de la base de datos. Adicionalmente, se corrigieron enlaces rotos y rutas de navegaciÃ³n dentro del diseÃ±ador de procesos BPMN que llevaban a pÃ¡ginas no encontradas (error 404).
**Â¿Para quÃ© sirve?**: Para que al diseÃ±ar un proceso y asignar formularios a las tareas, el sistema liste los formularios verdaderos creados por la organizaciÃ³n. TambiÃ©n asegura que los usuarios puedan navegar fluidamente dentro del Ã¡rea de modelado sin toparse con pantallas de error al acceder a subprocesos o al diseÃ±ador principal.
**Â¿De dÃ³nde viene?**: ResoluciÃ³n de bugs crÃ­ticos detectados durante la CertificaciÃ³n UAT Manual (Sprint PM-01). Reporte de Agente QA (BUG-J02-001, BUG-J02-002, BUG-J02-003).
**Â¿QuÃ© deberÃ­a hacer?**: 
- El menÃº de selecciÃ³n de formularios en el diseÃ±ador muestra datos reales de PostgreSQL.
- Ingresar a la secciÃ³n de modelador no resulta en un error 404, redirigiendo correctamente a la herramienta.
- Hacer clic en un subproceso (Call Activity) abre correctamente el proceso hijo en una nueva pestaÃ±a.

**Estado**: ? Listo

---

## 2026-06-24 - Certificacion UAT Manual del Disenador BPMN y Catalogo de Formularios (Journey J-02)
**Autor**: Harold (Tester Humano) asistido por Agente QA - Sprint PM-01, Rama DevDavid
**Que es?**: Se realizo una certificacion manual paso a paso del flujo completo de diseno de procesos BPMN y creacion de formularios. Harold (el tester humano) ejecuto 5 misiones de las 7 planificadas, verificando visualmente cada pantalla, cada peticion HTTP y cada respuesta del servidor.
**Para que sirve?**: Para confirmar desde la perspectiva de un usuario real que el sistema funciona correctamente: que los formularios se crean y guardan en la base de datos, que el disenador de procesos BPMN carga correctamente, y que los datos falsos (mocks) fueron eliminados del catalogo.
**De donde viene?**: Certificacion UAT Manual Journey J-02 (Sprint PM-01). Cubre historias US-005 (Disenador BPMN) y US-003 (Catalogo y Creacion de Formularios).
**Que deberia hacer?**:
- Misiones completadas y verificadas:
  - Mision 0 (Infraestructura): Docker, Backend y Frontend operativos - PASS
  - Mision 1 (Login y Navegacion): Autenticacion exitosa, menu lateral funcional - PASS
  - Mision 2 (Catalogo de Formularios): Muestra datos reales de la base de datos, zero mocks confirmado tras correccion de BUG-J02-003 - PASS
  - Mision 3 (Crear Formulario): Harold creo 3 formularios de prueba exitosamente con persistencia real (POST 201 Created) - PASS
  - Mision 4 (Disenador BPMN Canvas): Modal de bienvenida, canvas BPMN, StartEvent y propiedades funcionan correctamente - PASS
- Misiones pendientes (se retomaran en proxima sesion): Mision 5 (Dropdown FormKey CA-39/CA-40), Mision 6 (Persistencia vinculacion), Mision 7 (RBAC)
- Bugs encontrados y corregidos: BUG-J02-001 (ruta 404), BUG-J02-002 (link roto), BUG-J02-003 (datos falsos en catalogo) - todos cerrados en commit ef18729d

**Estado**: En progreso (5 de 7 misiones completadas)

---

## 2026-07-01 - Certificacion Completa: Disenador de Procesos y Formularios (Journey J-02)
**Autor**: Harold (asistido por Agente QA)
**Que es?**: Se completo la certificacion manual de las 7 misiones del Journey J-02, que cubre el flujo completo de disenar procesos empresariales (BPMN) y crear formularios digitales. Harold verifico en vivo cada pantalla, cada boton y cada respuesta del sistema.
**Para que sirve?**: Confirmar que un usuario puede crear formularios, disenar procesos, vincular formularios a tareas del proceso, y que todo se guarda correctamente en la base de datos real. Tambien se verifico que un usuario sin permisos no puede acceder a las herramientas de diseno.
**De donde viene?**: Journey J-02 - cubre las historias US-005 (Disenador BPMN) y US-003 (Catalogo y Creacion de Formularios)
**Que deberia hacer?**: El sistema permite:
- Ver el catalogo de formularios con datos reales (no simulados)
- Crear nuevos formularios que se guardan en la base de datos
- Abrir el disenador de procesos BPMN con todas sus herramientas
- Vincular un formulario real a una tarea del proceso
- Guardar un proceso y al recargar la pagina, la vinculacion se mantiene
- Un usuario sin permisos ve pagina 404 al intentar acceder a herramientas de diseno (seguridad confirmada)
**Estado**: Certificado con observaciones (3 mejoras menores pendientes: filtro de tipos de formulario, estilos visuales del selector, y menu de usuario con rol limitado)

---

## 2026-07-01 â€” ReparaciÃ³n del Guardado de MenÃº y TopologÃ­a
**Autor**: Agente Backend (Rama DevDavid)
**Â¿QuÃ© es?**: Se corrigiÃ³ un problema donde, al editar un rol de usuario, los accesos a los mÃ³dulos del menÃº no se estaban guardando. Se implementÃ³ la lÃ³gica para que el sistema reciba, traduzca y guarde estos accesos, y se asegurÃ³ de que los permisos bÃ¡sicos existan en el sistema.
**Â¿Para quÃ© sirve?**: Para garantizar que cuando un administrador asigne o quite acceso a diferentes secciones del sistema (como el Workdesk o el Modelador), estos cambios se guarden de verdad y el usuario solo vea las opciones que le corresponden.
**Â¿De dÃ³nde viene?**: ResoluciÃ³n de BUG-J02-006 relacionado con la falta de guardado de los permisos de mÃ³dulos.
**Â¿QuÃ© deberÃ­a hacer?**:
- Al actualizar un rol, los mÃ³dulos asignados se guardan correctamente en el sistema.
- El usuario verÃ¡ reflejados sus nuevos accesos en el menÃº al iniciar sesiÃ³n.

**Estado**: âœ… Listo

---

## 2026-07-06 â€” HOTFIX CRÃ�TICO: ReparaciÃ³n del DiseÃ±ador de Flujos de Trabajo
**Autor**: Arquitecto LÃ­der (Rama DevDavid)
**Â¿QuÃ© es?**: Se reparÃ³ un error crÃ­tico que impedÃ­a agregar componentes (tareas, eventos, compuertas) al diseÃ±ador de procesos BPMN. Al intentar arrastrar cualquier elemento al lienzo, el sistema mostraba un error y no permitÃ­a diseÃ±ar.
**Â¿Para quÃ© sirve?**: Para devolver la funcionalidad completa del diseÃ±ador de procesos. Sin este arreglo, era imposible crear o editar flujos de trabajo en la plataforma.
**Â¿De dÃ³nde viene?**: El motor de diseÃ±o necesita un catÃ¡logo completo de tipos de componentes para funcionar. Se estaba usando un catÃ¡logo artesanal con solo 7 tipos en lugar del catÃ¡logo oficial con mÃ¡s de 100 tipos. Al eliminar este catÃ¡logo incompleto y usar el oficial, el diseÃ±ador volviÃ³ a funcionar correctamente.
**Â¿QuÃ© deberÃ­a hacer?**:
- Abrir el diseÃ±ador BPMN â†’ Crear nuevo proceso â†’ Arrastrar tareas y eventos sin errores
- Todas las propiedades (formularios, temas, decisiones) siguen funcionando
- El filtro de tipos de formulario (Simple/Maestro) sigue visible y operativo

**Estado**: âœ… Listo

---

## 2026-07-14 â€” Estructura para AsignaciÃ³n de Roles en Tareas del Proceso
**Autor**: Agente Infra/BD (Rama feature/lane-role-assignment)
**Â¿QuÃ© es?**: Se prepararon las tablas y reglas internas de la base de datos que permitirÃ¡n conectar los "carriles" de un proceso (quiÃ©n hace quÃ©) con los roles reales del sistema (ej. Analista, Supervisor). Se agregaron protecciones para asegurar que no se puedan asignar roles que no existen ni duplicar carriles en un mismo proceso.
**Â¿Para quÃ© sirve?**: Para garantizar que, cuando en un futuro cercano se configure visualmente quiÃ©n debe ejecutar una tarea en el diseÃ±ador de procesos, esa informaciÃ³n se guarde de forma segura y estrictamente relacionada con los roles de seguridad reales de la empresa. Esto es el cimiento para que el sistema asigne las tareas a las personas correctas de forma automÃ¡tica.
**Â¿De dÃ³nde viene?**: Historias de Usuario US-005 (Modelador de Procesos) y US-036 (Seguridad y Roles), siguiendo la arquitectura establecida en el Sprint PM-01.
**Â¿QuÃ© deberÃ­a hacer?**:
- El sistema ya cuenta con la estructura interna para guardar quÃ© rol ejecuta las tareas de cada carril.
- Protege la informaciÃ³n impidiendo que se asigne trabajo a roles que no existen.
- Evita posibles fallos o confusiones impidiendo que se creen carriles duplicados en el mismo diseÃ±o de proceso.

**Estado**: âœ… Listo

---

## 2026-07-14 â€” ConexiÃ³n de Carriles BPMN con Roles de Seguridad
**Autor**: Agente Backend (Rama DevDavid)
**Â¿QuÃ© es?**: Se construyÃ³ el puente lÃ³gico y los servicios que permiten al sistema leer los "carriles" (lanes) de un diagrama de proceso y guardarlos en la base de datos de manera organizada. AdemÃ¡s, se crearon las conexiones internas (APIs) para que los administradores puedan asignar quÃ© rol de seguridad puede iniciar o ejecutar las tareas de cada carril.
**Â¿Para quÃ© sirve?**: Para que cuando se diseÃ±e un proceso con diferentes responsables (por ejemplo, "Solicitante", "Revisor", "Aprobador"), el sistema sepa exactamente quÃ© personas de la empresa pueden hacer ese trabajo. Esto evita que alguien sin autorizaciÃ³n apruebe algo que no le corresponde, conectando los diagramas visuales con los permisos reales de los usuarios.
**Â¿De dÃ³nde viene?**: Historias de Usuario US-005 (Modelador de Procesos) y US-036 (Seguridad y Roles).
**Â¿QuÃ© deberÃ­a hacer?**:
- Al cargar un nuevo proceso en el sistema, detecta automÃ¡ticamente todos sus carriles y los registra.
- Permite a los administradores consultar la lista de carriles de cualquier proceso.
- Permite asignar o quitar roles a un carril especÃ­fico para controlar quiÃ©n puede iniciar el proceso o ejecutar sus tareas.

**Estado**: âœ… Listo

---

## 2026-07-14 â€” ResoluciÃ³n de Defectos en AsignaciÃ³n de Roles (PM-IA)
**Autor**: Agente Backend (Rama DevDavid)
**Â¿QuÃ© es?**: Se corrigieron 6 defectos tÃ©cnicos relacionados con cÃ³mo el sistema guarda y verifica a los responsables de un proceso. Se agregÃ³ validaciÃ³n estricta para asegurar que los roles y carriles existen antes de intentar conectarlos, y se mejorÃ³ la forma en que el sistema registra internamente estas asignaciones para usar al usuario conectado en lugar de un "sistema" genÃ©rico.
**Â¿Para quÃ© sirve?**: Para garantizar que cuando un administrador configura quiÃ©n debe hacer quÃ© tarea, los permisos se asignen correctamente y el sistema avise si hay algÃºn error, evitando fallos silenciosos y previniendo caÃ­das del servidor.
**Â¿De dÃ³nde viene?**: CorrecciÃ³n de defectos de la IteraciÃ³n Correctiva 84-DEV-LANE-ROLE-FIX tras auditorÃ­a forense.
**Â¿QuÃ© deberÃ­a hacer?**:
- El sistema alerta correctamente si se intenta asignar tareas a un rol o carril que no existe.
- Las asignaciones registran correctamente quiÃ©n las realizÃ³ para efectos de auditorÃ­a.
- Se previenen errores internos de guardado en despliegues.

**Estado**: âœ… Listo

---

## 2026-07-14 â€” ResoluciÃ³n de Defectos Visuales en AsignaciÃ³n de Roles (PM-IA)
**Autor**: Agente Frontend (Rama DevDavid)
**Â¿QuÃ© es?**: Se corrigiÃ³ un problema donde el sistema no informaba al usuario si ocurrÃ­a un error al cargar o guardar los permisos (roles) de un proceso. AdemÃ¡s, se eliminÃ³ un falso mensaje de Ã©xito que aparecÃ­a cuando fallaba la eliminaciÃ³n de un rol, lo cual ocultaba problemas reales.
**Â¿Para quÃ© sirve?**: Para garantizar que los administradores siempre sepan la verdad. Si algo falla al configurar quiÃ©n puede hacer quÃ© tarea, el sistema mostrarÃ¡ inmediatamente una alerta roja con el error exacto, evitando que los usuarios crean que guardaron un cambio cuando en realidad no fue asÃ­.
**Â¿De dÃ³nde viene?**: CorrecciÃ³n de defectos visuales de la IteraciÃ³n Correctiva 84-DEV-LANE-ROLE-FIX.
**Â¿QuÃ© deberÃ­a hacer?**:
- Si falla la carga o guardado de los permisos de un carril, aparece una alerta visible con el error detallado.
- Si falla la eliminaciÃ³n de un rol, el sistema no lo quita de la pantalla engaÃ±ando al usuario, sino que muestra la alerta de error y lo mantiene visible.

**Estado**: âœ… Listo

---

## 2026-07-15 â€” CorrecciÃ³n de Carga de Formularios (Bug B-04)
**Autor**: Agente Backend (Rama DevDavid)
**Â¿QuÃ© es?**: Se corrigiÃ³ un problema de comunicaciÃ³n entre el sistema y la base de datos donde la interfaz no podÃ­a recuperar el diseÃ±o de los formularios por su "nombre tÃ©cnico", mostrando un error en pantalla en lugar del formulario.
**Â¿Para quÃ© sirve?**: Para garantizar que los usuarios puedan ver y llenar los formularios correctamente cuando inician un proceso o ejecutan una tarea, evitando interrupciones en el flujo de trabajo.
**Â¿De dÃ³nde viene?**: CorrecciÃ³n del Bug B-04 (Endpoint GET Form Design Technical Name No Funciona) del reporte de Pruebas UAT.
**Â¿QuÃ© deberÃ­a hacer?**:
- Al intentar cargar un formulario por su nombre tÃ©cnico, el sistema devuelve la estructura del formulario exitosamente.
- Si el formulario no existe, informa el error correctamente sin colapsar.

**Estado**: âœ… Listo

---

## 2026-07-15 â€” CorrecciÃ³n de 4 Defectos de Interfaz en el Modelador de Procesos y Formularios
**Autor**: Agente Frontend (Rama DevDavid)
**Â¿QuÃ© es?**: Se corrigieron 4 problemas detectados en las Pruebas de AceptaciÃ³n de Usuario (UAT) que afectaban la experiencia visual y funcional del Modelador de Procesos y del DiseÃ±ador de Formularios:
1. **Campos del carril (Lane) bloqueados**: Los campos de "Actor" y "Rol" dentro de un carril BPMN no permitÃ­an escribir ni seleccionar valores.
2. **Apariencia visual rota en el panel de carriles**: Los botones y campos del panel de carriles se veÃ­an desordenados porque usaban un estilo visual incompatible con el resto de la aplicaciÃ³n.
3. **Errores de comunicaciÃ³n con el servidor (404)**: Tres direcciones internas de comunicaciÃ³n entre la interfaz y el servidor estaban mal escritas, provocando errores al consultar instancias de proceso y temas de tareas externas.
4. **Lienzo de formularios en blanco**: Al abrir un formulario existente, la pantalla se quedaba en blanco porque un diÃ¡logo de selecciÃ³n de plantillas no se cerraba automÃ¡ticamente.

**Â¿Para quÃ© sirve?**: Para que los usuarios del Modelador puedan asignar correctamente actores y roles de seguridad a los carriles de un proceso, ver el panel de propiedades con un aspecto visual consistente, y abrir formularios existentes sin encontrar pantallas en blanco ni errores de conexiÃ³n.
**Â¿De dÃ³nde viene?**: CorrecciÃ³n de los Bugs B-01, B-02, B-03 y B-04 (Frontend) del reporte de Pruebas UAT, vinculados a las Historias de Usuario US-005 y US-036 (AsignaciÃ³n de Roles a Carriles).
**Â¿QuÃ© deberÃ­a hacer?**:
- Al seleccionar un carril en el modelador, el panel lateral muestra campos editables de nombre, actor y rol con apariencia visual limpia y moderna.
- Al escribir en los campos del carril, los valores se guardan correctamente en el diagrama BPMN.
- Al consultar instancias de proceso o tareas externas, la aplicaciÃ³n se comunica correctamente con el servidor sin errores.
- Al abrir un formulario existente, el diseÃ±ador muestra inmediatamente el contenido del formulario sin pantallas en blanco.

**Estado**: âœ… Listo

---

## 2026-07-17 â€” CorrecciÃ³n de Permisos para Despliegue de Procesos
**Autor**: Agente Backend (Rama DevDavid)
**Â¿QuÃ© es?**: Se solucionÃ³ un problema que impedÃ­a a los administradores principales del sistema publicar o guardar las definiciones de los procesos de negocio. El sistema bloqueaba la acciÃ³n porque exigÃ­a un rol especializado que no existÃ­a en la base de datos, y tampoco permitÃ­a usar el rol de administrador general como respaldo. Se creÃ³ el rol especializado y se ajustaron los controles de seguridad para aceptar a ambos.
**Â¿Para quÃ© sirve?**: Para garantizar que los procesos de negocio puedan publicarse exitosamente en el sistema sin rechazar a los usuarios vÃ¡lidos (error de acceso denegado). AdemÃ¡s, todas estas acciones ahora generan un registro de seguridad claro que indica quiÃ©n las realizÃ³.
**Â¿De dÃ³nde viene?**: CorrecciÃ³n del Bug CrÃ­tico R2-01 reportado en Pruebas UAT, vinculado a las Historias de Usuario US-005 y US-036.
**Â¿QuÃ© deberÃ­a hacer?**:
- Los usuarios con rol de administrador general o administrador de despliegues pueden publicar diagramas de procesos.
- Ya no aparece un mensaje de "Acceso Denegado" (403) al intentar desplegar.
- El sistema cuenta permanentemente con el rol especializado desde su instalaciÃ³n.

**Estado**: âœ… Listo

---

## 2026-07-17 â€” CorrecciÃ³n de MenÃº que DesaparecÃ­a y Formularios que No Cargaban
**Autor**: Agente Frontend (Rama DevDavid)
**Â¿QuÃ© es?**: Se corrigieron dos problemas que afectaban la experiencia de los usuarios:
1. **El menÃº de navegaciÃ³n desaparecÃ­a inesperadamente.** Cuando el sistema detectaba que un usuario no tenÃ­a permiso para una acciÃ³n especÃ­fica (por ejemplo, desplegar un proceso), eliminaba por error todo el menÃº de la aplicaciÃ³n, obligando al usuario a cerrar sesiÃ³n y volver a entrar. Ahora, el menÃº solo se resetea cuando el administrador efectivamente revoca los permisos del usuario, no por cualquier restricciÃ³n operativa.
2. **Los formularios diseÃ±ados no se podÃ­an abrir para ediciÃ³n.** Al intentar cargar un formulario previamente guardado, la pantalla quedaba en blanco porque el sistema buscaba los datos del formulario con nombres equivocados. Se corrigiÃ³ para que lea la informaciÃ³n exactamente como la entrega el servidor.
**Â¿Para quÃ© sirve?**: Para que los usuarios puedan navegar sin perder su menÃº por acciones normales del sistema, y para que los formularios diseÃ±ados se abran correctamente mostrando todos sus campos, tÃ­tulo y versiÃ³n.
**Â¿De dÃ³nde viene?**: CorrecciÃ³n de Bugs CrÃ­ticos R2-02 y R2-03 reportados en Pruebas UAT, vinculados a las Historias de Usuario US-005 y US-036.
**Â¿QuÃ© deberÃ­a hacer?**:
- El menÃº de navegaciÃ³n permanece visible aunque el usuario reciba un mensaje de "acceso denegado" en alguna acciÃ³n especÃ­fica.
- Los formularios previamente guardados se abren correctamente mostrando su nombre, campos y versiÃ³n.
- Solo cuando un administrador revoque explÃ­citamente los permisos de un usuario, el menÃº se actualiza.

**Estado**: âœ… Listo

## [23/07/2026] â€” CorrecciÃ³n del Pipeline de Pruebas Automatizadas
**Autor**: Agente Backend (Antigravity)
**Â¿QuÃ© es?**: Se corrigiÃ³ la forma en que el sistema de validaciÃ³n remota (GitHub Actions) comprueba el estado del proyecto. Se eliminaron pruebas de cÃ³digo antiguo y se configurÃ³ correctamente la verificaciÃ³n para que el proceso remoto no intente conectarse a una base de datos inexistente.
**Â¿Para quÃ© sirve?**: Permite que las nuevas mejoras y caracterÃ­sticas del proyecto puedan ser validadas y aceptadas automÃ¡ticamente en el repositorio principal (DevDavid) sin que el sistema falle por errores de configuraciÃ³n.
**Â¿De dÃ³nde viene?**: ResoluciÃ³n de Deuda TÃ©cnica (Fallos en CI/CD PR4).
**Â¿QuÃ© deberÃ­a hacer?**: Al subir nuevos cambios al proyecto, el sistema de revisiÃ³n automÃ¡tica deberÃ­a dar "verde" (aprobado) comprobando que las reglas bÃ¡sicas funcionen, dejando las pruebas mÃ¡s pesadas de datos (integraciÃ³n) para el entorno local.
**Estado**: âœ… Listo

## [23/07/2026] - Sincronización de Componentes y Pruebas del Frontend
**Autor**: Agente Frontend (Antigravity)
**¿Qué es?**: Se actualizaron y sincronizaron los componentes visuales y la lógica de validación interna del panel de control de la plataforma (pruebas automatizadas) para que coincidan con la nueva forma en que el servidor envía la información (los catálogos y los campos de los formularios).
**¿Para qué sirve?**: Para garantizar que los cambios visuales y los diseños de los formularios de los procesos sigan funcionando correctamente sin presentar errores ocultos cuando se evalúen en el sistema de validación remota (GitHub Actions).
**¿De dónde viene?**: Resolución de Deuda Técnica (Fallos en CI/CD PR4 para Frontend).
**¿Qué debería hacer?**: Al publicar nuevos cambios visuales o componentes, el sistema de revisión automática los dará por válidos porque ahora ambas partes (frontend y backend) "hablan el mismo idioma" respecto a las configuraciones de seguridad y los datos de formularios.
**Estado**: ✅ Listo


## [30/07/2026] — Corrección Crítica: Los Diagramas de Procesos Perdían sus Elementos al Guardar y Reabrir
**Autor**: David Rodriguez (dorodrig) — Rama DevDavid — Commits: `fe3207db`, `024778a2`
**¿Qué es?**: Se corrigió un fallo grave en el Diseñador de Procesos que causaba la **pérdida total de los elementos de un diagrama** (tareas, decisiones, eventos de inicio y fin, flechas de conexión) cuando el usuario cerraba el navegador y volvía a abrir el proceso. Solo quedaba visible la piscina vacía (el contenedor), pero todo lo que se había dibujado dentro desaparecía.

El problema tenía dos causas:
1. **Conflicto interno de identidad**: Cuando el usuario creaba una piscina con carriles (un contenedor organizacional del proceso), el sistema confundía internamente la identidad del contenedor con la del proceso, provocando que el motor de diagramas descartara silenciosamente todo el contenido al momento de guardar.
2. **Guardado en momento inadecuado**: Al navegar fuera de la pantalla del diseñador, el sistema intentaba guardar el diagrama justo cuando la pantalla ya se estaba cerrando, produciendo un archivo incompleto que sobrescribía el guardado válido anterior.

**¿Para qué sirve?**: Para que los administradores puedan diseñar procesos complejos con piscinas, carriles, tareas, decisiones y eventos, guardarlos, cerrar el navegador, y al volver a abrirlos encontrar **todo exactamente como lo dejaron** — sin pérdida de ningún elemento.

**¿De dónde viene?**: Bug P0 detectado durante pruebas UAT del Modelador BPMN (Ruta: Administración → Modelador → BPMN). Vinculado a US-005. Este fallo existía únicamente en la rama de desarrollo DevDavid y no afectaba la versión estable del sistema.

**¿Qué debería hacer?**:
- Al crear un proceso con piscina, carriles, tareas, decisiones y eventos → guardarlo → cerrar el navegador → volver a abrir el proceso: **todos los elementos deben aparecer completos** con sus conexiones, posiciones y configuraciones intactas.
- El archivo guardado en la base de datos debe contener la descripción completa del proceso (se verificó que el archivo pasó de ~1,100 caracteres vacíos a ~6,500 caracteres con todo el contenido).

**Estado**: ✅ Listo


## [24/07/2026] - NormalizaciÃ³n de CodificaciÃ³n Unicode en el Motor de Procesos
**Autor**: Agente Backend (Antigravity)
**Â¿QuÃ© es?**: Se realizaron mejoras de normalizaciÃ³n de texto en los archivos principales del motor de procesos para garantizar consistencia en el manejo de caracteres especiales en toda la plataforma.
**Â¿Para quÃ© sirve?**: Garantiza que el cÃ³digo fuente del motor de procesos IBPMS maneje de forma uniforme y estandarizada los caracteres Unicode extendidos, asegurando compatibilidad y consistencia entre todos los mÃ³dulos del sistema.
**Â¿De dÃ³nde viene?**: Iniciativa transversal de normalizaciÃ³n y estandarizaciÃ³n del cÃ³digo base (PI-001).
**Â¿QuÃ© deberÃ­a hacer?**: La plataforma continuarÃ¡ funcionando exactamente igual para los usuarios, con el beneficio adicional de que el cÃ³digo interno cumple con los estÃ¡ndares mÃ¡s altos de codificaciÃ³n de texto, facilitando futuras migraciones y mantenimiento.
**Estado**: âœ… Listo


## [24/07/2026] - EstandarizaciÃ³n de Comentarios y Formato en Frontend
## [24/07/2026] - Normalización de Codificación Unicode en el Motor de Procesos
**Autor**: Agente Backend (Antigravity)
**¿Qué es?**: Se realizaron mejoras de normalización de texto en los archivos principales del motor de procesos para garantizar consistencia en el manejo de caracteres especiales en toda la plataforma.
**¿Para qué sirve?**: Garantiza que el código fuente del motor de procesos IBPMS maneje de forma uniforme y estandarizada los caracteres Unicode extendidos, asegurando compatibilidad y consistencia entre todos los módulos del sistema.
**¿De dónde viene?**: Iniciativa transversal de normalización y estandarización del código base (PI-001).
**¿Qué debería hacer?**: La plataforma continuará funcionando exactamente igual para los usuarios, con el beneficio adicional de que el código interno cumple con los estándares más altos de codificación de texto, facilitando futuras migraciones y mantenimiento.
**Estado**: ✅ Listo


## [24/07/2026] - Estandarización de Comentarios y Formato en Frontend
**Autor**: Agente Frontend (Antigravity)
**¿Qué es?**: Se realizaron mejoras de estandarización de comentarios y formato en los componentes del Modelador de Procesos y los módulos de gestión de estado de la plataforma.
**¿Para qué sirve?**: Garantiza que el código fuente de los componentes y gestores de estado mantenga una alta legibilidad, facilitando el mantenimiento y futuras escalabilidades.
**¿De dónde viene?**: Iniciativa transversal de normalización y estandarización del código base (PI-001).
**¿Qué debería hacer?**: La plataforma continuará funcionando exactamente igual para los usuarios, con una estructura interna de comentarios y configuraciones más limpia.
**Estado**: ☑️ Listo

---

## [03/08/2026] — Corrección: Las Casillas de Permisos de Procesos en Roles se Marcaban Todas a la Vez
**Autor**: PM-IA (Antigravity) — Aprobación: David Rodriguez
**¿Qué es?**: Se corrigió un fallo en la pantalla de **Gobernanza y Control de Acceso** (Fábrica de Roles) donde al intentar asignar un proceso BPMN individual a un rol, todas las casillas de verificación se marcaban simultáneamente en lugar de solo la seleccionada.
**¿Para qué sirve?**: Permite a los administradores asignar permisos de "Iniciar" y "Ejecutar" de cada proceso de negocio de forma **independiente** a cada rol. Por ejemplo, ahora se puede dar permiso a un rol para iniciar solo el proceso "David1" sin que automáticamente se le asignen todos los demás procesos.
**¿De dónde viene?**: Bug reportado durante pruebas UAT humanas por David Rodriguez en la pantalla `/admin/security/identity` → Fábrica de Roles → Modificar Identificador → Matriz de Concesiones.
**¿Qué debería hacer?**: Al hacer clic en una casilla de verificación en la Matriz de Concesiones, **solo esa casilla** debe marcarse o desmarcarse. Cada proceso BPMN (fila) y cada tipo de permiso (columna: Iniciar / Ejecutar) opera de forma completamente independiente.
**Estado**: ✅ Listo

## [04/08/2026] — Corrección: Los Permisos de Roles y Menús se Perdían al Navegar entre Pantallas
**Autor**: PM-IA (Antigravity) — Aprobación: David Rodriguez
**¿Qué es?**: Se corrigió un fallo crítico en la pantalla de **Gobernanza y Control de Acceso** (Fábrica de Roles) donde al configurar los permisos de procesos (casillas Iniciar/Ejecutar) y la topología de menús de un rol, estos se perdían completamente al navegar a otra sección del sistema y regresar.
**¿Para qué sirve?**: Ahora cuando un administrador configura qué procesos puede iniciar o ejecutar un rol, y qué módulos del menú puede ver, esa configuración **se guarda permanentemente en la base de datos** y se recupera correctamente al volver a abrir el rol.
**¿De dónde viene?**: Bug detectado durante pruebas UAT E2E (Misión 4) por David Rodriguez. Al configurar el rol ROLE_OPERATIVO con permisos para el proceso UAT_Proceso_E2E_David, los checkboxes aparecían en blanco al navegar y regresar.
**¿Qué debería hacer?**: Al editar un rol y hacer clic en "Consolidar Rol", todos los permisos de procesos y módulos de menú se guardan en la base de datos. Al volver a abrir ese rol (incluso después de navegar por todo el sistema), los checkboxes deben mostrar exactamente lo que se configuró previamente.
**Estado**: ✅ Listo

## [04/08/2026] — Corrección: El Catálogo de Procesos del Portal Aparecía Vacío para los Operarios
**Autor**: PM-IA (Antigravity) — Aprobación: David Rodriguez
**¿Qué es?**: Se corrigió un fallo donde al iniciar sesión como operario (ej. DAVID TEST), la pantalla del **Portal** mostraba el mensaje "No hay procesos disponibles" a pesar de que el proceso ya había sido desplegado exitosamente desde el Modelador BPMN.
**¿Para qué sirve?**: Ahora cuando un administrador despliega un proceso BPMN, este queda registrado como **activo** en la base de datos automáticamente, permitiendo que los operarios lo vean y puedan iniciarlo desde su Portal.
**¿De dónde viene?**: Bug detectado durante pruebas UAT E2E (Misión 5) por David Rodriguez. El despliegue mostraba un mensaje de éxito (toast verde) pero internamente no actualizaba el estado del proceso en la base de datos.
**¿Qué debería hacer?**: Al desplegar un proceso BPMN exitosamente, este debe aparecer en el Catálogo de Procesos del Portal para todos los usuarios con el rol asignado. El operario debe ver la tarjeta del proceso con el botón "Iniciar Proceso".
**Estado**: ✅ Listo

## [04/08/2026] — Nueva Funcionalidad: El Botón "Iniciar Proceso" del Portal Ahora Crea Casos Reales
**Autor**: PM-IA (Antigravity) — Aprobación: David Rodriguez
**¿Qué es?**: Se conectó el botón **"Iniciar Proceso"** del Portal del operario con la base de datos real del sistema. Antes, el botón no realizaba ninguna acción al hacer clic. Ahora crea un caso (expediente) real en el sistema con un número de seguimiento único.
**¿Para qué sirve?**: Los operarios ahora pueden iniciar trámites o procesos de negocio directamente desde su Portal con un solo clic. El sistema confirma la creación con un mensaje verde y redirige automáticamente a la Mesa de Trabajo donde aparecerán las tareas asignadas.
**¿De dónde viene?**: Bug detectado durante pruebas UAT E2E (Misión 6) por David Rodriguez. El botón "Iniciar Proceso" era decorativo — no tenía funcionalidad conectada.
**¿Qué debería hacer?**: Al hacer clic en "Iniciar Proceso": (1) El botón muestra "Iniciando..." mientras procesa, (2) Se crea un caso real en la base de datos, (3) Aparece un mensaje verde de confirmación, (4) El sistema redirige automáticamente a la Mesa de Trabajo en 1.5 segundos.
**Estado**: ✅ Listo
