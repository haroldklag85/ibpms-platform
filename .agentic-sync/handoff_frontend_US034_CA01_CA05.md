# Handoff Arquitectónico: Frontend
**Iteración:** 01-DEV-034-DAVID
**Épica:** 12 — Integraciones (US-034)
**Criterios de Aceptación:** CA-01 al CA-05
**Rama de Trabajo:** DevDavid

## 1. Contexto de Negocio
La US-034 busca implementar herramientas de orquestación asíncrona y resiliencia. En el lado del Frontend (CA-2 y por derivación de CA-8), se requiere la creación de un "Dashboard Técnico de DLQ (Dead Letter Queue)" para que el Administrador IT visualice la cantidad de mensajes atascados y accione reintentos masivos o purgas. Aunque la US se centra en infraestructura, esta vista visual es fundamental para el monitoreo.

## 2. Alineación Arquitectónica
- **ADR-002 (Vue 3 Microfrontends):** El dashboard de DLQ debe integrarse dentro de las pantallas custom de Administración del iBPMS (Vue 3 + Pinia) y NO depender de iframes hacia RabbitMQ Management UI.
- **Stack Aprobado:** Vue 3, Pinia, Axios, TailwindCSS.

## 3. Requisitos Técnicos y Entregables (Frontend)

**A. Componente Dashboard DLQ (CA-2):**
- Crear o actualizar la vista `AdminDlqDashboard.vue` (o equivalente) dentro de la sección de Configuración/Administración.
- El Dashboard consumirá el endpoint `GET /api/v1/admin/queues/dlq/summary` para listar los mensajes muertos.
- Proveer dos botones principales: `[Purgar Cola]` y `[Reintentar Mensajes Forzosamente]`.
- Al usar estos botones, se deben consumir los endpoints `POST /api/v1/admin/queues/dlq/retry` y `DELETE /api/v1/admin/queues/dlq/purge`.
- Debe haber confirmaciones de seguridad (Sudo-Mode UI o Modales de Advertencia) previas a la invocación de `PURGE` o `RETRY`.

## 4. Criterios de Aceptación a Validar
- CA-2: Dashboard Visual de DLQ operativo y consumiendo los endpoints del Backend.

## 5. Instrucciones de Compilación y NFR
> ⚠️ **IMPORTANTE:** Todo desarrollo o configuración debe quedar exhaustivamente documentado. Se exige **PRECISIÓN QUIRÚRGICA** en cada cambio para evitar efectos colaterales en funcionalidades existentes. Se han detectado regresiones en iteraciones previas; cualquier daño a funcionalidades adyacentes será motivo de rechazo inmediato.

> **Build obligatorio:** Ejecuta el protocolo Zero-Trust UI documentado en `.agents/skills/frontend_build_audit/SKILL.md`.

> **NFR/QA Strategy:** Desarrollar sobre la arquitectura en la ruta "docs/architecture/arquitecturar.md". Confirmar que la UI respeta el estado de error, loading, y confirmaciones visuales.

## 6. Instrucciones Operativas y de Comunicación
> **INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
> 1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
> 2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
> 3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_frontend.md`.
> 4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_frontend.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
> 5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.
> 
> 📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
> - Aplica estrictamente el protocolo de pruebas **TDD** documentado en `.agents/skills/tdd_first/SKILL.md` (Red -> Green -> Refactor) antes de hacer lógica.
> - Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md` (naming, error handling, modularidad).
