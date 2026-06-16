# 📖 Bitácora de Avances — IBPMS Platform

> **¿Qué es este documento?**
>
> Este es el **registro de todo lo que se ha construido** en la plataforma IBPMS, escrito en un lenguaje que cualquier persona pueda entender — sin palabras técnicas, sin código, sin jerga de programación.
>
> Piénsalo como el "diario de obra" de una construcción: cada vez que se termina algo importante, se anota aquí qué se hizo, para qué sirve, y de dónde vino la necesidad.
>
> **¿Para quién es?** Para Harold, los stakeholders, y cualquier persona que necesite saber qué se ha avanzado sin tener que leer código o documentos técnicos.
>
> **¿Cómo se lee?** Las entradas más recientes están al final. Cada entrada responde 4 preguntas simples: ¿Qué es? ¿Para qué sirve? ¿De dónde vino? ¿Qué debería hacer?

---

## 📏 Formato de Cada Entrada

Toda entrada en esta bitácora sigue esta estructura:

```
## [FECHA] — [TÍTULO DESCRIPTIVO]
**Autor**: [Nombre del usuario o agente que completó el trabajo]
**¿Qué es?**: [Descripción en lenguaje cotidiano — qué se construyó]
**¿Para qué sirve?**: [Beneficio práctico para el usuario final]
**¿De dónde viene?**: [Qué historia de usuario o necesidad originó esto]
**¿Qué debería hacer?**: [Comportamiento esperado visible para el usuario]
**Estado**: ✅ Listo | 🔨 En progreso | ⚠️ Con observaciones
```

---

## 🤖 Reglas para los Agentes de IA

> **DIRECTIVA OBLIGATORIA**: Cuando cualquier agente del enjambre (Backend, Frontend, QA, Arquitecto) complete una tarea, una historia de usuario, o un bugfix significativo, **DEBE** agregar una entrada en este documento siguiendo el formato establecido.

### Reglas de Redacción

1. **Cero jerga técnica.** No mencionar nombres de clases, endpoints, bases de datos, frameworks, ni acrónimos de programación. Si no lo entendería un director ejecutivo que nunca ha programado, reescríbelo.
2. **Lenguaje activo y concreto.** Decir "ahora el usuario puede..." en lugar de "se implementó la funcionalidad de...".
3. **Cada entrada es auto-contenida.** No debe requerir leer otras entradas para entenderse.
4. **Sin auto-promoción.** No decir "se completó exitosamente con arquitectura robusta y escalable". Decir simplemente qué hace y para quién.

### Palabras Prohibidas

No usar jamás: API, endpoint, microservicio, refactoring, merge, commit, deploy, backend, frontend, middleware, pipeline, payload, token (excepto en contexto de seguridad para usuarios), schema, migration, cache hit, throughput, serialization, hexagonal, CQRS, DTO, VO, entity, aggregate.

### Reemplazos Sugeridos

| ❌ No decir | ✅ Decir en su lugar |
|---|---|
| "Se desplegó el endpoint de autenticación" | "Ahora el sistema puede verificar quién es cada usuario cuando inicia sesión" |
| "Se implementó el servicio de mensajería con RabbitMQ" | "El sistema ahora puede enviar y recibir mensajes internos entre usuarios de forma automática" |
| "Se refactorizó la capa de persistencia" | "Se mejoró la forma en que el sistema almacena y recupera la información para que sea más rápido y confiable" |
| "Se corrigió un bug en el middleware de autenticación" | "Se solucionó un problema que impedía a algunos usuarios iniciar sesión correctamente" |

---

## 📝 Directiva de Bitácora

Cada entrada **DEBE** incluir:

1. **Fecha y hora local** de cuando se completó el trabajo (formato: `YYYY-MM-DD HH:MM [zona horaria]`)
2. **Nombre del usuario** que solicitó o aprobó el trabajo (el Cartero proporcionará este dato)
3. **Resumen ejecutivo** que responda las 4 preguntas clave del formato
4. **Estado** usando exclusivamente los 3 indicadores: ✅ Listo | 🔨 En progreso | ⚠️ Con observaciones

