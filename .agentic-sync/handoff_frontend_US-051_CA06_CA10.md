# Handoff Técnico - Frontend
## 1. Metadatos del Handoff
- **Iteración:** 02-DEV-DAVID
- **Épica:** E — Seguridad, RBAC, Identidad & Configuración Global
- **User Story:** US-051: Matriz de Gobernanza Visual y Enrutamiento RBAC (Frontend)
- **Criterios de Aceptación:** CA-06, CA-07, CA-08, CA-09, CA-10
- **Rama Git:** DevDavid
- **Exclusiones:** V2

### Alineación Arquitectónica
- **ADRs Consultados:** 
  - ADR-002: Vue 3 + Vite para Frontend. (Composición dinámica y Pinia).
  - El diseño respeta el documento de arquitectura base (`docs/architecture/arquitecturar.md`).
- **Stack Confirmado:** Vue 3, Pinia, Axios.
- **Riesgos:** Sobrecarga del DOM o inyección insegura de componentes. Todo debe controlarse a través de variables reactivas en Pinia y directivas estándar (`v-if` / `v-show` y `component :is`).

## 2. Contexto de Negocio
Completar la segunda fase de la Gobernanza Visual del Frontend. En esta iteración se implementa la construcción dinámica del menú lateral a partir de los datos que envía el Backend, la composición de componentes en el Dashboard principal según el rol, y la protección de UI para vistas de solo lectura y operaciones destructivas (Sudo-Mode y telemetría de secretos).

## 3. Especificaciones Técnicas (Frontend)
- **Componentes Vue a tocar:** `Sidebar.vue` (o el menú lateral), `Dashboard.vue` (o `Workdesk.vue`), Modal de Sudo-Mode (nuevo o existente), y componentes que expongan tokens (DevPortal/API Keys).
- **Estado Global (Pinia):** El store de autenticación/menú debe almacenar en caché el árbol de navegación.
- **Detalles por CA:**
  - **CA-06:** Consumir el endpoint de menú asíncrono (creado en US-036) para construir el Sidebar. Aplicar Auto-Collapse: si una categoría padre queda sin hijos autorizados, no debe renderizarse en el DOM. Guardar en Pinia para no relanzar la petición al cambiar de vista.
  - **CA-07:** En la ruta `/` (Workdesk), usar Component Composition (`<component :is="...">`) en lugar de redireccionar a múltiples dashboards. Inyectar los widgets autorizados basados en Pinia.
  - **CA-08:** Utilizar directivas condicionales (ej. `v-if="hasWritePermission"`) para eliminar físicamente botones destructivos (`[+ Nuevo]`, `[Eliminar]`) cuando el usuario solo tiene permisos de lectura (Read-Only) en una vista transversal.
  - **CA-09:** Interceptar las peticiones POST/DELETE críticas. Suspender la ejecución en el Frontend para renderizar un modal de `Sudo Mode` que exija re-autenticación. Solo si el modal devuelve éxito (promesa resuelta), disparar la petición HTTP al Backend.
  - **CA-10:** En la vista de Secretos/API Keys, el campo debe iniciar ofuscado (`*****`). Al pulsar el botón "Mostrar", el Frontend DEBE disparar primero un POST asíncrono de telemetría de auditoría al Backend y luego revelar visualmente el texto.

## 4. NFR / QA Strategy
- Desarrollar sobre la arquitectura detallada en: `docs/architecture/arquitecturar.md`.

## 5. Instrucciones Operativas y de Comunicación
1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_Frontend.md`.
4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_Frontend.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.

> 📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
> - Aplica estrictamente el protocolo de pruebas **TDD** documentado en `.agents/skills/tdd_first/SKILL.md` (Red -> Green -> Refactor) antes de hacer lógica.
> - Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md` (naming, error handling, modularidad).

## 6. Compilación Obligatoria
Build obligatorio: Ejecuta el protocolo Zero-Trust UI documentado en `.agents/skills/frontend_build_audit/SKILL.md`.
