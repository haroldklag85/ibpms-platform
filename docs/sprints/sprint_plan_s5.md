# Sprint Plan S5: Ola de Integración y Mapeo Transaccional 🚀

## 1. Información General del Sprint
- **ID del Sprint:** Sprint 5
- **Estado Inicial:** Saneamiento Estructural Certificado (All-Green Sprint 4).
- **Objetivo Principal:** Reactivar la conectividad Camunda evitando colapsos del Daemon, y materializar la integración segura y visual entre el Engine transaccional y el Frontend mediante la ejecución formal de 115 Criterios de Aceptación (CA) meticulosamente rebanados (Slicing).

---

## 2. Parche de Infraestructura Base (Opción 1)
Debido a bloqueos operativos en el Entorno Local (Restricciones de recursos y fallos de long-polling), se ha postergado la migración de Workers hacia Node.js (US-V2-005) a favor de un Parche Intermedio:
*   **Parche Jackson (MismatchedInputException):** Se implementará la tolerancia a JSON vacío o primitivas nulas en el `ObjectMapper` que rodea al `camunda-external-task-client`.
*   **Reactivación de Worker:** Modificación de `application.yml` (`disable: false`) con un *backoff* regulado.

---

## 3. Scope Funcional del Sprint (115 Criterios de Aceptación)

Este ciclo madura funcionalidades nucleares de gobernanza de tareas e Inteligencia Operativa:
*   **US-002 (Reclamar Tarea / 28 CA):** Reglas duras de `Implicit Locking`, concurrencia múltiple de apropiación de tareas ("Robo limpio") y WebSockets reactivos.
*   **US-029 (Ejecución y Envío de Formulario / 37 CA):** Autoguardado isomórfico, sumisión segura con rechazo de estado, esquemas Zod en local y persistencia de borradores `PUT /draft`.
*   **US-007 (Generador DMN IA / 24 CA):** Creación del Engine NLP para conversión de Prompts a XML DMN. Debido al bloqueo de firmas Azure/OpenAI, este módulo correrá sobre un **Servicio Táctico Mock**.
*   **US-025 (Cards Dinámicas por Rol / 26 CA):** Frontend SPA Ciego y Obediente; App Shell UX y visualización regida por el Store de Autenticación de Roles (Pinia).

---

## 4. Estrategia de Slicing Táctico (Workflows Activos)

Rigiéndonos estrictamente por los dictámenes del `creacionIteracionesDev.md`, el Sprint 5 se entregará progresivamente en las siguientes iteraciones inamovibles:

### Iteración 1: Transaccionalidad Base y Transmisión de Errores (Fase Actual)
Aseguramiento de Persistencia y Gobernanza: Parche Jackson, `SELECT FOR UPDATE SKIP LOCKED` (Prevención Deadlock US-002) y rechazos preventivos `HTTP 403` para intentos furtivos de Form Submit (Zero-Trust). 
*Equipos FRONT/QA en Paralelo: Preparan Testcontainers y Mocks API de Axios.*

### Iteración 2: Integración (APIs y Eventos de Negocio)
Pasarelas lógicas para eventos de WebSockets (Ampliación y desaparición de tareas en el Workdesk), el Adaptador de Parsers DMN, y la ingesta de JSON M2M JWT.

### Iteración 3: Interfaz Client-Side (Vue / Pinia)
Levantamiento de Vistas 100% Mockeadas localmente usando el ecosistema Vitest para asegurar rendimiento libre de fallos DOM. Virtual Scrolling y Skeleton Loaders para Soportar la usabilidad de Clientes (Roles Visuales).

### Iteración 4: Blindaje y Certificación Playwright
Sellado UAT. Los 115 CA entran encadenados a través de flujos E2E de Playwright sobre contenedores reales aislados. Aseguramiento de que la rama alcanza calidad óptima.

---

## 5. Condiciones de Cierre (Gate)
Todo PR o unificación de la Iteración 4 debe acatar explícitamente los protocolos `reconciliacionCoberturaCa.md` y `router_certificacion_qa.md`. Se exige compilación "All-Green" total.
