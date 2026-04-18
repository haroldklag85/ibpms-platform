# Sprint Plan S4: Saneamiento Arquitectónico y Cierre de GAPs 🛠️

## 1. Información General del Sprint
- **ID del Sprint:** Sprint 4 (Technical Debt & Profilaxis)
- **Estado Inicial:** Code Freeze Inicial y Saneamiento.
- **Objetivo Principal:** Erradicar violaciones arquitectónicas estructurales heredadas, depurar el modelo de dominio Hexagonal (ADR-001) y asegurar perimetralmente las Historias de Usuario inmaduras a través de técnicas de *Architectural Fencing*. No se entregarán nuevas funcionalidades del negocio, asegurando 100% de estabilización.

---

## 2. Acciones Completadas y Certificadas (Baseline del Sprint)

Las siguientes tareas de profilaxis han sido **ejecutadas y cerradas con éxito** a través de la delegación de los Handoffs de Profilaxis Estructural GAPs (`handoff_backend_gap_profilaxis.md` y `handoff_frontend_gap_profilaxis.md`), garantizando que la deuda transaccional quede estática:

### 2.1 Backend (Fencing & Purga) ✅
* **Destrucción de Stubs Inseguros (US-035 / US-011):** 
  * Se inyectó `throw new UnsupportedOperationException` con alertas de GAP en todos los métodos huecos de `SharePointAdapterService.java` (uploadMassiveFileStream, createFolder, etc.) y en el bloque extra-funcional de `MailboxPollingCron.java`.
* **Aislamiento REST de Controladores (US-045):**
  * `AllowedDomainAdminController.java` protegido mediante `@Operation(hidden=true)` en OpenAPI y lanzamiento absoluto de `ResponseStatusException(501 NOT IMPLEMENTED)`.
* **Purga de Dominio Duplicado (Hallazgo Crítico SAC):**
  * Eliminación física y erradicación total de `com.ibpms.poc.domain.model.SacMailbox.java`. El motor ahora fluye unitariamente sobre el paquete sano `com.ibpms.core.sac.domain.SacMailbox`.

### 2.2 Frontend (Protección de Interfaz) ✅
* **Cercado del Sidebar (US-021 / US-045):**
  * Aplicación estricta de la Master Layout Policy Regla 14. Las rutas y archivos `.vue` de las Pantallas 11 (Hub Integraciones) y 15-A (Restricciones PMO) se conservaron intactas, pero sus puntos de entrada en el Sidebar fueron comentados conservadoramente con HTML (`<!-- v-if oculto -->`), previniendo accesos huérfanos.

---

## 3. Scope Activo del Sprint (En Ejecución)

El remanente técnico de este ciclo se enfocará exclusivamente en corregir vulnerabilidades severas en el cumplimiento del Estándar Hexagonal:

### Tarea de Remediación Profunda: `US-017` - CQRS Event Sourcing (Violación ADR-001)
* **Contexto del GAP:** La auditoría descubrió que `FormEvent.java`, situado en `domain/model/`, está infestado de anotaciones JPA (`@Entity`, `@Table`, `@Column`, `JdbcTypeCode`). Esto viola textualmente el dictamen Fundacional ADR-001: *"Capa domain/ sin dependencias JPA, Spring, Camunda"*.
* **Entregables Backend (Acción Correctiva OBLIGATORIA):**
  1. Despojar a `com.ibpms.poc.domain.model.FormEvent.java` de toda anotación foránea. Restaurarlo como un **POJO puro** regido por constructores nativos.
  2. Construir la entidad de infraestructura `com.ibpms.poc.infrastructure.jpa.entity.FormEventEntity.java` incorporando las llaves JPA y JSONB (incluyendo persistencias SQL específicas de dialecto).
  3. Crear una capa adaptadora (Mapper In/Out) en el `FormEventRepositoryJpa.java` para hidratar la Entidad a Dominio y viceversa.
  4. Certificar que el compilador y los Sagas Camunda del Workdesk sigan fluyendo perfectamente bajo este nuevo paradigma puro.

## 4. Condiciones de Cierre (Gate)
Este Sprint será clasificado como finalizado únicamente cuando el escáner de código AST sobre la capa `/domain/` reporte `0` dependencias de importación provenientes de `jakarta.persistence.*`, cerrando definitivamente todas las fugas detectadas.

---
*Documento estructurado por Arquitectura.*
