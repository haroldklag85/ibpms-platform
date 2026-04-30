# Handoff de Remediación Integrada: US-038 (Federación de Identidad y Fail-Open) - Bloque 2

**Fecha/Hora:** 2026-04-18
**Contexto:** Auditoría Técnica y Forense para el Bloque 2 de la US-038 por el Agente Arquitecto Líder.
**Alcance:** Remediación de las brechas funcionales de los Criterios de Aceptación CA-07 al CA-13.

---

## 🛑 Hallazgos y GAPs Detectados (Auditoría Forense - Bloque 2)

He recertificado pasivamente el **CA-13 (Postergación de Reset Password)** ya que, por diseño, se omiten módulos MVP.
Sin embargo, declaro 4 GAPs ineludibles que componen el Bloque 2 de la remediación:

1. **GAP de Conexión AMQP (CA-07 y CA-08 - Exorcismo Zombie):** La infraestructura RabbitMQ (`TaskRescueProducer` y `TaskRescueConsumer`) está impecablemente armada, vinculada a Dead-Letter-Queues. **PERO:** El productor (`triggerMassiveUnclaim`) es **Código Muerto**. Jamás se invoca. El Backend debe acoplar el disparo a las acciones reales de negocio.
2. **GAP de Trazabilidad SRE (CA-09 - Correlation-ID inyectado):** Carecemos de un mecanismo transversal que etiquete las solicitudes HTTP. Falta un JWT Claims logger que estampe el estado de permisos y asigne un `X-Correlation-ID` para seguimiento de Microservicios v2.
3. **GAP Visual Workdesk y Master Layout (CA-10 y CA-11):** La cabecera del sistema (`MainLayout.vue`) no expone dinámicamente los sombreros operativos del usuario (Ej: `Director | Aprobador VIP`), y la grilla de tareas operativas omite el "Badge" que explica bajo qué privilegio el usuario ve dicho caso.
4. **GAP de UX Defensiva (CA-12 - Tablero de Anomalías):** Las violaciones de "Juez y Parte" (CA-06 implementado en el bloque previo) o fallos SRE carecen de un visor de control CISO. Falta un Tab "Tablero de Anomalías" en Pantalla 14, atado a una tabla `ibpms_security_anomalies`.

---

## 🛠️ Cuadrilla de Desarrollo: Directivas de Remediación

### Para el Agente Backend (Experto Data & Security)
1. **Ensamblado del Event Bus (CA-07 y CA-08):** Inyecte la dependencia `TaskRescueProducer` dentro de `DelegationService.java` y `UserService.java`. 
   - En `UserService.deactivateUser()`, dispárelo para limpiar tareas del empleado despedido. 
   - En `DelegationService.createDelegation()`, dispárelo para trasladar provisionalmente o destrabar la cola del donante de poder.
2. **Correlation-ID (CA-09):** Construir un `OncePerRequestFilter` genérico (Ej. `CorrelationIdFilter.java`) que genere un `java.util.UUID` si `X-Correlation-ID` no viene en los headers. Guárdelo en el MDC de SLF4J (`MDC.put("correlationId", uuid)`) para que los logs viajen tagueados.
3. **Persistencia de Anomalías (CA-12):** Cree el esquema Base Entity + Repository para `SecurityAnomalyEntity` (Mensaje, Stacktrace, Gravedad, Estado "Abierto/Subsanado"). Integre un POST y GET en `SecurityAdminController`. Cuando el AOP Juez/Parte salte, grabe en esta tabla.

### Para el Agente Frontend (Experto UI)
1. **Tablero CISO (CA-12):** Agregue la Pestaña "Anomalías" en `RbacTabs.vue` o `RbacManagerView.vue`. Que invoque el `GET` al log de anomalías del Backend para visualizar intentos de Hack o Juez-y-Parte con un botón mitigador `[✅ Subsanar]`.
2. **Inyección Visual Roles (CA-10 y CA-11):** Modifique `MainLayout.vue` conectando el store Auth para leer el Array de Roles y pintarlos en texto pequeño bajo el avatar del nav. Añada el badge del rol resolutivo en la grilla del `WorkdeskView.vue`.

### Para el Agente QA (Testing E2E)
1. **Flujo Zombie (CA-07/08):** Integre Testcontainers para RabbitMQ. Valide en Integración que desactivar a un usuario verdaderamente dispara el Queue, y revise que el listener limpie las Tareas en Camunda DB.

---

**Protocolo de Uso:** Procedan de inmediato y repártanse las tareas en paralelo. Esta es la fase terminal de la US-038.
