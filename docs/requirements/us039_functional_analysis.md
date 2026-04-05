# Análisis Funcional y de Entendimiento: US-039

## Historia Analizada
**US-039: Formulario Genérico Base (Pantalla 7.B - El Camaleón Operativo)**

---

### 1. Resumen del Entendimiento

La US-039 define un **formulario comodín minimalista** que la plataforma inyecta automáticamente en tareas operativas simples (procedimentales) donde no se justifica diseñar un iForm Maestro completo con el IDE de la Pantalla 7. Es el "Plan B" del sistema de formularios: cuando una tarea de un BPMN no tiene asociado un formulario personalizado, o cuando se trata de una actividad tipo Kanban huérfana, la plataforma asigna silenciosamente esta Pantalla 7.B en lugar de mostrar un formulario vacío o un error.

La historia opera bajo tres principios:

1. **Anti-Bypass con Restricción VIP (CA-1):** El formulario genérico NO puede usarse en tareas de alta criticidad (Alta Dirección, Aprobador Financiero, Sello Legal). Un "Pre-Flight Analyzer" bloquea su uso en esos contextos, forzando la creación de un iForm Maestro formal.

2. **Prevención de Context Bleeding (CA-2):** El Backend (BFF) filtra las variables de Camunda con un Whitelist Regex antes de enviarlas al formulario genérico, evitando que el operario vea las 200+ variables técnicas internas del proceso. Solo llegan los metadatos de negocio vitales (Case_ID, Client_Name, Priority, SLA).

3. **Botones de Pánico con Justificación (CA-3):** La interfaz incluye botones de escalamiento/retorno ("Botones de Pánico") que fuerzan al operario a escribir una justificación mínima de 20 caracteres antes de disparar un Error Event o Escalamiento en Camunda.

---

### 2. Objetivo Principal

Proveer una **interfaz operativa ligera y segura** para tareas procedimentales simples (captura de evidencia, observaciones, tracking de avance) que no requieren la potencia completa del IDE de Formularios (Pantalla 7), eliminando la necesidad de diseñar manualmente decenas de formularios básicos mientras se mantiene la trazabilidad y el control de calidad del proceso.

---

### 3. Alcance Funcional Definido

| Dimensión | Hasta Dónde Llega | Dónde Termina |
|---|---|---|
| **Renderizado** | Formulario pre-construido con cuadrícula de metadatos Solo-Lectura + área de observaciones (CA-2) | No es un diseñador; no tiene Canvas, Mónaco IDE ni paleta de componentes |
| **Seguridad de Asignación** | Pre-Flight Analyzer bloquea uso en tareas VIP (CA-1) | No define qué roles específicos son "Alta Dirección" (delega a la configuración del BPMN/RBAC) |
| **Filtrado de Datos** | Whitelist Regex en BFF para variables de Camunda (CA-2) | No define la lista exacta de variables permitidas; depende de configuración por proceso |
| **Escalamiento** | Botones de Pánico con justificación obligatoria de 20+ chars (CA-3) | No define el flujo post-escalamiento (qué pasa después del Error Event en Camunda) |
| **Auto-vinculación** | Inyección silenciosa en tareas Kanban huérfanas (CA-1) | No cubre instanciación de procesos (solo tareas intermedias) |

---

### 4. Lista de Funcionalidades Incluidas

#### A. Control de Asignación
1. Inyección explícita del formulario genérico (`sys_generic_form`) vía Dropdown en Pantalla 6 (CA-1).
2. Pre-Flight Analyzer que bloquea uso en tareas de Alta Dirección, Aprobador Financiero o Sello Legal (CA-1).
3. Auto-vinculación silenciosa en tareas Kanban huérfanas sin formulario asociado (CA-1).

#### B. Renderizado Operativo
4. Cuadrícula superior de metadatos Solo-Lectura (Case_ID, Client_Name, Priority, SLA) (CA-2).
5. Filtrado BFF con Whitelist Regex que oculta variables técnicas de Camunda (CA-2).

#### C. Acciones y Escalamiento
6. Botones de Pánico: Aprobado / Retorno al Generador / Cancelar (CA-3).
7. Observación justificativa obligatoria (min 20 chars) antes de consumar escalamiento o Error Event (CA-3).

---

### 5. Lista de Brechas, GAPs o Ambigüedades Detectadas

#### GAP-1: Ausencia Total del Cuerpo del Formulario (¿Qué campos tiene el formulario genérico?)
La historia define con precisión:
- *Qué muestra arriba*: cuadrícula de metadatos Solo-Lectura (CA-2).
- *Qué muestra abajo*: botones de Pánico (CA-3).

Pero **no define qué campos editables contiene el formulario entre ambas zonas**. ¿Tiene un campo de texto libre "Observaciones"? ¿Un dropzone para adjuntos de evidencia? ¿Un checkbox de confirmación? ¿Un textarea multi-línea?

Para una historia que se auto-describe como formulario de "captura de evidencia, observaciones y tracking de avance", la ausencia total de los campos editables es un vacío crítico. El desarrollador Frontend no sabe qué renderizar en el cuerpo principal.

**Riesgo:** Sin definición, el Frontend podría implementar un simple textarea o podría sobreingenierar un mini-formulario con 10 campos. Ambos extremos son incorrectos sin directriz.

#### GAP-2: Whitelist Regex (CA-2) — ¿Quién la configura y dónde?
El CA-2 exige un "Whitelist Regex o filtro estricto" para las variables de Camunda que llegan al formulario genérico. Pero no aclara:
- ¿Es una lista global (misma para todos los procesos) o configurable por proceso?
- ¿Se configura en el BFF mediante un archivo YAML/properties o en la Pantalla 6 durante el diseño del BPMN?
- ¿Quién tiene permiso para modificar la whitelist (Arquitecto, Super Admin, PMO)?

