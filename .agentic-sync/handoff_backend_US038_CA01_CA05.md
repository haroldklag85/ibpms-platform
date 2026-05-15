# Handoff Arquitectónico: Backend
**Iteración:** 01-DEV-038-DAVID
**Épica:** 13 — Seguridad/RBAC (US-038)
**Criterios de Aceptación:** CA-01 al CA-05
**Rama de Trabajo:** DevDavid

## 1. Contexto de Negocio
La US-038 implementa tolerancia a fallos en JWT, previene saturación de cabeceras, maneja aprovisionamiento Just-In-Time (JIT) y establece un protocolo Break-Glass para emergencias. Es fundamental cerrar las brechas de seguridad (GAPs) identificadas en auditorías forenses previas.

## 2. Alineación Arquitectónica
- **Exclusión V2:** Cualquier referencia a funcionalidades futuras se excluye.
- **ADR-001:** La lógica de seguridad (`JwtAuthFilter`, `AuthSyncController`) se considera Infraestructura y debe operar estrictamente sobre la capa Spring Security, usando Redis y JWT. 

## 3. Requisitos Técnicos y Entregables (Backend)

**A. Tolerancia a Fallos (Redis Fail-Open) (CA-01):**
- Modificar `JwtAuthFilter` (aprox línea 63-78): Si la consulta a Redis falla (Timeout/SPOF), atrapar la excepción y permitir métodos `GET` (Fail-Open Degradado). Rechazar estricta y automáticamente métodos `POST/PUT/DELETE/PATCH` (Fail-Closed). 

**B. Anti-Token Bloat (CA-02):**
- Modificar la ingesta de Claims de EntraID (`JwtAuthFilter` o Provider): Filtrar para que solo los roles que inicien con `ibpms_rol_` sean inyectados en la sesión de Spring Security. 

**C. Aprovisionamiento JIT (CA-03):**
- Validar "Claims Mínimos Vitales" (Ej. `Sucursal_ID` o `Codigo_Jefe`) en el controller/service de Sincronización EntraID. Si están incompletos, lanzar un error HTTP `428 Precondition Required` con un payload JSON detallando qué campos faltan.

**D. Protocolo Break-Glass (CA-04):**
- Implementar el Endpoint de emergencia `/api/v1/auth/emergency-login` (o similar), asegurando que al restablecer EntraID, se dispare el cierre de ciclo (forzar invalidación post-contingencia rotando credenciales o alertando al Tablero de Anomalías).

**E. RBAC Simple Aditivo (CA-05):**
- Asegurar que la colección de GrantedAuthorities (Spring Security) suma los permisos de múltiples roles (Allow-Overrides) correctamente, lo cual suele ser nativo en Spring si se mapean adecuadamente.

## 4. Criterios de Aceptación a Validar
- CA-01 a CA-05 completamente implementados en la capa backend, con cobertura TDD.

## 5. Instrucciones de Compilación y NFR
> ⚠️ **IMPORTANTE:** Todo desarrollo o configuración debe quedar exhaustivamente documentado. Se exige **PRECISIÓN QUIRÚRGICA** en cada cambio para evitar efectos colaterales en funcionalidades existentes. Se han detectado regresiones en iteraciones previas; cualquier daño a funcionalidades adyacentes será motivo de rechazo inmediato.

> **Compilación obligatoria:** Ejecuta el protocolo Zero-Trust SRE documentado en `.agents/skills/backend_sre_compilation_audit/SKILL.md` (§0 a §2). Si hay bloqueos de infraestructura, aplica el protocolo de reporte 3B.

> **NFR/QA Strategy:** Desarrollar sobre la arquitectura en la ruta `docs\architecture\arquitecturar.md`.

## 6. Instrucciones Operativas y de Comunicación
> **INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
> 1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
> 2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
> 3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_backend.md`.
> 4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_backend.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
> 5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.
> 
> 📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
> - Aplica estrictamente el protocolo de pruebas **TDD** documentado en `.agents/skills/tdd_first/SKILL.md` (Red -> Green -> Refactor) antes de hacer lógica.
> - Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md` (naming, error handling, modularidad).
