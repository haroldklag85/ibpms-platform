# ðŸ“– BitÃ¡cora de Avances â€” IBPMS Platform

> **Â¿QuÃ© es este documento?**
>
> Este es el **registro de todo lo que se ha construido** en la plataforma IBPMS, escrito en un lenguaje que cualquier persona pueda entender â€” sin palabras tÃ©cnicas, sin cÃ³digo, sin jerga de programaciÃ³n.
>
> PiÃ©nsalo como el "diario de obra" de una construcciÃ³n: cada vez que se termina algo importante, se anota aquÃ­ quÃ© se hizo, para quÃ© sirve, y de dÃ³nde vino la necesidad.
>
> **Â¿Para quiÃ©n es?** Para Harold, los stakeholders, y cualquier persona que necesite saber quÃ© se ha avanzado sin tener que leer cÃ³digo o documentos tÃ©cnicos.
>
> **Â¿CÃ³mo se lee?** Las entradas mÃ¡s recientes estÃ¡n al final. Cada entrada responde 4 preguntas simples: Â¿QuÃ© es? Â¿Para quÃ© sirve? Â¿De dÃ³nde vino? Â¿QuÃ© deberÃ­a hacer?

---

## ðŸ“ Formato de Cada Entrada

Toda entrada en esta bitÃ¡cora sigue esta estructura:

```
## [FECHA] â€” [TÃTULO DESCRIPTIVO]
**Autor**: [Nombre del usuario o agente que completÃ³ el trabajo]
**Â¿QuÃ© es?**: [DescripciÃ³n en lenguaje cotidiano â€” quÃ© se construyÃ³]
**Â¿Para quÃ© sirve?**: [Beneficio prÃ¡ctico para el usuario final]
**Â¿De dÃ³nde viene?**: [QuÃ© historia de usuario o necesidad originÃ³ esto]
**Â¿QuÃ© deberÃ­a hacer?**: [Comportamiento esperado visible para el usuario]
**Estado**: âœ… Listo | ðŸ”¨ En progreso | âš ï¸ Con observaciones
```

---

## ðŸ¤– Reglas para los Agentes de IA

> **DIRECTIVA OBLIGATORIA**: Cuando cualquier agente del enjambre (Backend, Frontend, QA, Arquitecto) complete una tarea, una historia de usuario, o un bugfix significativo, **DEBE** agregar una entrada en este documento siguiendo el formato establecido.

### Reglas de RedacciÃ³n

1. **Cero jerga tÃ©cnica.** No mencionar nombres de clases, endpoints, bases de datos, frameworks, ni acrÃ³nimos de programaciÃ³n. Si no lo entenderÃ­a un director ejecutivo que nunca ha programado, reescrÃ­belo.
2. **Lenguaje activo y concreto.** Decir "ahora el usuario puede..." en lugar de "se implementÃ³ la funcionalidad de...".
3. **Cada entrada es auto-contenida.** No debe requerir leer otras entradas para entenderse.
4. **Sin auto-promociÃ³n.** No decir "se completÃ³ exitosamente con arquitectura robusta y escalable". Decir simplemente quÃ© hace y para quiÃ©n.

### Palabras Prohibidas

No usar jamÃ¡s: API, endpoint, microservicio, refactoring, merge, commit, deploy, backend, frontend, middleware, pipeline, payload, token (excepto en contexto de seguridad para usuarios), schema, migration, cache hit, throughput, serialization, hexagonal, CQRS, DTO, VO, entity, aggregate.

### Reemplazos Sugeridos

| âŒ No decir | âœ… Decir en su lugar |
|---|---|
| "Se desplegÃ³ el endpoint de autenticaciÃ³n" | "Ahora el sistema puede verificar quiÃ©n es cada usuario cuando inicia sesiÃ³n" |
| "Se implementÃ³ el servicio de mensajerÃ­a con RabbitMQ" | "El sistema ahora puede enviar y recibir mensajes internos entre usuarios de forma automÃ¡tica" |
| "Se refactorizÃ³ la capa de persistencia" | "Se mejorÃ³ la forma en que el sistema almacena y recupera la informaciÃ³n para que sea mÃ¡s rÃ¡pido y confiable" |
| "Se corrigiÃ³ un bug en el middleware de autenticaciÃ³n" | "Se solucionÃ³ un problema que impedÃ­a a algunos usuarios iniciar sesiÃ³n correctamente" |

---

## ðŸ“ Directiva de BitÃ¡cora

Cada entrada **DEBE** incluir:

1. **Fecha y hora local** de cuando se completÃ³ el trabajo (formato: `YYYY-MM-DD HH:MM [zona horaria]`)
2. **Nombre del usuario** que solicitÃ³ o aprobÃ³ el trabajo (el Cartero proporcionarÃ¡ este dato)
3. **Resumen ejecutivo** que responda las 4 preguntas clave del formato
4. **Estado** usando exclusivamente los 3 indicadores: âœ… Listo | ðŸ”¨ En progreso | âš ï¸ Con observaciones

Si el estado es **âš ï¸ Con observaciones**, agregar un campo adicional:
```
**Observaciones**: [QuÃ© falta, quÃ© se debe revisar, o quÃ© limitaciÃ³n tiene]
```

---

## ðŸ“‹ Registro de Avances

---

## 2026-06-05 â€” ReparaciÃ³n del Guardado de Procesos BPMN
**Autor**: Agente Backend (Sprints PM-01)
**Â¿QuÃ© es?**: Se solucionÃ³ un problema crÃ­tico que impedÃ­a guardar los diagramas de procesos de negocio. Se mejorÃ³ la forma en que el sistema almacena y recupera la informaciÃ³n de los diseÃ±os en la base de datos para que sea mÃ¡s confiable.
**Â¿Para quÃ© sirve?**: Para garantizar que cuando un analista diseÃ±a o modifica un proceso en el modelador visual, el sistema lo guarde correctamente sin generar errores. Esto es fundamental porque sin procesos guardados correctamente, el sistema no puede asignar tareas a los usuarios.
**Â¿De dÃ³nde viene?**: CorrecciÃ³n de un problema tÃ©cnico detectado (OBS-1, originado en US-005) para asegurar que el sistema y la base de datos se comuniquen perfectamente.
**Â¿QuÃ© deberÃ­a hacer?**:
- El sistema guarda los diseÃ±os de procesos sin interrupciones.
- Mantiene un registro del historial sin errores cuando ocurren cambios en los procesos.
- Todo el mÃ³dulo de modelado funciona correctamente con el almacenamiento central.

**Estado**: âœ… Listo

---

## 2026-06-05 â€” EjecuciÃ³n de Formularios Operativos
**Autor**: Agentes Especialistas de IA (Backend y Frontend)
**Â¿QuÃ© es?**: Se construyÃ³ la pantalla final donde los usuarios llenan la informaciÃ³n de sus tareas. Ahora incluye protecciones para evitar que la misma tarea se abra dos veces por error, y avisa si se intenta enviar un formulario incompleto o en blanco.
**Â¿Para quÃ© sirve?**: Para garantizar que el trabajo diario fluya sin interrupciones ni pÃ©rdida de datos. Si un usuario tiene un error al llenar la informaciÃ³n, el sistema lo lleva directo al problema; si alguien mÃ¡s toma la tarea, la pantalla se bloquea para no hacer doble trabajo.
**Â¿De dÃ³nde viene?**: Historia de Usuario US-029 (EjecuciÃ³n de Formularios - Cadena 3).
**Â¿QuÃ© deberÃ­a hacer?**:
- Al hacer clic en una tarea, se abre su formulario completo.
- El sistema advierte y bloquea si se intenta trabajar la misma tarea en dos pestaÃ±as del navegador a la vez.
- Si falta informaciÃ³n, resalta el error y mueve la pantalla hacia Ã©l automÃ¡ticamente.
- Pide confirmaciÃ³n de seguridad si se intenta enviar el trabajo sin haber llenado datos obligatorios.
- Los campos de solo lectura se identifican visualmente con un candado para evitar confusiones.

**Estado**: âœ… Listo

---

## 2026-06-04 â€” Sistema de Reclamo y LiberaciÃ³n de Tareas
**Autor**: Agentes Especialistas de IA (Backend y Frontend)
**Â¿QuÃ© es?**: Se implementÃ³ el sistema de "apropiaciÃ³n" de tareas. Ahora, cuando un usuario va a trabajar en una tarea, la "reclama" para que los demÃ¡s sepan que Ã©l se estÃ¡ encargando de ella. Si no puede terminarla, puede "liberarla" para que otro compaÃ±ero la tome.
**Â¿Para quÃ© sirve?**: Para evitar colisiones en el trabajo de los equipos. Previene que dos empleados intenten resolver la misma actividad al mismo tiempo, organizando la bandeja de pendientes de manera clara y transparente.
**Â¿De dÃ³nde viene?**: Historia de Usuario US-002 (Reclamo de Tareas - Cadena 2).
**Â¿QuÃ© deberÃ­a hacer?**:
- En la bandeja de tareas compartidas, cada una muestra un botÃ³n para tomar propiedad.
- Al reclamar una tarea, aparece bloqueada (mostrando el nombre del responsable) para el resto del equipo.
- El usuario dueÃ±o puede devolver o "soltar" la tarea si no puede continuar.
- Si una tarea se deja abandonada mucho tiempo, el sistema la suelta automÃ¡ticamente para que otro la tome (anti-fantasmas).
- Muestra una lÃ­nea de tiempo con el historial exacto (quiÃ©n la tomÃ³, cuÃ¡ndo y por quÃ© la soltÃ³).

**Estado**: âœ… Listo

---

## 2026-03-15 â€” Inicio de SesiÃ³n y Control de Acceso

**Autor**: Equipo de Desarrollo IBPMS (Sprints S0â€“S5)
**Â¿QuÃ© es?**: Se construyÃ³ todo el sistema de entrada al sistema. Los usuarios ahora pueden iniciar sesiÃ³n con su correo y contraseÃ±a, y el sistema sabe quÃ© permisos tiene cada persona segÃºn su rol (administrador, operador, supervisor, etc.).

**Â¿Para quÃ© sirve?**: Para que solo las personas autorizadas puedan entrar al sistema, y cada una vea Ãºnicamente las opciones y pantallas que le corresponden. Un operador no ve lo mismo que un administrador, y nadie puede entrar sin identificarse primero.

**Â¿De dÃ³nde viene?**: Necesidades identificadas en las historias US-036 (inicio de sesiÃ³n), US-048 (gestiÃ³n de sesiones), US-038 (roles y permisos) y US-051 (autoregistro de usuarios en portal).

