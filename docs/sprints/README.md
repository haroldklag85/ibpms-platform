# 📋 Roadmap de Sprints — iBPMS V1

> **Modelo:** Propuesta C — Dual Track con Orquestación Multi-Agente (ACP)  
> **Velocidad:** 4 días/sprint | 7-10 US/sprint (objetivo post S3)  
> **Última actualización:** 2026-04-10  
> **Autor:** Arquitecto Líder SW

---

## Visión General (CODE FREEZE ACTIVO)

```
S0 (Foundation) → Sprint PUENTE (Deuda US-001/002) → S1 (Hardening Unitario/API) → S2 (Playwright UAT)
                                                                                          │
                                                                                          ▼
                                                                        S3 (Bugs & Coverage Expansión)
```

---

## Sprint Index (Estrategia O-A: UAT Driven)

| Sprint | Título | Objetivo | US Scope Funcional | Estado | Plan |
|:------:|--------|:--------------|:--------|:------:|------|
| **S0** | Infraestructura Funcional | Gate E2E Base | N/A | ✅ Completado | [sprint_plan_s0.md](./sprint_plan_s0.md) |
| **P-0**| **Sprint Puente** | Llenar Código Faltante | US-001, US-002 | 🔨 Construcción | [sprint_plan_puente.md](./sprint_plan_puente.md) |
| **S1** | Test Pyramid (Alt. B) | Aislar y testear APIs | 11 US Completadas | ⬜ Pendiente | [sprint_plan_s1.md](./sprint_plan_s1.md) |
| **S2** | Playwright UAT | Guiado por Modelos UAT | 11 US Completadas | ⬜ Pendiente | [sprint_plan_s2.md](./sprint_plan_s2.md) |
| **S3** | Expansión + Features | Retornar al Desarrollo | TBD | ⬜ Pendiente | [sprint_plan_s3.md](./sprint_plan_s3.md) |

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
