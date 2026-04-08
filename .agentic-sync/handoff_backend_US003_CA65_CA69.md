# Contrato de Arquitectura Backend (Iteración 14: Soporte QA Shift-Left)

**Rol:** Desarrollador Backend Java/Spring Boot.
**Objetivo:** Auditar la infraestructura para garantizar soporte nulo/transparente al autogenerador de Tests del Frontend.

## 📋 Contexto y Criterios de Aceptación (Alcance Estricto):
En esta Iteración 14, la carga técnica recae 100% en el Frontend (Generador de Archivos `.spec.ts` en memoria). Dado que los Criterios CA-65 al CA-67 de la US-003 se excluyeron por ser V2, estamos abarcando la **US-028 (Auto-Generación de Test Suites Zod/Vitest)**, la cual se ejecuta estáticamente en el navegador del usuario.

*   **Rol del Backend:** Ninguno estructural. El backend no necesita generar el archivo ni exponer un nuevo endpoint, puesto que el Frontend procesará el AST y descargará el Blob en cliente puro.

## 📐 Reglas de Desarrollo:
1. Revisa brevemente la consola o el Logger general para confirmar que no has roto nada en tu branch actual.
2. Escribe un simple comentario en el archivo `FormDesignController.java` o en tu interceptor indicando `// Endpoint protegido y listo para consumo MOCK por Frontend Vitest` solo para asentar tu presencia en la iteración.

## 🛑 MANDATO LOCAL GATEKEEPER (REGLA DE ORO)
Tienes **ESTRICTAMENTE PROHIBIDO** hacer `git commit` a la rama principal.
En cuanto finalices la revisión inaneórica, empaqueta los cambios:
`git add . && git commit -m "chore(sync): save progress" && git push origin HEAD`

Informa textualmente al Arquitecto Líder apenas termine el empaquetado.