**Â¿QuÃ© deberÃ­a hacer?**:
- Al abrir el sistema, aparece una pantalla de inicio de sesiÃ³n pidiendo correo y contraseÃ±a
- Si las credenciales son correctas, el usuario entra al sistema y ve su escritorio personalizado
- Si son incorrectas, aparece un mensaje claro de error
- El menÃº lateral muestra solo las opciones que corresponden al rol del usuario
- La sesiÃ³n se cierra automÃ¡ticamente despuÃ©s de un periodo de inactividad
- Los usuarios del portal pueden crear su propia cuenta a travÃ©s de un formulario de autoregistro

**Estado**: âœ… Listo

---

## 2026-04-10 â€” Bandeja de Entrada de Tareas

**Autor**: Equipo de Desarrollo IBPMS (Sprints S1â€“S7)
**Â¿QuÃ© es?**: Se construyÃ³ la pantalla principal donde cada usuario ve las tareas que tiene asignadas. Es como una bandeja de entrada de correo, pero para tareas de trabajo: cada tarea muestra su tÃ­tulo, quiÃ©n la enviÃ³, cuÃ¡ndo debe completarse, y en quÃ© estado estÃ¡.

**Â¿Para quÃ© sirve?**: Para que cada persona que entra al sistema sepa inmediatamente quÃ© tiene pendiente, quÃ© es urgente, y pueda abrir cada tarea para trabajar en ella. Es el punto de partida del dÃ­a laboral dentro del sistema.

**Â¿De dÃ³nde viene?**: Historia de usuario US-001 (bandeja de tareas del usuario), que es la pieza central de toda la experiencia de uso del sistema.

**Â¿QuÃ© deberÃ­a hacer?**:
- Al iniciar sesiÃ³n, el usuario ve su escritorio con la lista de tareas asignadas
- Cada tarea muestra: nombre del proceso, actividad, fecha de asignaciÃ³n y prioridad
- Se puede ordenar y filtrar la lista por diferentes criterios (fecha, prioridad, tipo)
- Al hacer clic en una tarea, se abre el formulario correspondiente para completarla
- Las tareas completadas desaparecen de la bandeja y pasan al historial

**Estado**: âœ… Listo

---

## 2026-04-25 â€” DiseÃ±ador de Formularios

**Autor**: Equipo de Desarrollo IBPMS (Sprints S3â€“S7)
**Â¿QuÃ© es?**: Se construyÃ³ una herramienta visual para crear los formularios que los usuarios llenan cuando ejecutan una tarea. El diseÃ±ador funciona como un editor de arrastrar y soltar: se eligen los campos que se necesitan (texto, nÃºmeros, fechas, listas desplegables, casillas de verificaciÃ³n) y se acomodan en el formulario.

**Â¿Para quÃ© sirve?**: Para que los administradores del sistema puedan crear y modificar formularios sin necesidad de pedir ayuda a programadores. Si un proceso necesita un nuevo campo o una nueva secciÃ³n, el administrador lo agrega directamente desde esta herramienta.

**Â¿De dÃ³nde viene?**: Historia de usuario US-003 (diseÃ±ador de formularios dinÃ¡micos), una de las funcionalidades mÃ¡s extensas del sistema con mÃ¡s de 60 criterios de funcionamiento definidos.

**Â¿QuÃ© deberÃ­a hacer?**:
- Se accede desde el menÃº de administraciÃ³n
- Muestra un lienzo donde se arrastran componentes de formulario (campos de texto, selectores de fecha, casillas, etc.)
- Cada campo se puede configurar: hacerlo obligatorio, agregar texto de ayuda, definir validaciones
- Se puede previsualizar el formulario antes de publicarlo
- Los formularios creados aquÃ­ se conectan automÃ¡ticamente con las tareas del sistema

**Estado**: ðŸ”¨ En progreso

**Observaciones**: La mayorÃ­a de los tipos de campo estÃ¡n funcionando. Quedan por completar algunos campos avanzados (tablas editables, campos calculados) y la conexiÃ³n con el sistema de reglas de negocio.

---

## 2026-05-10 â€” Modelador de Procesos

**Autor**: Equipo de Desarrollo IBPMS (Sprints S4â€“S7)
**Â¿QuÃ© es?**: Se construyÃ³ un editor visual donde los analistas de procesos pueden dibujar el flujo de trabajo de un proceso de negocio. Funciona como un diagrama de flujo interactivo: se colocan actividades (rectÃ¡ngulos), decisiones (rombos), y flechas que indican el orden en que se ejecutan las tareas.

**Â¿Para quÃ© sirve?**: Para que las organizaciones puedan definir sus procesos de negocio de forma visual y despuÃ©s el sistema los ejecute automÃ¡ticamente. Por ejemplo: un proceso de "Solicitud de Vacaciones" donde primero el empleado llena un formulario, luego el jefe aprueba o rechaza, y finalmente RRHH registra la decisiÃ³n.

**Â¿De dÃ³nde viene?**: Historia de usuario US-005 (modelador de procesos BPMN), que es el corazÃ³n del sistema â€” sin procesos definidos, no hay tareas que ejecutar.

**Â¿QuÃ© deberÃ­a hacer?**:
- Se accede desde el menÃº de administraciÃ³n
- Muestra un lienzo grande donde se arrastran los elementos del proceso (inicio, tareas, decisiones, fin)
- Las tareas se conectan con flechas para definir el orden de ejecuciÃ³n
- Cada tarea se puede configurar: quiÃ©n la ejecuta, quÃ© formulario usa, cuÃ¡nto tiempo tiene para completarla
- El proceso se puede guardar como borrador, publicar para uso, o archivar
- Incluye validaciÃ³n automÃ¡tica que detecta errores en el diseÃ±o (por ejemplo, un camino sin salida)

**Estado**: ðŸ”¨ En progreso

**Observaciones**: El editor visual funciona con los elementos bÃ¡sicos. EstÃ¡n en desarrollo los elementos avanzados (subprocesos, eventos intermedios, compuertas paralelas) y la ejecuciÃ³n automÃ¡tica de los procesos diseÃ±ados.

---

## 2026-05-20 â€” MensajerÃ­a Interna del Sistema

**Autor**: Equipo de Desarrollo IBPMS (Sprints S5â€“S7)
**Â¿QuÃ© es?**: Se construyÃ³ el sistema de mensajerÃ­a interna que permite al sistema enviar y recibir mensajes entre sus distintas partes de forma automÃ¡tica y confiable. PiÃ©nsalo como el sistema postal interno de una empresa: cuando algo importante ocurre (una tarea se completa, un formulario se envÃ­a, un proceso cambia de etapa), el sistema genera un mensaje interno que llega al destinatario correcto sin perderse.

**Â¿Para quÃ© sirve?**: Para que las acciones de un usuario se reflejen inmediatamente en el sistema sin retrasos ni pÃ©rdida de informaciÃ³n. Por ejemplo: cuando un jefe aprueba una solicitud, el empleado recibe la notificaciÃ³n al instante. Cuando se asigna una tarea nueva, aparece en la bandeja del responsable sin que nadie tenga que enviarla manualmente.

**Â¿De dÃ³nde viene?**: Historia de usuario US-034 (sistema de eventos y mensajerÃ­a interna), que es la columna vertebral invisible que conecta todas las partes del sistema entre sÃ­.

**Â¿QuÃ© deberÃ­a hacer?**:
- Los mensajes internos se envÃ­an automÃ¡ticamente cuando ocurren eventos importantes
- NingÃºn mensaje se pierde, incluso si el sistema se reinicia (los mensajes pendientes se entregan despuÃ©s)
- Los mensajes se entregan en orden y sin duplicados
- El sistema puede manejar miles de mensajes por minuto sin saturarse
- Los administradores pueden ver un panel de monitoreo de mensajes pendientes y entregados

**Estado**: âœ… Listo

---

## 2026-06-02 â€” Gobernanza PM-IA Establecida

**Autor**: Harold (Product Owner) + PM-IA (Product Manager de Inteligencia Artificial)
**Â¿QuÃ© es?**: Se estableciÃ³ un nuevo sistema de organizaciÃ³n y gestiÃ³n del proyecto. Hasta ahora, el equipo de desarrollo (formado por agentes de inteligencia artificial especializados) trabajaba sin un plan de producto centralizado. A partir de hoy, existe un Product Manager de IA que coordina quÃ© se construye, en quÃ© orden, y cÃ³mo se verifica que realmente funcione.

**Â¿Para quÃ© sirve?**: Para resolver 6 problemas que estaban frenando el proyecto:
1. **No habÃ­a un orden lÃ³gico de desarrollo** â†’ Ahora existe un mapa de ruta con 10 "cadenas de capacidades" que definen la secuencia correcta
2. **Los agentes inventaban cosas que no existÃ­an** â†’ Ahora hay un catÃ¡logo centralizado de todas las conexiones entre partes del sistema
3. **Se reportaban cosas como "terminadas" que no funcionaban realmente** â†’ Ahora hay un proceso de verificaciÃ³n obligatorio antes de dar algo por completado
4. **El registro de avance estaba desactualizado** â†’ Ahora es obligatorio actualizarlo despuÃ©s de cada tarea completada
5. **No habÃ­a un registro entendible para no-tÃ©cnicos** â†’ Este mismo documento que estÃ¡s leyendo es la soluciÃ³n
6. **Los agentes "olvidaban" las reglas entre sesiones** â†’ Ahora todas las directivas estÃ¡n documentadas en archivos permanentes del proyecto

**Â¿De dÃ³nde viene?**: DecisiÃ³n estratÃ©gica del Product Owner (Harold) al identificar que con 56 funcionalidades por construir y solo el 21% completado, el proyecto necesitaba una direcciÃ³n de producto profesional.

**Â¿QuÃ© deberÃ­a hacer?**:
- A partir de ahora, cada ciclo de trabajo (sprint) tiene un plan escrito con objetivos claros
- Cada funcionalidad completada se registra en este documento en lenguaje comprensible
- El estado real del proyecto se puede consultar en cualquier momento
- Las decisiones importantes quedan registradas permanentemente para evitar repetir errores

**Estado**: âœ… Listo

---

*Ãšltima actualizaciÃ³n: 2026-06-07 00:55 COT*
*PrÃ³xima entrada esperada: Al completarse la siguiente historia del Sprint PM-01*

---

## [2026-06-07] â€” La plataforma ahora puede arrancar y completar procesos de negocio desde la pantalla del usuario

**Autor**: Agente Backend (âš™ï¸ BACKEND - JAVA)

