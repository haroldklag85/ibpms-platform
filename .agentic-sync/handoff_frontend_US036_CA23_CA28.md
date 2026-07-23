# Handoff Frontend: US-036 (CA-23 al CA-28)

## 1. Metadatos
- **Iteración:** 07-DEV-DAVID
- **US:** US-036 (Identity Governance)
- **CAs:** CA-23, CA-24, CA-26, CA-27, CA-28
- **Rama Git:** DevDavid
- **Estrategia NFR/QA:** desarrollar sobre la arquitectura en la ruta "C:\Users\USER\Desktop\Proyectos\Harold Ibpms\ibpms-platform\docs\architecture\arquitecturar.md"

### Alineación Arquitectónica
- **ADRs Consultados:** ADR-002 (Vue 3 Microfrontends).
- **Stack:** Vue 3, Pinia, Axios.
- **Riesgos:** FOUC y experiencia de usuario rota.

## 2. Contexto de Negocio
Adaptación de la Interfaz (Pantalla 14 y ruteador global) para gobernar topologías de menú, aplicar UX Fallbacks, emitir reportes ISO 27001 bajo demanda y prohibir la alteración de roles core.

## 3. Requerimientos Técnicos y de Componentes
1. **Generación de Reporte ISO 27001 (CA-24):**
   - En Pantalla 14, agregar botón `[Generar Reporte Matrizal]`.
   - Consumir el endpoint backend on-demand y forzar descarga del archivo CSV/Excel.
2. **UX Fallback por falta de menús (CA-26):**
   - Si el endpoint `GET /api/v1/users/me/menu-layout` retorna un menú vacío o carece de módulos macro permitidos, no dejar en estado bloqueado; enrutar hacia un componente Dashboard/Bienvenida neutral.
3. **Bloqueo UI de Roles Nativos (CA-27):**
   - Si el rol renderizado en el modal es `SUPER_ADMIN` o `SYSTEM_ADMIN`, bloquear visualmente (`disabled` / `readonly`) los checkboxes de módulos, previniendo mutaciones.
4. **Topología Macro (CA-28):**
   - Validar que los permisos inyectados manejen los 7 Módulos Macro principales (Workdesk, Service Delivery, BAM, Modeler, Integración, Proyectos, Administración).

## 4. Criterios de Aceptación a Cubrir
- **CA-24:** UI de Reportes bajo demanda.
- **CA-26:** Ruteo a página neutral si no hay permisos.
- **CA-27:** Interfaz inmutable para roles de sistema.
- **CA-28:** Presentación de los 7 módulos.

## 5. Dependencias y Bloqueantes
- Requiere endpoints provistos por el Backend.

## 6. Validaciones y Entregables
- **Build obligatorio:** Ejecuta el protocolo Zero-Trust UI documentado en `.agents/skills/frontend_build_audit/SKILL.md`.

> ⚠️ **IMPORTANTE:** Todo desarrollo o configuración debe quedar exhaustivamente documentado. Se exige **PRECISIÓN QUIRÚRGICA** en cada cambio para evitar efectos colaterales en funcionalidades existentes. Se han detectado regresiones en iteraciones previas; cualquier daño a funcionalidades adyacentes será motivo de rechazo inmediato. Debes documentar la solución y comentar en el código el CA y US que estás resolviendo.

**INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_frontend.md`.
4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_frontend.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.

> 📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
> - Aplica estrictamente el protocolo de pruebas **TDD** documentado en `.agents/skills/tdd_first/SKILL.md` (Red -> Green -> Refactor) antes de hacer lógica.
> - Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md` (naming, error handling, modularidad).
