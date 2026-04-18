# Análisis Funcional Definitivo: US-022 (Disparo 'Confirm-to-Create' Plan A)

## 1. Resumen del Entendimiento
La US-022 funciona como un mecanismo "Pre-Contractual" de Intake. El operador SAC en vez de abrir un proceso inmediatamente a raíz de una queja, le responde al cliente con un formulario u oferta formal ("Para este reclamo procederemos a abrir una apelación SD").

## 2. Objetivo Principal
Asegurar confirmación del Lead/Cliente. Dejar evidencia auditable y encolar la solicitud Administrativa sin gastar la Base de Datos transaccional pesada (SD).

## 3. Alcance Funcional Definido
**Inicia:** Un agente responde formalmente a un cliente Inbound generándole Expectativa.
**Termina:** El motor registra evento, inyecta `correlation_id` y encola Tarea de Creación.

## 4. Lista de Funcionalidades Incluidas
- **Retardo Condicional de Instanciación (CA-1):** Se graba en auditoría, se le asigna `correlation_id` para hilo de correos (Thread). Prohíbe expresamente INICIAR el Camunda BPMN. En su lugar, el sistema le adjudica una "User Task / Tarea de Usuario" a un Admin para que decida cuándo darle Play al proceso.

## 5. Lista de Brechas, Gaps o Ambigüedades Detectadas
- **Contradicción Arquitectónica sobre Estados Huérfanos (⚠️ CA-1):** El criterio de aceptación exige imperativamente "*NO Iniciar una Instancia BPMN en Camunda*". Pero el renglón contiguo exige "*Crear una Tarea de Usuario asignada al Líder*". **GAP:** En la arquitectura iBPMS de código, el objeto transaccional primario para "User Tasks / Bandejas" ESTÁ adentro del motor BPMN. Si no inicias un BPMN, ¿dónde demonios se encola o persiste esa tarea? (¿Es una tarjeta dummy Kanban? ¿Es una tabla puente SQL `t_pending_intakes`?). Es un claro error de modelado del requerimiento. La arquitectura limpia sugiere que este "Intake Cautivo" sí deba ser un Mini-BPMN de ingesta (`intake-pipeline.bpmn`), cuya única tarea de usuario es `[Aprobar Creación (SD)]`, que finalmente lanza el proceso robusto mediante un *Message Send Task*.

## 6. Lista de Exclusiones (Fuera de Alcance V1)
- Respuestas Automáticas Robot. Es el Líder SAC el que decide y lanza "envía el correo de confirmación".

## 7. Observaciones de Alineación o Riesgos
**Fricción Paralela (Punto Muerto):** Si el Líder de SAC envía "Confirm a crear Caso #111" pero luego olvida darle play o lo rechaza administrativamente en su propia tarea personal, el cliente externo recibe una confirmación "vacía" para un trámite que jamás existirá en la plataforma central, creando disonancia Operativa (Exposición al Riesgo Legal y Sanción Gubernamental sobre la entidad que ofreció arrancar la petición pero cortó los cables por detrás).
