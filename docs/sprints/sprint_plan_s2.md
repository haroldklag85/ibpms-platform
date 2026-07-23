# Sprint 2 — "Cumbre de la Pirámide" (E2E & UAT Automatizado)

> **Sprint:** 2
> **Estrategia:** Alternativa B (Shift-Left Testing) - Fase Superior
> **Prerequisito:** Gate Sprint 1 aprobado (Capa de APIs y Controladores 100% blindada con pruebas unitarias y cubierta verde en Jacoco).
> **Objetivo Único:** Ensamblar y certificar la UI mediante Playwright. Garantía plena de usabilidad para las 11 Historias de Usuario desarrolladas.

---

## Roles y Gobernanza (Code Freeze)

- **Jefe de Equipo (Harolt):** Emite el *Human Sign-Off* sobre la funcionalidad si Playwright arroja fallos que son considerados "aceptables" o "falsos positivos de UI".
- **Agente QA:** Actor principal. Escribe e ingesta a Playwright todos los scripts de prueba originados de los escenarios UAT en `docs/uat/`.
- **Arquitecto Líder:** Audita el patrón PO (Page Object Model) implementado por el Agente QA para evitar fragilidad en los selectores.
- **Agentes Ejecutores:** Correcciones exclusivas de UI (Vue).

---

## Ejecución por Journeys (E2E Track)

### 1. Journey Operativo y de Identidad
Validación enfocada fuertemente en el portal del empleado y los administradores.
- **US-001 y US-002 (Workdesk + Claiming):** Simular operador real ingresando, viendo Tareas compartidas con su `Team Scope`, haciendo clic en "Atender" y verificando que la vista reactiva y del compañero se bloquea (Stomp/Ghost Deletion).
- **US-036, US-038 y US-048 (Securización RBAC):** Simulación de múltiples sesiones (Cookies separadas) probando Aislamiento de Pantallas. Administrador otorga rol VIP; Empleado intenta forzar URL protegida y recibe 404 (Gaslighting) o 403.

### 2. Journey Creativo (Desarrolladores Locales)
Validando que los constructores de herramientas internas del ERP funcionen.
- **US-005 (BPMN Modeler):** Agente QA dibuja 3 cajas BPMN vía DOM (Drag & Drop), guarda modelo, prueba parseo XML.
- **US-003, US-028, US-039 (IDE de Formularios):** Arrastrar un input de texto, un select paramétrico y previsualizar generación automática Zod/Reactiva.

---

## Criterios de Aceptación (Gate de Firma UAT)

1. El pipeline de Playwright Report corre con cero (0) dependencias de mocks a nievel externo; usa el backend dockerizado nativo.
2. Todo Bug descubierto cae formalmente a la matriz del RTM en `uat_rtm_matrix.md`, catalogado si es problema de estado local, CSS, o latencia de eventos asíncronos.
3. Se alcanza un `Passing Rate` funcional de negocio verde, dando fin estricto a las 11 Historias Base de la Fase V1.
