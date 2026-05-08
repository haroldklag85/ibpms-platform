# Resolución de Conflictos Git: sprint-6 ← DevDavid

## Contexto

Se está realizando un merge de la rama `DevDavid` hacia `sprint-6` (estamos en `DevDavid`). Se detectaron **79 archivos** con estado `unmerged`, distribuidos en las siguientes categorías.

## Inventario de Conflictos (clasificados por prioridad)

### Grupo 1 — Backend Core (Seguridad & Servicios) — ALTA PRIORIDAD
> [!IMPORTANT]
> Estos archivos son críticos: afectan las US-036 (RBAC ✅), US-038 (Multi-Rol ✅), US-048 (IdP ✅) que ya están completadas.

| # | Archivo | Conflictos | US Relacionada |
|---|---------|-----------|----------------|
| 1 | `SecurityConfig.java` | 1 | US-036, US-038 |
| 2 | `JwtAuthFilter.java` | 4 | US-036 |
| 3 | `CorrelationIdFilter.java` | 2 | US-036 |
| 4 | `RoleService.java` | ? | US-036 |
| 5 | `UserService.java` | ? | US-038, US-048 |
| 6 | `RoleEntity.java` | ? | US-036 |
| 7 | `RoleRepository.java` | ? | US-036 |
| 8 | `Application.java` | ? | Infraestructura |
| 9 | `AuthDebugController.java` | ? | Debug/Dev |

### Grupo 2 — Backend Controllers (Web Layer) — MEDIA-ALTA
| # | Archivo | Conflictos | US Relacionada |
|---|---------|-----------|----------------|
| 10 | `AgileTaskController.java` | 9 | US-030 |
| 11 | `WorkboxTaskController.java` | 8 | US-001 |
| 12 | `BpmnDesignController.java` | 4 | US-005 |
| 13 | `AgileProjectController.java` | ? | US-030 |
| 14 | `AgileProjectClosureController.java` | ? | US-030 |
| 15 | `AnalyticsController.java` | 1 | US-009 |
| 16 | `DmnGeneratorController.java` | 1 | US-007 |
| 17 | `DmnGovernanceController.java` | 1 | US-007 |
| 18 | `DmnSimulatorController.java` | 1 | US-007 |
| 19 | `FormCertificationController.java` | 1 | US-017 |
| 20 | `GlobalExceptionHandler.java` | 1 | Transversal |
| 21 | `AuditReportController.java` | 2 | US-036 |
| 22 | `AuthSyncController.java` | 2 | US-036, US-038 |
| 23 | `SecurityStreamController.java` | 3 | US-036 |
| 24 | `FeatureToggleController.java` | 2 | Config |
| 25 | `KanbanStateController.java` | 2 | US-008 |
| 26 | `TaskExecutionController.java` | 2 | US-017 |
| 27 | `TaskSkipController.java` | 2 | US-017 |

### Grupo 3 — Backend Services & Adapters
| # | Archivo | US Relacionada |
|---|---------|----------------|
| 28 | `AgileTaskService.java` | US-030 |
| 29 | `MenuLayoutUseCase.java` | US-025, US-051 |
| 30 | `CamundaBpmnValidationAdapter.java` | US-005 |

### Grupo 4 — Backend Resources & DB
| # | Archivo | US Relacionada |
|---|---------|----------------|
| 31 | `db.changelog-master.yaml` | Infraestructura (Liquibase) |

### Grupo 5 — Frontend Core (Stores, Router, Services) — ALTA PRIORIDAD
| # | Archivo | Conflictos | US Relacionada |
|---|---------|-----------|----------------|
| 32 | `authStore.ts` | 6 | US-036, US-038, US-051 |
| 33 | `rbacStore.js` | ? | US-036 |
| 34 | `kanbanStore.ts` | ? | US-008 |
| 35 | `useWorkdeskStore.ts` | ? | US-001 |
| 36 | `router/index.ts` | ? | US-051 |
| 37 | `apiClient.ts` | ? | Transversal |
| 38 | `App.vue` | 2 | Transversal |

### Grupo 6 — Frontend Views & Components
| # | Archivo | Conflictos | US Relacionada |
|---|---------|-----------|----------------|
| 39 | `IdentityGovernance.vue` | 16 | US-036 ✅ |
| 40 | `GlobalRolesTable.vue` | 2 | US-036 ✅ |
| 41 | `KanbanView.vue` | 8 | US-008 |
| 42 | `MainLayout.vue` | 2 | US-025, US-051 |
| 43 | `KanbanColumn.vue` | ? | US-008 |
| 44 | `SudoModal.vue` | ? | US-036 |
| 45 | `ConnectionToast.vue` | ? | US-017 |
| 46 | `EvidenceDropzone.vue` | ? | US-039 |
| 47 | `GenericFormBody.vue` | ? | US-039 |
| 48 | `MetadataGrid.vue` | ? | US-039 |

### Grupo 7 — Frontend Tests
| # | Archivo | US Relacionada |
|---|---------|----------------|
| 49-58 | `*.spec.ts` (10 archivos) | Varios |

### Grupo 8 — Frontend E2E Tests
| # | Archivo | US Relacionada |
|---|---------|----------------|
| 59-64 | `e2e/*.spec.ts` + `helpers/auth.ts` | Varios |

### Grupo 9 — Configuración & Docs
| # | Archivo | US Relacionada |
|---|---------|----------------|
| 65 | `docker-compose.yml` | Infraestructura |
| 66 | `playwright.config.ts` | QA Config |
| 67 | `epic_E_seguridad_identidad_config.md` | US-036 SSOT |
| 68 | `us036_functional_analysis.md` | US-036 Auditoría |

### Grupo 10 — Agentic-Sync & Test Data (Baja prioridad)
| # | Archivo | Descripción |
|---|---------|-------------|
| 69-73 | `.agentic-sync/approval_*.md` | Documentos de aprobación |
| 74 | `.agentic-sync/qa_report_ARQ005.md` | Reporte QA |
| 75 | `test_results.txt` | Datos de prueba |

### Grupo 11 — Backend Tests (.bak)
| # | Archivo | Descripción |
|---|---------|-------------|
| 76-85 | `java.bak/*.java` (archivos de test backup) | Tests respaldo |

---

## Estrategia de Resolución

### Principio Rector
- **HEAD = DevDavid** (rama actual, contiene el trabajo más reciente de US-036 CA-17 a CA-22, Identity Governance avanzada)
- **sprint-6** (rama incoming, contiene trabajo paralelo de otros US)

### Reglas de Decisión
1. **Para US completadas (US-036, US-038, US-048, US-043)**: Priorizar la versión HEAD (DevDavid) ya que contiene el código certificado más reciente.
2. **Para US en construcción compartidas**: Combinar ambos cambios si no se contradicen, priorizando la completitud funcional.
3. **Para archivos de configuración (SecurityConfig, docker-compose)**: Combinar ambas versiones asegurando que todos los endpoints y servicios están cubiertos.
4. **Para tests**: Conservar todos los tests de ambas ramas.
5. **Para docs SSOT**: Priorizar la versión más completa y actualizada.

### Enfoque por Grupo
Para maximizar eficiencia, se usará `git checkout --theirs` o `--ours` donde sea apropiable, y resolución manual donde haya contenido que deba combinarse.

> [!WARNING]
> No se modificará ninguna lógica de negocio. Solo se resuelven marcadores de conflicto eligiendo la versión correcta según el SSOT.

## Verificación
- Tras resolver todos los conflictos: `git diff --check` para confirmar cero marcadores.
- Verificar que no queden archivos en estado unmerged.
