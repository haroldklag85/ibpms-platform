---
name: "Handoff Backend - US-007 (Modo Manual DMN) CA-26 a CA-32"
role: "Backend"
---

# 1. Metadatos y SSOT (Single Source of Truth)
- **Iteración/Sprint:** Sprint-6
- **Rama de trabajo:** sprint-6
- **User Story:** US-007 (Generador Cognitivo de DMN)
- **Criterios de Aceptación (CAs) a desarrollar:** CA-30, CA-32 (Backend focus)
- **Path del SSOT:** `docs/requirements/epics/epic_B_formularios_bpmn.md`
- **Flujo de Trabajo:** Backend -> Frontend -> QA

# 2. Alineación Arquitectónica y ADRs (Architecture Decision Records)
- **Validación de ADRs:**
  - `adr-001-hexagonal-architecture.md`: La lógica de negocio para la trazabilidad y versionado manual debe vivir en el dominio (UseCases), y el controlador sólo actúa como adaptador.
  - `adr_009_postgresql_pgvector_migration.md`: Las modificaciones en base de datos ya fueron delegadas al Agente Infra/BD. Tu responsabilidad es actualizar la Entidad JPA (`DmnModelEntity`) para mapear la nueva columna `is_manual` que el equipo de Infra proveerá.
- **Lineamientos Transversales:** Se garantiza el *Tenant Isolation* verificando el TenantId en cada operación PUT, y se preserva el inmutable historial de auditoría mediante `ibpms_audit_log` para registrar el badge "Modificada Manualmente" (CA-32).

# 3. Rutas Exactas y Contexto Preexistente
- **Entidad JPA:** `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/jpa/entity/dmn/DmnModelEntity.java`
- **Controlador:** `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/web/dmn/DmnGovernanceController.java`
- **Use Case:** `backend/ibpms-core/src/main/java/com/ibpms/poc/application/usecase/dmn/DmnGovernanceUseCase.java`
- **Entidad (si existe):** `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/jpa/entity/dmn/DmnModelEntity.java`

# 4. Snippets Prescriptivos (El "Qué" y el "Cómo")

**1. Entidad JPA (`DmnModelEntity`):**
Actualiza la entidad añadiendo el campo `isManual` para mapear la columna creada por el Agente Infra/BD:
```java
    @Column(name = "is_manual", nullable = false)
    private Boolean isManual = false;

    // Generar Getters y Setters...
```

**2. Controlador (`DmnGovernanceController`):**
Actualiza el payload del `PUT /{id}` para recibir el flag de modificación manual.
```java
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_PROCESS_ARCHITECT')")
    public ResponseEntity<?> overrideDmnDraft(@PathVariable String id, @RequestBody Map<String, Object> payload) {
        String invokerTenant = SecurityContextUtils.getTenantId();
        
        String incomingXml = (String) payload.get("xmlContent");
        boolean isManual = payload.containsKey("isManual") ? (Boolean) payload.get("isManual") : false;

        var result = dmnGovernanceUseCase.updateDmnContent(id, incomingXml, invokerTenant, isManual);
        return ResponseEntity.ok(result);
    }
```

**3. Use Case (`DmnGovernanceUseCase`):**
Añade el parámetro `isManual` y la lógica de versionado (CA-32):
```java
    @Transactional
    public DmnModelEntity updateDmnContent(String dmnId, String newXml, String invokerTenantId, boolean isManual) {
        DmnModelEntity dmn = dmnRepository.findById(dmnId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "DMN Model not found"));

        if (!dmn.getTenantId().equals(invokerTenantId)) {
            // log BOLA
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso Cruzado BOLA detectado.");
        }

        if ("SEALED".equals(dmn.getStatus())) {
            // CA-32: Incrementará a V2 obligatoriamente (Si ya está sealed, en V1 se asume que se rechaza, pero CA-32 y CA-18 dicen que se genera V2).
            // Para el MVP V1, si la lógica actual prohíbe editar SEALED, se debe adaptar para permitir clonar o rechazar.
            // Implementa el seteo del flag is_manual:
        }

        dmn.setXmlContent(newXml);
        if (isManual) {
            dmn.setIsManual(true);
            log.info("[AUDIT] DMN {} modificada manualmente. Perdió pureza IA.", dmnId);
            // Insertar en ibpms_audit_log
        }
        return dmnRepository.save(dmn);
    }
```
*(Nota: Actualiza la entidad `DmnModelEntity` añadiendo el campo `isManual` con sus Getters/Setters).*

# 5. Matriz de QA y Testing Atómico
Sección dirigida a QA:
- Script de Pruebas: `backend/ibpms-core/src/test/java/com/ibpms/poc/application/usecase/dmn/DmnGovernanceUseCaseTest.java`

| Test Name | CA Evaluado | Aserción Esperada |
| --- | --- | --- |
| `testUpdateDmnContent_WhenManualEdit_ShouldSetIsManualAndLog` | CA-32 | Verifica que al mandar `isManual=true`, la entidad se guarda con `isManual=true` y se hace el log correspondiente de auditoría. |

# 6. Mensaje de Despacho (Comunicación al Agente Especialista)

> **INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
> 1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
> 2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
> 3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_Backend.md`.
> 4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_Backend.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
> 5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.
>
> 📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
> - Aplica estrictamente el protocolo de pruebas **TDD** documentado en `.agents/skills/tdd_first/SKILL.md` (Red -> Green -> Refactor) antes de hacer lógica.
> - Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md` (naming, error handling, modularidad).
> 
> "Compilación obligatoria: Ejecuta el protocolo Zero-Trust SRE documentado en `.agents/skills/backend_sre_compilation_audit/SKILL.md` (§0 a §2). Si hay bloqueos de infraestructura, aplica el protocolo de reporte 3B."
