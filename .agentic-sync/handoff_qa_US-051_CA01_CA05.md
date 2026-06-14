# Handoff Técnico - QA
## 1. Metadatos del Handoff
- **Iteración:** 01-DEV-DAVID
- **Épica:** E — Seguridad, RBAC, Identidad & Configuración Global
- **User Story:** US-051: Matriz de Gobernanza Visual y Enrutamiento RBAC (Frontend)
- **Criterios de Aceptación:** CA-01, CA-02, CA-03, CA-04, CA-05
- **Rama Git:** DevDavid

## 2. Contexto de Pruebas
Validar el sistema de enrutamiento y gobernanza visual del Frontend (Vue Router) mediante pruebas E2E (Playwright) o Component Testing, garantizando el comportamiento frente a refrescos de página, carga de vistas, expiración de tokens e intentos de acceso no autorizado.

## 3. Escenarios a Validar (Gherkin Referencia)
- **CA-01:** Navegar a una ruta protegida y presionar F5 (Recargar página). Verificar que el sistema no expulsa al usuario mientras exista un token válido en LocalStorage.
- **CA-02:** Verificar visualmente que al cargar una vista asíncrona, el Skeleton de carga solo se aplica sobre el área de contenido (Router View), manteniendo visibles y funcionales el Header y Sidebar.
- **CA-03 & CA-04:** Forzar el acceso a una URL donde el usuario no tiene permisos (pero sí tiene sesión). Comprobar que la UI muestra la pantalla de "404 No Encontrado" y NO un error 403, y que la URL original se mantiene en la barra de direcciones. Comprobar que el LocalStorage sigue intacto.
- **CA-04:** Modificar el token en LocalStorage para simular expiración. Intentar navegar. Comprobar expulsión a `/login` y purgado de LocalStorage.
- **CA-05:** Acceder a una ruta configurada como pública sin token en LocalStorage. Comprobar que el sistema lo permite sin redirecciones.

## 4. NFR / QA Strategy
- Desarrollar sobre la arquitectura detallada en: `docs/architecture/arquitecturar.md`.
- Aplica el skill `.agents/skills/qa_e2e_validation_audit/SKILL.md` para garantizar la Ley de Correspondencia Gherkin (Test vs User Story). Todo CA sin test correspondiente debe reportarse como Cobertura Faltante.

## 5. Instrucciones Operativas y de Comunicación
1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_QA.md`.
4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_QA.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.

> 📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
> - Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md`.