Si el estado es **⚠️ Con observaciones**, agregar un campo adicional:
```
**Observaciones**: [Qué falta, qué se debe revisar, o qué limitación tiene]
```

---

## 📋 Registro de Avances

---

## 2026-06-05 — Reparación del Guardado de Procesos BPMN
**Autor**: Agente Backend (Sprints PM-01)
**¿Qué es?**: Se solucionó un problema crítico que impedía guardar los diagramas de procesos de negocio. Se mejoró la forma en que el sistema almacena y recupera la información de los diseños en la base de datos para que sea más confiable.
**¿Para qué sirve?**: Para garantizar que cuando un analista diseña o modifica un proceso en el modelador visual, el sistema lo guarde correctamente sin generar errores. Esto es fundamental porque sin procesos guardados correctamente, el sistema no puede asignar tareas a los usuarios.
**¿De dónde viene?**: Corrección de un problema técnico detectado (OBS-1, originado en US-005) para asegurar que el sistema y la base de datos se comuniquen perfectamente.
**¿Qué debería hacer?**:
- El sistema guarda los diseños de procesos sin interrupciones.
- Mantiene un registro del historial sin errores cuando ocurren cambios en los procesos.
- Todo el módulo de modelado funciona correctamente con el almacenamiento central.

**Estado**: ✅ Listo

---

## 2026-06-05 — Ejecución de Formularios Operativos
**Autor**: Agentes Especialistas de IA (Backend y Frontend)
**¿Qué es?**: Se construyó la pantalla final donde los usuarios llenan la información de sus tareas. Ahora incluye protecciones para evitar que la misma tarea se abra dos veces por error, y avisa si se intenta enviar un formulario incompleto o en blanco.
**¿Para qué sirve?**: Para garantizar que el trabajo diario fluya sin interrupciones ni pérdida de datos. Si un usuario tiene un error al llenar la información, el sistema lo lleva directo al problema; si alguien más toma la tarea, la pantalla se bloquea para no hacer doble trabajo.
**¿De dónde viene?**: Historia de Usuario US-029 (Ejecución de Formularios - Cadena 3).
**¿Qué debería hacer?**:
- Al hacer clic en una tarea, se abre su formulario completo.
- El sistema advierte y bloquea si se intenta trabajar la misma tarea en dos pestañas del navegador a la vez.
- Si falta información, resalta el error y mueve la pantalla hacia él automáticamente.
- Pide confirmación de seguridad si se intenta enviar el trabajo sin haber llenado datos obligatorios.
- Los campos de solo lectura se identifican visualmente con un candado para evitar confusiones.

**Estado**: ✅ Listo

---

## 2026-06-04 — Sistema de Reclamo y Liberación de Tareas
**Autor**: Agentes Especialistas de IA (Backend y Frontend)
**¿Qué es?**: Se implementó el sistema de "apropiación" de tareas. Ahora, cuando un usuario va a trabajar en una tarea, la "reclama" para que los demás sepan que él se está encargando de ella. Si no puede terminarla, puede "liberarla" para que otro compañero la tome.
**¿Para qué sirve?**: Para evitar colisiones en el trabajo de los equipos. Previene que dos empleados intenten resolver la misma actividad al mismo tiempo, organizando la bandeja de pendientes de manera clara y transparente.
**¿De dónde viene?**: Historia de Usuario US-002 (Reclamo de Tareas - Cadena 2).
**¿Qué debería hacer?**:
- En la bandeja de tareas compartidas, cada una muestra un botón para tomar propiedad.
- Al reclamar una tarea, aparece bloqueada (mostrando el nombre del responsable) para el resto del equipo.
- El usuario dueño puede devolver o "soltar" la tarea si no puede continuar.
- Si una tarea se deja abandonada mucho tiempo, el sistema la suelta automáticamente para que otro la tome (anti-fantasmas).
- Muestra una línea de tiempo con el historial exacto (quién la tomó, cuándo y por qué la soltó).

