# Contrato de Arquitectura Backend (US-003 Iteración 11: CA-51 al CA-54)

**Rol:** Desarrollador Backend Java/Spring Boot.
**Objetivo:** Proveer validación de seguridad para campos sensibles (Passwords) e inyectar marcadores de bloqueo en estructuras de datos repetibles (Grillas).

## 📋 Contexto y Criterios de Aceptación (Alcance Estricto):
En esta iteración 11 nos enfocamos en refinar interacciones de UI, pero el backend debe respaldarlas:

*   **CA-51 (Bloqueo Parcial de Grillas):** Cuando el endpoint devuelva `prefillData` para un campo de tipo `field_array` (Grilla), asegúrate de que el DTO o el JSON soporte atributos de metadatos a nivel de fila, como `_locked: true` o `_owner: "user_x"`. Si bien la regla dura se verá en V2, el backend V1 debe permitir que estos metadatos viajen sin ser borrados por serializadores estrictos.
*   **CA-53 (Campos Sensibles / Passwords):** El Frontend enviará contraseñas en texto plano por HTTPS (campo tipo `password`). Asegúrate de que las bitácoras o interceptores genéricos de logs (si existen) *enmascaren* cualquier variable cuyo nombre contenga `password`, `pwd` o `secret` antes de imprimir el request payload en consola. 

## 📐 Reglas de Desarrollo:
1. Revisa tu `TaskCompleteRequest.java` o el motor Jackson para asegurar que los List/Array de objetos dinámicos admiten llaves técnicas internas (Ej. `_locked`).
2. Agrega una simple comprobación de enmascaramiento de logs si tienes configurado `CommonsRequestLoggingFilter` o análogos.

## 🛑 MANDATO LOCAL GATEKEEPER (REGLA DE ORO)
Tienes **ESTRICTAMENTE PROHIBIDO** hacer `git commit` a la rama principal.
En cuanto finalices el análisis y/o adaptación Backend, empaqueta los cambios:
`git stash save "temp-backend-US003-ca51-ca54"`

Informa textualmente al Arquitecto Líder apenas termine el guardado.
