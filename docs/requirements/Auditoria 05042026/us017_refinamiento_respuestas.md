# Respuestas al Refinamiento: US-017 — CQRS & Event Sourcing
## Análisis de valor + CAs generados

**Fecha:** 2026-04-05  
**Historia:** US-017 (8 CAs vigentes: CA-01 a CA-08)  
**Resultado:** 10 preguntas generan nuevos CAs (CA-09 a CA-18)

---

## Guía de lectura

Para cada pregunta uso esta clasificación:
- ✅ **GENERA CA** — La pregunta descubrió algo importante que NO está cubierto.
- 🟡 **YA CUBIERTO** — Ya está resuelto por un CA existente o por otra US.
- ⚪ **DIFERIDO** — Es válido pero no es prioritario para la primera versión.
- ❌ **NO GENERA VALOR** — Es un detalle técnico de implementación, no un requisito de negocio.

---

## 1. ADECUACIÓN FUNCIONAL (20 Preguntas)

---

### Pregunta #1 — ¿Qué pasa si el proceso que transforma los datos para los reportes falla a medio camino?
**Clasificación:** 🟡 YA CUBIERTO  
**Respuesta simple:** Si el proceso de transformación falla, los datos del formulario NO se pierden — quedaron grabados en el "acta notarial" (Event Store). La transformación simplemente se reintenta cuando se recupere. Es como si la fotocopiadora se traba: el documento original está a salvo, solo hay que volver a fotocopiarlo.  
**¿Por qué no genera CA?** El CA-01 ya define que la grabación del evento es lo primero (lo importante), y la proyección es secundaria. Si la proyección falla, los reportes se atrasan temporalmente pero no se pierde información. El detalle del mecanismo de reintento es una decisión del equipo de desarrollo, no un requisito de negocio.

---

### Pregunta #2 — ¿Cada vez que el sistema guarda un borrador automático, se graba un evento en el acta notarial?
**Clasificación:** ✅ **GENERA CA-09**  
**Respuesta simple:** NO debería. El Event Store (la "bóveda de actas notariales") es para eventos TRASCENDENTES: cuando el operario presiona [Enviar], cuando se reclama una tarea automáticamente, cuando se rechaza un formulario. Si grabáramos un acta notarial cada vez que el sistema guarda un borrador automático (cada 10 segundos de inactividad), sería como ir al notario cada vez que añades una coma a un documento. La bóveda se saturaría con ruido.

**Decisión:** El tipo `FORM_DRAFT_SAVED` se ELIMINA de la lista de eventos del Event Store. Los borradores viven en su propia tabla (`task_drafts`, CA-07) que NO es inmutable y se borra tras el envío exitoso.

---

### Pregunta #3 — ¿Faltan eventos en la lista?
**Clasificación:** ⚪ DIFERIDO  
**Respuesta simple:** Los 3 eventos restantes (`FORM_SUBMITTED`, `TASK_AUTO_CLAIMED`, `FORM_REJECTED`) cubren los momentos importantes de la primera versión. Agregar eventos extra como "se adjuntó un archivo" o "la validación falló" añade detalle de auditoría que puede ser valioso en el FUTURO, pero para la V1 complica la implementación sin resolver un problema de negocio concreto. Se puede agregar en V2 sin romper nada porque el Event Store es de "solo agregar" (append-only).

---

### Pregunta #4 — ¿Qué pasa si alguien pide que borren sus datos personales (derecho al olvido)?
**Clasificación:** ⚪ DIFERIDO  
**Respuesta simple:** Esta es una pregunta legal muy importante, pero aplica a TODA la plataforma, no solo a la US-017. Necesitaría su propia Historia de Usuario (Ej: "US-XXX: Gobernanza de Datos Personales y Derecho al Olvido") porque afecta base de datos, archivos, logs, y hasta los respaldos. Para la V1, la plataforma opera en un contexto judicial/empresarial donde la retención de datos es legalmente obligatoria (contrario al GDPR), por lo que el "no borrar jamás" del Event Store es una ventaja, no un problema.