**Estado**: ✅ Listo

---

## 2026-03-15 — Inicio de Sesión y Control de Acceso

**Autor**: Equipo de Desarrollo IBPMS (Sprints S0–S5)
**¿Qué es?**: Se construyó todo el sistema de entrada al sistema. Los usuarios ahora pueden iniciar sesión con su correo y contraseña, y el sistema sabe qué permisos tiene cada persona según su rol (administrador, operador, supervisor, etc.).

**¿Para qué sirve?**: Para que solo las personas autorizadas puedan entrar al sistema, y cada una vea únicamente las opciones y pantallas que le corresponden. Un operador no ve lo mismo que un administrador, y nadie puede entrar sin identificarse primero.

**¿De dónde viene?**: Necesidades identificadas en las historias US-036 (inicio de sesión), US-048 (gestión de sesiones), US-038 (roles y permisos) y US-051 (autoregistro de usuarios en portal).

**¿Qué debería hacer?**:
- Al abrir el sistema, aparece una pantalla de inicio de sesión pidiendo correo y contraseña
- Si las credenciales son correctas, el usuario entra al sistema y ve su escritorio personalizado
- Si son incorrectas, aparece un mensaje claro de error
- El menú lateral muestra solo las opciones que corresponden al rol del usuario
- La sesión se cierra automáticamente después de un periodo de inactividad
- Los usuarios del portal pueden crear su propia cuenta a través de un formulario de autoregistro

**Estado**: ✅ Listo

---

## 2026-04-10 — Bandeja de Entrada de Tareas

**Autor**: Equipo de Desarrollo IBPMS (Sprints S1–S7)
**¿Qué es?**: Se construyó la pantalla principal donde cada usuario ve las tareas que tiene asignadas. Es como una bandeja de entrada de correo, pero para tareas de trabajo: cada tarea muestra su título, quién la envió, cuándo debe completarse, y en qué estado está.

**¿Para qué sirve?**: Para que cada persona que entra al sistema sepa inmediatamente qué tiene pendiente, qué es urgente, y pueda abrir cada tarea para trabajar en ella. Es el punto de partida del día laboral dentro del sistema.

**¿De dónde viene?**: Historia de usuario US-001 (bandeja de tareas del usuario), que es la pieza central de toda la experiencia de uso del sistema.

**¿Qué debería hacer?**:
- Al iniciar sesión, el usuario ve su escritorio con la lista de tareas asignadas
- Cada tarea muestra: nombre del proceso, actividad, fecha de asignación y prioridad
- Se puede ordenar y filtrar la lista por diferentes criterios (fecha, prioridad, tipo)
- Al hacer clic en una tarea, se abre el formulario correspondiente para completarla
- Las tareas completadas desaparecen de la bandeja y pasan al historial

**Estado**: ✅ Listo

---

## 2026-04-25 — Diseñador de Formularios

**Autor**: Equipo de Desarrollo IBPMS (Sprints S3–S7)
**¿Qué es?**: Se construyó una herramienta visual para crear los formularios que los usuarios llenan cuando ejecutan una tarea. El diseñador funciona como un editor de arrastrar y soltar: se eligen los campos que se necesitan (texto, números, fechas, listas desplegables, casillas de verificación) y se acomodan en el formulario.

**¿Para qué sirve?**: Para que los administradores del sistema puedan crear y modificar formularios sin necesidad de pedir ayuda a programadores. Si un proceso necesita un nuevo campo o una nueva sección, el administrador lo agrega directamente desde esta herramienta.

**¿De dónde viene?**: Historia de usuario US-003 (diseñador de formularios dinámicos), una de las funcionalidades más extensas del sistema con más de 60 criterios de funcionamiento definidos.

**¿Qué debería hacer?**:
- Se accede desde el menú de administración
- Muestra un lienzo donde se arrastran componentes de formulario (campos de texto, selectores de fecha, casillas, etc.)
- Cada campo se puede configurar: hacerlo obligatorio, agregar texto de ayuda, definir validaciones
- Se puede previsualizar el formulario antes de publicarlo
- Los formularios creados aquí se conectan automáticamente con las tareas del sistema

