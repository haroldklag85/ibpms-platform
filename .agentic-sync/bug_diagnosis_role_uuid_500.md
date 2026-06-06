# 🩺 Diagnóstico Forense: Bug-Fix Role UUID Deserialization 500
**ID:** `bug_diagnosis_role_uuid_500`
**Fecha:** 2026-05-20
**Agente:** BUG-FIX LEAD

## Descripción del Bug
El usuario reporta que al intentar crear un nuevo rol desde la pestaña "Fábrica de Roles", el sistema arroja una "ALERTA DEL SISTEMA: NIVEL 0" antes de concluir la operación. La consola del backend muestra un error `HttpMessageNotReadableException` y `InvalidFormatException: Cannot deserialize value of type java.util.UUID from String "R_"`.

## Análisis Forense (Quadruple Check)
1. **Síntoma / Capa Probable:** Backend arrojando Error 500 por una petición malformada o incompatible enviada desde el Frontend.
2. **Archivos Sospechosos y Causas:**
   - `backend/.../entity/security/RoleEntity.java`: La entidad define su identificador primario como `private UUID id;` con una estrategia de autogeneración (`GenerationType.AUTO`).
   - `frontend/src/views/admin/Security/IdentityGovernance.vue` (líneas 1168-1175): El método `saveRole()` estaba enviando la clave `"id"` dentro del JSON del POST, y su valor por defecto en la UI para nuevos roles es `"R_"`. Jackson en el backend intenta deserializar `"R_"` en una clase `java.util.UUID`, lo que es imposible por carecer de los 36 caracteres estándar. Además, la carga útil estaba enviando el atributo frontend-only `"topology"` que también es descartado o puede generar conflictos.
3. **Validación contra SSOT:** El Frontend no debe enviar llaves primarias cuando se delega a un ORM la autogeneración en base de datos.
   
## Plan Quirúrgico
- **Frontend (`IdentityGovernance.vue`):** Modificar el método `saveRole()` para los casos de inserción (`else` branch), filtrando los atributos `id` y `topology` del payload enviado al API (`POST /admin/roles`). Solo se enviarán `name` y, si existe, `parentRole` como objeto envuelto.
- **Flujo Posterior:** Tras recibir el código 201 CREATED del Backend, se captura el UUID real generado (`res.data.id`) y se inyecta nuevamente en la UI local y en la Matriz para mantener el estado reactivo limpio.