---

### Pregunta #5 — ¿Se guarda el formulario completo o solo lo que cambió?
**Clasificación:** 🟡 YA CUBIERTO  
**Respuesta simple:** Se guarda el formulario COMPLETO. Imagina un acta notarial: no dice "cambié la coma de la línea 3". Dice el documento entero, tal como quedó al momento de firmarlo. Esto simplifica mucho las consultas y los reportes (no hay que "reconstruir" el formulario juntando pedazos). El CA-06 ya define `payload_json` como "contenido íntegro del formulario enviado".

---

### Pregunta #6 — ¿Quién decide qué información mínima se le envía al motor de procesos?
**Clasificación:** 🟡 YA CUBIERTO  
**Respuesta simple:** Lo define el Arquitecto de Procesos cuando diseña el flujo (BPMN) en la Pantalla 6 (US-005). Es como un formulario de la DIAN: el diseñador del formulario decide qué campos van al resumen ejecutivo y cuáles son solo para el expediente. El CA-02 ya cubre esto y la US-005 (diseño de BPMN) es la responsable de definir las variables de enrutamiento.

---

### Pregunta #7 — ¿Qué pasa si al motor le falta una variable necesaria para tomar una decisión?
**Clasificación:** ❌ NO GENERA VALOR  
**Respuesta simple:** Si le falta una variable, Camunda levanta una alerta ("Incident") y el flujo se detiene hasta que un administrador lo corrija. Esto es el comportamiento estándar de Camunda 7 y no requiere un CA propio. Es como si a un juez le falta una prueba: detiene el caso hasta que la traigan. La prevención de este error es responsabilidad de la US-005 (validación del BPMN al desplegarlo), no de la US-017.

---

### Pregunta #8 — Cuando el sistema deshace un envío porque el motor falló, ¿borra el acta o genera un acta de anulación?
**Clasificación:** ✅ **GENERA CA-10**  
**Respuesta simple:** Genera un **acta de anulación** (evento compensatorio), NUNCA borra el acta original. Es la diferencia entre tachar un cheque y destruirlo. Si lo destruyes, no queda evidencia de que existió. Si lo tachas, cualquier auditor puede ver: "se intentó cobrar este cheque, pero fue anulado a las 3:15pm porque el banco estaba caído".

**Decisión:** El Rollback genera un evento `FORM_SUBMIT_ROLLED_BACK` que referencia el `event_id` del `FORM_SUBMITTED` original. El proceso de transformación a reportes ignora los eventos que tienen un evento de anulación posterior.

---

### Pregunta #9 — ¿Cuánto tiempo espera el sistema antes de darse por vencido con el motor?
**Clasificación:** ✅ **GENERA CA-10** (incluido en el mismo CA)  
**Respuesta simple:** El sistema esperará máximo **10 segundos** a que el motor responda. Si pasan 10 segundos sin respuesta, se asume que el motor está caído y se ejecuta la anulación. 10 segundos es un equilibrio entre darle una oportunidad al motor de recuperarse y no dejar al operario esperando demasiado.

---

### Pregunta #10 — ¿El sistema reintenta antes de darse por vencido?
**Clasificación:** ✅ **GENERA CA-10** (incluido en el mismo CA)  
**Respuesta simple:** SÍ, reintentará **3 veces** con esperas crecientes (1 segundo, luego 2, luego 4) antes de ejecutar la anulación. Es como cuando llamas a alguien y no contesta: no asumes que murió al primer tono. Llamas 3 veces con pausas. Si a la tercera no contesta, tomas acción.

---

### Pregunta #11 — ¿El operario puede abrir una tarea de grupo sin haberla reclamado?
**Clasificación:** 🟡 YA CUBIERTO  
**Respuesta simple:** En el flujo normal NO. El operario reclama la tarea (US-002) y LUEGO la abre. El Auto-Claim del CA-04 es un **seguro de respaldo** para un caso raro: el operario reclamó la tarea, la abrió, pero entre ese momento y el envío, un supervisor ejecutó un "desclamar forzado" (Forced Unclaim de US-029 CA-23). Cuando el operario presiona [Enviar], el sistema detecta que ya no tiene la tarea asignada y automáticamente la vuelve a reclamar en su nombre. El CA-04 punto 5 ya lo explica.