**Estado**: 🔨 En progreso

**Observaciones**: La mayoría de los tipos de campo están funcionando. Quedan por completar algunos campos avanzados (tablas editables, campos calculados) y la conexión con el sistema de reglas de negocio.

---

## 2026-05-10 — Modelador de Procesos

**Autor**: Equipo de Desarrollo IBPMS (Sprints S4–S7)
**¿Qué es?**: Se construyó un editor visual donde los analistas de procesos pueden dibujar el flujo de trabajo de un proceso de negocio. Funciona como un diagrama de flujo interactivo: se colocan actividades (rectángulos), decisiones (rombos), y flechas que indican el orden en que se ejecutan las tareas.

**¿Para qué sirve?**: Para que las organizaciones puedan definir sus procesos de negocio de forma visual y después el sistema los ejecute automáticamente. Por ejemplo: un proceso de "Solicitud de Vacaciones" donde primero el empleado llena un formulario, luego el jefe aprueba o rechaza, y finalmente RRHH registra la decisión.

**¿De dónde viene?**: Historia de usuario US-005 (modelador de procesos BPMN), que es el corazón del sistema — sin procesos definidos, no hay tareas que ejecutar.

**¿Qué debería hacer?**:
- Se accede desde el menú de administración
- Muestra un lienzo grande donde se arrastran los elementos del proceso (inicio, tareas, decisiones, fin)
- Las tareas se conectan con flechas para definir el orden de ejecución
- Cada tarea se puede configurar: quién la ejecuta, qué formulario usa, cuánto tiempo tiene para completarla
- El proceso se puede guardar como borrador, publicar para uso, o archivar
- Incluye validación automática que detecta errores en el diseño (por ejemplo, un camino sin salida)

**Estado**: 🔨 En progreso

**Observaciones**: El editor visual funciona con los elementos básicos. Están en desarrollo los elementos avanzados (subprocesos, eventos intermedios, compuertas paralelas) y la ejecución automática de los procesos diseñados.

---

## 2026-05-20 — Mensajería Interna del Sistema

**Autor**: Equipo de Desarrollo IBPMS (Sprints S5–S7)
**¿Qué es?**: Se construyó el sistema de mensajería interna que permite al sistema enviar y recibir mensajes entre sus distintas partes de forma automática y confiable. Piénsalo como el sistema postal interno de una empresa: cuando algo importante ocurre (una tarea se completa, un formulario se envía, un proceso cambia de etapa), el sistema genera un mensaje interno que llega al destinatario correcto sin perderse.

**¿Para qué sirve?**: Para que las acciones de un usuario se reflejen inmediatamente en el sistema sin retrasos ni pérdida de información. Por ejemplo: cuando un jefe aprueba una solicitud, el empleado recibe la notificación al instante. Cuando se asigna una tarea nueva, aparece en la bandeja del responsable sin que nadie tenga que enviarla manualmente.

**¿De dónde viene?**: Historia de usuario US-034 (sistema de eventos y mensajería interna), que es la columna vertebral invisible que conecta todas las partes del sistema entre sí.

**¿Qué debería hacer?**:
- Los mensajes internos se envían automáticamente cuando ocurren eventos importantes
- Ningún mensaje se pierde, incluso si el sistema se reinicia (los mensajes pendientes se entregan después)
- Los mensajes se entregan en orden y sin duplicados
- El sistema puede manejar miles de mensajes por minuto sin saturarse
- Los administradores pueden ver un panel de monitoreo de mensajes pendientes y entregados

**Estado**: ✅ Listo

---

## 2026-06-02 — Gobernanza PM-IA Establecida

**Autor**: Harold (Product Owner) + PM-IA (Product Manager de Inteligencia Artificial)
**¿Qué es?**: Se estableció un nuevo sistema de organización y gestión del proyecto. Hasta ahora, el equipo de desarrollo (formado por agentes de inteligencia artificial especializados) trabajaba sin un plan de producto centralizado. A partir de hoy, existe un Product Manager de IA que coordina qué se construye, en qué orden, y cómo se verifica que realmente funcione.

