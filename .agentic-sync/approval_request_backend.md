# Solicitud de Revisión de Arquitectura: Sprint 5.1 (Remediación Deuda Técnica)

**Fecha/Hora:** 2026-04-18
**Agente Requirente:** Backend Agent (Antigravity)
**Estado:** PENDIENTE DE APROBACIÓN LÍDER
**Rama:** `sprint-5/iteracion4`

## Resumen del Plan de Implementación (Remediación)

De acuerdo a las directivas operativas de remediación de Cierre de Deuda Técnica (US-002, US-007, US-029), el `implementation_plan` ha sido trazado abordando las fallas de seguridad persistentes e irregularidades en bases de datos:

1. **Security & Context:** Desplazamiento total de identificadores estáticos (`"e2e_user"`, strings hardcodeados) por inyección dinámica desde el `SecurityContextAdapter` (Spring Security), mitigando un inminente IDOR en la gestión DMN de `DmnGovernanceController` y en el `/claim` transversal. Segmentación estricta en el repositorio (Cache y BD) por `tenantId`.
2. **Data Persistence & Isolation:** Supresión de Repositories "mock" (`MockEventSourcingRepository`) en el componente BFF en pro del uso del Repositorio JPA real. Incorporación del Changelog para `claim_audit_log` enfocado a auditar el `force-unclaim`.
3. **Data Loss & Sanitization:** Implementación de `PiiSanitizer` (Regex pre-LLM invocation). 
4. **Resiliency:** Consolidación de un `DmnDraftCleanupScheduler` y enmascaramiento estandarizado `ConstraintViolationException` (Zod Server-side format) a RFC 7807 en el `GlobalExceptionHandler`.

## Confirmación
No se introducirán Features nuevos. Toda modificación está enfocada 100% a estabilizar las fallas reportadas. Cuento con las tácticas de JUnit (`@WebMvcTest` y `@DataJpaTest`) mapeadas en TDD para la certificación frente al QA.

Por favor, Arquitecto Líder, verifique los detalles técnicos depositados en el `implementation_plan.md` asociado y emita el veredicto para transición a la fase de EXECUTION en el componente Backend.
