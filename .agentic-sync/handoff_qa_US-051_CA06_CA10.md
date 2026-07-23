# Handoff Técnico - QA
## 1. Metadatos del Handoff
- **Iteración:** 02-DEV-DAVID
- **Épica:** E — Seguridad, RBAC, Identidad & Configuración Global
- **User Story:** US-051: Matriz de Gobernanza Visual y Enrutamiento RBAC (Frontend)
- **Criterios de Aceptación:** CA-06, CA-07, CA-08, CA-09, CA-10
- **Rama Git:** DevDavid

## 2. Contexto de Pruebas
Validar el comportamiento avanzado de la UI del iBPMS según los roles inyectados: armado dinámico de menú, visualización de dashboards basados en componentes, eliminación de botones para perfiles de solo lectura, comportamiento de Sudo Mode (re-autenticación) y disparo de auditoría visual.

## 3. Escenarios a Validar (Gherkin Referencia)
- **CA-06:** Simular un usuario con accesos restringidos. Verificar que el Menú Lateral no contiene carpetas padre vacías ni opciones bloqueadas.
- **CA-07:** Acceder a la ruta raíz `/` (Workdesk) con dos usuarios diferentes. Verificar que la URL se mantiene idéntica, pero el contenido y widgets varían según los permisos de cada usuario.
- **CA-08:** Ingresar a una vista donde el usuario solo tiene permisos de Lectura (Read-Only). Verificar a nivel de DOM que los botones de modificar/borrar no existen (no deben estar ocultos por CSS, deben estar ausentes por `v-if`).
- **CA-09:** Ejecutar una acción destructiva (ej. Borrar). Verificar que se abre un Modal exigiendo re-autenticación y que la petición POST original al backend no se ejecuta hasta que el Modal se resuelva exitosamente.
- **CA-10:** Acceder a la vista de API Keys. Verificar que las credenciales cargan ofuscadas. Presionar "Mostrar". Comprobar en la pestaña "Network" del navegador que el Frontend emite un POST de auditoría *antes* o *al mismo tiempo* de revelar visualmente la clave.

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