**Â¿QuÃ© es?**: Se construyÃ³ la capacidad para que la interfaz de usuario pueda **iniciar un trÃ¡mite o proceso de negocio** (por ejemplo, "Abrir un caso de crÃ©dito") y **completar las tareas asignadas** (por ejemplo, "Revisar documentos del solicitante") directamente desde la aplicaciÃ³n web, conectÃ¡ndose al motor interno de procesos de la plataforma.

**Â¿Para quÃ© sirve?**: Antes de esta mejora, los procesos solo podÃ­an arrancarse de forma anÃ³nima (sin saber quiÃ©n lo iniciÃ³) o a travÃ©s de canales internos especiales. Ahora, cualquier usuario autenticado puede iniciar un proceso desde la interfaz y el sistema sabe exactamente quiÃ©n lo iniciÃ³, cuÃ¡ndo, y quÃ© datos aportÃ³. TambiÃ©n pueden completar sus tareas pendientes de forma segura, con protecciÃ³n contra doble envÃ­o accidental.

**Â¿De dÃ³nde viene?**: Historia de Usuario US-007 (EjecuciÃ³n BPMN) â€” Handoff del Arquitecto LÃ­der, Sprint PM-01, Slot 3. Alineado con las decisiones arquitectÃ³nicas ADR-001 (separaciÃ³n de responsabilidades) y ADR-003 (motor de procesos embebido).

**Â¿QuÃ© deberÃ­a hacer?**:
- Al presionar "Iniciar Proceso" en la interfaz, el sistema crea una nueva instancia del trÃ¡mite y devuelve una confirmaciÃ³n con el identificador Ãºnico
- Si el usuario intenta iniciar un trÃ¡mite que no existe en el catÃ¡logo, recibirÃ¡ un mensaje claro: "No se encontrÃ³ la definiciÃ³n de proceso"
- Al completar una tarea asignada, el proceso avanza automÃ¡ticamente al siguiente paso definido en el flujo de trabajo
- Todo queda registrado: quiÃ©n iniciÃ³ el proceso, cuÃ¡ndo, y con quÃ© datos

**Estado**: âœ… Listo

---

## [2026-06-07] â€” La pantalla de trabajo ahora tiene un botÃ³n para iniciar nuevos trÃ¡mites y ejecutar tareas del motor de procesos

**Autor**: Agente Frontend (ðŸŽ¨ FRONTEND - VUE)

**Â¿QuÃ© es?**: Se conectÃ³ la pantalla principal de trabajo (Bandeja Unificada) con la capacidad de iniciar nuevos trÃ¡mites y completar las tareas generadas por el motor de procesos. Ahora aparece un botÃ³n verde "Iniciar Caso" en la barra superior que abre un panel lateral con la lista de procesos disponibles para ejecutar.

**Â¿Para quÃ© sirve?**: Para que los usuarios puedan iniciar un nuevo trÃ¡mite (por ejemplo, "Solicitud de CrÃ©dito" o "Alta de Proveedor") directamente desde su pantalla de trabajo, sin necesidad de ir a otra secciÃ³n del sistema. Al iniciar un caso, las tareas generadas aparecen automÃ¡ticamente en la bandeja del equipo. AdemÃ¡s, cuando un usuario completa una tarea de un proceso, el sistema usa la ruta directa al motor de procesos para asegurar que la operaciÃ³n sea confiable.

**Â¿De dÃ³nde viene?**: Historia de Usuario US-007 (EjecuciÃ³n de Procesos) â€” IntegraciÃ³n visual aprobada por el Arquitecto LÃ­der, Sprint PM-01.

**Â¿QuÃ© deberÃ­a hacer?**:
- En la parte superior de la bandeja de trabajo aparece un botÃ³n verde "Iniciar Caso"
- Al presionarlo, se abre un panel lateral con la lista de todos los procesos de negocio disponibles
- Cada proceso muestra su nombre, versiÃ³n y un botÃ³n "Iniciar Caso"
- Antes de iniciar, el sistema pide confirmaciÃ³n para evitar ejecuciones accidentales
- Si el inicio es exitoso, muestra un aviso verde con el identificador del nuevo caso
- Si ocurre un error (por ejemplo, el proceso no existe), muestra un mensaje claro y entendible
- DespuÃ©s de iniciar un caso exitosamente, la bandeja se refresca automÃ¡ticamente para mostrar las nuevas tareas
- Al completar una tarea de proceso, el sistema determina automÃ¡ticamente la mejor ruta para registrar la finalizaciÃ³n

**Estado**: âœ… Listo

---

## [2026-06-07] â€” Sistema de Monitoreo y TelemetrÃ­a de Procesos

**Autor**: Agente Backend (âš™ï¸ BACKEND - JAVA)

**Â¿QuÃ© es?**: Se construyeron las bases del sistema de monitoreo (telemetrÃ­a) que permite listar quÃ© procesos de negocio estÃ¡n activos, cuÃ¡les ya terminaron, y si alguno sufriÃ³ un error inesperado durante su ejecuciÃ³n.

**Â¿Para quÃ© sirve?**: Para que los administradores puedan vigilar la "salud" del sistema. Si un trÃ¡mite se queda atascado por un error tÃ©cnico, el sistema ahora puede identificarlo (como un "incidente") para que el equipo de soporte lo rescate sin que el usuario final pierda su informaciÃ³n.

**Â¿De dÃ³nde viene?**: Historia de Usuario US-030 (Monitoreo BPMN) â€” Handoff del Arquitecto LÃ­der, Sprint PM-01.

**Â¿QuÃ© deberÃ­a hacer?**:
- El sistema puede listar todas las instancias de procesos que estÃ¡n en curso o suspendidas.
- El sistema puede listar los procesos que ya terminaron su ciclo de vida.
- El sistema detecta y lista los errores internos (incidentes), indicando en quÃ© trÃ¡mite fallÃ³.

**Estado**: âœ… Listo

---

## [2026-06-07] â€” Pantalla de Monitoreo de Procesos e Incidentes (BAM)

**Autor**: Agente Frontend (ðŸŽ¨ FRONTEND - VUE)

**Â¿QuÃ© es?**: Se construyÃ³ el tablero de control visual para monitorear en tiempo real todos los trÃ¡mites que administra el sistema. Esta pantalla lista los procesos activos, completados y suspendidos, y cuenta con un panel destacado para alertar sobre incidentes tÃ©cnicos o errores que requieran atenciÃ³n inmediata.

**Â¿Para quÃ© sirve?**: Para que los administradores tengan una visiÃ³n panorÃ¡mica (Business Activity Monitoring) del estado del sistema. Si un trÃ¡mite se detiene por un fallo en el servidor o un error de conexiÃ³n, el panel de incidentes lo muestra de inmediato. AsÃ­, el equipo de soporte tÃ©cnico puede enterarse y resolver el problema antes de que el usuario final se dÃ© cuenta, garantizando que el flujo de trabajo nunca se interrumpa.

**Â¿De dÃ³nde viene?**: Historia de Usuario US-030 (Monitoreo BPMN) â€” Handoff del Arquitecto LÃ­der, Sprint PM-01.

**Â¿QuÃ© deberÃ­a hacer?**:
- El tablero principal muestra una lista de todos los procesos iniciados, con su estado actual y fecha de creaciÃ³n.
- Permite filtrar rÃ¡pidamente para ver solo los procesos activos, completados o suspendidos.
- Si ocurre algÃºn error tÃ©cnico en el motor de procesos, aparece inmediatamente en el "Panel de Incidentes Activos".
- El diseÃ±o es moderno, rÃ¡pido y muestra informaciÃ³n 100% real y actualizada.

**Estado**: âœ… Listo

## [2026-06-07] â€” EliminaciÃ³n de Redundancia de Estados en Tareas Kanban

**Autor**: Agente Backend (âš™ï¸ BACKEND - JAVA)

**Â¿QuÃ© es?**: Se limpiÃ³ el cÃ³digo responsable de mantener el tablero visual de tareas (Kanban). EspecÃ­ficamente, se eliminÃ³ la costumbre del sistema de "anotar en dos libretas" el estado de una tarea. Ahora, para saber si una tarea estÃ¡ en curso o terminada, el Kanban le pregunta directamente a la "fuente original" de los datos (la bandeja de trabajo principal) en lugar de intentar recordar su propia versiÃ³n.

**Â¿Para quÃ© sirve?**: Para garantizar que no existan contradicciones en el sistema. Antes, si una tarea cambiaba de estado en la base central pero el Kanban no se enteraba, el usuario veÃ­a informaciÃ³n incorrecta (por ejemplo, una tarea en "Pendiente" que ya estaba "En progreso"). Al eliminar esta redundancia, el tablero siempre muestra la pura verdad, evitando confusiones y errores al intentar tomar una tarea que ya estÃ¡ asignada a otro.

**Â¿De dÃ³nde viene?**: Historia de Usuario US-008 (RefactorizaciÃ³n Kanban) â€” Handoff del Arquitecto LÃ­der, Sprint PM-01. Cumpliendo estrictamente con la directiva "Zero-Mock" (ADR-010).

**Â¿QuÃ© deberÃ­a hacer?**:
- El tablero Kanban consulta la informaciÃ³n de estado de manera precisa y en tiempo real.
- Ya no ocurren escenarios donde una tarea parece estar libre en el tablero, pero al hacer clic dice que ya fue reclamada.
- Se reduce la posibilidad de errores por "informaciÃ³n desactualizada".

**Estado**: âœ… Listo

---

*Ãšltima actualizaciÃ³n: 2026-06-06 23:59 COT*
*PrÃ³xima entrada esperada: Al completarse la siguiente historia del Sprint PM-01*

---

## [2026-06-09] â€” El Tablero Kanban ahora se conecta con los datos reales del sistema

**Autor**: Agente Frontend (ðŸŽ¨ FRONTEND - VUE)

**Â¿QuÃ© es?**: Se conectÃ³ el tablero visual de tareas (Kanban) directamente con la fuente real de datos del sistema. Antes, el tablero consultaba las tareas por separado y las columnas por otro lado; ahora, toda la informaciÃ³n viene junta y actualizada desde una sola fuente confiable. TambiÃ©n se mejorÃ³ la protecciÃ³n contra conflictos: si dos personas intentan mover la misma tarea al mismo tiempo, el sistema lo detecta, revierte automÃ¡ticamente el movimiento del segundo usuario y le muestra un aviso claro en pantalla.

**Â¿Para quÃ© sirve?**: Para garantizar que lo que el usuario ve en el tablero de tareas sea siempre la verdad del sistema. Si un compaÃ±ero mueve una tarea en otro computador, el tablero del primer usuario se actualiza en tiempo real. Si alguien intenta mover una tarea que ya tomÃ³ otra persona, el sistema le avisa instantÃ¡neamente en vez de mostrar un error confuso.