---

### Pregunta #12 — Si dos operarios abren la misma tarea y uno envía primero, ¿qué ve el segundo?
**Clasificación:** 🟡 YA CUBIERTO  
**Respuesta simple:** El segundo ve un error claro: "Esta tarea ya fue tomada y completada por otro operario". El CA-04 punto 4 ya cubre esto con el HTTP 409 Conflict. La notificación proactiva por WebSocket (avisarle ANTES de que intente enviar) es una mejora de UX que pertenece a la US-029, no a la US-017.

---

### Pregunta #13 — ¿El registro de auto-reclamo guarda suficiente detalle para una auditoría?
**Clasificación:** ⚪ DIFERIDO  
**Respuesta simple:** Para V1, el evento `TASK_AUTO_CLAIMED` guarda QUIÉN, CUÁNDO y SOBRE QUÉ TAREA. Eso es suficiente para una auditoría básica. Guardar la lista completa de TODOS los candidatos que podrían haber tomado la tarea (el grupo completo de abogados, por ejemplo) añade complejidad sin resolver un problema inmediato. Cuando la auditoría regulatoria lo exija, se añade como campo adicional al evento.

---

### Pregunta #14 — ¿Qué información incluye cada registro de rechazo?
**Clasificación:** ✅ **GENERA CA-11**  
**Respuesta simple:** Cada registro de rechazo es como una "nota de devolución" que el revisor le deja al operario. Necesita incluir: QUIÉN lo devolvió (nombre del revisor), CUÁNDO (fecha y hora), POR QUÉ (el motivo de rechazo escrito por el revisor), y EN QUÉ ETAPA del proceso ocurrió. Sin estos campos, el operario recibe una devolución "anónima" sin contexto, como recibir un paquete devuelto sin saber quién lo rechazó ni por qué.

---

### Pregunta #15 — ¿Se muestra todo el historial de rechazos o solo los más recientes?
**Clasificación:** ✅ **GENERA CA-11** (incluido en el mismo CA)  
**Respuesta simple:** Se muestra el **último rechazo como Alert principal** y el historial completo como sección plegable debajo. Si una tarea se rechazó 15 veces, el operario necesita ver PRIMERO el motivo más reciente (la razón por la que le devolvieron esta vez), y opcionalmente puede expandir el historial para ver los rechazos anteriores. Es como un correo: ves el último mensaje arriba y el hilo completo abajo.

---

### Pregunta #16 — ¿El operario puede responder al rechazo con una nota?
**Clasificación:** ⚪ DIFERIDO  
**Respuesta simple:** Para V1, NO. El rechazo es informativo (solo lectura). La "respuesta" del operario ES EL NUEVO ENVÍO del formulario corregido. Añadir un campo de "nota de corrección" es una mejora de comunicación interna que puede implementarse en V2 si los usuarios lo solicitan. No es necesario para la funcionalidad base.

---

### Pregunta #17 — ¿Se guarda un solo borrador o un historial de borradores?
**Clasificación:** 🟡 YA CUBIERTO  
**Respuesta simple:** UN SOLO borrador por tarea. El borrador es como un bloc de notas borrable: cada vez que guardas, REEMPLAZAS lo anterior. No necesitas un historial de borradores porque el borrador NO es el documento final. El CA-07 ya define que los borradores "se sobrescriben en cada Merge Commit y se destruyen tras el submit". Un historial de borradores sería como guardar todas las versiones de una nota adhesiva.

---