**¿Para qué sirve?**: Para resolver 6 problemas que estaban frenando el proyecto:
1. **No había un orden lógico de desarrollo** → Ahora existe un mapa de ruta con 10 "cadenas de capacidades" que definen la secuencia correcta
2. **Los agentes inventaban cosas que no existían** → Ahora hay un catálogo centralizado de todas las conexiones entre partes del sistema
3. **Se reportaban cosas como "terminadas" que no funcionaban realmente** → Ahora hay un proceso de verificación obligatorio antes de dar algo por completado
4. **El registro de avance estaba desactualizado** → Ahora es obligatorio actualizarlo después de cada tarea completada
5. **No había un registro entendible para no-técnicos** → Este mismo documento que estás leyendo es la solución
6. **Los agentes "olvidaban" las reglas entre sesiones** → Ahora todas las directivas están documentadas en archivos permanentes del proyecto

**¿De dónde viene?**: Decisión estratégica del Product Owner (Harold) al identificar que con 56 funcionalidades por construir y solo el 21% completado, el proyecto necesitaba una dirección de producto profesional.

**¿Qué debería hacer?**:
- A partir de ahora, cada ciclo de trabajo (sprint) tiene un plan escrito con objetivos claros
- Cada funcionalidad completada se registra en este documento en lenguaje comprensible
- El estado real del proyecto se puede consultar en cualquier momento
- Las decisiones importantes quedan registradas permanentemente para evitar repetir errores

**Estado**: ✅ Listo

---

*Última actualización: 2026-06-07 00:55 COT*
*Próxima entrada esperada: Al completarse la siguiente historia del Sprint PM-01*

---

## [2026-06-07] — La plataforma ahora puede arrancar y completar procesos de negocio desde la pantalla del usuario

**Autor**: Agente Backend (⚙️ BACKEND - JAVA)

**¿Qué es?**: Se construyó la capacidad para que la interfaz de usuario pueda **iniciar un trámite o proceso de negocio** (por ejemplo, "Abrir un caso de crédito") y **completar las tareas asignadas** (por ejemplo, "Revisar documentos del solicitante") directamente desde la aplicación web, conectándose al motor interno de procesos de la plataforma.

**¿Para qué sirve?**: Antes de esta mejora, los procesos solo podían arrancarse de forma anónima (sin saber quién lo inició) o a través de canales internos especiales. Ahora, cualquier usuario autenticado puede iniciar un proceso desde la interfaz y el sistema sabe exactamente quién lo inició, cuándo, y qué datos aportó. También pueden completar sus tareas pendientes de forma segura, con protección contra doble envío accidental.

**¿De dónde viene?**: Historia de Usuario US-007 (Ejecución BPMN) — Handoff del Arquitecto Líder, Sprint PM-01, Slot 3. Alineado con las decisiones arquitectónicas ADR-001 (separación de responsabilidades) y ADR-003 (motor de procesos embebido).

**¿Qué debería hacer?**:
- Al presionar "Iniciar Proceso" en la interfaz, el sistema crea una nueva instancia del trámite y devuelve una confirmación con el identificador único
- Si el usuario intenta iniciar un trámite que no existe en el catálogo, recibirá un mensaje claro: "No se encontró la definición de proceso"
- Al completar una tarea asignada, el proceso avanza automáticamente al siguiente paso definido en el flujo de trabajo
- Todo queda registrado: quién inició el proceso, cuándo, y con qué datos

**Estado**: ✅ Listo

---

## [2026-06-07] — La pantalla de trabajo ahora tiene un botón para iniciar nuevos trámites y ejecutar tareas del motor de procesos

**Autor**: Agente Frontend (🎨 FRONTEND - VUE)

**¿Qué es?**: Se conectó la pantalla principal de trabajo (Bandeja Unificada) con la capacidad de iniciar nuevos trámites y completar las tareas generadas por el motor de procesos. Ahora aparece un botón verde "Iniciar Caso" en la barra superior que abre un panel lateral con la lista de procesos disponibles para ejecutar.