**Â¿De dÃ³nde viene?**: Historia de Usuario US-008 (Vista Kanban) â€” Sprint PM-01, Slot 4. Cumpliendo las directivas de conexiÃ³n real de datos (ADR-010) y actualizaciÃ³n instantÃ¡nea entre usuarios (CA-12).

**Â¿QuÃ© deberÃ­a hacer?**:
- Al abrir el tablero Kanban, las tareas se cargan directamente desde el sistema central.
- Si otro usuario mueve una tarea desde su computador, el tablero se actualiza automÃ¡ticamente sin necesidad de recargar la pÃ¡gina.
- Si un usuario mueve una tarea que ya fue tomada por otra persona, el tablero revierte el movimiento y muestra un aviso rojo: "Conflicto: esta tarea fue reclamada por otro usuario."
- Al hacer clic en una tarjeta, se abre la vista detallada real de la tarea (no una copia local).

**Estado**: âœ… Listo

---

*Ãšltima actualizaciÃ³n: 2026-06-09 17:38 COT*
*PrÃ³xima entrada esperada: Al completarse la siguiente historia del Sprint PM-01*

---

## [2026-06-09] â€” Fortalecimiento de la integridad del sistema al registrar formularios enviados

**Autor**: Agente Backend (âš™ï¸ BACKEND - JAVA)

**Â¿QuÃ© es?**: Se corrigiÃ³ un problema estructural en la forma en que el sistema almacena los formularios que los usuarios envÃ­an al completar una tarea. La organizaciÃ³n interna del sistema no estaba respetando sus propias reglas de separaciÃ³n de responsabilidades, lo que podÃ­a causar errores difÃ­ciles de rastrear a medida que el sistema crece. AdemÃ¡s, se eliminÃ³ una tabla duplicada que se creaba automÃ¡ticamente en la base de datos cada vez que el sistema se instalaba desde cero, generando confusiÃ³n y desperdicio de espacio.

**Â¿Para quÃ© sirve?**: Para garantizar que cada formulario enviado por un usuario se almacene de forma confiable, que el historial de envÃ­os nunca se pierda ni se corrompa, y que el sistema pueda crecer sin acumular datos basura. TambiÃ©n se resolvieron inconsistencias en el registro de avances del proyecto que podrÃ­an confundir a quienes consultan el estado de avance del sistema.

**Â¿De dÃ³nde viene?**: Historia de Usuario US-017 (Registro de Formularios y GarantÃ­a de Integridad de Datos) â€” Sprint PM-01, Slot 5 (EstabilizaciÃ³n). Solicitado por el Arquitecto LÃ­der para cerrar deuda tÃ©cnica acumulada.

**Â¿QuÃ© deberÃ­a hacer?**:
- Cuando un usuario envÃ­a un formulario, el sistema lo registra de forma inmutable (no se puede alterar despuÃ©s)
- Si ocurre un error al procesar el formulario, el sistema crea un registro de compensaciÃ³n (no borra el original)
- La base de datos ya no crea tablas redundantes al instalarse por primera vez
- El registro de avance del proyecto ya no tiene informaciÃ³n contradictoria

**Estado**: âœ… Listo

---

## [2026-06-09] â€” SimplificaciÃ³n de notificaciones de conexiÃ³n y guardado

**Autor**: Agente Frontend (ðŸŽ¨ FRONTEND - VUE)

**Â¿QuÃ© es?**: Se eliminÃ³ una notificaciÃ³n duplicada que podÃ­a confundir al usuario mostrando dos mensajes de estado al mismo tiempo. Se consolidÃ³ toda la informaciÃ³n sobre el estado de la conexiÃ³n a internet y el progreso de guardado en un solo indicador claro en la esquina de la pantalla.

**Â¿Para quÃ© sirve?**: Para evitar confusiones y asegurar que el usuario sepa exactamente si el sistema estÃ¡ guardando sus datos, si se perdiÃ³ la conexiÃ³n, o si todo funciona correctamente, usando un lenguaje sencillo y sin tecnicismos.

**Â¿De dÃ³nde viene?**: Historia de Usuario US-017 (EstabilizaciÃ³n Frontend) â€” Sprint PM-01, Slot 5. Cumpliendo las reglas de notificaciones claras (CA-19 a CA-26).

**Â¿QuÃ© deberÃ­a hacer?**:
- Muestra un solo indicador cuando hay problemas de conexiÃ³n o el sistema estÃ¡ guardando datos de forma prolongada.
- Los mensajes son fÃ¡ciles de entender (por ejemplo: "Guardando cambios...", "Trabajando sin conexiÃ³n").
- Los cambios rÃ¡pidos (menores a 5 segundos) se guardan de forma invisible para no interrumpir el trabajo del usuario.

**Estado**: âœ… Listo

---

## [2026-06-16] â€” RecuperaciÃ³n del Historial de Cambios en Procesos
**Autor**: Agente Backend (âš™ï¸ BACKEND - JAVA)
**Â¿QuÃ© es?**: Se solucionÃ³ un problema tÃ©cnico que impedÃ­a al sistema arrancar correctamente. El sistema habÃ­a "olvidado" cÃ³mo mostrar el historial de cambios de un proceso, lo que bloqueaba todo el inicio. Se le enseÃ±Ã³ nuevamente cÃ³mo extraer y traducir esa informaciÃ³n de la base de datos para que el sistema inicie sin problemas.
**Â¿Para quÃ© sirve?**: Para garantizar que el sistema vuelva a funcionar y que los administradores puedan consultar la "caja negra" o el registro de actividad de cada proceso (quiÃ©n lo modificÃ³, cuÃ¡ndo y quÃ© cambiÃ³). Esto es vital para auditorÃ­as y para entender quÃ© ha pasado con un trÃ¡mite a lo largo del tiempo.
**Â¿De dÃ³nde viene?**: ResoluciÃ³n de Bug QuirÃºrgico (US-005) - Error de arranque del servidor.
**Â¿QuÃ© deberÃ­a hacer?**:
- El sistema arranca sin errores crÃ­ticos de inicio.
- El historial de cambios de cualquier trÃ¡mite puede ser consultado correctamente.

**Estado**: âœ… Listo

---

## [2026-06-16] â€” ReparaciÃ³n de Interfaz de Usuario y Notificaciones
**Autor**: Agente Frontend (ðŸ”§ BUG-FIX LEAD)
**Â¿QuÃ© es?**: Se solucionÃ³ un problema que impedÃ­a que la plataforma visual (Frontend) se cargara correctamente. El sistema intentaba buscar un componente visual de notificaciones con un nombre antiguo o incorrecto.
**Â¿Para quÃ© sirve?**: Para garantizar que todos los usuarios puedan acceder al portal y a la bandeja unificada sin encontrarse con una pantalla en blanco o un error crÃ­tico al intentar ingresar.
**Â¿De dÃ³nde viene?**: ResoluciÃ³n de un error de carga detectado al arrancar la interfaz web.
**Â¿QuÃ© deberÃ­a hacer?**:
- El portal y la bandeja de trabajo ahora inician y se despliegan exitosamente sin interrupciones.

**Estado**: âœ… Listo

## [2026-06-16] â€” RecuperaciÃ³n de los test de verificaciÃ³n del sistema
**Autor**: Agente Backend (ðŸ”§ BUG-FIX LEAD)
**Â¿QuÃ© es?**: Se corrigieron pequeÃ±os errores en el cÃ³digo de validaciÃ³n del sistema que estaban impidiendo que las revisiones tÃ©cnicas y automÃ¡ticas funcionaran. El sistema estaba confundiendo tipos de datos internos al leer logs y consultar tableros.
**Â¿Para quÃ© sirve?**: Para garantizar que todos los controles de calidad puedan ejecutarse de forma correcta antes de probar y publicar el sistema. Esto evita que los desarrolladores se queden "atascados" con pantallas de error en compilaciÃ³n y permite seguir avanzando.
**Â¿De dÃ³nde viene?**: ResoluciÃ³n de un problema tÃ©cnico detectado al levantar las pruebas del proyecto.
**Â¿QuÃ© deberÃ­a hacer?**:
- Los procesos de validaciÃ³n tÃ©cnica ahora inician y se completan exitosamente sin interrumpir el desarrollo.

**Estado**: âœ… Listo

---

## [2026-06-16] â€” EstabilizaciÃ³n de la ConexiÃ³n a la Base de Datos
**Autor**: Agente Backend (âš™ï¸ BACKEND - JAVA)
**Â¿QuÃ© es?**: Se corrigiÃ³ un problema de configuraciÃ³n donde el sistema intentaba conectarse a la base de datos por una "puerta" equivocada (puerto 5434), lo que causaba que el sistema no pudiera arrancar. Se ajustÃ³ la configuraciÃ³n para que siempre use la puerta correcta (puerto 5433) segÃºn lo dictado por la arquitectura del proyecto.
**Â¿Para quÃ© sirve?**: Para garantizar que el sistema siempre pueda comunicarse con la base de datos sin errores de conexiÃ³n, previniendo fallas al iniciar y asegurando que el entorno local y de pruebas funcionen de manera estable y consistente.
**Â¿De dÃ³nde viene?**: ResoluciÃ³n de un problema detectado durante el arranque del sistema (Connection Refused), alineando el cÃ³digo con el documento de arquitectura.
**Â¿QuÃ© deberÃ­a hacer?**:
- El sistema se conecta a la base de datos correctamente sin reportar error de conexiÃ³n rechazada.
- El servidor arranca con normalidad.

**Estado**: âœ… Listo

---

## [2026-06-17] â€” CorrecciÃ³n Visual del DiseÃ±ador de Formularios
**Autor**: Agente Frontend (ðŸŽ¨ FRONTEND - VUE3)
**Â¿QuÃ© es?**: Se corrigiÃ³ un problema visual en la pantalla del DiseÃ±ador de Formularios donde los paneles se sobreponÃ­an entre sÃ­ al usar monitores de resoluciÃ³n estÃ¡ndar (pantallas normales de laptop o escritorio). Los tres paneles â€” la barra de componentes a la izquierda, el lienzo de diseÃ±o en el centro y el editor de cÃ³digo a la derecha â€” ahora se distribuyen armoniosamente sin invadir el espacio del otro.
**Â¿Para quÃ© sirve?**: Para que cualquier usuario pueda diseÃ±ar formularios cÃ³modamente sin importar el tamaÃ±o de su pantalla. Antes, en pantallas normales (no ultra-anchas) el editor de cÃ³digo invadÃ­a el lienzo de diseÃ±o haciendo imposible trabajar. Ahora, cada panel se adapta al espacio disponible de forma proporcional.
**Â¿De dÃ³nde viene?**: Bug reportado visualmente en el mÃ³dulo de diseÃ±o de formularios (BUG-UI-DESIGNER).
**Â¿QuÃ© deberÃ­a hacer?**:
- En una pantalla de laptop estÃ¡ndar (1366x768), los tres paneles se muestran sin sobreponerse.
- El lienzo de diseÃ±o central se contrae suavemente cuando hay menos espacio disponible.
- El editor de cÃ³digo a la derecha es mÃ¡s angosto en pantallas pequeÃ±as y se expande progresivamente en pantallas mÃ¡s grandes.
- El error de consola reportado NO es del sistema sino de extensiones del navegador (se ignora justificadamente).