### Pregunta #18 — ¿El reloj del borrador se reinicia solo cuando se edita o también cuando se lee?
**Clasificación:** ❌ NO GENERA VALOR  
**Respuesta simple:** Solo cuando se EDITA (PUT). Leer un borrador no lo mantiene vivo. Si el operario abre la tarea para "ver" sin hacer nada durante 3 días, el borrador expira y tendrá que empezar de nuevo. Es un detalle de implementación que no necesita un CA propio.

---

### Pregunta #19 — ¿Quién revisa los cambios que tocan ambas historias gemelas?
**Clasificación:** ❌ NO GENERA VALOR  
**Respuesta simple:** El Arquitecto y el PO. Esto es un proceso de gestión de equipo, no un requisito del producto. El CA-08 ya establece la regla de que los PRs que tocan ambas historias deben referenciar ambas. Quién revisa es una decisión del equipo, no del producto.

---

### Pregunta #20 — ¿Hay un proceso que audite si una historia invadió territorio de la otra?
**Clasificación:** ❌ NO GENERA VALOR  
**Respuesta simple:** Eso es lo que hacemos AHORA con estos análisis de refinamiento. No es algo que el producto haga automáticamente; es parte de la disciplina del equipo.

---

## 2. SEGURIDAD Y HARDENING (10 Preguntas)

---

### Pregunta #21 — ¿Los datos personales en la bóveda de eventos están protegidos?
**Clasificación:** ✅ **GENERA CA-12**  
**Respuesta simple:** SÍ, deben estarlo. Si en el navegador del operario los datos personales (cédula, teléfono) se cifran antes de guardarlos localmente (US-029 CA-11), sería contradictorio que en el servidor se guarden en texto plano donde cualquier persona con acceso a la base de datos podría leerlos. Los datos personales en el Event Store deben estar cifrados a nivel de base de datos.

---

### Pregunta #22 — ¿Los borradores devuelven datos personales sin protección?
**Clasificación:** 🟡 YA CUBIERTO  
**Respuesta simple:** Los borradores devuelven los datos TAL CUAL los guardó el operario. No se enmascaran porque el operario ya los vio cuando los escribió — sería absurdo que le escondas su propia cédula. La protección del CA-07 (Implicit Locking) garantiza que SOLO el operario dueño de la tarea puede recuperar sus borradores. Nadie más puede ver tus borradores.

---

### Pregunta #23 — ¿Las llaves de protección contra doble envío se limpian?
**Clasificación:** ❌ NO GENERA VALOR  
**Respuesta simple:** Las llaves son parte del índice de la tabla de eventos. Como la tabla de eventos es de "solo agregar" y nunca se borra, las llaves viven con los eventos para siempre. Esto no es un problema porque cada evento tiene su propia llave única, y el volumen de llaves es igual al volumen de eventos (que ya está gestionado con la política de archivado). No necesita un CA propio.

---

### Pregunta #24 — ¿El proceso de deshacer está protegido contra manipulación?
**Clasificación:** ❌ NO GENERA VALOR  
**Respuesta simple:** SÍ. Toda la plataforma usa consultas protegidas ("prepared statements") que impiden que alguien manipule los identificadores para borrar datos que no debería. Es un estándar básico de desarrollo que se aplica a TODA la aplicación, no solo a este caso. No necesita un CA propio.

---

### Pregunta #25 — ¿Se verifica que la tarea pertenezca al proceso correcto?
**Clasificación:** 🟡 YA CUBIERTO  
**Respuesta simple:** SÍ. El CA-04 valida que el usuario sea el `assignee` legítimo de la tarea O sea miembro del grupo autorizado. Camunda internamente vincula cada tarea con su proceso, así que no puedes completar una tarea de un proceso ajeno. El Implicit Locking de US-029 (CA-07/CA-18) complementa esta validación. No se necesita un CA adicional.

---

### Pregunta #26 — ¿Los operarios normales pueden consultar la bóveda de eventos?
**Clasificación:** ⚪ DIFERIDO  
**Respuesta simple:** Para V1, NO se expone un endpoint de consulta del Event Store. Los eventos están diseñados para alimentar internamente los reportes (dashboards). Si en el futuro se necesita exponer un "timeline de auditoría" para el operario o para un supervisor, se diseñará con filtros estrictos (solo puedes ver eventos de TUS tareas). Pero para V1, nadie consulta el Event Store directamente.

