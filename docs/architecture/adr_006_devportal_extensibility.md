# ADR-006: Arquitectura de Extensibilidad Interna y Súper Módulos (DevPortal)

**Status:** Propuesto (Para Agentes FullStack)
**Date:** 2026-03-07
**Context:** US-042 (DevPortal - Extensibilidad Interna)
**Autor:** Lead Software Architect

## 1. Contexto y Problema (El "Shift")
Originalmente se proyectaba el DevPortal como una API Pública B2B. El Product Owner ha aclarado el rumbo: El DevPortal (Pantalla 13) es una herramienta de **Extensibilidad Interna**.
Permitirá a nuestro propio equipo (humanos y Agentes de Código) crear interfaces complejas que sobrepasan las capacidades del constructor BPMN genérico (Pantalla 6), denominadas "Súper Módulos".

El reto es orquestar este desarrollo paralelo ("Custom Code") en la plataforma iBPMS sin violentar el aislamiento, la mantenibilidad ni las reglas de negocio del framework **Hexagonal**.

## 2. Decisión Arquitectónica 1: Sandboxing Frontend (Composición)
¿Cómo se orquestará el despliegue de estos Súper Módulos Custom en una UI unificada?

**Veredicto:** *IFrames Aislados (V1) que evolucionarán a Web Components / Module Federation (V2).*

*   **V1 Táctica (IFrame Sandboxing):** Para prevenir colisiones de dependencias (Ej: Un módulo usa React 18 y el Core Vue 3) y envenenamiento del DOM/CSS Global, el Súper Módulo será desarrollado y desplegado como un portal estático independiente en su propio Bucket/Azure Storage. La aplicación iBPMS Core cargará este módulo dinámicamente usando una ruta inyectada y la etiqueta `<iframe>`.
*   **Aislamiento de Seguridad:** El iframe operará bajo estrictas reglas de `sandbox` (No parent-domain manipulation).
*   **Comunicación Event-Driven (Cross-Document Messaging):** El Súper Módulo y el Shell Principal (iBPMS) se comunicarán exclusivamente a través del API nativa del navegador `window.postMessage()`. Ninguno comparte estado o store de Pinia/Redux en memoria.

## 3. Decisión Arquitectónica 2: Autenticación y Consumo Hexagonal
¿Cómo validará Spring Boot los JWT Tokens emitidos por el DevPortal para diferenciar el "Core Access" del "Extensibility Access"?

Como regla del Hexágono Interno: **NINGÚN Súper Módulo puede inyectar repositorios JPA ni tocar la base de datos de los expedientes.** Solo consumen REST APIs (Driving Adapters).

**Veredicto:** *Scopes y Audiencias Personalizados (JWT Custom Claims).*

1.  **Emisión Distintiva:** Al registrar el Súper Módulo en el DevPortal, Microsoft Entra ID generará credenciales OIDC (Client Credentials Type). El JWT resultante tendrá en su Payload Claims dedicados, por ejemplo:
    *   `aud` (Audience): `ibpms.extensibility.supermodules`
    *   `roles` / `scp` (Scope): `module:xyz:read`, `module:xyz:execute`
2.  **Spring Boot Security Filter:** El SecurityFilterChain de Spring Boot interceptará todo tráfico web y verificará explícitamente el "Audience" y los Scopes delegados.
3.  **Contexto Restringido (Tenant-Scope):** Una llamada con un Access Token de "Extensibility" estará sometida al filtro `@PreAuthorize` de Spring. Si un Súper Módulo de RR.HH. intenta consumir la API REST del módulo Legal, será rechazado en la capa HTTP. La ceguera debe ser estricta.

## 4. Consecuencias (Guardrails para los Agentes)
Esta política requiere que los Agentes de Desarrollo entiendan que:
*   **Prohibición de "Puertas Traseras":** Un Súper Módulo no puede requerir que el Agente Backend modifique entidades JPA ni servicios del Core. El Módulo debe usar las APIs públicas internas transaccionales (`/api/v1/expedientes`, `/api/v1/tareas`) como cualquier otro cliente.
*   **Plebiscito de Diseño:** Si el Agente necesita un nuevo Endpoint porque las APIs existentes no proveen un dato, debe solicitar que el Equipo de Arquitectura diseñe el nuevo Puerto de Entrada en el Hexágono (Driving Adapter). El endpoint jamás lo construye el desarrollador del Súper Módulo.
