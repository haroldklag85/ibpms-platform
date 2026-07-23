# Manual de Referencia de Seguridad: Root de Sistema vs. Super Administrador de Tenant

Este manual técnico y operativo detalla las diferencias arquitectónicas, funcionales y de seguridad entre el **Root del Sistema** y el **Super Administrador de Tenant** dentro de la plataforma IBPMS. Está diseñado para auditores de seguridad, oficiales de cumplimiento normativo (ISO 27001) y administradores de infraestructura.

---

## 1. Resumen y Propósito Operacional

| Característica | Root del Sistema (`root@ibpms.local`) | Super Administrador de Tenant (`admin@alpha.com`) |
| :--- | :--- | :--- |
| **Definición** | Identidad de bootstrap local ("Día Cero"). | Identidad corporativa de administración local. |
| **Ámbito** | Global (Cross-Tenant / Multi-tenant completo). | Local (Aislado a un único `tenant_id`). |
| **Uso Principal** | Recuperación ante desastres (Protocolo Break-Glass). | Operación y gobernanza diaria del Tenant. |
| **Autenticación** | Contraseñas Bcrypt locales / Parámetros estáticos. | Federación externa (SSO / EntraID) o base de datos local. |
| **Inmutabilidad** | Administrador de plantillas y topologías globales. | Sujeto a reglas de inmutabilidad de roles globales. |

---

## 2. Arquitectura de Aislamiento de Datos (Multi-tenancy)

El aislamiento de datos en la plataforma se rige por diferentes niveles según la identidad autenticada:

### A. Super Administrador de Tenant
* **Restricción RLS (Row Level Security):** Todas las consultas e interacciones de este usuario con la base de datos están limitadas a través de su contexto de inquilino.
* **Resolución de Tenant:** El backend intercepta el token JWT a través de [TenantArgumentResolver.java](file:///C:/Users/HaroltAndr%C3%A9sG%C3%B3mezAgu/ProyectoAntigravity/ibpms-platform/backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/web/interceptor/TenantArgumentResolver.java) y extrae el claim `tenant_id` (ej. `tenant_alpha`). Cualquier intento de consultar datos fuera de este tenant genera un rechazo de seguridad por control de acceso de tipo IDOR.

### B. Root del Sistema
* **Bypass de Tenant (Modo Cross-Tenant):** Al representar la identidad de soporte global de último recurso, el Root elude el aislamiento del RLS.
* **Funcionalidad:** Puede consultar e inspeccionar registros de tareas, proyecciones e integraciones en todos los tenants de la plataforma sin restricciones, permitiendo diagnosticar cuellos de botella de infraestructura a nivel global.

---

## 3. Matriz de Control de Acceso y Funcionalidades

A continuación se presenta la matriz de privilegios y operaciones críticas permitidas en la API:

| Operación / Endpoint | Root del Sistema | Super Admin de Tenant | Archivo / Componente Java |
| :--- | :---: | :---: | :--- |
| **Inicio de Sesión Estándar** | ❌ (Solo emergencia) |  (SSO/Local) | [AuthSyncController.java](file:///C:/Users/HaroltAndr%C3%A9sG%C3%B3mezAgu/ProyectoAntigravity/ibpms-platform/backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/web/security/AuthSyncController.java#L42) |
| **Acceso Break-Glass (UI)** |  |  | [AuthSyncController.java](file:///C:/Users/HaroltAndr%C3%A9sG%C3%B3mezAgu/ProyectoAntigravity/ibpms-platform/backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/web/security/AuthSyncController.java#L106) |
| **API de Recuperación Pura** |  | ❌ | [EmergencyLoginController.java](file:///C:/Users/HaroltAndr%C3%A9sG%C3%B3mezAgu/ProyectoAntigravity/ibpms-platform/backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/web/security/EmergencyLoginController.java#L40) |
| **Ejecutar Kill-Switch (Revocación)** |  |  | [SecurityAdminController.java](file:///C:/Users/HaroltAndr%C3%A9sG%C3%B3mezAgu/ProyectoAntigravity/ibpms-platform/backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/web/security/SecurityAdminController.java#L46) |
| **Modificar Roles Globales Core** |  | ❌ (Inmutable) | [MenuLayoutService.java](file:///C:/Users/HaroltAndr%C3%A9sG%C3%B3mezAgu/ProyectoAntigravity/ibpms-platform/backend/ibpms-core/src/main/java/com/ibpms/poc/application/service/ui/MenuLayoutService.java#L70) |
| **Impersonación de Usuarios** |  | ❌ | [AuthSyncController.java](file:///C:/Users/HaroltAndr%C3%A9sG%C3%B3mezAgu/ProyectoAntigravity/ibpms-platform/backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/web/security/AuthSyncController.java#L309) |

---

## 4. Protocolo Break-Glass (Acceso de Emergencia) y Auditoría

La activación de la cuenta Root del sistema o inicios de sesión mediante el protocolo Break-Glass están sujetos a auditoría estricta para cumplir con la normativa **ISO 27001 (Control A.9.1.2 - Gestión de derechos de acceso especial)**:

1. **Justificación Mandatoria:** El formulario web y la API bloquean cualquier petición que no incluya un campo `justification` detallado y no vacío.
2. **Log Forense Inmutable:** El sistema persiste un registro de auditoría en la tabla `ibpms_security_audit_log` que guarda:
   - Marca de tiempo precisa de la activación del protocolo.
   - Dirección IP pública/privada de la máquina cliente.
   - Bandera `is_break_glass = true`.
   - Texto de la justificación técnica ingresada por el operador.
3. **Alertas y Monitoreo:** Estas alertas están diseñadas para conectarse a herramientas SIEM corporativas debido a la naturaleza crítica del acceso global.

---

## 5. Trazabilidad de Código de Gobierno

Para propósitos de desarrollo y auditoría del código fuente, el comportamiento de estos dos roles se encuentra implementado en los siguientes archivos:

* **Inyección Inicial del Root:** Inicializado en el arranque en [DataSeeder.java](file:///C:/Users/HaroltAndr%C3%A9sG%C3%B3mezAgu/ProyectoAntigravity/ibpms-platform/backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/startup/DataSeeder.java).
* **Restricción de Modificación de Roles:** Reglas de inmutabilidad del rol `SUPER_ADMIN` en [MenuLayoutService.java](file:///C:/Users/HaroltAndr%C3%A9sG%C3%B3mezAgu/ProyectoAntigravity/ibpms-platform/backend/ibpms-core/src/main/java/com/ibpms/poc/application/service/ui/MenuLayoutService.java#L70).
* **Resolución del Contexto Inquilino:** Extracción y aislamiento de datos a través de [TenantArgumentResolver.java](file:///C:/Users/HaroltAndr%C3%A9sG%C3%B3mezAgu/ProyectoAntigravity/ibpms-platform/backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/web/interceptor/TenantArgumentResolver.java).
* **Controlador de Emergencia y Auditoría:** Persistencia de registros críticos en [EmergencyLoginController.java](file:///C:/Users/HaroltAndr%C3%A9sG%C3%B3mezAgu/ProyectoAntigravity/ibpms-platform/backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/web/security/EmergencyLoginController.java).