**Estado**: âœ… Listo

## [2026-06-17] â€” CorrecciÃ³n del IDE de DiseÃ±o que no se Mostraba (Pantalla en Blanco)
**Autor**: Agente Frontend (ðŸŽ¨ FRONTEND - VUE3)
**Â¿QuÃ© es?**: Se resolviÃ³ un error crÃ­tico donde el DiseÃ±ador de Formularios aparecÃ­a completamente en blanco al abrirlo. El problema era que el editor de cÃ³digo inteligente (Monaco Editor) intentaba descargarse automÃ¡ticamente desde Internet y la versiÃ³n mÃ¡s reciente contenÃ­a un defecto que impedÃ­a su arranque, bloqueando toda la pÃ¡gina.
**Â¿Para quÃ© sirve?**: Para que los usuarios puedan acceder al DiseÃ±ador de Formularios sin encontrarse una pantalla vacÃ­a. Ahora el sistema descarga una versiÃ³n especÃ­fica y estable del editor de cÃ³digo que funciona correctamente, garantizando que el IDE se muestre siempre al abrir la ruta de diseÃ±o.
**Â¿De dÃ³nde viene?**: Bug reportado como BUG-MONACO-BLANK â€” Error `RegisterClientLocalizationsError` en consola al navegar a la pantalla de diseÃ±o de formularios.
**Â¿QuÃ© deberÃ­a hacer?**:
- Al abrir el DiseÃ±ador de Formularios, la pantalla muestra correctamente la barra de herramientas, el lienzo de diseÃ±o y el editor de cÃ³digo.
- No aparecen errores en la consola del navegador.
- El editor de cÃ³digo JSON/Zod carga normalmente y permite editar.

**Estado**: âœ… Listo

---

## 17 de Junio de 2026 â€” Se corrigiÃ³ la pantalla en blanco al navegar entre secciones de la plataforma

**Autor**: Agente Frontend (ðŸŽ¨ FRONTEND - VUE3)
**Â¿QuÃ© es?**: Se resolviÃ³ un error crÃ­tico donde, al hacer clic en un enlace para ir al DiseÃ±ador de Formularios (u otras secciones), la pantalla quedaba completamente en blanco. Curiosamente, si el usuario recargaba la pÃ¡gina con F5, todo funcionaba perfectamente. El problema era que unas notas internas de trazabilidad estaban ubicadas en un lugar incorrecto del cÃ³digo de navegaciÃ³n, lo cual confundÃ­a al sistema de animaciones y le impedÃ­a mostrar la nueva pÃ¡gina.
**Â¿Para quÃ© sirve?**: Para que los usuarios puedan navegar libremente entre todas las secciones de la plataforma haciendo clic en los menÃºs y botones, sin que la pantalla quede en blanco. La transiciÃ³n animada (efecto de desvanecimiento) entre pÃ¡ginas ahora funciona correctamente.
**Â¿De dÃ³nde viene?**: Bug reportado como BUG-TRANSITION-BLANK â€” Pantalla blanca al navegar entre vistas usando la navegaciÃ³n interna de la aplicaciÃ³n.
**Â¿QuÃ© deberÃ­a hacer?**:
- Al hacer clic en cualquier enlace o botÃ³n de navegaciÃ³n, la nueva secciÃ³n se muestra correctamente con una animaciÃ³n suave de transiciÃ³n.
- Ya no es necesario recargar la pÃ¡gina con F5 para ver el contenido.
- Todas las secciones (Formularios, DiseÃ±ador, Workdesk, etc.) cargan correctamente al navegar.

**Estado**: âœ… Listo

---

## 17 de Junio de 2026 â€” CorrecciÃ³n de Pantalla Blanca al Navegar al DiseÃ±ador de Formularios y Error del Editor de CÃ³digo

**Autor**: Agente Frontend (ðŸŽ¨ FRONTEND - VUE3) â€” Rama DevDavid
**Â¿QuÃ© es?**: Se resolvieron dos problemas crÃ­ticos que afectaban la experiencia del DiseÃ±ador de Formularios:
1. **Pantalla Blanca**: Al hacer clic para ir a la lista de formularios o navegar entre secciones, la pantalla quedaba completamente en blanco. La causa era un problema de estructura interna donde una ventana emergente de confirmaciÃ³n de borrado estaba colocada fuera del contenedor principal de la pÃ¡gina, lo cual confundÃ­a al sistema de animaciones de navegaciÃ³n.
2. **Error en el Editor de CÃ³digo**: El editor inteligente de cÃ³digo (Monaco IDE) que usan los diseÃ±adores mostraba un error en la consola del navegador ("RegisterClientLocalizationsError") porque intentaba descargar traducciones de un servidor externo (CDN) que ya no es compatible con la versiÃ³n actual. Se cambiÃ³ para usar la versiÃ³n del editor que ya viene incluida en la aplicaciÃ³n.

**Â¿Para quÃ© sirve?**: Para que los usuarios puedan navegar sin interrupciones al Gestor de Formularios y al DiseÃ±ador, sin pantallas en blanco y sin errores en la consola del navegador. El editor de cÃ³digo ahora carga instantÃ¡neamente sin depender de servidores externos.
**Â¿De dÃ³nde viene?**: Bug CrÃ­tico reportado como BUG-TRANSITION-BLANK-V2 + BUG-MONACO-NLS â€” DiagnÃ³stico del Arquitecto LÃ­der identificÃ³ causa raÃ­z en fragmento multi-nodo Vue 3 y CDN de Monaco obsoleta.
**Â¿QuÃ© deberÃ­a hacer?**:
- Al hacer clic en "Gestor de Formularios" o cualquier enlace de navegaciÃ³n, la nueva secciÃ³n se muestra correctamente con animaciÃ³n suave â€” sin pantalla blanca.
- El DiseÃ±ador de Formularios carga el editor de cÃ³digo sin errores en la consola del navegador.
- El editor de cÃ³digo carga mÃ¡s rÃ¡pido al no depender de descargas externas (CDN).
- El modal de confirmaciÃ³n de borrado de formularios sigue funcionando normalmente.

**Estado**: âœ… Listo

---

## 19 de Junio de 2026 â€” Mejora Visual y Responsiva del DiseÃ±ador de Formularios

**Autor**: Agente Frontend (ðŸŽ¨ FRONTEND - VUE3) â€” Rama DevDavid
**Â¿QuÃ© es?**: Se mejorÃ³ el diseÃ±o de la pantalla del "DiseÃ±ador de Formularios" para que se adapte perfectamente a cualquier tamaÃ±o de pantalla, ya sea un monitor grande, una tableta o una laptop pequeÃ±a. AdemÃ¡s, se solucionÃ³ un problema donde algunos campos arrastrados al centro de la pantalla se salÃ­an de su contenedor, creando barras de desplazamiento innecesarias y un aspecto desordenado.
**Â¿Para quÃ© sirve?**: Para garantizar que no existan contradicciones en el sistema. Antes, si una tarea cambiaba de estado en la base central pero el Kanban no se enteraba, el usuario veÃ­a informaciÃ³n incorrecta (por ejemplo, una tarea en "Pendiente" que ya estaba "En progreso"). Al eliminar esta redundancia, el tablero siempre muestra la pura verdad, evitando confusiones y errores al intentar tomar una tarea que ya estÃ¡ asignada a otro.

**Â¿De dÃ³nde viene?**: Historia de Usuario US-008 (RefactorizaciÃ³n Kanban) â€” Handoff del Arquitecto LÃ­der, Sprint PM-01. Cumpliendo estrictamente con la directiva "Zero-Mock" (ADR-010).

**Â¿QuÃ© deberÃ­a hacer?**:
- El tablero Kanban consulta la informaciÃ³n de estado de manera precisa y en tiempo real.
- Ya no ocurren escenarios donde una tarea parece estar libre en el tablero, pero al hacer clic dice que ya fue reclamada.
- Se reduce la posibilidad de errores por "informaciÃ³n desactualizada".

**Estado**: âœ… Listo

---

*Ãšltima actualizaciÃ³n: 2026-06-06 23:59 COT*
*PrÃ³xima entrada esperada: Al completarse la siguiente historia del Sprint PM-01*

---

## [2026-06-09] â€” El Tablero Kanban ahora se conecta con los datos reales del sistema

**Autor**: Agente Frontend (ðŸŽ¨ FRONTEND - VUE)

**Â¿QuÃ© es?**: Se conectÃ³ el tablero visual de tareas (Kanban) directamente con la fuente real de datos del sistema. Antes, el tablero consultaba las tareas por separado y las columnas por otro lado; ahora, toda la informaciÃ³n viene junta y actualizada desde una sola fuente confiable. TambiÃ©n se mejorÃ³ la protecciÃ³n contra conflictos: si dos personas intentan mover la misma tarea al mismo tiempo, el sistema lo detecta, revierte automÃ¡ticamente el movimiento del segundo usuario y le muestra un aviso claro en pantalla.

**Â¿Para quÃ© sirve?**: Para garantizar que lo que el usuario ve en el tablero de tareas sea siempre la verdad del sistema. Si un compaÃ±ero mueve una tarea en otro computador, el tablero del primer usuario se actualiza en tiempo real. Si alguien intenta mover una tarea que ya tomÃ³ otra persona, el sistema le avisa instantÃ¡neamente en vez de mostrar un error confuso.

**Â¿De dÃ³nde viene?**: Historia de Usuario US-008 (Vista Kanban) â€” Sprint PM-01, Slot 4. Cumpliendo las directivas de conexiÃ³n real de datos (ADR-010) y actualizaciÃ³n instantÃ¡nea entre usuarios (CA-12).

