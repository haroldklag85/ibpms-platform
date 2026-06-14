# Handoff Técnico - Frontend
## 1. Metadatos del Handoff
- **Iteración:** 01-DEV-DAVID
- **Épica:** E — Seguridad, RBAC, Identidad & Configuración Global
- **User Story:** US-051: Matriz de Gobernanza Visual y Enrutamiento RBAC (Frontend)
- **Criterios de Aceptación:** CA-01, CA-02, CA-03, CA-04, CA-05
- **Rama Git:** DevDavid
- **Exclusiones:** V2

### Alineación Arquitectónica
- **ADRs Consultados:** 
  - ADR-002: Vue 3 + Vite para Frontend. Uso estricto de Composition API y Script Setup.
  - El diseño respeta el documento de arquitectura base (`docs/architecture/arquitecturar.md`).
- **Stack Confirmado:** Vue 3, Pinia, Vue Router, TailwindCSS.
- **Riesgos:** Fugas visuales (FOUC) o bloqueos en recargas (F5). Mitigado por hidratación asíncrona síncrona en el router y manejo granular de vistas.

## 2. Contexto de Negocio
Implementar los Guards y la gobernanza visual del Vue Router para la plataforma iBPMS, garantizando que no existan parpadeos visuales por carga asíncrona de roles, que no se expongan rutas confidenciales (Gaslighting Cibernético) y que el sistema sea resiliente a refrescos de pantalla preservando el token activo en LocalStorage.

## 3. Especificaciones Técnicas (Frontend)
- **Componentes Vue a tocar:** `MainLayout.vue` (o el App Shell que envuelva el `<RouterView>`), componente de Skeleton (Ej: `AppSkeleton.vue`), y el componente `NotFound404.vue`.
- **Rutas (Vue Router):** `router/index.ts` o los archivos de guards. Se debe implementar `router.beforeResolve` para inyectar la carga síncrona de estado.
- **Estado Global (Pinia):** Modificar o utilizar el store de autenticación (`useAuthStore` o similar) para proveer un método `hydrateAuth()` que lea el JWT del LocalStorage de forma asíncrona bloqueante.
- **Detalles por CA:**
  - **CA-01:** Implementar en el Router Guard una espera explícita a la recuperación de sesión (`await hydrateAuth()`).
  - **CA-02:** El Skeleton Loader no debe tapar el Sidebar ni Header, solo el `<RouterView>`.
  - **CA-03:** Si hay Token pero no hay Rol (Acceso Denegado), en lugar de redirigir a `/`, reemplazar el componente actual enrutando internamente o mostrando `NotFound404.vue` sin alterar la URL.
  - **CA-04:** Si el Token expiró (401), purgar LocalStorage y redirigir a `/login`. Si es un "Hyperlink Viejo" sin permiso, conservar LocalStorage y mostrar falso 404.
  - **CA-05:** Agregar `meta: { isPublic: true }` a rutas como login, recuperación o documentación (Swagger/Storybook) para saltar el Guard.

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
