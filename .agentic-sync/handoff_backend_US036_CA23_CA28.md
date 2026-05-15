# Handoff Backend: US-036 (CA-23 al CA-28)

## 1. Metadatos
- **Iteración:** 07-DEV-DAVID
- **US:** US-036 (Identity Governance)
- **CAs:** CA-23, CA-24, CA-25, CA-26, CA-27, CA-28
- **Rama Git:** DevDavid
- **Estrategia NFR/QA:** desarrollar sobre la arquitectura en la ruta "C:\Users\USER\Desktop\Proyectos\Harold Ibpms\ibpms-platform\docs\architecture\arquitecturar.md"

### Alineación Arquitectónica
- **ADRs Consultados:** ADR-001 (Hexagonal), ADR-010 (Pirámide Pruebas).
- **Stack:** Java 17+, Spring Boot, PostgreSQL.
- **Riesgos:** Riesgo de lógica duplicada entre US-036 y US-038. Usar el servicio de Blacklist Redis compartido (US-038).

## 2. Contexto de Negocio
Se requiere finalizar los criterios de refinamiento y reporte de la US-036, asegurando la inmutabilidad de roles críticos, la generación de reportes bajo demanda, y el manejo de delegación de tareas in-flight.

## 3. Requerimientos Técnicos y Reglas de Negocio
1. **Delegación de Tareas In-Flight (CA-23):**
   - Garantizar que al consultar tareas en el Workdesk, la lógica resuelva al suplente si existe una delegación activa vigente.
   - Las tareas no completadas deben "regresar" al expirar la delegación (esto puede resolverse evaluando `CURRENT_TIMESTAMP` contra `end_date` en la lógica de resolución, sin mover data físicamente, o mediante un scheduler si se requiere un "sello visual"). *Documenta tu solución.*
2. **Reporte ISO 27001 On-Demand (CA-24):**
   - Exponer un endpoint (ej. `POST /api/v1/admin/roles/reports/iso27001`) que genere el reporte on-demand cruzando Usuarios, Roles y Procesos.
   - Calcular hash SHA-256 del contenido generado.
   - Persistir en `ibpms_audit_reports` (generado por el usuario `requested_by`). Retornar el archivo o la data estructurada.
3. **Inmutabilidad de Roles Nativos (CA-27):**
   - Interceptar cualquier mutación (PUT/DELETE) a los roles `SUPER_ADMIN` y `SYSTEM_ADMIN` en los controladores/servicios correspondientes y rechazar con `403 Forbidden` si se intentan modificar sus permisos de menú.
4. **Endpoint Dinámico de Menú (CA-28 / CA-31):**
   - Asegurar que el endpoint `GET /api/v1/users/me/menu-layout` entregue los 7 módulos macro según la intersección de roles del usuario autenticado (incluyendo herencia si aplica).
5. **Acatar Coordinación con US-038 (CA-25):**
   - No reinventar infraestructura JWT/Redis. Utiliza la ya existente.

## 4. Criterios de Aceptación a Cubrir
- **CA-23:** Heredación de tareas y retorno al finalizar delegación.
- **CA-24:** Reporte ISO 27001 persistido con hash.
- **CA-25:** Integración coherente con US-038.
- **CA-27:** Protección de mutación para `SUPER_ADMIN`.
- **CA-28:** Topología de 7 módulos Macro.

## 5. Dependencias y Bloqueantes
- Requiere tabla `ibpms_audit_reports` creada por Infra.

## 6. Validaciones y Entregables
- **Compilación obligatoria:** Ejecuta el protocolo Zero-Trust SRE documentado en `.agents/skills/backend_sre_compilation_audit/SKILL.md` (§0 a §2). Si hay bloqueos de infraestructura, aplica el protocolo de reporte 3B.

> ⚠️ **IMPORTANTE:** Todo desarrollo o configuración debe quedar exhaustivamente documentado. Se exige **PRECISIÓN QUIRÚRGICA** en cada cambio para evitar efectos colaterales en funcionalidades existentes. Se han detectado regresiones en iteraciones previas; cualquier daño a funcionalidades adyacentes será motivo de rechazo inmediato. Debe existir obligatoriamente documentación Javadoc sobre la solución propuesta y referenciando el CA respectivo. 

**INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_backend.md`.
4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_backend.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.

> 📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
> - Aplica estrictamente el protocolo de pruebas **TDD** documentado en `.agents/skills/tdd_first/SKILL.md` (Red -> Green -> Refactor) antes de hacer lógica.
> - Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md` (naming, error handling, modularidad).