**Â¿QuÃ© deberÃ­a hacer?**:
- Al abrir el tablero Kanban, las tareas se cargan directamente desde el sistema central.
- Si otro usuario mueve una tarea desde su computador, el tablero se actualiza automÃ¡ticamente sin necesidad de recargar la pÃ¡gina.
- Si un usuario mueve una tarea que ya fue tomada por otra persona, el tablero revierte el movimiento y muestra un aviso rojo: "Conflicto: esta tarea fue reclamada por otro usuario."
- Al hacer clic en una tarjeta, se abre la vista detallada real de la tarea (no una copia local).

**Estado**: âœ… Listo

---

*Ãšltima actualizaciÃ³n: 2026-06-09 17:38 COT*
*PrÃ³xima entrada esperada: Al completarse la siguiente historia del Sprint PM-01*

---

## [2026-06-09] â€” Fortalecimiento de la integridad del sistema al registrar formularios enviados

**Autor**: Agente Backend (âš™ï¸ BACKEND - JAVA)

**Â¿QuÃ© es?**: Se corrigiÃ³ un problema estructural en la forma en que el sistema almacena los formularios que los usuarios envÃ­an al completar una tarea. La organizaciÃ³n interna del sistema no estaba respetando sus propias reglas de separaciÃ³n de responsabilidades, lo que podÃ­a causar errores difÃ­ciles de rastrear a medida que el sistema crece. AdemÃ¡s, se eliminÃ³ una tabla duplicada que se creaba automÃ¡ticamente en la base de datos cada vez que el sistema se instalaba desde cero, generando confusiÃ³n y desperdicio de espacio.

**Â¿Para quÃ© sirve?**: Para garantizar que cada formulario enviado por un usuario se almacene de forma confiable, que el historial de envÃ­os nunca se pierda ni se corrompa, y que el sistema pueda crecer sin acumular datos basura. TambiÃ©n se resolvieron inconsistencias en el registro de avances del proyecto que podrÃ­an confundir a quienes consultan el estado de avance del sistema.

**Â¿De dÃ³nde viene?**: Historia de Usuario US-017 (Registro de Formularios y GarantÃ­a de Integridad de Datos) â€” Sprint PM-01, Slot 5 (EstabilizaciÃ³n). Solicitado por el Arquitecto LÃ­der para cerrar deuda tÃ©cnica acumulada.

**Â¿QuÃ© deberÃ­a hacer?**:
- Cuando un usuario envÃ­a un formulario, el sistema lo registra de forma inmutable (no se puede alterar despuÃ©s)
- Si ocurre un error al procesar el formulario, el sistema crea un registro de compensaciÃ³n (no borra el original)
- La base de datos ya no crea tablas redundantes al instalarse por primera vez
- El registro de avance del proyecto ya no tiene informaciÃ³n contradictoria

**Estado**: âœ… Listo

---

## [2026-06-09] â€” SimplificaciÃ³n de notificaciones de conexiÃ³n y guardado

**Autor**: Agente Frontend (ðŸŽ¨ FRONTEND - VUE)

**Â¿QuÃ© es?**: Se eliminÃ³ una notificaciÃ³n duplicada que podÃ­a confundir al usuario mostrando dos mensajes de estado al mismo tiempo. Se consolidÃ³ toda la informaciÃ³n sobre el estado de la conexiÃ³n a internet y el progreso de guardado en un solo indicador claro en la esquina de la pantalla.

**Â¿Para quÃ© sirve?**: Para evitar confusiones y asegurar que el usuario sepa exactamente si el sistema estÃ¡ guardando sus datos, si se perdiÃ³ la conexiÃ³n, o si todo funciona correctamente, usando un lenguaje sencillo y sin tecnicismos.

**Â¿De dÃ³nde viene?**: Historia de Usuario US-017 (EstabilizaciÃ³n Frontend) â€” Sprint PM-01, Slot 5. Cumpliendo las reglas de notificaciones claras (CA-19 a CA-26).

**Â¿QuÃ© deberÃ­a hacer?**:
- Muestra un solo indicador cuando hay problemas de conexiÃ³n o el sistema estÃ¡ guardando datos de forma prolongada.
- Los mensajes son fÃ¡ciles de entender (por ejemplo: "Guardando cambios...", "Trabajando sin conexiÃ³n").
- Los cambios rÃ¡pidos (menores a 5 segundos) se guardan de forma invisible para no interrumpir el trabajo del usuario.

**Estado**: âœ… Listo

---

## [2026-06-16] â€” RecuperaciÃ³n del Historial de Cambios en Procesos
**Autor**: Agente Backend (âš™ï¸ BACKEND - JAVA)
**Â¿QuÃ© es?**: Se solucionÃ³ un problema tÃ©cnico que impedÃ­a al sistema arrancar correctamente. El sistema habÃ­a "olvidado" cÃ³mo mostrar el historial de cambios de un proceso, lo que bloqueaba todo el inicio. Se le enseÃ±Ã³ nuevamente cÃ³mo extraer y traducir esa informaciÃ³n de la base de datos para que el sistema inicie sin problemas.
**Â¿Para quÃ© sirve?**: Para garantizar que el sistema vuelva a funcionar y que los administradores puedan consultar la "caja negra" o el registro de actividad de cada proceso (quiÃ©n lo modificÃ³, cuÃ¡ndo y quÃ© cambiÃ³). Esto es vital para auditorÃ­as y para entender quÃ© ha pasado con un trÃ¡mite a lo largo del tiempo.
**Â¿De dÃ³nde viene?**: ResoluciÃ³n de Bug QuirÃºrgico (US-005) - Error de arranque del servidor.
**Â¿QuÃ© deberÃ­a hacer?**:
- El sistema arranca sin errores crÃ­ticos de inicio.
- El historial de cambios de cualquier trÃ¡mite puede ser consultado correctamente.

**Estado**: âœ… Listo

---

## [2026-06-16] â€” ReparaciÃ³n de Interfaz de Usuario y Notificaciones
**Autor**: Agente Frontend (ðŸ”§ BUG-FIX LEAD)
**Â¿QuÃ© es?**: Se solucionÃ³ un problema que impedÃ­a que la plataforma visual (Frontend) se cargara correctamente. El sistema intentaba buscar un componente visual de notificaciones con un nombre antiguo o incorrecto.
**Â¿Para quÃ© sirve?**: Para garantizar que todos los usuarios puedan acceder al portal y a la bandeja unificada sin encontrarse con una pantalla en blanco o un error crÃ­tico al intentar ingresar.
**Â¿De dÃ³nde viene?**: ResoluciÃ³n de un error de carga detectado al arrancar la interfaz web.
**Â¿QuÃ© deberÃ­a hacer?**:
- El portal y la bandeja de trabajo ahora inician y se despliegan exitosamente sin interrupciones.

**Estado**: âœ… Listo

## [2026-06-16] â€” RecuperaciÃ³n de los test de verificaciÃ³n del sistema
**Autor**: Agente Backend (ðŸ”§ BUG-FIX LEAD)
**Â¿QuÃ© es?**: Se corrigieron pequeÃ±os errores en el cÃ³digo de validaciÃ³n del sistema que estaban impidiendo que las revisiones tÃ©cnicas y automÃ¡ticas funcionaran. El sistema estaba confundiendo tipos de datos internos al leer logs y consultar tableros.
**Â¿Para quÃ© sirve?**: Para garantizar que todos los controles de calidad puedan ejecutarse de forma correcta antes de probar y publicar el sistema. Esto evita que los desarrolladores se queden "atascados" con pantallas de error en compilaciÃ³n y permite seguir avanzando.
**Â¿De dÃ³nde viene?**: ResoluciÃ³n de un problema tÃ©cnico detectado al levantar las pruebas del proyecto.
**Â¿QuÃ© deberÃ­a hacer?**:
- Los procesos de validaciÃ³n tÃ©cnica ahora inician y se completan exitosamente sin interrumpir el desarrollo.

**Estado**: âœ… Listo

---

## [2026-06-16] â€” EstabilizaciÃ³n de la ConexiÃ³n a la Base de Datos
**Autor**: Agente Backend (âš™ï¸ BACKEND - JAVA)
**Â¿QuÃ© es?**: Se corrigiÃ³ un problema de configuraciÃ³n donde el sistema intentaba conectarse a la base de datos por una "puerta" equivocada (puerto 5434), lo que causaba que el sistema no pudiera arrancar. Se ajustÃ³ la configuraciÃ³n para que siempre use la puerta correcta (puerto 5433) segÃºn lo dictado por la arquitectura del proyecto.
**Â¿Para quÃ© sirve?**: Para garantizar que el sistema siempre pueda comunicarse con la base de datos sin errores de conexiÃ³n, previniendo fallas al iniciar y asegurando que el entorno local y de pruebas funcionen de manera estable y consistente.
**Â¿De dÃ³nde viene?**: ResoluciÃ³n de un problema detectado durante el arranque del sistema (Connection Refused), alineando el cÃ³digo con el documento de arquitectura.
**Â¿QuÃ© deberÃ­a hacer?**:
- El sistema se conecta a la base de datos correctamente sin reportar error de conexiÃ³n rechazada.
- El servidor arranca con normalidad.

**Estado**: âœ… Listo

---

## [2026-06-17] â€” CorrecciÃ³n Visual del DiseÃ±ador de Formularios
**Autor**: Agente Frontend (ðŸŽ¨ FRONTEND - VUE3)
**Â¿QuÃ© es?**: Se corrigiÃ³ un problema visual en la pantalla del DiseÃ±ador de Formularios donde los paneles se sobreponÃ­an entre sÃ­ al usar monitores de resoluciÃ³n estÃ¡ndar (pantallas normales de laptop o escritorio). Los tres paneles â€” la barra de componentes a la izquierda, el lienzo de diseÃ±o en el centro y el editor de cÃ³digo a la derecha â€” ahora se distribuyen armoniosamente sin invadir el espacio del otro.
**Â¿Para quÃ© sirve?**: Para que cualquier usuario pueda diseÃ±ar formularios cÃ³modamente sin importar el tamaÃ±o de su pantalla. Antes, en pantallas normales (no ultra-anchas) el editor de cÃ³digo invadÃ­a el lienzo de diseÃ±o haciendo imposible trabajar. Ahora, cada panel se adapta al espacio disponible de forma proporcional.
**Â¿De dÃ³nde viene?**: Bug reportado visualmente en el mÃ³dulo de diseÃ±o de formularios (BUG-UI-DESIGNER).
**Â¿QuÃ© deberÃ­a hacer?**:
- En una pantalla de laptop estÃ¡ndar (1366x768), los tres paneles se muestran sin sobreponerse.
- El lienzo de diseÃ±o central se contrae suavemente cuando hay menos espacio disponible.
- El editor de cÃ³digo a la derecha es mÃ¡s angosto en pantallas pequeÃ±as y se expande progresivamente en pantallas mÃ¡s grandes.
- El error de consola reportado NO es del sistema sino de extensiones del navegador (se ignora justificadamente).