**Riesgo:** Si es global, procesos distintos con variables de negocio distintas (uno usa `Case_ID`, otro usa `Folio_Number`) no podrán filtrar correctamente. Si es por proceso, necesita una UI de configuración que no está definida.

#### GAP-3: Definición de "Roles VIP" que bloquean el formulario genérico (CA-1)
El CA-1 menciona tres categorías que disparan el Hard-Stop: "Alta Dirección", "Aprobador Financiero" y "Sello Legal". Pero:
- ¿Estos son roles RBAC definidos en la Pantalla 14 (US-036)?
- ¿O son etiquetas semánticas asociadas a la tarea dentro del BPMN?
- ¿Es una lista extensible o fija (hardcoded)?
- ¿Dónde se configura qué rol es "VIP"?

**Riesgo:** Si están hardcodeados en el Pre-Flight Analyzer, agregar un nuevo rol VIP requiere un despliegue de código. Si están en una tablas de configuración, necesita una UI de administración.

#### GAP-4: Relación con el ciclo de persistencia de datos (US-029)
El formulario genérico captura datos (observaciones, evidencia) pero la historia no especifica:
- ¿Los datos se persisten como variables de proceso en Camunda (`runtimeService.setVariable`)?
- ¿Se persisten en la tabla de borradores CQRS de la US-029?
- ¿El formulario genérico soporta auto-guardado (como CA-24/CA-85 de US-003) o es de un solo envío?

**Riesgo:** Si se trata como un formulario "fire-and-forget" sin borrador, el operario pierde todo si cierra la pestaña accidentalmente durante la redacción de una observación de 200 caracteres.

#### GAP-5: Comportamiento post-Botón de Pánico (CA-3) — ¿Qué evento BPMN se dispara?
El CA-3 dice que los botones de pánico consuman "un Error Event o Escalamiento en el Motor de Camunda", pero no especifica:
- ¿Qué `errorCode` se inyecta en el BPMN Error Event?
- ¿El "Retorno al Generador" reasigna la tarea al usuario anterior en el flujo o vuelve a una tarea BPMN específica?
- ¿"Cancelar" anula la tarea (complete + variable de rechazo) o cancela toda la instancia del proceso?

**Riesgo:** Sin mapeo explícito, cada implementación de un BPMN interpretará los botones de pánico de forma diferente, causando inconsistencia operativa.

---

### 6. Lista de Exclusiones (Fuera de Alcance)

1. **Diseño visual del formulario genérico** — NO es editable con el IDE de Pantalla 7; su estructura es fija.
2. **Validación Zod reactiva** — No mencionada; la historia asume que el formulario genérico no necesita validaciones complejas.
3. **Formularios multi-etapa** — El formulario genérico es plano, sin tabs, acordeones ni DataGrids.
4. **Generación de código Vue** — No aplica; el componente es pre-construido, no generado.
5. **Versionamiento inmutable** — No mencionado; el formulario genérico tiene una sola versión sistémica.
6. **Exposición pública B2C** — No mencionado; solo aplica a tareas internas.
7. **Herramientas QA (Sandbox/Auto-Vitest)** — No aplica por ser un formulario estático pre-construido.
8. **Smart Buttons CQRS** — La historia tiene sus propios "Botones de Pánico" en lugar de los Smart Buttons de US-003.

---

### 7. Observaciones de Alineación o Riesgos para Continuar

> [!WARNING]
> **Historia Peligrosamente Corta.** Con solo 3 CAs, la US-039 es la historia más compacta del backlog de formularios. Paradójicamente, esa brevedad genera más ambigüedad que una historia de 86 CAs como la US-003, porque deja demasiadas decisiones sin formalizar. Los 5 GAPs identificados son todos derivados de la falta de especificación, no de errores en lo que está escrito.

> [!IMPORTANT]
> **Dependencias Externas Críticas.** La US-039 depende directamente de:
> - **US-003 (Pantalla 7):** El Pre-Flight Analyzer que decide si un formulario genérico es admisible o si se fuerza un iForm Maestro.
> - **US-005 (Despliegue BPMN):** El Pre-Flight Analyzer reside en el pipeline de despliegue del BPMN.
> - **US-029 (Persistencia CQRS):** Si el formulario genérico persiste datos, consume los mismos endpoints de borrador.
> - **US-036 (RBAC):** La lista de "Roles VIP" que bloquean el uso del formulario genérico viene del módulo de permisos.
> - **US-034 (RabbitMQ):** Los Error Events disparados por los Botones de Pánico se enrutan a través del broker de mensajería.

> [!NOTE]
> **Fortaleza Arquitectónica.** A pesar de su brevedad, la historia incorpora dos decisiones de diseño maduras:
> 1. El **Whitelist Regex BFF** evita el Context Bleeding (filtrar variables internas del motor) — un problema real en implementaciones BPM donde los formularios muestran variables técnicas por accidente.
> 2. La **justificación obligatoria de 20 chars** para escalamiento previene el "click-and-forget" donde operarios escalan sin documentar por qué, arruinando la trazabilidad del proceso.

> [!CAUTION]
> **El HANDOFF TÉCNICO final (líneas 1177-1180) es gobernanza, no funcionalidad.** Las notas de "Eliminación de variables Toggle" y "Prevención de colisiones de Namespace" son restricciones de implementación certificadas por QA SRE, no criterios de aceptación. Deben respetarse como directrices técnicas pero no confundirse con CAs ejecutables.