---

### Pregunta #27 — ¿El proceso de transformación a reportes tiene permisos propios?
**Clasificación:** ❌ NO GENERA VALOR  
**Respuesta simple:** SÍ, usa una cuenta de servicio interna (no la del operario). Es un detalle de implementación: el Worker es un proceso del servidor que tiene acceso completo a la base de datos porque ES parte del servidor. No necesita que el operario le preste sus permisos.

---

### Pregunta #28 — ¿El Auto-Claim valida que el usuario realmente pertenezca al grupo?
**Clasificación:** ✅ **GENERA CA-13**  
**Respuesta simple:** DEBE hacerlo. Si alguien conoce el número de una tarea de grupo pero NO pertenece al grupo de trabajo "Abogados", NO debería poder auto-reclamarla y completarla. Es como si alguien con la llave de la oficina entrara a un expediente que no es de su departamento. El CA-04 asume que el RBAC lo filtra, pero no lo dice explícitamente. Necesita un CA que lo formalice.

---

### Pregunta #29 — ¿Hay protección contra bombardeo de guardados automáticos?
**Clasificación:** ✅ **GENERA CA-14**  
**Respuesta simple:** DEBE haberla. Si alguien malintencionado (o un error del navegador) envía miles de peticiones de guardar borrador por segundo, podría saturar la base de datos y hacer que el sistema deje de funcionar para TODOS los operarios. Es como si alguien llamara al 911 miles de veces, saturando la línea para las emergencias reales. Se necesita un límite de velocidad.

---

### Pregunta #30 — ¿Los motivos de rechazo se sanitizan contra código malicioso?
**Clasificación:** 🟡 YA CUBIERTO  
**Respuesta simple:** SÍ. Todos los textos que se muestran en la pantalla se tratan como TEXTO PLANO, no como código. Si un revisor escribe código malicioso en el motivo de rechazo, se mostrará como texto literal (el operario vería los caracteres extraños, no se ejecutaría nada). Esta es una práctica de seguridad estándar de la plataforma que aplica a TODA la UI (US-029), no específicamente a la US-017.

---

## 3. UX/UI (10 Preguntas)

---

### Pregunta #31 — ¿Qué mensaje ve el operario cuando el motor está caído?
**Clasificación:** 🟡 YA CUBIERTO (US-029)  
**Respuesta simple:** El CA-03 de la US-017 define que se devuelve un error "Motor No Disponible". La EXPERIENCIA VISUAL de ese error (cómo se muestra, qué texto ve, si hay un overlay rojo o un modal) es responsabilidad de la US-029. La US-029 CA-20 ya define que el overlay muestra mensajes de error detallados. La US-017 solo dice QUÉ error devolver; la US-029 dice CÓMO mostrarlo.

**Dato clave para el operario:** Cuando ves este error, tus datos SÍ se perdieron (por culpa del Rollback). El sistema deshizo el envío porque no pudo completar el ciclo. Pero tu BORRADOR en el navegador debería seguir ahí. Solo necesitas intentar de nuevo presionando [Enviar].

---

