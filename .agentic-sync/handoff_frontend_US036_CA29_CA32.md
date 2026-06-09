# 📄 Handoff de Arquitectura: Frontend
> **US:** US-036 | **CAs:** CA-29 al CA-32 | **Iteración:** 08-DEV-DAVID

## 1. Metadatos de la Delegación
- **Rol Destino:** Agente Frontend
- **Objetivo:** Implementar UI limpia en Modal de Roles y consumir endpoint dinámico de Menú.
- **Alineación Arquitectónica:**
  - **ADR-002 (Vue 3 + Vite):** Componentes reactivos, separación estricta en Pinia stores (`useMenuStore`).
  - **Anti-JWT Bloat:** Prohibición estricta de leer el menú desde JWT; consumo del nuevo endpoint `/api/v1/users/me/menu-layout`.

## 2. Contexto de Negocio
Actualmente, la asignación de accesos de menú por rol en la Pantalla 14 puede ser caótica visualmente y está propensa a desincronizaciones si solo depende del JWT.
Necesitamos un diseño limpio del Modal (Tabs) para separar "Información Básica" de "Topología de Menús".
Además, el sidebar maestro de la aplicación (Workdesk/Dashboard) debe consumir el endpoint del Backend, almacenarlo 1 vez por sesión en Pinia, y tener auto-curación si el Backend responde 403 (revocación en caliente).

## 3. Criterios de Aceptación
- **CA-29 (Diseño Limpio del Modal de Roles):** El modal de creación/edición de un rol (Pantalla 14) debe dividirse en 2 pestañas ("Tab 1: Información Básica" y "Tab 2: Topología de Menús").
- **CA-30 (Superposición Inclusiva Multirrol):** Visualizar el menú unificado devuelto por el Backend sin problemas.
- **CA-31 (Arquitectura Endpoint Dinámico):** El menú izquierdo (Sidebar) debe renderizarse iterando exclusivamente la respuesta obtenida del `GET /api/v1/users/me/menu-layout`.
- **CA-32 (Caché Híbrida y Auto-Curación):**
  - Pinia (`useMenuStore`) guarda este layout solo 1 vez post-login.
  - Interceptor Axios global: Si cualquier endpoint devuelve 403, limpiar caché de Pinia (`useMenuStore.$reset()`) y mostrar un Toast "Sus accesos han sido actualizados por el Administrador".

## 4. Directrices Técnicas y Arquitectónicas
- Modificar `IdentityGovernance.vue` o su Modal asociado para usar Tabs (ej. v-tabs de Vuetify, si se usa, o implementación nativa Tailwind).
- Modificar el store `useMenuStore.ts` para ejecutar Axios GET y guardar el array de módulos.
- Actualizar `Sidebar.vue` (o Layout asociado) para leer del Store en lugar de hardcodear.
- Integrar la validación 403 en `apiClient.ts` (Interceptor).
- **Build obligatorio:** Ejecuta el protocolo Zero-Trust UI documentado en `.agents/skills/frontend_build_audit/SKILL.md`.

## 5. Estructura de Archivos Esperada
- `src/stores/menuStore.ts` (o similar)
- `src/services/apiClient.ts`
- `src/components/layout/Sidebar.vue` (o equivalente)
- `src/views/admin/Security/IdentityGovernance.vue`

## 6. Instrucciones Operativas y de Comunicación
> ⚠️ **IMPORTANTE:** Todo desarrollo o configuración debe quedar exhaustivamente documentado. Se exige **PRECISIÓN QUIRÚRGICA** en cada cambio para evitar efectos colaterales en funcionalidades existentes. Se han detectado regresiones en iteraciones previas; cualquier daño a funcionalidades adyacentes será motivo de rechazo inmediato.

> **INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
> 1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
> 2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
> 3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_frontend.md`.
> 4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_frontend.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
> 5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.

> 📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
> - Aplica estrictamente el protocolo de pruebas **TDD** documentado en `.agents/skills/tdd_first/SKILL.md` (Red -> Green -> Refactor) antes de hacer lógica.
> - Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md` (naming, error handling, modularidad).