**¿Para qué sirve?**: Para que los usuarios puedan iniciar un nuevo trámite (por ejemplo, "Solicitud de Crédito" o "Alta de Proveedor") directamente desde su pantalla de trabajo, sin necesidad de ir a otra sección del sistema. Al iniciar un caso, las tareas generadas aparecen automáticamente en la bandeja del equipo. Además, cuando un usuario completa una tarea de un proceso, el sistema usa la ruta directa al motor de procesos para asegurar que la operación sea confiable.

**¿De dónde viene?**: Historia de Usuario US-007 (Ejecución de Procesos) — Integración visual aprobada por el Arquitecto Líder, Sprint PM-01.

**¿Qué debería hacer?**:
- En la parte superior de la bandeja de trabajo aparece un botón verde "Iniciar Caso"
- Al presionarlo, se abre un panel lateral con la lista de todos los procesos de negocio disponibles
- Cada proceso muestra su nombre, versión y un botón "Iniciar Caso"
- Antes de iniciar, el sistema pide confirmación para evitar ejecuciones accidentales
- Si el inicio es exitoso, muestra un aviso verde con el identificador del nuevo caso
- Si ocurre un error (por ejemplo, el proceso no existe), muestra un mensaje claro y entendible
- Después de iniciar un caso exitosamente, la bandeja se refresca automáticamente para mostrar las nuevas tareas
- Al completar una tarea de proceso, el sistema determina automáticamente la mejor ruta para registrar la finalización

**Estado**: ✅ Listo

---

## [2026-06-07] — Sistema de Monitoreo y Telemetría de Procesos

**Autor**: Agente Backend (⚙️ BACKEND - JAVA)

**¿Qué es?**: Se construyeron las bases del sistema de monitoreo (telemetría) que permite listar qué procesos de negocio están activos, cuáles ya terminaron, y si alguno sufrió un error inesperado durante su ejecución.

**¿Para qué sirve?**: Para que los administradores puedan vigilar la "salud" del sistema. Si un trámite se queda atascado por un error técnico, el sistema ahora puede identificarlo (como un "incidente") para que el equipo de soporte lo rescate sin que el usuario final pierda su información.

**¿De dónde viene?**: Historia de Usuario US-030 (Monitoreo BPMN) — Handoff del Arquitecto Líder, Sprint PM-01.

**¿Qué debería hacer?**:
- El sistema puede listar todas las instancias de procesos que están en curso o suspendidas.
- El sistema puede listar los procesos que ya terminaron su ciclo de vida.
- El sistema detecta y lista los errores internos (incidentes), indicando en qué trámite falló.

**Estado**: ✅ Listo

---

## [2026-06-07] — Pantalla de Monitoreo de Procesos e Incidentes (BAM)

**Autor**: Agente Frontend (🎨 FRONTEND - VUE)

**¿Qué es?**: Se construyó el tablero de control visual para monitorear en tiempo real todos los trámites que administra el sistema. Esta pantalla lista los procesos activos, completados y suspendidos, y cuenta con un panel destacado para alertar sobre incidentes técnicos o errores que requieran atención inmediata.

**¿Para qué sirve?**: Para que los administradores tengan una visión panorámica (Business Activity Monitoring) del estado del sistema. Si un trámite se detiene por un fallo en el servidor o un error de conexión, el panel de incidentes lo muestra de inmediato. Así, el equipo de soporte técnico puede enterarse y resolver el problema antes de que el usuario final se dé cuenta, garantizando que el flujo de trabajo nunca se interrumpa.

**¿De dónde viene?**: Historia de Usuario US-030 (Monitoreo BPMN) — Handoff del Arquitecto Líder, Sprint PM-01.

**¿Qué debería hacer?**:
- El tablero principal muestra una lista de todos los procesos iniciados, con su estado actual y fecha de creación.
- Permite filtrar rápidamente para ver solo los procesos activos, completados o suspendidos.
- Si ocurre algún error técnico en el motor de procesos, aparece inmediatamente en el "Panel de Incidentes Activos".
- El diseño es moderno, rápido y muestra información 100% real y actualizada.