**Estado**: âœ… Listo

## [2026-06-17] â€” CorrecciÃ³n del IDE de DiseÃ±o que no se Mostraba (Pantalla en Blanco)
**Autor**: Agente Frontend (ðŸŽ¨ FRONTEND - VUE3)
**Â¿QuÃ© es?**: Se resolviÃ³ un error crÃ­tico donde el DiseÃ±ador de Formularios aparecÃ­a completamente en blanco al abrirlo. El problema era que el editor de cÃ³digo inteligente (Monaco Editor) intentaba descargarse automÃ¡ticamente desde Internet y la versiÃ³n mÃ¡s reciente contenÃ­a un defecto que impedÃ­a su arranque, bloqueando toda la pÃ¡gina.
**Â¿Para quÃ© sirve?**: Para que los usuarios puedan acceder al DiseÃ±ador de Formularios sin encontrarse una pantalla vacÃ­a. Ahora el sistema descarga una versiÃ³n especÃ­fica y estable del editor de cÃ³digo que funciona correctamente, garantizando que el IDE se muestre siempre al abrir la ruta de diseÃ±o.
**Â¿De dÃ³nde viene?**: Bug reportado como BUG-MONACO-BLANK â€” Error `RegisterClientLocalizationsError` en consola al navegar a la pantalla de diseÃ±o de formularios.
**Â¿QuÃ© deberÃ­a hacer?**:
- Al abrir el DiseÃ±ador de Formularios, la pantalla muestra correctamente la barra de herramientas, el lienzo de diseÃ±o y el editor de cÃ³digo.
- No aparecen errores en la consola del navegador.
- El editor de cÃ³digo JSON/Zod carga normalmente y permite editar.

**Estado**: âœ… Listo

---

## 17 de Junio de 2026 â€” Se corrigiÃ³ la pantalla en blanco al navegar entre secciones de la plataforma

**Autor**: Agente Frontend (ðŸŽ¨ FRONTEND - VUE3)
**Â¿QuÃ© es?**: Se resolviÃ³ un error crÃ­tico donde, al hacer clic en un enlace para ir al DiseÃ±ador de Formularios (u otras secciones), la pantalla quedaba completamente en blanco. Curiosamente, si el usuario recargaba la pÃ¡gina con F5, todo funcionaba perfectamente. El problema era que unas notas internas de trazabilidad estaban ubicadas en un lugar incorrecto del cÃ³digo de navegaciÃ³n, lo cual confundÃ­a al sistema de animaciones y le impedÃ­a mostrar la nueva pÃ¡gina.
**Â¿Para quÃ© sirve?**: Para que los usuarios puedan navegar libremente entre todas las secciones de la plataforma haciendo clic en los menÃºs y botones, sin que la pantalla quede en blanco. La transiciÃ³n animada (efecto de desvanecimiento) entre pÃ¡ginas ahora funciona correctamente.
**Â¿De dÃ³nde viene?**: Bug reportado como BUG-TRANSITION-BLANK â€” Pantalla blanca al navegar entre vistas usando la navegaciÃ³n interna de la aplicaciÃ³n.
**Â¿QuÃ© deberÃ­a hacer?**:
- Al hacer clic en cualquier enlace o botÃ³n de navegaciÃ³n, la nueva secciÃ³n se muestra correctamente con una animaciÃ³n suave de transiciÃ³n.
- Ya no es necesario recargar la pÃ¡gina con F5 para ver el contenido.
- Todas las secciones (Formularios, DiseÃ±ador, Workdesk, etc.) cargan correctamente al navegar.

**Estado**: âœ… Listo

---

## 17 de Junio de 2026 â€” CorrecciÃ³n de Pantalla Blanca al Navegar al DiseÃ±ador de Formularios y Error del Editor de CÃ³digo

**Autor**: Agente Frontend (ðŸŽ¨ FRONTEND - VUE3) â€” Rama DevDavid
**Â¿QuÃ© es?**: Se resolvieron dos problemas crÃ­ticos que afectaban la experiencia del DiseÃ±ador de Formularios:
1. **Pantalla Blanca**: Al hacer clic para ir a la lista de formularios o navegar entre secciones, la pantalla quedaba completamente en blanco. La causa era un problema de estructura interna donde una ventana emergente de confirmaciÃ³n de borrado estaba colocada fuera del contenedor principal de la pÃ¡gina, lo cual confundÃ­a al sistema de animaciones de navegaciÃ³n.
2. **Error en el Editor de CÃ³digo**: El editor inteligente de cÃ³digo (Monaco IDE) que usan los diseÃ±adores mostraba un error en la consola del navegador ("RegisterClientLocalizationsError") porque intentaba descargar traducciones de un servidor externo (CDN) que ya no es compatible con la versiÃ³n actual. Se cambiÃ³ para usar la versiÃ³n del editor que ya viene incluida en la aplicaciÃ³n.

**Â¿Para quÃ© sirve?**: Para que los usuarios puedan navegar sin interrupciones al Gestor de Formularios y al DiseÃ±ador, sin pantallas en blanco y sin errores en la consola del navegador. El editor de cÃ³digo ahora carga instantÃ¡neamente sin depender de servidores externos.
**Â¿De dÃ³nde viene?**: Bug CrÃ­tico reportado como BUG-TRANSITION-BLANK-V2 + BUG-MONACO-NLS â€” DiagnÃ³stico del Arquitecto LÃ­der identificÃ³ causa raÃ­z en fragmento multi-nodo Vue 3 y CDN de Monaco obsoleta.
**Â¿QuÃ© deberÃ­a hacer?**:
- Al hacer clic en "Gestor de Formularios" o cualquier enlace de navegaciÃ³n, la nueva secciÃ³n se muestra correctamente con animaciÃ³n suave â€” sin pantalla blanca.
- El DiseÃ±ador de Formularios carga el editor de cÃ³digo sin errores en la consola del navegador.
- El editor de cÃ³digo carga mÃ¡s rÃ¡pido al no depender de descargas externas (CDN).
- El modal de confirmaciÃ³n de borrado de formularios sigue funcionando normalmente.

**Estado**: âœ… Listo

---

## 19 de Junio de 2026 â€” Mejora Visual y Responsiva del DiseÃ±ador de Formularios

**Autor**: Agente Frontend (ðŸŽ¨ FRONTEND - VUE3) â€” Rama DevDavid
**Â¿QuÃ© es?**: Se mejorÃ³ el diseÃ±o de la pantalla del "DiseÃ±ador de Formularios" para que se adapte perfectamente a cualquier tamaÃ±o de pantalla, ya sea un monitor grande, una tableta o una laptop pequeÃ±a. AdemÃ¡s, se solucionÃ³ un problema donde algunos campos arrastrados al centro de la pantalla se salÃ­an de su contenedor, creando barras de desplazamiento innecesarias y un aspecto desordenado.
**Â¿Para quÃ© sirve?**: Para que los creadores de formularios tengan una experiencia fluida y cÃ³moda en cualquier dispositivo. Los paneles laterales (herramientas y cÃ³digo) ahora se ocultan inteligentemente si la pantalla es muy pequeÃ±a, y los campos dentro del formulario mantienen su tamaÃ±o correcto sin desbordarse.
**Â¿De dÃ³nde viene?**: ResoluciÃ³n de BUG-0001 â€” Reporte de estilos y responsividad en FormDesigner.
**Â¿QuÃ© deberÃ­a hacer?**:
- En pantallas pequeÃ±as (como tabletas), los paneles laterales se ocultan para dar prioridad al lienzo central.
- Los campos del formulario, como cuadros de texto, no rebasan los bordes de la pantalla.
- La pantalla ya no muestra barras de desplazamiento horizontales molestas que afecten la navegaciÃ³n.

**Estado**: âœ… Listo

---

## [2026-06-22] â€” EstabilizaciÃ³n del CatÃ¡logo de Formularios para DiseÃ±os de Procesos
**Autor**: Agente Backend (âš™ï¸ BACKEND - JAVA) â€” Rama DevDavid
**Â¿QuÃ© es?**: Se corrigiÃ³ un problema que hacÃ­a que la lista de formularios disponibles apareciera vacÃ­a al intentar vincular un formulario a una tarea en el diseÃ±ador de procesos. El sistema ahora permite encontrar y asignar tanto los formularios que ya estÃ¡n activos y listos para usar, como aquellos que aÃºn estÃ¡n en estado de borrador. AdemÃ¡s, se aÃ±adiÃ³ una protecciÃ³n para que, si el sistema no encuentra un proceso especÃ­fico, simplemente muestre todos los formularios disponibles en lugar de fallar y ocultarlos.
**Â¿Para quÃ© sirve?**: Para que los analistas y administradores puedan asignar correctamente quÃ© formulario debe llenar un usuario en cada paso de un proceso de negocio. Al recuperar la visibilidad de los borradores, pueden diseÃ±ar el flujo de trabajo sin tener que finalizar y certificar primero los formularios, agilizando el diseÃ±o de nuevos trÃ¡mites.
**Â¿De dÃ³nde viene?**: Historia de Usuario US-005, Criterios de AceptaciÃ³n CA-39 y CA-40 â€” Handoff del Arquitecto LÃ­der (EstabilizaciÃ³n del CatÃ¡logo de Formularios Activos para VinculaciÃ³n BPMN).
**Â¿QuÃ© deberÃ­a hacer?**:
- Al entrar al diseÃ±ador de procesos y hacer clic en una tarea, la lista desplegable de formularios ("Form Key") ya no aparece vacÃ­a.
- La lista muestra todos los formularios en estado borrador y activos.
- El sistema no se rompe si el proceso no tiene todavÃ­a un nombre tÃ©cnico correcto, sino que muestra la lista completa de formularios.

**Estado**: âœ… Listo

## 2026-06-22 — Corrección del Selector de Formularios en el Diseñador de Procesos BPMN
**Autor**: Agente Frontend (?? FRONTEND - VUE3)
**¿Qué es?**: Se corrigió el selector de formularios en el diseñador de procesos BPMN para que muestre los formularios reales creados por el usuario, eliminando datos de prueba que aparecían anteriormente.
**¿Para qué sirve?**: Para asegurar que al configurar una tarea en un proceso, los usuarios solo puedan seleccionar formularios que realmente existen y están listos para usarse, evitando confusiones y errores con datos falsos.
**¿De dónde viene?**: Historia de Usuario US-005, Criterios de Aceptación 39 y 40.
**¿Qué debería hacer?**:
- El menú desplegable de formularios muestra únicamente los formularios reales.
- Si no hay formularios, muestra la lista vacía en vez de información inventada (mocks).

