# 🛠️ HANDOFF DE DELEGACIÓN — Agente Backend
## Misión: Remediación Crítica Seguridad y Auditoría (US-034/CA-8)

| Metadato | Valor |
|---|---|
| **Especialidad** | Java / Spring Boot / Security |
| **Iteración** | Hotfix 70-DEV (Post-Auditoría QA) |
| **Rama Destino** | `sprint-3/informe_auditoriaSprint1y2` (Aplica los cambios directamente a esta rama) |
| **Severidad** | 🔴 CRÍTICA (Bloquea pase a producción) |

---

## 📋 1. Contexto del Defecto (Hallazgos de QA)

El Agente QA ha certificado la arquitectura asíncrona de resiliencia (RabbitMQ), pero ha bloqueado el CA-8 (Panel DLQ) al detectar dos brechas críticas de cumplimiento en el archivo `DlqAdminController.java`:

- **DEF-02 (Brecha RBAC):** Los endpoints expuestos para purgar buzones o reenviar mensajes a la cola principal NO están protegidos. Cualquier usuario autenticado podría invocar la limpieza de mensajes.
- **DEF-03 (Brecha de Trazabilidad):** Las operaciones destructivas (Purge) y mutables (Retry) no dejan rastro en el ledger inmutable oficial `ibpms_audit_log` (Javers/Custom Logging), violando la arquitectura Base.

## 🎯 2. Objetivos de Ejecución (Lo que DEBES programar)

Debes modificar `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/web/admin/DlqAdminController.java` y cualquier servicio subyacente si fuese necesario.

### Tarea A: Corrección DEF-02 (RBAC Estricto)
- Inyectar seguridad pre-invocación a **TODOS** los endpoints HTTP expuestos en la clase.
- Aplicar la restricción explícita: `@PreAuthorize("hasRole('ADMIN_IT')")`.
- *Asegúrate de que la clase o los métodos tengan habilitada esta anotación de Spring Security.*

### Tarea B: Corrección DEF-03 (Audit Trail Inmutable)
- Implementar trazabilidad en los endpoints POST/DELETE (Re-queue y Purge).
- Todo evento de purga o reencolado debe inyectar un log detallando: qué identificador de mensaje se afectó, qué usuario invocó la acción (extraído del SecurityContext), y un timestamp.

---

## 📐 3. LEYES FRONTALES (Gobierno de Agentes)

> **⚠️ LEY 1: CERO ALUCINACIONES ARQUITECTÓNICAS**
> Solo vas a modificar y arreglar la inyección de la seguridad (`@PreAuthorize`) y la auditoría. No reinventes la topología AMQP ni reescribas la lógica existente de integración con las colas.

> **⚠️ LEY 2: ZERO-TRUST GIT (PROHIBIDO STASH)**
> Queda **TERMINANTEMENTE PROHIBIDO** el uso de `git stash` o `git stash save` para entregar esta tarea. 
> Tu única vía de entrega es:
> 1. Modificar el código fuente.
> 2. Probar compilación con `mvn clean compile`.
> 3. Ejecutar `git commit -am "fix(security): resolve DEF-02 and DEF-03 gaps in DlqAdmin"`.
> 4. Ejecutar `git push origin sprint-3/informe_auditoriaSprint1y2`.

## 🚀 4. INICIO DE TRANSFERENCIA
Si has leído y comprendido este contrato de delegación, puedes proceder a refactorizar el código en tu rama local, compilar, comitear y avisar finalización garantizando las políticas antifragilidad.