**Estado**: ✅ Listo

## [2026-06-07] — Eliminación de Redundancia de Estados en Tareas Kanban

**Autor**: Agente Backend (⚙️ BACKEND - JAVA)

**¿Qué es?**: Se limpió el código responsable de mantener el tablero visual de tareas (Kanban). Específicamente, se eliminó la costumbre del sistema de "anotar en dos libretas" el estado de una tarea. Ahora, para saber si una tarea está en curso o terminada, el Kanban le pregunta directamente a la "fuente original" de los datos (la bandeja de trabajo principal) en lugar de intentar recordar su propia versión.

**¿Para qué sirve?**: Para garantizar que no existan contradicciones en el sistema. Antes, si una tarea cambiaba de estado en la base central pero el Kanban no se enteraba, el usuario veía información incorrecta (por ejemplo, una tarea en "Pendiente" que ya estaba "En progreso"). Al eliminar esta redundancia, el tablero siempre muestra la pura verdad, evitando confusiones y errores al intentar tomar una tarea que ya está asignada a otro.

**¿De dónde viene?**: Historia de Usuario US-008 (Refactorización Kanban) — Handoff del Arquitecto Líder, Sprint PM-01. Cumpliendo estrictamente con la directiva "Zero-Mock" (ADR-010).

**¿Qué debería hacer?**:
- El tablero Kanban consulta la información de estado de manera precisa y en tiempo real.
- Ya no ocurren escenarios donde una tarea parece estar libre en el tablero, pero al hacer clic dice que ya fue reclamada.
- Se reduce la posibilidad de errores por "información desactualizada".

**Estado**: ✅ Listo

---

*Última actualización: 2026-06-06 23:59 COT*
*Próxima entrada esperada: Al completarse la siguiente historia del Sprint PM-01*

---

## [2026-06-09] — El Tablero Kanban ahora se conecta con los datos reales del sistema

**Autor**: Agente Frontend (🎨 FRONTEND - VUE)

**¿Qué es?**: Se conectó el tablero visual de tareas (Kanban) directamente con la fuente real de datos del sistema. Antes, el tablero consultaba las tareas por separado y las columnas por otro lado; ahora, toda la información viene junta y actualizada desde una sola fuente confiable. También se mejoró la protección contra conflictos: si dos personas intentan mover la misma tarea al mismo tiempo, el sistema lo detecta, revierte automáticamente el movimiento del segundo usuario y le muestra un aviso claro en pantalla.

**¿Para qué sirve?**: Para garantizar que lo que el usuario ve en el tablero de tareas sea siempre la verdad del sistema. Si un compañero mueve una tarea en otro computador, el tablero del primer usuario se actualiza en tiempo real. Si alguien intenta mover una tarea que ya tomó otra persona, el sistema le avisa instantáneamente en vez de mostrar un error confuso.

**¿De dónde viene?**: Historia de Usuario US-008 (Vista Kanban) — Sprint PM-01, Slot 4. Cumpliendo las directivas de conexión real de datos (ADR-010) y actualización instantánea entre usuarios (CA-12).

**¿Qué debería hacer?**:
- Al abrir el tablero Kanban, las tareas se cargan directamente desde el sistema central.
- Si otro usuario mueve una tarea desde su computador, el tablero se actualiza automáticamente sin necesidad de recargar la página.
- Si un usuario mueve una tarea que ya fue tomada por otra persona, el tablero revierte el movimiento y muestra un aviso rojo: "Conflicto: esta tarea fue reclamada por otro usuario."
- Al hacer clic en una tarjeta, se abre la vista detallada real de la tarea (no una copia local).

**Estado**: ✅ Listo

---

*Última actualización: 2026-06-09 17:38 COT*
*Próxima entrada esperada: Al completarse la siguiente historia del Sprint PM-01*

---

## [2026-06-09] — Fortalecimiento de la integridad del sistema al registrar formularios enviados

