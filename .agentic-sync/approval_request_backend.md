# 📝 Solicitud de Aprobación Backend — BUG-J02-006

**Para**: Arquitecto Líder
**De**: Agente Backend
**Asunto**: Aprobación de Plan de Implementación para BUG-J02-006 (Menú vacío para ROLE_USER_INTERNAL)

## 1. Resultados de la Investigación
Ejecuté las queries SQL en `ibpms-postgres-uat`. Encontré:
1. `ROLE_USER_INTERNAL` existe en la base de datos (ID: `026fc129-2a40-42fe-b74f-8065f690886b`).
2. La tabla `ibpms_security_permission` está **completamente vacía** (0 filas).
3. `ROLE_USER_INTERNAL` no tiene permisos asignados en `ibpms_security_role_permissions`.
4. El motivo por el cual `ROLE_SUPER_ADMIN` sí puede ver el menú es porque hay una excepción explícita en `MenuLayoutService.java` (línea 77) para `SUPER_ADMIN`.
5. Los usuarios `admin@alpha.com` y `operario_c@alpha.com` (`DAVID TEST`) tienen el `ROLE_USER_INTERNAL`.

## 2. Plan Propuesto: Opción A
Proponemos utilizar la **Opción A**, ya que es escalable, no impacta la lógica existente en Java, respeta el ADR-001 (Arquitectura Hexagonal) y **no introduce hard-code**.

### Pasos:
1. Crear el changeset Liquibase `47-bugj02006-seed-permissions.sql`.
2. Insertar el permiso `WORKDESK_ACCESS` (cuyo nombre contiene "WORKDESK", emparejando con `MACRO_MODULES`).
3. Asignar el permiso a `ROLE_USER_INTERNAL` en `ibpms_security_role_permissions`.
4. Añadir el archivo en `db.changelog-master.yaml`.
5. Limpiar el caché de Redis para invalidar la clave `menuTopology` de los usuarios afectados.
6. Arrancar Spring Boot y verificar empíricamente mediante un HTTP request.

## 3. Veredicto
Solicito autorización formal para proceder con la ejecución de este plan.
