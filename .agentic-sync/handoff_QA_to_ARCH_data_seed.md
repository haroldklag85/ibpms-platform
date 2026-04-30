# Handoff: QA a Arquitecto Líder - Error de Data Seed en UAT (J-04)

**De:** QA Lead Senior
**Para:** Arquitecto Líder
**Contexto:** Bloqueo en Certificación Manual E2E (Misión 0) - Sprint 6.2

## 🛑 Descripción del Incidente (P0)
Durante la inicialización del entorno E2E (`docker-compose.e2e.yml`) para la certificación del Journey J-04, se encontró que el **Data Seed de usuarios y roles no se está aplicando correctamente** en la base de datos `ibpms_e2e` (PostgreSQL).

Se consultó la tabla `ibpms_security_user` y sólo existen los usuarios: `admin`, `analista` y `root`. Faltan perfiles vitales requeridos para que las tareas enrutadas en Camunda puedan ser gestionadas desde el Workdesk unificado.

## 📋 Requerimiento de Datos (Modelo E/R & Seed)

Por favor, crea o ajusta los scripts de poblado SQL (Liquibase o Flyway) en un nuevo changeset para el entorno E2E o UAT. Se necesitan **exclusivamente** los siguientes usuarios con el `password_hash` por defecto para pruebas (`Test123!` o el equivalente que esté en la plataforma):

| Username      | Email                     | Rol (ibpms_security_role) | Is_Active | Descripción / Notas UAT |
|---------------|---------------------------|---------------------------|-----------|-------------------------|
| `analista_n1` | `analista_n1@alpha.com`   | Operario (Adapters)       | `true`    | *Nota: Actualmente existe como "analista", se requiere corregir el username a "analista_n1" para que cuadre con el BPMN/casos de uso.* |
| `perito_a`    | `perito_a@alpha.com`      | Operario                  | `true`    | Actor en J-04 para inspección de siniestros. |
| `perito_b`    | `perito_b@alpha.com`      | Operario                  | `true`    | Actor secundario para validación concurrente. |
| `director_1`  | `director_1@alpha.com`    | Supervisor                | `true`    | Autorizador final en escalamientos. |

### Consideraciones Adicionales para el Arquitecto:
1. Asegurar que las relaciones existan debidamente en la tabla intersección `ibpms_security_user_roles`.
2. Habilitar la integración de estas identidades hacia Camunda (`act_id_user` y `act_id_membership`) en caso de que IdentityProvider esté segregado temporalmente, debido a que el motor asignará tareas a estos candidates.

**Por favor, responde este handoff indicando el archivo `.sql` creado/modificado para levantar nuevamente nuestro Docker y continuar con la Misión 0.**