**Autor**: Agente Backend (⚙️ BACKEND - JAVA)

**¿Qué es?**: Se corrigió un problema estructural en la forma en que el sistema almacena los formularios que los usuarios envían al completar una tarea. La organización interna del sistema no estaba respetando sus propias reglas de separación de responsabilidades, lo que podía causar errores difíciles de rastrear a medida que el sistema crece. Además, se eliminó una tabla duplicada que se creaba automáticamente en la base de datos cada vez que el sistema se instalaba desde cero, generando confusión y desperdicio de espacio.

**¿Para qué sirve?**: Para garantizar que cada formulario enviado por un usuario se almacene de forma confiable, que el historial de envíos nunca se pierda ni se corrompa, y que el sistema pueda crecer sin acumular datos basura. También se resolvieron inconsistencias en el registro de avances del proyecto que podrían confundir a quienes consultan el estado de avance del sistema.

**¿De dónde viene?**: Historia de Usuario US-017 (Registro de Formularios y Garantía de Integridad de Datos) — Sprint PM-01, Slot 5 (Estabilización). Solicitado por el Arquitecto Líder para cerrar deuda técnica acumulada.

**¿Qué debería hacer?**:
- Cuando un usuario envía un formulario, el sistema lo registra de forma inmutable (no se puede alterar después)
- Si ocurre un error al procesar el formulario, el sistema crea un registro de compensación (no borra el original)
- La base de datos ya no crea tablas redundantes al instalarse por primera vez
- El registro de avance del proyecto ya no tiene información contradictoria

**Estado**: ✅ Listo

---

## [2026-06-09] — Simplificación de notificaciones de conexión y guardado

**Autor**: Agente Frontend (🎨 FRONTEND - VUE)

**¿Qué es?**: Se eliminó una notificación duplicada que podía confundir al usuario mostrando dos mensajes de estado al mismo tiempo. Se consolidó toda la información sobre el estado de la conexión a internet y el progreso de guardado en un solo indicador claro en la esquina de la pantalla.

**¿Para qué sirve?**: Para evitar confusiones y asegurar que el usuario sepa exactamente si el sistema está guardando sus datos, si se perdió la conexión, o si todo funciona correctamente, usando un lenguaje sencillo y sin tecnicismos.

**¿De dónde viene?**: Historia de Usuario US-017 (Estabilización Frontend) — Sprint PM-01, Slot 5. Cumpliendo las reglas de notificaciones claras (CA-19 a CA-26).

**¿Qué debería hacer?**:
- Muestra un solo indicador cuando hay problemas de conexión o el sistema está guardando datos de forma prolongada.
- Los mensajes son fáciles de entender (por ejemplo: "Guardando cambios...", "Trabajando sin conexión").
- Los cambios rápidos (menores a 5 segundos) se guardan de forma invisible para no interrumpir el trabajo del usuario.

**Estado**: ✅ Listo

---

## [2026-06-16] — Recuperación del Historial de Cambios en Procesos
**Autor**: Agente Backend (⚙️ BACKEND - JAVA)
**¿Qué es?**: Se solucionó un problema técnico que impedía al sistema arrancar correctamente. El sistema había "olvidado" cómo mostrar el historial de cambios de un proceso, lo que bloqueaba todo el inicio. Se le enseñó nuevamente cómo extraer y traducir esa información de la base de datos para que el sistema inicie sin problemas.
**¿Para qué sirve?**: Para garantizar que el sistema vuelva a funcionar y que los administradores puedan consultar la "caja negra" o el registro de actividad de cada proceso (quién lo modificó, cuándo y qué cambió). Esto es vital para auditorías y para entender qué ha pasado con un trámite a lo largo del tiempo.
**¿De dónde viene?**: Resolución de Bug Quirúrgico (US-005) - Error de arranque del servidor.
**¿Qué debería hacer?**:
- El sistema arranca sin errores críticos de inicio.
- El historial de cambios de cualquier trámite puede ser consultado correctamente.

**Estado**: ✅ Listo

