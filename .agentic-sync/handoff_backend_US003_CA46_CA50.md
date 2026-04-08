# Contrato de Arquitectura Backend (US-003 Iteración 10: CA-46 al CA-50)

**Rol:** Desarrollador Backend Java/Spring Boot.
**Objetivo:** Adaptar el backend para absorber Metadatos de Inputs Ocultos y garantizar tipados numéricos limpios tras el proceso de des-enmascaramiento del Frontend.

## 📋 Contexto y Criterios de Aceptación (Alcance Estricto):
En esta Iteración 10, la atención se centra mayoritariamente en la capa visual, pero requiero tu asistencia en las protecciones periféricas:

*   **CA-47 (Campos Ocultos para Metadata):** El JSON de variables emitido hacia `/complete` comenzará a traer campos de sistema o llaves de metadatos (Ej: `sys_origin`, `auth_reference`). Asegúrate de que el DTO `TaskCompleteRequest` o tu ObjectMapper no tenga activado estúpidamente `FAIL_ON_UNKNOWN_PROPERTIES` que provoque un 400 Bad Request al encontrarse llaves que el formulario envió de forma "Secreta" u oculta.
*   **CA-50 (Traducción Silenciosa de Formatos):** El Frontend limpiará las máscaras de pesos/moneda (Ej. "$ 15.000" -> `15000`). Tú, como backend Zero-Trust, debes garantizar que la API es estrictamente tipada: Si Camunda espera un Entero o un Double para la variable, el endpoint debe validar con `@Valid` y su Constraint Validator respectivo que la data entrante no sea inyectada como un String malicioso. 

## 📐 Reglas de Desarrollo:
1. Revisa tu `TaskCompleteRequest.java` o el interceptor Jackson para asegurar que admite dinámicas de campos sueltos.
2. Reafirma las anotaciones `@NotNull` o `Double/Integer` en tus entidades transaccionales.

## 🛑 MANDATO LOCAL GATEKEEPER (REGLA DE ORO)
Tienes **ESTRICTAMENTE PROHIBIDO** hacer `git commit` a la rama principal.
En cuanto realices los saneamientos y asegures la compilación, archívalos limpiamente:
`git add . && git commit -m "chore(sync): save progress" && git push origin HEAD`

Informa textualemente al Arquitecto Líder apenas termine el guardado.
