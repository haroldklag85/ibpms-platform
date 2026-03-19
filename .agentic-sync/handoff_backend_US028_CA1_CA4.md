# Contrato de Arquitectura Backend (Iteración 14 - US-028: Auto-Generador QA Vitest)

**Rol:** Desarrollador Backend Java/Spring Boot.
**Objetivo:** Auditar infraestructura pasiva para soportar los mocks generados por la UI.

## 📋 Contexto y Criterios de Aceptación (Alcance Estricto):
En esta Iteración 14 abordamos los 4 escenarios base (CA-1 al CA-4) de la **US-028 (Auto-Generación de Test Suites Zod/Vitest)**. Esta característica es "Shift-Left QA", lo que significa que el Frontend generará código de pruebas Unitarias de manera estática en el navegador sin intermediación Server-Side.

Por consiguiente, tu carga técnica es Nula/Transparente. Las operaciones de Descarga Blob y Mocks de red vivirán 100% en el ecosistema Vite/Vue3.

## 📐 Reglas de Desarrollo:
1. No modifiques endpoints funcionales ni destruyas los validadores tipo `StrictPrimitiveTyping`. 
2. Únicamente desplázate al controlador principal (`FormDesignController.java` o el más afín) e inyecta un comentario JavaDoc a nivel de Clase indicando explícitamente que los Endpoints de Mapeo asíncrono deben seguir siendo consumibles/mockeables por las pruebas BDD que auto-genera el Frontend. Esto asegurará la trazabilidad técnica en futuras iteraciones.

## 🛑 MANDATO LOCAL GATEKEEPER (REGLA DE ORO)
Tienes **ESTRICTAMENTE PROHIBIDO** hacer `git commit` a la rama principal.
En cuanto finalices la revisión preventiva, empaqueta los cambios (el JavaDoc):
`git stash save "temp-backend-US028-ca1-ca4"`

Informa textualmente al Arquitecto Líder apenas termine el guardado.