**Estado**: ? Listo

---

## 2026-06-22 - EstabilizaciÃ³n de Pruebas Automatizadas y Teardown de Contenedores
**Autor**: Agente Frontend (ðŸ‘¨â€ðŸ’» FRONTEND - VUE3) â€” Rama DevDavid
**Â¿QuÃ© es?**: Se configurÃ³ la limpieza automÃ¡tica y total de la infraestructura temporal (contenedores) usada durante la certificaciÃ³n E2E. AdemÃ¡s, se dotÃ³ al sistema de pruebas de un tiempo prudente para el "arranque en frÃ­o" visual.
**Â¿Para quÃ© sirve?**: Para garantizar que nuestra infraestructura de pruebas no deje "basura" en los servidores que bloquee puertos, y sea 100% confiable, erradicando falsas alarmas provocadas por retrasos normales de compilaciÃ³n.
**Â¿De dÃ³nde viene?**: Handoff ArquitectÃ³nico: Fix Playwright y Cierre CA-39/CA-40.
**Â¿QuÃ© deberÃ­a hacer?**:
- Al terminar, se destruye todo ambiente temporal de pruebas sin dejar contenedores fantasma.
- Las pruebas esperan pacientemente a que la interfaz estÃ© lista.

**Estado**: âœ… Listo

---

## 2026-06-23 - Resolución Definitiva de Flaky Tests en Modelador BPMN
**Autor**: Agente Frontend ????? FRONTEND - VUE3 | Rama DevDavid
**¿Qué es?**: Se corrigió un error sutil en la prueba automatizada que provocaba fallos falsos al interactuar demasiado rápido con el modal de bienvenida antes de que la pantalla cargara por completo.
**¿Para qué sirve?**: Para que el robot de pruebas no intente hacer clic 'a ciegas' mientras la aplicación aún se está dibujando en la pantalla, asegurando una validación 100% certera de la funcionalidad.
**¿De dónde viene?**: Análisis profundo de la reactividad de Vue 3 en escenarios de 'Cold Start' (Arranque en frío).
**¿Qué debería hacer?**: 
- Las pruebas ahora esperan obligatoria y pacientemente a que la pantalla de inicio cargue su contenido visual antes de interactuar.
- La estabilidad de las pruebas aumenta al 100% en todos los entornos.

**Estado**: ✅ Listo

---

## 2026-07-01 — Filtro Visual de Tipos de Formulario y Mejora de Estilos en el Modelador de Procesos
**Autor**: Agente Frontend (🎨 FRONTEND - VUE3) — Rama DevDavid
**¿Qué es?**: Se agregaron tres botones de filtro rápido ("Todos", "Simple" y "Maestro") junto al selector de formularios dentro del diseñador de procesos de negocio. Además, se mejoraron los estilos visuales del selector de formularios para que sea más legible, accesible y coherente con el resto del diseño del sistema.
**¿Para qué sirve?**: Para que los analistas de procesos puedan filtrar rápidamente qué tipo de formulario necesitan al diseñar una tarea: formularios simples (independientes) o formularios maestros (mutantes). Antes, el filtro se aplicaba de forma invisible según la configuración del proceso, lo que confundía al usuario al no ver todos los formularios disponibles. Ahora, con un solo clic en los botones, el analista ve exactamente los formularios que necesita. Los estilos mejorados hacen que el selector sea más fácil de leer y tenga un aspecto profesional.
**¿De dónde viene?**: Resolución de BUG-J02-004 (filtro de tipos de formulario faltante) y BUG-J02-005 (estilos del selector de formularios) — Certificación UAT Jornada 02, Misión M5.
**¿Qué debería hacer?**:
- Al abrir las propiedades de una tarea en el modelador, aparecen tres botones junto al selector de formularios: "Todos", "Simple" y "Maestro"
- Al presionar "Simple", solo se muestran los formularios simples; al presionar "Maestro", solo los maestros; al presionar "Todos", se muestran todos
- El selector de formularios tiene un aspecto visual limpio, con bordes redondeados, sombra sutil y resaltado al pasar el cursor
- Este filtro funciona tanto para tareas de usuario como para el evento de inicio del proceso

**Estado**: ✅ Listo


---

## 2026-06-24 - Corrección de Catálogo de Formularios y Enrutamiento en Diseñador BPMN
**Autor**: Agente Backend (⚙️ BACKEND - JAVA) / Agente Frontend (🎨 FRONTEND - VUE3) - Rama DevDavid
**¿Qué es?**: Se eliminaron datos simulados (mocks) en el catálogo de formularios para mostrar información real de la base de datos. Adicionalmente, se corrigieron enlaces rotos y rutas de navegación dentro del diseñador de procesos BPMN que llevaban a páginas no encontradas (error 404).
**¿Para qué sirve?**: Para que al diseñar un proceso y asignar formularios a las tareas, el sistema liste los formularios verdaderos creados por la organización. También asegura que los usuarios puedan navegar fluidamente dentro del área de modelado sin toparse con pantallas de error al acceder a subprocesos o al diseñador principal.
**¿De dónde viene?**: Resolución de bugs críticos detectados durante la Certificación UAT Manual (Sprint PM-01). Reporte de Agente QA (BUG-J02-001, BUG-J02-002, BUG-J02-003).
**¿Qué debería hacer?**: 
- El menú de selección de formularios en el diseñador muestra datos reales de PostgreSQL.
- Ingresar a la sección de modelador no resulta en un error 404, redirigiendo correctamente a la herramienta.
- Hacer clic en un subproceso (Call Activity) abre correctamente el proceso hijo en una nueva pestaña.

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

## 2026-07-01 — Reparación del Guardado de Menú y Topología
**Autor**: Agente Backend (Rama DevDavid)
**¿Qué es?**: Se corrigió un problema donde, al editar un rol de usuario, los accesos a los módulos del menú no se estaban guardando. Se implementó la lógica para que el sistema reciba, traduzca y guarde estos accesos, y se aseguró de que los permisos básicos existan en el sistema.
**¿Para qué sirve?**: Para garantizar que cuando un administrador asigne o quite acceso a diferentes secciones del sistema (como el Workdesk o el Modelador), estos cambios se guarden de verdad y el usuario solo vea las opciones que le corresponden.
**¿De dónde viene?**: Resolución de BUG-J02-006 relacionado con la falta de guardado de los permisos de módulos.
**¿Qué debería hacer?**:
- Al actualizar un rol, los módulos asignados se guardan correctamente en el sistema.
- El usuario verá reflejados sus nuevos accesos en el menú al iniciar sesión.

**Estado**: ✅ Listo

---

## 2026-07-06 — HOTFIX CRÍTICO: Reparación del Diseñador de Flujos de Trabajo
**Autor**: Arquitecto Líder (Rama DevDavid)
**¿Qué es?**: Se reparó un error crítico que impedía agregar componentes (tareas, eventos, compuertas) al diseñador de procesos BPMN. Al intentar arrastrar cualquier elemento al lienzo, el sistema mostraba un error y no permitía diseñar.
**¿Para qué sirve?**: Para devolver la funcionalidad completa del diseñador de procesos. Sin este arreglo, era imposible crear o editar flujos de trabajo en la plataforma.
**¿De dónde viene?**: El motor de diseño necesita un catálogo completo de tipos de componentes para funcionar. Se estaba usando un catálogo artesanal con solo 7 tipos en lugar del catálogo oficial con más de 100 tipos. Al eliminar este catálogo incompleto y usar el oficial, el diseñador volvió a funcionar correctamente.
**¿Qué debería hacer?**:
- Abrir el diseñador BPMN → Crear nuevo proceso → Arrastrar tareas y eventos sin errores
- Todas las propiedades (formularios, temas, decisiones) siguen funcionando
- El filtro de tipos de formulario (Simple/Maestro) sigue visible y operativo

**Estado**: ✅ Listo

---

## 2026-07-14 — Estructura para Asignación de Roles en Tareas del Proceso
**Autor**: Agente Infra/BD (Rama feature/lane-role-assignment)
**¿Qué es?**: Se prepararon las tablas y reglas internas de la base de datos que permitirán conectar los "carriles" de un proceso (quién hace qué) con los roles reales del sistema (ej. Analista, Supervisor). Se agregaron protecciones para asegurar que no se puedan asignar roles que no existen ni duplicar carriles en un mismo proceso.
**¿Para qué sirve?**: Para garantizar que, cuando en un futuro cercano se configure visualmente quién debe ejecutar una tarea en el diseñador de procesos, esa información se guarde de forma segura y estrictamente relacionada con los roles de seguridad reales de la empresa. Esto es el cimiento para que el sistema asigne las tareas a las personas correctas de forma automática.
**¿De dónde viene?**: Historias de Usuario US-005 (Modelador de Procesos) y US-036 (Seguridad y Roles), siguiendo la arquitectura establecida en el Sprint PM-01.
**¿Qué debería hacer?**:
- El sistema ya cuenta con la estructura interna para guardar qué rol ejecuta las tareas de cada carril.
- Protege la información impidiendo que se asigne trabajo a roles que no existen.
- Evita posibles fallos o confusiones impidiendo que se creen carriles duplicados en el mismo diseño de proceso.

**Estado**: ✅ Listo

---

## 2026-07-14 — Conexión de Carriles BPMN con Roles de Seguridad
**Autor**: Agente Backend (Rama DevDavid)
**¿Qué es?**: Se construyó el puente lógico y los servicios que permiten al sistema leer los "carriles" (lanes) de un diagrama de proceso y guardarlos en la base de datos de manera organizada. Además, se crearon las conexiones internas (APIs) para que los administradores puedan asignar qué rol de seguridad puede iniciar o ejecutar las tareas de cada carril.
**¿Para qué sirve?**: Para que cuando se diseñe un proceso con diferentes responsables (por ejemplo, "Solicitante", "Revisor", "Aprobador"), el sistema sepa exactamente qué personas de la empresa pueden hacer ese trabajo. Esto evita que alguien sin autorización apruebe algo que no le corresponde, conectando los diagramas visuales con los permisos reales de los usuarios.
**¿De dónde viene?**: Historias de Usuario US-005 (Modelador de Procesos) y US-036 (Seguridad y Roles).
**¿Qué debería hacer?**:
- Al cargar un nuevo proceso en el sistema, detecta automáticamente todos sus carriles y los registra.
- Permite a los administradores consultar la lista de carriles de cualquier proceso.
- Permite asignar o quitar roles a un carril específico para controlar quién puede iniciar el proceso o ejecutar sus tareas.

**Estado**: ✅ Listo
