# Contrato de Arquitectura Backend (Iteración 21 | US-005: CA-27, CA-30)

**Rol:** Desarrollador Backend Java/Spring Boot.
**Objetivo:** Habilitar los recursos base (Mocks de Plantillas y Variables de Configuración) para asistir la experiencia Frontend de Modelado, sin interactuar directamente con flujos lógicos complejos.

## 📋 Contexto y Criterios de Implementación:

*(Nota: CA-26, CA-28 y CA-29 son netamente de alcance Frontend o están vetados, por lo expuesto en la capa arquitectónica).*

### Tarea 1: Proveedor de Plantillas (CA-27)
*   Crea el Endpoint `GET /api/v1/design/processes/templates`.
*   Retorna un DTO de Array. Ejemplo: `[{ id: "template_1", name: "Aprobación Simple", xml: "<?xml..." }]`.
*   Puedes hardcodear / inyectar directamente 2 cadenas XML (Borradores básicos en variables estáticas o leer archivos del classpath `src/main/resources/templates/`) simulando una respuesta real de Base de Datos.

### Tarea 2: Configurador de Umbrales (CA-30)
*   Crea el Endpoint Módulo Admin `GET /api/v1/admin/settings/bpmn-complexity-limit`.
*   Devuelve un JSON simple `{ "nodeLimit": 100 }`.
*   Este número en producción vendría de Base de Datos / Vault, por ahora puedes mockearlo en tu Servicio/Controller para que Frontend dinamize la alerta, demostrando que es "Parametrizable".

## 🛑 MANDATO LOCAL GATEKEEPER
Tienes **ESTRICTAMENTE PROHIBIDO** hacer `git commit` a principal.
Construye los puertos, lánzalos junto al BpmnDesignController y asegura en stash:
`git stash save "temp-backend-US005-ca26-ca30"`

Informa textualmente la confirmación del guardado.