### Pregunta #32 — ¿El aviso de rechazo es plegable o fijo?
**Clasificación:** 🟡 YA CUBIERTO (US-029)  
**Respuesta simple:** La decisión de si es plegable o fijo es 100% una decisión de la UI, que pertenece a la US-029. El CA-11 nuevo (generado por la pregunta #14/#15) define QUÉ información mostrar; la US-029 define CÓMO mostrarlo.

---

### Pregunta #33 — ¿Qué ve el operario que perdió la carrera del Auto-Claim?
**Clasificación:** 🟡 YA CUBIERTO  
**Respuesta simple:** Ve un mensaje tipo: "Esta tarea ya fue tomada por otro operario". El CA-04 punto 4 ya lo define con el HTTP 409. La experiencia visual (modal, toast, redirect) es responsabilidad de la US-029.

---

### Pregunta #34 — ¿El operario recibe un número de referencia tras enviar?
**Clasificación:** ✅ **GENERA CA-15**  
**Respuesta simple:** SÍ, debería. Cuando envías un formulario oficial, siempre recibes un número de radicado o referencia. Si el operario necesita reportar un problema a soporte técnico, debe poder decir: "mi envío tiene la referencia EVT-abc123, ¿pueden verificar?". Sin esa referencia, la conversación con soporte sería: "envié algo ayer a las 3pm, creo..."  como ir al banco sin tu comprobante de consignación.

---

### Pregunta #35 — ¿Qué pasa si el borrador del servidor es de una versión antigua del formulario?
**Clasificación:** 🟡 YA CUBIERTO (US-029)  
**Respuesta simple:** Se aplica el Lazy Patching de la US-029 CA-08: los campos viejos se cargan con sus datos, los campos nuevos aparecen en rojo (vacíos, obligatorios). El operario tiene que completarlos antes de poder enviar. Es como cuando cambian el formato de la declaración de renta: te traen los datos del año anterior pero los campos nuevos los tienes que llenar tú.

---

### Pregunta #36 — ¿Mostrar quién rechazó puede generar conflictos entre empleados?
**Clasificación:** 🟡 YA CUBIERTO (decisión del CA-11)  
**Respuesta simple:** SÍ se muestra quién rechazó. En un entorno judicial/empresarial, la trazabilidad es obligatoria. Cada acción debe tener un responsable identificable. Si un revisor rechaza un formulario, el operario tiene derecho a saber QUIÉN lo rechazó para poder consultar dudas directamente. La anonimización se podría contemplar en V2 si genera problemas reales, pero para V1 prima la transparencia.

---

### Pregunta #37, #38 — ¿Los reportes avisan si están desactualizados?
**Clasificación:** 🟡 YA CUBIERTO (US-009)  
**Respuesta simple:** Eso es responsabilidad de los dashboards (US-009), no de la US-017. La US-017 produce los datos; la US-009 los muestra. Si la US-009 quiere mostrar "última actualización: hace 5 minutos", es su decisión de diseño.

---

### Pregunta #39 — ¿Cuándo se borra el borrador del servidor al completar una tarea?
**Clasificación:** ✅ **GENERA CA-16**  
**Respuesta simple:** ANTES de redirigir al operario. Si el borrador se borrara DESPUÉS y el proceso de borrado falla, el borrador se quedaría como "fantasma" en el servidor. La próxima vez que el operario abriera otra tarea, podría encontrar datos viejos de la tarea anterior. La limpieza del borrador debe ser PARTE del proceso de envío exitoso, no un paso posterior.

---

### Pregunta #40 — ¿Hay indicador visual cuando el borrador se sincroniza al servidor?
**Clasificación:** 🟡 YA CUBIERTO (US-029)  
**Respuesta simple:** SÍ, la US-029 CA-31 ya define el indicador de sincronización (☁️ nube cuando se guarda al servidor, 💾 disco cuando es solo local). La US-017 no necesita definir nada adicional porque el Frontend (US-029) ya maneja esa experiencia visual.

---

## 4. RENDIMIENTO (5 Preguntas)

---

### Pregunta #41 — ¿Cuánto puede tardar como máximo el envío del formulario?
**Clasificación:** ✅ **GENERA CA-17**  
**Respuesta simple:** Máximo **5 segundos** desde que el operario presiona [Enviar] hasta que recibe la confirmación (o el error). 5 segundos es el límite psicológico aceptable: menos de 3 segundos se siente "instantáneo", entre 3 y 5 es "tolerable con spinner", más de 5 el operario empieza a preguntar "¿se trabó?". Este SLA incluye TODO el proceso interno: validar datos, grabar el evento, notificar al motor de procesos, y responder.

---

### Pregunta #42 — ¿Cuánto tarda en aparecer un dato en los reportes después de enviarlo?
**Clasificación:** ⚪ DIFERIDO  
**Respuesta simple:** Para V1, la meta es **menos de 30 segundos**. Los dashboards no son "tiempo real" — si un operario completa un formulario y abre el dashboard 30 segundos después, su tarea debería aparecer. Esto es suficiente para la operación normal y no requiere un CA propio. Es más una meta de rendimiento que un requisito funcional.

---

### Pregunta #43 — ¿Qué pasa si 200 personas envían formularios al mismo tiempo?
**Clasificación:** ❌ NO GENERA VALOR  
**Respuesta simple:** El Event Store soporta escritura masiva porque cada envío es un INSERT independiente (no se bloquean entre sí). La pregunta es válida para una prueba de carga, no para un requisito de negocio. El equipo de QA lo verificará como parte de las pruebas de estrés. No necesita un CA.

---

### Pregunta #44 — ¿Qué pasa cuando se acumulan muchos datos en un año?
**Clasificación:** ✅ **GENERA CA-18**  
**Respuesta simple:** Si cada formulario pesa alrededor de 100KB y hay 500,000 envíos al año, la bóveda de eventos acumulará unos 50GB anuales. Después de un año, los eventos antiguos que ya no se consultan frecuentemente deben moverse a un "archivo frío" (como mudar expedientes viejos a una bodega) para que la tabla principal sea rápida. Es como la gestión documental de una oficina: los expedientes del año pasado van a la bodega, los activos quedan en el escritorio.

---

### Pregunta #45 — ¿Los borradores automáticos saturan las conexiones del servidor?
**Clasificación:** 🟡 YA CUBIERTO  
**Respuesta simple:** El CA-14 nuevo (generado por la pregunta #29) establece un límite de velocidad que previene la saturación. Además, el Debounce de 10 segundos de la US-029 ya limita la frecuencia a ~6 guardados por minuto por operario. Con 100 operarios, son ~600 peticiones por minuto, que es un volumen manejable para cualquier servidor moderno.

---

## RESUMEN: ¿Qué preguntas generan CAs?

| # Pregunta | Tema | Nuevo CA | ¿Qué resuelve? |
|---|---|---|---|
| #2 | Borradores NO van al Event Store | **CA-09** | Evita saturar la bóveda con ruido |
| #8, #9, #10 | Rollback usa evento compensatorio + retry 3x + timeout 10s | **CA-10** | Define cómo se "deshace" un envío de forma limpia |
| #14, #15 | Estructura del registro de rechazo + historial plegable | **CA-11** | El operario sabe POR QUÉ, QUIÉN y CUÁNDO le devolvieron |
| #21 | Datos personales cifrados en el Event Store | **CA-12** | Protege la privacidad en la bóveda del servidor |
| #28 | Auto-Claim valida pertenencia al grupo | **CA-13** | Impide que alguien reclame tareas de otro departamento |
| #29 | Límite de velocidad en borradores | **CA-14** | Previene saturación del servidor por abuso |
| #34 | Referencia visible de evento para el operario | **CA-15** | El operario tiene un "comprobante" citeable |
| #39 | Borrador se elimina ANTES del redirect | **CA-16** | Elimina borradores fantasma |
| #41 | SLA máximo 5 segundos para /complete | **CA-17** | Garantiza experiencia fluida |
| #44 | Archivado anual de eventos históricos | **CA-18** | Previene que la base de datos se vuelva lenta |

---

## Preguntas que NO generaron CAs (35):

| Motivo | Preguntas |
|---|---|
| **Ya cubierto por CAs existentes** | #5, #6, #11, #12, #17, #22, #25, #30, #35, #36, #40, #45 |
| **Pertenece a otra US** (US-029, US-009, US-005) | #7, #31, #32, #33, #37, #38 |
| **Detalle de implementación** (no requisito de negocio) | #1, #18, #19, #20, #23, #24, #27 |
| **Diferido a V2** | #3, #4, #13, #16, #26, #42 |
