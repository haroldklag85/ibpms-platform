# 📋 Roadmap de Sprints — iBPMS V1

> **Modelo:** Propuesta C — Dual Track con Orquestación Multi-Agente (ACP)  
> **Velocidad:** 4 días/sprint | 7-10 US/sprint (objetivo post S3)  
> **Última actualización:** 2026-04-10  
> **Autor:** Arquitecto Líder SW

---

## Visión General

```
S0 (Foundation) → S1 (J-04 Core) → S2 (J-02 CQRS) → S3 (Consolidation)
                                                            │
                                                            ▼
                                            S4 (J-01 Intake IA) → S5 (Portal B2C)
                                                                        │
                                                                        ▼
                                                            S6 (IA/Gantt/DMN) → S7 (Docs)
                                                                                    │
                                                                                    ▼
                                                                            S8 (V1 RC)
```

---

## Sprint Index

| Sprint | Título | Journey Target | US Scope | Estado | Plan |
|:------:|--------|:--------------:|:--------:|:------:|------|
| **S0** | Infraestructura Agéntica + E2E | — | 0 (setup) | ⬜ Pendiente | [sprint_plan_s0.md](./sprint_plan_s0.md) |
| **S1** | Core Thread (J-04) + Validación | J-04 | US-001r, US-002, US-029 | ⬜ Pendiente | [sprint_plan_s1.md](./sprint_plan_s1.md) |
| **S2** | CQRS/DMN (J-02) + Validación | J-02 | US-017, US-007 | ⬜ Pendiente | [sprint_plan_s2.md](./sprint_plan_s2.md) |
| **S3** | Consolidación + Expansión | J-03, J-06, J-07 | US-004, US-008, US-030 | ⬜ Pendiente | [sprint_plan_s3.md](./sprint_plan_s3.md) |
| **S4** | Intake IA (J-01 pasos 1-6) | J-01 parcial, J-08 | US-037, US-016, US-012..014, US-040, US-022, US-023 | ⬜ Pendiente | [sprint_plan_s4_plus.md](./sprint_plan_s4_plus.md) |
| **S5** | Portal B2C + DevPortal | J-01 completo, J-05, J-09 | US-049, US-050, US-026, US-042, US-033, US-046 | ⬜ Pendiente | [sprint_plan_s4_plus.md](./sprint_plan_s4_plus.md) |
| **S6** | Agentes IA + Gantt + DMN | J-10, J-11, J-12 | US-052, US-053, US-032, US-044, US-031, US-006, US-007, US-027 | ⬜ Pendiente | [sprint_plan_s4_plus.md](./sprint_plan_s4_plus.md) |
| **S7** | Cierre Funciones + Docs | J-13, extensiones | US-010, US-035, US-011, US-025, US-041, US-045, US-021 | ⬜ Pendiente | [sprint_plan_s4_plus.md](./sprint_plan_s4_plus.md) |
| **S8** | Hardening + V1 RC | Regresión 13/13 | Deuda técnica + NFR | ⬜ Pendiente | [sprint_plan_s4_plus.md](./sprint_plan_s4_plus.md) |

---

## Hitos Clave

| Hito | Sprint | Día Acumulado | Evidencia |
|------|:------:|:-------------:|-----------|
| 🚀 ACP operativo + Smoke E2E | S0 | 4 | Docker + Playwright + smoke spec verde |
| 📍 J-04 funcional (MVP operario) | S1 | 8 | Operario ve → reclama → completa tarea |
| 📍 J-02 funcional (MVP diseñador) | S2 | 12 | Arquitecto BPM modela → despliega → ejecuta |
| 🔒 Seguridad J-03 validada | S3 | 16 | RBAC E2E + Gaslighting 404 + IDOR prevention |
| 🤖 Pipeline IA J-01 operativo | S4 | 20 | Correo → clasificación IA → caso creado |
| 🌐 Portal B2C lanzado | S5 | 24 | Cliente consulta estado por Magic Link |
| 🧠 Agentes IA en BPMN | S6 | 28 | Tarea cognitiva ejecutada dentro de proceso |
| 📄 Feature complete | S7 | 32 | 50/53 US implementadas |
| ✅ **V1 Release Candidate** | S8 | 36 | ≥50 E2E specs + 0 P0 bugs + NFRs met |

---

## Cobertura de Journeys por Sprint

| Journey | Criticidad | Sprint Build | Sprint E2E | Status |
|---------|:----------:|:------------:|:----------:|:------:|
| J-04 | 🔴 | S1 | S1 | ⬜ |
| J-02 | 🔴 | S2 | S2 | ⬜ |
| J-03 | 🔴 | S3 | S3 | ⬜ |
| J-01 | 🔴 | S4-S5 | S5 | ⬜ |
| J-05 | 🟡 | S5 | S5 | ⬜ |
| J-06 | 🟡 | S3 | S3-S4 | ⬜ |
| J-07 | 🟡 | S3 | S3-S4 | ⬜ |
| J-08 | 🟡 | S4 | S4 | ⬜ |
| J-09 | 🟡 | S5 | S5 | ⬜ |
| J-10 | 🟡 | S6 | S6 | ⬜ |
| J-11 | ⚪ | S6 | S6 | ⬜ |
| J-12 | ⚪ | S6 | S6 | ⬜ |
| J-13 | ⚪ | S7 | S7 | ⬜ |

---

## Roles del Equipo

| Rol | Actor | Responsabilidad por Sprint |
|-----|-------|---------------------------|
| 👤 **Jefe de Equipo** | Harolt | Prioriza US → Confirma scope → UAT manual → Gate Final |
| 📋 **Agente PO** | Agente IA | Refina CAs → Escribe UAT → Gate Funcional |
| ★ **Arquitecto Lead** | Agente IA Lead | Descompone US → Genera handoffs → Audita entregas → Gate Técnico |
| 🔧 **Backend Agent** | Agente IA ejecutor | Implementa endpoints + dominio + tests unitarios |
| 🎨 **Frontend Agent** | Agente IA ejecutor | Implementa stores + vistas + integración API |
| 🧪 **QA Agent** | Agente IA ejecutor | Valida funcional + escribe E2E Playwright |

---

## Protocolo ACP (Resumen)

```
Medio:     .agentic-sync/ (Git-tracked)
Formato:   handoff_{rol}_{US}_{CAs}.md
Git:       Solo ★ Lead ejecuta commits
Agentes:   git stash save "temp-{agent}"
Auditoría: git diff focalizado por fases
```

Ver detalles completos en [sprint_plan_s0.md](./sprint_plan_s0.md) §Día 0.
