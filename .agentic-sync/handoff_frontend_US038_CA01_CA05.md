# Handoff Arquitectónico: Frontend
**Iteración:** 01-DEV-038-DAVID
**Épica:** 13 — Seguridad/RBAC (US-038)
**Criterios de Aceptación:** CA-01 al CA-05
**Rama de Trabajo:** DevDavid

## 1. Contexto de Negocio
El Frontend debe soportar los flujos de seguridad avanzados (US-038): reaccionar a bloqueos de mutación cuando Redis está caído (CA-01), inyectar formularios para perfiles JIT incompletos (CA-03) y proveer interfaces para emergencias (Break-Glass CA-04).

## 2. Alineación Arquitectónica
- **ADR-002 (Vue 3 Microfrontends):** Toda la gestión de estado de autenticación y permisos debe residir en Pinia (`authStore`). Los interceptores HTTP de Axios manejarán el código de respuesta `428`.

## 3. Requisitos Técnicos y Entregables (Frontend)

**A. Manejo de Fail-Open (CA-01):**
- Cuando el Backend responda con un 403 Forbidden específico por "Redis Fail-Open" en peticiones POST/PUT/DELETE, el interceptor de Axios debe renderizar una Alerta UI global indicando "Operación Denegada: Sistema en Degradación Segura" en lugar del clásico error genérico.

**B. Modal Incompletitud JIT (CA-03):**
- Si el login / sincronización SSO retorna HTTP `428 Precondition Required`, el Frontend debe atraparlo y en lugar de redirigir a `/login`, renderizará un Modal bloqueante de `[Completar Perfil Local]` solicitando los campos faltantes especificados en el payload (Ej: Sucursal, Código Jefe).
- Deberá existir una función en `authStore` para enviar este payload al backend y completar el inicio de sesión.

**C. Formulario Break-Glass (CA-04):**
- Crear o integrar una vista `BreakGlassLogin.vue` o habilitar una funcionalidad en el Login para contingencias (Ej. cuando EntraID falle). 

**D. RBAC Simple Aditivo (CA-05):**
- Validar que componentes como `IdentityGovernance.vue` rendericen adecuadamente el multi-select de roles para un mismo usuario, reflejando la fusión aditiva de permisos sin errores visuales.

## 4. Criterios de Aceptación a Validar
- CA-01, CA-03, CA-04, CA-05 soportados integralmente por Vue.js y Pinia.

## 5. Instrucciones de Compilación y NFR
> ⚠️ **IMPORTANTE:** Todo desarrollo o configuración debe quedar exhaustivamente documentado. Se exige **PRECISIÓN QUIRÚRGICA** en cada cambio para evitar efectos colaterales en funcionalidades existentes. Se han detectado regresiones en iteraciones previas; cualquier daño a funcionalidades adyacentes será motivo de rechazo inmediato.

> **Build obligatorio:** Ejecuta el protocolo Zero-Trust UI documentado en `.agents/skills/frontend_build_audit/SKILL.md`.

> **NFR/QA Strategy:** Desarrollar sobre la arquitectura en la ruta `docs\architecture\arquitecturar.md`. Importante: Documentar las soluciones propuestas y buenas prácticas.

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
