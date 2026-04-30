# Handoff Backend — ARQ-028-02 | Refactorización Hexagonal de Auditoría

---

## 1. Metadatos y SSOT

| Campo | Valor |
|-------|-------|
| **Iteración** | Deuda Técnica y Refactorización (Iteración 5 / sprint-6) |
| **Rama Git** | `sprint-6` |
| **Deuda a Cerrar** | **ARQ-028-02:** `JdbcTemplate` con SQL crudo en Application Service |
| **SSOT** | `task.md` (Punto ARQ-028-02) |
| **Flujo de Trabajo** | Backend → QA |

---

## 2. Contexto Arquitectónico

Actualmente, `FormCertificationService.java` (Capa de Aplicación) inyecta directamente `JdbcTemplate` para ejecutar un `INSERT INTO ibpms_audit_log...` con SQL crudo y casting `::jsonb`.
Según la Arquitectura Hexagonal y la directiva de Cierre de Deuda Técnica, **la Capa de Aplicación no debe conocer detalles de infraestructura ni SQL crudo**. Se debe extraer esta lógica hacia un Puerto (Out) y un Adaptador.

---

## 3. Instrucciones de Implementación

Tu objetivo es eliminar la inyección de `JdbcTemplate` de `FormCertificationService.java` y abstraer el guardado en log de auditoría.

### Tarea 1: Crear el Puerto de Salida (application/port/out)
Crea la interfaz `AuditLogPort.java` en el paquete `com.ibpms.poc.application.port.out`.
Debe exponer un contrato agnóstico, por ejemplo:
`void saveAuditLog(String id, String entityType, String entityId, String eventType, String performedBy, java.time.LocalDateTime createdAt, byte[] payloadSnapshot, boolean isCompressed, boolean truncated, String detailsJson);`

### Tarea 2: Crear el Adaptador de Infraestructura (infrastructure/adapter)
Crea la clase `AuditLogJpaAdapter.java` (o `AuditLogJdbcAdapter.java`) en `com.ibpms.poc.infrastructure.adapter`.
- Anótala con `@Component`.
- Implementa `AuditLogPort`.
- **Inyecta `JdbcTemplate` aquí.**
- Mueve el query `INSERT INTO ibpms_audit_log...` exactamente como estaba en el servicio a este adaptador.

### Tarea 3: Refactorizar el Servicio de Aplicación
Modifica `FormCertificationService.java`:
1. Elimina la importación y la dependencia de `JdbcTemplate`.
2. Inyecta `AuditLogPort` en su lugar.
3. Actualiza el método privado `auditLog(...)` para que llame a `auditLogPort.saveAuditLog(...)` en lugar de invocar a `jdbcTemplate` directamente.

---

## 4. Criterios de Aceptación y Veredicto
- [ ] `FormCertificationService.java` **no debe tener** `import org.springframework.jdbc.core.JdbcTemplate;`.
- [ ] La compilación y los tests de unidad existentes (`FormCertificationTest.java` u otros) deben pasar exitosamente (`mvn clean test`).
- [ ] NO alterar la lógica de truncamiento ni compresión GZIP de payloads (CA-15), que pertenece a la capa de aplicación. El puerto solo guarda la data ya procesada.

> ⚠️ **REGLAS DE EJECUCIÓN:** 
> 1. Inicia en PLANNING y genera un plan rápido.
> 2. Haz las modificaciones y ejecuta los tests de compilación.
> 3. Al terminar, notifica que el código está listo para la certificación de QA.
