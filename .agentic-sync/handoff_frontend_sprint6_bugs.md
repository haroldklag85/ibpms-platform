# Handoff: Estabilización Frontend Zero-Mock (Sprint 6.2)

## 1. Contexto y Objetivos
**US / BUG:** BUG-S6-003 (Zod Validation Timeouts), BUG-S6-005 (AssigneeMultiSelect)
**Rama de Trabajo:** `sprint-6`
**Objetivo:** Cumplir la directiva Zero-Mock conectando componentes transversales a la API real, y corregir fallos silenciosos de UI (Zod Validations) que bloquean los tests Playwright.

## 2. Alineación Arquitectónica
- **ADR-002 (Vue 3 Microfrontends):** Toda integración de estado asíncrono debe gestionarse vía Pinia Stores.
- Zero-Mock estricto: El archivo `mockAdapter.ts` está desactivado. Todo endpoint asume backend vivo.

## 3. Requerimientos Funcionales y Técnicos
- **AssigneeMultiSelect.vue:** Debe consultar a Pinia (`useUserStore` o `useRbacStore`) para obtener los responsables elegibles, quien a su vez debe llamar a `GET /api/v1/users`.
- **Zod Validations (Botones de Pánico US-039):** Los tests en Playwright asumen que el botón reacciona, pero la UI está fallando silenciosamente. Se requiere exponer el error visualmente mediante Toasts u otro mecanismo para que Playwright registre el comportamiento correcto.

## 4. Tareas a Ejecutar
1. **Conexión Zero-Mock (AssigneeMultiSelect):**
   - Eliminar el arreglo quemado (`mockDirectory` / `mockUsers`).
   - Mapear la data asíncrona real hacia las `<v-select>` o inputs equivalentes.
2. **Exposición de Zod Errors:**
   - Ubicar los botones o vistas referenciados en US-039 (Botones de pánico, restauración de drafts).
   - Asegurar que cualquier fallo de esquema Zod se procese y despache como un Toast global (o error en línea) visible en pantalla.

## 5. Criterios de Aceptación
- [ ] `AssigneeMultiSelect.vue` ya no tiene constantes `mock`.
- [ ] Los errores Zod en componentes de seguridad/Pánico emiten Toasts visibles.

## 6. Instrucciones Operativas y de Compilación
> **INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
> 1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
> 2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
> 3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_frontend.md`.
> 4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_frontend.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
> 5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.

*Build obligatorio:* Ejecuta el protocolo Zero-Trust UI documentado en `.agents/skills/frontend_build_audit/SKILL.md`.
> 📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
> - Aplica estrictamente el protocolo de pruebas **TDD** documentado en `.agents/skills/tdd_first/SKILL.md` (Red -> Green -> Refactor) antes de hacer lógica.
> - Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md` (naming, error handling, modularidad).
