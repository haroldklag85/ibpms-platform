# Contrato de Arquitectura Backend (US-003 Iteración 7: CA-31 al CA-35)

**Rol:** Desarrollador Backend Java/Spring Boot.
**Objetivo:** Acondicionar el Backend y el Object Mapper para recibir lógicas anidadas de las Grillas dinámicas y Payloads pesados de Base64 de la UI.

## 📋 Contexto y Criterios de Aceptación (Alcance Estricto):
En esta iteración (Fase 7), el Frontend inyectará estructuras de datos sustancialmente más complejas (Arrays de Objetos) y cadenas largas por las Firmas. 

Debes abordar proactivamente la capacidad del puerto de entrada:
*   **Acondicionamiento para CA-31 (Firma Base64):** El frontend enviará cadenas de imagen Base64 crudas (ej. `data:image/png;base64,iVBOR...`) integradas dentro del Payload del JSON (`variables`). Verifica o añade configuración en `application.yml` o al DTO para que Spring Boot no trunque el request por ser un JSON muy pesado (ej. `spring.servlet.multipart.max-request-size` y los límites de Tomcat/Undertow).
*   **Soporte para CA-34 (Data Grids / Tablas):** Las grillas repetibles enviarán estructuras complejas como `List<Map<String, Object>>`. Asegúrate de que el endpoint que completa tareas (`FormDesignController` o WebController) y delega al Api de Camunda use un ObjectMapper dinámico (Jackson/Camunda Spin) capaz de serializar y guardar estructuras de Array sin ahogarse ni generar un error de parseo primitivo.

## 📐 Reglas de Desarrollo:
1. Revisa tu `application.properties` o `application.yml`.
2. Las correcciones backend en esta iteración son periféricas de performance y serialización, pero críticas.

## 🛑 MANDATO LOCAL GATEKEEPER (REGLA DE ORO)
Tienes **ESTRICTAMENTE PROHIBIDO** hacer `git commit` a la rama principal.
En cuanto realices los ajustes y valides que el servidor Spring Boot arranca sin romper, empaqueta el contenido:
`git stash save "temp-backend-US003-ca31-ca35"`

Notifica al rol humano "Arquitecto Líder" una vez finalizado el guardado.
