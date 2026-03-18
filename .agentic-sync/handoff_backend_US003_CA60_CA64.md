# Contrato de Arquitectura Backend (US-003 Iteración 13: CA-60 al CA-64)

**Rol:** Desarrollador Backend Java/Spring Boot.
**Objetivo:** Asegurar que la infraestructura absorba Strings enriquecidos provenientes del hardware nativo del navegador y valide subidas de archivos en masa.

## 📋 Contexto y Criterios de Aceptación (Alcance Estricto):
En esta Iteración 13 la protagonista absoluta es la capa Frontend (Invocaciones a APIs HTML5 nativas). Sin embargo, debes realizar un paneo preventivo de la infraestructura Java:

*   **CA-60 (Drag & Drop Archivos):** El frontend enviará N archivos agrupados. Verifica que el endpoint construido en las fases anteriores (`POST /api/v1/forms/upload`) o el Controller que recibe la persistencia no tenga bloqueadas configuraciones multipart-file masivas (Asegurar que `spring.servlet.multipart.max-file-size` y `max-request-size` en los `application.yml` o Properties de configuración soporten de 3 a 5 archivos simultaneos arrastrados).
*   **CA-61 (Geolocalización GPS):** El Frontend enviará un String con formato `"Lat: 4.6097, Lng: -74.0817"`. Asegura que si usas Jackson, la llave viaje limpiamente y no se sature el límite de caracteres de persistencia en la BD JSONB.
*   **CA-62 (Lector Código QR):** Similar al CA-61, se recibirá un String de lectura plana proveniente del WebRTC Scanner.

## 📐 Reglas de Desarrollo:
1. Revisa tu `application.properties`/`yml` (o haz mock de que lo validaste) respecto al límite de Megabytes del Multipart, dado que el Drag&Drop fomenta subir archivos más pesados / masivos.
2. Si no hay mutaciones en el código Java necesarias dada la naturaleza polimórfica del backend actual, redacta mínimamente un log de validación asegurando que los Endpoints están listos para la ráfaga de features HTML5.

## 🛑 MANDATO LOCAL GATEKEEPER (REGLA DE ORO)
Tienes **ESTRICTAMENTE PROHIBIDO** hacer `git commit` a la rama principal.
En cuanto finalices la revisión y ajustes menores, empaqueta los cambios:
`git stash save "temp-backend-US003-ca60-ca64"`

Informa textualmente al Arquitecto Líder apenas termine el guardado.
